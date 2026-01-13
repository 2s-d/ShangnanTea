const fs = require('fs')
const path = require('path')

/**
 * 修复 examples 格式中 data 下子属性的缩进问题
 * 确保 data: 下的属性有正确的缩进
 */

const openapiPath = path.join(__dirname, '../openapi.yaml')

// 读取文件
let content = fs.readFileSync(openapiPath, 'utf-8')

const lines = content.split('\n')
const newLines = []
let i = 0
let inValueBlock = false
let valueIndent = 0
let dataIndent = 0

while (i < lines.length) {
  const line = lines[i]
  
  // 检测 value: 开始
  if (line.match(/^(\s+)value:\s*$/)) {
    inValueBlock = true
    valueIndent = line.match(/^(\s+)/)[1].length
    newLines.push(line)
    i++
    continue
  }
  
  // 在 value 块中
  if (inValueBlock) {
    const currentIndent = line.match(/^(\s*)/)[1].length
    const trimmed = line.trim()
    
    // 检测 data: 行
    if (trimmed === 'data:' || trimmed.startsWith('data: ')) {
      dataIndent = currentIndent
      newLines.push(line)
      i++
      continue
    }
    
    // 检测 data 下的属性（缩进应该比 data 多2级）
    if (dataIndent > 0 && currentIndent > dataIndent && currentIndent <= dataIndent + 2) {
      // 如果缩进不正确，修复它
      const content = line.substring(currentIndent)
      const correctIndent = ' '.repeat(dataIndent + 2)
      newLines.push(`${correctIndent}${content}`)
      i++
      continue
    }
    
    // 检查是否结束 value 块（遇到同级别或更高级别的键）
    if (line.trim() === '') {
      newLines.push('')
      i++
      continue
    }
    
    // 如果遇到同级别或更高级别的键，结束 value 块
    if (currentIndent <= valueIndent && line.match(/^\s+[a-zA-Z-]+:/)) {
      inValueBlock = false
      dataIndent = 0
      newLines.push(line)
      i++
      continue
    }
    
    // 其他情况保持原样
    newLines.push(line)
    i++
    continue
  }
  
  // 普通行
  newLines.push(line)
  i++
}

// 写回文件
const newContent = newLines.join('\n')
fs.writeFileSync(openapiPath, newContent, 'utf-8')

console.log('✅ 已修复 examples 格式中 data 下子属性的缩进问题')

