/**
 * 茶叶模块消息工具
 * 
 * 分类说明：
 * - SUCCESS: 操作成功反馈
 * - ERROR: 操作失败/错误提示
 * - PROMPT: 用户提示（表单验证+确认）
 */

import { successMessage, errorMessage, promptMessage } from './messageManager'

// 茶叶模块消息常量
export const TEA_MESSAGES = {
  // 成功消息
  SUCCESS: {
    // 列表页
    LIST_LOADED: '茶叶列表加载成功',
    // 详情页
    DETAIL_LOADED: '茶叶详情加载成功',
    ADDED_TO_FAVORITES: '已收藏',
    REMOVED_FROM_FAVORITES: '已取消收藏',
    ADDED_TO_CART: '成功加入购物车',
    REPLY_SUCCESS: '回复成功',
    // 管理页
    TEA_ON_SHELF: '上架成功',
    TEA_OFF_SHELF: '下架成功',
    BATCH_ON_SHELF: '批量上架成功',
    BATCH_OFF_SHELF: '批量下架成功',
    TEA_DELETE: '删除成功',
    TEA_UPDATE: '茶叶更新成功',
    TEA_CREATE: '茶叶添加成功',
    CATEGORY_DELETE: '分类删除成功',
    CATEGORY_UPDATE: '分类更新成功',
    CATEGORY_CREATE: '分类创建成功',
    // 卡片组件
    CARD_ADDED_TO_CART: '已添加到购物车'
  },
  
  // 错误消息
  ERROR: {
    // 列表页
    LIST_FAILED: '加载茶叶数据失败',
    // 详情页
    DETAIL_FAILED: '加载茶叶详情失败',
    REPLY_FAILED: '回复失败',
    LIKE_FAILED: '点赞失败',
    FAVORITE_FAILED: '操作失败',
    CART_FAILED: '加入购物车失败',
    BUY_FAILED: '立即购买失败',
    // 管理页
    TEA_ON_SHELF_FAILED: '上架失败',
    TEA_OFF_SHELF_FAILED: '下架失败',
    BATCH_ON_SHELF_FAILED: '批量上架失败',
    BATCH_OFF_SHELF_FAILED: '批量下架失败',
    TEA_DELETE_FAILED: '删除失败',
    TEA_SUBMIT_FAILED: '操作失败',
    CATEGORY_DELETE_FAILED: '删除分类失败',
    CATEGORY_SUBMIT_FAILED: '操作失败',
    // 卡片组件
    CARD_ADD_FAILED: '添加失败，请重试'
  },
  
  // 提示消息
  PROMPT: {
    // 详情页
    REPLY_EMPTY: '请输入回复内容',
    SOLD_OUT: '该商品已售罄',
    SELECT_SPEC: '请先选择规格',
    // 管理页
    SELECT_ON_SHELF: '请选择要上架的茶叶',
    SELECT_OFF_SHELF: '请选择要下架的茶叶',
    FORM_INVALID: '请正确填写表单内容',
    SPEC_REQUIRED: '请至少添加一个规格',
    SPEC_INCOMPLETE: '请填写完整的规格名称和价格',
    DEFAULT_SPEC_REQUIRED: '请指定一个默认规格',
    IMAGE_REQUIRED: '请至少上传一张商品图片',
    SUBMITTING: '正在提交数据，请稍候...'
  }
}

// 成功消息函数
export const teaSuccessMessages = {
  // 列表页
  showListLoaded(count) {
    successMessage.show(count ? `成功获取${count}款茶叶` : TEA_MESSAGES.SUCCESS.LIST_LOADED)
  },
  // 详情页
  showDetailLoaded(teaName) {
    successMessage.show(teaName ? `"${teaName}"详情获取成功` : TEA_MESSAGES.SUCCESS.DETAIL_LOADED)
  },
  showAddedToFavorites() {
    successMessage.show(TEA_MESSAGES.SUCCESS.ADDED_TO_FAVORITES)
  },
  showRemovedFromFavorites() {
    successMessage.show(TEA_MESSAGES.SUCCESS.REMOVED_FROM_FAVORITES)
  },
  showAddedToCart() {
    successMessage.show(TEA_MESSAGES.SUCCESS.ADDED_TO_CART)
  },
  showReplySuccess() {
    successMessage.show(TEA_MESSAGES.SUCCESS.REPLY_SUCCESS)
  },
  // 管理页
  showTeaOnShelf() {
    successMessage.show(TEA_MESSAGES.SUCCESS.TEA_ON_SHELF)
  },
  showTeaOffShelf() {
    successMessage.show(TEA_MESSAGES.SUCCESS.TEA_OFF_SHELF)
  },
  showBatchOnShelf() {
    successMessage.show(TEA_MESSAGES.SUCCESS.BATCH_ON_SHELF)
  },
  showBatchOffShelf() {
    successMessage.show(TEA_MESSAGES.SUCCESS.BATCH_OFF_SHELF)
  },
  showTeaDeleted() {
    successMessage.show(TEA_MESSAGES.SUCCESS.TEA_DELETE)
  },
  showTeaUpdated() {
    successMessage.show(TEA_MESSAGES.SUCCESS.TEA_UPDATE)
  },
  showTeaCreated() {
    successMessage.show(TEA_MESSAGES.SUCCESS.TEA_CREATE)
  },
  showCategoryDeleted() {
    successMessage.show(TEA_MESSAGES.SUCCESS.CATEGORY_DELETE)
  },
  showCategoryUpdated() {
    successMessage.show(TEA_MESSAGES.SUCCESS.CATEGORY_UPDATE)
  },
  showCategoryCreated() {
    successMessage.show(TEA_MESSAGES.SUCCESS.CATEGORY_CREATE)
  },
  // 卡片组件
  showCardAddedToCart() {
    successMessage.show(TEA_MESSAGES.SUCCESS.CARD_ADDED_TO_CART)
  }
}

