<template>
  <div class="order-manage-page">
    <el-card class="order-manage-card">
      <template #header>
        <div class="card-header">
          <h2 class="page-title">订单管理</h2>
          <div class="header-actions">
            <el-button
              type="success"
              size="small"
              @click="openExportDialog"
            >
              导出订单
            </el-button>
            <el-button
              type="primary"
              size="small"
              :disabled="!hasBatchSelection"
              @click="openBatchShipDialog"
            >
              批量发货
            </el-button>
            <el-tooltip content="刷新订单列表" placement="top">
              <el-button circle @click="refreshOrders" :loading="loading">
                <el-icon><Refresh /></el-icon>
              </el-button>
            </el-tooltip>
          </div>
        </div>
      </template>

      <!-- 任务组D：订单统计数据展示 -->
      <div class="statistics-section">
        <el-card shadow="hover" class="statistics-card">
          <template #header>
            <div class="statistics-header">
              <span class="statistics-title">订单统计</span>
              <el-date-picker
                v-model="statisticsDateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="YYYY-MM-DD"
                @change="handleStatisticsDateChange"
                size="small"
                style="width: 300px;"
              />
              <el-button type="primary" size="small" @click="loadStatistics">查询</el-button>
            </div>
          </template>
          
          <!-- 概览卡片 -->
          <div v-if="orderStatistics" class="overview-cards">
            <div class="overview-card">
              <div class="overview-label">总订单数</div>
              <div class="overview-value">{{ orderStatistics.overview?.total_orders || 0 }}</div>
            </div>
            <div class="overview-card">
              <div class="overview-label">总金额</div>
              <div class="overview-value">¥{{ (orderStatistics.overview?.total_amount || 0).toFixed(2) }}</div>
            </div>
            <div class="overview-card">
              <div class="overview-label">待付款</div>
              <div class="overview-value">{{ orderStatistics.overview?.pending_payment || 0 }}</div>
            </div>
            <div class="overview-card">
              <div class="overview-label">待发货</div>
              <div class="overview-value">{{ orderStatistics.overview?.pending_shipment || 0 }}</div>
            </div>
            <div class="overview-card">
              <div class="overview-label">待收货</div>
              <div class="overview-value">{{ orderStatistics.overview?.pending_receipt || 0 }}</div>
            </div>
            <div class="overview-card">
              <div class="overview-label">已完成</div>
              <div class="overview-value">{{ orderStatistics.overview?.completed || 0 }}</div>
            </div>
            <div class="overview-card">
              <div class="overview-label">已取消</div>
              <div class="overview-value">{{ orderStatistics.overview?.cancelled || 0 }}</div>
            </div>
            <div class="overview-card">
              <div class="overview-label">已退款</div>
              <div class="overview-value">{{ orderStatistics.overview?.refunded || 0 }}</div>
            </div>
          </div>
          
          <!-- 订单趋势 -->
          <div v-if="orderStatistics?.trends" class="trend-section">
            <h3 class="section-title">订单趋势（最近7天）</h3>
            <el-table :data="orderStatistics.trends" border size="small" style="margin-top: 10px;">
              <el-table-column prop="date" label="日期" width="120" />
              <el-table-column prop="orders_count" label="订单数" width="100" />
              <el-table-column prop="amount" label="金额" width="120">
                <template #default="scope">
                  ¥{{ (scope.row.amount || 0).toFixed(2) }}
                </template>
              </el-table-column>
            </el-table>
          </div>
          
          <!-- 状态分布 -->
          <div v-if="orderStatistics?.status_distribution" class="status-distribution-section">
            <h3 class="section-title">订单状态分布</h3>
            <el-table :data="orderStatistics.status_distribution" border size="small" style="margin-top: 10px;">
              <el-table-column prop="status_name" label="状态" width="120" />
              <el-table-column prop="count" label="数量" width="100" />
              <el-table-column prop="percentage" label="占比" width="120">
                <template #default="scope">
                  {{ scope.row.percentage }}%
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-card>
      </div>

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
          <el-form-item label="排序">
            <el-select v-model="sortKey" placeholder="默认排序" clearable>
              <el-option label="下单时间-最新优先" value="timeDesc" />
              <el-option label="下单时间-最早优先" value="timeAsc" />
              <el-option label="金额-从高到低" value="amountDesc" />
              <el-option label="金额-从低到高" value="amountAsc" />
            </el-select>
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
          @selection-change="handleSelectionChange"
          v-loading="loading"
          :data="orderList"
          style="width: 100%"
          border
          stripe
        >
          <el-table-column
            type="selection"
            width="55"
            :selectable="row => row.status === 1 && row.refund_status !== 1"
          />
          <el-table-column prop="id" label="订单编号" min-width="180" show-overflow-tooltip>
            <template #default="scope">
              <el-link type="primary" @click="viewOrderDetail(scope.row.id)">{{ scope.row.id }}</el-link>
            </template>
          </el-table-column>
          <el-table-column prop="tea_name" label="商品名称" min-width="180" show-overflow-tooltip />
          <el-table-column prop="spec_name" label="规格" min-width="120" show-overflow-tooltip />
          <el-table-column prop="price" label="单价" min-width="100">
            <template #default="scope">
              ¥{{ (scope.row.price || 0).toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" min-width="80" />
          <el-table-column prop="total_amount" label="总金额" min-width="120">
            <template #default="scope">
              ¥{{ (scope.row.total_amount || 0).toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="订单状态" min-width="120">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)" effect="light">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
              <el-tag
                v-if="scope.row.refund_status === 1"
                type="warning"
                effect="light"
                style="margin-left: 5px"
              >
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
                  v-if="scope.row.refund_status !== 1"
                  type="primary" 
                  size="small" 
                  @click="shipOrder(scope.row)"
                >
                  发货
                </el-button>
                <el-button 
                  v-if="scope.row.refund_status === 1"
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

    <!-- 批量发货对话框 -->
    <el-dialog
      v-model="batchShipDialogVisible"
      title="批量发货"
      width="500px"
    >
      <el-form :model="batchShipForm" label-width="120px">
        <el-form-item label="已选订单">
          <span>共 {{ selectedOrderIds.length }} 笔订单</span>
        </el-form-item>
        <el-form-item label="物流公司" required>
          <el-select v-model="batchShipForm.company" placeholder="请选择物流公司" style="width: 100%">
            <el-option label="顺丰速运" value="顺丰速运" />
            <el-option label="中通快递" value="中通快递" />
            <el-option label="圆通快递" value="圆通快递" />
            <el-option label="韵达快递" value="韵达快递" />
            <el-option label="申通快递" value="申通快递" />
            <el-option label="邮政EMS" value="邮政EMS" />
          </el-select>
        </el-form-item>
        <el-form-item label="统一单号" required>
          <el-input v-model="batchShipForm.trackingNumber" placeholder="请输入物流单号（可按内部规则编码）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="batchShipDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="confirmBatchShip">确认批量发货</el-button>
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
        <el-descriptions-item label="申请时间">{{ refundDetail?.apply_time || formatRefundApplyTime() }}</el-descriptions-item>
        <el-descriptions-item label="退款原因">
          {{ refundDetail?.reason || currentRefundOrder?.refund_reason || '未提供原因' }}
        </el-descriptions-item>
        <el-descriptions-item v-if="refundDetail?.reject_reason" label="拒绝原因">
          {{ refundDetail.reject_reason }}
        </el-descriptions-item>
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

    <!-- 任务组E：导出订单对话框 -->
    <el-dialog
      v-model="exportDialogVisible"
      title="导出订单"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="exportForm" label-width="120px">
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="exportForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="exportForm.status" placeholder="全部状态" clearable style="width: 100%">
            <el-option label="待付款" :value="0" />
            <el-option label="待发货" :value="1" />
            <el-option label="待收货" :value="2" />
            <el-option label="已完成" :value="3" />
            <el-option label="已取消" :value="4" />
            <el-option label="已退款" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="导出格式" required>
          <el-radio-group v-model="exportForm.format">
            <el-radio value="csv">CSV格式</el-radio>
            <el-radio value="excel">Excel格式</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="exportDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="exporting" @click="confirmExport">
            确认导出
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { showByCode, isSuccess } from '@/utils/apiMessages'
import { orderPromptMessages } from '@/utils/promptMessages'

/**
 * 订单权限控制说明（P2-6：订单权限控制的完整性）
 * 
 * 权限规则：
 * 1. 管理员(role=1)：
 *    - 可以查看所有订单（不进行shopId过滤）
 *    - 可以管理所有订单（发货、退款处理等）
 *    - 路由权限：meta.roles: [ROLES.ADMIN, ROLES.SHOP]
 * 
 * 2. 商家(role=3)：
 *    - 只能查看自己店铺的订单（后端通过shopId过滤）
 *    - 只能管理自己店铺的订单（发货、退款处理等）
 *    - 路由权限：meta.roles: [ROLES.ADMIN, ROLES.SHOP]
 *    - 前端需要从Vuex获取当前商家的shopId，并在API请求中附加
 * 
 * 3. 普通用户(role=2)：
 *    - 不能访问订单管理页面（路由守卫已拦截）
 *    - 只能访问 /order/list 查看自己的订单
 *    - 路由权限：meta.roles: [ROLES.USER, ROLES.SHOP]
 * 
 * 权限验证层级：
 * - 路由守卫：验证用户角色，无权限时重定向
 * - 前端组件：根据角色显示/隐藏功能按钮
 * - API请求：后端根据userId和shopId进行数据过滤
 * - 后端Controller：必须验证token中的用户信息，确保数据安全
 */

export default {
  name: 'OrderManagePage',
  setup() {
    const router = useRouter()
    const route = useRoute()
    const store = useStore()
    const loading = ref(false)
    const submitting = ref(false)
    
    // 分页相关（由 Vuex 维护）
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
    
    // 批量发货
    const batchShipDialogVisible = ref(false)
    const selectedOrderIds = ref([])
    const batchShipForm = reactive({
      company: '',
      trackingNumber: ''
    })
    const hasBatchSelection = ref(false)
    
    // 排序
    const sortKey = ref('')
    
    // 退款申请处理相关
    const refundDialogVisible = ref(false)
    const currentRefundOrder = ref(null)
    const refundDetail = ref(null)
    
    // 任务组D：订单统计数据相关
    const statisticsDateRange = ref([])
    const orderStatistics = computed(() => store.state.order.orderStatistics)
    
    // 任务组E：导出订单相关
    const exportDialogVisible = ref(false)
    const exporting = ref(false)
    const exportForm = reactive({
      dateRange: [],
      status: '',
      format: 'csv'
    })
    
    // 列表数据来自 Vuex
    const orderList = ref([])
    
    // 从后端获取订单列表（同步 Vuex filters）
    const fetchOrders = async () => {
      loading.value = true
      try {
        // 解析时间范围
        let startDate = ''
        let endDate = ''
        if (searchForm.dateRange && searchForm.dateRange.length === 2) {
          startDate = searchForm.dateRange[0] || ''
          endDate = searchForm.dateRange[1] || ''
        }
        // 解析排序
        let sortBy = ''
        let sortOrder = ''
        if (sortKey.value === 'timeDesc') {
          sortBy = 'create_time'
          sortOrder = 'desc'
        } else if (sortKey.value === 'timeAsc') {
          sortBy = 'create_time'
          sortOrder = 'asc'
        } else if (sortKey.value === 'amountDesc') {
          sortBy = 'total_amount'
          sortOrder = 'desc'
        } else if (sortKey.value === 'amountAsc') {
          sortBy = 'total_amount'
          sortOrder = 'asc'
        }
        // 更新 Vuex中的筛选条件
        const filters = {
          status: searchForm.status,
          startDate,
          endDate,
          keyword: searchForm.orderId || searchForm.teaName || '',
          sortBy,
          sortOrder
        }
        await store.dispatch('order/updateFilters', filters)
        const data = await store.dispatch('order/fetchOrders', {
          page: currentPage.value,
          size: pageSize.value
        })
        orderList.value = data.list || store.state.order.orderList || []
        const p = store.state.order.pagination
        total.value = p.total
        currentPage.value = p.currentPage
        pageSize.value = p.pageSize
      } finally {
        loading.value = false
      }
    }
    
    // 多选变更
    const handleSelectionChange = (rows) => {
      const ids = rows
        .filter(row => row.status === 1 && row.refund_status !== 1)
        .map(row => row.id)
      selectedOrderIds.value = ids
      hasBatchSelection.value = ids.length > 0
    }
    
    // 刷新订单列表
    const refreshOrders = () => {
      fetchOrders()
    }
    
    // 将筛选条件同步到 URL query
    const syncQueryToRoute = () => {
      // 解析时间范围
      let startDate = ''
      let endDate = ''
      if (searchForm.dateRange && searchForm.dateRange.length === 2) {
        startDate = searchForm.dateRange[0] || ''
        endDate = searchForm.dateRange[1] || ''
      }
      router.replace({
        query: {
          ...route.query,
          page: String(currentPage.value),
          orderId: searchForm.orderId || undefined,
          teaName: searchForm.teaName || undefined,
          status: searchForm.status !== '' ? String(searchForm.status) : undefined,
          startDate: startDate || undefined,
          endDate: endDate || undefined,
          sort: sortKey.value || undefined
        }
      })
    }
    
    // 搜索订单
    const handleSearch = () => {
      currentPage.value = 1
      syncQueryToRoute()
      fetchOrders()
    }
    
    // 重置搜索条件
    const resetSearch = () => {
      searchForm.orderId = ''
      searchForm.teaName = ''
      searchForm.status = ''
      searchForm.dateRange = []
      sortKey.value = ''
      currentPage.value = 1
      syncQueryToRoute()
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
      syncQueryToRoute()
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
    
    // 打开退款详情对话框（从后端获取退款详情）
    const openRefundDetail = async (order) => {
      currentRefundOrder.value = order
      refundDetail.value = null
      refundDialogVisible.value = true
      
      // 从后端获取退款详情
      try {
        const res = await store.dispatch('order/fetchRefundDetail', order.id)
        showByCode(res?.code)
        refundDetail.value = res?.data || res
      } catch (e) {
        console.error('获取退款详情失败:', e)
        // 如果获取失败，使用订单中的基本信息作为后备
        refundDetail.value = {
          reason: order.refund_reason || '',
          apply_time: order.update_time || ''
        }
      }
    }
    
    // 格式化退款申请时间（后备方法）
    const formatRefundApplyTime = () => {
      if (!currentRefundOrder.value) return ''
      return currentRefundOrder.value.update_time || ''
    }
    
    // 确认发货（走 Vuex + API）
    const confirmShip = async () => {
      if (!currentOrder.value) {
        orderPromptMessages.showOrderNotFound()
        return
      }
      
      // 表单验证
      if (!shipForm.company) {
        orderPromptMessages.showLogisticsCompanyRequired()
        return
      }
      
      if (!shipForm.trackingNumber) {
        orderPromptMessages.showLogisticsNumberRequired()
        return
      }
      
      submitting.value = true
      try {
        const res = await store.dispatch('order/shipOrder', {
          id: currentOrder.value.id,
          logisticsCompany: shipForm.company,
          logisticsNumber: shipForm.trackingNumber
        })
        showByCode(res?.code)
        shipDialogVisible.value = false
        // 成功后刷新列表，确保状态与后端一致
        await fetchOrders()
      } catch (e) {
        console.error('发货失败:', e)
      } finally {
        submitting.value = false
      }
    }
    
    // 打开批量发货对话框
    const openBatchShipDialog = () => {
      if (!hasBatchSelection.value) {
        orderPromptMessages.showShipSelectionRequired()
        return
      }
      batchShipForm.company = ''
      batchShipForm.trackingNumber = ''
      batchShipDialogVisible.value = true
    }
    
    // 批量发货确认
    const confirmBatchShip = async () => {
      if (!hasBatchSelection.value) {
        orderPromptMessages.showShipSelectionRequired()
        return
      }
      if (!batchShipForm.company) {
        orderPromptMessages.showLogisticsCompanyRequired()
        return
      }
      if (!batchShipForm.trackingNumber) {
        orderPromptMessages.showLogisticsNumberRequired()
        return
      }
      
      submitting.value = true
      try {
        const res = await store.dispatch('order/batchShipOrders', {
          orderIds: selectedOrderIds.value,
          logisticsCompany: batchShipForm.company,
          logisticsNumber: batchShipForm.trackingNumber
        })
        showByCode(res?.code)
        batchShipDialogVisible.value = false
        // 刷新列表
        await fetchOrders()
      } catch (e) {
        console.error('批量发货失败:', e)
      } finally {
        submitting.value = false
      }
    }
    
    // 处理退款申请（走 Vuex + API）
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
      ).then(async () => {
        submitting.value = true
        try {
          const res = await store.dispatch('order/processRefund', {
            orderId: order.id,
            approve: isApproved,
            reason: refundDetail.value?.reason || order.refund_reason || ''
          })
          showByCode(res?.code)
          refundDialogVisible.value = false
          refundDetail.value = null
          // 重新加载订单列表，保持与后端数据一致
          await fetchOrders()
        } catch (e) {
          console.error('处理退款失败:', e)
        } finally {
          submitting.value = false
        }
      }).catch(() => {
        // 用户取消操作
      })
    }
    
    // 获取收货人信息（当前阶段：仅显示占位，后续接入真实地址数据）
    const getReceiverInfo = () => {
      if (!currentOrder.value) return '无收货信息'
      return currentOrder.value.receiver || '无收货信息'
    }
    
    // 任务组D：加载订单统计数据
    const loadStatistics = async () => {
      try {
        const params = {}
        if (statisticsDateRange.value && statisticsDateRange.value.length === 2) {
          params.startDate = statisticsDateRange.value[0]
          params.endDate = statisticsDateRange.value[1]
        }
        const res = await store.dispatch('order/fetchOrderStatistics', params)
        showByCode(res?.code)
      } catch (error) {
        console.error('加载统计数据失败:', error)
      }
    }
    
    // 任务组D：统计日期范围变更处理
    const handleStatisticsDateChange = () => {
      // 日期变更时自动重新加载统计数据
      loadStatistics()
    }
    
    // 任务组E：打开导出对话框
    const openExportDialog = () => {
      exportForm.dateRange = []
      exportForm.status = ''
      exportForm.format = 'csv'
      exportDialogVisible.value = true
    }
    
    // 任务组E：确认导出
    const confirmExport = async () => {
      if (!exportForm.format) {
        orderPromptMessages.showSelectExportFormat()
        return
      }
      
      exporting.value = true
      try {
        const params = {
          format: exportForm.format
        }
        
        if (exportForm.dateRange && exportForm.dateRange.length === 2) {
          params.startDate = exportForm.dateRange[0]
          params.endDate = exportForm.dateRange[1]
        }
        
        if (exportForm.status !== '') {
          params.status = exportForm.status
        }
        
        const res = await store.dispatch('order/exportOrders', params)
        showByCode(res?.code)
        exportDialogVisible.value = false
      } catch (error) {
        console.error('导出失败:', error)
      } finally {
        exporting.value = false
      }
    }
    
    // 初始化：从 URL 恢复筛选条件
    onMounted(() => {
      const { page, orderId, teaName, status, startDate, endDate, sort } = route.query
      if (page) {
        currentPage.value = Number(page) || 1
      }
      if (orderId) {
        searchForm.orderId = String(orderId)
      }
      if (teaName) {
        searchForm.teaName = String(teaName)
      }
      if (status !== undefined) {
        const s = Number(status)
        searchForm.status = Number.isNaN(s) ? '' : s
      }
      if (startDate && endDate) {
        searchForm.dateRange = [String(startDate), String(endDate)]
      }
      if (sort) {
        sortKey.value = String(sort)
      }
      fetchOrders()
      // 任务组D：加载统计数据
      loadStatistics()
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
      batchShipDialogVisible,
      batchShipForm,
      selectedOrderIds,
      hasBatchSelection,
      refundDialogVisible,
      currentRefundOrder,
      Refresh,
      handleSelectionChange,
      openBatchShipDialog,
      confirmBatchShip,
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
      getReceiverInfo,
      // 任务组D：统计数据相关
      statisticsDateRange,
      orderStatistics,
      loadStatistics,
      handleStatisticsDateChange,
      // 任务组E：导出相关
      exportDialogVisible,
      exportForm,
      exporting,
      openExportDialog,
      confirmExport,
      // 排序
      sortKey,
      refundDetail
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

// 任务组D：统计数据样式
.statistics-section {
  margin-bottom: 20px;
}

.statistics-card {
  .statistics-header {
    display: flex;
    align-items: center;
    gap: 15px;
    
    .statistics-title {
      font-size: 16px;
      font-weight: bold;
    }
  }
  
  .overview-cards {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 15px;
    margin-bottom: 20px;
    
    .overview-card {
      padding: 15px;
      background: #f5f7fa;
      border-radius: 4px;
      text-align: center;
      
      .overview-label {
        font-size: 14px;
        color: #606266;
        margin-bottom: 8px;
      }
      
      .overview-value {
        font-size: 24px;
        font-weight: bold;
        color: #303133;
      }
    }
  }
  
  .trend-section,
  .status-distribution-section {
    margin-top: 20px;
    
    .section-title {
      font-size: 16px;
      font-weight: bold;
      margin-bottom: 10px;
      color: #303133;
    }
  }
}

@media (max-width: 1200px) {
  .statistics-card .overview-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .statistics-card {
    .statistics-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 10px;
    }
    
    .overview-cards {
      grid-template-columns: 1fr;
    }
  }
}
</style> 