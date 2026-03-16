/**
 * WebSocket工具类
 * 用于实时推送在线状态、消息通知等
 */

import { useTokenStorage } from '@/composables/useStorage'

class WebSocketManager {
  constructor() {
    this.ws = null
    this.reconnectTimer = null
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
    this.reconnectInterval = 3000 // 3秒

    // 通道心跳（ping0）：只用于保持通道存活 / 探测半开连接
    this.channelHeartbeatInterval = 30000 // 30秒通道心跳
    this.channelHeartbeatTimer = null

    // 业务心跳（ping1）：用于续用户「在线」TTL
    this.heartbeatInterval = 60 * 1000 // 1分钟业务心跳
    this.heartbeatTimer = null

    // 业务心跳立即补发标记：用于解决“连接中/重连后”恢复在线的空窗
    this.pendingImmediateBusinessHeartbeat = false
    this.listeners = new Map() // 存储消息监听器
    this.isConnecting = false
    this.shouldReconnect = true
    this.blockedReconnectToken = null // 认证失效导致的关闭：阻止同一token继续重连刷屏
  }

  /**
   * 连接WebSocket
   */
  connect() {
    // 如果当前 token 被标记为“禁止重连”（通常是被踢/登出导致会话失效），则不要继续尝试连接
    try {
      const tokenStorage = useTokenStorage()
      const token = tokenStorage.getToken()
      if (this.blockedReconnectToken && token && token === this.blockedReconnectToken) {
        console.warn('[WebSocket] 当前token已被标记为禁止重连，跳过连接')
        return
      }
    } catch (e) {
      // ignore
    }

    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      console.log('[WebSocket] 已连接，无需重复连接')
      return
    }

    if (this.isConnecting) {
      console.log('[WebSocket] 正在连接中，请勿重复调用')
      return
    }

    this.isConnecting = true

