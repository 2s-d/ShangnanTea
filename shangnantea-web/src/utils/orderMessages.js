/**
 * 订单模块消息工具
 * 按照三层架构设计消息：
 * 1. API层：处理网络请求相关消息
 * 2. Vuex业务层：处理业务操作结果消息
 * 3. UI交互层：处理界面交互提示消息
 * 
 * 整合了购物车和支付相关消息，为订单模块提供完整的消息工具
 */
import messageManager from './messageManager'

// 订单模块常用消息
export const ORDER_MESSAGES = {
  // API层消息
  API: {
    // 订单相关
    LOADING_ORDERS: '正在加载订单列表...',
    LOADING_ORDER_DETAIL: '正在加载订单详情...',
    CREATING_ORDER: '正在创建订单...',
    CANCELING_ORDER: '正在取消订单...',
    CONFIRMING_ORDER: '正在确认收货...',
    SHIPPING_ORDER: '正在发货...',
    LOADING_LOGISTICS: '正在加载物流信息...',
    LOADING_FAILED: '加载失败',
    CREATE_FAILED: '创建订单失败',
    CANCEL_FAILED: '取消订单失败',
    CONFIRM_FAILED: '确认收货失败',
    SHIPPING_FAILED: '发货失败',
    
    // 购物车相关
    LOADING_CART: '正在加载购物车...',
    ADDING_TO_CART: '正在添加到购物车...',
    UPDATING_CART: '正在更新购物车...',
    REMOVING_FROM_CART: '正在从购物车移除商品...',
    CLEARING_CART: '正在清空购物车...',
    CART_LOAD_FAILED: '购物车加载失败',
    CART_ADD_FAILED: '添加到购物车失败',
    CART_UPDATE_FAILED: '购物车更新失败',
    CART_REMOVE_FAILED: '从购物车移除失败',
    CART_CLEAR_FAILED: '清空购物车失败',
    
    // 支付相关
    PAYMENT_PROCESSING: '正在处理支付请求...',
    PAYMENT_SUCCESS: '支付成功',
    PAYMENT_FAILED: '支付失败',
    PAYMENT_CANCELED: '支付已取消',
    PAYMENT_TIMEOUT: '支付超时',
    LOADING_PAYMENT_METHODS: '正在加载支付方式...',
    PAYMENT_METHODS_LOAD_FAILED: '加载支付方式失败'
  },
  
  // 业务层消息
  BUSINESS: {
    // 订单相关
    ORDER_CREATED: '订单创建成功',
    ORDER_CANCELED: '订单已取消',
    ORDER_CONFIRMED: '已确认收货',
    ORDER_SHIPPED: '订单已发货',
    ORDER_REVIEWED: '订单评价成功',
    ORDER_DETAIL_LOADED: '订单详情加载完成',
    ORDER_LIST_LOADED: '订单列表加载完成',
    ORDER_STATUS_UPDATED: '订单状态已更新',
    LOGISTICS_UPDATED: '物流信息已更新',
    LOGISTICS_UPLOADED: '物流单号已上传',
    ORDER_NOT_FOUND: '订单不存在',
    ORDER_OPERATION_UNAUTHORIZED: '您没有权限操作此订单',
    
    // 购物车相关
    ADDED_TO_CART: '已加入购物车',
    ADD_TO_CART_FAILED: '加入购物车失败',
    CART_QUANTITY_UPDATED: '购物车数量已更新',
    ITEM_REMOVED: '已从购物车中移除',
    CART_CLEARED: '购物车已清空',
    CART_SELECTED_UPDATED: '已更新商品选中状态',
    CART_LOADED: '购物车已加载',
    CART_ITEM_OUT_OF_STOCK: '商品库存不足',
    CART_ITEM_LIMIT_REACHED: '已达到购买数量上限',
    CART_ITEM_INVALID: '商品已下架或不可用',
    
    // 支付相关
    PAYMENT_COMPLETED: '支付已完成',
    PAYMENT_FAILED_BUSINESS: '支付失败，请稍后重试',
    ORDER_PAID: '订单已支付',
    ORDER_PAYMENT_FAILED: '订单支付失败',
    ORDER_CREATION_SUCCESS: '订单创建成功',
    ORDER_CREATION_FAILED: '订单创建失败',
    INSUFFICIENT_BALANCE: '余额不足',
    PAYMENT_METHOD_CHANGED: '支付方式已更改',
    PAYMENT_STATUS_UPDATED: '支付状态已更新'
  },
  
  // UI交互层消息
  UI: {
    // 订单相关
    ADDRESS_REQUIRED: '请选择收货地址',
    ADDRESS_INVALID: '收货地址不完整',
    ORDER_EMPTY: '订单不能为空',
    CANCEL_REASON_REQUIRED: '请选择取消原因',
    SHIPPING_INFO_REQUIRED: '请填写物流信息',
    LOGISTICS_COMPANY_REQUIRED: '请选择物流公司',
    LOGISTICS_NUMBER_REQUIRED: '请填写物流单号',
    LOGISTICS_NUMBER_INVALID: '物流单号格式不正确',
    REVIEW_CONTENT_REQUIRED: '请填写评价内容',
    REVIEW_RATING_REQUIRED: '请为商品评分',
    CANCEL_CONFIRM: '确定要取消该订单吗？',
    CONFIRM_RECEIVE_CONFIRM: '确认已收到商品吗？确认后无法撤销',
    DELETE_ORDER_CONFIRM: '确定要删除该订单记录吗？',
    PAYMENT_REQUIRED: '请先完成支付',
    REVIEW_LIMIT_EXCEEDED: '评价内容不能超过500字',
    
    // 购物车相关
    QUANTITY_INVALID: '商品数量不正确',
    SELECTION_REQUIRED: '请选择要结算的商品',
    ITEM_SELECTED: '已选择{count}件商品',
    CART_EMPTY: '购物车是空的',
    PROCEED_CHECKOUT_WARNING: '请先添加商品到购物车',
    REMOVE_CONFIRM: '确定要从购物车中移除此商品吗？',
    CLEAR_CONFIRM: '确定要清空购物车吗？',
    MAX_QUANTITY_LIMIT: '单个商品最多购买{limit}件',
    OUT_OF_STOCK: '{teaName}库存不足，当前库存{stock}件',
    
    // 支付相关
    SELECT_PAYMENT_METHOD: '请选择支付方式',
    CONFIRM_PAYMENT: '确认支付¥{amount}？',
    PROCESSING_PAYMENT: '支付处理中，请稍候...',
    PAYMENT_AMOUNT_INVALID: '支付金额不正确',
    COUPON_APPLIED: '优惠券已应用，优惠{discount}元',
    COUPON_INVALID: '优惠券{code}无效或已过期',
    PAYMENT_INFO_INCOMPLETE: '请完善支付信息'
  }
}

