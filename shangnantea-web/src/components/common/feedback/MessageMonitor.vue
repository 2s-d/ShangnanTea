<template>
  <div class="message-monitor" v-if="visible" :class="{ collapsed: isCollapsed }">
    <div class="monitor-header" @click="toggleCollapse">
      <h3>消息监控面板 <span v-if="errorCount > 0" class="error-badge">{{ errorCount }}</span></h3>
      <div class="monitor-controls">
        <button class="copy-btn" @click.stop="copyErrors" title="复制错误日志">复制错误</button>
        <button class="copy-all-btn" @click.stop="copyAll" title="复制全部日志">复制全部</button>
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
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'

// 全局消息记录数组
const messages = reactive([])

// 保存原始console方法
let originalConsoleError = null
let originalConsoleWarn = null

export default {
  name: 'MessageMonitor',
  setup() {
    const visible = ref(process.env.NODE_ENV === 'development')
    const isCollapsed = ref(false)

    // 错误数量（包含运行时错误）
    const errorCount = computed(() => messages.filter(m => 
      m.type === 'error' || m.type === 'console-error' || m.type === 'runtime-error'
    ).length)

    const toggleCollapse = () => {
      isCollapsed.value = !isCollapsed.value
    }

    const clearMessages = () => {
      messages.splice(0, messages.length)
    }

    // 复制错误日志（去重+按来源分组）
    const copyErrors = () => {
      const errors = messages.filter(m => 
        m.type === 'error' || m.type === 'console-error' || 
        m.type === 'warning' || m.type === 'console-warn' || 
        m.type === 'runtime-error'
      )
      if (errors.length === 0) {
        ElMessage.info('没有错误日志')
        return
      }
      // 去重：相同来源+内容只保留一条
      const seen = new Set()
      const unique = errors.filter(m => {
        const key = `${m.source}|${m.content}`
        if (seen.has(key)) return false
        seen.add(key)
        return true
      })
      // 按来源分组输出
      const grouped = {}
      unique.forEach(m => {
        if (!grouped[m.source]) grouped[m.source] = []
        grouped[m.source].push(m.content)
      })
      let text = '=== 错误日志 ===\n'
      for (const [source, contents] of Object.entries(grouped)) {
        text += `\n【${source}】\n`
        contents.forEach(c => { text += `  - ${c}\n` })
      }
      navigator.clipboard.writeText(text).then(() => {
        ElMessage.success(`已复制 ${unique.length} 条错误（去重后）`)
      }).catch(() => {
        ElMessage.error('复制失败')
      })
    }

    // 复制全部日志
    const copyAll = () => {
      if (messages.length === 0) {
        ElMessage.info('没有日志')
        return
      }
      const text = messages.map(m => `[${m.time}] [${m.type}] [${m.source}] ${m.content}`).join('\n')
      navigator.clipboard.writeText(text).then(() => {
        ElMessage.success(`已复制 ${messages.length} 条日志`)
      }).catch(() => {
        ElMessage.error('复制失败')
      })
    }

    // 从调用栈提取来源文件
    const extractSource = () => {
      try {
        const stack = new Error().stack || ''
        const lines = stack.split('\n')
        // 跳过前几行(Error, interceptConsole, console.xxx)，找到真正的调用者
        for (let i = 3; i < lines.length; i++) {
          const line = lines[i]
          // 匹配 .vue 或 .js 文件
          const match = line.match(/([a-zA-Z0-9_-]+\.(vue|js|ts))[:)]/)
          if (match && !line.includes('MessageMonitor') && !line.includes('chunk-vendors')) {
            return match[1]
          }
        }
        return 'unknown'
      } catch { return 'unknown' }
    }

    // 拦截控制台
    const interceptConsole = () => {
      originalConsoleError = console.error
      originalConsoleWarn = console.warn

      console.error = function(...args) {
        const content = args.map(a => typeof a === 'object' ? JSON.stringify(a) : String(a)).join(' ')
        const source = extractSource()
        addMessageRecord(content, 'console-error', source)
        originalConsoleError.apply(console, args)
      }

      console.warn = function(...args) {
        const content = args.map(a => typeof a === 'object' ? JSON.stringify(a) : String(a)).join(' ')
        // 尝试从 Vue warn 内容中提取组件名
        let source = extractSource()
        const vueMatch = content.match(/<(\w+Page|\w+View|\w+Component)/)
        if (vueMatch) source = vueMatch[1] + '.vue'
        addMessageRecord(content, 'console-warn', source)
        originalConsoleWarn.apply(console, args)
      }
    }

    // 恢复控制台
    const restoreConsole = () => {
      if (originalConsoleError) console.error = originalConsoleError
      if (originalConsoleWarn) console.warn = originalConsoleWarn
    }

    // 全局错误处理
    const handleGlobalError = (event) => {
      const { message, filename, lineno, colno } = event
      const file = filename ? filename.split('/').pop() : 'unknown'
      addMessageRecord(`${message} (${file}:${lineno}:${colno})`, 'runtime-error', '运行时错误')
    }

    // Promise 未捕获错误
    const handleUnhandledRejection = (event) => {
      const reason = event.reason
      let msg = ''
      let source = 'Promise错误'
      if (reason instanceof Error) {
        msg = `${reason.name}: ${reason.message}`
        // 尝试从调用栈提取 .vue 文件名
        const stack = reason.stack || ''
        const vueMatch = stack.match(/([A-Z][a-zA-Z]+Page|[A-Z][a-zA-Z]+View)\.vue/)
        if (vueMatch) {
          source = vueMatch[1] + '.vue'
        } else {
          const jsMatch = stack.match(/\/([a-zA-Z0-9_-]+\.(vue|js|ts)):\d+/)
          if (jsMatch) source = jsMatch[1]
        }
      } else {
        msg = String(reason)
      }
      addMessageRecord(msg, 'runtime-error', source)
    }

    onMounted(() => {
      if (visible.value) {
        interceptConsole()
        window.addEventListener('error', handleGlobalError)
        window.addEventListener('unhandledrejection', handleUnhandledRejection)
      }
    })

    onBeforeUnmount(() => {
      restoreConsole()
      window.removeEventListener('error', handleGlobalError)
      window.removeEventListener('unhandledrejection', handleUnhandledRejection)
    })

    return {
      visible,
      isCollapsed,
      messages,
      errorCount,
      toggleCollapse,
      clearMessages,
      copyErrors,
      copyAll
    }
  }
}

