# API 规范 - 管理员端

**Base Path**: `/api/v1/admin`

---

## 1. 认证接口

### 1.1 管理员登录

```
POST /api/v1/admin/auth/login
```

**Request Body**:
```json
{
  "email": "admin@esmile.edu",
  "password": "..."
}
```

**Response** (200):
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "expiresIn": 86400
  }
}
```

---

## 2. 用户管理接口

### 2.1 用户列表

```
GET /api/v1/admin/users
Authorization: Bearer <token>
```

**Query Parameters**:
| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| role | String | - | 筛选角色：STUDENT, TEACHER |
| status | String | - | 筛选状态：PENDING_APPROVAL, ACTIVE, DISABLED |
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
        "id": 2,
        "email": "teacher@example.com",
        "nickname": "Teacher Zhang",
        "avatar": null,
        "role": "TEACHER",
        "status": "PENDING_APPROVAL",
        "createdAt": "2026-05-01T10:00:00Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 10,
    "totalPages": 1
  }
}
```

### 2.2 审批教师

```
PUT /api/v1/admin/users/{id}/approve
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
    "role": "TEACHER",
    "status": "ACTIVE",
    "updatedAt": "2026-05-03T10:00:00Z"
  }
}
```

### 2.3 更新用户状态

```
PUT /api/v1/admin/users/{id}/status
Authorization: Bearer <token>
```

**Request Body**:
```json
{
  "status": "DISABLED",
  "reason": "Violation of terms"
}
```

**Response** (200):
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "id": 2,
    "status": "DISABLED"
  }
}
```

---

## 3. 课程管理接口

### 3.1 课程列表

```
GET /api/v1/admin/courses
Authorization: Bearer <token>
```

**Query Parameters**:
| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| status | String | - | 筛选状态：DRAFT, PUBLISHED |
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
        "educator": {
          "id": 2,
          "nickname": "Teacher Zhang"
        },
        "status": "DRAFT",
        "createdAt": "2026-05-01T10:00:00Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 30,
    "totalPages": 2
  }
}
```

### 3.2 修改课程状态

```
PUT /api/v1/admin/courses/{id}/status
Authorization: Bearer <token>
```

**Request Body**:
```json
{
  "status": "PUBLISHED"
}
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

---

## 4. 数据统计接口

### 4.1 统计概览

```
GET /api/v1/admin/stats
Authorization: Bearer <token>
```

**Response** (200):
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "totalUsers": 1000,
    "totalStudents": 950,
    "totalTeachers": 48,
    "pendingTeachers": 5,
    "totalCourses": 100,
    "publishedCourses": 80,
    "totalEnrollments": 5000,
    "totalRedeemCodes": 10000,
    "redeemedCodes": 8000
  }
}
```

---

## 5. 错误码

| 错误码 | 说明 |
|--------|------|
| 10101 | 用户不存在 |
| 10105 | 用户不是教师 |
| 10106 | 无法修改管理员状态 |
| 10201 | 课程不存在 |
