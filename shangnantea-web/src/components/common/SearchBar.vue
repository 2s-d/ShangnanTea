<template>
  <div class="unified-search-bar">
    <el-input
      v-model="localSearchQuery"
      :placeholder="computedPlaceholder"
      class="search-input"
      clearable
      @keyup.enter="handleSearch"
      @clear="handleClear"
    >
      <template #prefix>
        <el-icon><Search /></el-icon>
      </template>
      <!-- 如果显示类型切换，添加前置选择器 -->
      <template v-if="showTypeSwitch" #prepend>
        <el-select v-model="localSearchType" style="width: 100px" @change="handleTypeChange">
          <el-option label="茶叶" value="tea" />
          <el-option label="店铺" value="shop" />
        </el-select>
      </template>
      <template #append>
        <el-button @click="handleSearch">搜索</el-button>
      </template>
    </el-input>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Search } from '@element-plus/icons-vue'

const props = defineProps({
  // 搜索类型：'tea' | 'shop' | 'auto'（自动根据当前页面判断）
  searchType: {
    type: String,
    default: 'auto'
  },
  // 搜索关键词（外部控制）
  modelValue: {
    type: String,
    default: ''
  },
  // 是否显示类型切换
  showTypeSwitch: {
    type: Boolean,
    default: false
  },
  // 占位符文本
  placeholder: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue', 'search', 'type-change'])

const router = useRouter()
const route = useRoute()

const localSearchQuery = ref(props.modelValue || '')
const localSearchType = ref(props.searchType === 'auto' ? detectSearchType() : props.searchType)

// 自动检测当前页面的搜索类型
function detectSearchType() {
  if (route.path.includes('/tea')) return 'tea'
  if (route.path.includes('/shop')) return 'shop'
  return 'tea' // 默认茶叶
}

// 计算占位符
const computedPlaceholder = computed(() => {
  if (props.placeholder) return props.placeholder
  const type = localSearchType.value
  return type === 'tea' ? '搜索茶叶名称' : '搜索店铺名称'
})

// 监听外部传入的搜索关键词
watch(() => props.modelValue, (newVal) => {
  if (newVal !== undefined && newVal !== null) {
    localSearchQuery.value = newVal
  }
})

// 监听路由变化，同步搜索关键词
watch(() => route.query.search, (newVal) => {
  if (newVal && newVal !== localSearchQuery.value) {
    localSearchQuery.value = newVal
    emit('update:modelValue', newVal)
  }
}, { immediate: true })

// 切换搜索类型
const handleTypeChange = (type) => {
  localSearchType.value = type
  emit('type-change', type)
  
  // 如果当前有搜索关键词，切换类型后自动搜索
  if (localSearchQuery.value.trim()) {
    handleSearch()
  }
}

// 执行搜索
const handleSearch = () => {
  const query = localSearchQuery.value.trim()
  if (!query) return
  
  emit('update:modelValue', query)
  emit('search', { query, type: localSearchType.value })
  
  // 根据类型跳转到对应页面
  const targetPath = localSearchType.value === 'tea' ? '/tea/mall' : '/shop/list'
  
  router.push({
    path: targetPath,
    query: {
      search: query,
      // 搜索时重置到第一页
      page: 1
    }
  })
}

// 清空搜索
const handleClear = () => {
  localSearchQuery.value = ''
  emit('update:modelValue', '')
  
  // 如果当前在搜索结果页，清空搜索参数
  if (route.query.search) {
    const query = { ...route.query }
    delete query.search
    router.replace({ query })
  }
}
</script>

<style scoped>
.unified-search-bar {
  width: 100%;
}

.search-input {
  width: 100%;
}
</style>
