/**
 * 修复 API Fox Mock 数据问题
 * 将 example 改为 examples 格式，确保 API Fox 能正确识别
 */

const fs = require('fs')
const path = require('path')

function fixExamplesFormat(filePath) {
  const content = fs.readFileSync(filePath, 'utf-8')
  const lines = content.split('\n')
  const newLines = []
  
  let inExampleBlock = false
  let exampleIndent = 0
  let exampleLines = []
  
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i]
    
    // 检测 example: 开始
    if (line.match(/^\s+example:\s*$/)) {
      inExampleBlock = true
      exampleIndent = line.match(/^(\s+)/)[1].length
      exampleLines = []
      
      // 改为 examples 格式
      const indent = line.match(/^(\s+)/)[1]
      newLines.push(`${indent}examples:`)
      newLines.push(`${indent}  default:`)
      newLines.push(`${indent}    value:`)
      continue
    }
    
    // 在 example 块中
    if (inExampleBlock) {
      const currentIndent = line.match(/^(\s*)/)[1].length
      exampleLines.push(line)
      
      // 检测 example 块结束
      const nextLine = i + 1 < lines.length ? lines[i + 1] : ''
      const isEnd = nextLine === '' || 
                   (nextLine.match(/^\s+[a-zA-Z]/) && nextLine.match(/^\s+[a-zA-Z]/)[0].length <= exampleIndent) ||
                   nextLine.match(/^\s*#/) ||
                   nextLine.match(/^\s+\/[a-z]/)
      
      if (isEnd) {
        // 输出 example 数据，需要调整缩进
        exampleLines.forEach(exampleLine => {
          const exampleIndentMatch = exampleLine.match(/^(\s+)/)
          if (exampleIndentMatch) {
            const originalIndent = exampleIndentMatch[1].length
            const newIndent = '    ' + exampleIndentMatch[1] // 增加4个空格（value: 的缩进）
            const content = exampleLine.substring(originalIndent)
            newLines.push(newIndent + content)
          } else {
            newLines.push('    ' + exampleLine)
          }
        })
        
        inExampleBlock = false
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

console.log('修复 example 格式为 examples...')
const fixedContent = fixExamplesFormat(openapiPath)

// 备份
const backupPath = openapiPath + '.backup6'
fs.writeFileSync(backupPath, originalContent)
console.log(`已创建备份: ${backupPath}`)

// 写入修复后的内容
fs.writeFileSync(openapiPath, fixedContent)
console.log('✅ 已修复为 examples 格式')

