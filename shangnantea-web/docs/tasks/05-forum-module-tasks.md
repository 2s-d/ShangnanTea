# 论坛模块 (Forum Module) 任务分解

## 文档信息

| 项目 | 内容 |
|------|------|
| 模块名称 | 论坛模块 (Forum Module) |
| 当前完成度 | 80% |
| 目标完成度 | 100% |
| 预计工时 | 5-6天 |
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

- [x] 茶文化首页基础UI展示
- [x] 文章详情页基础UI
- [x] 论坛列表页UI框架
- [x] 帖子详情页UI框架
- [x] 基础Vuex状态结构（首页数据部分实现）

### 1.4 待完成功能

**按任务组分类**：

- [x] **任务组0**：茶文化首页端到端闭环（首页数据获取/更新/Banner管理）
- [x] **任务组A**：论坛帖子基础功能端到端闭环（列表/详情/增删改查）
- [x] **任务组B**：帖子回复功能端到端闭环（回复列表/发布回复/多级回复）
- [x] **任务组C**：点赞收藏功能端到端闭环（点赞/收藏/状态同步）
- [ ] **任务组D**：论坛版块管理端到端闭环（版块列表/创建/管理）
- [ ] **任务组E**：茶文化文章管理端到端闭环（文章CRUD/分类管理）
- [ ] **任务组F**：内容审核功能端到端闭环（帖子审核/内容管理）

---

## 二、具体实施

### 2.0 端到端闭环开发模式（本模块采用）

> 本文档采用 **端到端小范围闭环** 的开发方式，不再默认"UI 已完成后再补 API/Vuex"。
>
> **目标**：每一个任务组都至少完成一次"可跑通的数据流闭环"：
>
> ```
> UI（组件/页面） → Vuex Actions → API（src/api） → 后端 Controller（返回测试数据，不走 DB/Service） → 响应 → Vuex State → UI 渲染
> ```
>
> **闭环验收（每个任务组都要做的最小自检）**：
> - [ ] 前端可编译运行（无构建级别错误）
> - [ ] 该任务组相关页面不再本地伪成功（不 setTimeout + 本地改业务状态）
> - [ ] Vuex State 能被真实请求更新（不是 UI 层造数组）
> - [ ] 后端 Controller 能对应该任务组接口返回可用的测试数据结构（字段齐全、可渲染）
>
> **最终测试策略调整**：
> - 原"每个任务组开发完立即全量写用例并执行"的方式，调整为：**开发期以闭环自检为主**
> - **模块全部任务组完成后**，再集中进入"最终统一测试"

### 2.1 任务拆分

> **任务组划分原则**：
> - 每个任务组对应一个**完整的功能域**（如"茶文化首页"、"论坛帖子"），而非单个操作
> - 每个任务组必须完成**端到端闭环**：后端Controller测试数据 → 前端API → Vuex状态管理 → UI组件
> - 任务组之间可能存在依赖关系，需按优先级顺序开发
> - 每个任务组包含"闭环自检"验收标准

---

#### 任务组0：茶文化首页端到端闭环（Controller → API → Vuex → UI） (优先级: 最高)

> **功能域**：茶文化首页数据获取、更新、Banner管理  
> **说明**：这是论坛模块的基础功能，首页展示是用户进入平台的第一印象。

**涉及功能点**：
- 获取首页数据（Banner、特色、分类、资讯、合作伙伴）
- 更新首页数据（管理员）
- Banner管理（上传、删除、排序）
- 首页内容展示

**涉及页面**：
- `CultureHomePage.vue` - 茶文化首页
- `CultureManagerPage.vue` - 茶文化管理页（管理员）

**涉及后端接口**：
- `GET /forum/home-contents` - 获取首页数据
- `PUT /forum/home-contents` - 更新首页数据（管理员）
- `GET /forum/banners` - 获取Banner列表
- `POST /forum/banners` - 上传Banner
- `PUT /forum/banners/{id}` - 更新Banner
- `DELETE /forum/banners/{id}` - 删除Banner
- `PUT /forum/banners/order` - 更新Banner顺序

