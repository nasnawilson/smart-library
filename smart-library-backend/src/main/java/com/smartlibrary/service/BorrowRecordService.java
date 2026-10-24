package com.smartlibrary.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartlibrary.dto.BorrowQueryDTO;
import com.smartlibrary.dto.PageResult;
import com.smartlibrary.entity.Book;
import com.smartlibrary.entity.BorrowRecord;
import com.smartlibrary.entity.User;
import com.smartlibrary.exception.BusinessException;
import com.smartlibrary.mapper.BorrowRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BorrowRecordService extends ServiceImpl<BorrowRecordMapper, BorrowRecord> {
    @Autowired private BookService bookService;
    @Autowired private UserService userService;
    private static final long MIN_BORROW_HOURS = 12;
    private static final long MAX_BORROW_HOURS = 720;

    @Transactional
    public void borrowBook(Long bookId, Long userId) {
        borrowBook(bookId, userId, MAX_BORROW_HOURS);
    }

    @Transactional
    public void borrowBook(Long bookId, Long userId, Long borrowHours) {
        long hours = validateBorrowHours(borrowHours);
        Book book = bookService.getById(bookId);
        if (book == null) throw new BusinessException("图书不存在");
        long sameBookCount = count(new QueryWrapper<BorrowRecord>()
                .eq("user_id", userId)
                .eq("book_id", bookId)
                .in("return_flag", 0, 2));
        if (sameBookCount > 0) {
            throw new BusinessException("同一账户同一本书只能借阅一本，请先归还后再借");
        }
        long activeBorrowCount = count(new QueryWrapper<BorrowRecord>()
                .eq("user_id", userId)
                .in("return_flag", 0, 2));
        if (activeBorrowCount >= 5) {
            throw new BusinessException("每个账户最多同时借阅5本不同图书");
        }
        if ((book.getAvailableCount() == null ? 0 : book.getAvailableCount()) <= 0) throw new BusinessException("图书库存不足");
        BorrowRecord record = new BorrowRecord();
        record.setUserId(userId);
        record.setBookId(bookId);
        LocalDateTime now = LocalDateTime.now();
        record.setBorrowTime(now);
        record.setDueTime(now.plusHours(hours));
        record.setRenewFlag(0);
        record.setReturnFlag(0);
        save(record);
        Book update = new Book();
        update.setId(bookId);
        update.setBorrowNum((book.getBorrowNum() == null ? 0 : book.getBorrowNum()) + 1);
        update.setInLibrary(update.getBorrowNum() < book.getTotalCount() ? 1 : 0);
        bookService.updateById(update);
    }

    private long validateBorrowHours(Long borrowHours) {
        long hours = borrowHours == null ? MAX_BORROW_HOURS : borrowHours;
        if (hours < MIN_BORROW_HOURS) {
            throw new BusinessException("借阅时间最小不能小于半天（12小时）");
        }
        if (hours > MAX_BORROW_HOURS) {
            throw new BusinessException("借阅时间最大不能超过一个月");
        }
        return hours;
    }

    @Transactional
    public void returnBook(Long id, Long operatorUserId, String operatorRole) {
        BorrowRecord record = getById(id);
        if (record == null || record.getReturnFlag() == 1) throw new BusinessException("借阅记录不存在或已归还");
        if (!"ADMIN".equals(operatorRole)) {
            if (!record.getUserId().equals(operatorUserId)) {
                throw new BusinessException("只能申请归还自己借阅的图书");
            }
            if (record.getReturnFlag() != null && record.getReturnFlag() == 2) {
                throw new BusinessException("已提交归还申请，请等待管理员审核");
            }
            record.setReturnFlag(2);
            updateById(record);
            return;
        }
        if (record.getReturnFlag() != null && record.getReturnFlag() != 0 && record.getReturnFlag() != 2) {
            throw new BusinessException("当前借阅记录状态不可归还");
        }
        record.setReturnFlag(1);
        updateById(record);
        Book book = bookService.getById(record.getBookId());
        if (book != null) {
            Book update = new Book();
            update.setId(book.getId());
            update.setBorrowNum(Math.max(0, (book.getBorrowNum() == null ? 0 : book.getBorrowNum()) - 1));
            update.setInLibrary(1);
            bookService.updateById(update);
        }
    }

    public void renewBook(Long id, Long operatorUserId) {
        BorrowRecord record = getById(id);
        if (record == null || record.getReturnFlag() == 1) throw new BusinessException("借阅记录不存在或已归还");
        if (record.getReturnFlag() != null && record.getReturnFlag() == 2) throw new BusinessException("归还申请审核中，不能续借");
        if (!record.getUserId().equals(operatorUserId)) {
            throw new BusinessException("只能续借自己借阅的图书");
        }
        if (record.getRenewFlag() == 1) throw new BusinessException("每本书只能续借一次");
        record.setRenewFlag(1);
        record.setDueTime(record.getDueTime().plusDays(30));
        updateById(record);
    }

    public PageResult<BorrowRecord> pageQuery(BorrowQueryDTO dto) {
        QueryWrapper<BorrowRecord> wrapper = new QueryWrapper<BorrowRecord>().orderByDesc("borrow_time");
        if (dto.getUserId() != null) {
            wrapper.eq("user_id", dto.getUserId());
        }
        Page<BorrowRecord> page = page(new Page<>(dto.getPage(), dto.getSize()), wrapper);
        page.getRecords().forEach(this::fillView);
        return PageResult.of(page);
    }

    public List<BorrowRecord> getUserBorrows(Long userId) {
        return list(new QueryWrapper<BorrowRecord>().eq("user_id", userId).orderByDesc("borrow_time")).stream().peek(this::fillView).collect(Collectors.toList());
    }

    public void checkOverdue() {
        list(new QueryWrapper<BorrowRecord>().eq("return_flag", 0).lt("due_time", LocalDateTime.now())).forEach(r -> {});
    }

    public String remindReturn(Long id) {
        BorrowRecord record = getById(id);
        if (record == null || record.getReturnFlag() != null && record.getReturnFlag() == 1) {
            throw new BusinessException("借阅记录不存在或已归还");
        }
        fillView(record);
        if (!"OVERDUE".equals(record.getStatus())) {
            throw new BusinessException("仅逾期未归还记录需要提醒");
        }
        return "已提醒用户 " + record.getUserName() + "（" + record.getStudentNo() + "）尽快归还《" + record.getBookTitle() + "》";
    }

    private void fillView(BorrowRecord record) {
        if (record.getBorrowTime() != null) record.setBorrowDate(record.getBorrowTime().toLocalDate());
        if (record.getDueTime() != null) record.setDueDate(record.getDueTime().toLocalDate());
        record.setRenewCount(record.getRenewFlag() == null ? 0 : record.getRenewFlag());
        if (record.getReturnFlag() != null && record.getReturnFlag() == 1) {
            record.setStatus("RETURNED");
        } else if (record.getReturnFlag() != null && record.getReturnFlag() == 2) {
            record.setStatus("RETURN_REQUESTED");
        } else {
            record.setStatus(record.getDueTime() != null && record.getDueTime().isBefore(LocalDateTime.now()) ? "OVERDUE" : "BORROWED");
        }
        Book book = bookService.getById(record.getBookId());
        if (book != null) record.setBookTitle(book.getTitle());
        User user = userService.getById(record.getUserId());
        if (user != null) {
            record.setUserName(user.getName());
            record.setStudentNo(user.getStudentNo());
        }
        record.setCreateTime(record.getBorrowTime());
    }
}
