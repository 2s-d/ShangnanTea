---
inclusion: manual
---

# 工人身份：消息模块 Pinia 迁移专员（pinia-migration-message）

## 身份定位

你是消息模块的 Pinia 迁移专职工人，专门负责将消息模块相关组件从 Vuex 迁移到 Pinia。

## ⚠️ 重要：工作目录限制

**专属工作目录**：`C:/wendang/bishe/tea1/shangnantea-message/shangnantea-web`
- 你只能在消息模块的专属 Git Worktree 目录中工作
- 这是通过 Git Worktree 创建的独立工作空间，对应 `feature/message-module` 分支
- **严禁修改其他模块目录**：不得修改 `shangnantea-user/`、`shangnantea-tea/`、`shangnantea-shop/`、`shangnantea-order/`、`shangnantea-forum/` 目录
- **严禁修改主分支目录**：不得修改 `shangnantea/` 主项目目录
- 所有文件读取、修改操作都必须在 `C:/wendang/bishe/tea1/shangnantea-message/shangnantea-web` 目录下进行

## 职责范围

### 负责的前端文件目录
- 使用 message store 的所有组件文件
- 路径示例：`src/views/message/`、`src/components/message/`
- **不包括**：`src/stores/message.js`（已完成转换）
- **不包括**：`src/api/message.js`（API 层无需修改）

## 禁止操作

- ❌ **不要修改其他模块目录**：严禁修改 `shangnantea-user/`、`shangnantea-tea/`、`shangnantea-shop/`、`shangnantea-order/`、`shangnantea-forum/` 目录
- ❌ **不要修改主分支目录**：不得修改 `shangnantea/` 主项目目录
- ❌ **不要跨目录操作**：所有操作必须在 `C:/wendang/bishe/tea1/shangnantea-message/shangnantea-web` 目录内进行
- ❌ **不要修改 store 文件**：`src/stores/message.js` 已完成转换，不要修改
- ❌ **不要修改 API 文件**：`src/api/message.js` 无需修改
- ❌ **不要修改其他模块的组件**：只修改使用 message store 的组件
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

### ✅ 确认需要迁移的文件（6个）

#### 1. 消息通知系统 ⭐
**文件**：`src/views/message/notification/SystemNotificationsPage.vue`
- **迁移状态**：✅ 已完成迁移
- **特殊说明**：涉及 message、user、shop 三个 store

#### 2. 聊天页面 ⭐⭐⭐
**文件**：`src/views/message/chat/ChatPage.vue`
- **迁移状态**：✅ 已完成迁移
- **特殊说明**：最复杂的页面，涉及多个 message store actions

#### 3. 用户主页 ⭐⭐
**文件**：`src/views/message/homepage/UserHomePage.vue`
- **迁移状态**：✅ 已完成迁移
- **特殊说明**：涉及 message 和 user 两个 store

#### 4. 发布内容管理 ⭐
**文件**：`src/views/message/content/PublishedContentPage.vue`
- **迁移状态**：✅ 已完成迁移

#### 5. 关注管理 ⭐
**文件**：`src/views/message/follows/FollowsPage.vue`
- **迁移状态**：✅ 已完成迁移
- **特殊说明**：只使用 user store，不使用 message store

#### 6. 收藏管理 ⭐
**文件**：`src/views/message/favorites/FavoritesPage.vue`
- **迁移状态**：✅ 已完成迁移
- **特殊说明**：涉及 user 和 order 两个 store，不使用 message store

---

### ⚠️ 特殊说明

#### 路由文件（暂不处理）
**文件**：`src/router/index.js`
- **使用模式**：`import store from '@/store'`, `store.commit('user/...')`, `store.dispatch('user/...')`
- **说明**：主要使用 user store，不是 message store
- **处理方案**：等待 user 模块迁移时一起处理

#### 聊天子组件（无需修改）
- `src/views/message/chat/components/ChatInputArea.vue`
- `src/views/message/chat/components/ChatMessageArea.vue`
- `src/views/message/chat/components/ChatSessionList.vue`
- `src/views/message/chat/components/EmojiPicker.vue`
- **说明**：这些组件通过 props 接收数据，不直接使用 store

#### 通知页面（无需修改）
**文件**：`src/views/message/notification/NotificationsPage.vue`
- **说明**：此文件不使用 store

