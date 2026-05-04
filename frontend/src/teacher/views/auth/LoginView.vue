<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { teacherApi } from '@/teacher/api/teacherApi'
import { useAuthStore } from '@/common/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const email = ref('')
const code = ref('')
const step = ref<'email' | 'code'>('email')
const loading = ref(false)
const error = ref('')
const countdown = ref(0)
let countdownTimer: number | null = null

async function handleSendCode() {
  if (!email.value || !email.value.includes('@')) {
    error.value = '请输入有效的邮箱地址'
    return
  }

  loading.value = true
  error.value = ''

  try {
    await teacherApi.sendCode(email.value)
    step.value = 'code'
    startCountdown()
  } catch (err: any) {
    error.value = err.message || '发送验证码失败'
  } finally {
    loading.value = false
  }
}

function startCountdown() {
  countdown.value = 60
  if (countdownTimer) clearInterval(countdownTimer)
  countdownTimer = window.setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) clearInterval(countdownTimer!)
  }, 1000)
}

async function handleVerify() {
  if (code.value.length !== 6) {
    error.value = '请输入6位验证码'
    return
  }

  loading.value = true
  error.value = ''

  try {
    const response = await teacherApi.verifyCode(email.value, code.value)
    authStore.setAuth(response.user, response.token)
    router.push('/teacher/courses')
  } catch (err: any) {
    error.value = err.message || '验证码错误'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-purple-50 to-indigo-100">
    <Card class="w-full max-w-md mx-4">
      <CardHeader class="text-center">
        <div class="mb-4">
          <h1 class="text-3xl font-bold text-primary">esmile edu</h1>
          <p class="text-sm text-muted-foreground mt-1">教师端</p>
        </div>
        <CardTitle class="text-xl">
          {{ step === 'email' ? '教师登录' : '输入验证码' }}
        </CardTitle>
      </CardHeader>
      <CardContent>
        <!-- Email Step -->
        <form v-if="step === 'email'" @submit.prevent="handleSendCode" class="space-y-4">
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
        </form>

        <!-- Code Step -->
        <form v-else @submit.prevent="handleVerify" class="space-y-4">
          <div class="text-center mb-4">
            <p class="text-sm text-muted-foreground">验证码已发送至</p>
            <p class="font-medium">{{ email }}</p>
          </div>

          <div class="space-y-2">
            <Label for="code">验证码</Label>
            <Input
              id="code"
              v-model="code"
              type="text"
              placeholder="请输入6位验证码"
              :disabled="loading"
              maxlength="6"
              class="text-center text-2xl tracking-widest"
            />
          </div>

          <p v-if="error" class="text-sm text-destructive text-center">{{ error }}</p>

          <Button type="submit" class="w-full" :disabled="loading || code.length !== 6">
            {{ loading ? '验证中...' : '验证' }}
          </Button>

          <div class="text-center">
            <Button
              variant="ghost"
              type="button"
              :disabled="countdown > 0"
              @click="handleSendCode"
            >
              {{ countdown > 0 ? `${countdown}s 后重新发送` : '重新发送' }}
            </Button>
          </div>

          <p class="text-center">
            <Button variant="link" @click="step = 'email'">
              返回
            </Button>
          </p>
        </form>
      </CardContent>
    </Card>
  </div>
</template>
