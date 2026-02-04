/**
 * 提示消息工具（纯前端）
 * 
 * 整合所有模块的 PROMPT 消息，用于表单验证、确认框等纯前端提示
 * 不涉及后端交互
 */

import { promptMessage } from './messageManager'

// ==================== 通用模块 PROMPT ====================
export const COMMON_PROMPT = {
  FILE_COUNT_LIMIT: '最多只能上传{count}张图片',
  UPLOAD_API_NOT_CONFIGURED: '未配置上传API',
  EMAIL_REQUIRED: '请输入您的邮箱',
  EMAIL_ALREADY_SUBSCRIBED: '该邮箱已订阅，无需重复操作',
  PROCESSING: '处理中...',
  IMAGE_TYPE_INVALID: '请选择图片文件',
  IMAGE_SIZE_EXCEEDED: '图片大小不能超过5MB'
}

export const commonPromptMessages = {
  showFileCountLimit(count) {
    const msg = COMMON_PROMPT.FILE_COUNT_LIMIT.replace('{count}', count)
    promptMessage.show(msg)
  },
  showUploadApiNotConfigured() {
    promptMessage.show(COMMON_PROMPT.UPLOAD_API_NOT_CONFIGURED)
  },
  showEmailRequired() {
    promptMessage.show(COMMON_PROMPT.EMAIL_REQUIRED)
  },
  showEmailAlreadySubscribed() {
    promptMessage.info(COMMON_PROMPT.EMAIL_ALREADY_SUBSCRIBED)
  },
  showProcessing() {
    promptMessage.info(COMMON_PROMPT.PROCESSING)
  },
  showImageTypeInvalid() {
    promptMessage.show(COMMON_PROMPT.IMAGE_TYPE_INVALID)
  },
  showImageSizeExceeded() {
    promptMessage.show(COMMON_PROMPT.IMAGE_SIZE_EXCEEDED)
  }
}

// ==================== 用户模块 PROMPT ====================
export const USER_PROMPT = {
  USERNAME_REQUIRED: '请输入用户名',
  PASSWORD_REQUIRED: '请输入密码',
  PASSWORD_FORMAT_INVALID: '密码格式不正确，需包含字母和数字且长度至少为8位',
  EMAIL_FORMAT_INVALID: '邮箱格式不正确',
  PHONE_FORMAT_INVALID: '手机号格式不正确',
  AGREEMENT_REQUIRED: '请先阅读并同意用户协议',
  CAPTCHA_REQUIRED: '请输入验证码',
  AVATAR_SIZE_LIMIT: '头像图片大小不能超过2MB',
  AVATAR_FORMAT_INVALID: '头像必须是JPG或PNG格式',
  FORM_INCOMPLETE: '请完善表单信息',
  CONFIRM_LOGOUT: '确定要退出登录吗？',
  USERNAME_INPUT_REQUIRED: '请先输入用户名',
  PHONE_INPUT_REQUIRED: '请先输入手机号',
  EMAIL_INPUT_REQUIRED: '请先输入邮箱',
  REGION_DATA_INCOMPLETE: '省市区数据加载不完整，将使用备用数据',
  EXPORT_FEATURE_DEVELOPING: '导出功能开发中，敬请期待...',
  CAPTCHA_SENT: '验证码已发送，请注意查收'
}


