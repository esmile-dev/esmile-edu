<script setup lang="ts">
import { ref, computed, watch, nextTick, onUnmounted } from 'vue'

export type VideoProvider = 'native' | 'tcplayer'

export interface VideoPlayerEmits {
  ready: []
  play: []
  pause: []
  ended: []
  timeupdate: [currentTime: number]
  error: [error: Error]
}

export interface VideoPlayerProps {
  /** Direct playback URL (takes precedence over videoId) */
  videoUrl?: string
  /** VOD video ID (requires backend to generate playback URL) */
  videoId?: string
  /** Signed playback URL from backend (for tcplayer) */
  playbackUrl?: string
  /** Poster image URL */
  poster?: string
  /** Autoplay on load */
  autoplay?: boolean
  /** Mute on load */
  muted?: boolean
  /** Show native controls */
  controls?: boolean
  /** Video provider type */
  provider?: VideoProvider | 'auto'
  /** Initial playback time */
  startTime?: number
  /** Watermark text for dynamic watermark */
  watermarkText?: string
  /** TCPlayer appId */
  appId?: string | number
}

const props = withDefaults(defineProps<VideoPlayerProps>(), {
  autoplay: false,
  muted: false,
  controls: true,
  provider: 'auto',
  startTime: 0,
  appId: 1408936978
})

const emit = defineEmits<VideoPlayerEmits>()

const videoRef = ref<HTMLVideoElement | null>(null)
const isReady = ref(false)
const isPlaying = ref(false)
const currentTime = ref(0)
const duration = ref(0)
const error = ref<Error | null>(null)
const isLoading = ref(true)

let playerInstance: any = null

const effectiveProvider = computed<VideoProvider>(() => {
  if (props.provider !== 'auto') return props.provider
  // If we have videoId and a source URL, use tcplayer for VOD
  if (props.videoId && videoSrc.value) return 'tcplayer'
  // Direct MP4 URLs use native
  if (props.videoUrl?.includes('.mp4')) return 'native'
  return 'native'
})

const videoSrc = computed(() => {
  if (props.playbackUrl) return props.playbackUrl
  if (props.videoUrl) return props.videoUrl
  return undefined
})

watch([() => effectiveProvider.value, () => props.videoId, videoSrc], ([provider, videoId, src]) => {
  if (provider === 'tcplayer' && videoId && src) {
    nextTick(initTCPlayer)
  }
}, { immediate: true })

onUnmounted(() => {
  if (playerInstance) {
    playerInstance.dispose()
    playerInstance = null
  }
})

function initTCPlayer() {
  if (!videoRef.value || !props.videoId) return

  // Check if TCPlayer is available
  if (typeof (window as any).TCPlayer !== 'function') {
    console.error('TCPlayer not loaded')
    error.value = new Error('Video player not available')
    isLoading.value = false
    return
  }

  // Destroy existing instance
  if (playerInstance) {
    playerInstance.dispose()
    playerInstance = null
  }

  const options: Record<string, any> = {
    appID: Number(props.appId) || 1408936978,
    fileID: props.videoId,
    sources: videoSrc.value ? [{ src: videoSrc.value, type: 'video/mp4' }] : undefined,
    autoplay: props.autoplay,
    muted: props.muted,
    controls: props.controls,
    poster: props.poster,
    volume: 0,
    plugins: {
      DynamicWatermark: {
        type: 'text',
        content: [props.watermarkText || ''],
        speed: 0.5,
        opacity: 0.7,
        fontSize: 16,
        color: '#ffffff',
        position: 'left'
      }
    }
  }

  playerInstance = (window as any).TCPlayer(videoRef.value, options)

  playerInstance.on('ready', () => {
    isReady.value = true
    isLoading.value = false
    emit('ready')
  })

  playerInstance.on('play', handlePlay)
  playerInstance.on('pause', handlePause)
  playerInstance.on('ended', handleEnded)
  playerInstance.on('timeupdate', () => {
    if (playerInstance) {
      currentTime.value = playerInstance.currentTime()
      emit('timeupdate', currentTime.value)
    }
  })
  playerInstance.on('error', (e: any) => {
    const err = new Error('Video playback error')
    error.value = err
    emit('error', err)
  })
}

function handleLoadedMetadata() {
  if (videoRef.value) {
    duration.value = videoRef.value.duration
    if (props.startTime > 0) {
      videoRef.value.currentTime = props.startTime
    }
    isReady.value = true
    emit('ready')
  }
}

function handlePlay() {
  isPlaying.value = true
  emit('play')
}

function handlePause() {
  isPlaying.value = false
  emit('pause')
}

function handleEnded() {
  isPlaying.value = false
  emit('ended')
}

function handleTimeUpdate() {
  if (videoRef.value) {
    currentTime.value = videoRef.value.currentTime
    emit('timeupdate', currentTime.value)
  }
}

function handleError(e: Event) {
  const target = e.target as HTMLVideoElement
  const err = new Error(target.error?.message || 'Video playback error')
  error.value = err
  emit('error', err)
}

function play() {
  if (playerInstance) {
    playerInstance.play()
  } else {
    videoRef.value?.play()
  }
}

function pause() {
  if (playerInstance) {
    playerInstance.pause()
  } else {
    videoRef.value?.pause()
  }
}

function seek(time: number) {
  if (playerInstance) {
    playerInstance.currentTime(time)
  } else if (videoRef.value) {
    videoRef.value.currentTime = time
  }
}

function getPlayerInstance() {
  return playerInstance || videoRef.value
}

defineExpose({
  play,
  pause,
  seek,
  getPlayerInstance,
  currentTime,
  duration,
  isReady,
  isPlaying
})
</script>

<template>
  <div class="video-player relative w-full bg-black">
    <video
      v-if="effectiveProvider === 'native'"
      ref="videoRef"
      class="w-full h-full object-contain"
      :src="videoSrc"
      :poster="poster"
      :autoplay="autoplay"
      :muted="muted"
      :controls="controls"
      @loadedmetadata="handleLoadedMetadata"
      @play="handlePlay"
      @pause="handlePause"
      @ended="handleEnded"
      @timeupdate="handleTimeUpdate"
      @error="handleError"
    />

    <!-- TCPlayer for VOD videos -->
    <div
      v-else-if="effectiveProvider === 'tcplayer'"
      id="tcplayer-container"
      class="w-full h-full"
    >
      <video
        ref="videoRef"
        class="video-js vjs-default-skin vjs-big-play-centered"
      />
    </div>

    <!-- Loading state -->
    <div
      v-if="isLoading || (!isReady && !error)"
      class="absolute inset-0 flex items-center justify-center bg-black/50"
    >
      <div class="w-12 h-12 border-4 border-white/30 border-t-white rounded-full animate-spin" />
    </div>

    <!-- Error state -->
    <div
      v-if="error"
      class="absolute inset-0 flex items-center justify-center bg-black/80"
    >
      <div class="text-center text-white">
        <p class="text-lg mb-2">播放失败</p>
        <p class="text-sm text-gray-400">{{ error.message }}</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.video-player {
  aspect-ratio: 16 / 9;
}
</style>
