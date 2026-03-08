<template>
  <div class="my-posts-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="container">
        <h1 class="page-title">我的帖子</h1>
        <p class="page-description">管理您发布的所有帖子</p>
      </div>
    </div>

    <div class="container main-content">
      <div class="main-posts">
        <!-- 操作栏 -->
        <div class="posts-header">
          <div class="header-actions">
            <el-button type="primary" plain size="small" @click="refreshPosts" :loading="loading">
              <el-icon><Refresh /></el-icon> 刷新
            </el-button>
          </div>
        </div>
        
        <!-- 帖子卡片列表 -->
        <div class="posts-container">
          <post-card 
            v-for="post in postList" 
            :key="post.id" 
            :post="post"
            :show-status="true"
            :show-delete="true"
            :show-edit="true"
            @reply="handleReply" 
            @like="handleLike"
            @favorite="handleFavorite"
            @edit="handleEdit"
            @delete="handleDelete"
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
          <el-empty v-if="postList.length === 0 && !loading" description="您还没有发表过帖子" />
        </div>
      </div>
    </div>

    <!-- 编辑帖子弹窗 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑帖子"
      width="600px"
      destroy-on-close
    >
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="editForm.title" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="摘要">
          <el-input
            v-model="editForm.summary"
            type="textarea"
            :rows="3"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="内容">
          <el-input
            v-model="editForm.content"
            type="textarea"
            :rows="6"
            maxlength="10000"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取 消</el-button>
          <el-button type="primary" :loading="editLoading" @click="submitEdit">
            保 存
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useForumStore } from '@/stores/forum'
import { useUserStore } from '@/stores/user'
import { Refresh } from '@element-plus/icons-vue'
import PostCard from '@/components/forum/PostCard.vue'
import { showByCode } from '@/utils/apiMessages'
import { ElMessageBox } from 'element-plus'

const router = useRouter()
const forumStore = useForumStore()
const userStore = useUserStore()

// 分页数据
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 从Pinia获取数据
const postList = computed(() => forumStore.forumPosts)
const loading = computed(() => forumStore.loading)
const paginationData = computed(() => forumStore.postPagination)
const currentUser = computed(() => userStore.userInfo)

// 编辑帖子弹窗状态
const editDialogVisible = ref(false)
const editingPostId = ref(null)
const editForm = ref({
  title: '',
  content: '',
  summary: ''
})
const editLoading = ref(false)

// 更新本地分页状态
const updatePagination = () => {
  pagination.currentPage = paginationData.value.current
  pagination.pageSize = paginationData.value.pageSize
  pagination.total = paginationData.value.total
}

// 获取我的帖子列表
const fetchPosts = async () => {
  if (!currentUser.value?.id) {
    console.warn('用户未登录')
    return
  }
  try {
    const params = {
      page: pagination.currentPage,
      pageSize: pagination.pageSize,
      userId: currentUser.value.id
    }
    const res = await forumStore.fetchForumPosts(params)
    showByCode(res.code)
    updatePagination()
  } catch (error) {
    console.error('获取我的帖子失败:', error)
  }
}

// 刷新帖子列表
const refreshPosts = () => {
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

// 查看帖子详情
const viewPost = postId => {
  router.push(`/forum/${postId}`)
}

// 帖子回复
const handleReply = post => {
  router.push(`/forum/${post.id}#reply-section`)
}

// 帖子点赞
const handleLike = async post => {
  try {
    if (post.isLiked) {
      const res = await forumStore.removeLike({
        targetId: String(post.id),
        targetType: 'post'
      })
      showByCode(res.code)
      await fetchPosts()
    } else {
      const res = await forumStore.addLike({
        targetId: String(post.id),
        targetType: 'post'
      })
      showByCode(res.code)
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
      const res = await forumStore.removeFavorite({
        itemId: String(post.id),
        itemType: 'post'
      })
      showByCode(res.code)
      await fetchPosts()
    } else {
      const res = await forumStore.addFavorite({
        itemId: String(post.id),
        itemType: 'post',
        targetName: post.title || '',
        targetImage: post.coverImage || ''
      })
      showByCode(res.code)
      await fetchPosts()
    }
  } catch (error) {
    console.error('收藏操作失败:', error)
  }
}

// 编辑帖子：复用“个人主页 / 我的发布”那套弹窗编辑逻辑
const handleEdit = post => {
  if (!post?.id) return
  editingPostId.value = post.id
  editForm.value = {
    title: post.title || '',
    content: post.content || '',
    summary: post.summary || ''
  }
  editDialogVisible.value = true
}

const submitEdit = async () => {
  if (!editingPostId.value) return
  try {
    editLoading.value = true
    const payload = {
      title: editForm.value.title,
      content: editForm.value.content,
      summary: editForm.value.summary
    }
    const res = await forumStore.updatePost(editingPostId.value, payload)
    showByCode(res.code)
    editDialogVisible.value = false
    await fetchPosts()
  } catch (error) {
    console.error('更新帖子失败:', error)
  } finally {
    editLoading.value = false
  }
}

// 删除帖子
const handleDelete = async post => {
  if (!post?.id) return
  try {
    await ElMessageBox.confirm(
      '确定要删除该帖子吗？删除后将无法恢复。',
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    const res = await forumStore.deletePost(post.id)
    showByCode(res.code)
    await fetchPosts()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      console.error('删除帖子失败:', error)
    }
  }
}

// 页面初始化
onMounted(async () => {
  await fetchPosts()
})
</script>

<style lang="scss" scoped>
.my-posts-page {
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
    
    .header-actions {
      display: flex;
      gap: 10px;
      align-items: center;
    }
  }
  
  .posts-container {
    padding: 15px;
  }
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
