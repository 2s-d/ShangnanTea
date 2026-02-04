<template>
  <div
    class="tilt-card"
    @mousemove="handleMouseMove"
    @mouseleave="handleMouseLeave"
    :style="cardStyle"
  >
    <slot></slot>
  </div>
</template>

<script setup>
import { ref, computed, onUnmounted } from 'vue'

const props = defineProps({
  maxTilt: {
    type: Number,
    default: 10
  }
})

const rotateX = ref(0)
const rotateY = ref(0)
const isHovering = ref(false)
let rafId = null
let targetRotateX = 0
let targetRotateY = 0

const cardStyle = computed(() => ({
  transform: `perspective(1000px) rotateX(${rotateX.value}deg) rotateY(${rotateY.value}deg) scale(${isHovering.value ? 1.02 : 1})`,
  transition: isHovering.value ? 'none' : 'transform 0.5s ease'
}))

// 使用requestAnimationFrame平滑更新
const updateRotation = () => {
  const lerp = (start, end, factor) => start + (end - start) * factor
  
  rotateX.value = lerp(rotateX.value, targetRotateX, 0.1)
  rotateY.value = lerp(rotateY.value, targetRotateY, 0.1)
  
  // 如果还在hover且未达到目标，继续更新
  if (isHovering.value && (
    Math.abs(rotateX.value - targetRotateX) > 0.01 ||
    Math.abs(rotateY.value - targetRotateY) > 0.01
  )) {
    rafId = requestAnimationFrame(updateRotation)
  }
}

const handleMouseMove = (e) => {
  if (!isHovering.value) {
    isHovering.value = true
  }
  
  const card = e.currentTarget
  const rect = card.getBoundingClientRect()
  
  const x = e.clientX - rect.left
  const y = e.clientY - rect.top
  
  const centerX = rect.width / 2
  const centerY = rect.height / 2
  
  const percentX = (x - centerX) / centerX
  const percentY = (y - centerY) / centerY
  
  // 设置目标值，降低强度
  targetRotateY = percentX * props.maxTilt * 0.6
  targetRotateX = -percentY * props.maxTilt * 0.6
  
  // 取消之前的动画并启动新的
  if (rafId) {
    cancelAnimationFrame(rafId)
  }
  rafId = requestAnimationFrame(updateRotation)
}

const handleMouseLeave = () => {
  isHovering.value = false
  targetRotateX = 0
  targetRotateY = 0
  rotateX.value = 0
  rotateY.value = 0
  
  if (rafId) {
    cancelAnimationFrame(rafId)
    rafId = null
  }
}

onUnmounted(() => {
  if (rafId) {
    cancelAnimationFrame(rafId)
  }
})
</script>

<style scoped>
.tilt-card {
  transform-style: preserve-3d;
  will-change: transform;
}
</style>
