/**
 * 商南茶文化项目UI开发辅助工具
 * 提供开发阶段使用的辅助功能，如设置开发模式、角色模拟等
 */

// 开发角色常量
export const DEV_ROLES = {
  ADMIN: 1,  // 管理员
  USER: 2,   // 普通用户
  SHOP: 3    // 商家
}

/**
 * 设置开发模式
 * @param {boolean} enabled 是否启用开发模式
 */
export function setDevMode(enabled = true) {
  localStorage.setItem('dev_mode', enabled.toString())
  console.log(`开发模式已${enabled ? '启用' : '禁用'}`)
  
  // 如果启用开发模式但没有设置角色，默认设置为用户角色
  if (enabled && !localStorage.getItem('dev_current_role')) {
    setDevRole(DEV_ROLES.USER)
  }
}

/**
 * 检查是否处于开发模式
 * @returns {boolean} 是否处于开发模式
 */
export function isDevMode() {
  return localStorage.getItem('dev_mode') === 'true'
}

/**
 * 设置当前开发角色
 * @param {number} roleId 角色ID
 */
export function setDevRole(roleId) {
  if (!Object.values(DEV_ROLES).includes(roleId)) {
    console.error('无效的角色ID')
    return
  }
  
  localStorage.setItem('dev_current_role', roleId.toString())
  
  const roleNames = {
    [DEV_ROLES.ADMIN]: '管理员',
    [DEV_ROLES.USER]: '普通用户',
    [DEV_ROLES.SHOP]: '商家'
  }
  
  console.log(`当前开发角色已设置为: ${roleNames[roleId]}`)
}

/**
 * 获取当前开发角色
 * @returns {number|null} 当前角色ID或null
 */
export function getDevRole() {
  const roleId = localStorage.getItem('dev_current_role')
  return roleId ? parseInt(roleId) : null
}

/**
 * 重置所有开发设置
 */
export function resetDevSettings() {
  localStorage.removeItem('dev_mode')
  localStorage.removeItem('dev_current_role')
  console.log('所有开发设置已重置')
}

/**
 * 初始化开发工具
 * 打印使用说明到控制台
 */
export function initDevTools() {
  if (process.env.NODE_ENV === 'development') {
    console.log(
      '%c商南茶文化项目 - 开发工具已加载',
      'color: white; background: #409EFF; padding: 4px 8px; border-radius: 4px; font-weight: bold;'
    )
    
    console.log(
      '%c使用方法:\n' +
      '1. setDevMode(true) - 启用开发模式\n' +
      '2. setDevRole(1) - 设置角色 (1:管理员, 2:用户, 3:商家)\n' +
      '3. resetDevSettings() - 重置所有开发设置',
      'color: #333; font-size: 12px;'
    )
    
    // 如果已经处于开发模式，显示当前状态
    if (isDevMode()) {
      const roleId = getDevRole()
      const roleNames = {
        [DEV_ROLES.ADMIN]: '管理员',
        [DEV_ROLES.USER]: '普通用户',
        [DEV_ROLES.SHOP]: '商家'
      }
      
      console.log(
        '%c当前开发状态: 开发模式已启用, 角色: ' + (roleId ? roleNames[roleId] : '未设置'),
        'color: #67C23A; font-weight: bold;'
      )
    }
  }
}

// 导出工具函数集合
export default {
  DEV_ROLES,
  setDevMode,
  isDevMode,
  setDevRole,
  getDevRole,
  resetDevSettings,
  initDevTools
} 