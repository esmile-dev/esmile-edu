# 课程模块设计规范

**模块**: esmile-edu-course
**依赖**: esmile-edu-common, esmile-edu-user

---

## 1. Domain 层

### 1.1 枚举定义

#### CourseStatus（课程状态枚举）

| 枚举值 | 说明 |
|--------|------|
| `DRAFT` | 草稿 |
| `PUBLISHED` | 已发布 |

#### LessonStatus（课时状态枚举）

| 枚举值 | 说明 |
|--------|------|
| `PROCESSING` | 处理中（视频转码中） |
| `READY` | 就绪（可播放） |
| `FAILED` | 失败（转码失败） |

#### EnrollmentStatus（选课状态枚举）

| 枚举值 | 说明 |
|--------|------|
| `ACTIVE` | 有效 |
| `EXPIRED` | 已过期 |

### 1.2 值对象定义

#### Title（标题值对象）

| 字段 | 类型 | 校验规则 |
|------|------|----------|
| value | String | 非空、1-200 字符 |

#### Description（描述值对象）

| 字段 | 类型 | 校验规则 |
|------|------|----------|
| value | String | 可为空 |

#### CoverImageUrl（封面URL值对象）

| 字段 | 类型 | 校验规则 |
|------|------|----------|
| value | String | 可为空、若非空则必须以 `http` 开头 |

#### VideoId（腾讯云VOD VideoId值对象）

| 字段 | 类型 | 校验规则 |
|------|------|----------|
| value | String | 非空 |

#### VideoUrl（视频播放URL值对象）

| 字段 | 类型 | 校验规则 |
|------|------|----------|
| value | String | 非空 |

#### Duration（视频时长值对象）

| 字段 | 类型 | 校验规则 |
|------|------|----------|
| seconds | Integer | 非负整数 |

### 1.3 聚合根定义

#### Course（课程聚合根）

**字段列表**:

| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | Long | 主键 | 自动生成 |
| educatorId | Long | 教师ID | 非空，引用User聚合 |
| title | Title | 课程标题 | 非空 |
| description | Description | 课程描述 | 可为空 |
| coverImage | CoverImageUrl | 封面图 | 可为空 |
| status | CourseStatus | 课程状态 | 非空 |
| chapters | List\<Chapter> | 章节列表 | 聚合内实体 |
| createdAt | LocalDateTime | 创建时间 | 自动设置 |
| updatedAt | LocalDateTime | 更新时间 | 自动设置 |

**聚合内实体: Chapter（章节）**:

| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | Long | 主键 | 自动生成 |
| title | Title | 章节标题 | 非空 |
| orderNum | Integer | 排序号 | 非空、正整数 |
| lessons | List\<Lesson> | 课时列表 | 聚合内实体 |

**聚合内实体: Lesson（课时）**:

| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | Long | 主键 | 自动生成 |
| title | Title | 课时标题 | 非空 |
| videoId | VideoId | 腾讯云VOD FileId | 可为空 |
| videoUrl | VideoUrl | 视频播放地址 | 可为空 |
| duration | Duration | 视频时长（秒） | 可为空 |
| orderNum | Integer | 排序号 | 非空、正整数 |
| status | LessonStatus | 课时状态 | 非空 |

#### Enrollment（选课聚合根）

**字段列表**:

| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | Long | 主键 | 自动生成 |
| userId | Long | 用户ID | 非空，引用User聚合 |
| courseId | Long | 课程ID | 非空，引用Course聚合 |
| status | EnrollmentStatus | 选课状态 | 非空 |
| expiresAt | LocalDateTime | 权限到期时间 | 可为空（null表示永久） |
| createdAt | LocalDateTime | 创建时间 | 自动设置 |
| updatedAt | LocalDateTime | 更新时间 | 自动设置 |

**唯一约束**: `(userId, courseId)` 组合唯一

### 1.4 仓储接口

#### CourseRepository

| 方法签名 | 说明 |
|----------|------|
| `Optional<Course> findById(Long id)` | 根据ID查询 |
| `Optional<Course> findByIdAndEducatorId(Long id, Long educatorId)` | 根据ID和教师ID查询 |
| `Page<Course> findByEducatorId(Long educatorId, Pageable pageable)` | 查询教师的课程 |
| `Page<Course> findPublished(Pageable pageable)` | 查询已发布课程 |
| `Course save(Course course)` | 保存课程 |
| `void delete(Course course)` | 删除课程 |

#### EnrollmentRepository

| 方法签名 | 说明 |
|----------|------|
| `Optional<Enrollment> findById(Long id)` | 根据ID查询 |
| `Optional<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId)` | 查询用户的课程选课 |
| `List<Enrollment> findByStudentId(Long studentId)` | 查询用户的所有选课 |
| `Page<Enrollment> findByStudentId(Long studentId, Pageable pageable)` | 分页查询用户选课 |
| `Enrollment save(Enrollment enrollment)` | 保存选课 |
| `boolean existsByStudentIdAndCourseId(Long studentId, Long courseId)` | 检查是否已选课 |