export const userPromptMessages = {
  showUsernameRequired() {
    promptMessage.show(USER_PROMPT.USERNAME_REQUIRED)
  },
  showPasswordRequired() {
    promptMessage.show(USER_PROMPT.PASSWORD_REQUIRED)
  },
  showPasswordFormatInvalid() {
    promptMessage.show(USER_PROMPT.PASSWORD_FORMAT_INVALID)
  },
  showEmailFormatInvalid() {
    promptMessage.show(USER_PROMPT.EMAIL_FORMAT_INVALID)
  },
  showPhoneFormatInvalid() {
    promptMessage.show(USER_PROMPT.PHONE_FORMAT_INVALID)
  },
  showAgreementRequired() {
    promptMessage.show(USER_PROMPT.AGREEMENT_REQUIRED)
  },
  showCaptchaRequired() {
    promptMessage.show(USER_PROMPT.CAPTCHA_REQUIRED)
  },
  showAvatarSizeLimit() {
    promptMessage.show(USER_PROMPT.AVATAR_SIZE_LIMIT)
  },
  showAvatarFormatInvalid() {
    promptMessage.show(USER_PROMPT.AVATAR_FORMAT_INVALID)
  },
  showFormIncomplete() {
    promptMessage.show(USER_PROMPT.FORM_INCOMPLETE)
  },
  showConfirmLogout() {
    promptMessage.info(USER_PROMPT.CONFIRM_LOGOUT)
  },
  showUsernameInputRequired() {
    promptMessage.show(USER_PROMPT.USERNAME_INPUT_REQUIRED)
  },
  showPhoneInputRequired() {
    promptMessage.show(USER_PROMPT.PHONE_INPUT_REQUIRED)
  },
  showEmailInputRequired() {
    promptMessage.show(USER_PROMPT.EMAIL_INPUT_REQUIRED)
  },
  showRegionDataIncomplete() {
    promptMessage.show(USER_PROMPT.REGION_DATA_INCOMPLETE)
  },
  showExportFeatureDeveloping() {
    promptMessage.info(USER_PROMPT.EXPORT_FEATURE_DEVELOPING)
  },
  showCaptchaSent() {
    promptMessage.success(USER_PROMPT.CAPTCHA_SENT)
  }
}

// ==================== 茶叶模块 PROMPT ====================
export const TEA_PROMPT = {
  REPLY_EMPTY: '请输入回复内容',
  SOLD_OUT: '该商品已售罄',
  SELECT_SPEC: '请先选择规格',
  SELECT_ON_SHELF: '请选择要上架的茶叶',
  SELECT_OFF_SHELF: '请选择要下架的茶叶',
  FORM_INVALID: '请正确填写表单内容',
  SPEC_REQUIRED: '请至少添加一个规格',
  SPEC_INCOMPLETE: '请填写完整的规格名称和价格',
  DEFAULT_SPEC_REQUIRED: '请指定一个默认规格',
  IMAGE_REQUIRED: '请至少上传一张商品图片',
  SUBMITTING: '正在提交数据，请稍候...'
}

export const teaPromptMessages = {
  showReplyEmpty() {
    promptMessage.show(TEA_PROMPT.REPLY_EMPTY)
  },
  showSoldOut() {
    promptMessage.show(TEA_PROMPT.SOLD_OUT)
  },
  showSelectSpec() {
    promptMessage.show(TEA_PROMPT.SELECT_SPEC)
  },
  showSelectOnShelf() {
    promptMessage.show(TEA_PROMPT.SELECT_ON_SHELF)
  },
  showSelectOffShelf() {
    promptMessage.show(TEA_PROMPT.SELECT_OFF_SHELF)
  },
  showFormInvalid() {
    promptMessage.show(TEA_PROMPT.FORM_INVALID)
  },
  showSpecRequired() {
    promptMessage.show(TEA_PROMPT.SPEC_REQUIRED)
  },
  showSpecIncomplete() {
    promptMessage.show(TEA_PROMPT.SPEC_INCOMPLETE)
  },
  showDefaultSpecRequired() {
    promptMessage.show(TEA_PROMPT.DEFAULT_SPEC_REQUIRED)
  },
  showImageRequired() {
    promptMessage.show(TEA_PROMPT.IMAGE_REQUIRED)
  },
  showSubmitting() {
    promptMessage.show(TEA_PROMPT.SUBMITTING)
  }
}


