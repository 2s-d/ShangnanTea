/**
 * 订单模块消息工具
 * 
 * 分类说明：
 * - SUCCESS: 操作成功反馈
 * - ERROR: 操作失败/错误提示
 * - PROMPT: 用户提示（表单验证+确认）
 */

import { successMessage, errorMessage, promptMessage } from './messageManager'

// 订单模块消息常量
export const ORDER_MESSAGES = {
  // 成功消息
  SUCCESS: {
    ORDER_CREATED: '订单创建成功',
    ORDER_SUBMITTED: '订单提交成功',
    ORDER_CANCELED: '订单已取消',
    ORDER_CONFIRMED: '确认收货成功',
    ORDER_SHIPPED: '发货成功',
    ORDER_REVIEWED: '订单评价成功',
    ORDER_PAID: '订单已支付',
    LOGISTICS_UPLOADED: '物流单号已上传',
    LOGISTICS_REFRESHED: '已刷新物流信息',
    ADDED_TO_CART: '已加入购物车',
    CART_QUANTITY_UPDATED: '商品数量已更新',
    CART_SPEC_UPDATED: '规格已更新',
    ITEM_REMOVED: '商品已从购物车移除',
    SELECTED_ITEMS_DELETED: '选中商品已删除',
    CART_CLEARED: '购物车已清空',
    PAYMENT_SUCCESS: '支付成功',
    REFUND_SUBMITTED: '退款申请已提交，等待商家审核',
    BATCH_SHIP_SUCCESS: '批量发货成功',
    REFUND_APPROVED: '已同意退款申请',
    REFUND_REJECTED: '已拒绝退款申请',
    EXPORT_SUCCESS: '订单导出成功',
    ADDRESS_ADDED: '地址添加成功',
    REVIEW_SUBMITTED: '评价提交成功，感谢您的反馈'
  },
  
  // 错误消息
  ERROR: {
    ORDER_CREATE_FAILED: '创建订单失败',
    ORDER_CANCEL_FAILED: '取消订单失败',
    ORDER_CONFIRM_FAILED: '确认收货失败',
    ORDER_SHIP_FAILED: '发货失败，请稍后重试',
    ORDER_NOT_FOUND: '订单不存在',
    ORDER_UNAUTHORIZED: '您没有权限操作此订单',
    ORDER_DETAIL_LOAD_FAILED: '获取订单详情失败',
    ORDER_ID_REQUIRED: '订单ID不能为空',
    ORDER_SUBMIT_FAILED: '提交订单失败',
    CART_LOAD_FAILED: '获取购物车数据失败',
    CART_ADD_FAILED: '加入购物车失败',
    CART_UPDATE_FAILED: '更新数量失败',
    CART_REMOVE_FAILED: '移除商品失败',
    CART_DELETE_FAILED: '删除商品失败',
    CART_CLEAR_FAILED: '清空购物车失败',
    CART_ITEM_OUT_OF_STOCK: '商品库存不足',
    CART_ITEM_LIMIT_REACHED: '已达到购买数量上限',
    CART_ITEM_INVALID: '商品已下架或不可用',
    SPEC_LOAD_FAILED: '获取规格列表失败',
    SPEC_UPDATE_FAILED: '规格更新失败',
    LOGISTICS_LOAD_FAILED: '获取物流信息失败',
    PAYMENT_FAILED: '支付失败，请稍后重试',
    PAYMENT_CANCELED: '支付已取消',
    PAYMENT_TIMEOUT: '支付超时，订单已自动取消',
    PAYMENT_ORDER_LOAD_FAILED: '加载订单信息失败',
    INSUFFICIENT_BALANCE: '余额不足',
    REFUND_SUBMIT_FAILED: '退款申请提交失败',
    REFUND_PROCESS_FAILED: '退款申请失败',
    REFUND_DETAIL_LOAD_FAILED: '获取退款详情失败，将显示订单基本信息',
    BATCH_SHIP_FAILED: '批量发货失败，请稍后重试',
    STATISTICS_LOAD_FAILED: '加载统计数据失败',
    EXPORT_FAILED: '导出失败',
    ADDRESS_LOAD_FAILED: '获取地址失败',
    ADDRESS_SAVE_FAILED: '保存地址失败',
    PRODUCT_INFO_EXPIRED: '商品信息已失效，请重新选择',
    PRODUCT_INFO_LOAD_FAILED: '获取商品信息失败'
  },
  
  // 提示消息
  PROMPT: {
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
    // 开发中功能提示
    CONTACT_SELLER_DEV: '联系卖家功能开发中',
    BUY_AGAIN_DEV: '再次购买功能开发中',
    DELETE_ORDER_DEV: '删除订单功能待后端接口接入',
    VIEW_LOGISTICS_DEV: '查看物流信息功能开发中',
    MODIFY_ADDRESS_DEV: '修改地址功能开发中',
    CONTACT_SHOP_DEV: '联系商家功能开发中'
  }
}