### 1.5 业务规则

| 规则ID | 规则描述 |
|--------|----------|
| CRS-001 | 课程创建者默认为授课教师，且创建后不可变更 |
| CRS-002 | 课程状态 `DRAFT` 时仅教师可见，`PUBLISHED` 后对学生可见 |
| CRS-003 | 课程发布后，章节和课时不应删除，只能标记为隐藏 |
| CRS-004 | 视频上传采用腾讯云 VOD 直传，应用服务器仅存储 videoId |
| CRS-005 | 视频转码完成前，课时状态为 `PROCESSING` |
| CRS-006 | 学生只能观看 `ACTIVE` 状态的 Enrollment 关联的课程 |
| CRS-007 | Enrollment 的 `expiresAt` 为空表示永久有效 |
| CRS-008 | 同一学生不能重复购买同一课程（唯一约束） |

---

## 2. Application 层

### 2.1 Driving Ports（用例接口）

#### CourseManagementUseCase

| 方法签名 | 说明 |
|----------|------|
| `CourseResponse createCourse(Long educatorId, CreateCourseCommand command)` | 创建课程 |
| `CourseResponse updateCourse(Long courseId, Long educatorId, UpdateCourseCommand command)` | 更新课程 |
| `CourseResponse publishCourse(Long courseId, Long educatorId)` | 发布课程 |
| `void deleteCourse(Long courseId, Long educatorId)` | 删除课程 |
| `ChapterResponse addChapter(Long courseId, Long educatorId, CreateChapterCommand command)` | 添加章节 |
| `ChapterResponse updateChapter(Long chapterId, Long educatorId, UpdateChapterCommand command)` | 更新章节 |
| `void deleteChapter(Long chapterId, Long educatorId)` | 删除章节 |
| `LessonResponse addLesson(Long courseId, Long chapterId, Long educatorId, CreateLessonCommand command)` | 添加课时 |
| `LessonResponse updateLesson(Long lessonId, Long educatorId, UpdateLessonCommand command)` | 更新课时 |
| `void deleteLesson(Long lessonId, Long educatorId)` | 删除课时 |

#### VideoUploadUseCase

| 方法签名 | 说明 |
|----------|------|
| `VideoUploadSignature applyUpload(Long educatorId, String fileName, long fileSize)` | 申请上传 |
| `LessonResponse confirmUpload(Long lessonId, Long educatorId, String videoId)` | 确认上传完成 |

#### EnrollmentUseCase

| 方法签名 | 说明 |
|----------|------|
| `EnrollmentResponse enrollStudent(Long studentId, Long courseId, LocalDateTime expiresAt)` | 选课 |
| `boolean hasEnrollment(Long studentId, Long courseId)` | 检查是否已选课 |
| `Page<CourseSummary> getEnrolledCourses(Long studentId, Pageable pageable)` | 获取已选课程 |

### 2.2 Command DTO 定义

#### CreateCourseCommand

| 字段 | 类型 | 校验 |
|------|------|------|
| title | String | @NotBlank, @Size(max=200) |
| description | String | @Size(max=5000) |
| coverImage | String | @Size(max=500) |

#### UpdateCourseCommand

| 字段 | 类型 | 校验 |
|------|------|------|
| title | String | @NotBlank, @Size(max=200) |
| description | String | @Size(max=5000) |
| coverImage | String | @Size(max=500) |

#### CreateChapterCommand

| 字段 | 类型 | 校验 |
|------|------|------|
| courseId | Long | @NotNull |
| title | String | @NotBlank, @Size(max=200) |
| orderNum | Integer | @NotNull, @Positive |

#### UpdateChapterCommand

| 字段 | 类型 | 校验 |
|------|------|------|
| title | String | @NotBlank, @Size(max=200) |
| orderNum | Integer | @NotNull, @Positive |

#### CreateLessonCommand

| 字段 | 类型 | 校验 |
|------|------|------|
| chapterId | Long | @NotNull |
| title | String | @NotBlank, @Size(max=200) |
| orderNum | Integer | @NotNull, @Positive |

#### UpdateLessonCommand

| 字段 | 类型 | 校验 |
|------|------|------|
| title | String | @NotBlank, @Size(max=200) |
| orderNum | Integer | @NotNull, @Positive |

### 2.3 Response DTO 定义

#### CourseResponse

| 字段 | 类型 |
|------|------|
| id | Long |
| educatorId | Long |
| title | String |
| description | String |
| coverImage | String |
| status | CourseStatus |
| chapters | List\<ChapterResponse> |
| createdAt | LocalDateTime |
| updatedAt | LocalDateTime |

#### ChapterResponse

| 字段 | 类型 |
|------|------|
| id | Long |
| courseId | Long |
| title | String |
| orderNum | Integer |
| lessons | List\<LessonResponse> |

#### LessonResponse

| 字段 | 类型 |
|------|------|
| id | Long |
| chapterId | Long |
| title | String |
| videoId | String |
| videoUrl | String |
| duration | Integer |
| orderNum | Integer |
| status | LessonStatus |

#### CourseSummary

