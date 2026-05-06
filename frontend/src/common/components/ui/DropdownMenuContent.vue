<script setup lang="ts">
import { inject, onMounted, onUnmounted } from 'vue'

const dropdown = inject<any>('dropdown')

function handleClickOutside(event: MouseEvent) {
  const target = event.target as HTMLElement
  if (!target.closest('.relative')) {
    dropdown?.closeMenu()
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<template>
  <div
    v-if="dropdown?.open.value"
    class="absolute z-50 min-w-[8rem] overflow-hidden rounded-md border bg-popover p-1 text-popover-foreground shadow-md mt-2"
    style="right: 0;"
    @click.stop
  >
    <slot />
  </div>
</template>
