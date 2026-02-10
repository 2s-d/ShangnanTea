<template>
  <div class="shop-detail-page" v-loading="loading">
    <div class="container" v-if="shop">
      <!-- 返回按钮 -->
      <div class="page-actions">
        <el-button @click="goBack" class="back-button">
          <el-icon><Back /></el-icon> 返回店铺列表
        </el-button>
      </div>
      
      <!-- 店铺基本信息 -->
      <div class="shop-header">
          <div class="shop-info">
          <div class="shop-logo">
            <SafeImage :src="shop.logo || shop.shop_logo" type="banner" :alt="shop.name || shop.shop_name || '商铺'" style="width:100px;height:100px;border-radius:50%;" />
            <div v-if="shop.certification && shop.certification.status === 'verified'" class="cert-badge">
              <el-tooltip content="已认证商家" placement="top">
                <el-icon><Check /></el-icon>
              </el-tooltip>
            </div>
          </div>
          
          <div class="shop-basic-info">
            <div class="shop-name-row">
              <h1 class="shop-name">{{ shop.name || shop.shop_name }}</h1>
              
              <div class="shop-actions">
              <el-button 
                :type="isFollowing ? 'primary' : 'default'"
                @click="toggleFollow"
                :loading="followLoading"
                  class="action-button"
              >
                <el-icon><Star /></el-icon> {{ isFollowing ? '已关注' : '关注店铺' }}
              </el-button>
                
                <el-button 
                  type="success"
                  @click="contactShop"
                  class="action-button"
                >
                  <el-icon><ChatLineRound /></el-icon> 联系客服
                </el-button>
              </div>
            </div>
            
            <div class="shop-rating">
              <el-rate
                v-model="shop.rating"
                disabled
                text-color="#ff9900"
                score-template="{value}"
              />
              <span class="rating-text">{{ shop.rating.toFixed(1) }}</span>
              <span class="rating-count">({{ shop.rating_count }})</span>
            </div>
            
            <div class="shop-brief">{{ shop.desc || shop.description }}</div>
            
            <div class="shop-stats">
              <div class="stat-item">
                <span class="stat-label">商品数</span>
                <span class="stat-value">{{ shopTeas.length }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">销量</span>
                <span class="stat-value">{{ shop.sales_count || shop.salesCount || 0 }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">关注数</span>
                <span class="stat-value">{{ shop.follower_count || shop.followerCount || 0 }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">开店时间</span>
                <span class="stat-value">{{ formatTime(shop.create_time || shop.createTime) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 任务组B：Banner轮播展示 -->
      <div class="shop-banner-section" v-if="shopBanners.length > 0">
        <el-carousel :interval="4000" type="card" height="300px">
          <el-carousel-item v-for="banner in shopBanners" :key="banner.id">
            <div class="banner-item" @click="handleBannerClick(banner)">
              <SafeImage 
                :src="banner.image_url" 
                type="banner" 
                :alt="banner.title"
                style="width: 100%; height: 100%; object-fit: cover; cursor: pointer;"
              />
              <div class="banner-overlay" v-if="banner.title">
                <div class="banner-title">{{ banner.title }}</div>
              </div>
            </div>
          </el-carousel-item>
        </el-carousel>
      </div>
      
      <!-- 任务组B：公告列表展示 -->
      <div class="shop-announcements-section" v-if="shopAnnouncements.length > 0">
        <el-card shadow="never">
          <template #header>
            <div class="announcements-header">
              <span class="announcements-title">
                <el-icon><Bell /></el-icon> 店铺公告
              </span>
            </div>
          </template>
          
          <div class="announcements-list">
            <div 
              v-for="announcement in shopAnnouncements" 
              :key="announcement.id" 
              class="announcement-item"
              :class="{ 'is-top': announcement.is_top }"
            >
              <div class="announcement-header">
                <div class="announcement-title-row">
                  <el-tag v-if="announcement.is_top" type="warning" size="small">置顶</el-tag>
                  <span class="announcement-title">{{ announcement.title }}</span>
                </div>
                <span class="announcement-time">{{ formatTime(announcement.create_time) }}</span>
              </div>
              <div class="announcement-content">{{ announcement.content }}</div>
            </div>
          </div>
        </el-card>
      </div>
      
      <!-- 详情内容 -->
      <div class="shop-content">
        <el-tabs v-model="activeTab" class="shop-tabs">
          <el-tab-pane label="店铺商品" name="products">
            <div class="products-container">
              <div v-if="shopTeas.length > 0" class="tea-grid">
                <div v-for="tea in shopTeas" :key="tea.id" class="tea-item">
                  <tea-card :tea="tea" />
                </div>
              </div>
              <div v-else class="empty-products">
                <el-empty description="暂无商品" />
              </div>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="店铺评价" name="reviews">
            <div class="shop-reviews-container">
              <!-- 评价列表 -->
              <div v-if="shopReviews.length > 0" class="reviews-list">
                <div v-for="review in shopReviews" :key="review.id" class="review-item">
                  <div class="review-header">
                    <div class="review-user">
                      <el-avatar :src="review.avatar" :size="40">{{ review.username?.charAt(0) }}</el-avatar>
                      <div class="user-info">
                        <div class="username">{{ review.username }}</div>
                        <div class="review-time">{{ formatTime(review.createTime) }}</div>
                      </div>
                    </div>
                    <el-rate
                      v-model="review.rating"
                      disabled
                      text-color="#ff9900"
                      score-template="{value}"
                    />
                  </div>
                  <div class="review-content">{{ review.content }}</div>
                  <div v-if="review.images && review.images.length > 0" class="review-images">
                    <SafeImage
                      v-for="(img, idx) in review.images"
                      :key="idx"
                      :src="img"
                      type="banner"
                      style="width: 80px; height: 80px; margin-right: 8px; cursor: pointer;"
                      @click="handleImagePreview(img)"
                    />
                  </div>
                  <div v-if="review.reply" class="review-reply">
                    <div class="reply-header">商家回复：</div>
                    <div class="reply-content">{{ review.reply }}</div>
                    <div class="reply-time">{{ formatTime(review.replyTime) }}</div>
                  </div>
                </div>
              </div>
              <div v-else class="empty-reviews">
                <el-empty description="暂无评价" />
              </div>
              
              <!-- 分页 -->
              <div v-if="reviewPagination.total > 0" class="review-pagination">
                <el-pagination
                  background
                  layout="total, prev, pager, next"
                  :total="reviewPagination.total"
                  :page-size="reviewPagination.pageSize"
                  :current-page="reviewPagination.currentPage"
                  @current-change="handleReviewPageChange"
                />
              </div>
              
              <!-- 提交评价表单 -->
              <div class="submit-review-section">
                <el-card shadow="never">
                  <template #header>
                    <span>发表评价</span>
                  </template>
                  <el-form :model="reviewForm" label-width="80px">
                    <el-form-item label="评分">
                      <el-rate v-model="reviewForm.rating" />
                    </el-form-item>
                    <el-form-item label="评价内容">
                      <el-input
                        v-model="reviewForm.content"
                        type="textarea"
                        :rows="4"
                        placeholder="请输入您的评价..."
                        maxlength="500"
                        show-word-limit
                      />
                    </el-form-item>
                    <el-form-item>
                      <el-button type="primary" @click="handleSubmitReview" :loading="reviewSubmitting">
                        提交评价
                      </el-button>
                    </el-form-item>
                  </el-form>
                </el-card>
              </div>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="店铺信息" name="info">
            <div class="shop-detail-info">
              <el-descriptions title="基本信息" :column="1" border>
                <el-descriptions-item label="店铺名称">{{ shop.name || shop.shop_name }}</el-descriptions-item>
                <el-descriptions-item label="店铺介绍">{{ shop.desc || shop.description }}</el-descriptions-item>
                <el-descriptions-item label="联系电话">{{ shop.contact_phone || shop.contactPhone || '未设置' }}</el-descriptions-item>
                <el-descriptions-item label="所在地区">
                  {{ `${shop.province || ''} ${shop.city || ''} ${shop.district || ''}`.trim() || '未设置' }}
                </el-descriptions-item>
                <el-descriptions-item label="详细地址">{{ shop.address || '未设置' }}</el-descriptions-item>
                <el-descriptions-item label="营业状态">
                  <el-tag :type="shop.status === 1 ? 'success' : 'info'">
                    {{ shop.status === 1 ? '营业中' : '休息中' }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="认证状态">
                  <el-tag :type="shop.certification?.status === 'verified' ? 'success' : 'warning'">
                    {{ shop.certification?.status === 'verified' ? '已认证' : '认证中' }}
                  </el-tag>
                </el-descriptions-item>
              </el-descriptions>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
    
    <!-- 空状态 -->
    <div v-else-if="!loading" class="empty-state">
      <el-empty description="未找到店铺信息" />
      <el-button type="primary" @click="goToShopList">返回店铺列表</el-button>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useStore } from 'vuex'
import { useUserStore } from '@/stores/user'

import { Back, Check, Star, ChatLineRound, Bell } from '@element-plus/icons-vue'
import TeaCard from '@/components/tea/card/TeaCard.vue'
import SafeImage from '@/components/common/form/SafeImage.vue'
import { showByCode, isSuccess } from '@/utils/apiMessages'
import { shopPromptMessages } from '@/utils/promptMessages'

export default {
  name: 'ShopDetailPage',
  components: {
    Back,
    Check,
    Star,
    ChatLineRound,
    Bell,
    TeaCard,
    SafeImage
  },
  setup() {
    const store = useStore()
    const userStore = useUserStore()
    const router = useRouter()
    const route = useRoute()
    const loading = computed(() => store.state.shop.loading)
    const activeTab = ref('products')
    const shop = computed(() => store.state.shop.currentShop)
    const shopTeas = computed(() => store.state.shop.shopTeas || [])
    // 任务组B：Banner和公告
    const shopBanners = computed(() => store.state.shop.shopBanners || [])
    const shopAnnouncements = computed(() => store.state.shop.shopAnnouncements || [])

    // 关注状态（从接口返回的isFollowed字段获取）
    const isFollowing = computed(() => shop.value?.isFollowed || false)
    const followLoading = ref(false)
    const defaultLogo = '/placeholder-shop.jpg'
    const defaultShopLogo = '/images/shops/default.jpg'
    
    // 店铺评价相关
    const shopReviews = computed(() => store.state.shop.shopReviews || [])
    const reviewPagination = computed(() => store.state.shop.reviewPagination)
    const reviewForm = ref({
      rating: 5,
      content: ''
    })
    const reviewSubmitting = ref(false)
    
    const loadShopDetail = async shopId => {
      if (!shopId) return

      if (shopId === 'PLATFORM' || shopId === '0') {
        shopPromptMessages.showShopIdNotExist()
        router.push('/shop/list')
        return
      }

      try {
        await store.dispatch('shop/fetchShopDetail', shopId)
        await store.dispatch('shop/fetchShopTeas', { shopId, params: { page: 1, size: 20 } })
        // 任务组B：加载Banner和公告
        await store.dispatch('shop/fetchShopBanners', shopId)
        await store.dispatch('shop/fetchShopAnnouncements', shopId)
        // 注意：关注状态已由fetchShopDetail接口返回的isFollowed字段提供，无需单独调用checkFollowStatus
        // 加载店铺评价
        await store.dispatch('shop/fetchShopReviews', {
          shopId,
          page: 1,
          size: 10
        })
      } catch (error) {
        console.error('加载店铺详情失败:', error)
      }
    }
    
    // 任务组B：Banner点击处理
    const handleBannerClick = banner => {
      if (banner.link_url) {
        router.push(banner.link_url)
      }
    }
    
    // 监听路由参数变化
    watch(
      () => route.params.id,
      shopId => {
        if (shopId) {
          loadShopDetail(shopId)
        }
      },
      { immediate: true }
    )

    const toggleFollow = async () => {
      if (!shop.value) return
      
      followLoading.value = true
      try {
        if (isFollowing.value) {
          // 取消关注：直接传递 targetId 和 targetType
          const response = await userStore.removeFollow({
            targetId: shop.value.id,
            targetType: 'shop'
          })
          showByCode(response.code)
          // 重新加载店铺详情以更新isFollowed状态
          await store.dispatch('shop/fetchShopDetail', shop.value.id)
        } else {
          // 添加关注
          const response = await userStore.addFollow({
            targetId: shop.value.id,
            targetType: 'shop',
            targetName: shop.value.name || shop.value.shop_name,
            targetAvatar: shop.value.logo || shop.value.shop_logo
          })
          showByCode(response.code)
          // 重新加载店铺详情以更新isFollowed状态
          await store.dispatch('shop/fetchShopDetail', shop.value.id)
        }
      } catch (error) {
        console.error('关注操作失败:', error)
      } finally {
        followLoading.value = false
      }
    }

    // 格式化时间
    const formatTime = timeString => {
      if (!timeString) return '未知'
      const date = new Date(timeString)
      return `${date.getFullYear()}-${date.getMonth() + 1}-${date.getDate()}`
    }

    // 联系店铺客服
    const contactShop = () => {
      router.push(`/message/center/chat?shopId=${route.params.id}`)
    }
    
    // 评价分页切换
    const handleReviewPageChange = async page => {
      const shopId = route.params.id
      if (!shopId) return
      try {
        await store.dispatch('shop/fetchShopReviews', {
          shopId,
          page,
          size: reviewPagination.value.pageSize
        })
      } catch (error) {
        console.error('加载评价失败:', error)
      }
    }
    
    // 提交评价
    const handleSubmitReview = async () => {
      const shopId = route.params.id
      if (!shopId) {
        shopPromptMessages.showShopIdNotExist()
        return
      }
      // 评分验证（评分是必须的）
      if (reviewForm.value.rating === 0) {
        shopPromptMessages.showReviewRatingRequired()
        return
      }
      // 评价内容是可选的（店铺评分不需要详细内容）
      
      reviewSubmitting.value = true
      try {
        const response = await store.dispatch('shop/submitShopReview', {
          shopId,
          reviewData: {
            rating: reviewForm.value.rating,
            content: reviewForm.value.content.trim() || '', // 内容可选
            images: []
          }
        })
        showByCode(response.code)
        reviewForm.value = {
          rating: 5,
          content: ''
        }
        // 重新加载评价列表
        await store.dispatch('shop/fetchShopReviews', {
          shopId,
          page: 1,
          size: reviewPagination.value.pageSize
        })
      } catch (error) {
        console.error('提交评价失败:', error)
      } finally {
        reviewSubmitting.value = false
      }
    }
    
    // 图片预览
    const handleImagePreview = img => {
      // 可以添加图片预览功能
      console.log('预览图片:', img)
    }
    
    // 返回上一页
    const goBack = () => {
      router.back()
    }
    
    // 返回店铺列表
    const goToShopList = () => {
      router.push('/shop/list')
    }
    
    onMounted(() => {
      loadShopDetail(route.params.id)
    })

    watch(
      () => route.params.id,
      shopId => {
        loadShopDetail(shopId)
      }
    )
    
    return {
      shop,
      shopTeas,
      loading,
      activeTab,
      defaultLogo,
      defaultShopLogo,
      isFollowing,
      followLoading,
      // 任务组B：Banner和公告
      shopBanners,
      shopAnnouncements,
      handleBannerClick,
      formatTime,
      toggleFollow,
      goBack,
      goToShopList,
      contactShop,
      shopReviews,
      reviewPagination,
      reviewForm,
      reviewSubmitting,
      handleReviewPageChange,
      handleSubmitReview,
      handleImagePreview
    }
  }
}
</script>

<style lang="scss" scoped>
.shop-detail-page {
  min-height: 100vh;
  background-color: #f5f7fa;
  padding: 20px 0 40px;
  
  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 15px;
  }
  
  .page-actions {
    margin-bottom: 20px;
    
    .back-button {
      display: flex;
      align-items: center;
      
      .el-icon {
        margin-right: 5px;
      }
    }
  }
  
  .shop-header {
    background-color: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
    padding: 20px;
    margin-bottom: 20px;
    
    .shop-info {
      display: flex;
      margin-bottom: 20px;
      
      .shop-logo {
        position: relative;
        margin-right: 20px;
        
        .cert-badge {
          position: absolute;
          right: 0;
          bottom: 0;
          background-color: var(--el-color-success);
          color: white;
          border-radius: 50%;
          width: 24px;
          height: 24px;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 14px;
        }
      }
      
      .shop-basic-info {
        flex: 1;
        
        .shop-name-row {
          display: flex;
          align-items: flex-start;
          justify-content: space-between;
          margin-bottom: 10px;
          
          .shop-name {
            font-size: 22px;
            font-weight: 600;
            margin: 0;
            color: var(--el-text-color-primary);
          }
          
          .shop-actions {
            display: flex;
            gap: 10px;
            
            .action-button {
              margin-top: 10px;
            }
          }
        }
        
        .shop-rating {
          display: flex;
          align-items: center;
          margin-bottom: 15px;
          
          .rating-text {
            margin-left: 5px;
            font-weight: bold;
            color: #ff9900;
          }
          
          .rating-count {
            margin-left: 5px;
            color: var(--el-text-color-secondary);
            font-size: 14px;
          }
        }
        
        .shop-brief {
          color: var(--el-text-color-regular);
          margin-bottom: 15px;
          line-height: 1.6;
        }
        
        .shop-stats {
          display: flex;
          flex-wrap: wrap;
          gap: 20px;
          
          .stat-item {
            display: flex;
            flex-direction: column;
            align-items: center;
            
            .stat-label {
              font-size: 12px;
              color: var(--el-text-color-secondary);
              margin-bottom: 5px;
            }
            
            .stat-value {
              font-size: 16px;
              font-weight: bold;
              color: var(--el-text-color-primary);
            }
          }
        }
      }
    }
    
    .shop-announcement {
      margin-top: 10px;
    }
  }
  
  // 任务组B：Banner轮播样式
  .shop-banner-section {
    margin: 20px 0;
    
    .banner-item {
      position: relative;
      width: 100%;
      height: 100%;
      border-radius: 8px;
      overflow: hidden;
      
      .banner-overlay {
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        background: linear-gradient(to top, rgba(0, 0, 0, 0.7), transparent);
        padding: 20px;
        color: #fff;
        
        .banner-title {
          font-size: 18px;
          font-weight: 500;
        }
      }
    }
  }
  
  // 任务组B：公告列表样式
  .shop-announcements-section {
    margin: 20px 0;
    
    .announcements-header {
      display: flex;
      align-items: center;
      
      .announcements-title {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 16px;
        font-weight: 500;
      }
    }
    
    .announcements-list {
      .announcement-item {
        padding: 16px;
        border-bottom: 1px solid var(--el-border-color-lighter);
        transition: background-color 0.3s;
        
        &:last-child {
          border-bottom: none;
        }
        
        &:hover {
          background-color: var(--el-bg-color-page);
        }
        
        &.is-top {
          background-color: #fffbf0;
          border-left: 3px solid var(--el-color-warning);
        }
        
        .announcement-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 8px;
          
          .announcement-title-row {
            display: flex;
            align-items: center;
            gap: 8px;
            
            .announcement-title {
              font-weight: 500;
              font-size: 15px;
            }
          }
          
          .announcement-time {
            font-size: 12px;
            color: var(--el-text-color-secondary);
          }
        }
        
        .announcement-content {
          color: var(--el-text-color-regular);
          line-height: 1.6;
          white-space: pre-wrap;
          font-size: 14px;
        }
      }
    }
  }
  
  .shop-content {
    background-color: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  padding: 20px;
    
    .shop-tabs {
      .products-container {
        padding: 10px 0;
        
        .tea-grid {
          display: grid;
          grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
          gap: 20px;
          
          .tea-item {
            height: 100%;
          }
        }
        
        .empty-products {
          padding: 40px 0;
          text-align: center;
        }
      }
      
      .shop-detail-info {
        padding: 20px 0;
      }
    }
  }
  
  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 80px 20px;
    background-color: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
    
    .el-button {
      margin-top: 20px;
    }
  }
}

@media (max-width: 768px) {
  .shop-detail-page {
    .shop-header {
      .shop-info {
        flex-direction: column;
        align-items: center;
        text-align: center;
        
        .shop-logo {
          margin-right: 0;
          margin-bottom: 20px;
        }
        
        .shop-basic-info {
          .shop-name-row {
            flex-direction: column;
            gap: 10px;
            
            .shop-name {
      margin-bottom: 10px;
            }
          }
          
          .shop-rating {
            justify-content: center;
          }
          
          .shop-stats {
            justify-content: center;
          }
        }
      }
    }
    
    .shop-content {
      .shop-tabs {
        .products-container {
          .tea-grid {
            grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
          }
        }
      }
    }
  }
}
</style> 