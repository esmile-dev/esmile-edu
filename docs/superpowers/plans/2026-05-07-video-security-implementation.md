# 视频安全播放实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现标准视频安全方案 - TCPlayer + 动态水印 + 签名 URL + 权限验证

**Architecture:** 后端 API 验证权限后返回签名 URL，前端 TCPlayer 渲染带动态水印的视频播放

**Tech Stack:** Spring Boot (Java), Vue 3 + TCPlayer, Tencent Cloud VOD

---

## 文件变更概览

| 文件 | 变更类型 | 说明 |
|------|----------|------|
| `backend/.../VideoPlaybackResponse.java` | 修改 | 添加 watermarkText 字段 |
| `backend/.../VideoService.java` | 修改 | 添加 getWatermarkText 方法 |
| `backend/.../VideoController.java` | 修改 | 添加 @RequireAuth 注解 |
| `backend/.../CourseBizService.java` | 修改 | 移除 getCourseDetail 中的 videoUrl |
| `backend/.../UserRepository.java` | 修改 | 添加 findById 方法 |
| `backend/.../application-dev.properties` | 修改 | signature-expire=3600 |
| `frontend/.../VideoPlayer.vue` | 重构 | 实现 TCPlayer + 动态水印 |
| `frontend/.../studentApi.ts` | 修改 | 添加 getPlaybackUrl 方法 |
| `frontend/.../LearnView.vue` | 修改 | 使用新播放流程 |

---

## 后端实现

### Task 1: VideoPlaybackResponse 添加 watermarkText 字段

**Files:**
- Modify: `backend/src/main/java/com/esmile/edu/dto/response/VideoPlaybackResponse.java`

- [ ] **Step 1: 读取当前文件内容**

Run: `cat backend/src/main/java/com/esmile/edu/dto/response/VideoPlaybackResponse.java`

- [ ] **Step 2: 修改 record 添加 watermarkText 字段**

```java
public record VideoPlaybackResponse(
    String playbackUrl,
    Integer duration,
    String coverImage,
    String watermarkText  // 新增字段
) {}
```

- [ ] **Step 3: 编译验证**

Run: `cd backend && mvn compile -q`
Expected: 编译成功，无错误

- [ ] **Step 4: 提交**

```bash
git add backend/src/main/java/com/esmile/edu/dto/response/VideoPlaybackResponse.java
git commit -m "feat(video): add watermarkText field to VideoPlaybackResponse"
```

---

### Task 2: VideoService 添加 getWatermarkText 方法

**Files:**
- Modify: `backend/src/main/java/com/esmile/edu/biz/VideoService.java`
- Modify: `backend/src/main/java/com/esmile/edu/module/user/UserRepository.java`

- [ ] **Step 1: 检查 UserRepository 是否有 findById**

Run: `grep -n "findById" backend/src/main/java/com/esmile/edu/module/user/UserRepository.java`

- [ ] **Step 2: 如果没有 findById，添加到 UserRepository**

```java
Optional<UserEntity> findById(Long id);
```

- [ ] **Step 3: 在 VideoService 中添加 getWatermarkText 方法**

```java
public String getWatermarkText(Long userId) {
    UserEntity user = userRepository.findById(userId)
        .orElseThrow(() -> new EntityNotFoundException("User not found"));
    return user.getEmail();
}
```

- [ ] **Step 4: 修改 getPlaybackUrl(videoId, userId) 返回带水印的响应**

