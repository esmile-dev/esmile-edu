package com.esmile.edu.common.exception.video;

import com.esmile.edu.common.exception.BusinessException;
import com.esmile.edu.common.exception.BusinessRuleException;

/**
 * 视频上传失败异常 (10401)
 */
public class VideoUploadFailedException extends BusinessRuleException {

    public VideoUploadFailedException(String message) {
        super(VIDEO_UPLOAD_FAILED, message);
    }
}
