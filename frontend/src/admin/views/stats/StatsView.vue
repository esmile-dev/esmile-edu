<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { adminApi } from '@/admin/api/adminApi'
import type { StatsOverview } from '@/common/types/api'
import { useAuthStore } from '@/common/stores/auth'
import { Users, BookOpen, GraduationCap, CheckCircle, TrendingUp, Award } from 'lucide-vue-next'

const router = useRouter()
const authStore = useAuthStore()

const stats = ref<StatsOverview | null>(null)
const loading = ref(true)
const error = ref('')

onMounted(async () => {
  try {
    stats.value = await adminApi.getStats()
  } catch (err: any) {
    error.value = err.message || '加载失败'
  } finally {
    loading.value = false
  }
})

function formatNumber(n: number): string {
  if (n >= 1000) {
    return (n / 1000).toFixed(1) + 'k'
  }
  return n.toString()
}

const redemptionRate = () => {
  if (!stats.value || stats.value.totalRedeemCodes === 0) return 0
  return Math.round((stats.value.redeemedCodes / stats.value.totalRedeemCodes) * 100)
}

const publishRate = () => {
  if (!stats.value || stats.value.totalCourses === 0) return 0
  return Math.round((stats.value.publishedCourses / stats.value.totalCourses) * 100)
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
              class="text-sm font-medium text-muted-foreground hover:text-foreground transition-colors"
            >
              课程管理
            </router-link>
            <router-link
              to="/admin/stats"
              class="text-sm font-medium text-foreground"
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
      <h1 class="text-2xl font-bold mb-6">统计分析</h1>

      <!-- Loading -->
      <div v-if="loading" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <Skeleton v-for="i in 4" :key="i" class="h-32 rounded-lg" />
      </div>

      <!-- Error -->
      <div v-else-if="error" class="text-center py-12">
        <p class="text-destructive mb-4">{{ error }}</p>
        <Button @click="$router.go(0)">重试</Button>
      </div>

      <!-- Stats -->
      <div v-else-if="stats" class="space-y-8">
        <!-- Top Stats -->
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          <Card>
            <CardContent class="p-6">
              <div class="flex items-center justify-between">
                <div>
                  <p class="text-sm text-muted-foreground">总用户</p>
                  <p class="text-3xl font-bold mt-1">{{ formatNumber(stats.totalUsers) }}</p>
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
                  <p class="text-sm text-muted-foreground">学生</p>
                  <p class="text-3xl font-bold mt-1">{{ formatNumber(stats.totalStudents) }}</p>
                </div>
                <div class="w-12 h-12 bg-green-100 rounded-full flex items-center justify-center">
                  <GraduationCap class="w-6 h-6 text-green-600" />
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent class="p-6">
              <div class="flex items-center justify-between">
                <div>
                  <p class="text-sm text-muted-foreground">教师</p>
                  <p class="text-3xl font-bold mt-1">{{ formatNumber(stats.totalTeachers) }}</p>
                </div>
                <div class="w-12 h-12 bg-purple-100 rounded-full flex items-center justify-center">
                  <BookOpen class="w-6 h-6 text-purple-600" />
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent class="p-6">
              <div class="flex items-center justify-between">
                <div>
                  <p class="text-sm text-muted-foreground">待审核</p>
                  <p class="text-3xl font-bold mt-1 text-orange-600">{{ stats.pendingTeachers }}</p>
                </div>
                <div class="w-12 h-12 bg-orange-100 rounded-full flex items-center justify-center">
                  <Award class="w-6 h-6 text-orange-600" />
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        <!-- Course Stats -->
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <Card>
            <CardHeader>
              <CardTitle>课程统计</CardTitle>
            </CardHeader>
            <CardContent class="space-y-4">
              <div class="flex items-center justify-between py-3 border-b">
                <span class="text-muted-foreground">总课程</span>
                <span class="font-semibold">{{ stats.totalCourses }}</span>
              </div>
              <div class="flex items-center justify-between py-3 border-b">
                <span class="text-muted-foreground">已发布</span>
                <span class="font-semibold text-green-600">{{ stats.publishedCourses }}</span>
              </div>
              <div class="flex items-center justify-between py-3 border-b">
                <span class="text-muted-foreground">草稿</span>
                <span class="font-semibold">{{ stats.totalCourses - stats.publishedCourses }}</span>
              </div>
              <div class="mt-4">
                <div class="flex justify-between text-sm mb-2">
                  <span class="text-muted-foreground">发布率</span>
                  <span class="font-medium">{{ publishRate() }}%</span>
                </div>
                <div class="w-full bg-muted rounded-full h-2">
                  <div
                    class="bg-green-500 h-2 rounded-full"
                    :style="{ width: `${publishRate()}%` }"
                  />
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>兑换码统计</CardTitle>
            </CardHeader>
            <CardContent class="space-y-4">
              <div class="flex items-center justify-between py-3 border-b">
                <span class="text-muted-foreground">总兑换码</span>
                <span class="font-semibold">{{ formatNumber(stats.totalRedeemCodes) }}</span>
              </div>
              <div class="flex items-center justify-between py-3 border-b">
                <span class="text-muted-foreground">已兑换</span>
                <span class="font-semibold text-green-600">{{ formatNumber(stats.redeemedCodes) }}</span>
              </div>
              <div class="flex items-center justify-between py-3 border-b">
                <span class="text-muted-foreground">未兑换</span>
                <span class="font-semibold">{{ formatNumber(stats.totalRedeemCodes - stats.redeemedCodes) }}</span>
              </div>
              <div class="mt-4">
                <div class="flex justify-between text-sm mb-2">
                  <span class="text-muted-foreground">兑换率</span>
                  <span class="font-medium">{{ redemptionRate() }}%</span>
                </div>
                <div class="w-full bg-muted rounded-full h-2">
                  <div
                    class="bg-blue-500 h-2 rounded-full"
                    :style="{ width: `${redemptionRate()}%` }"
                  />
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        <!-- Enrollment -->
        <Card>
          <CardHeader>
            <CardTitle class="flex items-center gap-2">
              <TrendingUp class="w-5 h-5 text-green-500" />
              平台增长
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
              <div class="text-center py-4">
                <p class="text-4xl font-bold text-green-600">{{ stats.totalEnrollments }}</p>
                <p class="text-sm text-muted-foreground mt-2">总Enrollment数</p>
              </div>
              <div class="text-center py-4 border-x">
                <p class="text-4xl font-bold text-blue-600">{{ formatNumber(stats.totalStudents) }}</p>
                <p class="text-sm text-muted-foreground mt-2">活跃学生</p>
              </div>
              <div class="text-center py-4">
                <p class="text-4xl font-bold text-purple-600">{{ formatNumber(stats.publishedCourses) }}</p>
                <p class="text-sm text-muted-foreground mt-2">优质课程</p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </main>
  </div>
</template>
