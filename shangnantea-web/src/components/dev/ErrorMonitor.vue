<template>
  <div 
    class="error-monitor" 
    v-if="visible" 
    :class="{ collapsed: isCollapsed }"
    :style="monitorStyle"
    @mousedown="startDrag"
  >
    <div class="monitor-header" @click="toggleCollapse">
      <div class="header-left">
        <span class="drag-handle" @mousedown.stop="startDrag">⋮⋮</span>
        <h3>开发监控</h3>
        <div class="tab-buttons" v-if="!isCollapsed" @click.stop>
          <button :class="{ active: activeTab === 'errors' }" @click="activeTab = 'errors'">
            错误 <span v-if="errorCount > 0" class="badge error">{{ errorCount }}</span>
          </button>
          <button :class="{ active: activeTab === 'api' }" @click="activeTab = 'api'">
            API <span v-if="apiFailCount > 0" class="badge error">{{ apiFailCount }}</span>
          </button>
        </div>
      </div>
      <div class="monitor-controls">
        <button class="copy-btn" @click.stop="copyCurrentTab">复制</button>
        <button class="clear-btn" @click.stop="clearCurrentTab">清空</button>
        <button class="toggle-btn">{{ isCollapsed ? '▲' : '▼' }}</button>
      </div>
    </div>
    
    <div class="monitor-body" v-if="!isCollapsed">
      <!-- 错误日志 Tab -->
      <div v-if="activeTab === 'errors'" class="tab-content">
        <div v-if="messages.length === 0" class="no-data">暂无错误记录</div>
        <div v-else class="message-list">
          <div v-for="(msg, index) in messages" :key="index" class="message-item" :class="msg.type">
            <div class="item-header">
              <span class="item-time">{{ msg.time }}</span>
              <span class="item-tag">{{ msg.typeLabel }}</span>
            </div>
            <div class="item-content">{{ msg.content }}</div>
            <div v-if="msg.file" class="item-file">📄 {{ msg.file }}</div>
            <div v-if="msg.stack" class="item-stack">
              <details>
                <summary>堆栈信息</summary>
                <pre>{{ msg.stack }}</pre>
              </details>
            </div>
            <div v-if="msg.componentChain" class="item-chain">🔗 {{ msg.componentChain }}</div>
          </div>
        </div>
      </div>
      
      <!-- API 请求 Tab -->
      <div v-if="activeTab === 'api'" class="tab-content">
        <!-- 后端连接状态 -->
        <div class="backend-status" :class="backendStatus">
          <span class="status-dot"></span>
          <span>{{ backendStatusText }}</span>
          <button class="check-btn" @click="checkBackendStatus">检测</button>
        </div>
        
        <div v-if="apiRequests.length === 0" class="no-data">暂无 API 请求</div>
        <div v-else class="message-list">
          <div v-for="(req, index) in apiRequests" :key="index" class="api-item" :class="req.status">
            <div class="item-header">
              <span class="item-time">{{ req.time }}</span>
              <span class="item-tag" :class="req.status">{{ req.statusCode || req.status }}</span>
              <span class="item-method">{{ req.method }}</span>
            </div>
            <div class="item-url">{{ req.url }}</div>
            <div v-if="req.error" class="item-error">❌ {{ req.error }}</div>
            <div v-if="req.duration" class="item-duration">⏱ {{ req.duration }}ms</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'

const messages = reactive([])
const apiRequests = reactive([])
const seenRequests = new Set() // 用于去重
let originalConsoleError = null
let originalConsoleWarn = null
let originalConsoleLog = null // 新增：捕获 console.log
let originalFetch = null
let originalXhrOpen = null
let originalXhrSend = null
let performanceObserver = null

