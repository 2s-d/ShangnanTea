<template>
  <div class="system-notifications-page">
    <!-- 筛选和操作区域 -->
    <div class="notifications-header">
      <div class="filter-actions">
        <el-radio-group v-model="readStatus" size="small">
          <el-radio-button value="all">全部</el-radio-button>
          <el-radio-button value="unread">未读</el-radio-button>
          <el-radio-button value="read">已读</el-radio-button>
        </el-radio-group>
        
        <el-select v-model="typeFilter" placeholder="消息类型" clearable size="small">
          <el-option label="全部类型" value=""></el-option>
          <el-option label="帖子回复" value="post_reply"></el-option>
          <el-option label="系统公告" value="system_announcement"></el-option>
          <el-option label="外部链接" value="external_link"></el-option>
        </el-select>
      </div>
      
      <div class="batch-actions" v-if="notifications.length > 0">
        <el-button size="small" type="primary" @click="markAllAsRead" :disabled="!hasUnread">
          全部标为已读
        </el-button>
        <el-button size="small" type="danger" @click="confirmDeleteAll">
          清空通知
        </el-button>
      </div>
    </div>
    
    <!-- 通知列表 -->
    <template v-if="filteredNotifications.length > 0">
      <div class="notifications-list">
        <div 
          v-for="notification in filteredNotifications" 
          :key="notification.id" 
          class="notification-item"
          :class="{ 'notification-unread': notification.isRead === 0 }"
        >
          <div class="notification-icon" :class="getNotificationIconClass(notification.type)">
            <el-icon v-if="notification.type === 'post_reply'"><ChatDotRound /></el-icon>
            <el-icon v-else-if="notification.type === 'system_announcement'"><Bell /></el-icon>
            <el-icon v-else-if="notification.type === 'external_link'"><Star /></el-icon>
            <el-icon v-else><Bell /></el-icon>
          </div>
          
          <div class="notification-content" @click="openNotification(notification)">
            <div class="notification-title">
              {{ notification.title }}
              <span class="unread-dot" v-if="notification.isRead === 0"></span>
            </div>
            <div class="notification-message">{{ notification.content }}</div>
            <div class="notification-time">{{ formatDate(notification.createTime) }}</div>
          </div>
          
          <div class="notification-actions">
            <el-tooltip 
              content="标记为已读" 
              placement="top" 
              v-if="notification.isRead === 0">
              <el-button 
                type="info" 
                circle 
                size="small"
                @click.stop="markAsRead(notification.id)">
                <el-icon><Check /></el-icon>
              </el-button>
            </el-tooltip>
            
            <el-tooltip content="删除通知" placement="top">
              <el-button 
                type="danger" 
                circle 
                size="small"
                @click.stop="deleteNotification(notification.id)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </el-tooltip>
          </div>
        
          <!-- 邮件式详情框：每条通知内根据 expandedId 控制展开 -->
        <transition name="el-fade-in">
          <div v-if="expandedId === notification.id" class="notification-detail-card">
            <div class="detail-header">
              <div class="detail-title">{{ (detailMap[notification.id] || notification).title }}</div>
              <div class="detail-meta">
                <span class="detail-time">{{ formatDate((detailMap[notification.id] || notification).createTime) }}</span>
                <el-tag size="small" type="info">{{ getTypeLabel(notification.type) }}</el-tag>
              </div>
            </div>
            
            <div class="detail-body">
              <!-- 1. 纯文本通知：系统公告等纯文字信息 -->
              <template v-if="notification.type === 'system_announcement'">
                <div class="detail-text">
                  {{ (detailMap[notification.id] || notification).content }}
                </div>
              </template>
              
              <!-- 2. 带外部链接通知：可跳转到外部资源 -->
              <template v-else-if="notification.type === 'external_link'">
                <div class="detail-text">
                  {{ (detailMap[notification.id] || notification).content }}
                </div>
                <div class="detail-actions" v-if="getExternalUrl(detailMap[notification.id] || notification)">
                  <el-button 
                    type="primary" 
                    @click="window.open(getExternalUrl(detailMap[notification.id] || notification), '_blank')">
                    打开链接
                  </el-button>
                </div>
              </template>
            </div>
          </div>
        </transition>
        </div>
      </div>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="totalNotifications"
          :page-size="pageSize"
          :current-page="currentPage"
          @current-change="handlePageChange">
        </el-pagination>
      </div>
    </template>
    
    <!-- 空状态 -->
    <el-empty 
      v-else 
      description="暂无通知消息" 
      :image-size="200">
    </el-empty>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { Bell, ChatDotRound, Star, Check, Delete } from '@element-plus/icons-vue'
