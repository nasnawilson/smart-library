package com.smartlibrary.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("seat_info")
public class Seat {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer floor;
    @TableField("seat_num")
    private String seatNo;
    @TableField("status")
    private Integer statusCode;

    @TableField(exist = false)
    private String status;
    @TableField(exist = false)
    private String area;
    @TableField(exist = false)
    private Long currentUserId;
    @TableField(exist = false)
    private String currentUserName;
    @TableField(exist = false)
    private LocalDateTime occupyTime;
    @TableField(exist = false)
    private LocalDateTime tempLeaveTime;
    @TableField(exist = false)
    private LocalDateTime tempLeaveExpireTime;
    @TableField(exist = false)
    private List<String> reservedTimeSlots;
}
