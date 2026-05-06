# esmile 教育平台 - MVP PRD

**日期**: 2026-05-04
**状态**: 开发中
**版本**: v1.1

---

## 1. 项目概述

**定位**: 面向大学生的专业课程和技能培训教育平台，连接个人教育者和生源。

**MVP 目标**: 1个月内上线，验证核心业务流程。

**团队**: 2开发 + 1测试

---

## 2. 功能范围

### 2.1 用户系统

| 功能 | 说明 | 状态 |
|------|------|------|
| 邮箱验证码登录 | 用户通过邮箱和验证码注册和登录 | ✅ 已实现 |
| 角色 | 学生、教师（需审批）、管理员 | ✅ 已实现 |
| 用户状态管理 | ACTIVE / PENDING_APPROVAL / DISABLED | ✅ 已实现 |
| 微信登录 | 后期接入（MVP 排除） | ❌ 排除 |

### 2.2 课程系统

**教师功能**：
- ✅ 创建课程（标题、描述、封面图）
- ✅ 管理课程状态：草稿 ↔ 已发布
- ✅ 添加章节（支持多个章节）
- ✅ 添加课时（每个课时含视频）
- ⚠️ 上传视频：平台内上传 → 腾讯云（当前为Mock）
- ✅ 生成兑换码

**学生功能**：
- ✅ 购买/兑换课程
- ✅ 观看视频（已购买的课程）
- ✅ 查看课程进度

### 2.3 管理员功能

| 功能 | 说明 | 状态 |
|------|------|------|
| 用户管理 | 审批教师、禁用账号 | ✅ 已实现 |
| 课程管理 | 查看所有课程 | ✅ 已实现 |
| 数据统计 | 平台运营数据概览 | ❌ 待实现 |

### 2.4 兑换码系统

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
| 生成方式 | 教师后台生成或外部系统调用 API 接口生成 |
| 格式 | 8位字母数字（如 `A1B2C3D4`） |
| 兑换码有效期 | 教师指定（如30天），必须在此之前兑换 |
| 使用次数 | 一次性，兑换后失效 |
| 绑定对象 | 绑定到用户账号 |

### 2.5 闲鱼集成

| 规则 | 说明 | 状态 |
|------|------|------|
| 商品形式 | 固定价格（不支持议价） | ❌ 排除 |
| 发货方式 | MVP 手动发货 | ✅ 已实现 |
| 自动发货 | 后期接入 OpenClaw | ❌ 排除 |

---

## 3. 技术方案

### 3.1 技术栈

#### 后端

| 层级 | 技术 | 版本 |
|------|------|------|
| 运行环境 | Java | 25 LTS |
| Web 框架 | Spring Boot | 3.3.x |
| ORM | Spring Data JPA | 6.x |
| 数据库 | PostgreSQL | 15+ |
| 认证 | JWT (jjwt) | 0.12.x |
| 邮件 | Tencent Cloud Email | - |
| 视频托管 | 腾讯云 VOD | - |

#### 前端

| 层级 | 技术 | 版本 |
|------|------|------|
| 框架 | Vue | 3.5.x |
| 构建工具 | Vite | 6.x |
| 语言 | TypeScript | 5.x |
| UI 组件 | shadcn/ui | latest |
| CSS | Tailwind CSS | 4.x |
| 路由 | Vue Router | 4.x |
| 状态管理 | Pinia | 2.x |
| HTTP 客户端 | Axios | 1.x |
| 类型校验 | Zod | 3.x |

### 3.2 项目结构

