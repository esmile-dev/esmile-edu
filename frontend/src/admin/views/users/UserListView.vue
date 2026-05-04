<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { adminApi } from '@/admin/api/adminApi'
import type { User, UserRole, UserStatus, PageResponse } from '@/common/types/api'
import { useAuthStore } from '@/common/stores/auth'
import { Search, Eye, CheckCircle, XCircle, Users, GraduationCap } from 'lucide-vue-next'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const users = ref<User[]>([])
const loading = ref(true)
const error = ref('')
const page = ref(0)
const totalPages = ref(0)
const totalElements = ref(0)

const roleFilter = ref<UserRole | ''>((route.query.role as UserRole) || '')
const statusFilter = ref<UserStatus | ''>((route.query.status as UserStatus) || '')

async function fetchUsers() {
  loading.value = true
  try {
    const params: any = { page: page.value, size: 20 }
    if (roleFilter.value) params.role = roleFilter.value
    if (statusFilter.value) params.status = statusFilter.value

    const response = await adminApi.getUsers(params)
    users.value = response.content || []
    totalPages.value = response.totalPages || 0
    totalElements.value = response.totalElements || 0
  } catch (err: any) {
    error.value = err.message || '加载失败'
  } finally {
    loading.value = false
  }
}

onMounted(fetchUsers)

watch([roleFilter, statusFilter], () => {
  page.value = 0
  fetchUsers()
})

function goToPage(p: number) {
  page.value = p
  fetchUsers()
}

function viewUser(id: number) {
  router.push(`/admin/users/${id}`)
}

async function approveUser(id: number) {
  try {
    await adminApi.approveTeacher(id)
    await fetchUsers()
  } catch (err: any) {
    alert(err.message || '操作失败')
  }
}

async function disableUser(id: number) {
  if (!confirm('确定要禁用该用户吗?')) return
  try {
    await adminApi.updateUserStatus(id, 'DISABLED')
    await fetchUsers()
  } catch (err: any) {
    alert(err.message || '操作失败')
  }
}

async function enableUser(id: number) {
  try {
    await adminApi.updateUserStatus(id, 'ACTIVE')
    await fetchUsers()
  } catch (err: any) {
    alert(err.message || '操作失败')
  }
}

const roleBadgeVariant = (role: UserRole) => {
  switch (role) {
    case 'ADMIN': return 'default'
    case 'TEACHER': return 'secondary'
    case 'STUDENT': return 'outline'
    default: return 'secondary'
  }
}

const statusBadgeVariant = (status: UserStatus) => {
  switch (status) {
    case 'ACTIVE': return 'default'
    case 'PENDING_APPROVAL': return 'secondary'
    case 'DISABLED': return 'destructive'
    default: return 'secondary'
  }
}

const roleLabel = (role: UserRole) => {
  switch (role) {
    case 'ADMIN': return '管理员'
    case 'TEACHER': return '教师'
    case 'STUDENT': return '学生'
    default: return role
  }
}

const statusLabel = (status: UserStatus) => {
  switch (status) {
    case 'ACTIVE': return '正常'
    case 'PENDING_APPROVAL': return '待审核'
    case 'DISABLED': return '已禁用'
    default: return status
  }
}
</script>

<template>
  <div class="min-h-screen bg-slate-50">
    <!-- Admin Header (same as Dashboard) -->
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

    <main class="pt-16 max-w-7xl mx-auto px-4 py-8">
      <h1 class="text-2xl font-bold mb-6">用户管理</h1>

      <!-- Filters -->
      <Card class="mb-6">
        <CardContent class="p-4">
          <div class="flex flex-wrap gap-4">
            <div class="flex items-center gap-2">
              <Label class="text-sm">角色:</Label>
              <select
                v-model="roleFilter"
                class="h-9 rounded-md border border-input bg-background px-3 text-sm"
              >
                <option value="">全部</option>
                <option value="STUDENT">学生</option>
                <option value="TEACHER">教师</option>
                <option value="ADMIN">管理员</option>
              </select>
            </div>
            <div class="flex items-center gap-2">
              <Label class="text-sm">状态:</Label>
              <select
                v-model="statusFilter"
                class="h-9 rounded-md border border-input bg-background px-3 text-sm"
              >
                <option value="">全部</option>
                <option value="ACTIVE">正常</option>
                <option value="PENDING_APPROVAL">待审核</option>
                <option value="DISABLED">已禁用</option>
              </select>
            </div>
          </div>
        </CardContent>
      </Card>

      <!-- Loading -->
      <div v-if="loading" class="space-y-4">
        <Skeleton v-for="i in 5" :key="i" class="h-16 w-full rounded-lg" />
      </div>

      <!-- Error -->
      <div v-else-if="error" class="text-center py-12">
        <p class="text-destructive mb-4">{{ error }}</p>
        <Button @click="fetchUsers">重试</Button>
      </div>

      <!-- Users Table -->
      <Card v-else>
        <CardContent class="p-0">
          <div class="overflow-x-auto">
            <table class="w-full">
              <thead class="bg-muted/50">
                <tr>
                  <th class="px-4 py-3 text-left text-sm font-medium">ID</th>
                  <th class="px-4 py-3 text-left text-sm font-medium">邮箱</th>
                  <th class="px-4 py-3 text-left text-sm font-medium">昵称</th>
                  <th class="px-4 py-3 text-left text-sm font-medium">角色</th>
                  <th class="px-4 py-3 text-left text-sm font-medium">状态</th>
                  <th class="px-4 py-3 text-right text-sm font-medium">操作</th>
                </tr>
              </thead>
              <tbody class="divide-y">
                <tr v-for="user in users" :key="user.id" class="hover:bg-muted/30">
                  <td class="px-4 py-3 text-sm">{{ user.id }}</td>
                  <td class="px-4 py-3 text-sm">{{ user.email }}</td>
                  <td class="px-4 py-3 text-sm">{{ user.nickname }}</td>
                  <td class="px-4 py-3">
                    <Badge :variant="roleBadgeVariant(user.role)" class="gap-1">
                      <component :is="user.role === 'TEACHER' ? GraduationCap : user.role === 'STUDENT' ? Users : Eye" class="w-3 h-3" />
                      {{ roleLabel(user.role) }}
                    </Badge>
                  </td>
                  <td class="px-4 py-3">
                    <Badge :variant="statusBadgeVariant(user.status)">
                      {{ statusLabel(user.status) }}
                    </Badge>
                  </td>
                  <td class="px-4 py-3 text-right">
                    <div class="flex justify-end gap-2">
                      <Button size="sm" variant="ghost" @click="viewUser(user.id)">
                        <Eye class="w-4 h-4" />
                      </Button>
                      <Button
                        v-if="user.role === 'TEACHER' && user.status === 'PENDING_APPROVAL'"
                        size="sm"
                        variant="default"
                        @click="approveUser(user.id)"
                      >
                        <CheckCircle class="w-4 h-4 mr-1" />
                        审核
                      </Button>
                      <Button
                        v-if="user.status === 'ACTIVE'"
                        size="sm"
                        variant="destructive"
                        @click="disableUser(user.id)"
                      >
                        <XCircle class="w-4 h-4" />
                      </Button>
                      <Button
                        v-if="user.status === 'DISABLED'"
                        size="sm"
                        variant="outline"
                        @click="enableUser(user.id)"
                      >
                        启用
                      </Button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- Pagination -->
          <div class="flex items-center justify-between px-4 py-3 border-t">
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
          </div>
        </CardContent>
      </Card>
    </main>
  </div>
</template>
