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

// 先检查格式问题：查找是否有未转换的 example:
const hasOldExample = content.match(/^\s+example:\s*$/m)
if (hasOldExample) {
  console.log('⚠️  发现未转换的 example:，先修复格式问题')
  // 这里可以调用之前的转换脚本
}

// 检查 YAML 基本格式
let braceCount = 0
let bracketCount = 0
let inString = false
let stringChar = ''

for (let i = 0; i < content.length; i++) {
  const char = content[i]
  const prevChar = i > 0 ? content[i - 1] : ''
  
  if (!inString && (char === '"' || char === "'")) {
    inString = true
    stringChar = char
  } else if (inString && char === stringChar && prevChar !== '\\') {
    inString = false
  } else if (!inString) {
    if (char === '{') braceCount++
    if (char === '}') braceCount--
    if (char === '[') bracketCount++
    if (char === ']') bracketCount--
  }
}

if (braceCount !== 0 || bracketCount !== 0) {
  console.log(`⚠️  发现格式问题: 大括号不平衡 (${braceCount}), 方括号不平衡 (${bracketCount})`)
}

console.log('✅ 格式检查完成')
console.log('📝 开始重构为 schema 引用格式...')

// 这里需要更复杂的逻辑来提取和重构
// 暂时先输出检查结果
console.log('⚠️  需要手动重构，因为涉及复杂的 schema 提取和引用')

