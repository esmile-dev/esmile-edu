# 基础设施 - 数据库配置规范

---

## 1. 数据库版本管理 (Flyway)

### 1.1 概述

使用 Flyway 进行数据库版本管理，所有数据库变更通过迁移脚本管理。

### 1.2 迁移脚本位置

```
src/main/resources/
└── db/
    └── migration/
        ├── V1__initial_schema.sql
        ├── V2__xxx.sql
        └── ...
```

### 1.3 Spring Boot 配置

```properties
# 启用 Flyway
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true
spring.flyway.validate-on-migrate=true
```

### 1.4 开发环境配置

开发环境使用 H2 内存数据库，`ddl-auto=create-drop`，Flyway 禁用：

```properties
spring.flyway.enabled=false
spring.jpa.hibernate.ddl-auto=create-drop
```

### 1.5 生产环境配置

生产环境使用 PostgreSQL，Flyway 启用，`ddl-auto=validate`：

```properties
spring.flyway.enabled=true
spring.jpa.hibernate.ddl-auto=validate
```

### 1.6 命名规范

| 类型 | 格式 | 示例 |
|------|------|------|
| 版本 | `V{version}__{description}.sql` | `V1__initial_schema.sql` |
| 版本号 | 数字递增 | V1, V2, V3... |
| 描述 | 下划线分隔 | `V2__add_user_index.sql` |

---

## 2. 数据库连接配置

### 1.1 配置项

| 配置项 | 环境变量 | 说明 |
|--------|----------|------|
| JDBC URL | `DB_HOST`, `DB_PORT`, `DB_NAME` | PostgreSQL 连接地址 |
| 用户名 | `DB_USERNAME` | 数据库用户名 |
| 密码 | `DB_PASSWORD` | 数据库密码 |

### 1.2 Spring Boot 配置格式

```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

---

## 3. JPA 配置

### 2.1 配置项

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
        jdbc:
          time_zone: UTC+8
    show-sql: false
    open-in-view: false
```

### 2.2 配置说明

| 配置项 | 推荐值 | 说明 |
|--------|--------|------|
| ddl-auto | `validate` | 仅验证 Schema，不自动修改 |
| dialect | PostgreSQLDialect | PostgreSQL 方言 |
| format_sql | `true` | 格式化 SQL 日志 |
| open-in-view | `false` | 禁止 Open Session In View |

---

## 4. Hikari 连接池配置

### 3.1 配置参数

| 参数 | 开发环境 | 生产环境 | 说明 |
|------|----------|----------|------|
| maximum-pool-size | 10 | 20 | 最大连接数 |
| minimum-idle | 2 | 5 | 最小空闲连接 |
| connection-timeout | 30s | 30s | 等待连接超时 |
| idle-timeout | 10min | 10min | 空闲连接超时 |
| max-lifetime | 30min | 30min | 连接最大生命周期 |

### 3.2 监控指标

| 指标 | 说明 |
|------|------|
| active | 当前活跃连接数 |
| idle | 当前空闲连接数 |
| waiting | 等待获取连接的线程数 |
| total | 总连接数 |

---

## 5. 数据库审计配置

### 4.1 自动填充审计字段

使用 JPA Auditing 自动填充 `createdAt` 和 `updatedAt`：

```java
@Configuration
@EnableJpaAuditing
public class JpaConfig {
    // 配置审计监听器
}
```

### 4.2 审计注解

| 注解 | 说明 |
|------|------|
| @CreatedDate | 自动设置创建时间 |
| @LastModifiedDate | 自动设置更新时间 |

---

## 6. 事务配置

### 5.1 事务隔离级别

| 级别 | 说明 | 使用场景 |
|------|------|----------|
| READ_COMMITTED | 已提交读（默认） | 大多数业务 |
| REPEATABLE_READ | 可重复读 | 财务相关 |
| SERIALIZABLE | 串行化 | 高并发写入 |

### 5.2 事务传播行为

| 传播行为 | 说明 | 使用场景 |
|----------|------|----------|
| REQUIRED | 在当前事务中执行（默认） | 大多数场景 |
| REQUIRES_NEW | 在新事务中执行 | 独立操作 |
| NESTED | 在嵌套事务中执行 | 部分回滚 |

---

## 7. 约束

| 约束 | 说明 |
|------|------|
| 密码存储 | 数据库密码必须通过环境变量注入 |
| 连接池大小 | 根据服务器资源合理配置 |
| SQL 日志 | 生产环境关闭 `show-sql` |
| ddl-auto | 生产环境必须使用 `validate`，禁止 `update` |
| Flyway 迁移 | 所有数据库变更必须通过迁移脚本，禁止手动修改数据库 |
| 开发环境 | 使用 dev profile，H2 + JPA ddl-auto，Flyway 禁用 |
| 生产环境 | 使用默认 profile，PostgreSQL + Flyway，ddl-auto=validate |
