<template>
  <div class="message-page">
    <div class="container">
      <el-card class="message-card">
        <!-- 顶部导航菜单 -->
        <div class="message-header">
          <el-menu
            :default-active="activeMenu"
            class="message-menu-list"
            mode="horizontal"
            @select="handleMenuSelect">
            <el-menu-item index="notifications">
              <el-icon><Bell /></el-icon>
              <span>系统通知</span>
            </el-menu-item>
            <el-menu-item index="chat">
              <el-icon><ChatLineRound /></el-icon>
              <span>私信聊天</span>
            </el-menu-item>
          </el-menu>
        </div>
        
        <!-- 分割线 -->
        <div class="divider"></div>
        
        <!-- 内容区域 -->
        <div class="message-content">
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
      </el-card>
    </div>
  </div>
</template>

<script>
import { ref, markRaw, watch, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import SystemNotificationsPage from './SystemNotificationsPage.vue'
import ChatPage from '../chat/ChatPage.vue'
import { Bell, ChatLineRound } from '@element-plus/icons-vue'
import SafeImage from '@/components/common/form/SafeImage.vue'

export default {
  name: 'NotificationsPage',
  components: {
    SystemNotificationsPage,
    ChatPage,
    Bell, ChatLineRound,
    SafeImage
  },
  setup() {
    const router = useRouter()
    const route = useRoute()
    
    // 菜单项和对应组件映射
    const menuOptions = {
      notifications: '系统通知',
      chat: '私信聊天'
    }
    
    // 组件映射
    const componentMap = {
      notifications: markRaw(SystemNotificationsPage),
      chat: markRaw(ChatPage)
    }
    
    // 活动菜单
    const activeMenu = ref('notifications')
    // 当前显示的组件
    const currentComponent = ref(componentMap.notifications)
    
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
        path: `/message/center/${key}`,
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
.message-page {
  padding: 40px 0;
  
  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 15px;
  }
  
  .message-card {
    border-radius: 8px;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
    
    .message-header {
      padding: 20px 30px 0;
      
      .message-menu-list {
        justify-content: center;
        border-bottom: none;
        
        .el-menu-item {
          font-size: 16px;
          
          .el-icon {
            margin-right: 5px;
          }
        }
      }
    }
    
    .divider {
      height: 1px;
      background-color: #f0f0f0;
      margin: 0;
    }
    
    .message-content {
      min-height: 500px;
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

@media (max-width: 768px) {
  .message-page {
    .message-card {
      .message-header {
        padding: 15px 15px 0;
        
        .message-menu-list {
          .el-menu-item {
            padding: 0 15px;
            font-size: 14px;
          }
        }
      }
      
      .message-content {
        padding: 20px 15px;
      }
    }
  }
}
</style> 