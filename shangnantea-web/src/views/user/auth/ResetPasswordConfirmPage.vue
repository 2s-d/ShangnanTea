<template>
  <div class="reset-password-page">
    <div class="reset-password-container">
      <div class="reset-password-box">
        <div class="reset-password-header">
          <h2>设置新密码</h2>
          <p>请为账户 {{ username }} 设置一个新的登录密码</p>
        </div>

        <el-form
          ref="confirmFormRef"
          :model="confirmForm"
          :rules="confirmRules"
          label-position="top"
        >
          <el-form-item label="新密码" prop="newPassword">
            <el-input
              v-model="confirmForm.newPassword"
              type="password"
              placeholder="请输入新密码，6-20个字符，需包含字母和数字"
              prefix-icon="el-icon-lock"
              show-password
            ></el-input>
          </el-form-item>

          <el-form-item label="确认新密码" prop="confirmPassword">
            <el-input
              v-model="confirmForm.confirmPassword"
              type="password"
              placeholder="请再次输入新密码"
              prefix-icon="el-icon-lock"
              show-password
            ></el-input>
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              class="reset-button"
              :loading="loading"
              @click="handleSubmit"
            >
              提交
            </el-button>
            <el-button @click="goBack">返回登录</el-button>
          </el-form-item>
        </el-form>

        <div class="reset-password-footer">
          <p>
            验证信息有误？
            <router-link to="/reset-password">返回上一步</router-link>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { showByCode, isSuccess } from '@/utils/apiMessages'
import { userPromptMessages } from '@/utils/promptMessages'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const username = route.query.username || ''
const contactType = route.query.contactType || ''
const contact = route.query.contact || ''
const verificationCode = route.query.verificationCode || ''

// 如果必需的参数缺失，直接回到第一步
if (!username || !contactType || !contact || !verificationCode) {
  router.replace('/reset-password')
}

const confirmFormRef = ref(null)

const confirmForm = reactive({
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (_rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入新密码'))
  } else if (value !== confirmForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const confirmRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6到20个字符之间', trigger: 'blur' },
    {
      pattern: /^(?=.*[a-zA-Z])(?=.*[0-9])[A-Za-z0-9!@#$%^&*()_+\-=\[\]{};':",.<>?/]+$/,
      message: '密码必须同时包含字母和数字',
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const loading = ref(false)

const handleSubmit = async () => {
  if (!confirmFormRef.value) return

  try {
    await confirmFormRef.value.validate()
  } catch {
    return
  }

  loading.value = true

  try {
    const resetData = {
      username,
      contactType,
      contact,
      verificationCode,
      newPassword: confirmForm.newPassword
    }

    const res = await userStore.findPassword(resetData)

    showByCode(res.code)

    if (isSuccess(res.code)) {
      setTimeout(() => {
        router.push('/login')
      }, 2000)
    }
  } catch (error) {
    console.error('密码重置失败:', error)
    userPromptMessages.error.showError?.('密码重置失败，请稍后再试')
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.push('/login')
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

