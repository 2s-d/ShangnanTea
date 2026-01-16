<template>
  <div class="error-monitor" v-if="visible" :class="{ collapsed: isCollapsed }">
    <div class="monitor-header" @click="toggleCollapse">
      <div class="header-left">
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
            <div class="item-source">📍 {{ msg.source }}</div>
            <div class="item-content">{{ msg.content }}</div>
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
    
    const errorCount = computed(() => messages.filter(m => m.type !== 'warn').length)
    const apiFailCount = computed(() => apiRequests.filter(r => r.status === 'error').length)

    const toggleCollapse = () => { isCollapsed.value = !isCollapsed.value }
    
    const clearCurrentTab = () => {
      if (activeTab.value === 'errors') {
        messages.splice(0, messages.length)
        recentMessages.clear() // 清空去重记录
      } else {
        apiRequests.splice(0, apiRequests.length)
        recentRequests.clear() // 清空去重记录
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

    const addMessage = (content, type, typeLabel, source, componentChain = '') => {
      const now = new Date()
      const time = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}:${now.getSeconds().toString().padStart(2, '0')}`
      
      // 改进去重：使用时间窗口去重，而不是永久去重
      const key = `${type}|${source}|${content.substring(0, 100)}`
      const lastTime = recentMessages.get(key)
      const currentTime = Date.now()
      
      // 如果在去重窗口内（1秒），则跳过
      if (lastTime && (currentTime - lastTime) < DEDUPE_WINDOW) {
        return
      }
      
      // 更新去重记录
      recentMessages.set(key, currentTime)
      
      // 清理过期的去重记录（超过10秒的）
      if (recentMessages.size > 100) {
        for (const [k, t] of recentMessages.entries()) {
          if (currentTime - t > 10000) {
            recentMessages.delete(k)
          }
        }
      }
      
      messages.unshift({ content, type, typeLabel, source, componentChain, time })
      if (messages.length > 100) messages.pop() // 增加到100条
    }

    const interceptConsole = () => {
      originalConsoleError = console.error
      originalConsoleWarn = console.warn

      console.error = function(...args) {
        let content = args.map(a => {
          if (a instanceof Error) return `${a.name}: ${a.message}`
          if (typeof a === 'object') return safeStringify(a)
          return String(a)
        }).join(' ')
        
        const stack = args.find(a => a instanceof Error)?.stack || new Error().stack
        const source = extractSource(content, stack)
        content = simplifyContent(content)
        
        addMessage(content, 'error', '错误', source)
        originalConsoleError.apply(console, args)
      }

      console.warn = function(...args) {
        let content = args.map(a => {
          if (typeof a === 'object') return safeStringify(a)
          return String(a)
        }).join(' ')
        
        if (content.includes('[Vue warn]')) {
          const { content: cleanContent, componentChain, source } = cleanVueWarn(content)
          addMessage(cleanContent, 'warn', 'Vue警告', source, componentChain)
        } else {
          const source = extractSource(content, new Error().stack)
          addMessage(simplifyContent(content), 'warn', '警告', source)
        }
        
        originalConsoleWarn.apply(console, args)
      }
    }

    const restoreConsole = () => {
      if (originalConsoleError) console.error = originalConsoleError
      if (originalConsoleWarn) console.warn = originalConsoleWarn
    }

    const handleGlobalError = event => {
      const { message, filename, lineno, colno } = event
      const file = filename ? filename.split('/').pop() : 'unknown'
      const source = `${file}:${lineno}:${colno}`
      addMessage(message, 'runtime', '运行时错误', source)
    }

    const handleUnhandledRejection = event => {
      const reason = event.reason
      let content = '', source = 'Promise'
      
      if (reason instanceof Error) {
        content = `${reason.name}: ${reason.message}`
        source = extractSource(content, reason.stack)
      } else {
        content = typeof reason === 'object' ? safeStringify(reason) : String(reason)
      }
      
      addMessage(simplifyContent(content), 'promise', 'Promise错误', source)
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
      
      const grouped = {}
      messages.forEach(m => {
        if (!grouped[m.source]) grouped[m.source] = []
        const exists = grouped[m.source].some(e => e.content === m.content)
        if (!exists) {
          grouped[m.source].push({ type: m.typeLabel, content: m.content, chain: m.componentChain })
        }
      })
      
      let text = '=== 错误日志 ===\n'
      for (const [source, items] of Object.entries(grouped)) {
        text += `\n【${source}】\n`
        items.forEach(item => {
          text += `[${item.type}] ${item.content}\n`
          if (item.chain) text += `  组件链: ${item.chain}\n`
        })
      }
      
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
      toggleCollapse, clearCurrentTab, copyCurrentTab, checkBackendStatus
    }
  }
}
</script>

<style scoped>
.error-monitor {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #1e1e1e;
  color: #d4d4d4;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 12px;
  z-index: 9999;
  max-height: 40vh;
  display: flex;
  flex-direction: column;
  border-top: 2px solid #007acc;
}

.error-monitor.collapsed {
  max-height: auto;
}

.monitor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 12px;
  background: #252526;
  cursor: pointer;
  user-select: none;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
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

.item-source {
  color: #4fc3f7;
  font-size: 11px;
  margin-bottom: 4px;
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
