package com.esmile.edu.module.course;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<LessonEntity, Long> {
    List<LessonEntity> findByCourseIdOrderByPosition(Long courseId);
    List<LessonEntity> findByChapterIdOrderByPosition(Long chapterId);
    Optional<LessonEntity> findByVideoId(String videoId);
}