// 成功消息函数
export const orderSuccessMessages = {
  showOrderCreated(orderNo) {
    successMessage.show(orderNo ? `订单${orderNo}创建成功` : ORDER_MESSAGES.SUCCESS.ORDER_CREATED)
  },
  showOrderCanceled() {
    successMessage.show(ORDER_MESSAGES.SUCCESS.ORDER_CANCELED)
  },
  showOrderConfirmed() {
    successMessage.show(ORDER_MESSAGES.SUCCESS.ORDER_CONFIRMED)
  },
  showOrderShipped() {
    successMessage.show(ORDER_MESSAGES.SUCCESS.ORDER_SHIPPED)
  },
  showOrderReviewed() {
    successMessage.show(ORDER_MESSAGES.SUCCESS.ORDER_REVIEWED)
  },
  showOrderPaid(orderNo, amount) {
    const msg = orderNo && amount ? `订单${orderNo}已支付成功，金额￥${amount}` : ORDER_MESSAGES.SUCCESS.ORDER_PAID
    successMessage.show(msg)
  },
  showLogisticsUploaded() {
    successMessage.show(ORDER_MESSAGES.SUCCESS.LOGISTICS_UPLOADED)
  },
  showLogisticsRefreshed() {
    successMessage.show(ORDER_MESSAGES.SUCCESS.LOGISTICS_REFRESHED)
  },
  showAddedToCart(teaName, quantity = 1) {
    const msg = teaName ? `已将${teaName} ${quantity > 1 ? quantity + '件' : ''}加入购物车` : ORDER_MESSAGES.SUCCESS.ADDED_TO_CART
    successMessage.show(msg)
  },
  showCartQuantityUpdated() {
    successMessage.show(ORDER_MESSAGES.SUCCESS.CART_QUANTITY_UPDATED)
  },
  showCartSpecUpdated() {
    successMessage.show(ORDER_MESSAGES.SUCCESS.CART_SPEC_UPDATED)
  },
  showItemRemoved() {
    successMessage.show(ORDER_MESSAGES.SUCCESS.ITEM_REMOVED)
  },
  showSelectedItemsDeleted() {
    successMessage.show(ORDER_MESSAGES.SUCCESS.SELECTED_ITEMS_DELETED)
  },
  showCartCleared() {
    successMessage.show(ORDER_MESSAGES.SUCCESS.CART_CLEARED)
  },
  showPaymentSuccess() {
    successMessage.show(ORDER_MESSAGES.SUCCESS.PAYMENT_SUCCESS)
  },
  showRefundSubmitted() {
    successMessage.show(ORDER_MESSAGES.SUCCESS.REFUND_SUBMITTED)
  },
  showBatchShipSuccess() {
    successMessage.show(ORDER_MESSAGES.SUCCESS.BATCH_SHIP_SUCCESS)
  },
  showRefundApproved() {
    successMessage.show(ORDER_MESSAGES.SUCCESS.REFUND_APPROVED)
  },
  showRefundRejected() {
    successMessage.show(ORDER_MESSAGES.SUCCESS.REFUND_REJECTED)
  },
  showExportSuccess() {
    successMessage.show(ORDER_MESSAGES.SUCCESS.EXPORT_SUCCESS)
  },
  showAddressAdded() {
    successMessage.show(ORDER_MESSAGES.SUCCESS.ADDRESS_ADDED)
  },
  showOrderSubmitted() {
    successMessage.show(ORDER_MESSAGES.SUCCESS.ORDER_SUBMITTED)
  },
  showReviewSubmitted() {
    successMessage.show(ORDER_MESSAGES.SUCCESS.REVIEW_SUBMITTED)
  }
}

