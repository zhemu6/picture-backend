package com.lushihao.picturebackend.exception;

import lombok.Getter;

/**
 * 业务异常 继承RuntimeException
 * @author lushihao
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     */
    private final int code;
    // 传入一个状态码和错误信息
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
    // 传入一个错误码 报的错就是错误码的信息和状态
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }
    // 传入一个错误码和自定义的错误信息
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

}
