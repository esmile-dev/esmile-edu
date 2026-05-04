# API 客户端规范

---

## 1. 概述

前端通过 Axios 与后端 REST API 通信，所有 API 遵循统一的响应格式和错误处理规范。

**Base URL**:
| 环境 | URL |
|------|-----|
| 开发环境 | `http://localhost:8080` |
| 测试环境 | `https://api-test.esmile.edu` |
| 生产环境 | `https://api.esmile.edu` |

---

## 2. API 端点总览

### 2.1 端点路径映射

| 端 | Base Path | 认证方式 |
|----|-----------|----------|
| 学生端 | `/api/v1/student` | Bearer Token |
| 教师端 | `/api/v1/teacher` | Bearer Token |
| 管理端 | `/api/v1/admin` | Bearer Token |
| 外部系统 | `/api/v1/redeem-codes` | API Key |

### 2.2 完整端点列表

**学生端 `/api/v1/student`**:
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/auth/send-code` | 发送验证码 |
| POST | `/auth/verify-code` | 验证登录 |
| GET | `/auth/me` | 获取当前用户 |
| GET | `/courses` | 课程列表 |
| GET | `/courses/{id}` | 课程详情 |
| POST | `/codes/redeem` | 兑换课程 |
| GET | `/my-courses` | 我的课程 |

**教师端 `/api/v1/teacher`**:
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/auth/send-code` | 发送验证码 |
| POST | `/auth/verify-code` | 验证登录 |
| GET | `/auth/me` | 获取当前用户 |
| GET | `/courses` | 我的课程列表 |
| POST | `/courses` | 创建课程 |
| GET | `/courses/{id}` | 课程详情 |
| PUT | `/courses/{id}` | 更新课程 |
| POST | `/courses/{id}/publish` | 发布课程 |
| DELETE | `/courses/{id}` | 删除课程 |
| POST | `/chapters` | 创建章节 |
| PUT | `/chapters/{id}` | 更新章节 |
| DELETE | `/chapters/{id}` | 删除章节 |
| POST | `/lessons` | 创建课时 |
| PUT | `/lessons/{id}` | 更新课时 |
| DELETE | `/lessons/{id}` | 删除课时 |
| GET | `/video/apply-upload` | 申请上传 |
| POST | `/video/commit-upload` | 确认上传 |

**管理端 `/api/v1/admin`**:
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/auth/login` | 管理员登录 |
| GET | `/users` | 用户列表 |
| PUT | `/users/{id}/approve` | 审批教师 |
| PUT | `/users/{id}/status` | 更新用户状态 |
| GET | `/courses` | 课程列表 |
| PUT | `/courses/{id}/status` | 修改课程状态 |
| GET | `/stats` | 统计概览 |

**外部系统 `/api/v1/redeem-codes`**:
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/apply` | 申请生成兑换码 |

---

## 3. 类型定义

### 3.1 通用类型

```typescript
// common/types/api.ts

/** 统一 API 响应格式 */
export interface ApiResponse<T> {
  code: number
  message: string
  data: T | null
}

/** 分页响应格式 */
export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

/** 用户角色 */
export type UserRole = 'STUDENT' | 'TEACHER' | 'ADMIN'

/** 用户状态 */
export type UserStatus = 'PENDING_APPROVAL' | 'ACTIVE' | 'DISABLED'

/** 课程状态 */
export type CourseStatus = 'DRAFT' | 'PUBLISHED'

/** 选课状态 */
export type EnrollmentStatus = 'ACTIVE' | 'EXPIRED'

/** 课时状态 */
export type LessonStatus = 'PROCESSING' | 'READY' | 'FAILED'

/** 兑换码状态 */
export type RedeemCodeStatus = 'PENDING' | 'REDEEMED' | 'EXPIRED'
```

### 3.2 用户类型

```typescript
/** 用户信息 */
export interface User {
  id: number
  email: string
  nickname: string
  avatar: string | null
  role: UserRole
  status: UserStatus
}

/** 用户简要信息 */
export interface UserSummary {
  id: number
  nickname: string
  avatar: string | null
}
```

### 3.3 课程类型

