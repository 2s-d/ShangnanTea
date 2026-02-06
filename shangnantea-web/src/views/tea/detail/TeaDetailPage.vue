<template>
  <div class="tea-detail-page" v-loading="loading">
    <div class="container" v-if="tea">
      <!-- 返回按钮 -->
      <div class="page-actions">
        <div class="action-group">
          <el-button @click="goBack" class="back-button">
            <el-icon><Back /></el-icon> 返回茶叶列表
          </el-button>
          
          <div class="right-actions">
            <el-button 
              type="primary" 
              @click="contactShop"
              class="contact-button"
              v-if="!isPlatformTea"
            >
              <el-icon><ChatLineRound /></el-icon> 联系客服
            </el-button>
            
          <el-button 
            :type="isFavorite ? 'danger' : 'default'"
            @click="toggleFavorite"
            :loading="favoriteLoading"
            class="favorite-button"
          >
            <el-icon><Star /></el-icon> {{ isFavorite ? '已收藏' : '收藏' }}
          </el-button>
          </div>
        </div>
      </div>
      
      <!-- 商品基本信息区 -->
      <div class="tea-basic-info">
        <!-- 左侧图片区 -->
        <div class="tea-images">
          <div class="main-image">
            <el-carousel indicator-position="outside" height="400px">
              <el-carousel-item v-for="(image, index) in teaImages" :key="index">
                <SafeImage :src="image" type="tea" :alt="tea.name" class="carousel-image tea-image" />
              </el-carousel-item>
            </el-carousel>
          </div>
          <div class="thumbnail-list">
            <div 
              v-for="(image, index) in teaImages" 
              :key="index" 
              class="thumbnail"
              :class="{ active: currentImageIndex === index }"
              @click="currentImageIndex = index"
            >
              <SafeImage :src="image" type="tea" :alt="tea.name" class="carousel-image tea-image" />
            </div>
          </div>
        </div>
        
        <!-- 右侧信息区 -->
        <div class="tea-info">
          <h1 class="tea-name">{{ tea.name }}</h1>
          
          <div class="shop-info">
            <h3 class="section-title">店铺信息</h3>
            <div class="shop-details">
              <!-- 平台直售茶叶显示平台标签，无法跳转 -->
              <template v-if="isPlatformTea">
                <div class="platform-shop">
                  <div class="platform-logo">
                    <SafeImage :src="tea.platform_logo" type="avatar" :alt="平台直售" style="width:50px;height:50px;border-radius:50%;object-fit:cover;" class="platform-avatar" />
                  </div>
                  <div class="platform-info">
                    <div class="platform-name">平台直售</div>
                    <div class="platform-tag">
                      <el-tag type="danger" size="small">官方直售</el-tag>
                    </div>
                    <div class="platform-desc">由商南茶文化平台官方直接销售的优质茶叶</div>
                  </div>
                </div>
              </template>
              
              <!-- 店铺茶叶显示店铺信息，可以跳转 -->
              <template v-else>
                <el-link @click="goToShop" class="shop-link">
                  <div class="shop-basic">
                    <SafeImage :src="tea.shop_logo" type="avatar" :alt="tea.shop_name || '商家店铺'" style="width:50px;height:50px;border-radius:50%;object-fit:cover;" class="shop-avatar" />
                    <div class="shop-text">
                      <div class="shop-name">{{ tea.shop_name || '商家店铺' }}</div>
                      <div class="shop-rating">
                        <el-rate 
                          v-model="tea.shop_rating" 
                          disabled 
                          :max="5"
                          text-color="#ff9900">
                        </el-rate>
                      </div>
                    </div>
                  </div>
                  <div class="shop-desc">{{ tea.shop_desc || '专业经营各类优质茶叶' }}</div>
                </el-link>
              </template>
            </div>
          </div>
          
          <div class="tea-brief">{{ tea.brief }}</div>
          
          <div class="tea-price-info">
            <div class="price-row">
              <span class="label">价格：</span>
              <span class="current-price">¥{{ selectedSpec ? selectedSpec.price : (tea.discount_price || tea.price) }}</span>
              <span class="original-price" v-if="tea.discount_price && !selectedSpec">¥{{ tea.price }}</span>
            </div>
            <div class="sales-row">
              <span class="label">销量：</span>
              <span class="sales-value">{{ tea.sales }} 件</span>
            </div>
          </div>
          
          <!-- 规格选择 -->
          <div class="tea-specs">
            <div class="spec-label">规格：</div>
            <div class="spec-options">
              <el-radio-group v-model="selectedSpecId" @change="handleSpecChange">
                <el-radio-button 
                  v-for="spec in teaSpecifications" 
                  :key="spec.id" 
                  :label="spec.id"
                  :disabled="spec.stock <= 0"
                >
                  {{ spec.spec_name }} - ¥{{ spec.price }}
                  <span v-if="spec.stock <= 0" class="sold-out">已售罄</span>
                </el-radio-button>
              </el-radio-group>
            </div>
          </div>
          
          <!-- 数量选择 -->
          <div class="tea-quantity">
            <span class="quantity-label">数量：</span>
            <el-input-number 
              v-model="quantity" 
              :min="1" 
              :max="currentStock" 
              size="large"
            />
            <span class="stock-info">库存：{{ currentStock }} 件</span>
          </div>
          
          <!-- 操作按钮 -->
          <div class="tea-actions">
            <el-button 
              type="primary" 
              size="large" 
              @click="addToCart"
              :loading="submitting"
              :disabled="!canAddToCart"
            >
              <el-icon><ShoppingCart /></el-icon> 加入购物车
            </el-button>
            <el-button 
              type="danger" 
              size="large" 
              @click="buyNow"
              :loading="submitting"
              :disabled="!canAddToCart"
            >
              立即购买
            </el-button>
          </div>
        </div>
      </div>
      
      <!-- 商品详情和评价 -->
      <div class="tea-detail-tabs">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="商品详情" name="detail">
            <div class="detail-content" v-html="tea.description"></div>
          </el-tab-pane>
          <el-tab-pane label="规格参数" name="specification">
            <div class="spec-table">
              <el-descriptions :column="1" border>
                <el-descriptions-item label="品名">{{ tea.name }}</el-descriptions-item>
                <el-descriptions-item label="分类">{{ getCategoryName(tea.category_id) }}</el-descriptions-item>
                <el-descriptions-item label="产地">商南县</el-descriptions-item>
                <el-descriptions-item label="规格">
                  <div v-for="spec in teaSpecifications" :key="spec.id">
                    {{ spec.spec_name }} - ¥{{ spec.price }}
                  </div>
                </el-descriptions-item>
                <el-descriptions-item label="净含量">根据规格不同，详见包装</el-descriptions-item>
                <el-descriptions-item label="保质期">18个月</el-descriptions-item>
                <el-descriptions-item label="存储方法">避光、干燥、密封保存</el-descriptions-item>
              </el-descriptions>
            </div>
          </el-tab-pane>
          <el-tab-pane :label="`用户评价(${reviewTotalCount})`" name="reviews">
            <!-- 评价统计 -->
            <div class="review-stats" v-if="reviewStats">
              <div class="stats-overview">
                <div class="average-rating">
                  <span class="rating-value">{{ reviewStats.averageRating || 0 }}</span>
                  <el-rate v-model="averageRatingNumber" disabled :max="5" />
                  <span class="total-count">共{{ reviewStats.totalCount || 0 }}条评价</span>
                </div>
                <div class="rating-distribution" v-if="reviewStats.ratingDistribution">
                  <div v-for="(count, rating) in reviewStats.ratingDistribution" :key="rating" class="distribution-item">
                    <span class="rating-label">{{ rating }}星</span>
                    <el-progress :percentage="(count / reviewStats.totalCount) * 100" :stroke-width="8" />
                    <span class="count-label">{{ count }}</span>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- 评价列表 -->
            <div class="review-list">
              <div v-if="teaReviews.length === 0" class="empty-reviews">
                暂无评价，购买后可以添加评价
              </div>
              <div v-else class="review-items">
                <div v-for="review in teaReviews" :key="review.id" class="review-item">
                  <div class="review-header">
                    <div class="user-info">
                      <SafeImage :src="review.avatar || review.user_avatar" type="avatar" :alt="review.username || review.user_name" style="width:40px;height:40px;border-radius:50%;object-fit:cover;" class="user-avatar" />
                      <span class="user-name">{{ review.username || review.user_name }}</span>
                    </div>
                    <div class="review-rating">
                      <el-rate :model-value="review.rating" disabled />
                      <span class="review-time">{{ formatTime(review.createTime || review.create_time) }}</span>
                    </div>
                  </div>
                  <div class="review-content">{{ review.content }}</div>
                  <div class="review-images" v-if="review.images && review.images.length > 0">
                    <div v-for="(img, index) in review.images" :key="index" class="review-image">
                      <SafeImage :src="img" type="tea" :alt="tea.name" class="tea-review-image" />
                    </div>
                  </div>
                  <div class="review-actions">
                    <el-button 
                      type="text" 
                      size="small" 
                      @click="handleLikeReview(review)"
                      :class="{ 'liked': review.isLiked }"
                    >
                      <el-icon><StarFilled /></el-icon> {{ review.likeCount || 0 }}
                    </el-button>
                  </div>
                  <div class="review-reply" v-if="review.reply">
                    <div class="reply-header">
                      <span class="shop-name">{{ isPlatformTea ? '平台回复' : tea.shop_name }}</span>
                      <span class="reply-time">{{ formatTime(review.replyTime || review.reply_time) }}</span>
                    </div>
                    <div class="reply-content">{{ review.reply }}</div>
                  </div>
                  <div class="shop-actions" v-if="isShopOwner && !review.reply">
                    <el-button 
                      type="primary" 
                      size="small" 
                      @click="showReplyForm(review)"
                      plain
                    >
                      回复此评价
                    </el-button>
                  </div>
                  <div class="reply-form" v-if="activeReplyId === review.id">
                    <el-input
                      v-model="replyContent"
                      type="textarea"
                      :rows="3"
                      placeholder="请输入回复内容..."
                      maxlength="500"
                      show-word-limit
                    ></el-input>
                    <div class="form-actions">
                      <el-button size="small" @click="cancelReply">取消</el-button>
                      <el-button 
                        type="primary" 
                        size="small" 
                        @click="submitReply(review)"
                        :disabled="!replyContent.trim()"
                        :loading="submittingReply"
                      >
                        提交回复
                      </el-button>
                    </div>
                  </div>
                </div>
              </div>
              
              <!-- 评价分页 -->
              <div class="review-pagination" v-if="reviewTotalCount > 0">
                <el-pagination
                  background
                  layout="prev, pager, next"
                  :total="reviewTotalCount"
                  :page-size="reviewPageSize"
                  :current-page="reviewCurrentPage"
                  @current-change="handleReviewPageChange"
                />
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
      
      <!-- 任务组F：相似推荐 -->
      <div class="recommend-section" v-if="similarTeas.length > 0">
        <h2 class="section-title">相似推荐</h2>
        <div class="recommend-list">
          <div 
            v-for="tea in similarTeas" 
            :key="tea.id" 
            class="recommend-item"
            @click="goToTeaDetail(tea.id)"
          >
            <SafeImage :src="tea.main_image" type="tea" :alt="tea.name" class="recommend-image" />
            <div class="recommend-info">
              <div class="recommend-name">{{ tea.name }}</div>
              <div class="recommend-price">
                <span class="current-price">¥{{ tea.discount_price || tea.price }}</span>
                <span v-if="tea.discount_price" class="original-price">¥{{ tea.price }}</span>
              </div>
              <div class="recommend-meta">
                <span class="sales">销量: {{ tea.sales }}</span>
                <el-rate :model-value="tea.rating" disabled :max="5" size="small" />
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 空状态 -->
    <div v-else-if="!loading" class="empty-state">
      <el-empty description="未找到茶叶信息" />
      <el-button type="primary" @click="goToTeaList">返回茶叶列表</el-button>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessageBox } from 'element-plus'
