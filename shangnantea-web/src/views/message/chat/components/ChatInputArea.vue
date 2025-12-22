<template>
  <div class="chat-input" v-if="visible">
    <!-- 工具栏 -->
    <div class="input-toolbar">
      <el-tooltip content="发送图片" placement="top">
        <el-button type="text" @click="triggerImageUpload" class="toolbar-button">
          <el-icon><Picture /></el-icon>
        </el-button>
      </el-tooltip>
      
      <el-tooltip content="表情" placement="top">
        <el-button type="text" @click="toggleEmojiPicker" class="toolbar-button">
          <el-icon><Smile /></el-icon>
          表情
        </el-button>
      </el-tooltip>
      
      <!-- 隐藏的文件上传 -->
      <input 
        type="file" 
        ref="imageInput"
        accept="image/*"
        style="display: none"
        @change="handleImageUpload" />
    </div>
    
    <!-- 表情选择器 -->
    <EmojiPicker 
      v-if="showEmojiPicker" 
      @select="insertEmoji" 
      @close="showEmojiPicker = false" />
    
    <!-- 文本输入区 -->
    <el-input 
      type="textarea" 
      :rows="3" 
      placeholder="请输入消息..." 
      v-model="localMessageInput"
      resize="none"
      @keydown.enter.exact.prevent="handleSend">
    </el-input>
    
    <!-- 发送按钮 -->
    <div class="input-actions">
      <el-button 
        type="primary" 
        @click="handleSend"
        :disabled="!canSend">
        发送
      </el-button>
    </div>
  </div>
</template>

<script>
import { ref, computed, watch } from 'vue'
import { Picture, Smile } from '@element-plus/icons-vue'
import { message } from '@/components/common'
import EmojiPicker from './EmojiPicker.vue'

export default {
  name: 'ChatInputArea',
  components: { Picture, Smile, EmojiPicker },
  props: {
    visible: { type: Boolean, default: true },
    messageInput: { type: String, default: '' }
  },
  emits: ['send', 'send-image', 'update:messageInput'],
  setup(props, { emit }) {
    const imageInput = ref(null)
    const showEmojiPicker = ref(false)
    const imageFile = ref(null)
    
    const localMessageInput = computed({
      get: () => props.messageInput,
      set: (val) => emit('update:messageInput', val)
    })
    
    const canSend = computed(() => {
      return localMessageInput.value.trim() || imageFile.value
    })
    
    const triggerImageUpload = () => {
      if (imageInput.value) {
        imageInput.value.click()
      }
    }
    
    const handleImageUpload = (event) => {
      const file = event.target.files[0]
      if (!file) return
      
      if (!file.type.startsWith('image/')) {
        message.error('只能上传图片文件')
        return
      }
      
      if (file.size > 5 * 1024 * 1024) {
        message.error('图片大小不能超过5MB')
        return
      }
      
      imageFile.value = file
      emit('send-image', file)
      event.target.value = ''
      imageFile.value = null
    }
    
    const toggleEmojiPicker = () => {
      showEmojiPicker.value = !showEmojiPicker.value
    }
    
    const insertEmoji = (emoji) => {
      localMessageInput.value += emoji
      showEmojiPicker.value = false
    }
    
    const handleSend = () => {
      if (!canSend.value) return
      emit('send', localMessageInput.value.trim())
      localMessageInput.value = ''
    }
    
    return {
      imageInput,
      showEmojiPicker,
      localMessageInput,
      canSend,
      triggerImageUpload,
      handleImageUpload,
      toggleEmojiPicker,
      insertEmoji,
      handleSend
    }
  }
}
</script>

<style lang="scss" scoped>
.chat-input {
  padding: 15px;
  background-color: #fff;
  border-top: 1px solid #eee;
  
  .input-toolbar {
    display: flex;
    margin-bottom: 10px;
    
    .toolbar-button {
      margin-right: 15px;
      color: var(--text-secondary);
      &:hover { color: var(--primary-color); }
    }
  }
  
  .input-actions {
    display: flex;
    justify-content: flex-end;
    margin-top: 10px;
  }
}
</style>
