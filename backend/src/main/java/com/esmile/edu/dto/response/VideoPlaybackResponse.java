package com.esmile.edu.dto.response;

/**
 * Video playback URL response with signed URL for TCPlayer.
 *
 * @param playbackUrl Signed playback URL with Key anti-hotlinking
 * @param duration Video duration in seconds
 * @param coverImage Cover image URL (optional)
 */
public record VideoPlaybackResponse(
    String playbackUrl,
    Integer duration,
    String coverImage
) {}