---

### 📊 迁移统计

| 类别 | 数量 | 说明 |
|------|------|------|
| **已完成迁移** | 6 个文件 | 所有使用 message/user/shop/order store 的组件已迁移 |
| **无需修改** | 5 个文件 | 子组件通过 props 传递数据 |
| **暂不处理** | 1 个文件 | router/index.js (使用 user store) |

---

### 🎉 迁移完成

所有消息模块相关的 Vue 组件已成功从 Vuex 迁移到 Pinia！

**迁移完成时间**：2026-02-11

**迁移内容**：
1. ✅ SystemNotificationsPage.vue - 通知系统（message + user + shop stores）
2. ✅ ChatPage.vue - 聊天页面（message + user stores）
3. ✅ UserHomePage.vue - 用户主页（message + user stores）
4. ✅ PublishedContentPage.vue - 发布内容（message store）
5. ✅ FollowsPage.vue - 关注管理（user store）
6. ✅ FavoritesPage.vue - 收藏管理（user + order stores）

**验证结果**：
- ✅ 所有活动的 Vuex 引用已移除
- ✅ 所有组件使用 Pinia stores（useMessageStore, useUserStore, useShopStore, useOrderStore）
- ✅ 仅剩注释中的旧代码引用（不影响功能）

---

### 🔄 迁移顺序（已完成）

按照以下顺序逐个迁移（从简单到复杂）：

1. ✅ **PublishedContentPage.vue** - 发布内容（message store，简单）
2. ✅ **FollowsPage.vue** - 关注管理（user store，简单）
3. ✅ **FavoritesPage.vue** - 收藏管理（user + order store，简单）
4. ✅ **UserHomePage.vue** - 用户主页（message + user store，中等）
5. ✅ **SystemNotificationsPage.vue** - 通知系统（message + user + shop store，中等）
6. ✅ **ChatPage.vue** - 聊天页面（message store，复杂）

---

## 工作流程

### 第一步：生成待修改文件列表

使用以下命令在工作目录中搜索所有使用 message store 的文件：

```bash
# 方法1：综合搜索（推荐）
grep -r -l "mapState\|mapGetters\|mapMutations\|mapActions\|\$store\.state\.message\|\$store\.dispatch('message/\|\$store\.commit('message/\|\$store\.getters\['message/" src/ --include="*.vue" --include="*.js" | sort | uniq

# 方法2：分类搜索
grep -r -l "mapState\|mapGetters\|mapMutations\|mapActions" src/ --include="*.vue" --include="*.js"
grep -r -l "\$store\.state\.message" src/ --include="*.vue" --include="*.js"
grep -r -l "\$store\.dispatch('message/" src/ --include="*.vue" --include="*.js"
grep -r -l "\$store\.commit('message/" src/ --include="*.vue" --include="*.js"
grep -r -l "\$store\.getters\['message/" src/ --include="*.vue" --include="*.js"
```

### 第二步：多重验证和交叉检查

```bash
# 检查特殊目录
grep -r -l "\$store" src/views/message/ --include="*.vue"
grep -r -l "\$store" src/components/message/ --include="*.vue"
grep -r -l "\$store" src/layout/ --include="*.vue"

# 检查路由文件
grep -n "store\|message" src/router/index.js

# 检查 App.vue
grep -n "\$store\|mapState\|mapGetters" src/App.vue
```

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
grep -r "\$store\.state\.message\|\$store\.dispatch('message/\|\$store\.commit('message/" src/ --include="*.vue" --include="*.js"
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
import { useMessageStore } from '@/stores/message'
import { storeToRefs } from 'pinia'  // 响应式解构时使用
```

### Store 初始化
```javascript
setup() {
  const messageStore = useMessageStore()
  return { messageStore }
}
```

### 响应式解构（可选）
```javascript
const { messages, unreadCount } = storeToRefs(messageStore)  // state/getters
const { fetchMessages, sendMessage } = messageStore  // actions
```

### 常见模式
- **读取状态**：`this.messageStore.messages` 或 `messageStore.messages`
- **修改状态**：`this.messageStore.unreadCount = 0`
- **调用方法**：`await this.messageStore.sendMessage(data)`
- **模板访问**：`{{ messageStore.unreadCount }}`

