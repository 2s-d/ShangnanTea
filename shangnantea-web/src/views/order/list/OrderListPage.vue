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
                <span v-if="order._temp_refund_applied && order.order_status !== 5" class="status-tag status-refund-applied">
                  退款申请中
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
                  <el-button type="primary" size="small" @click="applyRefund(order.order_id)" v-if="!order._temp_refund_applied">
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
                  <el-button size="small" @click="deleteOrder(order.order_id)">
                    删除订单
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
    
    <!-- 退款申请对话框 -->
    <el-dialog
      v-model="refundDialogVisible"
      title="申请退款"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="{ refundReason }" label-width="80px">
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
          <el-button @click="refundDialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            :loading="submittingRefund"
            @click="submitRefund"
          >
            提交申请
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 评价对话框 -->
    <el-dialog
      v-model="reviewDialogVisible"
      title="评价商品"
      width="550px"
      :close-on-click-modal="false"
    >
      <div v-if="currentReviewOrder" class="review-product-info">
        <div class="product-image">
          <SafeImage :src="currentReviewOrder.tea_image" type="tea" :alt="currentReviewOrder.tea_name" style="width:80px;height:80px;object-fit:cover;" />
        </div>
        <div class="product-details">
          <div class="product-name">{{ currentReviewOrder.tea_name }}</div>
          <div class="product-spec">规格：{{ currentReviewOrder.spec_name }}</div>
        </div>
      </div>
      
      <el-form :model="reviewForm" label-width="80px" class="review-form">
        <el-form-item label="评分" required>
          <el-rate
            v-model="reviewForm.rating"
            allow-half
            show-text
            :texts="['差评', '较差', '一般', '好评', '极好']"
          ></el-rate>
        </el-form-item>
        <el-form-item label="评价内容" required>
          <el-input
            v-model="reviewForm.content"
            type="textarea"
            :rows="4"
            placeholder="请分享您的使用体验（不少于5个字）"
            maxlength="500"
            show-word-limit
          ></el-input>
        </el-form-item>
        <el-form-item label="晒图">
          <el-upload
            v-model:file-list="reviewForm.fileList"
            action="#"
            list-type="picture-card"
            :auto-upload="false"
            :limit="3"
            :on-change="handleImageChange"
            :on-remove="handleImageRemove"
          >
            <i class="el-icon-plus"></i>
            <template #tip>
              <div class="el-upload__tip">可选择上传商品实物图片（最多3张）</div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item label="匿名评价">
          <el-switch v-model="reviewForm.isAnonymous"></el-switch>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="reviewDialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            :loading="submittingReview"
            @click="submitReview"
          >
            提交评价
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
/* UI-DEV-START */
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import SafeImage from '@/components/common/form/SafeImage.vue'
/* UI-DEV-END */

/*
// 真实代码（开发UI时注释）
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
*/

// TODO-DB: 【数据库缺陷警告】当前数据库设计缺少退款申请相关字段/表
// 需要在后端开发阶段先创建order_refunds表，建议包含以下字段:
// id INT PRIMARY KEY AUTO_INCREMENT - 主键
// order_id VARCHAR(20) NOT NULL - 订单ID
// reason VARCHAR(500) NOT NULL - 退款原因
// status TINYINT NOT NULL DEFAULT 1 - 状态：1-申请中，2-已通过，3-已拒绝
// apply_time DATETIME NOT NULL - 申请时间
// approve_time DATETIME DEFAULT NULL - 审批时间
// admin_id VARCHAR(8) DEFAULT NULL - 处理人ID
// admin_notes VARCHAR(500) DEFAULT NULL - 处理备注
// 前端Vuex开发前必须确认此表已创建，否则无法实现退款申请功能

