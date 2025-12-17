<template>
  <!-- UI-DEV-START -->
  <!-- 开发时始终显示导航栏，无视登录状态 -->
  <div class="navbar">
    <div class="navbar-container">
      <!-- 左侧Logo -->
      <div class="nav-logo">
        <router-link to="/tea-culture">
          <img src="/images/tea-logo.png" alt="商南茶文化" class="logo-img">
          <span class="logo-text">商南茶文化</span>
        </router-link>
      </div>

      <!-- 中间导航菜单 -->
      <el-menu
        :default-active="activeIndex"
        mode="horizontal"
        router
        background-color="#fff"
        text-color="#333"
        active-text-color="#409EFF"
        class="main-menu">
        
        <!-- 普通用户导航栏 -->
        <template v-if="currentRole === 2">
          <el-menu-item index="/tea-culture">茶文化</el-menu-item>
          <el-menu-item index="/tea/mall">茶叶商城</el-menu-item>
          <el-menu-item index="/forum/list">茶友论坛</el-menu-item>
          <el-menu-item index="/order/list">我的订单</el-menu-item>
          <el-menu-item index="/message/center">消息管理</el-menu-item>
          <el-menu-item index="/profile">个人主页</el-menu-item>
          <el-menu-item index="/order/cart">购物车</el-menu-item>
        </template>
        
        <!-- 茶叶商家导航栏 -->
        <template v-if="currentRole === 3">
          <el-menu-item index="/tea-culture">茶文化</el-menu-item>
          <el-menu-item index="/tea/mall">茶叶商城</el-menu-item>
          <el-menu-item index="/forum/list">茶友论坛</el-menu-item>
          <el-menu-item index="/order/manage">订单管理</el-menu-item>
          <el-menu-item index="/shop/manage">商家店铺</el-menu-item>
          <el-menu-item index="/message/center">消息管理</el-menu-item>
          <el-menu-item index="/profile">个人主页</el-menu-item>
          <el-menu-item index="/order/cart">购物车</el-menu-item>
        </template>
        
        <!-- 平台管理员导航栏 -->
        <template v-if="currentRole === 1">
          <el-menu-item index="/tea-culture">茶文化</el-menu-item>
          <el-menu-item index="/tea/manage">茶叶管理</el-menu-item>
          <el-menu-item index="/order/manage">订单管理</el-menu-item>
          <el-menu-item index="/forum/manage">内容管理</el-menu-item>
          <el-menu-item index="/user/manage">用户管理</el-menu-item>
          <el-menu-item index="/message/center">消息管理</el-menu-item>
          <el-menu-item index="/profile">个人主页</el-menu-item>
          <el-menu-item index="/order/cart">购物车</el-menu-item>
        </template>
      </el-menu>

      <!-- 右侧工具栏 -->
      <div class="nav-right">
        <!-- 开发者角色切换 -->
        <el-dropdown trigger="click" class="role-select">
          <span class="el-dropdown-link">
            <el-button type="primary" size="small">开发测试用户</el-button>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="switchRole(1)">管理员视图</el-dropdown-item>
              <el-dropdown-item @click="switchRole(2)">普通用户视图</el-dropdown-item>
              <el-dropdown-item @click="switchRole(3)">商家视图</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        
        <!-- 用户下拉菜单 -->
        <div class="user-dropdown">
          <el-dropdown trigger="click" @command="handleCommand">
            <span class="el-dropdown-link">
              <SafeImage src="/mock-images/default.jpg" type="avatar" alt="用户头像" style="width:30px;height:30px;border-radius:50%;object-fit:cover;" />
              <span>{{ username }}</span>
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
  <!-- UI-DEV-END -->

  <!-- 
  // ... existing code ...
  <div class="navbar" v-if="isLoggedIn">
    <el-menu
      :default-active="activeIndex"
      mode="horizontal"
      router
      background-color="#fff"
      text-color="#333"
      active-text-color="#409EFF">
      
      <template v-if="userRole === 2">
        <el-menu-item index="/tea-culture">茶文化</el-menu-item>
        <el-menu-item index="/tea/mall">茶叶商城</el-menu-item>
        <el-menu-item index="/forum/list">茶友论坛</el-menu-item>
        <el-menu-item index="/order/list">我的订单</el-menu-item>
        <el-menu-item index="/message/center">消息管理</el-menu-item>
        <el-menu-item index="/profile">个人主页</el-menu-item>
        <el-menu-item index="/order/cart">购物车</el-menu-item>
      </template>
      
      <template v-else-if="userRole === 3">
        <el-menu-item index="/tea-culture">茶文化</el-menu-item>
        <el-menu-item index="/tea/mall">茶叶商城</el-menu-item>
        <el-menu-item index="/forum/list">茶友论坛</el-menu-item>
        <el-menu-item index="/order/manage">订单管理</el-menu-item>
        <el-menu-item index="/shop/manage">商家店铺</el-menu-item>
        <el-menu-item index="/message/center">消息管理</el-menu-item>
        <el-menu-item index="/profile">个人主页</el-menu-item>
        <el-menu-item index="/order/cart">购物车</el-menu-item>
      </template>
      
      <template v-else-if="userRole === 1">
        <el-menu-item index="/tea-culture">茶文化</el-menu-item>
        <el-menu-item index="/tea/manage">茶叶管理</el-menu-item>
        <el-menu-item index="/order/manage">订单管理</el-menu-item>
        <el-menu-item index="/forum/manage">内容管理</el-menu-item>
        <el-menu-item index="/user/manage">用户管理</el-menu-item>
        <el-menu-item index="/message/center">消息管理</el-menu-item>
        <el-menu-item index="/profile">个人主页</el-menu-item>
        <el-menu-item index="/order/cart">购物车</el-menu-item>
      </template>
      
      <div class="user-dropdown">
        <el-dropdown trigger="click" @command="handleCommand">
          <span class="el-dropdown-link">
            <el-avatar :size="30" :src="/mock-images/default.jpg"></el-avatar>
            <span>{{ username }}</span>
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
    </el-menu>
  </div>
  -->
