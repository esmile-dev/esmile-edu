import { apiClient } from '@/common/utils/apiClient'
import type {
  LoginResponse,
  User,
  Course,
  CourseDetailForTeacher,
  CreateCourseRequest,
  UpdateCourseRequest,
  CreateChapterRequest,
  UpdateChapterRequest,
  CreateLessonRequest,
  UpdateLessonRequest,
  PageResponse,
  Lesson,
  VideoUploadSignature,
} from '@/common/types/api'

export const teacherApi = {
  // Auth
  async sendCode(email: string): Promise<void> {
    return apiClient.post('/api/v1/teacher/auth/send-code', { email })
  },

  async verifyCode(email: string, code: string): Promise<LoginResponse> {
    return apiClient.post<LoginResponse>('/api/v1/teacher/auth/verify-code', { email, code })
  },

  async getMe(): Promise<User> {
    return apiClient.get<User>('/api/v1/teacher/auth/me')
  },

  // Courses - 我的课程
  async getCourses(params?: { page?: number; size?: number }): Promise<PageResponse<Course>> {
    return apiClient.get<PageResponse<Course>>('/api/v1/teacher/my-courses', params)
  },

  async getCourseDetail(id: number): Promise<CourseDetailForTeacher> {
    return apiClient.get<CourseDetailForTeacher>(`/api/v1/teacher/courses/${id}`)
  },

  async createCourse(data: CreateCourseRequest): Promise<Course> {
    return apiClient.post<Course>('/api/v1/teacher/courses', data)
  },

  async updateCourse(id: number, data: UpdateCourseRequest): Promise<Course> {
    return apiClient.put<Course>(`/api/v1/teacher/courses/${id}`, data)
  },

  async deleteCourse(id: number): Promise<void> {
    return apiClient.delete(`/api/v1/teacher/courses/${id}`)
  },

  async publishCourse(id: number): Promise<{ id: number; status: string }> {
    return apiClient.put(`/api/v1/teacher/courses/${id}/publish`)
  },

  // Chapters
  async createChapter(data: CreateChapterRequest): Promise<any> {
    return apiClient.post('/api/v1/teacher/chapters', data)
  },

  async updateChapter(id: number, data: UpdateChapterRequest): Promise<any> {
    return apiClient.put(`/api/v1/teacher/chapters/${id}`, data)
  },

  async deleteChapter(id: number): Promise<void> {
    return apiClient.delete(`/api/v1/teacher/chapters/${id}`)
  },

  // Lessons
  async createLesson(data: CreateLessonRequest): Promise<any> {
    return apiClient.post('/api/v1/teacher/lessons', data)
  },

  async updateLesson(id: number, data: UpdateLessonRequest): Promise<any> {
    return apiClient.put(`/api/v1/teacher/lessons/${id}`, data)
  },

  async deleteLesson(id: number): Promise<void> {
    return apiClient.delete(`/api/v1/teacher/lessons/${id}`)
  },

  async getLesson(id: number): Promise<Lesson> {
    return apiClient.get<Lesson>(`/api/v1/teacher/lessons/${id}`)
  },

  // Video
  async applyUpload(data: { fileName: string; fileSize: number }): Promise<VideoUploadSignature> {
    return apiClient.get<VideoUploadSignature>('/api/v1/teacher/video/apply-upload', { params: data })
  },

  async commitUpload(data: { lessonId: number; videoId: string }): Promise<Lesson> {
    return apiClient.post<Lesson>('/api/v1/teacher/video/commit-upload', data)
  },

  async getStats(): Promise<{
    totalStudents: number
    activeEnrollments: number
    totalCourses: number
    publishedCourses: number
    totalLessons: number
    avgProgress: number
  }> {
    return apiClient.get('/api/v1/teacher/stats')
  },
}
