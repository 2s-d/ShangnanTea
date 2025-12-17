# 店铺模块 (Shop Module) 任务分解

## 文档信息

| 项目 | 内容 |
|------|------|
| 模块名称 | 店铺模块 (Shop Module) |
| 当前完成度 | 65% |
| 目标完成度 | 100% |
| 预计工时 | 2天 |
| 优先级 | 中 |
| 负责人 | - |
| 创建日期 | 2024-12-17 |
| 最后更新 | 2024-12-17 |

---

## 一、总体概览

### 1.1 模块职责

店铺模块负责处理所有与店铺相关的功能，包括：
- 店铺展示（列表、详情、搜索）
- 店铺管理（基本信息、装修、公告）
- 店铺茶叶管理（商家管理自己店铺的茶叶）
- 店铺统计数据（销售数据展示）

### 1.2 涉及文件

| 类型 | 文件路径 | 说明 |
|------|----------|------|
| Vuex | `src/store/modules/shop.js` | 店铺状态管理 |
| API | `src/api/shop.js` | 店铺API接口 |
| 常量 | `src/api/apiConstants.js` | API端点定义 |
| 页面 | `src/views/shop/list/ShopListPage.vue` | 店铺列表 |
| 页面 | `src/views/shop/detail/ShopDetailPage.vue` | 店铺详情 |
| 页面 | `src/views/shop/manage/ShopManagePage.vue` | 店铺管理(商家) |

### 1.3 已完成功能

- [x] 获取店铺列表
- [x] 获取店铺详情
- [x] 获取我的店铺（商家）
- [x] 更新店铺基本信息
- [x] 获取店铺茶叶列表
- [x] 店铺列表UI展示
- [x] 店铺详情页UI展示
- [x] 店铺管理页UI框架

### 1.4 待完成功能

- [ ] 店铺搜索功能
- [ ] 店铺Banner管理
- [ ] 店铺公告管理
- [ ] 店铺统计数据
- [ ] 店铺Logo上传
- [ ] 店铺茶叶管理完善

---

## 二、具体实施

### 2.1 任务拆分

#### 任务组A：店铺搜索功能 (优先级: 高)

| 任务ID | 任务描述 | 预计时间 | 状态 |
|--------|----------|----------|------|
| A-1 | 在shop.js API中添加搜索方法 | 15min | ⬜ 待开始 |
| A-2 | 在shop.js Vuex中添加searchKeyword状态 | 10min | ⬜ 待开始 |
| A-3 | 实现searchShops action | 20min | ⬜ 待开始 |
| A-4 | 在ShopListPage.vue中添加搜索框 | 25min | ⬜ 待开始 |
| A-5 | 实现搜索结果展示 | 20min | ⬜ 待开始 |
| A-6 | 测试搜索功能 | 15min | ⬜ 待开始 |

**任务组A详细实施步骤**：

```javascript
// A-1: API方法
export function searchShops(params) {
  // params: { keyword, sortBy, sortOrder, page, pageSize }
  return request({
    url: API.SHOP.LIST,
    method: 'get',
    params
  })
}

// A-2~A-3: Vuex实现
const state = () => ({
  // ... 现有状态
  searchKeyword: '',
  searchResults: []
})

// mutations
SET_SEARCH_KEYWORD(state, keyword) {
  state.searchKeyword = keyword
}

SET_SEARCH_RESULTS(state, results) {
  state.searchResults = results
}

// actions
async searchShops({ commit }, params) {
  commit('SET_LOADING', true)
  commit('SET_SEARCH_KEYWORD', params.keyword || '')
  try {
    const res = await searchShops(params)
    commit('SET_SEARCH_RESULTS', res.data.list || [])
    commit('SET_PAGINATION', res.data.pagination)
    return res.data
  } finally {
    commit('SET_LOADING', false)
  }
}
```

---

#### 任务组B：店铺装修功能 (优先级: 中)

