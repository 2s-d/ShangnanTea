<template>
  <div class="order-detail-page">
    <el-card class="detail-card" shadow="hover" v-loading="loading">
      <template #header>
        <div class="card-header">
          <div class="left">
            <el-button icon="ArrowLeft" @click="goBack" text>返回订单列表</el-button>
            <span class="title">订单详情</span>
          </div>
          <div class="status">
            <span :class="['status-tag', getStatusClass(orderDetail.order_status)]">
              {{ getStatusText(orderDetail.order_status) }}
            </span>
          </div>
        </div>
      </template>

      <div v-if="orderDetail" class="order-detail-content">
        <!-- 订单基本信息 -->
        <div class="detail-section">
          <div class="section-title">订单信息</div>
          <div class="info-item">
            <span class="label">订单号：</span>
            <span class="value">{{ orderDetail.order_id }}</span>
          </div>
          <div class="info-item">
            <span class="label">下单时间：</span>
            <span class="value">{{ formatTime(orderDetail.create_time) }}</span>
          </div>
          <div class="info-item">
            <span class="label">支付方式：</span>
            <span class="value">{{ getPaymentMethodText(orderDetail.payment_method) }}</span>
          </div>
          <div v-if="orderDetail.order_status >= 2" class="info-item">
            <span class="label">付款时间：</span>
            <span class="value">{{ formatTime(orderDetail.pay_time || orderDetail.update_time) }}</span>
          </div>
        </div>

        <!-- 收货信息 -->
        <div class="detail-section">
          <div class="section-title">收货信息</div>
          <div class="info-item">
            <span class="label">收货人：</span>
            <span class="value">{{ address.name }}</span>
          </div>
          <div class="info-item">
            <span class="label">联系电话：</span>
            <span class="value">{{ address.phone }}</span>
          </div>
          <div class="info-item">
            <span class="label">收货地址：</span>
            <span class="value">{{ address.province }} {{ address.city }} {{ address.district }} {{ address.detail }}</span>
          </div>
        </div>

        <!-- 商品信息 -->
        <div class="detail-section">
          <div class="section-title">商品信息</div>
          <div class="products-list">
            <!-- 注意：系统使用单品订单模式，每个订单只对应一个茶叶商品 -->
            <div
              class="product-item"
              @click="viewTeaDetail(orderDetail.tea_id)"
            >
              <div class="product-image">
                <SafeImage :src="orderDetail.tea_image" type="tea" :alt="orderDetail.tea_name" style="width:80px;height:80px;object-fit:cover;" />
              </div>
              <div class="product-info">
                <div class="product-name">{{ orderDetail.tea_name }}</div>
                <div class="product-spec">规格：{{ orderDetail.spec_name }}</div>
              </div>
              <div class="product-price">¥{{ orderDetail.price }}</div>
              <div class="product-quantity">x{{ orderDetail.quantity }}</div>
              <div class="product-subtotal">¥{{ (orderDetail.price * orderDetail.quantity).toFixed(2) }}</div>
            </div>
          </div>
        </div>

        <!-- 金额信息 -->
        <div class="detail-section amount-info">
          <div class="amount-item">
            <span class="label">商品金额：</span>
            <span class="value">¥{{ getProductsAmount().toFixed(2) }}</span>
          </div>
          <div class="amount-item">
            <span class="label">运费：</span>
            <span class="value">¥{{ orderDetail.shipping_fee.toFixed(2) }}</span>
          </div>
          <div class="amount-item total">
            <span class="label">实付金额：</span>
            <span class="value">¥{{ orderDetail.total_amount.toFixed(2) }}</span>
          </div>
        </div>

        <!-- 物流信息 (仅在待收货和已完成状态显示) -->
        <div v-if="orderDetail.order_status === 2 || orderDetail.order_status === 3" class="detail-section">
          <div class="section-title">物流信息</div>
          <div class="logistics-info">
            <div class="info-item">
              <span class="label">物流公司：</span>
              <span class="value">{{ logistics.company }}</span>
            </div>
            <div class="info-item">
              <span class="label">物流单号：</span>
              <span class="value">{{ logistics.tracking_number }}</span>
            </div>
            <div class="info-item">
              <span class="label">发货时间：</span>
              <span class="value">{{ formatTime(logistics.ship_time) }}</span>
            </div>
          </div>

          <div class="logistics-timeline">
            <div class="timeline-title">物流轨迹</div>
            <el-timeline>
              <el-timeline-item
                v-for="(activity, index) in logistics.traces"
                :key="index"
                :timestamp="activity.time"
                :type="index === 0 ? 'primary' : ''"
              >
                {{ activity.content }}
              </el-timeline-item>
            </el-timeline>
          </div>
        </div>

        <!-- 添加"查看详细物流"操作按钮，仅当订单是待收货状态时显示 -->
        <div v-if="orderDetail.order_status === 2" class="detail-section action-section">
          <el-button type="primary" @click="viewLogistics">查看详细物流</el-button>
        </div>
      </div>

      <div v-else-if="!loading" class="empty-detail">
        <el-empty description="订单不存在或已被删除" />
        <el-button @click="goBack">返回订单列表</el-button>
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import SafeImage from '@/components/common/form/SafeImage.vue'

