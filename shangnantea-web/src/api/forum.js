import request from './index'
import { API } from './apiConstants'

// forum.js - 论坛模块API接口

/**
 * 获取首页数据
 * @returns {Promise} 首页数据
 */
export function getHomeData() {
  return request({
    url: API.FORUM.HOME_CONTENTS,
    method: 'get'
  })
}

/**
 * 更新首页数据（管理员功能）
 * @param {Object} data 首页数据
 * @returns {Promise} 更新结果
 */
export function updateHomeData(data) {
  return request({
    url: API.FORUM.HOME_CONTENTS,
    method: 'put',
    data
  })
}

/**
 * 获取Banner列表
 * @returns {Promise} Banner列表
 */
export function getBanners() {
  return request({
    url: API.FORUM.BANNERS,
    method: 'get'
  })
}

/**
 * 上传Banner
 * @param {FormData} formData Banner数据（包含图片文件）
 * @returns {Promise} 上传结果
 */
export function uploadBanner(formData) {
  return request({
    url: API.FORUM.BANNERS,
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 更新Banner
 * @param {number} id Banner ID
 * @param {Object} data Banner数据
 * @returns {Promise} 更新结果
 */
export function updateBanner(id, data) {
  return request({
    url: `${API.FORUM.BANNERS}/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除Banner
 * @param {number} id Banner ID
 * @returns {Promise} 删除结果
 */
export function deleteBanner(id) {
  return request({
    url: `${API.FORUM.BANNERS}/${id}`,
    method: 'delete'
  })
}

/**
 * 更新Banner顺序
 * @param {Array} bannerIds Banner ID数组（按新顺序排列）
 * @returns {Promise} 更新结果
 */
export function updateBannerOrder(bannerIds) {
  return request({
    url: API.FORUM.BANNER_ORDER,
    method: 'put',
    data: { bannerIds }
  })
}

/**
 * 获取文章列表
 * @param {Object} params 查询参数
 * @returns {Promise} 文章列表
 */
export function getArticles(params = {}) {
  return request({
    url: API.FORUM.ARTICLES,
    method: 'get',
    params
  })
}

/**
 * 获取文章详情
 * @param {number} id 文章ID
 * @returns {Promise} 文章详情，包含 isLiked 和 isFavorited 字段（当前用户是否已点赞/收藏该文章）
 */
export function getArticleDetail(id) {
  return request({
    url: `${API.FORUM.ARTICLES}/${id}`,
    method: 'get'
  })
}

/**
 * 创建文章（管理员功能）
 * @param {Object} data 文章数据
 * @returns {Promise} 创建结果
 */
export function createArticle(data) {
  return request({
    url: API.FORUM.ARTICLES,
    method: 'post',
    data
  })
}

/**
 * 更新文章（管理员功能）
 * @param {number} id 文章ID
 * @param {Object} data 文章数据
 * @returns {Promise} 更新结果
 */
export function updateArticle(id, data) {
  return request({
    url: `${API.FORUM.ARTICLES}/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除文章（管理员功能）
 * @param {number} id 文章ID
 * @returns {Promise} 删除结果
 */
export function deleteArticle(id) {
  return request({
    url: `${API.FORUM.ARTICLES}/${id}`,
    method: 'delete'
  })
}

/**
 * 获取论坛版块列表
 * @returns {Promise} 版块列表
 */
export function getForumTopics() {
  return request({
    url: API.FORUM.TOPICS,
    method: 'get'
  })
}

/**
 * 获取版块详情
 * @param {number} id 版块ID
 * @returns {Promise} 版块详情
 */
export function getTopicDetail(id) {
  return request({
    url: `${API.FORUM.TOPICS}/${id}`,
    method: 'get'
  })
}

/**
 * 创建版块（管理员功能）
 * @param {Object} data 版块数据
 * @returns {Promise} 创建结果
 */
export function createTopic(data) {
  return request({
    url: API.FORUM.TOPICS,
    method: 'post',
    data
  })
}

/**
 * 更新版块（管理员功能）
 * @param {number} id 版块ID
 * @param {Object} data 版块数据
 * @returns {Promise} 更新结果
 */
export function updateTopic(id, data) {
  return request({
    url: `${API.FORUM.TOPICS}/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除版块（管理员功能）
 * @param {number} id 版块ID
 * @returns {Promise} 删除结果
 */
export function deleteTopic(id) {
  return request({
    url: `${API.FORUM.TOPICS}/${id}`,
    method: 'delete'
  })
}

/**
 * 获取帖子列表
 * @param {Object} params 查询参数
 * @returns {Promise} 帖子列表
 */
export function getForumPosts(params = {}) {
  return request({
    url: API.FORUM.POSTS,
    method: 'get',
    params
  })
}

/**
 * 获取帖子详情
 * @param {number} id 帖子ID
 * @returns {Promise} 帖子详情，包含 isLiked 和 isFavorited 字段（当前用户是否已点赞/收藏该帖子）
 */
export function getPostDetail(id) {
  return request({
    url: `${API.FORUM.POSTS}/${id}`,
    method: 'get'
  })
}

/**
 * 创建帖子
 * @param {Object} data 帖子数据
 * @returns {Promise} 创建结果
 */
export function createPost(data) {
  return request({
    url: API.FORUM.POSTS,
    method: 'post',
    data
  })
}

/**
 * 上传帖子图片
 * @param {File} file 图片文件
 * @returns {Promise} 上传结果
 */
export function uploadPostImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: API.FORUM.POST_IMAGE,
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 更新帖子
 * @param {number} id 帖子ID
 * @param {Object} data 帖子数据
 * @returns {Promise} 更新结果
 */
export function updatePost(id, data) {
  return request({
    url: `${API.FORUM.POSTS}/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除帖子
 * @param {number} id 帖子ID
 * @returns {Promise} 删除结果
 */
export function deletePost(id) {
  return request({
    url: `${API.FORUM.POSTS}/${id}`,
    method: 'delete'
  })
}

/**
 * 获取帖子回复列表
 * @param {number} postId 帖子ID
 * @param {Object} params 查询参数
 * @returns {Promise} 回复列表，每个回复对象包含 isLiked 字段（当前用户是否已点赞该回复）
 */
export function getPostReplies(postId, params = {}) {
  return request({
    url: `${API.FORUM.POSTS}/${postId}/replies`,
    method: 'get',
    params
  })
}

/**
 * 创建回复
 * @param {number} postId 帖子ID
 * @param {Object} data 回复数据
 * @returns {Promise} 创建结果
 */
export function createReply(postId, data) {
  return request({
    url: `${API.FORUM.POSTS}/${postId}/replies`,
    method: 'post',
    data
  })
}

/**
 * 删除回复
 * @param {number} id 回复ID
 * @returns {Promise} 删除结果
 */
export function deleteReply(id) {
  return request({
    url: `${API.FORUM.REPLIES}/${id}`,
    method: 'delete'
  })
}

/**
 * ⚠️ 已删除：likeReply, unlikeReply, likePost, unlikePost, favoritePost, unfavoritePost
 * 说明：帖子/回复/文章的点赞和收藏功能已统一使用用户模块的通用接口（user.js 中的 addLike/removeLike, addFavorite/removeFavorite）
 * 详情接口（getPostDetail, getArticleDetail, getPostReplies）已包含相应的状态字段，无需单独调用点赞/收藏接口
 */

// ===== 任务组F：内容审核相关API方法 =====

/**
 * 获取待审核帖子列表
 * @param {Object} params 查询参数
 * @returns {Promise} 待审核帖子列表
 */
export function getPendingPosts(params = {}) {
  return request({
    url: API.FORUM.POSTS_PENDING,
    method: 'get',
    params
  })
}

/**
 * 审核通过帖子
 * @param {number} id 帖子ID
 * @param {Object} data 审核数据
 * @returns {Promise} 审核结果
 */
export function approvePost(id, data) {
  return request({
    url: `${API.FORUM.POSTS}/${id}/approve`,
    method: 'post',
    data
  })
}

/**
 * 审核拒绝帖子
 * @param {number} id 帖子ID
 * @param {Object} data 审核数据（包含拒绝原因）
 * @returns {Promise} 审核结果
 */
export function rejectPost(id, data) {
  return request({
    url: `${API.FORUM.POSTS}/${id}/reject`,
    method: 'post',
    data
  })
}

/**
 * 设置帖子置顶/取消置顶
 * @param {number} id 帖子ID
 * @param {boolean} isSticky 是否置顶
 * @returns {Promise} 操作结果
 */
export function togglePostSticky(id, isSticky) {
  return request({
    url: `${API.FORUM.POSTS}/${id}/sticky`,
    method: 'put',
    params: { isSticky }
  })
}

/**
 * 设置帖子精华/取消精华
 * @param {number} id 帖子ID
 * @param {boolean} isEssence 是否精华
 * @returns {Promise} 操作结果
 */
export function togglePostEssence(id, isEssence) {
  return request({
    url: `${API.FORUM.POSTS}/${id}/essence`,
    method: 'put',
    params: { isEssence }
  })
}

export default {
  getHomeData,
  updateHomeData,
  getBanners,
  uploadBanner,
  updateBanner,
  deleteBanner,
  updateBannerOrder,
  getArticles,
  getArticleDetail,
  createArticle,
  updateArticle,
  deleteArticle,
  getForumTopics,
  getTopicDetail,
  createTopic,
  updateTopic,
  deleteTopic,
  getForumPosts,
  getPostDetail,
  createPost,
  uploadPostImage,
  updatePost,
  deletePost,
  getPostReplies,
  createReply,
  deleteReply,
  // 任务组F：内容审核相关
  getPendingPosts,
  approvePost,
  rejectPost,
  togglePostSticky,
  togglePostEssence
} 