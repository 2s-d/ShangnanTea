<template>
  <canvas ref="canvas" class="click-effect-canvas"></canvas>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const canvas = ref(null)
let ctx = null
let particles = []
let animationFrame = null

class Particle {
  constructor(x, y) {
    this.x = x
    this.y = y
    this.size = Math.random() * 4 + 2
    this.speedX = (Math.random() - 0.5) * 4
    this.speedY = (Math.random() - 0.5) * 4
    this.life = 1
    this.decay = Math.random() * 0.015 + 0.01
    this.rotation = Math.random() * Math.PI * 2
    this.rotationSpeed = (Math.random() - 0.5) * 0.2
    
    // 冰晶形状类型
    this.type = Math.floor(Math.random() * 3)
    
    // 颜色 - 冰蓝色系
    const colors = [
      'rgba(173, 216, 230, ',  // 浅蓝
      'rgba(135, 206, 250, ',  // 天蓝
      'rgba(176, 224, 230, ',  // 粉蓝
      'rgba(240, 248, 255, ',  // 爱丽丝蓝
      'rgba(230, 230, 250, ',  // 淡紫
    ]
    this.color = colors[Math.floor(Math.random() * colors.length)]
  }
  
  update() {
    this.x += this.speedX
    this.y += this.speedY
    this.speedY += 0.1 // 重力
    this.life -= this.decay
    this.rotation += this.rotationSpeed
    this.size *= 0.98
  }
  
  draw(ctx) {
    ctx.save()
    ctx.globalAlpha = this.life
    ctx.translate(this.x, this.y)
    ctx.rotate(this.rotation)
    
    if (this.type === 0) {
      // 六角星形冰晶
      this.drawSnowflake(ctx)
    } else if (this.type === 1) {
      // 圆形冰珠
      this.drawIceBall(ctx)
    } else {
      // 菱形冰晶
      this.drawDiamond(ctx)
    }
    
    ctx.restore()
  }
  
  drawSnowflake(ctx) {
    const arms = 6
    const size = this.size
    
    ctx.strokeStyle = this.color + this.life + ')'
    ctx.fillStyle = this.color + (this.life * 0.3) + ')'
    ctx.lineWidth = 1.5
    
    for (let i = 0; i < arms; i++) {
      ctx.save()
      ctx.rotate((Math.PI * 2 * i) / arms)
      
      // 主臂
      ctx.beginPath()
      ctx.moveTo(0, 0)
      ctx.lineTo(0, -size)
      ctx.stroke()
      
      // 分支
      ctx.beginPath()
      ctx.moveTo(0, -size * 0.6)
      ctx.lineTo(-size * 0.3, -size * 0.8)
      ctx.moveTo(0, -size * 0.6)
      ctx.lineTo(size * 0.3, -size * 0.8)
      ctx.stroke()
      
      ctx.restore()
    }
  }
  
  drawIceBall(ctx) {
    const gradient = ctx.createRadialGradient(0, 0, 0, 0, 0, this.size)
    gradient.addColorStop(0, this.color + this.life + ')')
    gradient.addColorStop(0.5, this.color + (this.life * 0.5) + ')')
    gradient.addColorStop(1, this.color + '0)')
    
    ctx.fillStyle = gradient
    ctx.beginPath()
    ctx.arc(0, 0, this.size, 0, Math.PI * 2)
    ctx.fill()
    
    // 高光
    ctx.fillStyle = 'rgba(255, 255, 255, ' + (this.life * 0.6) + ')'
    ctx.beginPath()
    ctx.arc(-this.size * 0.3, -this.size * 0.3, this.size * 0.4, 0, Math.PI * 2)
    ctx.fill()
  }
  
  drawDiamond(ctx) {
    ctx.strokeStyle = this.color + this.life + ')'
    ctx.fillStyle = this.color + (this.life * 0.4) + ')'
    ctx.lineWidth = 1.5
    
    ctx.beginPath()
    ctx.moveTo(0, -this.size)
    ctx.lineTo(this.size * 0.6, 0)
    ctx.lineTo(0, this.size)
    ctx.lineTo(-this.size * 0.6, 0)
    ctx.closePath()
    ctx.fill()
    ctx.stroke()
  }
}

const handleClick = (e) => {
  const rect = canvas.value.getBoundingClientRect()
  const x = e.clientX - rect.left
  const y = e.clientY - rect.top
  
  console.log('Click detected at:', x, y)
  
  // 创建15-25个粒子
  const count = Math.floor(Math.random() * 10) + 15
  for (let i = 0; i < count; i++) {
    particles.push(new Particle(x, y))
  }
  
  console.log('Particles created:', particles.length)
}

const animate = () => {
  ctx.clearRect(0, 0, canvas.value.width, canvas.value.height)
  
  // 更新和绘制粒子
  for (let i = particles.length - 1; i >= 0; i--) {
    particles[i].update()
    particles[i].draw(ctx)
    
    // 移除死亡粒子
    if (particles[i].life <= 0) {
      particles.splice(i, 1)
    }
  }
  
  animationFrame = requestAnimationFrame(animate)
}

const resizeCanvas = () => {
  if (canvas.value) {
    canvas.value.width = window.innerWidth
    canvas.value.height = window.innerHeight
  }
}

onMounted(() => {
  console.log('ClickEffect mounted')
  ctx = canvas.value.getContext('2d')
  resizeCanvas()
  animate()
  
  console.log('Canvas size:', canvas.value.width, canvas.value.height)
  
  window.addEventListener('click', handleClick)
  window.addEventListener('resize', resizeCanvas)
})

onUnmounted(() => {
  if (animationFrame) {
    cancelAnimationFrame(animationFrame)
  }
  window.removeEventListener('click', handleClick)
  window.removeEventListener('resize', resizeCanvas)
})
</script>

<style scoped>
.click-effect-canvas {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 10000;
}
</style>
