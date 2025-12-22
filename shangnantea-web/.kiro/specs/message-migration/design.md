# 消息工具迁移设计文档

## 架构设计

### 消息系统层次结构

```
┌─────────────────────────────────────────────────────────┐
│                    Vue 组件层                            │
│  (使用 xxxSuccessMessages / xxxErrorMessages 等)         │
└─────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────┐
│                  模块消息工具层                          │
│  userMessages / orderMessages / forumMessages / ...     │
│  - 定义消息常量 (XXX_MESSAGES)                          │
│  - 导出消息函数 (xxxSuccessMessages, xxxErrorMessages)   │
└─────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────┐
│                  消息管理器层                            │
│                 messageManager.js                        │
│  - successMessage.show()                                │
│  - errorMessage.show() / errorMessage.warning()         │
│  - promptMessage.show() / promptMessage.info()          │
│  - 防重复、队列管理、日志记录                            │
└─────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────┐
│                  UI 组件层                               │
│           @/components/common/message                    │
│              (Element Plus Message)                      │
└─────────────────────────────────────────────────────────┘
```

### 消息工具文件结构

```
src/utils/
├── messageManager.js      # 核心管理器（已完成）
├── commonMessages.js      # 通用消息（已完成）
├── userMessages.js        # 用户模块（已完成）
├── orderMessages.js       # 订单模块（需扩展）
├── messageMessages.js     # 消息模块（已完成）
├── forumMessages.js       # 论坛模块（需扩展）
├── shopMessages.js        # 店铺模块（需扩展）
└── teaMessages.js         # 茶叶模块（需扩展）
```

## 迁移模式

### 模式1: 简单消息替换

**迁移前:**
```javascript
import { message } from '@/components/common'

message.success('操作成功')
message.error('操作失败')
```

**迁移后:**
```javascript
import { commonSuccessMessages, commonErrorMessages } from '@/utils/commonMessages'

commonSuccessMessages.showOperationSuccess()
commonErrorMessages.showOperationFailed()
```

### 模式2: 带参数消息替换

**迁移前:**
```javascript
message.warning(`库存不足，当前可用库存：${item.stock}`)
```

**迁移后:**
```javascript
// 在 orderMessages.js 中添加
showStockInsufficient(stock) {
  promptMessage.show(`库存不足，当前可用库存：${stock}`)
}

// 在组件中使用
orderPromptMessages.showStockInsufficient(item.stock)
```

### 模式3: 动态消息替换

**迁移前:**
```javascript
message.success(`已连接到${shopInfo.name}客服`)
```

**迁移后:**
```javascript
// 在 messageMessages.js 中添加
showConnectedToService(shopName) {
  successMessage.show(`已连接到${shopName}客服`)
}

// 在组件中使用
messageSuccessMessages.showConnectedToService(shopInfo.name)
```

## 需要扩展的消息函数

### forumMessages.js 扩展

```javascript
// 成功消息
showArticleDetailFetched()
showFavoriteSuccess()
showUnfavoriteSuccess()
showLinkCopied()
showWeiboShareOpened()
showQQShareOpened()
showArticleRefreshed()
showPostDeleted()
showLikeSuccess()
showUnlikeSuccess()
showSectionCreated()
showSectionUpdated()
showSectionDeleted()
showSectionStatusToggled(name, status)
showPostApproved()
showPostRejected()
showBlockContentSaved()
showArticleCreated()
showArticleUpdated()
showArticleDeleted()
showArticleStatusToggled(title, status)

// 错误消息
showArticleDetailFetchFailed()
showOperationFailed()
showSectionListFetchFailed()
showPostListFetchFailed()
showSectionOperationFailed(action, error)
showPostOperationFailed(action, error)
showHomeDataFetchFailed()
showCarouselFetchFailed()
showRecommendTeaFetchFailed()
showArticleRefreshFailed()
showReplyFetchFailed()

// 提示消息
showShareDeveloping()
showReplyEmpty()
showCarouselDataReset()
showRecommendTeaDataReset()
showAuditPending()
showDeletePending()
```

### orderMessages.js 扩展

```javascript
// 成功消息
showQuantityUpdated()
showSpecUpdated()
showItemRemoved()
showSelectedItemsDeleted()
showOrderCancelled()
showReceiptConfirmed()
showLogisticsRefreshed()
showRefundSubmitted()
showItemAddedToCart()
showOrderDeleted()

// 错误消息
showCancelOrderFailed()
showConfirmReceiptFailed()
showOperationFailed()
showOrderDetailFetchFailed()
showOrderIdEmpty()
showDeleteOrderFailed()

// 提示消息
showStockInsufficient(stock)
showNoAvailableSpec()
showSelectSpec()
showSelectItemsFirst()
showSelectItemsToDelete()
showRefundReasonTooShort()
showOrderStatusNotSupportRefund()
showContactSellerDeveloping()
showBuyAgainDeveloping()
showDeleteOrderPending()
showLogisticsDeveloping()
showModifyAddressDeveloping()
showContactMerchantDeveloping()
showNoLogisticsInfo()
showOrderInfoAbnormal()
showOrderNotFound()
```

## 实施任务清单

### 阶段1: 扩展消息工具文件
- [ ] 扩展 `forumMessages.js`
- [ ] 扩展 `orderMessages.js`
- [ ] 扩展 `shopMessages.js`
- [ ] 扩展 `teaMessages.js`

### 阶段2: 迁移论坛模块
- [ ] `ArticleDetailPage.vue`
- [ ] `CultureHomePage.vue`
- [ ] `ForumDetailPage.vue`
- [ ] `ForumListPage.vue`
- [ ] `CultureManagerPage.vue`
- [ ] `ForumManagePage.vue`

### 阶段3: 迁移订单模块
- [ ] `CartPage.vue`
- [ ] `OrderDetailPage.vue`
- [ ] `OrderListPage.vue`
- [ ] `OrderManagePage.vue`

### 阶段4: 迁移消息模块
- [ ] `ChatPage.vue`
- [ ] `PublishedContentPage.vue`
- [ ] `FavoritesPage.vue`
- [ ] `UserHomePage.vue`
- [ ] `SystemNotificationsPage.vue`

### 阶段5: 迁移通用组件
- [ ] `Footer.vue`
- [ ] `ChatInputArea.vue`
- [ ] `TeaCard.vue`

### 阶段6: 验证与清理
- [ ] 运行 `getDiagnostics` 检查所有迁移文件
- [ ] 搜索确认无遗漏的 `message.` 调用
- [ ] 更新扫描报告
