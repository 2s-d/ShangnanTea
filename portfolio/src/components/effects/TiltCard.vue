<template>
  <div ref="tiltRef" class="tilt-card">
    <slot></slot>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import VanillaTilt from 'vanilla-tilt'

const props = defineProps({
  maxTilt: {
    type: Number,
    default: 10
  }
})

const tiltRef = ref(null)
let tiltInstance = null

onMounted(() => {
  if (tiltRef.value) {
    VanillaTilt.init(tiltRef.value, {
      max: props.maxTilt,
      speed: 400,
      glare: false,
      scale: 1.02,
      perspective: 1000,
      transition: true,
      easing: "cubic-bezier(.03,.98,.52,.99)"
    })
    tiltInstance = tiltRef.value.vanillaTilt
  }
})

onUnmounted(() => {
  if (tiltInstance) {
    tiltInstance.destroy()
  }
})
</script>

<style scoped>
.tilt-card {
  transform-style: preserve-3d;
}
</style>
