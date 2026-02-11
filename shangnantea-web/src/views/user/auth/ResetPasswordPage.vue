<template>
  <div class="reset-password-page">
    <div class="reset-password-container">
      <div class="reset-password-box">
        <div class="reset-password-header">
          <h2>密码找回</h2>
          <p>通过手机号或邮箱找回您的账户密码</p>
        </div>
        
        <el-form 
          ref="resetFormRef" 
          :model="resetForm" 
          :rules="resetRules" 
          label-position="top"
        >
          <el-form-item label="用户名" prop="username">
            <el-input
              v-model="resetForm.username"
              placeholder="请输入用户名"
              prefix-icon="el-icon-user"
            ></el-input>
          </el-form-item>
          <el-form-item label="找回方式" prop="method">
            <el-radio-group v-model="resetForm.method">
              <el-radio value="phone">手机号</el-radio>
              <el-radio value="email">邮箱</el-radio>
            </el-radio-group>
          </el-form-item>
          
          <el-form-item 
            v-if="resetForm.method === 'phone'"
            label="手机号" 
            prop="phone"
          >
            <el-input 
              v-model="resetForm.phone" 
              placeholder="请输入手机号" 
              prefix-icon="el-icon-mobile-phone"
            ></el-input>
          </el-form-item>
          
          <el-form-item 
            v-if="resetForm.method === 'email'"
            label="邮箱" 
            prop="email"
          >
            <el-input 
              v-model="resetForm.email" 
              placeholder="请输入邮箱" 
              prefix-icon="el-icon-message"
            ></el-input>
          </el-form-item>
          
          <el-form-item label="验证码" prop="verificationCode">
            <div class="verification-code-input">
              <el-input 
                v-model="resetForm.verificationCode" 
                placeholder="请输入验证码"
                prefix-icon="el-icon-key"
              ></el-input>
              <el-button 
                type="primary" 
                :disabled="codeCountdown > 0"
                @click="sendVerificationCode"
              >
                {{ codeCountdown > 0 ? `${codeCountdown}秒后重试` : '发送验证码' }}
              </el-button>
            </div>
          </el-form-item>
          
          <el-form-item>
            <el-button 
              type="primary" 
              class="reset-button" 
              :loading="loading" 
              @click="handleReset"
            >
              下一步
            </el-button>
          </el-form-item>
        </el-form>
        
        <div class="reset-password-footer">
          <p>想起密码了？ <router-link to="/login">返回登录</router-link></p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { showByCode, isSuccess } from '@/utils/apiMessages'
import { userPromptMessages } from '@/utils/promptMessages'

const userStore = useUserStore()
const router = useRouter()
const resetFormRef = ref(null)

// 表单数据
const resetForm = reactive({
  username: '',
  method: 'phone',
  phone: '',
  email: '',
  verificationCode: ''
})
    
// 表单验证规则
const resetRules = reactive({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3到20个字符之间', trigger: 'blur' }
  ],
  method: [
    { required: true, message: '请选择找回方式', trigger: 'change' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  verificationCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码必须为6位', trigger: 'blur' }
  ]
})

const loading = ref(false)
const codeCountdown = ref(0)

// 发送验证码
const sendVerificationCode = async () => {
  // 验证输入
  let contact = ''
  let contactType = ''

  if (resetForm.method === 'phone') {
    if (!resetForm.phone) {
      userPromptMessages.error.showFormIncomplete()
      return
    }
    contact = resetForm.phone
    contactType = 'phone'
  } else if (resetForm.method === 'email') {
    if (!resetForm.email) {
      userPromptMessages.error.showFormIncomplete()
      return
    }
    contact = resetForm.email
    contactType = 'email'
  } else {
    // 理论上不会走到这里，兜底处理
    userPromptMessages.error.showError('当前找回方式不支持，请选择手机号或邮箱')
    return
  }
  
  try {
    const res = await userStore.sendVerificationCode({
      contact,
      contactType,
      sceneType: 'reset_password'
    })
    
    // 显示发送结果
    showByCode(res.code)
    
    // 如果发送成功，启动倒计时
    if (isSuccess(res.code)) {
      codeCountdown.value = 60
      const timer = setInterval(() => {
        codeCountdown.value--
        if (codeCountdown.value <= 0) {
          clearInterval(timer)
        }
      }, 1000)
    }
  } catch (error) {
    console.error('发送验证码失败:', error)
  }
}

// 第一步：校验表单并跳转到设置新密码页面
const handleReset = async () => {
  if (!resetFormRef.value) return
  
  try {
    await resetFormRef.value.validate()
  } catch (error) {
    return false
  }

  // 将第一步收集到的数据通过路由参数传递给下一步页面
  const contactType = resetForm.method
  const contact = contactType === 'phone' ? resetForm.phone : resetForm.email

  router.push({
    path: '/reset-password/confirm',
    query: {
      username: resetForm.username,
      contactType,
      contact,
      verificationCode: resetForm.verificationCode
    }
  })
}

</script>

<style lang="scss" scoped>
.reset-password-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 40px 0;
}

.reset-password-container {
  width: 100%;
  max-width: 500px;
  padding: 0 20px;
}

.reset-password-box {
  background: white;
  border-radius: 8px;
  padding: 40px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.reset-password-header {
  text-align: center;
  margin-bottom: 30px;
  
  h2 {
    font-size: 24px;
    color: #303133;
    margin-bottom: 10px;
  }
  
  p {
    color: #909399;
    font-size: 14px;
  }
}

.verification-code-input {
  display: flex;
  gap: 10px;
  
  .el-input {
    flex: 1;
  }
}

.reset-button {
  width: 100%;
  margin-bottom: 10px;
}

.reset-password-footer {
  text-align: center;
  margin-top: 20px;
  color: #909399;
  font-size: 14px;
  
  a {
    color: #409EFF;
    text-decoration: none;
    
    &:hover {
      text-decoration: underline;
    }
  }
}
</style>
