package com.smartlibrary.dto;

import lombok.Data;

/**
 * 图书查询条件DTO
 */
@Data
public class BookQueryDTO {

    /** 页码 */
    private Integer page = 1;

    /** 每页大小 */
    private Integer size = 10;

    /** 图书名称（模糊查询） */
    private String title;

    /** 作者（模糊查询） */
    private String author;

    /** 分类（精确查询） */
    private String category;

    /** 学科（精确查询） */
    private String subject;

    /** 是否热门 */
    private Integer isHot;

    /** 是否新书 */
    private Integer isNew;

    /** 楼层 */
    private Integer floor;

    /** 标签（模糊查询） */
    private String tag;
}
