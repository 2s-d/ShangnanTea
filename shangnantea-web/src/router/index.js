import { createRouter, createWebHistory } from 'vue-router'
import { useTokenStorage } from '@/composables/useStorage'
import { message } from '@/components/common'
import { ROLES } from '@/composables/useAuth'
import store from '@/store'

// 导入组件 - 使用实际存在的路径
import CultureHomePage from '@/views/forum/culturehome/CultureHomePage.vue'

// 创建token存储实例
const tokenStorage = useTokenStorage()
const { verifyToken, removeToken } = tokenStorage

// 路由配置
const routes = [
  // 首页重定向到登录页
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/tea-culture',
    name: 'TeaCulture',
    component: CultureHomePage,
    meta: { 
      title: '茶文化 - 商南茶文化',
      requireAuth: true
    }
  },
  
  // 茶文化文章详情路由
  {
    path: '/article/:id',
    name: 'ArticleDetail',
    component: () => import('@/views/forum/culturehome/ArticleDetailPage.vue'),
    meta: { 
      title: '文章详情 - 商南茶文化',
      requireAuth: true
    }
  },
  
  // 茶叶相关路由
  {
    path: '/tea/mall',
    name: 'TeaMall',
    component: () => import('@/views/tea/list/TeaListPage.vue'),
    meta: { 
      title: '茶叶商城 - 商南茶文化',
      requireAuth: true
    }
  },
  {
    path: '/tea/:id',
    name: 'TeaDetail',
    component: () => import('@/views/tea/detail/TeaDetailPage.vue'),
    meta: { 
      title: '茶叶详情 - 商南茶文化',
      requireAuth: true
    }
  },
  {
    path: '/tea/manage',
    name: 'TeaManage',
    component: () => import('@/views/tea/manage/TeaManagePage.vue'),
    meta: {
      title: '茶叶管理 - 商南茶文化',
      requireAuth: true,
      roles: [ROLES.ADMIN]
    }
  },
  
  // 认证路由
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/user/auth/LoginPage.vue'),
    meta: { 
      title: '登录 - 商南茶文化',
      requireAuth: false
    }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/user/auth/RegisterPage.vue'),
    meta: { 
      title: '注册 - 商南茶文化',
      requireAuth: false
    }
  },
  {
    path: '/reset-password',
    name: 'ResetPassword',
    component: () => import('@/views/user/auth/ResetPasswordPage.vue'),
    meta: { 
      title: '密码找回 - 商南茶文化',
      requireAuth: false
    }
  },
  
  // 论坛路由
  {
    path: '/forum/list',
    name: 'ForumList',
    component: () => import('@/views/forum/list/ForumListPage.vue'),
    meta: {
      title: '茶友论坛 - 商南茶文化',
      requireAuth: true
    }
  },
  {
    path: '/forum/:id',
    name: 'ForumDetail',
    component: () => import('@/views/forum/detail/ForumDetailPage.vue'),
    meta: { 
      title: '帖子详情 - 商南茶文化',
      requireAuth: true
    }
  },
  {
    path: '/forum/topic/:id',
    name: 'ForumTopic',
    component: () => import('@/views/forum/list/ForumListPage.vue'),
    meta: { 
      title: '版块内容 - 商南茶文化',
      requireAuth: true
    }
  },
  {
    path: '/forum/manage',
    name: 'ForumManage',
    component: () => import('@/views/forum/manage/ForumManagePage.vue'),
    meta: { 
      title: '内容管理 - 商南茶文化',
      requireAuth: true,
      roles: [ROLES.ADMIN]
    }
  },
  {
    path: '/culture/manage',
    name: 'CultureManage',
    component: () => import('@/views/forum/manage/CultureManagerPage.vue'),
    meta: { 
      title: '茶文化内容管理 - 商南茶文化',
      requireAuth: true,
      roles: [ROLES.ADMIN]
    }
  },
  
  // 订单路由
  {
    path: '/order/cart',
    name: 'OrderCart',
    component: () => import('@/views/order/cart/CartPage.vue'),
    meta: {
      title: '购物车 - 商南茶文化',
      requireAuth: true
    }
  },
  {
    path: '/order/checkout',
    name: 'OrderCheckout',
    component: () => import('@/views/order/payment/CheckoutPage.vue'),
    meta: {
      title: '订单结算 - 商南茶文化',
      requireAuth: true
    }
  },
  {
    path: '/order/payment',
    name: 'OrderPayment',
    component: () => import('@/views/order/payment/PaymentPage.vue'),
    meta: {
      title: '订单支付 - 商南茶文化',
      requireAuth: true
    }
  },
  {
    path: '/order/list',
    name: 'OrderList',
    component: () => import('@/views/order/list/OrderListPage.vue'),
    meta: {
      title: '我的订单 - 商南茶文化',
      requireAuth: true,
      roles: [ROLES.USER, ROLES.SHOP]
    }
  },
  {
    path: '/order/detail/:id',
    name: 'OrderDetail',
    component: () => import('@/views/order/detail/OrderDetailPage.vue'),
    meta: {
      title: '订单详情 - 商南茶文化',
      requireAuth: true
    }
  },
  {
    path: '/order/manage',
    name: 'OrderManage',
    component: () => import('@/views/order/manage/OrderManagePage.vue'),
    meta: {
      title: '订单管理 - 商南茶文化',
      requireAuth: true,
      roles: [ROLES.ADMIN, ROLES.SHOP]
    }
  },
  {
    path: '/order/review/:id',
    name: 'OrderReview',
    component: () => import('@/views/order/review/OrderReviewPage.vue'),
    meta: {
      title: '订单评价 - 商南茶文化',
      requireAuth: true
    }
  },
  
  // 用户路由

  {
    path: '/user/settings/:tab?',
    name: 'UserSettings',
    component: () => import('@/views/user/settings/SettingsPage.vue'),
    meta: {
      title: '个人设置 - 商南茶文化',
      requireAuth: true
    }
  },
  {
    path: '/user/address',
    name: 'UserAddress',
    component: () => import('@/views/user/address/AddressPage.vue'),
    meta: {
      title: '收货地址 - 商南茶文化',
      requireAuth: true
    }
  },
  {
    path: '/user/manage',
    name: 'UserManage',
    component: () => import('@/views/user/manage/UserManagePage.vue'),
    meta: {
      title: '用户管理 - 商南茶文化',
      requireAuth: true,
      roles: [ROLES.ADMIN]
    }
  },
  {
    path: '/user/profile',
    name: 'UserProfileEdit',
    component: () => import('@/views/user/profile/ProfilePage.vue'),
    meta: {
      title: '个人资料 - 商南茶文化',
      requireAuth: true
    }
  },
  
  // 店铺路由
  {
    path: '/shop/manage',
    name: 'ShopManage',
    component: () => import('@/views/shop/manage/ShopManagePage.vue'),
    meta: {
      title: '商家店铺 - 商南茶文化',
      requireAuth: true,
      roles: [ROLES.SHOP]
    }
  },
  {
    path: '/shop/list',
    name: 'ShopList',
    component: () => import('@/views/shop/list/ShopListPage.vue'),
    meta: {
      title: '商铺列表 - 商南茶文化',
      requireAuth: true
    }
  },
  {
    path: '/shop/:id',
    name: 'ShopDetail',
    component: () => import('@/views/shop/detail/ShopDetailPage.vue'),
    meta: {
      title: '店铺详情 - 商南茶文化',
      requireAuth: true
    }
  },
  
  // 消息中心
  {
    path: '/message/center/:tab?',
    name: 'MessageCenter',
    component: () => import('@/views/message/notification/NotificationsPage.vue'),
    meta: {
      title: '消息管理 - 商南茶文化',
      requireAuth: true
    }
  },
  
  // 用户模块测试页面
  {
    path: '/test/user',
    name: 'UserTestPage',
    component: () => import('@/views/test/UserTestPage.vue'),
    meta: {
      title: '用户模块测试 - 商南茶文化',
      requireAuth: false
    }
  },
  
  // 聊天页面
  {
    path: '/message/chat',
    name: 'MessageChat',
    component: () => import('@/views/message/chat/ChatPage.vue'),
    meta: {
      title: '聊天 - 商南茶文化',
      requireAuth: true
    }
  },
  
  // 个人主页
  {
    path: '/profile/:tab?',
    name: 'UserProfile',
    component: () => import('@/views/message/homepage/UserHomePage.vue'),
    meta: {
      title: '个人主页 - 商南茶文化',
      requireAuth: true
    }
  },
  
  // 关注管理
  // {
  //   path: '/message/follows',
  //   name: 'MessageFollows',
  //   component: () => import('@/views/message/follows/FollowsPage.vue'),
  //   meta: {
  //     title: '我的关注 - 商南茶文化',
  //     requireAuth: true
  //   }
  // },
  
  // 收藏管理
  // {
  //   path: '/message/favorites',
  //   name: 'MessageFavorites',
  //   component: () => import('@/views/message/favorites/FavoritesPage.vue'),
  //   meta: {
  //     title: '我的收藏 - 商南茶文化',
  //     requireAuth: true
  //   }
  // },
  
  // 发布内容
  // {
  //   path: '/message/published',
  //   name: 'MessagePublished',
  //   component: () => import('@/views/message/content/PublishedContentPage.vue'),
  //   meta: {
  //     title: '我的发布 - 商南茶文化',
  //     requireAuth: true
  //   }
  // },
  
  // 错误页面
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/views/error/403.vue'),
    meta: {
      title: '无权限访问 - 商南茶文化',
      requireAuth: true
    }
  },
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: {
      title: '页面未找到 - 商南茶文化',
      requireAuth: true
    }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/login'
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(),
  routes
})

