---
inclusion: manual
---

# 工人身份：茶叶模块 Pinia 迁移专员（pinia-migration-tea）

## 身份定位

你是茶叶模块的 Pinia 迁移专职工人，专门负责将茶叶模块相关组件从 Vuex 迁移到 Pinia。

## ⚠️ 重要：工作目录限制

**专属工作目录**：`C:/wendang/bishe/tea1/shangnantea-tea/shangnantea-web`
- 你只能在茶叶模块的专属 Git Worktree 目录中工作
- 这是通过 Git Worktree 创建的独立工作空间，对应 `feature/tea-module` 分支
- **严禁修改其他模块目录**：不得修改 `shangnantea-user/`、`shangnantea-shop/`、`shangnantea-order/`、`shangnantea-forum/`、`shangnantea-message/` 目录
- **严禁修改主分支目录**：不得修改 `shangnantea/` 主项目目录
- 所有文件读取、修改操作都必须在 `C:/wendang/bishe/tea1/shangnantea-tea/shangnantea-web` 目录下进行

## 职责范围

### 负责的前端文件目录
- 使用 tea store 的所有组件文件
- 路径示例：`src/views/tea/`、`src/components/tea/`
- **不包括**：`src/stores/tea.js`（已完成转换）
- **不包括**：`src/api/tea.js`（API 层无需修改）

## 禁止操作

- ❌ **不要修改其他模块目录**：严禁修改 `shangnantea-user/`、`shangnantea-shop/`、`shangnantea-order/`、`shangnantea-forum/`、`shangnantea-message/` 目录
- ❌ **不要修改主分支目录**：严禁修改 `shangnantea/` 主项目目录
- ❌ **不要跨目录操作**：所有操作必须在 `C:/wendang/bishe/tea1/shangnantea-tea/shangnantea-web` 目录内进行
- ❌ **不要修改 store 文件**：`src/stores/tea.js` 已完成转换，不要修改
- ❌ **不要修改 API 文件**：`src/api/tea.js` 无需修改
- ❌ **不要修改其他模块的组件**：只修改使用 tea store 的组件
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

#### 1. 茶叶列表页 ✅ 已完成
**文件**：`src/views/tea/list/TeaListPage.vue`
- **使用模式**：~~`useStore()`, `store.state.tea`, `store.dispatch('tea/...')`~~ → `useTeaStore()`
- **涉及功能**：茶叶列表展示、分类筛选、价格筛选、排序、分页、热门推荐
- **迁移状态**：✅ **已完成迁移**

#### 2. 茶叶详情页 ✅ 已完成
**文件**：`src/views/tea/detail/TeaDetailPage.vue`
- **使用模式**：~~`useStore()`, `store.state.tea`, `store.state.user`, `store.dispatch('tea/...')`, `store.dispatch('user/...')`, `store.dispatch('order/...')`, `store.commit('tea/...')`~~ → `useTeaStore()`, `useUserStore()`, `useOrderStore()`
- **涉及功能**：茶叶详情展示、规格选择、评价管理、收藏、购物车、相似推荐
- **特殊说明**：同时使用 tea、user、order 三个模块的 store
- **迁移状态**：✅ **已完成迁移**

#### 3. 茶叶管理页 ✅ 已完成
**文件**：`src/views/tea/manage/TeaManagePage.vue`
- **使用模式**：~~`useStore()`, `store.state.tea`, `store.dispatch('tea/...')`, `store.commit('tea/...')`~~ → `useTeaStore()`
- **涉及功能**：茶叶增删改查、规格管理、图片管理、分类管理、批量上下架
- **迁移状态**：✅ **已完成迁移**

---

### ⚠️ 特殊说明

#### 茶叶卡片组件（无需修改）
**文件**：`src/components/tea/card/TeaCard.vue`
- **使用模式**：`useStore()`, `store.dispatch('order/addToCart')`
- **说明**：只使用 order store，不使用 tea store
- **处理方案**：无需修改，等待 order 模块迁移时一起处理

---

### 📊 迁移统计

| 类别 | 数量 | 说明 |
|------|------|------|
| **已完成迁移** | 3 个文件 | TeaListPage.vue ✅, TeaDetailPage.vue ✅, TeaManagePage.vue ✅ |
| **待迁移** | 0 个文件 | 全部完成 |
| **无需修改** | 1 个文件 | TeaCard.vue (仅使用 order store) |

---

### 🔄 迁移顺序

按照以下顺序逐个迁移（从简单到复杂）：

1. ✅ **TeaListPage.vue** - 茶叶列表页（tea store）**已完成**
2. ✅ **TeaDetailPage.vue** - 茶叶详情页（tea + user + order store）**已完成**
3. ✅ **TeaManagePage.vue** - 茶叶管理页（tea store，最复杂）**已完成**

---

## 🎉 迁移完成总结

**茶叶模块的 Pinia 迁移已全部完成！**

所有 3 个需要迁移的文件都已成功从 Vuex 迁移到 Pinia：
- ✅ TeaListPage.vue - 使用 `useTeaStore()`
- ✅ TeaDetailPage.vue - 使用 `useTeaStore()`, `useUserStore()`, `useOrderStore()`
- ✅ TeaManagePage.vue - 使用 `useTeaStore()`

迁移后的代码特点：
- 移除了所有 Vuex 相关导入（`mapState`, `mapGetters`, `mapActions`, `mapMutations`）
- 使用 Pinia 的 `useXxxStore()` 直接访问 store
- 直接修改 state（无需 mutations）
- 直接调用 actions（无需 dispatch）
- 代码更简洁、类型安全性更好

---

## 🔍 三重验证报告

### ✅ 验证方案执行

**执行方案**：方案 2（目录穷举）+ 方案 1（逐个检查）+ 方案 3（关键词交叉验证）

