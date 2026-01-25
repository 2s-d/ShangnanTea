/* eslint-disable no-useless-catch */
import { 
  createOrder, 
  getOrders, 
  getOrderDetail, 
  payOrder, 
  cancelOrder,
  confirmOrder,
  refundOrder,
  reviewOrder,
  uploadReviewImage as uploadReviewImageApi,
  processRefund,
  getRefundDetail,
  shipOrder,
  batchShipOrders,
  getOrderLogistics,
  getOrderStatistics,
  exportOrders,
  // 引入购物车相关API
  getCartItems,
  addToCart,
  updateCartItem,
  removeFromCart,
  clearCart
} from '@/api/order'

const state = () => ({
  // 订单相关状态
  orderList: [],
  currentOrder: null,
  pagination: {
    total: 0,
    currentPage: 1,
    pageSize: 10
  },
  // 筛选条件
  filters: {},
  // 订单统计数据
  orderStatistics: null,
  
  // 购物车相关状态
  cartItems: [],
  
  // 加载状态
  loading: false
})

const getters = {
  // 订单状态文字
  orderStatusText: () => status => {
    const statusMap = {
      0: '待付款',
      1: '待发货',
      2: '待收货',
      3: '已完成',
      4: '已取消',
      5: '已退款'
    }
    return statusMap[status] || '未知状态'
  },
  
  // 购物车商品总数
  cartItemCount: state => state.cartItems.reduce((sum, item) => sum + item.quantity, 0),
  
  // 购物车商品总价
  cartTotalPrice: state => state.cartItems.reduce((sum, item) => sum + item.price * item.quantity, 0).toFixed(2),
  
  // 是否有选中的商品
  hasSelectedItems: state => state.cartItems.some(item => item.selected)
}

const mutations = {
  // 订单相关mutations
  SET_ORDER_LIST(state, list) {
    state.orderList = list
  },
  SET_CURRENT_ORDER(state, order) {
    state.currentOrder = order
  },
  SET_PAGINATION(state, { total, currentPage, pageSize }) {
    state.pagination = {
      total,
      currentPage,
      pageSize
    }
  },
  UPDATE_ORDER_STATUS(state, { id, status }) {
    // 更新列表中的订单状态
    const order = state.orderList.find(o => o.id === id)
    if (order) {
      order.status = status
    }
    
    // 更新当前查看的订单状态
    if (state.currentOrder && state.currentOrder.id === id) {
      state.currentOrder.status = status
    }
  },
  
  // 购物车相关mutations
  SET_CART_ITEMS(state, items) {
    state.cartItems = items
  },
  UPDATE_CART_ITEM(state, { id, quantity }) {
    const item = state.cartItems.find(item => item.id === id)
    if (item) {
      item.quantity = quantity
    }
  },
  REMOVE_CART_ITEM(state, id) {
    state.cartItems = state.cartItems.filter(item => item.id !== id)
  },
  CLEAR_CART(state) {
    state.cartItems = []
  },
  
  // 通用mutations
  SET_LOADING(state, status) {
    state.loading = status
  },
  SET_FILTERS(state, filters) {
    state.filters = { ...state.filters, ...filters }
  },
  SET_ORDER_STATISTICS(state, statistics) {
    state.orderStatistics = statistics
  }
}

