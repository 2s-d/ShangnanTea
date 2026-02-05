<template>
  <div class="skill-progress-wrapper">
    <div class="skill-info">
      <span class="skill-percentage">{{ displayPercentage }}%</span>
    </div>
    <div class="progress-bar">
      <div 
        class="progress-fill" 
        :style="{ 
          width: `${displayPercentage}%`,
          background: color 
        }"
      >
        <div class="progress-shine"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const props = defineProps({
  name: String,
  level: Number,
  color: String
})

const displayPercentage = ref(0)
let observer = null

const animateProgress = () => {
  const duration = 1500
  const startTime = performance.now()
  
  const animate = (currentTime) => {
    const elapsed = currentTime - startTime
    const progress = Math.min(elapsed / duration, 1)
    
    // easeOutCubic
    const easeProgress = 1 - Math.pow(1 - progress, 3)
    displayPercentage.value = Math.floor(props.level * easeProgress)
    
    if (progress < 1) {
      requestAnimationFrame(animate)
    } else {
      displayPercentage.value = props.level
    }
  }
  
  requestAnimationFrame(animate)
}

onMounted(() => {
  observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          animateProgress()
          observer.disconnect()
        }
      })
    },
    { threshold: 0.5 }
  )
  
  const element = document.querySelector('.skill-progress-wrapper')
  if (element) {
    observer.observe(element)
  }
})
</script>

<style scoped>
.skill-progress-wrapper {
  width: 100%;
}

.skill-info {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: var(--spacing-sm);
}

.skill-percentage {
  font-size: var(--text-lg);
  font-weight: var(--font-bold);
  color: var(--color-primary);
  font-variant-numeric: tabular-nums;
}

.progress-bar {
  width: 100%;
  height: 10px;
  background: var(--color-bg-tertiary);
  border-radius: var(--radius-full);
  overflow: hidden;
  position: relative;
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.1);
}

.progress-fill {
  height: 100%;
  border-radius: var(--radius-full);
  transition: width 0.3s ease;
  position: relative;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.progress-shine {
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.4),
    transparent
  );
  animation: shine 2s ease-in-out infinite;
}

@keyframes shine {
  0% {
    left: -100%;
  }
  50%, 100% {
    left: 100%;
  }
}
</style>
