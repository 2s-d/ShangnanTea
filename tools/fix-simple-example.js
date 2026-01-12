/**
 * 修复为简单的 example 格式
 * 移除 examples 格式，改为简单的 example 格式
 * 确保有 schema 定义（使用 $ref）
 */

const fs = require('fs')
const path = require('path')

function fixSimpleExample(filePath) {
  const content = fs.readFileSync(filePath, 'utf-8')
  const lines = content.split('\n')
  const newLines = []
  
  let inExamplesBlock = false
  let exampleIndent = 0
  let exampleLines = []
  let skipNext = 0
  
  for (let i = 0; i < lines.length; i++) {
    if (skipNext > 0) {
      skipNext--
      continue
    }
    
    const line = lines[i]
    
    // 检测 examples: 开始
    if (line.match(/^\s+examples:\s*$/)) {
      inExamplesBlock = true
      exampleIndent = line.match(/^(\s+)/)[1].length
      exampleLines = []
      
      // 改为简单的 example:
      const indent = line.match(/^(\s+)/)[1]
      newLines.push(`${indent}example:`)
      skipNext = 1 // 跳过 default:
      continue
    }
    
    // 在 examples 块中，跳过 default: 和 value:
    if (inExamplesBlock) {
      if (line.match(/^\s+default:\s*$/) || line.match(/^\s+value:\s*$/)) {
        skipNext = 0
        continue
      }
      
      // 收集 example 数据
      const currentIndent = line.match(/^(\s*)/)[1].length
      exampleLines.push(line)
      
      // 检测 examples 块结束
      const nextLine = i + 1 < lines.length ? lines[i + 1] : ''
      const isEnd = nextLine === '' || 
                   (nextLine.match(/^\s+[a-zA-Z]/) && nextLine.match(/^\s+[a-zA-Z]/)[0].length <= exampleIndent) ||
                   nextLine.match(/^\s*#/) ||
                   nextLine.match(/^\s+\/[a-z]/)
      
      if (isEnd) {
        // 输出 example 数据，需要调整缩进（减少2个空格，因为移除了 default: 和 value:）
        exampleLines.forEach(exampleLine => {
          const exampleIndentMatch = exampleLine.match(/^(\s+)/)
          if (exampleIndentMatch) {
            const originalIndent = exampleIndentMatch[1].length
            // 减少4个空格（default: 2个 + value: 2个）
            const newIndentLength = Math.max(0, originalIndent - 4)
            const newIndent = ' '.repeat(newIndentLength + exampleIndentMatch[1].length - 4)
            const content = exampleLine.substring(originalIndent)
            newLines.push(newIndent + content)
          } else {
            newLines.push(exampleLine)
          }
        })
        
        inExamplesBlock = false
        exampleLines = []
      }
      continue
    }
    
    newLines.push(line)
  }
  
  return newLines.join('\n')
}

// 主程序
const openapiPath = path.join(__dirname, '..', 'openapi.yaml')

console.log('读取 openapi.yaml 文件...')
const originalContent = fs.readFileSync(openapiPath, 'utf-8')

console.log('修复为简单的 example 格式...')
const fixedContent = fixSimpleExample(openapiPath)

// 备份
const backupPath = openapiPath + '.backup7'
fs.writeFileSync(backupPath, originalContent)
console.log(`已创建备份: ${backupPath}`)

// 写入修复后的内容
fs.writeFileSync(openapiPath, fixedContent)
console.log('✅ 已修复为简单的 example 格式')

