package com.esmile.edu.api.course;

import com.esmile.edu.biz.CourseBizService;
import com.esmile.edu.common.ApiResponse;
import com.esmile.edu.common.auth.AuthContext;
import com.esmile.edu.common.auth.RequireAuth;
import com.esmile.edu.common.auth.RequireRole;
import com.esmile.edu.dto.request.CreateChapterRequest;
import com.esmile.edu.dto.request.CreateCourseRequest;
import com.esmile.edu.dto.request.CreateLessonRequest;
import com.esmile.edu.dto.request.UpdateChapterRequest;
import com.esmile.edu.dto.request.UpdateCourseRequest;
import com.esmile.edu.dto.request.UpdateLessonRequest;
import com.esmile.edu.dto.request.UpdateProgressRequest;
import com.esmile.edu.dto.response.ChapterResponse;
import com.esmile.edu.dto.response.CourseDetailResponse;
import com.esmile.edu.dto.response.CourseResponse;
import com.esmile.edu.dto.response.LessonResponse;
import com.esmile.edu.module.user.Role;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class CourseController {
    private final CourseBizService courseBizService;

    public CourseController(CourseBizService courseBizService) {
        this.courseBizService = courseBizService;
    }

    // 课程管理
    @PostMapping("/teacher/courses")
    @RequireRole(Role.TEACHER)
    public ApiResponse<CourseResponse> createCourse(@Valid @RequestBody CreateCourseRequest request) {
        return ApiResponse.created(courseBizService.createCourse(request, AuthContext.getCurrentUserId()));
    }

    @PutMapping("/teacher/courses/{id}/publish")
    @RequireRole(Role.TEACHER)
    public ApiResponse<CourseResponse> publishCourse(@PathVariable Long id) {
        return ApiResponse.ok(courseBizService.publishCourse(id, AuthContext.getCurrentUserId()));
    }

    @GetMapping("/teacher/courses/{id}")
    @RequireRole(Role.TEACHER)
    public ApiResponse<CourseResponse> getCourseById(@PathVariable Long id) {
        return ApiResponse.ok(courseBizService.getCourseById(id, AuthContext.getCurrentUserId()));
    }

    @PutMapping("/teacher/courses/{id}")
    @RequireRole(Role.TEACHER)
    public ApiResponse<CourseResponse> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCourseRequest request) {
        return ApiResponse.ok(courseBizService.updateCourse(id, AuthContext.getCurrentUserId(), request));
    }

    @DeleteMapping("/teacher/courses/{id}")
    @RequireRole(Role.TEACHER)
    public ApiResponse<Void> deleteCourse(@PathVariable Long id) {
        courseBizService.deleteCourse(id, AuthContext.getCurrentUserId());
        return ApiResponse.ok(null);
    }

    // 课程浏览（学生端）
    @GetMapping("/student/courses")
    public ApiResponse<Page<CourseResponse>> listCourses(Pageable pageable) {
        return ApiResponse.ok(courseBizService.listCourses(pageable));
    }

    @GetMapping("/student/courses/{id}")
    public ApiResponse<CourseDetailResponse> getCourseDetail(@PathVariable Long id) {
        return ApiResponse.ok(courseBizService.getCourseDetail(id, AuthContext.getCurrentUserId()));
    }

    // 章节管理
    @PostMapping("/teacher/chapters")
    @RequireRole(Role.TEACHER)
    public ApiResponse<ChapterResponse> createChapter(@Valid @RequestBody CreateChapterRequest request) {
        return ApiResponse.created(courseBizService.createChapter(request));
    }

    @PutMapping("/teacher/chapters/{id}")
    @RequireRole(Role.TEACHER)
    public ApiResponse<ChapterResponse> updateChapter(
            @PathVariable Long id,
            @Valid @RequestBody UpdateChapterRequest request) {
        return ApiResponse.ok(courseBizService.updateChapter(id, AuthContext.getCurrentUserId(), request));
    }

    @DeleteMapping("/teacher/chapters/{id}")
    @RequireRole(Role.TEACHER)
    public ApiResponse<Void> deleteChapter(@PathVariable Long id) {
        courseBizService.deleteChapter(id, AuthContext.getCurrentUserId());
        return ApiResponse.ok(null);
    }

    // 课时管理
    @PostMapping("/teacher/lessons")
    @RequireRole(Role.TEACHER)
    public ApiResponse<LessonResponse> createLesson(@Valid @RequestBody CreateLessonRequest request) {
        return ApiResponse.created(courseBizService.createLesson(request));
    }

    @PutMapping("/teacher/lessons/{id}")
    @RequireRole(Role.TEACHER)
    public ApiResponse<LessonResponse> updateLesson(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLessonRequest request) {
        return ApiResponse.ok(courseBizService.updateLesson(id, AuthContext.getCurrentUserId(), request));
    }

    @DeleteMapping("/teacher/lessons/{id}")
    @RequireRole(Role.TEACHER)
    public ApiResponse<Void> deleteLesson(@PathVariable Long id) {
        courseBizService.deleteLesson(id, AuthContext.getCurrentUserId());
        return ApiResponse.ok(null);
    }

    @GetMapping("/teacher/lessons/{id}")
    @RequireRole(Role.TEACHER)
    public ApiResponse<LessonResponse> getLesson(@PathVariable Long id) {
        return ApiResponse.ok(courseBizService.getLessonById(id));
    }

    // 选课（我的课程）
    @GetMapping("/student/my-courses")
    @RequireAuth
    public ApiResponse<List<CourseResponse>> myCourses() {
        return ApiResponse.ok(courseBizService.listEnrolledCourses(AuthContext.getCurrentUserId()));
    }

    @PostMapping("/student/progress")
    @RequireAuth
    public ApiResponse<Void> updateProgress(@Valid @RequestBody UpdateProgressRequest request) {
        courseBizService.updateProgress(AuthContext.getCurrentUserId(), request);
        return ApiResponse.ok(null);
    }

    @GetMapping("/teacher/my-courses")
    @RequireRole(Role.TEACHER)
    public ApiResponse<Page<CourseResponse>> myTeachingCourses(Pageable pageable) {
        return ApiResponse.ok(courseBizService.listCoursesByEducator(AuthContext.getCurrentUserId(), pageable));
    }
}
