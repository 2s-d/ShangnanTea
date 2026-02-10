<template>
  <div class="profile-edit-container" v-loading="loading">
    <el-page-header @back="goBack" title="返回" content="个性化设置" />
    
    <el-form label-width="100px" class="settings-form" :disabled="submitting">
      <el-card class="settings-card">
        <template #header>
          <div class="card-header">
            <h3>主题设置</h3>
          </div>
        </template>
        
        <!-- 主题模式选择 -->
        <el-form-item label="主题模式">
          <el-radio-group v-model="preferences.theme">
            <el-radio-button value="light">
              <el-icon><Sunny /></el-icon> 浅色
            </el-radio-button>
            <el-radio-button value="dark">
              <el-icon><Moon /></el-icon> 深色
            </el-radio-button>
            <el-radio-button value="auto">
              <el-icon><MagicStick /></el-icon> 自动
            </el-radio-button>
          </el-radio-group>
        </el-form-item>
        
        <!-- 主题色选择 -->
        <el-form-item label="主题色">
          <div class="color-picker-container">
            <el-color-picker v-model="preferences.primaryColor" show-alpha />
            <el-button size="small" @click="resetThemeColor">恢复默认</el-button>
          </div>
        </el-form-item>
        
        <!-- 字体大小设置 -->
        <el-form-item label="字体大小">
          <div class="font-size-container">
            <el-slider 
              v-model="preferences.fontSize" 
              :min="12" 
              :max="20"
              :format-tooltip="value => `${value}px`"
              :step="1"
              show-stops
              class="font-size-slider"
            />
            <span class="font-size-preview" :style="{
              fontSize: `${preferences.fontSize}px`,
              fontFamily: preferences.fontFamily || 'inherit'
            }">
              字体预览 ABC 123 茶文化
            </span>
          </div>
        </el-form-item>
        
        <!-- 字体选择 -->
        <el-form-item label="字体选择">
          <el-select v-model="preferences.fontFamily" placeholder="选择字体">
            <el-option label="默认字体" value="" />
            <el-option label="黑体" value="SimHei, 'Heiti SC', 'Heiti TC', sans-serif" />
            <el-option label="宋体" value="SimSun, 'Songti SC', 'Songti TC', serif" />
            <el-option label="楷体" value="KaiTi, 'Kaiti SC', 'Kaiti TC', cursive" />
            <el-option label="微软雅黑" value="'Microsoft YaHei', 'PingFang SC', 'Hiragino Sans GB', sans-serif" />
            <el-option label="华文细黑" value="'STXihei', sans-serif" />
          </el-select>
          <div class="font-preview" :style="{ fontFamily: preferences.fontFamily || 'inherit' }">
            {{ preferences.fontFamily ? '商南茶文化 - 字体预览' : '请选择字体以查看预览' }}
          </div>
        </el-form-item>
        
        <!-- UI动效开关 -->
        <el-form-item label="UI动效">
          <el-switch v-model="preferences.enableAnimation" />
          <span class="setting-hint">{{ preferences.enableAnimation ? '开启界面动画效果' : '关闭界面动画效果' }}</span>
        </el-form-item>
      </el-card>
      
      <el-card class="settings-card">
        <template #header>
          <div class="card-header">
            <h3>展示设置</h3>
          </div>
        </template>
        
        <!-- 列表展示模式 -->
        <el-form-item label="展示模式">
          <el-radio-group v-model="preferences.listMode">
            <el-radio-button value="grid">
              <el-icon><Grid /></el-icon> 网格
            </el-radio-button>
            <el-radio-button value="list">
              <el-icon><List /></el-icon> 列表
            </el-radio-button>
          </el-radio-group>
        </el-form-item>
        
        <!-- 每页商品数 -->
        <el-form-item label="每页商品数">
          <el-select v-model="preferences.pageSize">
            <el-option label="10条/页" :value="10" />
            <el-option label="20条/页" :value="20" />
            <el-option label="30条/页" :value="30" />
            <el-option label="50条/页" :value="50" />
          </el-select>
        </el-form-item>
      </el-card>
      
      <div class="form-actions">
        <el-button type="primary" @click="savePreferences" :loading="submitting">保存设置</el-button>
        <el-button @click="resetPreferences">恢复默认</el-button>
    </div>
    </el-form>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { Sunny, Moon, MagicStick, Grid, List } from '@element-plus/icons-vue'

