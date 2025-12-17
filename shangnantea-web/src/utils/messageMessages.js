/**
 * 消息模块消息工具
 * 按照三层架构设计消息：
 * 1. API层：处理网络请求相关消息
 * 2. Vuex业务层：处理业务操作结果消息
 * 3. UI交互层：处理界面交互提示消息
 */
import messageManager from './messageManager'

// 消息模块常用消息
export const MESSAGE_MESSAGES = {
  // API层消息
  API: {
    LOADING_NOTIFICATIONS: '正在加载通知...',
    LOADING_CHAT_SESSIONS: '正在加载聊天会话...',
    LOADING_CHAT_MESSAGES: '正在加载聊天记录...',
    SENDING_MESSAGE: '正在发送消息...',
    MARKING_READ: '正在标记已读...',
    LOADING_UNREAD_COUNT: '正在加载未读消息数...',
    LOADING_FAILED: '加载失败',
    SEND_FAILED: '发送失败',
    MARK_READ_FAILED: '标记已读失败',
    USER_INFO_LOADING_FAILED: '未能获取到用户信息'
  },
  
  // 业务层消息
  BUSINESS: {
    MESSAGE_SENT: '消息发送成功',
    NOTIFICATION_READ: '通知已标记为已读',
    ALL_NOTIFICATIONS_READ: '所有通知已标记为已读',
    NOTIFICATION_DELETED: '通知已删除',
    ALL_NOTIFICATIONS_DELETED: '所有通知已清空',
    SESSION_DELETED: '会话已删除',
    MESSAGE_RECALL_SUCCESS: '消息已撤回',
    MESSAGE_RECALL_FAILED: '消息撤回失败，已超过撤回时间',
    NOTIFICATION_TOP_SUCCESS: '通知已置顶',
    NOTIFICATION_UNTOP_SUCCESS: '通知已取消置顶',
    IMAGE_UPLOAD_SUCCESS: '图片上传成功',
    IMAGE_UPLOAD_FAILED: '图片上传失败',
    NEW_MESSAGE_RECEIVED: '收到新消息',
    NEW_NOTIFICATION_RECEIVED: '收到新通知',
    USER_INFO_EMPTY: '用户信息不完整'
  },
  
  // UI交互层消息
  UI: {
    MESSAGE_EMPTY: '消息内容不能为空',
    RECIPIENT_REQUIRED: '请选择接收人',
    IMAGE_SIZE_LIMIT: '图片大小不能超过5MB',
    IMAGE_FORMAT_INVALID: '只支持JPG、PNG和GIF格式的图片',
    DELETE_SESSION_CONFIRM: '确定要删除此会话吗？历史消息将无法恢复',
    DELETE_NOTIFICATION_CONFIRM: '确定要删除此通知吗？',
    CLEAR_ALL_NOTIFICATIONS_CONFIRM: '确定要清空所有通知吗？',
    RECALL_CONFIRM: '确定要撤回此消息吗？',
    NOTIFICATION_OPERATED: '操作已处理',
    SEND_EMPTY_MESSAGE: '不能发送空消息',
    USER_DATA_INCOMPLETE: '用户数据加载不完整，请刷新页面或重新登录'
  }
}

// 消息API层消息工具
const apiMessages = {
  // 显示API加载消息
  showLoading(message) {
    messageManager.api.showLoading(message)
  },
  
  // 显示API成功消息
  showSuccess(message) {
    messageManager.api.showSuccess(message)
  },
  
  // 显示API错误消息
  showError(message) {
    messageManager.api.showError(message)
  },
  
  // 预定义的API消息
  showLoadingNotifications() {
    this.showLoading(MESSAGE_MESSAGES.API.LOADING_NOTIFICATIONS)
  },
  
  showLoadingChatSessions() {
    this.showLoading(MESSAGE_MESSAGES.API.LOADING_CHAT_SESSIONS)
  },
  
  showLoadingChatMessages() {
    this.showLoading(MESSAGE_MESSAGES.API.LOADING_CHAT_MESSAGES)
  },
  
  showSendingMessage() {
    this.showLoading(MESSAGE_MESSAGES.API.SENDING_MESSAGE)
  },
  
  showMarkingRead() {
    this.showLoading(MESSAGE_MESSAGES.API.MARKING_READ)
  },
  
  showLoadingUnreadCount() {
    this.showLoading(MESSAGE_MESSAGES.API.LOADING_UNREAD_COUNT)
  },
  
  showLoadingFailed(error) {
    const errorMsg = error?.message || MESSAGE_MESSAGES.API.LOADING_FAILED
    this.showError(errorMsg)
  },
  
  showSendFailed(error) {
    const errorMsg = error?.message || MESSAGE_MESSAGES.API.SEND_FAILED
    this.showError(errorMsg)
  },
  
  showMarkReadFailed(error) {
    const errorMsg = error?.message || MESSAGE_MESSAGES.API.MARK_READ_FAILED
    this.showError(errorMsg)
  },
  
  showUserInfoLoadingFailed(error) {
    const errorMsg = error?.message || MESSAGE_MESSAGES.API.USER_INFO_LOADING_FAILED
    this.showError(errorMsg)
  }
}

