<template>
  <span>{{ displayNumber }}</span>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const props = defineProps({
  value: {
    type: Number,
    required: true
  },
  duration: {
    type: Number,
    default: 1500
  },
  delay: {
    type: Number,
    default: 0
  }
})

const displayNumber = ref(0)
let observer = null

const animate = () => {
  const startTime = performance.now()
  const startValue = 0
  const endValue = props.value

  const updateNumber = (currentTime) => {
    const elapsed = currentTime - startTime
    const progress = Math.min(elapsed / props.duration, 1)
    
    // 使用 easeOutCubic 缓动
    const easeProgress = 1 - Math.pow(1 - progress, 3)
    displayNumber.value = Math.floor(startValue + (endValue - startValue) * easeProgress)

    if (progress < 1) {
      requestAnimationFrame(updateNumber)
    } else {
      displayNumber.value = endValue
    }
  }

  setTimeout(() => {
    requestAnimationFrame(updateNumber)
  }, props.delay)
}

onMounted(() => {
  // 使用 Intersection Observer 在元素进入视口时触发动画
  observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          animate()
          observer.disconnect()
        }
      })
    },
    { threshold: 0.5 }
  )

  observer.observe(document.querySelector('.animate-number'))
})
</script>

<style scoped>
span {
  display: inline-block;
}
</style>
