<template>
  <div class="order-list-page">
    <div class="container main-content">
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
            :key="order.id"
            class="order-item"
          >
            <div class="order-header">
              <div class="order-info">
                <span class="order-time">{{ formatTime(order.createTime) }}</span>
                <span class="order-id">订单号：{{ order.id }}</span>
                <span v-if="order.status === 0 && order.paymentId" class="payment-id-hint">
                  所属支付单：{{ order.paymentId }}
                </span>
              </div>
              <div class="order-status">
                <span :class="['status-tag', getStatusClass(order.status)]">
                  {{ getStatusText(order.status) }}
                </span>
              </div>
            </div>

            <div class="order-content">
              <div
                class="order-product"
                @click="viewOrderDetail(order.id)"
              >
                <div class="product-image">
                  <SafeImage :src="order.teaImage" type="tea" :alt="order.teaName" style="width:80px;height:80px;object-fit:cover;" />
                </div>
                <div class="product-info">
                  <div class="product-name">{{ order.teaName }}</div>
                  <div class="product-spec">规格：{{ order.specName }}</div>
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
                <span class="total-price">¥{{ order.totalPrice }}</span>
                <span class="item-count">(数量：{{ order.quantity }})</span>
              </div>
              <div class="order-actions">
                <template v-if="order.status === 0">
                  <!-- 待付款 -->
                  <el-button type="primary" size="small" @click="continuePay(order)">
                    去支付
                  </el-button>
                  <el-button size="small" @click="cancelOrder(order.id)">
                    取消订单
                  </el-button>
                  <el-button size="small" @click="contactShop(order.shopId)">
                    联系商家
                  </el-button>
                </template>
                <template v-else-if="order.status === 1">
                  <!-- 待发货 -->
                  <el-button type="primary" size="small" @click="openRefundDialog(order.id)">
                    申请退款
                  </el-button>
                  <el-button size="small" @click="modifyAddress(order.id)">
                    修改地址
                  </el-button>
                  <el-button size="small" @click="contactShop(order.shopId)">
                    联系商家
                  </el-button>
                </template>
                <template v-else-if="order.status === 2">
                  <!-- 待收货 -->
                  <el-button type="primary" size="small" @click="confirmReceipt(order.id)">
                    确认收货
                  </el-button>
                  <el-button size="small" @click="viewLogistics(order.id)">
                    查看物流
                  </el-button>
                  <el-button size="small" @click="contactShop(order.shopId)">
                    联系商家
                  </el-button>
                </template>
                <template v-else-if="order.status === 3">
                  <!-- 已完成 -->
                  <el-button v-if="!order.isReviewed || order.isReviewed === 0" type="primary" size="small" @click="writeReview(order.id)">
                    评价
                  </el-button>
                  <el-button v-else type="info" size="small" plain disabled>
                    已评价
                  </el-button>
                  <el-button size="small" @click="deleteOrder(order.id)">
                    删除订单
                  </el-button>
                  <el-button size="small" @click="contactShop(order.shopId)">
                    联系商家
                  </el-button>
                </template>
                <template v-else>
                  <!-- 已取消/已退款等状态 -->
                  <el-button size="small" @click="viewOrderDetail(order.id)">
                    查看详情
                  </el-button>
                  <el-button size="small" @click="contactShop(order.shopId)">
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
          {{ addressSubmitText }}
        </el-button>
      </template>
    </el-dialog>

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
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useOrderStore } from '@/stores/order'
import { useUserStore } from '@/stores/user'
import { useMessageStore } from '@/stores/message'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Check } from '@element-plus/icons-vue'
import { regionData } from '@/utils/region'
import SafeImage from '@/components/common/form/SafeImage.vue'
import { showByCode, isSuccess } from '@/utils/apiMessages'
import { orderPromptMessages } from '@/utils/promptMessages'

// TODO: 退款申请功能需后端提供接口与数据结构支持（前端不在UI层模拟退款流程）

defineOptions({
  name: 'OrderListPage'
})

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
const orderStore = useOrderStore()
const userStore = useUserStore()
const messageStore = useMessageStore()
const router = useRouter()
const searchText = ref('')
const activeTab = ref('all')
const currentPage = computed({
  get: () => orderStore.pagination.currentPage,
  set: val => {
    orderStore.pagination.currentPage = val
  }
})

// 退款相关（生产版：只负责收集输入并请求后端）
const refundDialogVisible = ref(false)
const refundSubmitting = ref(false)
const refundReason = ref('')
const refundOrderId = ref('')

