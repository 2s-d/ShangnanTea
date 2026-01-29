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
 * 创建茶叶分类（仅管理员）
 * @param {Object} data 分类数据 {name, parentId, sortOrder, icon}
 * @returns {Promise} 创建结果
 */
export function createCategory(data) {
  return request({
    url: API.TEA.CATEGORIES,
    method: 'post',
    data
  })
}

/**
 * 更新茶叶分类（仅管理员）
 * @param {number} id 分类ID
 * @param {Object} data 分类数据 {name, parentId, sortOrder, icon}
 * @returns {Promise} 更新结果
 */
export function updateCategory(id, data) {
  return request({
    url: API.TEA.CATEGORIES + '/' + id,
    method: 'put',
    data
  })
}

/**
 * 删除茶叶分类（仅管理员）
 * @param {number} id 分类ID
 * @returns {Promise} 删除结果
 */
export function deleteCategory(id) {
  return request({
    url: API.TEA.CATEGORIES + '/' + id,
    method: 'delete'
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

// ==================== 任务组B：评价系统API ====================

/**
 * 获取茶叶评价列表
 * @param {string} teaId 茶叶ID
 * @param {Object} params 查询参数（page, pageSize）
 * @returns {Promise} 评价列表
 */
export function getTeaReviews(teaId, params) {
  return request({
    url: API.TEA.REVIEWS_LIST.replace('{teaId}', teaId),
    method: 'get',
    params
  })
}

/**
 * 获取茶叶评价统计数据
 * @param {string} teaId 茶叶ID
 * @returns {Promise} 评价统计数据
 */
export function getReviewStats(teaId) {
  return request({
    url: API.TEA.REVIEWS_STATS.replace('{teaId}', teaId),
    method: 'get'
  })
}

/**
 * 商家回复评价
 * @param {string} reviewId 评价ID
 * @param {Object} data 回复数据（reply）
 * @returns {Promise} 回复结果
 */
export function replyReview(reviewId, data) {
  return request({
    url: `${API.TEA.REVIEWS}/${reviewId}/reply`,
    method: 'post',
    data
  })
}

/**
 * 点赞评价
 * @param {string} reviewId 评价ID
 * @returns {Promise} 点赞结果
 */
export function likeReview(reviewId) {
  return request({
    url: `${API.TEA.REVIEWS}/${reviewId}/like`,
    method: 'post'
  })
}

// ==================== 任务组C：规格管理API ====================

/**
 * 获取茶叶规格列表
 * @param {string} teaId 茶叶ID
 * @returns {Promise} 规格列表
 */
export function getTeaSpecifications(teaId) {
  return request({
    url: API.TEA.SPECIFICATIONS.replace('{teaId}', teaId),
    method: 'get'
  })
}

/**
 * 添加规格
 * @param {string} teaId 茶叶ID
 * @param {Object} data 规格数据（spec_name, price, stock, is_default）
 * @returns {Promise} 添加结果
 */
export function addSpecification(teaId, data) {
  return request({
    url: API.TEA.SPECIFICATIONS.replace('{teaId}', teaId),
    method: 'post',
    data
  })
}

/**
 * 更新规格
 * @param {string} specId 规格ID
 * @param {Object} data 规格数据（spec_name, price, stock, is_default）
 * @returns {Promise} 更新结果
 */
export function updateSpecification(specId, data) {
  return request({
    url: API.TEA.SPECIFICATION_DETAIL.replace('{specId}', specId),
    method: 'put',
    data
  })
}

/**
 * 删除规格
 * @param {string} specId 规格ID
 * @returns {Promise} 删除结果
 */
export function deleteSpecification(specId) {
  return request({
    url: API.TEA.SPECIFICATION_DETAIL.replace('{specId}', specId),
    method: 'delete'
  })
}

/**
 * 设置默认规格
 * @param {string} specId 规格ID
 * @returns {Promise} 设置结果
 */
export function setDefaultSpecification(specId) {
  return request({
    url: `${API.TEA.SPECIFICATION_DETAIL.replace('{specId}', specId)}/default`,
    method: 'put'
  })
}

/**
 * 任务组D：图片管理相关API
 */

/**
 * 上传茶叶图片
 * @param {string} teaId 茶叶ID
 * @param {FormData} formData 包含图片文件的FormData
 * @returns {Promise} 上传结果（包含图片列表）
 */
export function uploadTeaImages(teaId, formData) {
  return request({
    url: API.TEA.IMAGES.replace('{teaId}', teaId),
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 删除茶叶图片
 * @param {string} imageId 图片ID
 * @returns {Promise} 删除结果
 */
export function deleteTeaImage(imageId) {
  return request({
    url: API.TEA.IMAGE_DETAIL.replace('{imageId}', imageId),
    method: 'delete'
  })
}

/**
 * 更新图片顺序
 * @param {Object} data 包含teaId和orders数组的数据
 * @returns {Promise} 更新结果
 */
export function updateImageOrder(data) {
  return request({
    url: API.TEA.IMAGE_ORDER,
    method: 'put',
    data
  })
}

/**
 * 设置主图
 * @param {string} imageId 图片ID
 * @returns {Promise} 设置结果
 */
export function setMainImage(imageId) {
  return request({
    url: `${API.TEA.IMAGE_DETAIL.replace('{imageId}', imageId)}/main`,
    method: 'put'
  })
}

/**
 * 任务组E：状态管理相关API
 */

/**
 * 更新茶叶状态（上架/下架）
 * @param {string} teaId 茶叶ID
 * @param {Object} data 包含status的状态数据
 * @returns {Promise} 更新结果
 */
export function toggleTeaStatus(teaId, data) {
  return request({
    url: API.TEA.STATUS_UPDATE.replace('{teaId}', teaId),
    method: 'put',
    data
  })
}

/**
 * 批量更新茶叶状态（上架/下架）
 * @param {Object} data 包含teaIds和status的批量数据
 * @returns {Promise} 更新结果
 */
export function batchToggleTeaStatus(data) {
  return request({
    url: API.TEA.BATCH_STATUS_UPDATE,
    method: 'put',
    data
  })
}

/**
 * 任务组F：推荐功能相关API
 */

/**
 * 获取推荐茶叶
 * @param {Object} params 推荐参数 { type: 'random'|'similar'|'popular', teaId?: string, count?: number }
 * @returns {Promise} 推荐茶叶列表
 */
export function getRecommendTeas(params) {
  return request({
    url: API.TEA.RECOMMEND,
    method: 'get',
    params
  })
} 