```typescript
/** 课程（列表项） */
export interface Course {
  id: number
  title: string
  description: string | null
  coverImage: string | null
  educatorName?: string
  educator?: UserSummary
  status?: CourseStatus
  chapterCount?: number
  lessonCount?: number
  enrollmentStatus?: EnrollmentStatus
  enrollmentExpiresAt?: string | null
  createdAt?: string
  updatedAt?: string
}

/** 章节 */
export interface Chapter {
  id: number
  courseId?: number
  title: string
  orderNum: number
  lessons: Lesson[]
}

/** 课时 */
export interface Lesson {
  id: number
  chapterId?: number
  title: string
  videoId?: string
  videoUrl?: string
  duration?: number
  orderNum: number
  status: LessonStatus
}

/** 课程详情（学生端） */
export interface CourseDetail extends Course {
  educator: UserSummary
  chapters: Chapter[]
  enrollmentStatus: EnrollmentStatus | null
  enrollmentExpiresAt: string | null
}

/** 课程详情（教师端） */
export interface CourseDetailForTeacher extends Course {
  chapters: Chapter[]
  createdAt: string
  updatedAt: string
}

/** 课程创建请求 */
export interface CreateCourseRequest {
  title: string
  description?: string
  coverImage?: string
}

/** 课程更新请求 */
export interface UpdateCourseRequest {
  title?: string
  description?: string
  coverImage?: string
}

/** 章节创建请求 */
export interface CreateChapterRequest {
  courseId: number
  title: string
  orderNum: number
}

/** 章节更新请求 */
export interface UpdateChapterRequest {
  title?: string
  orderNum?: number
}

/** 课时创建请求 */
export interface CreateLessonRequest {
  chapterId: number
  title: string
  orderNum: number
}

/** 课时更新请求 */
export interface UpdateLessonRequest {
  title?: string
  orderNum?: number
}
```

### 3.4 选课类型

```typescript
/** 我的课程项 */
export interface MyCourse {
  id: number
  title: string
  coverImage: string | null
  progress: number
  enrollmentExpiresAt: string | null
}
```

### 3.5 兑换码类型

```typescript
/** 兑换结果 */
export interface RedeemResult {
  success: boolean
  courseId?: number
  courseTitle?: string
  enrollmentExpiresAt?: string
  errorCode?: string
  errorMessage?: string
}

/** 兑换码生成请求（外部系统） */
export interface ApplyRedeemCodeRequest {
  courseId: number
  expiresAt: string
  courseExpiresAt?: string
  quantity: number
}

/** 兑换码生成响应 */
export interface ApplyRedeemCodeResponse {
  codes: string[]
}
```

### 3.6 统计类型

```typescript
/** 统计概览 */
export interface StatsOverview {
  totalUsers: number
  totalStudents: number
  totalTeachers: number
  pendingTeachers: number
  totalCourses: number
  publishedCourses: number
  totalEnrollments: number
  totalRedeemCodes: number
  redeemedCodes: number
}
```

---

## 4. API Client 封装

### 4.1 核心封装

```typescript
// common/utils/apiClient.ts
import axios, { type AxiosInstance, type AxiosError } from 'axios'
import router from '@/common/router'

class ApiClient {
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
    // 请求拦截器：注入 Token
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

    // 响应拦截器：处理 401
    this.client.interceptors.response.use(
      (response) => response,
      async (error: AxiosError) => {
        if (error.response?.status === 401) {
          sessionStorage.removeItem('token')
          router.push('/login')
        }
        return Promise.reject(error)
      }
    )
  }

  async get<T>(url: string, params?: object): Promise<T> {
    const response = await this.client.get<ApiResponse<T>>(url, { params })
    return this.handleResponse(response)
  }

  async post<T>(url: string, data?: object): Promise<T> {
    const response = await this.client.post<ApiResponse<T>>(url, data)
    return this.handleResponse(response)
  }

  async put<T>(url: string, data?: object): Promise<T> {
    const response = await this.client.put<ApiResponse<T>>(url, data)
    return this.handleResponse(response)
  }

  async delete<T>(url: string): Promise<T> {
    const response = await this.client.delete<ApiResponse<T>>(url)
    return this.handleResponse(response)
  }

  private handleResponse<T>(response: axios.AxiosResponse<ApiResponse<T>>): T {
    const { code, message, data } = response.data
    if (code >= 200 && code < 300) {
      return data as T
    }
    throw new ApiError(code, message)
  }
}

export class ApiError extends Error {
  constructor(
    public code: number,
    message: string
  ) {
    super(message)
    this.name = 'ApiError'
  }
}

export const apiClient = new ApiClient()
```

