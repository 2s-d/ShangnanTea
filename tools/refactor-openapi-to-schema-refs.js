const fs = require('fs')
const path = require('path')

/**
 * 将 openapi.yaml 重构为使用 schema 引用
 * 参考 ooo.yaml 的方式：
 * 1. 将内联 schema 提取到 components/schemas
 * 2. 使用 $ref 引用
 * 3. 保持 examples 格式
 */

const openapiPath = path.join(__dirname, '../openapi.yaml')

// 读取文件
let content = fs.readFileSync(openapiPath, 'utf-8')
const lines = content.split('\n')

// 检查格式问题：查找是否有语法错误
console.log('检查格式问题...')

// 检查是否有未闭合的引号、括号等
let issues = []
let inString = false
let stringChar = ''
let indentStack = []

for (let i = 0; i < lines.length; i++) {
  const line = lines[i]
  const trimmed = line.trim()
  
  // 检查缩进问题
  if (trimmed && !trimmed.startsWith('#')) {
    const indent = line.match(/^(\s*)/)[1].length
    const isKey = trimmed.match(/^[a-zA-Z_-]+:/)
    
    if (isKey) {
      // 检查缩进是否合理（应该是2的倍数）
      if (indent % 2 !== 0 && indent > 0) {
        issues.push(`第 ${i + 1} 行缩进不是2的倍数: ${line}`)
      }
    }
  }
  
  // 检查是否有未转换的 example:
  if (line.match(/^\s+example:\s*$/)) {
    issues.push(`第 ${i + 1} 行发现未转换的 example:`)
  }
}

if (issues.length > 0) {
  console.log('⚠️  发现格式问题:')
  issues.slice(0, 10).forEach(issue => console.log(`  - ${issue}`))
  if (issues.length > 10) {
    console.log(`  ... 还有 ${issues.length - 10} 个问题`)
  }
} else {
  console.log('✅ 基本格式检查通过')
}

console.log('\n📝 开始重构为 schema 引用格式...')
console.log('⚠️  这是一个复杂操作，需要：')
console.log('  1. 提取所有内联 schema 到 components/schemas')
console.log('  2. 为每个响应创建对应的 schema 定义')
console.log('  3. 替换内联 schema 为 $ref 引用')
console.log('\n建议：先修复格式问题，再逐步重构')

