<template>
  <div class="cart-page">
    <div class="container">
      <!-- 页面标题 -->
      <div class="page-header">
        <h1 class="page-title">我的购物车</h1>
      </div>
      
      <!-- 购物车内容 -->
      <div class="cart-content" v-loading="loading">
        <!-- 有商品时显示 -->
        <template v-if="cartItems.length > 0">
          <!-- 购物车表头 -->
          <div class="cart-header">
            <div class="header-checkbox">
              <el-checkbox 
                v-model="selectAll" 
                @change="handleSelectAllChange"
                :indeterminate="isIndeterminate"
              >全选</el-checkbox>
            </div>
            <div class="header-info">商品信息</div>
            <div class="header-price">单价</div>
            <div class="header-quantity">数量</div>
            <div class="header-subtotal">小计</div>
            <div class="header-action">操作</div>
          </div>
          
          <!-- 购物车商品列表 -->
          <div class="cart-list">
            <div 
              v-for="item in cartItems" 
              :key="item.id" 
              class="cart-item"
            >
              <div class="item-checkbox">
                <el-checkbox v-model="item.selected" @change="handleItemSelectChange"></el-checkbox>
              </div>
              
              <div class="item-info" @click="goToTeaDetail(item.teaId)">
                <div class="item-image">
                  <SafeImage :src="item.teaImage" type="tea" :alt="item.teaName" style="width:80px;height:80px;object-fit:cover;" />
                </div>
                <div class="item-details">
                  <div class="item-name">{{ item.teaName }}</div>
                  <div class="item-spec">规格：{{ item.specName }}</div>
                  <div class="item-stock" v-if="item.stock !== undefined">
                    <el-tag 
                      :type="item.stock >= item.quantity ? 'success' : 'danger'" 
                      size="small"
                    >
                      库存：{{ item.stock }}
                    </el-tag>
                  </div>
                  <div class="item-from">
                    <el-tag size="small" type="info">{{ isPlatformProduct(item.shopId) ? '平台直售' : '店铺商品' }}</el-tag>
                    <span v-if="!isPlatformProduct(item.shopId)" class="shop-name">{{ item.shopName }}</span>
                  </div>
                </div>
              </div>
              
              <div class="item-price">¥{{ item.price.toFixed(2) }}</div>
              
              <div class="item-quantity">
                <el-input-number 
                  v-model="item.quantity" 
                  :min="1" 
                  :max="item.stock !== undefined ? Math.min(item.stock, 99) : 99"
                  size="small"
                  @change="handleQuantityChange(item)"
                />
              </div>
              
              <div class="item-subtotal">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
              
              <div class="item-action">
                <el-button 
                  type="text" 
                  @click.stop="selectSpecification(item)"
                  class="spec-btn"
                >选规格</el-button>
                <el-button 
                  type="text" 
                  @click.stop="removeItem(item.id)"
                  class="delete-btn"
                >删除</el-button>
              </div>
            </div>
          </div>
          
          <!-- 购物车底部结算栏 -->
          <div class="cart-footer">
            <div class="footer-left">
              <el-checkbox 
                v-model="selectAll" 
                @change="handleSelectAllChange"
              >全选</el-checkbox>
              <el-button 
                type="text" 
                @click="removeSelectedItems"
                class="delete-selected-btn"
              >删除选中商品</el-button>
            </div>
            
            <div class="footer-right">
              <div class="summary">
                <span class="selected-count">已选择 <em>{{ selectedCount }}</em> 件商品</span>
                <span class="total-price">合计：<em>¥{{ totalPrice.toFixed(2) }}</em></span>
              </div>
              <el-button 
                type="primary" 
                size="large" 
                :disabled="selectedCount === 0"
                @click="checkout"
                class="checkout-btn"
              >去结算</el-button>
            </div>
          </div>
        </template>
        
        <!-- 空购物车时显示 -->
        <div v-else class="empty-cart">
          <el-empty description="您的购物车还是空的">
            <template #description>
              <p>您的购物车还是空的，快去选购喜欢的茶叶吧！</p>
            </template>
            <el-button type="primary" @click="goToTeaMall">去购物</el-button>
          </el-empty>
        </div>
      </div>
    </div>
    
    <!-- 规格选择对话框 -->
    <el-dialog
      v-model="specDialogVisible"
      title="选择规格"
      width="400px"
      destroy-on-close
    >
      <div class="spec-select-container">
        <div class="current-tea-info" v-if="currentSpecTea">
          <SafeImage :src="currentSpecTea.teaImage" type="tea" :alt="currentSpecTea.teaName" class="tea-image" style="width:80px;height:80px;object-fit:cover;" />
          <div class="tea-info">
            <div class="tea-name">{{ currentSpecTea.teaName }}</div>
            <div class="tea-price">¥{{ selectedSpecPrice.toFixed(2) }}</div>
            <div class="selected-spec">已选规格：{{ selectedSpecName }}</div>
          </div>
        </div>
        
        <div class="spec-options">
          <div class="spec-title">选择规格</div>
          <div class="spec-list">
            <el-radio-group v-model="tempSelectedSpecId">
              <el-radio 
                v-for="spec in availableSpecs" 
                :key="spec.id" 
                :label="spec.id"
                class="spec-radio"
              >
                <div class="spec-item">
                  <span class="spec-name">{{ spec.spec_name }}</span>
                  <span class="spec-price">¥{{ spec.price.toFixed(2) }}</span>
                </div>
              </el-radio>
            </el-radio-group>
          </div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="specDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmSpecChange">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useOrderStore } from '@/stores/order'
