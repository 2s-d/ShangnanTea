/**
 * 精确统计前端API接口数量
 * 逐个分析每个API函数，提取实际的HTTP方法和路径
 */

const fs = require('fs')
const path = require('path')

// API常量定义（从apiConstants.js复制）
const API = {
  USER: {
    LOGIN: '/user/login',
    REGISTER: '/user/register',
    LOGOUT: '/user/logout',
    REFRESH: '/user/refresh',
    INFO: '/user/',
    AVATAR: '/user/avatar',
    PASSWORD: '/user/password',
    PASSWORD_RESET: '/user/password/reset',
    ADDRESSES: '/user/addresses',
    FAVORITES: '/user/favorites',
    FOLLOWS: '/user/follows',
    LIKES: '/user/likes',
    SHOP_CERTIFICATION: '/user/shop-certification',
    PREFERENCES: '/user/preferences',
    ADMIN_USERS: '/user/admin/users',
    ADMIN_USER_UPDATE: '/user/admin/users/',
    ADMIN_USER_DELETE: '/user/admin/users/',
    ADMIN_USER_STATUS: '/user/admin/users/',
    ADMIN_CERTIFICATIONS: '/user/admin/certifications'
  },
  TEA: {
    LIST: '/tea/list',
    DETAIL: '/tea/',
    CATEGORIES: '/tea/categories',
    SEARCH: '/tea/search',
    RECOMMEND: '/tea/recommend',
    REVIEWS: '/tea/reviews',
    REVIEWS_LIST: '/tea/{teaId}/reviews',
    REVIEWS_STATS: '/tea/{teaId}/reviews/stats',
    REVIEW_SUBMIT: '/tea/reviews',
    REVIEW_REPLY: '/tea/reviews/{reviewId}/reply',
    REVIEW_LIKE: '/tea/reviews/{reviewId}/like',
    SPECIFICATIONS: '/tea/{teaId}/specifications',
    SPECIFICATION_ADD: '/tea/{teaId}/specifications',
    SPECIFICATION_UPDATE: '/tea/specifications/{specId}',
    SPECIFICATION_DELETE: '/tea/specifications/{specId}',
    SPECIFICATION_SET_DEFAULT: '/tea/specifications/{specId}/default',
    IMAGE_UPLOAD: '/tea/{teaId}/images',
    IMAGE_DELETE: '/tea/images/{imageId}',
    IMAGE_ORDER: '/tea/images/order',
    IMAGE_SET_MAIN: '/tea/images/{imageId}/main',
    STATUS_UPDATE: '/tea/{teaId}/status',
    BATCH_STATUS_UPDATE: '/tea/batch-status',
  },
  SHOP: {
    LIST: '/shop/list',
    SEARCH: '/shop/search',
    DETAIL: '/shop/',
    MY: '/shop/my',
    TEAS: '/shop/teas',
    UPDATE: '/shop/update',
    STATISTICS: '/shop/statistics',
    CERTIFICATIONS: '/shop/certifications',
    STATS: '/shop/',
    LOGO: '/shop/',
    BANNERS: '/shop/banners',
    BANNER_ORDER: '/shop/banners/order',
    ANNOUNCEMENTS: '/shop/announcements',
    FOLLOW_BASE: '/shop/',
    REVIEWS: '/shop/',
    REVIEW_SUBMIT: '/shop/'
  },
  ORDER: {
    CART: '/order/cart',
    CART_ADD: '/order/cart/add',
    CART_UPDATE: '/order/cart/update',
    CART_REMOVE: '/order/cart/remove',
    CART_CLEAR: '/order/cart/clear',
    CREATE: '/order/create',
    LIST: '/order/list',
    DETAIL: '/order/',
    PAY: '/order/pay',
    CANCEL: '/order/cancel',
    CONFIRM: '/order/confirm',
    PAYMENT_STATUS: '/order/payment-status',
    REVIEW: '/order/review',
    REFUND: '/order/refund',
    REFUND_BASE: '/order/',
    SHIP_BASE: '/order/',
    BATCH_SHIP: '/order/batch/ship',
    LOGISTICS_BASE: '/order/',
    STATISTICS: '/order/statistics',
    EXPORT: '/order/export'
  },
  FORUM: {
    TOPICS: '/forum/topics',
    TOPIC_DETAIL: '/forum/topics/',
    POSTS: '/forum/posts',
    POST_DETAIL: '/forum/posts/',
    REPLIES: '/forum/replies',
    ARTICLES: '/forum/articles',
    ARTICLE_DETAIL: '/forum/articles/',
    HOME_CONTENTS: '/forum/home',
    BANNERS: '/forum/banners',
    BANNER_ORDER: '/forum/banners/order',
    POST_LIKE: '/forum/posts/',
    POST_FAVORITE: '/forum/posts/',
    POSTS_PENDING: '/forum/posts/pending',
    POST_APPROVE: '/forum/posts/',
    POST_REJECT: '/forum/posts/',
    POST_STICKY: '/forum/posts/',
    POST_ESSENCE: '/forum/posts/'
  },
  MESSAGE: {
    SESSIONS: '/message/sessions',
    SESSION_DETAIL: '/message/sessions/',
    MESSAGES: '/message/messages',
    SEND: '/message/send',
    NOTIFICATIONS: '/message/notifications',
    NOTIFICATION_DETAIL: '/message/notifications/',
    NOTIFICATION_READ: '/message/notifications/{id}/read',
    NOTIFICATION_DELETE: '/message/notifications/',
    BATCH_READ: '/message/notifications/batch-read',
    BATCH_DELETE: '/message/notifications/batch',
    READ: '/message/read',
    UNREAD_COUNT: '/message/notifications/unread-count',
    USER_PROFILE: '/message/user/',
    USER_DYNAMIC: '/message/user/{userId}/dynamic',
    USER_STATISTICS: '/message/user/{userId}/statistics',
    USER_POSTS: '/message/user/posts',
    USER_REVIEWS: '/message/user/reviews',
    LIST: '/message/messages',
    DETAIL: '/message/messages/',
    DELETE: '/message/delete'
  }
}

