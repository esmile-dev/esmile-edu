package com.esmile.edu.common.exception.redeem;

import com.esmile.edu.common.exception.BusinessException;
import com.esmile.edu.common.exception.BusinessRuleException;

/**
 * 兑换码已过期异常 (10302)
 */
public class RedeemCodeExpiredException extends BusinessRuleException {

    public RedeemCodeExpiredException() {
        super(REDEEM_CODE_EXPIRED, "兑换码已过期");
    }
}
