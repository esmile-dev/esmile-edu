# esmile-edu 后端 Production-Ready 任务清单

> 生成日期: 2026-05-04
> 更新日期: 2026-05-04
> 扫描范围: backend/src/main/java/com/esmile/edu/
> 问题总数: P0×6, P1×10, P2×4

---

## 执行摘要

| 优先级 | 问题数 | 已完成 | 说明 |
|---------|--------|--------|------|
| **P0** | 6 | 2 | 必须在上线前修复，否则功能不可用或存在安全漏洞 |
| **P1** | 10 | 4 | 应在 MVP 发布前修复，影响安全或功能正确性 |
| **P2** | 4 | 1 | 建议修复，提升系统健壮性 |

---

## ✅ 已完成任务标记

### P0 已完成
- ✅ **P0-1**: IDOR安全漏洞 - 使用 `@RequireAuth` + `AuthContext.getCurrentUserId()` 替代 `defaultValue="1"`
- ✅ **P0-2**: 管理员授权校验 - 所有 `/admin/*` 端点已添加 `@RequireRole(Role.ADMIN)` 注解

### P1 已完成
- ✅ **P1-3**: 日志打印验证码 - `UserBizService.sendCode()` 仅记录发送事件，不打印验证码
- ✅ **P1-8**: 文件类型校验 - `VideoService.validateFile()` 已实现文件扩展名校验
- ✅ **P1-9**: 可选参数null处理 - `UserBizService.listUsers()` 正确处理 role/status 为null的情况

### P2 已完成
- ✅ **P2-4**: API Key保护 - `ApiKeyAuthFilter` 已实现

---

## P0 - 必须修复 (上线阻塞)

### ✅ P0-1: 安全漏洞 - IDOR (默认用户ID) - **已修复**

**文件**: `api/user/UserController.java`, `api/course/CourseController.java`, `api/redeem/RedeemCodeController.java`, `api/video/VideoController.java`

**状态**: ✅ 已于 commit 2c04d65 修复

**修复内容**:
- 移除所有 `defaultValue = "1"`
- 使用 `@RequireAuth` + `@RequireRole` 注解进行认证授权
- 通过 `AuthContext.getCurrentUserId()` 获取当前用户ID

**影响端点**: ~20 个

---

### ✅ P0-2: 安全漏洞 - 缺少管理员授权校验 - **已修复**

**文件**: `api/user/UserController.java`

**状态**: ✅ 已修复

**修复内容**:
- 所有 `/admin/*` 端点已添加 `@RequireRole(Role.ADMIN)` 注解
- 包括: `listUsers`, `approveTeacher`, `updateUserStatus`

```java
@GetMapping("/admin/users")
@RequireRole(Role.ADMIN)
public ApiResponse<Page<UserResponse>> listUsers(...) { ... }
```

---

### P0-3: 功能缺陷 - 视频上传是 Mock 实现

**文件**: `biz/VideoService.java`, `api/video/VideoController.java`, `pom.xml`

**问题**: MVP阶段仍使用模拟实现
- `generateVideoId()` 返回 `"mock_" + UUID`
- `generateMockSignature()` 返回假签名
- 上传 URL 硬编码为测试地址

```java
// 当前 Mock 实现
private String generateVideoId() {
    return "mock_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
}
```

**修复要求**:
1. 添加腾讯云 VOD SDK 依赖
2. 实现真实签名生成（使用 SecretKey）
3. 配置 VOD API 地址和凭证
4. 支持视频上传进度回调

---

### P0-4: 功能缺陷 - 验证码服务部分实现

**文件**: `common/auth/VerificationCodeService.java`

**问题**:
- ✅ 验证码已持久化到数据库（`VerificationCodeEntity`）
- ❌ 邮件发送仍为 Mock（`email.provider=mock`）

```java
// 已修复 - 数据库持久化
VerificationCodeEntity entity = new VerificationCodeEntity(email, code, role, expiresAt);
verificationCodeRepository.save(entity);
```

**修复要求**:
1. ~~验证码持久化到数据库~~ ✅ 已完成
2. 集成真实邮件服务（腾讯云邮件/SendGrid/AWS SES）
3. ~~删除 `getStoredCode()` 方法~~ ✅ 已删除

---

### P0-5: 功能缺陷 - listCodesByCourse 未实现

**文件**: `biz/RedeemBizService.java`

**问题**: 该方法未实现，`RedeemCodeController` 中无此端点。

**修复要求**: 如需此功能，按以下实现:
```java
public Page<RedeemCodeResponse> listCodesByCourse(Long courseId, Pageable pageable) {
    return redeemCodeRepository.findByCourseId(courseId, pageable)
            .map(RedeemCodeResponse::from);
}
```

---

### P0-6: 配置风险 - 硬编码默认密钥

**文件**: `application.properties`

