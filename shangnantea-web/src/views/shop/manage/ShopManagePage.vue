<template>
  <div class="shop-manage-page">
    <div class="shop-manage-container">
      <!-- 页面标题 -->
      <div class="page-header">
        <div class="header-title">
          <h1>店铺管理</h1>
          <p class="sub-title">管理您的店铺信息和商品</p>
        </div>
      </div>

      <!-- 主要内容 -->
      <div class="page-content">
        <!-- 店铺信息卡片 -->
        <el-card class="shop-info-card" v-loading="loading">
          <template #header>
            <div class="card-header">
              <span>店铺信息</span>
              <el-button type="primary" size="small" @click="showSettingsDialog">
                <el-icon><Setting /></el-icon> 店铺设置
              </el-button>
            </div>
          </template>
          
          <div class="shop-info-content" v-if="shop">
            <div class="shop-info-left">
              <div class="shop-avatar">
                <SafeImage :src="shop.logo" type="banner" :alt="shop.shop_name" style="width:80px;height:80px;border-radius:50%;object-fit:cover;" />
              </div>
            </div>
            
            <div class="shop-info-right">
              <h3 class="shop-name">{{ shop.shop_name }}</h3>
              
              <div class="shop-stats">
                <div class="stat-item">
                  <span class="stat-value">{{ shop.sales_count || 0 }}</span>
                  <span class="stat-label">销量</span>
                </div>
                <div class="stat-item">
                  <span class="stat-value">{{ shop.rating || '0.0' }}</span>
                  <span class="stat-label">评分</span>
                </div>
                <div class="stat-item">
                  <span class="stat-value">{{ shop.follow_count || 0 }}</span>
                  <span class="stat-label">关注数</span>
                </div>
              </div>
              
              <div class="shop-description">
                <span class="label">简介：</span>
                <span>{{ shop.description || '暂无简介' }}</span>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 茶叶管理部分 -->
        <el-card class="tea-manage-card">
          <template #header>
            <div class="card-header">
              <span>茶叶管理</span>
              <el-button type="primary" size="small" @click="showAddTeaDialog">
                <el-icon><Plus /></el-icon> 添加茶叶
              </el-button>
            </div>
          </template>
          
          <div class="tea-manage-content">
            <!-- 搜索和筛选 -->
            <div class="search-filter-container">
              <div class="search-box">
                <el-input
                  v-model="searchQuery"
                  placeholder="搜索茶叶名称"
                  clearable
                  @keyup.enter="handleSearch"
                >
                  <template #prefix>
                    <el-icon><Search /></el-icon>
                  </template>
                  <template #append>
                    <el-button @click="handleSearch">搜索</el-button>
                  </template>
                </el-input>
              </div>
              
              <div class="filter-box">
                <el-select v-model="statusFilter" placeholder="状态筛选" @change="handleFilterChange">
                  <el-option label="全部" value="" />
                  <el-option label="已上架" value="1" />
                  <el-option label="已下架" value="0" />
                </el-select>
                
                <el-select v-model="categoryFilter" placeholder="分类筛选" @change="handleFilterChange">
                  <el-option label="全部分类" value="" />
                  <el-option
                    v-for="category in categoryOptions"
                    :key="category.id"
                    :label="category.name"
                    :value="category.id"
                  />
                </el-select>
              </div>
            </div>
            
            <!-- 茶叶列表 -->
            <div class="tea-list-container">
              <!-- 茶叶表格 -->
              <el-table
                v-loading="teaLoading"
                :data="shopTeas"
                border
                stripe
                style="width: 100%"
              >
                <el-table-column label="图片" width="100">
                  <template #default="scope">
                    <SafeImage 
                      :src="scope.row.main_image" 
                      type="tea"
                      :alt="scope.row.name"
                      style="width: 60px; height: 60px; border-radius: 4px; object-fit: cover;"
                    />
                  </template>
                </el-table-column>
                <el-table-column prop="id" label="茶叶ID" width="120" />
                <el-table-column prop="name" label="茶叶名称" min-width="180" show-overflow-tooltip />
                <el-table-column label="价格" width="120">
                  <template #default="scope">
                    <div>
                      <span class="price">¥{{ getMinPrice(scope.row) }}</span>
                      <span v-if="scope.row.specifications && scope.row.specifications.length > 1" class="price-tip">起</span>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="分类" width="120">
                  <template #default="scope">
                    {{ getCategoryName(scope.row.category_id) }}
                  </template>
                </el-table-column>
                <el-table-column prop="stock" label="库存" width="100" sortable />
                <el-table-column prop="sales" label="销量" width="100" sortable />
                <el-table-column label="状态" width="100">
                  <template #default="scope">
                    <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
                      {{ scope.row.status === 1 ? '已上架' : '已下架' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" fixed="right" width="200">
                  <template #default="scope">
                    <el-button 
                      type="primary" 
                      link 
                      @click="handleEdit(scope.row)"
                    >
                      编辑
                    </el-button>
                    <el-button 
                      :type="scope.row.status === 1 ? 'danger' : 'success'" 
                      link 
                      @click="handleToggleStatus(scope.row)"
                    >
                      {{ scope.row.status === 1 ? '下架' : '上架' }}
                    </el-button>
                    <el-button 
                      type="danger" 
                      link 
                      @click="handleDelete(scope.row)"
                    >
                      删除
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
              
              <!-- 分页 -->
              <div class="pagination-container">
                <el-pagination
                  background
                  layout="total, sizes, prev, pager, next, jumper"
                  :total="totalCount"
                  :page-size="pageSize"
                  :page-sizes="[10, 20, 50]"
                  :current-page="currentPage"
                  @size-change="handleSizeChange"
                  @current-change="handlePageChange"
                />
              </div>
            </div>
          </div>
        </el-card>
      </div>
    </div>
    
    <!-- 茶叶表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑茶叶' : '添加茶叶'"
      width="800px"
      :before-close="handleDialogClose"
      destroy-on-close
    >
      <el-form 
        ref="teaFormRef" 
        :model="currentTea" 
        label-width="100px" 
        v-if="currentTea"
        class="tea-form"
      >
        <!-- 基本信息 -->
        <el-divider content-position="left">基本信息</el-divider>
        
        <el-form-item label="茶叶名称" prop="name">
          <el-input v-model="currentTea.name" placeholder="请输入茶叶名称" maxlength="100" show-word-limit />
        </el-form-item>
        
        <el-form-item label="茶叶分类" prop="category_id">
          <el-select v-model="currentTea.category_id" placeholder="请选择茶叶分类" style="width: 100%">
            <el-option
              v-for="category in categoryOptions"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="产地" prop="origin">
          <el-input v-model="currentTea.origin" placeholder="请输入产地" />
        </el-form-item>
        
        <el-form-item label="简介" prop="description">
          <el-input
            v-model="currentTea.description"
            type="textarea"
            :rows="2"
            placeholder="请输入简短介绍，支持基本HTML标签"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        
        <!-- 规格与价格 -->
        <el-divider content-position="left">规格与价格</el-divider>
        
        <div class="specs-container">
          <div class="specs-header">
            <div class="specs-title">规格列表</div>
            <el-button type="primary" size="small" @click="addSpec">
              添加规格
            </el-button>
          </div>
          
          <el-table :data="currentTea.specifications" border class="specs-table">
            <el-table-column prop="spec_name" label="规格名称" width="180">
              <template #default="scope">
                <el-input v-model="scope.row.spec_name" placeholder="如：罐装100g" />
              </template>
            </el-table-column>
            
            <el-table-column prop="price" label="价格" width="120">
              <template #default="scope">
                <el-input-number v-model="scope.row.price" :min="0" :precision="2" :step="10" />
              </template>
            </el-table-column>
            
            <el-table-column prop="stock" label="库存" width="120">
              <template #default="scope">
                <el-input-number v-model="scope.row.stock" :min="0" :precision="0" :step="10" />
              </template>
            </el-table-column>
            
            <el-table-column prop="is_default" label="默认规格" width="100">
              <template #default="scope">
                <el-checkbox v-model="scope.row.is_default" @change="handleDefaultChange(scope.$index)" />
              </template>
            </el-table-column>
            
            <el-table-column label="操作" width="80">
              <template #default="scope">
                <el-button 
                  type="danger" 
                  link
                  @click="removeSpec(scope.$index)"
                  :disabled="currentTea.specifications.length <= 1"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
        
        <el-form-item label="上架状态" prop="status">
          <el-switch
            v-model="currentTea.status"
            :active-value="1"
            :inactive-value="0"
            active-text="上架"
            inactive-text="下架"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSave" :loading="submitting">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Setting, Plus, Search } from '@element-plus/icons-vue'
import SafeImage from '@/components/common/form/SafeImage.vue'

export default {
  name: 'ShopManagePage',
  components: {
    Setting,
    Plus,
    Search,
    SafeImage
  },
  setup() {
    const router = useRouter()
    
    // 店铺信息相关
    const loading = ref(false)
    const shop = ref(null)
    const defaultLogo = '/mock-images/shop-default.jpg'
    const defaultTeaImage = '/mock-images/tea-default.jpg'
    
    // 茶叶列表相关
    const teaLoading = ref(false)
    const searchQuery = ref('')
    const statusFilter = ref('')
    const categoryFilter = ref('')
    const currentPage = ref(1)
    const pageSize = ref(10)
    const totalCount = ref(0)
    const shopTeas = ref([])
    
    // 对话框相关状态
    const dialogVisible = ref(false)
    const isEdit = ref(false)
    const currentTea = ref(null)
    const submitting = ref(false)
    const teaFormRef = ref(null)
    
    // 分类数据
    const categoryOptions = [
      { id: 1, name: '绿茶' },
      { id: 2, name: '红茶' },
      { id: 3, name: '黑茶' },
      { id: 4, name: '白茶' },
      { id: 5, name: '黄茶' },
      { id: 6, name: '青茶' },
      { id: 7, name: '花茶' }
    ]
    
    // TODO-SCRIPT: 店铺管理需要后端接口与 Vuex shop 模块；移除 UI-DEV 模拟流程
    // 加载店铺信息
    const loadShopInfo = () => {
      loading.value = true
      
      // 模拟API调用延迟
      setTimeout(() => {
        // 模拟店铺数据
        shop.value = {
          id: 'shop100001',
          owner_id: 'cy100003',
          shop_name: '商南茶叶专营店',
          logo: '/mock-images/shop-logo.png',
          banner: '/mock-images/shop-banner.png',
          description: '提供优质商南特产茶叶',
          announcement: '本店所有茶叶均产自商南县高山茶园，保证品质。',
          contact_phone: '13800000003',
          province: '陕西省',
          city: '商洛市',
          district: '商南县',
          address: '城关街道茶叶市场12号',
          status: 1,
          rating: 4.8,
          rating_count: 24,
          sales_count: 168,
          follow_count: 56,
          create_time: '2025-03-26 09:03:26',
          update_time: '2025-03-26 09:03:26'
        }
        
        loading.value = false
      }, 800)
    }
    
    // 加载茶叶列表
    const loadShopTeas = () => {
      teaLoading.value = true
      
      // 模拟API调用延迟
      setTimeout(() => {
        // 生成模拟数据
        const mockTeas = generateMockTeas()
        
        // 应用筛选
        let filteredTeas = [...mockTeas]
        
        // 状态筛选
        if (statusFilter.value !== '') {
          filteredTeas = filteredTeas.filter(tea => tea.status.toString() === statusFilter.value)
        }
        
        // 分类筛选
        if (categoryFilter.value !== '') {
          filteredTeas = filteredTeas.filter(tea => tea.category_id === parseInt(categoryFilter.value))
        }
        
        // 关键词搜索
        if (searchQuery.value) {
          const keyword = searchQuery.value.toLowerCase()
          filteredTeas = filteredTeas.filter(tea => 
            tea.name.toLowerCase().includes(keyword) || 
            tea.id.toLowerCase().includes(keyword)
          )
        }
        
        // 计算总数
        totalCount.value = filteredTeas.length
        
        // 分页
        const start = (currentPage.value - 1) * pageSize.value
        const end = start + pageSize.value
        shopTeas.value = filteredTeas.slice(start, end)
        
        teaLoading.value = false
      }, 600)
    }
    
    // 生成模拟茶叶数据
    const generateMockTeas = () => {
      const teas = []
      const shopId = shop.value ? shop.value.id : 'shop100001'
      
      // 生成10-20种茶叶
      const teaCount = 15
      
      for (let i = 1; i <= teaCount; i++) {
        const categoryId = Math.floor(Math.random() * 7) + 1
        const basePrice = Math.floor(Math.random() * 300) + 100
        const status = Math.random() > 0.3 ? 1 : 0 // 70%概率上架
        const totalStock = Math.floor(Math.random() * 200) + 50
        
        // 生成规格
        const specCount = Math.floor(Math.random() * 2) + 1
        const specifications = []
        
        for (let j = 1; j <= specCount; j++) {
          const isDefault = j === 1
          const specPrice = j === 1 ? basePrice : Math.floor(basePrice * (1 + j * 0.2))
          const specStock = Math.floor(totalStock / specCount)
          
          specifications.push({
            id: i * 10 + j,
            spec_name: j === 1 ? '默认规格' : `加量${j}0%装`,
            price: specPrice,
            stock: specStock,
            is_default: isDefault
          })
        }
        
        const teaId = `tea${shopId.substring(4)}${i.toString().padStart(2, '0')}`
        
        teas.push({
          id: teaId,
          name: `商南${getCategoryName(categoryId)} - ${i}号`,
          shop_id: shopId,
          category_id: categoryId,
          price: basePrice,
          description: `商南${getCategoryName(categoryId)}，高山茶园精制而成，香气高雅持久，滋味醇厚。`,
          detail: `产于商南县，特点：条索紧结，香气高雅，滋味醇厚回甘。冲泡方法：水温80-85℃，投茶量3-5克，冲泡时间30秒至1分钟。`,
          origin: '商南县',
          stock: totalStock,
          sales: Math.floor(Math.random() * 100),
          main_image: `/mock-images/tea-${(i % 8) + 1}.jpg`,
          status: status,
          create_time: new Date(Date.now() - Math.floor(Math.random() * 10000000000)).toISOString(),
          update_time: new Date(Date.now() - Math.floor(Math.random() * 1000000000)).toISOString(),
          specifications,
          images: [
            { id: i * 10 + 1, url: `/mock-images/tea-${(i % 8) + 1}.jpg`, is_main: true },
            { id: i * 10 + 2, url: `/mock-images/tea-detail-${(i % 4) + 1}.jpg`, is_main: false }
          ]
        })
      }
      
      return teas
    }
    
    // 处理编辑
    const handleEdit = (tea) => {
      isEdit.value = true
      currentTea.value = JSON.parse(JSON.stringify(tea)) // 深拷贝避免直接修改列表数据
      dialogVisible.value = true
    }
    
    // 处理状态切换
    const handleToggleStatus = (tea) => {
      const action = tea.status === 1 ? '下架' : '上架'
      const newStatus = tea.status === 1 ? 0 : 1
      
      ElMessageBox.confirm(
        `确定要${action}茶叶"${tea.name}"吗？`,
        `${action}确认`,
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(() => {
        // 模拟API调用
        teaLoading.value = true
        setTimeout(() => {
          // 更新本地数据
          tea.status = newStatus
          
          // 提示成功
          ElMessage.success(`${action}成功`)
          teaLoading.value = false
        }, 500)
      }).catch(() => {
        // 用户取消操作
      })
    }
    
    // 处理删除
    const handleDelete = (tea) => {
      ElMessageBox.confirm(
        `确定要删除茶叶"${tea.name}"吗？此操作不可恢复！`,
        `删除确认`,
        {
          confirmButtonText: '确定删除',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(() => {
        // 模拟API调用
        teaLoading.value = true
        setTimeout(() => {
          // 从列表中删除
          const index = shopTeas.value.findIndex(t => t.id === tea.id)
          if (index !== -1) {
            shopTeas.value.splice(index, 1)
          }
          
          // 提示成功
          ElMessage.success('删除成功')
          teaLoading.value = false
          
          // 如果当前页没有数据了，回到前一页
          if (shopTeas.value.length === 0 && currentPage.value > 1) {
            currentPage.value--
            loadShopTeas()
          }
        }, 500)
      }).catch(() => {
        // 用户取消操作
      })
    }
    
    // 对话框关闭
    const handleDialogClose = (done) => {
      if (submitting.value) {
        ElMessage.warning('正在提交数据，请稍候...')
        return
      }
      done()
    }
    
    // 保存处理
    const handleSave = () => {
      submitting.value = true
      
      setTimeout(() => {
        if (isEdit.value) {
          // 更新列表中的数据
          const index = shopTeas.value.findIndex(t => t.id === currentTea.value.id)
          if (index !== -1) {
            shopTeas.value[index] = currentTea.value
          }
          ElMessage.success('茶叶更新成功')
        } else {
          // 添加新茶叶
          currentTea.value.id = `tea${shop.value.id.substring(4)}${Date.now().toString().substring(8, 13)}`
          currentTea.value.shop_id = shop.value.id
          currentTea.value.create_time = new Date().toISOString()
          currentTea.value.update_time = new Date().toISOString()
          
          // 添加到列表首位
          shopTeas.value.unshift(currentTea.value)
          
          ElMessage.success('茶叶添加成功')
        }
        
        // 关闭对话框
        dialogVisible.value = false
        submitting.value = false
      }, 800)
    }
    
    // 显示添加茶叶对话框
    const showAddTeaDialog = () => {
      isEdit.value = false
      currentTea.value = {
        name: '',
        shop_id: shop.value ? shop.value.id : '',
        category_id: '',
        price: 0,
        description: '',
        detail: '',
        origin: '商南县',
        stock: 0,
        sales: 0,
        main_image: '',
        status: 0, // 默认下架状态
        specifications: [
          {
            id: Date.now(),
            spec_name: '默认规格',
            price: 0,
            stock: 0,
            is_default: true
          }
        ],
        images: []
      }
      dialogVisible.value = true
    }
    
    // 显示设置对话框
    const showSettingsDialog = () => {
      ElMessageBox.alert('店铺设置功能将在后续版本开发', '开发中', {
        confirmButtonText: '确定'
      })
    }
    
    // 获取最低价格
    const getMinPrice = (tea) => {
      if (!tea.specifications || tea.specifications.length === 0) {
        return tea.price
      }
      
      // 从规格中找出最低价格
      const prices = tea.specifications.map(spec => spec.price)
      return Math.min(...prices)
    }
    
    // 获取分类名称
    const getCategoryName = (categoryId) => {
      if (!categoryId) return '-'
      const category = categoryOptions.find(c => c.id === parseInt(categoryId))
      return category ? category.name : '-'
    }
    
    // 搜索处理
    const handleSearch = () => {
      currentPage.value = 1
      loadShopTeas()
    }
    
    // 筛选变更处理
    const handleFilterChange = () => {
      currentPage.value = 1
      loadShopTeas()
    }
    
    // 页码变更
    const handlePageChange = (page) => {
      currentPage.value = page
      loadShopTeas()
    }
    
    // 每页条数变更
    const handleSizeChange = (size) => {
      pageSize.value = size
      currentPage.value = 1
      loadShopTeas()
    }
    
    // 添加规格
    const addSpec = () => {
      if (!currentTea.value) return
      
      currentTea.value.specifications.push({
        id: Date.now(),
        spec_name: `规格${currentTea.value.specifications.length + 1}`,
        price: currentTea.value.price || 0,
        stock: 0,
        is_default: false
      })
    }
    
    // 移除规格
    const removeSpec = (index) => {
      if (!currentTea.value || currentTea.value.specifications.length <= 1) return
      
      currentTea.value.specifications.splice(index, 1)
      
      // 如果删除了默认规格，则将第一个规格设为默认
      if (!currentTea.value.specifications.some(spec => spec.is_default)) {
        currentTea.value.specifications[0].is_default = true
      }
    }
    
    // 处理默认规格变更
    const handleDefaultChange = (changedIndex) => {
      if (!currentTea.value) return
      
      // 只能有一个默认规格，将其他规格设为非默认
      currentTea.value.specifications.forEach((spec, index) => {
        if (index !== changedIndex) {
          spec.is_default = false
        }
      })
    }
    
    onMounted(() => {
      loadShopInfo()
      loadShopTeas()
    })
    
    return {
      // 店铺相关
      loading,
      shop,
      defaultLogo,
      defaultTeaImage,
      showSettingsDialog,
      
      // 茶叶列表相关
      teaLoading,
      searchQuery,
      statusFilter,
      categoryFilter,
      currentPage,
      pageSize,
      totalCount,
      shopTeas,
      categoryOptions,
      getCategoryName,
      getMinPrice,
      showAddTeaDialog,
      handleSearch,
      handleFilterChange,
      handlePageChange,
      handleSizeChange,
      
      // 对话框相关
      dialogVisible,
      isEdit,
      currentTea,
      submitting,
      teaFormRef,
      handleEdit,
      handleToggleStatus,
      handleDelete,
      handleDialogClose,
      handleSave,
      addSpec,
      removeSpec,
      handleDefaultChange
    }
  }
}
</script>

<style lang="scss" scoped>
.shop-manage-page {
  min-height: 100vh;
  background-color: #f5f7fa;
  padding: 20px 0 40px;
  
  .shop-manage-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 15px;
  }
  
  .page-header {
    margin-bottom: 20px;
    
    .header-title {
      h1 {
        font-size: 24px;
        margin: 0 0 8px 0;
        color: var(--el-text-color-primary);
      }
      
      .sub-title {
        font-size: 14px;
        color: var(--el-text-color-secondary);
        margin: 0;
      }
    }
  }
  
  .page-content {
    display: flex;
    flex-direction: column;
    gap: 20px;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      span {
        font-size: 16px;
        font-weight: 600;
      }
    }
    
    .shop-info-card {
      margin-bottom: 20px;
      
      .shop-info-content {
        display: flex;
        
        .shop-info-left {
          margin-right: 20px;
        }
        
        .shop-info-right {
          flex: 1;
          
          .shop-name {
            margin-top: 0;
            margin-bottom: 16px;
            font-size: 18px;
          }
          
          .shop-stats {
            display: flex;
            margin-bottom: 16px;
            
            .stat-item {
              display: flex;
              flex-direction: column;
              margin-right: 32px;
              
              .stat-value {
                font-size: 20px;
                font-weight: bold;
                color: var(--el-color-primary);
              }
              
              .stat-label {
                font-size: 12px;
                color: var(--el-text-color-secondary);
              }
            }
          }
          
          .shop-description {
            font-size: 14px;
            color: var(--el-text-color-regular);
            
            .label {
              color: var(--el-text-color-secondary);
            }
          }
        }
      }
    }
    
    .tea-manage-card {
      .search-filter-container {
        display: flex;
        justify-content: space-between;
        margin-bottom: 20px;
        
        .search-box {
          width: 360px;
        }
        
        .filter-box {
          display: flex;
          gap: 12px;
          
          .el-select {
            width: 140px;
          }
        }
      }
      
      .tea-list-container {
        .price {
          color: var(--el-color-danger);
          font-weight: bold;
        }
        
        .price-tip {
          font-size: 12px;
          color: var(--el-text-color-secondary);
          margin-left: 2px;
        }
        
        .pagination-container {
          margin-top: 20px;
          display: flex;
          justify-content: center;
        }
      }
    }
  }
  
  // 茶叶表单样式
  .tea-form {
    .specs-container {
      margin-bottom: 20px;
      
      .specs-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 10px;
        
        .specs-title {
          font-size: 14px;
          font-weight: 500;
        }
      }
      
      .specs-table {
        margin-bottom: 10px;
      }
    }
  }
}

@media (max-width: 768px) {
  .shop-manage-page {
    .page-content {
      .shop-info-card {
        .shop-info-content {
          flex-direction: column;
          
          .shop-info-left {
            margin-right: 0;
            margin-bottom: 20px;
            display: flex;
            justify-content: center;
          }
          
          .shop-info-right {
            .shop-name {
              text-align: center;
            }
            
            .shop-stats {
              justify-content: center;
            }
          }
        }
      }
      
      .tea-manage-card {
        .search-filter-container {
          flex-direction: column;
          
          .search-box {
            width: 100%;
            margin-bottom: 12px;
          }
          
          .filter-box {
            width: 100%;
            justify-content: space-between;
            
            .el-select {
              width: 48%;
            }
          }
        }
      }
    }
  }
}
</style> 