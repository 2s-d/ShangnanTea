import {
  getMessages,
  getMessageDetail,
  sendMessage,
  markAsRead,
  deleteMessages,
  getUnreadCount,
  getChatSessions,
  getChatHistory,
  getNotifications,
  getNotificationDetail,
  deleteNotification,
  batchMarkAsRead,
  batchDeleteNotifications,
  createChatSession,
  getUserProfile,
  getUserDynamic,
  getUserStatistics,
  pinChatSession,
  deleteChatSession,
  sendImageMessage,
  getUserPosts,
  getUserReviews
} from '@/api/message'

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
  loading: false,
  // 用户主页信息
  userProfile: null,
  // 用户动态
  userDynamic: null,
  // 用户统计数据
  userStatistics: null,
  // 用户发布的帖子列表
  userPosts: [],
  // 用户评价记录
  userReviews: [],
  // 发布内容分页信息
  postsPagination: {
    total: 0,
    currentPage: 1,
    pageSize: 10
  },
  reviewsPagination: {
    total: 0,
    currentPage: 1,
    pageSize: 10
  }
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
  DELETE_MESSAGES(state, ids) {
    const idsArray = Array.isArray(ids) ? ids : [ids]
    state.messages = state.messages.filter(msg => !idsArray.includes(msg.id))
  },
  MARK_MESSAGE_AS_READ(state, id) {
    const message = state.messages.find(msg => msg.id === id)
    if (message) {
      message.isRead = 1
    }
  },
  MARK_MESSAGES_AS_READ(state, ids) {
    const idsArray = Array.isArray(ids) ? ids : [ids]
    state.messages.forEach(msg => {
      if (idsArray.includes(msg.id)) {
        msg.isRead = 1
      }
    })
  },
  SET_LOADING(state, status) {
    state.loading = status
  },
  SET_USER_PROFILE(state, profile) {
    state.userProfile = profile
  },
  SET_USER_DYNAMIC(state, dynamic) {
    state.userDynamic = dynamic
  },
  SET_USER_STATISTICS(state, statistics) {
    state.userStatistics = statistics
  },
  PIN_CHAT_SESSION(state, sessionId) {
    const session = state.chatSessions.find(s => s.id === sessionId)
    if (session) {
      session.isPinned = true
      // 将置顶会话移到列表前面
      const index = state.chatSessions.indexOf(session)
      state.chatSessions.splice(index, 1)
      state.chatSessions.unshift(session)
    }
  },
  REMOVE_CHAT_SESSION(state, sessionId) {
    state.chatSessions = state.chatSessions.filter(s => s.id !== sessionId)
  },
  SET_USER_POSTS(state, { posts, pagination }) {
    state.userPosts = posts
    if (pagination) {
      state.postsPagination = pagination
    }
  },
  SET_USER_REVIEWS(state, { reviews, pagination }) {
    state.userReviews = reviews
    if (pagination) {
      state.reviewsPagination = pagination
    }
  }
}

