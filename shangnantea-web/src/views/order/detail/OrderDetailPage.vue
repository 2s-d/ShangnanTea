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
            <!-- 只有在成功获取到订单详情后才渲染真实状态，避免首次渲染访问 null -->
            <span
              v-if="orderDetail"
              :class="['status-tag', getStatusClass(orderDetail.status)]"
            >
              {{ getStatusText(orderDetail.status) }}
            </span>
            <!-- 数据尚未加载完成时给出轻量占位，避免页面抖动 -->
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
        <!-- 订单基本信息 -->
        <div class="detail-section">
          <div class="section-title">订单信息</div>
          <div class="info-item">
            <span class="label">订单号：</span>
            <span class="value">{{ orderDetail.id }}</span>
          </div>
          <!-- 管理员/商户视角：显示买家信息 -->
          <div v-if="isSellerView" class="info-item">
            <span class="label">买家ID：</span>
            <span class="value">{{ orderDetail.userId }}</span>
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
        <div v-if="orderDetail.refundStatus && orderDetail.refundStatus > 0" class="detail-section">
          <div class="section-title">退款信息</div>
          <div class="info-item">
            <span class="label">退款状态：</span>
            <span class="value">
              <el-tag v-if="orderDetail.refundStatus === 1" type="warning">申请中</el-tag>
              <el-tag v-else-if="orderDetail.refundStatus === 2" type="success">已同意</el-tag>
              <el-tag v-else-if="orderDetail.refundStatus === 3" type="danger">已拒绝</el-tag>
              <span v-else>未知状态</span>
            </span>
          </div>
          <div class="info-item" v-if="orderDetail.refundReason">
            <span class="label">退款原因：</span>
            <span class="value">{{ orderDetail.refundReason }}</span>
          </div>
          <div class="info-item" v-if="orderDetail.refundRejectReason">
            <span class="label">拒绝原因：</span>
            <span class="value">{{ orderDetail.refundRejectReason }}</span>
          </div>
          <div class="info-item" v-if="orderDetail.refundApplyTime">
            <span class="label">申请时间：</span>
            <span class="value">{{ formatTime(orderDetail.refundApplyTime) }}</span>
          </div>
          <div class="info-item" v-if="orderDetail.refundProcessTime">
            <span class="label">处理时间：</span>
            <span class="value">{{ formatTime(orderDetail.refundProcessTime) }}</span>
          </div>
        </div>
        
        <!-- 取消信息 -->
        <div v-if="orderDetail.status === 4 && orderDetail.cancelTime" class="detail-section">
          <div class="section-title">取消信息</div>
          <div class="info-item">
            <span class="label">取消时间：</span>
            <span class="value">{{ formatTime(orderDetail.cancelTime) }}</span>
          </div>
          <div class="info-item" v-if="orderDetail.cancelReason">
            <span class="label">取消原因：</span>
            <span class="value">{{ orderDetail.cancelReason }}</span>
          </div>
        </div>
        
        <!-- 支付状态信息（仅在待付款状态显示） -->
        <div v-if="orderDetail.status === 0 && orderDetail.paymentStatus !== null" class="detail-section">
          <div class="section-title">支付状态</div>
          <div class="info-item">
            <span class="label">支付单状态：</span>
            <span class="value">
              <el-tag v-if="orderDetail.paymentStatus === 0" type="warning">待支付</el-tag>
              <el-tag v-else-if="orderDetail.paymentStatus === 1" type="success">支付成功</el-tag>
              <el-tag v-else-if="orderDetail.paymentStatus === 2" type="danger">支付失败</el-tag>
              <el-tag v-else-if="orderDetail.paymentStatus === 3" type="info">已关闭</el-tag>
              <span v-else>未知状态</span>
            </span>
          </div>
        </div>
        
        <!-- 操作按钮区域 -->
        <div class="detail-section action-section">
          <!-- 用户视角：买家操作 -->
          <template v-if="!isSellerView">
            <!-- 待付款 -->
            <template v-if="orderDetail.status === 0">
              <el-button type="primary" @click="continuePay">立即支付</el-button>
              <el-button type="default" @click="cancelOrder">取消订单</el-button>
            </template>
            <!-- 待发货：允许修改地址 -->
            <template v-else-if="orderDetail.status === 1">
              <el-button type="primary" @click="openAddressDialog">修改收货地址</el-button>
            </template>
            <!-- 待收货 -->
            <template v-else-if="orderDetail.status === 2">
              <el-button type="primary" @click="viewLogistics">查看详细物流</el-button>
              <el-button type="success" @click="confirmReceipt">确认收货</el-button>
            </template>
            <!-- 已完成且未评价 -->
            <template v-else-if="canReview">
              <el-button type="primary" @click="writeReview">待评价</el-button>
              <el-button type="info" @click="viewLogistics">查看物流</el-button>
            </template>
            <!-- 已完成且已评价：仅查看物流 -->
            <template v-else-if="orderDetail.status === 3 && isReviewed">
              <el-button type="success" plain disabled>已评价</el-button>
              <el-button type="info" @click="viewLogistics">查看物流</el-button>
            </template>
            <!-- 退款中 -->
            <template v-else-if="orderDetail.status === 5">
              <el-button type="info" @click="viewRefundDetail">查看退款进度</el-button>
            </template>
            <!-- 已退款 -->
            <template v-else-if="orderDetail.status === 6">
              <el-button type="info" @click="viewRefundDetail">查看退款详情</el-button>
            </template>
            <!-- 退款相关 -->
            <el-button
              v-if="canRequestRefund"
              type="warning"
              @click="openRefundDialog"
            >
              申请退款
            </el-button>
          </template>
          
          <!-- 管理员/商户视角：卖家操作 -->
          <template v-else>
            <!-- 待发货状态：显示发货按钮 -->
            <el-button
              v-if="orderDetail.status === 1"
              type="primary"
              @click="handleShipOrder"
            >
              发货
            </el-button>
            <!-- 已发货/待收货状态：显示物流信息查看 -->
            <el-button
              v-if="orderDetail.status === 2 || orderDetail.status === 3"
              type="info"
              @click="viewLogistics"
            >
              查看物流
            </el-button>
            <!-- 有退款申请时：显示处理退款按钮 -->
            <el-button
              v-if="orderDetail.refundStatus === 1"
              type="warning"
              @click="handleProcessRefund"
            >
              处理退款
            </el-button>
          </template>
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
    
    <!-- 修改收货地址对话框（可编辑表单 + 地址簿选择） -->
    <el-dialog
      v-model="addressDialogVisible"
      title="修改收货地址"
      width="600px"
      :close-on-click-modal="false"
      destroy-on-close
      class="address-edit-dialog-wrapper"
    >
      <div class="address-edit-dialog">
        <!-- 当前订单地址编辑表单 -->
        <div class="address-form-section">
          <div class="section-title">编辑地址信息</div>
          <el-form 
            :model="editAddressForm" 
            label-width="80px"
            ref="editAddressFormRef"
          >
            <el-form-item label="收货人">
              <el-input v-model="editAddressForm.receiverName" placeholder="请输入收货人姓名" />
            </el-form-item>
            
            <el-form-item label="手机号">
              <el-input v-model="editAddressForm.receiverPhone" placeholder="请输入手机号码" />
            </el-form-item>
            
            <el-form-item label="所在地区">
              <el-cascader 
                v-model="editAddressForm.region" 
                :options="cascaderOptions" 
                placeholder="请选择省/市/区"
                :props="{
                  expandTrigger: 'hover',
                  checkStrictly: false
                }"
                style="width: 100%"
                @change="handleRegionChange"
              />
            </el-form-item>
            
            <el-form-item label="详细地址">
              <el-input 
                v-model="editAddressForm.detailAddress" 
                type="textarea" 
                placeholder="街道、门牌号等"
                :rows="3"
              />
            </el-form-item>
          </el-form>
        </div>
        
        <!-- 地址簿列表（可选择填充） -->
        <div class="address-book-section">
          <div class="section-title">从地址簿选择</div>
          <div class="address-list-container">
            <div 
              v-for="addr in userAddresses" 
              :key="addr.id"
              class="address-item"
              :class="{ 'selected': selectedAddressId === addr.id }"
              @click="fillAddressFromBook(addr)"
            >
              <div class="address-content">
                <div class="address-header">
                  <span class="receiver-name">{{ addr.receiverName || addr.name }}</span>
                  <span class="phone">{{ addr.receiverPhone || addr.phone }}</span>
                  <el-tag v-if="addr.isDefault" type="success" size="small">默认</el-tag>
                </div>
                <div class="address-detail">
                  {{ addr.province }}{{ addr.city }}{{ addr.district }} {{ addr.detailAddress || addr.detail }}
                </div>
              </div>
              <el-icon class="select-icon" v-if="selectedAddressId === addr.id"><Check /></el-icon>
            </div>
            <el-empty v-if="userAddresses.length === 0" description="暂无收货地址" :image-size="60" />
          </div>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="addressDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="addressSubmitting" @click="submitAddressChange">
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useOrderStore } from '@/stores/order'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'
import { Check } from '@element-plus/icons-vue'
import { showByCode } from '@/utils/apiMessages'
import { orderPromptMessages } from '@/utils/promptMessages'
import { regionData } from '@/utils/region'
import SafeImage from '@/components/common/form/SafeImage.vue'

