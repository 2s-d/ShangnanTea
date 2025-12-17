# 论坛模块 (Forum Module) 任务分解

## 文档信息

| 项目 | 内容 |
|------|------|
| 模块名称 | 论坛模块 (Forum Module) |
| 当前完成度 | 22% |
| 目标完成度 | 100% |
| 预计工时 | 4-5天 |
| 优先级 | 高 |
| 负责人 | - |
| 创建日期 | 2024-12-17 |
| 最后更新 | 2024-12-17 |

---

## 一、总体概览

### 1.1 模块职责

论坛模块承担平台的内容功能，分为三个主要部分：
1. **主菜单茶文化（首页功能）**：茶文化知识展示、Banner管理、内容推荐
2. **茶友论坛功能**：帖子发布、回复、点赞、收藏等社区功能
3. **内容管理功能（管理员）**：首页内容管理、论坛管理

### 1.2 涉及文件

| 类型 | 文件路径 | 说明 |
|------|----------|------|
| Vuex | `src/store/modules/forum.js` | 论坛状态管理 |
| API | `src/api/forum.js` | 论坛API接口 |
| 常量 | `src/api/apiConstants.js` | API端点定义 |
| 页面 | `src/views/forum/culturehome/CultureHomePage.vue` | 茶文化首页 |
| 页面 | `src/views/forum/culturehome/ArticleDetailPage.vue` | 文章详情 |
| 页面 | `src/views/forum/list/ForumListPage.vue` | 论坛列表 |
| 页面 | `src/views/forum/detail/ForumDetailPage.vue` | 帖子详情 |
| 页面 | `src/views/forum/manage/CultureManagerPage.vue` | 茶文化管理(管理员) |
| 页面 | `src/views/forum/manage/ForumManagePage.vue` | 论坛管理(管理员) |

### 1.3 已完成功能

- [x] 获取首页数据（Banner、特色、分类等）
- [x] 更新首页数据（管理员）
- [x] 茶文化首页UI展示
- [x] 文章详情页UI
- [x] 论坛列表页UI框架
- [x] 帖子详情页UI框架

### 1.4 待完成功能

- [ ] 论坛版块管理
- [ ] 帖子CRUD操作
- [ ] 帖子回复功能
- [ ] 点赞/收藏功能
- [ ] @用户功能
- [ ] 首页Banner管理
- [ ] 茶文化文章管理
- [ ] 内容审核功能（管理员）

---

## 二、具体实施

### 2.1 任务拆分

#### 任务组A：论坛帖子基础功能 (优先级: 高)

| 任务ID | 任务描述 | 预计时间 | 状态 |
|--------|----------|----------|------|
| A-1 | 在apiConstants.js中完善论坛API端点 | 15min | ⬜ 待开始 |
| A-2 | 在forum.js API中添加帖子相关方法 | 45min | ⬜ 待开始 |
| A-3 | 在forum.js Vuex中添加posts状态 | 15min | ⬜ 待开始 |
| A-4 | 添加帖子相关mutations | 20min | ⬜ 待开始 |
| A-5 | 实现fetchForumPosts action | 25min | ⬜ 待开始 |
| A-6 | 实现fetchPostDetail action | 25min | ⬜ 待开始 |
| A-7 | 实现createPost action | 25min | ⬜ 待开始 |
| A-8 | 实现updatePost action | 20min | ⬜ 待开始 |
| A-9 | 实现deletePost action | 15min | ⬜ 待开始 |
| A-10 | 在ForumListPage.vue中集成Vuex | 45min | ⬜ 待开始 |
| A-11 | 在ForumDetailPage.vue中集成Vuex | 40min | ⬜ 待开始 |
| A-12 | 创建发帖弹窗/页面组件 | 50min | ⬜ 待开始 |
| A-13 | 测试帖子基础功能 | 30min | ⬜ 待开始 |

**任务组A详细实施步骤**：

