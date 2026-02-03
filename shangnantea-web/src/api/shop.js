import request from './index'
import { API } from './apiConstants'

/**
 * 获取商店列表
 * 任务组A：支持搜索/筛选/排序参数
 * @param {Object} params 查询参数 { page, size, keyword, rating, salesCount, region, sortBy, sortOrder }
 * @returns {Promise} 商店列表
 */
export function getShops(params) {
  return request({
    url: API.SHOP.LIST,
    method: 'get',
    params
  })
}

/**
 * 获取商店详情
 * @param {number} id 商店ID
 * @returns {Promise} 商店详情，包含 isFollowed 字段（当前用户是否已关注该店铺）
 */
export function getShopDetail(id) {
  return request({
    url: API.SHOP.DETAIL + id,
    method: 'get'
  })
}

/**
 * 获取当前商家的店铺信息
 * @returns {Promise} 我的商店信息
 */
export function getMyShop() {
  return request({
    url: API.SHOP.MY,
    method: 'get'
  })
}

/**
 * 创建店铺（商家认证通过后自动创建）
 * @param {Object} shopData 店铺数据（可选）
 * @returns {Promise} 创建结果
 */
export function createShop(shopData = {}) {
  return request({
    url: API.SHOP.LIST, // POST /shop
    method: 'post',
    data: shopData
  })
}

/**
 * 获取商店销售的茶叶
 * @param {string} shopId 商店ID
 * @param {Object} params 查询参数（page, size等）
 * @returns {Promise} 商店茶叶列表
 */
export function getShopTeas(shopId, params = {}) {
  return request({
    url: API.SHOP.DETAIL + shopId + '/teas',
    method: 'get',
    params
  })
}

/**
 * 更新商店信息（商家）
 * @param {Object} data 商店数据
 * @returns {Promise} 更新结果
 */
export function updateShop(data) {
  return request({
    url: API.SHOP.DETAIL + data.id,
    method: 'put',
    data
  })
}

/**
 * 任务组C：店铺茶叶管理相关API
 */

/**
 * 添加茶叶到店铺
 * @param {string} shopId 店铺ID
 * @param {Object} teaData 茶叶数据
 * @returns {Promise} 添加结果
 */
export function addShopTea(shopId, teaData) {
  return request({
    url: API.SHOP.DETAIL + shopId + '/teas',
    method: 'post',
    data: teaData
  })
}

/**
 * 更新店铺茶叶
 * @param {string} teaId 茶叶ID
 * @param {Object} teaData 茶叶数据
 * @returns {Promise} 更新结果
 */
export function updateShopTea(teaId, teaData) {
  return request({
    url: API.SHOP.TEAS + '/' + teaId,
    method: 'put',
    data: teaData
  })
}

/**
 * 删除店铺茶叶
 * @param {string} teaId 茶叶ID
 * @returns {Promise} 删除结果
 */
export function deleteShopTea(teaId) {
  return request({
    url: API.SHOP.TEAS + '/' + teaId,
    method: 'delete'
  })
}

/**
 * 茶叶上下架
 * @param {string} teaId 茶叶ID
 * @param {number} status 状态（1-上架，0-下架）
 * @returns {Promise} 更新结果
 */
export function toggleShopTeaStatus(teaId, status) {
  return request({
    url: API.SHOP.TEAS + '/' + teaId + '/status',
    method: 'put',
    data: { status }
  })
}

/**
 * 任务组D：获取店铺统计数据
 * @param {string} shopId 店铺ID
 * @param {Object} params { startDate, endDate }
 */
export function getShopStatistics(shopId, params = {}) {
  return request({
    url: `${API.SHOP.DETAIL}${shopId}/statistics`,
    method: 'get',
    params
  })
}

/**
 * 任务组D：上传店铺Logo
 * @param {string} shopId 店铺ID
 * @param {File|FormData} file 文件
 */
