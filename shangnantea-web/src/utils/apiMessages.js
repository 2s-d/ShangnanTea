/**
 * API消息工具 - 基于状态码的消息映射
 * 
 * ⚠️ 重要：本文件必须与 docs/code-message-mapping.md 保持完全同步！
 * 
 * 功能：
 * - 根据状态码获取对应消息
 * - 自动显示成功/错误消息（静默码除外）
 * - 判断响应是否成功
 * 
 * 状态码规则：
 * - 200: HTTP成功（查询类静默，不显示消息）
 * - x0xx: 业务成功码（部分静默，部分显示）
 * - x1xx: 业务失败码（全部显示）
 * 
 * 同步维护要求：
 * 1. CODE_MAP 必须包含 code-message-mapping.md 中列出的所有状态码
 * 2. SILENT_CODES 必须包含 code-message-mapping.md 中所有标注 [静默] 的状态码
 * 3. 修改本文档时，必须同步更新 code-message-mapping.md
 * 4. 修改 code-message-mapping.md 时，必须同步更新本文档
 * 
 * 数据来源：docs/code-message-mapping.md
 * 最后更新: 2026-01-13
 */

import { apiMessage } from './messageManager'

// ============================================================================
// ⚠️ 同步维护区域：以下数据必须与 docs/code-message-mapping.md 完全一致
// ============================================================================

/**
 * 静默状态码列表
 * 
 * ⚠️ 重要：此列表必须与 code-message-mapping.md 中所有标注 [静默] 的状态码完全一致
 * 
 * 静默码规则：
 * - 查询类接口的成功码（如 200、3000、3001）
 * - 后台自动操作（如 refreshToken 的 200）
 * - 轻量级操作（如取消点赞、取消收藏）
 * 
 * 维护流程：
 * 1. 在 code-message-mapping.md 中标注 [静默]
 * 2. 在此列表中添加对应状态码
 * 3. 验证一致性
 */
const SILENT_CODES = [
  // TODO: 从 code-message-mapping.md 中提取所有 [静默] 标注的状态码
  // 示例：
  // 200,    // HTTP成功（查询类）
  // 3000,   // 茶叶列表加载成功
  // 3001,   // 茶叶详情加载成功
  // ... 其他静默码
]

/**
 * 状态码消息映射表
 * 
 * ⚠️ 重要：此映射表必须包含 code-message-mapping.md 中列出的所有状态码
 * 
 * 数据来源：从 code-message-mapping.md 中提取所有状态码和消息
 * 去重规则：同一状态码在不同接口中出现时，只保留一个消息（通常消息是一致的）
 * 
 * 维护流程：
 * 1. 在 code-message-mapping.md 中添加或修改状态码映射
 * 2. 在此映射表中同步更新
 * 3. 验证所有状态码都已正确映射
 */
export const CODE_MAP = {
  // TODO: 从 code-message-mapping.md 中提取所有状态码和消息
  // 示例：
  // HTTP状态码
  // 200: '操作成功',
  // 400: '请求错误',
  // ...
  
  // 通用模块 (1xxx)
  // ...
  
  // 用户模块 (2xxx)
  // ...
  
  // 其他模块...
}

// ============================================================================
// 同步维护区域结束
// ============================================================================

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
