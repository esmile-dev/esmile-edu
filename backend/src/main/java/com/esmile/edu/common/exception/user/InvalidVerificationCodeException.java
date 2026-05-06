package com.esmile.edu.common.exception.user;

import com.esmile.edu.common.exception.AuthenticationException;
import com.esmile.edu.common.exception.BusinessException;

/**
 * 无效验证码异常 (10001)
 */
public class InvalidVerificationCodeException extends AuthenticationException {

    public InvalidVerificationCodeException() {
        super(INVALID_VERIFICATION_CODE, "无效验证码");
    }
}
