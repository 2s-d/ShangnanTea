<template>
  <div class="switch-wrapper">
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
        <div class="box-icon">🎨</div>
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

const props = defineProps({
  currentTheme: String,
  onToggle: Function
})

// 位置和物理参数
const baseY = 80 // 基础Y位置
const baseX = 40 // 距离右边的距离
const offsetY = ref(0) // Y轴偏移
const offsetX = ref(0) // X轴偏移（用于晃动）
const velocity = ref(0) // 速度
const isDragging = ref(false)
const dragStartY = ref(0)
const dragStartOffset = ref(0)

// 物理参数
const SPRING_STRENGTH = 0.15 // 弹簧强度
const DAMPING = 0.85 // 阻尼
const TRIGGER_DISTANCE = 80 // 触发切换的距离
const MAX_STRETCH = 150 // 最大拉伸距离

// 绳子长度
const ropeLength = computed(() => {
  return 60 + Math.abs(offsetY.value)
})

// 绳子路径 - 贝塞尔曲线模拟弹性
const ropePath = computed(() => {
  const startX = 30
  const startY = 0
  const endX = 30 + offsetX.value
  const endY = ropeLength.value
  
  // 控制点让绳子有弧度
  const controlX = startX + offsetX.value * 0.5
  const controlY = endY * 0.4
  
  return `M ${startX} ${startY} Q ${controlX} ${controlY} ${endX} ${endY}`
})

// 方块样式
const boxStyle = computed(() => {
  const rotation = offsetX.value * 0.5 // 根据X偏移旋转
  return {
    top: `${baseY + offsetY.value}px`,
    right: `${baseX - offsetX.value}px`,
    transform: `rotateY(${rotation}deg) rotateX(${offsetY.value * 0.2}deg)`
  }
})

// 开始拖拽
const startDrag = (e) => {
  isDragging.value = true
  const clientY = e.touches ? e.touches[0].clientY : e.clientY
  dragStartY.value = clientY
  dragStartOffset.value = offsetY.value
  
  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', endDrag)
  document.addEventListener('touchmove', onDrag)
  document.addEventListener('touchend', endDrag)
}

// 拖拽中
const onDrag = (e) => {
  if (!isDragging.value) return
  
  const clientY = e.touches ? e.touches[0].clientY : e.clientY
  const deltaY = clientY - dragStartY.value
  
  // 限制最大拉伸
  offsetY.value = Math.max(0, Math.min(MAX_STRETCH, dragStartOffset.value + deltaY))
  
  // 添加一点横向晃动
  offsetX.value = Math.sin(offsetY.value * 0.1) * 10
}

// 结束拖拽
const endDrag = () => {
  if (!isDragging.value) return
  
  isDragging.value = false
  
  // 如果拉到足够远，触发切换
  if (offsetY.value > TRIGGER_DISTANCE) {
    props.onToggle?.()
  }
  
  // 设置初始速度用于回弹
  velocity.value = -offsetY.value * 0.3
  
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', endDrag)
  document.removeEventListener('touchmove', onDrag)
  document.removeEventListener('touchend', endDrag)
}

// 物理模拟循环
let animationFrame = null
const physicsLoop = () => {
  if (!isDragging.value) {
    // 弹簧力
    const springForce = -offsetY.value * SPRING_STRENGTH
    velocity.value += springForce
    
    // 阻尼
    velocity.value *= DAMPING
    
    // 更新位置
    offsetY.value += velocity.value
    
    // 横向晃动衰减
    offsetX.value *= 0.9
    
    // 停止条件
    if (Math.abs(velocity.value) < 0.1 && Math.abs(offsetY.value) < 0.5) {
      offsetY.value = 0
      velocity.value = 0
      offsetX.value = 0
    }
  }
  
  animationFrame = requestAnimationFrame(physicsLoop)
}

onMounted(() => {
  physicsLoop()
})

onUnmounted(() => {
  if (animationFrame) {
    cancelAnimationFrame(animationFrame)
  }
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
