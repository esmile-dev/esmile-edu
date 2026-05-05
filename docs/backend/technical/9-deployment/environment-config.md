# 部署指南 - 环境配置

---

## 1. 环境说明

| 环境 | 用途 | 域名 |
|------|------|------|
| 开发环境 | 本地开发 | localhost:8080 |
| 测试环境 | QA 测试 | api-test.esmile.edu |
| 生产环境 | 正式服务 | api.esmile.edu |

---

## 2. 环境变量清单

### 2.1 数据库

| 变量名 | 示例值 | 说明 |
|--------|--------|------|
| `DB_HOST` | 127.0.0.1 | 数据库主机 |
| `DB_PORT` | 5432 | 数据库端口 |
| `DB_NAME` | esmile_edu | 数据库名 |
| `DB_USERNAME` | esmile_user | 数据库用户名 |
| `DB_PASSWORD` | ******* | 数据库密码 |

### 2.2 腾讯云 VOD

| 变量名 | 示例值 | 说明 |
|--------|--------|------|
| `TENCENT_VOD_SECRET_ID` | AKIDxxxx | SecretId |
| `TENCENT_VOD_SECRET_KEY` | ******* | SecretKey |
| `TENCENT_VOD_APP_ID` | 123456789 | AppId |
| `TENCENT_VOD_REGION` | ap-guangzhou | 区域 |

### 2.3 邮件服务

| 变量名 | 示例值 | 说明 |
|--------|--------|------|
| `MAIL_HOST` | smtp.example.com | SMTP 服务器 |
| `MAIL_PORT` | 587 | SMTP 端口 |
| `MAIL_USERNAME` | noreply@esmile.edu | 用户名 |
| `MAIL_PASSWORD` | ******* | 密码 |
| `MAIL_FROM` | noreply@esmile.edu | 发件人地址 |

### 2.4 安全

| 变量名 | 示例值 | 说明 |
|--------|--------|------|
| `JWT_PRIVATE_KEY_PATH` | `resources/keys/private.pem` | RSA 私钥路径 |
| `JWT_PUBLIC_KEY_PATH` | `resources/keys/public.pem` | RSA 公钥路径 |
| `EXTERNAL_API_KEY` | ******* | 外部系统 API Key |

**注意**：RSA 密钥在首次启动时自动生成，无需手动配置。

---

## 3. Profile 配置

### 3.1 开发环境 (dev)

```yaml
spring:
  profiles:
    active: dev
  datasource:
    url: jdbc:postgresql://localhost:5432/esmile_edu_dev
    username: dev_user
    password: dev_password
  jpa:
    show-sql: true
    hibernate:
      ddl-auto: update
```

### 3.2 测试环境 (test)

```yaml
spring:
  profiles:
    active: test
  datasource:
    url: jdbc:postgresql://test-db:5432/esmile_edu_test
    username: test_user
    password: test_password
  jpa:
    show-sql: false
    hibernate:
      ddl-auto: validate
```

### 3.3 生产环境 (prod)

```yaml
spring:
  profiles:
    active: prod
  datasource:
    url: jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    show-sql: false
    hibernate:
      ddl-auto: validate
```

---

## 4. 启动命令

### 4.1 直接运行

```bash
java -jar esmile-edu.jar --spring.profiles.active=prod
```

### 4.2 Docker 运行

```bash
docker run -d \
  --name esmile-edu \
  -p 8080:8080 \
  -v jwt_keys:/run/secrets/jwt \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_HOST=db-host \
  -e DB_PORT=5432 \
  -e DB_NAME=esmile_edu \
  -e DB_USERNAME=esmile_user \
  -e DB_PASSWORD=****** \
  -e JWT_PRIVATE_KEY_PATH=/run/secrets/jwt/private.pem \
  -e JWT_PUBLIC_KEY_PATH=/run/secrets/jwt/public.pem \
  esmile-edu:latest
```

---

## 5. 健康检查

### 5.1 健康检查端点

```
GET /actuator/health
```

**响应**:
```json
{
  "status": "UP"
}
```

### 5.2 探活检查

```bash
curl http://localhost:8080/actuator/health
```

---

## 6. 约束

| 约束 | 说明 |
|------|------|
| 密码管理 | 生产环境密码必须通过环境变量注入 |
| ddl-auto | 生产环境必须使用 `validate` |
| 日志级别 | 生产环境不记录 DEBUG |
| 健康检查 | 必须配置健康检查端点 |
