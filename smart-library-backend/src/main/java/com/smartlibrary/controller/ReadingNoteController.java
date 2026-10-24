package com.smartlibrary.controller;

import com.smartlibrary.dto.ReadingNoteDTO;
import com.smartlibrary.dto.Result;
import com.smartlibrary.entity.ReadingNote;
import com.smartlibrary.service.ReadingNoteService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 读书笔记控制器
 * 处理读书笔记相关接口
 */
@RestController
@RequestMapping("/reading-notes")
public class ReadingNoteController {

    @Autowired
    private ReadingNoteService readingNoteService;

    /**
     * 创建笔记
     */
    @PostMapping
    public Result<Void> create(@Valid @RequestBody ReadingNoteDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        readingNoteService.create(dto, userId);
        return Result.success();
    }

    /**
     * 更新笔记
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ReadingNoteDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        dto.setId(id);
        readingNoteService.update(dto, userId);
        return Result.success();
    }

    /**
     * 删除笔记
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        readingNoteService.removeById(id, userId);
        return Result.success();
    }

    /**
     * 查询当前用户的笔记列表
     */
    @GetMapping("/my")
    public Result<List<ReadingNote>> getMyNotes(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(readingNoteService.getUserNotes(userId));
    }

    /**
     * 查询某图书的笔记列表
     */
    @GetMapping("/book/{bookId}")
    public Result<List<ReadingNote>> getBookNotes(@PathVariable Long bookId) {
        return Result.success(readingNoteService.getBookNotes(bookId));
    }

    /**
     * 查询笔记详情
     */
    @GetMapping("/{id}")
    public Result<ReadingNote> getById(@PathVariable Long id) {
        return Result.success(readingNoteService.getDetail(id));
    }
}
