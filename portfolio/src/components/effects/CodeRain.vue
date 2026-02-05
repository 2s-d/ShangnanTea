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
let mouseX = -1000
let mouseY = -1000

const characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%^&*()_+-=[]{}|;:,.<>?/~`'

class Column {
  constructor(x, index) {
    this.x = x
    this.baseX = x // 记录原始X位置
    this.index = index
    this.y = Math.random() * -100
    this.speed = Math.random() * 2 + 1
    this.baseSpeed = this.speed
    this.chars = []
    this.length = Math.floor(Math.random() * 20) + 10
    this.speedPhase = Math.random() * Math.PI * 2 // 速度波动的相位
    this.speedFrequency = Math.random() * 0.02 + 0.01 // 速度变化频率
    this.offsetX = 0 // X轴偏移量
    
    for (let i = 0; i < this.length; i++) {
      this.chars.push(characters[Math.floor(Math.random() * characters.length)])
    }
  }

  update() {
    // 使用sin波动让速度变化（在基础速度的0.7-1.3倍之间波动）
    this.speedPhase += this.speedFrequency
    const speedMultiplier = 1 + Math.sin(this.speedPhase) * 0.3
    this.speed = this.baseSpeed * speedMultiplier
    
    // 鼠标退让效果 - 计算到鼠标的距离
    const distToMouse = Math.sqrt(
      Math.pow(this.x - mouseX, 2) + 
      Math.pow(this.y - mouseY, 2)
    )
    
    // 在100px范围内产生轻微退让
    if (distToMouse < 100) {
      const force = (100 - distToMouse) / 100
      const angle = Math.atan2(this.y - mouseY, this.x - mouseX)
      // 轻微的横向偏移（最大5px）
      this.offsetX += Math.cos(angle) * force * 0.3
    }
    
    // 偏移量逐渐回归原位
    this.offsetX *= 0.95
    
    // 限制最大偏移
    this.offsetX = Math.max(-15, Math.min(15, this.offsetX))
    
    // 更新实际X位置
    this.x = this.baseX + this.offsetX
    
    this.y += this.speed
    
    if (this.y > canvas.value.height + this.length * fontSize) {
      this.y = Math.random() * -100
      this.baseSpeed = Math.random() * 2 + 1
      this.speed = this.baseSpeed
      this.speedPhase = Math.random() * Math.PI * 2
      this.speedFrequency = Math.random() * 0.02 + 0.01
      this.offsetX = 0
      this.x = this.baseX
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
        // 发光字符 - 更亮的发光效果
        const elapsed = Date.now() - glowData.startTime
        const progress = elapsed / 1500
        const glowIntensity = Math.sin(progress * Math.PI) // 0-1-0的脉冲
        
        // 更强的亮度
        const brightness = 220 + glowIntensity * 35
        ctx.fillStyle = `rgba(${brightness}, 255, ${brightness}, ${opacity})`
        ctx.shadowBlur = 25 + glowIntensity * 25
        ctx.shadowColor = `rgba(100, 255, 100, ${glowIntensity})`
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
    columns.push(new Column(i * fontSize, i))
  }
}

const animate = () => {
  if (!ctx || !canvas.value || !props.show) return

  // 半透明黑色背景，产生拖尾效果
  ctx.fillStyle = 'rgba(0, 0, 0, 0.05)'
  ctx.fillRect(0, 0, canvas.value.width, canvas.value.height)

  // 全局随机触发字符发光（每帧约1%概率）
  if (Math.random() > 0.99 && columns.length > 0) {
    const randomColumnIndex = Math.floor(Math.random() * columns.length)
    const randomColumn = columns[randomColumnIndex]
    const randomCharIndex = Math.floor(Math.random() * randomColumn.length)
    
    // 确保不重复添加
    const exists = glowingChars.some(g => 
      g.columnIndex === randomColumnIndex && g.charIndex === randomCharIndex
    )
    
    if (!exists) {
      glowingChars.push({
        columnIndex: randomColumnIndex,
        charIndex: randomCharIndex,
        startTime: Date.now()
      })
    }
  }
  
  // 清理已经发光超过1.5秒的字符
  glowingChars = glowingChars.filter(g => Date.now() - g.startTime < 1500)

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
