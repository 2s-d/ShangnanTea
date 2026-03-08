<template>
  <div class="forum-list-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="container">
        <h1 class="page-title">茶友论坛</h1>
        <p class="page-description">茶友交流的专业平台，分享茶文化与品茶心得</p>
      </div>
    </div>

    <div class="container main-content">
      <el-row :gutter="20">
        <!-- 左侧帖子列表 -->
        <el-col :xs="24" :sm="18" :md="16" :lg="17">
          <div class="main-posts">
            <!-- 搜索框、排序、刷新和发帖按钮 -->
            <div class="posts-header">
              <div class="search-section">
                <el-input
                  v-model="searchKeyword"
                  placeholder="搜索帖子标题或内容..."
                  clearable
                  @keyup.enter="handleSearch"
                  @clear="handleSearch"
                  class="search-input"
                >
                  <template #prefix>
                    <el-icon><Search /></el-icon>
                  </template>
                  <template #append>
                    <el-button @click="handleSearch">搜索</el-button>
                  </template>
                </el-input>
              </div>
              <div class="header-actions">
                <el-dropdown trigger="click" @command="handleSortChange">
                  <span class="sort-dropdown">
                    {{ sortOptions[currentSort] || '最新发布' }} <el-icon><ArrowDown /></el-icon>
                  </span>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item v-for="(label, value) in sortOptions" :key="value" :command="value">
                        {{ label }}
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
                
                <el-button type="primary" plain size="small" @click="refreshPosts" :loading="loading">
                  <el-icon><Refresh /></el-icon> 刷新
                </el-button>
                
                <el-button type="primary" size="small" @click="showPostDialog">
                  <el-icon><EditPen /></el-icon> 发表新帖
                </el-button>
              </div>
            </div>
            
            <!-- 帖子卡片列表 -->
            <div class="posts-container">
              <post-card 
                v-for="post in postList" 
                :key="post.id" 
                :post="post"
                @reply="handleReply" 
                @like="handleLike"
                @favorite="handleFavorite"
              />
              
              <!-- 分页 -->
              <div class="pagination-container" v-if="postList.length > 0">
                <el-pagination
                  v-model:current-page="pagination.currentPage"
                  v-model:page-size="pagination.pageSize"
                  :page-sizes="[10, 20, 30, 50]"
                  layout="total, sizes, prev, pager, next, jumper"
                  :total="pagination.total"
                  @size-change="handleSizeChange"
                  @current-change="handleCurrentChange"
                />
              </div>
              
              <!-- 无数据提示 -->
              <el-empty v-if="postList.length === 0" description="暂无帖子数据" />
            </div>
          </div>
        </el-col>
        
        <!-- 右侧用户信息和版块导航 -->
        <el-col :xs="24" :sm="6" :md="8" :lg="7">
          <!-- 简化的用户信息卡片 -->
          <div class="sidebar user-sidebar">
            <div class="user-info-card">
              <div class="user-info" @click="goToMyPosts">
                <div class="avatar">
                  <SafeImage :src="currentUser.avatar" type="avatar" :alt="currentUser.username" style="width:50px;height:50px;border-radius:50%;object-fit:cover;" />
                </div>
                <div class="info">
                  <div class="name">{{ currentUser.username }}</div>
                  <div class="my-posts-link">我的帖子</div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 版块导航 -->
          <div class="sidebar topics-sidebar">
            <div class="sidebar-header">
              <h3 class="sidebar-title">版块导航</h3>
            </div>
            <div class="topic-list">
              <div 
                class="topic-item" 
                :class="{ active: currentTopicId === 'all' }" 
                @click="switchTopic('all')"
              >
                <el-icon><Grid /></el-icon>
                <span>全部帖子</span>
                <span class="count">{{ pagination.total }}</span>
              </div>
              <div 
                v-for="topic in topicList" 
                :key="topic.id" 
                class="topic-item" 
                :class="{ active: currentTopicId === topic.id }"
                @click="switchTopic(topic.id)"
              >
                <SafeImage :src="topic.icon" type="banner" :alt="topic.name" class="topic-icon" style="width:16px;height:16px;margin-right:6px;" />
                <span>{{ topic.name }}</span>
                <span class="count">{{ topic.postCount }}</span>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
      
      <!-- 新建帖子对话框 -->
      <el-dialog
        title="发布新帖"
        v-model="dialogVisible.post"
        width="720px"
        :close-on-click-modal="false"
      >
        <el-form :model="postForm" :rules="postRules" ref="postFormRef" label-width="80px">
          <el-form-item label="标题" prop="title">
            <el-input 
              v-model="postForm.title" 
              placeholder="请输入帖子标题（5-100字）"
              maxlength="100"
              show-word-limit
            />
          </el-form-item>
          
          <el-form-item label="分类" prop="categoryId">
            <el-select v-model="postForm.categoryId" placeholder="请选择分类" style="width: 100%">
              <el-option 
                v-for="topic in topicList" 
                :key="topic.id" 
                :label="topic.name" 
                :value="topic.id"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="摘要">
            <el-input
              v-model="postForm.summary"
              type="textarea"
              :rows="3"
              maxlength="200"
              show-word-limit
              placeholder="可选：不填时将自动从正文前200字生成"
            />
          </el-form-item>
          
          <el-form-item label="内容" prop="content">
            <QuillEditor
              v-model:content="postForm.content"
              contentType="html"
              :options="postEditorOptions"
              style="height: 320px"
              @ready="onPostEditorReady"
            />
          </el-form-item>
          
          <el-form-item label="封面图">
            <el-upload
              class="cover-uploader"
              :show-file-list="false"
              :http-request="handleCoverUpload"
              accept="image/*"
            >
              <SafeImage
                v-if="postForm.coverImage"
                :src="postForm.coverImage"
                type="post"
                alt="封面图片"
                class="cover-image-preview"
              />
              <template v-else>
                <el-icon class="cover-uploader-icon"><Plus /></el-icon>
                <div class="cover-uploader-text">上传封面（可选）</div>
              </template>
            </el-upload>
            <div class="el-upload__tip">
              建议比例 16:9，用于列表和详情页顶部展示
            </div>
          </el-form-item>
        </el-form>
        
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="cancelPost">取消</el-button>
            <el-button type="primary" @click="submitPost" :loading="localLoading.submit">发布</el-button>
          </span>
        </template>
      </el-dialog>
      
      <!-- 删除帖子确认对话框 -->
      <el-dialog
        title="删除帖子"
        v-model="dialogVisible.delete"
        width="400px"
      >
        <p class="dialog-message">确定要删除这篇帖子吗？此操作不可恢复。</p>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="dialogVisible.delete = false">取消</el-button>
            <el-button type="danger" @click="deletePost" :loading="localLoading.delete">确认删除</el-button>
          </span>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