**任务清单**：

| 任务ID | 任务描述 | 预计时间 | 状态 |
|--------|----------|----------|------|
| 0-0 | **后端 Controller**：首页数据/Banner相关接口返回测试数据（不走Service/DB，返回完整数据结构） | 80min | ⬜ 待开始 |
| 0-1 | **前端 API层**：确认 `src/api/forum.js` 基础方法（getHomeData/updateHomeData）与 `apiConstants.js` 一致 | 25min | ⬜ 待开始 |
| 0-2 | **前端 API层**：在 `src/api/forum.js` 添加Banner管理方法（getBanners/uploadBanner/updateBanner/deleteBanner/updateBannerOrder） | 40min | ⬜ 待开始 |
| 0-3 | **前端 API层**：在 `apiConstants.js` 添加Banner API端点 | 15min | ⬜ 待开始 |
| 0-4 | **Vuex层**：确认 `src/store/modules/forum.js` 的 state/mutations/actions 结构完整（banners、cultureFeatures等） | 30min | ⬜ 待开始 |
| 0-5 | **Vuex层**：实现 fetchHomeData action（移除UI-DEV伪成功逻辑，使用真实API） | 35min | ⬜ 待开始 |
| 0-6 | **Vuex层**：实现 updateHomeData action（管理员更新首页数据） | 30min | ⬜ 待开始 |
| 0-7 | **Vuex层**：实现Banner管理 actions（fetchBanners/uploadBanner/updateBanner/deleteBanner/updateBannerOrder） | 50min | ⬜ 待开始 |
| 0-8 | **UI层**：`CultureHomePage.vue` 改为从 Vuex 读取首页数据（不本地造数据） | 50min | ⬜ 待开始 |
| 0-9 | **UI层**：`CultureManagerPage.vue` 改为使用 Vuex actions（更新首页数据、Banner管理） | 80min | ⬜ 待开始 |
| 0-10 | **闭环自检**：首页数据可加载、Banner管理功能正常、首页内容正确展示 | 40min | ⬜ 待开始 |

**注意事项**：
- 首页数据结构需与数据库设计文档一致（参考 `home_contents` 表结构）
- Banner上传需使用 `FormData` 格式，支持图片上传
- Banner数量限制（建议：最多10张）
- Banner排序通过拖拽或上下箭头调整
- 权限控制：只有管理员可以更新首页数据和Banner

**任务组0端到端闭环验收（开发期自检）**：
- [ ] UI 不本地造首页数据（数据来自 Vuex `state.forum`）
- [ ] Vuex Actions 通过 API 发请求获取/更新首页数据
- [ ] 后端 Controller 能返回首页数据/Banner的测试数据结构
- [ ] 页面可完成：首页展示、Banner管理（失败时提示错误，不伪成功）

---

#### 任务组A：论坛帖子基础功能端到端闭环（Controller → API → Vuex → UI） (优先级: 高)

> **功能域**：论坛帖子的列表、详情、增删改查  
> **说明**：帖子功能是论坛的核心，用户可以在论坛中发布、查看、编辑、删除帖子。

**涉及功能点**：
- 获取帖子列表（支持分页、筛选、排序）
- 获取帖子详情
- 发布帖子
- 编辑帖子
- 删除帖子
- 帖子筛选（版块、关键词、排序）

**涉及页面**：
- `ForumListPage.vue` - 论坛列表页
- `ForumDetailPage.vue` - 帖子详情页
- 发帖弹窗/页面组件

**涉及后端接口**：
- `GET /forum/posts` - 获取帖子列表
- `GET /forum/posts/{id}` - 获取帖子详情
- `POST /forum/posts` - 发布帖子
- `PUT /forum/posts/{id}` - 编辑帖子
- `DELETE /forum/posts/{id}` - 删除帖子

**任务清单**：

