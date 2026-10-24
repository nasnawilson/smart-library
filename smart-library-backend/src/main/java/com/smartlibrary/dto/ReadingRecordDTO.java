package com.smartlibrary.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 阅读记录请求DTO
 */
@Data
public class ReadingRecordDTO {

    /** 图书ID */
    @NotNull(message = "图书ID不能为空")
    private Long bookId;

    /** 阅读日期 */
    @NotNull(message = "阅读日期不能为空")
    private LocalDate readDate;

    /** 阅读时长（分钟） */
    @NotNull(message = "阅读时长不能为空")
    private Integer durationMinutes;
}
