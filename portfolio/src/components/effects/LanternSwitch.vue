<template>
  <div class="switch-wrapper">
    <!-- Matter.js Canvas for rope physics -->
    <canvas ref="canvasRef" class="physics-canvas"></canvas>
    
    <!-- 灯笼 DOM 元素 (跟随物理引擎位置) -->
    <div 
      class="lantern"
      :style="lanternStyle"
      @mousedown="startDrag"
      @touchstart="startDrag"
      @click="handleClick"
    >
      <div class="lantern-top"></div>
      <div class="lantern-body">
        <div class="lantern-pattern"></div>
        <div class="lantern-text">🏮</div>
      </div>
      <div class="lantern-bottom"></div>
      <div class="lantern-tassel"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import Matter from 'matter-js'

const props = defineProps({
  currentTheme: String,
  onToggle: Function
})

// Canvas 引用
const canvasRef = ref(null)

// Matter.js 核心对象
let engine = null
let render = null
let runner = null
let mouse = null
let mouseConstraint = null

// 绳子和灯笼的物理体
let ropeSegments = []
let lanternBody = null
let anchorPoint = null

// 灯笼位置（用于 DOM 同步）
const lanternX = ref(0)
const lanternY = ref(0)
const lanternAngle = ref(0)

// 拖拽状态
const isDragging = ref(false)
const hasBeenPulled = ref(false)
const initialY = ref(0)

// 常量
const CANVAS_WIDTH = 200
const CANVAS_HEIGHT = 300
const ANCHOR_X = 100 // 固定点 X
const ANCHOR_Y = 20  // 固定点 Y
const ROPE_SEGMENTS = 12 // 绳子段数
const SEGMENT_LENGTH = 8 // 每段长度
const SEGMENT_RADIUS = 2 // 绳子粗细
const LANTERN_SIZE = 40 // 灯笼大小
const PULL_THRESHOLD = 60 // 拉动阈值触发切换

// 灯笼样式（跟随物理引擎）
const lanternStyle = computed(() => {
  return {
    left: `${lanternX.value - LANTERN_SIZE / 2}px`,
    top: `${lanternY.value - LANTERN_SIZE / 2}px`,
    transform: `rotate(${lanternAngle.value}rad)`,
    width: `${LANTERN_SIZE}px`,
    height: `${LANTERN_SIZE}px`
  }
})

