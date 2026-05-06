// ============== Common Types ==============

export interface ApiResponse<T> {
  code: number
  message: string
  data: T | null
}

export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export type UserRole = 'STUDENT' | 'TEACHER' | 'ADMIN'
export type UserStatus = 'PENDING_APPROVAL' | 'ACTIVE' | 'DISABLED'
export type CourseStatus = 'DRAFT' | 'PUBLISHED'
export type EnrollmentStatus = 'ACTIVE' | 'EXPIRED'
export type LessonStatus = 'PROCESSING' | 'READY' | 'FAILED'
export type RedeemCodeStatus = 'PENDING' | 'REDEEMED' | 'EXPIRED'

// ============== User Types ==============

export interface User {
  id: number
  email: string
  nickname: string
  avatar: string | null
  role: UserRole
  status: UserStatus
}

export interface UserSummary {
  id: number
  nickname: string
  avatar: string | null
}

// ============== Course Types ==============

export interface Course {
  id: number
  title: string
  description: string | null
  cover: string | null
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

export interface Chapter {
  id: number
  courseId?: number
  title: string
  orderNum: number
  lessons: Lesson[]
}

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

export interface CourseDetail extends Course {
  educator: UserSummary
  chapters: Chapter[]
  enrollmentStatus: EnrollmentStatus | null
  enrollmentExpiresAt: string | null
}

export interface CourseDetailForTeacher extends Course {
  chapters: Chapter[]
  createdAt: string
  updatedAt: string
}

export interface CreateCourseRequest {
  title: string
  description?: string
  cover?: string
}

export interface UpdateCourseRequest {
  title?: string
  description?: string
  cover?: string
}

export interface CreateChapterRequest {
  courseId: number
  title: string
  position: number
}

export interface UpdateChapterRequest {
  title?: string
  orderNum?: number
}

export interface CreateLessonRequest {
  chapterId: number
  title: string
  orderNum: number
}

export interface UpdateLessonRequest {
  title?: string
  orderNum?: number
}

// ============== Enrollment Types ==============

export interface MyCourse {
  id: number
  title: string
  cover: string | null
  progress: number
  enrollmentExpiresAt: string | null
  currentLessonId?: number
}

// ============== Redeem Types ==============

export interface RedeemResult {
  success: boolean
  courseId?: number
  courseTitle?: string
  enrollmentExpiresAt?: string
  errorCode?: string
  errorMessage?: string
}

export interface ApplyRedeemCodeRequest {
  courseId: number
  expiresAt: string
  courseExpiresAt?: string
  quantity: number
}

export interface ApplyRedeemCodeResponse {
  codes: string[]
}

// ============== Stats Types ==============

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

// ============== Auth Types ==============

export interface LoginResponse {
  token: string
  expiresIn: number
  user: User
}

export interface VerifyCodeRequest {
  email: string
  code: string
}

// ============== Video Types ==============

export interface VideoUploadSignature {
  videoId: string
  signature: string
  uploadUrl: string
}
