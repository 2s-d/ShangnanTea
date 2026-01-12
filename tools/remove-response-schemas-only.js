/**
 * 只移除响应中的 schema 定义，保留请求体中的 schema
 * 参考旧版本：响应只有 example，没有 schema
 */

const fs = require('fs')
const path = require('path')

function removeResponseSchemasOnly(filePath) {
  const content = fs.readFileSync(filePath, 'utf-8')
  const lines = content.split('\n')
  const newLines = []
  
  let inResponseSection = false
  let inRequestBody = false
  let inResponseContent = false
  let inSchemaBlock = false
  let schemaIndent = 0
  let skipSchema = false
  
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i]
    
    // 检测 requestBody 开始
    if (line.match(/^\s+requestBody:\s*$/)) {
      inRequestBody = true
      inResponseSection = false
      newLines.push(line)
      continue
    }
    
    // 检测 responses 开始
    if (line.match(/^\s+responses:\s*$/)) {
      inResponseSection = true
      inRequestBody = false
      newLines.push(line)
      continue
    }
    
    // 在请求体中，保留所有内容（包括 schema）
    if (inRequestBody) {
      // 检测请求体结束
      if (line.match(/^\s+responses:/)) {
        inRequestBody = false
        inResponseSection = true
      }
      newLines.push(line)
      continue
    }
    
    // 在响应部分
    if (inResponseSection) {
      // 检测 application/json: 内容开始（只在响应中）
      if (line.match(/^\s+application\/json:\s*$/)) {
        inResponseContent = true
        inSchemaBlock = false
        skipSchema = false
        schemaIndent = 0
        newLines.push(line)
        continue
      }
      
      // 在响应内容中
      if (inResponseContent) {
        // 检测 schema: 开始（只在响应中移除）
        if (line.match(/^\s+schema:\s*$/)) {
          inSchemaBlock = true
          skipSchema = true
          schemaIndent = line.match(/^(\s+)/)[1].length
          // 跳过这一行，不添加到输出
          continue
        }
        
        // 如果在 schema 块中
        if (inSchemaBlock && skipSchema) {
          const currentIndent = line.match(/^(\s*)/)[1].length
          
          // 如果缩进大于 schema 的缩进，说明还在 schema 块中
          if (currentIndent > schemaIndent) {
            continue // 跳过这一行
          }
          
          // 如果缩进回到 schema 同级或更小，说明 schema 块结束
          if (currentIndent <= schemaIndent) {
            inSchemaBlock = false
            skipSchema = false
            // 继续处理这一行
          }
        }
        
        // 检测 example: 开始，说明 schema 块已结束
        if (line.match(/^\s+example:\s*$/)) {
          inSchemaBlock = false
          skipSchema = false
          inResponseContent = false
          newLines.push(line)
          continue
        }
      }
      
      // 检测响应结束（新的路径或方法）
      if (inResponseSection && (line.match(/^\s+\/[a-z]/) || line.match(/^\s+(get|post|put|delete|patch):/))) {
        inResponseSection = false
        inResponseContent = false
        inSchemaBlock = false
        skipSchema = false
      }
    }
    
    // 如果不是要跳过的行，添加到输出
    if (!skipSchema) {
      newLines.push(line)
    }
  }
  
  return newLines.join('\n')
}

// 主程序
const openapiPath = path.join(__dirname, '..', 'openapi.yaml')

console.log('读取 openapi.yaml 文件...')
const originalContent = fs.readFileSync(openapiPath, 'utf-8')

console.log('只移除响应中的 schema 定义，保留请求体中的 schema...')
const fixedContent = removeResponseSchemasOnly(openapiPath)

// 备份
const backupPath = openapiPath + '.backup11'
fs.writeFileSync(backupPath, originalContent)
console.log(`已创建备份: ${backupPath}`)

// 写入修复后的内容
fs.writeFileSync(openapiPath, fixedContent)
console.log('✅ 已移除响应中的 schema，只保留 example（参考旧版本）')