</template>

<script>
// 调整导入顺序，确保 vue 核心工具先导入
import { computed, ref, onMounted } from 'vue'
import { useStore } from 'vuex'
import { useRouter, useRoute } from 'vue-router'

// 然后导入工具类
import { useAuth } from '@/composables/useAuth'
// 导入开发工具类
import devUtils, { setDevMode, setDevRole, DEV_ROLES } from '@/utils/devUtils'
// 引入SafeImage通用图片组件
import SafeImage from '@/components/common/form/SafeImage.vue'

export default {
  name: 'NavBar',
  components: {
    SafeImage
  },
  setup() {
    /* UI-DEV-START */
    // 纯UI开发阶段的代码
    const router = useRouter()
    const route = useRoute()
    
    // 当前激活的导航项
    const activeIndex = ref('/')
    
    // 开发时使用的变量
    const currentRole = ref(2) // 默认以普通用户视图开始
    const showAllMenus = ref(false)
    const username = ref('开发测试用户')
    
    // 用户信息
    const userInfo = ref({
      avatar: '', // 让SafeImage自动兜底显示默认头像
      username: '开发测试用户'
    })
    
    // 购物车数量
    const cartCount = ref(0)
    
    // 删除 defaultAvatar 变量（已无用）
    // const defaultAvatar = ''
    const defaultLogo = 'https://img.zcool.cn/community/01233056fb62fe32f875520f7b67cb.jpg'
    
    // 初始化购物车数量
    const initCartCount = () => {
      // 从localStorage获取购物车数量
      const storedCount = localStorage.getItem('cartCount')
      if (storedCount) {
        cartCount.value = parseInt(storedCount, 10)
      }
      
      // 监听购物车更新事件
      window.addEventListener('cart-updated', (e) => {
        if (e.detail && typeof e.detail.count !== 'undefined') {
          cartCount.value = e.detail.count
        }
      })
      
      // 添加storage事件监听，当其他页面更新购物车数量时更新显示
      window.addEventListener('storage', (e) => {
        if (e.key === 'cartCount') {
          cartCount.value = e.newValue ? parseInt(e.newValue, 10) : 0
        }
      })
    }
    
    // 当路由变化时更新激活项
    onMounted(() => {
      activeIndex.value = route.path
      
      // 初始化购物车数量
      initCartCount()
      
      // 检查开发模式状态
      const isDevModeEnabled = localStorage.getItem('dev_mode') === 'true'
      
      // 自动启用开发模式
      if (!isDevModeEnabled) {
        setDevMode(true)
      }
      
      // 检查是否有上次选择的开发角色
      const devRole = localStorage.getItem('dev_current_role')
      if (devRole) {
        currentRole.value = parseInt(devRole)
      }
      
      // 初始化开发工具，在控制台显示使用说明
      devUtils.initDevTools()

      // 处理 ResizeObserver 错误
      window.addEventListener('error', (e) => {
        if (e.message === 'ResizeObserver loop completed with undelivered notifications.') {
          e.stopPropagation()
          return false
        }
      })
    })
    
    // 开发时切换角色的函数
    const switchRole = (role) => {
      currentRole.value = role
      setDevRole(role)
      
      // 根据角色展示不同提示
      let roleName = ''
      switch(role) {
        case DEV_ROLES.ADMIN: roleName = '管理员'; break;
        case DEV_ROLES.USER: roleName = '普通用户'; break;
        case DEV_ROLES.SHOP: roleName = '商家'; break;
      }
      alert(`已切换到${roleName}视图`)
    }
    
    // 模拟登出功能
    const handleLogout = () => {
      alert('模拟登出操作')
      router.push('/login')
    }
    
    // 处理下拉菜单点击
    const handleCommand = (command) => {
      if (command === 'logout') {
        handleLogout()
      } else {
        router.push(command)
      }
    }
    
    return {
      activeIndex,
      currentRole,
      showAllMenus,
      username,
      userInfo,
      cartCount,
      defaultLogo,
      switchRole,
      handleCommand,
      initCartCount
    }
    /* UI-DEV-END */

    /* 
    const store = useStore()
    const router = useRouter()
    const route = useRoute()
    
    // 使用useAuth
    const { isAdmin, isShop, isUser, isLoggedIn, ROLES } = useAuth()
    
    // 当前激活的导航项
    const activeIndex = ref('/')
    
    // 当路由变化时更新激活项
    onMounted(() => {
      activeIndex.value = route.path
    })
    
    // 用户角色
    const userRole = computed(() => {
      return store.state.user.userInfo?.role || 0
    })
    
    // 用户信息
    const username = computed(() => store.state.user.userInfo?.username || '用户')
    const userAvatar = computed(() => {
      return store.state.user.userInfo?.avatar || '/avatar/default.png'
    })
    
    // 退出登录
    const handleLogout = async () => {
      try {
        await store.dispatch('user/logout')
        router.push('/login')
      } catch (error) {
        console.error('退出登录失败:', error)
      }
    }
    
    // 处理下拉菜单点击
    const handleCommand = (command) => {
      if (command === 'logout') {
        handleLogout()
      } else {
        router.push(command)
      }
    }
    
    return {
      activeIndex,
      userRole,
      isAdmin,
      isShop,
      isUser,
      isLoggedIn,
      username,
      userAvatar,
      handleLogout,
      handleCommand,
      ROLES
    }
    */
  }
}
</script>

