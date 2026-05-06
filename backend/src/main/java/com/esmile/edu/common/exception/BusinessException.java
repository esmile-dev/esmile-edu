package com.esmile.edu.common.exception;

/**
 * 业务异常基类
 *
 * <p>所有业务异常都继承自此类。错误码常量定义在此类中，各具体异常通过引用这些常量来设置错误码。</p>
 *
 * @see <a href="../../../docs/backend/technical/5-exception-handling/error-codes.md">错误码规范</a>
 * @see <a href="../../../docs/backend/technical/5-exception-handling/exception-hierarchy.md">异常体系规范</a>
 */
public abstract class BusinessException extends RuntimeException {
    protected BusinessException(String message) {
        super(message);
    }

    /**
     * 返回错误码
     */
    public abstract int getCode();

    // ========== Authentication (10001-10008) ==========
    public static final int INVALID_VERIFICATION_CODE = 10001;
    public static final int VERIFICATION_CODE_EXPIRED = 10002;
    public static final int VERIFICATION_CODE_RATE_LIMITED = 10003;
    public static final int INVALID_TOKEN = 10004;
    public static final int TOKEN_EXPIRED = 10005;

    // ========== User Module (10101-10199) ==========
    public static final int USER_NOT_FOUND = 10101;
    public static final int USER_DISABLED = 10102;
    public static final int USER_PENDING_APPROVAL = 10103;
    public static final int EMAIL_ALREADY_EXISTS = 10104;
    public static final int USER_IS_NOT_TEACHER = 10105;
    public static final int CANNOT_MODIFY_ADMIN_STATUS = 10106;
    public static final int INVALID_STATUS_TRANSITION = 10107;
    public static final int INVALID_CREDENTIALS = 10108;

    // ========== Course Module (10201-10299) ==========
    public static final int COURSE_NOT_FOUND = 10201;
    public static final int COURSE_NOT_PUBLISHED = 10202;
    public static final int NOT_COURSE_OWNER = 10203;
    public static final int CHAPTER_NOT_FOUND = 10204;
    public static final int LESSON_NOT_FOUND = 10205;
    public static final int COURSE_HAS_NO_CHAPTERS = 10206;
    public static final int ALREADY_ENROLLED = 10207;

    // ========== Redeem Module (10301-10399) ==========
    public static final int REDEEM_CODE_NOT_FOUND = 10301;
    public static final int REDEEM_CODE_EXPIRED = 10302;
    public static final int REDEEM_CODE_ALREADY_USED = 10303;
    public static final int REDEEM_CODE_COURSE_UNAVAILABLE = 10304;
    public static final int REDEEM_COURSE_NOT_FOUND = 10305;
    public static final int REDEEM_COURSE_NOT_PUBLISHED = 10306;
    public static final int INVALID_API_KEY = 10307;
    public static final int EXCEED_MAX_GENERATION_QUANTITY = 10308;

    // ========== Video Module (10401-10499) ==========
    public static final int VIDEO_UPLOAD_FAILED = 10401;
    public static final int VIDEO_PROCESSING_FAILED = 10402;
    public static final int VIDEO_NOT_READY = 10403;

    // ========== Validation (40001-40008) ==========
    public static final int VALIDATION_FAILED = 40001;

    // ========== Authorization (40301-40308) ==========
    public static final int ACCESS_DENIED = 40301;
    public static final int INSUFFICIENT_PERMISSIONS = 40302;

    // ========== Resource Not Found (40401-40408) ==========
    public static final int RESOURCE_NOT_FOUND = 40401;

    // ========== External Service (50301-50308) ==========
    public static final int EXTERNAL_SERVICE_UNAVAILABLE = 50301;
    public static final int TENCENT_VOD_SERVICE_ERROR = 50302;
    public static final int EMAIL_SERVICE_ERROR = 50303;

    // ========== System Error (90001-90999) ==========
    public static final int INTERNAL_SERVER_ERROR = 90001;
    public static final int SERVICE_UNAVAILABLE = 90002;
}
