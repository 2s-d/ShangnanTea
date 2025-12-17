<template>
  <div class="user-manage-page">
    <div class="page-header">
    <div class="container">
      <h1 class="page-title">用户管理</h1>
        <div class="page-desc">管理平台注册用户，设置用户权限和状态</div>
      </div>
    </div>
    
    <div class="container main-content">
      <!-- 搜索和筛选区域 -->
      <div class="toolbar">
        <div class="filter-group">
          <el-input
            v-model="searchQuery"
            placeholder="搜索用户名/昵称/手机号"
            clearable
            class="search-input"
            @keyup.enter="handleSearch">
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
            <template #append>
              <el-button @click="handleSearch">搜索</el-button>
            </template>
          </el-input>
          
          <el-select v-model="roleFilter" placeholder="用户角色" clearable>
            <el-option label="全部角色" value=""></el-option>
            <el-option v-for="role in roleOptions" :key="role.value" :label="role.label" :value="role.value"></el-option>
          </el-select>
          
          <el-select v-model="statusFilter" placeholder="用户状态" clearable>
            <el-option label="全部状态" value=""></el-option>
            <el-option label="正常" value="1"></el-option>
            <el-option label="禁用" value="0"></el-option>
          </el-select>
          
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon> 筛选
          </el-button>
          
          <el-button @click="resetFilters">
            <el-icon><RefreshRight /></el-icon> 重置
          </el-button>
        </div>
        
        <div class="action-group">
          <el-button type="success" @click="handleExportUsers">
            <el-icon><Download /></el-icon> 导出数据
          </el-button>
          <el-button type="primary" @click="handleMerchantRequests">
            <el-icon><Check /></el-icon> 商家认证请求
          </el-button>
        </div>
      </div>
      
      <!-- 用户列表表格 -->
      <el-card class="table-card">
        <div class="card-header">
          <div class="header-title">用户列表</div>
          <div class="header-actions">
            <el-button type="primary" @click="showAddUserDialog">
              <el-icon><Plus /></el-icon> 添加用户
            </el-button>
          </div>
        </div>
        
        <el-table
          :data="userList"
          style="width: 100%"
          v-loading="loading"
          border
          stripe
          row-key="id">
          
          <el-table-column type="index" width="60" align="center" label="序号"></el-table-column>
          
          <el-table-column prop="username" label="用户名" min-width="110" show-overflow-tooltip>
            <template #default="scope">
              <div class="user-cell">
                <SafeImage :src="scope.row.avatar" type="avatar" :alt="scope.row.username" style="width:30px;height:30px;border-radius:50%;" />
                <span class="username">{{ scope.row.username }}</span>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column prop="nickname" label="昵称" min-width="120" show-overflow-tooltip></el-table-column>
          
          <el-table-column prop="phone" label="手机号" min-width="120" show-overflow-tooltip></el-table-column>
          
          <el-table-column prop="email" label="邮箱" min-width="150" show-overflow-tooltip></el-table-column>
          
          <el-table-column prop="roleName" label="角色" width="100" align="center">
            <template #default="scope">
              <el-tag :type="getRoleTagType(scope.row.role)" effect="plain">
                {{ scope.row.roleName }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="status" label="状态" width="80" align="center">
            <template #default="scope">
              <el-switch
                v-model="scope.row.status"
                :active-value="1"
                :inactive-value="0"
                @change="handleStatusChange(scope.row)"
                :disabled="scope.row.role === 1">
              </el-switch>
            </template>
          </el-table-column>
          
          <el-table-column prop="registerTime" label="注册时间" min-width="140" show-overflow-tooltip>
            <template #default="scope">
              {{ formatDate(scope.row.registerTime) }}
            </template>
          </el-table-column>
          
          <el-table-column prop="lastLoginTime" label="最后登录" min-width="140" show-overflow-tooltip>
            <template #default="scope">
              {{ formatDate(scope.row.lastLoginTime) }}
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="scope">
              <el-button
                size="small"
                type="primary"
                @click="handleEdit(scope.row)"
                :disabled="scope.row.id === 1 && currentUser.id !== 1">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button
                size="small"
                type="danger"
                @click="handleDelete(scope.row)"
                :disabled="scope.row.id === 1 || currentUser.id === scope.row.id">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 分页控件 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange">
          </el-pagination>
        </div>
      </el-card>
    </div>
    
    <!-- 添加/编辑用户对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑用户' : '添加用户'"
      width="500px">
      <el-form
        ref="userFormRef"
        :model="userForm"
        :rules="userRules"
        label-width="80px"
        label-position="right">
        
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" :disabled="isEdit"></el-input>
        </el-form-item>
        
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="userForm.nickname"></el-input>
        </el-form-item>
        
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="userForm.password" type="password" show-password></el-input>
        </el-form-item>
        
        <el-form-item label="确认密码" prop="confirmPassword" v-if="!isEdit">
          <el-input v-model="userForm.confirmPassword" type="password" show-password></el-input>
        </el-form-item>
        
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email"></el-input>
        </el-form-item>
        
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone"></el-input>
        </el-form-item>
        
        <el-form-item label="角色" prop="role">
          <el-select v-model="userForm.role" placeholder="请选择角色" style="width: 100%">
            <el-option
              v-for="role in roleOptions"
              :key="role.value"
              :label="role.label"
              :value="role.value"
              :disabled="role.value === 1 && currentUser.role !== 1">
            </el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="状态" prop="status">
          <el-select v-model="userForm.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="正常" :value="1"></el-option>
            <el-option label="禁用" :value="0"></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="头像" prop="avatar">
          <el-upload
            class="avatar-uploader"
            action="#"
            :http-request="uploadAvatar"
            :show-file-list="false"
            :before-upload="beforeAvatarUpload">
            <SafeImage v-if="userForm.avatar" :src="userForm.avatar" type="avatar" alt="用户头像" class="avatar" style="width:100px;height:100px;border-radius:50%;object-fit:cover;" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitUserForm">确认</el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 删除确认对话框 -->
    <el-dialog
      v-model="deleteDialogVisible"
      title="删除用户"
      width="400px">
      <div class="delete-confirm">
        <el-icon class="warning-icon"><WarningFilled /></el-icon>
        <p>确定要删除用户 "{{ selectedUser?.username || '' }}" 吗？</p>
        <p class="warning-text">删除后不可恢复，该用户的所有数据将被清除。</p>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="deleteDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="confirmDelete">确认删除</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted, getCurrentInstance } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Search, Plus, Edit, Delete, Download, RefreshRight, WarningFilled, Check, View
} from '@element-plus/icons-vue'
import SafeImage from '@/components/common/form/SafeImage.vue'

