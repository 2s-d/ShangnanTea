const fs = require('fs')
const path = require('path')

/**
 * 将 openapi.yaml 重构为使用 schema 引用
 * 参考 ooo.yaml 的方式：
 * 1. 提取所有响应 schema 到 components/schemas
 * 2. 使用 $ref 引用
 * 3. 保持 examples 格式
 */

const openapiPath = path.join(__dirname, '../openapi.yaml')

// 读取文件
let content = fs.readFileSync(openapiPath, 'utf-8')
const lines = content.split('\n')

// 找到 components/schemas 的位置
let componentsStartLine = -1
let schemasStartLine = -1
let pathsStartLine = -1

for (let i = 0; i < lines.length; i++) {
  if (lines[i].match(/^components:/)) {
    componentsStartLine = i
  }
  if (lines[i].match(/^\s+schemas:/)) {
    schemasStartLine = i
  }
  if (lines[i].match(/^paths:/)) {
    pathsStartLine = i
  }
}

console.log(`components 开始行: ${componentsStartLine + 1}`)
console.log(`schemas 开始行: ${schemasStartLine + 1}`)
console.log(`paths 开始行: ${pathsStartLine + 1}`)

// 读取现有的 schemas
const existingSchemas = []
if (schemasStartLine >= 0) {
  let i = schemasStartLine + 1
  let indent = lines[schemasStartLine].match(/^(\s+)/)?.[1]?.length || 0
  
  while (i < lines.length && i < pathsStartLine) {
    const line = lines[i]
    const lineIndent = line.match(/^(\s*)/)?.[1]?.length || 0
    
    if (lineIndent <= indent && line.trim() && !line.trim().startsWith('#')) {
      break
    }
    
    existingSchemas.push(line)
    i++
  }
}

console.log(`\n现有 schemas 行数: ${existingSchemas.length}`)
console.log('\n⚠️  这是一个复杂操作，需要：')
console.log('  1. 分析所有响应 schema')
console.log('  2. 提取到 components/schemas')
console.log('  3. 替换为 $ref 引用')
console.log('\n建议分步骤进行，先修复格式问题')

