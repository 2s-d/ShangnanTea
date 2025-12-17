# 茶叶模块 (Tea Module) 任务分解

## 文档信息

| 项目 | 内容 |
|------|------|
| 模块名称 | 茶叶模块 (Tea Module) |
| 当前完成度 | 55% |
| 目标完成度 | 100% |
| 预计工时 | 2-3天 |
| 优先级 | 高 |
| 负责人 | - |
| 创建日期 | 2024-12-17 |
| 最后更新 | 2024-12-17 |

---

## 一、总体概览

### 1.1 模块职责

茶叶模块负责处理所有与茶叶商品相关的功能，包括：
- 茶叶分类管理（查看、管理员管理）
- 茶叶商品管理（增删改查、上下架）
- 茶叶商城功能（搜索、筛选、排序、推荐）
- 茶叶评价系统（查看、提交、回复）
- 茶叶规格与图片管理

### 1.2 涉及文件

| 类型 | 文件路径 | 说明 |
|------|----------|------|
| Vuex | `src/store/modules/tea.js` | 茶叶状态管理 |
| API | `src/api/tea.js` | 茶叶API接口 |
| 常量 | `src/api/apiConstants.js` | API端点定义 |
| 页面 | `src/views/tea/list/TeaListPage.vue` | 茶叶列表 |
| 页面 | `src/views/tea/detail/TeaDetailPage.vue` | 茶叶详情 |
| 页面 | `src/views/tea/manage/TeaManagePage.vue` | 茶叶管理(管理员) |

### 1.3 已完成功能

- [x] 获取茶叶列表（支持分页）
- [x] 获取茶叶详情
- [x] 获取茶叶分类列表
- [x] 添加茶叶（管理员/商家）
- [x] 更新茶叶信息
- [x] 删除茶叶
- [x] 基础筛选和排序
- [x] 茶叶列表UI展示
- [x] 茶叶详情页UI展示

### 1.4 待完成功能

- [ ] 茶叶评价系统（获取、提交、回复）
- [ ] 茶叶规格管理
- [ ] 茶叶图片管理
- [ ] 茶叶上下架操作
- [ ] 茶叶搜索优化
- [ ] 茶叶推荐功能

---

## 二、具体实施

### 2.1 任务拆分

#### 任务组A：茶叶评价系统 (优先级: 高)

| 任务ID | 任务描述 | 预计时间 | 状态 |
|--------|----------|----------|------|
| A-1 | 在apiConstants.js中添加评价API端点 | 10min | ⬜ 待开始 |
| A-2 | 在tea.js API中添加评价相关方法 | 40min | ⬜ 待开始 |
| A-3 | 在tea.js Vuex中添加reviews状态 | 15min | ⬜ 待开始 |
| A-4 | 添加评价相关mutations | 15min | ⬜ 待开始 |
| A-5 | 实现fetchTeaReviews action | 25min | ⬜ 待开始 |
| A-6 | 实现submitTeaReview action | 25min | ⬜ 待开始 |
| A-7 | 实现replyToReview action（商家） | 25min | ⬜ 待开始 |
| A-8 | 在TeaDetailPage.vue中添加评价列表组件 | 45min | ⬜ 待开始 |
| A-9 | 创建评价提交弹窗组件 | 40min | ⬜ 待开始 |
| A-10 | 在订单完成后触发评价入口 | 30min | ⬜ 待开始 |
| A-11 | 测试评价功能 | 25min | ⬜ 待开始 |

**任务组A详细实施步骤**：

