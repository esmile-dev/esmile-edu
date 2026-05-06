# 腾讯云 VOD 视频播放实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现视频播放功能 - 后端生成 Key 防盗链签名 URL，前端使用 TCPlayer 播放

**Architecture:** 后端按需生成签名播放 URL 返回给前端，前端 TCPlayer 直接播放签名 URL。权限校验在后端通过 enrollment 验证。

**Tech Stack:** Spring Boot 3.x (Java), TCPlayer SDK, Tencent Cloud VOD API

---

## 文件变更概览

### 后端变更
| 文件 | 变更类型 |
|------|----------|
| `backend/src/main/java/com/esmile/edu/dto/response/VideoPlaybackResponse.java` | 新建 |
| `backend/src/main/java/com/esmile/edu/common/video/TencentVodProvider.java` | 修改 |
| `backend/src/main/java/com/esmile/edu/biz/VideoService.java` | 修改 |
| `backend/src/main/java/com/esmile/edu/api/video/VideoController.java` | 修改 |
| `backend/src/main/java/com/esmile/edu/module/course/LessonRepository.java` | 修改 |
| `backend/src/main/java/com/esmile/edu/common/exception/BusinessRuleException.java` | 修改 |
| `backend/src/main/resources/application.properties` | 修改 |

### 前端变更
| 文件 | 变更类型 |
|------|----------|
| `frontend/index.html` | 新建 |
| `frontend/src/common/components/VideoPlayer.vue` | 修改 |
| `frontend/src/student/api/studentApi.ts` | 修改 |
| `frontend/src/common/types/api.ts` | 修改 |

---

## Task 1: 后端 - VideoPlaybackResponse DTO

**Files:**
- Create: `backend/src/main/java/com/esmile/edu/dto/response/VideoPlaybackResponse.java`
- Test: `backend/src/test/java/com/esmile/edu/dto/response/VideoPlaybackResponseTest.java`

- [ ] **Step 1: 创建 VideoPlaybackResponse DTO**

```java
package com.esmile.edu.dto.response;

/**
 * Video playback URL response with signed URL for TCPlayer.
 *
 * @param playbackUrl Signed playback URL with Key anti-hotlinking
 * @param duration Video duration in seconds
 * @param coverImage Cover image URL (optional)
 */
public record VideoPlaybackResponse(
    String playbackUrl,
    Integer duration,
    String coverImage
) {}
```

- [ ] **Step 2: 创建单元测试**

```java
package com.esmile.edu.dto.response;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VideoPlaybackResponseTest {

    @Test
    void constructor_shouldSetAllFields() {
        var response = new VideoPlaybackResponse(
            "https://example.vod.com/video.mp4?t=abc&sign=xyz",
            3600,
            "https://example.vod.com/cover.jpg"
        );

        assertEquals("https://example.vod.com/video.mp4?t=abc&sign=xyz", response.playbackUrl());
        assertEquals(3600, response.duration());
        assertEquals("https://example.vod.com/cover.jpg", response.coverImage());
    }

    @Test
    void constructor_coverImageCanBeNull() {
        var response = new VideoPlaybackResponse(
            "https://example.vod.com/video.mp4",
            1800,
            null
        );

        assertNull(response.coverImage());
    }
}
```

- [ ] **Step 3: 运行测试验证**

Run: `cd backend && ./mvnw test -Dtest=VideoPlaybackResponseTest -q`
Expected: PASS

- [ ] **Step 4: 提交**

```bash
git add backend/src/main/java/com/esmile/edu/dto/response/VideoPlaybackResponse.java backend/src/test/java/com/esmile/edu/dto/response/VideoPlaybackResponseTest.java
git commit -m "feat(video): add VideoPlaybackResponse DTO

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

## Task 2: 后端 - LessonRepository 添加查询方法

**Files:**
- Modify: `backend/src/main/java/com/esmile/edu/module/course/LessonRepository.java`
- Test: `backend/src/test/java/com/esmile/edu/module/course/LessonRepositoryTest.java`

- [ ] **Step 1: 添加 findByVideoId 方法**

在 `LessonRepository` 接口中添加：
```java
Optional<LessonEntity> findByVideoId(String videoId);
```

- [ ] **Step 2: 提交**

```bash
git add backend/src/main/java/com/esmile/edu/module/course/LessonRepository.java
git commit -m "feat(course): add findByVideoId to LessonRepository

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

