<template>
  <div class="address-management">
    <el-card class="box-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>收货地址管理</span>
          <el-button type="primary" @click="handleAddAddress">
            <el-icon><Plus /></el-icon>新增地址
          </el-button>
        </div>
      </template>
      
      <div class="address-list">
        <el-skeleton :rows="3" animated :loading="loading">
          <template v-if="addresses.length">
            <div v-for="address in addressItems" :key="address.id" class="address-item">
              <div class="address-card">
                <div class="address-info">
                  <div class="address-header">
                    <span class="receiver">{{ address.name }}</span>
                    <span class="phone">{{ address.phone }}</span>
                    <el-tag type="success" v-if="address.isDefault">默认</el-tag>
                  </div>
                  <div class="address-detail">
                    {{ address.formattedAddress }}
                  </div>
                </div>
                <div class="address-actions">
                  <template v-if="!address.isDefault">
                    <el-button type="text" @click="handleSetDefaultAddress(address.id)">设为默认</el-button>
                  </template>
                  <el-button type="text" @click="handleEditAddress(address)">编辑</el-button>
                  <el-popconfirm
                    title="确定要删除这个地址吗?"
                    @confirm="handleDeleteAddress(address.id)"
                  >
                    <template #reference>
                      <el-button type="text" class="delete-btn">删除</el-button>
                    </template>
                  </el-popconfirm>
                </div>
              </div>
            </div>
          </template>
          <el-empty v-else description="暂无收货地址，请添加" />
        </el-skeleton>
      </div>
    </el-card>

    <!-- 新增/编辑地址弹窗 -->
    <el-dialog 
      v-model="addressModalVisible" 
      :title="isEditing ? '编辑收货地址' : '新增收货地址'" 
      width="500px"
      destroy-on-close
    >
      <el-form 
        :model="addressForm" 
        :rules="addressRules"
        ref="addressFormRef"
        label-width="80px"
      >
        <el-form-item label="收货人" prop="name">
          <el-input v-model="addressForm.name" placeholder="请输入收货人姓名"/>
        </el-form-item>
        
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="addressForm.phone" placeholder="请输入手机号码"/>
        </el-form-item>
        
        <el-form-item label="所在地区" prop="region">
          <el-cascader 
            v-model="addressForm.region" 
            :options="cascaderOptions" 
            placeholder="请选择省/市/区"
            :props="{
              expandTrigger: 'hover',
              checkStrictly: false
            }"
            :loading="regionLoading"
            @change="handleRegionChange"
          />
        </el-form-item>
        
        <el-form-item label="详细地址" prop="detail">
          <el-input 
            v-model="addressForm.detail" 
            type="textarea" 
            placeholder="街道、门牌号等"
            :rows="3"
          />
        </el-form-item>
        
        <el-form-item label="默认地址" prop="isDefault">
          <el-switch v-model="addressForm.isDefault" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="cancelAddressModal">取消</el-button>
          <el-button type="primary" @click="handleSaveAddress" :loading="confirmLoading">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { Plus } from '@element-plus/icons-vue'
import { ref, onMounted, reactive, toRaw, computed } from 'vue'

import { showByCode, isSuccess } from '@/utils/apiMessages'
import { regionData, getStaticRegionData } from '@/utils/region'
import { useStore } from 'vuex'
import { userPromptMessages as userMessages } from '@/utils/promptMessages'

