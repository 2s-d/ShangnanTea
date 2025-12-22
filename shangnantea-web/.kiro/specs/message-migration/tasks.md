# 消息工具迁移任务清单

## 任务状态说明
- [ ] 待完成
- [x] 已完成
- [~] 进行中

---

## 阶段1: 扩展消息工具文件

### Task 1.1: 扩展 forumMessages.js
- [ ] 添加文章相关成功消息函数
- [ ] 添加版块管理成功消息函数
- [ ] 添加帖子管理成功消息函数
- [ ] 添加错误消息函数
- [ ] 添加提示消息函数

### Task 1.2: 扩展 orderMessages.js
- [ ] 添加购物车相关消息函数
- [ ] 添加订单操作成功消息函数
- [ ] 添加订单错误消息函数
- [ ] 添加订单提示消息函数

### Task 1.3: 扩展 shopMessages.js
- [ ] 检查现有函数覆盖情况
- [ ] 添加缺失的消息函数

### Task 1.4: 扩展 teaMessages.js
- [ ] 检查现有函数覆盖情况
- [ ] 添加缺失的消息函数

---

## 阶段2: 迁移论坛模块 (65个消息调用)

### Task 2.1: 迁移 ArticleDetailPage.vue (8个调用)
- [ ] 移除 `message` 导入
- [ ] 添加 `forumMessages` 导入
- [ ] 替换 8 个消息调用
- [ ] 验证无语法错误

### Task 2.2: 迁移 CultureHomePage.vue (7个调用)
- [ ] 移除 `message` 导入
- [ ] 添加 `forumMessages` 导入
- [ ] 替换 7 个消息调用
- [ ] 验证无语法错误

### Task 2.3: 迁移 ForumDetailPage.vue (5个调用)
- [ ] 移除 `message` 导入
- [ ] 添加 `forumMessages` 导入
- [ ] 替换 5 个消息调用
- [ ] 验证无语法错误

### Task 2.4: 迁移 ForumListPage.vue (12个调用)
- [ ] 移除 `message` 导入
- [ ] 添加 `forumMessages` 导入
- [ ] 替换 12 个消息调用
- [ ] 验证无语法错误

### Task 2.5: 迁移 CultureManagerPage.vue (10个调用)
- [ ] 移除 `message` 导入
- [ ] 添加 `forumMessages` 导入
- [ ] 替换 10 个消息调用
- [ ] 验证无语法错误

### Task 2.6: 迁移 ForumManagePage.vue (23个调用)
- [ ] 移除 `message` 导入
- [ ] 添加 `forumMessages` 导入
- [ ] 替换 23 个消息调用
- [ ] 验证无语法错误

---

## 阶段3: 迁移订单模块 (74个消息调用)

### Task 3.1: 迁移 CartPage.vue (9个调用)
- [ ] 移除 `message` 导入
- [ ] 添加 `orderMessages` 导入
- [ ] 替换 9 个消息调用
- [ ] 验证无语法错误

### Task 3.2: 迁移 OrderDetailPage.vue (22个调用)
- [ ] 移除 `message` 导入
- [ ] 添加 `orderMessages` 导入
- [ ] 替换 22 个消息调用
- [ ] 验证无语法错误

### Task 3.3: 迁移 OrderListPage.vue (11个调用)
- [ ] 移除 `message` 导入
- [ ] 添加 `orderMessages` 导入
- [ ] 替换 11 个消息调用
- [ ] 验证无语法错误

### Task 3.4: 迁移 OrderManagePage.vue
- [ ] 移除 `message` 导入
- [ ] 添加 `orderMessages` 导入
- [ ] 替换所有消息调用
- [ ] 验证无语法错误

---

## 阶段4: 迁移消息模块 (28个消息调用)

### Task 4.1: 迁移 ChatPage.vue (13个调用)
- [ ] 移除 `message` 导入
- [ ] 添加 `messageMessages` 导入
- [ ] 替换 13 个消息调用
- [ ] 验证无语法错误

### Task 4.2: 迁移 PublishedContentPage.vue (4个调用)
- [ ] 移除 `message` 导入
- [ ] 添加 `messageMessages` 导入
- [ ] 替换 4 个消息调用
- [ ] 验证无语法错误

### Task 4.3: 迁移 FavoritesPage.vue (1个调用)
- [ ] 移除 `message` 导入
- [ ] 添加 `messageMessages` 或 `commonMessages` 导入
- [ ] 替换 1 个消息调用
- [ ] 验证无语法错误

### Task 4.4: 迁移 UserHomePage.vue (4个调用)
- [ ] 移除 `message` 导入
- [ ] 添加 `messageMessages` 导入
- [ ] 替换 4 个消息调用
- [ ] 验证无语法错误

### Task 4.5: 迁移 SystemNotificationsPage.vue (6个调用)
- [ ] 移除 `message` 导入
- [ ] 添加 `messageMessages` 导入
- [ ] 替换 6 个消息调用
- [ ] 验证无语法错误

---

## 阶段5: 迁移通用组件 (8个消息调用)

### Task 5.1: 迁移 Footer.vue (4个调用)
- [ ] 移除 `message` 导入
- [ ] 添加 `commonMessages` 导入
- [ ] 替换 4 个消息调用
- [ ] 验证无语法错误

### Task 5.2: 迁移 ChatInputArea.vue (2个调用)
- [ ] 移除 `message` 导入
- [ ] 添加 `commonMessages` 导入
- [ ] 替换 2 个消息调用
- [ ] 验证无语法错误

### Task 5.3: 迁移 TeaCard.vue (1个调用)
- [ ] 移除 `message` 导入
- [ ] 添加 `teaMessages` 或 `commonMessages` 导入
- [ ] 替换 1 个消息调用
- [ ] 验证无语法错误

---

## 阶段6: 验证与清理

### Task 6.1: 全面验证
- [ ] 对所有迁移文件运行 `getDiagnostics`
- [ ] 搜索 `message.(success|error|warning|info)` 确认无遗漏
- [ ] 搜索 `import { message }` 确认只剩基础设施文件

### Task 6.2: 更新文档
- [ ] 更新扫描报告
- [ ] 标记本规范为已完成

---

## 进度统计

| 阶段 | 任务数 | 已完成 | 进度 |
|------|--------|--------|------|
| 阶段1 | 4 | 0 | 0% |
| 阶段2 | 6 | 0 | 0% |
| 阶段3 | 4 | 0 | 0% |
| 阶段4 | 5 | 0 | 0% |
| 阶段5 | 3 | 0 | 0% |
| 阶段6 | 2 | 0 | 0% |
| **总计** | **24** | **0** | **0%** |