<style scoped>
.navbar {
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 1000;
  width: 100%;
  min-width: 1200px; /* 确保最小宽度，防止折叠 */
}

.navbar-container {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  white-space: nowrap; /* 防止文字换行 */
}

/* 顶部小条样式 */
.top-bar {
  background-color: #f5f7fa;
  height: 30px;
  line-height: 30px;
  font-size: 12px;
  border-bottom: 1px solid #e6e6e6;
}

.top-bar-right {
  text-align: right;
}

.profile-link {
  color: #606266;
  text-decoration: none;
}

/* Logo样式 */
.nav-logo {
  display: flex;
  align-items: center;
  padding: 0 15px;
}

.nav-logo a {
  display: flex;
  align-items: center;
  text-decoration: none;
  color: #303133;
}

.logo-img {
  height: 30px;
  margin-right: 10px;
}

.logo-text {
  font-size: 18px;
  font-weight: bold;
}

/* 主导航菜单样式 */
.main-menu {
  border-bottom: none !important;
  flex: 1;
  display: flex !important; /* 强制显示为flex布局 */
  white-space: nowrap;
  overflow: visible !important; /* 防止溢出隐藏 */
}

.main-menu .el-menu-item {
  height: 60px;
  line-height: 60px;
  flex-shrink: 0; /* 防止菜单项收缩 */
}

/* 移除折叠相关的样式 */
.el-menu--collapse {
  display: flex !important;
}

.el-menu--horizontal > .el-menu-item,
.el-menu--horizontal > .el-submenu {
  float: none !important;
  display: inline-flex !important;
}

/* 右侧工具栏样式 */
.nav-right {
  display: flex;
  align-items: center;
  margin-left: auto; /* 确保右对齐 */
  flex-shrink: 0; /* 防止工具栏收缩 */
}

.role-select,
.cart-link,
.user-dropdown {
  margin-left: 15px;
}

.role-select {
  background-color: transparent;
  opacity: 0;
}

.role-select .el-dropdown-link {
  background-color: transparent;
}

.role-select .el-button--text {
  background-color: transparent;
  color: #409EFF;
}

.role-select:hover {
  opacity: 0;
}

.cart-link {
  text-decoration: none;
}

.admin-btn {
  padding: 8px 15px;
  border-radius: 4px;
}

.el-dropdown-link {
  cursor: pointer;
  display: flex;
  align-items: center;
}

.user-dropdown .el-avatar {
  margin-right: 8px;
  vertical-align: middle;
}

.cart-badge {
  position: absolute;
  top: 10px;
  right: 8px;
  background-color: #f56c6c;
  color: white;
  border-radius: 50%;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  font-size: 12px;
  line-height: 16px;
  text-align: center;
  font-weight: bold;
}
</style> 