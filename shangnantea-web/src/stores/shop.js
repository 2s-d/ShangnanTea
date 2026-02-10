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
      Object.assign(pagination, {
        total: total,
        currentPage: data?.pageNum || pagination.currentPage,
        pageSize: data?.pageSize || pagination.pageSize
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
  async function updateFilters(newFilters) {
    Object.assign(filters, newFilters)
    pagination.currentPage = 1
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
      
      if (data?.total !== undefined) {
        Object.assign(pagination, {
          total: data.total,
          currentPage: data.pageNum || 1,
          pageSize: data.pageSize || 10
        })
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
      await fetchShopTeas({ shopId, params: {} })
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
      
      const index = shopTeas.value.findIndex(tea => tea.id === teaId)
      if (index !== -1) {
        shopTeas.value[index] = res.data
      } else {
        const shopId = myShop.value?.id || currentShop.value?.id
        if (shopId) {
          await fetchShopTeas({ shopId, params: {} })
        }
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
        await fetchShopTeas({ shopId, params: {} })
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
      
      Object.assign(shopStatistics, {
        overview: data?.overview || null,
        trends: data?.trends || [],
        hotProducts: data?.hotProducts || []
      })
      
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
        myShop.value = { ...myShop.value, logo: logoUrl, shop_logo: logoUrl }
      }
      
      if (currentShop.value && currentShop.value.id === shopId) {
        currentShop.value = { ...currentShop.value, logo: logoUrl, shop_logo: logoUrl }
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
  async function fetchShopBanners() {
    const shopId = myShop.value?.id
    if (!shopId) {
      shopBanners.value = []
      return { code: 200, data: [] }
    }
    
    try {
      loading.value = true
      const res = await getShopBanners(shopId)
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
  async function uploadShopBanner({ file, linkUrl, sortOrder }) {
    const shopId = myShop.value?.id
    if (!shopId) {
      throw new Error('店铺ID不能为空')
    }
    
    try {
      loading.value = true
      const res = await uploadBanner(shopId, file, { linkUrl, sortOrder })
      shopBanners.value.push(res.data)
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
      const index = shopBanners.value.findIndex(b => b.id === bannerId)
      if (index !== -1) {
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
      shopBanners.value = shopBanners.value.filter(b => b.id !== bannerId)
      return res
    } catch (error) {
      console.error('删除Banner失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 更新Banner排序
  async function updateShopBannerOrder(bannerIds) {
    const shopId = myShop.value?.id
    if (!shopId) {
      throw new Error('店铺ID不能为空')
    }
    
    try {
      loading.value = true
      const res = await updateBannerOrderApi(shopId, bannerIds)
      const orderedBanners = bannerIds.map(id => 
        shopBanners.value.find(b => b.id === id)
      ).filter(Boolean)
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
      shopAnnouncements.value.push(res.data)
      shopAnnouncements.value.sort((a, b) => {
        if (a.is_top && !b.is_top) return -1
        if (!a.is_top && b.is_top) return 1
        return new Date(b.create_time) - new Date(a.create_time)
      })
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
      const index = shopAnnouncements.value.findIndex(a => a.id === announcementId)
      if (index !== -1) {
        shopAnnouncements.value[index] = res.data
        shopAnnouncements.value.sort((a, b) => {
          if (a.is_top && !b.is_top) return -1
          if (!a.is_top && b.is_top) return 1
          return new Date(b.create_time) - new Date(a.create_time)
        })
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
      const res = await apiSubmitShopReview(targetShopId, reviewData)
      shopReviews.value.unshift(res.data)
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
    loading,
    pagination,
    filters,
    shopBanners,
    shopAnnouncements,
    shopStatistics,
    isFollowingShop,
    shopReviews,
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
    createShopAnnouncement,
    updateShopAnnouncement,
    deleteShopAnnouncement,
    fetchShopReviews,
    submitShopReview
  }
})