---

### 📁 步骤 1：目录穷举结果

#### src/views/tea/ 目录
```
src/views/tea/
├── detail/
│   └── TeaDetailPage.vue ✓
├── list/
│   └── TeaListPage.vue ✓
└── manage/
    └── TeaManagePage.vue ✓
```

#### src/components/tea/ 目录
```
src/components/tea/
└── card/
    └── TeaCard.vue ⚠️
```

**发现文件总数**：4 个 Vue 文件

---

### 🔎 步骤 2：逐个检查结果

| 文件 | 使用 useStore | 使用 tea store | 使用其他 store | 结论 |
|------|--------------|---------------|---------------|------|
| TeaDetailPage.vue | ✅ | ✅ | ✅ user, order | **需要迁移** |
| TeaListPage.vue | ✅ | ✅ | ❌ | **需要迁移** |
| TeaManagePage.vue | ✅ | ✅ | ❌ | **需要迁移** |
| TeaCard.vue | ✅ | ❌ | ✅ order | **无需修改** |

---

### 🔍 步骤 3：关键词交叉验证

#### 验证 1：`useStore from vuex`
- ✅ TeaDetailPage.vue - 第 342 行
- ✅ TeaListPage.vue - 第 176 行
- ✅ TeaManagePage.vue - 第 469 行
- ✅ TeaCard.vue - 第 41 行

#### 验证 2：`store.state.tea`
- ✅ TeaDetailPage.vue - 多处使用（loading, currentTea, teaReviews, reviewStats, categories, currentTeaSpecs, teaImages, recommendTeas）
- ✅ TeaListPage.vue - 多处使用（categories, teaList, pagination, loading, recommendTeas）
- ✅ TeaManagePage.vue - 多处使用（loading, teaList, pagination, categories, currentTeaSpecs, teaImages）
- ❌ TeaCard.vue - **未使用 tea store**

#### 验证 3：`store.dispatch('tea/`
- ✅ TeaDetailPage.vue - 使用 fetchTeaDetail, fetchTeaReviews, fetchReviewStats, fetchTeaSpecifications, fetchRecommendTeas, fetchCategories, replyReview
- ✅ TeaListPage.vue - 使用 updateFilters, resetFilters, setPage, fetchRecommendTeas, fetchCategories
- ✅ TeaManagePage.vue - 使用大量 actions（fetchCategories, updateFilters, setPage, fetchTeas, fetchTeaSpecifications, updateTea, addTea, deleteTea, toggleTeaStatus, batchToggleTeaStatus, addSpecification, updateSpecification, setDefaultSpecification, deleteSpecification, uploadTeaImages, deleteTeaImage, updateImageOrder, setMainImage, createCategory, updateCategory, deleteCategory）
- ❌ TeaCard.vue - **只使用 order/addToCart**

#### 验证 4：`store.commit('tea/`
- ✅ TeaDetailPage.vue - 使用 SET_SELECTED_SPEC
- ❌ TeaListPage.vue - 未使用 commit
- ✅ TeaManagePage.vue - 使用 SET_PAGINATION
- ❌ TeaCard.vue - 未使用 commit

---

### ✅ 验证结论

#### 需要迁移的文件（3个）✓ 确认无遗漏

1. ✅ **TeaListPage.vue** - 使用 tea store
2. ✅ **TeaDetailPage.vue** - 使用 tea + user + order store
3. ✅ **TeaManagePage.vue** - 使用 tea store

#### 无需修改的文件（1个）✓ 确认正确

1. ✅ **TeaCard.vue** - 仅使用 order store，不使用 tea store

#### 遗漏检查 ✓ 无遗漏

- ✅ 所有 tea 视图文件已检查
- ✅ 所有 tea 组件文件已检查
- ✅ 无其他隐藏的 Vue 文件
- ✅ 备份文件（.backup）已排除

---

### 📊 最终验证统计

| 验证项 | 结果 | 状态 |
|--------|------|------|
| 目录穷举完整性 | 4 个文件全部检查 | ✅ 通过 |
| 文件内容验证 | 3 个需迁移，1 个无需修改 | ✅ 通过 |
| 关键词交叉验证 | 所有 store 使用已确认 | ✅ 通过 |
| 遗漏文件检查 | 无遗漏 | ✅ 通过 |

**验证结论**：✅ **文件清单完整准确，可以开始迁移工作**

---

## 工作流程

### 第一步：生成待修改文件列表 ✅ 已完成

已通过搜索和分析确定了 3 个需要迁移的文件。

### 第二步：多重验证和交叉检查 ✅ 已完成

已确认所有文件的 store 使用情况。

### 第三步：整理并展示文件列表 ✅ 已完成

文件列表已按照标准格式添加到本文档。

### 第四步：等待用户确认 ⏳ 进行中

**⚠️ 重要：等待用户确认后才能开始修改**

### 第五步：逐个文件修改

按照 20 项迁移清单逐个修改文件。

### 第六步：最终验证

确认没有残留的 Vuex 代码。

---

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
import { useTeaStore } from '@/stores/tea'
import { storeToRefs } from 'pinia'  // 响应式解构时使用
```

### Store 初始化
```javascript
setup() {
  const teaStore = useTeaStore()
  return { teaStore }
}
```

### 响应式解构（可选）
```javascript
const { teas, currentTea, filteredTeas } = storeToRefs(teaStore)  // state/getters
const { fetchTeas, fetchTeaDetail } = teaStore  // actions
```

### 常见模式
- **读取状态**：`this.teaStore.teas` 或 `teaStore.teas`
- **修改状态**：`this.teaStore.currentTea = newTea`
- **调用方法**：`await this.teaStore.fetchTeas(params)`
- **模板访问**：`{{ teaStore.currentTea.name }}`

