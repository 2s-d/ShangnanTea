<template>
  <div class="tea-list-page">
    <div class="page-header">
      <div class="container">
        <div class="header-content">
          <h1 class="page-title">茶叶商城</h1>
          <p class="page-description">精选商南特色茶品，邀您共享醇香茶韵</p>
          
          <div class="header-toolbar">
            <div class="left-area">
              <div class="search-area">
                <SearchBar 
                  search-type="tea"
                  :model-value="searchQuery"
                  placeholder="搜索茶叶名称"
                  @update:model-value="searchQuery = $event"
                  @search="handleSearchFromBar"
                />
              </div>
              
          <el-button type="primary" @click="goToShopList">
            <el-icon><Shop /></el-icon>
            商铺列表
            </el-button>
        </div>
            
            <!-- 排序工具栏移到这里 -->
            <div class="sorting-options">
              <el-radio-group v-model="sortOption" size="small" @change="handleSortChange">
                <el-radio-button label="default">综合排序</el-radio-button>
                <el-radio-button label="sales">销量优先</el-radio-button>
                <el-radio-button label="price_asc">价格从低到高</el-radio-button>
                <el-radio-button label="price_desc">价格从高到低</el-radio-button>
                <el-radio-button label="newest">最新上架</el-radio-button>
              </el-radio-group>
              
              <div class="view-toggle">
                <el-switch
                  v-model="viewMode"
                  active-value="grid"
                  inactive-value="list"
                  active-text="网格"
                  inactive-text="列表"
                />
              </div>
            </div>
          </div>
        </div>
                </div>
              </div>
              
    <div class="container main-content">
      <div class="tea-list-container">
        <!-- 筛选栏 -->
        <div class="filter-sidebar">
          <div class="filter-card">
            <h3 class="filter-title">茶叶分类</h3>
            <el-checkbox-group v-model="filters.categories" @change="applyFilters">
              <el-checkbox v-for="category in categoryOptions" :key="category.id" :label="category.id">
                {{ category.name }}
              </el-checkbox>
            </el-checkbox-group>
          </div>
          
          <div class="filter-card">
            <h3 class="filter-title">价格区间</h3>
            <div class="price-range">
              <el-slider
                v-model="filters.priceRange"
                range
                :min="0"
                :max="1000"
                :step="10"
                @change="applyFilters"
              />
              <div class="price-inputs">
                <el-input-number 
                  v-model="filters.priceRange[0]" 
                  :min="0" 
                  :max="filters.priceRange[1]" 
                  size="small" 
                  controls-position="right"
                  @change="applyFilters"
                />
                <span class="price-separator">-</span>
                <el-input-number 
                  v-model="filters.priceRange[1]" 
                  :min="filters.priceRange[0]" 
                  :max="1000" 
                  size="small" 
                  controls-position="right"
                  @change="applyFilters"
                />
              </div>
            </div>
          </div>
          
          <div class="filter-card">
            <h3 class="filter-title">商品来源</h3>
            <el-radio-group v-model="filters.source" @change="applyFilters">
              <el-radio label="all">全部</el-radio>
              <el-radio label="platform">平台直售</el-radio>
              <el-radio label="shop">商家店铺</el-radio>
            </el-radio-group>
          </div>
          
          <div class="filter-card">
            <h3 class="filter-title">评分筛选</h3>
            <el-radio-group v-model="filters.rating" @change="applyFilters">
              <el-radio :label="null">全部</el-radio>
              <el-radio :label="4.5">4.5分以上</el-radio>
              <el-radio :label="4.0">4.0分以上</el-radio>
              <el-radio :label="3.5">3.5分以上</el-radio>
            </el-radio-group>
          </div>
          
          <div class="filter-card">
            <el-button type="default" @click="resetFilters" style="width: 100%">重置筛选</el-button>
          </div>
        </div>
        
        <!-- 茶叶列表 -->
        <div class="tea-content">
          <!-- 茶叶列表展示 -->
          <div v-if="loading" class="loading-container">
            <el-skeleton :rows="3" animated />
            <el-skeleton :rows="3" animated />
            <el-skeleton :rows="3" animated />
          </div>
          
          <template v-else>
            <div :class="['tea-list', `view-${viewMode}`]" v-if="teas.length > 0">
              <div v-for="tea in teas" :key="tea.id" class="tea-item">
                <tea-card :tea="tea" />
              </div>
            </div>
            
            <div v-else class="empty-result">
              <el-empty description="未找到符合条件的茶叶" />
            </div>
          </template>
          
          <!-- 分页 -->
          <div class="pagination-container">
            <el-pagination
              background
              layout="prev, pager, next, jumper"
              :total="totalCount"
              :page-size="pageSize"
              :current-page="currentPage"
              @current-change="handlePageChange"
            />
          </div>
        </div>
      </div>
      
      <!-- 任务组F：热门推荐 -->
      <div class="recommend-section" v-if="popularTeas.length > 0">
        <h2 class="section-title">热门推荐</h2>
        <div class="recommend-list">
          <TeaCard 
            v-for="tea in popularTeas" 
            :key="tea.id" 
            :tea="tea"
            @click="goToTeaDetail(tea.id)"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useStore } from 'vuex'

