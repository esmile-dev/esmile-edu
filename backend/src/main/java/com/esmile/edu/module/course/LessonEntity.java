package com.esmile.edu.module.course;

import com.esmile.edu.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "lessons")
@Getter
@Setter
public class LessonEntity extends BaseEntity {
    @Column(nullable = false)
    private String title;

    @Column(name = "chapter_id", nullable = false)
    private Long chapterId;

    @Column(nullable = false)
    private Integer position;

    @Column(name = "video_url")
    private String videoUrl;

    private Integer duration;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "video_id")
    private String videoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LessonStatus status = LessonStatus.PROCESSING;

    // 构造函数
    public LessonEntity() {}

    public LessonEntity(String title, Long chapterId, Long courseId, Integer position) {
        this.title = title;
        this.chapterId = chapterId;
        this.courseId = courseId;
        this.position = position;
    }
}
