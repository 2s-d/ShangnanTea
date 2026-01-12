<template>
  <div class="payment-page">
    <el-card class="box-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span class="title">订单支付</span>
          <div class="order-info">
            <span class="countdown">
              <el-icon><Timer /></el-icon>
              支付剩余时间: {{ formatTime(countdown) }}
            </span>
          </div>
        </div>
      </template>

      <div v-loading="loading" class="payment-content">
        <!-- 商品信息与订单号 -->
        <div class="payment-section tea-items-section">
          <div class="section-title">商品信息</div>
          <div v-for="(item, index) in teaItems" :key="index" class="tea-item">
            <div class="tea-image">
              <SafeImage :src="item.image" type="tea" :alt="item.name" style="width:80px;height:80px;object-fit:cover;" />
            </div>
            <div class="tea-info">
              <div class="tea-name">{{ item.name }}</div>
              <div class="tea-spec">规格：{{ item.spec }}</div>
              <div class="tea-quantity">数量：{{ item.quantity }}</div>
              <div class="order-id">订单编号：{{ item.orderId }}</div>
            </div>
            <div class="tea-price">¥{{ item.price.toFixed(2) }}</div>
          </div>
        </div>

        <!-- 订单信息摘要 -->
        <div class="payment-section order-summary">
          <div class="section-title">订单信息</div>
          <div class="order-amount">
            <span class="amount-label">支付金额</span>
            <span class="amount-value">¥{{ orderAmount.toFixed(2) }}</span>
          </div>
          <div class="order-detail">
            <div class="detail-item">
              <span class="label">商品金额：</span>
              <span class="value">¥{{ orderDetail.goodsAmount.toFixed(2) }}</span>
            </div>
            <div class="detail-item">
              <span class="label">运费：</span>
              <span class="value">¥{{ orderDetail.shippingFee.toFixed(2) }}</span>
            </div>
          </div>
        </div>

        <!-- 支付方式部分 -->
        <div class="payment-section payment-method">
          <div class="section-title">支付方式</div>
          
          <div class="payment-method-container">
            <div class="payment-method-info">
              <div class="payment-icon-container">
                <SafeImage 
                  v-if="paymentMethod === 'wechat'" 
                  src="/images/payments/wechat.jpg" 
                  type="banner"
                  alt="微信支付" 
                  class="payment-icon" 
                />
                <SafeImage 
                  v-else-if="paymentMethod === 'alipay'" 
                  src="/images/payments/alipay.jpg" 
                  type="banner"
                  alt="支付宝" 
                  class="payment-icon" 
                />
              </div>
              <div class="payment-desc">
                <div class="payment-method-name">
                  {{ paymentMethod === 'wechat' ? '微信支付' : '支付宝' }}（扫码支付）
                </div>
                <div class="payment-tip">点击"立即支付"按钮完成支付</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 支付操作 -->
        <div class="payment-section payment-action">
          <el-button 
            type="primary" 
            size="large" 
            :loading="submitting" 
            @click="handlePayment"
            class="pay-button"
          >
            立即支付
          </el-button>
          <el-button @click="cancelPayment">取消支付</el-button>
        </div>

        <!-- 支付说明 -->
        <div class="payment-section payment-notice">
          <div class="notice-title">
            <el-icon><InfoFilled /></el-icon>
            支付说明
          </div>
          <div class="notice-content">
            <p>1. 请在30分钟内完成支付，超时订单将自动取消</p>
            <p>2. 支付成功后，将自动跳转到订单详情页面</p>
            <p>3. 如遇支付问题，请联系客服：400-123-4567</p>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessageBox } from 'element-plus'
import { InfoFilled, Timer } from '@element-plus/icons-vue'
import SafeImage from '@/components/common/form/SafeImage.vue'
import { showByCode, isSuccess } from '@/utils/apiMessages'
import { errorMessage } from '@/utils/messageManager'
import { orderPromptMessages } from '@/utils/promptMessages'

