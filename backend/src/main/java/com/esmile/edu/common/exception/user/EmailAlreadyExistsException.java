package com.esmile.edu.common.exception.user;

import com.esmile.edu.common.exception.BusinessException;
import com.esmile.edu.common.exception.BusinessRuleException;

/**
 * 邮箱已注册异常 (10104)
 */
public class EmailAlreadyExistsException extends BusinessRuleException {

    public EmailAlreadyExistsException() {
        super(EMAIL_ALREADY_EXISTS, "邮箱已注册");
    }
}
