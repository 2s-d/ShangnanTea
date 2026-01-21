/**
 * API端点常量 - 按六大核心模块组织
 * 
 * 本文件与状态码消息映射文档保持完全同步
 * 更新时请同时更新：docs/tasks/code-message-mapping.md
 */
export const API = {
  // === 用户模块API ===
  USER: {
    // 认证相关 (5个接口)
    LOGIN: '/user/login',                    // POST - 用户登录
    REGISTER: '/user/register',              // POST - 用户注册  
    LOGOUT: '/user/logout',                  // POST - 退出登录
    REFRESH: '/user/refresh',                // POST - 刷新令牌
    ME: '/user/me',                          // GET - 获取当前用户信息
    
    // 用户信息相关 (5个接口)
    INFO: '/user/',                          // GET /user/{userId} - 获取用户信息, PUT /user - 更新用户信息
    AVATAR: '/user/avatar',                  // POST - 上传头像
    PASSWORD: '/user/password',              // PUT - 修改密码
    PASSWORD_RESET: '/user/password/reset',  // POST - 密码重置
    
    // 用户地址相关 (5个接口)
    ADDRESSES: '/user/addresses',            // GET - 获取地址列表, POST - 添加地址
    ADDRESS_DETAIL: '/user/addresses/',      // PUT /user/addresses/{id} - 更新地址, DELETE /user/addresses/{id} - 删除地址
    ADDRESS_DEFAULT: '/user/addresses/',     // PUT /user/addresses/{id}/default - 设置默认地址
    
    // 商家认证相关 (2个接口)
    SHOP_CERTIFICATION: '/user/shop-certification',              // GET - 获取认证状态, POST - 提交商家认证
    CERTIFICATION_IMAGE: '/user/merchant/certification/image',   // POST - 上传商家认证图片
    
    // 用户互动相关 (8个接口)
    FOLLOWS: '/user/follows',                // GET - 获取关注列表, POST - 添加关注
    FOLLOW_DETAIL: '/user/follows/',         // DELETE /user/follows/{id} - 取消关注
    FAVORITES: '/user/favorites',            // GET - 获取收藏列表, POST - 添加收藏
    FAVORITE_DETAIL: '/user/favorites/',     // DELETE /user/favorites/{id} - 取消收藏
    LIKES: '/user/likes',                    // POST - 点赞
    LIKE_DETAIL: '/user/likes/',             // DELETE /user/likes/{id} - 取消点赞
    
    // 用户偏好设置 (2个接口)
    PREFERENCES: '/user/preferences',        // GET - 获取用户偏好设置, PUT - 更新用户偏好设置
    
    // 管理员用户管理相关 (8个接口)
    ADMIN_USERS: '/user/admin/users',                    // GET - 获取用户列表, POST - 创建管理员
    ADMIN_USER_DETAIL: '/user/admin/users/',             // PUT /user/admin/users/{userId} - 更新用户, DELETE /user/admin/users/{userId} - 删除用户
    ADMIN_USER_STATUS: '/user/admin/users/',             // PUT /user/admin/users/{userId}/status - 切换用户状态
    ADMIN_CERTIFICATIONS: '/user/admin/certifications',  // GET - 获取认证列表
    ADMIN_CERTIFICATION_DETAIL: '/user/admin/certifications/' // PUT /user/admin/certifications/{id} - 审核认证
  },

  // === 茶叶模块API ===
  TEA: {
    // 茶叶基础操作 (6个接口)
    LIST: '/tea/list',                       // GET - 获取茶叶列表, POST - 添加茶叶
    DETAIL: '/tea/',                         // GET /tea/{id} - 获取茶叶详情, PUT /tea/{id} - 更新茶叶, DELETE /tea/{id} - 删除茶叶
    RECOMMEND: '/tea/recommend',             // GET - 获取推荐茶叶
    
    // 分类管理 (4个接口)
    CATEGORIES: '/tea/categories',           // GET - 获取分类列表, POST - 创建分类
    CATEGORY_DETAIL: '/tea/categories/',     // PUT /tea/categories/{id} - 更新分类, DELETE /tea/categories/{id} - 删除分类
    
    // 评价系统 (5个接口)
    REVIEWS_LIST: '/tea/',                   // GET /tea/{teaId}/reviews - 获取茶叶评价列表
    REVIEWS_STATS: '/tea/',                  // GET /tea/{teaId}/reviews/stats - 获取评价统计数据
    REVIEWS: '/tea/reviews',                 // POST - 提交评价
    REVIEW_REPLY: '/tea/reviews/',           // POST /tea/reviews/{reviewId}/reply - 回复评价
    REVIEW_LIKE: '/tea/reviews/',            // POST /tea/reviews/{reviewId}/like - 点赞评价
    
    // 规格管理 (5个接口)
    SPECIFICATIONS: '/tea/',                 // GET /tea/{teaId}/specifications - 获取规格列表, POST /tea/{teaId}/specifications - 添加规格
    SPECIFICATION_DETAIL: '/tea/specifications/', // PUT /tea/specifications/{specId} - 更新规格, DELETE /tea/specifications/{specId} - 删除规格
    SPECIFICATION_DEFAULT: '/tea/specifications/', // PUT /tea/specifications/{specId}/default - 设置默认规格
    
    // 图片管理 (4个接口)
    IMAGES: '/tea/',                         // POST /tea/{teaId}/images - 上传图片
    IMAGE_DETAIL: '/tea/images/',            // DELETE /tea/images/{imageId} - 删除图片
    IMAGE_MAIN: '/tea/images/',              // PUT /tea/images/{imageId}/main - 设置主图
    IMAGE_ORDER: '/tea/images/order',        // PUT - 更新图片顺序
    
    // 状态管理 (2个接口)
    STATUS_UPDATE: '/tea/',                  // PUT /tea/{teaId}/status - 更新茶叶状态
    BATCH_STATUS_UPDATE: '/tea/batch-status' // PUT - 批量更新状态
  },

  // === 店铺模块API ===
  SHOP: {
    // 店铺基础操作 (6个接口)
    LIST: '/shop/list',                      // GET - 获取店铺列表, POST - 创建店铺
    DETAIL: '/shop/',                        // GET /shop/{id} - 获取店铺详情, PUT /shop/{id} - 更新店铺信息
    MY: '/shop/my',                          // GET - 获取我的店铺
    STATISTICS: '/shop/',                    // GET /shop/{shopId}/statistics - 获取店铺统计
    
    // 店铺茶叶管理 (5个接口)
    TEAS: '/shop/',                          // GET /shop/{shopId}/teas - 获取店铺茶叶, POST /shop/{shopId}/teas - 添加店铺茶叶
    TEA_DETAIL: '/shop/teas/',               // PUT /shop/teas/{teaId} - 更新店铺茶叶, DELETE /shop/teas/{teaId} - 删除店铺茶叶
    TEA_STATUS: '/shop/teas/',               // PUT /shop/teas/{teaId}/status - 茶叶上下架
    
    // Logo上传 (1个接口)
    LOGO: '/shop/',                          // POST /shop/{shopId}/logo - 上传Logo
    
    // Banner管理 (5个接口)
    BANNERS: '/shop/',                       // GET /shop/{shopId}/banners - 获取Banner列表, POST /shop/{shopId}/banners - 上传Banner
    BANNER_DETAIL: '/shop/banners/',         // PUT /shop/banners/{bannerId} - 更新Banner, DELETE /shop/banners/{bannerId} - 删除Banner
    BANNER_ORDER: '/shop/banners/order',     // PUT - 更新Banner排序
    
    // 公告管理 (4个接口)
    ANNOUNCEMENTS: '/shop/',                 // GET /shop/{shopId}/announcements - 获取公告列表, POST /shop/{shopId}/announcements - 创建公告
    ANNOUNCEMENT_DETAIL: '/shop/announcements/', // PUT /shop/announcements/{announcementId} - 更新公告, DELETE /shop/announcements/{announcementId} - 删除公告
    
    // 店铺关注与评价 (5个接口)
    FOLLOW: '/shop/',                        // POST /shop/{shopId}/follow - 关注店铺, DELETE /shop/{shopId}/follow - 取消关注
    FOLLOW_STATUS: '/shop/',                 // GET /shop/{shopId}/follow-status - 获取关注状态
    REVIEWS: '/shop/',                       // GET /shop/{shopId}/reviews - 获取店铺评价, POST /shop/{shopId}/reviews - 提交店铺评价
  },

  // === 订单模块API ===
  ORDER: {
    // 购物车相关 (5个接口)
    CART: '/order/cart',                     // GET - 获取购物车
    CART_ADD: '/order/cart/add',             // POST - 加入购物车
    CART_UPDATE: '/order/cart/update',       // PUT - 更新购物车商品
    CART_REMOVE: '/order/cart/remove',       // DELETE - 移除购物车商品
    CART_CLEAR: '/order/cart/clear',         // DELETE - 清空购物车
    
    // 订单相关 (8个接口)
    CREATE: '/order/create',                 // POST - 创建订单
    LIST: '/order/list',                     // GET - 获取订单列表
    DETAIL: '/order/',                       // GET /order/{id} - 获取订单详情
    PAY: '/order/pay',                       // POST - 支付订单
    CANCEL: '/order/cancel',                 // PUT - 取消订单
    CONFIRM: '/order/confirm',               // PUT - 确认收货
    REVIEW: '/order/review',                 // POST - 评价订单
    REVIEW_IMAGE: '/order/review/image',     // POST - 上传订单评价图片
    
    // 退款相关 (3个接口)
    REFUND: '/order/refund',                 // POST - 申请退款
    REFUND_PROCESS: '/order/',               // PUT /order/{id}/refund/process - 处理退款
    REFUND_DETAIL: '/order/',                // GET /order/{id}/refund - 获取退款详情
    
    // 发货相关 (3个接口)
    SHIP: '/order/',                         // PUT /order/{id}/ship - 发货
    BATCH_SHIP: '/order/batch-ship',         // PUT - 批量发货
    LOGISTICS: '/order/',                    // GET /order/{id}/logistics - 获取物流信息
    
    // 统计与导出 (2个接口)
    STATISTICS: '/order/statistics',         // GET - 获取订单统计
    EXPORT: '/order/export'                  // GET - 导出订单数据
  },

  // === 论坛模块API ===
  FORUM: {
    // 首页内容相关 (2个接口)
    HOME_CONTENTS: '/forum/home',            // GET - 获取首页数据, PUT - 更新首页数据
    
    // Banner管理相关 (5个接口)
    BANNERS: '/forum/banners',               // GET - 获取Banner列表, POST - 上传Banner
    BANNER_DETAIL: '/forum/banners/',        // PUT /forum/banners/{id} - 更新Banner, DELETE /forum/banners/{id} - 删除Banner
    BANNER_ORDER: '/forum/banners/order',    // PUT - 更新Banner排序
    
    // 文章相关 (5个接口)
    ARTICLES: '/forum/articles',             // GET - 获取文章列表, POST - 创建文章
    ARTICLE_DETAIL: '/forum/articles/',      // GET /forum/articles/{id} - 获取文章详情, PUT /forum/articles/{id} - 更新文章, DELETE /forum/articles/{id} - 删除文章
    
    // 版块相关 (5个接口)
    TOPICS: '/forum/topics',                 // GET - 获取版块列表, POST - 创建版块
    TOPIC_DETAIL: '/forum/topics/',          // GET /forum/topics/{id} - 获取版块详情, PUT /forum/topics/{id} - 更新版块, DELETE /forum/topics/{id} - 删除版块
    
    // 帖子相关 (15个接口)
    POSTS: '/forum/posts',                   // GET - 获取帖子列表, POST - 创建帖子
    POST_DETAIL: '/forum/posts/',            // GET /forum/posts/{id} - 获取帖子详情, PUT /forum/posts/{id} - 更新帖子, DELETE /forum/posts/{id} - 删除帖子
    POST_IMAGE: '/forum/posts/image',        // POST - 上传帖子图片
    POSTS_PENDING: '/forum/posts/pending',   // GET - 获取待审核帖子列表
    POST_LIKE: '/forum/posts/',              // POST /forum/posts/{id}/like - 点赞帖子, DELETE /forum/posts/{id}/like - 取消点赞
    POST_FAVORITE: '/forum/posts/',          // POST /forum/posts/{id}/favorite - 收藏帖子, DELETE /forum/posts/{id}/favorite - 取消收藏
    POST_REPLIES: '/forum/posts/',           // GET /forum/posts/{id}/replies - 获取回复列表, POST /forum/posts/{id}/replies - 创建回复
    POST_APPROVE: '/forum/posts/',           // PUT /forum/posts/{id}/approve - 审核通过
    POST_REJECT: '/forum/posts/',            // PUT /forum/posts/{id}/reject - 审核拒绝
    POST_STICKY: '/forum/posts/',            // PUT /forum/posts/{id}/sticky - 置顶/取消置顶
    POST_ESSENCE: '/forum/posts/',           // PUT /forum/posts/{id}/essence - 加精/取消加精
    
    // 回复相关 (4个接口)
    REPLIES: '/forum/replies/',              // DELETE /forum/replies/{id} - 删除回复
    REPLY_LIKE: '/forum/replies/',           // POST /forum/replies/{id}/like - 点赞回复, DELETE /forum/replies/{id}/like - 取消点赞回复
  },

  // === 消息模块API ===
  MESSAGE: {
    // 消息基础操作 (5个接口)
    LIST: '/message/list',                   // GET - 获取消息列表
    DETAIL: '/message/',                     // GET /message/{id} - 获取消息详情
    SEND: '/message/send',                   // POST - 发送消息
    READ: '/message/read',                   // PUT - 标记已读
    DELETE: '/message/delete',               // DELETE - 删除消息
    UNREAD_COUNT: '/message/unread-count',   // GET - 获取未读数量
    
    // 通知相关 (6个接口)
    NOTIFICATIONS: '/message/notifications', // GET - 获取通知列表
    NOTIFICATION_DETAIL: '/message/notifications/', // GET /message/notifications/{id} - 获取通知详情, DELETE /message/notifications/{id} - 删除通知
    BATCH_READ: '/message/notifications/batch-read',   // PUT - 批量标记已读
    BATCH_DELETE: '/message/notifications/batch',      // DELETE - 批量删除通知
    
    // 聊天会话相关 (6个接口)
    SESSIONS: '/message/sessions',           // POST - 创建会话
    SESSION_DETAIL: '/message/sessions/',    // DELETE /message/sessions/{sessionId} - 删除会话, PUT /message/sessions/{sessionId}/pin - 置顶会话
    LIST_SESSIONS: '/message/list/sessions', // GET - 获取会话列表
    LIST_HISTORY: '/message/list/history',   // GET - 获取聊天记录
    MESSAGES_IMAGE: '/message/messages/image', // POST - 发送图片消息
    
    // 个人主页相关 (5个接口)
    USER_PROFILE: '/message/user/',          // GET /message/user/{userId} - 获取用户主页
    USER_DYNAMIC: '/message/user/',          // GET /message/user/{userId}/dynamic - 获取用户动态
    USER_STATISTICS: '/message/user/',       // GET /message/user/{userId}/statistics - 获取用户统计
    USER_POSTS: '/message/user/posts',       // GET - 获取用户帖子
    USER_REVIEWS: '/message/user/reviews'    // GET - 获取用户评价
  },

  // === 系统通用API ===
  SYSTEM: {
    UPLOAD: '/upload',                       // 通用文件上传
    UPLOAD_IMAGE: '/upload/image'            // 通用图片上传
  }
} 