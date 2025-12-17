<template>
  <div class="message-monitor" v-if="visible" :class="{ collapsed: isCollapsed }">
    <div class="monitor-header" @click="toggleCollapse">
      <h3>消息监控面板</h3>
      <div class="monitor-controls">
        <button class="clear-btn" @click.stop="clearMessages">清空</button>
        <button class="toggle-btn">{{ isCollapsed ? '展开' : '折叠' }}</button>
      </div>
    </div>
    <div class="monitor-body" v-if="!isCollapsed">
      <div v-if="messages.length === 0" class="no-messages">暂无消息记录</div>
      <div v-else class="message-list">
        <div 
          v-for="(msg, index) in messages" 
          :key="index" 
          class="message-item"
          :class="msg.type"
        >
          <div class="message-time">{{ msg.time }}</div>
          <div class="message-content">{{ msg.content }}</div>
          <div class="message-source">{{ msg.source }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'

// 全局消息记录数组，使其可在messageManager中访问
const messages = reactive([])

export default {
  name: 'MessageMonitor',
  setup() {
    const visible = ref(process.env.NODE_ENV === 'development')
    const isCollapsed = ref(false)

    const toggleCollapse = () => {
      isCollapsed.value = !isCollapsed.value
    }

    const clearMessages = () => {
      messages.splice(0, messages.length)
    }

    return {
      visible,
      isCollapsed,
      messages,
      toggleCollapse,
      clearMessages
    }
  }
}

// 导出添加消息记录的函数，供messageManager使用
export function addMessageRecord(content, type, source) {
  // 格式化时间
  const now = new Date()
  const time = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}:${now.getSeconds().toString().padStart(2, '0')}`
  
  // 添加到记录中
  messages.unshift({
    content,
    type,
    source,
    time
  })
  
  // 限制记录数量，最多保留100条
  if (messages.length > 100) {
    messages.pop()
  }
}

// 导出切换面板可见性的函数
export function toggleMonitorVisibility() {
  // 因为setup函数作用域内的visible不能直接导出
  // 这是一个简化实现，实际项目中可能需要使用provide/inject或Vuex
  document.querySelector('.message-monitor')?.classList.toggle('hidden')
}
</script>

<style scoped>
.message-monitor {
  position: fixed;
  bottom: 0;
  right: 0;
  width: 400px;
  max-height: 400px;
  background-color: rgba(0, 0, 0, 0.8);
  color: white;
  z-index: 9999;
  border-top-left-radius: 4px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.3);
  transition: all 0.3s ease;
  font-family: 'Courier New', monospace;
}

.message-monitor.collapsed {
  max-height: 40px;
}

.message-monitor.hidden {
  display: none;
}

.monitor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background-color: #333;
  cursor: pointer;
  user-select: none;
}

.monitor-header h3 {
  margin: 0;
  font-size: 14px;
}

.monitor-controls {
  display: flex;
  gap: 8px;
}

.monitor-controls button {
  background: none;
  border: none;
  color: white;
  cursor: pointer;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 2px;
}

.clear-btn {
  background-color: #d32f2f;
}

.toggle-btn {
  background-color: #0288d1;
}

.monitor-body {
  overflow-y: auto;
  max-height: 360px;
}

.message-list {
  padding: 8px;
}

.no-messages {
  padding: 16px;
  text-align: center;
  color: #aaa;
}

.message-item {
  margin-bottom: 8px;
  padding: 8px;
  border-radius: 4px;
  font-size: 12px;
}

.message-item.success {
  background-color: rgba(76, 175, 80, 0.3);
  border-left: 3px solid #4caf50;
}

.message-item.error {
  background-color: rgba(244, 67, 54, 0.3);
  border-left: 3px solid #f44336;
}

.message-item.warning {
  background-color: rgba(255, 152, 0, 0.3);
  border-left: 3px solid #ff9800;
}

.message-item.info {
  background-color: rgba(33, 150, 243, 0.3);
  border-left: 3px solid #2196f3;
}

.message-time {
  font-size: 10px;
  color: #ddd;
  margin-bottom: 4px;
}

.message-content {
  margin-bottom: 4px;
}

.message-source {
  font-size: 10px;
  color: #bbb;
  font-style: italic;
}
</style> 