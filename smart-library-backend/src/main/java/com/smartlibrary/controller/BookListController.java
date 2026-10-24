package com.smartlibrary.controller;

import com.smartlibrary.dto.BookListDTO;
import com.smartlibrary.dto.Result;
import com.smartlibrary.entity.BookList;
import com.smartlibrary.entity.BookListItem;
import com.smartlibrary.service.BookListService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 书单控制器
 * 处理书单广场相关接口
 */
@RestController
@RequestMapping("/book-lists")
public class BookListController {

    @Autowired
    private BookListService bookListService;

    /**
     * 查询书单列表
     */
    @GetMapping
    public Result<List<BookList>> list() {
        return Result.success(bookListService.listAll());
    }

    /**
     * 根据ID查询书单详情
     */
    @GetMapping("/{id}")
    public Result<BookList> getById(@PathVariable Long id) {
        return Result.success(bookListService.getDetail(id));
    }

    /**
     * 查询书单图书列表
     */
    @GetMapping("/{id}/items")
    public Result<List<BookListItem>> getItems(@PathVariable Long id) {
        return Result.success(bookListService.getListItems(id));
    }

    /**
     * 创建书单
     */
    @PostMapping
    public Result<Void> create(@RequestBody BookListDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        bookListService.create(dto, userId);
        return Result.success();
    }

    /**
     * 更新书单
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody BookListDTO dto) {
        dto.setId(id);
        bookListService.update(dto);
        return Result.success();
    }

    /**
     * 删除书单
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        bookListService.delete(id);
        return Result.success();
    }

    /**
     * 点赞书单
     */
    @PostMapping("/{id}/like")
    public Result<Void> like(@PathVariable Long id) {
        bookListService.like(id);
        return Result.success();
    }
}
