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
    this.size = Math.random() * 5 + 2
    this.speedX = (Math.random() - 0.5) * 5
    this.speedY = (Math.random() - 0.5) * 5
    this.life = 1
    this.decay = Math.random() * 0.008 + 0.005
    this.rotation = Math.random() * Math.PI * 2
    this.rotationSpeed = (Math.random() - 0.5) * 0.15
    this.age = 0 // 粒子年龄
    
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
    this.age += 0.01
    
    // 时间膨胀效果：在0.3-0.6生命周期时减速（扩散到最大时）
    let timeScale = 1
    if (this.age > 0.3 && this.age < 0.6) {
      // 在这个区间内，时间流逝变慢（定格帧效果）
      const peakProgress = (this.age - 0.3) / 0.3 // 0-1
      // 使用sin函数创建平滑的减速-加速曲线
      timeScale = 0.3 + Math.sin(peakProgress * Math.PI) * 0.4 // 0.3-0.7之间
    }
    
    this.x += this.speedX * timeScale
    this.y += this.speedY * timeScale
    this.speedY += 0.08 * timeScale // 重力也受时间膨胀影响
    this.speedX *= (1 - 0.01 * timeScale) // 空气阻力
    this.speedY *= (1 - 0.01 * timeScale)
    this.life -= this.decay * timeScale
    this.rotation += this.rotationSpeed * timeScale
    this.size *= (1 - 0.005 * timeScale)
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
  
  // 创建30-45个粒子
  const count = Math.floor(Math.random() * 15) + 30
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
