/**
 * 店铺模块消息工具
 * 
 * 分类说明：
 * - SUCCESS: 操作成功反馈
 * - ERROR: 操作失败/错误提示
 * - PROMPT: 用户提示（表单验证+确认）
 */

import { successMessage, errorMessage, promptMessage } from './messageManager'

// 店铺模块消息常量
export const SHOP_MESSAGES = {
  // 成功消息
  SUCCESS: {
    // 店铺操作
    FOLLOW_SUCCESS: '已关注店铺',
    UNFOLLOW_SUCCESS: '已取消关注',
    REVIEW_SUBMIT_SUCCESS: '评价提交成功',
    
    // 茶叶管理
    TEA_TOGGLE_SUCCESS: '成功', // 动态文本：上架成功/下架成功
    TEA_DELETE_SUCCESS: '删除成功',
    TEA_UPDATE_SUCCESS: '茶叶更新成功',
    TEA_ADD_SUCCESS: '茶叶添加成功',
    
    // Banner管理
    BANNER_UPDATE_SUCCESS: 'Banner更新成功',
    BANNER_ADD_SUCCESS: 'Banner添加成功',
    BANNER_DELETE_SUCCESS: '删除成功',
    BANNER_ORDER_UPDATE_SUCCESS: '排序更新成功',
    
    // 公告管理
    ANNOUNCEMENT_UPDATE_SUCCESS: '公告更新成功',
    ANNOUNCEMENT_ADD_SUCCESS: '公告添加成功',
    ANNOUNCEMENT_DELETE_SUCCESS: '删除成功',
    
    // Logo上传
    LOGO_UPLOAD_SUCCESS: 'Logo上传成功'
  },
  
  // 错误消息
  ERROR: {
    // 店铺操作
    SHOP_INFO_LOAD_FAILED: '加载店铺信息失败',
    SHOP_DATA_LOAD_FAILED: '加载店铺数据失败',
    SHOP_NOT_PLATFORM: '平台直售不是实体店铺，无法查看详情',
    FOLLOW_OPERATION_FAILED: '操作失败',
    REVIEW_LOAD_FAILED: '加载评价失败',
    REVIEW_SUBMIT_FAILED: '提交评价失败',
    SHOP_INFO_NOT_EXIST: '店铺信息不存在',
    
    // 茶叶管理
    TEA_LIST_LOAD_FAILED: '加载茶叶列表失败',
    TEA_TOGGLE_FAILED: '失败', // 动态文本：上架失败/下架失败
    TEA_DELETE_FAILED: '删除失败',
    TEA_SAVE_FAILED: '保存失败',
    
    // Banner管理
    BANNER_LIST_LOAD_FAILED: '加载Banner列表失败',
    BANNER_SAVE_FAILED: '保存失败',
    BANNER_DELETE_FAILED: '删除失败',
    BANNER_ORDER_UPDATE_FAILED: '排序更新失败',
    
    // 公告管理
    ANNOUNCEMENT_LIST_LOAD_FAILED: '加载公告列表失败',
    ANNOUNCEMENT_SAVE_FAILED: '保存失败',
    ANNOUNCEMENT_DELETE_FAILED: '删除失败',
    
    // 统计数据
    STATISTICS_LOAD_FAILED: '加载统计数据失败',
    
    // Logo上传
    LOGO_FORMAT_ERROR: '只能上传 JPG/PNG/WebP 格式的图片!',
    LOGO_SIZE_ERROR: '图片大小不能超过2MB!',
    LOGO_UPLOAD_FAILED: 'Logo上传失败',
    LOGO_UPLOAD_NO_SHOP: '店铺信息不存在，无法上传Logo'
  },
  
  // 提示消息
  PROMPT: {
    // 店铺操作
    SHOP_ID_NOT_EXIST: '店铺ID不存在',
    REVIEW_CONTENT_REQUIRED: '请输入评价内容',
    REVIEW_RATING_REQUIRED: '请选择评分',
    
    // 茶叶管理
    SHOP_INFO_LOAD_FIRST: '请先加载店铺信息',
    SUBMITTING_WAIT: '正在提交数据，请稍候...'
  }
}