---

## 5. 学生端 API

```typescript
// student/api/studentApi.ts
import { apiClient } from '@/common/utils/apiClient'
import { z } from 'zod'

/** Zod Schema - 课程列表项 */
const CourseSchema = z.object({
  id: z.number(),
  title: z.string(),
  description: z.string().nullable(),
  coverImage: z.string().url().nullable(),
  educatorName: z.string(),
  chapterCount: z.number(),
  lessonCount: z.number(),
})

/** Zod Schema - 课时 */
const LessonSchema = z.object({
  id: z.number(),
  title: z.string(),
  duration: z.number().nullable(),
  orderNum: z.number(),
  status: z.enum(['PROCESSING', 'READY', 'FAILED']),
})

/** Zod Schema - 章节 */
const ChapterSchema = z.object({
  id: z.number(),
  title: z.string(),
  orderNum: z.number(),
  lessons: z.array(LessonSchema),
})

/** Zod Schema - 用户信息 */
const UserSchema = z.object({
  id: z.number(),
  email: z.string(),
  nickname: z.string(),
  avatar: z.string().nullable(),
  role: z.enum(['STUDENT', 'TEACHER', 'ADMIN']),
  status: z.enum(['PENDING_APPROVAL', 'ACTIVE', 'DISABLED']),
})

/** Zod Schema - 登录响应 */
const LoginResponseSchema = z.object({
  token: z.string(),
  expiresIn: z.number(),
  user: UserSchema,
})

/** 学生端 API */
export const studentApi = {
  // ========== 认证 ==========

  /**
   * 发送验证码
   * POST /api/v1/student/auth/send-code
   * @param email - 邮箱地址
   */
  async sendCode(email: string): Promise<void> {
    return apiClient.post('/api/v1/student/auth/send-code', { email })
  },

  /**
   * 验证登录
   * POST /api/v1/student/auth/verify-code
   * @param email - 邮箱地址
   * @param code - 验证码
   * @returns { token: string, expiresIn: number, user: User }
   */
  async verifyCode(email: string, code: string): Promise<z.infer<typeof LoginResponseSchema>> {
    const data = await apiClient.post<z.infer<typeof LoginResponseSchema>>(
      '/api/v1/student/auth/verify-code',
      { email, code }
    )
    return LoginResponseSchema.parse(data)
  },

  /**
   * 获取当前用户
   * GET /api/v1/student/auth/me
   */
  async getMe(): Promise<z.infer<typeof UserSchema>> {
    return apiClient.get<z.infer<typeof UserSchema>>('/api/v1/student/auth/me')
  },

  // ========== 课程 ==========

  /**
   * 课程列表
   * GET /api/v1/student/courses
   * @param params.page - 页码（从0开始）
   * @param params.size - 每页条数
   * @param params.sort - 排序（默认 createdAt,desc）
   */
  async getCourses(params: {
    page?: number
    size?: number
    sort?: string
  } = {}): Promise<PageResponse<z.infer<typeof CourseSchema>>> {
    const response = await apiClient.get<PageResponse<z.infer<typeof CourseSchema>>>(
      '/api/v1/student/courses',
      { params }
    )
    return {
      ...response,
      content: z.array(CourseSchema).parse(response.content),
    }
  },

  /**
   * 课程详情
   * GET /api/v1/student/courses/{id}
   */
  async getCourseDetail(id: number): Promise<CourseDetail> {
    const data = await apiClient.get<CourseDetail>(`/api/v1/student/courses/${id}`)
    return CourseDetailSchema.parse(data)
  },

  // ========== 兑换码 ==========

  /**
   * 兑换课程
   * POST /api/v1/student/codes/redeem
   * @param code - 兑换码
   * @returns { success: boolean, courseId?: number, courseTitle?: string, enrollmentExpiresAt?: string }
   */
  async redeemCode(code: string): Promise<RedeemResult> {
    return apiClient.post<RedeemResult>('/api/v1/student/codes/redeem', { code })
  },

  // ========== 我的课程 ==========

  /**
   * 我的课程列表
   * GET /api/v1/student/my-courses
   * @param params.page - 页码
   * @param params.size - 每页条数
   */
  async getMyCourses(params: {
    page?: number
    size?: number
  } = {}): Promise<PageResponse<MyCourse>> {
    return apiClient.get<PageResponse<MyCourse>>('/api/v1/student/my-courses', { params })
  },
}

/** 课程详情 Schema（用于 Zod 校验） */
const CourseDetailSchema = CourseSchema.extend({
  educator: z.object({
    id: z.number(),
    nickname: z.string(),
    avatar: z.string().nullable(),
  }),
  chapters: z.array(ChapterSchema),
  enrollmentStatus: z.enum(['ACTIVE', 'EXPIRED']).nullable(),
  enrollmentExpiresAt: z.string().nullable(),
})
```

