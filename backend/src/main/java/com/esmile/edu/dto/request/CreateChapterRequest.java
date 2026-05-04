package com.esmile.edu.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateChapterRequest(
    @NotBlank String title,
    @NotNull Long courseId,
    @NotNull Integer position
) {}
