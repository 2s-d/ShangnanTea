/**
 * 论坛模块消息工具
 * 
 * 分类说明：
 * - SUCCESS: 操作成功反馈
 * - ERROR: 操作失败/错误提示
 * - PROMPT: 用户提示（表单验证+确认）
 */

import { successMessage, errorMessage, promptMessage } from './messageManager'

// 论坛模块消息常量
export const FORUM_MESSAGES = {
  // 成功消息
  SUCCESS: {
    POST_CREATED: '帖子发布成功',
    POST_UPDATED: '帖子更新成功',
    POST_DELETED: '帖子删除成功',
    POST_LIKED: '点赞成功',
    POST_UNLIKED: '已取消点赞',
    POST_FAVORITED: '收藏成功',
    POST_UNFAVORITED: '已取消收藏',
    POST_PINNED: '帖子已置顶',
    POST_UNPINNED: '帖子已取消置顶',
    POST_FEATURED: '帖子已加精',
    POST_UNFEATURED: '帖子已取消加精',
    COMMENT_CREATED: '评论发表成功',
    COMMENT_DELETED: '评论已删除',
    REPLY_CREATED: '回复发布成功',
    DRAFT_SAVED: '草稿已保存',
    // 审核管理
    POST_APPROVED: '帖子审核通过',
    POST_REJECTED: '帖子审核拒绝',
    // 版块管理
    TOPIC_CREATED: '添加版块成功',
    TOPIC_UPDATED: '更新版块成功',
    TOPIC_DELETED: '删除版块成功',
    TOPIC_ENABLED: '启用版块成功',
    TOPIC_DISABLED: '禁用版块成功',
    // 文章管理
    ARTICLE_CREATED: '文章创建成功',
    ARTICLE_UPDATED: '文章更新成功',
    ARTICLE_DELETED: '文章已删除',
    ARTICLE_RECOMMENDED: '已推荐文章',
    ARTICLE_UNRECOMMENDED: '已取消推荐',
    ARTICLE_REFRESHED: '文章内容已更新',
    // 区块管理
    BLOCK_SAVED: '区块内容已保存',
    BLOCK_STATUS_CHANGED: '区块状态已更新'
  },
  
  // 错误消息
  ERROR: {
    POST_CREATE_FAILED: '帖子发布失败',
    POST_UPDATE_FAILED: '帖子更新失败',
    POST_DELETE_FAILED: '帖子删除失败',
    POST_APPROVE_FAILED: '审核通过失败',
    POST_REJECT_FAILED: '审核拒绝失败',
    LIKE_FAILED: '点赞失败',
    UNLIKE_FAILED: '取消点赞失败',
    FAVORITE_FAILED: '收藏失败',
    UNFAVORITE_FAILED: '取消收藏失败',
    PIN_FAILED: '置顶失败',
    UNPIN_FAILED: '取消置顶失败',
    FEATURE_FAILED: '加精失败',
    UNFEATURE_FAILED: '取消加精失败',
    COMMENT_CREATE_FAILED: '评论发表失败',
    COMMENT_DELETE_FAILED: '评论删除失败',
    REPLY_CREATE_FAILED: '回复发布失败',
    LOAD_FAILED: '加载失败',
    LOAD_POSTS_FAILED: '获取帖子列表失败',
    LOAD_POST_DETAIL_FAILED: '获取帖子详情失败',
    LOAD_REPLIES_FAILED: '获取回复列表失败',
    LOAD_TOPICS_FAILED: '获取版块列表失败',
    LOAD_PENDING_POSTS_FAILED: '获取待审核帖子列表失败',
    OPERATION_FAILED: '操作失败，请重试',
    UNAUTHORIZED: '请先登录再进行操作',
    FORBIDDEN: '您没有权限执行此操作',
    // 版块管理
    TOPIC_CREATE_FAILED: '添加版块失败',
    TOPIC_UPDATE_FAILED: '更新版块失败',
    TOPIC_DELETE_FAILED: '删除版块失败',
    // 文章管理
    ARTICLE_CREATE_FAILED: '文章创建失败',
    ARTICLE_UPDATE_FAILED: '文章更新失败',
    ARTICLE_DELETE_FAILED: '文章删除失败',
    ARTICLE_LOAD_FAILED: '获取文章列表失败',
    ARTICLE_REFRESH_FAILED: '刷新文章失败，请稍后再试',
    // 首页数据
    HOME_DATA_LOAD_FAILED: '获取首页数据失败',
    BANNER_LOAD_FAILED: '获取轮播图数据失败',
    RECOMMEND_TEA_LOAD_FAILED: '获取推荐茶叶失败',
    BLOCK_SAVE_FAILED: '保存失败'
  },
  
  // 提示消息
  PROMPT: {
    TITLE_REQUIRED: '标题不能为空',
    CONTENT_REQUIRED: '内容不能为空',
    CATEGORY_REQUIRED: '请选择分类',
    COMMENT_REQUIRED: '回复内容不能为空',
    COMMENT_TOO_LONG: '评论内容不能超过500字',
    IMAGE_SIZE_LIMIT: '图片大小不能超过2MB',
    IMAGE_FORMAT_INVALID: '只支持JPG、PNG和GIF格式的图片',
    DELETE_CONFIRM: '确定要删除吗？此操作不可撤销',
    SHARE_DEVELOPING: '分享功能正在开发中',
    AR_DEVELOPING: 'AR虚拟试饮功能正在开发中，敬请期待...',
    FEATURE_DEVELOPING: '功能待后端接口接入',
    BANNER_DATA_ERROR: '轮播图数据格式有误，已重置',
    RECOMMEND_DATA_ERROR: '推荐茶叶数据格式有误，已重置'
  }
}

