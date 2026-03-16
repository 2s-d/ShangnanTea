<!-- eslint-disable indent, vue/html-indent, vue/script-indent -->
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
        <div class="search-bar" ref="searchBarRef" @click.stop>
          <el-input 
            v-model="searchQuery" 
            :class="['global-search-input', { 'is-typing': !!searchQuery }]"
            placeholder="搜索用户（昵称或ID）" 
            clearable
            prefix-icon="Search"
            @keyup.enter="handleGlobalSearch"
            @click.stop>
          </el-input>
          <el-button
            class="search-btn"
            type="primary"
            plain
            size="small"
            :loading="isSearchingUsers"
            @click.stop="handleGlobalSearch"
          >
            搜索
          </el-button>

          <!-- 全局搜索结果下拉 -->
          <div 
            v-if="showSearchResults" 
            class="global-search-results"
            @click.stop
          >
            <div 
              v-if="!globalSearchResults.length && !isSearchingUsers"
              class="search-empty"
            >
              暂未找到匹配的用户
            </div>
            <div
              v-for="user in globalSearchResults"
              :key="user.id || user.userId"
              class="search-user-item"
              @click="handleSelectSearchUser(user)"
            >
              <div class="search-user-avatar">
                <SafeImage
                  :src="user.avatar || ''"
                  type="avatar"
                  :alt="user.nickname || user.username || user.name || '用户头像'"
                  style="width:32px;height:32px;border-radius:50%;object-fit:cover;"
                />
              </div>
              <div class="search-user-info">
                <div class="name-line">
                  <span class="name-text">
                    {{ user.nickname || user.username || user.name || '未命名用户' }}
                  </span>
                  <span 
                    class="online-status" 
                    :class="{ online: !!user.online }"
                  >
                    <span class="dot"></span>
                    <span class="text">{{ user.online ? '在线' : '离线' }}</span>
                  </span>
                </div>
                <div class="id-line" v-if="user.id || user.userId">
                  ID：{{ user.id || user.userId }}
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <div class="left-panels">
          <div class="custom-collapse" :class="{ 
            'contacts-expanded': contactsExpanded, 
            'recent-expanded': recentExpanded,
            'contacts-collapsed': !contactsExpanded,
            'recent-collapsed': !recentExpanded
          }">
            <!-- 联系人面板 -->
            <div class="panel-item contacts-panel" :class="{ 'collapsed': !contactsExpanded }">
              <div class="panel-header" @click="contactsExpanded = !contactsExpanded">
                <div class="panel-title">
                  <span>联系人</span>
                  <span class="panel-count">{{ filteredContactCount }}</span>
                </div>
                <el-icon class="collapse-icon" :class="{ 'expanded': contactsExpanded }">
                  <ArrowDown />
                </el-icon>
              </div>

              <div class="panel-content" v-show="contactsExpanded">
              <div class="contacts-list">
                <template v-if="filteredContactGroups.length">
                  <div v-for="group in filteredContactGroups" :key="group.key" class="contact-group">
                    <div class="contact-group-header">{{ group.key }}</div>
                    <div
                      v-for="contact in group.items"
                      :key="contact.key"
                      class="contact-item"
                      @click="openContact(contact)" @dblclick.prevent="openContact(contact)"
                    >
                      <!-- 类型标识竖条 -->
                      <div class="contact-type-bar" :class="{ 'type-user': contact.type === 'user', 'type-shop': contact.type === 'shop' }"></div>
                      
                      <div class="contact-avatar" @click.stop="goToUserProfile(contact.id, contact.type)">
                        <SafeImage
                          :src="contact.avatar || ''"
                          type="avatar"
                          :alt="contact.name"
                          style="width:40px;height:40px;border-radius:50%;object-fit:cover;cursor:pointer;"
                        />
                      </div>
                      <div class="contact-info">
                        <div class="contact-name">
                          <span class="contact-name-text">{{ contact.name }}</span>
                          <span class="online-status" :class="{ online: contact.online }">
                            <span class="dot"></span>
                            <span class="text">{{ contact.online ? '在线' : '离线' }}</span>
                          </span>
                        </div>
                      </div>
                    </div>
                  </div>
                </template>
                <div v-else class="empty-panel">
                  <el-empty description="暂无联系人" :image-size="80" />
                </div>
              </div>
              </div>
            </div>

            <!-- 最近会话面板 -->
            <div class="panel-item recent-panel" :class="{ 'collapsed': !recentExpanded }">
              <div class="panel-header" @click="recentExpanded = !recentExpanded">
                <div class="panel-title">
                  <span>最近会话</span>
                  <span class="panel-count">{{ filteredSessions.length }}</span>
                </div>
                <el-icon class="collapse-icon" :class="{ 'expanded': recentExpanded }">
                  <ArrowDown />
                </el-icon>
              </div>

              <div class="panel-content" v-show="recentExpanded">
              <div class="session-list">
                <div
                  v-for="session in filteredSessions"
                  :key="session.sessionId"
                  class="session-item"
                  :class="{
                    'session-active': currentSessionId === session.sessionId,
                    'session-shop': session.targetType === 'shop'
                  }"
                  @click="selectSession(session)"
                >
                  <div class="session-avatar" @click.stop="goToUserProfile(session, session.targetType)">
                    <el-badge :value="session.unreadCount" :hidden="!session.unreadCount" type="danger">
                      <SafeImage
                        :src="session.avatar || ''"
                        type="avatar"
                        :alt="session.name"
                        style="width:40px;height:40px;border-radius:50%;object-fit:cover;cursor:pointer;"
                      />
                    </el-badge>
                  </div>

                  <div class="session-info">
                    <div class="session-name">
                      <span class="session-name-text">{{ session.name }}</span>
                      <span
                        v-if="session.targetType === 'shop'"
                        class="session-kind-badge is-shop"
                      >
                        客服会话
                      </span>
                      <el-icon v-if="session.isPinned" class="pin-icon" title="已置顶">
                        <Top />
                      </el-icon>
                    </div>
                    <div class="session-preview">{{ session.lastMessage }}</div>
                  </div>

                  <div class="session-meta">
                    <div class="session-time">{{ formatTime(session.lastTime) }}</div>
                    <span class="online-dot" :class="{ online: session.online }" title="在线状态">
                      <span class="dot"></span>
                    </span>
                    <div class="session-actions">
                      <el-popover placement="top" width="auto" trigger="click" @show="stopPropagation">
                        <template #reference>
                          <el-button circle size="small" class="more-action" @click.stop>
                            <el-icon><MoreFilled /></el-icon>
                          </el-button>
                        </template>
                        <div class="action-buttons">
                          <el-button size="small" @click="togglePinSession(session.sessionId)">
                            {{ session.isPinned ? '取消置顶' : '置顶会话' }}
                          </el-button>
                          <el-button size="small" type="danger" @click="deleteSession(session.sessionId)">
                            删除会话
                          </el-button>
                        </div>
                      </el-popover>
                    </div>
                  </div>
                </div>

                <el-empty
                  v-if="filteredSessions.length === 0"
                  description="暂无聊天会话"
                  :image-size="100"
                />
              </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 聊天内容区域 -->
      <div class="chat-content">
        <!-- 聊天头部 -->
        <div class="chat-header" v-if="currentSession">
          <div class="chat-header-avatar" @click="goToUserProfile(currentSession, currentSession.targetType)">
            <SafeImage :src="currentSession.avatar || ''" type="avatar" :alt="currentSession.name" style="width:40px;height:40px;border-radius:50%;object-fit:cover;cursor:pointer;margin-right:12px;" />
          </div>
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
                <!-- 对方消息：头像在左，消息在右 -->
                <template v-if="!message.isSelf">
                  <div class="message-avatar" @click="goToUserProfile(currentSession, currentSession?.targetType)">
                    <SafeImage :src="currentSession?.avatar || ''" type="avatar" :alt="currentSession?.name" style="width:40px;height:40px;border-radius:50%;object-fit:cover;cursor:pointer;" />
                  </div>
                  
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
                  </div>
                </template>
                
                <!-- 自己消息：消息在左，头像在右 -->
                <template v-else>
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
                      
                      <!-- 消息状态 - 放在消息框左下角 -->
                      <div class="message-status">
                        <el-icon v-if="message.status === 'sending'" style="font-size: 12px;"><Loading /></el-icon>
                        <el-icon v-else-if="message.status === 'sent'" style="font-size: 12px;"><Check /></el-icon>
                        <el-icon v-else-if="message.status === 'read'" style="font-size: 12px;"><CircleCheck /></el-icon>
                        <el-icon v-else-if="message.status === 'failed'" class="status-failed" style="font-size: 12px;"><Warning /></el-icon>
                      </div>
                    </div>
                  </div>
                  
                  <div class="message-avatar" @click="goToUserProfile(userStore.userInfo?.id, 'user')">
                    <SafeImage :src="currentUserAvatar" type="avatar" alt="我" style="width:40px;height:40px;border-radius:50%;object-fit:cover;cursor:pointer;" />
                  </div>
                </template>
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
  MoreFilled, Top, ArrowDown
} from '@element-plus/icons-vue'
import SafeImage from '@/components/common/form/SafeImage.vue'
import { timeFormat } from '@/utils/timeFormat'
import websocketManager from '@/utils/websocket'

