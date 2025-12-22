<template>
  <div class="chat-message-area">
    <!-- 聊天头部 -->
    <div class="chat-header" v-if="currentSession">
      <h3 class="chat-title">{{ currentSession.name }}</h3>
      <div class="chat-actions">
        <el-dropdown trigger="click">
          <el-button icon="el-icon-more" circle></el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="$emit('clear')">清空聊天记录</el-dropdown-item>
              <el-dropdown-item @click="$emit('report')">举报用户</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
    
    <div class="chat-header" v-else>
      <h3 class="chat-title">请选择一个联系人开始聊天</h3>
    </div>
    
    <!-- 消息列表 -->
    <div class="chat-messages" ref="messagesContainer" v-if="currentSession">
      <!-- 加载更多 -->
      <div class="load-more" v-if="hasMore">
        <el-button type="text" size="small" @click="$emit('load-more')" :loading="loading">
          加载更多消息
        </el-button>
      </div>
      
      <!-- 消息列表 -->
      <div class="messages-list">
        <div 
          v-for="message in messages" 
          :key="message.id"
          class="message-item"
          :class="{ 'message-self': message.isSelf, 'message-other': !message.isSelf }">
          
          <!-- 时间分割线 -->
          <div class="message-time-divider" v-if="message.showTimeDivider">
            <span>{{ formatMessageTime(message.createTime) }}</span>
          </div>
          
          <!-- 消息内容 -->
          <div class="message-container">
            <!-- 头像（对方） -->
            <div class="message-avatar" v-if="!message.isSelf">
              <SafeImage 
                :src="currentSession?.avatar || ''" 
                type="avatar" 
                :alt="currentSession?.name" 
                class="avatar-img" />
            </div>
            
            <!-- 消息气泡 -->
            <div class="message-content">
              <div class="message-bubble">
                <div class="message-text" v-if="message.type === 'text'">
                  {{ message.content }}
                </div>
                <div class="message-image" v-else-if="message.type === 'image'">
                  <SafeImage 
                    :src="message.content" 
                    type="post" 
                    alt="图片消息" 
                    class="img-content" />
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
              <SafeImage 
                :src="selfAvatar" 
                type="avatar" 
                alt="我" 
                class="avatar-img" />
            </div>
          </div>
        </div>
      </div>
      
      <!-- 无消息 -->
      <div class="empty-messages" v-if="messages.length === 0">
        <el-empty description="暂无消息记录" :image-size="100"></el-empty>
      </div>
    </div>
    
    <!-- 无选中会话 -->
    <div class="select-tip" v-else>
      <SafeImage src="/images/chat/start.jpg" type="banner" alt="开始聊天" class="tip-image" />
      <p>选择一个联系人开始聊天</p>
    </div>
  </div>
</template>

<script>
import { ref, nextTick, watch } from 'vue'
import { Check, CircleCheck, Warning, Loading } from '@element-plus/icons-vue'
import SafeImage from '@/components/common/form/SafeImage.vue'

export default {
  name: 'ChatMessageArea',
  components: { Check, CircleCheck, Warning, Loading, SafeImage },
  props: {
    currentSession: { type: Object, default: null },
    messages: { type: Array, default: () => [] },
    hasMore: { type: Boolean, default: false },
    loading: { type: Boolean, default: false },
    selfAvatar: { type: String, default: '/images/avatars/default.jpg' }
  },
  emits: ['load-more', 'clear', 'report'],
  setup(props) {
    const messagesContainer = ref(null)
    
    const scrollToBottom = () => {
      if (messagesContainer.value) {
        messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
      }
    }
    
    const formatMessageTime = (timestamp) => {
      if (!timestamp) return ''
      const date = new Date(timestamp)
      return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
    }
    
    // 消息变化时滚动到底部
    watch(() => props.messages.length, () => {
      nextTick(scrollToBottom)
    })
    
    return { messagesContainer, scrollToBottom, formatMessageTime }
  }
}
</script>

<style lang="scss" scoped>
.chat-message-area {
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
  }
  
  .chat-messages {
    flex: 1;
    overflow-y: auto;
    padding: 15px;
    
    .load-more {
      text-align: center;
      margin-bottom: 15px;
    }
    
    .messages-list {
      .message-item {
        margin-bottom: 15px;
        
        .message-time-divider {
          text-align: center;
          margin: 15px 0;
          span {
            background-color: rgba(0, 0, 0, 0.1);
            padding: 4px 10px;
            border-radius: 4px;
            font-size: 12px;
            color: var(--text-secondary);
          }
        }
        
        .message-container {
          display: flex;
          align-items: flex-start;
          
          .message-avatar {
            .avatar-img { width: 40px; height: 40px; border-radius: 50%; object-fit: cover; }
          }
          
          .message-content {
            max-width: 60%;
            margin: 0 10px;
            
            .message-bubble {
              padding: 10px 15px;
              border-radius: 8px;
              background-color: #fff;
              box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
              
              .message-text { word-break: break-word; line-height: 1.5; }
              .message-image .img-content { max-width: 200px; max-height: 200px; border-radius: 4px; }
            }
            
            .message-status {
              margin-top: 4px;
              font-size: 12px;
              color: var(--text-placeholder);
              .status-failed { color: #f56c6c; }
            }
          }
        }
        
        &.message-self {
          .message-container {
            flex-direction: row-reverse;
            .message-content .message-bubble {
              background-color: #95ec69;
            }
            .message-content .message-status { text-align: right; }
          }
        }
      }
    }
    
    .empty-messages { padding: 50px 0; }
  }
  
  .select-tip {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    color: var(--text-secondary);
    
    .tip-image { width: 200px; height: auto; margin-bottom: 20px; opacity: 0.6; }
    p { font-size: 14px; }
  }
}
</style>
