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

## 📋 待迁移文件清单

### ✅ 确认需要迁移的文件（39个）

#### 1. 登录页面
**文件**：`src/views/user/auth/LoginPage.vue`
- **使用模式**：`useStore()`, `store.dispatch('user/login')`
- **涉及功能**：用户登录、记住我、忘记密码
- **迁移状态**：⏳ 待迁移

#### 2. 注册页面
**文件**：`src/views/user/auth/RegisterPage.vue`
- **使用模式**：`useStore()`, `store.dispatch('user/register')`
- **涉及功能**：用户注册、验证码发送
- **迁移状态**：⏳ 待迁移

#### 3. 重置密码页面
**文件**：`src/views/user/auth/ResetPasswordPage.vue`
- **使用模式**：`useStore()`, `store.dispatch('user/findPassword')`
- **涉及功能**：密码找回、验证码验证
- **迁移状态**：⏳ 待迁移

#### 4. 商家认证申请页面
**文件**：`src/views/user/auth/MerchantApplication.vue`
- **使用模式**：`useStore()`, `store.dispatch('user/submitShopCertification')`, `store.dispatch('user/uploadCertificationImage')`
- **涉及功能**：商家认证申请、图片上传
- **迁移状态**：⏳ 待迁移

#### 5. 个人资料页面
**文件**：`src/views/user/profile/ProfilePage.vue`
- **使用模式**：`useStore()`, `store.state.user.userInfo`, `store.getters['user/isAdmin']`, `store.dispatch('user/getUserInfo')`, `store.dispatch('user/updateUserInfo')`, `store.dispatch('user/uploadAvatar')`
- **涉及功能**：查看和编辑个人资料、上传头像、商家认证状态
- **迁移状态**：⏳ 待迁移

#### 6. 个人设置页面
**文件**：`src/views/user/settings/ProfileEditPage.vue`
- **使用模式**：`useStore()`, `store.state.user`, `store.dispatch('user/...')`
- **涉及功能**：用户偏好设置、主题配置
- **迁移状态**：⏳ 待迁移

#### 7. 地址管理页面
**文件**：`src/views/user/address/AddressPage.vue`
- **使用模式**：`useStore()`, `store.state.user.addressList`, `store.dispatch('user/fetchAddresses')`, `store.dispatch('user/addAddress')`, `store.dispatch('user/updateAddress')`, `store.dispatch('user/deleteAddress')`, `store.dispatch('user/setDefaultAddress')`
- **涉及功能**：地址列表、新增地址、编辑地址、删除地址、设置默认地址
- **迁移状态**：⏳ 待迁移

#### 8. 用户管理页面（管理员）
**文件**：`src/views/user/manage/UserManagePage.vue`
- **使用模式**：`useStore()`, `store.state.user`, `store.dispatch('user/fetchUserList')`, `store.dispatch('user/toggleUserStatus')`, `store.dispatch('user/fetchCertificationList')`, `store.dispatch('user/processCertification')`
- **涉及功能**：用户列表、用户状态管理、商家认证审核
- **迁移状态**：⏳ 待迁移

#### 9. 认证组合式函数
**文件**：`src/composables/useAuth.js`
- **使用模式**：`useStore()`, `store.state.user`, `store.getters['user/...']`, `store.dispatch('user/login')`, `store.dispatch('user/register')`, `store.dispatch('user/logout')`, `store.commit('user/CLEAR_USER')`
- **涉及功能**：登录、注册、登出、权限检查、角色判断
- **特殊说明**：核心认证逻辑，被多个组件使用
- **迁移状态**：⏳ 待迁移

#### 10. 用户主页
**文件**：`src/views/message/homepage/UserHomePage.vue`
- **使用模式**：`useStore()`, `store.state.user`
- **涉及功能**：用户资料展示、用户动态
- **特殊说明**：在 message 模块中，但使用 user store
- **迁移状态**：⏳ 待迁移

#### 11. 关注管理页面
**文件**：`src/views/message/follows/FollowsPage.vue`
- **使用模式**：`useStore()`, `store.state.user`, `store.dispatch('user/fetchFollowList')`, `store.dispatch('user/removeFollow')`
- **涉及功能**：关注列表、取消关注
- **特殊说明**：在 message 模块中，但使用 user store
- **迁移状态**：⏳ 待迁移

