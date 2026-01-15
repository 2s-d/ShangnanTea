<template>
  <div class="order-review-page">
    <el-card class="review-card" shadow="hover" v-loading="loading">
      <template #header>
        <div class="card-header">
          <el-button icon="ArrowLeft" @click="goBack" text>返回订单详情</el-button>
          <span class="title">订单评价</span>
        </div>
      </template>

      <div v-if="orderDetail" class="review-content">
        <!-- 商品信息 -->
        <div class="product-section">
          <div class="section-title">评价商品</div>
          <div class="product-item">
            <div class="product-image">
              <SafeImage :src="orderDetail.tea_image" type="tea" :alt="orderDetail.tea_name" />
            </div>
            <div class="product-info">
              <div class="product-name">{{ orderDetail.tea_name }}</div>
              <div class="product-spec">规格：{{ orderDetail.spec_name }}</div>
              <div class="product-price">¥{{ orderDetail.price }} x {{ orderDetail.quantity }}</div>
            </div>
          </div>
        </div>

        <!-- 评价表单 -->
        <div class="review-form-section">
          <div class="section-title">填写评价</div>
          
          <!-- 评分 -->
          <div class="form-item">
            <div class="form-label">商品评分</div>
            <div class="rating-wrapper">
              <el-rate 
                v-model="reviewForm.rating" 
                :texts="ratingTexts"
                show-text
                :colors="['#F56C6C', '#E6A23C', '#67C23A']"
              />
            </div>
          </div>

          <!-- 评价内容 -->
          <div class="form-item">
            <div class="form-label">评价内容</div>
            <el-input
              v-model="reviewForm.content"
              type="textarea"
              :rows="5"
              placeholder="请分享您对这款茶叶的使用感受，如口感、香气、包装等..."
              maxlength="500"
              show-word-limit
            />
          </div>

          <!-- 上传图片 -->
          <div class="form-item">
            <div class="form-label">上传图片（可选，最多6张）</div>
            <el-upload
              v-model:file-list="imageList"
              action="#"
              list-type="picture-card"
              :auto-upload="false"
              :limit="6"
              accept="image/*"
              :on-exceed="handleExceed"
              :on-preview="handlePreview"
            >
              <el-icon><Plus /></el-icon>
            </el-upload>
          </div>

          <!-- 匿名评价 -->
          <div class="form-item">
            <el-checkbox v-model="reviewForm.isAnonymous">匿名评价</el-checkbox>
            <span class="anonymous-tip">匿名后，您的用户名将显示为"匿名用户"</span>
          </div>
        </div>

        <!-- 提交按钮 -->
        <div class="submit-section">
          <el-button @click="goBack">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="submitReview">
            提交评价
          </el-button>
        </div>
      </div>

      <div v-else-if="!loading" class="empty-content">
        <el-empty description="订单不存在或已被删除" />
        <el-button @click="goBack">返回</el-button>
      </div>
    </el-card>

    <!-- 图片预览 -->
    <el-dialog v-model="previewVisible" title="图片预览" width="600px">
      <img :src="previewUrl" style="width: 100%" alt="预览图片" />
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useStore } from 'vuex'
import { Plus } from '@element-plus/icons-vue'
import { showByCode, isSuccess } from '@/utils/apiMessages'
import { orderPromptMessages } from '@/utils/promptMessages'
import SafeImage from '@/components/common/form/SafeImage.vue'
import { uploadImage } from '@/api/upload'