import { Back, ShoppingCart, Star, ChatLineRound, StarFilled } from '@element-plus/icons-vue'
import SafeImage from '@/components/common/form/SafeImage.vue'
import { showByCode, isSuccess } from '@/utils/apiMessages'
import teaMessages from '@/utils/promptMessages'

export default {
  name: 'TeaDetailPage',
  components: {
    Back,
    ShoppingCart,
    Star,
    ChatLineRound,
    StarFilled,
    SafeImage
  },
  setup() {
    const store = useStore()
    const router = useRouter()
    const route = useRoute()
    const loading = computed(() => store.state.tea.loading)
    const submitting = ref(false)
    const tea = computed(() => store.state.tea.currentTea)
    const activeTab = ref('detail')
    const selectedSpecId = ref(null)
    const quantity = ref(1)
    const currentImageIndex = ref(0)
    // 收藏状态（从接口返回的isFavorited字段获取）
    const isFavorite = computed(() => tea.value?.isFavorited || false)
    const favoriteLoading = ref(false)
    
    // 回复相关状态
    const activeReplyId = ref(null) // 当前正在回复的评论ID
    const replyContent = ref('') // 回复内容
    const submittingReply = ref(false) // 提交回复的loading状态
    
    // 判断当前用户是否为商店所有者
    const isShopOwner = computed(() => {
      const currentUserId = store.state.user.userInfo?.id
      return currentUserId && tea.value && currentUserId === tea.value.shopOwnerId
    })
    
    // 任务组B：评价相关数据
    const teaReviews = computed(() => store.state.tea.teaReviews || [])
    const reviewStats = computed(() => store.state.tea.reviewStats)
    const reviewTotalCount = computed(() => store.state.tea.reviewPagination?.total || 0)
    const reviewCurrentPage = computed(() => store.state.tea.reviewPagination?.currentPage || 1)
    const reviewPageSize = computed(() => store.state.tea.reviewPagination?.pageSize || 10)
    const averageRatingNumber = computed(() => {
      if (reviewStats.value && reviewStats.value.averageRating) {
        return parseFloat(reviewStats.value.averageRating)
      }
      return 0
    })
    
    // 任务组C：规格相关数据
    const teaSpecifications = computed(() => {
      // 优先使用Vuex中的规格列表，如果没有则使用currentTea中的规格
      const specs = store.state.tea.currentTeaSpecs || []
      if (specs.length > 0) {
        return specs
      }
      return tea.value?.specifications || []
    })
    
    // 显示回复表单
    const showReplyForm = review => {
      activeReplyId.value = review.id
      replyContent.value = ''
    }
    
    // 取消回复
    const cancelReply = () => {
      activeReplyId.value = null
      replyContent.value = ''
    }
    
    // 提交回复
    const submitReply = async review => {
      if (!replyContent.value.trim()) {
        teaMessages.prompt.showReplyEmpty()
        return
      }
      
      submittingReply.value = true
      
      try {
        const response = await store.dispatch('tea/replyReview', {
          reviewId: review.id,
          reply: replyContent.value
        })
        showByCode(response.code)
        activeReplyId.value = null
        replyContent.value = ''
      } catch (error) {
        console.error('回复评价失败:', error)
      } finally {
        submittingReply.value = false
      }
    }
    
    // 点赞评价
    const handleLikeReview = async review => {
      try {
        if (review.isLiked) {
          // 取消点赞：直接传递targetId和targetType
          const response = await store.dispatch('user/removeLike', {
            targetId: String(review.id),
            targetType: 'review'
          })
          showByCode(response.code)
          // 重新加载评价列表以更新isLiked状态
          if (tea.value) {
            await store.dispatch('tea/fetchTeaReviews', {
              teaId: tea.value.id,
              page: reviewCurrentPage.value,
              pageSize: reviewPageSize.value
            })
          }
        } else {
          // 添加点赞
          const response = await store.dispatch('user/addLike', {
            targetId: String(review.id),
            targetType: 'review'
          })
          showByCode(response.code)
          // 重新加载评价列表以更新isLiked状态
          if (tea.value) {
            await store.dispatch('tea/fetchTeaReviews', {
              teaId: tea.value.id,
              page: reviewCurrentPage.value,
              pageSize: reviewPageSize.value
            })
          }
        }
      } catch (error) {
        console.error('点赞失败:', error)
      }
    }
    
    // 评价分页变化
    const handleReviewPageChange = page => {
      if (tea.value) {
        store.dispatch('tea/fetchTeaReviews', {
          teaId: tea.value.id,
          page,
          pageSize: reviewPageSize.value
        })
      }
    }
    
    // 格式化时间
    const formatTime = time => {
      if (!time) return ''
      const date = new Date(time)
      const now = new Date()
      const diff = now - date
      const days = Math.floor(diff / (1000 * 60 * 60 * 24))
      
      if (days === 0) {
        const hours = Math.floor(diff / (1000 * 60 * 60))
        if (hours === 0) {
          const minutes = Math.floor(diff / (1000 * 60))
          return minutes <= 0 ? '刚刚' : `${minutes}分钟前`
        }
        return `${hours}小时前`
      } else if (days < 7) {
        return `${days}天前`
      } else {
        return date.toLocaleDateString('zh-CN')
      }
    }
    
    // 茶叶分类（从 Vuex 获取）
    const categories = computed(() => store.state.tea.categories || [])
    
    // 获取茶叶类别名称
    const getCategoryName = categoryId => {
      const category = categories.value.find(c => c.id === categoryId)
      return category ? category.name : '未知分类'
    }
    
    // 切换收藏状态
    const toggleFavorite = async () => {
      if (!tea.value) return
      
      favoriteLoading.value = true
      try {
        if (isFavorite.value) {
          // 取消收藏：直接传递 itemId 和 itemType
          const response = await store.dispatch('user/removeFavorite', {
            itemId: tea.value.id,
            itemType: 'tea'
          })
          showByCode(response.code)
          // 重新加载茶叶详情以更新isFavorited状态
          await store.dispatch('tea/fetchTeaDetail', tea.value.id)
        } else {
          // 添加收藏
          const response = await store.dispatch('user/addFavorite', {
            itemId: tea.value.id,
            itemType: 'tea',
            targetName: tea.value.name,
            targetImage: tea.value.main_image || tea.value.images?.[0] || ''
          })
          showByCode(response.code)
          // 重新加载茶叶详情以更新isFavorited状态
          await store.dispatch('tea/fetchTeaDetail', tea.value.id)
        }
      } catch (error) {
        console.error('收藏操作失败:', error)
      } finally {
        favoriteLoading.value = false
      }
    }
    
    // 加载茶叶详情（生产版：走 Vuex）
    const loadTeaDetail = async () => {
      try {
        const teaId = route.params.id
        await store.dispatch('tea/fetchTeaDetail', teaId)
        
        // 任务组B：同时加载评价列表和统计数据
        // 任务组C：同时加载规格列表
        // 任务组D：加载图片列表（如果后端返回的tea.images为空，则从Vuex获取）
        // 任务组F：加载相似推荐
        await Promise.all([
          store.dispatch('tea/fetchTeaReviews', { teaId, page: 1, pageSize: 10 }),
          store.dispatch('tea/fetchReviewStats', teaId),
          store.dispatch('tea/fetchTeaSpecifications', teaId),
          store.dispatch('tea/fetchRecommendTeas', { type: 'similar', teaId, count: 6 })
        ])
        
        // 任务组D：如果当前茶叶的images为空，尝试从Vuex获取
        if ((!store.state.tea.currentTea?.images || store.state.tea.currentTea.images.length === 0) && 
            store.state.tea.teaImages && store.state.tea.teaImages.length > 0) {
          store.state.tea.currentTea.images = store.state.tea.teaImages
        }
        
        // 任务组C：设置默认规格（从Vuex的currentTeaSpecs获取）
        const specs = store.state.tea.currentTeaSpecs || []
        const defaultSpec = specs.find(spec => spec.is_default === 1)
        if (defaultSpec) {
          selectedSpecId.value = defaultSpec.id
        } else if (specs.length > 0) {
          selectedSpecId.value = specs[0].id
        }
      } catch (e) {
        console.error('加载茶叶详情失败:', e)
      }
    }
    
    // 计算属性 - 是否为平台直售
    const isPlatformTea = computed(() => {
      return tea.value && (tea.value.shopId === '0' || tea.value.shop_id === '0' || tea.value.shop_id === 'PLATFORM')
    })

    // 兼容后端返回的图片结构（可能是 string[] 或 {url}[]）
    // 任务组D：图片列表（优先使用Vuex中的teaImages，如果没有则使用currentTea中的images）
    const teaImages = computed(() => {
      // 优先使用Vuex中的teaImages
      const vuexImages = store.state.tea.teaImages || []
      if (vuexImages.length > 0) {
        // 按order排序，然后提取url
        return vuexImages
          .sort((a, b) => (a.order || 0) - (b.order || 0))
          .map(img => img.url)
          .filter(Boolean)
      }
      
      // 如果没有Vuex数据，使用currentTea中的images
      const imgs = tea.value?.images || []
      if (!Array.isArray(imgs)) return []
      if (imgs.length === 0) return []
      if (typeof imgs[0] === 'string') return imgs
      
      // 如果是对象数组，按order排序后提取url
      const imageObjects = imgs.filter(i => i && i.url)
      if (imageObjects.length > 0) {
        return imageObjects
          .sort((a, b) => (a.order || 0) - (b.order || 0))
          .map(i => i.url)
          .filter(Boolean)
      }
      
      return []
    })
    
    // 计算属性 - 当前选中的规格
    const selectedSpec = computed(() => {
      if (!selectedSpecId.value) return null
      return teaSpecifications.value.find(spec => spec.id === selectedSpecId.value)
    })
    
    // 任务组C：规格选择变化处理
    const handleSpecChange = specId => {
      const spec = teaSpecifications.value.find(s => s.id === specId)
      if (spec) {
        store.commit('tea/SET_SELECTED_SPEC', spec)
      }
    }
    
    // 任务组F：相似推荐数据
    const similarTeas = computed(() => store.state.tea.recommendTeas || [])
    
    // 任务组F：跳转到茶叶详情页
    const goToTeaDetail = teaId => {
      router.push(`/tea/${teaId}`)
    }
    
    // 计算属性 - 当前库存
    const currentStock = computed(() => {
      if (selectedSpec.value) {
        return selectedSpec.value.stock
      }
      return tea.value ? tea.value.stock : 0
    })
    
    // 计算属性 - 是否可以加入购物车
    const canAddToCart = computed(() => {
      return currentStock.value > 0
    })
    
    // 加入购物车
    const addToCart = async () => {
      if (!canAddToCart.value) {
        teaMessages.prompt.showSoldOut()
        return
      }
      
      if (!selectedSpecId.value) {
        teaMessages.prompt.showSelectSpec()
        return
      }
      
      submitting.value = true
      try {
        // 生产版：通过 order 模块 action 走后端，传递规格ID
        const response = await store.dispatch('order/addToCart', { 
          teaId: tea.value.id, 
          quantity: quantity.value,
          specificationId: selectedSpecId.value
        })
        showByCode(response.code)
      } catch (error) {
        console.error('加入购物车失败:', error)
      } finally {
        submitting.value = false
      }
    }
    
    // 立即购买
    const buyNow = async () => {
      if (!canAddToCart.value) {
        teaMessages.prompt.showSoldOut()
        return
      }
      
      if (!selectedSpecId.value) {
        teaMessages.prompt.showSelectSpec()
        return
      }
      
      try {
        submitting.value = true
        // TODO: 直接购买的生产版流程需要订单模块提供“direct buy”能力；目前先跳转结算页
        router.push('/order/checkout?direct=1')
      } catch (error) {
        // TODO: 迁移到新消息系统 - 使用 showByCode(response.code)

        teaMessages.error.showBuyFailed(error.message)
      } finally {
        submitting.value = false
      }
    }
    
    // 跳转到店铺详情
    const goToShop = () => {
      // 如果是平台直售茶叶，不进行跳转
      if (isPlatformTea.value) {
        return
      }
      
      // 否则跳转到对应的店铺详情页
      const shopId = tea.value?.shopId || tea.value?.shop_id
      if (shopId) {
        router.push(`/shop/${shopId}`)
      }
    }
    
    // 联系店铺客服
    const contactShop = () => {
      // 如果是平台直售茶叶，不应显示联系按钮
      if (isPlatformTea.value) {
        return
      }
      
      // 跳转到消息中心的私信聊天页面，传递店铺ID
      const shopId = tea.value?.shopId || tea.value?.shop_id
      if (shopId) {
        router.push(`/message/center/chat?shopId=${shopId}`)
      }
    }
    
    // 返回上一页
    const goBack = () => {
      router.back()
    }
    
    // 返回茶叶列表
    const goToTeaList = () => {
      router.push('/tea/list')
    }
    
    const defaultAvatar = '@/assets/images/avatars/default.jpg'
    
    onMounted(() => {
      store.dispatch('tea/fetchCategories')
      loadTeaDetail()
    })

    watch(
      () => route.params.id,
      () => {
        loadTeaDetail()
      }
    )
    
    return {
      tea,
      teaImages,
      loading,
      submitting,
      activeTab,
      selectedSpecId,
      quantity,
      currentImageIndex,
      isPlatformTea,
      selectedSpec,
      currentStock,
      canAddToCart,
      getCategoryName,
      addToCart,
      buyNow,
      goToShop,
      goBack,
      goToTeaList,
      isFavorite,
      favoriteLoading,
      toggleFavorite,
      // 新增回复相关
      isShopOwner,
      activeReplyId,
      replyContent,
      submittingReply,
      showReplyForm,
      cancelReply,
      submitReply,
      defaultAvatar,
      contactShop,
      // 任务组F：相似推荐
      similarTeas,
      goToTeaDetail
    }
  }
}
</script>

