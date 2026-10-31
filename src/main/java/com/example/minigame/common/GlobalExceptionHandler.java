package com.example.minigame.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理：保证接口返回统一 JSON，不把堆栈抛给前端。
 *
 * <p>注意：这里同时设置 **HTTP 状态码** 和业务 code。
 * 早期版本只返回 ApiResult 而不带状态码，导致「访问不存在的地址」
 * 也返回 HTTP 200，浏览器和监控都看不出这是错误 —— 已修正。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 参数校验失败：拼出「字段: 原因」。 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResult<Void>> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .orElse("参数校验失败");
        log.warn("参数校验失败: {}", msg);
        return ResponseEntity.badRequest().body(ApiResult.fail(ApiResult.CODE_BAD_REQUEST, msg));
    }

    /** 业务主动抛出的非法参数。 */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResult<Void>> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("非法请求: {}", e.getMessage());
        return ResponseEntity.badRequest().body(ApiResult.fail(ApiResult.CODE_BAD_REQUEST, e.getMessage()));
    }

    /**
     * 访问了不存在的路径 / 静态资源。
     *
     * <p>Spring 6.1 起未匹配到静态资源会抛 NoResourceFoundException，
     * 必须单独处理成 404，否则会被下面的兜底吞成 500。
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResult<Void>> handleNotFound(NoResourceFoundException e) {
        log.debug("资源不存在: {}", e.getResourcePath());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResult.fail(ApiResult.CODE_NOT_FOUND, "资源不存在"));
    }

    /** 兜底：记录完整堆栈到日志，对外只给一句话。 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<Void>> handleException(Exception e) {
        log.error("未处理异常", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResult.fail("服务器开小差了，请稍后再试"));
    }
}