import { showByCode, isSuccess } from '@/utils/apiMessages'
import { userPromptMessages as userMessages } from '@/utils/promptMessages'

export default {
  name: 'ProfileEditPage',
  components: {
    Sunny, Moon, MagicStick, Grid, List
  },
  setup() {
    const formRef = ref(null)
    const loading = ref(false)
    const submitting = ref(false)
    const userStore = useUserStore()
    
    // 默认主题色
    const DEFAULT_PRIMARY_COLOR = '#409EFF'
    
    // 个性化偏好设置（页面内编辑用副本；保存时统一走 Vuex）
    const preferences = reactive({
      theme: 'light',
      primaryColor: DEFAULT_PRIMARY_COLOR,
      fontSize: 14,
      fontFamily: '',
      enableAnimation: true,
      listMode: 'grid',
      pageSize: 20
    })
    
    // 应用主题设置
    const applyThemeSettings = theme => {
      // 获取html根元素
      const htmlEl = document.documentElement
      
      // 移除所有可能的主题类
      htmlEl.classList.remove('dark-mode', 'light-mode')
      
      // 根据设置添加对应的主题类
      if (theme === 'dark') {
        htmlEl.classList.add('dark-mode')
        // 设置Element Plus的内置dark模式
        htmlEl.setAttribute('data-theme', 'dark')
      } else if (theme === 'light') {
        htmlEl.classList.add('light-mode')
        htmlEl.setAttribute('data-theme', 'light')
      } else if (theme === 'auto') {
        // 自动模式，根据系统颜色模式设置
        if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
          htmlEl.classList.add('dark-mode')
          htmlEl.setAttribute('data-theme', 'dark')
        } else {
          htmlEl.classList.add('light-mode')
          htmlEl.setAttribute('data-theme', 'light')
        }
      }
    }
    
    // 重置主题色
    const resetThemeColor = () => {
      preferences.primaryColor = DEFAULT_PRIMARY_COLOR
    }
    
    // 保存设置
    const savePreferences = async () => {
      try {
        submitting.value = true
        const payload = { ...preferences }
        const response = await userStore.saveUserPreferences(payload)
        
        // 显示API响应消息（成功或失败都通过状态码映射显示）
        showByCode(response.code)
        
        // 只有成功时才应用设置
        if (isSuccess(response.code)) {
          // 应用主题设置
          applyThemeSettings(preferences.theme)
          
          // 应用颜色和字体设置
          document.documentElement.style.setProperty('--el-color-primary', preferences.primaryColor)
          document.documentElement.style.setProperty('--el-font-size-base', preferences.fontSize + 'px')
          
          // 如果设置了字体，应用字体设置
          if (preferences.fontFamily) {
            document.documentElement.style.setProperty('--el-font-family', preferences.fontFamily)
            document.body.style.fontFamily = preferences.fontFamily
          } else {
            document.documentElement.style.removeProperty('--el-font-family')
            document.body.style.fontFamily = ''
          }
        }
      } catch (e) {
        // 说明：在重构后的消息系统中，所有业务错误都通过状态码映射显示
        // 
        // 1. API业务失败：响应拦截器返回 {code: 错误码, data: null}
        //    → 通过 showByCode(response.code) 显示（有状态码映射）
        //    → 不会抛出异常，不会进入 catch
        // 
        // 2. 网络错误等：响应拦截器会显示错误消息并 reject
        //    → Vuex action 没有 catch，会抛出异常
        //    → 但响应拦截器已经显示了错误消息，这里不应该重复显示
        // 
        // 3. 真正的意外错误：如 DOM 操作失败、消息显示错误等
        //    → 这些不是业务逻辑问题，不应该显示给用户
        // 
        // 结论：catch 块是冗余的，因为：
        // - 业务失败已有状态码映射显示
        // - 网络错误已在响应拦截器显示
        // - 意外错误不应该显示给用户
        // 
        // 但保留 catch 块用于：
        // - 确保 submitting 状态被重置（在 finally 中）
        // - 开发环境记录意外错误用于调试
        
        console.error('[开发调试] 保存偏好设置时发生意外错误：', e)
        
      } finally {
        submitting.value = false
      }
    }
    
    // 重置所有设置
    const resetPreferences = () => {
      preferences.theme = 'light'
      preferences.primaryColor = DEFAULT_PRIMARY_COLOR
      preferences.fontSize = 14
      preferences.fontFamily = ''
      preferences.enableAnimation = true
      preferences.listMode = 'grid'
      preferences.pageSize = 20
      
      // 本地操作，使用 userMessages 是正确的（不涉及API调用）
      userMessages.success.showSettingsRestored()
    }
    
    // 路由
    const router = useRouter()
    const goBack = () => {
      router.back()
    }
    
    onMounted(() => {
      loading.value = true
      const fetchPreferences = async () => {
        try {
          const response = await userStore.fetchUserPreferences()
          
          if (isSuccess(response.code)) {
            const source = response.data || userStore.preferences
            Object.assign(preferences, source || {})
            // 初始化时应用主题设置
            applyThemeSettings(preferences.theme)
            // 初始化时应用颜色/字体设置
            document.documentElement.style.setProperty('--el-color-primary', preferences.primaryColor)
            document.documentElement.style.setProperty('--el-font-size-base', preferences.fontSize + 'px')
            if (preferences.fontFamily) {
              document.documentElement.style.setProperty('--el-font-family', preferences.fontFamily)
              document.body.style.fontFamily = preferences.fontFamily
            }
          } else {
            showByCode(response.code)
          }
        } catch (error) {
          console.error('获取设置失败:', error)
        } finally {
          loading.value = false
        }
      }
      
      fetchPreferences()
    })
    
    return {
      formRef,
      loading,
      submitting,
      preferences,
      resetThemeColor,
      savePreferences,
      resetPreferences,
      goBack
    }
  }
}
</script>