```java
public VideoPlaybackResponse getPlaybackUrl(String videoId, Long userId) {
    // ... 现有权限检查代码保持不变 ...

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

- [ ] **Step 5: 添加 UserEntity 导入**

```java
import com.esmile.edu.module.user.UserEntity;
```

- [ ] **Step 6: 编译验证**

Run: `cd backend && mvn compile -q`
Expected: 编译成功，无错误

- [ ] **Step 7: 提交**

```bash
git add backend/src/main/java/com/esmile/edu/biz/VideoService.java backend/src/main/java/com/esmile/edu/module/user/UserRepository.java
git commit -m "feat(video): add getWatermarkText and include watermark in playback response"
```

---

### Task 3: VideoController 添加 @RequireAuth

**Files:**
- Modify: `backend/src/main/java/com/esmile/edu/api/video/VideoController.java`

- [ ] **Step 1: 读取当前 VideoController 内容**

Run: `cat backend/src/main/java/com/esmile/edu/api/video/VideoController.java`

- [ ] **Step 2: 确认 getPlaybackUrl 方法当前没有 @RequireAuth**

预期：无 @RequireAuth 注解

- [ ] **Step 3: 添加 @RequireAuth 注解到 getPlaybackUrl 方法**

```java
@GetMapping("/video/playback-url/{videoId}")
@RequiredAuth  // 新增：要求登录
public ApiResponse<VideoPlaybackResponse> getPlaybackUrl(@PathVariable String videoId) {
    Long userId = AuthContext.getCurrentUserId();
    VideoPlaybackResponse response = videoService.getPlaybackUrl(videoId, userId);
    return ApiResponse.ok(response);
}
```

- [ ] **Step 4: 确保导入了 @RequireAuth**

```java
import com.esmile.edu.common.auth.RequireAuth;
```

- [ ] **Step 5: 编译验证**

Run: `cd backend && mvn compile -q`
Expected: 编译成功，无错误

- [ ] **Step 6: 提交**

```bash
git add backend/src/main/java/com/esmile/edu/api/video/VideoController.java
git commit -m "feat(video): add @RequireAuth to playback-url endpoint"
```

---

### Task 4: CourseBizService 移除 getCourseDetail 中的 videoUrl

**Files:**
- Modify: `backend/src/main/java/com/esmile/edu/biz/CourseBizService.java`

- [ ] **Step 1: 读取 getCourseDetail 方法**

Run: `grep -A 30 "public CourseDetailResponse getCourseDetail" backend/src/main/java/com/esmile/edu/biz/CourseBizService.java`

- [ ] **Step 2: 修改 lesson 映射，不再获取 videoUrl**

找到：
```java
List<LessonResponse> lessonResponses = lessons.stream()
    .map(lesson -> {
        String videoUrl = null;
        if (lesson.getVideoId() != null && !lesson.getVideoId().isBlank()) {
            try {
                VideoPlaybackResponse playback = videoService.getPlaybackUrlWithSign(lesson.getVideoId());
                videoUrl = playback.playbackUrl();
            } catch (Exception e) {
                log.warn("Failed to get playback URL for videoId {}: {}", lesson.getVideoId(), e.getMessage());
            }
        }
        return LessonResponse.from(lesson, videoUrl);
    })
    .toList();
```

修改为：
```java
List<LessonResponse> lessonResponses = lessons.stream()
    .map(lesson -> {
        // 不再获取 videoUrl，前端通过单独 API 获取签名 URL
        return LessonResponse.from(lesson, null);
    })
    .toList();
