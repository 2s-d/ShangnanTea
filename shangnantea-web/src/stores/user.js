import { defineStore } from 'pinia'
import { ref, computed, reactive } from 'vue'
import { 
  login as loginApi, 
  register as registerApi, 
  logout as logoutApi, 
  getCurrentUser as getCurrentUserApi, 
  getUserInfo as getUserInfoApi,
  updateUserInfo as updateUserInfoApi, 
  uploadAvatar as uploadAvatarApi,
  uploadCertificationImage as uploadCertificationImageApi,
  changePassword as changePasswordApi,
  refreshToken as refreshTokenApi,
  resetPassword as resetPasswordApi,
  sendVerificationCode as sendVerificationCodeApi,
  getAddressList,
  addAddress as addAddressApi,
  updateAddress as updateAddressApi,
  deleteAddress as deleteAddressApi,
  setDefaultAddress as setDefaultAddressApi,
  getUserPreferences as getUserPreferencesApi,
  updateUserPreferences as updateUserPreferencesApi,
  submitShopCertification as submitShopCertificationApi,
  getShopCertificationStatus as getShopCertificationStatusApi,
  getFollowList,
  addFollow as addFollowApi,
  removeFollow as removeFollowApi,
  getFavoriteList,
  addFavorite as addFavoriteApi,
  removeFavorite as removeFavoriteApi,
  addLike as addLikeApi,
  removeLike as removeLikeApi,
  getAdminUserList,
  createAdmin as createAdminApi,
  updateUser as updateUserApi,
  deleteUser as deleteUserApi,
  toggleUserStatus as toggleUserStatusApi,
  getCertificationList,
  processCertification as processCertificationApi
} from '@/api/user'
import { useTokenStorage } from '@/composables/useStorage'
import { userPromptMessages } from '@/utils/promptMessages'

