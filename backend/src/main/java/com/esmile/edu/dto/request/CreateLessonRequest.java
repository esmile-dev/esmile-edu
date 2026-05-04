package com.esmile.edu.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateLessonRequest(
    @NotNull Long chapterId,
    @NotBlank String title,
    @NotNull @Positive Integer position
) {}
