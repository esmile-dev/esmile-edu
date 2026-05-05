package com.esmile.edu.module.user;

import com.esmile.edu.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
public class UserEntity extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nickname;

    private String avatar;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    private LocalDateTime disabledAt;
    private String disableReason;

    // 构造函数
    public UserEntity() {}

    public UserEntity(String email, String password, String nickname, Role role) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.role = role;
        this.status = (role == Role.TEACHER) ? UserStatus.PENDING_APPROVAL : UserStatus.ACTIVE;
    }

    // 业务方法
    public void approve() {
        if (this.status != UserStatus.PENDING_APPROVAL) {
            throw new RuntimeException("只有待审批状态可以审批");
        }
        this.status = UserStatus.ACTIVE;
    }

    public void disable(String reason) {
        this.status = UserStatus.DISABLED;
        this.disabledAt = LocalDateTime.now();
        this.disableReason = reason;
    }

    public void enable() {
        if (this.status != UserStatus.DISABLED) {
            throw new RuntimeException("只有禁用状态可以启用");
        }
        this.status = UserStatus.ACTIVE;
        this.disabledAt = null;
        this.disableReason = null;
    }
}
