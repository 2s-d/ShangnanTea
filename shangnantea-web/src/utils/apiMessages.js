/**
 * API消息工具 - 基于状态码的消息映射
 * 
 * ⚠️ 重要：本文件必须与 docs/code-message-mapping.md 保持完全同步
 * 
 * 功能：
 * - 根据状态码获取对应消息
 * - 自动显示成功/错误消息
 * - 判断响应是否成功
 * - 静默状态码不显示消息（但记录日志）
 * 
 * 状态码规则：
 * - 200: HTTP成功（静默，不显示消息）
 * - x0xx: 业务成功码（需要显示消息，除非在静默列表中）
 * - x1xx: 业务失败码（需要显示消息）
 * 
 * 同步维护要求：
 * 1. CODE_MAP 必须包含文档中的所有状态码和消息
 * 2. SILENT_CODES 必须与文档中的 [静默] 标注完全一致
 * 3. 文档更新时，必须同步更新本文件
 * 4. 状态码去重：同一状态码只保留一个消息（四位数状态码足够分配，不会重复）
 * 
 * 数据来源：docs/code-message-mapping.md
 * 最后更新: 2026-01-13
 */

import { apiMessage } from './messageManager'

// ⚠️ 同步维护要求：
// 1. 本文档中的所有状态码和消息必须出现在 CODE_MAP 中
// 2. 文档中标注 [静默] 的状态码必须出现在 SILENT_CODES 中
// 3. 文档更新时，必须同步更新 CODE_MAP 和 SILENT_CODES
// 4. 状态码去重：同一状态码只保留一个消息（四位数状态码足够分配，不会重复）

// 静默状态码列表
// 这些状态码不会显示消息给用户，但会在开发环境记录日志
// ⚠️ 必须与 docs/code-message-mapping.md 中的 [静默] 标注完全一致
const SILENT_CODES = [
  200,  // HTTP成功 - 静默
  2013, // 用户模块-接口20: removeFollow - 已取消关注
  2015, // 用户模块-接口23: removeFavorite - 已取消收藏
  2017, // 用户模块-接口25: removeLike - 已取消点赞
  3000, // 茶叶模块-接口1: getTeas - 茶叶列表加载成功
  4016  // 店铺模块-接口23: unfollowShop - 已取消关注
]

