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
    ├── 状态码映射 (CODE_MAP)
    ├── 静默列表 (SILENT_CODES)
    ├── 状态码映射文档 (code-message-mapping.md)
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
apiMessages.js 检查静默列表
  ↓
  是静默码？ → 不显示消息（仅记录日志）
  否 → 查找状态码映射 → 自动显示对应消息
```

### 静默列表机制

静默列表用于控制哪些状态码不需要显示消息给用户，但仍在开发环境中记录日志：

- **代码层面**：`apiMessages.js` 中的 `SILENT_CODES` 数组
- **文档层面**：`code-message-mapping.md` 中标注 `[静默]` 的状态码
- **同步原则**：代码和文档必须保持一致

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

#### 静默状态码（Silent Status Codes）

某些状态码虽然定义了消息，但不需要显示给用户。系统通过**静默列表**来控制这些状态码的消息显示。

**静默状态码的典型场景：**

1. **查询类接口的成功码**：如 `200`（HTTP成功）、`3000`（茶叶列表加载成功）
   - 用户主动查询数据，成功时不需要额外提示
   - 数据已在界面显示，无需重复提示

2. **后台自动操作**：如 `200`（刷新Token成功）
   - 后台自动刷新，用户无感知，不应打扰用户

3. **轻量级操作**：如 `6011`（已取消点赞）、`3011`（已取消收藏）
   - 操作反馈已通过UI状态变化体现，无需额外消息

**静默列表维护：**

静默列表在 `apiMessages.js` 中维护，与 `code-message-mapping.md` 文档中的标注保持同步：

```javascript
// apiMessages.js
// 静默状态码列表 - 这些状态码不会显示消息
// ⚠️ 重要：此列表必须与 code-message-mapping.md 中的 [静默] 标注完全一致
const SILENT_CODES = [
  // HTTP成功码（查询类）
  200,
  
  // 查询类成功码（用户主动查询，成功时无需提示）
  3000,  // 茶叶列表加载成功
  3001,  // 茶叶详情加载成功
  
  // 后台自动操作
  // refreshToken 的 200 已在上面包含
  
  // 轻量级操作（UI已反馈）
  3011,  // 已取消收藏（茶叶）
  5001,  // 已取消关注
  6011,  // 已取消点赞
  6013,  // 已取消收藏（帖子）
  7010,  // 通知已标记为已读
  7011,  // 所有通知已标记为已读
  
  // ... 其他静默码（完整列表见 code-message-mapping.md）
]
```

**静默码处理逻辑：**

```javascript
export function showByCode(code, customMessage = null) {
  // 1. 检查是否为静默状态码
  if (SILENT_CODES.includes(code)) {
    // 静默码：开发环境记录日志，但不显示消息
    if (process.env.NODE_ENV === 'development') {
      const message = customMessage || getMessageByCode(code)
      console.log(`[静默] ${code}: ${message}`)
    }
    return // 直接返回，不显示消息
  }
  
  // 2. 获取消息
  const message = customMessage || getMessageByCode(code)
  
  // 3. 未知状态码处理：不显示，但记录警告
  if (message.startsWith('未知状态码')) {
    if (process.env.NODE_ENV === 'development') {
      console.warn(`[警告] 未知状态码: ${code}`)
    }
    return
  }
  
  // 4. 正常显示消息
  if (isSuccess(code)) {
    apiMessage.success(message)
  } else {
    apiMessage.error(message)
  }
}
```

**文档标注：**

在 `code-message-mapping.md` 中，静默状态码会在表格中标注 `[静默]`：

```markdown
| Code | 消息 | 场景 | 显示 |
|------|------|------|------|
| 200 | 操作成功 | 获取当前用户信息成功 | [静默] |
| 3000 | 茶叶列表加载成功 | 获取茶叶列表成功 | [静默] |
| 2000 | 登录成功 | 用户登录成功 | 显示 |
```

**注意事项：**

- 静默列表与文档标注必须保持同步
- 新增静默状态码时，需要同时更新代码和文档
- 错误码（x1xx）默认全部显示，不应加入静默列表
- 重要操作的成功码（如登录、注册、支付）必须显示，不应静默

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

**如果该状态码需要静默，在表格中标注：**

```markdown
| Code | 消息 | 场景 | 显示 |
|------|------|------|------|
| 2005 | 新功能操作成功 | 新功能操作成功 | [静默] |
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

#### 步骤3：如果状态码需要静默，添加到 `SILENT_CODES` 列表

```javascript
// 静默状态码列表
const SILENT_CODES = [
  // ... 现有静默码
  2005,  // 新功能操作成功（查询类，无需提示）
]
```

#### 步骤4：后端返回对应状态码

```java
// Controller层
return Result.success(2005, data);  // 成功
return Result.failure(2106);        // 失败
```

