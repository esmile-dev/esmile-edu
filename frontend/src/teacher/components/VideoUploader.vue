<script setup lang="ts">
import { ref, computed } from 'vue'
import { Upload, X, Loader2, CheckCircle, XCircle, Video } from 'lucide-vue-next'
import { teacherApi } from '@/teacher/api/teacherApi'
import type { LessonStatus } from '@/common/types/api'

export type UploadStatus = 'IDLE' | 'UPLOADING' | 'PROCESSING' | 'READY' | 'FAILED'

export interface VideoUploaderEmits {
  'update:modelValue': [videoUrl: string | undefined]
  uploaded: [videoId: string, videoUrl: string]
  error: [error: Error, code: 'FILE_TOO_LARGE' | 'INVALID_TYPE' | 'UPLOAD_FAILED' | 'COMMIT_FAILED']
  'status-change': [status: UploadStatus]
}

export interface VideoUploaderProps {
  /** Associated lesson ID */
  lessonId: number
  /** Max file size in bytes, default 1GB */
  maxSize?: number
  /** Accepted file types */
  accept?: string
  /** Current video URL (v-model) */
  modelValue?: string
  /** Current video status */
  videoStatus?: LessonStatus
}

const props = withDefaults(defineProps<VideoUploaderProps>(), {
  maxSize: 1 * 1024 * 1024 * 1024, // 1GB
  accept: 'video/*'
})

const emit = defineEmits<VideoUploaderEmits>()

const selectedFile = ref<File | null>(null)
const uploadStatus = ref<UploadStatus>('IDLE')
const uploadProgress = ref(0)
const uploadError = ref<string | null>(null)
const videoId = ref<string | null>(null)

const isVideoReady = computed(() => props.videoStatus === 'READY' && props.modelValue)

function handleFileSelect(event: Event) {
  const input = event.target as HTMLInputElement
  if (!input.files?.length) return

  const file = input.files[0]
  selectedFile.value = file
  uploadError.value = null

  // Validate file type
  if (!file.type.startsWith('video/')) {
    uploadError.value = '请选择视频文件'
    emit('error', new Error('Invalid file type'), 'INVALID_TYPE')
    return
  }

  // Validate file size
  if (file.size > props.maxSize) {
    uploadError.value = `文件大小不能超过 ${formatSize(props.maxSize)}`
    emit('error', new Error('File too large'), 'FILE_TOO_LARGE')
    return
  }

  uploadError.value = null
}

async function handleUpload() {
  if (!selectedFile.value) return

  uploadStatus.value = 'UPLOADING'
  uploadProgress.value = 0
  emit('status-change', 'UPLOADING')

  try {
    // Step 1: Apply for upload signature
    const { videoId: vid } = await teacherApi.applyUpload({
      fileName: selectedFile.value.name,
      fileSize: selectedFile.value.size
    })
    videoId.value = vid

    // Step 2: Simulate upload with progress (MVP - real implementation would use XHR)
    await simulateUpload()

    uploadStatus.value = 'PROCESSING'
    uploadProgress.value = 100
    emit('status-change', 'PROCESSING')

    // Step 3: Commit upload
    await teacherApi.commitUpload({
      lessonId: props.lessonId,
      videoId: vid
    })

    // For MVP, immediately mark as READY
    uploadStatus.value = 'READY'
    emit('status-change', 'READY')
    emit('update:modelValue', `https://play.vod.tscloud.com/mock/${vid}`)
    emit('uploaded', vid, `https://play.vod.tscloud.com/mock/${vid}`)
  } catch (err: any) {
    uploadStatus.value = 'FAILED'
    uploadError.value = err.message || '上传失败'
    emit('status-change', 'FAILED')
    emit('error', err, 'UPLOAD_FAILED')
  }
}

async function simulateUpload(): Promise<void> {
  // MVP: Simulate upload progress
  // Real implementation would use XMLHttpRequest with upload.onprogress
  const totalSteps = 10
  for (let i = 0; i < totalSteps; i++) {
    await new Promise(resolve => setTimeout(resolve, 200))
    uploadProgress.value = Math.round(((i + 1) / totalSteps) * 100)
  }
}

function handleRetry() {
  selectedFile.value = null
  uploadProgress.value = 0
  uploadError.value = null
  videoId.value = null
  uploadStatus.value = 'IDLE'
  emit('status-change', 'IDLE')
}

function clearFile() {
  selectedFile.value = null
  uploadProgress.value = 0
  uploadError.value = null
}