// 状态码消息映射表
// ⚠️ 必须与 docs/code-message-mapping.md 文档完全同步
// 基于文档中的166个接口提取，每个状态码只保留一个消息
// 状态码去重：同一状态码在不同接口中出现时，消息应该一致（四位数状态码足够分配，不会重复）
export const CODE_MAP = {
  // HTTP状态码
  200: '操作成功',
  400: '请求错误',
  401: '未认证',
  403: '无权限',
  404: '资源不存在',
  500: '服务器错误',

  // ========== 用户模块 (2xxx) ==========
  // 接口1: login - /user/login
  2000: '登录成功',
  2100: '登录失败，请检查用户名和密码',
  2101: '登录失败，服务器Token生成失败',
  
  // 接口2: register - /user/register
  2001: '注册成功，请登录',
  2102: '注册失败，用户名已存在或数据格式错误',
  
  // 接口3: logout - /user/logout
  2002: '已安全退出系统',
  2103: '退出登录失败',
  
  // 接口4: getCurrentUser - /user/me
  2104: '获取当前用户信息失败',
  
  // 接口5: refreshToken - /user/refresh
  2105: '刷新令牌失败',
  2106: '您的登录已过期，请重新登录',
  
  // 接口6: getUserInfo - /user/{userId}
  2107: '获取用户信息失败',
  
  // 接口7: updateUserInfo - /user
  2003: '个人资料更新成功',
  2108: '个人资料更新失败',
  
  // 接口8: uploadAvatar - /user/avatar
  2004: '头像更新成功',
  2109: '头像更新失败',
  2110: '不支持的文件类型',
  2111: '文件大小超限',
  
  // 接口9: changePassword - /user/password
  2005: '密码修改成功，请使用新密码登录',
  2112: '密码修改失败，请检查原密码是否正确',
  2113: '两次输入的密码不一致',
  
  // 接口10: resetPassword - /user/password/reset
  2006: '密码重置成功',
  2114: '密码重置失败',
  
  // 接口11: getAddressList - /user/addresses
  2115: '获取地址失败',
  
  // 接口12: addAddress - /user/addresses
  2007: '地址添加成功',
  2116: '保存地址失败',
  
  // 接口13: updateAddress - /user/addresses/{id}
  2008: '更新成功',
  2117: '保存地址失败',
  
  // 接口14: deleteAddress - /user/addresses/{id}
  2009: '删除成功',
  2118: '操作失败',
  
  // 接口15: setDefaultAddress - /user/addresses/{id}/default
  2010: '更新成功',
  2119: '操作失败',
  
  // 接口16: submitShopCertification - /user/shop-certification
  2011: '操作成功',
  2120: '操作失败',
  
  // 接口17: getShopCertificationStatus - /user/shop-certification
  2121: '加载失败',
  
  // 接口18: getFollowList - /user/follows
  2122: '加载失败',
  
  // 接口19: addFollow - /user/follows
  2012: '已关注店铺',
  2123: '操作失败',
  
  // 接口20: removeFollow - /user/follows/{id}
  2013: '已取消关注',
  2124: '操作失败',
  
  // 接口21: getFavoriteList - /user/favorites
  2125: '加载失败',
  
  // 接口22: addFavorite - /user/favorites
  2014: '已收藏',
  2126: '操作失败',
  
  // 接口23: removeFavorite - /user/favorites/{id}
  2015: '已取消收藏',
  2127: '操作失败',
  
  // 接口24: addLike - /user/likes
  2016: '点赞成功',
  2128: '点赞失败',
  
  // 接口25: removeLike - /user/likes/{id}
  2017: '已取消点赞',
  2129: '取消点赞失败',
  
  // 接口26: getUserPreferences - /user/preferences
  2130: '加载失败',
  
  // 接口27: updateUserPreferences - /user/preferences
  2018: '偏好设置更新成功',
  2131: '偏好设置更新失败',
  
  // 接口28: getAdminUserList - /user/admin/users
  2132: '获取用户列表失败',
  2133: '您没有权限执行此操作',
  
  // 接口29: createAdmin - /user/admin/users
  2019: '管理员已添加',
  2134: '提交失败',
  2135: '您没有权限执行此操作',
  
  // 接口30: updateUser - /user/admin/users/{userId}
  2020: '用户已更新',
  2136: '提交失败',
  2137: '您没有权限执行此操作',
  
  // 接口31: deleteUser - /user/admin/users/{userId}
  2021: '用户已删除',
  2138: '删除用户失败',
  2139: '您没有权限执行此操作',
  
  // 接口32: toggleUserStatus - /user/admin/users/{userId}/status
  2022: '用户状态已修改',
  2140: '修改用户状态失败',
  2141: '您没有权限执行此操作',
  
  // 接口33: getCertificationList - /user/admin/certifications
  2142: '加载失败',
  2143: '您没有权限执行此操作',
  
  // 接口34: processCertification - /user/admin/certifications/{id}
  2023: '操作成功',
  2144: '操作失败',
  2145: '您没有权限执行此操作',
  
  // ========== 茶叶模块 (3xxx) ==========
  // 接口1: getTeas - /tea/list
  3000: '茶叶列表加载成功',
  3100: '加载茶叶数据失败',
  
  // 接口2: addTea - /tea/list
  3001: '茶叶添加成功',
  3101: '操作失败',
  
  // 接口3: getTeaDetail - /tea/{id}
  3102: '加载茶叶详情失败',
  
  // 接口4: updateTea - /tea/{id}
  3002: '茶叶更新成功',
  3103: '操作失败',
  
  // 接口5: deleteTea - /tea/{id}
  3003: '删除成功',
  3104: '删除失败',
  
  // 接口6: getRecommendTeas - /tea/recommend
  3105: '加载茶叶数据失败',
  
  // 接口7: getTeaCategories - /tea/categories
  3106: '操作失败',
  
  // 接口8: createCategory - /tea/categories
  3004: '分类创建成功',
  3107: '操作失败',
  
  // 接口9: updateCategory - /tea/categories/{id}
  3005: '分类更新成功',
  3108: '操作失败',
  
  // 接口10: deleteCategory - /tea/categories/{id}
  3006: '分类删除成功',
  3109: '删除分类失败',
  
  // 接口11: getTeaReviews - /tea/{teaId}/reviews
  3110: '加载失败',
  
  // 接口12: getReviewStats - /tea/{teaId}/reviews/stats
  3111: '加载失败',
  
  // 接口13: submitReview - /tea/reviews
  3007: '评价提交成功，感谢您的反馈',
  3112: '评价提交失败',
  
  // 接口14: replyReview - /tea/reviews/{reviewId}/reply
  3008: '回复成功',
  3113: '回复失败',
  
  // 接口15: likeReview - /tea/reviews/{reviewId}/like
  3009: '点赞成功',
  3114: '点赞失败',
  
  // 接口16: getTeaSpecifications - /tea/{teaId}/specifications
  3115: '加载失败',
  
  // 接口17: addSpecification - /tea/{teaId}/specifications
  3010: '操作成功',
  3116: '操作失败',
  
  // 接口18: updateSpecification - /tea/specifications/{specId}
  3011: '更新成功',
  3117: '操作失败',
  
  // 接口19: deleteSpecification - /tea/specifications/{specId}
  3012: '删除成功',
  3118: '操作失败',
  
  // 接口20: setDefaultSpecification - /tea/specifications/{specId}/default
  3013: '更新成功',
  3119: '操作失败',
  
  // 接口21: uploadTeaImages - /tea/{teaId}/images
  3014: '上传成功',
  3120: '上传失败',
  3121: '不支持的文件类型',
  3122: '文件大小超限',
  
  // 接口22: deleteTeaImage - /tea/images/{imageId}
  3015: '删除成功',
  3123: '操作失败',
  
  // 接口23: setMainImage - /tea/images/{imageId}/main
  3016: '更新成功',
  3124: '操作失败',
  
  // 接口24: updateImageOrder - /tea/images/order
  3017: '更新成功',
  3125: '操作失败',
  
  // 接口25: toggleTeaStatus - /tea/{teaId}/status
  3018: '上架成功',
  3019: '下架成功',
  3126: '上架失败',
  3127: '下架失败',
  
  // 接口26: batchToggleTeaStatus - /tea/batch-status
  3020: '批量上架成功',
  3021: '批量下架成功',
  3128: '批量上架失败',
  3129: '批量下架失败',
  
  // ========== 店铺模块 (4xxx) ==========
  // 接口1: getShops - /shop/list
  4100: '加载店铺数据失败',
  
  // 接口2: createShop - /shop/list
  4000: '操作成功',
  4101: '操作失败',
  
  // 接口3: getShopDetail - /shop/{id}
  4102: '加载店铺信息失败',
  4103: '店铺信息不存在',
  
  // 接口4: updateShop - /shop/{id}
  4001: '更新成功',
  4104: '操作失败',
  
  // 接口5: getMyShop - /shop/my
  4105: '加载店铺信息失败',
  
  // 接口6: getShopStatistics - /shop/{shopId}/statistics
  4106: '加载失败',
  
  // 接口7: getShopTeas - /shop/{shopId}/teas
  4107: '加载店铺数据失败',
  
  // 接口8: addShopTea - /shop/{shopId}/teas
  4002: '茶叶添加成功',
  4108: '操作失败',
  
  // 接口9: updateShopTea - /shop/teas/{teaId}
  4003: '茶叶更新成功',
  4109: '操作失败',
  
  // 接口10: deleteShopTea - /shop/teas/{teaId}
  4004: '删除成功',
  4110: '删除失败',
  
  // 接口11: toggleShopTeaStatus - /shop/teas/{teaId}/status
  4005: '上架成功',
  4006: '下架成功',
  4111: '上架失败',
  4112: '下架失败',
  
  // 接口12: uploadShopLogo - /shop/{shopId}/logo
  4007: 'Logo上传成功',
  4113: 'Logo上传失败',
  4114: '不支持的文件类型',
  4115: '文件大小超限',
  
  // 接口13: getShopBanners - /shop/{shopId}/banners
  4116: '加载Banner列表失败',
  
  // 接口14: uploadBanner - /shop/{shopId}/banners
  4008: 'Banner添加成功',
  4117: '保存失败',
  4118: '不支持的文件类型',
  4119: '文件大小超限',
  
  // 接口15: updateBanner - /shop/banners/{bannerId}
  4009: 'Banner更新成功',
  4120: '保存失败',
  
  // 接口16: deleteBanner - /shop/banners/{bannerId}
  4010: '删除成功',
  4121: '删除失败',
  
  // 接口17: updateBannerOrder - /shop/banners/order
  4011: '排序更新成功',
  4122: '排序更新失败',
  
  // 接口18: getShopAnnouncements - /shop/{shopId}/announcements
  4123: '加载公告列表失败',
  
  // 接口19: createAnnouncement - /shop/{shopId}/announcements
  4012: '公告添加成功',
  4124: '保存失败',
  
  // 接口20: updateAnnouncement - /shop/announcements/{announcementId}
  4013: '公告更新成功',
  4125: '保存失败',
  
  // 接口21: deleteAnnouncement - /shop/announcements/{announcementId}
  4014: '删除成功',
  4126: '删除失败',
  
  // 接口22: followShop - /shop/{shopId}/follow
  4015: '已关注店铺',
  4127: '操作失败',
  
  // 接口23: unfollowShop - /shop/{shopId}/follow
  4016: '已取消关注',
  4128: '操作失败',
  
  // 接口24: checkFollowStatus - /shop/{shopId}/follow-status
  4129: '加载失败',
  
  // 接口25: getShopReviews - /shop/{shopId}/reviews
  4130: '加载评价失败',
  
  // 接口26: submitShopReview - /shop/{shopId}/reviews
  4017: '评价提交成功',
  4131: '提交评价失败',
  
}