<style lang="scss" scoped>
.profile-edit-container {
  padding: 20px;
  
  h2.section-title {
    font-size: 24px;
    margin-bottom: 8px;
    color: var(--el-text-color-primary);
  }
  
  p.section-desc {
    color: var(--el-text-color-secondary);
    margin-bottom: 20px;
  }
  
  .color-picker-container {
    display: flex;
    align-items: center;
    gap: 15px;
  }
  
  .font-size-container {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 10px;
  }
  
  .font-size-preview {
    padding: 10px;
    border: 1px solid var(--el-border-color);
    border-radius: 4px;
    background-color: var(--el-fill-color-light);
    margin-top: 10px;
  }
  
  .font-preview {
    margin-top: 10px;
    padding: 10px;
    border: 1px solid var(--el-border-color);
    border-radius: 4px;
    background-color: var(--el-fill-color-light);
  }
  
  .setting-hint {
    margin-left: 10px;
    color: var(--el-text-color-secondary);
    font-size: 12px;
  }
  
  .form-actions {
    margin-top: 20px;
    text-align: right;
  }
  
  .settings-card {
    margin-bottom: 20px;
  }
  
  .card-header {
    font-size: 18px;
    font-weight: bold;
    margin-bottom: 10px;
  }
}

/* 预览部分 */
.preview-section {
  margin-top: 40px;
  border-top: 1px solid var(--el-border-color-lighter);
  padding-top: 30px;
  
  .preview-header {
    margin-bottom: 20px;
    
    h3 {
      margin: 0 0 5px 0;
      font-size: 18px;
    }
    
    p {
      margin: 0;
      color: var(--el-text-color-secondary);
      font-size: 14px;
    }
  }
  
  .preview-container {
    border: 1px solid var(--el-border-color);
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
    transition: all 0.3s;
    
    /* 深色主题 */
    &.dark-theme {
      background-color: #303133;
      color: #e0e0e0;
      
      .preview-header {
        background-color: #202124;
        
        .preview-nav span.active {
          color: var(--primary-color, var(--el-color-primary));
        }
      }
      
      .preview-content {
        .preview-title {
          color: var(--primary-color, var(--el-color-primary));
        }
        
        .preview-card {
          background-color: #424242;
          border-color: #606266;
        }
      }
    }
    
    /* 带动画效果 */
    &.with-animation .preview-card {
      transition: transform 0.3s, box-shadow 0.3s;
      
      &:hover {
        transform: translateY(-5px);
        box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
      }
    }
    
    .preview-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 15px 20px;
      background-color: var(--el-bg-color);
      border-bottom: 1px solid var(--el-border-color-lighter);
      
      .preview-logo {
        font-weight: bold;
        color: var(--primary-color, var(--el-color-primary));
        font-size: 1.2em;
      }
      
      .preview-nav {
        display: flex;
        gap: 20px;
        
        span {
          cursor: pointer;
          padding: 5px 0;
          position: relative;
          
          &.active {
            color: var(--primary-color, var(--el-color-primary));
            font-weight: bold;
            
            &:after {
              content: '';
              position: absolute;
              bottom: 0;
              left: 0;
              width: 100%;
              height: 2px;
              background-color: var(--primary-color, var(--el-color-primary));
            }
          }
        }
      }
    }
    
    .preview-content {
      padding: 20px;
      
      .preview-title {
        font-size: 1.2em;
        font-weight: bold;
        margin-bottom: 10px;
        color: var(--primary-color, var(--el-color-primary));
      }
      
      .preview-text {
        margin-bottom: 20px;
        color: var(--el-text-color-secondary);
      }
      
      .preview-cards {
        &.grid-layout {
          display: grid;
          grid-template-columns: repeat(3, 1fr);
          gap: 15px;
        }
        
        &.list-layout {
          display: flex;
          flex-direction: column;
          gap: 10px;
          
          .preview-card {
            display: flex;
            flex-direction: column;
          }
        }
        
        .preview-card {
          padding: 15px;
          background-color: var(--el-bg-color-overlay);
          border-radius: 6px;
          border: 1px solid var(--el-border-color-lighter);
          cursor: pointer;
          
          .card-title {
            font-weight: bold;
            margin-bottom: 8px;
          }
          
          .card-content {
            color: var(--el-text-color-secondary);
            font-size: 0.9em;
          }
        }
      }
    }
  }
}

