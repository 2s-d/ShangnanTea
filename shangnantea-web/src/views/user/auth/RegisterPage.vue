<template>
  <div class="register-page">
    <div class="register-container">
      <div class="register-box">
        <div class="register-header">
          <h2>用户注册</h2>
          <p>注册商南茶文化平台账号</p>
        </div>
        
        <el-form 
          ref="registerFormRef" 
          :model="registerForm" 
          :rules="registerRules" 
          label-position="top"
        >
          <el-form-item label="用户名" prop="username">
            <el-input 
              v-model="registerForm.username" 
              placeholder="请输入用户名，3-20个字符" 
              prefix-icon="el-icon-user"
            ></el-input>
          </el-form-item>
          
          <el-form-item label="昵称" prop="nickname">
            <el-input 
              v-model="registerForm.nickname" 
              placeholder="请输入昵称，2-20个字符" 
              prefix-icon="el-icon-user"
            ></el-input>
          </el-form-item>
          
          <el-form-item label="密码" prop="password">
            <el-input 
              v-model="registerForm.password" 
              type="password" 
              placeholder="请输入密码，6-20个字符" 
              prefix-icon="el-icon-lock"
              show-password
            ></el-input>
          </el-form-item>
          
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input 
              v-model="registerForm.confirmPassword" 
              type="password" 
              placeholder="请再次输入密码" 
              prefix-icon="el-icon-lock"
              show-password
            ></el-input>
          </el-form-item>
          
          <el-form-item label="手机号" prop="phone">
            <el-input 
              v-model="registerForm.phone" 
              placeholder="请输入手机号（可选）" 
              prefix-icon="el-icon-mobile-phone"
            ></el-input>
          </el-form-item>
          
          <el-form-item label="邮箱" prop="email">
            <el-input 
              v-model="registerForm.email" 
              placeholder="请输入邮箱（可选）" 
              prefix-icon="el-icon-message"
            ></el-input>
          </el-form-item>
          
          <el-form-item>
            <el-checkbox v-model="registerForm.agreement" required>
              我已阅读并同意 <el-button type="text" @click="showAgreement">《用户协议》</el-button> 和 <el-button type="text" @click="showPrivacy">《隐私政策》</el-button>
            </el-checkbox>
          </el-form-item>
          
          <el-form-item>
            <el-button 
              type="primary" 
              class="register-button" 
              :loading="loading" 
              @click="handleRegister"
            >
              注册
            </el-button>
          </el-form-item>
        </el-form>
        
        <div class="register-footer">
          <p>已有账号？ <router-link to="/login">立即登录</router-link></p>
        </div>
      </div>
    </div>
    
    <!-- 协议对话框 -->
    <el-dialog v-model="agreementVisible" title="用户协议" width="60%">
      <div class="agreement-content">
        <h3>商南茶文化平台用户协议</h3>
        <p>欢迎使用商南茶文化平台！本协议是您与商南茶文化平台之间关于用户使用服务所订立的契约。</p>
        <p>1. 接受条款：通过注册成为商南茶文化平台用户，您确认已阅读并同意本协议的所有条款。</p>
        <p>2. 账户安全：您需要妥善保管账户信息，对账户下的所有活动负全部责任。</p>
        <p>3. 用户行为：您承诺遵守所有适用的法律法规，不发布违法、侵权或不适当的内容。</p>
        <p>4. 知识产权：平台上的内容受知识产权法保护，未经授权不得擅自使用。</p>
        <p>5. 服务变更：平台有权随时修改或终止服务，并保留修改本协议的权利。</p>
      </div>
    </el-dialog>
    
    <!-- 隐私政策对话框 -->
    <el-dialog v-model="privacyVisible" title="隐私政策" width="60%">
      <div class="agreement-content">
        <h3>商南茶文化平台隐私政策</h3>
        <p>我们非常重视用户的隐私保护，本政策说明我们如何收集、使用和保护您的个人信息。</p>
        <p>1. 信息收集：我们会收集必要的用户信息，如用户名、联系方式等，用于提供服务。</p>
        <p>2. 信息使用：我们仅将收集的信息用于提供、维护和改进服务，以及必要的通知。</p>
        <p>3. 信息共享：除法律要求或获得您的授权外，我们不会与第三方共享您的个人信息。</p>
        <p>4. 信息安全：我们采取合理的安全措施保护您的个人信息不被未授权访问或使用。</p>
        <p>5. Cookie使用：我们使用Cookie技术优化用户体验，您可以通过浏览器设置管理Cookie。</p>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { showByCode, isSuccess } from '@/utils/apiMessages'
import { userPromptMessages as userMessages } from '@/utils/promptMessages'