/**
 * 根据状态码获取对应消息
 * @param {number} code - 状态码
 * @returns {string} 对应的消息文本
 */
export function getMessageByCode(code) {
  return CODE_MAP[code] || `未知状态码: ${code}`
}

/**
 * 根据状态码自动显示消息
 * @param {number} code - 状态码
 * @param {string} customMessage - 自定义消息（可选）
 * 
 * 处理流程：
 * 1. 检查是否为静默状态码 → 静默处理（开发环境记录日志，不显示消息）
 * 2. 获取消息文本
 * 3. 检查是否为未知状态码 → 警告处理（开发环境记录警告，不显示消息）
 * 4. 正常显示消息（成功/错误）
 */
export function showByCode(code, customMessage = null) {
  // 1. 检查是否为静默状态码
  if (SILENT_CODES.includes(code)) {
    // 静默码：开发环境记录日志，但不显示消息
    if (process.env.NODE_ENV === 'development') {
      const message = customMessage || getMessageByCode(code)
      console.log(`[静默] ${code}: ${message}`)
    }
    return  // 直接返回，不显示消息
  }
  
  // 2. 获取消息
  const message = customMessage || getMessageByCode(code)
  
  // 3. 未知状态码处理：不显示，但记录警告
  if (message.startsWith('未知状态码')) {
    if (process.env.NODE_ENV === 'development') {
      console.warn(`[警告] 未知状态码: ${code}`)
    }
    return
  }
  
  // 4. 正常显示消息
  if (isSuccess(code)) {
    apiMessage.success(message)
  } else {
    apiMessage.error(message)
  }
}

/**
 * 判断状态码是否表示成功
 * @param {number} code - 状态码
 * @returns {boolean} 是否成功
 * 
 * 状态码规则：
 * - 200: HTTP成功
 * - x0xx: 业务成功码（百位为0，如 6010, 6011, 6012）
 * - x1xx: 业务失败码（百位为1，如 6110, 6111, 6112）
 */
export function isSuccess(code) {
  // HTTP 200 成功
  if (code === 200) return true
  
  // 业务状态码：判断百位数字
  // x0xx = 成功（百位为0）
  // x1xx = 失败（百位为1）
  if (code >= 1000 && code <= 7999) {
    const hundredsDigit = Math.floor((code % 1000) / 100)
    return hundredsDigit === 0
  }
  
  return false
}

// 默认导出
export default {
  CODE_MAP,
  getMessageByCode,
  showByCode,
  isSuccess
}
