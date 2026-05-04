# P0-4: Verification Code Persistence and Rate Limiting - Technical Design

## 1. Summary

将验证码从内存存储改为数据库持久化，实现频率限制，抽象邮件服务接口便于后续切换服务商。

## 2. 需求

### 2.1 功能需求
- 验证码持久化到 PostgreSQL（替代 ConcurrentHashMap）
- 6位数字验证码
- 5分钟过期
- 一次性使用（验证后标记已用）
- 绑定 email 和 role

### 2.2 频率限制
| 类型 | 阈值 | 窗口 | 范围 |
|------|------|------|------|
| Email 限制 | 5次 | 1分钟 | 每邮箱 |
| IP 限制 | 10次 | 1小时 | 每IP |

### 2.3 邮件服务抽象
- 使用 Strategy 模式抽象 EmailService 接口
- 支持: Mock, Tencent Cloud, SendGrid, AWS SES
- 配置驱动切换服务商

## 3. 数据库 Schema

### V2__verification_codes.sql

```sql
-- 验证码表
CREATE TABLE verification_codes (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    code VARCHAR(6) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('STUDENT', 'TEACHER')),
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_verification_code_email_code UNIQUE (email, code)
);

CREATE INDEX idx_verification_codes_email ON verification_codes(email);
CREATE INDEX idx_verification_codes_expires_at ON verification_codes(expires_at);

-- 频率限制表
CREATE TABLE rate_limit_requests (
    id BIGSERIAL PRIMARY KEY,
    identifier VARCHAR(255) NOT NULL,
    identifier_type VARCHAR(20) NOT NULL CHECK (identifier_type IN ('EMAIL', 'IP')),
    request_count INTEGER NOT NULL DEFAULT 1,
    window_start TIMESTAMP NOT NULL,
    window_end TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_rate_limit_identifier_window UNIQUE (identifier, identifier_type, window_start)
);

CREATE INDEX idx_rate_limit_identifier ON rate_limit_requests(identifier);
CREATE INDEX idx_rate_limit_window ON rate_limit_requests(window_start, window_end);
```

## 4. 类结构

```
module/auth/
├── VerificationCodeEntity.java      # 验证码实体
├── VerificationCodeRepository.java   # 验证码仓库
├── RateLimitRequestEntity.java      # 频率限制实体
└── RateLimitRepository.java         # 频率限制仓库

common/auth/
├── VerificationCodeService.java      # 重构：使用数据库
├── RateLimitService.java            # 新增：频率限制
└── EmailService.java               # 新增：邮件服务接口

common/email/
├── EmailService.java                # 接口
├── MockEmailProvider.java           # 开发环境 Mock
├── TencentCloudEmailProvider.java  # 腾讯云实现
└── EmailServiceFactory.java        # 工厂类
```

## 5. 接口定义

### EmailService.java
```java
public interface EmailService {
    boolean sendVerificationCode(String to, String code);
    String getProviderName();
}
```

### RateLimitService.java
```java
public void checkEmailRateLimit(String email);  // 5次/分钟
public void checkIpRateLimit(String ip);        // 10次/小时
```

## 6. UserBizService.sendCode() 更新

```java
@Transactional
public void sendCode(String email, Role role) {
    // 1. 频率限制检查
    rateLimitService.checkEmailRateLimit(email);
    rateLimitService.checkIpRateLimit(getClientIp());
    
    // 2. 生成并持久化验证码
    String code = verificationCodeService.generateCode(email, role);
    
    // 3. 发送邮件
    emailService.sendVerificationCode(email, code);
}
```

## 7. 配置

```properties
# email.provider: mock, tencent, sendgrid, aws
email.provider=${EMAIL_PROVIDER:mock}

# 开发环境使用 mock
spring.profiles.include=dev
```

## 8. 测试策略

移除 `getStoredCode()` 方法，改用：
- MockEmailProvider 捕获发送的邮件进行断言
- 直接测试数据库中的验证码记录

## 9. 文件清单

### 新增文件
| 文件 | 说明 |
|------|------|
| V2__verification_codes.sql | Flyway 迁移脚本 |
| module/auth/VerificationCodeEntity.java | 实体 |
| module/auth/VerificationCodeRepository.java | 仓库 |
| module/auth/RateLimitRequestEntity.java | 实体 |
| module/auth/RateLimitRepository.java | 仓库 |
| common/auth/RateLimitService.java | 频率限制服务 |
| common/email/EmailService.java | 接口 |
| common/email/MockEmailProvider.java | 开发 Mock |
| common/email/TencentCloudEmailProvider.java | 腾讯云实现 |
| common/email/EmailServiceFactory.java | 工厂 |

### 修改文件
| 文件 | 说明 |
|------|------|
| VerificationCodeService.java | 重构使用数据库，移除 getStoredCode() |
| UserBizService.java | 集成频率限制和邮件服务 |
| BusinessException.java | 已有 VERIFICATION_CODE_RATE_LIMITED (10003) |

## 10. 实施顺序

1. **Phase 1**: 数据库持久化 - V2 migration + Entity + Repository
2. **Phase 2**: 频率限制 - RateLimitService 集成
3. **Phase 3**: 邮件抽象 - EmailService 接口 + Mock 实现
4. **Phase 4**: 测试覆盖 + 清理

## 11. 回滚计划

```bash
git checkout HEAD~1 -- VerificationCodeService.java
git checkout HEAD~1 -- UserBizService.java
```
