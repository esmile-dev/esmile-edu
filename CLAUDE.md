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

## 2. Git 工作流

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
3. **开发中**: 使用 TDD 方法，参考 `superpowers:test-driven-development`
4. **每个任务完成后**: 使用 `requesting-code-review` skill 进行代码审查
5. **合并到 dev**: 代码审查通过后，合并到 dev 分支
6. **发布时**: 将 dev 合并到 main

---

## 3. 后端规范 (Spring Boot 3.x)

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

## 4. 前端规范 (Vue 3)

### 4.1 项目结构

```
src/frontend/src/
├── student/                # 学生端视图
│   ├── views/
│   ├── components/
│   └── router/
├── educator/               # 教育者端视图
│   ├── views/
│   ├── components/
│   └── router/
├── common/                 # 公共组件
├── api/                    # API 调用层
├── assets/                 # 静态资源
└── router/                 # 路由配置
```

### 4.2 shadcn/ui 使用规范

- 组件存放于 `@/common/components/ui/`
- 使用 `cn()` 工具类合并 class
- 遵循 shadcn 设计规范

---

## 5. 开发流程

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

## 6. 目录结构

```
esmile-edu/
├── docs/
│   ├── specs/              # PRD 和设计文档
│   └── superpowers/specs/  # 技能文档
├── src/
│   ├── frontend/          # Vue 3 前端
│   │   └── src/
│   │       ├── student/    # 学生端
│   │       ├── educator/   # 教育者端
│   │       ├── common/    # 公共组件
│   │       ├── api/       # API 调用
│   │       └── router/    # 路由
│   └── backend/           # Spring Boot 后端
│       ├── esmile-edu-common/
│       ├── esmile-edu-user/
│       ├── esmile-edu-course/
│       └── esmile-edu-redeem/
└── .worktrees/            # 工作树目录（已忽略）
```

---

## 7. 环境配置

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

## 8. 质量标准

- 测试覆盖率 > 80%
- 所有 Critical/Important 问题必须在合并前修复
- API 响应时间 < 500ms

---

## 9. 参考 Skills

- `superpowers:using-git-worktrees` - 创建隔离工作区
- `superpowers:finishing-a-development-branch` - 结束开发分支
- `superpowers:requesting-code-review` - 请求代码审查
- `superpowers:receiving-code-review` - 接收代码审查反馈
- `superpowers:test-driven-development` - TDD 开发方法