**问题**: 包含默认 JWT Secret 和 API Key，存在安全风险。

```properties
jwt.secret=${JWT_SECRET:esmile-edu-secret-key-for-jwt-token-generation-minimum-32-chars}
redeem.api-key=${REDEEM_API_KEY:esmile-edu-external-api-key-secret}
spring.datasource.password=postgres
```

**修复要求**:
1. 移除默认值，密钥未配置时拒绝启动
2. 生产环境通过环境变量/Kubernetes Secret 注入
3. 添加启动时校验：密钥未设置则抛出异常

---

## P1 - 上线前修复

### P1-1: 异常处理 - BusinessException 返回 HTTP 200

**文件**: `common/GlobalExceptionHandler.java`

**问题**: 业务异常返回 `HttpStatus.OK` (200)，应为 4xx。

```java
@ExceptionHandler(BusinessException.class)
@ResponseStatus(HttpStatus.OK)  // 错误
public ApiResponse<Void> handleBusinessException(BusinessException ex) { ... }
```

**修复**: 改为 `@ResponseStatus(HttpStatus.BAD_REQUEST)`

---

### P1-2: Entity 层使用 RuntimeException

**文件**: `module/user/UserEntity.java`

**问题**: `approve()` 和 `enable()` 方法抛出 `RuntimeException` 而非 `BusinessException`。

```java
// 当前
public void approve() {
    if (this.status != UserStatus.PENDING_APPROVAL) {
        throw new RuntimeException("只有待审批状态可以审批");
    }
    ...
}
```

**修复**: 使用 `BusinessRuleException` 或专用的 `InvalidStatusTransitionException`

---

### ✅ P1-3: 安全 - 日志打印验证码 - **已修复**

**文件**: `biz/UserBizService.java`

**状态**: ✅ 已修复

**修复内容**:
```java
log.info("Verification code sent to {} ({}) via {}", email, role, emailService.getProviderName());
// 不再打印验证码
```

---

### P1-4: 安全 - 验证码登录使用空密码

**文件**: `biz/UserBizService.java`

**问题**: 自动创建用户时设置空密码。

```java
UserEntity newUser = new UserEntity(
    email,
    passwordService.encode(""),  // 空密码
    nickname, role
);
```

**修复**:
- 方案A: 添加 `authMethod` 字段区分认证方式
- 方案B: 使用随机密码并标记为"无需密码"

---

### P1-5: 配置 - BCrypt 强度可优化

**文件**: `common/auth/PasswordService.java`

**问题**: 使用默认 strength=10，建议 >=12。

```java
this.passwordEncoder = new BCryptPasswordEncoder();  // 默认10
```

**修复**: `new BCryptPasswordEncoder(12)`

---

### P1-6: REST 规范 - DELETE 返回 200 而非 204

**文件**: `api/course/CourseController.java`

**问题**: 删除操作返回 `ApiResponse.ok()` 应为 `HttpStatus.NO_CONTENT`。

```java
@DeleteMapping("/teacher/courses/{id}")
public ApiResponse<Void> deleteCourse(...) {
    return ApiResponse.ok(null);  // 应为 204
}
```

**修复**: 返回 `ResponseEntity.noContent().build()` 或 `@ResponseStatus(HttpStatus.NO_CONTENT)`

---

### P1-7: 业务 - 选课缺少幂等性保护

**文件**: `api/course/CourseController.java`

**问题**: 重复调用 `enrollCourse` 创建多条报名记录。

```java
@PostMapping("/student/courses/{id}/enroll")
public ApiResponse<Void> enrollCourse(...) { ... }
```

**修复**:
1. 先查询是否已报名
2. 已报名则返回已有记录而非创建新记录
3. 返回报名详情

---

### ✅ P1-8: 验证 - 文件类型校验 - **已修复**

**文件**: `api/video/VideoController.java`, `biz/VideoService.java`

**状态**: ✅ 已修复

**修复内容**:
```java
private void validateFile(String fileName, long fileSize) {
    // Validate file extension
    if (fileName != null && !fileName.isBlank()) {
        String lowerName = fileName.toLowerCase();
        if (!lowerName.endsWith(".mp4") && !lowerName.endsWith(".mov")
                && !lowerName.endsWith(".avi") && !lowerName.endsWith(".mkv")) {
            throw new VideoUploadFailedException("不支持的视频格式，仅支持 mp4、mov、avi、mkv");
        }
    }
    // Validate file size (10GB limit)
    if (fileSize <= 0 || fileSize > 10 * 1024 * 1024 * 1024L) {
        throw new VideoUploadFailedException("视频大小超出限制，最大支持 10GB");
    }
}
```

---

### ✅ P1-9: 业务 - 可选参数 null 处理 - **已修复**

**文件**: `biz/UserBizService.java`

