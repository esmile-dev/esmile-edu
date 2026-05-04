package com.esmile.edu.common.exception.user;

import com.esmile.edu.common.exception.BusinessException;
import com.esmile.edu.common.exception.ResourceNotFoundException;

/**
 * 用户不存在异常 (10101)
 */
public class UserNotFoundException extends ResourceNotFoundException {

    public UserNotFoundException(Long userId) {
        super("User", userId);
    }

    @Override
    public int getCode() {
        return USER_NOT_FOUND;
    }
}
