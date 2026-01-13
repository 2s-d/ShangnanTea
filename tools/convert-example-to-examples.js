const fs = require('fs')
const path = require('path')

/**
 * 将 openapi.yaml 中的 example 转换为 examples 格式
 * 参考 ooo.yaml 的格式，使用 examples.default.value 结构
 */

const openapiPath = path.join(__dirname, '../openapi.yaml')

// 读取文件
let content = fs.readFileSync(openapiPath, 'utf-8')

// 使用正则表达式替换
// 匹配模式：example: 后面跟多行内容，直到下一个同级别或更高级别的键

// 先处理单行 example: { ... }
content = content.replace(
  /^(\s+)example:\s*(\{[^}]*\})\s*$/gm,
  (match, indent, exampleContent) => {
    return `${indent}examples:\n${indent}  default:\n${indent}    summary: 响应示例\n${indent}    value: ${exampleContent}`
  }
)

// 处理多行 example:
// 匹配 example: 开始，然后收集后续内容直到遇到同级别或更高级别的键
const lines = content.split('\n')
const newLines = []
let i = 0

while (i < lines.length) {
  const line = lines[i]
  
  // 检测 example: 开始（单独一行，后面跟多行内容）
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
      
      // 如果遇到同级别或更高级别的键（如 schema, content, responses, paths 等），停止
      if (nextIndent <= exampleIndent && nextLine.match(/^\s+[a-zA-Z]+:/)) {
        break
      }
      
      // 调整缩进：在 value 下再缩进2级
      const content = nextLine.substring(nextIndent)
      const newIndent = indent + '      ' // value 下2级缩进
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

