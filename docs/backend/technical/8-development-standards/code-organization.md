# 开发规范 - 代码组织

---

## 1. 项目结构

### 1.1 Maven 单模块结构

```
backend/
├── pom.xml
└── src/main/java/com/esmile/edu/
    ├── common/                    # 公共组件
    ├── module/                    # 数据层：实体 + Repository
    ├── biz/                       # 业务层：聚合服务
    ├── api/                      # 接口层：Controller
    └── dto/                      # 数据传输对象
```

---

## 2. 包结构

### 2.1 包命名规范

| 包名 | 说明 |
|------|------|
| `com.esmile.edu.common` | 通用组件 |
| `com.esmile.edu.common.exception` | 异常基类 |
| `com.esmile.edu.common.exception.user` | 用户领域异常 |
| `com.esmile.edu.common.exception.course` | 课程领域异常 |
| `com.esmile.edu.common.exception.redeem` | 兑换码领域异常 |
| `com.esmile.edu.common.exception.video` | 视频领域异常 |
| `com.esmile.edu.common.auth` | 认证服务 |
| `com.esmile.edu.module.user` | 用户模块 |
| `com.esmile.edu.module.course` | 课程模块 |
| `com.esmile.edu.module.redeem` | 兑换码模块 |
| `com.esmile.edu.biz` | 业务服务 |
| `com.esmile.edu.api` | 接口控制器 |
| `com.esmile.edu.dto` | 数据传输对象 |

### 2.2 分层包结构详解

```
com.esmile.edu/
├── common/                          # 公共组件
│   ├── ApiResponse.java             # 统一响应格式
│   ├── BaseEntity.java              # 基础实体（审计字段）
│   ├── GlobalExceptionHandler.java  # 全局异常处理
│   ├── EntityNotFoundException.java # 资源不存在异常
│   ├── exception/                   # 异常体系
│   │   ├── BusinessException.java              # 业务异常基类
│   │   ├── AuthenticationException.java       # 认证异常
│   │   ├── AuthorizationException.java       # 授权异常
│   │   ├── ResourceNotFoundException.java     # 资源不存在异常
│   │   ├── BusinessRuleException.java         # 业务规则异常
│   │   ├── ValidationException.java           # 验证异常
│   │   ├── ExternalServiceException.java      # 外部服务异常
│   │   ├── InvalidTokenException.java        # Token无效异常
│   │   ├── TokenExpiredException.java        # Token过期异常
│   │   ├── InsufficientPermissionsException.java # 权限不足异常
│   │   ├── user/                            # 用户领域异常
│   │   │   ├── UserNotFoundException.java
│   │   │   ├── UserDisabledException.java
│   │   │   ├── UserPendingApprovalException.java
│   │   │   ├── EmailAlreadyExistsException.java
│   │   │   ├── UserIsNotTeacherException.java
│   │   │   ├── CannotModifyAdminStatusException.java
│   │   │   ├── InvalidCredentialsException.java
│   │   │   └── InvalidVerificationCodeException.java
│   │   ├── course/                         # 课程领域异常
│   │   │   ├── CourseNotFoundException.java
│   │   │   ├── CourseNotPublishedException.java
│   │   │   ├── ChapterNotFoundException.java
│   │   │   ├── LessonNotFoundException.java
│   │   │   └── AlreadyEnrolledException.java
│   │   ├── redeem/                        # 兑换码领域异常
│   │   │   ├── RedeemCodeNotFoundException.java
│   │   │   ├── RedeemCodeExpiredException.java
│   │   │   └── RedeemCodeAlreadyUsedException.java
│   │   └── video/                         # 视频领域异常
│   │       ├── VideoUploadFailedException.java
│   │       └── TencentVodException.java
│   └── auth/                              # 认证服务
│       ├── JwtService.java               # JWT token 服务
│       ├── PasswordService.java          # 密码加密服务
│       ├── VerificationCodeService.java   # 验证码服务
│       └── ApiKeyAuthFilter.java         # API Key 认证过滤器
│
├── module/                           # 数据层
│   ├── user/
│   │   ├── Role.java                # 用户角色枚举
│   │   ├── UserStatus.java          # 用户状态枚举
│   │   ├── UserEntity.java          # 用户实体
│   │   └── UserRepository.java      # 用户仓储
│   ├── course/
│   │   ├── CourseStatus.java        # 课程状态枚举
│   │   ├── CourseEntity.java        # 课程实体
│   │   ├── CourseRepository.java     # 课程仓储
│   │   ├── ChapterEntity.java       # 章节实体
│   │   ├── ChapterRepository.java   # 章节仓储
│   │   ├── LessonEntity.java        # 课时实体
│   │   ├── LessonRepository.java     # 课时仓储
│   │   ├── LessonStatus.java        # 课时状态枚举
│   │   ├── EnrollmentEntity.java    # 选课实体
│   │   ├── EnrollmentRepository.java # 选课仓储
│   │   └── EnrollmentStatus.java    # 选课状态枚举
│   └── redeem/
│       ├── RedeemCodeStatus.java    # 兑换码状态枚举
│       ├── RedeemCodeEntity.java    # 兑换码实体
│       └── RedeemCodeRepository.java # 兑换码仓储
│
├── biz/                            # 业务层
│   ├── UserBizService.java         # 用户业务聚合
│   ├── CourseBizService.java       # 课程业务聚合
│   ├── RedeemBizService.java      # 兑换码业务聚合
│   └── VideoService.java           # 视频业务服务
│
├── api/                            # 接口层
│   ├── user/
│   │   └── UserController.java     # 用户接口
│   ├── course/
│   │   └── CourseController.java   # 课程接口
│   ├── redeem/
│   │   └── RedeemCodeController.java # 兑换码接口
│   └── video/
│       └── VideoController.java    # 视频接口
│
└── dto/                            # 数据传输对象
    ├── request/
    │   ├── LoginRequest.java
    │   ├── RegisterRequest.java
    │   ├── SendCodeRequest.java
    │   ├── VerifyCodeRequest.java
    │   ├── CreateCourseRequest.java
    │   ├── CreateChapterRequest.java
    │   ├── CreateLessonRequest.java
    │   ├── UpdateCourseRequest.java
    │   ├── UpdateChapterRequest.java
    │   ├── UpdateLessonRequest.java
    │   ├── GenerateCodesRequest.java
    │   ├── RedeemCodeRequest.java
    │   └── CommitUploadRequest.java
    └── response/
        ├── UserResponse.java
        ├── AuthResponse.java
        ├── CourseResponse.java
        ├── CourseDetailResponse.java
        ├── ChapterResponse.java
        ├── LessonResponse.java
        ├── RedeemCodeResponse.java
        ├── RedeemResultResponse.java
        ├── GenerateCodesResponse.java
        └── VideoUploadSignature.java
```

