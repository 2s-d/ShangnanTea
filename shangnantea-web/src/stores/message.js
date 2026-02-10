import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
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

export const useMessageStore = defineStore('message', () => {
  // State
  const messages = ref([])
  const currentMessage = ref(null)
  const unreadCount = ref(0)
  const chatSessions = ref([])
  const chatHistory = ref([])
  const currentChatUserId = ref(null)
  const pagination = ref({
    total: 0,
    currentPage: 1,
    pageSize: 10
  })
  const loading = ref(false)
  const userProfile = ref(null)
  const userDynamic = ref(null)
  const userStatistics = ref(null)
  const userPosts = ref([])
  const userReviews = ref([])
  const postsPagination = ref({
    total: 0,
    currentPage: 1,
    pageSize: 10
  })
  const reviewsPagination = ref({
    total: 0,
    currentPage: 1,
    pageSize: 10
  })

  // Getters
  const systemNotifications = computed(() => 
    messages.value.filter(msg => msg.type === 'system')
  )
  
  const privateMessages = computed(() => 
    messages.value.filter(msg => msg.type === 'private')
  )

  // Actions
  /**
   * 获取系统通知/通知列表（推荐入口）
   * @param {Object} params 查询参数
   * @returns {Promise<Object>} 通知数据
   */
  async function fetchNotifications(params = {}) {
    try {
      loading.value = true
      const queryParams = {
        page: pagination.value.currentPage,
        size: pagination.value.pageSize,
        ...params
      }

      const res = await getNotifications(queryParams)

      messages.value = res.data?.list || []
      pagination.value = {
        total: res.data?.total || 0,
        currentPage: pagination.value.currentPage,
        pageSize: pagination.value.pageSize
      }

      return res
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取通知详情
   * @param {number} id 通知ID
   * @returns {Promise<Object>} 通知详情
   */
  async function fetchNotificationDetail(id) {
    try {
      loading.value = true
      const res = await getNotificationDetail(id)
      currentMessage.value = res.data
      return res
    } finally {
      loading.value = false
    }
  }

  /**
   * 标记通知为已读
   * @param {number} id 通知ID
   * @returns {Promise<boolean>} 是否成功
   */
  async function markNotificationAsRead(id) {
    const res = await markAsRead(id)
    const message = messages.value.find(msg => msg.id === id)
    if (message) {
      message.isRead = 1
    }
    await fetchUnreadCount()
    return res
  }

  /**
   * 删除通知
   * @param {number} id 通知ID
   * @returns {Promise<boolean>} 是否成功
   */
  async function deleteNotificationAction(id) {
    const res = await deleteNotification(id)
    messages.value = messages.value.filter(msg => msg.id !== id)
    return res
  }

  /**
   * 批量标记通知为已读
   * @param {Array<number>} ids 通知ID数组
   * @returns {Promise<boolean>} 是否成功
   */
  async function batchMarkAsReadAction(ids) {
    const res = await batchMarkAsRead(ids)
    const idsArray = Array.isArray(ids) ? ids : [ids]
    messages.value.forEach(msg => {
      if (idsArray.includes(msg.id)) {
        msg.isRead = 1
      }
    })
    await fetchUnreadCount()
    return res
  }

  /**
   * 批量删除通知
   * @param {Array<number>} ids 通知ID数组
   * @returns {Promise<boolean>} 是否成功
   */
  async function batchDeleteNotificationsAction(ids) {
    const res = await batchDeleteNotifications(ids)
    const idsArray = Array.isArray(ids) ? ids : [ids]
    messages.value = messages.value.filter(msg => !idsArray.includes(msg.id))
    return res
  }

  /**
   * 批量删除通知/消息（兼容旧方法）
   * @param {Array<number|string>} ids 要删除的消息ID列表
   * @returns {Promise<boolean>} 是否成功
   */
  async function deleteMessagesBulk(ids) {
    const messageIds = Array.isArray(ids) ? ids : [ids]
    const res = await deleteMessages(messageIds)
    messages.value = messages.value.filter(msg => !messageIds.includes(msg.id))
    return res
  }

  /**
   * 获取消息列表
   * @param {Object} params 查询参数
   * @returns {Promise<Object>} 消息数据
   */
  async function fetchMessages(params = {}) {
    try {
      loading.value = true
      
      const queryParams = {
        page: pagination.value.currentPage,
        size: pagination.value.pageSize,
        ...params
      }
      
      const res = await getMessages(queryParams)
      
      messages.value = res.data?.list || []
      pagination.value = {
        total: res.data?.total || 0,
        currentPage: pagination.value.currentPage,
        pageSize: pagination.value.pageSize
      }
      
      return res
    } finally {
      loading.value = false
    }
  }
  
  /**
   * 获取消息详情
   * @param {number} id 消息ID
   * @returns {Promise<Object>} 消息详情
   */
  async function fetchMessageDetail(id) {
    try {
      loading.value = true
      
      const res = await getMessageDetail(id)
      currentMessage.value = res.data
      
      // 标记为已读
      markAsRead(id)
      
      return res
    } finally {
      loading.value = false
    }
  }
  
  /**
   * 发送消息
   * @param {Object} messageData 消息数据
   * @returns {Promise<Object>} 发送结果
   */
  async function sendMessageAction(messageData) {
    const res = await sendMessage(messageData)
    
    // 如果是发送给当前聊天用户，添加到聊天历史
    if (messageData.receiverId === currentChatUserId.value && res.data) {
      chatHistory.value.push(res.data)
    }
    
    return res
  }
  
  /**
   * 标记消息已读
   * @param {number|Array<number>} ids 消息ID或ID数组
   * @returns {Promise<boolean>} 是否成功
   */
  async function markAsReadAction(ids) {
    const res = await markAsRead(ids)
    
    // 更新未读消息数量
    await fetchUnreadCount()
    
    return res
  }
  
  /**
   * 删除消息
   * @param {number} id 消息ID
   * @returns {Promise<boolean>} 是否成功
   */
  async function deleteMessageAction(id) {
    const res = await deleteMessages(id)
    
    // 更新本地状态
    messages.value = messages.value.filter(msg => msg.id !== id)
    
    return res
  }
  
  /**
   * 获取未读消息数量
   * @returns {Promise<Object>} 未读数量数据
   */
  async function fetchUnreadCount() {
    const res = await getUnreadCount()
    const data = res.data
    // 后端返回 UnreadCountVO{total, system, chat}，提取 total 字段
    const count = data?.total || 0
    unreadCount.value = count
    
    return res
  }
  
  /**
   * 获取聊天会话列表
   * @returns {Promise<Object>} 会话列表数据
   */
  async function fetchChatSessions() {
    try {
      loading.value = true
      
      const res = await getChatSessions()
      chatSessions.value = res.data || []
      
      return res
    } finally {
      loading.value = false
    }
  }
  
  /**
   * 获取聊天历史
   * @param {Object} options 选项
   * @param {number} options.sessionId 会话ID
   * @param {Object} options.params 查询参数
   * @returns {Promise<Object>} 聊天历史数据
   */
  async function fetchChatHistory({ sessionId, params = {} }) {
    try {
      loading.value = true
      
      const res = await getChatHistory(sessionId, params)
      chatHistory.value = res.data?.list || []
      
      return res
    } finally {
      loading.value = false
    }
  }

  /**
   * 创建聊天会话
   * @param {Object} options 选项
   * @param {number} options.targetId 目标ID
   * @param {string} options.targetType 目标类型
   * @returns {Promise<Object>} 创建结果
   */
  async function createChatSessionAction({ targetId, targetType }) {
    const res = await createChatSession({ targetId, targetType })
    return res
  }

  /**
   * 获取用户主页信息
   * @param {number} userId 用户ID
   * @returns {Promise<Object>} 用户主页数据
   */
  async function fetchUserProfile(userId) {
    try {
      loading.value = true
      const res = await getUserProfile(userId)
      userProfile.value = res.data
      return res
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取用户动态
   * @param {number} userId 用户ID
   * @returns {Promise<Object>} 用户动态数据
   */
  async function fetchUserDynamic(userId) {
    try {
      loading.value = true
      const res = await getUserDynamic(userId)
      userDynamic.value = res.data
      return res
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取用户统计数据
   * @param {number} userId 用户ID
   * @returns {Promise<Object>} 用户统计数据
   */
  async function fetchUserStatistics(userId) {
    try {
      loading.value = true
      const res = await getUserStatistics(userId)
      userStatistics.value = res.data
      return res
    } finally {
      loading.value = false
    }
  }

  /**
   * 置顶聊天会话
   * @param {number} sessionId 会话ID
   * @returns {Promise<boolean>} 是否成功
   */
  async function pinChatSessionAction(sessionId) {
    const res = await pinChatSession(sessionId)
    const session = chatSessions.value.find(s => s.id === sessionId)
    if (session) {
      session.isPinned = true
      // 将置顶会话移到列表前面
      const index = chatSessions.value.indexOf(session)
      chatSessions.value.splice(index, 1)
      chatSessions.value.unshift(session)
    }
    return res
  }

  /**
   * 删除聊天会话
   * @param {number} sessionId 会话ID
   * @returns {Promise<boolean>} 是否成功
   */
  async function deleteChatSessionAction(sessionId) {
    const res = await deleteChatSession(sessionId)
    chatSessions.value = chatSessions.value.filter(s => s.id !== sessionId)
    return res
  }

  /**
   * 发送图片消息
   * @param {Object} messageData 消息数据
   * @returns {Promise<Object>} 发送结果
   */
  async function sendImageMessageAction(messageData) {
    const res = await sendImageMessage(messageData)
    
    // 如果是发送给当前聊天用户，添加到聊天历史
    if (messageData.receiverId === currentChatUserId.value && res.data) {
      chatHistory.value.push(res.data)
    }
    
    return res
  }

  /**
   * 获取用户发布的帖子列表
   * @param {Object} params 查询参数
   * @returns {Promise<Object>} 帖子列表数据
   */
  async function fetchUserPosts(params = {}) {
    try {
      loading.value = true
      const queryParams = {
        page: postsPagination.value.currentPage,
        size: postsPagination.value.pageSize,
        ...params
      }

      const res = await getUserPosts(queryParams)
      
      userPosts.value = res.data?.list || []
      postsPagination.value = {
        total: res.data?.total || 0,
        currentPage: queryParams.page,
        pageSize: queryParams.size
      }
      
      return res
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取用户评价记录
   * @param {Object} params 查询参数
   * @returns {Promise<Object>} 评价列表数据
   */
  async function fetchUserReviews(params = {}) {
    try {
      loading.value = true
      const queryParams = {
        page: reviewsPagination.value.currentPage,
        size: reviewsPagination.value.pageSize,
        ...params
      }

      const res = await getUserReviews(queryParams)
      
      userReviews.value = res.data?.list || []
      reviewsPagination.value = {
        total: res.data?.total || 0,
        currentPage: queryParams.page,
        pageSize: queryParams.size
      }
      
      return res
    } finally {
      loading.value = false
    }
  }

  return {
    // State
    messages,
    currentMessage,
    unreadCount,
    chatSessions,
    chatHistory,
    currentChatUserId,
    pagination,
    loading,
    userProfile,
    userDynamic,
    userStatistics,
    userPosts,
    userReviews,
    postsPagination,
    reviewsPagination,
    
    // Getters
    systemNotifications,
    privateMessages,
    
    // Actions
    fetchNotifications,
    fetchNotificationDetail,
    markNotificationAsRead,
    deleteNotification: deleteNotificationAction,
    batchMarkAsRead: batchMarkAsReadAction,
    batchDeleteNotifications: batchDeleteNotificationsAction,
    deleteMessagesBulk,
    fetchMessages,
    fetchMessageDetail,
    sendMessage: sendMessageAction,
    markAsRead: markAsReadAction,
    deleteMessage: deleteMessageAction,
    fetchUnreadCount,
    fetchChatSessions,
    fetchChatHistory,
    createChatSession: createChatSessionAction,
    fetchUserProfile,
    fetchUserDynamic,
    fetchUserStatistics,
    pinChatSession: pinChatSessionAction,
    deleteChatSession: deleteChatSessionAction,
    sendImageMessage: sendImageMessageAction,
    fetchUserPosts,
    fetchUserReviews
  }
})
