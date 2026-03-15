<template>
  <div class="user-home-page">
    <!-- 页面标题横条（100%宽度） -->
    <div class="page-title-bar">
      <div class="page-title-container">
        <div class="page-title-content">
          <!-- 返回按钮（仅当查看别人的主页时显示） -->
          <el-button 
            v-if="!isOwnProfile && canGoBack" 
            class="back-button" 
            text 
            @click="handleGoBack">
            <el-icon><ArrowLeft /></el-icon>
            <span>返回上一页</span>
          </el-button>
          
          <!-- 标题装饰和文字 -->
          <div class="title-wrapper">
            <!-- 蓝色竖杠始终显示，文字只在查看自己的主页时显示 -->
            <div class="title-decoration">
              <span class="decoration-text" v-if="isOwnProfile">个人主页</span>
            </div>
            <h1 class="page-title-text">
              {{ isOwnProfile ? '我的主页' : `${userInfo.nickname || userInfo.username || '用户'}的个人主页` }}
            </h1>
          </div>
        </div>
      </div>
    </div>
    
    <div class="container">
      <el-card class="home-card" v-loading="loading">
        <!-- 上半部分：用户信息展示区 -->
        <div class="user-profile-section">
          <div class="user-basic-info">
            <div class="user-avatar">
              <!-- 个人主页展示头像：圆形基础层 + 上方 120° 开放区域 pop 层 -->
              <div class="avatar-pop-wrapper">
                <div class="avatar-circle">
                  <SafeImage 
                    :src="userInfo.avatar" 
                    type="avatar" 
                    class="avatar-base"
                  />
                </div>
                <!-- pop 层：同一张图，只开放“上边 120°”的大区域（仅 GIF 动图显示） -->
                <SafeImage 
                  v-if="isGifAvatar"
                  :src="userInfo.avatar" 
                  type="avatar" 
                  class="avatar-pop"
                />
              </div>
            </div>
            <div class="user-details">
              <h2 class="user-name">
                {{ userInfo.nickname || '用户' }}
                <span class="user-gender">
                  <el-icon color="#409EFF" v-if="userInfo.gender === 1"><Male /></el-icon>
                  <el-icon color="#FF4949" v-else-if="userInfo.gender === 2"><Female /></el-icon>
                  <el-tooltip v-else content="性别保密" placement="top">
                    <el-icon color="#909399"><QuestionFilled /></el-icon>
                  </el-tooltip>
                </span>
                <!-- 角色标签：1-管理员，2-普通用户，3-商家（必须都能显示，否则无法判断接口返回是否正确） -->
                <el-tag v-if="userInfo.role === 1" type="danger" size="small">
                  <el-icon style="margin-right:4px;"><Key /></el-icon>管理员
                </el-tag>
                <el-tag v-else-if="userInfo.role === 3" type="warning" size="small">
                  <el-icon style="margin-right:4px;"><Shop /></el-icon>商家
                </el-tag>
                <el-tag v-else type="info" size="small">
                  <el-icon style="margin-right:4px;"><UserFilled /></el-icon>用户
                </el-tag>
              </h2>
              <div class="user-id">ID: {{ userInfo.id || '' }}</div>
              <div class="user-location" v-if="formattedLocation">
                <el-icon><Location /></el-icon>
                {{ formattedLocation }}
              </div>
              <div class="user-bio">{{ userInfo.bio || '这个用户很懒，什么都没有留下...' }}</div>
              <div class="user-register-time" v-if="userInfo.registerTime">
                注册时间：{{ formatDate(userInfo.registerTime) }}
              </div>
              <div class="user-shop-link" v-if="userInfo.role === 3 && userInfo.shopId">
                <el-link type="primary" @click="goToShop">
                  <el-icon><Shop /></el-icon>
                  {{ userInfo.shopName || '我的店铺' }}
                </el-link>
              </div>
            </div>
          </div>
          <div class="user-actions">
            <!-- 查看自己主页时显示编辑按钮 -->
            <el-button v-if="isOwnProfile" type="primary" @click="handleEditProfile">
              <el-icon><Edit /></el-icon>
              编辑资料
            </el-button>
            <!-- 查看他人主页时显示私信+关注按钮 -->
            <template v-else>
              <el-button class="follow-btn" :class="{ 'is-following': isFollowing }" @click="handleFollow">
                {{ isFollowing ? '√已关注' : '+关注' }}
            </el-button>
              <el-button class="private-message-btn" @click="handlePrivateMessage">
                私信
              </el-button>
            </template>
          </div>
        </div>
        
        <!-- 用户统计数据（当对方主页设置为私密时不展示统计） -->
        <div class="user-statistics" v-if="isProfileVisible || isOwnProfile">
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
          
        <!-- 下半部分：内容区域 -->
        <div class="home-content">
          <!-- 他人主页设置为私密时，只提示，不展示具体内容 -->
          <template v-if="!isOwnProfile && !isProfileVisible">
            <div class="profile-locked">
              <el-empty
                description="该用户已将个人主页设置为私密，目前无法查看其发布、关注和收藏等详细内容"
                :image-size="200"
              />
                  </div>
          </template>
          <template v-else>
            <!-- 标签页内容 -->
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
          </template>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, markRaw, watch, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessageStore } from '@/stores/message'
