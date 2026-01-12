<template>
  <div class="article-detail-page">
    <!-- 文章内容容器 -->
    <div class="article-container">
      <!-- 文章标题�?-->
      <div class="article-header">
        <h1 class="article-title">{{ article.title }}</h1>
        <div class="article-subtitle" v-if="article.subtitle">{{ article.subtitle }}</div>
        
        <!-- 文章元信�?-->
        <div class="article-meta">
          <div class="meta-item">
            <i class="el-icon-user"></i>
            <span>作者：{{ article.author }}</span>
          </div>
          <div class="meta-item">
            <i class="el-icon-date"></i>
            <span>发布时间：{{ formatDate(article.publishTime) }}</span>
          </div>
          <div class="meta-item">
            <i class="el-icon-view"></i>
            <span>阅读量：{{ article.viewCount }}</span>
          </div>
          <div class="meta-item" v-if="article.source">
            <i class="el-icon-collection"></i>
            <span>来源：{{ article.source }}</span>
          </div>
        </div>
      </div>
      
      <!-- 文章封面图片 -->
      <div class="article-cover" v-if="article.coverImage">
        <SafeImage :src="article.coverImage" type="post" :alt="article.title" style="width:100%;" />
      </div>
      
      <!-- 文章内容�?-->
      <div class="article-content" v-html="article.content"></div>
      
      <!-- 文章标签 -->
      <div class="article-tags" v-if="article.tags && article.tags.length">
        <h3>标签</h3>
        <div class="tags-container">
          <el-tag 
            v-for="(tag, index) in article.tags" 
            :key="index"
            type="success"
            effect="plain"
            size="small"
            class="tag-item"
          >
            {{ tag }}
          </el-tag>
        </div>
      </div>
      
      <!-- 文章操作�?-->
      <div class="article-actions">
        <el-button type="primary" @click="goBack">
          <i class="el-icon-back"></i> 返回
        </el-button>
        <div class="action-buttons">
          <el-button type="default" @click="handleLike" :class="{'is-liked': isLiked}">
            <i class="el-icon-star-off" v-if="!isLiked"></i>
            <i class="el-icon-star-on" v-else></i>
            收藏 ({{ article.likeCount }})
          </el-button>
          <el-button type="default" @click="handleShare">
            <i class="el-icon-share"></i> 分享
          </el-button>
        </div>
      </div>
    </div>
    
    <!-- 相关文章推荐 -->
    <div class="related-articles">
      <h2 class="section-title">相关文章</h2>
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="8" v-for="(item, index) in relatedArticles" :key="index">
          <div class="related-item" @click="viewArticle(item.id)">
            <div class="related-image" v-if="item.coverImage">
              <SafeImage :src="item.coverImage" type="post" :alt="item.title" style="width:100%;height:100%;object-fit:cover;" />
            </div>
            <div class="related-info">
              <h3 class="related-title">{{ item.title }}</h3>
              <p class="related-summary">{{ item.summary }}</p>
              <div class="related-meta">
                <span><i class="el-icon-date"></i> {{ formatDate(item.publishTime) }}</span>
                <span><i class="el-icon-view"></i> {{ item.viewCount }}</span>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
    
    <!-- 分享对话�?-->
    <el-dialog
      title="分享文章"
      v-model:visible="shareDialogVisible"
      width="300px"
      center
    >
      <div class="share-options">
        <div class="share-option" @click="shareToWeixin">
          <i class="share-icon weixin"></i>
          <span>微信</span>
        </div>
        <div class="share-option" @click="shareToWeibo">
          <i class="share-icon weibo"></i>
          <span>微博</span>
        </div>
        <div class="share-option" @click="shareToQQ">
          <i class="share-icon qq"></i>
          <span>QQ</span>
        </div>
        <div class="share-option" @click="copyLink">
          <i class="share-icon link"></i>
          <span>复制链接</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted, nextTick, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useStore } from 'vuex'

import SafeImage from '@/components/common/form/SafeImage.vue'
import { showByCode } from '@/utils/apiMessages'
import { forumPromptMessages } from '@/utils/promptMessages'

