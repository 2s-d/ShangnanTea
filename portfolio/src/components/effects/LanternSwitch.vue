<template>
  <div ref="wrapperRef" class="switch-wrapper">
    <!-- 彩虹流光：替代绳子的视觉效果，高度跟随挂件位置 -->
    <div class="rainbow-beam" :style="rainbowStyle"></div>
    <!-- 顶部挂点小圆球 -->
    <div class="beam-head"></div>
    <!-- 可拖拽的小方块 -->
    <div 
      class="switch-box"
      :style="boxStyle"
      @mousedown="startDrag"
      @touchstart="startDrag"
    >
      <!-- 使用 three.js 渲染的 Cloud Tea 3D 模型 -->
      <CloudTeaModel />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { Engine, Bodies, Body, Constraint, Composite } from 'matter-js'
import CloudTeaModel from './CloudTeaModel.vue'

const props = defineProps({
  currentTheme: String,
  onToggle: Function
})

// DOM 引用
const wrapperRef = ref(null)

// 位置和物理参数（以组件内部坐标为基准）
// wrapper 宽度是 80px，anchorX 取 40，让物理坐标的 x=0 对应到容器正中
// 这样彩虹柱（left:50%）和挂件盒子的中心就能在同一条竖线上
const anchorX = 25.5
const anchorY = 10
// 基础绳长（静止时顶部到骰子中心的距离），适当拉长一点让视觉上更协调
const ROPE_BASE_LENGTH = 200

const offsetY = ref(0) // Y轴偏移
const offsetX = ref(0) // X轴偏移（用于晃动和左右拉动）
const isDragging = ref(false)
const dragStartY = ref(0)
const dragStartOffset = ref(0)
const dragStartX = ref(0)
const dragStartOffsetX = ref(0)

// 物理参数
const TRIGGER_DISTANCE = 80 // 触发切换的距离
const MAX_STRETCH = 250 // 最大拉伸距离

// 挂件当前距离顶部的高度（由物理引擎驱动）
const ropeLength = ref(ROPE_BASE_LENGTH)

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

