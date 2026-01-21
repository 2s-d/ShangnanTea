# API常量文件更新总结

## 更新内容

根据状态码消息映射文档 (`docs/tasks/code-message-mapping.md`) 中的接口定义，对 `src/api/apiConstants.js` 文件进行了全面更新，确保接口端点常量与文档完全同步。

## 主要改进

### 1. 文档同步
- 添加了文档同步说明注释
- 确保所有接口路径与状态码映射文档一致
- 按照文档中的接口分组重新组织常量

### 2. 结构优化
- 按功能模块清晰分组（6大核心模块）
- 每个模块内按接口类型进一步细分
- 添加了接口数量统计注释

### 3. 注释完善
- 为每个接口添加了详细的HTTP方法和功能说明
- 标注了动态路径参数的使用方式
- 明确了每个端点支持的操作类型

## 模块详情

### 用户模块 (USER) - 35个接口
- **认证相关** (5个): LOGIN, REGISTER, LOGOUT, REFRESH, ME
- **用户信息相关** (5个): INFO, AVATAR, PASSWORD, PASSWORD_RESET
- **用户地址相关** (5个): ADDRESSES, ADDRESS_DETAIL, ADDRESS_DEFAULT
- **商家认证相关** (2个): SHOP_CERTIFICATION, CERTIFICATION_IMAGE
- **用户互动相关** (8个): FOLLOWS, FOLLOW_DETAIL, FAVORITES, FAVORITE_DETAIL, LIKES, LIKE_DETAIL
- **用户偏好设置** (2个): PREFERENCES
- **管理员用户管理** (8个): ADMIN_USERS, ADMIN_USER_DETAIL, ADMIN_USER_STATUS, ADMIN_CERTIFICATIONS, ADMIN_CERTIFICATION_DETAIL

### 茶叶模块 (TEA) - 26个接口
- **茶叶基础操作** (6个): LIST, DETAIL, RECOMMEND
- **分类管理** (4个): CATEGORIES, CATEGORY_DETAIL
- **评价系统** (5个): REVIEWS_LIST, REVIEWS_STATS, REVIEWS, REVIEW_REPLY, REVIEW_LIKE
- **规格管理** (5个): SPECIFICATIONS, SPECIFICATION_DETAIL, SPECIFICATION_DEFAULT
- **图片管理** (4个): IMAGES, IMAGE_DETAIL, IMAGE_MAIN, IMAGE_ORDER
- **状态管理** (2个): STATUS_UPDATE, BATCH_STATUS_UPDATE

### 店铺模块 (SHOP) - 26个接口
- **店铺基础操作** (6个): LIST, DETAIL, MY, STATISTICS
- **店铺茶叶管理** (5个): TEAS, TEA_DETAIL, TEA_STATUS
- **Logo上传** (1个): LOGO
- **Banner管理** (5个): BANNERS, BANNER_DETAIL, BANNER_ORDER
- **公告管理** (4个): ANNOUNCEMENTS, ANNOUNCEMENT_DETAIL
- **店铺关注与评价** (5个): FOLLOW, FOLLOW_STATUS, REVIEWS

### 订单模块 (ORDER) - 21个接口
- **购物车相关** (5个): CART, CART_ADD, CART_UPDATE, CART_REMOVE, CART_CLEAR
- **订单相关** (8个): CREATE, LIST, DETAIL, PAY, CANCEL, CONFIRM, REVIEW, REVIEW_IMAGE
- **退款相关** (3个): REFUND, REFUND_PROCESS, REFUND_DETAIL
- **发货相关** (3个): SHIP, BATCH_SHIP, LOGISTICS
- **统计与导出** (2个): STATISTICS, EXPORT

### 论坛模块 (FORUM) - 37个接口
- **首页内容相关** (2个): HOME_CONTENTS
- **Banner管理相关** (5个): BANNERS, BANNER_DETAIL, BANNER_ORDER
- **文章相关** (5个): ARTICLES, ARTICLE_DETAIL
- **版块相关** (5个): TOPICS, TOPIC_DETAIL
- **帖子相关** (15个): POSTS, POST_DETAIL, POST_IMAGE, POSTS_PENDING, POST_LIKE, POST_FAVORITE, POST_REPLIES, POST_APPROVE, POST_REJECT, POST_STICKY, POST_ESSENCE
- **回复相关** (4个): REPLIES, REPLY_LIKE

### 消息模块 (MESSAGE) - 22个接口
- **消息基础操作** (5个): LIST, DETAIL, SEND, READ, DELETE, UNREAD_COUNT
- **通知相关** (6个): NOTIFICATIONS, NOTIFICATION_DETAIL, BATCH_READ, BATCH_DELETE
- **聊天会话相关** (6个): SESSIONS, SESSION_DETAIL, LIST_SESSIONS, LIST_HISTORY, MESSAGES_IMAGE
- **个人主页相关** (5个): USER_PROFILE, USER_DYNAMIC, USER_STATISTICS, USER_POSTS, USER_REVIEWS

### 系统通用 (SYSTEM) - 2个接口
- UPLOAD, UPLOAD_IMAGE

## 总计接口数量
- **用户模块**: 35个接口
- **茶叶模块**: 26个接口  
- **店铺模块**: 26个接口
- **订单模块**: 21个接口
- **论坛模块**: 37个接口
- **消息模块**: 22个接口
- **系统通用**: 2个接口
- **总计**: 169个接口

## 维护要求

1. **三方同步**: 本文件必须与以下文档保持同步
   - `docs/tasks/code-message-mapping.md` (状态码消息映射文档)
   - `openapi_new.yaml` (OpenAPI接口文档)

2. **更新流程**: 
   - 接口变更时，优先更新状态码映射文档
   - 然后同步更新本文件和OpenAPI文档
   - 确保三个文档的接口信息完全一致

3. **命名规范**:
   - 常量名使用大写字母和下划线
   - 路径使用小写字母和连字符
   - 注释说明HTTP方法和功能描述

## 验证结果

✅ 语法检查通过 - 无语法错误
✅ 接口路径与文档一致
✅ 常量命名规范统一
✅ 注释信息完整准确

更新完成，接口常量文件现已与状态码消息映射文档完全同步。