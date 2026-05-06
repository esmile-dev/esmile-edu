# 整体架构设计

---

## 1. 项目概述

### 1.1 项目定位

esmile-edu 是一个面向大学生的在线教育平台，连接课程教师和生源。

### 1.2 MVP 功能范围

| 模块 | 功能 |
|------|------|
| 用户系统 | 邮箱注册/登录，学生、教师（需审批）、管理员三种角色 |
| 课程系统 | 教师创建/发布课程，添加章节和课时，上传视频 |
| 兑换码系统 | 生成兑换码，学生兑换课程 |
| 管理员功能 | 审批教师，管理用户和课程，数据统计 |

---

## 2. 技术栈版本

| 组件 | 版本 | 备注 |
|------|------|------|
| Java | 25 LTS | 运行环境 |
| Spring Boot | 3.3.x | Web 框架 |
| Spring Data JPA | 6.x | ORM |
| PostgreSQL | 15+ | 数据库 |
| Java JWT | 0.12.x | Token 认证 |
| Lombok | 1.18.x | 减少样板代码 |

**重要**: Spring Boot 3.x 使用 `jakarta.*` 命名空间

---

## 3. 模块划分与依赖关系

### 3.1 分层包结构

**单模块 + 分层包架构**：

```
backend/src/main/java/com/esmile/edu/
├── common/                          # 公共组件（无业务依赖）
├── module/                           # 数据层：实体 + Repository
│   ├── user/
│   │   ├── UserEntity.java
│   │   └── UserRepository.java
│   ├── course/
│   │   ├── CourseEntity.java
│   │   ├── ChapterEntity.java
│   │   ├── LessonEntity.java
│   │   ├── EnrollmentEntity.java
│   │   └── *Repository.java
│   └── redeem/
│       ├── RedeemCodeEntity.java
│       └── RedeemCodeRepository.java
├── biz/                             # 业务层：聚合服务
│   ├── UserBizService.java          # 用户业务聚合
│   ├── CourseBizService.java        # 课程业务聚合
│   └── RedeemBizService.java        # 兑换码业务聚合
├── dto/                             # 数据传输对象
│   ├── request/                    # 请求 DTO
│   └── response/                   # 响应 DTO
├── api/                             # 接口层：Controller
│   ├── user/
│   │   └── UserController.java
│   ├── course/
│   │   └── CourseController.java
│   └── redeem/
│       └── RedeemCodeController.java
└── config/                          # 全局配置
```

### 3.2 各层职责

