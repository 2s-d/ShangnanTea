<template>
  <div
    class="tilt-card-wrapper"
    @mouseenter="handleMouseEnter"
    @mousemove="handleMouseMove"
    @mouseleave="handleMouseLeave"
  >
    <div
      class="tilt-card"
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

const cardStyle = computed(() => ({
  transform: `perspective(1000px) rotateX(${rotateX.value}deg) rotateY(${rotateY.value}deg) scale(${isHovering.value ? 1.02 : 1})`,
  transition: isHovering.value ? 'transform 0.15s ease-out' : 'transform 0.5s ease'
}))

const handleMouseEnter = () => {
  isHovering.value = true
}

const handleMouseMove = (e) => {
  const wrapper = e.currentTarget
  const rect = wrapper.getBoundingClientRect()
  
  const x = e.clientX - rect.left
  const y = e.clientY - rect.top
  
  const centerX = rect.width / 2
  const centerY = rect.height / 2
  
  const percentX = (x - centerX) / centerX
  const percentY = (y - centerY) / centerY
  
  // 限制范围，防止过度倾斜
  const clampedPercentX = Math.max(-1, Math.min(1, percentX))
  const clampedPercentY = Math.max(-1, Math.min(1, percentY))
  
  rotateY.value = clampedPercentX * props.maxTilt * 0.6
  rotateX.value = -clampedPercentY * props.maxTilt * 0.6
}

const handleMouseLeave = () => {
  isHovering.value = false
  rotateX.value = 0
  rotateY.value = 0
}
</script>

<style scoped>
.tilt-card-wrapper {
  /* 添加透明padding作为缓冲区，防止边缘抖动 */
  padding: 10px;
  margin: -10px;
  display: inline-block;
  width: calc(100% + 20px);
}

.tilt-card {
  transform-style: preserve-3d;
  will-change: transform;
  /* 防止子元素触发事件 */
  pointer-events: auto;
}

.tilt-card > :deep(*) {
  pointer-events: none;
}
</style>
