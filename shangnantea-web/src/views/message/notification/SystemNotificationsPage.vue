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
          <el-option label="商家认证" value="merchant_verification"></el-option>
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
            <el-icon v-if="notification.type === 'system_announcement'"><Bell /></el-icon>
            <el-icon v-if="notification.type === 'external_link'"><Star /></el-icon>
            <el-icon v-if="notification.type === 'merchant_verification'"><User /></el-icon>
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
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { Bell, ChatDotRound, Star, User, Check, Delete } from '@element-plus/icons-vue'
import { useStore } from 'vuex'
import { showByCode, isSuccess } from '@/utils/apiMessages'
import { messagePromptMessages } from '@/utils/promptMessages'

export default {
  name: 'SystemNotificationsPage',
  components: {
    Bell, ChatDotRound, Star, User, Check, Delete
  },
  setup() {
    const router = useRouter()
    const store = useStore()
    
    // 分页参数
    const currentPage = ref(1)
    const pageSize = ref(10)
    const totalNotifications = ref(0)
    
    // 筛选参数
    const readStatus = ref('all')
    const typeFilter = ref('')
    
    // 通知列表数据（以 Vuex 为单一数据源）
    const notifications = computed(() => store.state.message.messages || [])
    const loading = computed(() => store.state.message.loading)
    
    // 获取通知列表
    const fetchNotifications = async () => {
      // 同步分页参数到 Vuex
      store.commit('message/SET_PAGINATION', {
        total: store.state.message.pagination.total,
        currentPage: currentPage.value,
        pageSize: pageSize.value
      })

      const response = await store.dispatch('message/fetchNotifications', {
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
        const response = await store.dispatch('message/markNotificationAsRead', id)
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

        const response = await store.dispatch('message/batchMarkAsRead', unreadIds)
        showByCode(response.code)
      } catch (error) {
        console.error(error)
      }
    }
    
    // 删除单条通知
    const deleteNotification = async id => {
      try {
        const response = await store.dispatch('message/deleteNotification', id)
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

        const response = await store.dispatch('message/batchDeleteNotifications', allIds)
        showByCode(response.code)
      } catch (error) {
        console.error(error)
      }
    }
    
    // 打开通知
    const openNotification = async notification => {
      // 注意：后端在 getNotificationDetail() 中会自动标记为已读，无需前端手动调用
      
      // 根据通知类型跳转到相应页面
      switch (notification.type) {
      case 'post_reply':
        // 帖子回复通知，跳转到帖子详情页
        if (notification.targetId && notification.targetType === 'post') {
          router.push(`/forum/posts/${notification.targetId}`)
        }
        break
      case 'system_announcement':
        // 系统公告一般不需要跳转
        break
      case 'external_link':
        // 外部链接通知，解析extraData中的链接
        try {
          const extraData = JSON.parse(notification.extraData || '{}')
          if (extraData.externalUrl) {
            window.open(extraData.externalUrl, '_blank')
          }
        } catch (e) {
          console.warn('解析外部链接失败:', e)
        }
        break
      case 'merchant_verification':
        // 商家认证通知，可能需要确认操作
        try {
          const extraData = JSON.parse(notification.extraData || '{}')
          if (extraData.actionType === 'confirm') {
            // 商家认证通过，需要确认通知并触发角色变更和店铺创建
            await handleMerchantCertificationConfirm(notification)
          } else {
            // 普通商家认证通知，跳转到商家中心
            router.push('/shop/my')
          }
        } catch (e) {
          console.warn('解析操作数据失败:', e)
          messagePromptMessages.showPleaseWait()
        }
        break
      }
    }
    
    // 处理商家认证确认通知
    const handleMerchantCertificationConfirm = async notification => {
      try {
        // 1. 标记通知为已读
        await store.dispatch('message/markNotificationAsRead', notification.id)
        
        // 2. 确认通知（触发后端角色变更）
        // 注意：实际项目中，角色变更应该由后端在审核通过时自动完成
        // 这里只是确认通知，刷新用户信息以获取最新角色
        await store.dispatch('user/fetchUserInfo')
        
        // 3. 检查用户角色是否已变更为商家（role === 3）
        const userInfo = store.state.user.userInfo
        if (userInfo && userInfo.role === 3) {
          // 4. 检查是否已有店铺，如果没有则自动创建
          try {
            await store.dispatch('shop/fetchMyShop')
            // 商家认证已确认
            router.push('/shop/my')
          } catch (error) {
            // 如果没有店铺，自动创建
            if (error.message && error.message.includes('获取我的店铺失败')) {
              try {
                await store.dispatch('shop/createShop', {
                  name: '我的商南茶叶店',
                  desc: '专业经营商南优质茶叶'
                })
                // 店铺已自动创建
                router.push('/shop/my')
              } catch (createError) {
                console.error('创建店铺失败:', createError)
                router.push('/shop/my')
              }
            } else {
              throw error
            }
          }
        } else {
          // 角色尚未变更，提示用户
          messagePromptMessages.showPleaseWait()
        }
      } catch (error) {
        console.error('处理商家认证确认失败:', error)
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
        external_link: 'icon-like',
        merchant_verification: 'icon-follow'
      }
      
      return classMap[type] || 'icon-default'
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