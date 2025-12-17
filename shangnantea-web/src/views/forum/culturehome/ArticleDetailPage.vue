<template>
  <div class="article-detail-page">
    <!-- 文章内容容器 -->
    <div class="article-container">
      <!-- 文章标题区 -->
      <div class="article-header">
        <h1 class="article-title">{{ article.title }}</h1>
        <div class="article-subtitle" v-if="article.subtitle">{{ article.subtitle }}</div>
        
        <!-- 文章元信息 -->
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
      
      <!-- 文章内容区 -->
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
      
      <!-- 文章操作栏 -->
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
    
    <!-- 分享对话框 -->
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
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import SafeImage from '@/components/common/form/SafeImage.vue'

export default {
  name: 'ArticleDetailPage',
  components: {
    SafeImage
  },
  setup() {
    const route = useRoute()
    const router = useRouter()
    const loading = ref(false)
    const isLiked = ref(false)
    const shareDialogVisible = ref(false)
    const article = ref({
      id: 0,
      title: '文章标题加载中...',
      subtitle: '',
      content: '内容加载中...',
      author: '未知',
      publishTime: new Date(),
      viewCount: 0,
      likeCount: 0,
      tags: [],
      source: '',
      coverImage: ''
    })
    
    const relatedArticles = ref([])

    // 添加默认图片常量
    const defaultImage = '/mock-images/article-default.jpg'

    // 格式化日期
    const formatDate = (date) => {
      if (!date) return ''
      const d = new Date(date)
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
    }

    // 加载文章详情
    const loadArticleDetail = async () => {
      /* UI-DEV-START */
      loading.value = true
      // 模拟API请求延迟
      await new Promise(resolve => setTimeout(resolve, 800))
      
      // 模拟文章数据
      article.value = {
        id: route.params.id || 1,
        title: '商南茶的历史与文化',
        subtitle: '探寻千年茶乡的古韵今香',
        content: `<p class="content-paragraph">商南茶的历史可以追溯到唐代，当时已有文人墨客记载了商南地区所产茶叶的独特品质。商南县位于秦岭南麓，属于亚热带气候，山高谷深、云雾缭绕，海拔高、湿度大、温差大，非常适合茶树的生长。</p>
                  <h3 class="content-subtitle">地理环境的独特优势</h3>
                  <p class="content-paragraph">商南县地处北纬33度，属于世界公认的茶叶黄金生产带。这里的山地土壤有机质含量丰富，PH值在5-6.5之间，特别适合茶树生长。商南四面环山、中间为谷地，形成了特有的小气候环境，使商南茶形成了其独特的品质特征。</p>
                  <p class="content-paragraph">商南茶以其清香、回甘、耐泡等特点而闻名，被誉为"秦岭明珠"。特别是高山云雾茶，滋味醇厚，香气高扬，具有极高的品饮和收藏价值。</p>
                  <h3 class="content-subtitle">商南茶文化的传承</h3>
                  <p class="content-paragraph">千百年来，商南茶不仅是一种饮品，更是当地文化的重要组成部分。在商南，几乎每家每户都有种茶、制茶的传统，茶文化已经深深融入到当地人的日常生活中。</p>
                  <p class="content-paragraph">每年春季，是商南茶最为繁忙的季节，当地人会按照传统工艺采摘和制作春茶。采茶时要选择清晨露水未干时进行，这时采摘的茶叶鲜嫩，含水量适中，最适合加工成高品质茶叶。</p>
                  <h3 class="content-subtitle">现代商南茶产业</h3>
                  <p class="content-paragraph">近年来，随着人们对健康饮品需求的增加，商南茶产业得到了长足发展。当地政府大力扶持茶产业，推动茶园标准化建设，引导农民科学种植，使商南茶的品质和知名度不断提升。</p>
                  <p class="content-paragraph">如今，商南茶已成为当地经济的重要支柱产业，带动了旅游、文化等相关产业的发展，为当地脱贫致富做出了重要贡献。</p>`,
        author: '茶史专家',
        publishTime: '2025-03-16',
        viewCount: 126,
        likeCount: 35,
        tags: ['商南茶', '茶文化', '历史', '传统工艺', '产业发展'],
        source: '商南茶文化研究所',
        coverImage: 'https://via.placeholder.com/800x400?text=商南茶的历史与文化'
      }
      
      // 模拟相关文章
      relatedArticles.value = [
        {
          id: 2,
          title: '正确冲泡商南绿茶的方法与技巧',
          summary: '本文详细介绍了商南绿茶的正确冲泡方式，包括水温、茶具选择和冲泡时间等关键因素。',
          coverImage: 'https://via.placeholder.com/400x300?text=商南绿茶冲泡方法',
          publishTime: '2025-03-12',
          viewCount: 98
        },
        {
          id: 3,
          title: '商南茶的保健功效与科学依据',
          summary: '深入探讨商南茶的营养成分及其对人体健康的多种益处，包括抗氧化、助消化等功效。',
          coverImage: 'https://via.placeholder.com/400x300?text=商南茶保健功效',
          publishTime: '2025-03-10',
          viewCount: 112
        },
        {
          id: 4,
          title: '从农田到茶杯：商南茶的生产全过程',
          summary: '全面记录商南茶从种植、采摘到加工、包装的完整生产链条，展现传统与现代工艺的结合。',
          coverImage: 'https://via.placeholder.com/400x300?text=商南茶生产过程',
          publishTime: '2025-03-05',
          viewCount: 86
        }
      ]
      
      loading.value = false

      // 防止ResizeObserver错误，延迟处理DOM更新
      await nextTick()
      // 添加一个安全检查，避免ResizeObserver循环
      suppressResizeObserverError()
      /* UI-DEV-END */
      
      /* 
      // 真实代码(开发UI时注释)
      try {
        loading.value = true
        const result = await store.dispatch('forum/getArticleDetail', route.params.id)
        article.value = result
        
        // 加载相关文章
        const relatedResult = await store.dispatch('forum/getRelatedArticles', {
          id: route.params.id,
          category: article.value.category,
          tags: article.value.tags
        })
        relatedArticles.value = relatedResult
        
        // 防止ResizeObserver错误，延迟处理DOM更新
        await nextTick()
        suppressResizeObserverError()
      } catch (error) {
        ElMessage.error('获取文章详情失败')
      } finally {
        loading.value = false
      }
      */
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

    // 返回上一页
    const goBack = () => {
      router.back()
    }

    // 处理收藏
    const handleLike = () => {
      /* UI-DEV-START */
      isLiked.value = !isLiked.value
      if (isLiked.value) {
        article.value.likeCount++
        ElMessage.success('收藏成功')
      } else {
        article.value.likeCount--
        ElMessage.success('已取消收藏')
      }
      /* UI-DEV-END */
      
      /* 
      // 真实代码(开发UI时注释)
      try {
        if (!isLiked.value) {
          store.dispatch('forum/likeArticle', article.value.id)
          article.value.likeCount++
          isLiked.value = true
          ElMessage.success('收藏成功')
        } else {
          store.dispatch('forum/unlikeArticle', article.value.id)
          article.value.likeCount--
          isLiked.value = false
          ElMessage.success('已取消收藏')
        }
      } catch (error) {
        ElMessage.error('操作失败，请稍后再试')
      }
      */
    }
    
    // 处理分享
    const handleShare = () => {
      shareDialogVisible.value = true
    }
    
    // 分享到微信
    const shareToWeixin = () => {
      ElMessage.success('已复制链接，请在微信中粘贴分享')
      shareDialogVisible.value = false
    }
    
    // 分享到微博
    const shareToWeibo = () => {
      ElMessage.success('已跳转到微博分享页面')
      shareDialogVisible.value = false
    }
    
    // 分享到QQ
    const shareToQQ = () => {
      ElMessage.success('已跳转到QQ分享页面')
      shareDialogVisible.value = false
    }
    
    // 复制链接
    const copyLink = () => {
      ElMessage.success('链接已复制到剪贴板')
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

// 分享对话框样式
.share-options {
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

// 响应式样式
@media (max-width: 768px) {
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