/*
// 真实代码（开发UI时注释）
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
*/

export default {
  name: 'OrderDetailPage',
  components: {
    SafeImage
    
    /*
    // 真实代码（开发UI时注释）
    ArrowLeft
    */
  },
  setup() {
    const router = useRouter()
    const route = useRoute()
    const store = useStore()
    const loading = computed(() => store.state.order.loading)
    
    // 从路由参数获取订单ID
    const orderId = route.params.id
    
    /**
     * 生产形态：不在 UI 层造假数据；通过 Vuex -> API -> 后端返回数据
     * TODO-SCRIPT: 订单详情页需对接 order/fetchOrderDetail，并补齐地址/物流等接口
     */
    const orderDetail = ref(null)
    
    const address = ref({
      name: '',
      phone: '',
      province: '',
      city: '',
      district: '',
      detail: ''
    })
    
    const logistics = ref({
      company: '',
      tracking_number: '',
      ship_time: '',
      traces: []
    })
    
    // 获取订单状态文本
    const getStatusText = (status) => {
      const statusMap = {
        0: '待付款',
        1: '待发货',
        2: '待收货',
        3: '已完成',
        4: '已取消',
        5: '已退款'
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
        5: 'status-refunded'
      }
      return classMap[status] || ''
    }
    
    // 获取支付方式文本
    const getPaymentMethodText = (method) => {
      const methodMap = {
        'alipay': '支付宝',
        'wechat': '微信支付',
        'unionpay': '银联支付'
      }
      return methodMap[method] || '未知方式'
    }
    
    // 计算商品总金额
    const getProductsAmount = () => {
      if (!orderDetail.value) return 0
      // 单品订单模式下，直接计算单个商品的总价
      return orderDetail.value.price * orderDetail.value.quantity
    }
    
    // 格式化时间
    const formatTime = (timeStr) => {
      if (!timeStr) return '--'
      
      const date = new Date(timeStr)
      return `${date.getFullYear()}-${padZero(date.getMonth() + 1)}-${padZero(date.getDate())} ${padZero(date.getHours())}:${padZero(date.getMinutes())}`
    }
    
    // 数字补零
    const padZero = (num) => {
      return num < 10 ? `0${num}` : num
    }
    
    // 返回订单列表
    const goBack = () => {
      router.push('/order/list')
    }
    
    // 查看茶叶详情
    const viewTeaDetail = (teaId) => {
      router.push(`/tea/${teaId}`)
    }
    
    // 继续支付
    const continuePay = () => {
      router.push(`/order/payment?orderIds=${orderDetail.value.order_id}`)
    }
    
    // 取消订单
    const cancelOrder = () => {
      ElMessageBox.confirm('确定要取消该订单吗？取消后无法恢复', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 生产形态：不在 UI 层 setTimeout 伪成功，不本地修改订单状态
        store.dispatch('order/cancelOrder', orderId)
          .then(() => {
            ElMessage.success('订单已取消')
            loadOrderDetail()
          })
          .catch((error) => {
            ElMessage.error(`取消订单失败: ${error.message || '请稍后再试'}`)
          })
      }).catch(() => {
        // 用户取消操作，不做任何处理
      })
    }
    
    // 确认收货
    const confirmReceipt = () => {
      ElMessageBox.confirm('确认已收到商品吗？', '提示', {
        confirmButtonText: '确认收货',
        cancelButtonText: '取消',
        type: 'info'
      }).then(() => {
        // 生产形态：不在 UI 层 setTimeout 伪成功，不本地修改订单状态
        store.dispatch('order/confirmReceipt', orderId)
          .then(() => {
            ElMessage.success('确认收货成功')
            loadOrderDetail()
          })
          .catch((error) => {
            ElMessage.error(`确认收货失败: ${error.message || '请稍后再试'}`)
          })
      }).catch(() => {
        // 用户取消操作，不做任何处理
      })
    }
    
    // 查看物流
    const viewLogistics = () => {
      ElMessage.info('查看完整物流信息功能开发中')
    }
    
    // 联系卖家
    const contactSeller = () => {
      ElMessage.info('联系卖家功能开发中')
    }
    
    // 评价商品
    const writeReview = () => {
      router.push(`/order/review/${orderDetail.value.order_id}`)
    }
    
    // 再次购买
    const buyAgain = () => {
      ElMessage.info('再次购买功能开发中')
      // 实际场景中，可能需要将所有商品添加到购物车，然后跳转到购物车页面
    }
    
    // 删除订单
    const deleteOrder = () => {
      ElMessageBox.confirm('确定要删除该订单吗？删除后无法恢复', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // TODO-SCRIPT: 删除订单需要后端接口支持（生产形态：不在 UI 层伪删除/伪成功）
        ElMessage.info('删除订单功能待后端接口接入')
      }).catch(() => {
        // 用户取消操作，不做任何处理
      })
    }
    
    // 加载订单详情
    const loadOrderDetail = () => {
      store.dispatch('order/fetchOrderDetail', orderId)
        .then((data) => {
          orderDetail.value = data
        })
        .catch((error) => {
          ElMessage.error(`获取订单详情失败: ${error.message || '请稍后再试'}`)
          orderDetail.value = null
        })
    }
    
    // 默认图片（生产形态：不使用 mock-images）
    const defaultTeaImage = ''
    
    // 初始化
    onMounted(() => {
      if (!orderId) {
        ElMessage.error('订单ID不能为空')
        goBack()
        return
      }
      
      loadOrderDetail()
    })
    
    
    /*
    // 真实代码（开发UI时注释）
    const router = useRouter()
    const route = useRoute()
    const store = useStore()
    const loading = ref(false)
    
    // 从路由参数获取订单ID
    const orderId = route.params.id
    
    // 订单详情数据
    const orderDetail = ref(null)
    
    // 收货地址
    const address = ref({})
    
    // 物流信息
    const logistics = ref({
      company: '',
      tracking_number: '',
      ship_time: '',
      traces: []
    })
    
    // 获取订单状态文本
    const getStatusText = (status) => {
      const statusMap = {
        1: '待付款',
        2: '待发货',
        3: '待收货',
        4: '已完成',
        5: '已取消',
        6: '已退款'
      }
      return statusMap[status] || '未知状态'
    }
    
    // 获取状态对应的类名
    const getStatusClass = (status) => {
      const classMap = {
        1: 'status-unpaid',
        2: 'status-unshipped',
        3: 'status-shipped',
        4: 'status-completed',
        5: 'status-cancelled',
        6: 'status-refunded'
      }
      return classMap[status] || ''
    }
    
    // 获取支付方式文本
    const getPaymentMethodText = (method) => {
      const methodMap = {
        'alipay': '支付宝',
        'wechat': '微信支付',
        'unionpay': '银联支付'
      }
      return methodMap[method] || '未知方式'
    }
    
    // 计算商品总金额
    const getProductsAmount = () => {
      if (!orderDetail.value || !orderDetail.value.items) return 0
      
      return orderDetail.value.items.reduce((sum, item) => {
        return sum + (item.price * item.quantity)
      }, 0)
    }
    
    // 格式化时间
    const formatTime = (timeStr) => {
      if (!timeStr) return '--'
      
      const date = new Date(timeStr)
      return `${date.getFullYear()}-${padZero(date.getMonth() + 1)}-${padZero(date.getDate())} ${padZero(date.getHours())}:${padZero(date.getMinutes())}`
    }
    
    // 数字补零
    const padZero = (num) => {
      return num < 10 ? `0${num}` : num
    }
    
    // 返回订单列表
    const goBack = () => {
      router.push('/order/list')
    }
    
    // 查看茶叶详情
    const viewTeaDetail = (teaId) => {
      router.push(`/tea/${teaId}`)
    }
    
    // 继续支付
    const continuePay = () => {
      router.push(`/order/payment?orderId=${orderDetail.value.order_id}`)
    }
    
    // 取消订单
    const cancelOrder = async () => {
      try {
        await ElMessageBox.confirm('确定要取消该订单吗？取消后无法恢复', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await store.dispatch('order/cancelOrder', orderId)
        ElMessage.success('订单已取消')
        loadOrderDetail() // 重新加载订单详情
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('取消订单失败')
        }
      }
    }
    
    // 确认收货
    const confirmReceipt = async () => {
      try {
        await ElMessageBox.confirm('确认已收到商品吗？', '提示', {
          confirmButtonText: '确认收货',
          cancelButtonText: '取消',
          type: 'info'
        })
        
        await store.dispatch('order/confirmReceipt', orderId)
        ElMessage.success('确认收货成功')
        loadOrderDetail() // 重新加载订单详情
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('确认收货失败')
        }
      }
    }
    
    // 查看物流
    const viewLogistics = () => {
      router.push(`/order/logistics/${orderId}`)
    }
    
    // 联系卖家
    const contactSeller = () => {
      // 这里可以实现跳转到消息页面或者打开联系窗口等功能
      router.push(`/message/chat?order=${orderId}`)
    }
    
    // 评价商品
    const writeReview = () => {
      router.push(`/order/review/${orderId}`)
    }
    
    // 再次购买
    const buyAgain = async () => {
      try {
        // 将订单中的商品添加到购物车
        await store.dispatch('order/buyAgain', orderId)
        ElMessage.success('已将商品添加到购物车')
        router.push('/order/cart')
      } catch (error) {
        ElMessage.error('操作失败')
      }
    }
    
    // 删除订单
    const deleteOrder = async () => {
      try {
        await ElMessageBox.confirm('确定要删除该订单吗？删除后无法恢复', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await store.dispatch('order/deleteOrder', orderId)
        ElMessage.success('订单已删除')
        goBack() // 删除后返回订单列表
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('删除订单失败')
        }
      }
    }
    
    // 加载订单详情
    const loadOrderDetail = async () => {
      try {
        loading.value = true
        
        // 获取订单详情
        const orderData = await store.dispatch('order/getOrderById', orderId)
        orderDetail.value = orderData
        
        // 获取地址信息
        if (orderDetail.value && orderDetail.value.address_id) {
          const addressData = await store.dispatch('user/getAddressById', orderDetail.value.address_id)
          address.value = addressData
        }
        
        // 如果订单状态为已发货或已完成，获取物流信息
        if (orderDetail.value && (orderDetail.value.order_status === 3 || orderDetail.value.order_status === 4)) {
          const logisticsData = await store.dispatch('order/getLogistics', orderId)
          logistics.value = logisticsData
        }
      } catch (error) {
        ElMessage.error('获取订单详情失败')
        orderDetail.value = null
      } finally {
        loading.value = false
      }
    }
    
    // 初始化
    onMounted(() => {
      if (!orderId) {
        ElMessage.error('订单ID不能为空')
        goBack()
        return
      }
      
      loadOrderDetail()
    })
    */
    
    return {
      loading,
      orderDetail,
      address,
      logistics,
      getStatusText,
      getStatusClass,
      getPaymentMethodText,
      getProductsAmount,
      formatTime,
      goBack,
      viewTeaDetail,
      continuePay,
      cancelOrder,
      confirmReceipt,
      viewLogistics,
      contactSeller,
      writeReview,
      buyAgain,
      deleteOrder,
      defaultTeaImage
      
      /*
      // 真实代码（开发UI时注释）
      loading,
      orderDetail,
      address,
      logistics,
      getStatusText,
      getStatusClass,
      getPaymentMethodText,
      getProductsAmount,
      formatTime,
      goBack,
      viewTeaDetail,
      continuePay,
      cancelOrder,
      confirmReceipt,
      viewLogistics,
      contactSeller,
      writeReview,
      buyAgain,
      deleteOrder
      */
    }
  }
}
</script>

