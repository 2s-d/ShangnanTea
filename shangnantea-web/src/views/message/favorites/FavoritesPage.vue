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
              <el-button size="small" plain type="danger" @click="cancelFavorite('tea_article', article.articleId)">
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
              <el-button size="small" plain type="danger" @click="cancelFavorite('tea', product.teaId)">
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
              <el-button size="small" plain type="danger" @click="cancelFavorite('post', post.postId)">
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
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

import { Search, View, ChatDotRound, Star, ShoppingCart } from '@element-plus/icons-vue'
import SafeImage from '@/components/common/form/SafeImage.vue'
import { showByCode } from '@/utils/apiMessages'

export default {
  name: 'FavoritesPage',
  components: {
    Search, View, ChatDotRound, Star, ShoppingCart, SafeImage
  },
  setup() {
    const router = useRouter()
    const userStore = useUserStore()
    const activeTab = ref('culture')
    
    // 从Pinia获取收藏列表
    const favoriteList = computed(() => userStore.favoriteList || [])
    
    // 茶文化文章相关数据（从Vuex筛选）
    const cultureSearchKeyword = ref('')
    const cultureSortOption = ref('recent')
    const cultureArticles = computed(() => {
      return favoriteList.value
        .filter(item => item.itemType === 'tea_article')
        .map(item => ({
          id: item.id,
          articleId: item.itemId,
          title: item.targetName || '未知文章',
          coverImg: item.targetImage || 'https://via.placeholder.com/400x200?text=文章',
          cover_image: item.targetImage || 'https://via.placeholder.com/400x200?text=文章',
          summary: '', // 后端未提供，使用默认值
          publishTime: item.createTime,
          favoriteTime: item.createTime,
          viewCount: 0 // 后端未提供，使用默认值
        }))
    })
    
    // 筛选和排序茶文化文章
    const filteredCultureArticles = computed(() => {
      let result = [...cultureArticles.value]
      
      // 搜索过滤
      if (cultureSearchKeyword.value) {
        const keyword = cultureSearchKeyword.value.toLowerCase()
        result = result.filter(article => 
          article.title.toLowerCase().includes(keyword) || 
          (article.summary && article.summary.toLowerCase().includes(keyword))
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
    
    // 茶叶商品相关数据（从Vuex筛选）
    const productSearchKeyword = ref('')
    const productSortOption = ref('recent')
    const products = computed(() => {
      return favoriteList.value
        .filter(item => item.itemType === 'tea')
        .map(item => ({
          id: item.id,
          teaId: item.itemId,
          name: item.targetName || '未知茶叶',
          image: item.targetImage || 'https://via.placeholder.com/200x200?text=茶叶',
          price: 0, // 后端未提供，使用默认值
          shopId: '', // 后端未提供，使用默认值
          shopName: '', // 后端未提供，使用默认值
          shop_logo: '', // 后端未提供，使用默认值
          favoriteTime: item.createTime
        }))
    })
    
    // 筛选和排序茶叶商品
    const filteredProducts = computed(() => {
      let result = [...products.value]
      
      // 搜索过滤
      if (productSearchKeyword.value) {
        const keyword = productSearchKeyword.value.toLowerCase()
        result = result.filter(product => 
          product.name.toLowerCase().includes(keyword) || 
          (product.shopName && product.shopName.toLowerCase().includes(keyword))
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
    
    // 论坛帖子相关数据（从Vuex筛选）
    const postSearchKeyword = ref('')
    const postSortOption = ref('recent')
    const posts = computed(() => {
      return favoriteList.value
        .filter(item => item.itemType === 'post')
        .map(item => ({
          id: item.id,
          postId: item.itemId,
          title: item.targetName || '未知帖子',
          content: '', // 后端未提供，使用默认值
          authorId: '', // 后端未提供，使用默认值
          authorName: '', // 后端未提供，使用默认值
          author_avatar: '', // 后端未提供，使用默认值
          publishTime: item.createTime,
          favoriteTime: item.createTime,
          viewCount: 0, // 后端未提供，使用默认值
          replyCount: 0, // 后端未提供，使用默认值
          likeCount: 0 // 后端未提供，使用默认值
        }))
    })
    
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
    const formatDate = dateString => {
      if (!dateString) return ''
      
      const date = new Date(dateString)
      return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
    }
    
    // 跳转到文章详情
    const goToArticleDetail = articleId => {
      router.push(`/culture/article/${articleId}`)
    }
    
    // 跳转到商品详情
    const goToProductDetail = productId => {
      router.push(`/tea/${productId}`)
    }
    
    // 跳转到店铺详情
    const goToShopDetail = shopId => {
      router.push(`/shop/${shopId}`)
    }
    
    // 跳转到帖子详情
    const goToPostDetail = postId => {
      router.push(`/forum/${postId}`)
    }
    
    // 跳转到用户主页
    const goToUserProfile = userId => {
      router.push(`/profile/${userId}`)
    }
    
    /**
     * 加入购物车
     * - 必须走真实 API / Vuex Action，返回 code 再用状态码消息系统提示
     * - 禁止在未调用接口时伪造成功码
     */
    const addToCart = async teaId => {
      try {
        // Favorites 列表目前只拿到了 teaId，规格/数量这里先用默认值
        const res = await store.dispatch('order/addToCart', {
          teaId: String(teaId),
          quantity: 1,
          specificationId: null
        })
        showByCode(res.code)
      } catch (error) {
        // 网络错误等由拦截器处理，这里仅保留开发日志
        if (process.env.NODE_ENV === 'development') {
          console.error('加入购物车失败:', error)
        }
      }
    }
    
    // 取消收藏
    const cancelFavorite = async (itemType, itemId) => {
      try {
        const res = await userStore.removeFavorite({
          itemId: String(itemId),
          itemType: itemType
        })
        // 使用状态码消息系统，符合项目规范
        showByCode(res.code)
        // 重新加载收藏列表以更新状态
        await loadFavoriteList()
      } catch (error) {
        // 网络错误等已由响应拦截器处理，这里只记录日志
        if (process.env.NODE_ENV === 'development') {
          console.error('取消收藏失败:', error)
        }
      }
    }
    
    // 加载收藏列表
    const loadFavoriteList = async () => {
      try {
        const response = await userStore.fetchFavoriteList()
        // 显示API响应消息（成功或失败都通过状态码映射显示）
        showByCode(response.code)
      } catch (error) {
        // 捕获意外的运行时错误（非API业务错误）
        // API业务失败已通过 showByCode 显示，网络错误已在响应拦截器显示
        // 这里只记录日志用于开发调试
        if (process.env.NODE_ENV === 'development') {
          console.error('[开发调试] 加载收藏列表时发生意外错误：', error)
        }
      }
    }
    
    // 组件挂载时加载数据
    onMounted(() => {
      loadFavoriteList()
    })
    
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
      loadFavoriteList
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
          line-clamp: 2;
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
          line-clamp: 2;
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