# API 规范 - 通用规范

---

## 1. 基础规范

### 1.1 URL 规范

| 规范 | 要求 | 示例 |
|------|------|------|
| 版本控制 | URL 路径包含版本 | `/api/v1/` |
| 路径命名 | 使用复数名词 | `/api/v1/users` |
| 嵌套资源 | 使用路径参数 | `/api/v1/courses/{courseId}/chapters` |
| 端点命名 | 使用名词，不使用动词 | `/api/v1/courses` 而非 `/api/v1/getCourses` |

### 1.2 域名规范

| 环境 | 域名 |
|------|------|
| 开发环境 | `http://localhost:8080` |
| 测试环境 | `https://api-test.esmile.edu` |
| 生产环境 | `https://api.esmile.edu` |

### 1.3 Content-Type

所有请求和响应必须使用 `application/json`：

```
Content-Type: application/json
Accept: application/json
```

---

## 2. 认证方式

### 2.1 Bearer Token（学生/教师）

| 说明 | 值 |
|------|---|
| Header | `Authorization: Bearer <token>` |
| Token 类型 | JWT |
| 有效期 | 24 小时 |

### 2.2 API Key（外部系统）

| 说明 | 值 |
|------|---|
| Header | `X-API-Key: <api_key>` |
| 用途 | 兑换码生成等外部系统接口 |

---

## 3. 统一响应格式

### 3.1 ApiResponse 结构

```json
{
  "code": 200,
  "message": "Success",
  "data": { ... }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| code | int | HTTP 状态码或业务错误码 |
| message | String | 状态描述 |
| data | Object | 响应数据，可为 null |

### 3.2 响应状态码

| HTTP 状态码 | 说明 |
|-------------|------|
| 200 | 成功 |
| 201 | 创建成功 |
| 204 | 无内容（删除成功） |
| 400 | 请求参数错误 |
| 401 | 未认证 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

### 3.3 成功响应示例

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "id": 1,
    "name": "Example"
  }
}
```

### 3.4 错误响应示例

```json
{
  "code": 40001,
  "message": "Invalid verification code",
  "data": null
}
```

---

## 4. 分页规范

### 4.1 分页请求参数

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| page | int | 0 | 页码（从0开始） |
| size | int | 20 | 每页条数 |
| sort | String | createdAt,desc | 排序字段和方向 |

### 4.2 分页响应格式

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "content": [ ... ],
    "page": 0,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5
  }
}
```

### 4.3 排序格式

```
sort=createdAt,desc
sort=title,asc
sort=createdAt,desc;orderNum,asc
```

---

## 5. 请求头规范

### 5.1 通用请求头

| Header | 必填 | 说明 |
|--------|------|------|
| Content-Type | 是 | `application/json` |
| Authorization | 需认证接口 | `Bearer <token>` |
| X-API-Key | 外部系统接口 | API Key |
| X-Request-Id | 否 | 请求追踪ID |

### 5.2 响应头

| Header | 说明 |
|--------|------|
| X-Request-Id | 请求追踪ID |
| X-Response-Time | 响应时间（毫秒） |

---

## 6. 路径参数和查询参数

### 6.1 路径参数

```
GET /api/v1/teacher/courses/{courseId}
GET /api/v1/admin/users/{userId}/approve
```

### 6.2 查询参数

```
GET /api/v1/admin/users?role=TEACHER&status=PENDING_APPROVAL&page=0&size=20
```

### 6.3 请求体

```
POST /api/v1/teacher/courses
Content-Type: application/json

{
  "title": "Java 入门教程",
  "description": "适合零基础学员",
  "coverImage": "https://example.com/cover.jpg"
}
```

---

## 7. 常用约束

| 约束 | 说明 |
|------|------|
| 字符串非空 | 使用 `@NotBlank` |
| 字符串长度 | 使用 `@Size(min, max)` |
| 数字范围 | 使用 `@Min`, `@Max` |
| 邮箱格式 | 使用 `@Email` |
| 日期格式 | ISO 8601 格式：`2026-05-03T10:00:00Z` |

---

## 8. API 版本管理

- 当前版本：`v1`
- URL 路径：`/api/v1/`
- 版本升级时保留旧版本，标记 `@Deprecated`
