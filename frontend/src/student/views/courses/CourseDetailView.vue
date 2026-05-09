<script setup lang="ts">
import { ref, computed, onMounted, onUpdated } from 'vue'
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
    } else if (course.value.currentLessonId) {
      currentLessonId.value = course.value.currentLessonId
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

const progressPercent = computed(() => {
  if (!course.value?.chapters) return 0
  let totalLessons = 0
  let completedLessons = 0
  course.value.chapters.forEach(ch => {
    if (ch.lessons) {
      totalLessons += ch.lessons.length
      completedLessons += ch.lessons.filter(l => l.isCompleted).length
    }
  })
  if (totalLessons === 0) return 0
  return Math.round((completedLessons / totalLessons) * 100)
})
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
            <Card class="sticky top-24 border shadow-sm rounded-xl bg-white dark:bg-slate-900">
              <CardContent class="p-6 pt-7 space-y-6">
                <!-- Instructor & Progress -->
                <div class="flex items-center justify-between">
                  <div class="flex items-center gap-4">
                    <Avatar class="h-16 w-16 shadow-sm border border-slate-100 dark:border-slate-800">
                      <AvatarImage
                        v-if="course.educatorAvatar"
                        :src="course.educatorAvatar"
                        :alt="course.educatorName"
                        class="object-cover"
                      />
                      <AvatarFallback class="bg-[#0A1930] text-white text-2xl font-medium">
                        {{ getInitials(course.educatorName || 'T') }}
                      </AvatarFallback>
                    </Avatar>
                    <div class="flex flex-col justify-center">
                      <p class="text-xl font-bold text-foreground">{{ course.educatorName }}</p>
                      <p class="text-sm text-muted-foreground mt-1">讲师</p>
                    </div>
                  </div>

                  <!-- Progress Circle -->
                  <div v-if="course.enrollmentStatus === 'ACTIVE'" class="relative flex items-center justify-center w-16 h-16" title="课程进度">
                    <svg class="w-full h-full transform -rotate-90" viewBox="0 0 36 36">
                      <!-- Background Circle -->
                      <path
                        class="text-slate-100 dark:text-slate-800"
                        stroke-width="3"
                        stroke="currentColor"
                        fill="none"
                        d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                      />
                      <!-- Progress Circle -->
                      <path
                        class="text-blue-600 transition-all duration-1000 ease-out"
                        stroke-width="3"
                        stroke-dasharray="100, 100"
                        :stroke-dashoffset="100 - progressPercent"
                        stroke-linecap="round"
                        stroke="currentColor"
                        fill="none"
                        d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                      />
                    </svg>
                    <div class="absolute flex flex-col items-center justify-center text-xs font-bold text-blue-600">
                      {{ progressPercent }}%
                    </div>
                  </div>
                </div>

                <!-- Stats -->
                <div class="flex items-center gap-6 text-sm font-medium text-muted-foreground">
                  <div class="flex items-baseline gap-1.5">
                    <span class="text-lg font-bold text-slate-700 dark:text-slate-200">{{ course.chapters?.length || 0 }}</span>
                    <span>章节</span>
                  </div>
                  <div class="flex items-baseline gap-1.5">
                    <span class="text-lg font-bold text-slate-700 dark:text-slate-200">{{ course.chapters?.reduce((sum, ch) => sum + (ch.lessons?.length || 0), 0) || 0 }}</span>
                    <span>课时</span>
                  </div>
                </div>

                <!-- Action -->
                <div v-if="course.enrollmentStatus === 'ACTIVE'" class="pt-2">
                  <Button class="w-full h-12 rounded-lg text-base font-medium shadow-sm bg-[#0A1930] text-white hover:bg-[#0A1930]/90" @click="handleContinue">
                    继续学习
                  </Button>
                  <p class="text-center text-xs text-muted-foreground mt-3">
                    有效期至: {{ course.enrollmentExpiresAt || '永久' }}
                  </p>
                </div>
                <div v-else class="pt-2">
                  <Button class="w-full h-12 rounded-lg text-base font-medium shadow-sm bg-[#0A1930] text-white hover:bg-[#0A1930]/90" @click="handleEnroll">
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
