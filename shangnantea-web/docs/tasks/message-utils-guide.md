# 消息提示工具使用指南

## 概述

项目使用集中式消息工具管理所有用户提示，按模块分类存放在 `src/utils/` 目录下。

## 文件结构与作用

| 文件 | 作用 |
|------|------|
| `messageManager.js` | **核心管理器**：封装 ElMessage，提供防重复、消息队列、优先级排序功能 |
| `messageHelper.js` | **辅助工具**：提供标准消息常量和异步操作消息处理 |
| `messages.js` | **索引文件**：统一导出所有模块消息，提供开发辅助函数 |
| `commonMessages.js` | **通用消息**：上传、保存、删除等通用操作 |
| `userMessages.js` | **用户模块**：登录、注册、认证、个人资料 |
| `orderMessages.js` | **订单模块**：下单、支付、物流、购物车、退款 |
| `shopMessages.js` | **店铺模块**：店铺关注、茶叶管理、Banner、公告 |
| `forumMessages.js` | **论坛模块**：帖子、评论、版块、文章管理 |
| `teaMessages.js` | **茶叶模块**：茶叶列表、详情、收藏、规格 |
| `messageMessages.js` | **消息模块**：私信、通知、会话管理 |

## 消息分类

每个模块包含三类消息：

| 类型 | 说明 | 颜色 |
|------|------|------|
| `xxxSuccessMessages` | 操作成功反馈 | 绿色 |
| `xxxErrorMessages` | 操作失败/错误提示 | 红色 |
| `xxxPromptMessages` | 表单验证/确认提示 | 黄色/蓝色 |

## 使用方法

### 1. 导入

```javascript
import { commonSuccessMessages, commonErrorMessages } from '@/utils/commonMessages'
import { userSuccessMessages, userErrorMessages } from '@/utils/userMessages'
import { orderSuccessMessages, orderErrorMessages } from '@/utils/orderMessages'
```

### 2. 调用示例

```javascript
// 通用操作
commonSuccessMessages.showSaveSuccess()           // "保存成功"
commonSuccessMessages.showDeleteSuccess()         // "删除成功"
commonErrorMessages.showOperationFailed('原因')   // "原因" 或 "操作失败"

// 用户操作
userSuccessMessages.showLoginSuccess('张三')      // "张三，登录成功"
userErrorMessages.showLoginFailure()              // "登录失败，请检查用户名和密码"

// 订单操作
orderSuccessMessages.showAddedToCart('龙井茶', 2) // "已将龙井茶 2件加入购物车"
orderErrorMessages.showCartItemOutOfStock(5)      // "库存不足，当前可用库存：5"
```

## 完整示例

```vue
<script>
import { commonSuccessMessages, commonErrorMessages } from '@/utils/commonMessages'
import { orderSuccessMessages, orderPromptMessages } from '@/utils/orderMessages'

export default {
  setup() {
    const handleAddToCart = async (tea) => {
      if (!tea.specId) {
        orderPromptMessages.showSpecRequired()  // "请选择规格"
        return
      }
      try {
        await addToCart(tea)
        orderSuccessMessages.showAddedToCart(tea.name)
      } catch (error) {
        commonErrorMessages.showOperationFailed('加入购物车失败')
      }
    }
    return { handleAddToCart }
  }
}
</script>
```

## 各模块函数速查

### commonMessages（通用）

```javascript
// 成功
showOperationSuccess(msg?)   // 操作成功（可自定义）
showSaveSuccess()            // 保存成功
showDeleteSuccess()          // 删除成功
showUpdateSuccess()          // 更新成功
showUploadSuccess()          // 上传成功
showCopySuccess()            // 复制成功

// 错误
showOperationFailed(msg?)    // 操作失败
showLoadFailed(msg?)         // 加载失败
showUploadFailed(error?)     // 上传失败
showFileSizeExceeded(size)   // 文件大小不能超过{size}MB
showImageTypeInvalid()       // 只能上传图片文件
```

### userMessages（用户）

```javascript
// 成功
showLoginSuccess(username?)  // 登录成功
showRegisterSuccess()        // 注册成功，请登录
showLogoutSuccess()          // 已安全退出系统
showProfileUpdateSuccess()   // 个人资料更新成功
showPasswordChangeSuccess()  // 密码修改成功
showAvatarUpdateSuccess()    // 头像更新成功

// 错误
showLoginFailure(reason?)    // 登录失败
showRegisterFailure(reason?) // 注册失败
showSessionExpired()         // 登录已过期
showPermissionDenied()       // 没有权限

// 提示
showUsernameRequired()       // 请输入用户名
showPasswordRequired()       // 请输入密码
showFormIncomplete()         // 请完善表单信息
```

