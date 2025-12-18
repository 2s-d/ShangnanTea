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
    PASSWORD: '/user/password',
    // 用户地址相关
    ADDRESSES: '/user/addresses',
    // 用户互动相关
    FAVORITES: '/user/favorites',
    FOLLOWS: '/user/follows',
    LIKES: '/user/likes',
    // 商家认证相关
    SHOP_CERTIFICATION: '/user/shop-certification',
    // 用户偏好设置（主题/展示等）
    PREFERENCES: '/user/preferences'
  },

  // === 茶叶模块API ===
  TEA: {
    LIST: '/tea/list',
    DETAIL: '/tea/',
    CATEGORIES: '/tea/categories',
    SEARCH: '/tea/search',
    RECOMMEND: '/tea/recommend',
    REVIEWS: '/tea/reviews',
    SPECIFICATIONS: '/tea/specifications',
    IMAGES: '/tea/images'
  },

  // === 店铺模块API ===
  SHOP: {
    LIST: '/shop/list',
    DETAIL: '/shop/',
    MY: '/shop/my',
    TEAS: '/shop/teas',
    UPDATE: '/shop/update',
    STATISTICS: '/shop/statistics',
    CERTIFICATIONS: '/shop/certifications'
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
    DETAIL: '/order/',
    PAY: '/order/pay',
    CANCEL: '/order/cancel',
    CONFIRM: '/order/confirm',
    REVIEW: '/order/review',
    REFUND: '/order/refund'
  },

  // === 论坛模块API ===
  FORUM: {
    TOPICS: '/forum/topics',
    TOPIC_DETAIL: '/forum/topics/',
    POSTS: '/forum/posts',
    POST_DETAIL: '/forum/posts/',
    REPLIES: '/forum/replies',
    ARTICLES: '/forum/articles',
    HOME_CONTENTS: '/forum/home-contents'
  },

  // === 消息模块API ===
  MESSAGE: {
    // 聊天相关
    SESSIONS: '/message/sessions',
    SESSION_DETAIL: '/message/sessions/',
    MESSAGES: '/message/messages',
    SEND: '/message/send',
    // 通知相关
    NOTIFICATIONS: '/message/notifications',
    READ: '/message/read',
    UNREAD_COUNT: '/message/unread/count',
    /**
     * 兼容字段：历史代码可能使用 LIST/DETAIL/DELETE
     * - LIST/DETAIL 默认指代 messages 列表与详情
     * - DELETE 为兼容占位，具体后端实现以实际接口为准
     */
    LIST: '/message/messages',
    DETAIL: '/message/messages/',
    DELETE: '/message/delete'
  },

  // === 系统通用API ===
  SYSTEM: {
    UPLOAD: '/upload',
    UPLOAD_IMAGE: '/upload/image'
  },

  // === 兼容旧版API ===
  CART: {
    LIST: '/cart',
    ADD: '/cart/add',
    UPDATE: '/cart/update',
    REMOVE: '/cart/remove',
    CLEAR: '/cart/clear'
  }
} 