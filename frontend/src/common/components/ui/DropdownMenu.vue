<script setup lang="ts">
import { provide, ref } from 'vue'

const open = ref(false)

function openMenu() {
  open.value = true
}

function closeMenu() {
  open.value = false
}

function toggleMenu() {
  open.value = !open.value
}

provide('dropdown', {
  open,
  openMenu,
  closeMenu,
  toggleMenu,
})
</script>

<template>
  <div class="relative inline-block">
    <slot />
  </div>
</template>
