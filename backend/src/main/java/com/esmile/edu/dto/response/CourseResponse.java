package com.esmile.edu.dto.response;

import com.esmile.edu.module.course.CourseEntity;
import com.esmile.edu.module.course.CourseStatus;

import java.time.LocalDateTime;
import java.util.List;

public record CourseResponse(
    Long id,
    String title,
    String description,
    Long educatorId,
    String cover,
    CourseStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    List<ChapterResponse> chapters
) {
    public static CourseResponse from(CourseEntity entity) {
        return new CourseResponse(
            entity.getId(),
            entity.getTitle(),
            entity.getDescription(),
            entity.getEducatorId(),
            entity.getCover(),
            entity.getStatus(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            List.of()
        );
    }

    public static CourseResponse from(CourseEntity entity, List<ChapterResponse> chapters) {
        return new CourseResponse(
            entity.getId(),
            entity.getTitle(),
            entity.getDescription(),
            entity.getEducatorId(),
            entity.getCover(),
            entity.getStatus(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            chapters
        );
    }
}
