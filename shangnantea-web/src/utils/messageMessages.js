/**
 * 消息模块消息工具（私信/通知）
 * 
 * 分类说明：
 * - SUCCESS: 操作成功反馈
 * - ERROR: 操作失败/错误提示
 * - PROMPT: 用户提示（表单验证+确认）
 */

import { successMessage, errorMessage, promptMessage } from './messageManager'

// 消息模块消息常量
export const MESSAGE_MESSAGES = {
  // 成功消息
  SUCCESS: {
    MESSAGE_SENT: '消息发送成功',
    NOTIFICATION_READ: '通知已标记为已读',
    ALL_NOTIFICATIONS_READ: '所有通知已标记为已读',
    NOTIFICATION_DELETED: '通知已删除',
    ALL_NOTIFICATIONS_DELETED: '所有通知已清空',
    SESSION_DELETED: '会话已删除',
    MESSAGE_RECALLED: '消息已撤回',
    NOTIFICATION_TOPPED: '通知已置顶',
    NOTIFICATION_UNTOPPED: '通知已取消置顶',
    IMAGE_UPLOADED: '图片上传成功'
  },
  
  // 错误消息
  ERROR: {
    SEND_FAILED: '发送失败',
    MARK_READ_FAILED: '标记已读失败',
    LOAD_FAILED: '加载失败',
    RECALL_FAILED: '消息撤回失败，已超过撤回时间',
    IMAGE_UPLOAD_FAILED: '图片上传失败',
    USER_INFO_LOAD_FAILED: '未能获取到用户信息',
    USER_INFO_EMPTY: '用户信息不完整'
  },
  
  // 提示消息
  PROMPT: {
    MESSAGE_EMPTY: '消息内容不能为空',
    RECIPIENT_REQUIRED: '请选择接收人',
    IMAGE_SIZE_LIMIT: '图片大小不能超过5MB',
    IMAGE_FORMAT_INVALID: '只支持JPG、PNG和GIF格式的图片',
    USER_DATA_INCOMPLETE: '用户数据加载不完整，请刷新页面或重新登录',
    DELETE_SESSION_CONFIRM: '确定要删除此会话吗？历史消息将无法恢复',
    DELETE_NOTIFICATION_CONFIRM: '确定要删除此通知吗？',
    CLEAR_ALL_NOTIFICATIONS_CONFIRM: '确定要清空所有通知吗？',
    RECALL_CONFIRM: '确定要撤回此消息吗？'
  }
}

// 成功消息函数
export const messageSuccessMessages = {
  showMessageSent() {
    successMessage.show(MESSAGE_MESSAGES.SUCCESS.MESSAGE_SENT)
  },
  showNotificationRead() {
    successMessage.show(MESSAGE_MESSAGES.SUCCESS.NOTIFICATION_READ)
  },
  showAllNotificationsRead() {
    successMessage.show(MESSAGE_MESSAGES.SUCCESS.ALL_NOTIFICATIONS_READ)
  },
  showNotificationDeleted() {
    successMessage.show(MESSAGE_MESSAGES.SUCCESS.NOTIFICATION_DELETED)
  },
  showAllNotificationsDeleted() {
    successMessage.show(MESSAGE_MESSAGES.SUCCESS.ALL_NOTIFICATIONS_DELETED)
  },
  showSessionDeleted() {
    successMessage.show(MESSAGE_MESSAGES.SUCCESS.SESSION_DELETED)
  },
  showMessageRecalled() {
    successMessage.show(MESSAGE_MESSAGES.SUCCESS.MESSAGE_RECALLED)
  },
  showNotificationTopped() {
    successMessage.show(MESSAGE_MESSAGES.SUCCESS.NOTIFICATION_TOPPED)
  },
  showNotificationUntopped() {
    successMessage.show(MESSAGE_MESSAGES.SUCCESS.NOTIFICATION_UNTOPPED)
  },
  showImageUploaded() {
    successMessage.show(MESSAGE_MESSAGES.SUCCESS.IMAGE_UPLOADED)
  },
  showNewMessageReceived(from) {
    const msg = from ? `来自 ${from} 的新消息` : '收到新消息'
    successMessage.show(msg)
  },
  showNewNotificationReceived(title) {
    const msg = title ? `新通知: ${title}` : '收到新通知'
    successMessage.show(msg)
  },
  showOperationSuccess(msg) {
    successMessage.show(msg || '操作成功')
  }
}

// 错误消息函数
export const messageErrorMessages = {
  showSendFailed(reason) {
    errorMessage.show(reason || MESSAGE_MESSAGES.ERROR.SEND_FAILED)
  },
  showMarkReadFailed(reason) {
    errorMessage.show(reason || MESSAGE_MESSAGES.ERROR.MARK_READ_FAILED)
  },
  showLoadFailed(reason) {
    errorMessage.show(reason || MESSAGE_MESSAGES.ERROR.LOAD_FAILED)
  },
  showRecallFailed() {
    errorMessage.warning(MESSAGE_MESSAGES.ERROR.RECALL_FAILED)
  },
  showImageUploadFailed(reason) {
    errorMessage.show(reason || MESSAGE_MESSAGES.ERROR.IMAGE_UPLOAD_FAILED)
  },
  showUserInfoLoadFailed(reason) {
    errorMessage.show(reason || MESSAGE_MESSAGES.ERROR.USER_INFO_LOAD_FAILED)
  },
  showUserInfoEmpty() {
    errorMessage.warning(MESSAGE_MESSAGES.ERROR.USER_INFO_EMPTY)
  },
  showOperationFailed(reason) {
    errorMessage.show(reason || '操作失败')
  }
}

// 提示消息函数
export const messagePromptMessages = {
  showMessageEmpty() {
    promptMessage.show(MESSAGE_MESSAGES.PROMPT.MESSAGE_EMPTY)
  },
  showRecipientRequired() {
    promptMessage.show(MESSAGE_MESSAGES.PROMPT.RECIPIENT_REQUIRED)
  },
  showImageSizeLimit() {
    promptMessage.show(MESSAGE_MESSAGES.PROMPT.IMAGE_SIZE_LIMIT)
  },
  showImageFormatInvalid() {
    promptMessage.show(MESSAGE_MESSAGES.PROMPT.IMAGE_FORMAT_INVALID)
  },
  showUserDataIncomplete() {
    promptMessage.show(MESSAGE_MESSAGES.PROMPT.USER_DATA_INCOMPLETE)
  },
  showDeleteSessionConfirm() {
    promptMessage.info(MESSAGE_MESSAGES.PROMPT.DELETE_SESSION_CONFIRM)
  },
  showDeleteNotificationConfirm() {
    promptMessage.info(MESSAGE_MESSAGES.PROMPT.DELETE_NOTIFICATION_CONFIRM)
  },
  showClearAllNotificationsConfirm() {
    promptMessage.info(MESSAGE_MESSAGES.PROMPT.CLEAR_ALL_NOTIFICATIONS_CONFIRM)
  },
  showRecallConfirm() {
    promptMessage.info(MESSAGE_MESSAGES.PROMPT.RECALL_CONFIRM)
  },
  showPleaseWait() {
    promptMessage.info('请稍候...')
  }
}

// 默认导出
export default {
  success: messageSuccessMessages,
  error: messageErrorMessages,
  prompt: messagePromptMessages,
  MESSAGE_MESSAGES
}
