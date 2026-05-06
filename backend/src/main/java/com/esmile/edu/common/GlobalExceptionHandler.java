package com.esmile.edu.common;

import com.esmile.edu.common.exception.AuthenticationException;
import com.esmile.edu.common.exception.AuthorizationException;
import com.esmile.edu.common.exception.BusinessException;
import com.esmile.edu.common.exception.BusinessRuleException;
import com.esmile.edu.common.exception.ExternalServiceException;
import com.esmile.edu.common.exception.ResourceNotFoundException;
import com.esmile.edu.common.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理认证异常 - 返回 401
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<Void> handleAuthenticationException(AuthenticationException ex) {
        return ApiResponse.error(ex.getCode(), ex.getMessage());
    }

    /**
     * 处理授权异常 - 返回 403
     */
    @ExceptionHandler(AuthorizationException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponse<Void> handleAuthorizationException(AuthorizationException ex) {
        return ApiResponse.error(ex.getCode(), ex.getMessage());
    }

    /**
     * 处理资源不存在异常 - 返回 404
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleResourceNotFound(ResourceNotFoundException ex) {
        return ApiResponse.error(ex.getCode(), ex.getMessage());
    }

    /**
     * 处理业务规则异常 - 返回 400
     */
    @ExceptionHandler(BusinessRuleException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleBusinessRuleException(BusinessRuleException ex) {
        return ApiResponse.error(ex.getCode(), ex.getMessage());
    }

    /**
     * 处理验证异常 - 返回 400
     */
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidationException(ValidationException ex) {
        return ApiResponse.error(ex.getCode(), ex.getMessage());
    }

    /**
     * 处理外部服务异常 - 返回 502
     */
    @ExceptionHandler(ExternalServiceException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public ApiResponse<Void> handleExternalServiceException(ExternalServiceException ex) {
        log.error("External service error: {}", ex.getMessage(), ex);
        return ApiResponse.error(ex.getCode(), ex.getMessage());
    }

    /**
     * 处理业务异常基类（兜底）
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> handleBusinessException(BusinessException ex) {
        return ApiResponse.error(ex.getCode(), ex.getMessage());
    }

    /**
     * 处理请求参数校验异常 - 返回 400
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Map<String, String>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );
        return new ApiResponse<>(400, "Validation failed", errors);
    }

    /**
     * 处理未预料的异常 - 返回 500
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred", ex);
        return ApiResponse.error(BusinessException.INTERNAL_SERVER_ERROR, "Internal server error");
    }
}
