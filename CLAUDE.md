# esmile-edu 项目规范

## 1. 项目概述

面向大学生的专业课程和技能培训教育平台。

**当前阶段**: MVP 开发中

**技术栈**:
- 前端：Vue 3 + Vite + shadcn/ui
- 后端：Spring Boot 3.x (Java 21) + Spring Data JPA
- 数据库：PostgreSQL
- 视频托管：腾讯云 VOD

---

## 2. Rules

### 2.1 What NOT to Do

- ❌ 禁止 `git add -A` 或 `git add .`（必须先确认文件列表）
- ❌ 禁止提交敏感文件（`.claude/`、`toolkit/`、`node_modules/`、`.env`）
- ❌ 禁止 `git push --force` 到 main/master 分支
- ❌ 禁止硬编码凭据、密钥到源代码
- ❌ 禁止提交生成的临时文件

### 2.2 Code Style

**通用规则**:
- 使用有明确含义的变量名，避免缩写
- 禁止魔法数字，使用命名常量
- 函数不超过 ~40 行，超出则拆分
- 删除死代码，不要注释掉

**Java**:
- 使用 Lombok 减少样板代码
- 优先使用 immutable 对象（final 字段、record）
- Stream API over 循环

**TypeScript/Vue**:
- `const` over `let`，禁止 `var`
- 箭头函数用于回调
- Async/await over `.then()` 链

### 2.3 Testing

**规则**:
- 每个 bug 修复需要测试用例
- 测试行为而非实现细节
- 测试名称描述场景：`it('returns null when user not found')`

**测试文件位置**:
- 单元测试放在源码旁边：`UserService.java` → `UserServiceTest.java`
- 集成测试在 `tests/integration/`

---

## 3. Git 工作流

### 分支策略

```
main        → 主分支，稳定可部署
dev         → 开发分支，所有功能先合入 dev
feature/*   → 功能分支（从 dev 创建）
fix/*       → 修复分支（从 dev 创建）
```

### 分支流程

```
feature/xxx → dev → main
fix/xxx     → dev → main
```

**注意**: 禁止直接从 feature/fix 分支合并到 main，必须经过 dev

### 提交规范

