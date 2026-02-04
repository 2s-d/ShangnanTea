import { 
  getHomeData,
  updateHomeData,
  getBanners,
  uploadBanner,
  updateBanner,
  deleteBanner,
  updateBannerOrder,
  getArticles,
  getArticleDetail,
  createArticle,
  updateArticle,
  deleteArticle,
  getForumTopics,
  getTopicDetail,
  createTopic,
  updateTopic,
  deleteTopic,
  getForumPosts,
  getPostDetail,
  createPost,
  uploadPostImage,
  updatePost,
  deletePost,
  getPostReplies,
  createReply,
  deleteReply,
  // ⚠️ 已删除：likeReply, unlikeReply, likePost, unlikePost, favoritePost, unfavoritePost
  // 说明：帖子/回复的点赞和收藏功能已统一使用用户模块的通用接口（user.js 中的 addLike/removeLike, addFavorite/removeFavorite）
  // 任务组F：内容审核相关
  getPendingPosts,
  approvePost,
  rejectPost,
  togglePostSticky,
  togglePostEssence
} from '@/api/forum'

// forum.js - 清空论坛功能，仅保留茶文化首页功能

const state = {
  // 茶文化首页数据
  banners: [],
  cultureFeatures: [],
  teaCategories: [],
  latestNews: [],
  partners: [],
  
  // 论坛帖子数据
  forumPosts: [],
  currentPost: null,
  postPagination: {
    current: 1,
    pageSize: 10,
    total: 0
  },
  postFilters: {
    topicId: null,
    keyword: '',
    sortBy: 'latest'
  },
  
  // 帖子回复数据
  postReplies: [],
  replyPagination: {
    current: 1,
    pageSize: 20,
    total: 0
  },
  
  // 论坛版块数据
  forumTopics: [],
  
  // 茶文化文章数据
  articles: [],
  currentArticle: null,
  articlePagination: {
    current: 1,
    pageSize: 10,
    total: 0
  },
  
  // 任务组F：内容审核数据
  pendingPosts: [],
  pendingPostsPagination: {
    current: 1,
    pageSize: 10,
    total: 0
  },
  
  // 通用状态
  loading: false,
  error: null
}