// 不需要登录验证的白名单路径 - 只保留登录和注册
const whiteList = [
  '/login',
  '/register'
]

// 全局前置守卫
router.beforeEach(async (to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? to.meta.title : '商南茶文化推广与销售平台'
  
  // 白名单路由直接通过
  if (whiteList.includes(to.path)) {
    next()
    return
  }
  
  try {
    // 验证token，获取用户信息
    const userInfo = verifyToken()
    
    if (!userInfo) {
      // 如果token无效，进行清理并提示
      removeToken()
      store.commit('user/CLEAR_USER')
      store.dispatch('user/handleSessionExpired')
      
      // 重定向到登录页
      if (to.path !== '/login') {
        return next({
          path: '/login',
          query: { redirect: to.fullPath }
        })
      }
    }
    
    // 已登录状态下访问登录页，重定向到茶文化页面
    if (to.path === '/login') {
      next({ path: '/tea-culture' })
      return
    }
    
    // 检查是否需要特定角色权限
    if (to.meta.roles && to.meta.roles.length > 0) {
      const hasRole = to.meta.roles.includes(userInfo.role)
      
      if (!hasRole) {
        // 没有权限访问
        store.dispatch('user/handlePermissionDenied')
        
        // 重定向到403页面
        return next({ path: '/403' })
      }
    }
    
    // 权限检查通过，允许访问请求的路由
    next()
  } catch (e) {
    console.error('路由认证错误:', e)
    next('/login')
  }
})

export default router 