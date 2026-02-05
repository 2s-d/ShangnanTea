<template>
  <div ref="wrapperRef" class="switch-wrapper">
    <!-- 绳子 - 根据物理引擎位置绘制，但高度保持固定，避免静止状态被裁剪看不见 -->
    <svg
      class="rope-svg"
      viewBox="0 0 80 230"
      preserveAspectRatio="xMidYMin slice"
    >
      <defs>
        <!-- 主体绳子渐变 -->
        <linearGradient id="ropeGradient" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stop-color="#e2b46a" />
          <stop offset="45%" stop-color="#b27a36" />
          <stop offset="100%" stop-color="#7b4a1a" />
        </linearGradient>
        <!-- 高光渐变 -->
        <linearGradient id="ropeHighlight" x1="0" y1="0" x2="1" y2="0">
          <stop offset="0%" stop-color="rgba(255,255,255,0.7)" />
          <stop offset="40%" stop-color="rgba(255,255,255,0.2)" />
          <stop offset="100%" stop-color="rgba(255,255,255,0)" />
        </linearGradient>
      </defs>

      <!-- 主体绳子 -->
      <path
        :d="ropePath"
        stroke="url(#ropeGradient)"
        stroke-width="4.5"
        fill="none"
        stroke-linecap="round"
        stroke-linejoin="round"
      />
      <!-- 侧面高光 -->
      <path
        :d="ropePath"
        stroke="url(#ropeHighlight)"
        stroke-width="2"
        fill="none"
        stroke-linecap="round"
        stroke-linejoin="round"
        opacity="0.8"
        transform="translate(-1, 0)"
      />

      <!-- 轻微外发光，提升在紫色背景下的可见度 -->
      <path
        :d="ropePath"
        stroke="rgba(255,255,255,0.2)"
        stroke-width="7"
        fill="none"
        stroke-linecap="round"
        stroke-linejoin="round"
        opacity="0.6"
      />

      <!-- 末端系结（接近骰子顶部的小结） -->
      <circle
        :cx="anchorX + offsetX"
        :cy="ropeLength"
        r="5.5"
        fill="#f5e1b5"
        stroke="#7b4a1a"
        stroke-width="2"
        opacity="0.95"
      />
      <circle
        :cx="anchorX + offsetX"
        :cy="ropeLength"
        r="2.5"
        fill="#fdf5dd"
        opacity="0.9"
      />
    </svg>
    
    <!-- 可拖拽的小方块 -->
    <div 
      class="switch-box"
      :style="boxStyle"
      @mousedown="startDrag"
      @touchstart="startDrag"
    >
      <!-- 全新的 3D 16 面骰子模型，完全替换原来的立方体 -->
      <div class="d16">
        <div
          v-for="n in 16"
          :key="n"
          class="d16-face"
          :style="d16FaceStyle(n)"
        >
          <span class="d16-num">{{ n }}</span>
        </div>
        <div class="d16-cap d16-cap-top"></div>
        <div class="d16-cap d16-cap-bottom"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { Engine, Bodies, Body, Constraint, Composite } from 'matter-js'

const props = defineProps({
  currentTheme: String,
  onToggle: Function
})

// DOM 引用
const wrapperRef = ref(null)

// 位置和物理参数（以组件内部坐标为基准）
// 将整套绳子 + 骰子在容器内部整体左移约 30px
const anchorX = -20
const anchorY = 0
// 基础绳长（静止时顶部到骰子中心的距离），适当拉长一点让视觉上更协调
const ROPE_BASE_LENGTH = 180

const offsetY = ref(0) // Y轴偏移
const offsetX = ref(0) // X轴偏移（用于晃动和左右拉动）
const isDragging = ref(false)
const dragStartY = ref(0)
const dragStartOffset = ref(0)
const dragStartX = ref(0)
const dragStartOffsetX = ref(0)

// 物理参数
const TRIGGER_DISTANCE = 80 // 触发切换的距离
const MAX_STRETCH = 220 // 最大拉伸距离

// 绳子当前长度（由物理引擎驱动）
const ropeLength = ref(ROPE_BASE_LENGTH)

// 绳子路径 - 贝塞尔曲线模拟弹性
const ropePath = computed(() => {
  const startX = anchorX
  const startY = anchorY
  const endX = anchorX + offsetX.value
  const endY = ropeLength.value
  
  // 控制点让绳子有弧度
  const controlX = startX + offsetX.value * 0.5
  const controlY = endY * 0.4
  
  return `M ${startX} ${startY} Q ${controlX} ${controlY} ${endX} ${endY}`
})

// 方块 / 骰子包裹元素样式
const boxStyle = computed(() => {
  const rotation = offsetX.value * 0.5 // 根据X偏移旋转
  const boxSize = 50
  const centerX = anchorX + offsetX.value
  const centerY = ropeLength.value
  const top = centerY - boxSize / 2
  const left = centerX - boxSize / 2
  return {
    top: `${top}px`,
    left: `${left}px`,
    transform: `rotateY(${rotation}deg) rotateX(${offsetY.value * 0.2}deg)`
  }
})

// ===== 16 面骰子：面变换样式 =====
const D16_RADIUS = 26 // 半径
const D16_HEIGHT = 40 // 高度

const d16FaceStyle = n => {
  const angle = (360 / 16) * (n - 1)
  return {
    transform: `
      rotateY(${angle}deg)
      translateZ(${D16_RADIUS}px)
      translateY(${D16_HEIGHT / -2}px)
    `
  }
}

