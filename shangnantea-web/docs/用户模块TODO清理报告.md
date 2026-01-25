# 用户模块TODO清理报告

## 清理时间
2026-01-25

## 清理范围
- `shangnantea-web/src/views/user/` - 用户模块前端页面
- `shangnantea-web/src/store/modules/user.js` - 用户模块Vuex状态管理
- `shangnantea-web/src/api/user.js` - 用户模块API接口

## 清理结果

### ✅ 已清理的TODO（共10个）

#### 1. LoginPage.vue（2个）
- **位置**: `resetLocalStorage()` 方法
- **原因**: 本地存储重置操作，不涉及API响应，正确使用`userMessages`
- **状态**: 已删除TODO标记

- **位置**: `migrateData()` 方法
- **原因**: 本地数据迁移操作，不涉及API响应，正确使用`userMessages`
- **状态**: 已删除TODO标记

#### 2. user.js Vuex Store（8个）
- **位置**: `uploadAvatar()` - 文件验证部分（2个）
- **原因**: 前端文件格式和大小验证，不涉及API响应，正确使用`userPromptMessages`
- **状态**: 已删除TODO标记

- **位置**: `uploadAvatar()` - 错误处理部分（2个）
- **原因**: 上传失败的错误处理，使用`userPromptMessages`显示错误信息
- **状态**: 已删除TODO标记

- **位置**: `changePassword()` - 密码匹配验证（2个）
- **原因**: 前端密码匹配验证，不涉及API响应，正确使用`userPromptMessages`
- **状态**: 已删除TODO标记

- **位置**: `findPassword()` - 成功/失败消息（2个）
- **原因**: 密码找回操作的消息提示，正确使用`userPromptMessages`
- **状态**: 已删除TODO标记

### ⚠️ 保留的TODO（共11个）

#### 1. UserManagePage.vue（10个）- Vuex Actions不返回response.code

**问题根源**: 
管理员相关的Vuex actions（`deleteUser`, `toggleUserStatus`, `updateUser`, `createAdmin`, `fetchUserList`）只返回`true`或抛出异常，不返回`{code, data}`格式。

**保留原因**:
这些TODO标记了一个架构问题：Vuex actions应该返回完整的API响应`{code, data}`，以便组件层可以使用`showByCode(response.code)`统一处理消息。

**当前实现**:
```javascript
// Vuex action
async deleteUser({ commit }, userId) {
  await deleteUserApi(userId)
  commit('REMOVE_USER_FROM_LIST', userId)
  return true  // ❌ 只返回true，没有code
}

// 组件调用
await store.dispatch('user/deleteUser', userId)
// TODO: 迁移到新消息系统 - 使用 showByCode(response.code)
userMessages.success.showUserDeleted(username)  // 只能手动显示消息
```

**期望实现**:
```javascript
// Vuex action
async deleteUser({ commit }, userId) {
  const res = await deleteUserApi(userId)  // 返回 {code, data}
  commit('REMOVE_USER_FROM_LIST', userId)
  return res  // ✅ 返回完整响应
}

// 组件调用
const res = await store.dispatch('user/deleteUser', userId)
showByCode(res.code)  // ✅ 统一消息处理
```

**涉及的TODO位置**:
1. `fetchUserList()` - 获取用户列表失败（1个）
2. `deleteUser()` - 删除用户成功/失败（2个）
3. `toggleUserStatus()` - 切换用户状态成功/失败（2个）
4. `updateUser()` - 更新用户成功（1个）
5. `createAdmin()` - 创建管理员成功（1个）
6. `submitUserForm()` - 提交表单失败（1个）
7. `beforeAvatarUpload()` - 头像格式/大小错误（2个）

**建议**:
未来重构时，修改这些Vuex actions返回完整的API响应，然后可以统一使用`showByCode()`。

#### 2. ResetPasswordPage.vue（1个）- 未实现的API

**位置**: `sendVerificationCode()` 方法