// 手动定义每个API函数对应的接口（最准确的方式）
const frontendAPIs = {
  user: [
    { func: 'login', method: 'POST', path: '/user/login' },
    { func: 'register', method: 'POST', path: '/user/register' },
    { func: 'logout', method: 'POST', path: '/user/logout' },
    { func: 'getCurrentUser', method: 'GET', path: '/user/me' },
    { func: 'refreshToken', method: 'POST', path: '/user/refresh' },
    { func: 'getUserInfo', method: 'GET', path: '/user/{userId}' },
    { func: 'updateUserInfo', method: 'PUT', path: '/user/' },
    { func: 'uploadAvatar', method: 'POST', path: '/user/avatar' },
    { func: 'changePassword', method: 'PUT', path: '/user/password' },
    { func: 'resetPassword', method: 'POST', path: '/user/password/reset' },
    { func: 'getAddressList', method: 'GET', path: '/user/addresses' },
    { func: 'addAddress', method: 'POST', path: '/user/addresses' },
    { func: 'updateAddress', method: 'PUT', path: '/user/addresses/{addressId}' },
    { func: 'deleteAddress', method: 'DELETE', path: '/user/addresses/{addressId}' },
    { func: 'setDefaultAddress', method: 'PUT', path: '/user/addresses/{addressId}/default' },
    { func: 'submitShopCertification', method: 'POST', path: '/user/shop-certification' },
    { func: 'getShopCertificationStatus', method: 'GET', path: '/user/shop-certification' },
    { func: 'getFollowList', method: 'GET', path: '/user/follows' },
    { func: 'addFollow', method: 'POST', path: '/user/follows' },
    { func: 'removeFollow', method: 'DELETE', path: '/user/follows/{followId}' },
    { func: 'getFavoriteList', method: 'GET', path: '/user/favorites' },
    { func: 'addFavorite', method: 'POST', path: '/user/favorites' },
    { func: 'removeFavorite', method: 'DELETE', path: '/user/favorites/{favoriteId}' },
    { func: 'addLike', method: 'POST', path: '/user/likes' },
    { func: 'removeLike', method: 'DELETE', path: '/user/likes/{likeId}' },
    { func: 'getAdminUserList', method: 'GET', path: '/user/admin/users' },
    { func: 'createAdmin', method: 'POST', path: '/user/admin/users' },
    { func: 'updateUser', method: 'PUT', path: '/user/admin/users/{userId}' },
    { func: 'deleteUser', method: 'DELETE', path: '/user/admin/users/{userId}' },
    { func: 'updateUserRole', method: 'PUT', path: '/user/admin/users/{userId}/role' },
    { func: 'toggleUserStatus', method: 'PUT', path: '/user/admin/users/{userId}/status' },
    { func: 'getCertificationList', method: 'GET', path: '/user/admin/certifications' },
    { func: 'processCertification', method: 'PUT', path: '/user/admin/certifications/{id}' },
    { func: 'getUserPreferences', method: 'GET', path: '/user/preferences' },
    { func: 'updateUserPreferences', method: 'PUT', path: '/user/preferences' },
  ],
  tea: [
    { func: 'getTeas', method: 'GET', path: '/tea/list' },
    { func: 'getTeaDetail', method: 'GET', path: '/tea/{id}' },
    { func: 'getTeaCategories', method: 'GET', path: '/tea/categories' },
    { func: 'createCategory', method: 'POST', path: '/tea/categories' },
    { func: 'updateCategory', method: 'PUT', path: '/tea/categories/{id}' },
    { func: 'deleteCategory', method: 'DELETE', path: '/tea/categories/{id}' },
    { func: 'addTea', method: 'POST', path: '/tea/list' },
    { func: 'updateTea', method: 'PUT', path: '/tea/{id}' },
    { func: 'deleteTea', method: 'DELETE', path: '/tea/{id}' },
    { func: 'getTeaReviews', method: 'GET', path: '/tea/{teaId}/reviews' },
    { func: 'getReviewStats', method: 'GET', path: '/tea/{teaId}/reviews/stats' },
    { func: 'submitReview', method: 'POST', path: '/tea/reviews' },
    { func: 'replyReview', method: 'POST', path: '/tea/reviews/{reviewId}/reply' },
    { func: 'likeReview', method: 'POST', path: '/tea/reviews/{reviewId}/like' },
    { func: 'getTeaSpecifications', method: 'GET', path: '/tea/{teaId}/specifications' },
    { func: 'addSpecification', method: 'POST', path: '/tea/{teaId}/specifications' },
    { func: 'updateSpecification', method: 'PUT', path: '/tea/specifications/{specId}' },
    { func: 'deleteSpecification', method: 'DELETE', path: '/tea/specifications/{specId}' },
    { func: 'setDefaultSpecification', method: 'PUT', path: '/tea/specifications/{specId}/default' },
    { func: 'uploadTeaImages', method: 'POST', path: '/tea/{teaId}/images' },
    { func: 'deleteTeaImage', method: 'DELETE', path: '/tea/images/{imageId}' },
    { func: 'updateImageOrder', method: 'PUT', path: '/tea/images/order' },
    { func: 'setMainImage', method: 'PUT', path: '/tea/images/{imageId}/main' },
    { func: 'toggleTeaStatus', method: 'PUT', path: '/tea/{teaId}/status' },
    { func: 'batchToggleTeaStatus', method: 'PUT', path: '/tea/batch-status' },
    { func: 'getRecommendTeas', method: 'GET', path: '/tea/recommend' },
  ],
  shop: [
    { func: 'getShops', method: 'GET', path: '/shop/list' },
    { func: 'getShopDetail', method: 'GET', path: '/shop/{id}' },
    { func: 'getMyShop', method: 'GET', path: '/shop/my' },
    { func: 'createShop', method: 'POST', path: '/shop' },
    { func: 'getShopTeas', method: 'GET', path: '/shop/{shopId}/teas' },
    { func: 'updateShop', method: 'PUT', path: '/shop/{id}' },
    { func: 'addShopTea', method: 'POST', path: '/shop/{shopId}/teas' },
    { func: 'updateShopTea', method: 'PUT', path: '/shop/teas/{teaId}' },
    { func: 'deleteShopTea', method: 'DELETE', path: '/shop/teas/{teaId}' },
    { func: 'toggleShopTeaStatus', method: 'PUT', path: '/shop/teas/{teaId}/status' },
    { func: 'getShopStatistics', method: 'GET', path: '/shop/{shopId}/statistics' },
    { func: 'uploadShopLogo', method: 'POST', path: '/shop/{shopId}/logo' },
    { func: 'getShopBanners', method: 'GET', path: '/shop/{shopId}/banners' },
    { func: 'uploadBanner', method: 'POST', path: '/shop/{shopId}/banners' },
    { func: 'updateBanner', method: 'PUT', path: '/shop/banners/{bannerId}' },
    { func: 'deleteBanner', method: 'DELETE', path: '/shop/banners/{bannerId}' },
    { func: 'updateBannerOrder', method: 'PUT', path: '/shop/banners/order' },
    { func: 'getShopAnnouncements', method: 'GET', path: '/shop/{shopId}/announcements' },
    { func: 'createAnnouncement', method: 'POST', path: '/shop/{shopId}/announcements' },
    { func: 'updateAnnouncement', method: 'PUT', path: '/shop/announcements/{announcementId}' },
    { func: 'deleteAnnouncement', method: 'DELETE', path: '/shop/announcements/{announcementId}' },
    { func: 'followShop', method: 'POST', path: '/shop/{shopId}/follow' },
    { func: 'unfollowShop', method: 'DELETE', path: '/shop/{shopId}/follow' },
    { func: 'checkFollowStatus', method: 'GET', path: '/shop/{shopId}/follow-status' },
    { func: 'getShopReviews', method: 'GET', path: '/shop/{shopId}/reviews' },
    { func: 'submitShopReview', method: 'POST', path: '/shop/{shopId}/reviews' },
  ],
  order: [
    { func: 'getCartItems', method: 'GET', path: '/order/cart' },
    { func: 'addToCart', method: 'POST', path: '/order/cart/add' },
    { func: 'updateCartItem', method: 'PUT', path: '/order/cart/update' },
    { func: 'removeFromCart', method: 'DELETE', path: '/order/cart/remove' },
    { func: 'clearCart', method: 'DELETE', path: '/order/cart/clear' },
    { func: 'createOrder', method: 'POST', path: '/order/create' },
    { func: 'getOrders', method: 'GET', path: '/order/list' },
    { func: 'getOrderDetail', method: 'GET', path: '/order/{id}' },
    { func: 'payOrder', method: 'POST', path: '/order/pay' },
    { func: 'cancelOrder', method: 'POST', path: '/order/cancel' },
    { func: 'confirmOrder', method: 'POST', path: '/order/confirm' },
    { func: 'reviewOrder', method: 'POST', path: '/order/review' },
    { func: 'refundOrder', method: 'POST', path: '/order/refund' },
    { func: 'requestRefund', method: 'POST', path: '/order/{orderId}/refund/request' },
    { func: 'processRefund', method: 'POST', path: '/order/{orderId}/refund/process' },
    { func: 'getRefundDetail', method: 'GET', path: '/order/{orderId}/refund' },
    { func: 'getPaymentStatus', method: 'GET', path: '/order/payment-status' },
    { func: 'shipOrder', method: 'POST', path: '/order/{id}/ship' },
    { func: 'batchShipOrders', method: 'POST', path: '/order/batch/ship' },
    { func: 'getOrderLogistics', method: 'GET', path: '/order/{id}/logistics' },
    { func: 'getOrderStatistics', method: 'GET', path: '/order/statistics' },
    { func: 'exportOrders', method: 'GET', path: '/order/export' },
  ],
  forum: [
    { func: 'getHomeData', method: 'GET', path: '/forum/home' },
    { func: 'updateHomeData', method: 'PUT', path: '/forum/home' },
    { func: 'getBanners', method: 'GET', path: '/forum/banners' },
    { func: 'uploadBanner', method: 'POST', path: '/forum/banners' },
    { func: 'updateBanner', method: 'PUT', path: '/forum/banners/{id}' },
    { func: 'deleteBanner', method: 'DELETE', path: '/forum/banners/{id}' },
    { func: 'updateBannerOrder', method: 'PUT', path: '/forum/banners/order' },
    { func: 'getArticles', method: 'GET', path: '/forum/articles' },
    { func: 'getArticleDetail', method: 'GET', path: '/forum/articles/{id}' },
    { func: 'createArticle', method: 'POST', path: '/forum/articles' },
    { func: 'updateArticle', method: 'PUT', path: '/forum/articles/{id}' },
    { func: 'deleteArticle', method: 'DELETE', path: '/forum/articles/{id}' },
    { func: 'getForumTopics', method: 'GET', path: '/forum/topics' },
    { func: 'getTopicDetail', method: 'GET', path: '/forum/topics/{id}' },
    { func: 'createTopic', method: 'POST', path: '/forum/topics' },
    { func: 'updateTopic', method: 'PUT', path: '/forum/topics/{id}' },
    { func: 'deleteTopic', method: 'DELETE', path: '/forum/topics/{id}' },
    { func: 'getForumPosts', method: 'GET', path: '/forum/posts' },
    { func: 'getPostDetail', method: 'GET', path: '/forum/posts/{id}' },
    { func: 'createPost', method: 'POST', path: '/forum/posts' },
    { func: 'updatePost', method: 'PUT', path: '/forum/posts/{id}' },
    { func: 'deletePost', method: 'DELETE', path: '/forum/posts/{id}' },
    { func: 'getPostReplies', method: 'GET', path: '/forum/posts/{postId}/replies' },
    { func: 'createReply', method: 'POST', path: '/forum/posts/{postId}/replies' },
    { func: 'deleteReply', method: 'DELETE', path: '/forum/replies/{id}' },
    { func: 'likeReply', method: 'POST', path: '/forum/replies/{id}/like' },
    { func: 'unlikeReply', method: 'DELETE', path: '/forum/replies/{id}/like' },
    { func: 'likePost', method: 'POST', path: '/forum/posts/{id}/like' },
    { func: 'unlikePost', method: 'DELETE', path: '/forum/posts/{id}/like' },
    { func: 'favoritePost', method: 'POST', path: '/forum/posts/{id}/favorite' },
    { func: 'unfavoritePost', method: 'DELETE', path: '/forum/posts/{id}/favorite' },
    { func: 'getPendingPosts', method: 'GET', path: '/forum/posts/pending' },
    { func: 'approvePost', method: 'POST', path: '/forum/posts/{id}/approve' },
    { func: 'rejectPost', method: 'POST', path: '/forum/posts/{id}/reject' },
    { func: 'togglePostSticky', method: 'PUT', path: '/forum/posts/{id}/sticky' },
    { func: 'togglePostEssence', method: 'PUT', path: '/forum/posts/{id}/essence' },
  ],
  message: [
    { func: 'getMessages', method: 'GET', path: '/message/messages' },
    { func: 'getMessageDetail', method: 'GET', path: '/message/messages/{id}' },
    { func: 'sendMessage', method: 'POST', path: '/message/send' },
    { func: 'markAsRead', method: 'POST', path: '/message/read' },
    { func: 'deleteMessages', method: 'POST', path: '/message/delete' },
    { func: 'getUnreadCount', method: 'GET', path: '/message/notifications/unread-count' },
    { func: 'getSystemNotifications', method: 'GET', path: '/message/notifications' },
    { func: 'getNotifications', method: 'GET', path: '/message/notifications' },
    { func: 'getNotificationDetail', method: 'GET', path: '/message/notifications/{id}' },
    { func: 'deleteNotification', method: 'DELETE', path: '/message/notifications/{id}' },
    { func: 'batchMarkAsRead', method: 'PUT', path: '/message/notifications/batch-read' },
    { func: 'batchDeleteNotifications', method: 'DELETE', path: '/message/notifications/batch' },
    { func: 'getChatSessions', method: 'GET', path: '/message/list/sessions' },
    { func: 'getChatHistory', method: 'GET', path: '/message/messages/history' },
    { func: 'createChatSession', method: 'POST', path: '/message/sessions' },
    { func: 'getUserProfile', method: 'GET', path: '/message/user/{userId}' },
    { func: 'getUserDynamic', method: 'GET', path: '/message/user/{userId}/dynamic' },
    { func: 'getUserStatistics', method: 'GET', path: '/message/user/{userId}/statistics' },
    { func: 'pinChatSession', method: 'PUT', path: '/message/sessions/{sessionId}/pin' },
    { func: 'deleteChatSession', method: 'DELETE', path: '/message/sessions/{sessionId}' },
    { func: 'sendImageMessage', method: 'POST', path: '/message/messages/image' },
    { func: 'getUserPosts', method: 'GET', path: '/message/user/posts' },
    { func: 'getUserReviews', method: 'GET', path: '/message/user/reviews' },
  ]
}

