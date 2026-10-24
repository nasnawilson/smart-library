package com.smartlibrary.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 阅读记录实体类
 * 对应数据库表: reading_record
 */
@Data
@TableName("reading_record")
public class ReadingRecord {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 图书ID */
    private Long bookId;

    /** 阅读日期 */
    private LocalDate readDate;

    /** 阅读时长（分钟） */
    private Integer durationMinutes;

    /** 创建时间 */
    private LocalDateTime createTime;

    // ========== 非数据库字段，用于关联查询展示 ==========

    /** 图书名称 */
    private transient String bookTitle;

    /** 图书作者 */
    private transient String bookAuthor;
}
