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
  // 将从文档中提取，待实现
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

  // ========== 通用模块 (1xxx) ==========
  // 将从文档中提取，待实现
  
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
