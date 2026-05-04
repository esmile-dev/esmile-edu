import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export interface User {
  id: number
  email: string
  nickname: string
  avatar: string | null
  role: 'STUDENT' | 'TEACHER' | 'ADMIN'
  status: 'PENDING_APPROVAL' | 'ACTIVE' | 'DISABLED'
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(sessionStorage.getItem('token'))
  const user = ref<User | null>(null)

  const isAuthenticated = computed(() => !!token.value)
  const isStudent = computed(() => user.value?.role === 'STUDENT')
  const isTeacher = computed(() => user.value?.role === 'TEACHER')
  const isAdmin = computed(() => user.value?.role === 'ADMIN')

  function setAuth(userData: User, tokenValue: string) {
    user.value = userData
    token.value = tokenValue
    sessionStorage.setItem('token', tokenValue)
  }

  function setUser(userData: User) {
    user.value = userData
  }

  function clearAuth() {
    user.value = null
    token.value = null
    sessionStorage.removeItem('token')
  }

  return {
    token,
    user,
    isAuthenticated,
    isStudent,
    isTeacher,
    isAdmin,
    setAuth,
    setUser,
    clearAuth,
  }
})
