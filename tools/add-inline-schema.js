/**
 * 添加内联 schema，同时保留 example
 * 不使用 $ref，直接内联定义，帮助 API Fox 理解数据结构
 */

const fs = require('fs')
const path = require('path')

function addInlineSchema(filePath) {
  const content = fs.readFileSync(filePath, 'utf-8')
  const lines = content.split('\n')
  const newLines = []
  
  let inResponseContent = false
  let hasSchema = false
  let hasExample = false
  let exampleCode = null
  
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i]
    
    // 检测 application/json: 内容开始
    if (line.match(/^\s+application\/json:\s*$/)) {
      inResponseContent = true
      hasSchema = false
      hasExample = false
      exampleCode = null
      newLines.push(line)
      continue
    }
    
    // 在响应内容中
    if (inResponseContent) {
      // 检测 example 开始
      if (line.match(/^\s+example:\s*$/)) {
        hasExample = true
        
        // 如果还没有 schema，先添加 schema
        if (!hasSchema) {
          const indent = line.match(/^(\s+)/)[1]
          newLines.push(`${indent}schema:`)
          newLines.push(`${indent}  type: object`)
          newLines.push(`${indent}  properties:`)
          newLines.push(`${indent}    code:`)
          newLines.push(`${indent}      type: integer`)
          newLines.push(`${indent}      description: 状态码`)
          newLines.push(`${indent}    data:`)
          newLines.push(`${indent}      description: 响应数据`)
          newLines.push(`${indent}      nullable: true`)
          newLines.push(`${indent}  required:`)
          newLines.push(`${indent}    - code`)
          hasSchema = true
        }
        
        newLines.push(line)
        continue
      }
      
      // 检测 code 值
      if (hasExample && line.match(/^\s+code:\s*(\d+)/)) {
        exampleCode = line.match(/^\s+code:\s*(\d+)/)[1]
      }
      
      // 检测 schema 存在
      if (line.match(/^\s+schema:\s*$/)) {
        hasSchema = true
      }
    }
    
    // 检测响应结束
    if (inResponseContent && (line.match(/^\s+\/[a-z]/) || (line.match(/^\s+(get|post|put|delete|patch):/) && !line.match(/^\s+example:/)))) {
      inResponseContent = false
      hasSchema = false
      hasExample = false
      exampleCode = null
    }
    
    newLines.push(line)
  }
  
  return newLines.join('\n')
}

// 主程序
const openapiPath = path.join(__dirname, '..', 'openapi.yaml')

console.log('读取 openapi.yaml 文件...')
const originalContent = fs.readFileSync(openapiPath, 'utf-8')

console.log('添加内联 schema...')
const fixedContent = addInlineSchema(openapiPath)

// 备份
const backupPath = openapiPath + '.backup9'
fs.writeFileSync(backupPath, originalContent)
console.log(`已创建备份: ${backupPath}`)

// 写入修复后的内容
fs.writeFileSync(openapiPath, fixedContent)
console.log('✅ 已添加内联 schema')

