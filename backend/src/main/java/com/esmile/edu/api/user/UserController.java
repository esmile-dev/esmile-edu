package com.esmile.edu.api.user;

import com.esmile.edu.biz.UserBizService;
import com.esmile.edu.common.ApiResponse;
import com.esmile.edu.dto.request.SendCodeRequest;
import com.esmile.edu.dto.request.VerifyCodeRequest;
import com.esmile.edu.dto.response.AuthResponse;
import com.esmile.edu.dto.response.UserResponse;
import com.esmile.edu.module.user.Role;
import com.esmile.edu.module.user.UserStatus;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class UserController {
    private final UserBizService userBizService;

    public UserController(UserBizService userBizService) {
        this.userBizService = userBizService;
    }

    // 学生认证 - 发送验证码
    @PostMapping("/student/auth/send-code")
    public ApiResponse<Void> sendCodeStudent(@Valid @RequestBody SendCodeRequest request) {
        userBizService.sendCode(request.email(), Role.STUDENT);
        return ApiResponse.ok(null);
    }

    // 学生认证 - 验证验证码
    @PostMapping("/student/auth/verify-code")
    public ApiResponse<AuthResponse> verifyCodeStudent(@Valid @RequestBody VerifyCodeRequest request) {
        return ApiResponse.ok(userBizService.verifyCode(request.email(), request.code(), Role.STUDENT));
    }

    @GetMapping("/student/auth/me")
    public ApiResponse<UserResponse> getCurrentStudent(@RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        return ApiResponse.ok(userBizService.findById(userId));
    }

    // 教师认证 - 发送验证码
    @PostMapping("/teacher/auth/send-code")
    public ApiResponse<Void> sendCodeTeacher(@Valid @RequestBody SendCodeRequest request) {
        userBizService.sendCode(request.email(), Role.TEACHER);
        return ApiResponse.ok(null);
    }

    // 教师认证 - 验证验证码
    @PostMapping("/teacher/auth/verify-code")
    public ApiResponse<AuthResponse> verifyCodeTeacher(@Valid @RequestBody VerifyCodeRequest request) {
        return ApiResponse.ok(userBizService.verifyCode(request.email(), request.code(), Role.TEACHER));
    }

    @GetMapping("/teacher/auth/me")
    public ApiResponse<UserResponse> getCurrentTeacher(@RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        return ApiResponse.ok(userBizService.findById(userId));
    }

    // 管理员接口
    @GetMapping("/admin/users")
    public ApiResponse<Page<UserResponse>> listUsers(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) UserStatus status,
            Pageable pageable) {
        return ApiResponse.ok(userBizService.listUsers(role, status, pageable));
    }

    @PutMapping("/admin/users/{id}/approve")
    public ApiResponse<Void> approveTeacher(@PathVariable Long id) {
        userBizService.approveTeacher(id);
        return ApiResponse.ok(null);
    }

    @PutMapping("/admin/users/{id}/status")
    public ApiResponse<Void> updateUserStatus(
            @PathVariable Long id,
            @RequestParam UserStatus status,
            @RequestParam(required = false) String reason) {
        if (status == UserStatus.DISABLED) {
            userBizService.disableUser(id, reason);
        } else if (status == UserStatus.ACTIVE) {
            userBizService.enableUser(id);
        }
        return ApiResponse.ok(null);
    }
}