defineOptions({
  name: 'OrderDetailPage'
})

const router = useRouter()
const route = useRoute()
const orderStore = useOrderStore()
const userStore = useUserStore()
const loading = computed(() => orderStore.loading)

// 判断当前页面是管理端还是用户端
const isManagePage = computed(() => route.path.includes('/order/manage/detail'))
// 判断当前用户角色
const userRole = computed(() => userStore.userInfo?.role || 0)
const isAdmin = computed(() => userRole.value === 1)
const isShop = computed(() => userRole.value === 3)
const isUser = computed(() => userRole.value === 2)
// 是否为卖家视角（管理员或商户）
const isSellerView = computed(() => isManagePage.value && (isAdmin.value || isShop.value))

// 从路由参数获取订单ID
const orderId = route.params.id

/**
 * 生产形态：不在 UI 层造假数据；通过 Pinia -> API -> 后端返回数据
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

// 退款信息现在直接从 orderDetail 中读取，不再需要单独的 refundInfo

// 修改订单地址对话框
const addressDialogVisible = ref(false)
const addressSubmitting = ref(false)
const selectedAddressId = ref(null)
const userAddresses = computed(() => (userStore.addressList || []).filter(addr => addr && addr.id))
const editAddressFormRef = ref(null)

// 地址编辑表单数据
const editAddressForm = ref({
  receiverName: '',
  receiverPhone: '',
  region: [],
  detailAddress: ''
})

// 初始表单值（用于判断是否修改）
const initialAddressForm = ref({
  receiverName: '',
  receiverPhone: '',
  region: [],
  detailAddress: ''
})

// 地区级联选择器数据
const cascaderOptions = ref(regionData || [])

// 打开修改地址对话框时，隐藏整页滚动条（只保留对话框内部滚动），不改对话框本身
watch(addressDialogVisible, (visible) => {
  if (typeof window === 'undefined') return
  const body = document.body
  const html = document.documentElement
  if (body) {
    body.style.overflowY = visible ? 'hidden' : ''
  }
  if (html) {
    html.style.overflowY = visible ? 'hidden' : ''
  }
})
    
    // 获取订单状态文本
    const getStatusText = status => {
      const statusMap = {
        0: '待付款',
        1: '待发货',
        2: '待收货',
        3: '已完成',
        4: '已取消',
        5: '退款中',
        6: '已退款'
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
        5: 'status-refunding',
        6: 'status-refunded'
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
      // 待付款且没有支付方式时，显示“待选择”
      if (!method && orderDetail.value && orderDetail.value.status === 0) {
        return '待选择'
      }
      if (!method) return '未设置'
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
      if (isManagePage.value) {
        router.push('/order/manage')
      } else {
        router.push('/order/list')
      }
    }
    
    // 打开修改收货地址对话框
    const openAddressDialog = async () => {
      // 确保用户地址列表已加载
      if (!userStore.addressList || userStore.addressList.length === 0) {
        try {
          await userStore.fetchAddresses()
        } catch (error) {
          console.error('获取地址列表失败:', error)
        }
      }
      
      // 初始化表单数据为当前订单地址
      const currentAddr = address.value
      const initialRegion = []
      
      // 根据省市区设置级联选择器的值
      if (currentAddr.province && currentAddr.city && currentAddr.district) {
        const province = cascaderOptions.value.find(p => p.label === currentAddr.province)
        if (province) {
          const city = province.children?.find(c => c.label === currentAddr.city)
          if (city) {
            const district = city.children?.find(d => d.label === currentAddr.district)
            if (district) {
              initialRegion.push(province.value, city.value, district.value)
            }
          }
        }
      }
      
      editAddressForm.value = {
        receiverName: currentAddr.name || '',
        receiverPhone: currentAddr.phone || '',
        region: initialRegion,
        detailAddress: currentAddr.detail || ''
      }
      
      // 保存初始值（深拷贝）
      initialAddressForm.value = {
        receiverName: currentAddr.name || '',
        receiverPhone: currentAddr.phone || '',
        region: [...initialRegion],
        detailAddress: currentAddr.detail || ''
      }
      
      selectedAddressId.value = null
      addressDialogVisible.value = true
    }
    
    // 从地址簿填充表单
    const fillAddressFromBook = (addr) => {
      selectedAddressId.value = addr.id
      editAddressForm.value.receiverName = addr.receiverName || addr.name || ''
      editAddressForm.value.receiverPhone = addr.receiverPhone || addr.phone || ''
      editAddressForm.value.detailAddress = addr.detailAddress || addr.detail || ''
      
      // 设置地区级联选择器的值
      if (addr.province && addr.city && addr.district) {
        const province = cascaderOptions.value.find(p => p.label === addr.province)
        if (province) {
          const city = province.children?.find(c => c.label === addr.city)
          if (city) {
            const district = city.children?.find(d => d.label === addr.district)
            if (district) {
              editAddressForm.value.region = [province.value, city.value, district.value]
            }
          }
        }
      } else {
        editAddressForm.value.region = []
      }
    }
    
    // 地区选择变化处理
    const handleRegionChange = (value) => {
      // 级联选择器值变化时，可以在这里做额外处理
      // 当前不需要额外处理
    }
    
    // 管理员/商户：发货操作
    const handleShipOrder = () => {
      router.push(`/order/manage/detail/${orderId}`)
      // 实际应该打开发货对话框，这里先跳转到管理页，由管理页处理发货
      // TODO: 如果管理端详情页有发货功能，应该在这里打开对话框
    }
    
    // 管理员/商户：处理退款操作
    const handleProcessRefund = () => {
      router.push(`/order/manage/detail/${orderId}`)
      // 实际应该打开退款处理对话框
      // TODO: 如果管理端详情页有退款处理功能，应该在这里打开对话框
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
    
    // 查看物流：调用 Pinia action 获取最新物流信息并刷新本地展示
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
    
    // 检查表单是否被修改
    const isFormModified = () => {
      return editAddressForm.value.receiverName !== initialAddressForm.value.receiverName ||
             editAddressForm.value.receiverPhone !== initialAddressForm.value.receiverPhone ||
             JSON.stringify(editAddressForm.value.region) !== JSON.stringify(initialAddressForm.value.region) ||
             editAddressForm.value.detailAddress !== initialAddressForm.value.detailAddress
    }
    
    // 提交修改订单地址
    const submitAddressChange = async () => {
      // 如果既没选择地址簿地址，也没修改表单，则保持原样（不提交）
      if (!selectedAddressId.value && !isFormModified()) {
        addressDialogVisible.value = false
        return
      }
      
      // 如果选择了地址簿地址，直接用这个ID（忽略表单内容）
      if (selectedAddressId.value) {
        addressSubmitting.value = true
        try {
          const res = await orderStore.updateOrderAddress({
            orderId,
            addressId: selectedAddressId.value
          })
          const code = res?.code
          if (code) {
            showByCode(code)
          }
          addressDialogVisible.value = false
          await loadOrderDetail()
        } catch (error) {
          showByCode(5147, error?.message || null)
        } finally {
          addressSubmitting.value = false
        }
        return
      }
      
      // 如果没有选择地址簿地址，但修改了表单，需要创建新地址
      // 验证表单数据
      if (!editAddressForm.value.receiverName || !editAddressForm.value.receiverPhone) {
        orderPromptMessages.showAddressRequired && orderPromptMessages.showAddressRequired()
        return
      }
      
      if (!editAddressForm.value.region || editAddressForm.value.region.length !== 3) {
        showByCode(5147, '请选择完整的省市区')
        return
      }
      
      if (!editAddressForm.value.detailAddress) {
        showByCode(5147, '请输入详细地址')
        return
      }
      
      addressSubmitting.value = true
      try {
        // 从级联选择器获取省市区名称
        const province = cascaderOptions.value.find(p => p.value === editAddressForm.value.region[0])
        const city = province?.children?.find(c => c.value === editAddressForm.value.region[1])
        const district = city?.children?.find(d => d.value === editAddressForm.value.region[2])
        
        const addressData = {
          receiverName: editAddressForm.value.receiverName,
          receiverPhone: editAddressForm.value.receiverPhone,
          province: province?.label || '',
          city: city?.label || '',
          district: district?.label || '',
          detailAddress: editAddressForm.value.detailAddress,
          isDefault: false
        }
        
        // 创建新地址
        const addRes = await userStore.addAddress(addressData)
        if (addRes && addRes.code !== 200) {
          showByCode(addRes.code)
          return
        }
        
        // 获取新创建的地址ID
        await userStore.fetchAddresses()
        const newAddress = userStore.addressList.find(addr => 
          addr && addr.receiverName === addressData.receiverName &&
          addr.receiverPhone === addressData.receiverPhone &&
          addr.province === addressData.province &&
          addr.city === addressData.city &&
          addr.district === addressData.district &&
          (addr.detailAddress || addr.detail) === addressData.detailAddress
        )
        
        if (!newAddress) {
          showByCode(5147, '无法获取新创建的地址ID')
          return
        }
        
        // 调用修改订单地址接口
        const res = await orderStore.updateOrderAddress({
          orderId,
          addressId: newAddress.id
        })
        const code = res?.code
        if (code) {
          showByCode(code)
        }
        addressDialogVisible.value = false
        await loadOrderDetail()
      } catch (error) {
        showByCode(5147, error?.message || null)
      } finally {
        addressSubmitting.value = false
      }
    }
    
    // 查看退款详情/进度：当前简单弹出提示，后续可跳转到专门页面
    const viewRefundDetail = () => {
      if (!orderDetail.value || !orderDetail.value.refundStatus || orderDetail.value.refundStatus === 0) {
        orderPromptMessages.showNoRefundInfo && orderPromptMessages.showNoRefundInfo()
        return
      }
      // 暂时直接弹出一个基础信息，后续可以换成对话框或独立页
      const statusTextMap = {
        1: '退款申请中',
        2: '退款已同意',
        3: '退款已拒绝'
      }
      const statusText = statusTextMap[orderDetail.value.refundStatus] || '退款处理中'
      const reason = orderDetail.value.refundReason || '无'
      const rejectReason = orderDetail.value.refundRejectReason || '无'
      const applyTime = orderDetail.value.refundApplyTime ? formatTime(orderDetail.value.refundApplyTime) : '无'
      const processTime = orderDetail.value.refundProcessTime ? formatTime(orderDetail.value.refundProcessTime) : '无'
      ElMessageBox.alert(
        `状态：${statusText}\n申请原因：${reason}\n拒绝原因：${rejectReason}\n申请时间：${applyTime}\n处理时间：${processTime}`,
        '退款详情',
        { confirmButtonText: '我知道了' }
      )
    }
    
    const canRequestRefund = computed(() => {
      if (!orderDetail.value) return false
      const status = orderDetail.value.status
      // 待付款、退款中、已退款都不允许申请退款
      if (status === 0 || status === 5 || status === 6) return false
      // 简单规则：待发货/待收货/已完成允许申请一次
      return status === 1 || status === 2 || status === 3
    })
    
    // 是否已评价
    const isReviewed = computed(() => {
      if (!orderDetail.value) return false
      const flag = orderDetail.value.isReviewed ?? orderDetail.value.buyerRate
      return flag === 1
    })
    
    // 是否可以评价（订单状态为已完成且未评价）
    const canReview = computed(() => {
      if (!orderDetail.value) return false
      return orderDetail.value.status === 3 && !isReviewed.value
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
        // 重新加载订单详情，退款信息会从 orderDetail 中获取
        await loadOrderDetail()
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
            name: addr.receiverName || addr.name || '',
            phone: addr.receiverPhone || addr.phone || '',
            province: addr.province || '',
            city: addr.city || '',
            district: addr.district || '',
            detail: addr.detailAddress || addr.detail || ''
          }
          // 物流信息：后端返回的是 logisticsCompany/logisticsNumber/shippingTime，需要映射
          logistics.value = {
            company: data?.logisticsCompany || data?.logistics?.company || '',
            tracking_number: data?.logisticsNumber || data?.logistics?.tracking_number || '',
            ship_time: data?.shippingTime || data?.logistics?.ship_time || '',
            traces: data?.logistics?.traces || []
          }
          // 退款信息现在直接从 orderDetail 中读取，不再需要单独调用 fetchRefundDetail
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

/* 修改地址对话框样式 */
.address-edit-dialog-wrapper :deep(.el-dialog__body) {
  max-height: 70vh;
  overflow-y: auto;
  padding: 20px;
}

