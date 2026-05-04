// Mock data for frontend demo
export const mockUsers = [
  { id: 1, email: 'admin@esmile.edu', nickname: '管理员', avatar: null, role: 'ADMIN', status: 'ACTIVE' },
  { id: 2, email: 'teacher@esmile.edu', nickname: '李老师', avatar: null, role: 'TEACHER', status: 'ACTIVE' },
  { id: 3, email: 'teacher2@esmile.edu', nickname: '王老师', avatar: null, role: 'TEACHER', status: 'PENDING_APPROVAL' },
  { id: 4, email: 'student@esmile.edu', nickname: '张三', avatar: null, role: 'STUDENT', status: 'ACTIVE' },
  { id: 5, email: 'student2@esmile.edu', nickname: '李四', avatar: null, role: 'STUDENT', status: 'ACTIVE' },
  { id: 6, email: 'student3@esmile.edu', nickname: '王五', avatar: null, role: 'STUDENT', status: 'ACTIVE' },
]

export const mockCourses = [
  {
    id: 1,
    title: 'Vue 3 实战课程',
    description: '从入门到精通，全面掌握 Vue 3 开发技能',
    coverImage: 'https://picsum.photos/seed/vue3/800/450',
    educatorName: '李老师',
    status: 'PUBLISHED',
    chapterCount: 3,
    lessonCount: 12,
    createdAt: '2024-01-15T08:00:00Z',
    updatedAt: '2024-03-20T14:30:00Z',
  },
  {
    id: 2,
    title: 'TypeScript 进阶指南',
    description: '深入理解 TypeScript 类型系统，提升代码质量',
    coverImage: 'https://picsum.photos/seed/ts/800/450',
    educatorName: '李老师',
    status: 'PUBLISHED',
    chapterCount: 4,
    lessonCount: 16,
    createdAt: '2024-02-01T10:00:00Z',
    updatedAt: '2024-03-18T09:15:00Z',
  },
  {
    id: 3,
    title: 'Spring Boot 企业级开发',
    description: '构建高性能企业应用的完整指南',
    coverImage: 'https://picsum.photos/seed/spring/800/450',
    educatorName: '王老师',
    status: 'PUBLISHED',
    chapterCount: 5,
    lessonCount: 20,
    createdAt: '2024-01-20T12:00:00Z',
    updatedAt: '2024-03-15T16:45:00Z',
  },
  {
    id: 4,
    title: 'React 18 新特性探索',
    description: '探索 React 18 的最新特性和最佳实践',
    coverImage: 'https://picsum.photos/seed/react/800/450',
    educatorName: '李老师',
    status: 'DRAFT',
    chapterCount: 2,
    lessonCount: 6,
    createdAt: '2024-03-01T14:00:00Z',
    updatedAt: '2024-03-10T11:20:00Z',
  },
  {
    id: 5,
    title: 'Node.js 后端开发入门',
    description: '使用 Node.js 和 Express 构建 RESTful API',
    coverImage: 'https://picsum.photos/seed/nodejs/800/450',
    educatorName: '王老师',
    status: 'PUBLISHED',
    chapterCount: 4,
    lessonCount: 14,
    createdAt: '2024-02-15T09:30:00Z',
    updatedAt: '2024-03-22T10:00:00Z',
  },
  {
    id: 6,
    title: 'Python 数据分析实战',
    description: '使用 Pandas 和 NumPy 进行数据分析',
    coverImage: 'https://picsum.photos/seed/python/800/450',
    educatorName: '李老师',
    status: 'PUBLISHED',
    chapterCount: 6,
    lessonCount: 24,
    createdAt: '2024-01-10T07:00:00Z',
    updatedAt: '2024-03-19T08:30:00Z',
  },
]

