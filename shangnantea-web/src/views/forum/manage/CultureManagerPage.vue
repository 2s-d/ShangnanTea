<template>
  <div class="culture-manager-page">
    <div class="page-header">
      <div class="container">
        <h1 class="page-title">茶文化内容管理</h1>
        <div class="header-actions">
          <el-button type="primary" @click="goToForumManage">
            <el-icon><DocumentCopy /></el-icon>
            论坛内容管理
          </el-button>
        </div>
      </div>
    </div>
    
    <div class="container main-content">
      <el-tabs v-model="activeTab" class="management-tabs">
        <el-tab-pane label="茶文章管理" name="articles">
          <div class="management-section">
            <div class="section-header">
              <div class="search-area">
                <el-input
                  v-model="articleSearch"
                  placeholder="搜索文章标题"
                  class="search-input"
                  clearable
                >
                  <template #prefix>
                    <el-icon><Search /></el-icon>
                  </template>
                </el-input>
                <el-select v-model="articleCategory" placeholder="选择分类" clearable>
                  <el-option
                    v-for="cat in categoryOptions"
                    :key="cat.value"
                    :label="cat.label"
                    :value="cat.value"
                  />
                </el-select>
                <el-select v-model="articleStatus" placeholder="文章状态" clearable>
                  <el-option label="已发布" :value="1" />
                  <el-option label="草稿" :value="0" />
                </el-select>
              </div>
              <div class="button-area">
                <el-button type="primary" @click="handleCreateArticle">
                  <el-icon><Plus /></el-icon> 新建文章
                </el-button>
              </div>
            </div>
            
            <el-table
              :data="filteredArticles"
              style="width: 100%"
              v-loading="articlesLoading"
              border
            >
              <el-table-column label="ID" prop="id" width="80" />
              <el-table-column label="标题" prop="title" min-width="260" />
              <!-- 文章标记列：只显示小标签，不挤占标题 -->
              <el-table-column label="标记" width="120">
                <template #default="scope">
                  <div class="article-flags">
                    <el-tag
                      v-if="scope.row.isTop === 1"
                      size="small"
                      effect="plain"
                      type="danger"
                    >
                      置顶
                    </el-tag>
                    <el-tag
                      v-if="scope.row.isRecommend === 1"
                      size="small"
                      effect="plain"
                      type="success"
                    >
                      推荐
                    </el-tag>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="分类" prop="category" width="120" />
              <el-table-column label="作者" width="120">
                <template #default="scope">
                  {{ scope.row.authorName || scope.row.author || '-' }}
                </template>
              </el-table-column>
              <el-table-column label="发布时间" width="180">
                <template #default="scope">
                  {{ formatDate(scope.row.publishTime) }}
                </template>
              </el-table-column>
              <el-table-column label="状态" width="100">
                <template #default="scope">
                  <el-tag :type="getStatusType(scope.row.status)">
                    {{ getStatusText(scope.row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="阅读量" width="100">
                <template #default="scope">
                  {{ scope.row.viewCount || 0 }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="250" fixed="right">
                <template #default="scope">
                  <el-button size="small" type="primary" @click="handleEditArticle(scope.row)">编辑</el-button>
                  <el-button size="small" type="success" @click="toggleRecommend(scope.row)" :disabled="scope.row.status !== 1">
                    {{ scope.row.isRecommend === 1 ? '取消推荐' : '推荐' }}
                  </el-button>
                  <el-button size="small" type="danger" @click="handleDeleteArticle(scope.row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            
            <div class="pagination-container">
              <el-pagination
                v-model:current-page="articlePagination.currentPage"
                v-model:page-size="articlePagination.pageSize"
                :page-sizes="[10, 20, 50, 100]"
                layout="total, sizes, prev, pager, next, jumper"
                :total="articlePagination.total"
              />
            </div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="分类管理" name="categories">
          <div class="management-section">
            <div class="section-header">
              <h3>文章分类</h3>
              <el-button type="primary" @click="handleAddCategory">
                <el-icon><Plus /></el-icon> 添加分类
              </el-button>
            </div>
            
            <el-table :data="categories" style="width: 100%" border>
              <el-table-column label="分类名称" prop="name" min-width="150" />
              <el-table-column label="描述" prop="description" min-width="200" />
              <el-table-column label="排序" prop="sort_order" width="80" />
              <el-table-column label="文章数量" width="100">
                <template #default="scope">
                  {{ getCategoryArticleCount(scope.row.name) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="180" fixed="right">
                <template #default="scope">
                  <el-button size="small" type="primary" @click="handleEditCategory(scope.row)">编辑</el-button>
                  <el-button size="small" type="danger" @click="handleDeleteCategory(scope.row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="轮播图管理" name="banners">
          <div class="management-section">
            <div class="section-header">
              <h3>首页轮播图</h3>
              <el-button type="primary" @click="handleAddBanner">
                <el-icon><Plus /></el-icon> 添加轮播图
              </el-button>
            </div>
            
            <el-table :data="banners" style="width: 100%" v-loading="bannersLoading" border>
              <el-table-column label="ID" prop="id" width="80" />
              <el-table-column label="预览" width="150">
                <template #default="scope">
                  <el-image 
                    :src="scope.row.image_url" 
                    fit="cover" 
                    style="width: 120px; height: 60px"
                    :preview-src-list="[scope.row.image_url]"
                  />
                </template>
              </el-table-column>
              <el-table-column label="标题" prop="title" min-width="150" />
              <el-table-column label="副标题" prop="subtitle" min-width="200" />
              <el-table-column label="链接" prop="link_url" min-width="200" show-overflow-tooltip />
              <el-table-column label="排序" prop="sort_order" width="80" />
              <el-table-column label="操作" width="180" fixed="right">
                <template #default="scope">
                  <el-button size="small" type="primary" @click="handleEditBanner(scope.row)">编辑</el-button>
                  <el-button size="small" type="danger" @click="handleDeleteBanner(scope.row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 文章表单对话框 -->
    <el-dialog
      v-model="articleFormVisible"
      :title="articleForm.id ? '编辑文章' : '新建文章'"
      width="90%"
      top="5vh"
      destroy-on-close
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form :model="articleForm" label-width="100px" :rules="articleRules" ref="articleFormRef">
        <el-row :gutter="20">
          <el-col :span="16">
            <el-form-item label="文章标题" prop="title">
              <el-input v-model="articleForm.title" placeholder="请输入文章标题" />
            </el-form-item>
            
            <el-form-item label="副标题" prop="subtitle">
              <el-input v-model="articleForm.subtitle" placeholder="请输入副标题" />
            </el-form-item>
            
            <el-form-item label="文章摘要" prop="summary">
              <el-input 
                v-model="articleForm.summary" 
                type="textarea" 
                :rows="3"
                placeholder="请输入文章摘要，将显示在文章列表中"
              />
            </el-form-item>
            
            <el-form-item label="文章内容" prop="content">
              <QuillEditor
                v-model:content="articleForm.content"
                contentType="html"
                :options="editorOptions"
                style="height: 400px"
                @ready="onEditorReady"
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="8">
            <el-card shadow="hover" class="meta-card">
              <template #header>
                <span>文章设置</span>
              </template>
              
              <el-form-item label="作者" prop="author">
                <el-input v-model="articleForm.author" placeholder="请输入作者" />
              </el-form-item>
              
              <el-form-item label="分类" prop="category">
                <el-select v-model="articleForm.category" placeholder="请选择分类" style="width: 100%">
                  <el-option
                    v-for="cat in categoryOptions"
                    :key="cat.value"
                    :label="cat.label"
                    :value="cat.value"
                  />
                </el-select>
              </el-form-item>
              
              <el-form-item label="来源" prop="source">
                <el-input v-model="articleForm.source" placeholder="请输入文章来源" />
              </el-form-item>
              
              <el-form-item label="标签" prop="tags">
                <el-input v-model="articleForm.tags" placeholder="用逗号分隔" />
                <div class="form-tip">多个标签用英文逗号分隔</div>
              </el-form-item>
              
              <el-divider />
              
              <el-form-item label="视频链接" prop="video_url">
                <el-input v-model="articleForm.video_url" placeholder="视频URL（可选）" />
              </el-form-item>
              
              <el-divider />
              
              <el-form-item label="发布状态" prop="status">
                <el-radio-group v-model="articleForm.status">
                  <el-radio :value="1">发布</el-radio>
                  <el-radio :value="0">草稿</el-radio>
                </el-radio-group>
              </el-form-item>
              
              <el-form-item label="置顶" prop="isTop">
                <el-switch v-model="articleForm.isTop" :active-value="1" :inactive-value="0" />
              </el-form-item>
              
              <el-form-item label="推荐" prop="isRecommend">
                <el-switch v-model="articleForm.isRecommend" :active-value="1" :inactive-value="0" />
              </el-form-item>
            </el-card>
          </el-col>
        </el-row>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="articleFormVisible = false">取消</el-button>
          <el-button type="primary" @click="submitArticleForm" :loading="articleSubmitting">
            {{ articleForm.id ? '保存修改' : '创建文章' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 分类表单对话框 -->
    <el-dialog
      v-model="categoryFormVisible"
      :title="categoryForm.id ? '编辑分类' : '添加分类'"
      width="500px"
      destroy-on-close
    >
      <el-form :model="categoryForm" label-width="100px" ref="categoryFormRef">
        <el-form-item label="分类名称" required>
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称" />
        </el-form-item>
        
        <el-form-item label="分类描述">
          <el-input v-model="categoryForm.description" type="textarea" :rows="3" placeholder="请输入分类描述" />
        </el-form-item>
        
        <el-form-item label="排序">
          <el-input-number v-model="categoryForm.sort_order" :min="1" :max="100" />
          <div class="form-tip">数字越小排序越靠前</div>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="categoryFormVisible = false">取消</el-button>
          <el-button type="primary" @click="submitCategoryForm" :loading="categorySubmitting">
            {{ categoryForm.id ? '保存修改' : '添加' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- Banner表单对话框 -->
    <el-dialog
      v-model="bannerFormVisible"
      :title="bannerForm.id ? '编辑轮播图' : '添加轮播图'"
      width="600px"
      destroy-on-close
    >
      <el-form :model="bannerForm" label-width="100px" ref="bannerFormRef" class="banner-form">
        <el-form-item label="轮播图片" required>
          <el-upload
            class="banner-uploader"
            :show-file-list="false"
            :http-request="handleBannerImageUpload"
            accept="image/*"
          >
            <img v-if="bannerForm.image_url" :src="bannerForm.image_url" class="banner-image" />
            <el-icon v-else class="banner-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="form-tip">建议尺寸: 1920x600px</div>
        </el-form-item>
        
        <el-form-item label="标题">
          <el-input v-model="bannerForm.title" placeholder="请输入标题" />
        </el-form-item>
        
        <el-form-item label="副标题">
          <el-input v-model="bannerForm.subtitle" placeholder="请输入副标题" />
        </el-form-item>
        
        <el-form-item label="链接地址">
          <el-input v-model="bannerForm.link_url" placeholder="点击后跳转的链接" />
        </el-form-item>
        
        <el-form-item label="排序">
          <el-input-number v-model="bannerForm.sort_order" :min="1" :max="100" />
          <div class="form-tip">数字越小排序越靠前</div>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="bannerFormVisible = false">取消</el-button>
          <el-button type="primary" @click="submitBannerForm" :loading="bannerSubmitting">
            {{ bannerForm.id ? '保存修改' : '添加' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useForumStore } from '@/stores/forum'
import { DocumentCopy, Plus, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css'
import forumAPI from '@/api/forum'
import { showByCode, isSuccess } from '@/utils/apiMessages'

const router = useRouter()
const forumStore = useForumStore()

// Tab 状态
const activeTab = ref('articles')

// 跳转到论坛管理
const goToForumManage = () => {
  router.push('/forum/manage')
}

// ============ 文章管理 ============
const articleSearch = ref('')
const articleCategory = ref('')
const articleStatus = ref('')
const articlesLoading = ref(false)
const articleFormRef = ref(null)
const articleFormVisible = ref(false)
const articleSubmitting = ref(false)

// Quill 编辑器配置
const editorOptions = {
  modules: {
    toolbar: {
      container: [
        [{ 'header': [1, 2, 3, 4, 5, 6, false] }],
        ['bold', 'italic', 'underline', 'strike'],
        [{ 'list': 'ordered'}, { 'list': 'bullet' }],
        [{ 'color': [] }, { 'background': [] }],
        [{ 'align': [] }],
        ['link', 'image'],
        ['clean']
      ],
      handlers: {
        image: imageHandler
      }
    }
  },
  placeholder: '请输入文章内容...',
  theme: 'snow'
}

let quillInstance = null

// 编辑器准备完成
const onEditorReady = (quill) => {
  quillInstance = quill
}

// 对话框关闭时重置 quillInstance
const handleDialogClose = () => {
  // 延迟重置，确保所有操作完成
  setTimeout(() => {
    quillInstance = null
  }, 100)
}

// 自定义图片上传处理
function imageHandler() {
  const input = document.createElement('input')
  input.setAttribute('type', 'file')
  input.setAttribute('accept', 'image/*')
  input.click()
  
  input.onchange = async () => {
    const file = input.files[0]
    if (!file) return
    
    // 检查 quillInstance 是否存在（防止组件已销毁）
    if (!quillInstance) {
      ElMessage.warning('编辑器未准备好，请稍后再试')
      return
    }
    
    try {
      const res = await forumAPI.uploadArticleImage(file)
      if (res.code === 6029 && res.data && res.data.url) {
        // 再次检查 quillInstance（防止异步操作期间组件被销毁）
        if (!quillInstance) {
          ElMessage.warning('编辑器已关闭，图片上传成功但未插入')
          return
        }
        const range = quillInstance.getSelection(true)
        const imageUrl = res.data.url
        
        // 确保URL是完整的（如果后端返回的是相对路径，需要补全）
        let fullImageUrl = imageUrl
        if (!imageUrl.startsWith('http://') && !imageUrl.startsWith('https://')) {
          // 如果是相对路径，可能需要补全基础URL
          // 这里假设后端已经返回了完整URL，如果不行再调整
          console.warn('图片URL可能是相对路径:', imageUrl)
        }
        
        if (range && range.index !== null) {
          // 使用 insertEmbed 插入图片
          quillInstance.insertEmbed(range.index, 'image', fullImageUrl)
          quillInstance.setSelection(range.index + 1)
        } else {
          // 如果没有选中位置，插入到末尾
          const length = quillInstance.getLength()
          quillInstance.insertEmbed(length - 1, 'image', fullImageUrl)
        }
        
        // 强制更新编辑器内容
        quillInstance.update()
        ElMessage.success('图片上传成功')
      } else {
        ElMessage.error('图片上传失败')
      }
    } catch (error) {
      console.error('图片上传失败:', error)
      ElMessage.error('图片上传失败')
    }
  }
}

// 文章表单数据
const articleForm = reactive({
  id: null,
  title: '',
  subtitle: '',
  author: '',
  category: '',
  content: '',
  summary: '',
  video_url: '',
  tags: '',
  source: '',
  status: 1,
  isTop: 0,
  isRecommend: 0
})

// 文章表单验证规则
const articleRules = {
  title: [{ required: true, message: '请输入文章标题', trigger: 'blur' }],
  category: [{ required: true, message: '请选择文章分类', trigger: 'change' }],
  content: [{ required: true, message: '请输入文章内容', trigger: 'blur' }]
}

// 文章分页
const articlePagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 从 store 获取文章列表
const articles = computed(() => forumStore.articles || [])

// 筛选后的文章列表
const filteredArticles = computed(() => {
  let result = articles.value
  
  if (articleSearch.value) {
    result = result.filter(article => 
      article.title.includes(articleSearch.value)
    )
  }
  
  if (articleCategory.value) {
    result = result.filter(article => 
      article.category === articleCategory.value
    )
  }
  
  if (articleStatus.value !== '') {
    result = result.filter(article => 
      article.status === articleStatus.value
    )
  }
  
  articlePagination.total = result.length
  return result
})

// 获取状态标签类型
const getStatusType = (status) => {
  const types = { 0: 'warning', 1: 'success', 2: 'info' }
  return types[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const texts = { 0: '草稿', 1: '已发布', 2: '已删除' }
  return texts[status] || '未知'
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 创建文章
const handleCreateArticle = () => {
  Object.assign(articleForm, {
    id: null,
    title: '',
    subtitle: '',
    author: '',
    category: '',
    content: '',
    summary: '',
    video_url: '',
    tags: '',
    source: '',
    status: 1,
    isTop: 0,
    isRecommend: 0
  })
  articleFormVisible.value = true
}

// 编辑文章
const handleEditArticle = async (article) => {
  try {
    // 编辑时需要调用详情接口获取完整数据（包括 content）
    const res = await forumStore.fetchArticleDetail(article.id)
    if (res && res.data) {
      const detail = res.data
      Object.assign(articleForm, {
        id: detail.id,
        title: detail.title || '',
        subtitle: detail.subtitle || '',
        author: detail.authorName || detail.author || '',
        category: detail.category || '',
        content: detail.content || '',
        summary: detail.summary || '',
        video_url: detail.videoUrl || detail.video_url || '',
        tags: Array.isArray(detail.tags) ? detail.tags.join(',') : (detail.tags || ''),
        source: detail.source || '',
        status: detail.status !== undefined ? detail.status : 1,
        // 详情接口使用 isTop / isRecommend
        isTop: detail.isTop !== undefined ? detail.isTop : 0,
        isRecommend: detail.isRecommend !== undefined ? detail.isRecommend : 0
      })
    } else {
      // 如果详情接口失败，使用列表数据（但可能没有 content）
      Object.assign(articleForm, {
        ...article,
        author: article.authorName || '',
        video_url: article.videoUrl || article.video_url || '',
        tags: Array.isArray(article.tags) ? article.tags.join(',') : (article.tags || ''),
        status: article.status !== undefined ? article.status : 1,
        // 列表接口使用 isTop / isRecommend
        isTop: article.isTop !== undefined ? article.isTop : 0,
        isRecommend: article.isRecommend !== undefined ? article.isRecommend : 0
      })
    }
    articleFormVisible.value = true
  } catch (error) {
    console.error('获取文章详情失败:', error)
    ElMessage.error('获取文章详情失败，请重试')
  }
}

// 切换推荐状态
const toggleRecommend = async (article) => {
  try {
    const currentStatus = article.isRecommend !== undefined ? article.isRecommend : 0
    const newStatus = currentStatus === 1 ? 0 : 1
    const res = await forumStore.updateArticle({
      id: article.id,
      data: { isRecommend: newStatus }
    })
    showByCode(res.code)
    if (isSuccess(res.code)) {
      await fetchArticles()
    }
  } catch (error) {
    console.error('切换推荐状态失败:', error)
    ElMessage.error('操作失败')
  }
}

// 删除文章
const handleDeleteArticle = (article) => {
  ElMessageBox.confirm(
    `确定要删除文章 "${article.title}" 吗？`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await forumStore.deleteArticle(article.id)
      showByCode(res.code)
      await fetchArticles()
    } catch (error) {
      console.error('删除文章失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 提交文章表单
const submitArticleForm = () => {
  if (!articleFormRef.value) return
  
  articleFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    articleSubmitting.value = true
    
    try {
      const formData = { ...articleForm }
      
      // 处理标签
      if (typeof formData.tags === 'string') {
        formData.tags = formData.tags.split(',').map(tag => tag.trim()).filter(tag => tag)
      }
      
      // 字段名映射：前端表单字段 -> 后端API字段
      const apiData = {
        title: formData.title,
        subtitle: formData.subtitle,
        content: formData.content,
        summary: formData.summary,
        category: formData.category,
        tags: formData.tags,
        source: formData.source,
        author: formData.author,
        videoUrl: formData.video_url,
        status: formData.status,
        isTop: formData.isTop,
        isRecommend: formData.isRecommend
      }
      
      // 调试：打印提交的数据
      console.log('提交文章数据:', {
        id: formData.id,
        title: apiData.title,
        contentLength: apiData.content ? apiData.content.length : 0,
        hasImages: apiData.content ? apiData.content.includes('<img') : false,
        contentPreview: apiData.content ? apiData.content.substring(0, 200) : ''
      })
      
      let res
      try {
        if (formData.id) {
          res = await forumStore.updateArticle({ id: formData.id, data: apiData })
        } else {
          res = await forumStore.createArticle(apiData)
        }
        
        console.log('文章操作结果:', res)
        showByCode(res.code)
        
        if (isSuccess(res.code)) {
          articleFormVisible.value = false
          await fetchArticles()
        } else {
          // 失败时显示详细错误信息
          console.error('文章操作失败:', res)
          ElMessage.error(res.message || '操作失败，请检查控制台日志')
        }
      } catch (error) {
        console.error('文章操作异常:', error)
        ElMessage.error('操作失败: ' + (error.message || '未知错误'))
        throw error
      }
    } catch (error) {
      console.error('提交文章失败:', error)
      ElMessage.error('提交失败')
    } finally {
      articleSubmitting.value = false
    }
  })
}


// 获取文章列表
const fetchArticles = async () => {
  articlesLoading.value = true
  try {
    await forumStore.fetchArticles()
  } catch (error) {
    console.error('获取文章列表失败:', error)
  } finally {
    articlesLoading.value = false
  }
}

// ============ 轮播图管理 ============
const bannersLoading = ref(false)
const bannerFormRef = ref(null)
const bannerFormVisible = ref(false)
const bannerSubmitting = ref(false)

// 轮播图表单数据
const bannerForm = reactive({
  id: null,
  image_url: '',
  title: '',
  subtitle: '',
  link_url: '',
  sort_order: 1
})

// 从 store 获取轮播图列表
const banners = computed(() => forumStore.banners || [])

// 添加轮播图
const handleAddBanner = () => {
  Object.assign(bannerForm, {
    id: null,
    image_url: '',
    title: '',
    subtitle: '',
    link_url: '',
    sort_order: 1
  })
  bannerFormVisible.value = true
}

// 编辑轮播图
const handleEditBanner = (banner) => {
  Object.assign(bannerForm, banner)
  bannerFormVisible.value = true
}

// 删除轮播图
const handleDeleteBanner = (banner) => {
  ElMessageBox.confirm(
    `确定要删除轮播图 "${banner.title}" 吗？`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await forumStore.deleteBanner(banner.id)
      showByCode(res.code)
      await fetchBanners()
    } catch (error) {
      console.error('删除轮播图失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 轮播图图片上传（仅本地预览 + 保存文件，真正上传在提交表单时一起完成）
const bannerUploadFile = ref(null)

const handleBannerImageUpload = async ({ file }) => {
  try {
    bannerUploadFile.value = file
    // 使用本地URL进行预览，避免提前上传
    bannerForm.image_url = URL.createObjectURL(file)
  } catch (error) {
    console.error('处理轮播图图片失败:', error)
    ElMessage.error('处理轮播图图片失败')
  }
}

// 提交轮播图表单
const submitBannerForm = async () => {
  if (!bannerUploadFile.value) {
    ElMessage.warning('请上传轮播图片')
    return
  }
  
  bannerSubmitting.value = true
  
  try {
    let res
    if (bannerForm.id) {
      res = await forumStore.updateBanner({ id: bannerForm.id, data: bannerForm })
    } else {
      const formData = new FormData()
      formData.append('file', bannerUploadFile.value)
      formData.append('title', bannerForm.title || '')
      if (bannerForm.subtitle) {
      formData.append('subtitle', bannerForm.subtitle)
      }
      if (bannerForm.link_url) {
      formData.append('linkUrl', bannerForm.link_url)
      }
      // sortOrder 在后端初始可以使用默认值，如需调整顺序用“排序拖拽+保存顺序”接口
      res = await forumStore.uploadBanner(formData)
    }
    
    showByCode(res.code)
    bannerFormVisible.value = false
    await fetchBanners()
  } catch (error) {
    console.error('提交轮播图失败:', error)
    ElMessage.error('提交失败')
  } finally {
    bannerSubmitting.value = false
  }
}

// 获取轮播图列表
const fetchBanners = async () => {
  bannersLoading.value = true
  try {
    await forumStore.fetchBanners()
  } catch (error) {
    console.error('获取轮播图列表失败:', error)
  } finally {
    bannersLoading.value = false
  }
}

// ============ 分类管理 ============
const categoryFormRef = ref(null)
const categoryFormVisible = ref(false)
const categorySubmitting = ref(false)

// 分类表单数据
const categoryForm = reactive({
  id: null,
  name: '',
  description: '',
  sort_order: 1
})

// 分类列表（从 home_contents 中读取 section='category' 的数据）
const categories = ref([])

// 分类下拉选项：优先使用后端配置（home_contents section='category'），为空时兜底固定四类
const categoryOptions = computed(() => {
  const fromApi = (categories.value || [])
    .filter(item => item && item.status !== 0)
    .sort((a, b) => (a.sort_order || 0) - (b.sort_order || 0))
    .map(item => ({ label: item.name, value: item.name }))

  if (fromApi.length > 0) {
    return fromApi
  }

  // 兜底：避免后端还未初始化分类导致无法创建/筛选文章
  return [
    { label: '茶叶历史', value: '茶叶历史' },
    { label: '茶艺茶道', value: '茶艺茶道' },
    { label: '茶叶百科', value: '茶叶百科' },
    { label: '茶文化传承', value: '茶文化传承' }
  ]
})

// 获取分类列表
const fetchCategories = async () => {
  try {
    const response = await forumAPI.getCategories()
    if (response.code === 200) {
      categories.value = response.data || []
    }
  } catch (error) {
    console.error('获取分类列表失败:', error)
    ElMessage.error('获取分类列表失败')
  }
}

// 获取分类的文章数量
const getCategoryArticleCount = (categoryName) => {
  return articles.value.filter(article => article.category === categoryName).length
}

// 添加分类
const handleAddCategory = () => {
  Object.assign(categoryForm, {
    id: null,
    name: '',
    description: '',
    sort_order: categories.value.length + 1
  })
  categoryFormVisible.value = true
}

// 编辑分类
const handleEditCategory = (category) => {
  Object.assign(categoryForm, category)
  categoryFormVisible.value = true
}

// 删除分类
const handleDeleteCategory = async (category) => {
  const articleCount = getCategoryArticleCount(category.name)
  
  if (articleCount > 0) {
    ElMessage.warning(`该分类下还有 ${articleCount} 篇文章，无法删除`)
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除分类 "${category.name}" 吗？`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await forumAPI.deleteCategory(category.id)
    showByCode(response.code)
    if (isSuccess(response.code)) {
      await fetchCategories()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除分类失败:', error)
    }
  }
}

// 提交分类表单
const submitCategoryForm = async () => {
  if (!categoryForm.name) {
    ElMessage.warning('请输入分类名称')
    return
  }
  
  categorySubmitting.value = true
  
  try {
    const data = {
      name: categoryForm.name,
      description: categoryForm.description,
      sort_order: categoryForm.sort_order
    }
    
    let response
    if (categoryForm.id) {
      // 编辑
      response = await forumAPI.updateCategory(categoryForm.id, data)
    } else {
      // 新增
      response = await forumAPI.createCategory(data)
    }
    
    showByCode(response.code)
    if (isSuccess(response.code)) {
      categoryFormVisible.value = false
      await fetchCategories()
    }
  } catch (error) {
    console.error('提交分类失败:', error)
  } finally {
    categorySubmitting.value = false
  }
}

// 页面加载时获取数据
onMounted(async () => {
  await Promise.all([
    fetchArticles(),
    fetchBanners(),
    fetchCategories()
  ])
})
</script>

<style lang="scss" scoped>
.culture-manager-page {
  min-height: 100vh;
  background-color: #f5f7fa;
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
    
    h3 {
      margin: 0;
      font-size: 18px;
      font-weight: 600;
      color: #303133;
    }
    
    .search-area {
      display: flex;
      gap: 12px;
      flex: 1;
      flex-wrap: wrap;
      
      .search-input {
        width: 280px;
      }
      
      .el-select {
        width: 160px;
      }
    }
    
    .button-area {
      display: flex;
      gap: 12px;
    }
  }
}

.article-title-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

// 文章标记小标签（置顶 / 推荐）
.article-flags {
  display: flex;
  align-items: center;
  gap: 6px;

  .el-tag {
    padding: 0 6px;
    font-size: 12px;
    line-height: 18px;
  }
}

.pagination-container {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}

// 文章表单样式
.meta-card {
  :deep(.el-card__header) {
    background-color: #f5f7fa;
    font-weight: 600;
    font-size: 16px;
  }
  
  .el-form-item {
    margin-bottom: 20px;
  }
}

.form-tip {
  color: #909399;
  font-size: 12px;
  margin-top: 4px;
  line-height: 1.5;
}

// Banner上传样式
.banner-uploader {
  :deep(.el-upload) {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: all 0.3s;
    width: 100%;
    height: 200px;
    display: flex;
    align-items: center;
    justify-content: center;
    
    &:hover {
      border-color: #409eff;
    }
  }
  
  .banner-image {
    width: 100%;
    height: 200px;
    object-fit: cover;
    display: block;
  }
  
  .banner-uploader-icon {
    font-size: 32px;
    color: #8c939d;
  }
}

// Banner 表单整体布局优化，避免表单内容区域“歪斜”
.banner-form {
  :deep(.el-form-item__content) {
    display: block;         // 内容垂直堆叠：上传区域在上，提示文字在下
    width: 100%;
  }
}

// Quill编辑器样式优化
:deep(.ql-container) {
  min-height: 400px;
  font-size: 14px;
  
  .ql-editor {
    min-height: 400px;
    
    &.ql-blank::before {
      color: #c0c4cc;
      font-style: normal;
    }
  }
}

:deep(.ql-toolbar) {
  border-top-left-radius: 4px;
  border-top-right-radius: 4px;
  background-color: #fafafa;
}

:deep(.ql-container) {
  border-bottom-left-radius: 4px;
  border-bottom-right-radius: 4px;
}

// 响应式设计
@media (max-width: 1200px) {
  .page-header .page-title {
    font-size: 28px;
  }
  
  .main-content {
    max-width: 100%;
  }
}

@media (max-width: 768px) {
  .page-header {
    padding: 30px 0;
    
    .page-title {
      font-size: 24px;
    }
  }
  
  .management-section .section-header {
    flex-direction: column;
    align-items: stretch;
    
    .search-area {
      width: 100%;
      
      .search-input,
      .el-select {
        width: 100%;
      }
    }
    
    .button-area {
      width: 100%;
      
      .el-button {
        flex: 1;
      }
    }
  }
  
  .el-table {
    font-size: 12px;
  }
  
  :deep(.el-dialog) {
    width: 95% !important;
    margin: 5vh auto !important;
  }
}

// 表格优化
:deep(.el-table) {
  .el-button + .el-button {
    margin-left: 8px;
  }
  
  .el-tag + .el-tag {
    margin-left: 4px;
  }
}

// 对话框优化
:deep(.el-dialog) {
  border-radius: 8px;
  
  .el-dialog__header {
    padding: 20px 24px;
    border-bottom: 1px solid #ebeef5;
    
    .el-dialog__title {
      font-size: 18px;
      font-weight: 600;
    }
  }
  
  .el-dialog__body {
    padding: 24px;
  }
  
  .el-dialog__footer {
    padding: 16px 24px;
    border-top: 1px solid #ebeef5;
  }
}

// 按钮优化
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

// 卡片优化
.el-card {
  border-radius: 8px;
  border: 1px solid #ebeef5;
  
  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  }
}
</style>