const actions = {
  /**
   * 获取系统通知/通知列表（推荐入口）
   * @param {Object} context Vuex context
   * @param {Object} params 查询参数
   * @returns {Promise<Object>} 通知数据
   */
  async fetchNotifications({ commit, state }, params = {}) {
    try {
      commit('SET_LOADING', true)
      const queryParams = {
        page: state.pagination.currentPage,
        size: state.pagination.pageSize,
        ...params
      }

      // 响应拦截器返回 {code, data}，从 res.data 提取数据
      const res = await getNotifications(queryParams)

      commit('SET_MESSAGES', res.data?.list || [])
      commit('SET_PAGINATION', {
        total: res.data?.total || 0,
        currentPage: state.pagination.currentPage,
        pageSize: state.pagination.pageSize
      })

      return res
    } finally {
      commit('SET_LOADING', false)
    }
  },

  /**
   * 获取通知详情
   * @param {Object} context Vuex context
   * @param {number} id 通知ID
   * @returns {Promise<Object>} 通知详情
   */
  async fetchNotificationDetail({ commit }, id) {
    try {
      commit('SET_LOADING', true)
      // 响应拦截器返回 {code, data}，从 res.data 提取数据
      const res = await getNotificationDetail(id)
      commit('SET_CURRENT_MESSAGE', res.data)
      return res
    } finally {
      commit('SET_LOADING', false)
    }
  },

  /**
   * 标记通知为已读
   * @param {Object} context Vuex context
   * @param {number} id 通知ID
   * @returns {Promise<boolean>} 是否成功
   */
  async markNotificationAsRead({ commit, dispatch }, id) {
    const res = await markAsRead(id)
    commit('MARK_MESSAGE_AS_READ', id)
    // 更新未读数量
    dispatch('fetchUnreadCount')
    return res
  },

  /**
   * 删除通知
   * @param {Object} context Vuex context
   * @param {number} id 通知ID
   * @returns {Promise<boolean>} 是否成功
   */
  async deleteNotification({ commit }, id) {
    const res = await deleteNotification(id)
    commit('DELETE_MESSAGE', id)
    return res
  },

  /**
   * 批量标记通知为已读
   * @param {Object} context Vuex context
   * @param {Array<number>} ids 通知ID数组
   * @returns {Promise<boolean>} 是否成功
   */
  async batchMarkAsRead({ commit, dispatch }, ids) {
    const res = await batchMarkAsRead(ids)
    commit('MARK_MESSAGES_AS_READ', ids)
    // 更新未读数量
    dispatch('fetchUnreadCount')
    return res
  },

  /**
   * 批量删除通知
   * @param {Object} context Vuex context
   * @param {Array<number>} ids 通知ID数组
   * @returns {Promise<boolean>} 是否成功
   */
  async batchDeleteNotifications({ commit }, ids) {
    const res = await batchDeleteNotifications(ids)
    commit('DELETE_MESSAGES', ids)
    return res
  },

  /**
   * 批量删除通知/消息（兼容旧方法）
   * @param {Object} context Vuex context
   * @param {Array<number|string>} ids 要删除的消息ID列表
   * @returns {Promise<boolean>} 是否成功
   */
  async deleteMessagesBulk({ commit, state }, ids) {
    const messageIds = Array.isArray(ids) ? ids : [ids]
    const res = await deleteMessages(messageIds)
    // 更新本地状态（以当前 state 为基准过滤）
    commit('SET_MESSAGES', (state.messages || []).filter(msg => !messageIds.includes(msg.id)))
    return res
  },

  // 获取消息列表
  async fetchMessages({ commit, state }, params = {}) {
    try {
      commit('SET_LOADING', true)
      
      const queryParams = {
        page: state.pagination.currentPage,
        size: state.pagination.pageSize,
        ...params
      }
      
      // 响应拦截器返回 {code, data}，从 res.data 提取数据
      const res = await getMessages(queryParams)
      
      commit('SET_MESSAGES', res.data?.list || [])
      commit('SET_PAGINATION', {
        total: res.data?.total || 0,
        currentPage: state.pagination.currentPage,
        pageSize: state.pagination.pageSize
      })
      
      return res
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // 获取消息详情
  async fetchMessageDetail({ commit }, id) {
    try {
      commit('SET_LOADING', true)
      
      // 响应拦截器返回 {code, data}，从 res.data 提取数据
      const res = await getMessageDetail(id)
      commit('SET_CURRENT_MESSAGE', res.data)
      
      // 标记为已读
      markAsRead(id)
      
      return res
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // 发送消息
  async sendMessage({ commit, state }, messageData) {
    // 响应拦截器返回 {code, data}，从 res.data 提取数据
    const res = await sendMessage(messageData)
    
    // 如果是发送给当前聊天用户，添加到聊天历史
    if (messageData.receiverId === state.currentChatUserId && res.data) {
      commit('ADD_CHAT_MESSAGE', res.data)
    }
    
    return res
  },
  
  // 标记消息已读
  async markAsRead({ dispatch }, ids) {
    const res = await markAsRead(ids)
    
    // 更新未读消息数量
    dispatch('fetchUnreadCount')
    
    return res
  },
  
  // 删除消息
  async deleteMessage({ commit }, id) {
    const res = await deleteMessages(id)
    
    // 更新本地状态
    commit('DELETE_MESSAGE', id)
    
    return res
  },
  
  // 获取未读消息数量
  async fetchUnreadCount({ commit }) {
    // 响应拦截器返回 {code, data}，从 res.data 提取数据
    const res = await getUnreadCount()
    const data = res.data
    // 如果返回的是数字直接使用，如果是对象取 count 字段
    const count = typeof data === 'number' ? data : (data?.count || 0)
    commit('SET_UNREAD_COUNT', count)
    
    return res
  },
  
  // 获取聊天会话列表
  async fetchChatSessions({ commit }) {
    try {
      commit('SET_LOADING', true)
      
      // 响应拦截器返回 {code, data}，从 res.data 提取数据
      const res = await getChatSessions()
      commit('SET_CHAT_SESSIONS', res.data || [])
      
      return res
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // 获取聊天历史
  async fetchChatHistory({ commit }, { userId, params = {} }) {
    try {
      commit('SET_LOADING', true)
      commit('SET_CURRENT_CHAT_USER', userId)
      
      // 响应拦截器返回 {code, data}，从 res.data 提取数据
      const res = await getChatHistory(userId, params)
      commit('SET_CHAT_HISTORY', res.data || [])
      
      return res
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // 创建聊天会话
  async createChatSession(_, { targetId, targetType }) {
    // 响应拦截器返回 {code, data}，从 res.data 提取数据
    const res = await createChatSession({ targetId, targetType })
    
    // 创建成功后，可以将新会话添加到会话列表
    // const newSession = res.data
    // if (newSession) {
    //   commit('ADD_CHAT_SESSION', newSession)
    // }
    
    return res
  },

  // 获取用户主页信息
  async fetchUserProfile({ commit }, userId) {
    try {
      commit('SET_LOADING', true)
      // 响应拦截器返回 {code, data}，从 res.data 提取数据
      const res = await getUserProfile(userId)
      commit('SET_USER_PROFILE', res.data)
      return res
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // 获取用户动态
  async fetchUserDynamic({ commit }, userId) {
    try {
      commit('SET_LOADING', true)
      // 响应拦截器返回 {code, data}，从 res.data 提取数据
      const res = await getUserDynamic(userId)
      commit('SET_USER_DYNAMIC', res.data)
      return res
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // 获取用户统计数据
  async fetchUserStatistics({ commit }, userId) {
    try {
      commit('SET_LOADING', true)
      // 响应拦截器返回 {code, data}，从 res.data 提取数据
      const res = await getUserStatistics(userId)
      commit('SET_USER_STATISTICS', res.data)
      return res
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // 置顶聊天会话
  async pinChatSession({ commit }, sessionId) {
    const res = await pinChatSession(sessionId)
    commit('PIN_CHAT_SESSION', sessionId)
    return res
  },

  // 删除聊天会话
  async deleteChatSession({ commit }, sessionId) {
    const res = await deleteChatSession(sessionId)
    commit('REMOVE_CHAT_SESSION', sessionId)
    return res
  },

  // 发送图片消息
  async sendImageMessage({ commit, state }, messageData) {
    // 响应拦截器返回 {code, data}，从 res.data 提取数据
    const res = await sendImageMessage(messageData)
    
    // 如果是发送给当前聊天用户，添加到聊天历史
    if (messageData.receiverId === state.currentChatUserId && res.data) {
      commit('ADD_CHAT_MESSAGE', res.data)
    }
    
    return res
  },

  // 获取用户发布的帖子列表
  async fetchUserPosts({ commit, state }, params = {}) {
    try {
      commit('SET_LOADING', true)
      const queryParams = {
        page: state.postsPagination.currentPage,
        size: state.postsPagination.pageSize,
        ...params
      }

      // 响应拦截器返回 {code, data}，从 res.data 提取数据
      const res = await getUserPosts(queryParams)
      
      commit('SET_USER_POSTS', {
        posts: res.data?.list || [],
        pagination: {
          total: res.data?.total || 0,
          currentPage: queryParams.page,
          pageSize: queryParams.size
        }
      })
      
      return res
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // 获取用户评价记录
  async fetchUserReviews({ commit, state }, params = {}) {
    try {
      commit('SET_LOADING', true)
      const queryParams = {
        page: state.reviewsPagination.currentPage,
        size: state.reviewsPagination.pageSize,
        ...params
      }

      // 响应拦截器返回 {code, data}，从 res.data 提取数据
      const res = await getUserReviews(queryParams)
      
      commit('SET_USER_REVIEWS', {
        reviews: res.data?.list || [],
        pagination: {
          total: res.data?.total || 0,
          currentPage: queryParams.page,
          pageSize: queryParams.size
        }
      })
      
      return res
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