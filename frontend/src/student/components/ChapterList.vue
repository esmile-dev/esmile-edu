<script setup lang="ts">
import { ref } from 'vue'
import type { Chapter, Lesson } from '@/common/types/api'
import { ChevronDown, ChevronRight, Play } from 'lucide-vue-next'

const props = defineProps<{
  chapters: Chapter[]
  enrolled?: boolean
}>()

const emit = defineEmits<{
  selectLesson: [lesson: Lesson]
}>()

const expandedChapters = ref<Set<number>>(new Set())

function toggleChapter(chapterId: number) {
  if (expandedChapters.value.has(chapterId)) {
    expandedChapters.value.delete(chapterId)
  } else {
    expandedChapters.value.add(chapterId)
  }
}

function isExpanded(chapterId: number) {
  return expandedChapters.value.has(chapterId)
}

function formatDuration(seconds: number | null | undefined): string {
  if (!seconds) return ''
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins}:${secs.toString().padStart(2, '0')}`
}
</script>

<template>
  <div class="space-y-4">
    <div v-for="chapter in chapters" :key="chapter.id">
      <Card>
        <CardHeader
          class="py-3 cursor-pointer hover:bg-muted/50"
          @click="toggleChapter(chapter.id)"
        >
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-2">
              <component
                :is="isExpanded(chapter.id) ? ChevronDown : ChevronRight"
                class="w-4 h-4"
              />
              <span class="font-medium">{{ chapter.orderNum }}. {{ chapter.title }}</span>
            </div>
            <Badge variant="secondary">{{ chapter.lessons?.length || 0 }} 课时</Badge>
          </div>
        </CardHeader>
        <CardContent v-if="isExpanded(chapter.id)" class="py-2">
          <ul class="space-y-2">
            <li
              v-for="lesson in chapter.lessons"
              :key="lesson.id"
              :id="`lesson-${lesson.id}`"
              class="flex items-center justify-between p-2 rounded hover:bg-muted/50 cursor-pointer"
              @click="emit('selectLesson', lesson)"
            >
              <div class="flex items-center gap-3">
                <Play class="w-4 h-4 text-muted-foreground" />
                <span>{{ lesson.title }}</span>
              </div>
              <div class="flex items-center gap-2">
                <Badge
                  v-if="lesson.status !== 'READY'"
                  :variant="lesson.status === 'PROCESSING' ? 'outline' : 'destructive'"
                  class="text-xs"
                >
                  {{ lesson.status === 'PROCESSING' ? '处理中' : '失败' }}
                </Badge>
                <span v-if="lesson.duration" class="text-sm text-muted-foreground">
                  {{ formatDuration(lesson.duration) }}
                </span>
              </div>
            </li>
          </ul>
        </CardContent>
      </Card>
    </div>
  </div>
</template>
