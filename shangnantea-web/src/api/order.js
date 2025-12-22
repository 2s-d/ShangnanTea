import request from './index'
import { API } from './apiConstants'

/**
 * 订单模块API - 包含订单和购物车相关的所有API请求
 */

// === 购物车相关API ===

/**
 * 获取购物车列表
 * @returns {Promise} 购物车商品列表
 */
export function getCartItems() {
  return request({
    url: API.ORDER.CART,
    method: 'get'
  })
}

/**
 * 添加商品到购物车
 * @param {Object} data 购物车商品数据 {teaId, quantity, specificationId}
 * @returns {Promise} 添加结果
 */
export function addToCart(data) {
  return request({
    url: API.ORDER.CART_ADD,
    method: 'post',
    data
  })
}

/**
 * 更新购物车商品
 * @param {Object} data 更新数据 {id, quantity, specificationId}
 * @returns {Promise} 更新结果
 */
export function updateCartItem(data) {
  return request({
    url: API.ORDER.CART_UPDATE,
    method: 'put',
    data
  })
}

/**
 * 移除购物车商品
 * @param {number} id 购物车项ID
 * @returns {Promise} 移除结果
 */
export function removeFromCart(id) {
  return request({
    url: API.ORDER.CART_REMOVE,
    method: 'delete',
    params: { id }
  })
}

/**
 * 清空购物车
 * @returns {Promise} 清空结果
 */
export function clearCart() {
  return request({
    url: API.ORDER.CART_CLEAR,
    method: 'delete'
  })
}

// === 订单相关API ===

/**
 * 创建订单
 * @param {Object} data 订单数据
 * @returns {Promise} 创建结果
 */
export function createOrder(data) {
  return request({
    url: API.ORDER.CREATE,
    method: 'post',
    data
  })
}

/**
 * 获取订单列表
 * @param {Object} params 查询参数
 * @returns {Promise} 订单列表
 */
export function getOrders(params) {
  return request({
    url: API.ORDER.LIST,
    method: 'get',
    params
  })
}

/**
 * 获取订单详情
 * @param {number} id 订单ID
 * @returns {Promise} 订单详情
 */
export function getOrderDetail(id) {
  return request({
    url: API.ORDER.DETAIL + id,
    method: 'get'
  })
}

/**
 * 任务组A：支付订单
 * @param {Object} data 支付数据 {orderId, paymentMethod}
 * @returns {Promise} 支付结果
 */
export function payOrder(data) {
  return request({
    url: API.ORDER.PAY,
    method: 'post',
    data
  })
}

/**
 * 任务组A：取消订单
 * @param {number|string} id 订单ID
 * @returns {Promise} 取消结果
 */
export function cancelOrder(id) {
  return request({
    url: API.ORDER.CANCEL,
    method: 'post',
    data: { id }
  })
}

/**
 * 确认收货
 * @param {number} id 订单ID
 * @returns {Promise} 确认结果
 */
export function confirmOrder(id) {
  return request({
    url: API.ORDER.CONFIRM,
    method: 'post',
    data: { id }
  })
}

/**
 * 评价订单
 * @param {Object} data 评价数据
 * @returns {Promise} 评价结果
 */
export function reviewOrder(data) {
  return request({
    url: API.ORDER.REVIEW,
    method: 'post',
    data
  })
} 

/**
 * 申请退款（兼容旧路径）
 * @param {Object} data 退款数据 {orderId, reason}
 * @returns {Promise} 申请结果
 */
export function refundOrder(data) {
  return request({
    url: API.ORDER.REFUND,
    method: 'post',
    data
  })
}

// === 退款相关 API ===

/**
 * 审批退款（/order/{id}/refund/process）
 * @param {Object} payload { orderId, approve, reason }
 */
export function processRefund(payload) {
  const { orderId, approve, reason } = payload
  return request({
    url: `${API.ORDER.DETAIL}${orderId}/refund/process`,
    method: 'post',
    data: { approve, reason }
  })
}

/**
 * 获取退款详情（/order/{id}/refund）
 * @param {string} orderId
 */
export function getRefundDetail(orderId) {
  return request({
    url: `${API.ORDER.DETAIL}${orderId}/refund`,
    method: 'get'
  })
}

// === 任务组B：发货与物流相关 API ===

/**
 * 发货（单个订单）
 * @param {Object} payload { id, logisticsCompany, logisticsNumber }
 */
export function shipOrder(payload) {
  const { id, logisticsCompany, logisticsNumber } = payload
  return request({
    url: `${API.ORDER.DETAIL}${id}/ship`,
    method: 'post',
    params: {
      logisticsCompany,
      logisticsNumber
    }
  })
}

/**
 * 批量发货
 * @param {Object} payload { orderIds: string[], logisticsCompany, logisticsNumber }
 */
export function batchShipOrders(payload) {
  return request({
    url: API.ORDER.BATCH_SHIP,
    method: 'post',
    data: payload
  })
}

/**
 * 获取订单物流信息
 * @param {string} id 订单ID
 */
export function getOrderLogistics(id) {
  return request({
    url: `${API.ORDER.DETAIL}${id}/logistics`,
    method: 'get'
  })
}

/**
 * 任务组D：获取订单统计数据
 * @param {Object} params 查询参数 { startDate, endDate, shopId }
 * @returns {Promise} 订单统计数据（概览、趋势、状态分布）
 */
export function getOrderStatistics(params = {}) {
  return request({
    url: API.ORDER.STATISTICS,
    method: 'get',
    params
  })
}

/**
 * 任务组E：导出订单数据
 * @param {Object} params 导出参数 { format, startDate, endDate, status, shopId }
 * @returns {Promise} 文件Blob
 */
export function exportOrders(params = {}) {
  return request({
    url: API.ORDER.EXPORT,
    method: 'get',
    params,
    responseType: 'blob' // 重要：设置为blob以接收文件流
  })
}