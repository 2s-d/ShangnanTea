/**
 * 分页和筛选相关操作的组合式函数
 */
import { ref, computed, reactive, watchEffect } from 'vue'
import { useRoute, useRouter } from 'vue-router'

/**
 * 使用分页和筛选功能
 * @param {Object} options 配置选项
 * @returns {Object} 分页和筛选相关方法和状态
 */
export function usePagination(options = {}) {
  const route = useRoute()
  const router = useRouter()
  
  // 默认配置
  const defaultOptions = {
    pageSize: 10,
    pageSizes: [10, 20, 50, 100],
    currentPage: 1,
    total: 0,
    syncWithRoute: false, // 是否与路由同步
    routeParamName: {
      page: 'page',
      size: 'size',
      sort: 'sort',
      order: 'order'
    },
    onPageChange: null, // 页码变化回调
    onSizeChange: null, // 每页条数变化回调
    onSortChange: null, // 排序变化回调
    onFilterChange: null // 筛选条件变化回调
  }
  
  // 合并配置
  const config = { ...defaultOptions, ...options }
  
  // 分页状态
  const pagination = reactive({
    currentPage: parseInt(route.query[config.routeParamName.page]) || config.currentPage,
    pageSize: parseInt(route.query[config.routeParamName.size]) || config.pageSize,
    pageSizes: config.pageSizes,
    total: config.total,
    layout: 'total, sizes, prev, pager, next, jumper',
    background: true
  })
  
  // 排序状态
  const sort = reactive({
    prop: route.query[config.routeParamName.sort] || '',
    order: route.query[config.routeParamName.order] || 'descending'
  })
  
  // 筛选条件
  const filters = ref({})
  
  // 加载状态
  const loading = ref(false)
  
  // 计算属性：获取当前页数据的范围
  const pageRange = computed(() => {
    const start = (pagination.currentPage - 1) * pagination.pageSize + 1
    const end = Math.min(pagination.currentPage * pagination.pageSize, pagination.total)
    return { start, end }
  })
  
  // 计算属性：总页数
  const totalPages = computed(() => {
    return Math.ceil(pagination.total / pagination.pageSize)
  })
  
  /**
   * 处理页码变化
   * @param {number} page 页码
   */
  const handleCurrentChange = (page) => {
    if (page === pagination.currentPage) return
    
    pagination.currentPage = page
    
    if (config.syncWithRoute) {
      updateRoute()
    }
    
    if (typeof config.onPageChange === 'function') {
      config.onPageChange(page)
    }
  }
  
  /**
   * 处理每页条数变化
   * @param {number} size 每页条数
   */
  const handleSizeChange = (size) => {
    if (size === pagination.pageSize) return
    
    pagination.pageSize = size
    pagination.currentPage = 1 // 重置到第一页
    
    if (config.syncWithRoute) {
      updateRoute()
    }
    
    if (typeof config.onSizeChange === 'function') {
      config.onSizeChange(size)
    }
  }
  
  /**
   * 处理排序变化
   * @param {Object} params 排序参数 {prop, order}
   */
  const handleSortChange = (params) => {
    if (params.prop === sort.prop && params.order === sort.order) return
    
    sort.prop = params.prop
    sort.order = params.order
    
    if (config.syncWithRoute) {
      updateRoute()
    }
    
    if (typeof config.onSortChange === 'function') {
      config.onSortChange(params)
    }
  }
  
  /**
   * 处理筛选条件变化
   * @param {Object} conditions 筛选条件
   */
  const handleFilterChange = (conditions) => {
    filters.value = { ...conditions }
    pagination.currentPage = 1 // 筛选条件变化时重置到第一页
    
    if (config.syncWithRoute) {
      updateRoute()
    }
    
    if (typeof config.onFilterChange === 'function') {
      config.onFilterChange(conditions)
    }
  }
  
  /**
   * 重置分页和筛选
   */
  const reset = () => {
    pagination.currentPage = config.currentPage
    pagination.pageSize = config.pageSize
    filters.value = {}
    sort.prop = ''
    sort.order = 'descending'
    
    if (config.syncWithRoute) {
      updateRoute()
    }
  }
  
  /**
   * 更新URL，同步分页、排序和筛选参数
   */
  const updateRoute = () => {
    if (!config.syncWithRoute) return
    
    const query = { ...route.query }
    
    // 更新分页参数
    query[config.routeParamName.page] = pagination.currentPage.toString()
    query[config.routeParamName.size] = pagination.pageSize.toString()
    
    // 更新排序参数
    if (sort.prop) {
      query[config.routeParamName.sort] = sort.prop
      query[config.routeParamName.order] = sort.order
    } else {
      delete query[config.routeParamName.sort]
      delete query[config.routeParamName.order]
    }
    
    // 更新路由
    router.push({
      path: route.path,
      query
    })
  }
  
  /**
   * 生成请求参数
   * @returns {Object} 请求参数
   */
  const getRequestParams = () => {
    const params = {
      page: pagination.currentPage,
      size: pagination.pageSize,
      ...filters.value
    }
    
    if (sort.prop) {
      params.sort = sort.prop
      params.order = sort.order === 'ascending' ? 'asc' : 'desc'
    }
    
    return params
  }
  
  /**
   * 更新总条数
   * @param {number} total 总条数
   */
  const setTotal = (total) => {
    pagination.total = total
    
    // 如果当前页超出总页数，则跳转到最后一页
    if (pagination.currentPage > totalPages.value && totalPages.value > 0) {
      handleCurrentChange(totalPages.value)
    }
  }
  
  // 监听路由变化，同步分页参数
  if (config.syncWithRoute) {
    watchEffect(() => {
      const query = route.query
      const page = parseInt(query[config.routeParamName.page])
      const size = parseInt(query[config.routeParamName.size])
      const sortProp = query[config.routeParamName.sort]
      const sortOrder = query[config.routeParamName.order]
      
      if (page && page !== pagination.currentPage) {
        pagination.currentPage = page
      }
      
      if (size && size !== pagination.pageSize) {
        pagination.pageSize = size
      }
      
      if (sortProp && sortProp !== sort.prop) {
        sort.prop = sortProp
      }
      
      if (sortOrder && sortOrder !== sort.order) {
        sort.order = sortOrder
      }
    })
  }
  
  return {
    // 状态
    pagination,
    sort,
    filters,
    loading,
    pageRange,
    totalPages,
    
    // 方法
    handleCurrentChange,
    handleSizeChange,
    handleSortChange,
    handleFilterChange,
    reset,
    getRequestParams,
    setTotal
  }
} 