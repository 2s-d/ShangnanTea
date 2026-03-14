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
              <div class="header-actions">
                <el-button type="primary" size="small" @click="goToShopSettings">
                <el-icon><Setting /></el-icon> 店铺设置
              </el-button>
                <el-button type="default" size="small" @click="handlePreviewShop">
                  <el-icon><View /></el-icon> 预览店铺
                </el-button>
              </div>
            </div>
          </template>
          
          <div class="shop-info-content" v-if="shop">
            <div class="shop-info-left">
              <div class="shop-avatar">
                <el-upload
                  class="logo-uploader"
                  :show-file-list="false"
                  :on-success="handleLogoSuccess"
                  :before-upload="beforeLogoUpload"
                  :http-request="handleLogoUploadRequest"
                >
                  <SafeImage 
                    v-if="shop.logo" 
                    :src="shop.logo" 
                    type="banner" 
                    :alt="shop.name" 
                    class="shop-logo"
                  />
                  <el-icon v-else class="logo-uploader-icon"><Plus /></el-icon>
                </el-upload>
                <div class="logo-tip">点击上传Logo</div>
              </div>
            </div>
            
            <div class="shop-info-right">
              <h3 class="shop-name">{{ shop.name }}</h3>
              
              <div class="shop-stats">
                <div class="stat-item">
                  <span class="stat-value">{{ shop.sales_count || shop.salesCount || 0 }}</span>
                  <span class="stat-label">销量</span>
                </div>
                <div class="stat-item">
                  <span class="stat-value">{{ shop.followCount || 0 }}</span>
                  <span class="stat-label">关注数</span>
                </div>
                <div class="stat-item">
                  <span class="stat-value">
                    {{ (overviewStats.rating ?? 0).toFixed(1) }}
                  </span>
                  <span class="stat-label">评分</span>
                </div>
              </div>
              
              <div class="shop-description">
                <span class="label">简介：</span>
                <span>{{ shop.desc || shop.description || '暂无简介' }}</span>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 任务组D：统计数据展示 -->
        <el-card class="statistics-card" v-loading="statisticsLoading">
          <template #header>
            <div class="card-header">
              <span>店铺统计</span>
              <el-date-picker
                v-model="dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                @change="handleDateRangeChange"
                size="small"
              />
            </div>
          </template>
          
          <div v-if="shopStatistics">
            <!-- 概览数据 -->
            <div class="statistics-overview">
              <div class="overview-item">
                <div class="overview-label">总销售额</div>
                <div class="overview-value">¥{{ formatMoney(shopStatistics.overview?.totalSales || 0) }}</div>
              </div>
              <div class="overview-item">
                <div class="overview-label">订单数</div>
                <div class="overview-value">{{ shopStatistics.overview?.totalOrders || 0 }}</div>
              </div>
              <div class="overview-item">
                <div class="overview-label">商品数</div>
                <div class="overview-value">{{ shopStatistics.overview?.totalProducts || 0 }}</div>
              </div>
              <div class="overview-item">
                <div class="overview-label">评分人数</div>
                <div class="overview-value">{{ shopStatistics.overview?.totalVisitors || 0 }}</div>
              </div>
            </div>
            
            <!-- 销售趋势 -->
          <div class="statistics-trend" v-if="shopStatistics.trends && shopStatistics.trends.length > 0">
              <h4 class="section-title">销售趋势（最近7天）</h4>
            <el-table :data="shopStatistics.trends" border style="width: 100%">
                <el-table-column prop="date" label="日期" width="120" />
                <el-table-column prop="sales" label="销售额" width="120">
                  <template #default="scope">
                    ¥{{ formatMoney(scope.row.sales) }}
                  </template>
                </el-table-column>
                <el-table-column prop="orders" label="订单数" width="100" />
                <el-table-column prop="visitors" label="访问量" width="100" />
              </el-table>
            </div>
            
            <!-- 热门商品 -->
            <div class="statistics-hot-products" v-if="shopStatistics.hotProducts && shopStatistics.hotProducts.length > 0">
              <h4 class="section-title">热门商品（Top 5）</h4>
              <el-table :data="shopStatistics.hotProducts" border style="width: 100%">
                <el-table-column type="index" label="排名" width="60" />
                <el-table-column prop="name" label="商品名称" />
                <el-table-column prop="sales" label="销量" width="100" />
                <el-table-column prop="revenue" label="销售额" width="120">
                  <template #default="scope">
                    ¥{{ formatMoney(scope.row.revenue) }}
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
          
          <el-empty v-else description="暂无统计数据" />
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
                      :src="scope.row.mainImage" 
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
    
    <!-- 添加/编辑茶叶对话框 -->
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
        :rules="teaRules" 
        label-width="100px" 
        v-if="currentTea"
        class="tea-form"
      >
        <!-- 基本信息 -->
        <el-divider content-position="left">基本信息</el-divider>
        
        <el-form-item label="茶叶名称" prop="name">
          <el-input v-model="currentTea.name" placeholder="请输入茶叶名称" maxlength="100" show-word-limit />
        </el-form-item>
        
        <el-form-item label="茶叶分类" prop="categoryId">
          <el-select v-model="currentTea.categoryId" placeholder="请选择茶叶分类" style="width: 100%">
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
        
        <el-form-item label="详细介绍" prop="detail">
          <el-input
            v-model="currentTea.detail"
            type="textarea"
            :rows="5"
            placeholder="请输入详细介绍，支持HTML格式"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>
        
        <!-- 规格与价格 -->
        <el-divider content-position="left">规格与价格</el-divider>
        
        <div class="specs-container">
          <div class="specs-header">
            <div class="specs-title">规格设置</div>
            <el-button type="primary" link @click="addSpec">
              <el-icon><Plus /></el-icon> 添加规格
            </el-button>
          </div>
          
          <el-table :data="currentTea.specifications" class="specs-table" border>
            <el-table-column label="规格名称" min-width="180">
              <template #default="scope">
                <el-input
                  v-model="scope.row.specName"
                  placeholder="如：特级 50g 罐装"
                  maxlength="50"
                />
              </template>
            </el-table-column>
            
            <el-table-column label="价格(元)" width="120">
              <template #default="scope">
                <el-input-number
                  v-model="scope.row.price"
                  :min="0"
                  :precision="2"
                  :step="10"
                  controls-position="right"
                  style="width: 100%"
                />
              </template>
            </el-table-column>
            
            <el-table-column label="库存" width="120">
              <template #default="scope">
                <el-input-number
                  v-model="scope.row.stock"
                  :min="0"
                  :precision="0"
                  :step="10"
                  controls-position="right"
                  style="width: 100%"
                />
              </template>
            </el-table-column>
            
            <el-table-column label="默认规格" width="100">
              <template #default="scope">
                <el-checkbox
                  v-model="scope.row.isDefault"
                  @change="(val) => handleDefaultChange(val, scope.$index)"
                />
              </template>
            </el-table-column>
            
            <el-table-column label="操作" width="100">
              <template #default="scope">
                <el-button
                  type="danger"
                  link
                  :disabled="currentTea.specifications.length <= 1"
                  @click="removeSpec(scope.$index)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
        
        <!-- 图片上传 -->
        <el-divider content-position="left">图片上传</el-divider>
        
        <el-form-item label="商品图片" prop="images">
          <div class="image-uploader">
            <el-upload
              action="#"
              list-type="picture-card"
              :file-list="teaImages"
              :on-change="handleImageChange"
              :on-remove="handleImageRemove"
              :on-preview="handleImagePreview"
              :http-request="handleImageUpload"
              multiple
              :limit="5"
            >
              <el-icon><Plus /></el-icon>
              <template #tip>
                <div class="el-upload__tip">
                  支持jpg/png/gif格式，单张不超过2MB，最多上传5张
                </div>
              </template>
            </el-upload>
            
            <el-dialog v-model="previewVisible" title="图片预览">
              <SafeImage :src="previewImage" type="tea" alt="预览图片" style="width: 100%" class="tea-preview" />
            </el-dialog>
          </div>
        </el-form-item>
        
        <el-form-item label="主图设置" v-if="validImages.length > 0">
          <el-select 
            v-model="mainImageIndex" 
            placeholder="请选择主图" 
            style="width: 100%"
          >
            <el-option
              v-for="(image, index) in validImages"
              :key="image.uid || index"
              :label="`图片${index + 1}`"
              :value="index"
            >
              <div class="main-image-option" style="display: flex; align-items: center;">
                <SafeImage
                  :src="image.url"
                  type="tea"
                  alt="茶叶图片"
                  style="width: 40px; height: 40px; margin-right: 10px; object-fit: cover;"
                />
                <span>图片{{ index + 1 }}{{ index === mainImageIndex ? ' (当前主图)' : '' }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="主图设置" v-else>
          <el-text type="info">请先上传图片</el-text>
        </el-form-item>
        
        <!-- 上架状态 -->
        <el-divider content-position="left">上架状态</el-divider>
        
        <el-form-item label="上架状态">
          <el-switch
            v-model="teaStatus"
            :active-value="1"
            :inactive-value="0"
            active-text="上架"
            inactive-text="下架"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSave" :loading="submitting">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useShopStore } from '@/stores/shop'
import { useTeaStore } from '@/stores/tea'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Setting, Plus, Search, View } from '@element-plus/icons-vue'
import SafeImage from '@/components/common/form/SafeImage.vue'
import { API } from '@/api/apiConstants'