export default {
  name: 'OrderListPage',
  components: {
    SafeImage
  },
  setup() {
    /* UI-DEV-START */
    const router = useRouter()
    const loading = ref(false)
    const searchText = ref('')
    const activeTab = ref('all')
    const currentPage = ref(1)
    const pageSize = ref(5)
    const total = ref(0)
    
    // 申请退款相关数据
    const refundDialogVisible = ref(false)
    const refundReason = ref('')
    const currentRefundOrderId = ref('')
    const submittingRefund = ref(false)
    
    // 添加默认图片常量
    // 模拟订单数据
    // 注意：系统使用单品订单模式，每个订单只对应一个茶叶商品
    // 不同商品会生成不同的订单号，即使它们是同时结算的
    const orders = ref([
      {
        order_id: 'ORD20230501001',
        user_id: 1,
        order_status: 0, // 待付款
        shop_id: 'shop100001',
        tea_id: 101,
        tea_name: '商南毛尖特级',
        spec_id: 1001,
        spec_name: '100g礼盒装',
        tea_image: '/images/tea1.jpg',
        quantity: 1, // 此商品的购买数量
        price: 168.00, // 单价
        total_amount: 168.00, // 订单总金额（含运费）
        shipping_fee: 10.00,
        payment_method: 'alipay',
        address_id: 1,
        create_time: '2023-05-01 10:23:45',
        update_time: '2023-05-01 10:23:45',
        is_reviewed: false, // 是否已评价
        // TODO-DB: 以下字段数据库中不存在，仅用于UI开发阶段模拟，实际开发时应使用order_refunds表
        _temp_refund_applied: false, // 临时字段：是否申请退款(仅用于UI开发)
        _temp_refund_reason: '' // 临时字段：退款原因(仅用于UI开发)
      },
      {
        order_id: 'ORD20230501002',
        user_id: 1,
        order_status: 0, // 待付款
        shop_id: 'shop100001',
        tea_id: 102,
        tea_name: '商南红茶',
        spec_id: 1002,
        spec_name: '200g罐装',
        tea_image: '/images/tea2.jpg',
        quantity: 1,
        price: 60.00,
        total_amount: 70.00,
        shipping_fee: 10.00,
        payment_method: 'alipay',
        address_id: 1,
        create_time: '2023-05-01 10:23:45',
        update_time: '2023-05-01 10:23:45',
        is_reviewed: false, // 是否已评价
        // TODO-DB: 以下字段数据库中不存在，仅用于UI开发阶段模拟，实际开发时应使用order_refunds表
        _temp_refund_applied: false, // 临时字段：是否申请退款(仅用于UI开发)
        _temp_refund_reason: '' // 临时字段：退款原因(仅用于UI开发)
      },
      {
        order_id: 'ORD20230428002',
        user_id: 1,
        order_status: 1, // 待发货
        shop_id: 'shop100001',
        tea_id: 103,
        tea_name: '商南绿茶珍品',
        spec_id: 1003,
        spec_name: '250g礼盒装',
        tea_image: '/images/tea3.jpg',
        quantity: 1,
        price: 328.00,
        total_amount: 328.00,
        shipping_fee: 0.00,
        payment_method: 'wechat',
        address_id: 2,
        create_time: '2023-04-28 16:45:32',
        update_time: '2023-04-28 16:50:19',
        is_reviewed: false, // 是否已评价
        // TODO-DB: 以下字段数据库中不存在，仅用于UI开发阶段模拟，实际开发时应使用order_refunds表
        _temp_refund_applied: false, // 临时字段：是否申请退款(仅用于UI开发)
        _temp_refund_reason: '' // 临时字段：退款原因(仅用于UI开发)
      },
      {
        order_id: 'ORD20230420003',
        user_id: 1,
        order_status: 2, // 待收货
        shop_id: 'shop100001',
        tea_id: 104,
        tea_name: '商南翠绿',
        spec_id: 1004,
        spec_name: '500g礼盒装',
        tea_image: '/images/tea4.jpg',
        quantity: 1,
        price: 438.00,
        total_amount: 458.00,
        shipping_fee: 20.00,
        payment_method: 'alipay',
        address_id: 1,
        create_time: '2023-04-20 09:12:37',
        update_time: '2023-04-20 14:25:41',
        is_reviewed: false, // 是否已评价
        // TODO-DB: 以下字段数据库中不存在，仅用于UI开发阶段模拟，实际开发时应使用order_refunds表
        _temp_refund_applied: false, // 临时字段：是否申请退款(仅用于UI开发)
        _temp_refund_reason: '' // 临时字段：退款原因(仅用于UI开发)
      },
      {
        order_id: 'ORD20230410004',
        user_id: 1,
        order_status: 3, // 已完成
        shop_id: 'shop100001',
        tea_id: 105,
        tea_name: '商南绿茶精品',
        spec_id: 1005,
        spec_name: '150g罐装',
        tea_image: '/images/tea5.jpg',
        quantity: 2,
        price: 98.00,
        total_amount: 196.00,
        shipping_fee: 10.00,
        payment_method: 'wechat',
        address_id: 2,
        create_time: '2023-04-10 15:33:22',
        update_time: '2023-04-15 10:12:38',
        is_reviewed: false, // 未评价
        // TODO-DB: 以下字段数据库中不存在，仅用于UI开发阶段模拟，实际开发时应使用order_refunds表
        _temp_refund_applied: false, // 临时字段：是否申请退款(仅用于UI开发)
        _temp_refund_reason: '' // 临时字段：退款原因(仅用于UI开发)
      },
      {
        order_id: 'ORD20230401005',
        user_id: 1,
        order_status: 4, // 已取消
        shop_id: 'shop100001',
        tea_id: 106,
        tea_name: '商南老茶',
        spec_id: 1006,
        spec_name: '200g礼盒装',
        tea_image: '/images/tea6.jpg',
        quantity: 1,
        price: 268.00,
        total_amount: 268.00,
        shipping_fee: 10.00,
        payment_method: 'alipay',
        address_id: 1,
        create_time: '2023-04-01 12:22:17',
        update_time: '2023-04-01 13:10:05',
        is_reviewed: false, // 是否已评价
        // TODO-DB: 以下字段数据库中不存在，仅用于UI开发阶段模拟，实际开发时应使用order_refunds表
        _temp_refund_applied: false, // 临时字段：是否申请退款(仅用于UI开发)
        _temp_refund_reason: '' // 临时字段：退款原因(仅用于UI开发)
      },
      {
        order_id: 'ORD20230315006',
        user_id: 1,
        order_status: 3, // 已完成
        shop_id: 'shop100001',
        tea_id: 107,
        tea_name: '商南特级绿茶',
        spec_id: 1007,
        spec_name: '200g精品礼盒',
        tea_image: '/images/tea7.jpg',
        quantity: 1,
        price: 356.00,
        total_amount: 356.00,
        shipping_fee: 0.00,
        payment_method: 'wechat',
        address_id: 1,
        create_time: '2023-03-15 09:10:12',
        update_time: '2023-03-20 14:25:30',
        is_reviewed: true, // 已评价
        // TODO-DB: 以下字段数据库中不存在，仅用于UI开发阶段模拟，实际开发时应使用order_refunds表
        _temp_refund_applied: false, // 临时字段：是否申请退款(仅用于UI开发)
        _temp_refund_reason: '' // 临时字段：退款原因(仅用于UI开发)
      },
      {
        order_id: 'ORD20230301007',
        user_id: 1,
        order_status: 5, // 已退款
        shop_id: 'shop100001',
        tea_id: 108,
        tea_name: '商南红茶特级',
        spec_id: 1008,
        spec_name: '100g罐装',
        tea_image: '/images/tea8.jpg',
        quantity: 1,
        price: 128.00,
        total_amount: 128.00,
        shipping_fee: 10.00,
        payment_method: 'alipay',
        address_id: 2,
        create_time: '2023-03-01 11:05:22',
        update_time: '2023-03-02 10:25:35',
        is_reviewed: false, // 是否已评价
        // TODO-DB: 以下字段数据库中不存在，仅用于UI开发阶段模拟，实际开发时应使用order_refunds表
        _temp_refund_applied: false, // 临时字段：是否申请退款(仅用于UI开发)
        _temp_refund_reason: '' // 临时字段：退款原因(仅用于UI开发)
      }
    ])
    
    // 根据状态过滤订单
    const filteredOrders = computed(() => {
      let result = orders.value
      
      // 根据选项卡过滤
      if (activeTab.value !== 'all') {
        if (activeTab.value === 'to-review') {
          // 待评价：已完成且未评价的订单
          result = result.filter(order => order.order_status === 3 && !order.is_reviewed)
        } else {
          const statusMap = {
            'unpaid': 0,    // 待付款
            'unshipped': 1, // 待发货
            'shipped': 2,   // 待收货
          }
          result = result.filter(order => order.order_status === statusMap[activeTab.value])
        }
      }
      
      // 根据搜索文本过滤
      if (searchText.value) {
        const keyword = searchText.value.toLowerCase()
        result = result.filter(order => {
          // 搜索订单号
          if (order.order_id.toLowerCase().includes(keyword)) {
            return true
          }
          
          // 搜索商品名称
          if (order.tea_name.toLowerCase().includes(keyword)) {
            return true
          }
          
          return false
        })
      }
      
      // 移除副作用：不在计算属性中修改状态
      const filteredResult = result
      
      // 分页处理
      const start = (currentPage.value - 1) * pageSize.value
      const end = start + pageSize.value
      return filteredResult.slice(start, end)
    })
    
    // 计算总数 - 添加一个独立的计算属性
    const calculateTotal = () => {
      let result = orders.value
      
      // 根据选项卡过滤
      if (activeTab.value !== 'all') {
        if (activeTab.value === 'to-review') {
          // 待评价：已完成且未评价的订单
          result = result.filter(order => order.order_status === 3 && !order.is_reviewed)
        } else {
          const statusMap = {
            'unpaid': 0,    // 待付款
            'unshipped': 1, // 待发货
            'shipped': 2,   // 待收货
          }
          result = result.filter(order => order.order_status === statusMap[activeTab.value])
        }
      }
      
      // 根据搜索文本过滤
      if (searchText.value) {
        const keyword = searchText.value.toLowerCase()
        result = result.filter(order => {
          // 搜索订单号
          if (order.order_id.toLowerCase().includes(keyword)) {
            return true
          }
          
          // 搜索商品名称
          if (order.tea_name.toLowerCase().includes(keyword)) {
            return true
          }
          
          return false
        })
      }
      
      // 设置总数
      total.value = result.length
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
      calculateTotal(); // 更新总数
    }
    
    // 处理分页变化
    const handleCurrentChange = (page) => {
      currentPage.value = page
    }
    
    // 处理标签页切换
    const handleTabChange = () => {
      currentPage.value = 1 // 重置到第一页
      calculateTotal(); // 更新总数
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
    const cancelOrder = (orderId) => {
      ElMessageBox.confirm('确定要取消该订单吗？取消后无法恢复', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 模拟取消订单
        setTimeout(() => {
          // 找到对应订单并更新状态
          const orderIndex = orders.value.findIndex(order => order.order_id === orderId)
          if (orderIndex !== -1) {
            orders.value[orderIndex].order_status = 4 // 已取消状态码修改为4
            orders.value[orderIndex].update_time = new Date().toISOString()
          }
          
          ElMessage.success('订单已取消')
        }, 500)
      }).catch(() => {
        // 用户取消操作，不做任何处理
      })
    }
    
    // 确认收货
    const confirmReceipt = (orderId) => {
      ElMessageBox.confirm('确认已收到商品吗？', '提示', {
        confirmButtonText: '确认收货',
        cancelButtonText: '取消',
        type: 'info'
      }).then(() => {
        // 模拟确认收货
        setTimeout(() => {
          // 找到对应订单并更新状态
          const orderIndex = orders.value.findIndex(order => order.order_id === orderId)
          if (orderIndex !== -1) {
            orders.value[orderIndex].order_status = 3 // 更新为已完成状态
            orders.value[orderIndex].is_reviewed = false // 标记为未评价
            orders.value[orderIndex].update_time = new Date().toISOString()
          }
          
          ElMessage.success('确认收货成功')
        }, 500)
      }).catch(() => {
        // 用户取消操作，不做任何处理
      })
    }
    
    // 查看物流
    const viewLogistics = (orderId) => {
      ElMessage.info('查看物流信息功能开发中')
      // router.push(`/order/logistics/${orderId}`)
    }
    
    // 评价相关数据
    const reviewDialogVisible = ref(false)
    const currentReviewOrder = ref(null)
    const submittingReview = ref(false)
    const reviewForm = ref({
      rating: 5,
      content: '',
      isAnonymous: false,
      fileList: []
    })
    
    // 写评价
    const writeReview = (orderId) => {
      // 查找当前订单
      const order = orders.value.find(o => o.order_id === orderId)
      if (order) {
        currentReviewOrder.value = order
        // 重置表单
        reviewForm.value = {
          rating: 5,
          content: '',
          isAnonymous: false,
          fileList: []
        }
        reviewDialogVisible.value = true
      }
    }
    
    // 处理评价图片变更
    const handleImageChange = (file, fileList) => {
      reviewForm.value.fileList = fileList.slice(0, 3) // 最多3张
    }
    
    // 处理评价图片移除
    const handleImageRemove = (file, fileList) => {
      reviewForm.value.fileList = fileList
    }
    
    // 提交评价
    const submitReview = () => {
      // 表单验证
      if (reviewForm.value.rating === 0) {
        ElMessage.warning('请为商品评分')
        return
      }
      
      if (!reviewForm.value.content.trim() || reviewForm.value.content.trim().length < 5) {
        ElMessage.warning('评价内容不能少于5个字')
        return
      }
      
      submittingReview.value = true
      
      // 模拟提交评价
      setTimeout(() => {
        // 将文件列表转换为图片URL数组
        const images = reviewForm.value.fileList.map(file => {
          // 实际场景中，这里应该是上传图片后返回的URL
          return file.url || '/images/default-review-image.jpg'
        })
        
        // 构建评价对象
        const reviewData = {
          tea_id: currentReviewOrder.value.tea_id,
          order_id: currentReviewOrder.value.order_id,
          content: reviewForm.value.content,
          rating: reviewForm.value.rating,
          images: images.length > 0 ? images : null,
          is_anonymous: reviewForm.value.isAnonymous ? 1 : 0,
          create_time: new Date().toISOString(),
          update_time: new Date().toISOString()
        }
        
        // 实际场景中，这里应该调用API提交评价
        console.log('提交评价数据:', reviewData)
        
        // 更新订单状态为已评价
        const orderIndex = orders.value.findIndex(o => o.order_id === currentReviewOrder.value.order_id)
        if (orderIndex !== -1) {
          orders.value[orderIndex].is_reviewed = true
          orders.value[orderIndex].update_time = new Date().toISOString()
        }
        
        submittingReview.value = false
        reviewDialogVisible.value = false
        ElMessage.success('评价提交成功，感谢您的反馈！')
      }, 1000)
    }
    
    // 申请退款
    const applyRefund = (orderId) => {
      currentRefundOrderId.value = orderId
      refundReason.value = ''
      refundDialogVisible.value = true
    }
    
    // 提交退款申请
    const submitRefund = () => {
      if (!refundReason.value.trim() || refundReason.value.trim().length < 5) {
        ElMessage.warning('退款原因不能少于5个字')
        return
      }
      
      submittingRefund.value = true
      
      // 模拟提交退款申请
      setTimeout(() => {
        // 找到对应订单并标记为申请退款状态
        const orderIndex = orders.value.findIndex(order => order.order_id === currentRefundOrderId.value)
        if (orderIndex !== -1) {
          // TODO-DB: 此处应调用创建退款申请记录的API，而非直接修改订单状态
          // 现阶段仅使用临时字段模拟，实际开发需要创建order_refunds表记录
          orders.value[orderIndex]._temp_refund_applied = true // 标记为已申请退款(临时字段)
          orders.value[orderIndex]._temp_refund_reason = refundReason.value // 退款原因(临时字段)
          orders.value[orderIndex].update_time = new Date().toISOString()
          
          // 模拟商家处理退款申请(5秒后自动变为已退款状态)
          setTimeout(() => {
            // TODO-DB: 实际应更新退款记录状态为已通过，同时更新订单状态
            orders.value[orderIndex].order_status = 5 // 变更为已退款状态
            orders.value[orderIndex]._temp_refund_applied = false // 清除退款申请标记，因为已经变为已退款状态
            orders.value[orderIndex].update_time = new Date().toISOString()
            
            // 通知用户
            ElMessage({
              message: `订单${currentRefundOrderId.value}的退款申请已被商家审核通过`,
              type: 'success',
              duration: 5000
            })
          }, 5000) // 延长到5秒，让用户能更清楚看到退款申请中状态
        }
        
        submittingRefund.value = false
        refundDialogVisible.value = false
        ElMessage.success('退款申请已提交，等待商家审核')
      }, 800)
    }
    
    // 删除订单
    const deleteOrder = (orderId) => {
      ElMessageBox.confirm('确定要删除该订单吗？删除后无法恢复', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 模拟删除订单
        setTimeout(() => {
          // 从数组中移除该订单
          orders.value = orders.value.filter(order => order.order_id !== orderId)
          
          ElMessage.success('订单已删除')
        }, 500)
      }).catch(() => {
        // 用户取消操作，不做任何处理
      })
    }
    
    // 添加修改地址功能
    const modifyAddress = (orderId) => {
      ElMessage.info('修改地址功能开发中')
    }
    
    // 添加联系商家功能
    const contactShop = (shopId) => {
      ElMessage.info('联系商家功能开发中')
    }
    
    // 初始化
    onMounted(() => {
      // 模拟加载数据
      loading.value = true
      
      setTimeout(() => {
        loading.value = false
        calculateTotal(); // 初始化时计算总数
      }, 800)
    })
    /* UI-DEV-END */
    
    /*
    // 真实代码（开发UI时注释）
    const router = useRouter()
    const store = useStore()
    const loading = ref(false)
    const searchText = ref('')
    const activeTab = ref('all')
    const currentPage = ref(1)
    const pageSize = ref(10)
    const total = ref(0)
    
    // 获取订单列表
    const fetchOrders = async () => {
      try {
        loading.value = true
        
        const params = {
          page: currentPage.value,
          pageSize: pageSize.value,
          keyword: searchText.value,
          status: getStatusFilter()
        }
        
        const result = await store.dispatch('order/fetchOrderList', params)
        total.value = result.total || 0
      } catch (error) {
        ElMessage.error('获取订单列表失败')
      } finally {
        loading.value = false
      }
    }
    
    // 获取状态过滤值
    const getStatusFilter = () => {
      const statusMap = {
        'all': null,
        'unpaid': 0,
        'unshipped': 1,
        'shipped': 2,
        'to-review': 'review' // 特殊值，表示待评价（已完成未评价的订单）
      }
      return statusMap[activeTab.value]
    }
    
    // 获取订单列表
    const filteredOrders = computed(() => {
      return store.getters['order/orderList'] || []
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
      fetchOrders()
    }
    
    // 处理分页变化
    const handleCurrentChange = (page) => {
      currentPage.value = page
      fetchOrders()
    }
    
    // 处理标签页切换
    const handleTabChange = () => {
      currentPage.value = 1 // 重置到第一页
      fetchOrders()
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
        await ElMessageBox.confirm('确定要取消该订单吗？取消后无法恢复', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await store.dispatch('order/cancelOrder', orderId)
        ElMessage.success('订单已取消')
        fetchOrders() // 刷新列表
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('取消订单失败')
        }
      }
    }
    
    // 确认收货
    const confirmReceipt = async (orderId) => {
      try {
        await ElMessageBox.confirm('确认已收到商品吗？', '提示', {
          confirmButtonText: '确认收货',
          cancelButtonText: '取消',
          type: 'info'
        })
        
        await store.dispatch('order/confirmReceipt', orderId)
        ElMessage.success('确认收货成功')
        fetchOrders() // 刷新列表
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('确认收货失败')
        }
      }
    }
    
    // 查看物流
    const viewLogistics = (orderId) => {
      router.push(`/order/logistics/${orderId}`)
    }
    
    // 写评价
    const writeReview = (orderId) => {
      router.push(`/order/review/${orderId}`)
    }
    
    // 删除订单
    const deleteOrder = async (orderId) => {
      try {
        await ElMessageBox.confirm('确定要删除该订单吗？删除后无法恢复', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await store.dispatch('order/deleteOrder', orderId)
        ElMessage.success('订单已删除')
        fetchOrders() // 刷新列表
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('删除订单失败')
        }
      }
    }
    
    // 初始化
    onMounted(() => {
      fetchOrders()
    })
    */
    
    return {
      /* UI-DEV-START */
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
      calculateTotal,
      // 新增退款相关
      refundDialogVisible,
      refundReason,
      submittingRefund,
      applyRefund,
      submitRefund,
      // 新增评价相关
      reviewDialogVisible,
      currentReviewOrder,
      reviewForm,
      submittingReview,
      handleImageChange,
      handleImageRemove,
      submitReview,
      modifyAddress,
      contactShop
      /* UI-DEV-END */
      
      /*
      // 真实代码（开发UI时注释）
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
      deleteOrder
      */
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