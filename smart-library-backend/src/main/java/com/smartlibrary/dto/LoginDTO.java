package com.smartlibrary.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求DTO
 */
@Data
public class LoginDTO {

    /** 学号/工号 */
    @NotBlank(message = "学号不能为空")
    private String studentNo;

    /** 密码 */
    @NotBlank(message = "密码不能为空")
    private String password;
}
