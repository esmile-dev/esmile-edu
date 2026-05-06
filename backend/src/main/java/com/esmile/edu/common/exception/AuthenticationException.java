package com.esmile.edu.common.exception;

/**
 * 认证异常 (10001-10008)
 *
 * <p>用于认证相关的业务异常，如验证码无效、Token过期等。</p>
 */
public class AuthenticationException extends BusinessException {

    private final int code;

    public AuthenticationException(int code, String message) {
        super(message);
        if (code < 10001 || code > 10008) {
            throw new IllegalArgumentException("AuthenticationException code must be between 10001 and 10008");
        }
        this.code = code;
    }

    @Override
    public int getCode() {
        return code;
    }
}
