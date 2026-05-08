# 视频安全播放设计方案

**版本**: v1.0
**日期**: 2026-05-07
**状态**: 已批准

---

## 1. 概述

### 1.1 背景

当前视频播放存在以下安全风险：

| 问题 | 风险等级 | 说明 |
|------|----------|------|
| 课程详情公开返回签名 URL | **严重** | 任何人无需登录即可获取视频链接 |
| TCPlayer 未初始化 | **高** | 无法使用腾讯云播放器安全功能 |
| 无动态水印 | **高** | 无法追溯录屏行为 |
| 签名 URL 长期有效 | **中** | 2小时过期窗口可被滥用 |

### 1.2 目标

实现一个安全可控的视频播放方案，满足：

1. **防未授权访问** - 只有已购买课程的用户才能观看视频
2. **防录屏追溯** - 动态水印标识观看者身份
3. **防链接分享** - 短期签名 URL + 域名限制
4. **标准化播放** - 使用 TCPlayer 获得完整的安全能力

### 1.3 方案选择

| 级别 | 技术方案 | 当前状态 |
|------|----------|----------|
| **标准方案** | TCPlayer + 动态水印 + 签名 URL | 本次实现 |
| **高级方案** | HLS私有加密 + DRM | 预留（需 DRM 开通） |

---

## 2. 系统架构

### 2.1 整体架构

```
┌─────────────────────────────────────────────────────────────────────┐
│                           前端 (Vue 3 + TCPlayer)                   │
│  ┌─────────────┐    ┌──────────────┐    ┌─────────────────────┐    │
│  │ LearnView  │───▶│ VideoPlayer  │───▶│   TCPlayer          │    │
│  │ (课程学习页) │    │  (播放器组件) │    │  - 动态水印         │    │
│  └─────────────┘    └──────────────┘    │  - videoId 模式     │    │
│                                          │  - 安全检测          │    │
│                                          └─────────────────────┘    │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    │ HTTPS + JWT Token
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│                         后端 (Spring Boot)                          │
│  ┌─────────────────┐    ┌────────────────┐    ┌───────────────┐  │
│  │ VideoController │───▶│ VideoService   │───▶│ TencentVod   │  │
│  │ - 权限验证      │    │ - 报名检查     │    │ Provider     │  │
│  │ - @RequireAuth │    │ - 签名生成     │    │ - 签名URL    │  │
│  └─────────────────┘    └────────────────┘    └───────────────┘  │
│                                    │                                │
│                                    ▼                                │
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │                 CourseBizService.getCourseDetail()           │    │
│  │  - 移除 videoUrl 返回（不再暴露签名 URL）                    │    │
│  │  - 仅返回 videoId 和 videoId 供播放器使用                    │    │
│  └─────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    │ API Call
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│                         腾讯云 VOD                                  │
│  ┌─────────────────┐    ┌────────────────┐                      │
│  │ DescribeMedia    │◀───│ 签名验证        │                      │
│  │ Infos           │    │ (防盗链 Key)    │                      │
│  └─────────────────┘    └────────────────┘                      │
└─────────────────────────────────────────────────────────────────────┘
```

### 2.2 数据流

```
用户点击播放
    │
    ▼
┌─────────────────────────────────────────────────────────────────┐
│ Step 1: 前端请求签名 URL                                          │
│   GET /api/v1/video/playback-url/{videoId}                       │
│   Header: Authorization: Bearer <jwt_token>                       │
└─────────────────────────────────────────────────────────────────┘
    │
    ▼
┌─────────────────────────────────────────────────────────────────┐
│ Step 2: 后端权限验证                                             │
│   1. JWT Token 验证 → 获取 userId                                │
│   2. 查询 Enrollment（userId + courseId）                         │
│   3. 检查 enrollment.status == ACTIVE                             │
│   4. 检查 enrollment.expiresAt > now                             │
└─────────────────────────────────────────────────────────────────┘
    │
    ▼
┌─────────────────────────────────────────────────────────────────┐
│ Step 3: 生成签名 URL                                             │
│   1. 调用腾讯云 VOD API 获取媒体信息                             │
│   2. 使用防盗链 Key 生成签名 URL（1小时过期）                     │
│   3. 返回 { playbackUrl, duration, watermarkText }               │
└─────────────────────────────────────────────────────────────────┘
    │
    ▼
┌─────────────────────────────────────────────────────────────────┐
│ Step 4: TCPlayer 播放                                            │
│   1. 初始化 TCPlayer（videoId 模式）                            │
│   2. 配置动态水印（用户邮箱）                                    │
│   3. 使用签名 URL 播放                                           │
└─────────────────────────────────────────────────────────────────┘
```

