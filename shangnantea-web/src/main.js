import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
// 导入Element Plus图标
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
// 导入公共组件
import CommonComponents from './components/common'
// 导入自定义指令
import Directives from './directives'
// 导入数据版本管理工具
import { checkAndMigrateData } from './utils/versionManager'
// 导入全局样式
import './assets/styles/global.scss'
import { message } from '@/components/common'
import messageManager from '@/utils/messageManager'

// 在应用初始化前检查并迁移本地存储数据
checkAndMigrateData();

// 应用启动时清除所有消息状态
messageManager.clearAllMessageStates()

// 初始化用户认证状态
const initAuthState = async () => {
  try {
    // 初始化认证状态，加载用户信息
    await store.dispatch('user/initAuth')
    console.log('用户认证状态初始化完成')
  } catch (error) {
    console.error('初始化用户认证状态失败:', error)
  }
}

const app = createApp(App)

// 注册所有Element Plus图标组件
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 配置Element Plus选项
const elementPlusOptions = {
  size: 'default', 
  zIndex: 3000,
  // 表单验证选项
  form: {
    // 只展示一个错误信息
    validateOnRuleChange: false,
    // 开启内联错误提示，不使用全局消息提示
    inlineMessage: true
  }
}

app.use(router)
   .use(store)
   .use(ElementPlus, elementPlusOptions)
   .use(CommonComponents)  // 注册公共组件
   .use(Directives)  // 注册自定义指令

// 初始化认证状态后挂载应用
initAuthState().then(() => {
  app.mount('#app')
}) 