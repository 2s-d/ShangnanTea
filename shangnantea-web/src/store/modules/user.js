import { login, register, logout, getCurrentUser, updateUserInfo, changePassword } from '@/api/user'
import { useTokenStorage } from '@/composables/useStorage'
import router from '@/router'
import userMessages from '@/utils/userMessages'

// 创建token存储实例
const tokenStorage = useTokenStorage()

// 用户相关的状态管理
const state = () => ({
  userInfo: null,
  isLoggedIn: false,
  loading: false
})

const getters = {
  // 获取用户角色
  userRole: state => state.userInfo?.role || 0,
  
  // 检查是否是管理员
  isAdmin: state => state.userInfo?.role === 1,
  
  // 检查是否是普通用户
  isUser: state => state.userInfo?.role === 2,
  
  // 检查是否是商家
  isShop: state => state.userInfo?.role === 3
}

const mutations = {
  // 设置用户信息
  SET_USER_INFO(state, userInfo) {
    state.userInfo = userInfo
  },
  
  // 设置登录状态
  SET_LOGGED_IN(state, status) {
    state.isLoggedIn = status
  },
  
  // 清除用户信息
  CLEAR_USER(state) {
    state.userInfo = null
    state.isLoggedIn = false
  },
  
  // 设置加载状态
  SET_LOADING(state, status) {
    state.loading = status
  }
}

