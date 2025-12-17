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
              <el-carousel-item v-for="(image, index) in tea.images" :key="index">
                <SafeImage :src="image" type="tea" :alt="tea.name" class="carousel-image tea-image" />
              </el-carousel-item>
            </el-carousel>
          </div>
          <div class="thumbnail-list">
            <div 
              v-for="(image, index) in tea.images" 
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
              <el-radio-group v-model="selectedSpecId">
                <el-radio-button 
                  v-for="spec in tea.specifications" 
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
                  <div v-for="spec in tea.specifications" :key="spec.id">
                    {{ spec.spec_name }} - ¥{{ spec.price }}
                  </div>
                </el-descriptions-item>
                <el-descriptions-item label="净含量">根据规格不同，详见包装</el-descriptions-item>
                <el-descriptions-item label="保质期">18个月</el-descriptions-item>
                <el-descriptions-item label="存储方法">避光、干燥、密封保存</el-descriptions-item>
              </el-descriptions>
            </div>
          </el-tab-pane>
          <el-tab-pane :label="`用户评价(${tea.reviews.length})`" name="reviews">
            <div class="review-list">
              <div v-if="tea.reviews.length === 0" class="empty-reviews">
                暂无评价，购买后可以添加评价
              </div>
              <div v-else class="review-items">
                <div v-for="review in tea.reviews" :key="review.id" class="review-item">
                  <div class="review-header">
                    <div class="user-info">
                      <SafeImage :src="review.user_avatar" type="avatar" :alt="review.user_name" style="width:40px;height:40px;border-radius:50%;object-fit:cover;" class="user-avatar" />
                      <span class="user-name">{{ review.user_name }}</span>
                    </div>
                    <div class="review-rating">
                      <el-rate v-model="review.rating" disabled />
                      <span class="review-time">{{ review.create_time }}</span>
                    </div>
                  </div>
                  <div class="review-content">{{ review.content }}</div>
                  <div class="review-images" v-if="review.images && review.images.length > 0">
                    <div v-for="(img, index) in review.images" :key="index" class="review-image">
                      <SafeImage :src="img" type="tea" :alt="tea.name" class="tea-review-image" />
                    </div>
                  </div>
                  <div class="review-reply" v-if="review.has_reply">
                    <div class="reply-header">
                      <span class="shop-name">{{ isPlatformTea ? '平台回复' : tea.shop_name }}</span>
                      <span class="reply-time">{{ review.reply_time }}</span>
                    </div>
                    <div class="reply-content">{{ review.reply }}</div>
                  </div>
                  <div class="shop-actions" v-if="isShopOwner && !review.has_reply">
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
            </div>
          </el-tab-pane>
        </el-tabs>
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
/* UI-DEV-START */
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Back, ShoppingCart, Star, ChatLineRound } from '@element-plus/icons-vue'
import SafeImage from '@/components/common/form/SafeImage.vue'
/* UI-DEV-END */

/*
// 真实代码（开发UI时注释）
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Back, ShoppingCart, Star } from '@element-plus/icons-vue'
*/

