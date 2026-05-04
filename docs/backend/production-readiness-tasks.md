# esmile-edu 后端 Production-Ready 任务清单

> 生成日期: 2026-05-04
> 扫描范围: backend/src/main/java/com/esmile/edu/
> 问题总数: P0×6, P1×10, P2×4

---

## 执行摘要

| 优先级 | 问题数 | 说明 |
|---------|--------|------|
| **P0** | 6 | 必须在上线前修复，否则功能不可用或存在安全漏洞 |
| **P1** | 10 | 应在 MVP 发布前修复，影响安全或功能正确性 |
| **P2** | 4 | 建议修复，提升系统健壮性 |

---

## P0 - 必须修复 (上线阻塞)

### P0-1: 安全漏洞 - IDOR (默认用户ID)

**文件**: `api/user/UserController.java`, `api/course/CourseController.java`, `api/redeem/RedeemCodeController.java`, `api/video/VideoController.java`

**问题**: 所有端点使用 `@RequestHeader(value = "X-User-Id", defaultValue = "1")`，攻击者可省略 header 冒充用户 ID 1。

```java
// 错误示例
@RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId

// 修复方案
@RequestHeader("X-User-Id") Long userId  // 移除 defaultValue
```

**修复要求**:
1. 移除所有 `defaultValue = "1"`
2. 在 service 层验证资源所有权
3. 添加请求头必填校验

**影响端点**: ~20 个

---

### P0-2: 安全漏洞 - 缺少管理员授权校验

**文件**: `api/user/UserController.java`

**问题**: `/admin/users/*` 端点无授权检查，任何用户可调用管理员接口。

```java
// 当前 - 无授权检查
@PutMapping("/admin/users/{id}/approve")
public ApiResponse<Void> approveTeacher(@PathVariable Long id) { ... }

@PutMapping("/admin/users/{id}/status")
public ApiResponse<Void> updateUserStatus(...) { ... }
```

**修复要求**:
1. 添加 `@PreAuthorize("hasRole('ADMIN')")` 注解
2. 或在方法开头验证调用者角色为 ADMIN
3. 添加操作审计日志

---

### P0-3: 功能缺陷 - 视频上传是 Mock 实现

**文件**: `biz/VideoService.java`, `api/video/VideoController.java`, `pom.xml`

**问题**:
- `generateVideoId()` 返回 `"mock_" + UUID`
- `generateMockSignature()` 返回假签名
- 上传 URL 硬编码为测试地址
- **pom.xml 中无腾讯云 VOD SDK 依赖**

```java
// 当前 Mock 实现
private String generateVideoId() {
    return "mock_" + UUID.randomUUID().toString();
}
```

**修复要求**:
1. 添加腾讯云 VOD SDK 依赖
2. 实现真实签名生成（使用 SecretKey）
3. 配置 VOD API 地址和凭证
4. 支持视频上传进度回调
5. 删除 `getStoredCode()` 方法（测试方法泄露凭证）

---

### P0-4: 功能缺陷 - 验证码服务是 MVP Stub

**文件**: `common/auth/VerificationCodeService.java`

**问题**:
- 使用内存 `ConcurrentHashMap` 存储，重启丢失
- **无邮件发送实现**，只生成 code
- `getStoredCode()` 方法用于测试，生产必须删除

```java
// 当前 - MVP 实现
private final Map<String, CodeEntry> codeStore = new ConcurrentHashMap<>();

// 测试方法泄露凭证
public String getStoredCode(String email) { ... }
```

**修复要求**:
1. 验证码持久化到数据库或 Redis
2. 集成真实邮件服务（腾讯云邮件/SendGrid/AWS SES）
3. 删除 `getStoredCode()` 方法
4. 添加发送频率限制（防滥用）

---

### P0-5: 功能缺陷 - listCodesByCourse 忽略参数

**文件**: `biz/RedeemBizService.java`

**问题**: `courseId` 参数被忽略，查询所有兑换码而非指定课程的。

```java
// 当前 - Bug
public Page<RedeemCodeResponse> listCodesByCourse(Long courseId, Pageable pageable) {
    return redeemCodeRepository.findAll(pageable)  // courseId 被忽略！
            .map(RedeemCodeResponse::from);
}
```

**修复要求**: 使用 `redeemCodeRepository.findByCourseId(courseId, pageable)`

---

### P0-6: 配置风险 - 硬编码默认密钥

**文件**: `application.properties`

**问题**: 包含默认 JWT Secret 和 API Key。

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

### P1-3: 安全 - 日志打印验证码

**文件**: `biz/UserBizService.java`

**问题**: 验证码被打印到日志，存在安全风险。

```java
log.info("Verification code for {} ({}): {}", email, role, code);
```

**修复**: 使用 `log.debug()` 或完全移除

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

### P1-8: 验证 - 缺少文件类型校验

**文件**: `api/video/VideoController.java`

**问题**: 只校验文件大小，无文件类型检查。

```java
@RequestParam long fileSize  // 无 fileType 校验
```

**修复**: 添加 `fileType` 参数并验证 `video/mp4`, `video/mov` 等

---

### P1-9: 业务 - 可选参数 null 处理

**文件**: `api/user/UserController.java`

**问题**: `role` 或 `status` 为 null 时查询逻辑可能不符合预期。

```java
@RequestParam(required = false) Role role,
@RequestParam(required = false) UserStatus status,
// null 时应如何处理？
```

**修复**: 显式处理 null，仅在参数存在时加入查询条件

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

### P2-4: API Key 保护范围有限

**文件**: `common/auth/ApiKeyAuthFilter.java`

**问题**: 仅保护 `/api/v1/redeem-codes/apply` 一个端点。

**修复**: 评估是否需要扩展保护范围，考虑 IP 白名单

---

## 修复优先级分组

### 阶段 1: 安全修复 (1-2 天)
| 任务 | 负责 |
|------|------|
| P0-1: 移除 defaultValue="1" | |
| P0-2: 添加管理员授权校验 | |
| P0-6: 移除默认密钥 | |
| P1-3: 移除日志打印验证码 | |

### 阶段 2: 功能修复 (2-3 天)
| 任务 | 负责 |
|------|------|
| P0-3: 实现真实视频上传 | |
| P0-4: 实现持久化验证码 | |
| P0-5: 修复 listCodesByCourse Bug | |
| P1-2: Entity 层异常类型 | |

### 阶段 3: 完善修复 (1-2 天)
| 任务 | 负责 |
|------|------|
| P1-1: 异常返回码修复 | |
| P1-4: 空密码问题 | |
| P1-6-9: REST 规范和业务逻辑 | |
| P2-1-4: 增强功能 | |

---

## 附录: 文件清单

### 需要修改的文件
```
backend/src/main/java/com/esmile/edu/
├── common/
│   ├── auth/
│   │   ├── PasswordService.java          [P1-5]
│   │   └── VerificationCodeService.java  [P0-4]
│   └── GlobalExceptionHandler.java       [P1-1]
├── module/user/UserEntity.java          [P1-2]
├── biz/
│   ├── VideoService.java                [P0-3, P1-8]
│   ├── RedeemBizService.java            [P0-5]
│   └── UserBizService.java              [P1-3, P1-4]
└── api/
    ├── user/UserController.java         [P0-1, P0-2, P1-9]
    ├── course/CourseController.java      [P0-1, P1-6, P1-7]
    ├── redeem/RedeemCodeController.java  [P0-1]
    └── video/VideoController.java       [P0-1, P1-8]

backend/src/main/resources/
├── application.properties                [P0-6]
└── application-dev.properties          [P1-10]
```
