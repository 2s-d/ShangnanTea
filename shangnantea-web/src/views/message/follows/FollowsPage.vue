<template>
  <div class="follows-page">
    <!-- 分类标签页 -->
    <div class="follows-tabs">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="关注的用户" name="users">
          <!-- 用户列表筛选和排序 -->
          <div class="list-header">
            <div class="list-title">关注的用户 ({{followedUsers.length}})</div>
            <div class="list-actions">
              <el-input
                v-model="searchKeyword"
                placeholder="搜索用户名"
                size="small"
                clearable
                style="width: 200px"
              >
                <template #suffix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
              <el-select v-model="sortOption" placeholder="排序方式" size="small" style="margin-left: 10px">
                <el-option label="最近关注" value="recent"></el-option>
                <el-option label="用户活跃度" value="active"></el-option>
              </el-select>
            </div>
          </div>
          
          <!-- 用户列表 -->
          <el-empty v-if="filteredUsers.length === 0" description="暂无关注用户" />
          
          <div v-else class="users-list">
            <div v-for="user in filteredUsers" :key="user.id" class="user-item">
              <div class="user-avatar" @click="goToUserProfile(user.id)">
                <SafeImage :src="user.avatar" type="avatar" :alt="user.nickname" style="width:50px;height:50px;border-radius:50%;object-fit:cover;" />
              </div>
              <div class="user-info" @click="goToUserProfile(user.id)">
                <div class="user-name">
                  {{ user.nickname }}
                  <span class="user-gender">
                    <el-icon color="#409EFF" v-if="user.gender === 1"><Male /></el-icon>
                    <el-icon color="#FF4949" v-else-if="user.gender === 2"><Female /></el-icon>
                  </span>
                </div>
                <div class="user-bio">{{ user.bio }}</div>
                <div class="follow-time">关注于 {{ formatDate(user.followTime) }}</div>
              </div>
              <div class="user-actions">
                <el-button size="small" @click="sendMessage(user.id)">
                  <el-icon><Message /></el-icon> 发消息
                </el-button>
                <el-button size="small" plain type="danger" @click="unfollowUser(user.id)">
                  取消关注
                </el-button>
              </div>
            </div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="关注的店铺" name="shops">
          <!-- 店铺列表筛选和排序 -->
          <div class="list-header">
            <div class="list-title">关注的店铺 ({{followedShops.length}})</div>
            <div class="list-actions">
              <el-input
                v-model="shopSearchKeyword"
                placeholder="搜索店铺名"
                size="small"
                clearable
                style="width: 200px"
              >
                <template #suffix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
              <el-select v-model="shopSortOption" placeholder="排序方式" size="small" style="margin-left: 10px">
                <el-option label="最近关注" value="recent"></el-option>
                <el-option label="店铺热度" value="popular"></el-option>
              </el-select>
            </div>
          </div>
          
          <!-- 店铺卡片列表 -->
          <el-empty v-if="filteredShops.length === 0" description="暂无关注店铺" />
          
          <div v-else class="shops-grid">
            <div v-for="shop in filteredShops" :key="shop.id" class="shop-card">
              <div class="shop-cover" @click="goToShopDetail(shop.id)">
                <SafeImage :src="shop.logo" type="banner" :alt="shop.name" style="width:100%;height:100%;object-fit:cover;" />
              </div>
              <div class="shop-content">
                <div class="shop-logo">
                  <SafeImage :src="shop.logo" type="banner" :alt="shop.name" style="width:50px;height:50px;border-radius:50%;object-fit:cover;" />
                </div>
                <div class="shop-info" @click="goToShopDetail(shop.id)">
                  <div class="shop-name">{{ shop.name }}</div>
                  <div class="shop-desc">{{ shop.description }}</div>
                </div>
              </div>
              <div class="shop-actions">
                <el-button size="small" type="primary" @click="contactShop(shop.id)">
                  <el-icon><Service /></el-icon> 联系客服
                </el-button>
                <el-button size="small" plain type="danger" @click="unfollowShop(shop.id)">
                  取消关注
                </el-button>
              </div>
            </div>
      </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Male, Female, Message, Service } from '@element-plus/icons-vue'
import SafeImage from '@/components/common/form/SafeImage.vue'

