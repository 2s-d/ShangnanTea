/**
 * 更新 openapi.yaml 文件
 * 1. 移除所有响应中的 message 字段
 * 2. 根据状态码映射文档更新所有接口的 code 字段
 */

const fs = require('fs')
const path = require('path')

// 路径到状态码的映射表（根据 code-message-mapping.md）
const PATH_TO_CODE_MAP = {
  // 用户模块
  'POST /user/login': 2000,
  'POST /user/register': 2001,
  'POST /user/logout': 2002,
  'GET /user/me': 200,
  'POST /user/refresh': 200,
  'GET /user/{userId}': 200,
  'PUT /user': 2010,
  'POST /user/avatar': 2012,
  'PUT /user/password': 2011,
  'POST /user/password/reset': 2004,
  'GET /user/addresses': 200,
  'POST /user/addresses': 4060,
  'PUT /user/addresses/{id}': 1004,
  'DELETE /user/addresses/{id}': 1003,
  'PUT /user/addresses/{id}/default': 1004,
  'GET /user/shop-certification': 200,
  'POST /user/shop-certification': 1000,
  'GET /user/follows': 200,
  'POST /user/follows': 5000,
  'DELETE /user/follows/{id}': 5001,
  'GET /user/favorites': 200,
  'POST /user/favorites': 3010, // 默认茶叶收藏
  'DELETE /user/favorites/{id}': 3011,
  'POST /user/likes': 6010,
  'DELETE /user/likes/{id}': 6011,
  'GET /user/preferences': 200,
  'PUT /user/preferences': 2013, // 文档建议的状态码
  'GET /user/admin/users': 200,
  'POST /user/admin/users': 2023,
  'PUT /user/admin/users/{userId}': 2022,
  'DELETE /user/admin/users/{userId}': 2020,
  'PUT /user/admin/users/{userId}/role': 2022, // 已废弃
  'PUT /user/admin/users/{userId}/status': 2021,
  'GET /user/admin/certifications': 200,
  'PUT /user/admin/certifications/{id}': 1000,

  // 茶叶模块
  'GET /tea/list': 3000,
  'POST /tea/list': 3026,
  'GET /tea/{id}': 3001,
  'PUT /tea/{id}': 3025,
  'DELETE /tea/{id}': 3024,
  'GET /tea/categories': 200,
  'POST /tea/categories': 3030,
  'PUT /tea/categories/{id}': 3031,
  'DELETE /tea/categories/{id}': 3032,
  'GET /tea/{teaId}/reviews': 200,
  'GET /tea/{teaId}/reviews/stats': 200,
  'POST /tea/reviews': 4050,
  'POST /tea/reviews/{reviewId}/reply': 3013,
  'POST /tea/reviews/{reviewId}/like': 6010,
  'GET /tea/{teaId}/specifications': 200,
  'POST /tea/{teaId}/specifications': 1000,
  'PUT /tea/specifications/{specId}': 1004,
  'DELETE /tea/specifications/{specId}': 1003,
  'PUT /tea/specifications/{specId}/default': 1004,
  'POST /tea/{teaId}/images': 1001,
  'DELETE /tea/images/{imageId}': 1003,
  'PUT /tea/images/{imageId}/main': 1004,
  'PUT /tea/images/order': 1004,
  'PUT /tea/{teaId}/status': 3020, // 默认上架成功
  'PUT /tea/batch-status': 3022, // 默认批量上架成功
  'GET /tea/recommend': 200,

  // 店铺模块
  'GET /shop/list': 200,
  'POST /shop/list': 1000,
  'GET /shop/{id}': 200,
  'PUT /shop/{id}': 1004,
  'GET /shop/my': 200,
  'GET /shop/{shopId}/teas': 200,
  'POST /shop/{shopId}/teas': 3026,
  'PUT /shop/teas/{teaId}': 3025,
  'DELETE /shop/teas/{teaId}': 3024,
  'PUT /shop/teas/{teaId}/status': 3020, // 默认上架成功
  'GET /shop/{shopId}/statistics': 200,
  'POST /shop/{shopId}/logo': 5030,
  'GET /shop/{shopId}/banners': 200,
  'POST /shop/{shopId}/banners': 5010,
  'PUT /shop/banners/{bannerId}': 5011,
  'DELETE /shop/banners/{bannerId}': 5012,
  'PUT /shop/banners/order': 5013,
  'GET /shop/{shopId}/announcements': 200,
  'POST /shop/{shopId}/announcements': 5020,
  'PUT /shop/announcements/{announcementId}': 5021,
  'DELETE /shop/announcements/{announcementId}': 5022,
  'POST /shop/{shopId}/follow': 5000,
  'DELETE /shop/{shopId}/follow': 5001,
  'GET /shop/{shopId}/follow-status': 200,
  'GET /shop/{shopId}/reviews': 200,
  'POST /shop/{shopId}/reviews': 5002,

  // 订单模块
  'GET /order/cart': 200,
  'POST /order/cart/add': 4010,
  'PUT /order/cart/update': 4011,
  'DELETE /order/cart/remove': 4013,
  'DELETE /order/cart/clear': 4015,
  'POST /order/create': 4000,
  'GET /order/list': 200,
  'GET /order/{id}': 200,
  'POST /order/pay': 4020,
  'POST /order/cancel': 4002,
  'POST /order/confirm': 4003,
  'POST /order/review': 4050,
  'POST /order/{id}/ship': 4004,
  'POST /order/batch-ship': 4006,
  'GET /order/{id}/logistics': 200,
  'POST /order/refund': 4030,
  'GET /order/{id}/refund': 200,
  'POST /order/{id}/refund/process': 4031, // 默认同意退款
  'GET /order/statistics': 200,
  'GET /order/export': 200,

  // 论坛模块
  'GET /forum/home': 200,
  'PUT /forum/home': 6060,
  'GET /forum/banners': 200,
  'POST /forum/banners': 5010,
  'PUT /forum/banners/{id}': 5011,
  'DELETE /forum/banners/{id}': 5012,
  'PUT /forum/banners/order': 5013,
  'GET /forum/articles': 200,
  'POST /forum/articles': 6050,
  'GET /forum/articles/{id}': 200,
  'PUT /forum/articles/{id}': 6051,
  'DELETE /forum/articles/{id}': 6052,
  'GET /forum/topics': 200,
  'POST /forum/topics': 6040,
  'GET /forum/topics/{id}': 200,
  'PUT /forum/topics/{id}': 6041,
  'DELETE /forum/topics/{id}': 6042,
  'GET /forum/posts': 200,
  'POST /forum/posts': 6000,
  'GET /forum/posts/pending': 200,
  'GET /forum/posts/{id}': 200,
  'PUT /forum/posts/{id}': 6001,
  'DELETE /forum/posts/{id}': 6002,
  'GET /forum/posts/{id}/replies': 200,
  'POST /forum/posts/{id}/replies': 6022,
  'DELETE /forum/replies/{id}': 6021,
  'POST /forum/replies/{id}/like': 6010,
  'DELETE /forum/replies/{id}/like': 6011,
  'POST /forum/posts/{id}/like': 6010,
  'DELETE /forum/posts/{id}/like': 6011,
  'POST /forum/posts/{id}/favorite': 6012,
  'DELETE /forum/posts/{id}/favorite': 6013,
  'POST /forum/posts/{id}/approve': 6034,
  'POST /forum/posts/{id}/reject': 6035,
  'PUT /forum/posts/{id}/sticky': 6030, // 默认置顶
  'PUT /forum/posts/{id}/essence': 6032, // 默认加精

  // 消息模块
  'GET /message/list': 200,
  'GET /message/{id}': 200,
  'POST /message/send': 7000,
  'POST /message/read': 7010,
  'POST /message/delete': 7012,
  'GET /message/unread-count': 200,
  'GET /message/notifications': 200,
  'GET /message/notifications/{id}': 200,
  'DELETE /message/notifications/{id}': 7012,
  'PUT /message/notifications/batch-read': 7011,
  'DELETE /message/notifications/batch': 7013,
  'GET /message/list/sessions': 200,
  'GET /message/list/history': 200,
  'POST /message/sessions': 1000,
  'DELETE /message/sessions/{sessionId}': 7001,
  'PUT /message/sessions/{sessionId}/pin': 7014,
  'POST /message/messages/image': 7003,
  'GET /message/user/{userId}': 200,
  'GET /message/user/{userId}/dynamic': 200,
  'GET /message/user/{userId}/statistics': 200,
  'GET /message/user/posts': 200,
  'GET /message/user/reviews': 200,

  // 公共模块
  'POST /upload': 1001,
  'POST /upload/image': 1001
}

