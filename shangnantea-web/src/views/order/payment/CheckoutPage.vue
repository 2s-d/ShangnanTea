<template>
  <div class="checkout-page">
    <el-card class="box-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span class="title">订单结算</span>
          <el-button @click="goBack" class="back-button">
            <el-icon><ArrowLeft /></el-icon> 返回购物车
          </el-button>
        </div>
      </template>

      <div v-loading="loading" class="checkout-content">
        <!-- 收货地址部分 -->
        <div class="checkout-section address-section">
          <div class="section-title">
            <span>收货地址</span>
            <el-button type="primary" link @click="openAddressDialog">
              <el-icon><Plus /></el-icon> 新增地址
            </el-button>
          </div>
          
          <div class="address-list" v-if="addresses.length > 0">
            <el-radio-group v-model="selectedAddressId" class="address-radio-group">
              <div v-for="address in addresses" :key="address.id" class="address-item">
                <el-radio :value="address.id" class="address-radio">
                  <div class="address-content">
                    <div class="address-header">
                      <span class="name">{{ address.name }}</span>
                      <span class="phone">{{ address.phone }}</span>
                      <el-tag v-if="address.isDefault" size="small" type="success">默认地址</el-tag>
                    </div>
                    <div class="address-detail">{{ address.formattedAddress }}</div>
                  </div>
                </el-radio>
              </div>
            </el-radio-group>
          </div>
          
          <el-empty v-else description="您还没有收货地址，请添加" :image-size="100">
            <el-button type="primary" @click="openAddressDialog">添加收货地址</el-button>
          </el-empty>
        </div>

        <!-- 订单商品部分 -->
        <div class="checkout-section order-items-section">
          <div class="section-title">
            <span>订单商品</span>
            <span class="item-count">共{{ orderItems.length }}件</span>
          </div>
          
          <div class="order-items">
            <div v-for="item in orderItems" :key="item.id" class="order-item">
              <div class="item-image">
                <SafeImage :src="item.tea_image" type="tea" :alt="item.teaName" style="width:80px;height:80px;object-fit:cover;" />
              </div>
              <div class="item-info">
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
                <div class="item-shop">{{ item.shopType === 'platform' ? '平台自营' : item.shopName }}</div>
                <div class="item-remark">
                  <el-input
                    v-model="item.remark"
                    placeholder="订单备注（选填）"
                    :maxlength="50"
                    show-word-limit
                    size="small"
                  />
                </div>
              </div>
              <div class="item-price">¥{{ item.price }}</div>
              <div class="item-quantity">x{{ item.quantity }}</div>
              <div class="item-subtotal">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
            </div>
          </div>
        </div>

        <!-- 支付方式部分 -->
        <div class="checkout-section payment-section">
          <div class="section-title">
            <span>支付方式</span>
          </div>
          
          <div class="payment-methods">
            <el-radio-group v-model="paymentMethod">
              <el-radio value="alipay">
                <SafeImage src="/images/payments/alipay.jpg" type="banner" alt="支付宝" class="payment-logo" style="width:24px;height:24px;object-fit:contain;" />
                支付宝
              </el-radio>
              <el-radio value="wechat">
                <SafeImage src="/images/payments/wechat.jpg" type="banner" alt="微信支付" class="payment-logo" style="width:24px;height:24px;object-fit:contain;" />
                微信支付
              </el-radio>
            </el-radio-group>
          </div>
        </div>

        <!-- 金额信息 -->
        <div class="checkout-section amount-section">
          <div class="amount-item">
            <span>商品金额：</span>
            <span>¥{{ goodsAmount.toFixed(2) }}</span>
          </div>
          <div class="amount-item">
            <span>运费：</span>
            <span>¥{{ shippingFee.toFixed(2) }}</span>
          </div>
          <div class="amount-item total-amount">
            <span>应付金额：</span>
            <span class="price">¥{{ totalAmount.toFixed(2) }}</span>
          </div>
        </div>
      </div>

      <!-- 底部结算栏 -->
      <div class="checkout-footer">
        <div class="footer-content">
          <div class="price-summary">
            <div class="total-price">
              <span class="label">应付金额：</span>
              <span class="price">¥{{ totalAmount.toFixed(2) }}</span>
            </div>
          </div>
          <el-button 
            type="primary" 
            size="large" 
            :disabled="!canSubmitOrder" 
            :loading="submitting" 
            @click="submitOrder"
            class="submit-button"
          >
            确认支付
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 新增地址对话框 -->
    <el-dialog
      v-model="addressDialogVisible"
      title="新增收货地址"
      width="500px"
      destroy-on-close
    >
      <el-form
        :model="addressForm"
        :rules="addressRules"
        ref="addressFormRef"
        label-width="80px"
      >
        <el-form-item label="收货人" prop="name">
          <el-input v-model="addressForm.name" placeholder="请输入收货人姓名" />
        </el-form-item>
        
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="addressForm.phone" placeholder="请输入手机号码" />
        </el-form-item>
        
        <el-form-item label="所在地区" prop="region">
          <el-cascader
            v-model="addressForm.region"
            :options="regionOptions"
            placeholder="请选择省/市/区"
            :props="{
              expandTrigger: 'hover',
              checkStrictly: false
            }"
          />
        </el-form-item>
        
        <el-form-item label="详细地址" prop="detail">
          <el-input
            v-model="addressForm.detail"
            type="textarea"
            placeholder="街道、门牌号等"
            :rows="3"
          />
        </el-form-item>
        
        <el-form-item label="设为默认" prop="isDefault">
          <el-switch v-model="addressForm.isDefault" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addressDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveAddress" :loading="addressSubmitting">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, computed, onMounted, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useOrderStore } from '@/stores/order'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

