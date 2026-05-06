package com.esmile.edu.common.auth;

import com.esmile.edu.module.user.UserEntity;

/**
 * 当前登录用户上下文
 * 使用 ThreadLocal 存储当前请求的用户信息
 */
public class AuthContext {

    private static final ThreadLocal<UserEntity> CURRENT_USER = new ThreadLocal<>();

    public static void setCurrentUser(UserEntity user) {
        CURRENT_USER.set(user);
    }

    public static UserEntity getCurrentUser() {
        return CURRENT_USER.get();
    }

    public static Long getCurrentUserId() {
        UserEntity user = CURRENT_USER.get();
        return user != null ? user.getId() : null;
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
