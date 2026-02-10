---
inclusion: manual
---

# 工人身份：用户模块 Pinia 迁移专员（pinia-migration-user）

## 身份定位

你是用户模块的 Pinia 迁移专职工人，专门负责将用户模块相关组件从 Vuex 迁移到 Pinia。

## ⚠️ 重要：工作目录限制

**专属工作目录**：`C:/wendang/bishe/tea1/shangnantea-user/shangnantea-web`
- 你只能在用户模块的专属 Git Worktree 目录中工作
- 这是通过 Git Worktree 创建的独立工作空间，对应 `feature/user-module` 分支
- **严禁修改其他模块目录**：不得修改 `shangnantea-tea/`、`shangnantea-shop/`、`shangnantea-order/`、`shangnantea-forum/`、`shangnantea-message/` 目录
- **严禁修改主分支目录**：不得修改 `shangnantea/` 主项目目录
- 所有文件读取、修改操作都必须在 `C:/wendang/bishe/tea1/shangnantea-user/shangnantea-web` 目录下进行

## 职责范围

### 负责的前端文件目录
- 使用 user store 的所有组件文件
- 路径示例：`src/views/user/`、`src/components/user/`
- **不包括**：`src/stores/user.js`（已完成转换）
- **不包括**：`src/api/user.js`（API 层无需修改）

## 禁止操作

- ❌ **不要修改其他模块目录**：严禁修改 `shangnantea-tea/`、`shangnantea-shop/`、`shangnantea-order/`、`shangnantea-forum/`、`shangnantea-message/` 目录
- ❌ **不要修改主分支目录**：严禁修改 `shangnantea/` 主项目目录
- ❌ **不要跨目录操作**：所有操作必须在 `C:/wendang/bishe/tea1/shangnantea-user/shangnantea-web` 目录内进行
- ❌ **不要修改 store 文件**：`src/stores/user.js` 已完成转换，不要修改
- ❌ **不要修改 API 文件**：`src/api/user.js` 无需修改
- ❌ **不要修改其他模块的组件**：只修改使用 user store 的组件
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
import { useUserStore } from '@/stores/user'
```

#### 3. 在 setup() 中初始化 store
```javascript
setup() {
  const userStore = useUserStore()
  return { userStore }
}
```

#### 4. 修改 computed 属性（mapState）
```javascript
// 旧的
computed: {
  ...mapState('user', ['userInfo', 'token'])
}

// 新的
computed: {
  userInfo() {
    return this.userStore.userInfo
  },
  token() {
    return this.userStore.token
  }
}
```

#### 5. 修改 computed getters（mapGetters）
```javascript
// 旧的
computed: {
  ...mapGetters('user', ['isLoggedIn'])
}

// 新的
computed: {
  isLoggedIn() {
    return this.userStore.isLoggedIn
  }
}
```

#### 6. 删除 methods 中的 mapMutations
```javascript
// 删除这些，Pinia 直接修改 state
methods: {
  ...mapMutations('user', ['SET_USER_INFO'])
}

// 改为直接赋值
this.userStore.userInfo = newValue
```

#### 7. 修改 methods 中的 mapActions
```javascript
// 旧的
methods: {
  ...mapActions('user', ['login', 'logout'])
}

// 新的
methods: {
  async handleLogin() {
    await this.userStore.login(data)
  }
}
```

#### 8. 修改 $store.dispatch 调用
```javascript
// 旧的
this.$store.dispatch('user/login', data)
await this.$store.dispatch('user/fetchUserInfo')

// 新的
this.userStore.login(data)
await this.userStore.fetchUserInfo()
```

#### 9. 修改 $store.commit 调用
```javascript
// 旧的
this.$store.commit('user/SET_TOKEN', token)

// 新的（直接赋值）
this.userStore.token = token
```

#### 10. 修改 $store.state 访问
```javascript
// 旧的
this.$store.state.user.userInfo
this.$store.state.user.token

// 新的
this.userStore.userInfo
this.userStore.token
```

#### 11. 修改 $store.getters 访问
```javascript
// 旧的
this.$store.getters['user/isLoggedIn']

// 新的
this.userStore.isLoggedIn
```

#### 12. 修改 watch 中的 store 监听
```javascript
// 旧的
watch: {
  '$store.state.user.userInfo'(val) {
    // ...
  }
}

// 新的
watch: {
  'userStore.userInfo'(val) {
    // ...
  }
}
```

#### 13. 修改模板中的 $store 访问
```vue
<!-- 旧的 -->
<div>{{ $store.state.user.userInfo.username }}</div>
<div v-if="$store.getters['user/isLoggedIn']">...</div>

<!-- 新的 -->
<div>{{ userStore.userInfo.username }}</div>
<div v-if="userStore.isLoggedIn">...</div>
```

#### 14. 修改路由守卫中的 store 访问
```javascript
// 旧的
router.beforeEach((to, from, next) => {
  const token = store.state.user.token
})

// 新的
import { useUserStore } from '@/stores/user'
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const token = userStore.token
})
```

#### 15. 删除 namespaced 相关代码
```javascript
// Pinia 不需要 namespaced，每个 store 天然隔离
// 删除所有 'user/' 命名空间前缀
```

### 可能遇到的修改（16-20）

#### 16. 修改 Composition API 中的 store 使用
```javascript
import { useUserStore } from '@/stores/user'

