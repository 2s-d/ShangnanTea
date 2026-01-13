const fs = require('fs')
const path = require('path')

/**
 * 将 openapi.yaml 转换为示例数据优先格式
 * 参考 ooo.yaml 的格式，使用 examples 而不是 example
 */

const openapiPath = path.join(__dirname, '../openapi.yaml')
const codeMappingPath = path.join(__dirname, '../shangnantea-web/docs/tasks/code-message-mapping.md')

// 读取文件
let content = fs.readFileSync(openapiPath, 'utf-8')
const codeMapping = fs.readFileSync(codeMappingPath, 'utf-8')

// 解析状态码映射（简化版，只提取成功码）
const successCodeMap = {}
const codeLines = codeMapping.split('\n')
let currentModule = ''
let currentPath = ''

codeLines.forEach(line => {
  // 提取模块标题
  const moduleMatch = line.match(/^##\s+(.+?)\s+\((\d+)xxx\)/)
  if (moduleMatch) {
    currentModule = moduleMatch[1]
  }
  
  // 提取状态码
  const codeMatch = line.match(/^\|\s*(\d{4})\s*\|\s*(.+?)\s*\|/)
  if (codeMatch) {
    const code = parseInt(codeMatch[1])
    // 成功码：百位为0
    if (Math.floor(code / 100) % 10 === 0) {
      successCodeMap[code] = codeMatch[2]
    }
  }
})

// 将单数 example 转换为复数 examples 格式
// 格式：example: { code: xxx, data: ... } -> examples: { default: { value: { code: xxx, data: ... } } }

const lines = content.split('\n')
const newLines = []
let i = 0
let inExampleBlock = false
let exampleIndent = 0
let exampleLines = []
let currentMethod = ''
let currentPath = ''

while (i < lines.length) {
  const line = lines[i]
  
  // 检测路径和方法
  const pathMatch = line.match(/^\s*(\/[^:]+):/)
  if (pathMatch) {
    currentPath = pathMatch[1]
  }
  
  const methodMatch = line.match(/^\s+(get|post|put|delete|patch):/)
  if (methodMatch) {
    currentMethod = methodMatch[1].toUpperCase()
  }
  
  // 检测 example: 开始
  if (line.match(/^\s+example:\s*$/)) {
    inExampleBlock = true
    exampleIndent = line.match(/^(\s+)/)?.[1]?.length || 0
    exampleLines = []
    newLines.push(line.replace(/example:/, 'examples:'))
    i++
    continue
  }
  
  // 检测 example: { 开始（单行）
  const singleLineExampleMatch = line.match(/^(\s+)example:\s*\{/)
  if (singleLineExampleMatch) {
    inExampleBlock = true
    exampleIndent = singleLineExampleMatch[1].length
    exampleLines = []
    // 替换为 examples 格式
    const indent = singleLineExampleMatch[1]
    newLines.push(`${indent}examples:`)
    newLines.push(`${indent}  default:`)
    newLines.push(`${indent}    summary: 响应示例`)
    newLines.push(`${indent}    value:`)
    // 提取后面的内容
    const rest = line.substring(line.indexOf('{'))
    if (rest.includes('}')) {
      // 单行完成
      newLines.push(`${indent}      ${rest}`)
      inExampleBlock = false
    } else {
      // 多行开始
      newLines.push(`${indent}      ${rest}`)
      exampleLines.push(rest)
    }
    i++
    continue
  }
  
  // 在 example 块中
  if (inExampleBlock) {
    const currentIndent = line.match(/^(\s*)/)?.[1]?.length || 0
    
    // 检查是否结束（遇到同级别或更高级别的非空行）
    if (line.trim() === '' || currentIndent <= exampleIndent) {
      // 结束 example 块，转换为 examples 格式
      if (exampleLines.length > 0) {
        // 添加 examples 包装
        const indent = ' '.repeat(exampleIndent)
        newLines.push(`${indent}examples:`)
        newLines.push(`${indent}  default:`)
        newLines.push(`${indent}    summary: 响应示例`)
        newLines.push(`${indent}    value:`)
        
        // 添加示例内容，调整缩进
        exampleLines.forEach(exampleLine => {
          const lineIndent = exampleLine.match(/^(\s*)/)?.[1]?.length || 0
          const content = exampleLine.trim()
          if (content) {
            // 计算新的缩进（在 value 下再缩进2级）
            const newIndent = indent + '      '
            newLines.push(`${newIndent}${content}`)
          }
        })
      }
      
      inExampleBlock = false
      exampleLines = []
      
      // 处理当前行（可能是空行或下一个块）
      if (line.trim() !== '') {
        newLines.push(line)
      } else {
        newLines.push('')
      }
    } else {
      // 继续收集 example 内容
      exampleLines.push(line)
    }
    i++
    continue
  }
  
  // 普通行
  newLines.push(line)
  i++
}

// 处理最后一个 example 块
if (inExampleBlock && exampleLines.length > 0) {
  const indent = ' '.repeat(exampleIndent)
  newLines.push(`${indent}examples:`)
  newLines.push(`${indent}  default:`)
  newLines.push(`${indent}    summary: 响应示例`)
  newLines.push(`${indent}    value:`)
  
  exampleLines.forEach(exampleLine => {
    const content = exampleLine.trim()
    if (content) {
      const newIndent = indent + '      '
      newLines.push(`${newIndent}${content}`)
    }
  })
}

// 写回文件
const newContent = newLines.join('\n')
fs.writeFileSync(openapiPath, newContent, 'utf-8')

console.log('✅ 已将所有 example 转换为 examples 格式')
console.log(`📝 处理了 ${lines.length} 行`)
console.log(`📝 生成了 ${newLines.length} 行`)