---

## 6. 教师端 API

```typescript
// teacher/api/teacherApi.ts
import { apiClient } from '@/common/utils/apiClient'
import { z } from 'zod'

/** Zod Schema - 课程项（教师端） */
const TeacherCourseSchema = z.object({
  id: z.number(),
  title: z.string(),
  status: z.enum(['DRAFT', 'PUBLISHED']),
  chapterCount: z.number(),
  lessonCount: z.number(),
  createdAt: z.string(),
  updatedAt: z.string(),
})

/** 教师端 API */
export const teacherApi = {
  // ========== 认证 ==========

  /**
   * 发送验证码
   * POST /api/v1/teacher/auth/send-code
   */
  async sendCode(email: string): Promise<void> {
    return apiClient.post('/api/v1/teacher/auth/send-code', { email })
  },

  /**
   * 验证登录
   * POST /api/v1/teacher/auth/verify-code
   */
  async verifyCode(email: string, code: string): Promise<{
    token: string
    expiresIn: number
    user: User
  }> {
    return apiClient.post('/api/v1/teacher/auth/verify-code', { email, code })
  },

  /**
   * 获取当前用户
   * GET /api/v1/teacher/auth/me
   */
  async getMe(): Promise<User> {
    return apiClient.get<User>('/api/v1/teacher/auth/me')
  },

  // ========== 课程管理 ==========

  /**
   * 我的课程列表
   * GET /api/v1/teacher/courses
   */
  async getMyCourses(params: {
    page?: number
    size?: number
  } = {}): Promise<PageResponse<z.infer<typeof TeacherCourseSchema>>> {
    const response = await apiClient.get<PageResponse<z.infer<typeof TeacherCourseSchema>>>(
      '/api/v1/teacher/courses',
      { params }
    )
    return {
      ...response,
      content: z.array(TeacherCourseSchema).parse(response.content),
    }
  },

  /**
   * 创建课程
   * POST /api/v1/teacher/courses
   */
  async createCourse(data: {
    title: string
    description?: string
    coverImage?: string
  }): Promise<{
    id: number
    title: string
    status: string
    createdAt: string
  }> {
    return apiClient.post('/api/v1/teacher/courses', data)
  },

  /**
   * 课程详情
   * GET /api/v1/teacher/courses/{id}
   */
  async getCourseDetail(id: number): Promise<CourseDetailForTeacher> {
    return apiClient.get<CourseDetailForTeacher>(`/api/v1/teacher/courses/${id}`)
  },

  /**
   * 更新课程
   * PUT /api/v1/teacher/courses/{id}
   */
  async updateCourse(id: number, data: {
    title?: string
    description?: string
    coverImage?: string
  }): Promise<{
    id: number
    title: string
    status: string
    updatedAt: string
  }> {
    return apiClient.put(`/api/v1/teacher/courses/${id}`, data)
  },

  /**
   * 发布课程
   * POST /api/v1/teacher/courses/{id}/publish
   */
  async publishCourse(id: number): Promise<{ id: number; status: 'PUBLISHED' }> {
    return apiClient.post(`/api/v1/teacher/courses/${id}/publish`)
  },

  /**
   * 删除课程
   * DELETE /api/v1/teacher/courses/{id}
   */
  async deleteCourse(id: number): Promise<void> {
    return apiClient.delete(`/api/v1/teacher/courses/${id}`)
  },

  // ========== 章节管理 ==========

  /**
   * 创建章节
   * POST /api/v1/teacher/chapters
   */
  async createChapter(data: {
    courseId: number
    title: string
    orderNum: number
  }): Promise<{
    id: number
    courseId: number
    title: string
    orderNum: number
  }> {
    return apiClient.post('/api/v1/teacher/chapters', data)
  },

  /**
   * 更新章节
   * PUT /api/v1/teacher/chapters/{id}
   */
  async updateChapter(id: number, data: {
    title?: string
    orderNum?: number
  }): Promise<{
    id: number
    title: string
    orderNum: number
  }> {
    return apiClient.put(`/api/v1/teacher/chapters/${id}`, data)
  },

  /**
   * 删除章节
   * DELETE /api/v1/teacher/chapters/{id}
   */
  async deleteChapter(id: number): Promise<void> {
    return apiClient.delete(`/api/v1/teacher/chapters/${id}`)
  },

  // ========== 课时管理 ==========

  /**
   * 创建课时
   * POST /api/v1/teacher/lessons
   */
  async createLesson(data: {
    chapterId: number
    title: string
    orderNum: number
  }): Promise<{
    id: number
    chapterId: number
    title: string
    orderNum: number
    status: 'PROCESSING'
  }> {
    return apiClient.post('/api/v1/teacher/lessons', data)
  },

  /**
   * 更新课时
   * PUT /api/v1/teacher/lessons/{id}
   */
  async updateLesson(id: number, data: {
    title?: string
    orderNum?: number
  }): Promise<{
    id: number
    title: string
    orderNum: number
    status: LessonStatus
  }> {
    return apiClient.put(`/api/v1/teacher/lessons/${id}`, data)
  },

  /**
   * 删除课时
   * DELETE /api/v1/teacher/lessons/{id}
   */
  async deleteLesson(id: number): Promise<void> {
    return apiClient.delete(`/api/v1/teacher/lessons/${id}`)
  },

  // ========== 视频上传 ==========

  /**
   * 申请上传
   * GET /api/v1/teacher/video/apply-upload
   */
  async applyUpload(data: {
    fileName: string
    fileSize: number
  }): Promise<{
    videoId: string
    signature: string
    uploadUrl: string
  }> {
    return apiClient.get('/api/v1/teacher/video/apply-upload', { params: data })
  },

  /**
   * 确认上传完成
   * POST /api/v1/teacher/video/commit-upload
   */
  async commitUpload(data: {
    lessonId: number
    videoId: string
  }): Promise<{
    lessonId: number
    videoId: string
    videoUrl: string
    duration: number
    status: 'READY'
  }> {
    return apiClient.post('/api/v1/teacher/video/commit-upload', data)
  },
}
```

