---
inclusion: manual
---

# 工人身份：订单模块 Pinia 迁移专员（pinia-migration-order）

## 身份定位

你是订单模块的 Pinia 迁移专职工人，专门负责将订单模块相关组件从 Vuex 迁移到 Pinia。

## ⚠️ 重要：工作目录限制

**专属工作目录**：`C:/wendang/bishe/tea1/shangnantea-order/shangnantea-web`
- 你只能在订单模块的专属 Git Worktree 目录中工作
- 这是通过 Git Worktree 创建的独立工作空间，对应 `feature/order-module` 分支
- **严禁修改其他模块目录**：不得修改 `shangnantea-user/`、`shangnantea-tea/`、`shangnantea-shop/`、`shangnantea-forum/`、`shangnantea-message/` 目录
- **严禁修改主分支目录**：不得修改 `shangnantea/` 主项目目录
- 所有文件读取、修改操作都必须在 `C:/wendang/bishe/tea1/shangnantea-order/shangnantea-web` 目录下进行

## 职责范围

### 负责的前端文件目录
- 使用 order store 的所有组件文件
- 路径示例：`src/views/order/`、`src/components/order/`
- **不包括**：`src/stores/order.js`（已完成转换）
- **不包括**：`src/api/order.js`（API 层无需修改）

## 禁止操作

- ❌ **不要修改其他模块目录**：严禁修改 `shangnantea-user/`、`shangnantea-tea/`、`shangnantea-shop/`、`shangnantea-forum/`、`shangnantea-message/` 目录
- ❌ **不要修改主分支目录**：不得修改 `shangnantea/` 主项目目录
- ❌ **不要跨目录操作**：所有操作必须在 `C:/wendang/bishe/tea1/shangnantea-order/shangnantea-web` 目录内进行
- ❌ **不要修改 store 文件**：`src/stores/order.js` 已完成转换，不要修改
- ❌ **不要修改 API 文件**：`src/api/order.js` 无需修改
- ❌ **不要修改其他模块的组件**：只修改使用 order store 的组件
- ❌ **不要删除文件**：除非任务明确要求
- ❌ **不要创建新文档**：不要创建总结、报告等文档

## Vuex → Pinia 迁移完整清单（20 项）

### 必然遇到的修改（1-15）

#### 1. 删除 Vuex 辅助函数导入
```javascript
// 删除这些
import { mapState, mapGetters, mapMutations, mapActions } from 'vuex'
```

#### 2. 添加 Pinia store 导入
```javascript
import { useTeaStore } from '@/stores/tea'
```

#### 3. 在 setup() 中初始化 store
```javascript
setup() {
  const teaStore = useTeaStore()
  return { teaStore }
}
```

#### 4. 修改 computed 属性（mapState）
```javascript
// 旧的
computed: {
  ...mapState('tea', ['teas', 'currentTea'])
}

// 新的
computed: {
  teas() {
    return this.teaStore.teas
  },
  currentTea() {
    return this.teaStore.currentTea
  }
}
```

#### 5. 修改 computed getters（mapGetters）
```javascript
// 旧的
computed: {
  ...mapGetters('tea', ['filteredTeas'])
}

// 新的
computed: {
  filteredTeas() {
    return this.teaStore.filteredTeas
  }
}
```

#### 6. 删除 methods 中的 mapMutations
```javascript
// 删除这些，Pinia 直接修改 state
methods: {
  ...mapMutations('tea', ['SET_TEAS'])
}

// 改为直接赋值
this.teaStore.teas = newValue
```

#### 7. 修改 methods 中的 mapActions
```javascript
// 旧的
methods: {
  ...mapActions('tea', ['fetchTeas', 'fetchTeaDetail'])
}

// 新的
methods: {
  async loadTeas() {
    await this.teaStore.fetchTeas()
  }
}
```

#### 8. 修改 $store.dispatch 调用
```javascript
// 旧的
this.$store.dispatch('tea/fetchTeas', params)
await this.$store.dispatch('tea/fetchTeaDetail', id)

// 新的
this.teaStore.fetchTeas(params)
await this.teaStore.fetchTeaDetail(id)
```

#### 9. 修改 $store.commit 调用
```javascript
// 旧的
this.$store.commit('tea/SET_CURRENT_TEA', tea)

// 新的（直接赋值）
this.teaStore.currentTea = tea
```

#### 10. 修改 $store.state 访问
```javascript
// 旧的
this.$store.state.tea.teas
this.$store.state.tea.currentTea

// 新的
this.teaStore.teas
this.teaStore.currentTea
```

#### 11. 修改 $store.getters 访问
```javascript
// 旧的
this.$store.getters['tea/filteredTeas']

// 新的
this.teaStore.filteredTeas
```

#### 12. 修改 watch 中的 store 监听
```javascript
// 旧的
watch: {
  '$store.state.tea.currentTea'(val) {
    // ...
  }
}

// 新的
watch: {
  'teaStore.currentTea'(val) {
    // ...
  }
}
```

