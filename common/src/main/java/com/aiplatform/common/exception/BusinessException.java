package com.aiplatform.common.exception;

/**
 * 业务异常基类，携带错误码和详细信息
 */
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }

    public int getCode() { return code; }

    /** 资源未找到 (404) */
    public static BusinessException notFound(String resource, Object id) {
        return new BusinessException(404, resource + " 未找到: " + id);
    }

    /** 参数校验失败 (400) */
    public static BusinessException badRequest(String message) {
        return new BusinessException(400, message);
    }

    /** 无权限 (403) */
    public static BusinessException forbidden(String message) {
        return new BusinessException(403, message);
    }

    /** 冲突 (409) */
    public static BusinessException conflict(String message) {
        return new BusinessException(409, message);
    }
}
