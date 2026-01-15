<template>
  <div class="merchant-application">
    <el-form 
      :model="applicationForm" 
      :rules="rules"
      ref="applicationFormRef"
      label-width="120px"
      class="application-form"
      status-icon>
      <h3 class="section-title">个人信息</h3>
      <el-form-item label="真实姓名" prop="realName">
        <el-input v-model="applicationForm.realName" placeholder="请输入真实姓名"></el-input>
      </el-form-item>
      <el-form-item label="身份证号" prop="idCard">
        <el-input v-model="applicationForm.idCard" placeholder="请输入身份证号"></el-input>
      </el-form-item>
      <el-form-item label="联系电话" prop="contactPhone">
        <el-input v-model="applicationForm.contactPhone" placeholder="请输入联系电话"></el-input>
      </el-form-item>
      
      <h3 class="section-title">上传证件</h3>
      <el-form-item label="身份证正面照" prop="idCardFront">
        <el-upload
          class="upload-container"
          action="#"
          :http-request="uploadIdCardFront"
          :show-file-list="false"
          :before-upload="beforeImageUpload">
          <SafeImage 
            v-if="applicationForm.idCardFront" 
            :src="applicationForm.idCardFront" 
            type="banner"
            alt="身份证正面照"
            class="upload-image id-card-front" 
          />
          <el-icon v-else class="upload-icon"><Plus /></el-icon>
          <div class="upload-text">点击上传</div>
        </el-upload>
      </el-form-item>
      <el-form-item label="身份证背面照" prop="idCardBack">
        <el-upload
          class="upload-container"
          action="#"
          :http-request="uploadIdCardBack"
          :show-file-list="false"
          :before-upload="beforeImageUpload">
          <SafeImage 
            v-if="applicationForm.idCardBack" 
            :src="applicationForm.idCardBack" 
            type="banner"
            alt="身份证背面照"
            class="upload-image id-card-back" 
          />
          <el-icon v-else class="upload-icon"><Plus /></el-icon>
          <div class="upload-text">点击上传</div>
        </el-upload>
      </el-form-item>
      <el-form-item label="营业执照" prop="businessLicense">
        <el-upload
          class="upload-container"
          action="#"
          :http-request="uploadBusinessLicense"
          :show-file-list="false"
          :before-upload="beforeImageUpload">
          <SafeImage 
            v-if="applicationForm.businessLicense" 
            :src="applicationForm.businessLicense" 
            type="banner"
            alt="营业执照"
            class="upload-image business-license" 
          />
          <el-icon v-else class="upload-icon"><Plus /></el-icon>
          <div class="upload-text">点击上传</div>
        </el-upload>
      </el-form-item>
      
      <h3 class="section-title">店铺信息</h3>
      <el-form-item label="店铺名称" prop="shopName">
        <el-input v-model="applicationForm.shopName" placeholder="请输入店铺名称"></el-input>
      </el-form-item>
      <el-form-item label="所在地区" prop="region">
        <el-cascader 
          v-model="region"
          placeholder="请选择所在地区"
          :options="cascaderOptions"
          :props="{
            expandTrigger: 'hover',
            checkStrictly: false
          }"
          :loading="regionLoading"
          @change="handleRegionChange"
        ></el-cascader>
      </el-form-item>
      <el-form-item label="详细地址" prop="address">
        <el-input v-model="applicationForm.address" placeholder="请输入详细地址"></el-input>
      </el-form-item>
      <el-form-item label="申请理由" prop="applyReason">
        <el-input 
          type="textarea" 
          v-model="applicationForm.applyReason" 
          placeholder="请简要说明申请成为商家的理由"
          :rows="4"
          maxlength="500"
          show-word-limit>
        </el-input>
      </el-form-item>
      
      <el-form-item>
        <el-button type="primary" @click="submitApplication" :loading="submitting">提交申请</el-button>
        <el-button @click="resetForm">重置</el-button>
      </el-form-item>
    </el-form>
    
    <!-- 申请状态显示 -->
    <el-card v-if="hasApplied" class="status-card">
      <h3>申请状态：{{ statusText }}</h3>
      <p v-if="applicationStatus === 2">拒绝原因：{{ rejectReason || '未提供' }}</p>
      <p>提交时间：{{ applicationTime }}</p>
      <el-button v-if="applicationStatus === 2" type="primary" @click="resetApplication">重新申请</el-button>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import { useStore } from 'vuex'
import { uploadImage } from '@/api/upload'
import { Plus } from '@element-plus/icons-vue'

import { regionData, getStaticRegionData } from '@/utils/region'

import SafeImage from '@/components/common/form/SafeImage.vue'
import { userPromptMessages as userMessages, shopPromptMessages as shopMessages } from '@/utils/promptMessages'
import { showByCode, isSuccess } from '@/utils/apiMessages'

