# 通用接口（点赞/关注/收藏）调用流程说明

## 一、当前实现流程

### 1. 添加操作流程（以点赞帖子为例）

```
组件（ForumDetailPage.vue）
  ↓ 1. 收集参数（从页面数据获取）
  {
    targetId: postId.value,      // 从路由参数或帖子数据获取
    targetType: 'post'           // 固定值，根据业务场景确定
  }
  ↓ 2. 调用状态管理Action
  store.dispatch('user/addLike', {targetId, targetType})
  ↓ 3. 状态管理Action调用API
  addLikeApi(likeData) → POST /user/likes
  ↓ 4. 后端处理并返回
  Result.success(2016, true)  // 只返回布尔值，不返回完整记录
  ↓ 5. 状态管理更新本地状态（问题：res.data只是true，无法更新likeList）
  commit('ADD_LIKE', res.data)  // res.data = true，无法添加到likeList
  ↓ 6. 组件重新加载详情数据
  fetchPostDetail() → GET /forum/posts/{id}
  ↓ 7. 详情接口返回包含isLiked字段
  { ..., isLiked: true, ... }
  ↓ 8. 组件使用状态字段更新UI
  isLiked = computed(() => post.value?.isLiked || false)
```

### 2. 取消操作流程（以取消点赞帖子为例）

```
组件（ForumDetailPage.vue）
  ↓ 1. 从状态管理中查找记录ID
  const likeList = store.state.user.likeList || []
  const likeItem = likeList.find(item => 
    item.targetType === 'post' && item.targetId === postId.value
  )
  ↓ 2. 如果找到记录，获取记录ID
  likeItem.id  // 这是点赞记录的ID，不是帖子ID
  ↓ 3. 调用状态管理Action
  store.dispatch('user/removeLike', likeItem.id)
  ↓ 4. 状态管理Action调用API
  removeLikeApi(likeId) → DELETE /user/likes/{id}
  ↓ 5. 后端处理并返回
  Result.success(2017, true)
  ↓ 6. 状态管理更新本地状态
  commit('REMOVE_LIKE', likeId)  // 从likeList中移除
  ↓ 7. 组件重新加载详情数据
  fetchPostDetail() → GET /forum/posts/{id}
  ↓ 8. 详情接口返回更新后的状态字段
  { ..., isLiked: false, ... }
```

## 二、参数传递说明

### 1. 添加操作的参数来源

**参数由组件收集，从页面数据中获取：**

```javascript
// 示例：点赞帖子
const likePost = async () => {
  // 参数1：targetId - 从页面数据获取
  const targetId = postId.value  // 从路由参数或帖子对象获取
  
  // 参数2：targetType - 根据业务场景固定值
  const targetType = 'post'  // 固定值：'post'/'reply'/'article'/'review'
  
  // 调用状态管理
  await store.dispatch('user/addLike', {
    targetId,
    targetType
  })
}
```

**不同场景的参数来源：**

| 场景 | targetId来源 | targetType值 |
|------|-------------|-------------|
| 点赞帖子 | `postId.value` 或 `post.id` | `'post'` |
| 点赞回复 | `reply.id` | `'reply'` |
| 点赞文章 | `article.value.id` | `'article'` |
| 点赞评价 | `review.id` | `'review'` |
| 关注用户 | `userId.value` | `'user'` |
| 关注店铺 | `shop.value.id` | `'shop'` |
| 收藏茶叶 | `tea.value.id` | `'tea'` (itemType) |
| 收藏帖子 | `postId.value` | `'post'` (itemType) |
| 收藏文章 | `article.value.id` | `'tea_article'` (itemType) |

### 2. 取消操作的参数来源

**参数需要从状态管理中查找：**

```javascript
// 示例：取消点赞帖子
const likePost = async () => {
  // 步骤1：从状态管理中查找记录
  const likeList = store.state.user.likeList || []
  const likeItem = likeList.find(item => 
    item.targetType === 'post' && 
    item.targetId === postId.value
  )
  
  // 步骤2：如果找到，使用记录ID
  if (likeItem) {
    await store.dispatch('user/removeLike', likeItem.id)  // likeItem.id是点赞记录的ID
  }
}
```

**问题：如果likeList为空或未正确更新，无法找到记录ID**

## 三、状态管理的职责

### 1. 状态管理负责什么？

**状态管理（Vuex user模块）的职责：**

1. **调用API接口**
   - `addLike` action → 调用 `addLikeApi(likeData)`
   - `removeLike` action → 调用 `removeLikeApi(likeId)`
   - `addFollow` action → 调用 `addFollowApi(followData)`
   - `removeFollow` action → 调用 `removeFollowApi(followId)`
   - `addFavorite` action → 调用 `addFavoriteApi(favoriteData)`
   - `removeFavorite` action → 调用 `removeFavoriteApi(favoriteId)`

2. **维护本地状态列表**
   - `followList`: 存储当前用户的关注列表
   - `favoriteList`: 存储当前用户的收藏列表
   - `likeList`: 存储当前用户的点赞列表（可选）

3. **更新状态**
   - 添加操作后：将新记录添加到对应列表（如果后端返回完整记录）
   - 取消操作后：从对应列表中移除记录

