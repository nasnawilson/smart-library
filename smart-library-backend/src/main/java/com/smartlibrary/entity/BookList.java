package com.smartlibrary.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("book_list")
public class BookList {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("list_name")
    private String title;
    @TableField("`desc`")
    private String description;
    private String bookIds;

    @TableField(exist = false)
    private Long creatorId;
    @TableField(exist = false)
    private String category;
    @TableField(exist = false)
    private String tags;
    @TableField(exist = false)
    private String coverImage;
    @TableField(exist = false)
    private Integer likeCount = 0;
    @TableField(exist = false)
    private Integer collectCount = 0;
    @TableField(exist = false)
    private String creatorName = "系统推荐";
    @TableField(exist = false)
    private Integer bookCount;
    @TableField(exist = false)
    private LocalDateTime createTime;
}
