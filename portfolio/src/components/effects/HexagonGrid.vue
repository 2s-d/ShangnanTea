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
let animationId = null

// 六边形类
class Hexagon {
  constructor(x, y, size) {
    this.x = x
    this.y = y
    this.size = size
    this.opacity = 0
    this.lastActivatedTime = 0 // 最后被激活的时间
    this.activationMouseX = 0 // 激活时鼠标的X位置
    this.activationMouseY = 0 // 激活时鼠标的Y位置
    this.sparkleOffset = Math.random() * 1000 // 闪烁偏移，让每个六边形闪烁时机不同
    this.sparkleSpeed = 0.5 + Math.random() * 1.5 // 闪烁速度随机
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

  // 计算到鼠标的距离
  distanceToMouse(mx, my) {
    const dx = this.x - mx
    const dy = this.y - my
    return Math.sqrt(dx * dx + dy * dy)
  }

  // 更新透明度
  update(mx, my, currentTime) {
    // 检查鼠标当前位置
    const distToCurrent = this.distanceToMouse(mx, my)
    
    // 如果在鼠标附近，点亮并记录时间
    if (distToCurrent < 80) {
      const intensity = 1 - (distToCurrent / 80)
      this.opacity = Math.max(this.opacity, intensity * 0.7)
      this.lastActivatedTime = currentTime
    }
    
    // 拖尾衰减逻辑：结合时间和距离
    if (this.lastActivatedTime > 0 && this.opacity > 0.01) {
      const timeSinceActivation = currentTime - this.lastActivatedTime
      
      // 基础存活时间：2.5秒
      const baseLifetime = 2500
      
      // 根据距离计算延迟消失时间
      // 距离越远，越早开始消失；距离越近，延迟越久
      // 每50px距离增加200ms延迟
      const distanceDelay = Math.max(0, (300 - distToCurrent) / 50 * 200)
      
      // 实际开始消失的时间 = 基础时间 + 距离延迟
      const effectiveLifetime = baseLifetime + distanceDelay
      
      if (timeSinceActivation < effectiveLifetime) {
        // 还在存活期内
        // 计算消失进度 (0-1)
        const fadeProgress = timeSinceActivation / effectiveLifetime
        
        // 基础衰减（很慢）
        this.opacity *= 0.998
        
        // 根据进度和距离加速衰减
        if (fadeProgress > 0.3) {
          // 30%之后开始根据距离加速衰减
          const distanceFactor = Math.max(0, distToCurrent - 80) / 200
          const progressFactor = (fadeProgress - 0.3) / 0.7
          
          // 距离越远且时间越久，衰减越快
          const extraDecay = distanceFactor * progressFactor * 0.015
          this.opacity *= (1 - extraDecay)
        }
        
        // 变窄效果：在后半段且距离较远时
        if (fadeProgress > 0.5 && distToCurrent > 80) {
          const narrowProgress = (fadeProgress - 0.5) * 2
          const excessDistance = distToCurrent - 80
          
          // 距离80-200px范围内逐渐变窄
          if (excessDistance < 120) {
            const narrowFactor = 1 - Math.sqrt(narrowProgress) * 0.6
            const narrowedRadius = 80 + excessDistance * narrowFactor
            
            // 在变窄边缘的六边形额外衰减
            if (distToCurrent > narrowedRadius) {
              const edgeFactor = (distToCurrent - narrowedRadius) / 30
              this.opacity *= (1 - Math.min(edgeFactor, 1) * 0.02)
            }
          }
        }
        
      } else {
        // 超过存活时间，快速消失
        this.opacity *= 0.92
      }
      
      // 偶尔闪烁（非常稀疏）
      if (this.opacity > 0.1 && this.opacity < 0.5 && distToCurrent > 80) {
        const sparklePhase = (currentTime + this.sparkleOffset) * this.sparkleSpeed * 0.003
        const sparkle = Math.sin(sparklePhase) * 0.5 + 0.5
        
        if (sparkle > 0.98 && Math.random() > 0.97) {
          this.opacity = Math.min(this.opacity * 2.5, 0.9)
        }
      }
      
      // 完全消失
      if (this.opacity < 0.01) {
        this.opacity = 0
        this.lastActivatedTime = 0
      }
    }
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
    
    // 半透明填充，不遮挡内容
    ctx.fillStyle = `rgba(99, 102, 241, ${this.opacity * 0.2})`
    ctx.fill()

    // 绘制边框
    ctx.strokeStyle = `rgba(99, 102, 241, ${this.opacity * 0.6})`
    ctx.lineWidth = 0.8
    ctx.stroke()

    // 高亮时的发光效果
    if (this.opacity > 0.5) {
      ctx.shadowBlur = 6
      ctx.shadowColor = `rgba(99, 102, 241, ${this.opacity * 0.7})`
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
  
  // 更小更密集的六边形
  const sizes = [5, 6, 7, 8]
  const hexWidth = 16 // 更密集
  const hexHeight = 14

  // 创建六边形网格
  for (let row = 0; row < canvas.value.height / hexHeight + 2; row++) {
    for (let col = 0; col < canvas.value.width / hexWidth + 2; col++) {
      const x = col * hexWidth + (row % 2) * (hexWidth / 2)
      const y = row * hexHeight
      
      // 随机选择大小
      const sizeIndex = Math.random() < 0.95 ? 
        Math.floor(Math.random() * 2) : // 95%是最小的
        Math.floor(Math.random() * sizes.length)
      
      const size = sizes[sizeIndex]
      
      hexagons.push(new Hexagon(x, y, size))
    }
  }
}

// 动画循环
const animate = () => {
  if (!ctx || !canvas.value) return

  const currentTime = performance.now()

  // 清空画布
  ctx.clearRect(0, 0, canvas.value.width, canvas.value.height)

  // 更新和绘制六边形
  hexagons.forEach(hex => {
    hex.update(mouseX, mouseY, currentTime)
    hex.draw()
  })

  animationId = requestAnimationFrame(animate)
}

// 鼠标移动事件
const handleMouseMove = (e) => {
  mouseX = e.clientX
  mouseY = e.clientY
}

// 鼠标离开事件
const handleMouseLeave = () => {
  mouseX = -1000
  mouseY = -1000
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
  opacity: 1;
}
</style>