```

- [ ] **Step 3: 移除不再需要的 VideoPlaybackResponse 导入**

检查并移除：`import com.esmile.edu.dto.response.VideoPlaybackResponse;`

- [ ] **Step 4: 编译验证**

Run: `cd backend && mvn compile -q`
Expected: 编译成功，无错误

- [ ] **Step 5: 提交**

```bash
git add backend/src/main/java/com/esmile/edu/biz/CourseBizService.java
git commit -m "feat(video): remove videoUrl from getCourseDetail for security"
```

---

### Task 5: 更新签名过期时间配置

**Files:**
- Modify: `backend/src/main/resources/application-dev.properties`

- [ ] **Step 1: 读取当前配置**

Run: `grep "signature-expire" backend/src/main/resources/application-dev.properties`

- [ ] **Step 2: 修改过期时间为 3600 秒（1小时）**

```properties
tencent.vod.signature-expire=3600
```

- [ ] **Step 3: 提交**

```bash
git add backend/src/main/resources/application-dev.properties
git commit -m "chore: set video signature expire to 1 hour (3600s)"
```

---

## 前端实现

### Task 6: StudentAPI 添加 getPlaybackUrl 方法

**Files:**
- Modify: `frontend/src/student/api/studentApi.ts`

- [ ] **Step 1: 读取当前 studentApi.ts**

Run: `cat frontend/src/student/api/studentApi.ts`

- [ ] **Step 2: 在 VideoUploadSignature 后添加新类型**

```typescript
export interface VideoPlaybackData {
  playbackUrl: string
  duration: number | null
  watermarkText: string | null
}
```

- [ ] **Step 3: 添加 getPlaybackUrl 方法**

```typescript
async getPlaybackUrl(videoId: string): Promise<VideoPlaybackData> {
  const response = await api.get<ApiResponse<VideoPlaybackData>>(`/video/playback-url/${videoId}`)
  if (response.data.code !== 200) {
    throw new Error(response.data.message || 'Failed to get playback URL')
  }
  return response.data.data!
}
```

- [ ] **Step 4: 编译类型检查**

Run: `cd frontend && npx vue-tsc --noEmit 2>&1 | head -30`
Expected: 无类型错误

- [ ] **Step 5: 提交**

```bash
git add frontend/src/student/api/studentApi.ts
git commit -m "feat(student): add getPlaybackUrl API method"
```

---

### Task 7: VideoPlayer 重构 - 实现 TCPlayer + 动态水印

**Files:**
- Modify: `frontend/src/common/components/VideoPlayer.vue`

- [ ] **Step 1: 读取当前 VideoPlayer.vue**

Run: `cat frontend/src/common/components/VideoPlayer.vue`

- [ ] **Step 2: 添加 props: playbackUrl, watermarkText, appId**

```typescript
export interface VideoPlayerProps {
  videoUrl?: string
  videoId?: string
  poster?: string
  autoplay?: boolean
  muted?: boolean
  controls?: boolean
  provider?: VideoProvider | 'auto'
  startTime?: number
  playbackUrl?: string  // 新增
  watermarkText?: string  // 新增
  appId?: string  // 新增 - TCPlayer appId
}
```

- [ ] **Step 3: 修改 videoSrc computed**

```typescript
const videoSrc = computed(() => {
  if (playbackUrl.value) return playbackUrl.value
  if (props.playbackUrl) return props.playbackUrl
  if (props.videoUrl) return props.videoUrl
  return undefined
})
```

- [ ] **Step 4: 修改 effectiveProvider 逻辑**

```typescript
const effectiveProvider = computed<VideoProvider>(() => {
  if (props.provider !== 'auto') return props.provider
  // 如果有 videoId 和 playbackUrl，使用 tcplayer
  if (props.videoId && videoSrc.value) return 'tcplayer'
  // VOD MP4 直链用 native
  if (props.videoUrl?.includes('vod') && props.videoUrl?.includes('.mp4')) return 'native'
  return 'native'
})
```

- [ ] **Step 5: 实现 TCPlayer 初始化**

```typescript
function initTCPlayer() {
  if (!videoRef.value || !props.videoId) return

  // 销毁已有实例
  if (playerInstance) {
    playerInstance.dispose()
    playerInstance = null
  }

  const options: any = {
    appID: props.appId || 1408936978,  // 使用配置的 appId
    fileID: props.videoId,
    sources: videoSrc.value ? [{ src: videoSrc.value, type: 'video/mp4' }] : undefined,
    autoplay: props.autoplay,
    muted: props.muted,
    controls: props.controls,
    poster: props.poster,
    plugins: {
      DynamicWatermark: {
        type: 'text',
        content: props.watermarkText || '',
        speed: 0.5,
        opacity: 0.7,
        fontSize: 16,
        color: '#ffffff',
        position: 'left'
      }
    }
  }

  playerInstance = TCPlayer(videoRef.value, options)

  playerInstance.on('ready', () => {
    isReady.value = true
    emit('ready')
  })

  playerInstance.on('play', handlePlay)
  playerInstance.on('pause', handlePause)
  playerInstance.on('ended', handleEnded)
  playerInstance.on('timeupdate', handleTimeUpdate)
  playerInstance.on('error', handleError)
}
```

- [ ] **Step 6: 添加 watch 监听 videoId 变化**

```typescript
watch(() => [props.videoId, videoSrc.value], () => {
  if (effectiveProvider.value === 'tcplayer' && props.videoId) {
    nextTick(initTCPlayer)
  }
}, { immediate: true })
```

- [ ] **Step 7: 移除旧的 videoId watch 中的 API 调用逻辑**（因为现在由父组件传入 playbackUrl）

- [ ] **Step 8: 编译类型检查**

Run: `cd frontend && npx vue-tsc --noEmit 2>&1 | head -30`
Expected: 无类型错误

- [ ] **Step 9: 提交**

```bash
git add frontend/src/common/components/VideoPlayer.vue
git commit -m "feat(player): implement TCPlayer with dynamic watermark"
```

---

### Task 8: LearnView 修改播放流程

**Files:**
- Modify: `frontend/src/student/views/learn/LearnView.vue`

- [ ] **Step 1: 读取当前 LearnView.vue**

Run: `cat frontend/src/student/views/learn/LearnView.vue`

- [ ] **Step 2: 添加 playbackUrl, watermarkText, appId refs**

```typescript
const playbackUrl = ref<string | null>(null)
const watermarkText = ref<string | null>(null)
const appId = ref<number>(1408936978)  // 从配置获取
```

- [ ] **Step 3: 修改 loadVideo 函数**

```typescript
async function loadVideo() {
  if (!currentLesson.value?.videoId) return

  try {
    const response = await studentApi.getPlaybackUrl(currentLesson.value.videoId)
    playbackUrl.value = response.playbackUrl
    watermarkText.value = response.watermarkText
    videoError.value = ''
  } catch (err: any) {
    videoError.value = err.message || '无法加载视频，请确认已购买该课程'
    playbackUrl.value = null
    watermarkText.value = null
  }
}
```

- [ ] **Step 4: 修改 onMounted 调用 loadVideo**

```typescript
onMounted(async () => {
  try {
    course.value = await studentApi.getCourseDetail(courseId)
    // ... 现有逻辑 ...

    // 加载视频
    await loadVideo()
  } catch (err: any) {
    error.value = err.message || '加载失败'
  } finally {
    loading.value = false
  }
})
```

- [ ] **Step 5: 修改 VideoPlayer 组件调用**

```vue
<VideoPlayer
  v-if="currentLesson?.videoId && playbackUrl"
  :video-id="currentLesson.videoId"
  :playback-url="playbackUrl"
  :watermark-text="watermarkText"
  :app-id="appId"
  @error="handleVideoError"
