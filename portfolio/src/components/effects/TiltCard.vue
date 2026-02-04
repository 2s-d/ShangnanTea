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
import { ref, computed } from 'vue'

const props = defineProps({
  maxTilt: {
    type: Number,
    default: 10
  }
})

const rotateX = ref(0)
const rotateY = ref(0)
const isHovering = ref(false)

const cardStyle = computed(() => ({
  transform: `perspective(1000px) rotateX(${rotateX.value}deg) rotateY(${rotateY.value}deg) scale(${isHovering.value ? 1.02 : 1})`,
  transition: isHovering.value ? 'transform 0.15s ease-out' : 'transform 0.5s ease'
}))

const handleMouseMove = (e) => {
  isHovering.value = true
  const card = e.currentTarget
  const rect = card.getBoundingClientRect()
  
  const x = e.clientX - rect.left
  const y = e.clientY - rect.top
  
  const centerX = rect.width / 2
  const centerY = rect.height / 2
  
  const percentX = (x - centerX) / centerX
  const percentY = (y - centerY) / centerY
  
  // 添加死区，减少边缘抖动
  const deadZone = 0.05
  const clampedPercentX = Math.abs(percentX) < deadZone ? 0 : percentX
  const clampedPercentY = Math.abs(percentY) < deadZone ? 0 : percentY
  
  // 使用缓动函数，让倾斜更平滑
  const easeOutQuad = (t) => t * (2 - t)
  const easedX = easeOutQuad(Math.abs(clampedPercentX)) * Math.sign(clampedPercentX)
  const easedY = easeOutQuad(Math.abs(clampedPercentY)) * Math.sign(clampedPercentY)
  
  rotateY.value = easedX * props.maxTilt * 0.7
  rotateX.value = -easedY * props.maxTilt * 0.7
}

const handleMouseLeave = () => {
  isHovering.value = false
  rotateX.value = 0
  rotateY.value = 0
}
</script>

<style scoped>
.tilt-card {
  transform-style: preserve-3d;
  will-change: transform;
}
</style>
