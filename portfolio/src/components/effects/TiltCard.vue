<template>
  <div ref="tiltRef" class="tilt-card">
    <slot></slot>
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
    
    // 初始化 VanillaTilt，使用最稳定的配置
    VanillaTilt.init(element, {
      max: props.maxTilt,
      speed: 3000, // 大幅降低响应速度，减少抖动
      glare: false,
      scale: 1.0, // 移除放大效果
      perspective: 1000,
      transition: false, // 禁用过渡动画，避免与实时计算冲突
      easing: "cubic-bezier(.03,.98,.52,.99)",
      reset: true,
      gyroscope: false,
      reverse: false,
      maxGlare: 0
    })
    
    tiltInstance = element.vanillaTilt
    
    // 拦截 VanillaTilt 的鼠标移动事件，在底部边缘时完全禁用 tilt
    const originalOnMouseMove = tiltInstance.onMouseMove.bind(tiltInstance)
    let isBottomEdge = false
    
    tiltInstance.onMouseMove = function(e) {
      const rect = element.getBoundingClientRect()
      const y = e.clientY - rect.top
      const height = rect.height
      const relY = y / height
      
      // 检测是否在底部边缘（最后8%区域）
      const wasBottomEdge = isBottomEdge
      isBottomEdge = relY > 0.92
      
      if (isBottomEdge) {
        // 在底部边缘时，完全阻止 tilt 计算
        // 如果刚从非边缘进入边缘，重置 transform
        if (!wasBottomEdge) {
          // 重置到初始状态，避免卡片上弹
          element.style.transform = 'perspective(1000px) scale3d(1, 1, 1) rotateX(0deg) rotateY(0deg)'
        }
        // 不调用原始的 onMouseMove，完全阻止 tilt
        return
      }
      
      // 不在底部边缘时，正常调用原始方法
      originalOnMouseMove(e)
    }
    
    // 鼠标离开时重置状态
    const handleMouseLeave = () => {
      isBottomEdge = false
    }
    
    element.addEventListener('mouseleave', handleMouseLeave)
    
    // 保存清理函数
    tiltRef.value._cleanup = () => {
      element.removeEventListener('mouseleave', handleMouseLeave)
      // 恢复原始方法
      if (tiltInstance && originalOnMouseMove) {
        tiltInstance.onMouseMove = originalOnMouseMove
      }
    }
  }
})

onUnmounted(() => {
  if (tiltRef.value && tiltRef.value._cleanup) {
    tiltRef.value._cleanup()
  }
  if (tiltInstance) {
    tiltInstance.destroy()
  }
})
</script>

<style scoped>
.tilt-card {
  transform-style: preserve-3d;
  will-change: transform; /* 优化渲染性能 */
  backface-visibility: hidden; /* 防止背面闪烁 */
  transform-origin: center center; /* 确保变换原点居中 */
}
</style>
