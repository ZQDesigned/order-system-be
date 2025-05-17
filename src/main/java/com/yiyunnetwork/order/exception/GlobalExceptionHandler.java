package com.yiyunnetwork.order.exception;

import com.yiyunnetwork.order.dto.ApiResult;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResult<Void> handleBusinessException(BusinessException e) {
        return ApiResult.failed(e.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ApiResult<Void> handleAuthenticationException(AuthenticationException e) {
        return ApiResult.failed(ApiResult.ResultCode.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ApiResult<Void> handleAccessDeniedException(AccessDeniedException e) {
        return ApiResult.failed(ApiResult.ResultCode.FORBIDDEN, "没有权限访问该资源");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResult<Void> handleValidException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        StringBuilder message = new StringBuilder();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            message.append(fieldError.getField()).append(": ").append(fieldError.getDefaultMessage()).append(", ");
        }
        return ApiResult.failed(ApiResult.ResultCode.VALIDATE_FAILED, message.toString());
    }

    @ExceptionHandler(BindException.class)
    public ApiResult<Void> handleBindException(BindException e) {
        BindingResult bindingResult = e.getBindingResult();
        StringBuilder message = new StringBuilder();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            message.append(fieldError.getField()).append(": ").append(fieldError.getDefaultMessage()).append(", ");
        }
        return ApiResult.failed(ApiResult.ResultCode.VALIDATE_FAILED, message.toString());
    }

    @ExceptionHandler(Exception.class)
    public ApiResult<Void> handleException(Exception e) {
        return ApiResult.failed(e.getMessage());
    }
} 