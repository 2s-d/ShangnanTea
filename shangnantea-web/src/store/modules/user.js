import { 
  login, 
  register, 
  logout, 
  getCurrentUser, 
  updateUserInfo, 
  changePassword,
  getAddressList,
  addAddress as addAddressApi,
  updateAddress as updateAddressApi,
  deleteAddress as deleteAddressApi,
  setDefaultAddress as setDefaultAddressApi,
  getUserPreferences as getUserPreferencesApi,
  updateUserPreferences as updateUserPreferencesApi,
  submitShopCertification as submitShopCertificationApi,
  getShopCertificationStatus as getShopCertificationStatusApi
} from '@/api/user'
import { useTokenStorage } from '@/composables/useStorage'
import router from '@/router'
import userMessages from '@/utils/userMessages'

// 创建token存储实例
const tokenStorage = useTokenStorage()

// 用户相关的状态管理
const state = () => ({
  userInfo: null,
  isLoggedIn: false,
  loading: false,
  // 用户偏好设置（主题/展示等）
  preferences: {
    theme: 'light',
    primaryColor: '#409EFF',
    fontSize: 14,
    fontFamily: '',
    enableAnimation: true,
    listMode: 'grid',
    pageSize: 20
  },
  // 地址相关状态
  addressList: [],
  defaultAddressId: null
})

