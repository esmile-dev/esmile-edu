<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { studentApi } from '@/student/api/studentApi'
import type { MyCourse, PageResponse } from '@/common/types/api'
import StudentHeader from '@/student/components/StudentHeader.vue'

const router = useRouter()

const courses = ref<MyCourse[]>([])
const loading = ref(true)
const error = ref('')
const page = ref(0)
const size = ref(20)
const totalElements = ref(0)

onMounted(async () => {
  await fetchCourses()
})

async function fetchCourses() {
  loading.value = true
  error.value = ''
  try {
    const response = await studentApi.getMyCourses({ page: page.value, size: size.value })
    courses.value = response.content || []
    totalElements.value = response.totalElements || 0
  } catch (err: any) {
    error.value = err.message || '加载失败'
  } finally {
    loading.value = false
  }
}

function goToCourse(course: any) {
  if (course.currentLessonId) {
    router.push(`/student/courses/${course.id}?lesson=${course.currentLessonId}`)
  } else {
    router.push(`/student/courses/${course.id}`)
  }
}
</script>

<template>
  <div class="min-h-screen bg-background">
    <StudentHeader />

    <main class="pt-14 md:pt-16 pb-20 md:pb-0 max-w-7xl mx-auto px-4 py-8">
      <h1 class="text-2xl font-bold mb-6">我的课程</h1>

      <!-- Loading -->
      <div v-if="loading" class="space-y-4">
        <div v-for="i in 3" :key="i" class="flex gap-4">
          <Skeleton class="h-24 w-40 rounded-lg" />
          <div class="flex-1 space-y-2">
            <Skeleton class="h-4 w-48" />
            <Skeleton class="h-4 w-32" />
            <Skeleton class="h-2 w-full" />
          </div>
        </div>
      </div>

      <!-- Error -->
      <div v-else-if="error" class="text-center py-12">
        <p class="text-destructive mb-4">{{ error }}</p>
        <Button @click="fetchCourses">重试</Button>
      </div>

      <!-- Course List -->
      <div v-else-if="courses.length > 0" class="space-y-4">
        <div
          v-for="course in courses"
          :key="course.id"
          class="flex gap-4 p-4 border rounded-lg hover:bg-muted/50 cursor-pointer"
          @click="goToCourse(course)"
        >
          <div class="w-40 h-24 bg-muted rounded-lg overflow-hidden shrink-0">
            <img
              v-if="course.coverImage"
              :src="course.coverImage"
              :alt="course.title"
              class="w-full h-full object-cover"
            />
          </div>
          <div class="flex-1 min-w-0">
            <h3 class="font-semibold truncate">{{ course.title }}</h3>
            <p class="text-sm text-muted-foreground">
              有效期至: {{ course.enrollmentExpiresAt || '永久' }}
            </p>
            <div class="mt-2 flex items-center gap-4">
              <div class="flex-1">
                <div class="h-2 bg-muted rounded-full overflow-hidden">
                  <div
                    class="h-full bg-primary transition-all"
                    :style="{ width: `${course.progress}%` }"
                  />
                </div>
              </div>
              <Badge :variant="course.progress >= 100 ? 'default' : 'secondary'">
                {{ course.progress >= 100 ? '已完成' : `${course.progress}%` }}
              </Badge>
            </div>
          </div>
        </div>
      </div>

      <!-- Empty -->
      <div v-else class="text-center py-12">
        <p class="text-muted-foreground mb-4">暂无已兑换的课程</p>
        <Button @click="router.push('/student/courses')">去浏览课程</Button>
      </div>
    </main>
  </div>
</template>
