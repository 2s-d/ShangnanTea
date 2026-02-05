<template>
  <canvas ref="canvas" class="starry-canvas"></canvas>
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
let stars = []
let shootingStars = []
let nebulae = []
let planets = []
let constellations = []
let mouseX = 0
let mouseY = 0
let lastMouseX = 0
let lastMouseY = 0
let mouseSpeed = 0
let timeScale = 1 // 时间缩放因子

// 星云类
class Nebula {
  constructor() {
    this.x = Math.random() * canvas.value.width
    this.y = Math.random() * canvas.value.height
    this.radius = Math.random() * 150 + 100
    this.color = [
      'rgba(138, 43, 226, ',   // 紫色
      'rgba(75, 0, 130, ',     // 靛蓝
      'rgba(0, 100, 200, ',    // 深蓝
      'rgba(100, 50, 150, ',   // 紫罗兰
    ][Math.floor(Math.random() * 4)]
    this.opacity = Math.random() * 0.15 + 0.05
    this.pulsePhase = Math.random() * Math.PI * 2
    this.pulseSpeed = Math.random() * 0.005 + 0.002
  }
  
  update() {
    this.pulsePhase += this.pulseSpeed
  }
  
  draw() {
    const pulse = Math.sin(this.pulsePhase) * 0.3 + 0.7
    const gradient = ctx.createRadialGradient(
      this.x, this.y, 0,
      this.x, this.y, this.radius * pulse
    )
    gradient.addColorStop(0, this.color + (this.opacity * pulse) + ')')
    gradient.addColorStop(0.5, this.color + (this.opacity * pulse * 0.5) + ')')
    gradient.addColorStop(1, this.color + '0)')
    
    ctx.fillStyle = gradient
    ctx.fillRect(
      this.x - this.radius * pulse,
      this.y - this.radius * pulse,
      this.radius * 2 * pulse,
      this.radius * 2 * pulse
    )
  }
}

// 行星类
class Planet {
  constructor() {
    this.x = Math.random() * canvas.value.width
    this.y = Math.random() * canvas.value.height
    this.radius = Math.random() * 30 + 20
    this.speed = Math.random() * 0.1 + 0.05
    this.angle = Math.random() * Math.PI * 2
    this.orbitRadius = Math.random() * 100 + 50
    this.centerX = this.x
    this.centerY = this.y
    this.colors = [
      ['#ff6b6b', '#ee5a6f'],  // 红色星球
      ['#4ecdc4', '#44a8a0'],  // 青色星球
      ['#ffe66d', '#f9ca24'],  // 黄色星球
      ['#a8e6cf', '#7fb3d5'],  // 绿蓝星球
    ]
    this.colorPair = this.colors[Math.floor(Math.random() * this.colors.length)]
  }
  
  update() {
    this.angle += this.speed * 0.01
    this.x = this.centerX + Math.cos(this.angle) * this.orbitRadius
    this.y = this.centerY + Math.sin(this.angle) * this.orbitRadius
  }
  
  draw() {
    // 行星主体
    const gradient = ctx.createRadialGradient(
      this.x - this.radius * 0.3,
      this.y - this.radius * 0.3,
      0,
      this.x,
      this.y,
      this.radius
    )
    gradient.addColorStop(0, this.colorPair[0])
    gradient.addColorStop(1, this.colorPair[1])
    
    ctx.fillStyle = gradient
    ctx.beginPath()
    ctx.arc(this.x, this.y, this.radius, 0, Math.PI * 2)
    ctx.fill()
    
    // 光晕
    const glowGradient = ctx.createRadialGradient(
      this.x, this.y, this.radius,
      this.x, this.y, this.radius * 1.5
    )
    glowGradient.addColorStop(0, this.colorPair[0] + '40')
    glowGradient.addColorStop(1, this.colorPair[0] + '00')
    
    ctx.fillStyle = glowGradient
    ctx.beginPath()
    ctx.arc(this.x, this.y, this.radius * 1.5, 0, Math.PI * 2)
    ctx.fill()
  }
}

// 星星类 - 多层视差
class Star {
  constructor(layer = 1) {
    this.layer = layer // 1-3, 1最远，3最近
    this.reset()
    this.y = Math.random() * canvas.value.height
    this.opacity = Math.random()
  }
  
