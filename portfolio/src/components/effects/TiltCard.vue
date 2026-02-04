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
    VanillaTilt.init(tiltRef.value, {
      max: props.maxTilt,
      speed: 1000, // 降低响应速度，减少抖动
      glare: false,
      scale: 1.0, // 移除放大效果，避免边缘抖动
      perspective: 1000,
      transition: false, // 禁用过渡动画，避免与实时计算冲突
      easing: "cubic-bezier(.03,.98,.52,.99)",
      reset: true, // 鼠标离开时重置状态
      gyroscope: false, // 禁用陀螺仪，避免不必要的计算
      reverse: false, // 确保方向正确
      axis: null, // 允许所有方向的倾斜
      disableAxis: null // 不禁用任何轴
    })
    tiltInstance = tiltRef.value.vanillaTilt
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
  transform-style: preserve-3d;
}
</style>
