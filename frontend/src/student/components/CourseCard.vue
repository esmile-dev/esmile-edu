<script setup lang="ts">
import type { Course } from '@/common/types/api'

defineProps<{
  course: Course
}>()
</script>

<template>
  <div
    class="cursor-pointer transition-transform hover:-translate-y-1 h-full"
  >
    <Card class="overflow-hidden h-full flex flex-col">
      <div class="aspect-video bg-muted shrink-0 border-b relative">
        <img
          v-if="course.cover"
          :src="course.cover"
          :alt="course.title"
          class="w-full h-full object-cover"
        />
        <div v-else class="w-full h-full flex items-center justify-center text-muted-foreground">
          无封面
        </div>
        <Badge 
          v-if="course.enrollmentStatus === 'ACTIVE'" 
          variant="default" 
          class="absolute top-2 right-2 bg-green-500 hover:bg-green-600 text-white shadow-sm border-none pointer-events-none"
        >
          已兑换
        </Badge>
      </div>
      <CardContent class="p-4 pt-5 bg-slate-50/50 dark:bg-slate-900/20 flex-grow">
        <div class="flex items-start justify-between gap-2">
          <h3 class="font-semibold line-clamp-2">{{ course.title }}</h3>
        </div>
        <p class="text-sm text-muted-foreground mt-1">
          {{ course.educatorName || '未知讲师' }} · {{ course.chapterCount || 0 }} 章节 · {{ course.lessonCount || 0 }} 课时
        </p>
      </CardContent>
    </Card>
  </div>
</template>
