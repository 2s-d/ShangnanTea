# 消息提示系统快速使用说明

本文件是 `message-system-guide.md` 的简化版，便于开发时快速复制用法。

## 1. 何时用哪一种

- **Prompt Messages（提示消息）**：纯前端校验/提示（不依赖后端状态码）
- **API Messages（API消息）**：后端返回 `{ code, data }` 后，基于 `code` 自动显示提示

## 2. Prompt Messages（纯前端提示）

```javascript
import { userPromptMessages, commonPromptMessages } from '@/utils/promptMessages'

// 表单校验
if (!username) {
    userPromptMessages.showUsernameRequired()
    return
}

// 上传校验（示例：5MB）
if (file.size > 5 * 1024 * 1024) {
    commonPromptMessages.showImageSizeExceeded()
    return
}
```

## 3. API Messages（状态码自动提示）

```javascript
import { showByCode, isSuccess } from '@/utils/apiMessages'

const res = await someApiCall()
showByCode(res.code) // 成功/失败都会自动提示（静默码不会显示）

if (!isSuccess(res.code)) {
    throw new Error('请求失败')
}
```

## 4. 状态码映射维护

- **权威数据源**：`docs/tasks/code-message-mapping.md`
- 文档更新后必须同步更新：`src/utils/apiMessages.js`（`CODE_MAP`、`SILENT_CODES`）

