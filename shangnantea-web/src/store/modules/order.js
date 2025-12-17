/* eslint-disable no-useless-catch */
import { 
  createOrder, 
  getOrders, 
  getOrderDetail, 
  payOrder, 
  cancelOrder,
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
  }
}

const actions = {
  // === 购物车相关actions ===
  
  // 获取购物车列表
  async fetchCartItems({ commit }) {
    try {
      commit('SET_LOADING', true)
      
      const res = await getCartItems()
      commit('SET_CART_ITEMS', res.data)
      
      return res.data
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
      
      return res.data
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
      
      commit('SET_ORDER_LIST', res.data.list)
      commit('SET_PAGINATION', {
        total: res.data.total,
        currentPage: state.pagination.currentPage,
        pageSize: state.pagination.pageSize
      })
      
      return res.data
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // 获取订单详情
  async fetchOrderDetail({ commit }, id) {
    try {
      commit('SET_LOADING', true)
      
      const res = await getOrderDetail(id)
      commit('SET_CURRENT_ORDER', res.data)
      
      return res.data
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
      
      return res.data
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
  
  // 更新分页
  setPage({ commit, dispatch }, page) {
    commit('SET_PAGINATION', {
      ...state.pagination,
      currentPage: page
    })
    return dispatch('fetchOrders')
  }
}

export default {
  namespaced: true,
  state,
  getters,
  mutations,
  actions
} 