export default {
  name: "TeaDetailPage",
  components: {
    /* UI-DEV-START */
    Back,
    ShoppingCart,
    Star,
    ChatLineRound,
    SafeImage
    /* UI-DEV-END */
    
    /*
    // 真实代码（开发UI时注释）
    Back,
    ShoppingCart,
    Star
    */
  },
  setup() {
    const router = useRouter()
    const route = useRoute()
    const loading = ref(true)
    const submitting = ref(false)
    const tea = ref(null)
    const activeTab = ref('detail')
    const selectedSpecId = ref(null)
    const quantity = ref(1)
    const currentImageIndex = ref(0)
    const isFavorite = ref(false)
    const favoriteLoading = ref(false)
    
    // 回复相关状态
    const activeReplyId = ref(null) // 当前正在回复的评论ID
    const replyContent = ref('') // 回复内容
    const submittingReply = ref(false) // 提交回复的loading状态
    
    /* UI-DEV-START */
    // 模拟判断当前用户是否为商店所有者
    const isShopOwner = ref(true) // 始终为true，方便测试UI功能
    
    // 显示回复表单
    const showReplyForm = (review) => {
      console.log('显示回复表单', review.id)
      activeReplyId.value = review.id
      replyContent.value = ''
    }
    
    // 取消回复
    const cancelReply = () => {
      activeReplyId.value = null
      replyContent.value = ''
    }
    
    // 提交回复
    const submitReply = (review) => {
      if (!replyContent.value.trim()) {
        ElMessage.warning('请输入回复内容')
        return
      }
      
      submittingReply.value = true
      
      // 模拟API调用
      setTimeout(() => {
        // 在UI上立即更新评论对象
        review.has_reply = true
        review.reply = replyContent.value
        review.reply_time = new Date().toLocaleString()
        
        // 重置状态
        activeReplyId.value = null
        replyContent.value = ''
        submittingReply.value = false
        
        ElMessage.success('回复成功')
      }, 800)
    }
    
    // 生成模拟评论数据函数
    const generateMockReviews = (count) => {
      const reviews = [];
      const avatars = [
        'https://img.zcool.cn/community/031d14260114a3b5b3086ed12789e6.jpg',
        'https://img.zcool.cn/community/031a6aa60114a3b5b3086ed127c771.jpg',
        'https://img.zcool.cn/community/031139460114a3b5b3086ed12789e6.jpg',
        'https://img.zcool.cn/community/031d14260114a3b5b3086ed12789e6.jpg'
      ];
      const reviewImages = [
        'https://img.zcool.cn/community/01f6c65fb0dd1ca8012066210a4c8f.jpg',
        'https://img.zcool.cn/community/0152bc5fb0dd1ca801206621f21b74.jpg',
        'https://img.zcool.cn/community/01c1c85fb0dd1ca801206621a014c4.jpg'
      ];
      const userNames = ['茶韵悠悠', '茶香四溢', '品茶人生', '茶之韵味', '清茗一笑', '茶道至简'];
      
      for (let i = 1; i <= count; i++) {
        const hasReply = Math.random() > 0.5;
        const hasPics = Math.random() > 0.6;
        
        reviews.push({
          id: i,
          tea_id: 'tea1000001',
          user_id: `USER${i.toString().padStart(3, '0')}`,
          user_name: userNames[Math.floor(Math.random() * userNames.length)],
          user_avatar: avatars[Math.floor(Math.random() * avatars.length)],
          rating: Math.floor(Math.random() * 2) + 4, // 4-5星
          content: `这款茶叶品质非常好，${i % 2 === 0 ? '香气高雅持久' : '滋味鲜爽甘醇'}，${i % 3 === 0 ? '价格也很合理' : '包装非常精美'}，值得推荐！`,
          images: hasPics ? [
            reviewImages[i % 3],
            i % 2 === 0 ? reviewImages[(i + 1) % 3] : null
          ].filter(Boolean) : [],
          has_reply: hasReply,
          reply: hasReply ? `感谢您的喜爱和评价，我们将继续${i % 2 === 0 ? '保持品质' : '改进服务'}，为您提供更好的茶叶产品！` : '',
          reply_time: hasReply ? `2023-${(6 + i % 6).toString().padStart(2, '0')}-${(i + 5).toString().padStart(2, '0')} ${10 + i}:${30 + i}:00` : null,
          create_time: `2023-${(5 + i % 6).toString().padStart(2, '0')}-${(i + 1).toString().padStart(2, '0')} ${8 + i}:${20 + i}:00`,
          update_time: `2023-${(5 + i % 6).toString().padStart(2, '0')}-${(i + 1).toString().padStart(2, '0')} ${8 + i}:${30 + i}:00`,
          is_anonymous: 0,
        });
      }
      
      return reviews;
    };
    /* UI-DEV-END */
    
    /*
    // 真实代码（开发UI时注释）
    const store = useStore()
    
    // 判断当前用户是否为商店所有者
    const isShopOwner = computed(() => {
      const currentUserId = store.state.user.userInfo?.id
      return currentUserId && tea.value && currentUserId === tea.value.shopOwnerId
    })
    
    // 显示回复表单
    const showReplyForm = (review) => {
      activeReplyId.value = review.id
      replyContent.value = ''
    }
    
    // 取消回复
    const cancelReply = () => {
      activeReplyId.value = null
      replyContent.value = ''
    }
    
    // 提交回复
    const submitReply = async (review) => {
      if (!replyContent.value.trim()) {
        ElMessage.warning('请输入回复内容')
        return
      }
      
      submittingReply.value = true
      
      try {
        await store.dispatch('tea/replyReview', {
          reviewId: review.id,
          content: replyContent.value
        })
        
        // 更新本地评论数据
        review.has_reply = true
        review.reply = replyContent.value
        review.reply_time = new Date().toLocaleString()
        
        // 重置状态
        activeReplyId.value = null
        replyContent.value = ''
        
        ElMessage.success('回复成功')
      } catch (error) {
        ElMessage.error(error.message || '回复失败')
      } finally {
        submittingReply.value = false
      }
    }
    */
    
    // 茶叶分类
    const categories = [
      { id: 1, name: '绿茶' },
      { id: 2, name: '红茶' },
      { id: 3, name: '黑茶' },
      { id: 4, name: '白茶' },
      { id: 5, name: '黄茶' },
      { id: 6, name: '青茶' },
      { id: 7, name: '花茶' }
    ]
    
    // 获取茶叶类别名称
    const getCategoryName = (categoryId) => {
      const category = categories.find(c => c.id === categoryId)
      return category ? category.name : '未知分类'
    }
    
    // 切换收藏状态
    const toggleFavorite = async () => {
      favoriteLoading.value = true
      
      /* UI-DEV-START */
      // 模拟API调用延迟
      setTimeout(() => {
        // 切换收藏状态
        isFavorite.value = !isFavorite.value
        
        // 构建模拟的收藏数据结构 - 符合user_favorites表结构
        const favoriteData = {
          id: Math.random().toString(36).substring(2, 10), // 模拟ID
          user_id: 'user123', // 模拟用户ID
          item_id: tea.value.id, // 茶叶ID
          item_type: 'tea', // 收藏项类型，固定为"tea"
          create_time: new Date().toISOString() // 创建时间
        };
        
        // 记录操作的数据 - 仅开发测试用
        console.log('收藏操作数据:', favoriteData);
        
        // 显示消息
        ElMessage.success(isFavorite.value ? '已添加到收藏' : '已取消收藏')
        
        favoriteLoading.value = false
      }, 600)
      /* UI-DEV-END */
      
      /*
      // 真实代码（开发UI时注释）
      try {
        // 调用Vuex action处理收藏/取消收藏操作
        // 根据user_favorites表结构传递数据
        const result = await store.dispatch(isFavorite.value ? 'favorites/removeFavorite' : 'favorites/addFavorite', {
          item_id: tea.value.id,
          item_type: 'tea'
        });
        
        // 更新收藏状态
        isFavorite.value = !isFavorite.value;
        
        // 显示消息
        ElMessage.success(isFavorite.value ? '已添加到收藏' : '已取消收藏');
      } catch (error) {
        ElMessage.error(error.message || '操作失败，请稍后重试');
      } finally {
        favoriteLoading.value = false;
      }
      */
    }
    
    // 模拟检查是否已收藏
    const checkIsFavorite = () => {
      /* UI-DEV-START */
      // 模拟API调用
      setTimeout(() => {
        // 随机设置收藏状态，模拟从服务器获取
        isFavorite.value = Math.random() > 0.5;
        
        // 输出模拟的查询参数 - 仅开发测试用
        console.log('检查收藏状态参数:', {
          user_id: 'user123', // 模拟用户ID
          item_id: tea.value?.id, // 茶叶ID
          item_type: 'tea' // 收藏项类型
        });
      }, 1000)
      /* UI-DEV-END */
      
      /*
      // 真实代码（开发UI时注释）
      // 如果用户未登录，直接返回
      if (!store.getters['user/isLoggedIn']) {
        isFavorite.value = false;
        return;
      }
      
      try {
        // 调用Vuex action检查是否已收藏
        const result = await store.dispatch('favorites/checkFavorite', {
          item_id: tea.value.id,
          item_type: 'tea'
        });
        
        // 更新收藏状态
        isFavorite.value = result.isFavorite;
      } catch (error) {
        console.error('检查收藏状态失败:', error);
        isFavorite.value = false;
      }
      */
    }
    
    // 模拟加载茶叶详情
    const loadTeaDetail = () => {
      loading.value = true
      
      // 模拟API调用延迟
      setTimeout(() => {
        const teaId = route.params.id
        
        // 创建模拟数据 - 更新了店铺相关信息和结构
        tea.value = {
          id: teaId,
          name: '商南特级绿茶 - 明前春茶',
          category_id: 1,
          category_name: '绿茶',
          price: 298,
          discount_price: 238,
          brief: '商南高山云雾茶，清香回甘，明前采摘',
          description: `
            <h3>茶叶简介</h3>
            <p>商南云雾茶产于海拔1200米以上的高山，四季云雾缭绕，日照充足，昼夜温差大，是培育优质茶叶的理想环境。</p>
            <h3>品质特点</h3>
            <p>本款明前春茶，芽头饱满，香气高扬，清爽甘醇，汤色嫩绿，叶底明亮。</p>
            <h3>冲泡建议</h3>
            <p>建议以80℃温水冲泡，投茶量5克，杯具以透明玻璃杯为佳，可以欣赏到茶叶舒展的美感。</p>
          `,
          stock: 120,
          sales: 256,
          origin: '陕西省商洛市商南县',
          main_image: 'https://img.zcool.cn/community/01a9d75fb0dd1ca801206621a38d83.jpg',
          images: [
            { id: 1, url: 'https://img.zcool.cn/community/01a9d75fb0dd1ca801206621a38d83.jpg', sort_order: 0, is_main: 1 },
            { id: 2, url: 'https://img.zcool.cn/community/01f6c65fb0dd1ca8012066210a4c8f.jpg', sort_order: 1, is_main: 0 },
            { id: 3, url: 'https://img.zcool.cn/community/0152bc5fb0dd1ca801206621f21b74.jpg', sort_order: 2, is_main: 0 },
            { id: 4, url: 'https://img.zcool.cn/community/01c1c85fb0dd1ca801206621a014c4.jpg', sort_order: 3, is_main: 0 }
          ],
          specifications: [
            { id: 1, tea_id: teaId, spec_name: '特级（50g）', price: 238, stock: 120, is_default: 1 },
            { id: 2, tea_id: teaId, spec_name: '特级（100g）', price: 458, stock: 85, is_default: 0 },
            { id: 3, tea_id: teaId, spec_name: '特级（200g礼盒装）', price: 888, stock: 32, is_default: 0 }
          ],
          status: 1,
          shop_id: Math.random() > 0.5 ? 'PLATFORM' : 'shop100001',
          shop_name: '商南茶业旗舰店',
          shop_logo: 'https://img.zcool.cn/community/01233056fb62fe32f875520f7b67cb.jpg',
          shop_rating: 4.8,
          shop_desc: '专营商南特产茶叶，传承百年制茶工艺，提供优质商南绿茶、红茶等各类茶品。',
          platform_logo: 'https://img.zcool.cn/community/01f6c65fb0dd1ca8012066210a4c8f.jpg',
          reviews: generateMockReviews(6)
        }
        
        // 默认选中默认规格
        const defaultSpec = tea.value.specifications.find(spec => spec.is_default === 1)
        if (defaultSpec) {
          selectedSpecId.value = defaultSpec.id
        }
        
        // 检查是否已收藏
        checkIsFavorite()
        
        loading.value = false
      }, 800)
    }
    
    // 计算属性 - 是否为平台直售
    const isPlatformTea = computed(() => {
      return tea.value && tea.value.shop_id === 'PLATFORM';
    })
    
    // 计算属性 - 当前选中的规格
    const selectedSpec = computed(() => {
      if (!tea.value || !selectedSpecId.value) return null
      return tea.value.specifications.find(spec => spec.id === selectedSpecId.value)
    })
    
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
        ElMessage.warning('该商品已售罄')
        return
      }
      
      if (!selectedSpecId.value) {
        ElMessage.warning('请先选择规格')
        return
      }
      
      submitting.value = true
      
      /* UI-DEV-START */
      // 模拟API调用
      setTimeout(() => {
        ElMessage.success('成功加入购物车')
        submitting.value = false
      }, 600)
      /* UI-DEV-END */
      
      /*
      // 真实代码（开发UI时注释）
      try {
        await store.dispatch('cart/addToCart', {
          tea_id: tea.value.id,
          quantity: quantity.value,
          spec_id: selectedSpecId.value
        })
        ElMessage.success('成功加入购物车')
      } catch (error) {
        ElMessage.error(error.message || '加入购物车失败')
      } finally {
        submitting.value = false
      }
      */
    }
    
    // 立即购买
    const buyNow = async () => {
      if (!canAddToCart.value) {
        ElMessage.warning('该商品已售罄')
        return
      }
      
      if (!selectedSpecId.value) {
        ElMessage.warning('请先选择规格')
        return
      }
      
      /* UI-DEV-START */
      // 模拟加入购物车然后跳转
      submitting.value = true
      setTimeout(() => {
        submitting.value = false
        // 跳转到结算页面，添加direct=1参数表示直接购买
        router.push('/order/checkout?direct=1')
      }, 600)
      /* UI-DEV-END */
      
      /*
      // 真实代码（开发UI时注释）
      try {
        submitting.value = true
        // 直接购买时，需要先将商品添加到Vuex的直接购买状态
        await store.dispatch('order/setDirectBuyItem', {
          tea_id: tea.value.id,
          quantity: quantity.value,
          spec_id: selectedSpecId.value,
          tea_name: tea.value.tea_name,
          image: tea.value.image,
          price: selectedSpec.value.price,
          spec_name: selectedSpec.value.spec_name,
          shop_id: tea.value.shop_id
        })
        // 跳转到结算页面，添加direct=1参数表示直接购买
        router.push('/order/checkout?direct=1')
      } catch (error) {
        ElMessage.error(error.message || '立即购买失败')
        submitting.value = false
      }
      */
    }
    
    // 跳转到店铺详情
    const goToShop = () => {
      // 如果是平台直售茶叶，不进行跳转
      if (isPlatformTea.value) {
        return;
      }
      
      // 否则跳转到对应的店铺详情页
      router.push(`/shop/${tea.value.shop_id}`);
    }
    
    // 联系店铺客服
    const contactShop = () => {
      // 如果是平台直售茶叶，不应显示联系按钮
      if (isPlatformTea.value) {
        return;
      }
      
      // 跳转到消息中心的私信聊天页面，传递店铺ID
      router.push(`/message/center/chat?shopId=${tea.value.shop_id}`);
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
      /* UI-DEV-START */
      loadTeaDetail()
      /* UI-DEV-END */
      
      /*
      // 真实代码（开发UI时注释）
      if (route.params.id) {
        loadTeaDetail()
      }
      
      // TODO-VUEX: 初始化Vuex模块
      // 在实际实现中，需要确保favorites模块已注册
      // store.dispatch('favorites/init');
      */
    })
    
    return {
      tea,
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
      contactShop
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