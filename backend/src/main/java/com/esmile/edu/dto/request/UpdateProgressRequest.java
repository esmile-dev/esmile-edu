package com.esmile.edu.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record UpdateProgressRequest(
    @NotNull Long lessonId,
    @NotNull @PositiveOrZero Integer watchedSeconds,
    Boolean isCompleted
) {}
