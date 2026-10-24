package com.smartlibrary.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartlibrary.entity.UserViolation;
import com.smartlibrary.mapper.UserViolationMapper;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 用户违规记录服务层。
 *
 * 新 PRD 将违规次数放在 `sys_user.violation_count` 中，这里保留空实现兼容旧接口。
 */
@Service
public class UserViolationService extends ServiceImpl<UserViolationMapper, UserViolation> {

    /**
     * 查询用户的违规记录
     */
    public List<UserViolation> getUserViolations(Long userId) {
        return Collections.emptyList();
    }

    /**
     * 查询未处理的违规记录
     */
    public List<UserViolation> getUnprocessedViolations() {
        return Collections.emptyList();
    }

    /**
     * 标记违规已处理
     */
    public void markProcessed(Long id) {
        // 新 PRD 暂无独立违规记录表。
    }
}
