/**
 * 用户模块消息工具
 * 
 * 分类说明：
 * - SUCCESS: 操作成功反馈
 * - ERROR: 操作失败/错误提示
 * - PROMPT: 用户提示（表单验证+确认）
 */

import { successMessage, errorMessage, promptMessage } from './messageManager'

// 用户模块消息常量
export const USER_MESSAGES = {
  // 成功消息
  SUCCESS: {
    LOGIN: '登录成功',
    REGISTER: '注册成功，请登录',
    LOGOUT: '已安全退出系统',
    PROFILE_UPDATE: '个人资料更新成功',
    PASSWORD_CHANGE: '密码修改成功，请使用新密码登录',
    AVATAR_UPDATE: '头像更新成功',
    PASSWORD_RESET: '密码重置成功',
    // 管理员功能
    USER_DELETE: '用户已删除',
    USER_STATUS_TOGGLE: '用户状态已修改',
    USER_UPDATE: '用户已更新',
    ADMIN_CREATE: '管理员已添加',
    // 设置相关
    SETTINGS_RESTORE: '已恢复默认设置，请点击保存以应用',
    DATA_MIGRATION: '数据迁移已触发',
    STORAGE_RESET: '本地存储已重置，页面将刷新',
    // 验证码相关
    CAPTCHA_SENT: '验证码已发送，请查收'
  },
  
  // 错误消息
  ERROR: {
    LOGIN_FAILED: '登录失败，请检查用户名和密码',
    REGISTER_FAILED: '注册失败',
    PROFILE_UPDATE_FAILED: '个人资料更新失败',
    PASSWORD_CHANGE_FAILED: '密码修改失败，请检查原密码是否正确',
    PASSWORD_MISMATCH: '两次输入的密码不一致',
    AVATAR_UPDATE_FAILED: '头像更新失败',
    SESSION_EXPIRED: '您的登录已过期，请重新登录',
    PERMISSION_DENIED: '您没有权限执行此操作',
    AUTH_ERROR: '认证失败，请重新登录',
    PASSWORD_RESET_FAILED: '密码重置失败',
    // 管理员功能
    USER_LIST_FETCH_FAILED: '获取用户列表失败，请稍后重试',
    USER_DELETE_FAILED: '删除用户失败，请稍后重试',
    USER_STATUS_TOGGLE_FAILED: '修改用户状态失败，请稍后重试',
    USER_FORM_SUBMIT_FAILED: '提交失败，请稍后重试',
    // 头像上传
    AVATAR_FORMAT_ERROR: '上传头像图片只能是图片格式!',
    AVATAR_SIZE_ERROR: '上传头像图片大小不能超过 2MB!',
    // 数据相关
    DATA_MIGRATION_FAILED: '数据迁移失败',
    // Token相关
    TOKEN_INVALID: '登录失败，服务器返回的Token无效'
  },
  
  // 提示消息
  PROMPT: {
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
    CONFIRM_LOGOUT: '确定要退出登录吗？',
    // 验证码相关
    USERNAME_INPUT_REQUIRED: '请先输入用户名',
    PHONE_INPUT_REQUIRED: '请先输入手机号',
    EMAIL_INPUT_REQUIRED: '请先输入邮箱',
    // 数据相关
    REGION_DATA_INCOMPLETE: '省市区数据加载不完整，将使用备用数据',
    // 功能开发中
    EXPORT_FEATURE_DEVELOPING: '导出功能开发中，敬请期待...'
  }
}

