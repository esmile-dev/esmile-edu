package com.esmile.edu.dto.response;

import com.esmile.edu.module.redeem.RedeemCodeEntity;
import com.esmile.edu.module.redeem.RedeemCodeStatus;

import java.time.LocalDateTime;

public record RedeemCodeResponse(
    Long id,
    String code,
    Long courseId,
    RedeemCodeStatus status,
    LocalDateTime expiresAt,
    LocalDateTime courseExpiresAt,
    LocalDateTime usedAt,
    Long usedBy
) {
    public static RedeemCodeResponse from(RedeemCodeEntity entity) {
        return new RedeemCodeResponse(
            entity.getId(),
            entity.getCode(),
            entity.getCourseId(),
            entity.getStatus(),
            entity.getExpiresAt(),
            entity.getCourseExpiresAt(),
            entity.getUsedAt(),
            entity.getUsedBy()
        );
    }
}
