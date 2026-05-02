# esmile-edu 项目规范

## 1. 项目概述

面向大学生的专业课程和技能培训教育平台。

**当前阶段**: MVP 开发中

**技术栈**: Next.js 14+ / Prisma / PostgreSQL / 腾讯云 VOD

---

## 2. Git 工作流

### 分支策略

- `main` - 主分支，稳定可部署
- `feature/*` - 功能分支
- `fix/*` - 修复分支

### 提交规范

遵循 [conventional commits](https://www.conventionalcommits.org/):

```
<type>: <description>

Types: feat, fix, refactor, docs, test, chore, perf, ci
```

### 工作流程

1. **开始工作前**: 使用 `using-git-worktrees` skill 创建隔离工作区
2. **开发中**: 使用 TDD 方法，参考 `superpowers:test-driven-development`
3. **每个任务完成后**: 使用 `requesting-code-review` skill 进行代码审查
4. **任务完成时**: 使用 `finishing-a-development-branch` skill 结束分支

---

## 3. 开发流程

### PRD → 设计 → 实现

1. 需求写入 `docs/specs/`
2. 设计文档评审通过后，使用 `brainstorming` skill 细化设计
3. 使用 `writing-plans` skill 创建实施计划
4. 使用 `subagent-driven-development` skill 执行计划

### 代码审查

- 每个任务完成后必须审查
- 审查结果：Critical → 立即修复，Important → 修复后继续，Minor → 记录

---

## 4. 目录结构

```
esmile-edu/
├── docs/specs/          # 需求和设计文档
├── prisma/              # 数据库模型
├── src/
│   ├── app/             # Next.js App Router
│   │   ├── (auth)/      # 认证页面
│   │   ├── (educator)/  # 教育者端
│   │   ├── (student)/   # 学生端
│   │   ├── (home)/      # 公开页面
│   │   └── api/         # API Routes
│   ├── components/      # 通用组件
│   ├── lib/             # 工具函数
│   └── hooks/           # React Hooks
└── .worktrees/          # 工作树目录（已忽略）
```

---

## 5. 环境配置

### 必需环境变量

```env
DATABASE_URL=           # PostgreSQL 连接
NEXTAUTH_SECRET=        # 认证密钥
TENCENT_VOD_SECRET_ID=   # 腾讯云 VOD
TENCENT_VOD_SECRET_KEY=  # 腾讯云 VOD
```

---

## 6. 质量标准

- 测试覆盖率 > 80%
- 所有 Critical/Important 问题必须在合并前修复
- API 响应时间 < 500ms

---

## 7. 参考 Skills

- `superpowers:using-git-worktrees` - 创建隔离工作区
- `superpowers:finishing-a-development-branch` - 结束开发分支
- `superpowers:requesting-code-review` - 请求代码审查
- `superpowers:receiving-code-review` - 接收代码审查反馈
- `superpowers:test-driven-development` - TDD 开发方法