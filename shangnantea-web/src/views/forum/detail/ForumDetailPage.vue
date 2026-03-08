<template>
  <div class="forum-detail-page">
    <!-- 页面标题 -->
    <div class="page-header" v-if="post">
      <div class="container">
        <div class="back-button" @click="goBack">
          <el-icon><Back></Back></el-icon> 返回列表
        </div>
        <h1 class="page-title">{{ post.title }}</h1>
        <div class="post-meta">
          <div class="post-author">
            <span class="author-avatar">
              <SafeImage :src="post.userAvatar" type="avatar" :alt="post.userName" style="width:40px;height:40px;border-radius:50%;object-fit:cover;" />
            </span>
            <span class="author-name">{{ post.userName }}</span>
          </div>
          <div class="post-info">
            <span class="post-topic" v-if="post.topicName">版块: {{ post.topicName }}</span>
            <span class="post-time">{{ formatDate(post.createTime) }}</span>
            <span class="post-views">
              <el-icon><View></View></el-icon> {{ post.viewCount }}
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
        
        <!-- 仅在帖子为已发布状态时允许互动操作 -->
        <div class="post-actions" v-if="canInteract">
          <el-button type="primary" plain @click="likePost" :loading="likeLoading" :class="{ 'liked': liked }">
            <el-icon class="like-icon">
              <svg v-if="liked" viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg" width="16" height="16">
                <path fill="currentColor" d="M885.9 533.7c16.8-22.2 26.1-49.4 26.1-77.7 0-44.9-25.1-87.4-65.5-111.1a67.67 67.67 0 0 0-34.3-9.3H572.4l6-122.9c1.4-24.9-9.1-49.8-26.5-66.7a82.48 82.48 0 0 0-55.9-24.1H184v72h312.1c4.9 0 9.5 2.2 12.6 6.1l112 141.7a12 12 0 0 1-1.7 16.9l-292.2 215a12.03 12.03 0 0 0-2.7 16.6c12.7 18.7 32.9 30.4 55.2 30.4h258.6c16.8 0 33.1 5 46.8 14.4a99.2 99.2 0 0 1 41.6 50.5c1.9 3.7 3.8 7.4 5.5 11.2 10.3 22.7 15.2 47.6 14.1 73.2-.9 25.5-6.9 50.4-16.9 73.5a178.04 178.04 0 0 1-47.2 63.4 181.24 181.24 0 0 1-78.4 38.4l-8.2 1.4h-.1c-8.1 1.3-16.3 2-24.5 2H136c-4.4 0-8-3.6-8-8v-72c0-4.4 3.6-8 8-8h251.1l11.6-1.9c12.4-2 24.3-5.1 35.5-9.3 40.7-15.1 76.3-42.4 101.5-78.3 7.5-10.9 14.3-22.4 20.1-34.5 7.1-14.8 12.2-30.2 15.3-46.1l6-122.8h-76.6c-4.4 0-8-3.6-8-8v-72c0-4.4 3.6-8 8-8h407.2c19.3 0 38.1-5.1 54.8-14.8 15.8-9.1 29.6-21.2 40.7-35.8z"/>
              </svg>
              <svg v-else viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg" width="16" height="16">
                <path fill="currentColor" d="M885.9 533.7c16.8-22.2 26.1-49.4 26.1-77.7 0-44.9-25.1-87.4-65.5-111.1a67.67 67.67 0 0 0-34.3-9.3H572.4l6-122.9c1.4-24.9-9.1-49.8-26.5-66.7a82.48 82.48 0 0 0-55.9-24.1H184v72h312.1c4.9 0 9.5 2.2 12.6 6.1l112 141.7a12 12 0 0 1-1.7 16.9l-292.2 215a12.03 12.03 0 0 0-2.7 16.6c12.7 18.7 32.9 30.4 55.2 30.4h258.6c16.8 0 33.1 5 46.8 14.4a99.2 99.2 0 0 1 41.6 50.5c1.9 3.7 3.8 7.4 5.5 11.2 10.3 22.7 15.2 47.6 14.1 73.2-.9 25.5-6.9 50.4-16.9 73.5a178.04 178.04 0 0 1-47.2 63.4 181.24 181.24 0 0 1-78.4 38.4l-8.2 1.4h-.1c-8.1 1.3-16.3 2-24.5 2H136c-4.4 0-8-3.6-8-8v-72c0-4.4 3.6-8 8-8h251.1l11.6-1.9c12.4-2 24.3-5.1 35.5-9.3 40.7-15.1 76.3-42.4 101.5-78.3 7.5-10.9 14.3-22.4 20.1-34.5 7.1-14.8 12.2-30.2 15.3-46.1l6-122.8h-76.6c-4.4 0-8-3.6-8-8v-72c0-4.4 3.6-8 8-8h407.2c19.3 0 38.1-5.1 54.8-14.8 15.8-9.1 29.6-21.2 40.7-35.8z" opacity="0.5"/>
              </svg>
            </el-icon>
            {{ liked ? '已点赞' : '点赞' }} ({{ post.likeCount || 0 }})
          </el-button>
          <el-button plain @click="scrollToReply">
            <el-icon><ChatDotRound></ChatDotRound></el-icon>
            回复 ({{ pagination.total !== undefined && pagination.total !== null ? pagination.total : (post.replyCount || 0) }})
          </el-button>
          <el-button plain @click="toggleFavorite" :loading="favoriteLoading" :class="{ 'favorited': favorited }">
            <el-icon>
              <StarFilled v-if="favorited" />
              <Star v-else />
            </el-icon>
            {{ favorited ? '已收藏' : '收藏' }} ({{ post.favoriteCount || 0 }})
          </el-button>
          <el-button plain @click="showShareDialog">
            <el-icon><Share></Share></el-icon> 分享
          </el-button>
        </div>
      </div>
      
      <!-- 回复区域：仅已发布帖子允许回复 -->
      <div class="reply-section" id="reply-section" v-if="post && canInteract">
        <div class="section-header">
          <h2 class="section-title">全部回复 ({{ pagination.total !== undefined && pagination.total !== null ? pagination.total : (post.replyCount || 0) }})</h2>
          <div class="section-actions">
            <el-select
              v-model="currentSort"
              size="small"
              style="width: 140px"
              @change="handleSortChange"
            >
              <el-option
                v-for="(label, value) in sortOptions"
                :key="value"
                :label="label"
                :value="value"
              />
            </el-select>
          </div>
        </div>
        
        <!-- 回复列表 -->
        <div class="reply-list">
          <!-- 加载状态 -->
          <div v-if="loading && replyList.length === 0" class="reply-loading">
            <el-skeleton :rows="3" animated />
          </div>
          
          <div v-for="reply in replyList" :key="reply.id" class="reply-item">
            <!-- 回复头部：头像、昵称/用户名、时间在同一行 -->
            <div class="reply-header">
              <div class="author-avatar">
                <SafeImage :src="reply.avatar" type="avatar" :alt="getDisplayName(reply)" style="width:40px;height:40px;border-radius:50%;object-fit:cover;" />
              </div>
              <div class="author-name">{{ getDisplayName(reply) }}</div>
              <div class="reply-time">{{ formatDate(reply.createTime) }}</div>
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
                  <el-icon class="like-icon">
                    <svg v-if="reply.isLiked" viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg" width="16" height="16">
                      <path fill="currentColor" d="M885.9 533.7c16.8-22.2 26.1-49.4 26.1-77.7 0-44.9-25.1-87.4-65.5-111.1a67.67 67.67 0 0 0-34.3-9.3H572.4l6-122.9c1.4-24.9-9.1-49.8-26.5-66.7a82.48 82.48 0 0 0-55.9-24.1H184v72h312.1c4.9 0 9.5 2.2 12.6 6.1l112 141.7a12 12 0 0 1-1.7 16.9l-292.2 215a12.03 12.03 0 0 0-2.7 16.6c12.7 18.7 32.9 30.4 55.2 30.4h258.6c16.8 0 33.1 5 46.8 14.4a99.2 99.2 0 0 1 41.6 50.5c1.9 3.7 3.8 7.4 5.5 11.2 10.3 22.7 15.2 47.6 14.1 73.2-.9 25.5-6.9 50.4-16.9 73.5a178.04 178.04 0 0 1-47.2 63.4 181.24 181.24 0 0 1-78.4 38.4l-8.2 1.4h-.1c-8.1 1.3-16.3 2-24.5 2H136c-4.4 0-8-3.6-8-8v-72c0-4.4 3.6-8 8-8h251.1l11.6-1.9c12.4-2 24.3-5.1 35.5-9.3 40.7-15.1 76.3-42.4 101.5-78.3 7.5-10.9 14.3-22.4 20.1-34.5 7.1-14.8 12.2-30.2 15.3-46.1l6-122.8h-76.6c-4.4 0-8-3.6-8-8v-72c0-4.4 3.6-8 8-8h407.2c19.3 0 38.1-5.1 54.8-14.8 15.8-9.1 29.6-21.2 40.7-35.8z"/>
                    </svg>
                    <svg v-else viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg" width="16" height="16">
                      <path fill="currentColor" d="M885.9 533.7c16.8-22.2 26.1-49.4 26.1-77.7 0-44.9-25.1-87.4-65.5-111.1a67.67 67.67 0 0 0-34.3-9.3H572.4l6-122.9c1.4-24.9-9.1-49.8-26.5-66.7a82.48 82.48 0 0 0-55.9-24.1H184v72h312.1c4.9 0 9.5 2.2 12.6 6.1l112 141.7a12 12 0 0 1-1.7 16.9l-292.2 215a12.03 12.03 0 0 0-2.7 16.6c12.7 18.7 32.9 30.4 55.2 30.4h258.6c16.8 0 33.1 5 46.8 14.4a99.2 99.2 0 0 1 41.6 50.5c1.9 3.7 3.8 7.4 5.5 11.2 10.3 22.7 15.2 47.6 14.1 73.2-.9 25.5-6.9 50.4-16.9 73.5a178.04 178.04 0 0 1-47.2 63.4 181.24 181.24 0 0 1-78.4 38.4l-8.2 1.4h-.1c-8.1 1.3-16.3 2-24.5 2H136c-4.4 0-8-3.6-8-8v-72c0-4.4 3.6-8 8-8h251.1l11.6-1.9c12.4-2 24.3-5.1 35.5-9.3 40.7-15.1 76.3-42.4 101.5-78.3 7.5-10.9 14.3-22.4 20.1-34.5 7.1-14.8 12.2-30.2 15.3-46.1l6-122.8h-76.6c-4.4 0-8-3.6-8-8v-72c0-4.4 3.6-8 8-8h407.2c19.3 0 38.1-5.1 54.8-14.8 15.8-9.1 29.6-21.2 40.7-35.8z" opacity="0.5"/>
                    </svg>
                  </el-icon>
                  {{ reply.isLiked ? '已赞' : '点赞' }} ({{ reply.likeCount || 0 }})
                </span>
                <span class="action-item" @click="showReplyInput(reply)">
                  <el-icon><ChatLineRound></ChatLineRound></el-icon> 回复
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
              <span class="item-author">{{ item.userName || item.authorName }}</span>
              <span class="item-views">
                <el-icon><View></View></el-icon> {{ item.viewCount }}
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

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useForumStore } from '@/stores/forum'
import { useUserStore } from '@/stores/user'

