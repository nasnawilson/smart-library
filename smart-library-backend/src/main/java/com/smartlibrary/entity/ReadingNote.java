package com.smartlibrary.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 读书笔记实体类
 * 对应数据库表: reading_note
 */
@Data
@TableName("reading_note")
public class ReadingNote {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 图书ID */
    private Long bookId;

    /** 笔记标题 */
    private String title;

    /** 笔记内容 */
    private String content;

    /** 逻辑删除：0-未删除，1-已删除 */
    @TableLogic
    private Integer deleted;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    // ========== 非数据库字段，用于关联查询展示 ==========

    /** 图书名称 */
    private transient String bookTitle;

    /** 图书作者 */
    private transient String bookAuthor;

    /** 作者姓名 */
    private transient String userName;
}