    try {
      const tokenStorage = useTokenStorage()
      const token = tokenStorage.getToken()

      if (!token) {
        console.warn('[WebSocket] 未找到token，无法连接')
        this.isConnecting = false
        return
      }

      // 构建WebSocket URL
      // 开发环境：直接连接到后端服务器（8080端口）
      // 生产环境：使用当前域名
      const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
      let host = window.location.host
      
      // 开发环境：如果是 localhost:8082，改为 localhost:8080（后端端口）
      if (host.includes('localhost:8082') || host.includes('127.0.0.1:8082')) {
        host = host.replace(':8082', ':8080')
      }
      
      const wsUrl = `${protocol}//${host}/api/ws?token=${encodeURIComponent(token)}`

      console.log('[WebSocket] 开始连接:', wsUrl)

      this.ws = new WebSocket(wsUrl)

      this.ws.onopen = () => {
        console.log('[WebSocket] 连接成功')
        this.isConnecting = false
        this.reconnectAttempts = 0
        // 连接建立后立即启动通道心跳（ping0），保证通道长期存活
        this.startChannelHeartbeat()

        // 如果之前有“恢复在线”诉求（例如：重连期间用户已操作/页面已恢复可见），连接成功后立刻补发一次 ping1
        if (this.pendingImmediateBusinessHeartbeat) {
          this.startHeartbeat(true)
          this.pendingImmediateBusinessHeartbeat = false
        }
      }

      this.ws.onmessage = (event) => {
        this.handleMessage(event.data)
      }

      this.ws.onerror = (error) => {
        console.error('[WebSocket] 连接错误:', error)
        this.isConnecting = false
      }

      this.ws.onclose = (event) => {
        console.log('[WebSocket] 连接关闭:', event.code, event.reason)
        this.isConnecting = false
        this.stopChannelHeartbeat()
        this.stopHeartbeat()

        // 如果是认证相关关闭（被踢/登出/token失效），阻止同一token继续重连刷屏
        const reason = (event && typeof event.reason === 'string') ? event.reason : ''
        if (reason.includes('login session invalid') || reason.includes('token invalid')) {
          try {
            const tokenStorage = useTokenStorage()
            this.blockedReconnectToken = tokenStorage.getToken()
          } catch (e) {
            // ignore
          }
          this.shouldReconnect = false
        }

        if (this.shouldReconnect) {
          this.attemptReconnect()
        }
      }
    } catch (error) {
      console.error('[WebSocket] 连接异常:', error)
      this.isConnecting = false
      this.attemptReconnect()
    }
  }

  /**
   * 处理接收到的消息
   */
  handleMessage(data) {
    // 处理通道/业务心跳响应
    if (data === 'pong0' || data === 'pong1') {
      return
    }

    try {
      const message = JSON.parse(data)
      console.log('[WebSocket] 收到消息:', message)

      // 根据消息类型分发
      const { type } = message
      if (this.listeners.has(type)) {
        const callbacks = this.listeners.get(type)
        callbacks.forEach(callback => {
          try {
            callback(message)
          } catch (error) {
            console.error(`[WebSocket] 消息处理回调执行失败 (type: ${type}):`, error)
          }
        })
      }
    } catch (error) {
      console.error('[WebSocket] 消息解析失败:', error, '原始数据:', data)
    }
  }

  /**
   * 发送消息
   */
  send(data) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(typeof data === 'string' ? data : JSON.stringify(data))
    } else {
      console.warn('[WebSocket] 连接未建立，无法发送消息')
    }
  }

  /**
   * 订阅消息
   * @param {string} type 消息类型
   * @param {Function} callback 回调函数
   */
  on(type, callback) {
    if (!this.listeners.has(type)) {
      this.listeners.set(type, [])
    }
    this.listeners.get(type).push(callback)
  }

  /**
   * 取消订阅
   * @param {string} type 消息类型
   * @param {Function} callback 回调函数（可选，不传则移除该类型的所有监听器）
   */
  off(type, callback) {
    if (!this.listeners.has(type)) {
      return
    }

    if (callback) {
      const callbacks = this.listeners.get(type)
      const index = callbacks.indexOf(callback)
      if (index > -1) {
        callbacks.splice(index, 1)
      }
      if (callbacks.length === 0) {
        this.listeners.delete(type)
      }
    } else {
      this.listeners.delete(type)
    }
  }

  /**
   * 启动通道心跳（ping0）
   * 仅用于保持 WebSocket 通道存活 / 探测半开连接，不参与业务在线状态
   */
  startChannelHeartbeat() {
    if (this.channelHeartbeatTimer && this.ws && this.ws.readyState === WebSocket.OPEN) {
      return
    }

    this.stopChannelHeartbeat()

    // 立即发送一次通道心跳，帮助尽快发现半开连接
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.send('ping0')
      console.log('[WebSocket] 立即发送通道心跳 ping0')
    } else {
      console.warn('[WebSocket] 连接未就绪，无法发送通道心跳，连接状态:', this.ws ? this.ws.readyState : 'null')
      return // 连接未就绪，不创建定时器
    }

    this.channelHeartbeatTimer = setInterval(() => {
      if (this.ws && this.ws.readyState === WebSocket.OPEN) {
        this.send('ping0')
      } else {
        console.warn('[WebSocket] 通道心跳发送失败，连接状态异常，尝试重连')
        this.stopChannelHeartbeat()
        // 如果连接异常，尝试重连
        if (this.ws && this.ws.readyState !== WebSocket.CONNECTING) {
          this.attemptReconnect()
        }
      }
    }, this.channelHeartbeatInterval)
  }

  /**
   * 停止通道心跳（ping0）
   */
  stopChannelHeartbeat() {
    if (this.channelHeartbeatTimer) {
      clearInterval(this.channelHeartbeatTimer)
      this.channelHeartbeatTimer = null
    }
  }

  /**
   * 开始业务心跳（ping1）
   * @param {boolean} forceImmediate 是否强制立即发送心跳（用于恢复在线状态）
   */
  startHeartbeat(forceImmediate = false) {
    // 如果定时器已经存在且连接正常
    if (this.heartbeatTimer && this.ws && this.ws.readyState === WebSocket.OPEN) {
      if (forceImmediate) {
        this.send('ping1')
        // 避免刷屏：业务心跳可能在用户频繁操作时被多次触发
        if (import.meta.env.MODE === 'development') {
          console.debug('[WebSocket] 强制立即发送业务心跳 ping1，恢复在线状态')
        }
      }
      // 心跳已经在运行，不需要重复启动
      return
    }

    this.stopHeartbeat()

    // 立即发送一次业务心跳，确保快速恢复在线状态
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.send('ping1')
      console.log('[WebSocket] 立即发送业务心跳 ping1，恢复在线状态')
    } else {
      console.warn('[WebSocket] 连接未就绪，无法发送业务心跳，连接状态:', this.ws ? this.ws.readyState : 'null')
      return // 连接未就绪，不创建定时器
    }

    // 设置定时业务心跳（用于续在线TTL）
    this.heartbeatTimer = setInterval(() => {
      if (this.ws && this.ws.readyState === WebSocket.OPEN) {
        this.send('ping1')
      } else {
        console.warn('[WebSocket] 业务心跳发送失败，连接状态异常，尝试重连')
        this.stopHeartbeat()
        if (this.ws && this.ws.readyState !== WebSocket.CONNECTING) {
          this.attemptReconnect()
        }
      }
    }, this.heartbeatInterval)
  }

  /**
   * 请求“尽快补发一次业务心跳（ping1）”：
   * - 如果当前已连接：立刻发
   * - 如果正在连接/即将重连成功：在 onopen 里补发
   */
  requestImmediateBusinessHeartbeat() {
    this.pendingImmediateBusinessHeartbeat = true
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.startHeartbeat(true)
      this.pendingImmediateBusinessHeartbeat = false
    }
  }

  /**
   * 停止心跳
   */
  stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer)
      this.heartbeatTimer = null
    }
  }

  /**
   * 尝试重连
   */
  attemptReconnect() {
    if (!this.shouldReconnect) {
      return
    }

    // 没有 token 时不应重连（通常表示已登出/被清理）
    try {
      const tokenStorage = useTokenStorage()
      const token = tokenStorage.getToken()
      if (!token) {
        console.warn('[WebSocket] 未找到token，停止重连')
        return
      }
      if (this.blockedReconnectToken && token === this.blockedReconnectToken) {
        console.warn('[WebSocket] 当前token已被标记为禁止重连，停止重连')
        return
      }
    } catch (e) {
      // ignore
    }

    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.warn('[WebSocket] 达到最大重连次数，停止重连')
      return
    }

    this.reconnectAttempts++
    console.log(`[WebSocket] ${this.reconnectInterval / 1000}秒后尝试第${this.reconnectAttempts}次重连...`)

    this.reconnectTimer = setTimeout(() => {
      this.connect()
    }, this.reconnectInterval)
  }

  /**
   * 断开连接
   */
  disconnect() {
    this.shouldReconnect = false
    this.blockedReconnectToken = null
    this.stopChannelHeartbeat()
    this.stopHeartbeat()
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }
    if (this.ws) {
      this.ws.close()
      this.ws = null
    }
    this.listeners.clear()
    this.reconnectAttempts = 0
  }

  /**
   * 获取连接状态
   */
  getState() {
    if (!this.ws) {
      return WebSocket.CLOSED
    }
    return this.ws.readyState
  }

  /**
   * 是否已连接
   */
  isConnected() {
    return this.ws && this.ws.readyState === WebSocket.OPEN
  }
}

// 创建单例
const websocketManager = new WebSocketManager()

export default websocketManager
