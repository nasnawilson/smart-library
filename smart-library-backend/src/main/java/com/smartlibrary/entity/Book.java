package com.smartlibrary.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("book_info")
public class Book {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("book_name")
    private String title;
    private String author;
    private String isbn;
    private String publisher;
    private String category;
    @TableField("cover")
    private String coverImage;
    @TableField("intro")
    private String description;
    private String location;
    @TableField("total_num")
    private Integer totalCount;
    @TableField("borrow_num")
    private Integer borrowNum;
    @TableField("in_library")
    private Integer inLibrary;

    @TableField(exist = false)
    private Integer availableCount;
    @TableField(exist = false)
    private Integer floor;
    @TableField(exist = false)
    private String subject;
    @TableField(exist = false)
    private LocalDate publishDate;
    @TableField(exist = false)
    private String tags;
    @TableField(exist = false)
    private Integer isHot;
    @TableField(exist = false)
    private Integer isNew;
    @TableField(exist = false)
    private Integer deleted = 0;
    @TableField(exist = false)
    private LocalDateTime createTime;
}
