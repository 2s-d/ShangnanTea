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
                <el-upload
                  class="logo-uploader"
                  :show-file-list="false"
                  :on-success="handleLogoSuccess"
                  :before-upload="beforeLogoUpload"
                  :http-request="handleLogoUploadRequest"
                >
                  <SafeImage 
                    v-if="shop.logo || shop.shop_logo" 
                    :src="shop.logo || shop.shop_logo" 
                    type="banner" 
                    :alt="shop.name || shop.shop_name" 
                    class="shop-logo"
                  />
                  <el-icon v-else class="logo-uploader-icon"><Plus /></el-icon>
                </el-upload>
                <div class="logo-tip">点击上传Logo</div>
              </div>
            </div>
            
            <div class="shop-info-right">
              <h3 class="shop-name">{{ shop.name || shop.shop_name }}</h3>
              
              <div class="shop-stats">
                <div class="stat-item">
                  <span class="stat-value">{{ shop.sales_count || shop.salesCount || 0 }}</span>
                  <span class="stat-label">销量</span>
                </div>
                <div class="stat-item">
                  <span class="stat-value">{{ shop.rating || '0.0' }}</span>
                  <span class="stat-label">评分</span>
                </div>
                <div class="stat-item">
                  <span class="stat-value">{{ shop.follower_count || shop.followerCount || 0 }}</span>
                  <span class="stat-label">关注数</span>
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
                <div class="overview-label">访问量</div>
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

        <!-- 任务组B：Banner管理部分 -->
        <el-card class="banner-manage-card">
          <template #header>
            <div class="card-header">
              <span>Banner管理</span>
              <el-button type="primary" size="small" @click="showAddBannerDialog">
                <el-icon><Plus /></el-icon> 添加Banner
              </el-button>
            </div>
          </template>
          
          <div class="banner-manage-content" v-loading="bannerLoading">
            <div v-if="shopBanners.length === 0" class="empty-banner">
              <el-empty description="暂无Banner，点击上方按钮添加" />
            </div>
            
            <div v-else class="banner-list">
              <div 
                v-for="(banner, index) in shopBanners" 
                :key="banner.id" 
                class="banner-item"
              >
                <div class="banner-preview">
                  <SafeImage 
                    :src="banner.image_url" 
                    type="banner" 
                    :alt="banner.title"
                    style="width: 100%; height: 150px; object-fit: cover; border-radius: 4px;"
                  />
                  <div class="banner-info">
                    <div class="banner-title">{{ banner.title || '未命名Banner' }}</div>
                    <div class="banner-link">{{ banner.link_url || '无链接' }}</div>
                    <div class="banner-order">排序：{{ banner.order || index + 1 }}</div>
                  </div>
                </div>
                
                <div class="banner-actions">
                  <el-button 
                    type="primary" 
                    link 
                    size="small"
                    @click="showEditBannerDialog(banner)"
                  >
                    编辑
                  </el-button>
                  <el-button 
                    type="danger" 
                    link 
                    size="small"
                    @click="handleDeleteBanner(banner)"
                  >
                    删除
                  </el-button>
                  <div class="banner-order-controls">
                    <el-button 
                      type="info" 
                      link 
                      size="small"
                      :disabled="index === 0"
                      @click="moveBanner(index, 'up')"
                    >
                      ↑
                    </el-button>
                    <el-button 
                      type="info" 
                      link 
                      size="small"
                      :disabled="index === shopBanners.length - 1"
                      @click="moveBanner(index, 'down')"
                    >
                      ↓
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 任务组B：公告管理部分 -->
        <el-card class="announcement-manage-card">
          <template #header>
            <div class="card-header">
              <span>公告管理</span>
              <el-button type="primary" size="small" @click="showAddAnnouncementDialog">
                <el-icon><Plus /></el-icon> 添加公告
              </el-button>
            </div>
          </template>
          
          <div class="announcement-manage-content" v-loading="announcementLoading">
            <div v-if="shopAnnouncements.length === 0" class="empty-announcement">
              <el-empty description="暂无公告，点击上方按钮添加" />
            </div>
            
            <div v-else class="announcement-list">
              <div 
                v-for="announcement in shopAnnouncements" 
                :key="announcement.id" 
                class="announcement-item"
                :class="{ 'is-top': announcement.is_top }"
              >
                <div class="announcement-header">
                  <div class="announcement-title">
                    <el-tag v-if="announcement.is_top" type="warning" size="small">置顶</el-tag>
                    <span>{{ announcement.title }}</span>
                  </div>
                  <div class="announcement-time">
                    {{ formatTime(announcement.create_time) }}
                  </div>
                </div>
                
                <div class="announcement-content">
                  {{ announcement.content }}
                </div>
                
                <div class="announcement-actions">
                  <el-button 
                    type="primary" 
                    link 
                    size="small"
                    @click="showEditAnnouncementDialog(announcement)"
                  >
                    编辑
                  </el-button>
                  <el-button 
                    type="danger" 
                    link 
                    size="small"
                    @click="handleDeleteAnnouncement(announcement)"
                  >
                    删除
                  </el-button>
                </div>
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

    <!-- 任务组B：Banner表单对话框 -->
    <el-dialog
      v-model="bannerDialogVisible"
      :title="isEditBanner ? '编辑Banner' : '添加Banner'"
      width="600px"
      destroy-on-close
    >
      <el-form 
        ref="bannerFormRef" 
        :model="currentBanner" 
        label-width="100px"
        v-if="currentBanner"
      >
        <el-form-item label="Banner标题" prop="title">
          <el-input v-model="currentBanner.title" placeholder="请输入Banner标题" maxlength="50" />
        </el-form-item>
        
        <el-form-item label="图片URL" prop="image_url">
          <el-input v-model="currentBanner.image_url" placeholder="请输入图片URL" />
        </el-form-item>
        
        <el-form-item label="链接地址" prop="link_url">
          <el-input v-model="currentBanner.link_url" placeholder="请输入链接地址（可选）" />
        </el-form-item>
        
        <el-form-item label="排序" prop="order">
          <el-input-number v-model="currentBanner.order" :min="1" :max="10" />
        </el-form-item>
        
        <el-form-item label="启用状态" prop="is_enabled">
          <el-switch v-model="currentBanner.is_enabled" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="bannerDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveBanner">保存</el-button>
      </template>
    </el-dialog>

    <!-- 任务组B：公告表单对话框 -->
    <el-dialog
      v-model="announcementDialogVisible"
      :title="isEditAnnouncement ? '编辑公告' : '添加公告'"
      width="700px"
      destroy-on-close
    >
      <el-form 
        ref="announcementFormRef" 
        :model="currentAnnouncement" 
        label-width="100px"
        v-if="currentAnnouncement"
      >
        <el-form-item label="公告标题" prop="title">
          <el-input v-model="currentAnnouncement.title" placeholder="请输入公告标题" maxlength="100" />
        </el-form-item>
        
        <el-form-item label="公告内容" prop="content">
          <el-input
            v-model="currentAnnouncement.content"
            type="textarea"
            :rows="6"
            placeholder="请输入公告内容"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="置顶" prop="is_top">
          <el-switch v-model="currentAnnouncement.is_top" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="announcementDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveAnnouncement">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessageBox } from 'element-plus'
