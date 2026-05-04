package com.esmile.edu.common.exception;

/**
 * 授权异常 (40301-40308)
 *
 * <p>用于授权相关的业务异常，如权限不足、访问被拒绝等。</p>
 */
public class AuthorizationException extends BusinessException {

    private final int code;

    public AuthorizationException(int code, String message) {
        super(message);
        if (code < 40301 || code > 40308) {
            throw new IllegalArgumentException("AuthorizationException code must be between 40301 and 40308");
        }
        this.code = code;
    }

    @Override
    public int getCode() {
        return code;
    }
}
