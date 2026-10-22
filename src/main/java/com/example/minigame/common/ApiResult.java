package com.example.minigame.common;

/**
 * 统一响应体。前端所有 fetch 调用都按这个结构解析：
 * <pre>{ "code": 0, "message": "ok", "data": {...} }</pre>
 * code = 0 表示成功，非 0 表示业务/系统错误。
 */
public record ApiResult<T>(int code, String message, T data) {

    public static final int CODE_OK = 0;
    public static final int CODE_ERROR = 500;
    public static final int CODE_BAD_REQUEST = 400;

    public static <T> ApiResult<T> ok(T data) {
        return new ApiResult<>(CODE_OK, "ok", data);
    }

    public static <T> ApiResult<T> ok(String message, T data) {
        return new ApiResult<>(CODE_OK, message, data);
    }

    public static <T> ApiResult<T> fail(String message) {
        return new ApiResult<>(CODE_ERROR, message, null);
    }

    public static <T> ApiResult<T> fail(int code, String message) {
        return new ApiResult<>(code, message, null);
    }
}
