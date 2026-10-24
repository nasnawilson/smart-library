package com.smartlibrary.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 座位预约请求DTO
 * 支持按小时区间预约（startHour/endHour），同时兼容旧的 timeSlot 字段
 */
@Data
public class SeatReserveDTO {

    /** 座位ID */
    @NotNull(message = "座位ID不能为空")
    private Long seatId;

    /** 预约日期 */
    @NotNull(message = "预约日期不能为空")
    private LocalDate reservationDate;

    /** 起始小时（0-23），与 endHour 配合使用 */
    private Integer startHour;

    /** 结束小时（1-24），需大于 startHour */
    private Integer endHour;

    /** 兼容旧版：MORNING/AFTERNOON/EVENING */
    private String timeSlot;
}
