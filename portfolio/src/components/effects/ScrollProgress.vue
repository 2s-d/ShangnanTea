<template>
  <div class="scroll-progress-wrapper">
    <div class="scroll-progress-bar" :style="{ width: `${scrollProgress}%` }"></div>
  </div>
</template>

<script setup>
import { useScroll } from '../../composables/useScroll'

const { scrollProgress } = useScroll()
</script>

<style scoped>
.scroll-progress-wrapper {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 3px;
  background: rgba(0, 0, 0, 0.1);
  z-index: var(--z-fixed);
}

.scroll-progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  transition: width 0.1s ease;
}

.dark-mode .scroll-progress-wrapper {
  background: rgba(255, 255, 255, 0.1);
}
</style>
