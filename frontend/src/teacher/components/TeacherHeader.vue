<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/common/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const navItems = [
  { name: '我的课程', path: '/teacher/courses' },
  { name: '统计分析', path: '/teacher/stats' },
]

const isActive = (path: string) => route.path === path

function handleLogout() {
  authStore.clearAuth()
  router.push('/teacher/login')
}

function getInitials(name: string) {
  return name ? name.charAt(0).toUpperCase() : 'T'
}
</script>

<template>
  <header class="h-16 bg-white border-b shadow-sm fixed top-0 left-0 right-0 z-50">
    <div class="h-full max-w-7xl mx-auto px-4 flex items-center justify-between">
      <div class="flex items-center gap-8">
        <router-link to="/teacher/courses" class="flex items-center gap-2">
          <span class="text-xl font-bold text-primary">esmile edu</span>
          <Badge variant="secondary" class="text-xs">教师端</Badge>
        </router-link>

        <nav class="hidden md:flex items-center gap-6">
          <router-link
            v-for="item in navItems"
            :key="item.path"
            :to="item.path"
            :class="[
              'text-sm font-medium transition-colors',
              isActive(item.path)
                ? 'text-primary'
                : 'text-muted-foreground hover:text-foreground'
            ]"
          >
            {{ item.name }}
          </router-link>
        </nav>
      </div>

      <div class="flex items-center gap-4">
        <Button variant="ghost" @click="handleLogout">
          退出
        </Button>
        <Avatar>
          <AvatarFallback class="bg-primary text-primary-foreground">
            {{ getInitials(authStore.user?.nickname || 'T') }}
          </AvatarFallback>
        </Avatar>
      </div>
    </div>
  </header>
</template>

<script lang="ts">
export default {
  components: { Badge }
}
</script>
