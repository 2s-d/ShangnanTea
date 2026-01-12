/**
 * 时间格式化工具
 * 提供各种日期时间格式化函数
 */

/**
 * 格式化时间为友好显示格式
 * @param {number|string|Date} timestamp - 时间戳或日期字符串
 * @returns {string} 格式化后的时间字符串
 */
export const timeFormat = timestamp => {
  if (!timestamp) return ''
  
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now - date
  
  // 今天内的消息显示时:分
  if (diff < 86400000 && date.getDate() === now.getDate()) {
    return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
  }
  
  // 昨天的消息
  if (diff < 2 * 86400000 && date.getDate() === now.getDate() - 1) {
    return '昨天'
  }
  
  // 一周内的消息显示周几
  if (diff < 7 * 86400000) {
    const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
    return weekdays[date.getDay()]
  }
  
  // 更早的消息显示年-月-日
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
}

/**
 * 格式化为完整日期时间
 * @param {number|string|Date} timestamp - 时间戳或日期字符串
 * @returns {string} 格式化后的完整日期时间字符串 (YYYY-MM-DD HH:mm:ss)
 */
export const fullTimeFormat = timestamp => {
  if (!timestamp) return ''
  
  const date = new Date(timestamp)
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}:${date.getSeconds().toString().padStart(2, '0')}`
}

/**
 * 格式化为日期
 * @param {number|string|Date} timestamp - 时间戳或日期字符串
 * @returns {string} 格式化后的日期字符串 (YYYY-MM-DD)
 */
export const dateFormat = timestamp => {
  if (!timestamp) return ''
  
  const date = new Date(timestamp)
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
}

/**
 * 相对时间格式化（如：刚刚、5分钟前、1小时前等）
 * @param {number|string|Date} timestamp - 时间戳或日期字符串
 * @returns {string} 相对时间描述
 */
export const relativeTimeFormat = timestamp => {
  if (!timestamp) return ''
  
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now - date
  
  // 小于1分钟
  if (diff < 60000) {
    return '刚刚'
  }
  
  // 小于1小时
  if (diff < 3600000) {
    return `${Math.floor(diff / 60000)}分钟前`
  }
  
  // 小于1天
  if (diff < 86400000) {
    return `${Math.floor(diff / 3600000)}小时前`
  }
  
  // 小于30天
  if (diff < 30 * 86400000) {
    return `${Math.floor(diff / 86400000)}天前`
  }
  
  // 小于12个月
  if (diff < 12 * 30 * 86400000) {
    return `${Math.floor(diff / (30 * 86400000))}个月前`
  }
  
  // 超过1年
  return `${Math.floor(diff / (365 * 86400000))}年前`
} 