---

## 7. 管理端 API

```typescript
// admin/api/adminApi.ts
import { apiClient } from '@/common/utils/apiClient'
import { z } from 'zod'

/** Zod Schema - 用户列表项 */
const AdminUserSchema = z.object({
  id: z.number(),
  email: z.string(),
  nickname: z.string(),
  avatar: z.string().nullable(),
  role: z.enum(['STUDENT', 'TEACHER', 'ADMIN']),
  status: z.enum(['PENDING_APPROVAL', 'ACTIVE', 'DISABLED']),
  createdAt: z.string(),
})

/** 管理端 API */
export const adminApi = {
  // ========== 认证 ==========

  /**
   * 管理员登录
   * POST /api/v1/admin/auth/login
   */
  async login(email: string, password: string): Promise<{
    token: string
    expiresIn: number
  }> {
    return apiClient.post('/api/v1/admin/auth/login', { email, password })
  },

  // ========== 用户管理 ==========

  /**
   * 用户列表
   * GET /api/v1/admin/users
   */
  async getUsers(params: {
    role?: 'STUDENT' | 'TEACHER'
    status?: 'PENDING_APPROVAL' | 'ACTIVE' | 'DISABLED'
    page?: number
    size?: number
  } = {}): Promise<PageResponse<z.infer<typeof AdminUserSchema>>> {
    const response = await apiClient.get<PageResponse<z.infer<typeof AdminUserSchema>>>(
      '/api/v1/admin/users',
      { params }
    )
    return {
      ...response,
      content: z.array(AdminUserSchema).parse(response.content),
    }
  },

  /**
   * 审批教师
   * PUT /api/v1/admin/users/{id}/approve
   */
  async approveTeacher(userId: number): Promise<{
    id: number
    email: string
    nickname: string
    role: string
    status: 'ACTIVE'
    updatedAt: string
  }> {
    return apiClient.put(`/api/v1/admin/users/${userId}/approve`)
  },

  /**
   * 更新用户状态
   * PUT /api/v1/admin/users/{id}/status
   */
  async updateUserStatus(userId: number, status: string, reason?: string): Promise<{
    id: number
    status: string
  }> {
    return apiClient.put(`/api/v1/admin/users/${userId}/status`, { status, reason })
  },

  // ========== 课程管理 ==========

  /**
   * 课程列表
   * GET /api/v1/admin/courses
   */
  async getCourses(params: {
    status?: 'DRAFT' | 'PUBLISHED'
    page?: number
    size?: number
  } = {}): Promise<PageResponse<Course>> {
    return apiClient.get('/api/v1/admin/courses', { params })
  },

  /**
   * 修改课程状态
   * PUT /api/v1/admin/courses/{id}/status
   */
  async updateCourseStatus(courseId: number, status: 'PUBLISHED'): Promise<{
    id: number
    status: 'PUBLISHED'
  }> {
    return apiClient.put(`/api/v1/admin/courses/${courseId}/status`, { status })
  },

  // ========== 统计 ==========

  /**
   * 统计概览
   * GET /api/v1/admin/stats
   */
  async getStats(): Promise<StatsOverview> {
    return apiClient.get<StatsOverview>('/api/v1/admin/stats')
  },
}
```

