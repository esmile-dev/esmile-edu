package com.esmile.edu.common.exception;

/**
 * 权限不足异常 (40302)
 */
public class InsufficientPermissionsException extends AuthorizationException {

    public InsufficientPermissionsException() {
        super(INSUFFICIENT_PERMISSIONS, "权限不足");
    }

    public InsufficientPermissionsException(String message) {
        super(INSUFFICIENT_PERMISSIONS, message);
    }
}
