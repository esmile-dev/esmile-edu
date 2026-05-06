package com.esmile.edu.common.exception;

/**
 * 资源不存在异常 (40401-40408)
 *
 * <p>用于资源查找失败的业务异常，如用户不存在、课程不存在等。</p>
 */
public class ResourceNotFoundException extends BusinessException {

    private final String resourceType;
    private final Object resourceId;

    public ResourceNotFoundException(String resourceType, Object resourceId) {
        super(resourceType + " not found: " + resourceId);
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public static ResourceNotFoundException user(Long userId) {
        return new ResourceNotFoundException("User", userId);
    }

    public static ResourceNotFoundException course(Long courseId) {
        return new ResourceNotFoundException("Course", courseId);
    }

    public static ResourceNotFoundException chapter(Long chapterId) {
        return new ResourceNotFoundException("Chapter", chapterId);
    }

    public static ResourceNotFoundException lesson(Long lessonId) {
        return new ResourceNotFoundException("Lesson", lessonId);
    }

    public static ResourceNotFoundException redeemCode(String code) {
        return new ResourceNotFoundException("RedeemCode", code);
    }

    public String resourceType() {
        return resourceType;
    }

    public Object resourceId() {
        return resourceId;
    }

    @Override
    public int getCode() {
        return 40401;
    }
}
