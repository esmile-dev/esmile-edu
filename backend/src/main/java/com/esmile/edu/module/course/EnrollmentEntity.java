package com.esmile.edu.module.course;

import com.esmile.edu.common.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "enrollments", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "course_id"}, name = "uk_enrollment_user_course")
})
public class EnrollmentEntity extends BaseEntity {
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "enrolled_at", nullable = false)
    private LocalDateTime enrolledAt;

    private LocalDateTime completedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnrollmentStatus status = EnrollmentStatus.ACTIVE;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    // 构造函数
    public EnrollmentEntity() {}

    public EnrollmentEntity(Long userId, Long courseId) {
        this.userId = userId;
        this.courseId = courseId;
        this.enrolledAt = LocalDateTime.now();
    }

    // Getters
    public Long getUserId() { return userId; }
    public Long getCourseId() { return courseId; }
    public LocalDateTime getEnrolledAt() { return enrolledAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public EnrollmentStatus getStatus() { return status; }
    public LocalDateTime getExpiresAt() { return expiresAt; }

    // Setters
    public void setStatus(EnrollmentStatus status) { this.status = status; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}