export default {
  name: 'PaymentPage',
  components: {
    InfoFilled,
    Timer,
    SafeImage
  },
  setup() {
    const router = useRouter()
    const route = useRoute()
    const store = useStore()

    // 一个支付页只处理单个订单，优先从 query.orderId / params.id 取
    const orderId = ref(
      route.query.orderId ||
      (route.query.orderIds ? String(route.query.orderIds).split(',')[0] : '') ||
      route.params.id ||
      ''
    )

    const loading = ref(false)
    const submitting = ref(false)
    const paymentMethod = ref('wechat') // 默认微信

    // 当前订单数据来自 Vuex
    const currentOrder = computed(() => store.state.order.currentOrder || {})

    const orderAmount = computed(() => {
      const o = currentOrder.value
      return o.total_amount || o.order_amount || 0
    })

    const orderDetail = computed(() => {
      const o = currentOrder.value
      const shipping = o.shipping_fee || 0
      const total = o.total_amount || o.order_amount || 0
      return {
        goodsAmount: Math.max(total - shipping, 0),
        shippingFee: shipping
      }
    })

    // 支付页只展示当前订单的第一件商品信息
    const teaItems = computed(() => {
      const o = currentOrder.value
      if (!o.order_id) {
        return []
      }
      return [
        {
          orderId: o.order_id,
          name: o.tea_name || '茶叶商品',
          spec: o.spec_name || '默认规格',
          quantity: o.quantity || 1,
          price: o.price || o.total_amount || 0,
          image: o.tea_image || '/images/tea-default.jpg'
        }
      ]
    })

    // 30 分钟倒计时（单位：秒）
    const countdown = ref(1800)
    let countdownTimer = null

    const formatTime = seconds => {
      const m = Math.floor(seconds / 60)
      const s = seconds % 60
      return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
    }

    const startCountdown = () => {
      if (countdownTimer) {
        clearInterval(countdownTimer)
      }
      countdownTimer = setInterval(() => {
        countdown.value -= 1
        if (countdown.value <= 0) {
          clearInterval(countdownTimer)
          handleOrderTimeout()
        }
      }, 1000)
    }

    const handleOrderTimeout = async () => {
      if (!orderId.value) {
        router.push('/order/list')
        return
      }
      try {
        showByCode(4122) // 支付超时，订单已自动取消
        await store.dispatch('order/cancelOrder', orderId.value)
      } catch (e) {
        // 忽略失败，仍然跳转
      }
      router.push('/order/list')
    }

    const loadOrderData = async () => {
      if (!orderId.value) {
        orderPromptMessages.showOrderIdRequired()
        router.push('/order/list')
        return
      }
      loading.value = true
      try {
        await store.dispatch('order/fetchOrderDetail', orderId.value)
        // 使用后端返回的支付方式
        if (currentOrder.value && currentOrder.value.payment_method) {
          paymentMethod.value = currentOrder.value.payment_method
        }
        // 简化：统一从现在开始倒计时 30 分钟
        countdown.value = 1800
        startCountdown()
      } catch (error) {
        showByCode(4123) // 加载订单信息失败
        router.push('/order/list')
      } finally {
        loading.value = false
      }
    }

    const handlePayment = async () => {
      if (!orderId.value) {
        orderPromptMessages.showOrderIdRequired()
        return
      }
      submitting.value = true
      try {
        const res = await store.dispatch('order/payOrder', {
          orderId: orderId.value,
          paymentMethod: paymentMethod.value
        })
        if (res?.code) showByCode(res.code)
        router.push(`/order/detail/${orderId.value}`)
      } catch (error) {
        errorMessage.show(error?.message || '支付失败')
      } finally {
        submitting.value = false
      }
    }

    const cancelPayment = () => {
      if (!orderId.value) {
        router.push('/order/list')
        return
      }
      ElMessageBox.confirm('确定要取消支付吗？订单将被取消', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '继续支付',
        type: 'warning'
      })
        .then(async () => {
          try {
            const res = await store.dispatch('order/cancelOrder', orderId.value)
            if (res?.code) showByCode(res.code)
          } catch (error) {
            errorMessage.show(error?.message || '取消订单失败')
          } finally {
            router.push('/order/list')
          }
        })
        .catch(() => {
          // 用户选择继续支付，不做处理
        })
    }

    onMounted(() => {
      loadOrderData()
    })

    onUnmounted(() => {
      if (countdownTimer) {
        clearInterval(countdownTimer)
      }
    })

    return {
      loading,
      submitting,
      paymentMethod,
      teaItems,
      orderAmount,
      orderDetail,
      countdown,
      formatTime,
      handlePayment,
      cancelPayment
    }
  }
}
</script>

