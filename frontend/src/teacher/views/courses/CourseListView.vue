<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { teacherApi } from '@/teacher/api/teacherApi'
import type { Course, PageResponse } from '@/common/types/api'
import TeacherHeader from '@/teacher/components/TeacherHeader.vue'
import { Plus, MoreVertical, Edit, Trash2, Eye } from 'lucide-vue-next'

const router = useRouter()

const courses = ref<Course[]>([])
const loading = ref(true)
const error = ref('')

onMounted(async () => {
  await fetchCourses()
})

async function fetchCourses() {
  loading.value = true
  error.value = ''
  try {
    const response = await teacherApi.getCourses()
    courses.value = response.content || []
  } catch (err: any) {
    error.value = err.message || '加载失败'
  } finally {
    loading.value = false
  }
}

function goToCreate() {
  router.push('/teacher/courses/create')
}

function goToEdit(courseId: number) {
  router.push(`/teacher/courses/${courseId}/edit`)
}

async function handleDelete(courseId: number) {
  if (!confirm('确定要删除这门课程吗?')) return
  try {
    await teacherApi.deleteCourse(courseId)
    await fetchCourses()
  } catch (err: any) {
    alert(err.message || '删除失败')
  }
}

async function handlePublish(courseId: number) {
  try {
    await teacherApi.publishCourse(courseId)
    await fetchCourses()
  } catch (err: any) {
    alert(err.message || '发布失败')
  }
}
</script>

<template>
  <div class="min-h-screen bg-background">
    <TeacherHeader />

    <main class="pt-16 max-w-7xl mx-auto px-4 py-8">
      <div class="flex items-center justify-between mb-6">
        <h1 class="text-2xl font-bold">我的课程</h1>
        <Button @click="goToCreate">
          <Plus class="w-4 h-4 mr-1" />
          新建课程
        </Button>
      </div>

      <!-- Loading -->
      <div v-if="loading" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div v-for="i in 6" :key="i" class="space-y-3">
          <Skeleton class="h-40 w-full rounded-lg" />
          <Skeleton class="h-4 w-3/4" />
          <Skeleton class="h-4 w-1/2" />
        </div>
      </div>

      <!-- Error -->
      <div v-else-if="error" class="text-center py-12">
        <p class="text-destructive mb-4">{{ error }}</p>
        <Button @click="fetchCourses">重试</Button>
      </div>

      <!-- Course Grid -->
      <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <Card v-for="course in courses" :key="course.id" class="overflow-hidden">
          <div class="aspect-video bg-muted">
            <img
              v-if="course.coverImage"
              :src="course.coverImage"
              :alt="course.title"
              class="w-full h-full object-cover"
            />
          </div>
          <CardContent class="p-4 pt-5">
            <div class="flex items-start justify-between gap-2">
              <h3 class="font-semibold line-clamp-1">{{ course.title }}</h3>
              <Badge :variant="course.status === 'PUBLISHED' ? 'default' : 'secondary'">
                {{ course.status === 'PUBLISHED' ? '已发布' : '草稿' }}
              </Badge>
            </div>
            <p class="text-sm text-muted-foreground mt-1">
              {{ course.chapterCount || 0 }} 章节 · {{ course.lessonCount || 0 }} 课时
            </p>
            <div class="flex gap-2 mt-4">
              <Button size="sm" variant="outline" @click="goToEdit(course.id)">
                <Edit class="w-3 h-3 mr-1" />
                编辑
              </Button>
              <Button
                v-if="course.status !== 'PUBLISHED'"
                size="sm"
                @click="handlePublish(course.id)"
              >
                发布
              </Button>
              <Button size="sm" variant="destructive" @click="handleDelete(course.id)">
                <Trash2 class="w-3 h-3" />
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>

      <!-- Empty -->
      <div v-if="!loading && !error && courses.length === 0" class="text-center py-12">
        <p class="text-muted-foreground mb-4">暂无课程</p>
        <Button @click="goToCreate">创建第一个课程</Button>
      </div>
    </main>
  </div>
</template>
