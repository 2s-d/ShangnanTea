<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-box">
        <div class="login-header">
          <h2>用户登录</h2>
          <p>欢迎回到商南茶文化平台</p>
        </div>
        
        <el-form 
          ref="loginFormRef" 
          :model="loginForm" 
          :rules="loginRules" 
          label-position="top"
          @keyup.enter="handleLogin"
        >
          <el-form-item label="用户名" prop="username">
            <el-input 
              v-model="loginForm.username" 
              placeholder="请输入用户名" 
              prefix-icon="el-icon-user"
            ></el-input>
          </el-form-item>
          
          <el-form-item label="密码" prop="password">
            <el-input 
              v-model="loginForm.password" 
              type="password" 
              placeholder="请输入密码" 
              prefix-icon="el-icon-lock"
              show-password
            ></el-input>
          </el-form-item>
          
          <el-form-item label="角色" prop="role">
            <el-radio-group v-model="loginForm.role">
              <el-radio :label="2">普通用户</el-radio>
              <el-radio :label="3">商家</el-radio>
              <el-radio :label="1">管理员</el-radio>
            </el-radio-group>
          </el-form-item>
          
          <div class="login-options">
            <el-checkbox v-model="rememberMe">记住我</el-checkbox>
            <el-button type="text" @click="forgotPassword">忘记密码？</el-button>
          </div>
          
          <el-form-item>
            <el-button 
              type="primary" 
              class="login-button" 
              :loading="loading" 
              @click="handleLogin"
            >
              登录
            </el-button>
          </el-form-item>
        </el-form>
        
        <div class="login-footer">
          <p>还没有账号？ <router-link to="/register">立即注册</router-link></p>
        </div>
        
        <!-- 开发者选项 -->
        <div class="dev-options">
          <el-divider>开发者选项</el-divider>
          <div class="dev-buttons">
            <el-button type="warning" size="small" @click="resetLocalStorage">重置本地存储</el-button>
            <el-button type="info" size="small" @click="migrateData">触发数据迁移</el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useStore } from 'vuex'
import { message } from '@/components/common'
import { checkAndMigrateData } from '@/utils/versionManager'
import { isDevMode, setDevRole } from '@/utils/devUtils'

export default {
  name: 'LoginPage',
  setup() {
    const store = useStore()
    const router = useRouter()
    const route = useRoute()
    
    // 登录表单
    const loginForm = reactive({
      username: '',
      password: '',
      role: 2 // 默认为普通用户
    })
    
    // 表单验证规则
    const loginRules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '用户名长度在3到20个字符之间', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '密码长度在6到20个字符之间', trigger: 'blur' }
      ],
      role: [
        { required: true, message: '请选择角色', trigger: 'change' }
      ]
    }
    
    const rememberMe = ref(false)
    const loading = ref(false)
    const loginFormRef = ref(null)
    const redirect = computed(() => route.query.redirect || '/tea-culture')
    
    // 自动填充记住的用户信息
    onMounted(() => {
      // 如果存在记住的用户信息，自动填充
      const rememberedUser = localStorage.getItem('rememberedUser')
      if (rememberedUser) {
        const userInfo = JSON.parse(rememberedUser)
        loginForm.username = userInfo.username
        loginForm.role = userInfo.role
        rememberMe.value = true
      }
    })
    
    // 处理登录
    const handleLogin = async () => {
      /* UI-DEV-START */
      // 开发时始终允许登录，无需真实验证
      if (isDevMode()) {
        console.log('开发模式：模拟登录成功')
        
        // 获取选择的角色（可选，如果有角色选择界面）
        const selectedRole = loginForm.role || 2 // 默认为普通用户
        
        // 设置开发角色
        setDevRole(selectedRole)
        
        // 跳转到首页或指定页面
        const redirect = route.query.redirect || '/tea-culture'
        router.push(redirect)
        return
      }
      /* UI-DEV-END */
      
      /* 
      // 真实代码(开发UI时注释)
      try {
        await store.dispatch('user/login', {
          username: loginForm.username,
          password: loginForm.password
        })
        
        const redirect = route.query.redirect || '/tea-culture'
        router.push(redirect)
      } catch (error) {
        console.error('登录失败:', error)
      }
      */
    }
    
    // 忘记密码
    const forgotPassword = () => {
      message.info('忘记密码功能尚未开放，请联系管理员重置密码')
    }
    
    // 开发者选项：重置本地存储
    const resetLocalStorage = () => {
      if (confirm('确定要重置所有本地存储数据吗？这将清除所有登录状态和用户数据。')) {
        localStorage.clear()
        message.success('本地存储已重置，页面将刷新')
        setTimeout(() => {
          window.location.reload()
        }, 1000)
      }
    }
    
    // 开发者选项：触发数据迁移
    const migrateData = () => {
      try {
        // 将版本号重置，触发迁移
        localStorage.removeItem('app_data_version')
        
        // 使用非静默模式(false)执行迁移，允许显示消息
        checkAndMigrateData(false)
        
        message.success('数据迁移已触发')
      } catch (e) {
        console.error('数据迁移失败:', e)
        message.error('数据迁移失败: ' + e.message)
      }
    }
    
    return {
      loginForm,
      loginRules,
      rememberMe,
      loading,
      loginFormRef,
      handleLogin,
      forgotPassword,
      resetLocalStorage,
      migrateData
    }
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 40px 0;
  position: relative;
}

.login-container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: center;
}

.login-box {
  width: 450px;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
  
  h2 {
    font-size: 28px;
    color: #409EFF;
    margin-bottom: 10px;
    font-weight: 600;
  }
  
  p {
    font-size: 16px;
    color: #606266;
  }
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.login-button {
  width: 100%;
  padding: 12px 0;
  font-size: 16px;
  border-radius: 4px;
}

.login-footer {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: #606266;
  
  a {
    color: #409EFF;
    text-decoration: none;
    font-weight: 500;
    
    &:hover {
      text-decoration: underline;
    }
  }
}

.dev-options {
  margin-top: 20px;
  
  .dev-buttons {
    display: flex;
    gap: 10px;
    justify-content: center;
  }
}

/* 添加Logo装饰效果 */
.login-page:before {
  content: "商南茶文化";
  position: absolute;
  top: 30px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 32px;
  font-weight: bold;
  color: #409EFF;
  text-shadow: 1px 1px 3px rgba(0, 0, 0, 0.1);
}

@media (max-width: 576px) {
  .login-box {
    width: 90%;
    padding: 30px 20px;
  }
  
  .login-page:before {
    font-size: 24px;
  }
}
</style> 