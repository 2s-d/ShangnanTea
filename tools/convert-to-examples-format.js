const fs = require('fs')
const path = require('path')

/**
 * 将 openapi.yaml 转换为示例数据优先格式
 * 参考 ooo.yaml 的格式：
 * 1. 将 example 改为 examples 格式
 * 2. 使用 examples.default.value 结构
 */

const openapiPath = path.join(__dirname, '../openapi.yaml')

// 读取文件
let content = fs.readFileSync(openapiPath, 'utf-8')

// 将单数 example 转换为复数 examples 格式
// 匹配模式：example: 后面跟多行内容，直到下一个同级别或更高级别的键

const lines = content.split('\n')
const newLines = []
let i = 0

while (i < lines.length) {
  const line = lines[i]
  
  // 检测 example: 开始（单独一行）
  if (line.match(/^(\s+)example:\s*$/)) {
    const indent = line.match(/^(\s+)/)[1]
    newLines.push(`${indent}examples:`)
    newLines.push(`${indent}  default:`)
    newLines.push(`${indent}    summary: 响应示例`)
    newLines.push(`${indent}    value:`)
    
    // 读取后续的示例内容
    i++
    let exampleIndent = indent.length
    while (i < lines.length) {
      const nextLine = lines[i]
      const nextIndent = nextLine.match(/^(\s*)/)[1].length
      
      // 如果遇到空行，继续
      if (nextLine.trim() === '') {
        newLines.push('')
        i++
        continue
      }
      
      // 如果遇到同级别或更高级别的键，停止
      if (nextIndent <= exampleIndent && nextLine.match(/^\s+\w+:/)) {
        break
      }
      
      // 调整缩进（在 value 下再缩进2级）
      const content = nextLine.substring(nextIndent)
      const newIndent = indent + '      '
      newLines.push(`${newIndent}${content}`)
      i++
    }
    continue
  }
  
  // 检测 example: { 开始（单行）
  const singleLineMatch = line.match(/^(\s+)example:\s*(\{.*\})\s*$/)
  if (singleLineMatch) {
    const indent = singleLineMatch[1]
    const exampleContent = singleLineMatch[2]
    newLines.push(`${indent}examples:`)
    newLines.push(`${indent}  default:`)
    newLines.push(`${indent}    summary: 响应示例`)
    newLines.push(`${indent}    value: ${exampleContent}`)
    i++
    continue
  }
  
  // 检测 example: { 开始（多行开始）
  const multiLineStartMatch = line.match(/^(\s+)example:\s*\{/)
  if (multiLineStartMatch) {
    const indent = multiLineStartMatch[1]
    newLines.push(`${indent}examples:`)
    newLines.push(`${indent}  default:`)
    newLines.push(`${indent}    summary: 响应示例`)
    newLines.push(`${indent}    value:`)
    
    // 读取后续内容
    i++
    let exampleIndent = indent.length
    while (i < lines.length) {
      const nextLine = lines[i]
      const nextIndent = nextLine.match(/^(\s*)/)[1].length
      
      // 如果遇到空行，继续
      if (nextLine.trim() === '') {
        newLines.push('')
        i++
        continue
      }
      
      // 如果遇到同级别或更高级别的键，停止
      if (nextIndent <= exampleIndent && nextLine.match(/^\s+\w+:/)) {
        break
      }
      
      // 调整缩进
      const content = nextLine.substring(nextIndent)
      const newIndent = indent + '      '
      newLines.push(`${newIndent}${content}`)
      i++
    }
    continue
  }
  
  // 普通行
  newLines.push(line)
  i++
}

// 写回文件
const newContent = newLines.join('\n')
fs.writeFileSync(openapiPath, newContent, 'utf-8')

console.log('✅ 已将所有 example 转换为 examples 格式')
console.log(`📝 处理了 ${lines.length} 行`)
console.log(`📝 生成了 ${newLines.length} 行`)

