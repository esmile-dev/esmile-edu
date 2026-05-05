<script setup lang="ts">
import { inject } from 'vue'

const props = defineProps<{
  disabled?: boolean
  as?: string
}>()

const dropdown = inject<any>('dropdown')

function handleClick() {
  if (props.disabled) return
  dropdown?.toggleMenu()
}
</script>

<template>
  <component
    :is="props.as || 'button'"
    @click="handleClick"
  >
    <slot />
  </component>
</template>