// ==================== 订单模块 PROMPT ====================
export const ORDER_PROMPT = {
  ADDRESS_REQUIRED: '请选择收货地址',
  ADDRESS_INVALID: '收货地址不完整',
  ORDER_EMPTY: '订单不能为空',
  ORDER_INFO_INVALID: '订单信息异常，请刷新后重试',
  ORDER_NOT_FOUND: '未找到当前订单信息',
  CANCEL_REASON_REQUIRED: '请选择取消原因',
  LOGISTICS_COMPANY_REQUIRED: '请选择物流公司',
  LOGISTICS_NUMBER_REQUIRED: '请输入物流单号',
  LOGISTICS_NUMBER_INVALID: '物流单号格式不正确',
  REVIEW_CONTENT_REQUIRED: '请填写评价内容',
  REVIEW_RATING_REQUIRED: '请为商品评分',
  REVIEW_LIMIT_EXCEEDED: '评价内容不能超过500字',
  REVIEW_CONTENT_TOO_SHORT: '评价内容不能少于5个字',
  REVIEW_NOT_ALLOWED: '当前订单状态不支持评价',
  ALREADY_REVIEWED: '该订单已评价',
  IMAGE_LIMIT_EXCEEDED: '最多只能上传6张图片',
  RATING_REQUIRED: '请为商品评分',
  QUANTITY_INVALID: '商品数量不正确',
  SELECTION_REQUIRED: '请先选择要结算的商品',
  DELETE_SELECTION_REQUIRED: '请先选择要删除的商品',
  SHIP_SELECTION_REQUIRED: '请先选择需要发货的订单',
  CART_EMPTY: '购物车是空的',
  SELECT_PAYMENT_METHOD: '请选择支付方式',
  SELECT_EXPORT_FORMAT: '请选择导出格式',
  CANCEL_CONFIRM: '确定要取消该订单吗？',
  CONFIRM_RECEIVE: '确认已收到商品吗？确认后无法撤销',
  DELETE_ORDER_CONFIRM: '确定要删除该订单记录吗？',
  REMOVE_CART_CONFIRM: '确定要从购物车中移除此商品吗？',
  CLEAR_CART_CONFIRM: '确定要清空购物车吗？',
  CONFIRM_PAYMENT: '确认支付吗？',
  SPEC_REQUIRED: '请选择规格',
  NO_SPEC_AVAILABLE: '该商品暂无可用规格',
  STOCK_INSUFFICIENT: '库存不足',
  REFUND_REASON_TOO_SHORT: '退款原因不能少于5个字',
  REFUND_NOT_SUPPORTED: '当前订单状态不支持退款',
  NO_LOGISTICS_INFO: '暂无物流信息',
  ORDER_INFO_INCOMPLETE: '请完善订单信息',
  CONTACT_SELLER_DEV: '联系卖家功能开发中',
  BUY_AGAIN_DEV: '再次购买功能开发中',
  DELETE_ORDER_DEV: '删除订单功能待后端接口接入',
  VIEW_LOGISTICS_DEV: '查看物流信息功能开发中',
  MODIFY_ADDRESS_DEV: '修改地址功能开发中',
  CONTACT_SHOP_DEV: '联系商家功能开发中'
}

