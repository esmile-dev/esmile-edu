package com.esmile.edu.common.exception;

/**
 * 外部服务异常 (50301-50308)
 *
 * <p>用于外部服务调用失败的异常，如腾讯云VOD服务错误、邮件服务错误等。</p>
 */
public class ExternalServiceException extends BusinessException {

    private final int code;

    public ExternalServiceException(int code, String message) {
        super(message);
        if (code < 50301 || code > 50308) {
            throw new IllegalArgumentException("ExternalServiceException code must be between 50301 and 50308");
        }
        this.code = code;
    }

    @Override
    public int getCode() {
        return code;
    }
}
