/**
 * 为所有响应添加 schema 定义
 */

const fs = require('fs')
const path = require('path')

function addResponseSchema(filePath) {
  const content = fs.readFileSync(filePath, 'utf-8')
  const lines = content.split('\n')
  const newLines = []
  
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i]
    newLines.push(line)
    
    // 检测 application/json 内容开始
    if (line.match(/^\s+application\/json:/)) {
      const nextLine = i + 1 < lines.length ? lines[i + 1] : ''
      
      // 如果下一行是 example:，说明缺少 schema
      if (nextLine && nextLine.match(/^\s+example:/)) {
        const indent = line.match(/^(\s+)/)[1]
        // 在 example 之前插入 schema
        newLines.push(`${indent}schema:`)
        newLines.push(`${indent}  $ref: '#/components/schemas/Result'`)
      }
    }
  }
  
  return newLines.join('\n')
}

// 主程序
const openapiPath = path.join(__dirname, '..', 'openapi.yaml')

console.log('读取 openapi.yaml 文件...')
const originalContent = fs.readFileSync(openapiPath, 'utf-8')

console.log('为所有响应添加 schema 定义...')
const fixedContent = addResponseSchema(openapiPath)

// 备份
const backupPath = openapiPath + '.backup3'
fs.writeFileSync(backupPath, originalContent)
console.log(`已创建备份: ${backupPath}`)

// 写入修复后的内容
fs.writeFileSync(openapiPath, fixedContent)
console.log('✅ 已为所有响应添加 schema 定义')

