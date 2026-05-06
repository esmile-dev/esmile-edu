import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/common/stores/auth'

const routes: RouteRecordRaw[] = [
  // Student routes
  {
    path: '/student/login',
    name: 'student-login',
    component: () => import('@/student/views/auth/LoginView.vue'),
    meta: { guest: true, role: 'STUDENT' },
  },
  {
    path: '/student/send-code',
    name: 'send-code',
    component: () => import('@/student/views/auth/SendCodeView.vue'),
    meta: { guest: true, role: 'STUDENT' },
  },
  {
    path: '/student/courses',
    name: 'student-courses',
    component: () => import('@/student/views/courses/CourseListView.vue'),
    meta: { requiresAuth: true, role: 'STUDENT' },
  },
  {
    path: '/student/courses/:id',
    name: 'student-course-detail',
    component: () => import('@/student/views/courses/CourseDetailView.vue'),
    meta: { requiresAuth: true, role: 'STUDENT' },
  },
  {
    path: '/student/my-courses',
    name: 'my-courses',
    component: () => import('@/student/views/my-courses/MyCoursesView.vue'),
    meta: { requiresAuth: true, role: 'STUDENT' },
  },
  {
    path: '/student/redeem',
    name: 'redeem',
    component: () => import('@/student/views/redeem/RedeemView.vue'),
    meta: { requiresAuth: true, role: 'STUDENT' },
  },
  {
    path: '/student/learn/:courseId/:lessonId',
    name: 'learn',
    component: () => import('@/student/views/learn/LearnView.vue'),
    meta: { requiresAuth: true, role: 'STUDENT' },
  },

  // Teacher routes
  {
    path: '/teacher/login',
    name: 'teacher-login',
    component: () => import('@/teacher/views/auth/LoginView.vue'),
    meta: { guest: true, role: 'TEACHER' },
  },
  {
    path: '/teacher/courses',
    name: 'teacher-courses',
    component: () => import('@/teacher/views/courses/CourseListView.vue'),
    meta: { requiresAuth: true, role: 'TEACHER' },
  },
  {
    path: '/teacher/courses/create',
    name: 'teacher-course-create',
    component: () => import('@/teacher/views/courses/CourseCreateView.vue'),
    meta: { requiresAuth: true, role: 'TEACHER' },
  },
  {
    path: '/teacher/courses/:id/edit',
    name: 'teacher-course-edit',
    component: () => import('@/teacher/views/courses/CourseEditView.vue'),
    meta: { requiresAuth: true, role: 'TEACHER' },
  },
  {
    path: '/teacher/courses/:courseId/lessons/:lessonId',
    name: 'teacher-lesson-edit',
    component: () => import('@/teacher/views/lessons/LessonEditView.vue'),
    meta: { requiresAuth: true, role: 'TEACHER' },
  },
  {
    path: '/teacher/stats',
    name: 'teacher-stats',
    component: () => import('@/teacher/views/stats/StatsView.vue'),
    meta: { requiresAuth: true, role: 'TEACHER' },
  },

  // Admin routes
  {
    path: '/admin/login',
    name: 'admin-login',
    component: () => import('@/admin/views/auth/LoginView.vue'),
    meta: { guest: true, role: 'ADMIN' },
  },
  {
    path: '/admin',
    name: 'admin-dashboard',
    component: () => import('@/admin/views/DashboardView.vue'),
    meta: { requiresAuth: true, role: 'ADMIN' },
  },
  {
    path: '/admin/users',
    name: 'admin-users',
    component: () => import('@/admin/views/users/UserListView.vue'),
    meta: { requiresAuth: true, role: 'ADMIN' },
  },
  {
    path: '/admin/users/:id',
    name: 'admin-user-detail',
    component: () => import('@/admin/views/users/UserDetailView.vue'),
    meta: { requiresAuth: true, role: 'ADMIN' },
  },
  {
    path: '/admin/courses',
    name: 'admin-courses',
    component: () => import('@/admin/views/courses/CourseListView.vue'),
    meta: { requiresAuth: true, role: 'ADMIN' },
  },
  {
    path: '/admin/stats',
    name: 'admin-stats',
    component: () => import('@/admin/views/stats/StatsView.vue'),
    meta: { requiresAuth: true, role: 'ADMIN' },
  },

  // Default redirects
  {
    path: '/',
    redirect: '/student/courses',
  },
  {
    path: '/student',
    redirect: '/student/courses',
  },
  {
    path: '/teacher',
    redirect: '/teacher/courses',
  },
  {
    path: '/admin',
    redirect: '/admin',
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/student/courses',
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    const loginPath = getLoginPath(to.meta.role as string)
    next({ path: loginPath, query: { redirect: to.fullPath } })
    return
  }

  if (to.meta.guest && authStore.isAuthenticated) {
    const defaultPath = getDefaultPath(authStore.user?.role || 'STUDENT')
    next({ path: defaultPath })
    return
  }

  if (to.meta.role && authStore.user?.role && authStore.user.role !== to.meta.role) {
    const defaultPath = getDefaultPath(authStore.user.role)
    next({ path: defaultPath })
    return
  }

  next()
})

function getLoginPath(role: string): string {
  switch (role) {
    case 'TEACHER':
      return '/teacher/login'
    case 'ADMIN':
      return '/admin/login'
    default:
      return '/student/login'
  }
}

function getDefaultPath(role: string): string {
  switch (role) {
    case 'TEACHER':
      return '/teacher/courses'
    case 'ADMIN':
      return '/admin'
    default:
      return '/student/courses'
  }
}

export default router
