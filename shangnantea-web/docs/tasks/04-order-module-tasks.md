# 订单模块 (Order Module) 任务分解

## 文档信息

| 项目 | 内容 |
|------|------|
| 模块名称 | 订单模块 (Order Module) |
| 当前完成度 | 85% |
| 目标完成度 | 100% |
| 预计工时 | 1天 |
| 优先级 | 中 |
| 负责人 | - |
| 创建日期 | 2024-12-17 |
| 最后更新 | 2024-12-17 |

---

## 一、总体概览

### 1.1 模块职责

订单模块负责处理所有与购物和订单相关的功能，包括：
- 购物车管理（增删改查、批量操作）
- 订单流程（创建、支付、发货、收货、评价）
- 订单管理（查询、状态变更）
- 订单统计（管理员/商家）

### 1.2 涉及文件

| 类型 | 文件路径 | 说明 |
|------|----------|------|
| Vuex | `src/store/modules/order.js` | 订单状态管理 |
| API | `src/api/order.js` | 订单API接口 |
| 常量 | `src/api/apiConstants.js` | API端点定义 |
| 页面 | `src/views/order/cart/CartPage.vue` | 购物车 |
| 页面 | `src/views/order/payment/CheckoutPage.vue` | 结算页 |
| 页面 | `src/views/order/payment/PaymentPage.vue` | 支付页 |
| 页面 | `src/views/order/list/OrderListPage.vue` | 订单列表 |
| 页面 | `src/views/order/detail/OrderDetailPage.vue` | 订单详情 |
| 页面 | `src/views/order/manage/OrderManagePage.vue` | 订单管理(管理员/商家) |

### 1.3 已完成功能

- [x] 购物车CRUD操作
- [x] 创建订单
- [x] 获取订单列表
- [x] 获取订单详情
- [x] 支付订单
- [x] 取消订单
- [x] 确认收货
- [x] 评价订单
- [x] 购物车UI
- [x] 结算流程UI
- [x] 订单列表UI
- [x] 订单详情UI

### 1.4 待完成功能

- [ ] 订单统计数据（管理员/商家）
- [ ] 订单导出功能（管理员/商家）
- [ ] 订单发货操作（商家/管理员）
- [ ] 订单退款处理
- [ ] 批量订单操作

---

## 二、具体实施

### 2.1 任务拆分

#### 任务组A：订单统计数据 (优先级: 高)

| 任务ID | 任务描述 | 预计时间 | 状态 |
|--------|----------|----------|------|
| A-1 | 在order.js API中添加统计数据方法 | 25min | ⬜ 待开始 |
| A-2 | 在order.js Vuex中添加statistics状态 | 10min | ⬜ 待开始 |
| A-3 | 添加统计相关mutations | 10min | ⬜ 待开始 |
| A-4 | 实现fetchOrderStatistics action | 25min | ⬜ 待开始 |
| A-5 | 在OrderManagePage.vue中添加统计展示区域 | 40min | ⬜ 待开始 |
| A-6 | 添加时间范围筛选功能 | 25min | ⬜ 待开始 |
| A-7 | 测试统计功能 | 15min | ⬜ 待开始 |

**任务组A详细实施步骤**：

```javascript
// A-1: API方法
export function getOrderStatistics(params) {
  // params: { 
  //   startDate, endDate, 
  //   shopId?, // 商家传自己的shopId，管理员不传或传'platform'
  //   type: 'overview' | 'trend' | 'status' 
  // }
  return request({
    url: '/order/statistics',
    method: 'get',
    params
  })
}

// A-2~A-4: Vuex实现
const state = () => ({
  // ... 现有状态
  orderStatistics: {
    overview: {
      totalOrders: 0,
      totalAmount: 0,
      pendingPayment: 0,
      pendingShipment: 0,
      pendingReceipt: 0,
      completed: 0,
      cancelled: 0
    },
    trend: [],  // 订单趋势数据
    statusDistribution: {}  // 状态分布
  }
})

// mutations
SET_ORDER_STATISTICS(state, stats) {
  state.orderStatistics = { ...state.orderStatistics, ...stats }
}

// actions
async fetchOrderStatistics({ commit, rootState }, params = {}) {
  try {
    // 根据用户角色确定shopId
    const userRole = rootState.user.userInfo?.role
    const shopId = userRole === 3 ? rootState.shop.myShop?.id : undefined
    
    const res = await getOrderStatistics({ 
      ...params, 
      shopId 
    })
    commit('SET_ORDER_STATISTICS', res.data)
    return res.data
  } catch (error) {
    console.error('获取订单统计失败', error)
  }
}
```

