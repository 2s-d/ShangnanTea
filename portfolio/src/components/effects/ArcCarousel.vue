<template>
  <div class="arc-carousel" 
       @mousedown="handleMouseDown"
       @touchstart="handleTouchStart">
    <div class="carousel-container" 
         :style="{ transform: `translateX(${offset}px)` }">
      <div
        v-for="(item, index) in items"
        :key="index"
        class="carousel-item"
        :style="getItemStyle(index)"
      >
        <slot :item="item" :index="index"></slot>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  items: {
    type: Array,
    required: true
  },
  itemWidth: {
    type: Number,
    default: 400
  }
})

const offset = ref(0)
const isDragging = ref(false)
const startX = ref(0)
const startOffset = ref(0)

// 计算每个卡片的位置和样式
const getItemStyle = (index) => {
  const centerIndex = -offset.value / props.itemWidth
  const relativePosition = index - centerIndex
  
  // 弧线参数
  const arcDepth = 0.1 // 10%弧度
  const y = Math.abs(relativePosition) * arcDepth * props.itemWidth
  
  // 缩放：中间最大(1.0)，两边递减
  const maxDistance = 2
  const distance = Math.abs(relativePosition)
  const scale = Math.max(0.7, 1 - (distance / maxDistance) * 0.3)
  
  // 透明度
  const opacity = distance > 1.5 ? Math.max(0.3, 1 - (distance - 1.5) * 0.5) : 1
  
  // Z轴深度
  const z = -Math.abs(relativePosition) * 100
  
  return {
    transform: `translateX(${index * props.itemWidth}px) translateY(${y}px) translateZ(${z}px) scale(${scale})`,
    opacity: opacity,
    zIndex: Math.floor(100 - Math.abs(relativePosition) * 10)
  }
}

const handleMouseDown = (e) => {
  isDragging.value = true
  startX.value = e.clientX
  startOffset.value = offset.value
  document.addEventListener('mousemove', handleMouseMove)
  document.addEventListener('mouseup', handleMouseUp)
}

const handleMouseMove = (e) => {
  if (!isDragging.value) return
  const deltaX = e.clientX - startX.value
  offset.value = startOffset.value + deltaX
  
  // 限制拖拽范围
  const maxOffset = 0
  const minOffset = -(props.items.length - 3) * props.itemWidth
  offset.value = Math.max(minOffset, Math.min(maxOffset, offset.value))
}

const handleMouseUp = () => {
  isDragging.value = false
  document.removeEventListener('mousemove', handleMouseMove)
  document.removeEventListener('mouseup', handleMouseUp)
  
  // 吸附到最近的卡片
  const nearestIndex = Math.round(-offset.value / props.itemWidth)
  offset.value = -nearestIndex * props.itemWidth
}

const handleTouchStart = (e) => {
  isDragging.value = true
  startX.value = e.touches[0].clientX
  startOffset.value = offset.value
  document.addEventListener('touchmove', handleTouchMove)
  document.addEventListener('touchend', handleTouchEnd)
}

const handleTouchMove = (e) => {
  if (!isDragging.value) return
  const deltaX = e.touches[0].clientX - startX.value
  offset.value = startOffset.value + deltaX
  
  const maxOffset = 0
  const minOffset = -(props.items.length - 3) * props.itemWidth
  offset.value = Math.max(minOffset, Math.min(maxOffset, offset.value))
}

const handleTouchEnd = () => {
  isDragging.value = false
  document.removeEventListener('touchmove', handleTouchMove)
  document.removeEventListener('touchend', handleTouchEnd)
  
  const nearestIndex = Math.round(-offset.value / props.itemWidth)
  offset.value = -nearestIndex * props.itemWidth
}
</script>

<style scoped>
.arc-carousel {
  width: 100%;
  height: 600px;
  perspective: 2000px;
  overflow: hidden;
  position: relative;
  cursor: grab;
  user-select: none;
}

.arc-carousel:active {
  cursor: grabbing;
}

.carousel-container {
  position: absolute;
  left: 50%;
  top: 50%;
  transform-style: preserve-3d;
  transition: transform 0.3s cubic-bezier(0.4, 0.0, 0.2, 1);
  will-change: transform;
}

.carousel-item {
  position: absolute;
  left: -200px;
  top: -250px;
  width: 400px;
  transform-style: preserve-3d;
  transition: all 0.3s cubic-bezier(0.4, 0.0, 0.2, 1);
  will-change: transform, opacity;
}
</style>