**TODO内容**:
```javascript
// TODO: 当后端API实现后，改为 const res = await store.dispatch('user/sendCaptcha', data); showByCode(res.code)
```

**保留原因**:
这是一个未来工作的提醒。当前使用模拟发送验证码，等后端API实现后需要替换为真实API调用。

**当前实现**:
```javascript
// 模拟发送验证码（实际应该调用后端API）
userPromptMessages.showCaptchaSent()
```

**期望实现**:
```javascript
const res = await store.dispatch('user/sendCaptcha', {
  method: resetForm.method,
  username: resetForm.username,
  phone: resetForm.phone,
  email: resetForm.email
})
showByCode(res.code)
```

## 清理原则总结

### 应该删除TODO的情况：
1. **前端验证**: 文件格式、大小、密码匹配等前端验证，使用`userMessages`或`userPromptMessages`
2. **本地操作**: localStorage操作、数据迁移等不涉及API的操作
3. **已正确实现**: 代码已经按照正确的模式实现，TODO只是历史遗留

### 应该保留TODO的情况：
1. **架构问题**: Vuex actions不返回response.code，无法使用`showByCode()`
2. **未实现功能**: 后端API尚未实现，需要等待后续开发
3. **未来重构**: 标记需要在未来重构时处理的代码

## 消息系统使用规范

### API响应消息（使用showByCode）
```javascript
const res = await store.dispatch('user/someAction', data)
showByCode(res.code)  // 自动根据code显示对应消息
```

### 前端验证消息（使用userMessages/userPromptMessages）
```javascript
if (!file) {
  userPromptMessages.showFormIncomplete()
  return
}

if (password !== confirmPassword) {
  userMessages.error.showPasswordMismatch()
  return
}
```

### 本地操作消息（使用userMessages）
```javascript
localStorage.clear()
userMessages.success.showStorageReset()
```

## 下一步行动

### 短期（当前分支）
- ✅ 清理已正确实现的TODO
- ✅ 保留架构问题和未实现功能的TODO
- ✅ 文档化保留TODO的原因

### 中期（未来重构）
1. 修改UserManagePage相关的Vuex actions，返回完整API响应
2. 实现验证码发送API
3. 统一使用`showByCode()`处理所有API响应消息

### 长期（架构优化）
1. 建立Vuex action返回值规范
2. 统一API响应处理模式
3. 完善消息系统文档

## 附录：剩余TODO清单

### UserManagePage.vue
```
Line 394: // TODO: 迁移到新消息系统 - 使用 showByCode(response.code)
Line 543: // TODO: 迁移到新消息系统 - 使用 showByCode(response.code)
Line 545: // TODO: [user] 迁移到 showByCode(response.code) - success
Line 550: // TODO: 迁移到新消息系统 - 使用 showByCode(response.code)
Line 564: // TODO: 迁移到新消息系统 - 使用 showByCode(response.code)
Line 569: // TODO: 迁移到新消息系统 - 使用 showByCode(response.code)
Line 599: // TODO: 迁移到新消息系统 - 使用 showByCode(response.code)
Line 616: // TODO: 迁移到新消息系统 - 使用 showByCode(response.code)
Line 626: // TODO: 迁移到新消息系统 - 使用 showByCode(response.code)
Line 642: // TODO: 迁移到新消息系统 - 使用 showByCode(response.code)
Line 648: // TODO: 迁移到新消息系统 - 使用 showByCode(response.code)
```

### ResetPasswordPage.vue
```
Line 164: // TODO: 当后端API实现后，改为 const res = await store.dispatch('user/sendCaptcha', data); showByCode(res.code)
```

### ProfilePage.vue
```
Line 147: // TODO-SCRIPT: 用户信息应从 Vuex user 模块获取（user/getUserInfo），此处不在 UI 层保留 mock 声明
```
（注：这是代码架构提醒，不是消息系统迁移相关）