import { showByCode, isSuccess } from '@/utils/apiMessages'
import { shopPromptMessages } from '@/utils/promptMessages'
import { teaPromptMessages } from '@/utils/promptMessages'

defineOptions({
  name: 'ShopManagePage'
})
    const router = useRouter()
    const shopStore = useShopStore()
    const teaStore = useTeaStore()
    const teaFormRef = ref(null)
    
    // 从Pinia获取店铺信息
    const loading = computed(() => shopStore.loading)
    const shop = computed(() => shopStore.myShop)
    const defaultLogo = '/mock-images/shop-default.jpg'
    const defaultTeaImage = '/mock-images/tea-default.jpg'
    
    // 茶叶列表相关
    const teaLoading = ref(false)
    const searchQuery = ref('')
    const statusFilter = ref('')
    const categoryFilter = ref('')
    // 使用 store 的分页信息，确保同步
    const currentPage = computed({
      get: () => shopStore.pagination.currentPage,
      set: (val) => { shopStore.pagination.currentPage = val }
    })
    const pageSize = computed({
      get: () => shopStore.pagination.pageSize,
      set: (val) => { shopStore.pagination.pageSize = val }
    })
    const shopTeas = computed(() => shopStore.shopTeas || [])
    const totalCount = computed(() => shopStore.pagination.total || 0) // 使用后端返回的总数
    
    // 对话框相关状态
    const dialogVisible = ref(false)
    const isEdit = ref(false)
    const currentTea = ref(null)
    const submitting = ref(false)
    
    // 分类数据从Pinia获取
    const categoryOptions = computed(() => teaStore.categories)
    
    // 图片上传相关状态
    const teaImages = ref([])
    const previewVisible = ref(false)
    const previewImage = ref('')
    const mainImageIndex = ref(0)
    const teaStatus = ref(0)
    
    // 计算属性：过滤出有URL的图片（能预览就说明有URL，可以选为主图）
    const validImages = computed(() => {
      const images = teaImages.value.filter(img => {
        // 检查是否有url属性（上传成功后设置）
        return img.url && img.url.trim() !== ''
      })
      // 开发环境调试日志
      if (process.env.NODE_ENV === 'development') {
        console.log('validImages计算:', {
          total: teaImages.value.length,
          valid: images.length,
          allImages: teaImages.value.map(img => ({ uid: img.uid, url: img.url, status: img.status }))
        })
      }
      return images
    })
    
    // 表单验证规则
    const teaRules = {
      name: [
        { required: true, message: '请输入茶叶名称', trigger: 'blur' },
        { min: 2, max: 100, message: '长度在2到100个字符', trigger: 'blur' }
      ],
      categoryId: [
        { required: true, message: '请选择茶叶分类', trigger: 'change' }
      ],
      origin: [
        { required: true, message: '请输入产地', trigger: 'blur' }
      ],
      description: [
        { required: true, message: '请输入茶叶简介', trigger: 'blur' }
      ],
      images: [
        { 
          validator: (rule, value, callback) => {
            if (teaImages.value.length === 0) {
              callback(new Error('请至少上传一张图片'))
            } else {
              callback()
            }
          }, 
          trigger: 'change' 
        }
      ]
    }
    
    // 任务组0：使用Pinia加载店铺信息
    const loadShopInfo = async () => {
      try {
        await shopStore.fetchMyShop()
      } catch (error) {
        console.error('加载店铺信息失败:', error)
      }
    }
    
    // 使用Pinia加载店铺茶叶列表
    const loadShopTeas = async () => {
      if (!shop.value || !shop.value.id) {
        shopPromptMessages.showShopInfoLoadFirst()
        return
      }
      
      teaLoading.value = true
      try {
        const params = {
          page: shopStore.pagination.currentPage,
          size: shopStore.pagination.pageSize  // 后端期望的参数名是 size
        }
        
        // 状态筛选：空字符串表示全部，1表示已上架，0表示已下架
        if (statusFilter.value !== '') {
          params.status = parseInt(statusFilter.value)
        }
        
        if (categoryFilter.value) {
          params.categoryId = categoryFilter.value
        }
        
        if (searchQuery.value) {
          params.keyword = searchQuery.value
        }
        
        // 店铺茶叶：使用当前店铺ID
        params.shopId = shop.value.id
        
        await shopStore.fetchShopTeas({
          shopId: shop.value.id,
          params
        })
      } catch (error) {
        console.error('加载茶叶列表失败:', error)
      } finally {
        teaLoading.value = false
      }
    }
    
    // 图片上传相关：选择图片后立即上传获取路径（完全照抄文章封面上传）
    const handleImageUpload = async (options) => {
      const { file } = options
      if (!file) return
      try {
        const res = await teaStore.uploadTeaImages({ files: [file] })
        if (res?.code) showByCode(res.code)
        const url = res?.data?.url
        const path = res?.data?.path
        // 存库使用相对路径（path），预览仍然可以用完整URL
        if (path) {
          file.path = path
          file.url = url || path
          file.status = 'success'
        } else if (url) {
          // 兜底：旧接口没有path字段时，仍然使用url
          file.path = url
          file.url = url
          file.status = 'success'
        } else {
          file.status = 'fail'
          ElMessage.error('图片上传失败：返回数据缺少路径')
          teaImages.value = teaImages.value.filter(img => img.uid !== file.uid)
          return
        }
        // 确保 teaImages 数组中的对应文件也被更新
        const index = teaImages.value.findIndex(img => img.uid === file.uid)
        if (index >= 0) {
          teaImages.value[index] = { ...teaImages.value[index], ...file }
        }
      } catch (error) {
        console.error('图片上传失败:', error)
        file.status = 'fail'
        ElMessage.error('图片上传失败，请重试')
        teaImages.value = teaImages.value.filter(img => img.uid !== file.uid)
      }
    }
    
    // 图片列表变化时同步更新（el-upload 的 on-change 事件）
    const handleImageChange = (file, fileList) => {
      teaImages.value = fileList
    }
    
    const handleImageRemove = (file, fileList) => {
      teaImages.value = fileList
      
      // 如果删除的是主图，重置主图索引（基于 validImages）
      if (mainImageIndex.value >= validImages.value.length) {
        mainImageIndex.value = validImages.value.length > 0 ? 0 : -1
      }
    }
    
    const handleImagePreview = file => {
      previewImage.value = file.url
      previewVisible.value = true
    }
    
    // 规格相关方法
    const addSpec = () => {
      if (!currentTea.value) return
      
      currentTea.value.specifications.push({
        id: Date.now(),
        specName: '',
        price: 0,
        stock: 0,
        isDefault: currentTea.value.specifications.length === 0
      })
    }
    
    const removeSpec = index => {
      if (!currentTea.value || currentTea.value.specifications.length <= 1) return
      
      // 如果删除的是默认规格，将第一个规格设为默认
      if (currentTea.value.specifications[index].isDefault) {
        currentTea.value.specifications[0].isDefault = true
      }
      
      currentTea.value.specifications.splice(index, 1)
    }
    
    // 处理默认规格变更
    const handleDefaultChange = (val, index) => {
      if (!val) {
        // 不允许取消默认，必须有一个默认规格
        currentTea.value.specifications[index].isDefault = true
        return
      }
      
      // 将其他规格设为非默认
      currentTea.value.specifications.forEach((spec, i) => {
        if (i !== index) {
          spec.isDefault = false
        }
      })
    }
    
    // 处理编辑
    const handleEdit = async tea => {
      isEdit.value = true
      currentTea.value = JSON.parse(JSON.stringify(tea)) // 深拷贝避免直接修改列表数据
      
      // 如果列表数据没有图片或规格，调用详情接口获取完整数据
      const needFetchDetail = !tea.images || tea.images.length === 0 || !tea.specifications || tea.specifications.length === 0
      
      if (needFetchDetail) {
        try {
          const detailRes = await teaStore.fetchTeaDetail(tea.id)
          if (detailRes && detailRes.code === 200 && detailRes.data) {
            const detailData = detailRes.data
            // 更新基本信息（保留列表数据中的字段，补充详情数据）
            if (detailData.specifications && detailData.specifications.length > 0) {
              currentTea.value.specifications = detailData.specifications.map(spec => ({
                id: spec.id,
                specName: spec.specName,
                price: spec.price,
                stock: spec.stock,
                isDefault: spec.isDefault === 1 || spec.isDefault === true ? 1 : 0
              }))
            }
            if (detailData.images && detailData.images.length > 0) {
              teaImages.value = detailData.images.map(img => ({
                name: img.url?.split('/').pop() || 'image',
                url: img.url,
                is_main: img.isMain === 1 || img.isMain === true || img.is_main === 1 || img.is_main === true,
                uid: img.id,
                id: img.id
              }))
            } else {
              teaImages.value = []
            }
          }
        } catch (error) {
          if (process.env.NODE_ENV === 'development') {
            console.error('获取茶叶详情失败:', error)
          }
        }
      }
      
      // 任务组C：加载规格列表（如果详情接口没有返回规格，则单独加载）
      if (!currentTea.value.specifications || currentTea.value.specifications.length === 0) {
        try {
          await teaStore.fetchTeaSpecifications(tea.id)
          // 如果Pinia中有规格数据，使用Pinia的数据
          const specs = teaStore.currentTeaSpecs
          if (specs && specs.length > 0) {
            currentTea.value.specifications = specs.map(spec => ({
              id: spec.id,
              specName: spec.specName,
              price: spec.price,
              stock: spec.stock,
              isDefault: spec.isDefault === 1 || spec.isDefault === true ? 1 : 0
            }))
          }
        } catch (error) {
          // 网络错误已由API拦截器处理并显示消息，这里只记录日志用于开发调试
          if (process.env.NODE_ENV === 'development') {
            console.error('加载规格列表失败:', error)
          }
          // 如果加载失败，使用tea中的规格数据或创建默认规格
          if (!currentTea.value.specifications || currentTea.value.specifications.length === 0) {
            currentTea.value.specifications = [{
              id: Date.now(),
              specName: '默认规格',
              price: tea.price || 0,
              stock: tea.stock || 0,
              isDefault: 1
            }]
          }
        }
      }
      
      // 任务组D：设置图片列表（如果详情接口没有返回图片，则使用列表数据）
      if (!teaImages.value || teaImages.value.length === 0) {
        if (tea.images && tea.images.length > 0) {
          teaImages.value = tea.images.map(img => {
            // 从完整URL中提取相对路径（用于保存时使用）
            let path = img.url
            if (path) {
              // 如果URL包含 /api/files/ 或 /files/，提取相对路径
              const apiFilesIndex = path.indexOf('/files/')
              if (apiFilesIndex >= 0) {
                path = path.substring(apiFilesIndex + 1) // 去掉开头的 /，保留 files/...
              } else if (path.startsWith('http://') || path.startsWith('https://')) {
                // 如果是完整URL，尝试提取路径部分
                try {
                  const urlObj = new URL(path)
                  const pathname = urlObj.pathname
                  if (pathname.startsWith('/api/files/')) {
                    path = pathname.substring(5) // 去掉 /api，保留 /files/...
                  } else if (pathname.startsWith('/files/')) {
                    path = pathname.substring(1) // 去掉开头的 /，保留 files/...
                  }
                } catch (e) {
                  // URL解析失败，保持原值
                }
              } else {
                path = url // 已经是相对路径
              }
            }
            return {
              name: img.url?.split('/').pop() || 'image',
              url: img.url, // 完整URL用于预览
              path: path,   // 相对路径用于保存
              is_main: img.is_main === 1 || img.is_main === true || img.isMain === 1 || img.isMain === true,
              uid: img.id,
              id: img.id,
              status: 'success' // 编辑时加载的图片视为已成功上传
            }
          })
        } else {
          teaImages.value = []
        }
      }
      
      // 设置主图索引（基于 validImages）
      // 检查 is_main 字段（可能是布尔值、数字1/0，或者 isMain）
      const mainImageIdx = validImages.value.findIndex(img => {
        return img.is_main === 1 || img.is_main === true || 
               img.isMain === 1 || img.isMain === true ||
               (img.is_main !== undefined && img.is_main !== 0 && img.is_main !== false)
      })
      mainImageIndex.value = mainImageIdx >= 0 ? mainImageIdx : 0
      
      // 调试日志
      if (process.env.NODE_ENV === 'development') {
        console.log('编辑时设置主图索引:', {
          validImagesCount: validImages.value.length,
          mainImageIdx,
          mainImageIndex: mainImageIndex.value,
          images: validImages.value.map((img, idx) => ({ 
            idx, 
            is_main: img.is_main, 
            isMain: img.isMain,
            url: img.url 
          }))
        })
      }
      
      // 设置上架状态
      teaStatus.value = tea.status
      
      dialogVisible.value = true
    }
    
    // 处理状态切换
    // 任务组C：处理状态切换（使用Pinia）
    const handleToggleStatus = async tea => {
      const action = tea.status === 1 ? '下架' : '上架'
      const newStatus = tea.status === 1 ? 0 : 1
      
      try {
        await ElMessageBox.confirm(
          `确定要${action}茶叶"${tea.name}"吗？`,
          `${action}确认`,
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        
        teaLoading.value = true
        const response = await shopStore.toggleShopTeaStatus({
          teaId: tea.id,
          status: newStatus
        })
        showByCode(response.code)
      } catch (error) {
        if (error !== 'cancel') {
          console.error(`${action}茶叶失败:`, error)
        }
      } finally {
        teaLoading.value = false
      }
    }
    
    // 任务组C：处理删除（使用Pinia）
    const handleDelete = async tea => {
      try {
        await ElMessageBox.confirm(
          `确定要删除茶叶"${tea.name}"吗？此操作不可恢复！`,
          '删除确认',
          {
            confirmButtonText: '确定删除',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        
        teaLoading.value = true
        const response = await shopStore.deleteShopTea({
          teaId: tea.id,
          shopId: shop.value?.id
        })
        showByCode(response.code)
        
        // 如果当前页没有数据了，回到前一页
        if (shopTeas.value.length === 0 && shopStore.pagination.currentPage > 1) {
          shopStore.pagination.currentPage--
          await loadShopTeas()
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除茶叶失败:', error)
        }
      } finally {
        teaLoading.value = false
      }
    }
    
    // 对话框关闭
    const handleDialogClose = done => {
      if (submitting.value) {
        teaPromptMessages.showSubmitting()
        return
      }
      done()
    }
    
    // 处理保存
    const handleSave = () => {
      if (!teaFormRef.value) return
      
      if (!shop.value || !shop.value.id) {
        shopPromptMessages.showShopInfoLoadFirst()
        return
      }
      
      teaFormRef.value.validate(async valid => {
        if (!valid) {
          teaPromptMessages.showFormInvalid()
          return
        }
        
        // 校验规格
        if (!currentTea.value.specifications || currentTea.value.specifications.length === 0) {
          teaPromptMessages.showSpecRequired()
          return
        }
        
        // 校验规格名称和价格
        const invalidSpec = currentTea.value.specifications.find(spec => 
          !spec.specName || spec.price <= 0
        )
        if (invalidSpec) {
          teaPromptMessages.showSpecIncomplete()
          return
        }
        
        // 校验是否有默认规格
        const hasDefault = currentTea.value.specifications.some(spec => spec.isDefault)
        if (!hasDefault) {
          teaPromptMessages.showDefaultSpecRequired()
          return
        }
        
        // 校验图片
        if (teaImages.value.length === 0) {
          teaPromptMessages.showImageRequired()
          return
        }
        
        submitting.value = true
        
        try {
          // 准备数据
          const formData = {
            ...currentTea.value,
            status: teaStatus.value,
            categoryId: parseInt(currentTea.value.categoryId),
            // 店铺茶叶：使用当前店铺ID
            shopId: shop.value.id
          }
          
          // 设置图片路径：选择图片后已立即上传获取路径，这里直接使用path（相对路径）存入数据库
          if (validImages.value.length > 0) {
            // mainImageIndex 是基于 validImages 的索引，确保索引有效
            const selectedIndex = mainImageIndex.value >= 0 && mainImageIndex.value < validImages.value.length 
              ? mainImageIndex.value 
              : 0
            const mainImg = validImages.value[selectedIndex]
            
            // 如果没有 path，尝试从 url 中提取相对路径，或者直接使用 url
            let mainImagePath = mainImg.path
            if (!mainImagePath && mainImg.url) {
              // 从完整URL中提取相对路径
              const url = mainImg.url
              const apiFilesIndex = url.indexOf('/files/')
              if (apiFilesIndex >= 0) {
                mainImagePath = url.substring(apiFilesIndex + 1) // 去掉开头的 /，保留 files/...
              } else if (url.startsWith('http://') || url.startsWith('https://')) {
                try {
                  const urlObj = new URL(url)
                  const pathname = urlObj.pathname
                  if (pathname.startsWith('/api/files/')) {
                    mainImagePath = pathname.substring(5) // 去掉 /api，保留 /files/...
                  } else if (pathname.startsWith('/files/')) {
                    mainImagePath = pathname.substring(1) // 去掉开头的 /，保留 files/...
                  } else {
                    mainImagePath = url // 如果无法提取，使用完整URL（后端会处理）
                  }
                } catch (e) {
                  mainImagePath = url // URL解析失败，使用完整URL
                }
              } else {
                mainImagePath = url // 已经是相对路径
              }
            }
            
            if (!mainImagePath) {
              ElMessage.error('主图路径不存在，请重新选择主图')
              submitting.value = false
              return
            }
            
            formData.mainImage = mainImagePath
            
            // 设置图片列表，根据选择的索引设置 is_main
            formData.images = validImages.value.map((img, idx) => {
              // 如果没有 path，从 url 中提取相对路径
              let imagePath = img.path
              if (!imagePath && img.url) {
                const url = img.url
                const apiFilesIndex = url.indexOf('/files/')
                if (apiFilesIndex >= 0) {
                  imagePath = url.substring(apiFilesIndex + 1)
                } else if (url.startsWith('http://') || url.startsWith('https://')) {
                  try {
                    const urlObj = new URL(url)
                    const pathname = urlObj.pathname
                    if (pathname.startsWith('/api/files/')) {
                      imagePath = pathname.substring(5)
                    } else if (pathname.startsWith('/files/')) {
                      imagePath = pathname.substring(1)
                    } else {
                      imagePath = url
                    }
                  } catch (e) {
                    imagePath = url
                  }
                } else {
                  imagePath = url
                }
              }
              return {
                id: img.id || null, // 重要：传递 id 字段，用于后端判断是更新还是新增
                url: imagePath || img.url, // 优先使用相对路径，否则使用完整URL
                is_main: idx === selectedIndex ? 1 : 0  // 使用数字 1/0，不是布尔值
              }
            })
            
            // 调试日志
            if (process.env.NODE_ENV === 'development') {
              console.log('保存主图设置:', {
                mainImageIndex: mainImageIndex.value,
                selectedIndex,
                mainImagePath: formData.mainImage,
                images: formData.images.map((img, idx) => ({ 
                  id: img.id,
                  url: img.url, 
                  is_main: img.is_main,
                  index: idx 
                }))
              })
            }
          }
          
          // 计算总库存
          formData.stock = formData.specifications.reduce((sum, spec) => sum + spec.stock, 0)
          
          // 设置基础价格为默认规格的价格
          const defaultSpec = formData.specifications.find(spec => spec.isDefault)
          if (defaultSpec) {
            formData.price = defaultSpec.price
          }
          
          // 调用店铺相关的API保存茶叶
          if (isEdit.value) {
            const response = await shopStore.updateShopTea({
              teaId: currentTea.value.id,
              teaData: formData
            })
            
            showByCode(response.code)
          } else {
            // 新增茶叶：店铺茶叶使用当前店铺ID
            formData.shopId = shop.value.id
            
            const result = await shopStore.addShopTea({
              shopId: shop.value.id,
              teaData: formData
            })
            
            showByCode(result.code)
          }
          
          // 关闭对话框
          dialogVisible.value = false
          submitting.value = false
          
          // 重新加载列表
          await loadShopTeas()
        } catch (error) {
          // 网络错误已由API拦截器处理并显示消息，这里只记录日志用于开发调试
          if (process.env.NODE_ENV === 'development') {
            console.error('保存茶叶失败:', error)
          }
          submitting.value = false
        }
      })
    }
    
    // 显示添加茶叶对话框
    const showAddTeaDialog = () => {
      if (!shop.value || !shop.value.id) {
        shopPromptMessages.showShopInfoLoadFirst()
        return
      }
      
      isEdit.value = false
      currentTea.value = {
        name: '',
        // 店铺茶叶：使用当前店铺ID
        shopId: shop.value.id,
        categoryId: '',
        price: 0,
        description: '',
        detail: '',
        origin: '商南县',
        stock: 0,
        sales: 0,
        mainImage: '',
        status: 0, // 默认下架状态
        specifications: [
          {
            id: Date.now(),
            specName: '默认规格',
            price: 0,
            stock: 0,
            isDefault: true
          }
        ],
        images: []
      }
      teaImages.value = []
      mainImageIndex.value = 0
      teaStatus.value = 0
      dialogVisible.value = true
    }
    
    // 跳转到店铺设置页
    const goToShopSettings = () => {
      if (!shop.value || !shop.value.id) {
        shopPromptMessages.showShopInfoLoadFirst()
        return
      }
      router.push('/shop/settings')
    }
    
    // 预览店铺：以普通用户视角打开店铺详情页
    const handlePreviewShop = () => {
      if (!shop.value || !shop.value.id) {
        shopPromptMessages.showShopInfoLoadFirst()
        return
      }
      router.push(`/shop/${shop.value.id}`)
    }
    
    // 获取最低价格
    const getMinPrice = tea => {
      if (!tea.specifications || tea.specifications.length === 0) {
        return tea.price
      }
      
      // 从规格中找出最低价格
      const prices = tea.specifications.map(spec => spec.price)
      return Math.min(...prices)
    }
    
    // 获取分类名称
    const getCategoryName = categoryId => {
      if (!categoryId) return '-'
      const category = categoryOptions.find(c => c.id === parseInt(categoryId))
      return category ? category.name : '-'
    }
    
    // 任务组C：搜索处理
    const handleSearch = async () => {
      shopStore.pagination.currentPage = 1
      await loadShopTeas()
    }
    
    // 任务组C：筛选变更处理
    const handleFilterChange = async () => {
      shopStore.pagination.currentPage = 1
      await loadShopTeas()
    }
    
    // 任务组C：页码变更
    const handlePageChange = async page => {
      shopStore.pagination.currentPage = page
      await loadShopTeas()
    }
    
    // 任务组C：每页条数变更
    const handleSizeChange = async size => {
      shopStore.pagination.pageSize = size
      shopStore.pagination.currentPage = 1
      await loadShopTeas()
    }
    
    
    // 任务组B：Banner管理相关
    const bannerLoading = ref(false)
    const shopBanners = computed(() => shopStore.shopBanners || [])
    const bannerDialogVisible = ref(false)
    const currentBanner = ref(null)
    const isEditBanner = ref(false)
    const bannerFormRef = ref(null)
    
    // 加载Banner列表
    const loadBanners = async () => {
      if (!shop.value || !shop.value.id) return
      
      bannerLoading.value = true
      try {
        const response = await shopStore.fetchShopBanners(shop.value.id)
        if (response && !isSuccess(response.code)) {
          showByCode(response.code)
        }
      } catch (error) {
        console.error('加载Banner列表失败:', error)
      } finally {
        bannerLoading.value = false
      }
    }
    
    // 显示添加Banner对话框
    const showAddBannerDialog = () => {
      isEditBanner.value = false
      currentBanner.value = {
        image_url: '',
        link_url: '',
        title: '',
        order: shopBanners.value.length + 1,
        is_enabled: 1
      }
      bannerDialogVisible.value = true
    }
    
    // 显示编辑Banner对话框
    const showEditBannerDialog = banner => {
      isEditBanner.value = true
      currentBanner.value = { ...banner }
      bannerDialogVisible.value = true
    }
    
    // 保存Banner
    const handleSaveBanner = async () => {
      if (!shop.value || !shop.value.id) {
        showByCode(4103) // 店铺信息不存在
        return
      }
      
      try {
        const bannerData = {
          image_url: currentBanner.value.image_url,
          link_url: currentBanner.value.link_url,
          title: currentBanner.value.title,
          order: currentBanner.value.order,
          is_enabled: currentBanner.value.is_enabled
        }
        
        let response
        if (isEditBanner.value) {
          response = await shopStore.updateBanner({
            bannerId: currentBanner.value.id,
            bannerData
          })
        } else {
          response = await shopStore.uploadBanner({
            shopId: shop.value.id,
            bannerData
          })
        }
        
        showByCode(response.code)
        
        if (isSuccess(response.code)) {
          bannerDialogVisible.value = false
          await loadBanners()
        }
      } catch (error) {
        console.error('保存Banner失败:', error)
      }
    }
    
    // 删除Banner
    const handleDeleteBanner = async banner => {
      try {
        await ElMessageBox.confirm(
          `确定要删除Banner"${banner.title || '未命名'}"吗？`,
          '删除确认',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        
        const response = await shopStore.deleteBanner({
          bannerId: banner.id,
          shopId: shop.value.id
        })
        showByCode(response.code)
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除Banner失败:', error)
        }
      }
    }
    
    // 移动Banner顺序
    const moveBanner = async (index, direction) => {
      if (!shop.value || !shop.value.id) return
      
      const banners = [...shopBanners.value]
      const newIndex = direction === 'up' ? index - 1 : index + 1
      
      if (newIndex < 0 || newIndex >= banners.length) return
      
      // 交换位置
      [banners[index], banners[newIndex]] = [banners[newIndex], banners[index]]
      
      // 更新order
      const orderData = banners.map((banner, i) => ({
        id: banner.id,
        order: i + 1
      }))
      
      try {
        const response = await shopStore.updateBannerOrder({
          orderData,
          shopId: shop.value.id
        })
        showByCode(response.code)
      } catch (error) {
        console.error('更新Banner排序失败:', error)
      }
    }
    
    // 任务组B：公告管理相关
    const announcementLoading = ref(false)
    const shopAnnouncements = computed(() => shopStore.shopAnnouncements || [])
    const announcementDialogVisible = ref(false)
    const currentAnnouncement = ref(null)
    const isEditAnnouncement = ref(false)
    const announcementFormRef = ref(null)
    
    // 加载公告列表
    const loadAnnouncements = async () => {
      if (!shop.value || !shop.value.id) return
      
      announcementLoading.value = true
      try {
        const response = await shopStore.fetchShopAnnouncements(shop.value.id)
        if (response && !isSuccess(response.code)) {
          showByCode(response.code)
        }
      } catch (error) {
        console.error('加载公告列表失败:', error)
      } finally {
        announcementLoading.value = false
      }
    }
    
    // 显示添加公告对话框
    const showAddAnnouncementDialog = () => {
      isEditAnnouncement.value = false
      currentAnnouncement.value = {
        title: '',
        content: '',
        isTop: false
      }
      announcementDialogVisible.value = true
    }
    
    // 显示编辑公告对话框
    const showEditAnnouncementDialog = announcement => {
      isEditAnnouncement.value = true
      currentAnnouncement.value = { ...announcement }
      announcementDialogVisible.value = true
    }
    
    // 保存公告
    const handleSaveAnnouncement = async () => {
      if (!shop.value || !shop.value.id) {
        showByCode(4103) // 店铺信息不存在
        return
      }
      
      try {
        const announcementData = {
          title: currentAnnouncement.value.title,
          content: currentAnnouncement.value.content,
          isTop: currentAnnouncement.value.isTop ? true : false
        }
        
        let response
        if (isEditAnnouncement.value) {
          response = await shopStore.updateAnnouncement({
            announcementId: currentAnnouncement.value.id,
            announcementData
          })
        } else {
          response = await shopStore.createAnnouncement({
            shopId: shop.value.id,
            announcementData
          })
        }
        
        showByCode(response.code)
        
        if (isSuccess(response.code)) {
          announcementDialogVisible.value = false
          await loadAnnouncements()
        }
      } catch (error) {
        console.error('保存公告失败:', error)
      }
    }
    
    // 删除公告
    const handleDeleteAnnouncement = async announcement => {
      try {
        await ElMessageBox.confirm(
          `确定要删除公告"${announcement.title}"吗？`,
          '删除确认',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        
        const response = await shopStore.deleteAnnouncement(announcement.id)
        showByCode(response.code)
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除公告失败:', error)
        }
      }
    }
    
    // 格式化时间
    const formatTime = time => {
      if (!time) return '-'
      const date = new Date(time)
      return date.toLocaleString('zh-CN')
    }
    
    // 任务组D：统计数据相关
    const statisticsLoading = ref(false)
    const shopStatistics = computed(() => shopStore.shopStatistics)
    const overviewStats = computed(() => shopStatistics.value?.overview || {})
    const dateRange = ref([])
    
    // 加载统计数据
    const loadStatistics = async () => {
      if (!shop.value || !shop.value.id) return
      
      statisticsLoading.value = true
      try {
        let startDate
        let endDate
        if (dateRange.value && dateRange.value.length === 2) {
          startDate = dateRange.value[0]
          endDate = dateRange.value[1]
        }
        const response = await shopStore.fetchShopStatistics({
          shopId: shop.value.id,
          startDate,
          endDate
        })
        if (response && !isSuccess(response.code)) {
          showByCode(response.code)
        }
      } catch (error) {
        console.error('加载统计数据失败:', error)
      } finally {
        statisticsLoading.value = false
      }
    }
    
    // 日期范围变更处理
    const handleDateRangeChange = () => {
      loadStatistics()
    }
    
    // 格式化金额
    const formatMoney = amount => {
      return Number(amount).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
    }
    
    // 任务组E：Logo上传前验证（格式、大小）
    const beforeLogoUpload = file => {
      const allowedTypes = ['image/jpeg', 'image/png', 'image/webp']
      const isAllowedType = allowedTypes.includes(file.type)
      const isLt2M = file.size / 1024 / 1024 < 2
      
      if (!isAllowedType) {
        showByCode(4114) // 不支持的文件类型
        return false
      }
      if (!isLt2M) {
        showByCode(4115) // 文件大小超限
        return false
      }
      return true
    }
    
    // Logo上传成功处理
    const handleLogoSuccess = async response => {
      if (response.code === 200 && response.data) {
        showByCode(4007) // Logo上传成功
        // 更新店铺信息
        await shopStore.fetchMyShop()
      } else {
        showByCode(response.code || 4113) // Logo上传失败
      }
    }

    // 任务组E：通过Pinia Action处理Logo上传，遵循组件 → Pinia → API 数据流
    const handleLogoUploadRequest = async param => {
      if (!shop.value || !shop.value.id) {
        showByCode(4103) // 店铺信息不存在
        param.onError(new Error('店铺信息不存在'))
        return
      }
      try {
        const res = await shopStore.uploadShopLogo({
          shopId: shop.value.id,
          file: param.file
        })
        // 触发 el-upload 的 on-success 回调
        param.onSuccess({ code: 200, data: res })
      } catch (error) {
        console.error('Logo上传失败:', error)
        showByCode(4113) // Logo上传失败
        param.onError(error)
      }
    }
    
    onMounted(async () => {
      // 加载分类数据
      await teaStore.fetchCategories()
      // 加载店铺信息
      await loadShopInfo()
      // 加载店铺茶叶列表
      await loadShopTeas()
      loadBanners()
      loadAnnouncements()
      loadStatistics()
    })
</script>

<style lang="scss" scoped>
.shop-manage-page {
  min-height: 100vh;
  background-color: #f5f7fa;
  padding: 20px 0 40px;
  
  .shop-manage-container {
    width: 85%;
    max-width: 1920px;
    margin: 0 auto;
    padding: 0;
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
          
          .logo-uploader {
            position: relative;
            
            .shop-logo {
              width: 80px;
              height: 80px;
              border-radius: 50%;
              object-fit: cover;
              cursor: pointer;
              border: 2px dashed var(--el-border-color);
              transition: border-color 0.3s;
              
              &:hover {
                border-color: var(--el-color-primary);
              }
            }
            
            .logo-uploader-icon {
              font-size: 28px;
              color: var(--el-text-color-secondary);
            }
          }
          
          .logo-tip {
            margin-top: 8px;
            font-size: 12px;
            color: var(--el-text-color-secondary);
            text-align: center;
          }
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
    
    // 任务组B：Banner管理样式
    .banner-manage-card {
      margin-bottom: 20px;
      
      .banner-manage-content {
        .empty-banner {
          padding: 40px 0;
        }
        
        .banner-list {
          display: flex;
          flex-direction: column;
          gap: 16px;
          
          .banner-item {
            display: flex;
            gap: 16px;
            padding: 16px;
            border: 1px solid var(--el-border-color-light);
            border-radius: 4px;
            background-color: #fff;
            
            .banner-preview {
              flex: 1;
              
              .banner-info {
                margin-top: 12px;
                
                .banner-title {
                  font-weight: 500;
                  margin-bottom: 4px;
                }
                
                .banner-link {
                  font-size: 12px;
                  color: var(--el-text-color-secondary);
                  margin-bottom: 4px;
                }
                
                .banner-order {
                  font-size: 12px;
                  color: var(--el-text-color-placeholder);
                }
              }
            }
            
            .banner-actions {
              display: flex;
              flex-direction: column;
              gap: 8px;
              
              .banner-order-controls {
                display: flex;
                flex-direction: column;
                gap: 4px;
              }
            }
          }
        }
      }
    }
    
    // 任务组B：公告管理样式
    .announcement-manage-card {
      margin-bottom: 20px;
      
      .announcement-manage-content {
        .empty-announcement {
          padding: 40px 0;
        }
        
        .announcement-list {
          display: flex;
          flex-direction: column;
          gap: 16px;
          
          .announcement-item {
            padding: 16px;
            border: 1px solid var(--el-border-color-light);
            border-radius: 4px;
            background-color: #fff;
            
            &.is-top {
              border-color: var(--el-color-warning);
              background-color: #fffbf0;
            }
            
            .announcement-header {
              display: flex;
              justify-content: space-between;
              align-items: center;
              margin-bottom: 12px;
              
              .announcement-title {
                display: flex;
                align-items: center;
                gap: 8px;
                font-weight: 500;
              }
              
              .announcement-time {
                font-size: 12px;
                color: var(--el-text-color-secondary);
              }
            }
            
            .announcement-content {
              margin-bottom: 12px;
              color: var(--el-text-color-regular);
              line-height: 1.6;
              white-space: pre-wrap;
            }
            
            .announcement-actions {
              display: flex;
              gap: 8px;
            }
          }
        }
      }
    }
    
    // 任务组D：统计数据样式
    .statistics-card {
      margin-bottom: 20px;
      
      .statistics-overview {
        display: grid;
        grid-template-columns: repeat(4, 1fr);
        gap: 20px;
        margin-bottom: 30px;
        
        .overview-item {
          padding: 20px;
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          border-radius: 8px;
          color: #fff;
          text-align: center;
          
          &:nth-child(2) {
            background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
          }
          
          &:nth-child(3) {
            background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
          }
          
          &:nth-child(4) {
            background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
          }
          
          .overview-label {
            font-size: 14px;
            opacity: 0.9;
            margin-bottom: 8px;
          }
          
          .overview-value {
            font-size: 24px;
            font-weight: bold;
          }
        }
      }
      
      .statistics-trend,
      .statistics-hot-products {
        margin-top: 30px;
        
        .section-title {
          margin-bottom: 16px;
          font-size: 16px;
          font-weight: 500;
          color: var(--el-text-color-primary);
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