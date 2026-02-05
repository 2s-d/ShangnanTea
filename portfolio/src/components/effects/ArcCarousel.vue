<template>
  <div class="arc-carousel" 
       @mousedown="handleMouseDown"
       @touchstart="handleTouchStart">
    <!-- 左箭头 -->
    <div class="arrow arrow-left" 
         @click="handleArrowClick('prev')">
      <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <polyline points="15 18 9 12 15 6"></polyline>
      </svg>
    </div>
    
    <!-- 右箭头 -->
    <div class="arrow arrow-right"
         @click="handleArrowClick('next')">
      <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <polyline points="9 18 15 12 9 6"></polyline>
      </svg>
    </div>
    
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
import { ref, computed, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  items: {
    type: Array,
    required: true
  },
  itemWidth: {
    type: Number,
    default: 400
  },
  autoPlayInterval: {
    type: Number,
    default: 10000 // 10秒
  }
})

const currentIndex = ref(0) // 当前中心项目的索引
const isDragging = ref(false)
const startX = ref(0)
const dragOffset = ref(0)
let autoPlayTimer = null

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

// 自动轮播：向左滑动（下一个）
const autoPlay = () => {
  currentIndex.value = (currentIndex.value + 1) % props.items.length
}

// 启动自动轮播
const startAutoPlay = () => {
  stopAutoPlay() // 先清除旧的定时器
  autoPlayTimer = setInterval(autoPlay, props.autoPlayInterval)
}

// 停止自动轮播
const stopAutoPlay = () => {
  if (autoPlayTimer) {
    clearInterval(autoPlayTimer)
    autoPlayTimer = null
  }
}

// 重置自动轮播（用户操作后重新计时）
const resetAutoPlay = () => {
  startAutoPlay()
}

// 箭头点击处理
const handleArrowClick = (direction) => {
  stopAutoPlay() // 停止自动轮播
  
  if (direction === 'prev') {
    // 向右滑动（显示前一个）
    currentIndex.value = (currentIndex.value - 1 + props.items.length) % props.items.length
  } else {
    // 向左滑动（显示下一个）
    currentIndex.value = (currentIndex.value + 1) % props.items.length
  }
  
  // 重新启动自动轮播
  resetAutoPlay()
}

// 计算每个卡片的位置和样式
const getItemStyle = (displayIndex) => {
  // displayIndex: 0,1,2,3,4 对应相对位置 -2,-1,0,1,2
  const relativePosition = displayIndex - 2 + dragOffset.value / props.itemWidth
  
  // 弧线参数 - 向下10%弧度
  const arcDepth = 0.1
  const y = Math.abs(relativePosition) * arcDepth * props.itemWidth
  
  // 缩放：中间最大(1.0)，两边递减，平滑过渡
  const distance = Math.abs(relativePosition)
  let scale
  if (distance <= 1) {
    // 中间到左右：1.0 -> 0.85
    scale = 1.0 - distance * 0.15
  } else {
    // 继续向外：0.85 -> 0.7
    scale = 0.85 - (distance - 1) * 0.15
    scale = Math.max(0.7, scale)
  }
  
  // 透明度：平滑过渡，不突然消失
  let opacity
  if (distance <= 1) {
    // 中间3个卡片：完全可见
    opacity = 1
  } else if (distance <= 2) {
    // 从1到2的距离：从1渐变到0
    opacity = 1 - (distance - 1)
  } else {
    // 超出2的距离：完全透明
    opacity = 0
  }
  
  // 可见性：只有完全透明时才隐藏
  const visibility = opacity > 0 ? 'visible' : 'hidden'
  
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
  stopAutoPlay() // 用户操作时停止自动轮播
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
  resetAutoPlay() // 用户操作后重新启动自动轮播
}

const handleTouchStart = (e) => {
  isDragging.value = true
  startX.value = e.touches[0].clientX
  dragOffset.value = 0
  stopAutoPlay() // 用户操作时停止自动轮播
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
  resetAutoPlay() // 用户操作后重新启动自动轮播
}

// 组件挂载时启动自动轮播
onMounted(() => {
  startAutoPlay()
})

// 组件卸载时清除定时器
onUnmounted(() => {
  stopAutoPlay()
})
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

/* 箭头按钮 */
.arrow {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  cursor: pointer;
  z-index: 10;
  transition: all 0.3s ease;
  opacity: 0.4;
  backdrop-filter: blur(10px);
}

.arrow:hover {
  opacity: 1;
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-50%) scale(1.1);
}

.arrow-left {
  left: 10px;
  animation: arrowPulse 3.5s ease-in-out infinite;
}

.arrow-right {
  right: 10px;
  animation: arrowPulse 3.5s ease-in-out infinite;
}

/* 若隐若现动画：3秒保持，0.5秒亮一下 */
@keyframes arrowPulse {
  0%, 85.7% {
    opacity: 0.4;
  }
  90%, 95% {
    opacity: 0.7;
  }
  100% {
    opacity: 0.4;
  }
}

.arrow svg {
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.3));
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
