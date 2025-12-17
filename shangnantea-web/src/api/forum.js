import request from './index'

// forum.js - 合并了home.js的功能，保留基本结构以便未来实现

// API 路径常量
const API = {
  // 论坛相关API - 保留以供未来参考
  TOPICS: '/forum/topics',
  TOPIC_DETAIL: '/forum/topics/{id}',
  POSTS: '/forum/posts',
  POST_DETAIL: '/forum/posts/{id}',
  REPLY: '/forum/posts/{id}/reply',
  LIKE: '/forum/posts/{id}/like',
  
  // 首页相关API
  HOME_DATA: '/api/home',
  UPDATE_HOME: '/api/admin/home'
}

/**
 * 获取首页数据
 * @returns {Promise} 首页数据
 */
export function getHomeData() {
  return request({
    url: API.HOME_DATA,
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
    url: API.UPDATE_HOME,
    method: 'post',
    data
  })
}

// 论坛相关功能API已清空，将在未来重新实现
// 暂时保留API文件框架以备后续开发

export default {
  API,
  getHomeData,
  updateHomeData
} 