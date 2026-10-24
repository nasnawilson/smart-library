package com.smartlibrary.dto;

import com.smartlibrary.entity.User;
import lombok.Data;

/**
 * 登录响应DTO
 */
@Data
public class LoginResponseDTO {

    /** JWT令牌 */
    private String token;

    /** 用户信息 */
    private User userInfo;
}