export const orderPromptMessages = {
  showAddressRequired() {
    promptMessage.show(ORDER_PROMPT.ADDRESS_REQUIRED)
  },
  showAddressInvalid() {
    promptMessage.show(ORDER_PROMPT.ADDRESS_INVALID)
  },
  showOrderEmpty() {
    promptMessage.show(ORDER_PROMPT.ORDER_EMPTY)
  },
  showOrderInfoInvalid() {
    promptMessage.show(ORDER_PROMPT.ORDER_INFO_INVALID)
  },
  showOrderNotFound() {
    promptMessage.show(ORDER_PROMPT.ORDER_NOT_FOUND)
  },
  showCancelReasonRequired() {
    promptMessage.show(ORDER_PROMPT.CANCEL_REASON_REQUIRED)
  },
  showLogisticsCompanyRequired() {
    promptMessage.show(ORDER_PROMPT.LOGISTICS_COMPANY_REQUIRED)
  },
  showLogisticsNumberRequired() {
    promptMessage.show(ORDER_PROMPT.LOGISTICS_NUMBER_REQUIRED)
  },
  showLogisticsNumberInvalid() {
    promptMessage.show(ORDER_PROMPT.LOGISTICS_NUMBER_INVALID)
  },
  showReviewContentRequired() {
    promptMessage.show(ORDER_PROMPT.REVIEW_CONTENT_REQUIRED)
  },
  showReviewRatingRequired() {
    promptMessage.show(ORDER_PROMPT.REVIEW_RATING_REQUIRED)
  },
  showReviewLimitExceeded() {
    promptMessage.show(ORDER_PROMPT.REVIEW_LIMIT_EXCEEDED)
  },
  showQuantityInvalid() {
    promptMessage.show(ORDER_PROMPT.QUANTITY_INVALID)
  },
  showSelectionRequired() {
    promptMessage.show(ORDER_PROMPT.SELECTION_REQUIRED)
  },
  showDeleteSelectionRequired() {
    promptMessage.show(ORDER_PROMPT.DELETE_SELECTION_REQUIRED)
  },
  showShipSelectionRequired() {
    promptMessage.show(ORDER_PROMPT.SHIP_SELECTION_REQUIRED)
  },
  showCartEmpty() {
    promptMessage.show(ORDER_PROMPT.CART_EMPTY)
  },
  showSelectPaymentMethod() {
    promptMessage.show(ORDER_PROMPT.SELECT_PAYMENT_METHOD)
  },
  showSelectExportFormat() {
    promptMessage.show(ORDER_PROMPT.SELECT_EXPORT_FORMAT)
  },
  showCancelConfirm() {
    promptMessage.info(ORDER_PROMPT.CANCEL_CONFIRM)
  },
  showConfirmReceive() {
    promptMessage.info(ORDER_PROMPT.CONFIRM_RECEIVE)
  },
  showDeleteOrderConfirm() {
    promptMessage.info(ORDER_PROMPT.DELETE_ORDER_CONFIRM)
  },
  showRemoveCartConfirm() {
    promptMessage.info(ORDER_PROMPT.REMOVE_CART_CONFIRM)
  },
  showClearCartConfirm() {
    promptMessage.info(ORDER_PROMPT.CLEAR_CART_CONFIRM)
  },
  showConfirmPayment(amount) {
    promptMessage.info(amount ? `确认支付¥${amount}？` : ORDER_PROMPT.CONFIRM_PAYMENT)
  },
  showSpecRequired() {
    promptMessage.show(ORDER_PROMPT.SPEC_REQUIRED)
  },
  showNoSpecAvailable() {
    promptMessage.show(ORDER_PROMPT.NO_SPEC_AVAILABLE)
  },
  showStockInsufficient(stock) {
    const msg = stock !== undefined ? `库存不足，当前可用库存：${stock}` : ORDER_PROMPT.STOCK_INSUFFICIENT
    promptMessage.show(msg)
  },
  showRefundReasonTooShort() {
    promptMessage.show(ORDER_PROMPT.REFUND_REASON_TOO_SHORT)
  },
  showRefundNotSupported() {
    promptMessage.info(ORDER_PROMPT.REFUND_NOT_SUPPORTED)
  },
  showNoLogisticsInfo() {
    promptMessage.info(ORDER_PROMPT.NO_LOGISTICS_INFO)
  },
  showContactSellerDev() {
    promptMessage.info(ORDER_PROMPT.CONTACT_SELLER_DEV)
  },
  showBuyAgainDev() {
    promptMessage.info(ORDER_PROMPT.BUY_AGAIN_DEV)
  },
  showDeleteOrderDev() {
    promptMessage.info(ORDER_PROMPT.DELETE_ORDER_DEV)
  },
  showViewLogisticsDev() {
    promptMessage.info(ORDER_PROMPT.VIEW_LOGISTICS_DEV)
  },
  showModifyAddressDev() {
    promptMessage.info(ORDER_PROMPT.MODIFY_ADDRESS_DEV)
  },
  showContactShopDev() {
    promptMessage.info(ORDER_PROMPT.CONTACT_SHOP_DEV)
  },
  showOrderInfoIncomplete() {
    promptMessage.show(ORDER_PROMPT.ORDER_INFO_INCOMPLETE)
  },
  showReviewContentTooShort() {
    promptMessage.show(ORDER_PROMPT.REVIEW_CONTENT_TOO_SHORT)
  },
  showReviewNotAllowed() {
    promptMessage.show(ORDER_PROMPT.REVIEW_NOT_ALLOWED)
  },
  showAlreadyReviewed() {
    promptMessage.show(ORDER_PROMPT.ALREADY_REVIEWED)
  },
  showImageLimitExceeded() {
    promptMessage.show(ORDER_PROMPT.IMAGE_LIMIT_EXCEEDED)
  },
  showRatingRequired() {
    promptMessage.show(ORDER_PROMPT.RATING_REQUIRED)
  }
}


