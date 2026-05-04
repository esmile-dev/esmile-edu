package com.esmile.edu.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CommitUploadRequest(
    @NotNull Long lessonId,
    @NotBlank String videoId
) {}
