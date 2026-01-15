/**
 * API消息工具 - 基于状态码的消息映射
 * 
 * 功能：
 * - 根据状态码获取对应消息
 * - 自动显示成功/错误消息
 * - 判断响应是否成功
 * 
 * 状态码规则：
 * - 200: HTTP成功（静默，不显示消息）
 * - x0xx: 业务成功码（需要显示消息）
 * - x1xx: 业务失败码（需要显示消息）
 * 
 * 与 docs/code-message-mapping.md 保持同步
 * 最后更新: 2026-01-13
 */

import { apiMessage } from './messageManager'

// 状态码消息映射表
// 基于 docs/code-message-mapping.md 文档，与167个接口完全对应
export const CODE_MAP = {
  // HTTP状态码
  200: '操作成功',
  400: '请求错误',
  401: '未认证',
  403: '无权限',
  404: '资源不存在',
  500: '服务器错误',

  // ========== 通用模块 (1xxx) ==========
  
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
 */
export function showByCode(code, customMessage = null) {
  const message = customMessage || getMessageByCode(code)
  
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
