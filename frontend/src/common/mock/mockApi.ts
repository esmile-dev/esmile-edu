import {
  mockUsers,
  mockCourses,
  mockCourseDetails,
  mockStats,
  mockEnrollments,
  mockRedeemResult,
  mockTeacherCourses,
  mockTeacherStats,
} from './mockData'

// Simulate network delay
const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms))
const randomDelay = () => delay(200 + Math.random() * 300)

// Current user for auth simulation
let currentUser: any = null
let currentToken: string | null = null

export const mockApi = {
  // Student Auth
  async sendCode(email: string) {
    await randomDelay()
    console.log('[Mock] sendCode:', email)
    return { message: '验证码已发送' }
  },

  async verifyCode(email: string, code: string) {
    await randomDelay()
    console.log('[Mock] verifyCode:', email, code)
    if (code !== '123456' && code.length !== 6) {
      throw new Error('验证码错误')
    }
    currentUser = mockUsers.find(u => u.email === email) || mockUsers[3] // Default to student
    currentToken = 'mock-jwt-token-' + Date.now()
    return {
      token: currentToken,
      expiresIn: 86400,
      user: currentUser,
    }
  },

  // Student Courses
  async getCourses(params?: { page?: number; size?: number }) {
    await randomDelay()
    const page = params?.page || 0
    const size = params?.size || 20
    return {
      content: mockCourses,
      page,
      size,
      totalElements: mockCourses.length,
      totalPages: 1,
    }
  },

  async getCourse(id: number) {
    await randomDelay()
    const course = mockCourseDetails[id]
    if (!course) throw new Error('课程不存在')
    return course
  },

  // My Courses (enrollments)
  async getMyCourses() {
    await randomDelay()
    return {
      content: mockEnrollments,
      page: 0,
      size: 20,
      totalElements: mockEnrollments.length,
      totalPages: 1,
    }
  },

  // Redeem
  async redeemCode(code: string) {
    await randomDelay()
    console.log('[Mock] redeemCode:', code)
    if (!code.trim()) {
      throw new Error('请输入兑换码')
    }
    return mockRedeemResult
  },

  // Teacher Auth
  async teacherSendCode(email: string) {
    await randomDelay()
    console.log('[Mock] teacherSendCode:', email)
    return { message: '验证码已发送' }
  },

  async teacherVerifyCode(email: string, code: string) {
    await randomDelay()
    if (code !== '123456' && code.length !== 6) {
      throw new Error('验证码错误')
    }
    currentUser = mockUsers.find(u => u.email === email && u.role === 'TEACHER') || mockUsers[1]
    currentToken = 'mock-teacher-token-' + Date.now()
    return {
      token: currentToken,
      expiresIn: 86400,
      user: currentUser,
    }
  },

  async getTeacherMe() {
    await randomDelay()
    return currentUser || mockUsers[1]
  },

  // Teacher Courses
  async getTeacherCourses(params?: { page?: number; size?: number }) {
    await randomDelay()
    return {
      content: mockTeacherCourses,
      page: params?.page || 0,
      size: params?.size || 20,
      totalElements: mockTeacherCourses.length,
      totalPages: 1,
    }
  },

  async getTeacherCourse(id: number) {
    await randomDelay()
    const course = mockCourseDetails[id]
    if (!course) throw new Error('课程不存在')
    return course
  },

  async createTeacherCourse(data: any) {
    await randomDelay()
    const newCourse = {
      id: Date.now(),
      ...data,
      status: 'DRAFT',
      chapterCount: 0,
      lessonCount: 0,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    }
    mockTeacherCourses.push(newCourse)
    return newCourse
  },

  async updateTeacherCourse(id: number, data: any) {
    await randomDelay()
    return { id, ...data }
  },

  async deleteTeacherCourse(id: number) {
    await randomDelay()
    return { success: true }
  },

  async publishTeacherCourse(id: number) {
    await randomDelay()
    return { id, status: 'PUBLISHED' }
  },

  // Teacher Chapters
  async createChapter(data: any) {
    await randomDelay()
    return { id: Date.now(), ...data, lessons: [] }
  },

  async updateChapter(id: number, data: any) {
    await randomDelay()
    return { id, ...data }
  },

  async deleteChapter(id: number) {
    await randomDelay()
    return { success: true }
  },

  // Teacher Lessons
  async createLesson(data: any) {
    await randomDelay()
    return { id: Date.now(), ...data, status: 'PROCESSING' }
  },

  async updateLesson(id: number, data: any) {
    await randomDelay()
    return { id, ...data }
  },

  async deleteLesson(id: number) {
    await randomDelay()
    return { success: true }
  },

  // Teacher Video
  async applyUpload(data: { fileName: string; fileSize: number }) {
    await randomDelay()
    return {
      videoId: 'mock-video-' + Date.now(),
      signature: 'mock-signature',
      uploadUrl: 'mock-upload-url',
    }
  },

  async commitUpload(data: { lessonId: number; videoId: string }) {
    await randomDelay()
    return { success: true }
  },

  // Teacher Stats
  async getTeacherStats() {
    await randomDelay()
    return mockTeacherStats
  },

  // Admin Auth
  async adminLogin(email: string, password: string) {
    await randomDelay()
    if (email !== 'admin@esmile.edu' || password !== 'admin123') {
      throw new Error('邮箱或密码错误')
    }
    currentUser = mockUsers[0]
    currentToken = 'mock-admin-token-' + Date.now()
    return {
      token: currentToken,
      expiresIn: 86400,
      user: currentUser,
    }
  },

  async getAdminMe() {
    await randomDelay()
    return currentUser || mockUsers[0]
  },

  // Admin Users
  async getUsers(params?: { page?: number; size?: number; role?: string; status?: string }) {
    await randomDelay()
    let users = [...mockUsers]
    if (params?.role) {
      users = users.filter(u => u.role === params.role)
    }
    if (params?.status) {
      users = users.filter(u => u.status === params.status)
    }
    return {
      content: users,
      page: params?.page || 0,
      size: params?.size || 20,
      totalElements: users.length,
      totalPages: 1,
    }
  },

  async getUser(id: number) {
    await randomDelay()
    const user = mockUsers.find(u => u.id === id)
    if (!user) throw new Error('用户不存在')
    return user
  },

  async updateUserStatus(id: number, status: string) {
    await randomDelay()
    const user = mockUsers.find(u => u.id === id)
    if (user) user.status = status
    return { id, status }
  },

  async approveTeacher(id: number) {
    await randomDelay()
    const user = mockUsers.find(u => u.id === id)
    if (user) user.status = 'ACTIVE'
    return user
  },

  // Admin Courses
  async getAdminCourses(params?: { page?: number; size?: number; status?: string }) {
    await randomDelay()
    let courses = [...mockCourses]
    if (params?.status) {
      courses = courses.filter(c => c.status === params.status)
    }
    return {
      content: courses,
      page: params?.page || 0,
      size: params?.size || 20,
      totalElements: courses.length,
      totalPages: 1,
    }
  },

  async getAdminCourse(id: number) {
    await randomDelay()
    return mockCourses.find(c => c.id === id) || mockCourseDetails[id]
  },

  async updateCourseStatus(id: number, status: string) {
    await randomDelay()
    return { id, status }
  },

  async deleteCourse(id: number) {
    await randomDelay()
    return { success: true }
  },

  // Admin Stats
  async getStats() {
    await randomDelay()
    return mockStats
  },
}

// Export for use in interceptors
export default mockApi
