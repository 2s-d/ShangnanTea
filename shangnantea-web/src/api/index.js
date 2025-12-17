/**
 * 标准HTTP请求实例 - 项目中所有API请求应使用此实例
 * 
 * ✅ 正确用法：import request from '@/api'
 * 
 * 特性：
 * - 统一的请求/响应拦截器
 * - 使用useTokenStorage处理token
 * - 标准化的错误处理
 * - 自动刷新过期token
 */

import axios from 'axios'
import store from '@/store'
import router from '@/router'
// 使用composable代替直接导入tokenUtils
import { useTokenStorage } from '@/composables/useStorage'
// 导入消息管理器
import { apiMessage } from '@/utils/messageManager'

// 创建token存储实例
const tokenStorage = useTokenStorage()
const { getToken, isTokenValid, verifyToken, removeToken } = tokenStorage

// 安全获取环境变量，避免undefined错误
const getApiBaseUrl = () => {
  // 检查是否在Vite环境中
  if (typeof import.meta !== 'undefined' && import.meta.env) {
    return import.meta.env.VITE_API_BASE_URL || '/api'
  }
  // 检查是否在Webpack环境中
  if (typeof process !== 'undefined' && process.env) {
    return process.env.VUE_APP_API_BASE_URL || '/api'
  }
  // 默认值
  return '/api'
}

// 创建axios实例
const service = axios.create({
  baseURL: getApiBaseUrl(), // 使用安全的方法获取API基础路径
  timeout: 15000, // 请求超时时间
  withCredentials: false, // 跨域请求是否携带cookie
  headers: {
    'Content-Type': 'application/json;charset=utf-8'
  }
})

// 请求计数器，用于生成请求ID
let requestCounter = 0;

// 活跃请求映射，用于开发环境监控
const activeRequests = new Map();

// 在开发环境中记录API调用
const logApiCall = (config, status, response = null, error = null) => {
  if (process.env.NODE_ENV !== 'development') return;
  
  // 确保config有requestId
  if (!config.requestId) {
    config.requestId = `req_${++requestCounter}`
  }
  
  const requestInfo = {
    url: `${config.method.toUpperCase()} ${config.url}`,
    params: config.params,
    data: config.data,
    time: new Date().toISOString(),
    status
  }
  
  if (status === 'start') {
    // 记录请求开始时间
    activeRequests.set(config.requestId, {
      ...requestInfo,
      startTime: Date.now()
    })
    console.log(`%cAPI请求 [${config.requestId}]: ${requestInfo.url}`, 'color: #9c27b0;')
  } 
  else if (status === 'success') {
    const requestData = activeRequests.get(config.requestId) || {}
    const duration = Date.now() - (requestData.startTime || Date.now())
    
    console.log(
      `%cAPI响应 [${config.requestId}]: ${requestInfo.url} (${duration}ms)`, 
      'color: #2e7d32;',
      response
    )
    
    // 完成请求，从活跃请求中移除
    activeRequests.delete(config.requestId)
  } 
  else if (status === 'error') {
    const requestData = activeRequests.get(config.requestId) || {}
    const duration = Date.now() - (requestData.startTime || Date.now())
    
    console.log(
      `%cAPI错误 [${config.requestId}]: ${requestInfo.url} (${duration}ms)`, 
      'color: #d32f2f;',
      error
    )
    
    // 错误请求，从活跃请求中移除
    activeRequests.delete(config.requestId)
  }
}

// 请求拦截器 - 整合前后端认证机制
service.interceptors.request.use(
  config => {
    // 生成请求ID，用于日志追踪
    config.requestId = `req_${++requestCounter}`
    
    // 从token存储中获取token
    const token = tokenStorage.getToken()
    
    // 当存在token时，为所有请求添加Authorization头
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    
    // 记录API调用开始
    logApiCall(config, 'start')
    
    return config
  },
  error => {
    console.error('请求拦截器错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器 - 统一处理错误和认证问题
service.interceptors.response.use(
  response => {
    // 记录API调用成功
    logApiCall(response.config, 'success', response.data)
    
    const res = response.data
    
    // 处理响应数据
    if (res.code === undefined) {
      // 直接返回没有code包装的数据
      return res
    }
    
    // 成功响应
    if (res.code === 200) {
      // 返回数据部分
      return res.data
    }
    
    // 认证相关错误（未登录、令牌无效等）
    if ([401, 403].includes(res.code)) {
      // 清除token和用户信息
      tokenStorage.removeToken()
      store.commit('user/CLEAR_USER')
      
      // 显示错误信息 - 使用apiMessage
      apiMessage.error(res.message || '认证失败，请重新登录')
      
      // 如果不是登录页，则跳转到登录页
      if (router.currentRoute.value.path !== '/login') {
        router.push(`/login?redirect=${router.currentRoute.value.path}`)
      }
      
      return Promise.reject(new Error(res.message || '认证失败'))
    }
    
    // 其他错误 - 使用apiMessage
    apiMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  error => {
    // 记录API调用错误
    if (error.config) {
      logApiCall(error.config, 'error', null, error)
    }
    
    // 处理HTTP错误
    if (error.response) {
      const { status, data } = error.response
      
      // 认证相关错误
      if ([401, 403].includes(status)) {
        // 清除token和用户信息
        tokenStorage.removeToken()
        store.commit('user/CLEAR_USER')
        
        // 跳转到登录页
        if (router.currentRoute.value.path !== '/login') {
          // 使用apiMessage
          apiMessage.error(data.message || '认证失败，请重新登录')
          router.push(`/login?redirect=${router.currentRoute.value.path}`)
        }
      } else if (status === 500) {
        // 使用apiMessage
        apiMessage.error('服务器错误，请稍后再试')
      } else {
        // 使用apiMessage
        apiMessage.error(data.message || `请求失败: ${status}`)
      }
    } else if (error.message.includes('timeout')) {
      // 使用apiMessage
      apiMessage.error('请求超时，请检查网络连接')
    } else {
      // 使用apiMessage
      apiMessage.error('网络错误，请检查网络连接')
    }
    
    return Promise.reject(error)
  }
)

export default service 