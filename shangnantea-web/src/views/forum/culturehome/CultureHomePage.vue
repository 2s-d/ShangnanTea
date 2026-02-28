<template>
  <div class="culture-home-page">
    <!-- 顶部轮播图 -->
    <div class="banner-section">
      <el-carousel height="400px" indicator-position="outside" :interval="5000">
        <el-carousel-item v-for="(item, index) in bannerData" :key="index">
          <div class="banner-content" :style="{ backgroundImage: `url(${item.imageUrl})` }">
            <div class="banner-text">
              <h1>{{ item.title }}</h1>
              <p>{{ item.subtitle }}</p>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>
    
    <!-- 文化简介 -->
    <div class="culture-intro">
      <h2 class="section-title">商南茶文化</h2>
      <div class="subtitle">千年茶乡，一片叶子的传奇</div>
      <p class="intro-text">商南茶文化源远流长，历史可追溯至唐代，以"秦山云雾茶叶"的独特风韵著称，让您通过一叶茶品尝到历史与文化的香醇。</p>
      
      <!-- AR虚拟试饮按钮 -->
      <div class="ar-virtual-btn">
        <el-button type="danger" @click="startARTasting">
          <el-icon><VideoPlay /></el-icon>
          AR虚拟试饮（体验版）
        </el-button>
      </div>
    </div>
    
    <!-- 四栏目文章板块 - 改为新样式 -->
    <div class="article-section">
      <div class="article-container">
        <!-- 动态分类栏目：与后端 /forum/categories 对接 -->
        <div
          class="article-column"
          v-for="category in categoryColumns"
          :key="category.id"
        >
          <div class="column-header">
            <div class="header-indicator"></div>
            <h3>{{ category.name }}</h3>
          </div>
          <ul class="article-list">
            <li
              v-for="article in category.articles"
              :key="article.id"
              @click="viewArticle(article.id)"
            >
              <span class="article-title">{{ article.title }}</span>
              <span class="article-date">{{ article.date }}</span>
            </li>
          </ul>
        </div>
      </div>
      
      <!-- 添加加载更多按钮 -->
      <!-- 中间刷新按钮：现在专门用于“换一批推荐好茶” -->
      <div class="load-more-container">
        <el-button 
          type="primary" 
          circle
          size="large"
          @click="refreshRecommendTeas"
          :loading="refreshing"
        >
          <el-icon><RefreshRight /></el-icon>
        </el-button>
      </div>
    </div>
    
    <!-- 推荐茶叶 -->
    <div class="tea-recommend-section">
      <h2 class="section-title">商南好茶推荐</h2>
      <div class="subtitle">甄选商南优质茶品</div>
      
      <div class="tea-list">
        <div 
          v-for="(tea, index) in recommendTeas" 
          :key="index" 
          class="tea-item"
          @click="viewTeaDetail(tea.id)"
        >
          <div class="tea-image">
            <SafeImage :src="tea.image" type="tea" :alt="tea.name" style="width:100%;height:100%;object-fit:cover;" />
          </div>
          <div class="tea-info">
            <h4>{{ tea.name }}</h4>
            <p class="tea-price">¥{{ tea.price }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useForumStore } from '@/stores/forum'
import { useTeaStore } from '@/stores/tea'

import { RefreshRight, VideoPlay } from '@element-plus/icons-vue'
import SafeImage from '@/components/common/form/SafeImage.vue'
import { showByCode } from '@/utils/apiMessages'
import { forumPromptMessages } from '@/utils/promptMessages'

const router = useRouter()
const forumStore = useForumStore()
const teaStore = useTeaStore()
const refreshing = ref(false)
    
// 从Pinia获取数据
const bannerData = computed(() => forumStore.banners)
const cultureFeatures = computed(() => forumStore.cultureFeatures)
const teaCategories = computed(() => forumStore.teaCategories)
const latestNews = computed(() => forumStore.latestNews)
const partners = computed(() => forumStore.partners)
const loading = computed(() => forumStore.loading)
    
// 首页文章栏目：与后端分类管理（/forum/categories）动态对接
// 每个分类一列，每列最多显示5篇文章
const categoryColumns = computed(() => {
  const categories = forumStore.categories || []
  const articles = forumStore.articles || []

  return categories.map(category => {
    const list = articles
      .filter(article => article.category === category.name)
      .slice(0, 5)

    return {
      id: category.id,
      name: category.name,
      articles: list
    }
  })
})

