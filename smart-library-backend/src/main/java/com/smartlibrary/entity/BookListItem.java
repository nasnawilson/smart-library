package com.smartlibrary.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

@Data
public class BookListItem {
    private Long id;
    private Long listId;
    private Long bookId;
    private Integer sortOrder;
    @TableField(exist = false)
    private String bookTitle;
    @TableField(exist = false)
    private String bookAuthor;
    @TableField(exist = false)
    private String bookCover;
}
