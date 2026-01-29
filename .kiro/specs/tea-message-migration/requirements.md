# 茶叶模块消息系统迁移需求文档

## 介绍

本文档定义了茶叶模块从旧消息系统迁移到新消息系统的需求。茶叶模块当前使用旧的 `teaMessages` 消息系统，需要迁移到基于状态码的新消息系统 `showByCode()`。

## 术语表

- **Tea_Module**: 茶叶模块，负责茶叶商品的展示、管理、评价等功能
- **Old_Message_System**: 旧消息系统，使用 `teaMessages.success/error.showXxx()` 方式
- **New_Message_System**: 新消息系统，使用 `showByCode(response.code)` 方式
- **Status_Code**: 状态码，后端返回的业务状态码（3xxx 为茶叶模块）
- **API_Response**: API响应对象，格式为 `{ code: number, data: any }`

## 需求

### 需求 1: 茶叶列表页面消息迁移

**用户故事**: 作为开发者，我想要将茶叶列表页面的消息系统迁移到新系统，以便统一消息管理。

#### 验收标准

1. WHEN 获取茶叶列表失败 THEN 系统 SHALL 使用 `showByCode(3100)` 显示错误消息
2. THE 系统 SHALL 移除所有 `teaMessages.error.showListFailed()` 调用
3. THE 系统 SHALL 确保错误消息通过状态码自动显示

### 需求 2: 茶叶管理页面上下架消息迁移

**用户故事**: 作为管理员，我想要在上下架茶叶时看到统一的消息提示，以便了解操作结果。

#### 验收标准

1. WHEN 茶叶上架成功 THEN 系统 SHALL 使用 `showByCode(3018)` 显示成功消息
2. WHEN 茶叶下架成功 THEN 系统 SHALL 使用 `showByCode(3019)` 显示成功消息
3. WHEN 茶叶上架失败 THEN 系统 SHALL 使用 `showByCode(3126)` 显示错误消息
4. WHEN 茶叶下架失败 THEN 系统 SHALL 使用 `showByCode(3127)` 显示错误消息
5. WHEN 批量上架成功 THEN 系统 SHALL 使用 `showByCode(3020)` 显示成功消息
6. WHEN 批量下架成功 THEN 系统 SHALL 使用 `showByCode(3021)` 显示成功消息
7. WHEN 批量上架失败 THEN 系统 SHALL 使用 `showByCode(3128)` 显示错误消息
8. WHEN 批量下架失败 THEN 系统 SHALL 使用 `showByCode(3129)` 显示错误消息
9. THE 系统 SHALL 移除所有 `teaMessages.success.showTeaOnShelf()` 等旧消息调用

### 需求 3: 茶叶删除消息迁移

**用户故事**: 作为管理员，我想要在删除茶叶时看到统一的消息提示，以便确认操作结果。

#### 验收标准

1. WHEN 删除茶叶成功 THEN 系统 SHALL 使用 `showByCode(3003)` 显示成功消息
2. WHEN 删除茶叶失败 THEN 系统 SHALL 使用 `showByCode(3104)` 显示错误消息
3. THE 系统 SHALL 移除所有 `teaMessages.success.showTeaDeleted()` 调用
4. THE 系统 SHALL 移除所有 `teaMessages.error.showTeaDeleteFailed()` 调用

### 需求 4: 茶叶添加和更新消息迁移

**用户故事**: 作为管理员，我想要在添加或更新茶叶时看到统一的消息提示，以便了解操作结果。

#### 验收标准

1. WHEN 添加茶叶成功 THEN 系统 SHALL 使用 `showByCode(3001)` 显示成功消息
2. WHEN 添加茶叶失败 THEN 系统 SHALL 使用 `showByCode(3101)` 显示错误消息
3. WHEN 更新茶叶成功 THEN 系统 SHALL 使用 `showByCode(3002)` 显示成功消息
4. WHEN 更新茶叶失败 THEN 系统 SHALL 使用 `showByCode(3103)` 显示错误消息
5. THE 系统 SHALL 移除所有 `teaMessages.success.showTeaCreated()` 调用
6. THE 系统 SHALL 移除所有 `teaMessages.success.showTeaUpdated()` 调用
7. THE 系统 SHALL 移除所有 `teaMessages.error.showTeaSubmitFailed()` 调用

### 需求 5: 分类管理消息迁移

**用户故事**: 作为管理员，我想要在管理茶叶分类时看到统一的消息提示，以便了解操作结果。

#### 验收标准

