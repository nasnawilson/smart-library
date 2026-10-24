package com.smartlibrary.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartlibrary.dto.ReadingNoteDTO;
import com.smartlibrary.entity.Book;
import com.smartlibrary.entity.ReadingNote;
import com.smartlibrary.entity.User;
import com.smartlibrary.exception.BusinessException;
import com.smartlibrary.mapper.ReadingNoteMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 读书笔记服务层。
 *
 * 数据来源为 reading_note 表，删除走 @TableLogic 逻辑删除，
 * 所以历史笔记不会真的消失，只是列表里不再显示。
 */
@Service
public class ReadingNoteService extends ServiceImpl<ReadingNoteMapper, ReadingNote> {

    @Autowired
    private BookService bookService;

    @Autowired
    private UserService userService;

    /**
     * 新建笔记
     */
    public void create(ReadingNoteDTO dto, Long userId) {
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        Book book = bookService.getById(dto.getBookId());
        if (book == null) {
            throw new BusinessException("图书不存在");
        }
        ReadingNote note = new ReadingNote();
        note.setUserId(userId);
        note.setBookId(dto.getBookId());
        note.setTitle(dto.getTitle());
        note.setContent(dto.getContent());
        note.setDeleted(0);
        LocalDateTime now = LocalDateTime.now();
        note.setCreateTime(now);
        note.setUpdateTime(now);
        save(note);
    }

    /**
     * 修改笔记：只能改自己的
     */
    public void update(ReadingNoteDTO dto, Long userId) {
        if (dto.getId() == null) {
            throw new BusinessException("笔记ID不能为空");
        }
        ReadingNote note = getById(dto.getId());
        if (note == null) {
            throw new BusinessException("笔记不存在");
        }
        if (userId == null || !userId.equals(note.getUserId())) {
            throw new BusinessException("只能修改自己的笔记");
        }
        if (dto.getBookId() != null) {
            Book book = bookService.getById(dto.getBookId());
            if (book == null) {
                throw new BusinessException("图书不存在");
            }
            note.setBookId(dto.getBookId());
        }
        note.setTitle(dto.getTitle());
        note.setContent(dto.getContent());
        note.setUpdateTime(LocalDateTime.now());
        updateById(note);
    }

    /**
     * 查询某个用户的笔记列表（按更新时间倒序，附带书名与作者）
     */
    public List<ReadingNote> getUserNotes(Long userId) {
        if (userId == null) {
            return new ArrayList<>();
        }
        List<ReadingNote> notes = list(new QueryWrapper<ReadingNote>()
                .eq("user_id", userId)
                .orderByDesc("update_time")
                .orderByDesc("id"));
        notes.forEach(this::fillView);
        return notes;
    }

    /**
     * 查询某本书的全部笔记（书友共享阅读心得）
     */
    public List<ReadingNote> getBookNotes(Long bookId) {
        if (bookId == null) {
            return new ArrayList<>();
        }
        List<ReadingNote> notes = list(new QueryWrapper<ReadingNote>()
                .eq("book_id", bookId)
                .orderByDesc("update_time")
                .orderByDesc("id"));
        notes.forEach(this::fillView);
        return notes;
    }

    /**
     * 查询笔记详情
     */
    public ReadingNote getDetail(Long id) {
        if (id == null) {
            return null;
        }
        ReadingNote note = getById(id);
        if (note != null) {
            fillView(note);
        }
        return note;
    }

    /**
     * 删除笔记：沿用逻辑删除，但先校验归属，避免删到别人的
     */
    public boolean removeById(java.io.Serializable id, Long userId) {
        ReadingNote note = getById(id);
        if (note == null) {
            throw new BusinessException("笔记不存在");
        }
        if (userId == null || !userId.equals(note.getUserId())) {
            throw new BusinessException("只能删除自己的笔记");
        }
        return super.removeById(id);
    }

    private void fillView(ReadingNote note) {
        if (note.getBookId() != null) {
            Book book = bookService.getById(note.getBookId());
            if (book != null) {
                note.setBookTitle(book.getTitle());
                note.setBookAuthor(book.getAuthor());
            }
        }
        if (note.getUserId() != null) {
            User user = userService.getById(note.getUserId());
            if (user != null) {
                note.setUserName(user.getName());
            }
        }
    }
}
