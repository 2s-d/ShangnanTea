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
       - 清空聊天记录
       - 举报功能
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
          <div class="chat-actions">
            <el-dropdown trigger="click">
              <el-button icon="el-icon-more" circle></el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="clearMessages">清空聊天记录</el-dropdown-item>
                  <el-dropdown-item @click="reportUser">举报用户</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
        
        <div class="chat-header" v-else>
          <h3 class="chat-title">请选择一个联系人开始聊天</h3>
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
          <SafeImage src="/images/chat/start.jpg" type="banner" alt="开始聊天" class="tip-image" />
          <p>选择一个联系人开始聊天</p>
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
                <el-icon><Smile /></el-icon>
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

<script>
import { ref, reactive, computed, nextTick, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useStore } from 'vuex'
import { message } from '@/components/common'
import { showByCode, isSuccess } from '@/utils/apiMessages'
import { 
  Check, CircleCheck, Warning, Picture, Smile, Loading, 
  MoreFilled, Top
} from '@element-plus/icons-vue'
import SafeImage from '@/components/common/form/SafeImage.vue'
import { timeFormat } from '@/utils/timeFormat'

export default {
  name: 'ChatPage',
  components: {
    Check,
    CircleCheck,
    Warning,
    Picture,
    Smile,
    Loading,
    MoreFilled,
    Top,
    SafeImage
  },
  setup() {
    const route = useRoute()
    const router = useRouter()
    const store = useStore()
    
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
    
    // 获取所有会话列表
    const fetchSessions = async () => {
      try {
        const response = await store.dispatch('message/fetchChatSessions')
        
        // 显示API响应消息（成功或失败都通过状态码映射显示）
        showByCode(response.code)
        
        // 失败时直接返回
        if (!isSuccess(response.code)) {
          return
        }

        // 从Vuex获取会话列表数据
        const sessions = store.state.message.chatSessions || []
        
        // 转换数据格式以匹配UI组件的期望格式
        mockSessions.value = sessions.map(session => ({
          sessionId: session.id,
          userId: session.receiverId, // 对方用户ID
          targetId: session.receiverId,
          targetType: session.sessionType === 'customer' ? 'shop' : 'user',
          name: session.sessionType === 'customer' ? `店铺${session.receiverId}客服` : `用户${session.receiverId}`,
          avatar: `https://via.placeholder.com/50x50?text=${session.sessionType === 'customer' ? '店铺' : '用户'}`,
          lastMessage: session.lastMessage || '',
          lastTime: session.lastMessageTime,
          unreadCount: session.initiatorUnread || 0,
          isPinned: session.isPinned || false // 添加置顶状态
        }))

        // 默认选中：优先未读，否则第一个
        const unreadSession = mockSessions.value.find(session => session.unreadCount > 0)
        if (unreadSession) {
          selectSession(unreadSession)
        } else if (mockSessions.value.length > 0) {
          selectSession(mockSessions.value[0])
        }

        return
      } catch (error) {
        // 捕获意外的运行时错误（非API业务错误）
        // API业务失败已通过 showByCode 显示，网络错误已在响应拦截器显示
        // 这里只记录日志用于开发调试
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

        // 通过Vuex调用后端API获取聊天记录
        const response = await store.dispatch('message/fetchChatHistory', {
          sessionId: sessionId,
          params: {
            page: 1,
            pageSize: 50
          }
        })
        
        // 显示API响应消息（成功或失败都通过状态码映射显示）
        showByCode(response.code)
        
        // 只有成功时才更新消息列表
        if (isSuccess(response.code)) {
          const history = response.data?.list || []
          messagesMap[sessionId] = history.map(msg => ({
            id: msg.id,
            sessionId: sessionId,
            content: msg.content,
            type: msg.contentType || 'text',
            createTime: msg.createTime,
            status: msg.isRead ? 'read' : 'sent',
            isSelf: msg.senderId === store.state.user?.userInfo?.id,
            showTimeDivider: false
          }))
        }

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
      // TODO-SCRIPT: 需要统一 session 字段命名（userId/targetId），并在后端接口中固定。
      currentTargetUserId.value = session.userId || session.targetId || null
      
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
        
        // 调用Vuex action删除会话
        const response = await store.dispatch('message/deleteChatSession', sessionId)
        
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
        const response = await store.dispatch('message/pinChatSession', sessionId)
        
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
    
    // 清空聊天记录
    const clearMessages = () => {
      if (!currentSessionId.value) return
      
      ElMessageBox.confirm('确定要清空聊天记录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 生产版：清空聊天记录必须由后端确认后再刷新历史，前端不做本地“假清空”
        // TODO-SCRIPT: 需要后端提供清空接口（例如 POST /message/history/clear）
        message.info('清空聊天记录：待后端接口接入（当前不执行本地清空，避免产生假状态）')
        return
      }).catch(() => {
        // 用户取消清空
      })
    }
    
    // 举报用户
    const reportUser = () => {
      if (!currentSessionId.value) return
      
      ElMessageBox.prompt('请输入举报原因', '举报用户', {
        confirmButtonText: '提交',
        cancelButtonText: '取消',
        inputPlaceholder: '请详细描述您的举报原因'
      }).then(({ value }) => {
        // 生产版：举报需要后端记录与审核，前端不做本地“假提交成功”状态变更
        // TODO-SCRIPT: 需要后端提供举报接口（例如 POST /message/report）
        // 这里先保留输入流程，但不提交到本地状态，避免伪成功
        if (!value.trim()) {
          message.warning('举报原因不能为空')
          return
        }
        
        // 生产版：举报需要后端记录与审核，前端不做本地“假提交成功”状态变更
        // TODO-SCRIPT: 需要后端提供举报接口（例如 POST /message/report）
        message.info('举报：待后端接口接入（已记录原因输入，但当前不提交）')
        return
      }).catch(() => {
        // 用户取消举报
      })
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
        
        // 如果有图片文件，则调用图片上传API
        if (imageFile.value) {
          messageType = 'image'
          
          // 调用图片上传API
          const uploadResponse = await store.dispatch('message/sendImageMessage', {
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

        const sendResponse = await store.dispatch('message/sendMessage', {
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
    
    // 当前会话
    const currentSession = computed(() => {
      return mockSessions.value.find(s => s.sessionId === currentSessionId.value) || null
    })
    
    // 当前会话的消息列表
    const currentMessages = computed(() => {
      return messagesMap[currentSessionId.value] || []
    })
    
    // 初始化函数，根据路由参数自动打开特定会话
    const initializeChatFromRouteParams = () => {
      // 检查URL中是否包含shopId参数
      const shopId = route.query.shopId
      // 检查URL中是否包含userId参数
      const userId = route.query.userId
      
      if (shopId) {
        // 根据shopId找到对应店铺的会话
        // 在实际应用中，这里可能需要先检查是否已有此会话，没有则创建新会话
        const shopSession = mockSessions.value.find(session => 
          session.targetType === 'shop' && session.targetId.toString() === shopId.toString()
        )
        
        if (shopSession) {
          // 如果找到对应会话，自动选择该会话
          selectSession(shopSession)
        } else {
          // 如果没有找到，创建新的店铺客服会话
          createShopServiceSession(shopId)
        }
      } else if (userId) {
        // 根据userId找到对应用户的会话
        const userSession = mockSessions.value.find(session => 
          session.targetType === 'user' && session.targetId.toString() === userId.toString()
        )
        
        if (userSession) {
          // 如果找到对应会话，自动选择该会话
          selectSession(userSession)
        } else {
          // 如果没有找到，创建新的用户会话
          createUserChatSession(userId)
        }
      }
    }
    
    // 创建新的店铺客服会话
    const createShopServiceSession = shopId => {
      // 模拟创建新的店铺客服会话
      // 实际应用中应该调用API获取店铺信息和创建会话
      
      // 模拟店铺信息，实际应用中应从API获取
      const shopInfo = {
        id: shopId,
        name: shopId === '101' ? '秦岭茗茶' : 
          shopId === '102' ? '云雾茶庄' : 
            shopId === '103' ? '福建茶行' : `店铺${shopId}`,
        avatar: shopId === '101' ? 'https://via.placeholder.com/50x50?text=秦岭' :
          shopId === '102' ? 'https://via.placeholder.com/50x50?text=云雾' :
            shopId === '103' ? 'https://via.placeholder.com/50x50?text=福建' : 'https://via.placeholder.com/50x50?text=店铺'
      }
      
      // 创建新会话
      const newSession = {
        sessionId: `shop_${shopId}_${Date.now()}`,
        targetId: shopId,
        targetType: 'shop',
        name: `${shopInfo.name}客服`,
        avatar: shopInfo.avatar,
        lastMessage: '您好，有什么可以帮到您的吗？',
        lastTime: new Date().toISOString(),
        unreadCount: 0,
        messages: [
          {
            id: `msg_${Date.now()}`,
            sessionId: `shop_${shopId}_${Date.now()}`,
            content: '您好，有什么可以帮到您的吗？',
            type: 'text',
            createTime: new Date().toISOString(),
            status: 'read',
            isSelf: false,
            showTimeDivider: true
          }
        ]
      }
      
      // 添加到会话列表
      mockSessions.value.unshift(newSession)
      
      // 选择新创建的会话
      selectSession(newSession)
      
      // 显示欢迎消息
      message.success(`已连接到${shopInfo.name}客服`)
    }
    
    // 创建新的用户聊天会话
    const createUserChatSession = userId => {
      // 模拟创建新的用户聊天会话
      // 实际应用中应该调用API获取用户信息和创建会话
      
      // 模拟用户信息，实际应用中应从API获取
      const userInfo = {
        id: userId,
        name: userId === '1' ? '茶香四溢' : 
          userId === '2' ? '茶艺小能手' : 
            userId === '3' ? '普洱控' : `用户${userId}`,
        avatar: userId === '1' ? 'https://via.placeholder.com/50x50?text=茶香' :
          userId === '2' ? 'https://via.placeholder.com/50x50?text=茶艺' :
            userId === '3' ? 'https://via.placeholder.com/50x50?text=普洱' : 'https://via.placeholder.com/50x50?text=用户'
      }
      
      // 创建新会话
      const newSession = {
        sessionId: `user_${userId}_${Date.now()}`,
        targetId: userId,
        targetType: 'user',
        name: userInfo.name,
        avatar: userInfo.avatar,
        lastMessage: '',
        lastTime: new Date().toISOString(),
        unreadCount: 0,
        messages: []
      }
      
      // 添加到会话列表
      mockSessions.value.unshift(newSession)
      
      // 选择新创建的会话
      selectSession(newSession)
      
      // 显示提示消息
      message.success(`已开始与${userInfo.name}的聊天`)
    }
    
    // 在组件挂载时检查路由参数
    onMounted(() => {
      // 先加载所有会话
      fetchSessions()
      
      // 然后处理路由参数，自动打开特定会话
      initializeChatFromRouteParams()
      
      // 监听路由变化，以处理在聊天页面内导航到不同会话的情况
      if (route.query.shopId || route.query.userId) {
        initializeChatFromRouteParams()
      }
    })
    
    // 监听路由参数变化
    watch(() => route.query.shopId, newShopId => {
      if (newShopId) {
        initializeChatFromRouteParams()
      }
    })
    
    // 监听userId路由参数变化
    watch(() => route.query.userId, newUserId => {
      if (newUserId) {
        initializeChatFromRouteParams()
      }
    })
    
    return {
      // 引用
      messagesContainer,
      imageInput,
      
      // 状态
      searchQuery,
      messageInput,
      mockSessions,
      currentSessionId,
      loadingMessages,
      hasMoreMessages,
      showEmojiPicker,
      defaultAvatar,
      currentUserAvatar,
      emojiList,
      
      // 计算属性
      filteredSessions,
      currentSession,
      currentMessages,
      
      // 方法
      fetchSessions,
      fetchMessages,
      markSessionAsRead,
      loadMoreMessages,
      selectSession,
      deleteSession,
      togglePinSession,
      clearMessages,
      reportUser,
      sendMessage,
      triggerImageUpload,
      handleImageUpload,
      toggleEmojiPicker,
      insertEmoji,
      stopPropagation,
      formatTime,
      formatMessageTime,
      initializeChatFromRouteParams,
      createShopServiceSession,
      createUserChatSession
    }
  }
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
        padding: 15px;
        border-bottom: 1px solid #eee;
        background-color: #fff;
        display: flex;
        justify-content: space-between;
        align-items: center;
        
        .chat-title {
          margin: 0;
          font-size: 16px;
          font-weight: 500;
          color: var(--text-primary);
          text-align: center;
          flex: 1;
        }
        
        .chat-actions {
          :deep(.el-button) {
            padding: 6px;
            font-size: 16px;
          }
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
        color: var(--text-secondary);
        
        .tip-image {
          width: 120px;
          margin-bottom: 20px;
          opacity: 0.6;
        }
        
        p {
          font-size: 16px;
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
