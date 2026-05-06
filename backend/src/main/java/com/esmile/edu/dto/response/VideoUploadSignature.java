package com.esmile.edu.dto.response;

public record VideoUploadSignature(
    String videoId,
    String signature,
    String uploadUrl
) {
    public static VideoUploadSignature from(VideoUploadResult result) {
        return new VideoUploadSignature(result.videoId(), result.signature(), result.uploadUrl());
    }
}
