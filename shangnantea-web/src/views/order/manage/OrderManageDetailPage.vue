<template>
  <div class="order-manage-detail-page" v-loading="loading">
    <el-card class="detail-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <div class="left">
            <el-button icon="ArrowLeft" @click="goBack" text>返回订单管理</el-button>
            <span class="title">订单详情（管理端）</span>
          </div>
          <div class="status">
            <span
              v-if="orderDetail"
              :class="['status-tag', getStatusClass(orderDetail.status)]"
            >
              {{ getStatusText(orderDetail.status) }}
            </span>
            <span
              v-else
              class="status-tag status-unpaid"
            >
              加载中...
            </span>
          </div>
        </div>
      </template>

        <div v-if="orderDetail" class="order-detail-content">
          <!-- 订单信息 -->
          <div class="detail-section">
            <div class="section-title">订单信息</div>
            <div class="info-item">
              <span class="label">订单号：</span>
              <span class="value">{{ orderDetail.id }}</span>
            </div>
            <div class="info-item buyer-info">
              <span class="label">买家信息：</span>
              <div class="buyer-content" @click="goToUserProfile(orderDetail.userId)">
                <SafeImage
                  :src="orderDetail.userAvatar || orderDetail.avatar || ''"
                  type="avatar"
                  :alt="orderDetail.userNickname || orderDetail.nickname || '用户'"
                  class="buyer-avatar"
                />
                <span class="buyer-nickname">{{ orderDetail.userNickname || orderDetail.nickname || `用户${orderDetail.userId}` }}</span>
              </div>
            </div>
            <div class="info-item">
              <span class="label">下单时间：</span>
              <span class="value">{{ formatTime(orderDetail.createTime) }}</span>
            </div>
            <div class="info-item">
              <span class="label">支付方式：</span>
              <span class="value">{{ getPaymentMethodText(orderDetail.paymentMethod) }}</span>
            </div>
            <div class="info-item" v-if="orderDetail.status >= 2 && orderDetail.paymentTime">
              <span class="label">付款时间：</span>
              <span class="value">{{ formatTime(orderDetail.paymentTime) }}</span>
            </div>
          </div>

          <!-- 收货信息 -->
          <div class="detail-section">
            <div class="section-title">收货信息</div>
            <div class="info-item" v-if="orderDetail.address && orderDetail.address.receiverName">
              <span class="label">收货人：</span>
              <span class="value">{{ orderDetail.address.receiverName }}</span>
            </div>
            <div class="info-item" v-if="orderDetail.address && orderDetail.address.receiverPhone">
              <span class="label">联系电话：</span>
              <span class="value">{{ orderDetail.address.receiverPhone }}</span>
            </div>
            <div class="info-item" v-if="orderDetail.address">
              <span class="label">收货地址：</span>
              <span class="value">{{ formatAddressDetail(orderDetail.address) }}</span>
            </div>
          </div>

          <!-- 买家留言 -->
          <div class="detail-section" v-if="orderDetail.buyerMessage">
            <div class="section-title">买家留言</div>
            <div class="info-item">
              <span class="value">{{ orderDetail.buyerMessage }}</span>
            </div>
          </div>

          <!-- 商品信息 -->
          <div class="detail-section">
            <div class="section-title">商品信息</div>
            <div class="products-list">
              <div class="product-item" @click="viewTeaDetail(orderDetail.teaId)">
                <div class="product-image">
                  <SafeImage
                    :src="orderDetail.teaImage"
                    type="tea"
                    :alt="orderDetail.teaName"
                    style="width:80px;height:80px;object-fit:cover;"
                  />
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
              <span class="value">¥{{ (orderDetail.price * orderDetail.quantity).toFixed(2) }}</span>
            </div>
            <div class="amount-item">
              <span class="label">运费：</span>
              <span class="value">¥0.00</span>
            </div>
            <div class="amount-item total">
              <span class="label">实付金额：</span>
              <span class="value">¥{{ (orderDetail.totalPrice || 0).toFixed(2) }}</span>
            </div>
          </div>

          <!-- 物流信息 (仅在待收货和已完成状态显示) -->
          <div v-if="orderDetail.status === 2 || orderDetail.status === 3" class="detail-section">
            <div class="section-title">物流信息</div>
            <div class="info-item">
              <span class="label">物流公司：</span>
              <span class="value">{{ orderDetail.logisticsCompany || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="label">物流单号：</span>
              <span class="value">{{ orderDetail.logisticsNumber || '-' }}</span>
            </div>
            <div class="info-item" v-if="orderDetail.shippingTime">
              <span class="label">发货时间：</span>
              <span class="value">{{ formatTime(orderDetail.shippingTime) }}</span>
            </div>
          </div>

          <!-- 操作按钮区域 -->
          <div class="detail-section action-section">
            <!-- 待发货(1)：发货 -->
            <el-button 
              v-if="Number(orderDetail.status) === 1" 
              type="primary" 
              @click="openShipDialog"
            >
              发货
            </el-button>
            <!-- 待收货(2) / 已完成(3)：查看物流 -->
            <el-button 
              v-else-if="Number(orderDetail.status) === 2 || Number(orderDetail.status) === 3" 
              type="info" 
              @click="viewLogistics"
            >
              查看物流
            </el-button>
            <!-- 退款中(5)：同意退款 / 拒绝退款 / 查看退款进度 -->
            <template v-else-if="Number(orderDetail.status) === 5">
              <el-button type="success" @click="openProcessRefundDialog">
                同意退款 / 拒绝退款
              </el-button>
              <el-button type="info" @click="viewRefundDetail">
                查看退款进度
              </el-button>
            </template>
            <!-- 已退款(6)：查看退款详情 -->
            <el-button 
              v-else-if="Number(orderDetail.status) === 6" 
              type="info" 
              @click="viewRefundDetail"
            >
              查看退款详情
            </el-button>
          </div>
        </div>

        <div v-else-if="!loading" class="empty-detail">
          <el-empty description="订单不存在或已被删除" />
          <el-button @click="goBack">返回订单管理</el-button>
        </div>
      </el-card>

    <!-- 卖家发货对话框 -->
    <el-dialog
      v-model="shipDialogVisible"
      title="订单发货"
      width="480px"
      :close-on-click-modal="false"
    >
      <el-form label-width="90px">
        <el-form-item label="物流公司">
          <el-input v-model="shipForm.company" placeholder="例如：顺丰速运" />
        </el-form-item>
        <el-form-item label="快递单号">
          <el-input v-model="shipForm.trackingNumber" placeholder="请输入快递单号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shipDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="shipSubmitting" @click="confirmShip">
          确认发货
        </el-button>
      </template>
    </el-dialog>

    <!-- 卖家处理退款对话框 -->
    <el-dialog
      v-model="processRefundDialogVisible"
      title="处理退款申请"
      width="520px"
      :close-on-click-modal="false"
    >
      <div class="refund-process-info">
        <p>申请原因：{{ refundProcessInfo?.reason || orderDetail?.refundReason || '无' }}</p>
        <p>申请时间：{{ refundProcessInfo?.applyTime || orderDetail?.refundApplyTime || '无' }}</p>
      </div>
      <el-form label-width="80px" style="margin-top: 10px;">
        <el-form-item label="拒绝理由">
          <el-input
            v-model="rejectReason"
            type="textarea"
            :rows="3"
            placeholder="同意退款可不填，拒绝退款时必须填写理由"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="processRefundDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="processRefundSubmitting" @click="submitProcessRefund(false)">
          拒绝退款
        </el-button>
        <el-button type="success" :loading="processRefundSubmitting" @click="submitProcessRefund(true)">
          同意退款
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useOrderStore } from '@/stores/order'
import SafeImage from '@/components/common/form/SafeImage.vue'
import { showByCode } from '@/utils/apiMessages'
import { ElMessageBox } from 'element-plus'
import { orderPromptMessages } from '@/utils/promptMessages'

const router = useRouter()
const route = useRoute()
const orderStore = useOrderStore()

const loading = ref(false)
const orderDetail = ref(null)
const orderId = route.params.id

// 发货对话框
const shipDialogVisible = ref(false)
const shipSubmitting = ref(false)
const shipForm = reactive({
  company: '',
  trackingNumber: ''
})

// 处理退款对话框
const processRefundDialogVisible = ref(false)
const processRefundSubmitting = ref(false)
const refundProcessInfo = ref(null)
const rejectReason = ref('')

const getStatusText = status => {
  const map = {
    0: '待付款',
    1: '待发货',
    2: '待收货',
    3: '已完成',
    4: '已取消',
    5: '退款中',
    6: '已退款'
  }
  return map[status] || '未知状态'
}

const getStatusClass = status => {
  const classMap = {
    0: 'status-unpaid',
    1: 'status-unshipped',
    2: 'status-shipped',
    3: 'status-completed',
    4: 'status-cancelled',
    5: 'status-refunding',
    6: 'status-refunded'
  }
  return classMap[status] || ''
}

const getPaymentMethodText = method => {
  if (!method) return '未设置'
  const map = {
    alipay: '支付宝',
    wechat: '微信支付',
    unionpay: '银联支付',
    '1': '支付宝',
    '2': '微信支付',
    '3': '银联支付'
  }
  return map[method] || method
}

// 格式化地址详情（仅省市区+详细地址，不含收货人信息）
const formatAddressDetail = address => {
  if (!address || typeof address !== 'object') return '--'
  const parts = []
  if (address.province) parts.push(address.province)
  if (address.city) parts.push(address.city)
  if (address.district) parts.push(address.district)
  if (address.detailAddress) parts.push(address.detailAddress)
  return parts.length > 0 ? parts.join(' ') : '--'
}

const formatTime = time => {
  if (!time) return '--'
  const d = new Date(time)
  const pad = n => (n < 10 ? `0${n}` : n)
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(
    d.getHours()
  )}:${pad(d.getMinutes())}`
}

const loadDetail = async () => {
  if (!orderId) return
  loading.value = true
  try {
    const res = await orderStore.fetchOrderDetail(orderId)
    showByCode(res?.code)
    const data = res?.data || res
    orderDetail.value = data
    // 调试：打印地址信息
    console.log('订单详情数据:', data)
    console.log('地址信息:', data?.address)
  } catch (e) {
    console.error('加载订单详情失败:', e)
    orderDetail.value = null
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.push('/order/manage')
}

const viewTeaDetail = teaId => {
  router.push(`/tea/${teaId}`)
}

const goToUserProfile = userId => {
  router.push(`/user/${userId}`)
}

// 打开发货对话框
const openShipDialog = () => {
  if (!orderDetail.value) return
  shipForm.company = ''
  shipForm.trackingNumber = ''
  shipDialogVisible.value = true
}

// 确认发货
const confirmShip = async () => {
  if (!orderDetail.value) return
  if (!shipForm.company) {
    orderPromptMessages.showLogisticsCompanyRequired && orderPromptMessages.showLogisticsCompanyRequired()
    return
  }
  if (!shipForm.trackingNumber) {
    orderPromptMessages.showLogisticsNumberRequired && orderPromptMessages.showLogisticsNumberRequired()
    return
  }

  shipSubmitting.value = true
  try {
    const res = await orderStore.shipOrder({
      id: orderDetail.value.id,
      logisticsCompany: shipForm.company,
      logisticsNumber: shipForm.trackingNumber
    })
    showByCode(res?.code)
    shipDialogVisible.value = false
    await loadDetail()
  } catch (error) {
    console.error('发货失败:', error)
  } finally {
    shipSubmitting.value = false
  }
}

// 查看物流
const viewLogistics = async () => {
  try {
    const res = await orderStore.fetchOrderLogistics(orderId)
    showByCode(res?.code)
    const data = res?.data || res
    if (data) {
      ElMessageBox.alert(
        `物流公司：${data.company || orderDetail.value?.logisticsCompany || '--'}\n物流单号：${data.tracking_number || orderDetail.value?.logisticsNumber || '--'}\n发货时间：${data.ship_time || orderDetail.value?.shippingTime || '--'}`,
        '物流信息',
        { confirmButtonText: '我知道了' }
      )
    } else {
      orderPromptMessages.showNoLogisticsInfo && orderPromptMessages.showNoLogisticsInfo()
    }
  } catch (error) {
    console.error('获取物流信息失败:', error)
  }
}

// 打开处理退款对话框
const openProcessRefundDialog = async () => {
  if (!orderDetail.value) return
  processRefundDialogVisible.value = true
  rejectReason.value = ''
  refundProcessInfo.value = null
  try {
    const res = await orderStore.fetchRefundDetail(orderId)
    showByCode(res?.code)
    refundProcessInfo.value = res?.data || res
  } catch (error) {
    console.error('获取退款详情失败:', error)
  }
}

// 提交退款审批
const submitProcessRefund = async approve => {
  if (!orderDetail.value) return
  if (!approve && !rejectReason.value.trim()) {
    showByCode(5147, '请填写拒绝退款的理由')
    return
  }
  processRefundSubmitting.value = true
  try {
    const res = await orderStore.processRefund({
      orderId,
      approve,
      reason: approve
        ? (refundProcessInfo.value?.reason || '同意退款')
        : rejectReason.value.trim()
    })
    showByCode(res?.code)
    processRefundDialogVisible.value = false
    await loadDetail()
  } catch (error) {
    console.error('处理退款失败:', error)
  } finally {
    processRefundSubmitting.value = false
  }
}

// 查看退款详情
const viewRefundDetail = async () => {
  if (!orderDetail.value) {
    orderPromptMessages.showNoRefundInfo && orderPromptMessages.showNoRefundInfo()
    return
  }
  try {
    const res = await orderStore.fetchRefundDetail(orderId)
    showByCode(res?.code)
    const data = res?.data || res
    if (!data) {
      orderPromptMessages.showNoRefundInfo && orderPromptMessages.showNoRefundInfo()
      return
    }
    const statusTextMap = {
      1: '退款申请中',
      2: '退款已同意',
      3: '退款已拒绝'
    }
    const status = data.status ?? orderDetail.value.refundStatus
    const statusText = statusTextMap[status] || '退款处理中'
    const reason = data.reason || orderDetail.value.refundReason || '无'
    const rejectReason = data.rejectReason || orderDetail.value.refundRejectReason || '无'
    const applyTime = data.applyTime || orderDetail.value.refundApplyTime
    const processTime = data.processTime || orderDetail.value.refundProcessTime
    ElMessageBox.alert(
      `状态：${statusText}\n申请原因：${reason}\n拒绝原因：${rejectReason}\n申请时间：${applyTime || '无'}\n处理时间：${processTime || '无'}`,
      '退款详情',
      { confirmButtonText: '我知道了' }
    )
  } catch (error) {
    console.error('获取退款详情失败:', error)
    orderPromptMessages.showNoRefundInfo && orderPromptMessages.showNoRefundInfo()
  }
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped lang="scss">
.order-manage-detail-page {
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
    
    .status-refunding {
      color: #e6a23c;
      background-color: rgba(230, 162, 60, 0.1);
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
    
    &:hover {
      background-color: #f8f9fa;
    }
    
    .product-image {
      width: 80px;
      height: 80px;
      margin-right: 15px;
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

.buyer-info {
  .buyer-content {
    display: flex;
    align-items: center;
    gap: 10px;
    cursor: pointer;
    transition: opacity 0.3s;
    
    &:hover {
      opacity: 0.8;
    }
    
    .buyer-avatar {
      width: 32px;
      height: 32px;
      border-radius: 50%;
      object-fit: cover;
    }
    
    .buyer-nickname {
      color: #303133;
      font-weight: 500;
    }
  }
}

.refund-process-info {
  p {
    margin: 8px 0;
    color: #606266;
  }
}
</style>
