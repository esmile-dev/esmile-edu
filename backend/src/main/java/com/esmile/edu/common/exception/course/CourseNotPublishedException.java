package com.esmile.edu.common.exception.course;

import com.esmile.edu.common.exception.BusinessException;
import com.esmile.edu.common.exception.BusinessRuleException;

/**
 * 课程未发布异常 (10202)
 */
public class CourseNotPublishedException extends BusinessRuleException {

    public CourseNotPublishedException() {
        super(COURSE_NOT_PUBLISHED, "课程未发布");
    }
}