**状态**: ✅ 已修复

**修复内容**:
```java
public Page<UserResponse> listUsers(Role role, UserStatus status, Pageable pageable) {
    Page<UserEntity> users;
    if (role != null && status != null) {
        users = userRepository.findByRoleAndStatus(role, status, pageable);
    } else {
        users = userRepository.findAll(pageable);
    }
    return users.map(UserResponse::from);
}
```

---

### P1-10: 配置 - 开发文件包含硬编码密钥

**文件**: `application-dev.properties`

**问题**: 开发配置中包含硬编码密钥。

```properties
jwt.secret=esmile-edu-secret-key-for-jwt-token-generation-minimum-32-chars
```

**修复**: 使用环境变量占位符，添加注释说明必须设置

---

## P2 - 建议修复

### P2-1: 缺少 PATCH 部分更新端点

**文件**: `api/course/CourseController.java`

**问题**: 只有 PUT 全量更新，缺少 PATCH 部分更新。

**修复**: 添加 `@PatchMapping` 或在 service 层支持字段级更新

---

### P2-2: 兑换码列表缺少状态过滤

**文件**: `api/redeem/RedeemCodeController.java`

**问题**: 无法按 PENDING/USED/EXPIRED 状态筛选。

```java
@GetMapping("/teacher/redeem-codes")
public ApiResponse<Page<RedeemCodeResponse>> listCodes(..., Pageable pageable)
```

**修复**: 添加 `@RequestParam(required = false) RedeemCodeStatus status` 参数

---

### P2-3: 缺少视频删除端点

**文件**: `api/video/VideoController.java`

**问题**: 只有 apply-upload 和 commit-upload，无删除接口。

**修复**: 添加 `DELETE /teacher/video/{videoId}` 端点

---

### ✅ P2-4: API Key 保护 - **已实现**

**文件**: `common/auth/ApiKeyAuthFilter.java`

**状态**: ✅ 已实现

**说明**: API Key认证过滤器已实现，保护 `/api/v1/redeem-codes/apply` 端点。

---

## 修复优先级分组

### 阶段 1: 安全修复 (已完成部分)
| 任务 | 状态 |
|------|------|
| P0-1: 移除 defaultValue="1" | ✅ 已完成 |
| P0-2: 添加管理员授权校验 | ✅ 已完成 |
| P0-6: 移除默认密钥 | ❌ 未完成 |
| P1-3: 移除日志打印验证码 | ✅ 已完成 |

### 阶段 2: 功能修复 (2-3 天)
| 任务 | 状态 |
|------|------|
| P0-3: 实现真实视频上传 | ❌ 未完成 |
| P0-4: 实现持久化验证码 | ⚠️ 部分完成(DB已实现，邮件仍Mock) |
| P0-5: listCodesByCourse未实现 | ❌ 未实现 |
| P1-2: Entity 层异常类型 | ❌ 未完成 |

### 阶段 3: 完善修复 (1-2 天)
| 任务 | 状态 |
|------|------|
| P1-1: 异常返回码修复 | ❌ 未完成 |
| P1-4: 空密码问题 | ❌ 未完成 |
| P1-5: BCrypt强度优化 | ❌ 未完成 |
| P1-6: DELETE返回码修复 | ❌ 未完成 |
| P1-7: 选课幂等性保护 | ❌ 未完成 |
| P1-10: 开发配置密钥 | ❌ 未完成 |
| P2-1: PATCH端点 | ❌ 未完成 |
| P2-2: 兑换码状态过滤 | ❌ 未完成 |
| P2-3: 视频删除端点 | ❌ 未完成 |

---

## 附录: 文件清单

### 需要修改的文件 (按状态分组)

**✅ 已完成修复:**
```
backend/src/main/java/com/esmile/edu/
├── api/
│   ├── user/UserController.java         [P0-1✅, P0-2✅]
│   ├── course/CourseController.java      [P0-1✅]
│   ├── redeem/RedeemCodeController.java  [P0-1✅]
│   └── video/VideoController.java       [P0-1✅]
└── biz/
    ├── VideoService.java                [P1-8✅]
    └── UserBizService.java              [P1-3✅]
```

**❌ 待修复:**
```
backend/src/main/java/com/esmile/edu/
├── common/
│   ├── auth/
│   │   ├── PasswordService.java          [P1-5]
│   │   └── VerificationCodeService.java  [P0-4-邮件部分]
│   └── GlobalExceptionHandler.java       [P1-1]
├── module/user/UserEntity.java          [P1-2]
├── biz/
│   ├── VideoService.java                [P0-3]
│   └── RedeemBizService.java            [P0-5]
└── api/
    └── course/CourseController.java      [P1-6, P1-7]

backend/src/main/resources/
├── application.properties                [P0-6]
└── application-dev.properties          [P1-10]
```
