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
  
  // 获取购物车列表
  async fetchCartItems({ commit }) {
    try {
      commit('SET_LOADING', true)
      
      const res = await getCartItems()
      commit('SET_CART_ITEMS', res)
      
      return res
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // 添加商品到购物车
  async addToCart({ commit, dispatch }, { teaId, quantity }) {
    try {
      commit('SET_LOADING', true)
      
      await addToCart({ teaId, quantity })
      
      // 刷新购物车列表
      dispatch('fetchCartItems')
      
      return true
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // 更新购物车商品数量
  async updateCartItem({ commit }, { id, quantity }) {
    try {
      commit('SET_LOADING', true)
      
      await updateCartItem({ id, quantity })
      
      // 乐观更新前端状态
      commit('UPDATE_CART_ITEM', { id, quantity })
      
      return true
    } catch (error) {
      // 如果失败，重新获取购物车
      commit('fetchCartItems')
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // 移除购物车商品
  async removeFromCart({ commit }, id) {
    try {
      commit('SET_LOADING', true)
      
      await removeFromCart(id)
      
      // 乐观更新前端状态
      commit('REMOVE_CART_ITEM', id)
      
      return true
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // 清空购物车
  async clearCart({ commit }) {
    try {
      commit('SET_LOADING', true)
      
      await clearCart()
      
      commit('CLEAR_CART')
      
      return true
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // === 订单相关actions ===
  
  // 创建订单
  async createOrder({ commit, dispatch }, orderData) {
    try {
      commit('SET_LOADING', true)
      
      const res = await createOrder(orderData)
      
      // 订单创建成功后清空购物车
      if (orderData.fromCart) {
        await dispatch('clearCart')
      }
      
      return res
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // 获取订单列表
  async fetchOrders({ commit, state }, params = {}) {
    try {
      commit('SET_LOADING', true)
      
      const queryParams = {
        page: state.pagination.currentPage,
        size: state.pagination.pageSize,
        ...params
      }
      
      const res = await getOrders(queryParams)
      
      commit('SET_ORDER_LIST', res.list)
      commit('SET_PAGINATION', {
        total: res.total,
        currentPage: state.pagination.currentPage,
        pageSize: state.pagination.pageSize
      })
      
      return res
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // 获取订单详情
  async fetchOrderDetail({ commit }, id) {
    try {
      commit('SET_LOADING', true)
      
      const res = await getOrderDetail(id)
      commit('SET_CURRENT_ORDER', res)
      
      return res
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // 支付订单
  async payOrder({ commit }, { orderId, paymentMethod }) {
    try {
      commit('SET_LOADING', true)
      
      const res = await payOrder({ orderId, paymentMethod })
      
      // 更新订单状态
      commit('UPDATE_ORDER_STATUS', { id: orderId, status: 1 }) // 1:待发货
      
      return res
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // 取消订单
  async cancelOrder({ commit }, id) {
    try {
      commit('SET_LOADING', true)
      
      await cancelOrder(id)
      
      // 更新订单状态
      commit('UPDATE_ORDER_STATUS', { id, status: 4 }) // 4:已取消
      
      return true
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // 确认收货
  async confirmReceipt({ commit }, id) {
    try {
      commit('SET_LOADING', true)

      await confirmOrder(id)

      // 更新订单状态：已完成
      commit('UPDATE_ORDER_STATUS', { id, status: 3 })

      return true
    } finally {
      commit('SET_LOADING', false)
    }
  },

  /**
   * 申请退款（生产版：走后端接口，不在前端本地模拟状态）
   * @param {Object} context Vuex context
   * @param {Object} payload { orderId, reason }
   * @returns {Promise} 接口返回
   */
  async applyRefund({ commit }, { orderId, reason }) {
    try {
      commit('SET_LOADING', true)
      const res = await refundOrder({ orderId, reason })
      return res
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  /**
   * 提交订单评价
   * @param {Object} payload { orderId, teaId, rating, content, images, isAnonymous }
   */
  async submitOrderReview({ commit }, payload) {
    try {
      commit('SET_LOADING', true)
      const res = await reviewOrder(payload)
      return res
    } catch (error) {
      console.error('提交评价失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // 更新筛选条件
  updateFilters({ commit }, filters) {
    commit('SET_FILTERS', filters)
  },
  
  // === 发货相关 actions ===
  
  /**
   * 单个订单发货
   * @param {Object} payload { id, logisticsCompany, logisticsNumber }
   */
  async shipOrder({ commit }, { id, logisticsCompany, logisticsNumber }) {
    try {
      commit('SET_LOADING', true)
      const res = await shipOrder({ id, logisticsCompany, logisticsNumber })
      // 更新订单状态为待收货
      commit('UPDATE_ORDER_STATUS', { id, status: 2 })
      return res
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  /**
   * 批量发货
   * @param {Object} payload { orderIds, logisticsCompany, logisticsNumber }
   */
  async batchShipOrders({ commit, dispatch }, { orderIds, logisticsCompany, logisticsNumber }) {
    try {
      commit('SET_LOADING', true)
      const res = await batchShipOrders({ orderIds, logisticsCompany, logisticsNumber })
      // 更新所有订单状态为待收货
      orderIds.forEach(id => {
        commit('UPDATE_ORDER_STATUS', { id, status: 2 })
      })
      return res
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  /**
   * 获取订单物流信息
   * @param {string} id 订单ID
   */
  async fetchOrderLogistics({ commit }, id) {
    try {
      const res = await getOrderLogistics(id)
      return res
    } catch (error) {
      console.error('获取物流信息失败:', error)
      throw error
    }
  },
  
  // === 退款相关 actions ===
  
  /**
   * 获取退款详情
   * @param {string} orderId 订单ID
   */
  async fetchRefundDetail({ commit }, orderId) {
    try {
      const res = await getRefundDetail(orderId)
      return res
    } catch (error) {
      console.error('获取退款详情失败:', error)
      throw error
    }
  },
  
  /**
   * 审批退款
   * @param {Object} payload { orderId, approve, reason }
   */
  async processRefund({ commit }, { orderId, approve, reason }) {
    try {
      commit('SET_LOADING', true)
      const res = await processRefund({ orderId, approve, reason })
      // 更新订单状态
      if (approve) {
        commit('UPDATE_ORDER_STATUS', { id: orderId, status: 5 }) // 已退款
      }
      return res
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // === 统计与导出 actions ===
  
  /**
   * 获取订单统计数据
   * @param {Object} params { startDate, endDate, shopId }
   */
  async fetchOrderStatistics({ commit }, params = {}) {
    try {
      const res = await getOrderStatistics(params)
      commit('SET_ORDER_STATISTICS', res)
      return res
    } catch (error) {
      console.error('获取订单统计失败:', error)
      throw error
    }
  },
  
  /**
   * 导出订单数据
   * @param {Object} params { format, startDate, endDate, status, shopId }
   */
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
      
      return true
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
  }
}

export default {
  namespaced: true,
  state,
  getters,
  mutations,
  actions
} 