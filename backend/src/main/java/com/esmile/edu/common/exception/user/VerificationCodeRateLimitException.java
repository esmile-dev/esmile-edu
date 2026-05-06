package com.esmile.edu.common.exception.user;

import com.esmile.edu.common.exception.AuthenticationException;
import com.esmile.edu.common.exception.BusinessException;

/**
 * Rate limit exceeded for verification code requests (10003)
 */
public class VerificationCodeRateLimitException extends AuthenticationException {

    public VerificationCodeRateLimitException() {
        super(VERIFICATION_CODE_RATE_LIMITED, "请求过于频繁，请稍后再试");
    }
}
