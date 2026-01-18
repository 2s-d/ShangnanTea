/**
 * API端点常量 - 按六大核心模块组织
 */
export const API = {
  // === 用户模块API ===
  USER: {
    // 认证相关
    LOGIN: '/user/login',
    REGISTER: '/user/register',
    LOGOUT: '/user/logout',
    REFRESH: '/user/refresh',
    // 用户信息相关
    INFO: '/user/',
    AVATAR: '/user/avatar',
    PASSWORD: '/user/password',
    PASSWORD_RESET: '/user/password/reset',
    // 用户地址相关
    ADDRESSES: '/user/addresses',
    // 用户互动相关
    FAVORITES: '/user/favorites',
    FOLLOWS: '/user/follows',
    LIKES: '/user/likes',
    // 商家认证相关
    SHOP_CERTIFICATION: '/user/shop-certification',
    CERTIFICATION_IMAGE: '/user/merchant/certification/image', // 上传商家认证图片
    // 用户偏好设置（主题/展示等）
    PREFERENCES: '/user/preferences',
    // 管理员用户管理相关
    ADMIN_USERS: '/user/admin/users', // 用户列表、创建管理员、/user/admin/users/{userId} - 更新/删除/状态
    ADMIN_CERTIFICATIONS: '/user/admin/certifications' // 商家认证申请列表
  },

  // === 茶叶模块API ===
  TEA: {
    LIST: '/tea/list',
    DETAIL: '/tea/', // /tea/{id} - 详情/更新/删除
    CATEGORIES: '/tea/categories', // 分类列表/创建, /tea/categories/{id} - 更新/删除
    SEARCH: '/tea/search',
    RECOMMEND: '/tea/recommend',
    // 评价相关
    REVIEWS: '/tea/reviews', // 提交评价, /tea/reviews/{reviewId}/reply - 回复, /tea/reviews/{reviewId}/like - 点赞
    REVIEWS_LIST: '/tea/{teaId}/reviews', // 获取茶叶评价列表
    REVIEWS_STATS: '/tea/{teaId}/reviews/stats', // 获取评价统计数据
    // 规格管理
    SPECIFICATIONS: '/tea/{teaId}/specifications', // 获取/添加规格
    SPECIFICATION_DETAIL: '/tea/specifications/{specId}', // 更新/删除规格, /tea/specifications/{specId}/default - 设置默认
    // 图片管理
    IMAGES: '/tea/{teaId}/images', // 上传图片
    IMAGE_DETAIL: '/tea/images/{imageId}', // 删除图片, /tea/images/{imageId}/main - 设置主图
    IMAGE_ORDER: '/tea/images/order', // 更新图片顺序
    // 状态管理
    STATUS_UPDATE: '/tea/{teaId}/status', // 更新茶叶状态
    BATCH_STATUS_UPDATE: '/tea/batch-status', // 批量更新状态
  },

  // === 店铺模块API ===
  SHOP: {
    LIST: '/shop/list',
    DETAIL: '/shop/', // /shop/{id} - 详情/更新, /shop/{shopId}/teas, /shop/{shopId}/banners, /shop/{shopId}/announcements, /shop/{shopId}/follow, /shop/{shopId}/follow-status, /shop/{shopId}/reviews, /shop/{shopId}/statistics, /shop/{shopId}/logo
    MY: '/shop/my',
    TEAS: '/shop/teas', // /shop/teas/{teaId} - 更新/删除, /shop/teas/{teaId}/status
    BANNERS: '/shop/banners', // /shop/banners/{bannerId} - 更新/删除
    BANNER_ORDER: '/shop/banners/order', // Banner排序
    ANNOUNCEMENTS: '/shop/announcements' // /shop/announcements/{announcementId} - 更新/删除
  },

  // === 订单模块API ===
  ORDER: {
    // 购物车相关
    CART: '/order/cart',
    CART_ADD: '/order/cart/add',
    CART_UPDATE: '/order/cart/update',
    CART_REMOVE: '/order/cart/remove',
    CART_CLEAR: '/order/cart/clear',
    // 订单相关
    CREATE: '/order/create',
    LIST: '/order/list',
    DETAIL: '/order/', // /order/{id} - 详情, /order/{id}/ship, /order/{id}/logistics, /order/{id}/refund, /order/{id}/refund/request, /order/{id}/refund/process
    PAY: '/order/pay',
    CANCEL: '/order/cancel',
    CONFIRM: '/order/confirm',
    REVIEW: '/order/review',
    REVIEW_IMAGE: '/order/review/image', // 上传订单评价图片
    // 退款相关
    REFUND: '/order/refund', // 兼容旧路径
    // 发货相关
    BATCH_SHIP: '/order/batch-ship',
    // 统计与导出
    STATISTICS: '/order/statistics',
    EXPORT: '/order/export'
  },

  // === 论坛模块API ===
  FORUM: {
    // 版块相关
    TOPICS: '/forum/topics', // 列表/创建
    TOPIC_DETAIL: '/forum/topics/', // /forum/topics/{id} - 详情/更新/删除
    // 帖子相关
    POSTS: '/forum/posts', // 列表/创建
    POST_DETAIL: '/forum/posts/', // /forum/posts/{id} - 详情/更新/删除/like/favorite/approve/reject/sticky/essence, /forum/posts/{id}/replies
    POSTS_PENDING: '/forum/posts/pending', // 获取待审核帖子列表
    // 回复相关
    REPLIES: '/forum/replies', // /forum/replies/{id} - 删除, /forum/replies/{id}/like - 点赞
    // 文章相关
    ARTICLES: '/forum/articles', // 列表/创建
    ARTICLE_DETAIL: '/forum/articles/', // /forum/articles/{id} - 详情/更新/删除
    // 首页内容相关
    HOME_CONTENTS: '/forum/home',
    // Banner管理相关
    BANNERS: '/forum/banners', // 列表/上传, /forum/banners/{id} - 更新/删除
    BANNER_ORDER: '/forum/banners/order'
  },

  // === 消息模块API ===
  MESSAGE: {
    // 聊天相关
    SESSIONS: '/message/sessions', // /message/sessions/{sessionId} - 删除, /message/sessions/{sessionId}/pin - 置顶
    MESSAGES: '/message/messages', // /message/messages/{id} - 详情
    SEND: '/message/send',
    // 通知相关
    NOTIFICATIONS: '/message/notifications', // /message/notifications/{id} - 详情/删除
    BATCH_READ: '/message/notifications/batch-read',
    BATCH_DELETE: '/message/notifications/batch',
    READ: '/message/read',
    DELETE: '/message/delete',
    UNREAD_COUNT: '/message/unread-count',
    // 聊天会话相关
    LIST_SESSIONS: '/message/list/sessions',
    LIST_HISTORY: '/message/list/history',
    MESSAGES_IMAGE: '/message/messages/image',
    // 个人主页相关
    USER_PROFILE: '/message/user/', // /message/user/{userId}, /message/user/{userId}/dynamic, /message/user/{userId}/statistics
    USER_POSTS: '/message/user/posts',
    USER_REVIEWS: '/message/user/reviews'
  },

  // === 系统通用API ===
  SYSTEM: {
    UPLOAD: '/upload',
    UPLOAD_IMAGE: '/upload/image'
  },


} 