package com.esmile.edu.common.exception.course;

import com.esmile.edu.common.exception.BusinessException;
import com.esmile.edu.common.exception.BusinessRuleException;

/**
 * 已选修异常 (10207)
 */
public class AlreadyEnrolledException extends BusinessRuleException {

    public AlreadyEnrolledException() {
        super(ALREADY_ENROLLED, "已选修此课程");
    }
}
