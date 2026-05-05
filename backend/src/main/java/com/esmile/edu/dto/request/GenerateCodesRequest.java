package com.esmile.edu.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record GenerateCodesRequest(
    @NotNull Long courseId,
    @NotNull @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime expiresAt,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime courseExpiresAt,
    @NotNull Integer quantity
) {}
