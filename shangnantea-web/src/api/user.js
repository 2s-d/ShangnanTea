import request from './index'
import { API } from './apiConstants'

/**
 * 用户模块API - 包含用户相关的所有API请求
 */

// === 用户认证相关API ===

/**
 * 用户登录
 * @param {Object} loginData 登录数据 {username, password}
 * @returns {Promise} 登录结果
 */
export function login(loginData) {
  return request({
    url: API.USER.LOGIN,
    method: 'post',
    data: loginData
  })
}

/**
 * 用户注册
 * @param {Object} registerData 注册数据
 * @returns {Promise} 注册结果
 */
export function register(registerData) {
  return request({
    url: API.USER.REGISTER,
    method: 'post',
    data: registerData
  })
}

/**
 * 退出登录
 * @returns {Promise} 退出结果
 */
export function logout() {
  return request({
    url: API.USER.LOGOUT,
    method: 'post'
  })
}

/**
 * 获取当前登录用户信息
 * @returns {Promise} 用户信息
 */
export function getCurrentUser() {
  return request({
    url: API.USER.INFO + 'me',
    method: 'get'
  })
}

/**
 * 刷新令牌
 * @returns {Promise} 新的令牌
 */
export function refreshToken() {
  return request({
    url: API.USER.REFRESH,
    method: 'post'
  })
}

// === 用户信息相关API ===

/**
 * 获取用户信息
 * @param {String} userId 用户ID，不传则获取当前用户
 * @returns {Promise} 用户信息
 */
export function getUserInfo(userId) {
  return request({
    url: userId ? `${API.USER.INFO}${userId}` : `${API.USER.INFO}me`,
    method: 'get'
  })
}

/**
 * 更新用户信息
 * @param {Object} userData 用户数据
 * @returns {Promise} 更新结果
 */
export function updateUserInfo(userData) {
  return request({
    url: API.USER.INFO,
    method: 'put',
    data: userData
  })
}

/**
 * 修改密码
 * @param {Object} passwordData 密码数据 {oldPassword, newPassword}
 * @returns {Promise} 修改结果
 */
export function changePassword(passwordData) {
  return request({
    url: API.USER.PASSWORD,
    method: 'put',
    data: passwordData
  })
}

// === 用户地址相关API ===

/**
 * 获取用户地址列表
 * @returns {Promise} 地址列表
 */
export function getAddressList() {
  return request({
    url: API.USER.ADDRESSES,
    method: 'get'
  })
}

/**
 * 添加收货地址
 * @param {Object} addressData 地址数据
 * @returns {Promise} 添加结果
 */
export function addAddress(addressData) {
  return request({
    url: API.USER.ADDRESSES,
    method: 'post',
    data: addressData
  })
}

/**
 * 更新收货地址
 * @param {String} addressId 地址ID
 * @param {Object} addressData 地址数据
 * @returns {Promise} 更新结果
 */
export function updateAddress(addressId, addressData) {
  return request({
    url: `${API.USER.ADDRESSES}/${addressId}`,
    method: 'put',
    data: addressData
  })
}

/**
 * 删除收货地址
 * @param {String} addressId 地址ID
 * @returns {Promise} 删除结果
 */
export function deleteAddress(addressId) {
  return request({
    url: `${API.USER.ADDRESSES}/${addressId}`,
    method: 'delete'
  })
}

/**
 * 设置默认收货地址
 * @param {String} addressId 地址ID
 * @returns {Promise} 设置结果
 */
export function setDefaultAddress(addressId) {
  return request({
    url: `${API.USER.ADDRESSES}/${addressId}/default`,
    method: 'put'
  })
}

// === 商家认证相关API ===

/**
 * 提交商家认证申请
 * @param {Object} certificationData 认证数据
 * @returns {Promise} 提交结果
 */
export function submitShopCertification(certificationData) {
  return request({
    url: API.USER.SHOP_CERTIFICATION,
    method: 'post',
    data: certificationData
  })
}

/**
 * 获取商家认证状态
 * @returns {Promise} 认证状态
 */
export function getShopCertificationStatus() {
  return request({
    url: API.USER.SHOP_CERTIFICATION,
    method: 'get'
  })
} 