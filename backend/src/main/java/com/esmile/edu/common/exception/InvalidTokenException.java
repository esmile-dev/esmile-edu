package com.esmile.edu.common.exception;

/**
 * 无效Token异常 (10004)
 */
public class InvalidTokenException extends AuthenticationException {

    public InvalidTokenException() {
        super(INVALID_TOKEN, "无效Token");
    }
}
