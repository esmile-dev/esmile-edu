# esmile 教育平台 - MVP PRD

**日期**: 2026-05-03
**状态**: 需求更新（统一后台 + RBAC）
**版本**: v2.0

---

## 1. 项目概述

**定位**: 面向大学生的专业课程和技能培训教育平台，连接个人教育者和生源。

**MVP 目标**: 1个月内上线，验证核心业务流程。

**团队**: 2开发 + 1测试

**重要变更 (v2.0)**: 教师端与管理端合并为统一后台管理系统，通过 RBAC 权限控制菜单和功能展示。

---

## 2. 功能范围

### 2.1 用户系统

| 功能 | 说明 |
|------|------|
| 邮箱注册/登录 | 用户通过邮箱和验证码注册和登录 |
| 角色 | 学生（独立前端）、教师（需审批）、管理员 |
| 微信登录 | 后期接入（MVP 排除） |

### 2.2 学生端

| 功能 | 说明 |
|------|------|
| 注册/登录 | 用邮箱和验证码登录 |
| 兑换课程 | 输入兑换码，开通课程学习权限 |
| 看视频 | 观看已购买课程的视频 |

### 2.3 统一后台管理系统

**说明**: 教师端和管理端合并为统一后台，通过 RBAC 权限控制菜单和功能。

#### 2.3.1 权限模型

**角色定义**:

| 角色 | 说明 | 权限范围 |
|------|------|----------|
| STUDENT | 学生端（独立系统，不参与合并） | - |
| TEACHER | 教师 | 课程管理、兑换码管理（仅自己的） |
| ADMIN | 管理员 | 全部权限 |

**权限定义**:

| 权限代码 | 说明 | TEACHER | ADMIN |
|----------|------|---------|-------|
| `user:view` | 查看用户列表 | - | ✓ |
| `user:approve` | 审批教师注册 | - | ✓ |
| `user:update` | 修改用户状态 | - | ✓ |
| `course:view` | 查看课程（自己/全部） | 自己 | 全部 |
| `course:create` | 创建课程 | ✓ | ✓ |
| `course:update` | 更新课程 | 自己 | 全部 |
| `course:delete` | 删除课程 | 自己 | 全部 |
| `course:audit` | 审核课程 | - | ✓ |
| `chapter:*` | 章节管理 | 自己课程 | 全部 |
| `lesson:*` | 课时管理 | 自己课程 | 全部 |
| `code:view` | 查看兑换码 | 自己 | 全部 |
| `code:create` | 生成兑换码 | ✓ | ✓ |
| `stats:view` | 数据统计 | - | ✓ |
| `system:config` | 系统配置 | - | ✓ |

**权限控制层级**:

1. **菜单级**: 根据用户角色动态渲染菜单项
2. **按钮级**: `v-permission` 指令控制操作按钮显示
3. **API 级**: 后端 Spring Security `@PreAuthorize` 注解鉴权

#### 2.3.2 页面结构

统一入口: `/admin/*`

| 路径 | 功能 | 权限 |
|------|------|------|
| `/admin/login` | 管理员/教师登录 | 公开 |
| `/admin/dashboard` | 数据概览 | ADMIN |
| `/admin/profile` | 个人中心 | 登录用户 |
| `/admin/users` | 用户管理 | ADMIN |
| `/admin/users/[id]` | 用户详情 | ADMIN |
| `/admin/courses` | 课程管理（全部） | ADMIN |
| `/admin/courses/[id]` | 课程详情 | ADMIN |
| `/admin/my-courses` | 我的课程 | TEACHER, ADMIN |
| `/admin/my-courses/new` | 创建课程 | TEACHER, ADMIN |
| `/admin/my-courses/[id]/edit` | 编辑课程 | TEACHER（自己）, ADMIN |
| `/admin/my-codes` | 我的兑换码 | TEACHER, ADMIN |
| `/admin/all-codes` | 全部兑换码 | ADMIN |

