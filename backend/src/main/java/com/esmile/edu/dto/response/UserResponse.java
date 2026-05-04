package com.esmile.edu.dto.response;

import com.esmile.edu.module.user.Role;
import com.esmile.edu.module.user.UserEntity;
import com.esmile.edu.module.user.UserStatus;

public record UserResponse(
    Long id,
    String email,
    String nickname,
    String avatar,
    Role role,
    UserStatus status
) {
    public static UserResponse from(UserEntity entity) {
        return new UserResponse(
            entity.getId(),
            entity.getEmail(),
            entity.getNickname(),
            entity.getAvatar(),
            entity.getRole(),
            entity.getStatus()
        );
    }
}
