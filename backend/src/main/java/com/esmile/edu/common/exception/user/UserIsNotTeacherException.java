package com.esmile.edu.common.exception.user;

import com.esmile.edu.common.exception.BusinessException;
import com.esmile.edu.common.exception.BusinessRuleException;

/**
 * 用户不是教师异常 (10105)
 */
public class UserIsNotTeacherException extends BusinessRuleException {

    public UserIsNotTeacherException() {
        super(USER_IS_NOT_TEACHER, "用户不是教师");
    }
}
