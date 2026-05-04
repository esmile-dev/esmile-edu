import axios, { type AxiosInstance, type AxiosError } from 'axios'
import { mockApi } from '@/common/mock/mockApi'

export class ApiError extends Error {
  constructor(
    public code: number,
    message: string
  ) {
    super(message)
    this.name = 'ApiError'
  }
}

// Check if we should use mock API
const useMock = import.meta.env.VITE_USE_MOCK === 'true' || import.meta.env.VITE_USE_MOCK === '1'

// Mock API caller that matches the apiClient interface
class MockApiCaller {
  private getPath(url: string): string {
    // Strip base URL and api version
    return url.replace('/api/v1', '')
  }

  private getIdFromUrl(url: string): number | undefined {
    const match = url.match(/\/(\d+)/)
    return match ? Number(match[1]) : undefined
  }

  async get<T>(url: string, params?: object): Promise<T> {
    const path = this.getPath(url)
    const id = this.getIdFromUrl(path)

    await new Promise(r => setTimeout(r, 200 + Math.random() * 300))

    // Student Auth
    if (path === '/student/auth/send-code') {
      return mockApi.sendCode((params as any)?.email || '') as T
    }
    if (path === '/student/auth/verify-code') {
      return mockApi.verifyCode((params as any)?.email || '', (params as any)?.code || '') as T
    }

    // Student Courses
    if (path === '/student/courses') {
      return mockApi.getCourses(params as any) as T
    }
    if (path.match(/^\/student\/courses\/\d+$/)) {
      return mockApi.getCourse(id!) as T
    }
    if (path === '/student/my-courses') {
      return mockApi.getMyCourses() as T
    }
    if (path === '/student/redeem') {
      return mockApi.redeemCode((params as any)?.code || '') as T
    }

    // Teacher Auth
    if (path === '/teacher/auth/send-code') {
      return mockApi.teacherSendCode((params as any)?.email || '') as T
    }
    if (path === '/teacher/auth/verify-code') {
      return mockApi.teacherVerifyCode((params as any)?.email || '', (params as any)?.code || '') as T
    }
    if (path === '/teacher/auth/me') {
      return mockApi.getTeacherMe() as T
    }

    // Teacher Courses
    if (path === '/teacher/courses' && !id) {
      return mockApi.getTeacherCourses(params as any) as T
    }
    if (path.match(/^\/teacher\/courses\/\d+$/) && params && (params as any)?.title) {
      // POST create course
      return mockApi.createTeacherCourse(params as any) as T
    }
    if (path.match(/^\/teacher\/courses\/\d+$/) && !path.includes('/publish') && params && Object.keys(params).length > 0) {
      // PUT update course (has body)
      return mockApi.updateTeacherCourse(id!, params as any) as T
    }
    if (path.match(/^\/teacher\/courses\/\d+$/) && !path.includes('/publish')) {
      // GET course detail
      return mockApi.getTeacherCourse(id!) as T
    }
    if (path.match(/^\/teacher\/courses\/\d+\/publish$/)) {
      return mockApi.publishTeacherCourse(id!) as T
    }

    // Teacher Chapters
    if (path === '/teacher/chapters' && params && (params as any)?.courseId) {
      return mockApi.createChapter(params as any) as T
    }
    if (path.match(/^\/teacher\/chapters\/\d+$/) && params && Object.keys(params).length > 0) {
      return mockApi.updateChapter(id!, params as any) as T
    }
    if (path.match(/^\/teacher\/chapters\/\d+$/) && (!params || Object.keys(params).length === 0)) {
      return mockApi.deleteChapter(id!) as T
    }

    // Teacher Lessons
    if (path === '/teacher/lessons' && params && (params as any)?.chapterId) {
      return mockApi.createLesson(params as any) as T
    }
    if (path.match(/^\/teacher\/lessons\/\d+$/) && params && Object.keys(params).length > 0) {
      return mockApi.updateLesson(id!, params as any) as T
    }
    if (path.match(/^\/teacher\/lessons\/\d+$/) && (!params || Object.keys(params).length === 0)) {
      return mockApi.deleteLesson(id!) as T
    }

    // Teacher Video
    if (path === '/teacher/video/apply-upload') {
      return mockApi.applyUpload(params as any) as T
    }
    if (path === '/teacher/video/commit-upload') {
      return mockApi.commitUpload(params as any) as T
    }

    // Teacher Stats
    if (path === '/teacher/stats') {
      return mockApi.getTeacherStats() as T
    }

    // Admin Auth
    if (path === '/admin/auth/login') {
      return mockApi.adminLogin((params as any)?.email || '', (params as any)?.password || '') as T
    }
    if (path === '/admin/auth/me') {
      return mockApi.getAdminMe() as T
    }

    // Admin Users
    if (path === '/admin/users') {
      return mockApi.getUsers(params as any) as T
    }
    if (path.match(/^\/admin\/users\/\d+$/) && !path.includes('/approve') && !path.includes('/status')) {
      return mockApi.getUser(id!) as T
    }
    if (path.match(/^\/admin\/users\/\d+\/approve$/)) {
      return mockApi.approveTeacher(id!) as T
    }
    if (path.match(/^\/admin\/users\/\d+\/status$/)) {
      return mockApi.updateUserStatus(id!, (params as any)?.status || '') as T
    }

    // Admin Courses
    if (path === '/admin/courses') {
      return mockApi.getAdminCourses(params as any) as T
    }
    if (path.match(/^\/admin\/courses\/\d+$/) && !path.includes('/status')) {
      return mockApi.getAdminCourse(id!) as T
    }
    if (path.match(/^\/admin\/courses\/\d+\/status$/)) {
      return mockApi.updateCourseStatus(id!, (params as any)?.status || '') as T
    }
    if (path.match(/^\/admin\/courses\/\d+$/) && (!params || Object.keys(params).length === 0)) {
      return mockApi.deleteCourse(id!) as T
    }

    // Admin Stats
    if (path === '/admin/stats') {
      return mockApi.getStats() as T
    }

    throw new Error(`Mock not implemented for GET: ${url}`)
  }

