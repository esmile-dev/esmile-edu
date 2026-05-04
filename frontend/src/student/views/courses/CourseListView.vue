<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { studentApi } from '@/student/api/studentApi'
import type { Course, PageResponse } from '@/common/types/api'
import StudentHeader from '@/student/components/StudentHeader.vue'
import CourseCard from '@/student/components/CourseCard.vue'

const router = useRouter()

const courses = ref<Course[]>([])
const loading = ref(true)
const error = ref('')
const page = ref(0)
const size = ref(20)
const totalElements = ref(0)
const totalPages = ref(0)

const categories = ['全部', '编程', '设计', '语言', '商业', '音乐']
const selectedCategory = ref('全部')

onMounted(async () => {
  await fetchCourses()
})

async function fetchCourses() {
  loading.value = true
  error.value = ''
  try {
    const response = await studentApi.getCourses({ page: page.value, size: size.value })
    courses.value = response.content
    totalElements.value = response.totalElements
    totalPages.value = response.totalPages
  } catch (err: any) {
    error.value = err.message || '加载课程失败'
  } finally {
    loading.value = false
  }
}

function goToCourseDetail(courseId: number) {
  router.push(`/student/courses/${courseId}`)
}

function goToPage(p: number) {
  page.value = p
  fetchCourses()
}
</script>

<template>
  <div class="min-h-screen bg-background">
    <StudentHeader />

    <main class="pt-14 md:pt-16 pb-20 md:pb-0">
      <!-- Banner -->
      <section class="bg-gradient-to-r from-primary to-blue-600 text-white py-12">
        <div class="max-w-7xl mx-auto px-4">
          <h1 class="text-3xl font-bold mb-4">探索优质课程</h1>
          <p class="text-lg opacity-90">发现适合自己的学习内容，提升专业技能</p>
        </div>
      </section>

      <!-- Categories -->
      <section class="border-b">
        <div class="max-w-7xl mx-auto px-4 py-4">
          <div class="flex gap-2 overflow-x-auto">
            <Button
              v-for="cat in categories"
              :key="cat"
              :variant="selectedCategory === cat ? 'default' : 'outline'"
              size="sm"
              @click="selectedCategory = cat"
            >
              {{ cat }}
            </Button>
          </div>
        </div>
      </section>

      <!-- Course Grid -->
      <section class="max-w-7xl mx-auto px-4 py-8">
        <h2 class="text-xl font-semibold mb-6">全部课程</h2>

        <!-- Loading State -->
        <div v-if="loading" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          <div v-for="i in 8" :key="i" class="space-y-3">
            <Skeleton class="h-40 w-full rounded-lg" />
            <Skeleton class="h-4 w-3/4" />
            <Skeleton class="h-4 w-1/2" />
          </div>
        </div>

        <!-- Error State -->
        <div v-else-if="error" class="text-center py-12">
          <p class="text-destructive mb-4">{{ error }}</p>
          <Button @click="fetchCourses">重试</Button>
        </div>

        <!-- Course Grid -->
        <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          <CourseCard
            v-for="course in courses"
            :key="course.id"
            :course="course"
            @click="goToCourseDetail(course.id)"
          />
        </div>

        <!-- Empty State -->
        <div v-if="!loading && !error && courses.length === 0" class="text-center py-12">
          <p class="text-muted-foreground">暂无课程</p>
        </div>

        <!-- Pagination -->
        <div v-if="!loading && courses.length > 0" class="mt-8 flex items-center justify-between">
          <p class="text-sm text-muted-foreground">
            显示 {{ page * size + 1 }}-{{ Math.min((page + 1) * size, totalElements) }} / {{ totalElements }}
          </p>
          <div class="flex gap-2">
            <Button
              variant="outline"
              size="sm"
              :disabled="page === 0"
              @click="goToPage(page - 1)"
            >
              上一页
            </Button>
            <Button
              variant="outline"
              size="sm"
              :disabled="page >= totalPages - 1"
              @click="goToPage(page + 1)"
            >
              下一页
            </Button>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>
