<template>
  <div class="settings-page">
    <div class="container">
      <h1 class="page-title">个人设置</h1>
      
      <el-card class="settings-card">
        <div class="settings-layout">
          <!-- 左侧导航菜单 -->
          <div class="settings-menu">
            <el-menu
              :default-active="activeMenu"
              class="settings-menu-list"
              @select="handleMenuSelect">
              <el-menu-item index="profile">
                <el-icon><User /></el-icon>
                <span>个性修改</span>
              </el-menu-item>
              <el-menu-item index="password">
                <el-icon><Lock /></el-icon>
                <span>密码修改</span>
              </el-menu-item>
              <el-menu-item index="merchant">
                <el-icon><Shop /></el-icon>
                <span>商家认证</span>
              </el-menu-item>
            </el-menu>
          </div>
          
          <!-- 右侧内容区域 -->
          <div class="settings-content">
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
                    <SafeImage src="/mock-images/default.jpg" type="banner" alt="功能开发中" class="dev-logo"/>
                  </template>
                </el-empty>
              </div>
            </template>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>
import { ref, markRaw, watch, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ProfileEditPage from './ProfileEditPage.vue'
import ChangePasswordPage from '@/views/user/change-password/ChangePasswordPage.vue'
import MerchantApplication from '../auth/MerchantApplication.vue'
import { User, Lock, Shop } from '@element-plus/icons-vue'
import SafeImage from '@/components/common/form/SafeImage.vue'

export default {
  name: 'SettingsPage',
  components: {
    ProfileEditPage,
    ChangePasswordPage,
    MerchantApplication,
    User, Lock, Shop,
    SafeImage
  },
  setup() {
    const router = useRouter()
    const route = useRoute()
    
    // 菜单项和对应组件映射
    const menuOptions = {
      profile: '个人编辑',
      password: '密码修改',
      merchant: '商家认证'
    }
    
    // 组件映射
    const componentMap = {
      profile: markRaw(ProfileEditPage),
      password: markRaw(ChangePasswordPage),
      merchant: markRaw(MerchantApplication)
    }
    
    // 活动菜单
    const activeMenu = ref('profile')
    // 当前显示的组件
    const currentComponent = ref(componentMap.profile)
    
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
    })
    
    // 监听路由参数变化
    watch(() => route.params.tab, newTab => {
      if (newTab && componentMap[newTab]) {
        activeMenu.value = newTab
        currentComponent.value = componentMap[newTab]
      }
    })
    
    // 菜单选择处理
    const handleMenuSelect = key => {
      activeMenu.value = key
      
      // 如果存在对应组件就设置，否则显示开发中
      if (componentMap[key]) {
        currentComponent.value = componentMap[key]
      } else {
        currentComponent.value = null
      }
      
      // 更新路由参数（不刷新页面）
      router.push({
        path: `/user/settings/${key}`,
        replace: true
      })
    }
    
    return {
      activeMenu,
      currentComponent,
      menuOptions,
      handleMenuSelect,
      hasComponent
    }
  }
}
</script>

<style lang="scss" scoped>
.settings-page {
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
  
  .settings-card {
    border-radius: 8px;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
    
    .settings-layout {
      display: flex;
      min-height: 500px;
      
      .settings-menu {
        width: 220px;
        border-right: 1px solid #eee;
        
        .settings-menu-list {
          border-right: none;
        }
      }
      
      .settings-content {
        flex: 1;
        padding: 20px;
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

@media (max-width: 768px) {
  .settings-page {
    .settings-card {
      .settings-layout {
        flex-direction: column;
        
        .settings-menu {
          width: 100%;
          border-right: none;
          border-bottom: 1px solid #eee;
          
          .settings-menu-list {
            display: flex;
            justify-content: space-around;
            
            .el-menu-item {
              flex: 1;
              text-align: center;
              padding: 0 10px;
              
              .el-icon {
                margin-right: 5px;
              }
            }
          }
        }
      }
    }
  }
}
</style> 