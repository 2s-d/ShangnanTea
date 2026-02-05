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
const CANVAS_WIDTH = 120
const CANVAS_HEIGHT = 180
const ANCHOR_X = 60 // 固定点 X
const ANCHOR_Y = 10  // 固定点 Y
const ROPE_SEGMENTS = 6 // 绳子段数（减少）
const SEGMENT_LENGTH = 6 // 每段长度（缩短）
const SEGMENT_RADIUS = 2 // 绳子粗细
const LANTERN_SIZE = 45 // 灯笼大小
const PULL_THRESHOLD = 40 // 拉动阈值触发切换

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
    LANTERN_SIZE * 0.7,
    LANTERN_SIZE * 0.9,
    {
      density: 0.008,
      friction: 0.3,
      frictionAir: 0.03,
      restitution: 0.3,
      render: {
        fillStyle: 'rgba(255, 0, 0, 0.2)',
        strokeStyle: '#ff0000',
        lineWidth: 1
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

  // 鼠标控制 - 只能拖拽灯笼
  mouse = Mouse.create(canvasRef.value)
  mouseConstraint = MouseConstraint.create(engine, {
    mouse: mouse,
    constraint: {
      stiffness: 0.3,
      damping: 0.1,
      render: { visible: false }
    },
    collisionFilter: {
      mask: 0x0001
    }
  })

  // 设置灯笼的碰撞过滤，让它可以被鼠标选中
  Matter.Body.set(lanternBody, {
    collisionFilter: {
      category: 0x0001
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
  e.stopPropagation()
  isDragging.value = true
}

// 点击切换主题
const handleClick = (e) => {
  e.stopPropagation()
  // 短时间内没有拖拽才算点击
  setTimeout(() => {
    if (!isDragging.value) {
      props.onToggle?.()
    }
  }, 50)
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
  right: 30px;
  z-index: 9999;
  width: 120px;
  height: 180px;
  pointer-events: none;
}

.physics-canvas {
  position: absolute;
  top: 0;
  left: 0;
  pointer-events: auto;
}

.lantern {
  position: absolute;
  pointer-events: auto;
  cursor: grab;
  transition: none;
  display: flex;
  flex-direction: column;
  align-items: center;
  z-index: 10;
  user-select: none;
}

.lantern:active {
  cursor: grabbing;
}

.lantern-top {
  width: 60%;
  height: 8%;
  background: linear-gradient(to bottom, #8b4513, #654321);
  border-radius: 4px 4px 0 0;
  box-shadow: 0 2px 4px rgba(0,0,0,0.3);
}

.lantern-body {
  position: relative;
  width: 100%;
  height: 70%;
  background: linear-gradient(135deg, #ff4444 0%, #cc0000 50%, #ff4444 100%);
  border-radius: 8px;
  box-shadow: 
    0 0 20px rgba(255, 68, 68, 0.6),
    inset 0 0 20px rgba(255, 255, 255, 0.2),
    0 4px 10px rgba(0,0,0,0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.lantern-pattern {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    repeating-linear-gradient(
      90deg,
      transparent,
      transparent 8px,
      rgba(139, 0, 0, 0.3) 8px,
      rgba(139, 0, 0, 0.3) 10px
    );
  pointer-events: none;
}

.lantern-text {
  font-size: 24px;
  z-index: 1;
  filter: drop-shadow(0 2px 4px rgba(0,0,0,0.5));
  animation: glow-pulse 2s ease-in-out infinite;
}

@keyframes glow-pulse {
  0%, 100% { 
    filter: drop-shadow(0 2px 4px rgba(0,0,0,0.5));
  }
  50% { 
    filter: drop-shadow(0 0 8px rgba(255, 200, 0, 0.8)) drop-shadow(0 2px 4px rgba(0,0,0,0.5));
  }
}

.lantern-bottom {
  width: 60%;
  height: 8%;
  background: linear-gradient(to bottom, #654321, #8b4513);
  border-radius: 0 0 4px 4px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.3);
}

.lantern-tassel {
  width: 2px;
  height: 14%;
  background: linear-gradient(to bottom, #ff4444, #ffaa00);
  position: relative;
}

.lantern-tassel::after {
  content: '';
  position: absolute;
  bottom: -4px;
  left: 50%;
  transform: translateX(-50%);
  width: 8px;
  height: 8px;
  background: #ffaa00;
  border-radius: 50%;
  box-shadow: 0 2px 4px rgba(0,0,0,0.3);
}

/* 悬停效果 */
.lantern:hover .lantern-body {
  box-shadow: 
    0 0 30px rgba(255, 68, 68, 0.9),
    inset 0 0 20px rgba(255, 255, 255, 0.3),
    0 4px 10px rgba(0,0,0,0.4);
}

.lantern:hover .lantern-text {
  animation: swing-text 0.5s ease-in-out;
}

@keyframes swing-text {
  0%, 100% { transform: rotate(0deg); }
  25% { transform: rotate(-5deg); }
  75% { transform: rotate(5deg); }
}
</style>
