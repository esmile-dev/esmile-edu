package com.esmile.edu.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCourseRequest(
    @NotBlank @Size(max = 200) String title,
    String description,
    String cover
) {}
