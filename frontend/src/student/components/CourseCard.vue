<script setup lang="ts">
import type { Course } from '@/common/types/api'

defineProps<{
  course: Course
}>()
</script>

<template>
  <div
    class="cursor-pointer transition-transform hover:-translate-y-1"
  >
    <Card class="overflow-hidden">
      <div class="aspect-video bg-muted relative">
        <img
          v-if="course.coverImage"
          :src="course.coverImage"
          :alt="course.title"
          class="w-full h-full object-cover"
        />
        <div v-else class="w-full h-full flex items-center justify-center text-muted-foreground">
          无封面
        </div>
      </div>
      <CardContent class="p-4">
        <h3 class="font-semibold line-clamp-2 mb-1">{{ course.title }}</h3>
        <p class="text-sm text-muted-foreground mb-2">
          {{ course.educatorName || course.educator?.nickname || '未知讲师' }}
        </p>
        <div class="flex gap-2">
          <Badge variant="secondary" class="text-xs">
            {{ course.chapterCount || 0 }} 章节
          </Badge>
          <Badge variant="secondary" class="text-xs">
            {{ course.lessonCount || 0 }} 课时
          </Badge>
        </div>
      </CardContent>
    </Card>
  </div>
</template>