// 成功消息函数
export const forumSuccessMessages = {
  showPostCreated() {
    successMessage.show(FORUM_MESSAGES.SUCCESS.POST_CREATED)
  },
  showPostUpdated() {
    successMessage.show(FORUM_MESSAGES.SUCCESS.POST_UPDATED)
  },
  showPostDeleted() {
    successMessage.show(FORUM_MESSAGES.SUCCESS.POST_DELETED)
  },
  showPostLiked() {
    successMessage.show(FORUM_MESSAGES.SUCCESS.POST_LIKED)
  },
  showPostUnliked() {
    successMessage.show(FORUM_MESSAGES.SUCCESS.POST_UNLIKED)
  },
  showPostFavorited() {
    successMessage.show(FORUM_MESSAGES.SUCCESS.POST_FAVORITED)
  },
  showPostUnfavorited() {
    successMessage.show(FORUM_MESSAGES.SUCCESS.POST_UNFAVORITED)
  },
  showPostPinned() {
    successMessage.show(FORUM_MESSAGES.SUCCESS.POST_PINNED)
  },
  showPostUnpinned() {
    successMessage.show(FORUM_MESSAGES.SUCCESS.POST_UNPINNED)
  },
  showPostFeatured() {
    successMessage.show(FORUM_MESSAGES.SUCCESS.POST_FEATURED)
  },
  showPostUnfeatured() {
    successMessage.show(FORUM_MESSAGES.SUCCESS.POST_UNFEATURED)
  },
  showCommentCreated() {
    successMessage.show(FORUM_MESSAGES.SUCCESS.COMMENT_CREATED)
  },
  showCommentDeleted() {
    successMessage.show(FORUM_MESSAGES.SUCCESS.COMMENT_DELETED)
  },
  showReplyCreated() {
    successMessage.show(FORUM_MESSAGES.SUCCESS.REPLY_CREATED)
  },
  showDraftSaved() {
    successMessage.show(FORUM_MESSAGES.SUCCESS.DRAFT_SAVED)
  },
  // 审核管理
  showPostApproved() {
    successMessage.show(FORUM_MESSAGES.SUCCESS.POST_APPROVED)
  },
  showPostRejected() {
    successMessage.show(FORUM_MESSAGES.SUCCESS.POST_REJECTED)
  },
  // 版块管理
  showTopicCreated() {
    successMessage.show(FORUM_MESSAGES.SUCCESS.TOPIC_CREATED)
  },
  showTopicUpdated() {
    successMessage.show(FORUM_MESSAGES.SUCCESS.TOPIC_UPDATED)
  },
  showTopicDeleted() {
    successMessage.show(FORUM_MESSAGES.SUCCESS.TOPIC_DELETED)
  },
  showTopicEnabled() {
    successMessage.show(FORUM_MESSAGES.SUCCESS.TOPIC_ENABLED)
  },
  showTopicDisabled() {
    successMessage.show(FORUM_MESSAGES.SUCCESS.TOPIC_DISABLED)
  },
  // 文章管理
  showArticleCreated() {
    successMessage.show(FORUM_MESSAGES.SUCCESS.ARTICLE_CREATED)
  },
  showArticleUpdated() {
    successMessage.show(FORUM_MESSAGES.SUCCESS.ARTICLE_UPDATED)
  },
  showArticleDeleted() {
    successMessage.show(FORUM_MESSAGES.SUCCESS.ARTICLE_DELETED)
  },
  showArticleRecommended(title) {
    successMessage.show(`已推荐文章: ${title}`)
  },
  showArticleUnrecommended(title) {
    successMessage.show(`已取消推荐: ${title}`)
  },
  showArticleRefreshed() {
    successMessage.show(FORUM_MESSAGES.SUCCESS.ARTICLE_REFRESHED)
  },
  // 区块管理
  showBlockSaved() {
    successMessage.show(FORUM_MESSAGES.SUCCESS.BLOCK_SAVED)
  },
  showBlockStatusChanged(blockName, status) {
    successMessage.show(`区块「${blockName}」已设为${status === 1 ? '显示' : '隐藏'}`)
  }
}

