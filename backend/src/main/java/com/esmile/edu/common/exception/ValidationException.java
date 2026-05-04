package com.esmile.edu.common.exception;

/**
 * 验证异常 (40001-40008)
 *
 * <p>用于请求参数校验失败的异常。</p>
 */
public class ValidationException extends BusinessException {

    public ValidationException(String message) {
        super(message);
    }

    @Override
    public int getCode() {
        return VALIDATION_FAILED;
    }
}
