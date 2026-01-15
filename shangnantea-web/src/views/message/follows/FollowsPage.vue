<template>
  <div class="follows-page">
    <!-- 顶部筛选栏 -->
    <div class="filter-bar">
      <div class="filter-tabs">
        <el-radio-group v-model="filterType" @change="handleFilterChange">
          <el-radio-button label="all">全部 ({{ totalCount }})</el-radio-button>
          <el-radio-button label="user">用户 ({{ followedUsers.length }})</el-radio-button>
          <el-radio-button label="shop">店铺 ({{ followedShops.length }})</el-radio-button>
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
      <!-- 全部关注 -->
      <div v-if="filterType === 'all'" class="all-follows">
        <el-empty v-if="filteredAllFollows.length === 0" description="暂无关注内容" />
        <div v-else class="mixed-list">
          <div v-for="item in filteredAllFollows" :key="`${item.type}-${item.id}`" class="follow-item">
            <!-- 用户项 -->
            <template v-if="item.type === 'user'">
              <div class="item-avatar" @click="goToUserProfile(item.userId)">
                <SafeImage :src="item.avatar" type="avatar" :alt="item.nickname" style="width:50px;height:50px;border-radius:50%;object-fit:cover;" />
                <div class="item-type-badge user-badge">用户</div>
              </div>
              <div class="item-info" @click="goToUserProfile(item.userId)">
                <div class="item-name">
                  {{ item.nickname }}
                  <span class="user-gender">
                    <el-icon color="#409EFF" v-if="item.gender === 1"><Male /></el-icon>
                    <el-icon color="#FF4949" v-else-if="item.gender === 2"><Female /></el-icon>
                  </span>
                </div>
                <div class="item-desc">{{ item.bio || '这个用户很懒，什么都没有留下...' }}</div>
                <div class="follow-time">关注于 {{ formatDate(item.followTime) }}</div>
              </div>
              <div class="item-actions">
                <el-button size="small" @click="sendMessage(item.userId)">
                  <el-icon><Message /></el-icon> 发消息
                </el-button>
                <el-button size="small" plain type="danger" @click="unfollowUser(item.userId)">
                  取消关注
                </el-button>
              </div>
            </template>
            
            <!-- 店铺项 -->
            <template v-else-if="item.type === 'shop'">
              <div class="item-avatar" @click="goToShopDetail(item.shopId)">
                <SafeImage :src="item.logo" type="banner" :alt="item.name" style="width:50px;height:50px;border-radius:8px;object-fit:cover;" />
                <div class="item-type-badge shop-badge">店铺</div>
              </div>
              <div class="item-info" @click="goToShopDetail(item.shopId)">
                <div class="item-name">{{ item.name }}</div>
                <div class="item-desc">{{ item.description || '暂无店铺介绍' }}</div>
                <div class="follow-time">关注于 {{ formatDate(item.followTime) }}</div>
              </div>
              <div class="item-actions">
                <el-button size="small" type="primary" @click="contactShop(item.shopId)">
                  <el-icon><Service /></el-icon> 联系客服
                </el-button>
                <el-button size="small" plain type="danger" @click="unfollowShop(item.shopId)">
                  取消关注
                </el-button>
              </div>
            </template>
          </div>
        </div>
      </div>

      <!-- 用户列表 -->
      <div v-else-if="filterType === 'user'" class="users-section">
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
            <div class="user-actions">
              <el-button size="small" @click="sendMessage(user.userId)">
                <el-icon><Message /></el-icon> 发消息
              </el-button>
              <el-button size="small" plain type="danger" @click="unfollowUser(user.userId)">
                取消关注
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
              <div class="shop-logo">
                <SafeImage :src="shop.logo" type="banner" :alt="shop.name" style="width:50px;height:50px;border-radius:8px;object-fit:cover;" />
              </div>
              <div class="shop-info" @click="goToShopDetail(shop.shopId)">
                <div class="shop-name">{{ shop.name }}</div>
                <div class="shop-desc">{{ shop.description || '暂无店铺介绍' }}</div>
                <div class="follow-time">关注于 {{ formatDate(shop.followTime) }}</div>
              </div>
            </div>
            <div class="shop-actions">
              <el-button size="small" type="primary" @click="contactShop(shop.shopId)">
                <el-icon><Service /></el-icon> 联系客服
              </el-button>
              <el-button size="small" plain type="danger" @click="unfollowShop(shop.shopId)">
                取消关注
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { Search, Male, Female, Message, Service } from '@element-plus/icons-vue'
import SafeImage from '@/components/common/form/SafeImage.vue'
import { apiMessage } from '@/utils/messageManager'