const getters = {
  // 获取用户角色
  userRole: state => state.userInfo?.role || 0,
  
  // 检查是否是管理员
  isAdmin: state => state.userInfo?.role === 1,
  
  // 检查是否是普通用户
  isUser: state => state.userInfo?.role === 2,
  
  // 检查是否是商家
  isShop: state => state.userInfo?.role === 3,
  
  // 获取默认地址
  defaultAddress: state => {
    if (state.defaultAddressId) {
      return state.addressList.find(addr => addr.id === state.defaultAddressId) || null
    }
    // 如果没有设置defaultAddressId，查找isDefault为true的地址
    return state.addressList.find(addr => addr.isDefault) || null
  }
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
  },

  /**
   * 设置用户偏好设置
   * @param {Object} state Vuex state
   * @param {Object} preferences 偏好设置
   */
  SET_PREFERENCES(state, preferences) {
    state.preferences = {
      ...state.preferences,
      ...(preferences || {})
    }
  },
  
  // === 地址相关mutations ===
  
  // 设置地址列表
  SET_ADDRESS_LIST(state, list) {
    state.addressList = list || []
    // 自动识别默认地址
    const defaultAddr = list?.find(addr => addr.isDefault)
    state.defaultAddressId = defaultAddr?.id || null
  },
  
  // 添加地址
  ADD_ADDRESS(state, address) {
    state.addressList.push(address)
    // 如果设置为默认，更新defaultAddressId
    if (address.isDefault) {
      // 取消其他地址的默认状态
      state.addressList.forEach(addr => {
        if (addr.id !== address.id) {
          addr.isDefault = false
        }
      })
      state.defaultAddressId = address.id
    }
  },
  
  // 更新地址
  UPDATE_ADDRESS(state, updatedAddress) {
    const index = state.addressList.findIndex(addr => addr.id === updatedAddress.id)
    if (index !== -1) {
      state.addressList.splice(index, 1, updatedAddress)
      // 如果设置为默认，更新defaultAddressId
      if (updatedAddress.isDefault) {
        // 取消其他地址的默认状态
        state.addressList.forEach(addr => {
          if (addr.id !== updatedAddress.id) {
            addr.isDefault = false
          }
        })
        state.defaultAddressId = updatedAddress.id
      } else if (state.defaultAddressId === updatedAddress.id) {
        // 如果取消默认，清空defaultAddressId
        state.defaultAddressId = null
      }
    }
  },
  
  // 删除地址
  REMOVE_ADDRESS(state, addressId) {
    state.addressList = state.addressList.filter(addr => addr.id !== addressId)
    // 如果删除的是默认地址，清空defaultAddressId
    if (state.defaultAddressId === addressId) {
      state.defaultAddressId = null
    }
  },
  
  // 设置默认地址
  SET_DEFAULT_ADDRESS(state, addressId) {
    // 取消所有地址的默认状态
    state.addressList.forEach(addr => {
      addr.isDefault = addr.id === addressId
    })
    state.defaultAddressId = addressId
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

  /**
   * 提交商家认证申请
   * @param {Object} context Vuex context
   * @param {Object} certificationData 认证数据
   * @returns {Promise<Object>} 提交结果
   */
  async submitShopCertification({ commit }, certificationData) {
    commit('SET_LOADING', true)
    try {
      return await submitShopCertificationApi(certificationData)
    } finally {
      commit('SET_LOADING', false)
    }
  },

  /**
   * 获取商家认证状态
   * @param {Object} context Vuex context
   * @returns {Promise<Object>} 状态数据
   */
  async fetchShopCertificationStatus({ commit }) {
    commit('SET_LOADING', true)
    try {
      return await getShopCertificationStatusApi()
    } finally {
      commit('SET_LOADING', false)
    }
  },

  /**
   * 获取用户偏好设置（主题/展示等）
   * @param {Object} context Vuex context
   * @returns {Promise<Object>} preferences
   */
  async fetchUserPreferences({ commit }) {
    commit('SET_LOADING', true)
    try {
      const result = await getUserPreferencesApi()
      // 兼容：后端可能返回 { data: {...} } 或直接返回 {...}
      const preferences = result?.data || result?.preferences || result
      commit('SET_PREFERENCES', preferences)
      return preferences
    } finally {
      commit('SET_LOADING', false)
    }
  },

  /**
   * 保存用户偏好设置（主题/展示等）
   * @param {Object} context Vuex context
   * @param {Object} preferences 偏好设置
   * @returns {Promise<Object>} 保存结果
   */
  async saveUserPreferences({ commit }, preferences) {
    commit('SET_LOADING', true)
    try {
      const result = await updateUserPreferencesApi(preferences)
      // 以服务端回写为准（若后端仅返回 success，则直接使用入参）
      const saved = result?.data || result?.preferences || result || preferences
      commit('SET_PREFERENCES', saved)
      return saved
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
  },
  
  // === 地址相关actions ===
  
  /**
   * 获取地址列表
   * @param {Object} context Vuex context
   * @returns {Promise} 地址列表
   */
  async fetchAddresses({ commit }) {
    commit('SET_LOADING', true)
    try {
      const res = await getAddressList()
      const addressList = res.data || []
      commit('SET_ADDRESS_LIST', addressList)
      return addressList
    } catch (error) {
      console.error('获取地址列表失败:', error)
      // 开发环境使用Mock数据
      if (process.env.NODE_ENV === 'development') {
        const mockAddresses = [
          {
            id: 1,
            userId: 1,
            name: '张三',
            phone: '13800138000',
            province: '陕西省',
            city: '商洛市',
            district: '商南县',
            detail: '城关镇XX路XX号',
            isDefault: true,
            createTime: '2024-01-01 10:00:00'
          },
          {
            id: 2,
            userId: 1,
            name: '李四',
            phone: '13900139000',
            province: '陕西省',
            city: '西安市',
            district: '雁塔区',
            detail: 'XX街道XX小区XX栋XX号',
            isDefault: false,
            createTime: '2024-01-15 14:30:00'
          }
        ]
        commit('SET_ADDRESS_LIST', mockAddresses)
        return mockAddresses
      }
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  /**
   * 添加地址
   * @param {Object} context Vuex context
   * @param {Object} addressData 地址数据
   * @returns {Promise} 添加的地址
   */
  async addAddress({ commit }, addressData) {
    commit('SET_LOADING', true)
    try {
      const res = await addAddressApi(addressData)
      const newAddress = res.data
      commit('ADD_ADDRESS', newAddress)
      return newAddress
    } catch (error) {
      console.error('添加地址失败:', error)
      // 开发环境使用Mock数据
      if (process.env.NODE_ENV === 'development') {
        const mockAddress = {
          id: Date.now(),
          userId: 1,
          ...addressData,
          createTime: new Date().toISOString()
        }
        commit('ADD_ADDRESS', mockAddress)
        return mockAddress
      }
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  /**
   * 更新地址
   * @param {Object} context Vuex context
   * @param {Object} addressData 地址数据（必须包含id）
   * @returns {Promise} 更新后的地址
   */
  async updateAddress({ commit }, addressData) {
    if (!addressData.id) {
      throw new Error('更新地址必须提供地址ID')
    }
    
    commit('SET_LOADING', true)
    try {
      const res = await updateAddressApi(addressData.id, addressData)
      const updatedAddress = res.data
      commit('UPDATE_ADDRESS', updatedAddress)
      return updatedAddress
    } catch (error) {
      console.error('更新地址失败:', error)
      // 开发环境使用Mock数据
      if (process.env.NODE_ENV === 'development') {
        const updatedAddress = {
          ...addressData,
          updateTime: new Date().toISOString()
        }
        commit('UPDATE_ADDRESS', updatedAddress)
        return updatedAddress
      }
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  /**
   * 删除地址
   * @param {Object} context Vuex context
   * @param {String|Number} addressId 地址ID
   * @returns {Promise} 删除结果
   */
  async deleteAddress({ commit }, addressId) {
    commit('SET_LOADING', true)
    try {
      await deleteAddressApi(addressId)
      commit('REMOVE_ADDRESS', addressId)
      return true
    } catch (error) {
      console.error('删除地址失败:', error)
      // 开发环境使用Mock数据
      if (process.env.NODE_ENV === 'development') {
        commit('REMOVE_ADDRESS', addressId)
        return true
      }
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  /**
   * 设置默认地址
   * @param {Object} context Vuex context
   * @param {String|Number} addressId 地址ID
   * @returns {Promise} 设置结果
   */
  async setDefaultAddress({ commit }, addressId) {
    commit('SET_LOADING', true)
    try {
      await setDefaultAddressApi(addressId)
      commit('SET_DEFAULT_ADDRESS', addressId)
      return true
    } catch (error) {
      console.error('设置默认地址失败:', error)
      // 开发环境使用Mock数据
      if (process.env.NODE_ENV === 'development') {
        commit('SET_DEFAULT_ADDRESS', addressId)
        return true
      }
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  }
}

export default {
  namespaced: true,
  state,
  getters,
  mutations,
  actions
} 