### 2. 状态管理不负责什么？

**状态管理不负责：**
- ❌ 收集业务参数（targetId、targetType等）- 由组件负责
- ❌ 判断当前状态（isLiked、isFavorited等）- 由详情接口返回
- ❌ 查找记录ID（用于取消操作）- 由组件从状态列表中查找

## 四、当前实现的问题

### 问题1：后端返回数据不完整

**后端返回：**
```java
return Result.success(2016, true);  // 只返回布尔值
```

**前端期望：**
```javascript
// 期望返回完整的点赞记录，包含id、targetType、targetId等
{
  id: 123,
  targetType: 'post',
  targetId: '1',
  userId: 'cy100002',
  createTime: '2025-01-28T10:00:00Z'
}
```

**影响：**
- `ADD_LIKE` mutation 无法正确更新 `likeList`
- 取消操作时无法从 `likeList` 中找到记录ID
- 需要重新加载详情数据才能获取状态字段

### 问题2：likeList可能为空

**原因：**
- 页面加载时没有主动获取点赞列表
- 添加点赞后，后端只返回true，无法更新likeList
- 取消操作时无法找到记录ID

**解决方案：**
1. 在用户登录时或页面加载时，主动获取点赞/关注/收藏列表
2. 或者：取消操作时不依赖likeList，而是通过详情接口的状态字段判断

## 五、推荐的完整流程

### 方案A：依赖详情接口的状态字段（当前方案）

**优点：**
- 不需要维护likeList
- 状态字段由后端统一管理，更可靠

**流程：**
```
1. 组件收集参数 → 调用状态管理Action
2. 状态管理调用API → 后端处理
3. 组件重新加载详情数据 → 获取最新的isLiked状态
4. 组件使用状态字段更新UI
```

**取消操作时：**
```
1. 组件从详情数据判断状态（isLiked）
2. 如果已点赞，需要找到记录ID
   - 方案A1：从likeList查找（需要确保likeList已加载）
   - 方案A2：调用"获取点赞列表"接口查找
   - 方案A3：后端提供"根据targetType和targetId查找记录ID"的接口
```

### 方案B：维护完整的本地状态列表

**优点：**
- 取消操作时可以直接从列表查找
- 不需要重新加载详情数据

**流程：**
```
1. 页面加载时：获取点赞/关注/收藏列表
2. 添加操作：后端返回完整记录 → 更新列表
3. 取消操作：从列表查找记录ID → 调用API → 更新列表
4. 状态判断：从列表查找，而不是从详情接口
```

## 六、当前代码的实际流程

### 实际执行流程（以点赞帖子为例）

```javascript
// 1. 组件收集参数
const likePost = async () => {
  const targetId = postId.value        // 从路由参数获取
  const targetType = 'post'           // 固定值
  
  // 2. 判断当前状态（从详情数据获取）
  if (post.value?.isLiked) {
    // 3. 取消操作：从likeList查找记录ID
    const likeList = store.state.user.likeList || []
    const likeItem = likeList.find(item => 
      item.targetType === 'post' && item.targetId === postId.value
    )
    
    if (likeItem) {
      // 4. 调用状态管理Action
      await store.dispatch('user/removeLike', likeItem.id)
      // 5. 重新加载详情数据
      await fetchPostDetail()
    }
  } else {
    // 6. 添加操作：直接调用状态管理Action
    await store.dispatch('user/addLike', {
      targetId: postId.value,
      targetType: 'post'
    })
    // 7. 重新加载详情数据
    await fetchPostDetail()
  }
}
```

### 状态管理的实际处理

```javascript
// user.js - addLike action
async addLike({ commit }, likeData) {
  // 1. 调用API
  const res = await addLikeApi(likeData)
  
  // 2. 尝试更新likeList（但res.data只是true，无法更新）
  const like = res.data || res  // like = true
  commit('ADD_LIKE', like)      // 无法正确添加到likeList
  
  // 3. 返回结果给组件
  return res
}
```

## 七、总结

### 参数传递路径

```
页面数据（postId, shopId等）
  ↓
组件收集参数（targetId, targetType）
  ↓
调用状态管理Action（传递参数对象）
  ↓
状态管理Action调用API（传递参数对象）
  ↓
后端处理（使用参数创建记录）
```

### 状态管理职责

1. **接口调用层**：调用API接口
2. **状态维护层**：维护本地列表（followList、favoriteList、likeList）
3. **状态更新层**：添加/删除操作后更新列表

### 组件职责

1. **参数收集层**：从页面数据收集参数
2. **状态判断层**：从详情数据判断当前状态（isLiked、isFavorited等）
3. **记录查找层**：从状态列表查找记录ID（用于取消操作）
4. **数据刷新层**：操作后重新加载详情数据以更新状态字段

### 当前方案的依赖关系

```
组件 ←→ 状态管理 ←→ API接口
  ↓
详情接口（获取状态字段）
  ↓
状态列表（查找记录ID，用于取消操作）
```

**关键点：**
- 状态字段（isLiked等）由详情接口提供，不依赖状态列表
- 取消操作需要记录ID，需要从状态列表查找（但列表可能为空）
- 操作后必须重新加载详情数据以更新状态字段
