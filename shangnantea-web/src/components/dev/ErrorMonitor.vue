<template>
  <div 
    class="error-monitor" 
    v-if="visible" 
    :class="{ collapsed: isCollapsed }"
    :style="monitorStyle"
    @mousedown="startDrag"
    ref="monitorRef"
  >
    <div class="monitor-header">
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
        <button class="toggle-btn" @click.stop="toggleCollapse">{{ isCollapsed ? '展开' : '折叠' }}</button>
      </div>
    </div>
    <!-- 自定义窗口缩放句柄：右边、下边、右下角 -->
    <div v-if="!isCollapsed" class="resize-handle right" @mousedown.stop="startResize($event, 'right')"></div>
    <div v-if="!isCollapsed" class="resize-handle bottom" @mousedown.stop="startResize($event, 'bottom')"></div>
    <div v-if="!isCollapsed" class="resize-handle corner" @mousedown.stop="startResize($event, 'corner')"></div>
    
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

<script setup>
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


    const visible = ref(process.env.NODE_ENV === 'development')
    const isCollapsed = ref(true)
    const activeTab = ref('errors')
    const backendStatus = ref('checking') // checking, connected, disconnected
    const backendStatusText = ref('检测中...')
    
    // 拖动和缩放相关状态
    // 初始位置整体向下移动 200 像素（在原来基础上 +200）
    const position = reactive({ x: 20, y: window.innerHeight - 200 }) // 默认左下角，向下偏移
    const isDragging = ref(false)
    const dragStart = reactive({ x: 0, y: 0 })
    const size = reactive({ width: 0, height: 0 })
    const monitorRef = ref(null)
    const isResizing = ref(false)
    const resizeDir = ref(null) // right | bottom | corner
    const resizeStart = reactive({ x: 0, y: 0, width: 0, height: 0 })
    
    // 保存当前的事件监听器引用，确保能正确清理
    let currentDragHandlers = { onMouseMove: null, onMouseUp: null }
    let currentResizeHandlers = { onMouseMove: null, onMouseUp: null }
    
    const monitorStyle = computed(() => ({
      left: `${position.x}px`,
      top: `${position.y}px`,
      width: `${isCollapsed.value ? 260 : (size.width || 800)}px`,
      height: isCollapsed.value ? '48px' : `${size.height || 360}px`,
      maxWidth: '80vw',
      maxHeight: '80vh'
    }))
    
    const errorCount = computed(() => messages.filter(m => m.type !== 'warn').length)
    const apiFailCount = computed(() => apiRequests.filter(r => r.status === 'error').length)

    const toggleCollapse = () => {
      isCollapsed.value = !isCollapsed.value
    }
    
    // 清理旧的拖动监听器
    const cleanupDragHandlers = () => {
      if (currentDragHandlers.onMouseMove) {
        document.removeEventListener('mousemove', currentDragHandlers.onMouseMove)
        currentDragHandlers.onMouseMove = null
      }
      if (currentDragHandlers.onMouseUp) {
        document.removeEventListener('mouseup', currentDragHandlers.onMouseUp)
        currentDragHandlers.onMouseUp = null
      }
      // 确保状态重置
      isDragging.value = false
    }
    
    // 拖动功能
    const startDrag = e => {
      // 先清理旧的监听器，避免重复绑定
      cleanupDragHandlers()
      
      // 确保状态完全重置
      isDragging.value = true
      dragStart.x = e.clientX - position.x
      dragStart.y = e.clientY - position.y
      
      const onMouseMove = e => {
        if (!isDragging.value) return
        // 拖动范围保持原来的相对宽松限制，避免感觉被"卡住"
        position.x = Math.max(0, Math.min(window.innerWidth - 300, e.clientX - dragStart.x))
        position.y = Math.max(0, Math.min(window.innerHeight - 100, e.clientY - dragStart.y))
      }
      
      const onMouseUp = () => {
        cleanupDragHandlers()
      }
      
      // 保存引用并绑定
      currentDragHandlers.onMouseMove = onMouseMove
      currentDragHandlers.onMouseUp = onMouseUp
      document.addEventListener('mousemove', onMouseMove)
      document.addEventListener('mouseup', onMouseUp)
    }
    
    // 清理旧的缩放监听器
    const cleanupResizeHandlers = () => {
      if (currentResizeHandlers.onMouseMove) {
        document.removeEventListener('mousemove', currentResizeHandlers.onMouseMove)
        currentResizeHandlers.onMouseMove = null
      }
      if (currentResizeHandlers.onMouseUp) {
        document.removeEventListener('mouseup', currentResizeHandlers.onMouseUp)
        currentResizeHandlers.onMouseUp = null
      }
      isResizing.value = false
    }
    
    const startResize = (e, dir) => {
      // 先清理旧的监听器
      cleanupResizeHandlers()
      
      isResizing.value = true
      resizeDir.value = dir
      resizeStart.x = e.clientX
      resizeStart.y = e.clientY
      const rect = monitorRef.value?.getBoundingClientRect()
      resizeStart.width = rect?.width || size.width || 800
      resizeStart.height = rect?.height || size.height || 360

      const onMouseMove = ev => {
        if (!isResizing.value) return
        const dx = ev.clientX - resizeStart.x
        const dy = ev.clientY - resizeStart.y
        if (dir === 'right' || dir === 'corner') {
          size.width = Math.max(260, resizeStart.width + dx)
        }
        if (dir === 'bottom' || dir === 'corner') {
          size.height = Math.max(160, resizeStart.height + dy)
        }
      }

      const onMouseUp = () => {
        cleanupResizeHandlers()
        resizeDir.value = null
      }

      // 保存引用并绑定
      currentResizeHandlers.onMouseMove = onMouseMove
      currentResizeHandlers.onMouseUp = onMouseUp
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
        const baseURL = import.meta.env.VITE_API_BASE_URL || '/api'
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
      let fullStack = ''
      
      if (stack) {
        fullStack = stack
        // 从堆栈中提取第一个有用的文件位置
        const lines = stack.split('\n')
        for (const stackLine of lines) {
          // 优先匹配项目源码（src/）
          let match = stackLine.match(/\(([^)]*src\/[^)]+):(\d+):(\d+)\)/)
          if (!match) {
            // 匹配其他格式（包括 webpack-internal 格式）
            match = stackLine.match(/at\s+(?:.*?\s+)?\(?([^)]+):(\d+):(\d+)\)?/)
          }
          if (!match) {
            // 匹配 element-plus 等库的格式
            match = stackLine.match(/\(([^)]*\/(?:element-plus|node_modules)\/[^)]+):(\d+):(\d+)\)/)
          }
          
          if (match) {
            const fullPath = match[1]
            const lineNum = match[2]
            const colNum = match[3]
            
            // 跳过监控组件自身
            if (/ErrorMonitor/.test(fullPath)) {
              continue
            }
            
            // 优先显示项目源码，如果没有则显示库文件
            if (fullPath.includes('src/')) {
              // 简化项目路径
              let simplePath = fullPath.substring(fullPath.indexOf('src/'))
              file = `${simplePath}:${lineNum}:${colNum}`
              break
            } else if (!file && /element-plus|node_modules/.test(fullPath)) {
              // 如果没有找到项目文件，至少记录库文件位置
              const libMatch = fullPath.match(/(element-plus\/[^/]+\/[^/]+\/[^/]+\.m?js)/)
              if (libMatch) {
                file = `${libMatch[1]}:${lineNum}:${colNum}`
              } else {
                // 提取最后的文件名
                const fileName = fullPath.split('/').pop()
                file = `${fileName}:${lineNum}:${colNum}`
              }
            }
          }
        }
      }
      
      return { file, stack: fullStack }
    }
    
    // 改进 Vue 警告清理，保留更多有用信息
    const cleanVueWarn = content => {
      // 提取主要错误信息（不截断）
      const warnMatch = content.match(/\[Vue warn\]:\s*(.+?)(?=\s+at\s+<|$)/s)
      const coreMessage = warnMatch ? warnMatch[1].trim() : content
      
      // 提取组件链
      const componentChain = []
      const atMatches = content.matchAll(/at\s+<([A-Z][a-zA-Z]+)/g)
      for (const match of atMatches) {
        if (!componentChain.includes(match[1])) componentChain.push(match[1])
      }
      
      return {
        content: coreMessage,
        componentChain: componentChain.length > 0 ? componentChain.join(' → ') : ''
      }
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
        
        // 特殊处理 Pinia 错误
        if (content.includes('[vuex]')) {
          addMessage(content, 'error', 'Pinia错误', stack)
        } else {
          addMessage(content, 'error', '错误', stack)
        }
        
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
        } else if (content.includes('ElementPlusError')) {
          // 特殊处理 ElementPlus 错误，尝试提取更多信息
          const fullContent = args.map(a => {
            if (typeof a === 'object' && a.message) return a.message
            if (typeof a === 'object') return safeStringify(a)
            return String(a)
          }).join(' ')
          addMessage(fullContent, 'warn', 'ElementPlus警告', stack)
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
      // 只复制错误，不复制警告
      const errors = messages.filter(m => m.type !== 'warn')
      
      if (errors.length === 0) {
        ElMessage.info('没有错误日志（警告已过滤）')
        return
      }
      
      let text = '=== 错误日志（用于 AI 分析）===\n\n'
      text += `总计 ${errors.length} 个错误（已过滤 ${messages.length - errors.length} 个警告）\n`
      text += `时间: ${new Date().toLocaleString()}\n\n`
      text += '---\n\n'
      
      errors.forEach((m, index) => {
        text += `## 错误 ${index + 1}: ${m.typeLabel}\n\n`
        text += `**时间**: ${m.time}\n`
        text += `**文件**: ${m.file}\n`
        if (m.componentChain) {
          text += `**组件链**: ${m.componentChain}\n`
        }
        text += `**错误内容**:\n${m.content}\n\n`
        if (m.stack) {
          text += `**完整堆栈**:\n\`\`\`\n${m.stack}\n\`\`\`\n\n`
        }
        text += '---\n\n'
      })
      
      navigator.clipboard.writeText(text).then(() => {
        ElMessage.success(`已复制 ${errors.length} 条错误日志（Markdown 格式，已过滤警告）`)
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
  // 清理所有事件监听器
  cleanupDragHandlers()
  cleanupResizeHandlers()
})
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
  min-width: 260px;
  overflow: hidden;
}

.error-monitor.collapsed {
  max-height: auto;
}

.resize-handle {
  position: absolute;
  z-index: 2;
}

.resize-handle.right {
  top: 12px;
  bottom: 12px;
  right: 0;
  width: 6px;
  cursor: ew-resize;
}

.resize-handle.bottom {
  left: 12px;
  right: 12px;
  bottom: 0;
  height: 6px;
  cursor: ns-resize;
}

.resize-handle.corner {
  right: 0;
  bottom: 0;
  width: 10px;
  height: 10px;
  cursor: nwse-resize;
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
.toggle-btn:hover { background: #007acc; }

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
