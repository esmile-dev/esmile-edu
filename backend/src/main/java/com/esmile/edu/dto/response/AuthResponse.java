package com.esmile.edu.dto.response;

public record AuthResponse(String token, long expiresIn, UserResponse user) {}
