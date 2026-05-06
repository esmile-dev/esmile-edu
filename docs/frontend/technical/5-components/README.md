# 组件设计

---

## 1. 组件分层

### 1.1 三层架构

| 层级 | 目录 | 说明 | 示例 |
|------|------|------|------|
| **UI 原子组件** | `common/components/ui/` | shadcn/ui 基础组件 | Button, Input, Card |
| **业务组件** | `{端}/components/` | 领域特定组件 | CourseCard, ChapterList |
| **页面组件** | `{端}/views/` | 路由页面 | CourseListView |

### 1.2 依赖关系

```
页面组件 (View)
    ↓ 依赖
业务组件 (Business Component)
    ↓ 依赖
UI 原子组件 (shadcn/ui)
```

---

## 2. shadcn/ui 组件

### 2.1 组件列表

```
common/components/ui/
├── button/
│   ├── Button.vue
│   └── index.ts
├── input/
│   ├── Input.vue
│   └── index.ts
├── label/
│   ├── Label.vue
│   └── index.ts
├── card/
│   ├── Card.vue
│   ├── CardHeader.vue
│   ├── CardContent.vue
│   ├── CardFooter.vue
│   └── index.ts
├── dialog/
│   ├── Dialog.vue
│   ├── DialogTrigger.vue
│   ├── DialogContent.vue
│   ├── DialogHeader.vue
│   ├── DialogFooter.vue
│   └── index.ts
├── dropdown-menu/
│   ├── DropdownMenu.vue
│   ├── DropdownMenuTrigger.vue
│   ├── DropdownMenuContent.vue
│   ├── DropdownMenuItem.vue
│   └── index.ts
├── table/
│   ├── Table.vue
│   ├── TableHeader.vue
│   ├── TableBody.vue
│   ├── TableRow.vue
│   ├── TableHead.vue
│   ├── TableCell.vue
│   └── index.ts
├── badge/
│   ├── Badge.vue
│   └── index.ts
├── avatar/
│   ├── Avatar.vue
│   ├── AvatarImage.vue
│   ├── AvatarFallback.vue
│   └── index.ts
├── skeleton/
│   ├── Skeleton.vue
│   └── index.ts
├── toast/
│   ├── Toast.vue
│   ├── Toaster.vue
│   └── index.ts
└── scroll-area/
    ├── ScrollArea.vue
    └── index.ts
```

### 2.2 使用示例

```vue
<script setup lang="ts">
import { Button } from '@/common/components/ui/button'
import { Input } from '@/common/components/ui/input'
import { Card, CardHeader, CardContent } from '@/common/components/ui/card'
</script>

<template>
  <Card>
    <CardHeader>
      <h3>课程信息</h3>
    </CardHeader>
    <CardContent>
      <Input placeholder="输入课程名称" />
      <Button>创建课程</Button>
    </CardContent>
  </Card>
</template>
```

---

## 3. 业务组件

### 3.1 CourseCard

```vue
<!-- student/components/CourseCard.vue -->
<script setup lang="ts">
import { Card, CardContent, CardFooter } from '@/common/components/ui/card'
import { Badge } from '@/common/components/ui/badge'
import { RouterLink } from 'vue-router'

interface Course {
  id: number
  title: string
  description: string | null
  coverImage: string | null
  educatorName: string
  chapterCount: number
  lessonCount: number
}

defineProps<{
  course: Course
}>()
</script>

<template>
  <RouterLink :to="`/student/courses/${course.id}`">
    <Card class="hover:shadow-lg transition-shadow">
      <img
        v-if="course.coverImage"
        :src="course.coverImage"
        :alt="course.title"
        class="w-full h-40 object-cover rounded-t-lg"
      />
      <CardContent class="p-4">
        <h3 class="font-semibold line-clamp-2">{{ course.title }}</h3>
        <p class="text-sm text-muted-foreground mt-1">
          {{ course.educatorName }}
        </p>
        <div class="flex gap-2 mt-2">
          <Badge variant="secondary">
            {{ course.chapterCount }} 章节
          </Badge>
          <Badge variant="secondary">
            {{ course.lessonCount }} 课时
          </Badge>
        </div>
      </CardContent>
    </Card>
  </RouterLink>
</template>
```

### 3.2 ChapterList

```vue
<!-- student/components/ChapterList.vue -->
<script setup lang="ts">
import { Card, CardHeader, CardContent } from '@/common/components/ui/card'
import { ChevronDown, ChevronUp, Play } from 'lucide-vue-next'

interface Lesson {
  id: number
  title: string
  duration: number | null
  orderNum: number
  status: 'PROCESSING' | 'READY' | 'FAILED'
}

interface Chapter {
  id: number
  title: string
  orderNum: number
  lessons: Lesson[]
}

defineProps<{
  chapters: Chapter[]
  enrolled?: boolean
}>()

const emit = defineEmits<{
  selectLesson: [lesson: Lesson]
}>()

function formatDuration(seconds: number | null): string {
  if (!seconds) return ''
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins}:${secs.toString().padStart(2, '0')}`
}
</script>

