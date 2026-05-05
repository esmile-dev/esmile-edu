# 腾讯云 VOD 视频播放功能设计

**日期**: 2026-05-05
**状态**: 已批准
**版本**: v1.0

---

## 1. 概述

### 1.1 背景

MVP 阶段采用手动上传视频方式：教师通过腾讯云控制台上传视频，获得 videoId 后录入系统。学生观看时，系统从腾讯云 VOD 获取视频并播放。

### 1.2 目标

实现安全、可落地的视频播放功能：
- 支持已上传视频的播放
- 使用 Key 防盗链保护视频内容
- 后端按需生成签名播放 URL
- 前端使用 TCPlayer 播放

---

## 2. 架构设计

### 2.1 数据流

```
┌─────────────┐     1.获取播放URL      ┌─────────────┐
│   学生端    │ ─────────────────────→ │   后端      │
│ (TCPlayer) │ ←───────────────────── │  /video/*  │
└─────────────┘     2.签名播放URL      └─────────────┘
                                             │
                                             │ 3.DescribeMediaInfos
                                             ↓
                                      ┌─────────────┐
                                      │  腾讯云 VOD │
                                      └─────────────┘
```

### 2.2 安全流程

1. 教师上传视频至腾讯云 VOD 控制台 → 获取 videoId
2. 教师录入 videoId → 后端更新 lesson.videoId
3. 学生请求播放 → 后端验证 enrollment 权限
4. 后端调用 DescribeMediaInfos → 获取原始 URL
5. 后端生成 Key 防盗链签名 URL → 返回给学生
6. TCPlayer 使用签名 URL 播放

---

## 3. API 设计

### 3.1 获取播放 URL

**端点**: `GET /api/v1/video/playback-url/{videoId}`

**认证**: 需要学生登录（通过 JWT）

**响应**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "playbackUrl": "https://example.vod2.myqcloud.com/xxx.mp4?t=xxx&sign=xxx",
    "duration": 3600,
    "coverImage": "https://example.vod2.myqcloud.com/cover.jpg"
  }
}
```

**错误响应**:
- 401: 未登录
- 403: 未购买该课程
- 404: 视频不存在

### 3.2 后端生成签名 URL 逻辑

```
1. 根据 videoId 调用 DescribeMediaInfos 获取 MediaUrl
2. 构造签名参数：
   - t = 当前时间 + 过期时间（秒转十六进制）
   - rand = 随机数
   - dir = URL 路径目录部分
   - sign = MD5(KEY + dir + t + rand)