import { useUserStore } from '@/stores/user'

import ProfilePage from '@/views/user/profile/ProfilePage.vue'
import FollowsPage from '@/views/message/follows/FollowsPage.vue'
import FavoritesPage from '@/views/message/favorites/FavoritesPage.vue'
import PublishedContentPage from '@/views/message/content/PublishedContentPage.vue'
import { 
  User, UserFilled, Star, Document, Male, Female, Edit, Shop, Key, QuestionFilled, Location, ArrowLeft
} from '@element-plus/icons-vue'
import { formatLocationDisplay } from '@/utils/region'
import SafeImage from '@/components/common/form/SafeImage.vue'
import { showByCode } from '@/utils/apiMessages'
import { commonPromptMessages } from '@/utils/promptMessages'

const router = useRouter()
const route = useRoute()
const messageStore = useMessageStore()
const userStore = useUserStore()
    
    // 从路由参数获取用户ID和tab
    // 路由格式：/profile/:userId?/:tab?
    // 如果第一个参数是已知的tab（published等），则当作tab，userId为undefined
    // 如果第一个参数不是已知的tab，则当作userId
    const userId = computed(() => {
      const firstParam = route.params.userId
      const secondParam = route.params.tab
      
      // 如果第一个参数是已知的tab，则它是tab，userId为undefined
      if (firstParam && componentMap[firstParam]) {
        return 'current'
      }
      
      // 如果第一个参数不是已知的tab，则它是userId
      if (firstParam && firstParam !== 'current') {
        return firstParam
      }
      
      // 默认返回current
      return 'current'
    })
    
    // 从路由参数获取tab
    const routeTab = computed(() => {
      const firstParam = route.params.userId
      const secondParam = route.params.tab
      
      // 如果第一个参数是已知的tab，则它是tab
      if (firstParam && componentMap[firstParam]) {
        return firstParam
      }
      
      // 否则使用第二个参数作为tab
      return secondParam
    })
    
    // 从Pinia获取用户信息（优先使用消息模块返回的数据，不足部分用全局用户信息补全）
    const userInfo = computed(() => {
      const profile = messageStore.userProfile || {}
      const base = userStore.userInfo || {}
      // 确保 role 和 gender 都是数字类型
      const role = profile.role != null ? Number(profile.role) : (base.role != null ? Number(base.role) : null)
      const gender = profile.gender != null ? Number(profile.gender) : (base.gender != null ? Number(base.gender) : 0)
      return {
        id: profile.id || base.id || base.userId || '',
        username: profile.username || base.username || '',
        nickname: profile.nickname || base.nickname || '',
        avatar: profile.avatar || base.avatar || '',
        gender: gender,
        bio: profile.bio || base.bio || '',
        role: role,
        currentLocation: profile.currentLocation || base.currentLocation || '',
        registerTime: profile.registerTime || base.createTime || base.registerTime || null,
        shopId: profile.shopId || base.shopId || null,
        shopName: profile.shopName || base.shopName || null,
        isFollowed: profile.isFollowed || false
      }
    })
    
    // 格式化现居地显示（使用 region.js 的工具函数）
    const formattedLocation = computed(() => {
      const location = userInfo.value.currentLocation
      if (!location) return ''
      return formatLocationDisplay(location)
    })
    
    // 从Pinia获取用户统计数据
    const userStatistics = computed(() => messageStore.userStatistics || {
      postCount: 0,
      likeCount: 0,
      favoriteCount: 0,
      followingCount: 0,
      followerCount: 0,
      commentCount: 0
    })
    
    // 加载状态
    const loading = computed(() => messageStore.loading)
    
    // 判断头像是否是 GIF 动图
    const isGifAvatar = computed(() => {
      const avatar = userInfo.value.avatar || ''
      return avatar.toLowerCase().endsWith('.gif')
    })
    
    // 当前登录用户ID（从全局用户信息里拿）
    const currentUserId = computed(() => {
      const base = userStore.userInfo || {}
      return base.id || base.userId || null
    })
    
    // 判断是否是查看自己的主页
    const isOwnProfile = computed(() => {
      // 路由参数显式指向 current 时，一定是自己的主页
      if (userId.value === 'current' || !route.params.userId) {
        return true
      }
      // 当路由上带的是实际用户ID时，如果与当前登录用户ID一致，也视为自己的主页
      if (currentUserId.value && userId.value === currentUserId.value) {
        return true
      }
      return false
    })

    // 是否允许查看当前用户的个人主页（自己永远允许）
    const isProfileVisible = computed(() => {
      if (isOwnProfile.value) return true
      const profile = messageStore.userProfile || {}
      // 后端没有返回该字段时默认可见
      if (profile.profileVisible === undefined || profile.profileVisible === null) return true
      return !!profile.profileVisible
    })
    
    // 判断是否可以返回（检查是否有历史记录，且不是直接访问）
    const canGoBack = computed(() => {
      // 如果是从其他页面跳转过来的，可以返回
      // 检查是否有referrer或者history.length > 1
      if (typeof window !== 'undefined') {
        // 如果有document.referrer且不是同源，或者history.length > 1，说明可以返回
        return window.history.length > 1 || (document.referrer && !document.referrer.includes(window.location.origin))
      }
      return false
    })
    
    // 返回上一页
    const handleGoBack = () => {
      router.back()
    }
    
    // 关注状态（从接口返回的isFollowed字段获取）
    const isFollowing = computed(() => userInfo.value?.isFollowed || false)
    
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
      favorites: markRaw(FavoritesPage)
    }
    
    // 活动菜单（默认显示"发布"）
    const activeMenu = ref('published')
    // 当前显示的组件
    const currentComponent = ref(componentMap.published)
    
    // 判断当前菜单是否有对应组件
    const hasComponent = computed(() => {
      return componentMap[activeMenu.value] !== undefined
    })
    
    // 从路由参数初始化activeMenu
    onMounted(async () => {
      const tab = routeTab.value
      if (tab && componentMap[tab]) {
        activeMenu.value = tab
        currentComponent.value = componentMap[tab]
      }
      
      // 加载用户信息
      await loadUserData()
    })
    
    // 监听路由参数变化
    watch(() => routeTab.value, newTab => {
      if (newTab && componentMap[newTab]) {
        activeMenu.value = newTab
          currentComponent.value = componentMap[newTab]
      }
    })
    
    // 监听用户ID变化
    watch(() => userId.value, async () => {
      await loadUserData()
    })
    
    // 菜单选择处理
    const handleMenuSelect = key => {
      activeMenu.value = key
      
      // 如果存在对应组件就设置，否则显示动态或开发中
      if (componentMap[key]) {
        currentComponent.value = componentMap[key]
      } else {
        currentComponent.value = null
      }
      
      // 更新路由参数（不刷新页面），保留userId参数
      const currentUserId = userId.value
      if (currentUserId && currentUserId !== 'current') {
      router.push({
          path: `/profile/${currentUserId}/${key}`,
          replace: true
        })
      } else {
        router.push({
          path: `/profile/current/${key}`,
        replace: true
      })
      }
    }
    
    // 处理编辑资料
    const handleEditProfile = () => {
      router.push('/user/profile')
    }
    
    // 处理关注/取消关注
    const handleFollow = async () => {
      if (isOwnProfile.value) return // 自己的主页不需要关注
      
      try {
        if (isFollowing.value) {
          // 取消关注：直接传递 targetId 和 targetType
          const res = await userStore.removeFollow({
            targetId: userId.value,
            targetType: 'user'
          })
          showByCode(res.code)
          // 重新加载用户信息以更新isFollowed状态
          await loadUserData()
        } else {
          // 添加关注
          const res = await userStore.addFollow({
            targetId: userId.value,
            targetType: 'user',
            targetName: userInfo.value.nickname,
            targetAvatar: userInfo.value.avatar
          })
          showByCode(res.code)
          // 重新加载用户信息以更新isFollowed状态
          await loadUserData()
        }
      } catch (error) {
        console.error('关注操作失败:', error)
      }
    }

    // 私信：个人主页场景只允许用户→用户私聊
    const handlePrivateMessage = () => {
      if (isOwnProfile.value) return
      const targetId = userInfo.value.id
      if (!targetId) {
        console.warn("无法获取目标用户ID，暂时无法发起私信")
        return
      }
      // 跳转到消息页，由 ChatPage 按 userId=对方ID 创建/恢复 private 会话
      router.push({
        path: "/message/chat",
        query: { userId: String(targetId) }
      })
    }
    
    // 跳转到店铺
    const goToShop = () => {
      if (userInfo.value.shopId) {
        router.push(`/shop/${userInfo.value.shopId}`)
      }
    }
    
    // 加载用户数据
    const loadUserData = async () => {
      try {
        let targetUserId = userId.value

        // 当前用户主页：优先使用当前登录用户ID
        if (targetUserId === 'current' || !targetUserId) {
          // 如果全局用户信息还没初始化，先从后端拉一次
          if (!userStore.userInfo) {
            await userStore.getUserInfo()
          }
          targetUserId = userStore.userInfo?.id || userStore.userInfo?.userId
        }

        if (!targetUserId) {
          console.warn('加载用户数据失败：无法获取目标用户ID')
          return
        }
        
        // 先加载主页基础信息（用于判断 profileVisible），再决定是否需要请求统计接口
        await messageStore.fetchUserProfile(targetUserId)
        
        const isSelf = currentUserId.value && String(targetUserId) === String(currentUserId.value)
        const profileVisible = messageStore.userProfile?.profileVisible !== false
        
        // 仅在本人或对方允许查看时才请求统计接口；否则直接置零并不发请求
        if (isSelf || profileVisible) {
          await messageStore.fetchUserStatistics(targetUserId)
        } else {
          messageStore.userStatistics = {
            postCount: 0,
            likeCount: 0,
            favoriteCount: 0,
            followingCount: 0,
            followerCount: 0,
            commentCount: 0
          }
        }
      } catch (error) {
        console.error('加载用户数据失败：', error)
      }
    }
    
    // 格式化日期
    const formatDate = date => {
      if (!date) return ''
      const d = new Date(date)
      return `${d.getFullYear()}-${(d.getMonth() + 1).toString().padStart(2, '0')}-${d.getDate().toString().padStart(2, '0')}`
    }

