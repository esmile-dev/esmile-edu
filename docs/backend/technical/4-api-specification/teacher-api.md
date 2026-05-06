# API 规范 - 教师端

**Base Path**: `/api/v1/teacher`

---

## 1. 认证接口

### 1.1 发送验证码

```
POST /api/v1/teacher/auth/send-code
```

**Request Body**:
```json
{
  "email": "teacher@example.com"
}
```

**Response** (200):
```json
{
  "code": 200,
  "message": "Success",
  "data": null
}
```

### 1.2 验证登录

```
POST /api/v1/teacher/auth/verify-code
```

**Request Body**:
```json
{
  "email": "teacher@example.com",
  "code": "123456"
}
```

**Response** (200):
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "expiresIn": 86400,
    "user": {
      "id": 2,
      "email": "teacher@example.com",
      "nickname": "Teacher Zhang",
      "avatar": null,
      "role": "TEACHER",
      "status": "ACTIVE"
    }
  }
}
```

### 1.3 获取当前用户

```
GET /api/v1/teacher/auth/me
Authorization: Bearer <token>
```

**Response** (200):
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "id": 2,
    "email": "teacher@example.com",
    "nickname": "Teacher Zhang",
    "avatar": null,
    "role": "TEACHER",
    "status": "ACTIVE"
  }
}
```

---

## 2. 课程管理接口

### 2.1 我的课程列表

```
GET /api/v1/teacher/courses
Authorization: Bearer <token>
```

**Query Parameters**:
| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| page | int | 0 | 页码 |
| size | int | 20 | 每页条数 |

**Response** (200):
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "content": [
      {
        "id": 1,
        "title": "Java 入门教程",
        "status": "DRAFT",
        "chapterCount": 5,
        "lessonCount": 30,
        "createdAt": "2026-05-01T10:00:00Z",
        "updatedAt": "2026-05-03T10:00:00Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 10,
    "totalPages": 1
  }
}
```

### 2.2 创建课程

```
POST /api/v1/teacher/courses
Authorization: Bearer <token>
```

**Request Body**:
```json
{
  "title": "Java 入门教程",
  "description": "适合零基础学员",
  "coverImage": "https://example.com/cover.jpg"
}
```

**Response** (201):
```json
{
  "code": 201,
  "message": "Created",
  "data": {
    "id": 1,
    "title": "Java 入门教程",
    "status": "DRAFT",
    "createdAt": "2026-05-03T10:00:00Z"
  }
}
```

### 2.3 课程详情

```
GET /api/v1/teacher/courses/{id}
Authorization: Bearer <token>
```

**Response** (200):
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "id": 1,
    "title": "Java 入门教程",
    "description": "适合零基础学员",
    "coverImage": "https://example.com/cover.jpg",
    "status": "DRAFT",
    "chapters": [...],
    "createdAt": "2026-05-01T10:00:00Z",
    "updatedAt": "2026-05-03T10:00:00Z"
  }
}
```

### 2.4 更新课程

```
PUT /api/v1/teacher/courses/{id}
Authorization: Bearer <token>
```

**Request Body**:
```json
{
  "title": "Java 入门教程 (第二版)",
  "description": "更新内容...",
  "coverImage": "https://example.com/cover-v2.jpg"
}
```

**Response** (200):
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "id": 1,
    "title": "Java 入门教程 (第二版)",
    "status": "DRAFT",
    "updatedAt": "2026-05-03T11:00:00Z"
  }
}
```

### 2.5 发布课程

```
POST /api/v1/teacher/courses/{id}/publish
Authorization: Bearer <token>
```

**Response** (200):
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "id": 1,
    "status": "PUBLISHED"
  }
}
```

### 2.6 删除课程

```
DELETE /api/v1/teacher/courses/{id}
Authorization: Bearer <token>
```

**Response** (204):
```
No Content
```

---

## 3. 章节管理接口

### 3.1 创建章节

```
POST /api/v1/teacher/chapters
Authorization: Bearer <token>
```

**Request Body**:
```json
{
  "courseId": 1,
  "title": "第一章 Java 基础",
  "orderNum": 1
}
```

**Response** (201):
```json
{
  "code": 201,
  "message": "Created",
  "data": {
    "id": 1,
    "courseId": 1,
    "title": "第一章 Java 基础",
    "orderNum": 1
  }
}
```

### 3.2 更新章节

```
PUT /api/v1/teacher/chapters/{id}
Authorization: Bearer <token>
```

**Request Body**:
```json
{
  "title": "第一章 Java 基础 (修订版)",
  "orderNum": 1
}
```

**Response** (200):
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "id": 1,
    "title": "第一章 Java 基础 (修订版)",
    "orderNum": 1
  }
}
```

### 3.3 删除章节

```
DELETE /api/v1/teacher/chapters/{id}
Authorization: Bearer <token>
```

**Response** (204):
```
No Content
```

---

## 4. 课时管理接口

### 4.1 创建课时

```
POST /api/v1/teacher/lessons
Authorization: Bearer <token>
```

**Request Body**:
```json
{
  "chapterId": 1,
  "title": "1.1 变量与数据类型",
  "orderNum": 1
}
```

**Response** (201):
```json
{
  "code": 201,
  "message": "Created",
  "data": {
    "id": 1,
    "chapterId": 1,
    "title": "1.1 变量与数据类型",
    "orderNum": 1,
    "status": "PROCESSING"
  }
}
```

### 4.2 更新课时

```
PUT /api/v1/teacher/lessons/{id}
Authorization: Bearer <token>
```

**Request Body**:
```json
{
  "title": "1.1 变量与数据类型 (更新版)",
  "orderNum": 2
}
```

**Response** (200):
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "id": 1,
    "title": "1.1 变量与数据类型 (更新版)",
    "orderNum": 2,
    "status": "READY"
  }
}
```

### 4.3 删除课时

```
DELETE /api/v1/teacher/lessons/{id}
Authorization: Bearer <token>
```

**Response** (204):
```
No Content
```

---

## 5. 视频上传接口

### 5.1 申请上传

```
GET /api/v1/teacher/video/apply-upload
Authorization: Bearer <token>
```

**Query Parameters**:
| 参数 | 类型 | 说明 |
|------|------|------|
| fileName | String | 文件名（含扩展名） |
| fileSize | long | 文件大小（字节） |

**Response** (200):
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "videoId": "3877023076522682411",
    "signature": "...",
    "uploadUrl": "https://upload.vod2.myqcloud.com/..."
  }
}
```

### 5.2 确认上传完成

```
POST /api/v1/teacher/video/commit-upload
Authorization: Bearer <token>
```

**Request Body**:
```json
{
  "lessonId": 1,
  "videoId": "3877023076522682411"
}
```

**Response** (200):
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "lessonId": 1,
    "videoId": "3877023076522682411",
    "videoUrl": "https://vod.example.com/...",
    "duration": 600,
    "status": "READY"
  }
}
```

---

## 6. 错误码

| 错误码 | 说明 |
|--------|------|
| 10201 | 课程不存在 |
| 10202 | 课程未发布 |
| 10203 | 不是课程所有者 |
| 10204 | 章节不存在 |
| 10205 | 课时不存在 |
| 10206 | 视频上传失败 |
| 10207 | 视频处理失败 |