// 订单API层消息工具
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
  
  // 订单相关预定义API消息
  showLoadingOrders() {
    this.showLoading(ORDER_MESSAGES.API.LOADING_ORDERS)
  },
  
  showLoadingOrderDetail() {
    this.showLoading(ORDER_MESSAGES.API.LOADING_ORDER_DETAIL)
  },
  
  showCreatingOrder() {
    this.showLoading(ORDER_MESSAGES.API.CREATING_ORDER)
  },
  
  showCancelingOrder() {
    this.showLoading(ORDER_MESSAGES.API.CANCELING_ORDER)
  },
  
  showConfirmingOrder() {
    this.showLoading(ORDER_MESSAGES.API.CONFIRMING_ORDER)
  },
  
  showShippingOrder() {
    this.showLoading(ORDER_MESSAGES.API.SHIPPING_ORDER)
  },
  
  showLoadingLogistics() {
    this.showLoading(ORDER_MESSAGES.API.LOADING_LOGISTICS)
  },
  
  showLoadingFailed(error) {
    const errorMsg = error?.message || ORDER_MESSAGES.API.LOADING_FAILED
    this.showError(errorMsg)
  },
  
  showCreateFailed(error) {
    const errorMsg = error?.message || ORDER_MESSAGES.API.CREATE_FAILED
    this.showError(errorMsg)
  },
  
  showCancelFailed(error) {
    const errorMsg = error?.message || ORDER_MESSAGES.API.CANCEL_FAILED
    this.showError(errorMsg)
  },
  
  showConfirmFailed(error) {
    const errorMsg = error?.message || ORDER_MESSAGES.API.CONFIRM_FAILED
    this.showError(errorMsg)
  },
  
  showShippingFailed(error) {
    const errorMsg = error?.message || ORDER_MESSAGES.API.SHIPPING_FAILED
    this.showError(errorMsg)
  },
  
  // 购物车相关预定义API消息
  showLoadingCart() {
    this.showLoading(ORDER_MESSAGES.API.LOADING_CART)
  },
  
  showAddingToCart() {
    this.showLoading(ORDER_MESSAGES.API.ADDING_TO_CART)
  },
  
  showUpdatingCart() {
    this.showLoading(ORDER_MESSAGES.API.UPDATING_CART)
  },
  
  showRemovingFromCart() {
    this.showLoading(ORDER_MESSAGES.API.REMOVING_FROM_CART)
  },
  
  showClearingCart() {
    this.showLoading(ORDER_MESSAGES.API.CLEARING_CART)
  },
  
  showCartLoadFailed(error) {
    const errorMsg = error?.message || ORDER_MESSAGES.API.CART_LOAD_FAILED
    this.showError(errorMsg)
  },
  
  showCartAddFailed(error) {
    const errorMsg = error?.message || ORDER_MESSAGES.API.CART_ADD_FAILED
    this.showError(errorMsg)
  },
  
  showCartUpdateFailed(error) {
    const errorMsg = error?.message || ORDER_MESSAGES.API.CART_UPDATE_FAILED
    this.showError(errorMsg)
  },
  
  showCartRemoveFailed(error) {
    const errorMsg = error?.message || ORDER_MESSAGES.API.CART_REMOVE_FAILED
    this.showError(errorMsg)
  },
  
  showCartClearFailed(error) {
    const errorMsg = error?.message || ORDER_MESSAGES.API.CART_CLEAR_FAILED
    this.showError(errorMsg)
  },
  
  // 支付相关预定义API消息
  showPaymentProcessing() {
    this.showLoading(ORDER_MESSAGES.API.PAYMENT_PROCESSING)
  },
  
  showPaymentSuccess() {
    this.showSuccess(ORDER_MESSAGES.API.PAYMENT_SUCCESS)
  },
  
  showPaymentFailed(error) {
    const errorMsg = error?.message || ORDER_MESSAGES.API.PAYMENT_FAILED
    this.showError(errorMsg)
  },
  
  showPaymentCanceled() {
    this.showError(ORDER_MESSAGES.API.PAYMENT_CANCELED)
  },
  
  showPaymentTimeout() {
    this.showError(ORDER_MESSAGES.API.PAYMENT_TIMEOUT)
  },
  
  showLoadingPaymentMethods() {
    this.showLoading(ORDER_MESSAGES.API.LOADING_PAYMENT_METHODS)
  },
  
  showPaymentMethodsLoadFailed(error) {
    const errorMsg = error?.message || ORDER_MESSAGES.API.PAYMENT_METHODS_LOAD_FAILED
    this.showError(errorMsg)
  }
}

