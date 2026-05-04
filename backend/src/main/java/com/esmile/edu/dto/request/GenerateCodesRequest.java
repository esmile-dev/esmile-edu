package com.esmile.edu.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record GenerateCodesRequest(
    @NotNull Long courseId,
    @NotNull LocalDateTime expiresAt,
    LocalDateTime courseExpiresAt,
    @NotNull Integer quantity
) {}
