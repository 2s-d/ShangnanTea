<template>
  <div class="favorites-page">
    <!-- 分类标签页 -->
    <el-tabs v-model="activeTab">
      <el-tab-pane label="茶文化内容" name="culture">
        <!-- 茶文化内容列表 -->
        <div class="list-header">
          <div class="list-title">收藏的茶文化内容 ({{filteredCultureArticles.length}})</div>
          <div class="list-actions">
            <el-input
              v-model="cultureSearchKeyword"
              placeholder="搜索标题"
              size="small"
              clearable
              style="width: 200px"
            >
              <template #suffix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-select v-model="cultureSortOption" placeholder="排序方式" size="small" style="margin-left: 10px">
              <el-option label="最近收藏" value="recent"></el-option>
              <el-option label="最热内容" value="popular"></el-option>
            </el-select>
          </div>
        </div>
        
        <el-empty v-if="filteredCultureArticles.length === 0" description="暂无收藏内容" />
        
        <div v-else class="culture-articles">
          <div v-for="article in filteredCultureArticles" :key="article.id" class="article-item">
            <div class="article-cover" @click="goToArticleDetail(article.id)">
              <SafeImage :src="article.cover_image" type="post" :alt="article.title" style="width:100%;height:100%;object-fit:cover;" />
            </div>
            <div class="article-info">
              <div class="article-title" @click="goToArticleDetail(article.id)">{{ article.title }}</div>
              <div class="article-summary">{{ article.summary }}</div>
              <div class="article-meta">
                <span class="publish-time">发布于 {{ formatDate(article.publishTime) }}</span>
                <span class="favorite-time">收藏于 {{ formatDate(article.favoriteTime) }}</span>
                <span class="view-count"><el-icon><View /></el-icon> {{ article.viewCount }}</span>
              </div>
            </div>
            <div class="article-actions">
              <el-button size="small" plain type="danger" @click="cancelFavorite('culture', article.id)">
                取消收藏
              </el-button>
            </div>
          </div>
        </div>
      </el-tab-pane>
      
      <el-tab-pane label="茶叶商品" name="product">
        <!-- 茶叶商品列表 -->
        <div class="list-header">
          <div class="list-title">收藏的茶叶商品 ({{filteredProducts.length}})</div>
          <div class="list-actions">
            <el-input
              v-model="productSearchKeyword"
              placeholder="搜索茶叶"
              size="small"
              clearable
              style="width: 200px"
            >
              <template #suffix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-select v-model="productSortOption" placeholder="排序方式" size="small" style="margin-left: 10px">
              <el-option label="最近收藏" value="recent"></el-option>
              <el-option label="价格从低到高" value="priceAsc"></el-option>
              <el-option label="价格从高到低" value="priceDesc"></el-option>
            </el-select>
          </div>
        </div>
        
        <el-empty v-if="filteredProducts.length === 0" description="暂无收藏茶叶" />
        
        <div v-else class="products-grid">
          <div v-for="product in filteredProducts" :key="product.id" class="product-card">
            <div class="product-cover" @click="goToProductDetail(product.id)">
              <SafeImage :src="product.image" type="tea" :alt="product.name" style="width:100%;height:100%;object-fit:cover;" />
            </div>
            <div class="product-info">
              <div class="product-title" @click="goToProductDetail(product.id)">{{ product.name }}</div>
              <div class="product-shop" @click="goToShopDetail(product.shopId)">
                <SafeImage :src="product.shop_logo" type="banner" :alt="product.shopName" class="shop-logo" style="width:20px;height:20px;border-radius:50%;object-fit:cover;" />
                <span>{{ product.shopName }}</span>
              </div>
              <div class="product-price">¥{{ product.price.toFixed(2) }}</div>
              <div class="favorite-time">收藏于 {{ formatDate(product.favoriteTime) }}</div>
            </div>
            <div class="product-actions">
              <el-button size="small" type="primary" @click="addToCart(product.id)">
                <el-icon><ShoppingCart /></el-icon> 加入购物车
              </el-button>
              <el-button size="small" plain type="danger" @click="cancelFavorite('product', product.id)">
                取消收藏
              </el-button>
            </div>
          </div>
        </div>
      </el-tab-pane>
      
      <el-tab-pane label="论坛帖子" name="post">
        <!-- 论坛帖子列表 -->
        <div class="list-header">
          <div class="list-title">收藏的论坛帖子 ({{filteredPosts.length}})</div>
          <div class="list-actions">
            <el-input
              v-model="postSearchKeyword"
              placeholder="搜索帖子标题"
              size="small"
              clearable
              style="width: 200px"
            >
              <template #suffix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-select v-model="postSortOption" placeholder="排序方式" size="small" style="margin-left: 10px">
              <el-option label="最近收藏" value="recent"></el-option>
              <el-option label="热门讨论" value="hot"></el-option>
            </el-select>
          </div>
        </div>
        
        <el-empty v-if="filteredPosts.length === 0" description="暂无收藏帖子" />
        
        <div v-else class="post-list">
          <div v-for="post in filteredPosts" :key="post.id" class="post-item">
            <div class="post-info" @click="goToPostDetail(post.id)">
              <div class="post-title">{{ post.title }}</div>
              <div class="post-summary">{{ post.content }}</div>
              <div class="post-meta">
                <span class="author" @click.stop="goToUserProfile(post.authorId)">
                  <SafeImage :src="post.author_avatar" type="avatar" :alt="post.authorName" class="author-avatar" style="width:24px;height:24px;border-radius:50%;object-fit:cover;" />
                  <span class="author-name">{{ post.authorName }}</span>
                </span>
                <span class="publish-time">发布于 {{ formatDate(post.publishTime) }}</span>
                <span class="favorite-time">收藏于 {{ formatDate(post.favoriteTime) }}</span>
                <div class="post-stats">
                  <span><el-icon><View /></el-icon> {{ post.viewCount }}</span>
                  <span><el-icon><ChatDotRound /></el-icon> {{ post.replyCount }}</span>
                  <span><el-icon><Star /></el-icon> {{ post.likeCount }}</span>
                </div>
              </div>
            </div>
            <div class="post-actions">
              <el-button size="small" plain type="danger" @click="cancelFavorite('post', post.id)">
                取消收藏
              </el-button>
            </div>
      </div>
    </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, View, ChatDotRound, Star, ShoppingCart } from '@element-plus/icons-vue'
