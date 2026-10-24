package com.smartlibrary.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.smartlibrary.dto.DashboardStatsDTO;
import com.smartlibrary.dto.Result;
import com.smartlibrary.entity.BorrowRecord;
import com.smartlibrary.entity.SeatReservation;
import com.smartlibrary.service.BookService;
import com.smartlibrary.service.BorrowRecordService;
import com.smartlibrary.service.SeatReservationService;
import com.smartlibrary.service.SeatService;
import com.smartlibrary.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {
    /** 借阅趋势图的展示天数 */
    private static final int TREND_DAYS = 7;
    /** 最近借阅列表的条数 */
    private static final int RECENT_LIMIT = 8;

    @Autowired private BookService bookService;
    @Autowired private UserService userService;
    @Autowired private BorrowRecordService borrowRecordService;
    @Autowired private SeatService seatService;
    @Autowired private SeatReservationService reservationService;

    @GetMapping("/stats")
    public Result<DashboardStatsDTO> getStats() {
        DashboardStatsDTO stats = new DashboardStatsDTO();
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime tomorrowStart = today.plusDays(1).atStartOfDay();

        stats.setTotalBooks(bookService.count());
        stats.setTotalUsers(userService.count());

        // 今日借阅：按借出时间落在今天来算
        stats.setTodayBorrows(borrowRecordService.count(new QueryWrapper<BorrowRecord>()
                .ge("borrow_time", todayStart)
                .lt("borrow_time", tomorrowStart)));

        // 逾期未还：还没归还（含已提交归还申请待审核）且已过应还时间
        stats.setOverdueCount(borrowRecordService.count(new QueryWrapper<BorrowRecord>()
                .in("return_flag", 0, 2)
                .lt("due_time", LocalDateTime.now())));

        stats.setFreeSeats((long) seatService.listByFloor(1).stream().filter(s -> "FREE".equals(s.getStatus())).count()
                + seatService.listByFloor(2).stream().filter(s -> "FREE".equals(s.getStatus())).count());
        stats.setOccupiedSeats((long) seatService.listByFloor(1).stream().filter(s -> "OCCUPIED".equals(s.getStatus())).count()
                + seatService.listByFloor(2).stream().filter(s -> "OCCUPIED".equals(s.getStatus())).count());

        // 今日预约：座位预约里日期为今天、且不是已取消/已完成的记录
        stats.setTodayReservations(reservationService.count(new QueryWrapper<SeatReservation>()
                .eq("reserve_date", today)
                .in("status", 0, 1, 3)));

        List<Map<String, Object>> categoryStats = new ArrayList<>();
        for (String category : bookService.getAllCategories()) {
            Map<String, Object> item = new HashMap<>();
            item.put("category", category);
            item.put("count", bookService.list().stream().filter(b -> category.equals(b.getCategory())).count());
            categoryStats.add(item);
        }
        stats.setCategoryStats(categoryStats);
        stats.setBorrowTrend(buildBorrowTrend(today));
        stats.setRecentBorrows(buildRecentBorrows());
        return Result.success(stats);
    }

    /**
     * 近 7 天借阅趋势：没有借阅的那天也补 0，保证折线图横轴连续
     */
    private List<Map<String, Object>> buildBorrowTrend(LocalDate today) {
        LocalDate start = today.minusDays(TREND_DAYS - 1L);
        Map<LocalDate, Long> counted = borrowRecordService.countByBorrowDate(start);
        List<Map<String, Object>> trend = new ArrayList<>();
        for (int i = 0; i < TREND_DAYS; i++) {
            LocalDate day = start.plusDays(i);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", day.toString());
            item.put("count", counted.getOrDefault(day, 0L));
            trend.add(item);
        }
        return trend;
    }

    /**
     * 最近借阅：把实体转成前端表格直接可渲染的字段
     */
    private List<Map<String, Object>> buildRecentBorrows() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (BorrowRecord record : borrowRecordService.getRecentBorrows(RECENT_LIMIT)) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", record.getId());
            item.put("bookTitle", record.getBookTitle());
            item.put("userName", record.getUserName());
            item.put("studentNo", record.getStudentNo());
            item.put("borrowDate", record.getBorrowTime() == null ? null : record.getBorrowTime().toLocalDate().toString());
            item.put("dueDate", record.getDueTime() == null ? null : record.getDueTime().toLocalDate().toString());
            item.put("status", record.getStatus());
            result.add(item);
        }
        return result;
    }
}