export function uploadShopLogo(shopId, file) {
  const formData = file instanceof FormData ? file : new FormData()
  if (!(file instanceof FormData)) {
    formData.append('file', file)
  }
  return request({
    url: `${API.SHOP.DETAIL}${shopId}/logo`,
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 任务组B：店铺Banner管理相关API
 */

/**
 * 获取店铺Banner列表
 * @param {string} shopId 店铺ID
 * @returns {Promise} Banner列表
 */
export function getShopBanners(shopId) {
  return request({
    url: API.SHOP.DETAIL + shopId + '/banners',
    method: 'get'
  })
}

/**
 * 上传/创建店铺Banner
 * @param {string} shopId 店铺ID
 * @param {FormData|Object} bannerData Banner数据（包含image、link_url、title等）
 * @returns {Promise} 创建后的Banner
 */
export function uploadBanner(shopId, bannerData) {
  return request({
    url: API.SHOP.DETAIL + shopId + '/banners',
    method: 'post',
    data: bannerData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 更新店铺Banner
 * @param {string} bannerId Banner ID
 * @param {Object} bannerData Banner数据
 * @returns {Promise} 更新后的Banner
 */
export function updateBanner(bannerId, bannerData) {
  return request({
    url: API.SHOP.BANNERS + '/' + bannerId,
    method: 'put',
    data: bannerData
  })
}

/**
 * 删除店铺Banner
 * @param {string} bannerId Banner ID
 * @returns {Promise} 删除结果
 */
export function deleteBanner(bannerId) {
  return request({
    url: API.SHOP.BANNERS + '/' + bannerId,
    method: 'delete'
  })
}

/**
 * 更新Banner顺序
 * @param {Array<Object>} orderData 排序数据 [{id: 'banner1', order: 1}, ...]
 * @returns {Promise} 更新结果
 */
export function updateBannerOrder(orderData) {
  return request({
    url: API.SHOP.BANNER_ORDER,
    method: 'put',
    data: { orders: orderData } // 后端期望orders字段
  })
}

/**
 * 任务组B：店铺公告管理相关API
 */

/**
 * 获取店铺公告列表
 * @param {string} shopId 店铺ID
 * @returns {Promise} 公告列表
 */
export function getShopAnnouncements(shopId) {
  return request({
    url: API.SHOP.DETAIL + shopId + '/announcements',
    method: 'get'
  })
}

/**
 * 创建店铺公告
 * @param {string} shopId 店铺ID
 * @param {Object} announcementData 公告数据（title、content、is_top等）
 * @returns {Promise} 创建后的公告
 */
export function createAnnouncement(shopId, announcementData) {
  return request({
    url: API.SHOP.DETAIL + shopId + '/announcements',
    method: 'post',
    data: announcementData
  })
}

/**
 * 更新店铺公告
 * @param {string} announcementId 公告ID
 * @param {Object} announcementData 公告数据
 * @returns {Promise} 更新后的公告
 */
export function updateAnnouncement(announcementId, announcementData) {
  return request({
    url: API.SHOP.ANNOUNCEMENTS + '/' + announcementId,
    method: 'put',
    data: announcementData
  })
}

/**
 * 删除店铺公告
 * @param {string} announcementId 公告ID
 * @returns {Promise} 删除结果
 */
export function deleteAnnouncement(announcementId) {
  return request({
    url: API.SHOP.ANNOUNCEMENTS + '/' + announcementId,
    method: 'delete'
  })
} 

/**
 * 任务组F：店铺关注相关API
 * ⚠️ 已删除：followShop, unfollowShop, checkFollowStatus
 * 说明：店铺关注功能已统一使用用户模块的通用接口（user.js 中的 addFollow/removeFollow）
 * 店铺详情接口（getShopDetail）已包含 isFollowed 字段，无需单独检查关注状态
 */

/**
 * 获取店铺评价列表
 * @param {string} shopId 店铺ID
 * @param {Object} params 查询参数 { page, size }
 * @returns {Promise} 评价列表
 */
export function getShopReviews(shopId, params) {
  return request({
    url: API.SHOP.DETAIL + shopId + '/reviews',
    method: 'get',
    params
  })
}

/**
 * 提交店铺评价
 * @param {string} shopId 店铺ID
 * @param {Object} reviewData 评价数据 { rating, content, images }
 * @returns {Promise} 结果
 */
export function submitShopReview(shopId, reviewData) {
  return request({
    url: API.SHOP.DETAIL + shopId + '/reviews',
    method: 'post',
    data: reviewData
  })
}