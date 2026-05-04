package com.esmile.edu.common.exception.redeem;

import com.esmile.edu.common.exception.BusinessException;
import com.esmile.edu.common.exception.BusinessRuleException;

/**
 * 兑换码已被使用异常 (10303)
 */
public class RedeemCodeAlreadyUsedException extends BusinessRuleException {

    public RedeemCodeAlreadyUsedException() {
        super(REDEEM_CODE_ALREADY_USED, "兑换码已被使用");
    }
}
