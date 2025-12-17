# 商南茶文化平台消息系统使用指南

## 1. 消息系统概述

商南茶文化平台消息系统基于三层架构设计，确保消息提示的一致性和规范性：

1. **API层**：处理网络请求相关消息，如加载状态、请求失败等
2. **业务层**：处理业务操作结果消息，如操作成功、业务规则验证等
3. **UI交互层**：处理界面交互提示消息，如表单验证、操作确认等

## 2. 消息工具结构

消息系统由以下主要组件组成：

- `messageManager.js`：基础消息管理器，提供核心消息处理功能
- 模块消息工具：按业务模块划分的专用消息工具
  - `userMessages.js`：用户模块消息工具
  - `teaMessages.js`：茶叶模块消息工具
  - `orderMessages.js`：订单模块消息工具（包含购物车和支付相关消息）
  - `forumMessages.js`：论坛模块消息工具
  - `shopMessages.js`：店铺模块消息工具
  - `messageMessages.js`：消息中心模块消息工具
- `messages.js`：统一导出文件，便于一次性导入所有消息工具
- `MessageMonitor.vue`：消息监控面板组件，用于开发调试

## 3. 如何使用消息系统

### 3.1 在组件中使用

```javascript
<script>
import { userMessages } from '@/utils/messages'

export default {
  methods: {
    handleLoginSuccess() {
      // 使用业务层消息显示登录成功
      userMessages.business.showLoginSuccess('张三')
    },
    validateForm() {
      // 使用UI层消息显示表单验证结果
      if (!this.username) {
        userMessages.ui.showUsernameRequired()
        return false
      }
      return true
    }
  }
}
</script>
```

### 3.2 在Vuex中使用

```javascript
// store/modules/tea.js
import { teaMessages } from '@/utils/messages'

const actions = {
  async fetchTeaList({ commit }) {
    try {
      // 调用API获取茶叶列表...
      const result = await getTeaList()
      commit('SET_TEA_LIST', result.data)
      
      // 使用业务层消息显示加载成功
      teaMessages.business.showListSuccess(result.data.length)
    } catch (error) {
      // 使用业务层消息显示加载失败
      teaMessages.business.showListError(error.message)
    }
  }
}
```

### 3.3 在API层使用

```javascript
// api/index.js
import { messageManager } from '@/utils/messages'

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 处理请求配置...
    return config
  },
  error => {
    // 使用API层消息显示请求错误
    messageManager.api.error('网络请求失败，请检查网络连接')
    return Promise.reject(error)
  }
)
```

### 3.4 使用订单模块整合的购物车和支付消息

订单模块消息工具(`orderMessages.js`)已整合了购物车和支付相关功能，无需再单独导入购物车或支付消息工具。

```javascript
// 购物车组件中使用
import { orderMessages } from '@/utils/messages'

export default {
  methods: {
    addToCart(product) {
      try {
        // 添加到购物车...
        
        // 显示添加成功消息
        orderMessages.business.showAddToCartSuccess(product.name, 2)
      } catch (error) {
        // 显示添加失败消息
        orderMessages.business.showAddToCartFailure(error.message)
      }
    },
    
    // 支付相关操作
    async payOrder(orderId) {
      try {
        // 处理支付...
        
        // 显示支付成功消息
        orderMessages.business.showPaymentSuccess(orderId)
      } catch (error) {
        // 显示支付失败消息
        orderMessages.business.showPaymentFailure(error.message)
      }
    }
  }
}
```

## 4. 使用统一导入

为了简化导入过程，可以使用统一导出的`messages.js`：

```javascript
import messages from '@/utils/messages'

// 使用不同模块的消息工具
messages.user.business.showLoginSuccess()
messages.tea.api.showLoadingTeaList()
messages.order.business.showOrderCreated()
messages.order.business.showAddToCartSuccess('西湖龙井', 2) // 购物车消息
messages.order.business.showPaymentSuccess('20230101001') // 支付消息
messages.forum.ui.showTitleRequired()
messages.shop.business.showShopCreated()
messages.message.business.showMessageSent()
```

