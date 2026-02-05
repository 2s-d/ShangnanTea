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
let glowingChars = [] // 全局发光字符列表 [{columnIndex, charIndex, startTime}]

const characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%^&*()_+-=[]{}|;:,.<>?/~`'

class Column {
  constructor(x, index) {
    this.x = x
    this.index = index
    this.y = Math.random() * -100
    this.speed = Math.random() * 2 + 1
    this.chars = []
    this.length = Math.floor(Math.random() * 20) + 10
    
    for (let i = 0; i < this.length; i++) {
      this.chars.push(characters[Math.floor(Math.random() * characters.length)])
    }
  }

  update() {
    this.y += this.speed
    
    if (this.y > canvas.value.height + this.length * fontSize) {
      this.y = Math.random() * -100
      this.speed = Math.random() * 2 + 1
    }
  }

  draw() {
    for (let i = 0; i < this.chars.length; i++) {
      const y = this.y + i * fontSize
      const opacity = 1 - (i / this.chars.length)
      
      // 检查这个字符是否在发光列表中
      const glowData = glowingChars.find(g => g.columnIndex === this.index && g.charIndex === i)
      
      if (i === 0) {
        // 头部字符更亮
        ctx.fillStyle = `rgba(200, 255, 200, ${opacity})`
        ctx.shadowBlur = 10
        ctx.shadowColor = '#00ff00'
      } else if (glowData) {
        // 发光字符
        const elapsed = Date.now() - glowData.startTime
        const progress = elapsed / 1500
        const glowIntensity = Math.sin(progress * Math.PI) // 0-1-0的脉冲
        
        const brightness = 200 + glowIntensity * 55
        ctx.fillStyle = `rgba(${brightness}, 255, ${brightness}, ${opacity * (0.7 + glowIntensity * 0.3)})`
        ctx.shadowBlur = 20 + glowIntensity * 20
        ctx.shadowColor = `rgba(0, 255, 0, ${glowIntensity * 0.8})`
      } else {
        // 普通字符
        ctx.fillStyle = `rgba(0, 255, 0, ${opacity * 0.7})`
        ctx.shadowBlur = 0
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
    columns.push(new Column(i * fontSize))
  }
}

const animate = () => {
  if (!ctx || !canvas.value || !props.show) return

  // 半透明黑色背景，产生拖尾效果
  ctx.fillStyle = 'rgba(0, 0, 0, 0.05)'
  ctx.fillRect(0, 0, canvas.value.width, canvas.value.height)

  columns.forEach(column => {
    column.update()
    column.draw()
  })

  animationId = requestAnimationFrame(animate)
}

const handleResize = () => {
  initCanvas()
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
})

onUnmounted(() => {
  if (animationId) {
    cancelAnimationFrame(animationId)
  }
  window.removeEventListener('resize', handleResize)
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