import { Setting, Plus, Search } from '@element-plus/icons-vue'
import SafeImage from '@/components/common/form/SafeImage.vue'
import { API } from '@/api/apiConstants'

import { showByCode, isSuccess } from '@/utils/apiMessages'
import { shopPromptMessages } from '@/utils/promptMessages'

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
    const store = useStore()
    
    // 从Vuex获取店铺信息
    const loading = computed(() => store.state.shop.loading)
    const shop = computed(() => store.state.shop.myShop)
    const defaultLogo = '/mock-images/shop-default.jpg'
    const defaultTeaImage = '/mock-images/tea-default.jpg'
    
    // 茶叶列表相关
    const teaLoading = ref(false)
    const searchQuery = ref('')
    const statusFilter = ref('')
    const categoryFilter = ref('')
    const currentPage = ref(1)
    const pageSize = ref(10)
    const shopTeas = computed(() => store.state.shop.shopTeas || [])
    const totalCount = computed(() => shopTeas.value.length) // 暂时使用列表长度，后续可添加分页
    
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
    
    // 任务组0：使用Vuex加载店铺信息
    const loadShopInfo = async () => {
      try {
        await store.dispatch('shop/fetchMyShop')
      } catch (error) {
        console.error('加载店铺信息失败:', error)
      }
    }
    
    // 任务组0：使用Vuex加载店铺茶叶列表
    const loadShopTeas = async () => {
      if (!shop.value || !shop.value.id) {
        shopPromptMessages.showShopInfoLoadFirst()
        return
      }
      
      teaLoading.value = true
      try {
        const params = {
          page: currentPage.value,
          size: pageSize.value
        }
        
        if (statusFilter.value) {
          params.status = statusFilter.value
        }
        
        if (categoryFilter.value) {
          params.categoryId = categoryFilter.value
        }
        
        if (searchQuery.value) {
          params.keyword = searchQuery.value
        }
        
        await store.dispatch('shop/fetchShopTeas', {
          shopId: shop.value.id,
          params
        })
      } catch (error) {
        console.error('加载茶叶列表失败:', error)
      } finally {
        teaLoading.value = false
      }
    }
    
    // 处理编辑
    const handleEdit = tea => {
      isEdit.value = true
      currentTea.value = JSON.parse(JSON.stringify(tea)) // 深拷贝避免直接修改列表数据
      dialogVisible.value = true
    }
    
    // 处理状态切换
    // 任务组C：处理状态切换（使用Vuex）
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
        const response = await store.dispatch('shop/toggleShopTeaStatus', {
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
    
    // 任务组C：处理删除（使用Vuex）
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
        const response = await store.dispatch('shop/deleteShopTea', {
          teaId: tea.id,
          shopId: shop.value?.id
        })
        showByCode(response.code)
        
        // 如果当前页没有数据了，回到前一页
        if (shopTeas.value.length === 0 && currentPage.value > 1) {
          currentPage.value--
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
        shopPromptMessages.showSubmittingWait()
        return
      }
      done()
    }
    
    // 任务组C：保存处理（使用Vuex）
    const handleSave = async () => {
      if (!shop.value || !shop.value.id) {
        console.error('店铺信息不存在')
        return
      }
      
      submitting.value = true
      try {
        const teaData = {
          ...currentTea.value,
          shop_id: shop.value.id
        }
        
        if (isEdit.value) {
          // 更新茶叶
          const response = await store.dispatch('shop/updateShopTea', {
            teaId: currentTea.value.id,
            teaData
          })
          showByCode(response.code)
        } else {
          // 添加茶叶
          const response = await store.dispatch('shop/addShopTea', {
            shopId: shop.value.id,
            teaData
          })
          showByCode(response.code)
        }
        
        // 关闭对话框
        dialogVisible.value = false
        // 重新加载列表
        await loadShopTeas()
      } catch (error) {
        console.error('保存茶叶失败:', error)
      } finally {
        submitting.value = false
      }
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
      currentPage.value = 1
      await loadShopTeas()
    }
    
    // 任务组C：筛选变更处理
    const handleFilterChange = async () => {
      currentPage.value = 1
      await loadShopTeas()
    }
    
    // 任务组C：页码变更
    const handlePageChange = async page => {
      currentPage.value = page
      await loadShopTeas()
    }
    
    // 任务组C：每页条数变更
    const handleSizeChange = async size => {
      pageSize.value = size
      currentPage.value = 1
      await loadShopTeas()
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
    const removeSpec = index => {
      if (!currentTea.value || currentTea.value.specifications.length <= 1) return
      
      currentTea.value.specifications.splice(index, 1)
      
      // 如果删除了默认规格，则将第一个规格设为默认
      if (!currentTea.value.specifications.some(spec => spec.is_default)) {
        currentTea.value.specifications[0].is_default = true
      }
    }
    
    // 处理默认规格变更
    const handleDefaultChange = changedIndex => {
      if (!currentTea.value) return
      
      // 只能有一个默认规格，将其他规格设为非默认
      currentTea.value.specifications.forEach((spec, index) => {
        if (index !== changedIndex) {
          spec.is_default = false
        }
      })
    }
    
    // 任务组B：Banner管理相关
    const bannerLoading = ref(false)
    const shopBanners = computed(() => store.state.shop.shopBanners || [])
    const bannerDialogVisible = ref(false)
    const currentBanner = ref(null)
    const isEditBanner = ref(false)
    const bannerFormRef = ref(null)
    
    // 加载Banner列表
    const loadBanners = async () => {
      if (!shop.value || !shop.value.id) return
      
      bannerLoading.value = true
      try {
        const response = await store.dispatch('shop/fetchShopBanners', shop.value.id)
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
          response = await store.dispatch('shop/updateBanner', {
            bannerId: currentBanner.value.id,
            bannerData
          })
        } else {
          response = await store.dispatch('shop/uploadBanner', {
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
        
        const response = await store.dispatch('shop/deleteBanner', {
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
        const response = await store.dispatch('shop/updateBannerOrder', {
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
    const shopAnnouncements = computed(() => store.state.shop.shopAnnouncements || [])
    const announcementDialogVisible = ref(false)
    const currentAnnouncement = ref(null)
    const isEditAnnouncement = ref(false)
    const announcementFormRef = ref(null)
    
    // 加载公告列表
    const loadAnnouncements = async () => {
      if (!shop.value || !shop.value.id) return
      
      announcementLoading.value = true
      try {
        const response = await store.dispatch('shop/fetchShopAnnouncements', shop.value.id)
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
        is_top: 0
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
          is_top: currentAnnouncement.value.is_top ? 1 : 0
        }
        
        let response
        if (isEditAnnouncement.value) {
          response = await store.dispatch('shop/updateAnnouncement', {
            announcementId: currentAnnouncement.value.id,
            announcementData
          })
        } else {
          response = await store.dispatch('shop/createAnnouncement', {
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
        
        const response = await store.dispatch('shop/deleteAnnouncement', announcement.id)
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
    const shopStatistics = computed(() => store.state.shop.shopStatistics)
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
        const response = await store.dispatch('shop/fetchShopStatistics', {
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
        await store.dispatch('shop/fetchMyShop')
      } else {
        showByCode(response.code || 4113) // Logo上传失败
      }
    }

    // 任务组E：通过Vuex Action处理Logo上传，遵循组件 → Vuex → API 数据流
    const handleLogoUploadRequest = async param => {
      if (!shop.value || !shop.value.id) {
        showByCode(4103) // 店铺信息不存在
        param.onError(new Error('店铺信息不存在'))
        return
      }
      try {
        const res = await store.dispatch('shop/uploadShopLogo', {
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
    
    onMounted(() => {
      loadShopInfo().then(() => {
        loadShopTeas()
        loadBanners()
        loadAnnouncements()
        loadStatistics()
      })
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

      // 任务组E：Logo上传
      beforeLogoUpload,
      handleLogoSuccess,
      handleLogoUploadRequest,
      
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
      handleDefaultChange,
      // 任务组B：Banner管理
      bannerLoading,
      shopBanners,
      bannerDialogVisible,
      currentBanner,
      isEditBanner,
      bannerFormRef,
      showAddBannerDialog,
      showEditBannerDialog,
      handleSaveBanner,
      handleDeleteBanner,
      moveBanner,
      // 任务组B：公告管理
      announcementLoading,
      shopAnnouncements,
      announcementDialogVisible,
      currentAnnouncement,
      isEditAnnouncement,
      announcementFormRef,
      showAddAnnouncementDialog,
      showEditAnnouncementDialog,
      handleSaveAnnouncement,
      handleDeleteAnnouncement,
      formatTime,
      // 任务组D：统计数据
      statisticsLoading,
      shopStatistics,
      dateRange,
      handleDateRangeChange,
      formatMoney
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