import { useTeaStore } from '@/stores/tea'
import { ElMessageBox } from 'element-plus'
import { showByCode, isSuccess } from '@/utils/apiMessages'
import { orderPromptMessages } from '@/utils/promptMessages'
import SafeImage from '@/components/common/form/SafeImage.vue'
export default {
  name: 'CartPage',
  components: {
    SafeImage
  },
  setup() {
    const orderStore = useOrderStore()
    const teaStore = useTeaStore()
    const router = useRouter()
    const loading = ref(false)
    const cartItems = ref([])
    const selectAll = ref(false)
    const cartCount = ref(0)
    
    // 规格选择相关状态
    const specDialogVisible = ref(false)
    const currentSpecTea = ref(null)
    const currentCartItemId = ref(null)
    const availableSpecs = ref([])
    const tempSelectedSpecId = ref(null)
  
    const initCartData = async () => {
      loading.value = true
      try {
        const res = await orderStore.fetchCartItems()
        // res = {code, data}
        if (res) {
          // 显示API响应消息（成功或失败都通过状态码映射显示）
          showByCode(res.code)
          
          // 只有成功时才处理数据
          if (isSuccess(res.code)) {
            const items = res.data || []
            cartItems.value = items.map(i => ({
              ...i,
              selected: typeof i.selected === 'boolean' ? i.selected : true
            }))
            updateSelectAllStatus()
          }
        }
      } catch (error) {
        // 网络错误等已由响应拦截器处理，这里只记录日志
        if (process.env.NODE_ENV === 'development') {
          console.error('加载购物车失败:', error)
        }
      } finally {
        loading.value = false
      }
    }
    
    // 判断是否为平台直售商品
    const isPlatformProduct = shopId => {
      return shopId === 'PLATFORM'
    }
    
    // 选中项数量
    const selectedCount = computed(() => {
      return cartItems.value.filter(item => item.selected).length
    })
    
    // 全选是否为半选状态
    const isIndeterminate = computed(() => {
      return selectedCount.value > 0 && selectedCount.value < cartItems.value.length
    })
    
    // 计算总价
    const totalPrice = computed(() => {
      return cartItems.value
        .filter(item => item.selected)
        .reduce((total, item) => total + item.price * item.quantity, 0)
    })
    
    // 当前选中规格名称
    const selectedSpecName = computed(() => {
      if (!tempSelectedSpecId.value || !availableSpecs.value.length) return ''
      const spec = availableSpecs.value.find(s => s.id === tempSelectedSpecId.value)
      return spec ? spec.spec_name : ''
    })
    
    // 当前选中规格价格
    const selectedSpecPrice = computed(() => {
      if (!tempSelectedSpecId.value || !availableSpecs.value.length) return 0
      const spec = availableSpecs.value.find(s => s.id === tempSelectedSpecId.value)
      return spec ? spec.price : 0
    })
    
    // 更新全选状态
    const updateSelectAllStatus = () => {
      const hasItems = cartItems.value.length > 0
      selectAll.value = hasItems && cartItems.value.every(item => item.selected)
    }
    
    // 处理全选变化
    const handleSelectAllChange = val => {
      cartItems.value.forEach(item => {
        item.selected = val
      })
    }
    
    // 处理单个商品选中状态变化
    const handleItemSelectChange = () => {
      updateSelectAllStatus()
    }
    
    // 处理数量变化
    const handleQuantityChange = async item => {
      // 检查库存
      if (item.stock !== undefined && item.quantity > item.stock) {
        orderPromptMessages.showStockInsufficient(item.stock)
        item.quantity = item.stock
        return
      }
      
      try {
        const res = await orderStore.updateCartItem({
          id: item.id,
          quantity: item.quantity
        })
        // res = {code, data}
        if (res && res.code !== 200) {
          showByCode(res.code)
        }
      } catch (error) {
        // 网络错误等已由响应拦截器处理，这里只记录日志
        if (process.env.NODE_ENV === 'development') {
          console.error('更新数量失败:', error)
        }
        // 恢复：重新拉取购物车
        await initCartData()
      }
    }
    
    // 选择规格
    const selectSpecification = async item => {
      currentCartItemId.value = item.id
      currentSpecTea.value = item
      tempSelectedSpecId.value = item.specId || null
      
      // 获取茶叶的规格列表
      try {
        loading.value = true
        const res = await teaStore.fetchTeaSpecifications(item.teaId)
        availableSpecs.value = res?.data || res || []
        
        // 如果没有规格，提示用户
        if (availableSpecs.value.length === 0) {
          orderPromptMessages.showNoSpecAvailable()
          return
        }
        
        specDialogVisible.value = true
      } catch (error) {
        // 网络错误等已由响应拦截器处理，这里只记录日志
        if (process.env.NODE_ENV === 'development') {
          console.error('加载规格失败:', error)
        }
      } finally {
        loading.value = false
      }
    }
    
    // 确认规格变更
    const confirmSpecChange = async () => {
      if (!tempSelectedSpecId.value || !currentCartItemId.value) {
        orderPromptMessages.showSpecRequired()
        return
      }

      // 如果选择的规格和当前规格相同，直接关闭
      const currentItem = cartItems.value.find(item => item.id === currentCartItemId.value)
      if (currentItem && currentItem.specId === tempSelectedSpecId.value) {
        specDialogVisible.value = false
        return
      }

      try {
        loading.value = true
        // 更新购物车商品的规格
        const res = await orderStore.updateCartItem({
          id: currentCartItemId.value,
          specificationId: tempSelectedSpecId.value
        })
        // res = {code, data}
        if (res && res.code !== 200) {
          showByCode(res.code)
        }
        specDialogVisible.value = false
        // 重新获取购物车数据以更新价格和库存
        await initCartData()
      } catch (error) {
        // 网络错误等已由响应拦截器处理，这里只记录日志
        if (process.env.NODE_ENV === 'development') {
          console.error('更新规格失败:', error)
        }
      } finally {
        loading.value = false
      }
    }
    
    // 移除商品
    const removeItem = id => {
      ElMessageBox.confirm('确定要从购物车中移除该商品吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        orderStore.removeFromCart(id).then(async res => {
          // res = {code, data}
          if (res && res.code !== 200) {
            showByCode(res.code)
          }
          // 刷新本地列表
          await initCartData()
        }).catch(error => {
          // 网络错误等已由响应拦截器处理，这里只记录日志
          if (process.env.NODE_ENV === 'development') {
            console.error('移除商品失败:', error)
          }
        })
      }).catch(() => {
        // 用户取消操作
      })
    }
    
    // 移除选中商品
    const removeSelectedItems = () => {
      if (selectedCount.value === 0) {
        orderPromptMessages.showDeleteSelectionRequired()
        return
      }
      
      ElMessageBox.confirm('确定要删除选中的商品吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        const selectedIds = cartItems.value
          .filter(item => item.selected)
          .map(item => item.id)

        Promise.all(selectedIds.map(id => orderStore.removeFromCart(id))).then(async results => {
          // 检查是否有失败的
          const failedRes = results.find(res => res && res.code !== 200 && res.code !== 4013)
          if (failedRes) {
            showByCode(failedRes.code)
          }
          await initCartData()
        }).catch(error => {
          // 网络错误等已由响应拦截器处理，这里只记录日志
          if (process.env.NODE_ENV === 'development') {
            console.error('删除商品失败:', error)
          }
        })
      }).catch(() => {
        // 用户取消操作
      })
    }
    
    // 去结算
    const checkout = () => {
      if (selectedCount.value === 0) {
        orderPromptMessages.showSelectionRequired()
        return
      }

      // TODO-SCRIPT: 结算页需要支持从购物车选择的项（建议通过 query 传 selectedIds，或在 Vuex 中存储临时选择）
      const selectedIds = cartItems.value.filter(item => item.selected).map(item => item.id)
      router.push({ path: '/order/checkout', query: { selectedIds: selectedIds.join(',') } })
    }
    
    // 跳转到茶叶详情页
    const goToTeaDetail = teaId => {
      router.push(`/tea/${teaId}`)
    }
    
    // 跳转到茶叶商城
    const goToTeaMall = () => {
      router.push('/tea/mall')
    }
    
    // 更新购物车数量方法
    const updateCartCountBadge = count => {
      // 更新localStorage中的购物车数量
      localStorage.setItem('cartCount', count.toString())
      
      // 尝试更新DOM中的购物车数量标识
      const cartBadge = document.querySelector('.cart-badge')
      if (cartBadge) {
        cartBadge.textContent = count.toString()
        cartBadge.style.display = count > 0 ? 'block' : 'none'
      }
      
      // 发送自定义事件，通知其他组件购物车数量变化
      // 这对于不同组件间的通信很有用
      window.dispatchEvent(new CustomEvent('cart-updated', { detail: { count } }))
      
      // 将当前购物车数量更新到状态
      cartCount.value = count
    }
    
    // 添加默认图片常量
    const defaultTeaImage = '@/assets/images/teas/default.jpg'
    
    // 初始化时更新购物车数量
    onMounted(() => {
      // 初始化购物车数据
      initCartData()
      
      // 立即从localStorage读取购物车数量并更新标识（即使数据还未加载完成）
      const storedCount = localStorage.getItem('cartCount')
      if (storedCount) {
        const count = parseInt(storedCount, 10)
        updateCartCountBadge(count)
      }
    })
    
    // 当cartItems改变时更新购物车数量
    watch(cartItems, newItems => {
      // 购物车项的数量（不同商品的数量，而不是总数量）
      const itemCount = newItems.length
      
      // 更新购物车数量标识
      updateCartCountBadge(itemCount)
    }, { deep: true })
    
    return {
      loading,
      cartItems,
      selectAll,
      isIndeterminate,
      selectedCount,
      totalPrice,
      isPlatformProduct,
      
      // 规格选择相关
      specDialogVisible,
      currentSpecTea,
      availableSpecs,
      tempSelectedSpecId,
      selectedSpecName,
      selectedSpecPrice,
      
      // 事件处理方法
      handleSelectAllChange,
      handleItemSelectChange,
      handleQuantityChange,
      selectSpecification,
      confirmSpecChange,
      removeItem,
      removeSelectedItems,
      checkout,
      goToTeaDetail,
      goToTeaMall,
      updateCartCountBadge,
      
      // 添加默认图片常量
      defaultTeaImage
    }
  }
}
</script>