export default {
  name: 'OrderReviewPage',
  components: {
    SafeImage,
    Plus
  },
  setup() {
    const router = useRouter()
    const route = useRoute()
    const store = useStore()
    
    const orderId = route.params.id
    const loading = computed(() => store.state.order.loading)
    const submitting = ref(false)
    
    const orderDetail = ref(null)
    const imageList = ref([])
    const previewVisible = ref(false)
    const previewUrl = ref('')
    
    const ratingTexts = ['很差', '较差', '一般', '满意', '非常满意']
    
    const reviewForm = reactive({
      rating: 5,
      content: '',
      isAnonymous: false
    })
    
    // 返回订单详情
    const goBack = () => {
      router.push(`/order/detail/${orderId}`)
    }
    
    // 加载订单详情
    const loadOrderDetail = async () => {
      try {
        const res = await store.dispatch('order/fetchOrderDetail', orderId)
        showByCode(res?.code)
        const data = res?.data || res
        orderDetail.value = data
        
        // 检查订单状态是否可以评价
        if (data && data.order_status !== 3) {
          orderPromptMessages.showReviewNotAllowed()
          goBack()
        }
        
        // 检查是否已评价
        if (data && data.buyer_rate === 1) {
          orderPromptMessages.showAlreadyReviewed()
          goBack()
        }
      } catch (error) {
        console.error('加载订单详情失败:', error)
        orderDetail.value = null
      }
    }
    
    // 图片超出限制
    const handleExceed = () => {
      orderPromptMessages.showImageLimitExceeded()
    }
    
    // 预览图片
    const handlePreview = file => {
      previewUrl.value = file.url
      previewVisible.value = true
    }
    
    // 上传图片到服务器
    const uploadImages = async () => {
      if (imageList.value.length === 0) return []
      
      const uploadedUrls = []
      for (const file of imageList.value) {
        if (file.raw) {
          try {
            const res = await uploadImage(file.raw, 'review')
            if (res && res.url) {
              uploadedUrls.push(res.url)
            }
          } catch (error) {
            console.error('图片上传失败:', error)
          }
        }
      }
      return uploadedUrls
    }
    
    // 提交评价
    const submitReview = async () => {
      // 验证
      if (reviewForm.rating === 0) {
        orderPromptMessages.showRatingRequired()
        return
      }
      
      if (!reviewForm.content || reviewForm.content.trim().length < 5) {
        orderPromptMessages.showReviewContentTooShort()
        return
      }
      
      submitting.value = true
      
      try {
        // 先上传图片
        const imageUrls = await uploadImages()
        
        // 提交评价
        const res = await store.dispatch('order/submitOrderReview', {
          orderId: orderId,
          teaId: orderDetail.value.tea_id,
          rating: reviewForm.rating,
          content: reviewForm.content.trim(),
          images: imageUrls,
          isAnonymous: reviewForm.isAnonymous ? 1 : 0
        })
        
        showByCode(res?.code)
        
        // 返回订单详情
        setTimeout(() => {
          router.push(`/order/detail/${orderId}`)
        }, 1500)
      } catch (error) {
        console.error('提交评价失败:', error)
      } finally {
        submitting.value = false
      }
    }
    
    onMounted(() => {
      if (!orderId) {
        orderPromptMessages.showOrderIdRequired()
        router.push('/order/list')
        return
      }
      loadOrderDetail()
    })
    
    return {
      loading,
      submitting,
      orderDetail,
      reviewForm,
      imageList,
      ratingTexts,
      previewVisible,
      previewUrl,
      goBack,
      handleExceed,
      handlePreview,
      submitReview
    }
  }
}
</script>

<style lang="scss" scoped>
.order-review-page {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.review-card {
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 15px;
  
  .title {
    font-size: 18px;
    font-weight: bold;
  }
}

.review-content {
  padding: 10px;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 15px;
  color: #303133;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.product-section {
  margin-bottom: 30px;
  
  .product-item {
    display: flex;
    align-items: center;
    padding: 15px;
    background-color: #f8f9fa;
    border-radius: 8px;
    
    .product-image {
      width: 80px;
      height: 80px;
      margin-right: 15px;
      
      :deep(img) {
        width: 100%;
        height: 100%;
        object-fit: cover;
        border-radius: 4px;
      }
    }
    
    .product-info {
      flex: 1;
      
      .product-name {
        font-weight: 500;
        color: #303133;
        margin-bottom: 5px;
        font-size: 15px;
      }
      
      .product-spec {
        color: #909399;
        font-size: 13px;
        margin-bottom: 5px;
      }
      
      .product-price {
        color: #f56c6c;
        font-size: 14px;
      }
    }
  }
}

.review-form-section {
  margin-bottom: 30px;
  
  .form-item {
    margin-bottom: 20px;
    
    .form-label {
      font-size: 14px;
      color: #606266;
      margin-bottom: 10px;
    }
    
    .rating-wrapper {
      :deep(.el-rate) {
        height: 32px;
        
        .el-rate__icon {
          font-size: 24px;
        }
        
        .el-rate__text {
          font-size: 14px;
          margin-left: 10px;
        }
      }
    }
    
    .anonymous-tip {
      color: #909399;
      font-size: 12px;
      margin-left: 10px;
    }
  }
}

.submit-section {
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
  
  .el-button {
    min-width: 120px;
    margin: 0 10px;
  }
}

.empty-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 0;
  
  .el-button {
    margin-top: 20px;
  }
}

@media (max-width: 768px) {
  .product-section {
    .product-item {
      flex-direction: column;
      text-align: center;
      
      .product-image {
        margin-right: 0;
        margin-bottom: 10px;
      }
    }
  }
  
  .submit-section {
    .el-button {
      display: block;
      width: 100%;
      margin: 10px 0;
    }
  }
}
</style>
