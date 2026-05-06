package com.esmile.edu.common.exception.course;

import com.esmile.edu.common.exception.BusinessException;
import com.esmile.edu.common.exception.ResourceNotFoundException;

/**
 * 章节不存在异常 (10204)
 */
public class ChapterNotFoundException extends ResourceNotFoundException {

    public ChapterNotFoundException(Long chapterId) {
        super("Chapter", chapterId);
    }

    @Override
    public int getCode() {
        return CHAPTER_NOT_FOUND;
    }
}
