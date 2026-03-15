<template>
  <div class="published-content-page">
    <div class="container">
  
      <!-- 
      需要实现的功能:
      1. 内容分类标签页：
         - "论坛帖子"标签页
         - "茶叶评价"标签页
      
      2. 发布的论坛帖子：
         - 帖子标题、发布时间
         - 帖子内容摘要
         - 帖子状态（点赞数、回复数、浏览数）
         - 编辑和删除功能
         - 点击可跳转至帖子详情
      
      3. 发布的茶叶评价：
         - 评价的茶叶信息（图片、名称）
         - 评价内容、评分
         - 评价时间
         - 评价状态（商家是否回复）
         - 删除功能
         - 点击可跳转至茶叶详情
      
      4. 管理功能：
         - 按发布时间排序
         - 按分类筛选
         - 按状态筛选（置顶、精华等）
         - 搜索发布内容
      -->
      
      <div class="content-tabs" v-loading="loading">
        <!-- 顶部横栏（同“关注”页样式）：统计放在横栏上，避免下方重复标题 -->
        <div class="filter-bar">
          <div class="filter-tabs">
            <el-radio-group v-model="activeTab" @change="handleTabChange">
              <el-radio-button value="posts">论坛帖子 ({{ posts.length }})</el-radio-button>
              <el-radio-button value="reviews">茶叶评价 ({{ reviews.length }})</el-radio-button>
            </el-radio-group>
          </div>
          <div class="filter-actions">
            <el-select
              v-if="activeTab === 'posts'"
              v-model="sortOption"
              placeholder="排序方式"
              size="small"
              style="width: 120px"
              @change="handleSortChange"
            >
                      <el-option label="最新发布" value="newest"></el-option>
                      <el-option label="最多浏览" value="mostViewed"></el-option>
                      <el-option label="最多回复" value="mostReplied"></el-option>
                    </el-select>
                  </div>
                </div>
                
        <!-- 论坛帖子 -->
        <div v-if="activeTab === 'posts'" class="posts-list">
          <el-empty v-if="posts.length === 0" description="暂无发布内容" />

          <div v-else class="post-items">
                  <div v-for="post in sortedPosts" :key="post.id" class="post-item" @click="viewPostDetail(post.id)">
                    <div class="post-content">
                      <div class="post-title">{{ post.title }}</div>
                      <div class="post-summary">{{ post.summary }}</div>
                      <div class="post-meta">
                        <span class="post-time">
                          <el-icon><Timer /></el-icon> {{ formatDate(post.createTime) }}
                        </span>
                        <span class="post-views">
                          <el-icon><View /></el-icon> {{ post.viewCount }}
                        </span>
                        <span class="post-replies">
                          <el-icon><ChatDotRound /></el-icon> {{ post.replyCount }}
                        </span>
                        <span class="post-likes">
                          <el-icon><Star /></el-icon> {{ post.likeCount }}
                        </span>
                      </div>
                    </div>
                    
                    <div v-if="isSelf" class="post-actions">
                <el-button type="primary" size="small" @click.stop="editPost(post)">
                        <el-icon><Edit /></el-icon> 编辑
                      </el-button>
                      <el-button type="danger" size="small" @click.stop="deletePost(post.id)">
                        <el-icon><Delete /></el-icon> 删除
                      </el-button>
                    </div>
                  </div>
                </div>
              </div>
          
        <!-- 茶叶评价 -->
        <div v-else class="reviews-list">
              <el-empty v-if="reviews.length === 0" description="暂无评价记录" />
              
          <div v-else class="review-items">
                  <div v-for="review in sortedReviews" :key="review.id" class="review-item" @click="viewTeaDetailWithReview(review.teaId, review.id)">
                    <div class="review-content">
                      <div class="review-rating">
                        <el-rate v-model="review.rating" disabled show-score text-color="#ff9900" />
                      </div>
                      <div class="review-text">{{ review.content }}</div>
                      
                      <!-- 评价图片（如果有） -->
                      <div v-if="review.images && review.images.length > 0" class="review-images-wrapper">
                        <div class="review-images">
                          <div v-for="(img, index) in review.images" :key="index" class="review-image-item">
                            <SafeImage :src="img" type="tea" :alt="`评价图片${index + 1}`" class="review-image" />
                          </div>
                        </div>
                        <div v-if="isSelf" class="review-actions" @click.stop>
                          <el-button type="danger" size="small" @click="deleteReview(review.id)">
                            <el-icon><Delete /></el-icon> 删除
                          </el-button>
                        </div>
                      </div>
                      
                      <div class="review-meta">
                        <span class="review-time">
                          <el-icon><Timer /></el-icon> {{ formatDate(review.createTime) }}
                        </span>
                        <span class="shop-reply-status" v-if="review.hasShopReply">
                          <el-icon><ChatDotRound /></el-icon> 商家已回复
                        </span>
                      </div>
                      
                      <div v-if="review.hasShopReply && review.shopReply" class="shop-reply">
                        <div class="reply-header">
                          <span class="reply-label">商家回复：</span>
                          <span class="reply-time">{{ formatDate(review.shopReplyTime) }}</span>
                        </div>
                        <div class="reply-content">{{ review.shopReply }}</div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
      </div>
      
    <!-- 编辑帖子弹窗：复用“发布新帖”同款编辑组件 -->
    <PostEditorDialog
      v-model="editDialogVisible"
      mode="edit"
      :post-id="editingPostId"
      :topic-list="topicList"
      @submitted="afterEditSubmitted"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessageStore } from '@/stores/message'
