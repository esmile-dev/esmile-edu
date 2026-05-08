package com.esmile.edu.biz;

import com.esmile.edu.common.EntityNotFoundException;
import com.esmile.edu.common.exception.BusinessRuleException;
import com.esmile.edu.common.video.VideoServiceFactory;
import com.esmile.edu.common.video.VideoStoragePort;
import com.esmile.edu.dto.response.VideoPlaybackResponse;
import com.esmile.edu.dto.response.VideoUploadResult;
import com.esmile.edu.module.course.EnrollmentRepository;
import com.esmile.edu.module.course.EnrollmentStatus;
import com.esmile.edu.module.course.LessonEntity;
import com.esmile.edu.module.course.LessonRepository;
import com.esmile.edu.module.user.UserEntity;
import com.esmile.edu.module.user.UserRepository;
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
    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;

    public VideoService(
        VideoServiceFactory videoServiceFactory,
        LessonRepository lessonRepository,
        EnrollmentRepository enrollmentRepository,
        UserRepository userRepository
    ) {
        this.videoServiceFactory = videoServiceFactory;
        this.lessonRepository = lessonRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
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
     * Get watermark text for a user (their email).
     *
     * @param userId the user ID
     * @return the user's email as watermark text
     * @throws EntityNotFoundException if user not found
     */
    public String getWatermarkText(Long userId) {
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        return user.getEmail();
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

    /**
     * Get playback URL with enrollment permission check.
     *
     * @param videoId the VOD video ID
     * @param userId the user ID requesting playback
     * @return video playback response with signed URL
     * @throws EntityNotFoundException if video not found
     * @throws BusinessRuleException if user is not enrolled or enrollment expired
     */
    public VideoPlaybackResponse getPlaybackUrl(String videoId, Long userId) {
        VideoStoragePort provider = videoServiceFactory.getVideoStorage();

        // Find lesson by videoId to get courseId for permission check
        LessonEntity lesson = lessonRepository.findByVideoId(videoId)
            .orElseThrow(() -> new EntityNotFoundException("Video not found: " + videoId));

        // Check user enrollment
        boolean hasEnrollment = enrollmentRepository.existsByUserIdAndCourseId(userId, lesson.getCourseId());
        if (!hasEnrollment) {
            throw new BusinessRuleException(
                BusinessRuleException.ACCESS_DENIED,
                "You do not have access to this video"
            );
        }

        // Check enrollment is active (not expired)
        var enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, lesson.getCourseId())
            .orElseThrow(() -> new BusinessRuleException(BusinessRuleException.ACCESS_DENIED, "Enrollment not found"));

        if (enrollment.getStatus() == EnrollmentStatus.EXPIRED) {
            throw new BusinessRuleException(
                BusinessRuleException.ACCESS_DENIED,
                "Your course access has expired"
            );
        }

        // Get signed playback URL from provider
        String watermarkText = getWatermarkText(userId);
        VideoPlaybackResponse response = provider.getPlaybackUrlWithSign(videoId);
        return new VideoPlaybackResponse(
            response.playbackUrl(),
            response.duration(),
            response.coverImage(),
            watermarkText
        );
    }
}