// 推荐茶叶（从茶叶 store 获取，并适配后端字段结构）
const recommendTeas = computed(() => {
  const source = Array.isArray(teaStore.recommendTeas) ? [...teaStore.recommendTeas] : []
  const DISPLAY_COUNT = 6

  // 如果推荐池大于展示数量，则随机抽取 DISPLAY_COUNT 条
  let sampled = source
  if (source.length > DISPLAY_COUNT) {
    // Fisher-Yates 洗牌，避免修改原始数组
    for (let i = source.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1))
      ;[source[i], source[j]] = [source[j], source[i]]
    }
    sampled = source.slice(0, DISPLAY_COUNT)
  }

  return sampled.map(item => {
    const images = Array.isArray(item.images) ? item.images : []
    const image =
      (images.length > 0 && images[0]) ||
      item.mainImage ||
      item.main_image ||
      defaultTeaImage

    return {
      id: item.id,
      name: item.name,
      price: item.price,
      image
    }
  })
})

// 获取首页数据
const fetchHomeData = async () => {
  try {
    const res = await forumStore.fetchHomeData()
    showByCode(res.code)
  } catch (error) {
    console.error('获取首页数据失败:', error)
  }
}

// 获取Banner数据
const fetchBanners = async () => {
  try {
    const res = await forumStore.fetchBanners()
    showByCode(res.code)
  } catch (error) {
    console.error('获取轮播图数据失败:', error)
  }
}

// 获取文章数据
const fetchArticles = async () => {
  try {
    const res = await forumStore.fetchArticles()
    showByCode(res.code)
    return res
  } catch (error) {
    console.error('获取文章数据失败:', error)
  }
}

// 获取分类列表（用于首页栏目）
const fetchCategories = async () => {
  try {
    const res = await forumStore.fetchCategories()
    showByCode(res.code)
    return res
  } catch (error) {
    console.error('获取分类列表失败:', error)
  }
}

// 获取推荐茶叶（后端推荐机制：随机 / 热门 / 相似，这里首页用随机，刷新时更有变化）
const fetchRecommendTeas = async () => {
  try {
    // 一次性从后端拿到最多30个符合规则的推荐茶叶
    const res = await teaStore.fetchRecommendTeas({ type: 'random', count: 30 })
    showByCode(res.code)
    return res
  } catch (error) {
    console.error('获取推荐茶叶失败:', error)
  }
}

// 页面加载时获取数据
onMounted(async () => {
  await Promise.all([
    fetchHomeData(),
    fetchBanners(),
    fetchArticles(),
    fetchCategories(),
    fetchRecommendTeas()
  ])
})

// 查看文章详情
const viewArticle = id => {
  router.push(`/article/${id}`)
}

// 查看茶叶详情
const viewTeaDetail = id => {
  router.push(`/tea/${id}`)
}

// 刷新推荐好茶（换一批）
const refreshRecommendTeas = async () => {
  refreshing.value = true
  try {
    await fetchRecommendTeas()
  } catch (error) {
    console.error('刷新推荐茶叶失败:', error)
  } finally {
    refreshing.value = false
  }
}

// AR虚拟试饮功能
const startARTasting = () => {
  forumPromptMessages.showARDeveloping()
}

// 默认图片（生产形态：不使用 mock-images）
const defaultTeaImage = ''
const defaultCover = ''
const defaultAvatar = ''

// 文章列表占位（待后端接入）
const articleList = ref([])
</script>

