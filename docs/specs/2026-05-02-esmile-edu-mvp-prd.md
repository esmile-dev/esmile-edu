# esmile 教育平台 - MVP PRD

**日期**: 2026-05-03
**状态**: 需求澄清完成
**版本**: v1.0

---

## 1. 项目概述

**定位**: 面向大学生的专业课程和技能培训教育平台，连接个人教育者和生源。

**MVP 目标**: 1个月内上线，验证核心业务流程。

**团队**: 2开发 + 1测试

---

## 2. 功能范围

### 2.1 用户系统

| 功能 | 说明 |
|------|------|
| 邮箱注册/登录 | 用户通过邮箱和验证码注册和登录 |
| 角色 | 学生、教师（需审批）、管理员 |
| 微信登录 | 后期接入（MVP 排除） |

### 2.2 课程系统

教师功能：
- 创建课程（标题、描述、封面图）
- 管理课程状态：草稿 ↔ 已发布
- 添加章节（支持多个章节）
- 添加课时（每个课时含视频）
- 上传视频：平台内上传 → 腾讯云自动转码
- 生成兑换码

学生功能：
- 购买/兑换课程
- 观看视频（已购买的课程）

### 2.3 管理员功能

- 用户管理（审批教师、禁用账号）
- 课程管理（审核、上下架）
- 数据统计

### 2.3 兑换码系统

| 规则 | 说明 |
|------|------|
| 生成方式 | 教育者单个生成 |
| 格式 | 8位字母数字（如 `A1B2C3D4`） |
| 兑换码有效期 | 教育者自定义（如30天） |
| 课程权限期限 | 教育者自定义（如兑换后1年） |
| 使用次数 | 一次性，兑换后失效 |
| 绑定对象 | 绑定到用户账号 |

### 2.4 闲鱼集成

| 规则 | 说明 |
|------|------|
| 商品形式 | 固定价格（不支持议价） |
| 发货方式 | MVP 手动发货 |
| 自动发货 | 后期接入 OpenClaw（MVP 排除） |

---

## 3. 技术方案

### 3.1 技术栈

| 层级 | 技术 | 备注 |
|------|------|------|
| 后端 | Spring Boot 3.x | Java 25 |
| 数据库 | PostgreSQL | 自建在 47.107.163.188 |
| ORM | Spring Data JPA | 数据库操作 |
| CSS | Tailwind CSS | 样式 |
| 视频托管 | 腾讯云 VOD | 含自动转码 |
| 视频防盗 | Referer 防盗链 | 免费、简单 |

### 3.2 项目结构

```
esmile-edu/
├── docs/specs/                       # 需求文档
├── frontend/                         # Vue 3 前端
│   └── src/
│       ├── student/                  # 学生端
│       │   ├── views/
│       │   ├── components/
│       │   ├── api/
│       │   └── router/
│       ├── teacher/                  # 教师端
│       │   ├── views/
│       │   ├── components/
│       │   ├── api/
│       │   └── router/
│       ├── admin/                    # 管理端
│       │   ├── views/
│       │   ├── components/
│       │   ├── api/
│       │   └── router/
│       └── common/                   # 公共组件
├── backend/                          # Spring Boot 后端
│   ├── esmile-edu-common/            # 通用模块
│   ├── esmile-edu-user/              # 用户模块
│   │   ├── api/student/              # 学生 API
│   │   ├── api/teacher/              # 教师 API
│   │   └── api/admin/                # 管理 API
│   ├── esmile-edu-course/            # 课程模块
│   ├── esmile-edu-redeem/            # 兑换模块
│   └── pom.xml
```

---

## 4. 数据模型

```prisma
enum Role { STUDENT TEACHER ADMIN }
enum UserStatus { ACTIVE PENDING_APPROVAL DISABLED }
enum CourseStatus { DRAFT PUBLISHED }
enum EnrollmentStatus { ACTIVE EXPIRED }

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
  videoUrl       String? # 腾讯云 VOD 播放地址
  videoId        String? # 腾讯云 VOD videoId
  duration       Int?    # 视频时长（秒）
  order          Int

  chapter Chapter @relation(fields: [chapterId], references: [id])
}

model Enrollment {
  id        String            @id @default(uuid())
  userId    String
  courseId  String
  status    EnrollmentStatus  @default(ACTIVE)
  expiresAt DateTime?         # 课程权限到期时间
  createdAt DateTime          @default(now())

  user   User   @relation(fields: [userId], references: [id])
  course Course @relation(fields: [courseId], references: [id])
}

model RedeemCode {
  id               String    @id @default(uuid())
  code             String    @unique
  courseId         String
  usedBy           String?
  usedAt           DateTime?
  expiresAt        DateTime? # 兑换码有效期
  courseExpiresAt  DateTime? # 兑换后课程权限期限
  createdAt        DateTime  @default(now())

  course   Course @relation(fields: [courseId], references: [id])
  redeemer User?  @relation("RedeemedBy", fields: [usedBy], references: [id])
}
```

