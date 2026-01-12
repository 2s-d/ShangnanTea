<template>
  <div class="user-home-page">
    <div class="container">
      <el-card class="home-card" v-loading="loading">
        <!-- 上半部分：用户信息展示区 -->
        <div class="user-profile-section">
          <div class="user-basic-info">
            <div class="user-avatar">
              <SafeImage 
                :src="userInfo.avatar" 
                type="avatar" 
                style="width:120px;height:120px;border-radius:50%;object-fit:cover;" />
            </div>
            <div class="user-details">
              <h2 class="user-name">
                {{ userInfo.nickname || userInfo.username || '用户' }}
                <span class="user-gender">
                  <el-icon color="#409EFF" v-if="userInfo.gender === 1"><Male /></el-icon>
                  <el-icon color="#FF4949" v-else-if="userInfo.gender === 2"><Female /></el-icon>
                </span>
                <el-tag v-if="userInfo.role === 'merchant'" type="warning" size="small">商家</el-tag>
              </h2>
              <div class="user-id">ID: {{ userInfo.username }}</div>
              <div class="user-bio">{{ userInfo.bio || '这个用户很懒，什么都没有留下...' }}</div>
              <div class="user-register-time" v-if="userInfo.registerTime">
                注册时间：{{ formatDate(userInfo.registerTime) }}
              </div>
              <div class="user-shop-link" v-if="userInfo.role === 'merchant' && userInfo.shopId">
                <el-link type="primary" @click="goToShop">
                  <el-icon><Shop /></el-icon>
                  {{ userInfo.shopName || '我的店铺' }}
                </el-link>
              </div>
            </div>
          </div>
          <div class="user-actions">
            <!-- 查看自己主页时显示编辑按�?-->
            <el-button v-if="isOwnProfile" type="primary" @click="handleEditProfile">
              <el-icon><Edit /></el-icon>
              编辑资料
            </el-button>
            <!-- 查看他人主页时显示关注按�?-->
            <el-button v-else type="primary" @click="handleFollow">
              <el-icon><Plus /></el-icon>
              {{ isFollowing ? '取消关注' : '关注' }}
            </el-button>
          </div>
        </div>
        
        <!-- 用户统计数据 -->
        <div class="user-statistics">
          <div class="stat-item">
            <div class="stat-number">{{ userStatistics.postCount }}</div>
            <div class="stat-label">发帖</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">{{ userStatistics.likeCount }}</div>
            <div class="stat-label">获赞</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">{{ userStatistics.favoriteCount }}</div>
            <div class="stat-label">收藏</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">{{ userStatistics.followingCount }}</div>
            <div class="stat-label">关注</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">{{ userStatistics.followerCount }}</div>
            <div class="stat-label">粉丝</div>
          </div>
        </div>
        
        <!-- 中间：导航分割线 -->
        <div class="navigation-divider">
          <el-menu
            :default-active="activeMenu"
            class="home-menu-list"
            mode="horizontal"
            @select="handleMenuSelect">
            <el-menu-item index="dynamic">
              <el-icon><Clock /></el-icon>
              <span>动�?/span>
            </el-menu-item>
            <el-menu-item index="published">
              <el-icon><Document /></el-icon>
              <span>发布</span>
            </el-menu-item>
            <el-menu-item index="follows">
              <el-icon><UserFilled /></el-icon>
              <span>关注</span>
            </el-menu-item>
            <el-menu-item index="favorites">
              <el-icon><Star /></el-icon>
              <span>收藏</span>
            </el-menu-item>
          </el-menu>
        </div>
          
        <!-- 下半部分：内容区�?-->
        <div class="home-content">
          <!-- 用户动�?-->
          <div v-if="activeMenu === 'dynamic'" class="dynamic-content">
            <div class="dynamic-section">
              <h3>最近发�?/h3>
              <div v-if="userDynamic.recentPosts && userDynamic.recentPosts.length > 0" class="posts-list">
                <div v-for="post in userDynamic.recentPosts" :key="post.id" class="post-item">
                  <div class="post-header">
                    <h4 class="post-title">{{ post.title }}</h4>
                    <span class="post-time">{{ formatDate(post.createTime) }}</span>
                  </div>
                  <p class="post-content">{{ post.content }}</p>
                  <div class="post-stats">
                    <span><el-icon><Star /></el-icon> {{ post.likeCount }}</span>
                    <span><el-icon><ChatDotRound /></el-icon> {{ post.commentCount }}</span>
                  </div>
                </div>
              </div>
              <el-empty v-else description="暂无发布内容" :image-size="100"></el-empty>
            </div>
            
            <div class="dynamic-section">
              <h3>最近评�?/h3>
              <div v-if="userDynamic.recentComments && userDynamic.recentComments.length > 0" class="comments-list">
                <div v-for="comment in userDynamic.recentComments" :key="comment.id" class="comment-item">
                  <div class="comment-header">
                    <span class="comment-post">回复：{{ comment.postTitle }}</span>
                    <span class="comment-time">{{ formatDate(comment.createTime) }}</span>
                  </div>
                  <p class="comment-content">{{ comment.content }}</p>
                </div>
              </div>
              <el-empty v-else description="暂无评论记录" :image-size="100"></el-empty>
            </div>
          </div>
          
          <!-- 其他标签页内�?-->
          <keep-alive v-else>
            <component :is="currentComponent" />
          </keep-alive>
          
          <!-- 开发中的功能提�?-->
          <template v-if="!hasComponent && activeMenu !== 'dynamic'">
            <div class="developing-feature">
              <el-empty 
                description="该功能正在开发中，敬请期�?.." 
                :image-size="200">
                <template #image>
                  <SafeImage :src="defaultImage" type="banner" class="dev-logo" />
                </template>
              </el-empty>
            </div>
          </template>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>
