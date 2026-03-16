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
    this.heartbeatInterval = 30000 // 30秒心跳
    this.heartbeatTimer = null
    this.listeners = new Map() // 存储消息监听器
    this.isConnecting = false
  }

  /**
   * 连接WebSocket
   */
  connect() {
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
        this.startHeartbeat()
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
        this.stopHeartbeat()
        this.attemptReconnect()
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
    // 处理心跳响应
    if (data === 'pong') {
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
   * 开始心跳
   * @param {boolean} forceImmediate 是否强制立即发送心跳（用于恢复在线状态）
   */
  startHeartbeat(forceImmediate = false) {
    // 如果定时器已经存在且连接正常
    if (this.heartbeatTimer && this.ws && this.ws.readyState === WebSocket.OPEN) {
      // 如果需要强制立即发送（恢复在线），立即发送一次
      if (forceImmediate) {
        this.send('ping')
        console.log('[WebSocket] 强制立即发送心跳，恢复在线状态')
      }
      // 否则心跳已经在运行，不需要重复启动
      return
    }
    
    this.stopHeartbeat()
    
    // 立即发送一次心跳，确保快速恢复在线状态
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.send('ping')
      console.log('[WebSocket] 立即发送心跳，恢复在线状态')
    } else {
      console.warn('[WebSocket] 连接未就绪，无法发送心跳，连接状态:', this.ws ? this.ws.readyState : 'null')
      return // 连接未就绪，不创建定时器
    }
    
    // 设置定时心跳（30秒间隔，用于保持在线状态）
    this.heartbeatTimer = setInterval(() => {
      if (this.ws && this.ws.readyState === WebSocket.OPEN) {
        this.send('ping')
      } else {
        console.warn('[WebSocket] 心跳发送失败，连接状态异常，尝试重连')
        this.stopHeartbeat()
        // 如果连接异常，尝试重连
        if (this.ws && this.ws.readyState !== WebSocket.CONNECTING) {
          this.attemptReconnect()
        }
      }
    }, this.heartbeatInterval)
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