// 成功消息函数
export const userSuccessMessages = {
  showLoginSuccess(username) {
    const msg = username ? `${username}，${USER_MESSAGES.SUCCESS.LOGIN}` : USER_MESSAGES.SUCCESS.LOGIN
    successMessage.show(msg)
  },
  showRegisterSuccess() {
    successMessage.show(USER_MESSAGES.SUCCESS.REGISTER)
  },
  showLogoutSuccess() {
    successMessage.show(USER_MESSAGES.SUCCESS.LOGOUT)
  },
  showProfileUpdateSuccess() {
    successMessage.show(USER_MESSAGES.SUCCESS.PROFILE_UPDATE)
  },
  showPasswordChangeSuccess() {
    successMessage.show(USER_MESSAGES.SUCCESS.PASSWORD_CHANGE)
  },
  showAvatarUpdateSuccess() {
    successMessage.show(USER_MESSAGES.SUCCESS.AVATAR_UPDATE)
  },
  showPasswordResetSuccess() {
    successMessage.show(USER_MESSAGES.SUCCESS.PASSWORD_RESET)
  },
  // 管理员功能
  showUserDeleted(username) {
    const msg = username ? `用户 "${username}" ${USER_MESSAGES.SUCCESS.USER_DELETE}` : USER_MESSAGES.SUCCESS.USER_DELETE
    successMessage.show(msg)
  },
  showUserStatusToggled(username, status) {
    const statusText = status === 1 ? '启用' : '禁用'
    const msg = username ? `用户 "${username}" 已${statusText}` : USER_MESSAGES.SUCCESS.USER_STATUS_TOGGLE
    successMessage.show(msg)
  },
  showUserUpdated(username) {
    const msg = username ? `用户 "${username}" ${USER_MESSAGES.SUCCESS.USER_UPDATE}` : USER_MESSAGES.SUCCESS.USER_UPDATE
    successMessage.show(msg)
  },
  showAdminCreated(username) {
    const msg = username ? `管理员 "${username}" ${USER_MESSAGES.SUCCESS.ADMIN_CREATE}` : USER_MESSAGES.SUCCESS.ADMIN_CREATE
    successMessage.show(msg)
  },
  // 设置相关
  showSettingsRestored() {
    successMessage.show(USER_MESSAGES.SUCCESS.SETTINGS_RESTORE)
  },
  showDataMigrationTriggered() {
    successMessage.show(USER_MESSAGES.SUCCESS.DATA_MIGRATION)
  },
  showStorageReset() {
    successMessage.show(USER_MESSAGES.SUCCESS.STORAGE_RESET)
  },
  // 验证码相关
  showCaptchaSent() {
    successMessage.show(USER_MESSAGES.SUCCESS.CAPTCHA_SENT)
  }
}

// 错误消息函数
export const userErrorMessages = {
  showLoginFailure(reason) {
    errorMessage.show(reason || USER_MESSAGES.ERROR.LOGIN_FAILED)
  },
  showRegisterFailure(reason) {
    errorMessage.show(reason || USER_MESSAGES.ERROR.REGISTER_FAILED)
  },
  showProfileUpdateFailure(reason) {
    errorMessage.show(reason || USER_MESSAGES.ERROR.PROFILE_UPDATE_FAILED)
  },
  showPasswordChangeFailure(reason) {
    errorMessage.show(reason || USER_MESSAGES.ERROR.PASSWORD_CHANGE_FAILED)
  },
  showPasswordMismatch() {
    errorMessage.show(USER_MESSAGES.ERROR.PASSWORD_MISMATCH)
  },
  showAvatarUpdateFailure(reason) {
    errorMessage.show(reason || USER_MESSAGES.ERROR.AVATAR_UPDATE_FAILED)
  },
  showSessionExpired() {
    errorMessage.warning(USER_MESSAGES.ERROR.SESSION_EXPIRED)
  },
  showPermissionDenied() {
    errorMessage.warning(USER_MESSAGES.ERROR.PERMISSION_DENIED)
  },
  showAuthError() {
    errorMessage.show(USER_MESSAGES.ERROR.AUTH_ERROR)
  },
  showPasswordResetFailure(reason) {
    errorMessage.show(reason || USER_MESSAGES.ERROR.PASSWORD_RESET_FAILED)
  },
  // 管理员功能
  showUserListFetchFailed() {
    errorMessage.show(USER_MESSAGES.ERROR.USER_LIST_FETCH_FAILED)
  },
  showUserDeleteFailed() {
    errorMessage.show(USER_MESSAGES.ERROR.USER_DELETE_FAILED)
  },
  showUserStatusToggleFailed() {
    errorMessage.show(USER_MESSAGES.ERROR.USER_STATUS_TOGGLE_FAILED)
  },
  showUserFormSubmitFailed() {
    errorMessage.show(USER_MESSAGES.ERROR.USER_FORM_SUBMIT_FAILED)
  },
  // 头像上传
  showAvatarFormatError() {
    errorMessage.show(USER_MESSAGES.ERROR.AVATAR_FORMAT_ERROR)
  },
  showAvatarSizeError() {
    errorMessage.show(USER_MESSAGES.ERROR.AVATAR_SIZE_ERROR)
  },
  // 数据相关
  showDataMigrationFailed(error) {
    const msg = error ? `${USER_MESSAGES.ERROR.DATA_MIGRATION_FAILED}: ${error}` : USER_MESSAGES.ERROR.DATA_MIGRATION_FAILED
    errorMessage.show(msg)
  },
  // Token相关
  showTokenInvalid() {
    errorMessage.show(USER_MESSAGES.ERROR.TOKEN_INVALID)
  }
}

