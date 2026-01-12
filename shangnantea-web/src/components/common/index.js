// 导入公共组件
// 避免直接导入NavBar，防止循环依赖
// import NavBar from './layout/NavBar.vue'
// 避免直接导入Footer，防止循环依赖
// import Footer from './layout/Footer.vue'
import { ElMessage, ElLoading } from 'element-plus'

// 动态导入组件
const getNavBar = () => import('./layout/NavBar.vue')
const getFooter = () => import('./layout/Footer.vue')

// 组件列表 - 不包含直接导入的组件
const components = [
  // Footer - 改为动态导入
]

// 消息提示封装
const message = {
  // 消息计数器，防止相同消息在短时间内重复显示
  _messages: {},
  
  // 清理10秒前的消息记录
  _cleanup() {
    const now = Date.now()
    Object.keys(this._messages).forEach(key => {
      if (now - this._messages[key] > 10000) {
        delete this._messages[key]
      }
    })
  },
  
  // 检查消息是否已显示（3秒内）- 缩短检测重复时间，更积极地防止重复
  _isDuplicate(msg) {
    const now = Date.now()
    const lastTime = this._messages[msg]
    
    // 将消息重复检测窗口从3秒缩短到1秒，更严格防止重复
    if (lastTime && now - lastTime < 1000) {
      console.log('拦截重复消息:', msg) // 添加日志帮助调试
      return true
    }
    
    this._messages[msg] = now
    this._cleanup()
    return false
  },
  
  // 清除特定消息的状态
  clearMessage(msg) {
    if (msg && this._messages[msg]) {
      delete this._messages[msg]
    }
  },
  
  // 清除所有消息状态
  clearAllMessages() {
    this._messages = {}
    console.log('已清除所有消息状态')
  },
  
  success(msg, options = {}) {
    // 默认启用重复检测
    if (!msg || this._isDuplicate(msg)) return
    ElMessage.success(msg)
  },
  
  warning(msg, options = {}) {
    // 默认启用重复检测
    if (!msg || this._isDuplicate(msg)) return
    ElMessage.warning(msg)
  },
  
  info(msg, options = {}) {
    // 默认启用重复检测
    if (!msg || this._isDuplicate(msg)) return
    ElMessage.info(msg)
  },
  
  error(msg, options = {}) {
    // 默认启用重复检测
    if (!msg || this._isDuplicate(msg)) return
    ElMessage.error(msg)
  }
}

// 加载封装
const loading = {
  show: options => ElLoading.service(options),
  hide: () => {
    ElLoading.service().close()
  }
}

// 安装插件
const install = app => {
  // 全局注册组件
  components.forEach(component => {
    app.component(component.name, component)
  })
  
  // 动态注册NavBar组件
  getNavBar().then(module => {
    app.component('NavBar', module.default)
  })
  
  // 动态注册Footer组件
  getFooter().then(module => {
    app.component('Footer', module.default)
  })
  
  // 添加全局属性
  app.config.globalProperties.$message = message
  app.config.globalProperties.$loading = loading
}

// 动态获取Footer组件的函数
export const Footer = () => getFooter()

export {
  message,
  loading
}

export default {
  install
} 