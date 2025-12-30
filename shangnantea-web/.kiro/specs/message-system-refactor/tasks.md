# Implementation Plan: 消息系统重构

## Overview

分三步完成消息系统重构：
1. 创建 promptMessages.js（纯前端提示消息）
2. 创建状态码映射文档
3. 创建 apiMessages.js + 修改拦截器 + 清理旧文件

## Tasks

- [x] 1. 第一步：创建 promptMessages.js
  - [x] 1.1 创建 `src/utils/promptMessages.js` 文件
    - 从 7 个模块文件中原封不动提取 PROMPT 常量和函数
    - 包含：COMMON_PROMPT、USER_PROMPT、TEA_PROMPT、ORDER_PROMPT、SHOP_PROMPT、FORUM_PROMPT、MESSAGE_PROMPT
    - 导出：commonPromptMessages、userPromptMessages、teaPromptMessages、orderPromptMessages、shopPromptMessages、forumPromptMessages、messagePromptMessages
    - _Requirements: 1.1, 1.2, 1.3_
  - [x] 1.2 更新所有引用提示消息的代码
    - 搜索所有使用 xxxPromptMessages 的文件
    - 修改 import 路径为 `@/utils/promptMessages`
    - _Requirements: 1.4_

- [x] 2. Checkpoint - 验证提示消息迁移
  - 确保所有提示消息函数正常工作
  - 确保无编译错误

- [x] 3. 第二步：创建状态码映射文档
  - [x] 3.1 创建 `docs/code-message-mapping.md`
    - 定义状态码分段规则
    - 列出所有模块的状态码和对应消息
    - _Requirements: 2.1, 2.2, 2.3, 2.4_

- [x] 4. 第三步：创建 apiMessages.js
  - [x] 4.1 创建 `src/utils/apiMessages.js` 文件
    - 创建 CODE_MAP 对象
    - 实现 getMessageByCode 函数
    - 实现 showByCode 函数
    - 实现 isSuccess 函数
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_
  - [x] 4.2 修改 api/index.js 拦截器
    - 移除自动消息显示逻辑
    - 返回完整响应 { code, data }
    - 保留网络错误的自动提示
    - _Requirements: 4.1, 4.2, 4.3, 4.4_

- [x] 5. Checkpoint - 验证 API 消息系统
  - 确保拦截器正确返回响应格式
  - 确保 showByCode 正常工作

- [ ] 6. 清理旧文件
  - [ ] 6.1 删除旧消息文件
    - 删除 commonMessages.js、userMessages.js、teaMessages.js、orderMessages.js、shopMessages.js、forumMessages.js、messageMessages.js
    - ⚠️ 注意：这些文件目前仍存在，且仍有代码在引用其 SUCCESS/ERROR 消息
    - _Requirements: 5.1_
  - [ ] 6.2 更新所有引用旧文件的代码
    - 搜索所有使用 SUCCESS/ERROR 消息的文件
    - 修改为使用 showByCode 或直接调用 messageManager
    - _Requirements: 5.2_

- [ ] 7. Final Checkpoint
  - 确保所有测试通过
  - 确保无编译错误
  - 确保无旧文件引用

## 当前状态 (2024-12-30 验证)

✅ **Step 1 已完成**:
1. ✅ 创建 `promptMessages.js` - 整合所有 7 个模块的 PROMPT 消息
2. ✅ 更新所有 Vue 文件的导入路径 - 所有 xxxPromptMessages 都从 `@/utils/promptMessages` 导入

✅ **Step 2 已完成**:
3. ✅ 创建状态码映射文档 - 完整的状态码规则

✅ **Step 3 部分完成**:
4. ✅ 创建 `apiMessages.js` - 状态码消息映射系统
5. ✅ 修改拦截器 - 返回 `{code, data}` 格式

⚠️ **Step 3 待完成 (任务6)**:
- 旧消息文件仍存在（7个文件）：
  - `commonMessages.js` - 仍有 2 个 Vue 文件引用 SUCCESS/ERROR
  - `userMessages.js` - 仍存在
  - `teaMessages.js` - 仍存在
  - `orderMessages.js` - 仍有 7 个 Vue 文件引用 SUCCESS/ERROR
  - `shopMessages.js` - 仍存在
  - `forumMessages.js` - 仍有 6 个 Vue 文件引用 SUCCESS/ERROR
  - `messageMessages.js` - 仍有 1 个 Vue 文件引用 SUCCESS/ERROR
- `messages.js` 聚合文件仍在引用所有旧模块
- 需要将约 16+ 个 Vue 文件的 SUCCESS/ERROR 消息调用改为使用 `showByCode()`

## 使用新系统

```javascript
// API调用示例
import { showByCode, isSuccess } from '@/utils/apiMessages'

const response = await api.someAction()
if (isSuccess(response.code)) {
  showByCode(response.code) // 自动显示成功消息
} else {
  showByCode(response.code) // 自动显示错误消息
}
```

## Notes

- 第一步：原封不动提取 PROMPT 部分，只改导入路径
- 第二步：创建映射文档供参考
- 第三步：创建 apiMessages.js 并清理旧文件
