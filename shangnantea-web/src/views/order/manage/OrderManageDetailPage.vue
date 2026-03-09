<template>
  <div class="order-manage-detail-page" v-loading="loading">
    <div class="container main-content">
      <el-card class="detail-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <div class="left">
              <el-button text type="primary" @click="goBack">
                返回订单管理
              </el-button>
              <span class="title">订单详情（管理端）</span>
            </div>
            <div class="status" v-if="orderDetail">
              <el-tag :type="getStatusType(orderDetail.status)" effect="light">
                {{ getStatusText(orderDetail.status) }}
              </el-tag>
            </div>
          </div>
        </template>

        <div v-if="orderDetail" class="detail-content">
          <!-- 订单信息 -->
          <div class="detail-section">
            <div class="section-title">订单信息</div>
            <div class="info-row">
              <span class="label">订单号：</span>
              <span class="value">{{ orderDetail.id }}</span>
            </div>
            <div class="info-row">
              <span class="label">下单时间：</span>
              <span class="value">{{ formatTime(orderDetail.createTime) }}</span>
            </div>
            <div class="info-row">
              <span class="label">订单状态：</span>
              <span class="value">{{ getStatusText(orderDetail.status) }}</span>
            </div>
            <div class="info-row">
              <span class="label">支付方式：</span>
              <span class="value">{{ getPaymentMethodText(orderDetail.paymentMethod) }}</span>
            </div>
            <div class="info-row" v-if="orderDetail.paymentTime">
              <span class="label">支付时间：</span>
              <span class="value">{{ formatTime(orderDetail.paymentTime) }}</span>
            </div>
            <div class="info-row" v-if="orderDetail.completionTime">
              <span class="label">完成时间：</span>
              <span class="value">{{ formatTime(orderDetail.completionTime) }}</span>
            </div>
          </div>

          <!-- 收货地址 -->
          <div class="detail-section" v-if="orderDetail.address && Object.keys(orderDetail.address).length > 0">
            <div class="section-title">收货信息</div>
            <div class="info-row">
              <span class="label">收货人：</span>
              <span class="value">{{ orderDetail.address.receiverName || '--' }}</span>
            </div>
            <div class="info-row">
              <span class="label">联系电话：</span>
              <span class="value">{{ orderDetail.address.receiverPhone || '--' }}</span>
            </div>
            <div class="info-row">
              <span class="label">收货地址：</span>
              <span class="value">{{ formatAddressDetail(orderDetail.address) }}</span>
            </div>
          </div>
          <div class="detail-section" v-else-if="orderDetail">
            <div class="section-title">收货信息</div>
            <div class="info-row">
              <span class="value" style="color: #909399;">暂无收货地址信息</span>
            </div>
          </div>

          <!-- 买家留言 -->
          <div class="detail-section" v-if="orderDetail.buyerMessage">
            <div class="section-title">买家留言</div>
            <div class="info-row">
              <span class="value">{{ orderDetail.buyerMessage }}</span>
            </div>
          </div>

          <!-- 商品信息 -->
          <div class="detail-section">
            <div class="section-title">商品信息</div>
            <div class="product-item">
              <div class="product-image">
                <SafeImage
                  :src="orderDetail.teaImage"
                  type="tea"
                  :alt="orderDetail.teaName"
                  style="width:80px;height:80px;object-fit:cover;"
                />
              </div>
              <div class="product-info">
                <div class="name">{{ orderDetail.teaName }}</div>
                <div class="spec">规格：{{ orderDetail.specName }}</div>
              </div>
              <div class="product-extra">
                <div>单价：¥{{ (orderDetail.price || 0).toFixed(2) }}</div>
                <div>数量：x{{ orderDetail.quantity }}</div>
                <div>小计：¥{{ ((orderDetail.price || 0) * (orderDetail.quantity || 0)).toFixed(2) }}</div>
              </div>
            </div>
          </div>

          <!-- 金额信息 -->
          <div class="detail-section">
            <div class="section-title">金额信息</div>
            <div class="info-row">
              <span class="label">实付金额：</span>
              <span class="value emphasis">¥{{ (orderDetail.totalPrice || 0).toFixed(2) }}</span>
            </div>
          </div>

          <!-- 物流信息 -->
          <div v-if="orderDetail.status === 2 || orderDetail.status === 3 || orderDetail.logisticsCompany" class="detail-section">
            <div class="section-title">物流信息</div>
            <div class="info-row">
              <span class="label">物流公司：</span>
              <span class="value">{{ orderDetail.logisticsCompany || '-' }}</span>
            </div>
            <div class="info-row">
              <span class="label">物流单号：</span>
              <span class="value">{{ orderDetail.logisticsNumber || '-' }}</span>
            </div>
            <div class="info-row" v-if="orderDetail.shippingTime">
              <span class="label">发货时间：</span>
              <span class="value">{{ formatTime(orderDetail.shippingTime) }}</span>
            </div>
          </div>
        </div>

        <div v-else-if="!loading" class="empty-detail">
          <el-empty description="订单不存在或已被删除" />
          <el-button @click="goBack">返回订单管理</el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useOrderStore } from '@/stores/order'
import SafeImage from '@/components/common/form/SafeImage.vue'
import { showByCode } from '@/utils/apiMessages'

const router = useRouter()
const route = useRoute()
const orderStore = useOrderStore()

const loading = ref(false)
const orderDetail = ref(null)
const orderId = route.params.id

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

const getStatusType = status => {
  const map = {
    0: 'warning',
    1: 'primary',
    2: 'success',
    3: 'info',
    4: 'danger',
    5: 'warning',
    6: 'info'
  }
  return map[status] || 'info'
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

onMounted(() => {
  loadDetail()
})
</script>

<style scoped lang="scss">
.order-manage-detail-page {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.container {
  width: 85%;
  max-width: 1920px;
  margin: 0 auto;
  padding: 20px 0;
}

.detail-card {
  background-color: #fff;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .left {
    display: flex;
    align-items: center;
    gap: 12px;

    .title {
      font-size: 18px;
      font-weight: 600;
    }
  }
}

.detail-content {
  padding: 10px 0 20px;
}

.detail-section {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;

  .section-title {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 12px;
  }

  .info-row {
    margin-bottom: 8px;

    .label {
      color: #909399;
      margin-right: 8px;
    }

    .value {
      color: #303133;

      &.emphasis {
        font-size: 18px;
        font-weight: 600;
        color: #f56c6c;
      }
    }
  }
}

.product-item {
  display: flex;
  align-items: center;
  gap: 16px;

  .product-info {
    flex: 1;

    .name {
      font-size: 15px;
      font-weight: 500;
      margin-bottom: 4px;
    }

    .spec {
      font-size: 13px;
      color: #909399;
    }
  }

  .product-extra {
    text-align: right;
    font-size: 13px;
    color: #606266;

    > div + div {
      margin-top: 4px;
    }
  }
}

.empty-detail {
  text-align: center;
  padding: 40px 0;
}
</style>
