<template>
  <div ref="tiltRef" class="tilt-card">
    <slot></slot>
    <!-- 底部边缘死区 overlay，阻止鼠标事件 -->
    <div class="edge-deadzone"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import VanillaTilt from 'vanilla-tilt'

const props = defineProps({
  maxTilt: {
    type: Number,
    default: 10
  }
})

const tiltRef = ref(null)
let tiltInstance = null

onMounted(() => {
  if (tiltRef.value) {
    const element = tiltRef.value
    
    // 初始化 VanillaTilt，使用标准配置
    VanillaTilt.init(element, {
      max: props.maxTilt,
      speed: 400,
      glare: false,
      scale: 1.0, // 移除放大效果，避免边缘抖动
      perspective: 1000,
      transition: true,
      easing: "cubic-bezier(.03,.98,.52,.99)",
      reset: true,
      gyroscope: false
    })
    
    tiltInstance = element.vanillaTilt
  }
})

onUnmounted(() => {
  if (tiltInstance) {
    tiltInstance.destroy()
  }
})
</script>

<style scoped>
.tilt-card {
  position: relative;
  transform-style: preserve-3d;
  will-change: transform;
  backface-visibility: hidden;
  transform-origin: center center;
}

/* 底部边缘死区 - 主流解决方案：用 CSS overlay 阻止鼠标事件 */
.edge-deadzone {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 20%; /* 底部10%区域为死区 */
  pointer-events: auto; /* 阻止鼠标事件穿透到底层 */
  z-index: 10; /* 确保在内容之上 */
  /* 透明，不影响视觉效果 */
  background: transparent;
  /* 可选：添加调试边框（开发时可见，生产环境删除） */
  /* border-top: 1px solid rgba(255, 0, 0, 0.3); */
}
</style>
