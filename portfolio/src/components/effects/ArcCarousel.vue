<template>
  <div class="arc-carousel" 
       @mousedown="handleMouseDown"
       @touchstart="handleTouchStart">
    <div class="carousel-container">
      <div
        v-for="(item, index) in displayItems"
        :key="`${item.id}-${index}`"
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

const currentIndex = ref(0) // 当前中心项目的索引
const isDragging = ref(false)
const startX = ref(0)
const dragOffset = ref(0)

// 创建循环显示的项目数组（显示5个用于过渡，但只有3个可见）
const displayItems = computed(() => {
  const total = props.items.length
  const result = []
  
  // 显示5个项目：-2, -1, 0(中心), 1, 2
  for (let i = -2; i <= 2; i++) {
    const index = (currentIndex.value + i + total) % total
    result.push(props.items[index])
  }
  
  return result
})

// 计算每个卡片的位置和样式
const getItemStyle = (displayIndex) => {
  // displayIndex: 0,1,2,3,4 对应相对位置 -2,-1,0,1,2
  const relativePosition = displayIndex - 2 + dragOffset.value / props.itemWidth
  
  // 弧线参数 - 向下10%弧度
  const arcDepth = 0.1
  const y = Math.abs(relativePosition) * arcDepth * props.itemWidth
  
  // 缩放：中间最大(1.0)，两边递减
  const maxDistance = 2
  const distance = Math.abs(relativePosition)
  const scale = Math.max(0.7, 1 - (distance / maxDistance) * 0.3)
  
  // 透明度和可见性：只显示中间3个（-1, 0, 1）
  let opacity = 1
  let visibility = 'visible'
  
  if (Math.abs(relativePosition) > 1.2) {
    // 超出范围的完全隐藏
    opacity = 0
    visibility = 'hidden'
  } else if (Math.abs(relativePosition) > 1.0) {
    // 边缘的开始淡出
    opacity = 1 - (Math.abs(relativePosition) - 1.0) / 0.2
  }
  
  // Z轴深度
  const z = -Math.abs(relativePosition) * 100
  
  // X轴位置
  const x = relativePosition * props.itemWidth
  
  return {
    transform: `translateX(${x}px) translateY(${y}px) translateZ(${z}px) scale(${scale})`,
    opacity: opacity,
    visibility: visibility,
    zIndex: Math.floor(100 - Math.abs(relativePosition) * 10)
  }
}

const handleMouseDown = (e) => {
  isDragging.value = true
  startX.value = e.clientX
  dragOffset.value = 0
  document.addEventListener('mousemove', handleMouseMove)
  document.addEventListener('mouseup', handleMouseUp)
}

const handleMouseMove = (e) => {
  if (!isDragging.value) return
  dragOffset.value = e.clientX - startX.value
}

const handleMouseUp = () => {
  isDragging.value = false
  document.removeEventListener('mousemove', handleMouseMove)
  document.removeEventListener('mouseup', handleMouseUp)
  
  // 根据拖拽距离决定切换方向
  const threshold = props.itemWidth * 0.3
  if (dragOffset.value > threshold) {
    // 向右拖拽，显示前一个（循环）
    currentIndex.value = (currentIndex.value - 1 + props.items.length) % props.items.length
  } else if (dragOffset.value < -threshold) {
    // 向左拖拽，显示下一个（循环）
    currentIndex.value = (currentIndex.value + 1) % props.items.length
  }
  
  dragOffset.value = 0
}

const handleTouchStart = (e) => {
  isDragging.value = true
  startX.value = e.touches[0].clientX
  dragOffset.value = 0
  document.addEventListener('touchmove', handleTouchMove, { passive: false })
  document.addEventListener('touchend', handleTouchEnd)
}

const handleTouchMove = (e) => {
  if (!isDragging.value) return
  e.preventDefault()
  dragOffset.value = e.touches[0].clientX - startX.value
}

const handleTouchEnd = () => {
  isDragging.value = false
  document.removeEventListener('touchmove', handleTouchMove)
  document.removeEventListener('touchend', handleTouchEnd)
  
  const threshold = props.itemWidth * 0.3
  if (dragOffset.value > threshold) {
    currentIndex.value = (currentIndex.value - 1 + props.items.length) % props.items.length
  } else if (dragOffset.value < -threshold) {
    currentIndex.value = (currentIndex.value + 1) % props.items.length
  }
  
  dragOffset.value = 0
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
  width: 0;
  height: 0;
}

.carousel-item {
  position: absolute;
  left: -200px;
  top: -250px;
  width: 400px;
  transform-style: preserve-3d;
  transition: all 0.4s cubic-bezier(0.4, 0.0, 0.2, 1);
  will-change: transform, opacity;
  pointer-events: auto;
}
</style>
