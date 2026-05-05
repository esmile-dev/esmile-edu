package com.esmile.edu.biz;

import com.esmile.edu.common.video.VideoServiceFactory;
import com.esmile.edu.common.video.VideoStoragePort;
import com.esmile.edu.dto.response.VideoUploadResult;
import com.esmile.edu.module.course.LessonEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Video upload service.
 * Delegates to configured VideoStoragePort implementation.
 */
@Service
public class VideoService {

    private static final Logger log = LoggerFactory.getLogger(VideoService.class);

    private final VideoServiceFactory videoServiceFactory;

    public VideoService(VideoServiceFactory videoServiceFactory) {
        this.videoServiceFactory = videoServiceFactory;
    }

    /**
     * Apply for video upload signature.
     *
     * @param educatorId the educator's user ID
     * @param fileName the video file name
     * @param fileSize the video file size in bytes
     * @return upload result with videoId, signature, and upload URL
     */
    public VideoUploadResult applyUpload(Long educatorId, String fileName, long fileSize) {
        VideoStoragePort provider = videoServiceFactory.getVideoStorage();
        log.debug("Using video provider: {}", provider.getProviderName());
        return provider.applyUpload(educatorId, fileName, fileSize);
    }

    /**
     * Confirm video upload completion and update lesson.
     *
     * @param lessonId the lesson ID to update
     * @param educatorId the educator's user ID (for ownership validation)
     * @param videoId the VOD video ID
     * @return the updated lesson
     */
    @Transactional
    public LessonEntity confirmUpload(Long lessonId, Long educatorId, String videoId) {
        VideoStoragePort provider = videoServiceFactory.getVideoStorage();
        return provider.confirmUpload(lessonId, educatorId, videoId);
    }

    /**
     * Get playback URL for a video (on-demand generation).
     *
     * @param videoId the VOD video ID
     * @return playback URL
     */
    public String getPlaybackUrl(String videoId) {
        VideoStoragePort provider = videoServiceFactory.getVideoStorage();
        return provider.getPlaybackUrl(videoId);
    }
}