---

## 3. API 设计

### 3.1 视频播放 URL 获取

**端点**: `GET /api/v1/video/playback-url/{videoId}`

| 属性 | 值 |
|------|-----|
| 认证 | **必须** (`@RequireAuth`) |
| 权限 | 需已购买对应课程 |

**请求头**:
```
Authorization: Bearer <jwt_token>
```

**响应** (成功 - 200):
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "playbackUrl": "https://xxx.vod2.myqcloud.com/.../video.mp4?t=xxx&sign=xxx",
    "duration": 1800,
    "watermarkText": "user@example.com"
  }
}
```

**响应** (未登录 - 401):
```json
{
  "code": 401,
  "message": "Authentication required",
  "data": null
}
```

**响应** (未购买课程 - 403):
```json
{
  "code": 40301,
  "message": "You do not have access to this video",
  "data": null
}
```

### 3.2 课程详情（修改）

**端点**: `GET /api/v1/student/courses/{courseId}`

**变更**: 移除 `lessons[].videoUrl` 字段

**修改前**:
```json
{
  "lessons": [
    { "id": 1, "title": "课时1", "videoId": "xxx", "videoUrl": "https://...", "status": "READY" }
  ]
}
```

**修改后**:
```json
{
  "lessons": [
    { "id": 1, "title": "课时1", "videoId": "xxx", "status": "READY" }
  ]
}
```

> **注意**: `videoUrl` 被移除，前端需要通过 `/video/playback-url/{videoId}` 单独获取签名 URL。

---

## 4. 后端实现

### 4.1 VideoController 变更

**文件**: `backend/src/main/java/com/esmile/edu/api/video/VideoController.java`

```java
@GetMapping("/video/playback-url/{videoId}")
@RequiredAuth  // 新增：要求登录
public ApiResponse<VideoPlaybackResponse> getPlaybackUrl(@PathVariable String videoId) {
    Long userId = AuthContext.getCurrentUserId();
    VideoPlaybackResponse response = videoService.getPlaybackUrl(videoId, userId);
    return ApiResponse.ok(response);
}
```

### 4.2 VideoService 变更

**文件**: `backend/src/main/java/com/esmile/edu/biz/VideoService.java`

新增方法 - 获取水印文本：

```java
public String getWatermarkText(Long userId) {
    UserEntity user = userRepository.findById(userId)
        .orElseThrow(() -> new EntityNotFoundException("User not found"));
    return user.getEmail();
}
```

修改 `getPlaybackUrl(videoId, userId)` 返回 `watermarkText`:

```java
public VideoPlaybackResponse getPlaybackUrl(String videoId, Long userId) {
    // ... 现有权限检查 ...

    // 获取水印文本
    String watermarkText = getWatermarkText(userId);

    // 获取签名 URL
    VideoPlaybackResponse response = provider.getPlaybackUrlWithSign(videoId);

    // 返回带水印文本的响应
    return new VideoPlaybackResponse(
        response.playbackUrl(),
        response.duration(),
        response.coverImage(),
        watermarkText
    );
}
```

### 4.3 VideoPlaybackResponse 变更

**文件**: `backend/src/main/java/com/esmile/edu/dto/response/VideoPlaybackResponse.java`

```java
public record VideoPlaybackResponse(
    String playbackUrl,
    Integer duration,
    String coverImage,
    String watermarkText  // 新增
) {}
```

### 4.4 CourseBizService 变更

**文件**: `backend/src/main/java/com/esmile/edu/biz/CourseBizService.java`

移除 `getCourseDetail()` 中的 videoUrl：

```java
@Transactional(readOnly = true)
public CourseDetailResponse getCourseDetail(Long courseId) {
    // ... 获取课程信息 ...

    List<ChapterResponse> chapterResponses = chapters.stream()
        .map(ch -> {
            List<LessonEntity> lessons = lessonRepository.findByChapterIdOrderByPosition(ch.getId());
            List<LessonResponse> lessonResponses = lessons.stream()
                .map(lesson -> {
                    // 不再获取 videoUrl，仅返回 videoId
                    return LessonResponse.from(lesson, null);
                })
                .toList();
            return ChapterResponse.from(ch, lessonResponses);
        })
        .toList();

    return CourseDetailResponse.from(course, chapterResponses);
}
```

### 4.5 签名 URL 过期时间

**配置**: `application-dev.properties`

```properties
# 视频签名 URL 过期时间（秒）
# 1小时 = 3600秒
tencent.vod.signature-expire=3600
```

---

## 5. 前端实现

### 5.1 VideoPlayer 组件重构

**文件**: `frontend/src/common/components/VideoPlayer.vue`

**核心变更**:

1. **TCPlayer 正确初始化** - 使用 videoId 模式而非直接 URL
2. **动态水印配置** - 显示用户邮箱
3. **获取签名 URL** - 通过 API 获取而非直接使用 videoUrl

**TCPlayer 初始化代码**:

```typescript
import TCPlayer from 'tcplayer'