const mutations = {
  // 首页数据相关mutations
  SET_BANNERS(state, banners) {
    state.banners = banners
  },
  SET_CULTURE_FEATURES(state, features) {
    state.cultureFeatures = features
  },
  SET_TEA_CATEGORIES(state, categories) {
    state.teaCategories = categories
  },
  SET_LATEST_NEWS(state, news) {
    state.latestNews = news
  },
  SET_PARTNERS(state, partners) {
    state.partners = partners
  },
  SET_HOME_DATA(state, data) {
    state.banners = data.banners || []
    state.cultureFeatures = data.cultureFeatures || []
    state.teaCategories = data.teaCategories || []
    state.latestNews = data.latestNews || []
    state.partners = data.partners || []
  },
  
  // Banner管理相关mutations
  ADD_BANNER(state, banner) {
    state.banners.push(banner)
  },
  UPDATE_BANNER(state, { id, banner }) {
    const index = state.banners.findIndex(b => b.id === id)
    if (index !== -1) {
      state.banners.splice(index, 1, banner)
    }
  },
  REMOVE_BANNER(state, id) {
    state.banners = state.banners.filter(b => b.id !== id)
  },
  UPDATE_BANNER_ORDER(state, banners) {
    state.banners = banners
  },

  // 论坛帖子相关mutations
  SET_FORUM_POSTS(state, posts) {
    state.forumPosts = posts
  },
  SET_CURRENT_POST(state, post) {
    state.currentPost = post
  },
  ADD_POST(state, post) {
    state.forumPosts.unshift(post)
  },
  UPDATE_POST(state, { id, post }) {
    const index = state.forumPosts.findIndex(p => p.id === id)
    if (index !== -1) {
      state.forumPosts.splice(index, 1, post)
    }
  },
  REMOVE_POST(state, id) {
    state.forumPosts = state.forumPosts.filter(p => p.id !== id)
  },
  SET_POST_PAGINATION(state, pagination) {
    state.postPagination = { ...state.postPagination, ...pagination }
  },
  SET_POST_FILTERS(state, filters) {
    state.postFilters = { ...state.postFilters, ...filters }
  },

  // 帖子回复相关mutations
  SET_POST_REPLIES(state, replies) {
    state.postReplies = replies
  },
  ADD_REPLY(state, reply) {
    state.postReplies.push(reply)
  },
  REMOVE_REPLY(state, id) {
    state.postReplies = state.postReplies.filter(r => r.id !== id)
  },
  SET_REPLY_PAGINATION(state, pagination) {
    state.replyPagination = { ...state.replyPagination, ...pagination }
  },
  UPDATE_REPLY_LIKE(state, { id, isLiked, likeCount }) {
    const reply = state.postReplies.find(r => r.id === id)
    if (reply) {
      reply.isLiked = isLiked
      reply.likeCount = likeCount
    }
  },

  // 帖子点赞收藏相关mutations
  UPDATE_POST_LIKE(state, { id, isLiked, likeCount }) {
    // 更新当前帖子的点赞状态
    if (state.currentPost && state.currentPost.id === id) {
      state.currentPost.isLiked = isLiked
      state.currentPost.likeCount = likeCount
    }
    // 更新帖子列表中的点赞状态
    const post = state.forumPosts.find(p => p.id === id)
    if (post) {
      post.isLiked = isLiked
      post.likeCount = likeCount
    }
  },

  UPDATE_POST_FAVORITE(state, { id, isFavorited, favoriteCount }) {
    // 更新当前帖子的收藏状态
    if (state.currentPost && state.currentPost.id === id) {
      state.currentPost.isFavorited = isFavorited
      state.currentPost.favoriteCount = favoriteCount
    }
    // 更新帖子列表中的收藏状态
    const post = state.forumPosts.find(p => p.id === id)
    if (post) {
      post.isFavorited = isFavorited
      post.favoriteCount = favoriteCount
    }
  },

  // 论坛版块相关mutations
  SET_FORUM_TOPICS(state, topics) {
    state.forumTopics = topics
  },
  ADD_TOPIC(state, topic) {
    state.forumTopics.push(topic)
  },
  UPDATE_TOPIC(state, { id, topic }) {
    const index = state.forumTopics.findIndex(t => t.id === id)
    if (index !== -1) {
      state.forumTopics.splice(index, 1, topic)
    }
  },
  REMOVE_TOPIC(state, id) {
    state.forumTopics = state.forumTopics.filter(t => t.id !== id)
  },

  // 茶文化文章相关mutations
  SET_ARTICLES(state, articles) {
    state.articles = articles
  },
  SET_CURRENT_ARTICLE(state, article) {
    state.currentArticle = article
  },
  ADD_ARTICLE(state, article) {
    state.articles.unshift(article)
  },
  UPDATE_ARTICLE(state, { id, article }) {
    const index = state.articles.findIndex(a => a.id === id)
    if (index !== -1) {
      state.articles.splice(index, 1, article)
    }
  },
  REMOVE_ARTICLE(state, id) {
    state.articles = state.articles.filter(a => a.id !== id)
  },
  SET_ARTICLE_PAGINATION(state, pagination) {
    state.articlePagination = { ...state.articlePagination, ...pagination }
  },

  // 任务组F：内容审核相关mutations
  SET_PENDING_POSTS(state, posts) {
    state.pendingPosts = posts
  },
  SET_PENDING_POSTS_PAGINATION(state, pagination) {
    state.pendingPostsPagination = { ...state.pendingPostsPagination, ...pagination }
  },
  REMOVE_PENDING_POST(state, id) {
    state.pendingPosts = state.pendingPosts.filter(p => p.id !== id)
  },
  UPDATE_POST_AUDIT_STATUS(state, { id, auditStatus }) {
    const post = state.pendingPosts.find(p => p.id === id)
    if (post) {
      post.auditStatus = auditStatus
    }
  },

  // 通用状态mutations
  SET_LOADING(state, status) {
    state.loading = status
  },
  SET_ERROR(state, error) {
    state.error = error
  }
}