// 错误消息函数
export const forumErrorMessages = {
  showPostCreateFailed(reason) {
    errorMessage.show(reason || FORUM_MESSAGES.ERROR.POST_CREATE_FAILED)
  },
  showPostUpdateFailed(reason) {
    errorMessage.show(reason || FORUM_MESSAGES.ERROR.POST_UPDATE_FAILED)
  },
  showPostDeleteFailed(reason) {
    errorMessage.show(reason || FORUM_MESSAGES.ERROR.POST_DELETE_FAILED)
  },
  showLikeFailed(reason) {
    errorMessage.show(reason || FORUM_MESSAGES.ERROR.LIKE_FAILED)
  },
  showUnlikeFailed(reason) {
    errorMessage.show(reason || FORUM_MESSAGES.ERROR.UNLIKE_FAILED)
  },
  showFavoriteFailed(reason) {
    errorMessage.show(reason || FORUM_MESSAGES.ERROR.FAVORITE_FAILED)
  },
  showUnfavoriteFailed(reason) {
    errorMessage.show(reason || FORUM_MESSAGES.ERROR.UNFAVORITE_FAILED)
  },
  showPinFailed(reason) {
    errorMessage.show(reason || FORUM_MESSAGES.ERROR.PIN_FAILED)
  },
  showUnpinFailed(reason) {
    errorMessage.show(reason || FORUM_MESSAGES.ERROR.UNPIN_FAILED)
  },
  showFeatureFailed(reason) {
    errorMessage.show(reason || FORUM_MESSAGES.ERROR.FEATURE_FAILED)
  },
  showUnfeatureFailed(reason) {
    errorMessage.show(reason || FORUM_MESSAGES.ERROR.UNFEATURE_FAILED)
  },
  showCommentCreateFailed(reason) {
    errorMessage.show(reason || FORUM_MESSAGES.ERROR.COMMENT_CREATE_FAILED)
  },
  showCommentDeleteFailed(reason) {
    errorMessage.show(reason || FORUM_MESSAGES.ERROR.COMMENT_DELETE_FAILED)
  },
  showReplyCreateFailed(reason) {
    errorMessage.show(reason || FORUM_MESSAGES.ERROR.REPLY_CREATE_FAILED)
  },
  showLoadFailed(reason) {
    errorMessage.show(reason || FORUM_MESSAGES.ERROR.LOAD_FAILED)
  },
  showLoadPostsFailed(reason) {
    errorMessage.show(reason || FORUM_MESSAGES.ERROR.LOAD_POSTS_FAILED)
  },
  showLoadPostDetailFailed(reason) {
    errorMessage.show(reason || FORUM_MESSAGES.ERROR.LOAD_POST_DETAIL_FAILED)
  },
  showLoadRepliesFailed(reason) {
    errorMessage.show(reason || FORUM_MESSAGES.ERROR.LOAD_REPLIES_FAILED)
  },
  showLoadTopicsFailed(reason) {
    errorMessage.show(reason || FORUM_MESSAGES.ERROR.LOAD_TOPICS_FAILED)
  },
  showLoadPendingPostsFailed(reason) {
    errorMessage.show(reason || FORUM_MESSAGES.ERROR.LOAD_PENDING_POSTS_FAILED)
  },
  showPostApproveFailed(reason) {
    errorMessage.show(reason || FORUM_MESSAGES.ERROR.POST_APPROVE_FAILED)
  },
  showPostRejectFailed(reason) {
    errorMessage.show(reason || FORUM_MESSAGES.ERROR.POST_REJECT_FAILED)
  },
  showOperationFailed(reason) {
    errorMessage.show(reason || FORUM_MESSAGES.ERROR.OPERATION_FAILED)
  },
  showUnauthorized() {
    errorMessage.warning(FORUM_MESSAGES.ERROR.UNAUTHORIZED)
  },
  showForbidden() {
    errorMessage.warning(FORUM_MESSAGES.ERROR.FORBIDDEN)
  },
  // 版块管理
  showTopicCreateFailed(reason) {
    errorMessage.show(reason || FORUM_MESSAGES.ERROR.TOPIC_CREATE_FAILED)
  },
  showTopicUpdateFailed(reason) {
    errorMessage.show(reason || FORUM_MESSAGES.ERROR.TOPIC_UPDATE_FAILED)
  },
  showTopicDeleteFailed(reason) {
    errorMessage.show(reason || FORUM_MESSAGES.ERROR.TOPIC_DELETE_FAILED)
  },
  // 文章管理
  showArticleCreateFailed(reason) {
    errorMessage.show(reason || FORUM_MESSAGES.ERROR.ARTICLE_CREATE_FAILED)
  },
  showArticleUpdateFailed(reason) {
    errorMessage.show(reason || FORUM_MESSAGES.ERROR.ARTICLE_UPDATE_FAILED)
  },
  showArticleDeleteFailed(reason) {
    errorMessage.show(reason || FORUM_MESSAGES.ERROR.ARTICLE_DELETE_FAILED)
  },
  showArticleLoadFailed(reason) {
    errorMessage.show(reason || FORUM_MESSAGES.ERROR.ARTICLE_LOAD_FAILED)
  },
  showArticleRefreshFailed() {
    errorMessage.show(FORUM_MESSAGES.ERROR.ARTICLE_REFRESH_FAILED)
  },
  // 首页数据
  showHomeDataLoadFailed(reason) {
    errorMessage.show(reason || FORUM_MESSAGES.ERROR.HOME_DATA_LOAD_FAILED)
  },
  showBannerLoadFailed(reason) {
    errorMessage.show(reason || FORUM_MESSAGES.ERROR.BANNER_LOAD_FAILED)
  },
  showRecommendTeaLoadFailed(reason) {
    errorMessage.show(reason || FORUM_MESSAGES.ERROR.RECOMMEND_TEA_LOAD_FAILED)
  },
  showBlockSaveFailed(reason) {
    errorMessage.show(reason || FORUM_MESSAGES.ERROR.BLOCK_SAVE_FAILED)
  }
}

