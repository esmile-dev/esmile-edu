package com.esmile.edu.common.auth;

import com.esmile.edu.module.user.Role;
import java.lang.annotation.*;

/**
 * 标记需要特定角色才能访问的端点
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {
    Role value();
}
