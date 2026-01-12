# 消息提示系统使用指南

## 📋 目录

1. [系统概述](#系统概述)
2. [系统架构](#系统架构)
3. [文件结构](#文件结构)
4. [使用指南](#使用指南)
5. [如何添加新消息](#如何添加新消息)
6. [最佳实践](#最佳实践)
7. [常见问题](#常见问题)

---

## 系统概述

本项目采用**双轨制消息系统**，将消息分为两类：

1. **提示消息（Prompt Messages）**：纯前端提示，用于表单验证、用户确认等
2. **API消息（API Messages）**：基于状态码的API响应消息，由后端状态码自动映射

### 设计理念

- **统一管理**：所有消息集中管理，便于维护和国际化
- **自动映射**：API响应通过状态码自动显示对应消息，减少重复代码
- **类型安全**：减少硬编码字符串，降低错误率
- **易于扩展**：新增消息只需更新映射表，无需修改业务代码

---

## 系统架构

```
消息系统
├── 提示消息系统 (promptMessages.js)
│   └── 用于：表单验证、用户确认、前端提示
│
└── API消息系统 (apiMessages.js)
    ├── 状态码映射 (code-message-mapping.md)
    └── 用于：API响应消息自动显示
```

### 数据流向

```
用户操作
  ↓
组件调用 API
  ↓
API 返回 { code, data }
  ↓
组件调用 showByCode(code)
  ↓
apiMessages.js 查找状态码映射
  ↓
自动显示对应消息
```

---

## 文件结构

### 核心文件

| 文件路径 | 用途 | 说明 |
|---------|------|------|
| `src/utils/promptMessages.js` | 提示消息定义 | 包含所有模块的前端提示消息 |
| `src/utils/apiMessages.js` | API消息映射 | 状态码到消息的映射和显示函数 |
| `docs/code-message-mapping.md` | 状态码映射文档 | 完整的状态码规则和消息定义 |

### 文件位置

```
shangnantea-web/
├── src/
│   └── utils/
│       ├── promptMessages.js      # 提示消息系统
│       └── apiMessages.js        # API消息系统
│
└── docs/
    ├── code-message-mapping.md   # 状态码映射文档
    └── message-system-guide.md   # 本文档
```

---

## 使用指南

### 1. 提示消息（Prompt Messages）

用于表单验证、用户确认等纯前端提示。

#### 导入方式

```javascript
import { 
  commonPromptMessages,    // 通用提示消息
  userPromptMessages,      // 用户模块提示消息
  teaPromptMessages,       // 茶叶模块提示消息
  orderPromptMessages,     // 订单模块提示消息
  shopPromptMessages,      // 店铺模块提示消息
  forumPromptMessages,     // 论坛模块提示消息
  messagePromptMessages    // 消息模块提示消息
} from '@/utils/promptMessages'
```

#### 使用示例

```javascript
// 表单验证
if (!username) {
  userPromptMessages.showUsernameRequired()
  return
}

// 文件上传验证
if (file.size > 2 * 1024 * 1024) {
  commonPromptMessages.showFileSizeExceeded(2)
  return
}

// 用户确认
if (!userPromptMessages.showConfirmLogout()) {
  return
}
```

### 2. API消息（API Messages）

用于API响应消息的自动显示。

#### 导入方式

```javascript
import { showByCode, isSuccess } from '@/utils/apiMessages'
```

#### 使用示例

```javascript
// 基本用法
async function login(credentials) {
  try {
    const response = await userApi.login(credentials)
    
    // 检查是否成功
    if (isSuccess(response.code)) {
      showByCode(response.code)  // 自动显示成功消息
      return response.data
    } else {
      showByCode(response.code)  // 自动显示错误消息
      throw new Error('登录失败')
    }
  } catch (error) {
    // 网络错误等已由拦截器处理
    throw error
  }
}

// 简化写法（如果只需要显示消息）
async function updateUserInfo(userData) {
  const response = await userApi.updateUserInfo(userData)
  showByCode(response.code)  // 无论成功失败都显示消息
  return response.data
}
```

#### 状态码判断

```javascript
import { isSuccess } from '@/utils/apiMessages'

const response = await someApi()
if (isSuccess(response.code)) {
  // 成功处理
} else {
  // 失败处理
}
```

---

## 如何添加新消息

### 场景1：添加新的提示消息（前端验证）

#### 步骤1：在 `promptMessages.js` 中添加常量

```javascript
// 在对应模块的 PROMPT 对象中添加
export const USER_PROMPT = {
  // ... 现有常量
  NEW_FIELD_REQUIRED: '请输入新字段',  // 新增
  NEW_FIELD_INVALID: '新字段格式不正确'  // 新增
}
```

#### 步骤2：在对应模块的 `xxxPromptMessages` 对象中添加方法

```javascript
export const userPromptMessages = {
  // ... 现有方法
  showNewFieldRequired() {
    promptMessage.show(USER_PROMPT.NEW_FIELD_REQUIRED)
  },
  showNewFieldInvalid() {
    promptMessage.show(USER_PROMPT.NEW_FIELD_INVALID)
  }
}
```

#### 步骤3：在组件中使用

```javascript
import { userPromptMessages } from '@/utils/promptMessages'

if (!newField) {
  userPromptMessages.showNewFieldRequired()
  return
}
```

### 场景2：添加新的API消息（后端状态码）

#### 步骤1：在 `code-message-mapping.md` 中添加状态码映射

```markdown
### 用户模块 (2000-2199)

#### 成功码 (2000-2049)
- **2005**: 新功能操作成功

#### 错误码 (2100-2199)
- **2106**: 新功能操作失败
- **2107**: 新字段验证失败
```

#### 步骤2：在 `apiMessages.js` 的 `CODE_MAP` 中添加映射

```javascript
const CODE_MAP = {
  // ... 现有映射
  2005: '新功能操作成功',
  2106: '新功能操作失败',
  2107: '新字段验证失败'
}
```

#### 步骤3：后端返回对应状态码

```java
// Controller层
return Result.success(2005, data);  // 成功
return Result.failure(2106);        // 失败
```

#### 步骤4：前端自动显示（无需额外代码）

```javascript
const response = await userApi.newFeature()
showByCode(response.code)  // 自动显示对应消息
```

### 场景3：添加新模块的消息

#### 步骤1：在 `promptMessages.js` 中添加新模块

```javascript
// 新增模块常量
export const NEW_MODULE_PROMPT = {
  FIELD_REQUIRED: '请输入字段',
  OPERATION_SUCCESS: '操作成功'
}

// 新增模块提示消息对象
export const newModulePromptMessages = {
  showFieldRequired() {
    promptMessage.show(NEW_MODULE_PROMPT.FIELD_REQUIRED)
  },
  showOperationSuccess() {
    promptMessage.show(NEW_MODULE_PROMPT.OPERATION_SUCCESS)
  }
}
```

#### 步骤2：在 `code-message-mapping.md` 中添加状态码段

```markdown
### 新模块 (6000-6199)

#### 成功码 (6000-6049)
- **6000**: 操作成功

#### 错误码 (6100-6199)
- **6100**: 操作失败
```

#### 步骤3：在 `apiMessages.js` 中添加状态码映射

```javascript
const CODE_MAP = {
  // ... 现有映射
  6000: '操作成功',
  6100: '操作失败'
}
```

---

## 最佳实践

### ✅ 推荐做法

1. **提示消息用于前端验证**
   ```javascript
   // ✅ 正确：表单验证使用提示消息
   if (!email) {
     userPromptMessages.showEmailRequired()
     return
   }
   ```

2. **API消息用于后端响应**
   ```javascript
   // ✅ 正确：API响应使用状态码消息
   const response = await userApi.updateEmail()
   showByCode(response.code)
   ```

3. **统一使用 showByCode**
   ```javascript
   // ✅ 正确：统一使用 showByCode
   showByCode(response.code)
   
   // ❌ 错误：不要直接使用 ElMessage
   ElMessage.success('操作成功')
   ```

4. **状态码判断使用 isSuccess**
   ```javascript
   // ✅ 正确：使用 isSuccess 判断
   if (isSuccess(response.code)) {
     // 成功处理
   }
   ```

### ❌ 避免做法

1. **不要混用消息系统**
   ```javascript
   // ❌ 错误：不要混用
   userPromptMessages.showEmailRequired()  // 提示消息
   ElMessage.success('操作成功')           // 直接使用 Element Plus
   ```

2. **不要在API响应中使用提示消息**
   ```javascript
   // ❌ 错误：API响应应该用状态码消息
   const response = await userApi.login()
   if (response.code === 2000) {
     userPromptMessages.showLoginSuccess()  // 错误！
   }
   
   // ✅ 正确：使用 showByCode
   showByCode(response.code)
   ```

3. **不要硬编码消息文本**
   ```javascript
   // ❌ 错误：硬编码
   ElMessage.success('登录成功')
   
   // ✅ 正确：使用消息系统
   showByCode(2000)
   ```

---

## 状态码规则

### 状态码分段

| 模块 | 成功码范围 | 错误码范围 | 说明 |
|------|-----------|-----------|------|
| 通用 | 1000-1049 | 1100-1199 | 通用操作 |
| 用户 | 2000-2049 | 2100-2199 | 用户相关 |
| 茶叶 | 3000-3049 | 3100-3199 | 茶叶相关 |
| 订单 | 4000-4049 | 4100-4199 | 订单相关 |
| 店铺 | 5000-5049 | 5100-5199 | 店铺相关 |
| 论坛 | 6000-6049 | 6100-6199 | 论坛相关 |
| 消息 | 7000-7049 | 7100-7199 | 消息相关 |

### 状态码判断规则

- **成功码**：`x0xx` 格式（如 2000, 3001）
- **错误码**：`x1xx` 格式（如 2100, 3101）
- **HTTP状态码**：4xx/5xx 自动映射为错误

### 查看完整映射

详细的状态码映射请参考：`docs/code-message-mapping.md`

---

## 常见问题

### Q1: 什么时候使用提示消息，什么时候使用API消息？

**A:** 
- **提示消息**：表单验证、用户确认、前端校验等不涉及API的场景
- **API消息**：所有API响应都应该使用状态码消息系统

### Q2: 如何判断一个状态码是成功还是失败？

**A:** 使用 `isSuccess()` 函数：
```javascript
import { isSuccess } from '@/utils/apiMessages'

if (isSuccess(response.code)) {
  // 成功
} else {
  // 失败
}
```

### Q3: 如果后端返回的状态码在映射表中不存在怎么办？

**A:** 系统会显示默认消息：
- 成功码：显示 "操作成功"
- 错误码：显示 "操作失败"
- 建议：及时在 `apiMessages.js` 中添加映射

### Q4: 可以自定义消息显示方式吗？

**A:** 可以，修改 `apiMessages.js` 中的 `showByCode` 函数，但建议保持统一风格。

### Q5: 提示消息支持国际化吗？

**A:** 当前版本不支持，但架构已预留扩展空间。未来可以在 `promptMessages.js` 中添加多语言支持。

### Q6: 如何查找某个状态码对应的消息？

**A:** 
1. 查看 `docs/code-message-mapping.md` 文档
2. 或在 `src/utils/apiMessages.js` 的 `CODE_MAP` 中搜索

---

## 迁移指南

### 从旧消息系统迁移

如果发现代码中仍在使用旧的消息系统：

1. **识别旧代码**
   ```javascript
   // 旧代码
   import userMessages from '@/utils/userMessages'
   userMessages.success.showLoginSuccess()
   ```

2. **替换为新系统**
   ```javascript
   // 新代码
   import { showByCode } from '@/utils/apiMessages'
   showByCode(response.code)
   ```

3. **运行检测脚本**
   ```bash
   node tools/detect-old-messages.js
   ```

---

## 维护者注意事项

### 添加新消息时的检查清单

- [ ] 在 `promptMessages.js` 中添加常量和方法（如果是提示消息）
- [ ] 在 `code-message-mapping.md` 中记录状态码（如果是API消息）
- [ ] 在 `apiMessages.js` 的 `CODE_MAP` 中添加映射（如果是API消息）
- [ ] 更新本文档（如果涉及新模块或重大变更）
- [ ] 测试消息显示是否正确

### 文件修改优先级

1. **高优先级**：`apiMessages.js`、`promptMessages.js`（核心文件）
2. **中优先级**：`code-message-mapping.md`（文档同步）
3. **低优先级**：本文档（使用指南更新）

---

## 相关文档

- [状态码映射文档](./code-message-mapping.md) - 完整的状态码规则和映射
- [消息系统使用说明](./message-system-usage.md) - 快速使用指南
- [API拦截器配置](../src/api/index.js) - API响应拦截逻辑

---

## 更新日志

- **2024-12-30**: 创建消息系统使用指南
- **2024-12-30**: 完成消息系统重构，统一使用新系统

---

## 联系方式

如有问题或建议，请联系项目维护者。

