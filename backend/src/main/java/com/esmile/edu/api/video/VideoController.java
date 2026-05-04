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
@RequestMapping("/api/v1/teacher/video")
public class VideoController {
    private final VideoService videoService;

    public VideoController(VideoService videoService) {
        this.videoService = videoService;
    }

    /**
     * Apply for video upload signature.
     * GET /teacher/video/apply-upload?fileName=xxx&fileSize=123456
     */
    @GetMapping("/apply-upload")
    @RequireRole(Role.TEACHER)
    public ApiResponse<VideoUploadSignature> applyUpload(
            @RequestParam String fileName,
            @RequestParam long fileSize) {
        return ApiResponse.ok(videoService.applyUpload(AuthContext.getCurrentUserId(), fileName, fileSize));
    }

    /**
     * Confirm video upload completion.
     * POST /teacher/video/commit-upload
     */
    @PostMapping("/commit-upload")
    @RequireRole(Role.TEACHER)
    public ApiResponse<LessonResponse> commitUpload(@Valid @RequestBody CommitUploadRequest request) {
        LessonEntity lesson = videoService.confirmUpload(
            request.lessonId(),
            AuthContext.getCurrentUserId(),
            request.videoId()
        );
        return ApiResponse.ok(LessonResponse.from(lesson));
    }
}