  async post<T>(url: string, data?: object): Promise<T> {
    const path = this.getPath(url)
    const id = this.getIdFromUrl(path)

    await new Promise(r => setTimeout(r, 200 + Math.random() * 300))

    // Student Auth
    if (path === '/student/auth/send-code') {
      return mockApi.sendCode((data as any)?.email || '') as T
    }
    if (path === '/student/auth/verify-code') {
      return mockApi.verifyCode((data as any)?.email || '', (data as any)?.code || '') as T
    }

    // Student Redeem
    if (path === '/student/redeem') {
      return mockApi.redeemCode((data as any)?.code || '') as T
    }

    // Teacher Auth
    if (path === '/teacher/auth/send-code') {
      return mockApi.teacherSendCode((data as any)?.email || '') as T
    }
    if (path === '/teacher/auth/verify-code') {
      return mockApi.teacherVerifyCode((data as any)?.email || '', (data as any)?.code || '') as T
    }

    // Teacher Courses
    if (path === '/teacher/courses') {
      return mockApi.createTeacherCourse(data as any) as T
    }
    if (path.match(/^\/teacher\/courses\/\d+\/publish$/)) {
      return mockApi.publishTeacherCourse(id!) as T
    }

    // Teacher Chapters
    if (path === '/teacher/chapters') {
      return mockApi.createChapter(data as any) as T
    }

    // Teacher Lessons
    if (path === '/teacher/lessons') {
      return mockApi.createLesson(data as any) as T
    }

    // Teacher Video
    if (path === '/teacher/video/commit-upload') {
      return mockApi.commitUpload(data as any) as T
    }

    // Admin Auth
    if (path === '/admin/auth/login') {
      return mockApi.adminLogin((data as any)?.email || '', (data as any)?.password || '') as T
    }

    // Admin Users
    if (path.match(/^\/admin\/users\/\d+\/approve$/)) {
      return mockApi.approveTeacher(id!) as T
    }

    throw new Error(`Mock not implemented for POST: ${url}`)
  }