export default {
  name: 'RegisterPage',
  setup() {
    const store = useStore()
    const router = useRouter()
    const registerFormRef = ref(null)
    
    // 注册表单
    const registerForm = reactive({
      username: '',
      nickname: '',
      password: '',
      confirmPassword: '',
      phone: '',
      email: '',
      agreement: false
    })
    
    // 确认密码验证
    const validateConfirmPassword = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请再次输入密码'))
      } else if (value !== registerForm.password) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }
    
    // 协议验证
    const validateAgreement = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请阅读并同意用户协议和隐私政策'))
      } else {
        callback()
      }
    }
    
    // 表单验证规则
    const registerRules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '用户名长度在3到20个字符之间', trigger: 'blur' },
        { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
      ],
      nickname: [
        { required: true, message: '请输入昵称', trigger: 'blur' },
        { min: 2, max: 20, message: '昵称长度在2到20个字符之间', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '密码长度在6到20个字符之间', trigger: 'blur' },
        { pattern: /^(?=.*[a-zA-Z])(?=.*[0-9])[a-zA-Z0-9]+$/, message: '密码必须同时包含字母和数字', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, message: '请再次输入密码', trigger: 'blur' },
        { validator: validateConfirmPassword, trigger: 'blur' }
      ],
      phone: [
        { 
          validator: (rule, value, callback) => {
            // 为空时不验证格式
            if (!value || value.trim() === '') {
              callback()
            } else if (!/^1[3-9]\d{9}$/.test(value)) {
              callback(new Error('手机号格式不正确'))
            } else {
              callback()
            }
          },
          trigger: 'blur' 
        }
      ],
      email: [
        { 
          validator: (rule, value, callback) => {
            // 为空时不验证格式
            if (!value || value.trim() === '') {
              callback()
            } else if (!/^[\w.-]+@[\w.-]+\.\w+$/.test(value)) {
              callback(new Error('邮箱格式不正确'))
            } else {
              callback()
            }
          },
          trigger: 'blur' 
        }
      ],
      agreement: [
        { validator: validateAgreement, trigger: 'change' }
      ]
    }
    
    const loading = ref(false)
    const agreementVisible = ref(false)
    const privacyVisible = ref(false)
    
    // 处理注册
    const handleRegister = async () => {
      if (!registerFormRef.value) return
      
      try {
        await registerFormRef.value.validate()
      } catch (error) {
        return false
      }
      
      if (!registerForm.agreement) {
        // 使用userMessages处理提示消息
        userMessages.prompt.showAgreementRequired()
        return false
      }
      
      loading.value = true
      
      try {
        // 准备注册数据，确保正确处理可选字段
        const registerData = {
          username: registerForm.username,
          nickname: registerForm.nickname,
          password: registerForm.password,
          confirmPassword: registerForm.confirmPassword,
          // 确保可选字段为空时正确处理
          phone: registerForm.phone && registerForm.phone.trim() ? registerForm.phone.trim() : undefined,
          email: registerForm.email && registerForm.email.trim() ? registerForm.email.trim() : undefined
        }
        
        // 调用注册API
        const response = await store.dispatch('user/register', registerData)
        
        if (isSuccess(response.code)) {
          showByCode(response.code)
          router.push('/login')
        } else {
          showByCode(response.code)
        }
      } catch (error) {
        console.error('注册失败:', error)
        loading.value = false
        return false
      }
    }
    
    // 显示用户协议
    const showAgreement = () => {
      agreementVisible.value = true
    }
    
    // 显示隐私政策
    const showPrivacy = () => {
      privacyVisible.value = true
    }
    
    return {
      registerForm,
      registerRules,
      registerFormRef,
      loading,
      agreementVisible,
      privacyVisible,
      handleRegister,
      showAgreement,
      showPrivacy
    }
  }
}
</script>

<style lang="scss" scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 40px 0;
  position: relative;
}

.register-container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: center;
}

.register-box {
  width: 500px;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
}

.register-header {
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

.register-button {
  width: 100%;
  padding: 12px 0;
  font-size: 16px;
  border-radius: 4px;
}

.register-footer {
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

.agreement-content {
  max-height: 400px;
  overflow-y: auto;
  
  h3 {
    margin-bottom: 20px;
    color: #303133;
    font-weight: 500;
  }
  
  p {
    margin-bottom: 15px;
    line-height: 1.6;
    color: #606266;
  }
}

/* 添加Logo装饰效果 */
.register-page:before {
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
  .register-box {
    width: 90%;
    padding: 30px 20px;
  }
  
  .register-page:before {
    font-size: 24px;
  }
}
</style> 