| 任务ID | 任务描述 | 预计时间 | 状态 |
|--------|----------|----------|------|
| A-0 | **后端 Controller**：帖子相关接口返回测试数据（列表/详情/增删改查） | 90min | ⬜ 待开始 |
| A-1 | **前端 API层**：在 `src/api/forum.js` 添加帖子相关方法（getForumPosts/getPostDetail/createPost/updatePost/deletePost） | 45min | ⬜ 待开始 |
| A-2 | **前端 API层**：在 `apiConstants.js` 添加帖子API端点 | 15min | ⬜ 待开始 |
| A-3 | **Vuex层**：在 `src/store/modules/forum.js` 添加帖子相关 state（forumPosts、currentPost、postPagination、postFilters） | 25min | ⬜ 待开始 |
| A-4 | **Vuex层**：添加帖子相关 mutations（SET_FORUM_POSTS、SET_CURRENT_POST、ADD_POST、UPDATE_POST、REMOVE_POST） | 30min | ⬜ 待开始 |
| A-5 | **Vuex层**：实现 fetchForumPosts action（获取帖子列表，支持筛选、排序、分页） | 40min | ⬜ 待开始 |
| A-6 | **Vuex层**：实现 fetchPostDetail action（获取帖子详情） | 30min | ⬜ 待开始 |
| A-7 | **Vuex层**：实现 createPost/updatePost/deletePost actions（移除UI-DEV伪成功逻辑） | 50min | ⬜ 待开始 |
| A-8 | **UI层**：`ForumListPage.vue` 改为从 Vuex 读取 forumPosts（不本地造数据） | 50min | ⬜ 待开始 |
| A-9 | **UI层**：`ForumDetailPage.vue` 改为从 Vuex 读取 currentPost（不本地造数据） | 45min | ⬜ 待开始 |
| A-10 | **UI层**：创建发帖弹窗/页面组件（标题、内容、图片、版块选择） | 70min | ⬜ 待开始 |
| A-11 | **UI层**：在 `ForumListPage.vue` 添加筛选功能（版块、关键词、排序） | 60min | ⬜ 待开始 |
| A-12 | **闭环自检**：帖子列表可加载、详情可查看、增删改查可完成、筛选功能正常 | 50min | ⬜ 待开始 |

**注意事项**：
- 帖子数据结构需与数据库设计文档一致（参考 `forum_posts` 表结构）
- 发布帖子时需选择版块（topicId）
- 帖子内容需支持富文本或纯文本（根据需求）
- 帖子图片需支持多张上传（最多9张）
- 权限控制：用户只能编辑/删除自己的帖子，管理员可以编辑/删除所有帖子

**任务组A端到端闭环验收（开发期自检）**：
- [ ] UI 不本地造帖子列表数据（列表来自 Vuex `state.forum.forumPosts`）
- [ ] Vuex Actions 通过 API 发请求获取/更新帖子数据
- [ ] 后端 Controller 能返回帖子列表/详情的测试数据结构
- [ ] 页面可完成：列表展示、详情查看、发布/编辑/删除帖子（失败时提示错误，不伪成功）
- [ ] 筛选功能正常工作（版块、关键词、排序）

---

#### 任务组B：帖子回复功能端到端闭环（Controller → API → Vuex → UI） (优先级: 高)

> **功能域**：帖子回复的列表、发布、删除、多级回复  
> **说明**：回复功能是论坛互动的基础，支持多级回复形成讨论链。

**涉及功能点**：
- 获取回复列表（支持分页）
- 发布回复（一级回复、二级回复）
- 删除回复
- 多级回复展示
- @用户功能（可选）

**涉及页面**：
- `ForumDetailPage.vue` - 帖子详情页（回复列表、回复输入）

**涉及后端接口**：
- `GET /forum/posts/{postId}/replies` - 获取回复列表
- `POST /forum/posts/{postId}/replies` - 发布回复
- `DELETE /forum/replies/{id}` - 删除回复

**任务清单**：

