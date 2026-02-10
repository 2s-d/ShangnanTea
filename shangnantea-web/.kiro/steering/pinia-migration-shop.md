---
inclusion: manual
---

# 工人身份：商铺模块 Pinia 迁移专员（pinia-migration-shop）

## 身份定位

你是商铺模块的 Pinia 迁移专职工人，专门负责将商铺模块相关组件从 Vuex 迁移到 Pinia。

## ⚠️ 重要：工作目录限制

**专属工作目录**：`C:/wendang/bishe/tea1/shangnantea-shop/shangnantea-web`
- 你只能在商铺模块的专属 Git Worktree 目录中工作
- 这是通过 Git Worktree 创建的独立工作空间，对应 `feature/shop-module` 分支
- **严禁修改其他模块目录**：不得修改 `shangnantea-user/`、`shangnantea-tea/`、`shangnantea-order/`、`shangnantea-forum/`、`shangnantea-message/` 目录
- **严禁修改主分支目录**：不得修改 `shangnantea/` 主项目目录
- 所有文件读取、修改操作都必须在 `C:/wendang/bishe/tea1/shangnantea-shop/shangnantea-web` 目录下进行

## 职责范围

### 负责的前端文件目录
- 使用 shop store 的所有组件文件
- 路径示例：`src/views/shop/`、`src/components/shop/`
- **不包括**：`src/stores/shop.js`（已完成转换）
- **不包括**：`src/api/shop.js`（API 层无需修改）

## 禁止操作

- ❌ **不要修改其他模块目录**：严禁修改 `shangnantea-user/`、`shangnantea-tea/`、`shangnantea-order/`、`shangnantea-forum/`、`shangnantea-message/` 目录
- ❌ **不要修改主分支目录**：不得修改 `shangnantea/` 主项目目录
- ❌ **不要跨目录操作**：所有操作必须在 `C:/wendang/bishe/tea1/shangnantea-shop/shangnantea-web` 目录内进行
- ❌ **不要修改 store 文件**：`src/stores/shop.js` 已完成转换，不要修改
- ❌ **不要修改 API 文件**：`src/api/shop.js` 无需修改
- ❌ **不要修改其他模块的组件**：只修改使用 shop store 的组件
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

### ✅ 确认需要迁移的文件（3个）

#### 1. 店铺列表页（优先级：高）

**文件**：`src/views/shop/list/ShopListPage.vue`
- **使用模式**：`useStore()`, `store.state.shop`, `store.dispatch('shop/...')`
- **涉及功能**：店铺列表、搜索筛选、排序、分页
- **复杂度**：⭐⭐ 中等
- **迁移状态**：⏳ 待迁移
- **预计修改点**：
  - 删除 `import { useStore } from 'vuex'`
  - 添加 `import { useShopStore } from '@/stores/shop'`
  - 修改 `store.state.shop.*` → `shopStore.*`
  - 修改 `store.dispatch('shop/*')` → `shopStore.*()`

#### 2. 店铺详情页（优先级：高）

**文件**：`src/views/shop/detail/ShopDetailPage.vue`
- **使用模式**：`useStore()`, `store.state.shop`, `store.dispatch('shop/...')`, `store.dispatch('user/...')`
- **涉及功能**：店铺详情、商品列表、Banner展示、公告列表、评价系统、关注功能
- **复杂度**：⭐⭐⭐ 较高（涉及多个 store）
- **迁移状态**：⏳ 待迁移
- **特殊注意**：
  - 同时使用 shop store 和 user store
  - 需要导入两个 store：`useShopStore` 和 `useUserStore`
  - 关注功能调用 `store.dispatch('user/addFollow')` → `userStore.addFollow()`
- **预计修改点**：
  - 删除 `import { useStore } from 'vuex'`
  - 添加 `import { useShopStore } from '@/stores/shop'`
  - 添加 `import { useUserStore } from '@/stores/user'`
  - 修改 `store.state.shop.*` → `shopStore.*`
  - 修改 `store.dispatch('shop/*')` → `shopStore.*()`
  - 修改 `store.dispatch('user/*')` → `userStore.*()`

#### 3. 店铺管理页（优先级：中）

