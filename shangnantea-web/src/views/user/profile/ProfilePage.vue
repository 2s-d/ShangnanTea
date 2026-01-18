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
            <el-form-item label="头像">
              <div class="avatar-upload-container">
                <SafeImage 
                  :src="userInfo?.avatar || '/images/avatar/default.png'" 
                  type="avatar" 
                  alt="用户头像" 
                  class="avatar-preview"
                  style="width:100px;height:100px;border-radius:50%;object-fit:cover;"
                />
                <el-upload
                  class="avatar-uploader"
                  :show-file-list="false"
                  :before-upload="beforeAvatarUpload"
                  :http-request="handleAvatarUpload"
                  accept="image/*"
                >
                  <el-button type="primary" size="small">更换头像</el-button>
                </el-upload>
              </div>
            </el-form-item>
            <el-form-item label="用户名">
              <el-input v-model="userForm.username" disabled></el-input>
            </el-form-item>
            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="userForm.nickname" placeholder="请输入您的昵称"></el-input>
            </el-form-item>
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="userForm.gender">
                <el-radio :value="1">男</el-radio>
                <el-radio :value="2">女</el-radio>
                <el-radio :value="0">保密</el-radio>
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
            <!-- 任务C-4：商家认证状态显示 -->
            <el-form-item label="商家认证" v-if="!isAdmin">
              <div class="certification-status">
                <el-tag 
                  :type="getCertificationTagType(certificationStatus)" 
                  v-if="certificationStatus !== null"
                >
                  {{ getCertificationStatusText(certificationStatus) }}
                </el-tag>
                <el-button 
                  type="primary" 
                  size="small" 
                  @click="goToMerchantApplication"
                  v-if="certificationStatus === null || certificationStatus === -1 || certificationStatus === 1"
                >
                  {{ certificationStatus === null || certificationStatus === -1 ? '申请商家认证' : '重新申请' }}
                </el-button>
              </div>
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
import { useRoute, useRouter } from 'vue-router'

import messageManager from '@/utils/messageManager'

import { useFormValidation } from '@/composables/useFormValidation'
import SafeImage from '@/components/common/form/SafeImage.vue'
import { userPromptMessages as userMessages, messagePromptMessages as messageMessages } from '@/utils/promptMessages'
import { showByCode, isSuccess } from '@/utils/apiMessages'