## Task 3: 后端 - TencentVodProvider 实现签名 URL 生成

**Files:**
- Modify: `backend/src/main/java/com/esmile/edu/common/video/TencentVodProvider.java`
- Test: `backend/src/test/java/com/esmile/edu/common/video/TencentVodProviderTest.java`

- [ ] **Step 1: 添加签名过期时间配置**

在类中添加配置字段：
```java
@Value("${tencent.vod.signature-expire:7200}")
private int signatureExpireSeconds;
```

- [ ] **Step 2: 添加 getPlaybackUrlWithSign 方法**

在 `TencentVodProvider` 类中添加方法：
```java
@Override
public VideoPlaybackResponse getPlaybackUrlWithSign(String videoId) {
    if (vodClient == null) {
        throw new VideoUploadFailedException("Tencent VOD credentials not configured");
    }

    try {
        DescribeMediaInfosRequest request = new DescribeMediaInfosRequest();
        request.setFileIds(new String[]{ videoId });

        DescribeMediaInfosResponse response = vodClient.DescribeMediaInfos(request);

        if (response.getMediaInfoSet() == null || response.getMediaInfoSet().length == 0) {
            throw new VideoUploadFailedException("Video not found: " + videoId);
        }

        var mediaInfo = response.getMediaInfoSet()[0];
        var basicInfo = mediaInfo.getBasicInfo();

        // Get original playback URL
        String originalUrl = getMediaUrl(mediaInfo);
        if (originalUrl == null || originalUrl.isBlank()) {
            throw new VideoUploadFailedException("Playback URL not available for video: " + videoId);
        }

        // Generate signed URL with Key anti-hotlinking
        String signedUrl = generateSignedUrl(originalUrl);

        // Get duration from metadata
        Integer duration = getDuration(mediaInfo);

        // Get cover image
        String coverImage = basicInfo.getCoverUrl();

        return new VideoPlaybackResponse(signedUrl, duration, coverImage);

    } catch (VideoUploadFailedException e) {
        throw e;
    } catch (Exception e) {
        log.error("[TENCENT VOD] Failed to get playback URL with sign", e);
        throw new VideoUploadFailedException("Failed to get playback URL: " + e.getMessage());
    }
}
```

- [ ] **Step 3: 添加辅助方法**

在类中添加：
```java
private String getMediaUrl(Object mediaInfo) {
    try {
        java.lang.reflect.Method method = mediaInfo.getClass().getMethod("getMediaUrl");
        return (String) method.invoke(mediaInfo);
    } catch (Exception e) {
        log.debug("[TENCENT VOD] Could not get MediaUrl via reflection: {}", e.getMessage());
        return null;
    }
}

private Integer getDuration(Object mediaInfo) {
    try {
        java.lang.reflect.Method method = mediaInfo.getClass().getMethod("getMetaData");
        Object metaData = method.invoke(mediaInfo);
        if (metaData != null) {
            java.lang.reflect.Method durationMethod = metaData.getClass().getMethod("getDuration");
            return ((Number) durationMethod.invoke(metaData)).intValue();
        }
        return null;
    } catch (Exception e) {
        log.debug("[TENCENT VOD] Could not get duration via reflection: {}", e.getMessage());
        return null;
    }
}

private String generateSignedUrl(String originalUrl) {
    try {
        java.net.URL url = new java.net.URL(originalUrl);
        String path = url.getPath();
        String dir = path.substring(0, path.lastIndexOf('/') + 1);

        long currentTime = System.currentTimeMillis() / 1000;
        long expireTime = currentTime + signatureExpireSeconds;
        String t = Long.toHexString(expireTime).toLowerCase();
        int rand = (int) (Math.random() * 999999);

        String signStr = secretKey + dir + t + rand;
        String sign = md5(signStr);

        String separator = originalUrl.contains("?") ? "&" : "?";
        return originalUrl + separator + "t=" + t + "&rand=" + rand + "&sign=" + sign;
    } catch (Exception e) {
        throw new VideoUploadFailedException("Failed to generate signed URL: " + e.getMessage());
    }
}

private String md5(String input) {
    try {
        java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
        byte[] hash = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    } catch (Exception e) {
        throw new VideoUploadFailedException("MD5 calculation failed", e);
    }
}
```

