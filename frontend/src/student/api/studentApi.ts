import { apiClient } from '@/common/utils/apiClient'
import type {
  LoginResponse,
  User,
  Course,
  CourseDetail,
  MyCourse,
  RedeemResult,
  PageResponse,
} from '@/common/types/api'

export const studentApi = {
  // Auth
  async sendCode(email: string): Promise<void> {
    return apiClient.post('/api/v1/student/auth/send-code', { email })
  },

  async verifyCode(email: string, code: string): Promise<LoginResponse> {
    return apiClient.post<LoginResponse>('/api/v1/student/auth/verify-code', { email, code })
  },

  async getMe(): Promise<User> {
    return apiClient.get<User>('/api/v1/student/auth/me')
  },

  // Courses
  async getCourses(params?: {
    page?: number
    size?: number
    sort?: string
  }): Promise<PageResponse<Course>> {
    return apiClient.get<PageResponse<Course>>('/api/v1/student/courses', params)
  },

  async getCourseDetail(id: number): Promise<CourseDetail> {
    return apiClient.get<CourseDetail>(`/api/v1/student/courses/${id}`)
  },

  // Redeem
  async redeemCode(code: string): Promise<RedeemResult> {
    return apiClient.post<RedeemResult>('/api/v1/student/codes/redeem', { code })
  },

  // My Courses
  async getMyCourses(params?: {
    page?: number
    size?: number
  }): Promise<PageResponse<MyCourse>> {
    return apiClient.get<PageResponse<MyCourse>>('/api/v1/student/my-courses', params)
  },
}
