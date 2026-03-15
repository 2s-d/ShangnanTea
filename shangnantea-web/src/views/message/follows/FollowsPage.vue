<template>
  <div class="follows-page">
    <!-- 顶部筛选栏 -->
    <div class="filter-bar">
      <div class="filter-tabs">
        <el-radio-group v-model="filterType" @change="handleFilterChange">
          <el-radio-button value="user">用户 ({{ followedUsers.length }})</el-radio-button>
          <el-radio-button value="shop">店铺 ({{ followedShops.length }})</el-radio-button>
        </el-radio-group>
      </div>
      <div class="filter-actions">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索关注的用户或店铺"
          size="small"
          clearable
          style="width: 250px"
        >
          <template #suffix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select v-model="sortOption" placeholder="排序方式" size="small" style="margin-left: 10px; width: 120px">
          <el-option label="最近关注" value="recent"></el-option>
          <el-option label="活跃度" value="active"></el-option>
        </el-select>
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="content-area">
      <!-- 用户列表 -->
      <div v-if="filterType === 'user'" class="users-section">
        <el-empty v-if="filteredUsers.length === 0" description="暂无关注用户" />
        <div v-else class="users-list">
          <div v-for="user in filteredUsers" :key="user.id" class="user-item">
            <div class="user-avatar" @click="goToUserProfile(user.userId)">
              <SafeImage :src="user.avatar" type="avatar" :alt="user.nickname" style="width:60px;height:60px;border-radius:50%;object-fit:cover;" />
            </div>
            <div class="user-info" @click="goToUserProfile(user.userId)">
              <div class="user-name">
                {{ user.nickname }}
                <span class="user-gender">
                  <el-icon color="#409EFF" v-if="user.gender === 1"><Male /></el-icon>
                  <el-icon color="#FF4949" v-else-if="user.gender === 2"><Female /></el-icon>
                </span>
              </div>
              <div class="user-bio">{{ user.bio || '这个用户很懒，什么都没有留下...' }}</div>
              <div class="follow-time">关注于 {{ formatDate(user.followTime) }}</div>
            </div>
            <div class="user-actions" v-if="user.userId !== currentUserId">
              <el-button class="action-btn action-btn-private" size="small" @click="privateMessage(user.userId)">
                <el-icon><Message /></el-icon> 私信
              </el-button>
              <el-button
                class="action-btn"
                :class="{ 'action-btn-follow': !isLocallyFollowed('user', user.userId) }"
                size="small"
                plain
                :type="isLocallyFollowed('user', user.userId) ? 'danger' : 'primary'"
                @click="toggleFollowUser(user)"
              >
                {{ isLocallyFollowed('user', user.userId) ? '取消关注' : '+关注' }}
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 店铺列表 -->
      <div v-else-if="filterType === 'shop'" class="shops-section">
        <el-empty v-if="filteredShops.length === 0" description="暂无关注店铺" />
        <div v-else class="shops-grid">
          <div v-for="shop in filteredShops" :key="shop.id" class="shop-card">
            <div class="shop-cover" @click="goToShopDetail(shop.shopId)">
              <SafeImage :src="shop.coverImage" type="banner" :alt="shop.name" style="width:100%;height:100%;object-fit:cover;" />
            </div>
            <div class="shop-content">
              <div class="shop-header">
              <div class="shop-logo">
                <div class="shop-logo-click" @click="goToShopDetail(shop.shopId)">
                  <SafeImage :src="shop.logo" type="banner" :alt="shop.name" style="width:50px;height:50px;border-radius:8px;object-fit:cover;cursor:pointer;" />
                </div>
              </div>
              <div class="shop-info" @click="goToShopDetail(shop.shopId)">
                <div class="shop-name">{{ shop.name }}</div>
                <div class="follow-time">关注于 {{ formatDate(shop.followTime) }}</div>
                </div>
              </div>
              <div class="shop-desc-wrapper" @click="goToShopDetail(shop.shopId)">
                <div class="shop-desc">{{ shop.description || '暂无店铺介绍' }}</div>
              </div>
            </div>
            <div class="shop-actions">
              <el-button class="action-btn action-btn-contact" size="small" type="primary" @click="contactShop(shop.shopId)">
                <el-icon><Service /></el-icon> 联系客服
              </el-button>
              <el-button
                class="action-btn"
                :class="{ 'action-btn-follow': !isLocallyFollowed('shop', shop.shopId) }"
                size="small"
                plain
                :type="isLocallyFollowed('shop', shop.shopId) ? 'danger' : 'primary'"
                @click="toggleFollowShop(shop)"
              >
                {{ isLocallyFollowed('shop', shop.shopId) ? '取消关注' : '+关注' }}
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useMessageStore } from '@/stores/message'
import { ElMessage } from 'element-plus'
import { Search, Male, Female, Message, Service } from '@element-plus/icons-vue'
import SafeImage from '@/components/common/form/SafeImage.vue'
import { showByCode } from '@/utils/apiMessages'
import { getFollowList } from '@/api/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const messageStore = useMessageStore()