```javascript
// A-1: apiConstants.js 完善
FORUM: {
  TOPICS: '/forum/topics',           // 版块列表
  TOPIC_DETAIL: '/forum/topics/',    // 版块详情
  POSTS: '/forum/posts',             // 帖子列表
  POST_DETAIL: '/forum/posts/',      // 帖子详情
  REPLIES: '/forum/replies',         // 回复列表
  REPLY_DETAIL: '/forum/replies/',   // 回复详情
  LIKE: '/forum/like',               // 点赞
  FAVORITE: '/forum/favorite',       // 收藏
  ARTICLES: '/forum/articles',       // 文章列表
  ARTICLE_DETAIL: '/forum/articles/', // 文章详情
  HOME_CONTENTS: '/forum/home-contents' // 首页内容
}

// A-2: API方法
export function getForumPosts(params) {
  // params: { topicId?, keyword?, page, pageSize, sortBy, sortOrder }
  return request({
    url: API.FORUM.POSTS,
    method: 'get',
    params
  })
}

export function getPostDetail(postId) {
  return request({
    url: API.FORUM.POST_DETAIL + postId,
    method: 'get'
  })
}

export function createPost(data) {
  // data: { topicId, title, content, images?[] }
  return request({
    url: API.FORUM.POSTS,
    method: 'post',
    data
  })
}

export function updatePost(postId, data) {
  return request({
    url: API.FORUM.POST_DETAIL + postId,
    method: 'put',
    data
  })
}

export function deletePost(postId) {
  return request({
    url: API.FORUM.POST_DETAIL + postId,
    method: 'delete'
  })
}

// A-3~A-9: Vuex实现
const state = {
  // ... 现有首页状态
  
  // 论坛状态
  forumTopics: [],      // 版块列表
  forumPosts: [],       // 帖子列表
  currentPost: null,    // 当前帖子
  postPagination: {
    total: 0,
    currentPage: 1,
    pageSize: 10
  },
  postFilters: {
    topicId: null,
    keyword: '',
    sortBy: 'createTime',
    sortOrder: 'desc'
  }
}

// mutations
SET_FORUM_POSTS(state, { list, pagination }) {
  state.forumPosts = list
  if (pagination) state.postPagination = pagination
}

SET_CURRENT_POST(state, post) {
  state.currentPost = post
}

ADD_POST(state, post) {
  state.forumPosts.unshift(post)
}

UPDATE_POST(state, updatedPost) {
  const index = state.forumPosts.findIndex(p => p.id === updatedPost.id)
  if (index !== -1) {
    state.forumPosts.splice(index, 1, updatedPost)
  }
  if (state.currentPost?.id === updatedPost.id) {
    state.currentPost = updatedPost
  }
}

REMOVE_POST(state, postId) {
  state.forumPosts = state.forumPosts.filter(p => p.id !== postId)
}

// actions
async fetchForumPosts({ commit, state }, params = {}) {
  commit('SET_LOADING', true)
  try {
    const res = await getForumPosts({
      ...state.postFilters,
      page: params.page || state.postPagination.currentPage,
      pageSize: params.pageSize || state.postPagination.pageSize,
      ...params
    })
    commit('SET_FORUM_POSTS', {
      list: res.data.list || [],
      pagination: res.data.pagination
    })
    return res.data
  } finally {
    commit('SET_LOADING', false)
  }
}

async createPost({ commit }, postData) {
  try {
    const res = await createPost(postData)
    commit('ADD_POST', res.data)
    return res.data
  } catch (error) {
    throw error
  }
}
```

---

#### 任务组B：帖子回复功能 (优先级: 高)

| 任务ID | 任务描述 | 预计时间 | 状态 |
|--------|----------|----------|------|
| B-1 | 在forum.js API中添加回复相关方法 | 30min | ⬜ 待开始 |
| B-2 | 在forum.js Vuex中添加replies状态 | 10min | ⬜ 待开始 |
| B-3 | 添加回复相关mutations | 15min | ⬜ 待开始 |
| B-4 | 实现fetchPostReplies action | 25min | ⬜ 待开始 |
| B-5 | 实现createReply action | 25min | ⬜ 待开始 |
| B-6 | 实现deleteReply action | 15min | ⬜ 待开始 |
| B-7 | 在ForumDetailPage.vue中添加回复列表 | 40min | ⬜ 待开始 |
| B-8 | 创建回复输入组件 | 35min | ⬜ 待开始 |
| B-9 | 实现多级回复展示 | 40min | ⬜ 待开始 |
| B-10 | 测试回复功能 | 25min | ⬜ 待开始 |

