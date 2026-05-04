# 状态管理

---

## 1. Pinia Store 设计

### 1.1 Store 列表

| Store | 文件 | 职责 | 持久化 |
|-------|------|------|--------|
| `authStore` | `stores/auth.ts` | 用户认证状态、Token | SessionStorage |
| `userStore` | `stores/user.ts` | 用户信息、角色 | SessionStorage |
| `uiStore` | `stores/ui.ts` | 主题、语言偏好 | LocalStorage |

---

## 2. Auth Store

### 2.1 定义

```typescript
// common/stores/auth.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User } from '@/common/types/api'

export const useAuthStore = defineStore('auth', () => {
  // State
  const token = ref<string | null>(sessionStorage.getItem('token'))
  const user = ref<User | null>(null)

  // Getters
  const isAuthenticated = computed(() => !!token.value)
  const isStudent = computed(() => user.value?.role === 'STUDENT')
  const isTeacher = computed(() => user.value?.role === 'TEACHER')
  const isAdmin = computed(() => user.value?.role === 'ADMIN')

  // Actions
  function setAuth(userData: User, tokenValue: string) {
    user.value = userData
    token.value = tokenValue
    sessionStorage.setItem('token', tokenValue)
  }

  function setUser(userData: User) {
    user.value = userData
  }

  function clearAuth() {
    user.value = null
    token.value = null
    sessionStorage.removeItem('token')
  }

  return {
    token,
    user,
    isAuthenticated,
    isStudent,
    isTeacher,
    isAdmin,
    setAuth,
    setUser,
    clearAuth,
  }
})
```

### 2.2 使用

```vue
<script setup lang="ts">
import { useAuthStore } from '@/common/stores/auth'

const authStore = useAuthStore()

// 组合式 API 中使用
if (authStore.isAuthenticated) {
  // 已登录
}

// 在 setup 外使用
const isLogin = computed(() => authStore.isAuthenticated)
</script>
```

---

## 3. User Store

### 3.1 定义

```typescript
// common/stores/user.ts
import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { User } from '@/common/types/api'

export const useUserStore = defineStore('user', () => {
  const profile = ref<User | null>(null)

  function setProfile(userData: User) {
    profile.value = userData
  }

  function updateProfile(updates: Partial<User>) {
    if (profile.value) {
      profile.value = { ...profile.value, ...updates }
    }
  }

  function clearProfile() {
    profile.value = null
  }

  return {
    profile,
    setProfile,
    updateProfile,
    clearProfile,
  }
})
```

---

## 4. UI Store

### 4.1 定义

```typescript
// common/stores/ui.ts
import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

type Theme = 'light' | 'dark' | 'system'
type Language = 'zh-CN' | 'en-US'

export const useUiStore = defineStore('ui', () => {
  // State
  const theme = ref<Theme>((localStorage.getItem('theme') as Theme) || 'system')
  const language = ref<Language>((localStorage.getItem('language') as Language) || 'zh-CN')
  const sidebarCollapsed = ref(false)
  const isLoading = ref(false)

  // Watchers - 持久化
  watch(theme, (newTheme) => {
    localStorage.setItem('theme', newTheme)
    applyTheme(newTheme)
  })

  watch(language, (newLang) => {
    localStorage.setItem('language', newLang)
  })

  // Actions
  function setTheme(newTheme: Theme) {
    theme.value = newTheme
  }

  function setLanguage(newLang: Language) {
    language.value = newLang
  }

  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  function setLoading(loading: boolean) {
    isLoading.value = loading
  }

  function applyTheme(theme: Theme) {
    const root = document.documentElement
    if (theme === 'dark') {
      root.classList.add('dark')
    } else if (theme === 'light') {
      root.classList.remove('dark')
    } else {
      const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
      root.classList.toggle('dark', prefersDark)
    }
  }

  return {
    theme,
    language,
    sidebarCollapsed,
    isLoading,
    setTheme,
    setLanguage,
    toggleSidebar,
    setLoading,
  }
})
```

---

## 5. 持久化策略

### 5.1 SessionStorage vs LocalStorage

| 存储 | 用途 | 示例 |
|------|------|------|
| **SessionStorage** | 会话级数据，关闭浏览器清除 | Token、用户信息 |
| **LocalStorage** | 持久化数据，长期保存 | 主题、语言偏好 |

### 5.2 清除策略

```typescript
// 登出时清除会话数据
function logout() {
  authStore.clearAuth()
  userStore.clearProfile()
  router.push('/login')
}

// 路由切换时检查 Token 有效性
router.beforeEach(async (to, from, next) => {
  if (authStore.isAuthenticated && !authStore.user) {
    try {
      const user = await userApi.getMe()
      userStore.setProfile(user)
    } catch {
      authStore.clearAuth()
      router.push('/login')
      return
    }
  }
  next()
})
```

---

## 6. Store 最佳实践

### 6.1 禁止在 Store 中直接调用 API

```typescript
// 错误
export const useAuthStore = defineStore('auth', () => {
  async function login(email: string, code: string) {
    const response = await apiClient.post('/api/v1/student/auth/verify-code', { email, code })
    // ...
  }
})

// 正确 - 在 API 层处理
export const useAuthStore = defineStore('auth', () => {
  function setAuth(userData: User, tokenValue: string) {
    // ...
  }
})

// 在 View/Composable 中调用 API
async function handleLogin() {
  const response = await studentApi.verifyCode(email.value, code.value)
  authStore.setAuth(response.user, response.token)
}
```

### 6.2 单一数据源

每个状态只在一个 Store 中定义，避免重复。

```typescript
// user.ts - 存放用户相关状态
export const useUserStore = defineStore('user', () => {
  const profile = ref<User | null>(null)
  // ...
})

// auth.ts - 只存放认证状态，引用 userStore
export const useAuthStore = defineStore('auth', () => {
  const isAuthenticated = computed(() => !!userStore.profile)
  // ...
})
```

---

## 7. 组合式 Store

推荐使用 Composition API 风格的 Store 定义：

```typescript
// stores/counter.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useCounterStore = defineStore('counter', () => {
  // State
  const count = ref(0)

  // Getters
  const doubleCount = computed(() => count.value * 2)

  // Actions
  function increment() {
    count.value++
  }

  function decrement() {
    count.value--
  }

  function reset() {
    count.value = 0
  }

  return {
    count,
    doubleCount,
    increment,
    decrement,
    reset,
  }
})
```
