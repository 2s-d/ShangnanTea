<template>
  <div class="favorites-page">
    <!-- 顶部横栏（同“关注”页样式）：统计放在横栏上 -->
    <div class="filter-bar">
      <div class="filter-tabs">
        <el-radio-group v-model="activeTab">
          <el-radio-button value="culture">茶文化内容 ({{ cultureArticles.length }})</el-radio-button>
          <el-radio-button value="product">茶叶商品 ({{ products.length }})</el-radio-button>
          <el-radio-button value="post">论坛帖子 ({{ posts.length }})</el-radio-button>
        </el-radio-group>
      </div>
      <div class="filter-actions">
        <template v-if="activeTab === 'culture'">
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
        </template>

        <template v-else-if="activeTab === 'product'">
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
        </template>

        <template v-else>
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
        </template>
          </div>
        </div>
        
    <!-- 茶文化内容 -->
    <template v-if="activeTab === 'culture'">
        <el-empty v-if="filteredCultureArticles.length === 0" description="暂无收藏内容" />
        
        <div v-else class="culture-articles">
          <div v-for="article in filteredCultureArticles" :key="article.id" class="article-item">
            <div class="article-cover" @click="goToArticleDetail(article.articleId)">
              <SafeImage :src="article.cover_image" type="post" :alt="article.title" style="width:100%;height:100%;object-fit:cover;" />
            </div>
            <div class="article-info">
              <div class="article-title" @click="goToArticleDetail(article.articleId)">{{ article.title }}</div>
              <div class="article-summary">{{ article.summary }}</div>
              <div class="article-meta">
                <span class="publish-time">发布于 {{ formatDate(article.publishTime) }}</span>
                <span class="favorite-time">收藏于 {{ formatDate(article.favoriteTime) }}</span>
                <span class="view-count"><el-icon><View /></el-icon> {{ article.viewCount }}</span>
              </div>
            </div>
            <div class="article-actions">
              <el-button
                size="small"
                plain
                :type="isLocallyFavorited('tea_article', article.articleId) ? 'danger' : 'primary'"
                @click="toggleFavorite('tea_article', article.articleId, article.title, article.cover_image)"
              >
                {{ isLocallyFavorited('tea_article', article.articleId) ? '取消收藏' : '收藏' }}
              </el-button>
            </div>
          </div>
        </div>
              </template>

    <!-- 茶叶商品 -->
    <template v-else-if="activeTab === 'product'">
        <el-empty v-if="filteredProducts.length === 0" description="暂无收藏茶叶" />
        
        <div v-else class="products-grid">
          <div v-for="product in filteredProducts" :key="product.id" class="product-card">
            <div class="product-cover" @click="goToProductDetail(product.teaId)">
              <SafeImage :src="product.image" type="tea" :alt="product.name" style="width:100%;height:100%;object-fit:cover;" />
            </div>
            <div class="product-info">
              <div class="product-title" @click="goToProductDetail(product.teaId)">{{ product.name }}</div>
              <!-- 平台直售：不跳转店铺，展示平台信息（对齐 TeaDetailPage） -->
              <div v-if="isPlatformTea(product)" class="product-shop platform-shop">
                <SafeImage
                  src="/images/tea-logo.png"
                  type="avatar"
                  :alt="'平台直售'"
                  class="shop-logo"
                  style="width:20px;height:20px;border-radius:50%;object-fit:cover;"
                />
                <span>商南茶文化平台</span>
                <el-tag class="platform-tag" type="danger" size="small">平台直售</el-tag>
              </div>
              <!-- 店铺茶叶：允许跳转店铺详情 -->
              <div v-else class="product-shop" @click="goToShopDetail(product.shopId)">
                <SafeImage
                  :src="product.shopLogo || product.shop?.logo"
                  type="banner"
                  :alt="product.shopName"
                  class="shop-logo"
                  style="width:20px;height:20px;border-radius:50%;object-fit:cover;"
                />
                <span>{{ product.shopName }}</span>
              </div>
              <div class="product-price">¥{{ product.price.toFixed(2) }}</div>
              <div class="favorite-time">收藏于 {{ formatDate(product.favoriteTime) }}</div>
            </div>
            <div class="product-actions">
              <el-button size="small" type="primary" @click="addToCart(product.teaId)">
                <el-icon><ShoppingCart /></el-icon> 加入购物车
              </el-button>
              <el-button
                size="small"
                plain
                :type="isLocallyFavorited('tea', product.teaId) ? 'danger' : 'primary'"
                @click="toggleFavorite('tea', product.teaId, product.name, product.image)"
              >
                {{ isLocallyFavorited('tea', product.teaId) ? '取消收藏' : '收藏' }}
              </el-button>
            </div>
          </div>
        </div>
              </template>

    <!-- 论坛帖子 -->
    <template v-else>
        <el-empty v-if="filteredPosts.length === 0" description="暂无收藏帖子" />
        
        <div v-else class="post-list">
          <div v-for="post in filteredPosts" :key="post.id" class="post-item">
            <div class="post-info" @click="goToPostDetail(post.postId)">
              <div class="post-title">{{ post.title }}</div>
              <div class="post-summary">{{ post.content }}</div>
              <div class="post-meta">
                <span class="author" @click.stop="goToUserProfile(post.userId)">
                  <SafeImage :src="post.userAvatar" type="avatar" :alt="post.nickname" class="author-avatar" style="width:24px;height:24px;border-radius:50%;object-fit:cover;" />
                  <span class="author-name">{{ post.nickname }}</span>
                </span>
                <span class="publish-time">发布于 {{ formatDate(post.publishTime) }}</span>
                <span class="favorite-time">收藏于 {{ formatDate(post.favoriteTime) }}</span>
                <div class="post-stats">
                  <span><el-icon><View /></el-icon> {{ post.viewCount }}</span>
                  <span><el-icon><ChatDotRound /></el-icon> {{ post.replyCount }}</span>
                  <span><el-icon><Star /></el-icon> {{ post.favoriteCount }}</span>
                </div>
              </div>
            </div>
            <div class="post-actions">
              <el-button
                size="small"
                plain
                :type="isLocallyFavorited('post', post.postId) ? 'danger' : 'primary'"
                @click="toggleFavorite('post', post.postId, post.title, '')"
              >
                {{ isLocallyFavorited('post', post.postId) ? '取消收藏' : '收藏' }}
              </el-button>
            </div>
          </div>
        </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useMessageStore } from '@/stores/message'
