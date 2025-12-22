/**
 * 通用消息工具
 * 
 * 用于通用组件和工具函数的消息提示
 * 分类说明：
 * - SUCCESS: 操作成功反馈
 * - ERROR: 操作失败/错误提示
 * - PROMPT: 用户提示（表单验证+确认）
 */

import { successMessage, errorMessage, promptMessage } from './messageManager'

// 通用消息常量
export const COMMON_MESSAGES = {
  // 成功消息
  SUCCESS: {
    UPLOAD_SUCCESS: '上传成功',
    SUBSCRIBE_SUCCESS: '订阅成功，感谢您的关注！',
    OPERATION_SUCCESS: '操作成功',
    SAVE_SUCCESS: '保存成功',
    DELETE_SUCCESS: '删除成功',
    UPDATE_SUCCESS: '更新成功',
    COPY_SUCCESS: '复制成功'
  },
  
  // 错误消息
  ERROR: {
    // 文件上传相关
    FILE_TYPE_INVALID: '不支持的文件类型，请上传正确格式的图片',
    FILE_SIZE_EXCEEDED: '文件大小不能超过{size}MB',
    UPLOAD_FAILED: '上传失败',
    // 表单相关
    FORM_REF_NOT_FOUND: '表单引用不存在',
    // 邮箱相关
    EMAIL_INVALID: '请输入有效的邮箱地址',
    // 图片上传
    IMAGE_TYPE_INVALID: '只能上传图片文件',
    IMAGE_SIZE_EXCEEDED: '图片大小不能超过5MB',
    // 通用错误
    OPERATION_FAILED: '操作失败',
    LOAD_FAILED: '加载失败'
  },
  
  // 提示消息
  PROMPT: {
    // 文件上传相关
    FILE_COUNT_LIMIT: '最多只能上传{count}张图片',
    UPLOAD_API_NOT_CONFIGURED: '未配置上传API',
    // 邮箱相关
    EMAIL_REQUIRED: '请输入您的邮箱',
    EMAIL_ALREADY_SUBSCRIBED: '该邮箱已订阅，无需重复操作',
    // 通用提示
    PROCESSING: '处理中...'
  }
}

// 成功消息函数
export const commonSuccessMessages = {
  showUploadSuccess() {
    successMessage.show(COMMON_MESSAGES.SUCCESS.UPLOAD_SUCCESS)
  },
  showSubscribeSuccess() {
    successMessage.show(COMMON_MESSAGES.SUCCESS.SUBSCRIBE_SUCCESS)
  },
  showOperationSuccess(msg) {
    successMessage.show(msg || COMMON_MESSAGES.SUCCESS.OPERATION_SUCCESS)
  },
  showSaveSuccess() {
    successMessage.show(COMMON_MESSAGES.SUCCESS.SAVE_SUCCESS)
  },
  showDeleteSuccess() {
    successMessage.show(COMMON_MESSAGES.SUCCESS.DELETE_SUCCESS)
  },
  showUpdateSuccess() {
    successMessage.show(COMMON_MESSAGES.SUCCESS.UPDATE_SUCCESS)
  },
  showCopySuccess() {
    successMessage.show(COMMON_MESSAGES.SUCCESS.COPY_SUCCESS)
  }
}

// 错误消息函数
export const commonErrorMessages = {
  showFileTypeInvalid() {
    errorMessage.show(COMMON_MESSAGES.ERROR.FILE_TYPE_INVALID)
  },
  showFileSizeExceeded(size) {
    const msg = COMMON_MESSAGES.ERROR.FILE_SIZE_EXCEEDED.replace('{size}', size)
    errorMessage.show(msg)
  },
  showUploadFailed(error) {
    const msg = error ? `${COMMON_MESSAGES.ERROR.UPLOAD_FAILED}: ${error}` : COMMON_MESSAGES.ERROR.UPLOAD_FAILED
    errorMessage.show(msg)
  },
  showFormRefNotFound() {
    errorMessage.show(COMMON_MESSAGES.ERROR.FORM_REF_NOT_FOUND)
  },
  showEmailInvalid() {
    errorMessage.show(COMMON_MESSAGES.ERROR.EMAIL_INVALID)
  },
  showImageTypeInvalid() {
    errorMessage.show(COMMON_MESSAGES.ERROR.IMAGE_TYPE_INVALID)
  },
  showImageSizeExceeded() {
    errorMessage.show(COMMON_MESSAGES.ERROR.IMAGE_SIZE_EXCEEDED)
  },
  showOperationFailed(reason) {
    errorMessage.show(reason || COMMON_MESSAGES.ERROR.OPERATION_FAILED)
  },
  showLoadFailed(reason) {
    errorMessage.show(reason || COMMON_MESSAGES.ERROR.LOAD_FAILED)
  }
}

// 提示消息函数
export const commonPromptMessages = {
  showFileCountLimit(count) {
    const msg = COMMON_MESSAGES.PROMPT.FILE_COUNT_LIMIT.replace('{count}', count)
    promptMessage.show(msg)
  },
  showUploadApiNotConfigured() {
    promptMessage.show(COMMON_MESSAGES.PROMPT.UPLOAD_API_NOT_CONFIGURED)
  },
  showEmailRequired() {
    promptMessage.show(COMMON_MESSAGES.PROMPT.EMAIL_REQUIRED)
  },
  showEmailAlreadySubscribed() {
    promptMessage.info(COMMON_MESSAGES.PROMPT.EMAIL_ALREADY_SUBSCRIBED)
  },
  showProcessing() {
    promptMessage.info(COMMON_MESSAGES.PROMPT.PROCESSING)
  }
}

// 默认导出
export default {
  success: commonSuccessMessages,
  error: commonErrorMessages,
  prompt: commonPromptMessages,
  COMMON_MESSAGES
}