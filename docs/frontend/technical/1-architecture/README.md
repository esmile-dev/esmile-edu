# 前端架构设计

---

## 1. 项目概述

### 1.1 项目定位

esmile-edu 是一个面向大学生的在线教育平台，前端采用 Vue 3 + Vite + shadcn/ui 构建。

### 1.2 技术栈版本

| 组件 | 版本 | 备注 |
|------|------|------|
| Vue | 3.5.x | 渐进式前端框架 |
| Vite | 6.x | 构建工具 |
| TypeScript | 5.x | 类型安全 |
| shadcn/ui | latest | UI 组件库 |
| Tailwind CSS | 4.x | 原子化 CSS |
| Vue Router | 4.x | 路由管理 |
| Pinia | 2.x | 状态管理 |
| Axios | 1.x | HTTP 客户端 |
| Zod | 3.x |运行时类型校验 |

---

## 2. 多端应用架构

### 2.1 端划分

| 端 | 路径 | 说明 |
|----|------|------|
| 学生端 | `/student` | 查看课程、兑换码、学习 |
| 教师端 | `/teacher` | 课程管理、视频上传 |
| 管理端 | `/admin` | 用户管理、数据统计 |

### 2.2 目录结构

```
frontend/src/
├── student/                # 学生端
│   ├── views/
│   │   ├── auth/
│   │   │   ├── LoginView.vue
│   │   │   └── SendCodeView.vue
│   │   ├── courses/
│   │   │   ├── CourseListView.vue
│   │   │   └── CourseDetailView.vue
│   │   ├── my-courses/
│   │   │   └── MyCoursesView.vue
│   │   └── redeem/
│   │       └── RedeemView.vue
│   ├── components/
│   │   ├── CourseCard.vue
│   │   └── ChapterList.vue
│   ├── api/
│   │   └── studentApi.ts
│   ├── router/
│   │   └── index.ts
│   └── stores/
│       └── auth.ts
│
├── teacher/               # 教师端
│   ├── views/
│   │   ├── auth/
│   │   ├── courses/
│   │   │   ├── CourseListView.vue
│   │   │   ├── CourseCreateView.vue
│   │   │   ├── CourseEditView.vue
│   │   │   └── CourseDetailView.vue
│   │   ├── chapters/
│   │   └── lessons/
│   ├── components/
│   │   ├── ChapterEditor.vue
│   │   ├── LessonEditor.vue
│   │   └── VideoUploader.vue
│   ├── api/
│   │   └── teacherApi.ts
│   └── stores/
│
├── admin/                 # 管理端
│   ├── views/
│   │   ├── auth/
│   │   ├── users/
│   │   │   └── UserListView.vue
│   │   ├── courses/
│   │   │   └── CourseListView.vue
│   │   └── stats/
│   │       └── StatsView.vue
│   ├── components/
│   │   ├── UserTable.vue
│   │   └── StatsCard.vue
│   ├── api/
│   │   └── adminApi.ts
│   └── stores/
│
└── common/               # 公共组件
    ├── components/
    │   └── ui/          # shadcn/ui 组件
    ├── composables/
    │   ├── useApi.ts
    │   ├── useAuth.ts
    │   └── useToast.ts
    ├── types/
    │   ├── api.ts
    │   └── models.ts
    └── utils/
        ├── apiClient.ts
        └── validators.ts
```

---

## 3. 分层架构

### 3.1 分层职责

| 层级 | 职责 | 技术方案 |
|------|------|----------|
| **View** | 页面渲染、用户交互 | Vue SFC |
| **Component** | 可复用 UI 组件 | Vue SFC |
| **Composable** | 业务逻辑复用 | Composition API |
| **Store** | 客户端状态管理 | Pinia |
| **API Client** | 后端通信 | Axios + Zod |

### 3.2 数据流向

```
User Interaction
      ↓
View (Template + Reactive State)
      ↓
Composable (Business Logic)
      ↓
Store (Global State) ←→ API Client (HTTP)
      ↓
Axios → Backend API
```

