<template>
  <div class="system-notifications-page">
    <!-- 筛选和操作区域 -->
    <div class="notifications-header">
      <div class="filter-actions">
        <el-radio-group v-model="readStatus" size="small">
          <el-radio-button label="all">全部</el-radio-button>
          <el-radio-button label="unread">未读</el-radio-button>
          <el-radio-button label="read">已读</el-radio-button>
        </el-radio-group>
        
        <el-select v-model="typeFilter" placeholder="消息类型" clearable size="small">
          <el-option label="全部类型" value=""></el-option>
          <el-option label="系统公告" value="system"></el-option>
          <el-option label="订单通知" value="order"></el-option>
          <el-option label="帖子回复" value="forum"></el-option>
          <el-option label="点赞提醒" value="like"></el-option>
          <el-option label="关注提醒" value="follow"></el-option>
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
          :class="{ 'notification-unread': !notification.isRead }"
        >
          <div class="notification-icon" :class="getNotificationIconClass(notification.type)">
            <el-icon v-if="notification.type === 'system'"><Bell /></el-icon>
            <el-icon v-if="notification.type === 'order'"><ShoppingCart /></el-icon>
            <el-icon v-if="notification.type === 'forum'"><ChatDotRound /></el-icon>
            <el-icon v-if="notification.type === 'like'"><Star /></el-icon>
            <el-icon v-if="notification.type === 'follow'"><User /></el-icon>
          </div>
          
          <div class="notification-content" @click="openNotification(notification)">
            <div class="notification-title">
              {{ notification.title }}
              <span class="unread-dot" v-if="!notification.isRead"></span>
            </div>
            <div class="notification-message">{{ notification.content }}</div>
            <div class="notification-time">{{ formatDate(notification.createTime) }}</div>
          </div>
          
          <div class="notification-actions">
            <el-tooltip 
              content="标记为已读" 
              placement="top" 
              v-if="!notification.isRead">
              <el-button 
                type="info" 
                circle 
                size="small"
                @click="markAsRead(notification.id)">
                <el-icon><Check /></el-icon>
              </el-button>
            </el-tooltip>
            
            <el-tooltip content="删除通知" placement="top">
              <el-button 
                type="danger" 
                circle 
                size="small"
                @click="deleteNotification(notification.id)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </el-tooltip>
          </div>
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

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Bell, ShoppingCart, ChatDotRound, Star, User, Check, Delete } from '@element-plus/icons-vue'

