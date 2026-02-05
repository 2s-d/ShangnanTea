<template>
  <div 
    class="border-glow-wrapper" 
    ref="wrapper"
    @mouseenter="isHovered = true"
    @mouseleave="isHovered = false"
  >
    <slot></slot>
    <canvas ref="canvas" class="border-glow-canvas"></canvas>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

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

const emit = defineEmits(['cycle-complete'])

const wrapper = ref(null)
const canvas = ref(null)
const isHovered = ref(false)
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
  
  // 获取原始尺寸（不受transform影响）
  width = card.offsetWidth
  height = card.offsetHeight
  
  canvas.value.width = width
  canvas.value.height = height
  
  // Canvas不需要设置left/top，因为它是absolute定位在wrapper内
  canvas.value.style.left = '0px'
  canvas.value.style.top = '0px'
  
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
  
  // 如果hover，绘制整圈发光阴影
  if (isHovered.value) {
    ctx.save()
    
    // 绘制多层阴影模拟box-shadow效果
    const shadowLayers = [
      { blur: 30, opacity: 0.15, offset: 0 },
      { blur: 20, opacity: 0.2, offset: 0 },
      { blur: 10, opacity: 0.25, offset: 0 }
    ]
    
    shadowLayers.forEach(layer => {
      ctx.shadowBlur = layer.blur
      ctx.shadowColor = `rgba(102, 126, 234, ${layer.opacity})`
      ctx.shadowOffsetX = layer.offset
      ctx.shadowOffsetY = layer.offset
      
      ctx.strokeStyle = `rgba(102, 126, 234, 0.3)`
      ctx.lineWidth = 1
      
      // 绘制圆角矩形路径
      ctx.beginPath()
      ctx.moveTo(borderRadius, 0)
      ctx.lineTo(width - borderRadius, 0)
      ctx.arcTo(width, 0, width, borderRadius, borderRadius)
      ctx.lineTo(width, height - borderRadius)
      ctx.arcTo(width, height, width - borderRadius, height, borderRadius)
      ctx.lineTo(borderRadius, height)
      ctx.arcTo(0, height, 0, height - borderRadius, borderRadius)
      ctx.lineTo(0, borderRadius)
      ctx.arcTo(0, 0, borderRadius, 0, borderRadius)
      ctx.closePath()
      ctx.stroke()
    })
    
    ctx.restore()
  }
  
  // 光带长度（像素）
  const lineLength = props.glowSize
  const glowLength = lineLength / ((width + height) * 2) // 转换为进度比例
  
  // 绘制两道光带
  for (let band = 0; band < 2; band++) {
    const bandOffset = band * 0.5 // 第二道光带在对面（相隔50%）
    
    // 绘制一条固定长度的实线
    ctx.beginPath()
    ctx.strokeStyle = 'rgba(100, 200, 255, 0.8)'
    ctx.lineWidth = 2
    ctx.shadowBlur = 15
    ctx.shadowColor = 'rgba(100, 200, 255, 0.9)'
    ctx.lineCap = 'round'
    
    // 绘制线段
    const segments = 50
    for (let i = 0; i < segments; i++) {
      const offset = (i / segments) * glowLength
      const currentProgress = (progress + bandOffset - offset + 1) % 1
      const point = getPointOnPath(currentProgress)
      
      if (i === 0) {
        ctx.moveTo(point.x, point.y)
      } else {
        ctx.lineTo(point.x, point.y)
      }
    }
    ctx.stroke()
    
    // 头部亮点已移除，只保留光带线条
  }
  
  // 更新进度
  progress += props.speed / ((width + height) * 2)
  if (progress >= 1) {
    progress = 0
    // 检测到完成一圈，触发事件
    emit('cycle-complete')
  }
  
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
  transition: all 0.3s;
  margin-bottom: 24px;
  border-radius: 16px; /* 和卡片的圆角一致 */
}

/* 把box-shadow放在wrapper上，增强亮度 */
.border-glow-wrapper:hover {
  box-shadow: 0 8px 40px rgba(102, 126, 234, 0.6);
}

.border-glow-canvas {
  position: absolute;
  top: 0;
  left: 0;
  pointer-events: none;
  z-index: 10;
}
</style>
