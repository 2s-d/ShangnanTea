<template>
  <canvas ref="canvas" class="hexagon-grid-canvas"></canvas>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const canvas = ref(null)
let ctx = null
let hexagons = []
let mouseX = -1000
let mouseY = -1000
let mouseTrail = [] // 鼠标拖尾
let animationId = null

// 六边形类
class Hexagon {
  constructor(x, y, size) {
    this.x = x
    this.y = y
    this.size = size
    this.opacity = 0
    this.targetOpacity = 0
  }

  // 计算六边形的顶点
  getVertices() {
    const vertices = []
    for (let i = 0; i < 6; i++) {
      const angle = (Math.PI / 3) * i
      vertices.push({
        x: this.x + this.size * Math.cos(angle),
        y: this.y + this.size * Math.sin(angle)
      })
    }
    return vertices
  }

  // 检查点是否在六边形内
  containsPoint(px, py) {
    const dx = px - this.x
    const dy = py - this.y
    return Math.sqrt(dx * dx + dy * dy) < this.size
  }

  // 计算到鼠标的距离
  distanceToMouse(mx, my) {
    const dx = this.x - mx
    const dy = this.y - my
    return Math.sqrt(dx * dx + dy * dy)
  }

  // 更新透明度
  update(mx, my, trail) {
    let maxOpacity = 0

    // 检查鼠标当前位置
    const distToCurrent = this.distanceToMouse(mx, my)
    if (distToCurrent < 150) {
      const opacity = 1 - (distToCurrent / 150)
      maxOpacity = Math.max(maxOpacity, opacity)
    }

    // 检查鼠标拖尾
    trail.forEach((point, index) => {
      const dist = this.distanceToMouse(point.x, point.y)
      if (dist < 150) {
        const trailFade = 1 - (index / trail.length) // 拖尾逐渐变淡
        const opacity = (1 - (dist / 150)) * trailFade * 0.6
        maxOpacity = Math.max(maxOpacity, opacity)
      }
    })

    this.targetOpacity = maxOpacity

    // 平滑过渡
    this.opacity += (this.targetOpacity - this.opacity) * 0.15
  }

  // 绘制六边形
  draw() {
    if (this.opacity < 0.01) return

    const vertices = this.getVertices()
    
    // 绘制填充
    ctx.beginPath()
    ctx.moveTo(vertices[0].x, vertices[0].y)
    for (let i = 1; i < vertices.length; i++) {
      ctx.lineTo(vertices[i].x, vertices[i].y)
    }
    ctx.closePath()
    
    // 渐变填充
    const gradient = ctx.createRadialGradient(
      this.x, this.y, 0,
      this.x, this.y, this.size
    )
    gradient.addColorStop(0, `rgba(99, 102, 241, ${this.opacity * 0.3})`)
    gradient.addColorStop(1, `rgba(99, 102, 241, ${this.opacity * 0.1})`)
    ctx.fillStyle = gradient
    ctx.fill()

    // 绘制边框
    ctx.strokeStyle = `rgba(99, 102, 241, ${this.opacity * 0.8})`
    ctx.lineWidth = 1.5
    ctx.stroke()

    // 绘制发光效果
    if (this.opacity > 0.5) {
      ctx.shadowBlur = 15
      ctx.shadowColor = `rgba(99, 102, 241, ${this.opacity})`
      ctx.stroke()
      ctx.shadowBlur = 0
    }
  }
}

// 初始化六边形网格
const initHexagons = () => {
  if (!canvas.value) return

  canvas.value.width = window.innerWidth
  canvas.value.height = window.innerHeight
  ctx = canvas.value.getContext('2d')

  hexagons = []
  
  // 六边形大小（混合大小）
  const sizes = [20, 25, 30, 35]
  const hexWidth = 60
  const hexHeight = 52

  // 创建六边形网格
  for (let row = 0; row < canvas.value.height / hexHeight + 2; row++) {
    for (let col = 0; col < canvas.value.width / hexWidth + 2; col++) {
      const x = col * hexWidth + (row % 2) * (hexWidth / 2)
      const y = row * hexHeight
      
      // 随机选择大小，偶尔出现大的
      const sizeIndex = Math.random() < 0.85 ? 
        Math.floor(Math.random() * 2) : // 大部分是小的
        Math.floor(Math.random() * sizes.length) // 偶尔大的
      
      const size = sizes[sizeIndex]
      
      hexagons.push(new Hexagon(x, y, size))
    }
  }
}

// 动画循环
const animate = () => {
  if (!ctx || !canvas.value) return

  // 清空画布
  ctx.clearRect(0, 0, canvas.value.width, canvas.value.height)

  // 更新和绘制六边形
  hexagons.forEach(hex => {
    hex.update(mouseX, mouseY, mouseTrail)
    hex.draw()
  })

  // 更新鼠标拖尾（保留最近20个位置）
  if (mouseTrail.length > 20) {
    mouseTrail.shift()
  }

  animationId = requestAnimationFrame(animate)
}

// 鼠标移动事件
const handleMouseMove = (e) => {
  mouseX = e.clientX
  mouseY = e.clientY
  
  // 添加到拖尾
  mouseTrail.push({ x: mouseX, y: mouseY })
}

// 鼠标离开事件
const handleMouseLeave = () => {
  mouseX = -1000
  mouseY = -1000
  mouseTrail = []
}

// 窗口大小改变
const handleResize = () => {
  initHexagons()
}

onMounted(() => {
  initHexagons()
  animate()
  window.addEventListener('mousemove', handleMouseMove)
  window.addEventListener('mouseleave', handleMouseLeave)
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  if (animationId) {
    cancelAnimationFrame(animationId)
  }
  window.removeEventListener('mousemove', handleMouseMove)
  window.removeEventListener('mouseleave', handleMouseLeave)
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.hexagon-grid-canvas {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
  opacity: 0.8;
}
</style>