/**
 * 本地关注态：用于“取消关注后不立即从列表消失”，但按钮文案/样式立刻切换
 * 下次刷新（重新进入页面/刷新页面）后才会从列表消失
 */
const localFollowState = ref({})

/**
 * 关注列表本地快照：避免 userStore.removeFollow() 立即改动 Pinia 列表导致 UI 立刻消失。
 * - 首次进入/手动刷新：用后端最新数据覆盖快照（此时取消关注的会消失）
 * - 点击取消关注/关注：仅切 localFollowState，不改快照内容
 */
const localFollowList = ref([])
// 当前登录用户的关注快照：用于计算“我是否关注了该条目”
const viewerFollowList = ref([])

const localKey = (type, targetId) => `${type}:${String(targetId)}`

const viewerFollowSet = computed(() => {
  const set = new Set()
  ;(viewerFollowList.value || []).forEach((item) => {
    if (!item) return
    set.add(localKey(item.targetType, item.targetId))
  })
  return set
})

/**
 * 当前登录用户是否关注该目标（按钮状态的唯一依据）
 * - 默认来自 viewerFollowSet
 * - 若本页做过切换，则用 localFollowState 覆盖（乐观更新 + 回滚）
 */
const isLocallyFollowed = (type, targetId) => {
  const key = localKey(type, targetId)
  if (Object.prototype.hasOwnProperty.call(localFollowState.value, key)) {
    return !!localFollowState.value[key]
  }
  return viewerFollowSet.value.has(key)
}

const setLocallyFollowed = (type, targetId, followed) => {
  // 使用 Vue 的响应式更新方式，确保触发响应式更新
  const key = localKey(type, targetId)
  localFollowState.value = {
    ...localFollowState.value,
    [key]: !!followed
  }
}

// 当前登录用户ID（用于处理 /profile/current/*）
const currentUserId = computed(() => {
  const base = userStore.userInfo || {}
  return base.id || base.userId || null
})

