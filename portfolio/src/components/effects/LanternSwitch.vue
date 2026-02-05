<template>
  <div ref="wrapperRef" class="switch-wrapper">
    <!-- 绳子 - 会根据拖拽伸缩 -->
    <svg class="rope-svg" :style="{ height: ropeLength + 'px' }">
      <path
        :d="ropePath"
        stroke="#8b4513"
        stroke-width="3"
        fill="none"
        stroke-linecap="round"
      />
    </svg>
    
    <!-- 可拖拽的小方块 -->
    <div 
      class="switch-box"
      :style="boxStyle"
      @mousedown="startDrag"
      @touchstart="startDrag"
    >
      <div class="box-face front">
        <div class="box-icon"></div>
      </div>
      <div class="box-face back"></div>
      <div class="box-face left"></div>
      <div class="box-face right"></div>
      <div class="box-face top"></div>
      <div class="box-face bottom"></div>
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
const anchorX = 40
const anchorY = 0
const ROPE_BASE_LENGTH = 80

const offsetY = ref(0) // Y轴偏移
const offsetX = ref(0) // X轴偏移（用于晃动和左右拉动）
const isDragging = ref(false)
const dragStartY = ref(0)
const dragStartOffset = ref(0)
const dragStartX = ref(0)
const dragStartOffsetX = ref(0)

// 物理参数
const TRIGGER_DISTANCE = 80 // 触发切换的距离
const MAX_STRETCH = 150 // 最大拉伸距离
const MAX_HORIZONTAL = 20 // 最大左右偏移

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

// 方块样式
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
  
  // 计算目标偏移
  const nextOffsetY = Math.max(0, Math.min(MAX_STRETCH, dragStartOffset.value + deltaY))
  const nextOffsetX = Math.max(
    -MAX_HORIZONTAL,
    Math.min(MAX_HORIZONTAL, dragStartOffsetX.value + deltaX * 0.2)
  )

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
  transition: height 0.1s ease-out;
  filter: drop-shadow(1px 1px 2px rgba(0,0,0,0.3));
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

.box-face {
  position: absolute;
  width: 50px;
  height: 50px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: 2px solid rgba(255,255,255,0.3);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.front {
  transform: translateZ(25px);
}

.back {
  transform: translateZ(-25px) rotateY(180deg);
  background: linear-gradient(135deg, #764ba2 0%, #667eea 100%);
}

.left {
  transform: rotateY(-90deg) translateZ(25px);
  background: linear-gradient(135deg, #5a67d8 0%, #667eea 100%);
}

.right {
  transform: rotateY(90deg) translateZ(25px);
  background: linear-gradient(135deg, #5a67d8 0%, #667eea 100%);
}

.top {
  transform: rotateX(90deg) translateZ(25px);
  background: linear-gradient(135deg, #7c3aed 0%, #667eea 100%);
}

.bottom {
  transform: rotateX(-90deg) translateZ(25px);
  background: linear-gradient(135deg, #7c3aed 0%, #667eea 100%);
}

.box-icon {
  font-size: 28px;
  animation: float-icon 2s ease-in-out infinite;
  filter: drop-shadow(0 2px 4px rgba(0,0,0,0.3));
}

@keyframes float-icon {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-3px); }
}

/* 悬停效果 */
.switch-box:hover .box-face {
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
}

.switch-box:hover .box-icon {
  animation: bounce-icon 0.6s ease-in-out;
}

@keyframes bounce-icon {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.2); }
}
</style>