## 5. 开发工具使用

消息系统提供了一些开发辅助工具，方便在开发过程中监控和管理消息：

```javascript
import { devHelper } from '@/utils/messages'

// 显示所有可用的消息常量
const allConstants = devHelper.showAllConstants()

// 查找包含特定文本的消息
const results = devHelper.findMessageByText('成功')
console.log(results) // 列出所有包含"成功"的消息常量

// 打开消息监控面板
devHelper.openMonitor()

// 关闭消息监控面板
devHelper.closeMonitor()

// 清除所有消息状态
devHelper.clearAllStates()
```

## 6. 消息监控面板

系统提供了一个消息监控面板，在开发环境中可以显示所有消息的触发记录：

- 按下 `Ctrl+Alt+M` 可以切换消息监控面板的显示状态
- 面板显示最近50条消息，包括消息内容、类型、来源和时间
- 可以按模块、层级和类型筛选消息
- 可以清空消息记录

## 7. 消息系统配置

可以在`main.js`中进行全局配置：

```javascript
// main.js
import messageManager from '@/utils/messageManager'

// 设置消息重复检测窗口时间
messageManager.setDuplicateWindow(2000) // 2秒内相同消息只显示一次

// 设置消息队列最大长度
messageManager.setQueueMaxLength(10) // 最多同时显示10条消息

// 开发环境下启用消息日志
if (process.env.NODE_ENV === 'development') {
  messageManager.enableLogging()
}
```

## 8. 消息规范与最佳实践

### 8.1 消息分层标准

* **API层消息**：与网络请求相关的消息
  * 加载状态消息: `showLoading()`
  * 网络错误消息: `showError()`
  * 接口超时消息: `showTimeout()`

* **业务层消息**：与业务操作结果相关的消息
  * 操作成功消息: `showSuccess()`
  * 操作失败消息: `showFailure()`
  * 业务警告消息: `showWarning()`

* **UI层消息**：与用户界面交互相关的消息
  * 验证错误消息: `showRequired()`, `showInvalid()`
  * 操作确认消息: `showConfirm()`
  * 信息提示消息: `showInfo()`

### 8.2 消息命名规范

* API层方法命名: `showXxxing`, `showXxxFailed`
* 业务层方法命名: `showXxxSuccess`, `showXxxFailure`
* UI层方法命名: `showXxxRequired`, `showXxxInvalid`, `showXxxConfirm`

### 8.3 避免消息重复

* 同一操作不应在多个层次重复显示消息
* 登录成功消息应只在业务层显示一次，不应在组件层重复显示
* 网络请求错误应只在API层处理，不应在业务层重复处理

## 9. 如何扩展消息系统

### 9.1 添加新的模块消息工具

创建新的模块消息工具遵循以下步骤：

1. 在`src/utils/`目录下创建新文件，命名为`{module}Messages.js`
2. 按三层架构定义消息常量和展示方法
3. 在`messages.js`中导入和导出新模块

### 9.2 添加新的消息类型

在现有模块中添加新的消息类型：

1. 在对应模块的`MESSAGES`常量中添加新消息
2. 在对应的层级中实现新的显示方法
3. 确保新方法的命名符合规范

## 10. 故障排除

### 10.1 常见问题

* **消息没有显示**：检查是否触发了重复检测机制
* **消息显示多次**：检查是否在多个地方触发了同一消息
* **消息内容错误**：检查消息常量定义是否正确

### 10.2 诊断工具

* 使用`devHelper.showAllConstants()`查看所有可用消息
* 使用消息监控面板观察消息触发情况
* 查看浏览器控制台中的消息日志

### 10.3 提交问题

如遇到消息系统问题，请提供以下信息：

* 问题描述和重现步骤
* 相关代码片段和截图
* 控制台错误信息
* 开发环境信息 