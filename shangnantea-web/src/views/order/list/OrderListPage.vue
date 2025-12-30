<template>
  <div class="order-list-page">
    <el-card class="list-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span class="title">我的订单</span>
          <el-input
            v-model="searchText"
            placeholder="搜索订单号或商品名称"
            class="search-input"
            clearable
            prefix-icon="el-icon-search"
            @input="handleSearch"
          />
        </div>
      </template>

      <div class="order-filter">
        <el-tabs v-model="activeTab" @tab-click="handleTabChange">
          <el-tab-pane label="全部" name="all"></el-tab-pane>
          <el-tab-pane label="待付款" name="unpaid"></el-tab-pane>
          <el-tab-pane label="待发货" name="unshipped"></el-tab-pane>
          <el-tab-pane label="待收货" name="shipped"></el-tab-pane>
          <el-tab-pane label="待评价" name="to-review"></el-tab-pane>
        </el-tabs>
      </div>

      <div v-loading="loading" class="order-list-content">
        <div v-if="filteredOrders.length === 0" class="empty-list">
          <el-empty description="暂无订单数据" />
        </div>

        <div v-else class="order-list">
          <div
            v-for="order in filteredOrders"
            :key="order.order_id"
            class="order-item"
          >
            <div class="order-header">
              <div class="order-info">
                <span class="order-time">{{ formatTime(order.create_time) }}</span>
                <span class="order-id">订单号：{{ order.order_id }}</span>
              </div>
              <div class="order-status">
                <span :class="['status-tag', getStatusClass(order.order_status)]">
                  {{ getStatusText(order.order_status) }}
                </span>
              </div>
            </div>

            <div class="order-content">
              <div
                class="order-product"
                @click="viewOrderDetail(order.order_id)"
              >
                <div class="product-image">
                  <SafeImage :src="order.tea_image" type="tea" :alt="order.tea_name" style="width:80px;height:80px;object-fit:cover;" />
                </div>
                <div class="product-info">
                  <div class="product-name">{{ order.tea_name }}</div>
                  <div class="product-spec">规格：{{ order.spec_name }}</div>
                  <div class="product-price-qty">
                    <span class="product-price">¥{{ order.price }}</span>
                    <span class="product-qty">x{{ order.quantity }}</span>
                  </div>
                </div>
              </div>
            </div>

            <div class="order-footer">
              <div class="order-total">
                <span class="total-label">实付金额：</span>
                <span class="total-price">¥{{ order.total_amount }}</span>
                <span class="item-count">(数量：{{ order.quantity }})</span>
              </div>
              <div class="order-actions">
                <template v-if="order.order_status === 0">
                  <!-- 待付款 -->
                  <el-button type="primary" size="small" @click="continuePay(order.order_id)">
                    去支付
                  </el-button>
                  <el-button size="small" @click="cancelOrder(order.order_id)">
                    取消订单
                  </el-button>
                  <el-button size="small" @click="contactShop(order.shop_id)">
                    联系商家
                  </el-button>
                </template>
                <template v-else-if="order.order_status === 1">
                  <!-- 待发货 -->
                  <el-button type="primary" size="small" @click="openRefundDialog(order.order_id)">
                    申请退款
                  </el-button>
                  <el-button size="small" @click="modifyAddress(order.order_id)">
                    修改地址
                  </el-button>
                  <el-button size="small" @click="contactShop(order.shop_id)">
                    联系商家
                  </el-button>
                </template>
                <template v-else-if="order.order_status === 2">
                  <!-- 待收货 -->
                  <el-button type="primary" size="small" @click="confirmReceipt(order.order_id)">
                    确认收货
                  </el-button>
                  <el-button size="small" @click="viewLogistics(order.order_id)">
                    查看物流
                  </el-button>
                  <el-button size="small" @click="contactShop(order.shop_id)">
                    联系商家
                  </el-button>
                </template>
                <template v-else-if="order.order_status === 3">
                  <!-- 已完成 -->
                  <el-button v-if="!order.is_reviewed" type="primary" size="small" @click="writeReview(order.order_id)">
                    评价
                  </el-button>
                  <el-button v-else type="info" size="small" plain disabled>
                    已评价
                  </el-button>
                  <el-button size="small" @click="deleteOrder(order.order_id)">
                    删除订单
                  </el-button>
                  <el-button size="small" @click="contactShop(order.shop_id)">
                    联系商家
                  </el-button>
                </template>
                <template v-else>
                  <!-- 已取消/已退款等状态 -->
                  <el-button size="small" @click="viewOrderDetail(order.order_id)">
                    查看详情
                  </el-button>
                  <el-button size="small" @click="contactShop(order.shop_id)">
                    联系商家
                  </el-button>
                </template>
              </div>
            </div>
          </div>
        </div>

        <div class="pagination-container">
          <el-pagination
            v-if="filteredOrders.length > 0"
            background
            layout="prev, pager, next"
            :total="total"
            :page-size="pageSize"
            :current-page="currentPage"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </el-card>

    <!-- 申请退款对话框（生产版：提交到后端，不在前端模拟状态） -->
    <el-dialog
      v-model="refundDialogVisible"
      title="申请退款"
      width="520px"
      :close-on-click-modal="false"
    >
      <el-form label-width="80px">
        <el-form-item label="退款原因" required>
          <el-input
            v-model="refundReason"
            type="textarea"
            :rows="4"
            placeholder="请填写退款原因（不少于5个字）"
            maxlength="200"
            show-word-limit
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="refundDialogVisible = false" :disabled="refundSubmitting">取消</el-button>
          <el-button type="primary" :loading="refundSubmitting" @click="submitRefund">
            提交申请
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessageBox } from 'element-plus'
import SafeImage from '@/components/common/form/SafeImage.vue'
import orderMessages from '@/utils/orderMessages'
import { orderPromptMessages } from '@/utils/promptMessages'

