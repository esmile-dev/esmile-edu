package com.esmile.edu.api.video;

import com.esmile.edu.biz.VideoService;
import com.esmile.edu.common.ApiResponse;
import com.esmile.edu.dto.request.CommitUploadRequest;
import com.esmile.edu.dto.response.LessonResponse;
import com.esmile.edu.dto.response.VideoUploadSignature;
import com.esmile.edu.module.course.LessonEntity;
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
    public ApiResponse<VideoUploadSignature> applyUpload(
            @RequestParam String fileName,
            @RequestParam long fileSize,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long educatorId) {
        return ApiResponse.ok(videoService.applyUpload(educatorId, fileName, fileSize));
    }

    /**
     * Confirm video upload completion.
     * POST /teacher/video/commit-upload
     */
    @PostMapping("/commit-upload")
    public ApiResponse<LessonResponse> commitUpload(
            @Valid @RequestBody CommitUploadRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long educatorId) {
        LessonEntity lesson = videoService.confirmUpload(
            request.lessonId(),
            educatorId,
            request.videoId()
        );
        return ApiResponse.ok(LessonResponse.from(lesson));
    }
}
