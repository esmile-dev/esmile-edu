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
public interface EnrollmentRepository extends JpaRepository<EnrollmentEntity, Long> {
    Optional<EnrollmentEntity> findByUserIdAndCourseId(Long userId, Long courseId);
    List<EnrollmentEntity> findByUserIdAndCourseIdIn(Long userId, List<Long> courseIds);
    List<EnrollmentEntity> findByUserId(Long userId);
    Page<EnrollmentEntity> findByCourseId(Long courseId, Pageable pageable);
    boolean existsByUserIdAndCourseId(Long userId, Long courseId);
    @Query("SELECT e.courseId FROM EnrollmentEntity e WHERE e.userId = :userId")
    List<Long> findCourseIdsByUserId(@Param("userId") Long userId);
}