export default {
  name: 'FollowsPage',
  components: {
    Search, Male, Female, Message, Service, SafeImage
  },
  setup() {
    const router = useRouter()
    const activeTab = ref('users')
    
    // 关注的用户
    const searchKeyword = ref('')
    const sortOption = ref('recent')
    const followedUsers = ref([
      {
        id: 1,
        nickname: '茶香四溢',
        avatar: 'https://via.placeholder.com/50x50?text=茶香',
        gender: 1,
        bio: '喜欢各种绿茶，平时喜欢分享茶道心得。',
        followTime: '2025-03-10 15:23:45',
        activeLevel: 9
      },
      {
        id: 2,
        nickname: '茶艺小能手',
        avatar: 'https://via.placeholder.com/50x50?text=茶艺',
        gender: 2,
        bio: '专注白茶十年，欢迎交流~',
        followTime: '2025-02-25 09:12:33',
        activeLevel: 7
      },
      {
        id: 3,
        nickname: '普洱控',
        avatar: 'https://via.placeholder.com/50x50?text=普洱',
        gender: 1,
        bio: '收藏各种年份的普洱茶，喜欢探讨茶叶存放心得。',
        followTime: '2025-01-15 18:45:21',
        activeLevel: 8
      }
    ])
    
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
    
    // 关注的店铺
    const shopSearchKeyword = ref('')
    const shopSortOption = ref('recent')
    const followedShops = ref([
      {
        id: 101,
        name: '秦岭茗茶',
        logo: 'https://via.placeholder.com/50x50?text=秦岭',
        coverImage: 'https://via.placeholder.com/300x100?text=秦岭茗茶',
        description: '专注于陕南高山茶叶，提供各种绿茶、红茶产品。',
        followTime: '2025-03-05 14:30:12',
        popularity: 95
      },
      {
        id: 102,
        name: '云雾茶庄',
        logo: 'https://via.placeholder.com/50x50?text=云雾',
        coverImage: 'https://via.placeholder.com/300x100?text=云雾茶庄',
        description: '销售高品质的云雾绿茶，茶叶口感清新爽口。',
        followTime: '2025-02-18 11:24:36',
        popularity: 87
      },
      {
        id: 103,
        name: '福建茶行',
        logo: 'https://via.placeholder.com/50x50?text=福建',
        coverImage: 'https://via.placeholder.com/300x100?text=福建茶行',
        description: '专营武夷岩茶、安溪铁观音等福建名茶。',
        followTime: '2025-01-22 16:18:45',
        popularity: 92
      }
    ])
    
    // 过滤和排序店铺
    const filteredShops = computed(() => {
      let result = [...followedShops.value]
      
      // 搜索过滤
      if (shopSearchKeyword.value) {
        const keyword = shopSearchKeyword.value.toLowerCase()
        result = result.filter(shop => 
          shop.name.toLowerCase().includes(keyword) || 
          shop.description.toLowerCase().includes(keyword)
        )
      }
      
      // 排序
      if (shopSortOption.value === 'recent') {
        result.sort((a, b) => new Date(b.followTime) - new Date(a.followTime))
      } else if (shopSortOption.value === 'popular') {
        result.sort((a, b) => b.popularity - a.popularity)
      }
      
      return result
    })
    
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
    const unfollowUser = (userId) => {
      // 实际项目中调用取消关注API
      followedUsers.value = followedUsers.value.filter(user => user.id !== userId)
      ElMessage.success('已取消关注')
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
    const unfollowShop = (shopId) => {
      // 实际项目中调用取消关注API
      followedShops.value = followedShops.value.filter(shop => shop.id !== shopId)
      ElMessage.success('已取消关注该店铺')
    }
    
    // 返回首页
    const goHome = () => {
      router.push('/')
    }
    
    // 添加默认图片常量
    const defaultAvatar = '/mock-images/avatar-default.jpg'
    const defaultShopLogo = '/mock-images/shop-logo-1.jpg'
    
    // 模拟关注数据
    const followList = ref([
      {
        id: 1,
        type: 'user',
        name: '茶友小王',
        avatar: defaultAvatar,
        description: '爱茶之人',
        followTime: '2025-03-15 10:30:00'
      },
      {
        id: 2,
        type: 'shop',
        name: '商南茶叶专营店',
        logo: defaultShopLogo,
        description: '专注商南好茶20年',
        followTime: '2025-03-14 15:20:00'
      }
    ])
    
    return {
      activeTab,
      searchKeyword,
      sortOption,
      followedUsers,
      filteredUsers,
      shopSearchKeyword,
      shopSortOption,
      followedShops,
      filteredShops,
      formatDate,
      goToUserProfile,
      sendMessage,
      unfollowUser,
      goToShopDetail,
      contactShop,
      unfollowShop,
      goHome,
      defaultAvatar,
      defaultShopLogo,
      followList
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
  
  // 分类标签页
  .follows-tabs {
    margin-bottom: 20px;
  }
  
  // 列表头部样式
  .list-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    .list-title {
      font-size: 16px;
      font-weight: 500;
      color: var(--el-text-color-primary);
    }
    
    .list-actions {
      display: flex;
      align-items: center;
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
    .list-header {
      flex-direction: column;
      align-items: flex-start;
      
      .list-title {
        margin-bottom: 10px;
      }
      
      .list-actions {
        width: 100%;
        
        .el-input,
        .el-select {
          width: 100%;
          margin-bottom: 10px;
          margin-left: 0 !important;
        }
      }
    }
    
    .users-list {
      .user-item {
        flex-direction: column;
        align-items: flex-start;
        
        .user-avatar {
          margin-bottom: 10px;
        }
        
        .user-info {
          margin-bottom: 15px;
          width: 100%;
        }
        
        .user-actions {
          width: 100%;
          justify-content: flex-end;
        }
      }
    }
    
    .shops-grid {
      grid-template-columns: 1fr;
    }
  }
}
</style> 