// === Matter.js 物理世界 ===
let engine = null
let boxBody = null
let ropeConstraint = null
let animationFrame = null
let lastTime = 0

const initPhysics = () => {
  engine = Engine.create({
    gravity: { x: 0, y: 1 }
  })

  // 小方块刚体
  boxBody = Bodies.circle(anchorX, ROPE_BASE_LENGTH, 25, {
    mass: 1,
    frictionAir: 0.02
  })

  // 绳子约束：将锚点与方块连接
  ropeConstraint = Constraint.create({
    pointA: { x: anchorX, y: anchorY },
    bodyB: boxBody,
    length: ROPE_BASE_LENGTH,
    stiffness: 0.02
  })

  Composite.add(engine.world, [boxBody, ropeConstraint])
}

const physicsTick = (time) => {
  if (!engine || !boxBody) {
    animationFrame = requestAnimationFrame(physicsTick)
    return
  }

  if (!lastTime) {
    lastTime = time
  }
  const delta = time - lastTime
  lastTime = time

  Engine.update(engine, delta)

  // 将物理世界位置映射到组件坐标
  const { x, y } = boxBody.position
  ropeLength.value = y
  offsetX.value = x - anchorX
  offsetY.value = y - ROPE_BASE_LENGTH

  animationFrame = requestAnimationFrame(physicsTick)
}

// === 交互逻辑：拖拽时直接设置刚体位置 ===

const startDrag = (e) => {
  isDragging.value = true
  const clientY = e.touches ? e.touches[0].clientY : e.clientY
  const clientX = e.touches ? e.touches[0].clientX : e.clientX
  dragStartY.value = clientY
  dragStartOffset.value = offsetY.value
  dragStartX.value = clientX
  dragStartOffsetX.value = offsetX.value
  
  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', endDrag)
  document.addEventListener('touchmove', onDrag)
  document.addEventListener('touchend', endDrag)
}

// 拖拽中
const onDrag = (e) => {
  if (!isDragging.value) return
  
  const clientY = e.touches ? e.touches[0].clientY : e.clientY
  const clientX = e.touches ? e.touches[0].clientX : e.clientX
  const deltaY = clientY - dragStartY.value
  const deltaX = clientX - dragStartX.value
  
  // 计算目标偏移（只限制纵向，横向不再人工夹住，让物理约束自己限制）
  const nextOffsetY = Math.max(0, Math.min(MAX_STRETCH, dragStartOffset.value + deltaY))
  const nextOffsetX = dragStartOffsetX.value + deltaX * 0.2

  if (boxBody) {
    const targetX = anchorX + nextOffsetX
    const targetY = ROPE_BASE_LENGTH + nextOffsetY
    Body.setPosition(boxBody, { x: targetX, y: targetY })
    Body.setVelocity(boxBody, { x: 0, y: 0 })
  }
}

// 结束拖拽
const endDrag = () => {
  if (!isDragging.value) return
  
  isDragging.value = false
  
  // 如果拉到足够远，触发切换
  if (offsetY.value > TRIGGER_DISTANCE) {
    props.onToggle?.()
  }
  
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', endDrag)
  document.removeEventListener('touchmove', onDrag)
  document.removeEventListener('touchend', endDrag)
}

onMounted(() => {
  initPhysics()
  animationFrame = requestAnimationFrame(physicsTick)
})

onUnmounted(() => {
  if (animationFrame) {
    cancelAnimationFrame(animationFrame)
  }
  engine = null
  boxBody = null
  ropeConstraint = null
})
</script>

<style scoped>
.switch-wrapper {
  position: fixed;
  top: 0;
  right: 40px;
  z-index: 9999;
  width: 80px;
  pointer-events: none;
}

.rope-svg {
  position: absolute;
  top: 0;
  left: 0;
  width: 80px;
  height: 230px;
  filter: drop-shadow(0 2px 4px rgba(0,0,0,0.35));
  overflow: visible;
}

.switch-box {
  position: absolute;
  width: 50px;
  height: 50px;
  cursor: grab;
  pointer-events: auto;
  transform-style: preserve-3d;
  transition: transform 0.1s ease-out;
}

.switch-box:active {
  cursor: grabbing;
}

/* === 3D 16 面骰子样式 === */

.d16 {
  position: relative;
  width: 52px;
  height: 52px;
  transform-style: preserve-3d;
  animation: d16-spin 8s linear infinite;
}

.d16-face {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 18px;
  height: 40px;
  margin: -20px -9px;
  background: linear-gradient(180deg, #8be9ff, #5f7bff 60%, #4338ca);
  border: 1px solid rgba(15, 23, 42, 0.35);
  box-shadow: 0 0 6px rgba(37, 99, 235, 0.45);
  backface-visibility: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.d16-num {
  font-size: 10px;
  font-weight: 700;
  color: #e5efff;
  text-shadow: 0 0 3px rgba(15, 23, 42, 0.9);
}

.d16-cap {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 52px;
  height: 52px;
  margin: -26px;
  border-radius: 50%;
  background: radial-gradient(circle at 30% 20%, #e5f0ff, #4f46e5 70%, #1e1b4b);
  opacity: 0.9;
}

.d16-cap-top {
  transform: translateY(-22px);
}

.d16-cap-bottom {
  transform: translateY(22px) rotateX(180deg);
}

@keyframes d16-spin {
  0%   { transform: rotateX(-18deg) rotateY(0deg); }
  100% { transform: rotateX(-18deg) rotateY(360deg); }
}
</style>