#### 12. 收藏管理页面
**文件**：`src/views/message/favorites/FavoritesPage.vue`
- **使用模式**：`useStore()`, `store.state.user`, `store.dispatch('user/fetchFavoriteList')`, `store.dispatch('user/removeFavorite')`
- **涉及功能**：收藏列表、取消收藏
- **特殊说明**：在 message 模块中，但使用 user store
- **迁移状态**：⏳ 待迁移

#### 13. 发布内容管理页面
**文件**：`src/views/message/content/PublishedContentPage.vue`
- **使用模式**：`useStore()`, `store.state.user`
- **涉及功能**：用户发布的帖子和评论列表
- **特殊说明**：在 message 模块中，但使用 user store
- **迁移状态**：⏳ 待迁移

#### 14. 系统通知页面
**文件**：`src/views/message/notification/SystemNotificationsPage.vue`
- **使用模式**：`useStore()`, `store.state.user`
- **涉及功能**：系统通知列表
- **特殊说明**：在 message 模块中，但使用 user store
- **迁移状态**：⏳ 待迁移

#### 15. 聊天页面
**文件**：`src/views/message/chat/ChatPage.vue`
- **使用模式**：`useStore()`, `store.state.user`, `store.state.message`
- **涉及功能**：聊天会话、消息发送
- **特殊说明**：同时使用 user store 和 message store
- **迁移状态**：⏳ 待迁移

#### 16. 导航栏组件
**文件**：`src/components/common/layout/NavBar.vue`
- **使用模式**：`useStore()`, `store.state.user`, `store.getters['user/isLoggedIn']`, `store.dispatch('user/logout')`
- **涉及功能**：用户信息展示、登出
- **特殊说明**：全局组件，被所有页面使用
- **迁移状态**：⏳ 待迁移

#### 17. 权限指令
**文件**：`src/directives/permission.js`
- **使用模式**：`import store from '@/store'`, `store.state.user`, `store.getters['user/userRole']`
- **涉及功能**：权限控制指令
- **特殊说明**：全局指令，用于权限控制
- **迁移状态**：⏳ 待迁移

#### 18. API 拦截器
**文件**：`src/api/index.js`
- **使用模式**：`import store from '@/store'`, `store.commit('user/CLEAR_USER')`
- **涉及功能**：HTTP 请求/响应拦截、认证错误处理
- **特殊说明**：全局拦截器，处理 401/403 错误时清除用户状态
- **迁移状态**：⏳ 待迁移

#### 19. 路由守卫
**文件**：`src/router/index.js`
- **使用模式**：`import store from '@/store'`, `store.commit('user/CLEAR_USER')`, `store.dispatch('user/handleSessionExpired')`
- **涉及功能**：路由权限控制、登录验证
- **特殊说明**：全局路由守卫，影响所有页面跳转
- **迁移状态**：⏳ 待迁移

---

### ⚠️ 特殊说明