export default {
  name: 'MerchantApplication',
  components: {
    Plus,
    SafeImage
  },
  setup() {
    const store = useStore()
    const applicationFormRef = ref(null)
    const submitting = ref(false)
    
    // 是否已申请认证
    const hasApplied = ref(false)
    // 申请状态：0待审核,1已通过,2已拒绝
    const applicationStatus = ref(0)
    const rejectReason = ref('')
    const applicationTime = ref('')
    
    // 申请状态文本
    const statusText = computed(() => {
      const statusMap = {
        0: '审核中',
        1: '已通过',
        2: '已拒绝'
      }
      return statusMap[applicationStatus.value] || '未知状态'
    })
    
    // 地区数据相关
    const cascaderOptions = ref([])
    const regionLoading = ref(false)
    const region = ref([])
    
    // 申请表单数据
    const applicationForm = reactive({
      realName: '',
      idCard: '',
      idCardFront: '',
      idCardBack: '',
      businessLicense: '',
      shopName: '',
      contactPhone: '',
      province: '',
      city: '',
      district: '',
      address: '',
      applyReason: ''
    })
    
    // 表单验证规则
    const rules = {
      realName: [
        { required: true, message: '请输入真实姓名', trigger: 'blur' },
        { min: 2, max: 20, message: '长度在2到20个字符之间', trigger: 'blur' }
      ],
      idCard: [
        { required: true, message: '请输入身份证号', trigger: 'blur' },
        { pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/, message: '请输入正确的身份证号码', trigger: 'blur' }
      ],
      idCardFront: [
        { required: true, message: '请上传身份证正面照', trigger: 'change' }
      ],
      idCardBack: [
        { required: true, message: '请上传身份证背面照', trigger: 'change' }
      ],
      businessLicense: [
        { required: true, message: '请上传营业执照', trigger: 'change' }
      ],
      shopName: [
        { required: true, message: '请输入店铺名称', trigger: 'blur' },
        { min: 2, max: 50, message: '长度在2到50个字符之间', trigger: 'blur' }
      ],
      contactPhone: [
        { required: true, message: '请输入联系电话', trigger: 'blur' },
        { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
      ],
      region: [
        { required: true, type: 'array', message: '请选择所在地区', trigger: 'change' }
      ],
      address: [
        { required: true, message: '请输入详细地址', trigger: 'blur' },
        { min: 5, max: 100, message: '长度在5到100个字符之间', trigger: 'blur' }
      ],
      applyReason: [
        { required: true, message: '请输入申请理由', trigger: 'blur' },
        { min: 10, max: 500, message: '长度在10到500个字符之间', trigger: 'blur' }
      ]
    }
    
    // 加载省市区数据
    const handleLoadRegionData = async () => {
      regionLoading.value = true
      try {
        const data = await getStaticRegionData()
        cascaderOptions.value = data || regionData
        console.log('省市区数据加载成功，共', cascaderOptions.value.length, '个省级行政区')
      } catch (error) {
        console.error('加载省市区数据出错：', error)
        userMessages.prompt.showRegionDataIncomplete()
        cascaderOptions.value = regionData
      } finally {
        regionLoading.value = false
      }
    }
    
    // 处理地区选择变化
    const handleRegionChange = value => {
      console.log('选择的地区:', value)
      if (value && value.length >= 3) {
        applicationForm.province = value[0]
        applicationForm.city = value[1]
        applicationForm.district = value[2]
      }
    }
    
    // 验证图片上传
    const beforeImageUpload = (file) => {
      // 检查文件类型
      const isImage = file.type === 'image/jpeg' || file.type === 'image/png'
      // 检查文件大小（小于2MB）
      const isLt2M = file.size / 1024 / 1024 < 2
      
      if (!isImage) {
        shopMessages.prompt.showLogoFormatInvalid()
        return false
      }
      
      if (!isLt2M) {
        shopMessages.prompt.showLogoSizeLimit()
        return false
      }
      
      return true
    }
    
    // 上传身份证正面
    const uploadIdCardFront = async ({ file }) => {
      try {
        const result = await uploadImage(file)
        // 显示API响应消息（成功或失败都通过状态码映射显示）
        showByCode(result.code)
        
        // 只有成功时才更新表单
        if (isSuccess(result.code) && result.data && result.data.url) {
          applicationForm.idCardFront = result.data.url
        }
      } catch (error) {
        // 捕获意外的运行时错误（非API业务错误）
        if (process.env.NODE_ENV === 'development') {
          console.error('[开发调试] 上传身份证正面时发生意外错误：', error)
        }
      }
    }
    
    // 上传身份证背面
    const uploadIdCardBack = async ({ file }) => {
      try {
        const result = await uploadImage(file)
        // 显示API响应消息（成功或失败都通过状态码映射显示）
        showByCode(result.code)
        
        // 只有成功时才更新表单
        if (isSuccess(result.code) && result.data && result.data.url) {
          applicationForm.idCardBack = result.data.url
        }
      } catch (error) {
        // 捕获意外的运行时错误（非API业务错误）
        if (process.env.NODE_ENV === 'development') {
          console.error('[开发调试] 上传身份证背面时发生意外错误：', error)
        }
      }
    }
    
    // 上传营业执照
    const uploadBusinessLicense = async ({ file }) => {
      try {
        const result = await uploadImage(file)
        // 显示API响应消息（成功或失败都通过状态码映射显示）
        showByCode(result.code)
        
        // 只有成功时才更新表单
        if (isSuccess(result.code) && result.data && result.data.url) {
          applicationForm.businessLicense = result.data.url
        }
      } catch (error) {
        // 捕获意外的运行时错误（非API业务错误）
        if (process.env.NODE_ENV === 'development') {
          console.error('[开发调试] 上传营业执照时发生意外错误：', error)
        }
      }
    }
    
    // 提交申请
    const submitApplication = () => {
      applicationFormRef.value.validate(async (valid) => {
        if (!valid) {
          shopMessages.prompt.showCertificationIncomplete()
          return
        }
        
        submitting.value = true
        
        try {
          const response = await store.dispatch('user/submitShopCertification', applicationForm)
          
          // 显示API响应消息（成功或失败都通过状态码映射显示）
          showByCode(response.code)
          
          // 只有成功时才执行后续操作
          if (isSuccess(response.code)) {
            // 提交成功后刷新状态
            await fetchCertificationStatus()
            resetForm()
          }
        } catch (error) {
          // 捕获意外的运行时错误（非API业务错误）
          // API业务失败已通过 showByCode 显示，网络错误已在响应拦截器显示
          if (process.env.NODE_ENV === 'development') {
            console.error('[开发调试] 提交认证申请时发生意外错误：', error)
          }
        } finally {
          submitting.value = false
        }
      })
    }
    
    // 重置表单
    const resetForm = () => {
      applicationFormRef.value.resetFields()
      region.value = []
    }
    
    // 获取认证状态
    const fetchCertificationStatus = async () => {
      try {
        const response = await store.dispatch('user/fetchShopCertificationStatus')
        
        // 显示API响应消息（成功或失败都通过状态码映射显示）
        showByCode(response.code)
        
        // 只有成功时才处理数据
        if (isSuccess(response.code)) {
          if (response.data) {
            hasApplied.value = true
            applicationStatus.value = response.data.status
            applicationTime.value = response.data.createTime
            rejectReason.value = response.data.rejectReason || ''
          } else {
            hasApplied.value = false
          }
        } else {
          hasApplied.value = false
        }
      } catch (error) {
        // 捕获意外的运行时错误（非API业务错误）
        // API业务失败已通过 showByCode 显示，网络错误已在响应拦截器显示
        hasApplied.value = false
        if (process.env.NODE_ENV === 'development') {
          console.error('[开发调试] 获取认证状态时发生意外错误：', error)
        }
      }
    }
    
    // 重新申请
    const resetApplication = () => {
      hasApplied.value = false
      resetForm()
    }
    
    // 组件挂载时获取认证状态和地区数据
    onMounted(() => {
      fetchCertificationStatus()
      handleLoadRegionData()
    })
    
    return {
      applicationFormRef,
      applicationForm,
      rules,
      region,
      cascaderOptions,
      regionLoading,
      submitting,
      hasApplied,
      applicationStatus,
      rejectReason,
      applicationTime,
      statusText,
      handleRegionChange,
      submitApplication,
      resetForm,
      beforeImageUpload,
      uploadIdCardFront,
      uploadIdCardBack,
      uploadBusinessLicense,
      resetApplication
    }
  }
}
</script>

<style lang="scss" scoped>
.merchant-application {
  margin: 0 auto;
  
  .section-title {
    margin: 20px 0;
    padding-bottom: 10px;
    border-bottom: 1px solid #eee;
    color: #333;
  }
  
  .application-form {
    max-width: 800px;
  }
  
  .upload-container {
    width: 200px;
    height: 150px;
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    overflow: hidden;
    
    &:hover {
      border-color: #409EFF;
    }
    
    .upload-image {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
    
    .upload-icon {
      font-size: 28px;
      color: #8c939d;
      margin-bottom: 8px;
    }
    
    .upload-text {
      font-size: 12px;
      color: #8c939d;
    }
  }
  
  .status-card {
    margin-top: 20px;
    max-width: 800px;
    
    h3 {
      margin-bottom: 10px;
    }
    
    p {
      margin-bottom: 8px;
      color: #666;
    }
  }
}
</style> 