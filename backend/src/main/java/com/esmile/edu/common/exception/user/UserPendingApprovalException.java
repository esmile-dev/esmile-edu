package com.esmile.edu.common.exception.user;

import com.esmile.edu.common.exception.BusinessException;
import com.esmile.edu.common.exception.BusinessRuleException;

/**
 * 教师待审批异常 (10103)
 */
public class UserPendingApprovalException extends BusinessRuleException {

    public UserPendingApprovalException() {
        super(USER_PENDING_APPROVAL, "教师待审批");
    }
}
