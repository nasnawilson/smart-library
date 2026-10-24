package com.smartlibrary.controller;

import com.smartlibrary.dto.Result;
import com.smartlibrary.dto.SeatReserveDTO;
import com.smartlibrary.entity.SeatReservation;
import com.smartlibrary.exception.BusinessException;
import com.smartlibrary.service.SeatReservationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 座位预约控制器
 * 处理座位预约相关接口
 */
@RestController
@RequestMapping("/reservations")
public class SeatReservationController {

    @Autowired
    private SeatReservationService seatReservationService;

    /**
     * 预约座位
     */
    @PostMapping
    public Result<Void> reserve(@Valid @RequestBody SeatReserveDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        if ("ADMIN".equals(role)) {
            throw new BusinessException("管理员不具备学生预约座位功能，请在座位管理中维护座位状态");
        }
        seatReservationService.reserve(dto, userId);
        return Result.success();
    }

    /**
     * 签到
     */
    @PostMapping("/{id}/check-in")
    public Result<Void> checkIn(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        if ("ADMIN".equals(role)) {
            throw new BusinessException("管理员不具备学生签到功能");
        }
        seatReservationService.checkIn(id, userId);
        return Result.success();
    }

    /**
     * 取消预约
     */
    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        if ("ADMIN".equals(role)) {
            throw new BusinessException("管理员不具备学生取消预约功能");
        }
        seatReservationService.cancel(id, userId);
        return Result.success();
    }

    /**
     * 完成预约（释放座位）
     */
    @PostMapping("/{id}/complete")
    public Result<Void> complete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        if ("ADMIN".equals(role)) {
            throw new BusinessException("管理员不具备学生完成预约功能，请在座位管理中维护座位状态");
        }
        seatReservationService.complete(id, userId);
        return Result.success();
    }

    /**
     * 查询当前用户的预约记录
     */
    @GetMapping("/my")
    public Result<List<SeatReservation>> getMyReservations(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(seatReservationService.getUserReservations(userId));
    }
}