```
esmile-edu/
├── docs/
│   ├── specs/                          # 需求文档
│   ├── backend/technical/              # 后端技术文档
│   │   ├── 1-architecture/            # 架构设计
│   │   ├── 2-modules/                 # 模块设计
│   │   ├── 3-data-model/              # 数据模型
│   │   ├── 4-api-specification/       # API 规范
│   │   ├── 5-exception-handling/      # 异常处理
│   │   ├── 6-infrastructure/           # 基础设施
│   │   ├── 7-security/                # 安全规范
│   │   ├── 8-development-standards/   # 开发规范
│   │   └── 9-deployment/               # 部署指南
│   └── frontend/technical/             # 前端技术文档
│
├── frontend/                           # Vue 3 前端
│   └── src/
│       ├── student/                    # 学生端
│       │   ├── views/                  # 页面
│       │   ├── components/             # 业务组件
│       │   ├── api/                    # API 调用
│       │   └── router/                 # 路由
│       ├── teacher/                    # 教师端
│       ├── admin/                      # 管理端
│       └── common/                     # 公共组件
│           ├── components/ui/           # shadcn/ui 组件
│           ├── composables/             # 组合式函数
│           ├── types/                  # 类型定义
│           └── utils/                   # 工具函数
│
└── backend/                           # Spring Boot 后端 (单体分层架构)
    └── src/main/java/com/esmile/edu/
        ├── api/                        # Controller 层
        │   ├── user/
        │   ├── course/
        │   ├── redeem/
        │   └── video/
        ├── biz/                        # 业务聚合层
        │   ├── UserBizService.java
        │   ├── CourseBizService.java
        │   ├── RedeemBizService.java
        │   └── VideoService.java
        ├── module/                     # 数据模型层
        │   ├── user/
        │   ├── course/
        │   ├── redeem/
        │   └── auth/
        ├── dto/                        # 数据传输对象
        │   ├── request/
        │   └── response/
        └── common/                     # 公共组件
            ├── auth/                   # 认证授权
            ├── email/                  # 邮件服务
            ├── exception/              # 异常定义
            └── config/                 # 配置类
```

### 3.3 分层架构

**后端分层**：
```
api → biz → module → common
```
- **api**: HTTP 请求处理、参数校验
- **biz**: 业务逻辑聚合、事务控制
- **module**: 数据模型、Repository
- **common**: 公共组件

**前端分层**：
```
view → composable → store/api
```
- **view**: 页面渲染、用户交互
- **composable**: 业务逻辑复用
- **store**: 状态管理
- **api**: 后端通信

---

## 4. 数据模型

### 4.1 枚举定义

| 枚举 | 值 | 说明 |
|------|-----|------|
| Role | STUDENT, TEACHER, ADMIN | 用户角色 |
| UserStatus | ACTIVE, PENDING_APPROVAL, DISABLED | 用户状态 |
| CourseStatus | DRAFT, PUBLISHED | 课程状态 |
| EnrollmentStatus | ACTIVE, EXPIRED | 选课状态 |
| RedeemCodeStatus | PENDING, USED, EXPIRED | 兑换码状态 |
| LessonStatus | PROCESSING, READY, FAILED | 课时/视频状态 |

### 4.2 数据表

**users**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGSERIAL | 主键 |
| email | VARCHAR(255) | 唯一邮箱 |
| password | VARCHAR(255) | 加密密码 |
| nickname | VARCHAR(100) | 昵称 |
| avatar | VARCHAR(500) | 头像URL |
| role | VARCHAR(20) | STUDENT/TEACHER/ADMIN |
| status | VARCHAR(20) | ACTIVE/PENDING_APPROVAL/DISABLED |
| disabled_at | TIMESTAMP | 禁用时间 |
| disable_reason | VARCHAR(500) | 禁用原因 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

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
| updated_at | TIMESTAMP | 更新时间 |

**chapters**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGSERIAL | 主键 |
| course_id | BIGINT | 课程ID |
| title | VARCHAR(200) | 章节标题 |
| order_num | INT | 排序 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

**lessons**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGSERIAL | 主键 |
| chapter_id | BIGINT | 章节ID |
| title | VARCHAR(200) | 课时标题 |
| video_url | VARCHAR(500) | 腾讯云VOD播放地址 |
| video_id | VARCHAR(100) | 腾讯云VOD videoId |
| duration | INT | 视频时长（秒） |
| status | VARCHAR(20) | PROCESSING/READY/FAILED |
| order_num | INT | 排序 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

