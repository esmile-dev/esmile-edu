package com.esmile.edu.dto.response;

import com.esmile.edu.module.course.ChapterEntity;
import java.util.List;

public record ChapterResponse(
    Long id,
    Long courseId,
    String title,
    Integer position,
    List<LessonResponse> lessons
) {
    public static ChapterResponse from(ChapterEntity entity, List<LessonResponse> lessons) {
        return new ChapterResponse(
            entity.getId(),
            entity.getCourseId(),
            entity.getTitle(),
            entity.getPosition(),
            lessons
        );
    }
}