| 任务ID | 任务描述 | 预计时间 | 状态 |
|--------|----------|----------|------|
| B-1 | 在shop.js API中添加Banner管理方法 | 25min | ⬜ 待开始 |
| B-2 | 在shop.js API中添加公告管理方法 | 20min | ⬜ 待开始 |
| B-3 | 在shop.js Vuex中添加shopBanners状态 | 10min | ⬜ 待开始 |
| B-4 | 在shop.js Vuex中添加shopAnnouncements状态 | 10min | ⬜ 待开始 |
| B-5 | 实现fetchShopBanners action | 20min | ⬜ 待开始 |
| B-6 | 实现updateShopBanners action | 25min | ⬜ 待开始 |
| B-7 | 实现fetchShopAnnouncements action | 20min | ⬜ 待开始 |
| B-8 | 实现createShopAnnouncement action | 20min | ⬜ 待开始 |
| B-9 | 实现deleteShopAnnouncement action | 15min | ⬜ 待开始 |
| B-10 | 在ShopManagePage.vue中添加Banner管理UI | 45min | ⬜ 待开始 |
| B-11 | 在ShopManagePage.vue中添加公告管理UI | 40min | ⬜ 待开始 |
| B-12 | 测试装修功能 | 20min | ⬜ 待开始 |

**任务组B详细实施步骤**：

```javascript
// B-1: Banner API
export function getShopBanners(shopId) {
  return request({
    url: `${API.SHOP.DETAIL}${shopId}/banners`,
    method: 'get'
  })
}

export function updateShopBanners(shopId, banners) {
  // banners: [{ imageUrl, linkUrl, sort }]
  return request({
    url: `${API.SHOP.DETAIL}${shopId}/banners`,
    method: 'put',
    data: { banners }
  })
}

// B-2: 公告API
export function getShopAnnouncements(shopId) {
  return request({
    url: `${API.SHOP.DETAIL}${shopId}/announcements`,
    method: 'get'
  })
}

export function createShopAnnouncement(shopId, data) {
  // data: { title, content, isTop }
  return request({
    url: `${API.SHOP.DETAIL}${shopId}/announcements`,
    method: 'post',
    data
  })
}

export function deleteShopAnnouncement(shopId, announcementId) {
  return request({
    url: `${API.SHOP.DETAIL}${shopId}/announcements/${announcementId}`,
    method: 'delete'
  })
}

// B-3~B-9: Vuex实现
const state = () => ({
  // ... 现有状态
  shopBanners: [],
  shopAnnouncements: []
})

// mutations
SET_SHOP_BANNERS(state, banners) {
  state.shopBanners = banners
}

SET_SHOP_ANNOUNCEMENTS(state, announcements) {
  state.shopAnnouncements = announcements
}

ADD_ANNOUNCEMENT(state, announcement) {
  state.shopAnnouncements.unshift(announcement)
}

REMOVE_ANNOUNCEMENT(state, id) {
  state.shopAnnouncements = state.shopAnnouncements.filter(a => a.id !== id)
}
```

---

#### 任务组C：店铺统计数据 (优先级: 中)

| 任务ID | 任务描述 | 预计时间 | 状态 |
|--------|----------|----------|------|
| C-1 | 在shop.js API中添加统计数据方法 | 20min | ⬜ 待开始 |
| C-2 | 在shop.js Vuex中添加statistics状态 | 10min | ⬜ 待开始 |
| C-3 | 实现fetchShopStatistics action | 25min | ⬜ 待开始 |
| C-4 | 在ShopManagePage.vue中添加统计展示 | 45min | ⬜ 待开始 |
| C-5 | 添加简单的数据图表展示 | 40min | ⬜ 待开始 |
| C-6 | 测试统计功能 | 15min | ⬜ 待开始 |

**任务组C详细实施步骤**：

```javascript
// C-1: API方法
export function getShopStatistics(shopId, params) {
  // params: { startDate, endDate, type: 'overview' | 'sales' | 'visitors' }
  return request({
    url: API.SHOP.STATISTICS,
    method: 'get',
    params: { shopId, ...params }
  })
}

// C-2~C-3: Vuex实现
const state = () => ({
  // ... 现有状态
  shopStatistics: {
    overview: {
      totalSales: 0,
      totalOrders: 0,
      totalProducts: 0,
      totalVisitors: 0
    },
    salesTrend: [],
    topProducts: []
  }
})

// mutations
SET_SHOP_STATISTICS(state, stats) {
  state.shopStatistics = { ...state.shopStatistics, ...stats }
}

// actions
async fetchShopStatistics({ commit, state }, params = {}) {
  const shopId = state.myShop?.id
  if (!shopId) return
  
  try {
    const res = await getShopStatistics(shopId, params)
    commit('SET_SHOP_STATISTICS', res.data)
    return res.data
  } catch (error) {
    console.error('获取店铺统计数据失败', error)
  }
}
```