/* eslint-disable vue/no-ref-as-operand */
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useForumStore } from '@/stores/forum'
import { useUserStore } from '@/stores/user'

import { Refresh, ArrowDown, Grid, EditPen, Delete, Male, Female, Plus, Search } from '@element-plus/icons-vue'
import PostCard from '@/components/forum/PostCard.vue'
import SafeImage from '@/components/common/form/SafeImage.vue'
import { showByCode } from '@/utils/apiMessages'
import { forumPromptMessages } from '@/utils/promptMessages'
import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css'
import forumAPI from '@/api/forum'

const router = useRouter()
const route = useRoute()
const forumStore = useForumStore()
const userStore = useUserStore()

// 默认图片常量（生产形态：不使用 mock-images）
const defaultAvatar = ''
const defaultCover = ''
const defaultIcon = ''

// 当前选中的版块ID（'all'表示全部帖子）
const currentTopicId = ref('all')

// 搜索关键词
const searchKeyword = ref('')

// 当前用户信息（从userStore获取）
const currentUser = computed(() => {
  const user = userStore.userInfo
  return {
    id: user?.id || '',
    username: user?.username || '未登录',
    avatar: user?.avatar || '',
    gender: user?.gender || 0
  }
})

// 待删除的帖子
const postToDelete = ref(null)

// 本地加载状态
const localLoading = reactive({
  delete: false,
  submit: false
})

// 对话框状态
const dialogVisible = reactive({
  post: false,
  delete: false
})

// 发帖表单
const postFormRef = ref(null)
const postForm = reactive({
  title: '',
  categoryId: '',
  content: '',
  summary: '',
  coverImage: ''
})

// 发帖表单验证规则
const postRules = {
  title: [
    { required: true, message: '请输入帖子标题', trigger: 'blur' },
    { min: 5, max: 100, message: '标题长度在 5 到 100 个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入帖子内容', trigger: 'change' }
  ]
}

// Quill 编辑器配置（帖子）
const postEditorOptions = {
  modules: {
    toolbar: {
      container: [
        [{ header: [1, 2, 3, false] }],
        ['bold', 'italic', 'underline', 'strike'],
        [{ list: 'ordered' }, { list: 'bullet' }],
        [{ color: [] }, { background: [] }],
        [{ align: [] }],
        ['link', 'image'],
        ['clean']
      ],
      handlers: {
        image: imageHandler
      }
    }
  },
  placeholder: '请输入帖子内容，可在正文中插入图片…',
  theme: 'snow'
}

let quillInstance = null

const onPostEditorReady = quill => {
  quillInstance = quill
}

