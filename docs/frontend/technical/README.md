# 前端技术文档

esmile-edu 教育平台前端技术文档。

---

## 目录结构

```
docs/frontend/technical/
├── 1-architecture/          # 架构设计
│   └── README.md
├── 2-modules/               # 模块设计
│   └── README.md
├── 3-state-management/      # 状态管理
│   └── README.md
├── 4-api-client/            # API 客户端
│   └── README.md
├── 5-components/            # 组件设计
│   └── README.md
├── 6-infrastructure/        # 基础设施
│   ├── video-player.md
│   └── image-upload.md
├── 7-security/              # 安全规范
│   └── README.md
├── 8-development-standards/ # 开发规范
│   ├── code-style.md
│   └── git-workflow.md
└── 9-deployment/            # 部署
    └── README.md
```

---

## 1. 架构设计

**核心**: Vue 3 + Vite + TypeScript + shadcn/ui + Tailwind CSS + Pinia

[详细文档 →](1-architecture/README.md)

---

## 2. 模块设计

**三端分离**: 学生端 / 教师端 / 管理端

[详细文档 →](2-modules/README.md)

---

## 3. 状态管理

**Pinia Store**: authStore / userStore / uiStore

[详细文档 →](3-state-management/README.md)

---

## 4. API 客户端

**Axios + Zod**: 统一请求封装、运行时类型校验

[详细文档 →](4-api-client/README.md)

---

## 5. 组件设计

**shadcn/ui**: 原子组件 + 业务组件分层

[详细文档 →](5-components/README.md)

---

## 6. 基础设施

**视频播放**: 腾讯云 VOD 播放器集成

[详细文档 →](6-infrastructure/video-player.md)

---

## 7. 安全规范

**JWT Token**: HttpOnly Cookie 存储、路由守卫

[详细文档 →](7-security/README.md)

---

## 8. 开发规范

**Code Style**: TypeScript + Vue 3 Composition API

[详细文档 →](8-development-standards/code-style.md)

---

## 9. 部署

**Vite 构建**: 多环境配置、CDN 部署

[详细文档 →](9-deployment/README.md)
