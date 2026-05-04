# 用户模块设计规范

**模块路径**: `module/user/`
**相关层**: `biz/UserBizService`, `api/user/UserController`, `dto/`

---

## 1. Entity

### 1.1 枚举定义

#### Role（角色枚举）

| 枚举值 | 说明 |
|--------|------|
| `STUDENT` | 学生 |
| `TEACHER` | 教师（需审批） |
| `ADMIN` | 管理员 |

#### UserStatus（用户状态枚举）

| 枚举值 | 说明 |
|--------|------|
| `PENDING_APPROVAL` | 待审批（教师注册后） |
| `ACTIVE` | 活跃 |
| `DISABLED` | 已禁用 |

### 1.2 UserEntity

**表名**: `users`

| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | Long | 主键 | 自动生成 |
| email | String | 邮箱 | 非空、唯一 |
| password | String | 密码 | 非空（加密存储） |
| nickname | String | 昵称 | 非空、1-100 字符 |
| avatar | String | 头像 | 可为空 |
| role | Role | 角色 | 非空 |
| status | UserStatus | 状态 | 非空 |
| disabled_at | LocalDateTime | 禁用时间 | 可为空 |
| disable_reason | String | 禁用原因 | 可为空 |
| created_at | LocalDateTime | 创建时间 | 自动设置 |
| updated_at | LocalDateTime | 更新时间 | 自动设置 |

### 1.3 业务规则

| 规则ID | 规则描述 |
|--------|----------|
| USR-001 | 教师注册后状态为 `PENDING_APPROVAL`，需管理员审批 |
| USR-002 | 管理员不可被注册，仅能通过数据库直接创建 |
| USR-003 | 邮箱必须唯一，重复注册应返回错误 |

---

## 2. Repository

### 2.1 UserRepository

**接口**: `JpaRepository<UserEntity, Long>`

| 方法签名 | 说明 |
|----------|------|
| `Optional<UserEntity> findByEmail(String email)` | 根据邮箱查询 |
| `boolean existsByEmail(String email)` | 检查邮箱是否存在 |
| `Page<UserEntity> findByRoleAndStatus(Role role, UserStatus status, Pageable pageable)` | 分页查询 |

---

## 3. Biz 层（聚合服务）

### 3.1 UserBizService

**路径**: `biz/UserBizService.java`

| 方法签名 | 说明 |
|----------|------|
| `UserEntity createStudent(String email, String password, String nickname)` | 注册学生 |
| `UserEntity createTeacher(String email, String password, String nickname)` | 注册教师 |
| `UserEntity authenticate(String email, String password)` | 邮箱密码登录 |
| `UserEntity findById(Long id)` | 根据ID查询 |
| `UserEntity findByEmail(String email)` | 根据邮箱查询 |
| `void approveTeacher(Long userId)` | 审批教师 |
| `void disableUser(Long userId, String reason)` | 禁用用户 |
| `void enableUser(Long userId)` | 启用用户 |
| `Page<UserEntity> listUsers(Role role, UserStatus status, Pageable pageable)` | 分页查询用户 |

---

## 4. API 层

### 4.1 UserController

**路径**: `api/user/UserController.java`

#### StudentAuthController

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/student/auth/register` | 注册学生 |
| POST | `/api/v1/student/auth/login` | 登录 |
| GET | `/api/v1/student/auth/me` | 获取当前用户 |

#### TeacherAuthController

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/teacher/auth/register` | 注册教师 |
| POST | `/api/v1/teacher/auth/login` | 登录 |
| GET | `/api/v1/teacher/auth/me` | 获取当前用户 |

#### AdminUserController

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/admin/users` | 用户列表 |
| PUT | `/api/v1/admin/users/{id}/approve` | 审批教师 |
| PUT | `/api/v1/admin/users/{id}/status` | 更新用户状态 |

---

## 5. DTO 层

### 5.1 Request DTO

#### RegisterRequest

| 字段 | 类型 | 校验 |
|------|------|------|
| email | String | @NotBlank, @Email |
| password | String | @NotBlank, @Size(min=8) |
| nickname | String | @NotBlank, @Size(min=1, max=100) |

#### LoginRequest

| 字段 | 类型 | 校验 |
|------|------|------|
| email | String | @NotBlank, @Email |
| password | String | @NotBlank |

#### UpdateStatusRequest

| 字段 | 类型 | 校验 |
|------|------|------|
| status | UserStatus | @NotNull |
| reason | String | 可为空 |

### 5.2 Response DTO

#### UserResponse

| 字段 | 类型 |
|------|------|
| id | Long |
| email | String |
| nickname | String |
| avatar | String |
| role | Role |
| status | UserStatus |

#### AuthResponse

| 字段 | 类型 |
|------|------|
| token | String |
| expiresIn | long |
| user | UserResponse |

---

## 6. 错误码

| 错误码 | 含义 |
|--------|------|
| 10101 | 用户不存在 |
| 10102 | 用户已禁用 |
| 10103 | 教师待审批 |
| 10104 | 邮箱已注册 |
| 10105 | 用户不是教师 |
| 10106 | 无法修改管理员状态 |
| 10107 | 无效的状态转换 |
| 10108 | 邮箱或密码错误 |

---

## 7. 验证清单

- [ ] UserEntity 字段完整
- [ ] UserRepository 方法签名正确
- [ ] UserBizService 聚合逻辑正确
- [ ] UserController 路径与 API 规范一致
- [ ] DTO 校验注解正确