import { useMessageStore } from '@/stores/message'
import { showByCode, isSuccess } from '@/utils/apiMessages'

const router = useRouter()
const messageStore = useMessageStore()
    
    // 分页参数
    const currentPage = ref(1)
    const pageSize = ref(10)
    const totalNotifications = ref(0)
    
    // 筛选参数
    const readStatus = ref('all')
    const typeFilter = ref('')
    
    // 展开详情状态
    const expandedId = ref(null)
    const detailMap = ref({})
    
    // 通知列表数据（以 Pinia 为单一数据源）
    const notifications = computed(() => messageStore.messages || [])
    const loading = computed(() => messageStore.loading)
    
    // 获取通知列表
    const fetchNotifications = async () => {
      // 同步分页参数到 Pinia
      if (messageStore.pagination) {
        messageStore.pagination.currentPage = currentPage.value
        messageStore.pagination.pageSize = pageSize.value
      }

      const response = await messageStore.fetchNotifications({
        readStatus: readStatus.value,
        type: typeFilter.value
      })
      
      // 显示API响应消息（成功或失败都通过状态码映射显示）
      showByCode(response.code)
      
      // 只有成功时才更新总数
      if (isSuccess(response.code)) {
        totalNotifications.value = response.data?.total || 0
      }
    }
    
    // 过滤通知
    const filteredNotifications = computed(() => {
      let result = [...notifications.value]
      
      // 按已读/未读状态过滤
      if (readStatus.value === 'read') {
        result = result.filter(item => item.isRead === 1)
      } else if (readStatus.value === 'unread') {
        result = result.filter(item => item.isRead === 0)
      }
      
      // 按类型过滤
      if (typeFilter.value) {
        result = result.filter(item => item.type === typeFilter.value)
      }
      
      return result
    })
    
    // 是否有未读通知
    const hasUnread = computed(() => {
      return notifications.value.some(item => item.isRead === 0)
    })
    
    // 标记单条通知为已读
    const markAsRead = async id => {
      try {
        const response = await messageStore.markNotificationAsRead(id)
        showByCode(response.code)
      } catch (error) {
        console.error(error)
      }
    }
    
    // 标记所有通知为已读
    const markAllAsRead = async () => {
      try {
        const unreadIds = notifications.value.filter(item => item.isRead === 0).map(item => item.id)
        if (!unreadIds.length) {
          return
        }

        const response = await messageStore.batchMarkAsRead(unreadIds)
        showByCode(response.code)
      } catch (error) {
        console.error(error)
      }
    }
    
    // 删除单条通知
    const deleteNotification = async id => {
      try {
        const response = await messageStore.deleteNotification(id)
        showByCode(response.code)
      } catch (error) {
        console.error(error)
      }
    }
    
    // 清空全部通知
    const confirmDeleteAll = () => {
      ElMessageBox.confirm('确定要清空所有通知吗？此操作不可恢复。', '确认操作', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteAllNotifications()
      }).catch(() => {
        // 用户取消操作
      })
    }
    
    // 删除所有通知
    const deleteAllNotifications = async () => {
      try {
        const allIds = notifications.value.map(item => item.id)
        if (!allIds.length) {
          return
        }

        const response = await messageStore.batchDeleteNotifications(allIds)
        showByCode(response.code)
      } catch (error) {
        console.error(error)
      }
    }
    
    // 打开通知
    const openNotification = async notification => {
      // 1. 可跳转通知：统一按targetType路由
      if (notification.type === 'post_reply') {
          if (notification.isRead === 0) {
            await markAsRead(notification.id)
          }

        const targetType = notification.targetType
        const targetId = notification.targetId

        // 根据不同目标类型跳转到对应业务页面
        switch (targetType) {
          case 'post':
            router.push({ name: 'ForumDetail', params: { id: targetId } })
            break
          case 'order':
            router.push({ name: 'OrderDetail', params: { id: targetId } })
            break
          case 'tea':
            router.push({ name: 'TeaDetail', params: { id: targetId } })
            break
          case 'shop':
            router.push({ name: 'ShopDetail', params: { id: targetId } })
            break
          case 'user':
            // 个人主页目前按当前用户路由设计，此处暂不区分，后续如有需要可扩展
            router.push({ name: 'UserProfile', params: { tab: 'overview' } })
            break
          case 'chat_session':
            router.push({ name: 'MessageChat', query: { sessionId: targetId } })
            break
          default:
            // 无法识别的targetType，不做跳转
            break
        }
        return
      }
      
      // 2. 其他类型（system_announcement、external_link）：展开详情框
      // 如果点击的是已展开的通知，则收起
      if (expandedId.value === notification.id) {
        expandedId.value = null
        return
      }
      
      // 展开当前通知
      expandedId.value = notification.id
      
      // 如果详情已加载，直接使用
      if (detailMap.value[notification.id]) {
        // 标记为已读（如果未读）
        if (notification.isRead === 0) {
          await markAsRead(notification.id)
        }
        return
      }
      
      // 获取通知详情（后端会自动标记为已读）
      try {
        const res = await messageStore.fetchNotificationDetail(notification.id)
        if (res.data) {
          detailMap.value[notification.id] = res.data
          // 同步本地未读状态
          if (notification.isRead === 0) {
            notification.isRead = 1
            await messageStore.fetchUnreadCount()
          }
        }
      } catch (error) {
        console.error('获取通知详情失败:', error)
        // 如果获取详情失败，使用列表数据
        detailMap.value[notification.id] = notification
      }
    }
    
    // 切换页码
    const handlePageChange = page => {
      currentPage.value = page
      fetchNotifications()
    }
    
    // 格式化时间
    const formatDate = dateString => {
      if (!dateString) return ''
      
      const date = new Date(dateString)
      const now = new Date()
      const diffTime = Math.abs(now - date)
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
      
      if (diffDays < 1) {
        // 当天显示时分
        return `今天 ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
      } else if (diffDays < 7) {
        // 一周内显示天数
        return `${diffDays}天前`
      } else {
        // 超过一周显示完整日期
        return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
      }
    }
    
    // 获取通知图标样式
    const getNotificationIconClass = type => {
      const classMap = {
        post_reply: 'icon-forum',
        system_announcement: 'icon-system',
        external_link: 'icon-like'
      }
      
      return classMap[type] || 'icon-default'
    }
    
    // 获取类型标签
    const getTypeLabel = type => {
      const labelMap = {
        post_reply: '帖子回复',
        system_announcement: '系统公告',
        external_link: '外部链接'
      }
      return labelMap[type] || '通知'
    }
    
    // 获取外部链接
    const getExternalUrl = detail => {
      try {
        const extraData = detail && detail.extraData ? JSON.parse(detail.extraData) : null
        return extraData && extraData.externalUrl ? extraData.externalUrl : ''
      } catch {
        return ''
      }
    }
    
    // 初始化
    onMounted(() => {
      fetchNotifications()
    })

// 筛选变化自动刷新（回到第一页）
watch([readStatus, typeFilter], () => {
  currentPage.value = 1
  fetchNotifications()
})
</script>

<style lang="scss" scoped>
.system-notifications-page {
  padding: 20px;
  
  .notifications-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    flex-wrap: wrap;
    gap: 15px;
    
    .filter-actions {
      display: flex;
      align-items: center;
      gap: 15px;
    }
  }
  
  .notifications-list {
    .notification-item {
      display: flex;
      align-items: flex-start;
      padding: 16px;
      border-bottom: 1px solid #f0f0f0;
      transition: background-color 0.2s;
      
      &:hover {
        background-color: #f9f9f9;
      }
      
      &.notification-unread {
        background-color: rgba(64, 158, 255, 0.05);
      }
      
      .notification-icon {
        width: 40px;
        height: 40px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 15px;
        flex-shrink: 0;
        
        &.icon-system {
          background-color: #e6f7ff;
          color: #1890ff;
        }
        
        &.icon-order {
          background-color: #f6ffed;
          color: #52c41a;
        }
        
        &.icon-forum {
          background-color: #fff7e6;
          color: #fa8c16;
        }
        
        &.icon-like {
          background-color: #fff1f0;
          color: #f5222d;
        }
        
        &.icon-follow {
          background-color: #f9f0ff;
          color: #722ed1;
        }
        
        &.icon-default {
          background-color: #f5f5f5;
          color: #666;
        }
        
        .el-icon {
      font-size: 18px;
        }
      }
      
      .notification-content {
        flex: 1;
        cursor: pointer;
        
        .notification-title {
          font-size: 16px;
          font-weight: 500;
          color: var(--el-text-color-primary);
          margin-bottom: 5px;
          display: flex;
          align-items: center;
          
          .unread-dot {
            width: 8px;
            height: 8px;
            border-radius: 50%;
            background-color: #f56c6c;
            margin-left: 8px;
          }
        }
        
        .notification-message {
          font-size: 14px;
          color: var(--el-text-color-regular);
          margin-bottom: 10px;
          line-height: 1.5;
        }
        
        .notification-time {
          font-size: 12px;
          color: var(--el-text-color-secondary);
        }
      }
      
      .notification-actions {
        display: flex;
        gap: 8px;
        margin-left: 15px;
      }
    }
    
    // 邮件式详情框样式
    .notification-detail-card {
      margin-top: 16px;
      margin-left: 55px;
      padding: 20px;
      background-color: #fafafa;
      border-left: 3px solid #409eff;
      border-radius: 4px;
      
      .detail-header {
        margin-bottom: 16px;
        padding-bottom: 12px;
        border-bottom: 1px solid #e4e7ed;
        
        .detail-title {
          font-size: 18px;
          font-weight: 600;
          color: var(--el-text-color-primary);
          margin-bottom: 8px;
        }
        
        .detail-meta {
          display: flex;
          align-items: center;
          gap: 12px;
          
          .detail-time {
            font-size: 12px;
            color: var(--el-text-color-secondary);
          }
        }
      }
      
      .detail-body {
        .detail-text {
          font-size: 14px;
          color: var(--el-text-color-regular);
          line-height: 1.8;
          white-space: pre-wrap;
          word-break: break-word;
          margin-bottom: 16px;
        }
        
        .detail-status {
          margin-bottom: 16px;
        }
        
        .detail-actions {
          display: flex;
          gap: 12px;
        }
      }
    }
  }
  
  .pagination-container {
    margin-top: 20px;
    text-align: right;
  }
}

@media (max-width: 768px) {
  .system-notifications-page {
    .notifications-header {
      flex-direction: column;
      align-items: flex-start;
      
      .filter-actions {
        width: 100%;
        flex-wrap: wrap;
        
        .el-radio-group {
      margin-bottom: 10px;
        }
        
        .el-select {
          width: 100%;
        }
      }
      
      .batch-actions {
        width: 100%;
        display: flex;
        justify-content: flex-end;
        gap: 10px;
      }
    }
    
    .notifications-list {
      .notification-item {
        flex-direction: column;
        
        .notification-icon {
          margin-bottom: 10px;
        }
        
        .notification-content {
          width: 100%;
          margin-bottom: 15px;
        }
        
        .notification-actions {
          align-self: flex-end;
          margin-left: 0;
        }
      }
    }
  }
}
</style> 