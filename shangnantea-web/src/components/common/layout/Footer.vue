<template>
  <footer class="footer">
    <div class="container">
      <div class="footer-content">
        <div class="footer-section about">
          <h3 class="footer-title">关于我们</h3>
          <p>商南茶文化推广与销售平台致力于传播商南茶叶文化，推广优质茶叶产品，促进茶商发展。</p>
          <div class="contact">
            <span><i class="el-icon-location"></i> 陕西省商洛市商南县</span>
            <span><i class="el-icon-phone"></i> 400-888-7777</span>
            <span><i class="el-icon-message"></i> contact@shangnantea.com</span>
          </div>
        </div>
        
        <div class="footer-section links">
          <h3 class="footer-title">快速链接</h3>
          <div class="footer-links-group">
            <h3>网站导航</h3>
            <ul>
              <li><router-link to="/tea-culture">首页</router-link></li>
              <li><router-link to="/tea/mall">茶叶商城</router-link></li>
              <li><router-link to="/forum/list">茶友论坛</router-link></li>
              <li><router-link to="/shop/list">茶商店铺</router-link></li>
            </ul>
          </div>
        </div>
        
        <div class="footer-section support">
          <h3 class="footer-title">帮助中心</h3>
          <ul>
            <li><router-link to="/faq">常见问题</router-link></li>
            <li><router-link to="/shipping">配送信息</router-link></li>
            <li><router-link to="/return">退换政策</router-link></li>
            <li><router-link to="/privacy">隐私政策</router-link></li>
            <li><router-link to="/terms">用户协议</router-link></li>
          </ul>
        </div>
        
        <div class="footer-section subscribe">
          <h3 class="footer-title">订阅资讯</h3>
          <p>订阅我们的电子报，获取最新茶叶资讯和优惠信息</p>
          <div class="subscribe-form">
            <el-input placeholder="请输入您的邮箱" v-model="email">
              <template #append>
                <el-button @click="subscribe">订阅</el-button>
              </template>
            </el-input>
          </div>
          <div class="social-media">
            <a href="#" title="微信"><i class="el-icon-chat-dot-round"></i></a>
            <a href="#" title="微博"><i class="el-icon-s-promotion"></i></a>
            <a href="#" title="抖音"><i class="el-icon-video-camera"></i></a>
            <a href="#" title="知乎"><i class="el-icon-s-help"></i></a>
          </div>
        </div>
      </div>
      
      <div class="footer-bottom">
        <p>© {{ currentYear }} 商南茶文化推广与销售平台 - 版权所有</p>
        <p>陕ICP备XXXXXXXX号</p>
      </div>
    </div>
  </footer>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { commonPromptMessages } from '@/utils/promptMessages'
import { useStorage } from '@/composables/useStorage'

export default {
  name: 'Footer',
  setup() {
    const email = ref('')
    const currentYear = computed(() => new Date().getFullYear())
    
    // 使用本地存储组合式函数来记住用户的邮箱
    const { value: subscribedEmails, setValue: setSubscribedEmails } = useStorage('subscribed_emails', [])
    
    // 邮箱验证
    const isValidEmail = email => {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      return emailRegex.test(email)
    }
    
    // 订阅
    const subscribe = () => {
      if (!email.value) {
        // 邮箱为空，提示用户
        commonPromptMessages.showEmailRequired()
        return
      }
      
      // 验证邮箱格式
      if (!isValidEmail(email.value)) {
        commonPromptMessages.showEmailFormatInvalid()
        return
      }
      
      // 检查邮箱是否已订阅
      if (subscribedEmails.value.includes(email.value)) {
        commonPromptMessages.showEmailAlreadySubscribed()
        return
      }
      
      // 添加到已订阅列表
      setSubscribedEmails([...subscribedEmails.value, email.value])
      
      // 模拟订阅成功（前端提示消息）
      commonPromptMessages.showSubscribeSuccess()
      email.value = ''
    }
    
    // 检查上次使用的邮箱
    onMounted(() => {
      const lastUsedEmail = localStorage.getItem('shangnantea_last_email')
      if (lastUsedEmail) {
        email.value = lastUsedEmail
      }
    })
    
    return {
      email,
      currentYear,
      subscribe
    }
  }
}
</script>

<style lang="scss" scoped>
.footer {
  background-color: #2c3e50;
  color: #ecf0f1;
  padding: 40px 0 20px;
  margin-top: 50px;
  
  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
  }
  
  .footer-content {
    display: flex;
    flex-wrap: wrap;
    justify-content: space-between;
    margin-bottom: 30px;
  }
  
  .footer-section {
    flex: 1;
    min-width: 200px;
    margin-right: 20px;
    margin-bottom: 20px;
    
    &:last-child {
      margin-right: 0;
    }
  }
  
  .footer-title {
    color: #3498db;
    margin-bottom: 20px;
    font-size: 1.2rem;
    position: relative;
    padding-bottom: 10px;
    
    &:after {
      content: '';
      position: absolute;
      left: 0;
      bottom: 0;
      width: 40px;
      height: 2px;
      background-color: #3498db;
    }
  }
  
  .contact {
    margin-top: 15px;
    
    span {
      display: block;
      margin-bottom: 8px;
      
      i {
        margin-right: 8px;
        color: #3498db;
      }
    }
  }
  
  ul {
    list-style: none;
    padding: 0;
    
    li {
      margin-bottom: 12px;
      
      a {
        color: #ecf0f1;
        text-decoration: none;
        transition: color 0.3s;
        
        &:hover {
          color: #3498db;
        }
      }
    }
  }
  
  .subscribe-form {
    margin-bottom: 15px;
  }
  
  .social-media {
    display: flex;
    margin-top: 15px;
    
    a {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 36px;
      height: 36px;
      border-radius: 50%;
      background-color: #34495e;
      color: #ecf0f1;
      margin-right: 10px;
      transition: all 0.3s;
      
      &:hover {
        background-color: #3498db;
        transform: translateY(-3px);
      }
      
      i {
        font-size: 18px;
      }
    }
  }
  
  .footer-bottom {
    text-align: center;
    padding-top: 20px;
    border-top: 1px solid #34495e;
    
    p {
      margin: 5px 0;
      color: #bdc3c7;
      font-size: 0.9rem;
    }
  }
}

// 响应式布局
@media (max-width: 768px) {
  .footer {
    .footer-content {
      flex-direction: column;
    }
    
    .footer-section {
      margin-right: 0;
      margin-bottom: 30px;
    }
  }
}
</style> 