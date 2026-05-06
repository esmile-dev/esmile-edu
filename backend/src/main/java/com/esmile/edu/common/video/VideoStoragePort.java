package com.esmile.edu.common.video;

import com.esmile.edu.dto.response.VideoPlaybackResponse;
import com.esmile.edu.dto.response.VideoUploadResult;
import com.esmile.edu.module.course.LessonEntity;

/**
 * Video storage provider interface.
 * Implementations: MockVideoProvider, TencentVodProvider, etc.
 *
 * <p>This port defines the contract for video upload and playback operations.
 * Each provider implementation handles the specific cloud VOD SDK integration.</p>
 */
public interface VideoStoragePort {

    /**
     * Apply for video upload credentials.
     * Returns upload signature and video ID for direct upload to cloud storage.
     *
     * @param educatorId the educator's user ID
     * @param fileName the video file name
     * @param fileSize the video file size in bytes
     * @return upload result with videoId, signature, and upload URL
     */
    VideoUploadResult applyUpload(Long educatorId, String fileName, long fileSize);

    /**
     * Confirm video upload completion and update lesson.
     *
     * @param lessonId the lesson ID to update
     * @param educatorId the educator's user ID (for ownership validation)
     * @param videoId the VOD video ID
     * @return the updated lesson entity
     */
    LessonEntity confirmUpload(Long lessonId, Long educatorId, String videoId);

    /**
     * Get playback URL for a video (on-demand generation).
     *
     * @param videoId the VOD video ID
     * @return playback URL
     */
    String getPlaybackUrl(String videoId);

    /**
     * Get playback URL with signed token for time-limited access.
     *
     * @param videoId the VOD video ID
     * @return video playback response with signed URL
     */
    VideoPlaybackResponse getPlaybackUrlWithSign(String videoId);

    /**
     * Get the provider name.
     *
     * @return provider name (e.g., "mock", "tencent")
     */
    String getProviderName();
}
