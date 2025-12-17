/**
 * 用户模块的专用消息工具
 * 
 * 包含用户模块的常用消息文本及显示方法
 * 遵循消息三层架构：API层、Vuex业务层、UI交互层
 */

import { apiMessage, vuexMessage, uiMessage } from './messageManager'

// 用户模块常用消息定义
export const USER_MESSAGES = {
  // API层消息
  API: {
    LOGIN_SUCCESS: '登录成功',
    LOGIN_FAILURE: '登录失败',
    REGISTER_SUCCESS: '注册成功',
    REGISTER_FAILURE: '注册失败',
    LOGOUT_SUCCESS: '退出登录成功',
    USER_INFO_GET_SUCCESS: '获取用户信息成功',
    USER_INFO_GET_FAILURE: '获取用户信息失败',
    USER_UPDATE_SUCCESS: '更新用户信息成功',
    USER_UPDATE_FAILURE: '更新用户信息失败',
    PASSWORD_CHANGE_SUCCESS: '密码修改成功',
    PASSWORD_CHANGE_FAILURE: '密码修改失败',
    SESSION_EXPIRED: '会话已过期，请重新登录',
    PERMISSION_DENIED: '没有访问权限',
    AUTH_ERROR: '认证失败，请重新登录'
  },
  
  // Vuex业务层消息
  BUSINESS: {
    LOGIN_SUCCESS: '登录成功',
    LOGIN_FAILURE: '登录失败，请检查用户名和密码',
    REGISTER_SUCCESS: '注册成功，请登录',
    REGISTER_FAILURE: '注册失败',
    LOGOUT_SUCCESS: '已安全退出系统',
    PROFILE_UPDATE_SUCCESS: '个人资料更新成功',
    PROFILE_UPDATE_FAILURE: '个人资料更新失败',
    PASSWORD_MISMATCH: '两次输入的密码不一致',
    PASSWORD_CHANGE_SUCCESS: '密码修改成功，请使用新密码登录',
    PASSWORD_CHANGE_FAILURE: '密码修改失败，请检查原密码是否正确',
    SESSION_EXPIRED: '您的登录已过期，请重新登录',
    PERMISSION_DENIED: '您没有权限执行此操作',
    AVATAR_UPDATE_SUCCESS: '头像更新成功',
    AVATAR_UPDATE_FAILURE: '头像更新失败'
  },
  
  // UI交互层消息
  UI: {
    USERNAME_REQUIRED: '请输入用户名',
    PASSWORD_REQUIRED: '请输入密码',
    PASSWORD_FORMAT_INVALID: '密码格式不正确，需包含字母和数字且长度至少为8位',
    EMAIL_FORMAT_INVALID: '邮箱格式不正确',
    PHONE_FORMAT_INVALID: '手机号格式不正确',
    AGREEMENT_REQUIRED: '请先阅读并同意用户协议',
    CAPTCHA_REQUIRED: '请输入验证码',
    AVATAR_SIZE_LIMIT: '头像图片大小不能超过2MB',
    AVATAR_FORMAT_INVALID: '头像必须是JPG或PNG格式',
    FORM_INCOMPLETE: '请完善表单信息',
    CONFIRM_LOGOUT: '确定要退出登录吗？'
  }
}

/**
 * 用户API层消息函数
 */
