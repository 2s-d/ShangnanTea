import { defineStore } from 'pinia'
import { ref, reactive } from 'vue'
import { 
  getHomeData,
  updateHomeData as updateHomeDataApi,
  getBanners,
  uploadBanner as uploadBannerApi,
  updateBanner as updateBannerApi,
  deleteBanner as deleteBannerApi,
  updateBannerOrder as updateBannerOrderApi,
  getArticles,
  getArticleDetail,
  createArticle as createArticleApi,
  updateArticle as updateArticleApi,
  deleteArticle as deleteArticleApi,
  getForumTopics,
  getTopicDetail,
  createTopic as createTopicApi,
  updateTopic as updateTopicApi,
  deleteTopic as deleteTopicApi,
  getForumPosts,
  getPostDetail,
  createPost as createPostApi,
  uploadPostImage as uploadPostImageApi,
  updatePost as updatePostApi,
  deletePost as deletePostApi,
  getPostReplies,
  createReply as createReplyApi,
  deleteReply as deleteReplyApi,
  getPendingPosts,
  approvePost as approvePostApi,
  rejectPost as rejectPostApi,
  togglePostSticky as togglePostStickyApi,
  togglePostEssence as togglePostEssenceApi,
  getCategories
} from '@/api/forum'

export const useForumStore = defineStore('forum', () => {
  // ========== State ==========
  
  // 茶文化首页数据
  const homeData = ref({})
  const banners = ref([])
  const cultureFeatures = ref([])
  const teaCategories = ref([])
  // 文章分类（从后端分类管理接口获取，用于首页栏目）
  const categories = ref([])
  const latestNews = ref([])
  const partners = ref([])
  
  // 论坛帖子数据
  const forumPosts = ref([])
  const currentPost = ref(null)
  const postPagination = reactive({
    current: 1,
    pageSize: 10,
    total: 0
  })
  const postFilters = reactive({
    topicId: null,
    keyword: '',
    sortBy: 'latest'
  })
  
  // 帖子回复数据
  const postReplies = ref([])
  const replyPagination = reactive({
    current: 1,
    pageSize: 20,
    total: 0
  })
  
  // 论坛版块数据
  const forumTopics = ref([])
  
  // 茶文化文章数据
  const articles = ref([])
  const currentArticle = ref(null)
  const articlePagination = reactive({
    current: 1,
    pageSize: 10,
    total: 0
  })
  
  // 内容审核数据
  const pendingPosts = ref([])
  const pendingPostsPagination = reactive({
    current: 1,
    pageSize: 10,
    total: 0
  })
  
  // 通用状态
  const loading = ref(false)
  const error = ref(null)
  
  // ========== Actions ==========
  
  // ========== 首页数据 Actions ==========
  
  // 获取首页数据
  async function fetchHomeData() {
    loading.value = true
    error.value = null
    
    try {
      const res = await getHomeData()
      const data = res.data || {}
      banners.value = data.banners || []
      cultureFeatures.value = data.cultureFeatures || []
      teaCategories.value = data.teaCategories || []
      latestNews.value = data.latestNews || []
      partners.value = data.partners || []
      return res
    } catch (err) {
      error.value = err.message || '获取首页数据失败'
      console.error('获取首页数据失败:', err)
      throw err
    } finally {
      loading.value = false
    }
  }
  
  /**
   * 获取文章分类列表（首页栏目使用）
   */
  async function fetchCategories() {
    loading.value = true
    error.value = null
    
    try {
      const res = await getCategories()
      const data = res.data || {}
      // 后端直接返回数组，兼容 { list: [] } 或 { categories: [] }
      const list = Array.isArray(data)
        ? data
        : data.list || data.categories || []
      categories.value = Array.isArray(list) ? list : []
      return res
    } catch (err) {
      error.value = err.message || '获取分类列表失败'
      console.error('获取分类列表失败:', err)
      throw err
    } finally {
      loading.value = false
    }
  }
  
  // 更新首页数据
  async function updateHomeData(homeData) {
    loading.value = true
    error.value = null
    
    try {
      const res = await updateHomeDataApi(homeData)
      const data = res.data || {}
      banners.value = data.banners || []
      cultureFeatures.value = data.cultureFeatures || []
      teaCategories.value = data.teaCategories || []
      latestNews.value = data.latestNews || []
      partners.value = data.partners || []
      return res
    } catch (err) {
      error.value = err.message || '更新首页数据失败'
      console.error('更新首页数据失败:', err)
      throw err
    } finally {
      loading.value = false
    }
  }
  
  // ========== Banner管理 Actions ==========
  
  // 获取Banner列表
  async function fetchBanners() {
    loading.value = true
    error.value = null
    
    try {
      const res = await getBanners()
      banners.value = res.data || []
      return res
    } catch (err) {
      error.value = err.message || '获取Banner列表失败'
      console.error('获取Banner列表失败:', err)
      throw err
    } finally {
      loading.value = false
    }
  }
  
  // 上传Banner
  async function uploadBanner(formData) {
    loading.value = true
    error.value = null
    
    try {
      const res = await uploadBannerApi(formData)
      if (res.data) {
        banners.value.push(res.data)
      }
      return res
    } catch (err) {
      error.value = err.message || '上传Banner失败'
      console.error('上传Banner失败:', err)
      throw err
    } finally {
      loading.value = false
    }
  }
  
  // 更新Banner
  async function updateBanner({ id, data }) {
    loading.value = true
    error.value = null
    
    try {
      const res = await updateBannerApi(id, data)
      const index = banners.value.findIndex(b => b.id === id)
      if (index !== -1) {
        banners.value.splice(index, 1, res.data)
      }
      return res
    } catch (err) {
      error.value = err.message || '更新Banner失败'
      console.error('更新Banner失败:', err)
      throw err
    } finally {
      loading.value = false
    }
  }
  
  // 删除Banner
  async function deleteBanner(id) {
    loading.value = true
    error.value = null
    
    try {
      const res = await deleteBannerApi(id)
      banners.value = banners.value.filter(b => b.id !== id)
      return res
    } catch (err) {
      error.value = err.message || '删除Banner失败'
      console.error('删除Banner失败:', err)
      throw err
    } finally {
      loading.value = false
    }
  }
  
  // 更新Banner顺序
  async function updateBannerOrder(bannerIds) {
    loading.value = true
    error.value = null
    
    try {
      const res = await updateBannerOrderApi(bannerIds)
      banners.value = res.data || []
      return res
    } catch (err) {
      error.value = err.message || '更新Banner顺序失败'
      console.error('更新Banner顺序失败:', err)
      throw err
    } finally {
      loading.value = false
    }
  }
  
  // ========== 文章管理 Actions ==========
  
  // 获取文章列表
  async function fetchArticles(params = {}) {
    loading.value = true
    error.value = null
    
    try {
      const res = await getArticles(params)
      const data = res.data || {}
      // 后端返回格式：{ list, total }，兼容旧版 { articles, pagination } 和直接数组
      const list = data.list || data.articles || (Array.isArray(data) ? data : [])
      articles.value = Array.isArray(list) ? list : []
      
      // 分页信息（当前只用 total，page/pageSize 前端自己控制）
      articlePagination.total = typeof data.total === 'number'
        ? data.total
        : articles.value.length
      return res
    } catch (err) {
      error.value = err.message || '获取文章列表失败'
      console.error('获取文章列表失败:', err)
      throw err
    } finally {
      loading.value = false
    }
  }
  
  // 获取文章详情
  async function fetchArticleDetail(id) {
    loading.value = true
    error.value = null
    
    try {
      const res = await getArticleDetail(id)
      currentArticle.value = res.data
      return res
    } catch (err) {
      error.value = err.message || '获取文章详情失败'
      console.error('获取文章详情失败:', err)
      throw err
    } finally {
      loading.value = false
    }
  }
  
  // 创建文章
  async function createArticle(data) {
    loading.value = true
    error.value = null
    
    try {
      const res = await createArticleApi(data)
      if (res.data) {
        articles.value.unshift(res.data)
      }
      return res
    } catch (err) {
      error.value = err.message || '创建文章失败'
      console.error('创建文章失败:', err)
      throw err
    } finally {
      loading.value = false
    }
  }
  
  // 更新文章
  async function updateArticle({ id, data }) {
    loading.value = true
    error.value = null
    
    try {
      const res = await updateArticleApi(id, data)
      const index = articles.value.findIndex(a => a.id === id)
      if (index !== -1) {
        articles.value.splice(index, 1, res.data)
      }
      return res
    } catch (err) {
      error.value = err.message || '更新文章失败'
      console.error('更新文章失败:', err)
      throw err
    } finally {
      loading.value = false
    }
  }
  
  // 删除文章
  async function deleteArticle(id) {
    loading.value = true
    error.value = null
    
    try {
      const res = await deleteArticleApi(id)
      articles.value = articles.value.filter(a => a.id !== id)
      return res
    } catch (err) {
      error.value = err.message || '删除文章失败'
      console.error('删除文章失败:', err)
      throw err
    } finally {
      loading.value = false
    }
  }
  
  // ========== 论坛版块 Actions ==========
  
  // 获取版块列表
  async function fetchForumTopics() {
    loading.value = true
    error.value = null
    
    try {
      const res = await getForumTopics()
      forumTopics.value = res.data || []
      return res
    } catch (err) {
      error.value = err.message || '获取版块列表失败'
      console.error('获取版块列表失败:', err)
      throw err
    } finally {
      loading.value = false
    }
  }
  
  // 获取版块详情
  async function fetchTopicDetail(id) {
    loading.value = true
    error.value = null
    
    try {
      const res = await getTopicDetail(id)
      return res
    } catch (err) {
      error.value = err.message || '获取版块详情失败'
      console.error('获取版块详情失败:', err)
      throw err
    } finally {
      loading.value = false
    }
  }
  
  // 创建版块
  async function createTopic(data) {
    loading.value = true
    error.value = null
    
    try {
      const res = await createTopicApi(data)
      if (res.data) {
        forumTopics.value.push(res.data)
      }
      return res
    } catch (err) {
      error.value = err.message || '创建版块失败'
      console.error('创建版块失败:', err)
      throw err
    } finally {
      loading.value = false
    }
  }
  
  // 更新版块
  async function updateTopic({ id, data }) {
    loading.value = true
    error.value = null
    
    try {
      const res = await updateTopicApi(id, data)
      const index = forumTopics.value.findIndex(t => t.id === id)
      if (index !== -1) {
        forumTopics.value.splice(index, 1, res.data)
      }
      return res
    } catch (err) {
      error.value = err.message || '更新版块失败'
      console.error('更新版块失败:', err)
      throw err
    } finally {
      loading.value = false
    }
  }
  
  // 删除版块
  async function deleteTopic(id) {
    loading.value = true
    error.value = null
    
    try {
      const res = await deleteTopicApi(id)
      forumTopics.value = forumTopics.value.filter(t => t.id !== id)
      return res
    } catch (err) {
      error.value = err.message || '删除版块失败'
      console.error('删除版块失败:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  // 论坛帖子相关 Actions
  async function fetchForumPosts(params = {}) {
    loading.value = true
    error.value = null
    
    try {
      const res = await getForumPosts(params)
      const data = res.data || {}
      // 后端返回结构：{ list: [...], total: number }
      const posts = Array.isArray(data.list) ? data.list : (Array.isArray(data.posts) ? data.posts : [])
      forumPosts.value = posts
      // 更新分页信息（使用内存分页结果）
      postPagination.current = params.page || 1
      postPagination.pageSize = params.pageSize || 10
      postPagination.total = typeof data.total === 'number' ? data.total : posts.length
      return res
    } catch (err) {
      error.value = err.message || '获取帖子列表失败'
      console.error('获取帖子列表失败:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  async function fetchPostDetail(id) {
    loading.value = true
    error.value = null
    
    try {
      const res = await getPostDetail(id)
      currentPost.value = res.data
      return res
    } catch (err) {
      error.value = err.message || '获取帖子详情失败'
      console.error('获取帖子详情失败:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  async function createPost(data) {
    loading.value = true
    error.value = null
    
    try {
      const res = await createPostApi(data)
      if (res.data) {
        forumPosts.value.unshift(res.data)
      }
      return res
    } catch (err) {
      error.value = err.message || '创建帖子失败'
      console.error('创建帖子失败:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  async function uploadPostImage(file) {
    try {
      const res = await uploadPostImageApi(file)
      return res
    } catch (err) {
      error.value = err.message || '上传图片失败'
      console.error('上传帖子图片失败:', err)
      throw err
    }
  }

  async function updatePost(id, data) {
    loading.value = true
    error.value = null
    
    try {
      const res = await updatePostApi(id, data)
      const index = forumPosts.value.findIndex(p => p.id === id)
      if (index !== -1) {
        forumPosts.value.splice(index, 1, res.data)
      }
      if (currentPost.value && currentPost.value.id === id) {
        currentPost.value = res.data
      }
      return res
    } catch (err) {
      error.value = err.message || '更新帖子失败'
      console.error('更新帖子失败:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  async function deletePost(id) {
    loading.value = true
    error.value = null
    
    try {
      const res = await deletePostApi(id)
      forumPosts.value = forumPosts.value.filter(p => p.id !== id)
      return res
    } catch (err) {
      error.value = err.message || '删除帖子失败'
      console.error('删除帖子失败:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  // 帖子回复相关 Actions
  async function fetchPostReplies(postId, params = {}) {
    loading.value = true
    error.value = null
    
    try {
      const res = await getPostReplies(postId, params)
      // 后端返回格式：{code: 200, data: {total: 3, list: [...]}}
      const replies = res.data?.list || res.data?.replies || (Array.isArray(res.data) ? res.data : [])
      postReplies.value = replies
      // 处理分页信息：后端返回的是 {total, list}，需要转换为分页格式
      if (res.data?.total !== undefined && res.data?.total !== null) {
        replyPagination.value = {
          ...replyPagination.value,
          total: res.data.total,
          current: params.page || replyPagination.value.current || 1,
          pageSize: params.size || replyPagination.value.pageSize || 20
        }
      } else if (res.data?.pagination) {
        replyPagination.value = { ...replyPagination.value, ...res.data.pagination }
      } else {
        // 如果没有total字段，使用list的长度作为total（至少显示当前页的数量）
        replyPagination.value = {
          ...replyPagination.value,
          total: replies.length,
          current: params.page || replyPagination.value.current || 1,
          pageSize: params.size || replyPagination.value.pageSize || 20
        }
      }
      return res
    } catch (err) {
      error.value = err.message || '获取回复列表失败'
      console.error('获取回复列表失败:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  async function createReply(postId, data) {
    loading.value = true
    error.value = null
    
    try {
      const res = await createReplyApi(postId, data)
      if (res.data) {
        postReplies.value.push(res.data)
      }
      return res
    } catch (err) {
      error.value = err.message || '发布回复失败'
      console.error('发布回复失败:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  async function deleteReply(id) {
    loading.value = true
    error.value = null
    
    try {
      const res = await deleteReplyApi(id)
      postReplies.value = postReplies.value.filter(r => r.id !== id)
      return res
    } catch (err) {
      error.value = err.message || '删除回复失败'
      console.error('删除回复失败:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  // 内容审核相关 Actions
  async function fetchPendingPosts(params = {}) {
    loading.value = true
    error.value = null
    
    try {
      const res = await getPendingPosts(params)
      const data = res.data || {}
      // 后端返回结构：{ list: [...], total: number }
      const posts = Array.isArray(data.list) ? data.list : (Array.isArray(data.posts) ? data.posts : (Array.isArray(data) ? data : []))
      pendingPosts.value = posts
      // 更新分页信息
      pendingPostsPagination.current = params.page || 1
      pendingPostsPagination.pageSize = params.pageSize || 10
      pendingPostsPagination.total = typeof data.total === 'number' ? data.total : posts.length
      return res
    } catch (err) {
      error.value = err.message || '获取待审核帖子列表失败'
      console.error('获取待审核帖子列表失败:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  async function approvePost(id, data) {
    loading.value = true
    error.value = null
    
    try {
      const res = await approvePostApi(id, data)
      pendingPosts.value = pendingPosts.value.filter(p => p.id !== id)
      return res
    } catch (err) {
      error.value = err.message || '审核通过失败'
      console.error('审核通过失败:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  async function rejectPost(id, data) {
    loading.value = true
    error.value = null
    
    try {
      const res = await rejectPostApi(id, data)
      pendingPosts.value = pendingPosts.value.filter(p => p.id !== id)
      return res
    } catch (err) {
      error.value = err.message || '审核拒绝失败'
      console.error('审核拒绝失败:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  async function togglePostSticky(id, isSticky) {
    try {
      const res = await togglePostStickyApi(id, isSticky)
      const postIndex = forumPosts.value.findIndex(p => p.id === id)
      if (postIndex !== -1) {
        forumPosts.value[postIndex].isSticky = isSticky
      }
      if (currentPost.value && currentPost.value.id === id) {
        currentPost.value.isSticky = isSticky
      }
      return res
    } catch (err) {
      error.value = err.message || '置顶操作失败'
      console.error('置顶操作失败:', err)
      throw err
    }
  }

  async function togglePostEssence(id, isEssence) {
    try {
      const res = await togglePostEssenceApi(id, isEssence)
      const postIndex = forumPosts.value.findIndex(p => p.id === id)
      if (postIndex !== -1) {
        forumPosts.value[postIndex].isEssence = isEssence
      }
      if (currentPost.value && currentPost.value.id === id) {
        currentPost.value.isEssence = isEssence
      }
      return res
    } catch (err) {
      error.value = err.message || '精华操作失败'
      console.error('精华操作失败:', err)
      throw err
    }
  }

  // 辅助函数
  function setPostFilters(filters) {
    postFilters.value = { ...postFilters.value, ...filters }
  }

  function clearCurrentPost() {
    currentPost.value = null
  }

  function clearPostReplies() {
    postReplies.value = []
    replyPagination.value = { current: 1, pageSize: 20, total: 0 }
  }

  function clearPendingPosts() {
    pendingPosts.value = []
    pendingPostsPagination.value = { current: 1, pageSize: 10, total: 0 }
  }

  return {
    // State
    homeData,
    banners,
    categories,
    articles,
    currentArticle,
    articlePagination,
    forumTopics,
    forumPosts,
    currentPost,
    postPagination,
    postFilters,
    postReplies,
    replyPagination,
    pendingPosts,
    pendingPostsPagination,
    loading,
    error,
    
    // Actions - 首页数据
    fetchHomeData,
    updateHomeData,
    fetchCategories,
    
    // Actions - Banner管理
    fetchBanners,
    uploadBanner,
    updateBanner,
    deleteBanner,
    updateBannerOrder,
    
    // Actions - 文章管理
    fetchArticles,
    fetchArticleDetail,
    createArticle,
    updateArticle,
    deleteArticle,
    
    // Actions - 论坛版块
    fetchForumTopics,
    fetchTopicDetail,
    createTopic,
    updateTopic,
    deleteTopic,
    
    // Actions - 论坛帖子
    fetchForumPosts,
    fetchPostDetail,
    createPost,
    uploadPostImage,
    updatePost,
    deletePost,
    
    // Actions - 帖子回复
    fetchPostReplies,
    createReply,
    deleteReply,
    
    // Actions - 内容审核
    fetchPendingPosts,
    approvePost,
    rejectPost,
    togglePostSticky,
    togglePostEssence,
    
    // 辅助函数
    setPostFilters,
    clearCurrentPost,
    clearPostReplies,
    clearPendingPosts
  }
})
