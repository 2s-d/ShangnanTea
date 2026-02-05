<template>
  <div class="dice-container">
    <div 
      class="dice" 
      :class="{ rolling: isRolling }"
      @click="rollDice"
      :style="diceStyle"
    >
      <div class="dice-number">{{ currentNumber }}</div>
    </div>
    <div class="result" v-if="showResult">
      结果: {{ result }}
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const currentNumber = ref(1)
const result = ref(0)
const isRolling = ref(false)
const showResult = ref(false)
const rotationX = ref(0)
const rotationY = ref(0)
const rotationZ = ref(0)

const diceStyle = computed(() => ({
  transform: `rotateX(${rotationX.value}deg) rotateY(${rotationY.value}deg) rotateZ(${rotationZ.value}deg)`
}))

const rollDice = () => {
  if (isRolling.value) return
  
  isRolling.value = true
  showResult.value = false
  
  // 随机旋转角度
  const randomRotations = Math.floor(Math.random() * 5) + 5 // 5-10圈
  rotationX.value += 360 * randomRotations + Math.random() * 360
  rotationY.value += 360 * randomRotations + Math.random() * 360
  rotationZ.value += 360 * randomRotations + Math.random() * 360
  
  // 快速变换数字
  let count = 0
  const interval = setInterval(() => {
    currentNumber.value = Math.floor(Math.random() * 16) + 1
    count++
    if (count > 20) {
      clearInterval(interval)
    }
  }, 50)
  
  // 2秒后显示结果
  setTimeout(() => {
    const finalResult = Math.floor(Math.random() * 16) + 1
    currentNumber.value = finalResult
    result.value = finalResult
    isRolling.value = false
    showResult.value = true
  }, 2000)
}
</script>

<style scoped>
.dice-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  padding: 40px;
}

.dice {
  width: 120px;
  height: 120px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transform-style: preserve-3d;
  transition: transform 2s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
  user-select: none;
  position: relative;
}

.dice::before {
  content: '';
  position: absolute;
  inset: 3px;
  background: linear-gradient(135deg, #764ba2 0%, #667eea 100%);
  border-radius: 17px;
  z-index: 0;
}

.dice:hover {
  box-shadow: 0 15px 40px rgba(102, 126, 234, 0.5);
  transform: scale(1.05);
}

.dice:active {
  transform: scale(0.95);
}

.dice.rolling {
  animation: shake 0.1s infinite;
  pointer-events: none;
}

.dice-number {
  font-size: 48px;
  font-weight: bold;
  color: white;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.5);
  z-index: 1;
  position: relative;
}

.result {
  font-size: 24px;
  font-weight: bold;
  color: #667eea;
  animation: fadeIn 0.5s ease-in;
  padding: 10px 20px;
  background: rgba(102, 126, 234, 0.1);
  border-radius: 10px;
  border: 2px solid #667eea;
}

@keyframes shake {
  0%, 100% { transform: translateX(0) translateY(0); }
  25% { transform: translateX(-2px) translateY(-2px); }
  50% { transform: translateX(2px) translateY(2px); }
  75% { transform: translateX(-2px) translateY(2px); }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