function imageHandler() {
  const input = document.createElement('input')
  input.setAttribute('type', 'file')
  input.setAttribute('accept', 'image/*')
  input.click()

  input.onchange = async () => {
    const file = input.files[0]
    if (!file) return
    if (!quillInstance) {
      forumPromptMessages.showCommonError('编辑器未准备好，请稍后重试')
      return
    }
    try {
      const res = await forumAPI.uploadPostImage(file)
      if (res?.code === 6037 && res.data?.url) {
        const url = res.data.url
        const range = quillInstance.getSelection(true)
        const index = range && range.index != null ? range.index : quillInstance.getLength() - 1
        quillInstance.insertEmbed(index, 'image', url)
        quillInstance.setSelection(index + 1)
        quillInstance.update()
      } else {
        forumPromptMessages.showCommonError('图片上传失败')
      }
    } catch (e) {
      console.error('上传帖子图片失败:', e)
      forumPromptMessages.showCommonError('图片上传失败')
    }
  }
}

// 封面上传
const coverUploading = ref(false)

const handleCoverUpload = async ({ file }) => {
  try {
    coverUploading.value = true
    const res = await forumAPI.uploadPostImage(file)
    if (res?.code === 6037 && res.data?.url) {
      postForm.coverImage = res.data.url
    } else {
      forumPromptMessages.showCommonError('封面上传失败')
    }
  } catch (e) {
    console.error('封面上传失败:', e)
    forumPromptMessages.showCommonError('封面上传失败')
  } finally {
    coverUploading.value = false
  }
}

// 排序选项
const currentSort = ref('latest')
const sortOptions = {
  latest: '最新发布',
  popular: '最多浏览',
  hot: '热门讨论'
}

// 分页数据
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 从Pinia获取数据
const topicList = computed(() => forumStore.forumTopics)
const postList = computed(() => forumStore.forumPosts)
const loading = computed(() => forumStore.loading)
const error = computed(() => forumStore.error)

// 分页数据从Pinia获取
const paginationData = computed(() => forumStore.postPagination)

// 我的帖子（暂时使用空数组，后续可以添加相关API）
const myPosts = ref([])
    
