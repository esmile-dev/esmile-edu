package com.esmile.edu.common.video;

import com.esmile.edu.common.exception.BusinessRuleException;
import com.esmile.edu.common.exception.video.VideoUploadFailedException;
import com.esmile.edu.dto.response.VideoUploadResult;
import com.esmile.edu.module.course.CourseRepository;
import com.esmile.edu.module.course.LessonEntity;
import com.esmile.edu.module.course.LessonRepository;
import com.esmile.edu.module.course.LessonStatus;
import com.tencentcloudapi.vod.v20180717.VodClient;
import com.tencentcloudapi.vod.v20180717.models.ApplyUploadResponse;
import com.tencentcloudapi.vod.v20180717.models.CommitUploadResponse;
import com.tencentcloudapi.vod.v20180717.models.DescribeMediaInfosRequest;
import com.tencentcloudapi.vod.v20180717.models.DescribeMediaInfosResponse;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;

/**
 * Tencent Cloud VOD provider.
 *
 * <p>This implementation integrates with Tencent Cloud VOD for video upload and playback.
 * Configure via application.properties:</p>
 * <pre>
 * video.provider=tencent
 * tencent.vod.secret-id=${TENCENT_VOD_SECRET_ID}
 * tencent.vod.secret-key=${TENCENT_VOD_SECRET_KEY}
 * </pre>
 */
@Component
@ConditionalOnProperty(name = "video.provider", havingValue = "tencent")
public class TencentVodProvider implements VideoStoragePort {

    private static final Logger log = LoggerFactory.getLogger(TencentVodProvider.class);
    private static final int SIGNATURE_EXPIRE_SECONDS = 3600;
    private static final Random RANDOM = new SecureRandom();

    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;
    private VodClient vodClient;

    @Value("${tencent.vod.secret-id:}")
    private String secretId;

    @Value("${tencent.vod.secret-key:}")
    private String secretKey;

    public TencentVodProvider(LessonRepository lessonRepository, CourseRepository courseRepository) {
        this.lessonRepository = lessonRepository;
        this.courseRepository = courseRepository;
    }

    @PostConstruct
    public void init() {
        if (secretId == null || secretId.isBlank() || secretKey == null || secretKey.isBlank()) {
            log.warn("[TENCENT VOD] Credentials not configured, VodClient will not be initialized");
            this.vodClient = null;
            return;
        }
        com.tencentcloudapi.common.Credential credential = new com.tencentcloudapi.common.Credential(secretId, secretKey);
        this.vodClient = new VodClient(credential, "");
        log.info("[TENCENT VOD] VodClient initialized successfully");
    }

    @Override
    public VideoUploadResult applyUpload(Long educatorId, String fileName, long fileSize) {
        if (vodClient == null) {
            throw new VideoUploadFailedException("Tencent VOD credentials not configured");
        }

        // Validate locally before API call
        if (fileName == null || fileName.isBlank()) {
            throw new VideoUploadFailedException("File name is required");
        }
        if (fileSize <= 0 || fileSize > 1024 * 1024 * 1024) {
            throw new VideoUploadFailedException("File size exceeds limit. Max: 1GB");
        }

        try {
            String signature = generateUploadSignature();

            com.tencentcloudapi.vod.v20180717.models.ApplyUploadRequest request =
                new com.tencentcloudapi.vod.v20180717.models.ApplyUploadRequest();
            request.setMediaName(fileName);
            request.setMediaType(getMediaType(fileName));

            ApplyUploadResponse response = vodClient.ApplyUpload(request);

            log.info("[TENCENT VOD] Applied for upload: sessionKey={}, mediaName={}",
                response.getVodSessionKey(), fileName);

            return new VideoUploadResult(
                response.getVodSessionKey(),
                signature,
                null,
                response.getVodSessionKey()
            );
        } catch (VideoUploadFailedException e) {
            throw e;
        } catch (Exception e) {
            log.error("[TENCENT VOD] Failed to apply for upload", e);
            throw new VideoUploadFailedException("Failed to apply for video upload: " + e.getMessage());
        }
    }

