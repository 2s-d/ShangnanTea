<template>
  <div class="search-bar">
    <el-input
      v-model="searchValue"
      :placeholder="placeholder"
      clearable
      @keyup.enter="handleSearch"
      @clear="handleClear"
    >
      <template #prefix>
        <el-icon><Search /></el-icon>
      </template>
      <template #append>
        <el-button @click="handleSearch">
          <el-icon><Search /></el-icon>
        </el-button>
      </template>
    </el-input>
  </div>
</template>

<script>
import { ref, watch } from 'vue'
import { Search } from '@element-plus/icons-vue'

export default {
  name: 'SearchBar',
  components: {
    Search
  },
  props: {
    modelValue: {
      type: String,
      default: ''
    },
    placeholder: {
      type: String,
      default: '请输入搜索内容'
    },
    searchType: {
      type: String,
      default: ''
    }
  },
  emits: ['update:modelValue', 'search'],
  setup(props, { emit }) {
    const searchValue = ref(props.modelValue)
    
    // 监听外部值变化
    watch(() => props.modelValue, (newVal) => {
      searchValue.value = newVal
    })
    
    // 监听内部值变化
    watch(searchValue, (newVal) => {
      emit('update:modelValue', newVal)
    })
    
    const handleSearch = () => {
      emit('search', searchValue.value)
    }
    
    const handleClear = () => {
      searchValue.value = ''
      emit('update:modelValue', '')
      emit('search', '')
    }
    
    return {
      searchValue,
      handleSearch,
      handleClear
    }
  }
}
</script>

<style lang="scss" scoped>
.search-bar {
  width: 100%;
  max-width: 400px;
  
  :deep(.el-input-group__append) {
    background-color: var(--el-color-primary);
    color: white;
    border-color: var(--el-color-primary);
    
    &:hover {
      background-color: var(--el-color-primary-light-3);
    }
  }
}
</style>
