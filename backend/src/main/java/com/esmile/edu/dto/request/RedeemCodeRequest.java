package com.esmile.edu.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RedeemCodeRequest(
    @NotBlank String code
) {}
