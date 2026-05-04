package com.esmile.edu.common.exception.user;

import com.esmile.edu.common.exception.BusinessException;
import com.esmile.edu.common.exception.BusinessRuleException;

/**
 * 用户已禁用异常 (10102)
 */
public class UserDisabledException extends BusinessRuleException {

    public UserDisabledException() {
        super(USER_DISABLED, "用户已禁用");
    }
}
