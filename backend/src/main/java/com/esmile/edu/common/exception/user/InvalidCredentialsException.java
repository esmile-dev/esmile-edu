package com.esmile.edu.common.exception.user;

import com.esmile.edu.common.exception.BusinessException;
import com.esmile.edu.common.exception.BusinessRuleException;

/**
 * 无效凭据异常 (10108)
 */
public class InvalidCredentialsException extends BusinessRuleException {

    public InvalidCredentialsException() {
        super(INVALID_CREDENTIALS, "邮箱或密码错误");
    }
}
