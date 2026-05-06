<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/common/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const showMenu = ref(false)

const navItems = [
  { name: '首页', path: '/student/courses' },
  { name: '课程', path: '/student/courses' },
  { name: '我的', path: '/student/my-courses' },
  { name: '兑换', path: '/student/redeem' },
]

const isActive = (path: string) => route.path === path

function handleLogout() {
  authStore.clearAuth()
  router.push('/student/login')
}

function getInitials(name: string) {
  return name ? name.charAt(0).toUpperCase() : 'U'
}

function toggleMenu() {
  showMenu.value = !showMenu.value
}

function closeMenu() {
  showMenu.value = false
}
</script>

<template>
  <!-- Mobile Top Header -->
  <header class="md:hidden h-14 bg-white border-b shadow-sm fixed top-0 left-0 right-0 z-50">
    <div class="h-full max-w-7xl mx-auto px-4 flex items-center justify-between">
      <router-link to="/student/courses" class="flex items-center gap-2">
        <span class="text-lg font-bold text-primary">esmile edu</span>
      </router-link>

      <button
        class="h-8 w-8 rounded-full bg-muted flex items-center justify-center"
        @click="toggleMenu"
      >
        <span class="text-xs font-medium text-primary">
          {{ getInitials(authStore.user?.nickname || 'U') }}
        </span>
      </button>
    </div>

    <div
      v-if="showMenu"
      class="absolute right-4 mt-2 w-48 bg-white rounded-md border shadow-lg py-1 z-50"
    >
      <div class="px-4 py-2 text-sm font-medium border-b">
        {{ authStore.user?.nickname }}
      </div>
      <button
        class="w-full text-left px-4 py-2 text-sm text-muted-foreground hover:bg-muted"
        @click="handleLogout"
      >
        退出登录
      </button>
    </div>

    <div
      v-if="showMenu"
      class="fixed inset-0 z-40 bg-black/20"
      @click="closeMenu"
    />
  </header>

  <!-- Desktop Top Header -->
  <header class="hidden md:block h-16 bg-white border-b shadow-sm fixed top-0 left-0 right-0 z-50">
    <div class="h-full max-w-7xl mx-auto px-4 flex items-center justify-between">
      <router-link to="/student/courses" class="flex items-center gap-2">
        <span class="text-xl font-bold text-primary">esmile edu</span>
      </router-link>

      <nav class="flex items-center gap-6">
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

      <div class="relative">
        <button
          class="relative h-10 w-10 rounded-full bg-muted flex items-center justify-center"
          @click="toggleMenu"
        >
          <span class="text-sm font-medium text-primary">
            {{ getInitials(authStore.user?.nickname || 'U') }}
          </span>
        </button>

        <div
          v-if="showMenu"
          class="absolute right-0 mt-2 w-48 bg-white rounded-md border shadow-lg py-1 z-50"
        >
          <div class="px-4 py-2 text-sm font-medium border-b">
            {{ authStore.user?.nickname }}
          </div>
          <button
            class="w-full text-left px-4 py-2 text-sm text-muted-foreground hover:bg-muted"
            @click="handleLogout"
          >
            退出登录
          </button>
        </div>

        <div
          v-if="showMenu"
          class="fixed inset-0 z-40"
          @click="closeMenu"
        />
      </div>
    </div>
  </header>

  <!-- Mobile Bottom Navigation -->
  <nav class="md:hidden fixed bottom-0 left-0 right-0 bg-white border-t shadow-lg z-50 h-16">
    <div class="flex justify-around items-center h-full">
      <router-link
        v-for="item in [
          { name: '首页', path: '/student/courses', icon: 'M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6' },
          { name: '课程', path: '/student/courses', icon: 'M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253' },
          { name: '我的', path: '/student/my-courses', icon: 'M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z' },
          { name: '兑换', path: '/student/redeem', icon: 'M12 8v13m0-13V6a2 2 0 10-4 0v2m0 0V8a2 2 0 10-4 0m4 4h4m-4 0a2 2 0 110 4h4a2 2 0 110 4' },
        ]"
        :key="item.path"
        :to="item.path"
        class="flex flex-col items-center justify-center flex-1 h-full text-xs transition-colors"
        :class="isActive(item.path) ? 'text-primary' : 'text-muted-foreground'"
      >
        <svg
          class="w-6 h-6 mb-1"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
          stroke-width="1.5"
        >
          <path stroke-linecap="round" stroke-linejoin="round" :d="item.icon" />
        </svg>
        {{ item.name }}
      </router-link>
    </div>
  </nav>
</template>