/>
```

- [ ] **Step 6: 添加 watch 监听 lesson 变化**

```typescript
watch(() => currentLesson.value?.id, async (newLessonId) => {
  if (newLessonId) {
    await loadVideo()
  }
})
```

- [ ] **Step 7: 编译类型检查**

Run: `cd frontend && npx vue-tsc --noEmit 2>&1 | head -30`
Expected: 无类型错误

- [ ] **Step 8: 提交**

```bash
git add frontend/src/student/views/learn/LearnView.vue
git commit -m "feat(learn): use new playback flow with signed URL"
```

---

## 最终验证

### Task 9: 集成测试

- [ ] **Step 1: 启动后端**

Run: `cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=dev 2>&1 &`
Expected: 服务启动在端口 10328

- [ ] **Step 2: 启动前端**

Run: `cd frontend && npm run dev 2>&1 &`
Expected: Vite 服务启动在端口 10420

- [ ] **Step 3: 测试 API - 未登录访问**

Run: `curl -s http://localhost:10328/api/v1/video/playback-url/5145403725695231445`
Expected: `{"code":401,"message":"Authentication required",...}`

- [ ] **Step 4: 测试课程详情无 videoUrl**

Run: `curl -s http://localhost:10328/api/v1/student/courses/1 | grep videoUrl`
Expected: 无 videoUrl 字段

- [ ] **Step 5: 提交所有更改**

```bash
git add -A
git commit -m "feat: implement video security (TCPlayer + dynamic watermark + signed URL)

- Backend: add @RequireAuth, watermarkText, remove videoUrl from course detail
- Frontend: implement TCPlayer with dynamic watermark
- Config: signature expire 1 hour

Co-Authored-By: Claude <noreply@anthropic.com>"
```

---

## 部署清单确认

- [ ] 后端：VideoPlaybackResponse 添加 watermarkText ✓
- [ ] 后端：VideoService 添加 getWatermarkText ✓
- [ ] 后端：VideoController 添加 @RequireAuth ✓
- [ ] 后端：CourseBizService 移除 videoUrl ✓
- [ ] 前端：VideoPlayer 实现 TCPlayer + 动态水印 ✓
- [ ] 前端：LearnView 使用新播放流程 ✓
- [ ] 配置：signature-expire=3600 ✓
