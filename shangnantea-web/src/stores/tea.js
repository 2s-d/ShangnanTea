import { defineStore } from 'pinia'
import { ref, computed, reactive } from 'vue'
import { 
  getTeas, 
  getTeaDetail, 
  getTeaCategories, 
  createCategory as createCategoryApi, 
  updateCategory as updateCategoryApi, 
  deleteCategory as deleteCategoryApi, 
  addTea as addTeaApi, 
  updateTea as updateTeaApi, 
  deleteTea as deleteTeaApi, 
  getTeaReviews, 
  getReviewStats, 
  replyReview as replyReviewApi, 
  getTeaSpecifications, 
  addSpecification as addSpecificationApi, 
  updateSpecification as updateSpecificationApi, 
  deleteSpecification as deleteSpecificationApi, 
  setDefaultSpecification as setDefaultSpecificationApi, 
  uploadTeaImages as uploadTeaImagesApi, 
  deleteTeaImage as deleteTeaImageApi, 
  updateImageOrder as updateImageOrderApi, 
  setMainImage as setMainImageApi, 
  toggleTeaStatus as toggleTeaStatusApi, 
  batchToggleTeaStatus as batchToggleTeaStatusApi, 
  getRecommendTeas 
} from '@/api/tea'

export const useTeaStore = defineStore('tea', () => {
  // ========== State ==========
  const teaList = ref([])
  const currentTea = ref(null)
  const categories = ref([])
  const loading = ref(false)
  
  // 分页信息
  const pagination = reactive({
    total: 0,
    currentPage: 1,
    pageSize: 10
  })
  
  // 过滤条件
  const filters = reactive({
    category: '',
    keyword: '',
    priceRange: [0, 1000],
    origin: '',
    rating: null,
    sortBy: '',
    sortOrder: 'asc'
  })
  
  // 评价相关state
  const teaReviews = ref([])
  const reviewPagination = reactive({
    total: 0,
    currentPage: 1,
    pageSize: 10
  })
  const reviewStats = ref(null)
  
  // 规格管理相关state
  const currentTeaSpecs = ref([])
  const selectedSpec = ref(null)
  
  // 图片管理相关state
  const teaImages = ref([])
  
  // 推荐功能相关state
  const recommendTeas = ref([])
  
  // ========== Getters ==========
  const categoryOptions = computed(() => 
    categories.value.map(item => ({
      value: item.id,
      label: item.name
    }))
  )
  
  // ========== Actions ==========
  
  // 获取茶叶列表
  async function fetchTeas() {
    try {
      loading.value = true
      
      const params = {
        page: pagination.currentPage,
        pageSize: pagination.pageSize
      }
      
      if (filters.category) params.category = filters.category
      if (filters.keyword) params.keyword = filters.keyword
      if (filters.priceRange && Array.isArray(filters.priceRange) && filters.priceRange.length === 2) {
        params.priceMin = filters.priceRange[0]
        params.priceMax = filters.priceRange[1]
      }
      if (filters.origin) params.origin = filters.origin
      if (filters.status !== undefined && filters.status !== null && filters.status !== '') {
        params.status = filters.status
      }
      if (filters.rating !== null && filters.rating !== undefined) {
        params.rating = filters.rating
      }
      if (filters.sortBy) params.sortBy = filters.sortBy
      if (filters.sortOrder) params.sortOrder = filters.sortOrder
      
      const res = await getTeas(params)
      
      const list = res.data?.list || res.data?.records || (Array.isArray(res.data) ? res.data : [])
      const total = res.data?.total || list.length || 0
      
      teaList.value = list
      Object.assign(pagination, {
        total: total,
        currentPage: res.data?.pageNum || pagination.currentPage,
        pageSize: res.data?.pageSize || pagination.pageSize
      })
      
      return res
    } catch (error) {
      console.error('获取茶叶列表失败:', error)
      teaList.value = []
      Object.assign(pagination, {
        total: 0,
        currentPage: 1,
        pageSize: pagination.pageSize
      })
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 获取茶叶详情
  async function fetchTeaDetail(id) {
    try {
      loading.value = true
      const res = await getTeaDetail(id)
      currentTea.value = res.data || null
      return res
    } catch (error) {
      console.error('获取茶叶详情失败:', error)
      currentTea.value = null
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 获取茶叶分类
  async function fetchCategories() {
    try {
      loading.value = true
      const res = await getTeaCategories()
      categories.value = res.data || []
      return res
    } catch (error) {
      console.error('获取茶叶分类失败:', error)
      categories.value = []
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 创建茶叶分类
  async function createCategory(categoryData) {
    try {
      const res = await createCategoryApi(categoryData)
      await fetchCategories()
      return res
    } catch (error) {
      console.error('创建茶叶分类失败:', error)
      throw error
    }
  }
  
  // 更新茶叶分类
  async function updateCategory({ id, categoryData }) {
    try {
      const res = await updateCategoryApi(id, categoryData)
      await fetchCategories()
      return res
    } catch (error) {
      console.error('更新茶叶分类失败:', error)
      throw error
    }
  }
  
  // 删除茶叶分类
  async function deleteCategory(id) {
    try {
      const res = await deleteCategoryApi(id)
      await fetchCategories()
      return res
    } catch (error) {
      console.error('删除茶叶分类失败:', error)
      throw error
    }
  }
  
  // 更新筛选条件
  function updateFilters(newFilters) {
    Object.assign(filters, newFilters)
    pagination.currentPage = 1
    fetchTeas()
  }
  
  // 重置筛选条件
  function resetFilters() {
    Object.assign(filters, {
      category: '',
      keyword: '',
      priceRange: [0, 1000],
      origin: '',
      rating: null,
      sortBy: '',
      sortOrder: 'asc'
    })
    Object.assign(pagination, {
      total: 0,
      currentPage: 1,
      pageSize: 10
    })
    fetchTeas()
  }
  
  // 设置分页
  function setPage(page) {
    pagination.currentPage = page
    fetchTeas()
  }
  
  // 设置排序
  function setSort({ sortBy, sortOrder }) {
    filters.sortBy = sortBy
    filters.sortOrder = sortOrder || 'asc'
    pagination.currentPage = 1
    fetchTeas()
  }
  
  // 添加茶叶
  async function addTea(teaData) {
    try {
      const res = await addTeaApi(teaData)
      await fetchTeas()
      return res
    } catch (error) {
      console.error('添加茶叶失败:', error)
      throw error
    }
  }
  
  // 更新茶叶
  async function updateTea(teaData) {
    try {
      const res = await updateTeaApi(teaData)
      
      if (currentTea.value && currentTea.value.id === teaData.id) {
        currentTea.value = res.data || null
      }
      
      await fetchTeas()
      return res
    } catch (error) {
      console.error('更新茶叶失败:', error)
      throw error
    }
  }
  
  // 删除茶叶
  async function deleteTea(id) {
    try {
      const res = await deleteTeaApi(id)
      await fetchTeas()
      return res
    } catch (error) {
      console.error('删除茶叶失败:', error)
      throw error
    }
  }
  
  // ========== 评价系统 Actions ==========
  
  // 获取茶叶评价列表
  async function fetchTeaReviews({ teaId, page = 1, pageSize = 10 }) {
    try {
      loading.value = true
      const res = await getTeaReviews(teaId, { page, pageSize })
      
      const list = res.data?.list || res.data?.records || (Array.isArray(res.data) ? res.data : [])
      const total = res.data?.total || list.length || 0
      
      teaReviews.value = list
      Object.assign(reviewPagination, {
        total: total,
        currentPage: res.data?.pageNum || page,
        pageSize: res.data?.pageSize || pageSize
      })
      
      return res
    } catch (error) {
      console.error('获取茶叶评价列表失败:', error)
      teaReviews.value = []
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 获取评价统计数据
  async function fetchReviewStats(teaId) {
    try {
      const res = await getReviewStats(teaId)
      reviewStats.value = res.data || null
      return res
    } catch (error) {
      console.error('获取评价统计数据失败:', error)
      reviewStats.value = null
      throw error
    }
  }
  
  // 商家回复评价
  async function replyReview({ reviewId, reply }) {
    try {
      const res = await replyReviewApi(reviewId, { reply })
      
      const review = teaReviews.value.find(r => r.id === reviewId)
      if (review) {
        review.reply = reply
        review.replyTime = res.data?.replyTime
      }
      
      return res
    } catch (error) {
      console.error('商家回复评价失败:', error)
      throw error
    }
  }
  
  // ========== 规格管理 Actions ==========
  
  // 获取茶叶规格列表
  async function fetchTeaSpecifications(teaId) {
    try {
      const res = await getTeaSpecifications(teaId)
      currentTeaSpecs.value = res.data || []
      return res
    } catch (error) {
      console.error('获取茶叶规格列表失败:', error)
      currentTeaSpecs.value = []
      throw error
    }
  }
  
  // 添加规格
  async function addSpecification({ teaId, specData }) {
    try {
      const res = await addSpecificationApi(teaId, specData)
      
      if (specData.is_default === 1) {
        currentTeaSpecs.value.forEach(spec => {
          if (res.data && spec.id !== res.data.id) {
            spec.is_default = 0
          }
        })
      }
      
      if (res.data) {
        currentTeaSpecs.value.push(res.data)
      }
      
      await fetchTeaSpecifications(teaId)
      return res
    } catch (error) {
      console.error('添加规格失败:', error)
      throw error
    }
  }
  
  // 更新规格
  async function updateSpecification({ teaId, specId, specData }) {
    try {
      const res = await updateSpecificationApi(specId, specData)
      
      if (specData.is_default === 1) {
        currentTeaSpecs.value.forEach(spec => {
          if (spec.id !== specId) {
            spec.is_default = 0
          }
        })
      }
      
      if (res.data) {
        const index = currentTeaSpecs.value.findIndex(s => s.id === specId)
        if (index !== -1) {
          currentTeaSpecs.value.splice(index, 1, res.data)
        }
      }
      
      await fetchTeaSpecifications(teaId)
      return res
    } catch (error) {
      console.error('更新规格失败:', error)
      throw error
    }
  }
  
  // 删除规格
  async function deleteSpecification({ teaId, specId }) {
    try {
      const res = await deleteSpecificationApi(specId)
      currentTeaSpecs.value = currentTeaSpecs.value.filter(s => s.id !== specId)
      await fetchTeaSpecifications(teaId)
      return res
    } catch (error) {
      console.error('删除规格失败:', error)
      throw error
    }
  }
  
  // 设置默认规格
  async function setDefaultSpecification({ teaId, specId }) {
    try {
      const res = await setDefaultSpecificationApi(specId)
      
      currentTeaSpecs.value.forEach(spec => {
        spec.is_default = spec.id === specId ? 1 : 0
      })
      
      await fetchTeaSpecifications(teaId)
      return res
    } catch (error) {
      console.error('设置默认规格失败:', error)
      throw error
    }
  }
  
  // ========== 图片管理 Actions ==========
  
  // 上传茶叶图片
  async function uploadTeaImages({ teaId, files }) {
    try {
      const formData = new FormData()
      files.forEach(file => {
        formData.append('files', file)
      })
      
      const res = await uploadTeaImagesApi(teaId, formData)
      const images = res.data || []
      teaImages.value = images
      
      if (currentTea.value && currentTea.value.id === teaId) {
        currentTea.value.images = images
      }
      
      return res
    } catch (error) {
      console.error('上传图片失败:', error)
      throw error
    }
  }
  
  // 删除茶叶图片
  async function deleteTeaImage({ teaId, imageId }) {
    try {
      const res = await deleteTeaImageApi(imageId)
      teaImages.value = teaImages.value.filter(img => img.id !== imageId)
      
      if (currentTea.value && currentTea.value.id === teaId) {
        currentTea.value.images = currentTea.value.images.filter(img => img.id !== imageId)
      }
      
      return res
    } catch (error) {
      console.error('删除图片失败:', error)
      throw error
    }
  }
  
  // 更新图片顺序
  async function updateImageOrder({ orders }) {
    try {
      const res = await updateImageOrderApi(orders)
      
      orders.forEach(({ imageId, order }) => {
        const image = teaImages.value.find(img => img.id === imageId)
        if (image) {
          image.order = order
        }
      })
      teaImages.value.sort((a, b) => (a.order || 0) - (b.order || 0))
      
      return res
    } catch (error) {
      console.error('更新图片顺序失败:', error)
      throw error
    }
  }
  
  // 设置主图
  async function setMainImage({ teaId, imageId }) {
    try {
      const res = await setMainImageApi(imageId)
      
      teaImages.value.forEach(img => {
        img.is_main = img.id === imageId ? 1 : 0
      })
      
      if (currentTea.value && currentTea.value.id === teaId) {
        const mainImage = teaImages.value.find(img => img.id === imageId)
        if (mainImage) {
          currentTea.value.main_image = mainImage.url
        }
      }
      
      return res
    } catch (error) {
      console.error('设置主图失败:', error)
      throw error
    }
  }
  
  // ========== 状态管理 Actions ==========
  
  // 更新茶叶状态
  async function toggleTeaStatus({ teaId, status }) {
    try {
      const res = await toggleTeaStatusApi(teaId, { status })
      
      const tea = teaList.value.find(t => t.id === teaId || t.id === String(teaId))
      if (tea) {
        tea.status = status
      }
      
      if (currentTea.value && (currentTea.value.id === teaId || currentTea.value.id === String(teaId))) {
        currentTea.value.status = status
      }
      
      return res
    } catch (error) {
      console.error('更新茶叶状态失败:', error)
      throw error
    }
  }
  
  // 批量更新茶叶状态
  async function batchToggleTeaStatus({ teaIds, status }) {
    try {
      const res = await batchToggleTeaStatusApi({ teaIds, status })
      
      teaIds.forEach(teaId => {
        const tea = teaList.value.find(t => t.id === teaId || t.id === String(teaId))
        if (tea) {
          tea.status = status
        }
      })
      
      if (currentTea.value && (teaIds.includes(currentTea.value.id) || teaIds.includes(String(currentTea.value.id)))) {
        currentTea.value.status = status
      }
      
      return res
    } catch (error) {
      console.error('批量更新茶叶状态失败:', error)
      throw error
    }
  }
  
  // ========== 推荐功能 Actions ==========
  
  // 获取推荐茶叶
  async function fetchRecommendTeas({ type = 'random', teaId = null, count = 6 }) {
    try {
      const params = { type, count }
      if (teaId) {
        params.teaId = teaId
      }
      
      const res = await getRecommendTeas(params)
      recommendTeas.value = res.data || []
      return res
    } catch (error) {
      console.error('获取推荐茶叶失败:', error)
      recommendTeas.value = []
      throw error
    }
  }
  
  return {
    // State
    teaList,
    currentTea,
    categories,
    loading,
    pagination,
    filters,
    teaReviews,
    reviewPagination,
    reviewStats,
    currentTeaSpecs,
    selectedSpec,
    teaImages,
    recommendTeas,
    // Getters
    categoryOptions,
    // Actions
    fetchTeas,
    fetchTeaDetail,
    fetchCategories,
    createCategory,
    updateCategory,
    deleteCategory,
    updateFilters,
    resetFilters,
    setPage,
    setSort,
    addTea,
    updateTea,
    deleteTea,
    fetchTeaReviews,
    fetchReviewStats,
    replyReview,
    fetchTeaSpecifications,
    addSpecification,
    updateSpecification,
    deleteSpecification,
    setDefaultSpecification,
    uploadTeaImages,
    deleteTeaImage,
    updateImageOrder,
    setMainImage,
    toggleTeaStatus,
    batchToggleTeaStatus,
    fetchRecommendTeas
  }
})
