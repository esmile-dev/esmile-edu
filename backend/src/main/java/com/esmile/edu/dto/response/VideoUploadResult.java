package com.esmile.edu.dto.response;

/**
 * Video upload result containing credentials for direct upload to cloud storage.
 *
 * @param videoId VOD video ID (used for playback and status queries)
 * @param signature upload signature for authentication
 * @param uploadUrl cloud storage upload endpoint
 * @param uploadContext server-side context for chunked upload tracking
 */
public record VideoUploadResult(
    String videoId,
    String signature,
    String uploadUrl,
    String uploadContext
) {
    public VideoUploadResult(String videoId, String signature, String uploadUrl) {
        this(videoId, signature, uploadUrl, null);
    }
}
