package com.esmile.edu.module.course;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChapterRepository extends JpaRepository<ChapterEntity, Long> {
    List<ChapterEntity> findByCourseIdOrderByPosition(Long courseId);
}
