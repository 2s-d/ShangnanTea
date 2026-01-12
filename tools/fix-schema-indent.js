/**
 * 修复 schema 中 example 的缩进问题
 */

const fs = require('fs')
const path = require('path')

function fixIndent(filePath) {
  const content = fs.readFileSync(filePath, 'utf-8')
  const lines = content.split('\n')
  const newLines = []
  
  for (let i = 0; i < lines.length; i++) {
    let line = lines[i]
    
    // 检测 type: integer 后面跟着缩进错误的 example
    if (line.match(/^\s+type: integer\s*$/)) {
      const nextLine = i + 1 < lines.length ? lines[i + 1] : ''
      const nextNextLine = i + 2 < lines.length ? lines[i + 2] : ''
      
      // 如果下一行是缩进错误的 example，需要修复
      if (nextLine.match(/^\s{6,8}example:\s*\d+/) && nextNextLine.match(/^\s+description:/)) {
        const indent = line.match(/^(\s+)/)[1]
        const exampleMatch = nextLine.match(/example:\s*(\d+)/)
        const descMatch = nextNextLine.match(/description:\s*(.+)/)
        
        newLines.push(line) // type: integer
        newLines.push(`${indent}description: ${descMatch ? descMatch[1] : '状态码'}`)
        newLines.push(`${indent}example: ${exampleMatch ? exampleMatch[1] : ''}`)
        i += 2 // 跳过已处理的行
        continue
      }
    }
    
    newLines.push(line)
  }
  
  return newLines.join('\n')
}

// 主程序
const openapiPath = path.join(__dirname, '..', 'openapi.yaml')

console.log('读取 openapi.yaml 文件...')
const originalContent = fs.readFileSync(openapiPath, 'utf-8')

console.log('修复缩进问题...')
const fixedContent = fixIndent(openapiPath)

// 备份
const backupPath = openapiPath + '.backup5'
fs.writeFileSync(backupPath, originalContent)
console.log(`已创建备份: ${backupPath}`)

// 写入修复后的内容
fs.writeFileSync(openapiPath, fixedContent)
console.log('✅ 已修复缩进问题')

