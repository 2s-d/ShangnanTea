# 消息模块 (Message Module) 任务分解

## 文档信息

| 项目 | 内容 |
|------|------|
| 模块名称 | 消息模块 (Message Module) |
| 当前完成度 | 48% |
| 目标完成度 | 100% |
| 预计工时 | 3-4天 |
| 优先级 | 高 |
| 负责人 | - |
| 创建日期 | 2024-12-17 |
| 最后更新 | 2024-12-17 |

---

## 一、总体概览

### 1.1 模块职责

消息模块负责处理所有与用户通信和个人主页相关的功能：
1. **系统通知**：四种通知类型的展示和管理
2. **即时聊天**：好友私信、店铺客服聊天
3. **个人主页**：用户资料展示、动态、统计
4. **关注收藏管理**：关注的用户/店铺、收藏的内容

### 1.2 涉及文件

| 类型 | 文件路径 | 说明 |
|------|----------|------|
| Vuex | `src/store/modules/message.js` | 消息状态管理 |
| API | `src/api/message.js` | 消息API接口 |
| 页面 | `src/views/message/notification/NotificationsPage.vue` | 系统通知 |
| 页面 | `src/views/message/chat/ChatPage.vue` | 聊天页面 |
| 页面 | `src/views/message/homepage/UserHomePage.vue` | 个人主页 |
| 页面 | `src/views/message/follows/FollowsPage.vue` | 关注管理 |
| 页面 | `src/views/message/favorites/FavoritesPage.vue` | 收藏管理 |
| 页面 | `src/views/message/content/PublishedContentPage.vue` | 发布内容 |

### 1.3 已完成功能

- [x] 消息列表获取
- [x] 消息详情获取
- [x] 发送消息
- [x] 标记已读
- [x] 删除消息
- [x] 未读数量获取
- [x] 聊天会话列表
- [x] 聊天历史记录
- [x] 聊天页面UI（含路由参数跳转）
- [x] 通知页面UI

### 1.4 待完成功能

- [ ] 批量消息操作
- [ ] 聊天会话管理（创建、置顶、删除）
- [ ] 图片消息发送
- [ ] 个人主页功能
- [ ] 关注列表管理
- [ ] 收藏列表管理
- [ ] 发布内容管理

---

## 二、具体实施

### 2.1 任务组A：聊天功能完善 (优先级: 高)

| 任务ID | 任务描述 | 预计时间 | 状态 |
|--------|----------|----------|------|
| A-1 | 在message.js API中添加会话管理方法 | 30min | ⬜ 待开始 |
| A-2 | 实现createChatSession action | 25min | ⬜ 待开始 |
| A-3 | 实现pinChatSession action | 15min | ⬜ 待开始 |
| A-4 | 实现deleteChatSession action | 15min | ⬜ 待开始 |
| A-5 | 实现sendImageMessage action | 30min | ⬜ 待开始 |
| A-6 | 在ChatPage.vue中集成会话管理 | 40min | ⬜ 待开始 |
| A-7 | 添加图片消息发送UI | 35min | ⬜ 待开始 |
| A-8 | 测试聊天功能 | 25min | ⬜ 待开始 |

**实施代码示例**：

```javascript
// A-1: API方法
export function createChatSession(data) {
  // data: { targetId, targetType: 'user' | 'shop' }
  return request({
    url: API.MESSAGE.SESSIONS,
    method: 'post',
    data
  })
}

export function pinChatSession(sessionId, isPinned) {
  return request({
    url: `${API.MESSAGE.SESSION_DETAIL}${sessionId}/pin`,
    method: 'put',
    data: { isPinned }
  })
}

export function deleteChatSession(sessionId) {
  return request({
    url: API.MESSAGE.SESSION_DETAIL + sessionId,
    method: 'delete'
  })
}

export function sendImageMessage(sessionId, file) {
  const formData = new FormData()
  formData.append('image', file)
  formData.append('sessionId', sessionId)
  
  return request({
    url: `${API.MESSAGE.SEND}/image`,
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
```