const actions = {
  // 获取首页数据
  // 接口#108: 获取首页数据 - 成功码200, 失败码6160
  async fetchHomeData({ commit }) {
    commit('SET_LOADING', true)
    commit('SET_ERROR', null)
    
    try {
      const res = await getHomeData()
      commit('SET_HOME_DATA', res.data || {})
      return res // 返回 {code, data}
    } catch (error) {
      commit('SET_ERROR', error.message || '获取首页数据失败')
      console.error('获取首页数据失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // 更新首页数据 (管理员功能)
  // 接口#109: 更新首页数据 - 成功码6060, 失败码6163
  async updateHomeData({ commit }, homeData) {
    commit('SET_LOADING', true)
    commit('SET_ERROR', null)
    
    try {
      const res = await updateHomeData(homeData)
      commit('SET_HOME_DATA', res.data || {})
      return res // 返回 {code, data}
    } catch (error) {
      commit('SET_ERROR', error.message || '更新首页数据失败')
      console.error('更新首页数据失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // Banner管理相关actions
  // 接口#110: 获取Banner列表 - 成功码200, 失败码6161
  async fetchBanners({ commit }) {
    commit('SET_LOADING', true)
    commit('SET_ERROR', null)
    
    try {
      const res = await getBanners()
      commit('SET_BANNERS', res.data || [])
      return res // 返回 {code, data}
    } catch (error) {
      commit('SET_ERROR', error.message || '获取Banner列表失败')
      console.error('获取Banner列表失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // 接口#111: 上传Banner - 成功码5010, 失败码5111
  async uploadBanner({ commit }, formData) {
    commit('SET_LOADING', true)
    commit('SET_ERROR', null)
    
    try {
      const res = await uploadBanner(formData)
      if (res.data) {
        commit('ADD_BANNER', res.data)
      }
      return res // 返回 {code, data}
    } catch (error) {
      commit('SET_ERROR', error.message || '上传Banner失败')
      console.error('上传Banner失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // 接口#112: 更新Banner - 成功码5011, 失败码5111
  async updateBanner({ commit }, { id, data }) {
    commit('SET_LOADING', true)
    commit('SET_ERROR', null)
    
    try {
      const res = await updateBanner(id, data)
      commit('UPDATE_BANNER', { id, banner: res.data })
      return res // 返回 {code, data}
    } catch (error) {
      commit('SET_ERROR', error.message || '更新Banner失败')
      console.error('更新Banner失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // 接口#113: 删除Banner - 成功码5012, 失败码5112
  async deleteBanner({ commit }, id) {
    commit('SET_LOADING', true)
    commit('SET_ERROR', null)
    
    try {
      const res = await deleteBanner(id)
      commit('REMOVE_BANNER', id)
      return res // 返回 {code, data}
    } catch (error) {
      commit('SET_ERROR', error.message || '删除Banner失败')
      console.error('删除Banner失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // 接口#114: 更新Banner顺序 - 成功码5013, 失败码5113
  async updateBannerOrder({ commit }, bannerIds) {
    commit('SET_LOADING', true)
    commit('SET_ERROR', null)
    
    try {
      const res = await updateBannerOrder(bannerIds)
      commit('UPDATE_BANNER_ORDER', res.data || [])
      return res // 返回 {code, data}
    } catch (error) {
      commit('SET_ERROR', error.message || '更新Banner顺序失败')
      console.error('更新Banner顺序失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // 文章管理相关actions
  // 接口#115: 获取文章列表 - 成功码200, 失败码6153
  async fetchArticles({ commit }, params = {}) {
    commit('SET_LOADING', true)
    commit('SET_ERROR', null)
    
    try {
      const res = await getArticles(params)
      const articles = res.data?.articles || (Array.isArray(res.data) ? res.data : [])
      commit('SET_ARTICLES', articles)
      if (res.data?.pagination) {
        commit('SET_ARTICLE_PAGINATION', res.data.pagination)
      }
      return res // 返回 {code, data}
    } catch (error) {
      commit('SET_ERROR', error.message || '获取文章列表失败')
      console.error('获取文章列表失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // 接口#116: 获取文章详情 - 成功码200
  async fetchArticleDetail({ commit }, id) {
    commit('SET_LOADING', true)
    commit('SET_ERROR', null)
    
    try {
      const res = await getArticleDetail(id)
      commit('SET_CURRENT_ARTICLE', res.data)
      return res // 返回 {code, data}
    } catch (error) {
      commit('SET_ERROR', error.message || '获取文章详情失败')
      console.error('获取文章详情失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // 接口#117: 创建文章 - 成功码6050, 失败码6150
  async createArticle({ commit }, data) {
    commit('SET_LOADING', true)
    commit('SET_ERROR', null)
    
    try {
      const res = await createArticle(data)
      if (res.data) {
        commit('ADD_ARTICLE', res.data)
      }
      return res // 返回 {code, data}
    } catch (error) {
      commit('SET_ERROR', error.message || '创建文章失败')
      console.error('创建文章失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // 接口#118: 更新文章 - 成功码6051, 失败码6151
  async updateArticle({ commit }, { id, data }) {
    commit('SET_LOADING', true)
    commit('SET_ERROR', null)
    
    try {
      const res = await updateArticle(id, data)
      commit('UPDATE_ARTICLE', { id, article: res.data })
      return res // 返回 {code, data}
    } catch (error) {
      commit('SET_ERROR', error.message || '更新文章失败')
      console.error('更新文章失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // 接口#119: 删除文章 - 成功码6052, 失败码6152
  async deleteArticle({ commit }, id) {
    commit('SET_LOADING', true)
    commit('SET_ERROR', null)
    
    try {
      const res = await deleteArticle(id)
      commit('REMOVE_ARTICLE', id)
      return res // 返回 {code, data}
    } catch (error) {
      commit('SET_ERROR', error.message || '删除文章失败')
      console.error('删除文章失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // 论坛版块相关actions
  // 接口#120: 获取版块列表 - 成功码200, 失败码6143
  async fetchForumTopics({ commit }) {
    commit('SET_LOADING', true)
    commit('SET_ERROR', null)
    
    try {
      const res = await getForumTopics()
      commit('SET_FORUM_TOPICS', res.data || [])
      return res // 返回 {code, data}
    } catch (error) {
      commit('SET_ERROR', error.message || '获取版块列表失败')
      console.error('获取版块列表失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // 接口#121: 获取版块详情 - 成功码200
  async fetchTopicDetail({ commit }, id) {
    commit('SET_LOADING', true)
    commit('SET_ERROR', null)
    
    try {
      const res = await getTopicDetail(id)
      return res // 返回 {code, data}
    } catch (error) {
      commit('SET_ERROR', error.message || '获取版块详情失败')
      console.error('获取版块详情失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // 接口#122: 创建版块 - 成功码6040, 失败码6140
  async createTopic({ commit }, data) {
    commit('SET_LOADING', true)
    commit('SET_ERROR', null)
    
    try {
      const res = await createTopic(data)
      if (res.data) {
        commit('ADD_TOPIC', res.data)
      }
      return res // 返回 {code, data}
    } catch (error) {
      commit('SET_ERROR', error.message || '创建版块失败')
      console.error('创建版块失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // 接口#123: 更新版块 - 成功码6041, 失败码6141
  async updateTopic({ commit }, { id, data }) {
    commit('SET_LOADING', true)
    commit('SET_ERROR', null)
    
    try {
      const res = await updateTopic(id, data)
      commit('UPDATE_TOPIC', { id, topic: res.data })
      return res // 返回 {code, data}
    } catch (error) {
      commit('SET_ERROR', error.message || '更新版块失败')
      console.error('更新版块失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // 接口#124: 删除版块 - 成功码6042, 失败码6142
  async deleteTopic({ commit }, id) {
    commit('SET_LOADING', true)
    commit('SET_ERROR', null)
    
    try {
      const res = await deleteTopic(id)
      commit('REMOVE_TOPIC', id)
      return res // 返回 {code, data}
    } catch (error) {
      commit('SET_ERROR', error.message || '删除版块失败')
      console.error('删除版块失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // 论坛帖子相关actions
  // 接口#125: 获取帖子列表 - 成功码200, 失败码6103
  async fetchForumPosts({ commit }, params = {}) {
    commit('SET_LOADING', true)
    commit('SET_ERROR', null)
    
    try {
      const res = await getForumPosts(params)
      const posts = res.data?.posts || (Array.isArray(res.data) ? res.data : [])
      commit('SET_FORUM_POSTS', posts)
      if (res.data?.pagination) {
        commit('SET_POST_PAGINATION', res.data.pagination)
      }
      return res // 返回 {code, data}
    } catch (error) {
      commit('SET_ERROR', error.message || '获取帖子列表失败')
      console.error('获取帖子列表失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // 接口#126: 获取帖子详情 - 成功码200, 失败码6104
  async fetchPostDetail({ commit }, id) {
    commit('SET_LOADING', true)
    commit('SET_ERROR', null)
    
    try {
      const res = await getPostDetail(id)
      commit('SET_CURRENT_POST', res.data)
      return res // 返回 {code, data}
    } catch (error) {
      commit('SET_ERROR', error.message || '获取帖子详情失败')
      console.error('获取帖子详情失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // 接口#127: 创建帖子 - 成功码6000, 失败码6100
  async createPost({ commit }, data) {
    commit('SET_LOADING', true)
    commit('SET_ERROR', null)
    
    try {
      const res = await createPost(data)
      if (res.data) {
        commit('ADD_POST', res.data)
      }
      return res // 返回 {code, data}
    } catch (error) {
      commit('SET_ERROR', error.message || '创建帖子失败')
      console.error('创建帖子失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // 接口#37(新增): 上传帖子图片 - 成功码6028, 失败码6140, 6141, 6142
  async uploadPostImage({ commit }, file) {
    try {
      const res = await uploadPostImage(file)
      return res // 返回 {code, data: {url, path}}
    } catch (error) {
      commit('SET_ERROR', error.message || '上传图片失败')
      console.error('上传帖子图片失败:', error)
      throw error
    }
  },

  // 接口#128: 更新帖子 - 成功码6001, 失败码6101
  async updatePost({ commit }, { id, data }) {
    commit('SET_LOADING', true)
    commit('SET_ERROR', null)
    
    try {
      const res = await updatePost(id, data)
      commit('UPDATE_POST', { id, post: res.data })
      // 如果当前正在查看这个帖子，也更新当前帖子数据
      commit('SET_CURRENT_POST', res.data)
      return res // 返回 {code, data}
    } catch (error) {
      commit('SET_ERROR', error.message || '更新帖子失败')
      console.error('更新帖子失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // 接口#129: 删除帖子 - 成功码6002, 失败码6102
  async deletePost({ commit }, id) {
    commit('SET_LOADING', true)
    commit('SET_ERROR', null)
    
    try {
      const res = await deletePost(id)
      commit('REMOVE_POST', id)
      return res // 返回 {code, data}
    } catch (error) {
      commit('SET_ERROR', error.message || '删除帖子失败')
      console.error('删除帖子失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // 设置帖子筛选条件
  setPostFilters({ commit }, filters) {
    commit('SET_POST_FILTERS', filters)
  },

  // 清空当前帖子
  clearCurrentPost({ commit }) {
    commit('SET_CURRENT_POST', null)
  },

  // 帖子回复相关actions
  // 接口#130: 获取回复列表 - 成功码200, 失败码6123
  async fetchPostReplies({ commit }, { postId, params = {} }) {
    commit('SET_LOADING', true)
    commit('SET_ERROR', null)
    
    try {
      const res = await getPostReplies(postId, params)
      const replies = res.data?.replies || (Array.isArray(res.data) ? res.data : [])
      commit('SET_POST_REPLIES', replies)
      if (res.data?.pagination) {
        commit('SET_REPLY_PAGINATION', res.data.pagination)
      }
      return res // 返回 {code, data}
    } catch (error) {
      commit('SET_ERROR', error.message || '获取回复列表失败')
      console.error('获取回复列表失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // 接口#131: 创建回复 - 成功码6022, 失败码6122
  async createReply({ commit }, { postId, data }) {
    commit('SET_LOADING', true)
    commit('SET_ERROR', null)
    
    try {
      const res = await createReply(postId, data)
      if (res.data) {
        commit('ADD_REPLY', res.data)
      }
      return res // 返回 {code, data}
    } catch (error) {
      commit('SET_ERROR', error.message || '发布回复失败')
      console.error('发布回复失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // 接口#132: 删除回复 - 成功码6021, 失败码6121
  async deleteReply({ commit }, id) {
    commit('SET_LOADING', true)
    commit('SET_ERROR', null)
    
    try {
      const res = await deleteReply(id)
      commit('REMOVE_REPLY', id)
      return res // 返回 {code, data}
    } catch (error) {
      commit('SET_ERROR', error.message || '删除回复失败')
      console.error('删除回复失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // 接口#133: 点赞回复 - 成功码6010, 失败码6110
  async likeReply({ commit }, id) {
    try {
      const res = await likeReply(id)
      commit('UPDATE_REPLY_LIKE', { id, isLiked: true, likeCount: res.data?.likeCount })
      return res // 返回 {code, data}
    } catch (error) {
      commit('SET_ERROR', error.message || '点赞失败')
      console.error('点赞回复失败:', error)
      throw error
    }
  },

  // 接口#134: 取消点赞回复 - 成功码6011, 失败码6111
  async unlikeReply({ commit }, id) {
    try {
      const res = await unlikeReply(id)
      commit('UPDATE_REPLY_LIKE', { id, isLiked: false, likeCount: res.data?.likeCount })
      return res // 返回 {code, data}
    } catch (error) {
      commit('SET_ERROR', error.message || '取消点赞失败')
      console.error('取消点赞回复失败:', error)
      throw error
    }
  },

  // 清空回复列表
  clearPostReplies({ commit }) {
    commit('SET_POST_REPLIES', [])
    commit('SET_REPLY_PAGINATION', { current: 1, pageSize: 20, total: 0 })
  },

  // 帖子点赞收藏相关actions
  // 接口#135: 点赞帖子 - 成功码6010, 失败码6110
  async likePost({ commit }, id) {
    try {
      const res = await likePost(id)
      commit('UPDATE_POST_LIKE', { 
        id, 
        isLiked: true, 
        likeCount: res.data?.likeCount 
      })
      return res // 返回 {code, data}
    } catch (error) {
      commit('SET_ERROR', error.message || '点赞失败')
      console.error('点赞帖子失败:', error)
      throw error
    }
  },

  // 接口#136: 取消点赞帖子 - 成功码6011, 失败码6111
  async unlikePost({ commit }, id) {
    try {
      const res = await unlikePost(id)
      commit('UPDATE_POST_LIKE', { 
        id, 
        isLiked: false, 
        likeCount: res.data?.likeCount 
      })
      return res // 返回 {code, data}
    } catch (error) {
      commit('SET_ERROR', error.message || '取消点赞失败')
      console.error('取消点赞帖子失败:', error)
      throw error
    }
  },

  // 接口#137: 收藏帖子 - 成功码6012, 失败码6112
  async favoritePost({ commit }, id) {
    try {
      const res = await favoritePost(id)
      commit('UPDATE_POST_FAVORITE', { 
        id, 
        isFavorited: true, 
        favoriteCount: res.data?.favoriteCount 
      })
      return res // 返回 {code, data}
    } catch (error) {
      commit('SET_ERROR', error.message || '收藏失败')
      console.error('收藏帖子失败:', error)
      throw error
    }
  },

  // 接口#138: 取消收藏帖子 - 成功码6013, 失败码6113
  async unfavoritePost({ commit }, id) {
    try {
      const res = await unfavoritePost(id)
      commit('UPDATE_POST_FAVORITE', { 
        id, 
        isFavorited: false, 
        favoriteCount: res.data?.favoriteCount 
      })
      return res // 返回 {code, data}
    } catch (error) {
      commit('SET_ERROR', error.message || '取消收藏失败')
      console.error('取消收藏帖子失败:', error)
      throw error
    }
  },

  // 任务组F：内容审核相关actions
  // 接口#139: 获取待审核帖子 - 成功码200, 失败码6136
  async fetchPendingPosts({ commit }, params = {}) {
    commit('SET_LOADING', true)
    commit('SET_ERROR', null)
    
    try {
      const res = await getPendingPosts(params)
      const posts = res.data?.posts || (Array.isArray(res.data) ? res.data : [])
      commit('SET_PENDING_POSTS', posts)
      if (res.data?.pagination) {
        commit('SET_PENDING_POSTS_PAGINATION', res.data.pagination)
      }
      return res // 返回 {code, data}
    } catch (error) {
      commit('SET_ERROR', error.message || '获取待审核帖子列表失败')
      console.error('获取待审核帖子列表失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // 接口#140: 审核通过 - 成功码6034, 失败码6134
  async approvePost({ commit }, { id, data }) {
    commit('SET_LOADING', true)
    commit('SET_ERROR', null)
    
    try {
      const res = await approvePost(id, data)
      commit('UPDATE_POST_AUDIT_STATUS', { id, auditStatus: 'approved' })
      commit('REMOVE_PENDING_POST', id)
      return res // 返回 {code, data}
    } catch (error) {
      commit('SET_ERROR', error.message || '审核通过失败')
      console.error('审核通过失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // 接口#141: 审核拒绝 - 成功码6035, 失败码6135
  async rejectPost({ commit }, { id, data }) {
    commit('SET_LOADING', true)
    commit('SET_ERROR', null)
    
    try {
      const res = await rejectPost(id, data)
      commit('UPDATE_POST_AUDIT_STATUS', { id, auditStatus: 'rejected' })
      commit('REMOVE_PENDING_POST', id)
      return res // 返回 {code, data}
    } catch (error) {
      commit('SET_ERROR', error.message || '审核拒绝失败')
      console.error('审核拒绝失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // 接口#142: 置顶帖子 - 成功码6030/6031, 失败码6130/6131
  async togglePostSticky({ commit, state }, { id, isSticky }) {
    try {
      const res = await togglePostSticky(id, isSticky)
      // 通过 mutation 更新帖子列表中的置顶状态
      const postIndex = state.forumPosts.findIndex(p => p.id === id)
      if (postIndex !== -1) {
        const updatedPost = { ...state.forumPosts[postIndex], isSticky }
        commit('UPDATE_POST', { id, post: updatedPost })
      }
      // 更新当前帖子的置顶状态
      if (state.currentPost && state.currentPost.id === id) {
        commit('SET_CURRENT_POST', { ...state.currentPost, isSticky })
      }
      return res // 返回 {code, data}
    } catch (error) {
      commit('SET_ERROR', error.message || '置顶操作失败')
      console.error('置顶操作失败:', error)
      throw error
    }
  },

  // 接口#143: 加精帖子 - 成功码6032/6033, 失败码6132/6133
  async togglePostEssence({ commit, state }, { id, isEssence }) {
    try {
      const res = await togglePostEssence(id, isEssence)
      // 通过 mutation 更新帖子列表中的精华状态
      const postIndex = state.forumPosts.findIndex(p => p.id === id)
      if (postIndex !== -1) {
        const updatedPost = { ...state.forumPosts[postIndex], isEssence }
        commit('UPDATE_POST', { id, post: updatedPost })
      }
      // 更新当前帖子的精华状态
      if (state.currentPost && state.currentPost.id === id) {
        commit('SET_CURRENT_POST', { ...state.currentPost, isEssence })
      }
      return res // 返回 {code, data}
    } catch (error) {
      commit('SET_ERROR', error.message || '精华操作失败')
      console.error('精华操作失败:', error)
      throw error
    }
  },

  // 清空待审核帖子列表
  clearPendingPosts({ commit }) {
    commit('SET_PENDING_POSTS', [])
    commit('SET_PENDING_POSTS_PAGINATION', { current: 1, pageSize: 10, total: 0 })
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
} 