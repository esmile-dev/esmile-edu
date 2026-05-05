<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { studentApi } from '@/student/api/studentApi'
import StudentHeader from '@/student/components/StudentHeader.vue'
import { Gift } from 'lucide-vue-next'

const router = useRouter()

const code = ref('')
const loading = ref(false)
const error = ref('')
const success = ref(false)
const courseTitle = ref('')

async function handleRedeem() {
  if (!code.value || code.value.length < 4) {
    error.value = '请输入有效的兑换码'
    return
  }

  loading.value = true
  error.value = ''

  try {
    const result = await studentApi.redeemCode(code.value.toUpperCase())
    // Backend returns data directly without success flag when successful
    if (result && result.courseTitle) {
      success.value = true
      courseTitle.value = result.courseTitle
    } else {
      error.value = result?.errorMessage || '兑换失败'
    }
  } catch (err: any) {
    error.value = err.message || '兑换失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

function goToCourse() {
  router.push('/student/my-courses')
}

function goToLearn() {
  router.push('/student/my-courses')
}
</script>

<template>
  <div class="min-h-screen bg-background">
    <StudentHeader />

    <main class="pt-14 md:pt-16 pb-20 md:pb-0 max-w-xl mx-auto px-4 py-8">
      <!-- Redeem Form -->
      <Card v-if="!success">
        <CardHeader class="text-center">
          <div class="mx-auto mb-4 w-16 h-16 bg-primary/10 rounded-full flex items-center justify-center">
            <Gift class="w-8 h-8 text-primary" />
          </div>
          <CardTitle class="text-2xl">兑换码兑换</CardTitle>
        </CardHeader>
        <CardContent>
          <form @submit.prevent="handleRedeem" class="space-y-4">
            <div class="space-y-2">
              <Label for="code">兑换码</Label>
              <Input
                id="code"
                v-model="code"
                placeholder="请输入兑换码"
                :disabled="loading"
                class="text-center text-lg tracking-widest uppercase"
              />
            </div>

            <p v-if="error" class="text-sm text-destructive text-center">{{ error }}</p>

            <Button type="submit" class="w-full" :disabled="loading">
              {{ loading ? '兑换中...' : '立即兑换' }}
            </Button>

            <p class="text-center text-sm text-muted-foreground">
              <Button variant="link" class="text-sm" @click="router.push('/student/courses')">
                还没有兑换码? 去浏览课程
              </Button>
            </p>
          </form>
        </CardContent>
      </Card>

      <!-- Success -->
      <Card v-else>
        <CardContent class="p-8 text-center">
          <div class="mx-auto mb-4 w-16 h-16 bg-green-100 rounded-full flex items-center justify-center">
            <span class="text-3xl">✓</span>
          </div>
          <h2 class="text-2xl font-bold mb-2">兑换成功!</h2>
          <p class="text-muted-foreground mb-6">
            已获得课程: <span class="font-medium text-foreground">{{ courseTitle }}</span>
          </p>
          <div class="flex gap-4">
            <Button variant="outline" class="flex-1" @click="goToCourse">
              查看课程
            </Button>
            <Button class="flex-1" @click="goToLearn">
              开始学习
            </Button>
          </div>
        </CardContent>
      </Card>

      <!-- Instructions -->
      <Card class="mt-6">
        <CardContent class="p-4">
          <h3 class="font-medium mb-2">兑换说明</h3>
          <ul class="text-sm text-muted-foreground space-y-1">
            <li>1. 输入正确的兑换码</li>
            <li>2. 点击"立即兑换"按钮</li>
            <li>3. 兑换成功后自动添加到您的课程</li>
            <li>4. 每位用户每个课程只能兑换一次</li>
          </ul>
        </CardContent>
      </Card>
    </main>
  </div>
</template>
