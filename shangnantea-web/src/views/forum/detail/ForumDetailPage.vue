<template>
  <div class="forum-detail-page">
    <!-- 页面标题 -->
    <div class="page-header">
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
      <!-- 帖子内容 -->
      <div class="post-container">
        <div class="post-content" v-html="post.content"></div>
        <div v-if="post.cover" class="post-cover">
          <SafeImage :src="post.cover" type="post" :alt="post.title" class="cover-img" style="width:100%;object-fit:cover;" />
        </div>
        
        <div class="post-actions">
          <el-button type="primary" plain @click="likePost" :disabled="liked">
            <el-icon><Star /></el-icon> 
            {{ liked ? '已点赞' : '点赞' }} ({{ post.likeCount }})
          </el-button>
          <el-button plain @click="scrollToReply">
            <el-icon><ChatDotRound /></el-icon> 
            回复 ({{ post.replyCount }})
          </el-button>
          <el-button plain @click="toggleFavorite">
            <el-icon><StarFilled v-if="favorited" /><Star v-else /></el-icon> 
            {{ favorited ? '已收藏' : '收藏' }}
          </el-button>
          <el-button plain @click="showShareDialog">
            <el-icon><Share /></el-icon> 分享
          </el-button>
        </div>
      </div>
      
      <!-- 回复区域 -->
      <div class="reply-section" id="reply-section">
        <div class="section-header">
          <h2 class="section-title">全部回复 ({{ post.replyCount }})</h2>
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
                <span class="action-item" @click="likeReply(reply)">
                  <el-icon><Star /></el-icon> 
                  {{ reply.liked ? '已赞' : '点赞' }} ({{ reply.likeCount }})
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
              <el-input
                v-model="replyContent"
                type="textarea"
                :rows="4"
                placeholder="请输入回复内容..."
                maxlength="1000"
                show-word-limit
              />
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
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { View, ChatDotRound, Star, StarFilled, Share, Back, ChatLineRound, ArrowDown } from '@element-plus/icons-vue'
import SafeImage from '@/components/common/form/SafeImage.vue'