---

#### 任务组D：店铺Logo上传 (优先级: 低)

| 任务ID | 任务描述 | 预计时间 | 状态 |
|--------|----------|----------|------|
| D-1 | 在shop.js API中添加Logo上传方法 | 20min | ⬜ 待开始 |
| D-2 | 实现uploadShopLogo action | 25min | ⬜ 待开始 |
| D-3 | 在ShopManagePage.vue中添加Logo上传组件 | 35min | ⬜ 待开始 |
| D-4 | 添加图片裁剪功能（可选） | 45min | ⬜ 待开始 |
| D-5 | 测试Logo上传功能 | 15min | ⬜ 待开始 |

**任务组D详细实施步骤**：

```javascript
// D-1: API方法
export function uploadShopLogo(shopId, file) {
  const formData = new FormData()
  formData.append('logo', file)
  formData.append('shopId', shopId)
  
  return request({
    url: `${API.SHOP.DETAIL}${shopId}/logo`,
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// D-2: Vuex action
async uploadShopLogo({ commit, state }, file) {
  const shopId = state.myShop?.id
  if (!shopId) throw new Error('店铺信息不存在')
  
  try {
    const res = await uploadShopLogo(shopId, file)
    // 更新店铺Logo
    commit('SET_MY_SHOP', { 
      ...state.myShop, 
      logo: res.data.logoUrl 
    })
    return res.data.logoUrl
  } catch (error) {
    throw error
  }
}
```

---

#### 任务组E：店铺茶叶管理完善 (优先级: 高)

| 任务ID | 任务描述 | 预计时间 | 状态 |
|--------|----------|----------|------|
| E-1 | 完善fetchShopTeas action支持分页和筛选 | 25min | ⬜ 待开始 |
| E-2 | 实现addShopTea action（调用tea模块） | 20min | ⬜ 待开始 |
| E-3 | 实现updateShopTea action | 20min | ⬜ 待开始 |
| E-4 | 实现deleteShopTea action | 15min | ⬜ 待开始 |
| E-5 | 在ShopManagePage.vue中完善茶叶管理UI | 50min | ⬜ 待开始 |
| E-6 | 添加茶叶添加/编辑弹窗 | 45min | ⬜ 待开始 |
| E-7 | 测试茶叶管理功能 | 20min | ⬜ 待开始 |

**任务组E详细实施步骤**：

```javascript
// E-1: 完善API调用
async fetchShopTeas({ commit, state }, params = {}) {
  const shopId = state.myShop?.id || params.shopId
  if (!shopId) return
  
  commit('SET_LOADING', true)
  try {
    const res = await getShopTeas(shopId, {
      page: params.page || 1,
      pageSize: params.pageSize || 10,
      keyword: params.keyword || '',
      status: params.status
    })
    commit('SET_SHOP_TEAS', res.data.list || [])
    commit('SET_PAGINATION', res.data.pagination)
    return res.data
  } finally {
    commit('SET_LOADING', false)
  }
}

// E-2~E-4: 商家茶叶管理（调用tea API但通过shop模块管理）
// 注意：实际的茶叶CRUD使用tea模块的API，但状态保存在shop模块
import { addTea, updateTea, deleteTea } from '@/api/tea'

async addShopTea({ commit, state, dispatch }, teaData) {
  const shopId = state.myShop?.id
  if (!shopId) throw new Error('店铺信息不存在')
  
  try {
    const res = await addTea({ ...teaData, shopId })
    // 刷新店铺茶叶列表
    await dispatch('fetchShopTeas')
    return res.data
  } catch (error) {
    throw error
  }
}
```

---

### 2.2 Mock数据设计

