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
    String educatorName,
    String cover,
    CourseStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    int chapterCount,
    int lessonCount
) {
    public static CourseResponse from(CourseEntity entity, String educatorName) {
        return new CourseResponse(
            entity.getId(),
            entity.getTitle(),
            entity.getDescription(),
            entity.getEducatorId(),
            educatorName,
            entity.getCover(),
            entity.getStatus(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            0,
            0
        );
    }

    public static CourseResponse from(CourseEntity entity, String educatorName, int chapterCount, int lessonCount) {
        return new CourseResponse(
            entity.getId(),
            entity.getTitle(),
            entity.getDescription(),
            entity.getEducatorId(),
            educatorName,
            entity.getCover(),
            entity.getStatus(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            chapterCount,
            lessonCount
        );
    }

    public static CourseResponse from(CourseEntity entity, String educatorName, List<ChapterResponse> chapters) {
        return new CourseResponse(
            entity.getId(),
            entity.getTitle(),
            entity.getDescription(),
            entity.getEducatorId(),
            educatorName,
            entity.getCover(),
            entity.getStatus(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            chapters.size(),
            chapters.stream().mapToInt(ch -> ch.lessons().size()).sum()
        );
    }
}
