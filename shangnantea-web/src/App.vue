<template>
  <div id="app">
    <!-- 根据路由判断是否显示导航栏和页脚 -->
    <template v-if="showHeaderAndFooter">
      <nav-bar />
      <main class="main-content">
        <router-view />
      </main>
      <Footer />
    </template>
    <!-- 登录/注册页面只显示内容，不显示导航栏和页脚 -->
    <template v-else>
      <div class="auth-container">
        <router-view />
      </div>
    </template>
    
    <!-- 开发环境下的错误监控面板 -->
    <error-monitor v-if="isDev" />
  </div>
</template>

<script>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import Footer from '@/components/common/layout/Footer.vue'
import NavBar from '@/components/common/layout/NavBar.vue'
import { checkAndMigrateData } from '@/utils/versionManager'
import ErrorMonitor from '@/components/dev/ErrorMonitor.vue'
import { clearAllMessageStates } from '@/utils/messageManager'

export default {
  name: 'App',
  components: {
    NavBar,
    Footer,
    ErrorMonitor
  },
  setup() {
    const route = useRoute()
    const isDev = ref(process.env.NODE_ENV === 'development')
    
    // 判断当前页面是否需要显示导航栏和页脚
    const showHeaderAndFooter = computed(() => {
      // 登录、注册页面不显示导航栏和页脚
      const authPages = ['/login', '/register']
      return !authPages.includes(route.path)
    })
    
    // 应用初始化时执行版本检查和数据迁移（静默模式）
    onMounted(() => {
      // 静默模式执行数据版本检查和迁移，避免产生错误提示
      checkAndMigrateData(true)
      
      // 清除所有消息状态，防止旧消息被显示
      clearAllMessageStates()
    })
    
    return {
      showHeaderAndFooter,
      isDev
    }
  }
}
</script>

<style lang="scss">
/* 移除全局样式导入，因为已经在main.js中导入 */
/* @import './assets/styles/global.scss'; */

#app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  padding: 20px;
}

.auth-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
</style> 