---

#### 任务组B：订单导出功能 (优先级: 中)

| 任务ID | 任务描述 | 预计时间 | 状态 |
|--------|----------|----------|------|
| B-1 | 在order.js API中添加导出方法 | 20min | ⬜ 待开始 |
| B-2 | 实现exportOrders action | 25min | ⬜ 待开始 |
| B-3 | 在OrderManagePage.vue中添加导出按钮 | 15min | ⬜ 待开始 |
| B-4 | 实现导出参数选择弹窗 | 30min | ⬜ 待开始 |
| B-5 | 处理文件下载逻辑 | 20min | ⬜ 待开始 |
| B-6 | 测试导出功能 | 15min | ⬜ 待开始 |

**任务组B详细实施步骤**：

```javascript
// B-1: API方法
export function exportOrders(params) {
  // params: { 
  //   startDate, endDate, 
  //   status?, shopId?,
  //   format: 'excel' | 'csv'
  // }
  return request({
    url: '/order/export',
    method: 'get',
    params,
    responseType: 'blob'  // 返回文件流
  })
}

// B-2: Vuex action
async exportOrders({ rootState }, params = {}) {
  try {
    const userRole = rootState.user.userInfo?.role
    const shopId = userRole === 3 ? rootState.shop.myShop?.id : undefined
    
    const res = await exportOrders({ 
      ...params, 
      shopId 
    })
    
    // 创建下载链接
    const blob = new Blob([res], { 
      type: params.format === 'csv' 
        ? 'text/csv' 
        : 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `订单数据_${new Date().toISOString().slice(0,10)}.${params.format || 'xlsx'}`
    link.click()
    window.URL.revokeObjectURL(url)
    
    return true
  } catch (error) {
    throw error
  }
}
```

---

#### 任务组C：订单发货操作 (优先级: 高)

| 任务ID | 任务描述 | 预计时间 | 状态 |
|--------|----------|----------|------|
| C-1 | 在order.js API中添加发货方法 | 15min | ⬜ 待开始 |
| C-2 | 实现shipOrder action | 20min | ⬜ 待开始 |
| C-3 | 在OrderManagePage.vue中添加发货按钮 | 15min | ⬜ 待开始 |
| C-4 | 创建发货信息填写弹窗 | 35min | ⬜ 待开始 |
| C-5 | 实现物流信息查看功能 | 25min | ⬜ 待开始 |
| C-6 | 测试发货功能 | 15min | ⬜ 待开始 |

**任务组C详细实施步骤**：

```javascript
// C-1: API方法
export function shipOrder(orderId, data) {
  // data: { 
  //   expressCompany, // 快递公司
  //   expressNo,      // 快递单号
  //   remark?         // 备注
  // }
  return request({
    url: `/order/${orderId}/ship`,
    method: 'post',
    data
  })
}

export function getOrderLogistics(orderId) {
  return request({
    url: `/order/${orderId}/logistics`,
    method: 'get'
  })
}

// C-2: Vuex action
async shipOrder({ commit, state }, { orderId, shipData }) {
  try {
    await shipOrder(orderId, shipData)
    
    // 更新订单列表中的状态
    const list = state.orderList.map(order => 
      order.id === orderId 
        ? { ...order, status: 2, ...shipData } // 状态变为待收货
        : order
    )
    commit('SET_ORDER_LIST', list)
    
    // 如果是当前订单，也更新
    if (state.currentOrder?.id === orderId) {
      commit('SET_CURRENT_ORDER', { 
        ...state.currentOrder, 
        status: 2, 
        ...shipData 
      })
    }
    
    return true
  } catch (error) {
    throw error
  }
}
```

---

#### 任务组D：订单退款处理 (优先级: 中)

