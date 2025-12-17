import { 
  getHomeData,
  updateHomeData
} from '@/api/forum'

// forum.js - 清空论坛功能，仅保留茶文化首页功能

const state = {
  // 茶文化首页数据
  banners: [],
  cultureFeatures: [],
  teaCategories: [],
  latestNews: [],
  partners: [],
  
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
  async fetchHomeData({ commit }) {
    commit('SET_LOADING', true)
    commit('SET_ERROR', null)
    
    try {
      const response = await getHomeData()
      commit('SET_HOME_DATA', response.data)
      return response.data
    } catch (error) {
      commit('SET_ERROR', error.message || '获取首页数据失败')
      console.error('获取首页数据失败:', error)
      return null
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // 更新首页数据 (管理员功能)
  async updateHomeData({ commit }, homeData) {
    commit('SET_LOADING', true)
    commit('SET_ERROR', null)
    
    try {
      const response = await updateHomeData(homeData)
      commit('SET_HOME_DATA', response.data)
      return response.data
    } catch (error) {
      commit('SET_ERROR', error.message || '更新首页数据失败')
      console.error('更新首页数据失败:', error)
      return null
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // 所有论坛功能已清空，可根据需要未来重新实现
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
} 