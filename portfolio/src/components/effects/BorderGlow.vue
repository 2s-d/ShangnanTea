<template>
  <div class="border-glow-wrapper" ref="wrapper">
    <slot></slot>
    <canvas ref="canvas" class="border-glow-canvas"></canvas>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'

const props = defineProps({
  speed: {
    type: Number,
    default: 1.5 // 速度：每帧移动的像素
  },
  glowSize: {
    type: Number,
    default: 60 // 光晕长度，从80减少到60
  }
})

const wrapper = ref(null)
const canvas = ref(null)
let ctx = null
let animationId = null
let progress = 0 // 0-1 的进度，表示光点在路径上的位置
let width = 0
let height = 0
let borderRadius = 12

const updateSize = () => {
  if (!wrapper.value || !canvas.value) return
  
  const card = wrapper.value.querySelector('.el-card')
  if (!card) return
  
  const rect = card.getBoundingClientRect()
  width = rect.width
  height = rect.height
  
  canvas.value.width = width
  canvas.value.height = height
  
  // 获取实际的 border-radius
  const styles = window.getComputedStyle(card)
  borderRadius = parseInt(styles.borderRadius) || 12
}

// 根据进度获取路径上的点
const getPointOnPath = (progress) => {
  // 路径总长度
  const perimeter = (width + height) * 2 - borderRadius * 8 + Math.PI * borderRadius * 2
  const distance = progress * perimeter
  
  const r = borderRadius
  
  // 定义四条边和四个圆角的长度
  const topEdge = width - 2 * r
  const rightEdge = height - 2 * r
  const bottomEdge = width - 2 * r
  const leftEdge = height - 2 * r
  const cornerArc = (Math.PI / 2) * r
  
  let currentDist = 0
  
  // 右上角圆角 (从顶部右侧开始)
  if (distance < currentDist + cornerArc) {
    const angle = -Math.PI / 2 + (distance - currentDist) / r
    return {
      x: width - r + Math.cos(angle) * r,
      y: r + Math.sin(angle) * r
    }
  }
  currentDist += cornerArc
  
  // 右边
  if (distance < currentDist + rightEdge) {
    return {
      x: width,
      y: r + (distance - currentDist)
    }
  }
  currentDist += rightEdge
  
  // 右下角圆角
  if (distance < currentDist + cornerArc) {
    const angle = 0 + (distance - currentDist) / r
    return {
      x: width - r + Math.cos(angle) * r,
      y: height - r + Math.sin(angle) * r
    }
  }
  currentDist += cornerArc
  
  // 底边
  if (distance < currentDist + bottomEdge) {
    return {
      x: width - r - (distance - currentDist),
      y: height
    }
  }
  currentDist += bottomEdge
  
  // 左下角圆角
  if (distance < currentDist + cornerArc) {
    const angle = Math.PI / 2 + (distance - currentDist) / r
    return {
      x: r + Math.cos(angle) * r,
      y: height - r + Math.sin(angle) * r
    }
  }
  currentDist += cornerArc
  
  // 左边
  if (distance < currentDist + leftEdge) {
    return {
      x: 0,
      y: height - r - (distance - currentDist)
    }
  }
  currentDist += leftEdge
  
  // 左上角圆角
  if (distance < currentDist + cornerArc) {
    const angle = Math.PI + (distance - currentDist) / r
    return {
      x: r + Math.cos(angle) * r,
      y: r + Math.sin(angle) * r
    }
  }
  currentDist += cornerArc
  
  // 顶边
  return {
    x: r + (distance - currentDist),
    y: 0
  }
}

const animate = () => {
  if (!ctx || !canvas.value) return
  
  ctx.clearRect(0, 0, width, height)
  
  // 绘制两道均匀的细线光带
  const glowLength = props.glowSize / ((width + height) * 2) // 转换为进度比例
  
  // 绘制两道光带
  for (let band = 0; band < 2; band++) {
    const bandOffset = band * 0.5 // 第二道光带在对面（相隔50%）
    
    // 绘制均匀的细线
    for (let i = 0; i < 40; i++) {
      const offset = (i / 40) * glowLength
      const currentProgress = (progress + bandOffset - offset + 1) % 1
      const point = getPointOnPath(currentProgress)
      
      // 均匀的透明度和大小
      const opacity = 0.6 - (i / 40) * 0.5 // 从0.6渐变到0.1
      const size = 1.5 // 固定细线宽度
      
      // 炫酷的蓝色亮光
      ctx.fillStyle = `rgba(100, 200, 255, ${opacity})`
      ctx.shadowBlur = 12
      ctx.shadowColor = `rgba(100, 200, 255, ${opacity * 0.8})`
      
      ctx.beginPath()
      ctx.arc(point.x, point.y, size, 0, Math.PI * 2)
      ctx.fill()
    }
    
    // 在光带头部添加一个亮点
    const headPoint = getPointOnPath((progress + bandOffset) % 1)
    ctx.fillStyle = 'rgba(150, 220, 255, 0.9)'
    ctx.shadowBlur = 20
    ctx.shadowColor = 'rgba(150, 220, 255, 0.9)'
    ctx.beginPath()
    ctx.arc(headPoint.x, headPoint.y, 3, 0, Math.PI * 2)
    ctx.fill()
  }
  
  // 更新进度
  progress += props.speed / ((width + height) * 2)
  if (progress >= 1) progress = 0
  
  animationId = requestAnimationFrame(animate)
}

onMounted(() => {
  if (!canvas.value) return
  
  ctx = canvas.value.getContext('2d')
  
  // 等待 DOM 渲染完成
  setTimeout(() => {
    updateSize()
    animate()
  }, 100)
  
  window.addEventListener('resize', updateSize)
})

onUnmounted(() => {
  if (animationId) {
    cancelAnimationFrame(animationId)
  }
  window.removeEventListener('resize', updateSize)
})
</script>

<style scoped>
.border-glow-wrapper {
  position: relative;
  display: inline-block;
  width: 100%;
  height: 100%;
}

.border-glow-canvas {
  position: absolute;
  top: 0;
  left: 0;
  pointer-events: none;
  z-index: 10;
}
</style>
