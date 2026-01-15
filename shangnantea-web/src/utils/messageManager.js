/**
 * 消息管理器 - 双轨制消息系统的底层显示引擎
 * 
 * 本文件是双轨制消息系统的底层实现，提供基础的消息显示功能：
 * - apiMessage: 供 apiMessages.js 使用，用于API响应消息显示
 * - promptMessage: 供 promptMessages.js 使用，用于前端提示消息显示
 * 
 * 注意：本文件只提供底层消息显示功能，不包含业务消息定义
 * 业务消息定义请参考：
 * - promptMessages.js: 提示消息定义（表单验证、用户确认等）
 * - apiMessages.js: API状态码消息映射（后端响应消息）
 */

import message from '@/components/common'
import { addMessageRecord } from '@/components/common/feedback/MessageMonitor.vue'

// 开发环境中的消息日志
const isDev = process.env.NODE_ENV === 'development' || 
             (typeof import.meta !== 'undefined' && import.meta.env && import.meta.env.DEV);

// 消息类型
export const MESSAGE_TYPE = {
  SUCCESS: 'success',
  WARNING: 'warning',
  INFO: 'info',
  ERROR: 'error'
}

// 消息层级（用于日志标识）
export const MESSAGE_LAYER = {
  API: 'API消息',      // API响应消息
  PROMPT: '提示消息'   // 前端提示消息
}

// 防止重复消息，存储最近的消息
const recentMessages = new Map()

// 消息队列配置
const MESSAGE_QUEUE = {
  active: false,      // 队列是否激活中
  messages: [],       // 待显示的消息队列
  maxWait: 5000,      // 消息最长等待时间
  interval: 2000      // 消息显示间隔
}

/**
 * 消息优先级 (1最高，4最低)
 */
const PRIORITY = {
  [MESSAGE_TYPE.ERROR]: 1,
  [MESSAGE_TYPE.WARNING]: 2,
  [MESSAGE_TYPE.SUCCESS]: 3,
  [MESSAGE_TYPE.INFO]: 4
}

/**
 * 日志消息到控制台和监控面板
 * @param {string} content 消息内容
 * @param {string} type 消息类型
 * @param {string} layer 消息层级
 */
function logMessage(content, type, layer) {
  // 添加到监控面板
  if (process.env.NODE_ENV === 'development') {
    addMessageRecord(content, type, layer)
    
    // 控制台彩色日志
    const styles = {
      [MESSAGE_TYPE.SUCCESS]: 'color: #4caf50; background: #e8f5e9; padding: 2px 4px; border-radius: 2px;',
      [MESSAGE_TYPE.ERROR]: 'color: #f44336; background: #ffebee; padding: 2px 4px; border-radius: 2px;',
      [MESSAGE_TYPE.WARNING]: 'color: #ff9800; background: #fff3e0; padding: 2px 4px; border-radius: 2px;',
      [MESSAGE_TYPE.INFO]: 'color: #2196f3; background: #e3f2fd; padding: 2px 4px; border-radius: 2px;'
    }
    
    console.log(
      `%c${type.toUpperCase()}%c [${layer}] ${content}`, 
      styles[type] || '', 
      'color: gray;'
    )
  }
}

/**
 * 处理消息队列
 */
function processMessageQueue() {
  if (MESSAGE_QUEUE.active || MESSAGE_QUEUE.messages.length === 0) {
    return
  }
  
  // 按优先级排序
  MESSAGE_QUEUE.messages.sort((a, b) => 
    PRIORITY[a.type] - PRIORITY[b.type]
  )
  
  // 激活队列，显示第一条消息
  MESSAGE_QUEUE.active = true
  const { content, type, duration } = MESSAGE_QUEUE.messages.shift()
  
  // 显示消息
  message[type](content, duration)
  
  // 设置间隔，处理下一条消息
  setTimeout(() => {
    MESSAGE_QUEUE.active = false
    processMessageQueue()
  }, MESSAGE_QUEUE.interval)
}

/**
 * 向队列添加消息
 * @param {string} content 消息内容
 * @param {string} type 消息类型
 * @param {number} duration 显示时长
 */