#### 店铺搜索Mock数据
```javascript
const mockShopSearchResults = [
  {
    id: 1,
    shopName: '商南茶庄',
    logo: '/shops/shop1.jpg',
    description: '专营正宗商南茶叶，品质保证',
    rating: 4.8,
    salesCount: 1256,
    productCount: 45,
    createTime: '2023-06-15'
  }
  // ...更多店铺
]
```

#### Banner Mock数据
```javascript
const mockShopBanners = [
  {
    id: 1,
    shopId: 1,
    imageUrl: '/banners/shop1-banner1.jpg',
    linkUrl: '/tea/101',
    sort: 1,
    status: 1
  }
  // ...更多Banner
]
```

#### 公告Mock数据
```javascript
const mockAnnouncements = [
  {
    id: 1,
    shopId: 1,
    title: '春茶上新通知',
    content: '2024年春茶已经上架，欢迎选购...',
    isTop: true,
    createTime: '2024-03-01 10:00:00'
  }
  // ...更多公告
]
```

#### 统计Mock数据
```javascript
const mockStatistics = {
  overview: {
    totalSales: 125680,
    totalOrders: 856,
    totalProducts: 45,
    totalVisitors: 12580
  },
  salesTrend: [
    { date: '2024-01-01', sales: 3500, orders: 28 },
    { date: '2024-01-02', sales: 4200, orders: 35 }
    // ...更多数据
  ],
  topProducts: [
    { id: 101, name: '特级绿茶', sales: 35600, orders: 245 }
    // ...更多产品
  ]
}
```

---

### 2.3 开发顺序

```
开发顺序建议：E → A → B → C → D

理由：
1. 店铺茶叶管理完善(E)是商家核心功能，优先完成
2. 店铺搜索(A)是用户常用功能，次优先
3. 店铺装修(B)是商家经营需要的功能
4. 统计数据(C)是运营参考功能
5. Logo上传(D)相对独立，最后完成
```

---

## 三、检查测试

### 3.1 单元测试检查清单

#### 店铺搜索测试
- [ ] 关键词搜索成功
- [ ] 空关键词返回全部
- [ ] 搜索结果分页正确
- [ ] 搜索结果排序正确

#### Banner管理测试
- [ ] 获取Banner列表成功
- [ ] 更新Banner成功
- [ ] Banner排序正确
- [ ] Banner图片验证

#### 公告管理测试
- [ ] 获取公告列表成功
- [ ] 创建公告成功
- [ ] 删除公告成功
- [ ] 置顶公告排序正确

#### 统计数据测试
- [ ] 获取概览数据成功
- [ ] 获取销售趋势数据成功
- [ ] 日期范围筛选正确

#### 茶叶管理测试
- [ ] 获取店铺茶叶列表成功
- [ ] 添加茶叶成功
- [ ] 更新茶叶成功
- [ ] 删除茶叶成功
- [ ] 筛选和分页正确

### 3.2 集成测试检查清单

- [ ] 店铺详情页正确加载店铺信息
- [ ] 店铺管理页面权限控制正确
- [ ] 商家只能管理自己的店铺
- [ ] 店铺茶叶与茶叶模块数据同步

### 3.3 UI交互测试清单

- [ ] 搜索框输入响应正常
- [ ] Banner轮播展示正确
- [ ] 公告列表展示正确
- [ ] 统计数据图表展示正确
- [ ] 茶叶管理表格交互正常
- [ ] Logo上传预览正确

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
| A-店铺搜索 | 6 | 0 | 0% | ⬜ 未开始 |
| B-店铺装修 | 12 | 0 | 0% | ⬜ 未开始 |
| C-统计数据 | 6 | 0 | 0% | ⬜ 未开始 |
| D-Logo上传 | 5 | 0 | 0% | ⬜ 未开始 |
| E-茶叶管理 | 7 | 0 | 0% | ⬜ 未开始 |

### 5.2 里程碑

| 里程碑 | 目标日期 | 实际完成 | 状态 |
|--------|----------|----------|------|
| 茶叶管理完善 | - | - | ⬜ 未开始 |
| 搜索功能完成 | - | - | ⬜ 未开始 |
| 店铺装修完成 | - | - | ⬜ 未开始 |
| 模块100%完成 | - | - | ⬜ 未开始 |

---

**文档版本**: 1.0  
**最后更新**: 2024-12-17

