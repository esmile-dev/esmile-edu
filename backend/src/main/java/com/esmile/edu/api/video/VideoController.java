package com.esmile.edu.api.video;

import com.esmile.edu.biz.VideoService;
import com.esmile.edu.common.ApiResponse;
import com.esmile.edu.common.auth.AuthContext;
import com.esmile.edu.common.auth.RequireRole;
import com.esmile.edu.dto.request.CommitUploadRequest;
import com.esmile.edu.dto.response.LessonResponse;
import com.esmile.edu.dto.response.VideoUploadSignature;
import com.esmile.edu.module.course.LessonEntity;
import com.esmile.edu.module.user.Role;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class VideoController {

    private final VideoService videoService;

    public VideoController(VideoService videoService) {
        this.videoService = videoService;
    }

    /**
     * Apply for video upload signature.
     * GET /teacher/video/apply-upload?fileName=xxx&fileSize=123456
     */
    @GetMapping("/teacher/video/apply-upload")
    @RequireRole(Role.TEACHER)
    public ApiResponse<VideoUploadSignature> applyUpload(
            @RequestParam String fileName,
            @RequestParam long fileSize) {
        var result = videoService.applyUpload(AuthContext.getCurrentUserId(), fileName, fileSize);
        return ApiResponse.ok(VideoUploadSignature.from(result));
    }

    /**
     * Confirm video upload completion.
     * POST /teacher/video/commit-upload
     */
    @PostMapping("/teacher/video/commit-upload")
    @RequireRole(Role.TEACHER)
    public ApiResponse<LessonResponse> commitUpload(@Valid @RequestBody CommitUploadRequest request) {
        LessonEntity lesson = videoService.confirmUpload(
            request.lessonId(),
            AuthContext.getCurrentUserId(),
            request.videoId()
        );
        return ApiResponse.ok(LessonResponse.from(lesson));
    }

    /**
     * Get playback URL for a video.
     * GET /video/playback-url/{videoId}
     */
    @GetMapping("/video/playback-url/{videoId}")
    public ApiResponse<String> getPlaybackUrl(@PathVariable String videoId) {
        String playbackUrl = videoService.getPlaybackUrl(videoId);
        return ApiResponse.ok(playbackUrl);
    }
}
