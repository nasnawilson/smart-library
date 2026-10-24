package com.smartlibrary.controller;

import com.smartlibrary.dto.ReadingRecordDTO;
import com.smartlibrary.dto.Result;
import com.smartlibrary.entity.ReadingRecord;
import com.smartlibrary.service.ReadingRecordService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 阅读记录控制器
 * 处理阅读打卡相关接口
 */
@RestController
@RequestMapping("/reading-records")
public class ReadingRecordController {

    @Autowired
    private ReadingRecordService readingRecordService;

    /**
     * 记录阅读时长
     */
    @PostMapping
    public Result<Void> record(@Valid @RequestBody ReadingRecordDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        readingRecordService.record(dto, userId);
        return Result.success();
    }

    /**
     * 查询当前用户的阅读记录
     */
    @GetMapping("/my")
    public Result<List<ReadingRecord>> getMyRecords(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(readingRecordService.getUserRecords(userId));
    }

    /**
     * 查询每日阅读统计
     */
    @GetMapping("/daily-stats")
    public Result<List<Map<String, Object>>> getDailyStats(
            @RequestParam String startDate,
            @RequestParam String endDate,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(readingRecordService.getDailyStats(
                userId, LocalDate.parse(startDate), LocalDate.parse(endDate)));
    }

    /**
     * 查询图书阅读统计
     */
    @GetMapping("/book-stats")
    public Result<List<Map<String, Object>>> getBookStats(
            @RequestParam String startDate,
            @RequestParam String endDate,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(readingRecordService.getBookStats(
                userId, LocalDate.parse(startDate), LocalDate.parse(endDate)));
    }

    /**
     * 查询总阅读时长和图书数量
     */
    @GetMapping("/total")
    public Result<Map<String, Integer>> getTotal(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Integer totalDuration = readingRecordService.getTotalDuration(userId);
        Integer totalBooks = readingRecordService.getTotalBooks(userId);
        return Result.success(Map.of("totalDuration", totalDuration, "totalBooks", totalBooks));
    }
}