function enqueueMessage(content, type, duration = 3000) {
  MESSAGE_QUEUE.messages.push({ content, type, duration })
  
  // 为队列中老的消息设置超时
  const now = Date.now()
  MESSAGE_QUEUE.messages = MESSAGE_QUEUE.messages.filter(msg => {
    if (!msg.timestamp) {
      msg.timestamp = now
      return true
    }
    // 移除等待太久的消息
    return now - msg.timestamp < MESSAGE_QUEUE.maxWait
  })
  
  // 处理队列
  processMessageQueue()
}

/**
 * 防止重复消息
 * @param {string} content 消息内容
 * @param {string} type 消息类型
 * @returns {boolean} 是否是重复消息
 */
function isDuplicateMessage(content, type) {
  const key = `${type}:${content}`
  const now = Date.now()
  
  // 检查是否在短时间内重复
  if (recentMessages.has(key)) {
    const timestamp = recentMessages.get(key)
    if (now - timestamp < 3000) { // 3秒内的相同消息被视为重复
      return true
    }
  }
  
  // 更新最近消息记录
  recentMessages.set(key, now)
  
  // 清理过期记录
  if (recentMessages.size > 50) {
    // 清理超过10秒的记录
    for (const [msgKey, timestamp] of recentMessages.entries()) {
      if (now - timestamp > 10000) {
        recentMessages.delete(msgKey)
      }
    }
  }
  
  return false
}

/**
 * 清除所有消息状态
 */
export function clearAllMessageStates() {
  recentMessages.clear()
  MESSAGE_QUEUE.messages = []
  MESSAGE_QUEUE.active = false
}

/**
 * API消息 - 供 apiMessages.js 使用，用于API响应消息显示
 */
export const apiMessage = {
  success(content, duration = 3000) {
    if (isDuplicateMessage(content, MESSAGE_TYPE.SUCCESS)) return
    
    logMessage(content, MESSAGE_TYPE.SUCCESS, MESSAGE_LAYER.API)
    enqueueMessage(content, MESSAGE_TYPE.SUCCESS, duration)
  },
  
  error(content, duration = 5000) {
    if (isDuplicateMessage(content, MESSAGE_TYPE.ERROR)) return
    
    logMessage(content, MESSAGE_TYPE.ERROR, MESSAGE_LAYER.API)
    enqueueMessage(content, MESSAGE_TYPE.ERROR, duration)
  },
  
  warning(content, duration = 4000) {
    if (isDuplicateMessage(content, MESSAGE_TYPE.WARNING)) return
    
    logMessage(content, MESSAGE_TYPE.WARNING, MESSAGE_LAYER.API)
    enqueueMessage(content, MESSAGE_TYPE.WARNING, duration)
  },
  
  info(content, duration = 3000) {
    if (isDuplicateMessage(content, MESSAGE_TYPE.INFO)) return
    
    logMessage(content, MESSAGE_TYPE.INFO, MESSAGE_LAYER.API)
    enqueueMessage(content, MESSAGE_TYPE.INFO, duration)
  }
}

/**
 * 提示消息 - 供 promptMessages.js 使用，用于前端提示消息显示
 * 统一使用 warning 类型显示，info 用于确认类提示
 */
export const promptMessage = {
  show(content, duration = 3000) {
    if (isDuplicateMessage(content, MESSAGE_TYPE.WARNING)) return
    
    logMessage(content, MESSAGE_TYPE.WARNING, MESSAGE_LAYER.PROMPT)
    enqueueMessage(content, MESSAGE_TYPE.WARNING, duration)
  },
  
  info(content, duration = 3000) {
    if (isDuplicateMessage(content, MESSAGE_TYPE.INFO)) return
    
    logMessage(content, MESSAGE_TYPE.INFO, MESSAGE_LAYER.PROMPT)
    enqueueMessage(content, MESSAGE_TYPE.INFO, duration)
  }
}

/**
 * 动态加载消息监控面板
 * 仅在开发环境中使用
 */
export function loadMessageMonitor() {
  if (process.env.NODE_ENV !== 'development') {
    return Promise.resolve(null)
  }
  
  return import('@/components/common/feedback/MessageMonitor.vue').then(module => {
    const { toggleMonitorVisibility } = module
    return { toggleVisibility: toggleMonitorVisibility }
  })
}

// 模块默认导出
export default {
  apiMessage,
  promptMessage,
  clearAllMessageStates
} 