/**
 * 从路径和方法构建键
 */
function buildKey(method, path) {
  // 标准化路径，移除路径参数的具体值
  const normalizedPath = path.replace(/\{[^}]+\}/g, (match) => {
    // 保持路径参数的占位符格式
    return match
  })
  return `${method.toUpperCase()} ${normalizedPath}`
}

/**
 * 获取状态码
 */
function getStatusCode(method, path) {
  const key = buildKey(method, path)
  return PATH_TO_CODE_MAP[key] || 200 // 默认200
}

/**
 * 处理响应示例
 */
function processResponseExample(content, method, path) {
  const statusCode = getStatusCode(method, path)
  
  // 移除 message 字段
  content = content.replace(/^\s*message:\s*"[^"]*"\s*$/gm, '')
  content = content.replace(/^\s*message:\s*[^\n]*$/gm, '')
  
  // 更新 code 字段
  content = content.replace(/code:\s*\d+/g, `code: ${statusCode}`)
  
  // 清理多余的空行（连续两个空行变为一个）
  content = content.replace(/\n\n\n+/g, '\n\n')
  
  return content
}

/**
 * 处理整个文件
 */
function processFile(filePath) {
  console.log('开始处理 openapi.yaml 文件...')
  
  let content = fs.readFileSync(filePath, 'utf-8')
  const lines = content.split('\n')
  const newLines = []
  
  let currentMethod = null
  let currentPath = null
  
  for (let i = 0; i < lines.length; i++) {
    let line = lines[i]
    
    // 检测路径定义
    const pathMatch = line.match(/^\s*(\/[^:]+):/)
    if (pathMatch) {
      currentPath = pathMatch[1]
      currentMethod = null // 重置方法
    }
    
    // 检测HTTP方法
    const methodMatch = line.match(/^\s+(get|post|put|delete|patch):\s*$/i)
    if (methodMatch) {
      currentMethod = methodMatch[1].toUpperCase()
    }
    
    // 移除 message 字段
    if (line.includes('message:')) {
      continue // 跳过这一行
    }
    
    // 更新 code 字段（在响应示例中）
    if (line.includes('code:') && currentMethod && currentPath) {
      const statusCode = getStatusCode(currentMethod, currentPath)
      line = line.replace(/code:\s*\d+/, `code: ${statusCode}`)
      console.log(`  ${currentMethod} ${currentPath} -> code: ${statusCode}`)
    }
    
    newLines.push(line)
  }
  
  let finalContent = newLines.join('\n')
  
  // 再次全局处理，确保所有 message 字段都被移除
  finalContent = finalContent.replace(/^\s*message:\s*"[^"]*"[\s,]*$/gm, '')
  finalContent = finalContent.replace(/^\s*message:\s*[^\n]*[\s,]*$/gm, '')
  finalContent = finalContent.replace(/,\s*message:\s*"[^"]*"/g, '')
  finalContent = finalContent.replace(/,\s*message:\s*[^\n,]*/g, '')
  
  // 清理多余的空行
  finalContent = finalContent.replace(/\n\n\n+/g, '\n\n')
  
  // 清理末尾的逗号
  finalContent = finalContent.replace(/,\s*\n\s*}/g, '\n  }')
  finalContent = finalContent.replace(/,\s*\n\s*]/g, '\n  ]')
  
  return finalContent
}

// 主程序
function main() {
  const openapiPath = path.join(__dirname, '..', 'openapi.yaml')
  
  if (!fs.existsSync(openapiPath)) {
    console.error(`文件不存在: ${openapiPath}`)
    process.exit(1)
  }
  
  console.log('读取 openapi.yaml 文件...')
  const originalContent = fs.readFileSync(openapiPath, 'utf-8')
  
  console.log('处理文件内容...')
  const processedContent = processFile(openapiPath)
  
  // 备份原文件
  const backupPath = openapiPath + '.backup'
  fs.writeFileSync(backupPath, originalContent)
  console.log(`已创建备份文件: ${backupPath}`)
  
  // 写入处理后的内容
  fs.writeFileSync(openapiPath, processedContent)
  console.log('✅ openapi.yaml 文件已更新')
  console.log('  - 已移除所有 message 字段')
  console.log('  - 已更新所有 code 字段为对应的成功状态码')
}

main()

