<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { teacherApi } from '@/teacher/api/teacherApi'
import type { CourseDetailForTeacher, Chapter, Lesson } from '@/common/types/api'
import TeacherHeader from '@/teacher/components/TeacherHeader.vue'
import { Plus, Trash2, ChevronDown, ChevronRight, Upload, Edit } from 'lucide-vue-next'

const router = useRouter()
const route = useRoute()

const courseId = Number(route.params.id)
const course = ref<CourseDetailForTeacher | null>(null)
const loading = ref(true)
const error = ref('')
const expandedChapters = ref<Set<number>>(new Set())

// New chapter/lesson state
const newChapterTitle = ref('')
const newChapterPosition = ref(1)
const newLessonTitles = ref<Record<number, string>>({})

onMounted(async () => {
  try {
    course.value = await teacherApi.getCourseDetail(courseId)
  } catch (err: any) {
    error.value = err.message || '加载失败'
  } finally {
    loading.value = false
  }
})

function toggleChapter(chapterId: number) {
  if (expandedChapters.value.has(chapterId)) {
    expandedChapters.value.delete(chapterId)
  } else {
    expandedChapters.value.add(chapterId)
  }
}

async function addChapter() {
  if (!newChapterTitle.value.trim()) return
  try {
    await teacherApi.createChapter({
      courseId,
      title: newChapterTitle.value,
      position: newChapterPosition.value,
    })
    newChapterTitle.value = ''
    // Refresh
    course.value = await teacherApi.getCourseDetail(courseId)
  } catch (err: any) {
    alert(err.message || '添加失败')
  }
}

async function addLesson(chapterId: number) {
  const title = newLessonTitles.value[chapterId]
  if (!title?.trim()) return
  try {
    const chapter = course.value?.chapters?.find(c => c.id === chapterId)
    await teacherApi.createLesson({
      chapterId,
      title,
      orderNum: (chapter?.lessons?.length || 0) + 1,
    })
    newLessonTitles.value[chapterId] = ''
    // Refresh
    course.value = await teacherApi.getCourseDetail(courseId)
  } catch (err: any) {
    alert(err.message || '添加失败')
  }
}

async function deleteChapter(chapterId: number) {
  if (!confirm('确定删除此章节?')) return
  try {
    await teacherApi.deleteChapter(chapterId)
    course.value = await teacherApi.getCourseDetail(courseId)
  } catch (err: any) {
    alert(err.message || '删除失败')
  }
}

async function deleteLesson(lessonId: number) {
  if (!confirm('确定删除此课时?')) return
  try {
    await teacherApi.deleteLesson(lessonId)
    course.value = await teacherApi.getCourseDetail(courseId)
  } catch (err: any) {
    alert(err.message || '删除失败')
  }
}

function goToLesson(courseId: number, lessonId: number) {
  router.push(`/teacher/courses/${courseId}/lessons/${lessonId}`)
}
</script>

<template>
  <div class="min-h-screen bg-background">
    <TeacherHeader />

    <main class="pt-16 max-w-4xl mx-auto px-4 py-8">
      <!-- Loading -->
      <div v-if="loading">
        <Skeleton class="h-8 w-48 mb-4" />
        <Skeleton class="h-4 w-full mb-2" />
        <Skeleton class="h-4 w-3/4" />
      </div>

      <!-- Error -->
      <div v-else-if="error" class="text-center py-12">
        <p class="text-destructive mb-4">{{ error }}</p>
        <Button @click="$router.back()">返回</Button>
      </div>

      <!-- Content -->
      <div v-else-if="course">
        <div class="flex items-center justify-between mb-6">
          <h1 class="text-2xl font-bold">{{ course.title }}</h1>
          <Badge :variant="course.status === 'PUBLISHED' ? 'default' : 'secondary'">
            {{ course.status === 'PUBLISHED' ? '已发布' : '草稿' }}
          </Badge>
        </div>

        <!-- Chapters -->
        <div class="space-y-4">
          <div v-for="chapter in course.chapters" :key="chapter.id" class="border rounded-lg">
            <CardHeader
              class="py-3 px-4 cursor-pointer hover:bg-muted/50"
              @click="toggleChapter(chapter.id)"
            >
              <div class="flex items-center justify-between">
                <div class="flex items-center gap-2">
                  <component :is="expandedChapters.has(chapter.id) ? ChevronDown : ChevronRight" class="w-4 h-4" />
                  <span class="font-medium">{{ chapter.orderNum }}. {{ chapter.title }}</span>
                </div>
                <Button size="sm" variant="ghost" @click.stop="deleteChapter(chapter.id)">
                  <Trash2 class="w-4 h-4" />
                </Button>
              </div>
            </CardHeader>

            <CardContent v-if="expandedChapters.has(chapter.id)" class="border-t py-4">
              <ul class="space-y-2">
                <li
                  v-for="lesson in chapter.lessons"
                  :key="lesson.id"
                  class="flex items-center justify-between p-2 rounded hover:bg-muted/50"
                >
                  <div class="flex items-center gap-2">
                    <Badge :variant="lesson.status === 'READY' ? 'default' : 'secondary'" class="text-xs">
                      {{ lesson.status === 'READY' ? '就绪' : lesson.status }}
                    </Badge>
                    <span>{{ lesson.title }}</span>
                  </div>
                  <div class="flex gap-2">
                    <Button size="sm" variant="ghost" @click="goToLesson(courseId, lesson.id)">
                      <Upload class="w-3 h-3 mr-1" />
                      视频
                    </Button>
                    <Button size="sm" variant="ghost" @click="deleteLesson(lesson.id)">
                      <Trash2 class="w-3 h-3" />
                    </Button>
                  </div>
                </li>
              </ul>

              <!-- Add Lesson -->
              <div class="flex gap-2 mt-4">
                <Input
                  v-model="newLessonTitles[chapter.id]"
                  placeholder="输入课时标题"
                  class="flex-1"
                  @keyup.enter="addLesson(chapter.id)"
                />
                <Button @click="addLesson(chapter.id)">
                  <Plus class="w-4 h-4 mr-1" />
                  添加课时
                </Button>
              </div>
            </CardContent>
          </div>
        </div>

        <!-- Add Chapter -->
        <Card class="mt-6">
          <CardContent class="p-4">
            <div class="flex gap-4">
              <Input
                v-model="newChapterTitle"
                placeholder="输入章节标题"
                class="flex-1"
                @keyup.enter="addChapter"
              />
              <Input
                v-model.number="newChapterPosition"
                type="number"
                placeholder="顺序"
                class="w-24"
              />
              <Button @click="addChapter">
                <Plus class="w-4 h-4 mr-1" />
                添加章节
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    </main>
  </div>
</template>
