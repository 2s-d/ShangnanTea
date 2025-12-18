import request from './index'
import { API } from './apiConstants'

/**
 * 获取消息列表（兼容：历史实现）
 * @param {Object} params 查询参数
 * @returns {Promise} 消息列表
 */
export function getMessages(params) {
  return request({
    url: API.MESSAGE.LIST,
    method: 'get',
    params
  })
}

/**
 * 获取消息详情（兼容：历史实现）
 * @param {number} id 消息ID
 * @returns {Promise} 消息详情
 */
export function getMessageDetail(id) {
  return request({
    url: API.MESSAGE.DETAIL + id,
    method: 'get'
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
 * 删除消息
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
 * 获取系统通知（推荐新接口：notifications）
 * @param {Object} params 查询参数
 * @returns {Promise} 系统通知列表
 */
export function getSystemNotifications(params) {
  return request({
    url: API.MESSAGE.NOTIFICATIONS,
    method: 'get',
    params: {
      ...params,
      type: 'system'
    }
  })
}

/**
 * 获取通知列表（推荐）
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
 * 获取聊天会话列表
 * @returns {Promise} 聊天会话列表
 */
export function getChatSessions() {
  return request({
    url: API.MESSAGE.LIST + '/sessions',
    method: 'get'
  })
}

/**
 * 获取与指定用户的聊天记录
 * @param {number} userId 用户ID
 * @param {Object} params 查询参数
 * @returns {Promise} 聊天记录
 */
export function getChatHistory(userId, params) {
  return request({
    url: API.MESSAGE.LIST + '/history',
    method: 'get',
    params: {
      ...params,
      userId
    }
  })
} 