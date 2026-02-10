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
            <span :class="['status-tag', getStatusClass(orderDetail.status)]">
              {{ getStatusText(orderDetail.status) }}
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
            <span class="value">{{ orderDetail.id }}</span>
          </div>
          <div class="info-item">
            <span class="label">下单时间：</span>
            <span class="value">{{ formatTime(orderDetail.createTime) }}</span>
          </div>
          <div class="info-item">
            <span class="label">支付方式：</span>
            <span class="value">{{ getPaymentMethodText(orderDetail.paymentMethod) }}</span>
          </div>
          <div v-if="orderDetail.status >= 2" class="info-item">
            <span class="label">付款时间：</span>
            <span class="value">{{ formatTime(orderDetail.paymentTime || orderDetail.updateTime) }}</span>
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
              @click="viewTeaDetail(orderDetail.teaId)"
            >
              <div class="product-image">
                <SafeImage :src="orderDetail.teaImage" type="tea" :alt="orderDetail.teaName" style="width:80px;height:80px;object-fit:cover;" />
              </div>
              <div class="product-info">
                <div class="product-name">{{ orderDetail.teaName }}</div>
                <div class="product-spec">规格：{{ orderDetail.specName }}</div>
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
            <span class="value">¥0.00</span>
          </div>
          <div class="amount-item total">
            <span class="label">实付金额：</span>
            <span class="value">¥{{ orderDetail.totalPrice.toFixed(2) }}</span>
          </div>
        </div>

        <!-- 物流信息 (仅在待收货和已完成状态显示) -->
        <div v-if="orderDetail.status === 2 || orderDetail.status === 3" class="detail-section">
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
        
        <!-- 退款信息 -->
        <div v-if="refundInfo.status" class="detail-section">
          <div class="section-title">退款信息</div>
          <div class="info-item">
            <span class="label">退款状态：</span>
            <span class="value">
              <el-tag v-if="refundInfo.status === 'pending'" type="warning">待审核</el-tag>
              <el-tag v-else-if="refundInfo.status === 'approved'" type="success">已同意</el-tag>
              <el-tag v-else-if="refundInfo.status === 'rejected'" type="danger">已拒绝</el-tag>
              <span v-else>{{ refundInfo.status }}</span>
            </span>
          </div>
          <div class="info-item">
            <span class="label">退款原因：</span>
            <span class="value">{{ refundInfo.reason || '未填写' }}</span>
          </div>
          <div class="info-item" v-if="refundInfo.process_reason">
            <span class="label">审核意见：</span>
            <span class="value">{{ refundInfo.process_reason }}</span>
          </div>
        </div>
        
        <!-- 操作按钮区域：待收货时支持查看物流与确认收货；已完成时显示评价按钮；可申请退款 -->
        <div class="detail-section action-section">
          <template v-if="orderDetail.status === 2">
            <el-button type="primary" @click="viewLogistics">查看详细物流</el-button>
            <el-button type="success" @click="confirmReceipt">确认收货</el-button>
          </template>
          <template v-else-if="canReview">
            <el-button type="primary" @click="writeReview">待评价</el-button>
            <el-button type="info" @click="viewLogistics">查看物流</el-button>
          </template>
          <el-button
            v-if="canRequestRefund"
            type="warning"
            @click="openRefundDialog"
          >
            申请退款
          </el-button>
        </div>
      </div>

      <div v-else-if="!loading" class="empty-detail">
        <el-empty description="订单不存在或已被删除" />
        <el-button @click="goBack">返回订单列表</el-button>
      </div>
    </el-card>

    <!-- 申请退款对话框 -->
    <el-dialog
      v-model="refundDialogVisible"
      title="申请退款"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form>
        <el-form-item label="退款原因" required>
          <el-input
            v-model="refundReason"
            type="textarea"
            :rows="4"
            placeholder="请详细说明退款原因，至少5个字"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="refundDialogVisible = false">取消</el-button>
        <el-button type="warning" :loading="refundSubmitting" @click="submitRefund">
          提交申请
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useOrderStore } from '@/stores/order'
import { ElMessageBox } from 'element-plus'
import { showByCode, isSuccess } from '@/utils/apiMessages'
import { orderPromptMessages } from '@/utils/promptMessages'
import SafeImage from '@/components/common/form/SafeImage.vue'
export default {
  name: 'OrderDetailPage',
  components: {
    SafeImage
  },
  setup() {
    const router = useRouter()
    const route = useRoute()
    const orderStore = useOrderStore()
    const loading = computed(() => orderStore.loading)
    
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
    
    const refundInfo = ref({
      status: '',
      reason: '',
      process_reason: ''
    })
    
    // 获取订单状态文本
    const getStatusText = status => {
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
    const getStatusClass = status => {
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
    const getPaymentMethodText = method => {
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
    const formatTime = timeStr => {
      if (!timeStr) return '--'
      
      const date = new Date(timeStr)
      return `${date.getFullYear()}-${padZero(date.getMonth() + 1)}-${padZero(date.getDate())} ${padZero(date.getHours())}:${padZero(date.getMinutes())}`
    }
    
    // 数字补零
    const padZero = num => {
      return num < 10 ? `0${num}` : num
    }
    
    // 返回订单列表
    const goBack = () => {
      router.push('/order/list')
    }
    
    // 查看茶叶详情
    const viewTeaDetail = teaId => {
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
        orderStore.cancelOrder(orderId)
          .then(res => {
            // res = {code, data}
            if (res && res.code !== 200) {
              showByCode(res.code)
            }
            loadOrderDetail()
          })
          .catch(error => {
            // 网络错误等已由响应拦截器处理，这里只记录日志
            if (process.env.NODE_ENV === 'development') {
              console.error('取消订单失败:', error)
            }
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
        orderStore.confirmReceipt(orderId)
          .then(res => {
            // res = {code, data}
            if (res && res.code !== 200) {
              showByCode(res.code)
            }
            loadOrderDetail()
          })
          .catch(error => {
            // 网络错误等已由响应拦截器处理，这里只记录日志
            if (process.env.NODE_ENV === 'development') {
              console.error('确认收货失败:', error)
            }
          })
      }).catch(() => {
        // 用户取消操作，不做任何处理
      })
    }
    
    // 查看物流：调用 Vuex action 获取最新物流信息并刷新本地展示
    const viewLogistics = async () => {
      try {
        const res = await orderStore.fetchOrderLogistics(orderId)
        showByCode(res?.code)
        const data = res?.data || res
        if (data) {
          logistics.value = {
            company: data.company || logistics.value.company,
            tracking_number: data.tracking_number || logistics.value.tracking_number,
            ship_time: data.ship_time || logistics.value.ship_time,
            traces: data.traces || logistics.value.traces
          }
        } else {
          orderPromptMessages.showNoLogisticsInfo()
        }
      } catch (error) {
        console.error('获取物流信息失败:', error)
      }
    }
    
    const canRequestRefund = computed(() => {
      if (!orderDetail.value) return false
      const status = orderDetail.value.status
      if (status === 5) return false
      // 简单规则：待发货/待收货/已完成允许申请一次
      return status === 1 || status === 2 || status === 3
    })
    
    // 是否可以评价（订单状态为已完成）
    const canReview = computed(() => {
      if (!orderDetail.value) return false
      return orderDetail.value.status === 3 // 已完成
    })
    
    const refundDialogVisible = ref(false)
    const refundSubmitting = ref(false)
    const refundReason = ref('')
    
    const openRefundDialog = () => {
      if (!canRequestRefund.value) {
        orderPromptMessages.showRefundNotSupported()
        return
      }
      refundReason.value = ''
      refundDialogVisible.value = true
    }
    
    const submitRefund = async () => {
      if (!refundReason.value || refundReason.value.trim().length < 5) {
        orderPromptMessages.showRefundReasonTooShort()
        return
      }
      refundSubmitting.value = true
      try {
        const res = await orderStore.applyRefund({
          orderId,
          reason: refundReason.value.trim()
        })
        // res = {code, data}
        if (res && res.code !== 200) {
          showByCode(res.code)
        }
        refundDialogVisible.value = false
        // 重新获取退款详情
        const detailRes = await orderStore.fetchRefundDetail(orderId)
        const detail = detailRes?.data || detailRes
        if (detail) {
          refundInfo.value = {
            status: detail.status || '',
            reason: detail.reason || '',
            process_reason: detail.process_reason || ''
          }
        }
      } catch (error) {
        // 5128: 退款申请提交失败（使用状态码消息系统；customMessage 仅用于补充异常信息）
        showByCode(5128, error?.message || null)
      } finally {
        refundSubmitting.value = false
      }
    }
    
    // 联系卖家
    const contactSeller = () => {
      orderPromptMessages.showContactSellerDev()
    }
    
    // 评价商品
    const writeReview = () => {
      router.push(`/order/review/${orderDetail.value.id}`)
    }
    
    // 再次购买
    const buyAgain = () => {
      orderPromptMessages.showBuyAgainDev()
      // 实际场景中，可能需要将所有商品添加到购物车，然后跳转到购物车页面
    }
    
    // 加载订单详情
    const loadOrderDetail = () => {
      orderStore.fetchOrderDetail(orderId)
        .then(res => {
          // res = {code, data}
          const data = res?.data || res
          orderDetail.value = data
          // 同步收货地址和物流信息
          const addr = data?.address || {}
          address.value = {
            name: addr.name || '',
            phone: addr.phone || '',
            province: addr.province || '',
            city: addr.city || '',
            district: addr.district || '',
            detail: addr.detail || ''
          }
          const logi = data?.logistics || {}
          logistics.value = {
            company: logi.company || '',
            tracking_number: logi.tracking_number || '',
            ship_time: logi.ship_time || '',
            traces: logi.traces || []
          }
          // 拉取退款详情（如果有）
          orderStore.fetchRefundDetail(orderId)
            .then(detailRes => {
              const detail = detailRes?.data || detailRes
              if (detail) {
                refundInfo.value = {
                  status: detail.status || '',
                  reason: detail.reason || '',
                  process_reason: detail.process_reason || ''
                }
              }
            })
        })
        .catch(error => {
          // 5116: 获取订单详情失败（使用状态码消息系统；customMessage 仅用于补充异常信息）
          showByCode(5116, error?.message || null)
          orderDetail.value = null
        })
    }
    
    // 默认图片（生产形态：不使用 mock-images）
    const defaultTeaImage = ''
    
    // 初始化
    onMounted(() => {
      if (!orderId) {
        orderPromptMessages.showOrderNotFound()
        goBack()
        return
      }
      
      loadOrderDetail()
    })
    return {
      loading,
      orderDetail,
      address,
      logistics,
      refundInfo,
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
      defaultTeaImage,
      canRequestRefund,
      canReview,
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