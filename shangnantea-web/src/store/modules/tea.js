import { getTeas, getTeaDetail, getTeaCategories, createCategory, updateCategory, deleteCategory, addTea, updateTea, deleteTea, getTeaReviews, getReviewStats, replyReview, /* ⚠️ 已删除：likeReview - 评价点赞功能已统一使用用户模块的通用接口 */ getTeaSpecifications, addSpecification, updateSpecification, deleteSpecification, setDefaultSpecification, uploadTeaImages, deleteTeaImage, updateImageOrder, setMainImage, toggleTeaStatus, batchToggleTeaStatus, getRecommendTeas } from '@/api/tea'

const state = () => ({
  // 茶叶列表
  teaList: [],
  // 当前查看的茶叶详情
  currentTea: null,
  // 分类列表
  categories: [],
  // 分页信息
  pagination: {
    total: 0,
    currentPage: 1,
    pageSize: 10
  },
  // 过滤条件
  filters: {
    category: '',
    keyword: '',
    priceRange: [0, 1000],
    origin: '',
    rating: null, // 最低评分
    sortBy: '', // 排序字段：price, sales, rating, time
    sortOrder: 'asc' // 排序方向：asc, desc
  },
  // 加载状态
  loading: false,
  // 任务组B：评价相关state
  teaReviews: [], // 当前茶叶的评价列表
  reviewPagination: {
    total: 0,
    currentPage: 1,
    pageSize: 10
  },
  reviewStats: null, // 评价统计数据
  // 任务组C：规格管理相关state
  currentTeaSpecs: [], // 当前茶叶的规格列表
  selectedSpec: null, // 当前选中的规格
  // 任务组D：图片管理相关state
  teaImages: [], // 当前茶叶的图片列表
  // 任务组F：推荐功能相关state
  recommendTeas: [] // 推荐茶叶列表
})

const getters = {
  // 获取分类选项
  categoryOptions: state => state.categories.map(item => ({
    value: item.id,
    label: item.name
  }))
}

const mutations = {
  SET_TEA_LIST(state, list) {
    state.teaList = list
  },
  SET_CURRENT_TEA(state, tea) {
    state.currentTea = tea
  },
  SET_CATEGORIES(state, categories) {
    state.categories = categories
  },
  SET_PAGINATION(state, { total, currentPage, pageSize }) {
    state.pagination = {
      total,
      currentPage,
      pageSize
    }
  },
  SET_FILTERS(state, filters) {
    state.filters = { ...state.filters, ...filters }
  },
  SET_LOADING(state, status) {
    state.loading = status
  },
  // 任务组B：评价相关mutations
  SET_TEA_REVIEWS(state, reviews) {
    state.teaReviews = reviews
  },
  SET_REVIEW_PAGINATION(state, pagination) {
    state.reviewPagination = { ...state.reviewPagination, ...pagination }
  },
  SET_REVIEW_STATS(state, stats) {
    state.reviewStats = stats
  },
  ADD_REVIEW(state, review) {
    state.teaReviews.unshift(review)
    state.reviewPagination.total += 1
  },
  UPDATE_REVIEW(state, review) {
    const index = state.teaReviews.findIndex(r => r.id === review.id)
    if (index !== -1) {
      state.teaReviews.splice(index, 1, review)
    }
  },
  // 任务组C：规格管理相关mutations
  SET_TEA_SPECIFICATIONS(state, specs) {
    state.currentTeaSpecs = specs
  },
  SET_SELECTED_SPEC(state, spec) {
    state.selectedSpec = spec
  },
  ADD_SPECIFICATION(state, spec) {
    state.currentTeaSpecs.push(spec)
  },
  UPDATE_SPECIFICATION(state, spec) {
    const index = state.currentTeaSpecs.findIndex(s => s.id === spec.id)
    if (index !== -1) {
      state.currentTeaSpecs.splice(index, 1, spec)
    }
  },
  REMOVE_SPECIFICATION(state, specId) {
    state.currentTeaSpecs = state.currentTeaSpecs.filter(s => s.id !== specId)
  },
  // 任务组D：图片管理相关mutations
  SET_TEA_IMAGES(state, images) {
    state.teaImages = images
  },
  ADD_TEA_IMAGE(state, image) {
    state.teaImages.push(image)
  },
  REMOVE_TEA_IMAGE(state, imageId) {
    state.teaImages = state.teaImages.filter(img => img.id !== imageId)
  },
  UPDATE_IMAGE_ORDER(state, orders) {
    // orders格式: [{imageId: 'img_1', order: 1}, ...]
    orders.forEach(({ imageId, order }) => {
      const image = state.teaImages.find(img => img.id === imageId)
      if (image) {
        image.order = order
      }
    })
    // 按order排序
    state.teaImages.sort((a, b) => (a.order || 0) - (b.order || 0))
  },
  SET_MAIN_IMAGE(state, imageId) {
    // 将所有图片的is_main设为0
    state.teaImages.forEach(img => {
      img.is_main = img.id === imageId ? 1 : 0
    })
  },
  // 任务组F：推荐功能相关mutations
  SET_RECOMMEND_TEAS(state, teas) {
    state.recommendTeas = teas
  }
}