export default {
  name: 'UserManagePage',
  components: {
    Search, Plus, Edit, Delete, Download, RefreshRight, WarningFilled, Check, SafeImage
  },
  setup() {
    const router = useRouter()
    const userFormRef = ref(null)
    
    // 用户列表数据
    const loading = ref(false)
    const userList = ref([])
    const total = ref(0)
    const currentPage = ref(1)
    const pageSize = ref(10)
    const searchQuery = ref('')
    const roleFilter = ref('')
    const statusFilter = ref('')
    const avatarImage = '/mock-images/avatar-default.jpg'
    
    // 对话框控制
    const dialogVisible = ref(false)
    const deleteDialogVisible = ref(false)
    const isEdit = ref(false)
    const selectedUser = ref(null)
    
    // 当前登录用户
    const currentUser = reactive({
      id: 1,
      role: 1 // 1: 管理员
    })
    
    // 角色选项
    const roleOptions = [
      { value: 1, label: '管理员' },
      { value: 2, label: '普通用户' },
      { value: 3, label: '商家' }
    ]
    
    // 表单初始数据
    const initialUserForm = {
      id: '',
      username: '',
      nickname: '',
      password: '',
      confirmPassword: '',
      email: '',
      phone: '',
      role: 2,
      status: 1,
      avatar: ''
    }
    
    // 用户表单数据
    const userForm = reactive({...initialUserForm})
    
    // 表单验证规则
    const userRules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
      ],
      nickname: [
        { required: true, message: '请输入昵称', trigger: 'blur' },
        { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, message: '请确认密码', trigger: 'blur' },
        { 
          validator: (rule, value, callback) => {
            if (value !== userForm.password) {
              callback(new Error('两次输入密码不一致'));
            } else {
              callback();
            }
          }, 
          trigger: 'blur' 
        }
      ],
      email: [
        { required: true, message: '请输入邮箱', trigger: 'blur' },
        { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
      ],
      phone: [
        { required: true, message: '请输入手机号', trigger: 'blur' },
        { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
      ],
      role: [
        { required: true, message: '请选择用户角色', trigger: 'change' }
      ],
      status: [
        { required: true, message: '请选择用户状态', trigger: 'change' }
      ]
    }
    
    // 获取用户列表
    const fetchUserList = async () => {
      loading.value = true
      try {
        // 在实际项目中，这里应该调用后端API
        // 模拟API响应
        const response = getMockUserList()
        userList.value = response.data.list
        total.value = response.data.total
      } catch (error) {
        console.error('获取用户列表失败：', error)
        ElMessage.error('获取用户列表失败，请稍后重试')
      } finally {
        loading.value = false
      }
    }
    
    // 模拟获取用户列表数据
    const getMockUserList = () => {
      // 生成模拟数据
      const list = []
      const total = 25
      
      for (let i = 1; i <= total; i++) {
        const role = i === 1 ? 1 : (i % 5 === 0 ? 3 : 2)
        
        list.push({
          id: i,
          username: `user${i}`,
          nickname: `用户${i}`,
          email: `user${i}@example.com`,
          phone: `1380000${(10000 + i).toString().substring(1)}`,
          role: role,
          roleName: role === 1 ? '管理员' : (role === 3 ? '商家' : '普通用户'),
          status: i % 7 === 0 ? 0 : 1,
          registerTime: getRandomDate(new Date(2022, 0, 1), new Date()),
          lastLoginTime: getRandomDate(new Date(2023, 0, 1), new Date()),
          avatar: i % 3 === 0 ? `https://via.placeholder.com/80x80?text=User${i}` : ''
        })
      }
      
      // 应用筛选条件
      let filteredList = [...list]
      
      if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase()
        filteredList = filteredList.filter(user => 
          user.username.toLowerCase().includes(query) || 
          user.nickname.toLowerCase().includes(query) || 
          user.phone.includes(query)
        )
      }
      
      if (roleFilter.value) {
        filteredList = filteredList.filter(user => user.role === Number(roleFilter.value))
      }
      
      if (statusFilter.value !== '') {
        filteredList = filteredList.filter(user => user.status === Number(statusFilter.value))
      }
      
      // 分页
      const start = (currentPage.value - 1) * pageSize.value
      const end = start + pageSize.value
      const pagedList = filteredList.slice(start, end)
      
      return {
        code: 0,
        data: {
          list: pagedList,
          total: filteredList.length
        }
      }
    }
    
    // 获取随机日期
    const getRandomDate = (start, end) => {
      return new Date(start.getTime() + Math.random() * (end.getTime() - start.getTime()))
    }
    
    // 格式化日期
    const formatDate = (date) => {
      if (!date) return ''
      
      const d = new Date(date)
      return `${d.getFullYear()}-${(d.getMonth() + 1).toString().padStart(2, '0')}-${d.getDate().toString().padStart(2, '0')} ${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
    }
    
    // 获取角色标签样式
    const getRoleTagType = (role) => {
      switch (role) {
        case 1: return 'danger'
        case 3: return 'success'
        default: return 'info'
      }
    }
    
    // 搜索用户
    const handleSearch = () => {
      currentPage.value = 1
      fetchUserList()
    }
    
    // 重置筛选
    const resetFilters = () => {
      searchQuery.value = ''
      roleFilter.value = ''
      statusFilter.value = ''
      currentPage.value = 1
      fetchUserList()
    }
    
    // 分页处理
    const handleSizeChange = (size) => {
      pageSize.value = size
      fetchUserList()
    }
    
    const handleCurrentChange = (page) => {
      currentPage.value = page
      fetchUserList()
    }
    
    // 添加用户
    const showAddUserDialog = () => {
      isEdit.value = false
      Object.assign(userForm, initialUserForm)
      dialogVisible.value = true
    }
    
    // 编辑用户
    const handleEdit = (row) => {
      isEdit.value = true
      // 复制用户数据到表单
      Object.assign(userForm, { 
        id: row.id,
        username: row.username,
        nickname: row.nickname,
        email: row.email,
        phone: row.phone,
        role: row.role,
        status: row.status,
        avatar: row.avatar
      })
      dialogVisible.value = true
    }
    
    // 删除用户
    const handleDelete = (row) => {
      selectedUser.value = row
      deleteDialogVisible.value = true
    }
    
    // 确认删除
    const confirmDelete = async () => {
      try {
        // 在实际项目中，这里应该调用后端API
        // 模拟API调用
        ElMessage.success(`用户 "${selectedUser.value.username}" 已删除`)
        
        // 从列表中移除
        userList.value = userList.value.filter(user => user.id !== selectedUser.value.id)
        
        deleteDialogVisible.value = false
      } catch (error) {
        console.error('删除用户失败：', error)
        ElMessage.error('删除用户失败，请稍后重试')
      }
    }
    
    // 修改用户状态
    const handleStatusChange = async (user) => {
      try {
        // 在实际项目中，这里应该调用后端API
        // 模拟API调用
        const statusText = user.status === 1 ? '启用' : '禁用'
        ElMessage.success(`用户 "${user.username}" 已${statusText}`)
      } catch (error) {
        console.error('修改用户状态失败：', error)
        ElMessage.error('修改用户状态失败，请稍后重试')
        // 恢复原状态
        user.status = user.status === 1 ? 0 : 1
      }
    }
    
    // 提交用户表单
    const submitUserForm = async () => {
      if (!userFormRef.value) return
      
      userFormRef.value.validate(async (valid) => {
        if (valid) {
          try {
            // 在实际项目中，这里应该调用后端API
            // 模拟API调用
            if (isEdit.value) {
              // 编辑用户
              // 更新列表中的用户数据
              const index = userList.value.findIndex(user => user.id === userForm.id)
              if (index !== -1) {
                userList.value[index] = {
                  ...userList.value[index],
                  nickname: userForm.nickname,
                  email: userForm.email,
                  phone: userForm.phone,
                  role: userForm.role,
                  roleName: roleOptions.find(r => r.value === userForm.role)?.label || '',
                  status: userForm.status,
                  avatar: userForm.avatar
                }
              }
              
              ElMessage.success(`用户 "${userForm.username}" 已更新`)
            } else {
              // 添加用户
              const newUser = {
                id: userList.value.length + 1,
                username: userForm.username,
                nickname: userForm.nickname,
                email: userForm.email,
                phone: userForm.phone,
                role: userForm.role,
                roleName: roleOptions.find(r => r.value === userForm.role)?.label || '',
                status: userForm.status,
                registerTime: new Date(),
                lastLoginTime: null,
                avatar: userForm.avatar
              }
              
              userList.value.unshift(newUser)
              
              ElMessage.success(`用户 "${userForm.username}" 已添加`)
            }
            
            dialogVisible.value = false
          } catch (error) {
            console.error('提交用户表单失败：', error)
            ElMessage.error('提交失败，请稍后重试')
          }
        } else {
          return false
        }
      })
    }
    
    // 头像上传前验证
    const beforeAvatarUpload = (file) => {
      const isImage = file.type.startsWith('image/')
      const isLt2M = file.size / 1024 / 1024 < 2
      
      if (!isImage) {
        ElMessage.error('上传头像图片只能是图片格式!')
      }
      
      if (!isLt2M) {
        ElMessage.error('上传头像图片大小不能超过 2MB!')
      }
      
      return isImage && isLt2M
    }
    
    // 头像上传处理
    const uploadAvatar = (options) => {
      const file = options.file
      
      // 在实际项目中，这里应该上传文件到服务器
      // 模拟上传，使用FileReader读取文件为base64
      const reader = new FileReader()
      reader.readAsDataURL(file)
      reader.onload = (e) => {
        userForm.avatar = e.target.result
      }
    }
    
    // 导出用户数据
    const handleExportUsers = () => {
      ElMessage.info('导出功能开发中，敬请期待...')
    }
    
    // 处理商家认证请求
    const handleMerchantRequests = () => {
      router.push('/user/merchant-requests')
    }
    
    // 默认头像
    const defaultAvatar = '/mock-images/avatar-default.jpg'
    
    onMounted(() => {
      fetchUserList()
    })
    
    return {
      // 数据
      loading,
      userList,
      total,
      currentPage,
      pageSize,
      searchQuery,
      roleFilter,
      statusFilter,
      roleOptions,
      defaultAvatar,
      dialogVisible,
      deleteDialogVisible,
      isEdit,
      userForm,
      userRules,
      selectedUser,
      currentUser,
      userFormRef,
      
      // 方法
      formatDate,
      getRoleTagType,
      handleSearch,
      resetFilters,
      handleSizeChange,
      handleCurrentChange,
      showAddUserDialog,
      handleEdit,
      handleDelete,
      confirmDelete,
      handleStatusChange,
      submitUserForm,
      beforeAvatarUpload,
      uploadAvatar,
      handleExportUsers,
      handleMerchantRequests
    }
  }
}
</script>

<style lang="scss" scoped>
.user-manage-page {
  padding-bottom: 40px;
  
  .page-header {
    background-color: #fff;
    padding: 20px 0;
    margin-bottom: 20px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
    
    .container {
      max-width: 1200px;
      margin: 0 auto;
      padding: 0 15px;
    }
  
  .page-title {
      font-size: 1.8rem;
      color: var(--el-text-color-primary);
      margin-bottom: 6px;
  }
  
  .page-desc {
      font-size: 0.95rem;
      color: var(--el-text-color-secondary);
    }
  }
  
  .main-content {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 15px;
  }
  
  .toolbar {
    display: flex;
    justify-content: space-between;
    margin-bottom: 20px;
    flex-wrap: wrap;
    gap: 15px;
    
    .filter-group {
      display: flex;
      gap: 10px;
      flex-wrap: wrap;
      
      .search-input {
        width: 250px;
      }
    }
    
    .action-group {
      display: flex;
      gap: 10px;
    }
  }
  
  .table-card {
    margin-bottom: 30px;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 15px;
      
      .header-title {
        font-size: 1.1rem;
        font-weight: 500;
        color: var(--el-text-color-primary);
      }
    }
    
    .user-cell {
      display: flex;
      align-items: center;
      
      .username {
        margin-left: 10px;
      }
    }
  }
  
  .pagination-container {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }
  
  .avatar-uploader {
    display: flex;
    justify-content: center;
    
    .avatar {
      width: 100px;
      height: 100px;
      border-radius: 50%;
      object-fit: cover;
    }
    
    .avatar-uploader-icon {
      font-size: 28px;
      color: #8c939d;
      width: 100px;
      height: 100px;
      line-height: 100px;
    text-align: center;
      border: 1px dashed #d9d9d9;
      border-radius: 50%;
    }
    
    :hover {
      border-color: var(--el-color-primary);
    }
  }
  
  .delete-confirm {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 20px 0;
    
    .warning-icon {
      font-size: 48px;
      color: #e6a23c;
      margin-bottom: 15px;
    }
    
    p {
      font-size: 16px;
      margin-bottom: 10px;
    }
    
    .warning-text {
      font-size: 14px;
      color: var(--el-color-danger);
    }
  }
  
  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    margin-top: 10px;
  }
}

@media (max-width: 768px) {
  .user-manage-page {
    .toolbar {
      .filter-group {
        flex-direction: column;
        width: 100%;
        
        .search-input {
          width: 100%;
        }
        
        .el-select {
          width: 100%;
        }
      }
      
      .action-group {
        width: 100%;
        justify-content: flex-end;
      }
    }
  }
}
</style> 