// 修改地址对话框相关
const addressDialogVisible = ref(false)
const addressSubmitting = ref(false)
const selectedAddressId = ref(null)
const currentOrderId = ref(null)
// 新增地址创建阶段 & ID：用于两步流程（先创建地址，再更新订单）
const addressActionStage = ref('initial') // 'initial' | 'created'
const createdAddressId = ref(null)
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

const loading = computed(() => orderStore.loading)
const pageSize = computed({
  get: () => orderStore.pagination.pageSize,
  set: val => {
    orderStore.pagination.pageSize = val
  }
})
const total = computed(() => orderStore.pagination.total)
const orders = computed(() => orderStore.orderList || [])

// 列表直接来自 Pinia，筛选交给后端
const filteredOrders = computed(() => orders.value)

    const openRefundDialog = orderId => {
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
        const { code } = await orderStore.applyRefund({
          orderId: refundOrderId.value,
          reason: refundReason.value.trim()
        })
        showByCode(code)
        refundDialogVisible.value = false
      } catch (e) {
        showByCode(4130) // 退款申请提交失败
      } finally {
        refundSubmitting.value = false
      }
    }
    
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
    
    // 格式化时间
    const formatTime = timeStr => {
      const date = new Date(timeStr)
      return `${date.getFullYear()}-${padZero(date.getMonth() + 1)}-${padZero(date.getDate())} ${padZero(date.getHours())}:${padZero(date.getMinutes())}`
    }
    
    // 数字补零
    const padZero = num => {
      return num < 10 ? `0${num}` : num
    }
    
    // 处理搜索
    const handleSearch = () => {
      currentPage.value = 1 // 重置到第一页
      orderStore.updateFilters({
        keyword: searchText.value
      })
      orderStore.fetchOrders({
        page: currentPage.value,
        size: pageSize.value
      })
    }
    
    // 处理分页变化
    const handleCurrentChange = page => {
      orderStore.setPage({ page })
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
      orderStore.updateFilters({
        status
      })
      orderStore.fetchOrders({
        page: currentPage.value,
        size: pageSize.value
      })
    }
    
    // 查看订单详情：跳转到订单详情页
    const viewOrderDetail = orderId => {
      router.push(`/order/detail/${orderId}`)
    }
    
    // 提交支付表单（自动跳转到支付宝）
    const submitAlipayForm = formHtml => {
      const div = document.createElement('div')
      div.innerHTML = formHtml
      document.body.appendChild(div)
      const form = div.querySelector('form')
      if (form) {
        form.submit()
      } else {
        console.error('支付表单格式错误')
        ElMessage.error('支付表单格式错误，请重试')
      }
    }
    
    // 继续支付：使用 paymentId 直接调用支付API，自动跳转到支付宝
    const continuePay = async order => {
      // order 必须是订单对象，包含 id 和 paymentId
      if (!order || !order.id) {
        ElMessage.error('订单信息异常，无法发起支付')
        return
      }
      
      const orderId = order.id
      const paymentId = order.paymentId
      
      if (!paymentId) {
        ElMessage.error('订单缺少支付单信息，无法发起支付')
        return
      }
      
      try {
        // 直接调用支付 API（优先使用 paymentId）
        const payRes = await orderStore.payOrder({
          paymentId: paymentId || undefined,
          orderId: orderId || undefined,
          paymentMethod: 'alipay' // 默认使用支付宝
        })
        
        // 检查支付接口返回的状态码
        if (payRes?.code === 5007) {
          // 5007 - 支付表单生成成功，正在跳转...
          showByCode(payRes.code)
          
          // 获取支付表单HTML并自动提交
          const formHtml = payRes?.data?.formHtml || payRes?.data
          if (formHtml) {
            submitAlipayForm(formHtml)
          } else {
            console.error('未返回支付表单')
            ElMessage.error('支付表单生成失败，请重试')
          }
        } else if (payRes?.code === 5006) {
          // 5006 - 订单已支付
          showByCode(payRes.code)
          // 重新加载订单列表，刷新状态
          await orderStore.fetchOrders({ page: currentPage.value, size: pageSize.value, keyword: searchText.value })
        } else {
          // 其他状态码（失败）
          if (payRes?.code) {
            showByCode(payRes.code)
          } else {
            ElMessage.error('支付发起失败，请重试')
          }
        }
      } catch (error) {
        console.error('发起支付失败:', error)
        ElMessage.error('支付发起失败，请重试')
      }
    }
    
    // 取消订单
    const cancelOrder = async orderId => {
      try {
        const { value } = await ElMessageBox.prompt('请输入取消原因（必填）', '取消订单', {
          confirmButtonText: '提交取消',
          cancelButtonText: '返回',
          inputPlaceholder: '例如：不想要了 / 地址填错了 / 重复下单',
          inputType: 'textarea',
          inputValidator: (val) => {
            if (!val || !String(val).trim()) return '取消原因不能为空'
            if (String(val).trim().length > 120) return '取消原因最多120字'
            return true
          }
        })

        const { code } = await orderStore.cancelOrder({ id: orderId, reason: String(value).trim() })
        showByCode(code)
        orderStore.fetchOrders({ page: currentPage.value, size: pageSize.value, keyword: searchText.value })
      } catch (error) {
        if (error !== 'cancel') {
          showByCode(4102) // 取消订单失败
        }
      }
    }
    
    // 确认收货
    const confirmReceipt = async orderId => {
      try {
        await ElMessageBox.confirm('确认已收到商品吗？', '提示', {
          confirmButtonText: '确认收货',
          cancelButtonText: '取消',
          type: 'info'
        })
      
        const { code } = await orderStore.confirmReceipt(orderId)
        showByCode(code)
        orderStore.fetchOrders({ page: currentPage.value, size: pageSize.value, keyword: searchText.value })
      } catch (error) {
        if (error !== 'cancel') {
          showByCode(4103) // 确认收货失败
        }
      }
    }
    
    // 查看物流
    const viewLogistics = orderId => {
      orderPromptMessages.showViewLogisticsDev()
      // router.push(`/order/logistics/${orderId}`)
    }

    // 写评价：生产结构下跳转到评价页面（后续由订单评价页接入真实提交流程）
    const writeReview = orderId => {
      router.push(`/order/review/${orderId}`)
    }
    
    // 删除订单
    const deleteOrder = orderId => {
      orderPromptMessages.showDeleteOrderDev()
    }
    
    // 修改订单地址：打开修改地址对话框
    const modifyAddress = async (orderId) => {
      currentOrderId.value = orderId
      
      // 确保用户地址列表已加载
      if (!userStore.addressList || userStore.addressList.length === 0) {
        try {
          await userStore.fetchAddresses()
        } catch (error) {
          console.error('获取地址列表失败:', error)
        }
      }
      
      // 获取订单详情以获取当前地址
      try {
        const res = await orderStore.fetchOrderDetail(orderId)
        const orderDetail = res?.data || res
        if (orderDetail && orderDetail.address) {
          const currentAddr = orderDetail.address
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
            receiverName: currentAddr.receiverName || currentAddr.name || '',
            receiverPhone: currentAddr.receiverPhone || currentAddr.phone || '',
            region: initialRegion,
            detailAddress: currentAddr.detailAddress || currentAddr.detail || ''
          }
          
          // 保存初始值（深拷贝）
          initialAddressForm.value = {
            receiverName: currentAddr.receiverName || currentAddr.name || '',
            receiverPhone: currentAddr.receiverPhone || currentAddr.phone || '',
            region: [...initialRegion],
            detailAddress: currentAddr.detailAddress || currentAddr.detail || ''
          }
        }
      } catch (error) {
        console.error('获取订单详情失败:', error)
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
    }
    
    // 检查表单是否被修改
    const isFormModified = () => {
      return editAddressForm.value.receiverName !== initialAddressForm.value.receiverName ||
             editAddressForm.value.receiverPhone !== initialAddressForm.value.receiverPhone ||
             JSON.stringify(editAddressForm.value.region) !== JSON.stringify(initialAddressForm.value.region) ||
             editAddressForm.value.detailAddress !== initialAddressForm.value.detailAddress
    }
    
    // 按钮文案：根据当前状态和是否修改决定显示“保存”还是“修改地址”
    const addressSubmitText = computed(() => {
      // 选了地址簿：始终是保存（直接更新订单地址）
      if (selectedAddressId.value) return '保存'
      // 已经创建过新地址：下一步就是保存到订单
      if (addressActionStage.value === 'created') return '保存'
      // 表单有改动但还没创建地址：先走“修改地址”（触发新增地址）
      if (isFormModified()) return '修改地址'
      // 默认：保存（什么都不做直接关闭）
      return '保存'
    })
    
    // 提交修改订单地址
    const submitAddressChange = async () => {
      if (!currentOrderId.value) {
        showByCode(5147, '订单ID不存在')
        return
      }
      
      // 1）既没选地址簿，也没改表单：什么都不做，直接关
      if (!selectedAddressId.value && !isFormModified() && addressActionStage.value === 'initial') {
        addressDialogVisible.value = false
        return
      }
      
      // 2）选了地址簿：直接更新订单地址
      if (selectedAddressId.value) {
        addressSubmitting.value = true
        try {
          const res = await orderStore.updateOrderAddress({
            orderId: currentOrderId.value,
            addressId: selectedAddressId.value
          })
          const code = res?.code
          if (code) {
            showByCode(code)
          }
          addressDialogVisible.value = false
          await orderStore.fetchOrders({ page: currentPage.value, size: pageSize.value, keyword: searchText.value })
        } catch (error) {
          showByCode(5147, error?.message || null)
        } finally {
          addressSubmitting.value = false
        }
        return
      }
      
      // 3）已经创建过新地址：这次点击是真正“保存到订单”
      if (addressActionStage.value === 'created' && createdAddressId.value) {
        addressSubmitting.value = true
        try {
          const res = await orderStore.updateOrderAddress({
            orderId: currentOrderId.value,
            addressId: createdAddressId.value
          })
          const code = res?.code
          if (code) {
            showByCode(code)
          }
          addressDialogVisible.value = false
          await orderStore.fetchOrders({ page: currentPage.value, size: pageSize.value, keyword: searchText.value })
        } catch (error) {
          showByCode(5147, error?.message || null)
        } finally {
          addressSubmitting.value = false
          // 重置阶段
          addressActionStage.value = 'initial'
          createdAddressId.value = null
        }
        return
      }
      
      // 4）表单有改动，但还没创建地址：当前点击是“修改地址”（触发新增地址）
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
        
        // 创建新地址（成功码为 2007，而不是 200）
        const addRes = await userStore.addAddress(addressData)
        if (!addRes || (addRes.code !== 200 && addRes.code !== 2007)) {
          showByCode(addRes?.code || 5147)
          return
        }
        
        // 获取新创建的地址ID
        await userStore.fetchAddresses()
        const newAddress = userStore.addressList.find(addr => {
          if (!addr) return false
          const name = addr.receiverName || addr.name
          const phone = addr.receiverPhone || addr.phone
          const detail = addr.detailAddress || addr.detail
          return (
            name === addressData.receiverName &&
            phone === addressData.receiverPhone &&
            addr.province === addressData.province &&
            addr.city === addressData.city &&
            addr.district === addressData.district &&
            detail === addressData.detailAddress
          )
        })
        
        if (!newAddress) {
          showByCode(5147, '无法获取新创建的地址ID')
          return
        }
        
        createdAddressId.value = newAddress.id
        addressActionStage.value = 'created'
        // 此时不关闭对话框，只提示“地址添加成功”，按钮文案自动变回“保存”
        showByCode(addRes.code)
      } catch (error) {
        showByCode(5147, error?.message || null)
      } finally {
        addressSubmitting.value = false
      }
    }
    
    // 联系商家（用户身份发起 customer 会话）
    // 必须与 ChatPage.openContact(店铺) 完全一致：先创建/恢复会话，再跳转并选中
    const contactShop = async shopId => {
      if (!shopId) return
      try {
        const res = await messageStore.createChatSession({
          targetId: String(shopId),
          targetType: 'customer'
        })
        if (!isSuccess(res.code)) {
          showByCode(res.code)
          return
        }
        const sessionId = res.data?.id
        router.push({
          path: '/message/chat',
          query: {
            sessionId: sessionId ? String(sessionId) : undefined,
            shopId: String(shopId)
          }
        })
      } catch (e) {
        if (process.env.NODE_ENV === 'development') {
          console.error('[开发调试] 联系商家失败：', e)
        }
      }
    }
    
// 初始化
onMounted(() => {
  orderStore.updateFilters({
    keyword: '',
    status: ''
  })
  orderStore.fetchOrders({ page: currentPage.value, size: pageSize.value })
})
</script>

<style lang="scss" scoped>
.order-list-page {
  padding: 20px 0 40px;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.container.main-content {
  width: 85%;
  max-width: 1920px;
  margin: 0 auto;
  padding: 0;
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
        margin-right: 12px;
      }
      
      .payment-id-hint {
        font-size: 12px;
        color: #909399;
        margin-left: 8px;
        
        &::before {
          content: '|';
          margin-right: 8px;
          color: #dcdfe6;
        }
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

/* 仅针对修改地址对话框本身，调整外边距位置，不影响内部结构 */
:deep(.address-edit-dialog-wrapper) {
  margin: 0 auto !important;
  top: 50% !important;
  transform: translateY(-50%) !important;
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