**任务组B详细实施步骤**：

```javascript
// B-1: API方法
export function getPostReplies(postId, params) {
  // params: { page, pageSize }
  return request({
    url: `${API.FORUM.POST_DETAIL}${postId}/replies`,
    method: 'get',
    params
  })
}

export function createReply(postId, data) {
  // data: { content, parentId?, images?[] }
  // parentId为空表示直接回复帖子，有值表示回复某条回复
  return request({
    url: `${API.FORUM.POST_DETAIL}${postId}/replies`,
    method: 'post',
    data
  })
}

export function deleteReply(replyId) {
  return request({
    url: API.FORUM.REPLY_DETAIL + replyId,
    method: 'delete'
  })
}

// B-2~B-6: Vuex实现
const state = {
  // ... 现有状态
  postReplies: [],
  replyPagination: {
    total: 0,
    currentPage: 1,
    pageSize: 20
  }
}

// mutations
SET_POST_REPLIES(state, { list, pagination }) {
  state.postReplies = list
  if (pagination) state.replyPagination = pagination
}

ADD_REPLY(state, reply) {
  // 如果是一级回复，直接添加
  if (!reply.parentId) {
    state.postReplies.push(reply)
  } else {
    // 如果是二级回复，找到父回复并添加
    const parentReply = state.postReplies.find(r => r.id === reply.parentId)
    if (parentReply) {
      if (!parentReply.children) parentReply.children = []
      parentReply.children.push(reply)
    }
  }
  // 更新帖子回复数
  if (state.currentPost) {
    state.currentPost.replyCount = (state.currentPost.replyCount || 0) + 1
  }
}

REMOVE_REPLY(state, replyId) {
  // 查找并删除回复（包括子回复中的）
  state.postReplies = state.postReplies.filter(r => {
    if (r.id === replyId) return false
    if (r.children) {
      r.children = r.children.filter(c => c.id !== replyId)
    }
    return true
  })
}
```

---

#### 任务组C：点赞收藏功能 (优先级: 中)

| 任务ID | 任务描述 | 预计时间 | 状态 |
|--------|----------|----------|------|
| C-1 | 在forum.js API中添加点赞收藏方法 | 25min | ⬜ 待开始 |
| C-2 | 实现likePost action | 20min | ⬜ 待开始 |
| C-3 | 实现unlikePost action | 15min | ⬜ 待开始 |
| C-4 | 实现favoritePost action | 20min | ⬜ 待开始 |
| C-5 | 实现unfavoritePost action | 15min | ⬜ 待开始 |
| C-6 | 在ForumDetailPage.vue中添加点赞收藏按钮 | 30min | ⬜ 待开始 |
| C-7 | 在ForumListPage.vue中添加点赞收藏快捷操作 | 25min | ⬜ 待开始 |
| C-8 | 实现点赞收藏状态同步 | 25min | ⬜ 待开始 |
| C-9 | 测试点赞收藏功能 | 20min | ⬜ 待开始 |

**任务组C详细实施步骤**：