```javascript
// A-1: apiConstants.js 添加
TEA: {
  // ... 现有端点
  REVIEWS: '/tea/reviews',
  REVIEW_DETAIL: '/tea/reviews/',
  REVIEW_REPLY: '/tea/reviews/reply'
}

// A-2: API方法
export function getTeaReviews(teaId, params) {
  return request({
    url: API.TEA.REVIEWS,
    method: 'get',
    params: { teaId, ...params }
  })
}

export function submitTeaReview(data) {
  // data: { teaId, orderId, rating, content, images[] }
  return request({
    url: API.TEA.REVIEWS,
    method: 'post',
    data
  })
}

export function replyToReview(reviewId, content) {
  return request({
    url: API.TEA.REVIEW_REPLY,
    method: 'post',
    data: { reviewId, content }
  })
}

// A-3~A-7: Vuex实现
const state = () => ({
  // ... 现有状态
  teaReviews: [],
  reviewPagination: {
    total: 0,
    currentPage: 1,
    pageSize: 10
  },
  reviewStats: {
    average: 0,
    count: 0,
    distribution: { 5: 0, 4: 0, 3: 0, 2: 0, 1: 0 }
  }
})

// mutations
SET_TEA_REVIEWS(state, { list, pagination, stats }) {
  state.teaReviews = list
  if (pagination) state.reviewPagination = pagination
  if (stats) state.reviewStats = stats
}

ADD_REVIEW(state, review) {
  state.teaReviews.unshift(review)
  state.reviewStats.count++
}

// actions
async fetchTeaReviews({ commit }, { teaId, page = 1, pageSize = 10 }) {
  commit('SET_LOADING', true)
  try {
    const res = await getTeaReviews(teaId, { page, pageSize })
    commit('SET_TEA_REVIEWS', {
      list: res.data.list,
      pagination: res.data.pagination,
      stats: res.data.stats
    })
    return res.data
  } finally {
    commit('SET_LOADING', false)
  }
}
```

---

#### 任务组B：茶叶规格管理 (优先级: 中)

| 任务ID | 任务描述 | 预计时间 | 状态 |
|--------|----------|----------|------|
| B-1 | 在apiConstants.js中添加规格API端点 | 10min | ⬜ 待开始 |
| B-2 | 在tea.js API中添加规格相关方法 | 30min | ⬜ 待开始 |
| B-3 | 在tea.js Vuex中添加specifications状态 | 10min | ⬜ 待开始 |
| B-4 | 添加规格相关mutations | 15min | ⬜ 待开始 |
| B-5 | 实现fetchTeaSpecifications action | 20min | ⬜ 待开始 |
| B-6 | 实现updateTeaSpecifications action | 25min | ⬜ 待开始 |
| B-7 | 在TeaDetailPage.vue中展示规格选择 | 35min | ⬜ 待开始 |
| B-8 | 在TeaManagePage.vue中添加规格管理 | 45min | ⬜ 待开始 |
| B-9 | 测试规格功能 | 20min | ⬜ 待开始 |

**任务组B详细实施步骤**：

```javascript
// B-1: apiConstants.js 添加
TEA: {
  // ... 现有端点
  SPECIFICATIONS: '/tea/specifications'
}

// B-2: API方法
export function getTeaSpecifications(teaId) {
  return request({
    url: `${API.TEA.SPECIFICATIONS}/${teaId}`,
    method: 'get'
  })
}

export function updateTeaSpecifications(teaId, specs) {
  // specs: [{ name: '250g', price: 198, stock: 100 }, ...]
  return request({
    url: `${API.TEA.SPECIFICATIONS}/${teaId}`,
    method: 'put',
    data: { specifications: specs }
  })
}

// B-3~B-6: Vuex实现
const state = () => ({
  // ... 现有状态
  currentTeaSpecs: []
})

// mutations
SET_TEA_SPECIFICATIONS(state, specs) {
  state.currentTeaSpecs = specs
}

// actions
async fetchTeaSpecifications({ commit }, teaId) {
  try {
    const res = await getTeaSpecifications(teaId)
    commit('SET_TEA_SPECIFICATIONS', res.data || [])
    return res.data
  } catch (error) {
    commit('SET_TEA_SPECIFICATIONS', [])
    throw error
  }
}
```

---

#### 任务组C：茶叶图片管理 (优先级: 中)

| 任务ID | 任务描述 | 预计时间 | 状态 |
|--------|----------|----------|------|
| C-1 | 在tea.js API中添加图片管理方法 | 25min | ⬜ 待开始 |
| C-2 | 实现uploadTeaImages action | 30min | ⬜ 待开始 |
| C-3 | 实现deleteTeaImage action | 20min | ⬜ 待开始 |
| C-4 | 实现updateImageOrder action | 20min | ⬜ 待开始 |
| C-5 | 在TeaManagePage.vue中添加图片管理 | 50min | ⬜ 待开始 |
| C-6 | 实现图片预览和排序功能 | 35min | ⬜ 待开始 |
| C-7 | 测试图片管理功能 | 20min | ⬜ 待开始 |

