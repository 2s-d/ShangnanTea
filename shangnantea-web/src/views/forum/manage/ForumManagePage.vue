<template>
  <div class="forum-manage-page">
    <div class="page-header">
      <div class="container">
        <h1 class="page-title">论坛内容管理</h1>
        <div class="header-actions">
          <el-button type="primary" @click="goToCultureManage">
            <el-icon><Reading /></el-icon>
            茶文化内容管理
          </el-button>
        </div>
      </div>
    </div>

    <div class="container main-content">
      <el-tabs v-model="activeTab" class="management-tabs">
        <el-tab-pane label="版块管理" name="topics">
          <div class="management-section">
            <div class="section-header">
              <h2 class="section-title">版块管理</h2>
              <el-button type="primary" @click="showAddTopicDialog">
                <el-icon><Plus /></el-icon>
                添加版块
              </el-button>
            </div>
            
            <!-- 版块列表表格 -->
            <el-table
              :data="topicsList"
              style="width: 100%"
              v-loading="topicLoading"
              border
            >
              <el-table-column label="ID" prop="id" width="80" align="center" />
              <el-table-column label="名称" prop="name" width="150" />
              <el-table-column label="描述" prop="description" show-overflow-tooltip />
              <el-table-column label="帖子数" prop="post_count" width="100" align="center" />
              <el-table-column label="排序" prop="sort_order" width="80" align="center" />
              <el-table-column label="状态" width="100" align="center">
                <template #default="scope">
                  <el-tag
                    :type="scope.row.status === 1 ? 'success' : 'info'"
                    effect="plain"
                  >
                    {{ scope.row.status === 1 ? '启用' : '禁用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="创建时间" width="180">
                <template #default="scope">
                  {{ formatDateTime(scope.row.createTime) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="220" fixed="right">
                <template #default="scope">
                  <el-button
                    size="small"
                    type="primary"
                    @click="showEditTopicDialog(scope.row)"
                  >
                    编辑
                  </el-button>
                  <el-button
                    size="small"
                    :type="scope.row.status === 1 ? 'warning' : 'success'"
                    @click="changeTopicStatus(scope.row)"
                  >
                    {{ scope.row.status === 1 ? '禁用' : '启用' }}
                  </el-button>
                  <el-button
                    size="small"
                    type="danger"
                    @click="deleteTopic(scope.row)"
                  >
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
            
            <!-- 添加/编辑版块对话框 -->
            <el-dialog
              :title="editTopicMode ? '编辑版块' : '添加版块'"
              v-model="addTopicDialogVisible"
              width="500px"
            >
              <el-form
                ref="topicFormRef"
                :model="topicForm"
                :rules="topicFormRules"
                label-width="100px"
              >
                <el-form-item label="版块名称" prop="name">
                  <el-input v-model="topicForm.name" placeholder="请输入版块名称" />
                </el-form-item>
                <el-form-item label="版块描述" prop="description">
                  <el-input
                    v-model="topicForm.description"
                    type="textarea"
                    rows="3"
                    placeholder="请输入版块描述"
                  />
                </el-form-item>
                <el-form-item label="图标路径" prop="icon">
                  <el-input v-model="topicForm.icon" placeholder="请输入图标URL路径" />
                </el-form-item>
                <el-form-item label="封面路径" prop="cover">
                  <el-input v-model="topicForm.cover" placeholder="请输入封面图URL路径" />
                </el-form-item>
                <el-form-item label="排序" prop="sort_order">
                  <el-input-number v-model="topicForm.sort_order" :min="0" />
                </el-form-item>
              </el-form>
              <template #footer>
                <div class="dialog-footer">
                  <el-button @click="addTopicDialogVisible = false">取消</el-button>
                  <el-button type="primary" @click="saveTopic">保存</el-button>
                </div>
              </template>
            </el-dialog>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="内容审核" name="audit">
          <div class="management-section">
            <div class="section-header">
              <h2 class="section-title">内容审核</h2>
              <div class="audit-stats">
                <el-tag type="warning" effect="plain">
                  待审核: {{ pendingPostsTotalCount }}
                </el-tag>
              </div>
            </div>
            
            <!-- 待审核帖子列表表格 -->
            <el-table
              :data="pendingPostsList"
              style="width: 100%"
              v-loading="pendingPostsLoading"
              border
            >
              <el-table-column label="ID" prop="id" width="80" align="center" />
              <el-table-column label="标题" prop="title" min-width="200" show-overflow-tooltip>
                <template #default="scope">
                  <span class="post-title-text">{{ scope.row.title }}</span>
                </template>
              </el-table-column>
              <el-table-column label="作者" width="120">
                <template #default="scope">
                  <div class="user-info">
                    <el-avatar :size="30" :src="scope.row.userAvatar" />
                    <span class="user-name">{{ getDisplayName(scope.row) }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="版块" width="120">
                <template #default="scope">
                  <el-tag size="small" effect="plain">{{ scope.row.topicName }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="优先级" width="100" align="center">
                <template #default="scope">
                  <el-tag
                    :type="scope.row.priority === 'high' ? 'danger' : 'info'"
                    effect="plain"
                    size="small"
                  >
                    {{ scope.row.priority === 'high' ? '高' : '普通' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="提交时间" width="180">
                <template #default="scope">
                  {{ formatDateTime(scope.row.createTime) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="200" fixed="right">
                <template #default="scope">
                  <div style="display: flex; gap: 12px;">
                    <el-button
                      size="small"
                      type="primary"
                      @click="viewPendingPost(scope.row)"
                    >
                      查看详情
                    </el-button>
                    <el-dropdown trigger="hover" @command="handleAuditCommand">
                      <el-button size="small" type="warning">
                        审核<el-icon class="el-icon--right"><ArrowDown /></el-icon>
                      </el-button>
                      <template #dropdown>
                        <el-dropdown-menu>
                          <el-dropdown-item 
                            :command="{action: 'approve', post: scope.row}"
                          >
                            <el-icon><Check /></el-icon>
                            通过
                          </el-dropdown-item>
                          <el-dropdown-item 
                            :command="{action: 'reject', post: scope.row}"
                            divided
                          >
                            <el-icon><Close /></el-icon>
                            拒绝
                          </el-dropdown-item>
                        </el-dropdown-menu>
                      </template>
                    </el-dropdown>
                  </div>
                </template>
              </el-table-column>
            </el-table>
            
            <div class="pagination-container">
              <el-pagination
                v-model:current-page="pendingPostsCurrentPage"
                v-model:page-size="pendingPostsPageSize"
                :page-sizes="[10, 20, 50]"
                layout="total, sizes, prev, pager, next, jumper"
                :total="pendingPostsTotalCount"
                @size-change="handlePendingPostsSizeChange"
                @current-change="handlePendingPostsCurrentChange"
              />
            </div>
            
            <!-- 审核通过对话框 -->
            <el-dialog
              v-model="approveDialogVisible"
              width="600px"
              :close-on-click-modal="false"
              class="audit-dialog"
            >
              <template #header>
                <div class="dialog-header-approve">
                  <el-icon class="header-icon"><Check /></el-icon>
                  <span class="header-title">审核通过</span>
                </div>
              </template>
              
              <div class="audit-dialog-content">
                <div class="post-preview">
                  <div class="preview-label">帖子标题</div>
                  <div class="preview-title">{{ currentAuditPost?.title }}</div>
                  
                  <div class="preview-label">帖子内容</div>
                  <div class="preview-content">{{ currentAuditPost?.content }}</div>
                  
                  <div class="post-meta">
                    <span class="meta-item">
                      <el-icon><User /></el-icon>
                      {{ getDisplayName(currentAuditPost) }}
                    </span>
                    <span class="meta-item">
                      <el-icon><Clock /></el-icon>
                      {{ formatDateTime(currentAuditPost?.createTime) }}
                    </span>
                  </div>
                </div>
                
                <el-divider />
                
                <el-form
                  ref="approveFormRef"
                  :model="approveForm"
                  label-width="90px"
                  class="audit-form"
                >
                  <el-form-item label="审核意见">
                    <el-input
                      v-model="approveForm.comment"
                      type="textarea"
                      :rows="4"
                      placeholder="请输入审核意见（可选），例如：内容优质，符合社区规范"
                      maxlength="200"
                      show-word-limit
                    />
                  </el-form-item>
                </el-form>
              </div>
              
              <template #footer>
                <div class="dialog-footer-custom">
                  <el-button size="large" @click="approveDialogVisible = false">取消</el-button>
                  <el-button size="large" type="success" @click="confirmApprove">
                    <el-icon><Check /></el-icon>
                    确认通过
                  </el-button>
                </div>
              </template>
            </el-dialog>
            
            <!-- 审核拒绝对话框 -->
            <el-dialog
              v-model="rejectDialogVisible"
              width="600px"
              :close-on-click-modal="false"
              class="audit-dialog"
            >
              <template #header>
                <div class="dialog-header-reject">
                  <el-icon class="header-icon"><Close /></el-icon>
                  <span class="header-title">审核拒绝</span>
                </div>
              </template>
              
              <div class="audit-dialog-content">
                <div class="post-preview">
                  <div class="preview-label">帖子标题</div>
                  <div class="preview-title">{{ currentAuditPost?.title }}</div>
                  
                  <div class="preview-label">帖子内容</div>
                  <div class="preview-content">{{ currentAuditPost?.content }}</div>
                  
                  <div class="post-meta">
                    <span class="meta-item">
                      <el-icon><User /></el-icon>
                      {{ getDisplayName(currentAuditPost) }}
                    </span>
                    <span class="meta-item">
                      <el-icon><Clock /></el-icon>
                      {{ formatDateTime(currentAuditPost?.createTime) }}
                    </span>
                  </div>
                </div>
                
                <el-divider />
                
                <el-form
                  ref="rejectFormRef"
                  :model="rejectForm"
                  :rules="rejectFormRules"
                  label-width="90px"
                  class="audit-form"
                >
                  <el-form-item label="拒绝原因" prop="reason">
                    <el-select 
                      v-model="rejectForm.reason" 
                      placeholder="请选择拒绝原因"
                      style="width: 100%"
                    >
                      <el-option label="内容违规" value="违规内容" />
                      <el-option label="垃圾信息" value="垃圾信息" />
                      <el-option label="重复发布" value="重复发布" />
                      <el-option label="标题不规范" value="标题不规范" />
                      <el-option label="其他原因" value="其他原因" />
                    </el-select>
                  </el-form-item>
                  <el-form-item label="详细说明" prop="comment">
                    <el-input
                      v-model="rejectForm.comment"
                      type="textarea"
                      :rows="4"
                      placeholder="请详细说明拒绝原因，帮助作者了解问题所在"
                      maxlength="200"
                      show-word-limit
                    />
                  </el-form-item>
                </el-form>
              </div>
              
              <template #footer>
                <div class="dialog-footer-custom">
                  <el-button size="large" @click="rejectDialogVisible = false">取消</el-button>
                  <el-button size="large" type="danger" @click="confirmReject">
                    <el-icon><Close /></el-icon>
                    确认拒绝
                  </el-button>
                </div>
              </template>
            </el-dialog>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="帖子管理" name="posts">
          <div class="management-section">
            <div class="section-header">
              <h2 class="section-title">帖子管理</h2>
              <div class="search-bar">
                <el-input
                  v-model="postSearchText"
                  placeholder="搜索帖子标题"
                  clearable
                  @keyup.enter="searchPosts"
                >
                  <template #suffix>
                    <el-icon class="el-input__icon" @click="searchPosts">
                      <Search />
                    </el-icon>
                  </template>
                </el-input>
              </div>
            </div>
            
            <!-- 帖子列表将在后续添加 -->
        
            
            <!-- 帖子列表表格 -->
            <el-table
              :data="postsList"
              style="width: 100%"
              v-loading="postsLoading"
              border
            >
              <el-table-column label="ID" prop="id" width="80" align="center" />
              <el-table-column label="标题" prop="title" min-width="150" show-overflow-tooltip>
                <template #default="scope">
                  <span class="post-title-text">{{ scope.row.title }}</span>
                </template>
              </el-table-column>
              <el-table-column label="作者" width="120">
                <template #default="scope">
                  <span>{{ getDisplayName(scope.row) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="版块" width="120">
                <template #default="scope">
                  <el-tag size="small" effect="plain">{{ scope.row.topicName }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="浏览/回复" width="120" align="center">
                <template #default="scope">
                  {{ scope.row.viewCount }}/{{ scope.row.replyCount }}
                </template>
              </el-table-column>
              <el-table-column label="状态" width="100" align="center">
                <template #default="scope">
                  <el-tag
                    :type="getPostStatusType(scope.row.status)"
                    effect="plain"
                    size="small"
                  >
                    {{ getPostStatusText(scope.row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="特性" width="120" align="center">
                <template #default="scope">
                  <div class="post-features">
                    <el-tag
                      v-if="scope.row.isSticky === 1"
                      type="danger"
                      effect="plain"
                      size="small"
                      class="feature-tag"
                    >
                      置顶
                    </el-tag>
                    <el-tag
                      v-if="scope.row.isEssence === 1"
                      type="success"
                      effect="plain"
                      size="small"
                      class="feature-tag"
                    >
                      精华
                    </el-tag>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="发布时间" width="180">
                <template #default="scope">
                  {{ formatDateTime(scope.row.createTime) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="350" fixed="right">
                <template #default="scope">
                  <div style="display: flex; flex-wrap: wrap; gap: 5px;">
                    <el-button
                      size="small"
                      type="info"
                      @click="viewPost(scope.row)"
                    >
                      查看
                    </el-button>
                    
                    <el-button
                      size="small"
                      type="success"
                      v-if="scope.row.status === 0"
                      @click="approvePost(scope.row)"
                    >
                      审核通过
                    </el-button>
                    
                    <el-button
                      size="small"
                      type="warning"
                      :disabled="scope.row.status !== 1"
                      @click="toggleTopPost(scope.row)"
                    >
                      {{ scope.row.isSticky === 1 ? '取消置顶' : '置顶' }}
                    </el-button>
                    
                    <el-button
                      size="small"
                      type="primary"
                      :disabled="scope.row.status !== 1"
                      @click="toggleEssencePost(scope.row)"
                    >
                      {{ scope.row.isEssence === 1 ? '取消加精' : '加精' }}
                    </el-button>
                    
                    <el-button
                      size="small"
                      type="danger"
                      @click="deletePost(scope.row)"
                    >
                      删除
                    </el-button>
                  </div>
                </template>
              </el-table-column>
            </el-table>
            
            <div class="pagination-container">
              <el-pagination
                v-model:current-page="postCurrentPage"
                v-model:page-size="postPageSize"
                :page-sizes="[10, 20, 50, 100]"
                layout="total, sizes, prev, pager, next, jumper"
                :total="postsTotalCount"
                @size-change="handlePostsSizeChange"
                @current-change="handlePostsCurrentChange"
              />
            </div>
      </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useForumStore } from '@/stores/forum'
import { Reading, Plus, Search, ArrowDown, Check, Close, User, Clock } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import { forumPromptMessages } from '@/utils/promptMessages'
import { showByCode } from '@/utils/apiMessages'
const router = useRouter()
const forumStore = useForumStore()
const activeTab = ref('audit')  // 默认显示审核标签页
const postSearchText = ref('')

// 获取显示名称（使用昵称）
const getDisplayName = (user) => {
  if (!user) return '未知用户'
  return user.nickname || '未知用户'
}
    
// 版块相关数据
const topicsList = computed(() => forumStore.forumTopics)
const topicLoading = computed(() => forumStore.loading)
    
// 内容审核相关数据
const pendingPostsList = computed(() => forumStore.pendingPosts)
const pendingPostsLoading = computed(() => forumStore.loading)
const pendingPostsTotalCount = computed(() => forumStore.pendingPostsPagination.total)
const pendingPostsCurrentPage = computed({
  get: () => forumStore.pendingPostsPagination?.currentPage || 1,
  set: val => {
    if (forumStore.pendingPostsPagination) forumStore.pendingPostsPagination.currentPage = val
  }
})
const pendingPostsPageSize = computed({
  get: () => forumStore.pendingPostsPagination?.pageSize || 10,
  set: val => {
    if (forumStore.pendingPostsPagination) forumStore.pendingPostsPagination.pageSize = val
  }
})
    
// 审核对话框相关
const approveDialogVisible = ref(false)
const rejectDialogVisible = ref(false)
const currentAuditPost = ref(null)
    
const approveForm = ref({
  comment: ''
})

const rejectForm = ref({
  reason: '',
  comment: ''
})

const rejectFormRules = {
  reason: [
    { required: true, message: '请选择拒绝原因', trigger: 'change' }
  ],
  comment: [
    { required: true, message: '请详细说明拒绝原因', trigger: 'blur' },
    { min: 10, message: '详细说明至少10个字符', trigger: 'blur' }
  ]
}

const approveFormRef = ref(null)
const rejectFormRef = ref(null)
const addTopicDialogVisible = ref(false)
const editTopicMode = ref(false)
const currentTopic = ref(null)
    
// 帖子相关数据
// 列表数据与总数统一从 forumStore 读取，遵循「组件 -> Store -> API」的数据流
const postsList = computed(() => forumStore.forumPosts)
const postsLoading = ref(false)
const postsTotalCount = computed(() => forumStore.postPagination?.total || 0)
const postCurrentPage = computed({
  get: () => forumStore.postPagination?.currentPage || 1,
  set: val => {
    if (forumStore.postPagination) forumStore.postPagination.currentPage = val
  }
})
const postPageSize = computed({
  get: () => forumStore.postPagination?.pageSize || 10,
  set: val => {
    if (forumStore.postPagination) forumStore.postPagination.pageSize = val
  }
})

const topicForm = ref({
  name: '',
  description: '',
  icon: '',
  cover: '',
  sort_order: 0
})

const topicFormRules = {
  name: [
    { required: true, message: '请输入版块名称', trigger: 'blur' },
    { min: 2, max: 50, message: '版块名称长度应为2-50个字符', trigger: 'blur' }
  ],
  description: [
    { max: 200, message: '描述不能超过200个字符', trigger: 'blur' }
  ]
}

const topicFormRef = ref(null)

const goToCultureManage = () => {
  router.push('/culture/manage')
}
    
// 加载版块列表
const loadTopics = async () => {
  try {
    const res = await forumStore.fetchForumTopics()
    showByCode(res.code)
  } catch (error) {
    console.error('获取版块列表失败:', error)
  }
}

// 显示添加版块对话框
const showAddTopicDialog = () => {
  editTopicMode.value = false
  topicForm.value = {
    name: '',
    description: '',
    icon: '',
    cover: '',
    sort_order: topicsList.value.length + 1
  }
  addTopicDialogVisible.value = true
}

// 显示编辑版块对话框
const showEditTopicDialog = topic => {
  editTopicMode.value = true
  currentTopic.value = topic
  topicForm.value = {
    name: topic.name,
    description: topic.description,
    icon: topic.icon,
    cover: topic.cover,
    sort_order: topic.sort_order
  }
  addTopicDialogVisible.value = true
}

// 保存版块（添加或更新）
const saveTopic = async () => {
  if (!topicFormRef.value) return
  
  await topicFormRef.value.validate(async valid => {
    if (valid) {
      const actionType = editTopicMode.value ? '更新' : '添加'
      
      try {
        if (editTopicMode.value) {
          const res = await forumStore.updateTopic({
            id: currentTopic.value.id,
            data: topicForm.value
          })
          showByCode(res.code)
        } else {
          const res = await forumStore.createTopic(topicForm.value)
          showByCode(res.code)
        }
        
        addTopicDialogVisible.value = false
        await loadTopics()
      } catch (error) {
        console.error(`${actionType}版块失败:`, error)
      }
    }
  })
}

// 更改版块状态
const changeTopicStatus = async topic => {
  const newStatus = topic.status === 1 ? 0 : 1
  
  try {
    const res = await forumStore.updateTopic({
      id: topic.id,
      data: { ...topic, status: newStatus }
    })
    showByCode(res.code)
    await loadTopics()
  } catch (error) {
    console.error('更改版块状态失败:', error)
  }
}

// 删除版块
const deleteTopic = async topic => {
  ElMessageBox.confirm(
    `确定要删除版块"${topic.name}"吗？此操作将无法恢复，且可能影响已发布的帖子。`,
    '删除确认',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(async () => {
      try {
        const res = await forumStore.deleteTopic(topic.id)
        showByCode(res.code)
        await loadTopics()
      } catch (error) {
        console.error('删除版块失败:', error)
      }
    })
    .catch(() => {
      // 用户取消
    })
}

// 加载帖子列表
const loadPosts = async () => {
  postsLoading.value = true
  try {
    const params = {
      page: postCurrentPage.value,
      pageSize: postPageSize.value,
      keyword: postSearchText.value.trim() || undefined
    }
    const res = await forumStore.fetchForumPosts(params)
    showByCode(res.code)
  } catch (error) {
    console.error('获取帖子列表失败:', error)
  } finally {
    postsLoading.value = false
  }
}

// 搜索帖子
const searchPosts = () => {
  postCurrentPage.value = 1
  loadPosts()
}

// 处理分页大小变化
const handlePostsSizeChange = val => {
  postPageSize.value = val
  postCurrentPage.value = 1
  loadPosts()
}

// 处理分页页码变化
const handlePostsCurrentChange = val => {
  postCurrentPage.value = val
  loadPosts()
}

// 获取帖子状态类型
const getPostStatusType = status => {
  switch (status) {
  case 0: return 'warning' // 待审核
  case 1: return 'success' // 已发布
  case 2: return 'info'    // 已下架
  case -1: return 'danger' // 已删除
  default: return 'info'
  }
}

// 获取帖子状态文本
const getPostStatusText = status => {
  switch (status) {
  case 0: return '待审核'
  case 1: return '已发布'
  case 2: return '已下架'
  case -1: return '已删除'
  default: return '未知'
  }
}

// 查看帖子
const viewPost = post => {
  router.push(`/forum/detail/${post.id}`)
}

// 审核通过帖子
const approvePost = async post => {
  showApproveDialog(post)
}

// 切换帖子置顶状态
const toggleTopPost = async post => {
  const newTopStatus = post.isSticky === 1 ? 0 : 1
  
  try {
    const res = await forumStore.togglePostSticky({
      id: post.id,
      isSticky: newTopStatus
    })
    showByCode(res.code)
    // 更新本地数据
    post.isSticky = newTopStatus
  } catch (error) {
    console.error('切换置顶状态失败:', error)
  }
}

// 切换帖子精华状态
const toggleEssencePost = async post => {
  const newEssenceStatus = post.isEssence === 1 ? 0 : 1
  
  try {
    const res = await forumStore.togglePostEssence({
      id: post.id,
      isEssence: newEssenceStatus
    })
    showByCode(res.code)
    // 更新本地数据
    post.isEssence = newEssenceStatus
  } catch (error) {
    console.error('切换精华状态失败:', error)
  }
}

// 删除帖子
const deletePost = async post => {
  ElMessageBox.confirm(
    `确定要删除帖子"${post.title}"吗？此操作将无法恢复。`,
    '删除确认',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(async () => {
      forumPromptMessages.showFeatureDeveloping()
      return
    })
    .catch(() => {
      // 用户取消
    })
}

// 加载待审核帖子列表
const loadPendingPosts = async () => {
  try {
    const res = await forumStore.fetchPendingPosts({
      page: pendingPostsCurrentPage.value,
      pageSize: pendingPostsPageSize.value
    })
    showByCode(res.code)
  } catch (error) {
    console.error('获取待审核帖子列表失败:', error)
  }
}

// 处理待审核帖子分页大小变化
const handlePendingPostsSizeChange = val => {
  pendingPostsPageSize.value = val
  pendingPostsCurrentPage.value = 1
  loadPendingPosts()
}

// 处理待审核帖子分页页码变化
const handlePendingPostsCurrentChange = val => {
  pendingPostsCurrentPage.value = val
  loadPendingPosts()
}

// 查看待审核帖子详情
const viewPendingPost = post => {
  router.push(`/forum/detail/${post.id}`)
}

// 处理审核下拉菜单命令
const handleAuditCommand = ({ action, post }) => {
  if (action === 'approve') {
    showApproveDialog(post)
  } else if (action === 'reject') {
    showRejectDialog(post)
  }
}

// 显示审核通过对话框
const showApproveDialog = post => {
  currentAuditPost.value = post
  approveForm.value = {
    comment: ''
  }
  approveDialogVisible.value = true
}

// 显示审核拒绝对话框
const showRejectDialog = post => {
  currentAuditPost.value = post
  rejectForm.value = {
    reason: '',
    comment: ''
  }
  rejectDialogVisible.value = true
}

// 确认审核通过
const confirmApprove = async () => {
  try {
    const res = await forumStore.approvePost(
      currentAuditPost.value.id,
      {
        comment: approveForm.value.comment
      }
    )
    showByCode(res.code)
    approveDialogVisible.value = false
    await loadPendingPosts()
  } catch (error) {
    console.error('审核通过失败:', error)
  }
}

// 确认审核拒绝
const confirmReject = async () => {
  if (!rejectFormRef.value) return
  
  await rejectFormRef.value.validate(async valid => {
    if (valid) {
      try {
        const res = await forumStore.rejectPost(
          currentAuditPost.value.id,
          {
            reason: rejectForm.value.reason,
            comment: rejectForm.value.comment
          }
        )
        showByCode(res.code)
        rejectDialogVisible.value = false
        await loadPendingPosts()
      } catch (error) {
        console.error('审核拒绝失败:', error)
      }
    }
  })
}

// 格式化日期时间
const formatDateTime = dateTime => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 监听标签页变化，加载相应数据
watch(activeTab, newVal => {
  if (newVal === 'posts') {
    loadPosts()
  } else if (newVal === 'audit') {
    loadPendingPosts()
  }
})

// 组件加载时获取数据
onMounted(() => {
  loadTopics()
  
  // 默认加载审核标签页数据
  if (activeTab.value === 'audit') {
    loadPendingPosts()
  } else if (activeTab.value === 'posts') {
    loadPosts()
  }
})
</script>

<style lang="scss" scoped>
.forum-manage-page {
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 0;
  margin-bottom: 30px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  
  .container {
    width: 85%;
    max-width: 1920px;
    margin: 0 auto;
    padding: 0;
  }
  
  .page-title {
    font-size: 32px;
    font-weight: 700;
    margin: 0 0 15px;
    color: #fff;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }
  
  .header-actions {
    margin-top: 15px;
    
    :deep(.el-button) {
      border-radius: 999px;
      padding: 10px 26px;
      font-weight: 600;
      letter-spacing: 0.5px;
      background: #82D2CE;
      border-color: #82D2CE;
      color: #103a3a;
      box-shadow: 0 8px 18px rgba(0, 0, 0, 0.28);
      transition: transform 0.15s ease, box-shadow 0.15s ease, background 0.15s ease;
    }

    :deep(.el-button:hover) {
      background: #9ce0dd;
      border-color: #9ce0dd;
      box-shadow: 0 10px 22px rgba(0, 0, 0, 0.32);
      transform: translateY(-1px);
    }

    :deep(.el-button:active) {
      box-shadow: 0 4px 10px rgba(0, 0, 0, 0.24);
      transform: translateY(0);
    }
  }
}

.main-content {
  width: 85%;
  max-width: 1920px;
  margin: 0 auto 40px;
  padding: 0;
}

.management-tabs {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  padding: 24px;
  
  :deep(.el-tabs__header) {
    margin-bottom: 24px;
  }
  
  :deep(.el-tabs__item) {
    font-size: 16px;
    font-weight: 500;
    padding: 0 24px;
    height: 48px;
    line-height: 48px;
  }
}

.management-section {
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    flex-wrap: wrap;
    gap: 16px;
    
    h2, h3 {
      margin: 0;
      font-size: 18px;
      font-weight: 600;
      color: #303133;
    }
    
    .section-title {
      font-size: 18px;
      font-weight: 600;
      margin: 0;
      color: #303133;
    }
    
    .search-bar {
      width: 300px;
    }
    
    .audit-stats {
      display: flex;
      gap: 10px;
      align-items: center;
    }
  }
  
  .post-title-text {
    font-weight: 500;
    color: #333;
  }
  
  .post-features {
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
    gap: 4px;
    
    .feature-tag {
      margin: 2px;
    }
  }
  
  .pagination-container {
    margin-top: 20px;
    text-align: right;
  }
  
  // 审核相关样式
  .user-info {
    display: flex;
    align-items: center;
    gap: 8px;
    
    .user-name {
      font-size: 14px;
      color: #606266;
    }
  }
}

// 审核对话框样式
.audit-dialog {
  :deep(.el-dialog__header) {
    padding: 0;
    margin: 0;
  }
  
  :deep(.el-dialog__body) {
    padding: 0;
  }
  
  :deep(.el-dialog__footer) {
    padding: 20px 24px;
    border-top: 1px solid #e4e7ed;
  }
  
  .dialog-header-approve,
  .dialog-header-reject {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 20px 24px;
    border-radius: 8px 8px 0 0;
    
    .header-icon {
      font-size: 24px;
    }
    
    .header-title {
      font-size: 18px;
      font-weight: 600;
    }
  }
  
  .dialog-header-approve {
    background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
    color: #fff;
  }
  
  .dialog-header-reject {
    background: linear-gradient(135deg, #f56c6c 0%, #f78989 100%);
    color: #fff;
  }
  
  .audit-dialog-content {
    padding: 24px;
  }
  
  .post-preview {
    .preview-label {
      font-size: 12px;
      color: #909399;
      margin-bottom: 8px;
      font-weight: 500;
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }
    
    .preview-title {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 16px;
      line-height: 1.5;
    }
    
    .preview-content {
      font-size: 14px;
      color: #606266;
      line-height: 1.8;
      margin-bottom: 16px;
      padding: 12px;
      background-color: #f5f7fa;
      border-radius: 6px;
      max-height: 120px;
      overflow-y: auto;
      
      &::-webkit-scrollbar {
        width: 6px;
      }
      
      &::-webkit-scrollbar-thumb {
        background-color: #dcdfe6;
        border-radius: 3px;
      }
    }
    
    .post-meta {
      display: flex;
      gap: 20px;
      font-size: 13px;
      color: #909399;
      
      .meta-item {
        display: flex;
        align-items: center;
        gap: 6px;
        
        .el-icon {
          font-size: 14px;
        }
      }
    }
  }
  
  .audit-form {
    :deep(.el-form-item__label) {
      font-weight: 500;
      color: #606266;
    }
    
    :deep(.el-textarea__inner) {
      border-radius: 6px;
      font-size: 14px;
      line-height: 1.6;
    }
    
    :deep(.el-select) {
      width: 100%;
    }
  }
  
  .dialog-footer-custom {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    
    .el-button {
      min-width: 100px;
      border-radius: 6px;
      font-weight: 500;
      
      &.el-button--success {
        background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
        border: none;
        
        &:hover {
          opacity: 0.9;
          transform: translateY(-1px);
          box-shadow: 0 4px 12px rgba(103, 194, 58, 0.4);
        }
      }
      
      &.el-button--danger {
        background: linear-gradient(135deg, #f56c6c 0%, #f78989 100%);
        border: none;
        
        &:hover {
          opacity: 0.9;
          transform: translateY(-1px);
          box-shadow: 0 4px 12px rgba(245, 108, 108, 0.4);
        }
      }
    }
  }
}

// 按钮优化（与茶文化内容管理统一）
.el-button {
  border-radius: 4px;
  font-weight: 500;
  transition: all 0.3s;
  
  &.el-button--primary {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;
    
    &:hover {
      opacity: 0.9;
      transform: translateY(-1px);
      box-shadow: 0 4px 8px rgba(102, 126, 234, 0.3);
    }
  }
}

// 下拉菜单样式优化
:deep(.el-dropdown-menu) {
  padding: 5px 0 !important;
  border-radius: 4px !important;
}

:deep(.el-dropdown-menu__item) {
  padding: 0 16px !important;
  height: 34px !important;
  line-height: 34px !important;
  font-size: 14px !important;
  
  .el-icon {
    margin-right: 8px !important;
    font-size: 14px !important;
  }
}

@media (max-width: 768px) {
  .management-section {
    .section-header {
      flex-direction: column;
      align-items: flex-start;
      
      .section-title {
        margin-bottom: 10px;
      }
      
      .search-bar {
        width: 100%;
      }
      
      .el-button {
        margin-top: 10px;
        width: 100%;
      }
    }
  }
}
</style> 