# 用户模块 (User Module) 任务分解

## 文档信息

| 项目 | 内容 |
|------|------|
| 模块名称 | 用户模块 (User Module) |
| 当前完成度 | 50% |
| 目标完成度 | 100% |
| 预计工时 | 3-4天 |
| 优先级 | 高 |
| 负责人 | - |
| 创建日期 | 2024-12-17 |
| 最后更新 | 2024-12-17 |

---

## 一、总体概览

### 1.1 模块职责

用户模块负责处理所有与用户相关的功能，包括：
- 用户认证（登录、注册、退出、Token管理）
- 个人资料管理（信息查看、编辑）
- 收货地址管理（增删改查、设置默认）
- 商家认证流程（申请、审核、状态查询）
- 用户互动功能（关注、收藏、点赞）
- 用户管理（管理员功能）

### 1.2 涉及文件

| 类型 | 文件路径 | 说明 |
|------|----------|------|
| Vuex | `src/store/modules/user.js` | 用户状态管理 |
| API | `src/api/user.js` | 用户API接口 |
| 常量 | `src/api/apiConstants.js` | API端点定义 |
| 工具 | `src/composables/useStorage.js` | Token存储 |
| 页面 | `src/views/user/auth/LoginPage.vue` | 登录页 |
| 页面 | `src/views/user/auth/RegisterPage.vue` | 注册页 |
| 页面 | `src/views/user/profile/ProfilePage.vue` | 个人中心 |
| 页面 | `src/views/user/settings/ProfileEditPage.vue` | 资料编辑 |
| 页面 | `src/views/user/settings/SettingsPage.vue` | 设置页 |
| 页面 | `src/views/user/address/AddressPage.vue` | 地址管理 |
| 页面 | `src/views/user/change-password/ChangePasswordPage.vue` | 修改密码 |
| 页面 | `src/views/user/auth/MerchantApplication.vue` | 商家认证 |
| 页面 | `src/views/user/manage/UserManagePage.vue` | 用户管理(管理员) |

### 1.3 已完成功能

- [x] 用户登录/注册/退出
- [x] Token管理（存储、刷新）
- [x] 获取/更新用户信息
- [x] 修改密码
- [x] 地址API接口定义
- [x] 商家认证API接口定义
- [x] 基础角色判断（isAdmin、isUser、isShop）

### 1.4 待完成功能

- [ ] 地址管理Vuex集成
- [ ] 收藏功能完整实现
- [ ] 关注功能完整实现
- [ ] 用户管理功能（管理员）
- [ ] 密码找回功能
- [ ] 头像上传功能

---

## 二、具体实施

### 2.1 任务拆分

#### 任务组A：地址管理Vuex集成 (优先级: 高)

| 任务ID | 任务描述 | 预计时间 | 状态 |
|--------|----------|----------|------|
| A-1 | 在user.js中添加addressList状态 | 15min | ⬜ 待开始 |
| A-2 | 添加SET_ADDRESS_LIST mutation | 10min | ⬜ 待开始 |
| A-3 | 实现fetchAddressList action | 20min | ⬜ 待开始 |
| A-4 | 实现addAddress action | 20min | ⬜ 待开始 |
| A-5 | 实现updateAddress action | 20min | ⬜ 待开始 |
| A-6 | 实现deleteAddress action | 15min | ⬜ 待开始 |
| A-7 | 实现setDefaultAddress action | 15min | ⬜ 待开始 |
| A-8 | 添加defaultAddress getter | 10min | ⬜ 待开始 |
| A-9 | 在AddressPage.vue中集成Vuex | 30min | ⬜ 待开始 |
| A-10 | 测试地址管理功能 | 20min | ⬜ 待开始 |

**任务组A详细实施步骤**：