| 任务ID | 任务描述 | 预计时间 | 状态 |
|--------|----------|----------|------|
| B-0 | **后端 Controller**：回复相关接口返回测试数据（列表/发布/删除，支持多级回复） | 70min | ✅ 已完成 |
| B-1 | **前端 API层**：在 `src/api/forum.js` 添加回复相关方法（getPostReplies/createReply/deleteReply） | 35min | ✅ 已完成 |
| B-2 | **前端 API层**：在 `apiConstants.js` 添加回复API端点 | 15min | ✅ 已完成 |
| B-3 | **Vuex层**：在 `src/store/modules/forum.js` 添加回复相关 state（postReplies、replyPagination） | 20min | ✅ 已完成 |
| B-4 | **Vuex层**：添加回复相关 mutations（SET_POST_REPLIES、ADD_REPLY、REMOVE_REPLY） | 25min | ✅ 已完成 |
| B-5 | **Vuex层**：实现 fetchPostReplies action（获取回复列表，支持分页） | 30min | ✅ 已完成 |
| B-6 | **Vuex层**：实现 createReply action（发布回复，支持一级/二级回复） | 35min | ✅ 已完成 |
| B-7 | **Vuex层**：实现 deleteReply action（删除回复） | 25min | ✅ 已完成 |
| B-8 | **UI层**：在 `ForumDetailPage.vue` 添加回复列表组件（展示回复、多级回复） | 60min | ✅ 已完成 |
| B-9 | **UI层**：创建回复输入组件（支持一级/二级回复、@用户） | 60min | ✅ 已完成 |
| B-10 | **UI层**：实现多级回复展示（回复的回复） | 50min | ✅ 已完成 |
| B-11 | **闭环自检**：回复列表可加载、发布回复可完成、删除回复可完成、多级回复正确展示 | 45min | ✅ 已完成 |

**注意事项**：
- 回复数据结构需与数据库设计文档一致（参考 `forum_replies` 表结构）
- 一级回复：parentId为空，直接回复帖子
- 二级回复：parentId有值，回复某条回复
- 回复内容需支持富文本或纯文本
- 回复图片需支持多张上传（最多3张）
- 权限控制：用户只能删除自己的回复，管理员可以删除所有回复

**任务组B端到端闭环验收（开发期自检）**：
- [ ] 回复列表可正确加载并分页显示
- [ ] 发布回复功能完整（一级/二级回复）
- [ ] 删除回复功能正常
- [ ] 多级回复正确展示（回复的回复）
- [ ] 回复发布后列表自动刷新

---

#### 任务组C：点赞收藏功能端到端闭环（Controller → API → Vuex → UI） (优先级: 中)

> **功能域**：帖子点赞、收藏、状态同步  
> **说明**：点赞和收藏是用户互动的重要功能，需与用户模块的关注收藏功能保持一致。

**涉及功能点**：
- 点赞帖子
- 取消点赞
- 收藏帖子
- 取消收藏
- 点赞/收藏状态同步

**涉及页面**：
- `ForumDetailPage.vue` - 帖子详情页（点赞/收藏按钮）
- `ForumListPage.vue` - 论坛列表页（点赞/收藏快捷操作）

**涉及后端接口**：
- `POST /forum/posts/{id}/like` - 点赞帖子
- `DELETE /forum/posts/{id}/like` - 取消点赞
- `POST /forum/posts/{id}/favorite` - 收藏帖子
- `DELETE /forum/posts/{id}/favorite` - 取消收藏

**任务清单**：