  reset() {
    this.x = Math.random() * canvas.value.width
    this.y = Math.random() * canvas.value.height
    this.size = (Math.random() * 1.5 + 0.3) * this.layer
    this.opacity = Math.random() * 0.5 + 0.5
    this.twinkleSpeed = Math.random() * 0.02 + 0.005
    this.twinklePhase = Math.random() * Math.PI * 2
    this.baseX = this.x
    this.baseY = this.y
  }
  
  update() {
    this.twinklePhase += this.twinkleSpeed
    this.opacity = 0.3 + Math.sin(this.twinklePhase) * 0.7
    
    // 视差效果
    const parallaxStrength = this.layer * 0.02
    this.x = this.baseX + (mouseX - canvas.value.width / 2) * parallaxStrength
    this.y = this.baseY + (mouseY - canvas.value.height / 2) * parallaxStrength
  }
  
  draw() {
    ctx.save()
    ctx.globalAlpha = this.opacity
    
    // 星星颜色 - 不同层有不同色调
    const colors = [
      '#ffffff',
      '#e8f4ff',
      '#fff4e8',
    ]
    ctx.fillStyle = colors[this.layer - 1]
    
    // 十字星芒效果（只对较大的星星）
    if (this.size > 1.5 && this.opacity > 0.7) {
      ctx.strokeStyle = colors[this.layer - 1]
      ctx.lineWidth = 0.5
      ctx.globalAlpha = this.opacity * 0.5
      
      ctx.beginPath()
      ctx.moveTo(this.x - this.size * 2, this.y)
      ctx.lineTo(this.x + this.size * 2, this.y)
      ctx.stroke()
      
      ctx.beginPath()
      ctx.moveTo(this.x, this.y - this.size * 2)
      ctx.lineTo(this.x, this.y + this.size * 2)
      ctx.stroke()
      
      ctx.globalAlpha = this.opacity
    }
    
    // 星星主体
    ctx.beginPath()
    ctx.arc(this.x, this.y, this.size, 0, Math.PI * 2)
    ctx.fill()
    
    // 发光
    const gradient = ctx.createRadialGradient(this.x, this.y, 0, this.x, this.y, this.size * 4)
    gradient.addColorStop(0, colors[this.layer - 1] + '80')
    gradient.addColorStop(1, colors[this.layer - 1] + '00')
    ctx.fillStyle = gradient
    ctx.beginPath()
    ctx.arc(this.x, this.y, this.size * 4, 0, Math.PI * 2)
    ctx.fill()
    
    ctx.restore()
  }
}

// 流星类 - 增强版
class ShootingStar {
  constructor() {
    this.reset()
  }
  
  reset() {
    this.x = Math.random() * canvas.value.width
    this.y = Math.random() * canvas.value.height * 0.4
    this.length = Math.random() * 100 + 60
    this.speed = Math.random() * 10 + 8
    this.angle = Math.PI / 4 + (Math.random() - 0.5) * 0.8
    this.angleVelocity = (Math.random() - 0.5) * 0.003 // 角度变化速度，产生弧度
    this.opacity = 1
    this.tail = []
    this.color = [
      'rgba(255, 255, 255, ',
      'rgba(200, 220, 255, ',
      'rgba(255, 240, 200, ',
    ][Math.floor(Math.random() * 3)]
    
    for (let i = 0; i < 20; i++) {
      this.tail.push({ x: this.x, y: this.y })
    }
  }
  
  update() {
    this.tail.unshift({ x: this.x, y: this.y })
    if (this.tail.length > 25) {
      this.tail.pop()
    }
    
    // 应用时间缩放（鼠标速度影响）
    const effectiveSpeed = this.speed * timeScale
    
    // 角度逐渐变化，产生弧线轨迹
    this.angle += this.angleVelocity * timeScale
    
    this.x += Math.cos(this.angle) * effectiveSpeed
    this.y += Math.sin(this.angle) * effectiveSpeed
    this.opacity -= 0.008 * timeScale
    
    if (this.x > canvas.value.width + 100 || this.y > canvas.value.height + 100 || this.opacity <= 0) {
      // 随机决定是否立即重置（控制流星频率）
      if (Math.random() > 0.95) {
        this.reset()
      }
    }
  }
  