// 订单业务层消息工具
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
  
  // 显示业务信息消息
  showInfo(message) {
    messageManager.business.showInfo(message)
  },
  
  // 订单相关预定义业务消息
  showOrderCreated() {
    this.showSuccess(ORDER_MESSAGES.BUSINESS.ORDER_CREATED)
  },
  
  showOrderCanceled() {
    this.showSuccess(ORDER_MESSAGES.BUSINESS.ORDER_CANCELED)
  },
  
  showOrderConfirmed() {
    this.showSuccess(ORDER_MESSAGES.BUSINESS.ORDER_CONFIRMED)
  },
  
  showOrderShipped() {
    this.showSuccess(ORDER_MESSAGES.BUSINESS.ORDER_SHIPPED)
  },
  
  showOrderReviewed() {
    this.showSuccess(ORDER_MESSAGES.BUSINESS.ORDER_REVIEWED)
  },
  
  showOrderStatusUpdated(status) {
    const statusText = status ? `为${status}` : ''
    this.showSuccess(`${ORDER_MESSAGES.BUSINESS.ORDER_STATUS_UPDATED}${statusText}`)
  },
  
  showLogisticsUpdated() {
    this.showSuccess(ORDER_MESSAGES.BUSINESS.LOGISTICS_UPDATED)
  },
  
  showLogisticsUploaded() {
    this.showSuccess(ORDER_MESSAGES.BUSINESS.LOGISTICS_UPLOADED)
  },
  
  showOrderNotFound() {
    this.showWarning(ORDER_MESSAGES.BUSINESS.ORDER_NOT_FOUND)
  },
  
  showOrderOperationUnauthorized() {
    this.showWarning(ORDER_MESSAGES.BUSINESS.ORDER_OPERATION_UNAUTHORIZED)
  },
  
  // 购物车相关预定义业务消息
  showAddToCartSuccess(teaName, quantity = 1) {
    const message = quantity > 1 
      ? `已将${teaName} ${quantity}件加入购物车`
      : `已将${teaName}加入购物车`
    this.showSuccess(message)
  },
  
  showAddToCartFailure(reason) {
    const errorMsg = reason || ORDER_MESSAGES.BUSINESS.ADD_TO_CART_FAILED
    this.showError(errorMsg)
  },
  
  showQuantityUpdated(teaName, quantity) {
    const message = teaName 
      ? `${teaName}数量已更新为${quantity}`
      : ORDER_MESSAGES.BUSINESS.CART_QUANTITY_UPDATED
    this.showSuccess(message)
  },
  
  showItemRemoved(teaName) {
    const message = teaName 
      ? `已将${teaName}从购物车中移除`
      : ORDER_MESSAGES.BUSINESS.ITEM_REMOVED
    this.showSuccess(message)
  },
  
  showCartCleared() {
    this.showSuccess(ORDER_MESSAGES.BUSINESS.CART_CLEARED)
  },
  
  showCartItemLimitReached(limit) {
    this.showWarning(`${ORDER_MESSAGES.BUSINESS.CART_ITEM_LIMIT_REACHED}(${limit}件)`)
  },
  
  showCartItemOutOfStock(teaName, stock) {
    const message = teaName && stock !== undefined
      ? `${teaName}库存不足，当前库存${stock}件`
      : ORDER_MESSAGES.BUSINESS.CART_ITEM_OUT_OF_STOCK
    this.showWarning(message)
  },
  
  showCartItemInvalid() {
    this.showWarning(ORDER_MESSAGES.BUSINESS.CART_ITEM_INVALID)
  },
  
  // 支付相关预定义业务消息
  showPaymentSuccess(orderNo) {
    const message = orderNo
      ? `订单${orderNo}支付成功`
      : ORDER_MESSAGES.BUSINESS.PAYMENT_COMPLETED
    this.showSuccess(message)
  },
  
  showPaymentFailure(reason) {
    const errorMsg = reason || ORDER_MESSAGES.BUSINESS.PAYMENT_FAILED_BUSINESS
    this.showError(errorMsg)
  },
  
  showOrderPaid(orderNo, amount) {
    const message = orderNo && amount
      ? `订单${orderNo}已支付成功，金额￥${amount}`
      : ORDER_MESSAGES.BUSINESS.ORDER_PAID
    this.showSuccess(message)
  },
  
  showOrderPaymentFailed(reason) {
    const errorMsg = reason || ORDER_MESSAGES.BUSINESS.ORDER_PAYMENT_FAILED
    this.showError(errorMsg)
  },
  
  showOrderCreationSuccess(orderNo) {
    const message = orderNo
      ? `订单${orderNo}创建成功`
      : ORDER_MESSAGES.BUSINESS.ORDER_CREATION_SUCCESS
    this.showSuccess(message)
  },
  
  showOrderCreationFailed(reason) {
    const errorMsg = reason || ORDER_MESSAGES.BUSINESS.ORDER_CREATION_FAILED
    this.showError(errorMsg)
  },
  
  showBalanceInsufficient(balance, amount) {
    const message = balance !== undefined && amount !== undefined
      ? `余额不足，当前余额￥${balance}，需支付￥${amount}`
      : ORDER_MESSAGES.BUSINESS.INSUFFICIENT_BALANCE
    this.showWarning(message)
  },
  
  showPaymentMethodChanged(method) {
    const methodText = method || ''
    this.showInfo(`${ORDER_MESSAGES.BUSINESS.PAYMENT_METHOD_CHANGED}${methodText ? '为' + methodText : ''}`)
  },
  
  showPaymentStatusUpdated(status) {
    const statusText = status || ''
    this.showInfo(`${ORDER_MESSAGES.BUSINESS.PAYMENT_STATUS_UPDATED}${statusText ? '为' + statusText : ''}`)
  }
}

