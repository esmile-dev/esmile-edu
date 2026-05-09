package com.esmile.edu.common.auth;

import com.esmile.edu.common.exception.AuthenticationException;
import com.esmile.edu.common.exception.AuthorizationException;
import com.esmile.edu.module.user.Role;
import com.esmile.edu.module.user.UserEntity;
import com.esmile.edu.module.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 认证授权拦截器
 * 从 JWT Bearer Token 解析用户信息并验证角色
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public AuthInterceptor(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RequireAuth requireAuth = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), RequireAuth.class);
        RequireRole requireRole = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), RequireRole.class);

        // 获取 Authorization header
        String authHeader = request.getHeader(HEADER_AUTHORIZATION);

        // 如果没有 RequireAuth 注解，检查是否有 token，有的话尝试解析，没有直接放行（公开接口）
        if (requireAuth == null && requireRole == null) {
            if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
                try {
                    String token = authHeader.substring(BEARER_PREFIX.length());
                    if (jwtService.validateToken(token)) {
                        Long userId = jwtService.getUserIdFromToken(token);
                        userRepository.findById(userId).ifPresent(AuthContext::setCurrentUser);
                    }
                } catch (Exception e) {
                    // 忽略解析错误，对于公开接口不强制要求有效 token
                }
            }
            return true;
        }

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            throw new AuthenticationException(
                    AuthenticationException.INVALID_TOKEN,
                    "缺少 Authorization header，请先登录"
            );
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        // 验证 JWT token
        if (!jwtService.validateToken(token)) {
            throw new AuthenticationException(
                    AuthenticationException.INVALID_TOKEN,
                    "Token 无效或已过期"
            );
        }

        // 从 token 获取用户 ID
        Long userId = jwtService.getUserIdFromToken(token);

        // 验证用户存在
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException(
                        AuthenticationException.INVALID_TOKEN,
                        "用户不存在"
                ));

        // 检查用户状态
        if (user.getStatus() == com.esmile.edu.module.user.UserStatus.DISABLED) {
            throw new AuthorizationException(
                    AuthorizationException.ACCESS_DENIED,
                    "用户已被禁用"
            );
        }

        // 如果有 RequireAuth 注解，验证用户状态
        if (requireAuth != null) {
            // 检查用户是否被禁用或待审批
            if (user.getStatus() == com.esmile.edu.module.user.UserStatus.PENDING_APPROVAL) {
                throw new AuthorizationException(
                        AuthorizationException.ACCESS_DENIED,
                        "账号待审批"
                );
            }
        }

        // 如果有 RequireRole 注解，验证角色
        if (requireRole != null) {
            Role requiredRole = requireRole.value();
            if (user.getRole() != requiredRole) {
                throw new AuthorizationException(
                        AuthorizationException.INSUFFICIENT_PERMISSIONS,
                        "需要 " + requiredRole + " 角色权限"
                );
            }
        }

        // 设置当前用户上下文
        AuthContext.setCurrentUser(user);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        AuthContext.clear();
    }
}
