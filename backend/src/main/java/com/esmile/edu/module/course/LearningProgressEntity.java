package com.esmile.edu.module.course;

import com.esmile.edu.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "learning_progress", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "lesson_id"}, name = "uk_learning_progress_user_lesson")
})
@Getter
@Setter
public class LearningProgressEntity extends BaseEntity {
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "lesson_id", nullable = false)
    private Long lessonId;

    @Column(name = "watched_seconds")
    private Integer watchedSeconds = 0;

    @Column(name = "is_completed")
    private Boolean isCompleted = false;

    @Column(name = "last_watched_at")
    private LocalDateTime lastWatchedAt;

    public LearningProgressEntity() {}

    public LearningProgressEntity(Long userId, Long courseId, Long lessonId) {
        this.userId = userId;
        this.courseId = courseId;
        this.lessonId = lessonId;
    }
}
