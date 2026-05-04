<script setup lang="ts">
</script>

<template>
  <div
    v-if="$parent?.open"
    class="absolute z-50 min-w-[8rem] overflow-hidden rounded-md border bg-popover p-1 text-popover-foreground shadow-md mt-2"
    style="right: 0;"
  >
    <slot />
  </div>
</template>
