# 异常处理 - 异常体系规范

---

## 1. 异常体系结构

```
Throwable
│
├── Error
│   └── (系统级错误，不捕获)
│
└── Exception
    │
    ├── RuntimeException
    │   │
    │   ├── BusinessException (业务异常)
    │   │   │
    │   │   ├── AuthenticationException (认证异常)
    │   │   │   ├── InvalidVerificationCodeException
    │   │   │   └── TokenExpiredException
    │   │   │
    │   │   ├── AuthorizationException (授权异常)
    │   │   │   └── AccessDeniedException
    │   │   │
    │   │   ├── ResourceNotFoundException (资源不存在)
    │   │   │   ├── UserNotFoundException
    │   │   │   ├── CourseNotFoundException
    │   │   │   └── ChapterNotFoundException
    │   │   │
    │   │   └── BusinessRuleException (业务规则异常)
    │   │       ├── CourseNotPublishedException
    │   │       ├── RedeemCodeExpiredException
    │   │       └── AlreadyEnrolledException
    │   │
    │   └── ValidationException (验证异常)
    │       └── InvalidRequestException
    │
    └── (受检异常)
        └── ExternalServiceException (外部服务异常)
            └── TencentVodException
```

---

## 2. 异常基类

### 2.1 BusinessException（业务异常基类）

| 属性 | 类型 | 说明 |
|------|------|------|
| code | int | 错误码 |
| message | String | 错误消息 |

**设计要求**:
- 继承 `RuntimeException`
- 包含错误码和错误消息
- 提供构造函数

### 2.2 ResourceNotFoundException（资源不存在异常）

| 属性 | 类型 | 说明 |
|------|------|------|
| resourceType | String | 资源类型 |
| resourceId | Object | 资源ID |

### 2.3 BusinessRuleException（业务规则异常）

| 属性 | 类型 | 说明 |
|------|------|------|
| ruleId | String | 规则ID |

---

## 3. 异常使用规范

### 3.1 何时抛出业务异常

| 场景 | 异常类型 | 示例 |
|------|----------|------|
| 用户不存在 | ResourceNotFoundException | `throw new UserNotFoundException(userId)` |
| 验证码无效 | AuthenticationException | `throw new InvalidVerificationCodeException()` |
| 业务规则违反 | BusinessRuleException | `throw new RedeemCodeExpiredException()` |
| 请求参数校验失败 | ValidationException | `throw new InvalidRequestException(errors)` |

### 3.2 异常处理原则

| 原则 | 说明 |
|------|------|
| 早发现 | 尽早抛出异常，避免错误传播 |
| 明确消息 | 异常消息应对用户友好 |
| 不吞异常 | 不应捕获后什么都不做 |
| 记录日志 | 异常应记录日志便于排查 |

### 3.3 异常与错误码映射

| 异常类型 | HTTP 状态码 | 错误码范围 |
|----------|-------------|------------|
| AuthenticationException | 401 | 10001-10099 |
| ResourceNotFoundException | 404 | 10101-10199 |
| BusinessRuleException | 400 | 10201-10399 |
| ValidationException | 400 | 40000 |
| AccessDeniedException | 403 | 40300 |
| ExternalServiceException | 502 | 50001-50999 |
| 其他未处理异常 | 500 | 90001-90999 |

---

## 4. 约束

| 约束 | 说明 |
|------|------|
| Domain 层 | 只抛出业务异常，不抛出框架异常 |
| Application 层 | 处理异常，转换为统一的错误响应 |
| Infrastructure 层 | 捕获外部服务异常，包装后抛出 |
| API 层 | 由全局异常处理器统一处理 |
