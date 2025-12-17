<template>
  <div class="post-card">
    <!-- 用户信息和发布时间 -->
    <div class="post-header">
      <div class="user-info" @click="goToUserProfile">
        <div class="avatar">
          <SafeImage :src="post.authorAvatar" type="avatar" :alt="post.authorName" style="width:100%;height:100%;object-fit:cover;" />
        </div>
        <div class="info">
          <div class="name">{{ post.authorName }}</div>
          <div class="meta">
            <span class="time">{{ formatDate(post.createTime) }}</span>
            <span class="gender" v-if="post.authorGender">
              <el-icon color="#409EFF" v-if="post.authorGender === 1"><Male /></el-icon>
              <el-icon color="#FF4949" v-else-if="post.authorGender === 2"><Female /></el-icon>
            </span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 帖子内容 -->
    <div class="post-content" @click="goToPostDetail">
      <div class="title">{{ post.title }}</div>
      <div class="content" :class="{ 'clamp-content': !showFullContent }">
        {{ post.summary || post.content }}
      </div>
      <div class="read-more" v-if="isContentOverflow && !showFullContent" @click.stop="toggleContent">
        全文
      </div>
      
      <!-- 帖子图片 -->
      <div class="post-image" v-if="post.coverImage">
        <SafeImage :src="post.coverImage" type="post" alt="帖子图片" style="width:100%;object-fit:cover;" />
      </div>
    </div>
    
    <!-- 底部操作栏 -->
    <div class="post-footer">
      <div class="post-topic" v-if="post.topicName">
        <el-tag size="small" effect="plain">{{ post.topicName }}</el-tag>
      </div>
      <div class="actions">
        <div class="action-item">
          <el-icon><View /></el-icon>
          <span>{{ post.viewCount || 0 }}</span>
        </div>
        <div class="action-item" @click.stop="handleReply">
          <el-icon><ChatDotRound /></el-icon>
          <span>{{ post.replyCount || 0 }}</span>
        </div>
        <div class="action-item" @click.stop="handleFavorite">
          <el-icon><Star v-if="!post.favorited" /><StarFilled v-else /></el-icon>
          <span>{{ post.likeCount || 0 }}</span>
        </div>
      </div>
    </div>
    
    <!-- 删除按钮（仅在显示自己的帖子时显示） -->
    <div class="delete-button" v-if="showDelete" @click.stop="handleDelete">
      <el-icon><Delete /></el-icon>
    </div>
  </div>
</template>

<script>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { View, ChatDotRound, Star, StarFilled, Male, Female, Delete } from '@element-plus/icons-vue'
import SafeImage from '@/components/common/form/SafeImage.vue'

export default {
  name: 'PostCard',
  components: {
    View,
    ChatDotRound,
    Star,
    StarFilled,
    Male,
    Female,
    Delete,
    SafeImage
  },
  props: {
    post: {
      type: Object,
      required: true
    },
    showDelete: {
      type: Boolean,
      default: false
    }
  },
  emits: ['reply', 'favorite', 'delete'],
  setup(props, { emit }) {
    const router = useRouter()
    const showFullContent = ref(false)
    
    // 判断内容是否超出两行
    const isContentOverflow = computed(() => {
      const content = props.post.summary || props.post.content || ''
      return content.length > 70 // 大约两行字的长度
    })
    
    // 切换显示全文/部分内容
    const toggleContent = () => {
      showFullContent.value = !showFullContent.value
    }
    
    // 跳转到帖子详情
    const goToPostDetail = () => {
      router.push(`/forum/${props.post.id}`)
    }
    
    // 跳转到用户主页
    const goToUserProfile = () => {
      router.push(`/profile/${props.post.authorId}`)
    }
    
    // 回复帖子
    const handleReply = () => {
      emit('reply', props.post)
      router.push(`/forum/${props.post.id}#reply-section`)
    }
    
    // 收藏帖子
    const handleFavorite = () => {
      emit('favorite', props.post)
    }
    
    // 删除帖子
    const handleDelete = () => {
      emit('delete', props.post)
    }
    
    // 格式化日期
    const formatDate = (dateString) => {
      if (!dateString) return ''
      const date = new Date(dateString)
      const now = new Date()
      const diff = now - date
      
      // 一小时内显示"刚刚"
      if (diff < 60 * 60 * 1000) {
        return '刚刚'
      }
      
      // 一天内显示"x小时前"
      if (diff < 24 * 60 * 60 * 1000) {
        const hours = Math.floor(diff / (60 * 60 * 1000))
        return `${hours}小时前`
      }
      
      // 一周内显示"x天前"
      if (diff < 7 * 24 * 60 * 60 * 1000) {
        const days = Math.floor(diff / (24 * 60 * 60 * 1000))
        return `${days}天前`
      }
      
      // 其他情况显示具体日期
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
    }
    
    return {
      showFullContent,
      isContentOverflow,
      toggleContent,
      goToPostDetail,
      goToUserProfile,
      handleReply,
      handleFavorite,
      handleDelete,
      formatDate
    }
  }
}
</script>