import { Search, Shop } from '@element-plus/icons-vue'
import TeaCard from '@/components/tea/card/TeaCard.vue'

import SearchBar from '@/components/common/SearchBar.vue'
import { showByCode, isSuccess } from '@/utils/apiMessages'
import teaMessages from '@/utils/promptMessages'

export default {
  name: 'TeaListPage',
  components: {
    Shop,
    TeaCard,
    SearchBar
  },
  setup() {
    const store = useStore()
    const router = useRouter()
    const route = useRoute()

    const searchQuery = ref('')
    const sortOption = ref('default')
    const viewMode = ref('grid')
    const currentPage = ref(1)
    
    // 筛选选项
    const filters = reactive({
      categories: [],
      priceRange: [0, 1000],
      source: 'all',
      rating: null
    })
    
    // 从store获取分类数据
    const categoryOptions = computed(() => store.state.tea.categories)
    
    // 从store获取茶叶列表
    const teas = computed(() => store.state.tea.teaList)
    const totalCount = computed(() => store.state.tea.pagination.total)
    const pageSize = computed(() => store.state.tea.pagination.pageSize)
    const loading = computed(() => store.state.tea.loading)
    
    // 加载茶叶数据（提前定义，避免 watch 中调用时未定义）
    const loadTeas = async () => {
      try {
        // 解析排序选项
        let sortBy = ''
        let sortOrder = 'asc'
        if (sortOption.value === 'sales') {
          sortBy = 'sales'
          sortOrder = 'desc'
        } else if (sortOption.value === 'price_asc') {
          sortBy = 'price'
          sortOrder = 'asc'
        } else if (sortOption.value === 'price_desc') {
          sortBy = 'price'
          sortOrder = 'desc'
        } else if (sortOption.value === 'newest') {
          sortBy = 'time'
          sortOrder = 'desc'
        }
        
        // 将UI筛选条件映射到 store.filters
        // 任务组E：只显示上架茶叶（status=1）
        await store.dispatch('tea/updateFilters', {
          keyword: searchQuery.value,
          // 当前 store 只支持单个 category 字段，这里用"第一个选中分类"作为查询条件
          category: filters.categories.length > 0 ? filters.categories[0] : '',
          priceRange: filters.priceRange,
          origin: '', // 暂时不支持产地筛选
          rating: filters.rating,
          sortBy: sortBy,
          sortOrder: sortOrder,
          status: 1 // 任务组E：只显示上架茶叶
        })
      } catch (error) {
        // TODO: 迁移到新消息系统 - 使用 showByCode(response.code)

        // TODO: [tea] 迁移到 showByCode(response.code) - error
        teaMessages.error.showListFailed(error.message)
      }
    }
    
    // 同步筛选条件到URL
    const updateQueryParams = () => {
      const query = {
        page: currentPage.value,
        sort: sortOption.value
      }
      
      if (searchQuery.value) query.search = searchQuery.value
      if (filters.categories.length > 0) query.categories = JSON.stringify(filters.categories)
      if (filters.priceRange[0] !== 0 || filters.priceRange[1] !== 1000) {
        query.price = JSON.stringify(filters.priceRange)
      }
      if (filters.source !== 'all') query.source = filters.source
      if (filters.rating !== null) query.rating = filters.rating
      
      router.replace({ query })
    }
    
    // 监听路由参数变化
    watch(
      () => route.query,
      query => {
        // 从URL参数更新筛选条件
        if (query.search) searchQuery.value = query.search
        if (query.sort) sortOption.value = query.sort
        if (query.page) currentPage.value = parseInt(query.page)
        if (query.categories) {
          try {
            filters.categories = JSON.parse(query.categories)
          } catch (e) {
            filters.categories = []
          }
        }
        if (query.price) {
          try {
            filters.priceRange = JSON.parse(query.price)
          } catch (e) {
            filters.priceRange = [0, 1000]
          }
        }
        if (query.source) filters.source = query.source
        if (query.rating !== undefined && query.rating !== null && query.rating !== '') {
          filters.rating = parseFloat(query.rating)
        } else {
          filters.rating = null
        }
        
        loadTeas()
      },
      { immediate: true }
    )
    
    // 页面跳转
    const goToShopList = () => {
      router.push('/shop/list')
    }
    
    // 搜索处理（原有方法，保留用于兼容）
    const handleSearch = () => {
      currentPage.value = 1
      updateQueryParams()
    }
    
    // 统一搜索组件的搜索处理
    const handleSearchFromBar = ({ query, type }) => {
      // SearchBar已经处理了跳转，这里只需要更新本地状态
      searchQuery.value = query
      // 由于SearchBar已经跳转到 /tea/mall?search=xxx，watch会自动触发数据加载
    }
    
    // 应用筛选 - 直接应用，不再需要按钮
    const applyFilters = () => {
      currentPage.value = 1
      updateQueryParams()
    }
    
    // 重置筛选
    const resetFilters = async () => {
      filters.categories = []
      filters.priceRange = [0, 1000]
      filters.source = 'all'
      filters.rating = null
      searchQuery.value = ''
      sortOption.value = 'default'
      currentPage.value = 1
      
      // 重置Vuex筛选条件
      await store.dispatch('tea/resetFilters')
      
      // 清空URL参数
      router.replace({ query: {} })
    }
    
    // 页面变化
    const handlePageChange = page => {
      currentPage.value = page
      updateQueryParams()
      store.dispatch('tea/setPage', page)
    }
    
    // 处理排序变更
    const handleSortChange = () => {
      currentPage.value = 1
      updateQueryParams()
      loadTeas()
    }
    
    // 任务组F：热门推荐数据
    const popularTeas = computed(() => store.state.tea.recommendTeas || [])
    
    // 任务组F：跳转到茶叶详情页
    const goToTeaDetail = teaId => {
      router.push(`/tea/${teaId}`)
    }
    
    // 任务组F：加载热门推荐
    const loadPopularTeas = async () => {
      try {
        await store.dispatch('tea/fetchRecommendTeas', { type: 'popular', count: 6 })
      } catch (error) {
        console.error('加载热门推荐失败:', error)
      }
    }
    
    // 初始化
    onMounted(async () => {
      await store.dispatch('tea/fetchCategories')
      // 如果URL中有查询参数，会触发watch自动加载
      // 如果没有查询参数，需要手动加载一次
      if (!route.query.page && !route.query.search && !route.query.categories) {
        await loadTeas()
      }
      // 任务组F：加载热门推荐
      await loadPopularTeas()
    })
    
    return {
      searchQuery,
      sortOption,
      viewMode,
      filters,
      loading,
      teas,
      currentPage,
      pageSize,
      totalCount,
      categoryOptions,
      goToShopList,
      handleSearch,
      handleSearchFromBar,
      applyFilters,
      resetFilters,
      handlePageChange,
      handleSortChange,
      // 任务组F：热门推荐
      popularTeas,
      goToTeaDetail
    }
  }
}
</script>

