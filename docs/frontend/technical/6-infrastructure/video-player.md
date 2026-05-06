# 视频播放器集成

---

## 1. 腾讯云 VOD 播放器

### 1.1 SDK 引入

```bash
npm install @cloudbase/vod-sdk @tencentcloud/tencentcloud-sdk-sdk-go
```

### 1.2 播放器组件

```vue
<!-- common/components/VideoPlayer.vue -->
<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import TCPlayer from 'tcplayer'

interface Props {
  videoUrl?: string
  videoId?: string
  poster?: string
  autoplay?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  autoplay: false,
})

const videoRef = ref<HTMLVideoElement>()

onMounted(() => {
  if (!videoRef.value) return

  const player = TCPlayer({
    id: 'tcplayer',
    appId: import.meta.env.VITE_TENCENT_VOD_APP_ID,
    fileId: props.videoId,
    playbackUrl: props.videoUrl,
    poster: props.poster,
    autoplay: props.autoplay,
    plugins: {
      ContinuePlay: { auto: true },
      Progress: { remember: true },
    },
  })

  // 存储 player 实例用于清理
  ;(videoRef.value as any)._player = player
})

onUnmounted(() => {
  if (videoRef.value?._player) {
    videoRef.value._player.destroy()
  }
})
</script>

<template>
  <div id="tcplayer" ref="videoRef" class="w-full aspect-video bg-black"></div>
</template>
```

---

## 2. 视频上传

### 2.1 上传流程

```
1. 前端选择文件 → 调用 teacherApi.applyUpload 获取上传凭证
2. 使用腾讯云 SDK 上传文件到云存储
3. 上传完成 → 调用 teacherApi.commitUpload 通知后端
4. 后端触发视频处理（转码、截图）
5. 轮询课时状态，等待视频处理完成
```

### 2.2 上传组件

```vue
<!-- teacher/components/VideoUploader.vue -->
<script setup lang="ts">
import { ref } from 'vue'
import { Upload, X, Loader2 } from 'lucide-vue-next'
import { Button } from '@/common/components/ui/button'
import { teacherApi } from '@/teacher/api/teacherApi'

interface Props {
  lessonId: number
  modelValue?: string
}

const props = defineProps<Props>()
const emit = defineEmits<{
  'update:modelValue': [value: string | undefined]
  'uploaded': [videoId: string, videoUrl: string]
}>()

const uploading = ref(false)
const progress = ref(0)
const error = ref<string | null>(null)

async function handleFileSelect(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]

  if (!file) return

  // 1. 申请上传
  const { videoId, signature, uploadUrl } = await teacherApi.applyUpload({
    fileName: file.name,
    fileSize: file.size,
  })

  uploading.value = true
  progress.value = 0
  error.value = null

  try {
    // 2. 使用腾讯云 SDK 上传
    await uploadWithProgress(file, uploadUrl, signature, (p) => {
      progress.value = p
    })

    // 3. 确认上传完成
    const result = await teacherApi.commitUpload({
      lessonId: props.lessonId,
      videoId,
    })

    emit('update:modelValue', result.videoUrl)
    emit('uploaded', videoId, result.videoUrl)
  } catch (err) {
    error.value = '上传失败，请重试'
  } finally {
    uploading.value = false
  }
}

function uploadWithProgress(
  file: File,
  uploadUrl: string,
  signature: string,
  onProgress: (percent: number) => void
): Promise<void> {
  return new Promise((resolve, reject) => {
    const xhr = new XMLHttpRequest()
    xhr.open('PUT', uploadUrl)

    xhr.setRequestHeader('Authorization', signature)

    xhr.upload.onprogress = (e) => {
      if (e.lengthComputable) {
        onProgress(Math.round((e.loaded / e.total) * 100))
      }
    }

    xhr.onload = () => {
      if (xhr.status >= 200 && xhr.status < 300) {
        resolve()
      } else {
        reject(new Error('Upload failed'))
      }
    }

    xhr.onerror = () => reject(new Error('Network error'))
    xhr.send(file)
  })
}
</script>

<template>
  <div class="border-2 border-dashed rounded-lg p-6 text-center">
    <input
      type="file"
      accept="video/*"
      class="hidden"
      id="video-upload"
      @change="handleFileSelect"
    />

    <label for="video-upload" class="cursor-pointer">
      <div v-if="uploading" class="flex flex-col items-center gap-2">
        <Loader2 class="w-8 h-8 animate-spin text-primary" />
        <p class="text-sm text-muted-foreground">上传中 {{ progress }}%</p>
        <div class="w-full max-w-xs h-2 bg-muted rounded-full overflow-hidden">
          <div
            class="h-full bg-primary transition-all"
            :style="{ width: `${progress}%` }"
          ></div>
        </div>
      </div>

      <div v-else class="flex flex-col items-center gap-2">
        <Upload class="w-8 h-8 text-muted-foreground" />
        <p class="text-sm text-muted-foreground">点击选择视频文件</p>
        <p class="text-xs text-muted-foreground">支持 MP4、AVI、MKV，最大 2GB</p>
      </div>
    </label>

    <p v-if="error" class="mt-2 text-sm text-red-500">{{ error }}</p>
  </div>
</template>
```