// TODO: 退款申请功能需后端提供接口与数据结构支持（前端不在UI层模拟退款流程）

export default {
  name: 'OrderListPage',
  components: {
    SafeImage
  },
  setup() {
    /**
     * 订单列表页面权限控制说明（P2-6：订单权限控制的完整性）
     * 
     * 权限规则：
     * - 普通用户(role=2)：只能查看自己的订单（后端通过userId过滤）
     * - 商家(role=3)：可以查看自己店铺的订单（后端通过shopId过滤）
     * - 管理员(role=1)：应访问 /order/manage 查看所有订单
     * 
     * 路由权限：meta.roles: [ROLES.USER, ROLES.SHOP]
     * 后端验证：API根据token中的userId/shopId自动过滤数据
     */
    const store = useStore()
    const router = useRouter()
    const searchText = ref('')
    const activeTab = ref('all')
    const currentPage = ref(1)

    // 退款相关（生产版：只负责收集输入并请求后端）
    const refundDialogVisible = ref(false)
    const refundSubmitting = ref(false)
    const refundReason = ref('')
    const refundOrderId = ref('')

    const loading = computed(() => store.state.order.loading)
    const pageSize = computed(() => store.state.order.pagination.pageSize)
    const total = computed(() => store.state.order.pagination.total)
    const orders = computed(() => store.state.order.orderList || [])


    
    // 列表直接来自 Vuex，筛选交给后端
    const filteredOrders = computed(() => orders.value)

    const openRefundDialog = (orderId) => {
      refundOrderId.value = orderId
      refundReason.value = ''
      refundDialogVisible.value = true
    }

    const submitRefund = async () => {
      if (!refundOrderId.value) {
        orderPromptMessages.showOrderInfoInvalid()
        return
      }

      if (!refundReason.value.trim() || refundReason.value.trim().length < 5) {
        orderPromptMessages.showRefundReasonTooShort()
        return
      }

      try {
        refundSubmitting.value = true
        await store.dispatch('order/applyRefund', {
          orderId: refundOrderId.value,
          reason: refundReason.value.trim()
        })
        orderSuccessMessages.showRefundSubmitted()
        refundDialogVisible.value = false
      } catch (e) {
        orderErrorMessages.showRefundSubmitFailed(e?.message)
      } finally {
        refundSubmitting.value = false
      }
    }
    
    // 获取订单状态文本
    const getStatusText = (status) => {
      const statusMap = {
        0: '待付款',
        1: '待发货',
        2: '待收货',
        3: '已完成',
        4: '已取消',
        5: '已退款',
        6: '已退款'
      }
      return statusMap[status] || '未知状态'
    }
    
    // 获取状态对应的类名
    const getStatusClass = (status) => {
      const classMap = {
        0: 'status-unpaid',
        1: 'status-unshipped',
        2: 'status-shipped',
        3: 'status-completed',
        4: 'status-cancelled',
        5: 'status-refunded',
        6: 'status-refunded'
      }
      return classMap[status] || ''
    }
    
    // 格式化时间
    const formatTime = (timeStr) => {
      const date = new Date(timeStr)
      return `${date.getFullYear()}-${padZero(date.getMonth() + 1)}-${padZero(date.getDate())} ${padZero(date.getHours())}:${padZero(date.getMinutes())}`
    }
    
    // 数字补零
    const padZero = (num) => {
      return num < 10 ? `0${num}` : num
    }
    
    // 处理搜索
    const handleSearch = () => {
      currentPage.value = 1 // 重置到第一页
      store.dispatch('order/updateFilters', {
        keyword: searchText.value
      })
      store.dispatch('order/fetchOrders', {
        page: currentPage.value,
        size: pageSize.value
      })
    }
    
    // 处理分页变化
    const handleCurrentChange = (page) => {
      currentPage.value = page
      store.dispatch('order/setPage', { page })
    }
    
    // 处理标签页切换
    const handleTabChange = () => {
      currentPage.value = 1 // 重置到第一页
      // 映射标签到后端状态码
      let status = ''
      if (activeTab.value === 'unpaid') status = 0
      else if (activeTab.value === 'unshipped') status = 1
      else if (activeTab.value === 'shipped') status = 2
      else if (activeTab.value === 'to-review') status = 3
      store.dispatch('order/updateFilters', {
        status
      })
      store.dispatch('order/fetchOrders', {
        page: currentPage.value,
        size: pageSize.value
      })
    }
    
    // 查看订单详情
    const viewOrderDetail = (orderId) => {
      router.push(`/order/detail/${orderId}`)
    }
    
    // 继续支付
    const continuePay = (orderId) => {
      router.push(`/order/payment?orderIds=${orderId}`)
    }
    
    // 取消订单
    const cancelOrder = async (orderId) => {
      try {
      ElMessageBox.confirm('确定要取消该订单吗？取消后无法恢复', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      
      await store.dispatch('order/cancelOrder', orderId)
      orderSuccessMessages.showOrderCanceled()
      store.dispatch('order/fetchOrders', { page: currentPage.value, size: pageSize.value, keyword: searchText.value })
      } catch (error) {
        if (error !== 'cancel') {
          orderErrorMessages.showOrderCancelFailed()
        }
      }
    }
    
    // 确认收货
    const confirmReceipt = async (orderId) => {
      try {
      ElMessageBox.confirm('确认已收到商品吗？', '提示', {
        confirmButtonText: '确认收货',
        cancelButtonText: '取消',
        type: 'info'
      })
      
      await store.dispatch('order/confirmReceipt', orderId)
      orderSuccessMessages.showOrderConfirmed()
      store.dispatch('order/fetchOrders', { page: currentPage.value, size: pageSize.value, keyword: searchText.value })
      } catch (error) {
        if (error !== 'cancel') {
          orderErrorMessages.showOrderConfirmFailed()
        }
      }
    }
    
    // 查看物流
    const viewLogistics = (orderId) => {
      orderPromptMessages.showViewLogisticsDev()
      // router.push(`/order/logistics/${orderId}`)
    }

    // 写评价：生产结构下跳转到评价页面（后续由订单评价页接入真实提交流程）
    const writeReview = (orderId) => {
      router.push(`/order/review/${orderId}`)
    }
    
    // 删除订单
    const deleteOrder = (orderId) => {
      orderPromptMessages.showDeleteOrderDev()
    }
    
    // 添加修改地址功能
    const modifyAddress = (orderId) => {
      orderPromptMessages.showModifyAddressDev()
    }
    
    // 添加联系商家功能
    const contactShop = (shopId) => {
      orderPromptMessages.showContactShopDev()
    }
    
    // 初始化
    onMounted(() => {
      store.dispatch('order/updateFilters', {
        keyword: '',
        status: ''
      })
      store.dispatch('order/fetchOrders', { page: currentPage.value, size: pageSize.value })
    })
    

    
    return {
      loading,
      searchText,
      activeTab,
      currentPage,
      pageSize,
      total,
      filteredOrders,
      getStatusText,
      getStatusClass,
      formatTime,
      handleSearch,
      handleCurrentChange,
      handleTabChange,
      viewOrderDetail,
      continuePay,
      cancelOrder,
      confirmReceipt,
      viewLogistics,
      writeReview,
      deleteOrder,
      modifyAddress,
      contactShop,

      // 退款
      refundDialogVisible,
      refundSubmitting,
      refundReason,
      openRefundDialog,
      submitRefund
    }
  }
}
</script>

<style lang="scss" scoped>
.order-list-page {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.list-card {
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  
  .title {
    font-size: 18px;
    font-weight: bold;
  }
  
  .search-input {
    width: 300px;
    
    @media (max-width: 768px) {
      width: 100%;
      margin-top: 10px;
    }
  }
}

.order-filter {
  margin-bottom: 15px;
}

.order-list-content {
  min-height: 300px;
}

.empty-list {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}

.order-list {
  .order-item {
    margin-bottom: 20px;
    border: 1px solid #ebeef5;
    border-radius: 4px;
    overflow: hidden;
    background-color: #fff;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
    
    &:last-child {
      margin-bottom: 0;
    }
  }
  
  .order-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 15px;
    background-color: #f8f9fa;
    border-bottom: 1px solid #ebeef5;
    
    .order-info {
      display: flex;
      flex-wrap: wrap;
      gap: 15px;
      
      .order-time {
        color: #606266;
      }
      
      .order-id {
        color: #606266;
      }
    }
    
    .order-status {
      .status-tag {
        padding: 2px 8px;
        border-radius: 4px;
        font-size: 12px;
        font-weight: bold;
      }
      
      .status-unpaid {
        color: #e6a23c;
        background-color: rgba(230, 162, 60, 0.1);
      }
      
      .status-unshipped {
        color: #409eff;
        background-color: rgba(64, 158, 255, 0.1);
      }
      
      .status-shipped {
        color: #67c23a;
        background-color: rgba(103, 194, 58, 0.1);
      }
      
      .status-completed {
        color: #909399;
        background-color: rgba(144, 147, 153, 0.1);
      }
      
      .status-cancelled {
        color: #f56c6c;
        background-color: rgba(245, 108, 108, 0.1);
      }
      
      .status-refunded {
        color: #909399;
        background-color: rgba(144, 147, 153, 0.1);
      }
      
      .status-refund-applied {
        color: #ff6b00;
        background-color: rgba(255, 107, 0, 0.1);
      }
    }
  }
  
  .order-content {
    padding: 15px;
    display: flex;
    flex-direction: column;
    gap: 15px;
    
    .order-product {
      display: flex;
      cursor: pointer;
      
      &:hover {
        background-color: #f8f9fa;
      }
      
      .product-image {
        width: 80px;
        height: 80px;
        margin-right: 15px;
        
        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
          border-radius: 4px;
        }
      }
      
      .product-info {
        flex: 1;
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        
        .product-name {
          font-weight: 500;
          color: #303133;
          margin-bottom: 5px;
        }
        
        .product-spec {
          color: #909399;
          font-size: 12px;
          margin-bottom: 10px;
        }
        
        .product-price-qty {
          display: flex;
          align-items: center;
          gap: 10px;
          
          .product-price {
            color: #f56c6c;
            font-weight: 500;
          }
          
          .product-qty {
            color: #909399;
          }
        }
      }
    }
  }
  
  .order-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 15px;
    border-top: 1px solid #ebeef5;
    background-color: #f8f9fa;
    
    @media (max-width: 768px) {
      flex-direction: column;
      align-items: flex-start;
      gap: 15px;
    }
    
    .order-total {
      display: flex;
      align-items: center;
      gap: 5px;
      
      .total-label {
        color: #606266;
      }
      
      .total-price {
        color: #f56c6c;
        font-weight: bold;
        font-size: 16px;
      }
      
      .item-count {
        color: #909399;
        font-size: 12px;
      }
    }
    
    .order-actions {
      display: flex;
      gap: 10px;
    }
  }
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.order-status {
  .status-tag {
    padding: 2px 8px;
    border-radius: 4px;
    font-size: 12px;
    font-weight: bold;
    margin-left: 5px;
  }
  
  .status-unpaid {
    color: #e6a23c;
    background-color: rgba(230, 162, 60, 0.1);
  }
  
  .status-unshipped {
    color: #409eff;
    background-color: rgba(64, 158, 255, 0.1);
  }
  
  .status-shipped {
    color: #67c23a;
    background-color: rgba(103, 194, 58, 0.1);
  }
  
  .status-completed {
    color: #909399;
    background-color: rgba(144, 147, 153, 0.1);
  }
  
  .status-cancelled {
    color: #f56c6c;
    background-color: rgba(245, 108, 108, 0.1);
  }
  
  .status-refunded {
    color: #909399;
    background-color: rgba(144, 147, 153, 0.1);
  }
  
  .status-refund-applied {
    color: #ff6b00;
    background-color: rgba(255, 107, 0, 0.1);
  }
}

// 添加评价对话框样式
.review-product-info {
  display: flex;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
  
  .product-image {
    width: 80px;
    height: 80px;
    margin-right: 15px;
    
    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      border-radius: 4px;
    }
  }
  
  .product-details {
    flex: 1;
    
    .product-name {
      font-weight: bold;
      margin-bottom: 5px;
    }
    
    .product-spec {
      color: #909399;
      font-size: 14px;
    }
  }
}

.review-form {
  margin-top: 20px;
  
  .el-rate {
    margin-top: 8px;
  }
  
  .el-upload__tip {
    color: #909399;
    font-size: 12px;
    margin-top: 5px;
  }
}
</style> 