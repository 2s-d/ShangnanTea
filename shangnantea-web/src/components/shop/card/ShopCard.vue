<template>
  <div class="shop-card" @click="goToShopDetail">
    <!-- 店铺基本信息区域 -->
    <div class="shop-info">
      <div class="shop-logo">
        <SafeImage :src="shop.logo" type="banner" :alt="shop.name" style="width:100%;height:100%;object-fit:cover;" />
        <div v-if="shop.certification && shop.certification.status === 'verified'" class="cert-badge">
          <el-tooltip content="已认证商家" placement="top">
            <el-icon><Check /></el-icon>
          </el-tooltip>
        </div>
      </div>
      
      <div class="shop-details">
        <h3 class="shop-name">{{ shop.name }}</h3>
        
        <div class="shop-rating">
          <el-rate
            v-model="shop.rating"
            disabled
            text-color="#ff9900"
            score-template="{value}"
          />
        </div>
        
        <div class="shop-brief">
          {{ shop.description || '这家店铺暂无简介' }}
        </div>
        
        <div class="shop-stats">
          <span class="stat-item">
            <el-icon><Goods /></el-icon>
            商品: {{ shopTeaCount }}
          </span>
          <span class="stat-item">
            <el-icon><ShoppingCart /></el-icon>
            销量: {{ shop.sales_count || shop.salesCount || 0 }}
          </span>
          <span class="stat-item">
            <el-icon><Timer /></el-icon>
            店龄: {{ formatExistTime(shop.createTime) }}
          </span>
        </div>
      </div>
    </div>
    
    <!-- 店铺茶叶展示区域 -->
    <div class="shop-teas">
      <div v-if="featuredTeas.length > 0" class="tea-list">
        <div v-for="tea in featuredTeas" :key="tea.id" class="tea-preview">
          <div class="tea-preview-image">
            <SafeImage :src="tea.mainImage" type="tea" :alt="tea.name" style="width:100%;height:100%;object-fit:cover;" />
          </div>
          <div class="tea-preview-info">
            <div class="tea-preview-name">{{ tea.name }}</div>
            <div class="tea-preview-price">¥{{ tea.price }}</div>
          </div>
        </div>
      </div>
      <div v-else class="no-teas">
        <p>暂无商品</p>
      </div>
    </div>
  </div>
</template>

<script>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { Check, Goods, ShoppingCart, Timer } from '@element-plus/icons-vue'
import SafeImage from '@/components/common/form/SafeImage.vue'

export default {
  name: 'ShopCard',
  components: {
    Check,
    Goods,
    ShoppingCart,
    Timer,
    SafeImage
  },
  props: {
    shop: {
      type: Object,
      required: true
    },
    shopTeas: {
      type: Array,
      default: () => []
    },
    productCount: {
      type: Number,
      default: null
    }
  },
  setup(props) {
    const router = useRouter()
    
    // 获取店铺的茶叶展示（最多2个，保持固定样式）
    const featuredTeas = computed(() => {
      return props.shopTeas.slice(0, 2)
    })
    
    // 计算店铺茶叶数量（优先使用总数，其次使用预览数量）
    const shopTeaCount = computed(() => {
      if (typeof props.productCount === 'number') {
        return props.productCount
      }
      return props.shopTeas.length
    })
    
    // 格式化店铺存在时间（店龄）
    const formatExistTime = timeString => {
      if (!timeString) return '未知'
      const createDate = new Date(timeString)
      if (Number.isNaN(createDate.getTime())) return '未知'
      
      const now = new Date()
      const diffMs = now.getTime() - createDate.getTime()
      if (diffMs <= 0) return '1个月'
      
      const diffMonths = Math.floor(diffMs / (1000 * 60 * 60 * 24 * 30)) // 约30天一月
      if (diffMonths < 12) {
        const months = Math.max(1, diffMonths)
        return `${months}个月`
      }
      
      const years = Math.floor(diffMonths / 12)
      const remainMonths = diffMonths - years * 12
      let displayYears = years
      // 超过一年6个月按下一年算
      if (remainMonths >= 6) {
        displayYears += 1
      }
      return `${displayYears}年`
    }
    
    // 跳转到店铺详情
    const goToShopDetail = () => {
      router.push(`/shop/${props.shop.id}`)
    }
    
    return {
      featuredTeas,
      shopTeaCount,
      formatExistTime,
      goToShopDetail
    }
  }
}
</script>

<style lang="scss" scoped>
.shop-card {
  display: flex;
  width: 100%;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  overflow: hidden;
  cursor: pointer;
  margin-bottom: 16px;
  
  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
  }
  
  .shop-info {
    flex: 1;
    display: flex;
    padding: 16px;
    border-right: 1px solid var(--el-border-color-lighter);
    
    .shop-logo {
      position: relative;
      width: 80px;
      height: 80px;
      flex-shrink: 0;
      margin-right: 16px;
      border-radius: 8px;
      overflow: hidden;
      
      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
      
      .cert-badge {
        position: absolute;
        right: -4px;
        top: -4px;
        background-color: var(--el-color-success);
        color: white;
        border-radius: 50%;
        width: 20px;
        height: 20px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 12px;
      }
    }
    
    .shop-details {
      flex: 1;
    display: flex;
    flex-direction: column;
      
      .shop-name {
        margin: 0 0 8px;
        font-size: 18px;
        font-weight: 600;
        color: var(--el-text-color-primary);
        display: -webkit-box;
        -webkit-line-clamp: 1;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }
      
      .shop-rating {
        display: flex;
        align-items: center;
        margin-bottom: 8px;
        
        .rating-count {
          margin-left: 4px;
          font-size: 12px;
          color: var(--el-text-color-secondary);
        }
      }
      
      .shop-brief {
        margin-bottom: 8px;
        font-size: 14px;
        color: var(--el-text-color-regular);
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
        line-height: 1.4;
        height: 40px;
      }
      
      .shop-stats {
        display: flex;
        flex-wrap: wrap;
        margin-top: auto;
        
        .stat-item {
          display: inline-flex;
          align-items: center;
          margin-right: 16px;
          font-size: 12px;
          color: var(--el-text-color-secondary);
          
          .el-icon {
            margin-right: 4px;
            font-size: 14px;
          }
        }
      }
    }
  }
  
  .shop-teas {
    width: 320px;
    flex-shrink: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .tea-list {
      width: 100%;
      display: flex;
      flex-direction: row;
      justify-content: flex-start;
      padding: 12px 16px;
      gap: 12px;
      
      .tea-preview {
        flex: 0 0 calc(50% - 6px); // 固定宽度，每个占50%减去gap的一半
        max-width: calc(50% - 6px);
        display: flex;
        flex-direction: column;
        align-items: flex-start;
        
        .tea-preview-image {
          width: 100%;
          height: 90px;
          border-radius: 4px;
          overflow: hidden;
          margin-bottom: 6px;
          
          img {
            width: 100%;
            height: 100%;
            object-fit: cover;
          }
        }
        
        .tea-preview-info {
          width: 100%;
          
          .tea-preview-name {
            font-size: 13px;
            color: var(--el-text-color-primary);
            margin-bottom: 2px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
          
          .tea-preview-price {
            font-size: 14px;
            font-weight: 600;
            color: var(--el-color-danger);
          }
        }
      }
    }
    
    .no-teas {
      display: flex;
      align-items: center;
      justify-content: center;
      height: 100%;
      color: var(--el-text-color-secondary);
      font-size: 14px;
      padding: 20px;
      text-align: center;
    }
  }
}
</style> 