package com.esmile.edu.biz;

import com.esmile.edu.common.exception.BusinessRuleException;
import com.esmile.edu.common.exception.BusinessException;
import com.esmile.edu.common.exception.course.AlreadyEnrolledException;
import com.esmile.edu.common.exception.course.CourseNotFoundException;
import com.esmile.edu.common.exception.redeem.RedeemCodeNotFoundException;
import com.esmile.edu.dto.request.GenerateCodesRequest;
import com.esmile.edu.dto.response.GenerateCodesResponse;
import com.esmile.edu.dto.response.RedeemResultResponse;
import com.esmile.edu.module.course.CourseEntity;
import com.esmile.edu.module.course.CourseRepository;
import com.esmile.edu.module.course.CourseStatus;
import com.esmile.edu.module.course.EnrollmentEntity;
import com.esmile.edu.module.course.EnrollmentRepository;
import com.esmile.edu.module.redeem.RedeemCodeEntity;
import com.esmile.edu.module.redeem.RedeemCodeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class RedeemBizService {
    private final RedeemCodeRepository redeemCodeRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    public RedeemBizService(
            RedeemCodeRepository redeemCodeRepository,
            CourseRepository courseRepository,
            EnrollmentRepository enrollmentRepository) {
        this.redeemCodeRepository = redeemCodeRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @Transactional
    public GenerateCodesResponse generateCodes(GenerateCodesRequest request, Long createdBy) {
        CourseEntity course = courseRepository.findById(request.courseId())
            .orElseThrow(() -> new CourseNotFoundException(request.courseId()));

        int qty = request.quantity();
        List<String> codes = new ArrayList<>();

        for (int i = 0; i < qty; i++) {
            RedeemCodeEntity redeemCode = new RedeemCodeEntity(
                request.courseId(),
                createdBy,
                request.expiresAt(),
                request.courseExpiresAt()
            );
            redeemCodeRepository.save(redeemCode);
            codes.add(redeemCode.getCode());
        }

        return new GenerateCodesResponse(codes, qty);
    }

    @Transactional
    public RedeemResultResponse redeemCode(String code, Long userId) {
        // 1. 校验兑换码
        RedeemCodeEntity redeemCode = redeemCodeRepository.findByCode(code)
            .orElseThrow(() -> new RedeemCodeNotFoundException(code));
        redeemCode.validate();

        // 2. 校验课程
        CourseEntity course = courseRepository.findById(redeemCode.getCourseId())
            .orElseThrow(() -> new CourseNotFoundException(redeemCode.getCourseId()));

        // 2.1 检查课程状态
        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new BusinessRuleException(
                BusinessException.REDEEM_COURSE_NOT_PUBLISHED, "课程未发布");
        }

        // 3. 检查是否已选课
        if (enrollmentRepository.existsByUserIdAndCourseId(userId, redeemCode.getCourseId())) {
            throw new AlreadyEnrolledException();
        }

        // 4. 创建选课记录
        EnrollmentEntity enrollment = new EnrollmentEntity(userId, redeemCode.getCourseId());
        enrollmentRepository.save(enrollment);

        // 5. 更新兑换码状态
        redeemCode.markAsUsed(userId);
        redeemCodeRepository.save(redeemCode);

        return new RedeemResultResponse(course.getTitle(), enrollment.getId());
    }
}