- [ ] **Step 4: 运行测试验证**（需要 Mock 环境的集成测试）

Run: `cd backend && ./mvnw compile -q`
Expected: SUCCESS (编译通过)

- [ ] **Step 5: 提交**

```bash
git add backend/src/main/java/com/esmile/edu/common/video/TencentVodProvider.java
git commit -m "feat(video): implement Key anti-hotlinking signed URL generation

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

## Task 4: 后端 - VideoService 添加播放 URL 获取方法

**Files:**
- Modify: `backend/src/main/java/com/esmile/edu/biz/VideoService.java`
- Test: `backend/src/test/java/com/esmile/edu/biz/VideoServiceTest.java`

- [ ] **Step 1: 添加依赖注入**

在 `VideoService` 类中添加：
```java
private final LessonRepository lessonRepository;
private final EnrollmentRepository enrollmentRepository;

public VideoService(
    VideoServiceFactory videoServiceFactory,
    LessonRepository lessonRepository,
    EnrollmentRepository enrollmentRepository
) {
    super(videoServiceFactory);
    this.lessonRepository = lessonRepository;
    this.enrollmentRepository = enrollmentRepository;
}
```

- [ ] **Step 2: 添加 getPlaybackUrl 方法**

在 `VideoService` 类中添加：
```java
public VideoPlaybackResponse getPlaybackUrl(String videoId, Long userId) {
    VideoStoragePort provider = videoServiceFactory.getVideoStorage();

    // Find lesson by videoId to get courseId for permission check
    LessonEntity lesson = lessonRepository.findByVideoId(videoId)
        .orElseThrow(() -> new EntityNotFoundException("Video not found: " + videoId));

    // Check user enrollment
    boolean hasEnrollment = enrollmentRepository.existsByUserIdAndCourseId(userId, lesson.getCourseId());
    if (!hasEnrollment) {
        throw new BusinessRuleException(
            BusinessRuleException.ACCESS_DENIED,
            "You do not have access to this video"
        );
    }

    // Check enrollment is active (not expired)
    var enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, lesson.getCourseId())
        .orElseThrow(() -> new BusinessRuleException(BusinessRuleException.ACCESS_DENIED, "Enrollment not found"));

    if (enrollment.getStatus() == EnrollmentStatus.EXPIRED) {
        throw new BusinessRuleException(
            BusinessRuleException.ACCESS_DENIED,
            "Your course access has expired"
        );
    }

    // Get signed playback URL from provider
    return provider.getPlaybackUrlWithSign(videoId);
}
```

- [ ] **Step 3: 提交**

```bash
git add backend/src/main/java/com/esmile/edu/biz/VideoService.java
git commit -m "feat(video): add getPlaybackUrl with enrollment permission check

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

## Task 5: 后端 - VideoController 添加播放 URL 端点

**Files:**
- Modify: `backend/src/main/java/com/esmile/edu/api/video/VideoController.java`

- [ ] **Step 1: 添加端点**

