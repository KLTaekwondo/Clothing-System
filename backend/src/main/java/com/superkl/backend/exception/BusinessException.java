package com.superkl.backend.exception;

import com.superkl.backend.enums.ErrorCodeEnum;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final Integer code;

    public BusinessException(Integer code, String msg) {
        super(msg);
        this.code = code;
    }

    public BusinessException(String msg) {
        super(msg);
        this.code = 400;
    }

    // 新http码异常处理构造方法
    public BusinessException(ErrorCodeEnum error , String msg) {
        super(msg);
        this.code = error.getCode();
    }
}