// 错误消息函数
export const teaErrorMessages = {
  // 列表页
  showListFailed(reason) {
    errorMessage.show(reason || TEA_MESSAGES.ERROR.LIST_FAILED)
  },
  // 详情页
  showDetailFailed(reason) {
    errorMessage.show(reason || TEA_MESSAGES.ERROR.DETAIL_FAILED)
  },
  showReplyFailed(reason) {
    errorMessage.show(reason || TEA_MESSAGES.ERROR.REPLY_FAILED)
  },
  showLikeFailed(reason) {
    errorMessage.show(reason || TEA_MESSAGES.ERROR.LIKE_FAILED)
  },
  showFavoriteFailed(reason) {
    errorMessage.show(reason || TEA_MESSAGES.ERROR.FAVORITE_FAILED)
  },
  showCartFailed(reason) {
    errorMessage.show(reason || TEA_MESSAGES.ERROR.CART_FAILED)
  },
  showBuyFailed(reason) {
    errorMessage.show(reason || TEA_MESSAGES.ERROR.BUY_FAILED)
  },
  // 管理页
  showTeaOnShelfFailed(reason) {
    errorMessage.show(reason || TEA_MESSAGES.ERROR.TEA_ON_SHELF_FAILED)
  },
  showTeaOffShelfFailed(reason) {
    errorMessage.show(reason || TEA_MESSAGES.ERROR.TEA_OFF_SHELF_FAILED)
  },
  showBatchOnShelfFailed(reason) {
    errorMessage.show(reason || TEA_MESSAGES.ERROR.BATCH_ON_SHELF_FAILED)
  },
  showBatchOffShelfFailed(reason) {
    errorMessage.show(reason || TEA_MESSAGES.ERROR.BATCH_OFF_SHELF_FAILED)
  },
  showTeaDeleteFailed(reason) {
    errorMessage.show(reason || TEA_MESSAGES.ERROR.TEA_DELETE_FAILED)
  },
  showTeaSubmitFailed(reason) {
    errorMessage.show(reason || TEA_MESSAGES.ERROR.TEA_SUBMIT_FAILED)
  },
  showCategoryDeleteFailed(reason) {
    errorMessage.show(reason || TEA_MESSAGES.ERROR.CATEGORY_DELETE_FAILED)
  },
  showCategorySubmitFailed(reason) {
    errorMessage.show(reason || TEA_MESSAGES.ERROR.CATEGORY_SUBMIT_FAILED)
  },
  // 卡片组件
  showCardAddFailed(reason) {
    errorMessage.show(reason || TEA_MESSAGES.ERROR.CARD_ADD_FAILED)
  }
}

// 提示消息函数
export const teaPromptMessages = {
  // 详情页
  showReplyEmpty() {
    promptMessage.show(TEA_MESSAGES.PROMPT.REPLY_EMPTY)
  },
  showSoldOut() {
    promptMessage.show(TEA_MESSAGES.PROMPT.SOLD_OUT)
  },
  showSelectSpec() {
    promptMessage.show(TEA_MESSAGES.PROMPT.SELECT_SPEC)
  },
  // 管理页
  showSelectOnShelf() {
    promptMessage.show(TEA_MESSAGES.PROMPT.SELECT_ON_SHELF)
  },
  showSelectOffShelf() {
    promptMessage.show(TEA_MESSAGES.PROMPT.SELECT_OFF_SHELF)
  },
  showFormInvalid() {
    promptMessage.show(TEA_MESSAGES.PROMPT.FORM_INVALID)
  },
  showSpecRequired() {
    promptMessage.show(TEA_MESSAGES.PROMPT.SPEC_REQUIRED)
  },
  showSpecIncomplete() {
    promptMessage.show(TEA_MESSAGES.PROMPT.SPEC_INCOMPLETE)
  },
  showDefaultSpecRequired() {
    promptMessage.show(TEA_MESSAGES.PROMPT.DEFAULT_SPEC_REQUIRED)
  },
  showImageRequired() {
    promptMessage.show(TEA_MESSAGES.PROMPT.IMAGE_REQUIRED)
  },
  showSubmitting() {
    promptMessage.show(TEA_MESSAGES.PROMPT.SUBMITTING)
  }
}

// 默认导出
export default {
  success: teaSuccessMessages,
  error: teaErrorMessages,
  prompt: teaPromptMessages,
  TEA_MESSAGES
}
