<template>
  <div class="chat-sessions">
    <div class="search-bar">
      <el-input 
        v-model="localSearchQuery" 
        placeholder="搜索联系人或消息" 
        clearable
        prefix-icon="Search"
        @input="handleSearch">
      </el-input>
    </div>
    
    <div class="session-list">
      <div 
        v-for="session in sessions" 
        :key="session.sessionId"
        class="session-item"
        :class="{ 'session-active': currentSessionId === session.sessionId }"
        @click="$emit('select', session)">
        
        <div class="session-avatar">
          <el-badge 
            :value="session.unreadCount" 
            :hidden="!session.unreadCount"
            type="danger">
            <SafeImage 
              :src="session.avatar || ''" 
              type="avatar" 
              :alt="session.name" 
              class="avatar-img" />
          </el-badge>
        </div>
        
        <div class="session-info">
          <div class="session-name">{{ session.name }}</div>
          <div class="session-preview">{{ session.lastMessage }}</div>
        </div>
        
        <div class="session-meta">
          <div class="session-time">{{ formatTime(session.lastTime) }}</div>
          <div class="session-pin-badge" v-if="session.isPinned">
            <el-icon><Top /></el-icon>
          </div>
          <div class="session-actions">
            <el-popover placement="top" width="auto" trigger="click">
              <template #reference>
                <el-button circle size="small" class="more-action" @click.stop>
                  <el-icon><MoreFilled /></el-icon>
                </el-button>
              </template>
              <div class="action-buttons">
                <el-button 
                  size="small" 
                  :type="session.isPinned ? 'warning' : 'primary'"
                  @click="$emit('pin', session.sessionId, session.isPinned)">
                  {{ session.isPinned ? '取消置顶' : '置顶会话' }}
                </el-button>
                <el-button 
                  size="small" 
                  type="danger" 
                  @click="$emit('delete', session.sessionId)">
                  删除会话
                </el-button>
              </div>
            </el-popover>
          </div>
        </div>
      </div>
      
      <el-empty 
        v-if="sessions.length === 0" 
        description="暂无聊天会话"
        :image-size="100">
      </el-empty>
    </div>
  </div>
</template>

<script>
import { ref, watch } from 'vue'
import { Top, MoreFilled } from '@element-plus/icons-vue'
import SafeImage from '@/components/common/form/SafeImage.vue'

export default {
  name: 'ChatSessionList',
  components: { Top, MoreFilled, SafeImage },
  props: {
    sessions: { type: Array, default: () => [] },
    currentSessionId: { type: String, default: null },
    searchQuery: { type: String, default: '' }
  },
  emits: ['select', 'pin', 'delete', 'search'],
  setup(props, { emit }) {
    const localSearchQuery = ref(props.searchQuery)
    
    watch(() => props.searchQuery, (val) => {
      localSearchQuery.value = val
    })
    
    const handleSearch = (val) => {
      emit('search', val)
    }
    
    const formatTime = (timestamp) => {
      if (!timestamp) return ''
      const date = new Date(timestamp)
      const now = new Date()
      const diff = now - date
      
      if (diff < 86400000 && date.getDate() === now.getDate()) {
        return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
      }
      if (diff < 2 * 86400000 && date.getDate() === now.getDate() - 1) {
        return '昨天'
      }
      if (diff < 7 * 86400000) {
        const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
        return weekdays[date.getDay()]
      }
      return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
    }
    
    return { localSearchQuery, handleSearch, formatTime }
  }
}
</script>

<style lang="scss" scoped>
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
      
      &:hover { background-color: #f9f9f9; }
      &.session-active { background-color: #f0f7ff; }
      
      .session-avatar {
        margin-right: 12px;
        .avatar-img { width: 40px; height: 40px; border-radius: 50%; object-fit: cover; }
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
        
        .session-pin-badge {
          color: #e6a23c;
          font-size: 12px;
          margin-bottom: 2px;
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
          button { margin: 3px 0; }
        }
      }
      
      &:hover .session-actions { visibility: visible; }
    }
  }
}
</style>