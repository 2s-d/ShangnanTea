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
    url: API.CART.LIST || API.ORDER.CART,
    method: 'get'
  })
}

/**
 * 添加商品到购物车
 * @param {Object} data 购物车商品数据 {teaId, quantity}
 * @returns {Promise} 添加结果
 */
export function addToCart(data) {
  return request({
    url: API.CART.ADD || API.ORDER.CART_ADD,
    method: 'post',
    data
  })
}

/**
 * 更新购物车商品数量
 * @param {Object} data 更新数据 {id, quantity}
 * @returns {Promise} 更新结果
 */
export function updateCartItem(data) {
  return request({
    url: API.CART.UPDATE || API.ORDER.CART_UPDATE,
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
    url: API.CART.REMOVE || API.ORDER.CART_REMOVE,
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
    url: API.CART.CLEAR || API.ORDER.CART_CLEAR,
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
 * 支付订单
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
 * 取消订单
 * @param {number} id 订单ID
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