在 `VideoController` 类中添加：
```java
/**
 * Get signed playback URL for a video.
 * GET /video/playback-url/{videoId}
 */
@GetMapping("/video/playback-url/{videoId}")
public ApiResponse<VideoPlaybackResponse> getPlaybackUrl(@PathVariable String videoId) {
    VideoPlaybackResponse response = videoService.getPlaybackUrl(
        videoId,
        AuthContext.getCurrentUserId()
    );
    return ApiResponse.ok(response);
}
```

- [ ] **Step 2: 验证编译**

Run: `cd backend && ./mvnw compile -q`
Expected: SUCCESS

- [ ] **Step 3: 提交**

```bash
git add backend/src/main/java/com/esmile/edu/api/video/VideoController.java
git commit -m "feat(video): add GET /video/playback-url/{videoId} endpoint

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

## Task 6: 前端 - index.html 添加 TCPlayer CDN

**Files:**
- Create: `frontend/index.html`

- [ ] **Step 1: 创建 index.html**

```html
<!DOCTYPE html>
<html lang="zh-CN">
  <head>
    <meta charset="UTF-8" />
    <link rel="icon" type="image/svg+xml" href="/vite.svg" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>esmile edu</title>
    <!-- TCPlayer CSS -->
    <link href="//imgcache.qq.com/open/qcloud/video/tcplayer/tcplayer.css" rel="stylesheet">
  </head>
  <body>
    <div id="app"></div>
    <script type="module" src="/src/main.ts"></script>
    <!-- TCPlayer HLS support -->
    <script src="//imgcache.qq.com/open/qcloud/video/tcplayer/libs/hls.min.0.12.4.js"></script>
    <!-- TCPlayer -->
    <script src="//imgcache.qq.com/open/qcloud/video/tcplayer/tcplayer.min.js"></script>
  </body>
</html>
```

- [ ] **Step 2: 提交**

```bash
git add frontend/index.html
git commit -m "feat(frontend): add TCPlayer CDN dependencies to index.html

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

## Task 7: 前端 - VideoPlayer 支持 videoId 模式

**Files:**
- Modify: `frontend/src/common/components/VideoPlayer.vue`
- Test: `frontend/src/common/components/VideoPlayer.test.ts`

- [ ] **Step 1: 更新 Props 类型定义**

将 props 接口更新为：
```typescript
export interface VideoPlayerProps {
  /** Direct playback URL (takes precedence over videoId) */
  videoUrl?: string
  /** VOD video ID (requires backend to generate playback URL) */
  videoId?: string
  /** Poster image URL */
  poster?: string
  /** Autoplay on load */
  autoplay?: boolean
  /** Mute on load */
  muted?: boolean
  /** Show native controls */
  controls?: boolean
  /** Video provider type */
  provider?: 'native' | 'tcplayer' | 'auto'
  /** Initial playback time */
  startTime?: number
  /** Loading callback for external loading state */
  onLoading?: (loading: boolean) => void
  /** Error callback */
  onError?: (error: Error) => void
}
```

- [ ] **Step 2: 添加 videoId 模式处理逻辑**

添加计算属性：
```typescript
const effectiveProvider = computed<VideoPlayerProvider>(() => {
  if (props.provider !== 'auto') return props.provider
  if (props.videoUrl?.includes('vod') || props.videoId) return 'tcplayer'
  return 'native'
})
```

添加加载状态：
```typescript
const isLoading = ref(true)
const playbackUrl = ref<string | null>(null)

// When videoId is provided, fetch playback URL from backend
watch(() => props.videoId, async (newVideoId) => {
  if (!newVideoId) {
    playbackUrl.value = null
    return
  }

  isLoading.value = true
  props.onLoading?.(true)

  try {
    const response = await studentApi.getPlaybackUrl(newVideoId)
    playbackUrl.value = response.playbackUrl
    // Use backend-provided cover if available
    if (response.coverImage && !props.poster) {
      posterUrl.value = response.coverImage
    }
  } catch (error) {
    console.error('Failed to get playback URL:', error)
    props.onError?.(error as Error)
  } finally {
    isLoading.value = false
    props.onLoading?.(false)
  }
}, { immediate: true })
```

