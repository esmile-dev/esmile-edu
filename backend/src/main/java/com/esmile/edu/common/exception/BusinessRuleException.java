package com.esmile.edu.common.exception;

/**
 * 业务规则异常
 *
 * <p>用于违反业务规则的异常，如课程未发布、兑换码已过期、已选修等。</p>
 */
public class BusinessRuleException extends BusinessException {

    private final int code;

    public BusinessRuleException(int code, String message) {
        super(message);
        this.code = code;
    }

    @Override
    public int getCode() {
        return code;
    }
}
