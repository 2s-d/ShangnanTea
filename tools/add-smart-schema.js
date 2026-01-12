/**
 * 智能添加 schema 定义
 * 根据 example 中的 data 值来决定 schema 的定义：
 * - 如果 data: null，schema 中 data 只定义 nullable: true
 * - 如果 data 有值，schema 中 data 定义 type: object + nullable: true + additionalProperties: true
 */

const fs = require('fs')
const path = require('path')

function addSmartSchema(filePath) {
  const content = fs.readFileSync(filePath, 'utf-8')
  const lines = content.split('\n')
  const newLines = []
  
  let inResponseContent = false
  let responseContentStartLine = -1
  let inExample = false
  let exampleStartLine = -1
  let dataIsNull = false
  
  // 第一遍：收集每个响应的 data 值信息
  const responseDataInfo = new Map()
  
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i]
    
    if (line.match(/^\s+application\/json:\s*$/)) {
      inResponseContent = true
      responseContentStartLine = i
      dataIsNull = false
    }
    
    if (inResponseContent && line.match(/^\s+example:\s*$/)) {
      inExample = true
      exampleStartLine = i
    }
    
    if (inExample && line.match(/^\s+data:\s*null\s*$/)) {
      dataIsNull = true
      responseDataInfo.set(responseContentStartLine, { isNull: true })
    } else if (inExample && line.match(/^\s+data:\s*(.+)$/)) {
      const dataValue = line.match(/^\s+data:\s*(.+)$/)[1].trim()
      if (dataValue !== 'null' && dataValue !== '') {
        responseDataInfo.set(responseContentStartLine, { isNull: false })
        dataIsNull = false
      }
    } else if (inExample && line.match(/^\s+data:\s*$/)) {
      const nextLine = i + 1 < lines.length ? lines[i + 1] : ''
      if (nextLine.match(/^\s+-\s+/) || nextLine.match(/^\s+\w+:/)) {
        responseDataInfo.set(responseContentStartLine, { isNull: false })
        dataIsNull = false
      }
    }
    
    // 检测 example 结束
    if (inExample) {
      const nextLine = i + 1 < lines.length ? lines[i + 1] : ''
      const indent = lines[exampleStartLine].match(/^(\s+)/)[1].length
      if (nextLine === '' || 
          (nextLine.match(/^\s+[a-z]/) && nextLine.match(/^\s+[a-z]/)[0].length <= indent) ||
          nextLine.match(/^\s*#/) ||
          nextLine.match(/^\s+\/[a-z]/)) {
        inExample = false
      }
    }
    
    // 检测响应结束
    if (line.match(/^\s+\/[a-z]/) || line.match(/^\s+(get|post|put|delete|patch):/)) {
      inResponseContent = false
      responseContentStartLine = -1
      inExample = false
    }
  }
  
  // 第二遍：添加 schema 定义
  inResponseContent = false
  responseContentStartLine = -1
  
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i]
    
    if (line.match(/^\s+application\/json:\s*$/)) {
      inResponseContent = true
      responseContentStartLine = i
      newLines.push(line)
      
      // 检查是否有 example
      const hasExample = responseDataInfo.has(responseContentStartLine)
      if (hasExample) {
        // 添加 schema
        const indent = line.match(/^(\s+)/)[1]
        const dataInfo = responseDataInfo.get(responseContentStartLine)
        
        newLines.push(`${indent}schema:`)
        newLines.push(`${indent}  type: object`)
        newLines.push(`${indent}  properties:`)
        newLines.push(`${indent}    code:`)
        newLines.push(`${indent}      type: integer`)
        newLines.push(`${indent}      description: 状态码`)
        newLines.push(`${indent}    data:`)
        newLines.push(`${indent}      description: 响应数据`)
        
        if (dataInfo.isNull) {
          // data 为 null，只定义 nullable
          newLines.push(`${indent}      nullable: true`)
        } else {
          // data 有值，定义 type 和 additionalProperties
          newLines.push(`${indent}      type: object`)
          newLines.push(`${indent}      nullable: true`)
          newLines.push(`${indent}      additionalProperties: true`)
        }
        
        newLines.push(`${indent}  required:`)
        newLines.push(`${indent}    - code`)
      }
      continue
    }
    
    // 检测响应结束
    if (inResponseContent && (line.match(/^\s+\/[a-z]/) || line.match(/^\s+(get|post|put|delete|patch):/))) {
      inResponseContent = false
      responseContentStartLine = -1
    }
    
    newLines.push(line)
  }
  
  return newLines.join('\n')
}

// 主程序
const openapiPath = path.join(__dirname, '..', 'openapi.yaml')

console.log('读取 openapi.yaml 文件...')
const originalContent = fs.readFileSync(openapiPath, 'utf-8')

console.log('智能添加 schema 定义...')
const fixedContent = addSmartSchema(openapiPath)

// 备份
const backupPath = openapiPath + '.backup13'
fs.writeFileSync(backupPath, originalContent)
console.log(`已创建备份: ${backupPath}`)

// 写入修复后的内容
fs.writeFileSync(openapiPath, fixedContent)
console.log('✅ 已智能添加 schema 定义')

