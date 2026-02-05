<template>
  <canvas ref="canvas" class="code-rain-canvas"></canvas>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  }
})

const canvas = ref(null)
let ctx = null
let animationId = null
let columns = []
let fontSize = 14
let columnCount = 0
let mouseX = -1000
let mouseY = -1000

const characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%^&*()_+-=[]{}|;:,.<>?/~`'

class Column {
  constructor(x, index) {
    this.baseX = x
    this.x = x
    this.index = index
    this.y = Math.random() * -100
    this.speed = Math.random() * 2 + 1
    this.chars = []
    this.length = Math.floor(Math.random() * 20) + 10
    this.depth = Math.random() * 0.5 + 0.5 // 0.5-1.0 深度
    this.glowPhase = Math.random() * Math.PI * 2 // 闪烁相位
    
    for (let i = 0; i < this.length; i++) {
      this.chars.push(characters[Math.floor(Math.random() * characters.length)])
    }
  }

  update() {
    // 鼠标交互 - 散开效果
    const distToMouse = Math.sqrt(
      Math.pow(this.x - mouseX, 2) + 
      Math.pow(this.y - mouseY, 2)
    )
    
    if (distToMouse < 200) {
      // 计算散开方向
      const angle = Math.atan2(this.y - mouseY, this.x - mouseX)
      const force = (200 - distToMouse) / 200
      
      // 横向散开
      this.x += Math.cos(angle) * force * 8
      // 加速下落
      this.speed = (Math.random() * 2 + 1) * (1 + force * 2)
    } else {
      // 恢复到原位
      this.x += (this.baseX - this.x) * 0.1
      this.speed = Math.random() * 2 + 1
    }
    
    this.y += this.speed
    this.glowPhase += 0.05
    
    if (this.y > canvas.value.height + this.length * fontSize) {
      this.y = Math.random() * -100
      this.speed = Math.random() * 2 + 1
      this.x = this.baseX
    }
  }

  draw() {
    const depthScale = this.depth
    const currentFontSize = fontSize * depthScale
    ctx.font = `${currentFontSize}px monospace`
    
    for (let i = 0; i < this.chars.length; i++) {
      const y = this.y + i * fontSize
      const opacity = (1 - (i / this.chars.length)) * this.depth
      
      // 闪烁效果
      const glowIntensity = Math.sin(this.glowPhase + i * 0.3) * 0.3 + 0.7
      
      // 头部字符更亮，带闪烁
      if (i === 0) {
        const brightness = 200 + glowIntensity * 55
        ctx.fillStyle = `rgba(${brightness}, 255, ${brightness}, ${opacity})`
        ctx.shadowBlur = 15 * glowIntensity * this.depth
        ctx.shadowColor = '#00ff00'
      } else {
        const brightness = glowIntensity * 255
        ctx.fillStyle = `rgba(0, ${brightness}, 0, ${opacity * 0.7})`
        ctx.shadowBlur = 5 * glowIntensity * this.depth
        ctx.shadowColor = '#00ff00'
      }
      
      ctx.fillText(this.chars[i], this.x, y)
    }
  }
}

const initCanvas = () => {
  if (!canvas.value) return

  canvas.value.width = window.innerWidth
  canvas.value.height = window.innerHeight
  ctx = canvas.value.getContext('2d')
  ctx.font = `${fontSize}px monospace`

  columnCount = Math.floor(canvas.value.width / fontSize)
  columns = []
  
  for (let i = 0; i < columnCount; i++) {
    columns.push(new Column(i * fontSize, i))
  }
}

const animate = () => {
  if (!ctx || !canvas.value || !props.show) return

  // 半透明黑色背景，产生拖尾效果
  ctx.fillStyle = 'rgba(0, 0, 0, 0.05)'
  ctx.fillRect(0, 0, canvas.value.width, canvas.value.height)

  // 按深度排序，先画远的（小的）
  columns.sort((a, b) => a.depth - b.depth)

  columns.forEach(column => {
    column.update()
    column.draw()
  })

  animationId = requestAnimationFrame(animate)
}

const handleResize = () => {
  initCanvas()
}

const handleMouseMove = (e) => {
  mouseX = e.clientX
  mouseY = e.clientY
}

const handleMouseLeave = () => {
  mouseX = -1000
  mouseY = -1000
}

watch(() => props.show, (newValue) => {
  if (newValue) {
    initCanvas()
    animate()
  } else {
    if (animationId) {
      cancelAnimationFrame(animationId)
      animationId = null
    }
  }
})

onMounted(() => {
  if (props.show) {
    initCanvas()
    animate()
  }
  window.addEventListener('resize', handleResize)
  window.addEventListener('mousemove', handleMouseMove)
  window.addEventListener('mouseleave', handleMouseLeave)
})

onUnmounted(() => {
  if (animationId) {
    cancelAnimationFrame(animationId)
  }
  window.removeEventListener('resize', handleResize)
  window.removeEventListener('mousemove', handleMouseMove)
  window.removeEventListener('mouseleave', handleMouseLeave)
})
</script>

<style scoped>
.code-rain-canvas {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
  opacity: 0.3;
}
</style>
