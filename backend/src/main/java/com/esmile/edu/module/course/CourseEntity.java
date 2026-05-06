package com.esmile.edu.module.course;

import com.esmile.edu.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "courses")
@Getter
@Setter
public class CourseEntity extends BaseEntity {
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "educator_id", nullable = false)
    private Long educatorId;

    @Column(nullable = false)
    private String cover;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourseStatus status;

    private LocalDateTime publishedAt;

    // 构造函数
    public CourseEntity() {}

    public CourseEntity(String title, String description, Long educatorId, String cover) {
        this.title = title;
        this.description = description;
        this.educatorId = educatorId;
        this.cover = cover;
        this.status = CourseStatus.DRAFT;
    }

    // 业务方法
    public void publish() {
        this.status = CourseStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }

    public void archive() {
        this.status = CourseStatus.ARCHIVED;
    }
}
