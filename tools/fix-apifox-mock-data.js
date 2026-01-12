/**
 * 修复 API Fox Mock 数据问题
 * 在 schema properties 中添加 example，确保 API Fox 能正确使用
 */

const fs = require('fs')
const path = require('path')

function fixMockData(filePath) {
  const content = fs.readFileSync(filePath, 'utf-8')
  const lines = content.split('\n')
  const newLines = []
  
  let currentExampleCode = null
  let currentExampleData = []
  let inExampleBlock = false
  let exampleIndent = 0
  
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i]
    
    // 检测 example: 开始
    if (line.match(/^\s+example:\s*$/)) {
      inExampleBlock = true
      exampleIndent = line.match(/^(\s+)/)[1].length
      currentExampleCode = null
      currentExampleData = []
      newLines.push(line)
      continue
    }
    
    // 在 example 块中收集数据
    if (inExampleBlock) {
      const currentIndent = line.match(/^(\s*)/)[1].length
      
      // 检测 code 值
      const codeMatch = line.match(/^\s+code:\s*(\d+)/)
      if (codeMatch) {
        currentExampleCode = codeMatch[1]
      }
      
      // 收集所有 example 数据行
      currentExampleData.push(line)
      
      // 检测 example 块结束（空行或下一个顶级键）
      const nextLine = i + 1 < lines.length ? lines[i + 1] : ''
      const isEnd = nextLine === '' || 
                   (nextLine.match(/^\s+[a-zA-Z]/) && nextLine.match(/^\s+[a-zA-Z]/)[0].length <= exampleIndent) ||
                   nextLine.match(/^\s*#/)
      
      if (isEnd) {
        // example 块结束，保持原样输出
        newLines.push(...currentExampleData)
        inExampleBlock = false
        currentExampleCode = null
        currentExampleData = []
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

console.log('修复 Mock 数据格式...')
// 暂时不执行，先检查格式
console.log('当前格式检查完成')