// 更新本地分页状态
const updatePagination = () => {
  pagination.currentPage = paginationData.value.current
  pagination.pageSize = paginationData.value.pageSize
  pagination.total = paginationData.value.total
}

    /*
    // 模拟数据 - 版块列表
    const topicListMock = ref([
      {
        id: 1,
        name: '茶叶知识',
        description: '分享茶叶相关知识、冲泡技巧等',
        icon: 'https://via.placeholder.com/50x50?text=知识',
        cover: 'https://via.placeholder.com/400x200?text=茶叶知识',
        postCount: 128,
        viewCount: 3426
      },
      {
        id: 2,
        name: '茶友交流',
        description: '茶友们的交流互动平台',
        icon: 'https://via.placeholder.com/50x50?text=交流',
        cover: 'https://via.placeholder.com/400x200?text=茶友交流',
        postCount: 95,
        viewCount: 2187
      },
      {
        id: 3,
        name: '茶具讨论',
        description: '探讨各类茶具的选购与使用',
        icon: 'https://via.placeholder.com/50x50?text=茶具',
        cover: 'https://via.placeholder.com/400x200?text=茶具讨论',
        postCount: 64,
        viewCount: 1543
      },
      {
        id: 4,
        name: '茶文化探索',
        description: '深入了解中国传统茶文化',
        icon: 'https://via.placeholder.com/50x50?text=文化',
        cover: 'https://via.placeholder.com/400x200?text=茶文化探索',
        postCount: 76,
        viewCount: 1892
      },
      {
        id: 5,
        name: '茶园风采',
        description: '分享各地茶园风光与采茶经验',
        icon: 'https://via.placeholder.com/50x50?text=茶园',
        cover: 'https://via.placeholder.com/400x200?text=茶园风采',
        postCount: 42,
        viewCount: 980
      },
      {
        id: 6,
        name: '品鉴交流',
        description: '茶叶品鉴心得与体验分享',
        icon: 'https://via.placeholder.com/50x50?text=品鉴',
        cover: 'https://via.placeholder.com/400x200?text=品鉴交流',
        postCount: 58,
        viewCount: 1256
      }
    ])
    
    // 模拟数据 - 帖子列表
    const postListMock = ref([
      {
        id: 1,
        title: '如何正确冲泡绿茶？',
        summary: '绿茶的冲泡温度一般控制在80℃左右较为适宜，水温过高会破坏茶叶中的营养物质，使茶汤变苦涩...',
        content: '绿茶的冲泡温度一般控制在80℃左右较为适宜，水温过高会破坏茶叶中的营养物质，使茶汤变苦涩...(此处省略详细内容)',
        topicId: 1,
        topicName: '茶叶知识',
        authorId: 'cy100002',
        authorName: '茶韵悠长',
        authorAvatar: 'https://via.placeholder.com/40x40?text=茶韵',
        authorGender: 1,
        coverImage: 'https://via.placeholder.com/200x120?text=绿茶冲泡',
        isSticky: 1,
        isEssence: 1,
        viewCount: 358,
        replyCount: 42,
        likeCount: 86,
        createTime: '2025-03-16 09:30:00'
      },
      {
        id: 2,
        title: '分享：商南茶的特有风味与鉴别方法',
        summary: '商南茶作为陕西名茶，具有独特的风味特征，本文将详细介绍如何通过外形、香气和滋味来鉴别正宗商南茶...',
        topicId: 1,
        topicName: '茶叶知识',
        authorId: 'cy100003',
        authorName: '茶香四溢',
        authorAvatar: 'https://via.placeholder.com/40x40?text=茶香',
        authorGender: 2,
        coverImage: null,
        isSticky: 0,
        isEssence: 1,
        viewCount: 246,
        replyCount: 28,
        likeCount: 54,
        createTime: '2025-03-15 14:25:00'
      },
      {
        id: 3,
        title: '我收藏的陶瓷茶具展示',
        summary: '多年来收藏了不少精美的陶瓷茶具，今天跟大家分享一下我的收藏心得和使用体验...',
        topicId: 3,
        topicName: '茶具讨论',
        authorId: 'cy100004',
        authorName: '紫砂爱好者',
        authorAvatar: 'https://via.placeholder.com/40x40?text=紫砂',
        authorGender: 1,
        coverImage: 'https://via.placeholder.com/200x120?text=陶瓷茶具',
        isSticky: 0,
        isEssence: 1,
        viewCount: 189,
        replyCount: 35,
        likeCount: 47,
        createTime: '2025-03-14 16:40:00'
      },
      {
        id: 4,
        title: '春茶采摘季：商南茶园实地探访',
        summary: '刚刚参加了商南茶园的春茶采摘活动，分享一下现场照片和采茶过程中的所见所闻...',
        topicId: 5,
        topicName: '茶园风采',
        authorId: 'cy100005',
        authorName: '采茶姑娘',
        authorAvatar: 'https://via.placeholder.com/40x40?text=采茶',
        authorGender: 2,
        coverImage: 'https://via.placeholder.com/200x120?text=春茶采摘',
        isSticky: 1,
        isEssence: 0,
        viewCount: 276,
        replyCount: 31,
        likeCount: 64,
        createTime: '2025-03-13 10:15:00'
      },
      {
        id: 5,
        title: '茶文化中的礼仪与传统',
        summary: '中国茶文化源远流长，其中蕴含了丰富的礼仪和传统，本文将介绍茶道中的基本礼仪和文化内涵...',
        topicId: 4,
        topicName: '茶文化探索',
        authorId: 'cy100006',
        authorName: '茶道传人',
        authorAvatar: 'https://via.placeholder.com/40x40?text=茶道',
        authorGender: 1,
        coverImage: 'https://via.placeholder.com/200x120?text=茶道礼仪',
        isSticky: 0,
        isEssence: 1,
        viewCount: 205,
        replyCount: 26,
        likeCount: 43,
        createTime: '2025-03-12 15:30:00'
      },
      {
        id: 6,
        title: '【新人报到】初识商南茶，请多指教',
        summary: '作为一个茶叶新手，刚接触商南茶不久，希望能在这里学习更多关于茶叶的知识，请各位前辈多多指教...',
        topicId: 2,
        topicName: '茶友交流',
        authorId: 'cy100007',
        authorName: '茶叶新手',
        authorAvatar: 'https://via.placeholder.com/40x40?text=新手',
        authorGender: 1,
        coverImage: null,
        isSticky: 0,
        isEssence: 0,
        viewCount: 156,
        replyCount: 40,
        likeCount: 28,
        createTime: '2025-03-11 09:45:00'
      },
      {
        id: 7,
        title: '品鉴会活动：商南毛尖VS信阳毛尖',
        summary: '上周参加了一场商南毛尖与信阳毛尖的品鉴对比活动，分享一下两种茶叶的风味差异和个人体验...',
        topicId: 6,
        topicName: '品鉴交流',
        authorId: 'cy100008',
        authorName: '茶香品鉴师',
        authorAvatar: 'https://via.placeholder.com/40x40?text=品鉴',
        authorGender: 1,
        coverImage: 'https://via.placeholder.com/200x120?text=茶叶品鉴',
        isSticky: 0,
        isEssence: 0,
        viewCount: 178,
        replyCount: 22,
        likeCount: 37,
        createTime: '2025-03-10 14:20:00'
      },
      {
        id: 8,
        title: '宜兴紫砂壶的保养与使用技巧',
        summary: '紫砂壶是众多茶友钟爱的茶具，正确的使用和保养可以让紫砂壶越用越好，本文分享个人多年养壶心得...',
        topicId: 3,
        topicName: '茶具讨论',
        authorId: 'cy100009',
        authorName: '紫砂匠人',
        authorAvatar: 'https://via.placeholder.com/40x40?text=匠人',
        authorGender: 1,
        coverImage: 'https://via.placeholder.com/200x120?text=紫砂壶',
        isSticky: 0,
        isEssence: 0,
        viewCount: 198,
        replyCount: 25,
        likeCount: 41,
        createTime: '2025-03-09 11:35:00'
      },
      {
        id: 9,
        title: '茶叶存储的正确方法与常见误区',
        summary: '茶叶的保存直接影响其品质，本文介绍不同茶类的存储方法，以及日常存茶过程中应该避免的误区...',
        topicId: 1,
        topicName: '茶叶知识',
        authorId: 'cy100010',
        authorName: '茶叶保鲜师',
        authorAvatar: 'https://via.placeholder.com/40x40?text=保鲜',
        authorGender: 1,
        coverImage: null,
        isSticky: 0,
        isEssence: 0,
        viewCount: 167,
        replyCount: 19,
        likeCount: 32,
        createTime: '2025-03-08 13:50:00'
      },
      {
        id: 10,
        title: '【讨论】你最喜欢什么类型的茶？',
        summary: '每个人的口味不同，喜欢的茶叶类型也各异，欢迎在此分享你最喜爱的茶类和理由，交流品茶心得...',
        topicId: 2,
        topicName: '茶友交流',
        authorId: 'cy100011',
        authorName: '茶香满园',
        authorAvatar: 'https://via.placeholder.com/40x40?text=茶香',
        authorGender: 2,
        coverImage: 'https://via.placeholder.com/200x120?text=茶类讨论',
        isSticky: 0,
        isEssence: 0,
        viewCount: 224,
        replyCount: 58,
        likeCount: 46,
        createTime: '2025-03-07 16:15:00'
      }
    ])
    
    // 模拟 - 我的帖子
    const myPostsMock = ref([
      {
        id: 1,
        title: '如何正确冲泡绿茶？',
        summary: '绿茶的冲泡温度一般控制在80℃左右较为适宜，水温过高会破坏茶叶中的营养物质...',
        topicId: 1,
        createTime: '2025-03-16 09:30:00'
      },
      {
        id: 12,
        title: '商南茶的采摘与制作过程分享',
        summary: '上个月有幸参观了商南茶的采摘与制作过程，分享一些见闻和照片...',
        topicId: 5,
        createTime: '2025-03-02 14:20:00'
      },
      {
        id: 15,
        title: '茶友交流活动：线下品茶会邀请',
        summary: '计划在下个月组织一次线下品茶交流活动，欢迎各位茶友参加...',
        topicId: 2,
        createTime: '2025-02-25 16:45:00'
      }
    ])
    
    pagination.total = 65 // 模拟总帖子数
    */
    
    /*
    // 真实代码(开发UI时注释)
    const topicList = ref([])
    const postList = ref([])
    const myPosts = ref([])
    const currentUser = ref({})
    
    // 获取当前用户信息
    const fetchCurrentUser = async () => {
      try {
        const result = await userStore.getUserInfo()
        currentUser.value = result
      } catch (error) {
        console.error('获取用户信息失败', error)
      }
    }
    
    // 获取我的帖子
    const fetchMyPosts = async () => {
      try {
        const result = await forumStore.getMyPosts({ limit: 3 })
        myPosts.value = result
      } catch (error) {
        console.error('获取我的帖子失败', error)
      }
    }
    
    // 获取版块列表
    const fetchTopics = async () => {
      try {
        loading.topics = true
        const result = await forumStore.getTopics()
        topicList.value = result
      } catch (error) {
        message.error('获取版块列表失败')
      } finally {
        loading.topics = false
      }
    }
    
    // 获取帖子列表
    const fetchPosts = async () => {
      try {
        loading.posts = true
        const params = {
          page: pagination.currentPage,
          limit: pagination.pageSize,
          sort: currentSort.value,
          topicId: currentTopicId.value === 'all' ? null : currentTopicId.value
        }
        const result = await forumStore.getPosts(params)
        postList.value = result.list
        pagination.total = result.total
      } catch (error) {
        message.error('获取帖子列表失败')
      } finally {
        loading.posts = false
      }
    }
    
    // 初始化数据
    onMounted(() => {
      fetchCurrentUser()
      fetchMyPosts()
      fetchTopics()
      fetchPosts()
    })
    */
    
    // 获取版块名称
    const getTopicName = topicId => {
      const topic = topicList.value.find(item => item.id === topicId)
      return topic ? topic.name : '未知版块'
    }
    
    // 获取当前版块名称
    const getCurrentTopicName = () => {
      if (currentTopicId.value === 'all') {
        return '全部帖子'
      }
      return getTopicName(currentTopicId.value)
    }
    
    // 获取当前版块描述
    const getCurrentTopicDesc = () => {
      if (currentTopicId.value === 'all') {
        return ''
      }
      const topic = topicList.value.find(item => item.id === currentTopicId.value)
      return topic ? topic.description : ''
    }
    
    // 获取版块列表
    const fetchTopics = async () => {
      try {
        const res = await forumStore.fetchForumTopics()
        showByCode(res.code)
      } catch (error) {
        console.error('获取版块列表失败:', error)
      }
    }
    
    // 获取帖子列表
    const fetchPosts = async () => {
      try {
        const params = {
          page: pagination.currentPage,
          size: pagination.pageSize,
          sortBy: currentSort.value
        }
        // 如果选中了版块且不是"全部"，传递topicId参数
        if (currentTopicId.value && currentTopicId.value !== 'all') {
          params.topicId = currentTopicId.value
        }
        // 如果有搜索关键词，传递keyword参数
        if (searchKeyword.value && searchKeyword.value.trim()) {
          params.keyword = searchKeyword.value.trim()
        }
        const res = await forumStore.fetchForumPosts(params)
        showByCode(res.code)
        updatePagination()
      } catch (error) {
        console.error('获取帖子列表失败:', error)
      }
    }
    
    // 处理搜索
    const handleSearch = () => {
      pagination.currentPage = 1
      fetchPosts()
    }
    
    // 切换版块
    const switchTopic = topicId => {
      currentTopicId.value = topicId
      // 切换版块时清空搜索关键词
      searchKeyword.value = ''
      pagination.currentPage = 1
      fetchPosts()
    }
    
    // 跳转到我的帖子页面
    const goToMyPosts = () => {
      router.push('/forum/my-posts')
    }
    
    // 刷新版块列表
    const refreshTopics = () => {
      fetchTopics()
    }
    
    // 刷新帖子列表
    const refreshPosts = () => {
      fetchPosts()
    }
    
    // 处理排序变更
    const handleSortChange = sort => {
      currentSort.value = sort
      pagination.currentPage = 1
      fetchPosts()
    }
    
    // 处理分页大小变更
    const handleSizeChange = size => {
      pagination.pageSize = size
      fetchPosts()
    }
    
    // 处理页码变更
    const handleCurrentChange = page => {
      pagination.currentPage = page
      fetchPosts()
    }
    
    // 查看版块
    const viewTopic = topicId => {
      router.push(`/forum/topic/${topicId}`)
    }
    
    // 查看帖子详情
    const viewPost = postId => {
      router.push(`/forum/${postId}`)
    }
    
    // 查看更多我的帖子
    const viewMoreMyPosts = () => {
      router.push('/user/settings/posts')
    }
    
