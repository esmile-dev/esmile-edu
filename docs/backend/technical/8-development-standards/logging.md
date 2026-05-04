# 开发规范 - 日志规范

---

## 1. 日志框架

| 框架 | 说明 |
|------|------|
| Slf4j | 日志门面 |
| Logback | 日志实现 |

---

## 2. 日志级别

### 2.1 级别定义

| 级别 | 优先级 | 使用场景 |
|------|--------|----------|
| ERROR | 高 | 错误异常，影响功能 |
| WARN | 中 | 警告，可恢复的错误 |
| INFO | 低 | 重要业务操作 |
| DEBUG | 低 | 开发调试 |

### 2.2 使用规范

| 级别 | 使用场景 |
|------|----------|
| ERROR | 异常捕获、数据库错误、外部服务失败 |
| WARN | 业务规则违反、参数校验失败 |
| INFO | 用户登录、订单创建、支付完成 |
| DEBUG | 方法入参、出参、SQL 日志 |

---

## 3. 日志格式

### 3.1 标准格式

```xml
[%d{yyyy-MM-dd HH:mm:ss.SSS}] [%thread] [%level] [%logger{36}] - %msg%n
```

### 3.2 输出示例

```
[2026-05-03 10:00:00.123] [http-nio-8080-exec-1] [INFO ] [c.e.e.u.a.s.AuthenticationService] - User login: email=user@example.com
[2026-05-03 10:00:00.456] [http-nio-8080-exec-1] [ERROR] [c.e.e.u.a.s.AuthenticationService] - Login failed: email=user@example.com, error=Invalid code
```

---

## 4. 日志内容规范

### 4.1 必含信息

| 信息 | 格式 | 说明 |
|------|------|------|
| 时间戳 | `yyyy-MM-dd HH:mm:ss.SSS` | 精确到毫秒 |
| 线程名 | - | 便于排查并发问题 |
| 日志级别 | - | ERROR/WARN/INFO/DEBUG |
| Logger 名 | 类全限定名 | 定位日志来源 |
| 消息内容 | - | 描述事件 |

### 4.2 消息模板

```java
// 使用占位符
log.info("User login: email={}, userId={}", email, userId);

// 禁止字符串拼接
log.info("User login: email=" + email);  // 禁止
```

### 4.3 敏感信息处理

| 信息 | 处理方式 |
|------|----------|
| 密码 | 禁止记录 |
| Token | 脱敏 `tok***abc` |
| 手机号 | 脱敏 `138****5678` |
| 邮箱 | 脱敏 `u***@example.com` |

---

## 5. 日志输出

### 5.1 开发环境

- 输出到控制台
- 开启 DEBUG 日志

### 5.2 生产环境

- 输出到文件
- 只开启 INFO 及以上级别
- 开启日志滚动

---

## 6. 约束

| 约束 | 说明 |
|------|------|
| 日志级别 | 生产环境不记录 DEBUG |
| 异常日志 | 必须包含堆栈信息 |
| 敏感信息 | 禁止记录敏感数据 |
| 日志格式 | 统一使用标准格式 |
| 性能 | 避免大量日志影响性能 |
