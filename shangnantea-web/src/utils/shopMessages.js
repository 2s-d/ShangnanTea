/**
 * 店铺模块消息工具
 * 按照三层架构设计消息：
 * 1. API层：处理网络请求相关消息
 * 2. Vuex业务层：处理业务操作结果消息
 * 3. UI交互层：处理界面交互提示消息
 */
import messageManager from './messageManager'

// 店铺模块常用消息
export const SHOP_MESSAGES = {
  // API层消息
  API: {
    LOADING_SHOPS: '正在加载店铺列表...',
    LOADING_SHOP_DETAIL: '正在加载店铺详情...',
    CREATING_SHOP: '正在创建店铺...',
    UPDATING_SHOP: '正在更新店铺信息...',
    LOADING_SHOP_TEAS: '正在加载店铺茶叶...',
    LOADING_SHOP_STATISTICS: '正在加载店铺统计数据...',
    LOADING_CERTIFICATIONS: '正在加载认证申请...',
    SUBMITTING_CERTIFICATION: '正在提交认证申请...',
    UPDATING_CERTIFICATION: '正在更新认证申请...',
    LOADING_FAILED: '加载失败',
    CREATE_FAILED: '创建失败',
    UPDATE_FAILED: '更新失败',
    SUBMIT_FAILED: '提交失败'
  },
  
  // 业务层消息
  BUSINESS: {
    SHOP_CREATED: '店铺创建成功',
    SHOP_UPDATED: '店铺信息已更新',
    SHOP_CLOSED: '店铺已关闭',
    SHOP_REOPENED: '店铺已重新开业',
    SHOP_DELETE_FAILED: '店铺删除失败',
    CERTIFICATION_SUBMITTED: '认证申请已提交，请等待审核',
    CERTIFICATION_APPROVED: '认证申请已通过',
    CERTIFICATION_REJECTED: '认证申请被拒绝',
    CERTIFICATION_CONFIRMED: '商家认证已确认，店铺已创建',
    TEA_ADDED: '茶叶已添加到店铺',
    TEA_UPDATED: '店铺茶叶信息已更新',
    TEA_REMOVED: '茶叶已从店铺移除',
    UNAUTHORIZED: '您没有权限执行此操作',
    SHOP_NOT_FOUND: '店铺不存在或已关闭'
  },
  
  // UI交互层消息
  UI: {
    SHOP_NAME_REQUIRED: '店铺名称不能为空',
    SHOP_DESCRIPTION_REQUIRED: '店铺简介不能为空',
    CONTACT_PHONE_REQUIRED: '联系电话不能为空',
    CONTACT_PHONE_INVALID: '联系电话格式不正确',
    ADDRESS_REQUIRED: '店铺地址不能为空',
    BUSINESS_LICENSE_REQUIRED: '请上传营业执照',
    LOGO_REQUIRED: '请上传店铺logo',
    LOGO_SIZE_LIMIT: '店铺logo大小不能超过2MB',
    LOGO_FORMAT_INVALID: '店铺logo只支持JPG、PNG格式',
    BANNER_SIZE_LIMIT: '店铺banner大小不能超过5MB',
    BANNER_FORMAT_INVALID: '店铺banner只支持JPG、PNG格式',
    CERTIFICATION_INCOMPLETE: '请完成所有认证资料填写',
    DELETE_CONFIRM: '确定要删除店铺吗？此操作不可撤销',
    CLOSE_CONFIRM: '确定要关闭店铺吗？关闭后店铺将不对外展示',
    REOPEN_CONFIRM: '确定要重新开业吗？'
  }
}

// 店铺API层消息工具
const apiMessages = {
  // 显示API加载消息
  showLoading(message) {
    messageManager.api.showLoading(message)
  },
  
  // 显示API成功消息
  showSuccess(message) {
    messageManager.api.showSuccess(message)
  },
  
  // 显示API错误消息
  showError(message) {
    messageManager.api.showError(message)
  },
  
  // 预定义的API消息
  showLoadingShops() {
    this.showLoading(SHOP_MESSAGES.API.LOADING_SHOPS)
  },
  
  showLoadingShopDetail() {
    this.showLoading(SHOP_MESSAGES.API.LOADING_SHOP_DETAIL)
  },
  
  showCreatingShop() {
    this.showLoading(SHOP_MESSAGES.API.CREATING_SHOP)
  },
  
  showUpdatingShop() {
    this.showLoading(SHOP_MESSAGES.API.UPDATING_SHOP)
  },
  
  showLoadingShopTeas() {
    this.showLoading(SHOP_MESSAGES.API.LOADING_SHOP_TEAS)
  },
  
  showLoadingShopStatistics() {
    this.showLoading(SHOP_MESSAGES.API.LOADING_SHOP_STATISTICS)
  },
  
  showLoadingCertifications() {
    this.showLoading(SHOP_MESSAGES.API.LOADING_CERTIFICATIONS)
  },
  
  showSubmittingCertification() {
    this.showLoading(SHOP_MESSAGES.API.SUBMITTING_CERTIFICATION)
  },
  
  showLoadingFailed(error) {
    const errorMsg = error?.message || SHOP_MESSAGES.API.LOADING_FAILED
    this.showError(errorMsg)
  },
  
  showCreateFailed(error) {
    const errorMsg = error?.message || SHOP_MESSAGES.API.CREATE_FAILED
    this.showError(errorMsg)
  },
  
  showUpdateFailed(error) {
    const errorMsg = error?.message || SHOP_MESSAGES.API.UPDATE_FAILED
    this.showError(errorMsg)
  },
  
  showSubmitFailed(error) {
    const errorMsg = error?.message || SHOP_MESSAGES.API.SUBMIT_FAILED
    this.showError(errorMsg)
  }
}

