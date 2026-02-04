<template>
  <div class="tilt-wrapper">
    <div
      class="tilt-card"
      @mouseenter="handleMouseEnter"
      @mousemove="handleMouseMove"
      @mouseleave="handleMouseLeave"
      :style="cardStyle"
    >
      <slot></slot>
    </div>
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
let isMouseInside = false

const cardStyle = computed(() => ({
  transform: `perspective(1000px) rotateX(${rotateX.value}deg) rotateY(${rotateY.value}deg) scale(${isHovering.value ? 1.02 : 1})`,
  transition: isHovering.value ? 'transform 0.1s ease' : 'transform 0.5s ease'
}))

const handleMouseEnter = () => {
  isMouseInside = true
  isHovering.value = true
}

const handleMouseMove = (e) => {
  if (!isMouseInside) return
  
  const card = e.currentTarget
  const rect = card.getBoundingClientRect()
  
  const x = e.clientX - rect.left
  const y = e.clientY - rect.top
  
  // 检查是否真的在卡片内
  if (x < 0 || x > rect.width || y < 0 || y > rect.height) {
    return
  }
  
  // 下边缘40%区域设为死区，不响应倾斜
  const deadZoneHeight = rect.height * 0.4
  if (y > rect.height - deadZoneHeight) {
    // 在死区内，保持当前状态或轻微复位
    rotateX.value *= 0.9
    rotateY.value *= 0.9
    return
  }
  
  const centerX = rect.width / 2
  const centerY = rect.height / 2
  
  const percentX = (x - centerX) / centerX
  const percentY = (y - centerY) / centerY
  
  rotateY.value = percentX * props.maxTilt
  rotateX.value = -percentY * props.maxTilt
}

const handleMouseLeave = (e) => {
  // 确保真的离开了卡片区域
  const card = e.currentTarget
  const rect = card.getBoundingClientRect()
  const x = e.clientX
  const y = e.clientY
  
  // 如果鼠标还在卡片范围内，不重置
  if (x >= rect.left && x <= rect.right && y >= rect.top && y <= rect.bottom) {
    return
  }
  
  isMouseInside = false
  isHovering.value = false
  rotateX.value = 0
  rotateY.value = 0
}
</script>

<style scoped>
/* 外层wrapper提供缓冲区 */
.tilt-wrapper {
  padding: 30px;
  margin: -30px;
}

.tilt-card {
  transform-style: preserve-3d;
  will-change: transform;
  isolation: isolate;
  backface-visibility: hidden;
  -webkit-backface-visibility: hidden;
}

/* 确保子元素不会干扰鼠标事件 */
.tilt-card :deep(*) {
  pointer-events: auto;
}
</style>