export default {
  name: 'ProfilePage',
  components: {
    SafeImage
  },
  setup() {
    const store = useStore()
    const route = useRoute()
    const router = useRouter()
    const userForm = ref(null)
    
    const loading = ref(false)
    const saving = ref(false)
    const certificationStatus = ref(null) // 任务C-4：认证状态 -1未申请 0待审核 1已拒绝 2已通过
    
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
    
    const disabledDate = time => {
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
    
    // 任务C-4：判断是否是管理员
    const isAdmin = computed(() => {
      return store.getters['user/isAdmin']
    })
    
    // 任务C-4：获取认证状态文本
    const getCertificationStatusText = status => {
      if (status === null || status === -1) return '未申请'
      if (status === 0) return '待审核'
      if (status === 1) return '已拒绝'
      if (status === 2) return '已通过'
      return '未知'
    }
    
    // 任务C-4：获取认证状态标签类型
    const getCertificationTagType = status => {
      if (status === null || status === -1) return 'info'
      if (status === 0) return 'warning'
      if (status === 1) return 'danger'
      if (status === 2) return 'success'
      return 'info'
    }
    
    // 任务C-4：跳转到商家认证申请页
    const goToMerchantApplication = () => {
      router.push('/merchant-application')
    }
    
    // 任务C-4：获取认证状态
    const fetchCertificationStatus = async () => {
      // 只有非管理员用户才需要显示认证状态
      if (isAdmin.value) {
        return
      }
      
      try {
        const result = await store.dispatch('user/fetchShopCertificationStatus')
        if (result && result.data) {
          certificationStatus.value = result.data.status
        } else {
          certificationStatus.value = -1 // 未申请
        }
      } catch (error) {
        console.error('获取认证状态失败:', error)
        certificationStatus.value = -1
      }
    }
    
    const handleInitForm = () => {
      if (userInfo.value) {
        formData.username = userInfo.value.username || ''
        formData.nickname = userInfo.value.nickname || ''
        formData.email = userInfo.value.email || ''
        formData.phone = userInfo.value.phone || ''
        formData.gender = userInfo.value.gender || 0
        formData.birthday = userInfo.value.birthday || ''
        formData.bio = userInfo.value.bio || ''
      } else {
        messageMessages.showUserDataIncomplete()
      }
    }
    
    const handleFetchUserInfo = async () => {
      try {
        loading.value = true
        
        const response = await store.dispatch('user/getUserInfo')
        
        // 显示API响应消息（成功或失败都通过状态码映射显示）
        showByCode(response.code)
        
        // 只有成功时才初始化表单
        if (isSuccess(response.code)) {
          handleInitForm()
        }
      } catch (error) {
        // TODO: 迁移到新消息系统 - 使用 showByCode(response.code)
        showByCode(7121) // 用户信息不完整
      } finally {
        loading.value = false
      }
    }
    
    const handleResetForm = () => {
      userForm.value.resetFields()
      handleInitForm()
    }
    
    // 头像上传前验证
    const beforeAvatarUpload = file => {
      const isImage = file.type.startsWith('image/')
      const isLt2M = file.size / 1024 / 1024 < 2
      
      if (!isImage) {
        // TODO: 迁移到新消息系统 - 使用 showByCode(response.code)
        userMessages.showAvatarFormatInvalid()
        return false
      }
      if (!isLt2M) {
        // TODO: 迁移到新消息系统 - 使用 showByCode(response.code)
        userMessages.showAvatarSizeLimit()
        return false
      }
      return true
    }
    
    // 头像上传处理
    // 任务A-5：使用Vuex uploadAvatar action
    const handleAvatarUpload = async options => {
      const file = options.file
      if (!file) return
      
      try {
        loading.value = true
        
        // 上传头像
        const uploadResponse = await store.dispatch('user/uploadAvatar', file)
        
        // 显示API响应消息（成功或失败都通过状态码映射显示）
        showByCode(uploadResponse.code)
        
        // 只有成功时才刷新用户信息
        if (isSuccess(uploadResponse.code)) {
          const userInfoResponse = await store.dispatch('user/getUserInfo')
          if (isSuccess(userInfoResponse.code)) {
            handleInitForm()
          }
        }
      } catch (error) {
        // 捕获意外的运行时错误（非API业务错误）
        // API业务失败已通过 showByCode 显示，网络错误已在响应拦截器显示
        if (process.env.NODE_ENV === 'development') {
          console.error('[开发调试] 头像上传时发生意外错误：', error)
        }
      } finally {
        loading.value = false
      }
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
          
          // 更新用户信息
          const updateResponse = await store.dispatch('user/updateUserInfo', userData)
          
          // 显示API响应消息（成功或失败都通过状态码映射显示）
          showByCode(updateResponse.code)
          
          // 只有成功时才刷新用户信息
          if (isSuccess(updateResponse.code)) {
            const userInfoResponse = await store.dispatch('user/getUserInfo')
            if (isSuccess(userInfoResponse.code)) {
              handleInitForm()
            }
          }
        })
      } catch (error) {
        // 捕获意外的运行时错误（非API业务错误）
        // API业务失败已通过 showByCode 显示，网络错误已在响应拦截器显示
        if (process.env.NODE_ENV === 'development') {
          console.error('[开发调试] 保存用户信息时发生意外错误：', error)
        }
      } finally {
        saving.value = false
        loading.value = false
      }
    }
    
    watch(() => userInfo.value, newVal => {
      if (newVal) {
        handleInitForm()
      }
    }, { immediate: true, deep: true })
    
    onMounted(() => {
      handleFetchUserInfo()
      fetchCertificationStatus() // 任务C-4：获取认证状态
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
      handleSaveUserInfo,
      beforeAvatarUpload,
      handleAvatarUpload,
      // 任务C-4：认证相关
      isAdmin,
      certificationStatus,
      getCertificationStatusText,
      getCertificationTagType,
      goToMerchantApplication
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
      
      .avatar-upload-container {
        display: flex;
        align-items: center;
        gap: 20px;
        
        .avatar-preview {
          border: 2px solid var(--el-border-color);
        }
        
        .avatar-uploader {
          display: inline-block;
        }
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