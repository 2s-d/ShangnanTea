<template>
  <div class="test-container">
    <h2>Canvas与Box-Shadow测试</h2>
    <div class="cards-wrapper">
      <!-- 测试1：只有box-shadow -->
      <div class="test-card card1">
        <h3>测试1</h3>
        <p>只有 box-shadow</p>
        <p>hover看效果</p>
      </div>

      <!-- 测试2：box-shadow + Canvas覆盖(z-index:10) -->
      <div class="test-card card2">
        <canvas ref="canvas2" class="canvas-overlay"></canvas>
        <h3>测试2</h3>
        <p>box-shadow + Canvas(z:10)</p>
        <p>hover看效果</p>
      </div>

      <!-- 测试3：box-shadow + Canvas在下层(z-index:-1) -->
      <div class="test-card card3">
        <canvas ref="canvas3" class="canvas-under"></canvas>
        <h3>测试3</h3>
        <p>box-shadow + Canvas(z:-1)</p>
        <p>hover看效果</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const canvas2 = ref(null)
const canvas3 = ref(null)

onMounted(() => {
  [canvas2.value, canvas3.value].forEach(canvas => {
    if (!canvas) return
    canvas.width = 200
    canvas.height = 280
    const ctx = canvas.getContext('2d')
    
    // 绘制一个蓝色边框
    ctx.strokeStyle = 'rgba(100, 200, 255, 0.8)'
    ctx.lineWidth = 2
    ctx.strokeRect(5, 5, 190, 270)
    
    // 绘制一些点
    ctx.fillStyle = 'rgba(100, 200, 255, 0.6)'
    for (let i = 0; i < 5; i++) {
      ctx.beginPath()
      ctx.arc(50 + i * 30, 150, 3, 0, Math.PI * 2)
      ctx.fill()
    }
  })
})
</script>

<style scoped>
.test-container {
  padding: 50px;
  background: #1a1a2e;
  min-height: 100vh;
}

h2 {
  color: white;
  text-align: center;
  margin-bottom: 50px;
}

.cards-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 50px;
}

.test-card {
  width: 200px;
  height: 280px;
  background: white;
  border-radius: 12px;
  padding: 20px;
  transition: all 0.3s;
  position: relative;
}

/* 测试1：只有box-shadow */
.card1:hover {
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.4);
}

/* 测试2：box-shadow + Canvas覆盖 */
.card2:hover {
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.4);
}

.canvas-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 10;
}

/* 测试3：box-shadow + Canvas在下层 */
.card3:hover {
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.4);
}

.canvas-under {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: -1;
}

h3 {
  margin: 0 0 10px 0;
  font-size: 16px;
  color: #333;
  position: relative;
  z-index: 5;
}

p {
  font-size: 14px;
  color: #666;
  margin: 5px 0;
  position: relative;
  z-index: 5;
}
</style>
