package com.esmile.edu.common.exception.course;

import com.esmile.edu.common.exception.BusinessException;
import com.esmile.edu.common.exception.ResourceNotFoundException;

/**
 * 课程不存在异常 (10201)
 */
public class CourseNotFoundException extends ResourceNotFoundException {

    public CourseNotFoundException(Long courseId) {
        super("Course", courseId);
    }

    @Override
    public int getCode() {
        return COURSE_NOT_FOUND;
    }
}
