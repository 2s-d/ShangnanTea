import { 
  getShops, getShopDetail, getMyShop, getShopTeas, updateShop, createShop,
  addShopTea, updateShopTea, deleteShopTea, toggleShopTeaStatus,
  getShopBanners, uploadBanner, updateBanner, deleteBanner, updateBannerOrder,
  getShopAnnouncements, createAnnouncement, updateAnnouncement, deleteAnnouncement,
  getShopStatistics, uploadShopLogo,
  followShop as apiFollowShop,
  unfollowShop as apiUnfollowShop,
  checkFollowStatus as apiCheckFollowStatus,
  getShopReviews as apiGetShopReviews,
  submitShopReview as apiSubmitShopReview
} from '@/api/shop'

const state = () => ({
  // 商店列表
  shopList: [],
  // 当前查看的商店
  currentShop: null,
  // 我的商店（商家用户）
  myShop: null,
  // 商店茶叶列表
  shopTeas: [],
  // 分页信息
  pagination: {
    total: 0,
    currentPage: 1,
    pageSize: 10
  },
  // 任务组A：筛选条件
  filters: {
    keyword: '',
    rating: null,
    salesCount: null,
    region: '',
    sortBy: 'default', // default, rating, salesCount, createTime
    sortOrder: 'desc' // asc, desc
  },
  // 任务组B：店铺Banner列表
  shopBanners: [],
  // 任务组B：店铺公告列表
  shopAnnouncements: [],
  // 任务组D：店铺统计
  shopStatistics: {
    overview: null,
    trends: [],
    hotProducts: []
  },
  // 任务组F：店铺关注状态（当前店铺）
  isFollowingShop: false,
  // 店铺评价相关
  shopReviews: [],
  reviewPagination: {
    total: 0,
    currentPage: 1,
    pageSize: 10
  },
  // 加载状态
  loading: false
})

const getters = {
  // 获取我的商店ID
  myShopId: state => state.myShop?.id || null,
  
  // 是否拥有商店
  hasShop: state => !!state.myShop
}

const mutations = {
  SET_SHOP_LIST(state, list) {
    state.shopList = list
  },
  SET_CURRENT_SHOP(state, shop) {
    state.currentShop = shop
  },
  SET_MY_SHOP(state, shop) {
    state.myShop = shop
  },
  SET_SHOP_TEAS(state, teas) {
    state.shopTeas = teas
  },
  SET_PAGINATION(state, { total, currentPage, pageSize }) {
    state.pagination = {
      total,
      currentPage,
      pageSize
    }
  },
  SET_LOADING(state, status) {
    state.loading = status
  },
  // 任务组A：筛选条件相关mutations
  SET_FILTERS(state, filters) {
    state.filters = { ...state.filters, ...filters }
  },
  RESET_FILTERS(state) {
    state.filters = {
      keyword: '',
      rating: null,
      salesCount: null,
      region: '',
      sortBy: 'default',
      sortOrder: 'desc'
    }
  },
  // 任务组B：Banner相关mutations
  SET_SHOP_BANNERS(state, banners) {
    state.shopBanners = banners
  },
  ADD_BANNER(state, banner) {
    state.shopBanners.push(banner)
  },
  UPDATE_BANNER(state, banner) {
    const index = state.shopBanners.findIndex(b => b.id === banner.id)
    if (index !== -1) {
      state.shopBanners[index] = banner
    }
  },
  REMOVE_BANNER(state, bannerId) {
    state.shopBanners = state.shopBanners.filter(b => b.id !== bannerId)
  },
  UPDATE_BANNER_ORDER(state, banners) {
    state.shopBanners = banners
  },
  // 任务组B：公告相关mutations
  SET_SHOP_ANNOUNCEMENTS(state, announcements) {
    state.shopAnnouncements = announcements
  },
  ADD_ANNOUNCEMENT(state, announcement) {
    state.shopAnnouncements.push(announcement)
    // 置顶公告排在前面
    state.shopAnnouncements.sort((a, b) => {
      if (a.is_top && !b.is_top) return -1
      if (!a.is_top && b.is_top) return 1
      return new Date(b.create_time) - new Date(a.create_time)
    })
  },
  UPDATE_ANNOUNCEMENT(state, announcement) {
    const index = state.shopAnnouncements.findIndex(a => a.id === announcement.id)
    if (index !== -1) {
      state.shopAnnouncements[index] = announcement
      // 重新排序
      state.shopAnnouncements.sort((a, b) => {
        if (a.is_top && !b.is_top) return -1
        if (!a.is_top && b.is_top) return 1
        return new Date(b.create_time) - new Date(a.create_time)
      })
    }
  },
  REMOVE_ANNOUNCEMENT(state, announcementId) {
    state.shopAnnouncements = state.shopAnnouncements.filter(a => a.id !== announcementId)
  },
  // 任务组D：统计数据
  SET_SHOP_STATISTICS(state, statistics) {
    state.shopStatistics = statistics || { overview: null, trends: [], hotProducts: [] }
  },
  // 任务组F：关注状态
  SET_FOLLOW_STATUS(state, status) {
    state.isFollowingShop = !!status
  },
  // 店铺评价相关mutations
  SET_SHOP_REVIEWS(state, reviews) {
    state.shopReviews = reviews
  },
  ADD_SHOP_REVIEW(state, review) {
    state.shopReviews.unshift(review)
  },
  SET_REVIEW_PAGINATION(state, pagination) {
    state.reviewPagination = { ...state.reviewPagination, ...pagination }
  }
}