// 订单UI交互层消息工具
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
  
  // 订单相关预定义UI消息
  showAddressRequired() {
    this.showWarning(ORDER_MESSAGES.UI.ADDRESS_REQUIRED)
  },
  
  showAddressInvalid() {
    this.showWarning(ORDER_MESSAGES.UI.ADDRESS_INVALID)
  },
  
  showOrderEmpty() {
    this.showWarning(ORDER_MESSAGES.UI.ORDER_EMPTY)
  },
  
  showCancelReasonRequired() {
    this.showWarning(ORDER_MESSAGES.UI.CANCEL_REASON_REQUIRED)
  },
  
  showShippingInfoRequired() {
    this.showWarning(ORDER_MESSAGES.UI.SHIPPING_INFO_REQUIRED)
  },
  
  showLogisticsCompanyRequired() {
    this.showWarning(ORDER_MESSAGES.UI.LOGISTICS_COMPANY_REQUIRED)
  },
  
  showLogisticsNumberRequired() {
    this.showWarning(ORDER_MESSAGES.UI.LOGISTICS_NUMBER_REQUIRED)
  },
  
  showLogisticsNumberInvalid() {
    this.showWarning(ORDER_MESSAGES.UI.LOGISTICS_NUMBER_INVALID)
  },
  
  showReviewContentRequired() {
    this.showWarning(ORDER_MESSAGES.UI.REVIEW_CONTENT_REQUIRED)
  },
  
  showReviewRatingRequired() {
    this.showWarning(ORDER_MESSAGES.UI.REVIEW_RATING_REQUIRED)
  },
  
  showCancelConfirm() {
    return this.showConfirm(ORDER_MESSAGES.UI.CANCEL_CONFIRM)
  },
  
  showConfirmReceiveConfirm() {
    return this.showConfirm(ORDER_MESSAGES.UI.CONFIRM_RECEIVE_CONFIRM)
  },
  
  showDeleteOrderConfirm() {
    return this.showConfirm(ORDER_MESSAGES.UI.DELETE_ORDER_CONFIRM)
  },
  
  showPaymentRequired() {
    this.showWarning(ORDER_MESSAGES.UI.PAYMENT_REQUIRED)
  },
  
  showReviewLimitExceeded() {
    this.showWarning(ORDER_MESSAGES.UI.REVIEW_LIMIT_EXCEEDED)
  },
  
  // 购物车相关预定义UI消息
  showQuantityInvalid() {
    this.showWarning(ORDER_MESSAGES.UI.QUANTITY_INVALID)
  },
  
  showSelectionRequired() {
    this.showWarning(ORDER_MESSAGES.UI.SELECTION_REQUIRED)
  },
  
  showItemSelected(count) {
    const message = ORDER_MESSAGES.UI.ITEM_SELECTED.replace('{count}', count)
    this.showInfo(message)
  },
  
  showCartEmpty() {
    this.showWarning(ORDER_MESSAGES.UI.CART_EMPTY)
  },
  
  showProceedCheckoutWarning() {
    this.showWarning(ORDER_MESSAGES.UI.PROCEED_CHECKOUT_WARNING)
  },
  
  showRemoveConfirm() {
    return this.showConfirm(ORDER_MESSAGES.UI.REMOVE_CONFIRM)
  },
  
  showClearConfirm() {
    return this.showConfirm(ORDER_MESSAGES.UI.CLEAR_CONFIRM)
  },
  
  showMaxQuantityLimit(limit) {
    const message = ORDER_MESSAGES.UI.MAX_QUANTITY_LIMIT.replace('{limit}', limit)
    this.showWarning(message)
  },
  
  showOutOfStock(teaName, stock) {
    const message = ORDER_MESSAGES.UI.OUT_OF_STOCK
      .replace('{teaName}', teaName)
      .replace('{stock}', stock)
    this.showWarning(message)
  },
  
  // 支付相关预定义UI消息
  showSelectPaymentMethod() {
    this.showWarning(ORDER_MESSAGES.UI.SELECT_PAYMENT_METHOD)
  },
  
  showConfirmPayment(amount) {
    const message = ORDER_MESSAGES.UI.CONFIRM_PAYMENT.replace('{amount}', amount)
    return this.showConfirm(message)
  },
  
  showProcessingPayment() {
    this.showInfo(ORDER_MESSAGES.UI.PROCESSING_PAYMENT)
  },
  
  showPaymentAmountInvalid() {
    this.showWarning(ORDER_MESSAGES.UI.PAYMENT_AMOUNT_INVALID)
  },
  
  showCouponApplied(code, discount) {
    const message = ORDER_MESSAGES.UI.COUPON_APPLIED.replace('{discount}', discount)
    this.showSuccess(message)
  },
  
  showCouponInvalid(code) {
    const message = ORDER_MESSAGES.UI.COUPON_INVALID.replace('{code}', code)
    this.showWarning(message)
  },
  
  showPaymentInfoIncomplete(field) {
    const message = field
      ? `${ORDER_MESSAGES.UI.PAYMENT_INFO_INCOMPLETE}：${field}`
      : ORDER_MESSAGES.UI.PAYMENT_INFO_INCOMPLETE
    this.showWarning(message)
  }
}

// 导出所有消息工具
export default {
  api: apiMessages,
  business: businessMessages,
  ui: uiMessages,
  ORDER_MESSAGES
}