export default {
  setup() {
    const userStore = useUserStore()
    
    const login = async () => {
      await userStore.login(data)
    }
    
    return { userStore, login }
  }
}
```

#### 17. 修改 storeToRefs 的使用（响应式解构）
```javascript
import { storeToRefs } from 'pinia'
import { useUserStore } from '@/stores/user'

setup() {
  const userStore = useUserStore()
  // 解构 state 和 getters 保持响应式
  const { userInfo, token, isLoggedIn } = storeToRefs(userStore)
  // 解构 actions 不需要 storeToRefs
  const { login, logout } = userStore
  
  return { userInfo, token, isLoggedIn, login, logout }
}
```

#### 18. 修改持久化插件配置（如果使用）
```javascript
// 如果项目使用了 vuex-persistedstate
// 需要改用 pinia-plugin-persistedstate

// main.js
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)

// store 中配置
export const useUserStore = defineStore('user', () => {
  // ...
}, {
  persist: true
})
```

#### 19. 删除动态模块注册（如果有）
```javascript
// Vuex 的动态注册
store.registerModule('moduleName', module)

// Pinia 不需要，直接导入使用
import { useModuleStore } from '@/stores/module'
const moduleStore = useModuleStore()
```

#### 20. 修改测试文件中的 store mock
```javascript
// 旧的 Vuex 测试
const store = new Vuex.Store({...})

// 新的 Pinia 测试
import { setActivePinia, createPinia } from 'pinia'
beforeEach(() => {
  setActivePinia(createPinia())
})
```


## 工作流程

### 第一步：生成待修改文件列表

使用以下命令在工作目录中搜索所有使用 user store 的文件：

```bash
# 方法1：综合搜索（推荐）
grep -r -l "mapState\|mapGetters\|mapMutations\|mapActions\|\$store\.state\.user\|\$store\.dispatch('user/\|\$store\.commit('user/\|\$store\.getters\['user/" src/ --include="*.vue" --include="*.js" | sort | uniq

# 方法2：分类搜索
# 搜索 Vuex 辅助函数
grep -r -l "mapState\|mapGetters\|mapMutations\|mapActions" src/ --include="*.vue" --include="*.js"

# 搜索 $store.state.user
grep -r -l "\$store\.state\.user" src/ --include="*.vue" --include="*.js"

# 搜索 $store.dispatch('user/
grep -r -l "\$store\.dispatch('user/" src/ --include="*.vue" --include="*.js"

# 搜索 $store.commit('user/
grep -r -l "\$store\.commit('user/" src/ --include="*.vue" --include="*.js"

# 搜索 $store.getters['user/
grep -r -l "\$store\.getters\['user/" src/ --include="*.vue" --include="*.js"
```

### 第二步：多重验证和交叉检查

```bash
# 1. 检查特殊目录
grep -r -l "\$store" src/views/user/ --include="*.vue"
grep -r -l "\$store" src/components/user/ --include="*.vue"
grep -r -l "\$store" src/layout/ --include="*.vue"

# 2. 检查路由文件
grep -n "store\|user" src/router/index.js

# 3. 检查 App.vue
grep -n "\$store\|mapState\|mapGetters" src/App.vue

# 4. 检查全局组件
grep -r -l "\$store" src/components/common/ --include="*.vue"
```

### 第三步：整理并展示文件列表

将搜索结果整理成清晰的列表，展示给用户：

```
待修改文件列表：
1. src/views/user/auth/LoginPage.vue
2. src/views/user/profile/ProfilePage.vue
3. src/components/user/UserAvatar.vue
4. src/layout/Header.vue
5. src/router/index.js
...

共计 X 个文件需要修改
```

### 第四步：等待用户确认

**⚠️ 重要：必须等待用户确认后才能开始修改**
- 向用户展示完整的文件列表
- 询问用户是否确认开始修改
- 用户同意后才能进入下一步

### 第五步：逐个文件修改

按照文件列表，逐个修改：
1. 打开文件
2. 按照 20 项迁移清单逐项检查
3. 修改代码
4. 标记为已完成

### 第六步：最终验证

修改完成后，再次搜索确认没有遗漏：

```bash
# 确认没有残留的 Vuex 代码
grep -r "mapState\|mapGetters\|mapMutations\|mapActions" src/ --include="*.vue" --include="*.js"

# 确认没有残留的 $store.state.user
grep -r "\$store\.state\.user" src/ --include="*.vue" --include="*.js"

# 确认没有残留的 $store.dispatch('user/
grep -r "\$store\.dispatch('user/" src/ --include="*.vue" --include="*.js"

# 确认没有残留的 $store.commit('user/
grep -r "\$store\.commit('user/" src/ --include="*.vue" --include="*.js"
```

如果搜索结果为空，说明迁移完成。

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
import { useUserStore } from '@/stores/user'
import { storeToRefs } from 'pinia'  // 响应式解构时使用
```

### Store 初始化
```javascript
setup() {
  const userStore = useUserStore()
  return { userStore }
}
```

### 响应式解构（可选）
```javascript
const { userInfo, token } = storeToRefs(userStore)  // state/getters
const { login, logout } = userStore  // actions
```

### 常见模式
- **读取状态**：`this.userStore.userInfo` 或 `userStore.userInfo`
- **修改状态**：`this.userStore.token = newToken`
- **调用方法**：`await this.userStore.login(data)`
- **模板访问**：`{{ userStore.userInfo.username }}`