export default {
  name: 'ArticleDetailPage',
  components: {
    SafeImage
  },
  setup() {
    const route = useRoute()
    const router = useRouter()
    const store = useStore()
    const loading = computed(() => store.state.forum.loading)
    const isLiked = ref(false)
    const shareDialogVisible = ref(false)
    
    // 从Vuex获取当前文章
    const article = computed(() => store.state.forum.currentArticle || {
      id: 0,
      title: '文章标题加载�?..',
      subtitle: '',
      content: '内容加载�?..',
      author: '未知',
      publishTime: new Date(),
      viewCount: 0,
      likeCount: 0,
      tags: [],
      source: '',
      coverImage: ''
    })
    
    // 相关文章（从文章列表中筛选同分类的其他文章）
    const relatedArticles = computed(() => {
      const articles = store.state.forum.articles || []
      const currentId = article.value.id
      const currentCategory = article.value.category
      
      return articles
        .filter(item => item.id !== currentId && item.category === currentCategory)
        .slice(0, 6) // 最多显�?篇相关文�?    })

    // 添加默认图片常量（生产形态：不使�?mock-images�?    const defaultImage = ''

    // 格式化日�?    const formatDate = (date) => {
      if (!date) return ''
      const d = new Date(date)
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
    }

    // 加载文章详情
    const loadArticleDetail = async () => {
      try {
        const articleId = route.params.id
        await store.dispatch('forum/fetchArticleDetail', articleId)
        
        // 如果文章列表为空，也加载文章列表用于相关文章推荐
        if (!store.state.forum.articles || store.state.forum.articles.length === 0) {
          await store.dispatch('forum/fetchArticles')
        }
        
        // 防止ResizeObserver错误，延迟处理DOM更新
        await nextTick()
        suppressResizeObserverError()
      } catch (error) {
        forumErrorMessages.showLoadFailed('获取文章详情失败')
        console.error('获取文章详情失败:', error)
      }
    }

    // 防止ResizeObserver循环错误
    const suppressResizeObserverError = () => {
      const resizeObserverError = window.ResizeObserver.prototype.observe
      window.ResizeObserver.prototype.observe = function (target, options) {
        try {
          return resizeObserverError.apply(this, [target, options])
        } catch (error) {
          if (error.message.includes('ResizeObserver loop limit exceeded')) {
            console.warn('ResizeObserver loop limit exceeded')
            return null
          }
          throw error
        }
      }
    }

    // 返回上一�?    const goBack = () => {
      router.back()
    }

    // 处理收藏
    const handleLike = async () => {
      try {
        if (!isLiked.value) {
          // 这里可以调用文章点赞API，目前暂时使用简单的状态切�?          isLiked.value = true
          forumPromptMessages.showShareDeveloping()
        } else {
          isLiked.value = false
          forumPromptMessages.showShareDeveloping()
        }
      } catch (error) {
        console.error('����ʧ��')
        console.error('收藏操作失败:', error)
      }
    }
    
    // 处理分享
    const handleShare = () => {
      shareDialogVisible.value = true
    }
    
    // 分享到微�?    const shareToWeixin = () => {
      forumPromptMessages.showShareDeveloping() // 临时使用，实际应该是复制链接成功
      shareDialogVisible.value = false
    }
    
    // 分享到微�?    const shareToWeibo = () => {
      forumPromptMessages.showShareDeveloping()
      shareDialogVisible.value = false
    }
    
    // 分享到QQ
    const shareToQQ = () => {
      forumPromptMessages.showShareDeveloping()
      shareDialogVisible.value = false
    }
    
    // 复制链接
    const copyLink = () => {
      forumPromptMessages.showShareDeveloping() // 临时使用，实际应该是复制链接成功
      shareDialogVisible.value = false
    }
    
    // 查看其他文章
    const viewArticle = (id) => {
      router.push(`/article/${id}`)
    }

    onMounted(() => {
      loadArticleDetail()
      
      // 全局捕获ResizeObserver错误
      window.addEventListener('error', (event) => {
        if (event && event.message && event.message.includes('ResizeObserver loop limit exceeded')) {
          event.stopImmediatePropagation()
        }
      })
    })

    return {
      article,
      relatedArticles,
      loading,
      isLiked,
      shareDialogVisible,
      formatDate,
      goBack,
      handleLike,
      handleShare,
      shareToWeixin,
      shareToWeibo,
      shareToQQ,
      copyLink,
      viewArticle,
      defaultImage
    }
  }
}
</script>

