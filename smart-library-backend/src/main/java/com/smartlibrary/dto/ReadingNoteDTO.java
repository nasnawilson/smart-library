package com.smartlibrary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 读书笔记请求DTO
 */
@Data
public class ReadingNoteDTO {

    /** 笔记ID（更新时使用） */
    private Long id;

    /** 图书ID */
    @NotNull(message = "图书ID不能为空")
    private Long bookId;

    /** 笔记标题 */
    @NotBlank(message = "笔记标题不能为空")
    private String title;

    /** 笔记内容 */
    @NotBlank(message = "笔记内容不能为空")
    private String content;
}
