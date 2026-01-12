/**
 * 正确修复 data 字段的 schema 定义
 * - 如果 example 中 data: null，schema 中只保留 nullable: true，不定义 type
 * - 如果 example 中 data 有实际数据，定义 type: object 和 additionalProperties: true
 */

const fs = require('fs')
const path = require('path')

function fixDataSchemaCorrect(filePath) {
  const content = fs.readFileSync(filePath, 'utf-8')
  const lines = content.split('\n')
  const newLines = []
  
  // 第一遍：收集每个响应的 data 值信息
  const dataInfoMap = new Map() // key: 行号, value: { isNull: true/false, hasData: true/false }
  
  let currentResponseLine = -1
  let inExample = false
  let exampleStartLine = -1
  
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i]
    
    // 检测响应开始（application/json）
    if (line.match(/^\s+application\/json:\s*$/)) {
      currentResponseLine = i
    }
    
    // 检测 example 开始
    if (line.match(/^\s+example:\s*$/)) {
      inExample = true
      exampleStartLine = i
    }
    
    // 在 example 中检测 data 的值
    if (inExample && currentResponseLine >= 0) {
      if (line.match(/^\s+data:\s*null\s*$/)) {
        dataInfoMap.set(currentResponseLine, { isNull: true, hasData: false })
      } else if (line.match(/^\s+data:\s*(.+)$/)) {
        const dataValue = line.match(/^\s+data:\s*(.+)$/)[1].trim()
        if (dataValue !== 'null' && dataValue !== '') {
          dataInfoMap.set(currentResponseLine, { isNull: false, hasData: true })
        }
      } else if (line.match(/^\s+data:\s*$/)) {
        const nextLine = i + 1 < lines.length ? lines[i + 1] : ''
        if (nextLine.match(/^\s+-\s+/) || nextLine.match(/^\s+\w+:/)) {
          dataInfoMap.set(currentResponseLine, { isNull: false, hasData: true })
        }
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
        exampleStartLine = -1
      }
    }
    
    // 检测响应结束
    if (line.match(/^\s+\/[a-z]/) || line.match(/^\s+(get|post|put|delete|patch):/)) {
      currentResponseLine = -1
    }
  }
  
  // 第二遍：修复 data property 的定义
  let inResponseContent = false
  let inDataProperty = false
  let dataPropertyStartLine = -1
  let currentResponseKey = null
  
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i]
    
    // 检测响应内容开始
    if (line.match(/^\s+application\/json:\s*$/)) {
      inResponseContent = true
      currentResponseKey = i
    }
    
    // 检测 data property
    if (inResponseContent && line.match(/^\s+data:\s*$/)) {
      inDataProperty = true
      dataPropertyStartLine = i
      const indent = line.match(/^(\s+)/)[1]
      
      // 获取 data 的信息
      const dataInfo = dataInfoMap.get(currentResponseKey)
      const isNull = dataInfo ? dataInfo.isNull : false
      
      newLines.push(line)
      newLines.push(`${indent}  description: 响应数据`)
      
      if (isNull) {
        // data 为 null，只定义 nullable
        newLines.push(`${indent}  nullable: true`)
      } else {
        // data 有值，定义 type 和 additionalProperties
        newLines.push(`${indent}  type: object`)
        newLines.push(`${indent}  nullable: true`)
        newLines.push(`${indent}  additionalProperties: true`)
      }
      
      continue
    }
    
    // 跳过旧的 data property 定义行
    if (inDataProperty) {
      const currentIndent = line.match(/^(\s*)/)[1].length
      const dataIndent = lines[dataPropertyStartLine].match(/^(\s+)/)[1].length
      
      // 如果缩进大于 data 的缩进，说明是 data 的子属性，跳过
      if (currentIndent > dataIndent && 
          line.match(/^\s+(type|additionalProperties|properties|nullable|description):/)) {
        continue
      }
      
      // 如果缩进回到 data 同级或更小，说明 data property 定义结束
      if (currentIndent <= dataIndent) {
        inDataProperty = false
        dataPropertyStartLine = -1
      }
    }
    
    // 检测响应结束
    if (inResponseContent && (line.match(/^\s+\/[a-z]/) || line.match(/^\s+(get|post|put|delete|patch):/))) {
      inResponseContent = false
      inDataProperty = false
      currentResponseKey = null
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
const fixedContent = fixDataSchemaCorrect(openapiPath)

// 备份
const backupPath = openapiPath + '.backup12'
fs.writeFileSync(backupPath, originalContent)
console.log(`已创建备份: ${backupPath}`)

// 写入修复后的内容
fs.writeFileSync(openapiPath, fixedContent)
console.log('✅ 已修复 data 字段的 schema 定义')

