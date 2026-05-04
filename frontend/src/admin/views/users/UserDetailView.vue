<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { adminApi } from '@/admin/api/adminApi'
import type { User } from '@/common/types/api'
import { useAuthStore } from '@/common/stores/auth'
import { ArrowLeft, CheckCircle, XCircle, Mail, User as UserIcon, Shield } from 'lucide-vue-next'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const userId = Number(route.params.id)
const user = ref<User | null>(null)
const loading = ref(true)
const error = ref('')

onMounted(async () => {
  try {
    user.value = await adminApi.getUser(userId)
  } catch (err: any) {
    error.value = err.message || '加载失败'
  } finally {
    loading.value = false
  }
})

function goBack() {
  router.push('/admin/users')
}

async function approveUser() {
  try {
    await adminApi.approveTeacher(userId)
    user.value = await adminApi.getUser(userId)
  } catch (err: any) {
    alert(err.message || '操作失败')
  }
}

async function disableUser() {
  if (!confirm('确定要禁用该用户吗?')) return
  try {
    await adminApi.updateUserStatus(userId, 'DISABLED')
    user.value = await adminApi.getUser(userId)
  } catch (err: any) {
    alert(err.message || '操作失败')
  }
}

async function enableUser() {
  try {
    await adminApi.updateUserStatus(userId, 'ACTIVE')
    user.value = await adminApi.getUser(userId)
  } catch (err: any) {
    alert(err.message || '操作失败')
  }
}

const statusBadgeVariant = (status: string) => {
  switch (status) {
    case 'ACTIVE': return 'default'
    case 'PENDING_APPROVAL': return 'secondary'
    case 'DISABLED': return 'destructive'
    default: return 'secondary'
  }
}

const statusLabel = (status: string) => {
  switch (status) {
    case 'ACTIVE': return '正常'
    case 'PENDING_APPROVAL': return '待审核'
    case 'DISABLED': return '已禁用'
    default: return status
  }
}

const roleLabel = (role: string) => {
  switch (role) {
    case 'ADMIN': return '管理员'
    case 'TEACHER': return '教师'
    case 'STUDENT': return '学生'
    default: return role
  }
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
              class="text-sm font-medium text-foreground"
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

    <main class="pt-16 max-w-3xl mx-auto px-4 py-8">
      <Button variant="ghost" @click="goBack" class="mb-4">
        <ArrowLeft class="w-4 h-4 mr-1" />
        返回用户列表
      </Button>

      <!-- Loading -->
      <div v-if="loading">
        <Skeleton class="h-8 w-48 mb-4" />
        <Skeleton class="h-64 w-full rounded-lg" />
      </div>

      <!-- Error -->
      <div v-else-if="error" class="text-center py-12">
        <p class="text-destructive mb-4">{{ error }}</p>
        <Button @click="goBack">返回</Button>
      </div>

      <!-- User Detail -->
      <div v-else-if="user">
        <div class="flex items-center justify-between mb-6">
          <h1 class="text-2xl font-bold">用户详情</h1>
          <Badge :variant="statusBadgeVariant(user.status)">
            {{ statusLabel(user.status) }}
          </Badge>
        </div>

        <div class="space-y-6">
          <!-- Basic Info -->
          <Card>
            <CardHeader>
              <CardTitle class="text-base">基本信息</CardTitle>
            </CardHeader>
            <CardContent class="space-y-4">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 bg-muted rounded-full flex items-center justify-center">
                  <UserIcon class="w-5 h-5 text-muted-foreground" />
                </div>
                <div>
                  <p class="font-medium">{{ user.nickname }}</p>
                  <p class="text-sm text-muted-foreground">ID: {{ user.id }}</p>
                </div>
              </div>

              <div class="grid grid-cols-2 gap-4">
                <div class="flex items-center gap-2 text-sm">
                  <Mail class="w-4 h-4 text-muted-foreground" />
                  <span>{{ user.email }}</span>
                </div>
                <div class="flex items-center gap-2 text-sm">
                  <Shield class="w-4 h-4 text-muted-foreground" />
                  <span>{{ roleLabel(user.role) }}</span>
                </div>
              </div>
            </CardContent>
          </Card>

          <!-- Actions -->
          <Card>
            <CardHeader>
              <CardTitle class="text-base">管理操作</CardTitle>
            </CardHeader>
            <CardContent class="space-y-4">
              <div class="flex flex-wrap gap-2">
                <Button
                  v-if="user.role === 'TEACHER' && user.status === 'PENDING_APPROVAL'"
                  @click="approveUser"
                >
                  <CheckCircle class="w-4 h-4 mr-1" />
                  审核通过
                </Button>
                <Button
                  v-if="user.status === 'ACTIVE'"
                  variant="destructive"
                  @click="disableUser"
                >
                  <XCircle class="w-4 h-4 mr-1" />
                  禁用用户
                </Button>
                <Button
                  v-if="user.status === 'DISABLED'"
                  variant="outline"
                  @click="enableUser"
                >
                  启用用户
                </Button>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </main>
  </div>
</template>
