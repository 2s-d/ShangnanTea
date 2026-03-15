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

<script setup>
import { computed, onMounted, onBeforeUnmount, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import Footer from '@/components/common/layout/Footer.vue'
import NavBar from '@/components/common/layout/NavBar.vue'
import { checkAndMigrateData } from '@/utils/versionManager'
import ErrorMonitor from '@/components/dev/ErrorMonitor.vue'
import { clearAllMessageStates } from '@/utils/messageManager'
import websocketManager from '@/utils/websocket'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const isDev = ref(process.env.NODE_ENV === 'development')
const userStore = useUserStore()

// 活动检测相关
let inactivityTimer = null
const INACTIVITY_THRESHOLD = 5 * 60 * 1000 // 5分钟无活动

const resetInactivityTimer = () => {
  if (inactivityTimer) {
    clearTimeout(inactivityTimer)
  }
  // 只有登录状态下才需要追踪
  if (userStore.isLoggedIn) {
    inactivityTimer = setTimeout(() => {
      // 超过5分钟无活动，视为离线：停止心跳（但不断开WS，方便快速恢复）
      console.log('[OnlineStatus] 超过5分钟无活动，暂停WebSocket心跳')
      websocketManager.stopHeartbeat && websocketManager.stopHeartbeat()
    }, INACTIVITY_THRESHOLD)
  }
}

const handleUserActivity = () => {
  // 用户有任何操作，恢复心跳并重置计时
  if (userStore.isLoggedIn) {
    if (websocketManager.isConnected()) {
      websocketManager.startHeartbeat && websocketManager.startHeartbeat()
    } else {
      websocketManager.connect()
    }
  }
  resetInactivityTimer()
}

const handleVisibilityChange = () => {
  if (document.visibilityState === 'visible') {
    // 页面重新可见，恢复心跳
    console.log('[OnlineStatus] 页面可见，恢复心跳')
    if (userStore.isLoggedIn) {
      if (websocketManager.isConnected()) {
        websocketManager.startHeartbeat && websocketManager.startHeartbeat()
      } else {
        websocketManager.connect()
      }
    }
    resetInactivityTimer()
  } else {
    // 页面隐藏，暂停心跳，等TTL自动过期标记离线
    console.log('[OnlineStatus] 页面隐藏，暂停心跳')
    websocketManager.stopHeartbeat && websocketManager.stopHeartbeat()
  }
}

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

  // 登录状态下，全局建立WebSocket连接，用于在线状态和消息推送
  if (userStore.isLoggedIn) {
    websocketManager.connect()
  }

  // 监听登录状态变化：登录→连接WS，登出→断开WS
  watch(() => userStore.isLoggedIn, (loggedIn) => {
    if (loggedIn) {
      websocketManager.connect()
      resetInactivityTimer()
    } else {
      console.log('[OnlineStatus] 用户已登出，断开WebSocket连接')
      websocketManager.disconnect()
      if (inactivityTimer) {
        clearTimeout(inactivityTimer)
        inactivityTimer = null
      }
    }
  }, { immediate: false })

  // 全局用户活动监听
  window.addEventListener('mousemove', handleUserActivity)
  window.addEventListener('keydown', handleUserActivity)
  window.addEventListener('scroll', handleUserActivity, true)
  document.addEventListener('visibilitychange', handleVisibilityChange)

  // 初始化一次活动计时
  resetInactivityTimer()
})

onBeforeUnmount(() => {
  window.removeEventListener('mousemove', handleUserActivity)
  window.removeEventListener('keydown', handleUserActivity)
  window.removeEventListener('scroll', handleUserActivity, true)
  document.removeEventListener('visibilitychange', handleVisibilityChange)
  if (inactivityTimer) {
    clearTimeout(inactivityTimer)
    inactivityTimer = null
  }
})
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