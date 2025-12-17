import request from './index'
import { API } from './apiConstants'

/**
 * 获取茶叶列表
 * @param {Object} params 查询参数
 * @returns {Promise} 茶叶列表
 */
export function getTeas(params) {
  return request({
    url: API.TEA.LIST,
    method: 'get',
    params
  })
}

/**
 * 获取茶叶详情
 * @param {number} id 茶叶ID
 * @returns {Promise} 茶叶详情
 */
export function getTeaDetail(id) {
  return request({
    url: API.TEA.DETAIL + id,
    method: 'get'
  })
}

/**
 * 获取茶叶分类
 * @returns {Promise} 茶叶分类列表
 */
export function getTeaCategories() {
  return request({
    url: API.TEA.CATEGORIES,
    method: 'get'
  })
}

/**
 * 添加茶叶（管理员/商家）
 * @param {Object} data 茶叶数据
 * @returns {Promise} 添加结果
 */
export function addTea(data) {
  return request({
    url: API.TEA.LIST,
    method: 'post',
    data
  })
}

/**
 * 更新茶叶信息（管理员/商家）
 * @param {Object} data 茶叶数据
 * @returns {Promise} 更新结果
 */
export function updateTea(data) {
  return request({
    url: API.TEA.DETAIL + data.id,
    method: 'put',
    data
  })
}

/**
 * 删除茶叶（管理员/商家）
 * @param {number} id 茶叶ID
 * @returns {Promise} 删除结果
 */
export function deleteTea(id) {
  return request({
    url: API.TEA.DETAIL + id,
    method: 'delete'
  })
} 