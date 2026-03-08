<template>
  <div class="tea-card" @click="viewDetail">
    <!-- 茶叶主图 -->
    <div class="tea-image">
      <SafeImage :src="tea.mainImage" type="tea" :alt="tea.name" style="width:100%;height:100%;object-fit:cover;" />
    </div>
    
    <!-- 茶叶信息 -->
    <div class="tea-info">
      <h3 class="tea-name" :title="tea.name">{{ tea.name }}</h3>
      
      <!-- 店铺标签：平台直售（红色）或商家店铺（绿色） -->
      <div class="shop-tag-container">
        <span v-if="isPlatformTea" class="shop-tag platform-tag">平台直售</span>
        <span v-else-if="shopName" class="shop-tag shop-tag-green">{{ shopName }}</span>
      </div>
      
      <div class="tea-brief">{{ tea.brief }}</div>
      
      <div class="tea-price-row">
        <div class="price-box">
          <span class="current-price">¥{{ tea.discount_price || tea.price }}</span>
          <span v-if="tea.discount_price" class="original-price">¥{{ tea.price }}</span>
        </div>
        <div class="sales">销量: {{ tea.sales }}</div>
      </div>
      
      <div class="tea-actions">
        <el-button 
          type="primary" 
          size="small" 
          @click.stop="addToCart"
        >
          <el-icon><ShoppingCart /></el-icon>
          加入购物车
        </el-button>
      </div>
    </div>
  </div>
</template>

<script>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

import { ShoppingCart } from '@element-plus/icons-vue'
import SafeImage from '@/components/common/form/SafeImage.vue'

export default {
  name: 'TeaCard',
  components: {
    ShoppingCart,
    SafeImage
  },
  props: {
    tea: {
      type: Object,
      required: true
    }
  },
  emits: ['add-to-cart'],
  setup(props, { emit }) {
    const router = useRouter()
    
    // 判断是否为平台直售茶叶
    const isPlatformTea = computed(() => {
      const shopId = props.tea.shop_id || props.tea.shopId
      return shopId === 'PLATFORM' || shopId === '0'
    })
    
    // 获取店铺名称
    const shopName = computed(() => {
      if (isPlatformTea.value) return null
      return props.tea.shopName || props.tea.shop_name || null
    })
    
    // 查看详情
    const viewDetail = () => {
      router.push(`/tea/${props.tea.id}`)
    }
    
    // 加入购物车
    const addToCart = () => {
      // 由父组件决定是直接加默认规格还是弹出规格选择框
      emit('add-to-cart', props.tea)
    }
    
    return {
      isPlatformTea,
      shopName,
      viewDetail,
      addToCart
    }
  }
}
</script>

<style lang="scss" scoped>
.tea-card {
  width: 100%;
  border-radius: 8px;
  background-color: #fff;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  transition: transform 0.3s, box-shadow 0.3s;
  cursor: pointer;
  overflow: hidden;
  position: relative;
  height: 100%;
  display: flex;
  flex-direction: column;
  
  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
  }
  
  .tea-image {
    position: relative;
    height: 0;
    padding-bottom: 100%; // 1:1 宽高比
    overflow: hidden;
    
    img {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.3s;
    }
  }
  
  .tea-info {
    padding: 12px;
    display: flex;
    flex-direction: column;
    flex: 1;
    
    .tea-name {
      margin: 0 0 -10px 0; // 缩减茶叶名字到图标之间的距离
      font-size: 16px;
      font-weight: 500;
      color: var(--el-text-color-primary);
      line-height: 1.4;
      height: 44px;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
    }
    
    .shop-tag-container {
      margin-bottom: -5px; // 缩减图标到价格之间的距离
      
      .shop-tag {
        display: inline-block;
        font-size: 12px;
        padding: 2px 8px;
        border-radius: 4px;
        font-weight: 500;
      }
      
      .platform-tag {
        background-color: #f56c6c; // 红色
        color: white;
      }
      
      .shop-tag-green {
        background-color: #67c23a; // 绿色
        color: white;
      }
    }
    
    .tea-brief {
      color: var(--el-text-color-secondary);
      font-size: 12px;
      line-height: 1.5;
      height: 36px;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      margin-bottom: 8px;
    }
    
    .tea-price-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;
      
      .price-box {
        .current-price {
          font-size: 18px;
          font-weight: bold;
          color: var(--el-color-danger);
        }
        
        .original-price {
          font-size: 12px;
          color: var(--el-text-color-secondary);
          text-decoration: line-through;
          margin-left: 4px;
        }
      }
      
      .sales {
        font-size: 12px;
        color: var(--el-text-color-secondary);
      }
    }
    
    .tea-actions {
      margin-top: auto; // 让按钮始终位于底部
      
      .el-button {
        width: 100%;
      }
    }
  }
}

// 列表模式样式（通过父级类名控制）
:deep(.view-list) .tea-card {
  flex-direction: row;
  height: auto;
  min-height: 150px;
  
  .tea-image {
    width: 150px;
    flex-shrink: 0;
    padding-bottom: 0;
    height: 150px;
    
    img {
      position: static;
      width: 100%;
      height: 100%;
    }
  }
  
  .tea-info {
    flex: 1;
    display: flex;
    flex-direction: row;
    align-items: center;
    padding: 20px;
    gap: 20px;
    
    .tea-name {
      flex: 1;
      height: auto;
      margin: 0;
      font-size: 18px;
      -webkit-line-clamp: 1;
    }
    
    .tea-brief {
      flex: 1;
      height: auto;
      margin: 0;
      -webkit-line-clamp: 1;
    }
    
    .tea-price-row {
      flex: 0 0 auto;
      margin: 0;
      flex-direction: column;
      align-items: flex-end;
      gap: 8px;
    }
    
    .tea-actions {
      flex: 0 0 auto;
      margin: 0;
      
      .el-button {
        width: auto;
        min-width: 120px;
      }
    }
  }
}
</style> 