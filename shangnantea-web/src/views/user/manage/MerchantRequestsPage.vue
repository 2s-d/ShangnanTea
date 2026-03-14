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
          <el-table-column prop="username" label="用户名" min-width="140" show-overflow-tooltip />
          <el-table-column prop="shopName" label="店铺名称" min-width="160" show-overflow-tooltip />
          <el-table-column prop="statusText" label="状态" width="120" align="center" />
          <el-table-column prop="createTime" label="提交时间" width="180" />
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="scope">
              <el-button size="small" type="primary" @click="viewDetail(scope.row)">
                查看
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getCertificationList } from '@/api/user'

const loading = ref(false)
const certList = ref([])

const loadData = async () => {
  try {
    loading.value = true
    const res = await getCertificationList()
    certList.value = Array.isArray(res.data) ? res.data : (res.data?.list || [])
  } finally {
    loading.value = false
  }
}

const viewDetail = row => {
  // TODO: 后续在这里打开审核弹窗：查看资料、通过/拒绝等
  console.log('cert detail', row)
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
</style>

