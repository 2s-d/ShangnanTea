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
 * 上传头像
 * @param {File} file 头像文件
 * @returns {Promise} 上传结果，包含avatarUrl
 */
export function uploadAvatar(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: API.USER.AVATAR,
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 修改密码
 * @param {Object} passwordData 密码数据 {oldPassword, newPassword, confirmNewPassword}
 * @returns {Promise} 修改结果
 */
export function changePassword(passwordData) {
  return request({
    url: API.USER.PASSWORD,
    method: 'put',
    data: passwordData
  })
}

/**
 * 密码找回
 * @param {Object} resetData 找回数据 {username/phone/email, verificationCode}
 * @returns {Promise} 找回结果
 */
export function resetPassword(resetData) {
  return request({
    url: API.USER.PASSWORD_RESET,
    method: 'post',
    data: resetData
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

// === 用户互动相关API（任务组D） ===

/**
 * 获取关注列表
 * @param {String} type 关注类型（user/shop），可选
 * @returns {Promise} 关注列表
 */
export function getFollowList(type = null) {
  return request({
    url: API.USER.FOLLOWS,
    method: 'get',
    params: type ? { type } : {}
  })
}

/**
 * 添加关注
 * @param {Object} followData 关注数据 {targetId, targetType, targetName?, targetAvatar?}
 * @returns {Promise} 关注结果
 */
export function addFollow(followData) {
  return request({
    url: API.USER.FOLLOWS,
    method: 'post',
    data: followData
  })
}

/**
 * 取消关注
 * @param {String|Number} followId 关注ID
 * @returns {Promise} 删除结果
 */
export function removeFollow(followId) {
  return request({
    url: `${API.USER.FOLLOWS}/${followId}`,
    method: 'delete'
  })
}

/**
 * 获取收藏列表
 * @param {String} type 收藏类型（tea/post/article），可选
 * @returns {Promise} 收藏列表
 */
export function getFavoriteList(type = null) {
  return request({
    url: API.USER.FAVORITES,
    method: 'get',
    params: type ? { type } : {}
  })
}

/**
 * 添加收藏
 * @param {Object} favoriteData 收藏数据 {targetId, targetType, targetName?, targetImage?}
 * @returns {Promise} 收藏结果
 */
export function addFavorite(favoriteData) {
  return request({
    url: API.USER.FAVORITES,
    method: 'post',
    data: favoriteData
  })
}

/**
 * 取消收藏
 * @param {String|Number} favoriteId 收藏ID
 * @returns {Promise} 删除结果
 */
export function removeFavorite(favoriteId) {
  return request({
    url: `${API.USER.FAVORITES}/${favoriteId}`,
    method: 'delete'
  })
}

/**
 * 点赞
 * @param {Object} likeData 点赞数据 {targetId, targetType}
 * @returns {Promise} 点赞结果
 */
export function addLike(likeData) {
  return request({
    url: API.USER.LIKES,
    method: 'post',
    data: likeData
  })
}

/**
 * 取消点赞
 * @param {String|Number} likeId 点赞ID
 * @returns {Promise} 删除结果
 */
export function removeLike(likeId) {
  return request({
    url: `${API.USER.LIKES}/${likeId}`,
    method: 'delete'
  })
}

// === 管理员用户管理相关API（任务组E） ===

/**
 * 获取用户列表（仅管理员）
 * @param {Object} params 查询参数 {keyword, role, status, page, pageSize}
 * @returns {Promise} 用户列表
 */
export function getAdminUserList(params) {
  return request({
    url: API.USER.ADMIN_USERS,
    method: 'get',
    params
  })
}

/**
 * 创建管理员账号（仅管理员）
 * @param {Object} data 管理员数据 {username, password, nickname, email, phone, avatar}
 * @returns {Promise} 创建结果
 */
export function createAdmin(data) {
  return request({
    url: API.USER.ADMIN_USERS,
    method: 'post',
    data
  })
}

/**
 * 更新用户信息（仅管理员，不包括角色）
 * @param {String} userId 用户ID
 * @param {Object} data 用户数据 {nickname, email, phone, status, avatar}
 * @returns {Promise} 更新结果
 */
export function updateUser(userId, data) {
  return request({
    url: `${API.USER.ADMIN_USERS}/${userId}`,
    method: 'put',
    data
  })
}

/**
 * 删除用户（仅管理员）
 * @param {String} userId 用户ID
 * @returns {Promise} 删除结果
 */
export function deleteUser(userId) {
  return request({
    url: `${API.USER.ADMIN_USERS}/${userId}`,
    method: 'delete'
  })
}

/**
 * 启用/禁用用户（仅管理员）
 * @param {String} userId 用户ID
 * @param {Object} data 状态数据 {status}
 * @returns {Promise} 更新结果
 */
export function toggleUserStatus(userId, data) {
  return request({
    url: `${API.USER.ADMIN_USERS}/${userId}/status`,
    method: 'put',
    data
  })
}

/**
 * 获取商家认证申请列表（仅管理员）
 * @returns {Promise} 认证申请列表
 */
export function getCertificationList() {
  return request({
    url: API.USER.ADMIN_CERTIFICATIONS,
    method: 'get'
  })
}

/**
 * 审核认证申请（仅管理员）
 * @param {String|Number} id 认证申请ID
 * @param {Object} data 审核数据 {status, message}
 * @returns {Promise} 审核结果
 */
export function processCertification(id, data) {
  return request({
    url: `${API.USER.ADMIN_CERTIFICATIONS}/${id}`,
    method: 'put',
    data
  })
}

// === 用户偏好设置相关API ===

/**
 * 获取当前用户的偏好设置（主题/展示等）
 * @returns {Promise} 偏好设置对象
 */
export function getUserPreferences() {
  return request({
    url: API.USER.PREFERENCES,
    method: 'get'
  })
}

/**
 * 更新当前用户的偏好设置（主题/展示等）
 * @param {Object} preferences 偏好设置对象
 * @returns {Promise} 更新结果
 */
export function updateUserPreferences(preferences) {
  return request({
    url: API.USER.PREFERENCES,
    method: 'put',
    data: preferences
  })
}