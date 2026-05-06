# 安全规范 - 认证授权

---

## 1. JWT Token 认证

### 1.1 Token 结构

| 字段 | 说明 |
|------|------|
| sub | 用户ID |
| role | 用户角色 |
| iat | 签发时间 |
| exp | 过期时间 |

### 1.2 Token 配置

| 配置项 | 值 | 说明 |
|--------|-----|------|
| 签名算法 | RS256 | RSA SHA-256 非对称签名 |
| 过期时间 | 86400 秒 | 24 小时 |
| 密钥类型 | RSA-2048 | 自动生成于 `resources/keys/` |
| 私钥用途 | 签发 Token | 后端持有 |
| 公钥用途 | 验证 Token | 可供其他服务使用 |

### 1.3 Token 存储

| 存储位置 | 优点 | 缺点 |
|----------|------|------|
| LocalStorage | 简单 | XSS 攻击风险 |
| HttpOnly Cookie | 安全 | CSRF 攻击风险 |

**推荐**: 使用 HttpOnly Cookie 存储 Token

---

## 2. 认证流程

### 2.1 邮箱验证码登录

```
1. 用户输入邮箱 → POST /api/v1/student/auth/send-code
2. 服务器发送验证码 → 存储验证码（5分钟有效）
3. 用户输入验证码 → POST /api/v1/student/auth/verify-code
4. 服务器验证 → 返回 JWT Token
```

### 2.2 Token 验证

```
1. 客户端请求 → Header: Authorization: Bearer <token>
2. 服务器验证 Token 签名和过期时间
3. 验证通过 → 提取用户ID和角色
4. 验证失败 → 返回 401 Unauthorized
```

---

## 3. 角色权限矩阵

### 3.1 角色定义

| 角色 | 说明 |
|------|------|
| STUDENT | 学生 |
| TEACHER | 教师 |
| ADMIN | 管理员 |

### 3.2 接口权限

| 接口路径 | STUDENT | TEACHER | ADMIN |
|----------|---------|---------|-------|
| `/api/v1/student/auth/*` | ✓ | - | - |
| `/api/v1/student/courses/**` | ✓ | - | - |
| `/api/v1/student/codes/redeem` | ✓ | - | - |
| `/api/v1/student/my-courses` | ✓ | - | - |
| `/api/v1/teacher/auth/*` | - | ✓ | - |
| `/api/v1/teacher/courses/**` | - | ✓ | ✓ |
| `/api/v1/admin/auth/login` | - | - | ✓ |
| `/api/v1/admin/**` | - | - | ✓ |

### 3.3 资源所有权

| 资源 | 权限规则 |
|------|----------|
| 课程 | 仅创建教师和管理员可修改 |
| 章节/课时 | 仅课程创建教师和管理员可修改 |
| 用户 | 仅管理员可修改状态 |

---

## 4. API Key 认证（外部系统）

### 4.1 API Key 用途

用于外部系统调用兑换码生成接口。

### 4.2 API Key 管理

| 要求 | 说明 |
|------|------|
| 存储 | 环境变量或配置中心 |
| 传输 | 请求头 `X-API-Key` |
| 验证 | 服务器端验证 Key 有效性 |

### 4.3 API Key 权限

| 权限 | 说明 |
|------|------|
| 生成兑换码 | ✓ |
| 兑换兑换码 | ✗ |

---

## 5. Spring Security 配置

### 5.1 安全配置要点

| 配置项 | 推荐值 |
|--------|--------|
| CSRF | 禁用（API 场景） |
| Session | 无状态（JWT） |
| 密码加密 | BCrypt（若有） |
| 账户锁定 | 5 次失败后锁定 |

### 5.2 路径匹配

```java
// 公开路径
/public/** - permitAll
/api/v1/student/auth/** - permitAll
/api/v1/teacher/auth/** - permitAll
/api/v1/admin/auth/login - permitAll
/api/v1/redeem-codes/** - permitAll (API Key认证)

// 受保护路径
/api/v1/student/** - authenticated
/api/v1/teacher/** - hasRole(TEACHER)
/api/v1/admin/** - hasRole(ADMIN)
```

---

## 6. 约束

| 约束 | 说明 |
|------|------|
| 密钥管理 | RSA 密钥自动生成，存储于 `resources/keys/` 目录 |
| 密钥持久化 | 部署时需持久化密钥目录，避免重启后密钥丢失 |
| Token 过期 | 必须设置合理的过期时间 |
| 敏感操作 | 敏感操作需重新验证 |
| 日志脱敏 | 日志中不记录 Token 明文 |
