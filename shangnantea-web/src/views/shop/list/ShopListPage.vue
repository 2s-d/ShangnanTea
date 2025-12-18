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
                <el-input
                  v-model="searchQuery"
                  placeholder="搜索店铺名称"
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
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { Search, Mug } from '@element-plus/icons-vue'
import ShopCard from '@/components/shop/card/ShopCard.vue'

export default {
  name: "ShopListPage",
  components: {
    Search,
    Mug,
    ShopCard
  },
  setup() {
    const store = useStore()
    const router = useRouter()
    const route = useRoute()

    const searchQuery = ref('')
    const sortOption = ref('default')
    const currentPage = ref(1)

    // 从store获取店铺列表/分页/加载态
    const shops = computed(() => store.state.shop.shopList)
    const totalCount = computed(() => store.state.shop.pagination.total)
    const pageSize = computed(() => store.state.shop.pagination.pageSize)
    const loading = computed(() => store.state.shop.loading)
    
    // 监听路由参数变化
    watch(
      () => route.query,
      (query) => {
        // 从URL参数更新筛选条件
        if (query.search) searchQuery.value = query.search
        if (query.sort) sortOption.value = query.sort
        if (query.page) currentPage.value = parseInt(query.page)
        
        loadShops()
      },
      { immediate: true }
    )
    
    // 获取指定店铺的茶叶
    const getShopTeas = (shopId) => {
      // 生产结构下：列表页不再本地造“店铺下的茶叶预览数据”
      // 后续对接后端后，可在此处实现“按店铺加载预览茶叶”的逻辑（通过 Vuex action）
      return []
    }
    
    // 加载店铺列表
    const loadShops = async () => {
      try {
        await store.dispatch('shop/fetchShops', {
          // 兼容后端可能的分页参数命名
          page: currentPage.value,
          size: pageSize.value,
          search: searchQuery.value,
          sort: sortOption.value
        })
      } catch (error) {
        ElMessage.error(error.message || '加载店铺数据失败')
      }
    }
    
    // 同步筛选条件到URL
    const updateQueryParams = () => {
      const query = {
        page: currentPage.value,
        sort: sortOption.value
      }
      
      if (searchQuery.value) query.search = searchQuery.value
      
      router.replace({ query })
    }
    
    // 搜索处理
    const handleSearch = () => {
      currentPage.value = 1
      updateQueryParams()
    }
    
    // 排序变更处理
    const handleSortChange = () => {
      currentPage.value = 1
      updateQueryParams()
    }
    
    // 页面变化
    const handlePageChange = (page) => {
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
      shops,
      loading,
      currentPage,
      pageSize,
      totalCount,
      handleSearch,
      handleSortChange,
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

