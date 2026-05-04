<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { teacherApi } from '@/teacher/api/teacherApi'
import type { Lesson } from '@/common/types/api'
import TeacherHeader from '@/teacher/components/TeacherHeader.vue'
import { Upload, CheckCircle, XCircle, Loader2, Video, ArrowLeft } from 'lucide-vue-next'

type UploadStatus = 'IDLE' | 'UPLOADING' | 'PROCESSING' | 'READY' | 'FAILED'

const router = useRouter()
const route = useRoute()

const courseId = Number(route.params.courseId)
const lessonId = Number(route.params.lessonId)

const lesson = ref<Lesson | null>(null)
const lessonTitle = ref('')
const loading = ref(true)
const error = ref('')

const uploadStatus = ref<UploadStatus>('IDLE')
const uploadProgress = ref(0)
const uploadError = ref('')
const selectedFile = ref<File | null>(null)

const statusConfig = {
  IDLE: { label: '未上传', variant: 'secondary' as const, icon: Video },
  UPLOADING: { label: '上传中', variant: 'default' as const, icon: Loader2 },
  PROCESSING: { label: '处理中', variant: 'default' as const, icon: Loader2 },
  READY: { label: '已完成', variant: 'default' as const, icon: CheckCircle },
  FAILED: { label: '失败', variant: 'destructive' as const, icon: XCircle },
}

const currentStatus = computed(() => {
  if (lesson.value?.status === 'READY') return 'READY'
  if (lesson.value?.status === 'PROCESSING') return 'PROCESSING'
  return uploadStatus.value
})

const currentConfig = computed(() => statusConfig[currentStatus.value])

onMounted(async () => {
  try {
    const course = await teacherApi.getCourseDetail(courseId)
    const found = course.chapters
      .flatMap(c => c.lessons)
      .find(l => l.id === lessonId)

    if (found) {
      lesson.value = found
      lessonTitle.value = found.title
    } else {
      error.value = '课时未找到'
    }
  } catch (err: any) {
    error.value = err.message || '加载失败'
  } finally {
    loading.value = false
  }
})

function goBack() {
  router.push(`/teacher/courses/${courseId}/edit`)
}

function handleFileSelect(event: Event) {
  const input = event.target as HTMLInputElement
  if (input.files && input.files[0]) {
    const file = input.files[0]
    if (!file.type.startsWith('video/')) {
      uploadError.value = '请选择视频文件'
      return
    }
    if (file.size > 500 * 1024 * 1024) {
      uploadError.value = '文件大小不能超过 500MB'
      return
    }
    selectedFile.value = file
    uploadError.value = ''
  }
}

async function handleUpload() {
  if (!selectedFile.value) return

  uploadStatus.value = 'UPLOADING'
  uploadProgress.value = 0
  uploadError.value = ''

  try {
    // Apply for upload signature
    const { videoId, uploadUrl, signature } = await teacherApi.applyUpload({
      fileName: selectedFile.value.name,
      fileSize: selectedFile.value.size,
    })

    // Simulate upload progress
    const progressInterval = setInterval(() => {
      uploadProgress.value = Math.min(uploadProgress.value + 10, 90)
    }, 200)

    // For demo: in production, you'd upload to the cloud storage directly
    // Using mock upload since we don't have actual cloud storage
    await new Promise(resolve => setTimeout(resolve, 2000))

    clearInterval(progressInterval)
    uploadProgress.value = 100

    uploadStatus.value = 'PROCESSING'

    // Commit upload
    await teacherApi.commitUpload({
      lessonId,
      videoId: videoId || 'mock-video-id',
    })

    // Poll for processing status
    await pollVideoStatus()
  } catch (err: any) {
    uploadStatus.value = 'FAILED'
    uploadError.value = err.message || '上传失败'
  }
}

async function pollVideoStatus() {
  const maxAttempts = 30
  let attempts = 0

  while (attempts < maxAttempts) {
    try {
      await new Promise(resolve => setTimeout(resolve, 2000))
      const course = await teacherApi.getCourseDetail(courseId)
      const updated = course.chapters
        .flatMap(c => c.lessons)
        .find(l => l.id === lessonId)

      if (updated?.status === 'READY') {
        lesson.value = updated
        uploadStatus.value = 'READY'
        return
      }
      if (updated?.status === 'FAILED') {
        uploadStatus.value = 'FAILED'
        uploadError.value = '视频处理失败'
        return
      }
    } catch {
      // Continue polling
    }
    attempts++
  }

  uploadStatus.value = 'PROCESSING'
}