<style lang="scss" scoped>
.tea-list-page {
  min-height: 100vh;
    background-color: #f5f7fa;
  
  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 15px;
  }
  
  .page-header {
    background-color: #fff;
    padding: 30px 0;
    margin-bottom: 20px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
    position: sticky;
    top: 0;
    z-index: 10;
    
    .header-content {
      display: flex;
      flex-direction: column;
    
    .page-title {
      font-size: 28px;
      font-weight: 600;
      margin: 0 0 10px;
      color: var(--el-text-color-primary);
    }
    
    .page-description {
      font-size: 16px;
      color: var(--el-text-color-secondary);
      margin-bottom: 20px;
    }
    
      .header-toolbar {
        display: flex;
        justify-content: space-between;
        align-items: center;
        flex-wrap: wrap;
        gap: 15px;
        
        .left-area {
          display: flex;
          align-items: center;
          gap: 15px;
          
          .search-area {
            width: 300px;
            
            .search-input {
              width: 100%;
            }
          }
      
      .el-button {
        padding: 9px 15px;
        font-size: 14px;
        line-height: 1.5;
        border-radius: 4px;
        font-weight: 500;
        height: 40px;
        
        .el-icon {
          margin-right: 5px;
            }
          }
        }
        
        .sorting-options {
          display: flex;
          align-items: center;
          gap: 15px;
          
          .view-toggle {
            margin-left: 10px;
          }
        }
      }
    }
  }
  
  .main-content {
    margin-bottom: 40px;
    
    .tea-list-container {
      display: flex;
      gap: 20px;
      
      .filter-sidebar {
        width: 250px;
        flex-shrink: 0;
        position: sticky;
        top: 163px; /* 调整距离，确保在顶部导航下方 */
        align-self: flex-start;
        
        .filter-card {
          background-color: #fff;
          border-radius: 8px;
          padding: 16px;
          margin-bottom: 16px;
          box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
          
          .filter-title {
            margin: 0 0 16px 0;
            font-size: 16px;
            font-weight: 600;
            color: var(--el-text-color-primary);
            border-bottom: 1px solid var(--el-border-color-lighter);
            padding-bottom: 8px;
          }
          
          .el-checkbox-group {
            display: flex;
            flex-direction: column;
            gap: 8px;
          }
          
          .el-radio-group {
      display: flex;
      flex-direction: column;
            gap: 8px;
          }
          
          .price-range {
            .price-inputs {
              display: flex;
      align-items: center;
              margin-top: 16px;
              
              .price-separator {
                margin: 0 8px;
              }
              
              .el-input-number {
                width: 100%;
              }
            }
          }
        }
      }
      
      .tea-content {
        flex: 1;
        
        .loading-container {
          background-color: #fff;
          border-radius: 8px;
          padding: 20px;
          box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
          
          .el-skeleton {
            margin-bottom: 20px;
          }
        }
        
        .empty-result {
          background-color: #fff;
          border-radius: 8px;
          padding: 40px 20px;
          box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
      text-align: center;
        }
        
        .tea-list {
          margin-bottom: 20px;
          
          &.view-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
            gap: 20px;
            
            .tea-item {
              height: 100%;
            }
          }
          
          &.view-list {
            display: flex;
            flex-direction: column;
            gap: 15px;
            
            .tea-item {
      background-color: #fff;
      border-radius: 8px;
              box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
            }
          }
        }
        
        .pagination-container {
          display: flex;
          justify-content: center;
          margin-top: 30px;
        }
      }
      
      // 任务组F：热门推荐样式
      .recommend-section {
        margin-top: 40px;
        padding: 30px;
        background-color: #fff;
        border-radius: 8px;
        box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
        
        .section-title {
          font-size: 20px;
          font-weight: 600;
          color: #303133;
          margin-bottom: 20px;
          padding-bottom: 10px;
          border-bottom: 2px solid #409eff;
        }
        
        .recommend-list {
          display: grid;
          grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
          gap: 20px;
        }
      }
    }
  }
}

@media (max-width: 992px) {
  .tea-list-page {
    .page-header {
      position: relative; /* 移动端取消固定位置 */
      
      .header-toolbar {
        flex-direction: column;
        align-items: stretch;
        
        .left-area {
          margin-bottom: 15px;
        }
      }
    }
    
    .main-content {
      .tea-list-container {
        flex-direction: column;
        
        .filter-sidebar {
          width: 100%;
          position: static;
        margin-bottom: 20px;
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .tea-list-page {
    .page-header {
      .header-content {
        .header-toolbar {
          .left-area {
            flex-direction: column;
            align-items: stretch;
            
            .search-area {
              width: 100%;
              margin-bottom: 10px;
            }
            
            .el-button {
              width: 100%;
            }
          }
          
          .sorting-options {
            flex-direction: column;
            align-items: stretch;
            
            .el-radio-group {
              display: flex;
              justify-content: space-between;
              overflow-x: auto;
              padding-bottom: 5px;
            }
            
            .view-toggle {
              margin: 10px 0 0 0;
              display: flex;
              justify-content: flex-end;
            }
          }
        }
      }
    }
    
    .main-content {
      .tea-content {
        .tea-list {
          &.view-grid {
            grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
          }
        }
      }
    }
  }
}
</style> 