import { useOrderStore } from '@/stores/order'
import { getFavoriteList } from '@/api/user'

import { Search, View, ChatDotRound, Star, ShoppingCart } from '@element-plus/icons-vue'
import SafeImage from '@/components/common/form/SafeImage.vue'
import { showByCode } from '@/utils/apiMessages'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const messageStore = useMessageStore()
const orderStore = useOrderStore()
const activeTab = ref('culture')

// 当前查看的主页用户ID：与 UserHomePage / FollowsPage 规则保持一致
const profileUserId = computed(() => {
  const firstParam = route.params.userId
  if (!firstParam || firstParam === 'current') return null
  if (firstParam === 'published' || firstParam === 'follows' || firstParam === 'favorites') return null
  return firstParam
})

// 当前登录用户ID
const currentUserId = computed(() => {
  const base = userStore.userInfo || {}
  return base.id || base.userId || null
})

    /**
 * 收藏列表本地状态管理（参考关注页逻辑）
 * - profileFavoriteList: 被查看用户的收藏列表（用于显示列表内容）
 * - viewerFavoriteList: 当前登录用户的收藏列表（用于判断按钮状态）
 * - localFavoriteState: 本地乐观更新状态（用于按钮即时切换）
 */
const profileFavoriteList = ref([]) // 被查看用户的收藏列表
const viewerFavoriteList = ref([]) // 当前登录用户的收藏列表
const localFavoriteState = ref({}) // 本地状态：{ "tea_article:123": true, "tea:456": false }

const favoriteKey = (itemType, itemId) => `${itemType}:${String(itemId)}`

// 当前登录用户的收藏集合（用于判断按钮状态）
const viewerFavoriteSet = computed(() => {
  const set = new Set()
  ;(viewerFavoriteList.value || []).forEach((item) => {
    if (!item) return
    set.add(favoriteKey(item.itemType, item.itemId))
  })
  return set
})

/**
 * 当前登录用户是否收藏该项目（按钮状态的唯一依据）
 * - 默认来自 viewerFavoriteSet
 * - 若本页做过切换，则用 localFavoriteState 覆盖（乐观更新 + 回滚）
 */
