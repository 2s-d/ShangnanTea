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
                  {{ scope.row.create_time }}
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
                    <span class="user-name">{{ scope.row.userName }}</span>
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
                  <el-button
                    size="small"
                    type="primary"
                    @click="viewPendingPost(scope.row)"
                  >
                    查看详情
                  </el-button>
                  <el-button
                    size="small"
                    type="success"
                    @click="showApproveDialog(scope.row)"
                  >
                    通过
                  </el-button>
                  <el-button
                    size="small"
                    type="danger"
                    @click="showRejectDialog(scope.row)"
                  >
                    拒绝
                  </el-button>
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
              title="审核通过"
              v-model="approveDialogVisible"
              width="500px"
            >
              <div class="audit-post-info">
                <h4>{{ currentAuditPost?.title }}</h4>
                <p class="post-content">{{ currentAuditPost?.content }}</p>
              </div>
              <el-form
                ref="approveFormRef"
                :model="approveForm"
                label-width="100px"
              >
                <el-form-item label="审核意见">
                  <el-input
                    v-model="approveForm.comment"
                    type="textarea"
                    rows="3"
                    placeholder="请输入审核意见（可选）"
                  />
                </el-form-item>
              </el-form>
              <template #footer>
                <div class="dialog-footer">
                  <el-button @click="approveDialogVisible = false">取消</el-button>
                  <el-button type="primary" @click="confirmApprove">确认通过</el-button>
                </div>
              </template>
            </el-dialog>
            
            <!-- 审核拒绝对话框 -->
            <el-dialog
              title="审核拒绝"
              v-model="rejectDialogVisible"
              width="500px"
            >
              <div class="audit-post-info">
                <h4>{{ currentAuditPost?.title }}</h4>
                <p class="post-content">{{ currentAuditPost?.content }}</p>
              </div>
              <el-form
                ref="rejectFormRef"
                :model="rejectForm"
                :rules="rejectFormRules"
                label-width="100px"
              >
                <el-form-item label="拒绝原因" prop="reason">
                  <el-select v-model="rejectForm.reason" placeholder="请选择拒绝原因">
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
                    rows="3"
                    placeholder="请详细说明拒绝原因"
                  />
                </el-form-item>
              </el-form>
              <template #footer>
                <div class="dialog-footer">
                  <el-button @click="rejectDialogVisible = false">取消</el-button>
                  <el-button type="danger" @click="confirmReject">确认拒绝</el-button>
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
            <div class="placeholder-content">
              帖子管理功能即将加载...
            </div>
            
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
                  <span>{{ scope.row.author_name }}</span>
                </template>
              </el-table-column>
              <el-table-column label="版块" width="120">
                <template #default="scope">
                  <el-tag size="small" effect="plain">{{ scope.row.topic_name }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="浏览/回复" width="120" align="center">
                <template #default="scope">
                  {{ scope.row.view_count }}/{{ scope.row.reply_count }}
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
                      v-if="scope.row.is_top"
                      type="danger"
                      effect="plain"
                      size="small"
                      class="feature-tag"
                    >
                      置顶
                    </el-tag>
                    <el-tag
                      v-if="scope.row.is_essence"
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
                  {{ scope.row.create_time }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="300" fixed="right">
                <template #default="scope">
                  <el-button
                    size="small"
                    type="primary"
                    link
                    @click="viewPost(scope.row)"
                  >
                    查看
                  </el-button>
                  
                  <el-button
                    size="small"
                    type="primary"
                    link
                    v-if="scope.row.status === 0"
                    @click="approvePost(scope.row)"
                  >
                    审核通过
                  </el-button>
                  
                  <el-button
                    size="small"
                    type="primary"
                    link
                    :disabled="scope.row.status !== 1"
                    @click="toggleTopPost(scope.row)"
                  >
                    {{ scope.row.is_top ? '取消置顶' : '置顶' }}
                  </el-button>
                  
                  <el-button
                    size="small"
                    type="primary"
                    link
                    :disabled="scope.row.status !== 1"
                    @click="toggleEssencePost(scope.row)"
                  >
                    {{ scope.row.is_essence ? '取消加精' : '加精' }}
                  </el-button>
                  
                  <el-button
                    size="small"
                    type="danger"
                    link
                    @click="deletePost(scope.row)"
                  >
                    删除
                  </el-button>
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

<script>
import { ref, onMounted, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { Reading, Plus, Search } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import { forumPromptMessages } from '@/utils/promptMessages'
import { showByCode } from '@/utils/apiMessages'

export default {
  name: 'ForumManagePage',
  components: {
    Reading,
    Plus,
    Search
  },
  setup() {
    const router = useRouter()
    const store = useStore()
    const activeTab = ref('audit')  // 默认显示审核标签页
    const postSearchText = ref('')
    
    // 版块相关数据
    const topicsList = computed(() => store.state.forum.forumTopics)
    const topicLoading = computed(() => store.state.forum.loading)
    
    // 内容审核相关数据
    const pendingPostsList = computed(() => store.state.forum.pendingPosts)
    const pendingPostsLoading = computed(() => store.state.forum.loading)
    const pendingPostsTotalCount = computed(() => store.state.forum.pendingPostsPagination.total)
    const pendingPostsCurrentPage = ref(1)
    const pendingPostsPageSize = ref(10)
    
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
    const postsList = ref([])
    const postsLoading = ref(false)
    const postsTotalCount = ref(0)
    const postCurrentPage = ref(1)
    const postPageSize = ref(10)
    
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
        const res = await store.dispatch('forum/fetchForumTopics')
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
    const showEditTopicDialog = (topic) => {
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
      
      await topicFormRef.value.validate(async (valid) => {
        if (valid) {
          const actionType = editTopicMode.value ? '更新' : '添加'
          
          try {
            if (editTopicMode.value) {
              const res = await store.dispatch('forum/updateTopic', {
                id: currentTopic.value.id,
                data: topicForm.value
              })
              showByCode(res.code)
            } else {
              const res = await store.dispatch('forum/createTopic', topicForm.value)
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
    const changeTopicStatus = async (topic) => {
      const newStatus = topic.status === 1 ? 0 : 1
      
      try {
        const res = await store.dispatch('forum/updateTopic', {
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
    const deleteTopic = async (topic) => {
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
            const res = await store.dispatch('forum/deleteTopic', topic.id)
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
      
      // TODO-SCRIPT: 帖子管理列表需要后端接口与 Vuex forum 模块（当前仅保留首页数据）
      // 生产形态：不在 UI 层 setTimeout 伪造数据与成功状态
      postsList.value = []
      postsTotalCount.value = 0

      /*
      // 模拟帖子数据
      const mockPosts = [
        {
          id: 1,
          title: '分享我最近喝过的好茶',
          content: '最近尝试了几款不同产地的绿茶，感觉口感各有特点...',
          author_id: 'cy100002',
          author_name: '茶香四溢',
          author_avatar: '/avatar/user2.jpg',
          topic_id: 1,
          topic_name: '茶叶知识',
          view_count: 128,
          reply_count: 15,
          is_top: true,
          is_essence: true,
          status: 1, // 1-已发布
          create_time: '2025-03-28 10:25:36',
          update_time: '2025-03-28 10:25:36'
        },
        {
          id: 2,
          title: '新手求推荐适合入门的茶具套装',
          content: '刚开始接触茶道，想买一套适合新手的茶具，有什么推荐吗？',
          author_id: 'cy100005',
          author_name: '茶道初学者',
          author_avatar: '/avatar/user5.jpg',
          topic_id: 3,
          topic_name: '茶具讨论',
          view_count: 72,
          reply_count: 8,
          is_top: false,
          is_essence: false,
          status: 1, // 1-已发布
          create_time: '2025-03-29 14:35:12',
          update_time: '2025-03-29 14:35:12'
        },
        {
          id: 3,
          title: '茶友们都来分享一下最喜欢的茶叶品种吧',
          content: '我个人最喜欢的是铁观音，浓香型的那种...',
          author_id: 'cy100008',
          author_name: '茶叶收藏家',
          author_avatar: '/avatar/user8.jpg',
          topic_id: 2,
          topic_name: '茶友交流',
          view_count: 95,
          reply_count: 23,
          is_top: false,
          is_essence: true,
          status: 1, // 1-已发布
          create_time: '2025-03-30 09:17:45',
          update_time: '2025-03-30 09:17:45'
        },
        {
          id: 4,
          title: '春茶采摘季开始了，有什么值得期待的新茶吗？',
          content: '眼看着春茶季就要到了，哪些产区的春茶大家觉得值得关注？',
          author_id: 'cy100012',
          author_name: '春茶爱好者',
          author_avatar: '/avatar/user12.jpg',
          topic_id: 1,
          topic_name: '茶叶知识',
          view_count: 42,
          reply_count: 5,
          is_top: false,
          is_essence: false,
          status: 0, // 0-待审核
          create_time: '2025-04-01 16:28:53',
          update_time: '2025-04-01 16:28:53'
        }
      ]
      
      postsList.value = mockPosts
      postsTotalCount.value = 28 // 模拟总数
      */
postsLoading.value = false
    }
    
    // 搜索帖子
    const searchPosts = () => {
      postCurrentPage.value = 1
      loadPosts()
    }
    
    // 处理分页大小变化
    const handlePostsSizeChange = (val) => {
      postPageSize.value = val
      loadPosts()
    }
    
    // 处理分页页码变化
    const handlePostsCurrentChange = (val) => {
      postCurrentPage.value = val
      loadPosts()
    }
    
    // 获取帖子状态类型
    const getPostStatusType = (status) => {
      switch (status) {
        case 0: return 'warning' // 待审核
        case 1: return 'success' // 已发布
        case 2: return 'info'    // 已下架
        case -1: return 'danger' // 已删除
        default: return 'info'
      }
    }
    
    // 获取帖子状态文本
    const getPostStatusText = (status) => {
      switch (status) {
        case 0: return '待审核'
        case 1: return '已发布'
        case 2: return '已下架'
        case -1: return '已删除'
        default: return '未知'
      }
    }
    
    // 查看帖子
    const viewPost = (post) => {
      router.push(`/forum/detail/${post.id}`)
    }
    
    // 审核通过帖子
    const approvePost = async (post) => {
      // TODO-SCRIPT: 审核帖子需要后端接口与 Vuex forum 模块；不在 UI 层伪造成功/本地改状态
      forumPromptMessages.showFeatureDeveloping()
      return
}
    
    // 切换帖子置顶状态
    const toggleTopPost = async (post) => {
      const newTopStatus = !post.is_top
      
      try {
        const res = await store.dispatch('forum/togglePostSticky', {
          id: post.id,
          isSticky: newTopStatus
        })
        showByCode(res.code)
        // 更新本地数据
        post.is_top = newTopStatus
      } catch (error) {
        console.error('切换置顶状态失败:', error)
      }
    }
    
    // 切换帖子精华状态
    const toggleEssencePost = async (post) => {
      const newEssenceStatus = !post.is_essence
      
      try {
        const res = await store.dispatch('forum/togglePostEssence', {
          id: post.id,
          isEssence: newEssenceStatus
        })
        showByCode(res.code)
        // 更新本地数据
        post.is_essence = newEssenceStatus
      } catch (error) {
        console.error('切换精华状态失败:', error)
      }
    }
    
    // 删除帖子
    const deletePost = async (post) => {
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
          // TODO-SCRIPT: 删除帖子需要后端接口与权限控制；不在 UI 层做本地伪删除
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
        const res = await store.dispatch('forum/fetchPendingPosts', {
          page: pendingPostsCurrentPage.value,
          size: pendingPostsPageSize.value
        })
        showByCode(res.code)
      } catch (error) {
        console.error('获取待审核帖子列表失败:', error)
      }
    }
    
    // 处理待审核帖子分页大小变化
    const handlePendingPostsSizeChange = (val) => {
      pendingPostsPageSize.value = val
      loadPendingPosts()
    }
    
    // 处理待审核帖子分页页码变化
    const handlePendingPostsCurrentChange = (val) => {
      pendingPostsCurrentPage.value = val
      loadPendingPosts()
    }
    
    // 查看待审核帖子详情
    const viewPendingPost = (post) => {
      router.push(`/forum/detail/${post.id}`)
    }
    
    // 显示审核通过对话框
    const showApproveDialog = (post) => {
      currentAuditPost.value = post
      approveForm.value = {
        comment: ''
      }
      approveDialogVisible.value = true
    }
    
    // 显示审核拒绝对话框
    const showRejectDialog = (post) => {
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
        const res = await store.dispatch('forum/approvePost', {
          id: currentAuditPost.value.id,
          data: {
            comment: approveForm.value.comment
          }
        })
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
      
      await rejectFormRef.value.validate(async (valid) => {
        if (valid) {
          try {
            const res = await store.dispatch('forum/rejectPost', {
              id: currentAuditPost.value.id,
              data: {
                reason: rejectForm.value.reason,
                comment: rejectForm.value.comment
              }
            })
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
    const formatDateTime = (dateTime) => {
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
    watch(activeTab, (newVal) => {
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
    
    return {
      activeTab,
      postSearchText,
      goToCultureManage,
      showAddTopicDialog,
      showEditTopicDialog,
      saveTopic,
      changeTopicStatus,
      deleteTopic,
      topicsList,
      topicLoading,
      addTopicDialogVisible,
      editTopicMode,
      currentTopic,
      topicForm,
      topicFormRules,
      topicFormRef,
      searchPosts,
      // 帖子相关
      postsList,
      postsLoading,
      postsTotalCount,
      postCurrentPage,
      postPageSize,
      handlePostsSizeChange,
      handlePostsCurrentChange,
      getPostStatusType,
      getPostStatusText,
      viewPost,
      approvePost,
      toggleTopPost,
      toggleEssencePost,
      deletePost,
      // 内容审核相关
      pendingPostsList,
      pendingPostsLoading,
      pendingPostsTotalCount,
      pendingPostsCurrentPage,
      pendingPostsPageSize,
      handlePendingPostsSizeChange,
      handlePendingPostsCurrentChange,
      viewPendingPost,
      showApproveDialog,
      showRejectDialog,
      confirmApprove,
      confirmReject,
      formatDateTime,
      // 审核对话框相关
      approveDialogVisible,
      rejectDialogVisible,
      currentAuditPost,
      approveForm,
      rejectForm,
      rejectFormRules,
      approveFormRef,
      rejectFormRef
    }
  }
}
</script>

<style lang="scss" scoped>
.forum-manage-page {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 15px;
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
  
  .header-actions {
    margin-top: 10px;
    
    .el-button {
      padding: 9px 15px;
      font-size: 14px;
      line-height: 1.5;
      border-radius: 4px;
      font-weight: 500;
      height: 40px;
      
      .el-icon {
        margin-right: 5px;
      }
    }
  }
}

.main-content {
  margin-bottom: 40px;
}

.management-tabs {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.management-section {
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    .section-title {
      font-size: 20px;
      font-weight: 500;
      margin: 0;
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
  
  .audit-post-info {
    margin-bottom: 20px;
    padding: 15px;
    background-color: #f8f9fa;
    border-radius: 6px;
    border-left: 4px solid #409eff;
    
    h4 {
      margin: 0 0 10px;
      font-size: 16px;
      font-weight: 500;
      color: #303133;
    }
    
    .post-content {
      margin: 0;
      font-size: 14px;
      color: #606266;
      line-height: 1.6;
      max-height: 100px;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 4;
      -webkit-box-orient: vertical;
    }
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