// 导出添加消息记录的函数
export function addMessageRecord(content, type, source) {
  const now = new Date()
  const time = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}:${now.getSeconds().toString().padStart(2, '0')}`
  
  messages.unshift({ content, type, source, time })
  
  if (messages.length > 100) {
    messages.pop()
  }
}

// 导出切换面板可见性的函数
export function toggleMonitorVisibility() {
  document.querySelector('.message-monitor')?.classList.toggle('hidden')
}
</script>

<style scoped>
.message-monitor {
  position: fixed;
  bottom: 0;
  right: 0;
  width: 450px;
  max-height: 400px;
  background-color: rgba(0, 0, 0, 0.85);
  color: white;
  z-index: 9999;
  border-top-left-radius: 4px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.3);
  transition: all 0.3s ease;
  font-family: 'Courier New', monospace;
}

.message-monitor.collapsed { max-height: 40px; }
.message-monitor.hidden { display: none; }

.monitor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background-color: #333;
  cursor: pointer;
  user-select: none;
}

.monitor-header h3 { margin: 0; font-size: 14px; display: flex; align-items: center; gap: 8px; }

.error-badge {
  background: #f44336;
  color: white;
  padding: 2px 6px;
  border-radius: 10px;
  font-size: 11px;
}

.monitor-controls { display: flex; gap: 6px; }

.monitor-controls button {
  background: none;
  border: none;
  color: white;
  cursor: pointer;
  font-size: 11px;
  padding: 3px 8px;
  border-radius: 3px;
}

.copy-btn { background-color: #e91e63 !important; }
.copy-all-btn { background-color: #9c27b0 !important; }
.clear-btn { background-color: #d32f2f !important; }
.toggle-btn { background-color: #0288d1 !important; }

.monitor-body { overflow-y: auto; max-height: 360px; }
.message-list { padding: 8px; }
.no-messages { padding: 16px; text-align: center; color: #aaa; }

.message-item {
  margin-bottom: 8px;
  padding: 8px;
  border-radius: 4px;
  font-size: 12px;
}

.message-item.success { background-color: rgba(76, 175, 80, 0.3); border-left: 3px solid #4caf50; }
.message-item.error, .message-item.console-error { background-color: rgba(244, 67, 54, 0.3); border-left: 3px solid #f44336; }
.message-item.runtime-error { background-color: rgba(183, 28, 28, 0.5); border-left: 3px solid #b71c1c; }
.message-item.warning, .message-item.console-warn { background-color: rgba(255, 152, 0, 0.3); border-left: 3px solid #ff9800; }
.message-item.info { background-color: rgba(33, 150, 243, 0.3); border-left: 3px solid #2196f3; }

.message-time { font-size: 10px; color: #ddd; margin-bottom: 4px; }
.message-content { margin-bottom: 4px; word-break: break-all; }
.message-source { font-size: 10px; color: #bbb; font-style: italic; }
</style>