```javascript
// A-1: 在state中添加
const state = () => ({
  // ... 现有状态
  addressList: [],        // 地址列表
  defaultAddressId: null  // 默认地址ID
})

// A-2: 添加mutation
SET_ADDRESS_LIST(state, list) {
  state.addressList = list
  // 自动识别默认地址
  const defaultAddr = list.find(addr => addr.isDefault)
  state.defaultAddressId = defaultAddr?.id || null
}

// A-3~A-7: 实现actions
async fetchAddressList({ commit }) {
  commit('SET_LOADING', true)
  try {
    const res = await getAddressList()
    commit('SET_ADDRESS_LIST', res.data || [])
    return res.data
  } finally {
    commit('SET_LOADING', false)
  }
}

// A-8: 添加getter
defaultAddress: state => state.addressList.find(addr => addr.id === state.defaultAddressId)
```

---

#### 任务组B：收藏功能实现 (优先级: 高)

| 任务ID | 任务描述 | 预计时间 | 状态 |
|--------|----------|----------|------|
| B-1 | 在apiConstants.js中确认收藏API端点 | 5min | ⬜ 待开始 |
| B-2 | 在user.js API中添加收藏相关方法 | 30min | ⬜ 待开始 |
| B-3 | 在user.js Vuex中添加favoriteList状态 | 10min | ⬜ 待开始 |
| B-4 | 添加收藏相关mutations | 15min | ⬜ 待开始 |
| B-5 | 实现fetchFavoriteList action | 25min | ⬜ 待开始 |
| B-6 | 实现addFavorite action | 20min | ⬜ 待开始 |
| B-7 | 实现removeFavorite action | 20min | ⬜ 待开始 |
| B-8 | 实现checkIsFavorite getter | 15min | ⬜ 待开始 |
| B-9 | 在FavoritesPage.vue中集成Vuex | 40min | ⬜ 待开始 |
| B-10 | 在TeaDetailPage.vue中集成收藏功能 | 30min | ⬜ 待开始 |
| B-11 | 测试收藏功能 | 20min | ⬜ 待开始 |

**任务组B详细实施步骤**：

```javascript
// B-2: API方法
export function getFavoriteList(params) {
  return request({
    url: API.USER.FAVORITES,
    method: 'get',
    params
  })
}

export function addFavorite(data) {
  // data: { targetId, targetType } 
  // targetType: 'tea' | 'post' | 'article'
  return request({
    url: API.USER.FAVORITES,
    method: 'post',
    data
  })
}

export function removeFavorite(id) {
  return request({
    url: `${API.USER.FAVORITES}/${id}`,
    method: 'delete'
  })
}

// B-3~B-8: Vuex实现
const state = () => ({
  // ... 现有状态
  favoriteList: [],
  favoriteIds: {  // 用于快速判断是否已收藏
    tea: [],
    post: [],
    article: []
  }
})

// getter
isFavorite: state => (targetId, targetType) => {
  return state.favoriteIds[targetType]?.includes(targetId)
}
```

---

#### 任务组C：关注功能实现 (优先级: 高)

| 任务ID | 任务描述 | 预计时间 | 状态 |
|--------|----------|----------|------|
| C-1 | 在user.js API中添加关注相关方法 | 30min | ⬜ 待开始 |
| C-2 | 在user.js Vuex中添加followList状态 | 10min | ⬜ 待开始 |
| C-3 | 添加关注相关mutations | 15min | ⬜ 待开始 |
| C-4 | 实现fetchFollowList action | 25min | ⬜ 待开始 |
| C-5 | 实现addFollow action | 20min | ⬜ 待开始 |
| C-6 | 实现removeFollow action | 20min | ⬜ 待开始 |
| C-7 | 实现checkIsFollowing getter | 15min | ⬜ 待开始 |
| C-8 | 在FollowsPage.vue中集成Vuex | 40min | ⬜ 待开始 |
| C-9 | 在ShopDetailPage.vue中集成关注功能 | 30min | ⬜ 待开始 |
| C-10 | 在UserHomePage.vue中集成关注功能 | 30min | ⬜ 待开始 |
| C-11 | 测试关注功能 | 20min | ⬜ 待开始 |

**任务组C详细实施步骤**：

