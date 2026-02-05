<template>
  <div class="aura-container">
    <canvas ref="canvas" class="aura-canvas"></canvas>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const canvas = ref(null)
let ctx = null
let animationId = null
let particles = []
let isActive = false
let cycleTimer = null

class Particle {
  constructor(x, y) {
    this.x = x
    this.y = y
    this.size = Math.random() * 4 + 2
    this.speedY = -(Math.random() * 2 + 1)
    this.speedX = (Math.random() - 0.5) * 1.5
    this.opacity = 1
    this.life = 1
    this.decay = Math.random() * 0.01 + 0.005
    this.color = this.getRandomColor()
  }
  
  getRandomColor() {
    const colors = [
      'rgba(100, 200, 255, ',  // 青色
      'rgba(150, 220, 255, ',  // 浅蓝
      'rgba(200, 230, 255, ',  // 白蓝
      'rgba(180, 210, 255, ',  // 淡蓝
    ]
    return colors[Math.floor(Math.random() * colors.length)]
  }
  
  update() {
    this.y += this.speedY
    this.x += this.speedX
    this.life -= this.decay
    this.opacity = this.life
    this.size *= 0.98
  }
  
  draw() {
    if (this.opacity <= 0) return
    
    ctx.save()
    ctx.globalAlpha = this.opacity
    
    // 粒子主体
    const gradient = ctx.createRadialGradient(
      this.x, this.y, 0,
      this.x, this.y, this.size
    )
    gradient.addColorStop(0, this.color + '0.8)')
    gradient.addColorStop(0.5, this.color + '0.4)')
    gradient.addColorStop(1, this.color + '0)')
    
    ctx.fillStyle = gradient
    ctx.beginPath()
    ctx.arc(this.x, this.y, this.size, 0, Math.PI * 2)
    ctx.fill()
    
    ctx.restore()
  }
  
  isDead() {
    return this.life <= 0
  }
}

const initCanvas = () => {
  if (!canvas.value) return
  
  canvas.value.width = 300
  canvas.value.height = 300
  ctx = canvas.value.getContext('2d')
}

const createParticles = () => {
  if (!isActive) return
  
  // 从头像底部周围创建粒子
  const centerX = 150
  const centerY = 200
  const radius = 80
  
  for (let i = 0; i < 3; i++) {
    const angle = Math.random() * Math.PI - Math.PI / 2
    const distance = Math.random() * radius
    const x = centerX + Math.cos(angle) * distance
    const y = centerY + Math.sin(angle) * 20
    
    particles.push(new Particle(x, y))
  }
}

const animate = () => {
  if (!ctx || !canvas.value) return
  
  ctx.clearRect(0, 0, canvas.value.width, canvas.value.height)
  
  // 创建新粒子
  if (isActive) {
    createParticles()
  }
  
  // 更新和绘制粒子
  particles = particles.filter(particle => {
    particle.update()
    particle.draw()
    return !particle.isDead()
  })
  
  animationId = requestAnimationFrame(animate)
}

const startCycle = () => {
  // 开始喷发
  isActive = true
  
  // 3-5秒后停止
  const activeTime = Math.random() * 2000 + 3000
  setTimeout(() => {
    isActive = false
    
    // 再等3-5秒后重新开始
    const waitTime = Math.random() * 2000 + 3000
    cycleTimer = setTimeout(startCycle, waitTime)
  }, activeTime)
}

onMounted(() => {
  initCanvas()
  animate()
  startCycle()
})

onUnmounted(() => {
  if (animationId) {
    cancelAnimationFrame(animationId)
  }
  if (cycleTimer) {
    clearTimeout(cycleTimer)
  }
})
</script>

<style scoped>
.aura-container {
  position: absolute;
  width: 300px;
  height: 300px;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  pointer-events: none;
  z-index: 1;
}

.aura-canvas {
  width: 100%;
  height: 100%;
}
</style>
