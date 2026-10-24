package com.smartlibrary.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("borrow_record")
public class BorrowRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long bookId;
    @TableField("borrow_time")
    private LocalDateTime borrowTime;
    @TableField("due_time")
    private LocalDateTime dueTime;
    private Integer renewFlag;
    private Integer returnFlag;

    @TableField(exist = false)
    private LocalDate borrowDate;
    @TableField(exist = false)
    private LocalDate dueDate;
    @TableField(exist = false)
    private LocalDate returnDate;
    @TableField(exist = false)
    private String status;
    @TableField(exist = false)
    private Integer renewCount;
    @TableField(exist = false)
    private String bookTitle;
    @TableField(exist = false)
    private String userName;
    @TableField(exist = false)
    private String studentNo;
    @TableField(exist = false)
    private LocalDateTime createTime;
}
