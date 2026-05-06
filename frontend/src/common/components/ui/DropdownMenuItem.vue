<script setup lang="ts">
import { inject } from 'vue'

const props = defineProps<{
  disabled?: boolean
}>()

const emit = defineEmits<{
  click: [event: MouseEvent]
}>()

const dropdown = inject<any>('dropdown')

function handleClick(event: MouseEvent) {
  if (props.disabled) return
  emit('click', event)
  dropdown?.closeMenu()
}
</script>

<template>
  <div
    class="relative flex cursor-pointer select-none items-center rounded-sm px-2 py-1.5 text-sm outline-none transition-colors hover:bg-accent hover:text-accent-foreground data-[disabled]:pointer-events-none data-[disabled]:opacity-50"
    :class="{ 'opacity-50 pointer-events-none': disabled }"
    @click="handleClick"
  >
    <slot />
  </div>
</template>
