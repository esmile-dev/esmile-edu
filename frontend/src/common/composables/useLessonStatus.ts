import { ref, computed, onUnmounted, watch, type Ref, type ComputedRef } from 'vue'
import { teacherApi } from '@/teacher/api/teacherApi'

export interface UseLessonStatusOptions {
  /** Polling interval in ms, default 5000 */
  interval?: number
  /** Max polling attempts, default 60 (~5 minutes) */
  maxAttempts?: number
  /** Execute check immediately on start, default true */
  immediate?: boolean
}

export interface UseLessonStatusReturn {
  /** Current status */
  status: Ref<'PROCESSING' | 'READY' | 'FAILED' | 'IDLE'>
  /** Whether actively polling */
  isPolling: Ref<boolean>
  /** Number of polling attempts made */
  attempts: Ref<number>
  /** Start polling */
  startPolling: () => void
  /** Stop polling */
  stopPolling: () => void
  /** Reset state */
  reset: () => void
  /** Convenience getters */
  isProcessing: ComputedRef<boolean>
  isReady: ComputedRef<boolean>
  isFailed: ComputedRef<boolean>
  isIdle: ComputedRef<boolean>
}

export function useLessonStatus(
  lessonId: Ref<number>,
  options: UseLessonStatusOptions = {}
): UseLessonStatusReturn {
  const {
    interval = 5000,
    maxAttempts = 60,
    immediate = true
  } = options

  const status = ref<'PROCESSING' | 'READY' | 'FAILED' | 'IDLE'>('IDLE')
  const isPolling = ref(false)
  const attempts = ref(0)
  let pollInterval: ReturnType<typeof setInterval> | null = null

  const isProcessing = computed(() => status.value === 'PROCESSING')
  const isReady = computed(() => status.value === 'READY')
  const isFailed = computed(() => status.value === 'FAILED')
  const isIdle = computed(() => status.value === 'IDLE')

  async function checkStatus() {
    if (!lessonId.value) return

    try {
      const lesson = await teacherApi.getLesson(lessonId.value)
      status.value = lesson.status

      if (lesson.status !== 'PROCESSING') {
        stopPolling()
      }
    } catch (err) {
      console.error('[useLessonStatus] Failed to check lesson status:', err)
    }

    attempts.value++

    if (attempts.value >= maxAttempts) {
      console.warn('[useLessonStatus] Max polling attempts reached')
      stopPolling()
      status.value = 'FAILED'
    }
  }

  function startPolling() {
    if (isPolling.value) return

    isPolling.value = true
    attempts.value = 0
    status.value = 'PROCESSING'

    if (immediate) {
      checkStatus()
    }

    pollInterval = setInterval(checkStatus, interval)
  }

  function stopPolling() {
    if (pollInterval) {
      clearInterval(pollInterval)
      pollInterval = null
    }
    isPolling.value = false
  }

  function reset() {
    stopPolling()
    status.value = 'IDLE'
    attempts.value = 0
  }

  // Auto-reset when lessonId changes
  watch(lessonId, () => {
    reset()
  })

  // Cleanup on unmount
  onUnmounted(() => {
    stopPolling()
  })

  return {
    status,
    isPolling,
    attempts,
    startPolling,
    stopPolling,
    reset,
    isProcessing,
    isReady,
    isFailed,
    isIdle
  }
}
