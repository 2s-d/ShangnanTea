import { getTeas, getTeaDetail, getTeaCategories, addTea, updateTea, deleteTea } from '@/api/tea'

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
    origin: ''
  },
  // 加载状态
  loading: false
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
      
      // 添加过滤条件
      if (state.filters) {
        Object.keys(state.filters).forEach(key => {
          if (state.filters[key]) {
            params[key] = state.filters[key]
          }
        })
      }
      
      const res = await getTeas(params)
      
      commit('SET_TEA_LIST', res.data.list)
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
  
  // 获取茶叶详情
  async fetchTeaDetail({ commit }, id) {
    try {
      commit('SET_LOADING', true)
      
      const res = await getTeaDetail(id)
      commit('SET_CURRENT_TEA', res.data)
      
      return res.data
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // 获取茶叶分类
  async fetchCategories({ commit }) {
    try {
      commit('SET_LOADING', true)
      
      const res = await getTeaCategories()
      commit('SET_CATEGORIES', res.data)
      
      return res.data
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // 添加茶叶
  async addTea({ dispatch }, teaData) {
    const res = await addTea(teaData)
    
    // 重新获取列表
    dispatch('fetchTeas')
    
    return res.data
  },
  
  // 更新茶叶
  async updateTea({ commit, dispatch, state }, teaData) {
    const res = await updateTea(teaData)
    
    // 如果当前查看的就是这个茶叶，更新当前茶叶
    if (state.currentTea && state.currentTea.id === teaData.id) {
      commit('SET_CURRENT_TEA', res.data)
    }
    
    // 重新获取列表
    dispatch('fetchTeas')
    
    return res.data
  },
  
  // 删除茶叶
  async deleteTea({ dispatch }, id) {
    await deleteTea(id)
    
    // 重新获取列表
    dispatch('fetchTeas')
    
    return true
  },
  
  // 更新分页
  setPage({ commit, dispatch, state }, page) {
    commit('SET_PAGINATION', {
      ...state.pagination,
      currentPage: page
    })
    return dispatch('fetchTeas')
  },
  
  // 更新过滤条件
  updateFilters({ commit, dispatch }, filters) {
    commit('SET_FILTERS', filters)
    // 更新过滤条件后重置页码
    commit('SET_PAGINATION', {
      ...state.pagination,
      currentPage: 1
    })
    return dispatch('fetchTeas')
  }
}

export default {
  namespaced: true,
  state,
  getters,
  mutations,
  actions
} 