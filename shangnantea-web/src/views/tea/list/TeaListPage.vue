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
                <el-input
                  v-model="searchQuery"
                  placeholder="搜索茶叶名称"
                  class="search-input"
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
    </div>
  </div>
</template>

<script>
/* UI-DEV-START */
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Shop, List } from '@element-plus/icons-vue'
import TeaCard from '@/components/tea/card/TeaCard.vue'
/* UI-DEV-END */

/*
// 真实代码（开发UI时注释）
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { Search, Shop, List } from '@element-plus/icons-vue'
import TeaCard from '@/components/tea/card/TeaCard.vue'
*/

export default {
  name: "TeaListPage",
  components: {
    /* UI-DEV-START */
    Shop,
    Search,
    TeaCard
    /* UI-DEV-END */
    
    /*
    // 真实代码（开发UI时注释）
    Shop,
    Search,
    List,
    TeaCard
    */
  },
  setup() {
    const router = useRouter()
    const loading = ref(false)
    const searchQuery = ref('')
    const sortOption = ref('default')
    const viewMode = ref('grid')
    const currentPage = ref(1)
    const pageSize = ref(12)
    const totalCount = ref(0)
    
    // 筛选选项
    const filters = reactive({
      categories: [],
      priceRange: [0, 1000],
      source: 'all'
    })
    
    /* UI-DEV-START */
    // 模拟数据 - 茶叶分类
    const categoryOptions = [
      { id: 1, name: '绿茶' },
      { id: 2, name: '红茶' },
      { id: 3, name: '黑茶' },
      { id: 4, name: '白茶' },
      { id: 5, name: '黄茶' },
      { id: 6, name: '青茶' },
      { id: 7, name: '花茶' }
    ]
    
    // 模拟数据 - 茶叶列表
    const teas = ref([])
    
    // 生成模拟茶叶数据
    const generateMockTeas = () => {
      const mockTeas = []
      // 更新店铺ID格式，确保平台ID为'PLATFORM'，商铺ID为'SHOP0001'格式
      const shopSources = ['PLATFORM', 'SHOP0001', 'SHOP0002', 'SHOP0003']
      
      for (let i = 1; i <= 24; i++) {
        const price = Math.floor(Math.random() * 800) + 200
        const hasDiscount = Math.random() > 0.5
        // 确保店铺名称与shop_id匹配
        const shopIndex = Math.floor(Math.random() * shopSources.length)
        const shopId = shopSources[shopIndex]
        const shopName = shopId === 'PLATFORM' ? '平台直售' : `商南茶业${shopIndex}号店`
        
        mockTeas.push({
          id: `TEA${i.toString().padStart(4, '0')}`,
          shop_id: shopId,
          shop_name: shopName,
          category_id: Math.floor(Math.random() * 7) + 1,
          name: `${shopName}${categoryOptions[Math.floor(Math.random() * 7)].name} - ${i}号`,
          brief: `产自商南高山，${Math.floor(Math.random() * 3) + 1}年陈茶，口感醇厚`,
          image_url: `/mock-images/tea-${(i % 8) + 1}.jpg`,
          price: price,
          discount_price: hasDiscount ? Math.floor(price * 0.8) : null,
          stock: Math.floor(Math.random() * 999) + 1,
          sales: Math.floor(Math.random() * 500)
        })
      }
      
      return mockTeas
    }
    
    // 模拟加载数据
    const loadTeas = () => {
      loading.value = true
      
      // 模拟网络请求延迟
      setTimeout(() => {
        const allTeas = generateMockTeas()
        
        // 应用筛选
        let filteredTeas = [...allTeas]
        
        // 分类筛选
        if (filters.categories.length > 0) {
          filteredTeas = filteredTeas.filter(tea => filters.categories.includes(tea.category_id))
        }
        
        // 价格筛选
        filteredTeas = filteredTeas.filter(tea => {
          const teaPrice = tea.discount_price || tea.price
          return teaPrice >= filters.priceRange[0] && teaPrice <= filters.priceRange[1]
        })
        
        // 来源筛选
        if (filters.source === 'platform') {
          filteredTeas = filteredTeas.filter(tea => tea.shop_id === 'PLATFORM')
        } else if (filters.source === 'shop') {
          filteredTeas = filteredTeas.filter(tea => tea.shop_id !== 'PLATFORM')
        }
        
        // 搜索筛选
        if (searchQuery.value) {
          filteredTeas = filteredTeas.filter(tea => 
            tea.name.toLowerCase().includes(searchQuery.value.toLowerCase())
          )
        }
        
        // 排序
        switch (sortOption.value) {
          case 'sales':
            filteredTeas.sort((a, b) => b.sales - a.sales)
            break
          case 'price_asc':
            filteredTeas.sort((a, b) => (a.discount_price || a.price) - (b.discount_price || b.price))
            break
          case 'price_desc':
            filteredTeas.sort((a, b) => (b.discount_price || b.price) - (a.discount_price || a.price))
            break
          case 'newest':
            // 假设ID越大越新
            filteredTeas.sort((a, b) => b.id.localeCompare(a.id))
            break
          default:
            // 默认综合排序，这里用随机
            filteredTeas.sort(() => Math.random() - 0.5)
        }
        
        // 分页
        totalCount.value = filteredTeas.length
        const start = (currentPage.value - 1) * pageSize.value
        const end = start + pageSize.value
        teas.value = filteredTeas.slice(start, end)
        
        loading.value = false
      }, 800)
    }
    /* UI-DEV-END */
    
    /*
    // 真实代码（开发UI时注释）
    const store = useStore()
    const route = useRoute()
    
    // 从store获取分类数据
    const categoryOptions = computed(() => store.state.tea.categories)
    
    // 从store获取茶叶列表
    const teas = computed(() => store.state.tea.teas)
    const totalCount = computed(() => store.state.tea.total)
    
    // 监听路由参数变化
    watch(
      () => route.query,
      (query) => {
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
        
        loadTeas()
      },
      { immediate: true }
    )
    
    // 加载茶叶数据
    const loadTeas = async () => {
      loading.value = true
      
      try {
        await store.dispatch('tea/fetchTeas', {
          page: currentPage.value,
          pageSize: pageSize.value,
          search: searchQuery.value,
          sort: sortOption.value,
          categories: filters.categories,
          priceRange: filters.priceRange,
          source: filters.source
        })
      } catch (error) {
        ElMessage.error(error.message || '加载茶叶数据失败')
      } finally {
        loading.value = false
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
      
      router.replace({ query })
    }
    */
    
    // 页面跳转
    const goToShopList = () => {
      router.push('/shop/list')
    }
    
    // 搜索处理
    const handleSearch = () => {
      currentPage.value = 1
      /* UI-DEV-START */
      loadTeas()
      /* UI-DEV-END */
      
      /*
      // 真实代码（开发UI时注释）
      updateQueryParams()
      */
    }
    
    // 应用筛选 - 直接应用，不再需要按钮
    const applyFilters = () => {
      currentPage.value = 1
      /* UI-DEV-START */
      loadTeas()
      /* UI-DEV-END */
      
      /*
      // 真实代码（开发UI时注释）
      updateQueryParams()
      */
    }
    
    // 重置筛选 - 内部使用，不再作为按钮
    const resetFilters = () => {
      filters.categories = []
      filters.priceRange = [0, 1000]
      filters.source = 'all'
      currentPage.value = 1
      /* UI-DEV-START */
      loadTeas()
      /* UI-DEV-END */
      
      /*
      // 真实代码（开发UI时注释）
      updateQueryParams()
      */
    }
    
    // 页面变化
    const handlePageChange = (page) => {
      currentPage.value = page
      /* UI-DEV-START */
      loadTeas()
      /* UI-DEV-END */
      
      /*
      // 真实代码（开发UI时注释）
      updateQueryParams()
      */
    }
    
    // 处理排序变更
    const handleSortChange = () => {
      currentPage.value = 1
      loadTeas()
    }
    
    // 初始化
    onMounted(() => {
      /* UI-DEV-START */
      loadTeas()
      /* UI-DEV-END */
      
      /*
      // 真实代码（开发UI时注释）
      // loadTeas会通过路由watch触发
      store.dispatch('tea/fetchCategories')
      */
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
      applyFilters,
      resetFilters,
      handlePageChange,
      handleSortChange
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

