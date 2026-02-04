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
let lastMouseX = -1000
let lastMouseY = -1000
let lastMouseMoveTime = 0
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

  // 根据密度计算系数（多段平滑过渡）
  getDensityFactor(nearbyCount) {
    // 分段密度系数，更平滑的过渡
    if (nearbyCount <= 2) {
      // 极低密度：0.25-0.35（非常快速消失）
      return 0.25 + nearbyCount * 0.05
    } else if (nearbyCount <= 5) {
      // 低密度：0.35-0.50
      return 0.35 + (nearbyCount - 2) * 0.05
    } else if (nearbyCount <= 10) {
      // 中低密度：0.50-0.70
      return 0.50 + (nearbyCount - 5) * 0.04
    } else if (nearbyCount <= 15) {
      // 中高密度：0.70-0.85
      return 0.70 + (nearbyCount - 10) * 0.03
    } else if (nearbyCount <= 20) {
      // 高密度：0.85-0.95
      return 0.85 + (nearbyCount - 15) * 0.02
    } else {
      // 极高密度：0.95-1.0
      return Math.min(0.95 + (nearbyCount - 20) * 0.01, 1.0)
    }
  }

  // 计算周围亮着的六边形数量（密度检测）
  countNearbyActive(allHexagons, radius = 50) {
    let count = 0
    for (const hex of allHexagons) {
      if (hex === this || hex.opacity < 0.1) continue
      
      const dx = this.x - hex.x
      const dy = this.y - hex.y
      const dist = Math.sqrt(dx * dx + dy * dy)
      
      if (dist < radius) {
        count++
      }
    }
    return count
  }

  // 更新透明度
  update(mx, my, currentTime, allHexagons, isMouseMoving) {
    // 检查鼠标当前位置
    const distToCurrent = this.distanceToMouse(mx, my)
    
    // 只有在鼠标移动时才点亮新的六边形
    if (isMouseMoving && distToCurrent < 80) {
      const intensity = 1 - (distToCurrent / 80)
      // 适度降低基础亮度：从0.7改为0.55
      this.opacity = Math.max(this.opacity, intensity * 0.55)
      this.lastActivatedTime = currentTime
    }
    
    // 拖尾衰减逻辑：结合时间、距离和密度
    if (this.lastActivatedTime > 0 && this.opacity > 0.01) {
      const timeSinceActivation = currentTime - this.lastActivatedTime
      
      // 计算周围密度
      const nearbyCount = this.countNearbyActive(allHexagons, 50)
      
      // 使用多段密度系数
      const densityFactor = this.getDensityFactor(nearbyCount)
      
      // 基础存活时间：根据密度大幅调整
      // 极短拖尾：0.625秒，长拖尾：2.5秒
      const baseLifetime = 2500 * densityFactor
      
      // 根据距离计算延迟消失时间
      const distanceDelay = Math.max(0, (300 - distToCurrent) / 50 * 200) * densityFactor
      
      // 实际开始消失的时间
      const effectiveLifetime = baseLifetime + distanceDelay
      
      if (timeSinceActivation < effectiveLifetime) {
        // 还在存活期内
        const fadeProgress = timeSinceActivation / effectiveLifetime
        
        // 基础衰减（根据密度分段调整）
        let baseDensityDecay
        if (densityFactor < 0.4) {
          baseDensityDecay = 0.985  // 极低密度：快速衰减
        } else if (densityFactor < 0.6) {
          baseDensityDecay = 0.990  // 低密度：中快衰减
        } else if (densityFactor < 0.8) {
          baseDensityDecay = 0.995  // 中密度：中等衰减
        } else {
          baseDensityDecay = 0.998  // 高密度：慢速衰减
        }
        this.opacity *= baseDensityDecay
        
        // 根据进度和距离加速衰减
        if (fadeProgress > 0.3) {
          const distanceFactor = Math.max(0, distToCurrent - 80) / 200
          const progressFactor = (fadeProgress - 0.3) / 0.7
          
          // 密度低的大幅加速衰减（分段）
          let densityDecayBoost
          if (densityFactor < 0.4) {
            densityDecayBoost = 0.035  // 极低密度：大幅加速
          } else if (densityFactor < 0.6) {
            densityDecayBoost = 0.025  // 低密度：中等加速
          } else if (densityFactor < 0.8) {
            densityDecayBoost = 0.015  // 中密度：轻微加速
          } else {
            densityDecayBoost = 0.005  // 高密度：几乎不加速
          }
          
          const extraDecay = (distanceFactor * progressFactor * 0.015) + densityDecayBoost
          this.opacity *= (1 - extraDecay)
          
          // 最后阶段（70%之后）大幅加速消失
          if (fadeProgress > 0.7) {
            const finalStageProgress = (fadeProgress - 0.7) / 0.3
            // 使用平方函数让最后阶段快速消失
            const finalDecay = finalStageProgress * finalStageProgress * 0.08
            this.opacity *= (1 - finalDecay)
          }
        }
        
        // 变窄效果：在后半段且距离较远时
        if (fadeProgress > 0.5 && distToCurrent > 80) {
          const narrowProgress = (fadeProgress - 0.5) * 2
          const excessDistance = distToCurrent - 80
          
          if (excessDistance < 120) {
            const narrowFactor = 1 - Math.sqrt(narrowProgress) * 0.6
            const narrowedRadius = 80 + excessDistance * narrowFactor
            
            if (distToCurrent > narrowedRadius) {
              const edgeFactor = (distToCurrent - narrowedRadius) / 30
              this.opacity *= (1 - Math.min(edgeFactor, 1) * 0.02)
            }
          }
        }
        
      } else {
        // 超过存活时间，快速消失
        this.opacity *= 0.88
      }
      
      // 偶尔闪烁（非常稀疏）- 闪烁时保持原亮度
      if (this.opacity > 0.1 && this.opacity < 0.5 && distToCurrent > 80) {
        const sparklePhase = (currentTime + this.sparkleOffset) * this.sparkleSpeed * 0.003
        const sparkle = Math.sin(sparklePhase) * 0.5 + 0.5
        
        if (sparkle > 0.98 && Math.random() > 0.97) {
          // 闪烁时提升到更高亮度
          this.opacity = Math.min(this.opacity * 3.0, 0.9)
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
    
    // 半透明填充，适度减暗（从0.2改为0.17）
    ctx.fillStyle = `rgba(99, 102, 241, ${this.opacity * 0.17})`
    ctx.fill()

    // 绘制边框，适度减暗（从0.6改为0.5）
    ctx.strokeStyle = `rgba(99, 102, 241, ${this.opacity * 0.5})`
    ctx.lineWidth = 0.8
    ctx.stroke()

    // 高亮时的发光效果（保持不变，让闪烁更明显）
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
  // 使用文档高度而不是视口高度
  canvas.value.height = Math.max(
    document.documentElement.scrollHeight,
    document.body.scrollHeight,
    window.innerHeight
  )
  ctx = canvas.value.getContext('2d')

  hexagons = []
  
  // 更小更密集的六边形
  const sizes = [4, 5, 6, 7]  // 从[5,6,7,8]改为[4,5,6,7]
  const hexWidth = 14  // 从16改为14，更密集
  const hexHeight = 12  // 从14改为12，更密集

  // 创建六边形网格（覆盖整个文档高度）
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

// 绘制鼠标光晕效果
const drawMouseGlow = (mx, my, isMoving) => {
  if (!isMoving || mx < 0 || my < 0) return
  
  // 绘制多层渐变光晕
  const glowLayers = [
    { radius: 60, opacity: 0.15 },
    { radius: 40, opacity: 0.25 },
    { radius: 20, opacity: 0.35 }
  ]
  
  glowLayers.forEach(layer => {
    const gradient = ctx.createRadialGradient(mx, my, 0, mx, my, layer.radius)
    gradient.addColorStop(0, `rgba(99, 102, 241, ${layer.opacity})`)
    gradient.addColorStop(0.5, `rgba(99, 102, 241, ${layer.opacity * 0.5})`)
    gradient.addColorStop(1, 'rgba(99, 102, 241, 0)')
    
    ctx.fillStyle = gradient
    ctx.beginPath()
    ctx.arc(mx, my, layer.radius, 0, Math.PI * 2)
    ctx.fill()
  })
  
  // 中心亮点
  ctx.fillStyle = 'rgba(139, 142, 255, 0.6)'
  ctx.beginPath()
  ctx.arc(mx, my, 4, 0, Math.PI * 2)
  ctx.fill()
  
  // 外圈光环
  ctx.strokeStyle = 'rgba(99, 102, 241, 0.4)'
  ctx.lineWidth = 2
  ctx.beginPath()
  ctx.arc(mx, my, 8, 0, Math.PI * 2)
  ctx.stroke()
}

// 动画循环
const animate = () => {
  if (!ctx || !canvas.value) return

  const currentTime = performance.now()
  
  // 检测鼠标是否在移动（100ms内有移动视为正在移动）
  const timeSinceLastMove = currentTime - lastMouseMoveTime
  const isMouseMoving = timeSinceLastMove < 100

  // 清空画布
  ctx.clearRect(0, 0, canvas.value.width, canvas.value.height)

  // 更新和绘制六边形
  hexagons.forEach(hex => {
    hex.update(mouseX, mouseY, currentTime, hexagons, isMouseMoving)
    hex.draw()
  })
  
  // 绘制鼠标光晕（在六边形之上）
  drawMouseGlow(mouseX, mouseY, isMouseMoving)

  animationId = requestAnimationFrame(animate)
}

// 鼠标移动事件
const handleMouseMove = (e) => {
  // 考虑页面滚动偏移
  const scrollY = window.pageYOffset || document.documentElement.scrollTop
  const scrollX = window.pageXOffset || document.documentElement.scrollLeft
  
  const newMouseX = e.clientX + scrollX
  const newMouseY = e.clientY + scrollY
  
  // 检测鼠标位置是否真的改变了
  if (newMouseX !== lastMouseX || newMouseY !== lastMouseY) {
    mouseX = newMouseX
    mouseY = newMouseY
    lastMouseX = newMouseX
    lastMouseY = newMouseY
    lastMouseMoveTime = performance.now()
  }
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
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
  opacity: 1;
}
</style>