```javascript
// C-1: API方法
export function likePost(postId) {
  return request({
    url: API.FORUM.LIKE,
    method: 'post',
    data: { targetId: postId, targetType: 'post' }
  })
}

export function unlikePost(postId) {
  return request({
    url: `${API.FORUM.LIKE}/${postId}`,
    method: 'delete',
    params: { targetType: 'post' }
  })
}

export function favoritePost(postId) {
  return request({
    url: API.FORUM.FAVORITE,
    method: 'post',
    data: { targetId: postId, targetType: 'post' }
  })
}

export function unfavoritePost(postId) {
  return request({
    url: `${API.FORUM.FAVORITE}/${postId}`,
    method: 'delete',
    params: { targetType: 'post' }
  })
}

// C-2~C-5: Vuex actions
async likePost({ commit, state }, postId) {
  try {
    await likePost(postId)
    
    // 更新帖子点赞状态
    if (state.currentPost?.id === postId) {
      commit('SET_CURRENT_POST', {
        ...state.currentPost,
        isLiked: true,
        likeCount: (state.currentPost.likeCount || 0) + 1
      })
    }
    
    // 更新列表中的状态
    commit('UPDATE_POST_LIKE', { postId, isLiked: true, delta: 1 })
    
    return true
  } catch (error) {
    throw error
  }
}

// mutation
UPDATE_POST_LIKE(state, { postId, isLiked, delta }) {
  const post = state.forumPosts.find(p => p.id === postId)
  if (post) {
    post.isLiked = isLiked
    post.likeCount = (post.likeCount || 0) + delta
  }
}

UPDATE_POST_FAVORITE(state, { postId, isFavorited, delta }) {
  const post = state.forumPosts.find(p => p.id === postId)
  if (post) {
    post.isFavorited = isFavorited
    post.favoriteCount = (post.favoriteCount || 0) + delta
  }
}
```

---

#### 任务组D：论坛版块管理 (优先级: 中)

| 任务ID | 任务描述 | 预计时间 | 状态 |
|--------|----------|----------|------|
| D-1 | 在forum.js API中添加版块管理方法 | 30min | ⬜ 待开始 |
| D-2 | 实现fetchForumTopics action | 20min | ⬜ 待开始 |
| D-3 | 实现createTopic action（管理员） | 20min | ⬜ 待开始 |
| D-4 | 实现updateTopic action（管理员） | 20min | ⬜ 待开始 |
| D-5 | 实现deleteTopic action（管理员） | 15min | ⬜ 待开始 |
| D-6 | 在ForumListPage.vue中添加版块筛选 | 30min | ⬜ 待开始 |
| D-7 | 在ForumManagePage.vue中添加版块管理UI | 45min | ⬜ 待开始 |
| D-8 | 测试版块管理功能 | 20min | ⬜ 待开始 |

**任务组D详细实施步骤**：

```javascript
// D-1: API方法
export function getForumTopics() {
  return request({
    url: API.FORUM.TOPICS,
    method: 'get'
  })
}

export function createTopic(data) {
  // data: { name, description, icon?, sort }
  return request({
    url: API.FORUM.TOPICS,
    method: 'post',
    data
  })
}

export function updateTopic(topicId, data) {
  return request({
    url: API.FORUM.TOPIC_DETAIL + topicId,
    method: 'put',
    data
  })
}

export function deleteTopic(topicId) {
  return request({
    url: API.FORUM.TOPIC_DETAIL + topicId,
    method: 'delete'
  })
}

// D-2~D-5: Vuex
const state = {
  forumTopics: []
}

// mutations
SET_FORUM_TOPICS(state, topics) {
  state.forumTopics = topics
}

// actions
async fetchForumTopics({ commit }) {
  try {
    const res = await getForumTopics()
    commit('SET_FORUM_TOPICS', res.data || [])
    return res.data
  } catch (error) {
    commit('SET_FORUM_TOPICS', [])
  }
}
```

---

#### 任务组E：茶文化内容管理 (优先级: 中)

| 任务ID | 任务描述 | 预计时间 | 状态 |
|--------|----------|----------|------|
| E-1 | 在forum.js API中添加文章管理方法 | 35min | ⬜ 待开始 |
| E-2 | 实现fetchArticles action | 20min | ⬜ 待开始 |
| E-3 | 实现fetchArticleDetail action | 20min | ⬜ 待开始 |
| E-4 | 实现createArticle action（管理员） | 25min | ⬜ 待开始 |
| E-5 | 实现updateArticle action（管理员） | 20min | ⬜ 待开始 |
| E-6 | 实现deleteArticle action（管理员） | 15min | ⬜ 待开始 |
| E-7 | 完善CultureHomePage.vue的文章展示 | 35min | ⬜ 待开始 |
| E-8 | 完善ArticleDetailPage.vue | 30min | ⬜ 待开始 |
| E-9 | 在CultureManagerPage.vue中添加文章管理 | 50min | ⬜ 待开始 |
| E-10 | 测试文章管理功能 | 25min | ⬜ 待开始 |