import { ArrowLeft, Plus } from '@element-plus/icons-vue'
import SafeImage from '@/components/common/form/SafeImage.vue'
import { showByCode, isSuccess } from '@/utils/apiMessages'
import { orderPromptMessages } from '@/utils/promptMessages'

export default {
  name: 'CheckoutPage',
  components: {
    ArrowLeft,
    Plus,
    SafeImage
  },
  setup() {
    const router = useRouter()
    const route = useRoute()
    const orderStore = useOrderStore()
    const userStore = useUserStore()
    
    /**
     * 纯 UI 占位数据（生产形态：不在 UI 层造数据）
     * TODO-SCRIPT: 结算页需要接入 Vuex（user 地址、order 购物车/下单、common 地区数据）
     */
    const loading = ref(false)
    const submitting = ref(false)
    
    // 收货地址相关
    const addresses = ref([])
    const selectedAddressId = ref(null)
    
    // 订单商品相关
    const orderItems = ref([])
    
    // 支付方式相关
    const paymentMethod = ref('alipay')
    
    // 金额计算
    const goodsAmount = computed(() => {
      return orderItems.value.reduce((sum, item) => {
        return sum + item.price * item.quantity
      }, 0)
    })
    
    const shippingFee = ref(0) // 运费，可以根据地址和商品设置不同的运费
    
    const totalAmount = computed(() => {
      return goodsAmount.value + shippingFee.value
    })
    
    // 地址对话框相关
    const addressDialogVisible = ref(false)
    const addressSubmitting = ref(false)
    const addressFormRef = ref(null)
    
    const addressForm = reactive({
      name: '',
      phone: '',
      region: [],
      detail: '',
      isDefault: false
    })
    
    const addressRules = {
      name: [
        { required: true, message: '请输入收货人姓名', trigger: 'blur' },
        { min: 2, max: 20, message: '收货人姓名长度为2-20个字符', trigger: 'blur' }
      ],
      phone: [
        { required: true, message: '请输入手机号码', trigger: 'blur' },
        { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
      ],
      region: [
        { required: true, type: 'array', message: '请选择所在地区', trigger: 'change' }
      ],
      detail: [
        { required: true, message: '请输入详细地址', trigger: 'blur' },
        { min: 5, max: 100, message: '详细地址长度为5-100个字符', trigger: 'blur' }
      ]
    }
    
    const regionOptions = ref([])
    
    // 添加默认图片常量（生产形态：不使用 mock-images）
    const defaultTeaImage = ''
    const defaultPaymentImage = ''
    
    // 加载地址和订单数据
    const loadAddresses = async () => {
      try {
        loading.value = true
        await userStore.fetchAddresses()
        addresses.value = userStore.addresses || []
        
        // 默认选中默认地址
        const defaultAddress = addresses.value.find(addr => addr.isDefault)
        if (defaultAddress) {
          selectedAddressId.value = defaultAddress.id
        } else if (addresses.value.length > 0) {
          selectedAddressId.value = addresses.value[0].id
        }
      } catch (error) {
        console.error('加载地址失败:', error)
      } finally {
        loading.value = false
      }
    }

    // 加载订单商品：从购物车选择的商品或直接购买
    const loadOrderItems = async () => {
      try {
        loading.value = true
        
        // 从路由参数判断是否是直接购买
        const isDirect = route.query.direct === '1'
        
        if (isDirect) {
          // 直接购买的情况，从缓存获取直接购买的商品
          const directBuyItem = orderStore.directBuyItem
          if (!directBuyItem) {
            orderPromptMessages.showOrderInfoInvalid()
            router.push('/tea/mall')
            return
          }
          orderItems.value = [directBuyItem]
        } else {
          // 购物车结算的情况
          await orderStore.fetchCartItems()
          const selectedIdsStr = route.query.selectedIds || ''
          const selectedIds = selectedIdsStr
            ? selectedIdsStr.split(',').filter(Boolean)
            : []
          const items = await orderStore.getSelectedCartItems(selectedIds)
          if (!items || items.length === 0) {
            orderPromptMessages.showSelectionRequired()
            router.push('/order/cart')
            return
          }
          orderItems.value = items
        }
      } catch (error) {
        console.error('加载商品信息失败:', error)
        router.push('/order/cart')
      } finally {
        loading.value = false
      }
    }
    
    // 初始化
    onMounted(() => {
      loadAddresses()
      loadOrderItems()
    })
    
    // 计算属性：是否可以提交订单
    const canSubmitOrder = computed(() => {
      return selectedAddressId.value && orderItems.value.length > 0 && paymentMethod.value
    })
    
    // 返回按钮
    const goBack = () => {
      router.back()
    }
    
    // 打开新增地址对话框
    const openAddressDialog = () => {
      addressDialogVisible.value = true
    }
    
    // 保存地址
    const saveAddress = async () => {
      if (!addressFormRef.value) return
      
      await addressFormRef.value.validate(async valid => {
        if (!valid) return
        
        try {
          addressSubmitting.value = true
          
          // 从表单区域值中提取省市区
          const [province, city, district] = addressForm.region
          
          const addressData = {
            name: addressForm.name,
            phone: addressForm.phone,
            province,
            city,
            district,
            detail: addressForm.detail,
            isDefault: addressForm.isDefault
          }
          
          // 添加地址
          await userStore.addAddress(addressData)
          
          // 刷新地址列表
          await loadAddresses()
          
          // 关闭对话框
          addressDialogVisible.value = false
          
          // 静默成功，不显示消息
        } catch (error) {
          // 网络错误等已由响应拦截器处理，这里只记录日志
          if (process.env.NODE_ENV === 'development') {
            console.error('保存地址失败:', error)
          }
        } finally {
          addressSubmitting.value = false
        }
      })
    }
    
    // 提交支付表单（自动跳转到支付宝）
    const submitAlipayForm = formHtml => {
      // 创建一个临时div来承载表单HTML
      const div = document.createElement('div')
      div.innerHTML = formHtml
      document.body.appendChild(div)
      
      // 查找表单并自动提交
      const form = div.querySelector('form')
      if (form) {
        form.submit()
      } else {
        console.error('支付表单格式错误')
        orderPromptMessages.showPaymentFailed()
      }
    }
    
    // 提交订单并发起支付
    const submitOrder = async () => {
      if (!canSubmitOrder.value) {
        orderPromptMessages.showOrderInfoIncomplete()
        return
      }
      
      if (!selectedAddressId.value) {
        orderPromptMessages.showAddressRequired()
        return
      }
      
      // 检查是否选择了微信支付
      if (paymentMethod.value === 'wechat') {
        ElMessage.warning('微信支付功能正在开发中，敬请期待！')
        return
      }
      
      submitting.value = true
      try {
        // 第一步：创建订单
        const orderData = {
          address_id: selectedAddressId.value,
          payment_method: paymentMethod.value,
          order_amount: totalAmount.value,
          shipping_fee: shippingFee.value,
          fromCart: true,
          items: orderItems.value.map(item => ({
            tea_id: item.tea_id,
            spec_id: item.spec_id,
            quantity: item.quantity,
            price: item.price,
            shop_id: item.shop_id,
            remark: item.remark || ''
          }))
        }
        const createRes = await orderStore.createOrder(orderData)
        
        // 显示创建订单的消息
        if (createRes?.code) showByCode(createRes.code)
        
        const orderId = createRes?.data?.order_id || createRes?.data?.id
        if (!orderId) {
          console.error('订单创建失败，未返回订单ID')
          return
        }
        
        // 第二步：发起支付
        const payRes = await orderStore.payOrder({
          orderId,
          paymentMethod: paymentMethod.value
        })
        
        // 检查支付接口返回的状态码
        if (payRes?.code === 5007) {
          // 5007 - 支付表单生成成功，正在跳转...
          showByCode(payRes.code)
          
          // 获取支付表单HTML并自动提交
          const formHtml = payRes?.data?.formHtml || payRes?.data
          if (formHtml) {
            submitAlipayForm(formHtml)
          } else {
            console.error('未返回支付表单')
            orderPromptMessages.showPaymentFailed()
          }
        } else if (payRes?.code === 5006) {
          // 5006 - 订单已支付
          showByCode(payRes.code)
          router.push(`/order/detail/${orderId}`)
        } else {
          // 其他状态码（失败）
          if (payRes?.code) showByCode(payRes.code)
        }
      } catch (error) {
        // 网络错误等已由响应拦截器处理，这里只记录日志
        if (process.env.NODE_ENV === 'development') {
          console.error('提交订单或支付失败:', error)
        }
      } finally {
        submitting.value = false
      }
    }
    
    return {
      loading,
      submitting,
      addresses,
      selectedAddressId,
      orderItems,
      paymentMethod,
      goodsAmount,
      shippingFee,
      totalAmount,
      canSubmitOrder,
      goBack,
      submitOrder,
      addressDialogVisible,
      addressSubmitting,
      addressForm,
      addressFormRef,
      addressRules,
      regionOptions,
      openAddressDialog,
      saveAddress,
      defaultTeaImage,
      defaultPaymentImage
    }
  }
}
</script>

<style lang="scss" scoped>
.checkout-page {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
  position: relative;
  padding-bottom: 80px;
}

.box-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .title {
    font-size: 18px;
    font-weight: bold;
  }
}

