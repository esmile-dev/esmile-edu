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
| 微信登录 | 后期接入（MVP 排除） |

### 2.2 课程系统

教育者（老师）功能：
- 创建课程（标题、描述、封面图）
- 管理课程状态：草稿 ↔ 已发布
- 添加章节（支持多个章节）
- 添加课时（每个课时含视频）
- 上传视频：平台内上传 → 腾讯云自动转码

学生功能：
- 购买/兑换课程
- 观看视频（已购买的课程）

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
│       ├── student/                  # 学生端视图
│       ├── educator/                 # 教育者端视图
│       ├── common/                   # 公共组件
│       ├── api/                      # API 调用
│       └── router/                   # 路由
├── backend/                          # Spring Boot 后端
│   ├── esmile-edu-common/            # 通用模块
│   ├── esmile-edu-user/              # 用户模块
│   │   └── api/                      # REST 接口
│   ├── esmile-edu-course/            # 课程模块
│   │   └── api/                      # REST 接口
│   ├── esmile-edu-redeem/            # 兑换模块
│   │   └── api/                      # REST 接口
│   └── pom.xml
```

---

## 4. 数据模型

```prisma
enum Role { EDUCATOR STUDENT }
enum CourseStatus { DRAFT PUBLISHED }
enum EnrollmentStatus { ACTIVE EXPIRED }

model User {
  id        String   @id @default(uuid())
  email     String   @unique
  nickname  String?
  avatar    String?
  role      Role     @default(STUDENT)
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

### 认证
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/send-code` | 发送邮箱验证码 |
| POST | `/api/auth/verify-code` | 验证并登录 |
| GET | `/api/auth/me` | 获取当前用户 |

### 课程
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/courses` | 课程列表 |
| POST | `/api/courses` | 创建课程 |
| GET | `/api/courses/[id]` | 课程详情 |
| PUT | `/api/courses/[id]` | 更新课程 |
| PUT | `/api/courses/[id]/publish` | 发布课程 |
| DELETE | `/api/courses/[id]` | 删除课程 |

### 章节
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/courses/[id]/chapters` | 章节列表 |
| POST | `/api/courses/[id]/chapters` | 创建章节 |
| PUT | `/api/chapters/[id]` | 更新章节 |
| DELETE | `/api/chapters/[id]` | 删除章节 |

### 课时
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/chapters/[id]/lessons` | 课时列表 |
| POST | `/api/chapters/[id]/lessons` | 创建课时 |
| PUT | `/api/lessons/[id]` | 更新课时 |
| DELETE | `/api/lessons/[id]` | 删除课时 |

### 兑换码
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/codes` | 兑换码列表 |
| POST | `/api/codes` | 生成兑换码 |
| POST | `/api/codes/redeem` | 兑换课程 |

### 视频上传
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/video/apply-upload` | 申请上传 |
| POST | `/api/video/commit-upload` | 确认上传完成 |

---

## 6. 页面结构

### 公开页面
| 路径 | 功能 |
|------|------|
| `/` | 首页 |
| `/login` | 登录 |
| `/register` | 注册 |

### 教育者端
| 路径 | 功能 |
|------|------|
| `/educator/dashboard` | 数据概览 |
| `/educator/courses` | 课程列表 |
| `/educator/courses/new` | 创建课程 |
| `/educator/courses/[id]` | 课程详情/编辑 |
| `/educator/codes` | 兑换码管理 |

### 学生端
| 路径 | 功能 |
|------|------|
| `/student/dashboard` | 学习概览 |
| `/student/courses` | 我的课程 |
| `/student/courses/[id]` | 课程详情 |
| `/student/courses/[id]/learn/[lessonId]` | 课时学习 |
| `/redeem` | 兑换课程 |

---

## 7. 验收标准

- [ ] 用户可通过邮箱+验证码登录/注册
- [ ] 教育者可创建课程（草稿/发布）
- [ ] 教育者可添加章节和课时
- [ ] 教育者可上传视频
- [ ] 教育者可生成单个兑换码
- [ ] 学生可输入兑换码兑换课程
- [ ] 学生可观看已兑换课程的视频
- [ ] 兑换码一次性使用，兑换后失效

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