// 错误消息函数
export const orderErrorMessages = {
  showOrderCreateFailed(reason) {
    errorMessage.show(reason || ORDER_MESSAGES.ERROR.ORDER_CREATE_FAILED)
  },
  showOrderCancelFailed(reason) {
    errorMessage.show(reason || ORDER_MESSAGES.ERROR.ORDER_CANCEL_FAILED)
  },
  showOrderConfirmFailed(reason) {
    errorMessage.show(reason || ORDER_MESSAGES.ERROR.ORDER_CONFIRM_FAILED)
  },
  showOrderShipFailed(reason) {
    errorMessage.show(reason || ORDER_MESSAGES.ERROR.ORDER_SHIP_FAILED)
  },
  showOrderNotFound() {
    errorMessage.warning(ORDER_MESSAGES.ERROR.ORDER_NOT_FOUND)
  },
  showOrderUnauthorized() {
    errorMessage.warning(ORDER_MESSAGES.ERROR.ORDER_UNAUTHORIZED)
  },
  showOrderDetailLoadFailed(reason) {
    errorMessage.show(reason || ORDER_MESSAGES.ERROR.ORDER_DETAIL_LOAD_FAILED)
  },
  showOrderIdRequired() {
    errorMessage.show(ORDER_MESSAGES.ERROR.ORDER_ID_REQUIRED)
  },
  showCartLoadFailed(reason) {
    errorMessage.show(reason || ORDER_MESSAGES.ERROR.CART_LOAD_FAILED)
  },
  showCartAddFailed(reason) {
    errorMessage.show(reason || ORDER_MESSAGES.ERROR.CART_ADD_FAILED)
  },
  showCartUpdateFailed(reason) {
    errorMessage.show(reason || ORDER_MESSAGES.ERROR.CART_UPDATE_FAILED)
  },
  showCartRemoveFailed(reason) {
    errorMessage.show(reason || ORDER_MESSAGES.ERROR.CART_REMOVE_FAILED)
  },
  showCartDeleteFailed(reason) {
    errorMessage.show(reason || ORDER_MESSAGES.ERROR.CART_DELETE_FAILED)
  },
  showCartClearFailed(reason) {
    errorMessage.show(reason || ORDER_MESSAGES.ERROR.CART_CLEAR_FAILED)
  },
  showCartItemOutOfStock(stock) {
    const msg = stock !== undefined ? `库存不足，当前可用库存：${stock}` : ORDER_MESSAGES.ERROR.CART_ITEM_OUT_OF_STOCK
    errorMessage.warning(msg)
  },
  showCartItemLimitReached(limit) {
    errorMessage.warning(`${ORDER_MESSAGES.ERROR.CART_ITEM_LIMIT_REACHED}(${limit}件)`)
  },
  showCartItemInvalid() {
    errorMessage.warning(ORDER_MESSAGES.ERROR.CART_ITEM_INVALID)
  },
  showSpecLoadFailed(reason) {
    errorMessage.show(reason || ORDER_MESSAGES.ERROR.SPEC_LOAD_FAILED)
  },
  showSpecUpdateFailed(reason) {
    errorMessage.show(reason || ORDER_MESSAGES.ERROR.SPEC_UPDATE_FAILED)
  },
  showLogisticsLoadFailed(reason) {
    errorMessage.show(reason || ORDER_MESSAGES.ERROR.LOGISTICS_LOAD_FAILED)
  },
  showPaymentFailed(reason) {
    errorMessage.show(reason || ORDER_MESSAGES.ERROR.PAYMENT_FAILED)
  },
  showPaymentCanceled() {
    errorMessage.show(ORDER_MESSAGES.ERROR.PAYMENT_CANCELED)
  },
  showPaymentTimeout() {
    errorMessage.warning(ORDER_MESSAGES.ERROR.PAYMENT_TIMEOUT)
  },
  showPaymentOrderLoadFailed(reason) {
    errorMessage.show(reason || ORDER_MESSAGES.ERROR.PAYMENT_ORDER_LOAD_FAILED)
  },
  showInsufficientBalance(balance, amount) {
    const msg = balance !== undefined && amount !== undefined 
      ? `余额不足，当前余额￥${balance}，需支付￥${amount}` 
      : ORDER_MESSAGES.ERROR.INSUFFICIENT_BALANCE
    errorMessage.warning(msg)
  },
  showRefundSubmitFailed(reason) {
    errorMessage.show(reason || ORDER_MESSAGES.ERROR.REFUND_SUBMIT_FAILED)
  },
  showRefundProcessFailed(action, reason) {
    const msg = reason ? `${action}退款申请失败: ${reason}` : ORDER_MESSAGES.ERROR.REFUND_PROCESS_FAILED
    errorMessage.show(msg)
  },
  showRefundDetailLoadFailed() {
    errorMessage.warning(ORDER_MESSAGES.ERROR.REFUND_DETAIL_LOAD_FAILED)
  },
  showBatchShipFailed(reason) {
    errorMessage.show(reason || ORDER_MESSAGES.ERROR.BATCH_SHIP_FAILED)
  },
  showStatisticsLoadFailed(reason) {
    errorMessage.show(reason || ORDER_MESSAGES.ERROR.STATISTICS_LOAD_FAILED)
  },
  showExportFailed(reason) {
    errorMessage.show(reason || ORDER_MESSAGES.ERROR.EXPORT_FAILED)
  },
  showAddressLoadFailed(reason) {
    errorMessage.show(reason || ORDER_MESSAGES.ERROR.ADDRESS_LOAD_FAILED)
  },
  showAddressSaveFailed(reason) {
    errorMessage.show(reason || ORDER_MESSAGES.ERROR.ADDRESS_SAVE_FAILED)
  },
  showProductInfoExpired() {
    errorMessage.show(ORDER_MESSAGES.ERROR.PRODUCT_INFO_EXPIRED)
  },
  showProductInfoLoadFailed(reason) {
    errorMessage.show(reason || ORDER_MESSAGES.ERROR.PRODUCT_INFO_LOAD_FAILED)
  },
  showOrderSubmitFailed(reason) {
    errorMessage.show(reason || ORDER_MESSAGES.ERROR.ORDER_SUBMIT_FAILED)
  },
  showStockInsufficient(availableStock, quantity) {
    const msg = availableStock !== undefined && quantity !== undefined
      ? `商品库存不足，当前可用库存：${availableStock}，您需要：${quantity}`
      : ORDER_MESSAGES.ERROR.CART_ITEM_OUT_OF_STOCK
    errorMessage.show(msg)
  },
  showReviewSubmitFailed(reason) {
    errorMessage.show(reason || '评价提交失败，请稍后重试')
  }
}

