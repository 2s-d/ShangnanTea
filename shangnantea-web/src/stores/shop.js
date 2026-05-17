import { defineStore } from 'pinia'
import { ref, computed, reactive } from 'vue'
import { 
  getShops, 
  getShopDetail, 
  getMyShop, 
  getShopTeas, 
  updateShop as updateShopApi, 
  createShop as createShopApi,
  addShopTea as addShopTeaApi, 
  updateShopTea as updateShopTeaApi, 
  deleteShopTea as deleteShopTeaApi, 
  toggleShopTeaStatus as toggleShopTeaStatusApi,
  getShopBanners, 
  uploadBanner, 
  updateBanner as updateBannerApi, 
  deleteBanner as deleteBannerApi, 
  updateBannerOrder as updateBannerOrderApi,
  getShopAnnouncements, 
  createAnnouncement as createAnnouncementApi, 
  updateAnnouncement as updateAnnouncementApi, 
  deleteAnnouncement as deleteAnnouncementApi,
  getShopStatistics, 
  uploadShopLogo as uploadShopLogoApi,
  getShopReviews as apiGetShopReviews,
  submitShopReview as apiSubmitShopReview
} from '@/api/shop'

export const useShopStore = defineStore('shop', () => {
  // ========== State ==========
  const shopList = ref([])
  const currentShop = ref(null)
  const myShop = ref(null)
  const shopTeas = ref([])
  // 店铺列表页右侧预览用：每个店铺的部分茶叶
  const shopTeasPreviewMap = reactive({})
  // 店铺商品总数（用于列表页“商品数”）
  const shopProductCountMap = reactive({})
  const loading = ref(false)
  
  // 分页信息
  const pagination = reactive({
    total: 0,
    currentPage: 1,
    pageSize: 10
  })
  
  // 筛选条件
  const filters = reactive({
    keyword: '',
    rating: null,
    salesCount: null,
    region: '',
    sortBy: 'default',
    sortOrder: 'desc'
  })
  
  // 店铺Banner列表
  const shopBanners = ref([])
  
  // 店铺公告列表
  const shopAnnouncements = ref([])
  
  // 店铺统计
  const shopStatistics = reactive({
    overview: null,
    trends: [],
    hotProducts: []
  })
  
  // 店铺关注状态
  const isFollowingShop = ref(false)
  
  // 店铺评价相关
  const shopReviews = ref([])
  const shopRatingSummary = reactive({
    rating: 0,
    ratingCount: 0,
    userRating: null
  })
  const reviewPagination = reactive({
    total: 0,
    currentPage: 1,
    pageSize: 10
  })
  
  // ========== Getters ==========
  const myShopId = computed(() => myShop.value?.id || null)
  const hasShop = computed(() => !!myShop.value)
  
  // ========== Actions ==========
  
  // 获取商店列表
  async function fetchShops(extraParams = {}) {
    try {
      loading.value = true
      
      const params = {
        page: pagination.currentPage,
        size: pagination.pageSize,
        ...filters,
        ...extraParams
      }
      
      Object.keys(params).forEach(key => {
        if (params[key] === '' || params[key] === null || params[key] === undefined) {
          delete params[key]
        }
      })
      
      if (params.sortBy === 'default') {
        delete params.sortBy
        delete params.sortOrder
      }
      
      const res = await getShops(params)
      const data = res.data
      
      const list = data?.list || data?.records || (Array.isArray(data) ? data : [])
      const total = data?.total || list.length || 0
      
      shopList.value = list
      const requestPage = Number(params.page) || pagination.currentPage || 1
      const requestPageSize = Number(params.size || params.pageSize) || pagination.pageSize || 10
      Object.assign(pagination, {
        total: total,
        currentPage: Number(data?.pageNum || data?.page) || requestPage,
        pageSize: Number(data?.pageSize || data?.size) || requestPageSize
      })
      
      return res
    } catch (error) {
      console.error('获取店铺列表失败:', error)
      shopList.value = []
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 搜索店铺
  async function searchShops({ keyword, page = 1 }) {
    try {
      filters.keyword = keyword
      pagination.currentPage = page
      return await fetchShops({})
    } catch (error) {
      console.error('搜索店铺失败:', error)
      throw error
    }
  }
  
  // 更新筛选条件
  async function updateFilters(newFilters, targetPage = 1) {
    Object.assign(filters, newFilters)
    pagination.currentPage = targetPage
    return await fetchShops({})
  }
  
  // 重置筛选条件
  async function resetFilters() {
    Object.assign(filters, {
      keyword: '',
      rating: null,
      salesCount: null,
      region: '',
      sortBy: 'default',
      sortOrder: 'desc'
    })
    pagination.currentPage = 1
    return await fetchShops({})
  }
  
  // 设置分页
  async function setPage({ page, extraParams = {} }) {
    pagination.currentPage = page
    return await fetchShops(extraParams)
  }
  
  // 获取商店详情
  async function fetchShopDetail(id) {
    try {
      loading.value = true
      const res = await getShopDetail(id)
      currentShop.value = res.data || null
      return res
    } catch (error) {
      console.error('获取店铺详情失败:', error)
      currentShop.value = null
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 获取我的商店
  async function fetchMyShop() {
    try {
      loading.value = true
      const res = await getMyShop()
      myShop.value = res.data || null
      return res
    } catch (error) {
      console.error('获取我的店铺失败:', error)
      myShop.value = null
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 获取商店茶叶
  async function fetchShopTeas({ shopId, params = {} }) {
    try {
      loading.value = true
      const res = await getShopTeas(shopId, params)
      const data = res.data
      
      const list = data?.list || data?.records || (Array.isArray(data) ? data : [])
      shopTeas.value = list
      
      // 更新分页信息
      if (data?.total !== undefined) {
        // 保留用户设置的 pageSize，不因后端返回值而重置
        const savedPageSize = pagination.pageSize
        Object.assign(pagination, {
          total: data.total,
          currentPage: data.pageNum || data.page || 1
          // 不更新 pageSize，保留用户设置的值
        })
        // 如果后端返回的 pageSize 与当前设置不一致，说明后端可能有问题，但我们仍然保留用户设置
        if (data.pageSize && data.pageSize !== savedPageSize) {
          console.warn(`后端返回的 pageSize (${data.pageSize}) 与请求的 pageSize (${savedPageSize}) 不一致`)
        }
      }
      
      return res
    } catch (error) {
      console.error('获取店铺茶叶列表失败:', error)
      shopTeas.value = []
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 获取某个店铺用于列表预览的茶叶（最多 size 条），结果缓存在 map 中
  async function fetchShopTeasPreview(shopId, size = 3) {
    if (!shopId) {
      throw new Error('店铺ID不能为空')
    }
    
    try {
      const res = await getShopTeas(shopId, { page: 1, size })
      const data = res.data
      
      const list = data?.list || data?.records || (Array.isArray(data) ? data : [])
      // 只存入预览用的前 size 条
      shopTeasPreviewMap[shopId] = list.slice(0, size)
      // 商品总数：优先使用 total，没有则退回列表长度
      if (data && typeof data.total === 'number') {
        shopProductCountMap[shopId] = data.total
      } else {
        shopProductCountMap[shopId] = list.length
      }
      
      return res
    } catch (error) {
      console.error('获取店铺预览茶叶失败:', error)
      shopTeasPreviewMap[shopId] = []
      throw error
    }
  }
  
  // 更新商店信息
  async function updateShop(shopData) {
    try {
      loading.value = true
      
      const shopId = shopData.id || myShop.value?.id
      if (!shopId) {
        throw new Error('店铺ID不能为空')
      }
      
      const res = await updateShopApi({
        ...shopData,
        id: shopId
      })
      
      if (myShop.value && myShop.value.id === shopId) {
        myShop.value = res.data
      }
      
      if (currentShop.value && currentShop.value.id === shopId) {
        currentShop.value = res.data
      }
      
      return res
    } catch (error) {
      console.error('更新店铺信息失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 创建店铺
  async function createShop(shopData) {
    try {
      loading.value = true
      const res = await createShopApi(shopData)
      myShop.value = res.data
      return res
    } catch (error) {
      console.error('创建店铺失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // ========== 店铺茶叶管理 Actions ==========
  
  // 添加茶叶到店铺
  async function addShopTea({ shopId, teaData }) {
    try {
      loading.value = true
      const res = await addShopTeaApi(shopId, teaData)
      // 重新加载时使用当前的分页设置
      await fetchShopTeas({ 
        shopId, 
        params: {
          page: pagination.currentPage,
          size: pagination.pageSize
        }
      })
      return res
    } catch (error) {
      console.error('添加茶叶失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 更新店铺茶叶
  async function updateShopTea({ teaId, teaData }) {
    try {
      loading.value = true
      const res = await updateShopTeaApi(teaId, teaData)
      
      // 更新后重新加载列表（参考 teaStore.updateTea 的做法，确保列表数据是最新的）
      const shopId = myShop.value?.id || currentShop.value?.id
      if (shopId) {
        // 重新加载时使用当前的分页设置
        await fetchShopTeas({ 
          shopId, 
          params: {
            page: pagination.currentPage,
            size: pagination.pageSize
          }
        })
      }
      
      return res
    } catch (error) {
      console.error('更新茶叶失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 删除店铺茶叶
  async function deleteShopTea({ teaId, shopId }) {
    try {
      loading.value = true
      const res = await deleteShopTeaApi(teaId)
      shopTeas.value = shopTeas.value.filter(tea => tea.id !== teaId)
      
      if (shopId) {
        // 重新加载时使用当前的分页设置
        await fetchShopTeas({ 
          shopId, 
          params: {
            page: pagination.currentPage,
            size: pagination.pageSize
          }
        })
      }
      
      return res
    } catch (error) {
      console.error('删除茶叶失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 茶叶上下架
  async function toggleShopTeaStatus({ teaId, status }) {
    try {
      loading.value = true
      const res = await toggleShopTeaStatusApi(teaId, status)
      
      const index = shopTeas.value.findIndex(tea => tea.id === teaId)
      if (index !== -1) {
        shopTeas.value[index].status = status
      }
      
      return res
    } catch (error) {
      console.error('更新茶叶状态失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // ========== 店铺统计 Actions ==========
  
  // 获取店铺统计
  async function fetchShopStatistics({ shopId, startDate, endDate } = {}) {
    if (!shopId) {
      throw new Error('店铺ID不能为空')
    }
    
    try {
      loading.value = true
      const params = {}
      if (startDate) params.startDate = startDate
      if (endDate) params.endDate = endDate
      
      const res = await getShopStatistics(shopId, params)
      const data = res.data
      
      // 后端返回的是平铺结构，需要转换为前端期望的嵌套结构
      if (data) {
        Object.assign(shopStatistics, {
          overview: {
            totalSales: data.totalSales || 0,
            totalOrders: data.orderCount || 0,
            totalProducts: data.productCount || 0,
            totalVisitors: data.ratingCount || 0, // 访问量改为评分人数
            rating: typeof data.rating === 'number' ? data.rating : 0,
            ratingCount: data.ratingCount || 0
          },
          trends: data.trends || [],
          hotProducts: data.hotProducts || []
        })
      } else {
        Object.assign(shopStatistics, {
          overview: null,
          trends: [],
          hotProducts: []
        })
      }
      
      return res
    } catch (error) {
      console.error('获取统计数据失败:', error)
      Object.assign(shopStatistics, { overview: null, trends: [], hotProducts: [] })
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 上传店铺Logo
  async function uploadShopLogo({ shopId, file }) {
    if (!shopId || !file) {
      throw new Error('店铺ID与文件不能为空')
    }
    
    try {
      loading.value = true
      const res = await uploadShopLogoApi(shopId, file)
      const logoUrl = res.data?.url
      
      if (myShop.value && myShop.value.id === shopId) {
        myShop.value = { ...myShop.value, logo: logoUrl }
      }
      
      if (currentShop.value && currentShop.value.id === shopId) {
        currentShop.value = { ...currentShop.value, logo: logoUrl }
      }
      
      return res
    } catch (error) {
      console.error('上传Logo失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // ========== Banner 管理 Actions ==========
  
  // 获取店铺Banner列表
  // shopId 可选：
  // - 店铺详情页：传入目标店铺ID，展示该店铺的轮播图
  // - 店铺设置页：不传则使用当前商家的 myShop.id
  async function fetchShopBanners(shopId) {
    const targetShopId = shopId || myShop.value?.id
    if (!targetShopId) {
      shopBanners.value = []
      return { code: 200, data: [] }
    }
    
    try {
      loading.value = true
      const res = await getShopBanners(targetShopId)
      shopBanners.value = Array.isArray(res.data) ? res.data : []
      return res
    } catch (error) {
      console.error('获取店铺Banner列表失败:', error)
      shopBanners.value = []
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 上传店铺Banner
  async function uploadShopBanner({ file, title = '', linkUrl = '', sortOrder = null }) {
    const shopId = myShop.value?.id
    if (!shopId) {
      throw new Error('店铺ID不能为空')
    }
    if (!file) {
      throw new Error('Banner图片不能为空')
    }
    
    try {
      loading.value = true
      
      const formData = new FormData()
      // 与后端接口保持一致：@RequestParam("image") MultipartFile file
      formData.append('image', file)
      // 可选参数
      if (title) {
        formData.append('title', title)
      }
      if (linkUrl) {
        formData.append('linkUrl', linkUrl)
      }
      if (sortOrder != null) {
        formData.append('sortOrder', sortOrder)
      }
      
      const res = await uploadBanner(shopId, formData)
      // 这里不直接改动本地列表，交给调用方通过 fetchShopBanners 刷新，避免数据结构不一致
      return res
    } catch (error) {
      console.error('上传Banner失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 更新店铺Banner
  async function updateShopBanner({ bannerId, bannerData }) {
    try {
      loading.value = true
      const res = await updateBannerApi(bannerId, bannerData)
      const index = shopBanners.value.findIndex(
        b => b && String(b.id) === String(bannerId)
      )
      if (index !== -1 && res && res.data) {
        shopBanners.value[index] = res.data
      }
      return res
    } catch (error) {
      console.error('更新Banner失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 删除店铺Banner
  async function deleteShopBanner(bannerId) {
    try {
      loading.value = true
      const res = await deleteBannerApi(bannerId)
      shopBanners.value = shopBanners.value.filter(b => String(b.id) !== String(bannerId))
      return res
    } catch (error) {
      console.error('删除Banner失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 更新Banner排序
  // orderData: [{ id, order }, ...]
  async function updateShopBannerOrder(orderData) {
    try {
      loading.value = true
      const res = await updateBannerOrderApi(orderData)
      const orderedBanners = orderData
        .map(o => shopBanners.value.find(b => String(b.id) === String(o.id)))
        .filter(Boolean)
      shopBanners.value = orderedBanners
      return res
    } catch (error) {
      console.error('更新Banner排序失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // ========== 公告管理 Actions ==========
  
  // 获取店铺公告列表
  async function fetchShopAnnouncements() {
    const shopId = myShop.value?.id
    if (!shopId) {
      shopAnnouncements.value = []
      return { code: 200, data: [] }
    }
    
    try {
      loading.value = true
      const res = await getShopAnnouncements(shopId)
      shopAnnouncements.value = Array.isArray(res.data) ? res.data : []
      return res
    } catch (error) {
      console.error('获取店铺公告列表失败:', error)
      shopAnnouncements.value = []
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 创建店铺公告
  async function createShopAnnouncement(announcementData) {
    const shopId = myShop.value?.id
    if (!shopId) {
      throw new Error('店铺ID不能为空')
    }
    
    try {
      loading.value = true
      const res = await createAnnouncementApi(shopId, announcementData)
      // 后端在服务器错误时可能返回 data=undefined，防止把 undefined 推进列表导致渲染异常
      if (res && res.data) {
        shopAnnouncements.value.push(res.data)
        shopAnnouncements.value.sort((a, b) => {
          if (a.isTop && !b.isTop) return -1
          if (!a.isTop && b.isTop) return 1
          return new Date(b.createTime) - new Date(a.createTime)
        })
      }
      return res
    } catch (error) {
      console.error('创建公告失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 更新店铺公告
  async function updateShopAnnouncement({ announcementId, announcementData }) {
    try {
      loading.value = true
      const res = await updateAnnouncementApi(announcementId, announcementData)
      if (res && res.data) {
        const index = shopAnnouncements.value.findIndex(a => a.id === announcementId)
        if (index !== -1) {
          shopAnnouncements.value[index] = res.data
          shopAnnouncements.value.sort((a, b) => {
            if (a.isTop && !b.isTop) return -1
            if (!a.isTop && b.isTop) return 1
            return new Date(b.createTime) - new Date(a.createTime)
          })
        }
      }
      return res
    } catch (error) {
      console.error('更新公告失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 删除店铺公告
  async function deleteShopAnnouncement(announcementId) {
    try {
      loading.value = true
      const res = await deleteAnnouncementApi(announcementId)
      shopAnnouncements.value = shopAnnouncements.value.filter(a => a.id !== announcementId)
      return res
    } catch (error) {
      console.error('删除公告失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // ========== 评价管理 Actions ==========
  
  // 获取店铺评价列表
  async function fetchShopReviews({ shopId, params = {} }) {
    const targetShopId = shopId || currentShop.value?.id
    if (!targetShopId) {
      throw new Error('店铺ID不能为空')
    }
    
    try {
      loading.value = true
      const res = await apiGetShopReviews(targetShopId, params)
      const data = res.data
      const reviews = data?.list || (Array.isArray(data) ? data : [])
      shopReviews.value = reviews
      
      // 填充评分汇总信息（rating、ratingCount、userRating）
      if (data) {
        shopRatingSummary.rating = typeof data.rating === 'number' ? data.rating : 0
        shopRatingSummary.ratingCount = typeof data.ratingCount === 'number' ? data.ratingCount : 0
        shopRatingSummary.userRating = data.userRating ?? null
      } else {
        shopRatingSummary.rating = 0
        shopRatingSummary.ratingCount = 0
        shopRatingSummary.userRating = null
      }
      
      if (data?.total !== undefined) {
        Object.assign(reviewPagination, {
          total: data.total,
          currentPage: data.pageNum || params.page || 1,
          pageSize: data.pageSize || params.size || 10
        })
      }
      
      return res
    } catch (error) {
      console.error('获取店铺评价失败:', error)
      shopReviews.value = []
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 提交店铺评价
  async function submitShopReview({ shopId, reviewData }) {
    const targetShopId = shopId || currentShop.value?.id
    if (!targetShopId) {
      throw new Error('店铺ID不能为空')
    }
    
    try {
      loading.value = true
      // 后端当前约定：提交成功时 data 为空，仅返回状态码
      // 这里不直接改本地列表，由调用方在成功后调用 fetchShopReviews 重新拉取最新数据
      const res = await apiSubmitShopReview(targetShopId, reviewData)
      return res
    } catch (error) {
      console.error('提交店铺评价失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }
  
  return {
    // State
    shopList,
    currentShop,
    myShop,
    shopTeas,
    shopTeasPreviewMap,
    shopProductCountMap,
    loading,
    pagination,
    filters,
    shopBanners,
    shopAnnouncements,
    shopStatistics,
    isFollowingShop,
    shopReviews,
    shopRatingSummary,
    reviewPagination,
    // Getters
    myShopId,
    hasShop,
    // Actions
    fetchShops,
    searchShops,
    updateFilters,
    resetFilters,
    setPage,
    fetchShopDetail,
    fetchMyShop,
    fetchShopTeas,
    fetchShopTeasPreview,
    updateShop,
    createShop,
    addShopTea,
    updateShopTea,
    deleteShopTea,
    toggleShopTeaStatus,
    fetchShopStatistics,
    uploadShopLogo,
    fetchShopBanners,
    uploadShopBanner,
    updateShopBanner,
    deleteShopBanner,
    updateShopBannerOrder,
    fetchShopAnnouncements,
    // 公告管理：对外同时暴露语义清晰的方法名和兼容旧调用的方法名
    createShopAnnouncement,
    updateShopAnnouncement,
    deleteShopAnnouncement,
    // 兼容旧代码中使用的 createAnnouncement/updateAnnouncement/deleteAnnouncement
    createAnnouncement: createShopAnnouncement,
    updateAnnouncement: updateShopAnnouncement,
    deleteAnnouncement: deleteShopAnnouncement,
    fetchShopReviews,
    submitShopReview
  }
})