| 任务ID | 任务描述 | 预计时间 | 状态 |
|--------|----------|----------|------|
| C-0 | **后端 Controller**：点赞/收藏相关接口返回测试数据（点赞/取消点赞/收藏/取消收藏） | 50min | ✅ 已完成 |
| C-1 | **前端 API层**：在 `src/api/forum.js` 添加点赞/收藏方法（likePost/unlikePost/favoritePost/unfavoritePost） | 30min | ✅ 已完成 |
| C-2 | **前端 API层**：在 `apiConstants.js` 添加点赞/收藏API端点 | 15min | ✅ 已完成 |
| C-3 | **Vuex层**：实现 likePost/unlikePost actions（更新帖子点赞状态和数量） | 35min | ✅ 已完成 |
| C-4 | **Vuex层**：实现 favoritePost/unfavoritePost actions（更新帖子收藏状态和数量） | 35min | ✅ 已完成 |
| C-5 | **UI层**：在 `ForumDetailPage.vue` 添加点赞/收藏按钮（显示状态、切换状态） | 45min | ✅ 已完成 |
| C-6 | **UI层**：在 `ForumListPage.vue` 添加点赞/收藏快捷操作 | 40min | ✅ 已完成 |
| C-7 | **UI层**：实现点赞/收藏状态同步（列表和详情页状态一致） | 40min | ✅ 已完成 |
| C-8 | **闭环自检**：点赞/收藏功能正常、状态正确更新、列表和详情页状态同步 | 35min | ✅ 已完成 |

**注意事项**：
- 点赞/收藏功能可复用用户模块的关注收藏API（addLike/removeLike/addFavorite/removeFavorite），传递类型为"post"
- 或直接在forum模块实现点赞收藏API（更符合模块职责）
- 点赞/收藏状态需在帖子数据中返回（isLiked、isFavorited字段）
- 点赞/收藏数量需实时更新（likeCount、favoriteCount字段）
- 点赞/收藏状态需在列表和详情页保持一致

**任务组C端到端闭环验收（开发期自检）**：
- [ ] 点赞功能正常（按钮状态切换、数量更新）
- [ ] 收藏功能正常（按钮状态切换、数量更新）
- [ ] 点赞/收藏状态在列表和详情页同步
- [ ] 点赞/收藏数量正确更新

---

#### 任务组D：论坛版块管理端到端闭环（Controller → API → Vuex → UI） (优先级: 中)

> **功能域**：论坛版块的列表、创建、管理  
> **说明**：版块管理允许管理员创建和管理论坛版块，用户可以根据版块筛选帖子。

**涉及功能点**：
- 获取版块列表
- 创建版块（管理员）
- 更新版块（管理员）
- 删除版块（管理员）
- 版块排序

**涉及页面**：
- `ForumListPage.vue` - 论坛列表页（版块筛选）
- `ForumManagePage.vue` - 论坛管理页（版块管理，管理员）

**涉及后端接口**：
- `GET /forum/topics` - 获取版块列表
- `POST /forum/topics` - 创建版块（管理员）
- `PUT /forum/topics/{id}` - 更新版块（管理员）
- `DELETE /forum/topics/{id}` - 删除版块（管理员）

**任务清单**：

| 任务ID | 任务描述 | 预计时间 | 状态 |
|--------|----------|----------|------|
| D-0 | **后端 Controller**：版块相关接口返回测试数据（列表/创建/更新/删除） | 60min | ⬜ 待开始 |
| D-1 | **前端 API层**：在 `src/api/forum.js` 添加版块管理方法（getForumTopics/createTopic/updateTopic/deleteTopic） | 35min | ⬜ 待开始 |
| D-2 | **前端 API层**：在 `apiConstants.js` 添加版块API端点 | 15min | ⬜ 待开始 |
| D-3 | **Vuex层**：在 `src/store/modules/forum.js` 添加版块相关 state（forumTopics） | 15min | ⬜ 待开始 |
| D-4 | **Vuex层**：添加版块相关 mutations（SET_FORUM_TOPICS、ADD_TOPIC、UPDATE_TOPIC、REMOVE_TOPIC） | 25min | ⬜ 待开始 |
| D-5 | **Vuex层**：实现 fetchForumTopics action（获取版块列表） | 25min | ⬜ 待开始 |
| D-6 | **Vuex层**：实现 createTopic/updateTopic/deleteTopic actions（管理员功能） | 40min | ⬜ 待开始 |
| D-7 | **UI层**：在 `ForumListPage.vue` 添加版块筛选（版块选择器） | 40min | ⬜ 待开始 |
| D-8 | **UI层**：在 `ForumManagePage.vue` 添加版块管理UI（增删改查、排序） | 70min | ⬜ 待开始 |
| D-9 | **闭环自检**：版块列表可加载、版块筛选正常、版块管理功能正常 | 40min | ⬜ 待开始 |

