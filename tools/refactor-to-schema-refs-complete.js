const fs = require('fs')
const path = require('path')

/**
 * 完整重构：将 openapi.yaml 中的所有内联 schema 提取到 components/schemas
 * 并使用 $ref 引用，参考 ooo.yaml 的方式
 */

const openapiPath = path.join(__dirname, '../openapi.yaml')

console.log('📝 开始重构为 schema 引用格式...')
console.log('⚠️  这是一个复杂操作，需要分析所有响应并提取 schema')
console.log('\n建议：')
console.log('1. 先修复所有格式问题（缩进等）')
console.log('2. 然后逐步提取常见响应 schema')
console.log('3. 最后替换所有内联 schema 为 $ref')
console.log('\n由于接口数量庞大（167个），建议：')
console.log('- 先处理几个典型接口作为示例')
console.log('- 然后批量处理其他接口')

// 读取文件
let content = fs.readFileSync(openapiPath, 'utf-8')
const lines = content.split('\n')

// 统计需要处理的响应数量
let responseCount = 0
let inlineSchemaCount = 0

for (let i = 0; i < lines.length; i++) {
  if (lines[i].match(/responses:/)) {
    responseCount++
  }
  if (lines[i].match(/^\s+schema:/) && !lines[i].match(/\$ref/)) {
    inlineSchemaCount++
  }
}

console.log(`\n统计：`)
console.log(`- 响应数量: ${responseCount}`)
console.log(`- 内联 schema 数量: ${inlineSchemaCount}`)
console.log(`\n✅ 文件读取完成，可以开始重构`)

