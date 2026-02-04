<template>
  <canvas ref="canvas" class="particles-canvas"></canvas>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const canvas = ref(null)
let ctx = null
let particles = []
let animationId = null

class Particle {
  constructor(x, y) {
    this.x = x
    this.y = y
    this.size = Math.random() * 3 + 1
    this.speedX = Math.random() * 1 - 0.5
    this.speedY = Math.random() * 1 - 0.5
    this.opacity = Math.random() * 0.5 + 0.2
  }

  update() {
    this.x += this.speedX
    this.y += this.speedY

    // 边界检测
    if (this.x < 0 || this.x > canvas.value.width) this.speedX *= -1
    if (this.y < 0 || this.y > canvas.value.height) this.speedY *= -1
  }

  draw() {
    ctx.fillStyle = `rgba(102, 126, 234, ${this.opacity})`
    ctx.beginPath()
    ctx.arc(this.x, this.y, this.size, 0, Math.PI * 2)
    ctx.fill()
  }
}

const initCanvas = () => {
  if (!canvas.value) return

  canvas.value.width = window.innerWidth
  canvas.value.height = window.innerHeight
  ctx = canvas.value.getContext('2d')

  // 创建粒子
  const particleCount = Math.floor((canvas.value.width * canvas.value.height) / 15000)
  particles = []
  for (let i = 0; i < particleCount; i++) {
    particles.push(
      new Particle(
        Math.random() * canvas.value.width,
        Math.random() * canvas.value.height
      )
    )
  }
}

const animate = () => {
  if (!ctx || !canvas.value) return

  ctx.clearRect(0, 0, canvas.value.width, canvas.value.height)

  // 更新和绘制粒子
  particles.forEach((particle) => {
    particle.update()
    particle.draw()
  })

  // 绘制连线
  particles.forEach((p1, i) => {
    particles.slice(i + 1).forEach((p2) => {
      const dx = p1.x - p2.x
      const dy = p1.y - p2.y
      const distance = Math.sqrt(dx * dx + dy * dy)

      if (distance < 120) {
        ctx.strokeStyle = `rgba(102, 126, 234, ${0.2 * (1 - distance / 120)})`
        ctx.lineWidth = 1
        ctx.beginPath()
        ctx.moveTo(p1.x, p1.y)
        ctx.lineTo(p2.x, p2.y)
        ctx.stroke()
      }
    })
  })

  animationId = requestAnimationFrame(animate)
}

const handleResize = () => {
  initCanvas()
}

onMounted(() => {
  initCanvas()
  animate()
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
.particles-canvas {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
  opacity: 0.6;
}
</style>
