<template>
  <div class="tea-manage-page">
    <div class="page-header">
      <div class="header-title">
        <h1>平台茶叶管理</h1>
        <p class="sub-title">管理平台直售茶叶商品，支持添加、编辑、上架和下架操作</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="showAddTeaDialog">
          <el-icon><Plus /></el-icon>添加茶叶
        </el-button>
      </div>
    </div>

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
      >
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
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { useImageUpload } from '@/composables/useImageUpload'
import SafeImage from '@/components/common/form/SafeImage.vue'

/*
// 真实代码（开发UI时注释）
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { useImageUpload } from '@/composables/useImageUpload'
*/

export default {
  name: 'TeaManagePage',
  components: {
    Search,
    Plus,
    SafeImage
  },
  setup() {
    const router = useRouter()
    const teaFormRef = ref(null)
    
    // 列表相关状态
    const loading = ref(false)
    const searchQuery = ref('')
    const statusFilter = ref('')
    const categoryFilter = ref('')
    const currentPage = ref(1)
    const pageSize = ref(10)
    const totalCount = ref(0)
    const teas = ref([])
    
    // 对话框相关状态
    const dialogVisible = ref(false)
    const isEdit = ref(false)
    const currentTea = ref(null)
    const submitting = ref(false)
    const teaImages = ref([])
    const previewVisible = ref(false)
    const previewImage = ref('')
    const mainImageIndex = ref(0)
    const teaStatus = ref(0)
    
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
    
    const handleImagePreview = (file) => {
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
    
    const removeSpec = (index) => {
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
    const formatDate = (dateString) => {
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
    const getCategoryName = (categoryId) => {
      if (!categoryId) return '-'
      const category = categoryOptions.find(c => c.id === parseInt(categoryId))
      return category ? category.name : '-'
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
    
    // 显示添加茶叶对话框
    const showAddTeaDialog = () => {
      isEdit.value = false
      currentTea.value = {
        name: '',
        shop_id: 'PLATFORM', // 固定为平台直售
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
    const handleEdit = (tea) => {
      isEdit.value = true
      currentTea.value = JSON.parse(JSON.stringify(tea)) // 深拷贝避免直接修改列表数据
      
      // 设置图片列表
      teaImages.value = tea.images.map(img => ({
        name: img.url.split('/').pop(),
        url: img.url,
        is_main: img.is_main,
        uid: img.id
      }))
      
      // 设置主图索引
      const mainImageIdx = tea.images.findIndex(img => img.is_main)
      mainImageIndex.value = mainImageIdx >= 0 ? mainImageIdx : 0
      
      // 设置上架状态
      teaStatus.value = tea.status
      
      dialogVisible.value = true
    }
    
    // 处理预览
    const handlePreview = (tea) => {
      router.push(`/tea/${tea.id}`)
    }
    
    // 处理上下架状态切换
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
        loading.value = true
        setTimeout(() => {
          // 更新本地数据
          tea.status = newStatus
          
          // 提示成功
          ElMessage.success(`${action}成功`)
          loading.value = false
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
        loading.value = true
        setTimeout(() => {
          // 从列表中删除
          const index = teas.value.findIndex(t => t.id === tea.id)
          if (index !== -1) {
            teas.value.splice(index, 1)
          }
          
          // 提示成功
          ElMessage.success('删除成功')
          loading.value = false
          
          // 如果当前页没有数据了，回到前一页
          if (teas.value.length === 0 && currentPage.value > 1) {
            currentPage.value--
            loadTeas()
          }
        }, 500)
      }).catch(() => {
        // 用户取消操作
      })
    }
    
    // 处理保存
    const handleSave = () => {
      if (!teaFormRef.value) return
      
      teaFormRef.value.validate(async (valid) => {
        if (!valid) {
          ElMessage.warning('请正确填写表单内容')
          return
        }
        
        // 校验规格
        if (!currentTea.value.specifications || currentTea.value.specifications.length === 0) {
          ElMessage.warning('请至少添加一个规格')
          return
        }
        
        // 校验规格名称和价格
        const invalidSpec = currentTea.value.specifications.find(spec => 
          !spec.spec_name || spec.price <= 0
        )
        if (invalidSpec) {
          ElMessage.warning('请填写完整的规格名称和价格')
          return
        }
        
        // 校验是否有默认规格
        const hasDefault = currentTea.value.specifications.some(spec => spec.is_default)
        if (!hasDefault) {
          ElMessage.warning('请指定一个默认规格')
          return
        }
        
        // 校验图片
        if (teaImages.value.length === 0) {
          ElMessage.warning('请至少上传一张商品图片')
          return
        }
        
        submitting.value = true
        
        try {
          // 准备数据
          const formData = {
            ...currentTea.value,
            status: teaStatus.value
          }
          
          // 设置主图
          if (teaImages.value.length > 0 && mainImageIndex.value >= 0) {
            formData.main_image = teaImages.value[mainImageIndex.value].url
            
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
          
          // 模拟API调用
          setTimeout(() => {
            // 保存操作
            if (isEdit.value) {
              // 更新现有茶叶
              const index = teas.value.findIndex(t => t.id === formData.id)
              if (index !== -1) {
                teas.value[index] = formData
              }
              ElMessage.success('茶叶更新成功')
            } else {
              // 添加新茶叶
              formData.id = `TEA${Date.now().toString().substring(6)}`
              formData.create_time = new Date().toISOString()
              formData.update_time = new Date().toISOString()
              formData.sales = 0
              
              // 添加到列表首位
              teas.value.unshift(formData)
              
              ElMessage.success('茶叶添加成功')
            }
            
            // 关闭对话框
            dialogVisible.value = false
            submitting.value = false
            
            // 刷新列表
            loadTeas()
          }, 800)
        } catch (error) {
          ElMessage.error('操作失败：' + error.message)
          submitting.value = false
        }
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
    
    // 搜索处理
    const handleSearch = () => {
      currentPage.value = 1
      loadTeas()
    }
    
    // 筛选变更处理
    const handleFilterChange = () => {
      currentPage.value = 1
      loadTeas()
    }
    
    // 页码变更
    const handlePageChange = (page) => {
      currentPage.value = page
      loadTeas()
    }
    
    // 每页条数变更
    const handleSizeChange = (size) => {
      pageSize.value = size
      currentPage.value = 1
      loadTeas()
    }
    
    // 加载茶叶数据
    const loadTeas = () => {
      loading.value = true
      
      // 模拟API调用延迟
      setTimeout(() => {
        // 创建模拟数据
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
        teas.value = filteredTeas.slice(start, end)
        
        loading.value = false
      }, 600)
    }
    
    // 生成模拟数据
    const generateMockTeas = () => {
      const mockTeas = []
      
      for (let i = 1; i <= 10; i++) {
        const teaId = `TEA${i.toString().padStart(4, '0')}`
        const categoryId = Math.floor(Math.random() * 7) + 1
        const basePrice = Math.floor(Math.random() * 500) + 100
        const status = Math.random() > 0.3 ? 1 : 0 // 70%上架, 30%下架
        
        // 创建规格
        const specifications = [
          {
            id: i * 100 + 1,
            tea_id: teaId,
            spec_name: '特级 50g',
            price: basePrice,
            stock: Math.floor(Math.random() * 100) + 20,
            is_default: true
          },
          {
            id: i * 100 + 2,
            tea_id: teaId,
            spec_name: '特级 100g',
            price: basePrice * 1.8,
            stock: Math.floor(Math.random() * 80) + 10,
            is_default: false
          },
          {
            id: i * 100 + 3,
            tea_id: teaId,
            spec_name: '特级 200g 礼盒装',
            price: basePrice * 3.5,
            stock: Math.floor(Math.random() * 50) + 5,
            is_default: false
          }
        ]
        
        // 计算总库存
        const totalStock = specifications.reduce((sum, spec) => sum + spec.stock, 0)
        
        mockTeas.push({
          id: teaId,
          name: `商南特级${getCategoryName(categoryId)} - ${i}号`,
          shop_id: 'PLATFORM', // 作为平台直售茶叶
          category_id: categoryId,
          price: basePrice,
          description: `<p>商南特级${getCategoryName(categoryId)}，高山茶园精制而成，香气高雅持久，滋味醇厚。</p>`,
          detail: `<div class="tea-detail">
                    <h3>商南特级${getCategoryName(categoryId)}</h3>
                    <p>产于商南县，特点：条索紧结，香气高雅，滋味醇厚回甘。</p>
                    <p>冲泡方法：水温80-85℃，投茶量3-5克，冲泡时间30秒至1分钟。</p>
                  </div>`,
          origin: '商南县',
          stock: totalStock,
          sales: Math.floor(Math.random() * 500),
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
      
      return mockTeas
    }
    
    // 初始化
    onMounted(() => {
      loadTeas()
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
      handleDelete
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
