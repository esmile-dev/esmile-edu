# API 规范 - 外部系统接口

**Base Path**: `/api/v1/redeem-codes`

---

## 1. 认证方式

外部系统接口使用 **API Key** 认证：

```
X-API-Key: <api_key>
```

**说明**:
- API Key 通过环境变量或配置中心管理
- 验证失败返回 `401 Unauthorized`

---

## 2. 兑换码生成接口

### 2.1 申请生成兑换码

```
POST /api/v1/redeem-codes/apply
X-API-Key: <api_key>
Content-Type: application/json
```

**Request Body**:
```json
{
  "courseId": 1,
  "expiresAt": "2026-06-03T00:00:00Z",
  "courseExpiresAt": "2027-01-01T00:00:00Z",
  "quantity": 10
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| courseId | Long | 是 | 课程ID |
| expiresAt | LocalDateTime | 是 | 兑换码有效期（必须晚于当前时间） |
| courseExpiresAt | LocalDateTime | 否 | 兑换后课程权限期限，null 表示永久有效 |
| quantity | Integer | 是 | 生成数量（1-100） |

**Response** (200):
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "codes": [
      "A1B2C3D4",
      "E5F6G7H8",
      "I9J0K1L2",
      "M3N4O5P6",
      "Q7R8S9T0",
      "U1V2W3X4",
      "Y5Z6A7B8",
      "C9D0E1F2",
      "G3H4I5J6",
      "K7L8M9N0"
    ]
  }
}
```

---

## 3. 错误响应

### 3.1 API Key 无效

**Response** (401):
```json
{
  "code": 401,
  "message": "Invalid API Key",
  "data": null
}
```

### 3.2 请求参数错误

**Response** (400):
```json
{
  "code": 400,
  "message": "Validation failed",
  "data": {
    "errors": [
      {
        "field": "quantity",
        "message": "must be between 1 and 100"
      }
    ]
  }
}
```

### 3.3 业务错误

| 错误码 | 说明 |
|--------|------|
| 10305 | 课程不存在 |
| 10306 | 课程未发布 |
| 10308 | 超出最大生成数量 |

---

## 4. 接口约束

| 约束 | 说明 |
|------|------|
| 请求频率 | 每分钟最多 100 次 |
| 批量数量 | 每次最多生成 100 个 |
| 课程验证 | 必须验证课程存在且已发布 |
| 日志记录 | 所有请求必须记录日志 |
