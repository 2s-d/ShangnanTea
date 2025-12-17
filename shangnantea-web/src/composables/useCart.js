/**
 * 购物车相关操作的组合式函数
 */
import { computed } from 'vue'
import { useStore } from 'vuex'
import { ElMessageBox } from 'element-plus'
import orderMessages from '@/utils/orderMessages'

/**
 * 使用购物车功能
 * @returns {Object} 购物车相关方法和状态
 */
export function useCart() {
  const store = useStore()
  
  // 计算属性
  const cartItems = computed(() => store.state.cart.cartItems)
  const cartLoading = computed(() => store.state.cart.loading)
  const cartItemCount = computed(() => store.getters['cart/cartItemCount'])
  const cartTotalPrice = computed(() => store.getters['cart/cartTotalPrice'])
  const hasSelectedItems = computed(() => store.getters['cart/hasSelectedItems'])
  
  /**
   * 添加商品到购物车
   * @param {Object} product 商品信息 {teaId, quantity, name}
   * @param {boolean} showMessage 是否显示成功消息
   * @returns {Promise} 添加结果
   */
  const addToCart = async (product, showMessage = true) => {
    try {
      await store.dispatch('cart/addToCart', product)
      
      if (showMessage) {
        orderMessages.business.showAddToCartSuccess(product.name, product.quantity)
      }
      
      return true
    } catch (error) {
      console.error('添加到购物车失败:', error)
      orderMessages.business.showAddToCartFailure()
      return false
    }
  }
  
  /**
   * 更新购物车商品数量
   * @param {Object} item 购物车商品 {id, quantity, name}
   * @param {boolean} showMessage 是否显示成功消息
   * @returns {Promise} 更新结果
   */
  const updateCartItem = async (item, showMessage = false) => {
    try {
      await store.dispatch('cart/updateCartItem', item)
      
      if (showMessage) {
        orderMessages.business.showQuantityUpdated(item.name, item.quantity)
      }
      
      return true
    } catch (error) {
      console.error('更新购物车失败:', error)
      orderMessages.business.showAddToCartFailure('更新购物车失败')
      return false
    }
  }
  
  /**
   * 从购物车中移除商品
   * @param {Object} item 要移除的商品 {id, name}
   * @param {boolean} showConfirm 是否显示确认对话框
   * @returns {Promise} 移除结果
   */
  const removeFromCart = async (item, showConfirm = true) => {
    const doRemove = async () => {
      try {
        await store.dispatch('cart/removeFromCart', item.id)
        orderMessages.business.showItemRemoved(item.name)
        return true
      } catch (error) {
        console.error('删除商品失败:', error)
        orderMessages.business.showAddToCartFailure('删除商品失败')
        return false
      }
    }
    
    if (showConfirm) {
      try {
        await ElMessageBox.confirm(
          orderMessages.ORDER_MESSAGES.UI.REMOVE_CONFIRM, 
          '提示', 
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        return doRemove()
      } catch {
        return false // 用户取消操作
      }
    } else {
      return doRemove()
    }
  }
  
  /**
   * 清空购物车
   * @param {boolean} showConfirm 是否显示确认对话框
   * @returns {Promise} 清空结果
   */
  const clearCart = async (showConfirm = true) => {
    const doClear = async () => {
      try {
        await store.dispatch('cart/clearCart')
        orderMessages.business.showCartCleared()
        return true
      } catch (error) {
        console.error('清空购物车失败:', error)
        orderMessages.business.showAddToCartFailure('清空购物车失败')
        return false
      }
    }
    
    if (showConfirm) {
      try {
        await ElMessageBox.confirm(
          orderMessages.ORDER_MESSAGES.UI.CLEAR_CONFIRM, 
          '提示', 
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        return doClear()
      } catch {
        return false // 用户取消操作
      }
    } else {
      return doClear()
    }
  }
  
  /**
   * 加载购物车数据
   * @param {boolean} showError 是否显示错误消息
   * @returns {Promise} 加载结果
   */
  const loadCartItems = async (showError = true) => {
    try {
      await store.dispatch('cart/fetchCartItems')
      return true
    } catch (error) {
      console.error('加载购物车失败:', error)
      if (showError) {
        orderMessages.api.showCartLoadFailed(error)
      }
      return false
    }
  }
  
  /**
   * 立即购买
   * @param {Object} product 商品信息 {teaId, quantity, name}
   * @param {Object} router vue-router实例
   * @returns {Promise} 购买结果
   */
  const buyNow = async (product, router) => {
    const result = await addToCart(product, false)
    if (result && router) {
      router.push('/cart')
    }
    return result
  }
  
  /**
   * 检查商品数量有效性
   * @param {number} quantity 商品数量
   * @param {number} maxLimit 最大限制数量
   * @returns {boolean} 是否有效
   */
  const validateQuantity = (quantity, maxLimit = 99) => {
    if (!quantity || quantity < 1 || quantity > maxLimit) {
      orderMessages.ui.showQuantityInvalid()
      return false
    }
    return true
  }
  
  return {
    // 状态
    cartItems,
    cartLoading,
    cartItemCount,
    cartTotalPrice,
    hasSelectedItems,
    
    // 方法
    addToCart,
    updateCartItem,
    removeFromCart,
    clearCart,
    loadCartItems,
    buyNow,
    validateQuantity
  }
} 