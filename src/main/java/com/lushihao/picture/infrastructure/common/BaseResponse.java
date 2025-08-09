package com.lushihao.picture.infrastructure.common;

import com.lushihao.picture.infrastructure.exception.ErrorCode;
import lombok.Data;

import java.io.Serializable;

/**
 * 基础响应类别
 * @author lushihao
 * @param <T>
 */
@Data
public class BaseResponse<T> implements Serializable {
    // 返回代码
    private int code;
    // 返回数据
    private T data;
    // 返回信息
    private String message;

    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }
    // message为空
    public BaseResponse(int code, T data) {
        this(code, data, "");
    }
    // 传入一个错误码 code为错误码 data为空 message为错误码信息
    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }
}