---

## 3. 各层职责与依赖约束

| 包 | 职责 | 依赖约束 |
|----|------|----------|
| `common/` | 通用组件、异常定义、认证服务 | 无业务依赖 |
| `module/` | 数据模型、基础 Repository（仅 CRUD） | 无业务逻辑 |
| `biz/` | 跨模块业务聚合、事务边界 | 依赖多个 module Repository |
| `dto/` | 请求/响应对象 | 无依赖 |
| `api/` | HTTP 处理、参数校验、路由 | 依赖 biz 服务 |

**依赖方向**: `api → biz → module → common`（严格单向）

---

## 4. 命名规范

### 4.1 类命名

| 类型 | 规范 | 示例 |
|------|------|------|
| 实体 | PascalCase | `User`, `Course` |
| 值对象 | PascalCase | `Email`, `Nickname` |
| 枚举 | PascalCase | `Role`, `UserStatus` |
| 仓储接口 | `XxxRepository` | `UserRepository` |
| 业务服务 | `XxxService` / `XxxBizService` | `UserBizService` |
| Controller | `XxxController` | `UserController` |
| DTO 请求 | `XxxRequest` | `CreateUserRequest` |
| DTO 响应 | `XxxResponse` | `UserResponse` |
| 异常 | `XxxException` | `UserNotFoundException` |

### 4.2 方法命名

| 操作 | 命名 | 示例 |
|------|------|------|
| 查询单个 | `findByXxx` | `findById`, `findByEmail` |
| 查询多个 | `findAllByXxx` | `findAllByRole` |
| 创建 | `create` / `save` | `createUser`, `saveUser` |
| 更新 | `update` | `updateUser` |
| 删除 | `delete` / `remove` | `deleteUser` |
| 业务动作 | 动词 | `enroll`, `redeem`, `approve` |

### 4.3 常量命名

| 类型 | 规范 | 示例 |
|------|------|------|
| 常量 | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT` |
| 枚举值 | UPPER_SNAKE_CASE | `STUDENT`, `PENDING_APPROVAL` |
| 错误码 | UPPER_SNAKE_CASE | `USER_NOT_FOUND` |

---

## 5. 文件大小限制

| 类型 | 推荐行数 | 最大行数 |
|------|----------|----------|
| 类 | 200-400 | 800 |
| 方法 | < 40 | 80 |
| 接口 | < 20 | 50 |

**超限处理**: 拆分为多个类或方法

---

## 6. 约束

| 约束 | 说明 |
|------|------|
| 模块依赖 | 只能依赖下游模块 |
| 循环依赖 | 禁止循环依赖 |
| 类大小 | 单个类不超过 800 行 |
| 方法大小 | 单个方法不超过 80 行 |
| 魔法数字 | 使用命名常量替代 |