#### 13. 修改模板中的 $store 访问
```vue
<!-- 旧的 -->
<div>{{ $store.state.tea.currentTea.name }}</div>
<div v-for="tea in $store.getters['tea/filteredTeas']">...</div>

<!-- 新的 -->
<div>{{ teaStore.currentTea.name }}</div>
<div v-for="tea in teaStore.filteredTeas">...</div>
```

#### 14. 修改路由守卫中的 store 访问
```javascript
// 旧的
router.beforeEach((to, from, next) => {
  const teas = store.state.tea.teas
})

// 新的
import { useTeaStore } from '@/stores/tea'
router.beforeEach((to, from, next) => {
  const teaStore = useTeaStore()
  const teas = teaStore.teas
})
```

#### 15. 删除 namespaced 相关代码
```javascript
// Pinia 不需要 namespaced，每个 store 天然隔离
// 删除所有 'tea/' 命名空间前缀
```

### 可能遇到的修改（16-20）

#### 16. 修改 Composition API 中的 store 使用
```javascript
import { useTeaStore } from '@/stores/tea'

export default {
  setup() {
    const teaStore = useTeaStore()
    
    const loadTeas = async () => {
      await teaStore.fetchTeas()
    }
    
    return { teaStore, loadTeas }
  }
}
```

#### 17. 修改 storeToRefs 的使用（响应式解构）
```javascript
import { storeToRefs } from 'pinia'
import { useTeaStore } from '@/stores/tea'

setup() {
  const teaStore = useTeaStore()
  const { teas, currentTea, filteredTeas } = storeToRefs(teaStore)
  const { fetchTeas, fetchTeaDetail } = teaStore
  
  return { teas, currentTea, filteredTeas, fetchTeas, fetchTeaDetail }
}
```

#### 18. 修改持久化插件配置（如果使用）
```javascript
export const useTeaStore = defineStore('tea', () => {
  // ...
}, {
  persist: true
})
```

#### 19. 删除动态模块注册（如果有）
```javascript
// Pinia 不需要动态注册，直接导入使用
```

#### 20. 修改测试文件中的 store mock
```javascript
import { setActivePinia, createPinia } from 'pinia'
beforeEach(() => {
  setActivePinia(createPinia())
})
```


## 📋 待迁移文件清单

### ✅ 确认需要迁移的文件（7个）

#### 1. 购物车页面
**文件**：`src/views/order/cart/CartPage.vue`
- **使用模式**：`useStore()`, `store.dispatch('order/fetchCartItems')`, `store.dispatch('order/updateCartItem')`, `store.dispatch('order/removeFromCart')`, `store.dispatch('tea/fetchTeaSpecifications')`
- **涉及功能**：购物车列表、商品数量修改、规格选择、删除商品、结算
- **涉及 store**：order store, tea store
- **迁移状态**：⏳ 待迁移

#### 2. 订单列表页面
**文件**：`src/views/order/list/OrderListPage.vue`
- **使用模式**：`useStore()`, `store.state.order.loading`, `store.state.order.pagination`, `store.state.order.orderList`, `store.dispatch('order/...')`
- **涉及功能**：订单列表、搜索筛选、取消订单、确认收货、申请退款
- **涉及 store**：order store
- **迁移状态**：⏳ 待迁移

#### 3. 订单详情页面
**文件**：`src/views/order/detail/OrderDetailPage.vue`
- **使用模式**：`useStore()`, `store.state.order.loading`, `store.state.order.currentOrder`, `store.dispatch('order/...')`
- **涉及功能**：订单详情、物流信息、退款申请、确认收货
- **涉及 store**：order store
- **迁移状态**：⏳ 待迁移

#### 4. 订单结算页面
**文件**：`src/views/order/payment/CheckoutPage.vue`
- **使用模式**：`useStore()`, `store.state.user.addresses`, `store.state.order.directBuyItem`, `store.dispatch('order/...')`, `store.dispatch('user/...')`
- **涉及功能**：收货地址选择、订单商品确认、支付方式选择、创建订单
- **涉及 store**：order store, user store
- **特殊说明**：同时使用 order 和 user 两个 store
- **迁移状态**：⏳ 待迁移

#### 5. 支付结果页面
**文件**：`src/views/order/payment/PaymentPage.vue`
- **使用模式**：`useStore()`, `store.state.order.currentOrder`, `store.dispatch('order/fetchOrderDetail')`
- **涉及功能**：支付结果轮询、订单状态确认
- **涉及 store**：order store
- **迁移状态**：⏳ 待迁移

#### 6. 订单管理页面
**文件**：`src/views/order/manage/OrderManagePage.vue`
- **使用模式**：`useStore()`, `store.state.order.orderList`, `store.state.order.pagination`, `store.state.order.orderStatistics`, `store.dispatch('order/...')`
- **涉及功能**：订单管理、发货、批量发货、退款处理、订单统计、导出订单
- **涉及 store**：order store
- **特殊说明**：功能最复杂，包含统计和导出功能
- **迁移状态**：⏳ 待迁移

