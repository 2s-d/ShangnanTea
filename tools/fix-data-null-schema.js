/**
 * 修复 data 字段的 schema 定义
 * - 如果 example 中 data: null，schema 中只保留 nullable: true，不定义 type
 * - 如果 example 中 data 有实际数据，需要定义 type: object 和 properties
 */

const fs = require('fs')
const path = require('path')

function fixDataNullSchema(filePath) {
  const content = fs.readFileSync(filePath, 'utf-8')
  const lines = content.split('\n')
  const newLines = []
  
  let inResponseSchema = false
  let inDataProperty = false
  let inExample = false
  let dataIsNull = false
  let dataHasValue = false
  let dataIndent = 0
  let exampleIndent = 0
  
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i]
    
    // 检测响应中的 schema 开始
    if (line.match(/^\s+application\/json:\s*$/)) {
      inResponseSchema = true
      inDataProperty = false
      inExample = false
      dataIsNull = false
      dataHasValue = false
      newLines.push(line)
      continue
    }
    
    // 检测 data property 定义
    if (inResponseSchema && line.match(/^\s+data:\s*$/)) {
      inDataProperty = true
      dataIndent = line.match(/^(\s+)/)[1].length
      newLines.push(line)
      continue
    }
    
    // 在 data property 中，收集其定义
    if (inDataProperty && !inExample) {
      const currentIndent = line.match(/^(\s*)/)[1].length
      
      // 如果缩进小于等于 data 的缩进，说明 data property 定义结束
      if (currentIndent <= dataIndent && !line.match(/^\s+(description|type|nullable|additionalProperties|properties):/)) {
        inDataProperty = false
      }
      
      // 检测 example 开始，说明 data property 定义结束
      if (line.match(/^\s+example:\s*$/)) {
        inDataProperty = false
        inExample = true
        exampleIndent = line.match(/^(\s+)/)[1].length
        newLines.push(line)
        continue
      }
      
      // 暂时跳过 data property 的定义行，稍后根据 example 重新生成
      if (currentIndent > dataIndent && line.match(/^\s+(type|additionalProperties|properties):/)) {
        continue
      }
    }
    
    // 检测 example 开始
    if (inResponseSchema && line.match(/^\s+example:\s*$/)) {
      inExample = true
      exampleIndent = line.match(/^(\s+)/)[1].length
      dataIsNull = false
      dataHasValue = false
      newLines.push(line)
      continue
    }
    
    // 在 example 中检测 data 的值
    if (inExample) {
      // 检测 data: null
      if (line.match(/^\s+data:\s*null\s*$/)) {
        dataIsNull = true
        dataHasValue = false
      }
      // 检测 data: { 或 data: 后面有其他内容
      else if (line.match(/^\s+data:\s*(.+)$/)) {
        const dataValue = line.match(/^\s+data:\s*(.+)$/)[1].trim()
        if (dataValue !== 'null' && dataValue !== '') {
          dataHasValue = true
          dataIsNull = false
        }
      } else if (line.match(/^\s+data:\s*$/)) {
        // data: 单独一行，下一行判断
        const nextLine = i + 1 < lines.length ? lines[i + 1] : ''
        if (nextLine.match(/^\s+-\s+/) || nextLine.match(/^\s+\w+:/)) {
          dataHasValue = true
          dataIsNull = false
        }
      }
      
      // 检测 example 结束
      const nextLine = i + 1 < lines.length ? lines[i + 1] : ''
      const isEnd = nextLine === '' || 
                   (nextLine.match(/^\s+[a-z]/) && nextLine.match(/^\s+[a-z]/)[0].length <= exampleIndent) ||
                   nextLine.match(/^\s*#/) ||
                   nextLine.match(/^\s+\/[a-z]/)
      
      if (isEnd) {
        // example 结束，现在可以修复 data property 的定义
        // 需要在 example 之前插入正确的 schema 定义
        // 但这需要在之前的 data property 位置修复，所以先记录，稍后处理
        
        newLines.push(line)
        inExample = false
        
        // 需要在 data property 的位置修复，但这需要回溯
        // 暂时先输出，后面统一处理
        continue
      }
    }
    
    // 检测响应结束
    if (inResponseSchema && (line.match(/^\s+\/[a-z]/) || line.match(/^\s+(get|post|put|delete|patch):/))) {
      inResponseSchema = false
      inDataProperty = false
      inExample = false
      dataIsNull = false
      dataHasValue = false
    }
    
    newLines.push(line)
  }
  
  // 第二遍：修复 data property 的定义
  const result = []
  let needFixData = false
  let dataNull = false
  
  for (let i = 0; i < newLines.length; i++) {
    const line = newLines[i]
    
    // 检测 data property
    if (line.match(/^\s+data:\s*$/)) {
      const indent = line.match(/^(\s+)/)[1]
      result.push(line)
      
      // 检查后续的 example 中 data 的值
      for (let j = i + 1; j < newLines.length; j++) {
        if (newLines[j].match(/^\s+example:\s*$/)) {
          // 找到 example，检查 data 的值
          for (let k = j + 1; k < newLines.length; k++) {
            if (newLines[k].match(/^\s+data:\s*null\s*$/)) {
              dataNull = true
              break
            } else if (newLines[k].match(/^\s+data:\s*(.+)$/) || newLines[k].match(/^\s+data:\s*$/)) {
              const dataValue = newLines[k].match(/^\s+data:\s*(.+)$/) ? 
                               newLines[k].match(/^\s+data:\s*(.+)$/)[1].trim() : ''
              if (dataValue !== 'null' && dataValue !== '') {
                dataNull = false
                break
              } else if (newLines[k].match(/^\s+data:\s*$/)) {
                // 检查下一行
                const nextLine = k + 1 < newLines.length ? newLines[k + 1] : ''
                if (nextLine.match(/^\s+-\s+/) || nextLine.match(/^\s+\w+:/)) {
                  dataNull = false
                  break
                }
              }
            }
            
            // 如果遇到下一个顶级键，说明 example 结束
            if (newLines[k].match(/^\s+(code|description|required):/) && 
                newLines[k].match(/^\s+/)[0].length <= indent.length) {
              break
            }
          }
          break
        }
      }
      
      // 添加正确的 data 定义
      result.push(`${indent}  description: 响应数据`)
      if (dataNull) {
        // data 为 null，只定义 nullable
        result.push(`${indent}  nullable: true`)
      } else {
        // data 有值，定义 type 和 additionalProperties
        result.push(`${indent}  type: object`)
        result.push(`${indent}  nullable: true`)
        result.push(`${indent}  additionalProperties: true`)
      }
      
      needFixData = false
      continue
    }
    
    // 跳过旧的 data property 定义行
    if (needFixData) {
      const currentIndent = line.match(/^(\s*)/)[1].length
      if (currentIndent > 0 && line.match(/^\s+(type|additionalProperties|properties|nullable|description):/)) {
        continue
      } else {
        needFixData = false
      }
    }
    
    result.push(line)
  }
  
  return result.join('\n')
}

// 主程序
const openapiPath = path.join(__dirname, '..', 'openapi.yaml')

console.log('读取 openapi.yaml 文件...')
const originalContent = fs.readFileSync(openapiPath, 'utf-8')

console.log('修复 data 字段的 schema 定义...')
const fixedContent = fixDataNullSchema(openapiPath)

// 备份
const backupPath = openapiPath + '.backup11'
fs.writeFileSync(backupPath, originalContent)
console.log(`已创建备份: ${backupPath}`)

// 写入修复后的内容
fs.writeFileSync(openapiPath, fixedContent)
console.log('✅ 已修复 data 字段的 schema 定义')

