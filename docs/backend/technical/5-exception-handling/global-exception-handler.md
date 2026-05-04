# 异常处理 - 全局异常处理器规范

---

## 1. 全局异常处理器设计

### 1.1 实现要求

- 使用 `@RestControllerAdvice` 注解
- 统一处理所有 Controller 抛出的异常
- 返回统一的 `ApiResponse` 格式

### 1.2 异常处理方法映射

| 异常类型 | 处理方法 | HTTP 状态码 |
|----------|----------|-------------|
| MethodArgumentNotValidException | handleValidation | 400 |
| BusinessException | handleBusiness | 由异常决定 |
| AccessDeniedException | handleAccessDenied | 403 |
| ResourceNotFoundException | handleNotFound | 404 |
| Exception | handleGeneral | 500 |

---

## 2. 处理器实现规范

### 2.1 校验异常处理

**异常**: `MethodArgumentNotValidException`
**处理**: 收集所有字段错误，拼接为错误消息
**响应码**: 40000

```json
{
  "code": 40000,
  "message": "Validation failed: field1: error1; field2: error2",
  "data": null
}
```

### 2.2 业务异常处理

**异常**: `BusinessException`
**处理**: 直接返回异常的 code 和 message
**响应码**: 由异常 code 决定

```json
{
  "code": 10101,
  "message": "User not found",
  "data": null
}
```

### 2.3 资源不存在异常处理

**异常**: `ResourceNotFoundException`
**处理**: 返回 404 和异常消息
**响应码**: 40400

```json
{
  "code": 40400,
  "message": "User not found with id: 123",
  "data": null
}
```

### 2.4 权限异常处理

**异常**: `AccessDeniedException`
**处理**: 返回 403
**响应码**: 40300

```json
{
  "code": 40300,
  "message": "Access denied",
  "data": null
}
```

### 2.5 通用异常处理

**异常**: `Exception`
**处理**: 记录日志，返回 500
**响应码**: 90001

```json
{
  "code": 90001,
  "message": "Internal server error",
  "data": null
}
```

---

## 3. 日志记录规范

| 异常类型 | 日志级别 | 日志内容 |
|----------|----------|----------|
| ValidationException | WARN | 请求参数校验失败 |
| BusinessException | WARN | 业务规则违反 |
| ResourceNotFoundException | WARN | 资源不存在 |
| AccessDeniedException | WARN | 权限不足 |
| 其他 Exception | ERROR | 服务器内部错误（含堆栈） |

---

## 4. 响应头处理

### 4.1 追踪ID

- 生成 UUID 作为请求追踪ID
- 在响应头中返回 `X-Request-Id`
- 便于日志排查

### 4.2 响应头示例

```
X-Request-Id: 550e8400-e29b-41d4-a716-446655440000
X-Response-Time: 123
```

---

## 5. 约束

| 约束 | 说明 |
|------|------|
| 不修改异常 | 全局处理器不应修改异常内容 |
| 记录日志 | 所有异常都应记录日志 |
| 用户友好 | 面向用户的错误消息应友好 |
| 堆栈保护 | 生产环境不返回详细堆栈 |