```javascript
// C-1: API方法
export function getFollowList(params) {
  return request({
    url: API.USER.FOLLOWS,
    method: 'get',
    params  // { type: 'user' | 'shop' }
  })
}

export function addFollow(data) {
  // data: { targetId, targetType }
  // targetType: 'user' | 'shop'
  return request({
    url: API.USER.FOLLOWS,
    method: 'post',
    data
  })
}

export function removeFollow(id) {
  return request({
    url: `${API.USER.FOLLOWS}/${id}`,
    method: 'delete'
  })
}

// C-2~C-7: Vuex实现
const state = () => ({
  // ... 现有状态
  followList: {
    users: [],
    shops: []
  },
  followingIds: {
    user: [],
    shop: []
  }
})

// getter
isFollowing: state => (targetId, targetType) => {
  return state.followingIds[targetType]?.includes(targetId)
}
```

---

#### 任务组D：用户管理功能 (优先级: 中)

| 任务ID | 任务描述 | 预计时间 | 状态 |
|--------|----------|----------|------|
| D-1 | 在user.js API中添加管理员用户管理方法 | 40min | ⬜ 待开始 |
| D-2 | 在user.js Vuex中添加userList状态 | 10min | ⬜ 待开始 |
| D-3 | 添加用户管理相关mutations | 15min | ⬜ 待开始 |
| D-4 | 实现fetchUserList action | 25min | ⬜ 待开始 |
| D-5 | 实现updateUserRole action | 20min | ⬜ 待开始 |
| D-6 | 实现toggleUserStatus action | 20min | ⬜ 待开始 |
| D-7 | 实现processCertification action | 25min | ⬜ 待开始 |
| D-8 | 在UserManagePage.vue中集成Vuex | 60min | ⬜ 待开始 |
| D-9 | 测试用户管理功能 | 30min | ⬜ 待开始 |

**任务组D详细实施步骤**：

```javascript
// D-1: API方法（管理员专用）
export function getUserList(params) {
  return request({
    url: '/admin/users',
    method: 'get',
    params
  })
}

export function updateUserRole(userId, role) {
  return request({
    url: `/admin/users/${userId}/role`,
    method: 'put',
    data: { role }
  })
}

export function toggleUserStatus(userId, status) {
  return request({
    url: `/admin/users/${userId}/status`,
    method: 'put',
    data: { status }  // 0: 禁用, 1: 启用
  })
}

export function getCertificationList(params) {
  return request({
    url: '/admin/certifications',
    method: 'get',
    params
  })
}

export function processCertification(id, data) {
  // data: { status: 'approved' | 'rejected', reason?: string }
  return request({
    url: `/admin/certifications/${id}`,
    method: 'put',
    data
  })
}
```

---

#### 任务组E：其他功能完善 (优先级: 低)

| 任务ID | 任务描述 | 预计时间 | 状态 |
|--------|----------|----------|------|
| E-1 | 实现密码找回API | 20min | ⬜ 待开始 |
| E-2 | 实现密码找回Vuex action | 15min | ⬜ 待开始 |
| E-3 | 创建密码找回页面组件 | 60min | ⬜ 待开始 |
| E-4 | 实现头像上传API | 20min | ⬜ 待开始 |
| E-5 | 实现头像上传Vuex action | 20min | ⬜ 待开始 |
| E-6 | 在ProfileEditPage.vue中集成头像上传 | 40min | ⬜ 待开始 |
| E-7 | 测试密码找回功能 | 20min | ⬜ 待开始 |
| E-8 | 测试头像上传功能 | 20min | ⬜ 待开始 |

---

### 2.2 Mock数据设计

#### 地址Mock数据
```javascript
const mockAddresses = [
  {
    id: 1,
    userId: 1,
    receiver: '张三',
    phone: '13800138000',
    province: '陕西省',
    city: '商洛市',
    district: '商南县',
    address: '城关镇XX路XX号',
    isDefault: true,
    createTime: '2024-01-01 10:00:00'
  }
  // ...更多地址
]
```