**enrollments**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGSERIAL | 主键 |
| user_id | BIGINT | 用户ID |
| course_id | BIGINT | 课程ID |
| status | VARCHAR(20) | ACTIVE/EXPIRED |
| expires_at | TIMESTAMP | 权限到期时间 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

**redeem_codes**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGSERIAL | 主键 |
| code | VARCHAR(20) | 8位兑换码 |
| course_id | BIGINT | 课程ID |
| created_by | BIGINT | 创建者ID（教师） |
| status | VARCHAR(20) | PENDING/USED/EXPIRED |
| used_by | BIGINT | 使用者ID |
| used_at | TIMESTAMP | 使用时间 |
| expires_at | TIMESTAMP | 兑换码有效期 |
| course_expires_at | TIMESTAMP | 兑换后课程权限期限 |
| created_at | TIMESTAMP | 创建时间 |

**verification_codes**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGSERIAL | 主键 |
| email | VARCHAR(255) | 邮箱 |
| code | VARCHAR(10) | 验证码 |
| role | VARCHAR(20) | 用户角色 |
| expires_at | TIMESTAMP | 过期时间 |
| used_at | TIMESTAMP | 使用时间 |
| created_at | TIMESTAMP | 创建时间 |

---

## 5. API 设计

### 5.1 统一响应格式

```json
{
  "code": 200,
  "message": "Success",
  "data": { ... }
}
```

**状态码**：
| HTTP Status | 说明 |
|-------------|------|
| 200 | 成功 |
| 201 | 创建成功 |
| 204 | 删除成功（无内容） |
| 400 | 请求参数错误 |
| 401 | 未认证 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器错误 |

### 5.2 学生 API (`/api/v1/student/*`)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/student/auth/send-code` | 发送邮箱验证码 |
| POST | `/student/auth/verify-code` | 验证并登录 |
| GET | `/student/auth/me` | 获取当前用户 |
| GET | `/student/courses` | 课程列表（分页） |
| GET | `/student/courses/{id}` | 课程详情（含章节课时） |
| POST | `/student/redeem` | 兑换课程 |
| GET | `/student/my-courses` | 我的课程（已购） |

### 5.3 教师 API (`/api/v1/teacher/*`)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/teacher/auth/send-code` | 发送邮箱验证码 |
| POST | `/teacher/auth/verify-code` | 验证并登录 |
| GET | `/teacher/auth/me` | 获取当前用户 |
| GET | `/teacher/my-courses` | 我的课程列表 |
| POST | `/teacher/courses` | 创建课程 |
| GET | `/teacher/courses/{id}` | 课程详情 |
| PUT | `/teacher/courses/{id}` | 更新课程 |
| DELETE | `/teacher/courses/{id}` | 删除课程 |
| POST | `/teacher/courses/{id}/publish` | 发布课程 |
| POST | `/teacher/chapters` | 创建章节 |
| PUT | `/teacher/chapters/{id}` | 更新章节 |
| DELETE | `/teacher/chapters/{id}` | 删除章节 |
| POST | `/teacher/lessons` | 创建课时 |
| PUT | `/teacher/lessons/{id}` | 更新课时 |
| DELETE | `/teacher/lessons/{id}` | 删除课时 |
| GET | `/teacher/video/apply-upload` | 申请视频上传 |
| POST | `/teacher/video/commit-upload` | 确认视频上传完成 |

### 5.4 管理员 API (`/api/v1/admin/*`)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/admin/users` | 用户列表（支持角色/状态筛选） |
| PUT | `/admin/users/{id}/approve` | 审批教师 |
| PUT | `/admin/users/{id}/status` | 更新用户状态（禁用/启用） |

