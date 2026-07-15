package com.aiplatform.common.dto;

import java.time.LocalDateTime;

/**
 * 通用 API 响应体
 */
public class ApiResponse<T> {

    private int code;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    public ApiResponse() {}

    public ApiResponse(int code, String message, T data, LocalDateTime timestamp) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = timestamp;
    }

    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "success", data, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null, LocalDateTime.now());
    }
}