### 3.3 依赖规则

**单向依赖，禁止循环**：
```
view → composable → store
                  → api client
```

---

## 4. 状态管理

### 4.1 Store 设计

| Store | 职责 | 持久化 |
|-------|------|--------|
| `authStore` | 用户登录状态、Token | SessionStorage |
| `userStore` | 用户信息、角色 | SessionStorage |
| `uiStore` | 主题、语言偏好 | LocalStorage |

### 4.2 Token 管理

```typescript
// common/utils/apiClient.ts
const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
})

// 请求拦截器：注入 Token
apiClient.interceptors.request.use((config) => {
  const token = sessionStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器：处理 401
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      // 清除 Token，跳转登录
      sessionStorage.removeItem('token')
      router.push('/login')
    }
    return Promise.reject(error)
  }
)
```

---

## 5. 组件设计

### 5.1 shadcn/ui 使用规范

```
common/components/ui/
├── button/
│   ├── Button.vue
│   └── index.ts
├── input/
├── card/
├── dialog/
├── dropdown-menu/
├── table/
└── ...
```

**使用方式**：
```vue
<script setup lang="ts">
import { Button } from '@/common/components/ui/button'
</script>

<template>
  <Button variant="default">提交</Button>
</template>
```

### 5.2 组件分类

| 类型 | 存放位置 | 示例 |
|------|----------|------|
| **UI 原子组件** | `common/components/ui/` | Button, Input, Card |
| **业务组件** | `{端}/components/` | CourseCard, ChapterList |
| **页面组件** | `{端}/views/` | CourseListView |

---

## 6. 路由设计

### 6.1 路由结构

```typescript
// student/router/index.ts
const routes = [
  // 公开路由
  { path: '/student/login', component: LoginView },
  { path: '/student/send-code', component: SendCodeView },

  // 受保护路由
  { path: '/student/courses', component: CourseListView, meta: { requiresAuth: true } },
  { path: '/student/courses/:id', component: CourseDetailView, meta: { requiresAuth: true } },
  { path: '/student/my-courses', component: MyCoursesView, meta: { requiresAuth: true } },
  { path: '/student/redeem', component: RedeemView, meta: { requiresAuth: true } },
]
```

### 6.2 路由守卫

```typescript
// common/router/guards.ts
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next({ path: '/student/login', query: { redirect: to.fullPath } })
  } else {
    next()
  }
})
```

---

## 7. API 客户端设计

### 7.1 API 响应类型

```typescript
// common/types/api.ts
export interface ApiResponse<T> {
  code: number
  message: string
  data: T | null
}

export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}
```

### 7.2 Zod 校验

```typescript
// common/validators/schemas.ts
import { z } from 'zod'

export const CourseSchema = z.object({
  id: z.number(),
  title: z.string(),
  description: z.string().nullable(),
  coverImage: z.string().url().nullable(),
  educatorName: z.string(),
  chapterCount: z.number(),
  lessonCount: z.number(),
})

export type Course = z.infer<typeof CourseSchema>
```

### 7.3 API 封装示例

```typescript
// student/api/studentApi.ts
import { apiClient } from '@/common/utils/apiClient'
import { CourseSchema, type Course } from '@/common/validators/schemas'

export const studentApi = {
  async getCourses(params: { page?: number; size?: number }) {
    const response = await apiClient.get('/api/v1/student/courses', { params })
    const data = await response.data
    return {
      ...data,
      data: {
        ...data.data,
        content: z.array(CourseSchema).parse(data.data.content),
      },
    }
  },
}
```

---

## 8. 关键文件索引

| 文件 | 说明 |
|------|------|
| [2-modules/README.md](../2-modules/README.md) | 模块设计 |
| [3-state-management/README.md](../3-state-management/README.md) | 状态管理 |
| [4-api-client/README.md](../4-api-client/README.md) | API 客户端 |
| [5-components/README.md](../5-components/README.md) | 组件设计 |