// 地址字段映射辅助函数
const mapAddressFromBackend = address => {
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

const mapAddressToBackend = address => {
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

export const useUserStore = defineStore('user', () => {
  // 创建token存储实例
  const tokenStorage = useTokenStorage()
  
  // ========== State ==========
  const userInfo = ref(null)
  const isLoggedIn = ref(false)
  const loading = ref(false)
  
  // 用户偏好设置
  const preferences = reactive({
    theme: 'light',
    primaryColor: '#409EFF',
    fontSize: 14,
    fontFamily: '',
    enableAnimation: true,
    listMode: 'grid',
    pageSize: 20
  })
  
  // 地址相关状态
  const addressList = ref([])
  const defaultAddressId = ref(null)
  
  // 用户互动相关状态
  const followList = ref([])
  const favoriteList = ref([])
  const likeList = ref([])
  
  // 管理员用户管理相关状态
  const userList = ref([])
  const userPagination = reactive({
    page: 1,
    pageSize: 20,
    total: 0
  })
  const userFilters = reactive({
    keyword: '',
    role: null,
    status: null
  })
  const certificationList = ref([])
  
  // ========== Getters ==========
  const userRole = computed(() => userInfo.value?.role || 0)
  const isAdmin = computed(() => userInfo.value?.role === 1)
  const isUser = computed(() => userInfo.value?.role === 2)
  const isShop = computed(() => userInfo.value?.role === 3)
  const defaultAddress = computed(() => {
    if (defaultAddressId.value) {
      return addressList.value.find(addr => addr.id === defaultAddressId.value) || null
    }
    return addressList.value.find(addr => addr.isDefault) || null
  })
  
  // ========== Actions ==========
  
  // 用户登录
  async function login(loginData) {
    try {
      loading.value = true
      
      const res = await loginApi(loginData)
      console.log('登录API响应:', JSON.stringify(res, null, 2))
      
      const token = res.data?.token || res.data?.data?.token || res.token
      
      console.log('提取的token:', {
        hasResData: !!res.data,
        resDataType: typeof res.data,
        resDataKeys: res.data ? Object.keys(res.data) : [],
        tokenFound: !!token,
        tokenType: typeof token,
        tokenValue: token
      })
      
      if (!token) {
        console.error('登录响应中未找到token:', res)
        throw new Error('登录响应中未找到token')
      }
      
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
      
      if (!tokenParts[1] || tokenParts[1].length === 0) {
        console.error('Token payload部分为空:', {
          fullToken: token,
          tokenParts: tokenParts,
          responseData: res.data
        })
        throw new Error('Token payload部分为空，可能是Mock服务器返回的token格式不正确')
      }
      
      tokenStorage.setToken(token)
      
      const userInfoData = tokenStorage.verifyToken()
      if (!userInfoData) {
        console.error('Token解析失败，完整token内容:', token)
        console.error('响应数据:', res)
        throw new Error('Token解析失败')
      }
      
      userInfo.value = userInfoData
      isLoggedIn.value = true
      
      return res
    } finally {
      loading.value = false
    }
  }
  
  // 用户注册
  async function register(registerData) {
    try {
      loading.value = true
      const res = await registerApi(registerData)
      return res
    } finally {
      loading.value = false
    }
  }
  
  // 提交商家认证申请
  async function submitShopCertification(certificationData) {
    loading.value = true
    try {
      return await submitShopCertificationApi(certificationData)
    } finally {
      loading.value = false
    }
  }
  
  // 获取商家认证状态
  async function fetchShopCertificationStatus() {
    loading.value = true
    try {
      return await getShopCertificationStatusApi()
    } finally {
      loading.value = false
    }
  }
  
  // 获取用户偏好设置
  async function fetchUserPreferences() {
    loading.value = true
    try {
      const res = await getUserPreferencesApi()
      const prefs = res?.data || res?.preferences || res
      Object.assign(preferences, prefs || {})
      return res
    } finally {
      loading.value = false
    }
  }
  
  // 保存用户偏好设置
  async function saveUserPreferences(prefs) {
    loading.value = true
    try {
      const res = await updateUserPreferencesApi(prefs)
      const saved = res?.data || res?.preferences || res || prefs
      Object.assign(preferences, saved)
      return res
    } finally {
      loading.value = false
    }
  }
  
  // 退出登录
  async function logout() {
    try {
      loading.value = true
      const res = await logoutApi()
      tokenStorage.removeToken()
      userInfo.value = null
      isLoggedIn.value = false
      return res
    } catch (error) {
      console.error('退出登录失败', error)
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 获取用户信息
  async function getUserInfo(userId = null) {
    try {
      loading.value = true
      const res = userId ? await getUserInfoApi(userId) : await getCurrentUserApi()
      
      if (!userId) {
        const userInfoData = res?.data || res
        userInfo.value = userInfoData
        isLoggedIn.value = true
      }
      
      return res
    } catch (error) {
      console.error('获取用户信息失败:', error)
      if (!userId) {
        const isAuthError = error?.response?.status === 401 || 
                           error?.response?.status === 403 ||
                           error?.code === 401 ||
                           error?.code === 403 ||
                           error?.message?.includes('认证') ||
                           error?.message?.includes('401') ||
                           error?.message?.includes('403')
        
        if (isAuthError) {
          tokenStorage.removeToken()
          userInfo.value = null
          isLoggedIn.value = false
        }
      }
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 更新用户信息
  async function updateUserInfo(newUserInfo) {
    if (!userInfo.value) {
      throw new Error('用户未登录')
    }
    
    try {
      loading.value = true
      const res = await updateUserInfoApi(newUserInfo)
      userInfo.value = res.data || res
      return res
    } finally {
      loading.value = false
    }
  }
  
  // 上传头像
  async function uploadAvatar(file) {
    if (!userInfo.value) {
      console.warn('Token已过期')
      return false
    }
    
    if (!file || !(file instanceof File)) {
      userPromptMessages.showFormIncomplete()
      return false
    }
    
    try {
      loading.value = true
      const res = await uploadAvatarApi(file)
      const avatarUrl = res?.data?.avatarUrl || res?.avatarUrl || res?.data
      
      if (!avatarUrl) {
        throw new Error('上传失败：未返回头像URL')
      }
      
      userInfo.value = {
        ...userInfo.value,
        avatar: avatarUrl
      }
      
      return res
    } catch (error) {
      userPromptMessages.error.showProfileUpdateFailure(error.message || '头像上传失败')
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 修改密码
  async function changePassword(passwordData) {
    try {
      loading.value = true
      
      if (passwordData.newPassword !== passwordData.confirmPassword) {
        userPromptMessages.error.showPasswordMismatch()
        return false
      }
      
      await changePasswordApi(passwordData)
      userPromptMessages.success.showPasswordChangeSuccess()
      
      return true
    } catch (error) {
      userPromptMessages.error.showPasswordChangeFailure(error.message)
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 刷新Token
  async function refreshToken() {
    try {
      const response = await refreshTokenApi()
      const { token } = response
      
      tokenStorage.setToken(token)
      
      const userInfoData = tokenStorage.verifyToken()
      if (userInfoData) {
        userInfo.value = userInfoData
        isLoggedIn.value = true
      }
      
      return { token, userInfo: userInfoData }
    } catch (error) {
      console.error('刷新Token失败:', error)
      tokenStorage.removeToken()
      userInfo.value = null
      isLoggedIn.value = false
      throw error
    }
  }
  
  // 密码找回
  async function findPassword(resetData) {
    try {
      loading.value = true
      const res = await resetPasswordApi(resetData)
      return res
    } finally {
      loading.value = false
    }
  }
  
  // 处理会话过期
  function handleSessionExpired() {
    tokenStorage.removeToken()
    userInfo.value = null
    isLoggedIn.value = false
    console.warn('Token已过期')
  }
  
  // 处理权限拒绝
  function handlePermissionDenied() {
    console.warn('权限不足')
  }
  
  // 处理认证错误
  function handleAuthError() {
    tokenStorage.removeToken()
    userInfo.value = null
    isLoggedIn.value = false
    console.warn('路由认证错误')
  }
  
  // ========== 地址相关 Actions ==========
  
  // 获取地址列表
  async function fetchAddresses() {
    loading.value = true
    try {
      const res = await getAddressList()
      const list = (res.data || []).map(mapAddressFromBackend)
      addressList.value = list
      const defaultAddr = list.find(addr => addr.isDefault)
      defaultAddressId.value = defaultAddr?.id || null
      return res
    } catch (error) {
      console.error('获取地址列表失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 添加地址
  async function addAddress(addressData) {
    loading.value = true
    try {
      const backendData = mapAddressToBackend(addressData)
      const res = await addAddressApi(backendData)
      const newAddress = mapAddressFromBackend(res.data)
      addressList.value.push(newAddress)
      
      if (newAddress.isDefault) {
        addressList.value.forEach(addr => {
          if (addr.id !== newAddress.id) {
            addr.isDefault = false
          }
        })
        defaultAddressId.value = newAddress.id
      }
      
      return res
    } catch (error) {
      console.error('添加地址失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 更新地址
  async function updateAddress(addressData) {
    if (!addressData.id) {
      throw new Error('更新地址必须提供地址ID')
    }
    
    loading.value = true
    try {
      const backendData = mapAddressToBackend(addressData)
      const res = await updateAddressApi(addressData.id, backendData)
      const updatedAddress = mapAddressFromBackend(res.data)
      
      const index = addressList.value.findIndex(addr => addr.id === updatedAddress.id)
      if (index !== -1) {
        addressList.value.splice(index, 1, updatedAddress)
        
        if (updatedAddress.isDefault) {
          addressList.value.forEach(addr => {
            if (addr.id !== updatedAddress.id) {
              addr.isDefault = false
            }
          })
          defaultAddressId.value = updatedAddress.id
        } else if (defaultAddressId.value === updatedAddress.id) {
          defaultAddressId.value = null
        }
      }
      
      return res
    } catch (error) {
      console.error('更新地址失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 删除地址
  async function deleteAddress(addressId) {
    loading.value = true
    try {
      await deleteAddressApi(addressId)
      addressList.value = addressList.value.filter(addr => addr.id !== addressId)
      if (defaultAddressId.value === addressId) {
        defaultAddressId.value = null
      }
      return true
    } catch (error) {
      console.error('删除地址失败:', error)
      if (process.env.NODE_ENV === 'development') {
        addressList.value = addressList.value.filter(addr => addr.id !== addressId)
        if (defaultAddressId.value === addressId) {
          defaultAddressId.value = null
        }
        return true
      }
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 设置默认地址
  async function setDefaultAddress(addressId) {
    loading.value = true
    try {
      await setDefaultAddressApi(addressId)
      addressList.value.forEach(addr => {
        addr.isDefault = addr.id === addressId
      })
      defaultAddressId.value = addressId
      return true
    } catch (error) {
      console.error('设置默认地址失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // ========== 用户互动功能 Actions ==========
  
  // 获取关注列表
  async function fetchFollowList(type = null) {
    loading.value = true
    try {
      const res = await getFollowList(type)
      followList.value = res.data || res || []
      return res
    } catch (error) {
      console.error('获取关注列表失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 添加关注
  async function addFollow(followData) {
    loading.value = true
    try {
      const res = await addFollowApi(followData)
      const follow = res.data || res
      followList.value.push(follow)
      return res
    } catch (error) {
      console.error('添加关注失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 取消关注
  async function removeFollow(followData) {
    loading.value = true
    try {
      const res = await removeFollowApi(followData)
      const followItem = followList.value.find(item => 
        item.targetType === followData.targetType && item.targetId === followData.targetId
      )
      if (followItem && followItem.id) {
        followList.value = followList.value.filter(f => f.id !== followItem.id)
      }
      return res
    } catch (error) {
      console.error('取消关注失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 获取收藏列表
  async function fetchFavoriteList(type = null) {
    loading.value = true
    try {
      const res = await getFavoriteList(type)
      favoriteList.value = res.data || res || []
      return res
    } catch (error) {
      console.error('获取收藏列表失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 添加收藏
  async function addFavorite(favoriteData) {
    loading.value = true
    try {
      const res = await addFavoriteApi(favoriteData)
      const favorite = res.data || res
      favoriteList.value.push(favorite)
      return res
    } catch (error) {
      console.error('添加收藏失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 取消收藏
  async function removeFavorite(favoriteData) {
    loading.value = true
    try {
      const res = await removeFavoriteApi(favoriteData)
      const favoriteItem = favoriteList.value.find(item => 
        item.itemType === favoriteData.itemType && item.itemId === favoriteData.itemId
      )
      if (favoriteItem && favoriteItem.id) {
        favoriteList.value = favoriteList.value.filter(f => f.id !== favoriteItem.id)
      }
      return res
    } catch (error) {
      console.error('取消收藏失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 点赞
  async function addLike(likeData) {
    loading.value = true
    try {
      const res = await addLikeApi(likeData)
      const like = res.data || res
      likeList.value.push(like)
      return res
    } catch (error) {
      console.error('点赞失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 取消点赞
  async function removeLike(likeData) {
    loading.value = true
    try {
      const res = await removeLikeApi(likeData)
      const likeItem = likeList.value.find(item => 
        item.targetType === likeData.targetType && item.targetId === likeData.targetId
      )
      if (likeItem && likeItem.id) {
        likeList.value = likeList.value.filter(l => l.id !== likeItem.id)
      }
      return res
    } catch (error) {
      console.error('取消点赞失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // ========== 管理员用户管理 Actions ==========
  
  // 获取用户列表（管理员）
  async function fetchUserList(params = {}) {
    loading.value = true
    try {
      const res = await getAdminUserList(params)
      const list = res?.data?.list || res?.list || res?.data || res || []
      const total = res?.data?.total || res?.total || list.length
      userList.value = list
      Object.assign(userPagination, { 
        page: params.page || 1, 
        pageSize: params.pageSize || 20, 
        total 
      })
      return res
    } catch (error) {
      console.error('获取用户列表失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 切换用户状态（管理员）
  async function toggleUserStatus({ userId, status }) {
    loading.value = true
    try {
      const res = await toggleUserStatusApi(userId, status)
      const index = userList.value.findIndex(u => u.id === userId)
      if (index !== -1) {
        userList.value[index] = { ...userList.value[index], status }
      }
      return res
    } catch (error) {
      console.error('切换用户状态失败', error)
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 获取商家认证申请列表（管理员）
  async function fetchCertificationList(params = {}) {
    loading.value = true
    try {
      const res = await getCertificationList(params)
      certificationList.value = res?.data?.list || res?.list || res?.data || res || []
      return res
    } catch (error) {
      console.error('获取认证申请列表失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 处理商家认证申请（管理员）
  async function processCertification({ certId, action, message }) {
    loading.value = true
    try {
      await processCertificationApi(certId, { action, message })
      const status = action === 'approve' ? 2 : 1
      const index = certificationList.value.findIndex(c => c.id === certId)
      if (index !== -1) {
        certificationList.value[index] = { ...certificationList.value[index], status }
      }
      return true
    } catch (error) {
      console.error('处理认证申请失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 创建管理员账号（仅管理员）
  async function createAdmin(adminData) {
    loading.value = true
    try {
      const result = await createAdminApi(adminData)
      return result
    } catch (error) {
      console.error('创建管理员失败', error)
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 更新用户信息（仅管理员）
  async function updateUser({ userId, userData }) {
    loading.value = true
    try {
      const res = await updateUserApi(userId, userData)
      const index = userList.value.findIndex(u => u.id === userId)
      if (index !== -1) {
        userList.value[index] = { ...userList.value[index], ...userData }
      }
      return res
    } catch (error) {
      console.error('更新用户失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 删除用户（仅管理员）
  async function deleteUser(userId) {
    loading.value = true
    try {
      const res = await deleteUserApi(userId)
      userList.value = userList.value.filter(u => u.id !== userId)
      return res
    } catch (error) {
      console.error('删除用户失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 上传商家认证图片
  async function uploadCertificationImage({ file, type }) {
    loading.value = true
    try {
      const res = await uploadCertificationImageApi(file, type)
      return res
    } catch (error) {
      console.error('上传认证图片失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 发送验证码
  async function sendVerificationCode(data) {
    loading.value = true
    try {
      const res = await sendVerificationCodeApi(data)
      return res
    } catch (error) {
      console.error('发送验证码失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }
  
  return {
    // State
    userInfo,
    isLoggedIn,
    loading,
    preferences,
    addressList,
    defaultAddressId,
    followList,
    favoriteList,
    likeList,
    userList,
    userPagination,
    userFilters,
    certificationList,
    // Getters
    userRole,
    isAdmin,
    isUser,
    isShop,
    defaultAddress,
    // Actions
    login,
    register,
    submitShopCertification,
    fetchShopCertificationStatus,
    fetchUserPreferences,
    saveUserPreferences,
    logout,
    getUserInfo,
    updateUserInfo,
    uploadAvatar,
    changePassword,
    refreshToken,
    findPassword,
    handleSessionExpired,
    handlePermissionDenied,
    handleAuthError,
    fetchAddresses,
    addAddress,
    updateAddress,
    deleteAddress,
    setDefaultAddress,
    fetchFollowList,
    addFollow,
    removeFollow,
    fetchFavoriteList,
    addFavorite,
    removeFavorite,
    addLike,
    removeLike,
    fetchUserList,
    toggleUserStatus,
    fetchCertificationList,
    processCertification,
    createAdmin,
    updateUser,
    deleteUser,
    uploadCertificationImage,
    sendVerificationCode
  }
})