3. 拼接签名 URL
4. 返回 { playbackUrl, duration, coverImage }
```

---

## 4. 后端实现

### 4.1 VideoService 改造

**新增方法**:
```java
public VideoPlaybackResponse getPlaybackUrl(String videoId, Long userId);
```

**VideoPlaybackResponse**:
```java
public record VideoPlaybackResponse(
    String playbackUrl,
    Integer duration,
    String coverImage
) {}
```

### 4.2 TencentVodProvider 改造

**新增方法**:
```java
@Override
public VideoPlaybackResponse getPlaybackUrlWithSign(String videoId);
```

**签名算法**:
```
signStr = KEY + dir + t + rand
sign = MD5(signStr)
signedUrl = originalUrl + "?t=" + t + "&rand=" + rand + "&sign=" + sign
```

### 4.3 权限校验

在 VideoService.getPlaybackUrl 中：
1. 根据 videoId 查询 lesson（需确定 lesson 与 videoId 的关联方式）
2. 查询 enrollment 验证用户是否有权观看
3. 如无权限抛出 BusinessRuleException(ACCESS_DENIED)

**注意**: 当前 LessonEntity 只有 videoId 字段，需通过 videoId 反查 lesson 再查 course。最终通过 enrollment 表验证用户是否有权。

---

## 5. 前端实现

### 5.1 VideoPlayer 组件改造

**支持两种模式**:
1. **videoUrl 模式**: 直接传入播放 URL（原有用法）
2. **videoId 模式**: 传入 videoId，组件内部调用 API 获取签名 URL

**Props 扩展**:
```typescript
interface VideoPlayerProps {
  videoUrl?: string      // 直接播放 URL
  videoId?: string       // VOD videoId，自动获取签名 URL
  courseId?: number      // 用于权限校验
  lessonId?: number      // 用于权限校验
  // ... 其他现有 props
}
```

**videoId 模式流程**:
1. 组件 mounted 时调用 `studentApi.getPlaybackUrl(videoId)`
2. 获取签名 playbackUrl
3. 使用 TCPlayer 播放

### 5.2 TCPlayer 集成

**CDN 引入**:
```html
<link href="//imgcache.qq.com/open/qcloud/video/tcplayer/tcplayer.css" rel="stylesheet">
<script src="//imgcache.qq.com/open/qcloud/video/tcplayer/libs/hls.min.0.12.4.js"></script>
<script src="//imgcache.qq.com/open/qcloud/video/tcplayer/tcplayer.min.js"></script>
```

**初始化**:
```javascript
const player = TCPlayer('player-container', {
  sources: [{
    src: playbackUrl,
    type: 'video/mp4'
  }],
  licenseUrl: 'https://license.vod.qcloud.com/path/to/license'
});
```

### 5.3 API 调用

**studentApi 扩展**:
```typescript
export const studentApi = {
  async getPlaybackUrl(videoId: string): Promise<{
    playbackUrl: string
    duration: number
    coverImage?: string
  }> {
    const { data } = await api.get(`/video/playback-url/${videoId}`)
    return data
  }
}
```

---

## 6. 视频状态

| 状态 | 说明 | 播放器行为 |
|------|------|-----------|
| PROCESSING | 视频上传后处理中 | 显示"视频处理中"提示 |
| READY | 视频可播放 | 正常显示播放器 |
| FAILED | 视频处理失败 | 显示"视频不可用"错误 |

---

## 7. 安全考虑

### 7.1 Key 防盗链

- 签名 URL 有效期: 2 小时（平衡安全与体验）
- KEY 存储在后端配置中，不暴露到前端
- 使用 HTTPS 传输播放 URL

### 7.2 播放权限

- 后端验证用户 enrollment 状态
- 验证用户是否已购买/兑换课程
- 验证 enrollment 未过期

---

## 8. 文件变更清单

### 后端
| 文件 | 变更 |
|------|------|
| `VideoService.java` | 新增 getPlaybackUrl 方法 |
| `VideoController.java` | 新增 GET /video/playback-url/{videoId} |
| `TencentVodProvider.java` | 实现 Key 签名 URL 生成 |
| `VideoPlaybackResponse.java` | 新增响应 DTO |
| `application.properties` | 添加签名过期配置 |

### 前端
| 文件 | 变更 |
|------|------|
| `VideoPlayer.vue` | 接入 TCPlayer，支持 videoId 模式 |
| `studentApi.ts` | 新增 getPlaybackUrl 方法 |
| `index.html` | 引入 TCPlayer CSS/JS |

---

## 9. 依赖配置

### 后端配置
```properties
# 视频播放
video.provider=tencent
tencent.vod.secret-id=${TENCENT_VOD_SECRET_ID}
tencent.vod.secret-key=${TENCENT_VOD_SECRET_KEY}
tencent.vod.app-id=${TENCENT_VOD_APP_ID}
# Key 防盗链签名有效期（秒）
tencent.vod.signature-expire=7200
```

### 前端环境变量
```env
VITE_TENCENT_VOD_LICENSE_URL=https://license.vod.qcloud.com/xxx
```

---

## 10. 文档链接

| 资源 | 链接 |
|------|------|
| TCPlayer 集成指南 | https://cloud.tencent.com/document/product/881/77877 |
| Key 防盗链 | https://cloud.tencent.com/document/product/266/14047 |
| DescribeMediaInfos API | https://cloud.tencent.com/document/product/266/78378 |
| 防盗链概述 | https://cloud.tencent.com/document/product/266/11243 |