// 店铺业务层消息工具
const businessMessages = {
  // 显示业务成功消息
  showSuccess(message) {
    messageManager.business.showSuccess(message)
  },
  
  // 显示业务错误消息
  showError(message) {
    messageManager.business.showError(message)
  },
  
  // 显示业务警告消息
  showWarning(message) {
    messageManager.business.showWarning(message)
  },
  
  // 预定义的业务消息
  showShopCreated() {
    this.showSuccess(SHOP_MESSAGES.BUSINESS.SHOP_CREATED)
  },
  
  showShopUpdated() {
    this.showSuccess(SHOP_MESSAGES.BUSINESS.SHOP_UPDATED)
  },
  
  showShopClosed() {
    this.showSuccess(SHOP_MESSAGES.BUSINESS.SHOP_CLOSED)
  },
  
  showShopReopened() {
    this.showSuccess(SHOP_MESSAGES.BUSINESS.SHOP_REOPENED)
  },
  
  showCertificationSubmitted() {
    this.showSuccess(SHOP_MESSAGES.BUSINESS.CERTIFICATION_SUBMITTED)
  },
  
  showCertificationApproved() {
    this.showSuccess(SHOP_MESSAGES.BUSINESS.CERTIFICATION_APPROVED)
  },
  
  showCertificationRejected(reason) {
    const message = reason 
      ? `${SHOP_MESSAGES.BUSINESS.CERTIFICATION_REJECTED}，原因：${reason}`
      : SHOP_MESSAGES.BUSINESS.CERTIFICATION_REJECTED
    this.showWarning(message)
  },
  
  showCertificationConfirmed() {
    this.showSuccess(SHOP_MESSAGES.BUSINESS.CERTIFICATION_CONFIRMED)
  },
  
  showTeaAdded() {
    this.showSuccess(SHOP_MESSAGES.BUSINESS.TEA_ADDED)
  },
  
  showTeaUpdated() {
    this.showSuccess(SHOP_MESSAGES.BUSINESS.TEA_UPDATED)
  },
  
  showTeaRemoved() {
    this.showSuccess(SHOP_MESSAGES.BUSINESS.TEA_REMOVED)
  },
  
  showUnauthorized() {
    this.showWarning(SHOP_MESSAGES.BUSINESS.UNAUTHORIZED)
  },
  
  showShopNotFound() {
    this.showWarning(SHOP_MESSAGES.BUSINESS.SHOP_NOT_FOUND)
  }
}

// 店铺UI交互层消息工具
const uiMessages = {
  // 显示UI成功消息
  showSuccess(message) {
    messageManager.ui.showSuccess(message)
  },
  
  // 显示UI错误消息
  showError(message) {
    messageManager.ui.showError(message)
  },
  
  // 显示UI警告消息
  showWarning(message) {
    messageManager.ui.showWarning(message)
  },
  
  // 显示UI信息消息
  showInfo(message) {
    messageManager.ui.showInfo(message)
  },
  
  // 显示确认消息
  showConfirm(message) {
    return messageManager.ui.showConfirm(message)
  },
  
  // 预定义的UI消息
  showShopNameRequired() {
    this.showWarning(SHOP_MESSAGES.UI.SHOP_NAME_REQUIRED)
  },
  
  showShopDescriptionRequired() {
    this.showWarning(SHOP_MESSAGES.UI.SHOP_DESCRIPTION_REQUIRED)
  },
  
  showContactPhoneRequired() {
    this.showWarning(SHOP_MESSAGES.UI.CONTACT_PHONE_REQUIRED)
  },
  
  showContactPhoneInvalid() {
    this.showWarning(SHOP_MESSAGES.UI.CONTACT_PHONE_INVALID)
  },
  
  showAddressRequired() {
    this.showWarning(SHOP_MESSAGES.UI.ADDRESS_REQUIRED)
  },
  
  showBusinessLicenseRequired() {
    this.showWarning(SHOP_MESSAGES.UI.BUSINESS_LICENSE_REQUIRED)
  },
  
  showLogoRequired() {
    this.showWarning(SHOP_MESSAGES.UI.LOGO_REQUIRED)
  },
  
  showLogoSizeLimit() {
    this.showWarning(SHOP_MESSAGES.UI.LOGO_SIZE_LIMIT)
  },
  
  showLogoFormatInvalid() {
    this.showWarning(SHOP_MESSAGES.UI.LOGO_FORMAT_INVALID)
  },
  
  showBannerSizeLimit() {
    this.showWarning(SHOP_MESSAGES.UI.BANNER_SIZE_LIMIT)
  },
  
  showBannerFormatInvalid() {
    this.showWarning(SHOP_MESSAGES.UI.BANNER_FORMAT_INVALID)
  },
  
  showCertificationIncomplete() {
    this.showWarning(SHOP_MESSAGES.UI.CERTIFICATION_INCOMPLETE)
  },
  
  showDeleteConfirm() {
    return this.showConfirm(SHOP_MESSAGES.UI.DELETE_CONFIRM)
  },
  
  showCloseConfirm() {
    return this.showConfirm(SHOP_MESSAGES.UI.CLOSE_CONFIRM)
  },
  
  showReopenConfirm() {
    return this.showConfirm(SHOP_MESSAGES.UI.REOPEN_CONFIRM)
  }
}

// 导出所有消息工具
export default {
  api: apiMessages,
  business: businessMessages,
  ui: uiMessages,
  SHOP_MESSAGES
}
