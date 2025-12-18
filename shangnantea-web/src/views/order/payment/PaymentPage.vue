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
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { InfoFilled, Timer } from '@element-plus/icons-vue'
import SafeImage from '@/components/common/form/SafeImage.vue'

/*
// 真实代码（开发UI时注释）
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import { InfoFilled, Timer } from '@element-plus/icons-vue'
import SafeImage from '@/components/common/form/SafeImage.vue'
*/

export default {
  name: 'PaymentPage',
  components: {
    InfoFilled,
    Timer,
    SafeImage
    
    /*
    // 真实代码（开发UI时注释）
    InfoFilled,
    Timer,
    SafeImage
    */
  },
  setup() {
    const router = useRouter()
    const route = useRoute()

    // 从路由参数获取订单ID数组
    const orderIds = ref(route.query.orderIds ? route.query.orderIds.split(',') : ['ORD' + Date.now()])
    const loading = ref(false)
    const submitting = ref(false)
    const paymentMethod = ref('wechat') // 默认选中微信支付
    
    // 模拟多个茶叶商品信息
    const teaItems = ref([
      {
        orderId: 'ORD' + (Date.now() - 100000),
        name: '商南毛尖特级',
        spec: '100g/盒',
        quantity: 2,
        price: 219.00,
        image: '/images/tea1.jpg'
      },
      {
        orderId: 'ORD' + Date.now(),
        name: '商南绿茶珍品',
        spec: '200g/罐',
        quantity: 1,
        price: 156.00,
        image: '/images/tea2.jpg'
      },
      {
        orderId: 'ORD' + (Date.now() + 100000),
        name: '商南红茶礼盒',
        spec: '300g/礼盒',
        quantity: 1,
        price: 83.00,
        image: '/images/tea3.jpg'
      }
    ])
    
    // 模拟订单金额和详情
    const orderAmount = ref(458.00)
    const orderDetail = ref({
      goodsAmount: 438.00,
      shippingFee: 20.00
    })
    
    // 30分钟倒计时 (30 * 60 = 1800秒)
    const countdown = ref(1800)
    let countdownTimer = null
    
    // 格式化时间为 mm:ss 格式
    const formatTime = (seconds) => {
      const minutes = Math.floor(seconds / 60)
      const remainingSeconds = seconds % 60
      return `${minutes.toString().padStart(2, '0')}:${remainingSeconds.toString().padStart(2, '0')}`
    }
    
    // 开始倒计时
    const startCountdown = () => {
      countdownTimer = setInterval(() => {
        countdown.value--
        
        if (countdown.value <= 0) {
          clearInterval(countdownTimer)
          // 订单超时，自动取消
          handleOrderTimeout()
        }
      }, 1000)
    }
    
    // 订单超时处理
    const handleOrderTimeout = () => {
      ElMessage.warning('支付超时，订单已自动取消')
      // 模拟取消订单
      setTimeout(() => {
        router.push('/order/list')
      }, 1500)
    }
    
    // 模拟加载订单数据
    const loadOrderData = () => {
      loading.value = true
      
      setTimeout(() => {
        // 模拟订单数据加载完成
        loading.value = false
        // 开始倒计时
        startCountdown()
      }, 800)
    }
    
    // 处理支付
    const handlePayment = () => {
      submitting.value = true
      
      // 模拟免密支付过程
      setTimeout(() => {
        // 显示支付成功消息
        ElMessage.success('免密支付成功')
        
        // 延迟一小段时间后跳转到订单列表页
        setTimeout(() => {
          submitting.value = false
          router.push('/order/list')
        }, 500)
      }, 1500)
    }
    
    // 取消支付
    const cancelPayment = () => {
      ElMessageBox.confirm('确定要取消支付吗？所有订单将被取消', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '继续支付',
        type: 'warning'
      }).then(() => {
        // 模拟取消订单
        ElMessage.success('订单已取消')
        // 跳转到订单列表页面
        router.push('/order/list')
      }).catch(() => {
        // 用户选择继续支付，不做任何操作
      })
    }
    
    // 组件卸载时清除定时器
    onUnmounted(() => {
      if (countdownTimer) {
        clearInterval(countdownTimer)
      }
    })

    
    /*
    // 真实代码（开发UI时注释）
    const store = useStore()
    
    // 从路由参数获取订单ID数组
    const orderIds = ref(route.query.orderIds ? route.query.orderIds.split(',') : [])
    const loading = ref(false)
    const submitting = ref(false)
    const paymentMethod = ref('wechat') // 默认选中微信支付
    const ordersData = ref([])
    
    // 茶叶商品信息数组
    const teaItems = ref([])
    
    // 30分钟倒计时
    const countdown = ref(1800) // 30 * 60 = 1800秒
    let countdownTimer = null
    
    // 格式化时间为 mm:ss 格式
    const formatTime = (seconds) => {
      const minutes = Math.floor(seconds / 60)
      const remainingSeconds = seconds % 60
      return `${minutes.toString().padStart(2, '0')}:${remainingSeconds.toString().padStart(2, '0')}`
    }
    
    // 计算订单剩余支付时间 - 使用最早创建的订单时间
    const calculateRemainingTime = (ordersData) => {
      if (!ordersData || ordersData.length === 0) return 1800
      
      // 找出最早创建的订单
      let earliestTime = new Date().getTime()
      
      ordersData.forEach(order => {
        const orderTime = new Date(order.create_time).getTime()
        if (orderTime < earliestTime) {
          earliestTime = orderTime
        }
      })
      
      const currentTime = new Date().getTime()
      const elapsedSeconds = Math.floor((currentTime - earliestTime) / 1000)
      const remainingSeconds = Math.max(1800 - elapsedSeconds, 0)
      return remainingSeconds
    }
    
    // 开始倒计时
    const startCountdown = (remainingTime) => {
      countdown.value = remainingTime
      
      countdownTimer = setInterval(() => {
        countdown.value--
        
        if (countdown.value <= 0) {
          clearInterval(countdownTimer)
          // 订单超时，自动取消
          handleOrderTimeout()
        }
      }, 1000)
    }
    
    // 订单超时处理
    const handleOrderTimeout = async () => {
      try {
        // 批量取消所有订单
        const cancelPromises = orderIds.value.map(orderId => 
          store.dispatch('order/cancelOrder', {
            orderId: orderId,
            reason: '支付超时，系统自动取消'
          })
        )
        
        await Promise.all(cancelPromises)
        
        ElMessage.warning('支付超时，订单已自动取消')
        router.push('/order/list')
      } catch (error) {
        console.error('取消订单失败:', error)
      }
    }
    
    // 计算属性：订单总金额
    const orderAmount = computed(() => {
      if (!ordersData.value || ordersData.value.length === 0) return 0
      
      return ordersData.value.reduce((total, order) => {
        return total + order.order_amount
      }, 0)
    })
    
    // 订单详情
    const orderDetail = computed(() => {
      if (!ordersData.value || ordersData.value.length === 0) {
        return {
          goodsAmount: 0,
          shippingFee: 0
        }
      }
      
      // 汇总所有订单的商品金额和运费
      const totalShippingFee = ordersData.value.reduce((total, order) => {
        return total + order.shipping_fee
      }, 0)
      
      return {
        goodsAmount: orderAmount.value - totalShippingFee,
        shippingFee: totalShippingFee
      }
    })
    
    // 加载订单数据
    const loadOrderData = async () => {
      if (!orderIds.value || orderIds.value.length === 0) {
        ElMessage.error('订单不存在')
        router.push('/order/list')
        return
      }
      
      try {
        loading.value = true
        
        // 获取多个订单详情
        const orderPromises = orderIds.value.map(orderId => 
          store.dispatch('order/getOrderById', orderId)
        )
        
        const responses = await Promise.all(orderPromises)
        ordersData.value = responses.filter(Boolean) // 过滤掉可能的null响应
        
        // 如果没有有效订单，跳转到订单列表
        if (ordersData.value.length === 0) {
          ElMessage.error('没有找到有效订单')
          router.push('/order/list')
          return
        }
        
        // 检查是否所有订单都是待支付状态
        const invalidOrder = ordersData.value.find(order => order.order_status !== 1)
        if (invalidOrder) {
          ElMessage.warning('部分订单状态已变更，无需支付')
          router.push('/order/list')
          return
        }
        
        // 设置商品信息 - 从每个订单提取第一个商品项
        teaItems.value = ordersData.value.map(order => {
          const item = order.items && order.items.length > 0 ? order.items[0] : {}
          return {
            orderId: order.order_id,
            name: item.tea_name || '未知商品',
            spec: item.spec_name || '默认规格',
            quantity: item.quantity || 1,
            price: item.price || 0,
            image: item.image || '/images/default-tea.jpg'
          }
        })
        
        // 使用第一个订单的支付方式
        paymentMethod.value = ordersData.value[0].payment_method || 'wechat'
        
        // 计算订单剩余支付时间并开始倒计时
        const remainingTime = calculateRemainingTime(ordersData.value)
        if (remainingTime <= 0) {
          handleOrderTimeout()
        } else {
          startCountdown(remainingTime)
        }
      } catch (error) {
        ElMessage.error('获取订单信息失败')
        router.push('/order/list')
      } finally {
        loading.value = false
      }
    }
    
    // 处理支付
    const handlePayment = async () => {
      try {
        submitting.value = true
        
        // 模拟免密支付过程 - 直接调用支付完成API
        await Promise.all(orderIds.value.map(orderId => 
          store.dispatch('order/completePayment', { orderId })
        ))
        
        // 显示支付成功消息
        ElMessage.success('免密支付成功')
        
        // 延迟一小段时间后跳转到订单列表页
        setTimeout(() => {
          router.push('/order/list')
        }, 500)
      } catch (error) {
        ElMessage.error(error.message || '支付失败，请重试')
      } finally {
        submitting.value = false
      }
    }
    
    // 取消支付
    const cancelPayment = () => {
      ElMessageBox.confirm('确定要取消支付吗？所有订单将被取消', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '继续支付',
        type: 'warning'
      }).then(async () => {
        try {
          // 批量取消所有订单
          const cancelPromises = orderIds.value.map(orderId => 
            store.dispatch('order/cancelOrder', {
              orderId: orderId,
              reason: '用户取消支付'
            })
          )
          
          await Promise.all(cancelPromises)
          
          ElMessage.success('订单已取消')
          router.push('/order/list')
        } catch (error) {
          ElMessage.error('取消订单失败')
        }
      }).catch(() => {
        // 用户选择继续支付，不做任何操作
      })
    }
    
    // 组件卸载时清除定时器
    onUnmounted(() => {
      if (countdownTimer) {
        clearInterval(countdownTimer)
      }
    })
    */
    
    // 初始化
    onMounted(() => {
      loadOrderData()
      
      /*
      // 真实代码（开发UI时注释）
      loadOrderData()
      */
    })
    
    // 默认图片（生产形态：不使用 mock-images）
    const defaultTeaImage = ''
    const defaultPaymentImage = ''
    
    return {
      teaItems,
      orderAmount,
      orderDetail,
      loading,
      submitting,
      paymentMethod,
      countdown,
      formatTime,
      handlePayment,
      cancelPayment,
      defaultTeaImage,
      defaultPaymentImage
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