**注意事项**：
- 版块数据结构需与数据库设计文档一致（参考 `forum_topics` 表结构）
- 版块需包含：名称、描述、图标（可选）、排序
- 版块排序通过拖拽或上下箭头调整
- 删除版块时需确认（防止误删，可能影响已有帖子）
- 权限控制：只有管理员可以创建/更新/删除版块

**任务组D端到端闭环验收（开发期自检）**：
- [ ] 版块列表可正确加载
- [ ] 版块筛选功能正常（根据版块筛选帖子）
- [ ] 版块管理功能正常（创建/编辑/删除/排序）
- [ ] 权限控制正确（只有管理员可以管理版块）

---

#### 任务组E：茶文化文章管理端到端闭环（Controller → API → Vuex → UI） (优先级: 中)

> **功能域**：茶文化文章的列表、详情、增删改查  
> **说明**：文章管理是茶文化内容的核心，管理员可以创建和管理茶文化知识文章。

**涉及功能点**：
- 获取文章列表（支持分页、分类筛选）
- 获取文章详情
- 创建文章（管理员）
- 更新文章（管理员）
- 删除文章（管理员）
- 文章分类管理

**涉及页面**：
- `CultureHomePage.vue` - 茶文化首页（文章展示）
- `ArticleDetailPage.vue` - 文章详情页
- `CultureManagerPage.vue` - 茶文化管理页（文章管理，管理员）

**涉及后端接口**：
- `GET /forum/articles` - 获取文章列表
- `GET /forum/articles/{id}` - 获取文章详情
- `POST /forum/articles` - 创建文章（管理员）
- `PUT /forum/articles/{id}` - 更新文章（管理员）
- `DELETE /forum/articles/{id}` - 删除文章（管理员）

**任务清单**：

| 任务ID | 任务描述 | 预计时间 | 状态 |
|--------|----------|----------|------|
| E-0 | **后端 Controller**：文章相关接口返回测试数据（列表/详情/增删改查） | 80min | ⬜ 待开始 |
| E-1 | **前端 API层**：在 `src/api/forum.js` 添加文章管理方法（getArticles/getArticleDetail/createArticle/updateArticle/deleteArticle） | 40min | ⬜ 待开始 |
| E-2 | **前端 API层**：在 `apiConstants.js` 添加文章API端点 | 15min | ⬜ 待开始 |
| E-3 | **Vuex层**：在 `src/store/modules/forum.js` 添加文章相关 state（articles、currentArticle、articlePagination） | 20min | ⬜ 待开始 |
| E-4 | **Vuex层**：添加文章相关 mutations（SET_ARTICLES、SET_CURRENT_ARTICLE、ADD_ARTICLE、UPDATE_ARTICLE、REMOVE_ARTICLE） | 30min | ⬜ 待开始 |
| E-5 | **Vuex层**：实现 fetchArticles action（获取文章列表，支持分页、分类筛选） | 35min | ⬜ 待开始 |
| E-6 | **Vuex层**：实现 fetchArticleDetail action（获取文章详情） | 25min | ⬜ 待开始 |
| E-7 | **Vuex层**：实现 createArticle/updateArticle/deleteArticle actions（管理员功能） | 45min | ⬜ 待开始 |
| E-8 | **UI层**：完善 `CultureHomePage.vue` 的文章展示（从Vuex读取） | 50min | ⬜ 待开始 |
| E-9 | **UI层**：完善 `ArticleDetailPage.vue`（从Vuex读取文章详情） | 45min | ⬜ 待开始 |
| E-10 | **UI层**：在 `CultureManagerPage.vue` 添加文章管理功能（增删改查） | 80min | ⬜ 待开始 |
| E-11 | **闭环自检**：文章列表可加载、详情可查看、文章管理功能正常 | 45min | ⬜ 待开始 |

