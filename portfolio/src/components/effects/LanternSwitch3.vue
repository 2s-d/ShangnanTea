<template>
  <div class="lantern-wrapper" :style="{ top: position.y + 'px', right: position.x + 'px' }">
    <div class="cord"></div>
    <div 
      class="lantern"
      @mousedown="startDrag"
      @click="handleClick"
    >
      <div class="cap top"></div>
      <div class="main-body">
        <div class="inner-light"></div>
        <div class="decoration"></div>
      </div>
      <div class="cap bottom"></div>
      <div class="fringe"></div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  currentTheme: String,
  onToggle: Function
})

const position = ref({ x: 280, y: 0 })

const startDrag = (e) => {
  const startY = e.clientY
  
  const onMove = (e) => {
    if (Math.abs(e.clientY - startY) > 50) {
      props.onToggle?.()
      endDrag()
    }
  }
  
  const endDrag = () => {
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

.cord {
  width: 2.5px;
  height: 65px;
  background: repeating-linear-gradient(
    to bottom,
    #8b4513 0px,
    #654321 2px,
    #8b4513 4px
  );
  margin: 0 auto;
  box-shadow: 2px 0 3px rgba(0,0,0,0.3);
}

.lantern {
  width: 55px;
  animation: gentle-swing 5s ease-in-out infinite;
  transition: transform 0.3s;
}

@keyframes gentle-swing {
  0%, 100% { transform: rotate(0deg); }
  50% { transform: rotate(2deg); }
}

.lantern:hover {
  transform: scale(1.15) !important;
  animation-play-state: paused;
}

.cap {
  width: 45px;
  height: 12px;
  background: radial-gradient(ellipse at center, #ffd700 0%, #daa520 100%);
  margin: 0 auto;
  position: relative;
  box-shadow: 0 3px 6px rgba(0,0,0,0.4);
}

.cap.top {
  border-radius: 50% 50% 20% 20%;
}

.cap.top::before {
  content: '';
  position: absolute;
  top: -5px;
  left: 50%;
  transform: translateX(-50%);
  width: 15px;
  height: 6px;
  background: #daa520;
  border-radius: 50% 50% 0 0;
}

.cap.bottom {
  border-radius: 20% 20% 50% 50%;
}

.main-body {
  width: 55px;
  height: 65px;
  background: linear-gradient(180deg, #ff5555 0%, #ff3333 50%, #dd0000 100%);
  border-radius: 40% 40% 50% 50%;
  position: relative;
  margin: 3px auto;
  box-shadow: 
    0 8px 16px rgba(0,0,0,0.5),
    inset 0 -15px 25px rgba(0,0,0,0.4),
    inset 0 15px 25px rgba(255,255,255,0.3),
    0 0 25px rgba(255,51,51,0.6);
}

.inner-light {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 35px;
  height: 45px;
  background: radial-gradient(ellipse, rgba(255,255,230,0.8) 0%, rgba(255,255,200,0.4) 50%, transparent 80%);
  border-radius: 50%;
  animation: pulse-light 3s ease-in-out infinite;
}

@keyframes pulse-light {
  0%, 100% { opacity: 0.7; transform: translate(-50%, -50%) scale(1); }
  50% { opacity: 1; transform: translate(-50%, -50%) scale(1.05); }
}

.decoration {
  position: absolute;
  width: 20px;
  height: 20px;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  border: 2px solid rgba(212,175,55,0.7);
  border-radius: 50%;
  box-shadow: 0 0 10px rgba(212,175,55,0.5);
}

.decoration::before,
.decoration::after {
  content: '';
  position: absolute;
  width: 100%;
  height: 1px;
  background: rgba(212,175,55,0.7);
  top: 50%;
  left: 0;
}

.decoration::after {
  transform: rotate(90deg);
}

.fringe {
  width: 100%;
  height: 18px;
  display: flex;
  justify-content: center;
  gap: 2px;
  margin-top: 2px;
}

.fringe::before,
.fringe::after {
  content: '';
  width: 1.5px;
  height: 18px;
  background: linear-gradient(to bottom, #d4af37 0%, #aa8c2c 70%, transparent 100%);
  display: block;
}

.fringe::before {
  margin-right: 2px;
}

.fringe::after {
  margin-left: 2px;
}
</style>