const isLocallyFavorited = (itemType, itemId) => {
  const key = favoriteKey(itemType, itemId)
  if (Object.prototype.hasOwnProperty.call(localFavoriteState.value, key)) {
    return !!localFavoriteState.value[key]
  }
  return viewerFavoriteSet.value.has(key)
}

const setLocallyFavorited = (itemType, itemId, favorited) => {
  localFavoriteState.value[favoriteKey(itemType, itemId)] = !!favorited
}

// 使用被查看用户的收藏列表渲染（列表内容来自"他"）
const favoriteList = computed(() => profileFavoriteList.value || [])
    
    // 茶文化文章相关数据
    const cultureSearchKeyword = ref('')
    const cultureSortOption = ref('recent')
    const cultureArticles = computed(() => {
      return favoriteList.value
        .filter(item => item.itemType === 'tea_article')
        .map(item => ({
          id: item.id,
          articleId: item.itemId,
          title: item.articleTitle || item.targetName || '未知文章',
          coverImg: item.articleCoverImage || item.targetImage || 'https://via.placeholder.com/400x200?text=文章',
          cover_image: item.articleCoverImage || item.targetImage || 'https://via.placeholder.com/400x200?text=文章',
          summary: item.articleSummary || '',
          publishTime: item.articlePublishTime || item.createTime,
          favoriteTime: item.createTime,
          viewCount: item.articleViewCount ?? 0
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
    
    // 茶叶商品相关数据
    const productSearchKeyword = ref('')
    const productSortOption = ref('recent')
    const products = computed(() => {
      return favoriteList.value
        .filter(item => item.itemType === 'tea')
        .map(item => ({
          id: item.id,
          teaId: item.itemId,
          name: item.teaName || item.targetName || '未知茶叶',
          image: item.teaMainImage || item.targetImage || 'https://via.placeholder.com/200x200?text=茶叶',
          price: item.teaPrice ?? 0,
          shopId: item.teaShopId || '',
          shopName: item.teaShopName || '',
          shopLogo: item.teaShopLogo || '',
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
    
    // 论坛帖子相关数据
    const postSearchKeyword = ref('')
    const postSortOption = ref('recent')
    const posts = computed(() => {
      return favoriteList.value
        .filter(item => item.itemType === 'post')
        .map(item => ({
          id: item.id,
          postId: item.itemId,
          title: item.postTitle || item.targetName || '未知帖子',
          content: item.postSummary || '',
          userId: item.postUserId || '',
          userName: item.postNickname || '',
          userAvatar: item.postUserAvatar || '',
          publishTime: item.postPublishTime || item.createTime,
          favoriteTime: item.createTime,
          viewCount: item.postViewCount ?? 0,
          replyCount: item.postReplyCount ?? 0,
          favoriteCount: item.postFavoriteCount ?? 0,
          likeCount: item.postLikeCount ?? 0
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
      router.push(`/article/${articleId}`)
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
     * - 必须走真实 API / Pinia Action，返回 code 再用状态码消息系统提示
     * - 禁止在未调用接口时伪造成功码
     */
    const addToCart = async teaId => {
      try {
        // Favorites 列表目前只拿到了 teaId，规格/数量这里先用默认值
        const res = await orderStore.addToCart({
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
    
    /**
     * 切换收藏状态（参考关注页的 toggleFollowUser/toggleFollowShop 逻辑）
     * - 改变的是"当前登录用户"和"该项目"的关系
     * - 列表项不会消失（因为列表是"他的收藏"，不是"我的收藏"）
     */
    const toggleFavorite = async (itemType, itemId, targetName = '', targetImage = '') => {
      const currentlyFavorited = isLocallyFavorited(itemType, itemId)
      // 先乐观更新UI，避免"接口成功但按钮样式不变"
      setLocallyFavorited(itemType, itemId, !currentlyFavorited)
      try {
        if (currentlyFavorited) {
          // 取消收藏
          const res = await userStore.removeFavorite({
            itemId: String(itemId),
            itemType: itemType
          })
          showByCode(res.code)
          // 如果失败，回滚UI状态
          if (!(res.code === 2015 || String(res.code).startsWith('7'))) {
            setLocallyFavorited(itemType, itemId, true)
          }
        } else {
          // 添加收藏
          const res = await userStore.addFavorite({
            itemId: String(itemId),
            itemType: itemType,
            targetName: targetName,
            targetImage: targetImage
          })
          showByCode(res.code)
          // 如果失败，回滚UI状态
          if (!(res.code === 2014 || String(res.code).startsWith('7'))) {
            setLocallyFavorited(itemType, itemId, false)
          }
        }
      } catch (error) {
        // 网络/异常：回滚UI
        setLocallyFavorited(itemType, itemId, currentlyFavorited)
        if (process.env.NODE_ENV === 'development') {
          console.error('切换收藏状态失败:', error)
        }
      }
    }
    
    // 兼容旧方法名（取消收藏）
    const cancelFavorite = async (itemType, itemId) => {
      await toggleFavorite(itemType, itemId)
    }
    
    /**
     * 加载收藏列表：同时加载两份数据
     * 1. 被查看用户的收藏列表（用于显示列表内容）
     * 2. 当前登录用户的收藏列表（用于判断按钮状态）
     * 参考统计接口的处理：仅在主页可见时才调用接口
     */
    const loadFavoriteList = async () => {
      // 参考统计接口的处理逻辑：判断主页是否可见
      const isSelf = currentUserId.value && profileUserId.value && String(profileUserId.value) === String(currentUserId.value)
      const profileVisible = messageStore.userProfile?.profileVisible !== false
      
      // 仅在本人或对方允许查看时才请求收藏接口；否则直接置空并不发请求
      if (!isSelf && !profileVisible) {
        profileFavoriteList.value = []
        viewerFavoriteList.value = []
        localFavoriteState.value = {}
        return
      }
      
      try {
        // 1) 被查看用户的收藏：用于渲染"他收藏了什么"
        if (profileUserId.value) {
          const resOwner = await getFavoriteList(null, profileUserId.value)
          profileFavoriteList.value = (resOwner.data || []).map(i => ({ ...i }))
        } else {
          // 看自己的主页：列表内容直接是当前登录用户
          const resOwner = await getFavoriteList()
          profileFavoriteList.value = (resOwner.data || []).map(i => ({ ...i }))
        }

        // 2) 当前登录用户的收藏：用于按钮状态（我是否收藏该条目）
        // - 看别人的主页才需要单独再拉一份
        if (profileUserId.value && profileUserId.value !== currentUserId.value) {
          const resViewer = await getFavoriteList()
          viewerFavoriteList.value = (resViewer.data || []).map(i => ({ ...i }))
        } else {
          // 看自己：两份相同，但仍做快照拷贝，避免被后续 Pinia 状态变更影响
          viewerFavoriteList.value = (profileFavoriteList.value || []).map(i => ({ ...i }))
        }

        // 切换用户/刷新时：清空本页的本地切换态，让"矫正"以最新后端结果为准
        localFavoriteState.value = {}
      } catch (error) {
        if (process.env.NODE_ENV === 'development') {
          console.error('[开发调试] 加载收藏列表时发生意外错误：', error)
        }
      }
    }
    
// 组件挂载时加载数据
onMounted(() => {
  loadFavoriteList()
})

/**
 * 是否为平台直售茶叶（对齐 TeaDetailPage 的判定逻辑）
 * @param {Object} product 收藏页茶叶卡片数据
 * @returns {boolean} 是否平台直售
 */
const isPlatformTea = (product) => {
  const sid = product?.shopId
  return !sid || sid === '0' || sid === 'PLATFORM'
}

// 查看不同用户主页时，刷新收藏列表并清空本页的本地切换态
watch(() => profileUserId.value, () => {
  localFavoriteState.value = {}
  loadFavoriteList()
})
</script>

<style lang="scss" scoped>
.favorites-page {
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
  
  // 茶文化文章列表样式
  .culture-articles {
    .article-item {
      display: flex;
      padding: 20px;
      margin-bottom: 16px;
      background-color: #fff;
      border: 1px solid #ebeef5;
      border-radius: 10px;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
      transition: all 0.3s ease;
      
      &:hover {
        box-shadow: 0 6px 18px rgba(0, 0, 0, 0.12);
        transform: translateY(-2px);
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
      padding: 20px;
      margin-bottom: 16px;
      background-color: #fff;
      border: 1px solid #ebeef5;
      border-radius: 10px;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
      transition: all 0.3s ease;
      
      &:hover {
        box-shadow: 0 6px 18px rgba(0, 0, 0, 0.12);
        transform: translateY(-2px);
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
    .filter-bar {
      flex-direction: column;
      align-items: flex-start;
      
      .filter-actions {
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