# 异常处理 - 错误码规范

---

## 1. 错误码分配表

| 错误码范围 | 模块 | 前缀 |
|-----------|------|------|
| 10001-10099 | 认证模块 | 10 |
| 10101-10199 | 用户模块 | 10 |
| 10201-10299 | 课程模块 | 10 |
| 10301-10399 | 兑换码模块 | 10 |
| 10401-10499 | 视频模块 | 10 |
| 40000-40099 | 请求校验 | 40 |
| 40100-40199 | 认证校验 | 40 |
| 40300-40399 | 权限校验 | 40 |
| 40400-40499 | 资源不存在 | 40 |
| 50001-50999 | 外部服务 | 50 |
| 90001-90999 | 系统错误 | 90 |

---

## 2. 详细错误码定义

### 2.1 认证模块 (10001-10099)

| 错误码 | 错误消息 | 说明 |
|--------|----------|------|
| 10001 | Invalid verification code | 无效验证码 |
| 10002 | Verification code expired | 验证码已过期 |
| 10003 | Verification code rate limited | 验证码发送过于频繁 |
| 10004 | Invalid token | 无效 Token |
| 10005 | Token expired | Token 已过期 |

### 2.2 用户模块 (10101-10199)

| 错误码 | 错误消息 | 说明 |
|--------|----------|------|
| 10101 | User not found | 用户不存在 |
| 10102 | User disabled | 用户已禁用 |
| 10103 | User pending approval | 教师待审批 |
| 10104 | Email already exists | 邮箱已注册 |
| 10105 | User is not a teacher | 用户不是教师 |
| 10106 | Cannot modify admin status | 无法修改管理员状态 |
| 10107 | Invalid status transition | 无效的状态转换 |

### 2.3 课程模块 (10201-10299)

| 错误码 | 错误消息 | 说明 |
|--------|----------|------|
| 10201 | Course not found | 课程不存在 |
| 10202 | Course not published | 课程未发布 |
| 10203 | Not course owner | 不是课程所有者 |
| 10204 | Chapter not found | 章节不存在 |
| 10205 | Lesson not found | 课时不存在 |
| 10206 | Course has no chapters | 课程没有章节（发布前检查） |
| 10207 | Already enrolled | 已选修此课程 |

### 2.4 兑换码模块 (10301-10399)

| 错误码 | 错误消息 | 说明 |
|--------|----------|------|
| 10301 | Redeem code not found | 兑换码不存在 |
| 10302 | Redeem code expired | 兑换码已过期 |
| 10303 | Redeem code already used | 兑换码已被使用 |
| 10304 | Redeem code course unavailable | 兑换码关联课程不可用 |
| 10305 | Course not found | 课程不存在 |
| 10306 | Course not published | 课程未发布 |
| 10307 | Invalid API key | API Key 无效 |
| 10308 | Exceed max generation quantity | 超出最大生成数量 |

### 2.5 视频模块 (10401-10499)

| 错误码 | 错误消息 | 说明 |
|--------|----------|------|
| 10401 | Video upload failed | 视频上传失败 |
| 10402 | Video processing failed | 视频处理失败 |
| 10403 | Video not ready | 视频未就绪 |

### 2.6 请求校验 (40000-40099)

| 错误码 | 错误消息 | 说明 |
|--------|----------|------|
| 40000 | Validation failed | 请求参数校验失败 |

### 2.7 认证校验 (40100-40199)

| 错误码 | 错误消息 | 说明 |
|--------|----------|------|
| 40100 | Authentication required | 需要认证 |
| 40101 | Invalid token | 无效 Token |
| 40102 | Token expired | Token 已过期 |

### 2.8 权限校验 (40300-40399)

| 错误码 | 错误消息 | 说明 |
|--------|----------|------|
| 40300 | Access denied | 访问被拒绝 |
| 40301 | Insufficient permissions | 权限不足 |

### 2.9 资源不存在 (40400-40499)

| 错误码 | 错误消息 | 说明 |
|--------|----------|------|
| 40400 | Resource not found | 资源不存在 |

### 2.10 外部服务 (50001-50999)

| 错误码 | 错误消息 | 说明 |
|--------|----------|------|
| 50001 | External service unavailable | 外部服务不可用 |
| 50002 | Tencent VOD service error | 腾讯云 VOD 服务错误 |
| 50003 | Email service error | 邮件服务错误 |

### 2.11 系统错误 (90001-90999)

| 错误码 | 错误消息 | 说明 |
|--------|----------|------|
| 90001 | Internal server error | 服务器内部错误 |
| 90002 | Service unavailable | 服务不可用 |

---

## 3. 错误码命名规范

| 规范 | 示例 |
|------|------|
| 使用常量 | `ErrorCodes.INVALID_VERIFICATION_CODE` |
| 命名格式 | `{MODULE}_{SHORT_NAME}` |
| 注释完整 | 每个错误码必须有错误消息 |

---

## 4. 错误码使用约束

| 约束 | 说明 |
|------|------|
| 不重复 | 错误码不能重复 |
| 不复用 | 已使用的错误码不能改变含义 |
| 保留 | 废弃的错误码应保留并标记 |
| 范围预留 | 为未来扩展预留错误码范围 |
