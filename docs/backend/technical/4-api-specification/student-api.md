# API 规范 - 学生端

**Base Path**: `/api/v1/student`

---

## 1. 认证接口

### 1.1 发送验证码

```
POST /api/v1/student/auth/send-code
```

**Request Body**:
```json
{
  "email": "student@example.com"
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
POST /api/v1/student/auth/verify-code
```

**Request Body**:
```json
{
  "email": "student@example.com",
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
      "id": 1,
      "email": "student@example.com",
      "nickname": "Student",
      "avatar": null,
      "role": "STUDENT",
      "status": "ACTIVE"
    }
  }
}
```

### 1.3 获取当前用户

```
GET /api/v1/student/auth/me
Authorization: Bearer <token>
```

**Response** (200):
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "id": 1,
    "email": "student@example.com",
    "nickname": "Student",
    "avatar": null,
    "role": "STUDENT",
    "status": "ACTIVE"
  }
}
```

---

## 2. 课程接口

### 2.1 课程列表

```
GET /api/v1/student/courses
Authorization: Bearer <token>
```

**Query Parameters**:
| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| page | int | 0 | 页码 |
| size | int | 20 | 每页条数 |
| sort | String | createdAt,desc | 排序 |

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
        "description": "适合零基础学员",
        "coverImage": "https://example.com/cover.jpg",
        "educatorName": "Teacher Zhang",
        "chapterCount": 5,
        "lessonCount": 30
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 50,
    "totalPages": 3
  }
}
```

### 2.2 课程详情

```
GET /api/v1/student/courses/{id}
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
    "educator": {
      "id": 2,
      "nickname": "Teacher Zhang",
      "avatar": "https://example.com/avatar.jpg"
    },
    "chapters": [
      {
        "id": 1,
        "title": "第一章 Java 基础",
        "orderNum": 1,
        "lessons": [
          {
            "id": 1,
            "title": "1.1 变量与数据类型",
            "duration": 600,
            "orderNum": 1,
            "status": "READY"
          }
        ]
      }
    ],
    "enrollmentStatus": "ACTIVE",
    "enrollmentExpiresAt": "2027-01-01T00:00:00Z"
  }
}
```

**说明**:
- `enrollmentStatus`: 仅当学生已购买时返回
- `enrollmentExpiresAt`: 选课到期时间，null 表示永久有效

---

## 3. 兑换接口

### 3.1 兑换课程

```
POST /api/v1/student/codes/redeem
Authorization: Bearer <token>
```

**Request Body**:
```json
{
  "code": "A1B2C3D4"
}
```

**Response** (200 - 成功):
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "success": true,
    "courseId": 1,
    "courseTitle": "Java 入门教程",
    "enrollmentExpiresAt": "2027-01-01T00:00:00Z"
  }
}
```

**Response** (400 - 失败):
```json
{
  "code": 400,
  "message": "Redeem code has expired",
  "data": {
    "success": false,
    "errorCode": "10302",
    "errorMessage": "兑换码已过期"
  }
}
```

---

## 4. 我的课程

### 4.1 我的课程列表

```
GET /api/v1/student/my-courses
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
        "coverImage": "https://example.com/cover.jpg",
        "progress": 45,
        "enrollmentExpiresAt": "2027-01-01T00:00:00Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 5,
    "totalPages": 1
  }
}
```

---

## 5. 错误码

| 错误码 | 说明 |
|--------|------|
| 10001 | 无效验证码 |
| 10002 | 验证码已过期 |
| 10101 | 用户不存在 |
| 10102 | 用户已禁用 |
| 10301 | 兑换码不存在 |
| 10302 | 兑换码已过期 |
| 10303 | 兑换码已被使用 |
| 10304 | 兑换码关联课程不可用 |