**注意事项**：
- 文章数据结构需与数据库设计文档一致（参考 `home_contents` 表结构，type为'article'）
- 文章内容需支持富文本（HTML格式）
- 文章需包含：标题、摘要、内容、分类、封面图（可选）
- 文章分类：历史、产地、泡茶方法、茶叶鉴赏等
- 权限控制：只有管理员可以创建/更新/删除文章

**任务组E端到端闭环验收（开发期自检）**：
- [ ] 文章列表可正确加载（首页展示、列表页展示）
- [ ] 文章详情可正确查看
- [ ] 文章管理功能正常（创建/编辑/删除）
- [ ] 权限控制正确（只有管理员可以管理文章）

---

#### 任务组F：内容审核功能端到端闭环（Controller → API → Vuex → UI） (优先级: 低)

> **功能域**：帖子审核、内容管理（管理员）  
> **说明**：内容审核功能允许管理员审核和管理论坛内容，确保内容质量。

**涉及功能点**：
- 获取待审核帖子列表
- 审核帖子（通过/拒绝）
- 帖子置顶/取消置顶
- 帖子精华/取消精华
- 删除违规帖子

**涉及页面**：
- `ForumManagePage.vue` - 论坛管理页（内容审核，管理员）

**涉及后端接口**：
- `GET /forum/posts/pending` - 获取待审核帖子列表
- `POST /forum/posts/{id}/approve` - 审核通过
- `POST /forum/posts/{id}/reject` - 审核拒绝
- `PUT /forum/posts/{id}/top` - 置顶/取消置顶
- `PUT /forum/posts/{id}/essence` - 精华/取消精华

**任务清单**：

| 任务ID | 任务描述 | 预计时间 | 状态 |
|--------|----------|----------|------|
| F-0 | **后端 Controller**：内容审核相关接口返回测试数据（待审核列表/审核/置顶/精华） | 70min | ⬜ 待开始 |
| F-1 | **前端 API层**：在 `src/api/forum.js` 添加内容审核方法（getPendingPosts/approvePost/rejectPost/toggleTop/toggleEssence） | 45min | ⬜ 待开始 |
| F-2 | **前端 API层**：在 `apiConstants.js` 添加内容审核API端点 | 15min | ⬜ 待开始 |
| F-3 | **Vuex层**：实现 getPendingPosts action（获取待审核帖子列表） | 30min | ⬜ 待开始 |
| F-4 | **Vuex层**：实现 approvePost/rejectPost actions（审核帖子） | 35min | ⬜ 待开始 |
| F-5 | **Vuex层**：实现 toggleTop/toggleEssence actions（置顶/精华） | 30min | ⬜ 待开始 |
| F-6 | **UI层**：在 `ForumManagePage.vue` 添加内容审核功能（待审核列表、审核操作） | 80min | ⬜ 待开始 |
| F-7 | **UI层**：在 `ForumManagePage.vue` 添加置顶/精华操作 | 40min | ⬜ 待开始 |
| F-8 | **闭环自检**：内容审核功能正常、置顶/精华功能正常 | 40min | ⬜ 待开始 |

**注意事项**：
- 审核功能需支持：通过、拒绝（需填写拒绝原因）
- 置顶功能：帖子置顶后显示在版块顶部
- 精华功能：帖子加精后显示精华标识
- 权限控制：只有管理员可以进行内容审核和置顶/精华操作
- 审核拒绝时需填写拒绝原因，并通知用户

**任务组F端到端闭环验收（开发期自检）**：
- [ ] 待审核帖子列表可正确加载
- [ ] 审核功能正常（通过/拒绝、填写原因）
- [ ] 置顶/精华功能正常（状态切换）
- [ ] 权限控制正确（只有管理员可以操作）

---

## 三、开发顺序建议

