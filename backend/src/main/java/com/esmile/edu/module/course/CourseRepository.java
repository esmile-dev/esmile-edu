package com.esmile.edu.module.course;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<CourseEntity, Long> {
    Optional<CourseEntity> findByIdAndEducatorId(Long id, Long educatorId);
    Page<CourseEntity> findByEducatorId(Long educatorId, Pageable pageable);
    Page<CourseEntity> findByStatus(CourseStatus status, Pageable pageable);
    List<CourseEntity> findByEducatorId(Long educatorId);
    @Query("SELECT c FROM CourseEntity c WHERE c.id IN :ids")
    List<CourseEntity> findByIdIn(@Param("ids") List<Long> ids);
}
