<template>
  <div class="tea-card" @click="viewDetail">
    <!-- 茶叶主图 -->
    <div class="tea-image">
      <SafeImage :src="tea.main_image" type="tea" :alt="tea.name" style="width:100%;height:100%;object-fit:cover;" />
      <span v-if="isPlatformTea" class="platform-tag">平台直售</span>
    </div>
    
    <!-- 茶叶信息 -->
    <div class="tea-info">
      <h3 class="tea-name" :title="tea.name">{{ tea.name }}</h3>
      
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
          :loading="loading"
        >
          <el-icon><ShoppingCart /></el-icon>
          加入购物车
        </el-button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
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
  setup(props) {
    const router = useRouter()
    const store = useStore()
    const loading = ref(false)
    
    // 判断是否为平台直售茶叶
    const isPlatformTea = computed(() => props.tea.shop_id === 'PLATFORM')
    
    // 查看详情
    const viewDetail = () => {
      router.push(`/tea/${props.tea.id}`)
    }
    
    // 加入购物车
    const addToCart = async () => {
      loading.value = true
      try {
        await store.dispatch('order/addToCart', {
          teaId: props.tea.id,
          quantity: 1
        })
        ElMessage.success('已添加到购物车')
      } catch (error) {
        ElMessage.error(error.message || '添加失败，请重试')
      } finally {
        loading.value = false
      }
    }
    
    return {
      loading,
      isPlatformTea,
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
    
    .platform-tag {
      position: absolute;
      top: 10px;
      left: 10px;
      background-color: var(--el-color-danger);
      color: white;
      font-size: 12px;
      padding: 2px 8px;
      border-radius: 4px;
      z-index: 1;
    }
  }
  
  .tea-info {
    padding: 12px;
    display: flex;
    flex-direction: column;
    flex: 1;
    
    .tea-name {
      margin: 0 0 8px 0;
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
</style> 