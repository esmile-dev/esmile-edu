<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { studentApi } from '@/student/api/studentApi'
import { useAuthStore } from '@/common/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const email = ref('')
const loading = ref(false)
const error = ref('')

async function handleSendCode() {
  if (!email.value || !email.value.includes('@')) {
    error.value = '请输入有效的邮箱地址'
    return
  }

  loading.value = true
  error.value = ''

  try {
    await studentApi.sendCode(email.value)
    router.push({
      path: '/student/send-code',
      query: { email: email.value, redirect: route.query.redirect as string },
    })
  } catch (err: any) {
    error.value = err.message || '发送验证码失败，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-100">
    <Card class="w-full max-w-md mx-4">
      <CardHeader class="text-center">
        <div class="mb-4">
          <h1 class="text-3xl font-bold text-primary">esmile edu</h1>
          <p class="text-sm text-muted-foreground mt-1">在线教育平台</p>
        </div>
        <CardTitle class="text-xl">学生登录</CardTitle>
      </CardHeader>
      <CardContent>
        <form @submit.prevent="handleSendCode" class="space-y-4">
          <div class="space-y-2">
            <Label for="email">邮箱地址</Label>
            <Input
              id="email"
              v-model="email"
              type="email"
              placeholder="请输入邮箱"
              :disabled="loading"
            />
          </div>

          <p v-if="error" class="text-sm text-destructive">{{ error }}</p>

          <Button type="submit" class="w-full" :disabled="loading">
            {{ loading ? '发送中...' : '发送验证码' }}
          </Button>

          <p class="text-center text-sm text-muted-foreground">
            已有账号? 请使用验证码登录
          </p>
        </form>
      </CardContent>
    </Card>
  </div>
</template>