export default {
  name: 'ErrorMonitor',
  setup() {
    const visible = ref(process.env.NODE_ENV === 'development')
    const isCollapsed = ref(false)
    const activeTab = ref('errors')
    const backendStatus = ref('checking') // checking, connected, disconnected
    const backendStatusText = ref('检测中...')
    
    // 拖动相关状态
    const position = reactive({ x: 20, y: window.innerHeight - 400 }) // 默认左下角
    const isDragging = ref(false)
    const dragStart = reactive({ x: 0, y: 0 })
    
    const monitorStyle = computed(() => ({
      left: `${position.x}px`,
      top: `${position.y}px`,
      maxWidth: '800px',
      maxHeight: '60vh'
    }))
    
    const errorCount = computed(() => messages.filter(m => m.type !== 'warn').length)
    const apiFailCount = computed(() => apiRequests.filter(r => r.status === 'error').length)

    const toggleCollapse = () => { isCollapsed.value = !isCollapsed.value }
    
    // 拖动功能
    const startDrag = (e) => {
      isDragging.value = true
      dragStart.x = e.clientX - position.x
      dragStart.y = e.clientY - position.y
      
      const onMouseMove = (e) => {
        if (!isDragging.value) return
        position.x = Math.max(0, Math.min(window.innerWidth - 300, e.clientX - dragStart.x))
        position.y = Math.max(0, Math.min(window.innerHeight - 100, e.clientY - dragStart.y))
      }
      
      const onMouseUp = () => {
        isDragging.value = false
        document.removeEventListener('mousemove', onMouseMove)
        document.removeEventListener('mouseup', onMouseUp)
      }
      
      document.addEventListener('mousemove', onMouseMove)
      document.addEventListener('mouseup', onMouseUp)
    }
    
    const clearCurrentTab = () => {
      if (activeTab.value === 'errors') {
        messages.splice(0, messages.length)
      } else {
        apiRequests.splice(0, apiRequests.length)
        seenRequests.clear() // 清空去重集合
      }
    }
    
    // 检测后端连接状态（使用 XMLHttpRequest 避免和 fetch 拦截冲突）
    const checkBackendStatus = () => {
      backendStatus.value = 'checking'
      backendStatusText.value = '检测中...'
      
      const xhr = new (originalXhrOpen ? XMLHttpRequest : window.XMLHttpRequest)()
      const timeoutId = setTimeout(() => {
        xhr.abort()
        backendStatus.value = 'disconnected'
        backendStatusText.value = '后端连接超时'
      }, 5000)
      
      xhr.onreadystatechange = function() {
        if (xhr.readyState === 4) {
          clearTimeout(timeoutId)
          
          if (xhr.status === 200 || xhr.status === 401) {
            // 200 或 401 都说明后端在运行
            backendStatus.value = 'connected'
            backendStatusText.value = '后端已连接'
          } else if (xhr.status >= 500) {
            backendStatus.value = 'error'
            backendStatusText.value = `后端错误 (${xhr.status})`
          } else if (xhr.status === 0) {
            backendStatus.value = 'disconnected'
            backendStatusText.value = '⚠️ 后端未连接 - 请启动后端服务'
          } else {
            backendStatus.value = 'connected'
            backendStatusText.value = '后端已连接'
          }
        }
      }
      
      xhr.onerror = function() {
        clearTimeout(timeoutId)
        backendStatus.value = 'disconnected'
        backendStatusText.value = '⚠️ 后端未连接 - 请启动后端服务'
      }
      
      try {
        // 使用环境变量中的 baseURL，如果没有则使用默认的 /api
        const baseURL = process.env.VUE_APP_API_BASE_URL || '/api'
        // 构建完整的请求 URL
        const apiUrl = baseURL.startsWith('http') 
          ? `${baseURL}/user/me`  // 云端 Mock 地址
          : `${baseURL}/user/me`  // 本地代理地址
        xhr.open('GET', apiUrl, true)
        xhr.send()
      } catch (e) {
        clearTimeout(timeoutId)
        backendStatus.value = 'disconnected'
        backendStatusText.value = '⚠️ 后端未连接 - 请启动后端服务'
      }
    }

    // ========== 错误监控部分 ==========
    const cleanVueWarn = content => {
      const warnMatch = content.match(/\[Vue warn\]:\s*([^.]+)/)
      const coreMessage = warnMatch ? warnMatch[1].trim() : ''
      
      const componentChain = []
      const atMatches = content.matchAll(/at\s+<([A-Z][a-zA-Z]+)/g)
      for (const match of atMatches) {
        if (!componentChain.includes(match[1])) componentChain.push(match[1])
      }
      
      const fileMatch = content.match(/"__file":"([^"]+)"/)
      const sourceFile = fileMatch ? fileMatch[1].split('/').pop() : null
      
      return {
        content: coreMessage || content.substring(0, 200),
        componentChain: componentChain.length > 0 ? componentChain.join(' → ') : '',
        source: sourceFile || (componentChain[0] ? componentChain[0] + '.vue' : 'Vue')
      }
    }

    const extractSource = (content, stack) => {
      const webpackMatch = content.match(/_([a-zA-Z]+)__WEBPACK_IMPORTED_MODULE/)
      if (webpackMatch) return webpackMatch[1] + '.js'
      
      if (stack) {
        const lines = stack.split('\n')
        for (const line of lines) {
          if (/chunk-vendors|webpack|node_modules|ErrorMonitor/.test(line)) continue
          const match = line.match(/([a-zA-Z0-9_-]+\.(vue|js|ts)):(\d+)/)
          if (match) return `${match[1]}:${match[3]}`
        }
      }
      
      const fileMatch = content.match(/([A-Z][a-zA-Z]+(?:Page|View|Component)?\.vue)/)
      if (fileMatch) return fileMatch[1]
      
      return 'unknown'
    }

    const simplifyContent = content => {
      let simplified = content.replace(/_[a-zA-Z]+__WEBPACK_IMPORTED_MODULE_\d+__\./g, '')
      simplified = simplified.replace(/\.default\./g, '.')
      if (simplified.length > 300) simplified = simplified.substring(0, 300) + '...'
      return simplified
    }

    // 安全的 JSON 序列化，处理循环引用
    const safeStringify = obj => {
      if (obj === null || obj === undefined) return String(obj)
      if (typeof obj !== 'object') return String(obj)
      
      // 跳过 Window、Document 等大型对象
      if (obj === window || obj === document) return '[Window/Document]'
      if (obj instanceof HTMLElement) return `[HTMLElement: ${obj.tagName}]`
      if (obj instanceof Event) return `[Event: ${obj.type}]`
      
      const seen = new WeakSet()
      try {
        return JSON.stringify(obj, (key, value) => {
          if (typeof value === 'object' && value !== null) {
            if (seen.has(value)) return '[Circular]'
            if (value === window || value === document) return '[Window/Document]'
            if (value instanceof HTMLElement) return '[HTMLElement]'
            if (value instanceof Event) return '[Event]'
            seen.add(value)
          }
          return value
        })
      } catch {
        return String(obj)
      }
    }

    // ========== 错误监控部分 ==========
    
    // 提取详细的文件信息和堆栈
    const extractDetailedError = (error, stack) => {
      let file = ''
      let line = ''
      let fullStack = ''
      
      if (stack) {
        fullStack = stack
        // 从堆栈中提取第一个有用的文件位置（跳过 node_modules 和内部文件）
        const lines = stack.split('\n')
        for (const stackLine of lines) {
          // 跳过无用的行
          if (/node_modules|webpack|ErrorMonitor/.test(stackLine)) continue
          
          // 匹配文件路径和行号
          const match = stackLine.match(/(?:at\s+)?(?:.*?\s+)?\(?([^)]+):(\d+):(\d+)\)?/)
          if (match) {
            const fullPath = match[1]
            const lineNum = match[2]
            const colNum = match[3]
            
            // 简化路径，只保留有用部分
            const simplePath = fullPath.replace(/^.*?\/src\//, 'src/')
                                       .replace(/^.*?\/node_modules\//, 'node_modules/')
                                       .replace(/^webpack-internal:\/\/\//, '')
            
            file = `${simplePath}:${lineNum}:${colNum}`
            break
          }
        }
      }
      
      return { file, stack: fullStack }
    }
    
    const addMessage = (content, type, typeLabel, rawStack = '', componentChain = '') => {
      const now = new Date()
      const time = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}:${now.getSeconds().toString().padStart(2, '0')}`
      
      // 提取详细错误信息
      const { file, stack } = extractDetailedError(content, rawStack)
      
      // 去重检查：使用 type + content 的前100个字符作为key（保持原有逻辑）
      const key = `${type}|${content.substring(0, 100)}`
      if (messages.some(m => `${m.type}|${m.content.substring(0, 100)}` === key)) return
      
      messages.unshift({ 
        content, 
        type, 
        typeLabel, 
        file: file || 'unknown',
        stack: stack || '',
        componentChain, 
        time 
      })
      if (messages.length > 50) messages.pop()
    }

    const interceptConsole = () => {
      originalConsoleError = console.error
      originalConsoleWarn = console.warn
      originalConsoleLog = console.log

      console.error = function(...args) {
        let content = args.map(a => {
          if (a instanceof Error) return `${a.name}: ${a.message}`
          if (typeof a === 'object') return safeStringify(a)
          return String(a)
        }).join(' ')
        
        const errorObj = args.find(a => a instanceof Error)
        const stack = errorObj?.stack || new Error().stack
        
        addMessage(content, 'error', '错误', stack)
        originalConsoleError.apply(console, args)
      }

      console.warn = function(...args) {
        let content = args.map(a => {
          if (typeof a === 'object') return safeStringify(a)
          return String(a)
        }).join(' ')
        
        const stack = new Error().stack
        
        if (content.includes('[Vue warn]')) {
          const { content: cleanContent, componentChain } = cleanVueWarn(content)
          addMessage(cleanContent, 'warn', 'Vue警告', stack, componentChain)
        } else {
          addMessage(content, 'warn', '警告', stack)
        }
        
        originalConsoleWarn.apply(console, args)
      }
      
      // 新增：捕获 console.log 中的错误对象
      console.log = function(...args) {
        // 检查是否有 Error 对象
        const errorObj = args.find(a => a instanceof Error)
        if (errorObj) {
          const content = `${errorObj.name}: ${errorObj.message}`
          addMessage(content, 'log', '日志错误', errorObj.stack)
        }
        
        originalConsoleLog.apply(console, args)
      }
    }

    const restoreConsole = () => {
      if (originalConsoleError) console.error = originalConsoleError
      if (originalConsoleWarn) console.warn = originalConsoleWarn
      if (originalConsoleLog) console.log = originalConsoleLog
    }

    const handleGlobalError = event => {
      const { message, filename, lineno, colno, error } = event
      const file = filename ? `${filename.split('/').pop()}:${lineno}:${colno}` : 'unknown'
      const stack = error?.stack || ''
      addMessage(message, 'runtime', '运行时错误', stack)
    }

    const handleUnhandledRejection = event => {
      const reason = event.reason
      let content = '', stack = ''
      
      if (reason instanceof Error) {
        content = `${reason.name}: ${reason.message}`
        stack = reason.stack || ''
      } else {
        content = typeof reason === 'object' ? safeStringify(reason) : String(reason)
      }
      
      addMessage(content, 'promise', 'Promise错误', stack)
    }

    // ========== API 请求监控部分 ==========
    // 使用 fetch/xhr 拦截来捕获所有网络请求
    
    // 判断是否是 API 请求（过滤静态资源）
    const isApiRequest = url => {
      // 过滤静态资源
      if (/\.(js|css|png|jpg|jpeg|gif|svg|ico|woff|woff2|ttf|eot|map)(\?|$)/i.test(url)) return false
      // 过滤热更新
      if (url.includes('hot-update') || url.includes('sockjs-node') || url.includes('__vite')) return false
      // 过滤 WebSocket
      if (url.startsWith('ws://') || url.startsWith('wss://')) return false
      return true
    }
    
    const addApiRequest = (url, method, statusCode, duration, error = '') => {
      // 过滤非 API 请求
      if (!isApiRequest(url)) return
      
      const now = new Date()
      const time = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}:${now.getSeconds().toString().padStart(2, '0')}`
      
      // 简化 URL，只保留路径部分
      let simplifiedUrl = url.replace(/^https?:\/\/[^/]+/, '')
      // 保留查询参数（可能有用）
      
      // 去重：同一个 方法+URL+状态码 只记录一次
      const key = `${method}|${simplifiedUrl}|${statusCode}`
      if (seenRequests.has(key)) return
      seenRequests.add(key)
      
      // 判断是否失败：4xx, 5xx, 0（网络错误）, 或其他异常状态码（非2xx/3xx）
      const isError = statusCode === 0 || statusCode >= 400 || (statusCode > 0 && statusCode < 200)
      
      const req = {
        time,
        method: method.toUpperCase(),
        url: simplifiedUrl,
        status: isError ? 'error' : 'success',
        statusCode: statusCode || '网络错误',
        error: error,
        duration: duration ? Math.round(duration) : null
      }
      
      apiRequests.unshift(req)
      if (apiRequests.length > 100) apiRequests.pop()
    }

    // 不需要 PerformanceObserver，fetch/xhr 拦截已经足够
    
    // 使用 PerformanceObserver 监控静态资源加载失败（图片404等）
    const startPerformanceObserver = () => {
      if (typeof PerformanceObserver === 'undefined') return
      
      performanceObserver = new PerformanceObserver(list => {
        for (const entry of list.getEntries()) {
          const url = entry.name
          // 过滤热更新等
          if (url.includes('hot-update') || url.includes('sockjs-node') || url.includes('__vite')) continue
          
          // 获取状态码（如果支持）
          const status = entry.responseStatus || 0
          
          // 只记录失败的请求（4xx, 5xx, 或异常状态码）
          if (status >= 400 || (status > 0 && status < 200)) {
            // 简化 URL
            let simplifiedUrl = url.replace(/^https?:\/\/[^/]+/, '')
            
            // 去重
            const key = `GET|${simplifiedUrl}|${status}`
            if (seenRequests.has(key)) continue
            seenRequests.add(key)
            
            const now = new Date()
            const time = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}:${now.getSeconds().toString().padStart(2, '0')}`
            
            // 判断资源类型
            let resourceType = '资源'
            if (/\.(png|jpg|jpeg|gif|svg|ico|webp)(\?|$)/i.test(url)) resourceType = '图片'
            else if (/\.(js)(\?|$)/i.test(url)) resourceType = '脚本'
            else if (/\.(css)(\?|$)/i.test(url)) resourceType = '样式'
            
            apiRequests.unshift({
              time,
              method: 'GET',
              url: simplifiedUrl,
              status: 'error',
              statusCode: status,
              error: `${resourceType}加载失败`,
              duration: entry.duration ? Math.round(entry.duration) : null
            })
            
            if (apiRequests.length > 100) apiRequests.pop()
          }
        }
      })
      
      try {
        performanceObserver.observe({ entryTypes: ['resource'] })
      } catch (e) {
        // 忽略不支持的情况
      }
    }

    // 拦截 fetch 请求
    const interceptFetch = () => {
      originalFetch = window.fetch
      window.fetch = async function(input, init = {}) {
        const url = typeof input === 'string' ? input : input.url
        const method = init.method || 'GET'
        const startTime = Date.now()
        
        try {
          const response = await originalFetch.apply(this, arguments)
          const duration = Date.now() - startTime
          
          // 克隆响应以便读取状态
          addApiRequest(url, method, response.status, duration, response.ok ? '' : response.statusText)
          
          return response
        } catch (error) {
          const duration = Date.now() - startTime
          addApiRequest(url, method, 0, duration, error.message || '网络错误')
          throw error
        }
      }
    }

    // 拦截 XMLHttpRequest
    const interceptXhr = () => {
      originalXhrOpen = XMLHttpRequest.prototype.open
      originalXhrSend = XMLHttpRequest.prototype.send
      
      XMLHttpRequest.prototype.open = function(method, url) {
        this._monitorMethod = method
        this._monitorUrl = url
        this._monitorStartTime = null
        return originalXhrOpen.apply(this, arguments)
      }
      
      XMLHttpRequest.prototype.send = function() {
        this._monitorStartTime = Date.now()
        
        this.addEventListener('loadend', function() {
          const duration = this._monitorStartTime ? Date.now() - this._monitorStartTime : null
          const url = this._monitorUrl || ''
          const method = this._monitorMethod || 'GET'
          
          // 过滤掉热更新等请求
          if (url.includes('hot-update') || url.includes('sockjs-node')) return
          
          addApiRequest(url, method, this.status, duration, this.status >= 400 ? this.statusText : '')
        })
        
        return originalXhrSend.apply(this, arguments)
      }
    }

    const restoreNetworkInterceptors = () => {
      if (performanceObserver) {
        performanceObserver.disconnect()
      }
      if (originalFetch) {
        window.fetch = originalFetch
      }
      if (originalXhrOpen) {
        XMLHttpRequest.prototype.open = originalXhrOpen
      }
      if (originalXhrSend) {
        XMLHttpRequest.prototype.send = originalXhrSend
      }
    }

    // ========== 复制功能 ==========
    const copyCurrentTab = () => {
      if (activeTab.value === 'errors') {
        copyErrors()
      } else {
        copyApiRequests()
      }
    }

    const copyErrors = () => {
      if (messages.length === 0) {
        ElMessage.info('没有错误日志')
        return
      }
      
      let text = '=== 错误日志 ===\n\n'
      messages.forEach((m, index) => {
        text += `[${index + 1}] ${m.typeLabel} - ${m.time}\n`
        text += `文件: ${m.file}\n`
        text += `内容: ${m.content}\n`
        if (m.componentChain) {
          text += `组件链: ${m.componentChain}\n`
        }
        if (m.stack) {
          text += `堆栈:\n${m.stack}\n`
        }
        text += '\n---\n\n'
      })
      
      navigator.clipboard.writeText(text).then(() => {
        ElMessage.success(`已复制 ${messages.length} 条错误日志`)
      }).catch(() => ElMessage.error('复制失败'))
    }

    const copyApiRequests = () => {
      // 只复制失败的请求
      const failed = apiRequests.filter(r => r.status === 'error')
      
      if (failed.length === 0) {
        ElMessage.info('没有失败的 API 请求')
        return
      }
      
      let text = '=== 失败的 API 请求 ===\n\n'
      failed.forEach(r => {
        text += `[${r.statusCode}] ${r.method} ${r.url}\n`
        if (r.error) text += `  错误: ${r.error}\n`
        if (r.duration) text += `  耗时: ${r.duration}ms\n`
      })
      
      navigator.clipboard.writeText(text).then(() => {
        ElMessage.success(`已复制 ${failed.length} 条失败请求`)
      }).catch(() => ElMessage.error('复制失败'))
    }

    onMounted(() => {
      if (visible.value) {
        interceptConsole()
        // 启动网络请求监控
        startPerformanceObserver()
        interceptFetch()
        interceptXhr()
        window.addEventListener('error', handleGlobalError)
        window.addEventListener('unhandledrejection', handleUnhandledRejection)
        // 启动时检测后端状态
        checkBackendStatus()
      }
    })

    onBeforeUnmount(() => {
      restoreConsole()
      restoreNetworkInterceptors()
      window.removeEventListener('error', handleGlobalError)
      window.removeEventListener('unhandledrejection', handleUnhandledRejection)
    })

    return {
      visible, isCollapsed, activeTab, messages, apiRequests,
      errorCount, apiFailCount, backendStatus, backendStatusText,
      monitorStyle, startDrag,
      toggleCollapse, clearCurrentTab, copyCurrentTab, checkBackendStatus
    }
  }
}
</script>

<style scoped>
.error-monitor {
  position: fixed;
  background: #1e1e1e;
  color: #d4d4d4;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 12px;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  border: 2px solid #007acc;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.5);
  min-width: 400px;
  resize: both;
  overflow: hidden;
}

.error-monitor.collapsed {
  max-height: auto;
  resize: none;
}

.monitor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 12px;
  background: #252526;
  cursor: pointer;
  user-select: none;
  border-bottom: 1px solid #007acc;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.drag-handle {
  cursor: move;
  color: #666;
  font-size: 16px;
  padding: 0 4px;
}

.drag-handle:hover {
  color: #007acc;
}

.monitor-header h3 {
  margin: 0;
  font-size: 13px;
  color: #fff;
}

.tab-buttons {
  display: flex;
  gap: 4px;
}

.tab-buttons button {
  padding: 3px 10px;
  background: #3c3c3c;
  border: none;
  color: #ccc;
  cursor: pointer;
  border-radius: 3px;
  font-size: 11px;
}

.tab-buttons button:hover {
  background: #4c4c4c;
}

.tab-buttons button.active {
  background: #007acc;
  color: #fff;
}

.badge {
  display: inline-block;
  padding: 1px 5px;
  border-radius: 8px;
  font-size: 10px;
  margin-left: 4px;
}

.badge.error {
  background: #f44336;
  color: #fff;
}

.monitor-controls {
  display: flex;
  gap: 8px;
}

.monitor-controls button {
  padding: 3px 8px;
  background: #3c3c3c;
  border: none;
  color: #ccc;
  cursor: pointer;
  border-radius: 3px;
  font-size: 11px;
}

.monitor-controls button:hover {
  background: #4c4c4c;
}

.copy-btn:hover { background: #007acc; }
.clear-btn:hover { background: #f44336; }

.monitor-body {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
  max-height: calc(60vh - 40px);
}

.tab-content {
  height: 100%;
}

.no-data {
  text-align: center;
  color: #666;
  padding: 20px;
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.message-item, .api-item {
  padding: 8px;
  border-radius: 4px;
  background: #2d2d2d;
  border-left: 3px solid #666;
}

.message-item.error { border-left-color: #f44336; }
.message-item.warn { border-left-color: #ff9800; }
.message-item.runtime { border-left-color: #e91e63; }
.message-item.promise { border-left-color: #9c27b0; }
.message-item.log { border-left-color: #2196f3; }

.api-item.success { border-left-color: #4caf50; }
.api-item.error { border-left-color: #f44336; }

.item-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.item-time {
  color: #888;
  font-size: 10px;
}

.item-tag {
  padding: 1px 6px;
  border-radius: 3px;
  font-size: 10px;
  background: #444;
}

.item-tag.success { background: #4caf50; color: #fff; }
.item-tag.error { background: #f44336; color: #fff; }

.item-method {
  font-weight: bold;
  color: #4fc3f7;
}

.item-file {
  color: #4fc3f7;
  font-size: 11px;
  margin-bottom: 4px;
  font-family: monospace;
}

.item-url {
  color: #81c784;
  font-size: 11px;
  word-break: break-all;
}

.item-content {
  color: #e0e0e0;
  word-break: break-word;
  line-height: 1.4;
  margin-bottom: 4px;
}

.item-stack {
  margin-top: 6px;
}

.item-stack details {
  cursor: pointer;
}

.item-stack summary {
  color: #ce93d8;
  font-size: 11px;
  padding: 2px 0;
}

.item-stack summary:hover {
  color: #e1bee7;
}

.item-stack pre {
  margin: 4px 0 0 0;
  padding: 6px;
  background: #1a1a1a;
  border-radius: 3px;
  font-size: 10px;
  color: #aaa;
  overflow-x: auto;
  max-height: 200px;
  line-height: 1.3;
}

.item-chain {
  color: #ce93d8;
  font-size: 11px;
  margin-top: 4px;
}

.item-error {
  color: #ef5350;
  font-size: 11px;
  margin-top: 4px;
}

.item-duration {
  color: #888;
  font-size: 10px;
  margin-top: 4px;
}

/* 后端状态样式 */
.backend-status {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 10px;
  margin-bottom: 8px;
  border-radius: 4px;
  font-size: 12px;
  background: #2d2d2d;
}

.backend-status.checking {
  color: #ffc107;
}

.backend-status.connected {
  color: #4caf50;
}

.backend-status.disconnected {
  color: #f44336;
  background: #3d2020;
}

.backend-status.error {
  color: #ff9800;
  background: #3d3020;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: currentColor;
}

.backend-status.checking .status-dot {
  animation: pulse 1s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}

.check-btn {
  margin-left: auto;
  padding: 2px 8px;
  background: #3c3c3c;
  border: none;
  color: #ccc;
  cursor: pointer;
  border-radius: 3px;
  font-size: 10px;
}

.check-btn:hover {
  background: #4c4c4c;
}
</style>