import { ref, markRaw, watch, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useStore } from 'vuex'

import ProfilePage from '@/views/user/profile/ProfilePage.vue'
import FollowsPage from '@/views/message/follows/FollowsPage.vue'
import FavoritesPage from '@/views/message/favorites/FavoritesPage.vue'
import PublishedContentPage from '@/views/message/content/PublishedContentPage.vue'
import { 
  User, UserFilled, Star, Document, Male, Female, Edit, Plus, Shop, Clock, ChatDotRound 
} from '@element-plus/icons-vue'
import SafeImage from '@/components/common/form/SafeImage.vue'
import { showByCode } from '@/utils/apiMessages'
import { commonPromptMessages } from '@/utils/promptMessages'

export default {
  name: 'UserHomePage',
  components: {
    ProfilePage,
    FollowsPage,
    FavoritesPage,
    PublishedContentPage,
    User, UserFilled, Star, Document, Male, Female, Edit, Plus, Shop, Clock, ChatDotRound,
    SafeImage
  },
  setup() {
    const router = useRouter()
    const route = useRoute()
    const store = useStore()
    
    // 从路由参数获取用户ID，如果没有则使用当前登录用户ID
    const userId = computed(() => route.params.userId || 'current')
    
    // 从Vuex获取用户信息
    const userInfo = computed(() => store.state.message.userProfile || {
      username: '',
      nickname: '',
      avatar: '',
      gender: 0,
      bio: '',
      role: 'user',
      registerTime: null,
      shopId: null,
      shopName: null
    })
    
    // 从Vuex获取用户动�?    const userDynamic = computed(() => store.state.message.userDynamic || {
      recentPosts: [],
      recentComments: []
    })
    
    // 从Vuex获取用户统计数据
    const userStatistics = computed(() => store.state.message.userStatistics || {
      postCount: 0,
      likeCount: 0,
      favoriteCount: 0,
      followingCount: 0,
      followerCount: 0,
      commentCount: 0
    })
    
    // 加载状�?    const loading = computed(() => store.state.message.loading)
    
    // 判断是否是查看自己的主页
    const isOwnProfile = computed(() => {
      return userId.value === 'current' || !route.params.userId
    })
    
    // 关注状态（这里简化处理，实际应该从用户模块获取）
    const isFollowing = ref(false)
    
    // 菜单项和对应组件映射
    const menuOptions = {
      dynamic: '动�?,
      published: '我的发布',
      follows: '我的关注',
      favorites: '我的收藏'
    }
    
    // 组件映射
    const componentMap = {
      published: markRaw(PublishedContentPage),
      follows: markRaw(FollowsPage),
      favorites: markRaw(FavoritesPage),
    }
    
    // 活动菜单
    const activeMenu = ref('dynamic')
    // 当前显示的组�?    const currentComponent = ref(componentMap.published)
    
    // 判断当前菜单是否有对应组�?    const hasComponent = computed(() => {
      return componentMap[activeMenu.value] !== undefined
    })
    
    // 从路由参数初始化activeMenu
    onMounted(async () => {
      if (route.params.tab && (componentMap[route.params.tab] || route.params.tab === 'dynamic')) {
        activeMenu.value = route.params.tab
        if (componentMap[route.params.tab]) {
          currentComponent.value = componentMap[route.params.tab]
        }
      }
      
      // 加载用户信息
      await loadUserData()
    })
    
    // 监听路由参数变化
    watch(() => route.params.tab, (newTab) => {
      if (newTab && (componentMap[newTab] || newTab === 'dynamic')) {
        activeMenu.value = newTab
        if (componentMap[newTab]) {
          currentComponent.value = componentMap[newTab]
        }
      }
    })
    
    // 监听用户ID变化
    watch(() => userId.value, async () => {
      await loadUserData()
    })
    
    // 菜单选择处理
    const handleMenuSelect = (key) => {
      activeMenu.value = key
      
      // 如果存在对应组件就设置，否则显示动态或开发中
      if (componentMap[key]) {
        currentComponent.value = componentMap[key]
      } else {
        currentComponent.value = null
      }
      
      // 更新路由参数（不刷新页面�?      router.push({
        path: `/profile/${key}`,
        replace: true
      })
    }
    
    // 处理编辑资料
    const handleEditProfile = () => {
      router.push('/user/profile')
    }
    
    // 处理关注/取消关注
    const handleFollow = async () => {
      try {
        if (isFollowing.value) {
          // 取消关注 - 这里应该调用用户模块的取消关注方�?          // await store.dispatch('user/removeFollow', userId.value)
          isFollowing.value = false
          commonPromptMessages.showProcessing()
        } else {
          // 添加关注 - 这里应该调用用户模块的添加关注方�?          // await store.dispatch('user/addFollow', { targetId: userId.value, targetType: 'user' })
          isFollowing.value = true
          commonPromptMessages.showProcessing()
        }
      } catch (error) {
        console.error('����ʧ��')
      }
    }
    
    // 跳转到店�?    const goToShop = () => {
      if (userInfo.value.shopId) {
        router.push(`/shop/${userInfo.value.shopId}`)
      }
    }
    
    // 加载用户数据
    const loadUserData = async () => {
      try {
        const targetUserId = userId.value === 'current' ? '123' : userId.value // 测试用户ID
        
        // 并行加载用户信息、动态和统计数据
        await Promise.all([
          store.dispatch('message/fetchUserProfile', targetUserId),
          store.dispatch('message/fetchUserDynamic', targetUserId),
          store.dispatch('message/fetchUserStatistics', targetUserId)
        ])
      } catch (error) {
        console.error('加载用户数据失败�?, error)
        console.error('����ʧ��')
      }
    }
    
    // 格式化日�?    const formatDate = (date) => {
      if (!date) return ''
      const d = new Date(date)
      return `${d.getFullYear()}-${(d.getMonth() + 1).toString().padStart(2, '0')}-${d.getDate().toString().padStart(2, '0')}`
    }
    
    // 默认图片
    const defaultImage = ''
    
    return {
      activeMenu,
      currentComponent,
      menuOptions,
      handleMenuSelect,
      hasComponent,
      userInfo,
      userDynamic,
      userStatistics,
      loading,
      isOwnProfile,
      isFollowing,
      defaultImage,
      handleEditProfile,
      handleFollow,
      goToShop,
      formatDate
    }
  }
}
</script>

