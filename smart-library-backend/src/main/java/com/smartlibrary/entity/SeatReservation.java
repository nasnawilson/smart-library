package com.smartlibrary.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("reserve_record")
public class SeatReservation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long seatId;
    @TableField("reserve_date")
    private LocalDate reservationDate;
    @TableField("time_period")
    private String timeSlot;
    @TableField("sign_time")
    private LocalDateTime checkInTime;
    @TableField("leave_time")
    private LocalDateTime leaveTime;
    @TableField("status")
    private Integer statusCode;

    @TableField(exist = false)
    private String status;
    @TableField(exist = false)
    private LocalDateTime checkInDeadline;
    @TableField(exist = false)
    private String seatNo;
    @TableField(exist = false)
    private Integer floor;
    @TableField(exist = false)
    private String area;
    @TableField(exist = false)
    private String userName;
    @TableField(exist = false)
    private String studentNo;
    @TableField("create_time")
    private LocalDateTime createTime;
}