| 包 | 职责 | 依赖约束 |
|----|------|----------|
| **module/** | 数据模型、基础 Repository（仅 CRUD） | 无业务逻辑 |
| **biz/** | 跨模块业务聚合、事务边界 | 依赖多个 module Repository |
| **dto/** | 请求/响应对象、复杂聚合对象 | 无依赖 |
| **api/** | HTTP 处理、参数校验、路由 | 依赖 biz 服务 |

### 3.3 依赖规则

**严格单向依赖**：
```
api → biz → module → common
```

**禁止**：
- api 直接调用 module（必须经过 biz）
- biz 之间的循环依赖
- module 之间的交叉引用

---

## 4. 分层详细设计

### 4.1 module 层（数据层）

**职责**：数据模型定义、基础 CRUD Repository

**规范**：
- 每个实体对应一个 Repository
- Repository 仅继承 `JpaRepository`，不做自定义查询
- 实体仅包含数据字段和 JPA 注解，不含业务方法

```
module/{name}/
├── {Name}Entity.java              # 实体
└── {Name}Repository.java         # 继承 JpaRepository
```

### 4.2 biz 层（业务层）

**职责**：
- 跨模块业务聚合（如：兑换码兑换需要聚合 User + Course + RedeemCode）
- 业务规则校验
- 事务边界控制

**规范**：
- 一个业务领域对应一个 BizService
- 聚合本领域内的多个 entity 操作
- 通过 `@Transactional` 控制事务

```
biz/
├── UserBizService.java
│   // 聚合：UserEntity + ProfileEntity → 用户完整信息
│   // 方法：registerStudent(), authenticate(), updateProfile()
│
├── CourseBizService.java
│   // 聚合：CourseEntity + ChapterEntity + LessonEntity
│   // 方法：createCourse(), publishCourse(), enrollCourse()
│
└── RedeemBizService.java
    // 聚合：RedeemCodeEntity + UserEntity + CourseEntity + EnrollmentEntity
    // 方法：generateCodes(), redeemCode()
```

### 4.3 api 层（接口层）

**职责**：
- HTTP 请求处理
- 参数校验
- 响应格式转换
- 路由定义

**规范**：
- Controller 仅做参数校验和响应转换
- 业务逻辑全部下沉到 biz 层

```
api/
├── user/
│   └── UserController.java
│       // POST /api/v1/student/auth/register → biz.registerStudent()
│       // POST /api/v1/student/auth/login → biz.authenticate()
│
├── course/
│   └── CourseController.java
│       // GET /api/v1/courses → biz.listCourses()
│
└── redeem/
    └── RedeemCodeController.java
        // POST /api/v1/redeem → biz.redeemCode()
```

### 4.4 dto 层（数据传输对象）

**职责**：
- 复杂请求对象（需校验）
- 聚合响应对象
- 分页响应对象

```
dto/
├── request/
│   ├── RegisterStudentRequest.java
│   ├── CreateCourseRequest.java
│   └── RedeemCodeRequest.java
│
└── response/
    ├── UserProfileResponse.java
    ├── CourseDetailResponse.java
    ├── RedeemResultResponse.java
    └── PageResponse.java
```

---

## 5. 跨模块业务示例

### 5.1 兑换码兑换流程

**调用链**：
```
RedeemCodeController (api)
    ↓
RedeemBizService (biz)
    ├── UserRepository.findById()
    ├── CourseRepository.findById()
    ├── RedeemCodeRepository.findByCode()
    ├── EnrollmentRepository.save()
    └── RedeemCodeRepository.save()
```

**代码示意**：
```java
// api/redeem/RedeemCodeController.java
@RestController
@RequiredArgsConstructor
public class RedeemCodeController {
    private final RedeemBizService redeemBizService;

    @PostMapping("/api/v1/redeem")
    public ApiResponse<RedeemResultResponse> redeem(@RequestBody RedeemCodeRequest request) {
        RedeemResultResponse result = redeemBizService.redeemCode(
            request.code(),
            request.userId()
        );
        return ApiResponse.ok(result);
    }
}

// biz/RedeemBizService.java
@Service
@RequiredArgsConstructor
public class RedeemBizService {
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final RedeemCodeRepository redeemCodeRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Transactional
    public RedeemResultResponse redeemCode(String code, Long userId) {
        // 1. 校验用户
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(404, "用户不存在"));

        // 2. 校验兑换码
        RedeemCodeEntity redeemCode = redeemCodeRepository.findByCode(code)
            .orElseThrow(() -> new BusinessException(404, "兑换码不存在"));
        redeemCode.validate();

        // 3. 校验课程
        CourseEntity course = courseRepository.findById(redeemCode.getCourseId())
            .orElseThrow(() -> new BusinessException(404, "课程不存在"));

        // 4. 创建选课记录
        EnrollmentEntity enrollment = new EnrollmentEntity(userId, course.getId());
        enrollmentRepository.save(enrollment);

        // 5. 更新兑换码状态
        redeemCode.markAsUsed(userId);
        redeemCodeRepository.save(redeemCode);

        // 6. 返回聚合结果
        return new RedeemResultResponse(course.getTitle(), enrollment.getId());
    }
}
```

---

## 6. 实体边界规则

### 6.1 跨实体引用

**必须使用 ID，禁止使用对象引用**：

```java
// 正确
@Entity
public class EnrollmentEntity {
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "course_id")
    private Long courseId;
}

// 错误
@Entity
public class EnrollmentEntity {
    @ManyToOne
    private UserEntity user;        // 禁止！

    @ManyToOne
    private CourseEntity course;    // 禁止！
}
```

### 6.2 实体关系

| 实体 | 关联实体 | 引用方式 |
|------|---------|----------|
| Course | Chapter, Lesson | 同一模块内可用 @OneToMany |
| Enrollment | - | 使用 userId, courseId (Long) |
| RedeemCode | - | 使用 courseId, usedBy (Long) |

---

## 7. 架构决策记录

### 7.1 为什么选择分层包架构？

| 考量 | 决策 |
|------|------|
| MVP 阶段 | 快速迭代优先，单 Maven 模块降低复杂度 |
| 三人团队 | api/biz/module 分层清晰，职责明确 |
| 演进路线 | 可平滑升级到多模块架构 |

### 7.2 为什么选择 JPA/Hibernate？

| 考量 | 决策 |
|------|------|
| Spring 集成 | Spring Data JPA 提供良好的 Spring 集成 |
| 快速开发 | 减少样板代码，无需手写 SQL |
| PostgreSQL | Hibernate 对 PostgreSQL 支持良好 |

### 7.3 为什么使用腾讯云 VOD？

| 考量 | 决策 |
|------|------|
| 视频处理 | 需要转码、截图等视频处理能力 |
| CDN 分发 | 需要全球 CDN 分发 |
| SDK 支持 | 提供完善的 Java SDK |

---

## 8. 关键文件索引

| 文件 | 说明 |
|------|------|
| [2-modules/user-module.md](../2-modules/user-module.md) | 用户模块设计规范 |
| [2-modules/course-module.md](../2-modules/course-module.md) | 课程模块设计规范 |
| [2-modules/redeem-module.md](../2-modules/redeem-module.md) | 兑换码模块设计规范 |
| [3-data-model/entities.md](../3-data-model/entities.md) | 实体设计规范 |
| [5-exception-handling/global-exception-handler.md](../5-exception-handling/global-exception-handler.md) | 异常处理规范 |
