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
  // 接口88-92: 购物车操作
  
  // #88 获取购物车列表 - 成功码200, 失败码4110
  async fetchCartItems({ commit }) {
    try {
      commit('SET_LOADING', true)
      const res = await getCartItems()
      commit('SET_CART_ITEMS', res.data)
      return res // 返回 {code, data}
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // #89 添加商品到购物车 - 成功码4010, 失败码4111/4116/4117/4118
  async addToCart({ commit, dispatch }, { teaId, quantity }) {
    try {
      commit('SET_LOADING', true)
      const res = await addToCart({ teaId, quantity })
      // 刷新购物车列表
      dispatch('fetchCartItems')
      return res // 返回 {code, data}
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // #90 更新购物车商品 - 成功码4011/4012, 失败码4112/4116/4117
  async updateCartItem({ commit, dispatch }, { id, quantity, specificationId }) {
    try {
      commit('SET_LOADING', true)
      const res = await updateCartItem({ id, quantity, specificationId })
      // 乐观更新前端状态
      if (quantity !== undefined) {
        commit('UPDATE_CART_ITEM', { id, quantity })
      }
      return res // 返回 {code, data}
    } catch (error) {
      // 如果失败，重新获取购物车
      dispatch('fetchCartItems')
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // #91 移除购物车商品 - 成功码4013, 失败码4113
  async removeFromCart({ commit }, id) {
    try {
      commit('SET_LOADING', true)
      const res = await removeFromCart(id)
      // 乐观更新前端状态
      commit('REMOVE_CART_ITEM', id)
      return res // 返回 {code, data}
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // #92 清空购物车 - 成功码4015, 失败码4115
  async clearCart({ commit }) {
    try {
      commit('SET_LOADING', true)
      const res = await clearCart()
      commit('CLEAR_CART')
      return res // 返回 {code, data}
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // === 订单相关actions ===
  // 接口93-99: 订单基础操作
  
  // #93 创建订单 - 成功码4000, 失败码4100/4116/4118
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
  
  // #94 获取订单列表 - 成功码200, 失败码1102
  async fetchOrders({ commit, state }, params = {}) {
    try {
      commit('SET_LOADING', true)
      const queryParams = {
        page: state.pagination.currentPage,
        size: state.pagination.pageSize,
        ...params
      }
      const res = await getOrders(queryParams)
      const data = res.data || res
      commit('SET_ORDER_LIST', data.list || [])
      commit('SET_PAGINATION', {
        total: data.total || 0,
        currentPage: state.pagination.currentPage,
        pageSize: state.pagination.pageSize
      })
      return res // 返回 {code, data}
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // #95 获取订单详情 - 成功码200, 失败码4105/4106/4107
  async fetchOrderDetail({ commit }, id) {
    try {
      commit('SET_LOADING', true)
      const res = await getOrderDetail(id)
      commit('SET_CURRENT_ORDER', res.data || res)
      return res // 返回 {code, data}
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // #96 支付订单 - 成功码4005/4020, 失败码4120/4121/4122/4124
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
  
  // #97 取消订单 - 成功码4002, 失败码4102/4105/4106
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

  // #98 确认收货 - 成功码4003, 失败码4103/4105/4106
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

  // #99 评价订单 - 成功码4050, 失败码4150
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

  // #100 申请退款 - 成功码4030, 失败码4130/4105/4106
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
  // 接口103-105: 发货与物流
  
  // #103 单个订单发货 - 成功码4004, 失败码4104/4105/4106
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
  
  // #104 批量发货 - 成功码4006, 失败码4108/4106
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
  
  // #105 获取订单物流信息 - 成功码200, 失败码4140/4105
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
  // 接口101-102: 退款处理
  
  // #101 审批退款 - 成功码4031/4032, 失败码4131/4106
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
  
  // #102 获取退款详情 - 成功码200, 失败码4132/4105
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
  // 接口106-107: 统计与导出
  
  // #106 获取订单统计数据 - 成功码200, 失败码1102
  async fetchOrderStatistics({ commit }, params = {}) {
    try {
      const res = await getOrderStatistics(params)
      commit('SET_ORDER_STATISTICS', res.data || res)
      return res // 返回 {code, data}
    } catch (error) {
      console.error('获取订单统计失败:', error)
      throw error
    }
  },
  
  // #107 导出订单数据 - 成功码200, 失败码1100
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
  }
}

export default {
  namespaced: true,
  state,
  getters,
  mutations,
  actions
} 