// 提示消息函数
export const orderPromptMessages = {
  showAddressRequired() {
    promptMessage.show(ORDER_MESSAGES.PROMPT.ADDRESS_REQUIRED)
  },
  showAddressInvalid() {
    promptMessage.show(ORDER_MESSAGES.PROMPT.ADDRESS_INVALID)
  },
  showOrderEmpty() {
    promptMessage.show(ORDER_MESSAGES.PROMPT.ORDER_EMPTY)
  },
  showOrderInfoInvalid() {
    promptMessage.show(ORDER_MESSAGES.PROMPT.ORDER_INFO_INVALID)
  },
  showOrderNotFound() {
    promptMessage.show(ORDER_MESSAGES.PROMPT.ORDER_NOT_FOUND)
  },
  showCancelReasonRequired() {
    promptMessage.show(ORDER_MESSAGES.PROMPT.CANCEL_REASON_REQUIRED)
  },
  showLogisticsCompanyRequired() {
    promptMessage.show(ORDER_MESSAGES.PROMPT.LOGISTICS_COMPANY_REQUIRED)
  },
  showLogisticsNumberRequired() {
    promptMessage.show(ORDER_MESSAGES.PROMPT.LOGISTICS_NUMBER_REQUIRED)
  },
  showLogisticsNumberInvalid() {
    promptMessage.show(ORDER_MESSAGES.PROMPT.LOGISTICS_NUMBER_INVALID)
  },
  showReviewContentRequired() {
    promptMessage.show(ORDER_MESSAGES.PROMPT.REVIEW_CONTENT_REQUIRED)
  },
  showReviewRatingRequired() {
    promptMessage.show(ORDER_MESSAGES.PROMPT.REVIEW_RATING_REQUIRED)
  },
  showReviewLimitExceeded() {
    promptMessage.show(ORDER_MESSAGES.PROMPT.REVIEW_LIMIT_EXCEEDED)
  },
  showQuantityInvalid() {
    promptMessage.show(ORDER_MESSAGES.PROMPT.QUANTITY_INVALID)
  },
  showSelectionRequired() {
    promptMessage.show(ORDER_MESSAGES.PROMPT.SELECTION_REQUIRED)
  },
  showDeleteSelectionRequired() {
    promptMessage.show(ORDER_MESSAGES.PROMPT.DELETE_SELECTION_REQUIRED)
  },
  showShipSelectionRequired() {
    promptMessage.show(ORDER_MESSAGES.PROMPT.SHIP_SELECTION_REQUIRED)
  },
  showCartEmpty() {
    promptMessage.show(ORDER_MESSAGES.PROMPT.CART_EMPTY)
  },
  showSelectPaymentMethod() {
    promptMessage.show(ORDER_MESSAGES.PROMPT.SELECT_PAYMENT_METHOD)
  },
  showSelectExportFormat() {
    promptMessage.show(ORDER_MESSAGES.PROMPT.SELECT_EXPORT_FORMAT)
  },
  showCancelConfirm() {
    promptMessage.info(ORDER_MESSAGES.PROMPT.CANCEL_CONFIRM)
  },
  showConfirmReceive() {
    promptMessage.info(ORDER_MESSAGES.PROMPT.CONFIRM_RECEIVE)
  },
  showDeleteOrderConfirm() {
    promptMessage.info(ORDER_MESSAGES.PROMPT.DELETE_ORDER_CONFIRM)
  },
  showRemoveCartConfirm() {
    promptMessage.info(ORDER_MESSAGES.PROMPT.REMOVE_CART_CONFIRM)
  },
  showClearCartConfirm() {
    promptMessage.info(ORDER_MESSAGES.PROMPT.CLEAR_CART_CONFIRM)
  },
  showConfirmPayment(amount) {
    promptMessage.info(amount ? `确认支付¥${amount}？` : ORDER_MESSAGES.PROMPT.CONFIRM_PAYMENT)
  },
  showSpecRequired() {
    promptMessage.show(ORDER_MESSAGES.PROMPT.SPEC_REQUIRED)
  },
  showNoSpecAvailable() {
    promptMessage.show(ORDER_MESSAGES.PROMPT.NO_SPEC_AVAILABLE)
  },
  showStockInsufficient(stock) {
    const msg = stock !== undefined ? `库存不足，当前可用库存：${stock}` : ORDER_MESSAGES.PROMPT.STOCK_INSUFFICIENT
    promptMessage.show(msg)
  },
  showRefundReasonTooShort() {
    promptMessage.show(ORDER_MESSAGES.PROMPT.REFUND_REASON_TOO_SHORT)
  },
  showRefundNotSupported() {
    promptMessage.info(ORDER_MESSAGES.PROMPT.REFUND_NOT_SUPPORTED)
  },
  showNoLogisticsInfo() {
    promptMessage.info(ORDER_MESSAGES.PROMPT.NO_LOGISTICS_INFO)
  },
  // 开发中功能提示
  showContactSellerDev() {
    promptMessage.info(ORDER_MESSAGES.PROMPT.CONTACT_SELLER_DEV)
  },
  showBuyAgainDev() {
    promptMessage.info(ORDER_MESSAGES.PROMPT.BUY_AGAIN_DEV)
  },
  showDeleteOrderDev() {
    promptMessage.info(ORDER_MESSAGES.PROMPT.DELETE_ORDER_DEV)
  },
  showViewLogisticsDev() {
    promptMessage.info(ORDER_MESSAGES.PROMPT.VIEW_LOGISTICS_DEV)
  },
  showModifyAddressDev() {
    promptMessage.info(ORDER_MESSAGES.PROMPT.MODIFY_ADDRESS_DEV)
  },
  showContactShopDev() {
    promptMessage.info(ORDER_MESSAGES.PROMPT.CONTACT_SHOP_DEV)
  },
  showOrderInfoIncomplete() {
    promptMessage.show(ORDER_MESSAGES.PROMPT.ORDER_INFO_INCOMPLETE)
  },
  showReviewContentTooShort() {
    promptMessage.show(ORDER_MESSAGES.PROMPT.REVIEW_CONTENT_TOO_SHORT)
  },
  showReviewNotAllowed() {
    promptMessage.show(ORDER_MESSAGES.PROMPT.REVIEW_NOT_ALLOWED)
  },
  showAlreadyReviewed() {
    promptMessage.show(ORDER_MESSAGES.PROMPT.ALREADY_REVIEWED)
  },
  showImageLimitExceeded() {
    promptMessage.show(ORDER_MESSAGES.PROMPT.IMAGE_LIMIT_EXCEEDED)
  },
  showRatingRequired() {
    promptMessage.show(ORDER_MESSAGES.PROMPT.RATING_REQUIRED)
  }
}

// 默认导出
export default {
  success: orderSuccessMessages,
  error: orderErrorMessages,
  prompt: orderPromptMessages,
  ORDER_MESSAGES
}