const actions = {
  /* UI-DEV-START */
  // 开发时的模拟登录功能
  login({ commit }, loginData) {
    return new Promise((resolve) => {
      console.log('开发模式：模拟登录请求', loginData)
      
      // 模拟登录成功
      const fakeToken = 'dev_token_' + Date.now()
      localStorage.setItem('token', fakeToken)
      
      // 根据用户名确定角色（可选）
      let role = 2 // 默认普通用户
      if (loginData.username === 'admin') {
        role = 1
      } else if (loginData.username === 'shop') {
        role = 3
      }
      
      // 创建模拟用户数据
      const mockUserInfo = {
        id: 'dev_' + Date.now(),
        username: loginData.username,
        nickname: loginData.username,
        avatar: '/avatar/default.png',
        role: role
      }
      
      // 更新状态
      commit('SET_USER_INFO', mockUserInfo)
      commit('SET_LOGGED_IN', true)
      
      // 模拟网络延迟
      setTimeout(() => {
        resolve(mockUserInfo)
      }, 500)
    })
  },
  /* UI-DEV-END */
  
  /* 
  // 真实代码(开发UI时注释)
  async login({ commit }, loginData) {
    try {
      // 调用登录API
      const response = await loginApi(loginData)
      const { token, userInfo } = response
      
      // 存储token
      localStorage.setItem('token', token)
      
      // 更新状态
      commit('SET_USER_INFO', userInfo)
      commit('SET_LOGGED_IN', true)
      
      return userInfo
    } catch (error) {
      throw error
    }
  }
  */
  
  // 用户注册
  async register({ commit }, registerData) {
    try {
      commit('SET_LOADING', true)
      
      // 调用注册API
      const result = await register(registerData)
      
      // 显示注册成功消息
      userMessages.business.showRegisterSuccess()
      
      return result
    } catch (error) {
      // 显示注册失败消息
      userMessages.business.showRegisterFailure(error.message)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // 退出登录
  async logout({ commit }) {
    try {
      commit('SET_LOADING', true)
      
      // 调用登出API
      await logout()
      
      // 清除token和用户信息
      tokenStorage.removeToken()
      commit('CLEAR_USER')
      
      // 显示退出成功消息
      userMessages.business.showLogoutSuccess()
      
      return true
    } catch (error) {
      console.error('退出登录失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // 初始化用户信息
  async initAuth({ commit, state }) {
    // 如果已经有用户信息，不再重复初始化
    if (state.userInfo) {
      return state.userInfo
    }
    
    // 获取token
    const token = tokenStorage.getToken()
    
    // 没有token，则未登录
    if (!token) {
      return null
    }
    
    // 检查token有效性
    if (!tokenStorage.verifyToken()) {
      tokenStorage.removeToken()
      return null
    }
    
    try {
      commit('SET_LOADING', true)
      
      // 从服务器获取最新用户信息
      const userInfo = await getCurrentUser()
      
      // 更新状态
      commit('SET_USER_INFO', userInfo)
      commit('SET_LOGGED_IN', true)
      
      return userInfo
    } catch (error) {
      console.error('初始化用户信息失败:', error)
      tokenStorage.removeToken()
      commit('CLEAR_USER')
      return null
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // 获取用户信息
  // TODO: 该方法将在Vuex开发阶段实现，UI开发阶段使用模拟数据
  // NOTE: 复用initAuth方法进行临时模拟实现
  async getUserInfo({ commit, state, dispatch }) {
    // UI开发阶段使用简化版实现
    /* UI-DEV-START */
    // 开发模式下模拟用户信息获取
    if (localStorage.getItem('dev_mode') === 'true') {
      // 获取开发角色
      const devRole = parseInt(localStorage.getItem('dev_current_role') || '2')
      
      // 创建模拟用户数据
      const mockUserInfo = {
        id: 'user10001',
        username: `dev_user_${devRole}`,
        nickname: `开发测试用户${devRole}`,
        email: 'test@example.com',
        phone: '13800138000',
        avatar: '/images/avatar/default.png',
        role: devRole,
        createTime: '2023-03-15T08:00:00.000Z',
        status: 1,
        is_verified: 1,
        last_login_time: '2023-06-20T10:30:00.000Z',
        gender: devRole === 1 ? 1 : (devRole === 2 ? 2 : 0), // 管理员默认男性，普通用户默认女性，商家默认保密
        birthday: '1990-01-01',
        bio: `这是一个${devRole === 1 ? '管理员' : (devRole === 2 ? '普通用户' : '商家')}的个人简介，用于测试个人中心页面的展示效果。`
      }
      
      // 更新状态
      commit('SET_USER_INFO', mockUserInfo)
      commit('SET_LOGGED_IN', true)
      
      return mockUserInfo
    }
    /* UI-DEV-END */
    
    // 复用initAuth方法 - 实际开发时会替换为专用实现
    return dispatch('initAuth')
  },
  
  // 更新用户信息
  async updateUserInfo({ commit, state }, newUserInfo) {
    if (!state.userInfo) {
      userMessages.business.showSessionExpired()
      return false
    }
    
    try {
      commit('SET_LOADING', true)
      
      // 调用更新API
      const userData = await updateUserInfo(newUserInfo)
      
      // 更新状态
      commit('SET_USER_INFO', userData)
      
      // 显示更新成功消息
      userMessages.business.showProfileUpdateSuccess()
      
      return userData
    } catch (error) {
      // 显示更新失败消息
      userMessages.business.showProfileUpdateFailure(error.message)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // 修改密码
  async changePassword({ commit }, passwordData) {
    try {
      commit('SET_LOADING', true)
      
      // 检查新密码与确认密码是否一致
      if (passwordData.newPassword !== passwordData.confirmPassword) {
        userMessages.business.showPasswordMismatch()
        return false
      }
      
      // 调用修改密码API
      await changePassword(passwordData)
      
      // 显示修改成功消息
      userMessages.business.showPasswordChangeSuccess()
      
      return true
    } catch (error) {
      // 显示修改失败消息
      userMessages.business.showPasswordChangeFailure(error.message)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // 处理会话过期
  handleSessionExpired({ commit }) {
    // 清除token和用户信息
    tokenStorage.removeToken()
    commit('CLEAR_USER')
    
    // 显示会话过期消息
    userMessages.business.showSessionExpired()
  },
  
  // 处理权限拒绝
  handlePermissionDenied() {
    // 显示权限拒绝消息
    userMessages.business.showPermissionDenied()
  },
  
  // 处理认证错误
  handleAuthError({ commit }) {
    // 清除token和用户信息
    tokenStorage.removeToken()
    commit('CLEAR_USER')
    
    // 显示认证错误消息
    userMessages.business.showSessionExpired()
  }
}

export default {
  namespaced: true,
  state,
  getters,
  mutations,
  actions
} 