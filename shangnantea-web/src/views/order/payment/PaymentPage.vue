<template>
  <div class="payment-result-page">
    <el-card class="box-card" shadow="hover">
      <div v-loading="loading" class="result-content">
        <!-- 加载中 -->
        <div v-if="loading" class="loading-section">
          <el-icon class="is-loading" :size="60"><Loading /></el-icon>
          <p class="loading-text">正在确认支付结果...</p>
          <p class="loading-tip">请稍候，不要关闭页面</p>
        </div>

        <!-- 支付成功 -->
        <div v-else-if="paymentSuccess" class="success-section">
          <el-icon class="success-icon" :size="80"><CircleCheck /></el-icon>
          <h2 class="result-title">支付成功！</h2>
          <p class="result-desc">您的订单已支付成功，我们将尽快为您发货</p>
          
          <div class="order-info">
            <div class="info-item">
              <span class="label">支付单号：</span>
              <span class="value">{{ paymentIdDisplay }}</span>
            </div>
            <div class="info-item">
              <span class="label">支付金额：</span>
              <span class="value amount">¥{{ orderAmount.toFixed(2) }}</span>
            </div>
            <div class="info-item">
              <span class="label">支付时间：</span>
              <span class="value">{{ paymentTime }}</span>
            </div>
          </div>

          <div class="action-buttons">
            <el-button size="large" @click="goToOrderList">
              我的订单
            </el-button>
            <el-button size="large" @click="goToHome">
              返回首页
            </el-button>
          </div>
        </div>

        <!-- 支付失败或取消 -->
        <div v-else class="failure-section">
          <el-icon class="failure-icon" :size="80"><CircleClose /></el-icon>
          <h2 class="result-title">支付失败</h2>
          <p class="result-desc">{{ failureReason }}</p>
          
          <div class="order-info" v-if="paymentIdDisplay">
            <div class="info-item">
              <span class="label">支付单号：</span>
              <span class="value">{{ paymentIdDisplay }}</span>
            </div>
          </div>

          <div class="action-buttons">
            <el-button type="primary" size="large" @click="retryPayment" v-if="currentOrder?.id">
              重新支付
            </el-button>
            <el-button size="large" @click="goToOrderList">
              我的订单
            </el-button>
            <el-button size="large" @click="goToHome">
              返回首页
            </el-button>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useOrderStore } from '@/stores/order'
import { CircleCheck, CircleClose, Loading } from '@element-plus/icons-vue'

defineOptions({
  name: 'PaymentPage'
})

const router = useRouter()
const route = useRoute()
const orderStore = useOrderStore()

const loading = ref(true)
const paymentSuccess = ref(false)
const failureReason = ref('支付已取消或支付失败，请重试')

// URL里可能是订单ID(OD...)，也可能是支付单ID(PM...)
const queryId = ref(route.query.orderId || route.query.out_trade_no || '')
const isPaymentId = computed(() => String(queryId.value || '').startsWith('PM'))
const orderId = computed(() => currentOrder.value?.id || '')
const paymentIdDisplay = computed(() => {
  // 优先显示 URL 中的 PM（支付宝回跳参数）
  const fromQuery = String(queryId.value || '')
  if (fromQuery.startsWith('PM')) return fromQuery
  // 兜底显示订单详情中的 paymentId
  return currentOrder.value?.paymentId || fromQuery
})

// 当前订单数据
const currentOrder = computed(() => orderStore.currentOrder || {})

const orderAmount = computed(() => {
  // 后端详情字段是 totalPrice / paymentTotalAmount（不是 totalAmount）
  const amount =
    currentOrder.value?.paymentTotalAmount ??
    currentOrder.value?.totalPrice ??
    currentOrder.value?.totalAmount ??
    0
  const num = Number(amount)
  return Number.isFinite(num) ? num : 0
})

const paymentTime = computed(() => {
  if (currentOrder.value.paymentTime) {
    return new Date(currentOrder.value.paymentTime).toLocaleString('zh-CN')
  }
  return new Date().toLocaleString('zh-CN')
})

let pollingTimer = null
let pollingCount = 0
const MAX_POLLING_COUNT = 10 // 最多轮询10次

/**
 * 轮询查询订单状态
 * 因为支付宝异步回调可能有延迟，需要轮询确认
 */