export default {
  name: 'SystemNotificationsPage',
  components: {
    Bell, ShoppingCart, ChatDotRound, Star, User, Check, Delete
  },
  setup() {
    const router = useRouter()
    
    // 分页参数
    const currentPage = ref(1)
    const pageSize = ref(10)
    const totalNotifications = ref(0)
    
    // 筛选参数
    const readStatus = ref('all')
    const typeFilter = ref('')
    
    // 通知列表数据
    const notifications = ref([])
    const loading = ref(false)
    
    // 获取通知列表
    const fetchNotifications = async () => {
      loading.value = true
      try {
        // 实际项目中这里会调用API
        // const response = await notificationAPI.getNotifications({
        //   page: currentPage.value,
        //   pageSize: pageSize.value,
        //   readStatus: readStatus.value,
        //   type: typeFilter.value
        // })
        
        /* UI-DEV-START */
        // 模拟API请求延迟
        await new Promise(resolve => setTimeout(resolve, 500))
        
        // 模拟数据
        const mockData = getMockNotifications()
        notifications.value = mockData.list
        totalNotifications.value = mockData.total
        /* UI-DEV-END */
        
      } catch (error) {
        ElMessage.error('获取通知列表失败')
        console.error(error)
      } finally {
        loading.value = false
      }
    }
    
    // 根据数据库表user_notifications结构生成模拟数据
    const getMockNotifications = () => {
      const mockNotifications = [
        {
          id: 1,
          userId: 1,
          title: '系统公告：茶文化平台全新升级',
          content: '尊敬的用户，我们的茶文化平台已完成全面升级，新增了更多功能，欢迎体验！',
          type: 'system',
          isRead: false,
          createTime: '2025-03-20 10:15:30'
        },
        {
          id: 2,
          userId: 1,
          title: '订单状态更新：您的订单已发货',
          content: '您购买的【龙井茶叶礼盒装】已发货，快递单号：SF1234567890，请注意查收。',
          type: 'order',
          related_id: 10086,
          isRead: true,
          createTime: '2025-03-19 16:42:15'
        },
        {
          id: 3,
          userId: 1,
          title: '新的帖子回复',
          content: '用户【茶艺小能手】回复了您的帖子【新手冲泡白茶总是苦涩，有什么技巧吗？】',
          type: 'forum',
          related_id: 3,
          isRead: false,
          createTime: '2025-03-18 14:28:52'
        },
        {
          id: 4,
          userId: 1,
          title: '您的帖子收到了新的点赞',
          content: '您的帖子【分享我最近喝过的安化黑茶，口感超赞！】收到了3个新的点赞。',
          type: 'like',
          related_id: 1,
          isRead: false,
          createTime: '2025-03-17 09:36:21'
        },
        {
          id: 5,
          userId: 1,
          title: '您有了新的粉丝',
          content: '用户【普洱控】关注了您，去看看Ta的主页吧！',
          type: 'follow',
          related_id: 503,
          isRead: true,
          createTime: '2025-03-16 20:15:43'
        },
        {
          id: 6,
          userId: 1,
          title: '订单状态更新：您的订单已签收',
          content: '您购买的【武夷山金骏眉红茶】已签收，如有问题请及时联系客服。',
          type: 'order',
          related_id: 10085,
          isRead: true,
          createTime: '2025-03-15 11:08:36'
        },
        {
          id: 7,
          userId: 1,
          title: '系统公告：春节期间发货安排',
          content: '尊敬的用户，春节期间（2025年2月8日至2025年2月15日）平台暂停发货，2月16日恢复正常，感谢理解。',
          type: 'system',
          isRead: true,
          createTime: '2025-02-05 08:30:00'
        }
      ]
      
      return {
        list: mockNotifications,
        total: mockNotifications.length
      }
    }
    
    // 过滤通知
    const filteredNotifications = computed(() => {
      let result = [...notifications.value]
      
      // 按已读/未读状态过滤
      if (readStatus.value === 'read') {
        result = result.filter(item => item.isRead)
      } else if (readStatus.value === 'unread') {
        result = result.filter(item => !item.isRead)
      }
      
      // 按类型过滤
      if (typeFilter.value) {
        result = result.filter(item => item.type === typeFilter.value)
      }
      
      return result
    })
    
    // 是否有未读通知
    const hasUnread = computed(() => {
      return notifications.value.some(item => !item.isRead)
    })
    
    // 标记单条通知为已读
    const markAsRead = async (id) => {
      try {
        // 实际项目中这里会调用API
        // await notificationAPI.markAsRead(id)
        
        /* UI-DEV-START */
        // 模拟API调用
        const index = notifications.value.findIndex(item => item.id === id)
        if (index !== -1) {
          notifications.value[index].isRead = true
        }
        /* UI-DEV-END */
        
        ElMessage.success('已标记为已读')
      } catch (error) {
        ElMessage.error('操作失败')
        console.error(error)
      }
    }
    
    // 标记所有通知为已读
    const markAllAsRead = async () => {
      try {
        // 实际项目中这里会调用API
        // await notificationAPI.markAllAsRead()
        
        /* UI-DEV-START */
        // 模拟API调用
        notifications.value.forEach(item => {
          item.isRead = true
        })
        /* UI-DEV-END */
        
        ElMessage.success('已全部标记为已读')
      } catch (error) {
        ElMessage.error('操作失败')
        console.error(error)
      }
    }
    
    // 删除单条通知
    const deleteNotification = async (id) => {
      try {
        // 实际项目中这里会调用API
        // await notificationAPI.deleteNotification(id)
        
        /* UI-DEV-START */
        // 模拟API调用
        notifications.value = notifications.value.filter(item => item.id !== id)
        totalNotifications.value--
        /* UI-DEV-END */
        
        ElMessage.success('通知已删除')
      } catch (error) {
        ElMessage.error('删除失败')
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
        // 实际项目中这里会调用API
        // await notificationAPI.deleteAllNotifications()
        
        /* UI-DEV-START */
        // 模拟API调用
        notifications.value = []
        totalNotifications.value = 0
        /* UI-DEV-END */
        
        ElMessage.success('已清空所有通知')
      } catch (error) {
        ElMessage.error('操作失败')
        console.error(error)
      }
    }
    
    // 打开通知
    const openNotification = async (notification) => {
      // 标记为已读
      if (!notification.isRead) {
        await markAsRead(notification.id)
      }
      
      // 根据通知类型跳转到相应页面
      switch (notification.type) {
        case 'system':
          // 系统通知一般不需要跳转
          break
        case 'order':
          if (notification.related_id) {
            router.push(`/order/detail/${notification.related_id}`)
          }
          break
        case 'forum':
          if (notification.related_id) {
            router.push(`/forum/${notification.related_id}`)
          }
          break
        case 'like':
          if (notification.related_id) {
            router.push(`/forum/${notification.related_id}`)
          }
          break
        case 'follow':
          if (notification.related_id) {
            router.push(`/profile/${notification.related_id}`)
          }
          break
      }
    }
    
    // 切换页码
    const handlePageChange = (page) => {
      currentPage.value = page
      fetchNotifications()
    }
    
    // 格式化时间
    const formatDate = (dateString) => {
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
    const getNotificationIconClass = (type) => {
      const classMap = {
        system: 'icon-system',
        order: 'icon-order',
        forum: 'icon-forum',
        like: 'icon-like',
        follow: 'icon-follow'
      }
      
      return classMap[type] || 'icon-default'
    }
    
    // 初始化
    onMounted(() => {
      fetchNotifications()
    })
    
    return {
      notifications,
      filteredNotifications,
      hasUnread,
      readStatus,
      typeFilter,
      currentPage,
      pageSize,
      totalNotifications,
      loading,
      markAsRead,
      markAllAsRead,
      deleteNotification,
      confirmDeleteAll,
      openNotification,
      handlePageChange,
      formatDate,
      getNotificationIconClass
    }
  }
}
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