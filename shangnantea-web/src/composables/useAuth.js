/**
 * 用户认证与权限相关操作的组合式函数 - 整合前后端认证
 */
import { computed, ref } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
// 避免直接导入UI组件，改为使用封装的message组件
import { message } from '@/components/common'
import { useTokenStorage } from '@/composables/useStorage' // 直接使用useTokenStorage
import { handleAsyncOperation, STANDARD_MESSAGES } from '@/utils/messageHelper'

// 角色常量 - 确保与后端一致 (从permission.js移入)
export const ROLES = {
  ADMIN: 1,  // 管理员
  USER: 2,   // 普通用户
  SHOP: 3    // 商家
}

/**
 * 使用认证与权限功能
 * @returns {Object} 认证与权限相关方法和状态
 */
export function useAuth() {
  const store = useStore()
  const router = useRouter()
  const tokenStorage = useTokenStorage() // 使用Token存储hooks
  
  // 加载状态
  const loading = ref(false)
  
  // 从tokenStorage获取方法
  const { 
    getToken, 
    setToken,
    removeToken,
    verifyToken
  } = tokenStorage
  
  /**
   * 检查用户是否已登录
   * @returns {boolean} 是否已登录
   */
  const isLoggedIn = computed(() => {
    return tokenStorage.isTokenValid()
  })
  
  /**
   * 获取当前用户信息，经过验证的
   * @returns {Object|null} 用户信息或null
   */
  const getCurrentUser = () => {
    return verifyToken()
  }
  
  /**
   * 获取当前用户角色
   * @returns {number|null} 用户角色或null
   */
  const getCurrentUserRole = () => {
    const user = getCurrentUser()
    return user ? user.role : null
  }
  
  // 构建角色相关计算属性
  const userInfo = computed(() => store.state.user.userInfo)
  const userRole = computed(() => getCurrentUserRole())
  const isAdmin = computed(() => getCurrentUserRole() === ROLES.ADMIN)
  const isShop = computed(() => getCurrentUserRole() === ROLES.SHOP)
  const isUser = computed(() => getCurrentUserRole() === ROLES.USER)
  
  /**
   * 检查是否有指定角色
   * @param {Array|number} roles 允许的角色ID数组或单个ID
   * @returns {boolean} 是否有权限
   */
  const hasRole = (roles) => {
    const currentRole = getCurrentUserRole()
    if (!currentRole) return false
    
    if (Array.isArray(roles)) {
      return roles.includes(currentRole)
    }
    
    return currentRole === roles
  }
  
  /**
   * 检查用户是否有权限访问资源
   * @param {Object} options 权限选项
   * @param {boolean} options.requireLogin 是否需要登录
   * @param {Array|number} options.roles 允许的角色列表
   * @param {Function} options.custom 自定义验证函数
   * @returns {boolean} 是否有权限
   */
  const canAccess = (options = {}) => {
    const { requireLogin = true, roles, custom } = options
    
    // 检查登录状态
    if (requireLogin && !isLoggedIn.value) {
      return false
    }
    
    // 检查角色权限
    if (roles && !hasRole(roles)) {
      return false
    }
    
    // 自定义验证
    if (custom && typeof custom === 'function' && !custom()) {
      return false
    }
    
    return true
  }
  
  /**
   * 用户登录
   * @param {Object} credentials 登录凭证 {username, password}
   * @returns {Promise} 登录结果
   */
  const login = async (credentials) => {
    loading.value = true
    
    try {
      const result = await handleAsyncOperation(
        store.dispatch('user/login', credentials),
        {
          successMessage: STANDARD_MESSAGES.LOGIN_SUCCESS
        }
      )
      
      // 登录成功后立即验证token的有效性
      const validUser = verifyToken()
      if (!validUser) {
        message.error('登录失败，服务器返回的Token无效')
        await store.dispatch('user/logout')
        router.push('/login')
        throw new Error('无效的Token')
      }
      
      return result
    } catch (error) {
      console.error('登录失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }
  
  /**
   * 用户注册
   * @param {Object} userData 用户注册数据
   * @returns {Promise} 注册结果
   */
  const register = async (userData) => {
    loading.value = true
    
    try {
      return await handleAsyncOperation(
        store.dispatch('user/register', userData),
        {
          successMessage: STANDARD_MESSAGES.REGISTER_SUCCESS,
          successCallback: () => {
            router.push('/login')
          }
        }
      )
    } catch (error) {
      console.error('注册失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }
  
  /**
   * 退出登录
   * @param {boolean} redirect 是否重定向到登录页
   * @returns {Promise} 退出结果
   */
  const logout = async (redirect = true) => {
    loading.value = true
    
    try {
      await handleAsyncOperation(
        store.dispatch('user/logout'),
        {
          successMessage: STANDARD_MESSAGES.LOGOUT_SUCCESS,
          successCallback: () => {
            if (redirect) {
              router.push('/login')
            }
          }
        }
      )
      
      return true
    } catch (error) {
      console.error('退出登录失败:', error)
      // 即便API调用失败，也要清除本地状态
      removeToken()
      store.commit('user/CLEAR_USER')
      if (redirect) {
        router.push('/login')
      }
      throw error
    } finally {
      loading.value = false
    }
  }
  
  /**
   * 更新用户信息
   * @param {Object} userData 用户数据
   * @returns {Promise} 更新结果
   */
  const updateUserInfo = async (userData) => {
    loading.value = true
    
    try {
      return await handleAsyncOperation(
        store.dispatch('user/updateUserInfo', userData),
        {
          successMessage: STANDARD_MESSAGES.UPDATE_SUCCESS
        }
      )
    } catch (error) {
      console.error('更新用户信息失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }
  
  /**
   * 修改密码
   * @param {Object} passwordData 密码数据 {oldPassword, newPassword}
   * @returns {Promise} 修改结果
   */
  const changePassword = async (passwordData) => {
    loading.value = true
    
    try {
      return await handleAsyncOperation(
        store.dispatch('user/changePassword', passwordData),
        {
          successMessage: '密码修改成功'
        }
      )
    } catch (error) {
      console.error('修改密码失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }
  
  /**
   * 初始化认证状态 - 确保应用启动时加载用户信息
   */
  const initAuth = async () => {
    // 验证token是否有效
    const user = verifyToken()
    if (user) {
      // 更新Vuex中的用户信息
      store.commit('user/SET_USER_INFO', user)
      // 可以选择更新详细用户信息
      try {
        await store.dispatch('user/validateSession')
      } catch (error) {
        console.error('验证会话失败:', error)
      }
      return user
    } else {
      // 确保清除无效状态
      await store.dispatch('user/logout', false)
      return null
    }
  }
  
  return {
    // 状态
    loading,
    isLoggedIn,
    userInfo,
    userRole,
    isAdmin,
    isShop,
    isUser,
    
    // token相关
    getToken,
    setToken,
    removeToken,
    verifyToken,
    
    // 角色常量
    ROLES,
    
    // 权限相关方法
    getCurrentUser,
    getCurrentUserRole,
    hasRole,
    canAccess,
    
    // 认证方法
    login,
    register,
    logout,
    initAuth,
    updateUserInfo,
    changePassword
  }
} 