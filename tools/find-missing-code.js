/**
 * 找出缺少 code 字段的接口
 */

const fs = require('fs')
const path = require('path')

function findMissingCode(filePath) {
  const content = fs.readFileSync(filePath, 'utf-8')
  const lines = content.split('\n')
  
  const interfaces = []
  let currentPath = null
  let currentMethod = null
  let methodLine = null
  let inResponseExample = false
  let foundCode = false
  
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i]
    
    // 检测路径
    const pathMatch = line.match(/^\s*(\/[^:]+):/)
    if (pathMatch) {
      currentPath = pathMatch[1]
    }
    
    // 检测HTTP方法
    const methodMatch = line.match(/^\s+(get|post|put|delete|patch):\s*$/i)
    if (methodMatch) {
      // 如果上一个接口没有找到code，记录下来
      if (currentMethod && !foundCode && methodLine !== null) {
        interfaces.push({
          path: currentPath,
          method: currentMethod,
          line: methodLine
        })
      }
      
      currentMethod = methodMatch[1].toUpperCase()
      methodLine = i + 1
      foundCode = false
      inResponseExample = false
    }
    
    // 检测响应示例开始
    if (line.includes('example:')) {
      inResponseExample = true
    }
    
    // 检测code字段
    if (inResponseExample && line.match(/^\s+code:\s*\d+/)) {
      foundCode = true
    }
    
    // 检测响应示例结束（遇到下一个HTTP方法或路径）
    if (inResponseExample && (methodMatch || pathMatch)) {
      if (currentMethod && !foundCode && methodLine !== null) {
        interfaces.push({
          path: currentPath,
          method: currentMethod,
          line: methodLine
        })
      }
      inResponseExample = false
      foundCode = false
    }
  }
  
  // 检查最后一个接口
  if (currentMethod && !foundCode && methodLine !== null) {
    interfaces.push({
      path: currentPath,
      method: currentMethod,
      line: methodLine
    })
  }
  
  return interfaces
}

// 主程序
const openapiPath = path.join(__dirname, '..', 'openapi.yaml')
const missing = findMissingCode(openapiPath)

if (missing.length > 0) {
  console.log(`找到 ${missing.length} 个缺少 code 字段的接口:`)
  missing.forEach(m => {
    console.log(`  ${m.method} ${m.path} (行 ${m.line})`)
  })
} else {
  console.log('✅ 所有接口都有 code 字段')
}