// ==================== 店铺模块 PROMPT ====================
export const SHOP_PROMPT = {
  SHOP_ID_NOT_EXIST: '店铺ID不存在',
  REVIEW_CONTENT_REQUIRED: '请输入评价内容',
  REVIEW_RATING_REQUIRED: '请选择评分',
  SHOP_INFO_LOAD_FIRST: '请先加载店铺信息',
  SUBMITTING_WAIT: '正在提交数据，请稍候...'
}

export const shopPromptMessages = {
  showShopIdNotExist() {
    promptMessage.show(SHOP_PROMPT.SHOP_ID_NOT_EXIST)
  },
  showReviewContentRequired() {
    promptMessage.show(SHOP_PROMPT.REVIEW_CONTENT_REQUIRED)
  },
  showReviewRatingRequired() {
    promptMessage.show(SHOP_PROMPT.REVIEW_RATING_REQUIRED)
  },
  showShopInfoLoadFirst() {
    promptMessage.show(SHOP_PROMPT.SHOP_INFO_LOAD_FIRST)
  },
  showSubmittingWait() {
    promptMessage.show(SHOP_PROMPT.SUBMITTING_WAIT)
  }
}

// ==================== 论坛模块 PROMPT ====================
export const FORUM_PROMPT = {
  TITLE_REQUIRED: '标题不能为空',
  CONTENT_REQUIRED: '内容不能为空',
  CATEGORY_REQUIRED: '请选择分类',
  COMMENT_REQUIRED: '回复内容不能为空',
  COMMENT_TOO_LONG: '评论内容不能超过500字',
  IMAGE_SIZE_LIMIT: '图片大小不能超过2MB',
  IMAGE_FORMAT_INVALID: '只支持JPG、PNG和GIF格式的图片',
  DELETE_CONFIRM: '确定要删除吗？此操作不可撤销',
  SHARE_DEVELOPING: '分享功能正在开发中',
  AR_DEVELOPING: 'AR虚拟试饮功能正在开发中，敬请期待...',
  FEATURE_DEVELOPING: '功能待后端接口接入',
  BANNER_DATA_ERROR: '轮播图数据格式有误，已重置',
  RECOMMEND_DATA_ERROR: '推荐茶叶数据格式有误，已重置'
}