import { useForumStore } from '@/stores/forum'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'
import { Timer, View, ChatDotRound, Star, Edit, Delete } from '@element-plus/icons-vue'
import { showByCode } from '@/utils/apiMessages'
import { commonPromptMessages } from '@/utils/promptMessages'
import PostEditorDialog from '@/components/forum/PostEditorDialog.vue'
import SafeImage from '@/components/common/form/SafeImage.vue'
import { deleteTeaReview } from '@/api/tea'

const router = useRouter()
const route = useRoute()
const messageStore = useMessageStore()
const forumStore = useForumStore()
const userStore = useUserStore()
const activeTab = ref('posts')
const sortOption = ref('newest')

// 编辑帖子弹窗相关状态
const editDialogVisible = ref(false)
const editingPostId = ref(null)
const topicList = computed(() => forumStore.forumTopics || [])

// 当前登录用户ID（与 UserHomePage 保持一致）
const currentUserId = computed(() => {
  const base = userStore.userInfo || {}
  return base.id || base.userId || null
})

// 直接用路由参数推导当前查看的 userId（与 FollowsPage / UserHomePage 规则一致）
const profileUserId = computed(() => {
  const firstParam = route.params.userId
  if (!firstParam || firstParam === 'current') return null
  if (firstParam === 'published' || firstParam === 'follows' || firstParam === 'favorites') return null
  return firstParam
})
    
    // 从Pinia获取数据
    const posts = computed(() => messageStore.userPosts || [])
    const reviews = computed(() => messageStore.userReviews || [])
    const loading = computed(() => messageStore.loading)
    const postsPagination = computed(() => messageStore.postsPagination)
    const reviewsPagination = computed(() => messageStore.reviewsPagination)
    
    // 判断是否为查看自己的主页
    // 如果 profileUserId 为 null（路由是 current 或 published/follows/favorites），且 currentUserId 存在，则认为是查看自己的主页
    // 如果 profileUserId 存在，则比较是否等于 currentUserId
    const isSelf = computed(() => {
      if (!currentUserId.value) return false
      // 如果 profileUserId 为 null，说明是查看自己的主页（路由是 /profile/current/...）
      if (!profileUserId.value) return true
      // 如果 profileUserId 存在，比较是否等于 currentUserId
      return String(profileUserId.value) === String(currentUserId.value)
    })
    
    // 根据排序选项对帖子进行排序
    const sortedPosts = computed(() => {
      const postsCopy = [...posts.value]
      
      switch(sortOption.value) {
      case 'newest':
        return postsCopy.sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
      case 'mostViewed':
        return postsCopy.sort((a, b) => b.viewCount - a.viewCount)
      case 'mostReplied':
        return postsCopy.sort((a, b) => b.replyCount - a.replyCount)
      default:
        return postsCopy
      }
    })
    
    // 根据排序选项对评价进行排序
    const sortedReviews = computed(() => {
      const reviewsCopy = [...reviews.value]
      return reviewsCopy.sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
    })
    
    // 格式化日期显示
    const formatDate = dateString => {
      if (!dateString) return ''
      
      const date = new Date(dateString)
      const now = new Date()
      const diffTime = Math.abs(now - date)
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
      
      if (diffDays < 1) {
        // 当天发布显示时间
        return `今天 ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
      } else if (diffDays < 30) {
        // 30天内显示天数
        return `${diffDays}天前`
      } else {
        // 超过30天显示完整日期
        return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
      }
    }
    
    // 查看帖子详情
    const viewPostDetail = id => {
      router.push(`/forum/${id}`)
    }
    
    // 编辑帖子：打开编辑弹窗，预填当前帖子数据
    const editPost = post => {
      if (!post) return
      editingPostId.value = post.id
      editDialogVisible.value = true
    }
    
    const afterEditSubmitted = async () => {
      await loadData()
    }
    
    // 删除帖子
    const deletePost = id => {
      ElMessageBox.confirm(
        '确定要删除该帖子吗？删除后将无法恢复。',
        '删除确认',
        {
          confirmButtonText: '确定删除',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
        .then(async () => {
          const res = await forumStore.deletePost(id)
          showByCode(res.code)
          // 重新加载数据
          await loadData()
        })
        .catch(() => {
          // 用户取消操作
        })
    }
    
    // 查看茶叶详情（跳转到评论区域）
    const viewTeaDetail = teaId => {
      router.push(`/tea/${teaId}`)
    }
    
    // 查看茶叶详情并定位到指定评价（跳转到评论区域并高亮该评价）
    const viewTeaDetailWithReview = (teaId, reviewId) => {
      // 跳转到茶叶详情页，并传递reviewId参数，详情页可以根据参数定位到对应评价
      router.push({
        path: `/tea/${teaId}`,
        query: { reviewId }
      })
    }
    
    // 删除评价
    const deleteReview = async id => {
      ElMessageBox.confirm(
        '确定要删除该评价吗？删除后将无法恢复。',
        '删除确认',
        {
          confirmButtonText: '确定删除',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
        .then(async () => {
          try {
            const res = await deleteTeaReview(id)
            showByCode(res.code)
          // 重新加载数据
            await loadData()
          } catch (error) {
            console.error('删除评价失败:', error)
            commonPromptMessages.showError('删除评价失败，请稍后重试')
          }
        })
        .catch(() => {
          // 用户取消操作
        })
    }
    
    // 加载数据（统一行为：同时加载帖子和评价数据，确保统计数字准确）
    // 注意：初始数据在 UserHomePage 的 loadUserData 中统一调用，这里只处理用户操作（切换标签页、排序等）
    const loadData = async (loadBoth = false) => {
      // 如果未登录，不调用接口（避免退出登录时触发）
      if (!userStore.isLoggedIn) {
        messageStore.userPosts = []
        messageStore.userReviews = []
        return
      }
      
      // 参考统计接口的处理逻辑：判断主页是否可见
      const isSelf = currentUserId.value && profileUserId.value && String(profileUserId.value) === String(currentUserId.value)
      const profileVisible = messageStore.userProfile?.profileVisible !== false
      
      // 仅在本人或对方允许查看时才请求内容接口；否则直接置空并不发请求
      if (!isSelf && !profileVisible) {
        messageStore.userPosts = []
        messageStore.userReviews = []
        return
      }
      
      try {
        const paramsBase = {}
        if (profileUserId.value) {
          paramsBase.userId = profileUserId.value
        }
        
        // 如果 loadBoth 为 true（页面初始化时），或者当前标签页需要的数据，都加载
        if (loadBoth || activeTab.value === 'posts') {
          const postsRes = await messageStore.fetchUserPosts({ ...paramsBase, sortBy: sortOption.value })
          if (postsRes) showByCode(postsRes.code)
        }
        
        if (loadBoth || activeTab.value === 'reviews') {
          const reviewsRes = await messageStore.fetchUserReviews(paramsBase)
          if (reviewsRes) showByCode(reviewsRes.code)
        }
      } catch (error) {
        console.error('加载发布内容失败：', error)
      }
    }
    
    // 监听标签页切换（切换时只加载当前标签页的数据，因为另一个已经在初始化时加载过了）
    const handleTabChange = tab => {
      activeTab.value = tab
      loadData(false) // 切换标签页时不需要同时加载两个接口
    }
    
    // 监听排序选项变化
    const handleSortChange = () => {
      if (activeTab.value === 'posts') {
        loadData()
      }
    }
    
// 组件挂载时：不调用接口，数据由 UserHomePage 的 loadUserData 统一加载
onMounted(() => {
  // 编辑弹窗需要分类下拉：确保 topicList 已加载
  if (!topicList.value || topicList.value.length === 0) {
    forumStore.fetchForumTopics().catch(() => {})
  }
  // 不在这里调用 loadData，数据由 UserHomePage 的 loadUserData 统一加载
})

watch(() => route.params.userId, () => {
  // 切换用户时也不在这里调用，由 UserHomePage 的 loadUserData 统一处理
})
</script>

<style lang="scss" scoped>
.published-content-page {
  padding: 40px 0;
  
  .page-title {
    font-size: 2rem;
    color: var(--text-primary);
    margin-bottom: 10px;
    text-align: center;
  }
  
  .page-desc {
    font-size: 1.2rem;
    color: var(--text-secondary);
    margin-bottom: 40px;
    text-align: center;
  }
  
  .content-tabs {
    margin-bottom: 20px;
  }

  // 顶部横栏样式（对齐“关注”页）
  .filter-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding: 15px;
    background-color: #f8f9fa;
    border-radius: 8px;

    .filter-tabs {
      .el-radio-group {
        .el-radio-button {
          margin-right: 0;
        }
      }
    }

    .filter-actions {
      display: flex;
      align-items: center;
    }
  }
  
  .list-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    .list-title {
      font-size: 16px;
      font-weight: 500;
      color: var(--el-text-color-primary);
    }
  }
  
  .post-items {
    .post-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 20px;
      margin-bottom: 15px;
      background-color: #f9f9f9;
      border-radius: 6px;
      transition: all 0.3s;
      cursor: pointer;
      
      &:hover {
        background-color: #f0f0f0;
        transform: translateY(-2px);
      }
      
      .post-content {
        flex: 1;
        
        .post-title {
          font-size: 18px;
          font-weight: 500;
          color: var(--el-text-color-primary);
          margin-bottom: 10px;
        }
        
        .post-summary {
          font-size: 14px;
          color: var(--el-text-color-regular);
          margin-bottom: 10px;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
          overflow: hidden;
        }
        
        .post-meta {
          display: flex;
          align-items: center;
          font-size: 13px;
          color: var(--el-text-color-secondary);
          
          span {
            display: flex;
            align-items: center;
            margin-right: 15px;
            
            .el-icon {
              margin-right: 4px;
            }
          }
        }
      }
      
      .post-actions {
        display: flex;
        gap: 10px;
      }
    }
  }
  
  .review-items {
    .review-item {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      padding: 20px;
      margin-bottom: 15px;
      background-color: #f9f9f9;
      border-radius: 6px;
      transition: all 0.3s;
      cursor: pointer;
      
      &:hover {
        background-color: #f0f0f0;
        transform: translateY(-2px);
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      }
      
      .review-content {
        flex: 1;
        
        .review-rating {
          margin-bottom: 10px;
        }
        
        .review-text {
          font-size: 14px;
          color: var(--el-text-color-regular);
          line-height: 1.6;
          margin-bottom: 10px;
        }
        
        .review-images-wrapper {
          display: flex;
          align-items: flex-start;
          gap: 15px;
          margin-bottom: 10px;
          
          .review-images {
            display: flex;
            flex-wrap: wrap;
            gap: 10px;
            flex: 1;
            
            .review-image-item {
              .review-image {
                max-width: 200px;
                max-height: 200px;
                width: auto;
                height: auto;
                border-radius: 6px;
                object-fit: contain;
                cursor: pointer;
                transition: transform 0.3s;
                
                &:hover {
                  transform: scale(1.05);
                }
              }
            }
          }
          
          .review-actions {
            display: flex;
            align-items: flex-start;
            flex-shrink: 0;
          }
        }
        
        .review-meta {
          display: flex;
          align-items: center;
          font-size: 13px;
          color: var(--el-text-color-secondary);
          margin-bottom: 10px;
          
          span {
            display: flex;
            align-items: center;
            margin-right: 15px;
            
            .el-icon {
              margin-right: 4px;
            }
          }
        }
        
        .shop-reply {
          background-color: #f5f7fa;
          padding: 15px;
          border-radius: 6px;
          border-left: 4px solid var(--el-color-primary);
          margin-top: 10px;
          
          .reply-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 8px;
            
            .reply-label {
              font-weight: 500;
              color: var(--el-color-primary);
            }
            
            .reply-time {
              font-size: 12px;
              color: var(--el-text-color-secondary);
            }
          }
          
          .reply-content {
            font-size: 14px;
            color: var(--el-text-color-regular);
            line-height: 1.6;
          }
        }
      }
      
      .review-actions {
        display: flex;
        align-items: flex-start;
        margin-left: 20px;
      }
    }
  }
  
}

@media (max-width: 768px) {
  .published-content-page {
    .post-items {
      .post-item {
        flex-direction: column;
        align-items: flex-start;
        
        .post-actions {
          width: 100%;
          margin-top: 15px;
          justify-content: flex-end;
        }
      }
    }
    
    .review-items {
      .review-item {
        flex-direction: column;
        
        .review-actions {
          margin-left: 0;
          margin-top: 15px;
          width: 100%;
          justify-content: flex-end;
        }
      }
    }
  }
}
</style> 