export const mockCourseDetails: Record<number, any> = {
  1: {
    ...mockCourses[0],
    educator: { id: 2, nickname: '李老师', avatar: null },
    chapters: [
      {
        id: 1,
        courseId: 1,
        title: '第一章：Vue 3 基础',
        orderNum: 1,
        lessons: [
          { id: 1, chapterId: 1, title: '1.1 Vue 3 简介', videoId: 'v1', videoUrl: null, duration: 720, orderNum: 1, status: 'READY' },
          { id: 2, chapterId: 1, title: '1.2 组合式 API 入门', videoId: 'v2', videoUrl: null, duration: 1080, orderNum: 2, status: 'READY' },
          { id: 3, chapterId: 1, title: '1.3 响应式系统原理', videoId: 'v3', videoUrl: null, duration: 900, orderNum: 3, status: 'READY' },
          { id: 4, chapterId: 1, title: '1.4 实战：计数器组件', videoId: 'v4', videoUrl: null, duration: 1200, orderNum: 4, status: 'READY' },
        ],
      },
      {
        id: 2,
        courseId: 1,
        title: '第二章：组件开发',
        orderNum: 2,
        lessons: [
          { id: 5, chapterId: 2, title: '2.1 组件生命周期', videoId: 'v5', videoUrl: null, duration: 840, orderNum: 1, status: 'READY' },
          { id: 6, chapterId: 2, title: '2.2 插槽与模板', videoId: 'v6', videoUrl: null, duration: 960, orderNum: 2, status: 'READY' },
          { id: 7, chapterId: 2, title: '2.3 依赖注入', videoId: 'v7', videoUrl: null, duration: 780, orderNum: 3, status: 'READY' },
          { id: 8, chapterId: 2, title: '2.4 实战：表单组件', videoId: null, videoUrl: null, duration: 0, orderNum: 4, status: 'PROCESSING' },
        ],
      },
      {
        id: 3,
        courseId: 1,
        title: '第三章：路由与状态管理',
        orderNum: 3,
        lessons: [
          { id: 9, chapterId: 3, title: '3.1 Vue Router 4', videoId: 'v9', videoUrl: null, duration: 1080, orderNum: 1, status: 'READY' },
          { id: 10, chapterId: 3, title: '3.2 Pinia 状态管理', videoId: 'v10', videoUrl: null, duration: 1200, orderNum: 2, status: 'READY' },
          { id: 11, chapterId: 3, title: '3.3 路由守卫', videoId: 'v11', videoUrl: null, duration: 900, orderNum: 3, status: 'READY' },
          { id: 12, chapterId: 3, title: '3.4 实战：后台管理系统', videoId: null, videoUrl: null, duration: 0, orderNum: 4, status: 'PROCESSING' },
        ],
      },
    ],
    enrollmentStatus: 'ACTIVE',
    enrollmentExpiresAt: '2025-12-31T23:59:59Z',
  },
  2: {
    ...mockCourses[1],
    educator: { id: 2, nickname: '李老师', avatar: null },
    chapters: [
      {
        id: 4,
        courseId: 2,
        title: '第一章：类型基础',
        orderNum: 1,
        lessons: [
          { id: 13, chapterId: 4, title: '1.1 TypeScript 简介', videoId: 'v13', videoUrl: null, duration: 660, orderNum: 1, status: 'READY' },
          { id: 14, chapterId: 4, title: '1.2 基本类型', videoId: 'v14', videoUrl: null, duration: 840, orderNum: 2, status: 'READY' },
          { id: 15, chapterId: 4, title: '1.3 枚举与联合类型', videoId: 'v15', videoUrl: null, duration: 720, orderNum: 3, status: 'READY' },
        ],
      },
      {
        id: 5,
        courseId: 2,
        title: '第二章：高级类型',
        orderNum: 2,
        lessons: [
          { id: 16, chapterId: 5, title: '2.1 泛型入门', videoId: 'v16', videoUrl: null, duration: 900, orderNum: 1, status: 'READY' },
          { id: 17, chapterId: 5, title: '2.2 条件类型', videoId: 'v17', videoUrl: null, duration: 1080, orderNum: 2, status: 'READY' },
          { id: 18, chapterId: 5, title: '2.3 映射类型', videoId: 'v18', videoUrl: null, duration: 840, orderNum: 3, status: 'READY' },
          { id: 19, chapterId: 5, title: '2.4 模板字面量类型', videoId: 'v19', videoUrl: null, duration: 780, orderNum: 4, status: 'READY' },
        ],
      },
    ],
    enrollmentStatus: null,
    enrollmentExpiresAt: null,
  },
}

export const mockStats = {
  totalUsers: 1250,
  totalStudents: 980,
  totalTeachers: 45,
  pendingTeachers: 8,
  totalCourses: 86,
  publishedCourses: 72,
  totalEnrollments: 3420,
  totalRedeemCodes: 5000,
  redeemedCodes: 3850,
}

export const mockEnrollments = [
  { id: 1, courseId: 1, courseTitle: 'Vue 3 实战课程', coverImage: 'https://picsum.photos/seed/vue3/800/450', progress: 75, enrollmentExpiresAt: '2025-12-31T23:59:59Z', currentLessonId: 9 },
  { id: 2, courseId: 6, courseTitle: 'Python 数据分析实战', coverImage: 'https://picsum.photos/seed/python/800/450', progress: 30, enrollmentExpiresAt: '2025-06-30T23:59:59Z', currentLessonId: 13 },
]

export const mockRedeemResult = {
  success: true,
  courseId: 3,
  courseTitle: 'Spring Boot 企业级开发',
  enrollmentExpiresAt: '2025-12-31T23:59:59Z',
}

export const mockTeacherCourses = mockCourses.slice(0, 4).map(c => ({
  ...c,
  educatorName: '李老师',
  lessonCount: c.lessonCount,
}))

export const mockTeacherStats = {
  totalStudents: 320,
  activeEnrollments: 245,
  totalCourses: 4,
  publishedCourses: 3,
  totalLessons: 58,
  avgProgress: 68,
}
