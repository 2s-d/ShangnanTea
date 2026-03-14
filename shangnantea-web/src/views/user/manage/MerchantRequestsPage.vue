<template>
  <div class="merchant-requests-page">
    <div class="page-header">
      <div class="container">
        <h1 class="page-title">商家认证请求</h1>
        <p class="page-desc">查看并审核用户提交的商家认证申请</p>
      </div>
    </div>

    <div class="container main-content">
      <el-card class="table-card">
        <div class="card-header">
          <div class="header-title">认证申请列表</div>
        </div>

        <el-table
          :data="certList"
          style="width: 100%"
          v-loading="loading"
          border
        >
          <el-table-column prop="id" label="ID" width="80" align="center" />
          <el-table-column prop="userId" label="用户ID" min-width="140" show-overflow-tooltip />
          <el-table-column prop="shopName" label="店铺名称" min-width="160" show-overflow-tooltip />
          <el-table-column prop="createTime" label="提交时间" width="180">
            <template #default="scope">
              {{ formatDate(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="scope">
              <el-button 
                size="small" 
                type="primary" 
                @click="viewDetail(scope.row)"
                :disabled="scope.row.status !== 0">
                查看
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <!-- 审核弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      title="审核商家认证申请"
      width="800px"
      :close-on-click-modal="false">
      <el-form
        ref="auditFormRef"
        :model="auditForm"
        :rules="auditRules"
        label-width="120px"
        v-if="currentCert">
        
        <!-- 认证信息展示 -->
        <el-divider content-position="left">个人信息</el-divider>
        <el-form-item label="用户ID">
          <span>{{ currentCert.userId }}</span>
        </el-form-item>
        <el-form-item label="真实姓名">
          <span>{{ currentCert.realName || '暂无' }}</span>
        </el-form-item>
        <el-form-item label="身份证号">
          <span>{{ currentCert.idCard || '暂无' }}</span>
        </el-form-item>
        <el-form-item label="联系电话">
          <span>{{ currentCert.contactPhone || '暂无' }}</span>
        </el-form-item>

        <el-divider content-position="left">店铺信息</el-divider>
        <el-form-item label="店铺名称">
          <span>{{ currentCert.shopName || '暂无' }}</span>
        </el-form-item>
        <el-form-item label="所在地区">
          <span>{{ getRegionText(currentCert) || '暂无' }}</span>
        </el-form-item>
        <el-form-item label="详细地址">
          <span>{{ currentCert.address || '暂无' }}</span>
        </el-form-item>
        <el-form-item label="申请理由">
          <span>{{ currentCert.applyReason || '暂无' }}</span>
        </el-form-item>

        <el-divider content-position="left">证件照片</el-divider>
        <el-form-item label="身份证正面">
          <SafeImage 
            v-if="currentCert.idCardFront"
            :src="currentCert.idCardFront"
            type="banner"
            alt="身份证正面"
            style="max-width: 300px; max-height: 200px; border: 1px solid #ddd; border-radius: 4px;" />
          <span v-else class="text-muted">暂无</span>
        </el-form-item>
        <el-form-item label="身份证背面">
          <SafeImage 
            v-if="currentCert.idCardBack"
            :src="currentCert.idCardBack"
            type="banner"
            alt="身份证背面"
            style="max-width: 300px; max-height: 200px; border: 1px solid #ddd; border-radius: 4px;" />
          <span v-else class="text-muted">暂无</span>
        </el-form-item>
        <el-form-item label="营业执照">
          <SafeImage 
            v-if="currentCert.businessLicense"
            :src="currentCert.businessLicense"
            type="banner"
            alt="营业执照"
            style="max-width: 300px; max-height: 200px; border: 1px solid #ddd; border-radius: 4px;" />
          <span v-else class="text-muted">暂无</span>
        </el-form-item>

        <el-divider content-position="left">审核操作</el-divider>
        <el-form-item label="审核结果" prop="status">
          <el-radio-group v-model="auditForm.status">
            <el-radio :label="1">通过</el-radio>
            <el-radio :label="2">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item 
          label="拒绝原因" 
          prop="message"
          v-if="auditForm.status === 2">
          <el-input
            v-model="auditForm.message"
            type="textarea"
            :rows="4"
            placeholder="请输入拒绝原因（必填）"
            maxlength="500"
            show-word-limit />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="handleAudit"
            :loading="submitting">
            提交审核
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getCertificationList, processCertification } from '@/api/user'
import { showByCode, isSuccess } from '@/utils/apiMessages'
import SafeImage from '@/components/common/form/SafeImage.vue'

const loading = ref(false)
const certList = ref([])
const dialogVisible = ref(false)
const currentCert = ref(null)
const submitting = ref(false)
const auditFormRef = ref(null)

const auditForm = ref({
  status: null,
  message: ''
})

const auditRules = {
  status: [
    { required: true, message: '请选择审核结果', trigger: 'change' }
  ],
  message: [
    { 
      validator: (rule, value, callback) => {
        if (auditForm.value.status === 2 && (!value || !value.trim())) {
          callback(new Error('拒绝时必须填写拒绝原因'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const loadData = async () => {
  try {
    loading.value = true
    // 只拉取待审核的认证申请（status=0）
    const res = await getCertificationList(0)
    if (isSuccess(res.code)) {
      const data = res.data
      certList.value = Array.isArray(data) ? data : (data?.list || [])
      // 后端已处理图片URL，直接使用
    } else {
      showByCode(res.code)
    }
  } catch (error) {
    console.error('加载认证列表失败:', error)
    ElMessage.error('加载认证列表失败')
  } finally {
    loading.value = false
  }
}

const viewDetail = (row) => {
  if (row.status !== 0) {
    ElMessage.warning('该认证申请已审核，无法再次审核')
    return
  }
  currentCert.value = { ...row }
  auditForm.value = {
    status: null,
    message: ''
  }
  dialogVisible.value = true
}

const getRegionText = (cert) => {
  if (!cert) return ''
  const parts = []
  if (cert.province) parts.push(cert.province)
  if (cert.city) parts.push(cert.city)
  if (cert.district) parts.push(cert.district)
  return parts.length > 0 ? parts.join(' / ') : ''
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

const handleAudit = async () => {
  if (!auditFormRef.value) return
  
  await auditFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    if (!currentCert.value) {
      ElMessage.error('认证信息不存在')
      return
    }
    
    try {
      submitting.value = true
      const res = await processCertification(currentCert.value.id, {
        status: auditForm.value.status,
        message: auditForm.value.status === 2 ? auditForm.value.message : null
      })
      
      if (isSuccess(res.code)) {
        ElMessage.success(auditForm.value.status === 1 ? '审核通过' : '已拒绝该认证申请')
        dialogVisible.value = false
        await loadData()
      } else {
        showByCode(res.code)
      }
    } catch (error) {
      console.error('审核失败:', error)
      ElMessage.error('审核失败，请稍后重试')
    } finally {
      submitting.value = false
    }
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.merchant-requests-page {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.container {
  width: 85%;
  max-width: 1920px;
  margin: 0 auto;
  padding: 0;
}

.page-header {
  background: #fff;
  padding: 30px 0;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.page-title {
  font-size: 26px;
  font-weight: 600;
  margin: 0 0 8px;
}

.page-desc {
  margin: 0;
  color: #909399;
}

.main-content {
  margin-bottom: 40px;
}

.table-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
}

.text-muted {
  color: #909399;
  font-style: italic;
}

:deep(.el-divider__text) {
  font-weight: 600;
  color: #303133;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}
</style>
