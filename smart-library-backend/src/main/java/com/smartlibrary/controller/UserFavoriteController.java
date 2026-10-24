package com.smartlibrary.controller;

import com.smartlibrary.dto.Result;
import com.smartlibrary.entity.UserFavorite;
import com.smartlibrary.service.UserFavoriteService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户收藏控制器
 * 处理收藏相关接口
 */
@RestController
@RequestMapping("/favorites")
public class UserFavoriteController {

    @Autowired
    private UserFavoriteService userFavoriteService;

    /**
     * 收藏
     */
    @PostMapping
    public Result<Void> favorite(@RequestParam String targetType,
                                  @RequestParam Long targetId,
                                  HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        userFavoriteService.favorite(userId, targetType, targetId);
        return Result.success();
    }

    /**
     * 取消收藏
     */
    @DeleteMapping
    public Result<Void> unfavorite(@RequestParam String targetType,
                                    @RequestParam Long targetId,
                                    HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        userFavoriteService.unfavorite(userId, targetType, targetId);
        return Result.success();
    }

    /**
     * 查询当前用户的收藏列表
     */
    @GetMapping("/my")
    public Result<List<UserFavorite>> getMyFavorites(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(userFavoriteService.getUserFavorites(userId));
    }

    /**
     * 检查是否已收藏
     */
    @GetMapping("/check")
    public Result<Boolean> checkFavorite(@RequestParam String targetType,
                                          @RequestParam Long targetId,
                                          HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(userFavoriteService.isFavorited(userId, targetType, targetId));
    }
}
