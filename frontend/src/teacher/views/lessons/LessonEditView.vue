<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { teacherApi } from '@/teacher/api/teacherApi'
import type { Lesson } from '@/common/types/api'
import TeacherHeader from '@/teacher/components/TeacherHeader.vue'
import VideoUploader from '@/teacher/components/VideoUploader.vue'
import { useLessonStatus } from '@/common/composables/useLessonStatus'
import { ArrowLeft } from 'lucide-vue-next'

const router = useRouter()
const route = useRoute()

const courseId = Number(route.params.courseId)
const lessonId = Number(route.params.lessonId)

const lesson = ref<Lesson | null>(null)
const lessonTitle = ref('')
const loading = ref(true)
const error = ref('')
const currentVideoUrl = ref<string | undefined>(undefined)

// Lesson status polling
const lessonIdRef = ref(lessonId)
const { status, startPolling, stopPolling, isReady, isProcessing, isFailed } = useLessonStatus(lessonIdRef, {
  interval: 5000,
  maxAttempts: 60,
  immediate: false
})

// Watch for status changes
watch(status, (newStatus) => {
  if (lesson.value) {
    lesson.value.status = newStatus as 'PROCESSING' | 'READY' | 'FAILED'
    if (newStatus === 'READY' && lesson.value.videoUrl) {
      stopPolling()
    }
  }
})

// Fetch lesson data on mount
watch(lessonIdRef, async () => {
  await fetchLesson()
}, { immediate: true })

async function fetchLesson() {
  loading.value = true
  error.value = ''
  try {
    const course = await teacherApi.getCourseDetail(courseId)
    const found = course.chapters
      .flatMap((c: any) => c.lessons)
      .find((l: any) => l.id === lessonId)

    if (found) {
      lesson.value = found
      lessonTitle.value = found.title
      currentVideoUrl.value = found.videoUrl
    } else {
      error.value = '课时未找到'
    }
  } catch (err: any) {
    error.value = err.message || '加载失败'
  } finally {
    loading.value = false
  }
}

function goBack() {
  router.push(`/teacher/courses/${courseId}/edit`)
}

function handleUploaded(videoId: string, videoUrl: string) {
  if (lesson.value) {
    lesson.value.videoId = videoId
    lesson.value.videoUrl = videoUrl
    currentVideoUrl.value = videoUrl
  }
  // Start polling to wait for processing
  lessonIdRef.value = lessonId
  startPolling()
}

function handleStatusChange(newStatus: 'IDLE' | 'UPLOADING' | 'PROCESSING' | 'READY' | 'FAILED') {
  if (newStatus === 'PROCESSING') {
    startPolling()
  } else {
    stopPolling()
    if (lesson.value && ['PROCESSING', 'READY', 'FAILED'].includes(newStatus)) {
      lesson.value.status = newStatus as 'PROCESSING' | 'READY' | 'FAILED'
    }
  }
}

function handleUploadError(err: Error) {
  console.error('Upload error:', err)
}

async function handleUpdateTitle() {
  if (!lessonTitle.value.trim()) return
  try {
    await teacherApi.updateLesson(lessonId, { title: lessonTitle.value })
  } catch (err: any) {
    alert(err.message || '更新失败')
  }
}
</script>

<template>
  <div class="min-h-screen bg-background">
    <TeacherHeader />

    <main class="pt-16 max-w-3xl mx-auto px-4 py-8">
      <!-- Back Button -->
      <Button variant="ghost" @click="goBack" class="mb-4">
        <ArrowLeft class="w-4 h-4 mr-1" />
        返回课程
      </Button>

      <!-- Loading -->
      <div v-if="loading">
        <Skeleton class="h-8 w-48 mb-4" />
        <Skeleton class="h-4 w-full mb-2" />
        <Skeleton class="h-4 w-3/4" />
      </div>

      <!-- Error -->
      <div v-else-if="error" class="text-center py-12">
        <p class="text-destructive mb-4">{{ error }}</p>
        <Button @click="goBack">返回</Button>
      </div>

      <!-- Content -->
      <div v-else-if="lesson">
        <div class="flex items-center justify-between mb-6">
          <h1 class="text-2xl font-bold">课时编辑</h1>
          <Badge :variant="isReady ? 'default' : isFailed ? 'destructive' : 'secondary'">
            {{ isReady ? '已完成' : isFailed ? '失败' : isProcessing ? '处理中' : '未上传' }}
          </Badge>
        </div>

        <div class="space-y-6">
          <!-- Title -->
          <Card>
            <CardHeader>
              <CardTitle class="text-base">课时信息</CardTitle>
            </CardHeader>
            <CardContent class="space-y-4">
              <div class="space-y-2">
                <Label for="title">课时标题</Label>
                <div class="flex gap-2">
                  <Input
                    id="title"
                    v-model="lessonTitle"
                    placeholder="输入课时标题"
                  />
                  <Button @click="handleUpdateTitle">保存</Button>
                </div>
              </div>
            </CardContent>
          </Card>

          <!-- Video Upload -->
          <Card>
            <CardHeader>
              <CardTitle class="text-base">视频上传</CardTitle>
            </CardHeader>
            <CardContent class="space-y-4">
              <VideoUploader
                :lesson-id="lessonId"
                :model-value="currentVideoUrl"
                :video-status="lesson.status"
                @update:model-value="currentVideoUrl = $event"
                @uploaded="handleUploaded"
                @error="handleUploadError"
                @status-change="handleStatusChange"
              />
            </CardContent>
          </Card>
        </div>
      </div>
    </main>
  </div>
</template>
