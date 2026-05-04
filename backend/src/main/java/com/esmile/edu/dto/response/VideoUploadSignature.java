package com.esmile.edu.dto.response;

public record VideoUploadSignature(
    String videoId,
    String signature,
    String uploadUrl
) {}
