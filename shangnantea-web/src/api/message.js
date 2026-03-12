import request from './index'
import { API } from './apiConstants'

/**
 * 获取联系人列表（包含在线状态）
 * @returns {Promise} 联系人列表（关注的人+店铺，包含在线状态）
 */
export function getContacts() {
  return request({
    url: API.MESSAGE.CONTACTS,
    method: 'get'
  })
}

/**
 * 全局用户搜索（支持ID和昵称搜索）
 * @param {Object} params 查询参数 {keyword, page, pageSize}
 * @returns {Promise} 用户列表（包含昵称、头像、在线状态）
 */
export function searchUsers(params) {
  return request({
    url: API.MESSAGE.SEARCH_USERS,
    method: 'get',
    params
  })
}

/**
 * 发送消息
 * @param {Object} data 消息数据 {receiverId, content, type}
 * @returns {Promise} 发送结果
 */
export function sendMessage(data) {
  return request({
    url: API.MESSAGE.SEND,
    method: 'post',
    data
  })
}

/**
 * 标记消息为已读
 * @param {number|Array} ids 消息ID或ID数组
 * @returns {Promise} 标记结果
 */
export function markAsRead(ids) {
  const messageIds = Array.isArray(ids) ? ids : [ids]
  return request({
    url: API.MESSAGE.READ,
    method: 'post',
    data: { messageIds }
  })
}

/**
 * TODO: 冗余接口，需要删除
 * 删除消息
 * 说明：前端已使用更具体的接口（deleteNotification, batchDeleteNotifications），此接口未被调用
 * @param {number|Array} ids 消息ID或ID数组
 * @returns {Promise} 删除结果
 */
export function deleteMessages(ids) {
  const messageIds = Array.isArray(ids) ? ids : [ids]
  return request({
    url: API.MESSAGE.DELETE,
    method: 'post',
    data: { messageIds }
  })
}

/**
 * 获取未读消息数量
 * @returns {Promise} 未读消息数量
 */
export function getUnreadCount() {
  return request({
    url: API.MESSAGE.UNREAD_COUNT,
    method: 'get'
  })
}

/**
 * 获取通知列表
 * @param {Object} params 查询参数
 * @returns {Promise} 通知列表
 */
export function getNotifications(params) {
  return request({
    url: API.MESSAGE.NOTIFICATIONS,
    method: 'get',
    params
  })
}

/**
 * 获取通知详情
 * @param {number} id 通知ID
 * @returns {Promise} 通知详情
 */
export function getNotificationDetail(id) {
  return request({
    url: API.MESSAGE.NOTIFICATIONS + '/' + id,
    method: 'get'
  })
}

/**
 * 删除通知
 * @param {number} id 通知ID
 * @returns {Promise} 删除结果
 */
export function deleteNotification(id) {
  return request({
    url: API.MESSAGE.NOTIFICATIONS + '/' + id,
    method: 'delete'
  })
}

/**
 * 批量标记通知为已读
 * @param {Array} ids 通知ID数组
 * @returns {Promise} 标记结果
 */
export function batchMarkAsRead(ids) {
  return request({
    url: API.MESSAGE.NOTIFICATIONS + '/batch-read',
    method: 'put',
    data: ids
  })
}

/**
 * 批量删除通知
 * @param {Array} ids 通知ID数组
 * @returns {Promise} 删除结果
 */
export function batchDeleteNotifications(ids) {
  return request({
    url: API.MESSAGE.NOTIFICATIONS + '/batch',
    method: 'delete',
    data: ids
  })
}

/**
 * 获取聊天会话列表
 * @returns {Promise} 聊天会话列表
 */
export function getChatSessions() {
  return request({
    url: API.MESSAGE.LIST_SESSIONS,
    method: 'get'
  })
}

/**
 * 获取聊天记录
 * @param {string} sessionId 会话ID
 * @param {Object} params 查询参数
 * @returns {Promise} 聊天记录
 */
export function getChatHistory(sessionId, params) {
  return request({
    url: API.MESSAGE.LIST_HISTORY,
    method: 'get',
    params: {
      ...params,
      sessionId
    }
  })
}

/**
 * 创建聊天会话 POST /message/sessions
 * 消息页仅用户身份：用户对用户 targetType=private + 对方用户ID；
 * 用户找店铺客服 targetType=customer + 店铺ID
 * @param {Object} data { targetId, targetType: 'private'|'customer' }
 */
export function createChatSession(data) {
  return request({
    url: API.MESSAGE.SESSIONS,
    method: 'post',
    params: data
  })
}

/**
 * 获取用户主页信息
 * @param {string} userId 用户ID
 * @returns {Promise} 用户主页信息，包含 isFollowed 字段（当前用户是否已关注该用户）
 */
export function getUserProfile(userId) {
  return request({
    url: API.MESSAGE.USER_PROFILE.replace('{userId}', userId),
    method: 'get'
  })
}

/**
 * 获取用户动态
 * @param {string} userId 用户ID
 * @returns {Promise} 用户动态
 */
export function getUserDynamic(userId) {
  return request({
    url: API.MESSAGE.USER_DYNAMIC.replace('{userId}', userId),
    method: 'get'
  })
}

/**
 * 获取用户统计数据
 * @param {string} userId 用户ID
 * @returns {Promise} 用户统计数据
 */
export function getUserStatistics(userId) {
  return request({
    url: API.MESSAGE.USER_STATISTICS.replace('{userId}', userId),
    method: 'get'
  })
}

/**
 * 置顶聊天会话
 * @param {string} sessionId 会话ID
 * @returns {Promise} 置顶结果
 */
export function pinChatSession(sessionId) {
  return request({
    url: `${API.MESSAGE.SESSIONS}/${sessionId}/pin`,
    method: 'put'
  })
}

/**
 * 删除聊天会话
 * @param {string} sessionId 会话ID
 * @returns {Promise} 删除结果
 */
export function deleteChatSession(sessionId) {
  return request({
    url: `${API.MESSAGE.SESSIONS}/${sessionId}`,
    method: 'delete'
  })
}

/**
 * 发送图片消息
 * @param {Object} data 图片消息数据 {sessionId, receiverId, image}
 * @returns {Promise} 发送结果
 */
export function sendImageMessage(data) {
  const formData = new FormData()
  formData.append('sessionId', data.sessionId)
  formData.append('receiverId', data.receiverId)
  formData.append('image', data.image)
  
  return request({
    url: API.MESSAGE.MESSAGES_IMAGE,
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 获取用户发布的帖子列表
 * @param {Object} params 查询参数 {page, size, sortBy}
 * @returns {Promise} 帖子列表
 */
export function getUserPosts(params = {}) {
  return request({
    url: API.MESSAGE.USER_POSTS,
    method: 'get',
    params
  })
}

/**
 * 获取用户评价记录
 * @param {Object} params 查询参数 {page, size}
 * @returns {Promise} 评价记录列表
 */
export function getUserReviews(params = {}) {
  return request({
    url: API.MESSAGE.USER_REVIEWS,
    method: 'get',
    params
  })
} 