  async put<T>(url: string, data?: object): Promise<T> {
    const path = this.getPath(url)
    const id = this.getIdFromUrl(path)

    await new Promise(r => setTimeout(r, 200 + Math.random() * 300))

    // Teacher Courses
    if (path.match(/^\/teacher\/courses\/\d+$/)) {
      return mockApi.updateTeacherCourse(id!, data as any) as T
    }

    // Teacher Chapters
    if (path.match(/^\/teacher\/chapters\/\d+$/)) {
      return mockApi.updateChapter(id!, data as any) as T
    }

    // Teacher Lessons
    if (path.match(/^\/teacher\/lessons\/\d+$/)) {
      return mockApi.updateLesson(id!, data as any) as T
    }

    // Admin Users
    if (path.match(/^\/admin\/users\/\d+\/status$/)) {
      return mockApi.updateUserStatus(id!, (data as any)?.status || '') as T
    }

    // Admin Courses
    if (path.match(/^\/admin\/courses\/\d+\/status$/)) {
      return mockApi.updateCourseStatus(id!, (data as any)?.status || '') as T
    }

    throw new Error(`Mock not implemented for PUT: ${url}`)
  }

  async delete<T>(url: string): Promise<T> {
    const path = this.getPath(url)
    const id = this.getIdFromUrl(path)

    await new Promise(r => setTimeout(r, 200 + Math.random() * 300))

    // Teacher Courses
    if (path.match(/^\/teacher\/courses\/\d+$/)) {
      return mockApi.deleteTeacherCourse(id!) as T
    }

    // Teacher Chapters
    if (path.match(/^\/teacher\/chapters\/\d+$/)) {
      return mockApi.deleteChapter(id!) as T
    }

    // Teacher Lessons
    if (path.match(/^\/teacher\/lessons\/\d+$/)) {
      return mockApi.deleteLesson(id!) as T
    }

    // Admin Courses
    if (path.match(/^\/admin\/courses\/\d+$/)) {
      return mockApi.deleteCourse(id!) as T
    }

    throw new Error(`Mock not implemented for DELETE: ${url}`)
  }
}

// Real API client
class RealApiClient {
  private client: AxiosInstance

  constructor() {
    this.client = axios.create({
      baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
      timeout: 30000,
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
      },
    })

    this.setupInterceptors()
  }

  private setupInterceptors() {
    this.client.interceptors.request.use(
      (config) => {
        const token = sessionStorage.getItem('token')
        if (token) {
          config.headers.Authorization = `Bearer ${token}`
        }
        return config
      },
      (error) => Promise.reject(error)
    )

    this.client.interceptors.response.use(
      (response) => response,
      async (error: AxiosError) => {
        if (error.response?.status === 401) {
          sessionStorage.removeItem('token')
          window.location.href = '/login'
        }
        return Promise.reject(error)
      }
    )
  }

  async get<T>(url: string, params?: object): Promise<T> {
    const response = await this.client.get<{ code: number; message: string; data: T | null }>(url, { params })
    return this.handleResponse(response)
  }

  async post<T>(url: string, data?: object): Promise<T> {
    const response = await this.client.post<{ code: number; message: string; data: T | null }>(url, data)
    return this.handleResponse(response)
  }

  async put<T>(url: string, data?: object): Promise<T> {
    const response = await this.client.put<{ code: number; message: string; data: T | null }>(url, data)
    return this.handleResponse(response)
  }

  async delete<T>(url: string): Promise<T> {
    const response = await this.client.delete<{ code: number; message: string; data: T | null }>(url)
    return this.handleResponse(response)
  }

  private handleResponse<T>(response: axios.AxiosResponse<{ code: number; message: string; data: T | null }>): T {
    const { code, message, data } = response.data
    if (code >= 200 && code < 300) {
      return data as T
    }
    throw new ApiError(code, message)
  }
}

// Export the appropriate client based on environment
export const apiClient = useMock ? new MockApiCaller() : new RealApiClient()

// Log which mode is active
if (useMock) {
  console.log('[Mock API] Using mock data for demo')
}
