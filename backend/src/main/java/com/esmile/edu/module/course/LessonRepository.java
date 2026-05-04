package com.esmile.edu.module.course;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<LessonEntity, Long> {
    List<LessonEntity> findByCourseIdOrderByPosition(Long courseId);
    List<LessonEntity> findByChapterIdOrderByPosition(Long chapterId);
}
