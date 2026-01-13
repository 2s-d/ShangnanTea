const fs = require('fs')
const path = require('path')

/**
 * 修复 examples 格式的缩进问题
 */

const openapiPath = path.join(__dirname, '../openapi.yaml')

// 读取文件
let content = fs.readFileSync(openapiPath, 'utf-8')

// 修复缩进问题
// 问题：value 下的内容缩进不正确，应该是在 value 下再缩进2级

const lines = content.split('\n')
const newLines = []
let i = 0
let inValueBlock = false
let valueIndent = 0

while (i < lines.length) {
  const line = lines[i]
  
  // 检测 value: 开始
  if (line.match(/^(\s+)value:\s*$/)) {
    inValueBlock = true
    valueIndent = line.match(/^(\s+)/)[1].length
    newLines.push(line)
    i++
    continue
  }
  
  // 在 value 块中
  if (inValueBlock) {
    const currentIndent = line.match(/^(\s*)/)[1].length
    
    // 检查是否结束（遇到同级别或更高级别的键，但不是 value 下的内容）
    if (line.trim() === '') {
      // 空行保持
      newLines.push('')
      i++
      continue
    }
    
    // 如果遇到同级别或更高级别的键（如 examples, schema, content 等），结束 value 块
    if (currentIndent <= valueIndent && line.match(/^\s+\w+:/)) {
      inValueBlock = false
      newLines.push(line)
      i++
      continue
    }
    
    // value 下的内容应该缩进到 value 下2级
    // 如果当前缩进小于等于 value 的缩进+2，需要调整
    const expectedIndent = valueIndent + 2
    const content = line.substring(currentIndent)
    
    // 如果缩进正确，直接添加
    if (currentIndent >= expectedIndent) {
      newLines.push(line)
    } else {
      // 调整缩进
      const newIndent = ' '.repeat(expectedIndent)
      newLines.push(`${newIndent}${content}`)
    }
    i++
    continue
  }
  
  // 普通行
  newLines.push(line)
  i++
}

// 写回文件
const newContent = newLines.join('\n')
fs.writeFileSync(openapiPath, newContent, 'utf-8')

console.log('✅ 已修复 examples 格式的缩进问题')