<style lang="scss" scoped>
.payment-page {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.box-card {
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .title {
    font-size: 18px;
    font-weight: bold;
  }
  
  .order-info {
    color: #606266;
    font-size: 14px;
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    
    .countdown {
      margin-top: 5px;
      color: #f56c6c;
      font-weight: bold;
      display: flex;
      align-items: center;
      gap: 5px;
    }
  }
}

.payment-content {
  min-height: 400px;
}

.payment-section {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
  
  &:last-child {
    border-bottom: none;
    margin-bottom: 0;
  }
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 20px;
  color: #303133;
}

// 茶叶商品信息样式
.tea-items-section {
  .tea-item {
    display: flex;
    align-items: center;
    padding: 15px;
    border: 1px solid #ebeef5;
    border-radius: 4px;
    background-color: #f8f9fa;
    margin-bottom: 12px;
    
    &:last-child {
      margin-bottom: 0;
    }
  }
  
  .tea-image {
    width: 80px;
    height: 80px;
    overflow: hidden;
    border-radius: 4px;
    margin-right: 15px;
    
    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }
  
  .tea-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 5px;
  }
  
  .tea-name {
    font-weight: bold;
    font-size: 16px;
    color: #303133;
  }
  
  .tea-spec, .tea-quantity {
    color: #606266;
    font-size: 14px;
  }
  
  .order-id {
    margin-top: 5px;
    color: #909399;
    font-size: 13px;
  }
  
  .tea-price {
    font-size: 18px;
    font-weight: bold;
    color: #f56c6c;
    margin-left: 15px;
  }
}

// 订单摘要样式
.order-summary {
  .order-amount {
    text-align: center;
    margin-bottom: 20px;
    
    .amount-label {
      font-size: 16px;
      color: #606266;
      margin-right: 10px;
    }
    
    .amount-value {
      font-size: 24px;
      font-weight: bold;
      color: #f56c6c;
    }
  }
  
  .order-detail {
    background-color: #f8f9fa;
    padding: 15px;
    border-radius: 4px;
    
    .detail-item {
      display: flex;
      justify-content: space-between;
      margin-bottom: 10px;
      
      &:last-child {
        margin-bottom: 0;
      }
      
      .label {
        color: #606266;
      }
      
      .value {
        font-weight: 500;
      }
    }
  }
}

// 支付方式样式
.payment-method {
  .payment-method-container {
    display: flex;
    justify-content: center;
    padding: 20px 0;
  }
  
  .payment-method-info {
    display: flex;
    align-items: center;
    padding: 15px 20px;
    border: 1px solid #dcdfe6;
    border-radius: 4px;
    background-color: #fff;
  }
  
  .payment-icon-container {
    margin-right: 15px;
    
    .payment-icon {
      width: 48px;
      height: 48px;
      object-fit: contain;
    }
  }
  
  .payment-desc {
    .payment-method-name {
      font-weight: bold;
      color: #303133;
      margin-bottom: 5px;
    }
    
    .payment-tip {
      color: #909399;
      font-size: 13px;
    }
  }
}

// 支付操作样式
.payment-action {
  text-align: center;
  padding: 20px 0;
  
  .pay-button {
    min-width: 150px;
    margin-right: 20px;
  }
}

// 支付说明样式
.payment-notice {
  background-color: #f8f9fa;
  padding: 15px;
  border-radius: 4px;
  
  .notice-title {
    display: flex;
    align-items: center;
    gap: 5px;
    font-weight: bold;
    margin-bottom: 10px;
    color: #409eff;
  }
  
  .notice-content {
    color: #606266;
    font-size: 14px;
    line-height: 1.6;
  }
}

// 响应式样式调整
@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
    
    .order-info {
      align-items: flex-start;
    }
  }
  
  .tea-item {
    flex-direction: column;
    align-items: flex-start;
    
    .tea-image {
      margin-right: 0;
      margin-bottom: 10px;
    }
    
    .tea-price {
      margin-left: 0;
      margin-top: 10px;
    }
  }
  
  .payment-action {
    .pay-button {
      margin-right: 0;
      margin-bottom: 10px;
    }
  }
  
  .payment-method-info {
    flex-direction: column;
    
    .payment-icon-container {
      margin-right: 0;
      margin-bottom: 10px;
    }
    
    .payment-desc {
      text-align: center;
    }
  }
}
</style> 