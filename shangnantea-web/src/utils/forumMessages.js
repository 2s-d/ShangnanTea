/**
 * 论坛模块消息工具
 * 按照三层架构设计消息：
 * 1. API层：处理网络请求相关消息
 * 2. Vuex业务层：处理业务操作结果消息
 * 3. UI交互层：处理界面交互提示消息
 */
import messageManager from './messageManager'

// 论坛模块常用消息
export const FORUM_MESSAGES = {
  // API层消息
  API: {
    LOADING_POSTS: '正在加载帖子列表...',
    LOADING_POST_DETAIL: '正在加载帖子详情...',
    LOADING_COMMENTS: '正在加载评论...',
    CREATING_POST: '正在发布帖子...',
    UPDATING_POST: '正在更新帖子...',
    DELETING_POST: '正在删除帖子...',
    CREATING_COMMENT: '正在发表评论...',
    DELETING_COMMENT: '正在删除评论...',
    LIKING_POST: '正在点赞...',
    LOADING_CATEGORIES: '正在加载分类...',
    LOADING_FAILED: '加载失败',
    CREATE_FAILED: '创建失败',
    UPDATE_FAILED: '更新失败',
    DELETE_FAILED: '删除失败',
    LIKE_FAILED: '点赞失败'
  },
  
  // 业务层消息
  BUSINESS: {
    POST_CREATED: '帖子发布成功',
    POST_UPDATED: '帖子更新成功',
    POST_DELETED: '帖子已删除',
    COMMENT_CREATED: '评论发表成功',
    COMMENT_DELETED: '评论已删除',
    POST_LIKED: '点赞成功',
    POST_CREATE_FAILED: '帖子发布失败',
    POST_UPDATE_FAILED: '帖子更新失败',
    POST_DELETE_FAILED: '帖子删除失败',
    COMMENT_CREATE_FAILED: '评论发表失败',
    COMMENT_DELETE_FAILED: '评论删除失败',
    LIKE_FAILED: '点赞失败',
    UNAUTHORIZED: '请先登录再进行操作',
    FORBIDDEN: '您没有权限执行此操作'
  },
  
  // UI交互层消息
  UI: {
    TITLE_REQUIRED: '标题不能为空',
    CONTENT_REQUIRED: '内容不能为空',
    CATEGORY_REQUIRED: '请选择分类',
    COMMENT_REQUIRED: '评论内容不能为空',
    COMMENT_TOO_LONG: '评论内容不能超过500字',
    DELETE_CONFIRM: '确定要删除吗？此操作不可撤销',
    IMAGE_SIZE_LIMIT: '图片大小不能超过2MB',
    IMAGE_FORMAT_INVALID: '只支持JPG、PNG和GIF格式的图片',
    DRAFT_SAVED: '草稿已保存',
    LOGIN_PROMPT: '请先登录再进行操作'
  }
}

// 论坛API层消息工具
const apiMessages = {
  // 显示API加载消息
  showLoading(message) {
    messageManager.api.showLoading(message)
  },
  
  // 显示API成功消息
  showSuccess(message) {
    messageManager.api.showSuccess(message)
  },
  
  // 显示API错误消息
  showError(message) {
    messageManager.api.showError(message)
  },
  
  // 预定义的API消息
  showLoadingPosts() {
    this.showLoading(FORUM_MESSAGES.API.LOADING_POSTS)
  },
  
  showLoadingPostDetail() {
    this.showLoading(FORUM_MESSAGES.API.LOADING_POST_DETAIL)
  },
  
  showLoadingComments() {
    this.showLoading(FORUM_MESSAGES.API.LOADING_COMMENTS)
  },
  
  showCreatingPost() {
    this.showLoading(FORUM_MESSAGES.API.CREATING_POST)
  },
  
  showUpdatingPost() {
    this.showLoading(FORUM_MESSAGES.API.UPDATING_POST)
  },
  
  showDeletingPost() {
    this.showLoading(FORUM_MESSAGES.API.DELETING_POST)
  },
  
  showCreatingComment() {
    this.showLoading(FORUM_MESSAGES.API.CREATING_COMMENT)
  },
  
  showDeletingComment() {
    this.showLoading(FORUM_MESSAGES.API.DELETING_COMMENT)
  },
  
  showLoadingFailed(error) {
    const errorMsg = error?.message || FORUM_MESSAGES.API.LOADING_FAILED
    this.showError(errorMsg)
  },
  
  showCreateFailed(error) {
    const errorMsg = error?.message || FORUM_MESSAGES.API.CREATE_FAILED
    this.showError(errorMsg)
  },
  
  showUpdateFailed(error) {
    const errorMsg = error?.message || FORUM_MESSAGES.API.UPDATE_FAILED
    this.showError(errorMsg)
  },
  
  showDeleteFailed(error) {
    const errorMsg = error?.message || FORUM_MESSAGES.API.DELETE_FAILED
    this.showError(errorMsg)
  },
  
  showLikeFailed(error) {
    const errorMsg = error?.message || FORUM_MESSAGES.API.LIKE_FAILED
    this.showError(errorMsg)
  }
}