const actions = {
  // 获取商店列表（任务组A：支持搜索/筛选/排序）
  // 接口#60: 获取店铺列表 - 成功码200, 失败码5101
  async fetchShops({ commit, state }, extraParams = {}) {
    try {
      commit('SET_LOADING', true)
      
      // 合并筛选条件和额外参数
      const params = {
        page: state.pagination.currentPage,
        size: state.pagination.pageSize,
        ...state.filters,
        ...extraParams
      }
      
      // 移除空值
      Object.keys(params).forEach(key => {
        if (params[key] === '' || params[key] === null || params[key] === undefined) {
          delete params[key]
        }
      })
      
      // 如果sortBy是default，移除排序参数
      if (params.sortBy === 'default') {
        delete params.sortBy
        delete params.sortOrder
      }
      
      const res = await getShops(params)
      
      // 统一处理分页数据格式
      const list = res.data?.list || res.data?.records || (Array.isArray(res.data) ? res.data : [])
      const total = res.data?.total || list.length || 0
      
      commit('SET_SHOP_LIST', list)
      commit('SET_PAGINATION', {
        total: total,
        currentPage: res.data?.pageNum || state.pagination.currentPage,
        pageSize: res.data?.pageSize || state.pagination.pageSize
      })
      return res // 返回 {code, data}
    } catch (error) {
      console.error('获取店铺列表失败:', error)
      commit('SET_SHOP_LIST', [])
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // 任务组A：搜索店铺
  async searchShops({ commit, dispatch, state }, { keyword, page = 1 }) {
    try {
      // 更新筛选条件和分页
      commit('SET_FILTERS', { keyword })
      commit('SET_PAGINATION', {
        ...state.pagination,
        currentPage: page
      })
      
      // 调用fetchShops获取结果
      return await dispatch('fetchShops', {})
    } catch (error) {
      console.error('搜索店铺失败:', error)
      throw error
    }
  },
  
  // 任务组A：更新筛选条件
  async updateFilters({ commit, dispatch, state }, filters) {
    commit('SET_FILTERS', filters)
    // 重置到第一页
    commit('SET_PAGINATION', {
      ...state.pagination,
      currentPage: 1
    })
    // 重新加载列表
    return await dispatch('fetchShops', {})
  },
  
  // 任务组A：重置筛选条件
  async resetFilters({ commit, dispatch, state }) {
    commit('RESET_FILTERS')
    // 重置到第一页
    commit('SET_PAGINATION', {
      ...state.pagination,
      currentPage: 1
    })
    // 重新加载列表
    return await dispatch('fetchShops', {})
  },

  /**
   * 更新分页并重新拉取店铺列表
   * @param {Object} context Vuex context
   * @param {number} page 页码
   * @param {Object} extraParams 额外查询参数（可选）
   * @returns {Promise} 列表数据
   */
  async setPage({ commit, dispatch, state }, { page, extraParams = {} }) {
    commit('SET_PAGINATION', {
      ...state.pagination,
      currentPage: page
    })
    return await dispatch('fetchShops', extraParams)
  },
  
  // 获取商店详情
  // 接口#61: 获取店铺详情 - 成功码200, 失败码5100/5105
  async fetchShopDetail({ commit }, id) {
    try {
      commit('SET_LOADING', true)
      
      const res = await getShopDetail(id)
      commit('SET_CURRENT_SHOP', res.data || null)
      return res // 返回 {code, data}
    } catch (error) {
      console.error('获取店铺详情失败:', error)
      commit('SET_CURRENT_SHOP', null)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // 获取我的商店（商家）
  // 接口#62: 获取我的店铺 - 成功码200, 失败码5100
  async fetchMyShop({ commit }) {
    try {
      commit('SET_LOADING', true)
      
      const res = await getMyShop()
      commit('SET_MY_SHOP', res.data || null)
      return res // 返回 {code, data}
    } catch (error) {
      console.error('获取我的店铺失败:', error)
      commit('SET_MY_SHOP', null)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // 获取商店茶叶（任务组C：支持分页、筛选）
  // 接口#63: 获取店铺茶叶 - 成功码200, 失败码5101
  async fetchShopTeas({ commit }, { shopId, params = {} }) {
    try {
      commit('SET_LOADING', true)
      
      const res = await getShopTeas(shopId, params)
      
      // 统一处理分页数据格式
      const list = res.data?.list || res.data?.records || (Array.isArray(res.data) ? res.data : [])
      
      commit('SET_SHOP_TEAS', list)
      // 更新分页信息
      if (res.data?.total !== undefined) {
        commit('SET_PAGINATION', {
          total: res.data.total,
          currentPage: res.data.pageNum || 1,
          pageSize: res.data.pageSize || 10
        })
      }
      return res // 返回 {code, data}
    } catch (error) {
      console.error('获取店铺茶叶列表失败:', error)
      commit('SET_SHOP_TEAS', [])
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // 更新商店信息
  // 接口#65: 更新店铺 - 成功码5004, 失败码5107
  async updateShop({ commit, state }, shopData) {
    try {
      commit('SET_LOADING', true)
      
      const shopId = shopData.id || state.myShop?.id
      if (!shopId) {
        throw new Error('店铺ID不能为空')
      }
      
      const res = await updateShop({
        ...shopData,
        id: shopId
      })
      
      // 更新我的商店信息
      if (state.myShop && state.myShop.id === shopId) {
        commit('SET_MY_SHOP', res.data)
      }
      
      // 如果当前查看的是我的商店，也更新currentShop
      if (state.currentShop && state.currentShop.id === shopId) {
        commit('SET_CURRENT_SHOP', res.data)
      }
      
      return res // 返回 {code, data}
    } catch (error) {
      console.error('更新店铺信息失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // 任务组C：店铺茶叶管理相关actions
  /**
   * 添加茶叶到店铺
   * 接口#66: 添加店铺茶叶 - 成功码3026, 失败码3125
   * @param {Object} context Vuex context
   * @param {Object} payload { shopId, teaData }
   */
  async addShopTea({ commit, dispatch }, { shopId, teaData }) {
    try {
      commit('SET_LOADING', true)
      
      const res = await addShopTea(shopId, teaData)
      
      // 重新加载店铺茶叶列表
      await dispatch('fetchShopTeas', { shopId, params: {} })
      return res // 返回 {code, data}
    } catch (error) {
      console.error('添加茶叶失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  /**
   * 更新店铺茶叶
   * 接口#67: 更新店铺茶叶 - 成功码3025, 失败码3125
   * @param {Object} context Vuex context
   * @param {Object} payload { teaId, teaData }
   */
  async updateShopTea({ commit, dispatch, state }, { teaId, teaData }) {
    try {
      commit('SET_LOADING', true)
      
      const res = await updateShopTea(teaId, teaData)
      
      // 更新本地列表中的茶叶数据
      const shopTeas = [...state.shopTeas]
      const index = shopTeas.findIndex(tea => tea.id === teaId)
      if (index !== -1 && res.data) {
        shopTeas[index] = res.data
        commit('SET_SHOP_TEAS', shopTeas)
      } else {
        // 如果不在列表中，重新加载
        const shopId = state.myShop?.id || state.currentShop?.id
        if (shopId) {
          await dispatch('fetchShopTeas', { shopId, params: {} })
        }
      }
      return res // 返回 {code, data}
    } catch (error) {
      console.error('更新茶叶失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  /**
   * 删除店铺茶叶
   * 接口#68: 删除店铺茶叶 - 成功码3024, 失败码3124
   * @param {Object} context Vuex context
   * @param {Object} payload { teaId, shopId }
   */
  async deleteShopTea({ commit, dispatch, state }, { teaId, shopId }) {
    try {
      commit('SET_LOADING', true)
      
      const res = await deleteShopTea(teaId)
      
      // 从列表中移除
      const shopTeas = state.shopTeas.filter(tea => tea.id !== teaId)
      commit('SET_SHOP_TEAS', shopTeas)
      
      // 重新加载列表以确保数据同步
      if (shopId) {
        await dispatch('fetchShopTeas', { shopId, params: {} })
      }
      return res // 返回 {code, data}
    } catch (error) {
      console.error('删除茶叶失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  /**
   * 茶叶上下架
   * 接口#69: 茶叶上下架 - 成功码3020/3021, 失败码3120/3121
   * @param {Object} context Vuex context
   * @param {Object} payload { teaId, status }
   */
  async toggleShopTeaStatus({ commit, state }, { teaId, status }) {
    try {
      commit('SET_LOADING', true)
      
      const res = await toggleShopTeaStatus(teaId, status)
      
      // 更新本地列表中的茶叶状态
      const shopTeas = [...state.shopTeas]
      const index = shopTeas.findIndex(tea => tea.id === teaId)
      if (index !== -1) {
        shopTeas[index].status = status
        commit('SET_SHOP_TEAS', shopTeas)
      }
      return res // 返回 {code, data}
    } catch (error) {
      console.error('更新茶叶状态失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // 任务组D：获取店铺统计
  // 接口#70: 获取店铺统计 - 成功码200, 失败码5101
  async fetchShopStatistics({ commit }, { shopId, startDate, endDate } = {}) {
    if (!shopId) {
      throw new Error('店铺ID不能为空')
    }
    try {
      commit('SET_LOADING', true)
      const params = {}
      if (startDate) params.startDate = startDate
      if (endDate) params.endDate = endDate
      
      const res = await getShopStatistics(shopId, params)
      
      commit('SET_SHOP_STATISTICS', {
        overview: res.data?.overview || null,
        trends: res.data?.trends || [],
        hotProducts: res.data?.hotProducts || []
      })
      return res // 返回 {code, data}
    } catch (error) {
      console.error('获取统计数据失败:', error)
      commit('SET_SHOP_STATISTICS', { overview: null, trends: [], hotProducts: [] })
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // 任务组D：上传店铺Logo
  // 接口#71: 上传Logo - 成功码5030, 失败码5130
  async uploadShopLogo({ commit, state }, { shopId, file }) {
    if (!shopId || !file) {
      throw new Error('店铺ID与文件不能为空')
    }
    try {
      commit('SET_LOADING', true)
      
      const res = await uploadShopLogo(shopId, file)
      
      const logoUrl = res.data?.url
      if (state.myShop && state.myShop.id === shopId) {
        commit('SET_MY_SHOP', { ...state.myShop, logo: logoUrl, shop_logo: logoUrl })
      }
      if (state.currentShop && state.currentShop.id === shopId) {
        commit('SET_CURRENT_SHOP', { ...state.currentShop, logo: logoUrl, shop_logo: logoUrl })
      }
      return res // 返回 {code, data}
    } catch (error) {
      console.error('上传Logo失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // 任务组F：获取店铺关注状态
  // 接口#72: 获取关注状态 - 成功码200
  async checkFollowStatus({ commit }, shopId) {
    if (!shopId) {
      throw new Error('店铺ID不能为空')
    }
    try {
      commit('SET_LOADING', true)
      
      const res = await apiCheckFollowStatus(shopId)
      
      commit('SET_FOLLOW_STATUS', res.data?.isFollowing || false)
      return res // 返回 {code, data}
    } catch (error) {
      console.error('获取关注状态失败:', error)
      commit('SET_FOLLOW_STATUS', false)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // 任务组F：关注店铺
  // 接口#73: 关注店铺 - 成功码5000, 失败码5102
  async followShop({ commit, rootState, dispatch }, shopId) {
    if (!shopId) {
      throw new Error('店铺ID不能为空')
    }
    try {
      commit('SET_LOADING', true)
      
      const res = await apiFollowShop(shopId)
      
      // 同步到用户关注列表
      const shop = rootState.shop.currentShop || rootState.shop.myShop || { id: shopId }
      await dispatch('user/addFollow', {
        targetId: shopId,
        targetType: 'shop',
        targetName: shop.name || shop.shop_name || '未知店铺',
        targetAvatar: shop.logo || shop.shop_logo || ''
      }, { root: true })
      commit('SET_FOLLOW_STATUS', true)
      return res // 返回 {code, data}
    } catch (error) {
      console.error('关注店铺失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // 任务组F：取消关注店铺
  // 接口#74: 取消关注 - 成功码5001, 失败码5102
  async unfollowShop({ commit, rootState, dispatch }, shopId) {
    if (!shopId) {
      throw new Error('店铺ID不能为空')
    }
    try {
      commit('SET_LOADING', true)
      
      const res = await apiUnfollowShop(shopId)
      
      // 在用户关注列表中找到该关注记录并删除
      const followList = rootState.user.followList || []
      const followItem = followList.find(item =>
        item.targetType === 'shop' && item.targetId === shopId
      )
      if (followItem) {
        await dispatch('user/removeFollow', followItem.id, { root: true })
      }
      commit('SET_FOLLOW_STATUS', false)
      return res // 返回 {code, data}
    } catch (error) {
      console.error('取消关注店铺失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // 任务组B：获取店铺Banner列表
  // 接口#75: 获取Banner列表 - 成功码200, 失败码5110
  async fetchShopBanners({ commit, state }) {
    const shopId = state.myShop?.id
    if (!shopId) {
      commit('SET_SHOP_BANNERS', [])
      return { code: 200, data: [] }
    }
    try {
      commit('SET_LOADING', true)
      
      const res = await getShopBanners(shopId)
      const banners = Array.isArray(res.data) ? res.data : []
      
      commit('SET_SHOP_BANNERS', banners)
      return res // 返回 {code, data}
    } catch (error) {
      console.error('获取店铺Banner列表失败:', error)
      commit('SET_SHOP_BANNERS', [])
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // 任务组B：获取店铺公告列表
  // 接口#80: 获取公告列表 - 成功码200, 失败码5120
  async fetchShopAnnouncements({ commit, state }) {
    const shopId = state.myShop?.id
    if (!shopId) {
      commit('SET_SHOP_ANNOUNCEMENTS', [])
      return { code: 200, data: [] }
    }
    try {
      commit('SET_LOADING', true)
      
      const res = await getShopAnnouncements(shopId)
      const announcements = Array.isArray(res.data) ? res.data : []
      
      commit('SET_SHOP_ANNOUNCEMENTS', announcements)
      return res // 返回 {code, data}
    } catch (error) {
      console.error('获取店铺公告列表失败:', error)
      commit('SET_SHOP_ANNOUNCEMENTS', [])
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // 创建店铺
  // 接口: 创建店铺 - 成功码5003, 失败码5106
  async createShop({ commit }, shopData = {}) {
    try {
      commit('SET_LOADING', true)
      const res = await createShop(shopData)
      commit('SET_MY_SHOP', res.data || null)
      return res // 返回 {code, data}
    } catch (error) {
      console.error('创建店铺失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // 任务组B：上传店铺Banner
  // 接口#76: 上传Banner - 成功码5010, 失败码5111
  async uploadShopBanner({ commit, state }, bannerData) {
    const shopId = state.myShop?.id
    if (!shopId) {
      throw new Error('店铺ID不能为空')
    }
    try {
      commit('SET_LOADING', true)
      const res = await uploadBanner(shopId, bannerData)
      if (res.data) {
        commit('ADD_BANNER', res.data)
      }
      return res // 返回 {code, data}
    } catch (error) {
      console.error('上传Banner失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // 任务组B：更新店铺Banner
  // 接口#77: 更新Banner - 成功码5011, 失败码5112
  async updateShopBanner({ commit }, { bannerId, bannerData }) {
    if (!bannerId) {
      throw new Error('Banner ID不能为空')
    }
    try {
      commit('SET_LOADING', true)
      const res = await updateBanner(bannerId, bannerData)
      if (res.data) {
        commit('UPDATE_BANNER', res.data)
      }
      return res // 返回 {code, data}
    } catch (error) {
      console.error('更新Banner失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // 任务组B：删除店铺Banner
  // 接口#78: 删除Banner - 成功码5012, 失败码5113
  async deleteShopBanner({ commit }, bannerId) {
    if (!bannerId) {
      throw new Error('Banner ID不能为空')
    }
    try {
      commit('SET_LOADING', true)
      const res = await deleteBanner(bannerId)
      commit('REMOVE_BANNER', bannerId)
      return res // 返回 {code, data}
    } catch (error) {
      console.error('删除Banner失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // 任务组B：更新Banner顺序
  // 接口#79: 更新Banner顺序 - 成功码5013, 失败码5114
  async updateShopBannerOrder({ commit }, orderData) {
    if (!orderData || !Array.isArray(orderData)) {
      throw new Error('排序数据不能为空')
    }
    try {
      commit('SET_LOADING', true)
      const res = await updateBannerOrder(orderData)
      // 更新本地顺序
      if (res.data && Array.isArray(res.data)) {
        commit('UPDATE_BANNER_ORDER', res.data)
      }
      return res // 返回 {code, data}
    } catch (error) {
      console.error('更新Banner顺序失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // 任务组B：创建店铺公告
  // 接口#81: 创建公告 - 成功码5020, 失败码5121
  async createShopAnnouncement({ commit, state }, announcementData) {
    const shopId = state.myShop?.id
    if (!shopId) {
      throw new Error('店铺ID不能为空')
    }
    try {
      commit('SET_LOADING', true)
      const res = await createAnnouncement(shopId, announcementData)
      if (res.data) {
        commit('ADD_ANNOUNCEMENT', res.data)
      }
      return res // 返回 {code, data}
    } catch (error) {
      console.error('创建公告失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // 任务组B：更新店铺公告
  // 接口#82: 更新公告 - 成功码5021, 失败码5122
  async updateShopAnnouncement({ commit }, { announcementId, announcementData }) {
    if (!announcementId) {
      throw new Error('公告ID不能为空')
    }
    try {
      commit('SET_LOADING', true)
      const res = await updateAnnouncement(announcementId, announcementData)
      if (res.data) {
        commit('UPDATE_ANNOUNCEMENT', res.data)
      }
      return res // 返回 {code, data}
    } catch (error) {
      console.error('更新公告失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // 任务组B：删除店铺公告
  // 接口#83: 删除公告 - 成功码5022, 失败码5123
  async deleteShopAnnouncement({ commit }, announcementId) {
    if (!announcementId) {
      throw new Error('公告ID不能为空')
    }
    try {
      commit('SET_LOADING', true)
      const res = await deleteAnnouncement(announcementId)
      commit('REMOVE_ANNOUNCEMENT', announcementId)
      return res // 返回 {code, data}
    } catch (error) {
      console.error('删除公告失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // 获取店铺评价列表
  // 接口#84: 获取评价列表 - 成功码200, 失败码5130
  async fetchShopReviews({ commit }, { shopId, params = {} }) {
    if (!shopId) {
      throw new Error('店铺ID不能为空')
    }
    try {
      commit('SET_LOADING', true)
      const res = await apiGetShopReviews(shopId, params)
      
      const reviews = res.data?.list || res.data?.records || (Array.isArray(res.data) ? res.data : [])
      commit('SET_SHOP_REVIEWS', reviews)
      
      if (res.data?.total !== undefined) {
        commit('SET_REVIEW_PAGINATION', {
          total: res.data.total,
          currentPage: res.data.pageNum || params.page || 1,
          pageSize: res.data.pageSize || params.size || 10
        })
      }
      return res // 返回 {code, data}
    } catch (error) {
      console.error('获取店铺评价失败:', error)
      commit('SET_SHOP_REVIEWS', [])
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // 提交店铺评价
  // 接口#85: 提交评价 - 成功码5040, 失败码5131
  async submitShopReview({ commit }, { shopId, reviewData }) {
    if (!shopId) {
      throw new Error('店铺ID不能为空')
    }
    try {
      commit('SET_LOADING', true)
      const res = await apiSubmitShopReview(shopId, reviewData)
      if (res.data) {
        commit('ADD_SHOP_REVIEW', res.data)
      }
      return res // 返回 {code, data}
    } catch (error) {
      console.error('提交评价失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
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