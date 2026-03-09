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
                <el-radio-button value="default">综合排序</el-radio-button>
                <el-radio-button value="sales">销量优先</el-radio-button>
                <el-radio-button value="rating">评分优先</el-radio-button>
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
            :product-count="getProductCount(shop.id)"
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

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useShopStore } from '@/stores/shop'

import { Mug } from '@element-plus/icons-vue'
import ShopCard from '@/components/shop/card/ShopCard.vue'

import SearchBar from '@/components/common/SearchBar.vue'
import { showByCode } from '@/utils/apiMessages'

defineOptions({
  name: 'ShopListPage'
})
    const shopStore = useShopStore()
    const router = useRouter()
    const route = useRoute()

    // 任务组A：筛选条件
    const searchQuery = ref('')
    const sortOption = ref('default')
    const ratingFilter = ref(null)
    const salesFilter = ref(null)
    const regionFilter = ref('')
    const isApplyingQuery = ref(false)

    // 从store获取店铺列表/分页/加载态/筛选条件
    const shops = computed(() => shopStore.shopList || [])
    const totalCount = computed(() => shopStore.pagination.total || 0)
    const currentPage = computed({
      get: () => shopStore.pagination.currentPage || 1,
      set: val => {
        shopStore.pagination.currentPage = val
      }
    })
    const pageSize = computed({
      get: () => shopStore.pagination.pageSize || 10,
      set: val => {
        shopStore.pagination.pageSize = val
      }
    })
    const loading = computed(() => shopStore.loading || false)
    const filters = computed(() => shopStore.filters || {})
    const shopTeasPreviewMap = computed(() => shopStore.shopTeasPreviewMap || {})
    const shopProductCountMap = computed(() => shopStore.shopProductCountMap || {})
    
    // 获取指定店铺的茶叶
    const getShopTeas = shopId => {
      return shopTeasPreviewMap.value[shopId] || []
    }
    
    const getProductCount = shopId => {
      const val = shopProductCountMap.value[shopId]
      return typeof val === 'number' ? val : 0
    }
    
    // 任务组A：加载店铺列表（使用Pinia）
    const loadShops = async () => {
      try {
        await shopStore.fetchShops({})
        // 加载每个店铺的预览茶叶（最多2个），失败时忽略
        shops.value.forEach(shop => {
          if (shop && shop.id && !shopTeasPreviewMap.value[shop.id]) {
            shopStore.fetchShopTeasPreview(shop.id, 2).catch(() => {})
          }
        })
      } catch (error) {
        console.error('加载店铺列表失败:', error)
      }
    }
    
    // 监听路由参数变化
    watch(
      () => route.query,
      async (query, oldQuery) => {
        if (isApplyingQuery.value) {
          return
        }

        const filterKeys = ['search', 'sort', 'rating', 'sales', 'region', 'size']
        const isFilterSame = oldQuery ? filterKeys.every(k => (query?.[k] ?? '') === (oldQuery?.[k] ?? '')) : false

        const nextPage = query.page ? (parseInt(query.page) || 1) : 1
        const nextSize = query.size ? (parseInt(query.size) || pageSize.value) : pageSize.value

        // URL -> 本地筛选状态
        searchQuery.value = query.search ? String(query.search) : ''
        sortOption.value = query.sort ? String(query.sort) : 'default'
        ratingFilter.value = query.rating !== undefined ? (parseFloat(query.rating) || null) : null
        salesFilter.value = query.sales !== undefined ? (parseInt(query.sales) || null) : null
        regionFilter.value = query.region ? String(query.region) : ''

        // 同步 pageSize（即使没展示 size 选择器，也允许 URL 带 size）
        pageSize.value = nextSize

        // sortOption -> store filters
        let sortBy = 'default'
        if (sortOption.value === 'rating') sortBy = 'rating'
        else if (sortOption.value === 'sales') sortBy = 'salesCount'

        const storeFilters = {
          keyword: searchQuery.value,
          rating: ratingFilter.value,
          salesCount: salesFilter.value,
          region: regionFilter.value,
          sortBy,
          sortOrder: 'desc'
        }

        if (isFilterSame && oldQuery && (String(query.page ?? '') !== String(oldQuery.page ?? ''))) {
          // 仅翻页：不重置 filters
          await shopStore.setPage({ page: nextPage, extraParams: {} })
        } else {
          // 筛选变更（或首次进入）：允许指定目标页
          await shopStore.updateFilters(storeFilters, nextPage)
        }

        // 预览茶叶
        shops.value.forEach(shop => {
          if (shop && shop.id && !shopTeasPreviewMap.value[shop.id]) {
            shopStore.fetchShopTeasPreview(shop.id, 2).catch(() => {})
          }
        })
      },
      { immediate: true }
    )
    
    // 任务组A：同步筛选条件到URL
    const updateQueryParams = () => {
      const query = {
        page: currentPage.value,
        size: pageSize.value
      }
      
      if (searchQuery.value) query.search = searchQuery.value
      if (sortOption.value !== 'default') query.sort = sortOption.value
      if (ratingFilter.value !== null) query.rating = ratingFilter.value
      if (salesFilter.value !== null) query.sales = salesFilter.value
      if (regionFilter.value) query.region = regionFilter.value
      
      isApplyingQuery.value = true
      router.replace({ query }).finally(() => {
        isApplyingQuery.value = false
      })
    }
    
    // 任务组A：搜索处理（原有方法，保留用于兼容）
    const handleSearch = () => {
      currentPage.value = 1
      updateQueryParams()
    }
    
    // 统一搜索组件的搜索处理
    const handleSearchFromBar = ({ query, type }) => {
      // SearchBar已经处理了跳转，这里只需要更新本地状态
      searchQuery.value = query
      // 由于SearchBar已经跳转到 /shop/list?search=xxx，watch会自动触发数据加载
    }
    
    // 任务组A：排序变更处理
    const handleSortChange = () => {
      currentPage.value = 1
      updateQueryParams()
    }
    
    // 任务组A：筛选变更处理
    const handleFilterChange = () => {
      currentPage.value = 1
      updateQueryParams()
    }
    
    // 任务组A：重置筛选条件
    const handleResetFilters = () => {
      searchQuery.value = ''
      sortOption.value = 'default'
      ratingFilter.value = null
      salesFilter.value = null
      regionFilter.value = ''
      currentPage.value = 1

      isApplyingQuery.value = true
      router.replace({ query: {} }).finally(() => {
        isApplyingQuery.value = false
      })
    }
    
    // 页面变化
    const handlePageChange = page => {
      currentPage.value = page
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
</script>

<style lang="scss" scoped>
.shop-list-page {
  min-height: 100vh;
  background-color: #f5f7fa;
  
  .container {
    width: 85%;
    max-width: 1920px;
    margin: 0 auto;
    padding: 0;
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
