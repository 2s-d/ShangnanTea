/**
 * 修复 API Fox Mock 数据问题
 * 将 example 改为 examples，并确保数据正确
 */

const fs = require('fs')
const path = require('path')

function fixExamples(filePath) {
  const content = fs.readFileSync(filePath, 'utf-8')
  const lines = content.split('\n')
  const newLines = []
  
  for (let i = 0; i < lines.length; i++) {
    let line = lines[i]
    
    // 将 example: 改为 examples: 并添加名称
    if (line.match(/^\s+example:\s*$/)) {
      const indent = line.match(/^(\s+)/)[1]
      // 改为 examples 格式
      newLines.push(`${indent}examples:`)
      newLines.push(`${indent}  default:`)
      newLines.push(`${indent}    value:`)
      // 下一行开始是实际的 example 数据，需要增加缩进
      continue
    }
    
    // 处理 example 数据行，需要增加缩进
    if (i > 0 && lines[i-1] && lines[i-1].match(/^\s+value:\s*$/)) {
      // 这是 example 数据的第一行，保持原样
      newLines.push(line)
      continue
    }
    
    // 检查是否在 example 数据块中
    let inExampleBlock = false
    for (let j = i - 1; j >= 0; j--) {
      if (lines[j].match(/^\s+value:\s*$/)) {
        inExampleBlock = true
        break
      }
      if (lines[j].match(/^\s+(schema|example|examples):\s*$/)) {
        break
      }
    }
    
    if (inExampleBlock) {
      // 在 example 数据块中，需要增加缩进
      const indent = line.match(/^(\s+)/)[1]
      newLines.push(`    ${line.trim()}`)
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

console.log('修复 example 格式...')
// 暂时不执行，先检查一下是否需要
console.log('注意：API Fox 可能需要 examples 格式，但先检查当前格式是否正确')

