import request from './index'
import { API } from './apiConstants'

/**
 * 获取商店列表
 * @param {Object} params 查询参数
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
 * @returns {Promise} 商店详情
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
 * 获取商店销售的茶叶
 * @param {number} shopId 商店ID
 * @param {Object} params 查询参数
 * @returns {Promise} 商店茶叶列表
 */
export function getShopTeas(shopId, params) {
  return request({
    url: API.SHOP.TEAS,
    method: 'get',
    params: {
      shopId,
      ...params
    }
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