<template>
  <div class="profile-page">
    <div class="container">
      <h1 class="page-title">个人中心</h1>
      
      <el-card class="profile-card" v-loading="loading">
        <div class="profile-content">
          <el-form 
            :model="userForm" 
            :rules="rules"
            ref="userFormRef"
            label-width="100px" 
            class="user-form"
            status-icon
          >
            <el-form-item label="用户名">
              <el-input v-model="userForm.username" disabled></el-input>
            </el-form-item>
            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="userForm.nickname" placeholder="请输入您的昵称"></el-input>
            </el-form-item>
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="userForm.gender">
                <el-radio :label="1">男</el-radio>
                <el-radio :label="2">女</el-radio>
                <el-radio :label="0">保密</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="生日" prop="birthday">
              <el-date-picker 
                v-model="userForm.birthday" 
                type="date" 
                placeholder="选择生日"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                :clearable="true"
                :disabled-date="disabledDate"
              ></el-date-picker>
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="userForm.phone" placeholder="请输入您的手机号"></el-input>
            </el-form-item>
            <el-form-item label="电子邮箱" prop="email">
              <el-input v-model="userForm.email" placeholder="请输入您的电子邮箱"></el-input>
            </el-form-item>
            <el-form-item label="个人简介" prop="bio">
              <el-input 
                v-model="userForm.bio" 
                type="textarea" 
                :rows="4" 
                placeholder="请输入您的个人简介"
                maxlength="200"
                show-word-limit
              ></el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSaveUserInfo" :loading="saving">保存修改</el-button>
              <el-button @click="handleResetForm">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useStore } from 'vuex'
import { useRoute } from 'vue-router'
import { message } from '@/components/common'
import messageManager from '@/utils/messageManager'
import { handleAsyncOperation, STANDARD_MESSAGES } from '@/utils/messageHelper'
import { uploadImage } from '@/api/upload'
import messageMessages from '@/utils/messageMessages'
import { useFormValidation } from '@/composables/useFormValidation'

export default {
  name: 'ProfilePage',
  setup() {
    const store = useStore()
    const route = useRoute()
    const userForm = ref(null)
    
    const loading = ref(false)
    const saving = ref(false)
    
    const pageTitle = computed(() => {
      return '个人中心'
    })
    
    const formData = reactive({
      username: '',
      nickname: '',
      email: '',
      phone: '',
      gender: 0,  // 0-保密 1-男 2-女
      birthday: '',
      bio: ''
    })
    
    // TODO-SCRIPT: 用户信息应从 Vuex user 模块获取（user/getUserInfo），此处不在 UI 层保留 mock 声明
    
    const { rules: validationRules } = useFormValidation()
    
    const validatePhone = (rule, value, callback) => {
      if (value && !/^1[3-9]\d{9}$/.test(value)) {
        callback(new Error('请输入正确的手机号码'))
      } else {
        callback()
      }
    }
    
    const validateEmail = (rule, value, callback) => {
      if (value && !/^[\w-]+(\.[\w-]+)*@([\w-]+\.)+[a-zA-Z]{2,7}$/.test(value)) {
        callback(new Error('请输入正确的邮箱地址'))
      } else {
        callback()
      }
    }
    
    const disabledDate = (time) => {
      return time.getTime() > Date.now()
    }
    
    const rules = {
      nickname: [
        { max: 20, message: '昵称不能超过20个字符', trigger: 'blur' }
      ],
      phone: [
        { validator: validatePhone, trigger: 'blur' }
      ],
      email: [
        { validator: validateEmail, trigger: 'blur' }
      ],
      bio: [
        { max: 200, message: '个人简介不能超过200个字符', trigger: 'blur' }
      ]
    }
    
    const userInfo = computed(() => {
      return store.state.user.userInfo
    })
    
    const handleInitForm = () => {
      if (userInfo.value) {
        formData.username = userInfo.value.username || '';
        formData.nickname = userInfo.value.nickname || '';
        formData.email = userInfo.value.email || '';
        formData.phone = userInfo.value.phone || '';
        formData.gender = userInfo.value.gender || 0;
        formData.birthday = userInfo.value.birthday || '';
        formData.bio = userInfo.value.bio || '';
      } else {
        messageMessages.ui.showUserDataIncomplete();
      }
    }
    
    const handleFetchUserInfo = async () => {
      try {
        loading.value = true;
        
        const result = await handleAsyncOperation(
          store.dispatch('user/getUserInfo'),
          {
            successMessage: null,
            errorMessage: messageMessages.MESSAGE_MESSAGES.API.USER_INFO_LOADING_FAILED
          }
        );
        
        if (result) {
          handleInitForm();
        } else {
          messageMessages.business.showUserInfoEmpty();
        }
      } catch (error) {
        messageMessages.api.showUserInfoLoadingFailed(error);
      } finally {
        loading.value = false;
      }
    }
    
    const handleResetForm = () => {
      userForm.value.resetFields()
      handleInitForm()
    }
    
    const handleSaveUserInfo = async () => {
      try {
        await userForm.value.validate(async valid => {
          if (!valid) return
          
          saving.value = true
          loading.value = true
          
          const userData = {
            ...userInfo.value,
            ...formData
          }
          
          await handleAsyncOperation(
            store.dispatch('user/updateUserInfo', userData),
            {
              successMessage: '个人信息更新成功',
              errorMessage: '保存用户信息失败，请重试'
            }
          )
          
          await store.dispatch('user/getUserInfo')
          
          handleInitForm()
        })
      } catch (error) {
        console.error('保存用户信息失败:', error)
      } finally {
        saving.value = false
        loading.value = false
      }
    }
    
    watch(() => userInfo.value, (newVal) => {
      if (newVal) {
        handleInitForm()
      }
    }, { immediate: true, deep: true })
    
    onMounted(() => {
      handleFetchUserInfo()
    })
    
    return {
      userFormRef: userForm,
      loading,
      saving,
      userInfo,
      userForm: formData,
      rules,
      disabledDate,
      pageTitle,
      handleResetForm,
      handleSaveUserInfo
    }
  }
}
</script>

<style lang="scss" scoped>
.profile-page {
  padding: 40px 0;
  
  .page-title {
    font-size: 2rem;
    color: var(--text-primary);
    margin-bottom: 30px;
    text-align: center;
  }
  
  .profile-card {
    margin-bottom: 40px;
    
    .profile-content {
      padding: 20px 0;
      
      .section-content {
        margin: 0 auto;
      }
    }
    
    .user-form {
      max-width: 500px;
      margin: 0 auto;
      
      .el-date-picker {
        width: 100%;
      }
      
      .el-textarea {
        font-size: 14px;
        
        &__inner {
          min-height: 100px;
          resize: vertical;
        }
      }
      
      .el-radio-group {
        display: flex;
        flex-wrap: wrap;
        gap: 10px;
      }
      
      .el-form-item__content {
        line-height: 1.5;
      }
    }
  }
}

/* 移动端适配 */
@media (max-width: 768px) {
  .profile-page {
    .profile-card {
      .user-form {
        max-width: 100%;
      }
    }
  }
}
</style> 