---

### 2.2 任务组B：个人主页功能 (优先级: 高)

| 任务ID | 任务描述 | 预计时间 | 状态 |
|--------|----------|----------|------|
| B-1 | 在message.js API中添加个人主页方法 | 35min | ⬜ 待开始 |
| B-2 | 在message.js Vuex中添加profile状态 | 15min | ⬜ 待开始 |
| B-3 | 实现fetchUserProfile action | 25min | ⬜ 待开始 |
| B-4 | 实现fetchUserDynamic action | 25min | ⬜ 待开始 |
| B-5 | 实现fetchUserStatistics action | 20min | ⬜ 待开始 |
| B-6 | 在UserHomePage.vue中集成Vuex | 50min | ⬜ 待开始 |
| B-7 | 完善个人主页UI展示 | 40min | ⬜ 待开始 |
| B-8 | 测试个人主页功能 | 25min | ⬜ 待开始 |

**实施代码示例**：

```javascript
// B-1: API方法
export function getUserProfile(userId) {
  return request({
    url: `/user/profile/${userId}`,
    method: 'get'
  })
}

export function getUserDynamic(userId, params) {
  return request({
    url: `/user/profile/${userId}/dynamic`,
    method: 'get',
    params
  })
}

export function getUserStatistics(userId) {
  return request({
    url: `/user/profile/${userId}/statistics`,
    method: 'get'
  })
}

// B-2~B-5: Vuex
const state = () => ({
  // ... 现有状态
  userProfile: null,
  userDynamic: [],
  userStatistics: {
    postCount: 0,
    likeCount: 0,
    favoriteCount: 0,
    followingCount: 0,
    followerCount: 0
  }
})
```

---

### 2.3 任务组C：关注管理功能 (优先级: 高)

| 任务ID | 任务描述 | 预计时间 | 状态 |
|--------|----------|----------|------|
| C-1 | 在message.js API中添加关注管理方法 | 30min | ⬜ 待开始 |
| C-2 | 实现fetchFollowingList action | 25min | ⬜ 待开始 |
| C-3 | 实现fetchFollowerList action | 25min | ⬜ 待开始 |
| C-4 | 实现addFollow action | 20min | ⬜ 待开始 |
| C-5 | 实现removeFollow action | 15min | ⬜ 待开始 |
| C-6 | 在FollowsPage.vue中集成Vuex | 45min | ⬜ 待开始 |
| C-7 | 实现关注/取关状态同步 | 25min | ⬜ 待开始 |
| C-8 | 测试关注功能 | 20min | ⬜ 待开始 |

**实施代码示例**：

```javascript
// C-1: API方法
export function getFollowingList(params) {
  // params: { type: 'user' | 'shop' | 'all', page, pageSize }
  return request({
    url: '/user/following',
    method: 'get',
    params
  })
}

export function getFollowerList(params) {
  return request({
    url: '/user/followers',
    method: 'get',
    params
  })
}

export function addFollow(data) {
  // data: { targetId, targetType: 'user' | 'shop' }
  return request({
    url: '/user/follow',
    method: 'post',
    data
  })
}

export function removeFollow(targetId, targetType) {
  return request({
    url: `/user/follow/${targetId}`,
    method: 'delete',
    params: { targetType }
  })
}
```

---

### 2.4 任务组D：收藏管理功能 (优先级: 中)

| 任务ID | 任务描述 | 预计时间 | 状态 |
|--------|----------|----------|------|
| D-1 | 在message.js API中添加收藏管理方法 | 30min | ⬜ 待开始 |
| D-2 | 实现fetchFavoriteList action | 25min | ⬜ 待开始 |
| D-3 | 实现addFavorite action | 20min | ⬜ 待开始 |
| D-4 | 实现removeFavorite action | 15min | ⬜ 待开始 |
| D-5 | 在FavoritesPage.vue中集成Vuex | 45min | ⬜ 待开始 |
| D-6 | 实现收藏分类筛选 | 25min | ⬜ 待开始 |
| D-7 | 测试收藏功能 | 20min | ⬜ 待开始 |

