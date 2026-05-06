package com.esmile.edu.dto.response;

import com.esmile.edu.module.course.LessonEntity;
import com.esmile.edu.module.course.LessonStatus;

public record LessonResponse(
    Long id,
    String title,
    Integer position,
    String videoId,
    String videoUrl,
    Integer duration,
    LessonStatus status
) {
    public static LessonResponse from(LessonEntity entity) {
        return new LessonResponse(
            entity.getId(),
            entity.getTitle(),
            entity.getPosition(),
            entity.getVideoId(),
            entity.getVideoUrl(),
            entity.getDuration(),
            entity.getStatus()
        );
    }
}