### 3.1 任务组优先级

```
开发顺序：0 → A → B → C → D → E → F

理由：
1. 任务组0是基础功能，首页展示是用户进入平台的第一印象，必须优先完成
2. 任务组A（帖子基础功能）是论坛核心，必须优先完成
3. 任务组B（回复功能）是帖子功能的必要补充，优先级高
4. 任务组C（点赞收藏）是社区互动的基础，优先级中等
5. 任务组D（版块管理）是帖子分类的基础，优先级中等
6. 任务组E（文章管理）是茶文化内容的核心，优先级中等
7. 任务组F（内容审核）是管理员功能，优先级低
```

### 3.2 依赖关系

- **任务组0** → 所有其他任务组（基础依赖）
- **任务组A** → 任务组B、C、F（回复、点赞收藏、审核都依赖帖子）
- **任务组D** → 任务组A（版块筛选依赖版块列表）

---

## 四、注意事项

### 4.1 数据流向规范

- ✅ **严格遵循数据流向**：组件 → Vuex Actions → API → 后端 Controller
- ❌ **禁止组件直接调用API**：所有API调用必须通过Vuex Actions
- ❌ **禁止UI层伪成功**：不使用 setTimeout 模拟成功，必须使用真实API响应

### 4.2 权限控制

- **普通用户**：可以浏览、发布帖子、回复、点赞、收藏
- **商家**：继承普通用户所有权限
- **管理员**：可以管理所有内容（帖子、回复、文章、Banner）

### 4.3 模块间协作

- **点赞收藏功能**：可复用用户模块的关注收藏API，或直接在forum模块实现
- **@用户功能**：可集成到回复功能中，触发用户模块的通知

### 4.4 数据库设计优先

- 所有字段必须与数据库设计文档一致
- 不能假设后端会为UI调整数据库结构
- 超出数据库能力的功能视为超出项目范围

### 4.5 测试数据要求

- 后端Controller返回的测试数据必须字段齐全
- 测试数据需覆盖各种边界情况（空数据、单条数据、多条数据）
- 测试数据需符合实际业务场景

---

## 五、进度追踪

### 5.1 任务组完成度

| 任务组 | 总任务数 | 已完成 | 完成率 | 状态 |
|--------|----------|--------|--------|------|
| 0-首页功能 | 11 | 11 | 100% | ✅ 已完成 |
| A-帖子基础 | 13 | 13 | 100% | ✅ 已完成 |
| B-回复功能 | 12 | 12 | 100% | ✅ 已完成 |
| C-点赞收藏 | 9 | 9 | 100% | ✅ 已完成 |
| D-版块管理 | 10 | 0 | 0% | ⬜ 未开始 |
| E-文章管理 | 12 | 0 | 0% | ⬜ 未开始 |
| F-内容审核 | 9 | 0 | 0% | ⬜ 未开始 |

### 5.2 里程碑

| 里程碑 | 目标日期 | 实际完成 | 状态 |
|--------|----------|----------|------|
| 首页功能完成 | - | - | ⬜ 未开始 |
| 帖子基础完成 | - | - | ⬜ 未开始 |
| 回复功能完成 | - | - | ⬜ 未开始 |
| 社区互动完成 | - | - | ⬜ 未开始 |
| 内容管理完成 | - | - | ⬜ 未开始 |
| 模块100%完成 | - | - | ⬜ 未开始 |

---

## 六、错误总结

> 此部分将在开发过程中持续更新，记录遇到的问题和解决方案。

### 6.1 已解决问题

| 日期 | 问题描述 | 解决方案 | 相关任务 |
|------|----------|----------|----------|
| - | - | - | - |

### 6.2 待解决问题

| 日期 | 问题描述 | 优先级 | 状态 |
|------|----------|--------|------|
| - | - | - | - |

### 6.3 经验教训

| 日期 | 教训描述 | 影响范围 |
|------|----------|----------|
| - | - | - |

---

**文档版本**: 2.0  
**最后更新**: 2024-12-17
