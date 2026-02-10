# Vuex 迁移剩余文件清单

## 检测时间
2026-02-11

## 检测结果
仍有 **26个文件** 使用 Vuex，需要迁移到 Pinia

---

## 1. Tea 模块 (3个文件)

### 1.1 TeaManagePage.vue
- **路径**: `shangnantea-web/src/views/tea/manage/TeaManagePage.vue`
- **问题**: 第469行 `import { useStore } from 'vuex'`
- **需要**: 改用 `useTeaStore` from `@/stores/tea`

### 1.2 TeaListPage.vue
- **路径**: `shangnantea-web/src/views/tea/list/TeaListPage.vue`
- **问题**: 第176行 `import { useStore } from 'vuex'`
- **需要**: 改用 `useTeaStore` from `@/stores/tea`

### 1.3 TeaDetailPage.vue
- **路径**: `shangnantea-web/src/views/tea/detail/TeaDetailPage.vue`
- **问题**: 第342行 `import { useStore } from 'vuex'`
- **需要**: 改用 `useTeaStore` from `@/stores/tea`

---

## 2. Shop 模块 (3个文件)

### 2.1 ShopManagePage.vue
- **路径**: `shangnantea-web/src/views/shop/manage/ShopManagePage.vue`
- **问题**: 第627行 `import { useStore } from 'vuex'`
- **需要**: 改用 `useShopStore` from `@/stores/shop`

### 2.2 ShopDetailPage.vue
- **路径**: `shangnantea-web/src/views/shop/detail/ShopDetailPage.vue`
- **问题**: 第271行 `import { useStore } from 'vuex'`
- **需要**: 改用 `useShopStore` from `@/stores/shop`

### 2.3 ShopListPage.vue
- **路径**: `shangnantea-web/src/views/shop/list/ShopListPage.vue`
- **问题**: 第113行 `import { useStore } from 'vuex'`
- **需要**: 改用 `useShopStore` from `@/stores/shop`

---

## 3. Order 模块 (6个文件)

### 3.1 OrderDetailPage.vue
- **路径**: `shangnantea-web/src/views/order/detail/OrderDetailPage.vue`
- **问题**: 第209行 `import { useStore } from 'vuex'`
- **需要**: 改用 `useOrderStore` from `@/stores/order`

### 3.2 OrderReviewPage.vue
- **路径**: `shangnantea-web/src/views/order/review/OrderReviewPage.vue`
- **问题**: 第106行 `import { useStore } from 'vuex'`
- **需要**: 改用 `useOrderStore` from `@/stores/order`

### 3.3 PaymentPage.vue
- **路径**: `shangnantea-web/src/views/order/payment/PaymentPage.vue`
- **问题**: 第79行 `import { useStore } from 'vuex'`
- **需要**: 改用 `useOrderStore` from `@/stores/order`

### 3.4 OrderManagePage.vue
- **路径**: `shangnantea-web/src/views/order/manage/OrderManagePage.vue`
- **问题**: 第412行 `import { useStore } from 'vuex'`
- **需要**: 改用 `useOrderStore` from `@/stores/order`

### 3.5 CheckoutPage.vue
- **路径**: `shangnantea-web/src/views/order/payment/CheckoutPage.vue`
- **问题**: 第206行 `import { useStore } from 'vuex'`
- **需要**: 改用 `useOrderStore` from `@/stores/order`

### 3.6 CartPage.vue
- **路径**: `shangnantea-web/src/views/order/cart/CartPage.vue`
- **问题**: 第182行 `import { useStore } from 'vuex'`
- **需要**: 改用 `useOrderStore` from `@/stores/order`

### 3.7 OrderListPage.vue
- **路径**: `shangnantea-web/src/views/order/list/OrderListPage.vue`
- **问题**: 第190行 `import { useStore } from 'vuex'`
- **需要**: 改用 `useOrderStore` from `@/stores/order`

---

## 4. Forum 模块 (5个文件)

### 4.1 ForumListPage.vue
- **路径**: `shangnantea-web/src/views/forum/list/ForumListPage.vue`
- **问题**: 第253行 `import { useStore } from 'vuex'`
- **需要**: 改用 `useForumStore` from `@/stores/forum`

### 4.2 ForumManagePage.vue
- **路径**: `shangnantea-web/src/views/forum/manage/ForumManagePage.vue`
- **问题**: 第459行 `import { useStore } from 'vuex'`
- **需要**: 改用 `useForumStore` from `@/stores/forum`

