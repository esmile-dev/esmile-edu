package com.esmile.edu.common.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String EXTERNAL_REDEEM_PATH = "/api/v1/redeem-codes/apply";

    @Value("${redeem.api-key}")
    private String validApiKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestPath = request.getRequestURI();

        if (requestPath.equals(EXTERNAL_REDEEM_PATH)) {
            String apiKey = request.getHeader(API_KEY_HEADER);

            if (apiKey == null || !apiKey.equals(validApiKey)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"code\":10307,\"message\":\"Invalid API key\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}