| 任务ID | 任务描述 | 预计时间 | 状态 |
|--------|----------|----------|------|
| D-1 | 在order.js API中添加退款相关方法 | 25min | ⬜ 待开始 |
| D-2 | 实现requestRefund action（用户发起） | 20min | ⬜ 待开始 |
| D-3 | 实现processRefund action（商家/管理员处理） | 25min | ⬜ 待开始 |
| D-4 | 在OrderDetailPage.vue中添加申请退款入口 | 20min | ⬜ 待开始 |
| D-5 | 在OrderManagePage.vue中添加退款处理功能 | 35min | ⬜ 待开始 |
| D-6 | 创建退款申请弹窗 | 30min | ⬜ 待开始 |
| D-7 | 测试退款功能 | 20min | ⬜ 待开始 |

**任务组D详细实施步骤**：

```javascript
// D-1: API方法
export function requestRefund(orderId, data) {
  // data: { reason, description?, images?[] }
  return request({
    url: `/order/${orderId}/refund/request`,
    method: 'post',
    data
  })
}

export function processRefund(orderId, data) {
  // data: { 
  //   action: 'approve' | 'reject',
  //   reason?: string // 拒绝原因
  // }
  return request({
    url: `/order/${orderId}/refund/process`,
    method: 'post',
    data
  })
}

export function getRefundDetail(orderId) {
  return request({
    url: `/order/${orderId}/refund`,
    method: 'get'
  })
}

// D-2~D-3: Vuex actions
async requestRefund({ commit, state }, { orderId, refundData }) {
  try {
    await requestRefund(orderId, refundData)
    
    // 更新订单状态为退款中
    if (state.currentOrder?.id === orderId) {
      commit('SET_CURRENT_ORDER', { 
        ...state.currentOrder, 
        refundStatus: 'pending'
      })
    }
    
    return true
  } catch (error) {
    throw error
  }
}

async processRefund({ commit, state }, { orderId, action, reason }) {
  try {
    await processRefund(orderId, { action, reason })
    
    // 更新订单状态
    const newStatus = action === 'approve' ? 5 : state.currentOrder?.status
    const refundStatus = action === 'approve' ? 'approved' : 'rejected'
    
    if (state.currentOrder?.id === orderId) {
      commit('SET_CURRENT_ORDER', { 
        ...state.currentOrder, 
        status: newStatus,
        refundStatus
      })
    }
    
    return true
  } catch (error) {
    throw error
  }
}
```

---

#### 任务组E：批量订单操作 (优先级: 低)

| 任务ID | 任务描述 | 预计时间 | 状态 |
|--------|----------|----------|------|
| E-1 | 在order.js API中添加批量操作方法 | 20min | ⬜ 待开始 |
| E-2 | 实现batchShipOrders action | 20min | ⬜ 待开始 |
| E-3 | 实现batchCancelOrders action | 15min | ⬜ 待开始 |
| E-4 | 在OrderManagePage.vue中添加批量操作UI | 35min | ⬜ 待开始 |
| E-5 | 添加全选和部分选择功能 | 20min | ⬜ 待开始 |
| E-6 | 测试批量操作功能 | 15min | ⬜ 待开始 |

**任务组E详细实施步骤**：

```javascript
// E-1: API方法
export function batchShipOrders(data) {
  // data: { orderIds[], shipData: { expressCompany, expressNo } }
  return request({
    url: '/order/batch/ship',
    method: 'post',
    data
  })
}

export function batchCancelOrders(orderIds) {
  return request({
    url: '/order/batch/cancel',
    method: 'post',
    data: { orderIds }
  })
}

// E-2~E-3: Vuex actions
async batchShipOrders({ commit, state }, { orderIds, shipData }) {
  try {
    await batchShipOrders({ orderIds, shipData })
    
    // 批量更新订单状态
    const list = state.orderList.map(order => 
      orderIds.includes(order.id) 
        ? { ...order, status: 2, ...shipData }
        : order
    )
    commit('SET_ORDER_LIST', list)
    
    return true
  } catch (error) {
    throw error
  }
}
```

---

### 2.2 Mock数据设计

#### 统计Mock数据
```javascript
const mockOrderStatistics = {
  overview: {
    totalOrders: 1256,
    totalAmount: 358900,
    pendingPayment: 45,
    pendingShipment: 89,
    pendingReceipt: 123,
    completed: 956,
    cancelled: 43
  },
  trend: [
    { date: '2024-01-01', orders: 45, amount: 12500 },
    { date: '2024-01-02', orders: 52, amount: 14800 }
    // ...更多数据
  ],
  statusDistribution: {
    0: 45,   // 待付款
    1: 89,   // 待发货
    2: 123,  // 待收货
    3: 956,  // 已完成
    4: 43,   // 已取消
    5: 0     // 已退款
  }
}
```