// 统计并输出
console.log('='.repeat(60))
console.log('前端API接口统计（按模块）')
console.log('='.repeat(60))

let totalFunctions = 0
let totalEndpoints = 0
const allEndpoints = new Set()

for (const [module, apis] of Object.entries(frontendAPIs)) {
  console.log(`\n【${module.toUpperCase()}模块】共 ${apis.length} 个函数`)
  totalFunctions += apis.length
  
  apis.forEach(api => {
    const endpoint = `${api.method} ${api.path}`
    if (!allEndpoints.has(endpoint)) {
      allEndpoints.add(endpoint)
    }
    console.log(`  ${api.func}: ${api.method} ${api.path}`)
  })
}

console.log('\n' + '='.repeat(60))
console.log(`总计：${totalFunctions} 个API函数`)
console.log(`去重后唯一接口数：${allEndpoints.size} 个`)
console.log('='.repeat(60))

// 输出去重后的接口列表
console.log('\n去重后的接口列表：')
const sortedEndpoints = Array.from(allEndpoints).sort()
sortedEndpoints.forEach((ep, i) => {
  console.log(`${i + 1}. ${ep}`)
})

// 保存结果到JSON
const result = {
  summary: {
    totalFunctions,
    uniqueEndpoints: allEndpoints.size
  },
  byModule: {},
  allEndpoints: sortedEndpoints
}

for (const [module, apis] of Object.entries(frontendAPIs)) {
  result.byModule[module] = {
    count: apis.length,
    apis: apis.map(a => `${a.method} ${a.path}`)
  }
}

fs.writeFileSync(
  path.join(__dirname, 'frontend-api-count.json'),
  JSON.stringify(result, null, 2)
)
console.log('\n结果已保存到 frontend-api-count.json')
