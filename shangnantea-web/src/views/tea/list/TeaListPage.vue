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
                <el-radio-button value="default">综合排序</el-radio-button>
                <el-radio-button value="sales">销量优先</el-radio-button>
                <el-radio-button value="price_asc">价格从低到高</el-radio-button>
                <el-radio-button value="price_desc">价格从高到低</el-radio-button>
                <el-radio-button value="newest">最新上架</el-radio-button>
              </el-radio-group>
              
              <div class="right-tools">
              <div class="view-toggle">
                <el-switch
                  v-model="viewMode"
                  active-value="grid"
                  inactive-value="list"
                  active-text="网格"
                  inactive-text="列表"
                />
                </div>
                <el-checkbox v-model="useDefaultSpecOnAdd" class="default-spec-checkbox">
                  默认规格
                </el-checkbox>
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
              <el-checkbox v-for="category in categoryOptions" :key="category.id" :value="category.id">
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
              <el-radio value="all">全部</el-radio>
              <el-radio value="platform">平台直售</el-radio>
            </el-radio-group>
          </div>
          
          <div class="filter-card">
            <h3 class="filter-title">评分筛选</h3>
            <el-radio-group v-model="filters.rating" @change="applyFilters">
              <el-radio :value="null">全部</el-radio>
              <el-radio :value="4.5">4.5分以上</el-radio>
              <el-radio :value="4.0">4.0分以上</el-radio>
              <el-radio :value="3.5">3.5分以上</el-radio>
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
                <tea-card :tea="tea" @add-to-cart="handleAddToCart" />
              </div>
            </div>
            
            <div v-else class="empty-result">
              <el-empty description="未找到符合条件的茶叶" />
            </div>
          </template>
          
          <!-- 分页 -->
          <div class="pagination-container" v-if="totalCount > 0">
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
      
    <!-- 任务组F：热门推荐（移到main-content外面，和首页一样） -->
    <div class="tea-recommend-section" v-if="popularTeas.length > 0">
        <h2 class="section-title">热门推荐</h2>
      <div class="subtitle">甄选商南优质茶品</div>
      
      <div class="tea-list">
        <div 
          v-for="(tea, index) in popularTeas" 
          :key="tea.id || index" 
          class="tea-item"
            @click="goToTeaDetail(tea.id)"
        >
          <div class="tea-image">
            <SafeImage :src="tea.image" type="tea" :alt="tea.name" style="width:100%;height:100%;object-fit:cover;" />
          </div>
          <div class="tea-info">
            <h4>{{ tea.name }}</h4>
            <p class="tea-price">¥{{ tea.price }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 列表页加入购物车时的规格选择对话框 -->
    <el-dialog
      v-model="specDialogVisible"
      title="选择规格"
      width="400px"
      destroy-on-close
    >
      <div class="spec-select-container" v-if="currentSpecTea">
        <div class="current-tea-info">
          <SafeImage
            :src="currentSpecTea.teaImage"
            type="tea"
            :alt="currentSpecTea.teaName"
            class="tea-image"
            style="width:80px;height:80px;object-fit:cover;"
          />
          <div class="tea-info">
            <div class="tea-name">{{ currentSpecTea.teaName }}</div>
            <div class="tea-price">¥{{ selectedSpecPrice.toFixed(2) }}</div>
            <div class="selected-spec">已选规格：{{ selectedSpecName }}</div>
        </div>
      </div>
        
        <div class="spec-options">
          <div class="spec-title-row">
            <div class="spec-title">选择规格</div>
            <el-checkbox v-model="selectMultipleSpecs" class="multi-spec-checkbox">选多款</el-checkbox>
    </div>
          <div class="spec-list">
            <el-radio-group v-if="!selectMultipleSpecs" v-model="tempSelectedSpecId">
              <el-radio
                v-for="spec in availableSpecs"
                :key="spec.id"
                :label="spec.id"
                class="spec-radio"
              >
                <div class="spec-item">
                  <span class="spec-name">{{ spec.specName }}</span>
                  <span class="spec-price">¥{{ spec.price.toFixed(2) }}</span>
                  <span v-if="spec.isDefault === 1" class="spec-tag">默认</span>
                </div>
              </el-radio>
            </el-radio-group>
            <el-checkbox-group v-else v-model="tempSelectedSpecIds">
              <el-checkbox
                v-for="spec in availableSpecs"
                :key="spec.id"
                :label="spec.id"
                class="spec-checkbox"
              >
                <div class="spec-item">
                  <span class="spec-name">{{ spec.specName }}</span>
                  <span class="spec-price">¥{{ spec.price.toFixed(2) }}</span>
                  <span v-if="spec.isDefault === 1" class="spec-tag">默认</span>
                </div>
              </el-checkbox>
            </el-checkbox-group>
          </div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="specDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmSpecAddToCart">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useTeaStore } from '@/stores/tea'