export default {
  name: 'FollowsPage',
  components: {
    Search, Male, Female, Message, Service, SafeImage
  },
  setup() {
    const router = useRouter()
    const store = useStore()
    
    // 筛选类型：all（全部）、user（用户）、shop（店铺）
    const filterType = ref('all')
    const searchKeyword = ref('')
    const sortOption = ref('recent')
    
    // 从Vuex获取关注列表
    const followList = computed(() => store.state.user.followList || [])
    
    // 关注的用户（从Vuex筛选）
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
    
    // 关注的店铺（从Vuex筛选）
    const followedShops = computed(() => {
      return followList.value
        .filter(item => item.targetType === 'shop')
        .map(item => ({
          id: item.id,
          shopId: item.targetId,
          name: item.targetName || '未知店铺',
          logo: item.targetAvatar || 'https://via.placeholder.com/50x50?text=店铺',
          coverImage: item.targetAvatar || 'https://via.placeholder.com/300x100?text=店铺',
          description: '', // 后端未提供，使用默认值
          followTime: item.createTime,
          popularity: 5 // 后端未提供，使用默认值
        }))
    })
    
    // 总数量
    const totalCount = computed(() => followedUsers.value.length + followedShops.value.length)
    
    // 混合列表（全部关注）
    const filteredAllFollows = computed(() => {
      let result = []
      
      // 添加用户
      followedUsers.value.forEach(user => {
        result.push({
          ...user,
          type: 'user'
        })
      })
      
      // 添加店铺
      followedShops.value.forEach(shop => {
        result.push({
          ...shop,
          type: 'shop'
        })
      })
      
      // 搜索过滤
      if (searchKeyword.value) {
        const keyword = searchKeyword.value.toLowerCase()
        result = result.filter(item => {
          if (item.type === 'user') {
            return item.nickname.toLowerCase().includes(keyword) || 
                   item.bio.toLowerCase().includes(keyword)
          } else {
            return item.name.toLowerCase().includes(keyword) || 
                   item.description.toLowerCase().includes(keyword)
          }
        })
      }
      
      // 排序
      if (sortOption.value === 'recent') {
        result.sort((a, b) => new Date(b.followTime) - new Date(a.followTime))
      } else if (sortOption.value === 'active') {
        result.sort((a, b) => {
          const aLevel = a.type === 'user' ? a.activeLevel : a.popularity
          const bLevel = b.type === 'user' ? b.activeLevel : b.popularity
          return bLevel - aLevel
        })
      }
      
      return result
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
    const handleFilterChange = (value) => {
      filterType.value = value
      // 清空搜索关键词
      searchKeyword.value = ''
    }
    
    // 格式化日期
    const formatDate = (dateString) => {
      if (!dateString) return ''
      
      const date = new Date(dateString)
      return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
    }
    
    // 跳转到用户主页
    const goToUserProfile = (userId) => {
      router.push(`/profile/${userId}`)
    }
    
    // 发送私信
    const sendMessage = (userId) => {
      // 跳转到消息中心的私信聊天页面，传递用户ID
      router.push(`/message/center/chat?userId=${userId}`)
    }
    
    // 取消关注用户
    const unfollowUser = async (userId) => {
      // 找到对应的关注记录
      const followItem = followList.value.find(item => 
        item.targetType === 'user' && item.targetId === userId
      )
      if (!followItem) {
        apiMessage.error('未找到关注记录')
        return
      }
      
      try {
        await store.dispatch('user/removeFollow', followItem.id)
        apiMessage.success('已取消关注')
      } catch (error) {
        apiMessage.error(error.message || '取消关注失败')
      }
    }
    
    // 跳转到店铺详情
    const goToShopDetail = (shopId) => {
      router.push(`/shop/${shopId}`)
    }
    
    // 联系店铺客服
    const contactShop = (shopId) => {
      // 跳转到消息中心的私信聊天页面，传递店铺ID
      router.push(`/message/center/chat?shopId=${shopId}`)
    }
    
    // 取消关注店铺
    const unfollowShop = async (shopId) => {
      // 找到对应的关注记录
      const followItem = followList.value.find(item => 
        item.targetType === 'shop' && item.targetId === shopId
      )
      if (!followItem) {
        apiMessage.error('未找到关注记录')
        return
      }
      
      try {
        await store.dispatch('user/removeFollow', followItem.id)
        apiMessage.success('已取消关注该店铺')
      } catch (error) {
        apiMessage.error(error.message || '取消关注失败')
      }
    }
    
    // 返回首页
    const goHome = () => {
      router.push('/')
    }
    
    // 加载关注列表
    const loadFollowList = async () => {
      try {
        const response = await store.dispatch('user/fetchFollowList')
        // 显示API响应消息（成功或失败都通过状态码映射显示）
        showByCode(response.code)
      } catch (error) {
        // 捕获意外的运行时错误（非API业务错误）
        // API业务失败已通过 showByCode 显示，网络错误已在响应拦截器显示
        // 这里只记录日志用于开发调试
        if (process.env.NODE_ENV === 'development') {
          console.error('[开发调试] 加载关注列表时发生意外错误：', error)
        }
      }
    }
    
    // 组件挂载时加载数据
    onMounted(() => {
      loadFollowList()
    })
    
    return {
      filterType,
      searchKeyword,
      sortOption,
      followedUsers,
      followedShops,
      totalCount,
      filteredAllFollows,
      filteredUsers,
      filteredShops,
      handleFilterChange,
      formatDate,
      goToUserProfile,
      sendMessage,
      unfollowUser,
      goToShopDetail,
      contactShop,
      unfollowShop,
      goHome,
      loadFollowList
    }
  }
}
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
        display: flex;
        padding: 15px;
        position: relative;
        
        .shop-logo {
          width: 50px;
          height: 50px;
          border-radius: 8px;
          overflow: hidden;
          margin-right: 15px;
          border: 2px solid #fff;
          position: relative;
          margin-top: -25px;
          background-color: #fff;
          
          img {
            width: 100%;
            height: 100%;
            object-fit: cover;
          }
        }
        
        .shop-info {
          flex: 1;
          cursor: pointer;
          
          .shop-name {
            font-size: 16px;
            font-weight: 500;
            color: var(--el-text-color-primary);
            margin-bottom: 5px;
          }
          
          .shop-desc {
            font-size: 13px;
            color: var(--el-text-color-regular);
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
          }
        }
      }
      
      .shop-actions {
        display: flex;
        justify-content: space-between;
        padding: 0 15px 15px;
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