1. WHEN 创建分类成功 THEN 系统 SHALL 使用 `showByCode(3004)` 显示成功消息
2. WHEN 创建分类失败 THEN 系统 SHALL 使用 `showByCode(3107)` 显示错误消息
3. WHEN 更新分类成功 THEN 系统 SHALL 使用 `showByCode(3005)` 显示成功消息
4. WHEN 更新分类失败 THEN 系统 SHALL 使用 `showByCode(3108)` 显示错误消息
5. WHEN 删除分类成功 THEN 系统 SHALL 使用 `showByCode(3006)` 显示成功消息
6. WHEN 删除分类失败 THEN 系统 SHALL 使用 `showByCode(3109)` 显示错误消息
7. THE 系统 SHALL 移除所有 `teaMessages.success.showCategoryXxx()` 调用
8. THE 系统 SHALL 移除所有 `teaMessages.error.showCategoryXxx()` 调用

### 需求 6: 茶叶详情页面消息迁移

**用户故事**: 作为用户，我想要在茶叶详情页面操作时看到统一的消息提示，以便了解操作结果。

#### 验收标准

1. WHEN 回复评价成功 THEN 系统 SHALL 使用 `showByCode(3008)` 显示成功消息
2. WHEN 回复评价失败 THEN 系统 SHALL 使用 `showByCode(3113)` 显示错误消息
3. WHEN 点赞评价失败 THEN 系统 SHALL 使用 `showByCode(3114)` 显示错误消息
4. WHEN 收藏茶叶成功 THEN 系统 SHALL 使用 `showByCode(2014)` 显示成功消息
5. WHEN 取消收藏成功 THEN 系统 SHALL 使用 `showByCode(2015)` 静默处理（不显示消息）
6. WHEN 收藏操作失败 THEN 系统 SHALL 使用 `showByCode(2126)` 显示错误消息
7. WHEN 加入购物车成功 THEN 系统 SHALL 使用 `showByCode(5000)` 显示成功消息
8. WHEN 加入购物车失败 THEN 系统 SHALL 使用相应的错误状态码显示错误消息
9. WHEN 获取详情失败 THEN 系统 SHALL 使用 `showByCode(3102)` 显示错误消息
10. THE 系统 SHALL 移除所有 `teaMessages.success/error.showXxx()` 调用

### 需求 7: 茶叶卡片组件消息迁移

**用户故事**: 作为用户，我想要在茶叶卡片上操作时看到统一的消息提示，以便了解操作结果。

#### 验收标准

1. WHEN 从卡片加入购物车成功 THEN 系统 SHALL 使用 `showByCode(5000)` 显示成功消息
2. WHEN 从卡片加入购物车失败 THEN 系统 SHALL 使用相应的错误状态码显示错误消息
3. THE 系统 SHALL 移除所有 `teaMessages.success.showCardAddedToCart()` 调用
4. THE 系统 SHALL 移除所有 `teaMessages.error.showCardAddFailed()` 调用

### 需求 8: 移除旧消息系统导入

**用户故事**: 作为开发者，我想要清理所有旧消息系统的导入，以便保持代码整洁。

#### 验收标准

1. THE 系统 SHALL 从所有茶叶模块文件中移除 `teaMessages` 的导入语句
2. THE 系统 SHALL 在所有需要的文件中添加 `showByCode` 和 `isSuccess` 的导入
3. THE 系统 SHALL 确保没有遗留的 `teaMessages` 引用

### 需求 9: Vuex Store 响应处理

**用户故事**: 作为开发者，我想要确保 Vuex store 正确返回 API 响应，以便组件能够使用状态码。

#### 验收标准

1. THE 系统 SHALL 确保所有 Vuex actions 返回完整的 API 响应对象 `{ code, data }`
2. THE 系统 SHALL 在组件中使用 `response.code` 调用 `showByCode()`
3. THE 系统 SHALL 使用 `isSuccess(response.code)` 判断操作是否成功

### 需求 10: 静默状态码处理

**用户故事**: 作为用户，我想要某些查询操作不显示成功消息，以便避免消息干扰。

#### 验收标准

1. WHEN 获取茶叶列表成功 THEN 系统 SHALL 使用静默码 `3000` 不显示消息
2. WHEN 获取茶叶详情成功 THEN 系统 SHALL 使用静默码 `200` 不显示消息
3. WHEN 取消收藏成功 THEN 系统 SHALL 使用静默码 `2015` 不显示消息
4. THE 系统 SHALL 确保静默码在 `apiMessages.js` 的 `SILENT_CODES` 列表中
