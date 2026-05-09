package com.esmile.edu.dto.response;

import com.esmile.edu.module.course.CourseEntity;
import com.esmile.edu.module.course.CourseStatus;
import com.esmile.edu.module.course.EnrollmentStatus;
import java.time.LocalDateTime;
import java.util.List;

public record CourseDetailResponse(
    Long id,
    String title,
    String description,
    Long educatorId,
    String educatorName,
    String educatorAvatar,
    String cover,
    CourseStatus status,
    LocalDateTime publishedAt,
    List<ChapterResponse> chapters,
    EnrollmentStatus enrollmentStatus,
    LocalDateTime enrollmentExpiresAt,
    Long currentLessonId
) {
    public static CourseDetailResponse from(CourseEntity entity, String educatorName, String educatorAvatar, List<ChapterResponse> chapters, EnrollmentStatus enrollmentStatus, LocalDateTime enrollmentExpiresAt, Long currentLessonId) {
        return new CourseDetailResponse(
            entity.getId(),
            entity.getTitle(),
            entity.getDescription(),
            entity.getEducatorId(),
            educatorName,
            educatorAvatar,
            entity.getCover(),
            entity.getStatus(),
            entity.getPublishedAt(),
            chapters,
            enrollmentStatus,
            enrollmentExpiresAt,
            currentLessonId
        );
    }
}