---

## 3. 兑换码系统

### 状态定义

| 状态 | 说明 |
|------|------|
| 待兑换 (PENDING) | 生成后等待兑换 |
| 已兑换 (REDEEMED) | 已被用户使用 |
| 已失效 (EXPIRED) | 超过有效期未兑换或关联课程被删除 |

### 状态流转

```
[生成] → 待兑换 → 已兑换
              ↘ 已失效
```

### 业务规则

| 规则 | 说明 |
|------|------|
| 生成方式 | 外部系统调用 API 接口生成 |
| 格式 | 8位字母数字（如 `A1B2C3D4`） |
| 兑换码有效期 | 调用方指定（如30天），必须在此之前兑换 |
| 使用次数 | 一次性，兑换后失效 |
| 绑定对象 | 绑定到用户账号 |

### 3.1 闲鱼集成

| 规则 | 说明 |
|------|------|
| 商品形式 | 固定价格（不支持议价） |
| 发货方式 | MVP 手动发货 |
| 自动发货 | 后期接入 OpenClaw（MVP 排除） |

---

## 4. 技术方案

### 4.1 技术栈

| 层级 | 技术 | 备注 |
|------|------|------|
| 后端 | Spring Boot 3.x + Spring Security 6.x | Java 25 |
| 数据库 | PostgreSQL | 自建在 47.107.163.188 |
| ORM | Spring Data JPA | 数据库操作 |
| 前端 | Vue 3 + Vite + shadcn/ui | 参考 shadcn-vue-admin |
| 状态管理 | Pinia + pinia-plugin-persistedstate | 权限状态持久化 |
| 视频托管 | 腾讯云 VOD | 含自动转码 |
| 视频防盗 | Referer 防盗链 | 免费、简单 |

### 4.2 项目结构

```
esmile-edu/
├── docs/specs/                       # 需求文档
├── frontend/                         # Vue 3 前端
│   └── src/
│       ├── admin/                    # 统一后台管理系统
│       │   ├── api/                  # API 定义
│       │   ├── components/           # 组件
│       │   │   ├── common/          # 通用组件
│       │   │   │   └── PermissionWrapper.vue  # 权限包装器
│       │   │   ├── layout/           # 布局组件
│       │   │   │   ├── AdminLayout.vue
│       │   │   │   ├── Sidebar.vue
│       │   │   │   └── Header.vue
│       │   │   └── ui/              # shadcn UI 组件
│       │   ├── directives/           # 指令
│       │   │   └── permission.ts    # v-permission
│       │   ├── layouts/              # 布局
│       │   ├── pages/                # 页面
│       │   │   ├── admin/           # 后台页面
│       │   │   │   ├── dashboard/
│       │   │   │   ├── users/
│       │   │   │   ├── courses/
│       │   │   │   └── my-codes/
│       │   │   └── login.vue
│       │   ├── router/              # 路由
│       │   │   ├── index.ts
│       │   │   ├── routes.ts
│       │   │   └── guards.ts        # 路由鉴权
│       │   ├── stores/              # 状态管理
│       │   │   ├── user.ts         # 用户+权限状态
│       │   │   └── permission.ts
│       │   └── types/              # 类型定义
│       ├── student/                  # 学生端（独立）
│       │   ├── views/
│       │   ├── components/
│       │   ├── api/
│       │   └── router/
│       └── common/                   # 公共组件
│           └── components/ui/        # shadcn UI 组件库
├── backend/                          # Spring Boot 后端
│   ├── esmile-edu-common/          # 通用模块
│   │   └── com/esmile/edu/common/
│   │       ├── config/             # 配置类（SecurityConfig 等）
│   │       ├── security/           # Spring Security 相关
│   │       │   ├── JwtAuthFilter.java
│   │       │   └── JwtProvider.java
│   │       └── util/              # 工具类
│   ├── esmile-edu-user/            # 用户模块
│   │   ├── api/                    # REST 接口
│   │   └── domain/                 # 领域层
│   ├── esmile-edu-course/          # 课程模块
│   ├── esmile-edu-redeem/          # 兑换模块
│   └── pom.xml
```

