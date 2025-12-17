/**
 * 茶叶模块的专用消息工具
 * 
 * 包含茶叶模块的常用消息文本及显示方法
 * 遵循消息三层架构：API层、Vuex业务层、UI交互层
 */

import { apiMessage, vuexMessage, uiMessage } from './messageManager'

// 茶叶模块常用消息定义
export const TEA_MESSAGES = {
  // API层消息
  API: {
    TEA_LIST_SUCCESS: '茶叶列表获取成功',
    TEA_LIST_ERROR: '获取茶叶列表失败',
    TEA_DETAIL_SUCCESS: '茶叶详情获取成功',
    TEA_DETAIL_ERROR: '获取茶叶详情失败',
    TEA_NOT_FOUND: '未找到指定茶叶'
  },
  
  // Vuex业务层消息
  BUSINESS: {
    TEA_ADDED_TO_FAVORITES: '已添加到收藏',
    TEA_REMOVED_FROM_FAVORITES: '已从收藏中移除',
    TEA_ADDED_TO_CART: '已加入购物车',
    TEA_QUANTITY_UPDATED: '数量已更新',
    TEA_RECOMMEND_SUCCESS: '茶叶推荐获取成功',
    TEA_CATEGORY_CHANGE: '茶叶分类已切换',
    TEA_FILTER_APPLIED: '过滤条件已应用'
  },
  
  // UI交互层消息
  UI: {
    TEA_SELECTION_LIMIT: '最多只能选择5种茶叶',
    TEA_COMPARISON_START: '开始对比所选茶叶',
    TEA_SEARCH_EMPTY: '请输入搜索关键词',
    TEA_SORT_CHANGED: '排序方式已更改',
    TEA_IMAGE_LOADING_ERROR: '图片加载失败',
    TEA_PREVIEW_MODE: '进入预览模式'
  }
}

/**
 * 茶叶API层消息函数
 */
export const teaApiMessages = {
  /**
   * 显示茶叶列表获取成功消息
   * @param {number} count 获取的茶叶数量
   */
  showListSuccess(count) {
    apiMessage.success(`成功获取${count}款茶叶`)
  },
  
  /**
   * 显示茶叶列表获取失败消息
   * @param {string} reason 失败原因
   */
  showListError(reason) {
    apiMessage.error(reason || TEA_MESSAGES.API.TEA_LIST_ERROR)
  },
  
  /**
   * 显示茶叶详情获取成功消息
   * @param {string} teaName 茶叶名称
   */
  showDetailSuccess(teaName) {
    apiMessage.success(`"${teaName}"详情获取成功`)
  },
  
  /**
   * 显示茶叶详情获取失败消息
   * @param {string} reason 失败原因
   */
  showDetailError(reason) {
    apiMessage.error(reason || TEA_MESSAGES.API.TEA_DETAIL_ERROR)
  },
  
  /**
   * 显示茶叶未找到消息
   * @param {string} teaId 茶叶ID
   */
  showTeaNotFound(teaId) {
    apiMessage.warning(`未找到ID为"${teaId}"的茶叶`)
  }
}

/**
 * 茶叶业务层消息函数
 */
export const teaBusinessMessages = {
  /**
   * 显示收藏成功消息
   * @param {string} teaName 茶叶名称
   */
  showAddedToFavorites(teaName) {
    vuexMessage.success(`"${teaName}"已添加到收藏`)
  },
  
  /**
   * 显示取消收藏消息
   * @param {string} teaName 茶叶名称
   */
  showRemovedFromFavorites(teaName) {
    vuexMessage.success(`"${teaName}"已从收藏中移除`)
  },
  
  /**
   * 显示添加到购物车消息
   * @param {string} teaName 茶叶名称
   * @param {number} quantity 数量
   */
  showAddedToCart(teaName, quantity = 1) {
    vuexMessage.success(`已将"${teaName}" ${quantity}件加入购物车`)
  },
  
  /**
   * 显示数量更新消息
   * @param {string} teaName 茶叶名称
   * @param {number} quantity 新数量
   */
  showQuantityUpdated(teaName, quantity) {
    vuexMessage.success(`"${teaName}"数量已更新为${quantity}`)
  },
  
  /**
   * 显示获取推荐成功消息
   * @param {number} count 推荐茶叶数量
   */
  showRecommendSuccess(count) {
    vuexMessage.info(`已为您推荐${count}款茶叶`)
  },
  
  /**
   * 显示分类切换消息
   * @param {string} categoryName 新分类名称
   */
  showCategoryChange(categoryName) {
    vuexMessage.info(`已切换到"${categoryName}"分类`)
  },
  
  /**
   * 显示过滤应用消息
   * @param {number} resultCount 过滤后的结果数量
   */
  showFilterApplied(resultCount) {
    vuexMessage.info(`过滤条件已应用，共找到${resultCount}款茶叶`)
  }
}

/**
 * 茶叶UI交互层消息函数
 */
export const teaUIMessages = {
  /**
   * 显示选择上限消息
   */
  showSelectionLimit() {
    uiMessage.warning(TEA_MESSAGES.UI.TEA_SELECTION_LIMIT)
  },
  
  /**
   * 显示开始对比消息
   * @param {number} count 对比茶叶数量
   */
  showComparisonStart(count) {
    uiMessage.success(`开始对比${count}款茶叶`)
  },
  
  /**
   * 显示搜索为空提醒
   */
  showEmptySearch() {
    uiMessage.warning(TEA_MESSAGES.UI.TEA_SEARCH_EMPTY)
  },
  
  /**
   * 显示排序方式变更消息
   * @param {string} sortName 排序方式名称
   */
  showSortChanged(sortName) {
    uiMessage.info(`排序方式已更改为"${sortName}"`)
  },
  
  /**
   * 显示图片加载错误消息
   */
  showImageLoadError() {
    uiMessage.error(TEA_MESSAGES.UI.TEA_IMAGE_LOADING_ERROR)
  },
  
  /**
   * 显示预览模式消息
   */
  showPreviewMode() {
    uiMessage.info(TEA_MESSAGES.UI.TEA_PREVIEW_MODE)
  }
}

export default {
  api: teaApiMessages,
  business: teaBusinessMessages,
  ui: teaUIMessages
} 