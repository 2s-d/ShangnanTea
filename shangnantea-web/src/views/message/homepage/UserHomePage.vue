<template>
  <div class="user-home-page">
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
                {{ userInfo.nickname || userInfo.username || '用户' }}
                <span class="user-gender">
                  <el-icon color="#409EFF" v-if="userInfo.gender === 1"><Male /></el-icon>
                  <el-icon color="#FF4949" v-else-if="userInfo.gender === 2"><Female /></el-icon>
                  <el-tooltip v-else content="性别未填写/后端未返回gender字段" placement="top">
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
              <div class="user-id">ID: {{ userInfo.id || userInfo.username }}</div>
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
            <!-- 查看他人主页时显示关注按钮 -->
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
              <span>动态</span>
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
          
        <!-- 下半部分：内容区域 -->
        <div class="home-content">
          <!-- 用户动态 -->
          <div v-if="activeMenu === 'dynamic'" class="dynamic-content">
            <div class="dynamic-section">
              <h3>最近发布</h3>
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
              <h3>最近评论</h3>
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
          
          <!-- 其他标签页内容 -->
          <keep-alive v-else>
            <component :is="currentComponent" />
          </keep-alive>
          
          <!-- 开发中的功能提示 -->
          <template v-if="!hasComponent && activeMenu !== 'dynamic'">
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
  User, UserFilled, Star, Document, Male, Female, Edit, Plus, Shop, Clock, ChatDotRound, Key, QuestionFilled, Location 
} from '@element-plus/icons-vue'
import { formatLocationDisplay } from '@/utils/region'
import SafeImage from '@/components/common/form/SafeImage.vue'
import { showByCode } from '@/utils/apiMessages'
import { commonPromptMessages } from '@/utils/promptMessages'

const router = useRouter()
const route = useRoute()
const messageStore = useMessageStore()
const userStore = useUserStore()
    
    // 从路由参数获取用户ID，如果没有则使用当前登录用户ID
    const userId = computed(() => route.params.userId || 'current')
    
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
    
    // 从Pinia获取用户动态
    const userDynamic = computed(() => messageStore.userDynamic || {
      recentPosts: [],
      recentComments: []
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
    
    // 判断是否是查看自己的主页
    const isOwnProfile = computed(() => {
      return userId.value === 'current' || !route.params.userId
    })
    
    // 关注状态（从接口返回的isFollowed字段获取）
    const isFollowing = computed(() => userInfo.value?.isFollowed || false)
    
    // 菜单项和对应组件映射
    const menuOptions = {
      dynamic: '动态',
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
    
    // 活动菜单
    const activeMenu = ref('dynamic')
    // 当前显示的组件
    const currentComponent = ref(componentMap.published)
    
    // 判断当前菜单是否有对应组件
    const hasComponent = computed(() => {
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
    watch(() => route.params.tab, newTab => {
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
    const handleMenuSelect = key => {
      activeMenu.value = key
      
      // 如果存在对应组件就设置，否则显示动态或开发中
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
            targetName: userInfo.value.nickname || userInfo.value.username,
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
        
        // 并行加载用户信息、动态和统计数据
        await Promise.all([
          messageStore.fetchUserProfile(targetUserId),
          messageStore.fetchUserDynamic(targetUserId),
          messageStore.fetchUserStatistics(targetUserId)
        ])
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
  padding: 40px 0;
  
  .container {
    width: 85%;
    max-width: 1920px;
    margin: 0 auto;
    padding: 0;
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