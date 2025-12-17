<template>
  <div class="error-container">
    <div class="error-content">
      <h1 class="error-code">404</h1>
      <div class="error-image">
        <SafeImage src="/images/errors/404.jpg" alt="页面不存在" type="banner" />
      </div>
      <h2 class="error-title">页面不存在</h2>
      <p class="error-desc">很抱歉，您访问的页面不存在或已被删除</p>
      <p class="error-tips">请检查网址是否正确，或返回首页浏览其他内容</p>
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
  name: 'NotFoundPage',
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
      color: #409EFF;
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