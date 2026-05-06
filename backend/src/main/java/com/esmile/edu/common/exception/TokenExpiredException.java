package com.esmile.edu.common.exception;

/**
 * Token已过期异常 (10005)
 */
public class TokenExpiredException extends AuthenticationException {

    public TokenExpiredException() {
        super(TOKEN_EXPIRED, "Token已过期");
    }
}