| 字段 | 类型 |
|------|------|
| id | Long |
| title | String |
| description | String |
| coverImage | String |
| educatorName | String |
| chapterCount | Integer |
| lessonCount | Integer |

#### EnrollmentResponse

| 字段 | 类型 |
|------|------|
| id | Long |
| userId | Long |
| courseId | Long |
| courseTitle | String |
| status | EnrollmentStatus |
| expiresAt | LocalDateTime |
| createdAt | LocalDateTime |

#### VideoUploadSignature

| 字段 | 类型 | 说明 |
|------|------|------|
| videoId | String | 腾讯云 VOD FileId |
| signature | String | 上传签名 |
| uploadUrl | String | 上传地址 |

### 2.4 业务规则约束

| 约束 | 说明 |
|------|------|
| 教师所有权 | 只有课程创建教师可以管理课程/章节/课时 |
| 课程发布前 | 必须至少有一个章节 |
| 视频上传 | 必须先申请上传签名，再确认上传完成 |
| 选课冲突 | 同一学生同一课程不能重复选课 |

---

## 3. Infrastructure 层

### 3.1 实现约束

| 约束 | 说明 |
|------|------|
| JPA 实现 | CourseRepository、EnrollmentRepository 必须使用 JPA 实现 |
| 聚合内实体 | Chapter 和 Lesson 使用 `@Embeddable` 或 `@OneToMany` |
| 持久化级联 | Course 删除时级联删除 Chapter 和 Lesson |

### 3.2 外部服务集成

#### 腾讯云 VOD 集成要点

- 使用腾讯云 VOD SDK 获取上传签名
- 视频转码完成回调更新 Lesson 状态
- 播放地址通过腾讯云 VOD API 获取

#### 防腐层（ACL）设计

```
TencentVodAcl
├── applyUpload()  → 调用腾讯云 API 获取上传签名
├── confirmUpload() → 查询视频信息，更新 Lesson
└── getPlayUrl()   → 获取视频播放地址
```

---

## 4. API 层

### 4.1 Controller 端点列表

#### TeacherCourseController

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/teacher/courses` | 我的课程列表 |
| POST | `/api/v1/teacher/courses` | 创建课程 |
| GET | `/api/v1/teacher/courses/{id}` | 课程详情 |
| PUT | `/api/v1/teacher/courses/{id}` | 更新课程 |
| DELETE | `/api/v1/teacher/courses/{id}` | 删除课程 |
| POST | `/api/v1/teacher/courses/{id}/publish` | 发布课程 |

#### TeacherChapterController

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/teacher/chapters` | 创建章节 |
| PUT | `/api/v1/teacher/chapters/{id}` | 更新章节 |
| DELETE | `/api/v1/teacher/chapters/{id}` | 删除章节 |

#### TeacherLessonController

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/teacher/lessons` | 创建课时 |
| PUT | `/api/v1/teacher/lessons/{id}` | 更新课时 |
| DELETE | `/api/v1/teacher/lessons/{id}` | 删除课时 |

#### TeacherVideoController

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/teacher/video/apply-upload` | 申请视频上传 |
| POST | `/api/v1/teacher/video/commit-upload` | 确认视频上传完成 |

#### StudentCourseController

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/student/courses` | 课程列表（已发布） |
| GET | `/api/v1/student/courses/{id}` | 课程详情 |
| GET | `/api/v1/student/my-courses` | 我的课程（已选课） |

### 4.2 请求/响应格式说明

#### 创建课程

- **Request Body**: `CreateCourseCommand`
- **Response**: `ApiResponse<CourseResponse>` (201)

#### 申请视频上传

- **Query Params**: `fileName`, `fileSize`
- **Response**: `ApiResponse<VideoUploadSignature>`

#### 确认视频上传

- **Request Body**: `{ lessonId, videoId }`
- **Response**: `ApiResponse<LessonResponse>`

---

## 5. 错误码

| 错误码 | 含义 |
|--------|------|
| 10201 | 课程不存在 |
| 10202 | 课程未发布 |
| 10203 | 不是课程所有者 |
| 10204 | 章节不存在 |
| 10205 | 课时不存在 |
| 10206 | 视频上传失败 |
| 10207 | 视频处理失败 |
| 10301 | 已选过该课程 |

---

## 6. 验证清单

### Domain 层验证

- [ ] Course 聚合根字段完整
- [ ] Chapter 和 Lesson 作为聚合内实体正确嵌入
- [ ] Enrollment 使用 ID 引用而非对象引用
- [ ] CourseRepository 方法签名正确

### Application 层验证

- [ ] CourseManagementService 处理课程CRUD
- [ ] VideoUploadUseCase 处理视频上传流程
- [ ] EnrollmentUseCase 处理选课逻辑

### Infrastructure 层验证

- [ ] JPA Repository 实现 Repository 接口
- [ ] 腾讯云 VOD ACL 正确封装

### API 层验证

- [ ] Controller 路径与 API 规范一致
- [ ] 教师只能操作自己的课程
- [ ] 学生只能观看已选课的课程