<style lang="scss" scoped>
.user-home-page {
  padding: 40px 0;
  
  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 15px;
  }
  
  .home-card {
    border-radius: 8px;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
    
    // 用户信息区域样式
    .user-profile-section {
      display: flex;
      justify-content: space-between;
      padding: 30px;
      border-bottom: 1px solid #f0f0f0;
      
      .user-basic-info {
        display: flex;
        align-items: center;
        
        .user-avatar {
          width: 120px;
          height: 120px;
          border-radius: 50%;
          overflow: hidden;
          margin-right: 30px;
          border: 4px solid #f5f7fa;
          
          img {
            width: 100%;
            height: 100%;
            object-fit: cover;
          }
        }
        
        .user-details {
          .user-name {
            font-size: 24px;
            margin: 0 0 10px;
            display: flex;
            align-items: center;
            gap: 10px;
            
            .user-gender {
              margin-left: 5px;
            }
          }
          
          .user-id {
            color: #909399;
            font-size: 14px;
            margin-bottom: 10px;
          }
          
          .user-bio {
            max-width: 600px;
            color: #606266;
            font-size: 14px;
            line-height: 1.6;
            margin-bottom: 10px;
          }
          
          .user-register-time {
            color: #909399;
            font-size: 12px;
            margin-bottom: 10px;
          }
          
          .user-shop-link {
            margin-top: 10px;
          }
        }
      }
      
      .user-actions {
        align-self: flex-start;
      }
    }
    
    // 用户统计数据样式
    .user-statistics {
      display: flex;
      justify-content: center;
      padding: 20px 30px;
      border-bottom: 1px solid #f0f0f0;
      gap: 40px;
      
      .stat-item {
        text-align: center;
        
        .stat-number {
          font-size: 24px;
          font-weight: bold;
          color: var(--el-color-primary);
          margin-bottom: 5px;
        }
        
        .stat-label {
          font-size: 14px;
          color: #909399;
        }
      }
    }
    
    // 导航分割线样�?    .navigation-divider {
      border-bottom: 1px solid #f0f0f0;
      margin: 0 -30px;
      
      .home-menu-list {
        display: flex;
        justify-content: center;
        border-bottom: none;
        padding: 0 30px;
        
        .el-menu-item {
          height: 60px;
          line-height: 60px;
          font-size: 16px;
          
          .el-icon {
            margin-right: 5px;
          }
        }
      }
    }
    
    // 内容区域样式
    .home-content {
      min-height: 300px;
      padding: 30px;
      
      .dynamic-content {
        .dynamic-section {
          margin-bottom: 30px;
          
          h3 {
            margin-bottom: 20px;
            color: var(--el-text-color-primary);
            border-bottom: 2px solid var(--el-color-primary);
            padding-bottom: 10px;
          }
          
          .posts-list {
            .post-item {
              padding: 15px;
              border: 1px solid #f0f0f0;
              border-radius: 8px;
              margin-bottom: 15px;
              
              .post-header {
                display: flex;
                justify-content: space-between;
                align-items: center;
                margin-bottom: 10px;
                
                .post-title {
                  margin: 0;
                  font-size: 16px;
                  color: var(--el-text-color-primary);
                }
                
                .post-time {
                  font-size: 12px;
                  color: #909399;
                }
              }
              
              .post-content {
                color: #606266;
                line-height: 1.6;
                margin-bottom: 10px;
              }
              
              .post-stats {
                display: flex;
                gap: 20px;
                font-size: 14px;
                color: #909399;
                
                span {
                  display: flex;
                  align-items: center;
                  gap: 5px;
                }
              }
            }
          }
          
          .comments-list {
            .comment-item {
              padding: 15px;
              border: 1px solid #f0f0f0;
              border-radius: 8px;
              margin-bottom: 15px;
              
              .comment-header {
                display: flex;
                justify-content: space-between;
                align-items: center;
                margin-bottom: 10px;
                
                .comment-post {
                  font-size: 14px;
                  color: var(--el-color-primary);
                }
                
                .comment-time {
                  font-size: 12px;
                  color: #909399;
                }
              }
              
              .comment-content {
                color: #606266;
                line-height: 1.6;
              }
            }
          }
        }
      }
    }
  }
  
  .developing-feature {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
    padding: 40px;
    
    .dev-logo {
      width: 150px;
      margin-bottom: 20px;
    }
  }
}

// 响应式样�?@media (max-width: 768px) {
  .user-home-page {
    .home-card {
      .user-profile-section {
        flex-direction: column;
        padding: 20px;
        
        .user-basic-info {
          flex-direction: column;
          text-align: center;
          margin-bottom: 20px;
          
          .user-avatar {
            margin-right: 0;
            margin-bottom: 20px;
          }
          
          .user-details {
            .user-name {
              justify-content: center;
            }
            
            .user-bio {
              max-width: 100%;
            }
          }
        }
        
        .user-actions {
          align-self: center;
          width: 100%;
          
          .el-button {
            width: 100%;
          }
        }
      }
      
      .user-statistics {
        gap: 20px;
        padding: 15px 20px;
        
        .stat-item {
          .stat-number {
            font-size: 20px;
          }
        }
      }
      
      .navigation-divider {
        .home-menu-list {
          .el-menu-item {
            height: 50px;
            line-height: 50px;
            padding: 0 10px;
            font-size: 14px;
          }
        }
      }
      
      .home-content {
        padding: 20px;
      }
    }
  }
}
</style>