export const forumPromptMessages = {
  showTitleRequired() {
    promptMessage.show(FORUM_PROMPT.TITLE_REQUIRED)
  },
  showContentRequired() {
    promptMessage.show(FORUM_PROMPT.CONTENT_REQUIRED)
  },
  showCategoryRequired() {
    promptMessage.show(FORUM_PROMPT.CATEGORY_REQUIRED)
  },
  showCommentRequired() {
    promptMessage.show(FORUM_PROMPT.COMMENT_REQUIRED)
  },
  showCommentTooLong() {
    promptMessage.show(FORUM_PROMPT.COMMENT_TOO_LONG)
  },
  showImageSizeLimit() {
    promptMessage.show(FORUM_PROMPT.IMAGE_SIZE_LIMIT)
  },
  showImageFormatInvalid() {
    promptMessage.show(FORUM_PROMPT.IMAGE_FORMAT_INVALID)
  },
  showDeleteConfirm() {
    promptMessage.info(FORUM_PROMPT.DELETE_CONFIRM)
  },
  showShareDeveloping() {
    promptMessage.info(FORUM_PROMPT.SHARE_DEVELOPING)
  },
  showARDeveloping() {
    promptMessage.info(FORUM_PROMPT.AR_DEVELOPING)
  },
  showFeatureDeveloping() {
    promptMessage.info(FORUM_PROMPT.FEATURE_DEVELOPING)
  },
  showBannerDataError() {
    promptMessage.show(FORUM_PROMPT.BANNER_DATA_ERROR)
  },
  showRecommendDataError() {
    promptMessage.show(FORUM_PROMPT.RECOMMEND_DATA_ERROR)
  }
}

// ==================== 消息模块 PROMPT ====================
export const MESSAGE_PROMPT = {
  MESSAGE_EMPTY: '消息内容不能为空',
  RECIPIENT_REQUIRED: '请选择接收人',
  IMAGE_SIZE_LIMIT: '图片大小不能超过5MB',
  IMAGE_FORMAT_INVALID: '只支持JPG、PNG和GIF格式的图片',
  USER_DATA_INCOMPLETE: '用户数据加载不完整，请刷新页面或重新登录',
  DELETE_SESSION_CONFIRM: '确定要删除此会话吗？历史消息将无法恢复',
  DELETE_NOTIFICATION_CONFIRM: '确定要删除此通知吗？',
  CLEAR_ALL_NOTIFICATIONS_CONFIRM: '确定要清空所有通知吗？',
  RECALL_CONFIRM: '确定要撤回此消息吗？',
  FOLLOW_NOT_FOUND: '未找到关注记录'
}

export const messagePromptMessages = {
  showMessageEmpty() {
    promptMessage.show(MESSAGE_PROMPT.MESSAGE_EMPTY)
  },
  showRecipientRequired() {
    promptMessage.show(MESSAGE_PROMPT.RECIPIENT_REQUIRED)
  },
  showImageSizeLimit() {
    promptMessage.show(MESSAGE_PROMPT.IMAGE_SIZE_LIMIT)
  },
  showImageFormatInvalid() {
    promptMessage.show(MESSAGE_PROMPT.IMAGE_FORMAT_INVALID)
  },
  showUserDataIncomplete() {
    promptMessage.show(MESSAGE_PROMPT.USER_DATA_INCOMPLETE)
  },
  showDeleteSessionConfirm() {
    promptMessage.info(MESSAGE_PROMPT.DELETE_SESSION_CONFIRM)
  },
  showDeleteNotificationConfirm() {
    promptMessage.info(MESSAGE_PROMPT.DELETE_NOTIFICATION_CONFIRM)
  },
  showClearAllNotificationsConfirm() {
    promptMessage.info(MESSAGE_PROMPT.CLEAR_ALL_NOTIFICATIONS_CONFIRM)
  },
  showRecallConfirm() {
    promptMessage.info(MESSAGE_PROMPT.RECALL_CONFIRM)
  },
  showPleaseWait() {
    promptMessage.info('请稍候...')
  },
  showFollowNotFound() {
    promptMessage.show(MESSAGE_PROMPT.FOLLOW_NOT_FOUND)
  }
}

// ==================== 默认导出 ====================
export default {
  common: commonPromptMessages,
  user: userPromptMessages,
  tea: teaPromptMessages,
  order: orderPromptMessages,
  shop: shopPromptMessages,
  forum: forumPromptMessages,
  message: messagePromptMessages
}
