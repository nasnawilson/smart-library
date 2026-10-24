package com.smartlibrary.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartlibrary.dto.BookListDTO;
import com.smartlibrary.entity.Book;
import com.smartlibrary.entity.BookList;
import com.smartlibrary.entity.BookListItem;
import com.smartlibrary.mapper.BookListMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookListService extends ServiceImpl<BookListMapper, BookList> {
    @Autowired private BookService bookService;

    public List<BookList> listAll() {
        return list().stream().peek(this::fillView).collect(Collectors.toList());
    }

    public BookList getDetail(Long id) {
        BookList bookList = getById(id);
        if (bookList != null) fillView(bookList);
        return bookList;
    }

    public List<BookListItem> getListItems(Long id) {
        BookList bookList = getById(id);
        if (bookList == null || bookList.getBookIds() == null) return new ArrayList<>();
        List<BookListItem> items = new ArrayList<>();
        List<String> ids = Arrays.asList(bookList.getBookIds().split(","));
        for (int i = 0; i < ids.size(); i++) {
            Long bookId = Long.valueOf(ids.get(i).trim());
            Book book = bookService.getById(bookId);
            BookListItem item = new BookListItem();
            item.setId((long) i + 1);
            item.setListId(id);
            item.setBookId(bookId);
            item.setSortOrder(i + 1);
            if (book != null) {
                item.setBookTitle(book.getTitle());
                item.setBookAuthor(book.getAuthor());
                item.setBookCover(book.getCoverImage());
            }
            items.add(item);
        }
        return items;
    }

    public void create(BookListDTO dto, Long userId) {
        BookList bookList = new BookList();
        bookList.setTitle(dto.getTitle());
        bookList.setDescription(dto.getDescription());
        if (dto.getBookIds() != null) bookList.setBookIds(dto.getBookIds().stream().map(String::valueOf).collect(Collectors.joining(",")));
        save(bookList);
    }

    public void update(BookListDTO dto) {
        BookList bookList = new BookList();
        bookList.setId(dto.getId());
        bookList.setTitle(dto.getTitle());
        bookList.setDescription(dto.getDescription());
        if (dto.getBookIds() != null) bookList.setBookIds(dto.getBookIds().stream().map(String::valueOf).collect(Collectors.joining(",")));
        updateById(bookList);
    }

    public void delete(Long id) {
        removeById(id);
    }

    public void like(Long id) {
    }

    private void fillView(BookList bookList) {
        bookList.setCreatorName("系统推荐");
        bookList.setBookCount(bookList.getBookIds() == null || bookList.getBookIds().isEmpty() ? 0 : bookList.getBookIds().split(",").length);
        bookList.setLikeCount(0);
        bookList.setCollectCount(0);
    }
}
