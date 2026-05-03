# esmile-edu 项目规范

## 1. 项目概述

面向大学生的专业课程和技能培训教育平台。

**当前阶段**: MVP 开发中

**技术栈**:
- 前端：Vue 3 + Vite + shadcn/ui
- 后端：Spring Boot 3.x (Java 25) + Spring Data JPA
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

### 3.1 技术版本

| 组件 | 版本 |
|------|------|
| Java | 25 LTS |
| Spring Boot | 3.3.x |
| PostgreSQL | 15+ |
| JPA/Hibernate | 6.x |

**重要**: Spring Boot 3.x 使用 `jakarta.*` 命名空间（原 `javax.*` 已废弃）

---

### 3.2 模块结构

```
src/backend/
├── esmile-edu-common/              # 通用模块
│   └── com/esmile/edu/common/
│       ├── config/                 # 配置类
│       ├── exception/              # 异常定义
│       ├── response/               # 统一响应
│       └── util/                   # 工具类
├── esmile-edu-user/                # 用户模块
│   └── com/esmile/edu/user/
│       ├── domain/                # 领域层
│       │   ├── model/             # 实体、值对象
│       │   ├── repository/        # 仓储接口
│       │   └── service/           # 领域服务
│       ├── application/           # 应用层
│       │   ├── dto/              # 数据传输对象
│       │   ├── service/          # 应用服务
│       │   └── port/             # 端口接口
│       ├── infrastructure/        # 基础设施层
│       │   └── persistence/       # 持久化适配器
│       └── api/                   # REST 接口（含用户端、管理端）
│           ├── UserController     # 用户端
│           └── AdminController   # 管理端
├── esmile-edu-course/              # 课程模块
└── esmile-edu-redeem/              # 兑换码模块
```

**包名规范**:
- 使用 `module.user` 而非 `module-user`（Java 包名禁止使用连字符）
- 所有 REST 控制器放在 `api` 包下，按端点类型拆分为 `XxxController` 和 `XxxAdminController`

---

### 3.3 API 响应格式

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

### 3.4 异常处理

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

### 3.5 JPA 实体规范

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

### 3.6 DTO 规范

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

### 3.7 REST API 规范

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

### 3.8 DDD 设计原则

#### 3.8.1 依赖规则

**核心原则**: 源代码依赖只能指向内部层级，外层不知道内层存在。

```
        ┌─────────────────────────┐
        │   API (Controllers)    │  ← 依赖 Application
        └────────────┬────────────┘
                     │
        ┌────────────▼────────────┐
        │      Application        │  ← 依赖 Domain
        │    (Use Cases/Services) │
        └────────────┬────────────┘
                     │
        ┌────────────▼────────────┐
        │        Domain           │  ← 无依赖（核心）
        │  (Entities, Value Objs) │
        └─────────────────────────┘

        Infrastructure (Adapters) ──► Domain Ports (接口)
        （外层，可依赖内层，内层无感知）
```

**强制规则**:
- Domain 层不得导入任何 Spring、Jakarta、Infrastructure 包的类
- Repository 接口定义在 Domain 层，实现在 Infrastructure 层
- Application 层协调领域对象，不包含业务逻辑

#### 3.8.2 端口与适配器

**端口类型**:
- **Driving Port（主端口）**: 用例接口，定义在 Application 层
- **Driven Port（从端口）**: 基础设施接口，定义在 Domain 层

```java
// Domain 层 - Driven Port（基础设施接口）
public interface RedeemCodeRepository {
    RedeemCode findByCode(String code);
    void save(RedeemCode redeemCode);
}

// Infrastructure 层 - Adapter（实现）
@Repository
public class JpaRedeemCodeRepository implements RedeemCodeRepository {
    // JPA implementation
}

// Application 层 - Driving Port（用例接口）
public interface GenerateRedeemCodeUseCase {
    RedeemCode generate(Long courseId, LocalDateTime expiresAt);
}
```

**适配器注册**: 通过 Spring `@Configuration` 或 `@Primary` 进行绑定

#### 3.8.3 防腐层（ACL）

**设计原则**: 防腐层用于隔离外部系统概念与领域概念，仅在语义冲突时使用。

**触发条件**（满足任一条件）:
1. 外部 API 语义与领域概念严重不匹配
2. 外部 API 即将变更，需要隔离影响
3. 需要将多个外部调用组合为单一领域操作

**不必要场景**:
- 简单的一对一映射（如 UserRepository.findById）
- 稳定的外部 API（如腾讯云 VOD SDK）

```java
// ACL 示例：外部模型 → 领域对象
@Service
public class TencentVodAcl {
    public Video toDomain(TencentVodResponse response) {
        return new Video(
            VideoId.from(response.getFileId()),
            new VideoUrl(response.getUrl()),
            Duration.ofSeconds(response.getDuration())
        );
    }
}
```

#### 3.8.4 聚合边界

**规则**: 聚合是领域对象的一致性边界。聚合内对象共同维护业务不变式，跨聚合引用使用 ID。

```java
// 聚合根示例
@Entity
@Table(name = "courses")
public class Course extends BaseEntity {
    @Id
    private Long id;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private List<Chapter> chapters;

    // 聚合根控制其内部对象
    public void addChapter(Chapter chapter) {
        chapters.add(chapter);
        chapter.setCourse(this);
    }
}

// 跨聚合引用 - 使用 ID 而非对象引用
@Entity
@Table(name = "enrollments")
public class Enrollment extends BaseEntity {
    @Column(name = "user_id")
    private Long userId;  // ← 使用 ID，引用 User 聚合

    @Column(name = "course_id")
    private Long courseId;  // ← 使用 ID，引用 Course 聚合
}
```

#### 3.8.5 领域事件

**规则**: 领域事件表示发生在领域中的事实，用于跨聚合或跨模块通信。

```java
// 领域事件 - 定义在 Domain 层
public record RedeemCodeGeneratedEvent(
    RedeemCodeId redeemCodeId,
    CourseId courseId,
    UserId redeemerId
) {}

// Application 层 - 发布事件
@Service
public class RedeemApplicationService {
    public void redeem(String code) {
        // ... 业务逻辑
        eventPublisher.publish(new RedeemCodeGeneratedEvent(...));
    }
}
```

#### 3.8.6 值对象

**规则**: 值对象不可变，按值比较，用于描述领域的无标识概念。

```java
// 值对象示例
public record Email(String value) {
    public Email {
        if (value == null || !value.contains("@")) {
            throw new IllegalArgumentException("Invalid email");
        }
    }
}

// 在实体中使用 @Embedded
@Embeddable
public class EmailAttribute {
    @Column(name = "email")
    private String value;
}
```

#### 3.8.7 限界上下文边界

**规则**: 每个模块（user、course、redeem）是独立的限界上下文。跨上下文通信通过事件或 API，不得直接引用其他上下文的实体。

```
user 模块  ←→  事件/API  ←→  course 模块
   ↓                              ↓
 User 实体                   Course 实体
（独立）                    （独立）
```

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
│   ├── specs/              # PRD 和设计文档
│   └── superpowers/specs/  # 技能文档
├── frontend/               # Vue 3 前端
│   └── src/
│       ├── student/        # 学生端
│       ├── teacher/        # 教师端
│       ├── admin/          # 管理端
│       └── common/         # 公共组件
├── backend/                # Spring Boot 后端
│   ├── esmile-edu-common/
│   ├── esmile-edu-user/
│   ├── esmile-edu-course/
│   └── esmile-edu-redeem/
└── .worktrees/            # 工作树目录（已忽略）
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