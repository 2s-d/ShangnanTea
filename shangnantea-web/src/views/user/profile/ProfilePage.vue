<template>
  <div class="profile-page">
    <div class="container">
      <h1 class="page-title">个人中心</h1>
      
      <el-card class="profile-card" v-loading="loading">
        <div class="profile-content">
          <el-form 
            :model="formData" 
            :rules="rules"
            ref="userForm"
            label-width="100px" 
            class="user-form"
            status-icon
          >
            <el-form-item label="头像">
              <div class="avatar-upload-container">
                <!-- 编辑页头像：使用传统圆形图片组件预览 -->
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
              <el-input v-model="formData.username" disabled></el-input>
            </el-form-item>
            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="formData.nickname" placeholder="请输入您的昵称"></el-input>
            </el-form-item>
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="formData.gender">
                <el-radio :value="1">男</el-radio>
                <el-radio :value="2">女</el-radio>
                <el-radio :value="0">保密</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="现居地" prop="locationRegion">
              <el-cascader 
                v-model="formData.locationRegion" 
                :options="locationOptions"
                :props="{ expandTrigger: 'hover' }"
                placeholder="请选择省市"
                clearable
                style="width: 100%"
              ></el-cascader>
            </el-form-item>
            <el-form-item label="生日" prop="birthday">
              <el-date-picker 
                v-model="formData.birthday" 
                type="date" 
                placeholder="选择生日"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                :clearable="true"
                :disabled-date="disabledDate"
              ></el-date-picker>
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="formData.phone" placeholder="请输入您的手机号"></el-input>
            </el-form-item>
            <el-form-item label="电子邮箱" prop="email">
              <el-input v-model="formData.email" placeholder="请输入您的电子邮箱"></el-input>
            </el-form-item>
            <el-form-item label="个人简介" prop="bio">
              <el-input 
                v-model="formData.bio" 
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

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useUserStore } from '@/stores/user'
import { useRoute, useRouter } from 'vue-router'

import { useFormValidation } from '@/composables/useFormValidation'
import SafeImage from '@/components/common/form/SafeImage.vue'
import { userPromptMessages as userMessages, messagePromptMessages as messageMessages } from '@/utils/promptMessages'
import { showByCode, isSuccess } from '@/utils/apiMessages'
import { provinceAndCityData, formatLocationDisplay } from '@/utils/region'

const userStore = useUserStore()
const route = useRoute()
const router = useRouter()
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
  locationRegion: [],  // 省市二级选择器使用的数组 [省code, 市code]
  birthday: '',
  bio: ''
})

// 省市二级联动数据
const locationOptions = ref(provinceAndCityData)
    
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
      return userStore.userInfo
    })
    
    const handleInitForm = () => {
      if (userInfo.value) {
        formData.username = userInfo.value.username || ''
        formData.nickname = userInfo.value.nickname || ''
        formData.email = userInfo.value.email || ''
        formData.phone = userInfo.value.phone || ''
        formData.gender = userInfo.value.gender || 0
        // 从 currentLocation 字符串解析为省市数组（格式：省code-市code 或 省名-市名）
        formData.locationRegion = parseLocationToRegion(userInfo.value.currentLocation)
        formData.birthday = userInfo.value.birthday || ''
        formData.bio = userInfo.value.bio || ''
      } else {
        messageMessages.showUserDataIncomplete()
      }
    }
    
    // 将 currentLocation 字符串解析为省市 code 数组
    const parseLocationToRegion = locationStr => {
      if (!locationStr) return []
      // 格式：610000-610100 或 陕西-商洛
      const parts = locationStr.split('-')
      if (parts.length >= 2) {
        return [parts[0], parts[1]]
      }
      return []
    }
    
    // 将省市 code 数组转换为 currentLocation 字符串（存储 code）
    const regionToLocationString = regionArr => {
      if (!regionArr || regionArr.length < 2) return ''
      return `${regionArr[0]}-${regionArr[1]}`
    }
    
    // 将省市 code 数组转换为中文显示（使用 region.js 的工具函数）
    const formatLocationForDisplay = regionArr => {
      if (!regionArr || regionArr.length < 2) return ''
      const locationCode = `${regionArr[0]}-${regionArr[1]}`
      return formatLocationDisplay(locationCode)
    }
    
    const handleFetchUserInfo = async () => {
      try {
        loading.value = true
        
        const response = await userStore.getUserInfo()
        
        // 显示API响应消息（成功或失败都通过状态码映射显示）
        showByCode(response.code)
        
        // 只有成功时才初始化表单
        if (isSuccess(response.code)) {
          handleInitForm()
        }
      } catch (error) {
        // 前端异常处理，使用固定错误码
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
        // 前端文件验证，使用 userMessages 是正确的（不涉及API调用）
        userMessages.showAvatarFormatInvalid()
        return false
      }
      if (!isLt2M) {
        // 前端文件验证，使用 userMessages 是正确的（不涉及API调用）
        userMessages.showAvatarSizeLimit()
        return false
      }
      return true
    }
    
    // 头像上传处理
    // 任务A-5：使用Pinia uploadAvatar action
    const handleAvatarUpload = async options => {
      const file = options.file
      if (!file) return
      
      try {
        loading.value = true
        
        // 上传头像
        const uploadResponse = await userStore.uploadAvatar(file)
        
        // 显示API响应消息（成功或失败都通过状态码映射显示）
        showByCode(uploadResponse.code)
        
        // 只有成功时才刷新用户信息
        if (isSuccess(uploadResponse.code)) {
          const userInfoResponse = await userStore.getUserInfo()
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
          
          // 将 locationRegion 数组转换为 currentLocation 字符串
          const userData = {
            ...userInfo.value,
            ...formData,
            currentLocation: regionToLocationString(formData.locationRegion)
          }
          // 删除前端专用的 locationRegion 字段，后端不需要
          delete userData.locationRegion
          
          // 更新用户信息
          const updateResponse = await userStore.updateUserInfo(userData)
          
          // 显示API响应消息（成功或失败都通过状态码映射显示）
          showByCode(updateResponse.code)
          
          // 只有成功时才刷新用户信息
          if (isSuccess(updateResponse.code)) {
            const userInfoResponse = await userStore.getUserInfo()
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
    })
</script>

<style lang="scss" scoped>
.profile-page {
  padding: 40px 0;
  min-height: 100vh;
  background-color: #f5f7fa;
  
  .container {
    width: 85%;
    max-width: 1920px;
    margin: 0 auto;
    padding: 0;
  }
  
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