export default {
  name: 'ForumDetailPage',
  components: {
    View, ChatDotRound, Star, StarFilled, Share, Back, ChatLineRound, ArrowDown, SafeImage
  },
  setup() {
    const route = useRoute()
    const router = useRouter()
    
    // 默认图片
    const defaultAvatar = '/mock-images/avatar-default.jpg'
    const defaultCover = '/mock-images/tea-default.jpg'
    
    // 获取帖子ID
    const postId = computed(() => route.params.id)
    
    // 点赞和收藏状态
    const liked = ref(false)
    const favorited = ref(false)
    
    /* UI-DEV-START */
    // 模拟帖子数据
    const post = ref({
      id: 1,
      title: '如何正确冲泡绿茶？详细教程分享',
      content: `<p>绿茶的冲泡温度一般控制在80℃左右较为适宜，水温过高会破坏茶叶中的营养物质，使茶汤变苦涩。</p>
                <p>以下是冲泡绿茶的详细步骤：</p>
                <h3>1. 准备工作</h3>
                <p>选择合适的茶具，建议使用玻璃杯或白瓷盖碗，这样可以欣赏到绿茶的色泽。茶叶用量一般为3-5克（约一茶匙）。</p>
                <h3>2. 温杯洗茶</h3>
                <p>先用80℃左右的热水温杯，倒掉后放入茶叶，再次倒入少量热水快速洗茶，然后倒掉。这一步可以除去茶叶表面的浮尘。</p>
                <h3>3. 冲泡</h3>
                <p>将80℃的水沿杯壁缓缓注入，水量为杯子的三分之二左右。注水高度保持在离茶叶3-5厘米，避免直接冲击茶叶，以防破坏其形态。</p>
                <h3>4. 出汤</h3>
                <p>绿茶的第一泡一般泡30-60秒即可，时间不宜过长，否则茶汤会变苦。后续每泡可适当增加10-15秒。</p>
                <p>绿茶一般可以冲泡3-4次，第一泡茶汤鲜爽，香气最佳；第二泡滋味醇厚；第三泡以后香气逐渐减弱。</p>
                <p>希望这个教程对大家有所帮助，欢迎在评论区分享你的冲泡心得！</p>`,
      topicId: 1,
      topicName: '茶叶知识',
      authorId: 'cy100002',
      authorName: '茶韵悠长',
      authorAvatar: 'https://via.placeholder.com/40x40?text=茶韵',
      isSticky: 1,
      isEssence: 1,
      viewCount: 362,
      replyCount: 42,
      likeCount: 86,
      createTime: '2025-03-16 09:30:00'
    })
    /* UI-DEV-END */
    
    /*
    // 真实代码(开发UI时注释)
    const post = ref({})
    const loading = ref(true)
    
    // 获取帖子详情
    const fetchPostDetail = async () => {
      try {
        loading.value = true
        const result = await store.dispatch('forum/getPostDetail', postId.value)
        post.value = result
      } catch (error) {
        ElMessage.error('获取帖子详情失败')
      } finally {
        loading.value = false
      }
    }
    
    // 初始化数据
    onMounted(() => {
      fetchPostDetail()
    })
    */
    
    // 返回帖子列表
    const goBack = () => {
      router.back()
    }
    
    // 点赞帖子
    const likePost = () => {
      /* UI-DEV-START */
      if (!liked.value) {
        post.value.likeCount++
        liked.value = true
        ElMessage.success('点赞成功')
      }
      /* UI-DEV-END */
      
      /*
      // 真实代码(开发UI时注释)
      if (!liked.value) {
        store.dispatch('forum/likePost', postId.value).then(() => {
          post.value.likeCount++
          liked.value = true
          ElMessage.success('点赞成功')
        }).catch(() => {
          ElMessage.error('点赞失败，请重试')
        })
      }
      */
    }
    
    // 滚动到回复区域
    const scrollToReply = () => {
      const replySection = document.getElementById('reply-section')
      if (replySection) {
        replySection.scrollIntoView({ behavior: 'smooth' })
      }
    }
    
    // 收藏/取消收藏
    const toggleFavorite = () => {
      favorited.value = !favorited.value
      ElMessage.success(favorited.value ? '收藏成功' : '已取消收藏')
    }
    
    // 显示分享对话框
    const showShareDialog = () => {
      ElMessage.info('分享功能正在开发中')
    }
    
    // 格式化日期
    const formatDate = (dateString) => {
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
    
    /* UI-DEV-START */
    // 模拟回复数据
    const replyList = ref([
      {
        id: 1,
        postId: 1,
        userId: 'cy100003',
        userName: '茶香四溢',
        userAvatar: 'https://via.placeholder.com/40x40?text=茶香',
        content: '感谢分享！我一直以为绿茶需要用沸水冲泡，难怪总是很苦，原来是水温太高了。',
        parentId: null,
        toUserId: null,
        likeCount: 15,
        liked: false,
        createTime: '2025-03-16 10:15:00'
      },
      {
        id: 2,
        postId: 1,
        userId: 'cy100004',
        userName: '绿茶爱好者',
        userAvatar: 'https://via.placeholder.com/40x40?text=绿茶',
        content: '我想补充一点，不同产地的绿茶冲泡水温也有细微差别，龙井一般75-85℃，碧螺春可以80-90℃，具体还是要看茶叶的嫩度。',
        parentId: null,
        toUserId: null,
        likeCount: 23,
        liked: false,
        createTime: '2025-03-16 10:30:00'
      },
      {
        id: 3,
        postId: 1,
        userId: 'cy100005',
        userName: '茶道初学者',
        userAvatar: 'https://via.placeholder.com/40x40?text=初学',
        content: '请问冲泡时间真的很重要吗？我经常忘记时间，导致茶叶泡太久...',
        parentId: null,
        toUserId: null,
        likeCount: 8,
        liked: false,
        createTime: '2025-03-16 11:05:00'
      },
      {
        id: 4,
        postId: 1,
        userId: 'cy100002',
        userName: '茶韵悠长',
        userAvatar: 'https://via.placeholder.com/40x40?text=茶韵',
        content: '冲泡时间非常重要！绿茶第一泡最好控制在1分钟以内，泡久了会苦涩。可以用手机计时或购买专门的茶艺计时器。',
        parentId: 3,
        toUserId: 'cy100005',
        likeCount: 12,
        liked: false,
        createTime: '2025-03-16 11:15:00'
      },
      {
        id: 5,
        postId: 1,
        userId: 'cy100006',
        userName: '遇见好茶',
        userAvatar: 'https://via.placeholder.com/40x40?text=好茶',
        content: '楼主的讲解很详细，对于新手非常友好。我想问一下，这些方法适用于所有绿茶吗？比如碧螺春、龙井、毛尖等。',
        parentId: null,
        toUserId: null,
        likeCount: 6,
        liked: false,
        createTime: '2025-03-16 13:20:00'
      }
    ])
    
    // 模拟推荐帖子
    const recommendList = ref([
      {
        id: 11,
        title: '不同季节的绿茶冲泡技巧有何不同？',
        authorName: '四季茶友',
        viewCount: 183
      },
      {
        id: 12,
        title: '茶具选择指南：如何选择适合绿茶的茶具',
        authorName: '茶具控',
        viewCount: 246
      },
      {
        id: 13,
        title: '绿茶品鉴基础：从外形、香气到口感的完整评价',
        authorName: '品茶师',
        viewCount: 328
      },
      {
        id: 14,
        title: '商南绿茶的特色与冲泡要点',
        authorName: '商南茶农',
        viewCount: 159
      }
    ])
    
    pagination.total = 42 // 模拟总回复数
    /* UI-DEV-END */
    
    /*
    // 真实代码(开发UI时注释)
    const replyList = ref([])
    const recommendList = ref([])
    
    // 获取回复列表
    const fetchReplies = async () => {
      try {
        const params = {
          postId: postId.value,
          page: pagination.currentPage,
          limit: pagination.pageSize,
          sort: currentSort.value
        }
        const result = await store.dispatch('forum/getPostReplies', params)
        replyList.value = result.list
        pagination.total = result.total
      } catch (error) {
        ElMessage.error('获取回复失败')
      }
    }
    
    // 获取推荐帖子
    const fetchRecommendPosts = async () => {
      try {
        const params = {
          postId: postId.value,
          topicId: post.value.topicId,
          limit: 4
        }
        const result = await store.dispatch('forum/getRecommendPosts', params)
        recommendList.value = result
      } catch (error) {
        console.error('获取推荐帖子失败', error)
      }
    }
    
    // 初始化数据
    onMounted(() => {
      fetchReplies()
      fetchRecommendPosts()
    })
    */
    
    // 处理排序变更
    const handleSortChange = (sort) => {
      currentSort.value = sort
      
      /* UI-DEV-START */
      // 模拟排序效果
      if (sort === 'time') {
        replyList.value = [...replyList.value].sort((a, b) => new Date(a.createTime) - new Date(b.createTime))
      } else if (sort === 'timeDesc') {
        replyList.value = [...replyList.value].sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
      } else if (sort === 'hot') {
        replyList.value = [...replyList.value].sort((a, b) => b.likeCount - a.likeCount)
      }
      /* UI-DEV-END */
      
      /*
      // 真实代码(开发UI时注释)
      pagination.currentPage = 1
      fetchReplies()
      */
    }
    
    // 处理分页大小变更
    const handleSizeChange = (size) => {
      pagination.pageSize = size
      /*
      // 真实代码(开发UI时注释)
      fetchReplies()
      */
    }
    
    // 处理页码变更
    const handleCurrentChange = (page) => {
      pagination.currentPage = page
      /*
      // 真实代码(开发UI时注释)
      fetchReplies()
      */
    }
    
    // 点赞回复
    const likeReply = (reply) => {
      /* UI-DEV-START */
      if (!reply.liked) {
        reply.likeCount++
        reply.liked = true
        ElMessage.success('点赞成功')
      }
      /* UI-DEV-END */
      
      /*
      // 真实代码(开发UI时注释)
      if (!reply.liked) {
        store.dispatch('forum/likeReply', reply.id).then(() => {
          reply.likeCount++
          reply.liked = true
          ElMessage.success('点赞成功')
        }).catch(() => {
          ElMessage.error('点赞失败，请重试')
        })
      }
      */
    }
    
    // 显示回复输入框
    const showReplyInput = (reply) => {
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
    
    // 提交回复
    const submitReply = () => {
      if (!replyContent.value.trim()) {
        ElMessage.warning('回复内容不能为空')
        return
      }
      
      submitting.value = true
      
      /* UI-DEV-START */
      setTimeout(() => {
        // 模拟新回复数据
        const newReply = {
          id: replyList.value.length + 100,
          postId: post.value.id,
          userId: 'current-user',
          userName: '当前用户',
          userAvatar: defaultAvatar,
          content: replyContent.value,
          parentId: currentReply.value ? currentReply.value.id : null,
          toUserId: currentReply.value ? currentReply.value.userId : null,
          likeCount: 0,
          liked: false,
          createTime: new Date().toISOString().replace('T', ' ').substring(0, 19)
        }
        
        // 添加到回复列表顶部
        replyList.value.unshift(newReply)
        
        // 增加帖子回复计数
        post.value.replyCount++
        
        // 重置表单
        replyContent.value = ''
        currentReply.value = null
        submitting.value = false
        
        ElMessage.success('回复发布成功')
      }, 800)
      /* UI-DEV-END */
      
      /*
      // 真实代码(开发UI时注释)
      const replyData = {
        postId: postId.value,
        content: replyContent.value,
        parentId: currentReply.value ? currentReply.value.id : null,
        toUserId: currentReply.value ? currentReply.value.userId : null
      }
      
      store.dispatch('forum/addReply', replyData).then(() => {
        // 增加帖子回复计数
        post.value.replyCount++
        
        // 重置表单
        replyContent.value = ''
        currentReply.value = null
        
        // 刷新回复列表
        fetchReplies()
        
        ElMessage.success('回复发布成功')
      }).catch(() => {
        ElMessage.error('回复发布失败，请重试')
      }).finally(() => {
        submitting.value = false
      })
      */
    }
    
    // 查看帖子详情
    const viewPost = (postId) => {
      router.push(`/forum/${postId}`)
    }
    
    // 获取回复用户名
    const getReplyUserName = (replyId) => {
      const reply = replyList.value.find(item => item.id === replyId)
      return reply ? reply.userName : '未知用户'
    }
    
    // 获取回复内容
    const getReplyContent = (replyId) => {
      const reply = replyList.value.find(item => item.id === replyId)
      if (!reply) return '该回复已被删除'
      
      // 截断过长的内容
      const content = reply.content
      return content.length > 50 ? content.substring(0, 50) + '...' : content
    }
    
    return {
      post,
      liked,
      favorited,
      defaultAvatar,
      defaultCover,
      goBack,
      likePost,
      scrollToReply,
      toggleFavorite,
      showShareDialog,
      formatDate,
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