.checkout-content {
  min-height: 400px;
}

.checkout-section {
  margin-bottom: 30px;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 20px;
  
  &:last-child {
    border-bottom: none;
  }
}

.section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  font-size: 16px;
  font-weight: bold;
  
  .item-count {
    font-size: 14px;
    color: #909399;
    font-weight: normal;
  }
}

// 地址部分样式
.address-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.address-radio-group {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.address-item {
  width: 100%;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  transition: all 0.3s;
  
  &:hover {
    border-color: #409eff;
  }
}

.address-radio {
  width: 100%;
  padding: 15px;
  height: auto;
  
  :deep(.el-radio__label) {
    padding-right: 15px;
    white-space: normal;
    width: 100%;
  }
}

.address-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.address-header {
  display: flex;
  align-items: center;
  gap: 10px;
  
  .name {
    font-weight: bold;
    font-size: 15px;
  }
  
  .phone {
    color: #606266;
  }
}

.address-detail {
  color: #606266;
  line-height: 1.5;
}

// 订单商品部分
.order-items {
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.order-item {
  display: flex;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #ebeef5;
  
  &:last-child {
    border-bottom: none;
  }
}

.item-image {
  width: 80px;
  height: 80px;
  overflow: hidden;
  border-radius: 4px;
  margin-right: 15px;
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.item-name {
  font-weight: bold;
  font-size: 15px;
}

.item-spec {
  color: #909399;
  font-size: 13px;
}

.item-shop {
  color: #606266;
  font-size: 13px;
}

.item-remark {
  margin-top: 8px;
  width: 100%;
}

.item-price, .item-quantity, .item-subtotal {
  width: 80px;
  text-align: center;
  color: #606266;
}

.item-subtotal {
  font-weight: bold;
  color: #f56c6c;
}

// 支付方式部分
.payment-methods {
  display: flex;
  gap: 20px;
}

.payment-method-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 5px 0;
}

.payment-logo {
  width: 28px;
  height: 28px;
  object-fit: contain;
}

// 金额信息
.amount-section {
  border-top: 1px dashed #ebeef5;
  padding-top: 20px;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10px;
}

.amount-item {
  display: flex;
  align-items: center;
  gap: 10px;
  
  &.total-amount {
    font-size: 16px;
    font-weight: bold;
    
    .price {
      color: #f56c6c;
      font-size: 20px;
    }
  }
}

// 底部结算栏
.checkout-footer {
  position: sticky;
  bottom: 0;
  left: 0;
  right: 0;
  margin-top: 20px;
  padding: 15px 20px;
  background-color: #fff;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
  border-top: 1px solid #ebeef5;
  z-index: 10;
  
  .footer-content {
    display: flex;
    justify-content: flex-end;
    align-items: center;
    max-width: 1000px;
    margin: 0 auto;
  }
  
  .price-summary {
    margin-right: 20px;
    
    .total-price {
      display: flex;
      align-items: center;
      
      .label {
        font-size: 14px;
        color: #606266;
      }
      
      .price {
        font-size: 20px;
        font-weight: bold;
        color: #f56c6c;
        margin-left: 8px;
      }
    }
  }
  
  .submit-button {
    min-width: 120px;
  }
}

// 响应式适配
@media (max-width: 768px) {
  .checkout-footer {
    .footer-content {
      flex-direction: column;
      align-items: flex-end;
    }
    
    .price-summary {
      margin-right: 0;
      margin-bottom: 10px;
      width: 100%;
      
      .total-price {
        justify-content: flex-end;
      }
    }
  }
}
</style> 