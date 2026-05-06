# esmile-edu Frontend

在线教育平台前端，基于 Vue 3 + TypeScript + Vite + Tailwind CSS 构建。

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5.x | 渐进式 JavaScript 框架 |
| Vite | 8.x | 下一代前端构建工具 |
| TypeScript | 6.x | JavaScript 超集 |
| Tailwind CSS | 3.4.x | utility-first CSS 框架 |
| Vue Router | 4.x | Vue.js 官方路由 |
| Pinia | 3.x | Vue 状态管理 |
| Axios | 1.x | HTTP 请求库 |
| Zod | 4.x | TypeScript schema 验证 |
| Lucide | 1.x | 图标库 |

## 项目结构

```
frontend/
├── src/
│   ├── main.ts                    # 应用入口
│   ├── App.vue                    # 根组件
│   ├── style.css                  # 全局样式 + Tailwind
│   │
│   ├── common/                    # 公共模块（所有 portal 共享）
│   │   ├── api/                   # API client
│   │   │   └── apiClient.ts       # Axios 实例 + 拦截器
│   │   ├── components/ui/         # UI 组件（shadcn/ui 风格）
│   │   │   ├── Button.vue
│   │   │   ├── Input.vue
│   │   │   ├── Card.vue
│   │   │   ├── Badge.vue
│   │   │   ├── Avatar.vue
│   │   │   ├── Label.vue
│   │   │   ├── Skeleton.vue
│   │   │   └── DropdownMenu.vue
│   │   ├── stores/                # Pinia 状态管理
│   │   │   └── auth.ts            # 认证状态
│   │   ├── router/                # 路由配置
│   │   │   └── index.ts           # 路由守卫
│   │   ├── types/                 # TypeScript 类型定义
│   │   │   └── api.ts             # API 类型
│   │   ├── mock/                  # Mock API（开发/演示用）
│   │   │   ├── mockApi.ts
│   │   │   └── mockData.ts
│   │   └── lib/                   # 工具库
│   │       └── utils.ts           # cn() 等工具函数
│   │
│   ├── student/                   # 学生端
│   │   ├── api/
│   │   │   └── studentApi.ts      # 学生端 API
│   │   ├── components/
│   │   │   ├── StudentHeader.vue  # 顶部导航 + 底部 Tab
│   │   │   ├── CourseCard.vue     # 课程卡片
│   │   │   └── ChapterList.vue    # 章节列表
│   │   └── views/
│   │       ├── auth/
│   │       │   ├── LoginView.vue      # 邮箱登录
│   │       │   └── SendCodeView.vue   # 验证码确认
│   │       ├── courses/
│   │       │   ├── CourseListView.vue   # 课程目录
│   │       │   └── CourseDetailView.vue # 课程详情
│   │       ├── my-courses/
│   │       │   └── MyCoursesView.vue   # 我的课程
│   │       ├── redeem/
│   │       │   └── RedeemView.vue      # 兑换码兑换
│   │       └── learn/
│   │           └── LearnView.vue        # 视频学习
│   │
│   ├── teacher/                   # 教师端
│   │   ├── api/
│   │   │   └── teacherApi.ts     # 教师端 API
│   │   ├── components/
│   │   │   └── TeacherHeader.vue
│   │   └── views/
│   │       ├── auth/
│   │       │   └── LoginView.vue
│   │       ├── courses/
│   │       │   ├── CourseListView.vue    # 课程管理
│   │       │   ├── CourseCreateView.vue  # 创建课程
│   │       │   └── CourseEditView.vue   # 编辑课程/章节/课时
│   │       ├── lessons/
│   │       │   └── LessonEditView.vue   # 课时编辑 + 视频上传
│   │       └── stats/
│   │           └── StatsView.vue         # 数据统计
│   │
│   └── admin/                    # 管理端
│       ├── api/
│       │   └── adminApi.ts       # 管理端 API
│       └── views/
│           ├── auth/
│           │   └── LoginView.vue  # 管理员登录
│           ├── DashboardView.vue  # 管理后台首页
│           ├── users/
│           │   ├── UserListView.vue    # 用户管理
│           │   └── UserDetailView.vue   # 用户详情
│           ├── courses/
│           │   └── CourseListView.vue  # 课程管理
│           └── stats/
│               └── StatsView.vue        # 数据统计
│
├── index.html
├── vite.config.ts
├── tailwind.config.js
├── postcss.config.js
└── tsconfig.json
```