export default {
  name: 'AddressPage',
  components: {
    Plus
  },
  setup() {
    // 使用Vuex store
    const store = useStore()
    
    // 地址列表数据
    const addresses = ref([])
    const loading = ref(false)
    
    // 地区数据相关
    const cascaderOptions = ref([])
    const regionLoading = ref(false)
    
    // 弹窗控制
    const addressModalVisible = ref(false)
    const isEditing = ref(false)
    const confirmLoading = ref(false)
    const addressFormRef = ref(null)
    
    // 表单数据
    const addressForm = reactive({
      id: '',
      name: '',
      phone: '',
      region: [],
      detail: '',
      isDefault: false
    })
    
    // 表单验证规则
    const addressRules = {
      name: [
        { required: true, message: '请输入收货人姓名', trigger: 'blur' },
        { min: 2, max: 20, message: '收货人姓名长度为2-20个字符', trigger: 'blur' }
      ],
      phone: [
        { required: true, message: '请输入手机号码', trigger: 'blur' },
        { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
      ],
      region: [
        { required: true, type: 'array', message: '请选择所在地区', trigger: 'change' }
      ],
      detail: [
        { required: true, message: '请输入详细地址', trigger: 'blur' },
        { min: 5, max: 100, message: '详细地址长度为5-100个字符', trigger: 'blur' }
      ]
    }
    
    // 获取地址列表 - 使用Vuex
    const handleFetchAddressList = async () => {
      loading.value = true
      try {
        const response = await store.dispatch('user/fetchAddresses')
        
        if (isSuccess(response.code)) {
          addresses.value = store.state.user.addressList || []
        } else {
          showByCode(response.code)
          addresses.value = []
        }
      } catch (error) {
        console.error('获取地址列表失败', error)
        // 当获取失败时，初始化为空数组，确保页面不会崩溃
        addresses.value = []
      } finally {
        loading.value = false
      }
    }
    
    // 新增地址
    const handleAddAddress = () => {
      handleResetAddressForm()
      isEditing.value = false
      addressModalVisible.value = true
    }
    
    // 编辑地址
    const handleEditAddress = address => {
      isEditing.value = true
      const { id, name, phone, province, city, district, detail, isDefault } = address
      addressForm.id = id
      addressForm.name = name
      addressForm.phone = phone
      addressForm.region = [province, city, district]
      addressForm.detail = detail
      addressForm.isDefault = isDefault
      addressModalVisible.value = true
    }
    
    // 删除地址 - 使用Vuex
    const handleDeleteAddress = async id => {
      const response = await store.dispatch('user/deleteAddress', id)
      
      // 显示API响应消息（成功或失败都通过状态码映射显示）
      showByCode(response.code)
      
      // 只有成功时才刷新列表
      if (isSuccess(response.code)) {
        handleFetchAddressList()
      }
    }
    
    // 设置默认地址 - 使用Vuex
    const handleSetDefaultAddress = async id => {
      const response = await store.dispatch('user/setDefaultAddress', id)
      
      // 显示API响应消息（成功或失败都通过状态码映射显示）
      showByCode(response.code)
      
      // 只有成功时才刷新列表
      if (isSuccess(response.code)) {
        handleFetchAddressList()
      }
    }
    
    // 保存地址（新增或更新）- 使用Vuex
    const handleSaveAddress = async () => {
      await addressFormRef.value.validate(async valid => {
        if (!valid) return
        
        confirmLoading.value = true
        
        // 从表单区域值中提取省市区
        const [province, city, district] = addressForm.region
        
        const addressData = {
          ...toRaw(addressForm),
          province,
          city,
          district
        }
        
        delete addressData.region // 删除region字段，后端不需要
        
        try {
          if (isEditing.value) {
            // 更新地址
            const updateResponse = await store.dispatch('user/updateAddress', addressData)
            
            if (isSuccess(updateResponse.code)) {
              showByCode(updateResponse.code)
              addressModalVisible.value = false
              handleFetchAddressList()
            } else {
              showByCode(updateResponse.code)
            }
          } else {
            // 新增地址
            delete addressData.id // 新增时不需要ID
            const addResponse = await store.dispatch('user/addAddress', addressData)
            
            if (isSuccess(addResponse.code)) {
              showByCode(addResponse.code)
              addressModalVisible.value = false
              handleFetchAddressList()
            } else {
              showByCode(addResponse.code)
            }
          }
        } finally {
          confirmLoading.value = false
        }
      })
    }
    
    // 取消弹窗
    const cancelAddressModal = () => {
      addressModalVisible.value = false
    }
    
    // 重置表单
    const handleResetAddressForm = () => {
      addressForm.id = ''
      addressForm.name = ''
      addressForm.phone = ''
      addressForm.region = []
      addressForm.detail = ''
      addressForm.isDefault = false
      if (addressFormRef.value) {
        addressFormRef.value.resetFields()
      }
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
    }
    
    // 添加省市区编码转换功能
    const translateRegion = (province, city, district) => {
      // 检查是否是编码形式
      if (isRegionCode(province)) {
        try {
          // 尝试使用getRegionByName函数找到对应的地区名称
          const regionNames = getStaticRegionData()
          const provinceText = findRegionNameById(regionNames, province) || province
          const cityText = findRegionNameById(provinceText.children || [], city) || city
          let districtText = district
          
          if (cityText.children) {
            const districtObj = cityText.children.find(d => d.value === district)
            if (districtObj) {
              districtText = districtObj.label
            }
          }
          
          return { 
            provinceText: typeof provinceText === 'string' ? provinceText : provinceText.label, 
            cityText: typeof cityText === 'string' ? cityText : cityText.label, 
            districtText: typeof districtText === 'string' ? districtText : districtText.label 
          }
        } catch (error) {
          console.error('地区编码转换错误:', error)
          // 返回原始值
          return { provinceText: province, cityText: city, districtText: district }
        }
      }
      // 若不是编码，直接返回原始值
      return { provinceText: province, cityText: city, districtText: district }
    }
    
    // 根据ID查找地区名称
    const findRegionNameById = (regions, id) => {
      return regions.find(r => r.value === id)
    }
    
    // 检查字符串是否为地区编码格式（数字字符串）
    const isRegionCode = str => {
      return /^\d+$/.test(str)
    }
    
    // 格式化地址为可读形式
    const formatAddress = address => {
      const { province, city, district, detail } = address
      
      // 如果已经是文本形式，直接使用
      if (!isRegionCode(province)) {
        return `${province} ${city} ${district} ${detail}`
      }
      
      // 否则进行编码转换
      const { provinceText, cityText, districtText } = translateRegion(province, city, district)
      return `${provinceText} ${cityText} ${districtText} ${detail}`
    }
    
    // 修改地址显示组件，加入格式化后的结果
    const addressItems = computed(() => {
      return addresses.value.map(address => ({
        ...address,
        formattedAddress: formatAddress(address)
      }))
    })
    
    onMounted(() => {
      handleFetchAddressList()
      handleLoadRegionData()
    })
    
    return {
      addresses,
      loading,
      addressModalVisible,
      isEditing,
      confirmLoading,
      addressForm,
      addressFormRef,
      addressRules,
      cascaderOptions,
      regionLoading,
      handleRegionChange,
      handleFetchAddressList,
      handleAddAddress,
      handleEditAddress,
      handleDeleteAddress,
      handleSetDefaultAddress,
      handleSaveAddress,
      cancelAddressModal,
      addressItems,
      formatAddress
    }
  }
}
</script>

<style scoped>
.address-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.address-list {
  margin-top: 20px;
}

.address-item {
  margin-bottom: 16px;
}

.address-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border: 1px solid #f0f0f0;
  border-radius: 4px;
  transition: all 0.3s;
}

.address-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.09);
}

.address-info {
  flex: 1;
}

.address-header {
  margin-bottom: 8px;
  display: flex;
  align-items: center;
}

.receiver {
  font-weight: bold;
  font-size: 16px;
  margin-right: 12px;
}

.phone {
  color: #666;
  margin-right: 12px;
}

.address-detail {
  color: #666;
  line-height: 1.5;
}

.address-actions {
  display: flex;
  align-items: center;
}

.address-actions .el-button {
  margin-left: 8px;
}

.delete-btn {
  color: #f56c6c;
}

@media (max-width: 768px) {
  .address-card {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .address-actions {
    margin-top: 12px;
    width: 100%;
    justify-content: flex-end;
  }
}
</style> 