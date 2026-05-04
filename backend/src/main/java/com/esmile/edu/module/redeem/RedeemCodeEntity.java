package com.esmile.edu.module.redeem;

import com.esmile.edu.common.BaseEntity;
import com.esmile.edu.common.exception.redeem.RedeemCodeAlreadyUsedException;
import com.esmile.edu.common.exception.redeem.RedeemCodeExpiredException;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "redeem_codes")
public class RedeemCodeEntity extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String code;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RedeemCodeStatus status;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    private LocalDateTime usedAt;

    @Column(name = "used_by")
    private Long usedBy;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "course_expires_at")
    private LocalDateTime courseExpiresAt;

    // 构造函数
    public RedeemCodeEntity() {}

    public RedeemCodeEntity(Long courseId, Long createdBy, LocalDateTime expiresAt, LocalDateTime courseExpiresAt) {
        this.code = generateCode();
        this.courseId = courseId;
        this.createdBy = createdBy;
        this.expiresAt = expiresAt;
        this.courseExpiresAt = courseExpiresAt;
        this.status = RedeemCodeStatus.PENDING;
    }

    private String generateCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // 业务方法
    public void validate() {
        if (this.status == RedeemCodeStatus.REDEEMED) {
            throw new RedeemCodeAlreadyUsedException();
        }
        if (this.status == RedeemCodeStatus.EXPIRED) {
            throw new RedeemCodeExpiredException();
        }
        if (this.expiresAt != null && this.expiresAt.isBefore(LocalDateTime.now())) {
            throw new RedeemCodeExpiredException();
        }
    }

    public void markAsUsed(Long userId) {
        this.status = RedeemCodeStatus.REDEEMED;
        this.usedAt = LocalDateTime.now();
        this.usedBy = userId;
    }

    // Getters
    public String getCode() { return code; }
    public Long getCourseId() { return courseId; }
    public RedeemCodeStatus getStatus() { return status; }
    public Long getCreatedBy() { return createdBy; }
    public LocalDateTime getUsedAt() { return usedAt; }
    public Long getUsedBy() { return usedBy; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public LocalDateTime getCourseExpiresAt() { return courseExpiresAt; }
}
