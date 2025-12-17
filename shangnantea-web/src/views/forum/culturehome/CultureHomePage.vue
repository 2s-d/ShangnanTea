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
        <!-- 茶叶历史 -->
        <div class="article-column">
          <div class="column-header">
            <div class="header-indicator"></div>
            <h3>茶叶历史</h3>
          </div>
          <ul class="article-list">
            <li v-for="(article, index) in articleData.history" :key="index" @click="viewArticle(article.id)">
              <span class="article-title">{{ article.title }}</span>
              <span class="article-date">{{ article.date }}</span>
            </li>
          </ul>
        </div>
        
        <!-- 茶艺茶道 -->
        <div class="article-column">
          <div class="column-header">
            <div class="header-indicator"></div>
            <h3>茶艺茶道</h3>
          </div>
          <ul class="article-list">
            <li v-for="(article, index) in articleData.art" :key="index" @click="viewArticle(article.id)">
              <span class="article-title">{{ article.title }}</span>
              <span class="article-date">{{ article.date }}</span>
            </li>
          </ul>
        </div>
        
        <!-- 茶叶百科 -->
        <div class="article-column">
          <div class="column-header">
            <div class="header-indicator"></div>
            <h3>茶叶百科</h3>
          </div>
          <ul class="article-list">
            <li v-for="(article, index) in articleData.encyclopedia" :key="index" @click="viewArticle(article.id)">
              <span class="article-title">{{ article.title }}</span>
              <span class="article-date">{{ article.date }}</span>
            </li>
          </ul>
        </div>
        
        <!-- 茶文化传承 -->
        <div class="article-column">
          <div class="column-header">
            <div class="header-indicator"></div>
            <h3>茶文化传承</h3>
          </div>
          <ul class="article-list">
            <li v-for="(article, index) in articleData.heritage" :key="index" @click="viewArticle(article.id)">
              <span class="article-title">{{ article.title }}</span>
              <span class="article-date">{{ article.date }}</span>
            </li>
          </ul>
        </div>
      </div>
      
      <!-- 添加加载更多按钮 -->
      <div class="load-more-container">
        <el-button 
          type="primary" 
          circle
          size="large"
          @click="refreshArticles"
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

<script>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { RefreshRight, VideoPlay } from '@element-plus/icons-vue'
import SafeImage from '@/components/common/form/SafeImage.vue'