#### 物流Mock数据
```javascript
const mockLogistics = {
  orderId: 1001,
  expressCompany: '顺丰速运',
  expressNo: 'SF1234567890',
  status: 'in_transit',
  traces: [
    { time: '2024-01-15 14:30', description: '快件已从西安发出' },
    { time: '2024-01-15 18:00', description: '快件到达商洛中转站' },
    { time: '2024-01-16 09:00', description: '快件正在派送中' }
  ]
}
```

#### 退款Mock数据
```javascript
const mockRefund = {
  id: 1,
  orderId: 1001,
  reason: '商品质量问题',
  description: '收到的茶叶有异味',
  images: ['/refunds/r1-1.jpg'],
  status: 'pending', // pending, approved, rejected
  createTime: '2024-01-16 10:00:00',
  processTime: null,
  processRemark: null
}
```

---

### 2.3 开发顺序

```
开发顺序建议：A → C → D → B → E

理由：
1. 订单统计(A)是管理者日常需要的核心功能
2. 订单发货(C)是商家必须的操作功能
3. 退款处理(D)是完整订单流程的必要环节
4. 订单导出(B)是数据分析的辅助功能
5. 批量操作(E)是效率提升功能，最后完成
```

---

## 三、检查测试

### 3.1 单元测试检查清单

#### 统计功能测试
- [ ] 获取概览数据成功
- [ ] 获取趋势数据成功
- [ ] 时间范围筛选正确
- [ ] 商家只能看自己店铺数据
- [ ] 管理员能看平台数据

#### 导出功能测试
- [ ] Excel导出成功
- [ ] CSV导出成功
- [ ] 文件下载正确
- [ ] 数据内容正确

#### 发货功能测试
- [ ] 发货操作成功
- [ ] 物流信息保存正确
- [ ] 订单状态更新正确
- [ ] 物流信息查询成功

#### 退款功能测试
- [ ] 申请退款成功
- [ ] 退款申请参数验证
- [ ] 审批退款成功
- [ ] 拒绝退款成功
- [ ] 退款状态更新正确

#### 批量操作测试
- [ ] 批量发货成功
- [ ] 批量取消成功
- [ ] 部分失败处理正确

### 3.2 集成测试检查清单

- [ ] 订单统计与订单列表数据一致
- [ ] 发货后订单状态正确更新
- [ ] 退款后订单状态正确更新
- [ ] 导出数据与列表数据一致

### 3.3 UI交互测试清单

- [ ] 统计图表展示正确
- [ ] 发货弹窗表单验证
- [ ] 退款弹窗表单验证
- [ ] 导出进度提示
- [ ] 批量选择交互正常

---

## 四、错误总结

> 此部分将在开发过程中持续更新，记录遇到的问题和解决方案。

### 4.1 已解决问题

| 日期 | 问题描述 | 解决方案 | 相关任务 |
|------|----------|----------|----------|
| - | - | - | - |

### 4.2 待解决问题

| 日期 | 问题描述 | 优先级 | 状态 |
|------|----------|--------|------|
| - | - | - | - |

### 4.3 经验教训

| 日期 | 教训描述 | 影响范围 |
|------|----------|----------|
| - | - | - |

---

## 五、进度追踪

### 5.1 任务组完成度

| 任务组 | 总任务数 | 已完成 | 完成率 | 状态 |
|--------|----------|--------|--------|------|
| A-订单统计 | 7 | 0 | 0% | ⬜ 未开始 |
| B-订单导出 | 6 | 0 | 0% | ⬜ 未开始 |
| C-订单发货 | 6 | 0 | 0% | ⬜ 未开始 |
| D-退款处理 | 7 | 0 | 0% | ⬜ 未开始 |
| E-批量操作 | 6 | 0 | 0% | ⬜ 未开始 |

### 5.2 里程碑

| 里程碑 | 目标日期 | 实际完成 | 状态 |
|--------|----------|----------|------|
| 统计功能完成 | - | - | ⬜ 未开始 |
| 发货功能完成 | - | - | ⬜ 未开始 |
| 退款功能完成 | - | - | ⬜ 未开始 |
| 模块100%完成 | - | - | ⬜ 未开始 |

---

**文档版本**: 1.0  
**最后更新**: 2024-12-17

