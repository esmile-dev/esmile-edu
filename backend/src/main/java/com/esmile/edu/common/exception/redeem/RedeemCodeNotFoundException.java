package com.esmile.edu.common.exception.redeem;

import com.esmile.edu.common.exception.BusinessException;
import com.esmile.edu.common.exception.ResourceNotFoundException;

/**
 * 兑换码不存在异常 (10301)
 */
public class RedeemCodeNotFoundException extends ResourceNotFoundException {

    public RedeemCodeNotFoundException(String code) {
        super("RedeemCode", code);
    }

    @Override
    public int getCode() {
        return REDEEM_CODE_NOT_FOUND;
    }
}
