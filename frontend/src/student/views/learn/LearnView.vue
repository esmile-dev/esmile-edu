<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { studentApi } from '@/student/api/studentApi'
import type { CourseDetail, Lesson } from '@/common/types/api'
import StudentHeader from '@/student/components/StudentHeader.vue'
import VideoPlayer from '@/common/components/VideoPlayer.vue'
import { ChevronLeft, ChevronRight } from 'lucide-vue-next'

const router = useRouter()
const route = useRoute()

const courseId = Number(route.params.courseId)
const lessonId = Number(route.params.lessonId)

const course = ref<CourseDetail | null>(null)
const currentLesson = ref<Lesson | null>(null)
const loading = ref(true)
const error = ref('')
const videoError = ref('')

onMounted(async () => {
  try {
    course.value = await studentApi.getCourseDetail(courseId)
    if (lessonId && course.value?.chapters) {
      for (const chapter of course.value.chapters) {
        const lesson = chapter.lessons?.find(l => l.id === lessonId)
        if (lesson) {
          currentLesson.value = lesson
          break
        }
      }
    }
    if (!currentLesson.value && course.value?.chapters?.[0]?.lessons?.[0]) {
      currentLesson.value = course.value.chapters[0].lessons[0]
    }
  } catch (err: any) {
    error.value = err.message || '加载失败'
  } finally {
    loading.value = false
  }
})

function handleVideoError(err: Error) {
  videoError.value = err.message
}

function navigateToLesson(lesson: Lesson) {
  router.push(`/student/learn/${courseId}/${lesson.id}`)
  currentLesson.value = lesson
  videoError.value = ''
}

function getAllLessons(): Lesson[] {
  if (!course.value?.chapters) return []
  return course.value.chapters.flatMap(ch => ch.lessons || [])
}

function getNextLesson(): Lesson | null {
  const lessons = getAllLessons()
  const currentIndex = lessons.findIndex(l => l.id === currentLesson.value?.id)
  return currentIndex >= 0 && currentIndex < lessons.length - 1 ? lessons[currentIndex + 1] : null
}

function getPrevLesson(): Lesson | null {
  const lessons = getAllLessons()
  const currentIndex = lessons.findIndex(l => l.id === currentLesson.value?.id)
  return currentIndex > 0 ? lessons[currentIndex - 1] : null
}
</script>

<template>
  <div class="min-h-screen bg-background">
    <StudentHeader />

    <main class="pt-14 md:pt-16 pb-20 md:pb-0">
      <!-- Loading -->
      <div v-if="loading" class="p-8">
        <Skeleton class="aspect-video max-w-4xl mx-auto mb-4" />
        <Skeleton class="h-8 w-64 mx-auto mb-4" />
      </div>

      <!-- Error -->
      <div v-else-if="error" class="text-center py-12">
        <p class="text-destructive mb-4">{{ error }}</p>
        <Button @click="$router.back()">返回</Button>
      </div>

      <!-- Content -->
      <div v-else class="max-w-6xl mx-auto">
        <!-- Video Area -->
        <div class="bg-black">
          <VideoPlayer
            v-if="currentLesson?.videoUrl && currentLesson?.status === 'READY'"
            :video-url="currentLesson.videoUrl"
            @error="handleVideoError"
          />
          <div v-else class="w-full aspect-video flex items-center justify-center text-white">
            <div class="text-center">
              <p v-if="currentLesson?.status === 'PROCESSING'" class="text-lg">
                视频处理中，请稍候...
              </p>
              <p v-else-if="currentLesson?.status === 'FAILED'" class="text-destructive">
                视频处理失败，请联系讲师
              </p>
              <p v-else class="text-lg">
                暂无视频
              </p>
            </div>
          </div>
        </div>

        <!-- Lesson Info -->
        <div class="p-6 border-b">
          <div class="flex items-center justify-between">
            <div>
              <h1 class="text-xl font-bold">{{ currentLesson?.title }}</h1>
              <p class="text-muted-foreground">{{ course?.title }}</p>
            </div>
            <Badge :variant="currentLesson?.status === 'READY' ? 'default' : 'secondary'">
              {{ currentLesson?.status === 'READY' ? '可播放' : currentLesson?.status }}
            </Badge>
          </div>
        </div>

        <!-- Navigation -->
        <div class="p-4 flex items-center justify-between">
          <Button
            variant="outline"
            :disabled="!getPrevLesson()"
            @click="getPrevLesson() && navigateToLesson(getPrevLesson()!)"
          >
            <ChevronLeft class="w-4 h-4 mr-1" />
            上一课
          </Button>
          <Button
            :disabled="!getNextLesson()"
            @click="getNextLesson() && navigateToLesson(getNextLesson()!)"
          >
            下一课
            <ChevronRight class="w-4 h-4 ml-1" />
          </Button>
        </div>
      </div>
    </main>
  </div>
</template>
