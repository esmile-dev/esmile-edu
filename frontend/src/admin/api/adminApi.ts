import { apiClient } from '@/common/utils/apiClient'
import type {
  LoginResponse,
  User,
  Course,
  StatsOverview,
  PageResponse,
  UserStatus,
  CourseStatus,
} from '@/common/types/api'

export const adminApi = {
  // Auth
  async login(email: string, password: string): Promise<LoginResponse> {
    return apiClient.post<LoginResponse>('/api/v1/admin/auth/login', { email, password })
  },

  async getMe(): Promise<User> {
    return apiClient.get<User>('/api/v1/admin/auth/me')
  },

  // Users
  async getUsers(params?: { page?: number; size?: number; role?: string; status?: string }): Promise<PageResponse<User>> {
    return apiClient.get<PageResponse<User>>('/api/v1/admin/users', { params })
  },

  async getUser(id: number): Promise<User> {
    return apiClient.get<User>(`/api/v1/admin/users/${id}`)
  },

  async updateUserStatus(id: number, status: UserStatus): Promise<User> {
    return apiClient.put<User>(`/api/v1/admin/users/${id}/status`, { status })
  },

  async approveTeacher(id: number): Promise<User> {
    return apiClient.post<User>(`/api/v1/admin/users/${id}/approve`)
  },

  // Courses
  async getCourses(params?: { page?: number; size?: number; status?: CourseStatus }): Promise<PageResponse<Course>> {
    return apiClient.get<PageResponse<Course>>('/api/v1/admin/courses', { params })
  },

  async getCourse(id: number): Promise<Course> {
    return apiClient.get<Course>(`/api/v1/admin/courses/${id}`)
  },

  async updateCourseStatus(id: number, status: CourseStatus): Promise<Course> {
    return apiClient.put<Course>(`/api/v1/admin/courses/${id}/status`, { status })
  },

  async deleteCourse(id: number): Promise<void> {
    return apiClient.delete(`/api/v1/admin/courses/${id}`)
  },

  // Stats
  async getStats(): Promise<StatsOverview> {
    return apiClient.get<StatsOverview>('/api/v1/admin/stats')
  },
}
