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
        <el-tabs v-model="activeTab">
          <el-tab-pane label="论坛帖子" name="posts">
            <div class="posts-list">
              <!-- 帖子列表 -->
              <el-empty v-if="posts.length === 0" description="暂无发布内容" />
              
              <div v-else>
                <!-- 帖子列表筛选和排序 -->
                <div class="list-header">
                  <div class="list-title">发布的帖子 ({{posts.length}})</div>
                  <div class="list-actions">
                    <el-select v-model="sortOption" placeholder="排序方式" size="small">
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
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Timer, View, ChatDotRound, Star, Edit, Delete } from '@element-plus/icons-vue'

export default {
  name: 'PublishedContentPage',
  components: {
    Timer, View, ChatDotRound, Star, Edit, Delete
  },
  setup() {
    const router = useRouter()
    const activeTab = ref('posts')
    const sortOption = ref('newest')
    
    // 模拟数据 - 用户发布的帖子列表
    const posts = ref([
      {
        id: 1,
        title: '分享我最近喝过的安化黑茶，口感超赞！',
        summary: '最近入手了几款安化黑茶，冲泡后口感醇厚，回甘很持久，特别是金花茯砖茶，口感层次非常丰富...',
        createTime: '2025-03-15 14:32:25',
        viewCount: 156,
        replyCount: 23,
        likeCount: 45
      },
      {
        id: 2,
        title: '求推荐适合夏天喝的茶，清热解暑的那种',
        summary: '天气越来越热了，想找一些适合夏季饮用的茶，最好是有清热解暑功效的。有没有茶友能推荐一些？',
        createTime: '2025-02-28 09:15:42',
        viewCount: 89,
        replyCount: 17,
        likeCount: 12
      },
      {
        id: 3,
        title: '新手冲泡白茶总是苦涩，有什么技巧吗？',
        summary: '刚开始接触白茶，买了一些白毫银针，但冲泡出来总是有些苦涩，不知道是水温问题还是时间问题？求大神指点！',
        createTime: '2025-01-18 21:05:37',
        viewCount: 210,
        replyCount: 32,
        likeCount: 28
      }
    ])
    
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
    
    // 格式化日期显示
    const formatDate = (dateString) => {
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
    const viewPostDetail = (id) => {
      router.push(`/forum/${id}`)
    }
    
    // 编辑帖子
    const editPost = (id) => {
      // 实际项目中跳转到编辑页面
      ElMessage.info('编辑帖子功能开发中...')
    }
    
    // 删除帖子
    const deletePost = (id) => {
      ElMessageBox.confirm(
        '确定要删除该帖子吗？删除后将无法恢复。',
        '删除确认',
        {
          confirmButtonText: '确定删除',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
        .then(() => {
          // 实际项目中调用删除API
          posts.value = posts.value.filter(post => post.id !== id)
          ElMessage.success('帖子已删除')
        })
        .catch(() => {
          // 用户取消操作
        })
    }
    
    // 返回首页
    const goHome = () => {
      router.push('/')
    }
    
    return {
      activeTab,
      posts,
      sortOption,
      sortedPosts,
      formatDate,
      viewPostDetail,
      editPost,
      deletePost,
      goHome
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
  }
}
</style> 