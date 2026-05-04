<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { adminApi } from '@/admin/api/adminApi'
import { useAuthStore } from '@/common/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const email = ref('')
const password = ref('')
const loading = ref(false)
const error = ref('')

async function handleLogin() {
  if (!email.value || !password.value) {
    error.value = '请输入邮箱和密码'
    return
  }

  loading.value = true
  error.value = ''

  try {
    const response = await adminApi.login(email.value, password.value)
    authStore.setAuth(response.user, response.token)
    router.push('/admin')
  } catch (err: any) {
    error.value = err.message || '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-100 to-slate-200">
    <Card class="w-full max-w-md mx-4">
      <CardHeader class="text-center">
        <div class="mb-4">
          <h1 class="text-3xl font-bold text-primary">esmile edu</h1>
          <p class="text-sm text-muted-foreground mt-1">管理端</p>
        </div>
        <CardTitle class="text-xl">管理员登录</CardTitle>
      </CardHeader>
      <CardContent>
        <form @submit.prevent="handleLogin" class="space-y-4">
          <div class="space-y-2">
            <Label for="email">邮箱地址</Label>
            <Input
              id="email"
              v-model="email"
              type="email"
              placeholder="请输入管理员邮箱"
              :disabled="loading"
            />
          </div>

          <div class="space-y-2">
            <Label for="password">密码</Label>
            <Input
              id="password"
              v-model="password"
              type="password"
              placeholder="请输入密码"
              :disabled="loading"
            />
          </div>

          <p v-if="error" class="text-sm text-destructive">{{ error }}</p>

          <Button type="submit" class="w-full" :disabled="loading">
            {{ loading ? '登录中...' : '登录' }}
          </Button>
        </form>
      </CardContent>
    </Card>
  </div>
</template>
