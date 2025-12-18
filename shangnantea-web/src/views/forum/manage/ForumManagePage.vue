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
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Reading, Plus, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

export default {
  name: 'ForumManagePage',
  components: {
    Reading,
    Plus,
    Search
  },
  setup() {
    const router = useRouter()
    const activeTab = ref('topics')
    const postSearchText = ref('')
    
    // 版块相关数据
    const topicsList = ref([])
    const topicLoading = ref(false)
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
      topicLoading.value = true

      // TODO-SCRIPT: 论坛管理功能需要后端接口与 Vuex forum 模块支持（当前 store/modules/forum.js 仅保留首页数据）
      // 生产形态：不在 UI 层 setTimeout 伪造数据与成功状态
      topicsList.value = []
      topicLoading.value = false
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

          // TODO-SCRIPT: 版块新增/编辑需要后端接口与 Vuex forum 模块；不在 UI 层 setTimeout 伪提交/本地更新
          ElMessage.info(`${actionType}版块功能待后端接口接入`)
          return
          
          /* 
          // 真实代码(开发UI时注释)
          try {
            if (editTopicMode.value) {
              await store.dispatch('forum/updateForumTopic', {
                id: currentTopic.value.id,
                ...topicForm.value
              })
            } else {
              await store.dispatch('forum/addForumTopic', topicForm.value)
            }
            
            ElMessage.success(`${actionType}版块成功`)
            addTopicDialogVisible.value = false
            await loadTopics()
          } catch (error) {
            ElMessage.error(`${actionType}版块失败: ${error.message}`)
          }
          */
        }
      })
    }
    
    // 更改版块状态
    const changeTopicStatus = async (topic) => {
      const newStatus = topic.status === 1 ? 0 : 1
      const actionText = newStatus === 1 ? '启用' : '禁用'

      // TODO-SCRIPT: 版块启用/禁用需要后端接口与 Vuex forum 模块支持
      ElMessage.info(`${actionText}版块功能待后端接口接入`)
      return
      
      /* 
      // 真实代码(开发UI时注释)
      try {
        await store.dispatch('forum/updateForumTopicStatus', {
          id: topic.id,
          status: newStatus
        })
        
        ElMessage.success(`${actionText}版块成功`)
        await loadTopics()
      } catch (error) {
        ElMessage.error(`${actionText}版块失败: ${error.message}`)
      }
      */
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

          // TODO-SCRIPT: 删除版块需要后端接口与权限控制；不在 UI 层做本地伪删除
          ElMessage.info('删除版块功能待后端接口接入')
          return
          
          /* 
          // 真实代码(开发UI时注释)
          try {
            await store.dispatch('forum/deleteForumTopic', topic.id)
            ElMessage.success('删除版块成功')
            await loadTopics()
          } catch (error) {
            ElMessage.error(`删除版块失败: ${error.message}`)
          }
          */
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
      
      /* 
      // 真实代码(开发UI时注释)
      try {
        const result = await store.dispatch('forum/getForumPosts', {
          page: postCurrentPage.value,
          pageSize: postPageSize.value,
          keyword: postSearchText.value
        })
        
        postsList.value = result.list
        postsTotalCount.value = result.total
      } catch (error) {
        ElMessage.error('获取帖子列表失败')
      }
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
      ElMessage.info('帖子审核功能待后端接口接入')
      return
      
      /* 
      // 真实代码(开发UI时注释)
      try {
        await store.dispatch('forum/approveForumPost', post.id)
        ElMessage.success('帖子审核通过')
        await loadPosts()
      } catch (error) {
        ElMessage.error(`帖子审核失败: ${error.message}`)
      }
      */
    }
    
    // 切换帖子置顶状态
    const toggleTopPost = async (post) => {
      const newTopStatus = !post.is_top
      const actionText = newTopStatus ? '置顶' : '取消置顶'
      // TODO-SCRIPT: 置顶/取消置顶需要后端接口与 Vuex forum 模块；不在 UI 层伪造成功/本地改状态
      ElMessage.info(`帖子${actionText}功能待后端接口接入`)
      return
      
      /* 
      // 真实代码(开发UI时注释)
      try {
        await store.dispatch('forum/updateForumPostTop', {
          id: post.id,
          isTop: newTopStatus
        })
        
        ElMessage.success(`帖子${actionText}成功`)
        await loadPosts()
      } catch (error) {
        ElMessage.error(`帖子${actionText}失败: ${error.message}`)
      }
      */
    }
    
    // 切换帖子精华状态
    const toggleEssencePost = async (post) => {
      const newEssenceStatus = !post.is_essence
      const actionText = newEssenceStatus ? '加精' : '取消加精'
      // TODO-SCRIPT: 加精/取消加精需要后端接口与 Vuex forum 模块；不在 UI 层伪造成功/本地改状态
      ElMessage.info(`帖子${actionText}功能待后端接口接入`)
      return
      
      /* 
      // 真实代码(开发UI时注释)
      try {
        await store.dispatch('forum/updateForumPostEssence', {
          id: post.id,
          isEssence: newEssenceStatus
        })
        
        ElMessage.success(`帖子${actionText}成功`)
        await loadPosts()
      } catch (error) {
        ElMessage.error(`帖子${actionText}失败: ${error.message}`)
      }
      */
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
          ElMessage.info('删除帖子功能待后端接口接入')
          return
          
          /* 
          // 真实代码(开发UI时注释)
          try {
            await store.dispatch('forum/deleteForumPost', post.id)
            ElMessage.success('删除帖子成功')
            await loadPosts()
          } catch (error) {
            ElMessage.error(`删除帖子失败: ${error.message}`)
          }
          */
        })
        .catch(() => {
          // 用户取消
        })
    }
    
    // 监听标签页变化，加载相应数据
    watch(activeTab, (newVal) => {
      if (newVal === 'posts') {
        loadPosts()
      }
    })
    
    // 组件加载时获取数据
    onMounted(() => {
      loadTopics()
      
      // 如果默认标签是帖子管理，则加载帖子
      if (activeTab.value === 'posts') {
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
      deletePost
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