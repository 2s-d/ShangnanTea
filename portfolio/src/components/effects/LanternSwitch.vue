<template>
  <div class="lantern-container">
    <!-- 绳子 -->
    <div class="rope" :style="{ height: ropeLength + 'px' }"></div>
    
    <!-- 灯笼 -->
    <div 
      class="lantern"
      :class="{ 'is-coderain': currentTheme === 'coderain', 'is-dragging': isDragging }"
      :style="lanternStyle"
      @mousedown="startDrag"
      @touchstart="startDrag"
    >
      <!-- 灯笼顶部 -->
      <div class="lantern-top"></div>
      
      <!-- 灯笼主体 -->
      <div class="lantern-body">
        <div class="lantern-light" :class="{ 'glow': currentTheme === 'coderain' }"></div>
        <div class="lantern-text">{{ currentTheme === 'coderain' ? '码' : '粒' }}</div>
        <div class="lantern-pattern pattern-1"></div>
        <div class="lantern-pattern pattern-2"></div>
      </div>
      
      <!-- 灯笼底部 -->
      <div class="lantern-bottom"></div>
      
      <!-- 流苏 -->
      <div class="tassel">
        <div class="tassel-line"></div>
        <div class="tassel-end"></div>
      </div>
    </div>
    
    <!-- 提示文字 -->
    <div class="hint" v-if="showHint">拉动灯笼切换主题</div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  currentTheme: String,
  onToggle: Function
})

const ropeLength = ref(60)
const isDragging = ref(false)
const startY = ref(0)
const currentY = ref(0)
const showHint = ref(true)

const lanternStyle = computed(() => ({
  transform: `translateY(${currentY.value}px) rotate(${currentY.value * 0.1}deg)`
}))

const startDrag = (e) => {
  isDragging.value = true
  showHint.value = false
  startY.value = e.type === 'mousedown' ? e.clientY : e.touches[0].clientY
  
  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', endDrag)
  document.addEventListener('touchmove', onDrag)
  document.addEventListener('touchend', endDrag)
}

const onDrag = (e) => {
  if (!isDragging.value) return
  
  const clientY = e.type === 'mousemove' ? e.clientY : e.touches[0].clientY
  const deltaY = clientY - startY.value
  
  // 限制拖拽范围 -20 到 80
  currentY.value = Math.max(-20, Math.min(80, deltaY))
}

const endDrag = () => {
  if (!isDragging.value) return
  
  // 如果拉动超过 40px，切换主题
  if (Math.abs(currentY.value) > 40) {
    props.onToggle?.()
  }
  
  // 重置位置
  isDragging.value = false
  currentY.value = 0
  
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', endDrag)
  document.removeEventListener('touchmove', onDrag)
  document.removeEventListener('touchend', endDrag)
}

// 自动隐藏提示
onMounted(() => {
  setTimeout(() => {
    showHint.value = false
  }, 5000)
})

onUnmounted(() => {
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', endDrag)
  document.removeEventListener('touchmove', onDrag)
  document.removeEventListener('touchend', endDrag)
})
</script>

<style scoped>
.lantern-container {
  position: fixed;
  top: 0;
  right: 80px;
  z-index: 9999;
  user-select: none;
}