---

### 2.5 任务组E：发布内容管理 (优先级: 中)

| 任务ID | 任务描述 | 预计时间 | 状态 |
|--------|----------|----------|------|
| E-1 | 在message.js API中添加发布内容方法 | 25min | ⬜ 待开始 |
| E-2 | 实现fetchUserPosts action | 25min | ⬜ 待开始 |
| E-3 | 实现fetchUserReviews action | 25min | ⬜ 待开始 |
| E-4 | 在PublishedContentPage.vue中集成Vuex | 45min | ⬜ 待开始 |
| E-5 | 实现内容分类和管理 | 30min | ⬜ 待开始 |
| E-6 | 测试发布内容功能 | 20min | ⬜ 待开始 |

---

### 2.6 任务组F：批量消息操作 (优先级: 低)

| 任务ID | 任务描述 | 预计时间 | 状态 |
|--------|----------|----------|------|
| F-1 | 在message.js API中添加批量操作方法 | 20min | ⬜ 待开始 |
| F-2 | 实现batchMarkAsRead action | 15min | ⬜ 待开始 |
| F-3 | 实现batchDeleteMessages action | 15min | ⬜ 待开始 |
| F-4 | 在NotificationsPage.vue中添加批量操作UI | 30min | ⬜ 待开始 |
| F-5 | 测试批量操作功能 | 15min | ⬜ 待开始 |

---

### 2.7 开发顺序

```
开发顺序建议：A → B → C → D → E → F

理由：
1. 聊天功能完善(A)是即时通讯的核心
2. 个人主页(B)是用户展示的重要功能
3. 关注管理(C)和收藏管理(D)是用户互动功能
4. 发布内容(E)是内容管理功能
5. 批量操作(F)是效率提升功能
```

---

## 三、检查测试

### 3.1 单元测试检查清单

- [ ] 创建聊天会话成功
- [ ] 置顶会话成功
- [ ] 删除会话成功
- [ ] 发送图片消息成功
- [ ] 获取个人主页成功
- [ ] 获取用户动态成功
- [ ] 获取关注列表成功
- [ ] 添加/取消关注成功
- [ ] 获取收藏列表成功
- [ ] 添加/取消收藏成功
- [ ] 获取发布内容成功
- [ ] 批量已读成功
- [ ] 批量删除成功

### 3.2 集成测试检查清单

- [ ] 聊天页面路由参数正确处理
- [ ] 关注状态在各页面同步
- [ ] 收藏状态在各页面同步
- [ ] 个人主页数据正确加载

---

## 四、错误总结

> 此部分将在开发过程中持续更新。

### 4.1 已解决问题

| 日期 | 问题描述 | 解决方案 | 相关任务 |
|------|----------|----------|----------|
| - | - | - | - |

---

## 五、进度追踪

### 5.1 任务组完成度

| 任务组 | 总任务数 | 已完成 | 完成率 | 状态 |
|--------|----------|--------|--------|------|
| A-聊天完善 | 8 | 0 | 0% | ⬜ 未开始 |
| B-个人主页 | 8 | 0 | 0% | ⬜ 未开始 |
| C-关注管理 | 8 | 0 | 0% | ⬜ 未开始 |
| D-收藏管理 | 7 | 0 | 0% | ⬜ 未开始 |
| E-发布内容 | 6 | 0 | 0% | ⬜ 未开始 |
| F-批量操作 | 5 | 0 | 0% | ⬜ 未开始 |

### 5.2 里程碑

| 里程碑 | 目标日期 | 实际完成 | 状态 |
|--------|----------|----------|------|
| 聊天功能完善 | - | - | ⬜ 未开始 |
| 个人主页完成 | - | - | ⬜ 未开始 |
| 用户互动完成 | - | - | ⬜ 未开始 |
| 模块100%完成 | - | - | ⬜ 未开始 |

---

**文档版本**: 1.0  
**最后更新**: 2024-12-17