// 成功消息函数
export const shopSuccessMessages = {
  // 店铺操作
  showFollowSuccess() {
    successMessage.show(SHOP_MESSAGES.SUCCESS.FOLLOW_SUCCESS)
  },
  showUnfollowSuccess() {
    successMessage.show(SHOP_MESSAGES.SUCCESS.UNFOLLOW_SUCCESS)
  },
  showReviewSubmitSuccess() {
    successMessage.show(SHOP_MESSAGES.SUCCESS.REVIEW_SUBMIT_SUCCESS)
  },
  
  // 茶叶管理
  showTeaToggleSuccess(action) {
    successMessage.show(`${action}${SHOP_MESSAGES.SUCCESS.TEA_TOGGLE_SUCCESS}`)
  },
  showTeaDeleteSuccess() {
    successMessage.show(SHOP_MESSAGES.SUCCESS.TEA_DELETE_SUCCESS)
  },
  showTeaUpdateSuccess() {
    successMessage.show(SHOP_MESSAGES.SUCCESS.TEA_UPDATE_SUCCESS)
  },
  showTeaAddSuccess() {
    successMessage.show(SHOP_MESSAGES.SUCCESS.TEA_ADD_SUCCESS)
  },
  
  // Banner管理
  showBannerUpdateSuccess() {
    successMessage.show(SHOP_MESSAGES.SUCCESS.BANNER_UPDATE_SUCCESS)
  },
  showBannerAddSuccess() {
    successMessage.show(SHOP_MESSAGES.SUCCESS.BANNER_ADD_SUCCESS)
  },
  showBannerDeleteSuccess() {
    successMessage.show(SHOP_MESSAGES.SUCCESS.BANNER_DELETE_SUCCESS)
  },
  showBannerOrderUpdateSuccess() {
    successMessage.show(SHOP_MESSAGES.SUCCESS.BANNER_ORDER_UPDATE_SUCCESS)
  },
  
  // 公告管理
  showAnnouncementUpdateSuccess() {
    successMessage.show(SHOP_MESSAGES.SUCCESS.ANNOUNCEMENT_UPDATE_SUCCESS)
  },
  showAnnouncementAddSuccess() {
    successMessage.show(SHOP_MESSAGES.SUCCESS.ANNOUNCEMENT_ADD_SUCCESS)
  },
  showAnnouncementDeleteSuccess() {
    successMessage.show(SHOP_MESSAGES.SUCCESS.ANNOUNCEMENT_DELETE_SUCCESS)
  },
  
  // Logo上传
  showLogoUploadSuccess() {
    successMessage.show(SHOP_MESSAGES.SUCCESS.LOGO_UPLOAD_SUCCESS)
  }
}

