package com.smartlibrary.controller;

import com.smartlibrary.dto.PageResult;
import com.smartlibrary.dto.Result;
import com.smartlibrary.entity.User;
import com.smartlibrary.exception.BusinessException;
import com.smartlibrary.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户控制器
 * 处理用户管理相关接口
 */
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 分页查询用户列表
     */
    @GetMapping
    public Result<PageResult<User>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String studentNo,
            @RequestParam(required = false) String role,
            HttpServletRequest request) {
        requireAdmin(request);
        return Result.success(userService.pageQuery(page, size, name, studentNo, role));
    }

    /**
     * 根据ID查询用户
     */
    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id, HttpServletRequest request) {
        requireAdmin(request);
        return Result.success(userService.getById(id));
    }

    /**
     * 新增用户
     */
    @PostMapping
    public Result<Void> save(@RequestBody User user, HttpServletRequest request) {
        requireAdmin(request);
        userService.createUser(user);
        return Result.success();
    }

    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody User user, HttpServletRequest request) {
        requireAdmin(request);
        user.setId(id);
        userService.updateUser(user);
        return Result.success();
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        requireAdmin(request);
        userService.removeById(id);
        return Result.success();
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public Result<Void> changePassword(@RequestBody Map<String, String> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        userService.changePassword(userId, oldPassword, newPassword);
        return Result.success();
    }

    private void requireAdmin(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            throw new BusinessException("仅管理员可访问读者管理");
        }
    }
}
