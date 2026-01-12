<template>
  <div class="shop-list-page">
    <div class="page-header">
      <div class="container">
        <div class="header-content">
          <h1 class="page-title">商铺列表</h1>
          <p class="page-description">浏览商南茶文化平台认证商家，发现优质茶品</p>
          
          <div class="header-toolbar">
            <div class="left-area">
              <div class="search-area">
                <SearchBar 
                  search-type="shop"
                  :model-value="searchQuery"
                  placeholder="搜索店铺名称"
                  @update:model-value="searchQuery = $event"
                  @search="handleSearchFromBar"
                />
              </div>
              
              <el-button type="primary" @click="goToTeaList">
                <el-icon><Mug /></el-icon>
                茶叶列表
              </el-button>
            </div>
            
            <!-- 排序选项 -->
            <div class="sorting-options">
              <el-radio-group v-model="sortOption" size="small" @change="handleSortChange">
                <el-radio-button label="default">综合排序</el-radio-button>
                <el-radio-button label="sales">销量优先</el-radio-button>
                <el-radio-button label="rating">评分优先</el-radio-button>
              </el-radio-group>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="container main-content">
      <!-- 任务组A：筛选面板 -->
      <div class="filter-panel">
        <el-card shadow="never">
          <div class="filter-content">
            <div class="filter-item">
              <span class="filter-label">评分：</span>
              <el-select v-model="ratingFilter" placeholder="选择评分" clearable @change="handleFilterChange" style="width: 150px">
                <el-option label="4.0分以上" :value="4.0" />
                <el-option label="4.5分以上" :value="4.5" />
                <el-option label="5.0分" :value="5.0" />
              </el-select>
            </div>
            
            <div class="filter-item">
              <span class="filter-label">销量：</span>
              <el-select v-model="salesFilter" placeholder="选择销量" clearable @change="handleFilterChange" style="width: 150px">
                <el-option label="500以上" :value="500" />
                <el-option label="1000以上" :value="1000" />
                <el-option label="2000以上" :value="2000" />
              </el-select>
            </div>
            
            <div class="filter-item">
              <span class="filter-label">地区：</span>
              <el-select v-model="regionFilter" placeholder="选择地区" clearable @change="handleFilterChange" style="width: 150px">
                <el-option label="商南县" value="商南县" />
                <el-option label="商洛市" value="商洛市" />
                <el-option label="西安市" value="西安市" />
                <el-option label="宝鸡市" value="宝鸡市" />
              </el-select>
            </div>
            
            <el-button type="info" plain @click="handleResetFilters">重置筛选</el-button>
          </div>
        </el-card>
      </div>
      
      <div v-loading="loading">
        <!-- 店铺列表 -->
        <div v-if="shops.length > 0" class="shop-list">
          <shop-card
            v-for="shop in shops"
            :key="shop.id"
            :shop="shop"
            :shopTeas="getShopTeas(shop.id)"
          />
        </div>
        
        <!-- 空状态 -->
        <div v-else-if="!loading" class="empty-result">
          <el-empty description="未找到符合条件的店铺" />
        </div>
        
        <!-- 分页控件 -->
        <div class="pagination-container" v-if="shops.length > 0">
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
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useStore } from 'vuex'

import { Mug } from '@element-plus/icons-vue'
import ShopCard from '@/components/shop/card/ShopCard.vue'

import SearchBar from '@/components/common/SearchBar.vue'
import { showByCode, isSuccess } from '@/utils/apiMessages'
import shopMessages from '@/utils/promptMessages'

