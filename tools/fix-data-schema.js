/**
 * 修复 data 字段的 schema 定义
 * 根据 example 中的实际数据，为 data 字段添加详细的结构定义
 */

const fs = require('fs')
const path = require('path')

function fixDataSchema(filePath) {
  const content = fs.readFileSync(filePath, 'utf-8')
  const lines = content.split('\n')
  const newLines = []
  
  let inResponseSchema = false
  let inDataProperty = false
  let inExample = false
  let dataStructure = null
  let exampleIndent = 0
  
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i]
    
    // 检测响应中的 schema 开始
    if (line.match(/^\s+application\/json:\s*$/)) {
      inResponseSchema = true
      inDataProperty = false
      inExample = false
      dataStructure = null
      newLines.push(line)
      continue
    }
    
    // 检测 data property 定义
    if (inResponseSchema && line.match(/^\s+data:\s*$/)) {
      inDataProperty = true
      newLines.push(line)
      continue
    }
    
    // 检测 example 开始
    if (inResponseSchema && line.match(/^\s+example:\s*$/)) {
      inExample = true
      exampleIndent = line.match(/^(\s+)/)[1].length
      dataStructure = null
      newLines.push(line)
      continue
    }
    
    // 在 example 中收集 data 的结构
    if (inExample) {
      const currentIndent = line.match(/^(\s*)/)[1].length
      
      // 检测 data: 后的内容
      if (line.match(/^\s+data:\s*(.+)$/)) {
        const dataValue = line.match(/^\s+data:\s*(.+)$/)[1].trim()
        if (dataValue === 'null') {
          dataStructure = 'null'
        } else if (dataValue === '[' || dataValue.match(/^\[/)) {
          dataStructure = 'array'
        } else if (dataValue === '{' || dataValue.match(/^\{/)) {
          dataStructure = 'object'
        } else {
          dataStructure = 'string' // 可能是字符串或其他简单类型
        }
      } else if (line.match(/^\s+data:\s*$/)) {
        // data: 单独一行，下一行判断
        const nextLine = i + 1 < lines.length ? lines[i + 1] : ''
        if (nextLine.match(/^\s+-\s+/)) {
          dataStructure = 'array'
        } else if (nextLine.match(/^\s+\w+:/)) {
          dataStructure = 'object'
        }
      }
      
      // 检测 data 对象的属性（用于判断 data 是 object 且有字段）
      if (line.match(/^\s+(token|id|username|nickname|avatar|role|status|list|total|page):/)) {
        if (dataStructure === null) {
          dataStructure = 'object'
        }
      }
      
      // 检测 example 结束
      const nextLine = i + 1 < lines.length ? lines[i + 1] : ''
      const isEnd = nextLine === '' || 
                   (nextLine.match(/^\s+[a-z]/) && nextLine.match(/^\s+[a-z]/)[0].length <= exampleIndent) ||
                   nextLine.match(/^\s*#/) ||
                   nextLine.match(/^\s+\/[a-z]/)
      
      if (isEnd && inDataProperty && dataStructure === null) {
        // 如果 data 只是 null，保持 nullable
        dataStructure = 'null'
      }
      
      newLines.push(line)
      continue
    }
    
    // 如果刚刚完成了 example，需要更新 data 的 schema
    if (inDataProperty && dataStructure !== null && !inExample) {
      // 检查当前行是否是 data 的 description 或 nullable
      if (line.match(/^\s+(description|nullable):/)) {
        newLines.push(line)
        continue
      }
      
      // 在 description 之后添加类型定义
      if (line.match(/^\s+description:/) && !line.match(/type:/)) {
        const indent = line.match(/^(\s+)/)[1]
        newLines.push(line)
        
        // 根据 dataStructure 添加类型定义
        if (dataStructure === 'null') {
          // 保持 nullable: true，已经定义了
          continue
        } else if (dataStructure === 'object') {
          // 添加 object 类型，但具体结构由 example 定义
          newLines.push(`${indent}type: object`)
          newLines.push(`${indent}additionalProperties: true`) // 允许额外字段
        } else if (dataStructure === 'array') {
          // 添加 array 类型
          newLines.push(`${indent}type: array`)
          newLines.push(`${indent}items:`)
          newLines.push(`${indent}  type: object`)
          newLines.push(`${indent}  additionalProperties: true`)
        } else {
          // 其他类型（string, integer 等）
          newLines.push(`${indent}type: ${dataStructure}`)
        }
        
        inDataProperty = false
        dataStructure = null
        continue
      }
    }
    
    // 检测响应结束
    if (inResponseSchema && (line.match(/^\s+\/[a-z]/) || line.match(/^\s+(get|post|put|delete|patch):/))) {
      inResponseSchema = false
      inDataProperty = false
      inExample = false
      dataStructure = null
    }
    
    newLines.push(line)
  }
  
  return newLines.join('\n')
}

// 主程序
const openapiPath = path.join(__dirname, '..', 'openapi.yaml')

console.log('读取 openapi.yaml 文件...')
const originalContent = fs.readFileSync(openapiPath, 'utf-8')

console.log('修复 data 字段的 schema 定义...')
const fixedContent = fixDataSchema(openapiPath)

// 备份
const backupPath = openapiPath + '.backup10'
fs.writeFileSync(backupPath, originalContent)
console.log(`已创建备份: ${backupPath}`)

// 写入修复后的内容
fs.writeFileSync(openapiPath, fixedContent)
console.log('✅ 已修复 data 字段的 schema 定义')

