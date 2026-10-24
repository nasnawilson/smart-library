package com.smartlibrary.exception;

import lombok.Getter;

/**
 * 业务异常类
 * 用于封装业务逻辑错误信息
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 错误码 */
    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