import { View, ChatDotRound, Star, StarFilled, Share, Back, ChatLineRound, ArrowDown } from '@element-plus/icons-vue'
import SafeImage from '@/components/common/form/SafeImage.vue'
import { showByCode } from '@/utils/apiMessages'
import { forumPromptMessages } from '@/utils/promptMessages'

const route = useRoute()
const router = useRouter()
const forumStore = useForumStore()
const userStore = useUserStore()
    
// 默认图片（生产形态：不使用 mock-images）
const defaultAvatar = ''
const defaultCover = ''

// 获取帖子ID
const postId = computed(() => route.params.id)

// 从Pinia获取收藏列表
const favoriteList = computed(() => userStore.favoriteList || [])

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

// 从Pinia获取帖子数据
const post = computed(() => forumStore.currentPost)
const loading = computed(() => forumStore.loading)
// 是否允许互动（仅已发布帖子）
const canInteract = computed(() => post.value && post.value.status === 1)
    
// 获取帖子详情
const fetchPostDetail = async () => {
  try {
    const res = await forumStore.fetchPostDetail(postId.value)
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
    const res = await forumStore.fetchPostReplies(postId.value, params)
    // 直接从API响应获取total并设置
    if (res?.data?.total !== undefined && res?.data?.total !== null) {
      pagination.total = res.data.total
    }
    // 同步其他分页信息
    const piniaPagination = forumStore.replyPagination
    pagination.currentPage = piniaPagination.current || 1
    pagination.pageSize = piniaPagination.pageSize || 10
  } catch (error) {
    console.error('获取回复列表失败:', error)
  }
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
    
// 点赞帖子（再点一次取消）
const likePost = async () => {
  if (!postId.value || !post.value || likeLoading.value) return
  
  likeLoading.value = true
  try {
    if (post.value.isLiked) {
      // 已点赞 -> 调用取消点赞接口
      const res = await userStore.removeLike({
        targetId: postId.value,
        targetType: 'post'
      })
      showByCode(res.code)
      // 重新获取帖子详情，更新点赞状态和数量
      await fetchPostDetail()
    } else {
      // 未点赞 -> 调用点赞接口
      const res = await userStore.addLike({
        targetId: postId.value,
        targetType: 'post'
      })
      showByCode(res.code)
      // 重新获取帖子详情，更新点赞状态和数量
      await fetchPostDetail()
    }
  } catch (error) {
    console.error('点赞操作失败:', error)
    // 出错时也重新获取数据，确保状态一致
    await fetchPostDetail()
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

// 从Pinia获取回复数据
const replyList = computed(() => forumStore.postReplies)
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

// 点赞回复（再点一次取消）
const likeReply = async reply => {
  if (!reply) return
  
  // 防止快速连续点击
  if (reply._liking) return
  reply._liking = true
  
  try {
    if (reply.isLiked) {
      // 已点赞 -> 调用取消点赞接口
      const res = await userStore.removeLike({
        targetId: reply.id,
        targetType: 'reply'
      })
      showByCode(res.code)
      // 重新获取回复列表，更新点赞状态和数量
      await fetchReplies()
    } else {
      // 未点赞 -> 调用点赞接口
      const res = await userStore.addLike({
        targetId: reply.id,
        targetType: 'reply'
      })
      showByCode(res.code)
      // 重新获取回复列表，更新点赞状态和数量
      await fetchReplies()
    }
  } catch (error) {
    console.error('点赞回复失败:', error)
    // 出错时也重新获取数据，确保状态一致
    await fetchReplies()
  } finally {
    reply._liking = false
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
            username: reply.username,
            avatar: reply.avatar
          })
        }
      })
      // 添加帖子作者
      if (post.value?.userId) {
        allUsers.set(post.value.userId, {
          id: post.value.userId,
          username: post.value.userName,
          avatar: post.value.userAvatar
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
      const user = replyList.value.find(r => r.username === username) ||
                  (post.value?.userName === username ? {
                    userId: post.value.userId,
                    userName: post.value.userName
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
    
    const res = await forumStore.createReply(postId.value, replyData)
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
  return reply ? reply.username : '未知用户'
}

// 获取回复内容
const getReplyContent = replyId => {
  const reply = replyList.value.find(item => item.id === replyId)
  if (!reply) return '该回复已被删除'
  
  // 截断过长的内容
  const content = reply.content
  return content.length > 50 ? content.substring(0, 50) + '...' : content
}

</script>

<style lang="scss" scoped>
.forum-detail-page {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.container {
  width: 85%;
  max-width: 1920px;
  margin: 0 auto;
  padding: 0;
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
    
    .like-icon {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      
      svg {
        width: 16px;
        height: 16px;
        vertical-align: middle;
      }
    }
    
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
            
            &.like-icon {
              display: inline-flex;
              align-items: center;
              justify-content: center;
              
              svg {
                width: 14px;
                height: 14px;
                vertical-align: middle;
              }
            }
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