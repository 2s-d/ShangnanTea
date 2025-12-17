import { getMessages, getMessageDetail, sendMessage, markAsRead, 
         deleteMessages, getUnreadCount, getChatSessions, getChatHistory } from '@/api/message'

const state = () => ({
  // 消息列表
  messages: [],
  // 当前消息
  currentMessage: null,
  // 未读消息数量
  unreadCount: 0,
  // 聊天会话列表
  chatSessions: [],
  // 聊天历史
  chatHistory: [],
  // 当前聊天用户ID
  currentChatUserId: null,
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
  // 获取系统通知
  systemNotifications: state => state.messages.filter(msg => msg.type === 'system'),
  
  // 获取私信
  privateMessages: state => state.messages.filter(msg => msg.type === 'private')
}

const mutations = {
  SET_MESSAGES(state, messages) {
    state.messages = messages
  },
  SET_CURRENT_MESSAGE(state, message) {
    state.currentMessage = message
  },
  SET_UNREAD_COUNT(state, count) {
    state.unreadCount = count
  },
  SET_CHAT_SESSIONS(state, sessions) {
    state.chatSessions = sessions
  },
  SET_CHAT_HISTORY(state, history) {
    state.chatHistory = history
  },
  SET_CURRENT_CHAT_USER(state, userId) {
    state.currentChatUserId = userId
  },
  SET_PAGINATION(state, { total, currentPage, pageSize }) {
    state.pagination = {
      total,
      currentPage,
      pageSize
    }
  },
  ADD_CHAT_MESSAGE(state, message) {
    // 添加新消息到聊天历史
    state.chatHistory.push(message)
  },
  DELETE_MESSAGE(state, id) {
    state.messages = state.messages.filter(msg => msg.id !== id)
  },
  SET_LOADING(state, status) {
    state.loading = status
  }
}

const actions = {
  // 获取消息列表
  async fetchMessages({ commit, state }, params = {}) {
    try {
      commit('SET_LOADING', true)
      
      const queryParams = {
        page: state.pagination.currentPage,
        size: state.pagination.pageSize,
        ...params
      }
      
      const res = await getMessages(queryParams)
      
      commit('SET_MESSAGES', res.data.list)
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
  
  // 获取消息详情
  async fetchMessageDetail({ commit }, id) {
    try {
      commit('SET_LOADING', true)
      
      const res = await getMessageDetail(id)
      commit('SET_CURRENT_MESSAGE', res.data)
      
      // 标记为已读
      markAsRead(id)
      
      return res.data
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // 发送消息
  async sendMessage({ commit, state }, messageData) {
    const res = await sendMessage(messageData)
    
    // 如果是发送给当前聊天用户，添加到聊天历史
    if (messageData.receiverId === state.currentChatUserId) {
      commit('ADD_CHAT_MESSAGE', res.data)
    }
    
    return res.data
  },
  
  // 标记消息已读
  async markAsRead({ commit, dispatch }, ids) {
    await markAsRead(ids)
    
    // 更新未读消息数量
    dispatch('fetchUnreadCount')
    
    return true
  },
  
  // 删除消息
  async deleteMessage({ commit }, id) {
    await deleteMessages(id)
    
    // 更新本地状态
    commit('DELETE_MESSAGE', id)
    
    return true
  },
  
  // 获取未读消息数量
  async fetchUnreadCount({ commit }) {
    const res = await getUnreadCount()
    commit('SET_UNREAD_COUNT', res.data)
    
    return res.data
  },
  
  // 获取聊天会话列表
  async fetchChatSessions({ commit }) {
    try {
      commit('SET_LOADING', true)
      
      const res = await getChatSessions()
      commit('SET_CHAT_SESSIONS', res.data)
      
      return res.data
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // 获取聊天历史
  async fetchChatHistory({ commit }, { userId, params = {} }) {
    try {
      commit('SET_LOADING', true)
      commit('SET_CURRENT_CHAT_USER', userId)
      
      const res = await getChatHistory(userId, params)
      commit('SET_CHAT_HISTORY', res.data)
      
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