### 4.3 权限控制实现

#### 前端 - 路由守卫

```typescript
// router/guards.ts
export async function routerGuard(to, from, next) {
  const userStore = useUserStore()

  // 公开路由直接通过
  if (to.meta.public) return next()

  // 检查登录
  if (to.meta.requiresAuth && !userStore.isAuthenticated) {
    return next({ name: 'AdminLogin', query: { redirect: to.fullPath } })
  }

  // 检查角色
  if (to.meta.roles && !to.meta.roles.includes(userStore.role)) {
    return next({ name: 'AdminDashboard' })
  }

  // 检查权限
  if (to.meta.permission && !userStore.hasPermission(to.meta.permission)) {
    return next({ name: 'AdminDashboard' })
  }

  next()
}
```

#### 前端 - 按钮权限指令

```typescript
// directives/permission.ts
export const permissionDirective = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const { value } = binding
    const userPermissions = userStore.permissions

    if (Array.isArray(value)) {
      if (!value.some(p => userPermissions.includes(p))) {
        el.remove()
      }
    } else {
      if (!userPermissions.includes(value)) {
        el.remove()
      }
    }
  }
}

// 使用: <Button v-permission="'user:create'">创建用户</Button>
```

#### 后端 - Spring Security 6

**安全配置**:

```java
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/admin/auth/**").permitAll()
                .requestMatchers("/api/v1/student/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
```

**权限校验**:

```java
// 使用 Spring Security @PreAuthorize 注解
@GetMapping
@PreAuthorize("hasAuthority('user:view')")
public ApiResponse<List<UserResponse>> listUsers() { ... }

// 多个权限（OR 关系）
@PostMapping
@PreAuthorize("hasAnyAuthority('user:create', 'user:update')")
public ApiResponse<Void> createUser() { ... }

// 角色校验
@PreAuthorize("hasRole('ADMIN')")
public ApiResponse<Void> adminOnly() { ... }
```

**JWT 认证过滤器**:

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);

        if (token != null && jwtProvider.validateToken(token)) {
            String userId = jwtProvider.getUserId(token);
            List<String> permissions = jwtProvider.getPermissions(token);

            // 构建 SecurityContext
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userId, null,
                    permissions.stream()
                        .map(p -> new SimpleGrantedAuthority("ROLE_" + p))  // ROLE_TEACHER, ROLE_ADMIN
                        .toList());

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
```

---

## 5. 数据模型

```prisma
enum Role { STUDENT TEACHER ADMIN }
enum UserStatus { ACTIVE PENDING_APPROVAL DISABLED }
enum CourseStatus { DRAFT PUBLISHED }
enum EnrollmentStatus { ACTIVE EXPIRED }
enum RedeemCodeStatus { PENDING REDEEMED EXPIRED }

// === 用户与权限 ===

model User {
  id        String   @id @default(uuid())
  email     String   @unique
  nickname  String
  avatar    String?
  role      Role     @default(STUDENT)
  status    UserStatus @default(ACTIVE)
  createdAt DateTime @default(now())

  courses     Course[]
  enrollments Enrollment[]
  redeemedCodes RedeemCode[] @relation("RedeemedBy")
}

// 角色实体（用于 RBAC）
model RoleEntity {
  id     String @id @default(uuid())
  code   String @unique  // ADMIN, TEACHER
  name   String

  permissions Permission[]
  users       User[]
}

// 权限定义
model Permission {
  id     String @id @default(uuid())
  code   String @unique  // user:view, course:create, etc.
  name   String
  remark String?

  roles RoleEntity[]
}

// === 课程相关 ===

