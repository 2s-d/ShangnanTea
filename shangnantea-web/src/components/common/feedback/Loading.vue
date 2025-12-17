<template>
  <transition name="fade">
    <div 
      v-if="visible" 
      :class="['loading', { 'loading--fullscreen': fullscreen }]"
      :style="backgroundColor ? { backgroundColor } : {}"
    >
      <div class="loading__spinner">
        <svg class="loading__circular" viewBox="25 25 50 50">
          <circle class="loading__path" cx="50" cy="50" r="20" fill="none"/>
        </svg>
        <p v-if="text" class="loading__text">{{ text }}</p>
      </div>
      <!-- 显示背景遮罩 -->
      <div v-if="showMask" class="loading__mask"></div>
    </div>
  </transition>
</template>

<script>
export default {
  name: 'Loading',
  props: {
    // 是否显示加载组件
    visible: {
      type: Boolean,
      default: false
    },
    // 是否全屏显示
    fullscreen: {
      type: Boolean,
      default: false
    },
    // 加载提示文本
    text: {
      type: String,
      default: '加载中...'
    },
    // 背景颜色
    backgroundColor: {
      type: String,
      default: ''
    },
    // 是否显示背景遮罩
    showMask: {
      type: Boolean,
      default: true
    }
  }
}
</script>

<style lang="scss" scoped>
.loading {
  position: relative;
  overflow: hidden;
  
  &--fullscreen {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 9999;
    display: flex;
    justify-content: center;
    align-items: center;
  }
  
  &__spinner {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    z-index: 10;
    display: flex;
    flex-direction: column;
    align-items: center;
  }
  
  &__circular {
    width: 42px;
    height: 42px;
    animation: loading-rotate 2s linear infinite;
  }
  
  &__path {
    stroke-dasharray: 90, 150;
    stroke-dashoffset: 0;
    stroke-width: 2;
    stroke: var(--primary-color, #409EFF);
    stroke-linecap: round;
    animation: loading-dash 1.5s ease-in-out infinite;
  }
  
  &__text {
    color: var(--primary-color, #409EFF);
    margin-top: 10px;
    font-size: 14px;
  }
  
  &__mask {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(255, 255, 255, 0.8);
    z-index: 5;
  }
}

@keyframes loading-rotate {
  100% {
    transform: rotate(360deg);
  }
}

@keyframes loading-dash {
  0% {
    stroke-dasharray: 1, 200;
    stroke-dashoffset: 0;
  }
  50% {
    stroke-dasharray: 90, 150;
    stroke-dashoffset: -40px;
  }
  100% {
    stroke-dasharray: 90, 150;
    stroke-dashoffset: -120px;
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style> 