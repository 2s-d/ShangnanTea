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
import { useUserStore } from '@/stores/user'
import router from '@/router'
// 使用composable代替直接导入tokenUtils
import { useTokenStorage } from '@/composables/useStorage'
// 导入消息管理器
import { apiMessage } from '@/utils/messageManager'

// 创建token存储实例
const tokenStorage = useTokenStorage()

// 获取API基础URL（Vite项目使用VITE_前缀）
const getApiBaseUrl = () => {
  // Vite环境变量
  const baseUrl = import.meta.env.VITE_API_BASE_URL
  console.log('[API] Base URL:', baseUrl || '/api (default)')
  return baseUrl || '/api'
}

// 自定义参数序列化函数，支持数组参数（Spring Boot格式：categoryIds=5&categoryIds=6）
const paramsSerializer = (params) => {
  const searchParams = new URLSearchParams()
  
  for (const [key, value] of Object.entries(params)) {
    if (value === null || value === undefined) {
      continue
    }
    
    if (Array.isArray(value)) {
      // 数组：使用重复参数名格式 categoryIds=5&categoryIds=6
      value.forEach(item => {
        searchParams.append(key, item)
      })
    } else {
      // 非数组：直接添加
      searchParams.append(key, value)
    }
  }
  
  return searchParams.toString()
}

// 创建axios实例
const service = axios.create({
  baseURL: getApiBaseUrl(), // 使用安全的方法获取API基础路径
  timeout: 15000, // 请求超时时间
  withCredentials: false, // 跨域请求是否携带cookie
  headers: {
    'Content-Type': 'application/json;charset=utf-8'
  },
  // 自定义参数序列化，支持数组参数（Spring Boot格式：categoryIds=5&categoryIds=6）
  paramsSerializer: paramsSerializer
})

// 请求计数器，用于生成请求ID
let requestCounter = 0

// 活跃请求映射，用于开发环境监控
const activeRequests = new Map()

// token 刷新单飞锁：避免并发请求触发多次 refresh
let refreshPromise = null
// 距离过期多久开始刷新（仅在有请求触发时刷新）
const REFRESH_THRESHOLD_MS = 10 * 60 * 1000 // 10分钟

// 在开发环境中记录API调用
const logApiCall = (config, status, response = null, error = null) => {
  if (import.meta.env.MODE !== 'development') return
  
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
  async config => {
    // 生成请求ID，用于日志追踪
    config.requestId = `req_${++requestCounter}`
    
    // 从token存储中获取token
    const token = tokenStorage.getToken()

    // 仅在存在 token 且非刷新接口本身时，尝试“按需刷新”
    if (token && config?.url && !String(config.url).includes('/user/refresh')) {
      const tokenInfo = tokenStorage.verifyToken && tokenStorage.verifyToken()
      const expMs = tokenInfo?.exp
      if (typeof expMs === 'number') {
        const remainingMs = expMs - Date.now()
        if (remainingMs > 0 && remainingMs < REFRESH_THRESHOLD_MS) {
          try {
            if (!refreshPromise) {
              const userStore = useUserStore()
              refreshPromise = userStore.refreshToken().finally(() => {
                refreshPromise = null
              })
            }
            await refreshPromise
          } catch (e) {
            // 刷新失败：按未登录处理
            tokenStorage.removeToken()
            const userStore = useUserStore()
            userStore.userInfo = null
            userStore.isLoggedIn = false
            if (router.currentRoute.value.path !== '/login') {
              apiMessage.error('登录已过期，请重新登录')
              router.push(`/login?redirect=${router.currentRoute.value.path}`)
            }
            return Promise.reject(e)
          }
        }
      }
    }
    
    // 当存在token时，为所有请求添加Authorization头
    const latestToken = tokenStorage.getToken()
    if (latestToken) {
      config.headers['Authorization'] = `Bearer ${latestToken}`
    }
    
    // 处理FormData：如果是FormData，删除Content-Type让浏览器自动设置（包含boundary）
    if (config.data instanceof FormData) {
      delete config.headers['Content-Type']
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
    
    // 认证相关错误（未登录、令牌无效等）
    // 注意：只匹配精确的 401 和 403，不匹配业务错误码（如 4010, 4030 等）
    if (res.code === 401 || res.code === 403) {
      // 清除token和用户信息
      tokenStorage.removeToken()
      const userStore = useUserStore()
      userStore.userInfo = null
      userStore.isLoggedIn = false
      
      // 已在登录页时，不再反复弹"认证失败"提示，避免刷新时干扰体验
      if (router.currentRoute.value.path !== '/login') {
        // 显示错误信息
        apiMessage.error(res.message || '认证失败，请重新登录')
        // 跳转到登录页
        router.push(`/login?redirect=${router.currentRoute.value.path}`)
      }
      
      return Promise.reject(new Error(res.message || '认证失败'))
    }
    
    // 返回完整响应格式 {code, data}，不再自动显示消息
    return {
      code: res.code,
      data: res.data
    }
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
        const userStore = useUserStore()
        userStore.userInfo = null
        userStore.isLoggedIn = false
        
        // 跳转到登录页
        if (router.currentRoute.value.path !== '/login') {
          apiMessage.error(data.message || '认证失败，请重新登录')
          router.push(`/login?redirect=${router.currentRoute.value.path}`)
        }
      } else if (status === 500) {
        apiMessage.error('服务器错误，请稍后再试')
      } else {
        apiMessage.error(data.message || `请求失败: ${status}`)
      }
    } else {
      // 处理非HTTP错误（网络错误、连接被拒绝等）
      // 这些错误不应该清除用户信息，因为可能是后端服务器未运行
      const errorMessage = error.message || ''
      const errorCode = error.code || ''
      
      if (errorMessage.includes('timeout') || errorCode === 'ETIMEDOUT') {
        apiMessage.error('请求超时，请检查网络连接')
      } else if (errorCode === 'ECONNREFUSED' || errorMessage.includes('ECONNREFUSED')) {
        // 连接被拒绝（后端服务器未运行），不显示错误消息，避免干扰用户
        // 只在开发环境显示提示
        if (import.meta.env.MODE === 'development') {
          console.warn('后端服务器未运行或无法连接，请检查后端服务状态')
        }
      } else {
        apiMessage.error('网络错误，请检查网络连接')
      }
    }
    
    return Promise.reject(error)
  }
)

export default service 