## 快速开始

### 环境要求

- Node.js 18+
- npm 9+ 或 pnpm 8+

### 安装依赖

```bash
cd frontend
npm install
```

### 配置环境变量

创建 `.env` 文件：

```env
VITE_API_BASE_URL=http://localhost:8080
```

### 启动开发服务器

```bash
npm run dev
```

访问 http://localhost:10420（默认端口，可能因占用而变化，请查看终端输出）

### 构建生产版本

```bash
npm run build
```

### 预览生产构建

```bash
npm run preview
```

## 功能概览

### 学生端 `/student/*`

| 路由 | 功能 |
|------|------|
| `/student/login` | 邮箱登录（发送验证码） |
| `/student/send-code` | 验证码确认 |
| `/student/courses` | 课程目录浏览 |
| `/student/courses/:id` | 课程详情 + 章节列表 |
| `/student/my-courses` | 已兑换课程 + 学习进度 |
| `/student/redeem` | 兑换码兑换 |
| `/student/learn/:courseId/:lessonId` | 视频播放学习 |

**特点**：
- 响应式设计：桌面端顶部导航，移动端底部 Tab
- 自动记住当前学习进度
- 视频状态处理（处理中/已完成/失败）

### 教师端 `/teacher/*`

| 路由 | 功能 |
|------|------|
| `/teacher/login` | 教师登录 |
| `/teacher/courses` | 课程列表管理 |
| `/teacher/courses/create` | 创建新课程 |
| `/teacher/courses/:id/edit` | 编辑课程（章节/课时） |
| `/teacher/courses/:id/lessons/:lessonId` | 课时编辑 + 视频上传 |
| `/teacher/stats` | 教学数据统计 |

**视频上传状态机**：
```
IDLE → UPLOADING → PROCESSING → READY
                  ↘ FAILED → UPLOADING (重试)
```

### 管理端 `/admin/*`

| 路由 | 功能 |
|------|------|
| `/admin/login` | 管理员登录（邮箱+密码） |
| `/admin` | 管理后台首页 + 统计概览 |
| `/admin/users` | 用户列表 + 审批 |
| `/admin/users/:id` | 用户详情 |
| `/admin/courses` | 课程管理 |
| `/admin/stats` | 平台数据统计 |

## 开发指南

### 添加新页面

1. 在对应 portal 的 `views/` 目录下创建 Vue 组件
2. 在 `common/router/index.ts` 中注册路由
3. 使用 `StudentHeader` / `TeacherHeader` 组件获取统一导航

### 添加 UI 组件

1. 在 `common/components/ui/` 目录下创建组件
2. 遵循 shadcn/ui 风格，使用 `cn()` 合并类名
3. 组件自动通过 `unplugin-vue-components` 按需导入

### API 调用

```typescript
import { studentApi } from '@/student/api/studentApi'

// 示例：获取课程列表
const courses = await studentApi.getCourses({ page: 0, size: 20 })
```

### Mock 数据

开发环境下，API 请求会被 `mockApi.ts` 拦截并返回模拟数据。无需后端即可完整演示所有功能。

## 路由守卫

| 守卫 | 说明 |
|------|------|
| `requiresAuth` | 需要登录才能访问 |
| `requiresRole` | 需要特定角色（STUDENT/TEACHER/ADMIN） |

未授权访问会重定向到对应角色的登录页。

## CSS 样式

- 使用 Tailwind CSS utility 类
- 全局样式在 `src/style.css` 中定义
- CSS 变量用于主题颜色（通过 Tailwind 配置）

## 许可证

私有项目