---

## 3. 视频播放

### 3.1 课时播放器

```vue
<!-- student/views/LessonPlayerView.vue -->
<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import VideoPlayer from '@/common/components/VideoPlayer.vue'
import { Card, CardContent } from '@/common/components/ui/card'
import { studentApi } from '@/student/api/studentApi'

const route = useRoute()
const lesson = ref<any>(null)
const loading = ref(true)

const courseId = Number(route.params.courseId)
const lessonId = Number(route.params.lessonId)

onMounted(async () => {
  try {
    const course = await studentApi.getCourseDetail(courseId)
    // 找到当前课时
    for (const chapter of course.chapters || []) {
      const found = chapter.lessons.find((l: any) => l.id === lessonId)
      if (found) {
        lesson.value = found
        break
      }
    }
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div v-if="loading" class="flex justify-center py-12">
    <Loader2 class="w-8 h-8 animate-spin" />
  </div>

  <div v-else-if="lesson" class="space-y-4">
    <!-- 播放器 -->
    <VideoPlayer
      v-if="lesson.videoUrl && lesson.status === 'READY'"
      :video-url="lesson.videoUrl"
      :poster="lesson.poster"
    />

    <Card v-else>
      <CardContent class="p-6 text-center">
        <p v-if="lesson.status === 'PROCESSING'">
          视频正在处理中，请稍后再试...
        </p>
        <p v-else-if="lesson.status === 'FAILED'">
          视频处理失败，请联系教师
        </p>
      </CardContent>
    </Card>

    <!-- 课时信息 -->
    <div class="flex items-center justify-between">
      <h1 class="text-xl font-semibold">{{ lesson.title }}</h1>
      <span v-if="lesson.duration" class="text-muted-foreground">
        {{ Math.floor(lesson.duration / 60) }}:{{ (lesson.duration % 60).toString().padStart(2, '0') }}
      </span>
    </div>
  </div>
</template>
```

---

## 4. 视频状态轮询

### 4.1 课时状态检查

```typescript
// common/composables/useLessonStatus.ts
import { ref } from 'vue'
import { teacherApi } from '@/teacher/api/teacherApi'

export function useLessonStatus(lessonId: Ref<number>) {
  const status = ref<'PROCESSING' | 'READY' | 'FAILED'>('PROCESSING')
  const loading = ref(false)
  let pollInterval: number | null = null

  async function checkStatus() {
    try {
      const lesson = await teacherApi.getLessonDetail(unref(lessonId))
      status.value = lesson.status

      if (lesson.status !== 'PROCESSING') {
        stopPolling()
      }
    } catch (err) {
      console.error('Failed to check lesson status', err)
    }
  }

  function startPolling(intervalMs = 5000) {
    stopPolling()
    checkStatus()
    pollInterval = window.setInterval(checkStatus, intervalMs)
  }

  function stopPolling() {
    if (pollInterval) {
      clearInterval(pollInterval)
      pollInterval = null
    }
  }

  onUnmounted(stopPolling)

  return {
    status,
    loading,
    startPolling,
    stopPolling,
  }
}
```