export default {
  name: 'CultureHomePage',
  components: {
    RefreshRight, VideoPlay, SafeImage
  },
  setup() {
    const router = useRouter()
    const refreshing = ref(false)
    
    /* UI-DEV-START */
    // 轮播图数据
    const bannerData = ref([
      {
        imageUrl: 'https://via.placeholder.com/1600x400?text=商南茶文化',
        title: '商南茶文化',
        subtitle: '千年茶乡，一片叶子的传奇'
      },
      {
        imageUrl: 'https://via.placeholder.com/1600x400?text=茶道体验',
        title: '茶道体验',
        subtitle: '感受茶的灵魂与精髓'
      },
      {
        imageUrl: 'https://via.placeholder.com/1600x400?text=茶园风光',
        title: '茶园风光',
        subtitle: '云雾缭绕的秦岭山脉茶园'
      }
    ])
    
    // 文章数据
    const articleData = ref({
      // 茶叶历史
      history: [
        { id: 1, title: '商南茶产业的历史发展与演变', date: '12.30' },
        { id: 2, title: '秦岭深处的千年茶文化传承', date: '12.15' },
        { id: 3, title: '从传统制茶到现代工艺的演变', date: '12.10' },
        { id: 4, title: '商南茶业发展阶段和创新历程', date: '12.03' },
        { id: 5, title: '2024年商南茶产新品研发报告', date: '11.30' },
        { id: 6, title: '商南茶发展历程和名优产品', date: '11.25' },
        { id: 7, title: '秦岭南侧的茶叶种植历史', date: '11.20' },
        { id: 8, title: '商南茶种植和茶产品发展趋势', date: '11.15' }
      ],
      
      // 茶艺茶道
      art: [
        { id: 9, title: '秦岭茶：传统茶艺的展现', date: '12.18' },
        { id: 10, title: '商南茶的正确冲泡和饮用方法', date: '12.13' },
        { id: 11, title: '商南茶道的艺术表现形式', date: '12.8' },
        { id: 12, title: '陕南茶席布置与茶道艺术', date: '12.3' },
        { id: 13, title: '茶道礼仪：如何品品茶道', date: '11.28' },
        { id: 14, title: '商南茶道茶艺的推广与普及', date: '11.23' },
        { id: 15, title: '茶艺：商南茶的冲泡技巧', date: '11.18' },
        { id: 16, title: '茶道和礼法：商南茶文化的核心', date: '11.13' }
      ],
      
      // 茶叶百科
      encyclopedia: [
        { id: 17, title: '商南绿茶的品鉴特征与冲泡方法', date: '12.17' },
        { id: 18, title: '商南茶叶等级与口感特点解析', date: '12.12' },
        { id: 19, title: '商南茶叶的功效与健康价值', date: '12.7' },
        { id: 20, title: '你知道茶叶中的氨基酸吗？', date: '12.2' },
        { id: 21, title: '商南茶叶的采摘和存储技巧', date: '11.27' },
        { id: 22, title: '绿茶、红茶、乌龙茶的区别', date: '11.22' },
        { id: 23, title: '茶叶季节：如何判断新茶', date: '11.17' },
        { id: 24, title: '商南茶叶特性与常见问题解答', date: '11.12' }
      ],
      
      // 茶文化传承
      heritage: [
        { id: 25, title: '商南茶文化：从唐代流传至今的韵味', date: '12.19' },
        { id: 26, title: '传统茶艺表演的现代传承与创新', date: '12.11' },
        { id: 27, title: '商南茶乡旅游文化发展', date: '12.6' },
        { id: 28, title: '古老茶道的现代转型之路', date: '12.1' },
        { id: 29, title: '商南茶文化人物故事与经典', date: '11.26' },
        { id: 30, title: '商南茶文化：从传统到现代', date: '11.21' },
        { id: 31, title: '茶礼仪：商南茶文化的心灵体现', date: '11.16' },
        { id: 32, title: '茶与山水：秦岭商南茶的自然传承', date: '11.11' }
      ]
    })
    
    // 推荐茶叶数据
    const recommendTeas = ref([
      {
        id: 'tea1000001',
        name: '商南翠峰',
        price: '128',
        image: 'https://via.placeholder.com/180x180?text=商南翠峰'
      },
      {
        id: 'tea1000002',
        name: '丹江银针',
        price: '198',
        image: 'https://via.placeholder.com/180x180?text=丹江银针'
      },
      {
        id: 'tea1000003',
        name: '秦山云雾',
        price: '108',
        image: 'https://via.placeholder.com/180x180?text=秦山云雾'
      },
      {
        id: 'tea1000004',
        name: '商山毛尖',
        price: '158',
        image: 'https://via.placeholder.com/180x180?text=商山毛尖'
      },
      {
        id: 'tea1000005',
        name: '商州碧绿',
        price: '178',
        image: 'https://via.placeholder.com/180x180?text=商州碧绿'
      },
      {
        id: 'tea1000006',
        name: '商南云雾',
        price: '198',
        image: 'https://via.placeholder.com/180x180?text=商南云雾'
      }
    ])
    /* UI-DEV-END */
    
    /* 
    // 真实代码(开发UI时注释)
    const bannerData = ref([])
    const articleData = ref({
      history: [],
      art: [],
      encyclopedia: [],
      heritage: []
    })
    const recommendTeas = ref([])
    
    // 获取轮播图数据
    const fetchBannerData = async () => {
      try {
        const result = await store.dispatch('forum/getBanners')
        bannerData.value = result
      } catch (error) {
        ElMessage.error('获取轮播图数据失败')
      }
    }
    
    // 获取文章数据
    const fetchArticleData = async () => {
      try {
        const result = await store.dispatch('forum/getArticlesByCategories')
        articleData.value = result
      } catch (error) {
        ElMessage.error('获取文章数据失败')
      }
    }
    
    // 获取推荐茶叶
    const fetchRecommendTeas = async () => {
      try {
        const result = await store.dispatch('tea/getRecommendTeas')
        recommendTeas.value = result
      } catch (error) {
        ElMessage.error('获取推荐茶叶失败')
      }
    }
    
    // 页面加载时获取数据
    onMounted(() => {
      fetchBannerData()
      fetchArticleData()
      fetchRecommendTeas()
    })
    */
    
    // 查看文章详情
    const viewArticle = (id) => {
      router.push(`/article/${id}`)
    }
    
    // 查看茶叶详情
    const viewTeaDetail = (id) => {
      router.push(`/tea/${id}`)
    }
    
    // 刷新文章列表
    const refreshArticles = async () => {
      refreshing.value = true
      
      /* UI-DEV-START */
      // 模拟加载延迟
      await new Promise(resolve => setTimeout(resolve, 1000))
      
      // 随机调整文章顺序，模拟刷新效果
      const shuffleArticles = (array) => {
        for (let i = array.length - 1; i > 0; i--) {
          const j = Math.floor(Math.random() * (i + 1));
          [array[i], array[j]] = [array[j], array[i]];
        }
        return array;
      }
      
      // 复制现有文章数据并随机排序
      articleData.value = {
        history: shuffleArticles([...articleData.value.history]),
        art: shuffleArticles([...articleData.value.art]),
        encyclopedia: shuffleArticles([...articleData.value.encyclopedia]),
        heritage: shuffleArticles([...articleData.value.heritage])
      }
      /* UI-DEV-END */
      
      /* 
      // 真实代码(开发UI时注释)
      try {
        await fetchArticleData()
        ElMessage.success('文章内容已更新')
      } catch (error) {
        ElMessage.error('刷新文章失败，请稍后再试')
      }
      */
      
      refreshing.value = false
    }
    
    // AR虚拟试饮功能
    const startARTasting = () => {
      ElMessage.info('AR虚拟试饮功能正在开发中，敬请期待...')
    }
    
    // 添加默认图片常量
    const defaultTeaImage = '/mock-images/tea-default.jpg'
    const defaultCover = '/mock-images/tea-default.jpg'
    const defaultAvatar = '/mock-images/avatar-default.jpg'

    // 模拟文章列表数据
    const articleList = ref([
      {
        id: 1,
        title: '商南茶文化的历史渊源',
        cover: defaultCover,
        author: '茶史专家',
        authorAvatar: defaultAvatar,
        publishTime: '2025-03-15',
        views: 1234,
        likes: 56,
        comments: 23
      },
      {
        id: 2,
        title: '商南绿茶制作工艺',
        cover: defaultCover,
        author: '制茶大师',
        authorAvatar: defaultAvatar,
        publishTime: '2025-03-14',
        views: 890,
        likes: 45,
        comments: 12
      }
    ])
    
    return {
      bannerData,
      articleData,
      recommendTeas,
      refreshing,
      viewArticle,
      viewTeaDetail,
      refreshArticles,
      RefreshRight,
      startARTasting,
      defaultTeaImage,
      defaultCover,
      defaultAvatar,
      articleList
    }
  }
}
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