import { useOrderStore } from '@/stores/order'
import { Shop } from '@element-plus/icons-vue'
import TeaCard from '@/components/tea/card/TeaCard.vue'
import SafeImage from '@/components/common/form/SafeImage.vue'

import SearchBar from '@/components/common/SearchBar.vue'
import { showByCode } from '@/utils/apiMessages'
import { orderPromptMessages } from '@/utils/promptMessages'

defineOptions({
  name: 'TeaListPage'
})
    const teaStore = useTeaStore()
    const orderStore = useOrderStore()
    const router = useRouter()
    const route = useRoute()

    const searchQuery = ref('')
    const sortOption = ref('default')
    const viewMode = ref('grid')
    // 标志：是否正在通过applyFilters更新，避免watch重复触发
    const isApplyingFilters = ref(false)
    // 保存上一次的query，用于判断是否只是翻页
    const previousQuery = ref(null)
    // 是否直接使用默认规格加入购物车（从全局orderStore读写，避免切页后丢失）
    const useDefaultSpecOnAdd = computed({
      get() {
        return orderStore.useDefaultSpecOnAdd
      },
      set(val) {
        orderStore.useDefaultSpecOnAdd = val
      }
    })
    // 筛选选项
    const filters = reactive({
      categories: [],
      priceRange: [0, 1000],
      source: 'all',
      rating: null
    })
    
    // 从store获取分类数据
    const categoryOptions = computed(() => teaStore.categories)
    
    // 从store获取茶叶列表
    const teas = computed(() => teaStore.teaList)
    const totalCount = computed(() => teaStore.pagination.total)
    const pageSize = computed(() => teaStore.pagination.pageSize)
    const currentPage = computed(() => teaStore.pagination.currentPage)
    const loading = computed(() => teaStore.loading)

    // 规格选择弹窗状态（用于从列表页加购物车时选择规格）
    const specDialogVisible = ref(false)
    const currentSpecTea = ref(null)
    const availableSpecs = ref([])
    const tempSelectedSpecId = ref(null)
    const tempSelectedSpecIds = ref([]) // 多选时的规格ID数组
    const selectMultipleSpecs = ref(false) // 是否选多款

    const selectedSpecName = computed(() => {
      if (!tempSelectedSpecId.value || !availableSpecs.value.length) return ''
      const spec = availableSpecs.value.find(s => s.id === tempSelectedSpecId.value)
      return spec ? spec.specName : ''
    })

    const selectedSpecPrice = computed(() => {
      if (!tempSelectedSpecId.value || !availableSpecs.value.length) return 0
      const spec = availableSpecs.value.find(s => s.id === tempSelectedSpecId.value)
      return spec ? spec.price : 0
    })
    
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
        // 商品来源筛选：platform 表示平台直售（shopId='PLATFORM'），all 表示全部（shopId=''）
        await teaStore.updateFilters({
          keyword: searchQuery.value,
          // 支持多个分类筛选：传递所有选中的分类ID数组
          category: filters.categories.length > 0 ? filters.categories : '',
          priceRange: filters.priceRange,
          origin: '', // 暂时不支持产地筛选
          rating: filters.rating,
          sortBy: sortBy,
          sortOrder: sortOrder,
          status: 1, // 任务组E：只显示上架茶叶
          shopId: filters.source === 'platform' ? 'PLATFORM' : '' // 平台直售筛选
        })
      } catch (error) {
        // 网络错误已由API拦截器处理并显示消息，这里只记录日志用于开发调试
        if (process.env.NODE_ENV === 'development') {
          console.error('加载茶叶列表失败:', error)
        }
      }
    }

    // 从后端获取某个茶叶的规格列表
    const fetchSpecsForTea = async teaId => {
      const res = await teaStore.fetchTeaSpecifications(teaId)
      const specs = res?.data || res || []
      if (!specs || specs.length === 0) {
        orderPromptMessages.showNoSpecAvailable()
        return []
      }
      return specs
    }

    // 处理“加入购物车”（来自 TeaCard）
    const handleAddToCart = async tea => {
      try {
        // 先查询规格列表
        const specs = await fetchSpecsForTea(tea.id)
        if (!specs.length) return

        // 找到默认规格（若无，则取第一个）
        const defaultSpec = specs.find(s => s.isDefault === 1) || specs[0]

        // 如果勾选了“默认规格”，直接加入购物车
        if (useDefaultSpecOnAdd.value && defaultSpec) {
          const res = await orderStore.addToCart({
            teaId: tea.id,
            quantity: 1,
            specificationId: String(defaultSpec.id)
          })
          if (res && res.code) {
            showByCode(res.code)
          }
          return
        }

        // 否则弹出规格选择框
        currentSpecTea.value = {
          teaId: tea.id,
          teaName: tea.name,
          teaImage: tea.mainImage
        }
        availableSpecs.value = specs
        tempSelectedSpecId.value = defaultSpec ? defaultSpec.id : specs[0].id
        tempSelectedSpecIds.value = [] // 重置多选
        selectMultipleSpecs.value = false // 重置选多款状态
        specDialogVisible.value = true
      } catch (error) {
        if (process.env.NODE_ENV === 'development') {
          console.error('从列表加入购物车失败:', error)
        }
      }
    }

    // 确认从列表页选择规格并加入购物车
    const confirmSpecAddToCart = async () => {
      if (!currentSpecTea.value) {
        orderPromptMessages.showSpecRequired()
        return
      }
      
      try {
        // 如果勾选了"选多款"，为每个选中的规格分别添加到购物车
        if (selectMultipleSpecs.value) {
          if (!tempSelectedSpecIds.value || tempSelectedSpecIds.value.length === 0) {
            orderPromptMessages.showSpecRequired()
            return
          }
          // 批量添加
          const promises = tempSelectedSpecIds.value.map(specId =>
            orderStore.addToCart({
              teaId: currentSpecTea.value.teaId,
              quantity: 1,
              specificationId: String(specId)
            })
          )
          const results = await Promise.all(promises)
          // 显示最后一个结果的消息
          if (results.length > 0 && results[results.length - 1]?.code) {
            showByCode(results[results.length - 1].code)
          }
        } else {
          // 单选模式
          if (!tempSelectedSpecId.value) {
            orderPromptMessages.showSpecRequired()
            return
          }
          const res = await orderStore.addToCart({
            teaId: currentSpecTea.value.teaId,
            quantity: 1,
            specificationId: String(tempSelectedSpecId.value)
          })
          if (res && res.code) {
            showByCode(res.code)
          }
        }
        specDialogVisible.value = false
        // 重置状态
        selectMultipleSpecs.value = false
        tempSelectedSpecIds.value = []
      } catch (error) {
        if (process.env.NODE_ENV === 'development') {
          console.error('加入购物车失败:', error)
        }
      }
    }
    
    // 同步筛选条件到URL
    const updateQueryParams = () => {
      const query = {
        page: teaStore.pagination.currentPage,
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
      async query => {
        // 如果正在通过applyFilters更新，跳过watch触发（避免重复加载）
        if (isApplyingFilters.value) {
          previousQuery.value = { ...query }
          return
        }
        
        // 判断是否只是翻页（其他筛选条件没变，只有页码变了）
        const isOnlyPageChange = previousQuery.value && 
          query.page !== previousQuery.value.page &&
          JSON.stringify({
            search: query.search || '',
            sort: query.sort || 'default',
            categories: query.categories || '',
            price: query.price || '',
            source: query.source || 'all',
            rating: query.rating || ''
          }) === JSON.stringify({
            search: previousQuery.value.search || '',
            sort: previousQuery.value.sort || 'default',
            categories: previousQuery.value.categories || '',
            price: previousQuery.value.price || '',
            source: previousQuery.value.source || 'all',
            rating: previousQuery.value.rating || ''
          })
        
        // 如果只是翻页，直接设置页码并请求，不更新筛选条件
        if (isOnlyPageChange) {
          const targetPage = query.page ? parseInt(query.page) : 1
          teaStore.setPage(targetPage)
          previousQuery.value = { ...query }
          return
        }
        
        // 筛选条件变化了，更新所有筛选条件
        if (query.search) searchQuery.value = query.search
        else searchQuery.value = ''
        
        if (query.sort) sortOption.value = query.sort
        else sortOption.value = 'default'
        
        if (query.categories) {
          try {
            filters.categories = JSON.parse(query.categories)
          } catch (e) {
            filters.categories = []
          }
        } else {
          filters.categories = []
        }
        
        if (query.price) {
          try {
            filters.priceRange = JSON.parse(query.price)
          } catch (e) {
            filters.priceRange = [0, 1000]
          }
        } else {
          filters.priceRange = [0, 1000]
        }
        
        // 商品来源：如果URL中有source参数则使用，否则重置为'all'
        if (query.source) {
          filters.source = query.source
        } else {
          filters.source = 'all'
        }
        
        if (query.rating !== undefined && query.rating !== null && query.rating !== '') {
          filters.rating = parseFloat(query.rating)
        } else {
          filters.rating = null
        }
        
        // 统一加载数据（会重置页码为1）
        await loadTeas()
        
        // 如果URL中有页码参数且不是1，恢复页码
        const targetPage = query.page ? parseInt(query.page) : 1
        if (targetPage !== 1) {
          teaStore.setPage(targetPage)
        }
        
        // 保存当前query
        previousQuery.value = { ...query }
      },
      { immediate: true }
    )
    
    // 页面跳转
    const goToShopList = () => {
      router.push('/shop/list')
    }
    
    // 搜索处理（原有方法，保留用于兼容）
    const handleSearch = () => {
      teaStore.setPage(1)
      updateQueryParams()
    }
    
    // 统一搜索组件的搜索处理
    const handleSearchFromBar = ({ query, type }) => {
      // SearchBar已经处理了跳转，这里只需要更新本地状态
      searchQuery.value = query
      // 由于SearchBar已经跳转到 /tea/mall?search=xxx，watch会自动触发数据加载
    }
    
    // 应用筛选 - 直接应用，不再需要按钮
    const applyFilters = async () => {
      // 设置标志，防止watch重复触发
      isApplyingFilters.value = true
      try {
        // 直接更新筛选条件并加载数据（updateFilters会重置页码为1）
        await loadTeas()
        // 然后同步URL参数（不会触发watch，因为isApplyingFilters=true）
        updateQueryParams()
      } finally {
        // 延迟重置标志，确保URL更新完成
        setTimeout(() => {
          isApplyingFilters.value = false
        }, 100)
      }
    }
    
    // 重置筛选
    const resetFilters = async () => {
      filters.categories = []
      filters.priceRange = [0, 1000]
      filters.source = 'all'
      filters.rating = null
      searchQuery.value = ''
      sortOption.value = 'default'
      teaStore.setPage(1)
      
      // 重置Pinia筛选条件
      await teaStore.resetFilters()
      
      // 清空URL参数
      router.replace({ query: {} })
    }
    
    // 页面变化
    const handlePageChange = page => {
      // 只更新URL，让watch统一处理，避免重复请求
      router.replace({ 
        query: { 
          ...route.query, 
          page: page 
        } 
      })
    }
    
    // 处理排序变更
    const handleSortChange = () => {
      teaStore.setPage(1)
      updateQueryParams()
      loadTeas()
    }
    
    // 任务组F：热门推荐数据（完全按照首页的数据格式处理）
    const popularTeas = computed(() => {
      const source = Array.isArray(teaStore.recommendTeas) ? [...teaStore.recommendTeas] : []
      const DISPLAY_COUNT = 6

      // 如果推荐池大于展示数量，则随机抽取 DISPLAY_COUNT 条
      let sampled = source
      if (source.length > DISPLAY_COUNT) {
        // Fisher-Yates 洗牌，避免修改原始数组
        const shuffled = [...source]
        for (let i = shuffled.length - 1; i > 0; i--) {
          const j = Math.floor(Math.random() * (i + 1))
          ;[shuffled[i], shuffled[j]] = [shuffled[j], shuffled[i]]
        }
        sampled = shuffled.slice(0, DISPLAY_COUNT)
      }

      return sampled.map(item => {
        // 后端返回格式：{ id, name, price, images: [], mainImage }
        // 首页需要格式：{ id, name, price, image }
        const images = Array.isArray(item.images) ? item.images : []
        const image =
          (images.length > 0 && images[0]) ||
          item.mainImage ||
          ''

        return {
          id: item.id,
          name: item.name,
          price: item.price,
          image
        }
      })
    })
    
    // 任务组F：跳转到茶叶详情页
    const goToTeaDetail = teaId => {
      router.push(`/tea/${teaId}`)
    }
    
    // 任务组F：加载热门推荐
    const loadPopularTeas = async () => {
      try {
        await teaStore.fetchRecommendTeas({ type: 'popular', count: 6 })
      } catch (error) {
        // 网络错误已由API拦截器处理并显示消息，这里只记录日志用于开发调试
        if (process.env.NODE_ENV === 'development') {
          console.error('加载热门推荐失败:', error)
        }
      }
    }
    
    // 初始化
    onMounted(async () => {
      await teaStore.fetchCategories()
      // 如果URL中有查询参数，会触发watch自动加载
      // 如果没有查询参数，需要手动加载一次
      if (!route.query.page && !route.query.search && !route.query.categories) {
        await loadTeas()
      }
      // 任务组F：加载热门推荐
      await loadPopularTeas()
    })
</script>

<style lang="scss" scoped>
.tea-list-page {
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
        
        .sorting-options {
          display: flex;
          align-items: center;
          gap: 15px;
          
          .right-tools {
            display: flex;
            align-items: center;
            gap: 16px;
          }
          
          .view-toggle {
            margin-left: 10px;
          }
          
          .default-spec-checkbox {
            font-size: 13px;
            color: #606266;
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
          
          // 列表模式：一行一个茶叶，左图右文，整体高度控制在一行的范围内
          &.view-list {
            display: flex;
            flex-direction: column;
            gap: 12px;
            
            .tea-item {
      background-color: #fff;
      border-radius: 8px;
              box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
              padding: 10px 16px;
            }
            
            // 调整列表模式下 TeaCard 的布局，避免图片过大、一条占满整页
            :deep(.tea-card) {
              display: flex;
              flex-direction: row;
              align-items: center;
              height: auto;
              min-height: 120px;
              
              .tea-image {
                width: 140px;
                height: 100px;
                flex-shrink: 0;
                padding-bottom: 0;
                margin-right: 16px;
              }
              
              .tea-info {
                flex: 1;
                padding: 0;
                display: flex;
                flex-direction: column;
                gap: 6px;
                
                .tea-name {
                  margin: 0;
                  height: auto;
                  -webkit-line-clamp: 1;
                }
                
                .tea-brief {
                  height: auto;
                  margin: 0;
                  -webkit-line-clamp: 1;
                }
                
                .tea-price-row {
                  margin: 0;
                }
                
                .tea-actions {
                  flex: 0 0 auto;
                  margin-left: auto;
                  
                  .el-button {
                    width: auto !important;
                    min-width: 120px;
                  }
                }
              }
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
      
  // 任务组F：热门推荐样式（完全按照首页的样式，移到main-content外面）
  .tea-recommend-section {
    width: 85%;
    max-width: 1920px;
    margin: 0 auto 60px;
    padding: 0;
        
        .section-title {
      text-align: center;
      font-size: 28px;
      color: #333;
      margin: 0 0 5px;
      font-weight: 500;
    }
    
    .subtitle {
      text-align: center;
      font-size: 16px;
      color: #777;
          margin-bottom: 20px;
        }
        
    .tea-list {
      display: flex;
      flex-wrap: wrap;
          gap: 20px;
      margin-top: 30px;
      
      .tea-item {
        width: calc(16.666% - 17px);
        min-width: 150px;
        background-color: #fff;
        border-radius: 8px;
        overflow: hidden;
        box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
        cursor: pointer;
        transition: transform 0.3s, box-shadow 0.3s;
        
        &:hover {
          transform: translateY(-5px);
          box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
        }
        
        .tea-image {
          width: 100%;
          padding-top: 100%; // 1:1 比例
          position: relative;
          
          img {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            object-fit: cover;
          }
        }
        
        .tea-info {
          padding: 12px;
          text-align: center;
          
          h4 {
            margin: 0 0 5px;
            font-size: 16px;
            color: #333;
            font-weight: 500;
          }
          
          .tea-price {
            color: #e74c3c;
            font-weight: 600;
            margin: 0;
          }
        }
      }
    }
  }

  // 列表页规格选择对话框样式（复用购物车的布局风格）
  .spec-select-container {
    padding: 10px 0;
    
    .current-tea-info {
      display: flex;
      align-items: center;
      padding-bottom: 15px;
      border-bottom: 1px dashed var(--el-border-color);
      margin-bottom: 15px;
      
      .tea-image {
        width: 80px;
        height: 80px;
        border-radius: 4px;
        object-fit: cover;
        margin-right: 15px;
      }
      
      .tea-info {
        flex: 1;
        
        .tea-name {
          font-size: 16px;
          font-weight: 500;
          margin-bottom: 5px;
        }
        
        .tea-price {
          color: var(--el-color-danger);
          font-size: 18px;
          margin-bottom: 5px;
        }
        
        .selected-spec {
          color: var(--el-text-color-secondary);
          font-size: 14px;
        }
      }
    }
    
    .spec-options {
      .spec-title-row {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 10px;
        
        .spec-title {
          font-weight: 500;
          margin: 0;
        }
        
        .multi-spec-checkbox {
          font-size: 13px;
          color: var(--el-text-color-secondary);
        }
      }
      
      .spec-list {
        .spec-radio,
        .spec-checkbox {
          display: block;
          margin-bottom: 10px;
          
          .spec-item {
            display: flex;
            justify-content: space-between;
            width: 300px;
            
            .spec-name {
              color: var(--el-text-color-primary);
            }
            
            .spec-price {
              color: var(--el-color-danger);
            }
            
            .spec-tag {
              margin-left: 8px;
              font-size: 12px;
              color: var(--el-color-primary);
            }
          }
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
      
      // 推荐茶叶响应式
      .tea-recommend-section {
        .tea-list {
          .tea-item {
            width: calc(50% - 10px);
          }
        }
      }
    }
  }
}

// 中等屏幕响应式
@media (max-width: 1024px) {
  .tea-list-page {
    .main-content {
      .tea-recommend-section {
        .tea-list {
          .tea-item {
            width: calc(33.333% - 14px);
          }
        }
      }
    }
  }
}
</style> 