const actions = {
  // === 购物车相关actions ===
  
  // 获取购物车列表 - 成功码200, 失败码5100
  async fetchCartItems({ commit }) {
    try {
      commit('SET_LOADING', true)
      const res = await getCartItems()
      commit('SET_CART_ITEMS', res.data)
      return res
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // 添加商品到购物车 - 成功码5000, 失败码5101/5102/5103/5104
  async addToCart({ commit, dispatch }, { teaId, quantity, specificationId }) {
    try {
      commit('SET_LOADING', true)
      const res = await addToCart({ teaId, quantity, specificationId })
      // 刷新购物车列表
      dispatch('fetchCartItems')
      return res
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // 更新购物车商品 - 成功码5001/5002, 失败码5105/5106/5107
  async updateCartItem({ commit, dispatch }, { id, quantity, specificationId }) {
    try {
      commit('SET_LOADING', true)
      const res = await updateCartItem({ id, quantity, specificationId })
      // 乐观更新前端状态
      if (quantity !== undefined) {
        commit('UPDATE_CART_ITEM', { id, quantity })
      }
      return res
    } catch (error) {
      // 如果失败，重新获取购物车
      dispatch('fetchCartItems')
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // 移除购物车商品 - 成功码5003, 失败码5108
  async removeFromCart({ commit }, id) {
    try {
      commit('SET_LOADING', true)
      const res = await removeFromCart(id)
      // 乐观更新前端状态
      commit('REMOVE_CART_ITEM', id)
      return res
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // 清空购物车 - 成功码5004, 失败码5109
  async clearCart({ commit }) {
    try {
      commit('SET_LOADING', true)
      const res = await clearCart()
      commit('CLEAR_CART')
      return res
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // === 订单相关actions ===
  
  // 创建订单 - 成功码5005, 失败码5110/5111/5112
  async createOrder({ commit, dispatch }, orderData) {
    try {
      commit('SET_LOADING', true)
      const res = await createOrder(orderData)
      // 订单创建成功后清空购物车
      if (orderData.fromCart) {
        await dispatch('clearCart')
      }
      return res // 返回 {code, data}
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // 获取订单列表 - 成功码200, 失败码5113
  async fetchOrders({ commit, state }, params = {}) {
    try {
      commit('SET_LOADING', true)
      const queryParams = {
        page: state.pagination.currentPage,
        size: state.pagination.pageSize,
        ...params
      }
      const res = await getOrders(queryParams)
      const data = res.data
      commit('SET_ORDER_LIST', data?.list || [])
      commit('SET_PAGINATION', {
        total: data?.total || 0,
        currentPage: state.pagination.currentPage,
        pageSize: state.pagination.pageSize
      })
      return res // 返回 {code, data}
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // 获取订单详情 - 成功码200, 失败码5114/5115/5116
  async fetchOrderDetail({ commit }, id) {
    try {
      commit('SET_LOADING', true)
      const res = await getOrderDetail(id)
      commit('SET_CURRENT_ORDER', res.data)
      return res // 返回 {code, data}
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // 支付订单 - 成功码5006/5007, 失败码5117/5118/5119/5120
  async payOrder({ commit }, { orderId, paymentMethod }) {
    try {
      commit('SET_LOADING', true)
      const res = await payOrder({ orderId, paymentMethod })
      // 更新订单状态
      commit('UPDATE_ORDER_STATUS', { id: orderId, status: 1 }) // 1:待发货
      return res // 返回 {code, data}
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // 取消订单 - 成功码5008, 失败码5121/5122/5123
  async cancelOrder({ commit }, id) {
    try {
      commit('SET_LOADING', true)
      const res = await cancelOrder(id)
      // 更新订单状态
      commit('UPDATE_ORDER_STATUS', { id, status: 4 }) // 4:已取消
      return res // 返回 {code, data}
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // 确认收货 - 成功码5009, 失败码5124/5125/5126
  async confirmReceipt({ commit }, id) {
    try {
      commit('SET_LOADING', true)
      const res = await confirmOrder(id)
      // 更新订单状态：已完成
      commit('UPDATE_ORDER_STATUS', { id, status: 3 })
      return res // 返回 {code, data}
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // 评价订单 - 成功码5010, 失败码5127
  async submitOrderReview({ commit }, payload) {
    try {
      commit('SET_LOADING', true)
      const res = await reviewOrder(payload)
      return res // 返回 {code, data}
    } catch (error) {
      console.error('提交评价失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // 申请退款 - 成功码5011, 失败码5128/5129/5130
  async applyRefund({ commit }, { orderId, reason }) {
    try {
      commit('SET_LOADING', true)
      const res = await refundOrder({ orderId, reason })
      return res // 返回 {code, data}
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // 更新筛选条件
  updateFilters({ commit }, filters) {
    commit('SET_FILTERS', filters)
  },
  
  // === 发货相关 actions ===
  
  // 单个订单发货 - 成功码5014, 失败码5135/5136/5137
  async shipOrder({ commit }, { id, logisticsCompany, logisticsNumber }) {
    try {
      commit('SET_LOADING', true)
      const res = await shipOrder({ id, logisticsCompany, logisticsNumber })
      // 更新订单状态为待收货
      commit('UPDATE_ORDER_STATUS', { id, status: 2 })
      return res // 返回 {code, data}
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // 批量发货 - 成功码5015, 失败码5138/5139
  async batchShipOrders({ commit }, { orderIds, logisticsCompany, logisticsNumber }) {
    try {
      commit('SET_LOADING', true)
      const res = await batchShipOrders({ orderIds, logisticsCompany, logisticsNumber })
      // 更新所有订单状态为待收货
      orderIds.forEach(id => {
        commit('UPDATE_ORDER_STATUS', { id, status: 2 })
      })
      return res // 返回 {code, data}
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // 获取订单物流信息 - 成功码200, 失败码5140/5141
  async fetchOrderLogistics(_, id) {
    try {
      const res = await getOrderLogistics(id)
      return res // 返回 {code, data}
    } catch (error) {
      console.error('获取物流信息失败:', error)
      throw error
    }
  },
  
  // === 退款相关 actions ===
  
  // 审批退款 - 成功码5012/5013, 失败码5131/5132
  async processRefund({ commit }, { orderId, approve, reason }) {
    try {
      commit('SET_LOADING', true)
      const res = await processRefund({ orderId, approve, reason })
      // 更新订单状态
      if (approve) {
        commit('UPDATE_ORDER_STATUS', { id: orderId, status: 5 }) // 已退款
      }
      return res // 返回 {code, data}
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // 获取退款详情 - 成功码200, 失败码5133/5134
  async fetchRefundDetail(_, orderId) {
    try {
      const res = await getRefundDetail(orderId)
      return res // 返回 {code, data}
    } catch (error) {
      console.error('获取退款详情失败:', error)
      throw error
    }
  },
  
  // === 统计与导出 actions ===
  
  // 获取订单统计数据 - 成功码200, 失败码5142
  async fetchOrderStatistics({ commit }, params = {}) {
    try {
      const res = await getOrderStatistics(params)
      commit('SET_ORDER_STATISTICS', res.data)
      return res // 返回 {code, data}
    } catch (error) {
      console.error('获取订单统计失败:', error)
      throw error
    }
  },
  
  // 导出订单数据 - 成功码200, 失败码5143
  async exportOrders({ commit }, params = {}) {
    try {
      commit('SET_LOADING', true)
      const blob = await exportOrders(params)
      
      // 创建下载链接
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      const format = params.format || 'csv'
      link.download = `orders_${new Date().toISOString().slice(0, 10)}.${format}`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
      
      return { code: 200, data: true } // 导出成功
    } catch (error) {
      console.error('导出订单失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // 更新分页
  setPage({ commit, dispatch, state }, { page, extraParams = {} }) {
    commit('SET_PAGINATION', {
      ...state.pagination,
      currentPage: page
    })
    return dispatch('fetchOrders', extraParams)
  },
  
  // 上传订单评价图片
  async uploadReviewImage({ commit }, file) {
    try {
      commit('SET_LOADING', true)
      const res = await uploadReviewImageApi(file)
      return res // 返回 {code, data}
    } catch (error) {
      console.error('上传评价图片失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  }
}

export default {
  namespaced: true,
  state,
  getters,
  mutations,
  actions
} 