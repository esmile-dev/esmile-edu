package com.esmile.edu.module.course;

import com.esmile.edu.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "chapters")
@Getter
@Setter
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
}