### 4.3 CultureManagerPage.vue
- **路径**: `shangnantea-web/src/views/forum/manage/CultureManagerPage.vue`
- **问题**: 第446行 `import { useStore } from 'vuex'`
- **需要**: 改用 `useForumStore` from `@/stores/forum`

### 4.4 ForumDetailPage.vue
- **路径**: `shangnantea-web/src/views/forum/detail/ForumDetailPage.vue`
- **问题**: 第214行 `import { useStore } from 'vuex'`
- **需要**: 改用 `useForumStore` from `@/stores/forum`

### 4.5 CultureHomePage.vue
- **路径**: `shangnantea-web/src/views/forum/culturehome/CultureHomePage.vue`
- **问题**: 第134行 `import { useStore } from 'vuex'`
- **需要**: 改用 `useForumStore` from `@/stores/forum`

### 4.6 ArticleDetailPage.vue
- **路径**: `shangnantea-web/src/views/forum/culturehome/ArticleDetailPage.vue`
- **问题**: 第133行 `import { useStore } from 'vuex'`
- **需要**: 改用 `useForumStore` from `@/stores/forum`

---

## 5. Message 模块 (4个文件)

### 5.1 SystemNotificationsPage.vue
- **路径**: `shangnantea-web/src/views/message/notification/SystemNotificationsPage.vue`
- **问题**: 第111行 `import { useStore } from 'vuex'`
- **需要**: 改用 `useMessageStore` from `@/stores/message`

### 5.2 UserHomePage.vue
- **路径**: `shangnantea-web/src/views/message/homepage/UserHomePage.vue`
- **问题**: 第163行 `import { useStore } from 'vuex'`
- **需要**: 改用 `useMessageStore` from `@/stores/message`

### 5.3 PublishedContentPage.vue
- **路径**: `shangnantea-web/src/views/message/content/PublishedContentPage.vue`
- **问题**: 第158行 `import { useStore } from 'vuex'`
- **需要**: 改用 `useMessageStore` from `@/stores/message`

### 5.4 ChatPage.vue
- **路径**: `shangnantea-web/src/views/message/chat/ChatPage.vue`
- **问题**: 第280行 `import { useStore } from 'vuex'`
- **需要**: 改用 `useMessageStore` from `@/stores/message`

---

## 6. Components (1个文件)

### 6.1 TeaCard.vue
- **路径**: `shangnantea-web/src/components/tea/card/TeaCard.vue`
- **问题**: 第41行 `import { useStore } from 'vuex'`
- **需要**: 改用 `useTeaStore` from `@/stores/tea`

---

## 7. Store 配置 (1个文件) ⚠️ 关键

### 7.1 store/index.js
- **路径**: `shangnantea-web/src/store/index.js`
- **问题**: 第1行 `import { createStore } from 'vuex'`
- **状态**: 这是 Vuex 的核心配置文件
- **处理**: 所有组件迁移完成后，可以删除整个 `src/store` 目录

---

## 迁移统计

| 模块 | 文件数 | 状态 |
|------|--------|------|
| Tea | 3 + 1 (component) | ❌ 未迁移 |
| Shop | 3 | ❌ 未迁移 |
| Order | 7 | ❌ 未迁移 |
| Forum | 6 | ❌ 未迁移 |
| Message | 4 | ❌ 未迁移 |
| Store配置 | 1 | ❌ 待删除 |
| **总计** | **26** | **0% 完成** |

---

## 迁移优先级建议

### 高优先级 (核心功能)
1. **Order 模块** (7个文件) - 涉及支付和购物车
2. **Tea 模块** (4个文件) - 核心商品展示

### 中优先级
3. **Shop 模块** (3个文件) - 商家管理
4. **Forum 模块** (6个文件) - 社区功能

### 低优先级
5. **Message 模块** (4个文件) - 消息通知

### 最后清理
6. **删除 store/index.js** 和整个 `src/store` 目录

---

## 下一步行动

1. 按模块逐个迁移（建议从 Order 或 Tea 开始）
2. 每个文件迁移后运行测试
3. 所有文件迁移完成后删除 Vuex 相关代码
4. 从 package.json 中移除 vuex 依赖

---

## 检测命令（用于验证）

```bash
# 检测是否还有 Vuex 导入
grep -r "from 'vuex'" shangnantea-web/src --include="*.vue" --include="*.js"

# 检测是否还有 useStore 使用
grep -r "useStore" shangnantea-web/src --include="*.vue" --include="*.js"

# 检测是否还有 mapState 等辅助函数
grep -r "mapState\|mapGetters\|mapActions\|mapMutations" shangnantea-web/src --include="*.vue"
```
