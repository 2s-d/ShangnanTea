import { useAuth } from '@/composables/useAuth'

/**
 * 权限指令 - 根据用户权限控制元素的显示
 */

/**
 * 使用示例:
 * 
 * 1. 需要登录才能显示:
 * <div v-permission></div>
 * 
 * 2. 需要特定角色才能显示:
 * <div v-permission="{ roles: [1] }"></div> // 只有管理员可见
 * <div v-permission="{ roles: [1, 3] }"></div> // 管理员和商家可见
 * 
 * 3. 自定义权限判断:
 * <div v-permission="{ custom: () => store.getters['product/isOwner'] }"></div>
 */
const permissionDirective = {
  mounted(el, binding) {
    const { value } = binding
    
    // 创建auth实例
    const auth = useAuth()
    
    // 检查权限
    const hasPermission = auth.canAccess(value || { requireLogin: true })
    
    if (!hasPermission) {
      // 无权限时移除元素
      el.parentNode && el.parentNode.removeChild(el)
    }
  }
}

export default {
  install(app) {
    app.directive('permission', permissionDirective)
  }
} 