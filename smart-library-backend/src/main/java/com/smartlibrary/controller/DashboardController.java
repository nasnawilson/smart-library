package com.smartlibrary.controller;

import com.smartlibrary.dto.DashboardStatsDTO;
import com.smartlibrary.dto.Result;
import com.smartlibrary.service.BookService;
import com.smartlibrary.service.BorrowRecordService;
import com.smartlibrary.service.SeatReservationService;
import com.smartlibrary.service.SeatService;
import com.smartlibrary.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {
    @Autowired private BookService bookService;
    @Autowired private UserService userService;
    @Autowired private BorrowRecordService borrowRecordService;
    @Autowired private SeatService seatService;
    @Autowired private SeatReservationService reservationService;

    @GetMapping("/stats")
    public Result<DashboardStatsDTO> getStats() {
        DashboardStatsDTO stats = new DashboardStatsDTO();
        stats.setTotalBooks(bookService.count());
        stats.setTotalUsers(userService.count());
        stats.setTodayBorrows(0L);
        stats.setOverdueCount(0L);
        stats.setFreeSeats((long) seatService.listByFloor(1).stream().filter(s -> "FREE".equals(s.getStatus())).count()
                + seatService.listByFloor(2).stream().filter(s -> "FREE".equals(s.getStatus())).count());
        stats.setOccupiedSeats((long) seatService.listByFloor(1).stream().filter(s -> "OCCUPIED".equals(s.getStatus())).count()
                + seatService.listByFloor(2).stream().filter(s -> "OCCUPIED".equals(s.getStatus())).count());
        stats.setTodayReservations(0L);
        List<Map<String, Object>> categoryStats = new ArrayList<>();
        for (String category : bookService.getAllCategories()) {
            Map<String, Object> item = new HashMap<>();
            item.put("category", category);
            item.put("count", bookService.list().stream().filter(b -> category.equals(b.getCategory())).count());
            categoryStats.add(item);
        }
        stats.setCategoryStats(categoryStats);
        stats.setBorrowTrend(new ArrayList<>());
        stats.setRecentBorrows(new ArrayList<>());
        return Result.success(stats);
    }
}