// 消息业务层消息工具
const businessMessages = {
  // 显示业务成功消息
  showSuccess(message) {
    messageManager.business.showSuccess(message)
  },
  
  // 显示业务错误消息
  showError(message) {
    messageManager.business.showError(message)
  },
  
  // 显示业务警告消息
  showWarning(message) {
    messageManager.business.showWarning(message)
  },
  
  // 显示业务信息消息
  showInfo(message) {
    messageManager.business.showInfo(message)
  },
  
  // 预定义的业务消息
  showMessageSent() {
    this.showSuccess(MESSAGE_MESSAGES.BUSINESS.MESSAGE_SENT)
  },
  
  showNotificationRead() {
    this.showSuccess(MESSAGE_MESSAGES.BUSINESS.NOTIFICATION_READ)
  },
  
  showAllNotificationsRead() {
    this.showSuccess(MESSAGE_MESSAGES.BUSINESS.ALL_NOTIFICATIONS_READ)
  },
  
  showNotificationDeleted() {
    this.showSuccess(MESSAGE_MESSAGES.BUSINESS.NOTIFICATION_DELETED)
  },
  
  showAllNotificationsDeleted() {
    this.showSuccess(MESSAGE_MESSAGES.BUSINESS.ALL_NOTIFICATIONS_DELETED)
  },
  
  showSessionDeleted() {
    this.showSuccess(MESSAGE_MESSAGES.BUSINESS.SESSION_DELETED)
  },
  
  showMessageRecallSuccess() {
    this.showSuccess(MESSAGE_MESSAGES.BUSINESS.MESSAGE_RECALL_SUCCESS)
  },
  
  showMessageRecallFailed() {
    this.showWarning(MESSAGE_MESSAGES.BUSINESS.MESSAGE_RECALL_FAILED)
  },
  
  showNotificationTopSuccess() {
    this.showSuccess(MESSAGE_MESSAGES.BUSINESS.NOTIFICATION_TOP_SUCCESS)
  },
  
  showNotificationUntopSuccess() {
    this.showSuccess(MESSAGE_MESSAGES.BUSINESS.NOTIFICATION_UNTOP_SUCCESS)
  },
  
  showImageUploadSuccess() {
    this.showSuccess(MESSAGE_MESSAGES.BUSINESS.IMAGE_UPLOAD_SUCCESS)
  },
  
  showImageUploadFailed(error) {
    const errorMsg = error?.message || MESSAGE_MESSAGES.BUSINESS.IMAGE_UPLOAD_FAILED
    this.showError(errorMsg)
  },
  
  showNewMessageReceived(from) {
    const message = from 
      ? `来自 ${from} 的新消息`
      : MESSAGE_MESSAGES.BUSINESS.NEW_MESSAGE_RECEIVED
    this.showInfo(message)
  },
  
  showNewNotificationReceived(title) {
    const message = title 
      ? `新通知: ${title}`
      : MESSAGE_MESSAGES.BUSINESS.NEW_NOTIFICATION_RECEIVED
    this.showInfo(message)
  },
  
  showUserInfoEmpty() {
    this.showWarning(MESSAGE_MESSAGES.BUSINESS.USER_INFO_EMPTY)
  }
}

// 消息UI交互层消息工具
const uiMessages = {
  // 显示UI成功消息
  showSuccess(message) {
    messageManager.ui.showSuccess(message)
  },
  
  // 显示UI错误消息
  showError(message) {
    messageManager.ui.showError(message)
  },
  
  // 显示UI警告消息
  showWarning(message) {
    messageManager.ui.showWarning(message)
  },
  
  // 显示UI信息消息
  showInfo(message) {
    messageManager.ui.showInfo(message)
  },
  
  // 显示确认消息
  showConfirm(message) {
    return messageManager.ui.showConfirm(message)
  },
  
  // 预定义的UI消息
  showMessageEmpty() {
    this.showWarning(MESSAGE_MESSAGES.UI.MESSAGE_EMPTY)
  },
  
  showRecipientRequired() {
    this.showWarning(MESSAGE_MESSAGES.UI.RECIPIENT_REQUIRED)
  },
  
  showImageSizeLimit() {
    this.showWarning(MESSAGE_MESSAGES.UI.IMAGE_SIZE_LIMIT)
  },
  
  showImageFormatInvalid() {
    this.showWarning(MESSAGE_MESSAGES.UI.IMAGE_FORMAT_INVALID)
  },
  
  showDeleteSessionConfirm() {
    return this.showConfirm(MESSAGE_MESSAGES.UI.DELETE_SESSION_CONFIRM)
  },
  
  showDeleteNotificationConfirm() {
    return this.showConfirm(MESSAGE_MESSAGES.UI.DELETE_NOTIFICATION_CONFIRM)
  },
  
  showClearAllNotificationsConfirm() {
    return this.showConfirm(MESSAGE_MESSAGES.UI.CLEAR_ALL_NOTIFICATIONS_CONFIRM)
  },
  
  showRecallConfirm() {
    return this.showConfirm(MESSAGE_MESSAGES.UI.RECALL_CONFIRM)
  },
  
  showNotificationOperated() {
    this.showInfo(MESSAGE_MESSAGES.UI.NOTIFICATION_OPERATED)
  },
  
  showSendEmptyMessage() {
    this.showWarning(MESSAGE_MESSAGES.UI.SEND_EMPTY_MESSAGE)
  },
  
  showUserDataIncomplete() {
    this.showWarning(MESSAGE_MESSAGES.UI.USER_DATA_INCOMPLETE)
  }
}

// 导出所有消息工具
export default {
  api: apiMessages,
  business: businessMessages,
  ui: uiMessages,
  MESSAGE_MESSAGES
}