#### 收藏Mock数据
```javascript
const mockFavorites = [
  {
    id: 1,
    userId: 1,
    targetId: 101,
    targetType: 'tea',
    targetInfo: {
      name: '商南绿茶',
      price: 198,
      image: '/images/tea1.jpg'
    },
    createTime: '2024-01-15 14:30:00'
  }
  // ...更多收藏
]
```

#### 关注Mock数据
```javascript
const mockFollows = {
  users: [
    {
      id: 1,
      userId: 1,
      targetId: 2,
      targetType: 'user',
      targetInfo: {
        username: '茶艺大师',
        avatar: '/avatars/user2.jpg',
        bio: '专注茶艺20年'
      },
      createTime: '2024-01-10 09:00:00'
    }
  ],
  shops: [
    {
      id: 2,
      userId: 1,
      targetId: 101,
      targetType: 'shop',
      targetInfo: {
        shopName: '商南茶庄',
        logo: '/shops/shop1.jpg',
        description: '正宗商南茶叶'
      },
      createTime: '2024-01-12 11:00:00'
    }
  ]
}
```

---

### 2.3 开发顺序

```
开发顺序建议：A → B → C → D → E

理由：
1. 地址管理(A)是订单流程的必要依赖，优先完成
2. 收藏功能(B)和关注功能(C)是用户互动的核心，次优先
3. 用户管理(D)是管理员功能，相对独立
4. 其他功能(E)优先级最低，可最后完成
```

---

## 三、检查测试

### 3.1 单元测试检查清单

#### 地址管理测试
- [ ] 获取地址列表成功
- [ ] 获取地址列表失败处理
- [ ] 添加地址成功
- [ ] 添加地址失败处理
- [ ] 更新地址成功
- [ ] 删除地址成功
- [ ] 设置默认地址成功
- [ ] 默认地址正确识别

#### 收藏功能测试
- [ ] 获取收藏列表成功
- [ ] 添加收藏成功
- [ ] 添加重复收藏处理
- [ ] 取消收藏成功
- [ ] 判断是否已收藏正确

#### 关注功能测试
- [ ] 获取关注列表成功
- [ ] 关注用户成功
- [ ] 关注店铺成功
- [ ] 取消关注成功
- [ ] 判断是否已关注正确

#### 用户管理测试
- [ ] 获取用户列表成功（管理员权限）
- [ ] 非管理员访问被拒绝
- [ ] 更新用户角色成功
- [ ] 启用/禁用用户成功
- [ ] 处理认证申请成功

### 3.2 集成测试检查清单

- [ ] 登录后自动获取用户信息
- [ ] 地址管理与订单流程集成
- [ ] 收藏状态在茶叶详情页正确显示
- [ ] 关注状态在店铺详情页正确显示
- [ ] 用户管理页面权限控制正确

### 3.3 UI交互测试清单

- [ ] 地址列表正确显示
- [ ] 添加地址表单验证
- [ ] 收藏按钮状态切换动画
- [ ] 关注按钮状态切换动画
- [ ] 用户管理列表分页正确
- [ ] 错误提示信息友好

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
| A-地址管理 | 10 | 0 | 0% | ⬜ 未开始 |
| B-收藏功能 | 11 | 0 | 0% | ⬜ 未开始 |
| C-关注功能 | 11 | 0 | 0% | ⬜ 未开始 |
| D-用户管理 | 9 | 0 | 0% | ⬜ 未开始 |
| E-其他功能 | 8 | 0 | 0% | ⬜ 未开始 |

### 5.2 里程碑

| 里程碑 | 目标日期 | 实际完成 | 状态 |
|--------|----------|----------|------|
| 地址管理完成 | - | - | ⬜ 未开始 |
| 收藏功能完成 | - | - | ⬜ 未开始 |
| 关注功能完成 | - | - | ⬜ 未开始 |
| 用户管理完成 | - | - | ⬜ 未开始 |
| 模块100%完成 | - | - | ⬜ 未开始 |

---

**文档版本**: 1.0  
**最后更新**: 2024-12-17

