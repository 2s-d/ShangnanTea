<template>
  <div class="lantern-wrapper" :style="{ top: position.y + 'px', right: position.x + 'px' }">
    <div class="string"></div>
    <div 
      class="lantern"
      @mousedown="startDrag"
      @click="handleClick"
    >
      <div class="top-ring"></div>
      <div class="body">
        <div class="light"></div>
        <div class="frame"></div>
      </div>
      <div class="bottom-ring"></div>
      <div class="tassels">
        <span></span><span></span><span></span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  currentTheme: String,
  onToggle: Function
})

const position = ref({ x: 180, y: 0 })
const isDragging = ref(false)

const startDrag = (e) => {
  isDragging.value = true
  const startY = e.clientY
  
  const onMove = (e) => {
    if (!isDragging.value) return
    if (Math.abs(e.clientY - startY) > 50) {
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

.string {
  width: 3px;
  height: 70px;
  background: linear-gradient(to bottom, #654321, #8b4513);
  margin: 0 auto;
  box-shadow: 1px 1px 2px rgba(0,0,0,0.3);
}

.lantern {
  width: 60px;
  animation: swing 4s ease-in-out infinite;
}

@keyframes swing {
  0%, 100% { transform: rotate(0deg); }
  25% { transform: rotate(3deg); }
  75% { transform: rotate(-3deg); }
}

.lantern:hover {
  animation-play-state: paused;
  transform: scale(1.1);
}

.top-ring {
  width: 50px;
  height: 10px;
  background: linear-gradient(to bottom, #ffd700, #daa520);
  border-radius: 50% 50% 0 0;
  margin: 0 auto;
  box-shadow: 0 2px 4px rgba(0,0,0,0.3);
}

.body {
  width: 60px;
  height: 70px;
  background: radial-gradient(ellipse at center, #ff6666 0%, #ff3333 50%, #cc0000 100%);
  border-radius: 45% 45% 50% 50%;
  position: relative;
  box-shadow: 
    0 6px 12px rgba(0,0,0,0.4),
    inset 0 -10px 20px rgba(0,0,0,0.3),
    0 0 30px rgba(255,51,51,0.5);
  margin: 2px auto;
}

.light {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 40px;
  height: 50px;
  background: radial-gradient(ellipse, rgba(255,255,220,0.7) 0%, transparent 70%);
  animation: glow 2s ease-in-out infinite;
}

@keyframes glow {
  0%, 100% { opacity: 0.6; }
  50% { opacity: 1; }
}

.frame {
  position: absolute;
  width: 100%;
  height: 100%;
  border: 2px solid rgba(212,175,55,0.3);
  border-radius: 45% 45% 50% 50%;
  box-sizing: border-box;
}

.frame::before,
.frame::after {
  content: '';
  position: absolute;
  width: 100%;
  height: 1px;
  background: linear-gradient(90deg, transparent, #d4af37, transparent);
  left: 0;
}

.frame::before { top: 30%; }
.frame::after { bottom: 30%; }

.bottom-ring {
  width: 50px;
  height: 10px;
  background: linear-gradient(to top, #ffd700, #daa520);
  border-radius: 0 0 50% 50%;
  margin: 0 auto;
  box-shadow: 0 2px 4px rgba(0,0,0,0.3);
}

.tassels {
  display: flex;
  justify-content: center;
  gap: 3px;
  margin-top: 2px;
}

.tassels span {
  display: block;
  width: 2px;
  height: 15px;
  background: linear-gradient(to bottom, #d4af37, #aa8c2c);
  position: relative;
}

.tassels span::after {
  content: '';
  position: absolute;
  bottom: -5px;
  left: 50%;
  transform: translateX(-50%);
  width: 5px;
  height: 5px;
  background: #ff3333;
  border-radius: 50%;
}
</style>