const playerRef = ref<any>(null)

function initTCPlayer(videoId: string, playbackUrl: string, watermarkText: string) {
  if (playerRef.value) {
    playerRef.value.dispose()
  }

  playerRef.value = TCPlayer('tcplayer-container', {
    appID: '1500014561',  // 替换为实际 appId
    fileID: videoId,
    psign: '',  // 如需 player signature
    sources: [{ src: playbackUrl, type: 'video/mp4' }],
    plugins: {
      DynamicWatermark: {
        type: 'text',
        content: watermarkText,
        speed: 0.5,
        opacity: 0.7,
        fontSize: 16,
        color: '#ffffff',
        position: 'left'
      }
    }
  })
}
```

### 5.2 LearnView 变更

**文件**: `frontend/src/student/views/learn/LearnView.vue`

**变更逻辑**:

```typescript
// 之前的逻辑（有问题）
VideoPlayer(:video-url="currentLesson.videoUrl")

// 新的逻辑
async function loadVideo() {
  if (!currentLesson.value?.videoId) return

  try {
    const response = await studentApi.getPlaybackUrl(currentLesson.value.videoId)
    playbackUrl.value = response.playbackUrl
    watermarkText.value = response.watermarkText
  } catch (err) {
    videoError.value = '无法加载视频，请确认已购买该课程'
  }
}

