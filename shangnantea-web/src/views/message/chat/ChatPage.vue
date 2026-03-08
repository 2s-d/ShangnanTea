<template>
  <div class="chat-page">
    <!-- 
    聊天功能设计：
    1. 会话列表：
       - 聊天对象列表（用户、商家、客服）
       - 最新消息预览和时间
       - 未读消息计数
       - 会话置顶功能
       - 搜索会话功能
    
    2. 聊天界面：
       - 消息气泡展示（自己/对方）
       - 消息时间戳
       - 图片消息支持
       - 消息发送状态（发送中/已发送/已读）
       - 历史消息加载
    
    3. 输入区域：
       - 文本输入框
       - 表情选择器
       - 图片上传按钮
       - 发送按钮
    
    4. 管理功能：
       - 删除会话
    -->
    
    <div class="chat-layout">
      <!-- 会话列表区域 -->
      <div class="chat-sessions">
        <div class="search-bar">
          <el-input 
            v-model="searchQuery" 
            placeholder="搜索联系人或消息" 
            clearable
            prefix-icon="Search">
          </el-input>
        </div>
        
        <div class="session-list">
          <div 
            v-for="session in filteredSessions" 
            :key="session.sessionId"
            class="session-item"
            :class="{ 'session-active': currentSessionId === session.sessionId }"
            @click="selectSession(session)">
            
            <div class="session-avatar">
              <el-badge 
                :value="session.unreadCount" 
                :hidden="!session.unreadCount"
                type="danger">
                <SafeImage :src="session.avatar || ''" type="avatar" :alt="session.name" style="width:40px;height:40px;border-radius:50%;object-fit:cover;" />
              </el-badge>
            </div>
            
            <div class="session-info">
              <div class="session-name">
                {{ session.name }}
                <el-icon v-if="session.isPinned" class="pin-icon" title="已置顶">
                  <Top />
                </el-icon>
              </div>
              <div class="session-preview">{{ session.lastMessage }}</div>
            </div>
            
            <div class="session-meta">
              <div class="session-time">{{ formatTime(session.lastTime) }}</div>
              <div class="session-actions">
                <el-popover
                  placement="top"
                  width="auto"
                  trigger="click"
                  @show="stopPropagation">
                  <template #reference>
                    <el-button 
                      circle
                      size="small"
                      class="more-action"
                      @click.stop>
                      <el-icon><MoreFilled /></el-icon>
                    </el-button>
                  </template>
                  <div class="action-buttons">
                    <el-button 
                      size="small" 
                      @click="togglePinSession(session.sessionId)">
                      {{ session.isPinned ? '取消置顶' : '置顶会话' }}
                    </el-button>
                    <el-button 
                      size="small" 
                      type="danger" 
                      @click="deleteSession(session.sessionId)">
                      删除会话
                    </el-button>
                  </div>
                </el-popover>
              </div>
            </div>
          </div>
          
          <!-- 无会话时显示 -->
          <el-empty 
            v-if="filteredSessions.length === 0" 
            description="暂无聊天会话"
            :image-size="100">
          </el-empty>
        </div>
      </div>
      
      <!-- 聊天内容区域 -->
      <div class="chat-content">
        <!-- 聊天头部 -->
        <div class="chat-header" v-if="currentSession">
          <h3 class="chat-title">{{ currentSession.name }}</h3>
        </div>
        
        <div class="chat-header" v-else>
          <h3 class="chat-title">
            {{ hasSessions ? '请选择一个联系人开始聊天' : '暂无聊天会话' }}
          </h3>
          <p v-if="!hasSessions" class="chat-subtitle">
            可以从茶叶详情页、店铺详情页或订单列表中发起私信咨询。
          </p>
        </div>
        
        <!-- 消息列表 -->
        <div 
          class="chat-messages" 
          ref="messagesContainer"
          v-if="currentSession">
          
          <!-- 加载更多按钮 -->
          <div class="load-more" v-if="hasMoreMessages">
            <el-button 
              type="text" 
              size="small" 
              @click="loadMoreMessages"
              :loading="loadingMessages">
              加载更多消息
            </el-button>
          </div>
          
          <!-- 消息列表 -->
          <div class="messages-list">
            <div 
              v-for="message in currentMessages" 
              :key="message.id"
              class="message-item"
              :class="{
                'message-self': message.isSelf,
                'message-other': !message.isSelf
              }">
              
              <!-- 时间分割线 -->
              <div class="message-time-divider" v-if="message.showTimeDivider">
                <span>{{ formatMessageTime(message.createTime) }}</span>
              </div>
              
              <!-- 消息内容 -->
              <div class="message-container">
                <!-- 头像（对方） -->
                <div class="message-avatar" v-if="!message.isSelf">
                  <SafeImage :src="currentSession?.avatar || ''" type="avatar" :alt="currentSession?.name" style="width:40px;height:40px;border-radius:50%;object-fit:cover;" />
                </div>
                
                <!-- 消息气泡 -->
                <div class="message-content">
                  <div class="message-bubble">
                    <!-- 文本消息 -->
                    <div class="message-text" v-if="message.type === 'text'">
                      {{ message.content }}
                    </div>
                    
                    <!-- 图片消息 -->
                    <div class="message-image" v-else-if="message.type === 'image'">
                      <SafeImage :src="message.content" type="post" :alt="`图片消息`" style="max-width:200px;max-height:200px;border-radius:4px;" />
                    </div>
                  </div>
                  
                  <!-- 消息状态 -->
                  <div class="message-status" v-if="message.isSelf">
                    <el-icon v-if="message.status === 'sending'"><Loading /></el-icon>
                    <el-icon v-else-if="message.status === 'sent'"><Check /></el-icon>
                    <el-icon v-else-if="message.status === 'read'"><CircleCheck /></el-icon>
                    <el-icon v-else-if="message.status === 'failed'" class="status-failed"><Warning /></el-icon>
                  </div>
                </div>
                
                <!-- 头像（自己） -->
                <div class="message-avatar" v-if="message.isSelf">
                  <SafeImage src="/images/avatars/default.jpg" type="avatar" alt="我" style="width:40px;height:40px;border-radius:50%;object-fit:cover;" />
                </div>
              </div>
            </div>
          </div>
          
          <!-- 无消息时显示 -->
          <div class="empty-messages" v-if="currentMessages.length === 0">
            <el-empty description="暂无消息记录" :image-size="100"></el-empty>
          </div>
        </div>
        
        <!-- 无选中会话时显示 -->
        <div class="select-tip" v-else>
          <template v-if="hasSessions">
          <SafeImage src="/images/chat/start.jpg" type="banner" alt="开始聊天" class="tip-image" />
            <p>在左侧选择一个联系人开始聊天</p>
          </template>
          <template v-else>
            <el-empty :image-size="160">
              <template #description>
                <p>还没有任何聊天会话</p>
                <p class="select-tip-desc">可以先去茶叶商城或店铺页面，向商家发起咨询。</p>
              </template>
              <el-button type="primary" @click="goToTeaMall">去茶叶商城逛逛</el-button>
            </el-empty>
          </template>
        </div>
        
        <!-- 输入区域 -->
        <div class="chat-input" v-if="currentSession">
          <!-- 工具栏 -->
          <div class="input-toolbar">
            <el-tooltip content="发送图片" placement="top">
              <el-button 
                type="text" 
                @click="triggerImageUpload"
                class="toolbar-button">
                <el-icon><Picture /></el-icon>
              </el-button>
            </el-tooltip>
            
            <el-tooltip content="表情" placement="top">
              <el-button 
                type="text" 
                @click="toggleEmojiPicker"
                class="toolbar-button">
                <el-icon><Star /></el-icon>
                表情
              </el-button>
            </el-tooltip>
            
            <!-- 隐藏的文件上传组件 -->
            <input 
              type="file" 
              ref="imageInput"
              accept="image/*"
              style="display: none"
              @change="handleImageUpload" />
          </div>
          
          <!-- 表情选择器 -->
          <div class="emoji-picker" v-if="showEmojiPicker">
            <div class="emoji-container">
              <span 
                v-for="emoji in emojiList" 
                :key="emoji"
                class="emoji-item"
                @click="insertEmoji(emoji)">
                {{ emoji }}
              </span>
            </div>
          </div>
          
          <!-- 文本输入区 -->
          <el-input 
            type="textarea" 
            :rows="3" 
            placeholder="请输入消息..." 
            v-model="messageInput"
            resize="none"
            @keydown.enter.exact.prevent="sendMessage">
          </el-input>
          
          <!-- 发送按钮 -->
          <div class="input-actions">
            <el-button 
              type="primary" 
              @click="sendMessage"
              :disabled="!messageInput.trim() && !imageFile">
              发送
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, nextTick, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useMessageStore } from '@/stores/message'
import { useUserStore } from '@/stores/user'
import { message } from '@/components/common'
import { showByCode, isSuccess } from '@/utils/apiMessages'
import { 
  Check, CircleCheck, Warning, Picture, Star, Loading,
  MoreFilled, Top
} from '@element-plus/icons-vue'
import SafeImage from '@/components/common/form/SafeImage.vue'
import { timeFormat } from '@/utils/timeFormat'