---

## 8. 外部系统 API

```typescript
// common/api/externalApi.ts
import axios from 'axios'

/**
 * 外部系统 API Client（使用 API Key 认证）
 */
class ExternalApiClient {
  private client: axios.AxiosInstance

  constructor() {
    this.client = axios.create({
      baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
      timeout: 30000,
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
      },
    })
  }

  setApiKey(apiKey: string) {
    this.client.defaults.headers['X-API-Key'] = apiKey
  }

  async post<T>(url: string, data?: object): Promise<T> {
    const response = await this.client.post<ApiResponse<T>>(url, data)
    const { code, message, data: result } = response.data
    if (code >= 200 && code < 300) {
      return result as T
    }
    throw new ApiError(code, message)
  }
}

export const externalApiClient = new ExternalApiClient()

/** 外部系统 API */
export const externalApi = {
  /**
   * 申请生成兑换码
   * POST /api/v1/redeem-codes/apply
   * @param apiKey - API Key
   * @param data - 生成参数
   */
  async applyRedeemCodes(
    apiKey: string,
    data: {
      courseId: number
      expiresAt: string
      courseExpiresAt?: string
      quantity: number
    }
  ): Promise<{ codes: string[] }> {
    externalApiClient.setApiKey(apiKey)
    return externalApiClient.post('/api/v1/redeem-codes/apply', data)
  },
}
```

---

## 9. 错误码

### 9.1 学生端错误码

| 错误码 | 说明 | 用户提示 |
|--------|------|----------|
| 10001 | 无效验证码 | 验证码错误，请重新输入 |
| 10002 | 验证码已过期 | 验证码已过期，请重新获取 |
| 10101 | 用户不存在 | 用户不存在 |
| 10102 | 用户已禁用 | 账号已被禁用，请联系管理员 |
| 10301 | 兑换码不存在 | 兑换码无效 |
| 10302 | 兑换码已过期 | 兑换码已过期 |
| 10303 | 兑换码已被使用 | 兑换码已被使用 |
| 10304 | 兑换码关联课程不可用 | 该课程暂不可兑换 |

### 9.2 教师端错误码

| 错误码 | 说明 | 用户提示 |
|--------|------|----------|
| 10201 | 课程不存在 | 课程不存在 |
| 10202 | 课程未发布 | 课程未发布 |
| 10203 | 不是课程所有者 | 无权限操作此课程 |
| 10204 | 章节不存在 | 章节不存在 |
| 10205 | 课时不存在 | 课时不存在 |
| 10206 | 视频上传失败 | 视频上传失败，请重试 |
| 10207 | 视频处理失败 | 视频处理失败，请重新上传 |

