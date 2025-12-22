# 消息工具迁移规范

## 概述

将项目中直接使用 `message` 组件的代码迁移到集中式消息工具函数（`xxxMessages.js`），实现消息提示的统一管理和维护。

## 用户故事

### US-1: 作为开发者，我希望所有消息提示使用统一的工具函数
**验收标准:**
- [ ] 所有 Vue 文件不再直接导入 `import { message } from '@/components/common'`
- [ ] 所有消息调用使用对应模块的 `xxxMessages.js` 工具函数
- [ ] 消息文本集中定义在常量对象中，便于维护和国际化

### US-2: 作为开发者，我希望消息按模块分类管理
**验收标准:**
- [ ] 用户模块使用 `userMessages.js`
- [ ] 订单模块使用 `orderMessages.js`
- [ ] 消息模块使用 `messageMessages.js`
- [ ] 论坛模块使用 `forumMessages.js`
- [ ] 店铺模块使用 `shopMessages.js`
- [ ] 茶叶模块使用 `teaMessages.js`
- [ ] 通用场景使用 `commonMessages.js`

### US-3: 作为开发者，我希望消息函数按语义分类
**验收标准:**
- [ ] 成功消息使用 `xxxSuccessMessages.showXxx()`
- [ ] 错误消息使用 `xxxErrorMessages.showXxx()`
- [ ] 提示消息使用 `xxxPromptMessages.showXxx()`

## 迁移范围

### 待迁移文件（基于扫描报告）

#### 通用组件 (common)
| 文件 | 消息数量 | 状态 |
|------|---------|------|
| `src/components/common/layout/Footer.vue` | 4 | 待迁移 |
| `src/components/message/chat/ChatInputArea.vue` | 2 | 待迁移 |
| `src/components/tea/card/TeaCard.vue` | 1 | 待迁移 |

#### 论坛模块 (forum)
| 文件 | 消息数量 | 状态 |
|------|---------|------|
| `src/views/forum/culturehome/ArticleDetailPage.vue` | 8 | 待迁移 |
| `src/views/forum/culturehome/CultureHomePage.vue` | 7 | 待迁移 |
| `src/views/forum/detail/ForumDetailPage.vue` | 5 | 待迁移 |
| `src/views/forum/list/ForumListPage.vue` | 12 | 待迁移 |
| `src/views/forum/manage/CultureManagerPage.vue` | 10 | 待迁移 |
| `src/views/forum/manage/ForumManagePage.vue` | 23 | 待迁移 |

#### 消息模块 (message)
| 文件 | 消息数量 | 状态 |
|------|---------|------|
| `src/views/message/chat/ChatPage.vue` | 13 | 待迁移 |
| `src/views/message/content/PublishedContentPage.vue` | 4 | 待迁移 |
| `src/views/message/favorites/FavoritesPage.vue` | 1 | 待迁移 |
| `src/views/message/homepage/UserHomePage.vue` | 4 | 待迁移 |
| `src/views/message/notification/SystemNotificationsPage.vue` | 6 | 待迁移 |

#### 订单模块 (order)
| 文件 | 消息数量 | 状态 |
|------|---------|------|
| `src/views/order/cart/CartPage.vue` | 9 | 待迁移 |
| `src/views/order/detail/OrderDetailPage.vue` | 22 | 待迁移 |
| `src/views/order/list/OrderListPage.vue` | 11 | 待迁移 |
| `src/views/order/manage/OrderManagePage.vue` | 待确认 | 待迁移 |

### 保留直接导入的文件
以下文件作为消息系统基础设施，保留直接导入：
- `src/utils/messageManager.js` - 消息管理器核心
- `src/utils/messageHelper.js` - 消息辅助工具
- `src/main.js` - 应用入口
- `src/router/index.js` - 路由配置

## 迁移规则

### 1. 消息类型映射
```
message.success() → xxxSuccessMessages.showXxx()
message.error()   → xxxErrorMessages.showXxx()
message.warning() → xxxPromptMessages.showXxx()
message.info()    → xxxPromptMessages.showXxx()
```

### 2. 模块归属判断
- 文件路径包含 `/user/` → userMessages
- 文件路径包含 `/order/` → orderMessages
- 文件路径包含 `/message/` → messageMessages
- 文件路径包含 `/forum/` → forumMessages
- 文件路径包含 `/shop/` → shopMessages
- 文件路径包含 `/tea/` → teaMessages
- 通用组件 → commonMessages

### 3. 函数命名规范
- 函数名应描述消息场景：`showLoginSuccess`, `showOrderCancelled`
- 带参数的消息使用参数传递：`showFileSizeExceeded(size)`
- 动态消息使用通用函数：`showOperationSuccess(msg)`

## 迁移步骤

1. **扩展消息工具** - 为缺失的消息场景添加函数
2. **替换导入语句** - 移除 `message` 导入，添加对应模块的消息工具导入
3. **替换调用代码** - 将 `message.xxx()` 替换为 `xxxMessages.showXxx()`
4. **验证无错误** - 使用 `getDiagnostics` 检查语法错误
5. **功能测试** - 确保消息正常显示

## 统计摘要

| 模块 | 文件数 | 消息调用数 |
|------|--------|-----------|
| common | 3 | 8 |
| forum | 6 | 65 |
| message | 5 | 28 |
| order | 4 | 74 |
| shop | 待统计 | 28 |
| tea | 待统计 | 27 |
| **总计** | **31** | **230** |