// 论坛业务层消息工具
const businessMessages = {
  // 显示业务成功消息
  showSuccess(message) {
    messageManager.business.showSuccess(message)
  },
  
  // 显示业务错误消息
  showError(message) {
    messageManager.business.showError(message)
  },
  
  // 显示业务警告消息
  showWarning(message) {
    messageManager.business.showWarning(message)
  },
  
  // 预定义的业务消息
  showPostCreated() {
    this.showSuccess(FORUM_MESSAGES.BUSINESS.POST_CREATED)
  },
  
  showPostUpdated() {
    this.showSuccess(FORUM_MESSAGES.BUSINESS.POST_UPDATED)
  },
  
  showPostDeleted() {
    this.showSuccess(FORUM_MESSAGES.BUSINESS.POST_DELETED)
  },
  
  showCommentCreated() {
    this.showSuccess(FORUM_MESSAGES.BUSINESS.COMMENT_CREATED)
  },
  
  showCommentDeleted() {
    this.showSuccess(FORUM_MESSAGES.BUSINESS.COMMENT_DELETED)
  },
  
  showPostLiked() {
    this.showSuccess(FORUM_MESSAGES.BUSINESS.POST_LIKED)
  },
  
  showPostCreateFailed(error) {
    const errorMsg = error?.message || FORUM_MESSAGES.BUSINESS.POST_CREATE_FAILED
    this.showError(errorMsg)
  },
  
  showPostUpdateFailed(error) {
    const errorMsg = error?.message || FORUM_MESSAGES.BUSINESS.POST_UPDATE_FAILED
    this.showError(errorMsg)
  },
  
  showPostDeleteFailed(error) {
    const errorMsg = error?.message || FORUM_MESSAGES.BUSINESS.POST_DELETE_FAILED
    this.showError(errorMsg)
  },
  
  showCommentCreateFailed(error) {
    const errorMsg = error?.message || FORUM_MESSAGES.BUSINESS.COMMENT_CREATE_FAILED
    this.showError(errorMsg)
  },
  
  showCommentDeleteFailed(error) {
    const errorMsg = error?.message || FORUM_MESSAGES.BUSINESS.COMMENT_DELETE_FAILED
    this.showError(errorMsg)
  },
  
  showLikeFailed(error) {
    const errorMsg = error?.message || FORUM_MESSAGES.BUSINESS.LIKE_FAILED
    this.showError(errorMsg)
  },
  
  showUnauthorized() {
    this.showWarning(FORUM_MESSAGES.BUSINESS.UNAUTHORIZED)
  },
  
  showForbidden() {
    this.showWarning(FORUM_MESSAGES.BUSINESS.FORBIDDEN)
  }
}

// 论坛UI交互层消息工具
const uiMessages = {
  // 显示UI成功消息
  showSuccess(message) {
    messageManager.ui.showSuccess(message)
  },
  
  // 显示UI错误消息
  showError(message) {
    messageManager.ui.showError(message)
  },
  
  // 显示UI警告消息
  showWarning(message) {
    messageManager.ui.showWarning(message)
  },
  
  // 显示UI信息消息
  showInfo(message) {
    messageManager.ui.showInfo(message)
  },
  
  // 预定义的UI消息
  showTitleRequired() {
    this.showWarning(FORUM_MESSAGES.UI.TITLE_REQUIRED)
  },
  
  showContentRequired() {
    this.showWarning(FORUM_MESSAGES.UI.CONTENT_REQUIRED)
  },
  
  showCategoryRequired() {
    this.showWarning(FORUM_MESSAGES.UI.CATEGORY_REQUIRED)
  },
  
  showCommentRequired() {
    this.showWarning(FORUM_MESSAGES.UI.COMMENT_REQUIRED)
  },
  
  showCommentTooLong() {
    this.showWarning(FORUM_MESSAGES.UI.COMMENT_TOO_LONG)
  },
  
  showDeleteConfirm() {
    return messageManager.ui.showConfirm(FORUM_MESSAGES.UI.DELETE_CONFIRM)
  },
  
  showImageSizeLimit() {
    this.showWarning(FORUM_MESSAGES.UI.IMAGE_SIZE_LIMIT)
  },
  
  showImageFormatInvalid() {
    this.showWarning(FORUM_MESSAGES.UI.IMAGE_FORMAT_INVALID)
  },
  
  showDraftSaved() {
    this.showInfo(FORUM_MESSAGES.UI.DRAFT_SAVED)
  },
  
  showLoginPrompt() {
    this.showWarning(FORUM_MESSAGES.UI.LOGIN_PROMPT)
  }
}

// 导出所有消息工具
export default {
  api: apiMessages,
  business: businessMessages,
  ui: uiMessages,
  FORUM_MESSAGES
} 