function formatSize(bytes: number): string {
  if (bytes >= 1024 * 1024 * 1024) {
    return `${(bytes / (1024 * 1024 * 1024)).toFixed(1)} GB`
  }
  if (bytes >= 1024 * 1024) {
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
  }
  return `${(bytes / 1024).toFixed(1)} KB`
}
</script>

<template>
  <div class="space-y-4">
    <!-- Current Video Display -->
    <div v-if="isVideoReady" class="border rounded-lg p-4">
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-3">
          <Video class="w-8 h-8 text-muted-foreground" />
          <div>
            <p class="font-medium">当前视频</p>
            <p class="text-sm text-muted-foreground">
              {{ props.modelValue ? '已上传' : '暂无视频' }}
            </p>
          </div>
        </div>
        <Button variant="outline" size="sm" @click="handleRetry">
          重新上传
        </Button>
      </div>
    </div>

    <!-- Upload Area (when no video or idle/failed) -->
    <div
      v-if="!isVideoReady && (uploadStatus === 'IDLE' || uploadStatus === 'FAILED')"
      class="space-y-4"
    >
      <!-- File Selection -->
      <div
        class="border-2 border-dashed rounded-lg p-8 text-center hover:bg-muted/50 cursor-pointer transition-colors"
        :class="{ 'border-destructive': uploadError }"
        @click="($refs.fileInput as HTMLInputElement).click()"
      >
        <Upload class="w-10 h-10 mx-auto text-muted-foreground mb-3" />
        <p class="font-medium">点击或拖拽上传视频</p>
        <p class="text-sm text-muted-foreground mt-1">
          支持 MP4、MOV、AVI、MKV 格式，最大 {{ formatSize(maxSize) }}
        </p>
      </div>

      <input
        ref="fileInput"
        type="file"
        :accept="accept"
        class="hidden"
        @change="handleFileSelect"
      />

      <!-- Selected File Info -->
      <div v-if="selectedFile" class="flex items-center justify-between p-3 bg-muted rounded-lg">
        <div class="flex items-center gap-3">
          <Video class="w-5 h-5 text-muted-foreground" />
          <div>
            <p class="text-sm font-medium">{{ selectedFile.name }}</p>
            <p class="text-xs text-muted-foreground">{{ formatSize(selectedFile.size) }}</p>
          </div>
        </div>
        <button @click="clearFile" class="text-muted-foreground hover:text-foreground">
          <X class="w-4 h-4" />
        </button>
      </div>

      <!-- Error Message -->
      <p v-if="uploadError" class="text-sm text-destructive">{{ uploadError }}</p>

      <!-- Upload Button -->
      <Button
        v-if="selectedFile"
        @click="handleUpload"
        class="w-full"
      >
        <Upload class="w-4 h-4 mr-2" />
        开始上传
      </Button>
    </div>

    <!-- Upload Progress -->
    <div
      v-if="uploadStatus === 'UPLOADING'"
      class="border rounded-lg p-6"
    >
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

    <!-- Processing State -->
    <div
      v-if="uploadStatus === 'PROCESSING'"
      class="border rounded-lg p-6 text-center"
    >
      <Loader2 class="w-10 h-10 mx-auto animate-spin text-primary mb-3" />
      <p class="font-medium">视频处理中...</p>
      <p class="text-sm text-muted-foreground mt-1">请稍候，通常需要几分钟</p>
    </div>

    <!-- Ready State -->
    <div
      v-if="uploadStatus === 'READY'"
      class="border rounded-lg p-6 text-center bg-green-50 border-green-200"
    >
      <CheckCircle class="w-10 h-10 mx-auto text-green-600 mb-3" />
      <p class="font-medium text-green-700">视频上传完成</p>
      <p class="text-sm text-green-600 mt-1">视频已准备就绪</p>
      <Button variant="outline" class="mt-4" @click="handleRetry">
        重新上传
      </Button>
    </div>

    <!-- Failed State -->
    <div
      v-if="uploadStatus === 'FAILED'"
      class="border border-destructive rounded-lg p-6 text-center"
    >
      <XCircle class="w-10 h-10 mx-auto text-destructive mb-3" />
      <p class="font-medium text-destructive">上传失败</p>
      <p class="text-sm text-muted-foreground mt-1">{{ uploadError || '请重试' }}</p>
      <Button class="mt-4" @click="handleUpload">
        重试
      </Button>
    </div>
  </div>
</template>
