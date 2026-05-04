package com.esmile.edu.api.redeem;

import com.esmile.edu.biz.RedeemBizService;
import com.esmile.edu.common.ApiResponse;
import com.esmile.edu.common.auth.AuthContext;
import com.esmile.edu.common.auth.RequireAuth;
import com.esmile.edu.common.auth.RequireRole;
import com.esmile.edu.dto.request.GenerateCodesRequest;
import com.esmile.edu.dto.request.RedeemCodeRequest;
import com.esmile.edu.dto.response.GenerateCodesResponse;
import com.esmile.edu.dto.response.RedeemCodeResponse;
import com.esmile.edu.dto.response.RedeemResultResponse;
import com.esmile.edu.module.user.Role;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class RedeemCodeController {
    private final RedeemBizService redeemBizService;

    public RedeemCodeController(RedeemBizService redeemBizService) {
        this.redeemBizService = redeemBizService;
    }

    // 教师生成兑换码
    @PostMapping("/teacher/redeem-codes/generate")
    @RequireRole(Role.TEACHER)
    public ApiResponse<GenerateCodesResponse> generateCodes(@Valid @RequestBody GenerateCodesRequest request) {
        return ApiResponse.created(redeemBizService.generateCodes(request, AuthContext.getCurrentUserId()));
    }

    // 外部系统批量生成兑换码（API Key认证）
    @PostMapping("/redeem-codes/apply")
    public ApiResponse<GenerateCodesResponse> applyCodes(@Valid @RequestBody GenerateCodesRequest request) {
        // External system uses API Key auth, no user context needed
        return ApiResponse.created(redeemBizService.generateCodes(request, null));
    }

    // 学生兑换课程
    @PostMapping("/student/redeem")
    @RequireAuth
    public ApiResponse<RedeemResultResponse> redeemCode(@Valid @RequestBody RedeemCodeRequest request) {
        return ApiResponse.ok(redeemBizService.redeemCode(request.code(), AuthContext.getCurrentUserId()));
    }

    // 查看兑换码列表（教师）
    @GetMapping("/teacher/redeem-codes")
    @RequireRole(Role.TEACHER)
    public ApiResponse<Page<RedeemCodeResponse>> listCodes(Pageable pageable) {
        return ApiResponse.ok(redeemBizService.listCodesByCreator(AuthContext.getCurrentUserId(), pageable));
    }
}
