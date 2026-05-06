package com.esmile.edu.dto.response;

import java.util.List;

public record GenerateCodesResponse(
    List<String> codes,
    int quantity
) {}
