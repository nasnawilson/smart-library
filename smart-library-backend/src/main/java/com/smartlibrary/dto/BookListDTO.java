package com.smartlibrary.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 书单请求DTO
 */
@Data
public class BookListDTO {

    /** 书单ID（更新时使用） */
    private Long id;

    /** 书单标题 */
    @NotBlank(message = "书单标题不能为空")
    private String title;

    /** 书单描述 */
    private String description;

    /** 书单分类 */
    private String category;

    /** 标签 */
    private String tags;

    /** 封面图片URL */
    private String coverImage;

    /** 书单包含的图书ID列表 */
    private List<Long> bookIds;
}
