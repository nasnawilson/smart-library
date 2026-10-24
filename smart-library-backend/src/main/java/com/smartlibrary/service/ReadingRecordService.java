package com.smartlibrary.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartlibrary.dto.ReadingRecordDTO;
import com.smartlibrary.entity.Book;
import com.smartlibrary.entity.ReadingRecord;
import com.smartlibrary.exception.BusinessException;
import com.smartlibrary.mapper.ReadingRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * 阅读记录服务层。
 *
 * 数据来源为 reading_record 表：每次打卡落一条记录，
 * 学习中心的累计时长、阅读天数、每日趋势、图书分布都基于这张表实时统计。
 */
@Service
public class ReadingRecordService extends ServiceImpl<ReadingRecordMapper, ReadingRecord> {

    @Autowired
    private BookService bookService;

    /**
     * 记录阅读时长：同一天同一本书重复打卡会累加到已有记录上，避免刷出一堆碎片行。
     */
    public void record(ReadingRecordDTO dto, Long userId) {
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        if (dto.getBookId() == null) {
            throw new BusinessException("请选择阅读的图书");
        }
        Book book = bookService.getById(dto.getBookId());
        if (book == null) {
            throw new BusinessException("图书不存在");
        }
        LocalDate readDate = dto.getReadDate() == null ? LocalDate.now() : dto.getReadDate();
        int minutes = dto.getDurationMinutes() == null ? 0 : dto.getDurationMinutes();
        if (minutes <= 0) {
            throw new BusinessException("阅读时长必须大于 0 分钟");
        }
        ReadingRecord exist = getOne(new QueryWrapper<ReadingRecord>()
                .eq("user_id", userId)
                .eq("book_id", dto.getBookId())
                .eq("read_date", readDate)
                .last("limit 1"), false);
        if (exist == null) {
            ReadingRecord record = new ReadingRecord();
            record.setUserId(userId);
            record.setBookId(dto.getBookId());
            record.setReadDate(readDate);
            record.setDurationMinutes(minutes);
            record.setCreateTime(LocalDateTime.now());
            save(record);
        } else {
            exist.setDurationMinutes((exist.getDurationMinutes() == null ? 0 : exist.getDurationMinutes()) + minutes);
            updateById(exist);
        }
    }

    /**
     * 查询用户的阅读记录（按日期倒序，附带书名和作者）
     */
    public List<ReadingRecord> getUserRecords(Long userId) {
        if (userId == null) {
            return new ArrayList<>();
        }
        List<ReadingRecord> records = list(new QueryWrapper<ReadingRecord>()
                .eq("user_id", userId)
                .orderByDesc("read_date")
                .orderByDesc("id"));
        records.forEach(this::fillBook);
        return records;
    }

    /**
     * 查询用户某段时间的每日阅读统计。
     *
     * 只返回「确实读过」的日期，前端用条数当打卡天数，
     * date 统一格式化为 yyyy-MM-dd，供趋势图与热力图直接比对。
     */
    public List<Map<String, Object>> getDailyStats(Long userId, LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, Integer> daily = new TreeMap<>();
        for (ReadingRecord record : rangeRecords(userId, startDate, endDate)) {
            LocalDate day = record.getReadDate();
            if (day == null) {
                continue;
            }
            int minutes = record.getDurationMinutes() == null ? 0 : record.getDurationMinutes();
            daily.merge(day, minutes, Integer::sum);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        daily.forEach((day, minutes) -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", day.toString());
            item.put("totalMinutes", minutes);
            result.add(item);
        });
        return result;
    }

    /**
     * 查询用户某段时间各本书的阅读时长
     */
    public List<Map<String, Object>> getBookStats(Long userId, LocalDate startDate, LocalDate endDate) {
        Map<Long, Integer> minutesByBook = new LinkedHashMap<>();
        for (ReadingRecord record : rangeRecords(userId, startDate, endDate)) {
            if (record.getBookId() == null) {
                continue;
            }
            int minutes = record.getDurationMinutes() == null ? 0 : record.getDurationMinutes();
            minutesByBook.merge(record.getBookId(), minutes, Integer::sum);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        minutesByBook.entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .forEach(entry -> {
                    Book book = bookService.getById(entry.getKey());
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("bookId", entry.getKey());
                    item.put("bookTitle", book == null ? "未知图书" : book.getTitle());
                    item.put("totalMinutes", entry.getValue());
                    result.add(item);
                });
        return result;
    }

    /**
     * 查询用户总阅读时长（分钟）
     */
    public Integer getTotalDuration(Long userId) {
        if (userId == null) {
            return 0;
        }
        return list(new QueryWrapper<ReadingRecord>().eq("user_id", userId)).stream()
                .mapToInt(r -> r.getDurationMinutes() == null ? 0 : r.getDurationMinutes())
                .sum();
    }

    /**
     * 查询用户读过的图书数量（去重）
     */
    public Integer getTotalBooks(Long userId) {
        if (userId == null) {
            return 0;
        }
        return (int) list(new QueryWrapper<ReadingRecord>().eq("user_id", userId)).stream()
                .map(ReadingRecord::getBookId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .count();
    }

    private List<ReadingRecord> rangeRecords(Long userId, LocalDate startDate, LocalDate endDate) {
        if (userId == null) {
            return new ArrayList<>();
        }
        QueryWrapper<ReadingRecord> wrapper = new QueryWrapper<ReadingRecord>().eq("user_id", userId);
        if (startDate != null) {
            wrapper.ge("read_date", startDate);
        }
        if (endDate != null) {
            wrapper.le("read_date", endDate);
        }
        wrapper.orderByAsc("read_date");
        return list(wrapper);
    }

    private void fillBook(ReadingRecord record) {
        if (record.getBookId() == null) {
            return;
        }
        Book book = bookService.getById(record.getBookId());
        if (book != null) {
            record.setBookTitle(book.getTitle());
            record.setBookAuthor(book.getAuthor());
        }
    }

    /**
     * 给一组记录补书名，供别的服务复用
     */
    public List<ReadingRecord> fillBooks(List<ReadingRecord> records) {
        if (records == null) {
            return new ArrayList<>();
        }
        return records.stream().peek(this::fillBook).collect(Collectors.toList());
    }
}
