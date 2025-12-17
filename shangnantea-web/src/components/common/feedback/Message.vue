<template>
  <transition name="message-fade">
    <div 
      v-if="visible" 
      class="message" 
      :class="[`message--${type}`, customClass]" 
      :style="customStyle"
    >
      <div class="message__content">
        <i class="message__icon" :class="iconClass"></i>
        <span class="message__text">{{ message }}</span>
      </div>
      <i v-if="showClose" class="message__close el-icon-close" @click="close"></i>
    </div>
  </transition>
</template>

<script>
export default {
  name: 'Message',
  props: {
    // 消息类型：success, warning, info, error
    type: {
      type: String,
      default: 'info',
      validator: val => ['success', 'warning', 'info', 'error'].includes(val)
    },
    // 消息文本
    message: {
      type: String,
      required: true
    },
    // 显示时长(ms)，0表示不会自动关闭
    duration: {
      type: Number,
      default: 3000
    },
    // 是否显示关闭按钮
    showClose: {
      type: Boolean,
      default: true
    },
    // 自定义类名
    customClass: {
      type: String,
      default: ''
    },
    // 自定义样式
    customStyle: {
      type: Object,
      default: () => ({})
    },
    // 关闭时的回调函数
    onClose: {
      type: Function,
      default: null
    },
    // 组件id，用于多消息管理
    id: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      visible: false,
      timer: null
    }
  },
  computed: {
    iconClass() {
      const iconMap = {
        success: 'el-icon-success',
        warning: 'el-icon-warning',
        info: 'el-icon-info',
        error: 'el-icon-error'
      }
      return iconMap[this.type] || 'el-icon-info'
    }
  },
  mounted() {
    this.show()
  },
  beforeUnmount() {
    this.clearTimer()
  },
  methods: {
    show() {
      this.visible = true
      this.startTimer()
    },
    close() {
      this.visible = false
      if (typeof this.onClose === 'function') {
        this.onClose(this.id)
      }
    },
    startTimer() {
      if (this.duration > 0) {
        this.timer = setTimeout(() => {
          this.close()
        }, this.duration)
      }
    },
    clearTimer() {
      if (this.timer) {
        clearTimeout(this.timer)
        this.timer = null
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.message {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  min-width: 300px;
  max-width: 500px;
  padding: 12px 16px;
  box-sizing: border-box;
  border-radius: 4px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  z-index: 9999;
  
  &__content {
    display: flex;
    align-items: center;
  }
  
  &__icon {
    margin-right: 10px;
    font-size: 18px;
  }
  
  &__text {
    font-size: 14px;
    line-height: 1.5;
  }
  
  &__close {
    margin-left: 10px;
    font-size: 16px;
    cursor: pointer;
    color: #C0C4CC;
    transition: color 0.3s;
    
    &:hover {
      color: #909399;
    }
  }
  
  &--success {
    background-color: #f0f9eb;
    border: 1px solid #e1f3d8;
    
    .message__icon {
      color: #67C23A;
    }
    
    .message__text {
      color: #67C23A;
    }
  }
  
  &--warning {
    background-color: #fdf6ec;
    border: 1px solid #faecd8;
    
    .message__icon {
      color: #E6A23C;
    }
    
    .message__text {
      color: #E6A23C;
    }
  }
  
  &--info {
    background-color: #f4f4f5;
    border: 1px solid #ebeef5;
    
    .message__icon {
      color: #909399;
    }
    
    .message__text {
      color: #909399;
    }
  }
  
  &--error {
    background-color: #fef0f0;
    border: 1px solid #fde2e2;
    
    .message__icon {
      color: #F56C6C;
    }
    
    .message__text {
      color: #F56C6C;
    }
  }
}

// 消息动画
.message-fade-enter-active,
.message-fade-leave-active {
  transition: opacity 0.3s, transform 0.3s;
}

.message-fade-enter-from,
.message-fade-leave-to {
  opacity: 0;
  transform: translate(-50%, -20px);
}
</style> 