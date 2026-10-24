package com.smartlibrary.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartlibrary.dto.BookQueryDTO;
import com.smartlibrary.dto.PageResult;
import com.smartlibrary.entity.Book;
import com.smartlibrary.mapper.BookMapper;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookService extends ServiceImpl<BookMapper, Book> {
    public PageResult<Book> pageQuery(BookQueryDTO dto) {
        QueryWrapper<Book> wrapper = buildWrapper(dto.getTitle(), dto.getAuthor(), dto.getCategory(), dto.getFloor());
        Page<Book> page = page(new Page<>(dto.getPage(), dto.getSize()), wrapper);
        page.getRecords().forEach(this::fillView);
        return PageResult.of(page);
    }

    public List<Book> getHotBooks(Integer limit) {
        return list(new QueryWrapper<Book>().orderByDesc("borrow_num").last("LIMIT " + limit)).stream().peek(this::fillView).collect(Collectors.toList());
    }

    public List<Book> getNewBooks(Integer limit) {
        return list(new QueryWrapper<Book>().orderByDesc("id").last("LIMIT " + limit)).stream().peek(this::fillView).collect(Collectors.toList());
    }

    public List<String> getAllCategories() {
        return list().stream().map(Book::getCategory).filter(c -> c != null && !c.isEmpty()).distinct().collect(Collectors.toList());
    }

    public Book getById(Long id) {
        Book book = super.getById(id);
        if (book != null) fillView(book);
        return book;
    }

    private QueryWrapper<Book> buildWrapper(String title, String author, String category, Integer floor) {
        QueryWrapper<Book> wrapper = new QueryWrapper<>();
        if (title != null && !title.isEmpty()) wrapper.like("book_name", title);
        if (author != null && !author.isEmpty()) wrapper.like("author", author);
        if (category != null && !category.isEmpty()) wrapper.eq("category", category);
        if (floor != null) wrapper.likeRight("location", floor + "楼");
        return wrapper;
    }

    public void fillView(Book book) {
        int total = book.getTotalCount() == null ? 0 : book.getTotalCount();
        int borrowed = book.getBorrowNum() == null ? 0 : book.getBorrowNum();
        book.setAvailableCount(Math.max(0, total - borrowed));
        book.setFloor(book.getLocation() != null && book.getLocation().startsWith("2楼") ? 2 : 1);
        book.setIsHot(borrowed >= 2 ? 1 : 0);
        book.setIsNew(book.getId() != null && book.getId() >= 46 ? 1 : 0);
        book.setSubject(book.getCategory());
        book.setTags(book.getCategory());
    }
}
