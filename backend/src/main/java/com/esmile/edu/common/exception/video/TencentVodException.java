package com.esmile.edu.common.exception.video;

import com.esmile.edu.common.exception.BusinessException;
import com.esmile.edu.common.exception.ExternalServiceException;

/**
 * 腾讯云VOD服务错误异常 (50302)
 */
public class TencentVodException extends ExternalServiceException {

    public TencentVodException(String message) {
        super(TENCENT_VOD_SERVICE_ERROR, message);
    }
}
