package com.esmile.edu.common.exception.course;

import com.esmile.edu.common.exception.BusinessException;
import com.esmile.edu.common.exception.ResourceNotFoundException;

/**
 * 课时不存在异常 (10205)
 */
public class LessonNotFoundException extends ResourceNotFoundException {

    public LessonNotFoundException(Long lessonId) {
        super("Lesson", lessonId);
    }

    @Override
    public int getCode() {
        return LESSON_NOT_FOUND;
    }
}