// 提示消息函数
export const forumPromptMessages = {
  showTitleRequired() {
    promptMessage.show(FORUM_MESSAGES.PROMPT.TITLE_REQUIRED)
  },
  showContentRequired() {
    promptMessage.show(FORUM_MESSAGES.PROMPT.CONTENT_REQUIRED)
  },
  showCategoryRequired() {
    promptMessage.show(FORUM_MESSAGES.PROMPT.CATEGORY_REQUIRED)
  },
  showCommentRequired() {
    promptMessage.show(FORUM_MESSAGES.PROMPT.COMMENT_REQUIRED)
  },
  showCommentTooLong() {
    promptMessage.show(FORUM_MESSAGES.PROMPT.COMMENT_TOO_LONG)
  },
  showImageSizeLimit() {
    promptMessage.show(FORUM_MESSAGES.PROMPT.IMAGE_SIZE_LIMIT)
  },
  showImageFormatInvalid() {
    promptMessage.show(FORUM_MESSAGES.PROMPT.IMAGE_FORMAT_INVALID)
  },
  showDeleteConfirm() {
    promptMessage.info(FORUM_MESSAGES.PROMPT.DELETE_CONFIRM)
  },
  showShareDeveloping() {
    promptMessage.info(FORUM_MESSAGES.PROMPT.SHARE_DEVELOPING)
  },
  showARDeveloping() {
    promptMessage.info(FORUM_MESSAGES.PROMPT.AR_DEVELOPING)
  },
  showFeatureDeveloping() {
    promptMessage.info(FORUM_MESSAGES.PROMPT.FEATURE_DEVELOPING)
  },
  showBannerDataError() {
    promptMessage.show(FORUM_MESSAGES.PROMPT.BANNER_DATA_ERROR)
  },
  showRecommendDataError() {
    promptMessage.show(FORUM_MESSAGES.PROMPT.RECOMMEND_DATA_ERROR)
  }
}

// 默认导出
export default {
  success: forumSuccessMessages,
  error: forumErrorMessages,
  prompt: forumPromptMessages,
  FORUM_MESSAGES
}
