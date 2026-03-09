<template>
  <div class="navbar" v-if="isLoggedIn">
    <div class="navbar-container">
      <!-- 左侧Logo -->
      <div class="nav-logo">
        <router-link to="/tea-culture">
          <img src="/images/tea-logo.png" alt="商南茶文化" class="logo-img">
          <span class="logo-text">商南茶文化</span>
        </router-link>
      </div>

      <el-menu
        :default-active="computedActiveIndex"
        mode="horizontal"
        router
        background-color="#fff"
        text-color="#333"
        active-text-color="#409EFF"
        class="main-menu">
        
        <template v-if="userRole === 2">
          <el-menu-item index="/tea-culture">茶文化</el-menu-item>
          <el-menu-item index="/tea/mall">茶叶商城</el-menu-item>
          <el-menu-item index="/forum/list">茶友论坛</el-menu-item>
          <el-menu-item index="/order/list">我的订单</el-menu-item>
          <el-menu-item index="/message/center">消息管理</el-menu-item>
          <el-menu-item index="/profile">个人主页</el-menu-item>
        </template>
        
        <template v-else-if="userRole === 3">
          <el-menu-item index="/tea-culture">茶文化</el-menu-item>
          <el-menu-item index="/tea/mall">茶叶商城</el-menu-item>
          <el-menu-item index="/forum/list">茶友论坛</el-menu-item>
          <el-menu-item index="/order/manage">订单管理</el-menu-item>
          <el-menu-item index="/shop/manage">商家店铺</el-menu-item>
          <el-menu-item index="/message/center">消息管理</el-menu-item>
          <el-menu-item index="/profile">个人主页</el-menu-item>
        </template>
        
        <template v-else-if="userRole === 1">
          <el-menu-item index="/tea-culture">茶文化</el-menu-item>
          <el-menu-item index="/tea/manage">茶叶管理</el-menu-item>
          <el-menu-item index="/order/manage">订单管理</el-menu-item>
          <el-menu-item index="/forum/manage">内容管理</el-menu-item>
          <el-menu-item index="/user/manage">用户管理</el-menu-item>
          <el-menu-item index="/message/center">消息管理</el-menu-item>
          <el-menu-item index="/profile">个人主页</el-menu-item>
        </template>
      </el-menu>
      
      <!-- 右侧工具栏 -->
      <div class="nav-right-tools">
