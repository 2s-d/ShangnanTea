/**
 * 修复 openapi.yaml 中的响应格式
 * 为所有响应添加 schema 定义
 */

const fs = require('fs')
const path = require('path')

function fixResponseSchema(filePath) {
  const content = fs.readFileSync(filePath, 'utf-8')
  const lines = content.split('\n')
  const newLines = []
  
  let inResponse = false
  let inJsonContent = false
  let hasExample = false
  let hasSchema = false
  let exampleStartLine = -1
  
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i]
    
    // 检测响应开始
    if (line.match(/^\s+responses:/)) {
      inResponse = true
      hasExample = false
      hasSchema = false
      inJsonContent = false
    }
    
    // 检测 application/json 内容开始
    if (line.match(/^\s+application\/json:/)) {
      inJsonContent = true
      hasExample = false
      hasSchema = false
    }
    
    // 检测 example 开始
    if (inJsonContent && line.match(/^\s+example:/)) {
      hasExample = true
      exampleStartLine = i
    }
    
    // 检测 schema 是否存在
    if (inJsonContent && line.match(/^\s+schema:/)) {
      hasSchema = true
    }
    
    // 检测响应示例结束（下一个顶级键或空行后跟非缩进行）
    if (inJsonContent && hasExample) {
      const nextLine = i + 1 < lines.length ? lines[i + 1] : ''
      const isEnd = nextLine === '' || 
                   nextLine.match(/^\s+[a-zA-Z]/) ||
                   nextLine.match(/^\s*#/) ||
                   (nextLine.trim() === '' && i + 2 < lines.length && lines[i + 2].match(/^\s+[a-zA-Z]/))
      
      if (isEnd && !hasSchema) {
        // 在 example 之前插入 schema 定义
        const indent = lines[exampleStartLine].match(/^(\s+)/)[1]
        newLines.push(`${indent}schema:`)
        newLines.push(`${indent}  type: object`)
        newLines.push(`${indent}  properties:`)
        newLines.push(`${indent}    code:`)
        newLines.push(`${indent}      type: integer`)
        newLines.push(`${indent}      example: 200`)
        newLines.push(`${indent}    data:`)
        newLines.push(`${indent}      type: object`)
        newLines.push(`${indent}      nullable: true`)
        newLines.push(`${indent}      example: null`)
        newLines.push('')
        hasSchema = true
      }
    }
    
    // 检测响应结束
    if (inResponse && line.match(/^\s+\/[a-z]/)) {
      inResponse = false
      inJsonContent = false
      hasExample = false
      hasSchema = false
    }
    
    newLines.push(line)
  }
  
  return newLines.join('\n')
}

// 主程序
const openapiPath = path.join(__dirname, '..', 'openapi.yaml')

console.log('读取 openapi.yaml 文件...')
const originalContent = fs.readFileSync(openapiPath, 'utf-8')

console.log('修复响应格式...')
const fixedContent = fixResponseSchema(openapiPath)

// 备份
const backupPath = openapiPath + '.backup2'
fs.writeFileSync(backupPath, originalContent)
console.log(`已创建备份: ${backupPath}`)

// 写入修复后的内容
fs.writeFileSync(openapiPath, fixedContent)
console.log('✅ 响应格式已修复')