const checkPaymentStatus = async () => {
  if (!queryId.value) {
    loading.value = false
    paymentSuccess.value = false
    failureReason.value = '订单信息丢失，请在"我的订单"中查看'
    return
  }

  try {
    // URL 是支付单ID时，走按 paymentId 查询；否则按 orderId 查询
    if (isPaymentId.value) {
      await orderStore.fetchOrderDetailByPaymentId(queryId.value)
    } else {
      await orderStore.fetchOrderDetail(queryId.value)
    }
    
    const order = currentOrder.value
    
    // 检查订单状态
    // 订单状态：0-待付款, 1-待发货(已支付), 2-待收货, 3-已完成, 4-已取消, 5-已退款
    if (order.status === 1 || order.status === 2 || order.status === 3) {
      // 支付成功
      paymentSuccess.value = true
      loading.value = false
      stopPolling()
      return
    }
    
    // 如果还是待付款状态，继续轮询
    if (order.status === 0) {
      pollingCount++
      
      if (pollingCount >= MAX_POLLING_COUNT) {
        // 轮询次数用完，认为支付失败
        paymentSuccess.value = false
        loading.value = false
        failureReason.value = '支付结果确认超时，请稍后在"我的订单"中查看订单状态'
        stopPolling()
        return
      }
      
      // 继续轮询，2秒后再次查询
      pollingTimer = setTimeout(checkPaymentStatus, 2000)
      return
    }
    
    // 其他状态（如已取消），认为支付失败
    paymentSuccess.value = false
    loading.value = false
    failureReason.value = '订单已取消或支付失败'
    stopPolling()
    
  } catch (error) {
    console.error('查询订单状态失败:', error)
    
    pollingCount++
    
    if (pollingCount >= MAX_POLLING_COUNT) {
      paymentSuccess.value = false
      loading.value = false
      failureReason.value = '无法确认支付结果，请稍后在"我的订单"中查看'
      stopPolling()
    } else {
      // 出错了也继续轮询
      pollingTimer = setTimeout(checkPaymentStatus, 2000)
    }
  }
}

const stopPolling = () => {
  if (pollingTimer) {
    clearTimeout(pollingTimer)
    pollingTimer = null
  }
}

const goToOrderList = () => {
  router.push('/order/list')
}

const goToHome = () => {
  router.push('/tea-culture')
}

const retryPayment = () => {
  // 跳转回结算页面，重新发起支付
  if (currentOrder.value?.id) {
    router.push(`/order/checkout?orderId=${currentOrder.value.id}`)
    return
  }
  router.push('/order/list')
}

onMounted(() => {
  // 开始轮询查询订单状态
  checkPaymentStatus()
})

onUnmounted(() => {
  stopPolling()
})
</script>

<style lang="scss" scoped>
.payment-result-page {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
  display: flex;
  align-items: center;
  justify-content: center;
}

.box-card {
  max-width: 600px;
  width: 100%;
}

.result-content {
  min-height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
}

// 加载中样式
.loading-section {
  text-align: center;
  
  .loading-text {
    font-size: 18px;
    color: #303133;
    margin-top: 20px;
    margin-bottom: 10px;
  }
  
  .loading-tip {
    font-size: 14px;
    color: #909399;
  }
}

// 成功样式
.success-section {
  text-align: center;
  width: 100%;
  
  .success-icon {
    color: #67c23a;
    margin-bottom: 20px;
  }
}

// 失败样式
.failure-section {
  text-align: center;
  width: 100%;
  
  .failure-icon {
    color: #f56c6c;
    margin-bottom: 20px;
  }
}

// 通用样式
.result-title {
  font-size: 24px;
  color: #303133;
  margin-bottom: 10px;
}

.result-desc {
  font-size: 16px;
  color: #606266;
  margin-bottom: 30px;
}

.order-info {
  background-color: #f8f9fa;
  padding: 20px;
  border-radius: 4px;
  margin-bottom: 30px;
  text-align: left;
  
  .info-item {
    display: flex;
    justify-content: space-between;
    margin-bottom: 12px;
    
    &:last-child {
      margin-bottom: 0;
    }
    
    .label {
      color: #606266;
      font-size: 14px;
    }
    
    .value {
      color: #303133;
      font-size: 14px;
      font-weight: 500;
      
      &.amount {
        color: #f56c6c;
        font-size: 18px;
        font-weight: bold;
      }
    }
  }
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 12px;
  
  .el-button {
    width: 100%;
  }
}

// 响应式
@media (min-width: 768px) {
  .action-buttons {
    flex-direction: row;
    justify-content: center;
    
    .el-button {
      width: auto;
      min-width: 120px;
    }
  }
}
</style>
