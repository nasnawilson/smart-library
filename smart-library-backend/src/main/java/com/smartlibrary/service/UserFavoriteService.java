package com.smartlibrary.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartlibrary.entity.UserFavorite;
import com.smartlibrary.mapper.UserFavoriteMapper;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 用户收藏服务层。
 *
 * 新 PRD 未包含收藏表，因此这里保留接口兼容，避免旧前端页面访问时报错。
 */
@Service
public class UserFavoriteService extends ServiceImpl<UserFavoriteMapper, UserFavorite> {

    public void favorite(Long userId, String targetType, Long targetId) {
        // 新 PRD 暂不落库，保留接口兼容。
    }

    public void unfavorite(Long userId, String targetType, Long targetId) {
        // 新 PRD 暂不落库，保留接口兼容。
    }

    public List<UserFavorite> getUserFavorites(Long userId) {
        return Collections.emptyList();
    }

    public boolean isFavorited(Long userId, String targetType, Long targetId) {
        return false;
    }
}