**任务组C详细实施步骤**：

```javascript
// C-1: API方法
export function uploadTeaImages(teaId, files) {
  const formData = new FormData()
  files.forEach(file => formData.append('images', file))
  formData.append('teaId', teaId)
  
  return request({
    url: API.TEA.IMAGES,
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function deleteTeaImage(teaId, imageId) {
  return request({
    url: `${API.TEA.IMAGES}/${imageId}`,
    method: 'delete',
    params: { teaId }
  })
}

export function updateTeaImageOrder(teaId, imageIds) {
  return request({
    url: `${API.TEA.IMAGES}/order`,
    method: 'put',
    data: { teaId, imageIds }
  })
}
```

---

#### 任务组D：茶叶上下架操作 (优先级: 中)

| 任务ID | 任务描述 | 预计时间 | 状态 |
|--------|----------|----------|------|
| D-1 | 在tea.js API中添加上下架方法 | 15min | ⬜ 待开始 |
| D-2 | 实现toggleTeaStatus action | 20min | ⬜ 待开始 |
| D-3 | 在TeaManagePage.vue中添加上下架按钮 | 25min | ⬜ 待开始 |
| D-4 | 添加批量上下架功能 | 30min | ⬜ 待开始 |
| D-5 | 测试上下架功能 | 15min | ⬜ 待开始 |

**任务组D详细实施步骤**：

```javascript
// D-1: API方法
export function toggleTeaStatus(teaId, status) {
  // status: 0-下架, 1-上架
  return request({
    url: `${API.TEA.DETAIL}${teaId}/status`,
    method: 'put',
    data: { status }
  })
}

export function batchToggleTeaStatus(teaIds, status) {
  return request({
    url: `${API.TEA.LIST}/batch-status`,
    method: 'put',
    data: { teaIds, status }
  })
}

// D-2: Vuex action
async toggleTeaStatus({ commit, state }, { teaId, status }) {
  try {
    await toggleTeaStatus(teaId, status)
    // 更新列表中的状态
    const list = state.teaList.map(tea => 
      tea.id === teaId ? { ...tea, status } : tea
    )
    commit('SET_TEA_LIST', list)
    return true
  } catch (error) {
    throw error
  }
}
```

---

#### 任务组E：搜索与推荐优化 (优先级: 低)

| 任务ID | 任务描述 | 预计时间 | 状态 |
|--------|----------|----------|------|
| E-1 | 优化搜索API支持更多条件 | 30min | ⬜ 待开始 |
| E-2 | 实现搜索历史记录功能 | 25min | ⬜ 待开始 |
| E-3 | 实现热门搜索展示 | 20min | ⬜ 待开始 |
| E-4 | 实现茶叶推荐API | 25min | ⬜ 待开始 |
| E-5 | 实现fetchRecommendTeas action | 20min | ⬜ 待开始 |
| E-6 | 在首页和详情页展示推荐茶叶 | 35min | ⬜ 待开始 |
| E-7 | 测试搜索和推荐功能 | 20min | ⬜ 待开始 |

**任务组E详细实施步骤**：

```javascript
// E-1: 优化搜索参数
export function searchTeas(params) {
  // params: { keyword, category, priceMin, priceMax, 
  //           origin, sortBy, sortOrder, page, pageSize }
  return request({
    url: API.TEA.SEARCH,
    method: 'get',
    params
  })
}

// E-4: 推荐API
export function getRecommendTeas(params) {
  // params: { teaId?, limit?, type: 'random' | 'similar' | 'popular' }
  return request({
    url: API.TEA.RECOMMEND,
    method: 'get',
    params
  })
}

// E-5: Vuex
const state = () => ({
  // ... 现有状态
  recommendTeas: [],
  searchHistory: []
})

async fetchRecommendTeas({ commit }, params = {}) {
  try {
    const res = await getRecommendTeas({ limit: 6, ...params })
    commit('SET_RECOMMEND_TEAS', res.data || [])
    return res.data
  } catch (error) {
    commit('SET_RECOMMEND_TEAS', [])
  }
}
```