/* 绳子 */
.rope {
  width: 2px;
  background: linear-gradient(to bottom, #8b4513, #654321);
  margin: 0 auto;
  box-shadow: 0 0 3px rgba(0, 0, 0, 0.3);
  transition: height 0.3s ease;
}

/* 灯笼容器 */
.lantern {
  width: 60px;
  height: 80px;
  cursor: grab;
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  animation: swing 3s ease-in-out infinite;
  transform-origin: top center;
}

.lantern:active {
  cursor: grabbing;
}

.lantern.is-dragging {
  animation: none;
  transition: none;
}

/* 灯笼顶部 */
.lantern-top {
  width: 50px;
  height: 8px;
  background: linear-gradient(to bottom, #8b4513, #654321);
  border-radius: 50% 50% 0 0;
  margin: 0 auto;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

/* 灯笼主体 */
.lantern-body {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #ff4444 0%, #cc0000 100%);
  border-radius: 50%;
  position: relative;
  box-shadow: 
    0 4px 8px rgba(0, 0, 0, 0.3),
    inset 0 -10px 20px rgba(0, 0, 0, 0.2),
    inset 0 10px 20px rgba(255, 255, 255, 0.2);
  overflow: hidden;
}

/* 灯光效果 */
.lantern-light {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 40px;
  height: 40px;
  background: radial-gradient(circle, rgba(255, 255, 200, 0.8) 0%, transparent 70%);
  border-radius: 50%;
  opacity: 0;
  transition: opacity 0.5s ease;
}

.lantern-light.glow {
  opacity: 1;
  animation: glow-pulse 2s ease-in-out infinite;
}

/* 灯笼文字 */
.lantern-text {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #ffd700;
  font-size: 20px;
  font-weight: bold;
  text-shadow: 0 0 10px rgba(255, 215, 0, 0.8);
  z-index: 2;
}

/* 灯笼花纹 */
.lantern-pattern {
  position: absolute;
  width: 100%;
  height: 2px;
  background: linear-gradient(90deg, transparent, #ffd700, transparent);
  left: 0;
}

.pattern-1 {
  top: 20px;
}

.pattern-2 {
  bottom: 20px;
}

/* 灯笼底部 */
.lantern-bottom {
  width: 50px;
  height: 8px;
  background: linear-gradient(to top, #8b4513, #654321);
  border-radius: 0 0 50% 50%;
  margin: 0 auto;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

/* 流苏 */
.tassel {
  width: 2px;
  margin: 0 auto;
}

.tassel-line {
  width: 2px;
  height: 15px;
  background: linear-gradient(to bottom, #8b4513, #654321);
  margin: 0 auto;
}

.tassel-end {
  width: 20px;
  height: 20px;
  background: linear-gradient(135deg, #ff4444, #cc0000);
  border-radius: 50% 50% 0 0;
  margin: 0 auto;
  position: relative;
  left: -9px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.tassel-end::after {
  content: '';
  position: absolute;
  bottom: -8px;
  left: 50%;
  transform: translateX(-50%);
  width: 0;
  height: 0;
  border-left: 8px solid transparent;
  border-right: 8px solid transparent;
  border-top: 8px solid #cc0000;
}

/* 提示文字 */
.hint {
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  margin-top: 10px;
  padding: 8px 12px;
  background: rgba(0, 0, 0, 0.8);
  color: white;
  font-size: 12px;
  border-radius: 4px;
  white-space: nowrap;
  animation: fadeInOut 5s ease-in-out;
  pointer-events: none;
}

/* 动画 */
@keyframes swing {
  0%, 100% {
    transform: rotate(0deg);
  }
  25% {
    transform: rotate(3deg);
  }
  75% {
    transform: rotate(-3deg);
  }
}

@keyframes glow-pulse {
  0%, 100% {
    opacity: 0.6;
    transform: translate(-50%, -50%) scale(1);
  }
  50% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1.1);
  }
}

@keyframes fadeInOut {
  0% {
    opacity: 0;
    transform: translateX(-50%) translateY(-10px);
  }
  10%, 90% {
    opacity: 1;
    transform: translateX(-50%) translateY(0);
  }
  100% {
    opacity: 0;
    transform: translateX(-50%) translateY(-10px);
  }
}

/* 代码雨主题下的灯笼 */
.lantern.is-coderain .lantern-body {
  box-shadow: 
    0 4px 8px rgba(0, 0, 0, 0.3),
    inset 0 -10px 20px rgba(0, 0, 0, 0.2),
    inset 0 10px 20px rgba(255, 255, 255, 0.2),
    0 0 30px rgba(255, 68, 68, 0.6);
}

/* 响应式 */
@media (max-width: 768px) {
  .lantern-container {
    right: 20px;
  }
  
  .lantern {
    width: 50px;
    height: 70px;
  }
  
  .lantern-body {
    width: 50px;
    height: 50px;
  }
  
  .lantern-text {
    font-size: 16px;
  }
}
</style>
