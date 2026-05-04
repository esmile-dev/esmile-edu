package com.esmile.edu.common.config;

import com.esmile.edu.common.auth.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    public WebMvcConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/v1/**")
                .excludePathPatterns(
                        // 公开接口 - 无需认证
                        "/api/v1/student/auth/**",
                        "/api/v1/teacher/auth/**",
                        "/api/v1/courses",
                        "/api/v1/courses/{id}",
                        "/api/v1/redeem-codes/apply"
                );
    }
}