#### 步骤5：前端自动显示（无需额外代码）

```javascript
const response = await userApi.newFeature()
showByCode(response.code)  // 自动显示对应消息（静默码不会显示）
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

5. **合理使用静默状态码**
   ```javascript
   // ✅ 正确：查询类接口使用静默码
   const response = await teaApi.getTeaList()
   showByCode(response.code)  // 静默码不会显示，避免干扰用户
   
   // ✅ 正确：重要操作必须显示消息
   const response = await userApi.login()
   showByCode(response.code)  // 登录成功必须提示用户
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

**A:** 系统处理方式：
- **开发环境**：在控制台输出警告 `[警告] 未知状态码: xxx`，不显示消息
- **生产环境**：静默处理，不显示消息
- **建议**：及时在 `apiMessages.js` 的 `CODE_MAP` 中添加映射，并在 `code-message-mapping.md` 中记录

### Q4: 可以自定义消息显示方式吗？

**A:** 可以，修改 `apiMessages.js` 中的 `showByCode` 函数，但建议保持统一风格。

### Q5: 提示消息支持国际化吗？

**A:** 当前版本不支持，但架构已预留扩展空间。未来可以在 `promptMessages.js` 中添加多语言支持。

### Q6: 如何查找某个状态码对应的消息？

**A:** 
1. 查看 `docs/code-message-mapping.md` 文档
2. 或在 `src/utils/apiMessages.js` 的 `CODE_MAP` 中搜索

### Q7: 如何判断一个状态码是否静默？

**A:** 
1. 查看 `docs/code-message-mapping.md` 文档，表格中标注了 `[静默]` 的状态码
2. 或在 `src/utils/apiMessages.js` 的 `SILENT_CODES` 列表中查找
3. 静默状态码调用 `showByCode()` 时不会显示消息，但仍会记录日志（开发环境）

### Q8: 什么时候应该将状态码设为静默？

**A:** 以下情况应该考虑静默：
- **查询类接口的成功码**：用户主动查询，数据已在界面显示
- **后台自动操作**：如Token刷新，用户无感知
- **轻量级操作**：如取消点赞，UI状态已反馈
- **高频操作**：如滚动加载，避免消息刷屏

**不应静默的情况：**
- 重要操作的成功码（登录、注册、支付、下单等）
- 所有错误码（x1xx）必须显示
- 用户主动提交的操作（创建、更新、删除等）

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
- [ ] **判断是否需要静默**：如果是查询类或后台操作，添加到 `SILENT_CODES` 列表
- [ ] **在文档中标注**：如果静默，在 `code-message-mapping.md` 表格中标注 `[静默]`
- [ ] 更新本文档（如果涉及新模块或重大变更）
- [ ] 测试消息显示是否正确（静默码不应显示消息）

### 文件修改优先级

1. **高优先级**：`apiMessages.js`、`promptMessages.js`（核心文件）
2. **中优先级**：`code-message-mapping.md`（文档同步，包括静默标注）
3. **低优先级**：本文档（使用指南更新）

### 静默列表维护规范

**同步更新原则（⚠️ 强制要求）：**
- **代码与文档必须完全同步**：`apiMessages.js` 中的 `SILENT_CODES` 列表必须与 `code-message-mapping.md` 中的 `[静默]` 标注完全一致
- **双向同步**：
  - 新增静默状态码时，必须同时更新代码和文档
  - 移除静默状态码时，必须同时从代码和文档中删除
  - 修改静默状态码时，必须同时更新代码和文档
- **验证机制**：每次修改后，必须验证代码和文档的一致性

**维护流程：**
1. **在文档中标注**：在 `code-message-mapping.md` 中标注需要静默的状态码（添加 `[静默]`）
2. **在代码中添加**：在 `apiMessages.js` 的 `SILENT_CODES` 列表中添加对应状态码
3. **验证一致性**：检查文档中所有 `[静默]` 标注的状态码是否都在 `SILENT_CODES` 中
4. **测试验证**：调用 `showByCode()` 时不应显示消息，但开发环境应记录日志
5. **提交检查**：提交代码前，确保代码和文档同步更新

**状态码映射同步规范（⚠️ 强制要求）：**
- **CODE_MAP 与文档必须完全同步**：`apiMessages.js` 中的 `CODE_MAP` 必须包含 `code-message-mapping.md` 中列出的所有状态码
- **数据来源**：`CODE_MAP` 的数据必须从 `code-message-mapping.md` 中提取，确保一致性
- **去重规则**：同一状态码在不同接口中出现时，只保留一个消息（通常消息是一致的）
- **更新流程**：
  1. 在 `code-message-mapping.md` 中添加或修改状态码映射
  2. 同步更新 `apiMessages.js` 中的 `CODE_MAP`
  3. 验证所有状态码都已正确映射

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