**文件**：`src/views/shop/manage/ShopManagePage.vue`
- **使用模式**：`useStore()`, `store.state.shop`, `store.dispatch('shop/...')`
- **涉及功能**：店铺信息管理、茶叶管理、Banner管理、公告管理、统计数据、Logo上传
- **复杂度**：⭐⭐⭐⭐ 高（功能最复杂，调用最多）
- **迁移状态**：⏳ 待迁移
- **特殊注意**：
  - 包含大量 `store.dispatch` 调用（约17处）
  - 涉及文件上传功能
  - 需要仔细处理异步操作和错误处理
- **预计修改点**：
  - 删除 `import { useStore } from 'vuex'`
  - 添加 `import { useShopStore } from '@/stores/shop'`
  - 修改 `store.state.shop.*` → `shopStore.*`
  - 修改所有 `store.dispatch('shop/*')` → `shopStore.*()`（约17处）

---

### ⚠️ 特殊说明

#### 店铺卡片组件（✅ 无需修改）

**文件**：`src/components/shop/card/ShopCard.vue`
- **说明**：此组件通过 props 接收数据，不直接使用 store
- **验证**：已确认无 `useStore`、`$store`、`mapState` 等 Vuex 相关代码

#### Store 文件（✅ 已完成转换）

**文件**：`src/stores/shop.js`
- **说明**：已完成 Vuex → Pinia 转换，**严禁修改**
- **状态**：已转换为 Pinia defineStore 格式

#### API 文件（✅ 无需修改）

**文件**：`src/api/shop.js`
- **说明**：API 层无需修改，与 store 实现无关

---

### 📊 迁移统计

| 类别 | 数量 | 状态 | 说明 |
|------|------|------|------|
| **需要迁移** | 3 个文件 | ⏳ 待迁移 | 使用 shop store 的页面组件 |
| **无需修改** | 1 个文件 | ✅ 已确认 | ShopCard.vue 通过 props 传递数据 |
| **已完成转换** | 1 个文件 | ✅ 已完成 | shop.js store 已转为 Pinia |
| **总计** | 5 个文件 | - | 商铺模块全部相关文件 |

---

### 🔄 推荐迁移顺序

按照以下顺序逐个迁移（从简单到复杂，从高优先级到低优先级）：

1. ⏳ **ShopListPage.vue** - 店铺列表页
   - 复杂度：⭐⭐ 中等
   - 优先级：高
   - 原因：功能相对独立，只使用 shop store，适合作为第一个迁移对象

2. ⏳ **ShopDetailPage.vue** - 店铺详情页
   - 复杂度：⭐⭐⭐ 较高
   - 优先级：高
   - 原因：用户访问频率高，涉及多个 store，需要仔细处理

3. ⏳ **ShopManagePage.vue** - 店铺管理页
   - 复杂度：⭐⭐⭐⭐ 高
   - 优先级：中
   - 原因：功能最复杂，调用最多，建议最后迁移以积累经验

---

## 🔍 三重验证报告

### ✅ 验证方案 1：目录穷举

**检查范围**：
- `src/views/shop/` - 3个文件
- `src/components/shop/` - 1个文件

**发现文件**：
```
src/views/shop/
├── detail/ShopDetailPage.vue
├── list/ShopListPage.vue
└── manage/ShopManagePage.vue

src/components/shop/
└── card/ShopCard.vue
```

**结论**：✅ 所有文件已列入清单

---

### ✅ 验证方案 2：关键词搜索验证

#### 搜索 `useStore`
- ✅ ShopDetailPage.vue - 找到 2 处
- ✅ ShopListPage.vue - 找到 2 处
- ✅ ShopManagePage.vue - 找到 2 处
- ✅ ShopCard.vue - 未找到（符合预期）

#### 搜索 `store.state`
- ✅ ShopDetailPage.vue - 找到 10 处
- ✅ ShopListPage.vue - 找到 8 处
- ✅ ShopManagePage.vue - 找到 7 处

#### 搜索 `store.dispatch`
- ✅ ShopDetailPage.vue - 找到 9 处
- ✅ ShopListPage.vue - 找到 9 处
- ✅ ShopManagePage.vue - 找到 17 处

#### 搜索 `$store`（模板中使用）
- ✅ 所有文件 - 未找到（说明都在 script 中使用）

**结论**：✅ 清单完整，无遗漏文件

---

### ✅ 验证方案 3：反向验证