.address-edit-dialog {
  display: flex;
  flex-direction: column;
  max-height: 70vh;
}

.address-edit-dialog .address-form-section {
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
  flex-shrink: 0;
}

.address-edit-dialog .address-form-section .section-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
}

.address-edit-dialog .address-book-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.address-edit-dialog .address-book-section .section-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
  flex-shrink: 0;
}

.address-edit-dialog .address-list-container {
  flex: 1;
  min-height: 0;
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 8px;
}

.address-edit-dialog .address-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  margin-bottom: 8px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.address-edit-dialog .address-item:hover {
  border-color: #409eff;
  background-color: #ecf5ff;
}

.address-edit-dialog .address-item.selected {
  border-color: #409eff;
  background-color: #ecf5ff;
}

.address-edit-dialog .address-item:last-child {
  margin-bottom: 0;
}

.address-edit-dialog .address-content {
  flex: 1;
}

.address-edit-dialog .address-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 6px;
}

.address-edit-dialog .receiver-name {
  font-weight: 500;
  color: #303133;
}

.address-edit-dialog .phone {
  color: #606266;
  font-size: 13px;
}

.address-edit-dialog .address-detail {
  color: #606266;
  font-size: 13px;
  line-height: 1.5;
}

.address-edit-dialog .select-icon {
  color: #409eff;
  font-size: 18px;
}
</style> 