// 初始化 Matter.js
const initPhysics = () => {
  const { Engine, Render, Runner, Bodies, Composite, Constraint, Mouse, MouseConstraint, Events } = Matter

  // 创建引擎
  engine = Engine.create({
    gravity: { x: 0, y: 1 }
  })

  // 创建渲染器
  render = Render.create({
    canvas: canvasRef.value,
    engine: engine,
    options: {
      width: CANVAS_WIDTH,
      height: CANVAS_HEIGHT,
      wireframes: false,
      background: 'transparent'
    }
  })

  // 创建固定锚点（不可见）
  anchorPoint = Bodies.circle(ANCHOR_X, ANCHOR_Y, 3, {
    isStatic: true,
    render: { visible: false }
  })

  // 创建绳子段（链式连接的小圆球）
  ropeSegments = []
  for (let i = 0; i < ROPE_SEGMENTS; i++) {
    const segment = Bodies.circle(
      ANCHOR_X,
      ANCHOR_Y + (i + 1) * SEGMENT_LENGTH,
      SEGMENT_RADIUS,
      {
        density: 0.001,
        friction: 0.1,
        frictionAir: 0.01,
        render: {
          fillStyle: '#8b4513',
          strokeStyle: '#654321',
          lineWidth: 1
        }
      }
    )
    ropeSegments.push(segment)
  }

  // 创建灯笼物理体
  lanternBody = Bodies.rectangle(
    ANCHOR_X,
    ANCHOR_Y + (ROPE_SEGMENTS + 1) * SEGMENT_LENGTH,
    LANTERN_SIZE * 0.6,
    LANTERN_SIZE * 0.8,
    {
      density: 0.01,
      friction: 0.3,
      frictionAir: 0.02,
      render: {
        fillStyle: 'rgba(255, 0, 0, 0.3)',
        strokeStyle: '#ff0000',
        lineWidth: 2
      }
    }
  )

  // 记录初始Y位置
  initialY.value = lanternBody.position.y

  // 添加所有物体到世界
  Composite.add(engine.world, [anchorPoint, ...ropeSegments, lanternBody])

  // 创建约束（连接绳子段）
  // 锚点到第一段
  Composite.add(engine.world, Constraint.create({
    bodyA: anchorPoint,
    bodyB: ropeSegments[0],
    length: SEGMENT_LENGTH,
    stiffness: 0.9,
    render: { visible: false }
  }))

  // 绳子段之间
  for (let i = 0; i < ropeSegments.length - 1; i++) {
    Composite.add(engine.world, Constraint.create({
      bodyA: ropeSegments[i],
      bodyB: ropeSegments[i + 1],
      length: SEGMENT_LENGTH,
      stiffness: 0.9,
      render: { visible: false }
    }))
  }

  // 最后一段到灯笼
  Composite.add(engine.world, Constraint.create({
    bodyA: ropeSegments[ropeSegments.length - 1],
    bodyB: lanternBody,
    length: SEGMENT_LENGTH,
    stiffness: 0.9,
    render: { visible: false }
  }))

  // 鼠标控制
  mouse = Mouse.create(canvasRef.value)
  mouseConstraint = MouseConstraint.create(engine, {
    mouse: mouse,
    constraint: {
      stiffness: 0.2,
      render: { visible: false }
    }
  })

  Composite.add(engine.world, mouseConstraint)

  // 监听拖拽事件
  Events.on(mouseConstraint, 'startdrag', () => {
    isDragging.value = true
  })

  Events.on(mouseConstraint, 'enddrag', () => {
    isDragging.value = false
    
    // 检查是否拉动超过阈值
    const pullDistance = lanternBody.position.y - initialY.value
    if (pullDistance > PULL_THRESHOLD && hasBeenPulled.value) {
      props.onToggle?.()
      hasBeenPulled.value = false
    }
  })

  // 更新循环 - 同步物理位置到 DOM
  Events.on(engine, 'afterUpdate', () => {
    lanternX.value = lanternBody.position.x
    lanternY.value = lanternBody.position.y
    lanternAngle.value = lanternBody.angle

    // 检测拉动
    const pullDistance = lanternBody.position.y - initialY.value
    if (pullDistance > PULL_THRESHOLD) {
      hasBeenPulled.value = true
    }
  })

  // 启动引擎和渲染
  Render.run(render)
  runner = Runner.create()
  Runner.run(runner, engine)
}

// 手动拖拽（用于触摸设备）
const startDrag = (e) => {
  e.preventDefault()
  isDragging.value = true
}

// 点击切换主题
const handleClick = () => {
  if (!isDragging.value) {
    props.onToggle?.()
  }
}

// 清理
const cleanup = () => {
  if (render) {
    Matter.Render.stop(render)
    render.canvas.remove()
    render.canvas = null
    render.context = null
    render.textures = {}
  }
  if (runner) {
    Matter.Runner.stop(runner)
  }
  if (engine) {
    Matter.Engine.clear(engine)
    Matter.World.clear(engine.world, false)
  }
}

onMounted(() => {
  initPhysics()
})

onUnmounted(() => {
  cleanup()
})
</script>

<style scoped>
.switch-wrapper {
  position: fixed;
  top: 0;
  right: 40px;
  z-index: 9999;
  width: 60px;
  pointer-events: none;
}

.rope-svg {
  position: absolute;
  top: 0;
  left: 0;
  width: 60px;
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