    @Override
    public LessonEntity confirmUpload(Long lessonId, Long educatorId, String videoId) {
        if (vodClient == null) {
            throw new VideoUploadFailedException("Tencent VOD credentials not configured");
        }

        try {
            LessonEntity lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new VideoUploadFailedException("Lesson not found: " + lessonId));

            Long courseId = lesson.getCourseId();
            if (courseId == null || courseRepository.findByIdAndEducatorId(courseId, educatorId).isEmpty()) {
                throw new BusinessRuleException(BusinessRuleException.ACCESS_DENIED,
                    "Unauthorized: lesson does not belong to educator");
            }

            com.tencentcloudapi.vod.v20180717.models.CommitUploadRequest request =
                new com.tencentcloudapi.vod.v20180717.models.CommitUploadRequest();
            request.setVodSessionKey(videoId);

            CommitUploadResponse response = vodClient.CommitUpload(request);

            log.info("[TENCENT VOD] Upload committed: lessonId={}, fileId={}", lessonId, response.getFileId());

            lesson.setVideoId(response.getFileId());
            lesson.setStatus(LessonStatus.READY);

            return lessonRepository.save(lesson);
        } catch (BusinessRuleException e) {
            throw e;
        } catch (Exception e) {
            log.error("[TENCENT VOD] Failed to confirm upload", e);
            throw new VideoUploadFailedException("Failed to confirm video upload: " + e.getMessage());
        }
    }

    @Override
    public String getPlaybackUrl(String videoId) {
        if (vodClient == null) {
            throw new VideoUploadFailedException("Tencent VOD credentials not configured");
        }

        try {
            DescribeMediaInfosRequest request = new DescribeMediaInfosRequest();
            request.setFileIds(new String[]{ videoId });

            DescribeMediaInfosResponse response = vodClient.DescribeMediaInfos(request);

            if (response.getMediaInfoSet() != null && response.getMediaInfoSet().length > 0) {
                var mediaInfo = response.getMediaInfoSet()[0];
                String[] urlSet = getMediaUrls(mediaInfo);
                if (urlSet != null && urlSet.length > 0) {
                    return urlSet[0];
                }
            }

            throw new VideoUploadFailedException("Playback URL not found for video: " + videoId);
        } catch (VideoUploadFailedException e) {
            throw e;
        } catch (Exception e) {
            log.error("[TENCENT VOD] Failed to get playback URL", e);
            throw new VideoUploadFailedException("Failed to get playback URL: " + e.getMessage());
        }
    }

    private String[] getMediaUrls(Object mediaInfo) {
        try {
            java.lang.reflect.Method method = mediaInfo.getClass().getMethod("getMediaUrlSet");
            return (String[]) method.invoke(mediaInfo);
        } catch (Exception e) {
            log.debug("[TENCENT VOD] Could not get MediaUrlSet via reflection: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public String getProviderName() {
        return "tencent";
    }

    private String generateUploadSignature() {
        long currentTime = System.currentTimeMillis() / 1000;
        long expireTime = currentTime + SIGNATURE_EXPIRE_SECONDS;
        int random = RANDOM.nextInt(Integer.MAX_VALUE);

        String signStr = String.format(
            "secretId=%s&currentTimeStamp=%d&expireTimeStamp=%d&random=%d",
            secretId, currentTime, expireTime, random
        );

        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA1");
            mac.init(keySpec);
            byte[] hash = mac.doFinal(signStr.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash) + ";" + signStr;
        } catch (Exception e) {
            throw new VideoUploadFailedException("Failed to generate upload signature: " + e.getMessage());
        }
    }

    private String getMediaType(String fileName) {
        if (fileName == null) return "mp4";
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".mp4")) return "mp4";
        if (lower.endsWith(".mov")) return "mov";
        if (lower.endsWith(".avi")) return "avi";
        if (lower.endsWith(".mkv")) return "mkv";
        if (lower.endsWith(".flv")) return "flv";
        if (lower.endsWith(".wmv")) return "wmv";
        if (lower.endsWith(".webm")) return "webm";
        if (lower.endsWith(".m4v")) return "m4v";
        return "mp4";
    }
}