<style lang="scss" scoped>
.article-detail-page {
  max-width: 1200px;
  margin: 20px auto 60px;
  padding: 0 20px;
  
  .article-container {
    background: #fff;
    border-radius: 8px;
    padding: 30px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    margin-bottom: 40px;
    
    .article-header {
      text-align: center;
      margin-bottom: 30px;
      padding-bottom: 20px;
      border-bottom: 1px solid #eaeaea;
      
      .article-title {
        font-size: 28px;
        font-weight: 600;
        color: #333;
        margin-bottom: 10px;
        line-height: 1.4;
      }
      
      .article-subtitle {
        font-size: 18px;
        color: #666;
        margin-bottom: 20px;
      }
      
      .article-meta {
        display: flex;
        justify-content: center;
        flex-wrap: wrap;
        margin-top: 15px;
        color: #999;
        font-size: 14px;
        
        .meta-item {
          margin: 0 15px;
          display: flex;
          align-items: center;
          
          i {
            margin-right: 5px;
            font-size: 16px;
          }
        }
      }
    }
    
    .article-cover {
      margin-bottom: 30px;
      text-align: center;
      position: relative;
      width: 100%;
      max-width: 800px;
      margin-left: auto;
      margin-right: auto;
      height: 0;
      padding-bottom: 50%; /* 保持固定的宽高比 2:1 */
      overflow: hidden;
      
      img {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        object-fit: cover;
        border-radius: 6px;
      }
    }
    
    .article-content {
      font-size: 16px;
      line-height: 1.8;
      color: #333;
      margin-bottom: 30px;
      
      :deep(.content-subtitle) {
        font-size: 20px;
        font-weight: 600;
        margin: 25px 0 15px;
        color: #4CAF50;
      }
      
      :deep(.content-paragraph) {
        margin-bottom: 16px;
        text-indent: 2em;
      }
      
      :deep(img) {
        max-width: 100%;
        height: auto;
        display: block;
        margin: 20px auto;
        border-radius: 6px;
      }
    }
    
    .article-tags {
      margin: 30px 0;
      padding-top: 20px;
      border-top: 1px dashed #eaeaea;
      
      h3 {
        font-size: 16px;
        color: #555;
        margin-bottom: 10px;
      }
      
      .tags-container {
        display: flex;
        flex-wrap: wrap;
        
        .tag-item {
          margin-right: 10px;
          margin-bottom: 10px;
          cursor: pointer;
        }
      }
    }
    
    .article-actions {
      display: flex;
      justify-content: space-between;
      margin-top: 30px;
      
      .action-buttons {
        display: flex;
        
        .el-button {
          margin-left: 10px;
          
          &.is-liked {
            color: #E6A23C;
            border-color: #E6A23C;
            
            i {
              color: #E6A23C;
            }
          }
        }
      }
    }
  }
  
  .related-articles {
    .section-title {
      font-size: 24px;
      font-weight: 500;
      margin-bottom: 20px;
      color: #333;
      padding-left: 15px;
      border-left: 4px solid #4CAF50;
    }
    
    .related-item {
      background: #fff;
      border-radius: 8px;
      overflow: hidden;
      margin-bottom: 20px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
      cursor: pointer;
      transition: transform 0.3s, box-shadow 0.3s;
      height: 100%; /* 固定高度 */
      display: flex;
      flex-direction: column;
      
      &:hover {
        transform: translateY(-5px);
        box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
      }
      
      .related-image {
        position: relative;
        width: 100%;
        padding-top: 56.25%; /* 16:9 的固定宽高比 */
        overflow: hidden;
        
        img {
          position: absolute;
          top: 0;
          left: 0;
          width: 100%;
          height: 100%;
          object-fit: cover;
          transition: transform 0.3s;
        }
        
        &:hover img {
          transform: scale(1.05);
        }
      }
      
      .related-info {
        padding: 15px;
        flex: 1;
        display: flex;
        flex-direction: column;
        
        .related-title {
          font-size: 16px;
          margin: 0 0 10px;
          font-weight: 500;
          color: #333;
          line-height: 1.4;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
          overflow: hidden;
          height: 44px;
        }
        
        .related-summary {
          color: #666;
          font-size: 14px;
          line-height: 1.5;
          margin-bottom: 10px;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
          overflow: hidden;
          height: 42px;
          flex: 1;
        }
        
        .related-meta {
          display: flex;
          justify-content: space-between;
          color: #999;
          font-size: 12px;
          margin-top: auto;
          
          span {
            display: flex;
            align-items: center;
            
            i {
              margin-right: 4px;
            }
          }
        }
      }
    }
  }
}

// 分享对话框样�?.share-options {
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
  
  .share-option {
    width: 48%;
    height: 70px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    border-radius: 6px;
    margin-bottom: 15px;
    transition: background-color 0.3s;
    
    &:hover {
      background-color: #f5f7fa;
    }
    
    .share-icon {
      width: 36px;
      height: 36px;
      margin-bottom: 5px;
      display: inline-block;
      background-size: cover;
      
      &.weixin {
        background-color: #07C160;
        border-radius: 50%;
      }
      
      &.weibo {
        background-color: #E6162D;
        border-radius: 50%;
      }
      
      &.qq {
        background-color: #12B7F5;
        border-radius: 50%;
      }
      
      &.link {
        background-color: #909399;
        border-radius: 50%;
      }
    }
    
    span {
      font-size: 12px;
      color: #606266;
    }
  }
}

// 响应式样�?@media (max-width: 768px) {
  .article-detail-page {
    .article-container {
      padding: 20px 15px;
      
      .article-header {
        .article-title {
          font-size: 22px;
        }
        
        .article-subtitle {
          font-size: 16px;
        }
        
        .article-meta {
          flex-direction: column;
          align-items: center;
          
          .meta-item {
            margin: 5px 0;
          }
        }
      }
      
      .article-actions {
        flex-direction: column;
        
        .el-button {
          margin-bottom: 10px;
        }
        
        .action-buttons {
          justify-content: space-between;
          
          .el-button {
            margin-left: 0;
            width: 48%;
          }
        }
      }
    }
  }
}
</style> 