VideoPlayer(
  :provider="'tcplayer'"
  :video-id="currentLesson.videoId"
  :playback-url="playbackUrl"
  :watermark-text="watermarkText"
)
```

### 5.3 StudentAPI 新增

**文件**: `frontend/src/student/api/studentApi.ts`

```typescript
async getPlaybackUrl(videoId: string): Promise<{
  playbackUrl: string
  duration: number
  watermarkText: string
}> {
  const response = await api.get<ApiResponse<any>>(`/video/playback-url/${videoId}`)
  if (response.data.code !== 200) {
    throw new Error(response.data.message)
  }
  return response.data.data
}
```

---

## 6. 腾讯云配置

### 6.1 防盗链配置

**位置**: 腾讯云控制台 → 应用管理 → 分发与播放设置 → 域名管理 → 访问控制

**配置项**:
- [x] 开启 Key 防盗链
- Key: `<生成的防盗链 Key>`
- 过期时间: 1小时（与 `tencent.vod.signature-expire` 保持一致）

### 6.2 Referer 白名单

**配置**:
```
允许的域名:
- localhost:* (开发环境)
- *.yourdomain.com (生产环境)
```

### 6.3 TCPlayer AppId

在 TCPlayer 初始化时需要配置正确的 `appID`，即腾讯云 VOD 的 `SubAppId`。

---

## 7. 安全考量

### 7.1 多层防护

| 层级 | 防护措施 | 说明 |
|------|----------|------|
| 认证层 | JWT Token | 验证用户身份有效 |
| 权限层 | 课程报名检查 | 确保已购买且未过期 |
| 传输层 | HTTPS | 加密传输防窃听 |
| 传输层 | 签名 URL | 1小时过期，绑定请求 |
| 域名层 | Referer 检查 | 仅允许平台域名播放 |
| 应用层 | 动态水印 | 追溯录屏行为 |

### 7.2 潜在风险与缓解

| 风险 | 缓解措施 |
|------|----------|
| 录屏仍可进行 | 动态水印可追溯来源 |
| 链接被分享 | 签名 URL 1小时过期 |
| 域名伪造 | Referer 检查 + 短期签名 |
| API 被滥用 | 需登录 + 报名验证 |

### 7.3 预留升级路径

**升级到 DRM 方案时**：

1. 开通腾讯云 DRM 功能
2. 配置 DRM 许可证
3. 使用 HLS 加密而非 MP4
4. TCPlayer 切换到 DRM 播放模式
5. 移除客户端生成的签名，改为服务端 psign

---

## 8. 测试计划

### 8.1 功能测试

| 用例 | 预期结果 |
|------|----------|
| 已登录 + 已购买 → 播放视频 | 正常播放，显示水印 |
| 已登录 + 未购买 → 播放视频 | 返回 403 错误 |
| 未登录 → 播放视频 | 返回 401 错误 |
| 签名 URL 过期后访问 | 播放失败（CDN 拒绝） |
| 录屏视频可识别水印 | 水印清晰可读 |

### 8.2 安全测试

| 用例 | 预期结果 |
|------|----------|
| 直接访问视频 URL（未签名） | CDN 拒绝访问 |
| 从其他域名 Referer 访问 | CDN 拒绝访问 |
| 移除水印的播放器 | TCPlayer 检测并停止播放 |

---

## 9. 部署清单

- [ ] 后端：更新 VideoPlaybackResponse 添加 watermarkText
- [ ] 后端：VideoService 添加 getWatermarkText 方法
- [ ] 后端：VideoController 添加 @RequireAuth
- [ ] 后端：CourseBizService 移除 videoUrl 返回
- [ ] 前端：VideoPlayer 实现真正的 TCPlayer 初始化
- [ ] 前端：VideoPlayer 配置动态水印
- [ ] 前端：LearnView 改为通过 API 获取签名 URL
- [ ] 配置：tencent.vod.signature-expire=3600
- [ ] 腾讯云：配置防盗链 Key 和过期时间
- [ ] 腾讯云：配置 Referer 白名单

---

## 10. 附录

### 10.1 相关文档

- [腾讯云 VOD 防盗链配置](https://cloud.tencent.com/document/product/266/33469)
- [TCPlayer 动态水印](https://cloud.tencent.com/document/product/881/96701)
- [TCPlayer 安全检测](https://cloud.tencent.com/document/product/881/96701)
- [播放器签名 (psign)](https://cloud.tencent.com/document/product/266/45554)

### 10.2 名词解释

| 名词 | 说明 |
|------|------|
| TCPlayer | 腾讯云 Web 播放器 |
| 防盗链 | 基于 URL 签名的访问控制 |
| 动态水印 | 播放器实时渲染的水印 |
| DRM | 数字版权管理 |
| SubAppId | 腾讯云 VOD 子应用 ID |
