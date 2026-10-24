package com.smartlibrary.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartlibrary.dto.ReadingRecordDTO;
import com.smartlibrary.entity.ReadingRecord;
import com.smartlibrary.mapper.ReadingRecordMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 阅读记录服务层。
 *
 * 新 PRD 未包含阅读记录表，保留空实现用于兼容旧前端。
 */
@Service
public class ReadingRecordService extends ServiceImpl<ReadingRecordMapper, ReadingRecord> {

    /**
     * 记录阅读时长
     */
    public void record(ReadingRecordDTO dto, Long userId) {
        // 新 PRD 暂不落库。
    }

    /**
     * 查询用户的阅读记录
     */
    public List<ReadingRecord> getUserRecords(Long userId) {
        return Collections.emptyList();
    }

    /**
     * 查询用户某段时间的每日阅读统计
     */
    public List<Map<String, Object>> getDailyStats(Long userId, LocalDate startDate, LocalDate endDate) {
        return Collections.emptyList();
    }

    /**
     * 查询用户某段时间的图书阅读统计
     */
    public List<Map<String, Object>> getBookStats(Long userId, LocalDate startDate, LocalDate endDate) {
        return Collections.emptyList();
    }

    /**
     * 查询用户总阅读时长
     */
    public Integer getTotalDuration(Long userId) {
        return 0;
    }

    /**
     * 查询用户阅读图书数量
     */
    public Integer getTotalBooks(Long userId) {
        return 0;
    }
}