// 错误消息函数
export const shopErrorMessages = {
  // 店铺操作
  showShopInfoLoadFailed(reason) {
    errorMessage.show(reason || SHOP_MESSAGES.ERROR.SHOP_INFO_LOAD_FAILED)
  },
  showShopDataLoadFailed(reason) {
    errorMessage.show(reason || SHOP_MESSAGES.ERROR.SHOP_DATA_LOAD_FAILED)
  },
  showShopNotPlatform() {
    errorMessage.show(SHOP_MESSAGES.ERROR.SHOP_NOT_PLATFORM)
  },
  showFollowOperationFailed(reason) {
    errorMessage.show(reason || SHOP_MESSAGES.ERROR.FOLLOW_OPERATION_FAILED)
  },
  showReviewLoadFailed(reason) {
    errorMessage.show(reason || SHOP_MESSAGES.ERROR.REVIEW_LOAD_FAILED)
  },
  showReviewSubmitFailed(reason) {
    errorMessage.show(reason || SHOP_MESSAGES.ERROR.REVIEW_SUBMIT_FAILED)
  },
  showShopInfoNotExist() {
    errorMessage.show(SHOP_MESSAGES.ERROR.SHOP_INFO_NOT_EXIST)
  },
  
  // 茶叶管理
  showTeaListLoadFailed(reason) {
    errorMessage.show(reason || SHOP_MESSAGES.ERROR.TEA_LIST_LOAD_FAILED)
  },
  showTeaToggleFailed(action, reason) {
    errorMessage.show(reason || `${action}${SHOP_MESSAGES.ERROR.TEA_TOGGLE_FAILED}`)
  },
  showTeaDeleteFailed(reason) {
    errorMessage.show(reason || SHOP_MESSAGES.ERROR.TEA_DELETE_FAILED)
  },
  showTeaSaveFailed(reason) {
    errorMessage.show(reason || SHOP_MESSAGES.ERROR.TEA_SAVE_FAILED)
  },
  
  // Banner管理
  showBannerListLoadFailed(reason) {
    errorMessage.show(reason || SHOP_MESSAGES.ERROR.BANNER_LIST_LOAD_FAILED)
  },
  showBannerSaveFailed(reason) {
    errorMessage.show(reason || SHOP_MESSAGES.ERROR.BANNER_SAVE_FAILED)
  },
  showBannerDeleteFailed(reason) {
    errorMessage.show(reason || SHOP_MESSAGES.ERROR.BANNER_DELETE_FAILED)
  },
  showBannerOrderUpdateFailed(reason) {
    errorMessage.show(reason || SHOP_MESSAGES.ERROR.BANNER_ORDER_UPDATE_FAILED)
  },
  
  // 公告管理
  showAnnouncementListLoadFailed(reason) {
    errorMessage.show(reason || SHOP_MESSAGES.ERROR.ANNOUNCEMENT_LIST_LOAD_FAILED)
  },
  showAnnouncementSaveFailed(reason) {
    errorMessage.show(reason || SHOP_MESSAGES.ERROR.ANNOUNCEMENT_SAVE_FAILED)
  },
  showAnnouncementDeleteFailed(reason) {
    errorMessage.show(reason || SHOP_MESSAGES.ERROR.ANNOUNCEMENT_DELETE_FAILED)
  },
  
  // 统计数据
  showStatisticsLoadFailed(reason) {
    errorMessage.show(reason || SHOP_MESSAGES.ERROR.STATISTICS_LOAD_FAILED)
  },
  
  // Logo上传
  showLogoFormatError() {
    errorMessage.show(SHOP_MESSAGES.ERROR.LOGO_FORMAT_ERROR)
  },
  showLogoSizeError() {
    errorMessage.show(SHOP_MESSAGES.ERROR.LOGO_SIZE_ERROR)
  },
  showLogoUploadFailed(reason) {
    errorMessage.show(reason || SHOP_MESSAGES.ERROR.LOGO_UPLOAD_FAILED)
  },
  showLogoUploadNoShop() {
    errorMessage.show(SHOP_MESSAGES.ERROR.LOGO_UPLOAD_NO_SHOP)
  }
}

// 提示消息函数
export const shopPromptMessages = {
  // 店铺操作
  showShopIdNotExist() {
    promptMessage.show(SHOP_MESSAGES.PROMPT.SHOP_ID_NOT_EXIST)
  },
  showReviewContentRequired() {
    promptMessage.show(SHOP_MESSAGES.PROMPT.REVIEW_CONTENT_REQUIRED)
  },
  showReviewRatingRequired() {
    promptMessage.show(SHOP_MESSAGES.PROMPT.REVIEW_RATING_REQUIRED)
  },
  
  // 茶叶管理
  showShopInfoLoadFirst() {
    promptMessage.show(SHOP_MESSAGES.PROMPT.SHOP_INFO_LOAD_FIRST)
  },
  showSubmittingWait() {
    promptMessage.show(SHOP_MESSAGES.PROMPT.SUBMITTING_WAIT)
  }
}

// 默认导出
export default {
  success: shopSuccessMessages,
  error: shopErrorMessages,
  prompt: shopPromptMessages,
  SHOP_MESSAGES
}
