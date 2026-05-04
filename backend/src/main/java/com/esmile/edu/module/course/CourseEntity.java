package com.esmile.edu.module.course;

import com.esmile.edu.common.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "courses")
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

    // Getters
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Long getEducatorId() { return educatorId; }
    public String getCover() { return cover; }
    public CourseStatus getStatus() { return status; }
    public LocalDateTime getPublishedAt() { return publishedAt; }

    // Setters
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setCover(String cover) { this.cover = cover; }
}