import SafeImage from '@/components/common/form/SafeImage.vue'

export default {
  name: 'FavoritesPage',
  components: {
    Search, View, ChatDotRound, Star, ShoppingCart, SafeImage
  },
  setup() {
    const router = useRouter()
    const activeTab = ref('culture')
    
    // 茶文化文章相关数据
    const cultureSearchKeyword = ref('')
    const cultureSortOption = ref('recent')
    const cultureArticles = ref([
      {
        id: 101,
        title: '传统茶道的历史渊源',
        coverImg: 'https://via.placeholder.com/400x200?text=茶道历史',
        summary: '茶道并非只是一种饮茶方式，更是一种文化传承，本文将深入探讨茶道的历史渊源和文化内涵...',
        publishTime: '2025-02-15 10:23:45',
        favoriteTime: '2025-03-10 18:45:12',
        viewCount: 1256
      },
      {
        id: 102,
        title: '中国六大茶类详解',
        coverImg: 'https://via.placeholder.com/400x200?text=六大茶类',
        summary: '详细介绍中国六大茶类的制作工艺、品质特征、冲泡方法以及历史文化背景，带您了解中国茶文化的多样性...',
        publishTime: '2025-01-28 14:18:32',
        favoriteTime: '2025-03-05 09:12:30',
        viewCount: 986
      },
      {
        id: 103,
        title: '茶与健康：科学角度探究',
        coverImg: 'https://via.placeholder.com/400x200?text=茶与健康',
        summary: '从现代医学和营养学角度分析不同茶类的健康价值，解析茶多酚、儿茶素等成分对人体的影响...',
        publishTime: '2025-03-01 16:42:15',
        favoriteTime: '2025-03-02 20:15:45',
        viewCount: 1542
      }
    ])
    
    // 筛选和排序茶文化文章
    const filteredCultureArticles = computed(() => {
      let result = [...cultureArticles.value]
      
      // 搜索过滤
      if (cultureSearchKeyword.value) {
        const keyword = cultureSearchKeyword.value.toLowerCase()
        result = result.filter(article => 
          article.title.toLowerCase().includes(keyword) || 
          article.summary.toLowerCase().includes(keyword)
        )
      }
      
      // 排序
      if (cultureSortOption.value === 'recent') {
        result.sort((a, b) => new Date(b.favoriteTime) - new Date(a.favoriteTime))
      } else if (cultureSortOption.value === 'popular') {
        result.sort((a, b) => b.viewCount - a.viewCount)
      }
      
      return result
    })
    
    // 茶叶商品相关数据
    const productSearchKeyword = ref('')
    const productSortOption = ref('recent')
    const products = ref([
      {
        id: 201,
        name: '明前龙井茶叶 2025年春茶',
        image: 'https://via.placeholder.com/200x200?text=龙井',
        price: 288.00,
        shopId: 301,
        shopName: '西湖茗茶',
        shopLogo: 'https://via.placeholder.com/30x30?text=西湖',
        favoriteTime: '2025-03-12 10:28:45'
      },
      {
        id: 202,
        name: '武夷山金骏眉红茶 特级',
        image: 'https://via.placeholder.com/200x200?text=金骏眉',
        price: 368.00,
        shopId: 302,
        shopName: '武夷山茶坊',
        shopLogo: 'https://via.placeholder.com/30x30?text=武夷',
        favoriteTime: '2025-02-28 15:42:18'
      },
      {
        id: 203,
        name: '安溪铁观音 浓香型',
        image: 'https://via.placeholder.com/200x200?text=铁观音',
        price: 198.00,
        shopId: 303,
        shopName: '安溪茶业',
        shopLogo: 'https://via.placeholder.com/30x30?text=安溪',
        favoriteTime: '2025-03-08 09:15:30'
      },
      {
        id: 204,
        name: '云南普洱熟茶 七子饼',
        image: 'https://via.placeholder.com/200x200?text=普洱',
        price: 498.00,
        shopId: 304,
        shopName: '云南茗茶',
        shopLogo: 'https://via.placeholder.com/30x30?text=云南',
        favoriteTime: '2025-01-15 18:20:10'
      }
    ])
    
    // 筛选和排序茶叶商品
    const filteredProducts = computed(() => {
      let result = [...products.value]
      
      // 搜索过滤
      if (productSearchKeyword.value) {
        const keyword = productSearchKeyword.value.toLowerCase()
        result = result.filter(product => 
          product.name.toLowerCase().includes(keyword) || 
          product.shopName.toLowerCase().includes(keyword)
        )
      }
      
      // 排序
      if (productSortOption.value === 'recent') {
        result.sort((a, b) => new Date(b.favoriteTime) - new Date(a.favoriteTime))
      } else if (productSortOption.value === 'priceAsc') {
        result.sort((a, b) => a.price - b.price)
      } else if (productSortOption.value === 'priceDesc') {
        result.sort((a, b) => b.price - a.price)
      }
      
      return result
    })
    
    // 论坛帖子相关数据
    const postSearchKeyword = ref('')
    const postSortOption = ref('recent')
    const posts = ref([
      {
        id: 401,
        title: '分享我最近喝过的安化黑茶，口感超赞！',
        content: '最近入手了几款安化黑茶，冲泡后口感醇厚，回甘很持久，特别是金花茯砖茶，口感层次非常丰富...',
        authorId: 501,
        authorName: '茶香四溢',
        authorAvatar: 'https://via.placeholder.com/30x30?text=茶香',
        publishTime: '2025-03-15 14:32:25',
        favoriteTime: '2025-03-16 09:12:30',
        viewCount: 156,
        replyCount: 23,
        likeCount: 45
      },
      {
        id: 402,
        title: '新手冲泡白茶总是苦涩，有什么技巧吗？',
        content: '刚开始接触白茶，买了一些白毫银针，但冲泡出来总是有些苦涩，不知道是水温问题还是时间问题？求大神指点！',
        authorId: 502,
        authorName: '茶艺小白',
        authorAvatar: 'https://via.placeholder.com/30x30?text=小白',
        publishTime: '2025-03-10 09:18:42',
        favoriteTime: '2025-03-12 18:25:10',
        viewCount: 210,
        replyCount: 32,
        likeCount: 28
      },
      {
        id: 403,
        title: '2025年春茶市场行情分析',
        content: '随着2025年春茶陆续上市，各产区茶叶价格出现了不同程度的波动。本帖汇总了主要茶产区的最新行情，并分析市场趋势...',
        authorId: 503,
        authorName: '茶市观察者',
        authorAvatar: 'https://via.placeholder.com/30x30?text=观察',
        publishTime: '2025-02-28 16:45:38',
        favoriteTime: '2025-03-01 10:32:45',
        viewCount: 325,
        replyCount: 18,
        likeCount: 56
      }
    ])
    
    // 筛选和排序论坛帖子
    const filteredPosts = computed(() => {
      let result = [...posts.value]
      
      // 搜索过滤
      if (postSearchKeyword.value) {
        const keyword = postSearchKeyword.value.toLowerCase()
        result = result.filter(post => 
          post.title.toLowerCase().includes(keyword) || 
          post.content.toLowerCase().includes(keyword)
        )
      }
      
      // 排序
      if (postSortOption.value === 'recent') {
        result.sort((a, b) => new Date(b.favoriteTime) - new Date(a.favoriteTime))
      } else if (postSortOption.value === 'hot') {
        result.sort((a, b) => (b.viewCount + b.replyCount * 2) - (a.viewCount + a.replyCount * 2))
      }
      
      return result
    })
    
    // 格式化日期
    const formatDate = (dateString) => {
      if (!dateString) return ''
      
      const date = new Date(dateString)
      return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
    }
    
    // 跳转到文章详情
    const goToArticleDetail = (articleId) => {
      router.push(`/culture/article/${articleId}`)
    }
    
    // 跳转到商品详情
    const goToProductDetail = (productId) => {
      router.push(`/tea/${productId}`)
    }
    
    // 跳转到店铺详情
    const goToShopDetail = (shopId) => {
      router.push(`/shop/${shopId}`)
    }
    
    // 跳转到帖子详情
    const goToPostDetail = (postId) => {
      router.push(`/forum/${postId}`)
    }
    
    // 跳转到用户主页
    const goToUserProfile = (userId) => {
      router.push(`/profile/${userId}`)
    }
    
    // 加入购物车
    const addToCart = (productId) => {
      // 实际项目中调用添加购物车API
      ElMessage.success('已添加到购物车')
    }
    
    // 取消收藏
    const cancelFavorite = (type, id) => {
      // 实际项目中调用取消收藏API
      if (type === 'culture') {
        cultureArticles.value = cultureArticles.value.filter(item => item.id !== id)
        ElMessage.success('已取消收藏文章')
      } else if (type === 'product') {
        products.value = products.value.filter(item => item.id !== id)
        ElMessage.success('已取消收藏茶叶')
      } else if (type === 'post') {
        posts.value = posts.value.filter(item => item.id !== id)
        ElMessage.success('已取消收藏帖子')
      }
    }
    
    // 添加默认图片常量
    const defaultArticleImage = '/mock-images/article-default.jpg'
    const defaultTeaImage = '/mock-images/tea-default.jpg'
    const defaultShopLogo = '/mock-images/shop-logo-1.jpg'
    const defaultAvatar = '/mock-images/avatar-default.jpg'
    const defaultCover = '/mock-images/tea-default.jpg'
    
    // 模拟收藏数据
    const favoriteList = ref([
      {
        id: 1,
        type: 'tea',
        name: '商南绿茶',
        cover: defaultTeaImage,
        price: 128.00,
        description: '商南特产绿茶，清香醇厚',
        favoriteTime: '2025-03-15 10:30:00'
      },
      {
        id: 2,
        type: 'shop',
        name: '商南茶叶专营店',
        logo: defaultShopLogo,
        description: '专注商南茶叶20年',
        favoriteTime: '2025-03-14 15:20:00'
      }
    ])
    
    return {
      activeTab,
      // 茶文化文章
      cultureSearchKeyword,
      cultureSortOption,
      cultureArticles,
      filteredCultureArticles,
      // 茶叶商品
      productSearchKeyword,
      productSortOption,
      products,
      filteredProducts,
      // 论坛帖子
      postSearchKeyword,
      postSortOption,
      posts,
      filteredPosts,
      // 方法
      formatDate,
      goToArticleDetail,
      goToProductDetail,
      goToShopDetail,
      goToPostDetail,
      goToUserProfile,
      addToCart,
      cancelFavorite,
      defaultArticleImage,
      defaultTeaImage,
      defaultShopLogo,
      defaultAvatar,
      defaultCover,
      favoriteList
    }
  }
}
</script>

