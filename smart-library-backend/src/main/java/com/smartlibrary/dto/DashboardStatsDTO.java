package com.smartlibrary.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 仪表盘统计数据DTO
 */
@Data
public class DashboardStatsDTO {

    /** 图书总数 */
    private Long totalBooks;

    /** 读者数量 */
    private Long totalUsers;

    /** 今日借阅数 */
    private Long todayBorrows;

    /** 逾期未还数 */
    private Long overdueCount;

    /** 空闲座位数 */
    private Long freeSeats;

    /** 已占用座位数 */
    private Long occupiedSeats;

    /** 今日预约数 */
    private Long todayReservations;

    /** 分类图书数量统计 */
    private List<Map<String, Object>> categoryStats;

    /** 最近7天借阅趋势 */
    private List<Map<String, Object>> borrowTrend;

    /** 最近借阅记录 */
    private List<Map<String, Object>> recentBorrows;
}
