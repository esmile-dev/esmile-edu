<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { teacherApi } from '@/teacher/api/teacherApi'
import TeacherHeader from '@/teacher/components/TeacherHeader.vue'
import { Users, BookOpen, CheckCircle, Clock, TrendingUp, Award } from 'lucide-vue-next'

interface TeacherStats {
  totalStudents: number
  activeEnrollments: number
  totalCourses: number
  publishedCourses: number
  totalLessons: number
  avgProgress: number
}

const stats = ref<TeacherStats | null>(null)
const loading = ref(true)
const error = ref('')

onMounted(async () => {
  try {
    // Get courses data for stats
    const response = await teacherApi.getCourses({ size: 100 })
    const courses = response.content

    let totalLessons = 0
    let publishedCourses = 0

    for (const course of courses) {
      if (course.status === 'PUBLISHED') publishedCourses++
      const detail = await teacherApi.getCourseDetail(course.id)
      for (const chapter of detail.chapters) {
        totalLessons += chapter.lessons.length
      }
    }

    stats.value = {
      totalStudents: response.content.reduce((acc, c) => acc + (c.lessonCount || 0) * 10, 0), // Mock data
      activeEnrollments: response.content.reduce((acc, c) => acc + (c.lessonCount || 0) * 5, 0), // Mock data
      totalCourses: courses.length,
      publishedCourses,
      totalLessons,
      avgProgress: 68, // Mock data
    }
  } catch (err: any) {
    error.value = err.message || '加载失败'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="min-h-screen bg-background">
    <TeacherHeader />

    <main class="pt-16 max-w-7xl mx-auto px-4 py-8">
      <h1 class="text-2xl font-bold mb-6">统计分析</h1>

      <!-- Loading -->
      <div v-if="loading" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <div v-for="i in 4" :key="i" class="space-y-3">
          <Skeleton class="h-32 w-full rounded-lg" />
        </div>
      </div>

      <!-- Error -->
      <div v-else-if="error" class="text-center py-12">
        <p class="text-destructive mb-4">{{ error }}</p>
        <Button @click="$router.go(0)">重试</Button>
      </div>

      <!-- Stats -->
      <div v-else-if="stats" class="space-y-8">
        <!-- Top Stats Cards -->
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          <Card>
            <CardContent class="p-6">
              <div class="flex items-center justify-between">
                <div>
                  <p class="text-sm text-muted-foreground">我的学生</p>
                  <p class="text-3xl font-bold mt-1">{{ stats.totalStudents }}</p>
                </div>
                <div class="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center">
                  <Users class="w-6 h-6 text-blue-600" />
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent class="p-6">
              <div class="flex items-center justify-between">
                <div>
                  <p class="text-sm text-muted-foreground">在学课程</p>
                  <p class="text-3xl font-bold mt-1">{{ stats.activeEnrollments }}</p>
                </div>
                <div class="w-12 h-12 bg-green-100 rounded-full flex items-center justify-center">
                  <BookOpen class="w-6 h-6 text-green-600" />
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent class="p-6">
              <div class="flex items-center justify-between">
                <div>
                  <p class="text-sm text-muted-foreground">已发布课程</p>
                  <p class="text-3xl font-bold mt-1">{{ stats.publishedCourses }}</p>
                </div>
                <div class="w-12 h-12 bg-purple-100 rounded-full flex items-center justify-center">
                  <CheckCircle class="w-6 h-6 text-purple-600" />
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent class="p-6">
              <div class="flex items-center justify-between">
                <div>
                  <p class="text-sm text-muted-foreground">平均完成率</p>
                  <p class="text-3xl font-bold mt-1">{{ stats.avgProgress }}%</p>
                </div>
                <div class="w-12 h-12 bg-orange-100 rounded-full flex items-center justify-center">
                  <TrendingUp class="w-6 h-6 text-orange-600" />
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        <!-- Course Stats -->
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <Card>
            <CardHeader>
              <CardTitle>课程概览</CardTitle>
            </CardHeader>
            <CardContent class="space-y-4">
              <div class="flex items-center justify-between py-3 border-b">
                <span class="text-muted-foreground">课程总数</span>
                <span class="font-semibold">{{ stats.totalCourses }}</span>
              </div>
              <div class="flex items-center justify-between py-3 border-b">
                <span class="text-muted-foreground">已发布</span>
                <span class="font-semibold text-green-600">{{ stats.publishedCourses }}</span>
              </div>
              <div class="flex items-center justify-between py-3 border-b">
                <span class="text-muted-foreground">草稿</span>
                <span class="font-semibold text-orange-600">{{ stats.totalCourses - stats.publishedCourses }}</span>
              </div>
              <div class="flex items-center justify-between py-3">
                <span class="text-muted-foreground">课时总数</span>
                <span class="font-semibold">{{ stats.totalLessons }}</span>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>学生参与度</CardTitle>
            </CardHeader>
            <CardContent class="space-y-4">
              <div class="flex items-center justify-between py-3 border-b">
                <span class="text-muted-foreground">总学生数</span>
                <span class="font-semibold">{{ stats.totalStudents }}</span>
              </div>
              <div class="flex items-center justify-between py-3 border-b">
                <span class="text-muted-foreground">活跃学习</span>
                <span class="font-semibold text-blue-600">{{ Math.floor(stats.totalStudents * 0.7) }}</span>
              </div>
              <div class="flex items-center justify-between py-3 border-b">
                <span class="text-muted-foreground">完成课程</span>
                <span class="font-semibold text-green-600">{{ Math.floor(stats.totalStudents * 0.3) }}</span>
              </div>
              <div class="flex items-center justify-between py-3">
                <span class="text-muted-foreground">平均进度</span>
                <span class="font-semibold">{{ stats.avgProgress }}%</span>
              </div>
            </CardContent>
          </Card>
        </div>

        <!-- Achievement -->
        <Card class="bg-gradient-to-r from-purple-500 to-indigo-600 text-white">
          <CardContent class="p-6">
            <div class="flex items-center gap-4">
              <div class="w-16 h-16 bg-white/20 rounded-full flex items-center justify-center">
                <Award class="w-8 h-8" />
              </div>
              <div>
                <p class="text-xl font-bold">教学成就</p>
                <p class="text-white/80 mt-1">
                  已帮助 {{ stats.totalStudents }} 名学生开始学习之旅
                </p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </main>
  </div>
</template>