// 当前正在查看的主页用户ID：查看别人的主页时，关注列表应该是“对方的关注”
const profileUserId = computed(() => {
  const firstParam = route.params.userId
  if (!firstParam || firstParam === 'current') return currentUserId.value
  if (firstParam === 'published' || firstParam === 'follows' || firstParam === 'favorites') return currentUserId.value
  return firstParam
})
    
    // 筛选类型：user（用户）、shop（店铺）
    const filterType = ref('user')
    const searchKeyword = ref('')
    const sortOption = ref('recent')
    
    // 使用本地快照渲染（避免取消关注立即从列表消失）
    const followList = computed(() => localFollowList.value || [])
    
    // 关注的用户（从Pinia筛选）
    const followedUsers = computed(() => {
      return followList.value
        .filter(item => item.targetType === 'user')
        .map(item => ({
          id: item.id,
          userId: item.targetId,
          nickname: item.targetName || '未知用户',
          avatar: item.targetAvatar || 'https://via.placeholder.com/50x50?text=用户',
          gender: 1, // 后端未提供，使用默认值
          bio: '', // 后端未提供，使用默认值
          followTime: item.createTime,
          activeLevel: 5 // 后端未提供，使用默认值
        }))
    })
    
    // 关注的店铺（从Pinia筛选）
    const followedShops = computed(() => {
      return followList.value
        .filter(item => item.targetType === 'shop')
        .map(item => ({
          id: item.id,
          shopId: item.targetId,
          name: item.targetName || '未知店铺',
          logo: item.targetLogo || item.targetAvatar || 'https://via.placeholder.com/50x50?text=店铺',
          coverImage: item.targetAvatar || item.targetLogo || 'https://via.placeholder.com/300x100?text=店铺',
          description: item.targetDescription || '暂无店铺介绍',
          followTime: item.createTime,
          popularity: 5 // 后端未提供，使用默认值
        }))
    })
    
    // 过滤和排序用户
    const filteredUsers = computed(() => {
      let result = [...followedUsers.value]
      
      // 搜索过滤
      if (searchKeyword.value) {
        const keyword = searchKeyword.value.toLowerCase()
        result = result.filter(user => 
          user.nickname.toLowerCase().includes(keyword) || 
          user.bio.toLowerCase().includes(keyword)
        )
      }
      
      // 排序
      if (sortOption.value === 'recent') {
        result.sort((a, b) => new Date(b.followTime) - new Date(a.followTime))
      } else if (sortOption.value === 'active') {
        result.sort((a, b) => b.activeLevel - a.activeLevel)
      }
      
      return result
    })
    
    // 过滤和排序店铺
    const filteredShops = computed(() => {
      let result = [...followedShops.value]
      
      // 搜索过滤
      if (searchKeyword.value) {
        const keyword = searchKeyword.value.toLowerCase()
        result = result.filter(shop => 
          shop.name.toLowerCase().includes(keyword) || 
          shop.description.toLowerCase().includes(keyword)
        )
      }
      
      // 排序
      if (sortOption.value === 'recent') {
        result.sort((a, b) => new Date(b.followTime) - new Date(a.followTime))
      } else if (sortOption.value === 'popular') {
        result.sort((a, b) => b.popularity - a.popularity)
      }
      
      return result
    })
    
    // 处理筛选类型变化
    const handleFilterChange = value => {
      filterType.value = value
      // 清空搜索关键词
      searchKeyword.value = ''
    }
    
    // 格式化日期
    const formatDate = dateString => {
      if (!dateString) return ''
      
      const date = new Date(dateString)
      return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
    }
    
    // 跳转到用户主页
    const goToUserProfile = userId => {
      router.push(`/profile/${userId}`)
    }
    
    /**
     * 私信：关注页只允许“用户身份”发起用户→用户私聊
     */
    const privateMessage = (userId) => {
      if (!userId) return
      router.push({
        path: '/message/chat',
        query: { userId: String(userId) }
      })
    }
    
    // 切换关注用户（乐观更新 + 延迟同步）
    const toggleFollowUser = async (user) => {
      const userId = user?.userId
      if (!userId) return
      
      // 判断是否是查看自己的列表
      const isSelf = currentUserId.value && profileUserId.value && String(profileUserId.value) === String(currentUserId.value)
      
      const currentlyFollowed = isLocallyFollowed('user', userId)
      // 先乐观更新UI，避免"接口成功但按钮样式不变"
      setLocallyFollowed('user', userId, !currentlyFollowed)
      try {
        if (currentlyFollowed) {
          const res = await userStore.removeFollow({ targetId: userId, targetType: 'user' })
          showByCode(res.code)
          // 2013: 已取消关注；2124: 关注记录不存在/已删除（幂等视为已取消）
          if (!(res.code === 2013 || res.code === 2124)) {
            setLocallyFollowed('user', userId, true)
          } else {
            // 操作成功：如果是别人的列表，立即刷新 viewerList 确保按钮状态准确
            if (!isSelf) {
              try {
                const resViewer = await getFollowList()
                viewerFollowList.value = (resViewer.data || []).map(i => ({ ...i }))
              } catch (err) {
                if (process.env.NODE_ENV === 'development') {
                  console.error('刷新查看者关注列表失败:', err)
                }
              }
            }
          }
        } else {
          const res = await userStore.addFollow({
            targetId: userId,
            targetType: 'user',
            targetName: user?.nickname,
            targetAvatar: user?.avatar
          })
          showByCode(res.code)
          // 2012: 已关注；如果后端返回其它成功码，也一律视为已关注
          if (!(res.code === 2012 || String(res.code).startsWith('7'))) {
            setLocallyFollowed('user', userId, false)
          } else {
            // 操作成功：如果是别人的列表，立即刷新 viewerList 确保按钮状态准确
            if (!isSelf) {
              try {
                const resViewer = await getFollowList()
                viewerFollowList.value = (resViewer.data || []).map(i => ({ ...i }))
              } catch (err) {
                if (process.env.NODE_ENV === 'development') {
                  console.error('刷新查看者关注列表失败:', err)
                }
              }
            }
          }
        }
      } catch (error) {
        // 网络/异常：回滚UI
        setLocallyFollowed('user', userId, currentlyFollowed)
        if (process.env.NODE_ENV === 'development') console.error('关注用户切换失败:', error)
      }
    }

    // 兼容旧方法名
    const unfollowUser = async (userId) => toggleFollowUser({ userId })
    
    // 跳转到店铺详情
    const goToShopDetail = shopId => {
      router.push(`/shop/${shopId}`)
    }
    
    // 联系店铺客服（用户身份发起 customer 会话）
    const contactShop = (shopId) => {
      if (!shopId) return
      router.push({
        path: '/message/chat',
        query: { shopId: String(shopId) }
      })
    }
    
    // 切换关注店铺（乐观更新 + 延迟同步）
    const toggleFollowShop = async (shop) => {
      const shopId = shop?.shopId
      if (!shopId) return
      
      // 判断是否是查看自己的列表
      const isSelf = currentUserId.value && profileUserId.value && String(profileUserId.value) === String(currentUserId.value)
      
      const currentlyFollowed = isLocallyFollowed('shop', shopId)
      setLocallyFollowed('shop', shopId, !currentlyFollowed)
      try {
        if (currentlyFollowed) {
          const res = await userStore.removeFollow({ targetId: shopId, targetType: 'shop' })
          showByCode(res.code)
          if (!(res.code === 2013 || res.code === 2124)) {
            setLocallyFollowed('shop', shopId, true)
          } else {
            // 操作成功：如果是别人的列表，立即刷新 viewerList 确保按钮状态准确
            if (!isSelf) {
              try {
                const resViewer = await getFollowList()
                viewerFollowList.value = (resViewer.data || []).map(i => ({ ...i }))
              } catch (err) {
                if (process.env.NODE_ENV === 'development') {
                  console.error('刷新查看者关注列表失败:', err)
                }
              }
            }
          }
        } else {
          const res = await userStore.addFollow({
            targetId: shopId,
            targetType: 'shop',
            targetName: shop?.name,
            targetAvatar: shop?.logo
          })
          showByCode(res.code)
          if (!(res.code === 2012 || String(res.code).startsWith('7'))) {
            setLocallyFollowed('shop', shopId, false)
          } else {
            // 操作成功：如果是别人的列表，立即刷新 viewerList 确保按钮状态准确
            if (!isSelf) {
              try {
                const resViewer = await getFollowList()
                viewerFollowList.value = (resViewer.data || []).map(i => ({ ...i }))
              } catch (err) {
                if (process.env.NODE_ENV === 'development') {
                  console.error('刷新查看者关注列表失败:', err)
                }
              }
            }
          }
        }
      } catch (error) {
        setLocallyFollowed('shop', shopId, currentlyFollowed)
        if (process.env.NODE_ENV === 'development') console.error('关注店铺切换失败:', error)
      }
    }

    // 兼容旧方法名
    const unfollowShop = async (shopId) => toggleFollowShop({ shopId })
    
    // 返回首页
    const goHome = () => {
      router.push('/')
    }
    
    // 加载关注列表（主页用户 + 当前登录用户）
    // 注意：初始数据在 UserHomePage 的 loadUserData 中统一调用，这里只处理用户操作
    const loadFollowList = async () => {
      // 如果未登录，不调用接口（避免退出登录时触发）
      if (!userStore.isLoggedIn) {
        localFollowList.value = []
        viewerFollowList.value = []
        return
      }
      
      // 参考统计接口的处理逻辑：判断主页是否可见
      const isSelf = currentUserId.value && profileUserId.value && String(profileUserId.value) === String(currentUserId.value)
      const profileVisible = messageStore.userProfile?.profileVisible !== false
      
      // 仅在本人或对方允许查看时才请求关注接口；否则直接置空并不发请求
      if (!isSelf && !profileVisible) {
        localFollowList.value = []
        viewerFollowList.value = []
        return
      }
      
      try {
        // 1) 主页用户的关注：用于渲染"他关注了谁/哪家店"
        const resOwner = await getFollowList(null, profileUserId.value)
        showByCode(resOwner.code)
        localFollowList.value = (resOwner.data || []).map(i => ({ ...i }))

        // 2) 当前登录用户的关注：用于按钮状态（我是否关注该条目）
        const resViewer = await getFollowList()
        viewerFollowList.value = (resViewer.data || []).map(i => ({ ...i }))
      } catch (error) {
        // 捕获意外的运行时错误（非API业务错误）
        // API业务失败已通过 showByCode 显示，网络错误已在响应拦截器显示
        // 这里只记录日志用于开发调试
        if (process.env.NODE_ENV === 'development') {
          console.error('[开发调试] 加载关注列表时发生意外错误：', error)
        }
      }
    }
    
