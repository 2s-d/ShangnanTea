/**
 * 格式化日期
 * @param {string|Date} date 日期对象或日期字符串
 * @param {string} format 格式模板，默认为 'YYYY-MM-DD'
 * @returns {string} 格式化后的日期字符串
 */
export function formatDate(date, format = 'YYYY-MM-DD') {
  if (!date) return ''
  
  const d = typeof date === 'string' ? new Date(date) : date
  
  if (!(d instanceof Date) || isNaN(d)) {
    return ''
  }
  
  const year = d.getFullYear()
  const month = d.getMonth() + 1
  const day = d.getDate()
  const hour = d.getHours()
  const minute = d.getMinutes()
  const second = d.getSeconds()
  
  const pad = num => (num < 10 ? '0' + num : num)
  
  return format
    .replace(/YYYY/g, year)
    .replace(/MM/g, pad(month))
    .replace(/DD/g, pad(day))
    .replace(/HH/g, pad(hour))
    .replace(/mm/g, pad(minute))
    .replace(/ss/g, pad(second))
}

/**
 * 计算相对时间 (如：3小时前，2天前)
 * @param {string|Date} date 日期对象或日期字符串
 * @returns {string} 相对时间字符串
 */
export function relativeTime(date) {
  if (!date) return ''
  
  const d = typeof date === 'string' ? new Date(date) : date
  
  if (!(d instanceof Date) || isNaN(d)) {
    return ''
  }
  
  const now = new Date()
  const diff = now.getTime() - d.getTime()
  
  // 换算时间
  const seconds = Math.floor(diff / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)
  const months = Math.floor(days / 30)
  const years = Math.floor(months / 12)
  
  if (years > 0) return `${years}年前`
  if (months > 0) return `${months}个月前`
  if (days > 0) return `${days}天前`
  if (hours > 0) return `${hours}小时前`
  if (minutes > 0) return `${minutes}分钟前`
  if (seconds > 0) return `${seconds}秒前`
  
  return '刚刚'
} 