<style lang="scss" scoped>
.cart-page {
  padding: 20px 0 40px;
  min-height: 100vh;
  background-color: #f5f7fa;
  
  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 15px;
  }
  
  .page-header {
    margin-bottom: 20px;
    
    .page-title {
      font-size: 24px;
      font-weight: 600;
      color: var(--el-text-color-primary);
    }
  }
  
  .cart-content {
    background-color: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
    overflow: hidden;
  }
  
  .cart-header {
    display: flex;
    align-items: center;
    padding: 15px;
    background-color: #f8f8f8;
    font-weight: 500;
    border-bottom: 1px solid var(--el-border-color-lighter);
    
    .header-checkbox {
      width: 70px;
      padding-left: 20px;
    }
    
    .header-info {
      flex: 1;
    }
    
    .header-price {
      width: 120px;
      text-align: center;
    }
    
    .header-quantity {
      width: 150px;
      text-align: center;
    }
    
    .header-subtotal {
      width: 120px;
      text-align: center;
    }
    
    .header-action {
      width: 120px;
      text-align: center;
    }
  }
  
  .cart-list {
    .cart-item {
      display: flex;
      align-items: center;
      padding: 20px 15px;
      border-bottom: 1px solid var(--el-border-color-lighter);
      
      &:last-child {
        border-bottom: none;
      }
      
      .item-checkbox {
        width: 70px;
        padding-left: 20px;
      }
      
      .item-info {
        flex: 1;
        display: flex;
        align-items: center;
        cursor: pointer;
        transition: opacity 0.3s;
        
        &:hover {
          opacity: 0.8;
        }
        
        .item-image {
          width: 80px;
          height: 80px;
          margin-right: 15px;
          border-radius: 4px;
          overflow: hidden;
          
          img {
            width: 100%;
            height: 100%;
            object-fit: cover;
          }
        }
        
        .item-details {
          flex: 1;
          
          .item-name {
            font-size: 16px;
            margin-bottom: 8px;
            color: var(--el-text-color-primary);
            line-height: 1.4;
          }
          
          .item-spec {
            font-size: 14px;
            color: var(--el-text-color-secondary);
            margin-bottom: 8px;
          }
          
          .item-from {
            margin-top: 5px;
            display: flex;
            align-items: center;
            
            .shop-name {
              margin-left: 8px;
              font-size: 14px;
              color: var(--el-text-color-secondary);
            }
          }
        }
      }
      
      .item-price {
        width: 120px;
        text-align: center;
        color: var(--el-text-color-secondary);
      }
      
      .item-quantity {
        width: 150px;
        text-align: center;
      }
      
      .item-subtotal {
        width: 120px;
        text-align: center;
        font-weight: 500;
        color: var(--el-color-danger);
      }
      
      .item-action {
        width: 120px;
    text-align: center;
        
        .spec-btn {
          color: var(--el-color-primary);
          margin-right: 10px;
          
          &:hover {
            color: var(--el-color-primary-light-3);
          }
        }
        
        .delete-btn {
          color: var(--el-text-color-secondary);
          
          &:hover {
            color: var(--el-color-danger);
          }
        }
      }
    }
  }
  
  .cart-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 15px;
    background-color: #f8f8f8;
    border-top: 1px solid var(--el-border-color-lighter);
    
    .footer-left {
      display: flex;
      align-items: center;
      
      .delete-selected-btn {
        margin-left: 15px;
      }
    }
    
    .footer-right {
      display: flex;
      align-items: center;
      
      .summary {
        margin-right: 20px;
        
        .selected-count {
          margin-right: 15px;
          
          em {
            font-style: normal;
            color: var(--el-color-danger);
            margin: 0 5px;
          }
        }
        
        .total-price {
          font-weight: 500;
          
          em {
            font-style: normal;
            font-size: 20px;
            color: var(--el-color-danger);
          }
        }
      }
      
      .checkout-btn {
        width: 120px;
      }
    }
  }
  
  .empty-cart {
    padding: 60px 20px;
    
    .el-empty {
      padding: 40px 0;
    }
  }
}