// 默认图片
const defaultImage = ''
</script>

<style lang="scss" scoped>
.user-home-page {
  padding: 0 0 40px 0;
  
  .container {
    width: 85%;
    max-width: 1920px;
    margin: 0 auto;
    padding: 0;
  }
  
  // 页面标题横条（100%宽度）
  .page-title-bar {
    width: 100%;
    background: linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%);
    padding: 12px 0;
    margin-top: 0;
    margin-bottom: 16px;
    border-bottom: 2px solid #e4e7ed;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
    
    .page-title-container {
      width: 85%;
      max-width: 1920px;
      margin: 0 auto;
      padding: 0;
    }
    
    .page-title-content {
      display: flex;
      align-items: center;
      gap: 20px;
      
      .back-button {
        display: flex;
        align-items: center;
        gap: 6px;
        padding: 8px 16px;
        color: var(--el-text-color-regular);
        font-size: 14px;
        transition: all 0.3s;
        
        &:hover {
          color: var(--el-color-primary);
          background-color: rgba(64, 158, 255, 0.1);
        }
        
        .el-icon {
          font-size: 16px;
        }
      }
      
      .title-wrapper {
        display: flex;
        align-items: center;
        gap: 16px;
        flex: 1;
        
        .title-decoration {
          display: flex;
          align-items: center;
          position: relative;
          
          &::before {
            content: '';
            width: 6px;
            height: 32px;
            background: linear-gradient(180deg, var(--el-color-primary) 0%, rgba(64, 158, 255, 0.6) 100%);
            border-radius: 3px;
            margin-right: 12px;
            flex-shrink: 0;
          }
          
          .decoration-text {
            font-size: 14px;
            color: var(--el-color-primary);
            font-weight: 500;
            letter-spacing: 1px;
            opacity: 0.8;
          }
        }
        
        .page-title-text {
          margin: 0;
          font-size: 22px;
          font-weight: 500;
          color: var(--el-text-color-primary);
          letter-spacing: 0.5px;
          flex: 1;
        }
      }
    }
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
          /* 外层：120x140 透视框区域（整体高度） */
          width: 120px;
          height: 140px;
          margin-right: 30px;
          position: relative;
        }

        .avatar-pop-wrapper {
          position: relative;
          width: 120px;
          height: 140px;
        }

        /* 底层：120x120 的圆头像，贴在透视框底部 */
        .avatar-circle {
          position: absolute;
          left: 0;
          bottom: 0;
          width: 120px;
          height: 120px;
          border-radius: 50%;
          overflow: hidden;
          /* 移除可见边框，仅保留柔和阴影增强层次感 */
          border: none;
          box-shadow:
            0 2px 4px rgba(0, 0, 0, 0.10),
            0 6px 12px rgba(0, 0, 0, 0.14);
          /* 彻底当作“圆形剪裁框”使用，本身不再上底色，避免看起来是方框 */
          background: transparent;
        }

        .avatar-base {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }

        .avatar-pop {
          position: absolute;
          left: 0;
          top: 0;
          width: 120px;
          height: 140px;
          object-fit: cover;
          pointer-events: none;
          /* 去掉外框的 drop-shadow，阴影只加在内框 */
          /* 用和预览一样的 border-radius 方法：上半部分椭圆弧线 */
          border-top-left-radius: 60px 60px;
          border-top-right-radius: 60px 60px;
          /* 下半部分保持标准圆：通过 border-bottom-left-radius 和 border-bottom-right-radius */
          border-bottom-left-radius: 60px;
          border-bottom-right-radius: 60px;
          overflow: hidden;
        }
        
        /* 弧线预览：用纯 CSS border-radius 画一条可见的弧线 */
        .arc-preview {
          width: 120px;
          height: 80px;
          background: rgba(255, 0, 0, 0.2);
          border: 2px solid rgba(255, 0, 0, 0.8);
          margin-left: 30px;
          margin-top: 20px;
          position: relative;
          /* 用 border-radius 画一个上半椭圆：上边是弧形，下边是直线 */
          border-top-left-radius: 60px 100px;
          border-top-right-radius: 60px 80px;
          border-bottom-left-radius: 0;
          border-bottom-right-radius: 0;
          overflow: hidden;
        }
        
        /* 用伪元素画一条更精确的弧线 */
        .arc-preview::before {
          content: '';
          position: absolute;
          top: 0;
          left: 0;
          width: 120px;
          height: 80px;
          background: transparent;
          border-top: 3px solid rgba(255, 0, 0, 1);
          border-top-left-radius: 60px 80px;
          border-top-right-radius: 60px 80px;
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
            margin-bottom: 8px;
          }
          
          .user-location {
            display: flex;
            align-items: center;
            gap: 4px;
            color: #909399;
            font-size: 13px;
            margin-bottom: 10px;
            
            .el-icon {
              font-size: 14px;
            }
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
        margin-right: 24px;
        display: flex;
        gap: 12px;

        .private-message-btn {
          width: 95px;
          height: 30px;
          padding: 0;
          border-radius: 8px;
          border: 1px solid #9fd7a8;
          background-color: #e4f7e8;
          color: #2c8f4a;
          font-size: 15px;
          font-weight: 500;

          &:hover {
            border-color: #84cb92;
            background-color: #d8f2de;
            color: #237a3d;
          }
        }

        .follow-btn {
          width: 95px;
          height: 30px;
          padding: 0;
          border-radius: 8px;
          border: 1px solid #f56c6c;
          background-color: #f56c6c;
          color: #fff;
          font-size: 15px;
          font-weight: 600;

          &:hover {
            background-color: #f26363;
            border-color: #f26363;
            color: #fff;
          }
        }

        .follow-btn.is-following {
          background-color: #f78989;
          border-color: #f78989;
          color: #fff;
        }
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