package com.smartlibrary.dto;

import lombok.Data;

/**
 * 座位查询条件DTO
 */
@Data
public class SeatQueryDTO {

    /** 楼层 */
    private Integer floor;

    /** 区域 */
    private String area;

    /** 状态 */
    private String status;
}
