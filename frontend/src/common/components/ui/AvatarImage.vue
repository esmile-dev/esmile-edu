<script setup lang="ts">
import { ref, onMounted } from 'vue'

const props = defineProps<{
  src?: string | null
  alt?: string
}>()

const hasError = ref(false)

function handleError() {
  hasError.value = true
}

onMounted(() => {
  hasError.value = false
})
</script>

<template>
  <img
    v-if="!hasError && src"
    :src="src"
    :alt="alt"
    class="aspect-square h-full w-full object-cover"
    @error="handleError"
  />
</template>