**任务组E详细实施步骤**：

```javascript
// E-1: API方法
export function getArticles(params) {
  // params: { category?, page, pageSize }
  return request({
    url: API.FORUM.ARTICLES,
    method: 'get',
    params
  })
}

export function getArticleDetail(articleId) {
  return request({
    url: API.FORUM.ARTICLE_DETAIL + articleId,
    method: 'get'
  })
}

export function createArticle(data) {
  // data: { title, content, category, coverImage?, summary? }
  return request({
    url: API.FORUM.ARTICLES,
    method: 'post',
    data
  })
}

export function updateArticle(articleId, data) {
  return request({
    url: API.FORUM.ARTICLE_DETAIL + articleId,
    method: 'put',
    data
  })
}

export function deleteArticle(articleId) {
  return request({
    url: API.FORUM.ARTICLE_DETAIL + articleId,
    method: 'delete'
  })
}

// Vuex state扩展
const state = {
  // ... 现有状态
  articles: [],
  currentArticle: null,
  articlePagination: {
    total: 0,
    currentPage: 1,
    pageSize: 10
  }
}
```

---

#### 任务组F：首页Banner管理 (优先级: 中)

| 任务ID | 任务描述 | 预计时间 | 状态 |
|--------|----------|----------|------|
| F-1 | 在forum.js API中添加Banner管理方法 | 25min | ⬜ 待开始 |
| F-2 | 实现fetchHomeBanners action | 15min | ⬜ 待开始 |
| F-3 | 实现updateHomeBanners action（管理员） | 20min | ⬜ 待开始 |
| F-4 | 在CultureManagerPage.vue中添加Banner管理UI | 45min | ⬜ 待开始 |
| F-5 | 实现Banner排序和预览功能 | 30min | ⬜ 待开始 |
| F-6 | 测试Banner管理功能 | 15min | ⬜ 待开始 |

---

### 2.2 Mock数据设计

#### 帖子Mock数据
```javascript
const mockPosts = [
  {
    id: 1,
    topicId: 1,
    topicName: '茶类主题',
    title: '第一次喝到正宗商南绿茶，真的太惊艳了！',
    content: '今天在朋友推荐下购买了一款商南绿茶...',
    images: ['/posts/p1-1.jpg', '/posts/p1-2.jpg'],
    author: {
      id: 1,
      username: '茶香四溢',
      avatar: '/avatars/user1.jpg'
    },
    likeCount: 56,
    replyCount: 23,
    favoriteCount: 12,
    viewCount: 458,
    isLiked: false,
    isFavorited: false,
    isTop: false,
    isEssence: true,
    createTime: '2024-01-15 14:30:00',
    updateTime: '2024-01-15 14:30:00'
  }
  // ...更多帖子
]
```

#### 回复Mock数据
```javascript
const mockReplies = [
  {
    id: 1,
    postId: 1,
    parentId: null,
    content: '确实，商南绿茶的香气真的很特别！',
    author: {
      id: 2,
      username: '茶艺大师',
      avatar: '/avatars/user2.jpg'
    },
    likeCount: 12,
    createTime: '2024-01-15 15:00:00',
    children: [
      {
        id: 2,
        postId: 1,
        parentId: 1,
        content: '同感同感，推荐试试他们家的毛尖',
        author: {
          id: 3,
          username: '普洱控',
          avatar: '/avatars/user3.jpg'
        },
        replyTo: {
          id: 2,
          username: '茶艺大师'
        },
        likeCount: 5,
        createTime: '2024-01-15 15:30:00'
      }
    ]
  }
  // ...更多回复
]
```

#### 版块Mock数据
```javascript
const mockTopics = [
  { id: 1, name: '茶类主题', description: '讨论各类茶叶', icon: 'tea', postCount: 156, sort: 1 },
  { id: 2, name: '茶文化讨论', description: '茶道、茶艺交流', icon: 'culture', postCount: 89, sort: 2 },
  { id: 3, name: '购买交流', description: '购茶经验分享', icon: 'shopping', postCount: 234, sort: 3 },
  { id: 4, name: '活动专区', description: '平台活动讨论', icon: 'activity', postCount: 45, sort: 4 }
]
```

