<template>
  <div class="change-password-container">
    <div class="page-header">
      <h1>修改密码</h1>
      <p class="subtitle">您可以在此更新您的账户密码</p>
    </div>

    <el-card class="password-form-card">
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px" class="password-form">
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入当前密码"></el-input>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码"></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitPasswordForm" :loading="loading">更新密码</el-button>
          <el-button @click="resetPasswordForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive, computed } from 'vue'
import { useFormValidation } from '@/composables/useFormValidation'
import { useAuth } from '@/composables/useAuth'

export default {
  name: 'ChangePasswordPage',
  setup() {
    // 使用表单验证组合式函数
    const { confirmPasswordValidator, rules, validateForm, resetForm } = useFormValidation()
    
    // 使用认证组合式函数
    const { userInfo, loading, changePassword } = useAuth()
    
    const passwordFormRef = ref(null)
    
    // 表单数据
    const passwordForm = reactive({
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    })
    
    // 表单验证规则
    const passwordRules = reactive({
      oldPassword: [
        rules.required('请输入当前密码'),
        rules.minLength(6, '密码长度不能少于6个字符')
      ],
      newPassword: [
        rules.required('请输入新密码'),
        rules.minLength(6, '密码长度不能少于6个字符')
      ],
      confirmPassword: [
        rules.required('请再次输入新密码'),
        {
          validator: confirmPasswordValidator(passwordForm, 'newPassword'),
          trigger: 'blur'
        }
      ]
    })
    
    // 提交表单
    const submitPasswordForm = () => {
      validateForm(passwordFormRef.value, async () => {
        if (!userInfo.value || !userInfo.value.id) {
          return
        }
        
        try {
          await changePassword({
            userId: userInfo.value.id,
            oldPassword: passwordForm.oldPassword,
            newPassword: passwordForm.newPassword
          })
          
          resetPasswordForm()
        } catch (error) {
          // 错误已在changePassword中处理
        }
      })
    }
    
    // 重置表单
    const resetPasswordForm = () => {
      resetForm(passwordFormRef.value)
    }
    
    return {
      loading,
      passwordForm,
      passwordRules,
      passwordFormRef,
      submitPasswordForm,
      resetPasswordForm
    }
  }
}
</script>

<style lang="scss" scoped>
.change-password-container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 20px;

  h1 {
    font-size: 28px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 10px;
  }

  .subtitle {
    color: #606266;
    font-size: 14px;
  }
}

.password-form-card {
  margin-bottom: 20px;
}

.password-form {
  max-width: 500px;
}
</style> 