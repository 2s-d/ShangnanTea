<template>
  <div class="error-container">
    <div class="error-content">
      <h1 class="error-code">403</h1>
      <div class="error-image">
        <SafeImage src="/images/errors/403.jpg" alt="无权限访问" type="banner" />
      </div>
      <h2 class="error-title">无权限访问</h2>
      <p class="error-desc">很抱歉，您没有权限访问此页面</p>
      <p class="error-tips">请联系管理员或返回首页</p>
      <div class="error-actions">
        <el-button type="primary" @click="goBack">返回上一页</el-button>
        <el-button @click="goHome">返回首页</el-button>
      </div>
    </div>
  </div>
</template>

<script>
import { useRouter } from 'vue-router'
import SafeImage from '@/components/common/form/SafeImage.vue'

export default {
  name: 'ForbiddenPage',
  components: {
    SafeImage
  },
  setup() {
    const router = useRouter()
    
    const goBack = () => {
      router.go(-1)
    }
    
    const goHome = () => {
      router.push('/')
    }
    
    return {
      goBack,
      goHome
    }
  }
}
</script>

<style lang="scss" scoped>
.error-container {
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 50px 20px;
  
  .error-content {
    max-width: 600px;
    text-align: center;
    
    .error-code {
      font-size: 8rem;
      color: #f56c6c;
      margin: 0;
    }
    
    .error-image {
      margin: 20px 0;
      
      img {
        max-width: 100%;
        height: auto;
      }
    }
    
    .error-title {
      font-size: 2rem;
      color: #303133;
      margin: 20px 0;
    }
    
    .error-desc {
      font-size: 1.2rem;
      color: #606266;
      margin: 10px 0;
    }
    
    .error-tips {
      font-size: 1rem;
      color: #909399;
      margin: 10px 0 30px;
    }
    
    .error-actions {
      display: flex;
      justify-content: center;
      gap: 20px;
    }
  }
}
</style> 