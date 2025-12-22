/**
 * 提示消息工具（纯前端）
 * 
 * 整合所有模块的 PROMPT 消息，用于表单验证、确认框等纯前端提示
 * 不涉及后端交互
 */

import { promptMessage } from './messageManager'

// ==================== 通用模块 PROMPT ====================
export const COMMON_PROMPT = {
  FILE_COUNT_LIMIT: '最多只能上传{count}张图片',
  UPLOAD_API_NOT_CONFIGURED: '未配置上传API',
  EMAIL_REQUIRED: '请输入您的邮箱',
  EMAIL_ALREADY_SUBSCRIBED: '该邮箱已订阅，无需重复操作',
  PROCESSING: '处理中...'
}

export const commonPromptMessages = {
  showFileCountLimit(count) {
    const msg = COMMON_PROMPT.FILE_COUNT_LIMIT.replace('{count}', count)
    promptMessage.show(msg)
  },
  showUploadApiNotConfigured() {
    promptMessage.show(COMMON_PROMPT.UPLOAD_API_NOT_CONFIGURED)
  },
  showEmailRequired() {
    promptMessage.show(COMMON_PROMPT.EMAIL_REQUIRED)
  },
  showEmailAlreadySubscribed() {
    promptMessage.info(COMMON_PROMPT.EMAIL_ALREADY_SUBSCRIBED)
  },
  showProcessing() {
    promptMessage.info(COMMON_PROMPT.PROCESSING)
  }
}

// ==================== 用户模块 PROMPT ====================
export const USER_PROMPT = {
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
  USERNAME_INPUT_REQUIRED: '请先输入用户名',
  PHONE_INPUT_REQUIRED: '请先输入手机号',
  EMAIL_INPUT_REQUIRED: '请先输入邮箱',
  REGION_DATA_INCOMPLETE: '省市区数据加载不完整，将使用备用数据',
  EXPORT_FEATURE_DEVELOPING: '导出功能开发中，敬请期待...'
}
