<script setup lang="ts">
import { ref } from 'vue'

const props = defineProps<{
  as?: string
}>()

const open = ref(false)

function toggle() {
  open.value = !open.value
}

function close() {
  open.value = false
}

defineExpose({ close })
</script>

<template>
  <component :is="props.as || 'button'" @click="toggle">
    <slot />
  </component>
</template>
