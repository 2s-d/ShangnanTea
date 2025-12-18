import { getShops, getShopDetail, getMyShop, getShopTeas, updateShop } from '@/api/shop'

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
  }
}

const actions = {
  // 获取商店列表
  async fetchShops({ commit, state }, extraParams = {}) {
    try {
      commit('SET_LOADING', true)
      
      const params = {
        page: state.pagination.currentPage,
        size: state.pagination.pageSize,
        ...extraParams
      }
      
      const res = await getShops(params)
      
      commit('SET_SHOP_LIST', res.data.list)
      commit('SET_PAGINATION', {
        total: res.data.total,
        currentPage: state.pagination.currentPage,
        pageSize: state.pagination.pageSize
      })
      
      return res.data
    } finally {
      commit('SET_LOADING', false)
    }
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
  async fetchShopDetail({ commit }, id) {
    try {
      commit('SET_LOADING', true)
      
      const res = await getShopDetail(id)
      commit('SET_CURRENT_SHOP', res.data)
      
      return res.data
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // 获取我的商店（商家）
  async fetchMyShop({ commit }) {
    try {
      commit('SET_LOADING', true)
      
      const res = await getMyShop()
      commit('SET_MY_SHOP', res.data)
      
      return res.data
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // 获取商店茶叶
  async fetchShopTeas({ commit }, { shopId, params = {} }) {
    try {
      commit('SET_LOADING', true)
      
      const res = await getShopTeas(shopId, params)
      commit('SET_SHOP_TEAS', res.data.list)
      
      return res.data
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // 更新商店信息
  async updateShop({ commit, state }, shopData) {
    try {
      commit('SET_LOADING', true)
      
      const res = await updateShop({
        ...shopData,
        id: state.myShop?.id
      })
      
      // 更新我的商店信息
      commit('SET_MY_SHOP', res.data)
      
      // 如果当前查看的是我的商店，也更新currentShop
      if (state.currentShop && state.currentShop.id === state.myShop?.id) {
        commit('SET_CURRENT_SHOP', res.data)
      }
      
      return res.data
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