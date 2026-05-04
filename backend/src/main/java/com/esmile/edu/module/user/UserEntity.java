package com.esmile.edu.module.user;

import com.esmile.edu.common.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
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

    // Getters
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getNickname() { return nickname; }
    public String getAvatar() { return avatar; }
    public Role getRole() { return role; }
    public UserStatus getStatus() { return status; }
    public LocalDateTime getDisabledAt() { return disabledAt; }
    public String getDisableReason() { return disableReason; }
}
