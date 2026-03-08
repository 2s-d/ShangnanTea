import { defineStore } from 'pinia'
import { ref, computed, reactive } from 'vue'
import { 
  createOrder as createOrderApi, 
  getOrders, 
  getOrderDetail, 
  payOrder as payOrderApi, 
  cancelOrder as cancelOrderApi,
  confirmOrder as confirmOrderApi,
  refundOrder as refundOrderApi,
  reviewOrder as reviewOrderApi,
  uploadReviewImage as uploadReviewImageApi,
  processRefund as processRefundApi,
  getRefundDetail,
  shipOrder as shipOrderApi,
  batchShipOrders as batchShipOrdersApi,
  getOrderLogistics,
  getOrderStatistics,
  exportOrders as exportOrdersApi,
  getCartItems,
  addToCart as addToCartApi,
  updateCartItem as updateCartItemApi,
  removeFromCart as removeFromCartApi,
  clearCart as clearCartApi
} from '@/api/order'

export const useOrderStore = defineStore('order', () => {
  // ========== State ==========
  const orderList = ref([])
  const currentOrder = ref(null)
  const loading = ref(false)
  
  // 分页信息
  const pagination = reactive({
    total: 0,
    currentPage: 1,
    pageSize: 10
  })
  
  // 筛选条件
  const filters = reactive({})
  
  // 订单统计数据
  const orderStatistics = ref(null)
  
// 购物车相关状态
const cartItems = ref([])

// 立即购买临时商品（不走购物车）
const directBuyItem = ref(null)

// 茶叶列表页：是否使用“默认规格”直接加入购物车（会在整个应用内保持，不做持久化）
const useDefaultSpecOnAdd = ref(false)
  
  // ========== Getters ==========
  const orderStatusText = computed(() => (status) => {
    const statusMap = {
      0: '待付款',
      1: '待发货',
      2: '待收货',
      3: '已完成',
      4: '已取消',
      5: '已退款'
    }
    return statusMap[status] || '未知状态'
  })
  
  // 购物车商品种类数（不同商品的种类数，不是总数量）
  const cartItemCount = computed(() => {
    if (!cartItems.value || cartItems.value.length === 0) return 0
    // 计算不同商品的数量（根据teaId和specId区分）
    const uniqueItems = new Set()
    cartItems.value.forEach(item => {
      // 兼容不同的字段命名：teaId/tea_id, specId/spec_id
      const teaId = item.teaId || item.tea_id || ''
      const specId = item.specId || item.spec_id || 'null'
      const key = `${teaId}_${specId}`
      uniqueItems.add(key)
    })
    return uniqueItems.size
  })
  
  const cartTotalPrice = computed(() => 
    cartItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0).toFixed(2)
  )
  
  const hasSelectedItems = computed(() => 
    cartItems.value.some(item => item.selected)
  )
  
  // ========== Actions ==========
  
  // ========== 购物车相关 Actions ==========
  
  // 获取购物车列表
  async function fetchCartItems() {
    try {
      loading.value = true
      const res = await getCartItems()
      const data = res?.data
      // 后端返回格式：{ items: [...] } 或 { list: [...] } 或直接是数组
      cartItems.value = Array.isArray(data) ? data : (data?.items || data?.list || [])
      return res
    } finally {
      loading.value = false
    }
  }

  /**
   * 根据选中的购物车项ID列表，返回对应的购物车商品
   * 主要用于从购物车跳转到结算页时，获取本次要结算的商品列表
   *
   * @param {Array<string|number>} selectedIds 选中的购物车项ID数组
   * @returns {Array<Object>} 对应的购物车商品列表
   */
  function getSelectedCartItems(selectedIds = []) {
    if (!Array.isArray(selectedIds) || selectedIds.length === 0) {
      return []
    }
    const idSet = new Set(selectedIds.map(id => String(id)))
    return cartItems.value.filter(item => idSet.has(String(item.id)))
  }
  
  // 添加商品到购物车
  async function addToCart({ teaId, quantity, specificationId }) {
    try {
      loading.value = true
      const res = await addToCartApi({ teaId, quantity, specificationId })
      await fetchCartItems()
      return res
    } finally {
      loading.value = false
    }
  }
  
  // 更新购物车商品
  async function updateCartItem({ id, quantity, specificationId }) {
    try {
      loading.value = true
      const res = await updateCartItemApi({ id, quantity, specificationId })
      
      if (quantity !== undefined) {
        const item = cartItems.value.find(item => item.id === id)
        if (item) {
          item.quantity = quantity
        }
      }
      
      return res
    } catch (error) {
      await fetchCartItems()
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 移除购物车商品
  async function removeFromCart(id) {
    try {
      loading.value = true
      const res = await removeFromCartApi(id)
      cartItems.value = cartItems.value.filter(item => item.id !== id)
      return res
    } finally {
      loading.value = false
    }
  }
  
  // 清空购物车
  async function clearCart() {
    try {
      loading.value = true
      const res = await clearCartApi()
      cartItems.value = []
      return res
    } finally {
      loading.value = false
    }
  }
  
  // ========== 订单相关 Actions ==========
  
  // 创建订单
  async function createOrder(orderData) {
    try {
      loading.value = true
      const res = await createOrderApi(orderData)
      // 后端在 fromCart=true 时会按 items[*].cartItemId 删除“本次结算”的购物车项
      // 前端不应清空整个购物车，只需要刷新一次即可
      if (orderData?.fromCart) {
        await fetchCartItems()
      }
      
      return res
    } finally {
      loading.value = false
    }
  }

  /**
   * 设置“立即购买”的临时商品信息
   * @param {Object|null} item
   */
  function setDirectBuyItem(item) {
    if (!item) {
      directBuyItem.value = null
      return
    }
    directBuyItem.value = { ...item }
  }
  
  // 获取订单列表
  async function fetchOrders(params = {}) {
    try {
      loading.value = true
      const queryParams = {
        page: pagination.currentPage,
        size: pagination.pageSize,
        ...params
      }
      
      const res = await getOrders(queryParams)
      const data = res.data
      
      orderList.value = data?.list || []
      Object.assign(pagination, {
        total: data?.total || 0,
        currentPage: pagination.currentPage,
        pageSize: pagination.pageSize
      })
      
      return res
    } finally {
      loading.value = false
    }
  }
  
  // 获取订单详情
  async function fetchOrderDetail(id) {
    try {
      loading.value = true
      const res = await getOrderDetail(id)
      currentOrder.value = res.data
      return res
    } finally {
      loading.value = false
    }
  }
  
  // 支付订单（优先使用 paymentId，多订单一次支付）
  async function payOrder({ paymentId, orderId, paymentMethod }) {
    try {
      loading.value = true
      const payload = { paymentMethod }
      // 后端优先按 paymentId 处理，兼容老的仅按 orderId 支付
      if (paymentId) {
        payload.paymentId = paymentId
      } else if (orderId) {
        payload.orderId = orderId
      }

      const res = await payOrderApi(payload)
      
      // 提前将本地状态更新为“待发货”可能与实际结果不一致，这里保持保守策略：
      // 仅在后端明确返回5006（订单已支付）时更新一次，正常支付走轮询页面。
      if (res?.code === 5006 && orderId) {
        const order = orderList.value.find(o => o.id === orderId)
        if (order) {
          order.status = 1
        }
        if (currentOrder.value && currentOrder.value.id === orderId) {
          currentOrder.value.status = 1
        }
      }
      
      return res
    } finally {
      loading.value = false
    }
  }
  
  // 取消订单
  async function cancelOrder(id) {
    try {
      loading.value = true
      const res = await cancelOrderApi(id)
      
      const order = orderList.value.find(o => o.id === id)
      if (order) {
        order.status = 4
      }
      
      if (currentOrder.value && currentOrder.value.id === id) {
        currentOrder.value.status = 4
      }
      
      return res
    } finally {
      loading.value = false
    }
  }
  
  // 确认收货
  async function confirmReceipt(id) {
    try {
      loading.value = true
      const res = await confirmOrderApi(id)
      
      const order = orderList.value.find(o => o.id === id)
      if (order) {
        order.status = 3
      }
      
      if (currentOrder.value && currentOrder.value.id === id) {
        currentOrder.value.status = 3
      }
      
      return res
    } finally {
      loading.value = false
    }
  }
  
  // 评价订单
  async function submitOrderReview(payload) {
    try {
      loading.value = true
      const res = await reviewOrderApi(payload)
      return res
    } catch (error) {
      console.error('提交评价失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 申请退款
  async function applyRefund({ orderId, reason }) {
    try {
      loading.value = true
      const res = await refundOrderApi({ orderId, reason })
      return res
    } finally {
      loading.value = false
    }
  }
  
  // 更新筛选条件
  function updateFilters(newFilters) {
    Object.assign(filters, newFilters)
  }
  
  // ========== 发货相关 Actions ==========
  
  // 单个订单发货
  async function shipOrder({ id, logisticsCompany, logisticsNumber }) {
    try {
      loading.value = true
      const res = await shipOrderApi({ id, logisticsCompany, logisticsNumber })
      
      const order = orderList.value.find(o => o.id === id)
      if (order) {
        order.status = 2
      }
      
      if (currentOrder.value && currentOrder.value.id === id) {
        currentOrder.value.status = 2
      }
      
      return res
    } finally {
      loading.value = false
    }
  }
  
  // 批量发货
  async function batchShipOrders({ orderIds, logisticsCompany, logisticsNumber }) {
    try {
      loading.value = true
      const res = await batchShipOrdersApi({ orderIds, logisticsCompany, logisticsNumber })
      
      orderIds.forEach(id => {
        const order = orderList.value.find(o => o.id === id)
        if (order) {
          order.status = 2
        }
        
        if (currentOrder.value && currentOrder.value.id === id) {
          currentOrder.value.status = 2
        }
      })
      
      return res
    } finally {
      loading.value = false
    }
  }
  
  // 获取订单物流信息
  async function fetchOrderLogistics(id) {
    try {
      const res = await getOrderLogistics(id)
      return res
    } catch (error) {
      console.error('获取物流信息失败:', error)
      throw error
    }
  }
  
  // ========== 退款相关 Actions ==========
  
  // 审批退款
  async function processRefund({ orderId, approve, reason }) {
    try {
      loading.value = true
      const res = await processRefundApi({ orderId, approve, reason })
      
      if (approve) {
        const order = orderList.value.find(o => o.id === orderId)
        if (order) {
          order.status = 5
        }
        
        if (currentOrder.value && currentOrder.value.id === orderId) {
          currentOrder.value.status = 5
        }
      }
      
      return res
    } finally {
      loading.value = false
    }
  }
  
  // 获取退款详情
  async function fetchRefundDetail(orderId) {
    try {
      const res = await getRefundDetail(orderId)
      return res
    } catch (error) {
      console.error('获取退款详情失败:', error)
      throw error
    }
  }
  
  // ========== 统计与导出 Actions ==========
  
  // 获取订单统计数据
  async function fetchOrderStatistics(params = {}) {
    try {
      const res = await getOrderStatistics(params)
      orderStatistics.value = res.data
      return res
    } catch (error) {
      console.error('获取订单统计失败:', error)
      throw error
    }
  }
  
  // 导出订单数据
  async function exportOrders(params = {}) {
    try {
      loading.value = true
      const blob = await exportOrdersApi(params)
      
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      const format = params.format || 'csv'
      link.download = `orders_${new Date().toISOString().slice(0, 10)}.${format}`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
      
      return { code: 200, data: true }
    } catch (error) {
      console.error('导出订单失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 更新分页
  function setPage({ page, extraParams = {} }) {
    pagination.currentPage = page
    return fetchOrders(extraParams)
  }
  
  // 上传订单评价图片
  async function uploadReviewImage(file) {
    try {
      loading.value = true
      const res = await uploadReviewImageApi(file)
      return res
    } catch (error) {
      console.error('上传评价图片失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }
  
  return {
    // State
    orderList,
    currentOrder,
    loading,
    pagination,
    filters,
    orderStatistics,
    cartItems,
    directBuyItem,
    useDefaultSpecOnAdd,
    directBuyItem,
    // Getters
    orderStatusText,
    cartItemCount,
    cartTotalPrice,
    hasSelectedItems,
    // Actions
    fetchCartItems,
    getSelectedCartItems,
    addToCart,
    updateCartItem,
    removeFromCart,
    clearCart,
    createOrder,
    fetchOrders,
    fetchOrderDetail,
    payOrder,
    cancelOrder,
    confirmReceipt,
    submitOrderReview,
    applyRefund,
    updateFilters,
    shipOrder,
    batchShipOrders,
    fetchOrderLogistics,
    processRefund,
    fetchRefundDetail,
    fetchOrderStatistics,
    exportOrders,
    setPage,
    uploadReviewImage,
    setDirectBuyItem
  }
})