---

## 5. API 设计

### 学生 API (`/api/v1/student/*`)
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/student/auth/send-code` | 发送邮箱验证码 |
| POST | `/api/v1/student/auth/verify-code` | 验证并登录 |
| GET | `/api/v1/student/auth/me` | 获取当前用户 |
| GET | `/api/v1/student/courses` | 课程列表 |
| GET | `/api/v1/student/courses/[id]` | 课程详情 |
| POST | `/api/v1/student/codes/redeem` | 兑换课程 |
| GET | `/api/v1/student/my-courses` | 我的课程 |

### 教师 API (`/api/v1/teacher/*`)
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/teacher/auth/send-code` | 发送邮箱验证码 |
| POST | `/api/v1/teacher/auth/verify-code` | 验证并登录 |
| GET | `/api/v1/teacher/auth/me` | 获取当前用户 |
| GET | `/api/v1/teacher/courses` | 我的课程列表 |
| POST | `/api/v1/teacher/courses` | 创建课程 |
| GET | `/api/v1/teacher/courses/[id]` | 课程详情 |
| PUT | `/api/v1/teacher/courses/[id]` | 更新课程 |
| DELETE | `/api/v1/teacher/courses/[id]` | 删除课程 |
| GET | `/api/v1/teacher/codes` | 兑换码列表 |
| POST | `/api/v1/teacher/codes` | 生成兑换码 |
| POST | `/api/v1/teacher/chapters` | 创建章节 |
| PUT | `/api/v1/teacher/chapters/[id]` | 更新章节 |
| DELETE | `/api/v1/teacher/chapters/[id]` | 删除章节 |
| POST | `/api/v1/teacher/lessons` | 创建课时 |
| PUT | `/api/v1/teacher/lessons/[id]` | 更新课时 |
| DELETE | `/api/v1/teacher/lessons/[id]` | 删除课时 |

### 管理员 API (`/api/v1/admin/*`)
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/admin/auth/login` | 管理员登录 |
| GET | `/api/v1/admin/users` | 用户列表 |
| PUT | `/api/v1/admin/users/[id]/approve` | 审批教师 |
| PUT | `/api/v1/admin/users/[id]/status` | 修改用户状态 |
| GET | `/api/v1/admin/courses` | 所有课程 |
| PUT | `/api/v1/admin/courses/[id]/status` | 修改课程状态 |
| GET | `/api/v1/admin/stats` | 数据统计 |

### 视频上传
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/teacher/video/apply-upload` | 申请上传 |
| POST | `/api/v1/teacher/video/commit-upload` | 确认上传完成 |

---

## 6. 页面结构

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

### 教师端 (`/teacher/*`)
| 路径 | 功能 |
|------|------|
| `/teacher/login` | 登录 |
| `/teacher/register` | 注册（待审批） |
| `/teacher/dashboard` | 个人中心 |
| `/teacher/courses` | 我的课程 |
| `/teacher/courses/new` | 创建课程 |
| `/teacher/courses/[id]/edit` | 编辑课程 |
| `/teacher/codes` | 兑换码管理 |

### 管理端 (`/admin/*`)
| 路径 | 功能 |
|------|------|
| `/admin/login` | 管理员登录 |
| `/admin/dashboard` | 数据概览 |
| `/admin/users` | 用户管理 |
| `/admin/users/[id]` | 用户详情/审批 |
| `/admin/courses` | 课程管理 |

---

## 7. 验收标准

- [ ] 学生可通过邮箱+验证码登录/注册
- [ ] 教师可通过邮箱+验证码注册（需管理员审批）
- [ ] 管理员可审批教师注册申请
- [ ] 教师可创建课程（草稿/发布）
- [ ] 教师可添加章节和课时
- [ ] 教师可上传视频
- [ ] 教师可生成兑换码
- [ ] 学生可输入兑换码兑换课程
- [ ] 学生可观看已兑换课程的视频
- [ ] 兑换码一次性使用，兑换后失效
- [ ] 管理员可管理用户状态
- [ ] 管理员可查看数据统计

---

## 8. MVP 排除项（后期迭代）

- 微信登录
- 作业批改
- 社区功能
- OpenClaw 自动发货
- Key 防盗链
- 学习路径

---

## 9. 待确认

- [ ] 腾讯云 VOD 控制台配置（防盗链、域名）
- [ ] 服务器 PostgreSQL 安装配置
- [ ] 邮件服务配置（发送验证码）