package com.esmile.edu.common.video;

import com.esmile.edu.common.exception.BusinessRuleException;
import com.esmile.edu.common.exception.course.CourseNotFoundException;
import com.esmile.edu.common.exception.course.LessonNotFoundException;
import com.esmile.edu.common.exception.video.VideoUploadFailedException;
import com.esmile.edu.dto.response.VideoUploadResult;
import com.esmile.edu.module.course.CourseEntity;
import com.esmile.edu.module.course.CourseRepository;
import com.esmile.edu.module.course.LessonEntity;
import com.esmile.edu.module.course.LessonRepository;
import com.esmile.edu.module.course.LessonStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mock video provider for development environment.
 * Simulates VOD operations without actual cloud storage.
 */
@Component
@ConditionalOnProperty(name = "video.provider", havingValue = "mock", matchIfMissing = true)
public class MockVideoProvider implements VideoStoragePort {

    private static final Logger log = LoggerFactory.getLogger(MockVideoProvider.class);
    private static final long MAX_FILE_SIZE = 1 * 1024L * 1024 * 1024; // 1GB
    private static final String UPLOAD_URL = "https://upload.vod.tscloud.com";
    private static final String PLAYBACK_URL_TEMPLATE = "https://play.vod.tscloud.com/mock/%s";

    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;

    // Simulates upload context storage (keyed by videoId)
    private final Map<String, UploadContext> uploadContexts = new ConcurrentHashMap<>();

    public MockVideoProvider(LessonRepository lessonRepository, CourseRepository courseRepository) {
        this.lessonRepository = lessonRepository;
        this.courseRepository = courseRepository;
    }

    @Override
    public VideoUploadResult applyUpload(Long educatorId, String fileName, long fileSize) {
        validateFile(fileName, fileSize);

        String videoId = "mock_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String uploadContext = UUID.randomUUID().toString();
        String signature = "mock_sig_" + videoId;

        uploadContexts.put(videoId, new UploadContext(uploadContext, educatorId, fileName, fileSize));

        log.info("[MOCK VIDEO] Apply upload: videoId={}, fileName={}, fileSize={}", videoId, fileName, fileSize);
        return new VideoUploadResult(videoId, signature, UPLOAD_URL, uploadContext);
    }

    @Override
    public LessonEntity confirmUpload(Long lessonId, Long educatorId, String videoId) {
        UploadContext ctx = uploadContexts.get(videoId);
        if (ctx == null) {
            throw new VideoUploadFailedException("Upload context not found or expired. Please apply for upload first.");
        }
        if (!ctx.educatorId().equals(educatorId)) {
            throw new BusinessRuleException(BusinessRuleException.ACCESS_DENIED, "Not authorized to confirm this upload");
        }

        // Validate lesson exists
        LessonEntity lesson = lessonRepository.findById(lessonId)
            .orElseThrow(() -> new LessonNotFoundException(lessonId));

        // Validate course ownership
        CourseEntity course = courseRepository.findById(lesson.getCourseId())
            .orElseThrow(() -> new CourseNotFoundException(lesson.getCourseId()));
        if (!course.getEducatorId().equals(educatorId)) {
            throw new BusinessRuleException(40301, "Not authorized to modify this lesson's video");
        }

        // Update lesson with video info
        lesson.setVideoId(videoId);
        lesson.setStatus(LessonStatus.READY);
        lesson.setVideoUrl(String.format(PLAYBACK_URL_TEMPLATE, videoId));

        uploadContexts.remove(videoId);
        log.info("[MOCK VIDEO] Confirm upload: lessonId={}, videoId={}", lessonId, videoId);
        return lessonRepository.save(lesson);
    }

    @Override
    public String getPlaybackUrl(String videoId) {
        log.info("[MOCK VIDEO] Get playback URL: videoId={}", videoId);
        return String.format(PLAYBACK_URL_TEMPLATE, videoId);
    }

    @Override
    public String getProviderName() {
        return "mock";
    }

    private void validateFile(String fileName, long fileSize) {
        if (fileName == null || fileName.isBlank()) {
            throw new VideoUploadFailedException("File name is required");
        }
        String lowerName = fileName.toLowerCase();
        if (!lowerName.endsWith(".mp4") && !lowerName.endsWith(".mov")
                && !lowerName.endsWith(".avi") && !lowerName.endsWith(".mkv")) {
            throw new VideoUploadFailedException("Unsupported video format. Supported: mp4, mov, avi, mkv");
        }
        if (fileSize <= 0 || fileSize > MAX_FILE_SIZE) {
            throw new VideoUploadFailedException("File size exceeds limit. Max: 10GB");
        }
    }

    private record UploadContext(String context, Long educatorId, String fileName, long fileSize) {}
}
