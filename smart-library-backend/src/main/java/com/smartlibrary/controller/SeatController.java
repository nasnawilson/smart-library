package com.smartlibrary.controller;

import com.smartlibrary.dto.Result;
import com.smartlibrary.dto.SeatQueryDTO;
import com.smartlibrary.entity.Seat;
import com.smartlibrary.service.SeatReservationService;
import com.smartlibrary.service.SeatService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 座位控制器
 * 处理座位管理相关接口
 */
@RestController
@RequestMapping("/seats")
public class SeatController {

    @Autowired
    private SeatService seatService;

    @Autowired
    private SeatReservationService seatReservationService;

    /**
     * 查询座位列表
     */
    @GetMapping
    public Result<List<Seat>> list(SeatQueryDTO queryDTO, HttpServletRequest request) {
        boolean isAdmin = "ADMIN".equals(request.getAttribute("role"));
        return Result.success(seatService.list(queryDTO, isAdmin));
    }

    /**
     * 查询某楼层的座位（带用户信息）
     */
    @GetMapping("/floor/{floor}")
    public Result<List<Seat>> listByFloor(@PathVariable Integer floor, HttpServletRequest request) {
        boolean isAdmin = "ADMIN".equals(request.getAttribute("role"));
        return Result.success(seatService.listByFloor(floor, isAdmin));
    }

    /**
     * 查询座位状态版本号，用于前端轻量判断是否需要刷新列表
     */
    @GetMapping("/version")
    public Result<Integer> version() {
        return Result.success(seatService.getSeatVersion());
    }

    /**
     * 占用座位（现场选座）
     */
    @PostMapping("/{id}/occupy")
    public Result<Void> occupy(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        seatReservationService.occupyNow(id, userId);
        return Result.success();
    }

    /**
     * 释放座位
     */
    @PostMapping("/{id}/release")
    public Result<Void> release(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        seatReservationService.releaseActiveSeat(id, userId, role);
        return Result.success();
    }

    /**
     * 暂离
     */
    @PostMapping("/{id}/temp-leave")
    public Result<Void> tempLeave(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        seatReservationService.markTempLeave(id, userId, role);
        seatService.tempLeave(id, userId);
        return Result.success();
    }

    /**
     * 暂离归来
     */
    @PostMapping("/{id}/return")
    public Result<Void> returnFromTempLeave(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        seatReservationService.markReturnFromTempLeave(id, userId, role);
        seatService.returnFromTempLeave(id, userId);
        return Result.success();
    }
}