遵循 [conventional commits](https://www.conventionalcommits.org/):

```
<type>: <description>

Types: feat, fix, refactor, docs, test, chore, perf, ci
```

### 工作流程

1. **开始工作前**: 使用 `using-git-worktrees` skill 创建隔离工作区
2. **创建功能分支**: 从 dev 创建 `feature/xxx` 或 `fix/xxx`

### 安全检查清单

执行 `git add` 前必须：
1. 先执行 `git status --short` 查看变更文件
2. 确认没有敏感文件（.claude/、.env、凭据等）
3. 使用 `git add <specific-files>` 而非全量 add

---

## 4. 后端规范 (Spring Boot 3.x)

### 4.1 技术版本

| 组件 | 版本 |
|------|------|
| Java | 25 LTS |
| Spring Boot | 3.3.x |
| PostgreSQL | 15+ |
| JPA/Hibernate | 6.x |

> ⚠️ **注意**: 项目使用单体分层架构（非多模块），所有代码在 `backend/src/main/java/com/esmile/edu/` 下

**重要**: Spring Boot 3.x 使用 `jakarta.*` 命名空间（原 `javax.*` 已废弃）

---

### 4.2 分层包结构

**单模块 + 分层包架构**：

```
backend/src/main/java/com/esmile/edu/
├── common/                          # 公共组件
│   ├── auth/                        # 认证授权（JwtService, PasswordService, AuthContext等）
│   ├── config/                      # 配置类
│   ├── email/                       # 邮件服务
│   ├── exception/                   # 异常定义（含子包user/course/redeem/video）
│   └── GlobalExceptionHandler.java   # 全局异常处理
├── module/                          # 数据层：实体 + Repository
│   ├── user/
│   │   ├── UserEntity.java
│   │   ├── UserRepository.java
│   │   ├── Role.java
│   │   └── UserStatus.java
│   ├── course/
│   │   ├── CourseEntity.java
│   │   ├── ChapterEntity.java
│   │   ├── LessonEntity.java
│   │   ├── EnrollmentEntity.java
│   │   └── *Repository.java
│   ├── redeem/
│   │   ├── RedeemCodeEntity.java
│   │   └── RedeemCodeRepository.java
│   └── auth/
│       ├── VerificationCodeEntity.java
│       └── VerificationCodeRepository.java
├── biz/                            # 业务层：聚合服务
│   ├── UserBizService.java
│   ├── CourseBizService.java
│   ├── RedeemBizService.java
│   └── VideoService.java
├── dto/                            # 数据传输对象
│   ├── request/
│   └── response/
├── api/                            # 接口层：Controller
│   ├── user/
│   │   └── UserController.java
│   ├── course/
│   │   └── CourseController.java
│   ├── redeem/
│   │   └── RedeemCodeController.java
│   └── video/
│       └── VideoController.java
└── EspmileEduApplication.java       # 启动类
```

**各层职责**：
| 包 | 职责 | 依赖约束 |
|----|------|----------|
| **module/** | 数据模型、基础 Repository | 无业务逻辑 |
| **biz/** | 跨模块业务聚合、事务边界 | 依赖多个 module Repository |
| **dto/** | 请求/响应对象 | 无依赖 |
| **api/** | HTTP 处理、参数校验 | 依赖 biz 服务 |

**依赖方向**：`api → biz → module → common`（严格单向）

---

### 4.3 API 响应格式

```java
public record ApiResponse<T>(
    int code,
    String message,
    T data
) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(200, "Success", data);
    }

    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(201, "Created", data);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
```

**响应头** (通过 Filter 全局设置):
- `X-Request-Id`: 请求追踪 ID
- `X-Response-Time`: 响应时间

---

### 4.4 异常处理

**异常基类**:
```java
public abstract class BusinessException extends RuntimeException {
    private final int code;
    public BusinessException(int code, String message) { ... }
    public int getCode() { return code; }
}
```

**全局处理器**:
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException ex) {
        // 收集字段错误，返回 400
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ApiResponse<Void> handleNotFound(EntityNotFoundException ex) {
        return ApiResponse.error(404, ex.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusiness(BusinessException ex) {
        return ApiResponse.error(ex.getCode(), ex.getMessage());
    }
}
```

---

### 4.5 JPA 实体规范

**基础实体**:
```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

**实体示例**:
```java
@Entity
@Table(name = "users")
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
}
```

**注意**:
- 使用 `@Table` 显式指定表名
- 优先使用 `@GeneratedValue(strategy = GenerationType.IDENTITY)` for PostgreSQL
- 不要在实体字段中使用 `Optional<>`（JPA 不支持）

---

### 4.6 DTO 规范

**使用 Java Record** (Java 17+):
```java
public record CreateUserRequest(
    @NotBlank @Email String email,
    @NotBlank @Size(min = 8) String password,
    String nickname
) {}

public record UserResponse(Long id, String email, String nickname, Role role) {}
```

**验证注解**:
- `@NotNull` - 非空（用于必须字段）
- `@NotBlank` - 非空字符串
- `@Email` - 邮箱格式
- `@Size(min, max)` - 长度限制
- `@Min` / `@Max` - 数值范围

---

### 4.7 REST API 规范

**URL 规范**:
- 使用名词复数: `/api/v1/users`, `/api/v1/courses`
- 嵌套资源: `/api/v1/courses/{courseId}/chapters`
- 不使用动词: `/api/v1/users` 而非 `/api/v1/getUsers`

**管理端 API 前缀**: `/api/v1/admin/xxx`

**HTTP 方法**:
| 方法 | 用途 | 响应码 |
|------|------|--------|
| GET | 查询 | 200 |
| POST | 创建 | 201 |
| PUT | 全量更新 | 200 |
| PATCH | 部分更新 | 200 |
| DELETE | 删除 | 204 |

**版本控制**: URL 路径 `/api/v1/`

---

### 4.8 分层包架构

#### 4.8.1 分层职责

| 包 | 职责 | 依赖约束 |
|----|------|----------|
| **module/** | 数据模型、基础 Repository（仅 CRUD） | 无业务逻辑 |
| **biz/** | 跨模块业务聚合、事务边界 | 依赖多个 module Repository |
| **dto/** | 请求/响应对象、复杂聚合对象 | 无依赖 |
| **api/** | HTTP 处理、参数校验、路由 | 依赖 biz 服务 |

#### 4.8.2 依赖方向

```
api → biz → module → common
```

**严格单向依赖，禁止反向调用**。

#### 4.8.3 module 层规范

```
module/{name}/
├── {Name}Entity.java              # 实体（JPA 注解）
└── {Name}Repository.java         # 继承 JpaRepository
```

**规范**：
- 仅包含数据字段和 JPA 注解
- Repository 仅继承 `JpaRepository`
- 不含业务方法

#### 4.8.4 biz 层规范

```
biz/
├── UserBizService.java            # 用户业务聚合
├── CourseBizService.java         # 课程业务聚合
└── RedeemBizService.java        # 兑换码业务聚合
```

**规范**：
- 聚合多个 module 的 Repository
- 处理跨模块业务逻辑
- 通过 `@Transactional` 控制事务

**示例**：
```java
@Service
public class RedeemBizService {
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final RedeemCodeRepository redeemCodeRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Transactional
    public RedeemResultResponse redeemCode(String code, Long userId) {
        // 聚合 user + course + redeemCode + enrollment
    }
}
```

#### 4.8.5 api 层规范

```
api/
├── user/
│   └── UserController.java
├── course/
│   └── CourseController.java
└── redeem/
    └── RedeemCodeController.java
```

**规范**：
- Controller 仅做参数校验和响应转换
- 业务逻辑全部下沉到 biz 层

#### 4.8.6 实体边界规则

**跨实体引用必须使用 ID，禁止使用对象引用**：

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
}
```

#### 4.8.7 演进策略

| 阶段 | 触发条件 | 动作 |
|------|----------|------|
| **Phase 1: MVP** | 当前 | 单模块 + 分层包 |
| **Phase 2: 增长期** | PMF 验证，团队扩展 | 抽取 biz 层为独立模块 |
| **Phase 3: 规模化** | 团队 > 5 人 | 完整 DDD 分层 + 事件驱动 |

---

## 5. 前端规范 (Vue 3)

### 5.1 项目结构

```
frontend/src/
├── student/                # 学生端
│   ├── views/
│   ├── components/
│   ├── api/
│   └── router/
├── teacher/               # 教师端
│   ├── views/
│   ├── components/
│   ├── api/
│   └── router/
├── admin/                 # 管理端
│   ├── views/
│   ├── components/
│   ├── api/
│   └── router/
└── common/               # 公共组件
```

### 5.2 shadcn/ui 使用规范

- 组件存放于 `@/common/components/ui/`
- 使用 `cn()` 工具类合并 class
- 遵循 shadcn 设计规范

---

## 6. 开发流程

### 5.1 标准流程

```
1. 需求分析
   └── PRD 写入 docs/specs/

2. 设计阶段
   ├── 使用 brainstorming skill 细化设计
   └── 设计文档写入 docs/superpowers/specs/

3. 实施计划
   ├── 使用 writing-plans skill 创建计划
   └── 任务分解后开始开发

4. 开发阶段
   ├── 从 dev 创建 feature/xxx 分支
   ├── 使用 TDD 开发（superpowers:test-driven-development）
   ├── 使用 requesting-code-review 进行代码审查
   └── 审查通过后合并到 dev

5. 发布阶段
   ├── 将 dev 合并到 main
   └── 打 tag 发布
```

### 5.2 代码审查

- 每个任务完成后必须审查
- 审查结果：Critical → 立即修复，Important → 修复后继续，Minor → 记录

---

## 7. 目录结构

```
esmile-edu/
├── docs/
│   ├── specs/                  # PRD 和设计文档
│   ├── backend/technical/      # 后端技术文档
│   ├── frontend/technical/     # 前端技术文档
│   ├── ux-ui/                 # UX/UI设计文档
│   └── production-readiness-tasks.md  # Production-Ready任务清单
├── frontend/                   # Vue 3 前端
│   └── src/
│       ├── student/           # 学生端
│       ├── teacher/           # 教师端
│       ├── admin/             # 管理端
│       └── common/            # 公共组件
├── backend/                   # Spring Boot 后端 (单体分层架构)
│   └── src/main/java/com/esmile/edu/
│       ├── api/               # Controller层
│       ├── biz/               # 业务聚合层
│       ├── module/            # 数据模型层
│       ├── dto/               # 数据传输对象
│       └── common/            # 公共组件
└── .worktrees/               # 工作树目录（已忽略）
```

---

## 8. 环境配置

### 必需环境变量

**后端**:
```env
SPRING_DATASOURCE_URL=       # PostgreSQL 连接
SPRING_DATASOURCE_USERNAME=  # 数据库用户名
SPRING_DATASOURCE_PASSWORD=  # 数据库密码
TENCENT_VOD_SECRET_ID=       # 腾讯云 VOD
TENCENT_VOD_SECRET_KEY=      # 腾讯云 VOD
```

**前端**:
```env
VITE_API_BASE_URL=           # API 基础路径
```

---

## 9. 质量标准

- 测试覆盖率 > 80%
- 所有 Critical/Important 问题必须在合并前修复
- API 响应时间 < 500ms

---

## 10. 参考 Skills

- `superpowers:using-git-worktrees` - 创建隔离工作区
- `superpowers:finishing-a-development-branch` - 结束开发分支
- `superpowers:requesting-code-review` - 请求代码审查
- `superpowers:receiving-code-review` - 接收代码审查反馈
- `superpowers:test-driven-development` - TDD 开发方法