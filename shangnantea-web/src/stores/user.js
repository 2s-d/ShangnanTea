import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  // State (用 ref)
  const userInfo = ref(null)
  const isLoggedIn = ref(false)
  const loading = ref(false)
  
  // Getters (用 computed)
  const userRole = computed(() => userInfo.value?.role || 0)
  const isAdmin = computed(() => userInfo.value?.role === 1)
  const isUser = computed(() => userInfo.value?.role === 2)
  const isShop = computed(() => userInfo.value?.role === 3)
  
  // Actions (普通函数)
  // TODO: 从 src/store/modules/user.js 迁移所有 actions
  
  return {
    // State
    userInfo,
    isLoggedIn,
    loading,
    // Getters
    userRole,
    isAdmin,
    isUser,
    isShop,
    // Actions
    // TODO: 导出所有 action 函数
  }
})