### orderMessages（订单）

```javascript
// 成功
showOrderCreated(orderNo?)   // 订单创建成功
showOrderCanceled()          // 订单已取消
showOrderConfirmed()         // 确认收货成功
showPaymentSuccess()         // 支付成功
showAddedToCart(name?, qty?) // 已加入购物车
showCartCleared()            // 购物车已清空
showRefundSubmitted()        // 退款申请已提交

// 错误
showOrderCreateFailed(reason?)    // 创建订单失败
showPaymentFailed(reason?)        // 支付失败
showCartItemOutOfStock(stock?)    // 库存不足
showInsufficientBalance(bal, amt) // 余额不足

// 提示
showAddressRequired()        // 请选择收货地址
showSpecRequired()           // 请选择规格
showCartEmpty()              // 购物车是空的
showSelectPaymentMethod()    // 请选择支付方式
```

### shopMessages（店铺）

```javascript
// 成功
showFollowSuccess()          // 已关注店铺
showUnfollowSuccess()        // 已取消关注
showTeaToggleSuccess(action) // 上架/下架成功
showBannerAddSuccess()       // Banner添加成功

// 错误
showShopInfoLoadFailed(reason?)   // 加载店铺信息失败
showTeaListLoadFailed(reason?)    // 加载茶叶列表失败
showLogoFormatError()             // 只能上传图片格式
```

### forumMessages（论坛）

```javascript
// 成功
showPostCreated()            // 帖子发布成功
showPostDeleted()            // 帖子删除成功
showPostLiked()              // 点赞成功
showPostFavorited()          // 收藏成功
showCommentCreated()         // 评论发表成功
showTopicCreated()           // 添加版块成功

// 错误
showPostCreateFailed(reason?)     // 帖子发布失败
showLoadPostsFailed(reason?)      // 获取帖子列表失败
showUnauthorized()                // 请先登录

// 提示
showTitleRequired()          // 标题不能为空
showContentRequired()        // 内容不能为空
showCommentRequired()        // 回复内容不能为空
```

### teaMessages（茶叶）

```javascript
// 成功
showAddedToFavorites()       // 已收藏
showRemovedFromFavorites()   // 已取消收藏
showAddedToCart()            // 成功加入购物车
showTeaCreated()             // 茶叶添加成功

// 错误
showListFailed(reason?)      // 加载茶叶数据失败
showDetailFailed(reason?)    // 加载茶叶详情失败
showCartFailed(reason?)      // 加入购物车失败

// 提示
showSoldOut()                // 该商品已售罄
showSelectSpec()             // 请先选择规格
showSpecRequired()           // 请至少添加一个规格
```

### messageMessages（消息/通知）

```javascript
// 成功
showMessageSent()            // 消息发送成功
showNotificationRead()       // 通知已标记为已读
showAllNotificationsRead()   // 所有通知已标记为已读
showSessionDeleted()         // 会话已删除
showMessageRecalled()        // 消息已撤回

// 错误
showSendFailed(reason?)      // 发送失败
showLoadFailed(reason?)      // 加载失败
showRecallFailed()           // 消息撤回失败

// 提示
showMessageEmpty()           // 消息内容不能为空
showRecipientRequired()      // 请选择接收人
```

## 核心功能说明

### messageManager.js 特性

1. **防重复消息**：3秒内相同消息不重复显示
2. **消息队列**：按优先级排序（错误 > 警告 > 成功 > 信息）
3. **开发日志**：开发环境下在控制台输出带颜色的消息日志

### 开发辅助（messages.js）

```javascript
import messages from '@/utils/messages'

// 查看所有消息常量统计
messages.devHelper.showAllConstants()

// 根据文本查找消息键
messages.devHelper.findMessageByText('登录')

// 清除所有消息状态
messages.devHelper.clearAllStates()
```

## 注意事项

1. **禁止直接使用** `message.success()` 等原始调用
2. **按模块选择**：优先使用对应模块的消息函数
3. **通用场景**：找不到对应函数时使用 `commonMessages`
4. **自定义消息**：部分函数支持传入自定义文本参数
5. **参数可选**：大部分函数的 `reason` 参数可选，不传则使用默认文本