<style lang="scss" scoped>
.favorites-page {
  // 列表头部样式
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
    
    .list-actions {
      display: flex;
      align-items: center;
    }
  }
  
  // 茶文化文章列表样式
  .culture-articles {
    .article-item {
      display: flex;
      border-bottom: 1px solid #f0f0f0;
      padding: 20px 0;
      
      &:last-child {
        border-bottom: none;
      }
      
      .article-cover {
        width: 200px;
        height: 120px;
        margin-right: 20px;
        border-radius: 6px;
        overflow: hidden;
        cursor: pointer;
        
        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
          transition: transform 0.3s;
          
          &:hover {
            transform: scale(1.05);
          }
        }
      }
      
      .article-info {
        flex: 1;
        
        .article-title {
          font-size: 18px;
          font-weight: 500;
          color: var(--el-text-color-primary);
          margin-bottom: 10px;
          cursor: pointer;
          
          &:hover {
            color: var(--el-color-primary);
          }
        }
        
        .article-summary {
          font-size: 14px;
          color: var(--el-text-color-regular);
    margin-bottom: 10px;
          line-height: 1.5;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
          overflow: hidden;
        }
        
        .article-meta {
          font-size: 12px;
          color: var(--el-text-color-secondary);
          display: flex;
          align-items: center;
          
          span {
            margin-right: 15px;
            display: flex;
            align-items: center;
            
            .el-icon {
              margin-right: 3px;
            }
          }
        }
      }
      
      .article-actions {
        display: flex;
        align-items: flex-start;
        margin-left: 20px;
      }
    }
  }
  
  // 茶叶商品列表样式
  .products-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
    gap: 20px;
    
    .product-card {
      background-color: #fff;
      border-radius: 8px;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
      overflow: hidden;
      transition: all 0.3s;
      
      &:hover {
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
        transform: translateY(-5px);
      }
      
      .product-cover {
        height: 200px;
        overflow: hidden;
        cursor: pointer;
        
        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
          transition: transform 0.3s;
          
          &:hover {
            transform: scale(1.05);
          }
        }
      }
      
      .product-info {
        padding: 15px;
        
        .product-title {
          font-size: 16px;
          font-weight: 500;
          color: var(--el-text-color-primary);
          margin-bottom: 10px;
          cursor: pointer;
          
          &:hover {
            color: var(--el-color-primary);
          }
        }
        
        .product-shop {
          display: flex;
          align-items: center;
          font-size: 13px;
          color: var(--el-text-color-regular);
          margin-bottom: 10px;
          cursor: pointer;
          
          &:hover {
            color: var(--el-color-primary);
          }
          
          .shop-logo {
            width: 18px;
            height: 18px;
            border-radius: 50%;
            margin-right: 5px;
          }
        }
        
        .product-price {
          font-size: 18px;
          font-weight: 500;
          color: #ff6b6b;
          margin-bottom: 5px;
        }
        
        .favorite-time {
          font-size: 12px;
          color: var(--el-text-color-secondary);
        }
      }
      
      .product-actions {
        display: flex;
        justify-content: space-between;
        padding: 0 15px 15px;
      }
    }
  }
  
  // 论坛帖子列表样式
  .post-list {
    .post-item {
      display: flex;
      justify-content: space-between;
      border-bottom: 1px solid #f0f0f0;
      padding: 20px 0;
      
      &:last-child {
        border-bottom: none;
      }
      
      .post-info {
        flex: 1;
        cursor: pointer;
        
        .post-title {
          font-size: 18px;
          font-weight: 500;
          color: var(--el-text-color-primary);
          margin-bottom: 10px;
          
          &:hover {
            color: var(--el-color-primary);
          }
        }
        
        .post-summary {
          font-size: 14px;
          color: var(--el-text-color-regular);
          margin-bottom: 15px;
          line-height: 1.6;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
          overflow: hidden;
        }
        
        .post-meta {
          display: flex;
          align-items: center;
          flex-wrap: wrap;
          font-size: 12px;
          color: var(--el-text-color-secondary);
          
          .author {
            display: flex;
            align-items: center;
            margin-right: 15px;
            
            &:hover {
              color: var(--el-color-primary);
            }
            
            .author-avatar {
              width: 24px;
              height: 24px;
              border-radius: 50%;
              margin-right: 5px;
            }
          }
          
          .publish-time, .favorite-time {
            margin-right: 15px;
          }
          
          .post-stats {
            display: flex;
            align-items: center;
            
            span {
              display: flex;
              align-items: center;
              margin-right: 10px;
              
              .el-icon {
                margin-right: 3px;
              }
            }
          }
        }
      }
      
      .post-actions {
        margin-left: 20px;
    display: flex;
        align-items: flex-start;
      }
    }
  }
}

// 响应式样式
@media (max-width: 768px) {
  .favorites-page {
    .list-header {
      flex-direction: column;
      align-items: flex-start;
      
      .list-title {
        margin-bottom: 10px;
      }
      
      .list-actions {
        width: 100%;
        flex-wrap: wrap;
        
        .el-input, .el-select {
          margin-bottom: 10px;
          width: 100%;
          margin-left: 0 !important;
        }
      }
    }
    
    .culture-articles {
      .article-item {
    flex-direction: column;
        
        .article-cover {
          width: 100%;
          height: 180px;
          margin-right: 0;
          margin-bottom: 15px;
        }
        
        .article-actions {
          margin-left: 0;
          margin-top: 15px;
          width: 100%;
          justify-content: flex-end;
        }
      }
    }
    
    .products-grid {
      grid-template-columns: 1fr;
    }
    
    .post-list {
      .post-item {
        flex-direction: column;
        
        .post-actions {
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