// 显示发帖对话框
    const showPostDialog = () => {
      // 重置表单
      postForm.title = ''
      postForm.content = ''
      postForm.summary = ''
      postForm.coverImage = ''
      
      // 如果当前在具体版块，自动选中该版块；如果在"全部帖子"，则为空（需要用户手动选择）
      if (currentTopicId.value && currentTopicId.value !== 'all') {
        postForm.categoryId = currentTopicId.value
      } else {
        postForm.categoryId = ''
      }
      
      dialogVisible.post = true
    }
    
    // 从HTML中提取纯文本（用于自动生成摘要）
    const extractPlainText = html => {
      if (!html) return ''
      const div = document.createElement('div')
      div.innerHTML = html
      return div.textContent || div.innerText || ''
    }

    // 从HTML中提取图片URL列表（用于images字段）
    const extractImageUrls = html => {
      if (!html) return []
      const div = document.createElement('div')
      div.innerHTML = html
      const imgs = Array.from(div.querySelectorAll('img'))
      const urls = imgs
        .map(img => img.getAttribute('src'))
        .filter(u => !!u)
      // 去重
      return Array.from(new Set(urls))
    }

    // 取消发帖
    const cancelPost = () => {
      dialogVisible.post = false
      // 重置表单
      if (postFormRef.value) {
        postFormRef.value.resetFields()
      }
    }
    
    // 提交发帖
    const submitPost = async () => {
      if (!postFormRef.value) return
      
      // 验证表单
      const valid = await postFormRef.value.validate().catch(() => false)
      if (!valid) return
      
      localLoading.submit = true
      
      try {
        // 1) 从富文本中提取图片URL列表
        const imageUrls = extractImageUrls(postForm.content)

        // 2) 生成摘要（优先使用用户填写的摘要，否则从正文前200字自动生成）
        const plainText = extractPlainText(postForm.content)
        const autoSummary = plainText.slice(0, 200)

        // 3) 准备提交数据（后端 CreatePostDTO: topicId, title, content, summary, coverImage, images）
        const submitData = {
          title: postForm.title,
          topicId: postForm.categoryId ? parseInt(postForm.categoryId, 10) : null,
          content: postForm.content,
          summary: postForm.summary || autoSummary || null,
          coverImage: postForm.coverImage || (imageUrls.length > 0 ? imageUrls[0] : null),
          images: imageUrls.length > 0 ? JSON.stringify(imageUrls) : null
        }
        
        // 调用API
        const res = await forumStore.createPost(submitData)
        showByCode(res.code)
        
        // 提交成功（等待审核）
        if (res.code === 6011) {
          dialogVisible.post = false
          // 重置表单
          postFormRef.value.resetFields()
          postForm.images = []
          
          // 不需要刷新帖子列表（因为待审核的帖子不会显示在列表中）
        }
      } catch (error) {
        console.error('发布帖子失败:', error)
      } finally {
        localLoading.submit = false
      }
    }
    
    // 帖子回复
    const handleReply = post => {
      router.push(`/forum/${post.id}#reply-section`)
    }
    
    // 帖子点赞
    const handleLike = async post => {
      try {
        if (post.isLiked) {
          // 取消点赞：直接传递targetId和targetType
          const res = await forumStore.removeLike({
            targetId: String(post.id),
            targetType: 'post'
          })
          showByCode(res.code)
          // 重新加载帖子列表以更新isLiked状态
          await fetchPosts()
        } else {
          // 添加点赞
          const res = await forumStore.addLike({
            targetId: String(post.id),
            targetType: 'post'
          })
          showByCode(res.code)
          // 重新加载帖子列表以更新isLiked状态
          await fetchPosts()
        }
      } catch (error) {
        console.error('点赞操作失败:', error)
      }
    }
    
    // 帖子收藏
    const handleFavorite = async post => {
      try {
        if (post.isFavorited) {
          // 取消收藏：直接传递 itemId 和 itemType
          const res = await forumStore.removeFavorite({
            itemId: String(post.id),
            itemType: 'post'
          })
          showByCode(res.code)
          // 重新加载帖子列表以更新isFavorited状态
          await fetchPosts()
        } else {
          // 添加收藏
          const res = await forumStore.addFavorite({
            itemId: String(post.id),
            itemType: 'post',
            targetName: post.title || '',
            targetImage: post.coverImage || ''
          })
          showByCode(res.code)
          // 重新加载帖子列表以更新isFavorited状态
          await fetchPosts()
        }
      } catch (error) {
        console.error('收藏操作失败:', error)
      }
    }
    
    // 确认删除帖子
    const confirmDeletePost = post => {
      postToDelete.value = post
      dialogVisible.delete = true
    }
    
    // 删除帖子
    const deletePost = async () => {
      if (!postToDelete.value) return
      
      localLoading.delete = true
      
      try {
        const res = await forumStore.deletePost(postToDelete.value.id)
        // 从我的帖子列表中移除
        myPosts.value = myPosts.value.filter(item => item.id !== postToDelete.value.id)
        
        showByCode(res.code)
        dialogVisible.delete = false
        postToDelete.value = null
        
        // 刷新帖子列表
        fetchPosts()
      } catch (error) {
        console.error('删除帖子失败:', error)
      } finally {
        localLoading.delete = false
      }
    }
    
    // 格式化日期
    const formatDate = dateString => {
      const date = new Date(dateString)
      const now = new Date()
      const diff = now - date
      
      // 一小时内显示"刚刚"
      if (diff < 60 * 60 * 1000) {
        return '刚刚'
      }
      
      // 一天内显示"x小时前"
      if (diff < 24 * 60 * 1000) {
        const hours = Math.floor(diff / (60 * 60 * 1000))
        return `${hours}小时前`
      }
      
      // 一周内显示"x天前"
      if (diff < 7 * 24 * 60 * 60 * 1000) {
        const days = Math.floor(diff / (24 * 60 * 60 * 1000))
        return `${days}天前`
      }
      
      // 其他情况显示具体日期
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
    }
    
    // 回到首页
    const goHome = () => {
      router.push('/tea-culture')
    }
    
    // 页面初始化
    onMounted(async () => {
      await fetchTopics()
      // 如果有路由参数topicId，设置为当前版块
      const routeTopicId = route.params.id
      if (routeTopicId) {
        currentTopicId.value = parseInt(routeTopicId)
      }
      // 默认加载帖子列表（全部帖子）
      await fetchPosts()
    })