// 组件挂载时：等待 UserHomePage 的 loadUserData 完成，然后检查 profileVisible 再决定是否调用接口
onMounted(async () => {
  // 如果未登录，不调用接口
  if (!userStore.isLoggedIn) {
    localFollowList.value = []
    viewerFollowList.value = []
    return
  }
  
  // 等待基础信息加载完成（如果还没有加载的话）
  let retryCount = 0
  while (!messageStore.userProfile && retryCount < 10) {
    await new Promise(resolve => setTimeout(resolve, 100))
    retryCount++
  }
  
  // 参考统计接口的处理逻辑：判断主页是否可见
  const isSelf = currentUserId.value && profileUserId.value && String(profileUserId.value) === String(currentUserId.value)
  const profileVisible = messageStore.userProfile?.profileVisible !== false
  
  // 仅在本人或对方允许查看时才请求关注接口；否则直接置空并不发请求
  if (isSelf || profileVisible) {
  loadFollowList()
  } else {
    localFollowList.value = []
    viewerFollowList.value = []
  }
})

// 查看不同用户主页时，刷新关注快照并清空本页的本地切换态
watch(() => profileUserId.value, async () => {
  // 如果未登录，不调用接口
  if (!userStore.isLoggedIn) {
    localFollowList.value = []
    viewerFollowList.value = []
  localFollowState.value = {}
    return
  }
  
  localFollowState.value = {}
  
  // 等待基础信息加载完成
  let retryCount = 0
  while (!messageStore.userProfile && retryCount < 10) {
    await new Promise(resolve => setTimeout(resolve, 100))
    retryCount++
  }
  
  // 参考统计接口的处理逻辑：判断主页是否可见
  const isSelf = currentUserId.value && profileUserId.value && String(profileUserId.value) === String(currentUserId.value)
  const profileVisible = messageStore.userProfile?.profileVisible !== false
  
  // 仅在本人或对方允许查看时才请求关注接口；否则直接置空并不发请求
  if (isSelf || profileVisible) {
  loadFollowList()
  } else {
    localFollowList.value = []
    viewerFollowList.value = []
  }
})
</script>