  draw() {
    if (this.opacity <= 0) return
    
    ctx.save()
    
    // 绘制尾迹
    for (let i = 0; i < this.tail.length - 1; i++) {
      const point = this.tail[i]
      const nextPoint = this.tail[i + 1]
      const alpha = (1 - i / this.tail.length) * this.opacity
      const width = (1 - i / this.tail.length) * 3
      
      ctx.strokeStyle = this.color + alpha + ')'
      ctx.lineWidth = width
      ctx.lineCap = 'round'
      
      ctx.beginPath()
      ctx.moveTo(point.x, point.y)
      ctx.lineTo(nextPoint.x, nextPoint.y)
      ctx.stroke()
    }
    
    // 头部光晕
    ctx.globalAlpha = this.opacity
    const headGradient = ctx.createRadialGradient(this.x, this.y, 0, this.x, this.y, 8)
    headGradient.addColorStop(0, this.color + '1)')
    headGradient.addColorStop(0.3, this.color + '0.8)')
    headGradient.addColorStop(1, this.color + '0)')
    
    ctx.fillStyle = headGradient
    ctx.beginPath()
    ctx.arc(this.x, this.y, 8, 0, Math.PI * 2)
    ctx.fill()
    
    ctx.restore()
  }
}

const initCanvas = () => {
  if (!canvas.value) return
  
  canvas.value.width = window.innerWidth
  canvas.value.height = window.innerHeight
  ctx = canvas.value.getContext('2d')
  
  // 创建星云
  nebulae = []
  for (let i = 0; i < 5; i++) {
    nebulae.push(new Nebula())
  }
  
  // 创建行星
  planets = []
  for (let i = 0; i < 3; i++) {
    planets.push(new Planet())
  }
  
  // 创建多层星星
  stars = []
  for (let layer = 1; layer <= 3; layer++) {
    const count = layer === 1 ? 150 : layer === 2 ? 100 : 50
    for (let i = 0; i < count; i++) {
      stars.push(new Star(layer))
    }
  }
  
  // 创建流星
  shootingStars = []
  for (let i = 0; i < 8; i++) {
    shootingStars.push(new ShootingStar())
  }
}

const animate = () => {
  if (!ctx || !canvas.value || !props.show) return
  
  // 计算时间缩放：鼠标速度越快，时间越慢
  // mouseSpeed范围大约0-50，映射到timeScale 1.0-0.2
  const maxMouseSpeed = 50
  const minTimeScale = 0.2
  const speedFactor = Math.min(mouseSpeed / maxMouseSpeed, 1)
  timeScale = 1 - speedFactor * (1 - minTimeScale)
  
  // 鼠标速度衰减
  mouseSpeed *= 0.95
  
  // 渐变背景 - 深空色
  const bgGradient = ctx.createLinearGradient(0, 0, 0, canvas.value.height)
  bgGradient.addColorStop(0, '#000814')
  bgGradient.addColorStop(0.5, '#001d3d')
  bgGradient.addColorStop(1, '#003566')
  ctx.fillStyle = bgGradient
  ctx.fillRect(0, 0, canvas.value.width, canvas.value.height)
  
  // 绘制星云
  nebulae.forEach(nebula => {
    nebula.update()
    nebula.draw()
  })
  
  // 绘制星星（按层级）
  stars.forEach(star => {
    star.update()
    star.draw()
  })
  
  // 绘制行星
  planets.forEach(planet => {
    planet.update()
    planet.draw()
  })
  
  // 绘制流星
  shootingStars.forEach(star => {
    star.update()
    star.draw()
  })
  
  animationId = requestAnimationFrame(animate)
}

const handleResize = () => {
  initCanvas()
}

const handleMouseMove = (e) => {
  const newMouseX = e.clientX
  const newMouseY = e.clientY
  
  // 计算鼠标移动距离
  const deltaX = newMouseX - lastMouseX
  const deltaY = newMouseY - lastMouseY
  const distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY)
  
  // 更新鼠标速度（使用指数移动平均平滑）
  mouseSpeed = mouseSpeed * 0.8 + distance * 0.2
  
  lastMouseX = newMouseX
  lastMouseY = newMouseY
  mouseX = newMouseX
  mouseY = newMouseY
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
})

onUnmounted(() => {
  if (animationId) {
    cancelAnimationFrame(animationId)
  }
  window.removeEventListener('resize', handleResize)
  window.removeEventListener('mousemove', handleMouseMove)
})
</script>

<style scoped>
.starry-canvas {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
}
</style>