<!-- 今日天气：市级名称 | 图标 | 温度 -->
        <div class="weather-display" v-if="hasWeather">
          <span class="weather-city" v-if="cityName">{{ cityName }}</span>
          <span class="weather-sep" v-if="cityName"> | </span>
          <el-tooltip effect="dark" :content="weatherText" placement="bottom">
            <span class="weather-icon-wrapper">
              <span class="weather-icon">{{ weatherIcon }}</span>
            </span>
          </el-tooltip>
          <span class="weather-sep"> | </span>
          <el-tooltip effect="dark" :content="temperatureTooltip" placement="bottom">
            <span class="weather-temp">{{ temperatureText }}</span>
          </el-tooltip>
        </div>

        <!-- 购物车图标 -->
        <router-link to="/order/cart" class="cart-link">
          <el-badge :value="cartCount" :hidden="cartCount === 0" class="cart-badge-wrapper">
            <el-icon :size="24" class="cart-icon">
              <ShoppingCart />
            </el-icon>
          </el-badge>
        </router-link>
        
        <!-- 用户下拉菜单 -->
        <div class="user-dropdown">
          <el-dropdown trigger="click" @command="handleCommand">
            <span class="el-dropdown-link">
              <SafeImage :src="userAvatar" type="avatar" alt="用户头像" style="width:30px;height:30px;border-radius:50%;object-fit:cover;" />
              <span>{{ displayName }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="/user/settings">个人设置</el-dropdown-item>
                <el-dropdown-item command="/user/address">收货地址管理</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
// 调整导入顺序，确保 vue 核心工具先导入
import { computed, ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'

// 导入 Pinia store
import { useUserStore } from '@/stores/user'
import { useOrderStore } from '@/stores/order'

// 然后导入工具类
import { useAuth } from '@/composables/useAuth'
import { formatLocationDisplay } from '@/utils/region'
// 引入SafeImage通用图片组件
import SafeImage from '@/components/common/form/SafeImage.vue'
// 引入Element Plus图标
import { ShoppingCart } from '@element-plus/icons-vue'

const userStore = useUserStore()
const orderStore = useOrderStore()
const router = useRouter()
const route = useRoute()

// 使用useAuth
const { isAdmin, isShop, isUser, isLoggedIn, ROLES } = useAuth()

// 计算激活的导航项（如果是从其他页面跳转到个人主页，保持之前的导航栏高亮）
const computedActiveIndex = computed(() => {
  // 如果当前路由是个人主页，且query中有from参数，使用from路径来决定导航栏高亮
  if (route.path.startsWith('/profile') && route.query.from) {
    const fromPath = route.query.from
    // 检查from路径是否匹配某个导航项
    const navPaths = ['/tea-culture', '/tea/mall', '/forum/list', '/order/list', '/order/manage', '/shop/manage', '/tea/manage', '/order/manage', '/forum/manage', '/user/manage', '/message/center', '/profile']
    if (navPaths.some(path => fromPath.startsWith(path))) {
      // 找到匹配的导航路径
      for (const navPath of navPaths) {
        if (fromPath.startsWith(navPath)) {
          return navPath
        }
      }
    }
  }
  // 否则使用当前路由路径
  return route.path
})

// 当路由变化时更新激活项，并在「已登录」状态下刷新必要的全局数据
onMounted(async () => {
  
  // 未登录时不请求需要鉴权的接口，避免在登录页/退出后刷新的 401 状态干扰逻辑
  if (!isLoggedIn.value) {
    return
  }

  try {
    // 刷新购物车数量
    await orderStore.fetchCartItems()
    // 刷新用户信息，确保拿到完整的昵称等资料（token 里只有基础字段）
      await userStore.getUserInfo()
// 基于当前用户现居地请求今日天气（带前端与后端双重缓存）
    await userStore.fetchTodayWeather()
  } catch (error) {
    // 静默失败，不影响导航栏显示（仅在开发环境打印）
    if (import.meta.env.MODE === 'development') {
      console.error('导航初始化失败:', error)
    }
  }
})

// 用户角色
const userRole = computed(() => {
  return userStore.userInfo?.role || 0
})

// 用户信息（显示昵称）
const displayName = computed(() => userStore.displayName)
const userAvatar = computed(() => {
  return userStore.userInfo?.avatar || ''
})

// 购物车数量
const cartCount = computed(() => orderStore.cartItemCount)

/**
 * ⚠️⚠️⚠️ 严重警告：禁止修改以下天气相关代码！⚠️⚠️⚠️
 * 
 * 此部分代码已经过多次修改和调试，当前实现依赖后端返回的四个字段：
 * - weather: 天气现象
 * - temperature: 当前温度
 * - maxTemperature: 最高温度
 * - minTemperature: 最低温度
 * 
 * 如需修改此部分代码，必须：
 * 1. 先向用户请示并获得批准
 * 2. 说明修改原因和影响范围
 * 3. 确保与后端返回的数据结构保持一致
 * 
 * 此部分代码已被修改超过5次，请勿随意改动！
 */
// 今日天气相关计算
const weather = computed(() => userStore.todayWeather)
const weatherText = computed(() => weather.value?.weather || '')
const temperatureText = computed(() => {
  if (!weather.value || !weather.value.temperature) return ''
  return `${weather.value.temperature}℃`
})

const temperatureTooltip = computed(() => {
  if (!temperatureText.value) return ''
  // 如果有最高/最低温度，则优先展示高低温
  const maxT = weather.value?.maxTemperature
  const minT = weather.value?.minTemperature
  if (maxT || minT) {
    const parts = []
    if (maxT) parts.push(`最高 ${maxT}℃`)
    if (minT) parts.push(`最低 ${minT}℃`)
    return parts.join(' / ')
  }
  // 兜底：只有当前温度
  return `当前温度 ${temperatureText.value}`
})

// 城市名称（市级）
const cityName = computed(() => {
  const locationCode = userStore.userInfo?.currentLocation || ''
  if (!locationCode) return ''

  const full = formatLocationDisplay(locationCode) || ''
  if (!full) return ''

  const parts = full.split('-')
  return parts[parts.length - 1] || parts[0] || ''
})

// 根据中文天气描述映射到 Unicode 天气符号
const mapWeatherToEmoji = weatherStr => {
  if (!weatherStr) return '🌡'
  if (weatherStr.includes('雷')) return '⛈'
  if (weatherStr.includes('雨')) return '🌧'
  if (weatherStr.includes('雪') || weatherStr.includes('冰')) return '❄'
  if (weatherStr.includes('雾') || weatherStr.includes('霾') || weatherStr.includes('沙') || weatherStr.includes('尘')) return '🌫'
  if (weatherStr.includes('多云')) return '⛅'
  if (weatherStr.includes('阴')) return '☁'
  if (weatherStr.includes('晴')) return '☀'
  return '🌡'
}

const weatherIcon = computed(() => mapWeatherToEmoji(weatherText.value))

const hasWeather = computed(() => {
  return !!(weatherText.value && temperatureText.value)
})

// 当用户在个人资料中修改了现居地后，自动刷新天气（强制绕过前端TTL缓存）
watch(
  () => userStore.userInfo?.currentLocation,
  (val, oldVal) => {
    if (!isLoggedIn.value) return
    if (val && val !== oldVal) {
      userStore.fetchTodayWeather(val, { force: true }).catch(err => {
        if (import.meta.env.MODE === 'development') {
          console.error('刷新天气失败:', err)
        }
      })
    }
  }
)

// 退出登录
const handleLogout = async () => {
  try {
    await userStore.logout()
    router.push('/login')
  } catch (error) {
    console.error('退出登录失败:', error)
  }
}

// 处理下拉菜单点击
const handleCommand = command => {
  if (command === 'logout') {
    handleLogout()
  } else {
    router.push(command)
  }
}
</script>

<style scoped>
.navbar {
  background: #ffffff;
  border-bottom: 1px solid #e5e7eb;
  position: sticky;
  top: 0;
  z-index: 1000;
  width: 100%;
  user-select: none;
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
}

.navbar-container {
  max-width: 1920px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  padding: 0 50px;
  height: 72px;
  gap: 60px;
}

/* Logo样式 */
.nav-logo {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.nav-logo a {
  display: flex;
  align-items: center;
  text-decoration: none;
  color: #111827;
}

.logo-img {
  height: 36px;
  width: 36px;
  margin-right: 10px;
  object-fit: cover;
}

.logo-text {
  font-size: 20px;
  font-weight: 600;
  color: #111827;
  letter-spacing: -0.3px;
}

/* 主导航菜单样式 */
.main-menu {
  flex: 1;
  display: flex;
  border-bottom: none !important;
  background: transparent !important;
}

:deep(.el-menu--horizontal) {
  border-bottom: none;
  background: transparent;
}

:deep(.el-menu--horizontal > .el-menu-item) {
  height: 72px;
  line-height: 72px;
  padding: 0 24px;
  font-size: 15px;
  font-weight: 500;
  color: #6b7280;
  border-bottom: none;
  transition: color 0.2s ease;
}

:deep(.el-menu--horizontal > .el-menu-item:hover) {
  color: #111827;
  background: transparent;
}

:deep(.el-menu--horizontal > .el-menu-item.is-active) {
  color: #409EFF !important;
  font-weight: 600;
  background: transparent !important;
  border-bottom: 2px solid #409EFF !important;
  border-top: none !important;
  box-shadow: none !important;
  text-shadow: none !important;
}

:deep(.el-menu--horizontal > .el-menu-item.is-active::before),
:deep(.el-menu--horizontal > .el-menu-item.is-active::after) {
  display: none !important;
  box-shadow: none !important;
}

/* 右侧工具栏样式 */
.nav-right-tools {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
  margin-left: auto;
}

.weather-display {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #4b5563;
  white-space: nowrap;
  margin-right: 24px;
}

.weather-city {
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.weather-icon-wrapper {
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.weather-icon {
  font-size: 18px;
}

.weather-sep {
  opacity: 0.7;
}

.weather-temp {
  min-width: 40px;
}

.cart-link {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 8px;
  text-decoration: none;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.2s ease;
}

.cart-link:hover {
  color: #111827;
  background: #f3f4f6;
}

.cart-icon {
  font-size: 20px;
}

.cart-badge-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.el-badge__content) {
  background: #ef4444;
  border: none;
  font-weight: 500;
}

/* 用户下拉菜单样式 */
.user-dropdown {
  flex-shrink: 0;
}

.el-dropdown-link {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s ease;
}

.el-dropdown-link:hover {
  background: #f3f4f6;
}

.el-dropdown-link span:last-child {
  font-size: 14px;
  font-weight: 500;
  color: #111827;
}

.user-dropdown :deep(.el-dropdown-menu) {
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
  padding: 4px;
  margin-top: 4px;
}

.user-dropdown :deep(.el-dropdown-menu__item) {
  border-radius: 6px;
  padding: 8px 12px;
  margin: 2px 0;
  transition: background 0.2s ease;
}

.user-dropdown :deep(.el-dropdown-menu__item:hover) {
  background: #f3f4f6;
  color: #111827;
}

/* 响应式调整 */
@media (max-width: 1400px) {
  .navbar-container {
    padding: 0 40px;
  }
  
  :deep(.el-menu--horizontal > .el-menu-item) {
    padding: 0 20px;
  }
}

@media (max-width: 1200px) {
  .navbar-container {
    padding: 0 30px;
  }
  
  .logo-text {
    font-size: 18px;
  }
  
  :deep(.el-menu--horizontal > .el-menu-item) {
    padding: 0 16px;
    font-size: 14px;
  }
}
</style> 