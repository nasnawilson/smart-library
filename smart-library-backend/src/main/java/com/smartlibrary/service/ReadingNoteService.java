package com.smartlibrary.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartlibrary.dto.ReadingNoteDTO;
import com.smartlibrary.entity.ReadingNote;
import com.smartlibrary.mapper.ReadingNoteMapper;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 读书笔记服务层。
 *
 * 新 PRD 未包含读书笔记表，保留空实现用于兼容旧前端。
 */
@Service
public class ReadingNoteService extends ServiceImpl<ReadingNoteMapper, ReadingNote> {

    /**
     * 创建笔记
     */
    public void create(ReadingNoteDTO dto, Long userId) {
        // 新 PRD 暂不落库。
    }

    /**
     * 更新笔记
     */
    public void update(ReadingNoteDTO dto, Long userId) {
        // 新 PRD 暂不落库。
    }

    /**
     * 查询用户的笔记列表
     */
    public List<ReadingNote> getUserNotes(Long userId) {
        return Collections.emptyList();
    }

    /**
     * 查询图书的笔记列表
     */
    public List<ReadingNote> getBookNotes(Long bookId) {
        return Collections.emptyList();
    }

    /**
     * 查询笔记详情
     */
    public ReadingNote getDetail(Long id) {
        return null;
    }

    @Override
    public boolean removeById(Serializable id) {
        return true;
    }
}
