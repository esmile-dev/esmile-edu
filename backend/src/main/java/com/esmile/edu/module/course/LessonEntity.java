package com.esmile.edu.module.course;

import com.esmile.edu.common.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "lessons")
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

    // Getters
    public String getTitle() { return title; }
    public Long getChapterId() { return chapterId; }
    public Integer getPosition() { return position; }
    public String getVideoUrl() { return videoUrl; }
    public Integer getDuration() { return duration; }
    public Long getCourseId() { return courseId; }
    public String getVideoId() { return videoId; }
    public LessonStatus getStatus() { return status; }

    // Setters
    public void setTitle(String title) { this.title = title; }
    public void setPosition(Integer position) { this.position = position; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public void setVideoId(String videoId) { this.videoId = videoId; }
    public void setStatus(LessonStatus status) { this.status = status; }
}