model Course {
  id          String       @id @default(uuid())
  educatorId  String
  title       String
  description String?
  coverImage  String?
  status      CourseStatus @default(DRAFT)
  createdAt   DateTime     @default(now())

  educator    User         @relation(fields: [educatorId], references: [id])
  chapters    Chapter[]
  enrollments Enrollment[]
  redeemCodes RedeemCode[]
}

model Chapter {
  id        String @id @default(uuid())
  courseId  String
  title     String
  order     Int

  course  Course   @relation(fields: [courseId], references: [id])
  lessons Lesson[]
}

model Lesson {
  id             String  @id @default(uuid())
  chapterId      String
  title          String
  videoUrl       String? // 腾讯云 VOD 播放地址
  videoId        String? // 腾讯云 VOD videoId
  duration       Int?    // 视频时长（秒）
  order          Int

  chapter Chapter @relation(fields: [chapterId], references: [id])
}

model Enrollment {
  id        String            @id @default(uuid())
  userId    String
  courseId  String
  status    EnrollmentStatus  @default(ACTIVE)
  expiresAt DateTime?         // 课程权限到期时间
  createdAt DateTime          @default(now())

  user   User   @relation(fields: [userId], references: [id])
  course Course @relation(fields: [courseId], references: [id])
}

model RedeemCode {
  id              String    @id @default(uuid())
  code            String    @unique
  courseId        String
  status          RedeemCodeStatus @default(PENDING)
  expiresAt       DateTime
  redeemedBy      String?
  redeemedAt      DateTime?
  createdAt       DateTime  @default(now())

  course   Course @relation(fields: [courseId], references: [id])
  redeemer User?  @relation("RedeemedBy", fields: [redeemedBy], references: [id])
}
```

---

## 6. API 设计

### 6.1 学生端 API (`/api/v1/student/*`)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/student/auth/send-code` | 发送邮箱验证码 |
| POST | `/api/v1/student/auth/verify-code` | 验证并登录 |
| GET | `/api/v1/student/auth/me` | 获取当前用户 |
| GET | `/api/v1/student/courses` | 课程列表 |
| GET | `/api/v1/student/courses/[id]` | 课程详情 |
| POST | `/api/v1/student/codes/redeem` | 兑换课程 |
| GET | `/api/v1/student/my-courses` | 我的课程 |

### 6.2 统一后台 API (`/api/v1/admin/*`)

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | `/api/v1/admin/auth/send-code` | - | 发送验证码 |
| POST | `/api/v1/admin/auth/verify-code` | - | 验证登录 |
| GET | `/api/v1/admin/auth/me` | - | 当前用户信息 |
| GET | `/api/v1/admin/users` | user:view | 用户列表 |
| GET | `/api/v1/admin/users/[id]` | user:view | 用户详情 |
| PUT | `/api/v1/admin/users/[id]/approve` | user:approve | 审批教师 |
| PUT | `/api/v1/admin/users/[id]/status` | user:update | 修改用户状态 |
| GET | `/api/v1/admin/courses` | course:view | 课程列表（全部） |
| GET | `/api/v1/admin/courses/[id]` | course:view | 课程详情 |
| PUT | `/api/v1/admin/courses/[id]/audit` | course:audit | 审核课程 |
| GET | `/api/v1/admin/teacher-courses` | course:view | 教师课程列表 |
| GET | `/api/v1/admin/stats` | stats:view | 数据统计 |
| GET | `/api/v1/admin/redeem-codes` | code:view | 兑换码列表（全部） |

### 6.3 教师自有课程 API (`/api/v1/teacher/*`)

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/api/v1/teacher/courses` | course:view | 我的课程列表 |
| POST | `/api/v1/teacher/courses` | course:create | 创建课程 |
| GET | `/api/v1/teacher/courses/[id]` | course:view | 课程详情 |
| PUT | `/api/v1/teacher/courses/[id]` | course:update | 更新课程 |
| DELETE | `/api/v1/teacher/courses/[id]` | course:delete | 删除课程 |
| POST | `/api/v1/teacher/chapters` | chapter:create | 创建章节 |
| PUT | `/api/v1/teacher/chapters/[id]` | chapter:update | 更新章节 |
| DELETE | `/api/v1/teacher/chapters/[id]` | chapter:delete | 删除章节 |
| POST | `/api/v1/teacher/lessons` | lesson:create | 创建课时 |
| PUT | `/api/v1/teacher/lessons/[id]` | lesson:update | 更新课时 |
| DELETE | `/api/v1/teacher/lessons/[id]` | lesson:delete | 删除课时 |
| GET | `/api/v1/teacher/my-codes` | code:view | 我的兑换码 |
| POST | `/api/v1/teacher/codes` | code:create | 生成兑换码 |

### 6.4 外部系统 API (`/api/v1/redeem-codes/*`)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/redeem-codes/apply` | 生成兑换码（外部调用） |

### 6.5 视频上传 API

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/teacher/video/apply-upload` | 申请上传 |
| POST | `/api/v1/teacher/video/commit-upload` | 确认上传完成 |

---

## 7. 页面结构

### 公开页面

| 路径 | 功能 |
|------|------|
| `/` | 首页 |

### 学生端 (`/student/*`)

| 路径 | 功能 |
|------|------|
| `/student/login` | 登录 |
| `/student/register` | 注册 |
| `/student/dashboard` | 个人中心 |
| `/student/courses` | 课程列表 |
| `/student/courses/[id]` | 课程详情 |
| `/student/my-courses` | 已购课程 |
| `/student/redeem` | 兑换课程 |

### 统一后台 (`/admin/*`)

| 路径 | 功能 | 权限 |
|------|------|------|
| `/admin/login` | 登录 | 公开 |
| `/admin/dashboard` | 数据概览 | ADMIN |
| `/admin/profile` | 个人中心 | 登录用户 |
| `/admin/users` | 用户管理 | ADMIN |
| `/admin/users/[id]` | 用户详情 | ADMIN |
| `/admin/courses` | 全部课程 | ADMIN |
| `/admin/courses/[id]` | 课程详情 | ADMIN |
| `/admin/my-courses` | 我的课程 | TEACHER, ADMIN |
| `/admin/my-courses/new` | 创建课程 | TEACHER, ADMIN |
| `/admin/my-courses/[id]/edit` | 编辑课程 | TEACHER（自己）, ADMIN |
| `/admin/my-codes` | 我的兑换码 | TEACHER, ADMIN |
| `/admin/all-codes` | 全部兑换码 | ADMIN |

---

## 8. 验收标准

- [ ] 学生可通过邮箱+验证码登录/注册
- [ ] 教师/管理员可通过统一后台登录
- [ ] 教师可通过邮箱+验证码注册（需管理员审批）
- [ ] 管理员可审批教师注册申请
- [ ] 管理员可管理用户状态
- [ ] 教师可创建课程（草稿/发布）
- [ ] 教师可添加章节和课时
- [ ] 教师可上传视频
- [ ] 教师可生成兑换码
- [ ] 学生可输入兑换码兑换课程
- [ ] 学生可观看已兑换课程的视频
- [ ] 兑换码一次性使用，兑换后失效
- [ ] 管理员可查看数据统计
- [ ] 不同角色看到不同菜单（RBAC 菜单级控制）
- [ ] 无权限用户无法访问对应 API（RBAC API 级控制）

---

## 9. MVP 排除项（后期迭代）

- 微信登录
- 作业批改
- 社区功能
- OpenClaw 自动发货
- Key 防盗链
- 学习路径

---

## 10. 待确认

- [ ] 腾讯云 VOD 控制台配置（防盗链、域名）
- [ ] 服务器 PostgreSQL 安装配置
- [ ] 邮件服务配置（发送验证码）
- [ ] 权限数据初始化（RoleEntity, Permission 表数据）