// 规格选择对话框样式
.spec-select-container {
  padding: 10px 0;
  
  .current-tea-info {
    display: flex;
    align-items: center;
    padding-bottom: 15px;
    border-bottom: 1px dashed var(--el-border-color);
    margin-bottom: 15px;
    
    .tea-image {
      width: 80px;
      height: 80px;
      border-radius: 4px;
      object-fit: cover;
      margin-right: 15px;
    }
    
    .tea-info {
      flex: 1;
      
      .tea-name {
        font-size: 16px;
        font-weight: 500;
        margin-bottom: 5px;
      }
      
      .tea-price {
        color: var(--el-color-danger);
      font-size: 18px;
        margin-bottom: 5px;
      }
      
      .selected-spec {
        color: var(--el-text-color-secondary);
        font-size: 14px;
      }
    }
  }
  
  .spec-options {
    .spec-title {
      font-weight: 500;
      margin-bottom: 10px;
    }
    
    .spec-list {
      .spec-radio {
        display: block;
        margin-bottom: 10px;
        
        .spec-item {
          display: flex;
          justify-content: space-between;
          width: 300px;
          
          .spec-name {
      color: var(--el-text-color-primary);
          }
          
          .spec-price {
            color: var(--el-color-danger);
          }
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .cart-page {
    .cart-header {
      .header-price {
        width: 80px;
      }
      
      .header-quantity {
        width: 120px;
      }
      
      .header-subtotal {
        width: 80px;
      }
      
      .header-action {
        width: 100px;
      }
    }
    
    .cart-list {
      .cart-item {
        .item-info {
          .item-image {
            width: 80px;
            height: 80px;
          }
        }
        
        .item-price {
          width: 80px;
        }
        
        .item-quantity {
          width: 120px;
        }
        
        .item-subtotal {
          width: 80px;
        }
        
        .item-action {
          width: 100px;
          
          .spec-btn, .delete-btn {
            padding: 4px;
            margin: 2px 0;
          }
        }
      }
    }
    
    .cart-footer {
      flex-direction: column;
      
      .footer-left {
        margin-bottom: 15px;
      }
      
      .footer-right {
        width: 100%;
        justify-content: space-between;
      }
  }
  }
}
</style> 