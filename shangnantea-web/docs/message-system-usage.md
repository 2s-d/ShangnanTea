# 新消息系统使用指南

## 概述

消息系统已重构为两个文件：
- `promptMessages.js` - 纯前端提示消息
- `apiMessages.js` - 基于状态码的API消息

## 使用方法

### 1. 提示消息 (promptMessages.js)

用于表单验证、用户确认等前端提示：

```javascript
import { userPromptMessages } from '@/utils/promptMessages'

// 使用方式不变
userPromptMessages.showUsernameRequired()
userPromptMessages.showPasswordRequired()
```

### 2. API消息 (apiMessages.js)

用于API响应的消息显示：

```javascript
import { showByCode, isSuccess } from '@/utils/apiMessages'

// 旧方式 (需要替换)
userMessages.success.showLoginSuccess()
userMessages.error.showLoginFailure()

// 新方式
async function login() {
  try {
    const response = await userApi.login(credentials)
    
    // 拦截器现在返回 {code, data} 格式
    if (isSuccess(response.code)) {
      showByCode(response.code) // 自动显示成功消息
      return response.data
    } else {
      showByCode(response.code) // 自动显示错误消息
      throw new Error('登录失败')
    }
  } catch (error) {
    // 网络错误等已由拦截器处理
    throw error
  }
}
```

### 3. 迁移步骤

#### 步骤1：更新导入语句

```javascript
// 旧方式
import userMessages from '@/utils/userMessages'
import { orderSuccessMessages, orderErrorMessages } from '@/utils/orderMessages'

// 新方式
import { userPromptMessages } from '@/utils/promptMessages'
import { showByCode, isSuccess } from '@/utils/apiMessages'
```

#### 步骤2：更新API调用

```javascript
// 旧方式
async function createOrder(orderData) {
  try {
    const result = await orderApi.create(orderData)
    orderMessages.success.showOrderCreated(result.orderNo)
    return result
  } catch (error) {
    orderMessages.error.showOrderCreateFailed(error.message)
    throw error
  }
}

// 新方式
async function createOrder(orderData) {
  try {
    const response = await orderApi.create(orderData)
    
    if (isSuccess(response.code)) {
      showByCode(response.code) // 显示 "订单创建成功"
      return response.data
    } else {
      showByCode(response.code) // 显示对应错误消息
      throw new Error('创建订单失败')
    }
  } catch (error) {
    throw error
  }
}
```

#### 步骤3：保留提示消息

```javascript
// 表单验证等提示消息保持不变
userPromptMessages.showUsernameRequired()
orderPromptMessages.showAddressRequired()
```

## 状态码映射

参考 `docs/code-message-mapping.md` 了解完整的状态码映射关系。

### 成功码规则
- HTTP 200: 通用成功
- x0xx: 各模块成功码 (1000-1049, 2000-2049, ...)

### 错误码规则  
- HTTP 4xx/5xx: HTTP错误
- x1xx: 各模块错误码 (1100-1199, 2100-2199, ...)

## 优势

1. **统一管理**: 所有状态码消息集中管理
2. **自动映射**: 根据状态码自动显示对应消息
3. **类型安全**: 减少硬编码字符串
4. **易于维护**: 消息修改只需更新映射表

## 注意事项

1. 拦截器现在返回 `{code, data}` 格式，不再自动显示消息
2. 认证错误(401/403)仍由拦截器自动处理
3. 网络错误仍由拦截器自动显示错误消息
4. 提示消息(PROMPT)继续使用原有方式