### 5.5 外部系统 API (`/api/v1/*`)

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/redeem-codes/apply` | 批量生成兑换码 | API Key |

### 5.6 视频上传流程

```
1. 教师调用 GET /teacher/video/apply-upload?fileName=xxx&fileSize=123
2. 服务端返回 videoId、signature、uploadUrl
3. 教师上传视频到 uploadUrl
4. 教师调用 POST /teacher/video/commit-upload 确认完成
5. 服务端更新课时状态为 READY
```

---

## 6. 前端页面结构

### 6.1 学生端 (`/student/*`)

| 路径 | 功能 |
|------|------|
| `/student/login` | 登录（发送验证码） |
| `/student/send-code` | 填写邮箱获取验证码 |
| `/student/courses` | 课程列表 |
| `/student/courses/:id` | 课程详情（章节、课时列表） |
| `/student/my-courses` | 已购课程列表 |
| `/student/my-courses/:id` | 继续学习（跳转至上次播放位置） |
| `/student/redeem` | 兑换课程 |

### 6.2 教师端 (`/teacher/*`)

| 路径 | 功能 |
|------|------|
| `/teacher/login` | 登录（发送验证码） |
| `/teacher/send-code` | 填写邮箱获取验证码 |
| `/teacher/courses` | 我的课程列表 |
| `/teacher/courses/new` | 创建课程 |
| `/teacher/courses/:id/edit` | 编辑课程 |
| `/teacher/courses/:id/chapters` | 管理章节和课时 |
| `/teacher/courses/:id/video` | 上传视频 |

### 6.3 管理端 (`/admin/*`)

| 路径 | 功能 |
|------|------|
| `/admin/login` | 管理员登录 |
| `/admin/users` | 用户管理（筛选教师/学生） |
| `/admin/users/:id` | 用户详情/审批教师 |

---

## 7. 验收标准

### 7.1 已完成

- [x] 学生可通过邮箱+验证码登录/注册
- [x] 教师可通过邮箱+验证码注册（需管理员审批）
- [x] 管理员可审批教师注册申请
- [x] 教师可创建课程（草稿/发布）
- [x] 教师可添加章节和课时
- [x] 教师可上传视频（Mock模式）
- [x] 教师可生成兑换码
- [x] 学生可输入兑换码兑换课程
- [x] 学生可观看已兑换课程的视频
- [x] 兑换码一次性使用，兑换后失效
- [x] 管理员可管理用户状态（禁用/启用）
- [x] 安全认证：IDOR漏洞已修复
- [x] 安全认证：管理员接口授权校验已添加

### 7.2 待完成

- [ ] 管理员可查看数据统计
- [ ] 视频上传真实腾讯云集成
- [ ] 邮件发送真实集成（当前为Mock）
- [ ] 生产环境配置优化（JWT密钥、API Key）

---

## 8. MVP 排除项（后期迭代）

- 微信登录
- 作业批改
- 社区功能
- OpenClaw 自动发货
- Key 防盗链
- 学习路径
- 数据统计
- 微信/支付宝支付

---

## 9. Production-Ready 任务清单

详见: [后端 Production-Ready 任务清单](../backend/production-readiness-tasks.md)

**已完成**: 7/20
**待完成**: 13/20

---

## 10. 文档索引

| 文档 | 路径 |
|------|------|
| 后端架构设计 | [docs/backend/technical/1-architecture/README.md](../backend/technical/1-architecture/README.md) |
| 后端模块设计 | [docs/backend/technical/2-modules/README.md](../backend/technical/2-modules/README.md) |
| 后端 API 规范 | [docs/backend/technical/4-api-specification/README.md](../backend/technical/4-api-specification/) |
| 前端架构设计 | [docs/frontend/technical/1-architecture/README.md](../frontend/technical/1-architecture/README.md) |
| 前端组件设计 | [docs/frontend/technical/5-components/README.md](../frontend/technical/5-components/README.md) |
| 前端 API 客户端 | [docs/frontend/technical/4-api-client/README.md](../frontend/technical/4-api-client/README.md) |
