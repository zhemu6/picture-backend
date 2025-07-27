package com.lushihao.picturebackend.exception;

import com.lushihao.picturebackend.common.BaseResponse;
import com.lushihao.picturebackend.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


/**
 * 全局异常处理器
 * @author lushihao
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    // 全局捕获BusinessException异常 返回封装后的错误码和错误消息
    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException", e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }
    // 全局捕获RuntimeException异常 返回封装后的错误码和错误消息
    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误");
    }
}

