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
                <el-radio :label="address.id" class="address-radio">
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
              <el-radio label="alipay">
                <SafeImage src="/images/payments/alipay.jpg" type="banner" alt="支付宝" class="payment-logo" style="width:24px;height:24px;object-fit:contain;" />
                支付宝
              </el-radio>
              <el-radio label="wechat">
                <SafeImage src="/images/payments/wechat.jpg" type="banner" alt="微信支付" class="payment-logo" style="width:24px;height:24px;object-fit:contain;" />
                微信支付
              </el-radio>
              <el-radio label="unionpay">
                <SafeImage src="/images/payments/unionpay.jpg" type="banner" alt="银联支付" class="payment-logo" style="width:24px;height:24px;object-fit:contain;" />
                银联支付
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
/* UI-DEV-START */
import { ref, computed, onMounted, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Plus } from '@element-plus/icons-vue'
import SafeImage from '@/components/common/form/SafeImage.vue'
/* UI-DEV-END */

/*
// 真实代码（开发UI时注释）
import { ref, computed, onMounted, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Plus } from '@element-plus/icons-vue'
*/

export default {
  name: 'CheckoutPage',
  components: {
    /* UI-DEV-START */
    ArrowLeft,
    Plus,
    SafeImage
    /* UI-DEV-END */
    
    /*
    // 真实代码（开发UI时注释）
    ArrowLeft,
    Plus
    */
  },
  setup() {
    const router = useRouter()
    const route = useRoute()
    
    /* UI-DEV-START */
    // 纯UI开发时的模拟数据和状态
    const loading = ref(false)
    const submitting = ref(false)
    
    // 收货地址相关
    const addresses = ref([
      {
        id: 1,
        name: '张三',
        phone: '13800138000',
        province: '陕西省',
        city: '商洛市',
        district: '商南县',
        detail: '城关镇商城路123号',
        isDefault: true,
        formattedAddress: '陕西省 商洛市 商南县 城关镇商城路123号'
      },
      {
        id: 2,
        name: '李四',
        phone: '13900139000',
        province: '陕西省',
        city: '西安市',
        district: '雁塔区',
        detail: '高新路88号科技大厦B座701',
        isDefault: false,
        formattedAddress: '陕西省 西安市 雁塔区 高新路88号科技大厦B座701'
      }
    ])
    const selectedAddressId = ref(null)
    
    // 订单商品相关
    const orderItems = ref([
      {
        id: 'cart1',
        teaId: 1,
        teaName: '商南毛尖',
        image: '/images/tea1.jpg',
        price: 128.00,
        quantity: 2,
        specName: '特级（100g）',
        shopId: 'PLATFORM',
        shopType: 'platform',
        shopName: '商南茶文化平台',
        selected: true,
        remark: ''
      },
      {
        id: 'cart2',
        teaId: 3,
        teaName: '丹江红茶',
        image: '/images/tea3.jpg',
        price: 98.00,
        quantity: 1,
        specName: '特级（50g）',
        shopId: 'shop1',
        shopType: 'shop',
        shopName: '丹江茶叶专营店',
        selected: true,
        remark: ''
      }
    ])
    
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
    
    // 模拟省市区数据
    const regionOptions = ref([
      {
        value: '610000',
        label: '陕西省',
        children: [
          {
            value: '610300',
            label: '宝鸡市',
            children: [
              { value: '610324', label: '扶风县' },
              { value: '610326', label: '眉县' }
            ]
          },
          {
            value: '611000',
            label: '商洛市',
            children: [
              { value: '611026', label: '柞水县' },
              { value: '611021', label: '洛南县' },
              { value: '611022', label: '丹凤县' },
              { value: '611023', label: '商南县' },
              { value: '611024', label: '山阳县' },
              { value: '611025', label: '镇安县' },
              { value: '611002', label: '商州区' }
            ]
          },
          {
            value: '610100',
            label: '西安市',
            children: [
              { value: '610113', label: '雁塔区' },
              { value: '610103', label: '碑林区' },
              { value: '610104', label: '莲湖区' },
              { value: '610111', label: '灞桥区' },
              { value: '610112', label: '未央区' }
            ]
          }
        ]
      }
    ])
    
    // 添加默认图片常量
    const defaultTeaImage = '/mock-images/tea-default.jpg'
    const defaultPaymentImage = '/mock-images/payment-default.jpg'
    /* UI-DEV-END */
    
    /*
    // 真实代码（开发UI时注释）
    const store = useStore()
    const loading = ref(false)
    const submitting = ref(false)
    
    // 收货地址相关
    const addresses = ref([])
    const selectedAddressId = ref(null)
    
    // 订单商品相关
    const orderItems = ref([])
    
    // 支付方式相关
    const paymentMethod = ref('alipay')
    
    // 加载地址和订单数据
    const loadAddresses = async () => {
      try {
        loading.value = true
        await store.dispatch('user/fetchAddresses')
        addresses.value = store.state.user.addresses || []
        
        // 默认选中默认地址
        const defaultAddress = addresses.value.find(addr => addr.isDefault)
        if (defaultAddress) {
          selectedAddressId.value = defaultAddress.id
        } else if (addresses.value.length > 0) {
          selectedAddressId.value = addresses.value[0].id
        }
      } catch (error) {
        ElMessage.error('获取地址失败')
      } finally {
        loading.value = false
      }
    }
    
    const loadOrderItems = async () => {
      try {
        loading.value = true
        
        // 从路由参数判断是否是直接购买
        const isDirect = route.query.direct === '1'
        
        if (isDirect) {
          // 直接购买的情况，从缓存获取直接购买的商品
          const directBuyItem = store.state.order.directBuyItem
          if (!directBuyItem) {
            ElMessage.error('商品信息已失效，请重新选择')
            router.push('/tea/mall')
            return
          }
          orderItems.value = [directBuyItem]
        } else {
          // 购物车结算的情况
          const items = await store.dispatch('order/getSelectedCartItems')
          if (!items || items.length === 0) {
            ElMessage.error('请先选择结算的商品')
            router.push('/order/cart')
            return
          }
          orderItems.value = items
        }
      } catch (error) {
        ElMessage.error('获取商品信息失败')
        router.push('/order/cart')
      } finally {
        loading.value = false
      }
    }
    
    // 金额计算
    const goodsAmount = computed(() => {
      return orderItems.value.reduce((sum, item) => {
        return sum + item.price * item.quantity
      }, 0)
    })
    
    const shippingFee = computed(() => {
      // 计算运费逻辑
      return 0
    })
    
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
    
    // 加载地区数据
    const regionOptions = ref([])
    const loadRegionData = async () => {
      try {
        const data = await store.dispatch('common/getRegionData')
        regionOptions.value = data
      } catch (error) {
        console.error('加载地区数据失败', error)
      }
    }
    */
    
    // 初始化
    onMounted(() => {
      /* UI-DEV-START */
      // 模拟加载数据
      loading.value = true
      setTimeout(() => {
        // 默认选中默认地址
        const defaultAddress = addresses.value.find(addr => addr.isDefault)
        if (defaultAddress) {
          selectedAddressId.value = defaultAddress.id
        } else if (addresses.value.length > 0) {
          selectedAddressId.value = addresses.value[0].id
        }
        
        loading.value = false
      }, 800)
      /* UI-DEV-END */
      
      /*
      // 真实代码（开发UI时注释）
      loadAddresses()
      loadOrderItems()
      loadRegionData()
      */
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
      
      await addressFormRef.value.validate(async (valid) => {
        if (!valid) return
        
        /* UI-DEV-START */
        // 模拟保存地址
        addressSubmitting.value = true
        
        setTimeout(() => {
          // 从表单区域值中提取省市区
          const [province, city, district] = addressForm.region
          
          // 构建新地址对象
          const newAddress = {
            id: addresses.value.length + 1,
            name: addressForm.name,
            phone: addressForm.phone,
            province: '陕西省',
            city: '商洛市',
            district: '商南县',
            detail: addressForm.detail,
            isDefault: addressForm.isDefault,
            formattedAddress: `陕西省 商洛市 商南县 ${addressForm.detail}`
          }
          
          // 如果设为默认地址，清除其他默认标记
          if (newAddress.isDefault) {
            addresses.value.forEach(addr => {
              addr.isDefault = false
            })
          }
          
          // 添加到地址列表
          addresses.value.push(newAddress)
          
          // 选中新地址
          selectedAddressId.value = newAddress.id
          
          // 关闭对话框
          addressDialogVisible.value = false
          
          // 提示成功
          ElMessage.success('地址添加成功')
          
          addressSubmitting.value = false
        }, 800)
        /* UI-DEV-END */
        
        /*
        // 真实代码（开发UI时注释）
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
          await store.dispatch('user/addAddress', addressData)
          
          // 刷新地址列表
          await loadAddresses()
          
          // 关闭对话框
          addressDialogVisible.value = false
          
          // 提示成功
          ElMessage.success('地址添加成功')
        } catch (error) {
          ElMessage.error('保存地址失败')
        } finally {
          addressSubmitting.value = false
        }
        */
      })
    }
    
    // 提交订单
    const submitOrder = async () => {
      if (!canSubmitOrder.value) {
        ElMessage.warning('请完善订单信息')
        return
      }
      
      if (!selectedAddressId.value) {
        ElMessage.warning('请选择收货地址')
        return
      }
      
      /* UI-DEV-START */
      // 模拟提交订单
      submitting.value = true
      
      setTimeout(() => {
        submitting.value = false
        
        // 模拟生成订单ID
        const orderId = 'ORD' + Date.now()
        
        // 跳转到支付页面，传递订单ID
        router.push(`/order/payment?orderId=${orderId}`)
        
        ElMessage.success('订单提交成功，请完成支付')
      }, 1000)
      /* UI-DEV-END */
      
      /*
      // 真实代码（开发UI时注释）
      try {
        submitting.value = true
        
        // 获取选中的地址
        const address = addresses.value.find(addr => addr.id === selectedAddressId.value)
        if (!address) {
          ElMessage.warning('请选择有效的收货地址')
          return
        }
        
        // 构建订单数据，参考 orders 表的字段
        const orderData = {
          // 必要字段
          user_id: store.state.user.userInfo.id, // 由后端从token自动获取
          address_id: selectedAddressId.value,
          payment_method: paymentMethod.value,
          order_status: 1, // 1-待支付
          order_amount: totalAmount.value,
          shipping_fee: shippingFee.value,
          // 商品项，与 order_items 表对应
          items: orderItems.value.map(item => ({
            tea_id: item.tea_id,
            spec_id: item.spec_id,
            quantity: item.quantity,
            price: item.price,
            shop_id: item.shop_id,
            remark: item.remark || ''
          }))
        }
        
        // 创建订单
        const result = await store.dispatch('order/createOrder', orderData)
        
        // 如果是从购物车结算，清空已购买的购物车商品
        if (!route.query.direct) {
          await store.dispatch('cart/removeCartItems', orderItems.value.map(item => item.id))
        }
        
        // 跳转到支付页面，传递订单ID
        router.push(`/order/payment?orderId=${result.order_id}`)
        
        ElMessage.success('订单提交成功，请完成支付')
      } catch (error) {
        ElMessage.error(error.message || '提交订单失败')
      } finally {
        submitting.value = false
      }
      */
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