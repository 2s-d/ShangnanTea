<template>
  <div class="lantern-wrapper" :style="{ top: position.y + 'px', right: position.x + 'px' }">
    <!-- 绳子 -->
    <div class="rope"></div>
    
    <!-- 灯笼 -->
    <div 
      class="lantern"
      @mousedown="startDrag"
      @click="handleClick"
    >
      <div class="lantern-top"></div>
      <div class="lantern-body">
        <div class="glow"></div>
      </div>
      <div class="lantern-bottom"></div>
      <div class="tassel"></div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  currentTheme: String,
  onToggle: Function
})

const position = ref({ x: 80, y: 0 })
const isDragging = ref(false)
const startPos = ref({ x: 0, y: 0 })

const startDrag = (e) => {
  isDragging.value = true
  startPos.value = { x: e.clientY, y: e.clientY }
  
  const onMove = (e) => {
    if (!isDragging.value) return
    const deltaY = e.clientY - startPos.value.y
    if (Math.abs(deltaY) > 50) {
      props.onToggle?.()
      endDrag()
    }
  }
  
  const endDrag = () => {
    isDragging.value = false
    document.removeEventListener('mousemove', onMove)
    document.removeEventListener('mouseup', endDrag)
  }
  
  document.addEventListener('mousemove', onMove)
  document.addEventListener('mouseup', endDrag)
}

const handleClick = () => {
  props.onToggle?.()
}
</script>

<style scoped>
.lantern-wrapper {
  position: fixed;
  z-index: 9999;
  cursor: pointer;
}

.rope {
  width: 2px;
  height: 60px;
  background: #8b4513;
  margin: 0 auto;
}

.lantern {
  width: 50px;
  transition: transform 0.3s;
}

.lantern:hover {
  transform: scale(1.1);
}

.lantern-top {
  width: 40px;
  height: 8px;
  background: #d4af37;
  border-radius: 50% 50% 0 0;
  margin: 0 auto;
}

.lantern-body {
  width: 50px;
  height: 60px;
  background: linear-gradient(135deg, #ff4444, #cc0000);
  border-radius: 50%;
  position: relative;
  box-shadow: 0 4px 8px rgba(0,0,0,0.3), 0 0 20px rgba(255,68,68,0.4);
}

.glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 30px;
  height: 30px;
  background: radial-gradient(circle, rgba(255,255,200,0.6), transparent);
  border-radius: 50%;
}

.lantern-bottom {
  width: 40px;
  height: 8px;
  background: #d4af37;
  border-radius: 0 0 50% 50%;
  margin: 0 auto;
}

.tassel {
  width: 2px;
  height: 20px;
  background: #d4af37;
  margin: 0 auto;
}

.tassel::after {
  content: '';
  display: block;
  width: 10px;
  height: 10px;
  background: #ff4444;
  border-radius: 50%;
  margin: 0 auto;
}
</style>
