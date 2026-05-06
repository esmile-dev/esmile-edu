# 模块设计

---

## 1. 端模块划分

### 1.1 三端职责

| 端 | 路径前缀 | 角色 | 核心功能 |
|----|----------|------|----------|
| 学生端 | `/student` | STUDENT | 浏览课程、学习、兑换 |
| 教师端 | `/teacher` | TEACHER | 创建课程、上传视频、管理内容 |
| 管理端 | `/admin` | ADMIN | 用户审批、数据统计、系统管理 |

### 1.2 模块依赖关系

```
common/                    # 公共模块（无依赖）
    ├── components/ui/    # shadcn/ui 原子组件
    ├── composables/      # 通用 Composable
    ├── types/            # 共享类型定义
    └── utils/            # 工具函数

student/                   # 学生端（依赖 common）
teacher/                   # 教师端（依赖 common）
admin/                     # 管理端（依赖 common）
```

---

## 2. 学生端模块

### 2.1 功能列表

| 模块 | 功能 | 路由 |
|------|------|------|
| 认证 | 邮箱验证码登录 | `/student/login`, `/student/send-code` |
| 课程 | 浏览课程列表、查看课程详情 | `/student/courses`, `/student/courses/:id` |
| 学习 | 我的课程、学习进度 | `/student/my-courses` |
| 兑换 | 兑换码兑换课程 | `/student/redeem` |

### 2.2 目录结构

```
student/
├── views/
│   ├── auth/
│   │   ├── LoginView.vue
│   │   └── SendCodeView.vue
│   ├── courses/
│   │   ├── CourseListView.vue
│   │   └── CourseDetailView.vue
│   ├── my-courses/
│   │   └── MyCoursesView.vue
│   └── redeem/
│       └── RedeemView.vue
├── components/
│   ├── CourseCard.vue
│   ├── ChapterList.vue
│   ├── LessonItem.vue
│   └── RedeemForm.vue
├── api/
│   └── studentApi.ts
├── router/
│   └── index.ts
└── stores/
    └── auth.ts
```

---

## 3. 教师端模块

### 3.1 功能列表

| 模块 | 功能 | 路由 |
|------|------|------|
| 认证 | 邮箱验证码登录 | `/teacher/login` |
| 课程 | 课程 CRUD、发布/下线 | `/teacher/courses` |
| 章节 | 章节 CRUD | `/teacher/courses/:id` |
| 课时 | 课时 CRUD | `/teacher/chapters/:id` |
| 视频 | 视频上传、腾讯云 VOD | `/teacher/lessons/:id/upload` |

### 3.2 目录结构

```
teacher/
├── views/
│   ├── auth/
│   │   └── LoginView.vue
│   ├── courses/
│   │   ├── CourseListView.vue
│   │   ├── CourseCreateView.vue
│   │   ├── CourseEditView.vue
│   │   └── CourseDetailView.vue
│   ├── chapters/
│   │   └── ChapterEditView.vue
│   └── lessons/
│       └── LessonEditView.vue
├── components/
│   ├── ChapterEditor.vue
│   ├── LessonEditor.vue
│   ├── VideoUploader.vue
│   └── CourseForm.vue
├── api/
│   └── teacherApi.ts
├── router/
│   └── index.ts
└── stores/
    └── auth.ts
```

---

## 4. 管理端模块

### 4.1 功能列表

| 模块 | 功能 | 路由 |
|------|------|------|
| 认证 | 管理员密码登录 | `/admin/login` |
| 用户 | 用户列表、审批教师、禁用用户 | `/admin/users` |
| 课程 | 课程列表、修改课程状态 | `/admin/courses` |
| 统计 | 数据概览 | `/admin/stats` |

### 4.2 目录结构

```
admin/
├── views/
│   ├── auth/
│   │   └── LoginView.vue
│   ├── users/
│   │   ├── UserListView.vue
│   │   └── UserDetailView.vue
│   ├── courses/
│   │   └── CourseListView.vue
│   └── stats/
│       └── StatsView.vue
├── components/
│   ├── UserTable.vue
│   ├── CourseTable.vue
│   ├── StatsCard.vue
│   └── StatusBadge.vue
├── api/
│   └── adminApi.ts
├── router/
│   └── index.ts
└── stores/
    └── auth.ts
```

---

## 5. 公共模块

### 5.1 公共组件 (shadcn/ui)

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
│   ├── Table.vue
│   ├── TableHeader.vue
│   ├── TableBody.vue
│   ├── TableRow.vue
│   ├── TableHead.vue
│   └── TableCell.vue
├── badge/
├── avatar/
├── skeleton/
└── toast/
```

### 5.2 Composable

```
common/composables/
├── useApi.ts           # 通用 API 请求
├── useAuth.ts          # 认证状态
├── useToast.ts         # 消息提示
├── useConfirm.ts       # 确认对话框
├── usePagination.ts    # 分页逻辑
└── useDebounce.ts      # 防抖
```

### 5.3 类型定义

```
common/types/
├── api.ts              # API 响应类型
├── models.ts           # 业务模型类型
└── router.ts          # 路由类型
```

---

## 6. 模块间通信

### 6.1 Props / Emit（父子组件）

```vue
<!-- Parent -->
<CourseCard :course="course" @click="goToDetail" />

<!-- Child -->
<script setup lang="ts">
defineProps<{ course: Course }>()
defineEmits<{ click: [] }>()
</script>
```

### 6.2 Provide / Inject（跨层级）

```typescript
// Parent
import { provide } from 'vue'
provide('apiClient', apiClient)

// Child
import { inject } from 'vue'
const apiClient = inject('apiClient')
```

### 6.3 Pinia Store（全局状态）

```typescript
// stores/auth.ts
export const useAuthStore = defineStore('auth', () => {
  const user = ref<User | null>(null)
  const token = ref<string | null>(null)

  function setAuth(userData: User, tokenValue: string) {
    user.value = userData
    token.value = tokenValue
    sessionStorage.setItem('token', tokenValue)
  }

  return { user, token, setAuth }
})
```

---

## 7. 路由配置

### 7.1 学生端路由

```typescript
// student/router/index.ts
export const studentRoutes = [
  {
    path: '/student',
    redirect: '/student/courses',
  },
  {
    path: '/student/login',
    name: 'student-login',
    component: LoginView,
    meta: { guest: true },
  },
  {
    path: '/student/send-code',
    name: 'send-code',
    component: SendCodeView,
    meta: { guest: true },
  },
  {
    path: '/student/courses',
    name: 'student-courses',
    component: CourseListView,
    meta: { requiresAuth: true, role: 'STUDENT' },
  },
  {
    path: '/student/courses/:id',
    name: 'student-course-detail',
    component: CourseDetailView,
    meta: { requiresAuth: true, role: 'STUDENT' },
  },
  {
    path: '/student/my-courses',
    name: 'my-courses',
    component: MyCoursesView,
    meta: { requiresAuth: true, role: 'STUDENT' },
  },
  {
    path: '/student/redeem',
    name: 'redeem',
    component: RedeemView,
    meta: { requiresAuth: true, role: 'STUDENT' },
  },
]
```

### 7.2 路由守卫

```typescript
// common/router/guards.ts
export function setupRouterGuards(router: Router) {
  router.beforeEach((to, from, next) => {
    const authStore = useAuthStore()

    if (to.meta.requiresAuth && !authStore.isAuthenticated) {
      next({
        path: '/login',
        query: { redirect: to.fullPath },
      })
      return
    }

    if (to.meta.role && authStore.user?.role !== to.meta.role) {
      next({ path: '/unauthorized' })
      return
    }

    next()
  })
}
```
