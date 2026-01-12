/**
 * 移除 $ref schema，直接使用 example
 * 参考旧版本格式：只有 example，没有 schema
 */

const fs = require('fs')
const path = require('path')

function removeSchemaRef(filePath) {
  const content = fs.readFileSync(filePath, 'utf-8')
  const lines = content.split('\n')
  const newLines = []
  
  let inResponseContent = false
  let skipSchema = false
  
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i]
    
    // 检测 application/json: 内容开始
    if (line.match(/^\s+application\/json:\s*$/)) {
      inResponseContent = true
      skipSchema = false
      newLines.push(line)
      continue
    }
    
    // 在响应内容中，跳过 schema 定义
    if (inResponseContent) {
      // 检测 schema: 开始
      if (line.match(/^\s+schema:\s*$/)) {
        skipSchema = true
        continue
      }
      
      // 检测 $ref 行
      if (skipSchema && line.match(/^\s+\$ref:/)) {
        skipSchema = false
        continue
      }
      
      // 跳过 schema 的其他行（type, properties 等）
      if (skipSchema) {
        const currentIndent = line.match(/^(\s*)/)[1].length
        const schemaIndent = lines[i-1] ? (lines[i-1].match(/^\s+schema:\s*$/) ? lines[i-1].match(/^(\s+)/)[1].length : null) : null
        
        // 如果缩进大于或等于 schema 的缩进，说明还在 schema 块中
        if (schemaIndent !== null && currentIndent > schemaIndent) {
          continue
        }
        
        // 如果缩进回到 schema 同级或更小，说明 schema 块结束
        if (schemaIndent !== null && currentIndent <= schemaIndent) {
          skipSchema = false
        } else {
          continue
        }
      }
      
      // 检测 example: 开始，说明 schema 块已结束
      if (line.match(/^\s+example:\s*$/)) {
        skipSchema = false
        inResponseContent = false
        newLines.push(line)
        continue
      }
    }
    
    // 检测响应结束（新的路径或方法）
    if (inResponseContent && (line.match(/^\s+\/[a-z]/) || line.match(/^\s+(get|post|put|delete|patch):/))) {
      inResponseContent = false
      skipSchema = false
    }
    
    newLines.push(line)
  }
  
  return newLines.join('\n')
}

// 主程序
const openapiPath = path.join(__dirname, '..', 'openapi.yaml')

console.log('读取 openapi.yaml 文件...')
const originalContent = fs.readFileSync(openapiPath, 'utf-8')

console.log('移除 $ref schema，直接使用 example...')
const fixedContent = removeSchemaRef(openapiPath)

// 备份
const backupPath = openapiPath + '.backup8'
fs.writeFileSync(backupPath, originalContent)
console.log(`已创建备份: ${backupPath}`)

// 写入修复后的内容
fs.writeFileSync(openapiPath, fixedContent)
console.log('✅ 已移除 schema，直接使用 example')

