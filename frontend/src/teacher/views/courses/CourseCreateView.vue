<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { teacherApi } from '@/teacher/api/teacherApi'
import TeacherHeader from '@/teacher/components/TeacherHeader.vue'
import { Upload, Image } from 'lucide-vue-next'

const router = useRouter()

const title = ref('')
const description = ref('')
const coverImage = ref('')
const coverPreview = ref('')
const fileInput = ref<HTMLInputElement | null>(null)
const uploading = ref(false)
const loading = ref(false)
const error = ref('')

function triggerFileInput() {
  fileInput.value?.click()
}

async function handleFileChange(event: Event) {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return

  if (!file.type.startsWith('image/')) {
    error.value = '请选择图片文件'
    return
  }

  if (file.size > 5 * 1024 * 1024) {
    error.value = '图片大小不能超过 5MB'
    return
  }

  uploading.value = true
  error.value = ''

  try {
    // For now, use local preview - in production, upload to cloud storage
    coverPreview.value = URL.createObjectURL(file)
    // TODO: Upload to Tencent Cloud VOD or similar
    coverImage.value = coverPreview.value
  } catch (err: any) {
    error.value = err.message || '上传失败'
  } finally {
    uploading.value = false
    target.value = ''
  }
}

async function handleCreate() {
  if (!title.value.trim()) {
    error.value = '请输入课程标题'
    return
  }

  loading.value = true
  error.value = ''

  try {
    const course = await teacherApi.createCourse({
      title: title.value,
      description: description.value,
      cover: coverImage.value || undefined,
    })
    router.push(`/teacher/courses/${course.id}/edit`)
  } catch (err: any) {
    error.value = err.message || '创建失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen bg-background">
    <TeacherHeader />

    <main class="pt-16 max-w-2xl mx-auto px-4 py-8">
      <h1 class="text-2xl font-bold mb-6">创建课程</h1>

      <Card>
        <CardContent class="p-6 space-y-6">
          <!-- Cover Upload -->
          <div>
            <Label>课程封面</Label>
            <input
              ref="fileInput"
              type="file"
              accept="image/*"
              class="hidden"
              @change="handleFileChange"
            />
            <div
              class="mt-2 border-2 border-dashed rounded-lg p-6 text-center hover:bg-muted/50 cursor-pointer transition-colors"
              :class="{ 'border-primary': coverPreview }"
              @click="triggerFileInput"
            >
              <div v-if="coverPreview" class="relative">
                <img
                  :src="coverPreview"
                  alt="封面预览"
                  class="max-h-48 mx-auto rounded-lg object-cover"
                />
                <p class="text-sm text-muted-foreground mt-2">点击更换图片</p>
              </div>
              <div v-else>
                <Image v-if="uploading" class="w-8 h-8 mx-auto text-muted-foreground mb-2 animate-pulse" />
                <Upload v-else class="w-8 h-8 mx-auto text-muted-foreground mb-2" />
                <p class="text-sm text-muted-foreground">{{ uploading ? '上传中...' : '点击或拖拽上传封面图片' }}</p>
                <p class="text-xs text-muted-foreground mt-1">建议尺寸 16:9</p>
              </div>
            </div>
          </div>

          <!-- Title -->
          <div class="space-y-2">
            <Label for="title">课程标题 *</Label>
            <Input
              id="title"
              v-model="title"
              placeholder="请输入课程标题"
              :disabled="loading"
            />
          </div>

          <!-- Description -->
          <div class="space-y-2">
            <Label for="description">课程简介</Label>
            <textarea
              id="description"
              v-model="description"
              placeholder="请输入课程简介"
              :disabled="loading"
              rows="4"
              class="flex w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
            />
          </div>

          <p v-if="error" class="text-sm text-destructive">{{ error }}</p>

          <div class="flex gap-4">
            <Button variant="outline" @click="router.back()" :disabled="loading">
              取消
            </Button>
            <Button @click="handleCreate" :disabled="loading">
              {{ loading ? '创建中...' : '创建' }}
            </Button>
          </div>
        </CardContent>
      </Card>
    </main>
  </div>
</template>
