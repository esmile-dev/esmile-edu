package com.esmile.edu.api.course;

import com.esmile.edu.biz.CourseBizService;
import com.esmile.edu.common.ApiResponse;
import com.esmile.edu.dto.request.CreateChapterRequest;
import com.esmile.edu.dto.request.CreateCourseRequest;
import com.esmile.edu.dto.request.CreateLessonRequest;
import com.esmile.edu.dto.request.UpdateChapterRequest;
import com.esmile.edu.dto.request.UpdateCourseRequest;
import com.esmile.edu.dto.request.UpdateLessonRequest;
import com.esmile.edu.dto.response.ChapterResponse;
import com.esmile.edu.dto.response.CourseDetailResponse;
import com.esmile.edu.dto.response.CourseResponse;
import com.esmile.edu.dto.response.LessonResponse;
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
    public ApiResponse<CourseResponse> createCourse(
            @Valid @RequestBody CreateCourseRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long educatorId) {
        return ApiResponse.created(courseBizService.createCourse(request, educatorId));
    }

    @PutMapping("/teacher/courses/{id}/publish")
    public ApiResponse<CourseResponse> publishCourse(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long educatorId) {
        return ApiResponse.ok(courseBizService.publishCourse(id, educatorId));
    }

    @GetMapping("/teacher/courses/{id}")
    public ApiResponse<CourseResponse> getCourseById(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long educatorId) {
        return ApiResponse.ok(courseBizService.getCourseById(id, educatorId));
    }

    @PutMapping("/teacher/courses/{id}")
    public ApiResponse<CourseResponse> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCourseRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long educatorId) {
        return ApiResponse.ok(courseBizService.updateCourse(id, educatorId, request));
    }

    @DeleteMapping("/teacher/courses/{id}")
    public ApiResponse<Void> deleteCourse(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long educatorId) {
        courseBizService.deleteCourse(id, educatorId);
        return ApiResponse.ok(null);
    }

    // 课程浏览
    @GetMapping("/courses")
    public ApiResponse<Page<CourseResponse>> listCourses(Pageable pageable) {
        return ApiResponse.ok(courseBizService.listCourses(pageable));
    }

    @GetMapping("/courses/{id}")
    public ApiResponse<CourseDetailResponse> getCourseDetail(@PathVariable Long id) {
        return ApiResponse.ok(courseBizService.getCourseDetail(id));
    }

    // 章节管理
    @PostMapping("/teacher/chapters")
    public ApiResponse<ChapterResponse> createChapter(
            @Valid @RequestBody CreateChapterRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long educatorId) {
        return ApiResponse.created(courseBizService.createChapter(request));
    }

    @PutMapping("/teacher/chapters/{id}")
    public ApiResponse<ChapterResponse> updateChapter(
            @PathVariable Long id,
            @Valid @RequestBody UpdateChapterRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long educatorId) {
        return ApiResponse.ok(courseBizService.updateChapter(id, educatorId, request));
    }

    @DeleteMapping("/teacher/chapters/{id}")
    public ApiResponse<Void> deleteChapter(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long educatorId) {
        courseBizService.deleteChapter(id, educatorId);
        return ApiResponse.ok(null);
    }

    // 课时管理
    @PostMapping("/teacher/lessons")
    public ApiResponse<LessonResponse> createLesson(@Valid @RequestBody CreateLessonRequest request) {
        return ApiResponse.created(courseBizService.createLesson(request));
    }

    @PutMapping("/teacher/lessons/{id}")
    public ApiResponse<LessonResponse> updateLesson(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLessonRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long educatorId) {
        return ApiResponse.ok(courseBizService.updateLesson(id, educatorId, request));
    }

    @DeleteMapping("/teacher/lessons/{id}")
    public ApiResponse<Void> deleteLesson(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long educatorId) {
        courseBizService.deleteLesson(id, educatorId);
        return ApiResponse.ok(null);
    }

    // 选课
    @PostMapping("/student/courses/{id}/enroll")
    public ApiResponse<Void> enrollCourse(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        courseBizService.enrollCourse(userId, id);
        return ApiResponse.created(null);
    }

    @GetMapping("/student/my-courses")
    public ApiResponse<List<CourseResponse>> myCourses(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        return ApiResponse.ok(courseBizService.listEnrolledCourses(userId));
    }

    @GetMapping("/teacher/my-courses")
    public ApiResponse<Page<CourseResponse>> myTeachingCourses(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long educatorId,
            Pageable pageable) {
        return ApiResponse.ok(courseBizService.listCoursesByEducator(educatorId, pageable));
    }
}