// 提示消息函数
export const userPromptMessages = {
  showUsernameRequired() {
    promptMessage.show(USER_MESSAGES.PROMPT.USERNAME_REQUIRED)
  },
  showPasswordRequired() {
    promptMessage.show(USER_MESSAGES.PROMPT.PASSWORD_REQUIRED)
  },
  showPasswordFormatInvalid() {
    promptMessage.show(USER_MESSAGES.PROMPT.PASSWORD_FORMAT_INVALID)
  },
  showEmailFormatInvalid() {
    promptMessage.show(USER_MESSAGES.PROMPT.EMAIL_FORMAT_INVALID)
  },
  showPhoneFormatInvalid() {
    promptMessage.show(USER_MESSAGES.PROMPT.PHONE_FORMAT_INVALID)
  },
  showAgreementRequired() {
    promptMessage.show(USER_MESSAGES.PROMPT.AGREEMENT_REQUIRED)
  },
  showCaptchaRequired() {
    promptMessage.show(USER_MESSAGES.PROMPT.CAPTCHA_REQUIRED)
  },
  showAvatarSizeLimit() {
    promptMessage.show(USER_MESSAGES.PROMPT.AVATAR_SIZE_LIMIT)
  },
  showAvatarFormatInvalid() {
    promptMessage.show(USER_MESSAGES.PROMPT.AVATAR_FORMAT_INVALID)
  },
  showFormIncomplete() {
    promptMessage.show(USER_MESSAGES.PROMPT.FORM_INCOMPLETE)
  },
  showConfirmLogout() {
    promptMessage.info(USER_MESSAGES.PROMPT.CONFIRM_LOGOUT)
  },
  // 验证码相关
  showUsernameInputRequired() {
    promptMessage.show(USER_MESSAGES.PROMPT.USERNAME_INPUT_REQUIRED)
  },
  showPhoneInputRequired() {
    promptMessage.show(USER_MESSAGES.PROMPT.PHONE_INPUT_REQUIRED)
  },
  showEmailInputRequired() {
    promptMessage.show(USER_MESSAGES.PROMPT.EMAIL_INPUT_REQUIRED)
  },
  // 数据相关
  showRegionDataIncomplete() {
    promptMessage.show(USER_MESSAGES.PROMPT.REGION_DATA_INCOMPLETE)
  },
  // 功能开发中
  showExportFeatureDeveloping() {
    promptMessage.info(USER_MESSAGES.PROMPT.EXPORT_FEATURE_DEVELOPING)
  }
}

// 默认导出
export default {
  success: userSuccessMessages,
  error: userErrorMessages,
  prompt: userPromptMessages,
  USER_MESSAGES
}