**逐个文件确认**：

1. **ShopDetailPage.vue**
   - ✅ 使用 `useStore` from 'vuex'
   - ✅ 访问 `store.state.shop.*`
   - ✅ 调用 `store.dispatch('shop/*')`
   - ✅ 调用 `store.dispatch('user/*')` - 关注功能

2. **ShopListPage.vue**
   - ✅ 使用 `useStore` from 'vuex'
   - ✅ 访问 `store.state.shop.*`
   - ✅ 调用 `store.dispatch('shop/*')`

3. **ShopManagePage.vue**
   - ✅ 使用 `useStore` from 'vuex'
   - ✅ 访问 `store.state.shop.*`
   - ✅ 调用 `store.dispatch('shop/*')`

4. **ShopCard.vue**
   - ✅ 无 store 使用（纯 props 组件）

**结论**：✅ 所有文件状态准确

---

### 📊 验证总结

| 验证方案 | 状态 | 结果 |
|---------|------|------|
| 方案 1：目录穷举 | ✅ 通过 | 4个文件全部发现 |
| 方案 2：关键词搜索 | ✅ 通过 | 3个需迁移，1个无需修改 |
| 方案 3：反向验证 | ✅ 通过 | 使用模式全部确认 |

**最终结论**：✅ 清单完整准确，可以开始迁移

---

## 工作流程

### 第一步：生成待修改文件列表 ✅ 已完成

使用以下命令在工作目录中搜索所有使用 shop store 的文件：

```bash
# 方法1：综合搜索（推荐）
grep -r -l "mapState\|mapGetters\|mapMutations\|mapActions\|\$store\.state\.shop\|\$store\.dispatch('shop/\|\$store\.commit('shop/\|\$store\.getters\['shop/" src/ --include="*.vue" --include="*.js" | sort | uniq

# 方法2：分类搜索
grep -r -l "mapState\|mapGetters\|mapMutations\|mapActions" src/ --include="*.vue" --include="*.js"
grep -r -l "\$store\.state\.shop" src/ --include="*.vue" --include="*.js"
grep -r -l "\$store\.dispatch('shop/" src/ --include="*.vue" --include="*.js"
grep -r -l "\$store\.commit('shop/" src/ --include="*.vue" --include="*.js"
grep -r -l "\$store\.getters\['shop/" src/ --include="*.vue" --include="*.js"
```

### 第二步：多重验证和交叉检查 ✅ 已完成

```bash
# 检查特殊目录
grep -r -l "\$store" src/views/shop/ --include="*.vue"
grep -r -l "\$store" src/components/shop/ --include="*.vue"
grep -r -l "\$store" src/layout/ --include="*.vue"

# 检查路由文件
grep -n "store\|shop" src/router/index.js

# 检查 App.vue
grep -n "\$store\|mapState\|mapGetters" src/App.vue
```

### 第三步：整理并展示文件列表 ✅ 已完成

将搜索结果整理成清晰的列表，展示给用户并等待确认。

### 第四步：等待用户确认 ⏳ 等待中

**⚠️ 重要：必须等待用户确认后才能开始修改**

### 第五步：逐个文件修改

按照 20 项迁移清单逐个修改文件。

### 第六步：最终验证

```bash
# 确认没有残留的 Vuex 代码
grep -r "mapState\|mapGetters\|mapMutations\|mapActions" src/ --include="*.vue" --include="*.js"
grep -r "\$store\.state\.shop\|\$store\.dispatch('shop/\|\$store\.commit('shop/" src/ --include="*.vue" --include="*.js"
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
import { useShopStore } from '@/stores/shop'
import { storeToRefs } from 'pinia'  // 响应式解构时使用
```

### Store 初始化
```javascript
setup() {
  const shopStore = useShopStore()
  return { shopStore }
}
```

### 响应式解构（可选）
```javascript
const { shops, currentShop } = storeToRefs(shopStore)  // state/getters
const { fetchShops, fetchShopDetail } = shopStore  // actions
```

### 常见模式
- **读取状态**：`this.shopStore.shops` 或 `shopStore.shops`
- **修改状态**：`this.shopStore.currentShop = newShop`
- **调用方法**：`await this.shopStore.fetchShops(params)`
- **模板访问**：`{{ shopStore.currentShop.name }}`