---

### 2.2 Mock数据设计

#### 评价Mock数据
```javascript
const mockReviews = [
  {
    id: 1,
    teaId: 101,
    orderId: 1001,
    userId: 1,
    userInfo: {
      username: '茶香四溢',
      avatar: '/avatars/user1.jpg'
    },
    rating: 5,
    content: '茶叶很新鲜，香气浓郁，包装也很精美，下次还会购买！',
    images: ['/reviews/r1-1.jpg', '/reviews/r1-2.jpg'],
    reply: {
      content: '感谢您的支持，欢迎再次光临！',
      replyTime: '2024-01-16 10:00:00'
    },
    createTime: '2024-01-15 15:30:00',
    likeCount: 12
  }
  // ...更多评价
]

const mockReviewStats = {
  average: 4.8,
  count: 156,
  distribution: { 5: 120, 4: 25, 3: 8, 2: 2, 1: 1 }
}
```

#### 规格Mock数据
```javascript
const mockSpecifications = [
  { id: 1, teaId: 101, name: '100g袋装', price: 68, stock: 200, isDefault: false },
  { id: 2, teaId: 101, name: '250g礼盒', price: 158, stock: 150, isDefault: true },
  { id: 3, teaId: 101, name: '500g礼盒', price: 298, stock: 80, isDefault: false }
]
```

---

### 2.3 开发顺序

```
开发顺序建议：A → D → B → C → E

理由：
1. 评价系统(A)是用户购买后的重要反馈环节，优先完成
2. 上下架操作(D)是商品管理的基础功能，实现简单
3. 规格管理(B)和图片管理(C)是商品完善的功能
4. 搜索推荐优化(E)是锦上添花的功能，最后完成
```

---

## 三、检查测试

### 3.1 单元测试检查清单

#### 评价系统测试
- [ ] 获取评价列表成功
- [ ] 获取评价列表分页正确
- [ ] 评价统计数据正确
- [ ] 提交评价成功
- [ ] 提交评价参数验证
- [ ] 商家回复评价成功
- [ ] 重复评价处理

#### 规格管理测试
- [ ] 获取规格列表成功
- [ ] 更新规格成功
- [ ] 规格价格验证
- [ ] 规格库存验证

#### 图片管理测试
- [ ] 上传图片成功
- [ ] 上传图片大小限制
- [ ] 删除图片成功
- [ ] 图片排序成功

#### 上下架测试
- [ ] 上架茶叶成功
- [ ] 下架茶叶成功
- [ ] 批量上下架成功
- [ ] 状态更新正确

### 3.2 集成测试检查清单

- [ ] 茶叶详情页评价列表正确加载
- [ ] 订单完成后评价入口正确显示
- [ ] 规格选择与价格联动正确
- [ ] 商家管理页面图片管理正常
- [ ] 上下架状态在商城正确显示

### 3.3 UI交互测试清单

- [ ] 评价列表展示样式正确
- [ ] 评价提交弹窗交互流畅
- [ ] 星级评分组件工作正常
- [ ] 图片上传预览正确
- [ ] 规格选择高亮正确
- [ ] 上下架按钮状态切换正确

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
| A-评价系统 | 11 | 0 | 0% | ⬜ 未开始 |
| B-规格管理 | 9 | 0 | 0% | ⬜ 未开始 |
| C-图片管理 | 7 | 0 | 0% | ⬜ 未开始 |
| D-上下架操作 | 5 | 0 | 0% | ⬜ 未开始 |
| E-搜索推荐 | 7 | 0 | 0% | ⬜ 未开始 |

### 5.2 里程碑

| 里程碑 | 目标日期 | 实际完成 | 状态 |
|--------|----------|----------|------|
| 评价系统完成 | - | - | ⬜ 未开始 |
| 商品管理完善 | - | - | ⬜ 未开始 |
| 搜索推荐完成 | - | - | ⬜ 未开始 |
| 模块100%完成 | - | - | ⬜ 未开始 |

---

**文档版本**: 1.0  
**最后更新**: 2024-12-17

