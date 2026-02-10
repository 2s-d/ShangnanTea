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
      
      <div class="content-tabs">
        <el-tabs v-model="activeTab" @tab-change="handleTabChange" v-loading="loading">
          <el-tab-pane label="论坛帖子" name="posts">
            <div class="posts-list">
              <!-- 帖子列表 -->
              <el-empty v-if="posts.length === 0" description="暂无发布内容" />
              
              <div v-else>
                <!-- 帖子列表筛选和排序 -->
                <div class="list-header">
                  <div class="list-title">发布的帖子 ({{posts.length}})</div>
                  <div class="list-actions">
                    <el-select v-model="sortOption" placeholder="排序方式" size="small" @change="handleSortChange">
                      <el-option label="最新发布" value="newest"></el-option>
                      <el-option label="最多浏览" value="mostViewed"></el-option>
                      <el-option label="最多回复" value="mostReplied"></el-option>
                    </el-select>
                  </div>
                </div>
                
                <!-- 帖子列表内容 -->
                <div class="post-items">
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
                    
                    <div class="post-actions">
                      <el-button type="primary" size="small" @click.stop="editPost(post.id)">
                        <el-icon><Edit /></el-icon> 编辑
                      </el-button>
                      <el-button type="danger" size="small" @click.stop="deletePost(post.id)">
                        <el-icon><Delete /></el-icon> 删除
                      </el-button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="茶叶评价" name="reviews">
            <div class="reviews-list">
              <!-- 评价列表 -->
              <el-empty v-if="reviews.length === 0" description="暂无评价记录" />
              
              <div v-else>
                <!-- 评价列表头部 -->
                <div class="list-header">
                  <div class="list-title">发布的评价 ({{reviews.length}})</div>
                </div>
                
                <!-- 评价列表内容 -->
                <div class="review-items">
                  <div v-for="review in sortedReviews" :key="review.id" class="review-item">
                    <div class="review-tea-info" @click="viewTeaDetail(review.teaId)">
                      <img :src="review.teaImage" :alt="review.teaName" class="tea-image" />
                      <div class="tea-details">
                        <div class="tea-name">{{ review.teaName }}</div>
                        <div class="shop-name">{{ review.shopName }}</div>
                      </div>
                    </div>
                    
                    <div class="review-content">
                      <div class="review-rating">
                        <el-rate v-model="review.rating" disabled show-score text-color="#ff9900" />
                      </div>
                      <div class="review-text">{{ review.content }}</div>
                      <div class="review-meta">
                        <span class="review-time">
                          <el-icon><Timer /></el-icon> {{ formatDate(review.createTime) }}
                        </span>
                        <span class="shop-reply-status" v-if="review.hasShopReply">
                          <el-icon><ChatDotRound /></el-icon> 商家已回复
                        </span>
                      </div>
                      
                      <!-- 商家回复 -->
                      <div v-if="review.hasShopReply && review.shopReply" class="shop-reply">
                        <div class="reply-header">
                          <span class="reply-label">商家回复：</span>
                          <span class="reply-time">{{ formatDate(review.shopReplyTime) }}</span>
                        </div>
                        <div class="reply-content">{{ review.shopReply }}</div>
                      </div>
                    </div>
                    
                    <div class="review-actions">
                      <el-button type="danger" size="small" @click="deleteReview(review.id)">
                        <el-icon><Delete /></el-icon> 删除
                      </el-button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
      
      <div class="coming-soon">
        <el-button type="primary" @click="goHome">返回茶文化</el-button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useMessageStore } from '@/stores/message'
import { ElMessageBox } from 'element-plus'
import { Timer, View, ChatDotRound, Star, Edit, Delete } from '@element-plus/icons-vue'
import { showByCode } from '@/utils/apiMessages'
import { commonPromptMessages } from '@/utils/promptMessages'

export default {
  name: 'PublishedContentPage',
  components: {
    Timer, View, ChatDotRound, Star, Edit, Delete
  },
  setup() {
    const router = useRouter()
    const messageStore = useMessageStore()
    const activeTab = ref('posts')
    const sortOption = ref('newest')
    
    // 从Pinia获取数据
    const posts = computed(() => messageStore.userPosts || [])
    const reviews = computed(() => messageStore.userReviews || [])
    const loading = computed(() => messageStore.loading)
    const postsPagination = computed(() => messageStore.postsPagination)
    const reviewsPagination = computed(() => messageStore.reviewsPagination)
    
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
    
    // 编辑帖子
    const editPost = id => {
      // 实际项目中跳转到编辑页面
      commonPromptMessages.showProcessing()
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
          // 实际项目中调用删除API
          // const res = await store.dispatch('message/deletePost', id)
          // showByCode(res.code)
          commonPromptMessages.showProcessing()
          // 重新加载数据
          loadData()
        })
        .catch(() => {
          // 用户取消操作
        })
    }
    
    // 查看茶叶详情
    const viewTeaDetail = teaId => {
      router.push(`/tea/${teaId}`)
    }
    
    // 删除评价
    const deleteReview = id => {
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
          // 实际项目中调用删除API
          // const res = await store.dispatch('message/deleteReview', id)
          // showByCode(res.code)
          commonPromptMessages.showProcessing()
          // 重新加载数据
          loadData()
        })
        .catch(() => {
          // 用户取消操作
        })
    }
    
    // 返回首页
    const goHome = () => {
      router.push('/')
    }
    
    // 加载数据
    const loadData = async () => {
      try {
        let res
        if (activeTab.value === 'posts') {
          res = await messageStore.fetchUserPosts({ sortBy: sortOption.value })
        } else if (activeTab.value === 'reviews') {
          res = await messageStore.fetchUserReviews()
        }
        if (res) showByCode(res.code)
      } catch (error) {
        console.error('加载发布内容失败：', error)
      }
    }
    
    // 监听标签页切换
    const handleTabChange = tab => {
      activeTab.value = tab
      loadData()
    }
    
    // 监听排序选项变化
    const handleSortChange = () => {
      if (activeTab.value === 'posts') {
        loadData()
      }
    }
    
    // 组件挂载时加载数据
    onMounted(() => {
      loadData()
    })
    
    return {
      activeTab,
      posts,
      reviews,
      sortOption,
      sortedPosts,
      sortedReviews,
      loading,
      postsPagination,
      reviewsPagination,
      formatDate,
      viewPostDetail,
      editPost,
      deletePost,
      viewTeaDetail,
      deleteReview,
      goHome,
      loadData,
      handleTabChange,
      handleSortChange
    }
  }
}
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
      padding: 20px;
      margin-bottom: 15px;
      background-color: #f9f9f9;
      border-radius: 6px;
      transition: all 0.3s;
      
      &:hover {
        background-color: #f0f0f0;
      }
      
      .review-tea-info {
        display: flex;
        align-items: flex-start;
        margin-right: 20px;
        cursor: pointer;
        
        .tea-image {
          width: 80px;
          height: 80px;
          border-radius: 6px;
          object-fit: cover;
          margin-right: 15px;
        }
        
        .tea-details {
          .tea-name {
            font-size: 16px;
            font-weight: 500;
            color: var(--el-text-color-primary);
            margin-bottom: 5px;
          }
          
          .shop-name {
            font-size: 14px;
            color: var(--el-text-color-regular);
          }
        }
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
  
  .coming-soon {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 50px;
    background-color: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    
    p {
      font-size: 1.2rem;
      color: var(--text-regular);
      margin-bottom: 30px;
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
        
        .review-tea-info {
          margin-right: 0;
          margin-bottom: 15px;
          width: 100%;
        }
        
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