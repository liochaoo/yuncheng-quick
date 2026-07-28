package com.yuncheng.framework.web.response;

/**
 * 平台统一 HTTP 响应。
 *
 * @param code 处理结果，0 表示成功，1 表示失败
 * @param data 响应数据
 * @param message 面向用户的响应信息
 */
public record ApiResponse<T>(int code, T data, String message) {

    private static final int SUCCESS = 0;
    private static final int FAILURE = 1;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(SUCCESS, data, "ok");
    }

    public static ApiResponse<Void> failure(String message) {
        return new ApiResponse<>(FAILURE, null, message);
    }
}
