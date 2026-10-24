package com.smartlibrary.dto;

import lombok.Data;

/**
 * 借阅记录查询条件DTO
 */
@Data
public class BorrowQueryDTO {

    /** 页码 */
    private Integer page = 1;

    /** 每页大小 */
    private Integer size = 10;

    /** 读者姓名（模糊查询） */
    private String userName;

    /** 图书名称（模糊查询） */
    private String bookTitle;

    /** 状态 */
    private String status;

    /** 用户ID（查询个人借阅记录） */
    private Long userId;
}