<style lang="scss" scoped>
.follows-page {
  padding: 40px 0;
  
  .page-title {
    font-size: 2rem;
    color: var(--text-primary);
    margin-bottom: 10px;
    text-align: center;
  }
  
  .page-desc {
    font-size: 1.2rem;
    color: var(--text-secondary);
    margin-bottom: 40px;
    text-align: center;
  }
  
  .coming-soon {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 50px;
    background-color: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    
    img {
      width: 200px;
      margin-bottom: 30px;
    }
    
    p {
      font-size: 1.2rem;
      color: var(--text-regular);
      margin-bottom: 30px;
    }
  }
  
  // 筛选栏样式
  .filter-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding: 15px;
    background-color: #f8f9fa;
    border-radius: 8px;
    
    .filter-tabs {
      .el-radio-group {
        .el-radio-button {
          margin-right: 0;
        }
      }
    }
    
    .filter-actions {
      display: flex;
      align-items: center;
    }
  }
  
  // 内容区域
  .content-area {
    min-height: 400px;
  }
  
  // 混合列表样式
  .mixed-list {
    .follow-item {
      display: flex;
      align-items: center;
      padding: 15px;
      margin-bottom: 15px;
      background-color: #fff;
      border-radius: 8px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      transition: all 0.3s;
      
      &:hover {
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        transform: translateY(-2px);
      }
      
      .item-avatar {
        position: relative;
        width: 60px;
        height: 60px;
        margin-right: 15px;
        cursor: pointer;
        
        .item-type-badge {
          position: absolute;
          top: -5px;
          right: -5px;
          padding: 2px 6px;
          font-size: 10px;
          border-radius: 10px;
          color: white;
          
          &.user-badge {
            background-color: #409EFF;
          }
          
          &.shop-badge {
            background-color: #67C23A;
          }
        }
      }
      
      .item-info {
        flex: 1;
        cursor: pointer;
        
        .item-name {
          font-size: 16px;
          font-weight: 500;
          color: var(--el-text-color-primary);
          margin-bottom: 5px;
          display: flex;
          align-items: center;
          
          .user-gender {
            margin-left: 5px;
          }
        }
        
        .item-desc {
          font-size: 14px;
          color: var(--el-text-color-regular);
          margin-bottom: 5px;
          display: -webkit-box;
          -webkit-line-clamp: 1;
          -webkit-box-orient: vertical;
          overflow: hidden;
        }
        
        .follow-time {
          font-size: 12px;
          color: var(--el-text-color-secondary);
        }
      }
      
      .item-actions {
        display: flex;
        gap: 10px;
      }
    }
  }

  // 统一操作按钮尺寸与私信配色（参考个人主页私信按钮）
  .action-btn {
    width: 95px;
    height: 30px;
    padding: 0;
    border-radius: 8px;
    font-size: 15px;
    font-weight: 500;
  }

  .action-btn-private {
    border: 1px solid #9fd7a8;
    background-color: #e4f7e8;
    color: #2c8f4a;

    &:hover {
      border-color: #84cb92;
      background-color: #d8f2de;
      color: #237a3d;
    }
  }

  .action-btn-contact {
    // primary按钮保持原主题色，仅做尺寸统一
    font-weight: 600;
  }

  // “+关注”按钮浅绿色底色（覆盖 ElementPlus plain primary 的默认样式，让状态一眼可见）
  .action-btn-follow {
    border-color: #9fd7a8 !important;
    background-color: #e4f7e8 !important;
    color: #2c8f4a !important;

    &:hover {
      border-color: #84cb92 !important;
      background-color: #d8f2de !important;
      color: #237a3d !important;
    }
  }
  
  // 用户列表样式
  .users-list {
    .user-item {
      display: flex;
      align-items: center;
      padding: 15px;
      margin-bottom: 15px;
      background-color: #f9f9f9;
      border-radius: 6px;
      transition: all 0.3s;
      
      &:hover {
        background-color: #f0f0f0;
      }
      
      .user-avatar {
        width: 60px;
        height: 60px;
        border-radius: 50%;
        overflow: hidden;
        margin-right: 15px;
        cursor: pointer;
        
        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
      }
      
      .user-info {
        flex: 1;
        cursor: pointer;
        
        .user-name {
          font-size: 16px;
          font-weight: 500;
          color: var(--el-text-color-primary);
          margin-bottom: 5px;
          display: flex;
          align-items: center;
          
          .user-gender {
            margin-left: 5px;
          }
        }
        
        .user-bio {
          font-size: 14px;
          color: var(--el-text-color-regular);
          margin-bottom: 5px;
          display: -webkit-box;
          -webkit-line-clamp: 1;
          -webkit-box-orient: vertical;
          overflow: hidden;
        }
        
        .follow-time {
          font-size: 12px;
          color: var(--el-text-color-secondary);
        }
      }
      
      .user-actions {
        display: flex;
        gap: 10px;
      }
    }
  }
  
  // 店铺卡片样式
  .shops-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 20px;
    
    .shop-card {
      background-color: #fff;
      border-radius: 8px;
      overflow: hidden;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
      transition: all 0.3s;
      display: flex;
      flex-direction: column;
      
      &:hover {
        transform: translateY(-5px);
        box-shadow: 0 5px 15px rgba(0, 0, 0, 0.15);
      }
      
      .shop-cover {
        height: 120px;
        overflow: hidden;
        cursor: pointer;
        
        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
          transition: transform 0.3s;
          
          &:hover {
            transform: scale(1.05);
          }
        }
      }
      
      .shop-content {
        padding: 15px;
        display: flex;
        flex-direction: column;
        flex: 1;
        min-height: 0;
        
        .shop-header {
          display: flex;
          align-items: flex-start;
          margin-bottom: 12px;
        
        .shop-logo {
          width: 50px;
          height: 50px;
          border-radius: 8px;
          overflow: hidden;
            margin-right: 12px;
          border: 2px solid #fff;
          position: relative;
          margin-top: -25px;
          background-color: #fff;
            flex-shrink: 0;
          
          img {
            width: 100%;
            height: 100%;
            object-fit: cover;
          }
        }
        
        .shop-info {
          flex: 1;
          cursor: pointer;
            min-width: 0;
          
          .shop-name {
            font-size: 16px;
            font-weight: 500;
            color: var(--el-text-color-primary);
              margin-bottom: 6px;
              line-height: 1.4;
              word-break: break-word;
            }
            
            .follow-time {
              font-size: 12px;
              color: var(--el-text-color-secondary);
              line-height: 1.4;
            }
          }
        }
        
        .shop-desc-wrapper {
          cursor: pointer;
          flex: 1;
          min-height: 40px;
          display: flex;
          align-items: flex-start;
          
          .shop-desc {
            font-size: 13px;
            color: var(--el-text-color-regular);
            line-height: 1.5;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
            text-overflow: ellipsis;
            min-height: 39px; // 固定两行高度：13px * 1.5 * 2 = 39px
          }
        }
      }
      
      .shop-actions {
        display: flex;
        justify-content: space-between;
        gap: 10px;
        padding: 0 15px 15px;
        margin-top: auto;
      }
    }
  }
}