/* 移动端适配 */
@media (max-width: 768px) {
  .profile-edit-container {
    .preview-section {
      .preview-container {
        .preview-header {
          flex-direction: column;
          gap: 10px;
          
          .preview-nav {
            width: 100%;
            justify-content: space-between;
          }
        }
        
        .preview-content {
          .preview-cards.grid-layout {
            grid-template-columns: repeat(1, 1fr);
          }
        }
      }
    }
  }
}
</style>

<style>
/* 全局无作用域样式，用于主题切换 */
html.dark-mode, html.dark-mode body {
  --el-bg-color: #1c1c1c;
  --el-bg-color-overlay: #2c2c2c;
  --el-text-color-primary: #ffffff;
  --el-text-color-regular: #e0e0e0;
  --el-text-color-secondary: #a0a0a0;
  --el-border-color: #4a4a4a;
  --el-border-color-light: #3a3a3a;
  --el-fill-color: #2a2a2a;
  --el-fill-color-light: #333333;
  --el-fill-color-blank: #1c1c1c;
  --el-mask-color: rgba(0, 0, 0, 0.7);
  --el-disabled-bg-color: #2d2d2d;
  --el-disabled-text-color: #888;
  
  color-scheme: dark;
  background-color: var(--el-bg-color);
  color: var(--el-text-color-primary);
}

html.light-mode, html.light-mode body {
  --el-bg-color: #ffffff;
  --el-bg-color-overlay: #ffffff;
  --el-text-color-primary: #303133;
  --el-text-color-regular: #606266;
  --el-text-color-secondary: #909399;
  --el-border-color: #dcdfe6;
  --el-border-color-light: #e4e7ed;
  --el-fill-color: #f0f2f5;
  --el-fill-color-light: #f5f7fa;
  --el-fill-color-blank: #ffffff;
  --el-mask-color: rgba(255, 255, 255, 0.9);
  --el-disabled-bg-color: #f5f7fa;
  --el-disabled-text-color: #c0c4cc;
  
  color-scheme: light;
  background-color: var(--el-bg-color);
  color: var(--el-text-color-primary);
}
</style> 