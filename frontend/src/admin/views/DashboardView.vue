<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { adminApi } from '@/admin/api/adminApi'
import type { StatsOverview } from '@/common/types/api'
import { useAuthStore } from '@/common/stores/auth'
import { Users, BookOpen, GraduationCap, Clock, CheckCircle, AlertCircle, TrendingUp } from 'lucide-vue-next'

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
              class="text-sm font-medium text-foreground"
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
      <h1 class="text-2xl font-bold mb-6">管理概览</h1>

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
                  <p class="text-sm text-muted-foreground">总用户数</p>
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
                  <p class="text-sm text-muted-foreground">待审核教师</p>
                  <p class="text-3xl font-bold mt-1 text-orange-600">{{ stats.pendingTeachers }}</p>
                </div>
                <div class="w-12 h-12 bg-orange-100 rounded-full flex items-center justify-center">
                  <Clock class="w-6 h-6 text-orange-600" />
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent class="p-6">
              <div class="flex items-center justify-between">
                <div>
                  <p class="text-sm text-muted-foreground">总课程数</p>
                  <p class="text-3xl font-bold mt-1">{{ formatNumber(stats.totalCourses) }}</p>
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
                  <p class="text-sm text-muted-foreground">已发布课程</p>
                  <p class="text-3xl font-bold mt-1">{{ formatNumber(stats.publishedCourses) }}</p>
                </div>
                <div class="w-12 h-12 bg-green-100 rounded-full flex items-center justify-center">
                  <CheckCircle class="w-6 h-6 text-green-600" />
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        <!-- Quick Actions & Pending -->
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <!-- Pending Teachers -->
          <Card>
            <CardHeader>
              <CardTitle class="flex items-center gap-2">
                <AlertCircle class="w-5 h-5 text-orange-500" />
                待处理事项
              </CardTitle>
            </CardHeader>
            <CardContent class="space-y-4">
              <div v-if="stats.pendingTeachers > 0" class="flex items-center justify-between py-3 border-b">
                <div class="flex items-center gap-3">
                  <GraduationCap class="w-5 h-5 text-muted-foreground" />
                  <span>待审核教师申请</span>
                </div>
                <div class="flex items-center gap-2">
                  <Badge variant="orange">{{ stats.pendingTeachers }}</Badge>
                  <Button size="sm" @click="router.push('/admin/users?role=TEACHER&status=PENDING_APPROVAL')">
                    查看
                  </Button>
                </div>
              </div>
              <div v-else class="text-center py-6 text-muted-foreground">
                <CheckCircle class="w-8 h-8 mx-auto mb-2 text-green-500" />
                <p>暂无待处理事项</p>
              </div>
            </CardContent>
          </Card>

          <!-- Stats Summary -->
          <Card>
            <CardHeader>
              <CardTitle class="flex items-center gap-2">
                <TrendingUp class="w-5 h-5 text-green-500" />
                平台数据
              </CardTitle>
            </CardHeader>
            <CardContent class="space-y-4">
              <div class="flex items-center justify-between py-3 border-b">
                <span class="text-muted-foreground">总学生数</span>
                <span class="font-semibold">{{ formatNumber(stats.totalStudents) }}</span>
              </div>
              <div class="flex items-center justify-between py-3 border-b">
                <span class="text-muted-foreground">总教师数</span>
                <span class="font-semibold">{{ formatNumber(stats.totalTeachers) }}</span>
              </div>
              <div class="flex items-center justify-between py-3 border-b">
                <span class="text-muted-foreground">总兑换码</span>
                <span class="font-semibold">{{ formatNumber(stats.totalRedeemCodes) }}</span>
              </div>
              <div class="flex items-center justify-between py-3">
                <span class="text-muted-foreground">已兑换</span>
                <span class="font-semibold text-green-600">{{ formatNumber(stats.redeemedCodes) }}</span>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </main>
  </div>
</template>
