<template>
  <div class="lantern-container">
    <!-- 摆动的绳子 -->
    <svg class="rope-svg" width="4" :height="ropeLength + 20">
      <path 
        :d="ropePath" 
        stroke="#8b4513" 
        stroke-width="3" 
        fill="none"
        filter="url(#rope-shadow)"
      />
      <defs>
        <filter id="rope-shadow">
          <feGaussianBlur in="SourceAlpha" stdDeviation="1"/>
          <feOffset dx="1" dy="1" result="offsetblur"/>
          <feMerge>
            <feMergeNode/>
            <feMergeNode in="SourceGraphic"/>
          </feMerge>
        </filter>
      </defs>
    </svg>
    
    <!-- 灯笼 -->
    <div 
      class="lantern"
      :class="{ 'theme-coderain': currentTheme === 'coderain' }"
      :style="lanternStyle"
      @click="handleClick"
      @mouseenter="isHovering = true"
      @mouseleave="isHovering = false"
    >
      <!-- 灯笼顶盖 -->
      <div class="lantern-cap top-cap"></div>
      
      <!-- 灯笼主体 -->
      <div class="lantern-body">
        <!-- 内部光晕 -->
        <div class="inner-glow"></div>
        
        <!-- 装饰条纹 -->
        <div class="stripe stripe-1"></div>
        <div class="stripe stripe-2"></div>
        <div class="stripe stripe-3"></div>
        
        <!-- 中心装饰 -->
        <div class="center-ornament">
          <div class="ornament-circle"></div>
        </div>
      </div>
      
      <!-- 灯笼底盖 -->
      <div class="lantern-cap bottom-cap"></div>
      
      <!-- 流苏 -->
      <div class="tassel-group">
        <div class="tassel" v-for="i in 5" :key="i" :style="{ animationDelay: `${i * 0.1}s` }">
          <div class="tassel-thread"></div>
          <div class="tassel-bead"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  currentTheme: String,
  onToggle: Function
})

const ropeLength = ref(80)
const swingAngle = ref(0)
const isHovering = ref(false)
let swingInterval = null

// 绳子路径（贝塞尔曲线模拟摆动）
const ropePath = computed(() => {
  const angle = swingAngle.value
  const controlX = 2 + Math.sin(angle) * 8
  const endX = 2 + Math.sin(angle) * 15
  return `M 2 0 Q ${controlX} ${ropeLength.value / 2} ${endX} ${ropeLength.value}`
})

// 灯笼样式（跟随绳子摆动）
const lanternStyle = computed(() => {
  const angle = swingAngle.value
  const translateX = Math.sin(angle) * 15
  const rotate = angle * (180 / Math.PI) * 0.5
  const scale = isHovering.value ? 1.05 : 1
  
  return {
    transform: `translateX(${translateX}px) rotate(${rotate}deg) scale(${scale})`
  }
})

// 点击切换主题
const handleClick = () => {
  props.onToggle?.()
  // 点击时增加摆动幅度
  const originalAmplitude = 0.15
  let clickAmplitude = 0.3
  const decay = 0.95
  
  const clickSwing = setInterval(() => {
    clickAmplitude *= decay
    if (clickAmplitude < originalAmplitude) {
      clearInterval(clickSwing)
    }
  }, 50)
}

// 自然摆动动画
onMounted(() => {
  let time = 0
  swingInterval = setInterval(() => {
    time += 0.02
    swingAngle.value = Math.sin(time) * 0.15 // 小幅度摆动
  }, 16)
})