### 9.3 管理端错误码

| 错误码 | 说明 | 用户提示 |
|--------|------|----------|
| 10101 | 用户不存在 | 用户不存在 |
| 10105 | 用户不是教师 | 该用户不是教师 |
| 10106 | 无法修改管理员状态 | 无法修改管理员状态 |
| 10201 | 课程不存在 | 课程不存在 |

### 9.4 外部系统错误码

| 错误码 | 说明 | 用户提示 |
|--------|------|----------|
| 10305 | 课程不存在 | 课程不存在 |
| 10306 | 课程未发布 | 课程未发布 |
| 10308 | 超出最大生成数量 | 每次最多生成100个兑换码 |

### 9.5 统一错误处理

```typescript
// common/composables/useApiError.ts
import { ref } from 'vue'
import { ApiError } from '@/common/utils/apiClient'

const errorMessages: Record<number, string> = {
  // 学生端
  10001: '验证码错误，请重新输入',
  10002: '验证码已过期，请重新获取',
  10101: '用户不存在',
  10102: '账号已被禁用，请联系管理员',
  10301: '兑换码无效',
  10302: '兑换码已过期',
  10303: '兑换码已被使用',
  10304: '该课程暂不可兑换',
  // 教师端
  10201: '课程不存在',
  10202: '课程未发布',
  10203: '无权限操作此课程',
  10204: '章节不存在',
  10205: '课时不存在',
  10206: '视频上传失败，请重试',
  10207: '视频处理失败，请重新上传',
  // 管理端
  10105: '该用户不是教师',
  10106: '无法修改管理员状态',
  // 外部系统
  10305: '课程不存在',
  10306: '课程未发布',
  10308: '每次最多生成100个兑换码',
}

export function useApiError() {
  const error = ref<string | null>(null)

  function handleError(err: unknown) {
    if (err instanceof ApiError) {
      error.value = errorMessages[err.code] || err.message
    } else if (err instanceof Error) {
      error.value = err.message
    } else {
      error.value = '网络错误，请稍后重试'
    }
  }

  function clearError() {
    error.value = null
  }

  return { error, handleError, clearError }
}
```

---

## 10. 请求示例

### 10.1 学生登录流程

```vue
<script setup lang="ts">
import { ref } from 'vue'
import { studentApi } from '@/student/api/studentApi'
import { useAuthStore } from '@/common/stores/auth'
import { useApiError } from '@/common/composables/useApiError'

const email = ref('')
const code = ref('')
const step = ref<'email' | 'code'>('email')
const { error, handleError, clearError } = useApiError()
const authStore = useAuthStore()

async function handleSendCode() {
  clearError()
  try {
    await studentApi.sendCode(email.value)
    step.value = 'code'
  } catch (err) {
    handleError(err)
  }
}

async function handleVerifyCode() {
  clearError()
  try {
    const response = await studentApi.verifyCode(email.value, code.value)
    authStore.setAuth(response.user, response.token)
    // 跳转到首页
  } catch (err) {
    handleError(err)
  }
}
</script>

<template>
  <div>
    <template v-if="step === 'email'">
      <input v-model="email" type="email" placeholder="输入邮箱" />
      <button @click="handleSendCode">发送验证码</button>
    </template>
    <template v-else>
      <input v-model="code" placeholder="输入验证码" />
      <button @click="handleVerifyCode">验证</button>
    </template>
    <p v-if="error" class="text-red-500">{{ error }}</p>
  </div>
</template>
```

### 10.2 教师创建课程

```typescript
// teacher/composables/useCourseCreate.ts
import { ref } from 'vue'
import { teacherApi } from '@/teacher/api/teacherApi'
import { useApiError } from '@/common/composables/useApiError'

export function useCourseCreate() {
  const loading = ref(false)
  const { error, handleError, clearError } = useApiError()

  async function createCourse(data: {
    title: string
    description?: string
    coverImage?: string
  }) {
    clearError()
    loading.value = true
    try {
      const result = await teacherApi.createCourse(data)
      return result
    } catch (err) {
      handleError(err)
      throw err
    } finally {
      loading.value = false
    }
  }

  return { loading, error, createCourse }
}
```
