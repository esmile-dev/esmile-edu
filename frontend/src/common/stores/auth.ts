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

const USER_KEY = 'user'

function getStoredUser(): User | null {
  try {
    const stored = sessionStorage.getItem(USER_KEY)
    return stored ? JSON.parse(stored) : null
  } catch {
    return null
  }
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(sessionStorage.getItem('token'))
  const user = ref<User | null>(getStoredUser())
  let initialized = false

  const isAuthenticated = computed(() => !!token.value)
  const isStudent = computed(() => user.value?.role === 'STUDENT')
  const isTeacher = computed(() => user.value?.role === 'TEACHER')
  const isAdmin = computed(() => user.value?.role === 'ADMIN')

  function setAuth(userData: User, tokenValue: string) {
    user.value = userData
    token.value = tokenValue
    sessionStorage.setItem('token', tokenValue)
    sessionStorage.setItem(USER_KEY, JSON.stringify(userData))
  }

  function setUser(userData: User) {
    user.value = userData
    sessionStorage.setItem(USER_KEY, JSON.stringify(userData))
  }

  function clearAuth() {
    user.value = null
    token.value = null
    sessionStorage.removeItem('token')
    sessionStorage.removeItem(USER_KEY)
  }

  function initAuth() {
    if (!initialized) {
      initialized = true
      token.value = sessionStorage.getItem('token')
      user.value = getStoredUser()
    }
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
    initAuth,
  }
})