</script>

<style lang="scss" scoped>
.forum-list-page {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.container {
  width: 85%;
  max-width: 1920px;
  margin: 0 auto;
  padding: 0;
}

.page-header {
  background-color: #fff;
  padding: 30px 0;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  
  .page-title {
    font-size: 28px;
    font-weight: 600;
    margin: 0 0 10px;
    color: var(--el-text-color-primary);
  }
  
  .page-description {
    font-size: 16px;
    color: var(--el-text-color-secondary);
    margin-bottom: 0;
  }
}

.main-content {
  margin-bottom: 40px;
}

.cover-uploader {
  .cover-image-preview {
    width: 200px;
    max-height: 120px;
    border-radius: 6px;
    object-fit: cover;
  }
  .cover-uploader-icon {
    font-size: 28px;
    color: #8c939d;
  }
  .cover-uploader-text {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
  }
}

// 侧边栏样式
.sidebar {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  margin-bottom: 20px;
  overflow: hidden;
  
  .sidebar-header {
    padding: 15px;
    border-bottom: 1px solid #f0f0f0;
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .sidebar-title {
      font-size: 16px;
      font-weight: 500;
      margin: 0;
      color: var(--el-text-color-primary);
    }
  }
}

// 版块导航样式
.topics-sidebar {
  .topic-list {
    padding: 5px 0;
    
    .topic-item {
      display: flex;
      align-items: center;
      padding: 12px 15px;
      cursor: pointer;
      transition: background-color 0.2s;
      position: relative;
      
      &:hover {
        background-color: #f9f9f9;
      }
      
      &.active {
        background-color: var(--el-color-primary-light-9);
        color: var(--el-color-primary);
        font-weight: 500;
        
        &::before {
          content: '';
          position: absolute;
          left: 0;
          top: 0;
          bottom: 0;
          width: 3px;
          background-color: var(--el-color-primary);
        }
      }
      
      .topic-icon {
        width: 20px;
        height: 20px;
        border-radius: 4px;
        margin-right: 10px;
      }
      
      .el-icon {
        margin-right: 10px;
        font-size: 18px;
      }
      
      span {
        flex: 1;
      }
      
      .count {
        flex: 0;
        font-size: 12px;
        color: var(--el-text-color-secondary);
      }
    }
  }
}

// 用户信息侧边栏（简化版）
.user-sidebar {
  .user-info-card {
    padding: 20px;
    
    .user-info {
      display: flex;
      align-items: center;
      cursor: pointer;
      transition: background-color 0.2s;
      padding: 10px;
      border-radius: 8px;
      
      &:hover {
        background-color: #f5f7fa;
      }
      
      .avatar {
        width: 50px;
        height: 50px;
        border-radius: 50%;
        overflow: hidden;
        margin-right: 12px;
        flex-shrink: 0;
        
        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
      }
      
      .info {
        flex: 1;
        min-width: 0;
        
        .name {
          font-size: 16px;
          font-weight: 500;
          margin-bottom: 6px;
          color: var(--el-text-color-primary);
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }
        
        .my-posts-link {
          font-size: 13px;
          color: var(--el-color-primary);
          cursor: pointer;
          
          &:hover {
            text-decoration: underline;
          }
        }
      }
    }
  }
}

// 主帖子区域
.main-posts {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
    margin-bottom: 20px;
  overflow: hidden;
  
  .posts-header {
    padding: 15px;
    border-bottom: 1px solid #f0f0f0;
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 15px;
    
    .search-section {
      flex: 1;
      min-width: 300px;
      
      .search-input {
        max-width: 500px;
      }
    }
    
    .header-actions {
      display: flex;
      gap: 10px;
      align-items: center;
      flex-shrink: 0;
      
      .sort-dropdown {
        cursor: pointer;
        display: inline-flex;
        align-items: center;
        color: var(--el-text-color-regular);
        
        .el-icon {
          margin-left: 4px;
        }
      }
    }
  }
  
  .posts-container {
    padding: 15px;
  }
}

// 分页样式
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

// 对话框
.dialog-message {
  text-align: center;
  padding: 20px 0;
    font-size: 16px;
    color: var(--el-text-color-secondary);
}

// 响应式调整
@media (max-width: 992px) {
  .user-sidebar {
    margin-top: 20px;
  }
}

@media (max-width: 768px) {
  .page-header {
    padding: 20px 0;
    
    .page-title {
      font-size: 24px;
    }
  }
  
  .posts-header {
    flex-direction: column;
    align-items: flex-start;
    
    .topic-info {
      margin-right: 0;
      margin-bottom: 15px;
    }
  }
}

// 对话框样式
.dialog-message {
  text-align: center;
  color: var(--el-text-color-secondary);
  font-size: 14px;
  padding: 20px 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

:deep(.el-upload__tip) {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 5px;
}

:deep(.el-upload-list__item) {
  transition: all 0.3s;
}
</style> 