<style lang="scss" scoped>
.culture-home-page {
  width: 100%;
  background-color: #f5f7fa;
  
  // 顶部轮播图
  .banner-section {
    width: 100%;
    margin-bottom: 30px;
    
    .banner-content {
      height: 100%;
      background-size: cover;
      background-position: center;
      position: relative;
      
      &::before {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(0, 0, 0, 0.2);
      }
      
      .banner-text {
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        text-align: center;
        color: #fff;
        z-index: 2;
        width: 80%;
        
        h1 {
          font-size: 36px;
          margin-bottom: 10px;
          font-weight: 500;
        }
        
        p {
          font-size: 18px;
        }
      }
    }
  }
  
  // 文化简介
  .culture-intro {
    max-width: 1200px;
    margin: 0 auto 40px;
    text-align: center;
    padding: 0 20px;
    
    .intro-text {
      font-size: 16px;
      line-height: 1.8;
      color: #5c5c5c;
      max-width: 800px;
      margin: 0 auto;
    }
  }
  
  // 文章板块 - 新样式
  .article-section {
    max-width: 1200px;
    margin: 0 auto 30px;
    padding: 0 20px;
    
    .article-container {
      display: flex;
      flex-wrap: wrap;
      gap: 20px;
      
      .article-column {
        flex: 1;
        min-width: 250px;
        background-color: #fff;
        padding: 0;
        
        .column-header {
          display: flex;
          align-items: center;
          padding: 0 0 10px 0;
          margin-bottom: 10px;
          border-bottom: 1px solid #eee;
          
          .header-indicator {
            width: 4px;
            height: 20px;
            background-color: #4CAF50;
            margin-right: 8px;
          }
          
          h3 {
            font-size: 16px;
            margin: 0;
            color: #333;
            font-weight: 500;
          }
        }
        
        .article-list {
          list-style: none;
          padding: 0;
          margin: 0;
          // 预留至少5行的高度，保证版面统一
          min-height: 150px;
          
          li {
            position: relative;
            padding: 6px 0 6px 12px;
            font-size: 14px;
            cursor: pointer;
            display: flex;
            justify-content: space-between;
            border-bottom: none;
            
            &:before {
              content: "•";
              position: absolute;
              left: 0;
              color: #666;
            }
            
            &:hover {
              .article-title {
                color: #4CAF50;
              }
            }
            
            .article-title {
              flex: 1;
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: nowrap;
              color: #333;
            }
            
            .article-date {
              color: #999;
              margin-left: 10px;
              white-space: nowrap;
            }
          }
        }
      }
    }
    
    // 添加加载更多按钮样式
    .load-more-container {
      display: flex;
      justify-content: center;
      margin-top: 30px;
      
      .el-button {
        font-size: 20px;
      }
    }
  }
  
  // 推荐茶叶
  .tea-recommend-section {
    max-width: 1200px;
    margin: 0 auto 60px;
    padding: 0 20px;
    
    .tea-list {
      display: flex;
      flex-wrap: wrap;
      gap: 20px;
      margin-top: 30px;
      
      .tea-item {
        width: calc(16.666% - 17px);
        min-width: 150px;
        background-color: #fff;
        border-radius: 8px;
        overflow: hidden;
        box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
        cursor: pointer;
        transition: transform 0.3s, box-shadow 0.3s;
        
        &:hover {
          transform: translateY(-5px);
          box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
        }
        
        .tea-image {
          width: 100%;
          padding-top: 100%; // 1:1 比例
          position: relative;
          
          img {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            object-fit: cover;
          }
        }
        
        .tea-info {
          padding: 12px;
          text-align: center;
          
          h4 {
            margin: 0 0 5px;
            font-size: 16px;
            color: #333;
            font-weight: 500;
          }
          
          .tea-price {
            color: #e74c3c;
            font-weight: 600;
            margin: 0;
          }
        }
      }
    }
  }
  
  // 通用样式
  .section-title {
    text-align: center;
    font-size: 28px;
    color: #333;
    margin: 0 0 5px;
    font-weight: 500;
  }
  
  .subtitle {
    text-align: center;
    font-size: 16px;
    color: #777;
    margin-bottom: 20px;
  }
}

// 响应式调整
@media (max-width: 1024px) {
  .culture-home-page {
    .article-container {
      .article-column {
        flex: calc(50% - 10px);
      }
    }
    
    .tea-recommend-section {
      .tea-list {
        .tea-item {
          width: calc(33.333% - 14px);
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .culture-home-page {
    .article-container {
      .article-column {
        flex: 100%;
      }
    }
    
    .tea-recommend-section {
      .tea-list {
        .tea-item {
          width: calc(50% - 10px);
        }
      }
    }
    
    .banner-section {
      .banner-content {
        .banner-text {
          h1 {
            font-size: 28px;
          }
          
          p {
            font-size: 16px;
          }
        }
      }
    }
  }
}
</style> 