package com.esmile.edu.common.exception.user;

import com.esmile.edu.common.exception.BusinessException;
import com.esmile.edu.common.exception.BusinessRuleException;

/**
 * 无法修改管理员状态异常 (10106)
 */
public class CannotModifyAdminStatusException extends BusinessRuleException {

    public CannotModifyAdminStatusException() {
        super(CANNOT_MODIFY_ADMIN_STATUS, "无法修改管理员状态");
    }
}