export const userApiMessages = {
  /**
   * 显示登录成功消息
   */
  showLoginSuccess() {
    apiMessage.success(USER_MESSAGES.API.LOGIN_SUCCESS)
  },
  
  /**
   * 显示登录失败消息
   * @param {string} reason 失败原因
   */
  showLoginFailure(reason) {
    apiMessage.error(reason || USER_MESSAGES.API.LOGIN_FAILURE)
  },
  
  /**
   * 显示注册成功消息
   */
  showRegisterSuccess() {
    apiMessage.success(USER_MESSAGES.API.REGISTER_SUCCESS)
  },
  
  /**
   * 显示注册失败消息
   * @param {string} reason 失败原因
   */
  showRegisterFailure(reason) {
    apiMessage.error(reason || USER_MESSAGES.API.REGISTER_FAILURE)
  },
  
  /**
   * 显示退出登录成功消息
   */
  showLogoutSuccess() {
    apiMessage.success(USER_MESSAGES.API.LOGOUT_SUCCESS)
  },
  
  /**
   * 显示用户信息获取成功消息
   */
  showUserInfoGetSuccess() {
    apiMessage.success(USER_MESSAGES.API.USER_INFO_GET_SUCCESS)
  },
  
  /**
   * 显示用户信息获取失败消息
   * @param {string} reason 失败原因
   */
  showUserInfoGetFailure(reason) {
    apiMessage.error(reason || USER_MESSAGES.API.USER_INFO_GET_FAILURE)
  },
  
  /**
   * 显示用户信息更新成功消息
   */
  showUserUpdateSuccess() {
    apiMessage.success(USER_MESSAGES.API.USER_UPDATE_SUCCESS)
  },
  
  /**
   * 显示用户信息更新失败消息
   * @param {string} reason 失败原因
   */
  showUserUpdateFailure(reason) {
    apiMessage.error(reason || USER_MESSAGES.API.USER_UPDATE_FAILURE)
  },
  
  /**
   * 显示密码修改成功消息
   */
  showPasswordChangeSuccess() {
    apiMessage.success(USER_MESSAGES.API.PASSWORD_CHANGE_SUCCESS)
  },
  
  /**
   * 显示密码修改失败消息
   * @param {string} reason 失败原因
   */
  showPasswordChangeFailure(reason) {
    apiMessage.error(reason || USER_MESSAGES.API.PASSWORD_CHANGE_FAILURE)
  },
  
  /**
   * 显示会话过期消息
   */
  showSessionExpired() {
    apiMessage.error(USER_MESSAGES.API.SESSION_EXPIRED)
  },
  
  /**
   * 显示权限拒绝消息
   */
  showPermissionDenied() {
    apiMessage.error(USER_MESSAGES.API.PERMISSION_DENIED)
  },
  
  /**
   * 显示认证错误消息
   */
  showAuthError() {
    apiMessage.error(USER_MESSAGES.API.AUTH_ERROR)
  }
}

/**
 * 用户业务层消息函数
 */
export const userBusinessMessages = {
  /**
   * 显示登录成功消息
   * @param {string} username 用户名
   */
  showLoginSuccess(username) {
    vuexMessage.success(username ? `${username}，${USER_MESSAGES.BUSINESS.LOGIN_SUCCESS}` : USER_MESSAGES.BUSINESS.LOGIN_SUCCESS)
  },
  
  /**
   * 显示登录失败消息
   * @param {string} reason 失败原因
   */
  showLoginFailure(reason) {
    vuexMessage.error(reason || USER_MESSAGES.BUSINESS.LOGIN_FAILURE)
  },
  
  /**
   * 显示注册成功消息
   */
  showRegisterSuccess() {
    vuexMessage.success(USER_MESSAGES.BUSINESS.REGISTER_SUCCESS)
  },
  
  /**
   * 显示注册失败消息
   * @param {string} reason 失败原因
   */
  showRegisterFailure(reason) {
    vuexMessage.error(reason || USER_MESSAGES.BUSINESS.REGISTER_FAILURE)
  },
  
  /**
   * 显示退出登录成功消息
   */
  showLogoutSuccess() {
    vuexMessage.success(USER_MESSAGES.BUSINESS.LOGOUT_SUCCESS)
  },
  
  /**
   * 显示个人资料更新成功消息
   */
  showProfileUpdateSuccess() {
    vuexMessage.success(USER_MESSAGES.BUSINESS.PROFILE_UPDATE_SUCCESS)
  },
  
  /**
   * 显示个人资料更新失败消息
   * @param {string} reason 失败原因
   */
  showProfileUpdateFailure(reason) {
    vuexMessage.error(reason || USER_MESSAGES.BUSINESS.PROFILE_UPDATE_FAILURE)
  },
  
  /**
   * 显示密码不匹配消息
   */
  showPasswordMismatch() {
    vuexMessage.error(USER_MESSAGES.BUSINESS.PASSWORD_MISMATCH)
  },
  
  /**
   * 显示密码修改成功消息
   */
  showPasswordChangeSuccess() {
    vuexMessage.success(USER_MESSAGES.BUSINESS.PASSWORD_CHANGE_SUCCESS)
  },
  
  /**
   * 显示密码修改失败消息
   * @param {string} reason 失败原因
   */
  showPasswordChangeFailure(reason) {
    vuexMessage.error(reason || USER_MESSAGES.BUSINESS.PASSWORD_CHANGE_FAILURE)
  },
  
  /**
   * 显示会话过期消息
   */
  showSessionExpired() {
    vuexMessage.warning(USER_MESSAGES.BUSINESS.SESSION_EXPIRED)
  },
  
  /**
   * 显示权限拒绝消息
   */
  showPermissionDenied() {
    vuexMessage.warning(USER_MESSAGES.BUSINESS.PERMISSION_DENIED)
  },
  
  /**
   * 显示头像更新成功消息
   */
  showAvatarUpdateSuccess() {
    vuexMessage.success(USER_MESSAGES.BUSINESS.AVATAR_UPDATE_SUCCESS)
  },
  
  /**
   * 显示头像更新失败消息
   * @param {string} reason 失败原因
   */
  showAvatarUpdateFailure(reason) {
    vuexMessage.error(reason || USER_MESSAGES.BUSINESS.AVATAR_UPDATE_FAILURE)
  }
}