- [ ] **Step 3: 更新 TCPlayer 渲染逻辑**

替换 TCPlayer placeholder 为真实实现：
```vue
<div
  v-else-if="effectiveProvider === 'tcplayer'"
  class="w-full h-full"
>
  <video
    ref="tcplayerRef"
    class="video-js vjs-default-skin"
    playsinline
    webkit-playsinline
    x5-playsinline
  />
</div>
```

添加 videoId 时的数据源绑定：
```typescript
// In videoId mode, use fetched playbackUrl
const videoSrc = computed(() => {
  if (props.videoUrl) return props.videoUrl
  if (playbackUrl.value) return playbackUrl.value
  return undefined
})
```

- [ ] **Step 4: 提交**

```bash
git add frontend/src/common/components/VideoPlayer.vue
git commit -m "feat(frontend): support videoId mode with TCPlayer

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

## Task 8: 前端 - studentApi 添加 getPlaybackUrl

**Files:**
- Modify: `frontend/src/student/api/studentApi.ts`
- Modify: `frontend/src/common/types/api.ts`

- [ ] **Step 1: 添加类型定义**

在 `frontend/src/common/types/api.ts` 中添加：
```typescript
// ============== Video Types ==============

export interface VideoPlaybackResponse {
  playbackUrl: string
  duration: number
  coverImage?: string
}
```

- [ ] **Step 2: 添加 API 方法**

在 `studentApi` 对象中添加：
```typescript
async getPlaybackUrl(videoId: string): Promise<VideoPlaybackResponse> {
  return apiClient.get<VideoPlaybackResponse>(`/api/v1/video/playback-url/${videoId}`)
}
```

- [ ] **Step 3: 提交**

```bash
git add frontend/src/student/api/studentApi.ts frontend/src/common/types/api.ts
git commit -m "feat(frontend): add getPlaybackUrl to studentApi

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

## Task 9: 前端 - 使用 VideoPlayer videoId 模式

**Files:**
- Modify: `frontend/src/student/views/LessonPlayerView.vue` (或相关播放页面)

- [ ] **Step 1: 更新播放器使用方式**

在课时播放页面中：
```vue
<VideoPlayer
  v-if="lesson.videoId && lesson.status === 'READY'"
  :video-id="lesson.videoId"
  :start-time="lastPosition"
  @ended="handleVideoEnded"
/>
```

- [ ] **Step 2: 提交**

```bash
git add frontend/src/student/views/LessonPlayerView.vue
git commit -m "feat(frontend): use VideoPlayer with videoId mode

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

## Task 10: 后端 - 配置签名过期时间

**Files:**
- Modify: `backend/src/main/resources/application.properties`

- [ ] **Step 1: 添加配置**

```properties
# Video playback
# Key anti-hotlinking signature expiration in seconds (default: 2 hours)
tencent.vod.signature-expire=7200
```

- [ ] **Step 2: 提交**

```bash
git add backend/src/main/resources/application.properties
git commit -m "chore: add tencent.vod.signature-expire configuration

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

## 实施检查清单

- [ ] Task 1: VideoPlaybackResponse DTO
- [ ] Task 2: LessonRepository 查询方法
- [ ] Task 3: TencentVodProvider 签名 URL 生成
- [ ] Task 4: VideoService 播放 URL 获取（带权限校验）
- [ ] Task 5: VideoController 端点
- [ ] Task 6: 前端 index.html TCPlayer CDN
- [ ] Task 7: VideoPlayer videoId 模式
- [ ] Task 8: studentApi.getPlaybackUrl
- [ ] Task 9: 播放页面使用 videoId 模式
- [ ] Task 10: 配置签名过期时间

---

## 文档更新

完成实施后更新以下文档：
- `docs/backend/technical/6-infrastructure/video-player.md` - 更新 TCPlayer 集成说明
- `docs/specs/2026-05-02-esmile-edu-mvp-prd.md` - 标记视频播放已完成