const route = useRoute()
const router = useRouter()
const messageStore = useMessageStore()
const userStore = useUserStore()
    
    // DOM引用
    const messagesContainer = ref(null)
    const imageInput = ref(null)
    const searchBarRef = ref(null)
    
    // 搜索关键词（同时用于联系人/最近会话过滤 + 全局用户搜索）
    const searchQuery = ref('')
    const globalSearchResults = ref([])
    const isSearchingUsers = ref(false)
    const showSearchResults = ref(false)
    
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
    const currentUserAvatar = computed(() => userStore.userInfo?.avatar || defaultAvatar)
    
    // 联系人（来自 /message/contacts）
    const contacts = ref([])
    const contactsExpanded = ref(true)
    const recentExpanded = ref(true)
    
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

    const getGroupKey = name => {
        const s = (name || '').trim()
        if (!s) return '#'
        const ch = s[0].toUpperCase()
        return /^[A-Z]$/.test(ch) ? ch : '#'
    }
    
    const filteredContacts = computed(() => {
        const q = (searchQuery.value || '').trim().toLowerCase()
        if (!q) return contacts.value
        return contacts.value.filter(c => (c.name || '').toLowerCase().includes(q))
    })
    
    const filteredContactGroups = computed(() => {
        const map = new Map()
        filteredContacts.value.forEach(c => {
            const k = getGroupKey(c.name)
            if (!map.has(k)) map.set(k, [])
            map.get(k).push(c)
        })
        const keys = Array.from(map.keys()).sort((a, b) => {
            if (a === '#') return 1
            if (b === '#') return -1
            return a.localeCompare(b)
        })
        return keys.map(k => ({
            key: k,
            items: map.get(k).sort((a, b) => (a.name || '').localeCompare(b.name || ''))
        }))
    })
    
    const filteredContactCount = computed(() => filteredContacts.value.length)

    const fetchContacts = async () => {
        try {
            const res = await messageStore.fetchContacts()
            if (!isSuccess(res.code)) {
                showByCode(res.code)
                contacts.value = []
                return
            }
            const rawList = res.data || []
            contacts.value = (Array.isArray(rawList) ? rawList : []).map(item => {
                const type = item.type || 'user'
                const ownerId = item.ownerId || null
                const id = item.id
                const chatPeerId = type === 'shop' ? ownerId : id
                return {
                key: `${item.type || 'user'}:${item.id}`,
                id,
                type,
                name: item.name || '',
                avatar: item.logo || item.avatar || defaultAvatar,
                ownerId,
                chatPeerId,
                online: item.online === true
                }
            })
        } catch (e) {
            if (process.env.NODE_ENV === 'development') {
                console.error('[开发调试] 获取联系人列表失败：', e)
            }
        }
    }

    /**
     * 全局用户搜索
     * - 支持昵称模糊搜索
     * - 以 cy 开头时视为ID搜索（精确），由后端根据 keyword 规则处理
     */
    const handleGlobalSearch = async () => {
        const keyword = (searchQuery.value || '').trim()
        if (!keyword) {
            globalSearchResults.value = []
            showSearchResults.value = false
            return
        }

        isSearchingUsers.value = true
        try {
            const params = { keyword, page: 1, pageSize: 10 }
            const res = await messageStore.fetchSearchUsers(params)
            if (!isSuccess(res.code)) {
                showByCode(res.code)
                globalSearchResults.value = []
                showSearchResults.value = false
                return
            }
            const list = Array.isArray(res.data) ? res.data : (res.data?.list || [])
            globalSearchResults.value = list || []
            showSearchResults.value = true
        } finally {
            isSearchingUsers.value = false
        }
    }

    /**
     * 选择搜索结果用户，跳转到个人主页
     */
    const handleSelectSearchUser = user => {
        if (!user) return
        const userId = user.id || user.userId
        if (!userId) return
        showSearchResults.value = false
        router.push(`/profile/${userId}`)
    }

        /**
     * 消息页仅以用户身份发起：点用户 → private + 用户ID；点店铺 → customer + 店铺ID
     * 单击/双击均打开或创建会话；列表删除为软隐藏，再次进入会恢复
     */
    const openContact = async (contact) => {
        if (!contact) return

        let targetType
        let targetId

        if (contact.type === 'shop') {
            targetType = 'customer'
            targetId = contact.id
        } else {
            targetType = 'private'
            targetId = contact.id || contact.chatPeerId
        }

        if (!targetId) {
            message.warning('无法识别联系人，无法打开会话')
            return
        }

        const existing = mockSessions.value.find((session) => {
            if (contact.type === 'shop') {
                return session.targetType === 'shop' && String(session.shopId) === String(targetId)
            }
            return session.targetType === 'user' && String(session.receiverId) === String(targetId)
        })
        if (existing) {
            selectSession(existing)
            return
        }

        try {
            const res = await messageStore.createChatSession({ targetId, targetType })
            if (!isSuccess(res.code)) {
                showByCode(res.code)
                return
            }
            const sessionId = res.data && res.data.id
            await fetchSessions()
            if (sessionId) {
                const created = mockSessions.value.find((s) => String(s.sessionId) === String(sessionId))
                if (created) {
                    selectSession(created)
                    return
                }
            }
            const created2 = mockSessions.value.find((s) => {
                if (contact.type === 'shop') return s.targetType === 'shop' && String(s.shopId) === String(targetId)
                return s.targetType === 'user' && String(s.receiverId) === String(targetId)
            })
            if (created2) selectSession(created2)
        } catch (e) {
            if (process.env.NODE_ENV === 'development') {
                console.error('[开发调试] 打开联系人会话失败：', e)
            }
        }
    }
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

        // 从 Pinia 获取会话列表数据（后端已转换为VO结构）
        const sessions = messageStore.chatSessions || []
        
        // 转换数据格式以匹配 UI 组件的期望格式：
        // - receiverId 始终表示“对端用户”的 ID（targetUserId）
        // - unreadCount 来自后端的未读字段
        mockSessions.value = sessions.map(session => {
          const targetId = session.targetUserId
          const unreadCount = session.unreadCount ?? 0
          const isCustomerService = session.sessionType === 'customer' || session.sessionType === 'shop'
          
          // 店铺会话：显示店铺名称和店铺LOGO
          // 用户会话：显示用户昵称和用户头像
          // 名称展示规则（避免拼接“店铺xxx客服”这类噪声）：
          // - 客服会话：优先店铺名，其次对端昵称/用户名，最后才用ID兜底
          // - 私聊会话：优先对端昵称/用户名，最后用ID兜底
          const isPlatformCustomerService =
            isCustomerService &&
            (String(session.shopId || '').toUpperCase() === 'PLATFORM' ||
             String(session.targetUserId || '') === 'cy100001')

          let name
          if (isPlatformCustomerService) {
            // 平台客服会话：固定名称
            name = '平台直售客服'
          } else if (isCustomerService) {
            name = session.shopName || session.targetNickname || session.targetUsername || `店铺${session.shopId || targetId}`
          } else {
            name = session.targetNickname || session.targetUsername || `用户${targetId}`
          }
          
          // 店铺会话：使用店铺LOGO（shopAvatar），用户会话：使用用户头像（targetAvatar）
          const avatar = isPlatformCustomerService
            ? '/images/tea-logo.png'
            : (isCustomerService
                ? (session.shopAvatar || session.targetAvatar || `https://via.placeholder.com/50x50?text=店铺`)
                : (session.targetAvatar || `https://via.placeholder.com/50x50?text=用户`))

          return {
            sessionId: session.id,
            receiverId: targetId, // 对端用户ID（对于店铺会话，这是店主ID，但显示的是店铺信息）
            targetType: isCustomerService ? 'shop' : 'user',
            // 对于店铺会话，保存店铺ID（后端已返回）
            shopId: isCustomerService ? (session.shopId || null) : null,
            ownerId: isCustomerService ? targetId : null, // 店主ID（用于查询店铺）
            name,
            avatar,
            lastMessage: session.lastMessage || '',
            lastTime: session.lastMessageTime,
            unreadCount,
            isPinned: session.isPinned || false,
            online: !!session.targetOnline,
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
            isSelf: String(msg.senderId) === String(userStore.userInfo?.id),
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

      if (process.env.NODE_ENV === 'development') {
        console.info('[Chat] selectSession', {
          sessionId,
          targetType: session.targetType,
          receiverId: session.receiverId,
          shopId: session.shopId
        })
      }
      
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
      // 发送过程中可能发生会话切换，这里对关键字段做快照，保证本次发送与刷新一致
      const sessionIdSnapshot = currentSessionId.value
      const receiverIdSnapshot = currentTargetUserId.value

      if (!sessionIdSnapshot) return
      if (!messageInput.value.trim() && !imageFile.value) return
      
      try {
        if (process.env.NODE_ENV === 'development') {
          console.info('[Chat] sendMessage', {
            sessionId: sessionIdSnapshot,
            receiverId: receiverIdSnapshot,
            contentType: imageFile.value ? 'image' : 'text'
          })
        }
        const now = Date.now()
        const messageId = `msg-${now}`
        
        let messageType = 'text'
        let messageContent = messageInput.value.trim()
        
        // 如果有图片文件,则调用图片上传API
        if (imageFile.value) {
          messageType = 'image'
          
          // 调用图片上传API
          const uploadResponse = await messageStore.sendImageMessage({
            sessionId: sessionIdSnapshot,
            receiverId: receiverIdSnapshot,
            image: imageFile.value
          })
          
          // 显示API响应消息
          showByCode(uploadResponse.code)
          
          // 清空图片文件
          imageFile.value = null
          
          // 成功时刷新消息列表
          if (isSuccess(uploadResponse.code)) {
            await fetchMessages(sessionIdSnapshot, false)
            await nextTick()
            scrollToBottom()
          }
          
          return
        }
        
        // 发送文本消息
        if (!receiverIdSnapshot) {
          message.warning('缺少会话目标用户ID，暂无法发送消息')
          return
        }

        const sendResponse = await messageStore.sendMessage({
          sessionId: sessionIdSnapshot,
          receiverId: receiverIdSnapshot,
          content: messageContent,
          type: messageType
        })
        
        // 清空输入框
        messageInput.value = ''
        
        // 显示API响应消息（成功或失败都通过状态码映射显示）
        showByCode(sendResponse.code)
        
        // 只有成功时才刷新消息列表
        if (isSuccess(sendResponse.code)) {
          await fetchMessages(sessionIdSnapshot, false)
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
    
    // 跳转到用户/店铺个人主页
    // 参数可以是 (targetId, targetType) 或 (session对象)
    const goToUserProfile = (targetIdOrSession, targetType) => {
      let targetId, targetTypeValue
      
      // 判断第一个参数是会话对象还是targetId
      if (typeof targetIdOrSession === 'object' && targetIdOrSession !== null) {
        // 是会话对象
        const session = targetIdOrSession
        targetId = session.receiverId || session.id
        targetTypeValue = session.targetType
        
        // 对于店铺会话，targetId是店主ID，需要查询店铺ID
        if (targetTypeValue === 'shop') {
          // 如果有保存的店铺ID，直接使用
          if (session.shopId) {
            router.push(`/shop/${session.shopId}`)
            return
          }
          // 否则，根据店主ID查询店铺ID（需要调用API）
          // 这里先尝试从raw数据中获取，如果没有则使用ownerId查询
          const ownerId = session.ownerId || session.receiverId
          if (ownerId) {
            // 暂时使用ownerId作为店铺ID（这是临时方案，应该调用API查询）
            // TODO: 调用API根据ownerId查询店铺ID
            router.push(`/shop/${ownerId}`)
            return
          }
        }
      } else {
        // 是targetId和targetType
        targetId = targetIdOrSession
        targetTypeValue = targetType
      }
      
      if (!targetId || !targetTypeValue) return
      
      if (targetTypeValue === 'shop') {
        // 跳转到店铺详情页
        router.push(`/shop/${targetId}`)
      } else if (targetTypeValue === 'user') {
        // 跳转到用户个人主页
        router.push(`/profile/${targetId}`)
      }
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
    
    // 消息页路由仅用户身份：shopId->customer+店铺ID；userId->private+用户ID
    // 初始化函数：根据路由参数自动打开特定会话（店铺 / 用户）
    const initializeChatFromRouteParams = async () => {
      const sessionId = route.query.sessionId
      const shopId = route.query.shopId
      const userId = route.query.userId

      // 如果显式传了 sessionId，则直接按 sessionId 精确选中（个人主页私信/联系人创建会话后跳转）
      if (sessionId) {
        const existingBySessionId = mockSessions.value.find(s => String(s.sessionId) === String(sessionId))
        if (existingBySessionId) {
          selectSession(existingBySessionId)
          return
        }
      }
      
      // 如果没有指定目标，仅在有会话但未选中时做一次默认选择
      if (!shopId && !userId) {
        if (!currentSessionId.value && mockSessions.value.length > 0) {
          const unreadSession = mockSessions.value.find(session => session.unreadCount > 0)
          selectSession(unreadSession || mockSessions.value[0])
        }
        return
      }

      const targetType = shopId ? 'customer' : 'private'
      const targetId = shopId || userId

      const findSessionByRoute = () => {
        if (shopId) {
          return mockSessions.value.find(s => s.targetType === 'shop' && String(s.shopId) === String(shopId))
        }
        return mockSessions.value.find(s => s.targetType === 'user' && String(s.receiverId) === String(userId))
      }

      const existing = findSessionByRoute()
      // 严格参考“点击联系人”的会话创建/恢复逻辑：
      // - 未创建：创建新会话并选中
      // - 已存在：直接选中
      // - 已存在但软删除：后端 createChatSession 会恢复为正常并返回，同样走“创建后刷新并选中”
      if (existing) {
        selectSession(existing)
        return
      }

      try {
        // 与 openContact 保持一致：创建/恢复 -> 刷新会话列表 -> 通过 sessionId 精确选中
        const createRes = await messageStore.createChatSession({ targetId: String(targetId), targetType })
        if (!isSuccess(createRes.code)) {
          showByCode(createRes.code)
          return
        }

        const createdSessionId = createRes.data?.id || createRes.data?.data?.id
        await fetchSessions()

        // 优先按 sessionId 精确定位；找不到再按路由规则兜底
        const created = createdSessionId
          ? mockSessions.value.find(s => String(s.sessionId) === String(createdSessionId))
          : null
        const created2 = created || findSessionByRoute()
        if (created2) {
          selectSession(created2)
        }
      } catch (e) {
        if (process.env.NODE_ENV === 'development') {
          console.error('[开发调试] 路由初始化创建会话失败：', e)
        }
      }
    }
    
    // WebSocket 在线状态增量更新处理
    const handleOnlineStatusUpdate = messageData => {
        if (!messageData || messageData.type !== 'onlineStatus' || !messageData.userId) return
        const targetId = messageData.userId
        mockSessions.value.forEach(session => {
            if (String(session.receiverId) === String(targetId)) {
                session.online = !!messageData.online
            }
        })
        contacts.value.forEach(contact => {
            // user联系人：id=用户id；shop联系人：ownerId=店主id
            const matchId = contact.type === 'shop' ? contact.ownerId : contact.id
            if (String(matchId) === String(targetId)) {
                contact.online = !!messageData.online
            }
        })
    }

    // 点击外部区域关闭搜索结果面板
    const handleClickOutside = (event) => {
        if (searchBarRef.value && !searchBarRef.value.contains(event.target)) {
            showSearchResults.value = false
        }
    }

    // 在组件挂载时初始化：先加载会话，再根据路由参数选择目标
    onMounted(async () => {
        await fetchContacts()
        await fetchSessions()
        await initializeChatFromRouteParams()
        websocketManager.on('onlineStatus', handleOnlineStatusUpdate)
        // 添加点击外部区域监听器
        document.addEventListener('click', handleClickOutside)
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

    // 组件卸载时取消WebSocket监听和点击外部区域监听
    onBeforeUnmount(() => {
      websocketManager.off('onlineStatus', handleOnlineStatusUpdate)
      document.removeEventListener('click', handleClickOutside)
    })

    // 引导按钮：跳转到茶叶商城
    const goToTeaMall = () => {
      router.push('/tea/mall')
    }
</script>

<style lang="scss" scoped>
.chat-page {
  position: relative;
  // 默认禁止选中文本
  user-select: none;
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
  
  // 输入框允许选中（el-input、el-textarea等），但搜索框除外
  :deep(.el-textarea__inner),
  :deep(textarea),
  :deep(input:not(.el-input__inner)) {
    user-select: text;
    -webkit-user-select: text;
    -moz-user-select: text;
    -ms-user-select: text;
  }
  
  // 聊天消息输入框允许选中
  .chat-input-area {
    :deep(.el-textarea__inner) {
      user-select: text;
      -webkit-user-select: text;
      -moz-user-select: text;
      -ms-user-select: text;
    }
  }
  
  .chat-layout {
    display: flex;
    height: calc(100vh - 280px);
    min-height: 650px;
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
        position: relative;
        padding: 15px;
        border-bottom: 1px solid #eee;
        background-color: #fff;
        display: flex;
        align-items: center;
        gap: 8px;
      }

      .global-search-input {
        flex: 1;

        :deep(.el-input__wrapper) {
          transition: box-shadow 0.2s, border-color 0.2s;
        }

        &.is-typing :deep(.el-input__wrapper) {
          box-shadow: 0 0 0 1px #a0d8ff;
          border-color: #a0d8ff;
        }
        
        // 搜索框输入框：聚焦时允许选中和编辑，未聚焦时禁止选中
        :deep(.el-input__inner) {
          &:focus {
            user-select: text;
            -webkit-user-select: text;
            -moz-user-select: text;
            -ms-user-select: text;
          }
          &:not(:focus) {
            user-select: none;
            -webkit-user-select: none;
            -moz-user-select: none;
            -ms-user-select: none;
          }
        }
      }

      .search-btn {
        white-space: nowrap;
      }

      .left-panels {
        flex: 1;
        overflow: hidden;
        background-color: #f7f7f7;
        display: flex;
        flex-direction: column;
      }

      .global-search-results {
        position: absolute;
        left: 15px;
        right: 15px;
        top: 100%;
        margin-top: 6px;
        max-height: 260px;
        overflow-y: auto;
        background-color: #fff;
        border-radius: 6px;
        box-shadow: 0 6px 18px rgba(0, 0, 0, 0.12);
        z-index: 20;
        padding: 6px 0;
      }

      .search-empty {
        padding: 10px 16px;
        font-size: 13px;
        color: var(--text-secondary);
      }

      .search-user-item {
        display: flex;
        align-items: center;
        padding: 8px 12px;
        cursor: pointer;
        transition: background-color 0.2s;

        &:hover {
          background-color: #f5f9ff;
        }
      }

      .search-user-avatar {
        margin-right: 10px;
      }

      .search-user-info {
        flex: 1;

        .name-line {
          display: flex;
          align-items: center;
          justify-content: space-between;
          margin-bottom: 2px;
        }

        .name-text {
          font-size: 14px;
          font-weight: 500;
          color: var(--text-primary);
        }

        .id-line {
          font-size: 12px;
          color: var(--text-secondary);
        }
      }

      .custom-collapse {
        height: 100%;
        display: flex;
        flex-direction: column;
        overflow: hidden;
      }

      .panel-item {
        flex-shrink: 0;
        display: flex;
        flex-direction: column;
        background-color: #fff;
        border-bottom: 1px solid #eee;
      }

      // 两个面板都展开时：平均分配空间
      .custom-collapse.contacts-expanded.recent-expanded {
        .contacts-panel:not(.collapsed),
        .recent-panel:not(.collapsed) {
          flex: 1;
          min-height: 0;
          
          .panel-content {
            flex: 1;
            min-height: 0;
            display: flex;
            flex-direction: column;
          }
        }
        
        .contacts-list,
        .session-list {
          flex: 1;
          min-height: 0;
          max-height: none;
        }
      }

      // 联系人面板：仅联系人展开时，占据剩余空间
      .custom-collapse.contacts-expanded.recent-collapsed {
        .contacts-panel:not(.collapsed) {
          flex: 1;
          min-height: 0;
          
          .panel-content {
            flex: 1;
            min-height: 0;
            display: flex;
            flex-direction: column;
          }
          
          .contacts-list {
            flex: 1;
            min-height: 0;
            max-height: none;
          }
        }
        
        // 最近会话折叠时，推到底部
        .recent-panel.collapsed {
          margin-top: auto;
        }
      }

      // 最近会话面板：仅最近会话展开时，占据剩余空间
      .custom-collapse.contacts-collapsed.recent-expanded {
        .recent-panel:not(.collapsed) {
          flex: 1;
          min-height: 0;
          
          .panel-content {
            flex: 1;
            min-height: 0;
            display: flex;
            flex-direction: column;
          }
          
          .session-list {
            flex: 1;
            min-height: 0;
            max-height: none;
          }
        }
      }

      .panel-header {
          padding: 0 12px;
          background: #fff;
          height: 44px;
        display: flex;
        align-items: center;
        justify-content: space-between;
        cursor: pointer;
        user-select: none;
        
        &:hover {
          background-color: #f5f5f5;
        }
      }

      .panel-content {
        display: flex;
        flex-direction: column;
        overflow: hidden;
      }

      .collapse-icon {
        transition: transform 0.3s;
        color: var(--text-secondary);
        
        &.expanded {
          transform: rotate(180deg);
        }
      }

      .panel-title {
        width: 100%;
        display: flex;
        justify-content: space-between;
        align-items: center;
        font-weight: 600;
        color: var(--text-primary);
      }

      .panel-count {
        font-size: 12px;
        color: var(--text-secondary);
      }

      .contacts-list {
        max-height: 240px;
        overflow-y: auto;
        background: #f7f7f7;
      }

      .contact-group-header {
        padding: 6px 12px;
        font-size: 12px;
        color: var(--text-secondary);
      }

      .contact-item {
        display: flex;
        padding: 10px 12px;
        cursor: pointer;
        transition: background-color 0.2s;
        align-items: center;
        position: relative;

        &:hover {
          background-color: #eef5ff;
        }

        .contact-type-bar {
          position: absolute;
          left: 0;
          top: 50%;
          transform: translateY(-50%);
          width: 3px;
          height: 32px;
          border-radius: 0 2px 2px 0;
          transition: all 0.3s ease;
          
          &.type-user {
            background: linear-gradient(180deg, #52c41a 0%, #73d13d 100%);
            box-shadow: 0 0 6px rgba(82, 196, 26, 0.3);
          }
          
          &.type-shop {
            background: linear-gradient(180deg, #ff4d4f 0%, #ff7875 100%);
            box-shadow: 0 0 6px rgba(255, 77, 79, 0.3);
          }
        }

        &:hover .contact-type-bar {
          width: 3.5px;
          box-shadow: 0 0 10px rgba(0, 0, 0, 0.2);
          
          &.type-user {
            box-shadow: 0 0 10px rgba(82, 196, 26, 0.5);
          }
          
          &.type-shop {
            box-shadow: 0 0 10px rgba(255, 77, 79, 0.5);
          }
        }
      }

      .contact-info {
        flex: 1;
        margin-left: 10px;
        min-width: 0;
      }

      .contact-name {
        display: flex;
        align-items: center;
        justify-content: space-between;
        font-size: 14px;
        color: var(--text-primary);

        .contact-name-text {
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          margin-right: 8px;
        }
      }

      .empty-panel {
        padding: 12px 0;
        background: #f7f7f7;
      }

      .session-list {
        max-height: 240px;
        overflow-y: auto;
        background-color: #f7f7f7;
        
        .session-item {
          display: flex;
          padding: 12px 15px;
          cursor: pointer;
          transition: background-color 0.2s;
          border-bottom: 1px solid #f0f0f0;
          background-color: #fff;

          &.session-shop {
            background: linear-gradient(90deg, rgba(64, 158, 255, 0.08), rgba(255, 255, 255, 1) 55%);
          }
          
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

              .session-name-text {
                flex: 1;
                min-width: 0;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
              }

              .session-kind-badge {
                flex-shrink: 0;
                font-size: 12px;
                line-height: 18px;
                padding: 0 6px;
                border-radius: 10px;
                border: 1px solid rgba(0, 0, 0, 0.08);
                color: rgba(0, 0, 0, 0.65);
                background: rgba(0, 0, 0, 0.03);

                &.is-shop {
                  border-color: rgba(245, 34, 45, 0.30);
                  color: #cf1322;
                  background: rgba(245, 34, 45, 0.10);
                }
              }
              
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

            .online-dot {
              display: inline-flex;
              align-items: center;
              justify-content: flex-end;
              height: 14px;
              margin-bottom: 6px;

              .dot {
                width: 8px;
                height: 8px;
                border-radius: 50%;
                background: #c0c4cc;
              }

              &.online {
                .dot {
                  background: #2ecc71;
                }
              }
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

      // 联系人/搜索结果仍使用 online-status（点 + 在线/离线 文案）
      .online-status {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        margin-left: 8px;
        font-size: 12px;
        color: var(--text-secondary);

        .dot {
          width: 8px;
          height: 8px;
          border-radius: 50%;
          background: #c0c4cc;
        }

        &.online {
          color: #2ecc71;

          .dot {
            background: #2ecc71;
          }
        }

        .text {
          white-space: nowrap;
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
        flex-direction: row;
        justify-content: flex-start;
        align-items: center;
        
        .chat-header-avatar {
          flex-shrink: 0;
        }
        
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
        // 聊天消息区域允许选中文本
        user-select: text;
        -webkit-user-select: text;
        -moz-user-select: text;
        -ms-user-select: text;
        
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
            display: flex;
            flex-direction: column;
            
            &.message-self {
              align-items: flex-end;
              
              .message-container {
                display: flex;
                flex-direction: row;
                justify-content: flex-end;
                align-items: flex-start;
                
                .message-content {
                  margin-right: 8px;
                  display: flex;
                  flex-direction: column;
                  align-items: flex-end;
                  
                  .message-bubble {
                    background-color: #95ec69;
                    position: relative;
                    padding-bottom: 18px;
                    
                    .message-status {
                      position: absolute;
                      left: 8px;
                      bottom: 4px;
                      font-size: 10px;
                      color: var(--text-placeholder);
                      display: flex;
                      align-items: center;
                      line-height: 1;
                      
                      .status-failed {
                        color: #f56c6c;
                      }
                    }
                  }
                }
                
                .message-avatar {
                  flex-shrink: 0;
                }
              }
            }
            
            &.message-other {
              align-items: flex-start;
              
              .message-container {
                display: flex;
                flex-direction: row;
                justify-content: flex-start;
                align-items: flex-start;
                
                .message-avatar {
                  flex-shrink: 0;
                  margin-right: 8px;
                }
                
                .message-content {
                  align-items: flex-start;
                  
                  .message-bubble {
                    background-color: #fff;
                  }
                }
              }
            }
            
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
              align-items: flex-start;
              
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

// 定制滚动条样式（统一使用淡色样式）
.contacts-list,
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