async function handleRetry() {
  uploadStatus.value = 'IDLE'
  selectedFile.value = null
  uploadProgress.value = 0
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
          <Badge :variant="currentConfig.variant" class="gap-1">
            <component :is="currentConfig.icon" class="w-3 h-3" :class="{ 'animate-spin': currentStatus === 'UPLOADING' || currentStatus === 'PROCESSING' }" />
            {{ currentConfig.label }}
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
              <!-- Current Video -->
              <div v-if="lesson.videoUrl" class="border rounded-lg p-4">
                <div class="flex items-center justify-between">
                  <div class="flex items-center gap-3">
                    <Video class="w-8 h-8 text-muted-foreground" />
                    <div>
                      <p class="font-medium">当前视频</p>
                      <p class="text-sm text-muted-foreground">
                        {{ lesson.duration ? `${Math.floor(lesson.duration / 60)}:${String(lesson.duration % 60).padStart(2, '0')}` : '时长未知' }}
                      </p>
                    </div>
                  </div>
                  <Badge v-if="lesson.status === 'READY'" variant="default">已上传</Badge>
                </div>
              </div>

              <!-- Upload Area -->
              <div v-if="currentStatus === 'IDLE' || currentStatus === 'FAILED'" class="space-y-4">
                <div
                  class="border-2 border-dashed rounded-lg p-8 text-center hover:bg-muted/50 cursor-pointer transition-colors"
                  :class="{ 'border-destructive': uploadError }"
                  @click="($refs.fileInput as HTMLInputElement).click()"
                >
                  <Upload class="w-10 h-10 mx-auto text-muted-foreground mb-3" />
                  <p class="font-medium">点击或拖拽上传视频</p>
                  <p class="text-sm text-muted-foreground mt-1">支持 MP4、MOV、AVI 格式，最大 500MB</p>
                </div>
                <input
                  ref="fileInput"
                  type="file"
                  accept="video/*"
                  class="hidden"
                  @change="handleFileSelect"
                />
                <p v-if="uploadError" class="text-sm text-destructive">{{ uploadError }}</p>
                <p v-if="selectedFile" class="text-sm">已选择: {{ selectedFile.name }}</p>
              </div>

              <!-- Upload Progress -->
              <div v-if="currentStatus === 'UPLOADING'" class="space-y-4">
                <div class="border rounded-lg p-6">
                  <div class="flex items-center gap-4 mb-4">
                    <Loader2 class="w-8 h-8 animate-spin text-primary" />
                    <div class="flex-1">
                      <p class="font-medium">{{ selectedFile?.name }}</p>
                      <p class="text-sm text-muted-foreground">上传中...</p>
                    </div>
                  </div>
                  <div class="w-full bg-muted rounded-full h-2">
                    <div
                      class="bg-primary h-2 rounded-full transition-all"
                      :style="{ width: `${uploadProgress}%` }"
                    />
                  </div>
                  <p class="text-sm text-muted-foreground text-right mt-2">{{ uploadProgress }}%</p>
                </div>
              </div>

              <!-- Processing -->
              <div v-if="currentStatus === 'PROCESSING'" class="border rounded-lg p-6 text-center">
                <Loader2 class="w-10 h-10 mx-auto animate-spin text-primary mb-3" />
                <p class="font-medium">视频处理中...</p>
                <p class="text-sm text-muted-foreground mt-1">请稍候，通常需要几分钟</p>
              </div>

              <!-- Ready -->
              <div v-if="currentStatus === 'READY'" class="border rounded-lg p-6 text-center bg-green-50 border-green-200">
                <CheckCircle class="w-10 h-10 mx-auto text-green-600 mb-3" />
                <p class="font-medium text-green-700">视频上传完成</p>
                <p class="text-sm text-green-600 mt-1">视频已准备就绪，可以使用了</p>
                <Button variant="outline" class="mt-4" @click="handleRetry">
                  重新上传
                </Button>
              </div>

              <!-- Upload Button -->
              <div v-if="(currentStatus === 'IDLE' || currentStatus === 'FAILED') && selectedFile" class="flex gap-2">
                <Button @click="handleUpload" class="flex-1">
                  <Upload class="w-4 h-4 mr-1" />
                  开始上传
                </Button>
                <Button variant="outline" @click="handleRetry">
                  取消
                </Button>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </main>
  </div>
</template>
