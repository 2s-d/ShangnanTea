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
            <!-- 文章管理区域 -->
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
                  <el-option label="茶文化" value="茶文化" />
                  <el-option label="茶艺茶道" value="茶艺茶道" />
                  <el-option label="茶叶百科" value="茶叶百科" />
                  <el-option label="茶文化传承" value="茶文化传承" />
                </el-select>
                <el-select v-model="articleStatus" placeholder="文章状态" clearable>
                  <el-option label="已发布" :value="1" />
                  <el-option label="草稿" :value="0" />
                  <el-option label="已删除" :value="2" />
                </el-select>
              </div>
              <div class="button-area">
                <el-button type="primary" @click="handleCreateArticle">
                  <el-icon><Plus /></el-icon> 新建文章
                </el-button>
              </div>
            </div>
            
            <!-- 文章列表 -->
            <el-table
              :data="filteredArticles"
              style="width: 100%"
              v-loading="articlesLoading"
              border
            >
              <el-table-column label="ID" prop="id" width="80" />
              <el-table-column label="标题" min-width="200">
                <template #default="scope">
                  <div class="article-title-cell">
                    <el-tag v-if="scope.row.is_top" type="danger" size="small" effect="plain">置顶</el-tag>
                    <el-tag v-if="scope.row.is_recommend" type="success" size="small" effect="plain">推荐</el-tag>
                    <span>{{ scope.row.title }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="分类" prop="category" width="120" />
              <el-table-column label="作者" prop="author" width="120" />
              <el-table-column label="发布时间" width="180">
                <template #default="scope">
                  {{ formatDate(scope.row.publish_time) }}
                </template>
              </el-table-column>
              <el-table-column label="状态" width="100">
                <template #default="scope">
                  <el-tag :type="getStatusType(scope.row.status)">
                    {{ getStatusText(scope.row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="阅读量" prop="view_count" width="100" />
              <el-table-column label="操作" width="250">
                <template #default="scope">
                  <el-button size="small" type="primary" @click="handleEditArticle(scope.row)">编辑</el-button>
                  <el-button size="small" type="success" @click="toggleRecommend(scope.row)" :disabled="scope.row.status !== 1">
                    {{ scope.row.is_recommend ? '取消推荐' : '推荐' }}
                  </el-button>
                  <el-button size="small" type="danger" @click="handleDeleteArticle(scope.row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            
            <!-- 分页 -->
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
        
        <el-tab-pane label="主页区块管理" name="blocks">
          <div class="management-section">
            <!-- 区块管理区域 -->
            <el-alert
              title="区块管理功能允许您配置茶文化主页的各个内容区域"
              type="info"
              :closable="false"
              class="block-info"
            />
            
            <el-table
              :data="homeBlocks"
              style="width: 100%"
              v-loading="blocksLoading"
              border
            >
              <el-table-column label="区块名称" min-width="150">
                <template #default="scope">
                  <div class="block-name">
                    <span>{{ getBlockName(scope.row.section) }}</span>
                    <el-tag size="small" type="info">{{ scope.row.section }}</el-tag>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="标题" prop="title" min-width="150" />
              <el-table-column label="副标题" prop="sub_title" min-width="200" />
              <el-table-column label="显示状态" width="100">
                <template #default="scope">
                  <el-switch
                    v-model="scope.row.status"
                    :active-value="1"
                    :inactive-value="0"
                    @change="toggleBlockStatus(scope.row)"
                  />
                </template>
              </el-table-column>
              <el-table-column label="排序" prop="sort_order" width="80" />
              <el-table-column label="最后更新" width="180">
                <template #default="scope">
                  {{ formatDate(scope.row.update_time) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120">
                <template #default="scope">
                  <el-button size="small" type="primary" @click="handleEditBlock(scope.row)">
                    编辑内容
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
      </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 文章表单对话框 将在下一部分添加 -->
    <el-dialog
      v-model="articleFormVisible"
      :title="articleForm.id ? '编辑文章' : '新建文章'"
      width="70%"
      destroy-on-close
    >
      <el-form :model="articleForm" label-width="100px" :rules="articleRules" ref="articleFormRef">
        <el-form-item label="文章标题" prop="title">
          <el-input v-model="articleForm.title" placeholder="请输入文章标题"></el-input>
        </el-form-item>
        
        <el-form-item label="副标题" prop="subtitle">
          <el-input v-model="articleForm.subtitle" placeholder="请输入副标题"></el-input>
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="作者" prop="author">
              <el-input v-model="articleForm.author" placeholder="请输入作者"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="分类" prop="category">
              <el-select v-model="articleForm.category" placeholder="请选择分类" style="width: 100%">
                <el-option label="茶文化" value="茶文化" />
                <el-option label="茶艺茶道" value="茶艺茶道" />
                <el-option label="茶叶百科" value="茶叶百科" />
                <el-option label="茶文化传承" value="茶文化传承" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="来源" prop="source">
              <el-input v-model="articleForm.source" placeholder="请输入文章来源"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="文章摘要" prop="summary">
          <el-input 
            v-model="articleForm.summary" 
            type="textarea" 
            :rows="3"
            placeholder="请输入文章摘要，将显示在文章列表中"
          ></el-input>
        </el-form-item>
        
        <el-form-item label="封面图片" prop="cover_image">
          <el-input v-model="articleForm.cover_image" placeholder="请输入封面图片URL">
            <template #append>
              <el-button>选择图片</el-button>
            </template>
          </el-input>
          <div class="cover-preview" v-if="articleForm.cover_image">
            <SafeImage :src="articleForm.cover_image" type="post" alt="封面预览" style="width:100%;height:auto;max-height:150px;object-fit:contain;" />
          </div>
        </el-form-item>
        
        <el-form-item label="文章内容" prop="content">
          <el-input 
            v-model="articleForm.content" 
            type="textarea" 
            :rows="10"
            placeholder="请输入文章内容，支持HTML格式"
          ></el-input>
          <div class="form-tip">支持HTML格式，可以添加段落、标题、图片等内容</div>
        </el-form-item>
        
        <el-form-item label="视频链接" prop="video_url">
          <el-input v-model="articleForm.video_url" placeholder="请输入视频链接URL（可选）"></el-input>
        </el-form-item>
        
        <el-form-item label="标签" prop="tags">
          <el-input v-model="articleForm.tags" placeholder="请输入标签，多个标签用英文逗号分隔"></el-input>
          <div class="form-tip">多个标签用英文逗号分隔，如：商南茶,历史,文化</div>
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="发布状态" prop="status">
              <el-radio-group v-model="articleForm.status">
                <el-radio :label="1">发布</el-radio>
                <el-radio :label="0">草稿</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="是否置顶" prop="is_top">
              <el-switch
                v-model="articleForm.is_top"
                :active-value="1"
                :inactive-value="0"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="是否推荐" prop="is_recommend">
              <el-switch
                v-model="articleForm.is_recommend"
                :active-value="1"
                :inactive-value="0"
              />
            </el-form-item>
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
    
    <!-- 区块编辑对话框 将在下一部分添加 -->
    <el-dialog
      v-model="blockFormVisible"
      :title="`编辑区块：${currentBlock ? getBlockName(currentBlock.section) : ''}`"
      width="70%"
      destroy-on-close
    >
      <template v-if="currentBlock">
        <el-form :model="blockForm" label-width="100px" ref="blockFormRef">
          <el-form-item label="区块标题" prop="title">
            <el-input v-model="blockForm.title" placeholder="请输入区块标题"></el-input>
          </el-form-item>
          
          <el-form-item label="副标题" prop="sub_title">
            <el-input v-model="blockForm.sub_title" placeholder="请输入副标题"></el-input>
          </el-form-item>
          
          <el-form-item label="排序" prop="sort_order">
            <el-input-number v-model="blockForm.sort_order" :min="1" :max="100"></el-input-number>
            <div class="form-tip">数字越小排序越靠前</div>
          </el-form-item>
          
          <!-- 轮播图区块编辑 -->
          <template v-if="currentBlock.section === 'banner'">
            <el-divider content-position="left">轮播图内容</el-divider>
            
            <el-form-item>
              <template #label>
                <div class="custom-label">
                  <span>轮播图列表</span>
                  <el-button type="primary" size="small" plain @click="addBannerItem">
                    <el-icon><Plus /></el-icon>
                    添加轮播图
                  </el-button>
                </div>
              </template>
              
              <div v-for="(item, index) in bannerItems" :key="index" class="banner-item">
                <el-card shadow="hover">
                  <div class="banner-header">
                    <span class="banner-index">轮播图 #{{ index + 1 }}</span>
                    <el-button type="danger" size="small" plain @click="removeBannerItem(index)">
                      移除
                    </el-button>
                  </div>
                  
                  <div class="banner-form">
                    <el-row :gutter="20">
                      <el-col :span="14">
                        <el-form-item label="图片URL">
                          <el-input v-model="item.url" placeholder="请输入图片URL"></el-input>
                        </el-form-item>
                      </el-col>
                      <el-col :span="10">
                        <el-form-item label="链接地址">
                          <el-input v-model="item.link" placeholder="点击后跳转的链接"></el-input>
                        </el-form-item>
                      </el-col>
                    </el-row>
                    <el-row :gutter="20">
                      <el-col :span="12">
                        <el-form-item label="标题">
                          <el-input v-model="item.title" placeholder="轮播图标题"></el-input>
                        </el-form-item>
                      </el-col>
                      <el-col :span="12">
                        <el-form-item label="副标题">
                          <el-input v-model="item.subtitle" placeholder="轮播图副标题"></el-input>
                        </el-form-item>
                      </el-col>
                    </el-row>
                  </div>
                </el-card>
              </div>
            </el-form-item>
          </template>
          
          <!-- 推荐茶叶区块编辑 -->
          <template v-else-if="currentBlock.section === 'recommend'">
            <el-divider content-position="left">推荐茶叶内容</el-divider>
            
            <el-form-item>
              <template #label>
                <div class="custom-label">
                  <span>推荐茶叶列表</span>
                  <el-button type="primary" size="small" plain @click="addRecommendItem">
                    <el-icon><Plus /></el-icon>
                    添加推荐茶叶
                  </el-button>
                </div>
              </template>
              
              <div v-for="(item, index) in recommendItems" :key="index" class="recommend-item">
                <el-card shadow="hover">
                  <div class="recommend-header">
                    <span class="recommend-index">茶叶 #{{ index + 1 }}</span>
                    <el-button type="danger" size="small" plain @click="removeRecommendItem(index)">
                      移除
                    </el-button>
                  </div>
                  
                  <div class="recommend-form">
                    <el-row :gutter="20">
                      <el-col :span="8">
                        <el-form-item label="茶叶ID">
                          <el-input v-model="item.id" placeholder="请输入茶叶ID"></el-input>
                        </el-form-item>
                      </el-col>
                      <el-col :span="8">
                        <el-form-item label="茶叶名称">
                          <el-input v-model="item.title" placeholder="请输入茶叶名称"></el-input>
                        </el-form-item>
                      </el-col>
                      <el-col :span="8">
                        <el-form-item label="价格">
                          <el-input v-model="item.price" placeholder="请输入价格"></el-input>
                        </el-form-item>
                      </el-col>
                    </el-row>
                    <el-form-item label="图片URL">
                      <el-input v-model="item.image" placeholder="请输入图片URL"></el-input>
                    </el-form-item>
                  </div>
                </el-card>
              </div>
            </el-form-item>
          </template>
          
          <!-- 其他类型区块的通用内容编辑 -->
          <template v-else>
            <el-form-item label="区块内容" prop="content">
              <el-input 
                v-model="rawBlockContent" 
                type="textarea" 
                :rows="10"
                placeholder="请输入区块内容（JSON格式）"
              ></el-input>
              <div class="form-tip">请使用JSON格式填写区块内容</div>
            </el-form-item>
          </template>
          
          <el-form-item label="显示状态" prop="status">
            <el-switch
              v-model="blockForm.status"
              :active-value="1"
              :inactive-value="0"
            />
          </el-form-item>
        </el-form>
      </template>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="blockFormVisible = false">取消</el-button>
          <el-button type="primary" @click="submitBlockForm" :loading="blockSubmitting">
            保存修改
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { DocumentCopy, Plus, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import SafeImage from '@/components/common/form/SafeImage.vue'

export default {
  name: 'CultureManagerPage',
  components: {
    DocumentCopy,
    Plus,
    Search,
    SafeImage
  },
  setup() {
    const router = useRouter()
    const activeTab = ref('articles')
    
    // ============ 文章管理 ============
    const articleSearch = ref('')
    const articleCategory = ref('')
    const articleStatus = ref('')
    const articlesLoading = ref(false)
    const articleFormRef = ref(null)
    const articleFormVisible = ref(false)
    const articleSubmitting = ref(false)
    
    // 文章表单数据
    const articleForm = ref({
      id: null,
      title: '',
      subtitle: '',
      author: '',
      category: '',
      content: '',
      summary: '',
      cover_image: '',
      images: '',
      video_url: '',
      tags: '',
      source: '',
      status: 1,
      is_top: 0,
      is_recommend: 0
    })
    
    // 文章表单验证规则
    const articleRules = {
      title: [{ required: true, message: '请输入文章标题', trigger: 'blur' }],
      category: [{ required: true, message: '请选择文章分类', trigger: 'change' }],
      content: [{ required: true, message: '请输入文章内容', trigger: 'blur' }]
    }
    
    /**
     * 数据列表（生产形态：不在 UI 层造数据）
     * TODO-SCRIPT: 茶文化内容管理需要后端接口与 Vuex forum 模块（文章列表/区块列表/编辑/保存/推荐/删除）
     */
    const articles = ref([])
    
    const articlePagination = reactive({
      currentPage: 1,
      pageSize: 10,
      total: 0
    })
    
    // 筛选后的文章列表
    const filteredArticles = computed(() => {
      return articles.value.filter(article => {
        let match = true
        
        // 搜索标题
        if (articleSearch.value && !article.title.includes(articleSearch.value)) {
          match = false
        }
        
        // 筛选分类
        if (articleCategory.value && article.category !== articleCategory.value) {
          match = false
        }
        
        // 筛选状态
        if (articleStatus.value !== '' && article.status !== articleStatus.value) {
          match = false
        }
        
        return match
      })
    })
    
    // 根据状态获取标签类型
    const getStatusType = (status) => {
      switch (status) {
        case 1: return 'success'
        case 0: return 'warning'
        case 2: return 'info'
        default: return 'info'
      }
    }
    
    // 根据状态获取文本
    const getStatusText = (status) => {
      switch (status) {
        case 1: return '已发布'
        case 0: return '草稿'
        case 2: return '已删除'
        default: return '未知'
      }
    }
    
    // 创建文章
    const handleCreateArticle = () => {
      articleForm.value = {
        id: null,
        title: '',
        subtitle: '',
        author: '',
        category: '',
        content: '',
        summary: '',
        cover_image: '',
        images: '',
        video_url: '',
        tags: '',
        source: '',
        status: 1,
        is_top: 0,
        is_recommend: 0
      }
      articleFormVisible.value = true
    }
    
    // 编辑文章
    const handleEditArticle = (article) => {
      articleForm.value = { ...article }
      // 如果tags是数组，转换为字符串
      if (Array.isArray(articleForm.value.tags)) {
        articleForm.value.tags = articleForm.value.tags.join(',')
      }
      articleFormVisible.value = true
    }
    
    // 切换推荐状态
    const toggleRecommend = (article) => {
      article.is_recommend = article.is_recommend === 1 ? 0 : 1
      const actionText = article.is_recommend === 1 ? '推荐' : '取消推荐'
      ElMessage.success(`已${actionText}文章: ${article.title}`)
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
      ).then(() => {
        // 模拟删除操作
        article.status = 2 // 标记为已删除
        ElMessage.success('文章已删除')
      }).catch(() => {
        // 取消删除
      })
    }
    
    // 提交文章表单
    const submitArticleForm = () => {
      if (!articleFormRef.value) return
      
      articleFormRef.value.validate(async (valid) => {
        if (!valid) {
          return false
        }
        
        articleSubmitting.value = true
        
        try {
          // TODO-SCRIPT: 提交文章表单需要后端接口与 Vuex forum 模块；不在 UI 层 setTimeout 伪提交/本地创建更新
          ElMessage.info('文章管理功能待后端接口接入')
          return
        } catch (error) {
          ElMessage.error('操作失败，请重试')
        } finally {
          articleSubmitting.value = false
        }
      })
    }
    
    // ============ 区块管理 ============
    const blocksLoading = ref(false)
    const blockFormRef = ref(null)
    const blockFormVisible = ref(false)
    const blockSubmitting = ref(false)
    const currentBlock = ref(null)
    
    // 区块表单数据
    const blockForm = ref({
      title: '',
      sub_title: '',
      sort_order: 1,
      status: 1
    })
    
    // 轮播图项目
    const bannerItems = ref([])
    
    // 推荐项目
    const recommendItems = ref([])
    
    // 原始区块内容
    const rawBlockContent = ref('')
    
    /**
     * 区块数据（生产形态：不在 UI 层造数据）
     * TODO-SCRIPT: 主页区块管理需要后端接口与 Vuex forum 模块
     */
    const homeBlocks = ref([])
    
    // 获取区块友好名称
    const getBlockName = (section) => {
      const blockNames = {
        'banner': '顶部轮播图',
        'recommend': '推荐茶叶',
        'culture_intro': '文化简介',
        'feature': '特色功能',
        'news': '新闻动态'
      }
      return blockNames[section] || section
    }
    
    // 切换区块状态
    const toggleBlockStatus = (block) => {
      const statusText = block.status === 1 ? '显示' : '隐藏'
      ElMessage.success(`区块「${getBlockName(block.section)}」已设为${statusText}`)
    }
    
    // 编辑区块
    const handleEditBlock = (block) => {
      currentBlock.value = block
      blockForm.value = {
        title: block.title,
        sub_title: block.sub_title,
        sort_order: block.sort_order,
        status: block.status
      }
      
      // 解析内容
      if (block.section === 'banner') {
        try {
          bannerItems.value = JSON.parse(block.content)
        } catch (e) {
          bannerItems.value = []
          ElMessage.warning('轮播图数据格式有误，已重置')
        }
      } else if (block.section === 'recommend') {
        try {
          recommendItems.value = JSON.parse(block.content)
        } catch (e) {
          recommendItems.value = []
          ElMessage.warning('推荐茶叶数据格式有误，已重置')
        }
      } else {
        rawBlockContent.value = block.content
      }
      
      blockFormVisible.value = true
    }
    
    // 添加轮播图项目
    const addBannerItem = () => {
      bannerItems.value.push({
        url: '',
        link: '',
        title: '',
        subtitle: ''
      })
    }
    
    // 移除轮播图项目
    const removeBannerItem = (index) => {
      bannerItems.value.splice(index, 1)
    }
    
    // 添加推荐茶叶项目
    const addRecommendItem = () => {
      recommendItems.value.push({
        id: '',
        title: '',
        image: '',
        price: ''
      })
    }
    
    // 移除推荐茶叶项目
    const removeRecommendItem = (index) => {
      recommendItems.value.splice(index, 1)
    }
    
    // 提交区块表单
    const submitBlockForm = () => {
      if (!currentBlock.value) return
      
      blockSubmitting.value = true
      
      try {
        // TODO-SCRIPT: 保存区块需要后端接口；不在 UI 层构造 content/本地更新/延迟伪成功
        ElMessage.info('保存区块功能待后端接口接入')
        blockSubmitting.value = false
        return
      } catch (error) {
        ElMessage.error('操作失败，请重试')
        blockSubmitting.value = false
      }
    }
    
    // ============ 共用函数 ============
    // 格式化日期
    const formatDate = (dateString) => {
      if (!dateString) return ''
      const date = new Date(dateString)
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
    }
    
    // 去论坛管理
    const goToForumManage = () => {
      router.push('/forum/manage')
    }
    
    // 添加默认图片常量（生产形态：不使用 mock-images）
    const defaultCover = ''
    
    // 加载页面数据
    onMounted(() => {
      // 在实际应用中，这里会从API获取数据
      // 现在使用模拟数据
    })
    
    return {
      activeTab,
      articleSearch,
      articleCategory,
      articleStatus,
      articles,
      filteredArticles,
      articlesLoading,
      articlePagination,
      homeBlocks,
      blocksLoading,
      
      getStatusType,
      getStatusText,
      handleCreateArticle,
      handleEditArticle,
      toggleRecommend,
      handleDeleteArticle,
      getBlockName,
      toggleBlockStatus,
      handleEditBlock,
      formatDate,
      goToForumManage,
      
      // 文章表单相关
      submitArticleForm,
      articleFormRef,
      articleFormVisible,
      articleSubmitting,
      articleForm,
      articleRules,
      
      // 区块表单相关
      blockFormRef,
      blockFormVisible,
      blockSubmitting,
      currentBlock,
      blockForm,
      bannerItems,
      recommendItems,
      rawBlockContent,
      submitBlockForm,
      addBannerItem,
      removeBannerItem,
      addRecommendItem,
      removeRecommendItem,
      
      // 添加默认图片常量
      defaultCover
    }
  }
}
</script>

<style lang="scss" scoped>
.culture-manager-page {
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
  
  .page-description {
    font-size: 16px;
    color: var(--el-text-color-secondary);
    margin-bottom: 20px;
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
  border-radius: 4px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.management-section {
  margin-top: 20px;
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    .search-area {
      display: flex;
      gap: 10px;
      flex: 1;
      
      .search-input {
        max-width: 300px;
      }
    }
    
    .button-area {
      display: flex;
      gap: 10px;
    }
  }
}

.article-title-cell {
  display: flex;
  align-items: center;
  gap: 5px;
  
  .el-tag {
    margin-right: 5px;
  }
}

.block-name {
  display: flex;
  align-items: center;
  gap: 8px;
  
  .el-tag {
    margin-left: 5px;
  }
}

.block-info {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.cover-preview {
  margin-top: 10px;
  width: 200px;
  height: 120px;
  overflow: hidden;
  border: 1px solid #eaeaea;
  border-radius: 4px;
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.form-tip {
  color: #909399;
  font-size: 12px;
  margin-top: 5px;
}

.custom-label {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.banner-item, .recommend-item {
  margin-bottom: 15px;
  
  .banner-header, .recommend-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;
    border-bottom: 1px dashed #eaeaea;
    padding-bottom: 10px;
  }
  
  .banner-form, .recommend-form {
    padding: 0 10px;
  }
}

@media (max-width: 768px) {
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    
    .search-area {
      margin-bottom: 10px;
      width: 100%;
      flex-wrap: wrap;
      
      .el-input, .el-select {
        width: 100%;
        margin-bottom: 10px;
      }
    }
  }
}
</style> 