// 响应式样式
@media (max-width: 768px) {
  .follows-page {
    .filter-bar {
      flex-direction: column;
      align-items: flex-start;
      
      .filter-tabs {
        margin-bottom: 15px;
        width: 100%;
        
        .el-radio-group {
          width: 100%;
          
          .el-radio-button {
            flex: 1;
          }
        }
      }
      
      .filter-actions {
        width: 100%;
        flex-direction: column;
        
        .el-input {
          width: 100%;
          margin-bottom: 10px;
        }
        
        .el-select {
          width: 100%;
          margin-left: 0 !important;
        }
      }
    }
    
    .mixed-list {
      .follow-item {
        flex-direction: column;
        align-items: flex-start;
        
        .item-avatar {
          margin-bottom: 10px;
          align-self: center;
        }
        
        .item-info {
          margin-bottom: 15px;
          width: 100%;
          text-align: center;
        }
        
        .item-actions {
          width: 100%;
          justify-content: center;
        }
      }
    }
    
    .users-list {
      .user-item {
        flex-direction: column;
        align-items: flex-start;
        
        .user-avatar {
          margin-bottom: 10px;
          align-self: center;
        }
        
        .user-info {
          margin-bottom: 15px;
          width: 100%;
          text-align: center;
        }
        
        .user-actions {
          width: 100%;
          justify-content: center;
        }
      }
    }
    
    .shops-grid {
      grid-template-columns: 1fr;
    }
  }
}
</style> 