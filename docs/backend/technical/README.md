# esmile-edu 后端技术文档

**版本**: v1.0
**日期**: 2026-05-03

---

## 文档目的

本套文档面向**后端开发人员**和 **AI Coding Agent**，提供：
- **设计方案**: 架构、模块划分、接口设计
- **规范**: 编码规范、API 规范、命名规范
- **约束**: 边界条件、业务规则、禁止事项

> **注意**: 本文档不包含完整代码实现。开发者和 AI Agent 应根据设计规范自行生成代码。

---

## 文档结构

| 章节 | 文件 | 内容 |
|------|------|------|
| 架构设计 | [1-architecture/README.md](1-architecture/README.md) | 模块划分、DDD分层、限界上下文 |
| 模块设计 | [2-modules/*.md](2-modules/) | 用户/课程/兑换码模块设计规范 |
| 数据模型 | [3-data-model/*.md](3-data-model/) | 实体规范、数据库Schema、值对象 |
| API规范 | [4-api-specification/*.md](4-api-specification/) | 各端API设计规范 |
| 异常处理 | [5-exception-handling/*.md](5-exception-handling/) | 异常体系、错误码规范 |
| 基础设施 | [6-infrastructure/*.md](6-infrastructure/) | 数据库、腾讯云VOD、邮件配置 |
| 安全规范 | [7-security/*.md](7-security/) | 认证授权、接口防护 |
| 开发规范 | [8-development-standards/*.md](8-development-standards/) | 代码组织、日志规范 |
| 部署指南 | [9-deployment/*.md](9-deployment/) | 环境配置、构建运行 |

---

## 模块依赖关系

```
esmile-edu-common          # 无依赖
       │
       ├── esmile-edu-user        # 依赖 common
       │         │
       │         └── esmile-edu-course      # 依赖 common, user
       │                   │
       │                   └── esmile-edu-redeem  # 依赖 common, user, course
```

---

## 技术栈

| 组件 | 版本 |
|------|------|
| Java | 25 LTS |
| Spring Boot | 3.3.x |
| Spring Data JPA | 6.x |
| PostgreSQL | 15+ |

---

## 快速开始

1. 阅读 [1-architecture/README.md](1-architecture/README.md) 理解整体架构
2. 阅读 [2-modules/user-module.md](2-modules/user-module.md) 了解用户模块设计
3. 按模块依赖顺序实现各模块

---

## 核心原则

### DDD 分层依赖规则

```
API → Application → Domain ← Infrastructure
```

- **Domain 层**: 纯 Java，不依赖任何框架
- **Application 层**: 定义端口接口，依赖 Domain
- **Infrastructure 层**: 实现 Domain 中定义的接口
- **API 层**: 依赖 Application 层

### 限界上下文隔离

每个业务模块是独立的限界上下文：
- 跨模块通信使用 ID 而非对象引用
- 使用领域事件实现跨模块状态同步

---

## 文档更新日志

| 日期 | 版本 | 更新内容 |
|------|------|----------|
| 2026-05-03 | v1.0 | 初始版本 |