const route = useRoute()
const router = useRouter()
const messageStore = useMessageStore()
const userStore = useUserStore()
    
    // DOM引用
    const messagesContainer = ref(null)
    const imageInput = ref(null)
    
    // 搜索关键词
    const searchQuery = ref('')
    
    // 会话管理
    const mockSessions = ref([])
    const currentSessionId = ref(null)
    const currentTargetUserId = ref(null)
    const messagesMap = reactive({})
    const messageInput = ref('')
    const imageFile = ref(null)
    const loadingMessages = ref(false)
    const hasMoreMessages = ref(false)
    const showEmojiPicker = ref(false)
    const defaultAvatar = '/images/avatars/default.jpg'
    const currentUserAvatar = '/images/avatars/default.jpg'
    
    // 表情列表
    const emojiList = [
      '😀', '😃', '😄', '😁', '😆', '😅', '😂', '🤣', '😊', '😇', 
      '🙂', '🙃', '😉', '😌', '😍', '🥰', '😘', '😗', '😙', '😚', 
      '😋', '😛', '😝', '😜', '🤪', '🤨', '🧐', '🤓', '😎', '🤩',
      '🥳', '😏', '😒', '😞', '😔', '😟', '😕', '🙁', '☹️', '😣',
      '😖', '😫', '😩', '🥺', '😢', '😭', '😤', '😠', '😡', '🤬',
      '🤯', '😳', '🥵', '🥶', '😱', '😨', '😰', '😥', '😓', '🤗',
      '👋', '👍', '👎', '❤️', '💋', '👏', '🙏', '🤝', '💪', '✌️'
    ]
    
    // 获取所有会话列表（使用后端真实数据）
    const fetchSessions = async () => {
      try {
        const response = await messageStore.fetchChatSessions()
        
        // 失败时提示并清空本地会话
        if (!isSuccess(response.code)) {
          showByCode(response.code)
          mockSessions.value = []
          currentSessionId.value = null
          return
        }

        // 从 Pinia 获取会话列表数据
        const sessions = messageStore.chatSessions || []
        const currentUserId = userStore.userInfo?.id
        
        // 转换数据格式以匹配 UI 组件的期望格式：
        // - receiverId 始终表示“对端用户”的 ID
        // - unreadCount 根据当前用户是发起者/接收者选择对应未读字段
        mockSessions.value = sessions.map(session => {
          const isInitiator = currentUserId && session.initiatorId === currentUserId
          const targetId = isInitiator ? session.receiverId : session.initiatorId
          const unreadCount = isInitiator
            ? (session.initiatorUnread ?? 0)
            : (session.receiverUnread ?? 0)

          const isCustomerService = session.sessionType === 'customer'

          return {
          sessionId: session.id,
            receiverId: targetId, // 对端用户ID
            targetType: isCustomerService ? 'shop' : 'user',
            name: isCustomerService ? (session.shopName || `店铺${targetId}客服`) : (session.nickname || `用户${targetId}`),
            avatar: session.targetAvatar || `https://via.placeholder.com/50x50?text=${isCustomerService ? '店铺' : '用户'}`,
          lastMessage: session.lastMessage || '',
          lastTime: session.lastMessageTime,
            unreadCount,
            isPinned: session.isPinned || false,
            raw: session
          }
        })

        // 如果当前没有选中的会话，默认选中：优先未读，否则第一个
        if (!currentSessionId.value && mockSessions.value.length > 0) {
        const unreadSession = mockSessions.value.find(session => session.unreadCount > 0)
          selectSession(unreadSession || mockSessions.value[0])
        }
      } catch (error) {
        // 捕获意外的运行时错误（非 API 业务错误）
        if (process.env.NODE_ENV === 'development') {
          console.error('[开发调试] 获取会话列表时发生意外错误：', error)
        }
      }
    }
    
    // 获取指定会话的消息列表
    const fetchMessages = async (sessionId, isLoadMore = false) => {
      if (!sessionId) return
      
      try {
        loadingMessages.value = true

        // 通过Pinia调用后端API获取聊天记录
        const response = await messageStore.fetchChatHistory({
          sessionId: sessionId,
          params: {
            page: 1,
            pageSize: 50
          }
        })
        
        // 失败时仅提示错误，不打断页面其他逻辑
        if (!isSuccess(response.code)) {
        showByCode(response.code)
          return
        }
        
        // 只有成功时才更新消息列表
          const history = response.data?.list || []
          messagesMap[sessionId] = history.map(msg => ({
            id: msg.id,
            sessionId: sessionId,
            content: msg.content,
            type: msg.contentType || 'text',
            createTime: msg.createTime,
            status: msg.isRead ? 'read' : 'sent',
            isSelf: msg.senderId === userStore.userInfo?.id,
            showTimeDivider: false
          }))

        return
      } catch (error) {
        // 捕获意外的运行时错误（非API业务错误）
        if (process.env.NODE_ENV === 'development') {
          console.error('[开发调试] 获取消息列表时发生意外错误：', error)
        }
      } finally {
        loadingMessages.value = false
      }
    }
    
    // 标记会话为已读
    const markSessionAsRead = sessionId => {
      const session = mockSessions.value.find(s => s.sessionId === sessionId)
      if (session && session.unreadCount > 0) {
        session.unreadCount = 0
        // 在实际项目中，这里应该调用后端API标记会话为已读
      }
    }
    
    // 加载更多消息
    const loadMoreMessages = () => {
      if (!currentSessionId.value || loadingMessages.value) return
      fetchMessages(currentSessionId.value, true)
    }
    
    // 选择会话
    const selectSession = session => {
      if (!session) return
      
      const sessionId = session.sessionId
      if (currentSessionId.value === sessionId) return
      
      currentSessionId.value = sessionId
      // 记录会话对端用户ID（用于拉取历史与发送消息）
      currentTargetUserId.value = session.receiverId
      
      // 如果该会话的消息尚未加载，则加载消息
      if (!messagesMap[sessionId]) {
        fetchMessages(sessionId)
      } else {
        // 如果已加载，则标记为已读并滚动到底部
        markSessionAsRead(sessionId)
        nextTick(() => {
          scrollToBottom()
        })
      }
    }
    
    // 删除会话
    const deleteSession = async sessionId => {
      try {
        await ElMessageBox.confirm('确定要删除此会话吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        // 调用Pinia action删除会话
        const response = await messageStore.deleteChatSession(sessionId)
        
        // 显示API响应消息（成功或失败都通过状态码映射显示）
        showByCode(response.code)
        
        // 成功时刷新会话列表
        if (isSuccess(response.code)) {
          await fetchSessions()
          
          // 如果删除的是当前会话，清空当前会话
          if (currentSessionId.value === sessionId) {
            currentSessionId.value = null
            currentTargetUserId.value = null
          }
        }
      } catch (error) {
        // 用户取消删除或操作失败
        if (error !== 'cancel' && process.env.NODE_ENV === 'development') {
          console.error('[开发调试] 删除会话时发生意外错误：', error)
        }
      }
    }
    
    // 置顶/取消置顶会话
    const togglePinSession = async sessionId => {
      try {
        const response = await messageStore.pinChatSession(sessionId)
        
        // 显示API响应消息（成功或失败都通过状态码映射显示）
        showByCode(response.code)
        
        // 成功时刷新会话列表
        if (isSuccess(response.code)) {
          await fetchSessions()
        }
      } catch (error) {
        // 捕获意外的运行时错误（非API业务错误）
        if (process.env.NODE_ENV === 'development') {
          console.error('[开发调试] 置顶会话时发生意外错误：', error)
        }
      }
    }
    
    // 发送消息
    const sendMessage = async () => {
      if (!currentSessionId.value) return
      if (!messageInput.value.trim() && !imageFile.value) return
      
      try {
        const now = Date.now()
        const messageId = `msg-${now}`
        
        let messageType = 'text'
        let messageContent = messageInput.value.trim()
        
        // 如果有图片文件,则调用图片上传API
        if (imageFile.value) {
          messageType = 'image'
          
          // 调用图片上传API
          const uploadResponse = await messageStore.sendImageMessage({
            sessionId: currentSessionId.value,
            receiverId: currentTargetUserId.value,
            image: imageFile.value
          })
          
          // 显示API响应消息
          showByCode(uploadResponse.code)
          
          // 清空图片文件
          imageFile.value = null
          
          // 成功时刷新消息列表
          if (isSuccess(uploadResponse.code)) {
            await fetchMessages(currentSessionId.value, false)
            await nextTick()
            scrollToBottom()
          }
          
          return
        }
        
        // 发送文本消息
        if (!currentTargetUserId.value) {
          message.warning('缺少会话目标用户ID，暂无法发送消息')
          return
        }

        const sendResponse = await messageStore.sendMessage({
          receiverId: currentTargetUserId.value,
          content: messageContent,
          type: messageType
        })
        
        // 清空输入框
        messageInput.value = ''
        
        // 显示API响应消息（成功或失败都通过状态码映射显示）
        showByCode(sendResponse.code)
        
        // 只有成功时才刷新消息列表
        if (isSuccess(sendResponse.code)) {
          await fetchMessages(currentSessionId.value, false)
          await nextTick()
          scrollToBottom()
        }
      } catch (error) {
        // 捕获意外的运行时错误（非API业务错误）
        // API业务失败已通过 showByCode 显示，网络错误已在响应拦截器显示
        if (process.env.NODE_ENV === 'development') {
          console.error('[开发调试] 发送消息时发生意外错误：', error)
        }
      }
    }
    
    // 触发图片上传
    const triggerImageUpload = () => {
      if (!currentSessionId.value) return
      if (imageInput.value) {
        imageInput.value.click()
      }
    }
    
    // 处理图片上传
    const handleImageUpload = event => {
      const file = event.target.files[0]
      if (!file) return
      
      // 检查文件类型
      if (!file.type.startsWith('image/')) {
        message.error('只能上传图片文件')
        return
      }
      
      // 检查文件大小，限制为5MB
      if (file.size > 5 * 1024 * 1024) {
        message.error('图片大小不能超过5MB')
        return
      }
      
      imageFile.value = file
      sendMessage()
      
      // 清空input，以便于下次选择同一文件时也能触发change事件
      event.target.value = ''
    }
    
    // 滚动到底部
    const scrollToBottom = () => {
      if (messagesContainer.value) {
        messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
      }
    }
    
    // 表情相关
    const toggleEmojiPicker = () => {
      showEmojiPicker.value = !showEmojiPicker.value
    }
    
    const insertEmoji = emoji => {
      messageInput.value += emoji
    }
    
    // 阻止事件冒泡
    const stopPropagation = event => {
      event.stopPropagation()
    }
    
    // 格式化时间
    const formatTime = timestamp => {
      if (!timestamp) return ''
      
      const date = new Date(timestamp)
      const now = new Date()
      const diff = now - date
      
      // 今天内的消息显示时:分
      if (diff < 86400000 && date.getDate() === now.getDate()) {
        return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
      }
      
      // 昨天的消息
      if (diff < 2 * 86400000 && date.getDate() === now.getDate() - 1) {
        return '昨天'
      }
      
      // 一周内的消息显示周几
      if (diff < 7 * 86400000) {
        const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
        return weekdays[date.getDay()]
      }
      
      // 更早的消息显示年-月-日
      return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
    }
    
    // 格式化消息时间
    const formatMessageTime = timestamp => {
      if (!timestamp) return ''
      
      const date = new Date(timestamp)
      return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
    }
    
    // 过滤会话列表
    const filteredSessions = computed(() => {
      if (!searchQuery.value.trim()) {
        return mockSessions.value
      }
      
      const query = searchQuery.value.toLowerCase()
      return mockSessions.value.filter(session => {
        return session.name.toLowerCase().includes(query) ||
               (session.lastMessage && session.lastMessage.toLowerCase().includes(query))
      })
    })
    
    // 是否存在任何会话
    const hasSessions = computed(() => mockSessions.value.length > 0)
    
    // 当前会话
    const currentSession = computed(() => {
      return mockSessions.value.find(s => s.sessionId === currentSessionId.value) || null
    })
    
    // 当前会话的消息列表
    const currentMessages = computed(() => {
      return messagesMap[currentSessionId.value] || []
    })
    
    // 初始化函数：根据路由参数自动打开特定会话（店铺 / 用户）
    const initializeChatFromRouteParams = async () => {
      const shopId = route.query.shopId
      const userId = route.query.userId
      
      // 如果没有指定目标，仅在有会话但未选中时做一次默认选择
      if (!shopId && !userId) {
        if (!currentSessionId.value && mockSessions.value.length > 0) {
          const unreadSession = mockSessions.value.find(session => session.unreadCount > 0)
          selectSession(unreadSession || mockSessions.value[0])
        }
        return
      }

      const targetType = shopId ? 'shop' : 'user'
      const targetId = shopId || userId

      // 1. 先在现有会话中查找
      const existing = mockSessions.value.find(session =>
        session.targetType === targetType &&
        session.receiverId?.toString() === targetId.toString()
        )
        
      if (existing) {
        selectSession(existing)
        return
    }
    
      // 2. 若不存在，则通过后端创建/获取会话
      try {
        const res = await messageStore.createChatSession({
          targetId,
          targetType
        })

        if (!isSuccess(res.code)) {
          showByCode(res.code)
          return
      }
      
        // 3. 重新拉取会话列表并选中目标会话
        await fetchSessions()
        const created = mockSessions.value.find(session =>
          session.targetType === targetType &&
          session.receiverId?.toString() === targetId.toString()
        )

        if (created) {
          selectSession(created)
        }
      } catch (error) {
        if (process.env.NODE_ENV === 'development') {
          console.error('[开发调试] 根据路由参数初始化聊天会话失败：', error)
        }
      }
    }
    
    // 在组件挂载时初始化：先加载会话，再根据路由参数选择目标
    onMounted(async () => {
      await fetchSessions()
      await initializeChatFromRouteParams()
    })
    
    // 监听路由参数变化
    watch(() => route.query.shopId, newShopId => {
      if (newShopId) {
        initializeChatFromRouteParams()
      }
    })
    
    // 监听 userId 路由参数变化
watch(() => route.query.userId, newUserId => {
  if (newUserId) {
    initializeChatFromRouteParams()
  }
})

    // 引导按钮：跳转到茶叶商城
    const goToTeaMall = () => {
      router.push('/tea/mall')
    }
</script>

<style lang="scss" scoped>
.chat-page {
  position: relative;
  
  .chat-layout {
    display: flex;
    height: 650px;
    background-color: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    overflow: hidden;
    
    // 会话列表区域
    .chat-sessions {
      width: 300px;
      border-right: 1px solid #eee;
      display: flex;
      flex-direction: column;
      background-color: #f7f7f7;
      
      .search-bar {
        padding: 15px;
        border-bottom: 1px solid #eee;
        background-color: #fff;
      }
      
      .session-list {
        flex: 1;
        overflow-y: auto;
        
        .session-item {
          display: flex;
          padding: 12px 15px;
          cursor: pointer;
          transition: background-color 0.2s;
          border-bottom: 1px solid #f0f0f0;
          background-color: #fff;
          
          &:hover {
            background-color: #f9f9f9;
          }
          
          &.session-active {
            background-color: #f0f7ff;
          }
          
          .session-avatar {
            margin-right: 12px;
            
            img {
              width: 48px;
              height: 48px;
              border-radius: 50%;
              object-fit: cover;
            }
          }
          
          .session-info {
            flex: 1;
            display: flex;
            flex-direction: column;
            justify-content: center;
            overflow: hidden;
            
            .session-name {
              font-size: 15px;
              font-weight: 500;
              color: var(--text-primary);
              margin-bottom: 4px;
              display: flex;
              align-items: center;
              gap: 6px;
              
              .pin-icon {
                color: #f59e0b;
                font-size: 14px;
              }
            }
            
            .session-preview {
              font-size: 13px;
              color: var(--text-secondary);
              white-space: nowrap;
              overflow: hidden;
              text-overflow: ellipsis;
              max-width: 150px;
            }
          }
          
          .session-meta {
            display: flex;
            flex-direction: column;
            align-items: flex-end;
            justify-content: space-between;
            
            .session-time {
              font-size: 12px;
              color: var(--text-placeholder);
              margin-bottom: 4px;
            }
            
            .session-actions {
              visibility: hidden;
              
              .more-action {
                padding: 0;
                background: transparent;
                border: none;
                font-size: 16px;
                color: var(--text-secondary);
              }
            }
            
            .action-buttons {
              display: flex;
              flex-direction: column;
              
              button {
                margin: 3px 0;
              }
            }
          }
          
          &:hover .session-actions {
            visibility: visible;
          }
        }
      }
    }
    
    // 聊天内容区域
    .chat-content {
      flex: 1;
      display: flex;
      flex-direction: column;
      background-color: #f7f7f7;
      
      .chat-header {
        padding: 16px 20px;
        border-bottom: 1px solid #eee;
        background-color: #fff;
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: flex-start;
        
        .chat-title {
          margin: 0;
          font-size: 16px;
          font-weight: 500;
          color: var(--text-primary);
        }

        .chat-subtitle {
          margin-top: 4px;
          font-size: 13px;
          color: var(--text-secondary);
        }
      }
      
      .chat-messages {
        flex: 1;
        padding: 15px;
        overflow-y: auto;
        
        .load-more {
          text-align: center;
          padding: 10px 0;
          
          .el-button {
            font-size: 13px;
          }
        }
        
        .messages-list {
          display: flex;
          flex-direction: column;
          
          .message-item {
            margin-bottom: 15px;
            
            .message-time-divider {
              text-align: center;
              margin: 10px 0;
              
              span {
                display: inline-block;
                padding: 2px 10px;
                font-size: 12px;
                color: var(--text-secondary);
                background-color: rgba(0, 0, 0, 0.05);
                border-radius: 10px;
              }
            }
            
            .message-container {
              display: flex;
              
              &.message-self {
                justify-content: flex-end;
              }
              
              .message-avatar {
                width: 36px;
                height: 36px;
                margin: 0 8px;
                
                img {
                  width: 36px;
                  height: 36px;
                  border-radius: 50%;
                  object-fit: cover;
                }
              }
              
              .message-content {
                display: flex;
                flex-direction: column;
                max-width: 70%;
                
                .message-bubble {
                  padding: 8px 12px;
                  border-radius: 8px;
                  position: relative;
                  
                  .message-text {
                    font-size: 14px;
                    line-height: 1.5;
                    word-break: break-word;
                  }
                  
                  .message-image {
                    width: 100%;
                    
                    :deep(.el-image) {
                      max-width: 240px;
                      max-height: 240px;
                      border-radius: 8px;
                      overflow: hidden;
                      
                      img {
                        width: 100%;
                        height: 100%;
                        object-fit: cover;
                      }
                    }
                    
                    .image-error {
                      display: flex;
                      flex-direction: column;
                      align-items: center;
                      justify-content: center;
                      padding: 20px;
                      background-color: #f5f5f5;
                      border-radius: 8px;
                      
                      .el-icon {
                        font-size: 24px;
                        color: #f56c6c;
                        margin-bottom: 5px;
                      }
                      
                      span {
                        font-size: 12px;
                        color: var(--text-secondary);
                      }
                    }
                  }
                }
                
                .message-status {
                  display: flex;
                  justify-content: flex-end;
                  margin-top: 2px;
                  font-size: 12px;
                  color: var(--text-secondary);
                  
                  .el-icon {
                    font-size: 12px;
                  }
                  
                  .status-failed {
                    color: #f56c6c;
                  }
                }
              }
            }
            
            &.message-self {
              .message-content {
                align-items: flex-end;
                
                .message-bubble {
                  background-color: #95ec69;
                  color: #000;
                  
                  &::after {
                    content: '';
                    position: absolute;
                    right: -6px;
                    top: 6px;
                    border-width: 6px 0 6px 6px;
                    border-style: solid;
                    border-color: transparent transparent transparent #95ec69;
                  }
                }
              }
            }
            
            &.message-other {
              .message-content {
                align-items: flex-start;
                
                .message-bubble {
                  background-color: #ffffff;
                  color: #333;
                  
                  &::before {
                    content: '';
                    position: absolute;
                    left: -6px;
                    top: 6px;
                    border-width: 6px 6px 6px 0;
                    border-style: solid;
                    border-color: transparent #ffffff transparent transparent;
                  }
                }
              }
            }
          }
        }
        
        .empty-messages {
          height: 100%;
          display: flex;
          align-items: center;
          justify-content: center;
        }
      }
      
      .select-tip {
        flex: 1;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        text-align: center;
        color: var(--text-secondary);
        
        .tip-image {
          width: 120px;
          margin-bottom: 20px;
          opacity: 0.6;
        }
        
        p {
          font-size: 16px;
        }

        .select-tip-desc {
          margin-top: 4px;
          font-size: 13px;
          color: #999;
        }
      }
      
      .chat-input {
        padding: 15px;
        border-top: 1px solid #eee;
        background-color: #fff;
        position: relative;
        
        .input-toolbar {
          display: flex;
          margin-bottom: 10px;
          
          .el-button {
            padding: 4px 8px;
            
            .el-icon {
              font-size: 18px;
              color: var(--text-secondary);
            }
          }
          
          .toolbar-button {
            display: flex;
            align-items: center;
            background-color: #f5f7fa;
            border-radius: 4px;
            padding: 6px 10px;
            margin-right: 10px;
            transition: all 0.3s;
            
            &:hover {
              background-color: #e4e7ed;
            }
            
            .el-icon {
              margin-right: 4px;
              font-size: 16px;
              color: #606266;
            }
          }
        }
        
        .emoji-picker {
          position: absolute;
          bottom: 100%;
          left: 15px;
          background-color: #fff;
          border: 1px solid #eee;
          border-radius: 8px;
          box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
          padding: 10px;
          z-index: 100;
          margin-bottom: 8px;
          
          &::after {
            content: '';
            position: absolute;
            bottom: -8px;
            left: 14px;
            border-width: 8px 8px 0;
            border-style: solid;
            border-color: #fff transparent transparent;
          }
          
          .emoji-container {
            display: flex;
            flex-wrap: wrap;
            width: 300px;
            max-height: 180px;
            overflow-y: auto;
            
            .emoji-item {
              width: 35px;
              height: 35px;
              display: flex;
              align-items: center;
              justify-content: center;
              font-size: 20px;
              cursor: pointer;
              transition: background-color 0.2s;
              
              &:hover {
                background-color: #f5f5f5;
                border-radius: 4px;
              }
            }
          }
        }
        
        :deep(.el-textarea__inner) {
          resize: none;
          min-height: 60px;
          font-size: 14px;
        }
        
        .input-actions {
          display: flex;
          justify-content: flex-end;
          margin-top: 10px;
        }
      }
    }
  }
}

// 定制滚动条样式
.session-list,
.chat-messages,
.emoji-container {
  &::-webkit-scrollbar {
    width: 6px;
  }
  
  &::-webkit-scrollbar-thumb {
    background-color: #ddd;
    border-radius: 3px;
    
    &:hover {
      background-color: #ccc;
    }
  }
  
  &::-webkit-scrollbar-track {
    background-color: #f7f7f7;
  }
}
</style> 
