import { 
  login as loginApi, 
  register, 
  logout, 
  getCurrentUser, 
  getUserInfo as getUserInfoApi,
  updateUserInfo, 
  uploadAvatar as uploadAvatarApi,
  uploadCertificationImage as uploadCertificationImageApi,
  changePassword,
  refreshToken as refreshTokenApi,
  resetPassword as resetPasswordApi,
  getAddressList,
  addAddress as addAddressApi,
  updateAddress as updateAddressApi,
  deleteAddress as deleteAddressApi,
  setDefaultAddress as setDefaultAddressApi,
  getUserPreferences as getUserPreferencesApi,
  updateUserPreferences as updateUserPreferencesApi,
  submitShopCertification as submitShopCertificationApi,
  getShopCertificationStatus as getShopCertificationStatusApi,
  // 任务组D：用户互动相关API
  getFollowList,
  addFollow as addFollowApi,
  removeFollow as removeFollowApi,
  getFavoriteList,
  addFavorite as addFavoriteApi,
  removeFavorite as removeFavoriteApi,
  addLike as addLikeApi,
  removeLike as removeLikeApi,
  // 任务组E：管理员用户管理相关API
  getAdminUserList,
  createAdmin as createAdminApi,
  updateUser as updateUserApi,
  deleteUser as deleteUserApi,
  toggleUserStatus as toggleUserStatusApi,
  getCertificationList,
  processCertification as processCertificationApi
} from '@/api/user'
import { useTokenStorage } from '@/composables/useStorage'
import router from '@/router'
import { userPromptMessages } from '@/utils/promptMessages'

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
  defaultAddressId: null,
  // 任务组D：用户互动相关状态
  followList: [], // 关注列表
  favoriteList: [], // 收藏列表
  likeList: [], // 点赞列表（可选，根据需求决定是否存储）
  // 任务组E：管理员用户管理相关状态
  userList: [], // 用户列表
  userPagination: {
    page: 1,
    pageSize: 20,
    total: 0
  },
  userFilters: {
    keyword: '',
    role: null,
    status: null
  },
  certificationList: [] // 商家认证申请列表
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
  },
  
  // 任务组D：用户互动相关mutations
  // 设置关注列表
  SET_FOLLOW_LIST(state, list) {
    state.followList = list || []
  },
  
  // 添加关注
  ADD_FOLLOW(state, follow) {
    state.followList.push(follow)
  },
  
  // 移除关注
  REMOVE_FOLLOW(state, followId) {
    state.followList = state.followList.filter(f => f.id !== followId)
  },
  
  // 设置收藏列表
  SET_FAVORITE_LIST(state, list) {
    state.favoriteList = list || []
  },
  
  // 添加收藏
  ADD_FAVORITE(state, favorite) {
    state.favoriteList.push(favorite)
  },
  
  // 移除收藏
  REMOVE_FAVORITE(state, favoriteId) {
    state.favoriteList = state.favoriteList.filter(f => f.id !== favoriteId)
  },
  
  // 设置点赞列表（可选）
  SET_LIKE_LIST(state, list) {
    state.likeList = list || []
  },
  
  // 添加点赞（可选）
  ADD_LIKE(state, like) {
    state.likeList.push(like)
  },
  
  // 移除点赞（可选）
  REMOVE_LIKE(state, likeId) {
    state.likeList = state.likeList.filter(l => l.id !== likeId)
  },
  
  // 任务组E：管理员用户管理相关mutations
  // 设置用户列表
  SET_USER_LIST(state, list) {
    state.userList = list || []
  },
  
  // 设置分页信息
  SET_USER_PAGINATION(state, pagination) {
    state.userPagination = { ...state.userPagination, ...pagination }
  },
  
  // 设置筛选条件
  SET_USER_FILTERS(state, filters) {
    state.userFilters = { ...state.userFilters, ...filters }
  },
  
  // 更新用户列表中的单个用户
  UPDATE_USER_IN_LIST(state, user) {
    const index = state.userList.findIndex(u => u.id === user.id)
    if (index !== -1) {
      state.userList[index] = { ...state.userList[index], ...user }
    }
  },

  // 从用户列表中移除用户
  REMOVE_USER_FROM_LIST(state, userId) {
    state.userList = state.userList.filter(u => u.id !== userId)
  },
  
  // 设置认证申请列表
  SET_CERTIFICATION_LIST(state, list) {
    state.certificationList = list || []
  },
  
  // 更新认证申请列表中的单个申请
  UPDATE_CERTIFICATION_IN_LIST(state, cert) {
    const index = state.certificationList.findIndex(c => c.id === cert.id)
    if (index !== -1) {
      state.certificationList[index] = { ...state.certificationList[index], ...cert }
    }
  }
}

