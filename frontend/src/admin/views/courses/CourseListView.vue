<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { adminApi } from '@/admin/api/adminApi'
import type { Course, CourseStatus, PageResponse } from '@/common/types/api'
import { useAuthStore } from '@/common/stores/auth'
import { Eye, Trash2, CheckCircle, XCircle, BookOpen } from 'lucide-vue-next'

const router = useRouter()
const authStore = useAuthStore()

const courses = ref<Course[]>([])
const loading = ref(true)
const error = ref('')
const page = ref(0)
const totalPages = ref(0)
const totalElements = ref(0)

const statusFilter = ref<CourseStatus | ''>('')

async function fetchCourses() {
  loading.value = true
  try {
    const params: any = { page: page.value, size: 20 }
    if (statusFilter.value) params.status = statusFilter.value

    const response = await adminApi.getCourses(params)
    courses.value = response.content
    totalPages.value = response.totalPages
    totalElements.value = response.totalElements
  } catch (err: any) {
    error.value = err.message || '加载失败'
  } finally {
    loading.value = false
  }
}

onMounted(fetchCourses)

watch(statusFilter, () => {
  page.value = 0
  fetchCourses()
})

function goToPage(p: number) {
  page.value = p
  fetchCourses()
}

async function deleteCourse(id: number) {
  if (!confirm('确定要删除该课程吗?')) return
  try {
    await adminApi.deleteCourse(id)
    await fetchCourses()
  } catch (err: any) {
    alert(err.message || '删除失败')
  }
}

async function publishCourse(id: number) {
  try {
    await adminApi.updateCourseStatus(id, 'PUBLISHED')
    await fetchCourses()
  } catch (err: any) {
    alert(err.message || '操作失败')
  }
}

async function unpublishCourse(id: number) {
  try {
    await adminApi.updateCourseStatus(id, 'DRAFT')
    await fetchCourses()
  } catch (err: any) {
    alert(err.message || '操作失败')
  }
}

const statusBadgeVariant = (status: CourseStatus) => {
  return status === 'PUBLISHED' ? 'default' : 'secondary'
}

const statusLabel = (status: CourseStatus) => {
  return status === 'PUBLISHED' ? '已发布' : '草稿'
}
</script>

<template>
  <div class="min-h-screen bg-slate-50">
    <!-- Admin Header -->
    <header class="h-16 bg-white border-b shadow-sm fixed top-0 left-0 right-0 z-50">
      <div class="h-full max-w-7xl mx-auto px-4 flex items-center justify-between">
        <div class="flex items-center gap-8">
          <router-link to="/admin" class="flex items-center gap-2">
            <span class="text-xl font-bold text-primary">esmile edu</span>
            <Badge variant="secondary" class="text-xs">管理端</Badge>
          </router-link>

          <nav class="hidden md:flex items-center gap-6">
            <router-link
              to="/admin"
              class="text-sm font-medium text-muted-foreground hover:text-foreground transition-colors"
            >
              概览
            </router-link>
            <router-link
              to="/admin/users"
              class="text-sm font-medium text-muted-foreground hover:text-foreground transition-colors"
            >
              用户管理
            </router-link>
            <router-link
              to="/admin/courses"
              class="text-sm font-medium text-foreground"
            >
              课程管理
            </router-link>
            <router-link
              to="/admin/stats"
              class="text-sm font-medium text-muted-foreground hover:text-foreground transition-colors"
            >
              统计分析
            </router-link>
          </nav>
        </div>

        <Button variant="ghost" @click="authStore.clearAuth(); router.push('/admin/login')">
          退出
        </Button>
      </div>
    </header>

    <main class="pt-16 max-w-7xl mx-auto px-4 py-8">
      <h1 class="text-2xl font-bold mb-6">课程管理</h1>

      <!-- Filter -->
      <Card class="mb-6">
        <CardContent class="p-4">
          <div class="flex items-center gap-4">
            <Label class="text-sm">状态:</Label>
            <select
              v-model="statusFilter"
              class="h-9 rounded-md border border-input bg-background px-3 text-sm"
            >
              <option value="">全部</option>
              <option value="PUBLISHED">已发布</option>
              <option value="DRAFT">草稿</option>
            </select>
          </div>
        </CardContent>
      </Card>

      <!-- Loading -->
      <div v-if="loading" class="space-y-4">
        <Skeleton v-for="i in 5" :key="i" class="h-24 w-full rounded-lg" />
      </div>

      <!-- Error -->
      <div v-else-if="error" class="text-center py-12">
        <p class="text-destructive mb-4">{{ error }}</p>
        <Button @click="fetchCourses">重试</Button>
      </div>

      <!-- Courses List -->
      <div v-else class="space-y-4">
        <Card v-for="course in courses" :key="course.id">
          <CardContent class="p-4">
            <div class="flex items-center justify-between gap-4">
              <div class="flex items-center gap-4 flex-1">
                <div class="w-16 h-12 bg-muted rounded flex items-center justify-center">
                  <BookOpen v-if="!course.coverImage" class="w-6 h-6 text-muted-foreground" />
                  <img
                    v-else
                    :src="course.coverImage"
                    :alt="course.title"
                    class="w-full h-full object-cover rounded"
                  />
                </div>
                <div class="flex-1 min-w-0">
                  <div class="flex items-center gap-2">
                    <h3 class="font-semibold truncate">{{ course.title }}</h3>
                    <Badge :variant="statusBadgeVariant(course.status)">
                      {{ statusLabel(course.status) }}
                    </Badge>
                  </div>
                  <p class="text-sm text-muted-foreground mt-1">
                    {{ course.chapterCount || 0 }} 章节 · {{ course.lessonCount || 0 }} 课时
                  </p>
                </div>
              </div>

              <div class="flex gap-2">
                <Button
                  v-if="course.status === 'DRAFT'"
                  size="sm"
                  @click="publishCourse(course.id)"
                >
                  <CheckCircle class="w-4 h-4 mr-1" />
                  发布
                </Button>
                <Button
                  v-if="course.status === 'PUBLISHED'"
                  size="sm"
                  variant="outline"
                  @click="unpublishCourse(course.id)"
                >
                  <XCircle class="w-4 h-4 mr-1" />
                  下架
                </Button>
                <Button
                  size="sm"
                  variant="destructive"
                  @click="deleteCourse(course.id)"
                >
                  <Trash2 class="w-4 h-4" />
                </Button>
              </div>
            </div>
          </CardContent>
        </Card>

        <!-- Pagination -->
        <Card>
          <CardContent class="p-4 flex items-center justify-between">
            <p class="text-sm text-muted-foreground">
              共 {{ totalElements }} 条记录
            </p>
            <div class="flex gap-2">
              <Button
                size="sm"
                variant="outline"
                :disabled="page === 0"
                @click="goToPage(page - 1)"
              >
                上一页
              </Button>
              <span class="px-3 py-1 text-sm">
                {{ page + 1 }} / {{ totalPages || 1 }}
              </span>
              <Button
                size="sm"
                variant="outline"
                :disabled="page >= totalPages - 1"
                @click="goToPage(page + 1)"
              >
                下一页
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    </main>
  </div>
</template>
