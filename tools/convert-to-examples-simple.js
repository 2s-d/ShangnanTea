const fs = require('fs')
const path = require('path')

/**
 * 将 openapi.yaml 中的 example 转换为 examples 格式
 * 简单直接的方法：找到 example: 行，替换为 examples 格式
 */

const openapiPath = path.join(__dirname, '../openapi.yaml')

// 读取文件
let content = fs.readFileSync(openapiPath, 'utf-8')

// 使用正则表达式精确匹配和替换
// 匹配模式：example: 后面跟多行内容（直到下一个同级别键）

const lines = content.split('\n')
const newLines = []
let i = 0

while (i < lines.length) {
  const line = lines[i]
  
  // 检测 example: 开始（单独一行）
  const exampleMatch = line.match(/^(\s+)example:\s*$/)
  if (exampleMatch) {
    const indent = exampleMatch[1]
    newLines.push(`${indent}examples:`)
    newLines.push(`${indent}  default:`)
    newLines.push(`${indent}    summary: 响应示例`)
    newLines.push(`${indent}    value:`)
    
    // 读取后续的示例内容
    i++
    const exampleIndent = indent.length
    
    while (i < lines.length) {
      const nextLine = lines[i]
      
      // 空行保持
      if (nextLine.trim() === '') {
        newLines.push('')
        i++
        continue
      }
      
      const nextIndent = nextLine.match(/^(\s*)/)[1].length
      const nextContent = nextLine.trim()
      
      // 如果遇到同级别或更高级别的键（如 schema, content, responses, paths 等），停止
      // 检查是否是路径定义（以 / 开头）
      if (nextContent.startsWith('/') && nextIndent <= exampleIndent) {
        break
      }
      
      // 检查是否是其他同级别键
      if (nextIndent <= exampleIndent && nextLine.match(/^\s+[a-zA-Z-]+:/)) {
        break
      }
      
      // 调整缩进：在 value 下再缩进2级（6个空格）
      const content = nextLine.substring(nextIndent)
      const newIndent = indent + '      ' // value 下2级缩进（6个空格）
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

