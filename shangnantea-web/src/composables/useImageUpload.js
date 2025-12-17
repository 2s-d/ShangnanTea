/**
 * 图片上传相关操作的组合式函数
 */
import { ref, computed } from 'vue'
import { message } from '@/components/common'

/**
 * 使用图片上传功能
 * @param {Object} options 配置选项
 * @returns {Object} 图片上传相关方法和状态
 */
export function useImageUpload(options = {}) {
  // 默认配置
  const defaultOptions = {
    maxSize: 2, // 默认最大2MB
    maxCount: 5, // 默认最多5张图片
    acceptTypes: ['image/jpeg', 'image/png', 'image/gif'], // 默认接受的类型
    uploadApi: null, // 上传API
    onSuccess: null, // 成功回调
    onError: null, // 失败回调
    multiple: false // 是否支持多选
  }
  
  // 合并配置
  const config = { ...defaultOptions, ...options }
  
  // 状态
  const fileList = ref([])
  const uploading = ref(false)
  const currentFile = ref(null)
  
  // 图片列表处理
  const setFileList = (list) => {
    fileList.value = list.map(item => ({
      ...item,
      status: item.status || 'success',
      percentage: item.percentage || 100
    }))
  }
  
  // 计算属性
  const isMaxCount = computed(() => {
    return fileList.value.length >= config.maxCount
  })
  
  /**
   * 校验文件类型
   * @param {File} file 文件对象
   * @returns {boolean} 是否通过验证
   */
  const validateFileType = (file) => {
    if (!config.acceptTypes || config.acceptTypes.length === 0) {
      return true
    }
    
    const isValid = config.acceptTypes.includes(file.type)
    if (!isValid) {
      message.error('不支持的文件类型，请上传正确格式的图片')
    }
    return isValid
  }
  
  /**
   * 校验文件大小
   * @param {File} file 文件对象
   * @returns {boolean} 是否通过验证
   */
  const validateFileSize = (file) => {
    const isValid = file.size / 1024 / 1024 <= config.maxSize
    if (!isValid) {
      message.error(`文件大小不能超过${config.maxSize}MB`)
    }
    return isValid
  }
  
  /**
   * 上传前校验
   * @param {File} file 文件对象
   * @returns {boolean} 是否可以上传
   */
  const beforeUpload = (file) => {
    if (isMaxCount.value && !currentFile.value) {
      message.warning(`最多只能上传${config.maxCount}张图片`)
      return false
    }
    
    return validateFileType(file) && validateFileSize(file)
  }
  
  /**
   * 处理文件变化事件
   * @param {Event} e 事件对象
   */
  const handleFileChange = (e) => {
    const files = e.target.files
    if (!files || !files.length) return
    
    // 检查文件数量限制
    if (config.multiple && fileList.value.length + files.length > config.maxCount) {
      message.warning(`最多只能上传${config.maxCount}张图片`)
      e.target.value = ''
      return
    }
    
    // 处理选择的文件
    Array.from(files).forEach(file => {
      if (beforeUpload(file)) {
        addFile(file)
      }
    })
    
    // 清空input，允许重复选择相同文件
    e.target.value = ''
  }
  
  /**
   * 添加文件到列表
   * @param {File} file 文件对象
   */
  const addFile = (file) => {
    // 创建预览URL
    const fileUrl = URL.createObjectURL(file)
    
    // 构建文件对象
    const fileItem = {
      raw: file,
      url: fileUrl,
      name: file.name,
      size: file.size,
      status: 'ready',
      percentage: 0,
      uid: Date.now() + Math.random().toString(36).substring(2)
    }
    
    // 如果是单文件上传，则替换当前文件
    if (!config.multiple) {
      // 清除旧的URL对象
      if (fileList.value.length > 0 && fileList.value[0].url.startsWith('blob:')) {
        URL.revokeObjectURL(fileList.value[0].url)
      }
      fileList.value = [fileItem]
      currentFile.value = fileItem
    } else {
      fileList.value.push(fileItem)
    }
    
    // 自动上传
    if (config.uploadApi) {
      uploadFile(fileItem)
    }
  }
  
  /**
   * 移除文件
   * @param {number|string} index 文件索引或UID
   */
  const removeFile = (index) => {
    let idx = index
    
    // 如果传入的是uid，查找对应索引
    if (typeof index === 'string') {
      idx = fileList.value.findIndex(item => item.uid === index)
    }
    
    if (idx !== -1) {
      // 移除URL对象，避免内存泄漏
      const fileItem = fileList.value[idx]
      if (fileItem.url && fileItem.url.startsWith('blob:')) {
        URL.revokeObjectURL(fileItem.url)
      }
      
      // 从列表中移除
      fileList.value.splice(idx, 1)
      
      // 如果是当前正在操作的文件，清除引用
      if (currentFile.value && currentFile.value.uid === fileItem.uid) {
        currentFile.value = null
      }
    }
  }
  
  /**
   * 上传文件
   * @param {Object} fileItem 文件项
   */
  const uploadFile = async (fileItem) => {
    if (!config.uploadApi || !fileItem || fileItem.status === 'success') return
    
    try {
      uploading.value = true
      fileItem.status = 'uploading'
      
      // 创建FormData
      const formData = new FormData()
      formData.append('file', fileItem.raw)
      
      // 调用上传API
      const response = await config.uploadApi(formData, (progress) => {
        fileItem.percentage = progress
      })
      
      // 上传成功
      fileItem.status = 'success'
      fileItem.percentage = 100
      
      // 更新文件URL，替换为服务器返回的URL
      if (response && response.url) {
        // 释放之前的Blob URL
        if (fileItem.url && fileItem.url.startsWith('blob:')) {
          URL.revokeObjectURL(fileItem.url)
        }
        fileItem.url = response.url
      }
      
      // 调用成功回调
      if (typeof config.onSuccess === 'function') {
        config.onSuccess(response, fileItem)
      }
      
      return response
    } catch (error) {
      // 上传失败
      fileItem.status = 'error'
      
      // 调用错误回调
      if (typeof config.onError === 'function') {
        config.onError(error, fileItem)
      }
      
      message.error('上传失败: ' + (error.message || '未知错误'))
      return Promise.reject(error)
    } finally {
      uploading.value = false
    }
  }
  
  /**
   * 上传所有未上传的文件
   */
  const uploadAll = async () => {
    if (!config.uploadApi) {
      message.warning('未配置上传API')
      return []
    }
    
    try {
      uploading.value = true
      
      const uploadTasks = fileList.value
        .filter(item => item.status !== 'success')
        .map(item => uploadFile(item))
      
      const responses = await Promise.all(uploadTasks)
      
      return responses
    } catch (error) {
      console.error('批量上传失败:', error)
      return []
    } finally {
      uploading.value = false
    }
  }
  
  /**
   * 重置文件列表
   */
  const resetFiles = () => {
    // 清除所有blob URL
    fileList.value.forEach(item => {
      if (item.url && item.url.startsWith('blob:')) {
        URL.revokeObjectURL(item.url)
      }
    })
    
    fileList.value = []
    currentFile.value = null
  }
  
  /**
   * 获取上传成功的文件列表
   * @returns {Array} 成功上传的文件列表
   */
  const getSuccessFiles = () => {
    return fileList.value
      .filter(item => item.status === 'success')
      .map(item => ({
        id: item.id,
        url: item.url,
        name: item.name
      }))
  }
  
  return {
    fileList,
    uploading,
    currentFile,
    isMaxCount,
    setFileList,
    handleFileChange,
    addFile,
    removeFile,
    uploadFile,
    uploadAll,
    resetFiles,
    validateFileType,
    validateFileSize,
    getSuccessFiles
  }
} 