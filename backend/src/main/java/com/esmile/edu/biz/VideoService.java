package com.esmile.edu.biz;

import com.esmile.edu.common.exception.BusinessException;
import com.esmile.edu.common.exception.course.LessonNotFoundException;
import com.esmile.edu.common.exception.video.VideoUploadFailedException;
import com.esmile.edu.dto.response.VideoUploadSignature;
import com.esmile.edu.module.course.LessonEntity;
import com.esmile.edu.module.course.LessonRepository;
import com.esmile.edu.module.course.LessonStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Video upload service for Tencent Cloud VOD integration.
 * MVP stage uses mock implementation with simulated responses.
 */
@Service
public class VideoService {
    private final LessonRepository lessonRepository;

    public VideoService(LessonRepository lessonRepository) {
        this.lessonRepository = lessonRepository;
    }

    /**
     * Apply for video upload signature.
     * MVP: Returns mock signature data.
     *
     * @param educatorId the educator's user ID
     * @param fileName the video file name
     * @param fileSize the video file size in bytes
     * @return upload signature with videoId
     */
    public VideoUploadSignature applyUpload(Long educatorId, String fileName, long fileSize) {
        // MVP: Validate file type and size limits
        validateFile(fileName, fileSize);

        // MVP: Generate mock videoId (in production, this comes from VOD API)
        String videoId = generateVideoId();

        // MVP: Generate mock signature and upload URL
        // In production, this would call Tencent VOD API to get real credentials
        String signature = generateMockSignature(videoId);
        String uploadUrl = "https://upload.vod.tscloud.com";

        return new VideoUploadSignature(videoId, signature, uploadUrl);
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
        LessonEntity lesson = lessonRepository.findById(lessonId)
            .orElseThrow(() -> new LessonNotFoundException(lessonId));

        // MVP: In production, validate videoId ownership and get actual video info
        lesson.setVideoId(videoId);
        lesson.setStatus(LessonStatus.PROCESSING);

        return lessonRepository.save(lesson);
    }

    private void validateFile(String fileName, long fileSize) {
        // Validate file extension
        if (fileName != null && !fileName.isBlank()) {
            String lowerName = fileName.toLowerCase();
            if (!lowerName.endsWith(".mp4") && !lowerName.endsWith(".mov")
                    && !lowerName.endsWith(".avi") && !lowerName.endsWith(".mkv")) {
                throw new VideoUploadFailedException("不支持的视频格式，仅支持 mp4、mov、avi、mkv");
            }
        }

        // Validate file size (10GB limit)
        if (fileSize <= 0 || fileSize > 10 * 1024 * 1024 * 1024L) {
            throw new VideoUploadFailedException("视频大小超出限制，最大支持 10GB");
        }
    }

    private String generateVideoId() {
        // MVP: Generate a mock videoId
        // In production, this would be returned by VOD API after applying for upload
        return "mock_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    private String generateMockSignature(String videoId) {
        // MVP: Generate a mock signature
        // In production, this would be computed using Tencent VOD SecretKey
        return "mock_sig_" + videoId;
    }
}
