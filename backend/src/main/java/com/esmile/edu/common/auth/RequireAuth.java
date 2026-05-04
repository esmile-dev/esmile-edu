package com.esmile.edu.common.auth;

import java.lang.annotation.*;

/**
 * 标记需要登录才能访问的端点
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireAuth {
}
