/**
 * API端点常量 - 与状态码消息映射文档完全同步
 */
export const API = {
  // === 用户模块API ===
  USER: {
    LOGIN: '/user/login',
    REGISTER: '/user/register',
    LOGOUT: '/user/logout',
    ME: '/user/me',
    REFRESH: '/user/refresh',
    INFO: '/user/{userId}',
    UPDATE: '/user',
    AVATAR: '/user/avatar',
    PASSWORD: '/user/password',
    PASSWORD_RESET: '/user/password/reset',
    SEND_VERIFICATION_CODE: '/user/verification-code/send',
    ADDRESSES: '/user/addresses',
    ADDRESS_DETAIL: '/user/addresses/{id}',
    ADDRESS_DEFAULT: '/user/addresses/{id}/default',
    SHOP_CERTIFICATION: '/user/shop-certification',
    FOLLOWS: '/user/follows',
    FOLLOW_DETAIL: '/user/follows/{id}',
    FAVORITES: '/user/favorites',
    FAVORITE_DETAIL: '/user/favorites/{id}',
    LIKES: '/user/likes',
    LIKE_DETAIL: '/user/likes/{id}',
    PREFERENCES: '/user/preferences',
    ADMIN_USERS: '/user/admin/users',
    ADMIN_USER_DETAIL: '/user/admin/users/{userId}',
    ADMIN_USER_STATUS: '/user/admin/users/{userId}/status',
    ADMIN_CERTIFICATIONS: '/user/admin/certifications',
    ADMIN_CERTIFICATION_DETAIL: '/user/admin/certifications/{id}',
    CERTIFICATION_IMAGE: '/user/merchant/certification/image'
  },

  // === 茶叶模块API ===
  TEA: {
    LIST: '/tea/list',
    DETAIL: '/tea/{id}',
    RECOMMEND: '/tea/recommend',
    CATEGORIES: '/tea/categories',
    CATEGORY_DETAIL: '/tea/categories/{id}',
    REVIEWS_LIST: '/tea/{teaId}/reviews',
    REVIEWS_STATS: '/tea/{teaId}/reviews/stats',
    REVIEWS: '/tea/reviews',
    REVIEW_REPLY: '/tea/reviews/{reviewId}/reply',
    REVIEW_LIKE: '/tea/reviews/{reviewId}/like',
    SPECIFICATIONS: '/tea/{teaId}/specifications',
    SPECIFICATION_DETAIL: '/tea/specifications/{specId}',
    SPECIFICATION_DEFAULT: '/tea/specifications/{specId}/default',
    IMAGES: '/tea/{teaId}/images',
    IMAGE_DETAIL: '/tea/images/{imageId}',
    IMAGE_MAIN: '/tea/images/{imageId}/main',
    IMAGE_ORDER: '/tea/images/order',
    STATUS: '/tea/{teaId}/status',
    BATCH_STATUS: '/tea/batch-status'
  },

  // === 店铺模块API ===
  SHOP: {
    LIST: '/shop/list',
    DETAIL: '/shop/{id}',
    MY: '/shop/my',
    STATISTICS: '/shop/{shopId}/statistics',
    TEAS: '/shop/{shopId}/teas',
    TEA_DETAIL: '/shop/teas/{teaId}',
    TEA_STATUS: '/shop/teas/{teaId}/status',
    LOGO: '/shop/{shopId}/logo',
    BANNERS: '/shop/{shopId}/banners',
    BANNER_DETAIL: '/shop/banners/{bannerId}',
    BANNER_ORDER: '/shop/banners/order',
    ANNOUNCEMENTS: '/shop/{shopId}/announcements',
    ANNOUNCEMENT_DETAIL: '/shop/announcements/{announcementId}',
    FOLLOW: '/shop/{shopId}/follow',
    FOLLOW_STATUS: '/shop/{shopId}/follow-status',
    REVIEWS: '/shop/{shopId}/reviews'
  },

  // === 订单模块API ===
  ORDER: {
    CART: '/order/cart',
    CART_ADD: '/order/cart/add',
    CART_UPDATE: '/order/cart/update',
    CART_REMOVE: '/order/cart/remove',
    CART_CLEAR: '/order/cart/clear',
    CREATE: '/order/create',
    LIST: '/order/list',
    DETAIL: '/order/{id}',
    PAY: '/order/pay',
    CANCEL: '/order/cancel',
    CONFIRM: '/order/confirm',
    REVIEW: '/order/review',
    REFUND: '/order/refund',我
    REFUND_PROCESS: '/order/{id}/refund/process',
    REFUND_DETAIL: '/order/{id}/refund',
    SHIP: '/order/{id}/ship',
    BATCH_SHIP: '/order/batch-ship',
    LOGISTICS: '/order/{id}/logistics',
    STATISTICS: '/order/statistics',
    EXPORT: '/order/export',
    REVIEW_IMAGE: '/order/review/image'
  },

  // === 论坛模块API ===
  FORUM: {
    HOME: '/forum/home',
    BANNERS: '/forum/banners',
    BANNER_DETAIL: '/forum/banners/{id}',
    BANNER_ORDER: '/forum/banners/order',
    ARTICLES: '/forum/articles',
    ARTICLE_DETAIL: '/forum/articles/{id}',
    TOPICS: '/forum/topics',
    TOPIC_DETAIL: '/forum/topics/{id}',
    POSTS: '/forum/posts',
    POST_DETAIL: '/forum/posts/{id}',
    POST_IMAGE: '/forum/posts/image',
    POSTS_PENDING: '/forum/posts/pending',
    POST_LIKE: '/forum/posts/{id}/like',
    POST_FAVORITE: '/forum/posts/{id}/favorite',
    POST_REPLIES: '/forum/posts/{id}/replies',
    POST_APPROVE: '/forum/posts/{id}/approve',
    POST_REJECT: '/forum/posts/{id}/reject',
    POST_STICKY: '/forum/posts/{id}/sticky',
    POST_ESSENCE: '/forum/posts/{id}/essence',
    REPLY_DETAIL: '/forum/replies/{id}',
    REPLY_LIKE: '/forum/replies/{id}/like'
  },

  // === 消息模块API ===
  MESSAGE: {
    LIST: '/message/list',
    DETAIL: '/message/{id}',
    SEND: '/message/send',
    READ: '/message/read',
    DELETE: '/message/delete',
    UNREAD_COUNT: '/message/unread-count',
    NOTIFICATIONS: '/message/notifications',
    NOTIFICATION_DETAIL: '/message/notifications/{id}',
    BATCH_READ: '/message/notifications/batch-read',
    BATCH_DELETE: '/message/notifications/batch',
    SESSIONS: '/message/sessions',
    SESSION_DETAIL: '/message/sessions/{sessionId}',
    SESSION_PIN: '/message/sessions/{sessionId}/pin',
    LIST_SESSIONS: '/message/list/sessions',
    LIST_HISTORY: '/message/list/history',
    MESSAGES_IMAGE: '/message/messages/image',
    USER_PROFILE: '/message/user/{userId}',
    USER_DYNAMIC: '/message/user/{userId}/dynamic',
    USER_STATISTICS: '/message/user/{userId}/statistics',
    USER_POSTS: '/message/user/posts',
    USER_REVIEWS: '/message/user/reviews'
  }
}