<style lang="scss" scoped>
.post-card {
  position: relative;
  background-color: #fff;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  transition: transform 0.2s, box-shadow 0.2s;
  
  &:hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  }
  
  .post-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;
    
    .user-info {
      display: flex;
      align-items: center;
      cursor: pointer;
      
      .avatar {
        width: 40px;
        height: 40px;
        border-radius: 50%;
        overflow: hidden;
        margin-right: 12px;
      }
      
      .info {
        .name {
          font-weight: 500;
          font-size: 14px;
          color: var(--el-text-color-primary);
          margin-bottom: 4px;
        }
        
        .meta {
          display: flex;
          align-items: center;
          font-size: 12px;
          color: var(--el-text-color-secondary);
          
          .time {
            margin-right: 8px;
          }
          
          .gender {
            display: flex;
            align-items: center;
          }
        }
      }
    }
  }
  
  .post-content {
    cursor: pointer;
    
    .title {
      font-size: 16px;
      font-weight: 500;
      margin-bottom: 8px;
      color: var(--el-text-color-primary);
    }
    
    .content {
      font-size: 14px;
      line-height: 1.6;
      color: var(--el-text-color-regular);
      margin-bottom: 12px;
      
      &.clamp-content {
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }
    }
    
    .read-more {
      font-size: 14px;
      color: var(--el-color-primary);
      cursor: pointer;
      margin-bottom: 12px;
    }
    
    .post-image {
      margin-top: 12px;
      border-radius: 4px;
      overflow: hidden;
      max-height: 160px;
    }
  }
  
  .post-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 16px;
    padding-top: 12px;
    border-top: 1px solid #f0f0f0;
    
    .post-topic {
      max-width: 40%;
    }
    
    .actions {
      display: flex;
      align-items: center;
      gap: 16px;
      
      .action-item {
        display: flex;
        align-items: center;
        font-size: 13px;
        color: var(--el-text-color-secondary);
        cursor: pointer;
        transition: color 0.2s;
        
        &:hover {
          color: var(--el-color-primary);
        }
        
        .el-icon {
          margin-right: 4px;
        }
      }
    }
  }
  
  .delete-button {
    position: absolute;
    top: 16px;
    right: 16px;
    color: var(--el-color-danger);
    cursor: pointer;
    font-size: 16px;
    z-index: 1;
  }
}

@media (max-width: 768px) {
  .post-card {
    padding: 12px;
    
    .post-header {
      margin-bottom: 10px;
      
      .user-info {
        .avatar {
          width: 36px;
          height: 36px;
        }
      }
    }
    
    .post-content {
      .title {
        font-size: 15px;
      }
    }
    
    .post-footer {
      .actions {
        gap: 12px;
      }
    }
  }
}
</style> 