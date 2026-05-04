<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { studentApi } from '@/student/api/studentApi'
import { useAuthStore } from '@/common/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const email = ref(route.query.email as string || '')
const code = ref('')
const loading = ref(false)
const error = ref('')
const countdown = ref(0)
let countdownTimer: number | null = null

onMounted(() => {
  if (!email.value) {
    router.push('/student/login')
    return
  }
  startCountdown()
})

function startCountdown() {
  countdown.value = 60
  if (countdownTimer) clearInterval(countdownTimer)
  countdownTimer = window.setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(countdownTimer!)
    }
  }, 1000)
}

watch(code, (val) => {
  if (val.length === 6) {
    handleVerify()
  }
})

async function handleVerify() {
  if (code.value.length !== 6) {
    error.value = '请输入6位验证码'
    return
  }

  loading.value = true
  error.value = ''

  try {
    const response = await studentApi.verifyCode(email.value, code.value)
    authStore.setAuth(response.user, response.token)

    const redirect = route.query.redirect as string
    router.push(redirect || '/student/courses')
  } catch (err: any) {
    error.value = err.message || '验证码错误，请重新输入'
    code.value = ''
  } finally {
    loading.value = false
  }
}

async function handleResend() {
  if (countdown.value > 0) return

  loading.value = true
  try {
    await studentApi.sendCode(email.value)
    startCountdown()
    error.value = ''
  } catch (err: any) {
    error.value = err.message || '发送失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

function maskEmail(e: string) {
  const [name, domain] = e.split('@')
  return `${name.slice(0, 2)}***@${domain}`
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-100">
    <Card class="w-full max-w-md mx-4">
      <CardHeader class="text-center">
        <CardTitle class="text-xl">输入验证码</CardTitle>
      </CardHeader>
      <CardContent>
        <div class="text-center mb-6">
          <p class="text-sm text-muted-foreground">验证码已发送至</p>
          <p class="font-medium">{{ maskEmail(email) }}</p>
        </div>

        <form @submit.prevent="handleVerify" class="space-y-4">
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

          <Button
            type="submit"
            class="w-full"
            :disabled="loading || code.length !== 6"
          >
            {{ loading ? '验证中...' : '验证' }}
          </Button>

          <div class="text-center">
            <Button
              variant="ghost"
              type="button"
              :disabled="countdown > 0"
              @click="handleResend"
            >
              {{ countdown > 0 ? `${countdown}s 后可重新发送` : '重新发送验证码' }}
            </Button>
          </div>

          <p class="text-center">
            <Button variant="link" @click="router.push('/student/login')">
              返回修改邮箱
            </Button>
          </p>
        </form>
      </CardContent>
    </Card>
  </div>
</template>