#### 不使用 store 的文件（无需修改）
- **src/views/user/change-password/ChangePasswordPage.vue** - 修改密码页面（不使用 store）
- **src/views/user/settings/SettingsPage.vue** - 设置页面（不使用 store）
- **src/views/message/notification/NotificationsPage.vue** - 通知页面（不使用 store）
- **src/views/message/chat/components/** - 聊天子组件（通过 props 传递数据）

#### 跨模块使用说明
1. **消息模块文件**：虽然在 `src/views/message/` 目录下，但主要使用 user store
2. **茶叶模块文件**：使用 user store 的点赞、收藏功能
3. **店铺模块文件**：使用 user store 的关注功能
4. **订单模块文件**：使用 user store 的地址管理功能
5. **论坛模块文件**：使用 user store 的点赞、收藏功能

---

### 🆕 新发现的文件（通过三重验证）

#### 全局基础设施（2个文件）

---

#### 茶叶模块（4个文件）
#### 20. 茶叶详情页
- **使用模式**：`useStore()`, `store.state.user.userInfo`, `store.dispatch('user/addLike')`, `store.dispatch('user/removeLike')`, `store.dispatch('user/addFavorite')`, `store.dispatch('user/removeFavorite')`
- **涉及功能**：点赞评价、收藏茶叶、判断店铺所有者
- **特殊说明**：tea 模块，但使用 user store 的互动功能
- **迁移状态**：⏳ 待迁移

#### 21. 茶叶列表页
**文件**：`src/views/tea/list/TeaListPage.vue`
- **使用模式**：`useStore()`, `store.state.user`
- **涉及功能**：用户相关功能
- **迁移状态**：⏳ 待迁移

#### 22. 茶叶管理页
**文件**：`src/views/tea/manage/TeaManagePage.vue`
- **使用模式**：`useStore()`, `store.state.user`
- **涉及功能**：用户权限判断
- **迁移状态**：⏳ 待迁移

#### 23. 茶叶卡片组件
**文件**：`src/components/tea/card/TeaCard.vue`
- **使用模式**：`useStore()`, `store.state.user`
- **涉及功能**：用户相关展示
- **迁移状态**：⏳ 待迁移

---

#### 店铺模块（3个文件）

#### 24. 店铺详情页
**文件**：`src/views/shop/detail/ShopDetailPage.vue`
- **使用模式**：`useStore()`, `store.dispatch('user/addFollow')`, `store.dispatch('user/removeFollow')`
- **涉及功能**：关注/取消关注店铺
- **特殊说明**：shop 模块，但使用 user store 的关注功能
- **迁移状态**：⏳ 待迁移

#### 25. 店铺列表页
**文件**：`src/views/shop/list/ShopListPage.vue`
- **使用模式**：`useStore()`, `store.state.user`
- **涉及功能**：用户相关功能
- **迁移状态**：⏳ 待迁移

#### 26. 店铺管理页
**文件**：`src/views/shop/manage/ShopManagePage.vue`
- **使用模式**：`useStore()`, `store.state.user`
- **涉及功能**：用户权限判断
- **迁移状态**：⏳ 待迁移

---

#### 订单模块（6个文件）

#### 27. 购物车页面
**文件**：`src/views/order/cart/CartPage.vue`
- **使用模式**：`useStore()`, `store.state.user`
- **涉及功能**：用户相关功能
- **迁移状态**：⏳ 待迁移

#### 28. 结算页面
**文件**：`src/views/order/payment/CheckoutPage.vue`
- **使用模式**：`useStore()`, `store.state.user.addresses`, `store.dispatch('user/fetchAddresses')`
- **涉及功能**：获取用户地址列表、选择收货地址
- **特殊说明**：order 模块，但使用 user store 的地址管理功能
- **迁移状态**：⏳ 待迁移

#### 29. 支付页面
**文件**：`src/views/order/payment/PaymentPage.vue`
- **使用模式**：`useStore()`, `store.state.user`
- **涉及功能**：用户相关功能
- **迁移状态**：⏳ 待迁移

#### 30. 订单列表页
**文件**：`src/views/order/list/OrderListPage.vue`
- **使用模式**：`useStore()`, `store.state.user`
- **涉及功能**：用户相关功能
- **迁移状态**：⏳ 待迁移

#### 31. 订单详情页
**文件**：`src/views/order/detail/OrderDetailPage.vue`
- **使用模式**：`useStore()`, `store.state.user`
- **涉及功能**：用户相关功能
- **迁移状态**：⏳ 待迁移

#### 32. 订单管理页
**文件**：`src/views/order/manage/OrderManagePage.vue`
- **使用模式**：`useStore()`, `store.state.user`
- **涉及功能**：用户权限判断
- **迁移状态**：⏳ 待迁移

#### 33. 订单评价页
**文件**：`src/views/order/review/OrderReviewPage.vue`
- **使用模式**：`useStore()`, `store.state.user`
- **涉及功能**：用户相关功能
- **迁移状态**：⏳ 待迁移

---

#### 论坛模块（6个文件）

#### 34. 论坛列表页
**文件**：`src/views/forum/list/ForumListPage.vue`
- **使用模式**：`useStore()`, `store.dispatch('user/getUserInfo')`, `store.dispatch('user/addLike')`, `store.dispatch('user/removeLike')`, `store.dispatch('user/addFavorite')`, `store.dispatch('user/removeFavorite')`
- **涉及功能**：获取用户信息、点赞帖子、收藏帖子
- **特殊说明**：forum 模块，但使用 user store 的互动功能
- **迁移状态**：⏳ 待迁移

#### 35. 论坛详情页
**文件**：`src/views/forum/detail/ForumDetailPage.vue`
- **使用模式**：`useStore()`, `store.state.user.favoriteList`, `store.dispatch('user/addLike')`, `store.dispatch('user/removeLike')`, `store.dispatch('user/addFavorite')`, `store.dispatch('user/removeFavorite')`
- **涉及功能**：点赞帖子/评论、收藏帖子
- **特殊说明**：forum 模块，但使用 user store 的互动功能
- **迁移状态**：⏳ 待迁移

#### 36. 论坛管理页
**文件**：`src/views/forum/manage/ForumManagePage.vue`
- **使用模式**：`useStore()`, `store.state.user`
- **涉及功能**：用户权限判断
- **迁移状态**：⏳ 待迁移

#### 37. 文化管理页
**文件**：`src/views/forum/manage/CultureManagerPage.vue`
- **使用模式**：`useStore()`, `store.state.user`
- **涉及功能**：用户权限判断
- **迁移状态**：⏳ 待迁移

#### 38. 文化主页
**文件**：`src/views/forum/culturehome/CultureHomePage.vue`
- **使用模式**：`useStore()`, `store.state.user`
- **涉及功能**：用户相关功能
- **迁移状态**：⏳ 待迁移

#### 39. 文章详情页
**文件**：`src/views/forum/culturehome/ArticleDetailPage.vue`
- **使用模式**：`useStore()`, `store.state.user`
- **涉及功能**：用户相关功能
- **迁移状态**：⏳ 待迁移

---

### 📊 迁移统计

| 类别 | 数量 | 说明 |
|------|------|------|
| **用户认证模块** | 4 个文件 | 登录、注册、密码重置、商家认证 |
| **用户资料模块** | 3 个文件 | 个人资料、设置、地址管理 |
| **用户管理模块** | 1 个文件 | 管理员用户管理 |
| **认证工具函数** | 1 个文件 | useAuth 组合式函数 |
| **消息模块（使用 user store）** | 5 个文件 | 用户主页、关注、收藏、发布内容、通知 |
| **聊天模块（混合使用）** | 1 个文件 | 聊天页面（user + message store） |
| **全局组件/指令/拦截器** | 4 个文件 | 导航栏、权限指令、API拦截器、路由守卫 |
| **茶叶模块（使用 user store）** | 4 个文件 | 茶叶详情、列表、管理、卡片组件 |
| **店铺模块（使用 user store）** | 3 个文件 | 店铺详情、列表、管理 |
| **订单模块（使用 user store）** | 6 个文件 | 购物车、结算、支付、订单列表/详情/管理/评价 |
| **论坛模块（使用 user store）** | 6 个文件 | 论坛列表/详情/管理、文化管理/主页、文章详情 |
| **总计** | **38 个文件** | 全部需要迁移 |

### ⚠️ 重要发现

通过三重验证，发现实际需要迁移的文件数量从 **17 个增加到 38 个**，主要原因：
1. **跨模块使用**：tea、shop、order、forum 模块大量使用 user store 的互动功能（点赞、收藏、关注）
2. **全局基础设施**：API 拦截器和路由守卫也使用了 user store
3. **地址功能**：订单模块的结算页面使用了 user store 的地址管理功能

---

### 🔄 迁移顺序

按照以下顺序逐个迁移（从简单到复杂，从独立到依赖，从核心到外围）：

**第一阶段：核心基础设施（优先级最高）**
1. ⏳ **useAuth.js** - 认证组合式函数（核心依赖，被多个组件使用）
2. ⏳ **api/index.js** - API 拦截器（全局基础设施）
3. ⏳ **router/index.js** - 路由守卫（全局基础设施）
4. ⏳ **permission.js** - 权限指令（全局指令）

**第二阶段：用户认证功能**
5. ⏳ **LoginPage.vue** - 登录页面
6. ⏳ **RegisterPage.vue** - 注册页面
7. ⏳ **ResetPasswordPage.vue** - 重置密码页面

**第三阶段：用户资料管理**
8. ⏳ **ProfilePage.vue** - 个人资料页面
9. ⏳ **ProfileEditPage.vue** - 个人设置页面
10. ⏳ **AddressPage.vue** - 地址管理页面
11. ⏳ **MerchantApplication.vue** - 商家认证申请

**第四阶段：用户管理功能**
12. ⏳ **UserManagePage.vue** - 用户管理页面（管理员）

**第五阶段：全局组件**
13. ⏳ **NavBar.vue** - 导航栏组件（影响所有页面）

**第六阶段：消息模块中的用户功能**
14. ⏳ **UserHomePage.vue** - 用户主页
15. ⏳ **FollowsPage.vue** - 关注管理
16. ⏳ **FavoritesPage.vue** - 收藏管理
17. ⏳ **PublishedContentPage.vue** - 发布内容管理
18. ⏳ **SystemNotificationsPage.vue** - 系统通知
19. ⏳ **ChatPage.vue** - 聊天页面（user + message store）

**第七阶段：茶叶模块（互动功能）**
20. ⏳ **TeaCard.vue** - 茶叶卡片组件
21. ⏳ **TeaListPage.vue** - 茶叶列表页
22. ⏳ **TeaDetailPage.vue** - 茶叶详情页（点赞、收藏）
23. ⏳ **TeaManagePage.vue** - 茶叶管理页

**第八阶段：店铺模块（关注功能）**
24. ⏳ **ShopListPage.vue** - 店铺列表页
25. ⏳ **ShopDetailPage.vue** - 店铺详情页（关注）
26. ⏳ **ShopManagePage.vue** - 店铺管理页

**第九阶段：订单模块（地址功能）**
27. ⏳ **CartPage.vue** - 购物车页面
28. ⏳ **CheckoutPage.vue** - 结算页面（地址选择）
29. ⏳ **PaymentPage.vue** - 支付页面
30. ⏳ **OrderListPage.vue** - 订单列表页
31. ⏳ **OrderDetailPage.vue** - 订单详情页
32. ⏳ **OrderManagePage.vue** - 订单管理页
33. ⏳ **OrderReviewPage.vue** - 订单评价页

**第十阶段：论坛模块（互动功能）**
34. ⏳ **ForumListPage.vue** - 论坛列表页（点赞、收藏）
35. ⏳ **ForumDetailPage.vue** - 论坛详情页（点赞、收藏）
36. ⏳ **ForumManagePage.vue** - 论坛管理页
37. ⏳ **CultureManagerPage.vue** - 文化管理页
38. ⏳ **CultureHomePage.vue** - 文化主页
39. ⏳ **ArticleDetailPage.vue** - 文章详情页

---

### ⚠️ 迁移注意事项

1. **useAuth.js 优先迁移**：这是核心认证逻辑，被多个组件依赖
2. **全局基础设施优先**：API 拦截器、路由守卫、权限指令影响整个应用
3. **NavBar.vue 影响全局**：导航栏在所有页面显示，需要仔细测试
4. **跨模块文件众多**：tea、shop、order、forum 模块大量使用 user store 的互动功能
5. **ChatPage.vue 混合使用**：同时使用 user 和 message store，需要协调迁移
6. **地址功能跨模块**：订单模块的结算页面使用了 user store 的地址管理
7. **互动功能广泛**：点赞、收藏、关注功能在多个模块中使用

### 🔍 三重验证结果

**方案 1：目录穷举** ✅
- 列出了所有相关目录的文件
- 发现了 ChangePasswordPage.vue、SettingsPage.vue、NotificationsPage.vue 不使用 store

**方案 2：逐个检查** ✅
- 验证了原清单中的 17 个文件全部使用 user store
- 发现了额外 22 个使用 user store 的文件

**方案 3：关键词交叉验证** ✅
- 使用 `store.state.user` 搜索：发现 14 处使用
- 使用 `store.dispatch('user/` 搜索：发现大量使用
- 使用 `store.getters['user/` 搜索：发现 1 处使用
- 使用 `store.commit('user/` 搜索：发现 4 处使用

**验证结论**：
- 原清单 17 个文件 ✅ 全部确认
- 新发现 22 个文件 🆕 需要补充
- 总计 **39 个文件**需要迁移（比原计划多 22 个）

---

### 验证说明
- ✅ 已搜索所有使用 `useStore from 'vuex'` 的文件
- ✅ 已过滤出包含 `user` 相关代码的文件
- ✅ 已检查关键文件的实际使用模式
- ✅ 已确认这些文件使用了 `store.dispatch('user/xxx')` 或 `store.state.user` 或 `store.getters['user/xxx']`

### 迁移模式分析
从检查的文件中，发现以下常见模式需要迁移：
1. **store.dispatch('user/xxx')** → `userStore.xxx()`
2. **store.state.user.xxx** → `userStore.xxx`
3. **store.getters['user/xxx']** → `userStore.xxx`
4. **store.commit('user/XXX')** → 直接赋值 `userStore.xxx = value`

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