/**
 * 用户UI交互层消息函数
 */
export const userUIMessages = {
  /**
   * 显示用户名必填消息
   */
  showUsernameRequired() {
    uiMessage.warning(USER_MESSAGES.UI.USERNAME_REQUIRED)
  },
  
  /**
   * 显示密码必填消息
   */
  showPasswordRequired() {
    uiMessage.warning(USER_MESSAGES.UI.PASSWORD_REQUIRED)
  },
  
  /**
   * 显示密码格式无效消息
   */
  showPasswordFormatInvalid() {
    uiMessage.warning(USER_MESSAGES.UI.PASSWORD_FORMAT_INVALID)
  },
  
  /**
   * 显示邮箱格式无效消息
   */
  showEmailFormatInvalid() {
    uiMessage.warning(USER_MESSAGES.UI.EMAIL_FORMAT_INVALID)
  },
  
  /**
   * 显示手机号格式无效消息
   */
  showPhoneFormatInvalid() {
    uiMessage.warning(USER_MESSAGES.UI.PHONE_FORMAT_INVALID)
  },
  
  /**
   * 显示需要同意协议消息
   */
  showAgreementRequired() {
    uiMessage.warning(USER_MESSAGES.UI.AGREEMENT_REQUIRED)
  },
  
  /**
   * 显示验证码必填消息
   */
  showCaptchaRequired() {
    uiMessage.warning(USER_MESSAGES.UI.CAPTCHA_REQUIRED)
  },
  
  /**
   * 显示头像大小限制消息
   */
  showAvatarSizeLimit() {
    uiMessage.warning(USER_MESSAGES.UI.AVATAR_SIZE_LIMIT)
  },
  
  /**
   * 显示头像格式无效消息
   */
  showAvatarFormatInvalid() {
    uiMessage.warning(USER_MESSAGES.UI.AVATAR_FORMAT_INVALID)
  },
  
  /**
   * 显示表单不完整消息
   */
  showFormIncomplete() {
    uiMessage.warning(USER_MESSAGES.UI.FORM_INCOMPLETE)
  },
  
  /**
   * 显示确认退出登录消息
   */
  showConfirmLogout() {
    uiMessage.info(USER_MESSAGES.UI.CONFIRM_LOGOUT)
  }
}

// 默认导出所有消息工具
export default {
  api: userApiMessages,
  business: userBusinessMessages,
  ui: userUIMessages
} 