/**
 * 修复 API Fox Mock 数据问题
 * 将 $ref schema 改为内联 schema，并在 properties 中添加 example
 */

const fs = require('fs')
const path = require('path')

function fixInlineSchema(filePath) {
  const content = fs.readFileSync(filePath, 'utf-8')
  const lines = content.split('\n')
  const newLines = []
  
  let inResponseExample = false
  let exampleCode = null
  let exampleData = []
  let exampleIndent = 0
  let schemaRefLine = -1
  
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i]
    
    // 检测 $ref schema
    if (line.match(/^\s+\$ref:\s*'#\/components\/schemas\/Result'/)) {
      schemaRefLine = i
      const indent = line.match(/^(\s+)/)[1]
      // 替换为内联 schema
      newLines.push(`${indent}type: object`)
      newLines.push(`${indent}properties:`)
      newLines.push(`${indent}  code:`)
      newLines.push(`${indent}    type: integer`)
      newLines.push(`${indent}    description: 状态码`)
      newLines.push(`${indent}  data:`)
      newLines.push(`${indent}    description: 响应数据`)
      newLines.push(`${indent}    nullable: true`)
      newLines.push(`${indent}required:`)
      newLines.push(`${indent}  - code`)
      continue
    }
    
    // 检测 example 开始
    if (line.match(/^\s+example:\s*$/)) {
      inResponseExample = true
      exampleIndent = line.match(/^(\s+)/)[1].length
      exampleCode = null
      exampleData = []
      newLines.push(line)
      continue
    }
    
    // 在 example 块中
    if (inResponseExample) {
      exampleData.push(line)
      
      // 提取 code 值
      const codeMatch = line.match(/^\s+code:\s*(\d+)/)
      if (codeMatch) {
        exampleCode = codeMatch[1]
      }
      
      // 检测 example 块结束
      const nextLine = i + 1 < lines.length ? lines[i + 1] : ''
      const isEnd = nextLine === '' || 
                   (nextLine.match(/^\s+[a-zA-Z]/) && nextLine.match(/^\s+[a-zA-Z]/)[0].length <= exampleIndent) ||
                   nextLine.match(/^\s*#/) ||
                   nextLine.match(/^\s+\/[a-z]/)
      
      if (isEnd) {
        // 如果找到了 code 值，更新 schema 中的 example
        if (exampleCode && schemaRefLine >= 0) {
          // 找到 code property 的位置并添加 example
          for (let j = newLines.length - 1; j >= 0; j--) {
            if (newLines[j].match(/^\s+code:/) && newLines[j+1] && newLines[j+1].match(/^\s+type: integer/)) {
              // 在 type 之后添加 example
              const codeIndent = newLines[j].match(/^(\s+)/)[1]
              const typeLine = j + 1
              newLines.splice(typeLine + 1, 0, `${codeIndent}    example: ${exampleCode}`)
              break
            }
          }
        }
        
        newLines.push(...exampleData)
        inResponseExample = false
        exampleCode = null
        exampleData = []
        schemaRefLine = -1
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

console.log('修复内联 schema...')
const fixedContent = fixInlineSchema(openapiPath)

// 备份
const backupPath = openapiPath + '.backup4'
fs.writeFileSync(backupPath, originalContent)
console.log(`已创建备份: ${backupPath}`)

// 写入修复后的内容
fs.writeFileSync(openapiPath, fixedContent)
console.log('✅ 已修复内联 schema')

