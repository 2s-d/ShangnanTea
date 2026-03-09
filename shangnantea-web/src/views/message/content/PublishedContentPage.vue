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
                    
                    <div class="post-actions">
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
      </div>
      
    <!-- 编辑帖子弹窗 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑帖子"
      width="600px"
      destroy-on-close
    >
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="editForm.title" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="摘要">
          <el-input
            v-model="editForm.summary"
            type="textarea"
            :rows="3"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="内容">
          <el-input
            v-model="editForm.content"
            type="textarea"
            :rows="6"
            maxlength="10000"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取 消</el-button>
          <el-button type="primary" :loading="editLoading" @click="submitEdit">
            保 存
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useMessageStore } from '@/stores/message'
import { useForumStore } from '@/stores/forum'
import { ElMessageBox } from 'element-plus'
import { Timer, View, ChatDotRound, Star, Edit, Delete } from '@element-plus/icons-vue'
import { showByCode } from '@/utils/apiMessages'
import { commonPromptMessages } from '@/utils/promptMessages'

const router = useRouter()
const messageStore = useMessageStore()
const forumStore = useForumStore()
const activeTab = ref('posts')
const sortOption = ref('newest')

// 编辑帖子弹窗相关状态
const editDialogVisible = ref(false)
const editingPostId = ref(null)
const editForm = ref({
  title: '',
  content: '',
  summary: ''
})
const editLoading = ref(false)
    
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
    
    // 编辑帖子：打开编辑弹窗，预填当前帖子数据
    const editPost = post => {
      if (!post) return
      editingPostId.value = post.id
      editForm.value = {
        title: post.title || '',
        // 个人中心列表没有富文本编辑，这里直接编辑纯文本内容
        content: post.content || '',
        summary: post.summary || ''
      }
      editDialogVisible.value = true
    }
    
    const submitEdit = async () => {
      if (!editingPostId.value) return
      try {
        editLoading.value = true
        const payload = {
          title: editForm.value.title,
          content: editForm.value.content,
          summary: editForm.value.summary
        }
        const res = await forumStore.updatePost(editingPostId.value, payload)
        showByCode(res.code)
        editDialogVisible.value = false
        // 编辑后刷新“我的帖子”列表
        await loadData()
      } catch (error) {
        console.error('更新帖子失败：', error)
      } finally {
        editLoading.value = false
      }
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