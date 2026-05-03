# esmile 教育平台 - MVP PRD

**日期**: 2026-05-03
**状态**: 需求澄清完成
**版本**: v1.0

---

## 1. 项目概述

**定位**: 面向大学生的专业课程和技能培训教育平台，连接个人教育者和生源。

**MVP 目标**: 1个月内上线，验证核心业务流程。

像·**团队**: 2开发 + 1测试

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

#### 状态定义

| 状态 | 说明 |
|------|------|
| 待兑换 (PENDING) | 生成后等待兑换 |
| 已兑换 (REDEEMED) | 已被用户使用 |
| 已失效 (EXPIRED) | 超过有效期未兑换或关联课程被删除 |

#### 状态流转

```
[生成] → 待兑换 → 已兑换
              ↘ 已失效
```

#### 业务规则

| 规则 | 说明 |
|------|------|
| 生成方式 | 外部系统调用 API 接口生成 |
| 格式 | 8位字母数字（如 `A1B2C3D4`） |
| 兑换码有效期 | 调用方指定（如30天），必须在此之前兑换 |
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

### 4.1 枚举定义

| 枚举 | 值 |
|------|-----|
| Role | STUDENT, TEACHER, ADMIN |
| UserStatus | ACTIVE, PENDING_APPROVAL, DISABLED |
| CourseStatus | DRAFT, PUBLISHED |
| EnrollmentStatus | ACTIVE, EXPIRED |

### 4.2 数据表

**users**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGSERIAL | 主键 |
| email | VARCHAR(255) | 唯一邮箱 |
| nickname | VARCHAR(100) | 昵称 |
| avatar | VARCHAR(500) | 头像URL |
| role | VARCHAR(20) | STUDENT/TEACHER/ADMIN |
| status | VARCHAR(20) | ACTIVE/PENDING_APPROVAL/DISABLED |
| created_at | TIMESTAMP | 创建时间 |

**courses**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGSERIAL | 主键 |
| educator_id | BIGINT | 教师ID |
| title | VARCHAR(200) | 课程标题 |
| description | TEXT | 课程描述 |
| cover_image | VARCHAR(500) | 封面图URL |
| status | VARCHAR(20) | DRAFT/PUBLISHED |
| created_at | TIMESTAMP | 创建时间 |

**chapters**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGSERIAL | 主键 |
| course_id | BIGINT | 课程ID |
| title | VARCHAR(200) | 章节标题 |
| order_num | INT | 排序 |

**lessons**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGSERIAL | 主键 |
| chapter_id | BIGINT | 章节ID |
| title | VARCHAR(200) | 课时标题 |
| video_url | VARCHAR(500) | 腾讯云VOD播放地址 |
| video_id | VARCHAR(100) | 腾讯云VOD videoId |
| duration | INT | 视频时长（秒） |
| order_num | INT | 排序 |

**enrollments**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGSERIAL | 主键 |
| user_id | BIGINT | 用户ID |
| course_id | BIGINT | 课程ID |
| status | VARCHAR(20) | ACTIVE/EXPIRED |
| expires_at | TIMESTAMP | 权限到期时间 |
| created_at | TIMESTAMP | 创建时间 |

**redeem_codes**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGSERIAL | 主键 |
| code | VARCHAR(20) | 8位兑换码 |
| course_id | BIGINT | 课程ID |
| status | VARCHAR(20) | PENDING/REDEEMED/EXPIRED |
| used_by | BIGINT | 使用者ID |
| used_at | TIMESTAMP | 使用时间 |
| expires_at | TIMESTAMP | 兑换码有效期 |
| course_expires_at | TIMESTAMP | 兑换后课程权限期限 |
| created_at | TIMESTAMP | 创建时间 |
>>>>>>> Stashed changes

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
| POST | `/api/v1/teacher/chapters` | 创建章节 |
| PUT | `/api/v1/teacher/chapters/[id]` | 更新章节 |
| DELETE | `/api/v1/teacher/chapters/[id]` | 删除章节 |
| POST | `/api/v1/teacher/lessons` | 创建课时 |
| PUT | `/api/v1/teacher/lessons/[id]` | 更新课时 |
| DELETE | `/api/v1/teacher/lessons/[id]` | 删除课时 |

### 外部系统 API (`/api/v1/redeem-codes/*`)
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/redeem-codes/apply` | 生成兑换码 |

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