// 任务B-2：地址字段映射辅助函数（模块级别）
const mapAddressFromBackend = (address) => {
  if (!address) return null
  return {
    id: address.id,
    userId: address.userId,
    name: address.receiverName || address.name,
    phone: address.receiverPhone || address.phone,
    province: address.province,
    city: address.city,
    district: address.district,
    detail: address.detailAddress || address.detail,
    isDefault: address.isDefault === 1 || address.isDefault === true,
    createTime: address.createTime,
    updateTime: address.updateTime
  }
}

const mapAddressToBackend = (address) => {
  if (!address) return null
  return {
    id: address.id,
    userId: address.userId,
    receiverName: address.name,
    receiverPhone: address.phone,
    province: address.province,
    city: address.city,
    district: address.district,
    detailAddress: address.detail,
    isDefault: address.isDefault === true || address.isDefault === 1 ? 1 : 0
  }
}

const actions = {
  // 用户登录
  // 接口#1: 登录 - 成功码:2000, 失败码:2100/2105
  async login({ commit }, loginData) {
    try {
      commit('SET_LOADING', true)
      
      // 调用登录API，返回{code, data}
      const res = await loginApi(loginData)
      console.log('登录API响应:', JSON.stringify(res, null, 2))
      
      // 后端返回格式：{ code, data: { token: string } }
      const token = res.data?.token || res.data?.data?.token || res.token
      
      console.log('提取的token:', {
        hasResData: !!res.data,
        resDataType: typeof res.data,
        resDataKeys: res.data ? Object.keys(res.data) : [],
        tokenFound: !!token,
        tokenType: typeof token,
        tokenValue: token
      })
      
      // 检查token是否存在
      if (!token) {
        console.error('登录响应中未找到token:', res)
        throw new Error('登录响应中未找到token')
      }
      
      // 检查token格式（应该是JWT格式，至少包含两个点）
      const tokenParts = token.split('.')
      console.log('Token调试信息:', {
        tokenLength: token.length,
        tokenPartsCount: tokenParts.length,
        tokenPreview: token.substring(0, 100) + (token.length > 100 ? '...' : ''),
        tokenParts: tokenParts.map((part, index) => ({
          index,
          length: part.length,
          preview: part.substring(0, 20) + (part.length > 20 ? '...' : '')
        }))
      })
      
      if (typeof token !== 'string' || tokenParts.length < 2) {
        console.error('Token格式不正确:', {
          type: typeof token,
          length: token?.length,
          partsCount: tokenParts.length,
          fullToken: token
        })
        throw new Error('Token格式不正确')
      }
      
      // 检查payload部分是否存在
      if (!tokenParts[1] || tokenParts[1].length === 0) {
        console.error('Token payload部分为空:', {
          fullToken: token,
          tokenParts: tokenParts,
          responseData: res.data
        })
        throw new Error('Token payload部分为空，可能是Mock服务器返回的token格式不正确')
      }
      
      // 存储token
      tokenStorage.setToken(token)
      
      // 从token中解析用户信息
      const userInfo = tokenStorage.verifyToken()
      if (!userInfo) {
        console.error('Token解析失败，完整token内容:', token)
        console.error('响应数据:', res)
        throw new Error('Token解析失败')
      }
      
      // 更新状态
      commit('SET_USER_INFO', userInfo)
      commit('SET_LOGGED_IN', true)
      
      return res // 返回 {code, data}，组件调用showByCode(res.code)
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // 用户注册
  // 接口#2: 注册 - 成功码:2001, 失败码:2101
  async register({ commit }, registerData) {
    try {
      commit('SET_LOADING', true)
      
      // 调用注册API
      const res = await register(registerData)
      
      return res // 返回 {code, data}，组件调用showByCode(res.code)
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
      const res = await getUserPreferencesApi()
      // 兼容：后端可能返回{ data: {...} } 或直接返回{...}
      const preferences = res?.data || res?.preferences || res
      commit('SET_PREFERENCES', preferences)
      return res // 返回 {code, data}，组件调用showByCode(res.code)
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
      const res = await updateUserPreferencesApi(preferences)
      // 以服务端回写为准（若后端仅返回success，则直接使用入参）
      const saved = res?.data || res?.preferences || res || preferences
      commit('SET_PREFERENCES', saved)
      return res // 返回 {code, data}，组件调用showByCode(res.code)
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // 退出登录
  // 接口#3: 登出 - 成功码:2002
  async logout({ commit }) {
    try {
      commit('SET_LOADING', true)
      
      // 调用登出API
      const res = await logout()
      
      // 清除token和用户信息
      tokenStorage.removeToken()
      commit('CLEAR_USER')
      
      return res // 返回 {code, data}，组件调用showByCode(res.code)
    } catch (error) {
      console.error('退出登录失败', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // 初始化用户信息
  // async initAuth({ commit, state }) {
  //   // 如果已经有用户信息，不再重复初始化
  //   if (state.userInfo) {
  //     return state.userInfo
  //   }
  //   
  //   // 获取token
  //   const token = tokenStorage.getToken()
  //   
  //   // 没有token，则未登录
  //   if (!token) {
  //     return null
  //   }
  //   
  //   // 检查token有效性
  //   if (!tokenStorage.verifyToken()) {
  //     tokenStorage.removeToken()
  //     return null
  //   }
  //   
  //   try {
  //     commit('SET_LOADING', true)
  //     
  //     // 从服务器获取最新用户信息
  //     const userInfo = await getCurrentUser()
  //     
  //     // 更新状态
  //     commit('SET_USER_INFO', userInfo)
  //     commit('SET_LOGGED_IN', true)
  //     
  //     return userInfo
  //   } catch (error) {
  //     console.error('初始化用户信息失败', error)
  //     tokenStorage.removeToken()
  //     commit('CLEAR_USER')
  //     return null
  //   } finally {
  //     commit('SET_LOADING', false)
  //   }
  // },
  
  // 获取用户信息
  // 任务A-2：使用真实API获取用户信息
  async getUserInfo({ commit, state }, userId = null) {
    try {
      commit('SET_LOADING', true)
      
      // 调用API获取用户信息
      const res = await getUserInfoApi(userId)
      
      // 更新状态（如果获取的是当前用户信息）
      if (!userId) {
        const userInfo = res?.data || res
        commit('SET_USER_INFO', userInfo)
        commit('SET_LOGGED_IN', true)
      }
      
      return res // 返回 {code, data}，组件调用showByCode(res.code)
    } catch (error) {
      console.error('获取用户信息失败:', error)
      // 只有在认证错误（401/403）时才清除登录状态
      // 其他错误（网络错误、服务器错误等）不应该清除用户信息
      if (!userId) {
        // 检查是否是认证错误
        const isAuthError = error?.response?.status === 401 || 
                           error?.response?.status === 403 ||
                           error?.code === 401 ||
                           error?.code === 403 ||
                           error?.message?.includes('认证') ||
                           error?.message?.includes('401') ||
                           error?.message?.includes('403')
        
        if (isAuthError) {
          tokenStorage.removeToken()
          commit('CLEAR_USER')
        }
      }
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // 更新用户信息
  // 接口#6: 更新个人资料 - 成功码:2010, 失败码:2110
  async updateUserInfo({ commit, state }, newUserInfo) {
    if (!state.userInfo) {
      throw new Error('用户未登录')
    }
    
    try {
      commit('SET_LOADING', true)
      
      // 调用更新API
      const res = await updateUserInfo(newUserInfo)
      
      // 更新状态
      commit('SET_USER_INFO', res.data || res)
      
      return res // 返回 {code, data}，组件调用showByCode(res.code)
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // 上传头像
  // 任务A-3：实现uploadAvatar action
  async uploadAvatar({ commit, state }, file) {
    if (!state.userInfo) {
      // TODO: 迁移到新消息系统 - 使用 showByCode(response.code)
      console.warn('Token已过期')
      return false
    }
    
    if (!file || !(file instanceof File)) {
      // TODO: 迁移到新消息系统 - 使用 showByCode(response.code)
      userPromptMessages.showFormIncomplete()
      return false
    }
    
    try {
      commit('SET_LOADING', true)
      
      // 调用上传头像API
      const res = await uploadAvatarApi(file)
      const avatarUrl = res?.data?.avatarUrl || res?.avatarUrl || res?.data
      
      if (!avatarUrl) {
        throw new Error('上传失败：未返回头像URL')
      }
      
      // 更新用户信息中的头像URL
      const updatedUserInfo = {
        ...state.userInfo,
        avatar: avatarUrl
      }
      commit('SET_USER_INFO', updatedUserInfo)
      
      return res // 返回 {code, data}，组件调用showByCode(res.code)
    } catch (error) {
      // 显示上传失败消息
      // TODO: 迁移到新消息系统 - 使用 showByCode(response.code)

      // TODO: 迁移到新消息系统 - 使用 showByCode(response.code)

      userPromptMessages.error.showProfileUpdateFailure(error.message || '头像上传失败')
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
        // TODO: 迁移到新消息系统 - 使用 showByCode(response.code)

        userPromptMessages.error.showPasswordMismatch()
        return false
      }
      
      // 调用修改密码API
      await changePassword(passwordData)
      
      // 显示修改成功消息
      // TODO: 迁移到新消息系统 - 使用 showByCode(response.code)

      // TODO: 迁移到新消息系统 - 使用 showByCode(response.code)

      userPromptMessages.success.showPasswordChangeSuccess()
      
      return true
    } catch (error) {
      // 显示修改失败消息
      // TODO: 迁移到新消息系统 - 使用 showByCode(response.code)

      // TODO: 迁移到新消息系统 - 使用 showByCode(response.code)

      userPromptMessages.error.showPasswordChangeFailure(error.message)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // 刷新Token
  // 任务0-3：使用真实API请求
  async refreshToken({ commit }) {
    try {
      // 调用刷新Token API
      const response = await refreshTokenApi()
      // 后端返回格式：{ token: string }
      const { token } = response
      
      // 更新token
      tokenStorage.setToken(token)
      
      // 从token中解析用户信息
      const userInfo = tokenStorage.verifyToken()
      if (userInfo) {
        commit('SET_USER_INFO', userInfo)
        commit('SET_LOGGED_IN', true)
      }
      
      return { token, userInfo }
    } catch (error) {
      console.error('刷新Token失败:', error)
      // Token刷新失败，清除用户信息
      tokenStorage.removeToken()
      commit('CLEAR_USER')
      throw error
    }
  },
  
  // 密码找回
  // 任务0-4：实现findPassword action（密码找回）
  async findPassword({ commit }, resetData) {
    try {
      commit('SET_LOADING', true)
      
      // 调用密码找回API
      const result = await resetPasswordApi(resetData)
      
      // 显示找回成功消息
      // TODO: 迁移到新消息系统 - 使用 showByCode(response.code)

      userPromptMessages.success.showPasswordResetSuccess()
      
      return result
    } catch (error) {
      // 显示找回失败消息
      // TODO: 迁移到新消息系统 - 使用 showByCode(response.code)

      userPromptMessages.error.showPasswordResetFailure(error.message)
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
    console.warn('Token已过期')
  },
  
  // 处理权限拒绝
  handlePermissionDenied() {
    // 显示权限拒绝消息
    console.warn('权限不足')
  },
  
  // 处理认证错误
  handleAuthError({ commit }) {
    // 清除token和用户信息
    tokenStorage.removeToken()
    commit('CLEAR_USER')
    
    // 显示认证错误消息
    console.warn('路由认证错误')
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
      // 映射地址数据用于更新状态
      const addressList = (res.data || []).map(mapAddressFromBackend)
      commit('SET_ADDRESS_LIST', addressList)
      return res // 返回 {code, data}，组件调用showByCode(res.code)
    } catch (error) {
      console.error('获取地址列表失败:', error)
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
      // 任务B-2：将前端字段转换为后端字段
      const backendData = mapAddressToBackend(addressData)
      const res = await addAddressApi(backendData)
      // 映射地址数据用于更新状态
      const newAddress = mapAddressFromBackend(res.data)
      commit('ADD_ADDRESS', newAddress)
      return res // 返回 {code, data}，组件调用showByCode(res.code)
    } catch (error) {
      console.error('添加地址失败:', error)
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
      // 任务B-2：将前端字段转换为后端字段
      const backendData = mapAddressToBackend(addressData)
      const res = await updateAddressApi(addressData.id, backendData)
      // 映射地址数据用于更新状态
      const updatedAddress = mapAddressFromBackend(res.data)
      commit('UPDATE_ADDRESS', updatedAddress)
      return res // 返回 {code, data}，组件调用showByCode(res.code)
    } catch (error) {
      console.error('更新地址失败:', error)
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
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // ========== 任务组D：用户互动功能actions ==========
  
  /**
   * 获取关注列表
   * @param {Object} context Vuex context
   * @param {String} type 关注类型（user/shop），可选
   * @returns {Promise} 关注列表
   */
  async fetchFollowList({ commit }, type = null) {
    commit('SET_LOADING', true)
    try {
      const res = await getFollowList(type)
      // 更新状态使用 res.data
      const followList = res.data || res || []
      commit('SET_FOLLOW_LIST', followList)
      return res // 返回 {code, data}，组件调用showByCode(res.code)
    } catch (error) {
      console.error('获取关注列表失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  /**
   * 添加关注
   * @param {Object} context Vuex context
   * @param {Object} followData 关注数据
   * @returns {Promise} 关注结果
   */
  async addFollow({ commit }, followData) {
    commit('SET_LOADING', true)
    try {
      const res = await addFollowApi(followData)
      // 更新状态使用 res.data
      const follow = res.data || res
      commit('ADD_FOLLOW', follow)
      return res // 返回 {code, data}，组件调用showByCode(res.code)
    } catch (error) {
      console.error('添加关注失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  /**
   * 取消关注
   * @param {Object} context Vuex context
   * @param {String|Number} followId 关注ID
   * @returns {Promise} 删除结果
   */
  async removeFollow({ commit }, followId) {
    commit('SET_LOADING', true)
    try {
      await removeFollowApi(followId)
      commit('REMOVE_FOLLOW', followId)
      return true
    } catch (error) {
      console.error('取消关注失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  /**
   * 获取收藏列表
   * @param {Object} context Vuex context
   * @param {String} type 收藏类型（tea/post/article），可选
   * @returns {Promise} 收藏列表
   */
  async fetchFavoriteList({ commit }, type = null) {
    commit('SET_LOADING', true)
    try {
      const res = await getFavoriteList(type)
      // 更新状态使用 res.data
      const favoriteList = res.data || res || []
      commit('SET_FAVORITE_LIST', favoriteList)
      return res // 返回 {code, data}，组件调用showByCode(res.code)
    } catch (error) {
      console.error('获取收藏列表失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  /**
   * 添加收藏
   * @param {Object} context Vuex context
   * @param {Object} favoriteData 收藏数据
   * @returns {Promise} 收藏结果
   */
  async addFavorite({ commit }, favoriteData) {
    commit('SET_LOADING', true)
    try {
      const res = await addFavoriteApi(favoriteData)
      // 更新状态使用 res.data
      const favorite = res.data || res
      commit('ADD_FAVORITE', favorite)
      return res // 返回 {code, data}，组件调用showByCode(res.code)
    } catch (error) {
      console.error('添加收藏失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  /**
   * 取消收藏
   * @param {Object} context Vuex context
   * @param {String|Number} favoriteId 收藏ID
   * @returns {Promise} 删除结果
   */
  async removeFavorite({ commit }, favoriteId) {
    commit('SET_LOADING', true)
    try {
      await removeFavoriteApi(favoriteId)
      commit('REMOVE_FAVORITE', favoriteId)
      return true
    } catch (error) {
      console.error('取消收藏失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  /**
   * 点赞
   * @param {Object} context Vuex context
   * @param {Object} likeData 点赞数据
   * @returns {Promise} 点赞结果
   */
  async addLike({ commit }, likeData) {
    commit('SET_LOADING', true)
    try {
      const res = await addLikeApi(likeData)
      // 更新状态使用 res.data
      const like = res.data || res
      commit('ADD_LIKE', like)
      return res // 返回 {code, data}，组件调用showByCode(res.code)
    } catch (error) {
      console.error('点赞失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  /**
   * 取消点赞
   * @param {Object} context Vuex context
   * @param {String|Number} likeId 点赞ID
   * @returns {Promise} 删除结果
   */
  async removeLike({ commit }, likeId) {
    commit('SET_LOADING', true)
    try {
      await removeLikeApi(likeId)
      commit('REMOVE_LIKE', likeId)
      return true
    } catch (error) {
      console.error('取消点赞失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // ========== 任务组E：管理员用户管理actions ==========
  
  /**
   * 获取用户列表（管理员）
   * @param {Object} context Vuex context
   * @param {Object} params 查询参数
   * @returns {Promise} 用户列表
   */
  async fetchUserList({ commit }, params = {}) {
    commit('SET_LOADING', true)
    try {
      const res = await getAdminUserList(params)
      // 更新状态使用 res.data
      const userList = res?.data?.list || res?.list || res?.data || res || []
      const total = res?.data?.total || res?.total || userList.length
      commit('SET_USER_LIST', userList)
      commit('SET_USER_PAGINATION', { 
        page: params.page || 1, 
        pageSize: params.pageSize || 20, 
        total 
      })
      return res // 返回 {code, data}，组件调用showByCode(res.code)
    } catch (error) {
      console.error('获取用户列表失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  /**
   * 切换用户状态（管理员）
   * @param {Object} context Vuex context
   * @param {Object} data { userId, status }
   * @returns {Promise} 更新结果
   */
  async toggleUserStatus({ commit }, { userId, status }) {
    commit('SET_LOADING', true)
    try {
      await toggleUserStatusApi(userId, status)
      commit('UPDATE_USER_IN_LIST', { id: userId, status })
      return true
    } catch (error) {
      console.error('切换用户状态失败', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  /**
   * 获取商家认证申请列表（管理员）
   * @param {Object} context Vuex context
   * @param {Object} params 查询参数
   * @returns {Promise} 认证申请列表
   */
  async fetchCertificationList({ commit }, params = {}) {
    commit('SET_LOADING', true)
    try {
      const res = await getCertificationList(params)
      // 更新状态使用 res.data
      const certList = res?.data?.list || res?.list || res?.data || res || []
      commit('SET_CERTIFICATION_LIST', certList)
      return res // 返回 {code, data}，组件调用showByCode(res.code)
    } catch (error) {
      console.error('获取认证申请列表失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  /**
   * 处理商家认证申请（管理员）
   * @param {Object} context Vuex context
   * @param {Object} data { certId, action, message }
   * @returns {Promise} 处理结果
   */
  async processCertification({ commit }, { certId, action, message }) {
    commit('SET_LOADING', true)
    try {
      await processCertificationApi(certId, { action, message })
      // 更新本地状态
      const status = action === 'approve' ? 2 : 1 // 2-已通过, 1-已拒绝
      commit('UPDATE_CERTIFICATION_IN_LIST', { id: certId, status })
      return true
    } catch (error) {
      console.error('处理认证申请失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  /**
   * 创建管理员账号（仅管理员）
   * @param {Object} context Vuex context
   * @param {Object} adminData 管理员数据
   * @returns {Promise} 创建结果
   */
  async createAdmin({ commit }, adminData) {
    commit('SET_LOADING', true)
    try {
      const result = await createAdminApi(adminData)
      // 创建成功后刷新用户列表
      return result
    } catch (error) {
      console.error('创建管理员失败', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  /**
   * 更新用户信息（仅管理员）
   * @param {Object} context Vuex context
   * @param {Object} data { userId, userData }
   * @returns {Promise} 更新结果
   */
  async updateUser({ commit }, { userId, userData }) {
    commit('SET_LOADING', true)
    try {
      await updateUserApi(userId, userData)
      commit('UPDATE_USER_IN_LIST', { id: userId, ...userData })
      return true
    } catch (error) {
      console.error('更新用户失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  /**
   * 删除用户（仅管理员）
   * @param {Object} context Vuex context
   * @param {String} userId 用户ID
   * @returns {Promise} 删除结果
   */
  async deleteUser({ commit }, userId) {
    commit('SET_LOADING', true)
    try {
      await deleteUserApi(userId)
      commit('REMOVE_USER_FROM_LIST', userId)
      return true
    } catch (error) {
      console.error('删除用户失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  /**
   * 上传商家认证图片
   * @param {Object} context Vuex context
   * @param {Object} payload { file, type }
   * @returns {Promise} 上传结果
   */
  async uploadCertificationImage({ commit }, { file, type }) {
    commit('SET_LOADING', true)
    try {
      const res = await uploadCertificationImageApi(file, type)
      return res // 返回 {code, data}
    } catch (error) {
      console.error('上传认证图片失败:', error)
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