<style lang="scss" scoped>
.order-detail-page {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.detail-card {
  max-width: 1000px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .left {
    display: flex;
    align-items: center;
    gap: 15px;
  }
  
  .title {
    font-size: 18px;
    font-weight: bold;
  }
  
  .status {
    .status-tag {
      padding: 4px 10px;
      border-radius: 4px;
      font-size: 13px;
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
  }
}

.order-detail-content {
  padding: 10px;
}

.detail-section {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
  
  &:last-child {
    border-bottom: none;
    margin-bottom: 0;
    padding-bottom: 0;
  }
  
  .section-title {
    font-size: 16px;
    font-weight: bold;
    margin-bottom: 15px;
    color: #303133;
  }
  
  .info-item {
    display: flex;
    margin-bottom: 10px;
    
    &:last-child {
      margin-bottom: 0;
    }
    
    .label {
      width: 100px;
      color: #606266;
    }
    
    .value {
      flex: 1;
      color: #303133;
    }
  }
}

.products-list {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  overflow: hidden;
  
  .product-item {
    display: flex;
    align-items: center;
    padding: 15px;
    cursor: pointer;
    
    &:not(:last-child) {
      border-bottom: 1px solid #ebeef5;
    }
    
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
      
      .product-name {
        font-weight: 500;
        color: #303133;
        margin-bottom: 5px;
      }
      
      .product-spec {
        color: #909399;
        font-size: 12px;
      }
    }
    
    .product-price,
    .product-quantity,
    .product-subtotal {
      width: 100px;
      text-align: center;
      color: #606266;
    }
    
    .product-subtotal {
      color: #f56c6c;
      font-weight: 500;
    }
  }
}

.amount-info {
  background-color: #f8f9fa;
  padding: 15px;
  border-radius: 4px;
  
  .amount-item {
    display: flex;
    justify-content: flex-end;
    margin-bottom: 10px;
    
    &:last-child {
      margin-bottom: 0;
    }
    
    .label {
      width: auto;
      margin-right: 10px;
      color: #606266;
    }
    
    .value {
      width: 120px;
      text-align: right;
      color: #303133;
    }
    
    &.total {
      margin-top: 15px;
      border-top: 1px dashed #dcdfe6;
      padding-top: 15px;
      
      .label {
        font-weight: bold;
        color: #303133;
      }
      
      .value {
        font-size: 18px;
        font-weight: bold;
        color: #f56c6c;
      }
    }
  }
}

.logistics-info {
  margin-bottom: 20px;
}

.logistics-timeline {
  background-color: #f8f9fa;
  padding: 15px;
  border-radius: 4px;
  
  .timeline-title {
    font-weight: 500;
    margin-bottom: 15px;
    color: #303133;
  }
}

.empty-detail {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 0;
  
  .el-button {
    margin-top: 20px;
  }
}

.action-section {
  text-align: center;
  padding-top: 20px;
  
  .el-button {
    margin: 0 10px 10px 0;
  }
}

@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .products-list {
    .product-item {
      flex-wrap: wrap;
      
      .product-image {
        margin-bottom: 10px;
      }
      
      .product-info {
        width: 100%;
        margin-bottom: 10px;
      }
      
      .product-price,
      .product-quantity,
      .product-subtotal {
        flex: 1;
        text-align: center;
      }
    }
  }
}
</style> 