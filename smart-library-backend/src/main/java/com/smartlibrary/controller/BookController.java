package com.smartlibrary.controller;

import com.smartlibrary.dto.BookQueryDTO;
import com.smartlibrary.dto.PageResult;
import com.smartlibrary.dto.Result;
import com.smartlibrary.entity.Book;
import com.smartlibrary.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 图书控制器
 * 处理图书管理相关接口
 */
@RestController
@RequestMapping("/books")
public class BookController {

    @Autowired
    private BookService bookService;

    /**
     * 分页查询图书列表
     */
    @GetMapping
    public Result<PageResult<Book>> list(BookQueryDTO queryDTO) {
        return Result.success(bookService.pageQuery(queryDTO));
    }

    /**
     * 根据ID查询图书详情
     */
    @GetMapping("/{id}")
    public Result<Book> getById(@PathVariable Long id) {
        return Result.success(bookService.getById(id));
    }

    /**
     * 新增图书
     */
    @PostMapping
    public Result<Void> save(@RequestBody Book book) {
        // 初始化可借阅数量
        if (book.getAvailableCount() == null) {
            book.setAvailableCount(book.getTotalCount());
        }
        bookService.save(book);
        return Result.success();
    }

    /**
     * 更新图书
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Book book) {
        book.setId(id);
        bookService.updateById(book);
        return Result.success();
    }

    /**
     * 删除图书
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        bookService.removeById(id);
        return Result.success();
    }

    /**
     * 获取热门图书
     */
    @GetMapping("/hot")
    public Result<List<Book>> getHotBooks(@RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(bookService.getHotBooks(limit));
    }

    /**
     * 获取新书
     */
    @GetMapping("/new")
    public Result<List<Book>> getNewBooks(@RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(bookService.getNewBooks(limit));
    }

    /**
     * 获取所有分类
     */
    @GetMapping("/categories")
    public Result<List<String>> getCategories() {
        return Result.success(bookService.getAllCategories());
    }
}