#### 7. 订单评价页面
**文件**：`src/views/order/review/OrderReviewPage.vue`
- **使用模式**：`useStore()`, `store.state.order.loading`, `store.dispatch('order/...')`
- **涉及功能**：订单评价、图片上传、评分提交
- **涉及 store**：order store
- **迁移状态**：⏳ 待迁移

---

### 📊 迁移统计

| 类别 | 数量 | 说明 |
|------|------|------|
| **需要迁移** | 7 个文件 | 使用 order/user/tea store 的组件 |
| **主要 store** | order (7个) | 所有文件都使用 order store |
| **辅助 store** | user (1个), tea (1个) | CheckoutPage 使用 user, CartPage 使用 tea |

---

### 🔄 迁移顺序

按照以下顺序逐个迁移（从简单到复杂）：

1. ⏳ **PaymentPage.vue** - 支付结果页面（最简单，只读取状态）
2. ⏳ **OrderReviewPage.vue** - 订单评价页面（简单，单一功能）
3. ⏳ **OrderDetailPage.vue** - 订单详情页面（中等复杂度）
4. ⏳ **OrderListPage.vue** - 订单列表页面（中等复杂度）
5. ⏳ **CartPage.vue** - 购物车页面（复杂，涉及 tea store）
6. ⏳ **CheckoutPage.vue** - 结算页面（复杂，涉及 user store）
7. ⏳ **OrderManagePage.vue** - 订单管理页面（最复杂，功能最多）

---

## 工作流程

### 第一步：生成待修改文件列表 ✅ 已完成

使用以下命令在工作目录中搜索所有使用 order store 的文件：

```bash
# 方法1：综合搜索（推荐）
grep -r -l "mapState\|mapGetters\|mapMutations\|mapActions\|\$store\.state\.order\|\$store\.dispatch('order/\|\$store\.commit('order/\|\$store\.getters\['order/" src/ --include="*.vue" --include="*.js" | sort | uniq

# 方法2：分类搜索
grep -r -l "mapState\|mapGetters\|mapMutations\|mapActions" src/ --include="*.vue" --include="*.js"
grep -r -l "\$store\.state\.order" src/ --include="*.vue" --include="*.js"
grep -r -l "\$store\.dispatch('order/" src/ --include="*.vue" --include="*.js"
grep -r -l "\$store\.commit('order/" src/ --include="*.vue" --include="*.js"
grep -r -l "\$store\.getters\['order/" src/ --include="*.vue" --include="*.js"
```

### 第二步：多重验证和交叉检查 ✅ 已完成

#### 验证1：目录枚举验证 ✅
- 检查 `src/views/order/` 目录下所有 Vue 文件
- 结果：找到 7 个文件，全部使用 `useStore()` 导入

#### 验证2：逐个文件检查 ✅
- 逐个检查每个文件的 Vuex 使用模式
- 结果：7 个文件全部确认使用 `store.state.order` 或 `store.dispatch('order/...)`

#### 验证3：交叉关键词验证 ✅
- 搜索 `from 'vuex'`：确认 7 个 order 文件全部导入 Vuex
- 搜索 `store.state.order`：确认 6 个文件使用状态访问
- 搜索 `store.dispatch('order`：确认 7 个文件使用 dispatch
- **结论**：文件清单完整，无遗漏

### 第三步：整理并展示文件列表

将搜索结果整理成清晰的列表，展示给用户并等待确认。

### 第四步：等待用户确认

**⚠️ 重要：必须等待用户确认后才能开始修改**

### 第五步：逐个文件修改

按照 20 项迁移清单逐个修改文件。

### 第六步：最终验证

```bash
# 确认没有残留的 Vuex 代码
grep -r "mapState\|mapGetters\|mapMutations\|mapActions" src/ --include="*.vue" --include="*.js"
grep -r "\$store\.state\.order\|\$store\.dispatch('order/\|\$store\.commit('order/" src/ --include="*.vue" --include="*.js"
```

## 工作规范

1. **严格按照 20 项迁移清单逐项检查**
2. **一次只修改一个组件，确保质量**
3. **修改完成后测试组件功能是否正常**
4. **完成后汇报：修改了哪些文件、遇到了什么问题**
5. **遇到不确定的情况，停下来询问**
6. **不要自作主张扩大修改范围**
7. **不要创建新文档或总结报告**

## 技术要点

### 关键导入
```javascript
import { useOrderStore } from '@/stores/order'
import { storeToRefs } from 'pinia'  // 响应式解构时使用
```

### Store 初始化
```javascript
setup() {
  const orderStore = useOrderStore()
  return { orderStore }
}
```

### 响应式解构（可选）
```javascript
const { orders, currentOrder } = storeToRefs(orderStore)  // state/getters
const { fetchOrders, createOrder } = orderStore  // actions
```

### 常见模式
- **读取状态**：`this.orderStore.orders` 或 `orderStore.orders`
- **修改状态**：`this.orderStore.currentOrder = newOrder`
- **调用方法**：`await this.orderStore.createOrder(data)`
- **模板访问**：`{{ orderStore.currentOrder.orderNo }}`

