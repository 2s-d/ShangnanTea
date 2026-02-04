<template>
  <div class="cursor-glow" :style="cursorStyle"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const cursorStyle = ref({
  left: '-100px',
  top: '-100px'
})

const handleMouseMove = (e) => {
  cursorStyle.value = {
    left: `${e.clientX}px`,
    top: `${e.clientY}px`
  }
}

onMounted(() => {
  window.addEventListener('mousemove', handleMouseMove)
})

onUnmounted(() => {
  window.removeEventListener('mousemove', handleMouseMove)
})
</script>

<style scoped>
.cursor-glow {
  position: fixed;
  width: 300px;
  height: 300px;
  border-radius: 50%;
  background: radial-gradient(
    circle,
    rgba(102, 126, 234, 0.15) 0%,
    rgba(102, 126, 234, 0.05) 40%,
    transparent 70%
  );
  pointer-events: none;
  transform: translate(-50%, -50%);
  z-index: 1;
  transition: left 0.1s ease, top 0.1s ease;
}

@media (max-width: 768px) {
  .cursor-glow {
    display: none;
  }
}
</style>