<style lang="scss" scoped>
.tea-detail-page {
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
    
    .action-group {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    
    .back-button, .favorite-button, .contact-button {
      display: flex;
      align-items: center;
      
      .el-icon {
        margin-right: 5px;
      }
    }
    
    .right-actions {
      display: flex;
      gap: 10px;
    }
  }
  
  .tea-basic-info {
    display: flex;
    gap: 30px;
    margin-bottom: 30px;
    background-color: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
    padding: 20px;
    
    .tea-images {
      flex: 0 0 400px;
      
      .main-image {
        width: 100%;
        margin-bottom: 15px;
        
        .carousel-image {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
      }
      
      .thumbnail-list {
        display: flex;
        gap: 10px;
        
        .thumbnail {
          width: 80px;
          height: 80px;
          border: 2px solid #eee;
          cursor: pointer;
          border-radius: 4px;
          overflow: hidden;
          
          &.active {
            border-color: var(--el-color-primary);
          }
          
          img {
            width: 100%;
            height: 100%;
            object-fit: cover;
          }
        }
      }
    }
    
    .tea-info {
      flex: 1;
      
      .tea-name {
        font-size: 24px;
        font-weight: 600;
        margin: 0 0 15px;
        color: var(--el-text-color-primary);
      }
      
      .shop-info {
        margin-bottom: 20px;
        
        .section-title {
          font-size: 18px;
          margin-bottom: 15px;
          font-weight: 600;
          color: var(--el-text-color-primary);
        }
        
        .shop-details {
          background-color: var(--el-fill-color-light);
          border-radius: 8px;
          padding: 15px;
          
          .shop-link {
            display: block;
            text-decoration: none;
            color: inherit;
            
            &:hover {
              .shop-name {
                color: var(--el-color-primary);
              }
            }
          }
          
          .shop-basic {
            display: flex;
            align-items: center;
            margin-bottom: 10px;
            
            .shop-text {
              margin-left: 15px;
              
              .shop-name {
                font-size: 16px;
                font-weight: 500;
                margin-bottom: 5px;
                transition: color 0.3s;
              }
              
              .shop-rating {
                display: flex;
                align-items: center;
                
                span {
                  margin-left: 5px;
                  color: #FF9900;
                }
              }
            }
          }
          
          .shop-desc {
            font-size: 14px;
            color: var(--el-text-color-secondary);
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
          }
          
          .platform-shop {
            display: flex;
            align-items: flex-start;
            
            .platform-logo {
              margin-right: 15px;
            }
            
            .platform-info {
              .platform-name {
                font-size: 16px;
                font-weight: 500;
                margin-bottom: 5px;
              }
              
              .platform-tag {
                margin-top: 5px;
              }
              
              .platform-desc {
                font-size: 14px;
                color: var(--el-text-color-secondary);
              }
            }
          }
        }
      }
      
      .tea-brief {
        font-size: 14px;
        color: var(--el-text-color-secondary);
        margin-bottom: 20px;
        line-height: 1.6;
        padding-bottom: 20px;
        border-bottom: 1px solid var(--el-border-color-lighter);
      }
      
      .tea-price-info {
        margin-bottom: 20px;
        
        .price-row {
          display: flex;
          align-items: center;
          margin-bottom: 10px;
          
          .label {
            color: var(--el-text-color-secondary);
            margin-right: 10px;
          }
          
          .current-price {
            font-size: 24px;
            font-weight: bold;
            color: var(--el-color-danger);
            margin-right: 10px;
          }
          
          .original-price {
            font-size: 16px;
            color: var(--el-text-color-secondary);
            text-decoration: line-through;
          }
        }
        
        .sales-row {
          display: flex;
          align-items: center;
          
          .label {
            color: var(--el-text-color-secondary);
            margin-right: 10px;
          }
        }
      }
      
      .tea-specs {
        margin-bottom: 20px;
        
        .spec-label {
          color: var(--el-text-color-secondary);
          margin-bottom: 10px;
        }
        
        .spec-options {
          .sold-out {
            margin-left: 5px;
            font-size: 12px;
            color: var(--el-color-danger);
          }
        }
      }
      
      .tea-quantity {
        display: flex;
        align-items: center;
        margin-bottom: 30px;
        
        .quantity-label {
          color: var(--el-text-color-secondary);
          margin-right: 10px;
        }
        
        .stock-info {
          margin-left: 15px;
          color: var(--el-text-color-secondary);
          font-size: 14px;
        }
      }
      
      .tea-actions {
        display: flex;
        gap: 15px;
        
        .el-button {
          width: 200px;
        }
      }
    }
  }
  
  .tea-detail-tabs {
    background-color: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  padding: 20px;
    
    .detail-content {
      padding: 20px 0;
      
      ::v-deep(img) {
        max-width: 100%;
        height: auto;
        margin: 15px 0;
      }
      
      ::v-deep(h3) {
        font-size: 20px;
        margin: 20px 0 15px;
        color: var(--el-text-color-primary);
      }
      
      ::v-deep(h4) {
        font-size: 18px;
        margin: 18px 0 12px;
        color: var(--el-text-color-primary);
      }
      
      ::v-deep(p) {
        line-height: 1.8;
        margin: 12px 0;
        color: var(--el-text-color-regular);
      }
    }
    
    .spec-table {
      padding: 20px 0;
    }
    
    .review-list {
      padding: 20px 0;
      
      .empty-reviews {
        text-align: center;
        padding: 30px;
        color: var(--el-text-color-secondary);
      }
      
      .review-items {
        .review-item {
          border-bottom: 1px solid var(--el-border-color-lighter);
          padding: 20px 0;
          
          &:last-child {
            border-bottom: none;
          }
          
          .review-header {
            display: flex;
            justify-content: space-between;
            margin-bottom: 15px;
            
            .user-info {
              display: flex;
              align-items: center;
              
              .user-avatar {
                margin-right: 10px;
              }
              
              .user-name {
                font-weight: 500;
              }
            }
            
            .review-rating {
              display: flex;
              flex-direction: column;
              align-items: flex-end;
              
              .review-time {
                margin-top: 5px;
                font-size: 12px;
                color: var(--el-text-color-secondary);
              }
            }
          }
          
          .review-content {
            line-height: 1.6;
            margin-bottom: 15px;
            color: var(--el-text-color-regular);
          }
          
          .review-images {
            display: flex;
            gap: 10px;
            margin-bottom: 15px;
            
            .review-image {
              width: 80px;
              height: 80px;
              border-radius: 4px;
              overflow: hidden;
              
              .el-image {
                width: 100%;
                height: 100%;
              }
            }
          }
          
          .review-reply {
            background-color: var(--el-fill-color-light);
            border-radius: 4px;
            padding: 15px;
            margin-top: 15px;
            
            .reply-header {
              display: flex;
              justify-content: space-between;
              margin-bottom: 10px;
              
              .shop-name {
                font-weight: 500;
                color: var(--el-color-primary);
              }
              
              .reply-time {
                font-size: 12px;
                color: var(--el-text-color-secondary);
              }
            }
            
            .reply-content {
              color: var(--el-text-color-regular);
              line-height: 1.6;
            }
          }
          
          .shop-actions {
            margin-top: 15px;
            display: flex;
            justify-content: flex-end;
          }
          
          .reply-form {
            margin-top: 15px;
            background-color: var(--el-fill-color-light);
            border-radius: 4px;
            padding: 15px;
            
            .form-actions {
              margin-top: 10px;
              display: flex;
              justify-content: flex-end;
              gap: 10px;
            }
          }
        }
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
  
  // 任务组F：相似推荐样式
  .recommend-section {
    margin-top: 40px;
    padding: 30px;
    background-color: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
    
    .section-title {
      font-size: 20px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 20px;
      padding-bottom: 10px;
      border-bottom: 2px solid #409eff;
    }
    
    .recommend-list {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
      gap: 20px;
      
      .recommend-item {
        cursor: pointer;
        transition: transform 0.3s, box-shadow 0.3s;
        border-radius: 8px;
        overflow: hidden;
        background-color: #fff;
        border: 1px solid #ebeef5;
        
        &:hover {
          transform: translateY(-5px);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        }
        
        .recommend-image {
          width: 100%;
          height: 200px;
          object-fit: cover;
        }
        
        .recommend-info {
          padding: 15px;
          
          .recommend-name {
            font-size: 14px;
            color: #303133;
            margin-bottom: 10px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
          
          .recommend-price {
            margin-bottom: 10px;
            
            .current-price {
              font-size: 18px;
              font-weight: 600;
              color: #f56c6c;
              margin-right: 8px;
            }
            
            .original-price {
              font-size: 14px;
              color: #909399;
              text-decoration: line-through;
            }
          }
          
          .recommend-meta {
            display: flex;
            justify-content: space-between;
            align-items: center;
            font-size: 12px;
            color: #909399;
            
            .sales {
              flex: 1;
            }
          }
        }
      }
    }
  }
}

@media (max-width: 992px) {
  .tea-detail-page {
    .tea-basic-info {
      flex-direction: column;
      
      .tea-images {
        flex: none;
        width: 100%;
      }
    }
  }
}

@media (max-width: 768px) {
  .tea-detail-page {
    .tea-info {
      .tea-actions {
        flex-direction: column;
        
        .el-button {
          width: 100%;
        }
      }
    }
  }
}
</style> 