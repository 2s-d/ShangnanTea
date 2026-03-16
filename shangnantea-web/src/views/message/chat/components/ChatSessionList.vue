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
        :class="{
          'session-active': currentSessionId === session.sessionId,
          'session-shop': getSessionKindInfo(session).kind === 'shop'
        }"
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
          <div class="session-name">
            <span class="name-left">
              <span
                class="session-kind-badge"
                :class="getSessionKindInfo(session).kind === 'shop' ? 'is-shop' : 'is-user'"
              >
                <el-icon class="kind-icon">
                  <component :is="getSessionKindInfo(session).icon" />
                </el-icon>
                {{ getSessionKindInfo(session).label }}
              </span>
              <span class="name-text">{{ session.name }}</span>
            </span>
            <span class="online-status">
              <span
                class="status-dot"
                :class="session.online ? 'online' : 'offline'"
              ></span>
              <span class="status-text">{{ session.online ? '在线' : '离线' }}</span>
            </span>
          </div>
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
import { Top, MoreFilled, Service, UserFilled } from '@element-plus/icons-vue'
import SafeImage from '@/components/common/form/SafeImage.vue'

export default {
  name: 'ChatSessionList',
  components: { Top, MoreFilled, Service, UserFilled, SafeImage },
  props: {
    sessions: { type: Array, default: () => [] },
    currentSessionId: { type: String, default: null },
    searchQuery: { type: String, default: '' }
  },
  emits: ['select', 'pin', 'delete', 'search'],
  setup(props, { emit }) {
    const localSearchQuery = ref(props.searchQuery)
    
    watch(() => props.searchQuery, val => {
      localSearchQuery.value = val
    })
    
    const handleSearch = val => {
      emit('search', val)
    }
    
    const formatTime = timestamp => {
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

    /**
     * 获取会话类型展示信息。
     * - user: 私聊（与对端用户）
     * - shop: 店铺客服（与店铺客服/店主）
     *
     * @param {Object} session 会话对象
     * @returns {{ kind: 'user'|'shop', label: string, icon: string }}
     */
    const getSessionKindInfo = (session) => {
      const isShop = session && session.targetType === 'shop'
      return {
        kind: isShop ? 'shop' : 'user',
        label: isShop ? '客服' : '私聊',
        icon: isShop ? 'Service' : 'UserFilled'
      }
    }
    
    return { localSearchQuery, handleSearch, formatTime, getSessionKindInfo }
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

      &.session-shop {
        background: linear-gradient(90deg, rgba(64, 158, 255, 0.08), rgba(255, 255, 255, 1) 55%);
      }
      
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
          display: flex;
          align-items: center;
          justify-content: space-between;
          
          .name-text {
            flex: 1;
            min-width: 0;
            margin-right: 6px;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
          }

          .name-left {
            display: inline-flex;
            align-items: center;
            min-width: 0;
            flex: 1;
            margin-right: 6px;
          }

          .session-kind-badge {
            display: inline-flex;
            align-items: center;
            flex-shrink: 0;
            font-size: 12px;
            line-height: 18px;
            padding: 0 6px;
            border-radius: 10px;
            border: 1px solid rgba(0, 0, 0, 0.08);
            color: rgba(0, 0, 0, 0.65);
            background: rgba(0, 0, 0, 0.03);
            margin-right: 6px;

            &.is-shop {
              border-color: rgba(64, 158, 255, 0.28);
              color: #1677ff;
              background: rgba(64, 158, 255, 0.12);
            }

            &.is-user {
              border-color: rgba(103, 194, 58, 0.26);
              color: #2f9e44;
              background: rgba(103, 194, 58, 0.10);
            }

            .kind-icon {
              font-size: 12px;
              margin-right: 4px;
            }
          }
          
          .online-status {
            display: inline-flex;
            align-items: center;
            flex-shrink: 0;
            
            .status-dot {
              width: 8px;
              height: 8px;
              border-radius: 50%;
              margin-right: 4px;
              
              &.online {
                background-color: #52c41a;
              }
              
              &.offline {
                background-color: #d9d9d9;
              }
            }
            
            .status-text {
              font-size: 12px;
              color: var(--text-secondary);
            }
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
