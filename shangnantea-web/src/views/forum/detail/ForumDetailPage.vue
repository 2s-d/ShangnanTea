<template>
  <div class="forum-detail-page">
    <!-- 页面标题 -->
    <div class="page-header" v-if="post">
      <div class="container">
        <div class="back-button" @click="goBack">
          <el-icon><Back /></el-icon> 返回列表
        </div>
        <h1 class="page-title">{{ post.title }}</h1>
        <div class="post-meta">
          <div class="post-author">
            <span class="author-avatar">
              <SafeImage :src="post.authorAvatar" type="avatar" :alt="post.authorName" style="width:40px;height:40px;border-radius:50%;object-fit:cover;" />
            </span>
            <span class="author-name">{{ post.authorName }}</span>
          </div>
          <div class="post-info">
            <span class="post-topic" v-if="post.topicName">版块: {{ post.topicName }}</span>
            <span class="post-time">{{ formatDate(post.createTime) }}</span>
            <span class="post-views">
              <el-icon><View /></el-icon> {{ post.viewCount }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <div class="container main-content">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="8" animated />
      </div>
      
      <!-- 帖子内容 -->
      <div v-else-if="post" class="post-container">
        <div class="post-content" v-html="post.content"></div>
        <div v-if="post.cover" class="post-cover">
          <SafeImage :src="post.cover" type="post" :alt="post.title" class="cover-img" style="width:100%;object-fit:cover;" />
        </div>
        
        <div class="post-actions">
          <el-button type="primary" plain @click="likePost" :loading="likeLoading" :class="{ 'liked': liked }">
            <el-icon><StarFilled v-if="liked" /><Star v-else /></el-icon> 
            {{ liked ? '已点赞' : '点赞' }} ({{ post.likeCount || 0 }})
          </el-button>
          <el-button plain @click="scrollToReply">
            <el-icon><ChatDotRound /></el-icon> 
            回复 ({{ post.replyCount || 0 }})
          </el-button>
          <el-button plain @click="toggleFavorite" :loading="favoriteLoading" :class="{ 'favorited': favorited }">
            <el-icon><StarFilled v-if="favorited" /><Star v-else /></el-icon> 
            {{ favorited ? '已收藏' : '收藏' }} ({{ post.favoriteCount || 0 }})
          </el-button>
          <el-button plain @click="showShareDialog">
            <el-icon><Share /></el-icon> 分享
          </el-button>
        </div>
      </div>
      
      <!-- 回复区域 -->
      <div class="reply-section" id="reply-section" v-if="post">
        <div class="section-header">
          <h2 class="section-title">全部回复 ({{ post.replyCount || 0 }})</h2>
          <div class="section-actions">
            <el-dropdown trigger="click" @command="handleSortChange">
              <span class="sort-dropdown">
                {{ sortOptions[currentSort] || '默认排序' }} <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item v-for="(label, value) in sortOptions" :key="value" :command="value">
                    {{ label }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
        
        <!-- 回复列表 -->
        <div class="reply-list">
          <!-- 加载状态 -->
          <div v-if="loading && replyList.length === 0" class="reply-loading">
            <el-skeleton :rows="3" animated />
          </div>
          
          <div v-for="reply in replyList" :key="reply.id" class="reply-item">
            <div class="reply-author">
              <div class="author-avatar">
                <SafeImage :src="reply.userAvatar" type="avatar" :alt="reply.userName" style="width:40px;height:40px;border-radius:50%;object-fit:cover;" />
              </div>
              <div class="author-info">
                <div class="author-name">{{ reply.userName }}</div>
                <div class="reply-time">{{ formatDate(reply.createTime) }}</div>
              </div>
            </div>
            
            <div class="reply-content">
              <!-- 引用的回复 -->
              <div v-if="reply.parentId" class="quoted-reply">
                <div class="quoted-user">引用 {{ getReplyUserName(reply.parentId) }} 的回复：</div>
                <div class="quoted-content">{{ getReplyContent(reply.parentId) }}</div>
              </div>
              
              <!-- 回复内容 -->
              <div class="content-text">{{ reply.content }}</div>
              
              <!-- 回复操作 -->
              <div class="reply-actions">
                <span class="action-item" @click="likeReply(reply)" :class="{ 'liked': reply.isLiked }">
                  <el-icon><StarFilled v-if="reply.isLiked" /><Star v-else /></el-icon> 
                  {{ reply.isLiked ? '已赞' : '点赞' }} ({{ reply.likeCount || 0 }})
                </span>
                <span class="action-item" @click="showReplyInput(reply)">
                  <el-icon><ChatLineRound /></el-icon> 回复
                </span>
              </div>
            </div>
          </div>
          
          <!-- 分页 -->
          <div class="pagination-container" v-if="replyList.length > 0">
            <el-pagination
              v-model:current-page="pagination.currentPage"
              v-model:page-size="pagination.pageSize"
              :page-sizes="[10, 20, 30, 50]"
              layout="total, sizes, prev, pager, next, jumper"
              :total="pagination.total"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
          
          <!-- 无回复提示 -->
          <el-empty v-if="replyList.length === 0" description="暂无回复，发表第一条回复吧" />
        </div>
        
        <!-- 回复输入框 -->
        <div class="reply-form">
          <h3 class="form-title">
            <span v-if="currentReply">回复 {{ currentReply.userName }}：</span>
            <span v-else>发表回复</span>
            <span v-if="currentReply" class="cancel-reply" @click="cancelReply">取消回复</span>
          </h3>
          
          <el-form>
            <el-form-item>
              <div class="reply-input-wrapper">
                <el-input
                  ref="replyTextareaRef"
                  v-model="replyContent"
                  type="textarea"
                  :rows="4"
                  placeholder="请输入回复内容，输入@可以提及用户..."
                  maxlength="1000"
                  show-word-limit
                  @input="handleReplyInput"
                  @keydown="handleReplyKeydown"
                />
                <!-- @用户下拉列表 -->
                <div v-if="showMentionList && mentionUsers.length > 0" class="mention-dropdown">
                  <div
                    v-for="(user, index) in mentionUsers"
                    :key="user.id"
                    class="mention-item"
                    :class="{ 'active': index === mentionSelectedIndex }"
                    @click="selectMentionUser(user)"
                    @mouseenter="mentionSelectedIndex = index"
                  >
                    <el-avatar :src="(user && user.avatar) || ''" :size="24" style="margin-right: 8px;">
                      {{ (user && user.username && user.username.charAt(0)) || '?' }}
                    </el-avatar>
                    <span>{{ (user && user.username) || '未知用户' }}</span>
                  </div>
                </div>
              </div>
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="submitReply" :loading="submitting">发布回复</el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
      
      <!-- 帖子推荐 -->
      <div class="recommend-posts">
        <h2 class="section-title">相关推荐</h2>
        
        <div class="recommend-list">
          <div v-for="item in recommendList" :key="item.id" class="recommend-item" @click="viewPost(item.id)">
            <div class="item-title">{{ item.title }}</div>
            <div class="item-meta">
              <span class="item-author">{{ item.authorName }}</span>
              <span class="item-views">
                <el-icon><View /></el-icon> {{ item.viewCount }}
              </span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 帖子不存在 -->
      <div v-if="!loading && !post" class="error-container">
        <el-empty description="帖子不存在或已被删除" />
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { useUserStore } from '@/stores/user'

import { View, ChatDotRound, Star, StarFilled, Share, Back, ChatLineRound, ArrowDown } from '@element-plus/icons-vue'
import SafeImage from '@/components/common/form/SafeImage.vue'
import { showByCode } from '@/utils/apiMessages'
import { forumPromptMessages } from '@/utils/promptMessages'

export default {
  name: 'ForumDetailPage',
  components: {
    View, ChatDotRound, Star, StarFilled, Share, Back, ChatLineRound, ArrowDown, SafeImage
  },
  setup() {
    const route = useRoute()
    const router = useRouter()
    const store = useStore()
    const userStore = useUserStore()
    
    // 默认图片（生产形态：不使用 mock-images）
    const defaultAvatar = ''
    const defaultCover = ''
    
    // 获取帖子ID
    const postId = computed(() => route.params.id)
    
    // 点赞状态（从帖子数据判断）
    const liked = computed(() => {
      return post.value?.isLiked || false
    })
    
    // 收藏状态（从帖子数据判断）
    const favorited = computed(() => {
      return post.value?.isFavorited || false
    })
    
    const likeLoading = ref(false)
    const favoriteLoading = ref(false)
    
    // 从Vuex获取帖子数据
    const post = computed(() => store.state.forum.currentPost)
    const loading = computed(() => store.state.forum.loading)
    
    // 获取帖子详情
    const fetchPostDetail = async () => {
      try {
        const res = await store.dispatch('forum/fetchPostDetail', postId.value)
        showByCode(res.code)
      } catch (error) {
        console.error('获取帖子详情失败:', error)
      }
    }
    
    // 获取回复列表
    const fetchReplies = async () => {
      if (!postId.value) return
      
      try {
        const params = {
          page: pagination.currentPage,
          size: pagination.pageSize,
          sortBy: currentSort.value
        }
        await store.dispatch('forum/fetchPostReplies', { postId: postId.value, params })
        updateReplyPagination()
      } catch (error) {
        console.error('获取回复列表失败:', error)
      }
    }
    
    // 更新回复分页信息
    const updateReplyPagination = () => {
      const vuexPagination = store.state.forum.replyPagination
      pagination.currentPage = vuexPagination.current
      pagination.pageSize = vuexPagination.pageSize
      pagination.total = vuexPagination.total
    }
    
    // 页面初始化
    onMounted(async () => {
      await fetchPostDetail()
      await fetchReplies()
    })
    
    // 返回帖子列表
    const goBack = () => {
      router.back()
    }
    
    // 点赞帖子
    const likePost = async () => {
      if (!postId.value) return
      
      likeLoading.value = true
      try {
        if (post.value?.isLiked) {
          // 取消点赞：直接传递targetId和targetType
          const res = await userStore.removeLike({
            targetId: postId.value,
            targetType: 'post'
          })
          showByCode(res.code)
          // 重新加载帖子详情以更新isLiked状态
          await fetchPostDetail()
        } else {
          // 添加点赞
          const res = await userStore.addLike({
            targetId: postId.value,
            targetType: 'post'
          })
          showByCode(res.code)
          // 重新加载帖子详情以更新isLiked状态
          await fetchPostDetail()
        }
      } catch (error) {
        console.error('点赞操作失败:', error)
      } finally {
        likeLoading.value = false
      }
    }
    
    // 滚动到回复区域
    const scrollToReply = () => {
      const replySection = document.getElementById('reply-section')
      if (replySection) {
        replySection.scrollIntoView({ behavior: 'smooth' })
      }
    }
    
    // 收藏/取消收藏
    const toggleFavorite = async () => {
      if (!postId.value) return
      
      favoriteLoading.value = true
      try {
        if (post.value?.isFavorited) {
          // 取消收藏：直接传递 itemId 和 itemType
          const res = await userStore.removeFavorite({
            itemId: postId.value,
            itemType: 'post'
          })
          showByCode(res.code)
          // 重新加载帖子详情以更新isFavorited状态
          await fetchPostDetail()
        } else {
          // 添加收藏
          const res = await userStore.addFavorite({
            itemId: postId.value,
            itemType: 'post',
            targetName: post.value?.title || '',
            targetImage: post.value?.coverImage || ''
          })
          showByCode(res.code)
          // 重新加载帖子详情以更新isFavorited状态
          await fetchPostDetail()
        }
      } catch (error) {
        console.error('收藏操作失败:', error)
      } finally {
        favoriteLoading.value = false
      }
    }
    
    // 显示分享对话框
    const showShareDialog = () => {
      forumPromptMessages.showShareDeveloping()
    }
    
    // 格式化日期
    const formatDate = dateString => {
      if (!dateString) return ''
      const date = new Date(dateString)
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
    }
    
    // 排序选项
    const currentSort = ref('default')
    const sortOptions = {
      default: '默认排序',
      time: '按时间正序',
      timeDesc: '按时间倒序',
      hot: '按热度排序'
    }
    
    // 分页数据
    const pagination = reactive({
      currentPage: 1,
      pageSize: 10,
      total: 0
    })
    
    // 回复相关状态
    const replyContent = ref('')
    const currentReply = ref(null)
    const submitting = ref(false)
    const replyTextareaRef = ref(null)
    
    // @用户相关状态
    const showMentionList = ref(false)
    const mentionUsers = ref([])
    const mentionSelectedIndex = ref(0)
    const mentionStartPos = ref(-1) // @符号的位置

    // 从Vuex获取回复数据
    const replyList = computed(() => store.state.forum.postReplies)
    const recommendList = ref([])
    // 处理排序变更
    const handleSortChange = async sort => {
      currentSort.value = sort
      pagination.currentPage = 1 // 重置到第一页
      await fetchReplies()
    }
    
    // 处理分页大小变更
    const handleSizeChange = async size => {
      pagination.pageSize = size
      pagination.currentPage = 1 // 重置到第一页
      await fetchReplies()
    }
    
    // 处理页码变更
    const handleCurrentChange = async page => {
      pagination.currentPage = page
      await fetchReplies()
    }
    
    // 点赞回复
    const likeReply = async reply => {
      try {
        if (reply.isLiked) {
          // 取消点赞：直接传递targetId和targetType
          const res = await userStore.removeLike({
            targetId: reply.id,
            targetType: 'reply'
          })
          showByCode(res.code)
          // 重新加载回复列表以更新isLiked状态
          await fetchReplies()
        } else {
          // 添加点赞
          const res = await userStore.addLike({
            targetId: reply.id,
            targetType: 'reply'
          })
          showByCode(res.code)
          // 重新加载回复列表以更新isLiked状态
          await fetchReplies()
        }
      } catch (error) {
        console.error('点赞回复失败:', error)
      }
    }
    
    // 显示回复输入框
    const showReplyInput = reply => {
      currentReply.value = reply
      // 聚焦到输入框
      setTimeout(() => {
        const textarea = document.querySelector('.reply-form textarea')
        if (textarea) {
          textarea.focus()
        }
      }, 100)
    }
    
    // 取消回复
    const cancelReply = () => {
      currentReply.value = null
    }
    
    // 处理回复输入
    const handleReplyInput = value => {
      // 检测@符号
      if (!replyTextareaRef.value || !replyTextareaRef.value.textarea) return
      
      const textarea = replyTextareaRef.value.textarea
      const cursorPos = textarea.selectionStart || value.length
      const textBeforeCursor = value.substring(0, cursorPos)
      const lastAtIndex = textBeforeCursor.lastIndexOf('@')
      
      if (lastAtIndex !== -1) {
        // 检查@后面是否有空格或其他分隔符
        const textAfterAt = textBeforeCursor.substring(lastAtIndex + 1)
        if (!textAfterAt.includes(' ') && !textAfterAt.includes('\n')) {
          // 正在输入用户名
          const searchKeyword = textAfterAt.toLowerCase()
          mentionStartPos.value = lastAtIndex
          // 从回复列表中搜索用户（简单实现：从当前回复列表的用户中搜索）
          const allUsers = new Map()
          replyList.value.forEach(reply => {
            if (!allUsers.has(reply.userId)) {
              allUsers.set(reply.userId, {
                id: reply.userId,
                username: reply.userName,
                avatar: reply.userAvatar
              })
            }
          })
          // 添加帖子作者
          if (post.value?.authorId) {
            allUsers.set(post.value.authorId, {
              id: post.value.authorId,
              username: post.value.authorName,
              avatar: post.value.authorAvatar
            })
          }
          
          mentionUsers.value = Array.from(allUsers.values())
            .filter(user => user.username.toLowerCase().includes(searchKeyword))
            .slice(0, 5) // 最多显示5个
          
          if (mentionUsers.value.length > 0) {
            showMentionList.value = true
            mentionSelectedIndex.value = 0
          } else {
            showMentionList.value = false
          }
          return
        }
      }
      
      // 没有@或@后面有空格，隐藏下拉列表
      showMentionList.value = false
    }
    
    // 处理键盘事件
    const handleReplyKeydown = event => {
      if (showMentionList.value && mentionUsers.value.length > 0) {
        if (event.key === 'ArrowDown') {
          event.preventDefault()
          mentionSelectedIndex.value = Math.min(mentionSelectedIndex.value + 1, mentionUsers.value.length - 1)
        } else if (event.key === 'ArrowUp') {
          event.preventDefault()
          mentionSelectedIndex.value = Math.max(mentionSelectedIndex.value - 1, 0)
        } else if (event.key === 'Enter' && !event.shiftKey) {
          event.preventDefault()
          if (mentionUsers.value[mentionSelectedIndex.value]) {
            selectMentionUser(mentionUsers.value[mentionSelectedIndex.value])
          }
        } else if (event.key === 'Escape') {
          showMentionList.value = false
        }
      }
    }
    
    // 选择@用户
    const selectMentionUser = user => {
      if (mentionStartPos.value === -1) return
      
      const textBefore = replyContent.value.substring(0, mentionStartPos.value)
      const textAfter = replyContent.value.substring(mentionStartPos.value)
      const textAfterAt = textAfter.substring(1) // 去掉@符号
      const spaceIndex = textAfterAt.indexOf(' ')
      const newlineIndex = textAfterAt.indexOf('\n')
      let endIndex = textAfterAt.length
      
      if (spaceIndex !== -1 && newlineIndex !== -1) {
        endIndex = Math.min(spaceIndex, newlineIndex)
      } else if (spaceIndex !== -1) {
        endIndex = spaceIndex
      } else if (newlineIndex !== -1) {
        endIndex = newlineIndex
      }
      
      const replaceText = textAfter.substring(0, endIndex + 1)
      const remainingText = textAfter.substring(endIndex + 1)
      
      replyContent.value = textBefore + '@' + user.username + ' ' + remainingText
      showMentionList.value = false
      mentionStartPos.value = -1
      
      // 聚焦到输入框
      setTimeout(() => {
        const textarea = replyTextareaRef.value?.textarea
        if (textarea) {
          const newPos = textBefore.length + user.username.length + 2 // @ + 用户名 + 空格
          textarea.setSelectionRange(newPos, newPos)
          textarea.focus()
        }
      }, 0)
    }
    
    // 提交回复
    const submitReply = async () => {
      if (!replyContent.value.trim()) {
        forumPromptMessages.showCommentRequired()
        return
      }
      
      submitting.value = true
      try {
        // 解析@的用户ID列表
        const mentionedUserIds = []
        const mentionRegex = /@(\w+)/g
        let match
        while ((match = mentionRegex.exec(replyContent.value)) !== null) {
          // 从回复列表和帖子作者中查找用户ID
          const username = match[1]
          const user = replyList.value.find(r => r.userName === username) ||
                      (post.value?.authorName === username ? {
                        userId: post.value.authorId,
                        userName: post.value.authorName
                      } : null)
          if (user) {
            mentionedUserIds.push(user.userId || user.id)
          }
        }
        
        const replyData = {
          content: replyContent.value.trim(),
          parentId: currentReply.value ? currentReply.value.id : null,
          mentionedUserIds: mentionedUserIds.length > 0 ? mentionedUserIds : undefined
        }
        
        const res = await store.dispatch('forum/createReply', { postId: postId.value, data: replyData })
        showByCode(res.code)
        
        // 清空输入框和当前回复
        replyContent.value = ''
        currentReply.value = null
        showMentionList.value = false
        
        // 刷新回复列表
        await fetchReplies()
      } catch (error) {
        console.error('创建回复失败:', error)
      } finally {
        submitting.value = false
      }
    }
    
    // 查看帖子详情
    const viewPost = postId => {
      router.push(`/forum/${postId}`)
    }
    
    // 获取回复用户名
    const getReplyUserName = replyId => {
      const reply = replyList.value.find(item => item.id === replyId)
      return reply ? reply.userName : '未知用户'
    }
    
    // 获取回复内容
    const getReplyContent = replyId => {
      const reply = replyList.value.find(item => item.id === replyId)
      if (!reply) return '该回复已被删除'
      
      // 截断过长的内容
      const content = reply.content
      return content.length > 50 ? content.substring(0, 50) + '...' : content
    }
    
    return {
      post,
      loading,
      liked,
      favorited,
      likeLoading,
      favoriteLoading,
      defaultAvatar,
      defaultCover,
      goBack,
      likePost,
      scrollToReply,
      toggleFavorite,
      showShareDialog,
      formatDate,
      fetchPostDetail,
      View,
      ChatDotRound,
      Star,
      StarFilled,
      Share,
      Back,
      replyList,
      recommendList,
      currentSort,
      sortOptions,
      pagination,
      replyContent,
      currentReply,
      submitting,
      replyTextareaRef,
      showMentionList,
      mentionUsers,
      mentionSelectedIndex,
      handleReplyInput,
      handleReplyKeydown,
      selectMentionUser,
      handleSortChange,
      handleSizeChange,
      handleCurrentChange,
      likeReply,
      showReplyInput,
      cancelReply,
      submitReply,
      viewPost,
      getReplyUserName,
      getReplyContent,
      ChatLineRound,
      ArrowDown
    }
  }
}
</script>

<style lang="scss" scoped>
.forum-detail-page {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 15px;
}

.page-header {
  background-color: #fff;
  padding: 30px 0;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  
  .back-button {
    display: inline-flex;
    align-items: center;
    color: var(--el-color-primary);
    cursor: pointer;
    margin-bottom: 15px;
    
    .el-icon {
      margin-right: 4px;
    }
  }
  
  .page-title {
    font-size: 24px;
    font-weight: 600;
    margin: 0 0 20px;
    color: var(--el-text-color-primary);
  }
  
  .post-meta {
    display: flex;
    align-items: center;
    justify-content: space-between;
    
    .post-author {
      display: flex;
      align-items: center;
      
      .author-avatar {
        width: 36px;
        height: 36px;
        border-radius: 50%;
        overflow: hidden;
        margin-right: 10px;
        
        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
      }
      
      .author-name {
        font-weight: 500;
        color: var(--el-text-color-primary);
      }
    }
    
    .post-info {
      display: flex;
      align-items: center;
      gap: 15px;
      color: var(--el-text-color-secondary);
      font-size: 14px;
      
      .post-views {
        display: flex;
        align-items: center;
        
        .el-icon {
          margin-right: 4px;
        }
      }
    }
  }
}

.main-content {
  margin-bottom: 40px;
}

.loading-container, .error-container {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  padding: 30px;
  margin-bottom: 20px;
}

.post-container {
  background-color: #fff;
    border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  padding: 30px;
  margin-bottom: 20px;
  
  .post-content {
    font-size: 16px;
    line-height: 1.8;
    color: var(--el-text-color-primary);
    
    p {
      margin-bottom: 1em;
    }
    
    h3 {
      font-size: 18px;
      font-weight: 500;
      margin: 1.5em 0 0.8em;
    }
  }
  
  .post-actions {
    margin-top: 30px;
    padding-top: 20px;
    border-top: 1px solid #eee;
    display: flex;
    gap: 15px;
    
    .el-button.liked {
      color: var(--el-color-primary);
      border-color: var(--el-color-primary);
    }
    
    .el-button.favorited {
      color: var(--el-color-warning);
      border-color: var(--el-color-warning);
    }
  }
}

.reply-section {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  padding: 30px;
  margin-bottom: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  
  .section-title {
    font-size: 18px;
    font-weight: 500;
    margin: 0;
    color: var(--el-text-color-primary);
  }
  
  .section-actions {
    .sort-dropdown {
      cursor: pointer;
      display: inline-flex;
      align-items: center;
      color: var(--el-text-color-regular);
      
      .el-icon {
        margin-left: 4px;
      }
    }
  }
}

.reply-list {
  margin-bottom: 30px;
  
  .reply-loading {
    padding: 20px 0;
  }
  
  .reply-item {
    display: flex;
    padding: 20px 0;
    border-bottom: 1px solid #f0f0f0;
    
    &:last-child {
      border-bottom: none;
    }
    
    .reply-author {
      width: 80px;
      flex-shrink: 0;
      
      .author-avatar {
        width: 50px;
        height: 50px;
        border-radius: 50%;
        overflow: hidden;
        margin-bottom: 8px;
        
        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
      }
      
      .author-info {
        overflow: hidden;
        
        .author-name {
          font-size: 14px;
          font-weight: 500;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
          margin-bottom: 4px;
      color: var(--el-text-color-primary);
        }
        
        .reply-time {
          font-size: 12px;
          color: var(--el-text-color-secondary);
        }
      }
    }
    
    .reply-content {
      flex: 1;
      margin-left: 15px;
      
      .quoted-reply {
        background-color: #f5f7fa;
        padding: 10px 15px;
        border-radius: 4px;
        margin-bottom: 12px;
        
        .quoted-user {
          font-size: 13px;
          font-weight: 500;
          color: var(--el-color-primary);
          margin-bottom: 5px;
        }
        
        .quoted-content {
          font-size: 13px;
          color: var(--el-text-color-secondary);
        }
      }
      
      .content-text {
        font-size: 14px;
        line-height: 1.6;
        color: var(--el-text-color-primary);
        word-break: break-all;
      }
      
      .reply-actions {
        margin-top: 15px;
        display: flex;
        gap: 20px;
        
        .action-item {
          display: inline-flex;
          align-items: center;
          font-size: 13px;
          color: var(--el-text-color-secondary);
          cursor: pointer;
          
          &:hover {
            color: var(--el-color-primary);
          }
          
          &.liked {
            color: var(--el-color-primary);
          }
          
          .el-icon {
            margin-right: 4px;
          }
        }
      }
    }
  }
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.reply-form {
  padding-top: 20px;
  border-top: 1px solid #eee;
  
  .form-title {
    font-size: 16px;
    font-weight: 500;
    margin-bottom: 15px;
    color: var(--el-text-color-primary);
    
    .cancel-reply {
      font-size: 13px;
      font-weight: normal;
      color: var(--el-color-primary);
      cursor: pointer;
      margin-left: 10px;
    }
  }
  
  .reply-input-wrapper {
    position: relative;
    
    .mention-dropdown {
      position: absolute;
      bottom: 100%;
      left: 0;
      right: 0;
      background: #fff;
      border: 1px solid var(--el-border-color);
      border-radius: 4px;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
      max-height: 200px;
      overflow-y: auto;
      z-index: 1000;
      margin-bottom: 5px;
      
      .mention-item {
        display: flex;
        align-items: center;
        padding: 8px 12px;
        cursor: pointer;
        transition: background-color 0.2s;
        
        &:hover,
        &.active {
          background-color: var(--el-color-primary-light-9);
        }
        
        span {
          font-size: 14px;
        }
      }
    }
  }
}

.recommend-posts {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  padding: 30px;
  
  .section-title {
    font-size: 18px;
    font-weight: 500;
    margin: 0 0 20px;
    color: var(--el-text-color-primary);
  }
  
  .recommend-list {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
    
    .recommend-item {
      cursor: pointer;
      padding: 15px;
      border-radius: 4px;
      transition: background-color 0.2s;
      
      &:hover {
        background-color: #f5f7fa;
      }
      
      .item-title {
        font-size: 15px;
        font-weight: 500;
        margin-bottom: 8px;
        color: var(--el-text-color-primary);
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
      
      .item-meta {
        display: flex;
        justify-content: space-between;
        font-size: 13px;
      color: var(--el-text-color-secondary);
        
        .item-views {
          display: flex;
          align-items: center;
          
          .el-icon {
            margin-right: 4px;
          }
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .page-header {
    padding: 20px 0;
    
    .page-title {
      font-size: 20px;
    }
    
    .post-meta {
      flex-direction: column;
      align-items: flex-start;
      
      .post-author {
        margin-bottom: 10px;
      }
    }
  }
  
  .post-container {
    padding: 20px 15px;
    
    .post-actions {
      flex-wrap: wrap;
    }
  }
  
  .reply-section, .recommend-posts {
    padding: 20px 15px;
  }
  
  .reply-item {
    flex-direction: column;
    
    .reply-author {
      width: 100%;
      display: flex;
      align-items: center;
      margin-bottom: 15px;
      
      .author-avatar {
        width: 40px;
        height: 40px;
        margin-bottom: 0;
        margin-right: 15px;
      }
    }
    
    .reply-content {
      margin-left: 0;
    }
  }
  
  .recommend-list {
    grid-template-columns: 1fr !important;
  }
}
</style> 