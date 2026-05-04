# 基础设施 - 腾讯云 VOD 集成规范

---

## 1. 集成概述

### 1.1 腾讯云 VOD 功能

| 功能 | 说明 |
|------|------|
| 视频上传 | 直传 VOD，服务器仅存储 videoId |
| 视频转码 | 上传后自动转码为多种清晰度 |
| 视频播放 | 提供播放地址和防盗链 |
| 视频管理 | 查询、删除视频 |

### 1.2 集成架构

```
┌─────────────┐      ┌─────────────┐      ┌─────────────┐
│   Client    │ ───→ │  Backend    │ ───→ │  Tencent   │
│  (Browser)  │      │   Server    │      │    VOD      │
└─────────────┘      └─────────────┘      └─────────────┘
      │                    │                     │
      │  1. 申请上传签名    │                     │
      │───────────────────→│                     │
      │                    │                     │
      │  2. 上传凭证        │                     │
      │←───────────────────│                     │
      │                    │                     │
      │  3. 直传视频        │                     │
      │──────────────────────────────────────────→│
      │                    │                     │
      │  4. 转码完成回调    │                     │
      │←──────────────────────────────────────────│
      │                    │                     │
```

---

## 2. 配置项

### 2.1 环境变量

| 变量名 | 说明 |
|--------|------|
| `TENCENT_VOD_SECRET_ID` | 腾讯云 SecretId |
| `TENCENT_VOD_SECRET_KEY` | 腾讯云 SecretKey |
| `TENCENT_VOD_APP_ID` | 腾讯云 AppId |
| `TENCENT_VOD_REGION` | 区域（默认：ap-guangzhou） |

### 2.2 防盗链配置

| 配置项 | 说明 |
|--------|------|
| 播放域名 | 配置播放域名 |
| Referer 白名单 | 允许的来源域名 |
| URL 过期时间 | 播放地址有效期（建议 24 小时） |

---

## 3. 核心接口

### 3.1 申请上传签名

**方法**: `applyUpload(fileName, fileSize)`
**输入**: 文件名、文件大小
**输出**: videoId、上传签名、上传地址

**业务逻辑**:
1. 验证文件类型（mp4、mov 等）
2. 验证文件大小（上限 10GB）
3. 调用 VOD API 获取上传签名
4. 返回 videoId 和上传信息

### 3.2 确认上传完成

**方法**: `confirmUpload(videoId)`
**输入**: videoId
**输出**: 视频信息（播放地址、时长、状态）

**业务逻辑**:
1. 查询 VOD 视频状态
2. 若转码完成，更新 Lesson 记录
3. 若转码失败，标记 Lesson 状态为 FAILED

### 3.3 获取播放地址

**方法**: `getPlayUrl(videoId)`
**输入**: videoId
**输出**: 播放 URL

---

## 4. 防腐层设计

### 4.1 防腐层目的

- 隔离腾讯云 VOD 的具体实现
- 保护业务代码不受外部 API 变化影响
- 统一错误处理

### 4.2 防腐层接口

```java
public interface VideoStoragePort {
    VideoUploadSignature applyUpload(String fileName, long fileSize);
    VideoInfo confirmUpload(String videoId);
    String getPlayUrl(String videoId);
}
```

### 4.3 防腐层实现

```java
@Service
public class TencentVodAdapter implements VideoStoragePort {
    // 实现腾讯云 VOD SDK 调用
}
```

---

## 5. 视频状态流转

```
[上传中] → PROCESSING → [转码中]
                              ↓
                    ┌─────────┴─────────┐
                    ↓                   ↓
                [READY]             [FAILED]
```

| 状态 | 说明 |
|------|------|
| PROCESSING | 视频上传中或转码中 |
| READY | 视频已就绪，可播放 |
| FAILED | 视频处理失败 |

---

## 6. 回调通知

### 6.1 回调配置

在腾讯云 VOD 控制台配置回调 URL：
```
https://api.esmile.edu/api/v1/internal/video/callback
```

### 6.2 回调内容

| 事件 | 回调字段 |
|------|----------|
| 上传完成 | eventType, videoId, fileId |
| 转码完成 | eventType, videoId, url, duration |
| 转码失败 | eventType, videoId, errorCode |

---

## 7. 错误处理

### 7.1 常见错误

| 错误码 | 说明 | 处理 |
|--------|------|------|
| VOD001 | 上传签名失效 | 重新申请上传签名 |
| VOD002 | 文件类型不支持 | 返回错误给用户 |
| VOD003 | 文件大小超限 | 返回错误给用户 |
| VOD004 | 转码失败 | 标记课时状态为 FAILED |
| VOD005 | 视频不存在 | 返回错误给用户 |

### 7.2 重试策略

| 场景 | 重试次数 | 重试间隔 |
|------|----------|----------|
| 网络错误 | 3 | 1s, 2s, 4s |
| VOD API 限流 | 5 | 1s, 2s, 4s, 8s, 16s |

---

## 8. 约束

| 约束 | 说明 |
|------|------|
| 禁止上传大文件 | 视频直接上传到 VOD，服务器不存储 |
| 敏感信息 | SecretId/SecretKey 必须通过环境变量注入 |
| 防盗链 | 必须配置 Referer 白名单 |
| 回调验证 | 验证回调签名防止伪造 |