// 彩虹流光样式：根据物理引擎的 offsetX / ropeLength 计算“从顶部锚点到挂件中心”的直线
const rainbowStyle = computed(() => {
  const offsetToModelCenter = 40 // 估算挂件中心到其顶部的距离

  // 终点相对于顶部锚点的位移
  const dx = offsetX.value
  const dyRaw = ropeLength.value - offsetToModelCenter
  const dy = Math.max(20, dyRaw)

  // 线段长度 = 彩虹的高度
  const length = Math.sqrt(dx * dx + dy * dy)

  // 计算旋转角度：DOM 坐标系中 y 向下，正角度为顺时针
  // 之前使用 atan2(dx, dy) 导致左右方向相反，这里对 dx 取反保证：
  // offsetX > 0 (挂件在右侧) 时，彩虹向右倾斜；offsetX < 0 时向左倾斜
  const angleRad = Math.atan2(-dx, dy)
  const angleDeg = (angleRad * 180) / Math.PI

  return {
    height: `${length}px`,
    transform: `translateX(-50%) rotate(${angleDeg}deg)`
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
let visibilityHandler = null

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
  // rAF 在标签页后台会暂停，恢复时 delta 会非常大：会导致刚体瞬移下坠、约束被拉到极限然后疯狂弹跳
  // 这里 clamp delta，避免“离开页面再回来就乱蹦”的 bug
  const rawDelta = time - lastTime
  const delta = Math.min(rawDelta, 33)
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
  
  // 计算目标偏移（只限制纵向，横向通过 0.2 系数控制“手感”和最大幅度）
  const nextOffsetY = Math.max(0, Math.min(MAX_STRETCH, dragStartOffset.value + deltaY))
  const nextOffsetX = dragStartOffsetX.value + deltaX * 1

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
  // 初始化物理世界，并重置挂件位置，避免路由切换后状态残留
  offsetX.value = 0
  offsetY.value = 0
  ropeLength.value = ROPE_BASE_LENGTH
  lastTime = 0

  if (!engine) {
    initPhysics()
    animationFrame = requestAnimationFrame(physicsTick)
  }

  // 切走/切回页面时重置计时，避免第一帧 delta 过大
  visibilityHandler = () => {
    if (document.visibilityState === 'visible') {
      lastTime = 0
    }
  }
  document.addEventListener('visibilitychange', visibilityHandler)
})

onUnmounted(() => {
  if (animationFrame) {
    cancelAnimationFrame(animationFrame)
  }
  if (visibilityHandler) {
    document.removeEventListener('visibilitychange', visibilityHandler)
    visibilityHandler = null
  }
  engine = null
  boxBody = null
  ropeConstraint = null
  lastTime = 0
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

.switch-box {
  position: absolute;
  width: 50px;
  height: 50px;
  cursor: grab;
  pointer-events: auto;
  transform-style: preserve-3d;
  transition: transform 0.1s ease-out;
}

/* 彩虹流光柱 */
.rainbow-beam {
  position: absolute;
  top: 0;
  left: 50%;
  width: 5px;
  /* 使用 CSS 变量控制颜色，便于在动画中做三种绚烂配色循环切换 */
  --rb-c1: rgba(255, 162, 200, 0.85);
  --rb-c2: rgba(167, 139, 250, 0.95);
  --rb-c3: rgba(45, 212, 191, 0.9);
  --rb-shadow: rgba(192, 132, 252, 0.8);
  background: linear-gradient(
    to bottom,
    rgba(255, 255, 255, 0.0),
    var(--rb-c1),
    var(--rb-c2),
    var(--rb-c3)
  );
  box-shadow:
    0 0 10px var(--rb-shadow),
    0 0 20px var(--rb-shadow);
  border-radius: 999px;
  overflow: hidden;
  background-size: 100% 200%;
  animation: rainbow-flow 3s linear infinite;
  transform-origin: top center;
}

/* 顶部挂点 */
.beam-head {
  position: absolute;
  top: 0;
  left: 50%;
  width: 16px;
  height: 16px;
  transform: translate(-50%, -8px);
  border-radius: 50%;
  background: radial-gradient(circle, #fef3c7 0%, #fbbf24 50%, #b45309 100%);
  box-shadow:
    0 0 8px rgba(251, 191, 36, 0.9),
    0 0 16px rgba(251, 191, 36, 0.7);
}

@keyframes rainbow-flow {
  0% {
    background-position: 0 0;
    --rb-c1: rgba(255, 162, 200, 0.85);   /* 粉橙渐变 */
    --rb-c2: rgba(251, 191, 36, 0.95);
    --rb-c3: rgba(244, 114, 182, 0.9);
    --rb-shadow: rgba(251, 191, 36, 0.8);
  }
  33% {
    background-position: 0 70%;
    --rb-c1: rgba(129, 140, 248, 0.9);    /* 蓝紫渐变 */
    --rb-c2: rgba(196, 181, 253, 0.95);
    --rb-c3: rgba(56, 189, 248, 0.9);
    --rb-shadow: rgba(129, 140, 248, 0.85);
  }
  66% {
    background-position: 0 140%;
    --rb-c1: rgba(45, 212, 191, 0.9);     /* 青绿渐变 */
    --rb-c2: rgba(74, 222, 128, 0.95);
    --rb-c3: rgba(250, 204, 21, 0.9);
    --rb-shadow: rgba(74, 222, 128, 0.85);
  }
  100% {
    background-position: 0 200%;
    --rb-c1: rgba(255, 162, 200, 0.85);
    --rb-c2: rgba(251, 191, 36, 0.95);
    --rb-c3: rgba(244, 114, 182, 0.9);
    --rb-shadow: rgba(251, 191, 36, 0.8);
  }
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
