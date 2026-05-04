package com.esmile.edu.module.course;

import com.esmile.edu.common.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chapters")
public class ChapterEntity extends BaseEntity {
    @Column(nullable = false)
    private String title;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(nullable = false)
    private Integer position;

    // 构造函数
    public ChapterEntity() {}

    public ChapterEntity(String title, Long courseId, Integer position) {
        this.title = title;
        this.courseId = courseId;
        this.position = position;
    }

    // Getters
    public String getTitle() { return title; }
    public Long getCourseId() { return courseId; }
    public Integer getPosition() { return position; }

    // Setters
    public void setTitle(String title) { this.title = title; }
    public void setPosition(Integer position) { this.position = position; }
}
