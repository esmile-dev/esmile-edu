# 部署

---

## 1. 构建

### 1.1 环境变量

```bash
# .env.production
VITE_API_BASE_URL=https://api.esmile.edu
VITE_TENCENT_VOD_APP_ID=xxx

# .env.development
VITE_API_BASE_URL=http://localhost:8080
```

### 1.2 构建命令

```bash
# 构建生产环境
npm run build

# 预览构建结果
npm run preview
```

### 1.3 构建产物

```
dist/
├── index.html
├── assets/
│   ├── index-[hash].js
│   ├── index-[hash].css
│   └── images/
└── static/
```

---

## 2. 部署配置

### 2.1 Nginx 配置

```nginx
server {
    listen 80;
    server_name www.esmile.edu;

    root /var/www/esmile-edu/dist;
    index index.html;

    # SPA 路由支持
    location / {
        try_files $uri $uri/ /index.html;
    }

    # 静态资源缓存
    location /assets/ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    # API 代理
    location /api/ {
        proxy_pass http://backend:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # gzip 压缩
    gzip on;
    gzip_types text/plain text/css application/json application/javascript;
}
```

### 2.2 Docker

```dockerfile
# Dockerfile
FROM nginx:alpine
COPY dist/ /usr/share/nginx/html/
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
```

---

## 3. 多环境部署

### 3.1 环境矩阵

| 环境 | 域名 | API Base URL |
|------|------|--------------|
| 开发 | localhost | http://localhost:8080 |
| 测试 | https://test.esmile.edu | https://api-test.esmile.edu |
| 生产 | https://www.esmile.edu | https://api.esmile.edu |

### 3.2 部署脚本

```bash
#!/bin/bash
# deploy.sh

ENV=$1
BUILD_CMD="npm run build -- --mode $ENV"

case $ENV in
  development)
    npm run dev
    ;;
  production)
    $BUILD_CMD
    rsync -avz dist/ deploy@www.esmile.edu:/var/www/esmile-edu/
    ;;
  *)
    echo "Unknown environment: $ENV"
    exit 1
    ;;
esac
```

---

## 4. CDN 配置

### 4.1 静态资源 CDN

```typescript
// vite.config.ts
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  base: 'https://cdn.esmile.edu/',
  build: {
    assetsDir: 'assets',
    rollupOptions: {
      output: {
        assetFileNames: 'assets/[name]-[hash][extname]',
        chunkFileNames: 'assets/[name]-[hash].js',
      },
    },
  },
})
```

---

## 5. 性能优化

### 5.1 代码分割

```typescript
// vite.config.ts
export default defineConfig({
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          'vendor': ['vue', 'vue-router', 'pinia'],
          'ui': ['@radix-ui/vue- primitives'],
        },
      },
    },
  },
})
```

### 5.2 图片优化

```vue
<!-- 使用 lazy loading -->
<img v-for="img in images" :src="img.url" loading="lazy" />

<!-- 使用现代格式 -->
<picture>
  <source :srcset="image.avif" type="image/avif" />
  <source :srcset="image.webp" type="image/webp" />
  <img :src="image.jpg" alt="..." />
</picture>
```

---

## 6. 监控

### 6.1 错误监控

```typescript
// main.ts
import { initErrorHandler } from '@/common/utils/errorHandler'

// 全局错误处理
window.onerror = (message, source, lineno, colno, error) => {
  console.error('Global error:', { message, source, lineno, colno, error })
  // 上报到监控服务
}

window.onunhandledrejection = (event) => {
  console.error('Unhandled promise rejection:', event.reason)
}
```

---

## 7. 检查清单

### 7.1 上线前检查

| 检查项 | 说明 |
|--------|------|
| 环境变量 | 生产环境变量正确配置 |
| 构建成功 | `npm run build` 无错误 |
| 路由正常 | 所有页面可访问 |
| API 联调 | 后端接口可正常调用 |
| 错误处理 | 错误边界正常展示 |
| 性能 | 首屏加载 < 3s |
