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
/* UI-DEV-START */
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Mug } from '@element-plus/icons-vue'
import ShopCard from '@/components/shop/card/ShopCard.vue'
/* UI-DEV-END */

/*
// 真实代码（开发UI时注释）
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { Search, Mug } from '@element-plus/icons-vue'
import ShopCard from '@/components/shop/card/ShopCard.vue'
*/

export default {
  name: "ShopListPage",
  components: {
    /* UI-DEV-START */
    Search,
    Mug,
    ShopCard
    /* UI-DEV-END */
    
    /*
    // 真实代码（开发UI时注释）
    Search,
    Mug,
    ShopCard
    */
  },
  setup() {
    const router = useRouter()
    const loading = ref(false)
    const searchQuery = ref('')
    const sortOption = ref('default')
    const currentPage = ref(1)
    const pageSize = ref(5) // 每页5个店铺
    const totalCount = ref(0)
    
    // 店铺列表
    const shops = ref([])
    // 店铺茶叶
    const shopTeas = ref({})
    
    /* UI-DEV-START */
    // 生成模拟店铺数据
    const generateMockShops = () => {
      const mockShops = []
      
      for (let i = 1; i <= 8; i++) {
        mockShops.push({
          id: `shop${100000 + i}`,
          owner_id: `cy1000${i+2}`,
          shop_name: `商南茶业专营店${i}号`,
          logo: `/mock-images/shop-logo-${i % 4 + 1}.jpg`,
          banner: `/mock-images/shop-banner-${i % 4 + 1}.jpg`,
          description: `专营商南特产茶叶，传承百年制茶工艺，提供优质商南${i % 2 === 0 ? '绿茶' : '红茶'}。`,
          announcement: `本店新到一批${i % 2 === 0 ? '明前春茶' : '特级红茶'}，欢迎品尝选购！`,
          contact_phone: `1380000${1000 + i}`,
          province: '陕西省',
          city: '商洛市',
          district: '商南县',
          address: `城关街道茶叶市场${i}号`,
          status: 1,
          rating: 4 + Math.random(),
          rating_count: Math.floor(Math.random() * 50) + 5,
          sales_count: Math.floor(Math.random() * 500) + 50,
          create_time: `2025-0${i % 3 + 1}-${10 + i} 09:00:00`,
          update_time: '2025-03-26 09:03:26',
          certification: {
            status: Math.random() > 0.3 ? 'verified' : 'pending',
            type: '企业认证',
            expireTime: '2025-12-31'
          }
        })
      }
      
      return mockShops
    }
    
    // 生成模拟茶叶数据
    const generateMockTeas = () => {
      const teasByShop = {}
      
      // 先生成所有店铺
      const mockShops = generateMockShops()
      shops.value = mockShops
      
      // 为每个店铺生成2-5个茶叶
      mockShops.forEach(shop => {
        const teasCount = Math.floor(Math.random() * 4) + 2  // 2-5个茶叶
        const shopTeas = []
        
        for (let i = 1; i <= teasCount; i++) {
          const teaId = `tea${shop.id.substring(4)}${i}`
          const price = Math.floor(Math.random() * 500) + 100
          
          shopTeas.push({
            id: teaId,
            name: `${shop.shop_name.substring(0, 4)}${i % 2 === 0 ? '特级绿茶' : '特级红茶'} ${i}号`,
            shop_id: shop.id,
            category_id: i % 2 === 0 ? 1 : 2, // 1=绿茶，2=红茶
            price: price,
            discount_price: Math.random() > 0.5 ? Math.floor(price * 0.8) : null,
            description: '高山茶叶，品质保证',
            origin: '陕西省商洛市商南县',
            stock: Math.floor(Math.random() * 100) + 20,
            sales: Math.floor(Math.random() * 200) + 10,
            main_image: `/mock-images/tea-${i % 8 + 1}.jpg`,
            status: 1,
            create_time: '2025-03-26 09:03:26',
            update_time: '2025-03-26 09:03:26'
          })
        }
        
        teasByShop[shop.id] = shopTeas
      })
      
      return teasByShop
    }
    
    // 获取指定店铺的茶叶
    const getShopTeas = (shopId) => {
      return shopTeas.value[shopId] || []
    }
    
    // 加载店铺数据
    const loadShops = () => {
      loading.value = true
      
      // 模拟API调用延迟
      setTimeout(() => {
        // 如果还没有生成过模拟数据，先生成
        if (Object.keys(shopTeas.value).length === 0) {
          shopTeas.value = generateMockTeas()
        }
        
        // 应用筛选
        let filteredShops = [...shops.value]
        
        // 搜索过滤
        if (searchQuery.value) {
          filteredShops = filteredShops.filter(shop => 
            shop.shop_name.toLowerCase().includes(searchQuery.value.toLowerCase())
          )
        }
        
        // 排序
        switch (sortOption.value) {
          case 'sales':
            filteredShops.sort((a, b) => b.sales_count - a.sales_count)
            break
          case 'rating':
            filteredShops.sort((a, b) => b.rating - a.rating)
            break
          default:
            // 默认排序，可以是综合排序，这里简单处理
            filteredShops.sort((a, b) => a.id.localeCompare(b.id))
        }
        
        // 分页
        totalCount.value = filteredShops.length
        const start = (currentPage.value - 1) * pageSize.value
        const end = start + pageSize.value
        shops.value = filteredShops.slice(start, end)
        
        loading.value = false
      }, 800)
    }
    /* UI-DEV-END */
    
    /*
    // 真实代码（开发UI时注释）
    const store = useStore()
    const route = useRoute()
    
    // 从store获取店铺列表
    const shops = computed(() => store.state.shop.shops)
    const totalCount = computed(() => store.state.shop.total)
    
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
      return store.getters['shop/getShopTeasPreview'](shopId)
    }
    
    // 加载店铺列表
    const loadShops = async () => {
      loading.value = true
      
      try {
        await store.dispatch('shop/fetchShops', {
          page: currentPage.value,
          pageSize: pageSize.value,
          search: searchQuery.value,
          sort: sortOption.value
        })
      } catch (error) {
        ElMessage.error(error.message || '加载店铺数据失败')
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
      
      router.replace({ query })
    }
    */
    
    // 搜索处理
    const handleSearch = () => {
      currentPage.value = 1
      /* UI-DEV-START */
      loadShops()
      /* UI-DEV-END */
      
      /*
      // 真实代码（开发UI时注释）
      updateQueryParams()
      */
    }
    
    // 排序变更处理
    const handleSortChange = () => {
      currentPage.value = 1
      /* UI-DEV-START */
      loadShops()
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
      loadShops()
      /* UI-DEV-END */
      
      /*
      // 真实代码（开发UI时注释）
      updateQueryParams()
      */
    }
    
    // 跳转到茶叶列表
    const goToTeaList = () => {
      router.push('/tea/mall')
    }
    
    // 初始化
    onMounted(() => {
      /* UI-DEV-START */
      loadShops()
      /* UI-DEV-END */
      
      /*
      // 真实代码（开发UI时注释）
      // loadShops会通过路由watch触发
      */
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

