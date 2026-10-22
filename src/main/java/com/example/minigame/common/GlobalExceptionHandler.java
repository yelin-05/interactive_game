package com.example.minigame.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理：保证接口永远返回统一 JSON，不把堆栈直接抛给前端。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 参数校验失败：拼出「字段: 原因」。 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResult<Void> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .orElse("参数校验失败");
        log.warn("参数校验失败: {}", msg);
        return ApiResult.fail(ApiResult.CODE_BAD_REQUEST, msg);
    }

    /** 业务主动抛出的非法参数。 */
    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResult<Void> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("非法请求: {}", e.getMessage());
        return ApiResult.fail(ApiResult.CODE_BAD_REQUEST, e.getMessage());
    }

    /** 兜底：记录完整堆栈到日志，对外只给一句话。 */
    @ExceptionHandler(Exception.class)
    public ApiResult<Void> handleException(Exception e) {
        log.error("未处理异常", e);
        return ApiResult.fail("服务器开小差了，请稍后再试");
    }
}
