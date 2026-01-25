<template>
  <div class="tea-manage-page">
    <div class="page-header">
      <div class="header-title">
        <h1>平台茶叶管理</h1>
        <p class="sub-title">管理平台直售茶叶商品，支持添加、编辑、上架和下架操作</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="showAddTeaDialog" v-if="activeTab === 'tea'">
          <el-icon><Plus /></el-icon>添加茶叶
        </el-button>
        <el-button type="primary" @click="showAddCategoryDialog" v-if="activeTab === 'category'">
          <el-icon><Plus /></el-icon>添加分类
        </el-button>
      </div>
    </div>

    <!-- 标签页 -->
    <el-tabs v-model="activeTab" class="manage-tabs">
      <el-tab-pane label="茶叶管理" name="tea">
        <!-- 搜索和筛选区域 -->
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
          <el-table
        v-loading="loading"
        :data="teas"
        style="width: 100%"
        border
        stripe
        highlight-current-row
        @selection-change="handleSelectionChange"
          >
            <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="茶叶ID" width="100">
          <template #default="scope">
            <el-link type="primary" @click="handlePreview(scope.row)">{{ scope.row.id }}</el-link>
          </template>
        </el-table-column>
        <el-table-column label="图片" width="100">
          <template #default="scope">
            <SafeImage
              :src="scope.row.main_image"
              type="tea"
              :alt="scope.row.name"
              style="width: 100px; height: 100px; object-fit: cover;"
              class="tea-image"
            />
          </template>
        </el-table-column>
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
        <el-table-column label="创建时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.create_time) }}
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
          
          <!-- 批量操作栏 -->
          <div v-if="selectedTeas.length > 0" class="batch-actions">
        <span class="selected-count">已选择 {{ selectedTeas.length }} 项</span>
        <el-button type="success" @click="handleBatchOnShelf">批量上架</el-button>
            <el-button type="warning" @click="handleBatchOffShelf">批量下架</el-button>
          </div>

          <!-- 分页 -->
          <div class="pagination-container">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalCount"
          :page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :current-page="currentPage"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
          />
          </div>
        </div>
      </el-tab-pane>
      
      <!-- 分类管理标签页 -->
      <el-tab-pane label="分类管理" name="category">
        <div class="category-manage-container">
          <el-table
            v-loading="categoryLoading"
            :data="categories"
            style="width: 100%"
            border
            stripe
          >
            <el-table-column prop="id" label="分类ID" width="100" />
            <el-table-column prop="name" label="分类名称" min-width="150" />
            <el-table-column prop="parentId" label="父分类ID" width="120">
              <template #default="scope">
                {{ scope.row.parentId || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="sortOrder" label="排序" width="100" />
            <el-table-column prop="icon" label="图标" width="150" />
            <el-table-column label="创建时间" width="180">
              <template #default="scope">
                {{ formatDate(scope.row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" fixed="right" width="150">
              <template #default="scope">
                <el-button 
                  type="primary" 
                  link 
                  @click="handleEditCategory(scope.row)"
                >
                  编辑
                </el-button>
                <el-button 
                  type="danger" 
                  link 
                  @click="handleDeleteCategory(scope.row)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
    </el-tabs>

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
                  v-model="scope.row.spec_name"
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
                  v-model="scope.row.is_default"
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
              :auto-upload="false"
              :file-list="teaImages"
              :on-change="handleImageChange"
              :on-remove="handleImageRemove"
              :on-preview="handleImagePreview"
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
        
        <el-form-item label="主图设置">
          <el-select v-model="mainImageIndex" placeholder="请选择主图" style="width: 100%">
            <el-option
              v-for="(image, index) in teaImages"
              :key="image.uid || index"
              :label="`图片${index + 1}`"
              :value="index"
            >
              <div class="main-image-option">
                <SafeImage
                  :src="image"
                  type="tea"
                  alt="茶叶图片"
                  style="width: 40px; height: 40px; margin-right: 10px"
                />
                <span>图片{{ index + 1 }}{{ index === mainImageIndex ? ' (当前主图)' : '' }}</span>
              </div>
            </el-option>
          </el-select>
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

    <!-- 添加/编辑分类对话框 -->
    <el-dialog
      v-model="categoryDialogVisible"
      :title="isEditCategory ? '编辑分类' : '添加分类'"
      width="500px"
      :before-close="handleCategoryDialogClose"
      destroy-on-close
    >
      <el-form 
        ref="categoryFormRef" 
        :model="currentCategory" 
        :rules="categoryRules" 
        label-width="100px"
      >
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="currentCategory.name" placeholder="请输入分类名称" maxlength="50" show-word-limit />
        </el-form-item>
        
        <el-form-item label="父分类" prop="parentId">
          <el-select v-model="currentCategory.parentId" placeholder="请选择父分类（可选）" clearable style="width: 100%">
            <el-option
              v-for="category in categoryOptions"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="currentCategory.sortOrder" :min="0" :max="9999" />
        </el-form-item>
        
        <el-form-item label="图标" prop="icon">
          <el-input v-model="currentCategory.icon" placeholder="请输入图标标识" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="categoryDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitCategoryForm" :loading="categorySubmitting">
            {{ isEditCategory ? '更新' : '创建' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { useImageUpload } from '@/composables/useImageUpload'
import SafeImage from '@/components/common/form/SafeImage.vue'

import { showByCode, isSuccess } from '@/utils/apiMessages'
import teaMessages from '@/utils/promptMessages'

export default {
  name: 'TeaManagePage',
  components: {
    Search,
    Plus,
    SafeImage
  },
  setup() {
    const router = useRouter()
    const store = useStore()
    const teaFormRef = ref(null)
    
    // 从Vuex获取状态
    const loading = computed(() => store.state.tea.loading)
    const teas = computed(() => store.state.tea.teaList)
    const totalCount = computed(() => store.state.tea.pagination.total)
    const currentPage = computed(() => store.state.tea.pagination.currentPage)
    const pageSize = computed(() => store.state.tea.pagination.pageSize)
    const categoryOptions = computed(() => store.state.tea.categories)
    const categories = computed(() => store.state.tea.categories)
    
    // 标签页状态
    const activeTab = ref('tea')
    
    // 筛选相关状态
    const searchQuery = ref('')
    const statusFilter = ref('')
    const categoryFilter = ref('')
    
    // 对话框相关状态
    const dialogVisible = ref(false)
    const isEdit = ref(false)
    const currentTea = ref(null)
    const submitting = ref(false)
    
    // 分类管理相关状态
    const categoryLoading = ref(false)
    const categoryDialogVisible = ref(false)
    const isEditCategory = ref(false)
    const currentCategory = ref({
      name: '',
      parentId: null,
      sortOrder: 999,
      icon: 'tea-icon-default'
    })
    const categorySubmitting = ref(false)
    const categoryFormRef = ref(null)
    
    // 分类表单验证规则
    const categoryRules = {
      name: [
        { required: true, message: '请输入分类名称', trigger: 'blur' },
        { min: 1, max: 50, message: '长度在1到50个字符', trigger: 'blur' }
      ],
      sortOrder: [
        { required: true, message: '请输入排序值', trigger: 'blur' },
        { type: 'number', min: 0, max: 9999, message: '排序值在0到9999之间', trigger: 'blur' }
      ]
    }
    
    const teaImages = ref([])
    const previewVisible = ref(false)
    const previewImage = ref('')
    const mainImageIndex = ref(0)
    const teaStatus = ref(0)
    
    // 任务组E：批量操作相关
    const selectedTeas = ref([])
    
    // 表单验证规则
    const teaRules = {
      name: [
        { required: true, message: '请输入茶叶名称', trigger: 'blur' },
        { min: 2, max: 100, message: '长度在2到100个字符', trigger: 'blur' }
      ],
      category_id: [
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
    
    // 分类数据从Vuex获取（已在computed中定义）
    
    // 图片上传相关
    const handleImageChange = (file, fileList) => {
      teaImages.value = fileList
    }
    
    const handleImageRemove = (file, fileList) => {
      teaImages.value = fileList
      
      // 如果删除的是主图，重置主图索引
      if (mainImageIndex.value >= teaImages.value.length) {
        mainImageIndex.value = teaImages.value.length > 0 ? 0 : -1
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
        spec_name: '',
        price: 0,
        stock: 0,
        is_default: currentTea.value.specifications.length === 0
      })
    }
    
    const removeSpec = index => {
      if (!currentTea.value || currentTea.value.specifications.length <= 1) return
      
      // 如果删除的是默认规格，将第一个规格设为默认
      if (currentTea.value.specifications[index].is_default) {
        currentTea.value.specifications[0].is_default = true
      }
      
      currentTea.value.specifications.splice(index, 1)
    }
    
    // 处理默认规格变更
    const handleDefaultChange = (val, index) => {
      if (!val) {
        // 不允许取消默认，必须有一个默认规格
        currentTea.value.specifications[index].is_default = true
        return
      }
      
      // 将其他规格设为非默认
      currentTea.value.specifications.forEach((spec, i) => {
        if (i !== index) {
          spec.is_default = false
        }
      })
    }
    
    // 格式化日期
    const formatDate = dateString => {
      if (!dateString) return '-'
      const date = new Date(dateString)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    }
    
    // 获取分类名称
    const getCategoryName = categoryId => {
      if (!categoryId) return '-'
      const category = categoryOptions.value.find(c => c.id === parseInt(categoryId))
      return category ? category.name : '-'
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
    
    // 显示添加茶叶对话框
    const showAddTeaDialog = () => {
      isEdit.value = false
      currentTea.value = {
        name: '',
        // 平台直售茶叶权限控制：管理员创建的茶叶shopId固定为'PLATFORM'
        // 权限规则：只有管理员(role=1)可以管理平台直售茶叶
        // shopId='PLATFORM'表示平台直售，区别于商家店铺茶叶(shopId为店铺ID)
        shop_id: 'PLATFORM',
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
      teaImages.value = []
      mainImageIndex.value = 0
      teaStatus.value = 0
      dialogVisible.value = true
    }
    
    // 处理编辑
    const handleEdit = async tea => {
      isEdit.value = true
      currentTea.value = JSON.parse(JSON.stringify(tea)) // 深拷贝避免直接修改列表数据
      
      // 任务组C：加载规格列表
      try {
        await store.dispatch('tea/fetchTeaSpecifications', tea.id)
        // 如果Vuex中有规格数据，使用Vuex的数据
        const specs = store.state.tea.currentTeaSpecs
        if (specs && specs.length > 0) {
          currentTea.value.specifications = specs.map(spec => ({
            id: spec.id,
            spec_name: spec.spec_name,
            price: spec.price,
            stock: spec.stock,
            is_default: spec.is_default === 1 || spec.is_default === true ? 1 : 0
          }))
        }
      } catch (error) {
        console.error('加载规格列表失败:', error)
        // 如果加载失败，使用tea中的规格数据
        if (!currentTea.value.specifications || currentTea.value.specifications.length === 0) {
          currentTea.value.specifications = [{
            id: Date.now(),
            spec_name: '默认规格',
            price: tea.price || 0,
            stock: tea.stock || 0,
            is_default: 1
          }]
        }
      }
      
      // 任务组D：设置图片列表（从tea.images获取）
      if (tea.images && tea.images.length > 0) {
        teaImages.value = tea.images.map(img => ({
          name: img.url?.split('/').pop() || 'image',
          url: img.url,
          is_main: img.is_main === 1 || img.is_main === true,
          uid: img.id,
          id: img.id
        }))
      } else {
        teaImages.value = []
      }
      
      // 设置主图索引
      const mainImageIdx = teaImages.value.findIndex(img => img.is_main)
      mainImageIndex.value = mainImageIdx >= 0 ? mainImageIdx : 0
      
      // 设置上架状态
      teaStatus.value = tea.status
      
      dialogVisible.value = true
    }
    
    // 处理预览
    const handlePreview = tea => {
      router.push(`/tea/${tea.id}`)
    }
    
    // 处理上下架状态切换
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
        
        const response = await store.dispatch('tea/toggleTeaStatus', {
          teaId: tea.id,
          status: newStatus
        })
        
        showByCode(response.code)
        
        // 刷新列表
        await loadTeas()
      } catch (error) {
        if (error !== 'cancel') {
          // 错误已由拦截器处理
          console.error('切换茶叶状态失败:', error)
        }
      }
    }
    
    // 任务组E：批量操作相关方法
    const handleSelectionChange = selection => {
      selectedTeas.value = selection
    }
    
    const handleBatchOnShelf = async () => {
      if (selectedTeas.value.length === 0) {
        teaMessages.prompt.showSelectOnShelf()
        return
      }
      
      try {
        await ElMessageBox.confirm(
          `确定要批量上架 ${selectedTeas.value.length} 个茶叶吗？`,
          '批量上架确认',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        
        const teaIds = selectedTeas.value.map(tea => tea.id)
        const response = await store.dispatch('tea/batchToggleTeaStatus', {
          teaIds,
          status: 1
        })
        
        showByCode(response.code)
        selectedTeas.value = []
        await loadTeas()
      } catch (error) {
        if (error !== 'cancel') {
          // 错误已由拦截器处理
          console.error('批量上架失败:', error)
        }
      }
    }
    
    const handleBatchOffShelf = async () => {
      if (selectedTeas.value.length === 0) {
        teaMessages.prompt.showSelectOffShelf()
        return
      }
      
      try {
        await ElMessageBox.confirm(
          `确定要批量下架 ${selectedTeas.value.length} 个茶叶吗？`,
          '批量下架确认',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        
        const teaIds = selectedTeas.value.map(tea => tea.id)
        const response = await store.dispatch('tea/batchToggleTeaStatus', {
          teaIds,
          status: 0
        })
        
        showByCode(response.code)
        selectedTeas.value = []
        await loadTeas()
      } catch (error) {
        if (error !== 'cancel') {
          // 错误已由拦截器处理
          console.error('批量下架失败:', error)
        }
      }
    }
    
    // 处理删除
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
        
        // 调用Vuex action删除茶叶
        const response = await store.dispatch('tea/deleteTea', tea.id)
        
        showByCode(response.code)
        
        // 如果当前页没有数据了，回到前一页
        if (teas.value.length === 0 && currentPage.value > 1) {
          await store.dispatch('tea/setPage', currentPage.value - 1)
        }
      } catch (error) {
        if (error !== 'cancel') {
          // 错误已由拦截器处理
          console.error('删除茶叶失败:', error)
        }
      }
    }
    
    // 处理保存
    const handleSave = () => {
      if (!teaFormRef.value) return
      
      teaFormRef.value.validate(async valid => {
        if (!valid) {
          teaMessages.prompt.showFormInvalid()
          return
        }
        
        // 校验规格
        if (!currentTea.value.specifications || currentTea.value.specifications.length === 0) {
          teaMessages.prompt.showSpecRequired()
          return
        }
        
        // 校验规格名称和价格
        const invalidSpec = currentTea.value.specifications.find(spec => 
          !spec.spec_name || spec.price <= 0
        )
        if (invalidSpec) {
          teaMessages.prompt.showSpecIncomplete()
          return
        }
        
        // 校验是否有默认规格
        const hasDefault = currentTea.value.specifications.some(spec => spec.is_default)
        if (!hasDefault) {
          teaMessages.prompt.showDefaultSpecRequired()
          return
        }
        
        // 校验图片
        if (teaImages.value.length === 0) {
          teaMessages.prompt.showImageRequired()
          return
        }
        
        submitting.value = true
        
        try {
          // 准备数据
          const formData = {
            ...currentTea.value,
            status: teaStatus.value,
            categoryId: parseInt(currentTea.value.category_id)
          }
          
          // 设置主图
          if (teaImages.value.length > 0 && mainImageIndex.value >= 0) {
            formData.mainImage = teaImages.value[mainImageIndex.value].url
            
            // 更新images中的is_main标记
            formData.images = teaImages.value.map((img, idx) => ({
              id: img.uid || Date.now() + idx,
              url: img.url,
              is_main: idx === mainImageIndex.value
            }))
          }
          
          // 计算总库存
          formData.stock = formData.specifications.reduce((sum, spec) => sum + spec.stock, 0)
          
          // 设置基础价格为默认规格的价格
          const defaultSpec = formData.specifications.find(spec => spec.is_default)
          if (defaultSpec) {
            formData.price = defaultSpec.price
          }
          
          // 调用Vuex action保存茶叶
          if (isEdit.value) {
            await store.dispatch('tea/updateTea', formData)
            
            // 任务组C：同步规格数据到后端
            const teaId = currentTea.value.id
            try {
              // 获取当前规格列表
              await store.dispatch('tea/fetchTeaSpecifications', teaId)
              const existingSpecs = store.state.tea.currentTeaSpecs || []
              
              // 处理规格的增删改
              for (const spec of formData.specifications) {
                const existingSpec = existingSpecs.find(s => s.id === spec.id)
                
                if (existingSpec) {
                  // 更新现有规格
                  await store.dispatch('tea/updateSpecification', {
                    teaId,
                    specId: spec.id,
                    specData: {
                      spec_name: spec.spec_name,
                      price: spec.price,
                      stock: spec.stock,
                      is_default: spec.is_default === 1 || spec.is_default === true ? 1 : 0
                    }
                  })
                  
                  // 如果设置为默认规格
                  if (spec.is_default === 1 || spec.is_default === true) {
                    await store.dispatch('tea/setDefaultSpecification', { teaId, specId: spec.id })
                  }
                } else {
                  // 添加新规格
                  await store.dispatch('tea/addSpecification', {
                    teaId,
                    specData: {
                      spec_name: spec.spec_name,
                      price: spec.price,
                      stock: spec.stock,
                      is_default: spec.is_default === 1 || spec.is_default === true ? 1 : 0
                    }
                  })
                }
              }
              
              // 删除不存在的规格
              const currentSpecIds = formData.specifications.map(s => s.id)
              for (const existingSpec of existingSpecs) {
                if (!currentSpecIds.includes(existingSpec.id)) {
                  await store.dispatch('tea/deleteSpecification', { teaId, specId: existingSpec.id })
                }
              }
            } catch (error) {
              console.error('同步规格数据失败:', error)
              // 规格同步失败不影响主流程，只记录错误
            }
            
            // 任务组D：同步图片数据到后端
            try {
              // 找出新上传的图片（File对象）
              const newFiles = teaImages.value.filter(img => img.raw instanceof File).map(img => img.raw)
              if (newFiles.length > 0) {
                await store.dispatch('tea/uploadTeaImages', { teaId, files: newFiles })
              }
              
              // 找出已删除的图片（有id但不在当前列表中的）
              const originalImages = currentTea.value.images || []
              const currentImageIds = teaImages.value.filter(img => img.id).map(img => img.id)
              for (const originalImg of originalImages) {
                if (originalImg.id && !currentImageIds.includes(originalImg.id)) {
                  await store.dispatch('tea/deleteTeaImage', { teaId, imageId: originalImg.id })
                }
              }
              
              // 更新图片顺序（如果有变化）
              const orders = teaImages.value.map((img, index) => ({
                imageId: img.id || img.uid,
                order: index + 1
              }))
              if (orders.length > 0) {
                await store.dispatch('tea/updateImageOrder', { teaId, orders })
              }
              
              // 设置主图
              if (mainImageIndex.value >= 0 && teaImages.value[mainImageIndex.value]) {
                const mainImageId = teaImages.value[mainImageIndex.value].id || teaImages.value[mainImageIndex.value].uid
                if (mainImageId) {
                  await store.dispatch('tea/setMainImage', { teaId, imageId: mainImageId })
                }
              }
            } catch (error) {
              console.error('同步图片数据失败:', error)
              // 图片同步失败不影响主流程，只记录错误
            }
            
            showByCode(response.code)
          } else {
            // 平台直售茶叶权限控制：管理员创建的茶叶shopId固定为'PLATFORM'
            // 权限规则：只有管理员(role=1)可以管理平台直售茶叶
            // shopId='PLATFORM'表示平台直售，区别于商家店铺茶叶(shopId为店铺ID)
            formData.shopId = 'PLATFORM'
            const result = await store.dispatch('tea/addTea', formData)
            const newTeaId = result.id || result.data?.id
            
            // 任务组C：添加新茶叶的规格
            if (newTeaId && formData.specifications) {
              try {
                for (const spec of formData.specifications) {
                  await store.dispatch('tea/addSpecification', {
                    teaId: newTeaId,
                    specData: {
                      spec_name: spec.spec_name,
                      price: spec.price,
                      stock: spec.stock,
                      is_default: spec.is_default === 1 || spec.is_default === true ? 1 : 0
                    }
                  })
                  
                  // 如果设置为默认规格
                  if (spec.is_default === 1 || spec.is_default === true) {
                    // 需要先获取规格ID，这里简化处理，假设后端返回的规格包含ID
                    await store.dispatch('tea/fetchTeaSpecifications', newTeaId)
                    const specs = store.state.tea.currentTeaSpecs || []
                    const addedSpec = specs.find(s => s.spec_name === spec.spec_name)
                    if (addedSpec) {
                      await store.dispatch('tea/setDefaultSpecification', { teaId: newTeaId, specId: addedSpec.id })
                    }
                  }
                }
              } catch (error) {
                console.error('添加规格失败:', error)
                // 规格添加失败不影响主流程
              }
            }
            
            // 任务组D：上传新茶叶的图片
            if (newTeaId && teaImages.value.length > 0) {
              try {
                const newFiles = teaImages.value.filter(img => img.raw instanceof File).map(img => img.raw)
                if (newFiles.length > 0) {
                  await store.dispatch('tea/uploadTeaImages', { teaId: newTeaId, files: newFiles })
                  
                  // 设置主图
                  if (mainImageIndex.value >= 0 && teaImages.value[mainImageIndex.value]) {
                    const uploadedImages = store.state.tea.teaImages || []
                    if (uploadedImages.length > 0 && mainImageIndex.value < uploadedImages.length) {
                      const mainImageId = uploadedImages[mainImageIndex.value].id
                      if (mainImageId) {
                        await store.dispatch('tea/setMainImage', { teaId: newTeaId, imageId: mainImageId })
                      }
                    }
                  }
                }
              } catch (error) {
                console.error('上传图片失败:', error)
                // 图片上传失败不影响主流程
              }
            }
            
            showByCode(result.code)
          }
          
          // 关闭对话框
          dialogVisible.value = false
          submitting.value = false
        } catch (error) {
          console.error('保存茶叶失败:', error)
          submitting.value = false
        }
      })
    }
    
    // 对话框关闭
    const handleDialogClose = done => {
      if (submitting.value) {
        teaMessages.prompt.showSubmitting()
        return
      }
      done()
    }
    
    // ==================== 分类管理相关方法 ====================
    
    // 显示添加分类对话框
    const showAddCategoryDialog = () => {
      isEditCategory.value = false
      currentCategory.value = {
        name: '',
        parentId: null,
        sortOrder: 999,
        icon: 'tea-icon-default'
      }
      categoryDialogVisible.value = true
    }
    
    // 编辑分类
    const handleEditCategory = category => {
      isEditCategory.value = true
      currentCategory.value = {
        id: category.id,
        name: category.name,
        parentId: category.parentId || null,
        sortOrder: category.sortOrder || 999,
        icon: category.icon || 'tea-icon-default'
      }
      categoryDialogVisible.value = true
    }
    
    // 删除分类
    const handleDeleteCategory = async category => {
      try {
        await ElMessageBox.confirm(
          `确定要删除分类"${category.name}"吗？删除后不可恢复。`,
          '确认删除',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        
        categoryLoading.value = true
        const response = await store.dispatch('tea/deleteCategory', category.id)
        showByCode(response.code)
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除分类失败:', error)
        }
      } finally {
        categoryLoading.value = false
      }
    }
    
    // 提交分类表单
    const submitCategoryForm = async () => {
      if (!categoryFormRef.value) return
      
      await categoryFormRef.value.validate(async valid => {
        if (!valid) return
        
        categorySubmitting.value = true
        try {
          const categoryData = {
            name: currentCategory.value.name,
            parentId: currentCategory.value.parentId,
            sortOrder: currentCategory.value.sortOrder,
            icon: currentCategory.value.icon
          }
          
          if (isEditCategory.value) {
            const response = await store.dispatch('tea/updateCategory', {
              id: currentCategory.value.id,
              categoryData
            })
            showByCode(response.code)
          } else {
            const response = await store.dispatch('tea/createCategory', categoryData)
            showByCode(response.code)
          }
          
          categoryDialogVisible.value = false
        } catch (error) {
          console.error('提交分类失败:', error)
        } finally {
          categorySubmitting.value = false
        }
      })
    }
    
    // 分类对话框关闭
    const handleCategoryDialogClose = done => {
      if (categorySubmitting.value) {
        teaMessages.prompt.showSubmitting()
        return
      }
      done()
    }
    
    // 加载茶叶数据
    const loadTeas = async () => {
      try {
        // 构建筛选条件
        const filters = {}
        if (searchQuery.value) {
          filters.keyword = searchQuery.value
        }
        if (statusFilter.value !== '') {
          filters.status = parseInt(statusFilter.value)
        }
        if (categoryFilter.value !== '') {
          filters.category = parseInt(categoryFilter.value)
        }
        
        // 更新Vuex filters并获取数据
        await store.dispatch('tea/updateFilters', filters)
      } catch (error) {
        console.error('加载茶叶列表失败:', error)
      }
    }
    
    // 搜索处理
    const handleSearch = async () => {
      await store.dispatch('tea/setPage', 1)
      await loadTeas()
    }
    
    // 筛选变更处理
    const handleFilterChange = async () => {
      await store.dispatch('tea/setPage', 1)
      await loadTeas()
    }
    
    // 页码变更
    const handlePageChange = async page => {
      await store.dispatch('tea/setPage', page)
    }
    
    // 每页条数变更
    const handleSizeChange = async size => {
      store.commit('tea/SET_PAGINATION', {
        ...store.state.tea.pagination,
        pageSize: size,
        currentPage: 1
      })
      await store.dispatch('tea/fetchTeas')
    }
    
    // 初始化
    onMounted(async () => {
      // 加载分类数据
      await store.dispatch('tea/fetchCategories')
      // 加载茶叶列表
      await loadTeas()
    })
    
    return {
      teaFormRef,
      loading, 
      searchQuery, 
      statusFilter, 
      categoryFilter, 
      currentPage, 
      pageSize, 
      totalCount, 
      teas, 
      categoryOptions, 
      dialogVisible, 
      isEdit, 
      currentTea, 
      submitting,
      teaImages,
      previewVisible,
      previewImage,
      mainImageIndex,
      teaStatus,
      teaRules,
      handleSearch, 
      handleFilterChange, 
      handlePageChange, 
      handleSizeChange, 
      handleEdit, 
      handlePreview, 
      handleToggleStatus, 
      showAddTeaDialog,
      handleSave, 
      handleDialogClose, 
      formatDate, 
      getCategoryName, 
      getMinPrice,
      handleImageChange,
      handleImageRemove,
      handleImagePreview,
      addSpec,
      removeSpec,
      handleDefaultChange,
      handleDelete,
      // 任务组E：批量操作
      selectedTeas,
      handleSelectionChange,
      handleBatchOnShelf,
      handleBatchOffShelf,
      loadTeas,
      // 分类管理相关
      categories,
      activeTab,
      categoryLoading,
      categoryDialogVisible,
      isEditCategory,
      currentCategory,
      categorySubmitting,
      categoryFormRef,
      categoryRules,
      showAddCategoryDialog,
      handleEditCategory,
      handleDeleteCategory,
      submitCategoryForm,
      handleCategoryDialogClose
    }
  }
}
</script>

<style lang="scss" scoped>
.tea-manage-page {
  padding: 20px;
  
  .page-header {
  display: flex;
    justify-content: space-between;
  align-items: center;
    margin-bottom: 24px;
    
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
  
  .search-filter-container {
    display: flex;
    justify-content: space-between;
    margin-bottom: 24px;
    
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
    background-color: #fff;
    border-radius: 4px;
    padding: 20px;
    margin-bottom: 24px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
    
    .price {
      color: var(--el-color-danger);
      font-weight: bold;
    }
    
    .price-tip {
      font-size: 12px;
      color: var(--el-text-color-secondary);
      margin-left: 2px;
    }
  }
  
  .pagination-container {
    display: flex;
    justify-content: center;
    margin-top: 24px;
  }
  
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
    
    .image-uploader {
      :deep(.el-upload--picture-card) {
        width: 120px;
        height: 120px;
        line-height: 120px;
      }
      
      :deep(.el-upload-list--picture-card .el-upload-list__item) {
        width: 120px;
        height: 120px;
      }
    }
    
    .main-image-option {
      display: flex;
      align-items: center;
    }
  }
}
</style>
