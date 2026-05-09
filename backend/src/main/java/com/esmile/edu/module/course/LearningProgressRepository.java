package com.esmile.edu.module.course;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LearningProgressRepository extends JpaRepository<LearningProgressEntity, Long> {
    Optional<LearningProgressEntity> findByUserIdAndLessonId(Long userId, Long lessonId);
    List<LearningProgressEntity> findByUserIdAndCourseId(Long userId, Long courseId);
}