export default {
  name: 'ShopListPage',
  components: {
    Mug,
    ShopCard,
    SearchBar
  },
  setup() {
    const store = useStore()
    const router = useRouter()
    const route = useRoute()

    // 任务组A：筛选条件
    const searchQuery = ref('')
    const sortOption = ref('default')
    const ratingFilter = ref(null)
    const salesFilter = ref(null)
    const regionFilter = ref('')
    const currentPage = ref(1)

    // 从store获取店铺列表/分页/加载态/筛选条件
    const shops = computed(() => store.state.shop.shopList || [])
    const totalCount = computed(() => store.state.shop.pagination.total || 0)
    const pageSize = computed(() => store.state.shop.pagination.pageSize || 10)
    const loading = computed(() => store.state.shop.loading || false)
    const currentPageFromStore = computed(() => store.state.shop.pagination.currentPage || 1)
    const filters = computed(() => store.state.shop.filters || {})
    
    // 同步Vuex分页到本地
    watch(currentPageFromStore, newPage => {
      if (newPage !== currentPage.value) {
        currentPage.value = newPage
      }
    })
    
    // 任务组A：同步Vuex filters到本地
    watch(filters, newFilters => {
      if (newFilters.keyword !== searchQuery.value) {
        searchQuery.value = newFilters.keyword || ''
      }
      if (newFilters.rating !== ratingFilter.value) {
        ratingFilter.value = newFilters.rating
      }
      if (newFilters.salesCount !== salesFilter.value) {
        salesFilter.value = newFilters.salesCount
      }
      if (newFilters.region !== regionFilter.value) {
        regionFilter.value = newFilters.region || ''
      }
      // 映射sortBy到sortOption
      if (newFilters.sortBy === 'rating') {
        sortOption.value = 'rating'
      } else if (newFilters.sortBy === 'salesCount') {
        sortOption.value = 'sales'
      } else {
        sortOption.value = 'default'
      }
    }, { immediate: true })
    
    // 获取指定店铺的茶叶
    const getShopTeas = shopId => {
      // 生产结构下：列表页不再本地造"店铺下的茶叶预览数据"
      // 后续对接后端后，可在此处实现"按店铺加载预览茶叶"的逻辑（通过 Vuex action）
      return []
    }
    
    // 任务组A：加载店铺列表（使用Vuex filters）
    const loadShops = async () => {
      try {
        await store.dispatch('shop/fetchShops', {})
      } catch (error) {
        // TODO: 迁移到新消息系统 - 使用 showByCode(response.code)

        shopMessages.error.showShopDataLoadFailed(error.message)
      }
    }
    
    // 监听路由参数变化
    watch(
      () => route.query,
      query => {
        // 从URL参数更新筛选条件
        if (query.search) {
          searchQuery.value = query.search
          store.dispatch('shop/updateFilters', { keyword: query.search })
        }
        if (query.sort) {
          sortOption.value = query.sort
          // 映射sortOption到sortBy
          let sortBy = 'default'
          if (query.sort === 'rating') sortBy = 'rating'
          else if (query.sort === 'sales') sortBy = 'salesCount'
          store.dispatch('shop/updateFilters', { sortBy })
        }
        if (query.rating) {
          ratingFilter.value = parseFloat(query.rating)
          store.dispatch('shop/updateFilters', { rating: parseFloat(query.rating) })
        }
        if (query.sales) {
          salesFilter.value = parseInt(query.sales)
          store.dispatch('shop/updateFilters', { salesCount: parseInt(query.sales) })
        }
        if (query.region) {
          regionFilter.value = query.region
          store.dispatch('shop/updateFilters', { region: query.region })
        }
        if (query.page) {
          const page = parseInt(query.page)
          currentPage.value = page
          store.dispatch('shop/setPage', { page, extraParams: {} })
        } else {
          loadShops()
        }
      },
      { immediate: true }
    )
    
    // 任务组A：同步筛选条件到URL
    const updateQueryParams = () => {
      const query = {
        page: currentPage.value
      }
      
      if (searchQuery.value) query.search = searchQuery.value
      if (sortOption.value !== 'default') query.sort = sortOption.value
      if (ratingFilter.value !== null) query.rating = ratingFilter.value
      if (salesFilter.value !== null) query.sales = salesFilter.value
      if (regionFilter.value) query.region = regionFilter.value
      
      router.replace({ query })
    }
    
    // 任务组A：搜索处理（原有方法，保留用于兼容）
    const handleSearch = async () => {
      currentPage.value = 1
      await store.dispatch('shop/updateFilters', { keyword: searchQuery.value })
      updateQueryParams()
    }
    
    // 统一搜索组件的搜索处理
    const handleSearchFromBar = ({ query, type }) => {
      // SearchBar已经处理了跳转，这里只需要更新本地状态
      searchQuery.value = query
      // 由于SearchBar已经跳转到 /shop/list?search=xxx，watch会自动触发数据加载
    }
    
    // 任务组A：排序变更处理
    const handleSortChange = async () => {
      currentPage.value = 1
      // 映射sortOption到sortBy
      let sortBy = 'default'
      if (sortOption.value === 'rating') sortBy = 'rating'
      else if (sortOption.value === 'sales') sortBy = 'salesCount'
      
      await store.dispatch('shop/updateFilters', { 
        sortBy,
        sortOrder: 'desc' // 默认降序
      })
      updateQueryParams()
    }
    
    // 任务组A：筛选变更处理
    const handleFilterChange = async () => {
      currentPage.value = 1
      await store.dispatch('shop/updateFilters', {
        rating: ratingFilter.value,
        salesCount: salesFilter.value,
        region: regionFilter.value
      })
      updateQueryParams()
    }
    
    // 任务组A：重置筛选条件
    const handleResetFilters = async () => {
      searchQuery.value = ''
      sortOption.value = 'default'
      ratingFilter.value = null
      salesFilter.value = null
      regionFilter.value = ''
      currentPage.value = 1
      
      await store.dispatch('shop/resetFilters')
      router.replace({ query: {} })
    }
    
    // 页面变化
    const handlePageChange = page => {
      currentPage.value = page
      store.dispatch('shop/setPage', { page, extraParams: { search: searchQuery.value, sort: sortOption.value } })
      updateQueryParams()
    }
    
    // 跳转到茶叶列表
    const goToTeaList = () => {
      router.push('/tea/mall')
    }
    
    // 初始化
    onMounted(() => {
      // loadShops 会由 route.query watch 触发（immediate: true）
    })
    
    return {
      searchQuery,
      sortOption,
      ratingFilter,
      salesFilter,
      regionFilter,
      shops,
      loading,
      currentPage,
      pageSize,
      totalCount,
      handleSearch,
      handleSearchFromBar,
      handleSortChange,
      handleFilterChange,
      handleResetFilters,
      handlePageChange,
      goToTeaList,
      getShopTeas
    }
  }
}
</script>

<style lang="scss" scoped>
.shop-list-page {
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
      }
    }
  }
  
  .main-content {
    margin-bottom: 40px;
    
    .filter-panel {
      margin-bottom: 20px;
      
      .filter-content {
        display: flex;
        align-items: center;
        flex-wrap: wrap;
        gap: 20px;
        
        .filter-item {
          display: flex;
          align-items: center;
          
          .filter-label {
            margin-right: 8px;
            color: #606266;
          }
        }
      }
    }
    
    .shop-list {
      display: flex;
      flex-direction: column;
      gap: 15px;
    }
    
    .empty-result {
      background-color: #fff;
      border-radius: 8px;
      padding: 40px 20px;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
      text-align: center;
    }
    
    .pagination-container {
      display: flex;
      justify-content: center;
      margin-top: 30px;
    }
  }
}

@media (max-width: 768px) {
  .shop-list-page {
    .page-header {
      .header-content {
        .header-toolbar {
          flex-direction: column;
          align-items: stretch;
          
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
            display: flex;
            justify-content: center;
          }
        }
      }
    }
  }
}
</style>
