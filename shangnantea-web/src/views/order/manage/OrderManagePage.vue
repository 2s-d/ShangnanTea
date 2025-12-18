<template>
  <div class="order-manage-page">
    <el-card class="order-manage-card">
      <template #header>
        <div class="card-header">
          <h2 class="page-title">订单管理</h2>
          <div class="header-actions">
            <el-tooltip content="刷新订单列表" placement="top">
              <el-button circle @click="refreshOrders" :loading="loading">
                <el-icon><Refresh /></el-icon>
              </el-button>
            </el-tooltip>
          </div>
        </div>
      </template>

      <!-- 搜索和筛选区域 -->
      <div class="search-filter-section">
        <el-form :model="searchForm" inline>
          <el-form-item label="订单编号">
            <el-input v-model="searchForm.orderId" placeholder="请输入订单编号" clearable />
          </el-form-item>
          <el-form-item label="商品名称">
            <el-input v-model="searchForm.teaName" placeholder="请输入商品名称" clearable />
          </el-form-item>
          <el-form-item label="订单状态">
            <el-select v-model="searchForm.status" placeholder="全部状态" clearable>
              <el-option label="待付款" :value="0" />
              <el-option label="待发货" :value="1" />
              <el-option label="待收货" :value="2" />
              <el-option label="已完成" :value="3" />
              <el-option label="已取消" :value="4" />
              <el-option label="已退款" :value="5" />
            </el-select>
          </el-form-item>
          <el-form-item label="下单时间">
            <el-date-picker
              v-model="searchForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 订单列表 -->
      <div class="order-list-section">
        <el-table
          v-loading="loading"
          :data="orderList"
          style="width: 100%"
          border
          stripe
        >
          <el-table-column prop="id" label="订单编号" min-width="180" show-overflow-tooltip>
            <template #default="scope">
              <el-link type="primary" @click="viewOrderDetail(scope.row.id)">{{ scope.row.id }}</el-link>
            </template>
          </el-table-column>
          <el-table-column prop="tea_name" label="商品名称" min-width="180" show-overflow-tooltip />
          <el-table-column prop="spec_name" label="规格" min-width="120" show-overflow-tooltip />
          <el-table-column prop="price" label="单价" min-width="100">
            <template #default="scope">
              ¥{{ scope.row.price.toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" min-width="80" />
          <el-table-column prop="total_amount" label="总金额" min-width="120">
            <template #default="scope">
              ¥{{ scope.row.total_amount.toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="订单状态" min-width="120">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)" effect="light">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
              <el-tag v-if="scope.row._temp_refund_applied" type="warning" effect="light" style="margin-left: 5px">
                退款申请中
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="create_time" label="下单时间" min-width="180" />
          <el-table-column label="操作" fixed="right" min-width="200">
            <template #default="scope">
              <!-- 待发货状态 -->
              <div v-if="scope.row.status === 1">
                <el-button 
                  v-if="!scope.row._temp_refund_applied" 
                  type="primary" 
                  size="small" 
                  @click="shipOrder(scope.row)"
                >
                  发货
                </el-button>
                <el-button 
                  v-if="scope.row._temp_refund_applied" 
                  type="warning" 
                  size="small" 
                  @click="openRefundDetail(scope.row)"
                >
                  处理退款
                </el-button>
              </div>
              
              <!-- 其他状态通用操作 -->
              <el-button size="small" @click="viewOrderDetail(scope.row.id)">
                查看详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </el-card>

    <!-- 发货对话框 -->
    <el-dialog
      v-model="shipDialogVisible"
      title="订单发货"
      width="500px"
    >
      <el-form :model="shipForm" label-width="120px">
        <el-form-item label="订单编号">
          <span>{{ currentOrder?.id }}</span>
        </el-form-item>
        <el-form-item label="商品信息">
          <span>{{ currentOrder?.tea_name }} ({{ currentOrder?.spec_name }}) x{{ currentOrder?.quantity }}</span>
        </el-form-item>
        <el-form-item label="收货人">
          <span>{{ getReceiverInfo() }}</span>
        </el-form-item>
        <el-form-item label="物流公司" required>
          <el-select v-model="shipForm.company" placeholder="请选择物流公司" style="width: 100%">
            <el-option label="顺丰速运" value="顺丰速运" />
            <el-option label="中通快递" value="中通快递" />
            <el-option label="圆通快递" value="圆通快递" />
            <el-option label="韵达快递" value="韵达快递" />
            <el-option label="申通快递" value="申通快递" />
            <el-option label="邮政EMS" value="邮政EMS" />
          </el-select>
        </el-form-item>
        <el-form-item label="物流单号" required>
          <el-input v-model="shipForm.trackingNumber" placeholder="请输入物流单号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="shipDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="confirmShip">确认发货</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 退款申请处理对话框 -->
    <el-dialog
      v-model="refundDialogVisible"
      title="退款申请处理"
      width="500px"
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="订单编号">{{ currentRefundOrder?.id }}</el-descriptions-item>
        <el-descriptions-item label="商品信息">
          {{ currentRefundOrder?.tea_name }} ({{ currentRefundOrder?.spec_name }}) x{{ currentRefundOrder?.quantity }}
        </el-descriptions-item>
        <el-descriptions-item label="订单金额">¥{{ currentRefundOrder?.total_amount?.toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ formatRefundApplyTime() }}</el-descriptions-item>
        <el-descriptions-item label="退款原因">{{ currentRefundOrder?._temp_refund_reason || '未提供原因' }}</el-descriptions-item>
      </el-descriptions>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="refundDialogVisible = false">关闭</el-button>
          <el-button type="warning" :loading="submitting" @click="handleRefund(currentRefundOrder, true)">
            同意退款
          </el-button>
          <el-button :loading="submitting" @click="handleRefund(currentRefundOrder, false)">
            拒绝退款
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'

/*
// 真实代码（开发UI时注释）
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
*/

// TODO-DB: 订单管理页面权限与数据过滤说明
// 1. 商家(角色ID: 3)和管理员(角色ID: 1)都可以访问此页面，但数据范围不同
// 2. 商家只能查看/操作自己店铺的订单 (根据shop_id过滤)
// 3. 管理员只能查看/操作平台直售的订单 (固定shop_id的订单)
// 4. 实际开发时，在OrderManageViewModel中实现shopId过滤逻辑：
//    - 商家：useStore().getters['user/shopId']
//    - 管理员：PLATFORM_SHOP_ID (平台店铺固定ID)
// 5. 所有API请求需要自动附加shopId参数，确保权限隔离

export default {
  name: 'OrderManagePage',
  setup() {
    const router = useRouter()
    const loading = ref(false)
    const submitting = ref(false)
    
    // 分页相关
    const currentPage = ref(1)
    const pageSize = ref(10)
    const total = ref(0)
    
    // 搜索表单
    const searchForm = reactive({
      orderId: '',
      teaName: '',
      status: '',
      dateRange: []
    })
    
    // 发货表单
    const shipDialogVisible = ref(false)
    const currentOrder = ref(null)
    const shipForm = reactive({
      company: '',
      trackingNumber: ''
    })
    
    // 退款申请处理相关
    const refundDialogVisible = ref(false)
    const currentRefundOrder = ref(null)
    
    // TODO-VUEX: 实际开发时，需要根据用户角色获取shopId
    // 模拟订单数据(UI开发阶段使用)
    const mockOrders = [
      {
        id: 'OD20250301001',
        user_id: 'cy100002',
        shop_id: 'shop100001', // 商家店铺ID
        tea_id: 'tea1000001',
        tea_name: '商南翠峰',
        spec_id: 1,
        spec_name: '罐装100g',
        tea_image: '/images/tea1_main.png',
        quantity: 2,
        price: 128.00,
        total_amount: 256.00,
        address_id: 1,
        status: 1, // 待发货
        payment_method: '支付宝',
        create_time: '2025-03-01 10:23:45',
        update_time: '2025-03-01 10:28:45',
        // 临时字段：是否申请退款(仅用于UI开发)
        _temp_refund_applied: true,
        // 临时字段：退款原因(仅用于UI开发)
        _temp_refund_reason: '商品与描述不符，请求退款'
      },
      {
        id: 'OD20250302001',
        user_id: 'cy100002',
        shop_id: 'shop100001', // 商家店铺ID
        tea_id: 'tea1000002',
        tea_name: '商南红茶特级',
        spec_id: 2,
        spec_name: '盒装200g',
        tea_image: '/images/tea2_main.png',
        quantity: 1,
        price: 238.00,
        total_amount: 238.00,
        address_id: 1,
        status: 1, // 待发货
        payment_method: '微信支付',
        create_time: '2025-03-02 14:35:22',
        update_time: '2025-03-02 14:40:15',
        _temp_refund_applied: false,
        _temp_refund_reason: ''
      },
      {
        id: 'OD20250303001',
        user_id: 'cy100003',
        shop_id: 'shop100001', // 商家店铺ID
        tea_id: 'tea1000003',
        tea_name: '商南绿茶珍品',
        spec_id: 3,
        spec_name: '礼盒装500g',
        tea_image: '/images/tea3_main.png',
        quantity: 1,
        price: 568.00,
        total_amount: 568.00,
        address_id: 2,
        status: 2, // 待收货
        payment_method: '支付宝',
        logistics_company: '顺丰速运',
        logistics_number: 'SF1234567890',
        shipping_time: '2025-03-03 10:15:30',
        create_time: '2025-03-03 09:12:45',
        update_time: '2025-03-03 10:15:30',
        _temp_refund_applied: false,
        _temp_refund_reason: ''
      },
      {
        id: 'OD20250304001',
        user_id: 'cy100002',
        shop_id: 'PLATFORM', // 平台直售商品
        tea_id: 'tea2000001',
        tea_name: '平台直售商南红茶',
        spec_id: 1,
        spec_name: '罐装100g',
        tea_image: '/images/platform_tea1.png',
        quantity: 1,
        price: 108.00,
        total_amount: 108.00,
        address_id: 1,
        status: 1, // 待发货
        payment_method: '微信支付',
        create_time: '2025-03-04 16:23:45',
        update_time: '2025-03-04 16:28:45',
        _temp_refund_applied: false,
        _temp_refund_reason: ''
      },
      {
        id: 'OD20250305001',
        user_id: 'cy100003',
        shop_id: 'PLATFORM', // 平台直售商品
        tea_id: 'tea2000002',
        tea_name: '平台直售商南绿茶',
        spec_id: 2,
        spec_name: '盒装200g',
        tea_image: '/images/platform_tea2.png',
        quantity: 2,
        price: 158.00,
        total_amount: 316.00,
        address_id: 2,
        status: 3, // 已完成
        payment_method: '支付宝',
        logistics_company: '顺丰速运',
        logistics_number: 'SF2345678901',
        shipping_time: '2025-03-05 11:45:30',
        completion_time: '2025-03-08 09:30:15',
        create_time: '2025-03-05 10:33:45',
        update_time: '2025-03-08 09:30:15',
        _temp_refund_applied: false,
        _temp_refund_reason: ''
      }
    ]
    
    // 模拟收货地址数据
    const mockAddresses = {
      1: {
        id: 1,
        user_id: 'cy100002',
        name: '张三',
        phone: '13900000002',
        province: '北京市',
        city: '北京市',
        district: '海淀区',
        detail: '中关村科技园23号楼403室'
      },
      2: {
        id: 2,
        user_id: 'cy100003',
        name: '李四',
        phone: '13900000003',
        province: '上海市',
        city: '上海市',
        district: '浦东新区',
        detail: '张江高科技园区88号'
      }
    }
    
    // UI模拟订单列表
    const orderList = ref([])
    
    // UI-DEV: 模拟查询订单列表
    const fetchOrders = () => {
      loading.value = true
      
      // 模拟网络请求延迟
      setTimeout(() => {
        // 模拟根据商家或管理员身份筛选订单
        // 注意：真实场景应在后端根据用户角色自动筛选
        
        // 假设当前是管理员，只能看平台订单
        const isAdmin = true
        // 假设当前是商家，拥有shop100001店铺
        const isShop = false
        const myShopId = 'shop100001'
        
        let filteredOrders = []
        
        if (isAdmin) {
          // 管理员只能看平台直售订单
          filteredOrders = mockOrders.filter(order => order.shop_id === 'PLATFORM')
        } else if (isShop) {
          // 商家只能看自己店铺的订单
          filteredOrders = mockOrders.filter(order => order.shop_id === myShopId)
        } else {
          // 模拟开发阶段，显示所有订单
          filteredOrders = [...mockOrders]
        }
        
        // 应用搜索条件
        if (searchForm.orderId) {
          filteredOrders = filteredOrders.filter(order => 
            order.id.toLowerCase().includes(searchForm.orderId.toLowerCase())
          )
        }
        
        if (searchForm.teaName) {
          filteredOrders = filteredOrders.filter(order => 
            order.tea_name.toLowerCase().includes(searchForm.teaName.toLowerCase())
          )
        }
        
        if (searchForm.status !== '') {
          filteredOrders = filteredOrders.filter(order => order.status === searchForm.status)
        }
        
        if (searchForm.dateRange && searchForm.dateRange.length === 2) {
          const startDate = new Date(searchForm.dateRange[0])
          const endDate = new Date(searchForm.dateRange[1])
          endDate.setHours(23, 59, 59, 999) // 设置为当天结束时间
          
          filteredOrders = filteredOrders.filter(order => {
            const orderDate = new Date(order.create_time)
            return orderDate >= startDate && orderDate <= endDate
          })
        }
        
        // 模拟分页
        total.value = filteredOrders.length
        const start = (currentPage.value - 1) * pageSize.value
        const end = start + pageSize.value
        
        orderList.value = filteredOrders.slice(start, end)
        loading.value = false
      }, 600)
    }
    
    // 刷新订单列表
    const refreshOrders = () => {
      fetchOrders()
    }
    
    // 搜索订单
    const handleSearch = () => {
      currentPage.value = 1
      fetchOrders()
    }
    
    // 重置搜索条件
    const resetSearch = () => {
      searchForm.orderId = ''
      searchForm.teaName = ''
      searchForm.status = ''
      searchForm.dateRange = []
      currentPage.value = 1
      fetchOrders()
    }
    
    // 处理分页大小变化
    const handleSizeChange = (size) => {
      pageSize.value = size
      fetchOrders()
    }
    
    // 处理当前页变化
    const handleCurrentChange = (page) => {
      currentPage.value = page
      fetchOrders()
    }
    
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
    
    // 获取状态对应的类型(用于标签颜色)
    const getStatusType = (status) => {
      const statusMap = {
        0: 'warning',  // 待付款：黄色警告
        1: 'primary',  // 待发货：蓝色主色
        2: 'success',  // 待收货：绿色成功
        3: 'info',     // 已完成：灰色信息
        4: 'danger',   // 已取消：红色危险
        5: 'info'      // 已退款：灰色信息
      }
      return statusMap[status] || 'info'
    }
    
    // 查看订单详情
    const viewOrderDetail = (orderId) => {
      router.push(`/order/detail/${orderId}`)
    }
    
    // 跳转发货对话框
    const shipOrder = (order) => {
      currentOrder.value = order
      shipForm.company = ''
      shipForm.trackingNumber = ''
      shipDialogVisible.value = true
    }
    
    // 打开退款详情对话框
    const openRefundDetail = (order) => {
      currentRefundOrder.value = order
      refundDialogVisible.value = true
    }
    
    // 格式化退款申请时间
    const formatRefundApplyTime = () => {
      if (!currentRefundOrder.value) return ''
      // 实际开发时应从退款申请表中获取申请时间
      // 这里简单返回更新时间作为示例
      return currentRefundOrder.value.update_time
    }
    
    // 确认发货
    const confirmShip = () => {
      // 表单验证
      if (!shipForm.company) {
        ElMessage.warning('请选择物流公司')
        return
      }
      
      if (!shipForm.trackingNumber) {
        ElMessage.warning('请输入物流单号')
        return
      }
      
      submitting.value = true
      
      // 模拟发货处理
      setTimeout(() => {
        // 更新本地状态
        const index = orderList.value.findIndex(item => item.id === currentOrder.value.id)
        if (index !== -1) {
          orderList.value[index].status = 2 // 更新为待收货状态
          orderList.value[index].logistics_company = shipForm.company
          orderList.value[index].logistics_number = shipForm.trackingNumber
          orderList.value[index].shipping_time = new Date().toISOString().replace('T', ' ').substring(0, 19)
          orderList.value[index].update_time = new Date().toISOString().replace('T', ' ').substring(0, 19)
        }
        
        submitting.value = false
        shipDialogVisible.value = false
        ElMessage.success('发货成功')
      }, 800)
    }
    
    // 处理退款申请
    const handleRefund = (order, isApproved) => {
      if (!order) return
      
      const action = isApproved ? '同意' : '拒绝'
      
      ElMessageBox.confirm(
        `确定要${action}该退款申请吗？`,
        `${action}退款申请`,
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: isApproved ? 'warning' : 'info'
        }
      ).then(() => {
        // 模拟处理退款请求
        submitting.value = true
        
        setTimeout(() => {
          const index = orderList.value.findIndex(item => item.id === order.id)
          if (index !== -1) {
            if (isApproved) {
              // 同意退款 - 更新状态为已退款
              orderList.value[index].status = 5
              orderList.value[index]._temp_refund_applied = false
              ElMessage.success('已同意退款申请')
            } else {
              // 拒绝退款 - 保持原状态，清除退款申请标记
              orderList.value[index]._temp_refund_applied = false
              ElMessage.success('已拒绝退款申请')
            }
            
            orderList.value[index].update_time = new Date().toISOString().replace('T', ' ').substring(0, 19)
          }
          
          submitting.value = false
          refundDialogVisible.value = false
        }, 800)
      }).catch(() => {
        // 用户取消操作
      })
    }
    
    // 获取收货人信息
    const getReceiverInfo = () => {
      if (!currentOrder.value) return ''
      
      const address = mockAddresses[currentOrder.value.address_id]
      if (address) {
        return `${address.name} ${address.phone} ${address.province}${address.city}${address.district}${address.detail}`
      }
      
      return '无收货信息'
    }
    
    // 初始化
    onMounted(() => {
      fetchOrders()
    })
    
    return {
      loading,
      submitting,
      orderList,
      currentPage,
      pageSize,
      total,
      searchForm,
      shipDialogVisible,
      currentOrder,
      shipForm,
      refundDialogVisible,
      currentRefundOrder,
      Refresh,
      refreshOrders,
      handleSearch,
      resetSearch,
      handleSizeChange,
      handleCurrentChange,
      getStatusText,
      getStatusType,
      viewOrderDetail,
      shipOrder,
      confirmShip,
      handleRefund,
      openRefundDetail,
      formatRefundApplyTime,
      getReceiverInfo
    }
  }
}
</script>

<style lang="scss" scoped>
.order-manage-page {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.order-manage-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .page-title {
    margin: 0;
    font-size: 20px;
    font-weight: bold;
  }
  
  .header-actions {
    display: flex;
    align-items: center;
  }
}

.search-filter-section {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.order-list-section {
  .el-table {
    margin-bottom: 20px;
  }
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 15px;
}

@media (max-width: 768px) {
  .search-filter-section {
    .el-form-item {
      margin-bottom: 10px;
    }
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    
    .page-title {
      margin-bottom: 10px;
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  width: 100%;
  gap: 10px;
}
</style> 