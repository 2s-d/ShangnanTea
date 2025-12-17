<template>
  <div class="user-home-page">
    <div class="container">
      <el-card class="home-card">
        <!-- 上半部分：用户信息展示区 -->
        <div class="user-profile-section">
          <div class="user-basic-info">
            <div class="user-avatar">
              <SafeImage :src="userInfo.avatar" type="avatar" style="width:120px;height:120px;border-radius:50%;object-fit:cover;" />
            </div>
            <div class="user-details">
              <h2 class="user-name">
                {{ userInfo.nickname || '用户' }}
                <span class="user-gender">
                  <el-icon color="#409EFF" v-if="userInfo.gender === 1"><Male /></el-icon>
                  <el-icon color="#FF4949" v-else-if="userInfo.gender === 2"><Female /></el-icon>
                </span>
              </h2>
              <div class="user-id">ID: {{ userInfo.username }}</div>
              <div class="user-bio">{{ userInfo.bio || '这个用户很懒，什么都没有留下...' }}</div>
            </div>
          </div>
          <div class="edit-profile-btn">
            <el-button type="primary" @click="handleEditProfile">
              <el-icon><Edit /></el-icon>
              编辑资料
            </el-button>
          </div>
        </div>
        
        <!-- 中间：导航分割线 -->
        <div class="navigation-divider">
            <el-menu
              :default-active="activeMenu"
              class="home-menu-list"
            mode="horizontal"
              @select="handleMenuSelect">
            <el-menu-item index="published">
              <el-icon><Document /></el-icon>
              <span>我的发布</span>
              </el-menu-item>
              <el-menu-item index="follows">
                <el-icon><UserFilled /></el-icon>
                <span>我的关注</span>
              </el-menu-item>
              <el-menu-item index="favorites">
                <el-icon><Star /></el-icon>
                <span>我的收藏</span>
              </el-menu-item>
            </el-menu>
          </div>
          
        <!-- 下半部分：内容区域 -->
          <div class="home-content">
            <keep-alive>
              <component :is="currentComponent" />
            </keep-alive>
            
            <!-- 开发中的功能提示 -->
            <template v-if="!hasComponent">
              <div class="developing-feature">
                <el-empty 
                  description="该功能正在开发中，敬请期待..." 
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
import { ElMessage } from 'element-plus'
import ProfilePage from '@/views/user/profile/ProfilePage.vue'
import FollowsPage from '@/views/message/follows/FollowsPage.vue'
import FavoritesPage from '@/views/message/favorites/FavoritesPage.vue'
import PublishedContentPage from '@/views/message/content/PublishedContentPage.vue'
import { User, UserFilled, Star, Document, Male, Female, Edit } from '@element-plus/icons-vue'
import SafeImage from '@/components/common/form/SafeImage.vue'

export default {
  name: 'UserHomePage',
  components: {
    ProfilePage,
    FollowsPage,
    FavoritesPage,
    PublishedContentPage,
    User, UserFilled, Star, Document, Male, Female, Edit,
    SafeImage
  },
  setup() {
    const router = useRouter()
    const route = useRoute()
    const avatarImage = '/mock-images/avatar-default.jpg'
    
    // 用户信息
    const userInfo = ref({
      username: 'testuser',
      nickname: '测试用户',
      avatar: '',
      gender: 1, // 1-男，2-女
      bio: '这是一个测试用户的个人简介，用于展示个人主页的布局效果。'
    })
    
    // 菜单项和对应组件映射
    const menuOptions = {
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
    const activeMenu = ref('published')
    // 当前显示的组件
    const currentComponent = ref(componentMap.published)
    
    // 判断当前菜单是否有对应组件
    const hasComponent = computed(() => {
      return componentMap[activeMenu.value] !== undefined
    })
    
    // 从路由参数初始化activeMenu
    onMounted(() => {
      if (route.params.tab && componentMap[route.params.tab]) {
        activeMenu.value = route.params.tab
        currentComponent.value = componentMap[route.params.tab]
      }
      
      // 在这里可以添加获取用户信息的逻辑
      // loadUserInfo()
    })
    
    // 监听路由参数变化
    watch(() => route.params.tab, (newTab) => {
      if (newTab && componentMap[newTab]) {
        activeMenu.value = newTab
        currentComponent.value = componentMap[newTab]
      }
    })
    
    // 菜单选择处理
    const handleMenuSelect = (key) => {
      activeMenu.value = key
      
      // 如果存在对应组件就设置，否则显示开发中
      if (componentMap[key]) {
        currentComponent.value = componentMap[key]
      } else {
        currentComponent.value = null
      }
      
      // 更新路由参数（不刷新页面）
      router.push({
        path: `/profile/${key}`,
        replace: true
      })
    }
    
    // 处理编辑资料
    const handleEditProfile = () => {
      router.push('/user/profile')
    }
    
    // 加载用户信息（实际项目中会从API获取）
    const loadUserInfo = async () => {
      /* UI-DEV-START */
      // 开发时使用模拟数据，实际项目中会从API获取
      /* UI-DEV-END */
      
      /* 
      // 真实代码(开发UI时注释)
      try {
        const result = await store.dispatch('user/getUserProfile')
        userInfo.value = result
      } catch (error) {
        ElMessage.error('获取用户信息失败')
      }
      */
    }
    
    // 默认图片
    const defaultAvatar = '/mock-images/avatar-default.jpg'
    const defaultImage = '/mock-images/default.jpg'
    
    return {
      activeMenu,
      currentComponent,
      menuOptions,
      handleMenuSelect,
      hasComponent,
      userInfo,
      defaultAvatar,
      defaultImage,
      handleEditProfile
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
  
  .page-title {
    font-size: 2rem;
    color: var(--el-text-color-primary);
    margin-bottom: 30px;
    text-align: center;
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
            
            .user-gender {
              margin-left: 10px;
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
          }
        }
      }
      
      .edit-profile-btn {
        align-self: flex-start;
      }
    }
    
    // 导航分割线样式
    .navigation-divider {
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

// 响应式样式
@media (max-width: 768px) {
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
        
        .edit-profile-btn {
          align-self: center;
          width: 100%;
          
          .el-button {
            width: 100%;
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