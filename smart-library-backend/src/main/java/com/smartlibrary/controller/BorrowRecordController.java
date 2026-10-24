package com.smartlibrary.controller;

import com.smartlibrary.dto.BorrowQueryDTO;
import com.smartlibrary.dto.PageResult;
import com.smartlibrary.dto.Result;
import com.smartlibrary.entity.BorrowRecord;
import com.smartlibrary.exception.BusinessException;
import com.smartlibrary.service.BorrowRecordService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 借阅记录控制器
 * 处理借阅管理相关接口
 */
@RestController
@RequestMapping("/borrows")
public class BorrowRecordController {

    @Autowired
    private BorrowRecordService borrowRecordService;

    /**
     * 分页查询借阅记录
     */
    @GetMapping
    public Result<PageResult<BorrowRecord>> list(BorrowQueryDTO queryDTO, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            queryDTO.setUserId((Long) request.getAttribute("userId"));
        }
        return Result.success(borrowRecordService.pageQuery(queryDTO));
    }

    /**
     * 借阅图书
     */
    @PostMapping
    public Result<Void> borrow(@RequestBody Map<String, Long> params, HttpServletRequest request) {
        Long bookId = params.get("bookId");
        Long operatorUserId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        if ("ADMIN".equals(role)) {
            throw new BusinessException("管理员不具备学生借书功能，请在借阅管理中维护借阅状态");
        }
        Long targetUserId = "ADMIN".equals(role) && params.get("userId") != null ? params.get("userId") : operatorUserId;
        borrowRecordService.borrowBook(bookId, targetUserId, params.get("borrowHours"));
        return Result.success();
    }

    /**
     * 归还图书
     */
    @PutMapping("/{id}/return")
    public Result<Void> returnBook(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        borrowRecordService.returnBook(id, userId, role);
        return Result.success();
    }

    /**
     * 续借图书
     */
    @PutMapping("/{id}/renew")
    public Result<Void> renew(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        if ("ADMIN".equals(role)) {
            throw new BusinessException("管理员不具备学生续借功能");
        }
        borrowRecordService.renewBook(id, userId);
        return Result.success();
    }

    /**
     * 管理员提醒逾期用户归还图书
     */
    @PostMapping("/{id}/remind")
    public Result<String> remindReturn(@PathVariable Long id, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            throw new BusinessException("仅管理员可发送归还提醒");
        }
        return Result.success(borrowRecordService.remindReturn(id));
    }

    /**
     * 查询当前用户的借阅记录
     */
    @GetMapping("/my")
    public Result<List<BorrowRecord>> getMyBorrows(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(borrowRecordService.getUserBorrows(userId));
    }
}
