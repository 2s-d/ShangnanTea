/**
 * 检查 openapi.yaml 中缺少 code 字段的接口
 */

const fs = require('fs')
const path = require('path')

function checkMissingCodes(filePath) {
  const content = fs.readFileSync(filePath, 'utf-8')
  const lines = content.split('\n')
  
  const methods = []
  const codes = []
  
  let currentPath = null
  let currentMethod = null
  let inResponseExample = false
  let hasCode = false
  
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
      currentMethod = methodMatch[1].toUpperCase()
      hasCode = false
      inResponseExample = false
    }
    
    // 检测响应示例开始
    if (line.includes('example:')) {
      inResponseExample = true
    }
    
    // 检测code字段
    if (inResponseExample && line.includes('code:')) {
      hasCode = true
      codes.push({
        path: currentPath,
        method: currentMethod,
        line: i + 1
      })
    }
    
    // 检测响应示例结束（下一个HTTP方法或路径）
    if (inResponseExample && (methodMatch || pathMatch)) {
      if (currentMethod && !hasCode) {
        methods.push({
          path: currentPath,
          method: currentMethod,
          line: i + 1
        })
      }
      inResponseExample = false
      hasCode = false
    }
    
    // 检测文件结束
    if (i === lines.length - 1 && currentMethod && !hasCode) {
      methods.push({
        path: currentPath,
        method: currentMethod,
        line: i + 1
      })
    }
  }
  
  return { methods, codes }
}

// 主程序
const openapiPath = path.join(__dirname, '..', 'openapi.yaml')
const { methods, codes } = checkMissingCodes(openapiPath)

console.log(`总HTTP方法数: ${methods.length + codes.length}`)
console.log(`有code字段的接口: ${codes.length}`)
console.log(`缺少code字段的接口: ${methods.length}`)

if (methods.length > 0) {
  console.log('\n缺少code字段的接口:')
  methods.forEach(m => {
    console.log(`  ${m.method} ${m.path} (行 ${m.line})`)
  })
} else {
  console.log('\n✅ 所有接口都有code字段')
}

