package com.yiyunnetwork.order.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ApiResult<T> implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Integer code;
    private String message;
    private T data;
    
    private ApiResult() {
    }
    
    private ApiResult(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
    
    private ApiResult(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
    
    public static <T> ApiResult<T> success() {
        return new ApiResult<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage());
    }
    
    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }
    
    public static <T> ApiResult<T> success(String message, T data) {
        return new ApiResult<>(ResultCode.SUCCESS.getCode(), message, data);
    }
    
    public static <T> ApiResult<T> failed() {
        return new ApiResult<>(ResultCode.FAILED.getCode(), ResultCode.FAILED.getMessage());
    }
    
    public static <T> ApiResult<T> failed(String message) {
        return new ApiResult<>(ResultCode.FAILED.getCode(), message);
    }
    
    public static <T> ApiResult<T> failed(ResultCode resultCode) {
        return new ApiResult<>(resultCode.getCode(), resultCode.getMessage());
    }
    
    public static <T> ApiResult<T> failed(ResultCode resultCode, String message) {
        return new ApiResult<>(resultCode.getCode(), message);
    }
    
    public static <T> ApiResult<T> failed(Integer code, String message) {
        return new ApiResult<>(code, message);
    }
    
    public enum ResultCode {
        SUCCESS(200, "操作成功"),
        FAILED(500, "操作失败"),
        VALIDATE_FAILED(400, "参数校验失败"),
        UNAUTHORIZED(401, "暂未登录或身份已过期"),
        FORBIDDEN(403, "没有相关权限"),
        NOT_FOUND(404, "资源不存在");
        
        private final Integer code;
        private final String message;
        
        ResultCode(Integer code, String message) {
            this.code = code;
            this.message = message;
        }
        
        public Integer getCode() {
            return code;
        }
        
        public String getMessage() {
            return message;
        }
    }
} 