onUnmounted(() => {
  if (swingInterval) {
    clearInterval(swingInterval)
  }
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

.rope-svg {
  display: block;
  margin: 0 auto;
  filter: drop-shadow(0 2px 3px rgba(0, 0, 0, 0.3));
}

/* 灯笼 */
.lantern {
  width: 70px;
  cursor: pointer;
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  transform-origin: top center;
  margin-top: -10px;
}

/* 顶盖和底盖 */
.lantern-cap {
  width: 60px;
  height: 12px;
  margin: 0 auto;
  background: linear-gradient(135deg, #d4af37 0%, #aa8c2c 50%, #d4af37 100%);
  position: relative;
  box-shadow: 
    0 2px 4px rgba(0, 0, 0, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

.top-cap {
  border-radius: 50% 50% 20% 20% / 60% 60% 10% 10%;
}

.top-cap::before {
  content: '';
  position: absolute;
  top: -6px;
  left: 50%;
  transform: translateX(-50%);
  width: 20px;
  height: 8px;
  background: linear-gradient(135deg, #d4af37, #aa8c2c);
  border-radius: 50% 50% 0 0;
  box-shadow: 0 -1px 2px rgba(0, 0, 0, 0.2);
}

.bottom-cap {
  border-radius: 20% 20% 50% 50% / 10% 10% 60% 60%;
}

/* 灯笼主体 */
.lantern-body {
  width: 70px;
  height: 85px;
  background: linear-gradient(135deg, 
    #ff3333 0%, 
    #ff5555 25%,
    #ff3333 50%,
    #cc0000 75%,
    #ff3333 100%
  );
  border-radius: 45% 45% 50% 50% / 40% 40% 60% 60%;
  position: relative;
  margin: 2px auto;
  box-shadow: 
    0 8px 16px rgba(0, 0, 0, 0.4),
    inset 0 -15px 30px rgba(0, 0, 0, 0.3),
    inset 0 15px 30px rgba(255, 255, 255, 0.2),
    0 0 20px rgba(255, 51, 51, 0.3);
  overflow: hidden;
  animation: lantern-glow 3s ease-in-out infinite;
}

/* 内部光晕 */
.inner-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 50px;
  height: 60px;
  background: radial-gradient(ellipse, 
    rgba(255, 255, 200, 0.6) 0%, 
    rgba(255, 255, 150, 0.3) 40%,
    transparent 70%
  );
  border-radius: 50%;
  animation: glow-pulse 2.5s ease-in-out infinite;
}

/* 装饰条纹 */
.stripe {
  position: absolute;
  width: 100%;
  height: 1.5px;
  background: linear-gradient(90deg, 
    transparent 0%, 
    rgba(212, 175, 55, 0.8) 20%,
    rgba(212, 175, 55, 1) 50%,
    rgba(212, 175, 55, 0.8) 80%,
    transparent 100%
  );
  left: 0;
  box-shadow: 0 0 4px rgba(212, 175, 55, 0.6);
}

.stripe-1 { top: 25%; }
.stripe-2 { top: 50%; }
.stripe-3 { top: 75%; }

/* 中心装饰 */
.center-ornament {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 24px;
  height: 24px;
}

.ornament-circle {
  width: 100%;
  height: 100%;
  border: 2px solid rgba(212, 175, 55, 0.9);
  border-radius: 50%;
  box-shadow: 
    0 0 8px rgba(212, 175, 55, 0.6),
    inset 0 0 8px rgba(255, 255, 200, 0.3);
  animation: ornament-spin 8s linear infinite;
}

.ornament-circle::before,
.ornament-circle::after {
  content: '';
  position: absolute;
  background: rgba(212, 175, 55, 0.8);
  border-radius: 50%;
}

.ornament-circle::before {
  width: 4px;
  height: 4px;
  top: 50%;
  left: 2px;
  transform: translateY(-50%);
}

.ornament-circle::after {
  width: 4px;
  height: 4px;
  top: 50%;
  right: 2px;
  transform: translateY(-50%);
}

/* 流苏组 */
.tassel-group {
  display: flex;
  justify-content: center;
  gap: 4px;
  margin-top: 2px;
}

.tassel {
  display: flex;
  flex-direction: column;
  align-items: center;
  animation: tassel-swing 2s ease-in-out infinite;
}

.tassel-thread {
  width: 1.5px;
  height: 18px;
  background: linear-gradient(to bottom, #d4af37, #aa8c2c);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.tassel-bead {
  width: 6px;
  height: 8px;
  background: linear-gradient(135deg, #ff3333, #cc0000);
  border-radius: 50% 50% 60% 60%;
  box-shadow: 0 2px 3px rgba(0, 0, 0, 0.4);
}

/* 代码雨主题样式 */
.theme-coderain .lantern-body {
  box-shadow: 
    0 8px 16px rgba(0, 0, 0, 0.4),
    inset 0 -15px 30px rgba(0, 0, 0, 0.3),
    inset 0 15px 30px rgba(255, 255, 255, 0.2),
    0 0 40px rgba(255, 51, 51, 0.8),
    0 0 60px rgba(255, 51, 51, 0.4);
  animation: lantern-glow-strong 2s ease-in-out infinite;
}

.theme-coderain .inner-glow {
  background: radial-gradient(ellipse, 
    rgba(255, 255, 200, 0.9) 0%, 
    rgba(255, 255, 150, 0.6) 40%,
    transparent 70%
  );
}

/* 动画 */
@keyframes lantern-glow {
  0%, 100% {
    box-shadow: 
      0 8px 16px rgba(0, 0, 0, 0.4),
      inset 0 -15px 30px rgba(0, 0, 0, 0.3),
      inset 0 15px 30px rgba(255, 255, 255, 0.2),
      0 0 20px rgba(255, 51, 51, 0.3);
  }
  50% {
    box-shadow: 
      0 8px 16px rgba(0, 0, 0, 0.4),
      inset 0 -15px 30px rgba(0, 0, 0, 0.3),
      inset 0 15px 30px rgba(255, 255, 255, 0.2),
      0 0 30px rgba(255, 51, 51, 0.5);
  }
}

@keyframes lantern-glow-strong {
  0%, 100% {
    box-shadow: 
      0 8px 16px rgba(0, 0, 0, 0.4),
      inset 0 -15px 30px rgba(0, 0, 0, 0.3),
      inset 0 15px 30px rgba(255, 255, 255, 0.2),
      0 0 40px rgba(255, 51, 51, 0.8),
      0 0 60px rgba(255, 51, 51, 0.4);
  }
  50% {
    box-shadow: 
      0 8px 16px rgba(0, 0, 0, 0.4),
      inset 0 -15px 30px rgba(0, 0, 0, 0.3),
      inset 0 15px 30px rgba(255, 255, 255, 0.2),
      0 0 50px rgba(255, 51, 51, 1),
      0 0 80px rgba(255, 51, 51, 0.6);
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

@keyframes ornament-spin {
  from { transform: translate(-50%, -50%) rotate(0deg); }
  to { transform: translate(-50%, -50%) rotate(360deg); }
}

@keyframes tassel-swing {
  0%, 100% { transform: translateX(0) rotate(0deg); }
  25% { transform: translateX(-1px) rotate(-2deg); }
  75% { transform: translateX(1px) rotate(2deg); }
}

/* 响应式 */
@media (max-width: 768px) {
  .lantern-container {
    right: 20px;
  }
  
  .lantern {
    width: 60px;
  }
  
  .lantern-body {
    width: 60px;
    height: 75px;
  }
  
  .lantern-cap {
    width: 50px;
  }
}
</style>