<template>
  <div class="space-y-4">
    <div v-for="chapter in chapters" :key="chapter.id">
      <Card>
        <CardHeader class="py-3">
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-2">
              <span class="font-medium">{{ chapter.orderNum }}.</span>
              <span>{{ chapter.title }}</span>
            </div>
          </div>
        </CardHeader>
        <CardContent class="py-2">
          <ul class="space-y-2">
            <li
              v-for="lesson in chapter.lessons"
              :key="lesson.id"
              class="flex items-center justify-between p-2 rounded hover:bg-muted/50 cursor-pointer"
              @click="emit('selectLesson', lesson)"
            >
              <div class="flex items-center gap-3">
                <Play class="w-4 h-4 text-muted-foreground" />
                <span>{{ lesson.title }}</span>
              </div>
              <div class="flex items-center gap-2 text-sm text-muted-foreground">
                <Badge
                  v-if="lesson.status !== 'READY'"
                  :variant="lesson.status === 'PROCESSING' ? 'outline' : 'destructive'"
                >
                  {{ lesson.status === 'PROCESSING' ? '处理中' : '失败' }}
                </Badge>
                <span v-if="lesson.duration">
                  {{ formatDuration(lesson.duration) }}
                </span>
              </div>
            </li>
          </ul>
        </CardContent>
      </Card>
    </div>
  </div>
</template>
```

---

## 4. 组件 Props 设计

### 4.1 Props 类型定义

```typescript
// 使用 TypeScript interface 定义 Props
interface Props {
  // 基础类型
  title: string
  count?: number

  // 联合类型
  status: 'draft' | 'published'

  // 复杂类型
  course: Course

  // 数组类型
  items: string[]

  // 函数类型
  onClick?: () => void
  onChange?: (value: string) => void
}

// 带默认值的 Props
const props = withDefaults(defineProps<Props>(), {
  count: 0,
  status: 'draft',
})
```

### 4.2 Props 校验

```typescript
// 使用 Zod 进行运行时校验
import { z } from 'zod'

const CourseCardPropsSchema = z.object({
  course: z.object({
    id: z.number(),
    title: z.string(),
    coverImage: z.string().nullable(),
  }),
})

type CourseCardProps = z.infer<typeof CourseCardPropsSchema>
```

---

## 5. 组件样式

### 5.1 Tailwind CSS 规范

```vue
<!-- 使用 Tailwind CSS 类名 -->
<template>
  <div class="p-4 bg-white rounded-lg shadow">
    <h3 class="text-lg font-semibold text-gray-900">
      {{ title }}
    </h3>
  </div>
</template>

<!-- 使用 shadcn/ui 的 CSS 变量 -->
<template>
  <Button variant="default" class="bg-primary hover:bg-primary/90">
    提交
  </Button>
</template>
```

### 5.2 CSS 变量（shadcn/ui）

```css
/* 主题变量 */
:root {
  --background: 0 0% 100%;
  --foreground: 222.2 84% 4.9%;
  --primary: 222.2 47.4% 11.2%;
  --primary-foreground: 210 40% 98%;
  /* ... */
}
```

---

## 6. 组件测试

### 6.1 Vitest 测试示例

```typescript
// components/__tests__/CourseCard.test.ts
import { render, screen } from '@testing-library/vue'
import { describe, it, expect } from 'vitest'
import CourseCard from '../CourseCard.vue'

describe('CourseCard', () => {
  it('renders course title', () => {
    const mockCourse = {
      id: 1,
      title: 'Java 入门教程',
      description: '适合零基础学员',
      coverImage: null,
      educatorName: 'Teacher Zhang',
      chapterCount: 5,
      lessonCount: 30,
    }

    render(CourseCard, {
      props: { course: mockCourse },
    })

    expect(screen.getByText('Java 入门教程')).toBeDefined()
  })
})
```

---

## 7. 组件文档

### 7.1 使用示例

每个业务组件应包含：

1. **Props 接口** - 类型定义
2. **使用示例** - 基础用法
3. **事件说明** - Emit 定义
4. **Slots** - 插槽定义

```typescript
/**
 * CourseCard - 课程卡片组件
 *
 * @example
 * ```vue
 * <CourseCard :course="course" @click="handleClick" />
 * ```
 *
 * @props
 * - course: Course - 课程数据
 *
 * @events
 * - click: 点击卡片时触发
 */
```