#### 文章Mock数据
```javascript
const mockArticles = [
  {
    id: 1,
    title: '商南茶叶的历史渊源',
    summary: '商南茶叶种植历史悠久，可追溯到唐代...',
    content: '<p>商南茶叶种植历史悠久...</p>',
    category: 'history',
    coverImage: '/articles/a1-cover.jpg',
    author: '平台管理员',
    viewCount: 1256,
    likeCount: 89,
    createTime: '2024-01-10 10:00:00'
  }
  // ...更多文章
]
```

---

### 2.3 开发顺序

```
开发顺序建议：A → B → C → D → E → F

理由：
1. 帖子基础功能(A)是论坛核心，必须优先完成
2. 回复功能(B)是帖子功能的必要补充
3. 点赞收藏(C)是社区互动的基础
4. 版块管理(D)是帖子分类的基础
5. 文章管理(E)是茶文化内容的核心
6. Banner管理(F)是首页展示的组成部分
```

---

## 三、检查测试

### 3.1 单元测试检查清单

#### 帖子功能测试
- [ ] 获取帖子列表成功
- [ ] 帖子分页正确
- [ ] 获取帖子详情成功
- [ ] 发布帖子成功
- [ ] 发布帖子参数验证
- [ ] 编辑帖子成功
- [ ] 删除帖子成功
- [ ] 筛选和排序正确

#### 回复功能测试
- [ ] 获取回复列表成功
- [ ] 发布回复成功
- [ ] 发布子回复成功
- [ ] 删除回复成功
- [ ] 回复数量更新正确

#### 点赞收藏测试
- [ ] 点赞帖子成功
- [ ] 取消点赞成功
- [ ] 收藏帖子成功
- [ ] 取消收藏成功
- [ ] 计数更新正确

#### 版块管理测试
- [ ] 获取版块列表成功
- [ ] 创建版块成功（管理员）
- [ ] 更新版块成功（管理员）
- [ ] 删除版块成功（管理员）

#### 文章管理测试
- [ ] 获取文章列表成功
- [ ] 获取文章详情成功
- [ ] 创建文章成功（管理员）
- [ ] 更新文章成功（管理员）
- [ ] 删除文章成功（管理员）

### 3.2 集成测试检查清单

- [ ] 帖子列表与版块筛选联动正确
- [ ] 帖子详情页回复正确加载
- [ ] 点赞收藏状态在列表和详情页同步
- [ ] 发帖后列表自动刷新
- [ ] 用户只能编辑/删除自己的帖子和回复

### 3.3 UI交互测试清单

- [ ] 帖子卡片展示正确
- [ ] 发帖弹窗/页面交互流畅
- [ ] 回复输入组件正常工作
- [ ] 多级回复展示正确
- [ ] 点赞收藏按钮动画效果
- [ ] 文章详情页排版正确
- [ ] Banner轮播正常工作

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
| A-帖子基础 | 13 | 0 | 0% | ⬜ 未开始 |
| B-回复功能 | 10 | 0 | 0% | ⬜ 未开始 |
| C-点赞收藏 | 9 | 0 | 0% | ⬜ 未开始 |
| D-版块管理 | 8 | 0 | 0% | ⬜ 未开始 |
| E-文章管理 | 10 | 0 | 0% | ⬜ 未开始 |
| F-Banner管理 | 6 | 0 | 0% | ⬜ 未开始 |

### 5.2 里程碑

| 里程碑 | 目标日期 | 实际完成 | 状态 |
|--------|----------|----------|------|
| 帖子基础完成 | - | - | ⬜ 未开始 |
| 回复功能完成 | - | - | ⬜ 未开始 |
| 社区互动完成 | - | - | ⬜ 未开始 |
| 内容管理完成 | - | - | ⬜ 未开始 |
| 模块100%完成 | - | - | ⬜ 未开始 |

---

**文档版本**: 1.0  
**最后更新**: 2024-12-17

