# 安全规范

---

## 1. 认证安全

### 1.1 Token 存储

**推荐使用 HttpOnly Cookie**，但由于前端无法直接操作 HttpOnly Cookie，实际采用以下策略：

| 存储方式 | 安全性 | 适用场景 |
|----------|--------|----------|
| SessionStorage | 中等 | 当前会话，关闭标签页清除 |
| LocalStorage | 较低 | XSS 攻击风险 |
| HttpOnly Cookie | 高 | 需要后端配合 |

```typescript
// common/utils/apiClient.ts
// Token 存储在 SessionStorage
sessionStorage.setItem('token', tokenValue)
sessionStorage.getItem('token')
sessionStorage.removeItem('token')
```

### 1.2 Token 刷新

```typescript
// common/composables/useTokenRefresh.ts
import { ref } from 'vue'

const refreshing = ref(false)

export function useTokenRefresh() {
  async function refreshToken() {
    if (refreshing.value) return
    refreshing.value = true

    try {
      const response = await apiClient.post('/api/v1/auth/refresh')
      authStore.setAuth(response.user, response.token)
    } catch (err) {
      authStore.clearAuth()
      router.push('/login')
    } finally {
      refreshing.value = false
    }
  }

  return { refreshToken }
}
```

---

## 2. 路由守卫

### 2.1 权限检查

```typescript
// common/router/guards.ts
import { useAuthStore } from '@/common/stores/auth'

export function setupRouterGuards(router: Router) {
  router.beforeEach(async (to, from, next) => {
    const authStore = useAuthStore()

    // 需要认证的路由
    if (to.meta.requiresAuth) {
      if (!authStore.isAuthenticated) {
        next({
          path: '/login',
          query: { redirect: to.fullPath },
        })
        return
      }

      // 角色检查
      if (to.meta.role && authStore.user?.role !== to.meta.role) {
        next({ path: '/unauthorized' })
        return
      }
    }

    // 已登录访问公开路由，跳转首页
    if (to.meta.guest && authStore.isAuthenticated) {
      next({ path: '/' })
      return
    }

    next()
  })
}
```

### 2.2 路由元信息定义

```typescript
// common/types/router.ts
export interface RouteMeta {
  requiresAuth?: boolean
  guest?: boolean
  role?: 'STUDENT' | 'TEACHER' | 'ADMIN'
  title?: string
}

// 路由配置示例
{
  path: '/admin/users',
  component: UserListView,
  meta: {
    requiresAuth: true,
    role: 'ADMIN',
    title: '用户管理',
  },
}
```

---

## 3. XSS 防护

### 3.1 Vue 自动转义

Vue 默认对模板中的动态内容进行 HTML 转义：

```vue
<!-- 自动转义，不会执行 -->
<div>{{ userInput }}</div>

<!-- 危险写法，需要手动避免 -->
<div v-html="userInput"></div>
```

### 3.2 富文本内容

```typescript
// 使用 DOMPurify 净化 HTML
import DOMPurify from 'dompurify'

function sanitizeHtml(dirty: string): string {
  return DOMPurify.sanitize(dirty, {
    ALLOWED_TAGS: ['b', 'i', 'em', 'strong', 'p', 'br'],
    ALLOWED_ATTR: [],
  })
}
```

---

## 4. CSRF 防护

### 4.1 Token 头

后端使用 CSRF Token，前端在请求头中携带：

```typescript
// apiClient 请求拦截器
apiClient.interceptors.request.use((config) => {
  const csrfToken = sessionStorage.getItem('csrfToken')
  if (csrfToken) {
    config.headers['X-CSRF-Token'] = csrfToken
  }
  return config
})
```

---

## 5. 敏感数据处理

### 5.1 日志脱敏

```typescript
// 脱敏工具函数
function maskEmail(email: string): string {
  const [name, domain] = email.split('@')
  return `${name.slice(0, 2)}***@${domain}`
}

function maskPhone(phone: string): string {
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

// 错误日志中不记录敏感信息
console.error('Login failed for:', maskEmail(email))
```

### 5.2 环境变量

```
# .env.example
VITE_API_BASE_URL=http://localhost:8080
VITE_TENCENT_VOD_APP_ID=xxx

# 敏感信息不能暴露在前端
# VITE_JWT_SECRET=xxx  # 禁止
```

---

## 6. 输入校验

### 6.1 表单校验

```typescript
// common/validators/schemas.ts
import { z } from 'zod'

export const LoginSchema = z.object({
  email: z.string().email('请输入有效的邮箱地址'),
  code: z.string().length(6, '验证码为6位数字'),
})

export type LoginInput = z.infer<typeof LoginSchema>

// 组件中使用
const form = ref<LoginInput>({
  email: '',
  code: '',
})

function validateForm(): boolean {
  const result = LoginSchema.safeParse(form.value)
  if (!result.success) {
    errors.value = result.error.flatten().fieldErrors
    return false
  }
  errors.value = {}
  return true
}
```

---

## 7. 安全检查清单

| 检查项 | 说明 |
|--------|------|
| Token 存储 | 使用 SessionStorage，避免 LocalStorage |
| 路由守卫 | 所有受保护路由配置 `requiresAuth` |
| 角色检查 | 受保护路由配置 `role` 元信息 |
| 输入校验 | 使用 Zod 进行表单校验 |
| XSS 防护 | 避免使用 `v-html`，富文本净化 |
| 敏感日志 | 脱敏处理后再记录 |
| 环境变量 | 敏感信息不暴露在前端 |
