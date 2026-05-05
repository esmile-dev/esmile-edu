<script setup lang="ts">
import { ref, computed } from 'vue'

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
}

const props = withDefaults(defineProps<VideoPlayerProps>(), {
  autoplay: false,
  muted: false,
  controls: true,
  provider: 'auto',
  startTime: 0
})

const emit = defineEmits<VideoPlayerEmits>()

const videoRef = ref<HTMLVideoElement | null>(null)
const isReady = ref(false)
const isPlaying = ref(false)
const currentTime = ref(0)
const duration = ref(0)
const error = ref<Error | null>(null)

const effectiveProvider = computed<VideoProvider>(() => {
  if (props.provider !== 'auto') return props.provider
  if (props.videoUrl?.includes('vod')) return 'tcplayer'
  return 'native'
})

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
  videoRef.value?.play()
}

function pause() {
  videoRef.value?.pause()
}

function seek(time: number) {
  if (videoRef.value) {
    videoRef.value.currentTime = time
  }
}

function getPlayerInstance() {
  return videoRef.value
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
      :src="videoUrl"
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

    <!-- TCPlayer placeholder - to be implemented later -->
    <div
      v-else-if="effectiveProvider === 'tcplayer'"
      class="w-full h-full flex items-center justify-center text-white"
    >
      <div class="text-center">
        <p class="text-lg mb-2">TCPlayer integration pending</p>
        <p class="text-sm text-gray-400">
          {{ videoId ? `Video ID: ${videoId}` : 'No videoId provided' }}
        </p>
      </div>
    </div>

    <!-- Loading state -->
    <div
      v-if="!isReady && !error"
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
