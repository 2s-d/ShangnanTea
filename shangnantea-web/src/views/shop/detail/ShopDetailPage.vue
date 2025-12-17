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
            <SafeImage :src="shop.logo" type="banner" :alt="shop.shop_name || '商铺'" style="width:100px;height:100px;border-radius:50%;" />
            <div v-if="shop.certification && shop.certification.status === 'verified'" class="cert-badge">
              <el-tooltip content="已认证商家" placement="top">
                <el-icon><Check /></el-icon>
              </el-tooltip>
            </div>
          </div>
          
          <div class="shop-basic-info">
            <div class="shop-name-row">
              <h1 class="shop-name">{{ shop.shop_name }}</h1>
              
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
            
            <div class="shop-brief">{{ shop.description }}</div>
            
            <div class="shop-stats">
              <div class="stat-item">
                <span class="stat-label">商品数</span>
                <span class="stat-value">{{ shopTeas.length }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">销量</span>
                <span class="stat-value">{{ shop.sales_count }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">关注数</span>
                <span class="stat-value">{{ shop.follow_count || 0 }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">开店时间</span>
                <span class="stat-value">{{ formatTime(shop.create_time) }}</span>
              </div>
            </div>
          </div>
        </div>
        
        <div class="shop-announcement" v-if="shop.announcement">
          <el-alert
            :title="shop.announcement"
            type="info"
            :closable="false"
            show-icon
          />
        </div>
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
          
          <el-tab-pane label="店铺信息" name="info">
            <div class="shop-detail-info">
              <el-descriptions title="基本信息" :column="1" border>
                <el-descriptions-item label="店铺名称">{{ shop.shop_name }}</el-descriptions-item>
                <el-descriptions-item label="店铺介绍">{{ shop.description }}</el-descriptions-item>
                <el-descriptions-item label="联系电话">{{ shop.contact_phone }}</el-descriptions-item>
                <el-descriptions-item label="所在地区">
                  {{ `${shop.province} ${shop.city} ${shop.district}` }}
                </el-descriptions-item>
                <el-descriptions-item label="详细地址">{{ shop.address }}</el-descriptions-item>
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
/* UI-DEV-START */
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Back, Check, Star, ChatLineRound } from '@element-plus/icons-vue'
import TeaCard from '@/components/tea/card/TeaCard.vue'
import SafeImage from '@/components/common/form/SafeImage.vue'
/* UI-DEV-END */

/*
// 真实代码（开发UI时注释）
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { Back, Check, Star, ChatLineRound } from '@element-plus/icons-vue'
import TeaCard from '@/components/tea/card/TeaCard.vue'
import SafeImage from '@/components/common/form/SafeImage.vue'
*/

export default {
  name: "ShopDetailPage",
  components: {
    /* UI-DEV-START */
    Back,
    Check,
    Star,
    ChatLineRound,
    TeaCard,
    SafeImage
    /* UI-DEV-END */
    
    /*
    // 真实代码（开发UI时注释）
    Back,
    Check,
    Star,
    ChatLineRound,
    TeaCard,
    SafeImage
    */
  },
  setup() {
    const router = useRouter()
    const route = useRoute()
    const loading = ref(true)
    const activeTab = ref('products')
    const shop = ref(null)
    const shopTeas = ref([])
    const isFollowing = ref(false)
    const followLoading = ref(false)
    const defaultLogo = '/placeholder-shop.jpg'
    const defaultShopLogo = '/images/shops/default.jpg'
    
    /* UI-DEV-START */
    // 加载店铺数据
    const loadShopDetail = () => {
      loading.value = true
      
      // 模拟API调用延迟
      setTimeout(() => {
        const shopId = route.params.id
        
        // 确保不是展示平台直售店铺
        if (shopId === 'PLATFORM') {
          ElMessage.error('平台直售不是实体店铺，无法查看详情')
          router.push('/shop/list')
          return
        }
        
        // 模拟店铺数据
        shop.value = {
          id: shopId,
          owner_id: 'cy100003',
          shop_name: '商南茶业旗舰店',
          logo: '/mock-images/shop-logo-1.jpg',
          banner: '/mock-images/shop-banner-1.jpg',
          description: '专营商南特产茶叶，传承百年制茶工艺，提供优质商南绿茶、红茶等各类茶品，欢迎品鉴选购！',
          announcement: '本店新到一批明前春茶，欢迎品尝选购！',
          contact_phone: '13800001001',
          province: '陕西省',
          city: '商洛市',
          district: '商南县',
          address: '城关街道茶叶市场12号',
          status: 1,
          rating: 4.8,
          rating_count: 56,
          follow_count: 120,
          sales_count: 389,
          create_time: '2023-06-15 09:03:26',
          update_time: '2025-03-26 09:03:26',
          certification: {
            status: 'verified',
            type: '企业认证',
            expireTime: '2025-12-31'
          }
        }
        
        // 模拟店铺茶叶数据 - 确保只显示属于该店铺的茶叶（非平台直售）
        const teasCount = Math.floor(Math.random() * 6) + 5  // 5-10个茶叶
        const teas = []
        
        for (let i = 1; i <= teasCount; i++) {
          const price = Math.floor(Math.random() * 400) + 100
          
          teas.push({
            id: `tea${shopId.substring(4)}${i}`,
            name: `${shop.value.shop_name.substring(0, 4)}${i % 2 === 0 ? '特级绿茶' : '特级红茶'} ${i}号`,
            shop_id: shopId, // 确保茶叶属于当前店铺
            shop_name: shop.value.shop_name,
            category_id: i % 2 === 0 ? 1 : 2, // 1=绿茶，2=红茶
            price: price,
            discount_price: Math.random() > 0.5 ? Math.floor(price * 0.8) : null,
            brief: `来自商南高山的${i % 2 === 0 ? '绿茶' : '红茶'}，品质上乘，口感醇厚`,
            image_url: `/mock-images/tea-${i % 8 + 1}.jpg`,
            description: '高山茶叶，品质保证',
            origin: '陕西省商洛市商南县',
            stock: Math.floor(Math.random() * 100) + 20,
            sales: Math.floor(Math.random() * 200) + 10,
            main_image: `/mock-images/tea-${i % 8 + 1}.jpg`,
            status: 1,
            create_time: '2025-03-26 09:03:26',
            update_time: '2025-03-26 09:03:26'
          })
        }
        
        shopTeas.value = teas
        
        // 随机设置是否关注
        isFollowing.value = Math.random() > 0.5
        
        loading.value = false
      }, 800)
    }
    
    // 切换关注状态
    const toggleFollow = () => {
      followLoading.value = true
      
      // 模拟API调用
      setTimeout(() => {
        isFollowing.value = !isFollowing.value
        
        // 更新关注数
        if (isFollowing.value) {
          shop.value.follow_count = (shop.value.follow_count || 0) + 1
        } else {
          shop.value.follow_count = Math.max((shop.value.follow_count || 0) - 1, 0)
        }
        
        ElMessage.success(isFollowing.value ? '已关注店铺' : '已取消关注')
        followLoading.value = false
      }, 500)
    }
    
    // 格式化时间
    const formatTime = (timeString) => {
      if (!timeString) return '未知'
      const date = new Date(timeString)
      return `${date.getFullYear()}-${date.getMonth() + 1}-${date.getDate()}`
    }
    
    // 联系店铺客服
    const contactShop = () => {
      // 跳转到消息中心的私信聊天页面，传递店铺ID
      router.push(`/message/center/chat?shopId=${shop.value.id}`)
    }
    /* UI-DEV-END */
    
    /*
    // 真实代码（开发UI时注释）
    const store = useStore()
    
    // 从store获取店铺和茶叶数据
    const shop = computed(() => store.state.shop.currentShop)
    const shopTeas = computed(() => {
      // 确保只返回属于当前店铺的茶叶（非平台直售）
      return store.state.shop.shopTeas.filter(tea => 
        tea.shop_id === route.params.id && tea.shop_id !== 'PLATFORM'
      )
    })
    const isFollowing = computed(() => store.getters['user/isFollowingShop'](route.params.id))
    
    // 监听路由参数变化
    watch(
      () => route.params.id,
      (shopId) => {
        if (shopId) {
          // 如果是平台直售，不允许查看
          if (shopId === 'PLATFORM') {
            ElMessage.error('平台直售不是实体店铺，无法查看详情')
            router.push('/shop/list')
            return
          }
          loadShopDetail(shopId)
        }
      },
      { immediate: true }
    )
    
    // 加载店铺详情
    const loadShopDetail = async (shopId) => {
      loading.value = true
      
      try {
        await Promise.all([
          store.dispatch('shop/fetchShopDetail', { shopId }),
          store.dispatch('shop/fetchShopTeas', { shopId })
        ])
        
        // 处理shopTeas数据初始化
        const shopTeaData = store.state.shop.shopTeas || []
        // 不需要额外赋值，因为使用了computed属性
      } catch (error) {
        ElMessage.error(error.message || '加载店铺数据失败')
      } finally {
        loading.value = false
      }
    }
    
    // 切换关注状态
    const toggleFollow = async () => {
      followLoading.value = true
      
      try {
        await store.dispatch(
          isFollowing.value ? 'user/unfollowShop' : 'user/followShop', 
          { shopId: shop.value.id }
        )
        
        ElMessage.success(isFollowing.value ? '已取消关注' : '已关注店铺')
      } catch (error) {
        ElMessage.error(error.message || '操作失败')
      } finally {
        followLoading.value = false
      }
    }
    
    // 格式化时间
    const formatTime = (timeString) => {
      if (!timeString) return '未知'
      const date = new Date(timeString)
      return `${date.getFullYear()}-${date.getMonth() + 1}-${date.getDate()}`
    }
    */
    
    // 返回上一页
    const goBack = () => {
      router.back()
    }
    
    // 返回店铺列表
    const goToShopList = () => {
      router.push('/shop/list')
    }
    
    onMounted(() => {
      /* UI-DEV-START */
      loadShopDetail()
      /* UI-DEV-END */
      
      /*
      // 真实代码（开发UI时注释）
      // 通过路由watch自动触发loadShopDetail
      */
    })
    
    return {
      shop,
      shopTeas,
      loading,
      activeTab,
      defaultLogo,
      defaultShopLogo,
      isFollowing,
      followLoading,
      formatTime,
      toggleFollow,
      goBack,
      goToShopList,
      contactShop
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