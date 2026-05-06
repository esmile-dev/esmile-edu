<script setup lang="ts">
import { ref, onMounted, onUpdated } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { studentApi } from '@/student/api/studentApi'
import type { CourseDetail } from '@/common/types/api'
import StudentHeader from '@/student/components/StudentHeader.vue'
import ChapterList from '@/student/components/ChapterList.vue'

const router = useRouter()
const route = useRoute()

const courseId = Number(route.params.id)
const course = ref<CourseDetail | null>(null)
const loading = ref(true)
const error = ref('')
const currentLessonId = ref<number | null>(null)

onMounted(async () => {
  try {
    course.value = await studentApi.getCourseDetail(courseId)
    // Check if coming from "My Courses" with a specific lesson to scroll to
    if (route.query.lesson) {
      currentLessonId.value = Number(route.query.lesson)
    }
  } catch (err: any) {
    error.value = err.message || '加载课程失败'
  } finally {
    loading.value = false
  }
})

// Scroll to current lesson after content loads
onUpdated(() => {
  if (currentLessonId.value && !loading.value) {
    const element = document.getElementById(`lesson-${currentLessonId.value}`)
    if (element) {
      element.scrollIntoView({ behavior: 'smooth', block: 'center' })
    }
  }
})

function handleEnroll() {
  router.push('/student/redeem')
}

function handleContinue() {
  // If we have a current lesson from progress, go there
  if (currentLessonId.value) {
    router.push(`/student/learn/${courseId}/${currentLessonId.value}`)
    return
  }
  // Otherwise go to first lesson
  if (course.value?.chapters?.[0]?.lessons?.[0]) {
    const lesson = course.value.chapters[0].lessons[0]
    router.push(`/student/learn/${courseId}/${lesson.id}`)
  }
}

function getInitials(name: string) {
  return name ? name.charAt(0).toUpperCase() : 'T'
}
</script>

<template>
  <div class="min-h-screen bg-background">
    <StudentHeader />

    <main class="pt-14 md:pt-16 pb-20 md:pb-0">
      <!-- Loading -->
      <div v-if="loading" class="max-w-7xl mx-auto px-4 py-8">
        <Skeleton class="h-8 w-64 mb-4" />
        <Skeleton class="h-4 w-full mb-2" />
        <Skeleton class="h-4 w-3/4" />
      </div>

      <!-- Error -->
      <div v-else-if="error" class="max-w-7xl mx-auto px-4 py-8 text-center">
        <p class="text-destructive mb-4">{{ error }}</p>
        <Button @click="$router.back()">返回</Button>
      </div>

      <!-- Content -->
      <div v-else-if="course" class="max-w-7xl mx-auto px-4 py-8">
        <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
          <!-- Main Content -->
          <div class="lg:col-span-2 space-y-6">
            <!-- Cover -->
            <div class="aspect-video bg-muted rounded-lg overflow-hidden">
              <img
                v-if="course.cover"
                :src="course.cover"
                :alt="course.title"
                class="w-full h-full object-cover"
              />
            </div>

            <!-- Title -->
            <h1 class="text-2xl font-bold">{{ course.title }}</h1>

            <!-- Description -->
            <div v-if="course.description">
              <h2 class="text-lg font-semibold mb-2">课程介绍</h2>
              <p class="text-muted-foreground">{{ course.description }}</p>
            </div>

            <!-- Chapters -->
            <div>
              <h2 class="text-lg font-semibold mb-4">课程目录</h2>
              <ChapterList
                v-if="course.chapters?.length"
                :chapters="course.chapters"
                :enrolled="!!course.enrollmentStatus"
                @select-lesson="(lesson) => router.push(`/student/learn/${courseId}/${lesson.id}`)"
              />
              <p v-else class="text-muted-foreground">暂无章节</p>
            </div>
          </div>

          <!-- Sidebar -->
          <div>
            <Card class="sticky top-24">
              <CardContent class="p-6 space-y-4">
                <!-- Instructor -->
                <div class="flex items-center gap-3">
                  <Avatar>
                    <AvatarFallback class="bg-primary text-primary-foreground">
                      {{ getInitials(course.educator?.nickname || 'T') }}
                    </AvatarFallback>
                  </Avatar>
                  <div>
                    <p class="font-medium">{{ course.educator?.nickname }}</p>
                    <p class="text-sm text-muted-foreground">讲师</p>
                  </div>
                </div>

                <!-- Stats -->
                <div class="flex gap-4 text-sm">
                  <span class="text-muted-foreground">
                    {{ course.chapters?.length || 0 }} 章节
                  </span>
                  <span class="text-muted-foreground">
                    {{ course.chapters?.reduce((sum, ch) => sum + (ch.lessons?.length || 0), 0) || 0 }} 课时
                  </span>
                </div>

                <!-- Action -->
                <div v-if="course.enrollmentStatus === 'ACTIVE'">
                  <Button class="w-full" @click="handleContinue">
                    继续学习
                  </Button>
                  <p class="text-center text-sm text-muted-foreground mt-2">
                    有效期至: {{ course.enrollmentExpiresAt || '永久' }}
                  </p>
                </div>
                <div v-else>
                  <Button class="w-full" @click="handleEnroll">
                    立即兑换
                  </Button>
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>
