# 代码规范

---

## 1. TypeScript 规范

### 1.1 类型定义

```typescript
// 使用 interface 定义对象类型
interface User {
  id: number
  email: string
  nickname: string
  role: 'STUDENT' | 'TEACHER' | 'ADMIN'
}

// 使用 type 定义联合类型或别名
type ApiResponse<T> = {
  code: number
  message: string
  data: T | null
}

// 使用 enum 定义枚举（谨慎使用）
enum CourseStatus {
  DRAFT = 'DRAFT',
  PUBLISHED = 'PUBLISHED',
}

// 使用 readonly 标记不可变字段
interface Config {
  readonly apiUrl: string
  readonly maxRetries: number
}

// 使用 Zod 进行运行时校验
import { z } from 'zod'
const UserSchema = z.object({
  id: z.number(),
  email: z.string().email(),
  role: z.enum(['STUDENT', 'TEACHER', 'ADMIN']),
})
```

### 1.2 函数类型

```typescript
// 函数签名
type QueryFn = (params: Record<string, unknown>) => Promise<any>

// 回调类型
type EventHandler = (event: Event) => void

// 可选参数
interface Props {
  onClick?: () => void
  onChange?: (value: string) => void
}
```

---

## 2. Vue 3 Composition API

### 2.1 组件定义

```vue
<script setup lang="ts">
// 1. 类型导入放最上方
import { ref, computed, onMounted } from 'vue'
import type { Course } from '@/common/types/api'

// 2. Props 定义
interface Props {
  course: Course
  editable?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  editable: false,
})

// 3. Emits 定义
const emit = defineEmits<{
  click: []
  update: [course: Course]
}>()

// 4. Composables
const { error, handleError } = useApiError()

// 5. Reactive state
const loading = ref(false)
const localCourse = ref<Course>({ ...props.course })

// 6. Computed
const isPublished = computed(() => localCourse.value.status === 'PUBLISHED')

// 7. Watchers
watch(() => props.course, (newCourse) => {
  localCourse.value = { ...newCourse }
})

// 8. Methods
function handleSave() {
  emit('update', localCourse.value)
}

// 9. Lifecycle
onMounted(() => {
  // 初始化
})
</script>

<template>
  <!-- 模板 -->
</template>

<style scoped>
/* 样式 */
</style>
```

### 2.2 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| 组件名 | PascalCase | `CourseCard.vue` |
| 组件变量 | camelCase | `const courseCard = ref()` |
| Props | camelCase | `defineProps<{ courseId: number }>()` |
| Events | kebab-case | `emit('course-selected')` |
| Composables | `use` 前缀 | `useAuth()`, `useCourse()` |

---

## 3. 文件组织

### 3.1 目录命名

| 类型 | 规范 | 示例 |
|------|------|------|
| 视图目录 | kebab-case | `course-list/` |
| 组件文件 | PascalCase | `CourseCard.vue` |
| 工具文件 | camelCase | `apiClient.ts` |
| 类型文件 | camelCase | `types/models.ts` |
| 常量文件 | camelCase | `constants.ts` |

### 3.2 导入顺序

```typescript
// 1. Vue/Core
import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'

// 2. 外部库
import { z } from 'zod'
import { format } from 'date-fns'

// 3. 内部模块（common）
import { Button } from '@/common/components/ui/button'
import { useApiError } from '@/common/composables/useApiError'

// 4. 本端模块
import { studentApi } from '@/student/api/studentApi'

// 5. 类型定义
import type { Course } from '@/common/types/api'
```

---

## 4. 样式规范

### 4.1 Tailwind CSS

```vue
<!-- 使用有意义的类名组合 -->
<template>
  <div class="p-4 bg-white rounded-lg shadow">
    <h3 class="text-lg font-semibold text-gray-900">
      {{ title }}
    </h3>
  </div>
</template>

<!-- 避免过度使用 arbitrary values -->
<!-- 建议：使用配置好的 Tailwind 主题值 -->
<div class="text-primary">...</div>

<!-- 避免： -->
<div class="text-[#123456]">...</div>
```

### 4.2 shadcn/ui 变量

```vue
<!-- 使用 design token -->
<template>
  <Button variant="default" class="bg-primary hover:bg-primary/90">
    提交
  </Button>

  <div class="text-muted-foreground">
    辅助文本
  </div>

  <Card class="border-border">
    ...
  </Card>
</template>
```

---

## 5. API 规范

### 5.1 API 文件结构

```typescript
// student/api/studentApi.ts
import { apiClient } from '@/common/utils/apiClient'
import { z } from 'zod'

// 1. Schema 定义
const CourseSchema = z.object({
  id: z.number(),
  title: z.string(),
  // ...
})

// 2. API 函数
export const studentApi = {
  async getCourses(params?: { page?: number; size?: number }) {
    return apiClient.get('/api/v1/student/courses', { params })
  },

  async getCourseDetail(id: number) {
    return apiClient.get(`/api/v1/student/courses/${id}`)
  },
}
```

### 5.2 错误处理

```typescript
// 在 composable 或 view 中处理
async function fetchCourses() {
  try {
    loading.value = true
    const data = await studentApi.getCourses()
    courses.value = data.content
  } catch (err) {
    if (err instanceof ApiError) {
      error.value = err.message
    } else {
      error.value = '网络错误'
    }
  } finally {
    loading.value = false
  }
}
```

---

## 6. Git 提交规范

### 6.1 提交信息格式

```
<type>(<scope>): <subject>

<body>

<footer>
```

### 6.2 Type 类型

| 类型 | 说明 |
|------|------|
| feat | 新功能 |
| fix | Bug 修复 |
| docs | 文档变更 |
| style | 代码格式（不影响功能） |
| refactor | 重构 |
| test | 测试相关 |
| chore | 构建/工具变更 |

### 6.3 示例

```
feat(student): add course redemption feature

- add redeem code input component
- integrate redeem API
- handle success/error states

Closes #123
```
