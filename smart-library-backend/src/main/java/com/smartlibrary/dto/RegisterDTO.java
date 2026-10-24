package com.smartlibrary.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 注册请求DTO
 */
@Data
public class RegisterDTO {

    /** 学号/工号 */
    @NotBlank(message = "学号不能为空")
    private String studentNo;

    /** 密码 */
    @NotBlank(message = "密码不能为空")
    private String password;

    /** 姓名 */
    @NotBlank(message = "姓名不能为空")
    private String name;

    /** 院系 */
    private String department;

    /** 班级 */
    private String className;

    /** 专业 */
    private String major;

    /** 年级 */
    private String grade;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;
}