const actions = {
  // 获取茶叶列表
  async fetchTeas({ commit, state }) {
    try {
      commit('SET_LOADING', true)
      
      // 构建查询参数
      const params = {
        page: state.pagination.currentPage,
        pageSize: state.pagination.pageSize
      }
      
      // 添加过滤条件（映射到后端接口参数）
      if (state.filters) {
        if (state.filters.category) {
          params.category = state.filters.category
        }
        if (state.filters.keyword) {
          params.keyword = state.filters.keyword
        }
        if (state.filters.priceRange && Array.isArray(state.filters.priceRange) && state.filters.priceRange.length === 2) {
          params.priceMin = state.filters.priceRange[0]
          params.priceMax = state.filters.priceRange[1]
        }
        if (state.filters.origin) {
          params.origin = state.filters.origin
        }
        if (state.filters.status !== undefined && state.filters.status !== null && state.filters.status !== '') {
          params.status = state.filters.status
        }
        if (state.filters.rating !== null && state.filters.rating !== undefined) {
          params.rating = state.filters.rating
        }
        if (state.filters.sortBy) {
          params.sortBy = state.filters.sortBy
        }
        if (state.filters.sortOrder) {
          params.sortOrder = state.filters.sortOrder
        }
      }
      
      const res = await getTeas(params)
      
      // 统一处理分页数据格式
      const list = res.data?.list || res.data?.records || (Array.isArray(res.data) ? res.data : [])
      const total = res.data?.total || list.length || 0
      
      commit('SET_TEA_LIST', list)
      commit('SET_PAGINATION', {
        total: total,
        currentPage: res.data?.pageNum || state.pagination.currentPage,
        pageSize: res.data?.pageSize || state.pagination.pageSize
      })
      
      return res // 返回 {code, data}
    } catch (error) {
      console.error('获取茶叶列表失败:', error)
      commit('SET_TEA_LIST', [])
      commit('SET_PAGINATION', {
        total: 0,
        currentPage: 1,
        pageSize: state.pagination.pageSize
      })
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // 获取茶叶详情
  async fetchTeaDetail({ commit }, id) {
    try {
      commit('SET_LOADING', true)
      
      const res = await getTeaDetail(id)
      commit('SET_CURRENT_TEA', res.data || null)
      
      return res // 返回 {code, data}
    } catch (error) {
      console.error('获取茶叶详情失败:', error)
      commit('SET_CURRENT_TEA', null)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // 获取茶叶分类
  async fetchCategories({ commit }) {
    try {
      commit('SET_LOADING', true)
      
      const res = await getTeaCategories()
      const categories = res.data || []
      commit('SET_CATEGORIES', categories)
      
      return res // 返回 {code, data}
    } catch (error) {
      console.error('获取茶叶分类失败:', error)
      commit('SET_CATEGORIES', [])
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // 创建茶叶分类
  async createCategory({ dispatch }, categoryData) {
    try {
      const res = await createCategory(categoryData)
      // 刷新分类列表
      await dispatch('fetchCategories')
      return res // 返回 {code, data}
    } catch (error) {
      console.error('创建茶叶分类失败:', error)
      throw error
    }
  },
  
  // 更新茶叶分类
  async updateCategory({ dispatch }, { id, categoryData }) {
    try {
      const res = await updateCategory(id, categoryData)
      // 刷新分类列表
      await dispatch('fetchCategories')
      return res // 返回 {code, data}
    } catch (error) {
      console.error('更新茶叶分类失败:', error)
      throw error
    }
  },
  
  // 删除茶叶分类
  async deleteCategory({ dispatch }, id) {
    try {
      const res = await deleteCategory(id)
      // 刷新分类列表
      await dispatch('fetchCategories')
      return res // 返回 {code, data}
    } catch (error) {
      console.error('删除茶叶分类失败:', error)
      throw error
    }
  },
  
  // 任务A-4：更新筛选条件
  updateFilters({ commit, dispatch, state }, newFilters) {
    commit('SET_FILTERS', newFilters)
    // 重置到第一页
    commit('SET_PAGINATION', {
      ...state.pagination,
      currentPage: 1
    })
    // 重新获取数据
    dispatch('fetchTeas')
  },
  
  // 任务A-5：重置筛选条件
  resetFilters({ commit, dispatch }) {
    const defaultFilters = {
      category: '',
      keyword: '',
      priceRange: [0, 1000],
      origin: '',
      rating: null,
      sortBy: '',
      sortOrder: 'asc'
    }
    commit('SET_FILTERS', defaultFilters)
    commit('SET_PAGINATION', {
      total: 0,
      currentPage: 1,
      pageSize: 10
    })
    dispatch('fetchTeas')
  },
  
  // 设置分页
  setPage({ commit, dispatch, state }, page) {
    commit('SET_PAGINATION', {
      ...state.pagination,
      currentPage: page
    })
    dispatch('fetchTeas')
  },
  
  // 设置排序
  setSort({ commit, dispatch, state }, { sortBy, sortOrder }) {
    commit('SET_FILTERS', {
      ...state.filters,
      sortBy,
      sortOrder: sortOrder || 'asc'
    })
    // 重置到第一页
    commit('SET_PAGINATION', {
      ...state.pagination,
      currentPage: 1
    })
    dispatch('fetchTeas')
  },
  
  // 添加茶叶
  async addTea({ dispatch }, teaData) {
    try {
      const res = await addTea(teaData)
      
      // 重新获取列表
      await dispatch('fetchTeas')
      
      return res // 返回 {code, data}
    } catch (error) {
      console.error('添加茶叶失败:', error)
      throw error
    }
  },
  
  // 更新茶叶
  async updateTea({ commit, dispatch, state }, teaData) {
    try {
      const res = await updateTea(teaData)
      
      // 如果当前查看的就是这个茶叶，更新当前茶叶
      if (state.currentTea && state.currentTea.id === teaData.id) {
        commit('SET_CURRENT_TEA', res.data || null)
      }
      
      // 重新获取列表
      await dispatch('fetchTeas')
      
      return res // 返回 {code, data}
    } catch (error) {
      console.error('更新茶叶失败:', error)
      throw error
    }
  },
  
  // 删除茶叶
  async deleteTea({ dispatch }, id) {
    try {
      const res = await deleteTea(id)
      
      // 重新获取列表
      await dispatch('fetchTeas')
      
      return res // 返回 {code, data}
    } catch (error) {
      console.error('删除茶叶失败:', error)
      throw error
    }
  },
  
  // ==================== 任务组B：评价系统Actions ====================
  
  // 获取茶叶评价列表
  async fetchTeaReviews({ commit }, { teaId, page = 1, pageSize = 10 }) {
    try {
      commit('SET_LOADING', true)
      
      const res = await getTeaReviews(teaId, { page, pageSize })
      
      // 统一处理分页数据格式
      const list = res.data?.list || res.data?.records || (Array.isArray(res.data) ? res.data : [])
      const total = res.data?.total || list.length || 0
      
      commit('SET_TEA_REVIEWS', list)
      commit('SET_REVIEW_PAGINATION', {
        total: total,
        currentPage: res.data?.pageNum || page,
        pageSize: res.data?.pageSize || pageSize
      })
      
      return res // 返回 {code, data}
    } catch (error) {
      console.error('获取茶叶评价列表失败:', error)
      commit('SET_TEA_REVIEWS', [])
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // 获取评价统计数据
  async fetchReviewStats({ commit }, teaId) {
    try {
      const res = await getReviewStats(teaId)
      commit('SET_REVIEW_STATS', res.data || null)
      return res // 返回 {code, data}
    } catch (error) {
      console.error('获取评价统计数据失败:', error)
      commit('SET_REVIEW_STATS', null)
      throw error
    }
  },
  
  // 商家回复评价
  async replyReview({ commit, state }, { reviewId, reply }) {
    try {
      const res = await replyReview(reviewId, { reply })
      
      // 更新评价列表中的回复
      const review = state.teaReviews.find(r => r.id === reviewId)
      if (review) {
        review.reply = reply
        review.replyTime = res.data?.replyTime
        commit('UPDATE_REVIEW', review)
      }
      
      return res // 返回 {code, data}
    } catch (error) {
      console.error('商家回复评价失败:', error)
      throw error
    }
  },
  
  // ⚠️ 已删除：点赞评价action（likeReview）
  // 说明：评价点赞功能已统一使用用户模块的通用接口（user.js 中的 addLike/removeLike）
  // 评价列表接口（getTeaReviews）已包含 isLiked 字段，无需单独调用点赞接口
  // 组件应直接调用 user/addLike, user/removeLike
  
  // ==================== 任务组C：规格管理Actions ====================
  
  // 获取茶叶规格列表
  async fetchTeaSpecifications({ commit }, teaId) {
    try {
      const res = await getTeaSpecifications(teaId)
      commit('SET_TEA_SPECIFICATIONS', res.data || [])
      return res // 返回 {code, data}
    } catch (error) {
      console.error('获取茶叶规格列表失败:', error)
      commit('SET_TEA_SPECIFICATIONS', [])
      throw error
    }
  },
  
  // 添加规格
  async addSpecification({ commit, dispatch, state }, { teaId, specData }) {
    try {
      const res = await addSpecification(teaId, specData)
      
      // 如果设置了默认规格，需要取消其他规格的默认状态
      if (specData.is_default === 1) {
        state.currentTeaSpecs.forEach(spec => {
          if (res.data && spec.id !== res.data.id) {
            spec.is_default = 0
          }
        })
      }
      
      if (res.data) {
        commit('ADD_SPECIFICATION', res.data)
      }
      
      // 刷新规格列表
      await dispatch('fetchTeaSpecifications', teaId)
      
      return res // 返回 {code, data}
    } catch (error) {
      console.error('添加规格失败:', error)
      throw error
    }
  },
  
  // 更新规格
  async updateSpecification({ commit, dispatch, state }, { teaId, specId, specData }) {
    try {
      const res = await updateSpecification(specId, specData)
      
      // 如果设置了默认规格，需要取消其他规格的默认状态
      if (specData.is_default === 1) {
        state.currentTeaSpecs.forEach(spec => {
          if (spec.id !== specId) {
            spec.is_default = 0
          }
        })
      }
      
      if (res.data) {
        commit('UPDATE_SPECIFICATION', res.data)
      }
      
      // 刷新规格列表
      await dispatch('fetchTeaSpecifications', teaId)
      
      return res // 返回 {code, data}
    } catch (error) {
      console.error('更新规格失败:', error)
      throw error
    }
  },
  
  // 删除规格
  async deleteSpecification({ commit, dispatch }, { teaId, specId }) {
    try {
      const res = await deleteSpecification(specId)
      
      commit('REMOVE_SPECIFICATION', specId)
      
      // 刷新规格列表
      await dispatch('fetchTeaSpecifications', teaId)
      
      return res // 返回 {code, data}
    } catch (error) {
      console.error('删除规格失败:', error)
      throw error
    }
  },
  
  // 设置默认规格
  async setDefaultSpecification({ commit, dispatch, state }, { teaId, specId }) {
    try {
      const res = await setDefaultSpecification(specId)
      
      // 取消其他规格的默认状态
      state.currentTeaSpecs.forEach(spec => {
        if (spec.id === specId) {
          spec.is_default = 1
        } else {
          spec.is_default = 0
        }
      })
      
      // 刷新规格列表
      await dispatch('fetchTeaSpecifications', teaId)
      
      return res // 返回 {code, data}
    } catch (error) {
      console.error('设置默认规格失败:', error)
      throw error
    }
  },
  
  // 任务组D：图片管理相关actions
  /**
   * 上传茶叶图片
   * @param {Object} context Vuex context
   * @param {Object} payload { teaId, files: File[] }
   */
  async uploadTeaImages({ commit, state }, { teaId, files }) {
    try {
      // 创建FormData
      const formData = new FormData()
      files.forEach(file => {
        formData.append('files', file)
      })
      
      const res = await uploadTeaImages(teaId, formData)
      
      // 从 res.data 获取图片数组
      const images = res.data || []
      commit('SET_TEA_IMAGES', images)
      
      // 如果当前茶叶是正在查看的茶叶，更新currentTea的images
      if (state.currentTea && state.currentTea.id === teaId) {
        state.currentTea.images = images
      }
      
      return res // 返回 {code, data}
    } catch (error) {
      console.error('上传图片失败:', error)
      throw error
    }
  },
  
  /**
   * 删除茶叶图片
   * @param {Object} context Vuex context
   * @param {Object} payload { teaId, imageId }
   */
  async deleteTeaImage({ commit, state }, { teaId, imageId }) {
    try {
      const res = await deleteTeaImage(imageId)
      
      // 从state中移除
      commit('REMOVE_TEA_IMAGE', imageId)
      
      // 如果当前茶叶是正在查看的茶叶，更新currentTea的images
      if (state.currentTea && state.currentTea.id === teaId) {
        state.currentTea.images = state.currentTea.images.filter(img => img.id !== imageId)
      }
      
      return res // 返回 {code, data}
    } catch (error) {
      console.error('删除图片失败:', error)
      throw error
    }
  },
  
  /**
   * 更新图片顺序
   * @param {Object} context Vuex context
   * @param {Object} payload { teaId, orders: [{imageId, order}] }
   */
  async updateImageOrder({ commit }, { orders }) {
    try {
      const res = await updateImageOrder(orders)
      
      // 更新state中的顺序
      commit('UPDATE_IMAGE_ORDER', orders)
      return res // 返回 {code, data}
    } catch (error) {
      console.error('更新图片顺序失败:', error)
      throw error
    }
  },
  
  /**
   * 设置主图
   * @param {Object} context Vuex context
   * @param {Object} payload { teaId, imageId }
   */
  async setMainImage({ commit, state }, { teaId, imageId }) {
    try {
      const res = await setMainImage(imageId)
      
      // 更新state中的主图标记
      commit('SET_MAIN_IMAGE', imageId)
      
      // 如果当前茶叶是正在查看的茶叶，更新currentTea的main_image
      if (state.currentTea && state.currentTea.id === teaId) {
        const mainImage = state.teaImages.find(img => img.id === imageId)
        if (mainImage) {
          state.currentTea.main_image = mainImage.url
        }
      }
      
      return res // 返回 {code, data}
    } catch (error) {
      console.error('设置主图失败:', error)
      throw error
    }
  },
  
  // 任务组E：状态管理相关actions
  /**
   * 更新茶叶状态（上架/下架）
   * @param {Object} context Vuex context
   * @param {Object} payload { teaId, status }
   */
  async toggleTeaStatus({ state }, { teaId, status }) {
    try {
      const res = await toggleTeaStatus(teaId, { status })
      
      // 更新列表中的茶叶状态
      const tea = state.teaList.find(t => t.id === teaId || t.id === String(teaId))
      if (tea) {
        tea.status = status
      }
      
      // 如果当前查看的茶叶是目标茶叶，更新currentTea
      if (state.currentTea && (state.currentTea.id === teaId || state.currentTea.id === String(teaId))) {
        state.currentTea.status = status
      }
      
      return res // 返回 {code, data}
    } catch (error) {
      console.error('更新茶叶状态失败:', error)
      throw error
    }
  },
  
  /**
   * 批量更新茶叶状态（上架/下架）
   * @param {Object} context Vuex context
   * @param {Object} payload { teaIds: string[], status: number }
   */
  async batchToggleTeaStatus({ state }, { teaIds, status }) {
    try {
      const res = await batchToggleTeaStatus({ teaIds, status })
      
      // 更新列表中的茶叶状态
      teaIds.forEach(teaId => {
        const tea = state.teaList.find(t => t.id === teaId || t.id === String(teaId))
        if (tea) {
          tea.status = status
        }
      })
      
      // 如果当前查看的茶叶在批量列表中，更新currentTea
      if (state.currentTea && (teaIds.includes(state.currentTea.id) || teaIds.includes(String(state.currentTea.id)))) {
        state.currentTea.status = status
      }
      
      return res // 返回 {code, data}
    } catch (error) {
      console.error('批量更新茶叶状态失败:', error)
      throw error
    }
  },
  
  // 任务组F：推荐功能相关actions
  /**
   * 获取推荐茶叶
   * @param {Object} context Vuex context
   * @param {Object} payload { type: 'random'|'similar'|'popular', teaId?: string, count?: number }
   */
  async fetchRecommendTeas({ commit }, { type = 'random', teaId = null, count = 6 }) {
    try {
      const params = { type, count }
      if (teaId) {
        params.teaId = teaId
      }
      
      const res = await getRecommendTeas(params)
      
      // 从 res.data 获取推荐列表
      const teas = res.data || []
      commit('SET_RECOMMEND_TEAS', teas)
      return res // 返回 {code, data}
    } catch (error) {
      console.error('获取推荐茶叶失败:', error)
      commit('SET_RECOMMEND_TEAS', [])
      throw error
    }
  }
}

export default {
  namespaced: true,
  state,
  getters,
  mutations,
  actions
} 