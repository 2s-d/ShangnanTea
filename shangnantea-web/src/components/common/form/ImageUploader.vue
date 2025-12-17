<template>
  <div class="image-uploader">
    <!-- 图片列表 -->
    <div class="image-uploader__list" v-if="fileList.length > 0">
      <div 
        v-for="(file, index) in fileList" 
        :key="index" 
        class="image-uploader__item"
      >
        <SafeImage 
          src="/mock-images/default.jpg" 
          alt="预览图" 
          type="tea"
          class="image-uploader__img" 
          @click="previewImage(file)"
        />
        <div class="image-uploader__actions">
          <i class="el-icon-zoom-in" @click="previewImage(file)"></i>
          <i class="el-icon-delete" @click="removeFile(index)"></i>
        </div>
        <div class="image-uploader__progress" v-if="file.status === 'uploading'">
          <el-progress 
            :percentage="file.percentage" 
            :show-text="false" 
            :stroke-width="5"
          ></el-progress>
        </div>
        <div class="image-uploader__mask" v-if="file.status === 'error'">
          <i class="el-icon-warning-outline"></i>
          <span>上传失败</span>
        </div>
      </div>
    </div>
    
    <!-- 上传按钮 -->
    <div 
      class="image-uploader__upload" 
      v-if="!disabled && (multiple || fileList.length === 0)"
      @click="handleClick"
    >
      <i class="el-icon-plus"></i>
      <span v-if="uploadText">{{ uploadText }}</span>
      <input 
        ref="inputRef"
        type="file" 
        class="image-uploader__input" 
        :accept="accept" 
        :multiple="multiple"
        @change="handleFileChange"
      >
    </div>
    
    <!-- 提示文本 -->
    <div class="image-uploader__tip" v-if="tip">{{ tip }}</div>
    
    <!-- 图片预览弹窗 -->
    <el-dialog 
      v-model="previewVisible" 
      title="图片预览" 
      :append-to-body="true"
      class="image-uploader__preview-dialog"
    >
      <SafeImage src="/mock-images/default.jpg" alt="大图预览" type="tea" class="image-uploader__preview-img" style="max-width:100%;max-height:80vh;"/>
    </el-dialog>
  </div>
</template>

<script>
import { ref, computed, watch, onMounted } from 'vue'
import { useImageUpload } from '@/composables/useImageUpload'
import SafeImage from '@/components/common/form/SafeImage.vue'

export default {
  name: 'ImageUploader',
  components: {
    SafeImage
  },
  props: {
    // 已上传的文件列表
    value: {
      type: Array,
      default: () => []
    },
    // 是否支持多选
    multiple: {
      type: Boolean,
      default: false
    },
    // 接受的文件类型
    accept: {
      type: String,
      default: 'image/jpeg,image/png,image/gif'
    },
    // 上传按钮文本
    uploadText: {
      type: String,
      default: '上传图片'
    },
    // 提示文本
    tip: {
      type: String,
      default: '支持jpg/png/gif格式，单张不超过2MB'
    },
    // 是否禁用
    disabled: {
      type: Boolean,
      default: false
    },
    // 限制文件大小，单位MB
    sizeLimit: {
      type: Number,
      default: 2
    },
    // 最大文件数
    limit: {
      type: Number,
      default: 5
    },
    // 自动上传
    autoUpload: {
      type: Boolean,
      default: true
    },
    // 自定义上传方法
    customUpload: {
      type: Function,
      default: null
    }
  },
  emits: ['update:value', 'change'],
  setup(props, { emit }) {
    const inputRef = ref(null)
    const previewVisible = ref(false)
    const previewUrl = ref('')
    
    // 使用图片上传组合式函数
    const { 
      fileList, 
      uploading,
      handleFileChange: _handleFileChange,
      removeFile,
      resetFiles,
      validateFileType,
      validateFileSize,
      setFileList
    } = useImageUpload({
      maxSize: props.sizeLimit,
      maxCount: props.limit,
      acceptTypes: props.accept.split(',').map(type => type.trim()),
      multiple: props.multiple,
      uploadApi: props.customUpload,
      onSuccess: (response, file) => {
        updateValue()
      },
      onError: (error, file) => {
        updateValue()
      }
    })
    
    // 初始化文件列表
    onMounted(() => {
      if (props.value && props.value.length > 0) {
        setFileList(props.value)
      }
    })
    
    // 监听值变化
    watch(() => props.value, (newVal) => {
      if (newVal && newVal.length > 0) {
        setFileList(newVal)
      } else {
        resetFiles()
      }
    })
    
    // 触发文件选择
    const handleClick = () => {
      if (!props.disabled) {
        inputRef.value.click()
      }
    }
    
    // 处理文件选择
    const handleFileChange = (e) => {
      _handleFileChange(e)
      updateValue()
    }
    
    // 预览图片
    const previewImage = (file) => {
      previewUrl.value = file.url
      previewVisible.value = true
    }
    
    // 更新v-model值
    const updateValue = () => {
      emit('update:value', fileList.value)
      emit('change', fileList.value)
    }
    
    return {
      inputRef,
      fileList,
      previewVisible,
      previewUrl,
      handleClick,
      handleFileChange,
      previewImage,
      removeFile
    }
  }
}
</script>

<style lang="scss" scoped>
.image-uploader {
  display: flex;
  flex-direction: column;
  
  &__list {
    display: flex;
    flex-wrap: wrap;
    margin-bottom: 8px;
  }
  
  &__item {
    width: 100px;
    height: 100px;
    margin-right: 8px;
    margin-bottom: 8px;
    border-radius: 4px;
    position: relative;
    overflow: hidden;
    
    &:hover .image-uploader__actions {
      opacity: 1;
    }
  }
  
  &__img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  
  &__actions {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    display: flex;
    justify-content: center;
    align-items: center;
    background-color: rgba(0, 0, 0, 0.5);
    opacity: 0;
    transition: opacity 0.3s;
    
    i {
      font-size: 20px;
      color: #fff;
      margin: 0 5px;
      cursor: pointer;
    }
  }
  
  &__progress {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    padding: 2px;
  }
  
  &__mask {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    background-color: rgba(0, 0, 0, 0.5);
    color: #fff;
    
    i {
      font-size: 20px;
      margin-bottom: 5px;
      color: #F56C6C;
    }
    
    span {
      font-size: 12px;
    }
  }
  
  &__upload {
    width: 100px;
    height: 100px;
    border: 1px dashed var(--border-color, #dcdfe6);
    border-radius: 4px;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    cursor: pointer;
    transition: border-color 0.3s;
    position: relative;
    background-color: #fbfdff;
    
    &:hover {
      border-color: var(--primary-color, #409EFF);
    }
    
    i {
      font-size: 28px;
      color: #8c939d;
      margin-bottom: 5px;
    }
    
    span {
      font-size: 12px;
      color: #606266;
    }
  }
  
  &__input {
    position: absolute;
    width: 0;
    height: 0;
    opacity: 0;
    cursor: pointer;
  }
  
  &__tip {
    font-size: 12px;
    color: #909399;
    margin-top: 8px;
  }
  
  &__preview-img {
    width: 100%;
    max-height: 70vh;
    object-fit: contain;
  }
}

// 预览对话框自定义样式
:deep(.image-uploader__preview-dialog) {
  .el-dialog__header {
    padding: 15px;
  }
  
  .el-dialog__body {
    text-align: center;
    padding: 10px;
  }
}
</style> 